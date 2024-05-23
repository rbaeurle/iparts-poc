/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataCombText;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataCombTextList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsCombTextHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditCombTextHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiSplitPane;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventCreator;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerFireOnce;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.responsive.base.theme.ThemeManager;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.Utils;

import java.util.*;

/**
 * Control zum Bearbeiten der sprachabhängigen und sprachneutralen Textbausteine eines kombinierten Text
 */
public class iPartsGuiCombTextCompleteEditControl extends GuiPanel implements iPartsConst {

    public static final String TYPE = "iPartsGuiCombTextCompleteEditControl";

    // Enum um Unterscheiden zu können, ob die Texte vom Autor gesetzt wurden oder über das Form kamen
    enum TextChangeSource {
        EDIT_FORM, INPLACE_EDITOR

    }

    private iPartsMultiLangGuiCombTextButtonField multiCombTextField;
    private iPartsNeutralGuiCombTextButtonField neutralCombTextField;
    private AbstractJavaViewerFormIConnector connector;
    private PartListEntryId partListEntryId;
    private GuiSplitPane guiSplitPaneForControls;
    private Map<String, EditCombinedTextForm.TextKindType> textKinds;
    private String neutralTextFromPart;
    private boolean isMultiEdit;
    private boolean isForwardESCKeyReleasedEvent;
    private boolean isForwardENTERKeyReleasedEvent;
    private TextChangeSource currentTextChangeSource;
    private EnumSet<DictTextKindTypes> searchTextKindTypes;

    public iPartsGuiCombTextCompleteEditControl() {
        super();
        type = TYPE;
        init();
    }

