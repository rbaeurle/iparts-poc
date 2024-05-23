/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.importer;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.SetOfEnumDataType;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsDataCodeList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ASUsageHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsAggTypeMappingCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.config.db.EtkFieldType;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateUtils;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.SQLStringConvert;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Importhelper mit Funktionen, die von DIALOG, EDS/BCS, MAD und PRIMUS Importer genutzt werden.
 */
public class iPartsMainImportHelper implements iPartsConst {

    public static final int ELEMNTS_PER_LINE = 20; // Anzahl Nummern pro Zeile für Ausgabe in Importer Log-Datei
    public static final int LEFT_BLANKS_COUNT = 20; // Einrückung links wegen Datum in Importer Log-Datei
    public static final int MAX_ELEMS_FOR_SHOW = 10;
    protected static final String AS_PEM_PREFIX = "ZV";
    protected static final String THEFT_REL_FLAG_VALUE_FALSE = "N";
    private static final String PART_IMAGE_DATE_FORMAT = "yyMMdd";
    private static final String IMAGE_DATE_PREVIOUS_CENTURY = "19";
    private static final String IMAGE_DATE_CURRENT_CENTURY = "20";

    public static String buildPartNumberList(EtkProject project, List<String> matNoList) {
        if (!matNoList.isEmpty()) {
            StringBuilder str = new StringBuilder();
            int partNoCount = 0;
            str.append("   (");
            for (String matNo : matNoList) {
                if (!StrUtils.isEmpty(matNo)) {
                    if (partNoCount > MAX_ELEMS_FOR_SHOW) {
                        str.append("...");
                        break;
                    }
                    if (partNoCount > 0) {
                        str.append(", ");
                    }
                    str.append(iPartsNumberHelper.formatPartNo(project, matNo));
                    partNoCount++;
                }
            }
            str.append(")");
            return str.toString();
        }
        return "";
    }

    /**
     * bildet aus einer Liste von Nummern die kommaseparierte Liste für die Import-Logdatei
     * pro Zeile werden 20 Nummern ausgegeben
     * Beispiel: aus der Liste a,b,c,d,e,f,g werden die Ausgabezeilen (bei 3 Nummern/Zeile)
     * a, b, c,
     * d, e, f
     * g          gemacht.
     *
     * @param numberList
     * @return
     */
    public static StringBuilder buildNumberListForLogFile(Set<String> numberList) {
        return buildNumberListForLogFile(new DwList<>(numberList));
    }

    public static StringBuilder buildNumberListForLogFile(List<String> numberList) {
        StringBuilder str = new StringBuilder();
        StringBuilder line = new StringBuilder();
        int elemCount = 0;
        for (String number : numberList) {
            if (!line.toString().isEmpty()) {
                line.append(", ");
            }
            line.append(number);
            elemCount++;
            if (elemCount >= ELEMNTS_PER_LINE) {
                if (!str.toString().isEmpty()) {
                    str.append("\n");
                    str.append(StrUtils.leftFill("", LEFT_BLANKS_COUNT, ' '));
                }
                str.append(line.toString());
                elemCount = 0;
                line = new StringBuilder();
            }
        }
        if (!line.toString().isEmpty()) {
            if (!str.toString().isEmpty()) {
                str.append("\n");
                str.append(StrUtils.leftFill("", LEFT_BLANKS_COUNT, ' '));
            }
            str.append(line.toString());
        }
        return str;
    }


    private EtkProject project;
    protected Map<String, String> mapping;
    protected String tableName;
    private iPartsNumberHelper numberHelper;
    private boolean isNewerVersion;
    private ASUsageHelper asUsageHelper;
    private iPartsAggTypeMappingCache aggTypeMappingCache;

    public iPartsMainImportHelper(EtkProject project, Map<String, String> mapping, String tableName) {
        this.project = project;
        this.mapping = mapping;
        this.tableName = tableName;
        this.numberHelper = new iPartsNumberHelper();
        this.asUsageHelper = new ASUsageHelper(project);
        if (project != null) { // Bei manchen Unittests ist project=null
            this.aggTypeMappingCache = iPartsAggTypeMappingCache.getInstance(project);
        }
    }

    /**
     * Die Import-Werte komplett in einen Datensatz übertragen.
     * <p>
     * Das Mapping besitzt dabei den folgenden Aufbau:
     * Key = DB-FieldName, Value = Import-Spaltenname
     *
     * @param dataObject
     * @param importRec
     * @param langDef
     */
    public void fillOverrideCompleteDataReverse(EtkDataObject dataObject, Map<String, String> importRec, Language langDef) {
        // über alle Mapping Felder
        for (Map.Entry<String, String> mappingForField : mapping.entrySet()) {
            String dbDestFieldName = mappingForField.getKey();
            String importFieldName = mappingForField.getValue();
            String value = importRec.get(importFieldName);
            importValue(dataObject, importFieldName, dbDestFieldName, value, langDef);
        }
    }

