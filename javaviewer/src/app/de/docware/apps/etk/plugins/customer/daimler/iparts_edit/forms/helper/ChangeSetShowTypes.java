/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.drawing.DataImageId;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.project.mechanic.ids.TextEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsChangeSetInfoDefinitions;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsFactoryDataTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetSource;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictLanguageMetaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictMetaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteCatalogueRefId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteMatRefId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.order.iPartsPicOrderModulesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsIncludePartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsReplacePartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleEinPASId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsProductModelsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsProductSAsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsPicOrderId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsWorkBasketFollowUpDateId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditSelectSAABKForm;
import de.docware.framework.modules.config.db.DBConst;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.serialization.*;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.SQLStringConvert;

import java.util.*;

/**
 * Klasse für die Verwaltung und Anzeige der ObjectTypes für die Historie (Autoren-Auftrag und DataObjects)
 */
public class ChangeSetShowTypes implements iPartsConst {

    public final static int MAX_LENGTH_FOR_ARRAYS = 76;
    public final String[] DONT_SHOW_ATTRIBUTE_NAMES = new String[]{ DBConst.FIELD_STAMP };

    /**
     * WICHTIG: wird ein neues enum hinzugefügt, so muss die Methode "calculateObjectId" erweitert werden!!
     */
    public enum SHOW_TYPES {
        UNKNOWN("", "!!Unbekannt", "", "", false, true),

        PART_LIST_ENTRY(PartListEntryId.TYPE, PartListEntryId.DESCRIPTION, "!!Teileposition", "", false, true), // eigene Benennung bei Daimler // -> Teileposition
        FACTORY_DATA(iPartsFactoryDataId.TYPE, iPartsFactoryDataId.DESCRIPTION, "!!Teileposition - Werkseinsatzdaten", "", false, true), // -> Teileposition - Werkseinsatzdaten (Konstruktion)
        FACTORY_DATA_AS(iPartsFactoryDataId.TYPE, iPartsFactoryDataId.DESCRIPTION, "!!Teileposition - Werkseinsatzdaten AS", "", true, true), // -> Teileposition - Werkseinsatzdaten (Aftersales)
        REPLACE_PART(iPartsReplacePartId.TYPE, iPartsReplacePartId.DESCRIPTION, "!!Teileposition - Ersetzungen", "", false, true), // -> Teileposition - Ersetzungen
        INCLUDE_PART(iPartsIncludePartId.TYPE, iPartsIncludePartId.DESCRIPTION, "!!Teileposition - Ersetzungen - Mitlieferteile", "", false, true), // -> Teileposition - Ersetzungen
        INTERNAL_TEXT(iPartsDataInternalTextId.TYPE, iPartsDataInternalTextId.DESCRIPTION, "!!Teileposition - Interner Text", "", false, true), // -> Teileposition - Interner Text
        FOOTNOTES(iPartsFootNoteCatalogueRefId.TYPE, iPartsFootNoteCatalogueRefId.DESCRIPTION, "!!Teileposition - Fußnoten", "", false, true), // Teileposition - Fußnoten (iPartsFootNoteCatalogueRefId)
        COMBTEXT(iPartsCombTextId.TYPE, iPartsCombTextId.DESCRIPTION, "!!Teileposition - Kombinierter Text", "", false, true), // Teileposition - Kombinierter Text (iPartsCombTextId)

        VARIANT_TABLE_TO_PART(iPartsColorTableToPartId.TYPE, iPartsColorTableToPartId.DESCRIPTION, "!!Variantentabelle - Teil", "", false, true), // Variantentabelle - Teil
        COLOR_TABLE_FACTORY_DATA(iPartsColorTableFactoryId.TYPE, iPartsColorTableFactoryId.DESCRIPTION, "!!Variantentabelle - Teil - Werkseinsatzdaten",
                                 iPartsChangeSetInfoDefinitions.SPECIAL_INFO_DEF_PREFIX, false, true), // Unterscheidung notwendig // -> Variantentabelle - Teil - Werkseinsatzdaten (auch AS je nach Kenner)
        COLOR_TABLE_FACTORY_DATA_AS(iPartsColorTableFactoryId.TYPE, iPartsColorTableFactoryId.DESCRIPTION, "!!Variantentabelle - Teil - Werkseinsatzdaten AS",
                                    iPartsChangeSetInfoDefinitions.SPECIAL_INFO_DEF_PREFIX, true, true), // Unterscheidung notwendig // -> Variantentabelle - Teil - Werkseinsatzdaten (auch AS je nach Kenner)
        VARIANT_DATA(iPartsColorTableContentId.TYPE, iPartsColorTableContentId.DESCRIPTION, "!!Variantentabelle - Variante", "", false, true), // Variantentabelle - Variante
        COLOR_TABLE_CONTENT_FACTORY_DATA(iPartsColorTableFactoryId.TYPE, iPartsColorTableFactoryId.DESCRIPTION, "!!Variantentabelle - Variante - Werkseinsatzdaten", "", false, true), // Unterscheidung notwendig // -> Variantentabelle - Variante - Werkseinsatzdaten (auch AS je nach Kenner)
        COLOR_TABLE_CONTENT_FACTORY_DATA_AS(iPartsColorTableFactoryId.TYPE, iPartsColorTableFactoryId.DESCRIPTION, "!!Variantentabelle - Variante - Werkseinsatzdaten AS", "", true, true), // Unterscheidung notwendig // -> Variantentabelle - Variante - Werkseinsatzdaten (auch AS je nach Kenner)

        RESPONSE_DATA(iPartsResponseDataId.TYPE, iPartsResponseDataId.DESCRIPTION, "!!Rückmeldedaten", "", false, true), //-> Rückmeldedaten (auch AS je nach Kenner)
        RESPONSE_DATA_AS(iPartsResponseDataId.TYPE, iPartsResponseDataId.DESCRIPTION, "!!Rückmeldedaten AS", "", true, true), //-> Rückmeldedaten (auch AS je nach Kenner)
        RESPONSE_SPIKE(iPartsResponseSpikeId.TYPE, iPartsResponseSpikeId.DESCRIPTION, "!!Rückmeldedaten - Vorläufer/Nachzügler", "", false, true), // -> Rückmeldedaten - Vorläufer/Nachzügler (auch AS je nach Kenner)
        RESPONSE_SPIKE_AS(iPartsResponseSpikeId.TYPE, iPartsResponseSpikeId.DESCRIPTION, "!!Rückmeldedaten - Vorläufer/Nachzügler AS", "", true, true), // -> Rückmeldedaten - Vorläufer/Nachzügler (auch AS je nach Kenner)

