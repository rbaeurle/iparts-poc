/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getmedia;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataPool;
import de.docware.apps.etk.base.project.mechanic.drawing.ImageVariant;
import de.docware.apps.etk.base.project.mechanic.ids.PoolId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsDataPool;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.iPartsWSAbstractEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.http.HttpConstants;
import de.docware.framework.modules.gui.misc.http.server.HttpServerRequest;
import de.docware.framework.modules.gui.misc.http.server.HttpServerResponse;
import de.docware.framework.modules.gui.misc.resources.FrameworkImage;
import de.docware.framework.modules.gui.misc.resources.FrameworkImageUtils;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.framework.modules.webservice.restful.WSError;
import de.docware.framework.modules.webservice.restful.annotations.Path;
import de.docware.framework.modules.webservice.restful.annotations.PathParam;
import de.docware.framework.modules.webservice.restful.annotations.Produces;
import de.docware.framework.modules.webservice.restful.annotations.methods.GET;
import de.docware.util.StrUtils;
import de.docware.util.imageconverter.ImageInformation;
import de.docware.util.j2ee.EC;

/**
 * Endpoint für den GetMedia-Webservice
 */
public class iPartsWSGetMediaEndpoint extends iPartsWSAbstractEndpoint<iPartsWSGetMediaRequest> {

    public static final String DEFAULT_ENDPOINT_URI = "/parts/media";
    public static final String DEFAULT_BASE_URL = "";
    public static final int DEFAULT_CACHE_LIFETIME = 30 * 60; // 30 Minuten
    public static final int DEFAULT_GRAPHICS_MAX_THUMBNAIL_HEIGHT = 30;

    public static final String MEDIA_PREFIX_ADDITIONAL_GRAPHICS = "graphics_";

    public static final String MEDIA_PREFIX_DRAWING = "drawing_";
    public static final String MEDIA_VERSION_DELIMITER = "_version_";
    public static final String MEDIA_USAGE_DELIMITER = "_usage_";

    /**
     * Erzeugt eine relative oder absolute URL (abhängig von den Einstellungen im Admin-Modus) für den Aufruf vom GetMedia
     * Webservice für das Bild mit der angegebenen <i>imageId</i> abhängig von <i>isThumbnail</i> als Vollbild bzw. Thumbnail.
     *
     * @param imageId
     * @param isThumbnail
     * @return
     */
    public static String createGetMediaCallURL(String imageId, boolean isThumbnail) {
        // URL zusammensetzen
        String url = StrUtils.removeLastCharacterIfCharacterIs(iPartsWebservicePlugin.getPluginConfig().getConfigValueAsString(iPartsWebservicePlugin.CONFIG_BASE_URL_GET_MEDIA), '/');
        url += '/' + StrUtils.removeFirstCharacterIfCharacterIs(iPartsWebservicePlugin.getPluginConfig().getConfigValueAsString(iPartsWebservicePlugin.CONFIG_URI_GET_MEDIA), '/');
        url = StrUtils.addCharacterIfLastCharacterIsNot(url, '/');
        if (isThumbnail) {
            url += "previews/";
        }

        url += EC.encodePath(imageId);
        return url;
    }

    /**
     * Erzeugt eine einfache String-ID für die Zeichnung mit der angegebenen {@link PoolId}.
     *
     * @param imagePoolId
     * @return
     */
    public static String createDrawingId(PoolId imagePoolId) {
        String imageNumber = imagePoolId.getPImages();
        String imageVersion = imagePoolId.getPVer();
        String imageUsage = imagePoolId.getPUsage();

        String imageId = MEDIA_PREFIX_DRAWING + imageNumber;
        if (!imageVersion.isEmpty()) {
            imageId += MEDIA_VERSION_DELIMITER + imageVersion;
        }
        if (!imageUsage.isEmpty()) {
            imageId += MEDIA_USAGE_DELIMITER + imageUsage;
        }
        return imageId;
    }

    /**
     * Erzeugt eine einfache String-ID für die Zusatzgrafik mit dem angegebenen Namen.
     *
     * @param pictureName
     * @return
     */
    public static String createAdditionalGraphicsId(String pictureName) {
        return MEDIA_PREFIX_ADDITIONAL_GRAPHICS + pictureName;
    }


