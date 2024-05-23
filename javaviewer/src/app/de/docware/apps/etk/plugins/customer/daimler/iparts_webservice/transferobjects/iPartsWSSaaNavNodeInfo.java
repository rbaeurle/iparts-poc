/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import java.util.List;

/**
 * SaaNavNodeInfo Data Transfer Object für die iParts Webservices
 */
public class iPartsWSSaaNavNodeInfo extends iPartsWSSaaCode {

    private List<iPartsWSFootNote> footnotes;
    private List<iPartsWSSaCode> referencedSAs;

    /**
     * Leerer Konstruktur (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSSaaNavNodeInfo() {
    }

    public iPartsWSSaaNavNodeInfo(String code, String description) {
        super(code, description);
    }

    public List<iPartsWSFootNote> getFootnotes() {
        return footnotes;
    }

    public void setFootnotes(List<iPartsWSFootNote> footnotes) {
        this.footnotes = footnotes;
    }

    public List<iPartsWSSaCode> getReferencedSAs() {
        return referencedSAs;
    }

    public void setReferencedSAs(List<iPartsWSSaCode> referencedSAs) {
        this.referencedSAs = referencedSAs;
    }
}