        ASSEMBLY(AssemblyId.TYPE, AssemblyId.DESCRIPTION, "!!Technischer Umfang", "", false, true), // eigene Benennung bei Daimler // -> Technischer Umfang
        MODULE_METADATA(iPartsModuleId.TYPE, iPartsModuleId.DESCRIPTION, "!!Technischer Umfang - Metadaten", "", false, true), // -> TU Metadaten
        MODULE_USAGE(iPartsModuleEinPASId.TYPE, iPartsModuleEinPASId.DESCRIPTION, "!!Technischer Umfang - Verwendung", "", false, true), // -> TU Verortung
        PIC_ORDER_MODULE(iPartsPicOrderModulesId.TYPE, iPartsPicOrderModulesId.DESCRIPTION, "!!Technischer Umfang - Bildauftrag zu Modul", "", false, true), // -> Technischer Umfang - Bildauftrag zu Modul
        PIC_ORDER(iPartsPicOrderId.TYPE, iPartsPicOrderId.DESCRIPTION, "!!Technischer Umfang - Bildauftrag", "", true, true), // -> Technischer Umfang - Bildauftrag
        DATA_IMAGE(DataImageId.TYPE, DataImageId.DESCRIPTION, "!!Technischer Umfang - Bildtafel", "", false, true), // -> Technischer Umfang - Bildtafel

        PART(PartId.TYPE, PartId.DESCRIPTION, "!!Teilestamm", "", false, true), // -> Teilestamm
        PART_FOOTNOTE(iPartsFootNoteMatRefId.TYPE, iPartsFootNoteMatRefId.DESCRIPTION, "!!Teilestamm - Fußnote", "", false, true), // Teilestamm - Fußnote (können aktuell nicht bearbeitet werden)

        KG_TU_AFTERSALES(iPartsDataKgTuAfterSalesId.TYPE, iPartsDataKgTuAfterSalesId.DESCRIPTION, "!!Technischer Umfang - Verortung", "", false, true),  // TU Verortung ?
        DIALOG_CONST_TU(iPartsDialogId.TYPE, iPartsDialogId.DESCRIPTION, "", "", false, true), // Änderung Const Stückliste (Doku-Relevant) ?
        DIALOG_CHANGES(iPartsDialogChangesId.TYPE, iPartsDialogChangesId.DESCRIPTION, "", "", false, true), // Änderungsdienst Importer ?
        PRODUCT(iPartsProductId.TYPE, iPartsProductId.DESCRIPTION, "", "", false, true), // Produktstammdaten (Eigenes Changeset)?

        MAT_ETKZ_APPROVED(PartId.TYPE + "ETKZ", "!!ETKZ-Änderung bestätigt", "!!ETKZ-Änderung bestätigt", "", false, true), // Künstlichen Typ für die ETKZ-Änderungen verwenden

        SA_TU(iPartsSAModulesId.TYPE, iPartsSAModulesId.DESCRIPTION, "!!Technischer Umfang - SA-TU", "", false, true), // SA Modul (nur imDEVELOPMENT angezeigt)
        PRODUCT_SAS(iPartsProductSAsId.TYPE, iPartsProductSAsId.DESCRIPTION, "!!Freie SA - Zuordnung", "", false, true),

        // ab hier die Elemente für Tech ChangeSet
        SERIES(iPartsSeriesId.TYPE, "!!Baureihe", "!!Baureihe", "", false, false),
        DICTIONARY(iPartsDictMetaId.TYPE, "!!Lexikon", "!!Lexikon", "", false, false),
        DICT_TEXT(iPartsDictLanguageMetaId.TYPE, "!!Lexikon Sprachen", "!!Lexikon Sprachen", "", false, false),
        LANGUAGE(TextEntryId.TYPE, "!!SprachenText", "!!Sprachen Text", "", false, false),
        PEM(iPartsPemId.TYPE, iPartsPemId.DESCRIPTION, "!!PEM", "", false, false),
        AS_MODEL(iPartsModelId.TYPE, "!!After-Sales Baumuster", "!!After-Sales Baumuster", "", false, false),
        NUTZDOKSAA(iPartsNutzDokSAAId.TYPE, "!!NutzDok-SAA", "!!NutzDok-SAA", "", false, false),
        NUTZDOKKEM(iPartsNutzDokKEMId.TYPE, "!!NutzDok-KEM", "!!NutzDok-KEM", "", false, false),
        WORKBASKET_EDS_KEM(iPartsKEMWorkBasketEDSId.TYPE, "!!EDS KEM Arbeitsvorrat", "!!EDS KEM Arbeitsvorrat", "", false, false),
        WORKBASKET_MBS_KEM(iPartsKEMWorkBasketMBSId.TYPE, "!!MBS KEM Arbeitsvorrat", "!!MBS KEM Arbeitsvorrat", "", false, false),
        WORKBASKET_SAA_STATES(iPartsWorkBasketSaaStatesId.TYPE, "!!SAA Arbeitsvorrat Status", "!!SAA Arbeitsvorrat Status", "", false, false),
        SAA_MODELS(iPartsSAAModelsId.TYPE, "!!SAA Baumuster", "!!SAA Baumuster", "", false, false),
        SA(iPartsSaId.TYPE, "!!SA Stammdaten", "!!SA Stammdaten", "", false, false),
        SAA(iPartsSaaId.TYPE, "!!SAA Stammdaten", "!!SAA Stammdaten", "", false, false),
        MODEL_AGGS(iPartsModelsAggsId.TYPE, "!!Baumuster - Aggregate", "!!Baumuster - Aggregate", "", false, false),
        PRODUCT_MODELS(iPartsProductModelsId.TYPE, "!!Baumuster zu Produkt", "", "", false, false),
        PRODUCT_FACTORY(iPartsProductFactoryId.TYPE, "!!Werk zu Produkt", "", "", false, false),
        PRODUCT_VARIANT(iPartsPSKProductVariantId.TYPE, "!!Variante zu Produkt", "", "", false, false),

        INTERNAL_TEXT_WB(iPartsWorkBasketInternalTextId.TYPE, "!!Arbeitsvorrat - Interner Text", "!!Arbeitsvorrat - Interner Text", "", false, false), // -> Teileposition - Interner Text
        FOLLOW_UP_DATE_WB(iPartsWorkBasketFollowUpDateId.TYPE, "!!Arbeitsvorrat - Nachfolgetermin", "!!Arbeitsvorrat - Nachfolgetermin", "", false, false); // -> Teileposition - Interner Text

        private String type;
        private String description;
        private String daimlerDescription;
        private String tableNamePrefixForDefs;
        private boolean isAS;
        private boolean isStandard;

        public static SHOW_TYPES getShowTypeByIdType(String idType) {
            for (SHOW_TYPES showType : SHOW_TYPES.values()) {
                if (showType.getType().equals(idType)) {
                    return showType;
                }
            }

            return UNKNOWN;
        }

