/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images;

import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.framework.modules.gui.design.DesignImage;
import de.docware.framework.modules.gui.misc.resources.FrameworkImage;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * iParts-Edit Images
 */
public class EditDefaultImages extends DefaultImages {

    private static Map<String, DefaultImages> editDefaultImages = new HashMap<>();

    public static final EditDefaultImages edit_btn_makeFlat = new EditDefaultImages("MakeFlat", "img_edit_make_flat.png");
    public static final EditDefaultImages edit_btn_constructKits = new EditDefaultImages("ConstructKits", "img_edit_construction_kits.png");
    public static final EditDefaultImages edit_btn_constructKits_new = new EditDefaultImages("ConstructKitsNew", "img_edit_construction_kits_new.png");
    public static final EditDefaultImages edit_btn_wireHarness = new EditDefaultImages("WireHarness", "img_edit_wire_harness.png");
    public static final EditDefaultImages edit_btn_footNotes = new EditDefaultImages("FootNotes", "img_edit_footnotes.png");
    public static final EditDefaultImages edit_btn_hierachy = new EditDefaultImages("Hierarchy", "img_edit_hierachy.png");
    public static final EditDefaultImages edit_btn_numPos = new EditDefaultImages("NumPos", "img_edit_numbering_pos.png");
    public static final EditDefaultImages edit_btn_compress = new EditDefaultImages("Compress", "img_edit_compress_entries.png");
    public static final EditDefaultImages edit_btn_edit_material = new EditDefaultImages("DetailWindow", "img_edit_material.png");
    public static final EditDefaultImages edit_btn_edit_partslistentry = new EditDefaultImages("EditPartListEntry", "img_edit_partlistentry.png");
    public static final EditDefaultImages edit_btn_undo = new EditDefaultImages("Undo", "img_edit_undo.png");
    public static final EditDefaultImages edit_btn_replace = new EditDefaultImages("Replace", "img_replace.png");
    public static final EditDefaultImages edit_btn_refresh = new EditDefaultImages("Refresh", "img_edit_refresh.png");
    public static final EditDefaultImages edit_btn_picture_order = new EditDefaultImages("PictureOrder", "img_edit_picture_order.png");
    public static final EditDefaultImages edit_btn_picture_change_order = new EditDefaultImages("PictureChangeOrder", "img_edit_picture_change_order.png");
    public static final EditDefaultImages edit_btn_picture_order_closed = new EditDefaultImages("PictureOrderClosed", "img_edit_picture_order_closed.png");
    public static final EditDefaultImages edit_btn_add_to_picture_order = new EditDefaultImages("AddToPictureOrder", "img_edit_add_to_picture_order.png");
    public static final EditDefaultImages edit_history = new EditDefaultImages("History", "img_history.png");
    public static final EditDefaultImages edit_module_master_data = new EditDefaultImages("ModuleMasterData", "img_edit_module_master_data.png");
    public static final EditDefaultImages edit_code = new EditDefaultImages("Code", "img_code.png");
    public static final EditDefaultImages edit_colorVariants = new EditDefaultImages("ColorVariants", "img_color_variants.png");
    public static final EditDefaultImages edit_colorVariants_invalid = new EditDefaultImages("ColorVariantsInvalid", "img_color_variants_invalid.png");
    public static final EditDefaultImages edit_factoryData = new EditDefaultImages("FactoryData", "img_factory_data.png");
    public static final EditDefaultImages edit_factoryData_invalid = new EditDefaultImages("FactoryDataInvalid", "img_factory_data_invalid.png");
    public static final EditDefaultImages edit_factoryData_notEndnumberFilterRelevant = new EditDefaultImages("FactoryDataNotEndnumberFilterRelevant", "img_factory_data_not_endnumber_filter_relevant.png");
    public static final EditDefaultImages edit_ww_parts = new EditDefaultImages("OptionalParts", "img_optional_parts.png");
    public static final EditDefaultImages edit_alternative_materials = new EditDefaultImages("AlternativeParts", "img_alternative_materials.png");
    public static final EditDefaultImages edit_single_pic_for_part = new EditDefaultImages("SinglePictureForPart", "img_single_pic_for_part.png");
    public static final EditDefaultImages edit_product = new EditDefaultImages("Product", "img_product.png");
    public static final EditDefaultImages edit_btn_internal_text = new EditDefaultImages("InternalText", "img_edit_internal_text.png");
    public static final EditDefaultImages edit_validate_hotspot = new EditDefaultImages("ValidateHotspot", "img_edit_validate_hotspot.png");
    public static final EditDefaultImages edit_validate_assembly = new EditDefaultImages("ValidateAssembly", "img_edit_validate_assembly.png");
    public static final EditDefaultImages edit_btn_edit_multiple = new EditDefaultImages("EditMultiple", "img_edit_multiple.png");
    public static final EditDefaultImages edit_btn_accept_material_changes = new EditDefaultImages("AcceptMaterialChanges", "img_edit_accept_material_changes.png");
    public static final EditDefaultImages edit_btn_accept_partlistentry_changes = new EditDefaultImages("AcceptPartListEntryChanges", "img_edit_accept_partlistentry_changes.png");
    public static final EditDefaultImages edit_btn_view_all_pages = new EditDefaultImages("EditAllPages", "img_edit_pages_all.png");
    public static final EditDefaultImages edit_btn_view_all_pages_selected = new EditDefaultImages("EditAllPagesSelected", "img_edit_pages_all_selected.png");
    public static final EditDefaultImages edit_btn_partlistentry_modified = new EditDefaultImages("PartListEntryModified", "img_edit_partlistentry_modified.png");
    public static final EditDefaultImages edit_btn_partlistentry_modified_selected = new EditDefaultImages("PartListEntryModifiedSelected", "img_edit_partlistentry_modified_selected.png");
    public static final EditDefaultImages edit_copy_tu = new EditDefaultImages("CopyTU", "img_edit_copy_tu.png");
    public static final EditDefaultImages edit_update_tu = new EditDefaultImages("UpdateTU", "img_edit_merge.png");
    public static final EditDefaultImages edit_btn_edit_merge = new EditDefaultImages("EditMerge", "img_edit_merge.png");
    public static final EditDefaultImages edit_sort_by_hotspot = new EditDefaultImages("SortByHotspot", "img_sort_by_hotspot.png");
    public static final EditDefaultImages edit_import_ctt = new EditDefaultImages("ImportCTT", "img_import_ctt.png");
    public static final EditDefaultImages edit_import_excel = new EditDefaultImages("ImportExcel", "img_import_excel.png");
    public static final EditDefaultImages edit_btn_module_in_edit = new EditDefaultImages("ModuleInEdit", "img_module_in_edit.png");
    public static final EditDefaultImages edit_btn_module_in_conflict = new EditDefaultImages("ModuleInConflict", "img_module_in_conflict.png");
    public static final EditDefaultImages edit_btn_show_all_pic_orders = new EditDefaultImages("ShowAllPicOrders", "img_edit_show_all_picorder_active.png");
    public static final EditDefaultImages edit_btn_show_all_pic_orders_pressed = new EditDefaultImages("ShowAllPicOrdersPressed", "img_edit_show_all_picorder_white.png");
    public static final EditDefaultImages edit_btn_optimize_north = new EditDefaultImages("OptimizeNorth", "img_optimize_north.png");
    public static final EditDefaultImages edit_btn_optimize_south = new EditDefaultImages("OptimizeSouth", "img_optimize_south.png");

