/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.initial;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsBOMPartHistoryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataBOMPartHistory;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSMaterialRemarksImportHelper;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.Map;

/**
 * EDS (Bom-DB) Wahlweise-Kennbuchstabe und Bemerkungsziffer (Nfz) Importer (T43RTEIE) Urladung
 */
public class EDSMaterialRemarksImporter extends AbstractBOMDataImporter {

    public static final String IMPORT_TABLENAME = "T43RTEIE";

    // Teilenummer (Feld ("SNR"; 13-stellig)
    private final static String EDS_TEIE_SNR = "TEIE_SNR";
    // Änderungsstand (Feld "AS"; 3 stellig)
    private final static String EDS_TEIE_AS = "TEIE_AS";

    // 10 Bemerkungsziffern (BEM_ZIFFER0 - BEMZIFFER9; jeweils 1-stellig)
    private final static String EDS_TEIE_BEMZ_PREFIX = "TEIE_BEM_ZIFFER";
    // 10 zu den Bemerkungsziffern zugehörige Bemerkungsziffertexte (Felder "BEMZ_TEXT0" - "BEMZ_TEXT9"; jeweils 140-stellig)
    private final static String EDS_TEIE_TEXT_PREFIX = "TEIE_BEMZ_TEXT";

    // 26 verschiedene Wahlweise-Kennzeichen (Felder "WW_KZ1" - "WW_KZ26"; jeweils 1-stellig)
    private final static String EDS_TEIE_WW_KZ_PREFIX = "TEIE_WW_KZ";
    // 26 zu den Wahlweise-Kennzeichen zugehörige Wahlweise-Erklärungen (Felder "WW_ERKL1" - "WW_ERKL26"; jeweils 38-stellig)
    private final static String EDS_TEIE_WW_ERKL_PREFIX = "TEIE_WW_ERKL";

    public final static int MAX_REMARK_IDX = 10;     // 0 -  9
    public final static int MAX_WW_FLAG_IDX = 26;    // 1 - 26

    // Der Helper wird nur einmalig benötigt
    EDSMaterialRemarksImportHelper importHelper;

    // bis zu 10 Bemerkungsziffern (BEM_ZIFFER0 - BEMZIFFER9; jeweils 1-stellig)
    private final static String EDS_TEIE_BEM_ZIFFER0 = "TEIE_BEM_ZIFFER0";
    private final static String EDS_TEIE_BEM_ZIFFER1 = "TEIE_BEM_ZIFFER1";
    private final static String EDS_TEIE_BEM_ZIFFER2 = "TEIE_BEM_ZIFFER2";
    private final static String EDS_TEIE_BEM_ZIFFER3 = "TEIE_BEM_ZIFFER3";
    private final static String EDS_TEIE_BEM_ZIFFER4 = "TEIE_BEM_ZIFFER4";
    private final static String EDS_TEIE_BEM_ZIFFER5 = "TEIE_BEM_ZIFFER5";
    private final static String EDS_TEIE_BEM_ZIFFER6 = "TEIE_BEM_ZIFFER6";
    private final static String EDS_TEIE_BEM_ZIFFER7 = "TEIE_BEM_ZIFFER7";
    private final static String EDS_TEIE_BEM_ZIFFER8 = "TEIE_BEM_ZIFFER8";
    private final static String EDS_TEIE_BEM_ZIFFER9 = "TEIE_BEM_ZIFFER9";

    // bis zu 10 zu den Bemerkungsziffern zugehörige Bemerkungsziffertexte (Felder "BEMZ_TEXT0" - "BEMZ_TEXT9"; jeweils 140-stellig)
    private final static String EDS_TEIE_BEMZ_TEXT0 = "TEIE_BEMZ_TEXT0";
    private final static String EDS_TEIE_BEMZ_TEXT1 = "TEIE_BEMZ_TEXT1";
    private final static String EDS_TEIE_BEMZ_TEXT2 = "TEIE_BEMZ_TEXT2";
    private final static String EDS_TEIE_BEMZ_TEXT3 = "TEIE_BEMZ_TEXT3";
    private final static String EDS_TEIE_BEMZ_TEXT4 = "TEIE_BEMZ_TEXT4";
    private final static String EDS_TEIE_BEMZ_TEXT5 = "TEIE_BEMZ_TEXT5";
    private final static String EDS_TEIE_BEMZ_TEXT6 = "TEIE_BEMZ_TEXT6";
    private final static String EDS_TEIE_BEMZ_TEXT7 = "TEIE_BEMZ_TEXT7";
    private final static String EDS_TEIE_BEMZ_TEXT8 = "TEIE_BEMZ_TEXT8";
    private final static String EDS_TEIE_BEMZ_TEXT9 = "TEIE_BEMZ_TEXT9";

