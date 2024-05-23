/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.dictionary;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumEntry;
import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumValue;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFieldsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.MenuRunnable;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.translation.forms.LanguageSelector;
import de.docware.apps.etk.base.translation.model.LanguageType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetSource;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNote;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.SessionKeyHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.footnotes.iPartsStandardFootNotesCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditUserControlForCreateFootnote;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditDataObjectGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.misc.DWPoint;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolbar;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogButtons;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogIcon;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.constraints.AbstractConstraints;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.*;

/**
 * Einfache Form zur Anzeige der TextArten und den Metadaten der Texte
 */
public class DictShowTextKindForm extends AbstractJavaViewerForm implements iPartsConst {

    private static final String PLACEHOLDER_TEXT_FIELD = VirtualFieldsUtils.addVirtualFieldMask("TEXT_FIELD"); // Platzhalter für den Text der angezeigt werden soll
    private static final int MAX_HITS_PER_SEARCH = 10000; // Maximale Trefferanzahl für eine Suche (Speicher!)
    private static final String COPY_TEXT_TO_NAME = "menuCopyTextTo";
    private static final String COPY_TEXT_TO_TEXT = "!!Text in weitere Textart kopieren";
    private static final String ADD_FOOTNOTE_NAME = "addFootnoteMenu";
    private static final String ADD_FOOTNOTE_TEXT = "!!Neue DIALOG-Fußnote anlegen";
    private static final boolean COPY_CREATE_EXACT_COPY = true;  // true: keine neue TextId anlegen

    public static boolean isCurrentUserProductAdministrator() {
        return iPartsRight.EDIT_TEXT_PRODUCT_ADMIN.checkRightInSession();
    }

    public static boolean isCurrentUserDataAdministrator() {
        return iPartsRight.EDIT_TEXT_DATA_ADMIN.checkRightInSession();
    }

    public static void showDictTextKind(AbstractJavaViewerForm owner) {
        // Aktive Form holen
        AbstractJavaViewerForm activeForm = owner.getConnector().getActiveForm();
        showDictTextKind(activeForm.getConnector(), activeForm);
    }

    public static void showDictTextKind(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        DictShowTextKindForm dlg = new DictShowTextKindForm(dataConnector, parentForm);
        dlg.showModal();
    }

    /**
     * Eigenes DataObjectGrid für weitere Methoden
     */
    private class DictDataObjectGrid extends EditDataObjectGrid {

        public DictDataObjectGrid(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
            super(dataConnector, parentForm);
        }

        public void scrollToIdIfExists(iPartsDictMetaId dictMetaId) {
            super.scrollToIdIfExists(dictMetaId, iPartsConst.TABLE_DA_DICT_META);
        }
    }

    private DictDataObjectGrid dataGrid;
    private LanguageSelector languageFormDatabase;
    private volatile FrameworkThread searchThread = null;
    private volatile boolean searchRunning = false;
    private Set<String> dictUsageSet = new TreeSet<>();
    private boolean withUsageFilter = false; //Schalter für den Filter nach S_FELD
    private boolean editAllowed;
    private Set<DictTextKindTypes> allowedTextKindTypes = new HashSet<>();
    private Set<DictTextKindTypes> allowedTextKindTypesForEditMigration = new HashSet<>();
    private GuiMenuItem menuCopyTextTo;
    private GuiMenuItem menuAddFootnote;
    private String lastSelectedLanguage;
    private boolean carAndVanInSession = iPartsRight.checkCarAndVanInSession();
    private boolean truckAndBusInSession = iPartsRight.checkTruckAndBusInSession();

    /**
     * Erzeugt eine Instanz von DictShoewTextKindForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public DictShowTextKindForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        editAllowed = isCurrentUserDataAdministrator() || isCurrentUserProductAdministrator();
        setTitle(editAllowed ? "!!Lexikon bearbeiten" : "!!Lexikon anzeigen");
        if (editAllowed) {
            if (isCurrentUserDataAdministrator()) {
                allowedTextKindTypes = DictTextKindTypes.getDataAdminTypes();
            } else if (isCurrentUserProductAdministrator()) {
                allowedTextKindTypes = DictTextKindTypes.getProductAdminTypes();
            }
            allowedTextKindTypesForEditMigration = DictTextKindTypes.getEditMigrationTypes();
        }
        postCreateGui();
    }

    private void onSearchCancelButtonClick(Event event) {
        startOrStopSearch();
    }

    /**
     * Startet oder Beendet die Suche
     */
    private void startOrStopSearch() {
        if (searchRunning) {
            stopSearch();
            showResultCount(getHitCount(), true);
        } else {
            reloadGrid(null, true);
        }
    }

