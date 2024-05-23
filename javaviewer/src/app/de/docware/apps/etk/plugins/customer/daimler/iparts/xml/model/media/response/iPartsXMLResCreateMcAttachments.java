/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.response.AbstractXMLResponseOperation;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repräsentiert das "ResCreateMCAttachments" Element in der Transfer XML
 */
public class iPartsXMLResCreateMcAttachments extends AbstractXMLResponseOperation {

    private List<iPartsXMLSuccess> successList;
    private Map<String, iPartsXMLSuccess> successMap;

    public iPartsXMLResCreateMcAttachments() {
        resultType = iPartsTransferNodeTypes.RES_CREATE_MC_ATTACHMENTS;
        successList = new ArrayList<iPartsXMLSuccess>();
    }

    public iPartsXMLResCreateMcAttachments(DwXmlNode node) {
        this();
        loadFromXML(node);
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode rcma = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.RES_CREATE_MC_ATTACHMENTS.getAlias());
        for (iPartsXMLSuccess success : successList) {
            rcma.appendChild(success.getAsDwXMLNode(namespacePrefix));
        }
        return rcma;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.RES_CREATE_MC_ATTACHMENTS)) {
                for (DwXmlNode childNode : node.getChildNodes()) {
                    if (XMLImportExportHelper.checkTagWithNamespace(childNode.getName(), iPartsTransferNodeTypes.SUCCESS)) {
                        successList.add(new iPartsXMLSuccess(childNode));
                    }
                }
            }
        }
    }

    /**
     * Fügt ein Success Objekt hinzu.
     *
     * @param success
     */
    public void addSuccess(iPartsXMLSuccess success) {
        if (success != null) {
            successList.add(success);
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Attachment value must not be null.");
        }
    }

    public List<iPartsXMLSuccess> getSuccessList() {
        return successList;
    }

    /**
     * Gibt die Liste mit {@link iPartsXMLSuccess} Objekten als Map zurück. Key ist die eindeutige Attachment ID
     *
     * @return
     */
    public Map<String, iPartsXMLSuccess> getSuccessMap() {
        if (successMap == null) {
            successMap = new HashMap<String, iPartsXMLSuccess>();
        }
        if (successMap.size() != successList.size()) {
            successMap.clear();
            for (iPartsXMLSuccess success : successList) {
                successMap.put(success.getTargetId(), success);
            }
        }
        return successMap;
    }
}