    // bis zu 26 verschiedene Wahlweise-Kennzeichen (Felder "WW_KZ1" - "WW_KZ26"; jeweils 1-stellig)
    private final static String EDS_TEIE_WW_KZ1 = "TEIE_WW_KZ1";
    private final static String EDS_TEIE_WW_KZ2 = "TEIE_WW_KZ2";
    private final static String EDS_TEIE_WW_KZ3 = "TEIE_WW_KZ3";
    private final static String EDS_TEIE_WW_KZ4 = "TEIE_WW_KZ4";
    private final static String EDS_TEIE_WW_KZ5 = "TEIE_WW_KZ5";
    private final static String EDS_TEIE_WW_KZ6 = "TEIE_WW_KZ6";
    private final static String EDS_TEIE_WW_KZ7 = "TEIE_WW_KZ7";
    private final static String EDS_TEIE_WW_KZ8 = "TEIE_WW_KZ8";
    private final static String EDS_TEIE_WW_KZ9 = "TEIE_WW_KZ9";
    private final static String EDS_TEIE_WW_KZ10 = "TEIE_WW_KZ10";
    private final static String EDS_TEIE_WW_KZ11 = "TEIE_WW_KZ11";
    private final static String EDS_TEIE_WW_KZ12 = "TEIE_WW_KZ12";
    private final static String EDS_TEIE_WW_KZ13 = "TEIE_WW_KZ13";
    private final static String EDS_TEIE_WW_KZ14 = "TEIE_WW_KZ14";
    private final static String EDS_TEIE_WW_KZ15 = "TEIE_WW_KZ15";
    private final static String EDS_TEIE_WW_KZ16 = "TEIE_WW_KZ16";
    private final static String EDS_TEIE_WW_KZ17 = "TEIE_WW_KZ17";
    private final static String EDS_TEIE_WW_KZ18 = "TEIE_WW_KZ18";
    private final static String EDS_TEIE_WW_KZ19 = "TEIE_WW_KZ19";
    private final static String EDS_TEIE_WW_KZ20 = "TEIE_WW_KZ20";
    private final static String EDS_TEIE_WW_KZ21 = "TEIE_WW_KZ21";
    private final static String EDS_TEIE_WW_KZ22 = "TEIE_WW_KZ22";
    private final static String EDS_TEIE_WW_KZ23 = "TEIE_WW_KZ23";
    private final static String EDS_TEIE_WW_KZ24 = "TEIE_WW_KZ24";
    private final static String EDS_TEIE_WW_KZ25 = "TEIE_WW_KZ25";
    private final static String EDS_TEIE_WW_KZ26 = "TEIE_WW_KZ26";

    // bis zu 26 zu den Wahlweise-Kennzeichen zugehörige Wahlweise-Erklärungen (Felder "WW_ERKL1" - "WW_ERKL26"; jeweils 38-stellig)
    private final static String EDS_TEIE_WW_ERKL1 = "TEIE_WW_ERKL1";
    private final static String EDS_TEIE_WW_ERKL2 = "TEIE_WW_ERKL2";
    private final static String EDS_TEIE_WW_ERKL3 = "TEIE_WW_ERKL3";
    private final static String EDS_TEIE_WW_ERKL4 = "TEIE_WW_ERKL4";
    private final static String EDS_TEIE_WW_ERKL5 = "TEIE_WW_ERKL5";
    private final static String EDS_TEIE_WW_ERKL6 = "TEIE_WW_ERKL6";
    private final static String EDS_TEIE_WW_ERKL7 = "TEIE_WW_ERKL7";
    private final static String EDS_TEIE_WW_ERKL8 = "TEIE_WW_ERKL8";
    private final static String EDS_TEIE_WW_ERKL9 = "TEIE_WW_ERKL9";
    private final static String EDS_TEIE_WW_ERKL10 = "TEIE_WW_ERKL10";
    private final static String EDS_TEIE_WW_ERKL11 = "TEIE_WW_ERKL11";
    private final static String EDS_TEIE_WW_ERKL12 = "TEIE_WW_ERKL12";
    private final static String EDS_TEIE_WW_ERKL13 = "TEIE_WW_ERKL13";
    private final static String EDS_TEIE_WW_ERKL14 = "TEIE_WW_ERKL14";
    private final static String EDS_TEIE_WW_ERKL15 = "TEIE_WW_ERKL15";
    private final static String EDS_TEIE_WW_ERKL16 = "TEIE_WW_ERKL16";
    private final static String EDS_TEIE_WW_ERKL17 = "TEIE_WW_ERKL17";
    private final static String EDS_TEIE_WW_ERKL18 = "TEIE_WW_ERKL18";
    private final static String EDS_TEIE_WW_ERKL19 = "TEIE_WW_ERKL19";
    private final static String EDS_TEIE_WW_ERKL20 = "TEIE_WW_ERKL20";
    private final static String EDS_TEIE_WW_ERKL21 = "TEIE_WW_ERKL21";
    private final static String EDS_TEIE_WW_ERKL22 = "TEIE_WW_ERKL22";
    private final static String EDS_TEIE_WW_ERKL23 = "TEIE_WW_ERKL23";
    private final static String EDS_TEIE_WW_ERKL24 = "TEIE_WW_ERKL24";
    private final static String EDS_TEIE_WW_ERKL25 = "TEIE_WW_ERKL25";
    private final static String EDS_TEIE_WW_ERKL26 = "TEIE_WW_ERKL26";

