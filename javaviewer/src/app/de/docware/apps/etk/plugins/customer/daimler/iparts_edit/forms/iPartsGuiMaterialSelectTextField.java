/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.SelectSearchGridMaterial;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsSelectSearchGridMaterial;
import de.docware.framework.modules.gui.controls.GuiButtonTextField;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;

/**
 * {@link GuiButtonTextField} zur Auswahl von Material.
 */
public class iPartsGuiMaterialSelectTextField extends GuiButtonTextField {

    public static final String TYPE = "iPartsGuiMaterialSelectTextField";

    private String matNr;
    private boolean pskMaterialsAllowed;
    private EtkProject project;
    private String errorMessage;
    private String warningMessage;

    public iPartsGuiMaterialSelectTextField() {
        super();
        setType(TYPE);
        setEditable(true);
    }

    public void init(final AbstractJavaViewerForm parentForm, boolean pskMaterialsAllowed) {
        this.pskMaterialsAllowed = pskMaterialsAllowed;
        if (parentForm != null) {
            project = parentForm.getProject();
            super.addEventListener(new EventListener(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(Event event) {
                    SelectSearchGridMaterial selectSearchGridMaterial = new iPartsSelectSearchGridMaterial(parentForm, false,
                                                                                                           pskMaterialsAllowed);
                    selectSearchGridMaterial.setTitle("!!Teil auswählen");
                    String matNo = selectSearchGridMaterial.showGridSelectionDialog(getText());
                    if (StrUtils.isValid(matNo)) {
                        setText(matNo);
                    }
                }
            });
        } else {
            project = null;
            super.removeEventListeners(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT);
        }
    }

    @Override
    public void setEditable(boolean isEditable) {
        super.setEditable(false); // Auswahl nur über die Materialsuche
        this.getButton().setEnabled(isEditable);
    }

    @Override
    public void setText(String text) {
        matNr = text;
        if (project != null) {
            text = project.getVisObject().asText(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_BESTNR, text, project.getDBLanguage());
        }
        super.setText(text);
    }

    @Override
    public String getText() {
        if (matNr != null) {
            return matNr;
        }

        return "";
    }

    private void clearErrorsAndWarnings() {
        errorMessage = null;
        warningMessage = null;
    }

    /**
     * Abfrage für Fehlermeldung bei vorangegangener Gültigkeitsprüfung
     * {@link #checkInput()}
     *
     * @return
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Abfrage für Warnungen bei vorangegangener Gültigkeitsprüfung
     * {@link #checkInput()}
     *
     * @return
     */
    public String getWarningMessage() {
        return warningMessage;
    }

    public boolean checkInput() {
        clearErrorsAndWarnings();
        if (!StrUtils.isValid(matNr)) {
            errorMessage = TranslationHandler.translate("!!Keine Teilenummer angegeben!");
            return false;
        }
        EtkDataPart dataPart = EtkDataObjectFactory.getInstance().createDataPart(project, matNr, "");
        if (!dataPart.existsInDB() || (!pskMaterialsAllowed && dataPart.getFieldValueAsBoolean(iPartsConst.FIELD_M_PSK_MATERIAL))) {
            errorMessage = TranslationHandler.translate("!!Die Teilenummer \"%1\" existiert nicht!", matNr);
            return false;
        }
        return true;
    }
}

