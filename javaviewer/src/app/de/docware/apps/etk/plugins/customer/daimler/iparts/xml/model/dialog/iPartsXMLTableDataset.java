/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.dialog;

import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractXMLObject;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.framework.modules.xml.DwXmlNodeAttribute;

import java.util.*;

/**
 * Repräsentiert den Datensatz einer DIALOG Tabelle
 */
public class iPartsXMLTableDataset extends AbstractXMLObject {

    public static final String SUB_DATASETS = "hasSubdatasets";

    private String kem;
    private String seqNo;
    private String origin;
    private String tableName;
    private LinkedHashMap<String, String> tagsAndValues;
    private LinkedHashMap<String, iPartsXMLTableDataset> tagsAndValuesWithDatasets;
    private iPartsSDBValues sdbValue;

    public iPartsXMLTableDataset(iPartsXMLMixedTable table, DwXmlNode node) {
        this();
        origin = table.getOrigin();
        sdbValue = iPartsSDBValues.NO_VALUE;
        if (table instanceof iPartsXMLTable) {
            this.tableName = ((iPartsXMLTable)table).getTableName();
        }
        loadFromXML(node);
    }

    public iPartsXMLTableDataset(String kem, String seqNo) {
        this();
        this.kem = kem;
        this.seqNo = seqNo;
    }

    public iPartsXMLTableDataset(String tableName, DwXmlNode node) {
        this();
        this.tableName = tableName;
        loadFromXML(node);
    }