    private String[] headerNames = new String[]{
            EDS_TEIE_SNR,
            EDS_TEIE_AS,
            // bis zu 10 Bemerkungsziffern (BEM_ZIFFER0 - BEMZIFFER9; jeweils 1-stellig)
            // mit den zugehörigen Bemerkungsziffertexten (Felder "BEMZ_TEXT0" - "BEMZ_TEXT9"; jeweils 140-stellig)
            // Immer im Wechsel, BEM_ZIFFER(x), BEMZ_TEXT(x), BEM_ZIFFER(y), BEMZ_TEXT(y)
            EDS_TEIE_BEM_ZIFFER0,
            EDS_TEIE_BEMZ_TEXT0,
            EDS_TEIE_BEM_ZIFFER1,
            EDS_TEIE_BEMZ_TEXT1,
            EDS_TEIE_BEM_ZIFFER2,
            EDS_TEIE_BEMZ_TEXT2,
            EDS_TEIE_BEM_ZIFFER3,
            EDS_TEIE_BEMZ_TEXT3,
            EDS_TEIE_BEM_ZIFFER4,
            EDS_TEIE_BEMZ_TEXT4,
            EDS_TEIE_BEM_ZIFFER5,
            EDS_TEIE_BEMZ_TEXT5,
            EDS_TEIE_BEM_ZIFFER6,
            EDS_TEIE_BEMZ_TEXT6,
            EDS_TEIE_BEM_ZIFFER7,
            EDS_TEIE_BEMZ_TEXT7,
            EDS_TEIE_BEM_ZIFFER8,
            EDS_TEIE_BEMZ_TEXT8,
            EDS_TEIE_BEM_ZIFFER9,
            EDS_TEIE_BEMZ_TEXT9,
            // bis zu 26 verschiedene Wahlweise-Kennzeichen (Felder "WW_KZ1" - "WW_KZ26"; jeweils 1-stellig)
            // bis zu 26 zu den Wahlweise-Kennzeichen zugehörige Wahlweise-Erklärungen (Felder "WW_ERKL1" - "WW_ERKL26"; jeweils 38-stellig)
            // Immer im Wechsel, EDS_TEIE_WW_KZ(x), EDS_TEIE_WW_ERKL(x),EDS_TEIE_WW_KZ(y), EDS_TEIE_WW_ERKL(y)
            EDS_TEIE_WW_KZ1,
            EDS_TEIE_WW_ERKL1,
            EDS_TEIE_WW_KZ2,
            EDS_TEIE_WW_ERKL2,
            EDS_TEIE_WW_KZ3,
            EDS_TEIE_WW_ERKL3,
            EDS_TEIE_WW_KZ4,
            EDS_TEIE_WW_ERKL4,
            EDS_TEIE_WW_KZ5,
            EDS_TEIE_WW_ERKL5,
            EDS_TEIE_WW_KZ6,
            EDS_TEIE_WW_ERKL6,
            EDS_TEIE_WW_KZ7,
            EDS_TEIE_WW_ERKL7,
            EDS_TEIE_WW_KZ8,
            EDS_TEIE_WW_ERKL8,
            EDS_TEIE_WW_KZ9,
            EDS_TEIE_WW_ERKL9,
            EDS_TEIE_WW_KZ10,
            EDS_TEIE_WW_ERKL10,
            EDS_TEIE_WW_KZ11,
            EDS_TEIE_WW_ERKL11,
            EDS_TEIE_WW_KZ12,
            EDS_TEIE_WW_ERKL12,
            EDS_TEIE_WW_KZ13,
            EDS_TEIE_WW_ERKL13,
            EDS_TEIE_WW_KZ14,
            EDS_TEIE_WW_ERKL14,
            EDS_TEIE_WW_KZ15,
            EDS_TEIE_WW_ERKL15,
            EDS_TEIE_WW_KZ16,
            EDS_TEIE_WW_ERKL16,
            EDS_TEIE_WW_KZ17,
            EDS_TEIE_WW_ERKL17,
            EDS_TEIE_WW_KZ18,
            EDS_TEIE_WW_ERKL18,
            EDS_TEIE_WW_KZ19,
            EDS_TEIE_WW_ERKL19,
            EDS_TEIE_WW_KZ20,
            EDS_TEIE_WW_ERKL20,
            EDS_TEIE_WW_KZ21,
            EDS_TEIE_WW_ERKL21,
            EDS_TEIE_WW_KZ22,
            EDS_TEIE_WW_ERKL22,
            EDS_TEIE_WW_KZ23,
            EDS_TEIE_WW_ERKL23,
            EDS_TEIE_WW_KZ24,
            EDS_TEIE_WW_ERKL24,
            EDS_TEIE_WW_KZ25,
            EDS_TEIE_WW_ERKL25,
            EDS_TEIE_WW_KZ26,
            EDS_TEIE_WW_ERKL26
    };

