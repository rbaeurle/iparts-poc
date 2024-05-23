/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.ExtendedDataTypeLoadType;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsFactoryDataTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsColorTableFactoryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataColorTableFactory;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataColorTableFactoryList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPem;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPemList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsPemId;
import de.docware.framework.modules.db.*;
import de.docware.framework.modules.db.etkrecord.EtkRecord;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.CanceledException;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;
import de.docware.util.sql.terms.Condition;

import java.util.*;
import java.util.stream.Collectors;

import static de.docware.framework.modules.db.DBDatabaseDomain.MAIN;

/**
 * Helfer zum Verarbeiten von PEM Stammdaten
 */
public class PEMDataHelper implements iPartsConst {

    public static final String AS_PEM_PREFIX = "IZV"; // Prefix für in iParts erzeugte PEMs

    private static final Set<String> INFINITE_PEM_DATES = new HashSet<>();

    static {
        INFINITE_PEM_DATES.add("99999999");
        INFINITE_PEM_DATES.add("99991231");
    }

    /**
     * Holt einzigartig die Kombination aus Werk und PEMA aus den Werkseinsatzdaten.
     *
     * @param project
     * @param source
     * @param dataId
     */
    public static DBDataObjectAttributesList loadDistinctFactoryPemListForSourceAndDataId(EtkProject project, String source, String dataId) {
        // Folgendes SQL-Statement wird hier zusammengebaut:
        // select distinct DFD_FACTORY, DFD_PEMA from DA_FACTORY_DATA where DFD_SOURCE = 'DIALOG' and DFD_DATA_ID = 'BCTP' order by DFD_FACTORY, DFD_PEMA;
        String[] selectFields = new String[]{ FIELD_DFD_FACTORY, FIELD_DFD_PEMA };
        String[] whereFields = new String[]{ FIELD_DFD_SOURCE, FIELD_DFD_DATA_ID };
        String[] whereValues = new String[]{ source, dataId };
        DBDataObjectAttributesList attributesList = project.getDbLayer().getAttributesList(TABLE_DA_FACTORY_DATA,
                                                                                           selectFields, whereFields, whereValues,
                                                                                           ExtendedDataTypeLoadType.MARK,
                                                                                           false, true);
        // Nach Werk und PEM ab sortieren
        String[] sortFields = new String[]{ FIELD_DFD_FACTORY, FIELD_DFD_PEMA };
        attributesList.sortBetterSort(sortFields, false);

        return attributesList;
    }

    /**
     * Lädt den Werkseinsatzdatensatz mit dem höchsten ADAT zur übergebenen PEM ab und zum übergebenen Werk. Es werden
     * nur die Attribute zurückgeliefert, die für den PEM Stamm benötigt werden.
     *
     * @param project
     * @param pem
     * @param factory
     * @return
     */
    private static DBDataObjectAttributes loadHighestADATEntryForPEMAndFactory(EtkProject project, String pem, String factory) {
        // Folgendes SQL-Statement wird hier zusammengebaut:
        // select DFD_PEMA, DFD_FACTORY, DFD_PEMTA, DFD_STCA, DFD_ADAT from DA_FACTORY_DATA where DFD_PEMA = pem and DFD_FACTORY = fatory and DFD_SOURCE = 'DIALOG' and DFD_DATA_ID = 'BCTP' order by DFD_ADAT DESC;
        String[] selectFields = new String[]{ FIELD_DFD_PEMA, FIELD_DFD_FACTORY, FIELD_DFD_PEMTA, FIELD_DFD_STCA, FIELD_DFD_ADAT };
        DBSQLQuery query = project.getEtkDbs().getDBForDomain(MAIN).getNewQuery();
        query.select(selectFields).from(TABLE_DA_FACTORY_DATA);
        query.where(new Condition(FIELD_DFD_PEMA, Condition.OPERATOR_EQUALS, pem));
        query.and(new Condition(FIELD_DFD_FACTORY, Condition.OPERATOR_EQUALS, factory));
        query.and(new Condition(FIELD_DFD_SOURCE, Condition.OPERATOR_EQUALS, iPartsImportDataOrigin.DIALOG.getOrigin()));
        query.and(new Condition(FIELD_DFD_DATA_ID, Condition.OPERATOR_EQUALS, iPartsFactoryDataTypes.FACTORY_DATA_CONSTRUCTION.getDbValue()));
        query.orderBy(new String[]{ FIELD_DFD_ADAT }, new boolean[]{ true });
        DBDataSetCancelable dataSet = null;
        try {
            // Query ausführen
            dataSet = query.executeQueryCancelable();
            // Falls es Treffer gibt, nur den obersten Treffer heranziehen. Alle Treffer sind via Query nach ADAT
            // absteigend sortiert, aher brauchen wir nur den obersten bzw. ersten Treffer.
            if (dataSet != null) {
                // Ersten Treffer via Verbindung zur DB holen
                dataSet.next();
                EtkRecord record = dataSet.getRecord(selectFields);
                return DBDataObjectAttributes.getFromRecord(record, DBActionOrigin.FROM_DB);
            }
        } catch (CanceledException e) {
            Logger.getLogger().throwRuntimeException(e);
        } finally {
            // Verbindugn schließen
            if (dataSet != null) {
                dataSet.close();
            }
        }

        return null;
    }

