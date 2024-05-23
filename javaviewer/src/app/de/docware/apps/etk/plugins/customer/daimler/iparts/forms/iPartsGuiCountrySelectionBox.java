package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.forms.common.EnumRComboBox;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.util.StrUtils;
import de.docware.util.Utils;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Combobox für die Anzeige von Ländergültigkeiten.
 * Kann als Multiselect Variante mit Checkboxen und als Singleselect Variante benutzt werden.
 */
public class iPartsGuiCountrySelectionBox extends EnumRComboBox {

    public static final String COUNTRY_DB_DELIMITER = "|";  // muss aus EditImportPSKForm entfernt werden

    private Mode checkboxMode;

    public iPartsGuiCountrySelectionBox(EtkProject project, Mode checkboxMode, String initialValue) {
        super();
        this.checkboxMode = checkboxMode;
        setMode(this.checkboxMode);
        setEnumTexte(project, iPartsConst.DAIMLER_ISO_COUNTRY_CODE_ENUM_NAME, project.getDBLanguage(), true, true);
        if (initialValue == null) {
            setSelectedIndex(-1);
        } else {
            setSelectedUserObject(initialValue);
        }

    }

    public iPartsGuiCountrySelectionBox(EtkProject project, String initialValue) {
        this(project, Mode.STANDARD, initialValue);
    }

    @Override
    protected boolean omitEmptyToken() {
        if (checkboxMode == Mode.CHECKBOX) {
            return true;
        }
        return false;
    }

    @Override
    public String getActToken() {
        Set<String> countries = getSelectedCountryCodes();
        if (countries != null) {
            return StrUtils.stringListToString(countries, COUNTRY_DB_DELIMITER);

        }
        return "";
    }

    @Override
    public void setActToken(String value) {
        if (StrUtils.isValid(value)) {
            setSelectedUserObjects(StrUtils.toStringArray(value, COUNTRY_DB_DELIMITER, false, false));
        }
    }

    public String getSelectedCountryCode() {
        return getSelectedUserObject();
    }

    public Set<String> getSelectedCountryCodes() {
        List<String> selectedUserObjects = getSelectedUserObjects();
        if (Utils.isValid(selectedUserObjects)) {
            Set<String> result = new TreeSet<>();
            result.addAll(selectedUserObjects);
            return result;
        }
        return null;
    }
}
