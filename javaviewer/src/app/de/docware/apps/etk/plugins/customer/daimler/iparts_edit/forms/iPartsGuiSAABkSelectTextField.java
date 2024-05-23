/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsGuiArraySelectionTextField;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.gui.controls.GuiButtonTextField;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;

import java.util.Collection;

/**
 * {@link GuiButtonTextField} zur Auswahl von SAA/BKs
 */
public class iPartsGuiSAABkSelectTextField extends iPartsGuiArraySelectionTextField {

    public static final String TYPE = "iPartsGuiSAABkSelectTextField";
    private Collection<String> modelListForSaaBkRetrieval;

    public iPartsGuiSAABkSelectTextField(EtkProject project) {
        super(project, TYPE);
        setEditable(false);
    }

    public void init(final AbstractJavaViewerForm parentForm, final iPartsProductId productId, Collection<String> modelList) {
        if (parentForm != null) {
            setModelList(modelList);
            super.addEventListener(new EventListener(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(Event event) {
                    Collection<String> saaBkList = EditSelectSAABKForm.showSelectionSaaBk(parentForm, productId,
                                                                                          modelListForSaaBkRetrieval,
                                                                                          dataArray.getArrayAsStringList(), true);
                    if (saaBkList != null) {
                        addDataArrayFromSelection(saaBkList);
                    }
                }
            });
            addRadioButtons("!!Vereinheitlichen", "!!Hinzufügen");
        } else {
            super.removeEventListeners(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT);
        }
    }

    @Override
    protected String getArrayAsFormattedString() {
        // String mit den visualisierten Arraywerten
        return project.getVisObject().getArrayAsFormattedString(dataArray, "", project.getDBLanguage(), iPartsConst.TABLE_KATALOG,
                                                                iPartsConst.FIELD_K_SA_VALIDITY, false);
    }

    public Collection<String> getModelList() {
        return modelListForSaaBkRetrieval;
    }

    public void setModelList(Collection<String> modelList) {
        this.modelListForSaaBkRetrieval = modelList;
    }
}