    private void init() {
        AbstractGuiCombTextButtonField.CombTextChangeCallback callBack = createOnButtonClickCallback();
        multiCombTextField = new iPartsMultiLangGuiCombTextButtonField();
        multiCombTextField.setButtonVisible(false);
        multiCombTextField.setCombTextChangeCallback(callBack);
        neutralCombTextField = new iPartsNeutralGuiCombTextButtonField();
        neutralCombTextField.setCombTextChangeCallback(callBack);
        setLayout(new LayoutBorder());
        guiSplitPaneForControls = new GuiSplitPane();
        guiSplitPaneForControls.setHorizontal(true);
        guiSplitPaneForControls.setDividerSize(2);
        guiSplitPaneForControls.addEventListener(new EventListenerFireOnce(Event.ON_RESIZE_EVENT, guiSplitPaneForControls) {
            @Override
            public boolean isFireOnceValid(Event event) {
                if (guiSplitPaneForControls.getUniqueId().endsWith(event.getReceiverId())) {
                    int newWidth = event.getIntParameter(Event.EVENT_PARAMETER_NEWWIDTH);
                    if (newWidth > 0) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void fireOnce(Event event) {
                int newWidth = event.getIntParameter(Event.EVENT_PARAMETER_NEWWIDTH);
                guiSplitPaneForControls.setDividerPosition((int)(newWidth * 0.5));
            }
        });
        addChildBorder(guiSplitPaneForControls, LayoutBorder.POSITION_CENTER);
        guiSplitPaneForControls.setFirstChild(multiCombTextField);
        guiSplitPaneForControls.setSecondChild(neutralCombTextField);
        ThemeManager.get().render(this);
    }

    @Override
    public boolean isCompositeControl() {
        return true;
    }

    @Override
    public void requestFocus() {
        getMultiCombTextField().requestFocus();
    }

    /**
     * Liefert alle Textbausteine des kombinierten Textes aus beiden {@link AbstractGuiCombTextButtonField} Controls.
     * Es werden zuerst die sprachabhängigen Teile hinzugefügt und dann die sprachneutralen. Danach werden die Inhalte nach
     * ihrer Sequenznummer sortiert und, wenn benötigt, die laufenden Nummern korrigiert.
     *
     * @return
     */
    public iPartsDataCombTextList getAllCombTexts(boolean correctSeqNo) {
        List<iPartsDataCombText> combList = new ArrayList<>();
        // Sprachabhängigen Teile aufsammeln
        iPartsDataCombTextList multiCombTextList = getMultiCombTextField().getDataCombTextList();
        List<iPartsDataCombText> deleteList = new ArrayList<>();
        if (multiCombTextList != null) {
            // Die gelöschten Elemente aufsammeln
            deleteList.addAll(multiCombTextList.getDeletedList());
            // Die eigentlichen Objekte in einer eigenen Liste sammeln um später zu sortieren
            combList.addAll(multiCombTextList.getAsList());
        }
        // Sprachunabhängigen Teile aufsammeln
        iPartsDataCombTextList neutralCombTextList = getNeutralCombTextField().getDataCombTextList();
        if (neutralCombTextList != null) {
            // Die gelöschten Elemente aufsammeln
            deleteList.addAll(neutralCombTextList.getDeletedList());

            // Den deutschen Text für die weitere Anzeige und Bearbeitung in alle DB-Sprachen kopieren
            List<iPartsDataCombText> neutralCombTexts = neutralCombTextList.getAsList();
            List<String> databaseLanguages = connector.getProject().getConfig().getDatabaseLanguages();
            for (iPartsDataCombText neutralCombText : neutralCombTexts) {
                EtkMultiSprache multiLang = neutralCombText.getFieldValueAsMultiLanguage(FIELD_DCT_DICT_TEXT);
                multiLang.fillAllLanguages(databaseLanguages, Language.DE);
                neutralCombText.setFieldValueAsMultiLanguage(FIELD_DCT_DICT_TEXT, multiLang, DBActionOrigin.FROM_EDIT);
            }

            // Die eigentlichen Objekte in einer eigenen Liste sammeln um später zu sortieren
            combList.addAll(neutralCombTexts);
        }

        iPartsCombTextHelper.sortCombTextByTextSeqNo(combList);

        iPartsDataCombTextList result = new iPartsDataCombTextList();
        if (correctSeqNo) {
            Map<String, EtkMultiSprache> textSeqNoToCombText = new LinkedHashMap<>();
            int textSeqNo = 0;
            for (iPartsDataCombText combText : combList) {
                textSeqNo++;
                textSeqNoToCombText.put(EtkDbsHelper.formatLfdNr(textSeqNo), combText.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DCT_DICT_TEXT));
            }
            // gelöschte Elemente ggf. wiederverwenden - werden erkannt, da sie nicht in textSeqNoToCombText vorhanden sind
            // Beim MultiEdit dürfen die Platzhalter nicht übergeben werden
            if (isMultiEdit()) {
                for (iPartsDataCombText combText : deleteList) {
                    if (!combText.getFieldValueAsMultiLanguage(FIELD_DCT_DICT_TEXT).getTextId().equals(AbstractGuiCombTextButtonField.KEEP_TEXTS_ENTRY_ID)) {
                        combList.addAll(deleteList);
                    }
                }
            } else {
                combList.addAll(deleteList);
            }
            // Texte auf bestehende Objekte verteilen bzw neue anlegen oder alte löschen
            iPartsCombTextHelper.handleCombTextsWithOrder(connector.getProject(), partListEntryId, textSeqNoToCombText, combList, result);
        } else {
            result.addAll(combList, DBActionOrigin.FROM_EDIT);
        }
        return result;
    }

    /**
     * Liefert den neutralen Text vom Teilestamm zum aktuellen Stücklisteneintrag
     *
     * @return
     */
    private String getNeutralTextFromPart() {
        if (neutralTextFromPart != null) {
            return neutralTextFromPart;
        }

        if (connector == null) {
            return null;
        }

        // Im Multi-Edit nicht in die DB gehen
        if (!isMultiEdit) {
            EtkDataPartListEntry partListEntry = EtkDataObjectFactory.createDataPartListEntry(connector.getProject(), partListEntryId);
            neutralTextFromPart = partListEntry.getPart().getFieldValue(iPartsConst.FIELD_M_ADDTEXT, Language.DE.getCode(), false); // Sprache egal
            return neutralTextFromPart;
        }

        return "";
    }

    public void setConnector(AbstractJavaViewerFormIConnector connector) {
        this.connector = connector;
        getMultiCombTextField().setConnector(connector);
        getNeutralCombTextField().setConnector(connector);
        textKinds = EditCombinedTextForm.loadTextKinds(connector.getProject());
    }

    public void setPartListEntryId(PartListEntryId partListEntryId) {
        setPartListEntryId(partListEntryId, null);
    }

    /**
     * Setzt die {@link PartListEntryId} und lädt den dazugehörigen kombinierten Text. Zusätzlich werden die Elemente des
     * geladenen Texts auf die jeweiligen Controls verteilt.
     *
     * @param partListEntryId
     * @param combTextsExtern
     */
    public void setPartListEntryId(PartListEntryId partListEntryId, iPartsDataCombTextList combTextsExtern) {
        neutralCombTextField.setButtonVisible(neutralCombTextField.getConnector() != null);
        if (!Utils.objectEquals(this.partListEntryId, partListEntryId)) {
            this.partListEntryId = partListEntryId;
            // ID setzen
            getMultiCombTextField().setPartListEntryId(partListEntryId);
            getNeutralCombTextField().setPartListEntryId(partListEntryId);
            // kombinierten Text bestimmen - entweder von extern (bei MultiEdit) oder aus DB laden
            iPartsDataCombTextList combTexts = (combTextsExtern != null) ? combTextsExtern : iPartsDataCombTextList.loadForPartListEntry(partListEntryId, null, connector.getProject());
            // kombinierten Text auf Controls verteilen
            // Text nur setzen, wenn es sich um den MultiEdit handelt. Sonst wird der Text von außen gesetzt
            setCombDataAndText(combTexts, combTextsExtern != null);
        }
    }

    /**
     * Verteilt die Elemente des kombinierten Texts an die jeweiligen Controls und setzt die richtigen Textbausteine
     *
     * @param newDataCombTextList
     * @param doSetText
     */
    private void setCombDataAndText(iPartsDataCombTextList newDataCombTextList, boolean doSetText) {
        if (newDataCombTextList != null) {
            setDataCombTextList(newDataCombTextList);
            if (doSetText) {
                setText(getConstructedCombinedText());
            }
        }
    }

    /**
     * Liefert den konstruierten kombinierten Text auf Basis aller Textbausteine aus beiden {@link AbstractGuiCombTextButtonField}s
     *
     * @return
     */
    private String getConstructedCombinedText() {
        if ((partListEntryId != null) && (connector != null)) {
            Map<String, String> neutralTextsFromPartForModule = null;
            String neutralTextFromPart = getNeutralTextFromPart();
            // Neutralen Text vom Stamm holen
            if (StrUtils.isValid(neutralTextFromPart)) {
                neutralTextsFromPartForModule = new HashMap<>(1); // k_lfdnr -> neutraler Text
                neutralTextsFromPartForModule.put(partListEntryId.getKLfdnr(), neutralTextFromPart);
            }
            // Alle aktuellen Textbausteine holen
            iPartsDataCombTextList combTexts = getAllCombTexts(false);
            List<iPartsDataCombText> combTextList = combTexts.getAsList();
            // Nach ihrer Reihenfolge sortieren
            iPartsCombTextHelper.sortCombTextByTextSeqNo(combTextList);
            combTexts.clear(DBActionOrigin.FROM_DB);
            combTexts.addAll(combTextList, DBActionOrigin.FROM_DB);
            // Kombinierte Textelemente bestimmen
            Map<String, String> combTextMap = combTexts.getCombTexts(neutralTextsFromPartForModule, connector.getProject());
            return combTextMap.get(partListEntryId.getKLfdnr());
        }
        return null;
    }

    /**
     * Setzt die einzelnen Textbausteine des übergebenen kombinierten Text. Zusätzlich werden die einzelnen Bestandteile
     * bestimmt und in einzelne Liste abgelegt. Die beiden {@link AbstractGuiCombTextButtonField} Controls werden mit
     * den jeweiligen Textbausteinen befüllt.
     *
     * @param completeCombTexts
     */
    public void setDataCombTextList(iPartsDataCombTextList completeCombTexts) {
        neutralTextFromPart = null;
        // Leere Liste nur setzen, wenn MultiEdit
        if (!completeCombTexts.isEmpty() || isMultiEdit() || (currentTextChangeSource == TextChangeSource.EDIT_FORM)) {
            // Erst die gelöschten Texte nach Textart sortieren
            Map<DictTextKindTypes, iPartsDataCombTextList> combTextsForTextKind = iPartsEditCombTextHelper.getTextKindToCombTextMap(connector.getProject(), completeCombTexts, getTextKinds());
            // Elemente setzen abhängig von Textart
            iPartsDataCombTextList currentMultiCombTextList = combTextsForTextKind.get(DictTextKindTypes.ADD_TEXT);
            iPartsDataCombTextList currentNeutralCombTextList = combTextsForTextKind.get(DictTextKindTypes.NEUTRAL_TEXT);
            getMultiCombTextField().setDataCombTextList(currentMultiCombTextList);
            getNeutralCombTextField().setDataCombTextList(currentNeutralCombTextList);
        } else {
            // Controls leeren
            clearCombTextField(getMultiCombTextField());
            clearCombTextField(getNeutralCombTextField());
        }
    }

    /**
     * Entfernmt alle kombinierten Textelemente aus dem übergebenen {@link AbstractGuiCombTextButtonField} Control
     *
     * @param combTextField
     */
    private void clearCombTextField(AbstractGuiCombTextButtonField combTextField) {
        if ((combTextField.getDataCombTextList() != null) && !combTextField.getDataCombTextList().isEmpty()) {
            combTextField.setDataCombTextList(null);
        }
    }

    /**
     * Zerlegt den übergebenen Text in seine Textelemente und setzt ihn dann entsprechend in das jeweilige Control.
     *
     * @param textValue
     */
    public void setText(String textValue) {
        String currentText = (textValue != null) ? textValue : "";
        List<String> multiText = new ArrayList<>();
        List<String> neutralText = new ArrayList<>();
        if (StrUtils.isValid(currentText)) {
            fillTextListWithTextElements(getMultiCombTextField().getDataCombTextList(), multiText);
            fillTextListWithTextElements(getNeutralCombTextField().getDataCombTextList(), neutralText);
        }
        if (!isMultiEdit()) {
            getMultiCombTextField().resetFilterText();
        }
        getMultiCombTextField().setText(StrUtils.stringListToString(multiText, " "));
        if (!isMultiEdit()) {
            getNeutralCombTextField().resetFilterText();
        }
        getNeutralCombTextField().setText(StrUtils.stringListToString(neutralText, " "));
        setToolTip(currentText);
    }

    private void setToolTip(String currentText) {
        getMultiCombTextField().setToolTip(currentText);
        getNeutralCombTextField().setToolTip(currentText);
    }

    /**
     * Befüllt für die aktuelle DB-Sprache die übergebene <code>multiTextElements</code> Liste mit den echten Textwerten
     * aus <code>combTextList</code>
     *
     * @param combTextList
     * @param multiTextElements
     * @return
     */
    private void fillTextListWithTextElements(iPartsDataCombTextList combTextList, List<String> multiTextElements) {
        if (combTextList != null) {
            for (iPartsDataCombText multiObject : combTextList) {
                String text = multiObject.getTextValue(iPartsConst.FIELD_DCT_DICT_TEXT, connector.getProject().getDBLanguage());
                if (multiTextElements != null) {
                    multiTextElements.add(text);
                }
            }
        }
    }

    /**
     * Erzeugt den {@link de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.AbstractGuiCombTextButtonField.CombTextChangeCallback}
     * für das Klicken des Buttons im {@link AbstractGuiCombTextButtonField}.
     *
     * @return
     */
    private AbstractGuiCombTextButtonField.CombTextChangeCallback createOnButtonClickCallback() {
        return new AbstractGuiCombTextButtonField.CombTextChangeCallback() {

            @Override
            public void onButtonClick() {
                // Nicht direkt dataCombTextList setzen, weil bei Abbrechen in EditCombinedTextForm ansonsten dataCombTextList
                // auf null gesetzt werden würde evtl. schon VOR dem Speichern in der DB/ChangeSet
                iPartsDataCombTextList allCombText = getAllCombTexts(true);
                // Beim MultiEdit, dürfen die Platzhalter nicht an das Form übergeben werden
                if (isMultiEdit()) {
                    Iterator<iPartsDataCombText> iterator = allCombText.iterator();
                    while (iterator.hasNext()) {
                        iPartsDataCombText combText = iterator.next();
                        if (AbstractGuiCombTextButtonField.isPseudoCombText(combText)) {
                            iterator.remove();
                        }
                    }
                }
                iPartsDataCombTextList newDataCombTextList = EditCombinedTextForm.showEditCombinedText(connector, partListEntryId,
                                                                                                       allCombText, searchTextKindTypes,
                                                                                                       isEnabled(),
                                                                                                       isMultiEdit());
                if (newDataCombTextList != null) {
                    // Damit die Checkbox gesetzt wird
                    fireEvent(EventCreator.createOnChangeEvent(eventHandlerComponent, uniqueId));
                }
                // Quelle setzen
                currentTextChangeSource = TextChangeSource.EDIT_FORM;
                setCombDataAndText(newDataCombTextList, true);
            }

            @Override
            public int getNextSeqNo() {
                int index = 1;
                if (getMultiCombTextField().getDataCombTextList() != null) {
                    index += getMultiCombTextField().getDataCombTextList().size();
                }
                if (getNeutralCombTextField().getDataCombTextList() != null) {
                    index += getNeutralCombTextField().getDataCombTextList().size();
                }
                return index;
            }

            @Override
            public void textChangeFromSearch(boolean newDataObjectCreated) {
                if (currentTextChangeSource != TextChangeSource.INPLACE_EDITOR) {
                    currentTextChangeSource = TextChangeSource.INPLACE_EDITOR;
                }
                // Wurde ein neues Objekt angelegt -> Check, ob es sich um einen Sonderfall handelt, bei dem
                // die Textpositionen vertauscht werden müssen
                if (newDataObjectCreated) {
                    checkIfTextNeedsToBeSwitched();
                }
                setToolTip(getConstructedCombinedText());
            }
        };
    }

    public void setSearchTextKindTypes(EnumSet<DictTextKindTypes> searchTextKindTypes) {
        this.searchTextKindTypes = searchTextKindTypes;
    }

    /**
     * Check, ob die Textposition von sprachabhängigen und sprachneutralen Text getauscht werden soll. Das ist der Fall,
     * wenn es genau ein Textelemente von beiden Typen gibt und der sprachabhängige Text NACH dem sprachneutralen Text
     * erzeugt wurde.
     */
    private void checkIfTextNeedsToBeSwitched() {
        iPartsMultiLangGuiCombTextButtonField multiField = getMultiCombTextField();
        iPartsNeutralGuiCombTextButtonField neutralField = getNeutralCombTextField();
        if ((multiField != null) && (neutralField != null)) {
            iPartsDataCombTextList multiTexts = multiField.getDataCombTextList();
            iPartsDataCombTextList neutralTexts = neutralField.getDataCombTextList();
            if ((multiTexts != null) && (neutralTexts != null) && (multiTexts.size() == 1) && (neutralTexts.size() == 1)) {
                iPartsDataCombText multiCombText = multiTexts.get(0);
                // Aktuelle Textposition sprachabhängiger Text
                int multiSeqNo = StrUtils.strToIntDef(multiCombText.getAsId().getTextSeqNo(), -1);
                iPartsDataCombText neutralCombText = neutralTexts.get(0);
                // Aktuelle Textposition sprachneutraler Text
                int neutralSeqNo = StrUtils.strToIntDef(neutralCombText.getAsId().getTextSeqNo(), -1);
                // Wenn der neutrale Text in dieser Methode vor dem sprachabhängigen text steht, dann müssen die Texte
                // vertauscht werden, da wir hier nur reinkommen, wenn ein neuer Text via InplaceEditor erzeugt wurde.
                if (neutralSeqNo <= multiSeqNo) {
                    EtkMultiSprache multiText = multiCombText.getFieldValueAsMultiLanguage(FIELD_DCT_DICT_TEXT);
                    EtkMultiSprache neutralText = neutralCombText.getFieldValueAsMultiLanguage(FIELD_DCT_DICT_TEXT);
                    multiCombText.setFieldValueAsMultiLanguage(FIELD_DCT_DICT_TEXT, neutralText, DBActionOrigin.FROM_EDIT);
                    neutralCombText.setFieldValueAsMultiLanguage(FIELD_DCT_DICT_TEXT, multiText, DBActionOrigin.FROM_EDIT);
                    multiTexts.clear(DBActionOrigin.FROM_EDIT);
                    multiTexts.add(neutralCombText, DBActionOrigin.FROM_EDIT);
                    neutralTexts.clear(DBActionOrigin.FROM_EDIT);
                    neutralTexts.add(multiCombText, DBActionOrigin.FROM_EDIT);
                }
            }
        }
    }

    /**
     * Liefert die Textarten, die für die kombinierten Texte zuständig sind
     *
     * @return
     */
    public Map<String, EditCombinedTextForm.TextKindType> getTextKinds() {
        if (textKinds == null) {
            textKinds = EditCombinedTextForm.loadTextKinds(connector.getProject());
        }
        return textKinds;
    }

    @Override
    public String getText() {
        String result = getConstructedCombinedText();
        if (result == null) {
            result = "";
        }
        return result;
    }

    /**
     * Sorgt dafür, dass bei der Such-ComboBoxen optionale Platzhalter Texte angezeigt werden
     *
     * @param showForMultiText
     * @param showForNeutralText
     */
    public void showKeepTextsEntries(boolean showForMultiText, boolean showForNeutralText) {
        if (isMultiEdit()) {
            if (showForMultiText) {
                getMultiCombTextField().setShowKeepTextEntry();
            }
            if (showForNeutralText) {
                getNeutralCombTextField().setShowKeepTextEntry();
            }
        }
    }

    /**
     * Liefert zurück, ob die aktuellen Textänderung über den Editor erfolgt sind
     *
     * @return
     */
    public boolean isEditedByEditor() {
        return currentTextChangeSource == TextChangeSource.EDIT_FORM;
    }

    public iPartsMultiLangGuiCombTextButtonField getMultiCombTextField() {
        return multiCombTextField;
    }

    public iPartsNeutralGuiCombTextButtonField getNeutralCombTextField() {
        return neutralCombTextField;
    }

    public void setMultiEdit(boolean isMultiEdit) {
        this.isMultiEdit = isMultiEdit;
        getMultiCombTextField().setMultiEdit(isMultiEdit);
        getNeutralCombTextField().setMultiEdit(isMultiEdit);
    }

    public boolean isMultiEdit() {
        return isMultiEdit;
    }

    public boolean isForwardESCKeyReleasedEvent() {
        return this.isForwardESCKeyReleasedEvent;
    }

    public void setForwardESCKeyReleasedEvent(boolean forwardESCKeyReleasedEvent) {
        this.isForwardESCKeyReleasedEvent = forwardESCKeyReleasedEvent;
        getMultiCombTextField().setForwardESCKeyReleasedEvent(forwardESCKeyReleasedEvent);
        getNeutralCombTextField().setForwardESCKeyReleasedEvent(forwardESCKeyReleasedEvent);
    }

    public boolean isForwardENTERKeyReleasedEvent() {
        return isForwardENTERKeyReleasedEvent;
    }

    public void setForwardENTERKeyReleasedEvent(boolean forwardENTERKeyReleasedEvent) {
        this.isForwardENTERKeyReleasedEvent = forwardENTERKeyReleasedEvent;
        getMultiCombTextField().setForwardENTERKeyReleasedEvent(forwardENTERKeyReleasedEvent);
        getNeutralCombTextField().setForwardENTERKeyReleasedEvent(forwardENTERKeyReleasedEvent);
    }

    public boolean isEditedViaEditForm() {
        return currentTextChangeSource == TextChangeSource.EDIT_FORM;
    }

    @Override
    public void addEventListener(EventListener eventListener) {
        super.addEventListener(eventListener);
        getMultiCombTextField().addEventListener(eventListener);
        getNeutralCombTextField().addEventListener(eventListener);
    }

    @Override
    public void removeEventListener(EventListener eventListener) {
        super.removeEventListener(eventListener);
        getMultiCombTextField().removeEventListener(eventListener);
        getNeutralCombTextField().removeEventListener(eventListener);
    }

    @Override
    public void switchOffEventListeners() {
        super.switchOffEventListeners();
        getMultiCombTextField().switchOffEventListeners();
        getNeutralCombTextField().switchOffEventListeners();
    }

    @Override
    public void switchOnEventListeners() {
        super.switchOnEventListeners();
        getMultiCombTextField().switchOnEventListeners();
        getNeutralCombTextField().switchOnEventListeners();
    }

    public void setDividerPosition(int dividerPosition) {
        guiSplitPaneForControls.setDividerPosition(dividerPosition);
    }
}
