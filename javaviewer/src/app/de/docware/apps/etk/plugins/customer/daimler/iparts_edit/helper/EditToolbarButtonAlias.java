/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts.images.iPartsDefaultImages;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditAuthorOrderView;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.framework.modules.gui.design.DesignImage;

/**
 * Sammlung der standardmäßigen iParts-Edit Toolbar-Buttons inkl. Alias-Namen zur Identifizierung
 */
public class EditToolbarButtonAlias extends iPartsToolbarButtonAlias {

    // Konstanten für die Assembly ImageForm
    public static final EditToolbarButtonAlias IMG_NEW = new EditToolbarButtonAlias("buttonImageNew", DefaultImages.newObject, "", "!!Neu", false);
    public static final EditToolbarButtonAlias IMG_EDIT = new EditToolbarButtonAlias("buttonImageEdit", DefaultImages.edit, "", "!!Bearbeiten", false);
    public static final EditToolbarButtonAlias IMG_DELETE = new EditToolbarButtonAlias("buttonImageDelete", DefaultImages.delete, "", "!!Löschen", false);
    public static final EditToolbarButtonAlias IMG_REPLACE = new EditToolbarButtonAlias("buttonImageReplace", EditDefaultImages.edit_btn_replace, "", "!!Ersetzen", false);
    public static final EditToolbarButtonAlias IMG_COPY = new EditToolbarButtonAlias("buttonImageCopy", DefaultImages.copy, "", "!!Kopieren", false);
    public static final EditToolbarButtonAlias IMG_UP = new EditToolbarButtonAlias("buttonImageUp", iPartsDefaultImages.edit_btn_up, "", "!!Nach oben schieben", false);
    public static final EditToolbarButtonAlias IMG_UP_ALL = new EditToolbarButtonAlias("buttonImageUpAll", iPartsDefaultImages.edit_btn_up_all, "", "!!Alle nach oben schieben", false);
    public static final EditToolbarButtonAlias IMG_DOWN = new EditToolbarButtonAlias("buttonImageDown", iPartsDefaultImages.edit_btn_down, "", "!!Nach unten schieben", false);
    public static final EditToolbarButtonAlias IMG_DOWN_ALL = new EditToolbarButtonAlias("buttonImageDownAll", iPartsDefaultImages.edit_btn_down_all, "", "!!Alle nach unten schieben", false);
    public static final EditToolbarButtonAlias IMG_LEFT = new EditToolbarButtonAlias("buttonImageLeft", iPartsDefaultImages.edit_btn_left, "", "!!Nach links schieben", false);
    public static final EditToolbarButtonAlias IMG_LEFT_ALL = new EditToolbarButtonAlias("buttonImageLeftAll", iPartsDefaultImages.edit_btn_left_all, "", "!!Alle nach links schieben", false);
    public static final EditToolbarButtonAlias IMG_RIGHT = new EditToolbarButtonAlias("buttonImageRight", iPartsDefaultImages.edit_btn_right, "", "!!Nach rechts schieben", false);
    public static final EditToolbarButtonAlias IMG_RIGHT_ALL = new EditToolbarButtonAlias("buttonImageRightAll", iPartsDefaultImages.edit_btn_right_all, "", "!!Alle nach rechts schieben", false);
    public static final EditToolbarButtonAlias IMG_PICORDER = new EditToolbarButtonAlias("buttonPictureOrder", EditDefaultImages.edit_btn_picture_order, "", "!!Bildauftrag erstellen", false);
    public static final EditToolbarButtonAlias IMG_PIC_CHANGE_ORDER = new EditToolbarButtonAlias("buttonPictureChangeOrder", EditDefaultImages.edit_btn_picture_change_order, "", "!!Änderungsauftrag erstellen", false);
    public static final EditToolbarButtonAlias IMG_COPY_PIC_ORDER = new EditToolbarButtonAlias("buttonCopyPictureOrder", EditDefaultImages.edit_btn_copy_pic_order, "", "!!Kopierauftrag erstellen", false);
    public static final EditToolbarButtonAlias IMG_ADD_TO_PICORDER = new EditToolbarButtonAlias("buttonAddToPictureOrder", EditDefaultImages.edit_btn_add_to_picture_order, "", "!!Zum Bildauftrag hinzufügen", false);
    public static final EditToolbarButtonAlias IMG_REFRESH = new EditToolbarButtonAlias("buttonImageRefresh", EditDefaultImages.edit_btn_refresh, "", "!!Bildaufträge aktualisieren", false);
    public static final EditToolbarButtonAlias IMG_OPEN_MC_IN_ASPLM = new EditToolbarButtonAlias("buttonOpenMCInASPLM", EditDefaultImages.edit_openNonModalMechanicWindow, "", "!!In AS-PLM suchen", false);
    public static final EditToolbarButtonAlias IMG_FILTER_PIC_ORDERS = new EditToolbarButtonAlias("buttonFillPicOrders", EditDefaultImages.edit_btn_show_all_pic_orders, null, EditDefaultImages.edit_btn_show_all_pic_orders_pressed, null, "", "!!Alle Bildaufträge anzeigen", true);
    public static final EditToolbarButtonAlias IMG_OPEN_IN_NEW_WINDOW = new EditToolbarButtonAlias("buttonOpenImagesInNewWindow", EditDefaultImages.edit_openNonModalMechanicWindow, "", "!!Auswahl in einem neuen Fenster öffnen...", false);
    public static final EditToolbarButtonAlias IMG_MULTIPLE_USE = new EditToolbarButtonAlias("buttonImageMultipleUse", DefaultImages.module, "", "!!Mehrfachverwendung in TU", false);

