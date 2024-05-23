package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.framework.modules.gui.controls.GuiTextField;
import de.docware.util.StrUtils;

import java.util.List;

/**
 * Normales GuiTextField für mehrere Werte, wobei der Delimiter in der DB ein anderer ist, als visualisiert wird
 */
public class iPartsGuiDoubleDelimitedTextField extends GuiTextField {

    public static final String TYPE = "ipartsdoubledelimitedtextfield";
    public static final String DEFAULT_DB_DELIMITER = "|";
    public static final String DEFAULT_SHOW_DELIMITER = ", ";

    private String dbDelimiter;
    private String showDelimiter;

    public iPartsGuiDoubleDelimitedTextField() {
        super();
        setType(TYPE);
        setDbDelimiter(DEFAULT_DB_DELIMITER);
        setShowDelimiter(DEFAULT_SHOW_DELIMITER);
    }

    public iPartsGuiDoubleDelimitedTextField(String text) {
        this();
        setText(text);
    }

    public void setDbDelimiter(String dbDelimiter) {
        this.dbDelimiter = dbDelimiter;
    }

    public void setShowDelimiter(String showDelimiter) {
        this.showDelimiter = showDelimiter;
    }


    @Override
    public void setText(String text) {
        List<String> undelimitedTextList = StrUtils.toStringList(text, dbDelimiter, true, false);
        String showDelimitedText = StrUtils.stringListToString(undelimitedTextList, showDelimiter);
        super.setText(showDelimitedText);
    }

    @Override
    public String getText() {
        String text = super.getText();
        List<String> undelimitedTextList = StrUtils.toStringList(text, showDelimiter.trim(), true, true);
        return StrUtils.stringListToString(undelimitedTextList, dbDelimiter);
    }
}
