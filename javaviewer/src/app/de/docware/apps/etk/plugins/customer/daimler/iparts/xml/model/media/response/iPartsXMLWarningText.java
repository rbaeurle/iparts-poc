/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.response.AbstractXMLText;
import de.docware.framework.modules.xml.DwXmlNode;

/**
 * Repräsentiert das "WarningText" Element in der Transfer XML
 */
public class iPartsXMLWarningText extends AbstractXMLText {

    public iPartsXMLWarningText(String warningText) {
        setWarningText(warningText);
    }

    public iPartsXMLWarningText(DwXmlNode node) {
        loadFromXML(node);
    }

    public void setWarningText(String warningText) {
        setText(warningText);
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode warningTextNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.WARNINGTEXT.getAlias());
        if ((getTextID() != null) && !getTextID().isEmpty()) {
            warningTextNode.setAttribute(ATTR_TEXT_ID, getTextID());
        }
        if ((getLanguage() != null) && !getLanguage().isEmpty()) {
            warningTextNode.setAttribute(ATTR_LANG, getLanguage());
        }
        warningTextNode.setTextContent(getText());
        return warningTextNode;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.WARNINGTEXT)) {
                setLanguage(node.getAttribute(ATTR_LANG));
                setTextID(node.getAttribute(ATTR_TEXT_ID));
                setWarningText(node.getTextContent());
            }
        }
    }
}
