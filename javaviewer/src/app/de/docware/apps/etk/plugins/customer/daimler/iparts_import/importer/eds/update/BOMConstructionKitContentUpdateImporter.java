/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.update;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataBOMConstKitContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMXMLDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.BOMConstructionKitImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSImportHelper;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BOMConstructionKitContentUpdateImporter extends AbstractBOMXMLDataImporter {

    public static final String IMPORT_TABLENAME = "getT43RBK";

    public static final String BK_PARTS_LIST = "PartsList"; // Obere Sachnummer (BK-SNR)
    public static final String BK_POSITION = "Position"; // Position (BK-POS)
    public static final String BK_ITEM = "Item"; // Untere Sachnummer (BK-SNRU)
    public static final String BK_RELEASE_FROM = "ReleaseDateFrom"; // FRG_DAT_AB
    public static final String BK_RELEASE_TO = "ReleaseDateTo"; // FRG_DAT_BIS
    public static final String BK_ECO_FROM = "EcoFrom"; // KEM_AB
    public static final String BK_ECO_TO = "EcoTo"; // KEM_BIS
    public static final String BK_AS_AB = "VersionFrom"; // AS_AB
    private static final String BK_AS_BIS = "VersionTo"; // AS_BIS
    public static final String BK_VAKZ_AB = "StatusFrom"; // VAKZ_AB
    public static final String BK_VAKZ_BIS = "StatusTo"; // VAKZ_BIS
    private static final String BK_REMARK_DIGIT = "RemarkDigit"; // BK-BEMZ
    public static final String BK_ALTERNATIVE_FLAG = "AlternativeFlag"; // BK-WWKB
    public static final String BK_QUANTITY = "Quantity"; // BK_MENGE
    public static final String BK_STEERING_TYPE = "SteeringType"; // BK_LKG
    public static final String BK_MATURITY_LEVEL = "MaturityLevel"; // BK_RF
    public static final String BK_ACQUISITION_TYPE = "AcquisitionType"; // BK_BZA
    public static final String BK_PIPE_PARTS_LIST_FLAG = "PipePartsListFlag"; // BK_LTG_BK
    public static final String BK_PLANTSUPPLIES = "PlantSupplies";
    public static final String BK_PLANTSUPPLY = "PlantSupply"; // WK - Länge: 200 (bis zu 100 2-stellige Werkskennbuchstaben)

    // Nur via TruckBom.foundation
    public static final String BK_TBF_ALTERNATIVE_COMBINATION_IDENTIFIER = "alternativeCombinationIdentifier"; // WwZM - Wahlweise zusammen mit

    // Es reicht ein Helper für die ganze Importdatei. Datensatz-spezifische Werte werden in importRecord() gestezt.
    private BOMConstructionKitImportHelper importHelper;

    public BOMConstructionKitContentUpdateImporter(EtkProject project) {
        super(project, BOMConstructionKitImportHelper.IMPORTER_TITLE_CONTENT, TABLE_DA_EDS_CONST_KIT, IMPORT_TABLENAME,
              new FilesImporterFileListType(TABLE_DA_EDS_CONST_KIT, EDS_SAA_CONSTRUCTION_NAME_UPDATE, false, false, false,
                                            new String[]{ MimeTypes.EXTENSION_XML }));
    }

    @Override
    protected void initXMLMapping(HashMap<String, String> mapping, Set<String> allXMLElementsToConsider, Set<String> allXMLElements) {
        mapping.put(FIELD_DCK_REVFROM, BK_AS_AB);
        mapping.put(FIELD_DCK_REVTO, BK_AS_BIS);
        mapping.put(FIELD_DCK_KEMFROM, BK_ECO_FROM);
        mapping.put(FIELD_DCK_KEMTO, BK_ECO_TO);
        mapping.put(FIELD_DCK_RELEASE_FROM, BK_RELEASE_FROM);
        mapping.put(FIELD_DCK_RELEASE_TO, BK_RELEASE_TO);
        mapping.put(FIELD_DCK_NOTE_ID, BK_REMARK_DIGIT);
        mapping.put(FIELD_DCK_WWKB, BK_ALTERNATIVE_FLAG);
        mapping.put(FIELD_DCK_SUB_SNR, BK_ITEM);
        mapping.put(FIELD_DCK_QUANTITY, BK_QUANTITY);
        mapping.put(FIELD_DCK_STEERING, BK_STEERING_TYPE);
        mapping.put(FIELD_DCK_RFG, BK_MATURITY_LEVEL);
        mapping.put(FIELD_DCK_REPLENISHMENT_KIND, BK_ACQUISITION_TYPE);
        mapping.put(FIELD_DCK_TRANSMISSION_KIT, BK_PIPE_PARTS_LIST_FLAG);
        mapping.put(FIELD_DCK_WWZM, BK_TBF_ALTERNATIVE_COMBINATION_IDENTIFIER);
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        importHelper = new BOMConstructionKitImportHelper(getProject(), getMapping(), getDestinationTable(), BK_RELEASE_FROM,
                                                          BK_RELEASE_TO, BK_QUANTITY, BK_AS_BIS, BK_POSITION);
    }

    @Override
    protected void postImportTask() {
        super.postImportTask();
        importHelper = null;
    }

    @Override
    protected String[] getMustExist() {
        return new String[]{ BK_PARTS_LIST, BK_POSITION, BK_AS_AB };
    }

    @Override
    protected String[] getMustHaveData() {
        return getMustExist();
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        // Im Urladungsimporter sollen nur die Datensätze übernommen werden, die FRG_KZ_AB (hier VAKZ_AB) = "0" haben.
        // Hier im Änderungsdienst kommen laut Daimler nur freigegebene Datensätze an. Somit könnte man hier hart
        // EDSImportHelper.DATA_RELEASED_VALUE ("0") durchreichen. Da via AS-PLM nicht immer die Datensätze so durchgereicht
        // werden, wie es Daimler gerne hätte, frage ich das eigentliche Feld (VAKZ_AB) trotzdem ab. Zusätzlich hat
        // AS-PLM die Eigenschaft gültige bzw leere Elemente nicht aufzuführen, d.h. wenn VAKZ_AB in der XML nicht vorkommt,
        // dann hatte es den Wert "0". Also gebe ich in dem Fall den echten Wert "0" weiter.
        String vakzFromValue = EDSImportHelper.getTrimmedValueFromRecord(importRec, BK_VAKZ_AB);
        if (StrUtils.isEmpty(vakzFromValue)) {
            vakzFromValue = EDSImportHelper.DATA_RELEASED_VALUE;
        }
        // Die Datensatz-spezifischen Werte im Helper setzen
        importHelper.setReleasedValueFrom(vakzFromValue);
        importHelper.setReleasedValueTo(EDSImportHelper.getTrimmedValueFromRecord(importRec, BK_VAKZ_BIS));

        iPartsDataBOMConstKitContent dataObject = importHelper.createConstKitDataObject(this, importRec, recordNo, BK_PARTS_LIST, BK_POSITION, BK_AS_AB);
        if (dataObject != null) {
            importHelper.fillPlantSupplies(dataObject, importRec, BK_PLANTSUPPLIES, BK_PLANTSUPPLY, FIELD_DCK_FACTORY_IDS);
            if (importToDB) {
                saveToDB(dataObject);
            }
        }
    }
}