    public iPartsWSGetMediaEndpoint(String endpointUri) {
        super(endpointUri);
        setResponseCacheLifetime(iPartsWebservicePlugin.getPluginConfig().getConfigValueAsInteger(iPartsWebservicePlugin.CONFIG_CACHE_LIFETIME_GET_MEDIA));
    }

    @Override
    protected boolean isHttpMethodPostValid() {
        return false;
    }

    /**
     * GetMedia für Vollbilder verwendet GET mit der ID des Bildes als Pfadparameter in der URL.
     *
     * @return
     */
    @GET
    @Path("{imageId}")
    @Produces({ MimeTypes.MIME_TYPE_PNG, MimeTypes.MIME_TYPE_JPEG, MimeTypes.MIME_TYPE_GIF, MimeTypes.MIME_TYPE_SVG,
                MimeTypes.MIME_TYPE_JT, MimeTypes.MIME_TYPE_XV })
    public RESTfulTransferObjectInterface getMedia(@PathParam("imageId") String imageId) {
        iPartsWSGetMediaRequest request = new iPartsWSGetMediaRequest();
        request.setImageId(imageId);
        request.setThumbnail(false);
        return handleWebserviceRequestIntern(request);
    }

    /**
     * GetMedia für Thumbnails verwendet GET mit der ID des Bildes als Pfadparameter in der URL.
     *
     * @return
     */
    @GET
    @Path("previews/{imageId}")
    @Produces({ MimeTypes.MIME_TYPE_PNG, MimeTypes.MIME_TYPE_JPEG, MimeTypes.MIME_TYPE_GIF, MimeTypes.MIME_TYPE_SVG,
                MimeTypes.MIME_TYPE_JT, MimeTypes.MIME_TYPE_XV })
    public RESTfulTransferObjectInterface getThumbnail(@PathParam("imageId") String imageId) {
        iPartsWSGetMediaRequest request = new iPartsWSGetMediaRequest();
        request.setImageId(imageId);
        request.setThumbnail(true);
        return handleWebserviceRequestIntern(request);
    }

    @Override
    protected void modifyResponseHeaders(HttpServerResponse response) {
        super.modifyResponseHeaders(response);

        long expireHeaderConfigValue = iPartsWebservicePlugin.getPluginConfig().getConfigValueAsInteger(iPartsWebservicePlugin.CONFIG_EXPIRE_HEADER_OVERRIDE_GET_MEDIA);
        if (expireHeaderConfigValue != 0) { // 0 bedeutet kein Caching erlaubt -> Standard-Header-Werte aus HttpServerResponse beibehalten
            if (expireHeaderConfigValue > 0) {
                // Header-Werte für Caching anpassen
                response.setHeader("Pragma", "cache");
                response.setHeader("Cache-Control", "max-age=" + expireHeaderConfigValue + ",private,must-revalidate");

                // expireHeaderConfigValue ist in Sekunden angegeben
                response.setDateHeader("Expires", System.currentTimeMillis() + expireHeaderConfigValue * 1000);
            } else { // < 0 entfernt den Expires-Header
                // Header-Werte für Caching entfernen
                response.setHeader("Pragma", null);
                response.removeHeader("Expires");
                response.removeHeader("Cache-Control");
            }
        }
    }

    @Override
    protected iPartsWSGetMediaResponse executeWebservice(EtkProject project, iPartsWSGetMediaRequest requestObject) {
        requestObject.checkIfValid(""); // bei GET muss das requestObject manuell auf Gültigkeit geprüft werden

        iPartsWSGetMediaResponse response = null;

        String imageId = requestObject.getImageId();
        boolean isThumbnail = requestObject.isThumbnail();
        if (imageId.startsWith(MEDIA_PREFIX_ADDITIONAL_GRAPHICS)) { // Zusatzgrafiken
            response = getAdditionalGraphics(project, imageId.substring(MEDIA_PREFIX_ADDITIONAL_GRAPHICS.length()), isThumbnail);
        } else if (imageId.startsWith(MEDIA_PREFIX_DRAWING)) { // Stücklisten-Zeichnungen
            response = getDrawing(project, imageId.substring(MEDIA_PREFIX_DRAWING.length()), isThumbnail);
        }

        if (response != null) {
            return response;
        } else {
            // throwResourceNotFoundError() würde zur bisherigen iParts-Schnittstelle inkompatiblen Fehlercode 4041 zurückliefern
            throwError(HttpConstants.HTTP_STATUS_NOT_FOUND, WSError.REQUEST_PARAMETER_WRONG, "No media object found for '"
                                                                                             + imageId + "'", null);
            return null;
        }
    }

