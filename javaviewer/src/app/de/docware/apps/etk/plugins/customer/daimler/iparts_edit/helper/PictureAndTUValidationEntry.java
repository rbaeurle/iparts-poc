/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.project.mechanic.ids.PoolEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsEditBaseValidationForm;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

/**
 * Ein Eintrag in der Qualitätsprüfungs-Tabelle für Bilder und den TU.
 */
public class PictureAndTUValidationEntry {

    private IdWithType validationObject;
    private String validationObjectVisValue;
    private iPartsEditBaseValidationForm.ValidationResult validationResult;
    private String validationMessage;
    private String additionalInformation;

    public PictureAndTUValidationEntry(IdWithType validationObject, iPartsEditBaseValidationForm.ValidationResult validationResult,
                                       String validationMessage, String additionalInformation) {
        this.validationObject = validationObject;
        this.validationResult = validationResult;
        this.validationMessage = validationMessage;
        this.additionalInformation = additionalInformation;
    }

    /**
     * Den Anzeige String aus der ID lesen
     *
     * @return
     */
    public String getVisiualValueOfId() {
        if (validationObject.getType().equals(AssemblyId.TYPE)) {
            AssemblyId assemblyId = new AssemblyId(validationObject.toStringArrayWithoutType());
            return assemblyId.getKVari(); // K_VARI
        } else if (validationObject.getType().equals(PartListEntryId.TYPE)) {
            PartListEntryId partListEntryId = new PartListEntryId(validationObject.toStringArrayWithoutType());
            return partListEntryId.getKVari() + ", " + partListEntryId.getKLfdnr(); // K_VARI, K_LFDNR
        } else if (validationObject.getType().equals(PoolEntryId.TYPE)) {
            PoolEntryId poolEntryId = new PoolEntryId(validationObject.toStringArrayWithoutType());
            return poolEntryId.getPEImages(); // // PE_IMAGES
        } else {
            return validationObject.toString(", ");
        }
    }

    public void setValidationObject(IdWithType validation_object) {
        this.validationObject = validation_object;
    }

    public void setValidationResult(iPartsEditBaseValidationForm.ValidationResult validationResult) {
        this.validationResult = validationResult;
    }

    public IdWithType getValidationObject() {
        return validationObject;
    }

    public iPartsEditBaseValidationForm.ValidationResult getValidationResult() {
        return validationResult;
    }

    public String getValidation_resultAsDBValue() {
        return validationResult.getDbValue();
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    public String getValidationObjectVisValue() {
        return validationObjectVisValue;
    }

    public void setValidationObjectVisValue(String validationObjectVisValue) {
        if (StrUtils.isValid(validationObjectVisValue)) {
            this.validationObjectVisValue = validationObjectVisValue;
        }
    }

    public void setVisValueFromValidationId() {
        this.validationObjectVisValue = getVisiualValueOfId();
    }
}
