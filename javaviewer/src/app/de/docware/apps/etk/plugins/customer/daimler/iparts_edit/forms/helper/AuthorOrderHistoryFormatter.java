/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper;

import de.docware.apps.etk.base.config.db.datatypes.VisObject;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.drawing.DataImageId;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.project.mechanic.ids.TextEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetSource;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictTextKind;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictLanguageMetaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictMetaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictTextKindId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteCatalogueRefId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteMatRefId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.order.iPartsPicOrderModulesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsIncludePartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleEinPASId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsProductSAsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTxtKindIdByMADId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.helper.iPartsSaaPartsListPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsPicOrderId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsWorkBasketFollowUpDateId;
import de.docware.framework.modules.db.serialization.SerializedDBDataObjectAttribute;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

import java.util.Collection;

/**
 * Formatter für die ObjectId bei Anzeige der Historie der ChangeSets
 */
public class AuthorOrderHistoryFormatter implements iPartsConst {

    private static final String OBJECTID_DELIMITER = ", ";
    private static final String TECH_CHANGESET_STRING = "!!TCS: %1 (%2)";

    /**
     * Liefert den Enum-Value eines Technischen ChangeSets
     *
     * @param project
     * @param source
     * @return
     */
    public static String getTechChangeSetName(EtkProject project, iPartsChangeSetSource source) {
        AuthorOrderHistoryFormatter formatter = new AuthorOrderHistoryFormatter(project);
        return formatter.getVisTechChangeSetName(source);
    }

    public static String formatPartId(EtkProject project, PartId partId) {
        AuthorOrderHistoryFormatter formatter = new AuthorOrderHistoryFormatter(project);
        return formatter.formatMatId(partId);
    }

    public static String formatAssemblyId(EtkProject project, AssemblyId assemblyId) {
        AuthorOrderHistoryFormatter formatter = new AuthorOrderHistoryFormatter(project);
        return formatter.formatModuleId(assemblyId);
    }


    private EtkProject project;

    public AuthorOrderHistoryFormatter(EtkProject project) {
        this.project = project;
    }

    public EtkProject getProject() {
        return project;
    }

    private String getDBLanguage() {
        return getProject().getDBLanguage();
    }

    private VisObject getVisObject() {
        return getProject().getVisObject();
    }

    private String visText(String tableName, String fieldName, String strValue) {
        return getVisObject().asText(tableName, fieldName, strValue, getDBLanguage());
    }

    public String findInAttributes(Collection<SerializedDBDataObjectAttribute> serializedDBDataObjectAttributes, String fieldName) {
        if (serializedDBDataObjectAttributes != null) {
            for (SerializedDBDataObjectAttribute serializedDBDataObjectAttribute : serializedDBDataObjectAttributes) {
                if (serializedDBDataObjectAttribute.getName().equals(fieldName)) {
                    return serializedDBDataObjectAttribute.getValue();
                }
            }
        }
        return "";
    }


    public String formatDateTime(String value) {
        return visText(TABLE_DA_INTERNAL_TEXT, FIELD_DIT_CHANGE_DATE, value);
    }

    public String formatSaNo(String matNo) {
        return visText(TABLE_DA_SA, FIELD_DS_SA, matNo);
    }

    public String formatMatNo(String matNo) {
        return visText(TABLE_MAT, FIELD_M_BESTNR, matNo);
    }

    public String formatMatId(PartId partId) {
        return formatObjectNoAndVer(partId.getMatNr(), partId.getMVer(), TABLE_MAT);
    }

    public String formatModuleId(AssemblyId assemblyId) {
        String text = formatMatId(new PartId(assemblyId.getKVari(), assemblyId.getKVer()));
        if (text.isEmpty() && (assemblyId != null) && assemblyId.isValidId()) {
            return formatObjectNoAndVer(assemblyId.getKVari(), assemblyId.getKVer(), TABLE_KATALOG);
        }
        return text;
    }

    public String formatModuleAndLfdNo(PartListEntryId id) {
        return formatModuleAndLfdNo(id.getKVari(), id.getKLfdnr());
    }

