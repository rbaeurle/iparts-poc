package de.docware.apps.etk.plugins.customer.daimler.iparts.forms.formattedfields;

import de.docware.apps.etk.base.config.EtkConfig;
import de.docware.apps.etk.base.config.db.datatypes.date.DateConfig;
import de.docware.apps.etk.base.config.db.datatypes.date.DateDataType;

/**
 * Erweiterung für Datentyp Date
 */
public class EditExtControlCalendar extends GuiExtCalendar {

    private DateDataType dateType;

    public void init(EtkConfig config, String tableName, String fieldName, String language) {
        dateType = new DateDataType(tableName, fieldName);
        dateType.loadConfig(config, "");
        setCustomPatterns(DateConfig.getInstance(config).asDateProperties(config.getDatabaseLanguages()));
        setDateLanguage(language);
    }
}
