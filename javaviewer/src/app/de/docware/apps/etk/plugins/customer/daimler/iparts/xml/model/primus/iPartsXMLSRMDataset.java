/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.primus;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsMQChannelTypeNames;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractMQPartDataMessage;
import de.docware.framework.modules.xml.DwXmlNode;

/**
 * Repräsentiert einen SRM Datensatz, der über MQ importiert wird (Dient als Wrapper für die eigentliche XML Datei)
 */
public class iPartsXMLSRMDataset extends AbstractMQPartDataMessage {

    public static final String TYPE = "iPartsSRMDataset";

    public iPartsXMLSRMDataset(DwXmlNode node) {
        super(node, TYPE);
    }

    @Override
    public boolean isValidForMQChannelTypeName(iPartsMQChannelTypeNames channelTypeName) {
        return channelTypeName == iPartsMQChannelTypeNames.SRM_IMPORT;
    }

    @Override
    protected iPartsImportDataOrigin getTargetOrigin() {
        return iPartsImportDataOrigin.SRM;
    }

    @Override
    public iPartsTransferNodeTypes getIPartsNodeType() {
        return iPartsTransferNodeTypes.SRM_DATASET;
    }


}
