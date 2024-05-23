/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * PartResult Data Transfer Object für den iParts SearchParts Webservice
 */
public class iPartsWSPartResult implements RESTfulTransferObjectInterface {

    private String partNo; // unformatiert = Speicherformat
    private String partNoFormatted;
    private String name; // Benennung
    private String description; // kombinierter Text
    private String aggTypeId;  // Type of aggregate this part belongs to
    private String aggProductId; // Code of aggregate product this part belongs to
    private List<iPartsWSNavNode> navContext;
    private List<String> assortmentClassIds; // optional (nur relevant für SearchMaterialParts)
    private boolean pictureAvailable; // Flag für Einzelzeilbilder
    private List<String> es2Keys;


    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }

    public List<iPartsWSNavNode> getNavContext() {
        return navContext;
    }

    public void setNavContext(List<iPartsWSNavNode> navContext) {
        this.navContext = navContext;
    }

    public String getAggProductId() {
        return aggProductId;
    }

    public void setAggProductId(String aggProductId) {
        this.aggProductId = aggProductId;
    }

    public String getAggTypeId() {
        return aggTypeId;
    }

    public void setAggTypeId(String aggTypeId) {
        this.aggTypeId = aggTypeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPartNoFormatted() {
        return partNoFormatted;
    }

    public void setPartNoFormatted(String partNoFormatted) {
        this.partNoFormatted = partNoFormatted;
    }

    public List<String> getAssortmentClassIds() {
        return assortmentClassIds;
    }

    public void setAssortmentClassIds(List<String> assortmentClassIds) {
        this.assortmentClassIds = assortmentClassIds;
    }

    public boolean isPictureAvailable() {
        return pictureAvailable;
    }

    public void setPictureAvailable(boolean pictureAvailable) {
        this.pictureAvailable = pictureAvailable;
    }

    public List<String> getEs2Keys() {
        return es2Keys;
    }

    public void setEs2Keys(List<String> es2Keys) {
        this.es2Keys = es2Keys;
    }
}
