/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataCombText;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.framework.utils.EtkMultiSprache;

public class iPartsNeutralGuiCombTextButtonField extends AbstractGuiCombTextButtonField {

    public static final String TYPE = "iPartsNeutralGuiCombTextButtonField";
    public static final String TEXT_FOR_PLACEHOLDER_ENTRY = "KEEP_NEUTRAL_TEXTS";

    private EtkMultiSprache multiLang;

    public iPartsNeutralGuiCombTextButtonField() {
        super();
        setType(TYPE);
    }

    @Override
    protected String getTextForPlaceHolderEntry() {
        return TEXT_FOR_PLACEHOLDER_ENTRY;
    }

    @Override
    protected DictTextKindTypes getDictTypeForSearch() {
        return DictTextKindTypes.NEUTRAL_TEXT;
    }

    public EtkMultiSprache getMultiLang() {
        if (partListEntryId != null) {
            return null;
        }
        return multiLang;
    }

    @Override
    protected boolean createAndModifyCombTextEntry(iPartsDataCombText existingCombText, String textId, boolean addToList) {
        if (partListEntryId != null) {
            return super.createAndModifyCombTextEntry(existingCombText, textId, addToList);
        } else {
            if (textId.equals(KEEP_TEXTS_ENTRY_ID)) {
                multiLang = new EtkMultiSprache();
                multiLang.setTextId(KEEP_TEXTS_ENTRY_ID);
                multiLang.setText(getConnector().getProject().getDBLanguage(), getTextForPlaceHolderEntry());
            } else {
                multiLang = dataConnector.getProject().getDbLayer().getLanguagesTextsByTextId(textId);
            }
            return true;
        }
    }
}
