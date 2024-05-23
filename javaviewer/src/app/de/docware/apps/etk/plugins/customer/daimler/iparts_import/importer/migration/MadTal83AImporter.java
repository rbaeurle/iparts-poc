/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.importer.base.model.fixedlength.FixedLenFieldDescription;
import de.docware.apps.etk.base.importer.base.model.fixedlength.FixedLenRecordType;
import de.docware.apps.etk.base.importer.base.model.fixedlength.FixedLenRecordTypeIdentifier;
import de.docware.apps.etk.base.importer.base.model.fixedlength.KeyValueRecordFixedLengthGZFileReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.ArrayUtil;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Eldas Tal83A Importer
 * Die TAL83A Dateienen sind fixed Length und enthalten die Gültigkeiten einer SAA/BK zu einem/mehreren Baumuster/n aus MAD.
 * - Z-Nummern sind SAA
 * - A,H-Nummern sind Baukästen
 * - Q-Nummern sind Kundenwünsche
 * Aber alle Sachnummern, die nicht mit Z beginnen, werden in diesem Kontext als Baukästen angesehen
 */
public class MadTal83AImporter extends AbstractDataImporter implements iPartsConst, EtkDbConst {

    final static int TAL83A_RECORD_LEN = 23;

    final static String SATZART_Z = "SATZART_Z";
    final static String SATZART_A = "SATZART_A";
    final static String SATZART_H = "SATZART_H";
    final static String SATZART_N = "SATZART_N";
    final static String SATZART_Q = "SATZART_Q";

    final static String SATZART_Z_KENNER = "Z";
    final static String SATZART_A_KENNER = "A";
    final static String SATZART_H_KENNER = "H";
    final static String SATZART_N_KENNER = "N";
    final static String SATZART_Q_KENNER = "Q";

    final static String Z_SAA_NO = "SAA_NO";
    final static String Z_FILLER = "FILLER";
    final static String Z_BM = "BM";

    final static String A_SACHNUMMER = "SACHNUMMER";
    final static String A_FILLER = "FILLER";
    final static String A_BM = "BM";

    final static String H_SACHNUMMER = "SACHNUMMER";
    final static String H_BM = "BM";

    final static String N_SACHNUMMER = "SACHNUMMER";
    final static String N_BM = "BM";

    final static String Q_KUNDENNUMMER = "KUNDENNUMMER";
    final static String Q_BM = "BM";

    private Map<String, iPartsModelId> shortModelList;
    private Map<String, List<iPartsModelId>> shortMultipleModelList;
    private Map<String, Set<String>> modelSaaMap;

    private boolean importToDB = true;
    private boolean doBufferSave = true;

    public MadTal83AImporter(EtkProject project) {
        super(project, "!!SAA/BK-Gültigkeit zu Baumuster (TAL83A)",
              new FilesImporterFileListType("MAD-Rohdaten", "MAD-Rohdaten", true, false, false,
                                            new String[]{ MimeTypes.EXTENSION_GZ, MimeTypes.EXTENSION_ALL_FILES }));
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        return true;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        return true;
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        shortModelList = new HashMap<String, iPartsModelId>();
        shortMultipleModelList = new HashMap<String, List<iPartsModelId>>();
        modelSaaMap = new HashMap<>();
        iPartsDataModelList modelList = iPartsDataModelList.loadAllDataModelList(getProject(), DBDataObjectList.LoadType.ONLY_IDS);
        for (iPartsDataModel dataModel : modelList) {
            addToList(dataModel.getAsId());
        }
        for (String key : shortMultipleModelList.keySet()) {
            List<iPartsModelId> modelIdList = shortMultipleModelList.get(key);
            Collections.sort(modelIdList);
        }
        progressMessageType = ProgressMessageType.READING;
        setBufferedSave(doBufferSave);
    }

    private void addToList(iPartsModelId modelId) {
        String shortModelNo = buildShortModelNo(modelId);
        iPartsModelId searchModelId = shortModelList.get(shortModelNo);
        if (searchModelId == null) {
            List<iPartsModelId> modelList = shortMultipleModelList.get(shortModelNo);
            if (modelList == null) {
                shortModelList.put(shortModelNo, modelId);
            } else {
                modelList.add(modelId);
            }
        } else {
            shortModelList.remove(shortModelNo);
            List<iPartsModelId> modelList = new ArrayList<iPartsModelId>();
            modelList.add(searchModelId);
            modelList.add(modelId);
            shortMultipleModelList.put(shortModelNo, modelList);
        }
    }

