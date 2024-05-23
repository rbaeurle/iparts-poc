/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.update;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsBOMPartHistoryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataBOMPartHistory;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMXMLDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSMaterialRemarksImportHelper;
import de.docware.framework.modules.gui.misc.MimeTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * EDS (Bom-DB) Wahlweise-Kennbuchstabe und Bemerkungsziffer (Nfz) Importer (T43RTEIE) Änderungsdienst (XML)
 */
public class EDSMaterialRemarksUpdateImporter extends AbstractBOMXMLDataImporter {

    public static final String IMPORT_TABLENAME = "getT43RTEIE";

    // Attribute
    private static final String ATTRIBUTE_BEM_TEXT_NO = "nr";
    private static final String ATTRIBUTE_WW_TYPE = "type";

    // XML-Tags
    private static final String TEIE_PARTLIST_ADDITIONAL_MASTER_DATA = "PartAdditionalMasterData";
    private static final String TEIE_PARTLIST_NUMBER = "PartNumber";
    private static final String TEIE_PARTLIST_ADDITIONAL_LANG_DATA = "PartAdditionalLangData";
    private static final String TEIE_S05_ALT_EXPLANATIONS = "S05AlternativeExplanations";
    private static final String TEIE_S05_ALT_EXPLANATION = "S05AlternativeExplanation";   // WAHLWEISE-Text, WW_KZ im Attribut "type"
    private static final String TEIE_S05_REMARKS = "S05Remarks";
    private static final String TEIE_S05_REMARK = "S05Remark";                            // BEMZ_TEXT // Attribut "nr" hat die BEM_ZIFFER
    private static final String TEIE_AS = "Version";                                      // Änderungsstand (Feld "AS"; 3 stellig)
    private static final String TEIE_DESIGN_CHANGE_COUNTER = "DesignChangeCounter";

    // Der Helper wird nur einmalig benötigt
    private EDSMaterialRemarksImportHelper importHelper;

    public EDSMaterialRemarksUpdateImporter(EtkProject project) {
        super(project, "!!EDS sprachunabhängige Teilestammdaten (TEIE)", TABLE_DA_EDS_MAT_REMARKS, IMPORT_TABLENAME,
              new FilesImporterFileListType(TABLE_DA_EDS_MAT_REMARKS, EDS_MATERIAL_REMARKS_UPDATE, false, false, false, new String[]{ MimeTypes.EXTENSION_XML }));

        importToDB = true;
        doBufferSave = true;
    }

    @Override
    protected void initXMLMapping(HashMap<String, String> mapping, Set<String> allXMLElementsToConsider, Set<String> allXMLElements) {
        // Die relevanten XML-Tags der Importdatei
        allXMLElementsToConsider.add(TEIE_PARTLIST_ADDITIONAL_MASTER_DATA);
        allXMLElementsToConsider.add(TEIE_PARTLIST_NUMBER);
        allXMLElementsToConsider.add(TEIE_PARTLIST_ADDITIONAL_LANG_DATA);
        allXMLElementsToConsider.add(TEIE_S05_ALT_EXPLANATIONS);
        allXMLElementsToConsider.add(TEIE_S05_ALT_EXPLANATION);             // WW_KZ im Attribut "type"
        allXMLElementsToConsider.add(TEIE_S05_REMARKS);
        allXMLElementsToConsider.add(TEIE_S05_REMARK);                      // BEMZ_TEXT, Attribut "nr" hat die BEM_ZIFFER
        allXMLElementsToConsider.add(TEIE_AS);                              // "Version" = Änderungsstand
        allXMLElementsToConsider.add(TEIE_DESIGN_CHANGE_COUNTER);           // "DesignChangeCounter"

        allXMLElements.addAll(allXMLElementsToConsider);
    }

    @Override
    protected String[] getMustExist() {
        return new String[]{ TEIE_PARTLIST_NUMBER, TEIE_AS };
    }

    @Override
    protected String[] getMustHaveData() {
        return getMustExist();
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        importHelper = new EDSMaterialRemarksImportHelper(getProject(), getMapping(), TABLE_DA_EDS_MAT_REMARKS);
    }

    @Override
    protected void postImportTask() {
        super.postImportTask();
        importHelper = null;
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        // Teilenummer (Feld ("SNR"; 13-stellig)
        String partNo = importHelper.handleValueOfSpecialField(TEIE_PARTLIST_NUMBER, importRec);
        // Änderungsstand (Feld "AS"; 3 stellig)
        String revFrom = importHelper.handleValueOfSpecialField(TEIE_AS, importRec);
        importHelper.checkImportRecordRevision(partNo, revFrom, this);
        iPartsBOMPartHistoryId bomPartHistoryId = new iPartsBOMPartHistoryId(partNo, "", revFrom);
        iPartsDataBOMPartHistory bomPartHistory = new iPartsDataBOMPartHistory(getProject(), bomPartHistoryId);
        importHelper.fillRemarksForUpdate(bomPartHistory, importRec, TEIE_S05_REMARKS, TEIE_S05_REMARK, ATTRIBUTE_BEM_TEXT_NO);
        importHelper.fillWWFlagsForUpdate(bomPartHistory, importRec, TEIE_S05_ALT_EXPLANATIONS, TEIE_S05_ALT_EXPLANATION, ATTRIBUTE_WW_TYPE);
        if (importToDB) {
            saveToDB(bomPartHistory);
        }

    }
}
