/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.mechanic.treeview.forms.AssemblyTreeFormIConnector;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiTabbedPane;
import de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;

import java.util.EnumSet;
import java.util.List;

/**
 * Related Info Dialog für die Anzeige der Filterabsicherung (mit ungefilterter Stückliste inkl. Grund für die Ausfilterung
 * von Stücklisteneinträgen bzw. Baumusterauswertung)
 */
public class iPartsRelatedInfoFilterValidationForm extends AbstractRelatedInfoPartlistDataForm implements iPartsConst {

    public static final String IPARTS_MENU_ITEM_SHOW_FILTER_VALIDATION = "iPartsMenuItemShowFilterValidation";

    private static final EnumSet<iPartsModuleTypes> VALID_MODULE_TYPES = EnumSet.of(iPartsModuleTypes.DialogRetail,
                                                                                    iPartsModuleTypes.EDSRetail,
                                                                                    iPartsModuleTypes.PSK_PKW,
                                                                                    iPartsModuleTypes.PSK_TRUCK,
                                                                                    iPartsModuleTypes.SA_TU,
                                                                                    iPartsModuleTypes.PRODUCT,
                                                                                    iPartsModuleTypes.PRODUCT_MODEL,
                                                                                    iPartsModuleTypes.KG,
                                                                                    iPartsModuleTypes.TU,
                                                                                    iPartsModuleTypes.SpecialCatKG,
                                                                                    iPartsModuleTypes.WorkshopMaterial,
                                                                                    iPartsModuleTypes.CAR_PERSPECTIVE);

    private GuiTabbedPane tabbedPane;
    private iPartsRelatedInfoFilterReasonDataForm unfilteredPartListForm;
    private iPartsRelatedInfoModelEvaluationForm modelEvaluationForm;

    /**
     * Modifiziert das Popupmenü des Baumes in der übergebenen Form.
     *
     * @param menu
     * @param formWithTree
     */
    public static void modifyTreePopupMenu(GuiContextMenu menu, final AbstractJavaViewerForm formWithTree) {
        GuiMenuItem menuItem = modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_SHOW_FILTER_VALIDATION, RELATED_INFO_FILTER_VALIDATION_TEXT,
                                                   CONFIG_KEY_RELATED_INFO_FILTER_VALIDATION);
        if (menuItem != null) {
            menuItem.setIcon(DefaultImages.filter.getImage());
        }
    }

    // Wird verwendet bei ...
    public static void updateTreePopupMenu(GuiContextMenu popupMenu, AssemblyTreeFormIConnector connector) {
        updateTreePopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_FILTER_VALIDATION, VALID_MODULE_TYPES);
    }

    // Wird verwendet bei ...
    public static boolean relatedInfoIsVisible(EtkDataAssembly assembly) {
        return relatedInfoIsVisible(assembly, VALID_MODULE_TYPES);
    }

    /**
     * Erzeugt einen neuen Dialog für die Anzeige der Filterabsicherung.
     *
     * @param dataConnector
     * @param parentForm
     * @param relatedInfo
     */
    public iPartsRelatedInfoFilterValidationForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                 IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo);

        unfilteredPartListForm = new iPartsRelatedInfoFilterReasonDataForm(dataConnector, this, relatedInfo);
        modelEvaluationForm = new iPartsRelatedInfoModelEvaluationForm(dataConnector, this, relatedInfo);

        tabbedPane = new GuiTabbedPane();

        // Ungefilterte Stückliste
        GuiTabbedPaneEntry unfilteredPartListTabbedPaneEntry = new GuiTabbedPaneEntry("!!Ungefilterte Stückliste");
        unfilteredPartListTabbedPaneEntry.addChild(unfilteredPartListForm.getGui());
        tabbedPane.addChild(unfilteredPartListTabbedPaneEntry);

        // Baumusterauswertung (nur bei DIALOG und EDS Retail-Stücklisten unter Berücksichtigung vom Ausblenden einzelner
        // Baugruppen, die nur eine Kind-Baugruppe haben)
        EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getProject(), dataConnector.getRelatedInfoData().getSachAssemblyId());
        String partListType = assembly.getLastHiddenSingleSubAssemblyOrThis(null).getEbeneName();
        if (partListType.equals(iPartsConst.PARTS_LIST_TYPE_DIALOG_RETAIL) ||
            partListType.equals(iPartsConst.PARTS_LIST_TYPE_EDS_RETAIL)) {
            GuiTabbedPaneEntry modelEvaluationTabbedPaneEntry = new GuiTabbedPaneEntry("!!Baumusterauswertung");
            modelEvaluationTabbedPaneEntry.addChild(modelEvaluationForm.getGui());
            tabbedPane.addChild(modelEvaluationTabbedPaneEntry);
        }

        tabbedPane.selectTab(unfilteredPartListTabbedPaneEntry);
    }

    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields(String configKey) {
        return null;
    }

    @Override
    public AbstractGuiControl getGui() {
        return tabbedPane;
    }

    @Override
    public void dispose() {
        unfilteredPartListForm.dispose();
        modelEvaluationForm.dispose();
        super.dispose();
    }
}