/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getmedia;

import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObject;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;

/**
 * Request Data Transfer Object für den GetMedia-Webservice
 *
 * Beispiele (GET):
 * - /parts/media/graphics_C204_8020 -> Zusatzgrafik C204_8020 als Vollbild
 * - /parts/media/previews/graphics_C204_8020 -> Zusatzgrafik C204_8020 als Thumbnail
 * - /parts/media/drawing_B26035000034.tif -> Zeichnung B26035000034.tif als Vollbild
 * - /parts/media/previews/drawing_B26035000034.tif -> Zeichnung B26035000034.tif als Thumbnail
 * - /parts/media/drawing_PV000.010.000.284_version_001 -> Zeichnung PV000.010.000.284 mit Version 001 als Vollbild
 * - /parts/media/previews/drawing_PV000.010.000.284_version_001 -> Zeichnung PV000.010.000.284 mit Version 001 als Thumbnail
 */
public class iPartsWSGetMediaRequest extends WSRequestTransferObject {

    private String imageId;
    private boolean thumbnail;

    public iPartsWSGetMediaRequest() {
    }

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        checkAttribValid(path, "imageId", imageId);
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ imageId, thumbnail };
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public boolean isThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(boolean thumbnail) {
        this.thumbnail = thumbnail;
    }
}