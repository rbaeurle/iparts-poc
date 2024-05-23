/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.searchgrids.SelectSearchGridSeries;
import de.docware.framework.modules.gui.controls.GuiButtonTextField;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;

/**
 * {@link GuiButtonTextField} zur Auswahl einer Baureihe.
 */
public class iPartsGuiSeriesSelectTextField extends GuiButtonTextField {

    public static final String TYPE = "iPartsGuiSeriesSelectTextField";

    public iPartsGuiSeriesSelectTextField() {
        super();
        setType(TYPE);
    }

    public void init(final AbstractJavaViewerForm parentForm) {
        if (parentForm != null) {
            super.addEventListener(new EventListener(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(Event event) {
                    SelectSearchGridSeries selectSearchGridSeries = new SelectSearchGridSeries(parentForm);
                    selectSearchGridSeries.setTitle("!!Zugehörige Baureihe auswählen");
                    String seriesNo = selectSearchGridSeries.showGridSelectionDialog("*");
                    if (!seriesNo.isEmpty()) {
                        setText(seriesNo);
                    }
                }
            });
        } else {
            super.removeEventListeners(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT);
        }
    }

    @Override
    public void setEditable(boolean isEditable) {
        super.setEditable(isEditable);
        this.getButton().setEnabled(isEditable);
    }
}