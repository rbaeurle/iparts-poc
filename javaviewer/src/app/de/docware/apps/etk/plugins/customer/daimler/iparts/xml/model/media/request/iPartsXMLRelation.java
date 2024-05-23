/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsRelationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractXMLObject;
import de.docware.framework.modules.xml.DwXmlNode;

/**
 * Repräsentiert das "Relation" Element in der Transfer XML
 */
public class iPartsXMLRelation extends AbstractXMLObject {

    private iPartsRelationType relationType;

    public iPartsXMLRelation(iPartsRelationType relationType) {
        this.relationType = relationType;
    }

    public iPartsXMLRelation(DwXmlNode node) {
        loadFromXML(node);
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode relationNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.RELATION.getAlias());
        if (relationType != null) {
            relationNode.setAttribute(ATTR_NAME, relationType.getRelationValue());
        }
        return relationNode;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.RELATION)) {
                relationType = iPartsRelationType.getFromAlias(node.getTextContent());
            }
        }
    }
}
