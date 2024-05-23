/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.EditSelectFINForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsGuiArraySelectionTextField;
import de.docware.framework.modules.gui.controls.GuiButtonTextField;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class iPartsGuiFinSelectTextField extends iPartsGuiArraySelectionTextField {

    public static final String TYPE = "iPartsGuiFinSelectTextField";

    public iPartsGuiFinSelectTextField(EtkProject project) {
        super(project, TYPE);
    }

    public void init(AbstractJavaViewerForm parentForm, EditConnectModelsForm modelsForm) {
        removeEventListeners(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT);
        if (parentForm != null) {
            addEventListener(new EventListener(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(Event event) {
                    Set<String> modelNumbers = modelsForm.getModelIds().stream()
                            .map(modelId -> modelId.getModelNumber())
                            .collect(Collectors.toSet());
                    Collection<String> finNumbers = EditSelectFINForm.showSelectionFins(parentForm, productId, modelNumbers,
                                                                                        dataArray.getArrayAsStringList(),
                                                                                        true);
                    if (finNumbers != null) {
                        addDataArrayFromSelection(finNumbers);
                    }
                }
            });
        }
    }

    @Override
    protected String getArrayAsFormattedString() {
        // String mit den visualisierten Arraywerten
        return project.getVisObject().getArrayAsFormattedString(dataArray, "", project.getDBLanguage(), iPartsConst.TABLE_DA_PRODUCT,
                                                                iPartsConst.FIELD_DP_FINS, false);
    }
}