    private iPartsXMLTableDataset() {
        this.tagsAndValues = new LinkedHashMap<String, String>();
        this.tagsAndValuesWithDatasets = new LinkedHashMap<String, iPartsXMLTableDataset>();
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public Map<String, String> getTagsAndValues() {
        return tagsAndValues;
    }

    public iPartsSDBValues getSdbValue() {
        return sdbValue;
    }

    /**
     * Gibt alle Tags (Schlüssel) des Datensatzes zurück
     *
     * @return
     */
    public Set<String> getTags() {
        if (tagsAndValues != null && !tagsAndValues.isEmpty()) {
            return Collections.unmodifiableSet(tagsAndValues.keySet());
        }
        return null;
    }

    /**
     * Fügt einen Tag + Value hinzu (Schlüssel + Value) bzw. Tag,Tag + Value falls das Tag bereits existiert
     *
     * @param tag
     * @param value
     */
    public void addTagAndValue(String tag, String value, List<DwXmlNodeAttribute> attributes) {
        if ((tag != null) && !tag.isEmpty() && (value != null)) {
            String keyValue = getValidKeyValue(tag, tagsAndValues.keySet());
            tagsAndValues.put(keyValue, value);
            // Attribute werden als eigene Key-Value Paare abgelegt. Um die Zugehörigkeit zum Element zu erhalten, werden
            // Element- und Attributname zu einem Key-Wert verknüpft. Vor allem wichtig bei EDS "Key-Value" Daten, die
            // mehrstufig sein können und Attribute enthalten
            addAttributesAsKeyValues(keyValue, attributes);
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Tag must not be null or empty! Tag: " + tag + ";Value: " + value);
        }
    }

    /**
     * Fügt einem XML Element die angehängten Attribute als Key-Value Werte hinzu
     *
     * @param keyValue
     * @param attributes
     */
    private void addAttributesAsKeyValues(String keyValue, List<DwXmlNodeAttribute> attributes) {
        if (checkDialogDatasetAttribute(attributes)) {
            return;
        }
        if (attributes != null) {
            for (DwXmlNodeAttribute attribute : attributes) {
                String attributeKey = getValidKeyValue(createAttributeValue(keyValue, attribute.getName()), tagsAndValues.keySet());
                tagsAndValues.put(attributeKey, attribute.getValue());
            }
        }
    }

    /**
     * Erzeugt den Schlüssel aus dem XML Element und dem Attributnamen
     *
     * @param keyValue
     * @param attName
     * @return
     */
    public static String createAttributeValue(String keyValue, String attName) {
        return keyValue + ELEMENT_ATTRIBUTE_DELIMITER + attName;
    }

    /**
     * Überprüft, ob es sich bei den übergebenen Attributen um DIALOG spezifische Attribute handelt. Falls ja, werden
     * die dazugehörigen Variablen gesetzt.
     *
     * @param attributes
     * @return
     */
    private boolean checkDialogDatasetAttribute(List<DwXmlNodeAttribute> attributes) {
        boolean result = false;
        // DIALOG-spezifische Attribute müssen speziell behandelt werden
        for (DwXmlNodeAttribute attribute : attributes) {
            String value = attribute.getValue();
            if (attribute.getName().equals(ATTR_TABLE_SEQUENCE_NO)) {
                seqNo = value;
                result = true;
            } else if (attribute.getName().equals(ATTR_TABLE_KEM)) {
                kem = value;
                result = true;
            } else if (attribute.getName().equals(ATTR_TABLE_SDB_FLAG)) {
                sdbValue = iPartsSDBValues.getFromOriginalValue(value);
                result = true;
            } else if (attribute.getName().equals(ATTR_TABLE_ORIGIN)) {
                if ((origin == null) && (value != null)) {
                    origin = value;
                }
            }
        }
        return result;
    }

    public void addTagAndValueWithDataset(String tag, iPartsXMLTableDataset subDataset) {
        if ((tag != null) && !tag.isEmpty() && (subDataset != null)) {
            String keyValue = getValidKeyValue(tag, tagsAndValuesWithDatasets.keySet());
            tagsAndValuesWithDatasets.put(keyValue, subDataset);
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Tag must not be null or empty! Tag: " + tag + "; Subdataset: " + subDataset);
        }
    }

    /**
     * Gibt das Value für einen bestimmten Tag zurück. Falls das Value ein SubDataset hat, wird {@link #SUB_DATASETS} zurückgegeben.
     *
     * @param tag
     * @return
     */
    public String getValueForTag(String tag) {
        return tagsAndValues.get(tag);
    }

    /**
     * Liefert zurück, ob eines der Elemente dieses Datensatzes einen Subdatensazt besitzt.
     *
     * @return
     */
    public boolean hasSubDataSets() {
        return !tagsAndValuesWithDatasets.isEmpty();
    }

    /**
     * Liefert zurück, ob das übergebene Element einen Subdatensatz enthält.
     *
     * @param tag
     * @return
     */
    public boolean hasSubDataSetsForTag(String tag) {
        return tagsAndValues.get(tag).equals(SUB_DATASETS);
    }

    /**
     * Liefert den Subdatensatz für das übergebene Element zurück
     *
     * @param tag
     * @return
     */
    public iPartsXMLTableDataset getSubDataset(String tag) {
        if (!tagsAndValues.get(tag).equals(SUB_DATASETS)) {
            return null;
        }
        return tagsAndValuesWithDatasets.get(tag);
    }

    public String getTableName() {
        return tableName;
    }

    public String getKem() {
        return kem;
    }

    public String getSeqNo() {
        return seqNo;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int size() {
        return tagsAndValues.size();
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode datasetNode = new DwXmlNode(namespacePrefix + tableName);
        datasetNode.setAttribute(ATTR_TABLE_SEQUENCE_NO, seqNo);
        if (kem == null) {
            kem = "";
        }
        datasetNode.setAttribute(ATTR_TABLE_KEM, kem);
        for (String key : tagsAndValues.keySet()) {
            DwXmlNode childNode = new DwXmlNode(namespacePrefix + key);
            if (hasSubDataSetsForTag(key)) {
                childNode.appendChild(tagsAndValuesWithDatasets.get(key).getAsDwXMLNode(namespacePrefix));
            } else {
                childNode.setTextContent(tagsAndValues.get(key));
            }
            datasetNode.appendChild(childNode);
        }
        return datasetNode;

    }

    @Override
    public void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if ((tableName == null) || tableName.isEmpty()) {
                tableName = node.getName();
            }
            // Hat der Knoten Kinder, dann wird geprüft, ob es sich um einen Datensatz mit Sub-Datensätzen handelt (Beim EDS/BCS Import)
            if (node.hasChildNodes()) {
                List<DwXmlNode> childNodes = node.getChildNodes();
                for (DwXmlNode child : childNodes) {
                    if (child.hasChildNodes()) {
                        iPartsXMLTableDataset subDataset = new iPartsXMLTableDataset(getTableName(), child);
                        addTagAndValueWithDataset(child.getName(), subDataset);
                        addTagAndValue(child.getName(), SUB_DATASETS, child.getAttributes());
                    } else {
                        addTagAndValue(child.getName(), child.getTextContent(), child.getAttributes());
                    }
                }
                addAttributesAsKeyValues(node.getName(), node.getAttributes());
            } else {
                addTagAndValue(node.getName(), node.getTextContent(), node.getAttributes());
            }
        }
    }

    public String getValidKeyValue(String tag, Set<String> tagsAndValues) {
        String keyValue = tag;
        int order = 1;
        while (tagsAndValues.contains(keyValue)) {
            keyValue = tag + "_" + EtkDbsHelper.formatLfdNr(order);
            order++;
        }
        return keyValue;
    }
}