        SHOW_TYPES(String type, String description, String daimlerDescription, String tableNamePrefixForDefs, boolean isAS, boolean isStandard) {
            this.type = type;
            this.description = description;
            this.daimlerDescription = daimlerDescription;
            this.tableNamePrefixForDefs = tableNamePrefixForDefs;
            this.isAS = isAS;
            this.isStandard = isStandard;
        }

        public String getType() {
            return type;
        }

        public String getTypeForInfoDefinitions() {
            if (tableNamePrefixForDefs.isEmpty()) {
                return getType();
            }
            return tableNamePrefixForDefs + type;
        }

        public String getDescription() {
            if (StrUtils.isValid(daimlerDescription)) {
                return daimlerDescription;
            }
            return description;
        }

        public String getIdDescription() {
            return description;
        }

        public String getDaimlerDescription() {
            return daimlerDescription;
        }

        public String getTableNamePrefixForDefs() {
            return tableNamePrefixForDefs;
        }

        public boolean isAS() {
            return isAS;
        }

        public boolean isStandard() {
            return isStandard;
        }
    }

    public static String getDisplayName(EtkProject project, EtkDatabaseTable table, String fieldName) {
        String changeContent = fieldName;
        if (table != null) {
            EtkDatabaseField field = table.getField(fieldName);
            if (field != null) {
                String fieldDescription = field.getDisplayName();
                EtkMultiSprache userDescription = field.getUserDescription();
                if ((userDescription != null) && !userDescription.isEmpty()) {
                    fieldDescription = userDescription.getTextByNearestLanguage(project.getDBLanguage(), project.getDataBaseFallbackLanguages());
                }
                changeContent = TranslationHandler.translate(fieldDescription);
            }
        }
        return changeContent;
    }

    public static String getTranslatedDescriptionFromObjectType(String dataObjectType) {
        SHOW_TYPES showType = SHOW_TYPES.getShowTypeByIdType(dataObjectType);
        if (showType != SHOW_TYPES.UNKNOWN) {
            return TranslationHandler.translate(showType.getDescription());
        } else {
            return dataObjectType;
        }
    }


    private EtkProject project;
    private AuthorOrderHistoryFormatter formatter;
    private Map<String, String> visualTypeMap;
    private Set<String> dontShowAttributeSet;
    private boolean isExtendedSet;

    public ChangeSetShowTypes(EtkProject project) {
        this.project = project;
        this.formatter = new AuthorOrderHistoryFormatter(project);
        this.visualTypeMap = new HashMap<>();
        this.dontShowAttributeSet = new HashSet<>();
        this.isExtendedSet = false;
        setValidTypes();

    }

    private void setValidTypes() {
        for (SHOW_TYPES showType : SHOW_TYPES.values()) {
            // DIALOG-Änderungen sollen nur im DEVELOPMENT-Modus angezeigt werden
            if ((showType != SHOW_TYPES.DIALOG_CHANGES) || Constants.DEVELOPMENT) {
                if (showType.isStandard()) {
                    visualTypeMap.put(showType.getType(), showType.getDescription());
                }
            }
        }
        for (String dontShowAttributeName : DONT_SHOW_ATTRIBUTE_NAMES) {
            dontShowAttributeSet.add(dontShowAttributeName);
        }
    }

    public void setExtendedValidTypes() {
        for (SHOW_TYPES showType : SHOW_TYPES.values()) {
            if (!showType.isStandard()) {
                visualTypeMap.put(showType.getType(), showType.getDescription());
            }
        }
        isExtendedSet = true;
    }

    private SHOW_TYPES getSpecialShowType(SerializedDBDataObject mergedObject) {
        String type = mergedObject.getType();
        if (type.equals(SHOW_TYPES.COLOR_TABLE_FACTORY_DATA.getType()) || type.equals(SHOW_TYPES.COLOR_TABLE_CONTENT_FACTORY_DATA.getType())) {
            String dataId = mergedObject.getPkValues()[4];
            if (dataId.equals(iPartsFactoryDataTypes.COLORTABLE_PART_AS.getDbValue())) {
                return SHOW_TYPES.COLOR_TABLE_FACTORY_DATA_AS;
            } else if (dataId.equals(iPartsFactoryDataTypes.COLORTABLE_PART.getDbValue())) {
                return SHOW_TYPES.COLOR_TABLE_FACTORY_DATA;
            } else if (dataId.equals(iPartsFactoryDataTypes.COLORTABLE_CONTENT.getDbValue())) {
                return SHOW_TYPES.COLOR_TABLE_CONTENT_FACTORY_DATA;
            } else if (dataId.equals(iPartsFactoryDataTypes.COLORTABLE_CONTENT_AS.getDbValue())) {
                return SHOW_TYPES.COLOR_TABLE_CONTENT_FACTORY_DATA_AS;
            }
        } else if (type.equals(SHOW_TYPES.FACTORY_DATA.getType())) {
            String dataId = mergedObject.getPkValues()[4];
            if (dataId.equals(iPartsFactoryDataTypes.FACTORY_DATA_CONSTRUCTION.getDbValue())) {
                return SHOW_TYPES.FACTORY_DATA;
            } else if (dataId.equals(iPartsFactoryDataTypes.FACTORY_DATA_AS.getDbValue())) {
                return SHOW_TYPES.FACTORY_DATA_AS;
            }
        } else if (type.equals(SHOW_TYPES.RESPONSE_DATA.getType())) {
            boolean isASData = SQLStringConvert.ppStringToBoolean(mergedObject.getPkValues()[7]);
            if (isASData) {
                return SHOW_TYPES.RESPONSE_DATA_AS;
            }
            return SHOW_TYPES.RESPONSE_DATA;

        } else if (type.equals(SHOW_TYPES.RESPONSE_SPIKE.getType())) {
            boolean isASData = SQLStringConvert.ppStringToBoolean(mergedObject.getPkValues()[8]);
            if (isASData) {
                return SHOW_TYPES.RESPONSE_SPIKE_AS;
            }
            return SHOW_TYPES.RESPONSE_SPIKE;
        } else if (type.equals(SHOW_TYPES.DIALOG_CHANGES.getType())) {
            String pkValue = mergedObject.getPkValues()[0];
            if (pkValue.equals(PartId.TYPE)) {
                return SHOW_TYPES.MAT_ETKZ_APPROVED;
            }
        } else if (isExtendedSet) {
            // Internal Text
            if (type.equals(SHOW_TYPES.INTERNAL_TEXT.getType())) {
                String dataType = mergedObject.getPkValues()[2];
                if (dataType.equals(SHOW_TYPES.INTERNAL_TEXT_WB.getType())) {
                    return SHOW_TYPES.INTERNAL_TEXT_WB;
                } else if (dataType.equals(SHOW_TYPES.FOLLOW_UP_DATE_WB.getType())) {
                    return SHOW_TYPES.FOLLOW_UP_DATE_WB;
                }
            }
        }
        return null;
    }