    public String formatModuleUsage(iPartsModuleEinPASId moduleEinPASId) {
        StringBuilder str = new StringBuilder();
        str.append(moduleEinPASId.getProductNumber());
        str.append(OBJECTID_DELIMITER);
        str.append(moduleEinPASId.getModuleNumber());
        str.append(OBJECTID_DELIMITER);
        str.append(moduleEinPASId.getSerialNumber());
        return str.toString();
    }

    public String formatFactoryData(iPartsFactoryDataId id) {
        // <DIALOG-Schlüssel>, Werk: <WERK>, ADAT: <ADAT>
        return formatFactoryData(id.getGuid(), id.getFactory(), id.getAdat());
    }

    public String formatFactoryDataAS(iPartsFactoryDataId id) {
        // <DIALOG-Schlüssel>, Werk: <WERK>, ADAT: <ADAT>, Aftersales
        return formatFactoryData(id) + addAftersales();
    }

    public String formatIncludePart(iPartsIncludePartId id) {
        //   // <TU-Nummer>, <laufende Nummer>, <MitlieferTeileNummer>
        return formatIncludePart(id.getIncludeVari(), id.getPredecessorLfdNr(), id.getIncludeReplaceMatNr());
    }

    public String formatFootNote(iPartsFootNoteCatalogueRefId id) {
        // <TU-Nummer>, <laufende Nummer>
        return formatModuleAndLfdNo(id.getPartListEntryId().getKVari(), id.getPartListEntryId().getKLfdnr());
    }

    public String formatCombinedTexte(iPartsCombTextId id) {
        // <TU-Nummer>, <laufende Nummer>
        return formatModuleAndLfdNo(id.getPartListEntryId().getKVari(), id.getPartListEntryId().getKLfdnr());
    }

    public String formatVariantTable(iPartsColorTableToPartId id, Collection<SerializedDBDataObjectAttribute> dataObjectAttributeList) {
        // <Variantentabelle>, <Teilenummer>, KEM Datum ab: <SDATA>
        return formatVariantTable(id.getColorTableId(), id.getSDATA(), dataObjectAttributeList);
    }

    public String formatColorFactoryTable(iPartsColorTableFactoryId id, Collection<SerializedDBDataObjectAttribute> dataObjectAttributeList) {
        // <Variantentabelle>, <Teilenummer>, KEM Datum ab: <SDATA>, Werk: <WERK>, ADAT: <ADAT>
        return formatColorFactoryTable(id.getTableId(), id.getFactory(), id.getAdat(), id.getSdata(), dataObjectAttributeList);
    }

    public String formatColorFactoryTableAS(iPartsColorTableFactoryId id, Collection<SerializedDBDataObjectAttribute> dataObjectAttributeList) {
        // <Variantentabelle>, <Teilenummer>, KEM Datum ab: <SDATA>, Werk: <WERK>, ADAT: <ADAT>, Aftersales
        return formatColorFactoryTable(id, dataObjectAttributeList) + addAftersales();
    }

    public String formatVariantData(iPartsColorTableContentId id) {
        // <Variantentabelle>, <Position>, KEM Datum ab: <SDATA>
        return formatVariantData(id.getColorTableId(), id.getPosition(), id.getSDATA());
    }

    public String formatColorContentFactoryTable(iPartsColorTableFactoryId id) {
        // <Variantentabelle>, <Position>, KEM Datum ab: <SDATA>, Werk: <WERK>, <ADAT>
        return formatColorContentFactoryTable(id.getTableId(), id.getPos(), id.getFactory(), id.getAdat(), id.getSdata());
    }

    public String formatColorContentFactoryTableAS(iPartsColorTableFactoryId id) {
        // <Variantentabelle>, <Position>, KEM Datum ab: <SDATA>, Werk: <WERK>, <ADAT>, Aftersales
        return formatColorContentFactoryTable(id) + addAftersales();
    }

