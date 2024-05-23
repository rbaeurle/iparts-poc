/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import com.owlike.genson.annotation.JsonIgnore;

import java.util.List;

public class iPartsWSValidateResult extends iPartsWSPart {

    private String aggTypeId;  // Type of aggregate this part belongs to
    private String aggProductId; // Code of aggregate product this part belongs to
    private List<iPartsWSNavNode> navContext;

    public String getAggTypeId() {
        return aggTypeId;
    }

    public void setAggTypeId(String aggTypeId) {
        this.aggTypeId = aggTypeId;
    }

    public String getAggProductId() {
        return aggProductId;
    }

    public void setAggProductId(String aggProductId) {
        this.aggProductId = aggProductId;
    }

    public List<iPartsWSNavNode> getNavContext() {
        return navContext;
    }

    public void setNavContext(List<iPartsWSNavNode> navContext) {
        this.navContext = navContext;
    }

    @JsonIgnore
    public void assignValuesFromSearchResponse(iPartsWSPartResult searchResult) {
        setPartNo(searchResult.getPartNo());
        setPartNoFormatted(searchResult.getPartNoFormatted());
        setName(searchResult.getName());
        setDescription(searchResult.getDescription());
        setPictureAvailable(searchResult.isPictureAvailable());
        this.aggTypeId = searchResult.getAggTypeId();
        this.aggProductId = searchResult.getAggProductId();
        this.navContext = searchResult.getNavContext();
    }
}
