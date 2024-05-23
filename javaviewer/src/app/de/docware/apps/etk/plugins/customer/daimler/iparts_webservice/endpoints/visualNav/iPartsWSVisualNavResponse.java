/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.visualNav;

import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSCgSubgroup;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSImage;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * Response Data Transfer Object für den visualNav-Webservice
 *
 * Beispiel-Response siehe Confluence: https://confluence.docware.de/x/fZVHCw
 */
public class iPartsWSVisualNavResponse implements RESTfulTransferObjectInterface {

    private List<iPartsWSImage> navImages;
    private List<iPartsWSCgSubgroup> cgSubgroups;

    public iPartsWSVisualNavResponse() {
    }

    public List<iPartsWSImage> getNavImages() {
        return navImages;
    }

    public void setNavImages(List<iPartsWSImage> navImages) {
        this.navImages = navImages;
    }

    public List<iPartsWSCgSubgroup> getCgSubgroups() {
        return cgSubgroups;
    }

    public void setCgSubgroups(List<iPartsWSCgSubgroup> cgSubgroups) {
        this.cgSubgroups = cgSubgroups;
    }
}