    public String formatResponseData(iPartsResponseDataId id) {
        // <PEM>, Werk: <WERK>, Baureihe: <Baureihe>, Ausführungsart: <AA>,  Baumuster: <Baumuster>, ADAT: <ADAT>
        return formatResponseData(id.getPem(), id.getFactory(), id.getSeriesNo(), id.getBmaa(), id.getBmaa(), id.getAdatAttribute());
    }

    public String formatResponseDataAS(iPartsResponseDataId id) {
        // <PEM>, Werk: <WERK>, Baureihe: <Baureihe>, Ausführungsart: <AA>,  Baumuster: <Baumuster>, ADAT: <ADAT>, Aftersales
        return formatResponseData(id) + addAftersales();
    }

    public String formatResponseSpike(iPartsResponseSpikeId id) {
        // <PEM>, Werk: <WERK>, Baureihe: <Baureihe>, Ausführungsart: <AA>,  Baumuster: <Baumuster>, ADAT: <ADAT>, Ident: <Ident>
        return formatResponseSpike(id.getPem(), id.getFactory(), id.getSeriesNo(), id.getAusfuehrungsArt(), id.getBmaa(), id.getAdatAttribute(), id.getIdent());
    }

    public String formatResponseSpikeAS(iPartsResponseSpikeId id) {
        // <PEM>, Werk: <WERK>, Baureihe: <Baureihe>, Ausführungsart: <AA>,  Baumuster: <Baumuster>, ADAT: <ADAT>, Ident: <Ident>, Aftersales
        return formatResponseSpike(id) + addAftersales();
    }

    public String formatPicOrderModule(iPartsPicOrderModulesId id) {
        // <TU-Nummer>, Bildauftrag: <Mediencontainer>
        return formatPicOrderModule(id.getModuleNo(), id.getOrderGuid()); //!!
    }

    public String formatPicOrder(iPartsPicOrderId id) {
        iPartsDataPicOrder picOrder = new iPartsDataPicOrder(project, id);
        StringBuilder str = new StringBuilder();
        if (picOrder.existsInDB()) {
            str.append(picOrder.getOrderIdExtern());
            str.append(OBJECTID_DELIMITER);
            str.append("Rev: " + picOrder.getOrderRevisionExtern());
            String proposedName = picOrder.getProposedName();
            if (StrUtils.isValid(proposedName)) {
                str.append(OBJECTID_DELIMITER);
                str.append(proposedName);
            }
            str.append(OBJECTID_DELIMITER);
            str.append(id.getOrderGuid());
        } else {
            str.append(id.getOrderGuid());
        }
        return str.toString();
    }

    public String formatPicDataImage(DataImageId id, Collection<SerializedDBDataObjectAttribute> dataObjectAttributeList) {
        // <TU-Nummer>, Bildtafel: <Bildtafelnummer>
        return formatPicDataImage(id.getITiffName(), dataObjectAttributeList);
    }

    public String formatPartFootnote(iPartsFootNoteMatRefId id) {
        // // <Teilenummer>
        return formatMatNo(id.getMatNumber());
    }

    public String formatProduct(iPartsProductId id) {
        return formatProduct(id.getProductNumber());
    }

    public String formatKGTU(iPartsDataKgTuAfterSalesId id) {
        return formatKGTU(id.getProduct(), id.getKg(), id.getTu());
    }

    public String formatProductKgSa(iPartsProductSAsId productSAsId) {
        return formatSATU(productSAsId.getProductNumber(), productSAsId.getKG(), productSAsId.getSaNumber());
    }

    public String formatDialogChange(String[] pkValues, String description) {
        String value = "";
        if ((pkValues != null) && (pkValues.length >= 2)) {
            value = IdWithType.fromDBString(pkValues[0], pkValues[1]).toStringForLogMessages();
        }
        return TranslationHandler.translate("!!%1 %2", TranslationHandler.translate(description), value);
    }

    public String formatMatETKZapproved(iPartsDialogChangesId dialogChangesId, String description) {
        if ((dialogChangesId != null) && dialogChangesId.isValidId()) {
            IdWithType id = IdWithType.fromDBString(dialogChangesId.getType(), dialogChangesId.getDoId());
            PartId partId = new PartId(id.toStringArrayWithoutType());
            return TranslationHandler.translate("%1 \"%2\"", TranslationHandler.translate(description), formatMatNo(partId.getMatNr()));
        }
        return "";
    }

