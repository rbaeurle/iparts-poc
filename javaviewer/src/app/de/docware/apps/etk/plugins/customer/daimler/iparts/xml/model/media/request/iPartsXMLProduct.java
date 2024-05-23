/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsProductTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractXMLObject;
import de.docware.framework.modules.xml.DwXmlNode;

/**
 * Repräsentiert das "Product" Element in der Transfer XML
 */
public class iPartsXMLProduct extends AbstractXMLObject {

    private String name;
    private iPartsProductTypes type;

    public iPartsXMLProduct(String name, iPartsProductTypes productType) {
        this.name = name;
        this.type = productType;
    }

    public iPartsXMLProduct(DwXmlNode node) {
        loadFromXML(node);
    }

    public String getName() {
        return name;
    }

    public iPartsProductTypes getType() {
        return type;
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode productNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.PRODUCT.getAlias());
        productNode.setAttribute(ATTR_NAME, getName());
        productNode.setAttribute(CMO_TYPE, getType().getAsplmValue());
        return productNode;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.PRODUCT)) {
                type = iPartsProductTypes.getFromASPLMValue(node.getAttribute(iPartsTransferConst.CMO_TYPE));
                name = node.getAttribute(iPartsTransferConst.ATTR_NAME);
            }
        }
    }
}
