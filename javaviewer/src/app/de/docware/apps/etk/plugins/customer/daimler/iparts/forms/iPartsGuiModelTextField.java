package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.misc.validator.GuiControlValidator;
import de.docware.framework.modules.gui.misc.validator.ValidationState;
import de.docware.util.StrUtils;

public class iPartsGuiModelTextField extends iPartsGuiIdentTextField {

    public static final String TYPE = "ipartsmodeltextfield";

    private String errorMessage;
    private String warningMessage;

    public iPartsGuiModelTextField() {
        super();
        setType(TYPE);
        GuiControlValidator oldValidator = getValidator();
        this.setValidator(new GuiControlValidator() {
            @Override
            public ValidationState validate(AbstractGuiControl control) {
                return validateModel();
            }
        });
    }

    /**
     * Abfrage für Fehlermeldung bei vorangegangener Gültigkeitsprüfung
     * {@link #checkInput(EtkProject project)}
     *
     * @return
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Abfrage für Warnungen bei vorangegangener Gültigkeitsprüfung
     * {@link #checkInput(EtkProject project)}
     *
     * @return
     */
    public String getWarningMessage() {
        return warningMessage;
    }

    private ValidationState validateModel() {
        boolean isValid = true;
        String ident = getText();
        if (StrUtils.isValid(ident)) {
            isValid = iPartsModel.isModelNumberValid(ident);
            if (isValid) {
                // nur AS-BMs zulassen
                isValid = ident.length() == 7;
            }
        }
        return new ValidationState(isValid);
    }

    private void clearErrorsAndWarnings() {
        errorMessage = null;
        warningMessage = null;
    }

    public boolean checkInput(EtkProject project) {
        clearErrorsAndWarnings();
        // keine Bauhreihe hinterlegt -> Speicherung ohne Hinweis
        if ((project == null) || !StrUtils.isValid(getSeriesNo())) {
            return true;
        }
        String text = getText();
        // leeres Baumuster -> Speicherung ohne Hinweis
        if (!StrUtils.isValid(text)) {
            return true;
        }
        if (!isIdentValid()) {
            errorMessage = TranslationHandler.translate("!!Die eingegebene Baumusternummer \"%1\" ist ungültig!", text);
            return false;
        }
        iPartsModel currentModel = iPartsModel.getInstance(project, new iPartsModelId(text));
        if ((currentModel == null) || !currentModel.existsInDB()) {
            errorMessage = TranslationHandler.translate("!!Das eingegebene Baumuster \"%1\" existiert nicht!", text);
            return false;
        }
        //Baumuster passt nicht zur Baureihe -> Speicherung mit Hinweis
        if (!text.startsWith(getSeriesNo())) {
            warningMessage = TranslationHandler.translate("!!Das eingegebene Baumuster \"%1\" passt nicht zur Baureihe \"%2\"!", text, getSeriesNo());
            return false;
        }
        // Baumuster passt zur Baureihe -> Speicherung ohne Hinweis
        return true;
    }
}
