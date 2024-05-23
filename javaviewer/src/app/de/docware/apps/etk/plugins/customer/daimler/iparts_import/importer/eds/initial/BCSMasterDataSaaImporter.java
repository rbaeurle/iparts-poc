/*
 * Copyright (c) 2017 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.initial;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSaa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Importer für die Urladung der SAA Stammdaten aus BCS (nicht EDS!)
 */
// EDS/BCS-Urladung: SAA Stammdaten aus BCS übernehmen, DAIMLER-491
public class BCSMasterDataSaaImporter extends AbstractBOMDataImporter {

    // Tabellenname der DAIMLER-Quelltabelle der BCS SAA Stammdaten
    public static final String IMPORT_TABLENAME = "T43RSAA";

    //Felder der DAIMLER-Quelltabelle der BCS SAA Stammdaten
    private static final String SAA_SNR = "SAA_SNR";                    // SAA-Nummer
    private static final String SAA_AS_AB_BCS = "SAA_AS_AB_BCS";        // Revision von
    private static final String SAA_UNG_KZ_BIS = "SAA_UNG_KZ_BIS";      // -
    private static final String SAA_VAKZ_AB = "SAA_VAKZ_AB";            // -
    private static final String SAA_AS_BIS_BCS = "SAA_AS_BIS_BCS";      // Revision bis
    private static final String SAA_VAKZ_BIS = "SAA_VAKZ_BIS";          // -
    private static final String SAA_BEN = "SAA_BEN";                    // Benennung  <<==== in andere Tabelle DA_SAA.DS_DESC_CONST
    private static final String SAA_ERW_BEN = "SAA_ERW_BEN";            // Erweiterte Benennung
    private static final String SAA_BEM = "SAA_BEM";                    // Bemerkung

    private String[] headerNames = new String[]{
            SAA_SNR,
            SAA_AS_AB_BCS,
            SAA_UNG_KZ_BIS,
            SAA_VAKZ_AB,
            SAA_AS_BIS_BCS,
            SAA_VAKZ_BIS,
            SAA_BEN,
            SAA_ERW_BEN,
            SAA_BEM
    };

    private boolean importFileWithHeader = true;
    private Set<String> importedSAAs = new HashSet<>();

    public BCSMasterDataSaaImporter(EtkProject project) {
        super(project, "!!BCS SAA-Stammdaten (T43RSAA)", TABLE_DA_SAA, IMPORT_TABLENAME,
              new FilesImporterFileListType(TABLE_DA_SAA, BCS_SAA_MASTERDATA, true, false, false, new String[]{ MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_ALL_FILES }));
    }

    @Override
    protected void initMapping(HashMap<String, String> mapping) {
        // Das Mapping für die BCS-SAA Stammdaten in die DA_EDS_SAA_MASTERDATA-Tabelle
        mapping.put(FIELD_DS_REV_FROM, SAA_AS_AB_BCS);
        mapping.put(FIELD_DS_CONST_DESC, SAA_BEN);
        mapping.put(FIELD_DS_DESC_EXTENDED, SAA_ERW_BEN);
        mapping.put(FIELD_DS_REMARK, SAA_BEM);
    }

    @Override
    protected String[] getMustExist() {
        return new String[]{ SAA_SNR, SAA_AS_AB_BCS };
    }

    @Override
    protected String[] getMustHaveData() {
        return new String[]{ SAA_SNR };
    }

    @Override
    protected void preImportTask() {
        progressMessageType = ProgressMessageType.READING;
        importedSAAs.clear();
        super.preImportTask();
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {

        BcsSaaImportHelper importHelper = new BcsSaaImportHelper(getProject(), getMapping(), getDestinationTable());

        // Per Definition: nur die Datensätze genauer untersuchen, bei denen das Feld VAKZ_AB leer ist!
        // ==> Alle Datensätze, bei denen VAKZ_AB NICHT leer ist werden übersprungen.
        boolean skipIt = !(importHelper.handleValueOfSpecialField(SAA_VAKZ_AB, importRec).isEmpty());
        if (skipIt) {
            // Wenn der Datensatz NICHT zu übernehmen ist, die Anzahl importierter Datensätze korrigieren.
            getMessageLog().fireMessage(translateForLog("!!Record %1: \"%2\" enthält \"%3\" und ist nicht leer ==> Datensatz wird übersprungen.",
                                                        String.valueOf(recordNo), SAA_VAKZ_AB, importHelper.handleValueOfSpecialField(SAA_VAKZ_AB, importRec)),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }

        // Erst einmal die SAA-ID aus den Werten aus der Importdatei erzeugen.
        String saaNumber = importHelper.handleValueOfSpecialField(SAA_SNR, importRec);
        iPartsSaaId saaId = new iPartsSaaId(saaNumber);

        // Die SAA darf per Definition nur einmal im Importfile vorhanden sein.
        if (importedSAAs.contains(saaId.getSaaNumber())) {
            getMessageLog().fireMessage(translateForLog("!!Record %1: Die zu importierende SAA \"%2\"ist mehrfach in der Importdatei enthalten. Dieses Vorkommen wird übersprungen.",
                                                        String.valueOf(recordNo), saaId.getSaaNumber()),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }

        iPartsDataSaa dataSaa = new iPartsDataSaa(getProject(), saaId);
        if (!dataSaa.existsInDB()) {
            dataSaa.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }
        String currentRevFrom = dataSaa.getFieldValue(FIELD_DS_REV_FROM);
        if (!importHelper.hasHigherOrEqualsVersion(importRec, currentRevFrom, SAA_VAKZ_AB, SAA_AS_AB_BCS)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1: Die aktuelle SAA Revision (\"%2\") ist höher als " +
                                                        "die neue SAA Revision (\"%3\"). Importdatensatz wird übersprungen.",
                                                        String.valueOf(recordNo), currentRevFrom,
                                                        importHelper.handleValueOfSpecialField(SAA_AS_AB_BCS, importRec)),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }

        // Endlich wird etwas übernommen
        importHelper.fillOverrideCompleteDataForEDSReverse(dataSaa, importRec, iPartsEDSLanguageDefs.EDS_DE);
        // Die Quelle setzen (DAIMLER-9895)
        dataSaa.setFieldValue(FIELD_DS_SOURCE, iPartsImportDataOrigin.EDS.getOrigin(), DBActionOrigin.FROM_EDIT);

        iPartsDataSa dataSa = importHelper.addSaIfNotExists(dataSaa, iPartsImportDataOrigin.EDS);

        if (importToDB) {
            if (dataSa != null) {
                saveToDB(dataSa);
            }
            saveToDB(dataSaa);
        }

        // Merken, dass diese SAA bereits importiert wird.
        importedSAAs.add(saaId.getSaaNumber());
    }


    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(getDestinationTable())) {
            return importMasterData(prepareImporterKeyValue(importFile, getDestinationTable(), '|', importFileWithHeader, headerNames, '\0'));
        }
        return false;
    }

    /**
     * Die Helper-Klasse
     */
    private class BcsSaaImportHelper extends EDSImportHelper {

        public BcsSaaImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (value == null) {
                return "";
            } else {
                // Folgende Felder ohne führende Nullen abspeichern:
                if (sourceField.equals(SAA_AS_AB_BCS) || sourceField.equals(SAA_AS_BIS_BCS)) {
                    int intValue = StrUtils.strToIntDef(value.trim(), -1);
                    if (intValue > -1) {
                        return Integer.toString(intValue);
                    }
                }
                return value.trim();
            }
        }
    }
}