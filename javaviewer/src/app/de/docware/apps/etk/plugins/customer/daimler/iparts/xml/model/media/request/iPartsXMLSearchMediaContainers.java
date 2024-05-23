/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferSMCAttributes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request.AbstractXMLRequestOperation;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;

import java.util.*;

/**
 * Repräsentiert das "SearchMediaContainers" Element in der Transfer XML
 */
public class iPartsXMLSearchMediaContainers extends AbstractXMLRequestOperation {

    private int maxResultFromIParts = -1;
    private Map<iPartsTransferSMCAttributes, iPartsXMLSearchCriterion> searchCriteria;
    private Set<iPartsTransferSMCAttributes> resultAttributes;

    public iPartsXMLSearchMediaContainers() {
        setOperationType(iPartsTransferNodeTypes.SEARCH_MEDIA_CONTAINERS);
        this.searchCriteria = new LinkedHashMap<iPartsTransferSMCAttributes, iPartsXMLSearchCriterion>();
        this.resultAttributes = new LinkedHashSet<iPartsTransferSMCAttributes>();
    }

    public iPartsXMLSearchMediaContainers(DwXmlNode node) {
        this();
        loadFromXML(node);
    }


    public Map<iPartsTransferSMCAttributes, iPartsXMLSearchCriterion> getSearchCriteria() {
        return searchCriteria;
    }

    /**
     * Fügt der Suchanfrage ein neues Suchkriterium hinzu. Das Suchattribut wird zusätzlich als Ergebnis-Attribut gesetzt
     *
     * @param attributeName
     * @param attributeValue
     */
    public void addSearchCriterion(iPartsTransferSMCAttributes attributeName, String attributeValue) {
        if (searchCriteria != null) {
            if ((attributeName != null) && (attributeValue != null && !attributeValue.isEmpty())) {
                searchCriteria.put(attributeName, new iPartsXMLSearchCriterion(attributeName, attributeValue));
                resultAttributes.add(attributeName);
            } else {
                Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "addSearchCriterion: Attribute name or value must not be null or empty.");
            }
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "addSearchCriterion: List with SearchCriterions must not be null.");
        }
    }

    /**
     * Entfernt das Suchkriterium, das zum übergebenen Attributnamen gehört
     *
     * @param attributeName
     */
    public void removeSearchCriterion(iPartsTransferSMCAttributes attributeName) {
        if (searchCriteria != null) {
            if (attributeName != null) {
                searchCriteria.remove(attributeName);
                resultAttributes.remove(attributeName);
            } else {
                Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "removeSearchCriterion: Attribute name or value must not be null or empty.");
            }
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "removeSearchCriterion: List with SearchCriterions must not be null.");
        }
    }

    public void addSearchCriterion(iPartsXMLSearchCriterion searchCriterion) {
        if (searchCriteria != null) {
            if ((searchCriterion.getAttributeName() != null) && (searchCriterion.getAttributeValue() != null && !searchCriterion.getAttributeValue().isEmpty())) {
                searchCriteria.put(searchCriterion.getAttributeName(), searchCriterion);
                resultAttributes.add(searchCriterion.getAttributeName());

            } else {
                Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "addSearchCriterion: Attribute name or value must not be null or empty.");
            }
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "addSearchCriterion: List with SearchCriterions must not be null.");
        }
    }

    public boolean hasSearchCriteria() {
        return !searchCriteria.isEmpty();
    }

    public iPartsXMLSearchCriterion getSearchCriterionByAttributeName(iPartsTransferSMCAttributes attName) {
        return searchCriteria.get(attName);
    }

    public int getMaxResultFromIParts() {
        return maxResultFromIParts;
    }

    public void setMaxResultFromIParts(int maxResultFromIParts) {
        this.maxResultFromIParts = maxResultFromIParts;
    }

    /**
     * Fügt der Suchanfrage ein gewünschtes Ergebnis-Attribut hinzu.
     *
     * @param attribute
     */
    public void addResultAttribut(iPartsTransferSMCAttributes attribute) {
        if (attribute != null) {
            resultAttributes.add(attribute);
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Attribute name or value must not be null or empty.");
        }
    }

    public boolean hasResultAttributes() {
        return !resultAttributes.isEmpty();
    }


    public boolean hasMaxResult() {
        return maxResultFromIParts > 0;
    }

    public Set<iPartsTransferSMCAttributes> getResultAttributes() {
        return resultAttributes;
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode smcNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.SEARCH_MEDIA_CONTAINERS.getAlias());
        if (hasMaxResult()) {
            smcNode.setAttribute(ATTR_SMC_MAX_RESULTS, String.valueOf(maxResultFromIParts));
        }
        for (iPartsXMLSearchCriterion sc : searchCriteria.values()) {
            smcNode.appendChild(sc.getAsDwXMLNode(namespacePrefix));
        }
        for (iPartsTransferSMCAttributes resultAtt : resultAttributes) {
            DwXmlNode resultAttNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.RESULT_ATTRIBUTE.getAlias());
            resultAttNode.setAttribute(ATTR_SMC_NAME, resultAtt.getAsASPLMValue());
            smcNode.appendChild(resultAttNode);
        }
        return smcNode;

    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.SEARCH_MEDIA_CONTAINERS)) {
                String maxResult = node.getAttribute(ATTR_SMC_MAX_RESULTS);
                if (maxResult != null) {
                    setMaxResultFromIParts(Integer.parseInt(maxResult));
                }
                fillSearchMediaContainers(node.getChildNodes());
            }
        }
    }

    private void fillSearchMediaContainers(List<DwXmlNode> childNodes) {
        for (DwXmlNode childNode : childNodes) {
            if (XMLImportExportHelper.checkTagWithNamespace(childNode.getName(), iPartsTransferNodeTypes.SEARCH_CRITERION)) {
                addSearchCriterion(new iPartsXMLSearchCriterion(childNode));
            } else if (XMLImportExportHelper.checkTagWithNamespace(childNode.getName(), iPartsTransferNodeTypes.RESULT_ATTRIBUTE)) {
                iPartsTransferSMCAttributes attName = iPartsTransferSMCAttributes.getValidatedAttributeByDescription(childNode.getAttribute(ATTR_SMC_NAME));
                addResultAttribut(attName);
            }
        }
    }
}