    @Override
    public void dataChangedByEdit(iPartsDataChangedEventByEdit.DataType dataType) {
        if (dataType == iPartsDataChangedEventByEdit.DataType.DRAWING) {
            clearCaches();
        }
    }

    @Override
    protected SecureResult isValidRequestSignature(HttpServerRequest request) {
        if (iPartsWebservicePlugin.getPluginConfig().getConfigValueAsBoolean(iPartsWebservicePlugin.CONFIG_NO_AUTHENTIFICATION_GET_MEDIA)) {
            return new SecureResult(SecureReturnCode.SUCCESS); // Immer OK, da keine Authentifizierung notwendig
        } else {
            return super.isValidRequestSignature(request);
        }
    }

    /**
     * Liefert die Zusatzgrafik für die übergebene <i>imageId</i> abhängig von <i>isThumbnail</i> als Vollbild bzw.
     * Thumbnail zurück.
     *
     * @param project
     * @param imageId
     * @param isThumbnail
     * @return
     */
    private iPartsWSGetMediaResponse getAdditionalGraphics(EtkProject project, String imageId, boolean isThumbnail) {
        // Maximale Höhe der Zusatzgrafik bestimmen
        int maxHeight;
        if (isThumbnail) {
            maxHeight = iPartsWebservicePlugin.getPluginConfig().getConfigValueAsInteger(iPartsWebservicePlugin.CONFIG_GRAPHICS_MAX_THUMBNAIL_HEIGHT_GET_MEDIA);
        } else {
            maxHeight = -1;
        }

        // Zusatzgrafik aus DB laden
        FrameworkImage image = project.getVisObject().asPicture(project, imageId, maxHeight);
        if (image == null) {
            return null;
        }

        // Response erzeugen
        iPartsWSGetMediaResponse response = new iPartsWSGetMediaResponse();
        response.setImage(image);

        return response;
    }

