package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request.AbstractSourceKey;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.util.StrUtils;

import java.util.Set;

/**
 * Repräsentiert das "SourceKeyTruck" Element in der Transfer XML
 */
public class iPartsXMLSourceKeyTruck extends AbstractSourceKey {

    private String saaValidityAsString;

    public iPartsXMLSourceKeyTruck(Set<String> saaValidityAsString) {
        setSaaValidityAsString(saaValidityAsString);
    }

    private void setSaaValidityAsString(Set<String> saaValidityAsString) {
        if ((saaValidityAsString != null) && !saaValidityAsString.isEmpty()) {
            this.saaValidityAsString = StrUtils.stringListToString(saaValidityAsString, ",");
        }
    }

    public iPartsXMLSourceKeyTruck(String saaValidityasString) {
        this.saaValidityAsString = saaValidityasString;
    }

    public iPartsXMLSourceKeyTruck(DwXmlNode node) {
        super(node);
    }

    @Override
    protected void fillSourceKeyNode(DwXmlNode sourceKeyNode) {
        if (StrUtils.isValid(saaValidityAsString)) {
            sourceKeyNode.setTextContent(saaValidityAsString);
        }
    }

    @Override
    protected iPartsTransferNodeTypes getSourceKeyNodeType() {
        return iPartsTransferNodeTypes.SOURCE_KEY_TRUCK;
    }

    @Override
    protected void fillObjectFromNode(DwXmlNode node) {
        saaValidityAsString = node.getTextContent();
    }
}