    /**
     * Lädt alle Variantentabellen Werkseinsatzdaten aus der Datenbank und erzeugt aus den Datensätzen {@link PEMImportRecord}s.
     * Diese werden zur übergebenen <code>pemData</code> Map hinzugefügt.
     *
     * @param project
     * @param pemData
     */
    public static void addPEMDataFromColorFactoryData(EtkProject project, Map<iPartsPemId, PEMImportRecord> pemData) {
        // Die Liste der Werkseinsatzdaten für Farben holen
        List<iPartsDataColorTableFactory> dataList =
                loadColorTableFactoryDataListForSourceAndIds(project, iPartsImportDataOrigin.DIALOG.getOrigin(),
                                                             iPartsFactoryDataTypes.COLORTABLE_CONTENT.getDbValue(), // "X9P"
                                                             iPartsFactoryDataTypes.COLORTABLE_PART.getDbValue(),    // "X10P"
                                                             DBActionOrigin.FROM_DB);
        if (dataList != null) {
            for (iPartsDataColorTableFactory colorFactoryData : dataList) {
                // ADAT
                String changeDate = colorFactoryData.getAsId().getAdat();
                // Werk
                String factory = colorFactoryData.getAsId().getFactory();
                // PEM ab
                String pemFrom = colorFactoryData.getFieldValue(FIELD_DCCF_PEMA);
                // PEM Datum ab
                String pemDate = colorFactoryData.getFieldValue(FIELD_DCCF_PEMTA);
                // Steuercode ab
                String controlCode = colorFactoryData.getFieldValue(FIELD_DCCF_STCA);
                addPemData(pemData, changeDate, pemFrom, pemDate, controlCode, factory);
            }
        }
    }

    /**
     * Lädt den Werkseinsatzdatensatz mit dem höchsten ADAT zur übergebenen PEM ab und zum übergebenen Werk und erzeugt
     * daraus ein {@link PEMImportRecord}. Dieses wird zur übergebenen <code>pemData</code> Map hinzugefügt.
     *
     * @param project
     * @param pemData
     * @param pemA
     * @param factory
     */
    public static void addPEMDataFromFactoryDataWithHighestADAT(EtkProject project, Map<iPartsPemId, PEMImportRecord> pemData, String pemA, String factory) {
        if (pemData != null) {
            DBDataObjectAttributes attributesFromEntryWithHighestADAT = loadHighestADATEntryForPEMAndFactory(project, pemA, factory);
            if (attributesFromEntryWithHighestADAT != null) {
                String changeDate = attributesFromEntryWithHighestADAT.getFieldValue(FIELD_DFD_ADAT);
                String pemDate = attributesFromEntryWithHighestADAT.getFieldValue(FIELD_DFD_PEMTA);
                String controlCode = attributesFromEntryWithHighestADAT.getFieldValue(FIELD_DFD_STCA);
                addPemData(pemData, changeDate, pemA, pemDate, controlCode, factory);
            }
        }
    }

