/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictTextKind;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictTextKindList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.dictionary.DictMultiLangEditForm;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.utils.EtkMultiSprache;

import java.util.Collection;
import java.util.EnumSet;

public class iPartsGuiNeutralTextCompleteEditControl extends GuiPanel implements iPartsConst {

    public static final String TYPE = "iPartsGuiNeutralTextCompleteEditControl";

    private static EnumSet<DictTextKindTypes> DEFAULT_TEXTKIND_TYPES = EnumSet.of(DictTextKindTypes.NEUTRAL_TEXT);

    private iPartsNeutralGuiCombTextButtonField neutralCombTextField;
    private AbstractJavaViewerFormIConnector connector;

    public iPartsGuiNeutralTextCompleteEditControl() {
        super();
        type = TYPE;
        init();
    }

    private void init() {
        AbstractGuiCombTextButtonField.CombTextChangeCallback callBack = createOnButtonClickCallback();
        neutralCombTextField = new iPartsNeutralGuiCombTextButtonField();
        neutralCombTextField.setCombTextChangeCallback(callBack);
        setLayout(new LayoutBorder());
        addChildBorder(neutralCombTextField, LayoutBorder.POSITION_CENTER);
    }

    public void requestFocus() {
        getNeutralCombTextField().requestFocus();
    }

    public EtkMultiSprache getMultiLanguage() {
        EtkMultiSprache multi = getNeutralCombTextField().getMultiLang();
        if (multi == null) {
            multi = new EtkMultiSprache();
        }
        return multi.cloneMe();
    }

    public void setMultiText(String text, String lang) {
        EtkMultiSprache multi = new EtkMultiSprache();
        multi.setText(lang, text);
        multi.fillAllLanguages(connector.getConfig().getDatabaseLanguages(), Language.DE);
        setMultiLanguage(multi);
    }

    public void setMultiLanguage(EtkMultiSprache multi) {
        getNeutralCombTextField().createAndModifyCombTextEntry(null, multi.getTextId(), false);
        getNeutralCombTextField().setText(multi.getText(Language.DE.getCode()), multi.getTextId());
    }

    public void setConnector(AbstractJavaViewerFormIConnector connector) {
        this.connector = connector;
        getNeutralCombTextField().setConnector(connector);
        getNeutralCombTextField().setPartListEntryId(null);
    }

    public void enableButton() {
        getNeutralCombTextField().setButtonVisible(getNeutralCombTextField().getConnector() != null);
    }

    public iPartsNeutralGuiCombTextButtonField getNeutralCombTextField() {
        return neutralCombTextField;
    }

    private void setToolTip(String currentText) {
        getNeutralCombTextField().setToolTip(currentText);
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
                Language lang = Language.findLanguage(connector.getProject().getDBLanguage());
                String initialSearchValue = getMultiLanguage().getText(lang.getCode());
                DictTextKindTypes initialType = DEFAULT_TEXTKIND_TYPES.iterator().next();
                Collection<iPartsDataDictTextKind> textKindList = iPartsDataDictTextKindList.loadSpecialTextKindListSortedByName(connector.getProject(),
                                                                                                                                 DEFAULT_TEXTKIND_TYPES);
                EtkMultiSprache multiLang = DictMultiLangEditForm.showDictMultiLangEdit(connector, connector.getActiveForm(),
                                                                                        lang, initialSearchValue, initialType,
                                                                                        textKindList, true, true, TABLE_MAT);
                if (multiLang != null) {
                    setMultiLanguage(multiLang);
                }
            }

            @Override
            public int getNextSeqNo() {
                // es kann nur einen sprachneutralen Text am Material geben
                return 1;
            }

            @Override
            public void textChangeFromSearch(boolean newDataObjectCreated) {
                if (!newDataObjectCreated && getText().trim().isEmpty()) {
                    getNeutralCombTextField().createAndModifyCombTextEntry(null, "", false);
                }
            }
        };
    }


}
