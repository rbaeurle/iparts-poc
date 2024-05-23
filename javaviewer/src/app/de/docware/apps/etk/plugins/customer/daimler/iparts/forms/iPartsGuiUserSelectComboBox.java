package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.framework.combimodules.useradmin.db.UserDbObject;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.util.StrUtils;

import java.util.Map;

/**
 * {@link RComboBox} zur Auswahl von einem {@link UserDbObject}.
 * Die Liste der {@link UserDbObject}s wird i.A. noch bei init() übergeben.
 * Ist die User-Verwaltung bei iParts klar, so kann diese Methode benutzt werden, die {@link UserDbObject}s selbst zu bestimmen.
 */
public class iPartsGuiUserSelectComboBox extends RComboBox<String> {

    /**
     * Initialisiert die Auswahlliste mit der übergebenen Map mit Name auf ID.
     *
     * @param usersMap       Benutzername bzw. virtuelle Benutzergruppe als Schlüssel und ID als Wert
     * @param addEmptyUser
     * @param selectUserId
     * @param excludeUserIds
     */
    public void init(Map<String, String> usersMap, boolean addEmptyUser, String selectUserId, String... excludeUserIds) {
        setMaximumRowCount(20);
        switchOffEventListeners();
        removeAllItems();
        if (addEmptyUser && !usersMap.isEmpty()) {
            addItem(null, "");
        }
        int selectedIndex = -1;

        // Benutzer bzw. virtuelle Benutzergruppen zur ComboBox hinzufügen
        for (Map.Entry<String, String> userEntry : usersMap.entrySet()) {
            String userId = userEntry.getValue();
            if ((excludeUserIds != null) && StrUtils.arrayContains(userId, excludeUserIds)) {
                continue;
            }

            if ((selectUserId != null) && userId.equals(selectUserId)) {
                selectedIndex = getItemCount();
            }
            addItem(userId, userEntry.getKey());
        }

        setSelectedIndex(selectedIndex);
        setEnabled(getItemCount() > 0);
        switchOnEventListeners();
    }
}
