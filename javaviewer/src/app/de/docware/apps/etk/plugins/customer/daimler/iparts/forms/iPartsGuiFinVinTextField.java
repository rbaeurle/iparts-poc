/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.iPartsVINModelMappingCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.FinId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.VinId;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.misc.validator.GuiControlValidator;
import de.docware.framework.modules.gui.misc.validator.ValidationState;
import de.docware.util.StrUtils;
import de.docware.util.Utils;

import java.util.List;
import java.util.Set;

/**
 * {@link iPartsGuiFinTextField} zur Eingabe einer FIN/VIN.
 */
public class iPartsGuiFinVinTextField extends iPartsGuiFinTextField {

    public static final String TYPE = "ipartsfinvintextfield";

    private EtkProject project;

    public iPartsGuiFinVinTextField() {
        super();
        setType(TYPE);
        project = null;
        final GuiControlValidator finValidator = getValidator();  // Validator for FIN
        this.setValidator(new GuiControlValidator() {
            @Override
            public ValidationState validate(AbstractGuiControl control) {
                if (isMandatory()) {
                    ValidationState vState = finValidator.validate(control);
                    if (!vState.isValid()) {
                        vState = new ValidationState(isVinValid());
                    }
                    if (!vState.isValid()) {
                        // VIN-Fallback überprüfen
                        vState = new ValidationState(isVinFallbackModelValid());
                    }
                    return vState;
                }
                return finValidator.validate(control);
            }
        });

    }

    public iPartsGuiFinVinTextField(boolean isMandatory) {
        this();
        setMandatoryInput(isMandatory);
    }

    public void initForVinFallback(EtkProject project) {
        this.project = project;
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

    public String getModelNo() {
        if (isFinValid()) {
            return getFinId().getFullModelNumber();
        } else if (isVinValid()) {
            return getVinFallbackModel();
        }
        return "";
    }

    public boolean checkFinAgainstModelNos(Set<String> modelNumbers) {
        if (isFinValid()) {
            return modelNumbers.contains(getFinId().getFullModelNumber());
        } else if (isVinValid()) {
            return vinContainsModelNo(getVinId().getVIN(), modelNumbers);
        }
        return false;
    }

    public boolean checkFinAgainstModelNos(String identNo, Set<String> modelNumbers) {
        FinId finId = new FinId(identNo.toUpperCase());
        if (finId.isValidId()) {
            return modelNumbers.contains(finId.getFullModelNumber());
        } else {
            VinId vinId = new VinId(identNo.toUpperCase());
            if (vinId.isValidId()) {
                return vinContainsModelNo(vinId.getVIN(), modelNumbers);
            }
        }
        return false;
    }

    private boolean vinContainsModelNo(String vinNo, Set<String> modelNumbers) {
        if (StrUtils.isValid(vinNo)) {
            List<String> vinModelNumbers = iPartsVINModelMappingCache.getInstance(project).getVisibleModelsForVINPrefix(project, vinNo);
            if (Utils.isValid(modelNumbers)) {
                for (String modelNo : vinModelNumbers) {
                    if (modelNumbers.contains(modelNo)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String getVinFallbackModel() {
        String vinNo = getVinId().getVIN();
        List<String> modelNumbers = getVinModelNumbers(vinNo);
        if (Utils.isValid(modelNumbers)) {
            return modelNumbers.get(0);
        }
        return null;
    }

    private List<String> getVinModelNumbers(String vinNo) {
        if (project != null) {
            List<String> modelNumbers = iPartsVINModelMappingCache.getInstance(project).getVisibleModelsForVINPrefix(project, vinNo);
            if (!modelNumbers.isEmpty()) {
                return modelNumbers;
            }
        }
        return null;
    }

    public boolean isFinOrVinValid() {
        return isFinValid() || isVinValid() || isVinFallbackModelValid();
    }
}
