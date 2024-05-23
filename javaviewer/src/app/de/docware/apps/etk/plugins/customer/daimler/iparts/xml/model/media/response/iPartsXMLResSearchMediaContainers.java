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
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.List;

/**
 * Repräsentiert das "ResSearchMediaContainers" Element in der Transfer XML
 */
public class iPartsXMLResSearchMediaContainers extends AbstractXMLResponseOperation {

    private List<iPartsXMLMediaContainer> mContainers;
    private int numResultsDelivered = -1;
    private int numResultsFound = -1;

    public iPartsXMLResSearchMediaContainers(int numResultsDelivered) {
        this.numResultsDelivered = numResultsDelivered;
        this.resultType = iPartsTransferNodeTypes.RES_SEARCH_MEDIA_CONTAINERS;
        mContainers = new DwList<iPartsXMLMediaContainer>();
    }

    public iPartsXMLResSearchMediaContainers(DwXmlNode node) {
        this(-1);
        loadFromXML(node);
    }

    public int getNumResultsDelivered() {
        return numResultsDelivered;
    }

    public void setNumResultsDelivered(int numResultsDelivered) {
        this.numResultsDelivered = numResultsDelivered;
    }

    public int getNumResultsFound() {
        return numResultsFound;
    }

    public void setNumResultsFound(int numResultsFound) {
        this.numResultsFound = numResultsFound;
    }

    public void addMediaContainer(iPartsXMLMediaContainer mContainer) {
        if (mContainer != null) {
            mContainers.add(mContainer);
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "MediaContainer must not be null!");
        }
    }

    public List<iPartsXMLMediaContainer> getMContainers() {
        return mContainers;
    }

    public boolean hasResultsDelivered() {
        return numResultsDelivered > 0;
    }

    public boolean hasResultsFound() {
        return numResultsFound > 0;
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode rsmc = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.RES_SEARCH_MEDIA_CONTAINERS.getAlias());
        rsmc.setAttribute(ATT_RSMC_RESULTS_DELIVERED, String.valueOf(numResultsDelivered)); // ATT_RSMC_RESULTS_DELIVERED ist required
        if (hasResultsFound()) {
            rsmc.setAttribute(ATT_RSMC_RESULTS_FOUND, String.valueOf(numResultsFound));
        }
        if ((mContainers != null) && !mContainers.isEmpty()) {
            for (iPartsXMLMediaContainer mc : mContainers) {
                rsmc.appendChild(mc.getAsDwXMLNode(namespacePrefix));
            }
        }
        return rsmc;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.RES_SEARCH_MEDIA_CONTAINERS)) {

                // ATT_RSMC_RESULTS_DELIVERED ist required
                String resultsDelivered = node.getAttribute(ATT_RSMC_RESULTS_DELIVERED);
                if (StrUtils.isValid(resultsDelivered)) {
                    setNumResultsDelivered(StrUtils.strToIntDef(resultsDelivered, 0));
                } else {
                    setNumResultsDelivered(0);
                }

                String resultsFound = node.getAttribute(ATT_RSMC_RESULTS_FOUND);
                if (StrUtils.isValid(resultsFound)) {
                    setNumResultsFound(StrUtils.strToIntDef(resultsFound, 0));
                }

                List<DwXmlNode> childNodes = node.getChildNodes();
                for (DwXmlNode childNode : childNodes) {
                    iPartsTransferNodeTypes nodeType = XMLImportExportHelper.getIPartsNodeType(childNode);
                    if (nodeType == null) {
                        continue;
                    }
                    switch (nodeType) {
                        case MEDIA_CONTAINER:
                            addMediaContainer(new iPartsXMLMediaContainer(childNode));
                            break;
                    }
                }
            }
        }
    }

    @Override
    public void convertToNotificationOnly() {
        super.convertToNotificationOnly();
        if (mContainers != null) {
            for (iPartsXMLMediaContainer mContainer : mContainers) {
                mContainer.convertToNotificationOnly();
            }
        }
    }
}
