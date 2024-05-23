/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.dictionary;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.sqlbuilder.EtkSqlCommonDbSelect;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.*;
import de.docware.apps.etk.base.forms.common.components.MenuRunnable;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.forms.events.OnStartSearchEvent;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsLanguage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictTextKind;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictTextKindList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictTextKindId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictTransJobStates;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTxtKindIdByMADId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.OnEditChangeRecordEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.SimpleMasterDataSearchFilterGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditDatabaseHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.config.db.EtkFieldType;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.config.db.datatypes.DatatypeUtils;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiCheckbox;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.controls.table.HtmlTablePageSplitMode;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.HTMLUtils;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.j2ee.EC;
import de.docware.util.os.OsUtils;
import de.docware.util.sql.TableAndFieldName;
import de.docware.util.sql.terms.CaseMode;
import de.docware.util.sql.terms.Condition;
import de.docware.util.sql.terms.Fields;
import de.docware.util.sql.terms.LeftOuterJoin;

import java.util.*;

/**
 * Formular für die Anzeige der Tabelle DICT_TRANS_JOB und CSV-Export Möglichkeit
 * (keine Besrbeitung der Records möglich)
 */
public class DictShowTransJobs extends SimpleMasterDataSearchFilterGrid {

    public static final String TITLE = "!!Verwaltungstabelle für Übersetzungsprozess";
    public static final String TOOLTIP_TEXT = "!!Verwaltungstabelle als CSV exportieren...";
    private static final String SEPARATOR = "\t";
    // Felder für speziellen Spaltenfilter
    private static final String[] SPECIAL_TABLEFILTER_FIELDS = new String[]{ FIELD_DTJ_SOURCE_LANG, FIELD_DTJ_DEST_LANG,
                                                                             FIELD_DTJ_TEXTKIND };
    private static final String[] SPECIAL_LANG_TABLEFILTER_FIELDS = new String[]{ FIELD_DTJ_SOURCE_LANG, FIELD_DTJ_DEST_LANG };

    public static void showDictTransJobData(AbstractJavaViewerForm owner) {
        // Aktive Form holen
        AbstractJavaViewerForm activeForm = owner.getConnector().getActiveForm();
        showDictTransJobData(activeForm.getConnector(), activeForm);
    }

    private static void showDictTransJobData(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        DictShowTransJobs dlg = new DictShowTransJobs(dataConnector, parentForm, TABLE_DA_DICT_TRANS_JOB, null);

        // Anzeigefelder definieren
        EtkDisplayFields displayFields = getDisplayFields(dataConnector);
        EtkDisplayFields searchFields = getSearchFields(dataConnector);
        // Sortierung
        LinkedHashMap<String, Boolean> sortFields = new LinkedHashMap<>();
        sortFields.put(FIELD_DTJ_TEXTID, false);
        sortFields.put(FIELD_DTJ_SOURCE_LANG, false);
        sortFields.put(FIELD_DTJ_DEST_LANG, false);
        dlg.setSortFields(sortFields);

        dlg.doResizeWindow(SCREEN_SIZES.SCALE_FROM_PARENT);
        dlg.setMaxSearchControlsPerRow(4);
        dlg.setSearchFields(searchFields);
        dlg.setDisplayResultFields(displayFields, true);
        dlg.setEditFields(null);
        dlg.setRequiredResultFields(null);

        dlg.setModifyAllowed(false);
        dlg.setEditAllowed(false);
        dlg.setDeleteAllowed(false);
        dlg.setWindowName("DictShowTransJobs");
        dlg.setTitle(TranslationHandler.translate(TITLE));
        // Maximal 3000 Treffer anzeigen
        dlg.setMaxResults(J2EEHandler.isJ2EE() ? 10 * MAX_SELECT_SEARCH_RESULTS_SIZE : -1);

        dlg.showModal();
    }