    public EtkProject getProject() {
        return project;
    }

    public AuthorOrderHistoryFormatter getFormatter() {
        return formatter;
    }

    public boolean filterSerializedObject(SerializedDBDataObject serializedObject) {
        if (serializedObject == null) {
            return false;
        }
        // DAIMLER-15725: alle REVERTED ausblenden
        if (serializedObject.getState() == SerializedDBDataObjectState.REVERTED) {
            return false;
        }
        if (isValidObjectType(serializedObject)) {
            return true;
        }
        return false;
    }

    public boolean filterAllowedAttributes(String attributName) {
        return !dontShowAttributeSet.contains(attributName);
    }

    public boolean isValidObjectType(SerializedDBDataObject mergedObject) {
        if (visualTypeMap.containsKey(mergedObject.getType())) {
            return true;
        }
        SHOW_TYPES specialShowType = getSpecialShowType(mergedObject);
        if ((specialShowType != null) && specialShowType.equals(SHOW_TYPES.MAT_ETKZ_APPROVED)) {
            return true;
        }
        return false;
    }

    public String getTitleBy(List<iPartsDataAuthorOrder> authorOrderList) {
        StringBuilder authorNames = new StringBuilder();
        int count = 0;
        if ((authorOrderList != null) && !authorOrderList.isEmpty()) {
            for (iPartsDataAuthorOrder authorOrder : authorOrderList) {
                if (!authorOrder.getAuthorOrderName().trim().isEmpty()) {
                    if (count == 0) {
                        authorNames.append(authorOrder.getAuthorOrderName());
                    } else {
                        authorNames.append(", ");
                        authorNames.append(authorOrder.getAuthorOrderName());
                    }
                    count++;
                }
            }
        } else {
            authorNames.append("???");
        }
        String msg = "!!Autoren-Auftrag: %1";
        if (count > 1) {
            msg = "!!Autoren-Aufträge: %1";
        }
        return TranslationHandler.translate(msg, StrUtils.makeAbbreviation(authorNames.toString(), EditSelectSAABKForm.MAX_LENGTH_MODELS_FOR_TITLE));
    }

    public String getTitleBy(EtkDataAssembly assembly) {
        String idName = TranslationHandler.translate("!!TU");
        String idString = "???";
        if (assembly != null) {
//            idName = getTranslatedDescriptionFromObjectType(assembly.getAsId().getType());
            idString = assembly.getAsId().getKVari();
        }
        return TranslationHandler.translate("!!Änderungshistorie für %1 \"%2\"", idName, idString);
    }

    public String getTitleBy(EtkDataPartListEntry partListEntry) {
        String idString = TranslationHandler.translate(ChangeSetShowTypes.SHOW_TYPES.PART_LIST_ENTRY.getDaimlerDescription());
        String idName = "???";

        if (partListEntry != null) {
            idName = getTranslatedDescriptionFromObjectType(partListEntry.getAsId().getType());
            idString = getFormatter().formatModuleAndLfdNo(partListEntry.getAsId());
        }
        return TranslationHandler.translate("!!Änderungshistorie für %1 \"%2\"", idName, idString);
    }

    public String getTitleBy(String techChangeSetGUID) {
        if (StrUtils.isValid(techChangeSetGUID)) {
            iPartsDataChangeSet dataChangeSet = new iPartsDataChangeSet(getProject(), new iPartsChangeSetId(techChangeSetGUID));
            if (dataChangeSet.existsInDB()) {
                if (dataChangeSet.getSource() != null) {
                    return TranslationHandler.translate("!!Technisches Änderungsset \"%1\"",
                                                        getFormatter().getVisTechChangeSetName(dataChangeSet.getSource()));
                } else {
                    return TranslationHandler.translate("!!Technisches Änderungsset \"%1\"", dataChangeSet.getFieldValue(FIELD_DCS_SOURCE));
                }
            }
        }
        return "!!Technisches Änderungsset";
    }

    public String getTitleByTechnicalChangeSets(List<iPartsChangeSetId> techChangeSetGUIDList) {
        if (techChangeSetGUIDList != null) {
            if (techChangeSetGUIDList.size() == 1) {
                return getTitleBy(techChangeSetGUIDList.get(0).getGUID());
            }
            return TranslationHandler.translate("!!%1 Technische Änderungssets", String.valueOf(techChangeSetGUIDList.size()));
        }
        return "!!Technisches Änderungsset";
    }

    public String getTitleForSM(EtkDataAssembly assembly) {
        String idName = TranslationHandler.translate("!!Submodul");
        String idString = "???";
        if (assembly != null) {
            idString = assembly.getHeading1(-1, null);
        }
        return TranslationHandler.translate("!!Änderungshistorie für %1 \"%2\"", idName, idString);
    }

    public String getTitleForSMPartListEntry(EtkProject project, EtkDataAssembly assembly, EtkDataPartListEntry selectedPartListEntry) {
        String defaultText = getTitleForSM(assembly);
        return TranslationHandler.translate("!!Änderungshistorie für %1", iPartsPlugin.getModifyRelatedInfoDisplayText(project, selectedPartListEntry, defaultText));
    }

    public String getObjectTypeDescription(SerializedDBDataObject mergedObject) {
        SHOW_TYPES specialShowType = getSpecialShowType(mergedObject);
        if (specialShowType == null) {
            return visualTypeMap.get(mergedObject.getType());
        } else {
            return specialShowType.getDescription();
        }
    }

    public iPartsChangeSetInfoDefinitions.ChangeSetObjectIdInfoDefinitions getInfoDefinitions(SerializedDBDataObject serializedDBDataObject) {
        iPartsChangeSetInfoDefinitions.ChangeSetObjectIdInfoDefinitions infoDefinitions;
        ChangeSetShowTypes.SHOW_TYPES showType = getShowType(serializedDBDataObject);
        if (showType.isAS()) {
            infoDefinitions = iPartsChangeSetInfoDefinitions.getInstance(getProject()).getChangeSetDefinitionsForAS(showType.getTypeForInfoDefinitions());
        } else {
            infoDefinitions = iPartsChangeSetInfoDefinitions.getInstance(getProject()).getChangeSetDefinitions(showType.getTypeForInfoDefinitions());
        }
        return infoDefinitions;
    }

    public String getTechChangeSetName(iPartsChangeSetSource source, int index) {
        return getFormatter().formatTechChangeSetName(source, index);
    }

    public String getObjectTypeDescriptionTranslated(SerializedDBDataObject mergedObject) {
        return TranslationHandler.translate(getObjectTypeDescription(mergedObject));
    }

