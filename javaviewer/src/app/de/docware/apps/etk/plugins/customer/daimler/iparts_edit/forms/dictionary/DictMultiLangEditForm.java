/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.dictionary;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.db.sqlbuilder.EtkSqlCommonDbSelect;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.events.OnChangeEvent;
import de.docware.apps.etk.base.forms.events.OnDblClickEvent;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.common.EtkDataTextEntryList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.helper.DictTextSearchHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.iPartsDictConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.searchgrids.SelectSearchGridText;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBExtendedDataTypeProvider;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;
import de.docware.util.sql.terms.CaseMode;
import de.docware.util.sql.terms.Condition;
import de.docware.util.sql.terms.Fields;
import de.docware.util.sql.terms.InnerJoin;

import java.util.*;

/**
 * Formular für die Suche und Auswahl von wiederverwendbaren Texten in Abhängigkeit vom Lexikon
 */
public class DictMultiLangEditForm extends AbstractJavaViewerForm implements iPartsConst {

    static public EtkMultiSprache getMultiLangFromLangEditResult(EtkProject project,
                                                                 DictMultiLangResult result,
                                                                 String tableName) {
        DBExtendedDataTypeProvider provider = EtkDataObject.getTempExtendedDataTypeProvider(project, tableName);
        DBDataObjectAttribute attribute = new DBDataObjectAttribute(result.fieldName, DBDataObjectAttribute.TYPE.MULTI_LANGUAGE, false);
        attribute.setTextIdForMultiLanguage(result.textNr, result.textId, DBActionOrigin.FROM_DB);
        return attribute.getAsMultiLanguage(provider, true);
    }

    public static class DictMultiLangResult {

        public String textNr;
        public String textId;
        public String fieldName;
        public iPartsDataDictTextKind fromDictTextKind;
        public boolean isCreated;
    }

    public static EtkMultiSprache showDictMultiLangEditForCreate(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                                 Language searchLanguage, String initialSearchValue, DictTextKindTypes initialTextKindType,
                                                                 Collection<iPartsDataDictTextKind> textKindList, boolean onlyReleasedTexts,
                                                                 String foreignSourceForCreate, boolean dontCreateNewText,
                                                                 String tableName) {
        DictMultiLangResult result = showDictMultiLangEdit(dataConnector, parentForm, searchLanguage, initialSearchValue,
                                                           initialTextKindType, textKindList, onlyReleasedTexts, foreignSourceForCreate,
                                                           dontCreateNewText);
        if (result != null) {
            return getMultiLangFromLangEditResult(dataConnector.getProject(), result, tableName);
        }
        return null;
    }

    public static EtkMultiSprache showDictMultiLangEdit(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                        Language searchLanguage, String initialSearchValue, DictTextKindTypes initialTextKindType,
                                                        Collection<iPartsDataDictTextKind> textKindList, boolean onlyReleasedTexts,
                                                        boolean dontCreateNewText, String tableName) {
        DictMultiLangResult result = showDictMultiLangEdit(dataConnector, parentForm, searchLanguage, initialSearchValue,
                                                           initialTextKindType, textKindList, onlyReleasedTexts, dontCreateNewText);
        if (result != null) {
            return getMultiLangFromLangEditResult(dataConnector.getProject(), result, tableName);
        }
        return null;
    }

    public static DictMultiLangResult showDictMultiLangEdit(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                            Language searchLanguage, String initialSearchValue, DictTextKindTypes initialTextKindType,
                                                            Collection<iPartsDataDictTextKind> textKindList,
                                                            boolean onlyReleasedTexts, boolean dontCreateNewText) {
        return showDictMultiLangEdit(dataConnector, parentForm, searchLanguage, initialSearchValue, initialTextKindType,
                                     textKindList, onlyReleasedTexts, null, dontCreateNewText);
    }

