/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.mechanic.treeview.forms.AssemblyTreeFormIConnector;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.relatedinfo.main.connectors.RelatedInfoFormConnector;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSAAModelsList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.AbstractSimpleDataObjectGridForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.Utils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Formular für die SAA/BK-Gültigkeiten zu einem EDS-Baumuster innerhalb der RelatedInfo.
 */
public class iPartsRelatedInfoSAAsModelsDataForm extends AbstractSimpleDataObjectGridForm implements iPartsConst {

    public static final String IPARTS_MENU_ITEM_SHOW_SAAS_MODELS_DATA = "iPartsMenuItemShowSAAsModelsData";
    public static final String CONFIG_KEY_SAAS_MODELS_DATA = "Plugin/iPartsEdit/SAAsModelsData";

    private static EnumSet<iPartsModuleTypes> VALID_MODULE_TYPES = EnumSet.of(iPartsModuleTypes.CONSTRUCTION_MODEL, iPartsModuleTypes.CONSTRUCTION_MODEL_CTT);

    private iPartsModelId modelId;

    public static GuiMenuItem createMenuItem(final SAAsModelsDataCallback callback, final AbstractJavaViewerForm parentForm) {
        GuiMenuItem menuItem = new GuiMenuItem();
        menuItem.setUserObject(IPARTS_MENU_ITEM_SHOW_SAAS_MODELS_DATA);
        menuItem.setName(IPARTS_MENU_ITEM_SHOW_SAAS_MODELS_DATA);
        menuItem.setText(RELATED_INFO_SAAS_MODELS_DATA_TEXT);
        menuItem.setIcon(DefaultImages.module.getImage());
        menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                SAAsModelsDataQuery saasModelsDataQuery = callback.getSAAsModelsDataQuery();
                if (saasModelsDataQuery == null) {
                    saasModelsDataQuery = new SAAsModelsDataQuery();
                }
                RelatedInfoFormConnector relatedInfoFormConnector = new RelatedInfoFormConnector(parentForm.getConnector());
                iPartsRelatedInfoSAAsModelsDataForm saasModelsDataForm = new iPartsRelatedInfoSAAsModelsDataForm(saasModelsDataQuery,
                                                                                                                 relatedInfoFormConnector,
                                                                                                                 parentForm);
                saasModelsDataForm.addOwnConnector(relatedInfoFormConnector);
                saasModelsDataForm.showModal();
            }
        });

        return menuItem;
    }

    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_SAAS_MODELS_DATA, RELATED_INFO_SAAS_MODELS_DATA_TEXT,
                                DefaultImages.module.getImage(),
                                CONFIG_KEY_RELATED_INFO_SAAS_MODELS_DATA);
    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        updatePartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_SAAS_MODELS_DATA, VALID_MODULE_TYPES);
    }

    public static void modifyTreePopupMenu(GuiContextMenu menu, final AbstractJavaViewerForm formWithTree) {
        modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_SHOW_SAAS_MODELS_DATA, RELATED_INFO_SAAS_MODELS_DATA_TEXT,
                            CONFIG_KEY_RELATED_INFO_SAAS_MODELS_DATA);
    }

    public static void updateTreePopupMenu(GuiContextMenu popupMenu, AssemblyTreeFormIConnector connector) {
        updateTreePopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_SAAS_MODELS_DATA, VALID_MODULE_TYPES);
    }

    public static boolean relatedInfoIsVisible(EtkDataAssembly assembly) {
        return relatedInfoIsVisible(assembly, VALID_MODULE_TYPES);
    }

    /**
     * Erzeugt ein neues RelatedInfoForm für die Anzeige der SAA/BK-Gültigkeiten zu einem EDS-Baumuster.
     *
     * @param dataConnector
     * @param parentForm
     * @param relatedInfo
     */
    public iPartsRelatedInfoSAAsModelsDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                               IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo, CONFIG_KEY_SAAS_MODELS_DATA, null);
    }

    /**
     * Erzeugt einen neuen Dialog für die Anzeige der SAA/BK-Gültigkeiten zu einem EDS-Baumuster basierend auf dem Baumuster
     * aus der übergebenen {@link SAAsModelsDataQuery}.
     *
     * @param saasModelsDataQuery
     * @param dataConnector
     * @param parentForm
     */
    public iPartsRelatedInfoSAAsModelsDataForm(SAAsModelsDataQuery saasModelsDataQuery, RelatedInfoBaseFormIConnector dataConnector,
                                               AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm, CONFIG_KEY_SAAS_MODELS_DATA, null, RELATED_INFO_SAAS_MODELS_DATA_TEXT,
              TranslationHandler.translate("!!SAA/BK-Gültigkeiten zu Baumuster \"%1\"", saasModelsDataQuery.getModelId().getModelNumber()));
        setModelId(saasModelsDataQuery.getModelId());
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        if ((getConnector().getActiveRelatedSubForm() == this)) {
            String newModelNumber = iPartsVirtualNode.getModelNumberFromAssemblyId(getConnector().getRelatedInfoData().getSachAssemblyId());
            iPartsModelId newModelId = null;
            if (newModelNumber != null) {
                newModelId = new iPartsModelId(newModelNumber);
            }
            if ((newModelId == null) || hasIdChanged(newModelId)) {
                setModelId(newModelId);
            }
        }
    }

    public void setModelId(iPartsModelId modelId) {
        this.modelId = modelId;
        dataToGrid();
    }

    /**
     * Abfrage, ob die übergebene ID identisch ist zur aktuellen ID
     *
     * @param modelId
     * @return
     */
    private boolean hasIdChanged(iPartsModelId modelId) {
        return !Utils.objectEquals(modelId, this.modelId);
    }

    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields() {
        List<EtkDisplayField> defaultDisplayFields = new ArrayList<EtkDisplayField>();
        EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_SAA_NO, false, false);
        displayField.setColumnFilterEnabled(true);
        defaultDisplayFields.add(displayField);
        displayField = new EtkDisplayField(TABLE_DA_EDS_SAA_MODELS, iPartsDataVirtualFieldsDefinition.DESM_DESCRIPTION, true, false);
        displayField.setColumnFilterEnabled(true);
        displayField = new EtkDisplayField(TABLE_DA_EDS_SAA_MODELS, iPartsDataVirtualFieldsDefinition.DESM_DESCRIPTION_CONST, true, false);
        displayField.setColumnFilterEnabled(true);
        defaultDisplayFields.add(displayField);

        return defaultDisplayFields;
    }

    @Override
    protected DBDataObjectList<? extends EtkDataObject> createDataObjectList() {
        iPartsDataSAAModelsList saaModelsList = new iPartsDataSAAModelsList();
        if (modelId != null) {
            saaModelsList = iPartsDataSAAModelsList.loadAllSaasAndBKsForModel(getProject(), modelId);
        }
        return saaModelsList;
    }


    /**
     * Callback zur Bestimmung der Daten für die Anzeige der SAA/BK-Gültigkeiten zu einem EDS-Baumuster.
     */
    public static abstract interface SAAsModelsDataCallback {

        SAAsModelsDataQuery getSAAsModelsDataQuery();
    }


    /**
     * Abfrageinformationen zur Bestimmung der Daten für die Anzeige der SAA/BK-Gültigkeiten zu einem EDS-Baumuster.
     */
    public static class SAAsModelsDataQuery {

        iPartsModelId modelId;

        public SAAsModelsDataQuery(iPartsModelId modelId) {
            this.modelId = modelId;
        }

        public SAAsModelsDataQuery() {
            this(new iPartsModelId());
        }

        public iPartsModelId getModelId() {
            return modelId;
        }

        public void setModelId(iPartsModelId modelId) {
            this.modelId = modelId;
        }
    }
}