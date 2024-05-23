/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.initial;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataBOMConstKitContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.BOMConstructionKitImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSImportHelper;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Importer für den Baukasteninhalt aus der BOM-DB (T43RBK)
 */
public class BOMConstructionKitContentImporter extends AbstractBOMDataImporter {

    public static final String IMPORT_TABLENAME = "T43RBK";

    private static final String BK_SNR = "BK_SNR";
    private static final String BK_POS = "BK_POS";
    private static final String BK_AS_AB = "BK_AS_AB";
    private static final String BK_KEM_AB = "BK_KEM_AB";
    private static final String BK_FRG_KZ_AB = "BK_FRG_KZ_AB";
    private static final String BK_FRG_DAT_AB = "BK_FRG_DAT_AB";
    private static final String BK_AS_BIS = "BK_AS_BIS";
    private static final String BK_KEM_BIS = "BK_KEM_BIS";
    private static final String BK_FRG_KZ_BIS = "BK_FRG_KZ_BIS";
    private static final String BK_FRG_DAT_BIS = "BK_FRG_DAT_BIS";
    private static final String BK_BEMZ = "BK_BEMZ";
    private static final String BK_WWKB = "BK_WWKB";
    private static final String BK_SNRU = "BK_SNRU";
    private static final String BK_MENGE = "BK_MENGE";
    private static final String BK_MGKZ = "BK_MGKZ";
    private static final String BK_LKG = "BK_LKG";
    private static final String BK_RF = "BK_RF";
    private static final String BK_WK = "BK_WK";
    private static final String BK_BZA = "BK_BZA";
    private static final String BK_LTG_BK = "BK_LTG_BK";

    // Es reicht ein Helper für die ganze Importdatei. Datensatz-spezifische Werte werden in importRecord() gestezt.
    private BOMConstructionKitImportHelper importHelper;

    public BOMConstructionKitContentImporter(EtkProject project) {
        super(project, BOMConstructionKitImportHelper.IMPORTER_TITLE_CONTENT, TABLE_DA_EDS_CONST_KIT, IMPORT_TABLENAME,
              new FilesImporterFileListType(TABLE_DA_EDS_CONST_KIT, EDS_SAA_CONSTRUCTION_NAME, true, false, false, new String[]{ MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_ALL_FILES }));
    }

    @Override
    protected void initMapping(HashMap<String, String> mapping) {
        mapping.put(FIELD_DCK_REVFROM, BK_AS_AB);
        mapping.put(FIELD_DCK_REVTO, BK_AS_BIS);
        mapping.put(FIELD_DCK_KEMFROM, BK_KEM_AB);
        mapping.put(FIELD_DCK_KEMTO, BK_KEM_BIS);
        mapping.put(FIELD_DCK_RELEASE_FROM, BK_FRG_DAT_AB);
        mapping.put(FIELD_DCK_RELEASE_TO, BK_FRG_DAT_BIS);
        mapping.put(FIELD_DCK_NOTE_ID, BK_BEMZ);
        mapping.put(FIELD_DCK_WWKB, BK_WWKB);
        mapping.put(FIELD_DCK_SUB_SNR, BK_SNRU);
        mapping.put(FIELD_DCK_QUANTITY, BK_MENGE);
        mapping.put(FIELD_DCK_QUANTITY_FLAG, BK_MGKZ);
        mapping.put(FIELD_DCK_STEERING, BK_LKG);
        mapping.put(FIELD_DCK_RFG, BK_RF);
        mapping.put(FIELD_DCK_REPLENISHMENT_KIND, BK_BZA);
        mapping.put(FIELD_DCK_TRANSMISSION_KIT, BK_LTG_BK);
    }

    @Override
    protected String[] getMustExist() {
        return new String[]{ BK_SNR, BK_POS, BK_SNRU, BK_AS_AB, BK_AS_BIS, BK_KEM_AB, BK_KEM_BIS };
    }

    @Override
    protected String[] getMustHaveData() {
        return new String[]{ BK_SNR, BK_POS, BK_AS_AB };
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        importHelper = new BOMConstructionKitImportHelper(getProject(), getMapping(), getDestinationTable(),
                                                          BK_FRG_DAT_AB, BK_FRG_DAT_BIS, BK_MENGE, BK_AS_BIS, BK_POS);
    }

    @Override
    protected void postImportTask() {
        super.postImportTask();
        importHelper = null;
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        // Die Datensatz-spezifischen Werte im Helper setzen
        importHelper.setReleasedValueFrom(EDSImportHelper.getTrimmedValueFromRecord(importRec, BK_FRG_KZ_AB));
        importHelper.setReleasedValueTo(EDSImportHelper.getTrimmedValueFromRecord(importRec, BK_FRG_KZ_BIS));

        iPartsDataBOMConstKitContent dataObject = importHelper.createConstKitDataObject(this, importRec, recordNo, BK_SNR, BK_POS, BK_AS_AB);
        if (dataObject != null) {
            importHelper.fillPlantSupplies(dataObject, importRec, BK_WK, FIELD_DCK_FACTORY_IDS);
            if (importToDB) {
                saveToDB(dataObject);
            }
        }
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(getDestinationTable())) {
            return importMasterData(prepareImporterKeyValue(importFile, getDestinationTable(), '|', true, null, '\0'));
        }
        return false;
    }
}
