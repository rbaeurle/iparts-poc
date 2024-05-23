/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;

/**
 * Combobox für die Ausführungsarten-Enums zu einer Baureihe
 */
public class iPartsGuiSeriesAAComboBox extends AbstractGuiSeriesAAComboBox {


    public iPartsGuiSeriesAAComboBox() {
        super(false);
        setMode(Mode.STANDARD);
    }

    @Override
    protected void setAAEnumTexte(EtkProject project, iPartsSeriesId seriesId, String tablename, String fieldname) {
        super.setAAEnumTexte(project, seriesId, tablename, fieldname);
        if (!getItem(0).isEmpty()) {
            addItem("");
        }
    }
}