    private static EtkDisplayFields getDisplayFields(AbstractJavaViewerFormIConnector dataConnector) {
        // Anzeigefelder definieren
        EtkDisplayFields displayFields = new EtkDisplayFields();
        EtkProject project = dataConnector.getProject();
        displayFields.load(project.getConfig(), iPartsEditConfigConst.iPARTS_EDIT_DICT_TRANSJOB_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS);

        if (displayFields.size() == 0) {
            createTransJobdisplayField(project, displayFields, FIELD_DTJ_TEXTID, false, 0);
            createTransJobdisplayField(project, displayFields, iPartsDataVirtualFieldsDefinition.DTJ_SOURCE_TEXT, false, 40);
            EtkDisplayField displayField = addDisplayField(TABLE_DA_DICT_META, FIELD_DA_DICT_META_SOURCE, false, false, null, project, displayFields);
            displayField.setColumnFilterEnabled(true);
            displayField.setWidth(0);
            createTransJobdisplayField(project, displayFields, FIELD_DTJ_JOBID, false, 0);
            createTransJobdisplayField(project, displayFields, FIELD_DTJ_SOURCE_LANG, false, 0);
            createTransJobdisplayField(project, displayFields, FIELD_DTJ_DEST_LANG, false, 0);
            createTransJobdisplayField(project, displayFields, FIELD_DTJ_TRANSLATION_DATE, false, 0);
            createTransJobdisplayField(project, displayFields, FIELD_DTJ_BUNDLE_NAME, false, 0);
            createTransJobdisplayField(project, displayFields, FIELD_DTJ_TRANSLATION_STATE, false, 0);
            createTransJobdisplayField(project, displayFields, FIELD_DTJ_STATE_CHANGE, false, 0);
            createTransJobdisplayField(project, displayFields, FIELD_DTJ_LAST_MODIFIED, false, 0);
            createTransJobdisplayField(project, displayFields, FIELD_DTJ_JOB_TYPE, false, 0);
            createTransJobdisplayField(project, displayFields, FIELD_DTJ_TEXTKIND, false, 0);
            createTransJobdisplayField(project, displayFields, FIELD_DTJ_USER_ID, false, 0);
            createTransJobdisplayField(project, displayFields, FIELD_DTJ_ERROR_CODE, false, 0);
        }
        return displayFields;
    }

    private static EtkDisplayField createTransJobdisplayField(EtkProject project, EtkDisplayFields displayFields, String fieldName, boolean multiLanguage, int width) {
        EtkDisplayField displayField = addDisplayField(TABLE_DA_DICT_TRANS_JOB, fieldName, multiLanguage, false, null, project, displayFields);
        displayField.setColumnFilterEnabled(true);
        displayField.setWidth(width);
        return displayField;
    }

    private static EtkDisplayFields getSearchFields(AbstractJavaViewerFormIConnector dataConnector) {
        // Suchfelder definieren
        EtkDisplayFields searchFields = new EtkDisplayFields();
        EtkProject project = dataConnector.getProject();

        searchFields.load(project.getConfig(), iPartsEditConfigConst.iPARTS_EDIT_DICT_TRANSJOB_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_SEARCHFIELDS,
                          dataConnector.getConfig().getCurrentDatabaseLanguage());
        if (searchFields.size() == 0) {
            String tableName = TABLE_DA_DICT_TRANS_JOB;
            // Suchfelder definieren
            searchFields.addFeld(createSearchField(project, tableName, FIELD_DTJ_TEXTID, false, false));
            searchFields.addFeld(createSearchField(project, tableName, FIELD_DTJ_SOURCE_LANG, false, false));
            searchFields.addFeld(createSearchField(project, tableName, FIELD_DTJ_DEST_LANG, false, false));
            searchFields.addFeld(createSearchField(project, tableName, FIELD_DTJ_JOBID, false, false));
            searchFields.addFeld(createSearchField(project, tableName, FIELD_DTJ_BUNDLE_NAME, false, false));
            searchFields.addFeld(createSearchField(project, tableName, FIELD_DTJ_TRANSLATION_STATE, false, false));
            searchFields.addFeld(createSearchField(project, tableName, FIELD_DTJ_USER_ID, false, false));
            searchFields.addFeld(createSearchField(project, tableName, FIELD_DTJ_TEXTKIND, false, false));
            searchFields.loadStandards(project.getConfig());
        }
        return searchFields;
    }