    public String formatTechChangeSetName(iPartsChangeSetSource source, int index) {
        return formatTechChangeSetName(source, String.valueOf(index));
    }

    public String formatTechChangeSetName(iPartsChangeSetSource source, String identifier) {
        String sourceName = getVisTechChangeSetName(source);
        return TranslationHandler.translate(TECH_CHANGESET_STRING, sourceName, identifier);
    }

    public String getVisTechChangeSetName(iPartsChangeSetSource source) {
        if (source == null) {
            return "";
        }
        return visText(TABLE_DA_CHANGE_SET, FIELD_DCS_SOURCE, source.name());
    }

    public String formatSeries(iPartsSeriesId seriesId) {
        return seriesId.getSeriesNumber();
    }

    public String formatDictMeta(iPartsDictMetaId dictMetaId) {
        String description = dictMetaId.getTextKindId();
        iPartsDictTextKindId textKindId = new iPartsDictTextKindId(description);
        iPartsDataDictTextKind dataDictTextKind = DictTxtKindIdByMADId.getInstance(getProject()).findDictTextKindByTextKindId(textKindId, getProject());
        if (dataDictTextKind != null) {
            description = dataDictTextKind.getName(getProject().getDBLanguage());
        }
        return makeDelimitedString(description, dictMetaId.getTextId());
    }

    public String formatDictLangMeta(iPartsDictLanguageMetaId dictMetaLangId) {
        return makeDelimitedString(dictMetaLangId.getTextId(), dictMetaLangId.getLanguage());
    }

    public String formatLanguage(TextEntryId textEntryId) {
        return makeDelimitedString(textEntryId.getField(), textEntryId.getLanguage(), textEntryId.getTextNr());
    }

    public String formatPEM(iPartsPemId pemId) {
        return makeDelimitedString(pemId.getPEM(), pemId.getFactoryNo());
    }

    public String formatSAA(iPartsNutzDokSAAId nutzDokSaaId) {
        return formatSAA(nutzDokSaaId.getSAANo());
    }

    public String formatSAA(String saaNumber) {
        return visText(TABLE_DA_SAA, FIELD_DS_SAA, saaNumber);
    }

    public String formatKEM(iPartsNutzDokKEMId nutzDokKemId) {
        return nutzDokKemId.getKEMNo();
    }

    public String formatKEMWorkBasketEDS(iPartsKEMWorkBasketEDSId kemWorkBasketEDSId) {
        // String kemNo, String saaNo, String productNo, String kgNo, String moduleNo
        return makeDelimitedString(kemWorkBasketEDSId.getKEMNo(), formatSAA(kemWorkBasketEDSId.getSAANo()),
                                   kemWorkBasketEDSId.getProductNo(), kemWorkBasketEDSId.getKgNo(),
                                   kemWorkBasketEDSId.getModuleNo());
    }

    public String formatKEMWorkBasketMBS(iPartsKEMWorkBasketMBSId kemWorkBasketMBSId) {
        // String kemNo, String saaNo, String group, String productNo, String kgNo, String moduleNo
        return makeDelimitedString(kemWorkBasketMBSId.getKEMNo(), formatSAA(kemWorkBasketMBSId.getSAANo()),
                                   kemWorkBasketMBSId.getGroup(), kemWorkBasketMBSId.getProductNo(), kemWorkBasketMBSId.getKgNo(),
                                   kemWorkBasketMBSId.getModuleNo());
    }

    public String formatWorkBasketSaaStates(iPartsWorkBasketSaaStatesId wbSaaStatesId) {
        // MODEL_NO, PRODUCT_NO, SAA_NO, SOURCE
        return makeDelimitedString(wbSaaStatesId.getModelNo(), wbSaaStatesId.getProductNo(),
                                   formatSaaBkNumber(wbSaaStatesId.getSAANo()), wbSaaStatesId.getSource());
    }

