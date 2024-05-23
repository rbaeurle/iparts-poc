package de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements;

import com.owlike.genson.annotation.JsonProperty;

/**
 * Cache-Objekt für ein Mitlieferteil von einem {@link iPartsPRIMUSReplacementCacheObject}.
 */
public class iPartsPRIMUSIncludePartCacheObject {

    @JsonProperty
    private String includePartNo;
    @JsonProperty
    private String quantity;

    public iPartsPRIMUSIncludePartCacheObject() {
    }

    protected iPartsPRIMUSIncludePartCacheObject(String includePartNo, String quantity) {
        this.includePartNo = includePartNo;
        this.quantity = quantity;
    }

    public String getIncludePartNo() {
        return includePartNo;
    }

    public String getQuantity() {
        return quantity;
    }
}