    // Konstanten für die Assembly ListForm
    public static final EditToolbarButtonAlias EDIT_WORK = new EditToolbarButtonAlias("buttonWork", DefaultImages.edit, "", "!!Bearbeiten", false);
    public static final EditToolbarButtonAlias EDIT_USAGE = new EditToolbarButtonAlias("buttonUsage", DefaultImages.usageAssemblyToPart, "", "!!Verwendung", false);
    public static final EditToolbarButtonAlias EDIT_REPLACEMENTS = new EditToolbarButtonAlias("buttonReplacement", EditDefaultImages.edit_rep_predecessor_successor, "", "!!Ersetzungen bearbeiten", false);
    public static final EditToolbarButtonAlias EDIT_CHANGETOMAT = new EditToolbarButtonAlias("buttonChangeToMat", DefaultImages.part, "", "!!Umwandeln in Material", false);
    public static final EditToolbarButtonAlias EDIT_CHANGETOASSEMBLY = new EditToolbarButtonAlias("buttonChangeToAssembly", DefaultImages.module, "", "!!Umwandeln in Modul", false);
    public static final EditToolbarButtonAlias EDIT_MAT = new EditToolbarButtonAlias("buttonEditMat", EditDefaultImages.edit_btn_edit_material, "", "!!Material bearbeiten", false);
    public static final EditToolbarButtonAlias EDIT_PARTLISTENTRY = new EditToolbarButtonAlias("buttonEditPartListEntry", EditDefaultImages.edit_btn_edit_partslistentry, "", "!!Teileposition bearbeiten", false);
    public static final EditToolbarButtonAlias EDIT_INTERNAL_TEXT = new EditToolbarButtonAlias("buttonInternalText", EditDefaultImages.edit_btn_internal_text, "", "!!Interner Text vorhanden", false);
    public static final EditToolbarButtonAlias EDIT_MULTIPLE_ENTRIES = new EditToolbarButtonAlias("buttonEditMultipleEntries", EditDefaultImages.edit_btn_edit_multiple, "", "!!Teilepositionen vereinheitlichen", false);
    public static final EditToolbarButtonAlias EDIT_MERGING_ENTRIES = new EditToolbarButtonAlias("buttonEditMergingEntries", EditDefaultImages.edit_btn_edit_merge, "", "!!Teilepositionen zusammenlegen", false);
    public static final EditToolbarButtonAlias EDIT_CODE_MATRIX = new EditToolbarButtonAlias("buttonEditCodeMatrix", EditDefaultImages.edit_code, "", "!!Code-Erklärung anzeigen", false);
    public static final EditToolbarButtonAlias EDIT_PRIMUS_REPLACEMENT_CHAIN = new EditToolbarButtonAlias("buttonEditPrimus", null, "", "", false);
    public static final EditToolbarButtonAlias EDIT_DIALOG_RETAIL_REPLACEMENT_CHAIN = new EditToolbarButtonAlias("buttonEditReplacementChainDaialog", null, "", "", false);
    public static final EditToolbarButtonAlias EDIT_ACCEPT_MATERIAL_CHANGES = new EditToolbarButtonAlias("buttonEditAcceptMaterialChanges", EditDefaultImages.edit_btn_accept_material_changes, "", "!!ET-KZ-Änderung am Material bestätigen", false);
    public static final EditToolbarButtonAlias EDIT_ACCEPT_PARTLISTENTRY_CHANGES = new EditToolbarButtonAlias("buttonEditAcceptPartlistentryChanges", EditDefaultImages.edit_btn_accept_partlistentry_changes, "", "!!ETK-Änderung der Teileposition bestätigen", false);
    public static final EditToolbarButtonAlias EDIT_VIEW_ALL_PAGES = new EditToolbarButtonAlias("buttonEditViewAllPages", EditDefaultImages.edit_btn_view_all_pages,
                                                                                                EditDefaultImages.edit_btn_view_all_pages, EditDefaultImages.edit_btn_view_all_pages_selected,
                                                                                                null, "", "!!Alle Zeichnungsblätter in der Übersicht", false);