    public String formatSaaBkNumber(String saaBkNumber) {
        iPartsNumberHelper helper = new iPartsNumberHelper();
        if (helper.isValidSaa(saaBkNumber)) {
            return formatSAA(saaBkNumber);
        } else {
            return formatMatNo(saaBkNumber);
        }
    }

    public String formatSaaModels(iPartsSAAModelsId saaModelsId) {
        // String saaNumber, String modelNumber
        return makeDelimitedString(formatSAA(saaModelsId.getSAANumber()), saaModelsId.getModelNumber());
    }

    public String formatModelAggs(iPartsModelsAggsId modelAggsId) {
        // String modelNumber, String aggregateModelNumber
        return makeDelimitedString(modelAggsId.getModelNumber(), modelAggsId.getAggregateModelNumber());
    }

    public String formatWorkBasketFollowUpDate(iPartsWorkBasketFollowUpDateId wbFollowUpDateId) {
        // iPartsWorkBasketTypes wbType, String saaBkKemNo
        String value;
        if (wbFollowUpDateId.isKEM()) {
            value = wbFollowUpDateId.getKEM();
        } else {
            value = formatSAA(wbFollowUpDateId.getSaaBk());
        }
        return makeDelimitedString(wbFollowUpDateId.getWbType().name(), value);
    }

    public String formatConstructionPrimaryKey(String guid) {
        iPartsDialogBCTEPrimaryKey bcteKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(guid);
        if (bcteKey != null) {
            return formatDIALOGKey(bcteKey);
        }
        iPartsSaaPartsListPrimaryKey saaPartsListKey = iPartsSaaPartsListPrimaryKey.createFromSaaPartsListGuid(guid);
        if (saaPartsListKey != null) {
            return formatSaaPartsListKey(saaPartsListKey);
        }
        return guid;
    }

    public String formatDIALOGKey(String guid) {
        iPartsDialogBCTEPrimaryKey bcteKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(guid);
        if (bcteKey != null) {
            return formatDIALOGKey(bcteKey);
        }
        return guid;
    }

    public String formatSaaPartsListKey(String guid) {
        iPartsSaaPartsListPrimaryKey saaPartsListKey = iPartsSaaPartsListPrimaryKey.createFromSaaPartsListGuid(guid);
        if (saaPartsListKey != null) {
            return formatSaaPartsListKey(saaPartsListKey);
        }
        return guid;
    }

    public String formatHmMSm(String hmMSmValue) {
        HmMSmId hmMSmId = HmMSmId.getHmMSmIdFromDIALOGSourceContext(hmMSmValue);
        if (hmMSmId != null) {
            return formatHmMSm(hmMSmId.getSeries(), hmMSmId.getHm(), hmMSmId.getM(), hmMSmId.getSm());
        }
        return hmMSmValue;
    }

    public String formatModuleAndLfdNo(String moduleNo, String lfdNo) {
        return makeDelimitedString(moduleNo, lfdNo);
    }

    public String formatMatAndLfdNo(String matNo, String lfdNo) {
        return makeDelimitedString(formatMatNo(matNo), lfdNo);
    }

    public String formatIncludePart(String moduleNo, String lfdNo, String includeMatNo) {
        return makeDelimitedString(formatModuleAndLfdNo(moduleNo, lfdNo), formatMatNo(includeMatNo));
    }


    protected String formatFactoryData(String guid, String factory, String aDat) {
        // <DIALOG-Schlüssel>, Werk: <WERK>, ADAT: <ADAT>
        return makeDelimitedString(formatDIALOGKey(guid), formatFactory(factory), formatADat(aDat));
    }

    protected String addAftersales() {
        return OBJECTID_DELIMITER + TranslationHandler.translate("!!Aftersales");
    }

    protected String formatVariantTable(String tableId, String sDatA, Collection<SerializedDBDataObjectAttribute> dataObjectAttributeList) {
        // <Variantentabelle>, <Teilenummer>, KEM Datum ab: <SDATA>
        String partNo = findInAttributes(dataObjectAttributeList, FIELD_DCTP_PART);
        return makeDelimitedString(tableId, formatMatNo(partNo), formatKemDatumAb(sDatA));
    }

