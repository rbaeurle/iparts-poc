/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.update;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsBOMConstKitTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataBOMConstKitText;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMXMLDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.BOMConstructionKitTextImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSImportHelper;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BOMConstructionKitTextUpdateImporter extends AbstractBOMXMLDataImporter {

    public static final String IMPORT_TABLENAME = "getT43RBKV";

    private static final String BKV_PARTS_LIST = "PartsList"; // Obere Sachnummer (BKV_SNR)
    private static final String BKV_POSITION = "Position"; // Position (BV_POS)
    private static final String BKV_RELEASE_FROM = "ReleaseDateFrom"; // FRG_DAT_AB
    private static final String BKV_RELEASE_TO = "ReleaseDateTo"; // FRG_DAT_BIS
    private static final String BKV_ECO_FROM = "EcoFrom"; // KEM_AB
    private static final String BKV_ECO_TO = "EcoTo"; // KEM_BIS
    private static final String BKV_AS_AB = "VersionFrom"; // AS_AB
    private static final String BKV_AS_BIS = "VersionTo"; // AS_BIS
    private static final String BKV_VAKZ_AB = "StatusFrom"; // VAKZ_AB
    private static final String BKV_VAKZ_BIS = "StatusTo"; // VAKZ_BIS
    public static final String BKV_TEXT_TYPE = "TextType"; // BKV_BTXKZ - Textart (mögliche Werte: V oder T)
    public static final String BKV_TEXT_LANG_DATA = "PointOfUsageTextLangData";
    public static final String BKV_TEXT_LANG_ATTRIBUTE = "language";
    public static final String BKV_TEXT_DATA = "Text"; // BKV_TEXT

    private static final Map<String, String> EXTERNAL_TEXT_MAPPING = new HashMap<>();

    static {
        EXTERNAL_TEXT_MAPPING.put(BKV_TEXT_DATA, FIELD_DCP_TEXT);
    }

    private Map<iPartsBOMConstKitTextId, iPartsDataBOMConstKitText> constKitTextMap;
    // Es reicht ein Helper für die ganze Importdatei. Datensatz-spezifische Werte werden in importRecord() gesetzt.
    private BOMConstructionKitTextImportHelper importHelper;

    public BOMConstructionKitTextUpdateImporter(EtkProject project) {
        super(project, BOMConstructionKitTextImportHelper.IMPORTER_TITLE_TEXTS, TABLE_DA_EDS_CONST_PROPS, IMPORT_TABLENAME,
              new FilesImporterFileListType(TABLE_DA_EDS_CONST_PROPS, EDS_SAA_CONSTRUCTION_TEXT_UPDATE, false, false, false,
                                            new String[]{ MimeTypes.EXTENSION_XML }));
    }

    @Override
    protected void initXMLMapping(HashMap<String, String> mapping, Set<String> allXMLElementsToConsider, Set<String> allXMLElements) {
        mapping.put(FIELD_DCP_REVTO, BKV_AS_BIS);
        mapping.put(FIELD_DCP_KEMFROM, BKV_ECO_FROM);
        mapping.put(FIELD_DCP_KEMTO, BKV_ECO_TO);
        mapping.put(FIELD_DCP_RELEASE_FROM, BKV_RELEASE_FROM);
        mapping.put(FIELD_DCP_RELEASE_TO, BKV_RELEASE_TO);
    }

    @Override
    protected String[] getMustExist() {
        return new String[]{ BKV_PARTS_LIST, BKV_POSITION, BKV_TEXT_TYPE, BKV_AS_AB };
    }

    @Override
    protected String[] getMustHaveData() {
        return getMustExist();
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        constKitTextMap = new HashMap<>();
        importHelper = new BOMConstructionKitTextImportHelper(getProject(), getMapping(), getDestinationTable(),
                                                              BKV_RELEASE_FROM, BKV_RELEASE_TO, BKV_AS_BIS, BKV_POSITION);
    }

    @Override
    protected void postImportTask() {
        super.postImportTask();
        if (!isCancelled() && importToDB) {
            progressMessageType = ProgressMessageType.IMPORTING;
            importHelper.storeCreatedData(this, constKitTextMap);
        }
        constKitTextMap = null;
        importHelper = null;
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        // Im Urladungsimporter sollen nur die Datensätze übernommen werden, die FRG_KZ_AB (hier VAKZ_AB) = "0" haben.
        // Hier im Änderungsdienst kommen laut Daimler nur freigegebene Datensätze an. Somit könnte man hier hart
        // EDSImportHelper.DATA_RELEASED_VALUE ("0") durchreichen. Da via AS-PLM nicht immer die Datensätze so durchgereicht
        // werden, wie es Daimler gerne hätte, frage ich das eigentliche Feld (VAKZ_AB) trotzdem ab. Zusätzlich hat
        // AS-PLM die Eigenschaft gültige bzw leere Elemente nicht aufzuführen, d.h. wenn VAKZ_AB in der XML nicht vorkommt,
        // dann hatte es den Wert "0". Also gebe ich in dem Fall den echten Wert "0" weiter.
        String vakzFromValue = EDSImportHelper.getTrimmedValueFromRecord(importRec, BKV_VAKZ_AB);
        if (StrUtils.isEmpty(vakzFromValue)) {
            vakzFromValue = EDSImportHelper.DATA_RELEASED_VALUE;
        }
        // Die Datensatz-spezifischen Werte im Helper setzen
        importHelper.setReleasedValueFrom(vakzFromValue);
        importHelper.setReleasedValueTo(EDSImportHelper.getTrimmedValueFromRecord(importRec, BKV_VAKZ_BIS));

        iPartsBOMConstKitTextId constKitTextId = importHelper.getConstKitTextId(BKV_PARTS_LIST, BKV_POSITION, BKV_AS_AB, BKV_TEXT_TYPE, importRec);
        String langText = importHelper.handleValueOfSpecialField(BKV_TEXT_DATA, importRec);
        iPartsEDSLanguageDefs edsLanguageDef = importHelper.getEDSLanguageDefFromAttribute(BKV_TEXT_LANG_DATA, BKV_TEXT_LANG_ATTRIBUTE, importRec);
        importHelper.handleImportData(this, constKitTextId, constKitTextMap, importRec, recordNo, edsLanguageDef, langText);
        iPartsDataBOMConstKitText currentObject = constKitTextMap.get(constKitTextId);
        if (currentObject != null) {
            setExternalTexts(currentObject, EXTERNAL_TEXT_MAPPING);
        }
    }
}
