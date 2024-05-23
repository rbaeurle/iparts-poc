/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractXMLObject;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.util.collections.dwlist.DwList;

import java.util.List;

/**
 * Repräsentiert das "Warning" Element in der Transfer XML
 */
public class iPartsXMLWarning extends AbstractXMLObject {

    private List<iPartsXMLWarningText> warningTexts;

    public iPartsXMLWarning(iPartsXMLWarningText warningText) {
        addWarningText(warningText);
    }

    public iPartsXMLWarning(DwXmlNode node) {
        loadFromXML(node);

    }

    public List<iPartsXMLWarningText> getWarningTexts() {
        return warningTexts;
    }

    public void addWarningText(iPartsXMLWarningText warningText) {
        if (warningTexts == null) {
            warningTexts = new DwList<iPartsXMLWarningText>();
        }
        warningTexts.add(warningText);
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode warningNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.WARNING.getAlias());
        if (warningTexts != null) {
            for (iPartsXMLWarningText warningText : warningTexts) {
                warningNode.appendChild(warningText.getAsDwXMLNode(namespacePrefix));
            }
        }
        return warningNode;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.WARNING)) {
                List<DwXmlNode> childNodes = node.getChildNodes();
                for (DwXmlNode childNode : childNodes) {
                    addWarningText(new iPartsXMLWarningText(childNode));
                }
            }
        }
    }
}