    public EDSMaterialRemarksImporter(EtkProject project) {
        super(project, "EDS Teilestammdaten sprachunabhängig (T43RTEIE)", TABLE_DA_EDS_MAT_REMARKS, IMPORT_TABLENAME,
              new FilesImporterFileListType(TABLE_DA_EDS_MAT_REMARKS, EDS_MATERIAL_REMARKS, true, false,
                                            false, new String[]{ MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_ALL_FILES }));
        importToDB = true;
        doBufferSave = true;
    }

    @Override
    protected void initMapping(HashMap<String, String> mapping) {
        // kein Mapping möglich
    }

    @Override
    protected String[] getMustExist() {
        return new String[]{ EDS_TEIE_SNR, EDS_TEIE_AS };
    }

    @Override
    protected String[] getMustHaveData() {
        return getMustExist();
    }

    @Override
    protected void preImportTask() {
        progressMessageType = ProgressMessageType.READING;
        super.preImportTask();
        // Den Helper nur einmalig pro Import anlegen
        importHelper = new EDSMaterialRemarksImportHelper(getProject(), getMapping(), TABLE_DA_EDS_MAT_REMARKS);
    }


    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        // Teilenummer (Feld ("SNR"; 13-stellig)
        String partNo = importHelper.handleValueOfSpecialField(EDS_TEIE_SNR, importRec);
        // Änderungsstand (Feld "AS"; 3 stellig)
        String revFrom = importHelper.handleValueOfSpecialField(EDS_TEIE_AS, importRec);
        // Check, ob der Änderungsstand <= dem höchsten Änderungsstand der Stammdaten
        importHelper.checkImportRecordRevision(partNo, revFrom, this);
        iPartsBOMPartHistoryId bomPartHistoryId = new iPartsBOMPartHistoryId(partNo, "", revFrom);
        iPartsDataBOMPartHistory bomPartHistory = new iPartsDataBOMPartHistory(getProject(), bomPartHistoryId);
        importHelper.fillWWFlagsForInitialImport(bomPartHistory, importRec, EDS_TEIE_WW_KZ_PREFIX, EDS_TEIE_WW_ERKL_PREFIX);
        importHelper.fillRemarksForInitialImport(bomPartHistory, importRec, EDS_TEIE_BEMZ_PREFIX, EDS_TEIE_TEXT_PREFIX);
        if (importToDB) {
            saveToDB(bomPartHistory);
        }

    }

    @Override
    protected void postImportTask() {
        getMessageLog().hideProgress();
        super.postImportTask();
        importHelper = null;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(getDestinationTable())) {
            return importMasterData(prepareImporterKeyValue(importFile, getDestinationTable(), '|', true, headerNames, '\0'));
        }
        return false;
    }
}
