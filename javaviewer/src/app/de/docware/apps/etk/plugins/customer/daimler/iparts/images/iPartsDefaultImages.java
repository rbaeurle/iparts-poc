/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.images;

import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.framework.modules.gui.design.DesignCategory;
import de.docware.framework.modules.gui.design.DesignImage;
import de.docware.framework.modules.gui.misc.resources.FrameworkImage;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * iParts Images
 */
public class iPartsDefaultImages extends DefaultImages {

    private static Map<String, de.docware.apps.etk.base.misc.images.DefaultImages> defaultImages = new HashMap<>();

    public static final iPartsDefaultImages btn_jobs = new iPartsDefaultImages("JobsButton", "img_jobs.png");
    public static final iPartsDefaultImages btn_jobsWhite = new iPartsDefaultImages("JobsButtonWhite", "img_jobs_w.png");
    public static final iPartsDefaultImages btn_jobsGray = new iPartsDefaultImages("JobsButtonGray", "img_jobs_g.png");

    public static final iPartsDefaultImages edit_import_files = new iPartsDefaultImages("ImportFiles", "img_import_files.png");
    public static final iPartsDefaultImages edit_export = new iPartsDefaultImages("Export", "img_export.png");
    public static final iPartsDefaultImages edit_import = new iPartsDefaultImages("Import", "img_import.png");

    public static final iPartsDefaultImages edit_toggle_off = new iPartsDefaultImages("ToggleOff", "img_toggle_off.png");
    public static final iPartsDefaultImages edit_toggle_on = new iPartsDefaultImages("ToggleOn", "img_toggle_on.png");

    public static final iPartsDefaultImages edit_btn_cut = new iPartsDefaultImages("Cut", "img_edit_cut.png");
    public static final iPartsDefaultImages edit_btn_toClipboard = new iPartsDefaultImages("ToClipboard", "img_edit_toclipboard.png");
    public static final iPartsDefaultImages edit_btn_fromClipboard = new iPartsDefaultImages("FromClipboard", "img_edit_fromclipboard.png");
    public static final iPartsDefaultImages edit_btn_selectAll = new iPartsDefaultImages("SelectAll", "img_edit_selectall.png");
    public static final iPartsDefaultImages edit_btn_ao_activate = new iPartsDefaultImages("Activate", "img_edit_authororder_activate.png");
    public static final iPartsDefaultImages edit_btn_ao_deactivate = new iPartsDefaultImages("Deactivate", "img_edit_authororder_deactivate.png");
    public static final iPartsDefaultImages edit_btn_new_multiple = new iPartsDefaultImages("NewMultiple", "img_edit_new_multiple.png");
    public static final iPartsDefaultImages edit_btn_new_with_product_select = new iPartsDefaultImages("NewWithProductSelect", "img_new_with_product_select.png");

    public static final iPartsDefaultImages edit_lang_selectall = new iPartsDefaultImages("LangSelectAll", "img_lang_selectall.png");
    public static final iPartsDefaultImages edit_lang_daimler_select = new iPartsDefaultImages("LangDaimlerSelect", "img_lang_daimler_select.png");
    public static final iPartsDefaultImages edit_clear = new iPartsDefaultImages("clear", "img_clear.png");
    public static final iPartsDefaultImages edit_lock_entry = new iPartsDefaultImages("lockEntry", "img_lock_entry.png");
    public static final iPartsDefaultImages edit_unlock_entry = new iPartsDefaultImages("unlockEntry", "img_unlock_entry.png");

    public static final iPartsDefaultImages edit_refresh_view = new iPartsDefaultImages("RefreshView", "img_refresh_view.png");
    public static final iPartsDefaultImages edit_refresh_view_hover = new iPartsDefaultImages("RefreshViewHover", "img_refresh_view_hover.png");

    public static final iPartsDefaultImages btn_move_to_parentwindow = new iPartsDefaultImages("MoveToParentWindow", "img_move_to_parentwindow.png");
    public static final iPartsDefaultImages btn_move_to_parentwindow_hover = new iPartsDefaultImages("MoveToParentWindowHover", "img_move_to_parentwindow_hover.png");

    public static final iPartsDefaultImages edit_btn_up = new iPartsDefaultImages("MoveUp", "img_button_up.png");
    public static final iPartsDefaultImages edit_btn_up_all = new iPartsDefaultImages("MoveUpAll", "img_button_up_all.png");
    public static final iPartsDefaultImages edit_btn_down = new iPartsDefaultImages("MoveDown", "img_button_down.png");
    public static final iPartsDefaultImages edit_btn_down_all = new iPartsDefaultImages("MoveDownAll", "img_button_down_all.png");
    public static final iPartsDefaultImages edit_btn_left = new iPartsDefaultImages("MoveLeft", "img_button_left.png");
    public static final iPartsDefaultImages edit_btn_left_all = new iPartsDefaultImages("MoveLeftAll", "img_button_left_all.png");
    public static final iPartsDefaultImages edit_btn_right = new iPartsDefaultImages("MoveRight", "img_button_right.png");
    public static final iPartsDefaultImages edit_btn_right_all = new iPartsDefaultImages("MoveRightAll", "img_button_right_all.png");
    public static final iPartsDefaultImages edit_btn_move_to_position = new iPartsDefaultImages("MoveToPosition", "img_button_move_to_position.png");
    public static final iPartsDefaultImages edit_show_list = new iPartsDefaultImages("ShowList", "img_show_list.png");

    private iPartsDefaultImages(String name, String filename) {
        super(DesignCategory.PLUGIN, name, filename, true, iPartsDefaultImages.defaultImages, null, false, "ohne Beschreibung");
    }

    /**
     * Liste aller Bilder in dieser Klasse.
     *
     * @return
     */
    public static Collection<? extends DesignImage> getImages() {
        return Collections.unmodifiableCollection(iPartsDefaultImages.defaultImages.values());
    }

    public static FrameworkImage getByFilename(String filename) {
        return getByFilename(filename, iPartsDefaultImages.defaultImages);
    }
}
