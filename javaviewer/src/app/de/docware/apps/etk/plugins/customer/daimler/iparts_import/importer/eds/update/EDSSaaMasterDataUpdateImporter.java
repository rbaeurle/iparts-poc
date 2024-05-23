/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.update;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSaaHistory;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaHistoryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMXMLDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSSaaMasterDataImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Importer für die SAA Stammdaten aus EDS (Änderungsdienst)
 */
public class EDSSaaMasterDataUpdateImporter extends AbstractBOMXMLDataImporter {

    public static final String IMPORT_TABLENAME = "getT43RSAAE";
    private static final String ATTRIBUTE_BEM_TEXT = "nr";
    private static final String ATTRIBUTE_TYPE_WW = "type";

    private static final String SAAE_PARTLIST_NUMBER = "PartsListNumber";
    private static final String SAAE_PARTLIST_ADDITIONAL_LANG_DATA = "PartsListAdditionalLangData";
    private static final String SAAE_S05_REMARKS = "S05Remarks";
    private static final String SAAE_S05_REMARK = "S05Remark"; // BEMZ_TEXT // Attribut "nr" hat die BEM_ZIFFER
    private static final String SAAE_S05_ALT_EXPLANATIONS = "S05AlternativeExplanations";
    private static final String SAAE_S05_ALT_EXPLANATION = "S05AlternativeExplanation"; //WW_KZi in Attribut "type"
    public static final String SAAE_RELEASE_FROM = "ReleaseDateFrom"; // FRG_DAT_AB
    public static final String SAAE_RELEASE_TO = "ReleaseDateTo"; // FRG_DAT_BIS
    public static final String SAAE_ECO_FROM = "EcoFrom"; // KEM_AB
    public static final String SAAE_ECO_TO = "EcoTo"; // KEM_BIS
    public static final String SAAE_VAKZ_AB = "StatusFrom"; // VAKZ_AB
    public static final String SAAE_VAKZ_BIS = "StatusTo"; // VAKZ_BIS
    private static final String SAAE_AS_AB = "VersionFrom"; // AS_AB
    private static final String SAAE_AS_BIS = "VersionTo"; // AS_BIS
    public static final String SAAE_PLANTSUPPLIES = "PlantSupplies";
    public static final String SAAE_PLANTSUPPLY = "PlantSupply"; // WK_EDS - Werke zu SAA

    public EDSSaaMasterDataUpdateImporter(EtkProject project) {
        super(project, "!!EDS SAA-Stammdaten (T43RSAAE)", TABLE_DA_SAA_HISTORY, IMPORT_TABLENAME,
              new FilesImporterFileListType(TABLE_DA_SAA_HISTORY, EDS_SAA_MASTERDATA_UPDATE, false, false, false, new String[]{ MimeTypes.EXTENSION_XML }));
    }

    @Override
    protected void initXMLMapping(HashMap<String, String> mapping, Set<String> allXMLElementsToConsider, Set<String> allXMLElements) {
        mapping.put(FIELD_DSH_KEM_FROM, SAAE_ECO_FROM);
        mapping.put(FIELD_DSH_KEM_TO, SAAE_ECO_TO);
        mapping.put(FIELD_DSH_RELEASE_FROM, SAAE_RELEASE_FROM);
        mapping.put(FIELD_DSH_RELEASE_TO, SAAE_RELEASE_TO);
        mapping.put(FIELD_DSH_REV_TO, SAAE_AS_BIS);

        allXMLElementsToConsider.addAll(mapping.values());
        allXMLElementsToConsider.add(SAAE_PLANTSUPPLIES);
        allXMLElementsToConsider.add(SAAE_PLANTSUPPLY);
        allXMLElementsToConsider.add(SAAE_PARTLIST_ADDITIONAL_LANG_DATA);
        allXMLElementsToConsider.add(SAAE_S05_REMARKS);
        allXMLElementsToConsider.add(SAAE_S05_REMARK);
        allXMLElementsToConsider.add(SAAE_S05_ALT_EXPLANATIONS);
        allXMLElementsToConsider.add(SAAE_S05_ALT_EXPLANATION);
        allXMLElementsToConsider.add(SAAE_VAKZ_AB);
        allXMLElementsToConsider.add(SAAE_VAKZ_BIS);

        allXMLElements.addAll(allXMLElementsToConsider);
    }

    @Override
    protected String[] getMustExist() {
        return new String[]{ SAAE_PARTLIST_NUMBER, SAAE_AS_AB };
    }

    @Override
    protected String[] getMustHaveData() {
        return getMustExist();
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        String vakzToValue = importRec.get(SAAE_VAKZ_BIS);
        // OB VAKZ_BIS leer ist oder nicht wirkt sich auf einzelene Werte aus. Daher wird hier diese Information direkt an
        // den Helper weitergegeben.
        boolean isVakzToValueEmpty = (vakzToValue == null) || StrUtils.isEmpty(vakzToValue.trim());
        EDSSaaMasterDataImportHelper importHelper = new EDSSaaMasterDataImportHelper(getProject(), getMapping(), getDestinationTable(),
                                                                                     isVakzToValueEmpty, SAAE_AS_BIS, SAAE_ECO_TO,
                                                                                     SAAE_RELEASE_FROM, SAAE_RELEASE_TO);
        importHelper.prepareXMLImportRec(importRec, getAllXMLElementsToConsider(), getAllXMLElements());

        // Nur Datensätze übernehmen bei denen VAKZ_AB leer ist (d.h. alle freigegebenen historischen Stände werden übernommen)
        if (!importHelper.isValidRecord(importRec, SAAE_VAKZ_AB)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 wird übersprungen: \"%2\" enthält \"%3\" und ist nicht leer.",
                                                        String.valueOf(recordNo), SAAE_VAKZ_AB, importHelper.handleValueOfSpecialField(SAAE_VAKZ_AB, importRec)),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }

        String saaNumber = importHelper.handleValueOfSpecialField(SAAE_PARTLIST_NUMBER, importRec);
        String saaRevFrom = importHelper.handleValueOfSpecialField(SAAE_AS_AB, importRec);
        iPartsSaaHistoryId saaHistoryId = new iPartsSaaHistoryId(saaNumber, saaRevFrom);
        iPartsDataSaaHistory saaHistory = new iPartsDataSaaHistory(getProject(), saaHistoryId);
        if (!saaHistory.existsInDB()) {
            saaHistory.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }
        importHelper.fillOverrideCompleteDataForEDSReverse(saaHistory, importRec, iPartsEDSLanguageDefs.EDS_DE);
        importHelper.fillPlantSupplies(saaHistory, importRec, SAAE_PLANTSUPPLIES, SAAE_PLANTSUPPLY, FIELD_DSH_FACTORY_IDS);

        importHelper.fillRemarksUpdate(saaHistory, importRec, SAAE_S05_REMARKS, SAAE_S05_REMARK, ATTRIBUTE_BEM_TEXT);
        importHelper.fillWWFlagsUpdate(saaHistory, importRec, SAAE_S05_ALT_EXPLANATIONS, SAAE_S05_ALT_EXPLANATION, ATTRIBUTE_TYPE_WW);

        importHelper.addSaaIfNotExists(this, saaHistory);

        if (importToDB) {
            saveToDB(saaHistory);
        }
    }
}
