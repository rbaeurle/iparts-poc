/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;

public class iPartsMultiLangGuiCombTextButtonField extends AbstractGuiCombTextButtonField {

    public static final String TYPE = "iPartsMultiLangGuiCombTextButtonField";
    public static final String TEXT_FOR_PLACEHOLDER_ENTRY = "KEEP_MULTI_TEXTS";

    public iPartsMultiLangGuiCombTextButtonField() {
        super();
        setType(TYPE);
    }

    @Override
    protected String getTextForPlaceHolderEntry() {
        return TEXT_FOR_PLACEHOLDER_ENTRY;
    }

    @Override
    protected DictTextKindTypes getDictTypeForSearch() {
        return DictTextKindTypes.ADD_TEXT;
    }


}