    public static final EditDefaultImages edit_btn_order_assign = new EditDefaultImages("OrderMove", "img_edit_order_move.png");
    public static final EditDefaultImages edit_order_status_history = new EditDefaultImages("OrderStatusHistory", "img_order_status_history.png");
    public static final EditDefaultImages edit_order_partial_undo = new EditDefaultImages("OrderPartialUndo", "img_order_partial_undo.png");

    public static final EditDefaultImages edit_rep_predecessor = new EditDefaultImages("Predecessor", "img_replace_predecessor.png");
    public static final EditDefaultImages edit_rep_predecessor_virt = new EditDefaultImages("PredecessorVirtual", "img_replace_predecessor_virt.png");
    public static final EditDefaultImages edit_rep_successor = new EditDefaultImages("Successor", "img_replace_successor.png");
    public static final EditDefaultImages edit_rep_successor_virt = new EditDefaultImages("SuccessorVirtual", "img_replace_successor_virt.png");

    public static final EditDefaultImages edit_rep_predecessor_successor = new EditDefaultImages("PredecessorAndSuccessor", "img_replace_both.png");
    public static final EditDefaultImages edit_rep_predecessor_virt_successor_virt = new EditDefaultImages("PredecessorVirtualAndSuccessorVirtual", "img_replace_both_virt.png");
    public static final EditDefaultImages edit_rep_predecessor_virt_successor = new EditDefaultImages("PredecessorVirtualAndSuccessor", "img_replace_both_preVirt.png");
    public static final EditDefaultImages edit_rep_predecessor_successor_virt = new EditDefaultImages("PredecessorAndSuccessorVirtual", "img_replace_both_succVirt.png");

    public static final EditDefaultImages edit_rep_chain = new EditDefaultImages("ReplaceChain", "img_replace_chain.png");
    public static final EditDefaultImages edit_rep_chain_both = new EditDefaultImages("ReplaceChainPredecessorAndSuccessor", "img_replace_chain_both.png");

