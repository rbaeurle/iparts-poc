/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import java.util.List;

/**
 * Response Data Transfer Object für ein Modul vom partsList-Webservice (Stücklisten-Aufruf für FIN/VIN oder BM6)
 */
public class iPartsWSPartsListModule extends iPartsWSModelInfo {

    private String moduleId;
    private List<iPartsWSPart> parts;
    private List<iPartsWSImage> images;

    /**
     * Leerer Konstruktor (notwendig für die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSPartsListModule() {
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
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