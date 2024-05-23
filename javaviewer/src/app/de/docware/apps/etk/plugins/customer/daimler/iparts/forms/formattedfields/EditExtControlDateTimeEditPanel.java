package de.docware.apps.etk.plugins.customer.daimler.iparts.forms.formattedfields;

import de.docware.apps.etk.base.config.EtkConfig;
import de.docware.apps.etk.base.config.db.datatypes.date.DateConfig;
import de.docware.apps.etk.base.config.db.datatypes.date.DateDataType;
import de.docware.apps.etk.base.config.db.datatypes.datetime.DateTimeDataType;

import java.util.List;

/**
 * Erweiterung vom {@link GuiExtDateTimeEditPanel} für Datentyp DateTime
 */
public class EditExtControlDateTimeEditPanel extends GuiExtDateTimeEditPanel {


    private DateDataType dateTimeType;

    public void init(EtkConfig config, String tableName, String fieldName, String language) {
        dateTimeType = new DateTimeDataType(tableName, fieldName);
        dateTimeType.loadConfig(config, "");
        DateConfig dateConfig = DateConfig.getInstance(config);
        List<String> dbLanguages = config.getDatabaseLanguages();
        setCustomPatterns(dateConfig.asDateProperties(dbLanguages), dateConfig.asTimeProperties(dbLanguages));
        setDateTimeLanguage(language);
        setShowSeconds(DateConfig.withSeconds(dateConfig.getTimePattern(language)));
    }
}