    private String sourceLang; // Quellsprache für die Darstellung des Textes
    private Map<String, EtkMultiSprache> textMap;  // Map für TextId zu EtkMultiSprache
    private String currentTextId;  // aktuelle TextId einer Row
    private boolean needsJoin;
    private Map<String, String> textKindNameMap;  // Namen der Textarten
    private GuiCheckbox checkboxShowCompleted;  // Checkbox für die Anzeige mit Status Translated
    private TreeMap<String, iPartsDataDictTextKind> sortedTextKindMap;

    /**
     * Erzeugt eine Instanz von SimpleMasterDataSearchResultGrid.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     * @param parentForm
     * @param tableName
     * @param onEditChangeRecordEvent
     */
    public DictShowTransJobs(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName, OnEditChangeRecordEvent onEditChangeRecordEvent) {
        super(dataConnector, parentForm, tableName, onEditChangeRecordEvent);
        textMap = new HashMap<>();
        textKindNameMap = new HashMap<>();
        sortedTextKindMap = null;
        OnStartSearchEvent onStartSearchEvent = () -> textMap.clear();
        setOnStartSearchEvent(onStartSearchEvent);
        // Page-Mode des Grid einschalten
        getTable().setPageSplitNumberOfEntriesPerPage(iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE);
        getTable().setHtmlTablePageSplitMode(HtmlTablePageSplitMode.BUTTONS);
        setColumnFilterFactory(new DictShowDataSearchFilterFactory(getProject()));
    }

