/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.application;

import de.docware.apps.etk.base.config.EtkConfig;
import de.docware.apps.etk.base.config.db.EtkDatabaseDescription;
import de.docware.apps.etk.plugins.customer.daimler.iparts.database.iPartsDBMigrations;
import de.docware.framework.modules.config.ConfigContainer;
import de.docware.framework.modules.config.db.DBConst;
import de.docware.framework.modules.config.db.DBDatabaseDescription;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.util.sql.TableAndFieldName;

import java.util.HashMap;
import java.util.Map;

public class iPartsEtkConfig extends EtkConfig {

    public iPartsEtkConfig(ConfigContainer mainContainer, ConfigContainer userContainer) {
        super(mainContainer, userContainer);
    }

    @Override
    protected void loadDBDatabaseDescription(DBDatabaseDescription databaseDescription, ConfigContainer container, String pathForDescription) {
        super.loadDBDatabaseDescription(databaseDescription, container, pathForDescription);
        if ((databaseDescription instanceof EtkDatabaseDescription) && pathForDescription.equals(DBConst.DATABASETABLES)) {
            iPartsDBMigrations migrations = new iPartsDBMigrations(this);
            // Original TypeConfigurations und Texte aufsammeln
            Map<String, EtkDatabaseField> fieldNameToDBField = new HashMap<>();
            databaseDescription.getTableList().forEach(table -> {
                table.getFieldList().forEach(field -> {
                    String tableFieldName = TableAndFieldName.make(table.getName(), field.getName());
                    fieldNameToDBField.put(tableFieldName, field);
                });
            });
            databaseDescription.clear();

            // Ausführung in einem eigenen Thread, weil der aktuelle Thread bereits zu einer Session gehört und in
            // addStructureDefinitionToDatabaseDescription() eine neue Session erzeugt wird für den aktuellen Thread
            FrameworkThread thread = new FrameworkThread("AddStructureDefinitionToDatabaseDescriptionThread", Thread.NORM_PRIORITY, () -> {
                try {
                    migrations.addStructureDefinitionToDatabaseDescription((EtkDatabaseDescription)databaseDescription, !container.isFileReadOnly());
                } finally {
                    migrations.clearReferences();
                }
            });
            thread.__internal_start();
            thread.waitFinished();

            // TypeConfiguration setzen
            databaseDescription.getTableList().forEach(table -> {
                table.getFieldList().forEach(field -> {
                    String tableFieldName = TableAndFieldName.make(table.getName(), field.getName());
                    EtkDatabaseField oldField = fieldNameToDBField.get(tableFieldName);
                    if (oldField != null) {
                        field.setTypeConfiguration(oldField.getTypeConfiguration());
                        field.setDisplayName(oldField.getDisplayName());
                        field.setUserDescription(oldField.getUserDescription());
                    }
                });
            });
        }
    }
}
