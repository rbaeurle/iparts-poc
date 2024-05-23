/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractXMLObject;

/**
 * Repräsentiert das "AbstractOperation" Element in der Transfer XML
 */
public abstract class AbstractXMLRequestOperation extends AbstractXMLObject {

    private iPartsTransferNodeTypes operationType;

    public iPartsTransferNodeTypes getOperationType() {
        return operationType;
    }

    public void setOperationType(iPartsTransferNodeTypes operationType) {
        this.operationType = operationType;
    }

}