    public String getStateDescription(SerializedDBDataObject mergedObject) {
        SHOW_TYPES showType = getShowType(mergedObject);
        String result = mergedObject.getState().getDescription();
        if (showType != null) {
            switch (showType) {
                case MAT_ETKZ_APPROVED:
                    result = SerializedDBDataObjectState.COMMITTED.getDescription();
                    break;
            }
        }
        return result;
    }

    public String getStateDescriptionTranslated(SerializedDBDataObject mergedObject) {
        return TranslationHandler.translate(getStateDescription(mergedObject));
    }

    public SHOW_TYPES getShowType(SerializedDBDataObject mergedObject) {
        SHOW_TYPES specialShowType = getSpecialShowType(mergedObject);
        if (specialShowType != null) {
            return specialShowType;
        }
        String type = mergedObject.getType();
        return SHOW_TYPES.getShowTypeByIdType(type);
    }

    public String calculateObjectId(SerializedDBDataObject mergedObject) {
        return calculateObjectId(getShowType(mergedObject), mergedObject.getPkValues(), mergedObject.getAttributes());
    }

    public String calculateObjectId(SHOW_TYPES showType, String[] pkValues, Collection<SerializedDBDataObjectAttribute> attributes) {
        String result = "";
        if (showType != null) {
            switch (showType) {
                case UNKNOWN:
                    result = pkValues[0];
                    break;
                case PART_LIST_ENTRY:  // <TU-Nummer>, <laufende Nummer>
                    result = formatter.formatModuleAndLfdNo(new PartListEntryId(pkValues));
                    break;
                case FACTORY_DATA:  // <DIALOG-Schlüssel>, Werk: <WERK>, ADAT: <ADAT>
                    result = formatter.formatFactoryData(new iPartsFactoryDataId(pkValues));
                    break;
                case FACTORY_DATA_AS:  // <DIALOG-Schlüssel>, Werk: <WERK>, ADAT: <ADAT>, Aftersales
                    result = formatter.formatFactoryDataAS(new iPartsFactoryDataId(pkValues));
                    break;
                case REPLACE_PART:  // <TU-Nummer>, <laufende Nummer>
                    iPartsReplacePartId replacePartId = new iPartsReplacePartId(pkValues);
                    result = formatter.formatModuleAndLfdNo(replacePartId.getPredecessorPartListEntryId());
                    break;
                case INCLUDE_PART:  // <TU-Nummer>, <laufende Nummer>, <MitlieferTeileNummer>
                    iPartsIncludePartId includePartId = new iPartsIncludePartId(pkValues);
                    result = formatter.formatIncludePart(includePartId);
                    break;
                case INTERNAL_TEXT:  // <TU-Nummer>, <laufende Nummer>
                    result = getInternalTextValue(new iPartsDataInternalTextId(pkValues));
                    break;
                case FOOTNOTES:  // <TU-Nummer>, <laufende Nummer>
                    result = formatter.formatFootNote(new iPartsFootNoteCatalogueRefId(pkValues));
                    break;
                case COMBTEXT:  // <TU-Nummer>, <laufende Nummer>
                    result = formatter.formatCombinedTexte(new iPartsCombTextId(pkValues));
                    break;
                case VARIANT_TABLE_TO_PART:  // <Variantentabelle>, <Teilenummer>, KEM Datum ab: <SDATA>
                    result = formatter.formatVariantTable(new iPartsColorTableToPartId(pkValues), attributes);
                    break;
                case COLOR_TABLE_FACTORY_DATA:  // <Variantentabelle>, <Teilenummer>, KEM Datum ab: <SDATA>, Werk: <WERK>, ADAT: <ADAT>
                    result = formatter.formatColorFactoryTable(new iPartsColorTableFactoryId(pkValues), attributes);
                    break;
                case COLOR_TABLE_FACTORY_DATA_AS:  // <Variantentabelle>, <Teilenummer>, KEM Datum ab: <SDATA>, Werk: <WERK>, ADAT: <ADAT>, Aftersales
                    result = formatter.formatColorFactoryTableAS(new iPartsColorTableFactoryId(pkValues), attributes);
                    break;
                case VARIANT_DATA:  // <Variantentabelle>, <Position>, KEM Datum ab: <SDATA>
                    result = formatter.formatVariantData(new iPartsColorTableContentId(pkValues));
                    break;
                case COLOR_TABLE_CONTENT_FACTORY_DATA:  // <Variantentabelle>, <Position>, KEM Datum ab: <SDATA>, Werk: <WERK>, <ADAT>
                    result = formatter.formatColorContentFactoryTable(new iPartsColorTableFactoryId(pkValues));
                    break;
                case COLOR_TABLE_CONTENT_FACTORY_DATA_AS:  // <Variantentabelle>, <Position>, KEM Datum ab: <SDATA>, Werk: <WERK>, <ADAT>, Aftersales
                    result = formatter.formatColorContentFactoryTableAS(new iPartsColorTableFactoryId(pkValues));
                    break;
                case RESPONSE_DATA:  // <PEM>, Werk: <WERK>, Baureihe: <Baureihe>, Ausführungsart: <AA>,  Baumuster: <Baumuster>, ADAT: <ADAT>
                    result = formatter.formatResponseData(new iPartsResponseDataId(pkValues));
                    break;
                case RESPONSE_DATA_AS:  // <PEM>, Werk: <WERK>, Baureihe: <Baureihe>, Ausführungsart: <AA>,  Baumuster: <Baumuster>, ADAT: <ADAT>, Aftersales
                    result = formatter.formatResponseDataAS(new iPartsResponseDataId(pkValues));
                    break;
                case RESPONSE_SPIKE:  // <PEM>, Werk: <WERK>, Baureihe: <Baureihe>, Ausführungsart: <AA>,  Baumuster: <Baumuster>, ADAT: <ADAT>, Ident: <Ident>
                    result = formatter.formatResponseSpike(new iPartsResponseSpikeId(pkValues));
                    break;
                case RESPONSE_SPIKE_AS:  // <PEM>, Werk: <WERK>, Baureihe: <Baureihe>, Ausführungsart: <AA>,  Baumuster: <Baumuster>, ADAT: <ADAT>, Ident: <Ident>, Aftersales
                    result = formatter.formatResponseSpikeAS(new iPartsResponseSpikeId(pkValues));
                    break;
                case ASSEMBLY:  // <TU-Nummer>
                    result = pkValues[0];
                    break;
                case MODULE_METADATA:  // <TU-Nummer>
                    result = pkValues[0];
                    break;
                case MODULE_USAGE:  // <Produkt-Nummer, TU-Nummer, laufende Nummer>
                    result = formatter.formatModuleUsage(new iPartsModuleEinPASId(pkValues));
                    break;
                case PIC_ORDER_MODULE:  // <TU-Nummer>, Bildauftrag: <Mediencontainer>
                    result = formatter.formatPicOrderModule(new iPartsPicOrderModulesId(pkValues));
                    break;
                case PIC_ORDER:  // <TU-Nummer>, Bildauftrag: <Mediencontainer>
                    result = formatter.formatPicOrder(new iPartsPicOrderId(pkValues));
                    break;
                case DATA_IMAGE:  // <TU-Nummer>, Bildtafel: <Bildtafelnummer>
                    result = formatter.formatPicDataImage(new DataImageId(pkValues), attributes);
                    break;
                case PART:  // <Teilenummer>
                    result = formatter.formatMatNo(pkValues[0]);
                    break;
                case PART_FOOTNOTE:  // <Teilenummer>
                    result = formatter.formatPartFootnote(new iPartsFootNoteMatRefId(pkValues));
                    break;


                case KG_TU_AFTERSALES:
                    result = formatter.formatKGTU(new iPartsDataKgTuAfterSalesId(pkValues));
                    break;
                case DIALOG_CONST_TU:
                    result = formatter.formatDIALOGKey(pkValues[0]);
                    break;
                case DIALOG_CHANGES:
                    String dialogChangesType = pkValues[0];
                    result = formatter.formatDialogChange(pkValues, SHOW_TYPES.getShowTypeByIdType(dialogChangesType).getDescription());
//                    result = TranslationHandler.translate("!!%1 %2", TranslationHandler.translate(SHOW_TYPES.getShowTypeByIdType(dialogChangesType).getDescription()),
//                                                          IdWithType.fromDBString(dialogChangesType, pkValues[1]).toStringForLogMessages());
                    break;
                case PRODUCT:
                    result = formatter.formatProduct(new iPartsProductId(pkValues));
                    break;
                case SA_TU:
                    result = formatter.formatMatNo(pkValues[0]);
                    break;
                case PRODUCT_SAS:
                    result = formatter.formatProductKgSa(new iPartsProductSAsId(pkValues));
                    break;
                case MAT_ETKZ_APPROVED:
                    dialogChangesType = pkValues[0];
                    result = formatter.formatMatETKZapproved(new iPartsDialogChangesId(pkValues), SHOW_TYPES.getShowTypeByIdType(dialogChangesType).getDescription());
                    break;
                // ab hier DataTypes dür Tech ChangeSet
                case SERIES:
                    result = formatter.formatSeries(new iPartsSeriesId(pkValues));
                    break;
                case DICTIONARY:
                    result = formatter.formatDictMeta(new iPartsDictMetaId(pkValues));
                    break;
                case DICT_TEXT:
                    result = formatter.formatDictLangMeta(new iPartsDictLanguageMetaId(pkValues));
                    break;
                case LANGUAGE:
                    result = formatter.formatLanguage(new TextEntryId(pkValues));
                    break;
                case PEM:
                    result = formatter.formatPEM(new iPartsPemId(pkValues));
                    break;
                case AS_MODEL:
                    result = pkValues[0];  //formatter.formatPEM(new iPartsPemId(pkValues));
                    break;
                case NUTZDOKSAA:
                    result = formatter.formatSAA(new iPartsNutzDokSAAId(pkValues));
                    break;
                case NUTZDOKKEM:
                    result = formatter.formatKEM(new iPartsNutzDokKEMId(pkValues));
                    break;
                case WORKBASKET_EDS_KEM:
                    result = formatter.formatKEMWorkBasketEDS(new iPartsKEMWorkBasketEDSId(pkValues));
                    break;
                //        WORKBASKET_MBS_KEM(iPartsKEMWorkBasketMBSId.TYPE, "!!MBS KEM Arbeitsvorrat", "!!MBS KEM Arbeitsvorrat", "", false, false),
                case WORKBASKET_MBS_KEM:
                    result = formatter.formatKEMWorkBasketMBS(new iPartsKEMWorkBasketMBSId(pkValues));
                    break;
                case WORKBASKET_SAA_STATES:
                    result = formatter.formatWorkBasketSaaStates(new iPartsWorkBasketSaaStatesId(pkValues));
                    break;
                case FOLLOW_UP_DATE_WB:
                    result = getInternalTextValue(new iPartsDataInternalTextId(pkValues));
                    break;
                case SAA_MODELS:
                    result = formatter.formatSaaModels(new iPartsSAAModelsId(pkValues));
                    break;
                case SA:
                    result = formatter.formatSA(pkValues[0]);
                    break;
                case SAA:
                    result = formatter.formatSAA(pkValues[0]);
                    break;
                //        MODEL_AGGS(iPartsModelsAggsId.TYPE, "!!Baumuster - Aggregate", "!!Baumuster - Aggregate", "", false, false),
                case MODEL_AGGS:
                    result = formatter.formatModelAggs(new iPartsModelsAggsId(pkValues));
                    break;
            }
        }
        return result;
    }

