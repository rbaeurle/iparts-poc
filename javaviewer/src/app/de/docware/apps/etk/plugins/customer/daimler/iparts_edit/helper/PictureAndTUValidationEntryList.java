/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsEditBaseValidationForm;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;

/**
 * Eine Liste von Einträgen in der Qualitätsprüfungs-Tabelle für Bilder und den TU.
 */
public class PictureAndTUValidationEntryList extends DwList<PictureAndTUValidationEntry> {

    public PictureAndTUValidationEntry getLast() {
        if (size() > 0) {
            return this.get(size() - 1);
        }
        return null;
    }

    /**
     * Mindestens ein Warnung
     *
     * @return
     */
    public boolean isWarningInList() {
        boolean isWarning = false;
        for (PictureAndTUValidationEntry entry : this) {
            if (entry.getValidationResult() == iPartsEditBaseValidationForm.ValidationResult.WARNING) {
                isWarning = true;
                break;
            }
        }
        return isWarning;
    }

    /**
     * Mindestens ein Fehler
     *
     * @return
     */
    public boolean isErrorInList() {
        boolean isError = false;
        for (PictureAndTUValidationEntry entry : this) {
            if (entry.getValidationResult() == iPartsEditBaseValidationForm.ValidationResult.ERROR) {
                isError = true;
                break;
            }
        }
        return isError;
    }

    /**
     * Hat Fehler?
     *
     * @return
     */
    public boolean getIsError() {
        return isErrorInList();
    }

    /**
     * Hat Wanungen?
     */
    public boolean getIsWarning() {
        return isWarningInList();
    }

    public void addWarningT(IdWithType validationId, String key, String... placeHolderTexts) {
        addWarning(validationId, TranslationHandler.translate(key, placeHolderTexts));
    }

    public void addWarning(IdWithType validationId, String message) {
        addWarning(validationId, null, message, "");
    }

    public void addWarning(IdWithType validationId, String object_vis, String message) {
        addWarning(validationId, object_vis, message, "");
    }

    public void addWarning(IdWithType validationId, String object_vis, String message, String information) {
        addValidationEntry(validationId, object_vis, iPartsEditBaseValidationForm.ValidationResult.WARNING, message, information);
    }

    public void addErrorT(IdWithType validationId, String key, String... placeHolderTexts) {
        addError(validationId, TranslationHandler.translate(key, placeHolderTexts));
    }

    public void addError(IdWithType validationId, String message) {
        addError(validationId, null, message, "");
    }

    public void addError(IdWithType validationId, String object_vis, String message) {
        addError(validationId, object_vis, message, "");
    }

    public void addError(IdWithType validationId, String object_vis, String message, String information) {
        addValidationEntry(validationId, object_vis, iPartsEditBaseValidationForm.ValidationResult.ERROR, message, information);
    }

    public void addOK(IdWithType validationId, String message) {
        addOK(validationId, null, message, "");
    }

    public void addOK(IdWithType validationId, String object_vis, String message, String information) {
        addValidationEntry(validationId, object_vis, iPartsEditBaseValidationForm.ValidationResult.OK, message, information);
    }

    protected void addValidationEntry(IdWithType validationId, String object_vis, iPartsEditBaseValidationForm.ValidationResult result,
                                      String message, String information) {
        PictureAndTUValidationEntry newEntry = new PictureAndTUValidationEntry(validationId, result,
                                                                               message, information);
        if (StrUtils.isValid(object_vis)) {
            newEntry.setValidationObjectVisValue(object_vis);
        } else {
            newEntry.setVisValueFromValidationId();
        }
        add(newEntry);
    }

}
