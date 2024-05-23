/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.inplaceditor;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsNeutralGuiCombTextButtonField;
import de.docware.util.sql.TableAndFieldName;

import java.util.EnumSet;

public class iPartsGuiNeutralTextInplaceEditor extends iPartsGuiCombTextElementInplaceEditor {

    public static final String DEFAULT_TABLE_AND_FIELD_NAME = TableAndFieldName.make(iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.RETAIL_TEXT_NEUTRAL);

    public iPartsGuiNeutralTextInplaceEditor() {
        super(new iPartsNeutralGuiCombTextButtonField());
    }

    @Override
    protected EnumSet<DictTextKindTypes> getSearchType() {
        return EnumSet.of(DictTextKindTypes.NEUTRAL_TEXT);
    }

    @Override
    public iPartsNeutralGuiCombTextButtonField getElementControl() {
        if ((editorControl instanceof iPartsNeutralGuiCombTextButtonField)) {
            return (iPartsNeutralGuiCombTextButtonField)this.editorControl;
        }
        return null;
    }

    @Override
    protected iPartsNeutralGuiCombTextButtonField getElementControlFromCombEditor() {
        return getCombTextCompleteEditControl().getNeutralCombTextField();
    }
}
