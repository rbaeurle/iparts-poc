/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.saaworkbasket;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.HierarchicalIDWithType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.helper.iPartsEdsStructureHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsGotoHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.DocuBrowserEDSHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.iPartsWSWorkBasketItem;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.kemworkbasket.WorkBasketNutzDokRemarkHelper;
import de.docware.framework.modules.config.db.VirtualFieldDefinition;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.menu.GuiMenu;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;

import java.util.Comparator;
import java.util.List;

public class MasterDataEDSWorkBasketForm extends MasterDataAbstractWorkBasketForm {


    private static final String TABLE_WORK_BASKET_EDS = iPartsDataVirtualFieldsDefinition.TABLE_WORK_BASKET_EDS;
    private static final String FIELD_SAA_CASE = iPartsDataVirtualFieldsDefinition.WBE_SAA_CASE;

    public static void showEDSWorkBasket(AbstractJavaViewerForm owner) {
        AbstractJavaViewerForm activeForm = owner.getConnector().getActiveForm();
        showEDSWorkBasket(owner.getConnector(), activeForm);
    }

    public static void showEDSWorkBasket(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        MasterDataEDSWorkBasketForm dlg = getNewEDSWorkBasketInstance(dataConnector, parentForm);
        dlg.showModal();
    }

    /**
     * Neue Instanz von MasterDataEDSWorkBasketForm erzeugen und Default-Sachen vorbesetzen
     *
     * @param dataConnector
     * @param parentForm
     * @return
     */
    public static MasterDataEDSWorkBasketForm getNewEDSWorkBasketInstance(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        MasterDataEDSWorkBasketForm dlg = new MasterDataEDSWorkBasketForm(dataConnector, parentForm);

        EtkDisplayFields searchFields = getSearchFields(dataConnector);

        dlg.setEditFields(null);
        dlg.setSearchFields(searchFields);
        dlg.setEditAllowed(false);
        dlg.setModifyAllowed(false);
        dlg.setNewAllowed(false);
        dlg.setDeleteAllowed(false);
        dlg.showToolbar(false);
        dlg.showSearchFields(true);
        dlg.setMaxResults(-1);
        dlg.setTitlePrefix("!!EDS-SAA-Arbeitsvorrat");
        dlg.setWindowName("EDSWorkBasketMasterData");
        return dlg;
    }

    /**
     * SearchFields noch ohne Konfiguration besetzen
     *
     * @param dataConnector
     * @return
     */
    private static EtkDisplayFields getSearchFields(AbstractJavaViewerFormIConnector dataConnector) {
        EtkDisplayFields searchFields = new EtkDisplayFields();
        if (searchFields.size() == 0) {
            // Suchfelder definieren
            searchFields.addFeld(createSearchField(dataConnector.getProject(), TABLE_DA_PRODUCT_MODELS, FIELD_DPM_PRODUCT_NO, false, false));
            searchFields.addFeld(createSearchField(dataConnector.getProject(), TABLE_DA_WB_SAA_CALCULATION, FIELD_WSC_MODEL_NO, false, false));
            searchFields.addFeld(createSearchField(dataConnector.getProject(), TABLE_DA_WB_SAA_CALCULATION, FIELD_WSC_SAA, false, false));
//                searchFields.addFeld(createSearchField(dataConnector.getProject(), TABLE_WORK_BASKET_EDS, FIELD_SAA_CASE, false, false));
            EtkDisplayField field = createSearchField(dataConnector.getProject(), TABLE_WORK_BASKET_EDS, FIELD_SAA_CASE, false, false);
            VirtualFieldDefinition vField = iPartsDataVirtualFieldsDefinition.findField(TABLE_WORK_BASKET_EDS, FIELD_SAA_CASE);
            String labelText = "!!Geschäftsfall";
            if (vField != null) {
                labelText = vField.getDBDescription(dataConnector.getProject()).getDisplayName();
            }
            field.setDefaultText(false);
            field.setText(new EtkMultiSprache(labelText, new String[]{ TranslationHandler.getUiLanguage() }));
            searchFields.addFeld(field);

            searchFields.loadStandards(dataConnector.getConfig());
        }
        return searchFields;
    }

    protected GuiMenu docuBrowserMenu;