    /**
     * Stoppt den Suchthread
     */
    private void stopSearch() {
        FrameworkThread searchThreadLocal = searchThread;
        if (searchThreadLocal != null) {
            searchThreadLocal.cancel(false);
            searchThread = null;
        }
        searchRunning = false;
        handleSearchButton(false);
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        mainWindow.panelMain.addEventListener(new EventListener(Event.KEY_PRESSED_EVENT) {
            @Override
            public void fire(Event event) {
                if (event.getIntParameter(Event.EVENT_PARAMETER_KEY_CODE) == KeyEvent.VK_ENTER) {

                    startOrStopSearch();
                }
            }
        });
        dataGrid = new DictDataObjectGrid(getConnector(), this) {
            @Override
            protected void onTableSelectionChanged(Event event) {
                if (searchRunning) {
                    stopSearch();
                }
                doEnableButtons();
            }

            @Override
            protected void onCellDblClicked(int row, int col, Event event) {
                doEditTextId(event);
            }

            @Override
            protected void onHeaderDblClicked(int col, Event event) {
                if (searchRunning) {
                    stopSearch();
                }
                doGridHeaderDoubleClick(event);
            }


            @Override
            protected void createToolbarButtons(GuiToolbar toolbar) {
                ToolbarButtonMenuHelper.ToolbarMenuHolder holder;
                if (editAllowed) {
                    holder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.IMG_NEW, getUITranslationHandler(), new MenuRunnable() {
                        public void run(Event event) {
                            doNewTextId(event);
                        }
                    });
                    contextmenuTextKind.addChild(holder.menuItem);
                }

                holder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_WORK, getUITranslationHandler(), new MenuRunnable() {
                    @Override
                    public void run(Event event) {
                        doEditTextId(event);
                    }
                });
                contextmenuTextKind.addChild(holder.menuItem);
                if (!editAllowed) {
                    holder.toolbarButton.setTooltip("!!Anzeigen");
                    holder.menuItem.setText("!!Anzeigen");
                }

                if (editAllowed) {
                    holder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_DELETE, getUITranslationHandler(), new MenuRunnable() {
                        @Override
                        public void run(Event event) {
                            doDeleteTextId(event);
                        }
                    });
                    contextmenuTextKind.addChild(holder.menuItem);
                }
                holder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_USAGE, getUITranslationHandler(), new MenuRunnable() {
                    @Override
                    public void run(Event event) {
                        doSearchUsage();
                    }
                });
                contextmenuTextKind.addChild(holder.menuItem);
                getToolbarHelper().hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_USAGE, contextmenuTextKind);
            }

            @Override
            protected void createContextMenuItems(GuiContextMenu contextMenu) {
                List<AbstractGuiControl> menuList = new DwList<>(contextmenuTextKind.getChildren());
                for (AbstractGuiControl menu : menuList) {
                    contextmenuTextKind.removeChild(menu);
                    contextMenu.addChild(menu);
                }
                menuCopyTextTo = getToolbarHelper().createMenuEntry(COPY_TEXT_TO_NAME, COPY_TEXT_TO_TEXT,
                                                                    EditDefaultImages.edit_dict_CopyText.getImage(),
                                                                    new EventListener(Event.MENU_ITEM_EVENT) {

                                                                        @Override
                                                                        public void fire(Event event) {
                                                                            doCopySelectedText();
                                                                        }
                                                                    }, DictShowTextKindForm.this.getUITranslationHandler());
                contextMenu.addChild(menuCopyTextTo);

                // Kontextmenü "Neue DIALOG-Fußnote anlegen" nur für die Textart Fußnote
                menuAddFootnote = getToolbarHelper().createMenuEntry(ADD_FOOTNOTE_NAME, ADD_FOOTNOTE_TEXT,
                                                                     EditDefaultImages.edit_btn_footNotes.getImage(),
                                                                     new EventListener(Event.MENU_ITEM_EVENT) {
                                                                         @Override
                                                                         public void fire(Event event) {
                                                                             doCreateFootNote();
                                                                         }
                                                                     }, DictShowTextKindForm.this.getUITranslationHandler());

                contextMenu.addChild(menuAddFootnote);
                menuAddFootnote.setVisible(false);
            }
        };
        //Enumwerte in Combobox setzen
        EnumValue sourceEnum = getProject().getEtkDbs().getEnumValue("DictionaryForeignSource");
        mainWindow.comboboxSource.addItem("");
        String viewerLanguage = getProject().getConfig().getCurrentViewerLanguage();
        for (Map.Entry<String, EnumEntry> entry : sourceEnum.entrySet()) {
            if (!checkSourceForSession(entry.getKey())) {
                continue;
            }

            mainWindow.comboboxSource.addItem(entry.getValue(), entry.getValue().getEnumText().getText(viewerLanguage));
        }
        mainWindow.comboboxSource.setMaximumRowCount(30);
        dataGrid.setPageSplitNumberOfEntriesPerPage(iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE);
        dataGrid.setDisplayFields(buildDisplayFields());

        ConstraintsBorder constraints = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
        dataGrid.getGui().setConstraints(constraints);
        mainWindow.panelGrid.addChild(dataGrid.getGui());

        GuiLabel lbl = new GuiLabel(getUITranslationHandler().getText("!!Suche in Sprache"));
        languageFormDatabase = new LanguageSelector(getProject(), true, false);
        languageFormDatabase.setWithHeader(false);
        languageFormDatabase.setLanguageType(LanguageType.LANGUAGE_DATABASE);
        languageFormDatabase.getCombobox().setMaximumRowCount(30);
        languageFormDatabase.setTestNames(LanguageType.LANGUAGE_DATABASE.name());
        languageFormDatabase.setListenerOnSpracheChanged(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                resetGrid(false);
                reloadGrid(null, false);
            }
        });
        AbstractConstraints langConstraints = mainWindow.comboBoxLanguage.getConstraints();
        mainWindow.panelLanguage.removeAllChildren();
        languageFormDatabase.getGui().setConstraints(langConstraints);
        de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag comboBoxLanguageLabelConstraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "c", "h", 12, 4, 4, 4);
        lbl.setConstraints(comboBoxLanguageLabelConstraints);
        mainWindow.panelLanguage.addChild(lbl);
        mainWindow.panelLanguage.addChild(languageFormDatabase.getGui());

        fillTextKinds();
        doEnableButtons();

        Dimension screenSize = FrameworkUtils.getScreenSize();
        mainWindow.setSize(screenSize.width - 20, screenSize.height - 20);
    }

    private boolean checkSourceForSession(String sourceKey) {
        // Für das Lexikon sind andere Quellen gültig als sonst (also NICHT iPartsImportDataOrigin.isSourceVisible() verwenden!)
        if (!carAndVanInSession && (sourceKey.equals(iPartsImportDataOrigin.IPARTS_MB.getOrigin()) || sourceKey.equals(iPartsImportDataOrigin.CONNECT.getOrigin()))) {
            // Ohne die Eigenschaften für PKW/Van dürfen Quelle IPARTS-MB und Connect nicht angezeigt werden
            return false;
        }
        if (!truckAndBusInSession && sourceKey.equals(iPartsImportDataOrigin.IPARTS_TRUCK.getOrigin())) {
            // Ohne die Eigenschaften für Truck/Bus darf Quelle IPARTS-TRUCK nicht angezeigt werden
            return false;
        }
        return true;
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public ModalResult showModal() {
        ModalResult modalResult = mainWindow.showModal();
        super.close();
        return modalResult;
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public EtkDisplayFields getSelectFields() {
        // SelectFields
        EtkDisplayFields selectFields = EtkDisplayFieldsHelper.createDefaultDisplayResultFields(getProject(), TABLE_DA_DICT_META);
        selectFields.addFeld(new EtkDisplayField(TABLE_SPRACHE, FIELD_S_BENENN, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_SPRACHE, FIELD_S_BENENN_LANG, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_SPRACHE, FIELD_S_FELD, false, false));
        return selectFields;
    }

    /**
     * Erstellt die benötigten Anzeigefelder
     *
     * @return
     */
    private EtkDisplayFields buildDisplayFields() {
        EtkDisplayFields displayFields = new EtkDisplayFields();
        EtkDisplayField displayField = new EtkDisplayField(iPartsConst.TABLE_DA_DICT_META, PLACEHOLDER_TEXT_FIELD, true, false);
        displayField.setDefaultText(false);
        displayField.setText(new EtkMultiSprache("!!Text", new String[]{ TranslationHandler.getUiLanguage() }));
        displayFields.addFeld(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_DICT_META, iPartsConst.FIELD_DA_DICT_META_FOREIGNID, false, false);
        displayFields.addFeld(displayField);
        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_DICT_META, iPartsConst.FIELD_DA_DICT_META_SOURCE, false, false);
        displayFields.addFeld(displayField);
        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_DICT_META, iPartsConst.FIELD_DA_DICT_META_STATE, false, false);
        displayFields.addFeld(displayField);
        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_DICT_META, iPartsConst.FIELD_DA_DICT_META_DIALOGID, false, false);
        displayField.setVisible(false);
        displayFields.addFeld(displayField);
        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_DICT_META, iPartsConst.FIELD_DA_DICT_META_ELDASID, false, false);
        displayField.setVisible(false);
        displayFields.addFeld(displayField);
        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_DICT_META, iPartsConst.FIELD_DA_DICT_META_CREATE, false, false);
        displayFields.addFeld(displayField);
        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_DICT_META, iPartsConst.FIELD_DA_DICT_META_CHANGE, false, false);
        displayFields.addFeld(displayField);
        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_DICT_META, iPartsConst.FIELD_DA_DICT_META_USERID, false, false);
        displayFields.addFeld(displayField);
        displayFields.loadStandards(getConfig());
        return displayFields;
    }

    private String getLanguage() {
        return languageFormDatabase.getSelectedLanguage();
    }

    /**
     * Befüllt die Textarten - Combobox
     */
    private void fillTextKinds() {
        mainWindow.comboboxTextKind.switchOffEventListeners();
        mainWindow.comboboxTextKind.removeAllItems();
        TreeMap<String, iPartsDataDictTextKind> textKinds = iPartsDataDictTextKindList.loadAllTextKindListSortedByName(getProject());
        for (Map.Entry<String, iPartsDataDictTextKind> textKind : textKinds.entrySet()) {
            mainWindow.comboboxTextKind.addItem(textKind.getValue(), textKind.getKey());
        }
        mainWindow.comboboxTextKind.setMaximumRowCount(Math.min(30, textKinds.size()));
        mainWindow.comboboxTextKind.switchOnEventListeners();
        mainWindow.comboboxTextKind.setSelectedIndex(getPreselectIndex());
    }

    private int getPreselectIndex() {
        int result = -1;
        if (mainWindow.comboboxTextKind.getItemCount() > 0) {
            result = 0;
            String preselectValue = SessionKeyHelper.getDictionaryPreSelectIndex();
            if (StrUtils.isValid(preselectValue)) {
                int index = 0;
                for (iPartsDataDictTextKind data : mainWindow.comboboxTextKind.getUserObjects()) {
                    if (data.getAsId().getTextKindId().equals(preselectValue)) {
                        result = index;
                        break;
                    }
                    index++;
                }
            }
        }
        return result;
    }

    private boolean isTextKindNewAllowed(iPartsDataDictTextKind dataTextKind) {
        if (dataTextKind == null) {
            return false;
        }

        // die MAD-Kennung holen
        String foreignTextKind = dataTextKind.getFieldValue(iPartsConst.FIELD_DA_DICT_TK_FOREIGN_TKIND);
        if (!foreignTextKind.isEmpty()) {
            // Überprüfen, ob die aktuelle MAD-Kennung in den erlaubten TextKindTypes ist
            DictTextKindTypes dictTextKindType = DictTextKindTypes.getType(foreignTextKind);
            if (dictTextKindType != DictTextKindTypes.UNKNOWN) {
                if (allowedTextKindTypes.contains(dictTextKindType)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTextKindEditAllowed(iPartsDataDictTextKind dataTextKind) {
        if (isTextKindNewAllowed(dataTextKind)) {
            String foreignTextKind = dataTextKind.getFieldValue(iPartsConst.FIELD_DA_DICT_TK_FOREIGN_TKIND);
            if (!foreignTextKind.isEmpty()) {
                // Überprüfen, ob es ein sprachneutraler Text ist und dieser editiert werden darf
                DictTextKindTypes dictTextKindType = DictTextKindTypes.getType(foreignTextKind);
                Set<DictTextKindTypes> isNeutralTextKindList = DictTextKindTypes.getNeutTextTypes();
                if (iPartsRight.EDIT_TEXT_DATA_ADMIN.checkRightInSession() || (isNeutralTextKindList == null) || !isNeutralTextKindList.contains(dictTextKindType)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void doEnableButtons() {
        List<List<EtkDataObject>> selected = dataGrid.getMultiSelection();
        // Anzeigen / Bearbeiten nur bei Einzelselektion
        boolean singleSelection = (selected.size() == 1);
        dataGrid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, singleSelection);
        iPartsDataDictTextKind dataTextKind = mainWindow.comboboxTextKind.getSelectedUserObject();

        // Neu und Löschen gibt es nur falls der User generell das Recht zum Bearbeiten hat
        if (editAllowed) {
            // Für Neu die ausgewählte Textart mit den erlaubten Textarten pro Recht abgleichen
            boolean isSelectedTextKindAllowed = isTextKindNewAllowed(dataTextKind);
            dataGrid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_NEW, isSelectedTextKindAllowed);
            dataGrid.changeToolbarButtonAndMenuTooltip(EditToolbarButtonAlias.EDIT_WORK, isTextKindEditAllowed(dataTextKind) ? "!!Bearbeiten"
                                                                                                                             : "!!Anzeigen");
            Set<DictTextKindTypes> isNeutralTextKindList = DictTextKindTypes.getNeutTextTypes();

            // Text kopieren ist nicht zulässig für sprachneutrale Texte
            menuCopyTextTo.setEnabled(isSelectedTextKindAllowed && singleSelection && ((isNeutralTextKindList == null)
                                                                                       || !isNeutralTextKindList.contains(dataTextKind.getForeignTextKindType())));

            // Für Löschen zusätzlich die Quelle auf iParts checken
            boolean deleteAllowed = singleSelection && isSelectedTextKindAllowed;
            if (deleteAllowed) {
                for (List<EtkDataObject> selectedObject : selected) {
                    iPartsDataDictMeta dataDictMeta = (iPartsDataDictMeta)selectedObject.get(0);
                    if (dataDictMeta != null) {
                        if (!dataDictMeta.getSource().startsWith(DictHelper.getIPartsSource())) {
                            deleteAllowed = false;
                            break;
                        }
                    }
                }
            }

            dataGrid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, deleteAllowed);
            dataGrid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_USAGE, singleSelection);
        }

        // Für das Anlegen der Fußnoten auf das Rechte "Fußnote anlegen", Textart == Fußnote, singleSelection und Status = freigegeben checken
        boolean enableAddFootnote = false;
        if (iPartsRight.CREATE_FOOTNOTE.checkRightInSession()) {
            if ((dataTextKind != null) && (dataTextKind.getForeignTextKindType() == DictTextKindTypes.FOOTNOTE)) {
                if (singleSelection) {
                    iPartsDataDictMeta dataDictMeta = (iPartsDataDictMeta)selected.get(0).get(0);
                    String state = dataDictMeta.getState();
                    if ((state != null) && state.equals(iPartsDictConst.DICT_STATUS_RELEASED)) {
                        enableAddFootnote = true;
                    }
                }
            }
        }
        menuAddFootnote.setEnabled(enableAddFootnote);
    }

    private List<iPartsDataDictMeta> getDataDictMetaInTranslation() {
        return getDataDictMetaInTranslation(dataGrid.getSelection());
    }

    private List<iPartsDataDictMeta> getDataDictMetaInTranslation(List<EtkDataObject> selectedObjects) {
        // Durchlaufe alle Objekte und falls ein iPartsDataDictMeta gerade im Übersetzungsdurchlauf ist, dann darf
        // der Text nicht editiert werden.
        List<iPartsDataDictMeta> dataDictMetaInTranslationList = new DwList<>();
        if (selectedObjects != null) {
            for (EtkDataObject dataObject : selectedObjects) {
                if (dataObject instanceof iPartsDataDictMeta) {
                    iPartsDataDictMeta dataDictMeta = (iPartsDataDictMeta)dataObject;
                    if (dataDictMeta.isInTranslationWorkflow()) {
                        dataDictMetaInTranslationList.add(dataDictMeta);
                    }
                }
            }
        }
        return dataDictMetaInTranslationList;
    }

    private void comboTextKindChanged(Event event) {
        iPartsDataDictTextKind dataTextKind = mainWindow.comboboxTextKind.getSelectedUserObject();
        Set<DictTextKindTypes> isNeutralTextKindList = DictTextKindTypes.getNeutTextTypes();
        boolean doReset = true;
        if (dataTextKind != null) {
            if (isNeutralTextKindList != null && isNeutralTextKindList.contains(dataTextKind.getForeignTextKindType())) {
                // Neutral Text angewählt
                lastSelectedLanguage = languageFormDatabase.getSelectedLanguage();
                languageFormDatabase.getCombobox().setEnabled(false);
                languageFormDatabase.setSelectedLanguage(Language.DE.getCode());
                doReset = false;
            }
            if (dataTextKind.getForeignTextKindType() == DictTextKindTypes.FOOTNOTE) {
                menuAddFootnote.setVisible(true);
            } else {
                menuAddFootnote.setVisible(false);
            }
            SessionKeyHelper.setDictionaryPreSelectIndex(dataTextKind.getAsId().getTextKindId());
        }

        if (doReset && (lastSelectedLanguage != null)) {
            languageFormDatabase.getCombobox().setEnabled(true);
            languageFormDatabase.setSelectedLanguage(lastSelectedLanguage);
            lastSelectedLanguage = null;
        }
        resetGrid(true);
        reloadGrid(null, false);
        doEnableButtons();
    }

    private void checkboxLongTextSearchChanged(Event event) {
        if (searchRunning) {
            stopSearch();
            showResultCount(getHitCount(), true);
        }
    }

    private boolean isSearchWithLongTexts() {
        return mainWindow.checkboxLongTextSearch.isSelected();
    }

    private boolean isSortSearchResults() {
        return mainWindow.checkboxSortSearchResults.isSelected();
    }

    /**
     * liefert die Anzahl der Treffer im Grid
     *
     * @return
     */
    private int getHitCount() {
        return dataGrid.getTable().getRowCount();
    }

    /**
     * Setzt das Grid zurück (z.B. nach Textartwechsel)
     *
     * @param resetSearchFields
     */
    private void resetGrid(boolean resetSearchFields) {
        stopSearch();
        if (resetSearchFields) {
            mainWindow.textfieldText.setText("");
            mainWindow.textfieldForeignId.setText("");
            mainWindow.textfieldEldasId.setText("");
            mainWindow.textfieldDialogId.setText("");
            // Setze die zusätzlichen Felder für Ergänzungstexte, sofern Textart "Ergänzungstexte ausgewählt wurde
            iPartsDataDictTextKind dataTextKind = mainWindow.comboboxTextKind.getSelectedUserObject();
            DictTextKindTypes foreignTextKindTypes = dataTextKind.getForeignTextKindType();
            setAddTextFieldsVisible(foreignTextKindTypes == DictTextKindTypes.ADD_TEXT);
            setSearchUsageToolbarButtonVisible(foreignTextKindTypes == DictTextKindTypes.ADD_TEXT ||
                                               foreignTextKindTypes == DictTextKindTypes.SAA_NAME ||
                                               foreignTextKindTypes == DictTextKindTypes.FOOTNOTE);
            iPartsDataDictTextKindUsageList usageList = iPartsDataDictTextKindUsageList.loadTextKindUsageList(getProject(), dataTextKind.getAsId());
            dictUsageSet.clear();
            for (iPartsDataDictTextKindUsage dataDictTextKindUsage : usageList) {
                dictUsageSet.add(dataDictTextKindUsage.getFieldValue(FIELD_DA_DICT_TKU_FELD));
            }
        }
        mainWindow.textfieldText.requestFocus();
        showResultCount(-1, false);
    }

    /**
     * Erneutes Laden des
     *
     * @param dictMetaId
     * @param search
     */
    private void reloadGrid(final iPartsDictMetaId dictMetaId, final boolean search) {
        final iPartsDataDictTextKind dataTextKind = mainWindow.comboboxTextKind.getSelectedUserObject();
        if (dataTextKind != null) {
            // Lösche den Inhalt des Grids
            dataGrid.clear();
            // ausgewählte Textart
            final iPartsDictTextKindId textKindId = new iPartsDictTextKindId(dataTextKind.getAsId().getTextKindId());
            if (search) {
                final iPartsDataDictMetaList metaList = new iPartsDataDictMetaList();
                searchRunning = true;
                Session.invokeThreadSafeInSession(() -> handleSearchButton(true));
                showResultCount(-1, false);
                final List<String> whereFields = new ArrayList<>();
                final List<String> whereValues = new ArrayList<>();
                final List<Boolean> searchCaseInsensitives = new ArrayList<>();
                String foreignID = mainWindow.textfieldForeignId.getTrimmedText();
                final String searchText = mainWindow.textfieldText.getTrimmedText();
                boolean isForeignIdEmpty = StrUtils.isEmpty(foreignID);
                boolean isTextEmpty = StrUtils.isEmpty(searchText);
                boolean areSearchCritsEmpty = isForeignIdEmpty && isTextEmpty;

                fillDefaultWhereFieldsAndValues(textKindId, whereFields, whereValues, searchCaseInsensitives);

                addSingleWhereFieldAndValue(foreignID, false, TABLE_DA_DICT_META, FIELD_DA_DICT_META_FOREIGNID, false, true,
                                            whereFields, whereValues, searchCaseInsensitives);
                addSingleWhereFieldAndValue(searchText, false, TABLE_SPRACHE, FIELD_S_BENENN, true, true,
                                            whereFields, whereValues, searchCaseInsensitives);

                if (dataTextKind.getForeignTextKindType() == DictTextKindTypes.ADD_TEXT) {
                    // ELDAS IDs
                    String id = mainWindow.textfieldEldasId.getTrimmedText();
                    areSearchCritsEmpty &= StrUtils.isEmpty(id);
                    addSingleWhereFieldAndValue(id, false, TABLE_DA_DICT_META, FIELD_DA_DICT_META_ELDASID, false, true,
                                                whereFields, whereValues, searchCaseInsensitives);
                    // DIALOG-IDs
                    id = mainWindow.textfieldDialogId.getTrimmedText();
                    areSearchCritsEmpty &= StrUtils.isEmpty(id);
                    addSingleWhereFieldAndValue(id, false, TABLE_DA_DICT_META, FIELD_DA_DICT_META_DIALOGID, false, true,
                                                whereFields, whereValues, searchCaseInsensitives);
                }
                final boolean isSourceEmpty = StrUtils.isEmpty(mainWindow.comboboxSource.getSelectedItem());
                areSearchCritsEmpty &= isSourceEmpty;
                if (!isSourceEmpty) {
                    addSingleWhereFieldAndValue(mainWindow.comboboxSource.getSelectedUserObject().getToken(), false, TABLE_DA_DICT_META,
                                                FIELD_DA_DICT_META_SOURCE, false, false, whereFields, whereValues, searchCaseInsensitives);
                } else {
                    if (!carAndVanInSession) {
                        // Ohne die Eigenschaften für PKW/Van dürfen Quelle IPARTS-MB und Connect nicht angezeigt werden
                        addSingleWhereFieldAndValue(iPartsImportDataOrigin.IPARTS_MB.getOrigin(), true, TABLE_DA_DICT_META,
                                                    FIELD_DA_DICT_META_SOURCE, false, false, whereFields, whereValues, searchCaseInsensitives);
                        addSingleWhereFieldAndValue(iPartsImportDataOrigin.CONNECT.getOrigin(), true, TABLE_DA_DICT_META,
                                                    FIELD_DA_DICT_META_SOURCE, false, false, whereFields, whereValues, searchCaseInsensitives);
                    }
                    if (!truckAndBusInSession) {
                        // Ohne die Eigenschaften für Truck/Bus darf Quelle IPARTS-TRUCK nicht angezeigt werden
                        addSingleWhereFieldAndValue(iPartsImportDataOrigin.IPARTS_TRUCK.getOrigin(), true, TABLE_DA_DICT_META,
                                                    FIELD_DA_DICT_META_SOURCE, false, false, whereFields, whereValues, searchCaseInsensitives);
                    }
                }
                final boolean doSearch = !areSearchCritsEmpty;
                final Set<iPartsDictMetaId> handledIdSet = new HashSet<>();
                searchThread = Session.startChildThreadInSession(new FrameworkRunnable() {
                    @Override
                    public void run(final FrameworkThread thread) {
                        if (doSearch) {
                            // Zuerst nur Suche in den kurzen Texten
                            searchInDB(false);

                            // Danach falls notwendig auch noch Suche in den langen Texten
                            if (isSearchWithLongTexts() && getProject().getConfig().getDBDescription().isSearchInLongTextsNeeded(searchText)) {
                                Session.invokeThreadSafeInSession(() -> mainWindow.labelResults.setText(TranslationHandler.translate("!!%1 Datensätze " +
                                                                                                                                     "in kurzen Texten gefunden; " +
                                                                                                                                     "Suche in langen Texten " +
                                                                                                                                     "läuft...",
                                                                                                                                     String.valueOf(getHitCount())))
                                );
                                searchInDB(true);
                            }
                        }
                        Session.invokeThreadSafeInSession(() -> {
                            if (!thread.wasCanceled()) {
                                adjustGrid(dictMetaId);
                                stopSearch();
                            }
                        });
                    }

                    private void searchInDB(boolean searchInLongTexts) {
                        // searchCaseInsensitives.toArray() liefert Boolean[] und nicht boolean[] -> müssen wir zu Fuß machen
                        boolean[] searchCaseInsensitivesArray = new boolean[searchCaseInsensitives.size()];
                        int index = 0;
                        for (Boolean searchCaseInsensitive : searchCaseInsensitives) {
                            searchCaseInsensitivesArray[index] = searchCaseInsensitive;
                            index++;
                        }

                        metaList.searchSortAndFillWithJoin(getProject(), getLanguage(), getSelectFields(),
                                                           ArrayUtil.toStringArray(whereFields),
                                                           ArrayUtil.toStringArray(whereValues),
                                                           false,
                                                           new String[]{ FIELD_S_BENENN },
                                                           isSortSearchResults() && !searchInLongTexts, searchCaseInsensitivesArray,
                                                           searchInLongTexts, true, false,
                                                           new EtkDataObjectList.FoundAttributesCallback() {
                                                               @Override
                                                               public boolean foundAttributes(DBDataObjectAttributes attributes) {
                                                                   if (!Thread.currentThread().isInterrupted()) {
                                                                       handleAttributes(attributes, handledIdSet);
                                                                   }
                                                                   return false;  //damit die Ergebnisse nicht zusätzlich in metaList gespeichert werden
                                                               }
                                                           }, true,
                                                           new EtkDataObjectList.JoinData(TABLE_SPRACHE,
                                                                                          new String[]{ FIELD_DA_DICT_META_TEXTID },
                                                                                          new String[]{ FIELD_S_TEXTID },
                                                                                          true,
                                                                                          false));
                    }
                });
            }
        } else {
            dataGrid.showNoResultsLabel(true);
        }
    }

    private void addSingleWhereFieldAndValue(String value, boolean isNotValue, String tablename, String fieldname, boolean searchCaseInsensitive,
                                             boolean searchLike, List<String> whereFields, List<String> whereValues,
                                             List<Boolean> searchCaseInsensitives) {
        if (StrUtils.isEmpty(tablename, fieldname)) {
            return;
        }
        if (!StrUtils.isEmpty(value)) {
            whereFields.add(TableAndFieldName.make(tablename, fieldname));
            if (searchLike) {
                value = StrUtils.addCharacterIfLastCharacterIsNot(value, '*');
            }
            if (isNotValue) {
                value = EtkDataObjectList.getNotWhereValue(value);
            }
            whereValues.add(value);
            searchCaseInsensitives.add(searchCaseInsensitive);
        }
    }

    private void fillDefaultWhereFieldsAndValues(iPartsDictTextKindId textKindId, List<String> whereFields, List<String> whereValues,
                                                 List<Boolean> searchCaseInsensitives) {
        whereFields.add(TableAndFieldName.make(TABLE_DA_DICT_META, FIELD_DA_DICT_META_TXTKIND_ID));
        whereValues.add(textKindId.getTextKindId());
        searchCaseInsensitives.add(false);
        whereFields.add(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_SPRACH));
        whereValues.add(getLanguage());
        searchCaseInsensitives.add(false);
    }

    private void adjustGrid(iPartsDictMetaId dictMetaId) {
        int rowCount = getHitCount();
        dataGrid.showNoResultsLabel(rowCount == 0);
        showResultCount(rowCount, false);
        if (rowCount > 0) {
            int sortColumn = dataGrid.getTable().getSortColumn();
            sortColumn = (sortColumn == -1) ? 0 : sortColumn;
            boolean sortAscending = dataGrid.getTable().isSortAscending();
            if (sortColumn >= 0) {
                dataGrid.getTable().sortRowsAccordingToColumn(sortColumn, sortAscending);
            }
            dataGrid.scrollToIdIfExists(dictMetaId);
        }
    }

    private void handleSearchButton(boolean searchRunning) {
        if (searchRunning) {
            mainWindow.kitLight.setVisible(true);
            mainWindow.buttonForStartAndCancel.setText("!!Suche abbrechen");
        } else {
            mainWindow.buttonForStartAndCancel.setText("!!Suche starten");
            if (mainWindow.kitLight.isVisible()) {
                mainWindow.kitLight.setVisible(false);
            }
        }
    }

    private void handleAttributes(final DBDataObjectAttributes attributes, final Set<iPartsDictMetaId> handledIdSet) {
        Session.invokeThreadSafeInSession(() -> {
            if (withUsageFilter) {
                if (!dictUsageSet.contains(attributes.getFieldValue(FIELD_S_FELD))) {
                    return;
                }
            }

            // Quelle filtern je nach Eigenschaften des Benutzers der aktuellen Session; wird eigentlich schon über
            // das SQL-Statement gemacht, aber sicher ist sicher...
            if (!checkSourceForSession(attributes.getFieldValue(FIELD_DA_DICT_META_SOURCE))) {
                return;
            }

            // Doppelte Suchergebnisse aufgrund der ausmultiplizierten Einträge in der Sprachtabelle vermeiden
            iPartsDictMetaId dictMetaId = new iPartsDictMetaId(attributes.getFieldValue(FIELD_DA_DICT_META_TXTKIND_ID),
                                                               attributes.getFieldValue(FIELD_DA_DICT_META_TEXTID));
            if (handledIdSet.contains(dictMetaId)) {
                return;
            }

            String longText = attributes.getFieldValue(FIELD_S_BENENN_LANG);
            String text = longText.isEmpty() ? attributes.getFieldValue(FIELD_S_BENENN) : longText;
            //sollte es keinen Text geben, so zeige die Text-ID
            if (text.isEmpty()) {
                text = attributes.getFieldValue(FIELD_DA_DICT_META_TEXTID);
            }
            attributes.addField(PLACEHOLDER_TEXT_FIELD, text, true, DBActionOrigin.FROM_DB);
            iPartsDataDictMeta dictMeta = new iPartsDataDictMeta(getProject(), dictMetaId);
            dictMeta.setAttributes(attributes, DBActionOrigin.FROM_DB);
            dataGrid.addObjectToGrid(dictMeta);
            handledIdSet.add(dictMetaId);

            if (getHitCount() >= MAX_HITS_PER_SEARCH) {
                adjustGrid(null);
                startOrStopSearch();
            }
        });
    }

    /**
     * Macht alle Felder (un-)sichtbar, die nur für die Textart "Ergänzungstexte MAD" relevant sind.
     *
     * @param visible
     */
    private void setAddTextFieldsVisible(boolean visible) {
        dataGrid.setColumnVisible(visible, TableAndFieldName.make(iPartsConst.TABLE_DA_DICT_META, iPartsConst.FIELD_DA_DICT_META_DIALOGID),
                                  TableAndFieldName.make(iPartsConst.TABLE_DA_DICT_META, iPartsConst.FIELD_DA_DICT_META_ELDASID));
        mainWindow.labelEldasId.setVisible(visible);
        mainWindow.labelDialogId.setVisible(visible);
        mainWindow.textfieldEldasId.setVisible(visible);
        mainWindow.textfieldDialogId.setVisible(visible);
    }

    private void setSearchUsageToolbarButtonVisible(boolean visible) {
        if (visible) {
            dataGrid.showToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_USAGE);
        } else {
            dataGrid.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_USAGE);
        }
    }

    private void showResultCount(int count, boolean isCanceled) {
        if (searchRunning) {
            if (count >= 0) {
                mainWindow.labelResults.setText(TranslationHandler.translate("!!%d Datensätze gefunden", String.valueOf(count)));
            } else {
                mainWindow.labelResults.setText(TranslationHandler.translate("!!Suche läuft..."));
            }
            return;
        }
        if (count >= 0) {
            if (isCanceled) {
                mainWindow.labelResults.setText(TranslationHandler.translate("!!%d Datensätze gefunden (Suche abgebrochen)", String.valueOf(count)));
            } else {
                mainWindow.labelResults.setText(TranslationHandler.translate("!!%d Datensätze gefunden", String.valueOf(count)));
            }
        } else {
            mainWindow.labelResults.setText("");
        }
    }


    private void doNewTextId(Event event) {
        if (searchRunning) {
            stopSearch();
            showResultCount(getHitCount(), true);
        }
        iPartsDataDictTextKind dataTextKind = mainWindow.comboboxTextKind.getSelectedUserObject();
        iPartsDictTextKindId textKindId = null;
        if (dataTextKind != null) {
            textKindId = dataTextKind.getAsId();
        }
        iPartsDictMetaId dictMetaId = DictCreateTextIdForm.createNewTextId(getConnector(), this, allowedTextKindTypes, textKindId);
        if (dictMetaId != null) {
            reloadGrid(dictMetaId, false);
            doEnableButtons();
        }
    }

    private boolean isTableHeaderDblClick(Event event) {
        //Doppelklick auf TableHeader?
        if (event.hasParameter(Event.EVENT_PARAMETER_CELL_POS)) {
            DWPoint p = (DWPoint)event.getParameter(Event.EVENT_PARAMETER_CELL_POS);
            if ((p.getY() < 0) && (p.getX() >= 0) && p.getX() < dataGrid.getTable().getColCount()) {
                return true;
            }
        }
        return false;
    }


    private void doEditTextId(Event event) {
        iPartsDataDictMeta dataDictMeta = getSingleSelectedObject();
        if (dataDictMeta != null) {
            iPartsDataDictTextKind dataTextKind = mainWindow.comboboxTextKind.getSelectedUserObject();
            iPartsDictTextKindId textKindId = null;
            if (dataTextKind != null) {
                textKindId = dataTextKind.getAsId();
            }

            // Prüfen, ob der Benutzer die gewählte Textart bearbeiten darf
            boolean isEditForTextKindAllowed = isTextKindEditAllowed(dataTextKind);
            Set<DictTextKindTypes> currentAllowedTextKinds = null;
            Set<DictTextKindTypes> currentAllowedTextKindsForEditMigration = null;
            if (isEditForTextKindAllowed) {
                currentAllowedTextKinds = new HashSet<>(this.allowedTextKindTypes);
                currentAllowedTextKindsForEditMigration = this.allowedTextKindTypesForEditMigration;
            }

            boolean dictMetaEditAllowed = this.editAllowed && isEditForTextKindAllowed;
            List<iPartsDataDictMeta> dataDictMetaInTranslationList = getDataDictMetaInTranslation();
            if (dictMetaEditAllowed && !dataDictMetaInTranslationList.isEmpty()) {
                MessageDialog.show("!!Bearbeiten des Textes nicht möglich, da dieser sich gerade im Übersetzungsprozess befindet.",
                                   "!!Text bearbeiten");
                dictMetaEditAllowed = false;
            }

            iPartsDictMetaId dictMetaId = DictCreateTextIdForm.editNewTextId(getConnector(), this,
                                                                             currentAllowedTextKinds, currentAllowedTextKindsForEditMigration,
                                                                             textKindId, dataDictMeta, dictMetaEditAllowed,
                                                                             dictMetaEditAllowed);
            if (dictMetaId != null) {
                reloadGrid(dictMetaId, true);
                doEnableButtons();
            }
        }
    }

    private void doGridHeaderDoubleClick(Event event) {
        //Doppelklick auf TableHeader
        dataGrid.getTable().clearSort();
    }


    private void doDeleteTextId(Event event) {
        iPartsDataDictTextKind dataTextKind = mainWindow.comboboxTextKind.getSelectedUserObject();
        iPartsDataDictMeta dataDictMeta = getSingleSelectedObject();
        if ((dataDictMeta != null) && (dataTextKind != null)) {
            if (!isInUsage(dataTextKind, dataDictMeta)) {
                String msg = "!!Wollen Sie den selektierten Text wirklich löschen?";
                List<iPartsDataDictMeta> dataDictMetaInTranslationList = getDataDictMetaInTranslation();
                if (!dataDictMetaInTranslationList.isEmpty()) {
                    msg = TranslationHandler.translate(msg) + "\n\n" +
                          TranslationHandler.translate("!!Der Text befindet sich gerade im Übersetzungsprozess.");
                }

                if (MessageDialog.show(msg, "!!Löschen", MessageDialogIcon.CONFIRMATION,
                                       MessageDialogButtons.YES, MessageDialogButtons.NO) == ModalResult.YES) {
                    // vor dem Löschen laden für Cluster-Event
                    EtkMultiSprache multiLang = getProject().getDbLayer().getLanguagesTextsByTextId(dataDictMeta.getTextId());
                    if (multiLang == null) {
                        multiLang = new EtkMultiSprache();
                    }

                    if (deleteTextId(dataDictMeta)) {
                        // Eintrag aus Lexikon-Cache löschen
                        DictClusterEventHelper.fireDeleteDictionaryClusterEvent(dataDictMeta, dataTextKind, multiLang);
                        reloadGrid(null, true);
                    }
                }
            } else {
                MessageDialog.showWarning("!!Der selektierte Text kann nicht gelöscht werden, da er noch verwendet wird.");
            }
        }
    }

    private void doCopySelectedText() {
        iPartsDataDictMeta dataDictMeta = getSingleSelectedObject();
        if (dataDictMeta != null) {
            iPartsDataDictTextKind dataTextKind = mainWindow.comboboxTextKind.getSelectedUserObject();
            iPartsDictTextKindId textKindId = null;
            if (dataTextKind != null) {
                textKindId = dataTextKind.getAsId();
            }
            boolean isEditForTextKindAllowed = isTextKindEditAllowed(dataTextKind);
            Set<DictTextKindTypes> currentAllowedTextKinds = null;
            if (isEditForTextKindAllowed) {
                currentAllowedTextKinds = new HashSet<>(this.allowedTextKindTypes);

                // Textart "Sprachneutraler Text" darf nie Ziel vom Kopieren sein und auch nie Quelle
                currentAllowedTextKinds.remove(DictTextKindTypes.NEUTRAL_TEXT);
                if (dataTextKind.getForeignTextKindType() == DictTextKindTypes.NEUTRAL_TEXT) {
                    return; // Wird eigentlich schon in doEnableButtons() abgefangen
                }
            }

            boolean dictMetaEditAllowed = this.editAllowed && isEditForTextKindAllowed;
            List<iPartsDataDictMeta> dataDictMetaInTranslationList = getDataDictMetaInTranslation();
            if (dictMetaEditAllowed && !dataDictMetaInTranslationList.isEmpty()) {
                MessageDialog.show("!!Kopieren des Textes nicht möglich, da dieser sich gerade im Übersetzungsprozess befindet.",
                                   "!!Text kopieren");
                return;
            }

            iPartsDataDictMeta futureDataDictMeta = createDictMetaForCopy(dataDictMeta);

            DictCopyTextForm.copyDictText(getConnector(), this, currentAllowedTextKinds, textKindId, futureDataDictMeta,
                                          dictMetaEditAllowed, dictMetaEditAllowed, COPY_CREATE_EXACT_COPY);
        }
    }

    private void doCreateFootNote() {
        iPartsDataDictMeta dataDictMeta = getSingleSelectedObject();
        if (dataDictMeta != null) {
            iPartsDataFootNoteContent footNoteContent =
                    EditUserControlForCreateFootnote.showEditUserControlsForCreateFootnote(getConnector(), this, dataDictMeta);
            if (footNoteContent != null) {
                String footNoteNumber = footNoteContent.getAsId().getFootNoteId();
                //FN_ID = DFN_NAME = DFNC_FNID = eingegebene FN-Nummer
                iPartsDataFootNote footNote = new iPartsDataFootNote(getProject(), new iPartsFootNoteId(footNoteNumber));
                footNote.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                footNote.setFieldValue(FIELD_DFN_NAME, footNoteNumber, DBActionOrigin.FROM_EDIT);
                //DFN_STANDARD = '0', falls DFN_ID 6-stellig, sonst '1'
                boolean isStandardFootNote = footNoteNumber.length() == 3;
                footNote.setFieldValueAsBoolean(FIELD_DFN_STANDARD, isStandardFootNote, DBActionOrigin.FROM_EDIT);

                getDbLayer().startTransaction();
                try {
                    // Fußnote direkt in DB speichern
                    footNoteContent.saveToDB();
                    footNote.saveToDB();
                    getDbLayer().commit();
                } catch (Exception e) {
                    getDbLayer().rollback();
                    Logger.getLogger().handleRuntimeException(e);
                }

                // Cache für die Standard-Fußnoten neu aufbauen
                if (isStandardFootNote) {
                    iPartsStandardFootNotesCache.clearCache();
                    iPartsStandardFootNotesCache.warmUpCache(getProject());
                }
            }
        }
    }

    /**
     * Verwendung des selektierten Textes suchen
     */
    private void doSearchUsage() {
        iPartsDataDictTextKind dataTextKind = mainWindow.comboboxTextKind.getSelectedUserObject();
        DictTextKindTypes foreignTextKindTypes = dataTextKind.getForeignTextKindType();
        EtkDataObject searchValue = getSingleSelectedObject();
        DictDataSearchAddAndSAATextUsageForm.showUsage(foreignTextKindTypes.getTextKindName(), searchValue, getConnector(),
                                                       this);
    }

    private iPartsDataDictMeta createDictMetaForCopy(iPartsDataDictMeta currentDataDictMeta) {
        iPartsDataDictMeta futureDataDictMeta;
        if (COPY_CREATE_EXACT_COPY) {
            futureDataDictMeta = currentDataDictMeta.cloneMe(getProject());
            futureDataDictMeta.setSource(iPartsImportDataOrigin.IPARTS.getOrigin(), DBActionOrigin.FROM_EDIT);
        } else {
            EtkMultiSprache multi = new EtkMultiSprache(DictHelper.buildDictTextId(StrUtils.makeGUID()));
            multi.setLanguagesAndTexts(currentDataDictMeta.getMultiLang().getLanguagesAndTextsModifiable());

            iPartsDictMetaId futureDictMetaId = new iPartsDictMetaId(currentDataDictMeta.getAsId().getTextKindId(), multi.getTextId());
            futureDataDictMeta = new iPartsDataDictMeta(getProject(), futureDictMetaId);
            DictImportTextIdHelper.initDataDictMeta(futureDataDictMeta, currentDataDictMeta.getForeignId(),
                                                    iPartsImportDataOrigin.IPARTS.getOrigin(), multi,
                                                    currentDataDictMeta.getELDASId(), currentDataDictMeta.getDIALOGId());
        }
        return futureDataDictMeta;
    }

    private boolean isInUsage(iPartsDataDictTextKind dataTextKind, iPartsDataDictMeta dataDictMeta) {
        return dataTextKind.getUsages().isTextIdUsed(dataDictMeta.getTextId());
    }

    private boolean deleteTextId(iPartsDataDictMeta dataDictMeta) {
        if (dataDictMeta != null) {
            getDbLayer().startTransaction();
            try {
                // Metadaten für die einzelnen Sprachen explizit laden, damit diese auch im ChangeSet sauber als gelöscht
                // dokumentiert werden
                dataDictMeta.prepareLanguagesForDelete();

                // Löschen im ChangeSet dokumentieren und direkt in der DB ausführen
                boolean deleted = iPartsRevisionChangeSet.deleteDataObjectWithChangeSet(getProject(), dataDictMeta, iPartsChangeSetSource.DICTIONARY);
                if (deleted) {
                    dataDictMeta.deleteFromDB();
                    getDbLayer().commit();
                    return true;
                } else {
                    getDbLayer().rollback();
                }
            } catch (Exception e) {
                getDbLayer().rollback();
                Logger.getLogger().handleRuntimeException(e);
            }
        }

        return false;
    }

    private iPartsDataDictMeta getSingleSelectedObject() {
        List<EtkDataObject> selectedList = dataGrid.getSelection();
        if ((selectedList != null) && !selectedList.isEmpty()) {
            if (searchRunning) {
                stopSearch();
                showResultCount(getHitCount(), true);
            }
            return (iPartsDataDictMeta)selectedList.get(0);
        }
        return null;
    }

    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    private de.docware.framework.modules.gui.controls.menu.GuiContextMenu contextmenuTextKind;

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelTop;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictTextKind> comboboxTextKind;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelResults;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiCheckbox checkboxLongTextSearch;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiCheckbox checkboxSortSearchResults;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelContainer;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelText;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfieldText;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelForeignId;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfieldForeignId;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelSource;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<EnumEntry> comboboxSource;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelEldasId;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfieldEldasId;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelDialogId;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfieldDialogId;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelForCancel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiProgressBar kitLight;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton buttonForStartAndCancel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelLanguage;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelLanguage;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<Object> comboBoxLanguage;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelGrid;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            contextmenuTextKind = new de.docware.framework.modules.gui.controls.menu.GuiContextMenu();
            contextmenuTextKind.setName("contextmenuTextKind");
            contextmenuTextKind.__internal_setGenerationDpi(96);
            contextmenuTextKind.registerTranslationHandler(translationHandler);
            contextmenuTextKind.setScaleForResolution(true);
            contextmenuTextKind.setMinimumWidth(10);
            contextmenuTextKind.setMinimumHeight(10);
            contextmenuTextKind.setMenuName("contextmenuTextKind");
            contextmenuTextKind.setParentControl(this);
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
            title = new de.docware.framework.modules.gui.controls.GuiTitle();
            title.setName("title");
            title.__internal_setGenerationDpi(96);
            title.registerTranslationHandler(translationHandler);
            title.setScaleForResolution(true);
            title.setMinimumWidth(10);
            title.setMinimumHeight(50);
            title.setTitle("...");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleConstraints.setPosition("north");
            title.setConstraints(titleConstraints);
            this.addChild(title);
            panelMain = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelMain.setName("panelMain");
            panelMain.__internal_setGenerationDpi(96);
            panelMain.registerTranslationHandler(translationHandler);
            panelMain.setScaleForResolution(true);
            panelMain.setMinimumWidth(10);
            panelMain.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelMain.setLayout(panelMainLayout);
            panelTop = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelTop.setName("panelTop");
            panelTop.__internal_setGenerationDpi(96);
            panelTop.registerTranslationHandler(translationHandler);
            panelTop.setScaleForResolution(true);
            panelTop.setMinimumWidth(10);
            panelTop.setMinimumHeight(40);
            panelTop.setTitle("!!Textart Auswahl");
            de.docware.framework.modules.gui.layout.LayoutGridBag panelTopLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelTop.setLayout(panelTopLayout);
            comboboxTextKind = new de.docware.framework.modules.gui.controls.GuiComboBox<>();
            comboboxTextKind.setName("comboboxTextKind");
            comboboxTextKind.__internal_setGenerationDpi(96);
            comboboxTextKind.registerTranslationHandler(translationHandler);
            comboboxTextKind.setScaleForResolution(true);
            comboboxTextKind.setMinimumWidth(10);
            comboboxTextKind.setMinimumHeight(10);
            comboboxTextKind.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    comboTextKindChanged(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag comboboxTextKindConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 2, 1, 100.0, 100.0, "c", "b", 4, 4, 4, 4);
            comboboxTextKind.setConstraints(comboboxTextKindConstraints);
            panelTop.addChild(comboboxTextKind);
            labelResults = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelResults.setName("labelResults");
            labelResults.__internal_setGenerationDpi(96);
            labelResults.registerTranslationHandler(translationHandler);
            labelResults.setScaleForResolution(true);
            labelResults.setMinimumWidth(10);
            labelResults.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelResultsConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 2, 1, 0.0, 0.0, "c", "h", 4, 4, 4, 4);
            labelResults.setConstraints(labelResultsConstraints);
            panelTop.addChild(labelResults);
            checkboxLongTextSearch = new de.docware.framework.modules.gui.controls.GuiCheckbox();
            checkboxLongTextSearch.setName("checkboxLongTextSearch");
            checkboxLongTextSearch.__internal_setGenerationDpi(96);
            checkboxLongTextSearch.registerTranslationHandler(translationHandler);
            checkboxLongTextSearch.setScaleForResolution(true);
            checkboxLongTextSearch.setMinimumWidth(10);
            checkboxLongTextSearch.setMinimumHeight(10);
            checkboxLongTextSearch.setText("!!Suche in langen Texten");
            checkboxLongTextSearch.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    checkboxLongTextSearchChanged(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag checkboxLongTextSearchConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 0.0, 0.0, "w", "n", 4, 4, 4, 4);
            checkboxLongTextSearch.setConstraints(checkboxLongTextSearchConstraints);
            panelTop.addChild(checkboxLongTextSearch);
            checkboxSortSearchResults = new de.docware.framework.modules.gui.controls.GuiCheckbox();
            checkboxSortSearchResults.setName("checkboxSortSearchResults");
            checkboxSortSearchResults.__internal_setGenerationDpi(96);
            checkboxSortSearchResults.registerTranslationHandler(translationHandler);
            checkboxSortSearchResults.setScaleForResolution(true);
            checkboxSortSearchResults.setMinimumWidth(10);
            checkboxSortSearchResults.setMinimumHeight(10);
            checkboxSortSearchResults.setText("!!Suchergebnisse sortieren");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag checkboxSortSearchResultsConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 2, 1, 1, 0.0, 0.0, "w", "n", 4, 4, 4, 4);
            checkboxSortSearchResults.setConstraints(checkboxSortSearchResultsConstraints);
            panelTop.addChild(checkboxSortSearchResults);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelTopConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "c", "h", 0, 0, 0, 0);
            panelTop.setConstraints(panelTopConstraints);
            panelMain.addChild(panelTop);
            panelContainer = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelContainer.setName("panelContainer");
            panelContainer.__internal_setGenerationDpi(96);
            panelContainer.registerTranslationHandler(translationHandler);
            panelContainer.setScaleForResolution(true);
            panelContainer.setMinimumWidth(10);
            panelContainer.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelContainerLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelContainer.setLayout(panelContainerLayout);
            labelText = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelText.setName("labelText");
            labelText.__internal_setGenerationDpi(96);
            labelText.registerTranslationHandler(translationHandler);
            labelText.setScaleForResolution(true);
            labelText.setMinimumWidth(10);
            labelText.setMinimumHeight(10);
            labelText.setText("!!Text");
            labelText.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelTextConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "e", "n", 8, 8, 4, 4);
            labelText.setConstraints(labelTextConstraints);
            panelContainer.addChild(labelText);
            textfieldText = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfieldText.setName("textfieldText");
            textfieldText.__internal_setGenerationDpi(96);
            textfieldText.registerTranslationHandler(translationHandler);
            textfieldText.setScaleForResolution(true);
            textfieldText.setMinimumWidth(200);
            textfieldText.setMinimumHeight(10);
            textfieldText.setMaximumWidth(600);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfieldTextConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, "c", "n", 8, 4, 4, 4);
            textfieldText.setConstraints(textfieldTextConstraints);
            panelContainer.addChild(textfieldText);
            labelForeignId = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelForeignId.setName("labelForeignId");
            labelForeignId.__internal_setGenerationDpi(96);
            labelForeignId.registerTranslationHandler(translationHandler);
            labelForeignId.setScaleForResolution(true);
            labelForeignId.setMinimumWidth(10);
            labelForeignId.setMinimumHeight(10);
            labelForeignId.setText("!!Fremd-Id");
            labelForeignId.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelForeignIdConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(2, 0, 1, 1, 0.0, 0.0, "e", "n", 8, 4, 4, 4);
            labelForeignId.setConstraints(labelForeignIdConstraints);
            panelContainer.addChild(labelForeignId);
            textfieldForeignId = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfieldForeignId.setName("textfieldForeignId");
            textfieldForeignId.__internal_setGenerationDpi(96);
            textfieldForeignId.registerTranslationHandler(translationHandler);
            textfieldForeignId.setScaleForResolution(true);
            textfieldForeignId.setMinimumWidth(200);
            textfieldForeignId.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfieldForeignIdConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(3, 0, 1, 1, 0.0, 0.0, "c", "n", 8, 4, 4, 4);
            textfieldForeignId.setConstraints(textfieldForeignIdConstraints);
            panelContainer.addChild(textfieldForeignId);
            labelSource = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelSource.setName("labelSource");
            labelSource.__internal_setGenerationDpi(96);
            labelSource.registerTranslationHandler(translationHandler);
            labelSource.setScaleForResolution(true);
            labelSource.setMinimumWidth(10);
            labelSource.setMinimumHeight(10);
            labelSource.setText("!!Fremdquelle");
            labelSource.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelSourceConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(4, 0, 1, 1, 0.0, 0.0, "e", "n", 8, 4, 4, 4);
            labelSource.setConstraints(labelSourceConstraints);
            panelContainer.addChild(labelSource);
            comboboxSource = new de.docware.framework.modules.gui.controls.GuiComboBox<>();
            comboboxSource.setName("comboboxSource");
            comboboxSource.__internal_setGenerationDpi(96);
            comboboxSource.registerTranslationHandler(translationHandler);
            comboboxSource.setScaleForResolution(true);
            comboboxSource.setMinimumWidth(10);
            comboboxSource.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag comboboxSourceConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(5, 0, 1, 1, 0.0, 0.0, "c", "n", 8, 4, 4, 4);
            comboboxSource.setConstraints(comboboxSourceConstraints);
            panelContainer.addChild(comboboxSource);
            labelEldasId = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelEldasId.setName("labelEldasId");
            labelEldasId.__internal_setGenerationDpi(96);
            labelEldasId.registerTranslationHandler(translationHandler);
            labelEldasId.setScaleForResolution(true);
            labelEldasId.setMinimumWidth(10);
            labelEldasId.setMinimumHeight(10);
            labelEldasId.setText("!!ELDAS-ID");
            labelEldasId.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelEldasIdConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "e", "n", 4, 8, 8, 4);
            labelEldasId.setConstraints(labelEldasIdConstraints);
            panelContainer.addChild(labelEldasId);
            textfieldEldasId = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfieldEldasId.setName("textfieldEldasId");
            textfieldEldasId.__internal_setGenerationDpi(96);
            textfieldEldasId.registerTranslationHandler(translationHandler);
            textfieldEldasId.setScaleForResolution(true);
            textfieldEldasId.setMinimumWidth(200);
            textfieldEldasId.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfieldEldasIdConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 1, 1, 1, 0.0, 0.0, "c", "n", 4, 4, 8, 4);
            textfieldEldasId.setConstraints(textfieldEldasIdConstraints);
            panelContainer.addChild(textfieldEldasId);
            labelDialogId = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelDialogId.setName("labelDialogId");
            labelDialogId.__internal_setGenerationDpi(96);
            labelDialogId.registerTranslationHandler(translationHandler);
            labelDialogId.setScaleForResolution(true);
            labelDialogId.setMinimumWidth(10);
            labelDialogId.setMinimumHeight(10);
            labelDialogId.setText("!!DIALOG-ID");
            labelDialogId.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelDialogIdConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(2, 1, 1, 1, 0.0, 0.0, "e", "n", 4, 4, 8, 4);
            labelDialogId.setConstraints(labelDialogIdConstraints);
            panelContainer.addChild(labelDialogId);
            textfieldDialogId = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfieldDialogId.setName("textfieldDialogId");
            textfieldDialogId.__internal_setGenerationDpi(96);
            textfieldDialogId.registerTranslationHandler(translationHandler);
            textfieldDialogId.setScaleForResolution(true);
            textfieldDialogId.setMinimumWidth(200);
            textfieldDialogId.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfieldDialogIdConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(3, 1, 1, 1, 0.0, 0.0, "c", "n", 4, 4, 8, 4);
            textfieldDialogId.setConstraints(textfieldDialogIdConstraints);
            panelContainer.addChild(textfieldDialogId);
            panelForCancel = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelForCancel.setName("panelForCancel");
            panelForCancel.__internal_setGenerationDpi(96);
            panelForCancel.registerTranslationHandler(translationHandler);
            panelForCancel.setScaleForResolution(true);
            panelForCancel.setMinimumWidth(10);
            panelForCancel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelForCancelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelForCancel.setLayout(panelForCancelLayout);
            kitLight = new de.docware.framework.modules.gui.controls.GuiProgressBar();
            kitLight.setName("kitLight");
            kitLight.__internal_setGenerationDpi(96);
            kitLight.registerTranslationHandler(translationHandler);
            kitLight.setScaleForResolution(true);
            kitLight.setMinimumWidth(200);
            kitLight.setMinimumHeight(10);
            kitLight.setMaximumWidth(200);
            kitLight.setVisible(false);
            kitLight.setMarquee(true);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag kitLightConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "e", "h", 8, 8, 4, 4);
            kitLight.setConstraints(kitLightConstraints);
            panelForCancel.addChild(kitLight);
            buttonForStartAndCancel = new de.docware.framework.modules.gui.controls.GuiButton();
            buttonForStartAndCancel.setName("buttonForStartAndCancel");
            buttonForStartAndCancel.__internal_setGenerationDpi(96);
            buttonForStartAndCancel.registerTranslationHandler(translationHandler);
            buttonForStartAndCancel.setScaleForResolution(true);
            buttonForStartAndCancel.setMinimumWidth(100);
            buttonForStartAndCancel.setMinimumHeight(10);
            buttonForStartAndCancel.setMnemonicEnabled(true);
            buttonForStartAndCancel.setText("!!Suche starten");
            buttonForStartAndCancel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onSearchCancelButtonClick(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag buttonForStartAndCancelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, "e", "h", 8, 4, 4, 8);
            buttonForStartAndCancel.setConstraints(buttonForStartAndCancelConstraints);
            panelForCancel.addChild(buttonForStartAndCancel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelForCancelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(7, 0, 1, 1, 100.0, 0.0, "e", "n", 0, 0, 0, 0);
            panelForCancel.setConstraints(panelForCancelConstraints);
            panelContainer.addChild(panelForCancel);
            panelLanguage = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelLanguage.setName("panelLanguage");
            panelLanguage.__internal_setGenerationDpi(96);
            panelLanguage.registerTranslationHandler(translationHandler);
            panelLanguage.setScaleForResolution(true);
            panelLanguage.setMinimumWidth(10);
            panelLanguage.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelLanguageLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelLanguage.setLayout(panelLanguageLayout);
            labelLanguage = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelLanguage.setName("labelLanguage");
            labelLanguage.__internal_setGenerationDpi(96);
            labelLanguage.registerTranslationHandler(translationHandler);
            labelLanguage.setScaleForResolution(true);
            labelLanguage.setMinimumWidth(10);
            labelLanguage.setMinimumHeight(10);
            labelLanguage.setVisible(false);
            labelLanguage.setText("!!Suchsprache");
            labelLanguage.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelLanguageConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "e", "n", 8, 4, 4, 4);
            labelLanguage.setConstraints(labelLanguageConstraints);
            panelLanguage.addChild(labelLanguage);
            comboBoxLanguage = new de.docware.framework.modules.gui.controls.GuiComboBox<>();
            comboBoxLanguage.setName("comboBoxLanguage");
            comboBoxLanguage.__internal_setGenerationDpi(96);
            comboBoxLanguage.registerTranslationHandler(translationHandler);
            comboBoxLanguage.setScaleForResolution(true);
            comboBoxLanguage.setMinimumWidth(10);
            comboBoxLanguage.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag comboBoxLanguageConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, "c", "h", 12, 4, 4, 4);
            comboBoxLanguage.setConstraints(comboBoxLanguageConstraints);
            panelLanguage.addChild(comboBoxLanguage);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelLanguageConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(6, 0, 1, 1, 0.0, 0.0, "c", "n", 0, 0, 0, 0);
            panelLanguage.setConstraints(panelLanguageConstraints);
            panelContainer.addChild(panelLanguage);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelContainerConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "c", "h", 4, 4, 8, 4);
            panelContainer.setConstraints(panelContainerConstraints);
            panelMain.addChild(panelContainer);
            panelGrid = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelGrid.setName("panelGrid");
            panelGrid.__internal_setGenerationDpi(96);
            panelGrid.registerTranslationHandler(translationHandler);
            panelGrid.setScaleForResolution(true);
            panelGrid.setMinimumWidth(10);
            panelGrid.setMinimumHeight(10);
            panelGrid.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panelGridLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelGrid.setLayout(panelGridLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelGridConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 100.0, 100.0, "c", "b", 0, 0, 0, 0);
            panelGrid.setConstraints(panelGridConstraints);
            panelMain.addChild(panelGrid);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMainConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMain.setConstraints(panelMainConstraints);
            this.addChild(panelMain);
            buttonpanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonpanel.setName("buttonpanel");
            buttonpanel.__internal_setGenerationDpi(96);
            buttonpanel.registerTranslationHandler(translationHandler);
            buttonpanel.setScaleForResolution(true);
            buttonpanel.setMinimumWidth(10);
            buttonpanel.setMinimumHeight(10);
            buttonpanel.setDialogStyle(de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel.DialogStyle.CLOSE);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder buttonpanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            buttonpanelConstraints.setPosition("south");
            buttonpanel.setConstraints(buttonpanelConstraints);
            this.addChild(buttonpanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}