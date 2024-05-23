/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.inplaceditor;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.AbstractGuiCombTextButtonField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsMultiLangGuiCombTextButtonField;
import de.docware.util.sql.TableAndFieldName;

import java.util.EnumSet;

public class iPartsGuiAddTextInplaceEditor extends iPartsGuiCombTextElementInplaceEditor {

    public static final String DEFAULT_TABLE_AND_FIELD_NAME = TableAndFieldName.make(iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.RETAIL_ADD_TEXT);

    public iPartsGuiAddTextInplaceEditor() {
        super(new iPartsMultiLangGuiCombTextButtonField());
    }

    @Override
    public iPartsMultiLangGuiCombTextButtonField getElementControl() {
        if (editorControl instanceof iPartsMultiLangGuiCombTextButtonField) {
            return (iPartsMultiLangGuiCombTextButtonField)this.editorControl;
        }
        return null;
    }

    @Override
    protected AbstractGuiCombTextButtonField getElementControlFromCombEditor() {
        return getCombTextCompleteEditControl().getMultiCombTextField();
    }

    @Override
    protected EnumSet<DictTextKindTypes> getSearchType() {
        return EnumSet.of(DictTextKindTypes.ADD_TEXT);
    }
}
