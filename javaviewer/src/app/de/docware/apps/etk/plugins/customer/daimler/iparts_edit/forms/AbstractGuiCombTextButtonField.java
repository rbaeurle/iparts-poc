/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsCombTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataCombText;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataCombTextList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictSearchComboBox;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.AbstractGuiButtonTextField;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiButtonTextField;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.Utils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Erweiterung des {@link GuiButtonTextField}s um Kombinierte Texte eines {@link de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry}
 * anzuzeigen/editieren zu können
 */
public abstract class AbstractGuiCombTextButtonField extends AbstractGuiButtonTextField {

    private static final String KEEP_TEXTS_ENTRY = "!![ Texte beibehalten ]"; // Pseudo-Text Platzhalter
    private static final int MAX_LINE_LENGTH_FOR_HINT = 100;
    public static final String KEEP_TEXTS_ENTRY_ID = "KEEP_ALL_TEXT_ENTRIES"; // Pseudo-Id Platzhalter

    protected AbstractJavaViewerFormIConnector dataConnector;
    protected PartListEntryId partListEntryId;
    protected iPartsDataCombTextList dataCombTextList;
    protected DBDataObjectList<iPartsDataCombText> originalDataCombTextList; // Original Textbausteine, falls man den Ausgangsstatus herstellen will
    protected String selectedCombText = ""; // inkl. sprachneutraler Text vom Teilestamm
    protected boolean isMultiEdit;
    private CombTextChangeCallback combTextChangeCallback;
    private boolean showKeepTextEntry;  // Kenner, ob PSeudo Platzhalter in diese Search-ComboBox verwendet werden soll

    public AbstractGuiCombTextButtonField() {
        super();
        getButton().setVisible(false);
        super.setEditable(false);
        dataCombTextList = null;
    }

    public boolean isForwardESCKeyReleasedEvent() {
        if (edit instanceof CombTextDictSearchComboBox) {
            return ((CombTextDictSearchComboBox)edit).isForwardESCKeyReleasedEvent();
        }
        return false;
    }

    public void setForwardESCKeyReleasedEvent(boolean forwardESCKeyReleasedEvent) {
        if (edit instanceof CombTextDictSearchComboBox) {
            ((CombTextDictSearchComboBox)edit).setForwardESCKeyReleasedEvent(forwardESCKeyReleasedEvent);
        }
    }

    public boolean isForwardENTERKeyReleasedEvent() {
        if (edit instanceof CombTextDictSearchComboBox) {
            return ((CombTextDictSearchComboBox)edit).isForwardENTERKeyReleasedEvent();
        }
        return false;
    }

    public void setForwardENTERKeyReleasedEvent(boolean forwardENTERKeyReleasedEvent) {
        if (edit instanceof CombTextDictSearchComboBox) {
            ((CombTextDictSearchComboBox)edit).setForwardENTERKeyReleasedEvent(forwardENTERKeyReleasedEvent);
        }
    }

    public void requestFocus() {
        edit.requestFocus();
    }

    @Override
    protected AbstractGuiControl createEditControl() {
        final CombTextDictSearchComboBox editControl = new CombTextDictSearchComboBox(null, getDictTypeForSearch());
        editControl.setWithEmptyItem(true);
        editControl.setEditable(true);

        // Reaktion auf die Auswahl eines Ergänzungstexts
        addOnChangeEventListener(editControl);

        return editControl;
    }

