/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.response;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractXMLObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response.iPartsXMLErrorText;

/**
 * Abstrakte Klasse für Textelemente in MQ XML Dateien 8z.B. {@link iPartsXMLErrorText}
 */
public abstract class AbstractXMLText extends AbstractXMLObject {

    protected static final String NO_TEXT = "Unknown text";
    protected static final String NO_TEXT_ID = "";
    protected static final String NO_LANGUAGE_PROVIDED = "";

    private String language;
    private String textID;
    private String text;

    public String getLanguage() {
        if (language != null) {
            return language;
        }
        return NO_LANGUAGE_PROVIDED;
    }

    public String getTextID() {
        if (textID != null) {
            return textID;
        }
        return NO_TEXT_ID;
    }

    public String getText() {
        if (text != null) {
            return text;
        }
        return NO_TEXT;
    }

    protected void setText(String text) {
        this.text = text;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setTextID(String textID) {
        this.textID = textID;
    }

}
