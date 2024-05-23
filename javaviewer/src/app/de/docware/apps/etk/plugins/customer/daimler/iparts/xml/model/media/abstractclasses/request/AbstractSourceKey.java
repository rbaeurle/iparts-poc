package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractXMLObject;
import de.docware.framework.modules.xml.DwXmlNode;

/**
 * Abstrakte Klasse für die eindeutigen SourceKeys in AS-PLM Media Nachrichten (DIALOG und Truck)
 */
public abstract class AbstractSourceKey extends AbstractXMLObject {

    public AbstractSourceKey(DwXmlNode node) {
        loadFromXML(node);
    }

    public AbstractSourceKey() {

    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode sourceKeyNode = new DwXmlNode(namespacePrefix + getSourceKeyNodeType().getAlias());
        fillSourceKeyNode(sourceKeyNode);
        return sourceKeyNode;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, getSourceKeyNodeType())) {
                fillObjectFromNode(node);
            }
        }
    }

    protected abstract void fillSourceKeyNode(DwXmlNode sourceKeyNode);

    protected abstract iPartsTransferNodeTypes getSourceKeyNodeType();

    protected abstract void fillObjectFromNode(DwXmlNode node);

}
