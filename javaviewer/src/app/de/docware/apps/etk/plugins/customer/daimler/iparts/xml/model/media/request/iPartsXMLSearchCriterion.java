/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferSMCAttributes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractXMLObject;
import de.docware.framework.modules.xml.DwXmlNode;

/**
 * Repräsentiert ein Suchkriterium für {@link iPartsXMLSearchMediaContainers}
 */
public class iPartsXMLSearchCriterion extends AbstractXMLObject {

    private iPartsTransferSMCAttributes attributeName;
    private String attributeValue;

    public iPartsXMLSearchCriterion(iPartsTransferSMCAttributes attributeName, String attributeValue) {
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
    }

    public iPartsXMLSearchCriterion(DwXmlNode node) {
        loadFromXML(node);
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public iPartsTransferSMCAttributes getAttributeName() {
        return attributeName;
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode scNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.SEARCH_CRITERION.getAlias());
        if ((attributeName != null) && (attributeValue != null)) {
            scNode.setAttribute(ATTR_SMC_NAME, attributeName.getAsASPLMValue());
            scNode.setTextContent(attributeValue);
        }
        return scNode;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.SEARCH_CRITERION)) {
                attributeName = iPartsTransferSMCAttributes.getValidatedAttributeByDescription(node.getAttribute(iPartsTransferConst.ATTR_SMC_NAME));
                attributeValue = node.getTextContent();
            }
        }
    }

}