    public static DictMultiLangResult showDictMultiLangEdit(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                            Language searchLanguage, String initialSearchValue, DictTextKindTypes initialTextKindType,
                                                            Collection<iPartsDataDictTextKind> textKindList,
                                                            boolean onlyReleasedTexts, String foreignSourceForCreate, boolean dontCreateNewText) {
        DictMultiLangEditForm dlg = new DictMultiLangEditForm(dataConnector, parentForm, searchLanguage, initialSearchValue,
                                                              initialTextKindType, textKindList, onlyReleasedTexts, dontCreateNewText);
        dlg.setSearchLanguage(searchLanguage);
        dlg.setForeignSourceForCreate(foreignSourceForCreate);
        dlg.setTitle(TranslationHandler.translate("!!Wiederverwendbare Texte im Lexikon suchen (%1)",
                                                  TranslationHandler.translate(dlg.getSearchLanguage().getDisplayName())));
        if (dlg.showModal() == ModalResult.OK) {
            DictMultiLangResult result = new DictMultiLangResult();
            result.textNr = dlg.getSelectedAttributes().getField(EtkDbConst.FIELD_S_TEXTNR).getAsString();
            result.textId = dlg.getSelectedAttributes().getField(EtkDbConst.FIELD_S_TEXTID).getAsString();
            result.fieldName = dlg.getSelectedAttributes().getField(EtkDbConst.FIELD_S_FELD).getAsString();
            result.fromDictTextKind = dlg.getSelectedDict();
            result.isCreated = dlg.isCreated();
            return result;
        }
        return null;
    }

    protected boolean onlyReleasedTexts;
    protected DictSelectSearchGridText selectSearchGridText;
    private OnDblClickEvent doExternalDblClick;
    private DBDataObjectAttributes createdAttributes;
    private String foreignSourceForCreate;

    /**
     * Erzeugt eine Instanz von DictMultiLangEditForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public DictMultiLangEditForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                 Language searchLanguage, String initialSearchValue, DictTextKindTypes initialTextKindType,
                                 Collection<iPartsDataDictTextKind> textKindList, boolean onlyReleasedTexts, boolean dontCreateNewText) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.onlyReleasedTexts = onlyReleasedTexts;
        this.doExternalDblClick = null;
        this.createdAttributes = null;
        postCreateGui(dontCreateNewText);
        fillTextKinds(initialTextKindType, textKindList);
        setSearchLanguage(searchLanguage);
        if (onlyReleasedTexts) { // Textart für Text-Cache nur dann setzen, falls nur nach freigegebenen Texten gesucht wird
            selectSearchGridText.setTextKind(initialTextKindType);
        }
        selectSearchGridText.setInitialSearchValue(initialSearchValue);

        enableButtons();
    }

    protected DictSelectSearchGridText createSearchGrid() {
        return new DictSelectSearchGridText(getConnector(), parentForm);
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    protected void postCreateGui(boolean dontCreateNewText) {
        selectSearchGridText = createSearchGrid();
        selectSearchGridText.setSortEnabled(false);
        OnChangeEvent doChangeEvent = new OnChangeEvent() {
            @Override
            public void onChange() {
                enableButtons();
            }
        };
        selectSearchGridText.setOnChangeEvent(doChangeEvent);
        OnDblClickEvent doDblClick = new OnDblClickEvent() {
            @Override
            public void onDblClick() {
                if (doExternalDblClick != null) {
                    selectSearchGridText.doEndSearch();
                    doExternalDblClick.onDblClick();
                } else {
                    GuiButtonOnPanel okButton = mainWindow.buttonpanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK);
                    if (okButton.isEnabled()) {
                        selectSearchGridText.doEndSearch();
                        okButton.doClick();
                    }
                }
            }
        };
        selectSearchGridText.setOnDblClickEvent(doDblClick);
        if (J2EEHandler.isJ2EE()) {
            selectSearchGridText.setMaxResults(iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE);
        }

        mainWindow.panelGrid.addChildBorderCenter(selectSearchGridText.getGui());
        setWithNewText(dontCreateNewText);
        selectSearchGridText.requestFocusForSearchValue();
    }

    protected void setWithNewText(boolean dontCreateNewText) {
        // DAIMLER-8537: Recht abfragen, ob Anlage neuer Text erlaubt
        mainWindow.buttonNewText.setVisible((DictShowTextKindForm.isCurrentUserProductAdministrator() ||
                                             DictShowTextKindForm.isCurrentUserDataAdministrator()) &&
                                            !dontCreateNewText);
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

    public Language getSearchLanguage() {
        return selectSearchGridText.getSearchLanguageForTextKind();
    }

    public void setSearchLanguage(Language searchLanguage) {
        selectSearchGridText.setSearchLanguage(searchLanguage);
    }

    public void setForeignSourceForCreate(String foreignSourceForCreate) {
        this.foreignSourceForCreate = foreignSourceForCreate;
    }

    public boolean isCreated() {
        return createdAttributes != null;
    }

    public DBDataObjectAttributes getSelectedAttributes() {
        if (createdAttributes != null) {
            return createdAttributes;
        } else {
            return selectSearchGridText.getSelectedAttributes();
        }
    }

    public OnDblClickEvent getDoExternalDblClick() {
        return doExternalDblClick;
    }

    public void setDoExternalDblClick(OnDblClickEvent doExternalDblClick) {
        this.doExternalDblClick = doExternalDblClick;
    }

    /**
     * Weiteres Popup-Menu zum SearchGrid hinzufügen
     *
     * @param child
     */
    public void addMenuEntryToGrid(AbstractGuiControl child) {
        selectSearchGridText.addMenuEntryToGrid(child);
    }

