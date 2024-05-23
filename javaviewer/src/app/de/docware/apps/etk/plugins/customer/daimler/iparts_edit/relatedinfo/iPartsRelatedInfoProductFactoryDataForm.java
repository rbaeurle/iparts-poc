/*
 * Copyright (c) 2018 Docware GmbH
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
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataFactoriesList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.AbstractSimpleDataObjectGridForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.util.Utils;
import de.docware.util.sql.TableAndFieldName;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * DAMILER-1855, Related Info, Anzeige der gültigen Werke zum Produkt
 */
public class iPartsRelatedInfoProductFactoryDataForm extends AbstractSimpleDataObjectGridForm implements iPartsConst {

    public static final String IPARTS_MENU_ITEM_SHOW_PRODUCT_FACTORIES_DATA = "iPartsMenuItemShowProductFactoriesData";

    private static final EnumSet<iPartsModuleTypes> VALID_MODULE_TYPES = EnumSet.of(iPartsModuleTypes.PRODUCT_MODEL);
    private iPartsProductId productId;

    /**
     * Funktion, die die DEFAULT-Spalten des Ergebnis-Grids festlegt, falls niemand die Spalten über die Workbench konfiguriert.
     */
    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields() {
        List<EtkDisplayField> defaultDisplayFields = new ArrayList<EtkDisplayField>();
        EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_FACTORIES, FIELD_DF_LETTER_CODE, false, false);
        displayField.setColumnFilterEnabled(true);
        defaultDisplayFields.add(displayField);
        displayField = new EtkDisplayField(TABLE_DA_FACTORIES, FIELD_DF_FACTORY_NO, false, false);
        displayField.setColumnFilterEnabled(true);
        defaultDisplayFields.add(displayField);
        displayField = new EtkDisplayField(TABLE_DA_FACTORIES, FIELD_DF_DESC, true, false);
        defaultDisplayFields.add(displayField);

        return defaultDisplayFields;
    }

    @Override
    protected DBDataObjectList<? extends EtkDataObject> createDataObjectList() {
        iPartsDataFactoriesList dataListFactories = new iPartsDataFactoriesList();
        if (productId != null) {
            dataListFactories.searchSortAndFillWithJoin(getProject(), getProject().getDBLanguage(), grid.getDisplayFields(),
                                                        new String[]{ FIELD_DF_FACTORY_NO },                // Feld(er) der Tabelle für den Join
                                                        TABLE_DA_PRODUCT_FACTORIES,                         // Join-Tabelle
                                                        new String[]{ FIELD_DPF_FACTORY_NO },               // Join-Tabellenfeld(er)
                                                        true,                                               // LeftOuterJoin?
                                                        false,                                              // Sind die Bedingungen vom Join ODER-verknüpft?
                                                        new String[]{ TableAndFieldName.make(TABLE_DA_PRODUCT_FACTORIES, FIELD_DPF_PRODUCT_NO) }, // Where-Felder inkl. Tabellennamen
                                                        new String[]{ productId.getProductNumber() },       // Where-Werte
                                                        false,                                              // Sind die Where-Bedingungen ODER-verknüpft?
                                                        new String[]{ FIELD_DF_FACTORY_NO },                // Feld(er) der Tabelle zum Sortieren
                                                        false                                               // nicht caseInsensitive
            );

/*      Das entstehende SQL-Statement:

        select da_factories.df_letter_code,
            da_factories.df_factory_no,
            da_factories.df_desc,
            da_product_factories.dpf_factory_no,
            s0.s_benenn as s0_da_factories_df_desc
        from da_product_factories
            left outer join da_factories on ((da_factories.df_factory_no = da_product_factories.dpf_factory_no))
            left outer join sprache as s0 on (da_factories.df_desc = s0.s_textnr and s0.s_feld = 'DA_FACTORIES.DF_DESC' and s0.s_sprach = 'DE')
        where (da_product_factories.dpf_product_no = 'C01')
*/
        }

        return dataListFactories;
    }

    /**
     * Modifiziert das Popupmenü der Stückliste in der übergebenen Form.
     *
     * @param popupMenu
     * @param connector
     */
    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_PRODUCT_FACTORIES_DATA, RELATED_INFO_PRODUCT_FACTORIES_DATA_TEXT,
                                DefaultImages.module.getImage(), CONFIG_KEY_RELATED_INFO_PRODUCT_FACTORIES_DATA);
    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        updatePartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_PRODUCT_FACTORIES_DATA, VALID_MODULE_TYPES);
    }

    /**
     * Modifiziert das Popupmenü des Baumes in der übergebenen Form.
     *
     * @param menu
     * @param formWithTree
     */
    public static void modifyTreePopupMenu(GuiContextMenu menu, final AbstractJavaViewerForm formWithTree) {
        modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_SHOW_PRODUCT_FACTORIES_DATA, RELATED_INFO_PRODUCT_FACTORIES_DATA_TEXT,
                            CONFIG_KEY_RELATED_INFO_PRODUCT_FACTORIES_DATA);
    }

    // Wird verwendet bei ...
    public static void updateTreePopupMenu(GuiContextMenu popupMenu, AssemblyTreeFormIConnector connector) {
        updateTreePopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_PRODUCT_FACTORIES_DATA, VALID_MODULE_TYPES);
    }

    // Wird verwendet bei ...
    public static boolean relatedInfoIsVisible(EtkDataAssembly assembly) {
        return relatedInfoIsVisible(assembly, VALID_MODULE_TYPES);
    }

    /**
     * Constructor, erzeugt einen neuen Dialog für die Anzeige der Werke zu einem Produkt
     *
     * @param dataConnector
     * @param parentForm
     * @param relatedInfo
     */
    public iPartsRelatedInfoProductFactoryDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                   IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo, iPartsEditConfigConst.iPARTS_EDIT_CONFIG_PRODUCT_FACTORIES_KEY, null);

    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        if ((getConnector().getActiveRelatedSubForm() == this)) {
            String newProductNumber = iPartsVirtualNode.getProductNumberFromAssemblyId(getConnector().getRelatedInfoData().getSachAssemblyId());
            iPartsProductId id = null;
            if (newProductNumber != null) {
                id = new iPartsProductId(newProductNumber);
            }
            updateGridIfNecessary(id);
        }
    }

    /**
     * Das Grid aktualisieren.
     *
     * @param id
     */
    public void updateGridIfNecessary(iPartsProductId id) {
        // Nur wenn sich die ID geändert hat, wird die Anzeige aktualisiert.
        if ((id == null) || hasIdChanged(id)) {
            this.productId = id;
            dataToGrid();
        }
    }

    /**
     * Abfrage, ob die übergebene ID identisch ist zur aktuell gesetzten ID
     *
     * @param id
     * @return
     */
    private boolean hasIdChanged(iPartsProductId id) {
        return !Utils.objectEquals(id, this.productId);
    }
}