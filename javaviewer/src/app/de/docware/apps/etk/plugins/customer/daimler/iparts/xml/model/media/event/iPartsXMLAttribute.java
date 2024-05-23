/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.event;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractXMLObject;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;

/**
 * Repräsentiert das "Attribute" Element in der Transfer XML
 */
public class iPartsXMLAttribute extends AbstractXMLObject {

    private String name;
    private String value;

    public iPartsXMLAttribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public iPartsXMLAttribute(DwXmlNode node) {
        loadFromXML(node);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode attributeNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.ATTRIBUTE.getAlias());
        if ((name == null) || name.isEmpty()) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Name must not be null or empty (ASPLM Schema)!");
        }
        attributeNode.setAttribute(ATTR_NAME, name);
        if (value != null) {
            attributeNode.setTextContent(value);
        }
        return attributeNode;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.ATTRIBUTE)) {
                name = node.getAttribute(ATTR_NAME);
                value = node.getTextContent();
            }
        }
    }
}