    public static final EditDefaultImages edit_editModulesToolbarButton = new EditDefaultImages("EditModulesToolbarButton", "img_edit_modules.png");
    public static final EditDefaultImages edit_editModulesToolbarButtonWhite = new EditDefaultImages("EditModulesToolbarButtonWhite", "img_edit_modules_w.png");
    public static final EditDefaultImages edit_editModulesToolbarButtonGray = new EditDefaultImages("EditModulesToolbarButtonGray", "img_edit_modules_g.png");

    public static final EditDefaultImages edit_workEditToolbarButton = new EditDefaultImages("WorkEditToolbarButton", "img_workedit.png");
    public static final EditDefaultImages edit_workEditToolbarButtonWhite = new EditDefaultImages("WorkEditToolbarButtonWhite", "img_workedit_w.png");
    public static final EditDefaultImages edit_workEditToolbarButtonGray = new EditDefaultImages("WorkEditToolbarButtonGray", "img_workedit_g.png");

    public static final EditDefaultImages edit_openNonModalMechanicWindow = new EditDefaultImages("OpenNonModalMechanicWindow", "img_open_non_modal_mechanic_window.png");

    public static final EditDefaultImages edit_btn_copy_pic_order = new EditDefaultImages("CopyPicOrder", "img_copy_fd_selected.png");
    public static final EditDefaultImages edit_btn_copy_factory_data_toClipboard = new EditDefaultImages("CopyFactoryData", "img_copy_fd_selected.png");
    public static final EditDefaultImages edit_btn_paste_factory_data_toClipboard = new EditDefaultImages("PasteFactoryData", "img_paste_fd_selected.png");

    public static final EditDefaultImages edit_btn_copy_factory_data_toClipboard_and_link = new EditDefaultImages("CopyFactoryDataAndLink", "img_paste_and_link_selected.png");
    public static final EditDefaultImages edit_btn_unlink_factory_data = new EditDefaultImages("UnlinkFactoryData", "img_unlink_selected.png");

    public static final EditDefaultImages edit_mailbox_ToolbarButton = new EditDefaultImages("MailboxToolbarButton", "img_mailbox.png");
    public static final EditDefaultImages edit_mailbox_ToolbarButtonWhite = new EditDefaultImages("MailboxToolbarButtonWhite", "img_mailbox_w.png");
    public static final EditDefaultImages edit_mailbox_ToolbarButtonGray = new EditDefaultImages("MailboxToolbarButtonGray", "img_mailbox_g.png");
    public static final EditDefaultImages edit_mailboxEmpty_ToolbarButton = new EditDefaultImages("MailboxEmptyToolbarButton", "img_mailbox_empty.png");
    public static final EditDefaultImages edit_mailbox_view = new EditDefaultImages("MailboxView", "img_mailbox_view.png");
    public static final EditDefaultImages edit_mailbox_read = new EditDefaultImages("MailboxRead", "img_mailbox_read.png");
    public static final EditDefaultImages edit_mailbox_read_selected = new EditDefaultImages("MailboxReadSelected", "img_mailbox_read_selected.png");
    public static final EditDefaultImages edit_mailbox_unread = new EditDefaultImages("MailboxUnread", "img_mailbox_unread.png");
    public static final EditDefaultImages edit_mailbox_answer = new EditDefaultImages("MailboxAnswer", "img_mailbox_answer.png");
    public static final EditDefaultImages edit_mailbox_forward = new EditDefaultImages("MailboxForward", "img_mailbox_forward.png");

    public static final EditDefaultImages edit_workBasket_ToolbarButton = new EditDefaultImages("WorkBasketToolbarButton", "img_eds_workbasket.png");
    public static final EditDefaultImages edit_workBasket_ToolbarButtonWhite = new EditDefaultImages("WorkBasketToolbarButtonWhite", "img_eds_workbasket_w.png");
    public static final EditDefaultImages edit_workBasket_ToolbarButtonGray = new EditDefaultImages("WorkBasketToolbarButtonGray", "img_eds_workbasket_g.png");

    public static final EditDefaultImages edit_dict_CopyText = new EditDefaultImages("DictCopyText", "img_edit_dict_copy_text.png");

    private EditDefaultImages(String name, String filename) {
        super(name, filename, true, EditDefaultImages.editDefaultImages);
    }

    /**
     * Liste aller Bilder in dieser Klasse.
     *
     * @return
     */
    public static Collection<? extends DesignImage> getImages() {
        return Collections.unmodifiableCollection(EditDefaultImages.editDefaultImages.values());
    }

    public static FrameworkImage getByFilename(String filename) {
        return getByFilename(filename, EditDefaultImages.editDefaultImages);
    }
}