    private String buildShortModelNo(iPartsModelId modelId) {
        return modelId.getModelNumber().substring(1);
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        // Aus dem Key des ersten Feldes den Alias (Datensatzart) bestimmen. Der Alias und das Feld ist mit '.' getrennt

        String key = importRec.keySet().iterator().next();
        String alias = TableAndFieldName.getFirstPart(key);

        if (alias != null) {
            if (alias.equals(SATZART_Z)) {
                importSatzartZ(importRec, recordNo);
            } else if (alias.equals(SATZART_A)) {
                importSatzartA(importRec, recordNo);
            } else if (alias.equals(SATZART_H)) {
                importSatzartH(importRec, recordNo);
            } else if (alias.equals(SATZART_N)) {
                importSatzartN(importRec, recordNo);
            } else if (alias.equals(SATZART_Q)) {
                importSatzartQ(importRec, recordNo);
            }
        }
    }

    /**
     * Z-Nummern = SAA-Nummern
     * Das Feld ist 9-stellig. SAA-Nummern ohne Kennzeichen sind 8-stellig. Die Daten werden linksbündig erwartet mit
     * 9. Stelle Leerzeichen. Das Right-Trim passiert tief unten in der Standardimplementierung des Fixed-Length-Importers.
     *
     * @param importRec
     * @param recordNo
     */
    private void importSatzartZ(Map<String, String> importRec, int recordNo) {
        String alias = SATZART_Z;
        if (checkSachNoAndModelNo(importRec, recordNo, alias, SATZART_Z_KENNER, Z_SAA_NO, Z_BM)) {
            String saaNo = null;
            try {
                iPartsNumberHelper numberHelper = new iPartsNumberHelper();
                saaNo = numberHelper.unformatSaaForDB(importRec.get(TableAndFieldName.make(alias, Z_SAA_NO)));
            } catch (RuntimeException e) {
                getMessageLog().fireMessage(e.getMessage(), MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                reduceRecordCount();
                return;
            }

            iPartsModelId modelId = calculateModelId(importRec.get(TableAndFieldName.make(alias, Z_BM)), recordNo);
            if (modelId != null) {
                prepareAndAdd(saaNo, modelId);
            }
        }
    }

    /**
     * A-Nummern = Baukästen
     *
     * Laut DAIMLER-2661 soll es 10- und 12-stellige A-Sachnummern geben (bzw. 11- und 13-stellig mit Sachnummernkennzeichen).
     * Die Nummern stehen linksbündig im Feld, d.h. rechts sind Leerzeichen. Der Importer macht weiter unten einen RTRIM().
     * Also werden kürzere Nummern auch richtig importiert.
     *
     * @param importRec
     * @param recordNo
     */
    private void importSatzartA(Map<String, String> importRec, int recordNo) {
        String alias = SATZART_A;
        if (checkSachNoAndModelNo(importRec, recordNo, alias, SATZART_A_KENNER, A_SACHNUMMER, A_BM)) {
            String constructionKitNo = SATZART_A_KENNER + importRec.get(TableAndFieldName.make(alias, A_SACHNUMMER));
            iPartsModelId modelId = calculateModelId(importRec.get(TableAndFieldName.make(alias, A_BM)), recordNo);
            if (modelId != null) {
                prepareAndAdd(constructionKitNo, modelId);
            }
        }
    }

    /**
     * H-Nummern = Baukästen
     *
     * @param importRec
     * @param recordNo
     */
    private void importSatzartH(Map<String, String> importRec, int recordNo) {
        String alias = SATZART_H;
        if (checkSachNoAndModelNo(importRec, recordNo, alias, SATZART_H_KENNER, H_SACHNUMMER, H_BM)) {
            String constructionKitNo = SATZART_H_KENNER + importRec.get(TableAndFieldName.make(alias, H_SACHNUMMER));
            iPartsModelId modelId = calculateModelId(importRec.get(TableAndFieldName.make(alias, H_BM)), recordNo);
            if (modelId != null) {
                prepareAndAdd(constructionKitNo, modelId);
            }
        }
    }

    /**
     * N-Nummern = Baukästen
     *
     * @param importRec
     * @param recordNo
     */
    private void importSatzartN(Map<String, String> importRec, int recordNo) {
        String alias = SATZART_N;
        if (checkSachNoAndModelNo(importRec, recordNo, alias, SATZART_N_KENNER, N_SACHNUMMER, N_BM)) {
            String constructionKitNo = SATZART_N_KENNER + importRec.get(TableAndFieldName.make(alias, N_SACHNUMMER));
            iPartsModelId modelId = calculateModelId(importRec.get(TableAndFieldName.make(alias, N_BM)), recordNo);
            if (modelId != null) {
                prepareAndAdd(constructionKitNo, modelId);
            }
        }
    }

    /**
     * Q-Nummern = Kundenwunsch-Nummern
     *
     * @param importRec
     * @param recordNo
     */
    private void importSatzartQ(Map<String, String> importRec, int recordNo) {
        String alias = SATZART_Q;
        if (checkSachNoAndModelNo(importRec, recordNo, alias, SATZART_Q_KENNER, Q_KUNDENNUMMER, Q_BM)) {
            String customerRequestNo = SATZART_Q_KENNER + importRec.get(TableAndFieldName.make(alias, Q_KUNDENNUMMER));
            iPartsModelId modelId = calculateModelId(importRec.get(TableAndFieldName.make(alias, Q_BM)), recordNo);
            if (modelId != null) {
                prepareAndAdd(customerRequestNo, modelId);
            }
        }
    }

    /**
     * @param importRec
     * @param recordNo
     * @param alias
     * @param aliasKenner    Kennbuchstabe für Sachnummernart
     * @param saaAttribute
     * @param modelAttribute
     * @return
     */
    private boolean checkSachNoAndModelNo(Map<String, String> importRec, int recordNo, String alias, String aliasKenner, String saaAttribute, String modelAttribute) {
        String sachNo = importRec.get(TableAndFieldName.make(alias, saaAttribute));
        if (sachNo.isEmpty()) {
            getMessageLog().fireMessage(translateForLog("!!%1-Record %2 mit leerer Sachnummer übersprungen",
                                                        aliasKenner, String.valueOf(recordNo)),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return false;
        }
        String shortModelNo = importRec.get(TableAndFieldName.make(alias, modelAttribute));
        if (shortModelNo.isEmpty()) {
            getMessageLog().fireMessage(translateForLog("!!%1-Record %2 mit leerer Baumuster-Nummer übersprungen",
                                                        aliasKenner, String.valueOf(recordNo)),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return false;
        }
        return true;
    }

    private iPartsModelId calculateModelId(String shortModelNo, int recordNo) {
        iPartsModelId modelId = shortModelList.get(shortModelNo);
        if (modelId == null) {
            List<iPartsModelId> modelList = shortMultipleModelList.get(shortModelNo);
            if (modelList == null) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 Keine Baumuster-Stammdaten zu \"%2\" gefunden. Überspringe Record.",
                                                            String.valueOf(recordNo), shortModelNo),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                reduceRecordCount();
            } else {
                //nachdem die Liste sortiert ist, nimm das erste
                modelId = modelList.get(0);
            }
        }
        return modelId;
    }

    private void prepareAndAdd(String saaNo, iPartsModelId modelId) {
        Set<String> saaBkList = modelSaaMap.get(modelId.getModelNumber());
        if (saaBkList == null) {
            saaBkList = new HashSet<>();
            modelSaaMap.put(modelId.getModelNumber(), saaBkList);
        }
        saaBkList.add(saaNo);
    }

    @Override
    public void postImportTask() {
        if (!isCancelled()) {
            if (importToDB) {
                iPartsDataSAAModelsList resultSaaModelsList = new iPartsDataSAAModelsList();
                int counter = 0;
                int countDatas = modelSaaMap.size();
                getMessageLog().fireMessage(translateForLog("!!%1 Baumuster wurden erfaßt", String.valueOf(countDatas)),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                getMessageLog().fireMessage(translateForLog("!!Vergleiche SAA/BK-Gültigkeiten..."), MessageLogType.tmlMessage,
                                            MessageLogOption.TIME_STAMP);
                getMessageLog().fireProgress(0, countDatas, "", true, false);
                for (Map.Entry<String, Set<String>> entry : modelSaaMap.entrySet()) {
                    if (Thread.currentThread().isInterrupted()) {
                        cancelImport("!!Import-Thread wurde frühzeitig beendet");
                        return;
                    }
                    String modelNumber = entry.getKey();
                    iPartsDataSAAModelsList saaModelsList = iPartsDataSAAModelsList.loadDataSAAModelsListForModel(getProject(),
                                                                                                                  new iPartsModelId(modelNumber));

                    // Map von den IDs auf Datensätze für schnelle Suche
                    Map<iPartsSAAModelsId, iPartsDataSAAModels> saaModelIdsInDB = new HashMap<>(saaModelsList.size());
                    for (iPartsDataSAAModels dataSAAModels : saaModelsList) {
                        saaModelIdsInDB.put(dataSAAModels.getAsId(), dataSAAModels);
                    }

                    List<iPartsDataSAAModels> saaModelsWorkList = saaModelsList.getAsList();
                    for (String saaBkNo : entry.getValue()) {
                        iPartsSAAModelsId saaModelsId = new iPartsSAAModelsId(saaBkNo, modelNumber);
                        iPartsDataSAAModels dataSAAModels = saaModelIdsInDB.get(saaModelsId);
                        if (dataSAAModels != null) { // Datensatz ist bereits vorhanden
                            saaModelsWorkList.remove(dataSAAModels);
                            reduceRecordCount(); // Dieser Record wurde nicht importiert, weil er schon existiert
                        } else { // Neuer Datensatz
                            dataSAAModels = new iPartsDataSAAModels(getProject(), saaModelsId);
                            dataSAAModels.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                            dataSAAModels.setFieldValue(FIELD_DA_ESM_SOURCE, iPartsImportDataOrigin.ELDAS.getOrigin(), DBActionOrigin.FROM_EDIT);
                            resultSaaModelsList.add(dataSAAModels, DBActionOrigin.FROM_EDIT);
                        }
                    }

                    // Nicht mehr vorhandene Datensätze löschen
                    if (!saaModelsWorkList.isEmpty()) {
                        for (iPartsDataSAAModels dataSAAModels : saaModelsWorkList) {
                            resultSaaModelsList.delete(dataSAAModels, true, DBActionOrigin.FROM_EDIT);
                        }
                    }
                    counter++;
                    getMessageLog().fireProgress(counter, countDatas, "", true, true);
                }
                getMessageLog().hideProgress();
                getMessageLog().fireMessage(translateForLog("!!%1 Records werden gespeichert; %2 gelöscht", String.valueOf(resultSaaModelsList.size()),
                                                            String.valueOf(resultSaaModelsList.getDeletedList().size())),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                resultSaaModelsList.saveToDB(getProject(), false);
            } else {
                cancelImport(translateForLog("!!Abbruch/Rollback wegen Menü-Aufruf"));
            }
        }
        shortModelList.clear();
        shortMultipleModelList.clear();
        modelSaaMap.clear();
        super.postImportTask();
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        return importMasterData(prepareImporterFixedLength(importFile));
    }

    protected AbstractKeyValueRecordReader prepareImporterFixedLength(DWFile xmlImportFile) {
        List<FixedLenRecordType> recordTypes = new ArrayList<FixedLenRecordType>();

        recordTypes.add(
                new FixedLenRecordType(SATZART_Z,
                                       new FixedLenRecordTypeIdentifier[]{
                                               new FixedLenRecordTypeIdentifier(1, 1, SATZART_Z_KENNER)
                                       },
                                       new FixedLenFieldDescription[]{
                                               new FixedLenFieldDescription(2, 10, Z_SAA_NO),   // 9-stellig
                                               //new FixedLenFieldDescription(10, 14, Z_FILLER),
                                               new FixedLenFieldDescription(14, 22, Z_BM)
                                       }
                )
        );

        recordTypes.add(
                new FixedLenRecordType(SATZART_A,
                                       new FixedLenRecordTypeIdentifier[]{
                                               new FixedLenRecordTypeIdentifier(1, 1, SATZART_A_KENNER)
                                       },
                                       new FixedLenFieldDescription[]{
                                               new FixedLenFieldDescription(2, 13, A_SACHNUMMER),  // 10-und 12-stellig
                                               //new FixedLenFieldDescription(12, 14, A_FILLER),
                                               new FixedLenFieldDescription(14, 22, A_BM)
                                       }
                )
        );

        recordTypes.add(
                new FixedLenRecordType(SATZART_H,
                                       new FixedLenRecordTypeIdentifier[]{
                                               new FixedLenRecordTypeIdentifier(1, 1, SATZART_H_KENNER)
                                       },
                                       new FixedLenFieldDescription[]{
                                               new FixedLenFieldDescription(2, 13, H_SACHNUMMER),  // 12-stellig
                                               new FixedLenFieldDescription(14, 22, H_BM)
                                       }
                )
        );

        recordTypes.add(
                new FixedLenRecordType(SATZART_N,
                                       new FixedLenRecordTypeIdentifier[]{
                                               new FixedLenRecordTypeIdentifier(1, 1, SATZART_N_KENNER)
                                       },
                                       new FixedLenFieldDescription[]{
                                               new FixedLenFieldDescription(2, 13, N_SACHNUMMER),  // 12-stellig
                                               new FixedLenFieldDescription(14, 22, N_BM)
                                       }
                )
        );

        recordTypes.add(
                new FixedLenRecordType(SATZART_Q,
                                       new FixedLenRecordTypeIdentifier[]{
                                               new FixedLenRecordTypeIdentifier(1, 1, SATZART_Q_KENNER)
                                       },
                                       new FixedLenFieldDescription[]{
                                               new FixedLenFieldDescription(2, 13, Q_KUNDENNUMMER),  // 12-stellig
                                               new FixedLenFieldDescription(14, 22, Q_BM)
                                       }
                )
        );

        // Daimler definiert seine Recordlänge als echte Recordlänge, also Nutzdaten + Zeilenende; da Daimler immer Unix-Zeileende hat
        // gilt also: Nutzdatenlänge = Daimler-Record-Länge - 1
        return new KeyValueRecordFixedLengthGZFileReader(xmlImportFile, "", ArrayUtil.toArray(recordTypes), TAL83A_RECORD_LEN - 1, DWFileCoding.UTF8);
    }
}