    /**
     * Erzeugt aus dem übergebenen {@link PEMImportRecord} ein {@link iPartsDataPem} Objekt. Optional kann das eigentliche
     * {@link iPartsDataPem}, das vorher geladen wurde, für mögliche Aktualisierungen ebenfalls übergeben werden.
     * Außerdem kann via <code>checkIfExists</code> bestimmt werden, ob das {@link iPartsDataPem} schon in der DB existiert.
     *
     * @param project
     * @param pemImportRecord
     * @param existingPEMDataObject
     * @param checkIfExists
     * @return
     */
    public static iPartsDataPem createPEMDataObjectFromDIALOGImportRecord(EtkProject project, PEMImportRecord pemImportRecord,
                                                                          iPartsDataPem existingPEMDataObject, boolean checkIfExists) {
        iPartsPemId pemId = new iPartsPemId(pemImportRecord.getPem(), pemImportRecord.getFactory());
        iPartsDataPem dataPem = (existingPEMDataObject == null) ? new iPartsDataPem(project, pemId) : existingPEMDataObject;
        if (checkIfExists) {
            if (!dataPem.existsInDB()) {
                // PEM Stamm gibt es noch nicht -> anlegen
                dataPem.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            } else {
                // PEM Stamm existiert -> Prüfen, ob Datensatz in DB veraltet
                String currentChangeDate = dataPem.getFieldValue(FIELD_DPM_ADAT);
                String currentChangeDateSortString = Utils.toSortString(currentChangeDate);
                String newDateSortString = Utils.toSortString(convertToValidPEMDateLength(pemImportRecord.getChangeDate()));

                if (currentChangeDateSortString.compareTo(newDateSortString) > 0) {
                    return null;
                }
            }
        } else {
            dataPem.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }
        dataPem.setFieldValue(FIELD_DPM_PEM_DATE, pemImportRecord.getPemDate(), DBActionOrigin.FROM_EDIT);
        dataPem.setFieldValue(FIELD_DPM_STC, pemImportRecord.getControlCode(), DBActionOrigin.FROM_EDIT);
        dataPem.setFieldValue(FIELD_DPM_ADAT, pemImportRecord.getChangeDate(), DBActionOrigin.FROM_EDIT);
        // Die Importdatenquelle auf IPARTS_MB setzen, da PEMs nur über DIALOG Importer kommen
        dataPem.setFieldValue(FIELD_DPM_SOURCE, iPartsImportDataOrigin.IPARTS_MB.getOrigin(), DBActionOrigin.FROM_EDIT);
        return dataPem;
    }

    /**
     * Überprüft, ob die Stammdaten zur übergebenen PEM neuer sind als die bestehenden PEM Stammdaten
     * in <code>pemData</code>. Falls ja, wird pro PEM ab ein neuer PEM Stammdatensatz angelegt.
     *
     * @param pemData
     * @param changeDate
     * @param pem
     * @param pemDate
     * @param controlCode
     * @param factory
     */
    public static void addPemData(Map<iPartsPemId, PEMImportRecord> pemData, String changeDate, String pem, String pemDate,
                                  String controlCode, String factory) {
        if (pemData != null) {
            if (StrUtils.isValid(pem)) {
                // Schlüssel aus PEM und Werk erzeugen
                iPartsPemId pemFactoryKey = new iPartsPemId(pem, factory);
                PEMImportRecord singlePemData = pemData.get(pemFactoryKey);
                if (singlePemData != null) {
                    String currentChangeDate = singlePemData.getChangeDate();
                    // Check, ob schon ein neuerer PEM Stamm zu PEM und Werk gefunden wurde. Falls ja, überspringen.
                    if (Utils.toSortString(currentChangeDate).compareTo(Utils.toSortString(changeDate)) > 0) {
                        return;
                    }
                }
                singlePemData = new PEMImportRecord(pem, changeDate, pemDate, controlCode, factory);
                pemData.put(pemFactoryKey, singlePemData);
            }
        }
    }