    public static final EditToolbarButtonAlias EDIT_NUMPOS = new EditToolbarButtonAlias("buttonNumPos", EditDefaultImages.edit_btn_numPos, "", "!!Positionen nummerieren", false);
    public static final EditToolbarButtonAlias EDIT_MAKEFLAT = new EditToolbarButtonAlias("buttonMakeFlat", EditDefaultImages.edit_btn_makeFlat, "", "!!Modul ausflachen", false);
    public static final EditToolbarButtonAlias EDIT_CONSTKITS = new EditToolbarButtonAlias("buttonConstKits", EditDefaultImages.edit_btn_constructKits, "", "!!Baukästen bearbeiten...", false);
    public static final EditToolbarButtonAlias EDIT_CONSTKITS_NEW = new EditToolbarButtonAlias("buttonConstKitsNew", EditDefaultImages.edit_btn_constructKits_new, "", "!!Baukästen bearbeiten...", false);
    public static final EditToolbarButtonAlias EDIT_FOOTNOTES = new EditToolbarButtonAlias("buttonFootNotes", EditDefaultImages.edit_btn_footNotes, "", "!!Fußnoten bearbeiten", false);
    public static final EditToolbarButtonAlias EDIT_HIERARCHY = new EditToolbarButtonAlias("buttonHierarchy", EditDefaultImages.edit_btn_hierachy, "", "!!Einrückung bearbeiten...", false);
    public static final EditToolbarButtonAlias EDIT_COMPRESS = new EditToolbarButtonAlias("buttonCompressEntries", EditDefaultImages.edit_btn_compress, "", "!!Positionen zusammenfassen", false);
    public static final EditToolbarButtonAlias EDIT_PRINT = new EditToolbarButtonAlias("buttonPrint", DefaultImages.printer, "", "!!Aktuelle Stückliste drucken", false);
    public static final EditToolbarButtonAlias EDIT_CLEARSORT = new EditToolbarButtonAlias("buttonClearSort", DefaultImages.clearSort, "", "!!Sortierung aufheben", false);
    public static final EditToolbarButtonAlias EDIT_OPTIONALPARTS = new EditToolbarButtonAlias("buttonOptionalParts", EditDefaultImages.edit_ww_parts, "", "!!Wahlweise-Teile bearbeiten", false);
    public static final EditToolbarButtonAlias EDIT_DELETE_OPTIONAL_PART = new EditToolbarButtonAlias("buttonDeleteOptionalPart", DefaultImages.cancel, "", "!!Eintrag aus Wahlweise-Set entfernen", false);
    public static final EditToolbarButtonAlias EDIT_COLORTABLES = new EditToolbarButtonAlias("buttonColortables", EditDefaultImages.edit_colorVariants, "", "!!Variantenzuordnung zu Teil bearbeiten", false);
    public static final EditToolbarButtonAlias EDIT_FACTORY_DATA = new EditToolbarButtonAlias("buttonFactoryData", EditDefaultImages.edit_factoryData, "", "!!Werksdaten bearbeiten", false);

