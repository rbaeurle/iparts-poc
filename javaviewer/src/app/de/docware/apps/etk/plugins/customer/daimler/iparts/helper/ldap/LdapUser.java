/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ldap;

import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Repräsentiert einen LDAP User im Directory (eventuell nur temporär bis das Rechte-/Rollenmanagement implementiert wird)
 */
public class LdapUser {

    private String givenName;
    private String surname;
    private String uid;
    private Set<String> roles;
    private String commonName;
    private String statusText;
    private String[] statusTextPlaceHolders;
    private String email;
    private String corporateAffiliation;
    private String costUnit;

    public LdapUser(Properties ldapProperties) {
        if (ldapProperties != null) {
            fillFromProperties(ldapProperties);
        }
    }

    public LdapUser(String statusText, String... statusTextPlaceHolders) {
        this.statusText = statusText;
        this.statusTextPlaceHolders = statusTextPlaceHolders;
    }

    private void fillFromProperties(Properties ldapProperties) {
        givenName = getValueForLDAPKey(ldapProperties, LDAPHelper.LDAP_ATTRIBUTE_KEY_GIVEN_NAME);
        surname = getValueForLDAPKey(ldapProperties, LDAPHelper.LDAP_ATTRIBUTE_KEY_SURNAME);
        uid = getValueForLDAPKey(ldapProperties, LDAPHelper.LDAP_ATTRIBUTE_KEY_UID);
        commonName = getValueForLDAPKey(ldapProperties, LDAPHelper.LDAP_ATTRIBUTE_KEY_COMMON_NAME);
        email = getValueForLDAPKey(ldapProperties, LDAPHelper.LDAP_ATTRIBUTE_KEY_EMAIL);
        corporateAffiliation = getValueForLDAPKey(ldapProperties, LDAPHelper.LDAP_ATTRIBUTE_KEY_CORPORATE_AFFILIATION);
        costUnit = getValueForLDAPKey(ldapProperties, LDAPHelper.LDAP_ATTRIBUTE_KEY_COST_UNIT);

        if (ldapProperties.containsKey(LDAPHelper.LDAP_ATTRIBUTE_KEY_ROLE)) {
            // hier werden die Rollen aus dem Directory für den Benutzer ausgelesen
            // Das Directory gibt den Rollen String mit dem Attribut als Prefix aus -> Schneide den Attributnamen ab
            // Sollte es sich um ein "multi-value" Attribut handeln, werden die Rollen im String durch ein "," getrennt
            String roles = removeLdapAttributePrefix(ldapProperties.getProperty(LDAPHelper.LDAP_ATTRIBUTE_KEY_ROLE),
                                                     LDAPHelper.LDAP_ATTRIBUTE_KEY_ROLE);
            this.roles = new HashSet<>(StrUtils.toStringList(roles, ",", false, true));
        }
    }

    private String getValueForLDAPKey(Properties ldapProperties, String attributeKey) {
        if (ldapProperties.containsKey(attributeKey)) {
            return removeLdapAttributePrefix(ldapProperties.getProperty(attributeKey), attributeKey);
        }
        return null;
    }

    public String getGivenName() {
        return givenName;
    }


    public String getSurname() {
        return surname;
    }


    public String getUid() {
        return uid;
    }


    public Set<String> getRoles() {
        return roles;
    }


    public String getCommonName() {
        return commonName;
    }

    public String getEmail() {
        return email;
    }

    public String getCorporateAffiliation() {
        return corporateAffiliation;
    }

    public String getCostUnit() {
        return costUnit;
    }

    private String removeLdapAttributePrefix(String attValue, String attributeName) {
        return StrUtils.removeFirstCharacterIfCharacterIs(attValue, attributeName + ": ");
    }

    public String buildNameFromAttributes() {
        String result = "";
        if (StrUtils.isValid(getGivenName())) {
            result = getGivenName();
        }
        if (StrUtils.isValid(getSurname(), getGivenName())) {
            result += " ";
        }
        if (StrUtils.isValid(getSurname())) {
            result += getSurname();
        }
        return result;
    }

    public String getStatusText() {
        String[] translatedStatusTextPlaceHolders = null;
        if (statusTextPlaceHolders != null) {
            translatedStatusTextPlaceHolders = new String[statusTextPlaceHolders.length];
            for (int i = 0; i < statusTextPlaceHolders.length; i++) {
                translatedStatusTextPlaceHolders[i] = TranslationHandler.translate(statusTextPlaceHolders[i]);
            }
        }
        return StrUtils.isValid(statusText) ? TranslationHandler.translate(statusText, translatedStatusTextPlaceHolders) : "";
    }

    public void setStatusText(String statusText, String... statusTextPlaceHolders) {
        this.statusText = statusText;
        this.statusTextPlaceHolders = statusTextPlaceHolders;
    }

    public Set<String> getIPartsRoles() {
        Set<String> iPartsRoles = new HashSet<>();
        if (hasRoles()) {
            String searchValue = LDAPHelper.getLdapSearchValueForUsers();
            for (String role : getRoles()) {
                if (StrUtils.matchesSqlLike(searchValue, role)) {
                    iPartsRoles.add(role);
                }
            }
        }
        return iPartsRoles;
    }

    public boolean hasRoles() {
        return (getRoles() != null) && !getRoles().isEmpty();
    }

    public boolean hasIPartsRoles() {
        return !getIPartsRoles().isEmpty();
    }

    public boolean hasRole(String rolename) {
        if (hasRoles()) {
            return roles.contains(rolename);
        } else {
            return false;
        }
    }
}
