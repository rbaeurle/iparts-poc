package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.util.StrUtils;

/**
 * TextField für die Eingabe von PosE und PosV in der Konstruktion (nur digits erlaubt, Ergebnis wird links mit Nullen aufgefüllt)
 */
public class iPartsGuiConstPosTextField extends iPartsGuiHotSpotTextField {

    private static final int POS_LEN = 4;
    private static final char POS_FILL_CHAR = '0';

    public static final String TYPE = "ipartsconstpostextfield";

    /**
     * Überprüft, ob der übergebene Wert in iParts erzeugt wurde.
     *
     * @param value
     * @return
     */
    public static boolean isIPartsCreatedValue(String value) {
        return iPartsDialogBCTEPrimaryKey.isIPartsCreatedPosV(value);
    }

    /**
     * Markiert den übergebenen Wert als in iParts erzeugt.
     *
     * @param value
     * @return
     */
    public static String makeIPartsCreatedValue(String value) {
        return iPartsDialogBCTEPrimaryKey.makeIPartsCreatedPosV(value);
    }

    private boolean checkInput = true; // Flag um die Syntax des zu setzenden Textes zu prüfen

    public iPartsGuiConstPosTextField() {
        super();
        setType(TYPE);
    }

    @Override
    public String getText() {
        String text = super.getText();
        if (StrUtils.isValid(text)) {
            text = StrUtils.leftFill(text, POS_LEN, POS_FILL_CHAR);
        }
        return text;
    }

    @Override
    protected String controlText(String actText) {
        // Soll der Positionswert nicht
        if (!checkInput) {
            return actText;
        }
        actText = super.controlText(actText);
        return StrUtils.copySubString(actText, 0, POS_LEN);
    }

    /**
     * Setzt den Positionsvariantenwert mit optionaler Syntaxprüfung
     *
     * @param textValue
     */
    public void setText(String textValue, boolean checkInput) {
        if (checkInput) {
            super.setText(textValue);
        } else {
            boolean oldValue = this.checkInput;
            setCheckInput(false);
            super.setText(textValue);
            setCheckInput(oldValue);
        }
    }

    public void setCheckInput(boolean checkInput) {
        this.checkInput = checkInput;
    }
}