    public static final EditToolbarButtonAlias EDIT_SORT_BY_HOTSPOT = new EditToolbarButtonAlias("sortByHotspot", EditDefaultImages.edit_sort_by_hotspot, "", "!!TU nach Hotspot sortieren", false);
    public static final EditToolbarButtonAlias EDIT_MOVE_UP = new EditToolbarButtonAlias("buttonMoveUp", iPartsDefaultImages.edit_btn_up, "", "!!Stücklisteneintrag nach oben verschieben", false);
    public static final EditToolbarButtonAlias EDIT_MOVE_DOWN = new EditToolbarButtonAlias("buttonMoveDown", iPartsDefaultImages.edit_btn_down, "", "!!Stücklisteneintrag nach unten verschieben", false);
    public static final EditToolbarButtonAlias EDIT_MOVE_TO_POSITION = new EditToolbarButtonAlias("buttonMoveToPosition", iPartsDefaultImages.edit_btn_move_to_position, "", "!!Stücklisteneintrag verschieben nach ...", false);
    public static final EditToolbarButtonAlias EDIT_COPY_ENTRY = new EditToolbarButtonAlias("buttonCopyEntryToOtherTU", DefaultImages.copy, "", "!!Stücklisteneintrag in anderen TU kopieren", false);
    public static final EditToolbarButtonAlias EDIT_RELOCATE_ENTRY = new EditToolbarButtonAlias("buttonRelocateEntryToOtherTU", iPartsDefaultImages.edit_btn_right_all, "", "!!Stücklisteneintrag in anderen TU verschieben", false);
    public static final EditToolbarButtonAlias EDIT_COPY_ENTRY_TO_SA = new EditToolbarButtonAlias("buttonCopyEntryToOtherSA", DefaultImages.copy, "", "!!Stücklisteneintrag in andere SA kopieren", false);

    public static final EditToolbarButtonAlias EDIT_PARTLISTENTRY_MODIFIED = new EditToolbarButtonAlias("buttonPartListEntryModified", EditDefaultImages.edit_btn_partlistentry_modified,
                                                                                                        EditDefaultImages.edit_btn_partlistentry_modified,
                                                                                                        EditDefaultImages.edit_btn_partlistentry_modified_selected,
                                                                                                        null, "", "!!Stücklisteneintrag ist neu", false);