    /**
     * Importiert den übergebenen Wert in das angegebene Ziel-DB-Feld mit evtl. Korrekturen abhängig von dessen Typ.
     *
     * @param dataObject      Ziel-{@link EtkDataObject}, in das der Wert importiert werden soll
     * @param importFieldName Feldname in den Importdaten
     * @param dbDestFieldName Feldname in der Ziel-Tabelle
     * @param value           Wert aus den Importdaten
     * @param langDef
     * @return
     */
    protected void importValue(EtkDataObject dataObject, String importFieldName, String dbDestFieldName, String value, Language langDef) {
        if (value != null) {
            // Sonderbehandlung für MultiLanguage-Felder
            EtkDatabaseField destFieldDescription = getProject().getFieldDescription(tableName, dbDestFieldName);
            if (destFieldDescription.isMultiLanguage()) {
                fillOverrideOneLanguageText(dataObject, langDef, dbDestFieldName, value);
            } else {
                // Sonderbehandlung für spezielle Import Felder
                value = handleValueOfSpecialField(importFieldName, value);
                value = extractCorrespondingSubstring(importFieldName, dbDestFieldName, value);

                // Sonderbehandlung für Booleans, Integer und Double
                if (destFieldDescription.getType() == EtkFieldType.feBoolean) {
                    dataObject.setFieldValueAsBoolean(dbDestFieldName, handleValueOfBooleanField(value), DBActionOrigin.FROM_EDIT);
                } else if (destFieldDescription.getType() == EtkFieldType.feInteger) {
                    dataObject.setFieldValueAsInteger(dbDestFieldName, SQLStringConvert.ppStringToInt(value), DBActionOrigin.FROM_EDIT);
                } else if (destFieldDescription.getType() == EtkFieldType.feFloat) {
                    dataObject.setFieldValueAsDouble(dbDestFieldName, SQLStringConvert.ppStringToDouble(value), DBActionOrigin.FROM_EDIT);
                } else if (destFieldDescription.getType() == EtkFieldType.feSetOfEnum) {
                    dataObject.setFieldValueAsSetOfEnum(dbDestFieldName, StrUtils.toStringList(value, SetOfEnumDataType.SOE_DEFAULT_SEPARATOR,
                                                                                               true, true), DBActionOrigin.FROM_EDIT);
                } else {
                    // Attributwert im DataObject setzen
                    dataObject.setFieldValue(dbDestFieldName, value, DBActionOrigin.FROM_EDIT);
                }
            }
        }
    }

    /**
     * einen Multilang Text (destField) zu einer Sprache (langDef) setzen bzw überschreiben
     *
     * @param dataObject
     * @param langDef
     * @param destField  Feld in der Datenbank
     * @param value
     */
    public void fillOverrideOneLanguageText(EtkDataObject dataObject, Language langDef, String destField, String value) {
        String handledValue = handleValueOfMultiLangField(destField, value, langDef);
        if ((langDef != null) && (handledValue != null)) {
            EtkMultiSprache multiSprache = dataObject.getFieldValueAsMultiLanguage(destField);
            if (!handledValue.isEmpty()) {
                multiSprache.setText(langDef.getCode(), handledValue);
                dataObject.setFieldValueAsMultiLanguage(destField, multiSprache, DBActionOrigin.FROM_EDIT);
            } else if (multiSprache.removeLanguage(langDef.getCode())) { // bei leerem String Sprache entfernen
                dataObject.setFieldValueAsMultiLanguage(destField, multiSprache, DBActionOrigin.FROM_EDIT);
            }
        }
    }


    /**
     * Werke zu AS-PEM aus Cache abfragen
     *
     * @param pem
     * @param br
     * @param aa  leer möglich
     * @return null wenn PEM nicht in Cache gefunden
     */
    protected Set<String> getFactoriesForASPemFromCache(Map<String, Set<String>> asPemToFactoriesMap, String pem, String br, String aa) {
        String cacheKey = getASPemToFactoriesCacheKey(pem, br, aa);
        if (asPemToFactoriesMap != null) {
            return asPemToFactoriesMap.get(cacheKey);
        }
        return null;
    }

    public String getASPemToFactoriesCacheKey(String pem, String br, String aa) {
        return StrUtils.makeDelimitedString("_", pem, br, aa);
    }