    /**
     * Holt die {@Link iPartsDataColorTableFactory} Datenobjekte aus den Werkseinsatzdaten der Farben.
     * Das Besondere ist die WHERE-Bedingung mit einer OR-Verknüpfung.
     *
     * @param project
     * @param source
     * @param dataId1
     * @param dataId2
     * @param origin
     * @return
     */
    public static List<iPartsDataColorTableFactory> loadColorTableFactoryDataListForSourceAndIds(final EtkProject project, String source, String dataId1,
                                                                                                 String dataId2, final DBActionOrigin origin) {

        final List<iPartsDataColorTableFactory> result = new DwList<>();

        // Folgendes Select-Statement wird hier zusammengebaut:
        // select * from DA_COLORTABLE_FACTORY where DCCF_SOURCE = 'DIALOG' and ( DCCF_DATA_ID = 'X9P' or DCCF_DATA_ID = 'X10P');

        // -----------------------------------------------------------------
        // Die WHERE-Fields in der ersten Dimension werden ver-UND-et!
        // -----------------------------------------------------------------
        // Die WHERE-Fields für: "where DCCF_SOURCE = 'DIALOG'" and ...
        String[][] whereFields = new String[2][];
        EtkDataObjectList.addElemsTo2dArray(whereFields, 0, TableAndFieldName.make(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_SOURCE));

        // -----------------------------------------------------------------
        // Die WHERE-Fields in der zweiten Dimension werden ver-ODER-t
        // -----------------------------------------------------------------
        // ... und die WHERE-Fields für:  and ... "( DCCF_DATA_ID = 'X9P' or DCCF_DATA_ID = 'X10P')"
        EtkDataObjectList.addElemsTo2dArray(whereFields, 1,
                                            TableAndFieldName.make(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_DATA_ID),
                                            TableAndFieldName.make(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_DATA_ID));

        // Die WHERE-Values
        String[][] whereValues = new String[2][];
        // DCCF_SOURCE = 'DIALOG'
        EtkDataObjectList.addElemsTo2dArray(whereValues, 0, source);

        // and ( DCCF_DATA_ID = 'X9P' or DCCF_DATA_ID = 'X10P')
        EtkDataObjectList.addElemsTo2dArray(whereValues, 1, dataId1, dataId2);

        EtkDataObjectList.FoundAttributesCallback foundAttributesCallback = new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {

                // Ein iPartsDataColorTableFactory-Objekt aus den Attributen erzeugen und zu den Ergebnissen hinzufügen
                iPartsColorTableFactoryId colorTableFactoryId = new iPartsColorTableFactoryId(attributes.getFieldValue(FIELD_DCCF_TABLE_ID),
                                                                                              attributes.getFieldValue(FIELD_DCCF_POS),
                                                                                              attributes.getFieldValue(FIELD_DCCF_FACTORY),
                                                                                              attributes.getFieldValue(FIELD_DCCF_ADAT),
                                                                                              attributes.getFieldValue(FIELD_DCCF_DATA_ID),
                                                                                              attributes.getFieldValue(FIELD_DCCF_SDATA));

                iPartsDataColorTableFactory colorTableFactory = new iPartsDataColorTableFactory(project, colorTableFactoryId);
                colorTableFactory.assignAttributes(project, attributes, true, origin);
                result.add(colorTableFactory);

                return false;
            }
        };