    // Konstanten für die Edit Header
    public static final EditToolbarButtonAlias EDIT_CLOSE_TAB = new EditToolbarButtonAlias("buttonCloseTab", DefaultImages.close, "", "!!Schließen", false);
    public static final EditToolbarButtonAlias EDIT_SAVE = new EditToolbarButtonAlias("buttonSave", DefaultImages.save, "", "!!Speichern", false);
    public static final EditToolbarButtonAlias EDIT_DOWNLOAD = new EditToolbarButtonAlias("buttonDownload", DefaultImages.save, "", "!!Herunterladen...", false);
    public static final EditToolbarButtonAlias EDIT_UNDO = new EditToolbarButtonAlias("buttonUndo", EditDefaultImages.edit_btn_undo, "", "!!Änderungen rückgängig machen", false);
    public static final EditToolbarButtonAlias EDIT_UNDOMODULE = new EditToolbarButtonAlias("buttonUndoModule", DefaultImages.delete, "", "!!Modul löschen", false);
    public static final EditToolbarButtonAlias EDIT_DELETE_EMPTY_MODULE = new EditToolbarButtonAlias("buttonDeleteEmptyModule", iPartsDefaultImages.edit_clear, "", "!!Leeren TU sofort löschen", false);
    public static final EditToolbarButtonAlias EDIT_HDR_USAGE = new EditToolbarButtonAlias("buttonHdrUsage", DefaultImages.usageAssemblyToPart, "", "!!Verwendung", false);
    public static final EditToolbarButtonAlias EDIT_HDR_PRINT = new EditToolbarButtonAlias("buttonHdrPrint", DefaultImages.printer, "", "!!Modul drucken", false);
    public static final EditToolbarButtonAlias EDIT_MODULE_MASTER_DATA = new EditToolbarButtonAlias("buttonModuleMasterData", EditDefaultImages.edit_module_master_data, "", "!!Stammdaten der Stückliste", false);
    public static final EditToolbarButtonAlias EDIT_TEST = new EditToolbarButtonAlias("buttonTest", EditDefaultImages.edit_validate_hotspot, "", "!!Hotspots prüfen", false);
    public static final EditToolbarButtonAlias EDIT_VALIDATE_ASSEMBLY = new EditToolbarButtonAlias("buttonValidateAssembly", EditDefaultImages.edit_validate_assembly, "", "!!TU überprüfen", false);
    public static final EditToolbarButtonAlias EDIT_ASSEMBLY = new EditToolbarButtonAlias("buttonEditAssembly", DefaultImages.module, "", "!!TU bearbeiten", false);
    public static final EditToolbarButtonAlias EDIT_IMPORT_CTT = new EditToolbarButtonAlias("importCTT", EditDefaultImages.edit_import_ctt, "", "!!Import SAP.CTT-Datei", false);
    public static final EditToolbarButtonAlias EDIT_IMPORT_PSK = new EditToolbarButtonAlias("importPSK", EditDefaultImages.edit_import_excel, "", "!!Import PSK/Excel-Datei", false);
    public static final EditToolbarButtonAlias EDIT_IMPORT_EXCEL = new EditToolbarButtonAlias("importExcel", EditDefaultImages.edit_import_excel, "", "!!Import Excel-Datei", false);
    public static final EditToolbarButtonAlias EDIT_MODULE_IN_EDIT = new EditToolbarButtonAlias("buttonModuleInEdit", EditDefaultImages.edit_btn_module_in_edit, "", "!!TU wird bearbeitet", false);
    public static final EditToolbarButtonAlias EDIT_MODULE_HISTORY = new EditToolbarButtonAlias("buttonModuleHistory", EditDefaultImages.edit_history, "", EditAuthorOrderView.IPARTS_MENU_ITEM_SHOW_MODULE_HISTORY_TEXT, false);

    public static final EditToolbarButtonAlias EDIT_COPY_FACTORY_DATA = new EditToolbarButtonAlias("buttonEditCopy", EditDefaultImages.edit_btn_copy_factory_data_toClipboard, "", "!!Werksdaten kopieren", false);
    public static final EditToolbarButtonAlias EDIT_PASTE_FACTORY_DATA = new EditToolbarButtonAlias("buttonEditPaste", EditDefaultImages.edit_btn_paste_factory_data_toClipboard, "", "!!Werksdaten einfügen", false);
    public static final EditToolbarButtonAlias EDIT_PASTE_AND_LINK_FD = new EditToolbarButtonAlias("buttonEditPasteAndLink", EditDefaultImages.edit_btn_copy_factory_data_toClipboard_and_link, "", "!!Werksdaten einfügen und koppeln", false);
    public static final EditToolbarButtonAlias EDIT_UNLINK_FD = new EditToolbarButtonAlias("buttonEditUnlink", EditDefaultImages.edit_btn_unlink_factory_data, "", "!!Werksdaten entkoppeln", false);
    public static final EditToolbarButtonAlias EDIT_GO_TO_BCTE = new EditToolbarButtonAlias("buttonEditGoToBCTE", EditDefaultImages.edit_openNonModalMechanicWindow, "", "!!Gehe zur gekoppelten Teilepostion", false);