    /**
     * Erzeugt eine Instanz von SimpleMasterDataSearchResultGrid.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     * @param parentForm
     */
    public MasterDataEDSWorkBasketForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm, TABLE_WORK_BASKET_EDS, iPartsImportDataOrigin.EDS);
        nutzDokRemarkHelper = new WorkBasketNutzDokRemarkHelper(this, getProject(), iPartsWSWorkBasketItem.TYPE.SAA);
        searchHelper = new SearchWorkBasketHelperEDS(getProject(), getVirtualTableName(), this, nutzDokRemarkHelper);
        setSearchHelper(searchHelper);
        setDisplayResultFields(searchHelper.buildDisplayFields(), true);
    }

    /**
     * Callback für Goto-Construction falls gewollt mit Laden der TUs im Edit
     *
     * @param withOwnDialog
     */
    @Override
    protected void gotoConstruction(boolean withOwnDialog, boolean withLoadInEdit) {
        DBDataObjectAttributes attributes = getSelectedAttributes();
        if (attributes != null) {
            String modelNumber = getModelNumber(attributes);
            String saaBkNo = getSaaBkNo(attributes);
            iPartsEdsStructureHelper structureHelper = iPartsEdsStructureHelper.getInstance();
            EtkDataObjectList<? extends EtkDataObject> datalist = structureHelper.loadAllStructureEntriesForModelAndSaaBk(getProject(), modelNumber, saaBkNo);
            if (!datalist.isEmpty()) {
                List<? extends EtkDataObject> edsModelList = datalist.getAsList();
                edsModelList.sort((Comparator<EtkDataObject>)(o1, o2) -> {
                    String o1Value = o1.getFieldValue(structureHelper.getReleaseFromField());
                    String o2Value = o2.getFieldValue(structureHelper.getReleaseFromField());
                    return o2Value.compareTo(o1Value);
                });
                EtkDataObject newestEntry = edsModelList.get(0);
                HierarchicalIDWithType structureId = structureHelper.createStructureIdFromDataObject(newestEntry);
                iPartsGotoHelper.gotoEDSConstruction(getConnector(), this, withOwnDialog, modelNumber,
                                                     structureId, saaBkNo, null);
                if (withLoadInEdit) {
                    loadAssembliesFromAttributeIdListInEdit(attributes, withOwnDialog);
                }
            }
        }
    }

    @Override
    protected boolean checkSearchValueForEmptyProduct(String searchValue, String searchParamName) {
        // Das Baumuster und die SAA müssen mindestens 4-stellig sein (sofern nicht leer), wenn kein Produkt angegeben wurde
        if (StrUtils.isValid(searchValue) && (searchValue.replace("*", "").replace("?", "").length() < MIN_CHAR_COUNT_FOR_MODEL_AND_SAA)) {
            MessageDialog.showWarning(TranslationHandler.translate("!!Ohne explizit angegebenes Produkt muss der Suchtext für '%1' mindestens %2 Zeichen lang sein.",
                                                                   TranslationHandler.translate(searchParamName), String.valueOf(MIN_CHAR_COUNT_FOR_MODEL_AND_SAA)));
            return false;
        }
        return true;
    }

    // ===== Implementierung der abstrakten Getter aus der Basisklasse ====
    @Override
    protected String getVirtualTableName() {
        return TABLE_WORK_BASKET_EDS;
    }

    @Override
    protected boolean isSaaDisplayFieldName(String fieldName) {
        return fieldName.equals(getFieldSaa()) || fieldName.equals(FIELD_DS_SAA) || fieldName.equals(FIELD_DNS_SAA);
    }

    @Override
    protected void onTableSelectionChanged(Event event) {
        super.onTableSelectionChanged(event);
        if (docuBrowserMenu != null) {
            docuBrowserMenu.setVisible(iPartsEditPlugin.isDocuBrowserActive());
            if (docuBrowserMenu.isVisible()) {
                DBDataObjectAttributesList selectedAttribList = getSelectedAttributesList();
                docuBrowserMenu.setEnabled((selectedAttribList != null) && (selectedAttribList.size() == 1));
            }
        }
    }

    @Override
    protected void addCustomContextMenus(List<AbstractGuiControl> list) {
        docuBrowserMenu = DocuBrowserEDSHelper.createDocuBrowserPopupMenuItem(toolbarHelper,
                                                                              iPartsWorkBasketTypes.EDS_SAA_WB, getUITranslationHandler(),
                                                                              new EventListener(Event.MENU_ITEM_EVENT) {
                                                                                  @Override
                                                                                  public void fire(Event event) {
                                                                                      DBDataObjectAttributes attributes = getSelectedAttributes();
                                                                                      if (attributes != null) {
                                                                                          String saaBkNo = getSaaBkNo(attributes);
                                                                                          DocuBrowserEDSHelper.showDocuBrowserSAA(saaBkNo);
                                                                                      }
                                                                                  }
                                                                              });
        docuBrowserMenu.setVisible(false);
        list.add(docuBrowserMenu);
    }
}