    protected String formatColorFactoryTable(String tableId, String factory, String aDat, String sDatA, Collection<SerializedDBDataObjectAttribute> dataObjectAttributeList) {
        // <Variantentabelle>, <Teilenummer>, KEM Datum ab: <SDATA>, Werk: <WERK>, ADAT: <ADAT>
        String partNo = "??";// todo woher?
        return makeDelimitedString(tableId, partNo, formatKemDatumAb(sDatA), formatFactory(factory), formatADat(aDat));
    }

    protected String formatVariantData(String tableId, String pos, String sDatA) {
        // <Variantentabelle>, <Position>, KEM Datum ab: <SDATA>
        return makeDelimitedString(tableId, pos, formatKemDatumAb(sDatA));
    }

    protected String formatColorContentFactoryTable(String tableId, String pos, String factory, String aDat, String sDatA) {
        // <Variantentabelle>, <Position>, KEM Datum ab: <SDATA>, Werk: <WERK>, <ADAT>
        return makeDelimitedString(formatVariantData(tableId, pos, sDatA), formatFactory(factory), formatADat(aDat));
    }

    protected String formatResponseData(String pem, String factory, String seriesNo, String aa, String modelNo, String aDat) {
        // <PEM>, Werk: <WERK>, Baureihe: <Baureihe>, Ausführungsart: <AA>,  Baumuster: <Baumuster>, ADAT: <ADAT>
        return makeDelimitedString(pem, formatFactory(factory), formatSeries(seriesNo), formatAusfuehrungsart(aa),
                                   formatModel(modelNo), formatADat(aDat));
    }

    protected String formatResponseSpike(String pem, String factory, String seriesNo, String aa, String modelNo, String aDat, String ident) {
        // <PEM>, Werk: <WERK>, Baureihe: <Baureihe>, Ausführungsart: <AA>,  Baumuster: <Baumuster>, ADAT: <ADAT>, Ident: <Ident>
        return makeDelimitedString(formatResponseData(pem, factory, seriesNo, aa, modelNo, aDat), formatIdent(ident));
    }

    protected String formatPicOrderModule(String moduleNo, String picOrderGuid) {
        //   // <TU-Nummer>, Bildauftrag: <Mediencontainer>
        StringBuilder str = new StringBuilder();
        str.append(moduleNo);
        str.append(OBJECTID_DELIMITER);
        str.append(picOrderGuid); // todo hier aus TABLE_DA_PICORDER mit Guid die Spalte FIELD_PO_ORDER_ID_EXTERN holen
        return str.toString();
    }

    protected String formatPicDataImage(String moduleNo, Collection<SerializedDBDataObjectAttribute> dataObjectAttributeList) {
        // <TU-Nummer>, Bildtafel: <Bildtafelnummer>
        String picNo = findInAttributes(dataObjectAttributeList, FIELD_I_IMAGES);
        return makeDelimitedString(moduleNo, formatBildTafel(picNo));
    }

    protected String formatKGTU(String product, String kg, String tu) {
        StringBuilder str = new StringBuilder();
        str.append(formatProduct(product));
        str.append(OBJECTID_DELIMITER);
        str.append(formatKG(kg));
        if (StrUtils.isValid(tu)) {
            str.append(OBJECTID_DELIMITER);
            str.append(formatTU(tu));
        }
        return str.toString();
    }

    protected String formatSATU(String product, String kg, String sa) {
        StringBuilder str = new StringBuilder();
        str.append(formatProduct(product));
        str.append(OBJECTID_DELIMITER);
        str.append(formatKG(kg));
        if (!StrUtils.isEmpty(sa)) {
            str.append(OBJECTID_DELIMITER);
            str.append(formatSA(sa));
        }
        return str.toString();
    }