    protected iPartsDataDictTextKind getSelectedDict() {
        return mainWindow.comboboxTextKind.getSelectedUserObject();
    }

    protected DictTextKindTypes getSelectedTextKind() {
        iPartsDataDictTextKind dataDictTextKind = getSelectedDict();
        if (dataDictTextKind != null) {
            return dataDictTextKind.getForeignTextKindType();
        } else {
            return DictTextKindTypes.UNKNOWN;
        }
    }

    protected void enableButtons() {
        boolean isGridSelected = (selectSearchGridText.getSelectedAttributes() != null);
        mainWindow.buttonpanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, isGridSelected);
    }

    /**
     * Auswahl des Lexikon-Art hat sich geändert
     *
     * @param event
     */
    protected void comboTextKindChanged(Event event) {
        selectSearchGridText.doEndSearch();
        if (onlyReleasedTexts) { // Textart für Text-Cache nur dann setzen, falls nur nach freigegebenen Texten gesucht wird
            selectSearchGridText.setTextKind(getSelectedTextKind());
        }
        selectSearchGridText.doStartSearch();
    }

    protected void onCreateNewText(Event event) {
        selectSearchGridText.doEndSearch();
        Set<DictTextKindTypes> allowedTextKindTypes = new HashSet<>();
        iPartsDataDictTextKind dataTextKind = mainWindow.comboboxTextKind.getSelectedUserObject();
        iPartsDictTextKindId textKindId = null;
        if (dataTextKind != null) {
            textKindId = dataTextKind.getAsId();
            allowedTextKindTypes.add(dataTextKind.getForeignTextKindType());
        }
        List<String> allowedEditLangs = new DwList<>();
        for (Language lang : iPartsDictTransJobHelper.getSourceLanguages()) {
            allowedEditLangs.add(lang.getCode());
        }

        iPartsDictMetaId dictMetaId = DictCreateTextIdForm.createNewTextId(getConnector(), this, allowedTextKindTypes, textKindId,
                                                                           foreignSourceForCreate, allowedEditLangs);
        if (dictMetaId != null) {
            // DAIMLER-8537: Neuen Text anzeigen (geht nur, wenn beim Erzeugen die getSearchLanguage() einen Text hat
            iPartsDataDictMeta dataDictMeta = new iPartsDataDictMeta(getProject(), dictMetaId);
            if (dataDictMeta.existsInDB()) {
                EtkMultiSprache multi = dataDictMeta.getMultiLang();
                EtkDataTextEntryList textEntryList = DictTextSearchHelper.searchTextAttributesInTableSprache(getProject(),
                                                                                                             multi.getText(getSearchLanguage().getCode()),
                                                                                                             multi.getTextId(),
                                                                                                             getSearchLanguage());
                if (!textEntryList.isEmpty()) {
                    createdAttributes = textEntryList.get(0).getAttributes();
                    GuiButtonOnPanel okButton = mainWindow.buttonpanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK);
                    okButton.doClick();
                }
            }
        }
    }

    /**
     * Befüllt die Textarten - Combobox
     */
    protected void fillTextKinds(DictTextKindTypes initialTextKindType, Collection<iPartsDataDictTextKind> textKindList) {
        mainWindow.comboboxTextKind.switchOffEventListeners();
        mainWindow.comboboxTextKind.removeAllItems();
        if (textKindList != null) {
            String language = getProject().getConfig().getCurrentViewerLanguage();
            for (iPartsDataDictTextKind dataDictTextKind : textKindList) {
                mainWindow.comboboxTextKind.addItem(dataDictTextKind, dataDictTextKind.getName(language));
            }
        } else {
            TreeMap<String, iPartsDataDictTextKind> textKinds = iPartsDataDictTextKindList.loadAllTextKindListSortedByName(getProject());
            for (Map.Entry<String, iPartsDataDictTextKind> textKind : textKinds.entrySet()) {
                mainWindow.comboboxTextKind.addItem(textKind.getValue(), textKind.getKey());
            }
        }
        mainWindow.comboboxTextKind.switchOnEventListeners();
        mainWindow.comboboxTextKind.setEnabled(mainWindow.comboboxTextKind.getItemCount() > 1);
        if (mainWindow.comboboxTextKind.getItemCount() > 0) {
            if ((initialTextKindType != null) && (initialTextKindType != DictTextKindTypes.UNKNOWN)) {
                for (iPartsDataDictTextKind dataDictTextKind : mainWindow.comboboxTextKind.getUserObjects()) {
                    if (dataDictTextKind.getForeignTextKindType() == initialTextKindType) {
                        mainWindow.comboboxTextKind.setSelectedUserObject(dataDictTextKind);
                        return;
                    }
                }
            }
            mainWindow.comboboxTextKind.setSelectedIndex(0);
        }
    }


    /**
     * private Klasse (Ableitung von {@link SelectSearchGridText}) mit dem veränderten Query
     */
    protected class DictSelectSearchGridText extends SelectSearchGridText {

        public DictSelectSearchGridText(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
            super(dataConnector, parentForm);
            setAutoSelectSingleSearchResult(true);
        }

        public void doEndSearch() {
            endSearch();
        }

        public void doStartSearch() {
            startSearch();
        }

        public void setInitialSearchValue(String searchValue) {
            if (!StrUtils.isEmpty(searchValue)) {
                setSearchValue(searchValue);
            }
        }

        public void addMenuEntryToGrid(AbstractGuiControl child) {
            addMenuEntry(child);
        }

        public void showSearchField(boolean visible) {
            super.showSearchField(visible);
        }

        public void setSearchValue(String value) {
            super.setSearchValue(value);
        }

        public String getSearchValue() {
            return super.getSearchValue();
        }

        @Override
        protected EtkSqlCommonDbSelect buildQuery(String searchValue) {
            EtkSqlCommonDbSelect sqlSelect = super.buildQuery(searchValue);
            iPartsDataDictTextKind dataTextKind = getSelectedDict();
            Condition condition = new Condition(TableAndFieldName.make(searchTable, iPartsConst.FIELD_S_TEXTID), Condition.OPERATOR_EQUALS,
                                                new Fields(TableAndFieldName.make(iPartsConst.TABLE_DA_DICT_META, iPartsConst.FIELD_DA_DICT_META_TEXTID)));
            condition.and(buildConditionWithValue(sqlSelect, iPartsConst.TABLE_DA_DICT_META, iPartsConst.FIELD_DA_DICT_META_TXTKIND_ID,
                                                  Condition.OPERATOR_EQUALS, dataTextKind.getAsId().getTextKindId(), CaseMode.NOTHING));

            if (onlyReleasedTexts) { // Nur freigegebene Texte?
                condition.and(buildConditionWithValue(sqlSelect, iPartsConst.TABLE_DA_DICT_META, iPartsConst.FIELD_DA_DICT_META_STATE,
                                                      Condition.OPERATOR_EQUALS, iPartsDictConst.DICT_STATUS_RELEASED, CaseMode.NOTHING));
            }

            sqlSelect.getQuery().join(new InnerJoin(iPartsConst.TABLE_DA_DICT_META, condition));

            return sqlSelect;
        }

        public void setSortEnabled(boolean sortEnabled) {
            getTable().setSortEnabled(sortEnabled);
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(96);
    }

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
        private de.docware.framework.modules.gui.controls.GuiButton buttonNewText;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel DNQ25898;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelGrid;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
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
            panelTop = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelTop.setName("panelTop");
            panelTop.__internal_setGenerationDpi(96);
            panelTop.registerTranslationHandler(translationHandler);
            panelTop.setScaleForResolution(true);
            panelTop.setMinimumWidth(10);
            panelTop.setMinimumHeight(10);
            panelTop.setTitle("!!Textart Auswahl");
            de.docware.framework.modules.gui.layout.LayoutGridBag panelTopLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelTop.setLayout(panelTopLayout);
            comboboxTextKind = new de.docware.framework.modules.gui.controls.GuiComboBox<de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictTextKind>();
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
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 100.0, "c", "b", 4, 4, 4, 4);
            comboboxTextKind.setConstraints(comboboxTextKindConstraints);
            panelTop.addChild(comboboxTextKind);
            buttonNewText = new de.docware.framework.modules.gui.controls.GuiButton();
            buttonNewText.setName("buttonNewText");
            buttonNewText.__internal_setGenerationDpi(96);
            buttonNewText.registerTranslationHandler(translationHandler);
            buttonNewText.setScaleForResolution(true);
            buttonNewText.setMinimumWidth(21);
            buttonNewText.setMinimumHeight(10);
            buttonNewText.setMaximumWidth(21);
            buttonNewText.setMaximumHeight(21);
            DNQ25898 = new de.docware.framework.modules.gui.controls.GuiLabel();
            DNQ25898.setName("DNQ25898");
            DNQ25898.__internal_setGenerationDpi(96);
            DNQ25898.registerTranslationHandler(translationHandler);
            DNQ25898.setScaleForResolution(true);
            DNQ25898.setText("!!Neuen Text anlegen");
            buttonNewText.setTooltip(DNQ25898);
            buttonNewText.setFontStyle(de.docware.framework.modules.gui.controls.misc.DWFontStyle.BOLD);
            buttonNewText.setMnemonicEnabled(true);
            buttonNewText.setText("+");
            buttonNewText.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onCreateNewText(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag buttonNewTextConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, "c", "n", 0, 0, 0, 4);
            buttonNewText.setConstraints(buttonNewTextConstraints);
            panelTop.addChild(buttonNewText);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelTopConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelTopConstraints.setPosition("north");
            panelTop.setConstraints(panelTopConstraints);
            panelMain.addChild(panelTop);
            panelGrid = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelGrid.setName("panelGrid");
            panelGrid.__internal_setGenerationDpi(96);
            panelGrid.registerTranslationHandler(translationHandler);
            panelGrid.setScaleForResolution(true);
            panelGrid.setMinimumWidth(10);
            panelGrid.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelGridLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelGrid.setLayout(panelGridLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelGridConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
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