    /**
     * Überprüft, ob das Material valide für die eigentliche Verarbeitung ist. Valide ist ein Material, wenn es noch nicht
     * in der DB existiert oder ein neueres Freigabedatum hat als der aktuelle Datensatz.
     *
     * @param importer             Importer für Logausgaben
     * @param dataPart
     * @param releaseDate
     * @param releaseFromFieldname
     * @param originOfOtherSources
     * @param recordNo
     * @return
     */
    public boolean checkDataPartValidity(AbstractGenericImporter importer, iPartsDataPart dataPart, String releaseDate,
                                         String releaseFromFieldname, int recordNo, iPartsImportDataOrigin... originOfOtherSources) {
        // Aktuell ist es so, dass anhand des Freigabedatum zwischen Teielstammdaten aus DIALOG und BOM-DB
        // unterschieden wird. Diese Unterscheidung bezieht sich eigentlich nur auf die Felder, die von beiden
        // Importer beschrieben werden. Wenn der neue Datensatz älter ist als der Datensatz in der DB und der Datensatz
        // noch nie aus originOfOtherSources kam, dann können wir hier aussteigen. Ansonsten müssen die Felder importiert werden,
        // die von originOfOtherSources nicht beschrieben werden.
        boolean isNewerVersion = checkIfPartIsNewer(dataPart, releaseDate);
        setIsNewerVersion(isNewerVersion);
        if (!isNewerVersion) {
            boolean hasOlderPartDataConflictingSources = hasOlderDataPartConflictingSources(dataPart, isNewerVersion, originOfOtherSources);
            if (!hasOlderPartDataConflictingSources) {
                if (importer != null) {
                    importer.getMessageLog().fireMessage(importer.translateForLog("!!Record %1 wird übersprungen: \"%2\" " +
                                                                                  "enthält \"%3\" und ist älter als das aktuelle" +
                                                                                  " Freigabedatum \"%4\" (Teilenummer: \"%5\").",
                                                                                  String.valueOf(recordNo), releaseFromFieldname,
                                                                                  releaseDate, dataPart.getFieldValue(iPartsConst.FIELD_M_LAST_MODIFIED),
                                                                                  dataPart.getAsId().getMatNr()),
                                                         MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP,
                                                         MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                }
            }
            return hasOlderPartDataConflictingSources;
        }
        return true;
    }


    /**
     * Überprüft, ob der ältere Datensatz trotz seines Freigabedatums importiert werden kann.
     *
     * @param dataPart
     * @param conflictingSources
     * @param isNewerVersion
     * @return
     */
    public boolean hasOlderDataPartConflictingSources(iPartsDataPart dataPart, boolean isNewerVersion, iPartsImportDataOrigin... conflictingSources) {
        if (!isNewerVersion && (conflictingSources != null)) {
            for (iPartsImportDataOrigin conflictingSource : conflictingSources) {
                if (dataPart.containsFieldValueSetOfEnumValue(iPartsConst.FIELD_M_SOURCE, conflictingSource.getOrigin())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check, ob der übergebene Datensatz neuer ist als der Datensatz in der Datenbank.
     *
     * @param dataPart
     * @param releaseDate
     * @return
     */
    public boolean checkIfPartIsNewer(iPartsDataPart dataPart, String releaseDate) {
        // Freigabedatum-Check: DIALOG und BOM-DB schicken ein Freigabedatum mit. Dieses wird am Material gespeichert
        // und beim erneuten Import von DIALOG bzw. BOM-DB wird dieses Datum geprüft. Hat der zu importierende Datensatz
        // ein älteres Datum, dann wird er nicht importiert.
        if (!dataPart.existsInDB()) {
            dataPart.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        } else {
            String currentReleaseDate = dataPart.getFieldValue(iPartsConst.FIELD_M_LAST_MODIFIED);
            if (currentReleaseDate.compareTo(releaseDate) > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Setzt am Material das Boolean-Feld {@code M_THEFTREL}. Der Wert dafür wird aus dem Wert in {@code M_THEFTRELINFO} berechnet.
     * Laut DAIMLER: "N" = false und Rest = true
     *
     * @param dataPart
     */
    public void setTheftRelFlagForDataPart(iPartsDataPart dataPart) {
        String theftRelInfo = dataPart.getFieldValue(iPartsConst.FIELD_M_THEFTRELINFO);
        boolean theftRelFlag = !theftRelInfo.equals(THEFT_REL_FLAG_VALUE_FALSE);
        dataPart.setFieldValueAsBoolean(iPartsConst.FIELD_M_THEFTREL, theftRelFlag, DBActionOrigin.FROM_EDIT);
    }

    public void cancelImporterDueToIncorrectBCTEKey(AbstractGenericImporter importer, int recordNo) {
        importer.cancelImport(importer.translateForLog("!!Fehler in den Importdaten: Record %1 (ungültiger BCTE Schlüssel)",
                                                       String.valueOf(recordNo)));

    }

    /**
     * Hilfsmethode um {@link iPartsDialogBCTEPrimaryKey} Objekte mit der übergebnen {@link HmMSmId} und den übergebenen
     * Werten zu erzeugen
     *
     * @param importer
     * @param recordNo
     * @param hmMSmId
     * @param pose
     * @param posv
     * @param ww
     * @param etz
     * @param aa
     * @param sda
     * @return
     */
    public iPartsDialogBCTEPrimaryKey getPartListPrimaryBCTEKey(AbstractGenericImporter importer, int recordNo, HmMSmId hmMSmId,
                                                                String pose, String posv, String ww, String etz, String aa, String sda) {

        if ((hmMSmId != null) && hmMSmId.isValidId()) {
            iPartsDialogBCTEPrimaryKey primaryBCTEKey = new iPartsDialogBCTEPrimaryKey(hmMSmId.getSeries(), hmMSmId.getHm(),
                                                                                       hmMSmId.getM(), hmMSmId.getSm(),
                                                                                       pose, posv, ww, etz, aa, sda);

            return primaryBCTEKey;
        } else {
            String msg;
            if (hmMSmId == null) {
                msg = importer.translateForLog("!!Record %1 fehlerhaft (HM/M/SM Strukturobjekt konnte nicht erstellt werden)",
                                               String.valueOf(recordNo));
            } else if (StrUtils.isEmpty(hmMSmId.getSeries())) {
                msg = importer.translateForLog("!!Record %1 fehlerhaft (ungültige Baureihe: %2)",
                                               String.valueOf(recordNo), hmMSmId.getSeries());
            } else {
                msg = importer.translateForLog("!!Record %1 fehlerhaft (ungültige HM/M/SM Struktur: %2)",
                                               String.valueOf(recordNo), hmMSmId.toString());
            }
            importer.getMessageLog().fireMessage(msg, MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            return null;
        }
    }

    /**
     * Cache während des Imports der PODW Daten (Werkseinsatzdaten) aufbauen, der zur Werksermittlung für die folgenden
     * Rückmeldedaten-Importe benötigt wird. Zuordnung (pem, br, aa) ->> Factory
     *
     * @param asPemToFactoriesMap
     * @param pem
     * @param br
     * @param aa
     * @param factory
     */
    public void addASPemToFactoriesCacheEntry(Map<String, Set<String>> asPemToFactoriesMap, String pem, String br, String aa, String factory) {
        if (isASPem(pem) && StrUtils.isValid(factory)) {
            String cacheKey = getASPemToFactoriesCacheKey(pem, br, aa);
            Set<String> currentFactories = asPemToFactoriesMap.get(cacheKey);
            if (currentFactories == null) {
                currentFactories = new TreeSet<>();
                asPemToFactoriesMap.put(cacheKey, currentFactories);
            }
            currentFactories.add(factory);
        }
    }

    /**
     * Wandelt DIALOG Aggregattypen in MAD Aggregattypen um
     *
     * @param aggType
     * @return
     */
    protected String handleAggTypeValue(String aggType) {
        if (StrUtils.isEmpty(aggType)) {
            return aggType;
        }
        String madAggType = aggTypeMappingCache.getAggTypeMapping(aggType);
        if (madAggType == null) {
            return aggType;
        }
        return madAggType;
    }


    public boolean isNewerVersion() {
        return isNewerVersion;
    }

    public void setIsNewerVersion(boolean isNewerVersion) {
        this.isNewerVersion = isNewerVersion;
    }

    /**
     * Überprüft, ob die übergebene Sachnummer im Speicherformat vorliegt. Falls ja, wird die Sachnummer ins
     * Eingabeformat konvertiert. Das Eingabeformat wird zurückgegeben. Die Konvertierung verläuft nach den originalen
     * Richtlinien von Daimler (siehe Confluence).
     * Implementierung erfolgt in {@link iPartsNumberHelper}
     *
     * @param partNo
     * @return
     */
    public String checkNumberInputFormat(String partNo, EtkMessageLog messageLog) {
        return numberHelper.checkNumberInputFormat(partNo, messageLog);
    }

    public String checkQuantityFormat(String quantity) {
        return numberHelper.convertQuantityFormat(quantity);
    }

    /**
     * Liefert zurück, ob es sich bei der übergebenen PEM um eine AS-PEM handelt (Prefix: "ZV")
     *
     * @param pem
     * @return
     */
    public boolean isASPem(String pem) {
        return StrUtils.isValid(pem) && StrUtils.stringStartsWith(pem, AS_PEM_PREFIX, true);
    }

    /**
     * nur der Vollständigkeit halber
     *
     * @return
     */
    public EtkProject getProject() {
        return project;
    }

    public ASUsageHelper getAsUsageHelper() {
        return asUsageHelper;
    }

    /**
     * Spezielle Behandlung für bestimmte Felder (Modifikation des Wertes aus dem Feld im <i>importRecord</i>).
     * <br/>Diese Methode sollte von Importern beim Import aufgerufen werden.
     *
     * @param sourceField
     * @param importRec
     * @return
     */
    public String handleValueOfSpecialField(String sourceField, Map<String, String> importRec) {
        return handleValueOfSpecialField(sourceField, importRec.get(sourceField));
    }

    /**
     * Spezielle Behandlung für bestimmte Felder (Modifikation des übergebenen <i>values</i>s).
     * <br/>Diese Methode muss für spezielle Behandlungen von den ImportHelpern überschrieben werden.
     * <br/>Importer sollten beim Import nicht diese Methode aufrufen sondern {@link #handleValueOfSpecialField(String, java.util.Map)}.
     *
     * @param sourceField
     * @param value
     * @return
     */
    protected String handleValueOfSpecialField(String sourceField, String value) {
        return value;
    }

    /**
     * Diese Routine sollte nur überschrieben (aufgerufen) werden, wenn aus einem Quellfeld mehrere Zielfelder gefüllt werden müssen.
     * Die Quelle ist gleich!
     * Zur Unterscheidung kann in so einem Fall dann nur das Zielfeld dienen.
     * <p>
     * Beispiel:
     * Aus dem DIALOG-Quellfeld xyz mit Inhalt:                "ABCDEFGHIJK" sollen die ...
     * .. Stellen 1+2 in     [DB_FIELD_VAR1] übernommen werden "AB*********" = "AB"
     * .. Stelle 4 in        [DB_FIELD_VAR2] übernommen werden "**D*******" = "D"
     * .. Stelle 7,8 + 10 in [DB_FIELD_VAR3] übernommen werden "*****GH*J*" = "GHJ"
     *
     * @param sourceField
     * @param destField
     * @param sourceValue
     * @return
     */
    protected String extractCorrespondingSubstring(String sourceField, String destField, String sourceValue) {
        return sourceValue;
    }

    protected String handleValueOfMultiLangField(String destField, String value, Language langDef) {
        return value;
    }

    protected boolean handleValueOfBooleanField(String value) {
        return SQLStringConvert.ppStringToBoolean(value);
    }

    public static void searchMADDatAfterDIALOGImport(final EtkProject project, final Set<String> chosenSeries) {
        final EtkMessageLogForm logForm = new EtkMessageLogForm("!!MAD <> DIALOG Datenabgleich", "!!Suche MAD Daten nach DIALOG Import", null);
        logForm.showMarquee();
        logForm.showModal(new FrameworkRunnable() {
            @Override
            public void run(FrameworkThread thread) {
                for (String seriesNo : chosenSeries) {
                    logForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Suche MAD Daten zur Baureihe \"%1\"", seriesNo) + "\n");
                    int overall = 0;

                    iPartsDataCodeList codeList = iPartsDataCodeList.loadCodeDataSortedWithoutJoinForSeriesAndSource(project,
                                                                                                                     seriesNo,
                                                                                                                     iPartsImportDataOrigin.MAD);
                    overall = handleMADDataInTable(codeList, logForm, iPartsConst.TABLE_DA_CODE, overall);

                    iPartsDataColorTableFactoryList colorFactoryList
                            = iPartsDataColorTableFactoryList.loadColorTableFactoryForSeriesAndSource(project,
                                                                                                      seriesNo,
                                                                                                      iPartsImportDataOrigin.MAD);
                    overall = handleMADDataInTable(colorFactoryList, logForm, iPartsConst.TABLE_DA_COLORTABLE_FACTORY, overall);

                    iPartsDataColorTableDataList colorDataList
                            = iPartsDataColorTableDataList.loadColorTableDataForSeriesAndSource(project,
                                                                                                new iPartsSeriesId(seriesNo),
                                                                                                iPartsImportDataOrigin.MAD);
                    overall = handleMADDataInTable(colorDataList, logForm, iPartsConst.TABLE_DA_COLORTABLE_DATA, overall);

                    iPartsDataColorTableContentList colorContentList
                            = iPartsDataColorTableContentList.loadColortableContentForSeriesAndSource(project,
                                                                                                      seriesNo,
                                                                                                      iPartsImportDataOrigin.MAD);
                    overall = handleMADDataInTable(colorContentList, logForm, iPartsConst.TABLE_DA_COLORTABLE_CONTENT, overall);

                    iPartsDataColorTableToPartList colorToPartList
                            = iPartsDataColorTableToPartList.loadColortableToPartForSeriesAndSource(project,
                                                                                                    seriesNo,
                                                                                                    iPartsImportDataOrigin.MAD);
                    overall = handleMADDataInTable(colorToPartList, logForm, iPartsConst.TABLE_DA_COLORTABLE_PART, overall);

                    iPartsDataFactoryDataList factoryDataList
                            = iPartsDataFactoryDataList.loadFactoryDataForSeriesAndSource(project, seriesNo,
                                                                                          iPartsImportDataOrigin.MAD);
                    overall = handleMADDataInTable(factoryDataList, logForm, iPartsConst.TABLE_DA_FACTORY_DATA, overall);

                    iPartsDataModelPropertiesList modelPropertiesList
                            = iPartsDataModelPropertiesList.loadDataModelPropertiesListForSeriesAndSource(project,
                                                                                                          seriesNo,
                                                                                                          iPartsImportDataOrigin.MAD,
                                                                                                          DBDataObjectList.LoadType.ONLY_IDS);
                    overall = handleMADDataInTable(modelPropertiesList, logForm, iPartsConst.TABLE_DA_MODEL_PROPERTIES, overall);

                    iPartsDataResponseDataList responseDataList
                            = iPartsDataResponseDataList.loadResponseDataForSeriesAndSource(project,
                                                                                            seriesNo,
                                                                                            iPartsImportDataOrigin.MAD);
                    overall = handleMADDataInTable(responseDataList, logForm, iPartsConst.TABLE_DA_RESPONSE_DATA, overall);

                    iPartsDataResponseSpikeList responseSpikes
                            = iPartsDataResponseSpikeList.loadResponseSpikesForSeriesAndSource(project,
                                                                                               seriesNo,
                                                                                               iPartsImportDataOrigin.MAD);
                    overall = handleMADDataInTable(responseSpikes, logForm, iPartsConst.TABLE_DA_RESPONSE_SPIKES, overall);

                    logForm.getMessageLog().fireMessage("########################");
                    logForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Für die Baureihe " +
                                                                                     "\"%1\" wurden insgesamt \"%2\" " +
                                                                                     "MAD Datensätze gefunden.",
                                                                                     seriesNo, String.valueOf(overall)));
                    logForm.getMessageLog().fireMessage("\n");
                }
                logForm.disableMarquee();
            }
        });
    }

    /**
     * Schreibt die Meldungen bezüglich der gefundenen MAD Datensätze ins {@link EtkMessageLogForm}.
     *
     * @param objectList
     * @param logForm
     * @param tablename
     * @param overall
     * @return
     */
    private static int handleMADDataInTable(EtkDataObjectList objectList, EtkMessageLogForm logForm, String tablename, int overall) {
        if ((objectList == null) || objectList.isEmpty()) {
            logForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Keine MAD Datensätze in \"%1\" gefunden", tablename));
            return overall;
        }
        int size = objectList.size();
        logForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Anzahl MAD Datensätze in \"%1\": %2", tablename, String.valueOf(size)));
        return overall + size;
    }


    private static boolean isValidImageDate(String currentImageDate) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(PART_IMAGE_DATE_FORMAT);
            formatter.parse(currentImageDate);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

    /**
     * Korrigiert das Zeichnungsdatum. Aus den Importdaten kommt das Zeichnungsdatum nur als zweistellige
     * Jahresangabe: yy und nicht yyyy
     * Es wird überprüft, ob die Jahresangabe größer ist als der vorgegebene Wert. Falls ja, wird "19" vorangestellt. Falls nein, dann "20".
     *
     * @param dataObject
     * @param dateFieldname
     */
    public static void handleImageDateWithoutFirstTwoYearDigits(EtkDataObject dataObject, String currentImageDate, String dateFieldname, String splitYear) {
        String newImageDate = convertImageDateWithoutFirstTwoYearDigits(currentImageDate, splitYear);
        dataObject.setFieldValue(dateFieldname, newImageDate, DBActionOrigin.FROM_EDIT);
    }

    public static String convertImageDateWithoutFirstTwoYearDigits(String currentImageDate, String splitYear) {
        if ((currentImageDate.length() == 6) && isValidImageDate(currentImageDate)) {
            String lastTwoYearDigits = StrUtils.copySubString(currentImageDate, 0, 2);
            if (lastTwoYearDigits.compareTo(splitYear) > 0) {
                return IMAGE_DATE_PREVIOUS_CENTURY + currentImageDate;
            } else {
                return IMAGE_DATE_CURRENT_CENTURY + currentImageDate;
            }
        }
        return "";
    }

    public void handleImageDateWithoutFirstTwoYearDigitsWithinObject(EtkDataObject dataObject, String dateFieldname, String splitYear) {
        String currentImageDate = dataObject.getFieldValue(dateFieldname);
        handleImageDateWithoutFirstTwoYearDigits(dataObject, currentImageDate, dateFieldname, splitYear);
    }

    /**
     * Sucht passend zur übergebenen Sortimentsklasse die passende Aftersales Produktklasse.
     *
     * @param assortmentClass
     */
    public String convertAssortmentClassToReferencingASProductClass(String assortmentClass) {
        if (StrUtils.isEmpty(assortmentClass)) {
            return assortmentClass;
        }
        iPartsDataAssortmentClassesMapping mappingData = new iPartsDataAssortmentClassesMapping(getProject(), new iPartsAssortmentClassMappingId(assortmentClass));
        if (!mappingData.existsInDB()) {
            return assortmentClass;
        }
        return mappingData.getASProductClass();
    }

    /**
     * Setzt den Titel des Produkts. Dabei handelt es sich um die Produktnummer mit optionaler zusätzlicher Benennung.
     *
     * @param dataProduct
     * @param additionalDescription
     */
    public void setProductTitle(iPartsDataProduct dataProduct, String additionalDescription) {
        String title = dataProduct.getAsId().getProductNumber();
        if (!additionalDescription.isEmpty()) {
            title = title + " (" + additionalDescription + ")";
        }
        EtkMultiSprache currentText = dataProduct.getFieldValueAsMultiLanguage(FIELD_DP_TITLE);
        EtkMultiSprache newText = new EtkMultiSprache();
        newText.setText(Language.DE, title);
        // Alle verfügbaren Sprachen mit dem deutschen Text füllen
        newText.fillAllLanguages(getProject().getConfig().getDatabaseLanguages(), Language.DE);
        if (!currentText.equalContent(newText)) {
            dataProduct.setFieldValueAsMultiLanguage(FIELD_DP_TITLE, newText, DBActionOrigin.FROM_EDIT);
        }
    }

    /**
     * Überprüft, ob die SA in der Tabelle DA_SA existiert.
     * Falls nicht, dann wird sie mit den verfügbaren Daten angelegt.
     * Falls ja, werden die einzelnen Zielfelder auf "leer" oder "nicht leer" überprüft.
     * Bei "nicht leer" werden keine neuen Werte übernommen.
     * Bei "leer" werden vorhandene Werte der SAA in die SA übernommen.
     *
     * @param dataSAA
     */
    public iPartsDataSa addSaIfNotExists(iPartsDataSaa dataSAA, iPartsImportDataOrigin dataOrigin) {
        if (dataSAA == null) {
            return null;
        }
        return extractSaFromSaa(dataSAA, dataOrigin);
    }

    /**
     * Extrahiert aus der übergebenen SAA eine SA mit allen relevanten Daten der SAA und legt die SA auch an, wenn diese
     * noch nicht existiert.
     *
     * @param dataSAA
     * @param dataOrigin
     * @return
     */
    public iPartsDataSa extractSaFromSaa(iPartsDataSaa dataSAA, iPartsImportDataOrigin dataOrigin) {
        String saNumber = iPartsNumberHelper.convertSAAtoSANumber(dataSAA.getAsId().getSaaNumber());
        if (StrUtils.isValid(saNumber)) {
            iPartsDataSa dataSA = new iPartsDataSa(getProject(), new iPartsSaId(saNumber));
            if (!dataSA.existsInDB()) {
                dataSA.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }
            transferSaaDataIfNecessary(dataSAA, dataSA, dataOrigin);
            return dataSA;
        }
        return null;
    }

    /**
     * Sind die Attributswerte des SA-Stamms bereits vorhanden, dann überschreibe die existierenden Werte NICHT.
     * Sind die Attributswerte des SA-Stamms nicht vorhanden, dann ergänze die Werte aus dem SAA-Stamm.
     *
     * @param sourceSaa
     * @param destSa
     */
    private void transferSaaDataIfNecessary(iPartsDataSaa sourceSaa, iPartsDataSa destSa, iPartsImportDataOrigin dataOrigin) {
        // Die Quelle nur mit EDS überschreiben, falls sie noch nicht gesetzt ist oder das Objekt komplett neu ist
        String destinationSource = destSa.getFieldValue(FIELD_DS_SOURCE);
        boolean saExistsInDB = destSa.existsInDB();
        if (!saExistsInDB || destinationSource.isEmpty()) {
            destSa.setFieldValue(FIELD_DS_SOURCE, dataOrigin.getOrigin(), DBActionOrigin.FROM_EDIT);
        }
        // Wird der Text gesetzt, muss auch das EDAT und ADAT aktualisiert werden (mit dem aktuellen Zeitstempel).
        // ADAT immer und EDAT nur wenn die SA noch nicht existiert hat.
        if (transferMultiLangSaaValueIfDestFieldIsEmpty(sourceSaa, FIELD_DS_CONST_DESC, destSa, FIELD_DS_CONST_DESC) || !saExistsInDB) {
            String dateTime = DateUtils.toyyyyMMddHHmmss_Calendar(Calendar.getInstance());
            destSa.setFieldValue(FIELD_DS_ADAT, dateTime, DBActionOrigin.FROM_EDIT);
            if (StrUtils.isEmpty(destSa.getFieldValue(FIELD_DS_EDAT))) {
                destSa.setFieldValue(FIELD_DS_EDAT, dateTime, DBActionOrigin.FROM_EDIT);
            }
        }

        transferConstructionSaaData(sourceSaa, destSa);
    }

    /**
     * Überträgt die Konstruktions-Nummer der SAA an die SA.
     *
     * @param sourceSaa
     * @param destSa
     */
    public void transferConstructionSaaData(iPartsDataSaa sourceSaa, iPartsDataSa destSa) {
        // Konstruktions-SA-Nummer setzen falls Konstruktions-SAA-Nummer unterschiedlich zur Retail-SAA-Nummer
        String constructionSAANumber = sourceSaa.getFieldValue(FIELD_DS_CONST_SAA);
        String constructionSANumber = "";
        if (!constructionSAANumber.isEmpty()) {
            constructionSANumber = iPartsNumberHelper.convertSAAtoSANumber(constructionSAANumber);
        }
        destSa.setFieldValue(FIELD_DS_CONST_SA, constructionSANumber, DBActionOrigin.FROM_EDIT);
    }

    /**
     * Funktion, die den mehrsprachigen Wert aus einem Quellfeld der SAA in das Zielfeld der SA überträgt, wenn ...
     * - der zu übernehmende Wert nicht leer ist
     * - und das zu setzende Zielfeld noch keinen Wert enthält
     *
     * @param sourceSaa
     * @param sourceField
     * @param destSa
     * @param destField
     */
    private boolean transferMultiLangSaaValueIfDestFieldIsEmpty(iPartsDataSaa sourceSaa, String sourceField, iPartsDataSa destSa, String destField) {
        EtkMultiSprache sourceMultiLang = sourceSaa.getFieldValueAsMultiLanguage(sourceField);
        // Leere zu setzende Werte nicht übernehmen.
        if (!sourceMultiLang.allStringsAreEmpty()) {
            EtkMultiSprache destMultiLang = destSa.getFieldValueAsMultiLanguage(destField);
            // Nur leere Zielfelder überschreiben. Bestehende Werte werden nicht angetastet.
            if (destMultiLang.allStringsAreEmpty()) {
                destSa.setFieldValueAsMultiLanguage(destField, sourceMultiLang, DBActionOrigin.FROM_EDIT);
                return true;
            }
        }
        return false;
    }


    /**
     * Erzeugt ein {@link iPartsDataSAAModels} falls es noch nicht existiert
     *
     * @param uSaaNo
     * @param modelNo
     * @param usedIds
     * @param source
     * @return
     */
    public static iPartsDataSAAModels createSaaModelEntryIfNotExists(EtkProject project, String uSaaNo, String modelNo, Set<IdWithType> usedIds,
                                                                     iPartsImportDataOrigin source) {
        // Bei jedem Datensatz ohne Aggregatebaumuster oder Fahrzeugbaumuster als untere Sachnummer (uSaaNo != "D*" && uSaaNo != "C*") ist zu püfen ...
        if (!uSaaNo.startsWith(MODEL_NUMBER_PREFIX_AGGREGATE) && !uSaaNo.startsWith(MODEL_NUMBER_PREFIX_CAR)) {
            // ... ob es einen Datensatz zu dieser unteren SAA-Sachnummer und dieser Fahrzeug oder Aggregatebaumustergruppe
            // gibt. Wenn nicht, neu anlegen.
            iPartsSAAModelsId saaModelsId = new iPartsSAAModelsId(uSaaNo, modelNo);
            if (!usedIds.contains(saaModelsId)) {
                iPartsDataSAAModels saaModels = new iPartsDataSAAModels(project, saaModelsId);
                usedIds.add(saaModelsId);
                if (!saaModels.existsInDB()) {
                    saaModels.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                    saaModels.setFieldValue(FIELD_DA_ESM_SOURCE, source.getOrigin(), DBActionOrigin.FROM_EDIT);
                    return saaModels;
                }
            }
        }
        return null;
    }

    /**
     * Erzeugt ein {@link iPartsDataModelsAggs} falls es noch nicht existiert
     *
     * @param saaNoOrAggModel
     * @param modelNo
     * @param usedIds
     * @param source
     * @return
     */
    public static iPartsDataModelsAggs createModelsAggsEntryIfNotExists(EtkProject project, String modelNo, String saaNoOrAggModel, Set<IdWithType> usedIds, iPartsImportDataOrigin source) {
        // Bei einem Fahrzeugbaumuster "C*" mit zugeordnetem Aggregatebaumuster "D*" prüfen, ...
        if (modelNo.startsWith(iPartsConst.MODEL_NUMBER_PREFIX_CAR) && saaNoOrAggModel.startsWith(iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE)) {
            // ... ob es diese Zuordnung schon in DA_MODELS_AGGS gibt, wenn nicht, neu anlegen.
            iPartsModelsAggsId id = new iPartsModelsAggsId(modelNo, saaNoOrAggModel);
            if (!usedIds.contains(id)) {
                iPartsDataModelsAggs modelAgg = new iPartsDataModelsAggs(project, id);
                usedIds.add(id);
                if (!modelAgg.existsInDB()) {
                    modelAgg.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                    modelAgg.setFieldValue(FIELD_DMA_SOURCE, source.getOrigin(), DBActionOrigin.FROM_EDIT);
                    return modelAgg;
                }
            }
        }
        return null;
    }

}