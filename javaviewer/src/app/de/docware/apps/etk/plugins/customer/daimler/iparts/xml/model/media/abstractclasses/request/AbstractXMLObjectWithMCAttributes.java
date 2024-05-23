/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request;

/**
 * Repräsentiert das "AbstractOperation" Element mit MediaContainer Attributen in der Transfer XML
 */
public abstract class AbstractXMLObjectWithMCAttributes extends AbstractXMLRequestOperation {

    protected String mcItemId;
    protected String mcItemRevId;

    public String getMcItemId() {
        return mcItemId;
    }

    public String getMcItemRevId() {
        return mcItemRevId;
    }
}
