package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.initial;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsBOMConstKitTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataBOMConstKitText;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.BOMConstructionKitTextImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSImportHelper;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Importer für Baukastenverwendungsstellentexte aus der BOM-DB (T43RBKV)
 */
public class BOMConstructionKitTextImporter extends AbstractBOMDataImporter {

    public static final String IMPORT_TABLENAME = "T43RBKV";

    private static final String BKV_SNR = "BKV_SNR";
    private static final String BKV_POS = "BKV_POS";
    private static final String BKV_SPS = "BKV_SPS";
    private static final String BKV_BTXKZ = "BKV_BTXKZ";
    private static final String BKV_AS_AB = "BKV_AS_AB";
    private static final String BKV_KEM_AB = "BKV_KEM_AB";
    private static final String BKV_FRG_KZ_AB = "BKV_FRG_KZ_AB";
    private static final String BKV_FRG_DAT_AB = "BKV_FRG_DAT_AB";
    private static final String BKV_AS_BIS = "BKV_AS_BIS";
    private static final String BKV_KEM_BIS = "BKV_KEM_BIS";
    private static final String BKV_FRG_KZ_BIS = "BKV_FRG_KZ_BIS";
    private static final String BKV_FRG_DAT_BIS = "BKV_FRG_DAT_BIS";
    private static final String BKV_TEXT = "BKV_TEXT";

    private Map<iPartsBOMConstKitTextId, iPartsDataBOMConstKitText> constKitTextMap;
    // Es reicht ein Helper für die ganze Importdatei. Datensatz-spezifische Werte werden in importRecord() gesetzt.
    private BOMConstructionKitTextImportHelper helper;

    public BOMConstructionKitTextImporter(EtkProject project) {
        super(project, BOMConstructionKitTextImportHelper.IMPORTER_TITLE_TEXTS, TABLE_DA_EDS_CONST_PROPS, IMPORT_TABLENAME,
              new FilesImporterFileListType(TABLE_DA_EDS_CONST_PROPS, EDS_SAA_CONSTRUCTION_TEXT, true, false, true, new String[]{ MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_ALL_FILES }));
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        progressMessageType = ProgressMessageType.READING;
        constKitTextMap = new HashMap<>();
        helper = new BOMConstructionKitTextImportHelper(getProject(), getMapping(), getDestinationTable(),
                                                        BKV_FRG_DAT_AB, BKV_FRG_DAT_BIS, BKV_AS_BIS, BKV_POS);
    }

    @Override
    protected void postImportTask() {
        super.postImportTask();
        if (!isCancelled() && importToDB) {
            progressMessageType = ProgressMessageType.IMPORTING;
            helper.storeCreatedData(this, constKitTextMap);
        }
        constKitTextMap = null;
        helper = null;
    }

    @Override
    protected void initMapping(HashMap<String, String> mapping) {
        mapping.put(FIELD_DCP_REVTO, BKV_AS_BIS);
        mapping.put(FIELD_DCP_KEMFROM, BKV_KEM_AB);
        mapping.put(FIELD_DCP_KEMTO, BKV_KEM_BIS);
        mapping.put(FIELD_DCP_RELEASE_FROM, BKV_FRG_DAT_AB);
        mapping.put(FIELD_DCP_RELEASE_TO, BKV_FRG_DAT_BIS);
    }

    @Override
    protected String[] getMustExist() {
        return new String[]{ BKV_SNR, BKV_POS, BKV_BTXKZ, BKV_AS_AB };
    }

    @Override
    protected String[] getMustHaveData() {
        return getMustExist();
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        // Die Datensatz-spezifischen Werte im Helper setzen
        helper.setReleasedValueFrom(EDSImportHelper.getTrimmedValueFromRecord(importRec, BKV_FRG_KZ_AB));
        helper.setReleasedValueTo(EDSImportHelper.getTrimmedValueFromRecord(importRec, BKV_FRG_KZ_BIS));

        iPartsBOMConstKitTextId constKitTextId = helper.getConstKitTextId(BKV_SNR, BKV_POS, BKV_AS_AB, BKV_BTXKZ, importRec);
        String text = helper.handleValueOfSpecialField(BKV_TEXT, importRec);
        iPartsEDSLanguageDefs edsLanguageDef = iPartsEDSLanguageDefs.getType(helper.handleValueOfSpecialField(BKV_SPS, importRec));
        helper.handleImportData(this, constKitTextId, constKitTextMap, importRec, recordNo, edsLanguageDef, text);
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(getDestinationTable())) {
            return importMasterData(prepareImporterKeyValue(importFile, getDestinationTable(), '|', true, null, '\0'));
        }
        return false;
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        if (importFileType.getFileListType().equals(getDestinationTable())) {
            getProject().getDB().delete(getDestinationTable());
            return true;
        }
        return false;
    }
}
