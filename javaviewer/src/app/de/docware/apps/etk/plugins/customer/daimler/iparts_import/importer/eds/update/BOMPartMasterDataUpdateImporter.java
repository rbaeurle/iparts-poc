/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.update;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMXMLDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.BOMPartMasterDataImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Importer für die Teilestammdaten aus der BOM-DB (Änderungsdienst)
 */
public class BOMPartMasterDataUpdateImporter extends AbstractBOMXMLDataImporter {

    public static final String IMPORT_TABLENAME = "getT43RTEIL";

    public static final String TEIL_PART_NUMBER = "PartNumber"; // SNR - Länge: 13
    public static final String TEIL_PART_LANG_DATA = "PartLangData";
    public static final String TEIL_PART_LANG_DATA_ATTRIBUTE = "language";
    public static final String TEIL_DESCRIPTION = "Description"; // BEN - Länge 50
    public static final String TEIL_REMARK = "Remark"; // BEM - Länge: 114
    private static final String TEIL_S05_REMARKS = "S05Remarks";
    private static final String TEIL_S05_REMARK = "S05Remark"; // BEMZ_TEXT
    public static final String TEIL_RELEASE_FROM = "ReleaseDateFrom"; // FRG_DAT_AB
    public static final String TEIL_RELEASE_TO = "ReleaseDateTo"; // FRG_DAT_BIS
    public static final String TEIL_ECO_FROM = "EcoFrom"; // KEM_AB
    public static final String TEIL_ECO_TO = "EcoTo"; // KEM_BIS
    public static final String TEIL_AS_AB = "VersionFrom"; // AS_AB
    private static final String TEIL_AS_BIS = "VersionTo"; // AS_BIS
    public static final String TEIL_VAKZ_AB = "StatusFrom"; // VAKZ_AB
    public static final String TEIL_VAKZ_BIS = "StatusTo"; // VAKZ_BIS
    public static final String TEIL_PARTS_TYPE = "PartsType"; // ZBKZ - Mögliche Werte: ZB (assembly), EB (installation drawing), AO (arrangement drawing), LU (supplier part), TB (table drawing) etc.
    public static final String TEIL_DRAWING_GEOMETRY_VERSION = "DrawingGeometryVersion"; // ZGS - Länge: 4
    public static final String TEIL_DRAWING_DATE_OR_TYPE = "DrawingDateOrType"; // ZDAT - Länge: 6
    public static final String TEIL_REFERENCE_PART = "ReferencePart"; // ZSN
    public static final String TEIL_REFERENCE_DRAWING = "ReferenceDrawing"; // HZSN
    public static final String TEIL_PART_RELEASE_STATUS = "PartReleaseStatus"; // FGST - Länge: 2
    public static final String TEIL_QUANTITY_UNIT = "QuantityUnit"; // ME - Länge: 2
    public static final String TEIL_COLOR_ITEM_TYPE = "ColorItemType"; // KZFRB - Länge: 1
    public static final String TEIL_DOCUMENTATION_OBLIGATION = "DocumentationObligation"; // DFPL - Länge: 1
    public static final String TEIL_CALCULATED_WEIGHT = "CalculatedWeight"; // BGEW - Float: 0 - 999.999
    public static final String TEIL_SAFETY_RELEVANT = "SafetyRelevant"; // DS - Länge: 1
    public static final String TEIL_CERT_RELEVANT = "CertificationRelevant"; // DZ - Länge: 1
    public static final String TEIL_VEHICLE_DOC_RELEVANT = "VehicleDocumentationRelevant"; // FDOK - Länge: 1
    public static final String TEIL_THEFT_RELEVANT = "TheftRelevant"; // DRT - Länge: 1
    public static final String TEIL_PLANTSUPPLIES = "PlantSupplies";
    public static final String TEIL_PLANTSUPPLY = "PlantSupply"; // WK - Länge: 200 (bis zu 100 2-stellige Werkskennbuchstaben)

    private static final Map<String, String> EXTERNAL_TEXT_MAPPING = new HashMap<>();

    static {
        EXTERNAL_TEXT_MAPPING.put(TEIL_DESCRIPTION, FIELD_M_CONST_DESC);
        EXTERNAL_TEXT_MAPPING.put(TEIL_REMARK, FIELD_M_NOTEONE);
    }

    private HashMap<iPartsPartId, BOMPartMasterDataImportHelper.PartWithVersion> partsWithNewestVersion;

