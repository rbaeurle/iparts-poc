/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.initial;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSaaHistory;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaHistoryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSSaaMasterDataImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Importer für die Urladung der SAA Stammdaten aus EDS (nicht BCS!)
 */
public class EDSSaaMasterDataImporter extends AbstractBOMDataImporter {

    // Tabellenname der DAIMLER-Quelltabelle der BCS SAA Stammdaten
    public static final String IMPORT_TABLENAME = "T43RSAAE";

    private static final String PREFIX_REMARK_NO = "SAAE_BEM_ZIFFER";
    private static final String PREFIX_REMARK_TEXT = "SAAE_BEMZ_TEXT";
    private static final String PREFIX_WW_TYPE = "SAAE_WW_KZ";
    private static final String PREFIX_WW_TEXT = "SAAE_WW_ERKL";

    private static final String SAAE_SNR = "SAAE_SNR";
    private static final String SAAE_AS_AB = "SAAE_AS_AB";
    private static final String SAAE_AS_BIS = "SAAE_AS_BIS";
    private static final String SAAE_KEM_AB = "SAAE_KEM_AB";
    private static final String SAAE_KEM_BIS = "SAAE_KEM_BIS";
    private static final String SAAE_VAKZ_AB = "SAAE_VAKZ_AB";
    private static final String SAAE_VAKZ_BIS = "SAAE_VAKZ_BIS";
    private static final String SAAE_FRG_DAT_AB = "SAAE_FRG_DAT_AB";
    private static final String SAAE_FRG_DAT_BIS = "SAAE_FRG_DAT_BIS";
    private static final String SAAE_SAAE_WK_EDS = "SAAE_WK_EDS";

    private String[] headerNames = new String[]{
            SAAE_SNR,
            SAAE_AS_AB,
            SAAE_KEM_AB,
            SAAE_VAKZ_AB,
            SAAE_FRG_DAT_AB,
            SAAE_AS_BIS,
            SAAE_KEM_BIS,
            SAAE_VAKZ_BIS,
            SAAE_FRG_DAT_BIS,
            SAAE_SAAE_WK_EDS
    };

    public EDSSaaMasterDataImporter(EtkProject project) {
        super(project, "!!EDS SAA-Stammdaten (T43RSAAE)", TABLE_DA_SAA_HISTORY, IMPORT_TABLENAME,
              new FilesImporterFileListType(TABLE_DA_SAA_HISTORY, EDS_SAA_MASTERDATA, true, false, false, new String[]{ MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_ALL_FILES }));
    }

    @Override
    protected void initMapping(HashMap<String, String> mapping) {
        mapping.put(FIELD_DSH_REV_TO, SAAE_AS_BIS);
        mapping.put(FIELD_DSH_KEM_FROM, SAAE_KEM_AB);
        mapping.put(FIELD_DSH_KEM_TO, SAAE_KEM_BIS);
        mapping.put(FIELD_DSH_RELEASE_FROM, SAAE_FRG_DAT_AB);
        mapping.put(FIELD_DSH_RELEASE_TO, SAAE_FRG_DAT_BIS);
    }

    @Override
    protected String[] getMustExist() {
        return new String[]{ SAAE_SNR, SAAE_AS_AB };
    }

    @Override
    protected String[] getMustHaveData() {
        return getMustExist();
    }

    @Override
    protected void preImportTask() {
        progressMessageType = ProgressMessageType.READING;
        super.preImportTask();
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        String vakzToValue = importRec.get(SAAE_VAKZ_BIS);
        if (vakzToValue == null) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 wird übersprungen: \"%2\" existiert nicht im Importdatensatz.",
                                                        String.valueOf(recordNo), SAAE_VAKZ_AB),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }
        // OB VAKZ_BIS leer ist oder nicht wirkt sich auf einzelene Werte aus. Daher wird hier diese Information direkt an
        // den Helper weitergegeben.
        boolean isVakzToValueEmpty = StrUtils.isEmpty(vakzToValue.trim());
        EDSSaaMasterDataImportHelper importHelper = new EDSSaaMasterDataImportHelper(getProject(), getMapping(), getDestinationTable(),
                                                                                     isVakzToValueEmpty, SAAE_AS_BIS,
                                                                                     SAAE_KEM_BIS, SAAE_FRG_DAT_AB,
                                                                                     SAAE_FRG_DAT_BIS);
        // Nur Datensätze übernehmen bei denen VAKZ_AB leer ist
        if (!importHelper.isValidRecord(importRec, SAAE_VAKZ_AB)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 wird übersprungen: \"%2\" enthält \"%3\" und ist nicht leer.",
                                                        String.valueOf(recordNo), SAAE_VAKZ_AB, importHelper.handleValueOfSpecialField(SAAE_VAKZ_AB, importRec)),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }

        String saaNumber = importHelper.handleValueOfSpecialField(SAAE_SNR, importRec);
        String saaRevFrom = importHelper.handleValueOfSpecialField(SAAE_AS_AB, importRec);
        iPartsSaaHistoryId saaHistoryId = new iPartsSaaHistoryId(saaNumber, saaRevFrom);
        iPartsDataSaaHistory saaHistory = new iPartsDataSaaHistory(getProject(), saaHistoryId);
        if (!saaHistory.existsInDB()) {
            saaHistory.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }

        importHelper.fillOverrideCompleteDataForEDSReverse(saaHistory, importRec, iPartsEDSLanguageDefs.EDS_DE);
        importHelper.fillPlantSupplies(saaHistory, importRec, SAAE_SAAE_WK_EDS, FIELD_DSH_FACTORY_IDS);

        importHelper.fillWWFlagsForInitialImport(saaHistory, importRec, PREFIX_WW_TYPE, PREFIX_WW_TEXT);
        importHelper.fillRemarksForInitialImport(saaHistory, importRec, PREFIX_REMARK_NO, PREFIX_REMARK_TEXT);

        importHelper.addSaaIfNotExists(this, saaHistory);

        if (importToDB) {
            saveToDB(saaHistory);
        }

    }


    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(getDestinationTable())) {
            return importMasterData(prepareImporterKeyValue(importFile, getDestinationTable(), '|', true, headerNames, '\0'));
        }
        return false;
    }
}
