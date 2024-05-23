/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsMQChannelTypeNames;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractMQMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractMQPartDataMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.dialog.iPartsXMLMixedTable;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.dialog.iPartsXMLTable;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.iPartsXMLMediaMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.primus.iPartsXMLPrimusDataset;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.primus.iPartsXMLSRMDataset;
import de.docware.framework.modules.xml.DwXmlNode;

public class MQMessageFactory {

    public static AbstractMQMessage createMainMessage(iPartsTransferNodeTypes nodeType, DwXmlNode node,
                                                      boolean createBinaryObjectsEnabled, boolean writeDebugFilesEnabled) {
        switch (nodeType) {
            case MESSAGE:
                return new iPartsXMLMediaMessage(node, createBinaryObjectsEnabled, writeDebugFilesEnabled);
            case TABLE:
                return new iPartsXMLTable(node);
            case MIXED_TABLE:
                return new iPartsXMLMixedTable(node);
            case PRIMUS_DATASET:
                return new iPartsXMLPrimusDataset(node);
            case SRM_DATASET:
                return new iPartsXMLSRMDataset(node);
        }
        return null;
    }

    public static AbstractMQPartDataMessage createPartDataMessage(iPartsMQChannelTypeNames channelTypeName, DwXmlNode node) {
        switch (channelTypeName) {
            case SRM_IMPORT:
                return new iPartsXMLSRMDataset(node);
            case PRIMUS_IMPORT:
                return new iPartsXMLPrimusDataset(node);
        }
        return null;
    }
}