    private void addOnChangeEventListener(final CombTextDictSearchComboBox editControl) {
        editControl.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                String text = editControl.getSelectedItem();
                if (text != null) {
                    // Neuen Ergänzungstext hinzufügen und vorher den bisherigen Ergänzungstext löschen (falls selectedUserObject
                    // null ist, wird nur der bisherige Ergänzungstext gelöscht)
                    String textId = editControl.getSelectedUserObject();
                    boolean realNewTextObjectCreated = false; // Kenner, ob ein Text ausgewählt und erzeugt wurde
                    if (textId != null) {
                        if (!textId.isEmpty()) { // Ergänzungstext wurde ausgewählt
                            iPartsDataCombText originalText = null;
                            boolean addToList = true;
                            if (dataCombTextList == null) {
                                dataCombTextList = new iPartsDataCombTextList();
                            } else {
                                for (iPartsDataCombText combText : dataCombTextList.getAsList()) { // getAsList() zur Vermeidung von ConcurrentModificationExceptions
                                    if ((originalText == null) && !Utils.objectEquals(combText.getFieldValueAsMultiLanguageIncomplete(iPartsConst.FIELD_DCT_DICT_TEXT, false).getTextId(), KEEP_TEXTS_ENTRY_ID)) {
                                        originalText = combText;
                                        addToList = false;
                                        continue;
                                    }
                                    dataCombTextList.delete(combText, true, DBActionOrigin.FROM_EDIT);
                                }
                                if ((originalText == null) && !dataCombTextList.getDeletedList().isEmpty()) {
                                    originalText = dataCombTextList.getDeletedList().get(0);
                                }
                            }

                            // Neuen Ergänzungstext (oder Platzhalter im MultiEdit) hinzufügen
                            realNewTextObjectCreated = createAndModifyCombTextEntry(originalText, textId, addToList) && !textId.equals(KEEP_TEXTS_ENTRY_ID);
                        } else if (!isMultiEdit()) {
                            // dataCombTextList muss neu bestimmt werden, wenn der bisherige Ergänzungstext wiederhergestellt wird
                            // Im Multi-Edit wird die Erzeugung und das Löschen von neuen Objekten an anderer Stelle gemacht,
                            // daher darf man hier im Multi-Edit nicht rein.
                            dataCombTextList.addAll(originalDataCombTextList, DBActionOrigin.FROM_DB);
                        }
                    } else if (editControl.getSelectedIndex() >= 0) {
                        // Alte Ergänzungstexte löschen
                        if (dataCombTextList != null) {
                            dataCombTextList.deleteAll(DBActionOrigin.FROM_EDIT);
                        }
                    }
                    // dataCombTextList wird in iPartsEditPlugin.saveAdditionalDataForEditControl() gespeichert
                    setText(text, textId);
                    // Hinweis, dass sich der Suchtext geändert hat
                    combTextChangeCallback.textChangeFromSearch(realNewTextObjectCreated);
                }
            }
        });
    }

    /**
     * Fügt ein neues {@link iPartsDataCombText} Objekt zu den aktuellen Objekten dieser Such-ComboBox hinzu. Falls es sich
     * um einen Platzhalter-Text handelt, wird ein Pltzhalter {@link iPartsDataCombText} Objekt angelegt.
     *
     * Liefert als Ergebnis <code>true</code> zurück, wenn ein neues Objekt erzeugt wurde.
     *
     * @param existingCombText
     * @param textId
     */
    protected boolean createAndModifyCombTextEntry(iPartsDataCombText existingCombText, String textId, boolean addToList) {
        // Wenn keine ID übergeben wurde, eine neue erzeugen
        boolean result = false;
        iPartsDataCombText dataCombText = existingCombText;
        if ((dataCombText == null) || isPseudoCombText(dataCombText)) {
            iPartsCombTextId combTextId = new iPartsCombTextId(partListEntryId, EtkDbsHelper.formatLfdNr(combTextChangeCallback.getNextSeqNo()));
            dataCombText = new iPartsDataCombText(dataConnector.getProject(), combTextId);
            dataCombText.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            result = true;
        }
        EtkMultiSprache multiLang;
        if (textId.equals(KEEP_TEXTS_ENTRY_ID)) {
            multiLang = new EtkMultiSprache();
            multiLang.setTextId(KEEP_TEXTS_ENTRY_ID);
            multiLang.setText(getConnector().getProject().getDBLanguage(), getTextForPlaceHolderEntry());
        } else {
            multiLang = dataConnector.getProject().getDbLayer().getLanguagesTextsByTextId(textId);
        }
        dataCombText.setFieldValueAsMultiLanguage(iPartsConst.FIELD_DCT_DICT_TEXT, multiLang, DBActionOrigin.FROM_EDIT);
        if (addToList) {
            dataCombTextList.add(dataCombText, DBActionOrigin.FROM_EDIT);
        }
        return result;
    }

    public static boolean isPseudoCombText(iPartsDataCombText dataCombText) {
        return (dataCombText != null) && dataCombText.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DCT_DICT_TEXT).getTextId().equals(KEEP_TEXTS_ENTRY_ID);
    }

    public void setCombTextChangeCallback(CombTextChangeCallback combTextChangeCallback) {
        this.combTextChangeCallback = combTextChangeCallback;
    }

    /**
     * ist immer nicht editierbar
     *
     * @param isEditable
     */
    @Override
    public void setEditable(boolean isEditable) {
        if (getButton().isVisible()) {
            getButton().setEnabled(isEditable);
        }
    }

    @Override
    public void setText(String text) {
        setText(text, null);
    }

    public void setText(String text, String userObject) {
        if (text == null) {
            text = "";
        }
        if (Utils.objectEquals(selectedCombText, text)) {
            return;
        }
        if (edit instanceof CombTextDictSearchComboBox) {
            edit.switchOffEventListeners();
            try {
                // Leere TextId als UserObject setzen (weil nicht bekannt zur Erkennung der bisherigen Auswahl) sofern der
                // Text nicht leer ist (dann null setzen, um den Ergänzungstext löschen zu können)
                if (StrUtils.isEmpty(userObject)) {
                    userObject = !text.isEmpty() ? "" : null;
                }
                ((CombTextDictSearchComboBox)edit).setFilterText(text, userObject, false);
            } finally {
                edit.switchOnEventListeners();
            }
        }
        selectedCombText = text;
    }

    public void resetFilterText() {
        selectedCombText = "";
        if (edit instanceof CombTextDictSearchComboBox) {
            edit.switchOffEventListeners();
            try {
                ((CombTextDictSearchComboBox)edit).setFilterText("");
            } finally {
                edit.switchOnEventListeners();
            }
        }
    }

    protected void setToolTip(String text) {
        if (StrUtils.isValid(text)) {
            if (text.length() > MAX_LINE_LENGTH_FOR_HINT) {
                List<String> textList = StrUtils.toStringList(text, " ", true);
                StringBuilder str = new StringBuilder(textList.get(0));
                StringBuilder line = new StringBuilder(textList.get(0));
                for (int wordCounter = 1; wordCounter < textList.size(); wordCounter++) {
                    String nextText = textList.get(wordCounter);
                    if ((line.length() + nextText.length() + 1) < MAX_LINE_LENGTH_FOR_HINT) {
                        str.append(" ");
                        str.append(nextText);
                        line.append(" ");
                        line.append(nextText);
                    } else {
                        str.append("\n");
                        line.setLength(0);
                        str.append(nextText);
                        line.append(nextText);
                    }
                }
                text = str.toString();
            }
            text = TranslationHandler.translate("!!Vorschau:") + "\n" + text;
        }
        edit.setTooltip(text);
    }

    public CombTextChangeCallback getCombTextChangeCallback() {
        return combTextChangeCallback;
    }

    @Override
    public String getText() {
        return selectedCombText;
    }

    public String getEditResult() {
        if ((partListEntryId != null) && Utils.isValid(getDataCombTextList())) {
            Map<String, String> combTextMap = getDataCombTextList().getCombTexts(null, getConnector().getProject());
            return combTextMap.get(partListEntryId.getKLfdnr());
        } else {
            return "";
        }
    }

    public AbstractJavaViewerFormIConnector getConnector() {
        return dataConnector;
    }

    public void setConnector(AbstractJavaViewerFormIConnector connector) {
        this.dataConnector = connector;
        if ((connector != null) && (edit instanceof CombTextDictSearchComboBox)) {
            ((CombTextDictSearchComboBox)edit).setProject(connector.getProject());
        }
    }

    /**
     * Liefert die Liste der kombinierten Texte zurück.
     *
     * @return {@code null} falls das Editieren abgebrochen wurde
     */
    public iPartsDataCombTextList getDataCombTextList() {
        cleanCombTextList();
        return dataCombTextList;
    }

    /**
     * Entfernt gelöschte Objekte, die die gleiche ID haben, wie neu erzeugte Objekte
     */
    private void cleanCombTextList() {
        if (dataCombTextList != null) {
            if (!dataCombTextList.getDeletedList().isEmpty() && !dataCombTextList.getAsList().isEmpty()) {
                List<iPartsDataCombText> list = dataCombTextList.getAsList();
                List<iPartsDataCombText> deleteList = dataCombTextList.getDeletedList();
                Set<iPartsCombTextId> validIds = new HashSet<>();
                iPartsDataCombTextList result = new iPartsDataCombTextList();
                for (iPartsDataCombText combText : list) {
                    validIds.add(combText.getAsId());
                    result.add(combText, DBActionOrigin.FROM_EDIT);
                }
                for (iPartsDataCombText deleteCombText : deleteList) {
                    if (!validIds.contains(deleteCombText.getAsId())) {
                        result.delete(deleteCombText, true, DBActionOrigin.FROM_EDIT);
                    }
                }
                dataCombTextList = result;
            }
        }
    }

    @Override
    protected void doButtonClick() {
        if (combTextChangeCallback != null) {
            combTextChangeCallback.onButtonClick();
        }
    }

    /**
     * Setzt den kombinierten text für dieses Control und sperrt es für den direkten Edit, wenn der kombinierte Text aus mehr
     * als zwei Textelementen besteht.
     *
     * @param combTextList
     */
    public void setDataCombTextList(iPartsDataCombTextList combTextList) {
        if (combTextList == null) {
            // ComboBox nur dann enablen, wenn es höchstens einen Ergänzungstext bzw. sprachneutralen Text am Teil gibt
            // loadCombText() mit dbLanguage = null aufrufen, damit kein Join und damit auch keine Pseudo-Transaktion notwendig ist
            dataCombTextList = new iPartsDataCombTextList();
        } else {
            dataCombTextList = combTextList;
        }
        // Original Objekte setzen
        originalDataCombTextList = new iPartsDataCombTextList();
        originalDataCombTextList = dataCombTextList.cloneMe(getConnector().getProject());
        edit.setEnabled(dataCombTextList.size() <= 1);
    }

    /**
     * Fügt der aktuellen Such-ComboBox ein Platzhalter Objekt hinzu, das bei beim Multi-Edit benötigt
     */
    public void setShowKeepTextEntry() {
        if (isMultiEdit() && (edit instanceof CombTextDictSearchComboBox)) {
            // Den eigentlichen Eintrag in der ComboBox
            String translatedText = TranslationHandler.translate(KEEP_TEXTS_ENTRY);
            setText(translatedText, KEEP_TEXTS_ENTRY_ID);
            // Das dazugehörige iPartDataCombText Objekt
            createAndModifyCombTextEntry(null, KEEP_TEXTS_ENTRY_ID, true);
            showKeepTextEntry = true;
        }
    }

    public void setPartListEntryId(PartListEntryId partListEntryId) {
        this.partListEntryId = partListEntryId;
    }

    public boolean isMultiEdit() {
        return isMultiEdit;
    }

    public void setMultiEdit(boolean multiEdit) {
        isMultiEdit = multiEdit;
    }

    protected abstract DictTextKindTypes getDictTypeForSearch();

    protected abstract String getTextForPlaceHolderEntry();

    /**
     * Callback um Infos an das {@link iPartsGuiCombTextCompleteEditControl} weiterzugeben
     */
    public interface CombTextChangeCallback {

        void onButtonClick();

        int getNextSeqNo();

        void textChangeFromSearch(boolean newDataObjectCreated);
    }

    /**
     * Eigene Such-ComboBox mit möglichen Platzhalter Einträgen
     */
    private class CombTextDictSearchComboBox extends DictSearchComboBox {

        public CombTextDictSearchComboBox(EtkProject project, DictTextKindTypes textKind) {
            super(project, textKind);
        }

        /**
         * Fügt den Platzhalter hinzu. Zusätzlich kann der Filtertext gesetzt werden
         */
        public void addKeepTextEntry() {
            String translatedText = TranslationHandler.translate(KEEP_TEXTS_ENTRY);
            // Existiert der Eintrag schon?
            int index = getIndexOfItem(translatedText);
            if (index == -1) {
                // Falls nicht, an erster bzw zweiter Stelle platzieren
                index = (getItemCount() > 0) ? 1 : 0;
                super.addItemWithoutGUI(KEEP_TEXTS_ENTRY_ID, translatedText, null, index, true, false);
            }
        }

        @Override
        public synchronized void showDropDown() {
            if (isMultiEdit && showKeepTextEntry) {
                addKeepTextEntry();
            }
            super.showDropDown();
        }
    }
}