    @Override
    protected void postCreateGui() {
        super.postCreateGui();

        // Checkbox für "Anzeige abgeschlossener Übersetzungsaufträge"
        checkboxShowCompleted = new GuiCheckbox("!!Anzeige abgeschlossener Übersetzungsaufträge", false);
        checkboxShowCompleted.setConstraints(new ConstraintsGridBag(0, 2, 1, 1, 0, 0, ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_HORIZONTAL, 8, 4, 8, 4));
        getPanelMain().addChild(checkboxShowCompleted);
        checkboxShowCompleted.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                DBDataObjectAttributes selection = getSelection();
                if (selection != null) {
                    setSelectionAfterSearch(selection);
                }
                startSearch();
            }
        });
    }

    private TreeMap<String, iPartsDataDictTextKind> getSortedTextKindMap() {
        if (sortedTextKindMap == null) {
            sortedTextKindMap = iPartsDataDictTextKindList.loadAllTextKindListSortedByName(getProject());
        }
        return sortedTextKindMap;
    }

    @Override
    protected void addToolbarButtonsAndContextMenuItems(EditToolbarButtonMenuHelper toolbarHelper, GuiContextMenu contextMenu) {
        super.addToolbarButtonsAndContextMenuItems(toolbarHelper, contextMenu);

        // Export Menu/Toolbar Button einhängen
        ToolbarButtonMenuHelper.ToolbarMenuHolder holder;
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_AO_EXPORT, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                endSearch();
                doExport();
            }
        });
        toolbarHelper.changeToolbarButtonAndMenuTooltip(EditToolbarButtonAlias.EDIT_AO_EXPORT, contextMenu, TOOLTIP_TEXT);
        holder.menuItem.setText(TOOLTIP_TEXT);
        contextMenu.addChild(holder.menuItem);
    }

    /**
     * Export der Einträge im Grid
     */
    private void doExport() {
        StringBuilder transJobAsCSV = createHeader();

        for (GuiTableRow row : getTable().getAllRows()) {
            if (row instanceof SimpleSelectSearchResultGrid.GuiTableRowWithAttributes) {
                transJobAsCSV.append(StrUtils.DW_NEWLINE); // Leerzeile
                DBDataObjectAttributes attributes = ((SimpleSelectSearchResultGrid.GuiTableRowWithAttributes)row).attributes;
                transJobAsCSV.append(createCSVRow(attributes));
            }
        }
        // Export mit Download Dialog
        EditDatabaseHelper.buildCSVAndDownload(transJobAsCSV, "Verwaltungstabelle.csv");
    }

    /**
     * Eine Grid-Zeile zu einer CSV-Zeile umwandeln
     * Benutzt wird die Standard-Routine für Attributwerte im Grid
     * Der HTML Text wird danach für den CSV-Export aufbereitet (convertToCSV)
     *
     * @param attributes
     */
    private StringBuilder createCSVRow(DBDataObjectAttributes attributes) {
        StringBuilder contentStringBuilder = new StringBuilder();
        for (EtkDisplayField field : getDisplayResultFields().getVisibleFields()) {
            if (contentStringBuilder.toString().length() > 0) {
                contentStringBuilder.append(SEPARATOR);
            }
            String tableName = field.getKey().getTableName();
            String fieldName = field.getKey().getFieldName();
            String value = "";
            DBDataObjectAttribute attribute = attributes.getField(fieldName, false);
            if (attribute != null) {
                value = getVisualValueOfFieldValue(tableName, fieldName, attribute, field.isMultiLanguage());
            }
            contentStringBuilder.append(convertToCSV(value));
        }
        return contentStringBuilder;
    }

    /**
     * Aufbereitung eines Cell-Textes (HTML) für CSV
     *
     * @param value
     * @return
     */
    private String convertToCSV(String value) {
        if (EC.isAlreadyHtml(value)) { //HTML-Text
            //Boolean-Text wird aus img-Tag kopiert
            if (DatatypeUtils.containsBooleanImageTag(value)) {
                value = DatatypeUtils.getBooleanValueFromBooleanImageTag(value, false);
            }
            value = HTMLUtils.removeHtmlTags(value);
            value = StrUtils.replaceNewlinesWithSpaces(value);
            value = StrUtils.convertHTMLtoString(value);
        } else {
            value = value.replace(OsUtils.NEWLINE, " ");
        }
        return value;
    }

    /**
     * Ausgabe der Grid-Header als CSV
     */
    private StringBuilder createHeader() {
        StringBuilder headerStringBuilder = new StringBuilder();
        String viewerLanguage = getConfig().getCurrentViewerLanguage();
        List<String> fallbackLanguages = getConfig().getDataBaseFallbackLanguages();
        EtkDisplayFields displayFields = getDisplayResultFields();
        for (EtkDisplayField displayField : displayFields.getVisibleFields()) {
            if (headerStringBuilder.toString().length() > 0) {
                headerStringBuilder.append(SEPARATOR);
            }
            EtkDatabaseField dbField = getProject().getFieldDescription(displayField.getKey().getTableName(), displayField.getKey().getFieldName());
            String labelText = displayField.isDefaultText() ?
                               dbField.getDisplayText(viewerLanguage, fallbackLanguages) : displayField.getText().getTextByNearestLanguage(viewerLanguage, fallbackLanguages);
            headerStringBuilder.append(labelText);
        }
        return headerStringBuilder;
    }
    //=========================================
    // Ende Export-Routinen
    //=========================================

    @Override
    protected void enableButtons() {
        super.enableButtons();

        // Behandlung des Export Toolbar Buttons
        boolean enabled = getTable().getRowCount() > 0;
        // das Popupmenu muss nicht aktualisiert werden, da es bei leerem Grid nicht aufgerufen werdenb lann
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_AO_EXPORT, null, enabled);
    }

    @Override
    protected EtkDisplayFields getSelectFields() {
        needsJoin = false;
        EtkDisplayFields selectedFields = super.getSelectFields();
        for (EtkDisplayField displayResultField : getDisplayResultFields().getFields()) {
            if (displayResultField.getKey().getTableName().equals(TABLE_DA_DICT_META) && !VirtualFieldsUtils.isVirtualField(displayResultField.getKey().getFieldName())) {
                selectedFields.addFeld(displayResultField);
                needsJoin = true;
            }
        }
        return selectedFields;
    }


    @Override
    protected EtkSqlCommonDbSelect buildQuery() {
        EtkSqlCommonDbSelect sqlSelect = super.buildQuery();
        // Query um nicht Translated-Status erweitern, falls nötig
        if (checkboxShowCompleted.isEnabled() && !checkboxShowCompleted.isSelected()) {
            String tableAndFieldName = TableAndFieldName.make(TABLE_DA_DICT_TRANS_JOB.toLowerCase(), FIELD_DTJ_TRANSLATION_STATE.toLowerCase());
            sqlSelect.getQuery().whereNot(new Condition(tableAndFieldName, Condition.OPERATOR_EQUALS, Condition.PARAMETER_SIGN, CaseMode.NOTHING));
            sqlSelect.addParamValue(TABLE_DA_DICT_TRANS_JOB.toLowerCase(), FIELD_DTJ_TRANSLATION_STATE.toLowerCase(),
                                    iPartsDictTransJobStates.TRANSLATED.getDbValue(), CaseMode.NOTHING);
        }
        addJoinIfNecessary(sqlSelect);
        return sqlSelect;
    }

    private void addJoinIfNecessary(EtkSqlCommonDbSelect sqlSelect) {
        if (needsJoin) {
            Condition joinCondition = new Condition(TableAndFieldName.make(TABLE_DA_DICT_TRANS_JOB, FIELD_DTJ_TEXTID),
                                                    Condition.OPERATOR_EQUALS,
                                                    new Fields(TableAndFieldName.make(TABLE_DA_DICT_META, FIELD_DA_DICT_META_TEXTID)));
            joinCondition.and(new Condition(TableAndFieldName.make(TABLE_DA_DICT_TRANS_JOB, FIELD_DTJ_TEXTKIND),
                                            Condition.OPERATOR_EQUALS,
                                            new Fields(TableAndFieldName.make(TABLE_DA_DICT_META, FIELD_DA_DICT_META_TXTKIND_ID))));
            sqlSelect.getQuery().join(new LeftOuterJoin(TABLE_DA_DICT_META, joinCondition));
        }
    }

    @Override
    protected void modifySearchControl(EtkDisplayField searchField, EditControl ctrl) {
        String fieldName = searchField.getKey().getFieldName();
        if (fieldName.equals(FIELD_DTJ_TRANSLATION_STATE)) {
            // Behandlung der RcomboBox für Status
            AbstractGuiControl statusCtrl = ctrl.getEditControl().getControl();
            // Rcombobox macht sich so breit wie der breiteste Text => unterbinden
            statusCtrl.setMaximumWidth(200);
            if (statusCtrl instanceof EnumRComboBox) {
                // Callback einhängen, damit die CheckBox bei Suche nach Status disabled wird
                statusCtrl.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
                    @Override
                    public void fire(Event event) {
                        boolean isEnabled = StrUtils.isEmpty(((EnumRComboBox)statusCtrl).getSelectedItem());
                        checkboxShowCompleted.setEnabled(isEnabled);
                    }
                });
            }
        } else if (fieldName.equals(FIELD_DTJ_SOURCE_LANG) || fieldName.equals(FIELD_DTJ_DEST_LANG)) {
            RComboBox langComboBox = createDaimlerLangCombo();
            langComboBox.setMaximumWidth(200);
            ctrl.getEditControl().getControl().copyEventListeners(langComboBox, Event.MENU_ITEM_EVENT);
            ctrl.getEditControl().setControl(langComboBox);
        } else if (fieldName.equals(FIELD_DTJ_TEXTKIND)) {
            RComboBox textKindComboBox = createTextKindCombo();
            textKindComboBox.setMaximumWidth(200);
            ctrl.getEditControl().getControl().copyEventListeners(textKindComboBox, Event.MENU_ITEM_EVENT);
            ctrl.getEditControl().setControl(textKindComboBox);
        }
    }

    private RComboBox createDaimlerLangCombo() {
        RComboBox langComboBox = new RComboBox();
        langComboBox.addItem("", "");
        List<Language> languages = iPartsLanguage.getAvailDaimlerLanguages(getProject());
        for (Language lang : languages) {
            langComboBox.addItem(lang, getComboText(lang));
        }
        langComboBox.setMaximumRowCount(Math.min(30, languages.size()));
        langComboBox.setSelectedIndex(0);
        return langComboBox;
    }

    private RComboBox createTextKindCombo() {
        RComboBox textKindComboBox = new RComboBox();
        textKindComboBox.addItem("", "");
        TreeMap<String, iPartsDataDictTextKind> textKinds = getSortedTextKindMap();
        for (Map.Entry<String, iPartsDataDictTextKind> textKind : textKinds.entrySet()) {
            textKindComboBox.addItem(textKind.getValue().getAsId().getTextKindId(), textKind.getKey());
        }
        textKindComboBox.setMaximumRowCount(Math.min(30, textKinds.size()));
        textKindComboBox.setSelectedIndex(0);
        return textKindComboBox;
    }

    private String getComboText(Language lang) {
        return lang.getCode() + " - " + TranslationHandler.translate(lang.getDisplayName());
    }

    @Override
    protected boolean doValidateAttributes(DBDataObjectAttributes attributes) {
        // Diese Routine wird beim Anlegen einer neuen Zeile im Grid aufgerufen
        // und dazu benutzt, die aktuelle textId und Quell-Sprache zu bestimmen
        // Hier wird auch die EtkMultiSprache zur TextId geladen und in der textMap gespeichert
        currentTextId = "";
        sourceLang = Language.DE.getCode();
        if (attributes.fieldExists(FIELD_DTJ_TEXTID)) {
            currentTextId = attributes.getFieldValue(FIELD_DTJ_TEXTID);
            if (StrUtils.isValid(currentTextId)) {
                EtkMultiSprache multi = textMap.get(currentTextId);
                if (multi == null) {
                    multi = getProject().getDbLayer().getLanguagesTextsByTextId(currentTextId);
                    textMap.put(currentTextId, multi);
                }
            }
        }
        if (attributes.fieldExists(FIELD_DTJ_SOURCE_LANG)) {
            sourceLang = attributes.getFieldValue(FIELD_DTJ_SOURCE_LANG);
        }
        return true;
    }

    @Override
    protected String getVisualValueOfFieldValue(String tableName, String fieldName, DBDataObjectAttribute fieldValue, boolean isMultiLanguage) {
        if (fieldName.equals(FIELD_DTJ_TEXTKIND)) {
            // Anzeige des TextArt-Textes und nicht der GUID
            return getTextKindName(fieldValue);
        }

        return super.getVisualValueOfFieldValue(tableName, fieldName, fieldValue, isMultiLanguage);
    }

    @Override
    protected String getValueForVirtualField(EtkDisplayField virtualField, DBDataObjectAttributes attributes) {
        // Spezialbehandlung für den Text zur TextId und Quellsprache
        if (virtualField.getKey().getFieldName().equals(iPartsDataVirtualFieldsDefinition.DTJ_SOURCE_TEXT)) {
            if (StrUtils.isValid(currentTextId)) {
                EtkMultiSprache multi = textMap.get(currentTextId);
                if (multi != null) {
                    return multi.getText(sourceLang);
                }
            }
            return "";
        }
        return super.getValueForVirtualField(virtualField, attributes);
    }

    /**
     * Liefert den Namen einer TextArt bzgl GUID (gecached)
     *
     * @param fieldValue
     * @return
     */
    private String getTextKindName(DBDataObjectAttribute fieldValue) {
        return getTextKindName(fieldValue.getAsString());
    }

    private String getTextKindName(String textKindNo) {
        String name = textKindNameMap.get(textKindNo);
        if (!StrUtils.isValid(name)) {
            iPartsDictTextKindId textKindId = new iPartsDictTextKindId(textKindNo);
            iPartsDataDictTextKind dataDictTextKind = DictTxtKindIdByMADId.getInstance(getProject()).findDictTextKindByTextKindId(textKindId, getProject());
            if (dataDictTextKind != null) {
                name = dataDictTextKind.getName(getProject().getDBLanguage());
                textKindNameMap.put(textKindNo, name);
            } else {
                name = textKindNo;
            }
        }
        return name;
    }


    /**
     * Eigene FilterFactory, damit die Spaltenfilter für SOURCE_/DEST_LANG als SetOfEnum erscheinen
     * (und die Sprachen richtig sortiert sind)
     */
    public class DictShowDataSearchFilterFactory extends SimpleMasterDataSearchFilterGrid.SimpleMasterDataSearchFilterFactory {

        public DictShowDataSearchFilterFactory(EtkProject project) {
            super(project);
        }

        @Override
        protected boolean changeColumnTableFilterValues(int column, EditControlFactory editControl) {
            if (editControl.getValues().editCreateMode == EditCreateMode.ecmTableColumnFilter) {
                String fieldName = editControl.getFieldName();
                List<String> filterNames = new DwList<>(SPECIAL_TABLEFILTER_FIELDS);
                if (filterNames.contains(fieldName)) {
                    // Trick um im Tabellenfilter ein SetOfEnum Eingabefeld zu erzeugen, das als Tokens
                    // die Werte aus der zugehörigen Spalte der Tabelle enthält
                    editControl.getValues().field = editControl.getField().cloneMe();  // zur Sicherheit, damit die Originalwerte sich nicht ändern
                    editControl.getValues().field.setType(EtkFieldType.feSetOfEnum);  // behaupte, das Feld ist ein SetOfEnum
                    editControl.getOptions().handleAsSetOfEnum = true;  // und soll als SetOfEnum behandelt werden
                    editControl.getOptions().searchDisjunctive = true;
                    // alles weitere übernimmt EditControlFactory und das FilterInterface
                    AbstractGuiControl guiCtrl = EditControlFactory.doCreateEnumCheckBoxForTableColumnFilter(editControl.getValues(), editControl.getOptions());
                    if (guiCtrl != null) {
                        editControl.setControl(guiCtrl);
                    }
                    List<String> langFilterNames = new DwList<>(SPECIAL_LANG_TABLEFILTER_FIELDS);
                    if (langFilterNames.contains(fieldName)) {
                        // Spezialbehandlung für Sprachen, da hier die Sortierung anders ist
                        if (super.changeColumnTableFilterValues(column, editControl)) {
                            SimpleMasterDataSearchFilterGrid.modifyLangTableFilterComboBox(getProject(), editControl.getControl());
                            return true;
                        }
                        return false;
                    } else if (fieldName.equals(FIELD_DTJ_TEXTKIND)) {
                        // Bei den TextArten steht die gefundenen GUIDs in der ComboBox
                        // => Umsortieren und Namen eintragen
                        if (super.changeColumnTableFilterValues(column, editControl)) {
                            modifyTextKindTableFilterComboBos(editControl.getControl());
                            return true;
                        }
                        return false;
                    }
                }
            }
            return super.changeColumnTableFilterValues(column, editControl);
        }

        /**
         * Gefundene TextArt GUID sortieren und den Namen anzeigebn
         *
         * @param control
         */
        private void modifyTextKindTableFilterComboBos(AbstractGuiControl control) {
            // Textart Texte einbauen
            if (control instanceof EnumCheckComboBox) {
                EnumCheckComboBox comboBox = (EnumCheckComboBox)control;
                List<String> items = comboBox.getItems();
                if (!items.isEmpty()) {
                    List<String> tokens = new DwList(comboBox.getTokens());
                    comboBox.removeAllItems();
                    TreeMap<String, iPartsDataDictTextKind> textKinds = getSortedTextKindMap();
                    for (Map.Entry<String, iPartsDataDictTextKind> textKind : textKinds.entrySet()) {
                        int index = items.indexOf(textKind.getValue().getAsId().getTextKindId());
                        if (index != -1) {
                            SimpleMasterDataSearchFilterGrid.addElemToComboBox(comboBox, tokens.get(index), getTextKindName(items.get(index)));
                        }
                    }
                }
            } else if (control instanceof EnumCheckRComboBox) {
                EnumCheckRComboBox comboBox = (EnumCheckRComboBox)control;
                List<String> items = comboBox.getItems();
                if (!items.isEmpty()) {
                    List<String> tokens = new DwList(comboBox.getTokens());
                    comboBox.removeAllItems();
                    TreeMap<String, iPartsDataDictTextKind> textKinds = getSortedTextKindMap();
                    for (Map.Entry<String, iPartsDataDictTextKind> textKind : textKinds.entrySet()) {
                        int index = items.indexOf(textKind.getValue().getAsId().getTextKindId());
                        if (index != -1) {
                            SimpleMasterDataSearchFilterGrid.addElemToComboBox(comboBox, tokens.get(index), getTextKindName(items.get(index)));
                        }
                    }
                }
            }
        }

        @Override
        protected List<SimpleSelectSearchResultGrid.GuiTableRowWithAttributes> calculateFilteredList() {
            List<SimpleSelectSearchResultGrid.GuiTableRowWithAttributes> entries = calculateFilteredListWithTableNameFromFilterItems();
            showHitCount(entries.size());
            return entries;
        }
    }
}
