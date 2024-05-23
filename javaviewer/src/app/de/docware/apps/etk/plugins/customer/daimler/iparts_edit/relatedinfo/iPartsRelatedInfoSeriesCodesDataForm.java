/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.mechanic.treeview.forms.AssemblyTreeFormIConnector;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.AbstractSimpleDataObjectGridForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditSeriesCodesForm;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * RelatedInfoForm für die Series Series-Codes: Anzeige Baubarkeit (X4E) zur Baureihe
 */
public class iPartsRelatedInfoSeriesCodesDataForm extends AbstractSimpleDataObjectGridForm implements iPartsConst {

    public static final String IPARTS_MENU_ITEM_SERIES_CODES = "iPartsMenuItemSeriesCodes";

    private static final EnumSet<iPartsModuleTypes> VALID_MODULE_TYPES = EnumSet.of(iPartsModuleTypes.PRODUCT_MODEL,
                                                                                    iPartsModuleTypes.CONSTRUCTION_SERIES);

    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SERIES_CODES, iPartsConst.RELATED_INFO_SERIES_CODES_TEXT,
                                DefaultImages.module.getImage(),
                                iPartsConst.CONFIG_KEY_RELATED_INFO_SERIES_CODES_DATA);
    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        EtkDataAssembly destAssembly = getDestinationAssemblyForPartListEntryFromConnector(connector);
        updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_SERIES_CODES, isPopUpMenuItemVisible(destAssembly));

    }

    public static void modifyTreePopupMenu(GuiContextMenu menu, final AbstractJavaViewerForm formWithTree) {
        modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_SERIES_CODES, iPartsConst.RELATED_INFO_SERIES_CODES_TEXT,
                            iPartsConst.CONFIG_KEY_RELATED_INFO_SERIES_CODES_DATA);
    }

    public static void updateTreePopupMenu(GuiContextMenu popupMenu, AssemblyTreeFormIConnector connector) {
        EtkDataAssembly assembly = connector.getCurrentAssembly();
        setVisibilityForItem(popupMenu, IPARTS_MENU_ITEM_SERIES_CODES, isPopUpMenuItemVisible(assembly));
    }


    private static boolean isPopUpMenuItemVisible(EtkDataAssembly assembly) {
        return (assembly != null) && relatedInfoIsVisible(assembly, VALID_MODULE_TYPES) && isValidForSeriesCodeRelInfo(assembly);
    }

    /**
     * Liefert zurück, ob der Pfad in der übergebenen {@link EtkDataAssembly} gerade auf einen Knoten/Eintrag zeigt,
     * der die Code zu Barureihe (X4E) anzeigen lassen könnte.
     *
     * @param assembly
     * @return
     */
    public static boolean isValidForSeriesCodeRelInfo(EtkDataAssembly assembly) {
        // DAIMLER-6328
        if (assembly instanceof iPartsDataAssembly) {
            iPartsDataAssembly iPartsDataAssembly = (iPartsDataAssembly)assembly;
            if (iPartsDataAssembly.isProductModelAssembly() || iPartsDataAssembly.isConstructionSeriesAssembly()) {
                IdWithType id = iPartsDataAssembly.getVirtualNodesPath().get(0).getId();
                if ((id instanceof iPartsProductId) || (id instanceof iPartsSeriesId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private EditSeriesCodesForm seriesCodesForm;

    protected iPartsRelatedInfoSeriesCodesDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                   IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo, "", iPartsConst.RELATED_INFO_SERIES_CODES_TEXT);
    }

    @Override
    protected void postCreateGui() {
        setDataObjectGridTitle();
        seriesCodesForm = new EditSeriesCodesForm(getConnector(), this);
        ConstraintsBorder constraints = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
        seriesCodesForm.getGui().setConstraints(constraints);
        getPanelDataObjectGrid().addChild(seriesCodesForm.getGui());
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        if (getConnector().getActiveRelatedSubForm() == this) {
            String seriesNumber = iPartsVirtualNode.getSeriesNumberFromAssemblyId(getConnector().getRelatedInfoData().getSachAssemblyId());
            if (!StrUtils.isValid(seriesNumber)) {
                String productNo = iPartsVirtualNode.getProductNumberFromAssemblyId(getConnector().getRelatedInfoData().getSachAssemblyId());
                if (StrUtils.isValid(productNo)) {
                    Set<String> modelTypesSet = iPartsProduct.getInstance(getProject(), new iPartsProductId(productNo)).getReferencedSeriesOrAllModelTypes(getProject());
                    if (!modelTypesSet.isEmpty()) {
                        seriesNumber = modelTypesSet.iterator().next();
                    }
                }
            }
            iPartsSeriesId seriesId = null;
            if (StrUtils.isValid(seriesNumber)) {
                seriesId = new iPartsSeriesId(seriesNumber);
                title = TranslationHandler.translate(iPartsConst.RELATED_INFO_SERIES_CODES_TEXT_MULTIPLE, seriesNumber);
            } else {
                title = iPartsConst.RELATED_INFO_SERIES_CODES_TEXT;
            }
            setDataObjectGridTitle();
            seriesCodesForm.setSeriesId(seriesId);
        }
    }

    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields() {
        return null;
    }

    @Override
    protected DBDataObjectList<? extends EtkDataObject> createDataObjectList() {
        return null;
    }
}
