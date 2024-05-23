/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImage;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.project.mechanic.ids.PoolId;
import de.docware.apps.etk.base.webservice.endpoints.helper.WSHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.iPartsNote;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getmedia.iPartsWSGetMediaEndpoint;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.util.StrUtils;

import java.util.List;

/**
 * Image Data Transfer Object für die iParts Webservices
 */
public class iPartsWSImage implements RESTfulTransferObjectInterface {

    private String id;
    private String href;
    private String previewHref;
    private String type;
    private List<iPartsWSNote> notes;
    private List<iPartsWSCallout> callouts;

    /**
     * Leerer Konstruktur (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSImage() {
    }

    /**
     * Erzeugt ein Image DTO und initialisiert {@link #id}, {@link #href} sowie {@link #previewHref} für die Zeichnung mit
     * der angegebenen {@link PoolId}.
     *
     * @param imagePoolId
     * @return
     */
    public iPartsWSImage(PoolId imagePoolId, EtkDataImage dataImage, String language, EtkProject project) {
        String imageId = iPartsWSGetMediaEndpoint.createDrawingId(imagePoolId);
        setId(imageId);

        // URLs über GetMediaEndpoint erzeugen
        setHref(iPartsWSGetMediaEndpoint.createGetMediaCallURL(imageId, false));
        setPreviewHref(iPartsWSGetMediaEndpoint.createGetMediaCallURL(imageId, true));

        // Den Typ (=Navigationsperspektive) setzen
        String naviPerspective = dataImage.getFieldValue(iPartsConst.FIELD_I_NAVIGATION_PERSPECTIVE);
        setType(WSHelper.getNullForEmptyString(naviPerspective));

        // Notizen zum Bild
        List<iPartsNote> notes = iPartsNote.getNotes(new PartId(imagePoolId.getPImages(), imagePoolId.getPVer()), project);
        setNotes(iPartsWSNote.convertToWSNotes(notes, language, project.getDataBaseFallbackLanguages()));
    }

    /**
     * Erzeugt ein Image DTO und initialisiert {@link #id}, {@link #href} sowie {@link #previewHref} für die Zusatzgrafik
     * mit dem angegebenen Namen.
     *
     * @param pictureName
     */
    public iPartsWSImage(String pictureName) {
        String imageId = iPartsWSGetMediaEndpoint.createAdditionalGraphicsId(pictureName);
        setId(imageId);

        // URLs über GetMediaEndpoint erzeugen
        setHref(iPartsWSGetMediaEndpoint.createGetMediaCallURL(imageId, false));
        setPreviewHref(iPartsWSGetMediaEndpoint.createGetMediaCallURL(imageId, true));
    }

    // Getter und Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getPreviewHref() {
        return previewHref;
    }

    public void setPreviewHref(String previewHref) {
        this.previewHref = previewHref;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        // Leerstring gezielt auf NULL setzen, dann wird im DTO die Ausgabe des Typs automatisch unterdrückt.
        if (!StrUtils.isValid(type)) {
            this.type = null;
        } else {
            this.type = type;
        }
    }

    public List<iPartsWSNote> getNotes() {
        return notes;
    }

    public void setNotes(List<iPartsWSNote> notes) {
        this.notes = notes;
    }

    public List<iPartsWSCallout> getCallouts() {
        return callouts;
    }

    public void setCallouts(List<iPartsWSCallout> callouts) {
        this.callouts = callouts;
    }
}