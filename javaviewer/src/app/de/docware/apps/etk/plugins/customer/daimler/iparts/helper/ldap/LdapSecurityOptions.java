/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ldap;

import de.docware.framework.modules.gui.misc.translation.TranslationHandler;

/**
 * Verschlüsselungsstufen für die LDAP Verbindung
 */
public enum LdapSecurityOptions {
    NONE("!!Keine", "none"),
    SIMPLE("!!Schwach", "simple");

    private String description;
    private String contextValue;

    LdapSecurityOptions(String description, String contextValue) {
        this.description = description;
        this.contextValue = contextValue;
    }

    public String getDescription() {
        return description;
    }

    public String getContextValue() {
        return contextValue;
    }

    public static String[] getDescriptions() {
        String[] descriptions = new String[values().length];
        for (int i = 0; i < values().length; i++) {
            descriptions[i] = values()[i].getDescription();
        }
        return descriptions;
    }

    public LdapSecurityOptions getFromDescription(String configValueAsString) {
        String currentValue = TranslationHandler.translate(configValueAsString);
        for (LdapSecurityOptions ldapSecurityOption : values()) {
            String securityValue = TranslationHandler.translate(ldapSecurityOption.getDescription());
            if (securityValue.equalsIgnoreCase(currentValue)) {
                return ldapSecurityOption;
            }
        }
        return null;
    }
}