    // Konstanten für die Autoren-Aufträge
    public static final EditToolbarButtonAlias EDIT_ORDER_ASSIGN = new EditToolbarButtonAlias("buttonOrderAssign", EditDefaultImages.edit_btn_order_assign, "", "!!Autoren-Auftrag zuweisen...", false);
    public static final EditToolbarButtonAlias EDIT_AO_COMPLETE_HISTORY = new EditToolbarButtonAlias("buttonOrderCompleteHistory", EditDefaultImages.edit_history, "", "!!Komplette Historie", false);
    public static final EditToolbarButtonAlias EDIT_AO_COMPLETE_HISTORY_FOR_TU = new EditToolbarButtonAlias("buttonOrderCompleteHistoryForTU", EditDefaultImages.edit_history, "", "!!Komplette Historie des Autoren-Auftrags", false);
    public static final EditToolbarButtonAlias EDIT_AO_UNDO = new EditToolbarButtonAlias("buttonOrderUndo", iPartsDefaultImages.edit_clear, "", "!!Alle Änderungen rückgängig machen", false);
    public static final EditToolbarButtonAlias EDIT_AO_STATUS_HISTORY = new EditToolbarButtonAlias("buttonOrderStatusHistory", EditDefaultImages.edit_order_status_history, "", "!!Autoren-Auftrags-Historie", false);
    public static final EditToolbarButtonAlias EDIT_PRE_RELEASE_CHECK = new EditToolbarButtonAlias("buttonPreReleaseCheck", EditDefaultImages.edit_validate_assembly, "", "!!Freigabevorprüfung", false);
    public static final EditToolbarButtonAlias EDIT_AO_EXPORT = new EditToolbarButtonAlias("buttonorderExport", iPartsDefaultImages.edit_export, "", "!!Autoren-Auftrag exportieren", false);
    public static final EditToolbarButtonAlias EDIT_AO_IMPORT = new EditToolbarButtonAlias("buttonOrderImport", iPartsDefaultImages.edit_import, "", "!!Autoren-Auftrag importieren", false);
    public static final EditToolbarButtonAlias EDIT_AO_PARTIAL_UNDO = new EditToolbarButtonAlias("buttonOrderPartialUndo", EditDefaultImages.edit_order_partial_undo, "", "!!Änderung zurücknehmen", false);

    // Mailbox
    public static final EditToolbarButtonAlias EDIT_VIEW_MESSAGE = new EditToolbarButtonAlias("buttonViewMessage", EditDefaultImages.edit_mailbox_view, null, null, null, "", "!!Nachricht anzeigen", false);
    public static final EditToolbarButtonAlias EDIT_ANSWER_MESSAGE = new EditToolbarButtonAlias("buttonAnswerMessage", EditDefaultImages.edit_mailbox_answer, null, null, null, "", "!!Nachricht beantworten", false);
    public static final EditToolbarButtonAlias EDIT_FORWARD_MESSAGE = new EditToolbarButtonAlias("buttonForwardMessage", EditDefaultImages.edit_mailbox_forward, null, null, null, "", "!!Nachricht weiterleiten", false);
    public static final EditToolbarButtonAlias EDIT_SHOW_READ_MESSAGES = new EditToolbarButtonAlias("buttonShowReadMessages", EditDefaultImages.edit_mailbox_read, null, EditDefaultImages.edit_mailbox_read_selected, null, "", "!!Auch gelesene Nachrichten anzeigen", true);

    protected EditToolbarButtonAlias(String alias, DesignImage image, String text, String tooltip, boolean isToggle) {
        super(alias, image, text, tooltip, isToggle);
    }

    public EditToolbarButtonAlias(String alias, DesignImage image, DesignImage imageHover, DesignImage imageSelected, DesignImage imageDisabled, String text, String tooltip, boolean isToggle) {
        super(alias, image, imageHover, imageSelected, imageDisabled, text, tooltip, isToggle);
    }
}
