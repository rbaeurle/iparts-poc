/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.iPartsVINModelMappingCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.VinId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.misc.validator.GuiControlValidator;
import de.docware.framework.modules.gui.misc.validator.ValidationState;
import de.docware.util.StrUtils;

import java.util.List;

/**
 * {@link de.docware.framework.modules.gui.controls.GuiButtonTextField} zur Eingabe einer FIN oder eines Baumusters.
 */
public class iPartsGuiFinModelTextField extends iPartsGuiFinTextField {

    public static final String TYPE = "ipartsfinmodeltextfield";

    private EtkProject project;

    public iPartsGuiFinModelTextField() {
        super();
        setType(TYPE);
        project = null;
        final GuiControlValidator oldValidator = getValidator();
        this.setValidator(new GuiControlValidator() {
            @Override
            public ValidationState validate(AbstractGuiControl control) {
                if (isMandatory()) {
                    ValidationState vState = oldValidator.validate(control);
                    if (!vState.isValid()) {
                        vState = new ValidationState(getModelNo().isEmpty() || isModelNoValid());
                    }
                    if (!vState.isValid()) {
                        vState = new ValidationState(getModelNo().isEmpty() || isVinValid());
                    }
                    if (!vState.isValid()) {
                        // VIN-Fallback überprüfen
                        vState = new ValidationState(getModelNo().isEmpty() || isVinFallbackModelValid());
                    }
                    return vState;
                }
                return oldValidator.validate(control);
            }
        });
    }

    public iPartsGuiFinModelTextField(boolean isMandatory) {
        this();
        setMandatoryInput(isMandatory);
    }

    public void initForVinFallback(EtkProject project) {
        this.project = project;
    }

    public boolean isModelNoValid() {
        return iPartsModel.isModelNumberValid(getModelNo());
    }

    public String getModelNo() {
        String trimmedText = getTrimmedText();
        if (StrUtils.isValid(trimmedText)) {
            return trimmedText.toUpperCase();
        }
        return "";
    }

    public void setModelNo(String modelNo) {
        setText(modelNo);
        if (!eventListeners.isActive()) {
            // falls die EventListener ausgeschaltet sind: auf jeden Fall die Backgroundcolor nachziehen
            toggleBackColor(getValidationState().isValid());
        }
    }


    public boolean isVinValid() {
        return getVinId().isValidId();
    }

    public VinId getVinId() {
        return new VinId(getTrimmedText().toUpperCase());
    }

    public void setVin(String vin) {
        setText(vin);
        if (!eventListeners.isActive()) {
            // falls die EventListener ausgeschaltet sind: auf jeden Fall die Backgroundcolor nachziehen
            toggleBackColor(getValidationState().isValid());
        }
    }

    public boolean isVinFallbackModelValid() {
        return getVinFallbackModel() != null;
    }

    public String getVinFallbackModel() {
        String modelNo = getModelNo();
        if ((modelNo.length() == 7) && (project != null)) {
            List<String> modelNumbers = iPartsVINModelMappingCache.getInstance(project).getVisibleModelsForVINPrefix(project, modelNo);
            if (!modelNumbers.isEmpty()) {
                return modelNumbers.get(0);
            }
        }
        return null;
    }


    public boolean isFinOrModelValid() {
        return isFinValid() || isVinValid() || isModelNoValid() || isVinFallbackModelValid();
    }
}