    public BOMPartMasterDataUpdateImporter(EtkProject project) {
        super(project, "!!BOM Teilestammdaten (TEIL)", TABLE_MAT, IMPORT_TABLENAME,
              new FilesImporterFileListType(TABLE_MAT, BOM_PART_MASTERDATA_UPDATE, false, false, false, new String[]{ MimeTypes.EXTENSION_XML }));
    }

    @Override
    protected void initXMLMapping(HashMap<String, String> mapping, Set<String> allXMLElementsToConsider, Set<String> allXMLElements) {
        mapping.put(FIELD_M_ASSEMBLYSIGN, TEIL_PARTS_TYPE);
        mapping.put(FIELD_M_IMAGESTATE, TEIL_DRAWING_GEOMETRY_VERSION);
        mapping.put(FIELD_M_IMAGEDATE, TEIL_DRAWING_DATE_OR_TYPE);
        mapping.put(FIELD_M_RELATEDPIC, TEIL_REFERENCE_PART);
        mapping.put(FIELD_M_REFSER, TEIL_REFERENCE_DRAWING);
        mapping.put(FIELD_M_RELEASESTATE, TEIL_PART_RELEASE_STATUS);
        mapping.put(FIELD_M_QUANTUNIT, TEIL_QUANTITY_UNIT);
        mapping.put(FIELD_M_VARIANT_SIGN, TEIL_COLOR_ITEM_TYPE);
        mapping.put(FIELD_M_DOCREQ, TEIL_DOCUMENTATION_OBLIGATION);
        mapping.put(FIELD_M_WEIGHTCALC, TEIL_CALCULATED_WEIGHT);
        mapping.put(FIELD_M_SECURITYSIGN, TEIL_SAFETY_RELEVANT);
        mapping.put(FIELD_M_CERTREL, TEIL_CERT_RELEVANT);
        mapping.put(FIELD_M_VEDOCSIGN, TEIL_VEHICLE_DOC_RELEVANT);
        mapping.put(FIELD_M_THEFTRELINFO, TEIL_THEFT_RELEVANT);

        allXMLElementsToConsider.addAll(mapping.values());
        allXMLElementsToConsider.add(TEIL_PART_LANG_DATA);
        allXMLElementsToConsider.add(TEIL_PART_LANG_DATA_ATTRIBUTE);
        allXMLElementsToConsider.add(TEIL_VAKZ_AB);
        allXMLElementsToConsider.add(TEIL_VAKZ_BIS);
        allXMLElementsToConsider.add(TEIL_PLANTSUPPLIES);
        allXMLElementsToConsider.add(TEIL_PLANTSUPPLY);
        allXMLElementsToConsider.add(TEIL_AS_AB);
        allXMLElementsToConsider.add(TEIL_AS_BIS);
        allXMLElementsToConsider.add(TEIL_ECO_FROM);
        allXMLElementsToConsider.add(TEIL_ECO_TO);
        allXMLElementsToConsider.add(TEIL_RELEASE_FROM);
        allXMLElementsToConsider.add(TEIL_RELEASE_TO);
        allXMLElementsToConsider.add(TEIL_S05_REMARKS);
        allXMLElementsToConsider.add(TEIL_S05_REMARK);
        allXMLElementsToConsider.add(TEIL_REMARK);

        allXMLElements.addAll(allXMLElementsToConsider);
    }

    @Override
    protected String[] getMustExist() {
        return new String[]{ TEIL_PART_NUMBER };
    }

    @Override
    protected String[] getMustHaveData() {
        return getMustExist();
    }