        iPartsDataColorTableFactoryList colorTableFactoryList = new iPartsDataColorTableFactoryList();
        colorTableFactoryList.searchSortAndFillWithJoin(project, null, null, whereFields, whereValues,
                                                        false, null, null, false,
                                                        null, false, false, false,
                                                        foundAttributesCallback,
                                                        false);
        return result;
    }

    /**
     * Erzeugt aus den Werkseinsatzdaten und den Werkseinsatzdaten der Farben die Einträge für die PEM-Stammdaten.
     * DAIMLER-8458, Anlage PEM Stammdaten-Tabelle und Befüllung über DIALOG Urladung/Delta
     *
     * @param project
     * @return
     */
    public static void generatePemMasterdata(final EtkProject project) {
        if (MessageDialog.showYesNo("!!Möchten Sie die PEM-Stammdaten wirklich erzeugen?") == ModalResult.YES) {
            final EtkMessageLogForm messageLogForm = new EtkMessageLogForm("!!PEM-Stammdaten",
                                                                           "Erzeuge PEM-Stammdaten aus " +
                                                                           "Werkseinsatzdaten und Werkseinsatzdaten für Farben",
                                                                           null);
            messageLogForm.showModal(new FrameworkRunnable() {
                @Override
                public void run(FrameworkThread thread) {

                    messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Lade bestehende PEM-Stammdatensätze."),
                                                               MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                    // Die bestehenden Werte aus der Datenbank holen.
                    iPartsDataPemList pemDataList = iPartsDataPemList.loadDataPemList(project, DBDataObjectList.LoadType.COMPLETE);
                    Map<iPartsPemId, iPartsDataPem> idToPemFactoryMap = new HashMap<>();
                    for (iPartsDataPem dataPem : pemDataList) {
                        iPartsPemId key = new iPartsPemId(dataPem.getAsId().getPEM(), dataPem.getAsId().getFactoryNo());
                        idToPemFactoryMap.put(key, dataPem);
                    }

                    messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!%1 bestehende " +
                                                                                            "PEM-Stammdatensätze gefunden.",
                                                                                            String.valueOf(pemDataList.size())),
                                                               MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);

                    messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Suche passende " +
                                                                                            "Werkseinsatzdaten in %1.",
                                                                                            TABLE_DA_FACTORY_DATA),
                                                               MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);


                    // Map um alle möglichen PEM zu Werk Kombinationen zu halten
                    Map<iPartsPemId, PEMImportRecord> foundPEMs = new HashMap<>();
                    // Werkseinsatzdaten von Stücklistenpositionen
                    // Diese Liste enthält EINZIGARTIG die Kombination aus FACTORY und PEMA aus DA_FACTORY_DATA
                    DBDataObjectAttributesList attributesList
                            = loadDistinctFactoryPemListForSourceAndDataId(project,
                                                                           iPartsImportDataOrigin.DIALOG.getOrigin(),
                                                                           iPartsFactoryDataTypes.FACTORY_DATA_CONSTRUCTION.getDbValue());
                    messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!%1 einzgartige Kombinationen aus Werk und PEM gefunden.",
                                                                                            String.valueOf(attributesList.getCount())),
                                                               MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                    // Durchlaufe alle gefunden Einträge und erzeuge PEM zu Werk Datensätze
                    int rowCounter = 0;
                    for (DBDataObjectAttributes attributes : attributesList) {

                        // Wenn jemand den Abbrechen-Button in der Oberfläche gedrückt hat einfach beenden ohne zu speichern.
                        if (Thread.currentThread().isInterrupted()) {
                            return;
                        }
                        String factory = attributes.getFieldValue(FIELD_DFD_FACTORY);
                        String pemA = attributes.getFieldValue(FIELD_DFD_PEMA);
                        rowCounter++;
                        // PEM zu Werk Datensatz erzeugen und hinzufügen
                        addPEMDataFromFactoryDataWithHighestADAT(project, foundPEMs, pemA, factory);
                        if ((rowCounter % 1000) == 0) {
                            writeHandledMessage(rowCounter, attributesList.getCount());
                        }

                    }
                    writeHandledMessage(attributesList.getCount(), attributesList.getCount());

                    int factoryDataCount = foundPEMs.size();
                    messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!%1 relevante " +
                                                                                            "Datensätze ermittelt.",
                                                                                            String.valueOf(factoryDataCount)),
                                                               MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);


                    // Werkseinsatzdaten von Variantentabellen
                    messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Suche passende " +
                                                                                            "Werkseinsatzdaten in %1.",
                                                                                            TABLE_DA_COLORTABLE_FACTORY),
                                                               MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);

                    addPEMDataFromColorFactoryData(project, foundPEMs);

                    int colorFactoryDataCount = foundPEMs.size() - factoryDataCount;
                    messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!%1 relevante Datensätze gefunden.",
                                                                                            String.valueOf(colorFactoryDataCount)),
                                                               MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);

                    messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Analysiere %1 PEM" +
                                                                                            " Stammdaten.",
                                                                                            String.valueOf(foundPEMs.size())),
                                                               MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                    savePEMDataSets(project, foundPEMs, idToPemFactoryMap);

                    messageLogForm.getMessageLog().fireMessageWithSeparators(TranslationHandler.translate("!!Datenerzeugung abgeschlossen."),
                                                                             MessageLogOption.TIME_STAMP);
                }

                private void writeHandledMessage(int rowCounter, int attCount) {
                    messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!%1 von %2 verarbeitet",
                                                                                            String.valueOf(rowCounter),
                                                                                            String.valueOf(attCount)),
                                                               MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                }

                /**
                 * Erzeugt und speichert die gefundenen {@link iPartsDataPem} Objekte.
                 * @param project
                 * @param foundPEMs
                 * @param idToPemFactoryMap
                 */
                private void savePEMDataSets(EtkProject project, Map<iPartsPemId, PEMImportRecord> foundPEMs,
                                             Map<iPartsPemId, iPartsDataPem> idToPemFactoryMap) {
                    iPartsDataPemList pemList = new iPartsDataPemList();
                    int count = 0;
                    int dbCount = 0;
                    int maxCount = foundPEMs.size();
                    for (Map.Entry<iPartsPemId, PEMImportRecord> singlePEMData : foundPEMs.entrySet()) {
                        if (Thread.currentThread().isInterrupted()) {
                            return;
                        }

                        iPartsDataPem existingEntry = idToPemFactoryMap.get(singlePEMData.getKey());
                        iPartsDataPem dataPem = createPEMDataObjectFromDIALOGImportRecord(project,
                                                                                          singlePEMData.getValue(),
                                                                                          existingEntry,
                                                                                          (existingEntry != null));
                        if (dataPem != null) {
                            // Alle 500 Einträge speichern, falls etwas schief laufen sollte
                            if (pemList.size() >= 500) {
                                savePEMList(project, pemList);
                                pemList.saveToDB(project);
                                pemList.clear(DBActionOrigin.FROM_EDIT);
                                writeHandledMessage(count, maxCount);
                            }
                            if (dataPem.isModified()) {
                                dbCount++;
                                pemList.add(dataPem, DBActionOrigin.FROM_EDIT);
                            }
                        }
                        count++;
                    }
                    messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!%1 PEM Stammdaten " +
                                                                                            "analysiert. %2 gespeichert",
                                                                                            String.valueOf(foundPEMs.size()),
                                                                                            String.valueOf(dbCount)),
                                                               MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                    savePEMList(project, pemList);
                }

                private void savePEMList(EtkProject project, iPartsDataPemList pemList) {
                    project.getDbLayer().startTransaction();
                    project.getDbLayer().startBatchStatement();
                    try {
                        if (!pemList.isEmpty()) {
                            pemList.saveToDB(project);
                            pemList.clear(DBActionOrigin.FROM_EDIT);
                        }
                        project.getDbLayer().endBatchStatement();
                        project.getDbLayer().commit();
                    } catch (Exception e) {
                        project.getDbLayer().cancelBatchStatement();
                        project.getDbLayer().rollback();
                        throw e;
                    }
                }
            });
        }
    }

    /**
     * Konvertiert den übergeben String in ein gültiges Datum für den PEM Stamm.
     *
     * @param dateValue
     * @return
     */
    private static String convertToValidPEMDateLength(String dateValue) {
        if (StrUtils.isEmpty(dateValue) || INFINITE_PEM_DATES.contains(dateValue)) {
            return "";
        } else {
            if (dateValue.length() < 14) {
                return StrUtils.padStringWithCharsUpToLength(dateValue, '0', 14);
            } else if (dateValue.length() > 14) {
                return StrUtils.copySubString(dateValue, 0, 14);
            }
        }
        return dateValue;
    }

    /**
     * Liefert alle PEMs zum Werk unter der Berücksichtigung des PEM Ursprungs <code>pemDataOrigin</code>
     *
     * @param project
     * @param factory
     * @param pemDataOrigin
     * @return
     */
    public static Map<String, iPartsDataPem> getPEMsForPEMOrigin(EtkProject project, String factory, PEMDataOrigin pemDataOrigin) {
        List<iPartsDataPem> dataPemList;
        if (pemDataOrigin == null) {
            dataPemList = iPartsDataPemList.loadDataPemListWithoutPEMOrigin(project, factory).getAsList();
        } else if (pemDataOrigin.isSA()) {
            // Bei freien SAs nur die PEMs laden, die zur SA gehören
            dataPemList = iPartsDataPemList.loadDataPemListWithPEMOrigin(project, factory, pemDataOrigin.getPemOriginValue()).getAsList();
        } else {
            // Bei ELDAS alles Einträge laden, die einen Bezug eingetragen haben. Hier werden auch PEMs von SAs
            // geladen, weil wir nicht garantieren können, dass Produkte nicht mit "Z" anfangen.
            dataPemList = iPartsDataPemList.loadDataPemListForEldas(project, factory).getAsList();

            // Jetzt alle SA PEMs ausfiltern
            dataPemList = dataPemList.stream()
                    .filter(pemEntry -> !iPartsNumberHelper.isValidSa(pemEntry.getFieldValue(FIELD_DPM_PRODUCT_NO), true))
                    .collect(Collectors.toList());
        }
        Map<String, iPartsDataPem> pemMap = new HashMap<>();
        for (iPartsDataPem dataPem : dataPemList) {
            pemMap.put(dataPem.getAsId().getPEM(), dataPem);
        }

        return pemMap;
    }

    public static boolean isIZVPem(DBDataObjectAttributes attributes) {
        if (!attributes.fieldExists(FIELD_DPM_PEM)) {
            return false;
        }
        String pem = attributes.getFieldValue(FIELD_DPM_PEM);
        return isIZVPem(pem);
    }

    public static boolean isIZVPem(String pem) {
        return StrUtils.isValid(pem) && pem.startsWith(AS_PEM_PREFIX);
    }

    public static class PEMImportRecord {

        private String changeDate;
        private String pem;
        private String pemDate;
        private String controlCode;
        private String factory;

        public PEMImportRecord(String pem, String changeDate, String pemDate, String controlCode, String factory) {
            this.pem = pem;
            this.changeDate = changeDate;
            setPemDate(pemDate);
            this.controlCode = controlCode;
            this.factory = factory;
        }

        /**
         * Überprüft das pemDate auf gültige Werte, leert ungültige und füllt auf 14 Zeichen auf.
         *
         * @param pemDate
         */
        private void setPemDate(String pemDate) {
            this.pemDate = convertToValidPEMDateLength(pemDate);
        }

        public String getChangeDate() {
            return changeDate;
        }

        public String getPem() {
            return pem;
        }

        public String getPemDate() {
            return pemDate;
        }

        public String getControlCode() {
            return controlCode;
        }

        public String getFactory() {
            return factory;
        }
    }


    /**
     * Hilfsklasse für den Ursprung einer erzeugten IZV PEM
     */
    public static class PEMDataOrigin {

        private final String pemOriginValue;
        private final boolean isSA;

        public PEMDataOrigin(String pemOriginValue) {
            this.pemOriginValue = pemOriginValue;
            this.isSA = iPartsNumberHelper.isValidSa(pemOriginValue, true);
        }

        public String getPemOriginValue() {
            return pemOriginValue;
        }

        public boolean isSA() {
            return isSA;
        }
    }
}
