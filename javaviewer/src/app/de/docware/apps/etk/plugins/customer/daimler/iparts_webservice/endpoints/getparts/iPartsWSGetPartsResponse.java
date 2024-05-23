/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getparts;

import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSImage;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSReleaseInfo;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * Response Data Transfer Object für den GetParts-Webservice
 *
 * Beispiel-Response siehe Confluence: https://confluence.docware.de/confluence/x/6IEFAQ
 */
public class iPartsWSGetPartsResponse implements RESTfulTransferObjectInterface {

    private iPartsWSReleaseInfo releaseInfo;
    private List<iPartsWSPart> parts;
    private List<iPartsWSImage> images;

    public iPartsWSGetPartsResponse() {
    }

    public void assignRMIValues(List<iPartsWSPart> parts) {
        setParts(parts);
    }

    public void assignNonRMIValues(iPartsWSReleaseInfo releaseInfo, List<iPartsWSImage> images) {
        setReleaseInfo(releaseInfo);
        setImages(images);
    }

    public iPartsWSReleaseInfo getReleaseInfo() {
        return releaseInfo;
    }

    public void setReleaseInfo(iPartsWSReleaseInfo releaseInfo) {
        this.releaseInfo = releaseInfo;
    }

    public List<iPartsWSPart> getParts() {
        return parts;
    }

    public void setParts(List<iPartsWSPart> parts) {
        this.parts = parts;
    }

    public List<iPartsWSImage> getImages() {
        return images;
    }

    public void setImages(List<iPartsWSImage> images) {
        this.images = images;
    }
}