    protected String formatDIALOGKey(iPartsDialogBCTEPrimaryKey bcteKey) {
        if (bcteKey != null) {
            StringBuilder str = new StringBuilder();
            // C205 050505 0100 0010 - AA: FW - KEM Termin Ab: 05.03.2018
            str.append(formatHmMSm(bcteKey.seriesNo, bcteKey.hm, bcteKey.m, bcteKey.sm));
            str.append(" ");
            str.append(bcteKey.getPosE());
            str.append(" ");
            str.append(bcteKey.getPosV());
            str.append(" - AA: ");
            str.append(bcteKey.getAA());
            str.append(" - ");
            str.append(TranslationHandler.translate("!!KEM Termin Ab: %1", formatDateTime(bcteKey.getSData())));
            return str.toString();
        }
        return "";
    }

    protected String formatSaaPartsListKey(iPartsSaaPartsListPrimaryKey saaPartsListKey) {
        if (saaPartsListKey != null) {
            StringBuilder str = new StringBuilder();
            str.append(TranslationHandler.translate("!!Obere SNr."));
            str.append(" ");
            str.append(formatMatNo(saaPartsListKey.getSaaBkNo()));
            str.append(" ");
            str.append("Pos");
            str.append(" ");
            str.append(saaPartsListKey.getPos());
            str.append(" ");
            str.append("Rev From");
            str.append(" ");
            str.append(saaPartsListKey.getRevFrom());
            str.append(" ");
            str.append("Kem From");
            str.append(" ");
            str.append(saaPartsListKey.getKemFrom());
            return str.toString();
        }
        return "";
    }

    protected String formatHmMSm(String series, String hm, String m, String sm) {
        StringBuilder str = new StringBuilder();
        // C205 050505
        str.append(series);
        str.append(" ");
        str.append(hm);
        str.append(m);
        str.append(sm);
        return str.toString();
    }

    protected String formatKemDatumAb(String sDatA) {
        return TranslationHandler.translate("!!KEM Datum ab: %1", formatDateTime(sDatA));
    }

    protected String formatADat(String aDat) {
        return TranslationHandler.translate("!!ADAT: %1", formatDateTime(aDat));
    }

    protected String formatFactory(String factory) {
        return TranslationHandler.translate("!!Werk : %1", factory);
    }

    protected String formatSeries(String seriesNo) {
        return TranslationHandler.translate("!!Baureihe: %1", seriesNo);
    }

    protected String formatModel(String modelNo) {
        return TranslationHandler.translate("!!Baumuster: %1", modelNo);
    }

    protected String formatAusfuehrungsart(String aa) {
        return TranslationHandler.translate("!!Ausführungsart: %1", aa);
    }

    protected String formatIdent(String ident) {
        return TranslationHandler.translate("!!Ident: %1", ident);
    }

    protected String formatBildTafel(String iBlatt) {
        return TranslationHandler.translate("!!Bildtafel: %1", iBlatt);
    }

    protected String formatProduct(String productNo) {
        return TranslationHandler.translate("!!Produkt: %1", productNo);
    }

    protected String formatKG(String kg) {
        return TranslationHandler.translate("!!KG: %1", kg);
    }

    protected String formatTU(String tu) {
        return TranslationHandler.translate("!!TU: %1", tu);
    }

    protected String formatSA(String sa) {
        return TranslationHandler.translate("!!SA: %1", formatSaNo(sa));
    }

    protected String makeDelimitedString(String... strings) {
        return StrUtils.makeDelimitedString(OBJECTID_DELIMITER, strings);
    }

    protected String formatObjectNoAndVer(String objectNo, String objectVer, String tableName) {
        String text = "";
        if (!objectNo.isEmpty()) {
            String numberFieldName = "";
            String verFieldName = "";
            if (tableName.equals(TABLE_MAT)) {
                numberFieldName = FIELD_M_BESTNR;
                verFieldName = FIELD_M_VER;
            } else if (tableName.equals(TABLE_KATALOG)) {
                numberFieldName = FIELD_K_VARI;
                verFieldName = FIELD_K_VER;
            }
            if (StrUtils.isValid(numberFieldName)) {
                text = visText(tableName, numberFieldName, objectNo);
                if (!objectVer.isEmpty()) {
                    text += "V " + visText(tableName, verFieldName, objectVer);
                }
            }
        }
        return text;
    }

}
