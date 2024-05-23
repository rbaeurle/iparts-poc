/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.primus;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsMQChannelTypeNames;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractMQPartDataMessage;
import de.docware.framework.modules.xml.DwXmlNode;

/**
 * Repräsentiert einen PRIMUS Datensatz, der über MQ importiert wird (Dient als Wrapper für die eigentliche XML Datei)
 */
public class iPartsXMLPrimusDataset extends AbstractMQPartDataMessage {

    public static final String TYPE = "iPartsPRIMUSDataset";

    public iPartsXMLPrimusDataset(DwXmlNode node) {
        super(node, TYPE);
    }

    @Override
    public boolean isValidForMQChannelTypeName(iPartsMQChannelTypeNames channelTypeName) {
        return channelTypeName == iPartsMQChannelTypeNames.PRIMUS_IMPORT;
    }


    public iPartsImportDataOrigin getTargetOrigin() {
        return iPartsImportDataOrigin.PRIMUS;
    }

    @Override
    public iPartsTransferNodeTypes getIPartsNodeType() {
        return iPartsTransferNodeTypes.PRIMUS_DATASET;
    }
}
