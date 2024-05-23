/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.config;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.viewer.webapp.deploytool.forms.BaseSettingsPanel;
import de.docware.framework.combimodules.useradmin.db.UserAdminDbActions;
import de.docware.framework.combimodules.useradmin.db.factory.UserAdminDbActionsBuilder;
import de.docware.framework.modules.config.ConfigBase;
import de.docware.util.sql.pool.ConnectionPool;

/**
 * Bauplan (Builder) für die Userverwaltung im iParts
 */
public class iPartsUserAdminDbBuilder implements UserAdminDbActionsBuilder {

    @Override
    public UserAdminDbActions createInstance(ConfigBase config, String dbAlias) {
        return iPartsUserAdminDb.createInstance(config, dbAlias, iPartsPlugin.getMqProject());
    }

    @Override
    public String getUserDBAlias(ConfigBase config) {
        // Immer die DB-Verbindungseinstellungen von den Grundeinstellungen (der aktuellen Umgebungsvariante) verwenden
        return config.getString(BaseSettingsPanel.XML_CONFIG_PATH_BASE + BaseSettingsPanel.XML_CONFIG_SUBPATH_DB_ALIAS, "");
    }

    @Override
    public boolean showAppMigrationPanel() {
        return false;
    }

    /**
     * Die Konfiguration von "Passwort Standard Richtlinie" wird IParts nicht benötigt.
     *
     * @return false
     */
    @Override
    public boolean forceCreateDefaultPasswordPolicy() {
        return false;
    }

    @Override
    public boolean useEtkModeForUserAdminDb() {
        return true;
    }

    @Override
    public boolean isActivatedByAdmin() {
        return iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_USER_ADMIN_ENABLED);
    }

    @Override
    public boolean modifyUserDBProperties(ConnectionPool connectionPool) {
        return false;
    }
}
