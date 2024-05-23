/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config;

import de.docware.framework.modules.gui.misc.resources.FrameworkImage;

import java.util.Map;

/**
 * Sammelt die Daten aus MediaSearch und das zugehörige Bild aus MediaPreview
 */
public class iPartsMQ_MediaSearchPreview {

    public static class iPartsMQ_MediaPreview {

        // Elemente aus MediaPreview
        public FrameworkImage image;
        public String fileType;

    }

    private iPartsASPLMItemId mqItemId;
    private Map<String, String> mediaSearchElements;  // Elemente aus MediaSearch
    // Elemente aus MediaPreview
    iPartsMQ_MediaPreview mediaPreviewElements;

    public iPartsMQ_MediaSearchPreview(iPartsASPLMItemId mqItemId, Map<String, String> mediaSearchElements) {
        this.mqItemId = mqItemId;
        this.mediaSearchElements = mediaSearchElements;
        this.mediaPreviewElements = null;
    }

    public iPartsMQ_MediaSearchPreview(String mcItemId, String mcItemRevId, Map<String, String> mediaSearchElements) {
        this(new iPartsASPLMItemId(mcItemId, mcItemRevId), mediaSearchElements);
    }

    public iPartsASPLMItemId getMqItemId() {
        return mqItemId;
    }

    public Map<String, String> getMediaSearchElements() {
        return mediaSearchElements;
    }

    public FrameworkImage getImage() {
        if (mediaPreviewElements != null) {
            return mediaPreviewElements.image;
        }
        return null;
    }

    public String getFileType() {
        if (mediaPreviewElements != null) {
            return mediaPreviewElements.fileType;
        }
        return null;
    }

    public void setImage(FrameworkImage image) {
        if (mediaPreviewElements == null) {
            mediaPreviewElements = new iPartsMQ_MediaPreview();
        }
        mediaPreviewElements.image = image;
    }

    public void setFileType(String fileType) {
        if (mediaPreviewElements == null) {
            mediaPreviewElements = new iPartsMQ_MediaPreview();
        }
        mediaPreviewElements.fileType = fileType;
    }

    public boolean isValid() {
        if (mqItemId != null) {
            return mqItemId.isValidId();
        }
        return false;
    }

    public boolean isPreviewImageValid() {
        if (mediaPreviewElements != null) {
            return mediaPreviewElements.image != null;
        }
        return false;
    }
}
