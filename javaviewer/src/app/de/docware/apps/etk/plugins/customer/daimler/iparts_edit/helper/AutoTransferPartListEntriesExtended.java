package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.mechanic.treeview.forms.AbstractAssemblyTreeForm;
import de.docware.apps.etk.base.mechanic.treeview.forms.AssemblyTreeFormIConnector;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;

import java.util.EnumSet;

public class AutoTransferPartListEntriesExtended {

    public static final String IPARTS_MENU_ITEM_AUTO_TRANSFER_PART_LIST_ENTRIES_EXTENDED = "iPartsMenuItemAutoTransferPartListEntriesExtended";
    public static final String IPARTS_MENU_ITEM_AUTO_TRANSFER_PART_LIST_ENTRIES_EXTENDED_TEXT = "!!Automatische Submodul-Verarbeitung";
    public static final EnumSet<iPartsModuleTypes> VALID_MODULE_TYPES = EnumSet.of(iPartsModuleTypes.Dialog_SM_Construction);

    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        GuiMenuItem menuItem = AbstractRelatedInfoPartlistDataForm.modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_AUTO_TRANSFER_PART_LIST_ENTRIES_EXTENDED,
                                                                                           IPARTS_MENU_ITEM_AUTO_TRANSFER_PART_LIST_ENTRIES_EXTENDED_TEXT, DefaultImages.module.getImage(),
                                                                                           null);
        menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                EtkDataAssembly assembly = AbstractRelatedInfoPartlistDataForm.getDestinationAssemblyForPartListEntryFromConnector(connector);
                if (assembly != null) {
                    AutoTransferPartListEntriesHelper.doExtendedAutoTransferPartListEntries(assembly, connector);
                }
            }
        });
    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        EtkDataAssembly destAssembly = AbstractRelatedInfoPartlistDataForm.getDestinationAssemblyForPartListEntryFromConnector(connector);
        boolean isVisible = connector.getProject().isRevisionChangeSetActiveForEdit() &&
                            AbstractRelatedInfoPartlistDataForm.relatedInfoIsVisible(destAssembly, VALID_MODULE_TYPES) &&
                            iPartsRight.AUTO_TRANSFER_PART_LIST_ENTRIES_EXTENDED.checkRightInSession();
        AbstractRelatedInfoPartlistDataForm.updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_AUTO_TRANSFER_PART_LIST_ENTRIES_EXTENDED, isVisible);
    }

    public static void modifyTreePopupMenu(GuiContextMenu menu, final AbstractAssemblyTreeForm formWithTree) {
        GuiMenuItem menuItem = AbstractRelatedInfoPartlistDataForm.modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_AUTO_TRANSFER_PART_LIST_ENTRIES_EXTENDED,
                                                                                       IPARTS_MENU_ITEM_AUTO_TRANSFER_PART_LIST_ENTRIES_EXTENDED_TEXT, null);
        menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                AssemblyTreeFormIConnector connector = formWithTree.getConnector();
                AutoTransferPartListEntriesHelper.doExtendedAutoTransferPartListEntries(connector.getCurrentAssembly(), connector);
            }
        });
    }

    public static void updateTreePopupMenu(GuiContextMenu popupMenu, AssemblyTreeFormIConnector connector) {
        GuiMenuItem menuItem = AbstractRelatedInfoPartlistDataForm.updateTreePopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_AUTO_TRANSFER_PART_LIST_ENTRIES_EXTENDED,
                                                                                       VALID_MODULE_TYPES);
        boolean isVisible = ((menuItem != null) && menuItem.isVisible()) && connector.getProject().isRevisionChangeSetActiveForEdit() &&
                            iPartsRight.AUTO_TRANSFER_PART_LIST_ENTRIES_EXTENDED.checkRightInSession();
        AbstractRelatedInfoPartlistDataForm.setVisibilityForItem(popupMenu, IPARTS_MENU_ITEM_AUTO_TRANSFER_PART_LIST_ENTRIES_EXTENDED, isVisible);
    }

}
