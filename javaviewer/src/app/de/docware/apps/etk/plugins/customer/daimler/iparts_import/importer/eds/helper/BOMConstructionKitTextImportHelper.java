package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsBOMConstKitTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataBOMConstKitText;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMDataImporter;
import de.docware.framework.modules.gui.misc.MessageLogType;

import java.util.Map;

public class BOMConstructionKitTextImportHelper extends BOMConstructionKitImportHelper {

    public static final String IMPORTER_TITLE_TEXTS = "!!BOM Baukastenverwendungsstellentexte (BKV)";

    /**
     * Konstruktor mit allen Muss-Felder aus beiden Importer
     *
     * @param project
     * @param mapping
     * @param tableName
     * @param releaseDateFromSourceField
     * @param releaseDateToSourceField
     * @param versionToSourceField
     * @param posSourceField
     */
    public BOMConstructionKitTextImportHelper(EtkProject project, Map<String, String> mapping, String tableName,
                                              String releaseDateFromSourceField, String releaseDateToSourceField,
                                              String versionToSourceField, String posSourceField) {
        super(project, mapping, tableName, releaseDateFromSourceField, releaseDateToSourceField, "",
              versionToSourceField, posSourceField);
    }

    public iPartsDataBOMConstKitText createConstKitTextDataObject(AbstractDataImporter importer,
                                                                  Map<String, String> importRec, int recordNo,
                                                                  iPartsBOMConstKitTextId constKitTextId) {
        if (checkReleasedDataSets(importer, recordNo)) {
            iPartsDataBOMConstKitText constKitText = new iPartsDataBOMConstKitText(getProject(), constKitTextId);
            fillDataObject(constKitText, importRec);
            return constKitText;
        }
        return null;
    }

    public iPartsBOMConstKitTextId getConstKitTextId(String constKitNoSourceField, String posSourceField,
                                                     String revFromSourceField, String textTypeField,
                                                     Map<String, String> importRec) {
        String constKitNo = handleValueOfSpecialField(constKitNoSourceField, importRec);
        String position = handleValueOfSpecialField(posSourceField, importRec);
        String revFrom = handleValueOfSpecialField(revFromSourceField, importRec);
        String textType = handleValueOfSpecialField(textTypeField, importRec);

        return new iPartsBOMConstKitTextId(constKitNo, position, textType, revFrom);
    }

    public void handleImportData(AbstractBOMDataImporter importer, iPartsBOMConstKitTextId constKitTextId,
                                 Map<iPartsBOMConstKitTextId, iPartsDataBOMConstKitText> constKitTextMap,
                                 Map<String, String> importRec, int recordNo, iPartsEDSLanguageDefs edsLanguageDef, String text) {
        iPartsDataBOMConstKitText dataObject = constKitTextMap.get(constKitTextId);
        if (dataObject == null) {
            dataObject = createConstKitTextDataObject(importer, importRec, recordNo, constKitTextId);
            if (dataObject == null) {
                importer.reduceRecordCount();
                return;
            }
            constKitTextMap.put(constKitTextId, dataObject);
        } else {
            importer.increaseSkippedRecord();
        }
        if (edsLanguageDef != iPartsEDSLanguageDefs.EDS_UNKNOWN) {
            fillOverrideOneLanguageTextForEDS(dataObject, edsLanguageDef, FIELD_DCP_TEXT, text);
        }
    }

    public void storeCreatedData(AbstractBOMDataImporter importer, Map<iPartsBOMConstKitTextId, iPartsDataBOMConstKitText> constKitTextMap) {
        // Aufgesammelte Datensätze speichern
        int size = constKitTextMap.values().size();
        importer.getMessageLog().fireMessage(importer.translateForLog("!!Importiere %1 Datensätze...", String.valueOf(size)), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        int importedRecord = 0;
        for (iPartsDataBOMConstKitText dataObject : constKitTextMap.values()) {
            if (importer.isCancelled()) {
                break;
            }
            importer.saveToDB(dataObject);
            importedRecord++;
            importer.getMessageLog().fireProgress(importedRecord, size, "", true, true);
        }
    }
}
