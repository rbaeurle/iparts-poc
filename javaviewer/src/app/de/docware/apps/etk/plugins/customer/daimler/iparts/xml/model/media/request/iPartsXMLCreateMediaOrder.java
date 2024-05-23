/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request.AbstractMediaOrderRequest;
import de.docware.framework.modules.xml.DwXmlNode;

import java.util.Date;

/**
 * Repräsentiert das "CreateMediaOrder" Element in der Transfer XML
 */
public class iPartsXMLCreateMediaOrder extends AbstractMediaOrderRequest {

    public iPartsXMLCreateMediaOrder(String realization, Date dateDue) {
        // Description ist ein Muss Feld wird aber von uns nicht gesetzt
        setDescription("");
        setRealization(realization);
        setDateDue(dateDue);
    }

    public iPartsXMLCreateMediaOrder(DwXmlNode node) {
        super(node);
    }

    @Override
    protected void initMediaOrderRequest() {
        setOperationType(iPartsTransferNodeTypes.CREATE_MEDIA_ORDER);
    }

}

