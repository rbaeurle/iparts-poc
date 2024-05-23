/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.event;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractXMLObject;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.util.StrUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Repräsentiert das "TcObject" Element in der Transfer XML
 */
public class iPartsXMLTcObject extends AbstractXMLObject {

    private String itemId;
    private String itemRevId;
    private List<iPartsXMLAttribute> attributes;

    public iPartsXMLTcObject(DwXmlNode node) {
        loadFromXML(node);
    }

    public iPartsXMLTcObject(String itemId, String itemRevId) {
        this.itemId = itemId;
        this.itemRevId = itemRevId;
    }

    public String getMcItemId() {
        return itemId;
    }

    public String getMcItemRevId() {
        return itemRevId;
    }

    public List<iPartsXMLAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<iPartsXMLAttribute> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(iPartsXMLAttribute attribute) {
        if (attributes == null) {
            attributes = new LinkedList<iPartsXMLAttribute>();
        }
        if (attribute != null) {
            attributes.add(attribute);
        }
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode tcObjectNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.TC_OBJECT.getAlias());
        if (itemId != null) {
            tcObjectNode.setAttribute(ATTR_ITEM_ID, itemId);
        }
        if (itemRevId != null) {
            tcObjectNode.setAttribute(ATTR_ITEM_REV_ID, itemRevId);
        }
        if (attributes != null) {
            for (iPartsXMLAttribute attribute : attributes) {
                tcObjectNode.appendChild(attribute.getAsDwXMLNode(namespacePrefix));
            }
        }
        return tcObjectNode;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.TC_OBJECT)) {
                itemId = node.getAttribute(ATTR_ITEM_ID);
                itemRevId = node.getAttribute(ATTR_ITEM_REV_ID);
                List<DwXmlNode> childNodes = node.getChildNodes();
                for (DwXmlNode childNode : childNodes) {
                    iPartsTransferNodeTypes nodeType = XMLImportExportHelper.getIPartsNodeType(childNode);
                    if (nodeType == null) {
                        continue;
                    }
                    if (nodeType == iPartsTransferNodeTypes.ATTRIBUTE) {
                        addAttribute(new iPartsXMLAttribute(childNode));
                    } else {
                        Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Wrong element for creating a TcObject object! Nodetype: " + nodeType);
                    }
                }
            }
        }
    }

    /**
     * Gibt zurück, ob das TcObjekt mind. einen leeren Wert enthält (Item ID und Item Rev ID sollten eigentlich nie leer sein)
     *
     * @return
     */
    public boolean hasAtLeastOneEmptyValue() {
        return !StrUtils.isValid(itemId, itemRevId);
    }
}
