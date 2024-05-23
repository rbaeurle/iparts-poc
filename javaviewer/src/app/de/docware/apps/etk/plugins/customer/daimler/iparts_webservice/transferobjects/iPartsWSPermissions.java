/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import com.owlike.genson.annotation.JsonIgnore;
import com.owlike.genson.annotation.JsonProperty;
import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.framework.modules.webservice.restful.WSError;
import de.docware.util.StrUtils;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * DTO für die "brand" und "branch" Gültigkeiten im Payload des JWT Token
 */
public class iPartsWSPermissions extends WSRequestTransferObject implements iPartsConst {

    Set<String> branchesMercedesBenz;
    Set<String> branchesSmart;
    Set<String> branchesMaybach;
    Set<String> branchesSpecial;

    @JsonIgnore
    Map<String, Set<String>> brandAndBranchesAsMap;

    /**
     * Leerer Konstruktur (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSPermissions() {
    }

    @JsonProperty(BRAND_MERCEDES_BENZ)
    public Set<String> getBranchesMercedesBenz() {
        return branchesMercedesBenz;
    }

    @JsonProperty(BRAND_MERCEDES_BENZ)
    public void setBranchesMercedesBenz(Set<String> branchesMercedesBenz) {
        this.branchesMercedesBenz = branchesMercedesBenz;
        brandAndBranchesAsMap = null;
    }

    @JsonProperty(BRAND_SMART)
    public Set<String> getBranchesSmart() {
        return branchesSmart;
    }

    @JsonProperty(BRAND_SMART)
    public void setBranchesSmart(Set<String> branchesSmart) {
        this.branchesSmart = branchesSmart;
        brandAndBranchesAsMap = null;
    }

    @JsonProperty(BRAND_MAYBACH)
    public Set<String> getBranchesMaybach() {
        return branchesMaybach;
    }

    @JsonProperty(BRAND_MAYBACH)
    public void setBranchesMaybach(Set<String> branchesMaybach) {
        this.branchesMaybach = branchesMaybach;
        brandAndBranchesAsMap = null;
    }

    @JsonProperty(BRAND_SPECIAL)
    public Set<String> getBranchesSpecial() {
        return branchesSpecial;
    }

    @JsonProperty(BRAND_SPECIAL)
    public void setBranchesSpecials(Set<String> branchesSpecial) {
        this.branchesSpecial = branchesSpecial;
        brandAndBranchesAsMap = null;
    }

    @JsonIgnore
    public Map<String, Set<String>> getAsBrandAndBranchesMap() {
        if (brandAndBranchesAsMap == null) {
            brandAndBranchesAsMap = new LinkedHashMap<>();

            if ((branchesMercedesBenz != null) && !branchesMercedesBenz.isEmpty()) {
                brandAndBranchesAsMap.put(BRAND_MERCEDES_BENZ, new HashSet<>(branchesMercedesBenz));
            }

            if ((branchesSmart != null) && !branchesSmart.isEmpty()) {
                brandAndBranchesAsMap.put(BRAND_SMART, new HashSet<>(branchesSmart));
            }

            if ((branchesMaybach != null) && !branchesMaybach.isEmpty()) {
                brandAndBranchesAsMap.put(BRAND_MAYBACH, new HashSet<>(branchesMaybach));
            }

            if ((branchesSpecial != null) && !branchesSpecial.isEmpty()) {
                brandAndBranchesAsMap.put(BRAND_SPECIAL, new HashSet<>(branchesSpecial));
            }
        }

        return brandAndBranchesAsMap;
    }

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        // Es muss mindestens eine Marken-/Sparten-Berechtigung vorhanden sein
        if ((branchesMercedesBenz == null) && (branchesSmart == null) && (branchesMaybach == null)) {
            String[] names = new String[]{ BRAND_MERCEDES_BENZ, BRAND_SMART, BRAND_MAYBACH, BRAND_SPECIAL };
            String message = "At least one of the attributes [" + StrUtils.stringArrayToString(", ", names) + "] must not be empty";
            throwRequestError(WSError.REQUEST_PARAMETER_MISSING, message, path);
        }
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ branchesMercedesBenz, branchesSmart, branchesMaybach, branchesSpecial };
    }
}