    public AssemblyId calculateAssemblyIdFromObjectId(SerializedDBDataObject mergedObject) {
        return calculateAssemblyIdFromObjectId(getShowType(mergedObject), mergedObject.getPkValues(), mergedObject.getAttributes());
    }

    public AssemblyId calculateAssemblyIdFromObjectId(SHOW_TYPES showType, String[] pkValues, Collection<SerializedDBDataObjectAttribute> attributes) {
        AssemblyId result = null;
        if (showType != null) {
            switch (showType) {
                case UNKNOWN:
                    break;
                case PART_LIST_ENTRY:  // <TU-Nummer>, <laufende Nummer>
                    PartListEntryId id = new PartListEntryId(pkValues);
                    result = id.getOwnerAssemblyId();
                    break;
                case FACTORY_DATA:  // <DIALOG-Schlüssel>, Werk: <WERK>, ADAT: <ADAT>
                    break;
                case FACTORY_DATA_AS:  // <DIALOG-Schlüssel>, Werk: <WERK>, ADAT: <ADAT>, Aftersales
                    break;
                case REPLACE_PART:  // <TU-Nummer>, <laufende Nummer>
                    iPartsReplacePartId replacePartId = new iPartsReplacePartId(pkValues);
                    result = new AssemblyId(replacePartId.getReplaceVari(), replacePartId.getReplaceVer());
                    break;
                case INCLUDE_PART:  // <TU-Nummer>, <laufende Nummer>, <MitlieferTeileNummer>
                    iPartsIncludePartId includePartId = new iPartsIncludePartId(pkValues);
                    result = new AssemblyId(includePartId.getIncludeVari(), includePartId.getIncludeVer());
                    break;
                case INTERNAL_TEXT:  // <TU-Nummer>, <laufende Nummer>
                    iPartsDataInternalTextId internalTextId = new iPartsDataInternalTextId(pkValues);
                    if (internalTextId.getDataObjectType().equals(PartListEntryId.TYPE)) {
                        IdWithType pleId = IdWithType.fromDBString(PartListEntryId.TYPE, internalTextId.getDataObjectId());
                        result = new PartListEntryId(pleId.toStringArrayWithoutType()).getOwnerAssemblyId();
                    }
                    break;
                case FOOTNOTES:  // <TU-Nummer>, <laufende Nummer>
                    iPartsFootNoteCatalogueRefId footNoteCatalogueRefId = new iPartsFootNoteCatalogueRefId(pkValues);
                    result = footNoteCatalogueRefId.getPartListEntryId().getOwnerAssemblyId();
                    break;
                case COMBTEXT:  // <TU-Nummer>, <laufende Nummer>
                    iPartsCombTextId combTextId = new iPartsCombTextId(pkValues);
                    result = new AssemblyId(combTextId.getModuleId(), combTextId.getModuleVer());
                    break;
                case VARIANT_TABLE_TO_PART:  // <Variantentabelle>, <Teilenummer>, KEM Datum ab: <SDATA>
                    break;
                case COLOR_TABLE_FACTORY_DATA:  // <Variantentabelle>, <Teilenummer>, KEM Datum ab: <SDATA>, Werk: <WERK>, ADAT: <ADAT>
                    break;
                case COLOR_TABLE_FACTORY_DATA_AS:  // <Variantentabelle>, <Teilenummer>, KEM Datum ab: <SDATA>, Werk: <WERK>, ADAT: <ADAT>, Aftersales
                    break;
                case VARIANT_DATA:  // <Variantentabelle>, <Position>, KEM Datum ab: <SDATA>
                    break;
                case COLOR_TABLE_CONTENT_FACTORY_DATA:  // <Variantentabelle>, <Position>, KEM Datum ab: <SDATA>, Werk: <WERK>, <ADAT>
                    break;
                case COLOR_TABLE_CONTENT_FACTORY_DATA_AS:  // <Variantentabelle>, <Position>, KEM Datum ab: <SDATA>, Werk: <WERK>, <ADAT>, Aftersales
                    break;
                case RESPONSE_DATA:  // <PEM>, Werk: <WERK>, Baureihe: <Baureihe>, Ausführungsart: <AA>,  Baumuster: <Baumuster>, ADAT: <ADAT>
                    break;
                case RESPONSE_DATA_AS:  // <PEM>, Werk: <WERK>, Baureihe: <Baureihe>, Ausführungsart: <AA>,  Baumuster: <Baumuster>, ADAT: <ADAT>, Aftersales
                    break;
                case RESPONSE_SPIKE:  // <PEM>, Werk: <WERK>, Baureihe: <Baureihe>, Ausführungsart: <AA>,  Baumuster: <Baumuster>, ADAT: <ADAT>, Ident: <Ident>
                    break;
                case RESPONSE_SPIKE_AS:  // <PEM>, Werk: <WERK>, Baureihe: <Baureihe>, Ausführungsart: <AA>,  Baumuster: <Baumuster>, ADAT: <ADAT>, Ident: <Ident>, Aftersales
                    break;
                case ASSEMBLY:  // <TU-Nummer>
                    result = new AssemblyId(pkValues);
                    break;
                case MODULE_METADATA:  // <TU-Nummer>
                    iPartsModuleId moduleId = new iPartsModuleId(pkValues);
                    result = new AssemblyId(moduleId.getModuleNumber(), "");
                    break;
                case MODULE_USAGE:  // <Produkt-Nummer, TU-Nummer, laufende Nummer>
                    iPartsModuleEinPASId moduleEinPASId = new iPartsModuleEinPASId(pkValues);
                    result = new AssemblyId(moduleEinPASId.getModuleNumber(), "");
                    break;
                case PIC_ORDER_MODULE:  // <TU-Nummer>, Bildauftrag: <Mediencontainer>
                    iPartsPicOrderModulesId picOrderModulesId = new iPartsPicOrderModulesId(pkValues);
                    result = new AssemblyId(picOrderModulesId.getModuleNo(), "");
                    break;
                case PIC_ORDER:  // <TU-Nummer>, Bildauftrag: <Mediencontainer>
                    break;
                case DATA_IMAGE:  // <TU-Nummer>, Bildtafel: <Bildtafelnummer>
                    break;
                case PART:  // <Teilenummer>
                    break;
                case PART_FOOTNOTE:  // <Teilenummer>
                    break;
                case KG_TU_AFTERSALES:
                    break;
                case DIALOG_CONST_TU:
                    break;
                case DIALOG_CHANGES:
                    break;
                case PRODUCT:
                    break;
                case SA_TU:
                    result = new AssemblyId(pkValues[0], "");
                    break;
                case PRODUCT_SAS:
                    break;
                case MAT_ETKZ_APPROVED:
                    break;
                // ab hier DataTypes dür Tech ChangeSet
                case SERIES:
                    break;
                case DICTIONARY:
                    break;
                case DICT_TEXT:
                    break;
                case LANGUAGE:
                    break;
                case PEM:
                    break;
                case AS_MODEL:
                    break;
                case NUTZDOKSAA:
                    break;
                case NUTZDOKKEM:
                    break;
                case WORKBASKET_EDS_KEM:
                    break;
                case WORKBASKET_MBS_KEM:
                    break;
                case WORKBASKET_SAA_STATES:
                    break;
                case FOLLOW_UP_DATE_WB:
                    break;
                case SAA_MODELS:
                    break;
                case SA:
                    break;
                case SAA:
                    break;
                case MODEL_AGGS:
                    break;
            }
        }
        return result;
    }

