/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.initial;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.BOMPartMasterDataImportHelper;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Importer für die Teilestammdaten aus der BOM-DB
 */
public class BOMPartMasterDataImporter extends AbstractBOMDataImporter {

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
    private static final String TEIL_ZBKZ = "TEIL_ZBKZ";
    private static final String TEIL_BEN = "TEIL_BEN";
    private static final String TEIL_ZGS = "TEIL_ZGS";
    private static final String TEIL_ZDAT = "TEIL_ZDAT";
    private static final String TEIL_ZSN = "TEIL_ZSN";
    private static final String TEIL_HZSN = "TEIL_HZSN";
    private static final String TEIL_FGST = "TEIL_FGST";
    private static final String TEIL_ME = "TEIL_ME";
    private static final String TEIL_KZFRB = "TEIL_KZFRB";
    private static final String TEIL_DPFL = "TEIL_DPFL";
    private static final String TEIL_BGEW = "TEIL_BGEW";
    private static final String TEIL_DS = "TEIL_DS";
    private static final String TEIL_DZ = "TEIL_DZ";
    private static final String TEIL_BEM = "TEIL_BEM";
    private static final String TEIL_FDOK = "TEIL_FDOK";
    private static final String TEIL_DRT = "TEIL_DRT";
    private static final String TEIL_WK = "TEIL_WK";

    private String[] headerNames = new String[]{
            TEIL_SNR,
            TEIL_AS_AB,
            TEIL_KEM_AB,
            TEIL_VAKZ_AB,
            TEIL_FRG_DAT_AB,
            TEIL_AS_BIS,
            TEIL_KEM_BIS,
            TEIL_VAKZ_BIS,
            TEIL_FRG_DAT_BIS,
            TEIL_ZBKZ,
            TEIL_BEN,
            TEIL_ZGS,
            TEIL_ZDAT,
            TEIL_ZSN,
            TEIL_HZSN,
            TEIL_FGST,
            TEIL_ME,
            TEIL_KZFRB,
            TEIL_DPFL,
            TEIL_BGEW,
            TEIL_DS,
            TEIL_DZ,
            TEIL_BEM,
            TEIL_FDOK,
            TEIL_DRT,
            TEIL_WK
    };

    private HashMap<iPartsPartId, BOMPartMasterDataImportHelper.PartWithVersion> partsWithNewestVersion;


    public BOMPartMasterDataImporter(EtkProject project) {
        super(project, "!!BOM Teilestammdaten (TEIL)", TABLE_MAT, IMPORT_TABLENAME,
              new FilesImporterFileListType(TABLE_MAT, BOM_PART_MASTERDATA, true, false, false, new String[]{ MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_ALL_FILES }));
    }

    @Override
    protected void initMapping(HashMap<String, String> mapping) {
        mapping.put(FIELD_M_ASSEMBLYSIGN, TEIL_ZBKZ);
        mapping.put(FIELD_M_CONST_DESC, TEIL_BEN);
        mapping.put(FIELD_M_IMAGESTATE, TEIL_ZGS);
        mapping.put(FIELD_M_IMAGEDATE, TEIL_ZDAT);
        mapping.put(FIELD_M_RELATEDPIC, TEIL_ZSN);
        mapping.put(FIELD_M_REFSER, TEIL_HZSN);
        mapping.put(FIELD_M_RELEASESTATE, TEIL_FGST);
        mapping.put(FIELD_M_QUANTUNIT, TEIL_ME);
        mapping.put(FIELD_M_VARIANT_SIGN, TEIL_KZFRB);
        mapping.put(FIELD_M_DOCREQ, TEIL_DPFL);
        mapping.put(FIELD_M_WEIGHTCALC, TEIL_BGEW);
        mapping.put(FIELD_M_SECURITYSIGN, TEIL_DS);
        mapping.put(FIELD_M_CERTREL, TEIL_DZ);
        mapping.put(FIELD_M_NOTEONE, TEIL_BEM);
        mapping.put(FIELD_M_VEDOCSIGN, TEIL_FDOK);
        mapping.put(FIELD_M_THEFTRELINFO, TEIL_DRT);
    }

    @Override
    protected String[] getMustExist() {
        return new String[]{ TEIL_SNR, TEIL_AS_AB };
    }

    @Override
    protected String[] getMustHaveData() {
        return getMustExist();
    }

    @Override
    protected void preImportTask() {
        progressMessageType = ProgressMessageType.READING;
        partsWithNewestVersion = new HashMap<>();
        super.preImportTask();
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        BOMPartMasterDataImportHelper importHelper = new BOMPartMasterDataImportHelper(this, getProject(), getMapping(), getDestinationTable(),
                                                                                       TEIL_DS, TEIL_DZ, TEIL_FDOK, TEIL_DRT,
                                                                                       TEIL_FRG_DAT_AB, TEIL_FRG_DAT_BIS);
        importHelper.setTrimValues(true);
        if (!importHelper.isValidRecord(importRec, TEIL_VAKZ_AB)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 wird übersprungen: \"%2\" enthält \"%3\" und ist nicht leer.",
                                                        String.valueOf(recordNo), TEIL_VAKZ_AB, importHelper.handleValueOfSpecialField(TEIL_VAKZ_AB, importRec)),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }

        String releaseDate = importHelper.handleValueOfSpecialField(TEIL_FRG_DAT_AB, importRec);
        if (StrUtils.isEmpty(releaseDate)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 wird übersprungen: \"%2\" enthält \"%3\" und ist kein gültiges Freigabedatum.",
                                                        String.valueOf(recordNo), TEIL_FRG_DAT_AB, releaseDate),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }

        String partNumber = importHelper.handleValueOfSpecialField(TEIL_SNR, importRec);
        if (StrUtils.isEmpty(partNumber)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 wird übersprungen: \"%2\" enthält \"%3\" und ist keine gültige Teilenummer.",
                                                        String.valueOf(recordNo), TEIL_SNR, partNumber),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }
        iPartsPartId partId = new iPartsPartId(partNumber, "");
        // Ab eine bestimmten Größe sollen alle Teile im Buffer gespeichert werden
        if (importHelper.checkStoreData(this, partId, partsWithNewestVersion, MAX_ENTRIES_FOR_BUFFERED_SAVE_AND_COMMIT, importToDB)) {
            saveBufferListToDB(true);
            partsWithNewestVersion.clear();
        }

        iPartsDataPart dataPart = importHelper.getDataPart(partId);
        if (dataPart != null) {
            if (!importHelper.checkDataPartValidity(this, dataPart, releaseDate, TEIL_FRG_DAT_AB, recordNo, iPartsImportDataOrigin.DIALOG)) {
                reduceRecordCount();
                return;
            }
            // Befülle das Material mit Mapping und Default Werten
            importHelper.fillPartMasterData(importHelper, dataPart, importRec, releaseDate);
            importHelper.fillPlantSupplies(dataPart, importRec, TEIL_WK, FIELD_M_FACTORY_IDS);
            importHelper.checkAndFillNewerPartData(dataPart, importRec, TEIL_AS_AB, partsWithNewestVersion);
        }
    }


    @Override
    protected void postImportTask() {
        // Die restlichen Teile speichern
        getMessageLog().hideProgress();

        if (!isCancelled()) {
            if (importToDB) {
                BOMPartMasterDataImportHelper.handlePartMasterDataPostImport(this, partsWithNewestVersion);
            }
        }
        partsWithNewestVersion.clear();
        super.postImportTask();
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(getDestinationTable())) {
            return importMasterData(prepareImporterKeyValue(importFile, getDestinationTable(), '|', true, null, '\0'));
        }
        return false;
    }
}