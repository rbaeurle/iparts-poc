package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.misc.validator.GuiControlValidator;
import de.docware.framework.modules.gui.misc.validator.ValidationState;
import de.docware.util.StrUtils;

public class iPartsGuiIdentTextField extends iPartsGuiAlphaNumTextField {

    public static final String TYPE = "ipartsidenttextfield";

    private String seriesNo;
    private iPartsNumberHelper numberHelper;

    public iPartsGuiIdentTextField() {
        super();
        setType(TYPE);
        this.numberHelper = new iPartsNumberHelper();
        this.setValidator(new GuiControlValidator() {
            @Override
            public ValidationState validate(AbstractGuiControl control) {
                return validateIdent();
            }
        });
    }

    public String getSeriesNo() {
        return seriesNo;
    }

    public void setSeriesNo(String seriesNo) {
        this.seriesNo = seriesNo;
    }

    public boolean isIdentValid() {
        return getValidationState().isValid();
    }

    private ValidationState validateIdent() {
        boolean isValid = true;
        String ident = getText();
        int length = ident.length();
        if (length > 0) {
            if (StrUtils.isValid(seriesNo)) {
                isValid = validateIdentWithSeries(ident, length);
            } else {
                isValid = validateIdentWithoutSeries(ident, length);
            }
        }
        return new ValidationState(isValid);
    }

    private boolean validateIdentWithSeries(String ident, int length) {
        boolean isValid = false;
        if (iPartsModel.isVehicleModel(seriesNo)) {
/* Folgende Formate sind bei Fahrzeugbaureihen zulässig, wenn das Baureihenfeld mit einer C-Baureihe befüllt ist:

    6 stellig: nur Ziffern erlaubt
    7 stellig: erste Stelle Buchstaben und Ziffern erlaubt, Stellen 2-7 nur Ziffern erlaubt
 */
            switch (length) {
                case 6:
                    isValid = StrUtils.isDigit(ident);
                    break;
                case 7:
                    isValid = numberHelper.isIdentWellFormed(ident);
                    break;
            }
        } else {
/* Folgende Formate sind bei Aggregaten erlaubt, wenn das Baureihenfeld mit einer D-Baureihe befüllt ist:

    6 stellig: nur Ziffern erlaubt
    7 stellig: erste Stelle Buchstaben und Ziffern erlaubt, Stellen 2-7 nur Ziffern erlaubt
    8 stellig erste Stelle Buchstaben und Ziffern erlaubt, Stellen 2-8 nur Ziffern erlaubt
 */
            switch (length) {
                case 6:
                    isValid = StrUtils.isDigit(ident);
                    break;
                case 7:
                case 8:
                    isValid = numberHelper.isIdentWellFormed(ident);
                    break;
            }
        }
        return isValid;
    }

    private boolean validateIdentWithoutSeries(String ident, int length) {
/* Folgende Formate sind erlaubt, wenn das Baureihenfeld "leer" ist:

    6 stellig: nur Ziffern erlaubt
    7 stellig: erste Stelle Buchstaben und Ziffern erlaubt, Stellen 2-7 nur Ziffern erlaubt
    8 stellig erste Stelle Buchstaben und Ziffern erlaubt, Stellen 2-8 nur Ziffern erlaubt
 */
        boolean isValid = false;
        switch (length) {
            case 6:
                isValid = StrUtils.isDigit(ident);
                break;
            case 7:
            case 8:
                isValid = numberHelper.isIdentWellFormed(ident);
                break;
        }
        return isValid;
    }
}
