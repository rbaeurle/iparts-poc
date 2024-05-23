/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.webservice.transferobjects.JWTDefaultClaims;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin;
import de.docware.apps.etk.viewer.JavaViewerApplication;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.util.StrUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * UserInfo Data Transfer Object für die iParts Webservices (entweder im Webservice-Token vom Request-Header oder als Payload
 * im POST-Request).
 * Praktisch wird z.B. "exp" (Token Expiry date) kaum jemals über das UserInfo DTO kommen. Dazu müsste man ja dann auch mehr Validierung
 * als nur "mandatory" machen. Aber das Ziel ist hier Token Payload und UserInfo DTO gleich zu halten.
 */
public class iPartsWSUserInfo extends JWTDefaultClaims {

    private String userId;
    private String country;
    private String language;
    private String lang1;
    private String lang2;
    private String lang3;
    private iPartsWSPermissions permissions; // branch und brand Gültigkeiten

    private boolean isFromToken;

    /**
     * Leerer Konstruktur (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSUserInfo() {
    }

    /**
     * Liefert die gewünschte Benutzer-ID zurück. ({@link #userId} hat Vorrang vor {@link #sub falls vorhanden).
     *
     * @return
     */
    public String getUserId() {
        if (StrUtils.isValid(userId)) { // userId hat Vorrang vor sub
            return userId;
        } else {
            return getSub();
        }
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCountry() {
        // Früher war country im JSON POST-Payload eine Zahl wie 200, was aber kein gültiges ISO-Land ist und deswegen
        // ignoriert werden muss -> ansonsten müssten alle Webservice Unittests usw. angepasst werden
        if (!isFromToken && ((country == null) || StrUtils.isDigit(country))) {
            return null;
        }

        return country;

    }

    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Liefert die gewünschte Hauptsprache zurück ({@link #lang1} hat Vorrang vor {@link #language} falls vorhanden) mit
     * Fallback auf die erste Rückfallsprache bzw. Englisch als allerletzten Fallback.
     *
     * @return
     */
    @JsonIgnore
    public String getLanguage() {
        if (StrUtils.isValid(lang1)) { // lang1 hat Vorrang vor language
            return getLang1();
        } else if (StrUtils.isValid(language)) {
            return language.toLowerCase();
        } else { // erste Rückfallsprache der aktuellen Session zurückliefern
            EtkProject project = JavaViewerApplication.getInstance().getProject();
            if (project != null) {
                List<String> dataBaseFallbackLanguages = project.getDataBaseFallbackLanguages();
                if (!dataBaseFallbackLanguages.isEmpty()) {
                    return dataBaseFallbackLanguages.get(0).toLowerCase();
                }
            }

            // Englisch als allerletzten Fallback zurückliefern
            return Language.EN.getCode().toLowerCase();
        }
    }

    public void setLanguage(String language) {
        this.language = language.toLowerCase();
    }

    public String getLang1() {
        if (lang1 != null) {
            return lang1.toLowerCase();
        } else {
            return null;
        }
    }

    public void setLang1(String lang1) {
        if (StrUtils.isValid(lang1)) { // lang1 hat Vorrang vor language -> Defaultwert für language entfernen
            language = null;
        }
        this.lang1 = lang1;
    }

    public String getLang2() {
        if (lang2 != null) {
            return lang2.toLowerCase();
        } else {
            return null;
        }
    }

    public void setLang2(String lang2) {
        this.lang2 = lang2;
    }

    public String getLang3() {
        if (lang3 != null) {
            return lang3.toLowerCase();
        } else {
            return null;
        }
    }

    public void setLang3(String lang3) {
        this.lang3 = lang3;
    }

    public iPartsWSPermissions getPermissions() {
        return permissions;
    }

    public void setPermissions(iPartsWSPermissions permissions) {
        this.permissions = permissions;
    }

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        // DAIMLER-4495 TODO Pflichtattribute definieren
//        checkAttribValid(path, "country", country);
//        checkAttribValid(path, "permission", permissions);

        if (isFromToken()) {
//            checkAtLeastOneAttribValid(path, new String[]{ "userId", "sub" }, new String[]{ userId, sub });
//            checkAttribValid(path, "iss", iss);
            checkAttribValid(path, "exp", getExp());
        } else {
            checkAttribValid(path, "userId", userId);
        }
    }

    /**
     * Flag, ob diese {@link iPartsWSUserInfo} im Webservice-Token enthalten war.
     *
     * @return
     */
    @JsonIgnore
    public boolean isFromToken() {
        return isFromToken;
    }

    /**
     * Flag, ob diese {@link iPartsWSUserInfo} im Webservice-Token enthalten war.
     *
     * @param isFromToken
     */
    @JsonIgnore
    public void setIsFromToken(boolean isFromToken) {
        this.isFromToken = isFromToken;
    }

    @JsonIgnore
    public Map<String, Set<String>> getPermissionsAsMapForValidation() {
        iPartsWSPermissions permissionsForValidation = getPermissionsForValidation();
        if (permissionsForValidation != null) {
            return permissionsForValidation.getAsBrandAndBranchesMap();
        }
        return null;
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        // getPermissionsForValidation() ist hier OK, weil ohne diese Admin-Option die Ergebnisse identisch sind und beim
        // Ändern der Admin-Optionen die Caches sowieso gelöscht werden
        return new Object[]{ getCountry(), getLanguage(), getLang2(), getLang3(), getPermissionsForValidation() };
    }

    @JsonIgnore
    public iPartsWSPermissions getPermissionsForValidation() {
        if (!iPartsWebservicePlugin.isCheckTokenPermissions()) {
            return null;
        }
        return getPermissions();
    }

    @JsonIgnore
    public String getCountryForValidation() {
        if (!iPartsWebservicePlugin.isCheckTokenCountryValidity()) {
            return null;
        }
        return getCountry();
    }

    @JsonIgnore
    public Set<String> getSpecialPermissions() {
        iPartsWSPermissions permissions = getPermissions();
        if (permissions != null) {
            Map<String, Set<String>> brandAndBranches = permissions.getAsBrandAndBranchesMap();
            if ((brandAndBranches != null) && !brandAndBranches.isEmpty()) {
                Set<String> branchesForSpecialBrand = brandAndBranches.get(iPartsConst.BRAND_SPECIAL);
                if ((branchesForSpecialBrand != null) && !branchesForSpecialBrand.isEmpty()) {
                    return branchesForSpecialBrand;
                }
            }
        }
        return null;
    }
}
