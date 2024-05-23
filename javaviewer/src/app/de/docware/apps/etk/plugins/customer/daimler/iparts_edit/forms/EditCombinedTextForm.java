/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.forms.common.components.MenuRunnable;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMeta;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMetaList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictTextKind;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictTextKindList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsCombTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataCombText;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataCombTextList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindEPCTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsCombTextHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.dictionary.DictMultiLangEditForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiMultiLangEdit;
import de.docware.framework.modules.gui.controls.GuiSeparator;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.misc.DWOrientation;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.cache.ObjectInstanceStrongLRUList;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Formular für Kombinierte Texte (Hinzufügen, Verschieben, Löschen)
 */
public class EditCombinedTextForm extends AbstractJavaViewerForm implements iPartsConst {

    private static final ObjectInstanceStrongLRUList<Object, Map<String, TextKindType>> textKindsCache =
            new ObjectInstanceStrongLRUList<>(MAX_CACHE_SIZE_STRUCTURE_INFOS, MAX_CACHE_LIFE_TIME_CORE);

    public static iPartsDataCombTextList showEditCombinedText(AbstractJavaViewerFormIConnector dataConnector, PartListEntryId id,
                                                              iPartsDataCombTextList dataCombTextList,
                                                              EnumSet<DictTextKindTypes> searchTextKindTypes,
                                                              boolean isEditAllowed, boolean isMultiEdit) {
        if (!isMultiEdit && ((id == null) || !id.isValidId())) {
            return null;
        }
        EditCombinedTextForm dlg = new EditCombinedTextForm(dataConnector, dataConnector.getActiveForm(), id,
                                                            dataCombTextList, searchTextKindTypes, isMultiEdit);
        dlg.mainWindow.setName("EditCombinedTextForm");
        if (isEditAllowed) {
            dlg.setTitle("!!Bearbeiten von kombinierten Texten");
        } else {
            dlg.setTitle("!!Anzeigen von kombinierten Texten");
        }
        dlg.setEditAllowed(isEditAllowed);
        if (dlg.showModal() == ModalResult.OK) {
            return dlg.getResultDataCombTextList();
        }
        return null;
    }

    private static final String EXTRAFIELD_TEXTID = "FIELD_DCT_DICT_TEXT_ID";
    private static final String EXTRAFIELD_TEXTART = "FIELD_DCT_DICT_TEXT_ART";
    private static final EnumSet<DictTextKindTypes> DEFAULT_TEXTKIND_TYPES = EnumSet.of(DictTextKindTypes.ADD_TEXT, DictTextKindTypes.NEUTRAL_TEXT);
    private static final EnumSet<DictTextKindEPCTypes> DEFAULT_EPC_TEXTKIND_TYPES = EnumSet.of(DictTextKindEPCTypes.ADD_TEXT);

    private EditToolbarButtonMenuHelper toolbarHelper;
    private DataObjectGrid entryGrid;
    private GuiMultiLangEdit previewMulti;
    private PartListEntryId partListEntryId;
    private List<iPartsDataCombText> combTextList;
    private iPartsDataCombTextList resultDataCombTextList;
    private Map<String, TextKindType> textKinds = null;
    private Map<String, TextKindType> combTextKinds = null;
    private EnumSet<DictTextKindTypes> textKindTypes;
    private String neutralText;
    private boolean isEditAllowed;
    private boolean isMultiEdit; // Kenner, ob der Dialog für den Multi-Edit benutzt wird (Falls ja, darf er keine Objekte selber speichern)

    public static void clearCache() {
        synchronized (textKindsCache) {
            textKindsCache.clear();
        }
    }

