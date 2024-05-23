/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.common.EnumCheckRComboBox;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsAAPartsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;

/**
 * Abstrakte Klasse für die Auswahl von Ausführungsarten zu einer Baureihe. Es kann unterschieden werden zwischen einer
 * normalen EnumRComboBox oder einer EnumCheckRComboBox
 */
public abstract class AbstractGuiSeriesAAComboBox extends EnumCheckRComboBox {

    private boolean isSOEComboBox;

    public AbstractGuiSeriesAAComboBox(boolean isSOEComboBox) {
        super();
        this.isSOEComboBox = isSOEComboBox;
    }

    /**
     * Befüllt die Combobox mit allen Ausführungsarten zur übergebenen Baureihe und dem gewünschten DB-Feld
     *
     * @param project
     * @param seriesId
     * @param tablename
     * @param fieldname
     */
    public void fillSeriesAAEnumValues(EtkProject project, iPartsSeriesId seriesId, String tablename, String fieldname) {
        setAAEnumTexte(project, seriesId, tablename, fieldname);
    }

    /**
     * Bestimmt zur übergebenen {@link iPartsSeriesId} die gültigen Ausführungsarten und setzt diese
     *
     * @param project
     * @param seriesId
     * @param tablename
     * @param fieldname
     */
    protected void setAAEnumTexte(EtkProject project, iPartsSeriesId seriesId, String tablename, String fieldname) {
        // Liste der gültigen AA's aus X4E (DA_SERIES_CODES) holen
        iPartsAAPartsHelper.setEnumTexteByX4E(project, this, tablename, fieldname, seriesId.getSeriesNumber(),
                                              project.getDBLanguage(), true);
    }

    @Override
    public String getActToken() {
        if (isSOEComboBox) {
            return super.getActToken();
        } else {
            return getTokenByIndex(getSelectedIndex());
        }
    }

    @Override
    public boolean isSoeToken() {
        return isSOEComboBox;
    }
}
