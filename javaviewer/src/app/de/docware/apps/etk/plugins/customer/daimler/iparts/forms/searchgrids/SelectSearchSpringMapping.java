/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms.searchgrids;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFieldsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.SimpleSelectSearchResultGrid;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;

/**
 * Dialog zum Handling der Federfilter (Anzeige und Editor)
 */
public class SelectSearchSpringMapping extends SimpleSelectSearchResultGrid {

    public static String doSelectSpringMappingDialog(AbstractJavaViewerForm parentForm, String initialSearchValue, String title) {
        AbstractJavaViewerFormIConnector dataConnector = parentForm.getConnector();
        SelectSearchSpringMapping dlg = new SelectSearchSpringMapping(dataConnector, parentForm, iPartsConst.MAX_MATERIAL_SEARCH_RESULTS_SIZE);
        EtkDisplayFields displayResultFields = EtkDisplayFieldsHelper.createDefaultDisplayResultFields(dataConnector.getProject(), iPartsConst.TABLE_DA_SPRING_MAPPING);
        EtkDisplayField displayField = displayResultFields.getFeldByName(iPartsConst.TABLE_DA_SPRING_MAPPING, iPartsConst.FIELD_DSM_SPRING);
        if (displayField != null) {
            EtkMultiSprache multiSprache = new EtkMultiSprache();
            multiSprache.setText(dataConnector.getProject().getViewerLanguage(), TranslationHandler.translate("!!Benennung"));
            displayField.setText(multiSprache);
        }
        //dlg.setFilterFieldNames(filterFieldNames);
        //dlg.setFilterValues(filterValues);
        dlg.setDisplayResultFields(displayResultFields);
        dlg.setTitle(title);
        dlg.setOnValidateAttributesEvent(null);
        if ((initialSearchValue != null) && !initialSearchValue.isEmpty()) {
            dlg.setSearchValue(initialSearchValue);
        }
        if (dlg.showModal() == ModalResult.OK) {
            DBDataObjectAttributes attributes = dlg.getSelectedAttributes();
            // attributes kann nur in der Swing Version null sein, weil dort der Dialog per ENTER geschlossen werden kann
            if (attributes != null) {
                return attributes.getField(iPartsConst.FIELD_DSM_ZB_SPRING_LEG).getAsString();
            }

        }
        return "";
    }

    /**
     * Erzeugt eine Instanz von SimpleSelectSearchResultGrid.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public SelectSearchSpringMapping(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, int maxResults) {
        super(dataConnector, parentForm, iPartsConst.TABLE_DA_SPRING_MAPPING, iPartsConst.FIELD_DSM_ZB_SPRING_LEG);
        setMaxResults(maxResults);
        setMultiSelect(false);
        setAutoSelectSingleSearchResult(true);
        onChangeSearchValueEvent = startValue -> getVisObject().getDatabaseValueOfVisValue(EtkDbConst.TABLE_MAT, EtkDbConst.FIELD_M_BESTNR, startValue, getProject().getViewerLanguage());
    }

    protected GuiTableRowWithAttributes setRow(DBDataObjectAttributes attributes) {
        GuiTableRowWithAttributes row = new GuiTableRowWithAttributes();
        String springNo = attributes.getField(iPartsConst.FIELD_DSM_ZB_SPRING_LEG).getAsString();
        for (EtkDisplayField field : displayResultFields.getFields()) {
            if (field.isVisible()) {
                String fieldName = field.getKey().getFieldName();
                String value;
                if (fieldName.equals(iPartsConst.FIELD_DSM_SPRING)) {
                    PartId partID = new PartId(springNo, "");
                    EtkDataPart dataPart = EtkDataObjectFactory.createDataPart(getProject(), partID);
                    EtkMultiSprache multiLang = dataPart.getFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR);
                    DBDataObjectAttribute attribute = new DBDataObjectAttribute(EtkDbConst.FIELD_M_TEXTNR, DBDataObjectAttribute.TYPE.MULTI_LANGUAGE, false);
                    attribute.setValueAsMultiLanguage(multiLang, DBActionOrigin.FROM_DB);
                    setAttributeToMultiLang(attribute, iPartsConst.TABLE_MAT);
                    value = attribute.getMultiLanguageText(getProject().getDBLanguage(), EtkDataObject.getTempExtendedDataTypeProvider(getProject(), field.getKey().getTableName()));
                } else {
                    value = getVisualValueOfFieldValue(fieldName, attributes.getField(fieldName), field.isMultiLanguage());
                }
                GuiLabel label = new GuiLabel(value);
                row.addChild(label);
            }
        }
        return row;
    }

}