    private String getInternalTextValue(iPartsDataInternalTextId internalTextId) {
        String result = "";
        String objectType = internalTextId.getDataObjectType();
        if (objectType.equals(PartListEntryId.TYPE)) {
            IdWithType id = IdWithType.fromDBString(PartListEntryId.TYPE, internalTextId.getDataObjectId());
            result = formatter.formatModuleAndLfdNo(new PartListEntryId(id.toStringArrayWithoutType()));
        } else if (objectType.equals(iPartsWorkBasketInternalTextId.TYPE)) {
            IdWithType id = IdWithType.fromDBString(iPartsWorkBasketInternalTextId.TYPE, internalTextId.getDataObjectId());
            iPartsWorkBasketInternalTextId wbInternalTextId = new iPartsWorkBasketInternalTextId(id.toStringArrayWithoutType());
            String value;
            if (wbInternalTextId.isKEM()) {
                value = wbInternalTextId.getKEM();
            } else {
                value = formatter.formatSAA(wbInternalTextId.getSaaBk());
            }
            result = TranslationHandler.translate(wbInternalTextId.getWbType().getTitle()) + ": " + value;
        } else if (objectType.equals(iPartsWorkBasketFollowUpDateId.TYPE)) {
            IdWithType id = IdWithType.fromDBString(iPartsWorkBasketFollowUpDateId.TYPE, internalTextId.getDataObjectId());
            result = formatter.formatWorkBasketFollowUpDate(new iPartsWorkBasketFollowUpDateId(id.toStringArrayWithoutType()));
        }
        return result;
    }

