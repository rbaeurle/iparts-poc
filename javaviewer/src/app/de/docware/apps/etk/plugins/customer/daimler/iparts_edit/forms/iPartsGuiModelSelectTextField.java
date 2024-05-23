/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsGuiArraySelectionTextField;
import de.docware.framework.modules.gui.controls.GuiButtonTextField;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.utils.EtkDataArray;

import java.util.Collection;

/**
 * {@link GuiButtonTextField} zur Auswahl von Baumustern.
 */
public class iPartsGuiModelSelectTextField extends iPartsGuiArraySelectionTextField {

    public static final String TYPE = "iPartsGuiModelSelectTextField";

    private String partialModelNumberWithWildCard;
    private boolean modelNumberSearchFieldVisible;

    public iPartsGuiModelSelectTextField(EtkProject project) {
        super(project, TYPE);
    }

    public void init(final AbstractJavaViewerForm parentForm) {
        if (parentForm != null) {
            removeEventListeners(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT);
            addEventListener(new EventListener(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(Event event) {
                    Collection<String> modelNumbers = EditSelectModelsForm.showSelectionModels(parentForm, productId,
                                                                                               partialModelNumberWithWildCard,
                                                                                               modelNumberSearchFieldVisible,
                                                                                               dataArray.getArrayAsStringList());
                    if (modelNumbers != null) {
                        addDataArrayFromSelection(modelNumbers);
                    }
                }
            });
            addRadioButtons("!!Vereinheitlichen", "!!Hinzufügen");
        } else {
            removeEventListeners(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT);
        }
    }

    public void reinit(final AbstractJavaViewerForm parentForm, MasterDataProductCharacteristics currentMasterDataProductCharacteristics) {
        if (parentForm != null) {
            removeEventListeners(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT);
            addEventListener(new EventListener(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(Event event) {
                    Collection<String> modelNumbers = EditSelectModelsForm.showSelectionModels(parentForm, productId,
                                                                                               partialModelNumberWithWildCard,
                                                                                               modelNumberSearchFieldVisible,
                                                                                               dataArray.getArrayAsStringList(),
                                                                                               currentMasterDataProductCharacteristics);
                    if (modelNumbers != null) {
                        addDataArrayFromSelection(modelNumbers);
                    }
                }
            });
            addRadioButtons("!!Vereinheitlichen", "!!Hinzufügen");
        } else {
            removeEventListeners(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT);
        }
    }

    @Override
    protected String getArrayAsFormattedString() {
        // String mit den visualisierten Arraywerten
        return project.getVisObject().getArrayAsFormattedString(dataArray, "", project.getDBLanguage(), iPartsConst.TABLE_KATALOG,
                                                                iPartsConst.FIELD_K_MODEL_VALIDITY, false);
    }

    /**
     * Liefert die (partielle) Baumusternummer zurück, die auch Wildcards enthalten kann, für die Suche/Vorfilterung der verfügbaren
     * Baumuster.
     *
     * @return
     */
    public String getPartialModelNumberWithWildCard() {
        return partialModelNumberWithWildCard;
    }

    /**
     * Setzt eine (partielle) Baumusternummer, die auch Wildcards enthalten kann, für die Suche/Vorfilterung der verfügbaren
     * Baumuster.
     *
     * @param partialModelNumberWithWildCard
     */
    public void setPartialModelNumberWithWildCard(String partialModelNumberWithWildCard) {
        this.partialModelNumberWithWildCard = partialModelNumberWithWildCard;
    }

    /**
     * Setzt das Flag, ob das Suchfeld für die Baumusternummer angezeigt werden soll.
     *
     * @return
     */
    public boolean isModelNumberSearchFieldVisible() {
        return modelNumberSearchFieldVisible;
    }

    /**
     * Liefert das Flag zurück, ob das Suchfeld für die Baumusternummer angezeigt werden soll.
     *
     * @param modelNumberSearchFieldVisible
     */
    public void setModelNumberSearchFieldVisible(boolean modelNumberSearchFieldVisible) {
        this.modelNumberSearchFieldVisible = modelNumberSearchFieldVisible;
    }

    /**
     * Weist diesem Baumuster-Gültigkeits-Control ein zugehöriges SAA-/BK-Gültigkeits-Control zu. Der Input der Baumuster-
     * gültigkeit beeinflusst in den meisten Fällen den Input der SAA-/BK-Gültigkeit.
     *
     * @param saaBkSelectTextField
     */
    public void assignRelatedSaaBkControl(final iPartsGuiSAABkSelectTextField saaBkSelectTextField) {
        addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                EtkDataArray dataArray = getArray();
                saaBkSelectTextField.setModelList(dataArray.getArrayAsStringList());
            }
        });
    }
}