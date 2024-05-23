/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.forms.common.EditControlFactory;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsProductModels;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

import java.util.EnumSet;

/**
 * EditUserControls für AS-Baumuster speziell für den Zusatztext
 */
public class EditUserControlForAfterSalesModel extends EditUserControls {

    public EditUserControlForAfterSalesModel(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName,
                                             IdWithType id, DBDataObjectAttributes attributes, EtkEditFields externalEditFields) {
        super(dataConnector, parentForm, tableName, id, attributes, externalEditFields);
    }

    @Override
    protected void modifyEditControl(EditControl ctrl, EtkEditField field, String initialValue, EtkDataArray initialDataArray) {
        if (field.getKey().getFieldName().equals(FIELD_DM_ADD_TEXT)) {
            // Edit-Control für ADD_TEXT tauschen
            GuiMultiLangEditDict langEditDict = new GuiMultiLangEditDict();
            langEditDict.setDataConnector(getConnector());
            EditControlFactory.setDefaultLayout(langEditDict);
            langEditDict.setStartLanguage(Language.findLanguage(ctrl.getEditControl().getValues().dbLanguage));
            langEditDict.setSearchTextKindTypes(EnumSet.of(DictTextKindTypes.ELDAS_MODEL_ADDTEXT));
            langEditDict.setTableForDictionary(iPartsConst.TABLE_DA_MODEL);
            langEditDict.setForeignSourceForCreate(calcForeignSourceForCreatre(attributes));
            ctrl.getEditControl().setControl(langEditDict);
            return;
        }
        super.modifyEditControl(ctrl, field, initialValue, initialDataArray);
    }

    @Override
    protected void fillAttribByEditControlValue(int index, EtkEditField field, DBDataObjectAttribute attrib) {
        if (field.getKey().getFieldName().equals(FIELD_DM_ADD_TEXT) && field.isMultiLanguage()) {
            // Überprüfung, ob Löschen angewählt worden ist
            EditControl controlByFeldIndex = editControls.getControlByFeldIndex(index);
            EditControlFactory ctrl = controlByFeldIndex.getEditControl();
            EtkMultiSprache multi = ctrl.getMultiLangText();
            if (multi.allStringsAreEmpty()) {
                // Löschen: Reset der EtkMultiSprache
                EtkMultiSprache newMulti = new EtkMultiSprache();
                attrib.setValueAsMultiLanguage(newMulti, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                attrib.setTextIdForMultiLanguage("", "", DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                return;
            }
        }
        super.fillAttribByEditControlValue(index, field, attrib);
    }

    private String calcForeignSourceForCreatre(DBDataObjectAttributes attributes) {
        String modelNo = attributes.getFieldValue(iPartsConst.FIELD_DM_MODEL_NO);
        if (StrUtils.isValid(modelNo)) {
            boolean isCarOrVan = false;
            boolean isTruckOrBus = false;
            for (iPartsProduct product : iPartsProductModels.getInstance(getProject()).getProductsByModel(getProject(), modelNo)) {
                isCarOrVan |= product.isCarAndVanProduct();
                isTruckOrBus |= product.isTruckAndBusProduct();
            }

            iPartsImportDataOrigin source = iPartsImportDataOrigin.getiPartsSourceForRights(isCarOrVan, isTruckOrBus);
            if (source != null) {
                return source.getOrigin();
            }
        }
        return DictHelper.getIPartsSourceForCurrentSession();
    }
}
