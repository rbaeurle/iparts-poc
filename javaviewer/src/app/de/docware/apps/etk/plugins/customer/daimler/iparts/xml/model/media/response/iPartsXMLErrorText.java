/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.response.AbstractXMLText;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.util.StrUtils;

/**
 * Repräsentiert das "ErrorText" Element in der Transfer XML
 */
public class iPartsXMLErrorText extends AbstractXMLText {

    public iPartsXMLErrorText(DwXmlNode node) {
        loadFromXML(node);
    }

    public iPartsXMLErrorText(String errorText) {
        setText(errorText);
    }

    public void setErrorText(String errorText) {
        setText(errorText);
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode errorNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.ERRORTEXT.getAlias());
        if (getTextID() != null) {
            errorNode.setAttribute(ATTR_TEXT_ID, getTextID());
        }
        if (getLanguage() != null) {
            errorNode.setAttribute(ATTR_LANG, getLanguage());
        }
        errorNode.setTextContent(getText());
        return errorNode;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.ERRORTEXT)) {
                setLanguage(node.getAttribute(ATTR_LANG));
                setTextID(node.getAttribute(ATTR_TEXT_ID));
                String errorText = node.getTextContent();
                if (StrUtils.isEmpty(errorText)) {
                    errorText = "Received error contains no errortext!";
                }
                setErrorText(errorText);
            }
        }
    }
}