    /**
     * Liefert die Stücklistenzeichnung für die übergebene <i>imageId</i> abhängig von <i>isThumbnail</i> als Vollbild bzw.
     * Thumbnail zurück. <i>imageId</i> ist dabei wie folgt aufgebaut: {imageNumber}_version_{imageVersion}_usage_{imageUsage},
     * wobei der Versions- und Verwendungsteil jeweils optional sind
     *
     * @param project
     * @param imageId
     * @param isThumbnail
     * @return
     */
    private iPartsWSGetMediaResponse getDrawing(EtkProject project, String imageId, boolean isThumbnail) {
        // imageId aufsplitten in imageNumber, imageVersion und imageUsage
        String imageNumber;
        String imageVersion;
        String imageUsage;

        // imageUsage bestimmen (letzter Parameter)
        int usageIndex = imageId.lastIndexOf(MEDIA_USAGE_DELIMITER);
        if (usageIndex >= 0) {
            imageUsage = imageId.substring(usageIndex + MEDIA_USAGE_DELIMITER.length());
            imageId = imageId.substring(0, usageIndex);
        } else {
            imageUsage = ImageVariant.iv2D.getUsage();
        }

        // imageNumber und imageVersion bestimmen
        int versionIndex = imageId.lastIndexOf(MEDIA_VERSION_DELIMITER);
        if (versionIndex >= 0) {
            imageNumber = imageId.substring(0, versionIndex);
            imageVersion = imageId.substring(versionIndex + MEDIA_VERSION_DELIMITER.length());
        } else {
            imageNumber = imageId;
            imageVersion = "";
        }

        if (imageNumber.isEmpty()) {
            return null;
        }

        // Zeichnung aus DB laden
        EtkDataPool dataImage = EtkDataObjectFactory.createDataPool();
        dataImage.init(project);
        dataImage.setPKValues(imageNumber, imageVersion, "", imageUsage, DBActionOrigin.FROM_DB);
        if (dataImage.loadFromDB(dataImage.getAsId())) {
            byte[] imageContent = dataImage.getImgBytes();
            if ((imageContent == null) || (imageContent.length == 0)) {
                return null;
            }

            // MimeType bestimmen
            boolean isFramework2DImage;
            String mimeType = dataImage.getFieldValue(EtkDbConst.FIELD_P_IMGTYPE);
            if (!mimeType.isEmpty()) { // Extension -> MimeType
                isFramework2DImage = MimeTypes.isFramework2dImageFile(mimeType);
                mimeType = MimeTypes.getMimeType(mimeType);
            } else { // MimeType aus imageContent auslesen
                isFramework2DImage = true;
                ImageInformation imageInformation = FrameworkImageUtils.getImageInformation(imageContent);
                mimeType = imageInformation.getMimeType();
            }

            // Response erzeugen
            iPartsWSGetMediaResponse response = new iPartsWSGetMediaResponse();
            if (isThumbnail) {
                // Falls ein AS-PLM Vorschaubild vorhanden ist, liefere das zurück
                byte[] previewContent = null;
                if (dataImage instanceof iPartsDataPool) {
                    iPartsDataPool dataPool = (iPartsDataPool)dataImage;
                    previewContent = dataPool.getPreviewImageContent();
                    if ((previewContent != null) && (previewContent.length > 0)) {
                        // MimeType bestimmen
                        String previewMimeType = dataPool.getPreviewImageType();
                        if (!previewMimeType.isEmpty()) { // Extension -> MimeType
                            previewMimeType = MimeTypes.getMimeType(previewMimeType);
                        } else { // MimeType aus previewContent auslesen
                            ImageInformation imageInformation = FrameworkImageUtils.getImageInformation(previewContent);
                            previewMimeType = imageInformation.getMimeType();
                        }

                        response.setImage(previewContent, previewMimeType);
                    } else {
                        previewContent = null;
                    }
                }

                // Fallback falls kein AS-PLM Vorschaubild vorhanden ist
                if (previewContent == null) {
                    // Aktuell für Thumbnails von Framework 2D-Bildern das Vorschaubild vom ImageViewer verwenden (132 x 120)
                    if (isFramework2DImage) {
                        FrameworkImage image = FrameworkImage.getFromByteArray(imageContent);
                        response.setImage(image.getVersion(ImageInformation.MIME_TYPE_PNG, ImageInformation.SUBTYPE_UNKNOWN, 132, 120).getContent(),
                                          MimeTypes.MIME_TYPE_PNG);
                    } else { // für 3D, SVG und andere Bilder, die keine Framework 2D-Bilder sind, können/wollen wir kein Thumbnail berechnen -> Zeichnung direkt ausliefern
                        response.setImage(imageContent, mimeType);
                    }
                }
            } else { // Vollbild
                // PNG, JPEG und GIF können direkt ausgeliefert werden
                if (mimeType.equals(MimeTypes.MIME_TYPE_PNG) || mimeType.equals(MimeTypes.MIME_TYPE_JPEG) || mimeType.equals(MimeTypes.MIME_TYPE_GIF)) {
                    response.setImage(imageContent, mimeType);
                } else if (isFramework2DImage) { // Zeichnung nach PNG konvertieren bei Framework 2D-Bildern
                    FrameworkImage image = FrameworkImage.getFromByteArray(imageContent);
                    response.setImage(image.getVersion(ImageInformation.MIME_TYPE_PNG, ImageInformation.SUBTYPE_UNKNOWN, 1.0d).getContent(),
                                      MimeTypes.MIME_TYPE_PNG);
                } else { // Zeichnung (z.B. 3D, SVG) direkt ausliefern
                    response.setImage(imageContent, mimeType);
                }
            }
            return response;
        } else {
            return null;
        }
    }
}