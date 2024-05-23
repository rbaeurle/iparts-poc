/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.initial;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.BOMPartHistoryImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSImportHelper;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Importer für den Teilestammdaten für Baukästen aus der BOM-DB (T43RTEIL)
 */
public class BOMPartHistoryImporter extends AbstractBOMDataImporter {

    public static final String IMPORT_TABLENAME = "T43RTEIL";

    private static final String TEIL_SNR = "TEIL_SNR";
    private static final String TEIL_AS_AB = "TEIL_AS_AB";
    private static final String TEIL_AS_BIS = "TEIL_AS_BIS";
    private static final String TEIL_KEM_AB = "TEIL_KEM_AB";
    private static final String TEIL_KEM_BIS = "TEIL_KEM_BIS";
    private static final String TEIL_VAKZ_AB = "TEIL_VAKZ_AB";
    private static final String TEIL_VAKZ_BIS = "TEIL_VAKZ_BIS";
    private static final String TEIL_FRG_DAT_AB = "TEIL_FRG_DAT_AB";
    private static final String TEIL_FRG_DAT_BIS = "TEIL_FRG_DAT_BIS";

    public BOMPartHistoryImporter(EtkProject project) {
        super(project, "!!BOM Teilestammdaten für Baukasten (TEIL)", TABLE_DA_BOM_MAT_HISTORY, IMPORT_TABLENAME,
              new FilesImporterFileListType(TABLE_DA_BOM_MAT_HISTORY, BOM_PART_MASTERDATA_HISTORY, true, false, false, new String[]{ MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_ALL_FILES }));
    }

    @Override
    protected void initMapping(HashMap<String, String> mapping) {
        mapping.put(FIELD_DBMH_REV_TO, TEIL_AS_BIS);
        mapping.put(FIELD_DBMH_KEM_FROM, TEIL_KEM_AB);
        mapping.put(FIELD_DBMH_KEM_TO, TEIL_KEM_BIS);
        mapping.put(FIELD_DBMH_RELEASE_FROM, TEIL_FRG_DAT_AB);
        mapping.put(FIELD_DBMH_RELEASE_TO, TEIL_FRG_DAT_BIS);
    }

    @Override
    protected String[] getMustExist() {
        return new String[]{ TEIL_SNR, TEIL_AS_AB, TEIL_VAKZ_AB, TEIL_VAKZ_BIS };
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
        String vakzToValue = EDSImportHelper.getTrimmedValueFromRecord(importRec, TEIL_VAKZ_BIS);
        boolean isVakzValueEmpty = StrUtils.isEmpty(vakzToValue);
        BOMPartHistoryImportHelper importHelper = new BOMPartHistoryImportHelper(getProject(), getMapping(), getDestinationTable(), TEIL_AS_BIS,
                                                                                 TEIL_FRG_DAT_AB, TEIL_FRG_DAT_BIS, isVakzValueEmpty);
        if (!importHelper.isValidRecord(importRec, TEIL_VAKZ_AB)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 wird übersprungen: \"%2\" enthält \"%3\" und ist nicht leer.",
                                                        String.valueOf(recordNo), TEIL_VAKZ_AB, importHelper.handleValueOfSpecialField(TEIL_VAKZ_AB, importRec)),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }

        String partNo = importHelper.handleValueOfSpecialField(TEIL_SNR, importRec);
        String revFrom = importHelper.handleValueOfSpecialField(TEIL_AS_AB, importRec);
        importHelper.importPartHistoryData(this, importRec, partNo, revFrom, recordNo, importToDB);
    }

    @Override
    protected void postImportTask() {
        // Die angezeigte Zahl des aktuellen Import-Datensatzes stimmt zum Ende hin nicht mehr.
        // Refresh-Problem. ==> Den Fortschritt kurzerhand verschwinden lassen.
        getMessageLog().hideProgress();
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(getDestinationTable())) {
            return importMasterData(prepareImporterKeyValue(importFile, getDestinationTable(), '|', true, null, '\0'));
        }
        return false;
    }
}