    /**
     * Erzeugt eine Instanz von EditCombinedTextForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditCombinedTextForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                PartListEntryId id, iPartsDataCombTextList dataCombTextList,
                                EnumSet<DictTextKindTypes> searchTextKindTypes, boolean isMultiEdit) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.partListEntryId = id;
        this.isMultiEdit = isMultiEdit;
        this.resultDataCombTextList = null;
        this.isEditAllowed = true;
        this.textKindTypes = searchTextKindTypes;
        if (this.textKindTypes == null) {
            this.textKindTypes = DEFAULT_TEXTKIND_TYPES;
        }
        postCreateGui();
        loadOriginalCombTexte(dataCombTextList);
        fillGrid(combTextList);
        this.neutralText = "";
        if (getConnector() instanceof EditModuleFormIConnector) {
            List<EtkDataPartListEntry> selectedPartListEntryList = ((EditModuleFormIConnector)getConnector()).getSelectedPartListEntries();
            if ((selectedPartListEntryList != null) && !selectedPartListEntryList.isEmpty()) {
                EtkDataPartListEntry partListEntry = selectedPartListEntryList.get(0);
                neutralText = partListEntry.getPart().getFieldValue(iPartsConst.FIELD_M_ADDTEXT, Language.DE.getCode(), false); // Sprache egal
            }
        }
        buildPreview(combTextList);
        doEnableButtons();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        toolbarHelper = new EditToolbarButtonMenuHelper(getConnector(), mainWindow.toolbarEntries);
        toolbarManager = toolbarHelper.getToolbarManager();
        mainWindow.toolbarEntries.setButtonOrientation(DWOrientation.VERTICAL);

        entryGrid = new DataObjectGrid(getConnector(), this) {

            /**
             * Spezialbehandlung der beiden 'Dummy'-Felder EXTRAFIELD_TEXTID und EXTRAFIELD_TEXTART
             *
             * @param tableName
             * @param fieldName
             * @param objectForTable
             * @return
             */
            @Override
            protected String getVisualValueOfField(String tableName, String fieldName, EtkDataObject objectForTable) {
                if (fieldName.equals(EXTRAFIELD_TEXTID)) {
                    String textId = objectForTable.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DCT_DICT_TEXT).getTextId();
                    TextKindType textKindType = combTextKinds.get(textId);
                    if (textKindType != null) {
                        if (textKindType.type == DictTextKindTypes.NEUTRAL_TEXT) {
                            // bei Nautral_Text keine TextId anzeigen
                            return "";
                        }
                    }
                    return objectForTable.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DCT_DICT_TEXT).getTextId();
                } else if (fieldName.equals(EXTRAFIELD_TEXTART)) {
                    String textId = objectForTable.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DCT_DICT_TEXT).getTextId();
                    TextKindType textKindType = combTextKinds.get(textId);
                    if (textKindType != null) {
                        return textKindType.description;
                    }
                    return "??";
                }
                return super.getVisualValueOfField(tableName, fieldName, objectForTable);
            }

            @Override
            protected void onTableSelectionChanged(Event event) {
                doEnableButtons();
            }

            @Override
            protected void onCellDblClicked(int row, int col, Event event) {
                GuiButtonOnPanel okButton = mainWindow.buttonpanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK);
                if (okButton.isEnabled()) {
                    okButton.doClick();
                }
            }

        };
        entryGrid.getTable().setSortEnabled(false);

        entryGrid.setDisplayFields(getDisplayFields());
        entryGrid.setMultiSelect(false);
        entryGrid.setPageSplitNumberOfEntriesPerPage(iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE);

        mainWindow.panelEntries.addChildBorderCenter(entryGrid.getGui());

        previewMulti = new GuiMultiLangEdit();
        previewMulti.setReadOnly(true);
        previewMulti.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        mainWindow.panelPreview.addChild(previewMulti);

        createToolbarButtons();
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


    //===== Getter and Setter =====
    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public boolean isEditAllowed() {
        return isEditAllowed;
    }

    public void setEditAllowed(boolean editAllowed) {
        isEditAllowed = editAllowed;
    }

    public iPartsDataCombTextList getResultDataCombTextList() {
        return resultDataCombTextList;
    }

    //===== Getter and Setter End =====

    /**
     * Menus/Toolbar-Buttons und OK-Button behandeln
     */
    private void doEnableButtons() {
        List<EtkDataObject> selectedList = entryGrid.getSelection();
        boolean isSingleSelected;
        boolean isMultiSelected;
        if (selectedList == null) {
            isSingleSelected = false;
            isMultiSelected = false;
        } else {
            isSingleSelected = selectedList.size() == 1;
            isMultiSelected = selectedList.size() > 1;
        }
        if (isEditAllowed) {
            toolbarHelper.showToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW, entryGrid.getContextMenu());
            // Nur die Texte löschen, die auch zu den möglichen Textarten passen
            boolean isDeletionOrEditPossible = (isSingleSelected || isMultiSelected) && selectionMatchesTextKinds();
            toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, entryGrid.getContextMenu(), isDeletionOrEditPossible);
            toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, entryGrid.getContextMenu(), isDeletionOrEditPossible);
            if (isSingleSelected || isMultiSelected) {
                int[] selectedRowIndeces = entryGrid.getTable().getSelectedRowIndices();
                int firstIndex = selectedRowIndeces[0];
                int lastIndex = selectedRowIndeces[selectedRowIndeces.length - 1];

                // Die erste Zeile im Grid ist ausgewählt
                if (firstIndex == 0) {
                    toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_UP, entryGrid.getContextMenu(), false);
                    // Wenn die letzte Zeile auch gleichzeitig die letzte im Grid ist, kann nicht nach unten geschoben werden
                    if (lastIndex == (entryGrid.getTable().getRowCount() - 1)) {
                        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_DOWN, entryGrid.getContextMenu(), false);
                    } else {
                        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_DOWN, entryGrid.getContextMenu(), true);
                    }
                    // Die letzte Zeile im Grid ist ausgewählt
                } else if (lastIndex == (entryGrid.getTable().getRowCount() - 1)) {
                    toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_UP, entryGrid.getContextMenu(), true);
                    toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_DOWN, entryGrid.getContextMenu(), false);
                } else { // Mittendrin
                    toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_UP, entryGrid.getContextMenu(), true);
                    toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_DOWN, entryGrid.getContextMenu(), true);
                }
            } else {
                toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_UP, entryGrid.getContextMenu(), false);
                toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_DOWN, entryGrid.getContextMenu(), false);
            }
            mainWindow.buttonpanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, isModified());
        } else {
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW, entryGrid.getContextMenu());
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, entryGrid.getContextMenu());
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, entryGrid.getContextMenu());
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_UP, entryGrid.getContextMenu());
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_DOWN, entryGrid.getContextMenu());
            mainWindow.buttonpanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, false);
        }

    }

    private boolean selectionMatchesTextKinds() {
        int[] selectedRows = entryGrid.getTable().getSelectedRowIndices();
        for (int index : selectedRows) {
            if (!singleSelectionMatchesTextKinds(index)) {
                return false;
            }
        }
        return true;
    }

    private boolean singleSelectionMatchesTextKinds(int selectedIndex) {
        List<iPartsDataCombText> dataCombTextList = getCurrentCombTextList();
        iPartsDataCombText combText = dataCombTextList.get(selectedIndex);
        String textId = combText.getFieldValueAsMultiLanguage(FIELD_DCT_DICT_TEXT).getTextId();
        TextKindType textKind = combTextKinds.get(textId);
        return textKindTypes.contains(textKind.type);
    }

    /**
     * Laden der übergebenen Kombinierten Texte (via partListEntryId)
     */
    private void loadOriginalCombTexte(iPartsDataCombTextList dataCombTextList) {
        combTextKinds = new HashMap<>();
        // Beim Multi-Edit wird eine "Platzhalter"-Stücklistenposition benutzt, die keine valide ID hat
        if (((partListEntryId != null) && partListEntryId.isValidId()) || isMultiEdit) {
            if (dataCombTextList == null) {
                startPseudoTransactionForActiveChangeSet(true);
                try {
                    combTextList = iPartsDataCombTextList.loadForPartListEntry(partListEntryId, null, getProject()).getAsList();

                    for (iPartsDataCombText dataCombText : combTextList) {
                        // Multilang nachladen, da nicht vollständig besetzt
                        EtkMultiSprache multiLang = getProject().getDbLayer().loadMultiLanguageByTextNr(TableAndFieldName.make(iPartsConst.TABLE_DA_COMB_TEXT, iPartsConst.FIELD_DCT_DICT_TEXT),
                                                                                                        dataCombText.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DCT_DICT_TEXT).getTextId());
                        dataCombText.setFieldValueAsMultiLanguage(iPartsConst.FIELD_DCT_DICT_TEXT, multiLang, DBActionOrigin.FROM_DB);
                        dataCombText.removeForeignTablesAttributes();
                    }
                } finally {
                    stopPseudoTransactionForActiveChangeSet();
                }
            } else {
                combTextList = dataCombTextList.getAsList();
            }
        } else {
            combTextList = new DwList<>();
        }
    }

    /**
     * Grid füllen
     *
     * @param combTextList
     */
    private void fillGrid(List<iPartsDataCombText> combTextList) {
        if (textKinds == null) {
            textKinds = loadTextKinds(getProject());
        }

        entryGrid.getTable().switchOffEventListeners();
        entryGrid.clearGrid();
        entryGrid.getTable().switchOnEventListeners();
        if (!combTextList.isEmpty()) {
            for (iPartsDataCombText dataCombText : combTextList) {
                EtkMultiSprache multiSprache = dataCombText.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DCT_DICT_TEXT);
                if (combTextKinds.get(multiSprache.getTextId()) == null) {
                    // TextKindType bestimmen in Abhängigkeit von der TextId
                    iPartsDataDictMetaList dataDictMetaList = iPartsDataDictMetaList.loadMetaFromTextIdList(getProject(), multiSprache.getTextId());
                    if (!dataDictMetaList.isEmpty()) {
                        TextKindType txtKindType = null;
                        for (iPartsDataDictMeta dataDictMeta : dataDictMetaList) {
                            TextKindType testTxtKindType = textKinds.get(dataDictMeta.getAsId().getTextKindId());
                            if (testTxtKindType != null) {
                                txtKindType = testTxtKindType;
                                break;
                            }
                        }
                        if (txtKindType != null) {
                            combTextKinds.put(multiSprache.getTextId(), txtKindType);
                        }
                    }
                }
                entryGrid.addObjectToGrid(dataCombText);
            }
        }
    }

    /**
     * Grid und Preview besetzen
     * ggf. vorherigen selectedIndex setzen
     *
     * @param combTextList
     * @param selectedIndex
     */
    private void reloadGrid(List<iPartsDataCombText> combTextList, int selectedIndex) {
        fillGrid(combTextList);
        buildPreview(combTextList);
        if (selectedIndex != -1) {
            entryGrid.getTable().setSelectedRow(selectedIndex, true);
        } else {
            doEnableButtons();
        }
    }

    /**
     * Tabelle und Vorschau threadsafe laden
     *
     * @param combTextList
     * @param selectedIndex
     */
    private void reloadGridThreadSafe(final List<iPartsDataCombText> combTextList, final int selectedIndex) {
        Session.invokeThreadSafeInSession(() -> reloadGrid(combTextList, selectedIndex));
    }


    /**
     * Vorschau für alle Kombierten Texte in allen Sprachen
     *
     * @param combTextList
     */
    private void buildPreview(List<iPartsDataCombText> combTextList) {
        Set<String> langs = new TreeSet<>();
        // die verwendeten Sprachen bestimmen
        for (iPartsDataCombText dataCombText : combTextList) {
            langs.addAll(dataCombText.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DCT_DICT_TEXT).getSprachen());
        }
        if (langs.isEmpty() && !neutralText.isEmpty()) {
            langs.addAll(getProject().getConfig().getDatabaseLanguages());
        }
        langs.add(getProject().getDBLanguage());
        List<Language> langList = new DwList<>();
        for (String lang : langs) {
            langList.add(Language.findLanguage(lang));
        }
        EtkMultiSprache multi = new EtkMultiSprache(langList);
        List<String> dbFallBackLanguages = getProject().getDataBaseFallbackLanguages();

        // kombinierte Texte für alle Sprachen zusammenbauen
        for (String lang : langs) {
            String currentText = multi.getText(lang);
            String lastText = "";
            for (iPartsDataCombText dataCombText : combTextList) {
                EtkMultiSprache multiSprache = dataCombText.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DCT_DICT_TEXT);
                String text;
                TextKindType textKindType = combTextKinds.get(multiSprache.getTextId());
                if ((textKindType != null) && (textKindType.type == DictTextKindTypes.NEUTRAL_TEXT)) {
                    // bei neutralem Text immer DE holen (zur Sicherheit)
                    text = multiSprache.getText(Language.DE.getCode());
                    lastText = text;
                } else {
                    text = multiSprache.getTextByNearestLanguage(lang, dbFallBackLanguages);
                    lastText = "";
                }
                if (!currentText.isEmpty()) {
                    if (!text.isEmpty()) {
                        currentText = currentText + " ";
                    }
                }
                currentText = currentText + text;
            }
            if (!neutralText.isEmpty() && !neutralText.equals(lastText)) {
                if (currentText.isEmpty()) {
                    currentText = neutralText;
                } else {
                    currentText = currentText + "; " + neutralText;
                }
            }
            multi.setText(lang, currentText);
        }
        previewMulti.setMultiLanguage(multi);
        previewMulti.setSelectedLanguage(Language.getFromCode(getProject().getDBLanguage()));
    }

    /**
     * Überprüfung, ob die aktuelle Liste unterschiedlich zur Übergebenen ist
     *
     * @return
     */
    private boolean isModified() {
        List<iPartsDataCombText> dataCombTextList = getCurrentCombTextList();
        if (dataCombTextList.size() != combTextList.size()) {
            return true;
        }
        for (int lfdNr = 0; lfdNr < dataCombTextList.size(); lfdNr++) {
            iPartsDataCombText currentDataComText = dataCombTextList.get(lfdNr);
            iPartsDataCombText dataComText = combTextList.get(lfdNr);
            if (!currentDataComText.getAsId().equals(dataComText.getAsId())) {
                return true;
            }
            String currentTextId = currentDataComText.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DCT_DICT_TEXT).getTextId();
            String textId = dataComText.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DCT_DICT_TEXT).getTextId();
            if (!currentTextId.equals(textId)) {
                return true;
            }
        }
        return false;
    }

    protected EtkDisplayFields getDisplayFields() {
        EtkDisplayFields displayResultFields = new EtkDisplayFields();
        //displayResultFields.load(getConfig(), configKey);

        if (displayResultFields.size() == 0) {
            EtkDisplayFields defaultDisplayFields = createDefaultDisplayFields();
            if (defaultDisplayFields != null) {
                for (EtkDisplayField defaultDisplayField : defaultDisplayFields.getFields()) {
                    displayResultFields.addFeld(defaultDisplayField);
                }
            }

            displayResultFields.loadStandards(getConfig());
        }

        return displayResultFields;
    }

    protected EtkDisplayFields createDefaultDisplayFields() {
        EtkDisplayFields defaultDisplayFields = new EtkDisplayFields();
        //LookupListHelper.initGridFields(getProject(), iPartsConst.TABLE_DA_COMB_TEXT, defaultDisplayFields);
        EtkDisplayField displayField = new EtkDisplayField(iPartsConst.TABLE_DA_COMB_TEXT, EXTRAFIELD_TEXTID, false, false);
        EtkMultiSprache multi = new EtkMultiSprache();
        multi.setText(getProject().getViewerLanguage(), "!!Text-ID");
        displayField.setText(multi);
        displayField.setDefaultText(false);
        defaultDisplayFields.addFeld(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_COMB_TEXT, EXTRAFIELD_TEXTART, false, false);
        multi = new EtkMultiSprache();
        multi.setText(getProject().getViewerLanguage(), "!!Textart");
        displayField.setText(multi);
        displayField.setDefaultText(false);
        defaultDisplayFields.addFeld(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_COMB_TEXT, iPartsConst.FIELD_DCT_DICT_TEXT, true, false);
        multi = new EtkMultiSprache();
        multi.setText(getProject().getViewerLanguage(), "!!Text");
        displayField.setText(multi);
        displayField.setDefaultText(false);
        defaultDisplayFields.addFeld(displayField);
        return defaultDisplayFields;
    }


    private void createToolbarButtons() {
        ToolbarButtonMenuHelper.ToolbarMenuHolder holder;

        GuiSeparator separator = toolbarHelper.createMenuSeparator("menuSeparator", getUITranslationHandler());
        entryGrid.getContextMenu().addChild(separator);

        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_NEW, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doAdd(event);
            }
        });
        entryGrid.getContextMenu().addChild(holder.menuItem);
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_WORK, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doEdit(event);
            }
        });
        entryGrid.getContextMenu().addChild(holder.menuItem);
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_DELETE, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doRemove(event);
            }
        });
        entryGrid.getContextMenu().addChild(holder.menuItem);
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.IMG_UP, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doMoveUp(event);
            }
        });
        entryGrid.getContextMenu().addChild(holder.menuItem);
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.IMG_DOWN, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doMoveDown(event);
            }
        });
        entryGrid.getContextMenu().addChild(holder.menuItem);
    }

    /**
     * bildet die Liste der gerade angezeigten {@link iPartsDataCombText}s
     *
     * @return
     */
    private List<iPartsDataCombText> getCurrentCombTextList() {
        final List<iPartsDataCombText> dataCombTextList = new DwList<>();
        for (int row = 0; row < entryGrid.getTable().getRowCount(); row++) {
            DataObjectGrid.GuiTableRowWithObjects rowEx = (DataObjectGrid.GuiTableRowWithObjects)entryGrid.getTable().getRow(row);
            dataCombTextList.add((iPartsDataCombText)rowEx.getObjectForTable(iPartsConst.TABLE_DA_COMB_TEXT));
        }
        return dataCombTextList;
    }

    /**
     * einen Eintrag nach oben schieben
     *
     * @param event
     */
    private void doMoveUp(Event event) {
        List<iPartsDataCombText> dataCombTextList = getCurrentCombTextList();
        int selectedIndex = entryGrid.getTable().getSelectedRowIndex();

        iPartsDataCombText oldDataComText = dataCombTextList.remove(selectedIndex);
        dataCombTextList.add(selectedIndex - 1, oldDataComText);
        reloadGridThreadSafe(dataCombTextList, selectedIndex - 1);
    }

    /**
     * einen Eintrag nach unten schieben
     *
     * @param event
     */
    private void doMoveDown(Event event) {
        List<iPartsDataCombText> dataCombTextList = getCurrentCombTextList();
        int selectedIndex = entryGrid.getTable().getSelectedRowIndex();

        iPartsDataCombText oldDataComText = dataCombTextList.remove(selectedIndex);
        dataCombTextList.add(selectedIndex + 1, oldDataComText);
        reloadGridThreadSafe(dataCombTextList, selectedIndex + 1);
    }

    /**
     * einen Eintrag löschen
     *
     * @param event
     */
    private void doRemove(Event event) {
        List<iPartsDataCombText> dataCombTextList = getCurrentCombTextList();
        int selectedIndex = entryGrid.getTable().getSelectedRowIndex();
        dataCombTextList.remove(selectedIndex);
        reloadGridThreadSafe(dataCombTextList, -1);
    }

    /**
     * einen Eintrag hinzufügen
     *
     * @param event
     */
    private void doAdd(Event event) {
        Language lang = Language.findLanguage(getProject().getDBLanguage());
        Collection<iPartsDataDictTextKind> textKindList = iPartsDataDictTextKindList.loadSpecialTextKindListSortedByName(getProject(), textKindTypes);
        List<iPartsDataCombText> dataCombTextList = getCurrentCombTextList();

        // Textart aus der aktuellen (optionalen) Selektion ermitteln
        DictTextKindTypes initialType = textKindTypes.iterator().next();
        int selectedIndex = entryGrid.getTable().getSelectedRowIndex();
        if (selectedIndex >= 0) {
            iPartsDataCombText currentDataCombText = dataCombTextList.get(selectedIndex);
            TextKindType textKindType = combTextKinds.get(currentDataCombText.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DCT_DICT_TEXT).getTextId());
            if (textKindType != null) {
                initialType = textKindType.type;
            }
        }

        EtkMultiSprache multiLang = DictMultiLangEditForm.showDictMultiLangEdit(getConnector(), this,
                                                                                lang, "", initialType,
                                                                                textKindList, true, false,
                                                                                TABLE_DA_COMB_TEXT);
        if (multiLang != null) {
            // Anlegen from Scratch
            iPartsCombTextId combTextId = new iPartsCombTextId(partListEntryId, EtkDbsHelper.formatLfdNr(0));
            iPartsDataCombText newDataCombText = new iPartsDataCombText(getProject(), combTextId);
            newDataCombText.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            newDataCombText.setFieldValueAsMultiLanguage(iPartsConst.FIELD_DCT_DICT_TEXT, multiLang, DBActionOrigin.FROM_DB);
            if (selectedIndex >= 0) {
                dataCombTextList.add(selectedIndex + 1, newDataCombText);
            } else {
                dataCombTextList.add(newDataCombText);
                selectedIndex = dataCombTextList.size() - 2;
            }
            reloadGridThreadSafe(dataCombTextList, selectedIndex + 1);
        }
    }

    /**
     * einen Eintrag editieren (ggf. ändern)
     *
     * @param event
     */
    private void doEdit(Event event) {
        Language lang = Language.findLanguage(getProject().getDBLanguage());
        Collection<iPartsDataDictTextKind> textKindList = iPartsDataDictTextKindList.loadSpecialTextKindListSortedByName(getProject(), textKindTypes);
        List<iPartsDataCombText> dataCombTextList = getCurrentCombTextList();
        int selectedIndex = entryGrid.getTable().getSelectedRowIndex();

        iPartsDataCombText currentDataCombText = dataCombTextList.get(selectedIndex);
        DictTextKindTypes initialType = null;
        TextKindType textKindType = combTextKinds.get(currentDataCombText.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DCT_DICT_TEXT).getTextId());
        if (textKindType != null) {
            initialType = textKindType.type;
        }

        String langWithTextKind;
        if (initialType == DictTextKindTypes.NEUTRAL_TEXT) {
            langWithTextKind = Language.DE.getCode(); // Bei sprachneutralen Texten immer Deutsch als Sprache nehmen
        } else {
            langWithTextKind = lang.getCode();
        }
        String text = currentDataCombText.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DCT_DICT_TEXT).getText(langWithTextKind);

        DictMultiLangEditForm.DictMultiLangResult result = DictMultiLangEditForm.showDictMultiLangEdit(getConnector(), this, lang,
                                                                                                       text, initialType,
                                                                                                       textKindList, true, false);
        if (result != null) {
            if (!result.textId.equals(currentDataCombText.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DCT_DICT_TEXT).getTextId())) {
                EtkMultiSprache multiLang = getProject().getDbLayer().loadMultiLanguageByTextNr(result.fieldName, result.textNr);
                iPartsDataCombText changedDataCombText = new iPartsDataCombText(getProject(), null);
                changedDataCombText.assign(getProject(), currentDataCombText, DBActionOrigin.FROM_DB);
                changedDataCombText.setFieldValueAsMultiLanguage(iPartsConst.FIELD_DCT_DICT_TEXT, multiLang, DBActionOrigin.FROM_DB);
                dataCombTextList.set(selectedIndex, changedDataCombText);
                reloadGridThreadSafe(dataCombTextList, selectedIndex);
            }
        }
    }

    /**
     * Vor dem Schließen der Form müssen die Elemente neu durchnummeriert werden
     *
     * @param event
     */
    private void onButtonOkClick(Event event) {
        resultDataCombTextList = new iPartsDataCombTextList();
        List<iPartsDataCombText> currentDataCombTextList = getCurrentCombTextList();
        if (isModified()) {
            List<iPartsDataCombText> originalCombTextList = new DwList<>(combTextList);
            Map<String, EtkMultiSprache> textToSeqNumber = new LinkedHashMap<>();
            // Alle Texte samt Text-Positionsnummer aufsammeln
            for (int lfdNr = 0; lfdNr < currentDataCombTextList.size(); lfdNr++) {
                iPartsDataCombText currentDataCombText = currentDataCombTextList.get(lfdNr);
                String textSeqNo = EtkDbsHelper.formatLfdNr(lfdNr + 1);
                // Merke die Textposition des aktuellen Textes
                EtkMultiSprache currentText = currentDataCombText.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DCT_DICT_TEXT);
                textToSeqNumber.put(textSeqNo, currentText);
            }
            iPartsCombTextHelper.handleCombTextsWithOrder(getProject(), partListEntryId, textToSeqNumber, originalCombTextList, resultDataCombTextList);

        } else {
            // keine Änderung
            resultDataCombTextList.addAll(currentDataCombTextList, DBActionOrigin.FROM_EDIT);
        }
        close();
    }

    public static Map<String, TextKindType> loadTextKinds(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), EditCombinedTextForm.class, "TextKinds", false);
        synchronized (textKindsCache) {
            Map<String, EditCombinedTextForm.TextKindType> loadedTextKinds = textKindsCache.get(hashObject);
            if (loadedTextKinds == null) {
                EnumSet<DictTextKindTypes> textKindTypes = DEFAULT_TEXTKIND_TYPES;

                // HashMap mit TextKindId und der zugehörigen Beschreibung und dem zugehörigen DictTextKindTypes aufbauen
                loadedTextKinds = new HashMap<>();
                String language = project.getConfig().getCurrentViewerLanguage();
                Set<String> foreignTextKindIds = new LinkedHashSet<>();
                // MAD Textart-Ids sammeln
                for (DictTextKindTypes textKindId : textKindTypes) {
                    foreignTextKindIds.add(textKindId.getMadId());
                }
                // EPC Textart-IDs sammeln
                for (DictTextKindEPCTypes epcTextKindId : DEFAULT_EPC_TEXTKIND_TYPES) {
                    foreignTextKindIds.add(epcTextKindId.getEpcId());
                }
                Collection<iPartsDataDictTextKind> list = iPartsDataDictTextKindList.loadSpecialTextKindListSortedByName(project, foreignTextKindIds);
                for (iPartsDataDictTextKind dataDictTextKind : list) {
                    EditCombinedTextForm.TextKindType txtKindType = new EditCombinedTextForm.TextKindType();
                    txtKindType.description = dataDictTextKind.getName(language);
                    txtKindType.type = dataDictTextKind.getForeignTextKindType();
                    loadedTextKinds.put(dataDictTextKind.getAsId().getTextKindId(), txtKindType);
                }

                textKindsCache.put(hashObject, loadedTextKinds);
            }
            return loadedTextKinds;
        }
    }

    public static class TextKindType {

        public String description;
        public DictTextKindTypes type;
    }

    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    private de.docware.framework.modules.gui.controls.menu.GuiContextMenu contextmenu;

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelEntries;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelEntries;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.toolbar.GuiToolbar toolbarEntries;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelPreview;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelPreview;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            contextmenu = new de.docware.framework.modules.gui.controls.menu.GuiContextMenu();
            contextmenu.setName("contextmenu");
            contextmenu.__internal_setGenerationDpi(96);
            contextmenu.registerTranslationHandler(translationHandler);
            contextmenu.setScaleForResolution(true);
            contextmenu.setMinimumWidth(10);
            contextmenu.setMinimumHeight(10);
            contextmenu.setMenuName("contextmenu");
            contextmenu.setParentControl(this);
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
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMain.setLayout(panelMainLayout);
            panelEntries = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelEntries.setName("panelEntries");
            panelEntries.__internal_setGenerationDpi(96);
            panelEntries.registerTranslationHandler(translationHandler);
            panelEntries.setScaleForResolution(true);
            panelEntries.setMinimumWidth(10);
            panelEntries.setMinimumHeight(10);
            panelEntries.setPaddingLeft(8);
            panelEntries.setPaddingRight(8);
            de.docware.framework.modules.gui.layout.LayoutBorder panelEntriesLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelEntries.setLayout(panelEntriesLayout);
            labelEntries = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelEntries.setName("labelEntries");
            labelEntries.__internal_setGenerationDpi(96);
            labelEntries.registerTranslationHandler(translationHandler);
            labelEntries.setScaleForResolution(true);
            labelEntries.setMinimumWidth(10);
            labelEntries.setMinimumHeight(10);
            labelEntries.setPaddingTop(4);
            labelEntries.setPaddingLeft(8);
            labelEntries.setPaddingRight(8);
            labelEntries.setPaddingBottom(4);
            labelEntries.setText("!!Ausgewählte Elemente");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelEntriesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelEntriesConstraints.setPosition("north");
            labelEntries.setConstraints(labelEntriesConstraints);
            panelEntries.addChild(labelEntries);
            toolbarEntries = new de.docware.framework.modules.gui.controls.toolbar.GuiToolbar();
            toolbarEntries.setName("toolbarEntries");
            toolbarEntries.__internal_setGenerationDpi(96);
            toolbarEntries.registerTranslationHandler(translationHandler);
            toolbarEntries.setScaleForResolution(true);
            toolbarEntries.setMinimumWidth(20);
            toolbarEntries.setMinimumHeight(10);
            toolbarEntries.setBackgroundImage(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignToolbarBigBackground"));
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder toolbarEntriesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            toolbarEntriesConstraints.setPosition("west");
            toolbarEntries.setConstraints(toolbarEntriesConstraints);
            panelEntries.addChild(toolbarEntries);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelEntriesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelEntries.setConstraints(panelEntriesConstraints);
            panelMain.addChild(panelEntries);
            panelPreview = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelPreview.setName("panelPreview");
            panelPreview.__internal_setGenerationDpi(96);
            panelPreview.registerTranslationHandler(translationHandler);
            panelPreview.setScaleForResolution(true);
            panelPreview.setMinimumWidth(10);
            panelPreview.setMinimumHeight(70);
            panelPreview.setPaddingLeft(8);
            panelPreview.setPaddingRight(8);
            de.docware.framework.modules.gui.layout.LayoutBorder panelPreviewLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelPreview.setLayout(panelPreviewLayout);
            labelPreview = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelPreview.setName("labelPreview");
            labelPreview.__internal_setGenerationDpi(96);
            labelPreview.registerTranslationHandler(translationHandler);
            labelPreview.setScaleForResolution(true);
            labelPreview.setMinimumWidth(10);
            labelPreview.setMinimumHeight(10);
            labelPreview.setPaddingTop(4);
            labelPreview.setPaddingBottom(4);
            labelPreview.setText("!!Vorschau");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelPreviewConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelPreviewConstraints.setPosition("north");
            labelPreview.setConstraints(labelPreviewConstraints);
            panelPreview.addChild(labelPreview);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelPreviewConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelPreviewConstraints.setPosition("south");
            panelPreview.setConstraints(panelPreviewConstraints);
            panelMain.addChild(panelPreview);
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
            buttonpanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonOKActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onButtonOkClick(event);
                }
            });
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