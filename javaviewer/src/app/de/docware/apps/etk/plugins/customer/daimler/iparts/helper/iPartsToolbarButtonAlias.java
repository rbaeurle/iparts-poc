/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.forms.toolbar.ToolbarButtonAlias;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.plugins.customer.daimler.iparts.images.iPartsDefaultImages;
import de.docware.framework.modules.gui.design.DesignImage;

/**
 * Sammlung der standardmäßigen iParts Toolbar-Buttons inkl. Alias-Namen zur Identifizierung
 */
public class iPartsToolbarButtonAlias extends ToolbarButtonAlias {

    // Allgemein nutzbare Konstanten
    public static final iPartsToolbarButtonAlias EDIT_NEW = new iPartsToolbarButtonAlias("buttonNew", DefaultImages.newObject, "", "!!Neu", false);
    public static final iPartsToolbarButtonAlias EDIT_CUT = new iPartsToolbarButtonAlias("buttonCut", iPartsDefaultImages.edit_btn_cut, "", "!!Ausschneiden", false);
    public static final iPartsToolbarButtonAlias EDIT_COPY = new iPartsToolbarButtonAlias("buttonEditCopy", iPartsDefaultImages.edit_btn_toClipboard, "", "!!Kopieren", false);
    public static final iPartsToolbarButtonAlias EDIT_PASTE = new iPartsToolbarButtonAlias("buttonEditPaste", iPartsDefaultImages.edit_btn_fromClipboard, "", "!!Einfügen", false);
    public static final iPartsToolbarButtonAlias EDIT_DELETE = new iPartsToolbarButtonAlias("buttonEditDelete", DefaultImages.delete, "", "!!Löschen", false);
    public static final iPartsToolbarButtonAlias EDIT_DELETEALL = new iPartsToolbarButtonAlias("buttonEditDeleteAll", DefaultImages.cancel, "", "!!Alles löschen", false);
    public static final iPartsToolbarButtonAlias EDIT_SELECTALL = new iPartsToolbarButtonAlias("buttonSelectAll", iPartsDefaultImages.edit_btn_selectAll, "", "!!Alles auswählen", false);
    public static final iPartsToolbarButtonAlias EDIT_AO_ACTIVATE = new iPartsToolbarButtonAlias("buttonActivate", iPartsDefaultImages.edit_btn_ao_activate, "", "!!Autoren-Auftrag aktivieren", false);
    public static final iPartsToolbarButtonAlias EDIT_AO_DEACTIVATE = new iPartsToolbarButtonAlias("buttonDeactivate", iPartsDefaultImages.edit_btn_ao_deactivate, "", "!!Autoren-Auftrag deaktivieren", false);
    public static final iPartsToolbarButtonAlias EDIT_DUPLICATE = new iPartsToolbarButtonAlias("buttonDuplicate", DefaultImages.copy, "", "!!Duplizieren", false);
    public static final iPartsToolbarButtonAlias EDIT_NEW_WITH_PRODUCT_SELECT = new iPartsToolbarButtonAlias("buttonNewWithProductSelect", iPartsDefaultImages.edit_btn_new_with_product_select, "", "!!Neu mit Produktauswahl", false);
    public static final iPartsToolbarButtonAlias EDIT_LOCK_PART_LIST_ENTRY = new iPartsToolbarButtonAlias("buttonLockPartListEntry", iPartsDefaultImages.edit_lock_entry, "", "!!Stücklisteneintrag für Edit sperren", false);
    public static final iPartsToolbarButtonAlias EDIT_UNLOCK_PART_LIST_ENTRY = new iPartsToolbarButtonAlias("buttonUnlockPartListEntry", iPartsDefaultImages.edit_unlock_entry, "", "!!Stücklisteneintrag für Edit entsperren", false);

    public static final iPartsToolbarButtonAlias REFRESH_WINDOW = new iPartsToolbarButtonAlias("buttonRefreshWindow", iPartsDefaultImages.edit_refresh_view, iPartsDefaultImages.edit_refresh_view_hover, "", "!!Fenster aktualisieren");
    public static final iPartsToolbarButtonAlias MOVE_TO_PARENTWINDOW = new iPartsToolbarButtonAlias("buttonMoveToParentWindow", iPartsDefaultImages.btn_move_to_parentwindow, iPartsDefaultImages.btn_move_to_parentwindow_hover, "", "!!Schließen und Selektion übernehmen");
    public static final iPartsToolbarButtonAlias FILTER_IPARTS = new iPartsToolbarButtonAlias("buttoniPartsFilter", DefaultImages.t2filter, DefaultImages.t2filterHover, "", "!!Filter");
    public static final iPartsToolbarButtonAlias TOOLBAR_FILTER_FIELD = new iPartsToolbarButtonAlias("toolbarFilterField", null, null, "", "");
    public static final iPartsToolbarButtonAlias SHOW_LIST = new iPartsToolbarButtonAlias("buttonImageShowList", iPartsDefaultImages.edit_show_list, "", "!!Liste anzeigen", false);

    public static final iPartsToolbarButtonAlias IMG_UP = new iPartsToolbarButtonAlias("buttonImageUp", iPartsDefaultImages.edit_btn_up, "", "!!Nach oben schieben", false);
    public static final iPartsToolbarButtonAlias IMG_UP_ALL = new iPartsToolbarButtonAlias("buttonImageUpAll", iPartsDefaultImages.edit_btn_up_all, "", "!!Alle nach oben schieben", false);
    public static final iPartsToolbarButtonAlias IMG_DOWN = new iPartsToolbarButtonAlias("buttonImageDown", iPartsDefaultImages.edit_btn_down, "", "!!Nach unten schieben", false);
    public static final iPartsToolbarButtonAlias IMG_DOWN_ALL = new iPartsToolbarButtonAlias("buttonImageDownAll", iPartsDefaultImages.edit_btn_down_all, "", "!!Alle nach unten schieben", false);
    public static final iPartsToolbarButtonAlias IMG_LEFT = new iPartsToolbarButtonAlias("buttonImageLeft", iPartsDefaultImages.edit_btn_left, "", "!!Nach links schieben", false);
    public static final iPartsToolbarButtonAlias IMG_LEFT_ALL = new iPartsToolbarButtonAlias("buttonImageLeftAll", iPartsDefaultImages.edit_btn_left_all, "", "!!Alle nach links schieben", false);
    public static final iPartsToolbarButtonAlias IMG_RIGHT = new iPartsToolbarButtonAlias("buttonImageRight", iPartsDefaultImages.edit_btn_right, "", "!!Nach rechts schieben", false);
    public static final iPartsToolbarButtonAlias IMG_RIGHT_ALL = new iPartsToolbarButtonAlias("buttonImageRightAll", iPartsDefaultImages.edit_btn_right_all, "", "!!Alle nach rechts schieben", false);

    public iPartsToolbarButtonAlias(String alias, DesignImage image, String text, String tooltip, boolean isToggle) {
        super(alias, image, text, tooltip, isToggle);
    }

    public iPartsToolbarButtonAlias(String alias, DesignImage image, DesignImage imageHover, String text, String tooltip) {
        super(alias, image, imageHover, null, null, text, tooltip, false);
    }

    public iPartsToolbarButtonAlias(String alias, DesignImage image, DesignImage imageHover, DesignImage imageSelected, DesignImage imageDisabled, String text, String tooltip, boolean isToggle) {
        super(alias, image, imageHover, imageSelected, imageDisabled, text, tooltip, isToggle);
    }
}
