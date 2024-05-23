package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.util.StrUtils;

/**
 * Erweiterung von [@link iPartsGuiTextFieldBackgroundToggle}
 * Zugelassen werden nur AlphaNum-Characters (ohne Umlaute) und alles Groß geschrieben
 * Zusätzlich können noch WhiteSpaces (' ' und \t) zugelassen werden
 */
public class iPartsGuiAlphaNumTextField extends iPartsGuiTextFieldBackgroundToggle {

    public static final String TYPE = "ipartsalphanumtextfield";

    private EtkProject project;
    private boolean withWhitspaces;  // Leerzeichen erlaubt?

    public iPartsGuiAlphaNumTextField() {
        super();
        setType(TYPE);
    }

    public iPartsGuiAlphaNumTextField(String text) {
        super(text);
        setType(TYPE);
    }

    public void init(EtkProject project) {
        this.project = project;
    }

    public boolean isInit() {
        return project != null;
    }

    protected EtkProject getProject() {
        return project;
    }

    public boolean isWithWhitspaces() {
        return withWhitspaces;
    }

    public void setWithWhitspaces(boolean withWhitspaces) {
        this.withWhitspaces = withWhitspaces;
    }

    @Override
    protected String controlText(String text) {
        if (StrUtils.isValid(text)) {
            StringBuilder str = new StringBuilder();
            for (int lfdNr = 0; lfdNr < text.length(); lfdNr++) {
                char ch = text.charAt(lfdNr);
                if (Character.isLetterOrDigit(ch)) {
                    // .isLetter(ch) lässt Umlaute etc durch, deswegen nochmal gezielt abgefragt
                    if ((ch >= '0') && (ch <= '9')) {
                        str.append(ch);
                    } else if ((ch >= 'A') && (ch <= 'Z')) {
                        str.append(ch);
                    } else if ((ch >= 'a') && (ch <= 'z')) {
                        str.append(Character.toUpperCase(ch));
                    }
                } else if (withWhitspaces) {
                    if (ch == ' ') {
                        str.append(ch);
                    } else if (ch == '\t') {
                        str.append(' ');
                    }
                }
            }
            return str.toString();
        }
        return text;
    }
}