    @Override
    protected void preImportTask() {
        partsWithNewestVersion = new HashMap<>();
        super.preImportTask();
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        BOMPartMasterDataImportHelper importHelper = new BOMPartMasterDataImportHelper(this, getProject(), getMapping(), getDestinationTable(),
                                                                                       TEIL_SAFETY_RELEVANT, TEIL_CERT_RELEVANT,
                                                                                       TEIL_VEHICLE_DOC_RELEVANT, TEIL_THEFT_RELEVANT,
                                                                                       TEIL_RELEASE_FROM, TEIL_RELEASE_TO);
        importHelper.prepareXMLImportRec(importRec, getAllXMLElementsToConsider(), getAllXMLElements());
        if (!importHelper.isValidRecord(importRec, TEIL_VAKZ_AB)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 wird übersprungen: \"%2\" enthält \"%3\" und ist nicht leer.",
                                                        String.valueOf(recordNo), TEIL_VAKZ_AB, importHelper.handleValueOfSpecialField(TEIL_VAKZ_AB, importRec)),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }
        String releaseDate = importHelper.handleValueOfSpecialField(TEIL_RELEASE_FROM, importRec);
        if (StrUtils.isEmpty(releaseDate)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 wird übersprungen: \"%2\" enthält \"%3\" und ist kein gültiges Freigabedatum.",
                                                        String.valueOf(recordNo), TEIL_RELEASE_FROM, importHelper.handleValueOfSpecialField(TEIL_RELEASE_FROM, importRec)),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }
        String partNumber = importHelper.handleValueOfSpecialField(TEIL_PART_NUMBER, importRec);
        if (StrUtils.isEmpty(partNumber)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 wird übersprungen: \"%2\" enthält \"%3\" und ist keine gültige Teilenummer.",
                                                        String.valueOf(recordNo), TEIL_PART_NUMBER, importHelper.handleValueOfSpecialField(TEIL_PART_NUMBER, importRec)),
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
            if (!importHelper.checkDataPartValidity(this, dataPart, releaseDate, TEIL_RELEASE_FROM, recordNo, iPartsImportDataOrigin.DIALOG)) {
                reduceRecordCount();
                return;
            }

            // Befülle das Material mit Mapping und Default Werten
            importHelper.fillPartMasterData(importHelper, dataPart, importRec, releaseDate);
            // Spezielle Felder setzen
            importHelper.fillPlantSupplies(dataPart, importRec, TEIL_PLANTSUPPLIES, TEIL_PLANTSUPPLY, FIELD_M_FACTORY_IDS);
            // Texte setzen
            setTextFromImportRecord(importHelper, importRec, dataPart);
            setExternalTexts(dataPart, EXTERNAL_TEXT_MAPPING);
            importHelper.checkAndFillNewerPartData(dataPart, importRec, TEIL_AS_AB, partsWithNewestVersion);
        }
    }


    /**
     * Setzt den Text aus dem <code>importRec</code>
     *
     * @param importHelper
     * @param importRec
     * @param dataPart
     */
    private void setTextFromImportRecord(BOMPartMasterDataImportHelper importHelper, Map<String, String> importRec, iPartsDataPart dataPart) {
        iPartsEDSLanguageDefs languageDefs = importHelper.getEDSLanguageDefFromAttribute(TEIL_PART_LANG_DATA, TEIL_PART_LANG_DATA_ATTRIBUTE, importRec);
        String description = importHelper.handleValueOfSpecialField(TEIL_DESCRIPTION, importRec);
        // Check, ob die richtige Sprache gesetzt wurde
        if ((languageDefs == iPartsEDSLanguageDefs.EDS_DE)) {
            // Konstruktionsbenennung
            if (!importHelper.skipFieldIfOlderVersion(FIELD_M_CONST_DESC)) {
                EtkMultiSprache multiLang = dataPart.getFieldValueAsMultiLanguage(FIELD_M_CONST_DESC);
                if (multiLang == null) {
                    multiLang = new EtkMultiSprache();
                }
                multiLang.setText(languageDefs.getDbValue(), description);
                dataPart.setFieldValueAsMultiLanguage(FIELD_M_CONST_DESC, multiLang, DBActionOrigin.FROM_EDIT);
            }

            // Bemerkung
            String remark = importHelper.handleValueOfSpecialField(TEIL_REMARK, importRec);
            EtkMultiSprache remarkMulti = dataPart.getFieldValueAsMultiLanguage(FIELD_M_NOTEONE);
            if (remarkMulti == null) {
                remarkMulti = new EtkMultiSprache();
            }
            remarkMulti.setText(iPartsEDSLanguageDefs.EDS_DE.getDbValue(), remark);
            dataPart.setFieldValueAsMultiLanguage(FIELD_M_NOTEONE, remarkMulti, DBActionOrigin.FROM_EDIT);
        }
    }

    @Override
    protected void postImportTask() {
        if (!isCancelled()) {
            if (importToDB) {
                BOMPartMasterDataImportHelper.handlePartMasterDataPostImport(this, partsWithNewestVersion);
            }
        }
        partsWithNewestVersion.clear();
        super.postImportTask();
    }
}