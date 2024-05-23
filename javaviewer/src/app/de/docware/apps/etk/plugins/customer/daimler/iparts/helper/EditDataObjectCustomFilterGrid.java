/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControlFactory;
import de.docware.apps.etk.base.forms.common.components.AbstractGuiTableColumnFilterFactory;
import de.docware.apps.etk.base.forms.common.components.DataObjectFilterGrid;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;

/**
 * Unterklasse von {@link DataObjectFilterGrid} zum Verwenden von eigenen Prüfungen in {@link AbstractGuiTableColumnFilterFactory}
 */
public class EditDataObjectCustomFilterGrid extends DataObjectFilterGrid {


    public EditDataObjectCustomFilterGrid(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
        setColumnFilterFactory(new CustomDataObjectColumnFilterFactory(getProject()));

    }

    private class CustomDataObjectColumnFilterFactory extends DataObjectColumnFilterFactory {

        public CustomDataObjectColumnFilterFactory(EtkProject project) {
            super(project);
        }

        @Override
        protected String getFilterValueFromVisObject(String value, EditControlFactory control) {
            // Hier können die Werte für einzelne Felder formatiert werden
            if (control.getTableName().equals(iPartsConst.TABLE_DA_EDS_SAA_MODELS) && control.getFieldName().equals(iPartsConst.FIELD_DA_ESM_SAA_NO)) {
                iPartsNumberHelper numberHelper = new iPartsNumberHelper();
                return numberHelper.unformatSaaBkForEdit(getProject(), value);
            }
            return super.getFilterValueFromVisObject(value, control);
        }
    }
}
