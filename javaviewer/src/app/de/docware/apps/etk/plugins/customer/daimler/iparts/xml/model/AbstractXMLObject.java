/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsLanguage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.framework.utils.EtkMultiSprache;

/**
 * Abstrakte Klasse für MQ XML Elemente
 */
public abstract class AbstractXMLObject implements iPartsTransferConst {

    public abstract DwXmlNode getAsDwXMLNode(String namespacePrefix);

    protected abstract void loadFromXML(DwXmlNode node);

    protected boolean checkNodeType(DwXmlNode node, iPartsTransferNodeTypes nodeTypeForCheck) {
        iPartsTransferNodeTypes nodeType = XMLImportExportHelper.getIPartsNodeType(node);
        if ((nodeType != null) && (nodeType == nodeTypeForCheck)) {
            return true;
        }
        Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Wrong element for creating an " + nodeTypeForCheck + " object! Nodetype: " + nodeType);
        return false;
    }

    /**
     * Erzeugt ein XML Element, das nur einen Textinhalt und keine Attribute besitzt. Das erzeugte Element wird an das
     * übergebene <code>parentNode</code> Element gehängt.
     *
     * @param parentNode
     * @param namespacePrefix
     * @param multiText
     * @param transferNodeType
     */
    protected void appendTextElements(DwXmlNode parentNode, String namespacePrefix, EtkMultiSprache multiText, iPartsTransferNodeTypes transferNodeType) {
        if ((multiText != null) && !multiText.isEmpty() && !multiText.allStringsAreEmpty()) {
            for (Language language : iPartsLanguage.getASPLMPrimaryLanguages()) {
                DwXmlNode textChildNode = new DwXmlNode(namespacePrefix + transferNodeType.getAlias());
                textChildNode.setAttribute(ATTR_LANGUAGE, language.getCode());
                String text = multiText.getText(language.getCode());
                textChildNode.setTextContent(text);
                parentNode.appendChild(textChildNode);
            }
        }
    }
}