    public String modifyValue(String tableName, String fieldName, String value, String dbLanguage) {
        if (tableName.equals(TABLE_DA_INTERNAL_TEXT)) {
            if (fieldName.equals(FIELD_DIT_DO_ID)) {
                IdWithType id = IdWithType.fromDBString(PartListEntryId.TYPE, value);
                value = formatter.formatModuleAndLfdNo(new PartListEntryId(id.toStringArrayWithoutType()));
            }
        } else if (tableName.equals(TABLE_DA_FACTORY_DATA)) {
            if (fieldName.equals(FIELD_DFD_GUID)) {
                value = formatter.formatDIALOGKey(value);
            }
        } else if (tableName.equals(TABLE_KATALOG)) {
            if (fieldName.equals(FIELD_K_SOURCE_CONTEXT)) {
                value = formatter.formatHmMSm(value);
            } else if (fieldName.equals(FIELD_K_SOURCE_GUID)) {
                value = formatter.formatConstructionPrimaryKey(value);
            }
        } else if (tableName.equals(TABLE_DA_DIALOG_CHANGES)) {
            if (fieldName.equals(FIELD_DDC_DO_ID)) {
                // multiple ID-Use => allgemeine Routine nötig
//                value = IdWithType.fromDBString(iPartsReplaceConstId.TYPE, value).toStringForLogMessages();
//                value = IdWithType.fromStringArrayWithTypeFromClass(iPartsReplaceConstId.class, pkValues).toStringForLogMessages();
            } else if (fieldName.equals(FIELD_DDC_BCTE)) {
                value = formatter.formatDIALOGKey(value);
            }
        }
        return value;
    }

    public void setAttributeValueExtended(SerializedDBDataObjectAttribute serializedDBDataObjectAttribute, String tableName, String fieldName,
                                          String dbLanguage, DBDataObjectAttribute attrib, boolean isOld, SerializedDBDataObject serializedObject) {
        boolean isSet = false;
        if (serializedObject.getType().equals(ChangeSetShowTypes.SHOW_TYPES.INTERNAL_TEXT.getType())) {
            String value = "";
            if (fieldName.equals(FIELD_DIT_DO_TYPE) || fieldName.equals(FIELD_DIT_DO_ID)) {
                if (fieldName.equals(FIELD_DIT_DO_TYPE)) {
                    value = TranslationHandler.translate(getObjectTypeDescription(serializedObject));
                } else {
                    value = getInternalTextValue(new iPartsDataInternalTextId(serializedObject.getPkValues()));
                }
                setAttributeValueAsString(value, tableName, fieldName, dbLanguage, attrib);
                isSet = true;
            }
        }
        if (!isSet) {
            setAttributeValue(serializedDBDataObjectAttribute, tableName, fieldName, dbLanguage, attrib, isOld);
        }
    }

    public void setAttributeValue(SerializedDBDataObjectAttribute serializedDBDataObjectAttribute, String tableName, String fieldName,
                                  String dbLanguage, DBDataObjectAttribute attrib, boolean isOld) {
        switch (serializedDBDataObjectAttribute.calculateType()) {
            case STRING:
                String value = isOld ? serializedDBDataObjectAttribute.getOldValue() : serializedDBDataObjectAttribute.getValue();
                if (value != null) {
                    value = modifyValue(tableName, fieldName, value, dbLanguage);
                    setAttributeValueAsString(value, tableName, fieldName, dbLanguage, attrib);
                }
                break;
            case ARRAY:
                SerializedEtkDataArray dataArray = isOld ? serializedDBDataObjectAttribute.getOldArray() : serializedDBDataObjectAttribute.getArray();
                setAttributeValueAsArray(dataArray, tableName, fieldName, dbLanguage, attrib);
                break;
            case MULTI_LANGUAGE:
                SerializedEtkMultiSprache serializedMultiSprache = isOld ? serializedDBDataObjectAttribute.getOldMultiLanguage() : serializedDBDataObjectAttribute.getMultiLanguage();
                setAttributeValueAsMultiSprache(serializedMultiSprache, attrib);
                break;
            case BLOB:
                attrib.setValueAsString("", DBActionOrigin.FROM_DB);
                break;
        }
    }

    public void setAttributeValueAsArray(SerializedEtkDataArray dataArray, String tableName, String fieldName,
                                         String dbLanguage, DBDataObjectAttribute attrib) {
        if (dataArray != null) {
            String fieldValue = getProject().getVisObject().getArrayAsFormattedString(dataArray.createDataArray(""), "", dbLanguage,
                                                                                      tableName, fieldName, false);

            attrib.setValueAsString(StrUtils.makeAbbreviation(fieldValue, MAX_LENGTH_FOR_ARRAYS), DBActionOrigin.FROM_DB);
        } else {
            attrib.setValueAsString("", DBActionOrigin.FROM_DB);
        }
    }

    public void setAttributeValueAsMultiSprache(SerializedEtkMultiSprache serializedMultiSprache, DBDataObjectAttribute attrib) {
        if (serializedMultiSprache != null) {
            EtkMultiSprache multi = convertToMultiSprache(serializedMultiSprache);
            attrib.setValueAsMultiLanguage(multi, DBActionOrigin.FROM_DB);
        } else {
            attrib.setValueAsString("", DBActionOrigin.FROM_DB);
        }
    }

    public void setAttributeValueAsString(String value, String tableName, String fieldName, String dbLanguage,
                                          DBDataObjectAttribute attrib) {
        if (value == null) {
            value = "";
        }
        String fieldValue = getProject().getVisObject().asText(tableName, fieldName, value, dbLanguage);
        attrib.setValueAsString(fieldValue, DBActionOrigin.FROM_DB);
    }

    public EtkMultiSprache convertToMultiSprache(SerializedEtkMultiSprache serializedMultiSprache) {
        EtkMultiSprache multi = new EtkMultiSprache();
        if (serializedMultiSprache != null) {
            LinkedHashMap<String, String> langsAndTexts = serializedMultiSprache.loadAndGetLanguagesAndTexts(EtkDataObject.getExtendedDataTypeProviderForTextIds(getProject()));
            if (langsAndTexts != null) {
                for (Map.Entry<String, String> entry : langsAndTexts.entrySet()) {
                    multi.setText(entry.getKey(), entry.getValue());
                }
            }
        }
        return multi;
    }
}
