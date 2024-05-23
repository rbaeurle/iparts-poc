/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.dialog;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsMQChannelTypeNames;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractMQMessage;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Repräsentiert das "TabelleGemischt" Element (DIALOG) in der Transfer XML. Oberklasse von {@link iPartsXMLTable}.
 * Enthält mehrere DIALOG/EDS Tabellen mit den jeweiligen Datensätzen
 */
public class iPartsXMLMixedTable extends AbstractMQMessage {

    public static final String TYPE = "iPartsImportMixedTable";
    public static final String INVALID_SCHEMAVERSION = "No schema version - from mixed table";

    private String origin;
    private String sourceExportTime;
    private String trafoTime;
    private LinkedHashMap<String, List<iPartsXMLTableDataset>> datasets;
    protected iPartsTransferNodeTypes nodeType;

    public iPartsXMLMixedTable() {
        super();
        messageType = TYPE;
        this.nodeType = iPartsTransferNodeTypes.MIXED_TABLE;
        datasets = new LinkedHashMap<String, List<iPartsXMLTableDataset>>();
    }

    public iPartsXMLMixedTable(DwXmlNode node) {
        this();
        loadFromXML(node);
    }

    @Override
    public boolean isValidForMQChannelTypeName(iPartsMQChannelTypeNames channelTypeName) {
        switch (channelTypeName) {
            case DIALOG_IMPORT:
            case DIALOG_DELTA_IMPORT:
            case EDS_IMPORT:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean isValid() {
        return true;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getSourceExportTime() {
        return sourceExportTime;
    }

    public void setSourceExportTime(String sourceExportTime) {
        this.sourceExportTime = sourceExportTime;
    }

    public String getTrafoTime() {
        return trafoTime;
    }

    public void setTrafoTime(String trafoTime) {
        this.trafoTime = trafoTime;
    }

    public iPartsTransferNodeTypes getNodeType() {
        return nodeType;
    }

    /**
     * Fügt einen Datensatz hinzu. Wenn die dazugehörige Tabelle nicht existiert->neu anlegen
     *
     * @param dataset
     */

    public void addDataset(iPartsXMLTableDataset dataset) {
        String tableName = dataset.getTableName();
        if (tableName != null) {
            if (datasets.containsKey(tableName)) {
                datasets.get(tableName).add(dataset);
            } else {
                List<iPartsXMLTableDataset> data = new ArrayList<iPartsXMLTableDataset>();
                data.add(dataset);
                datasets.put(tableName, data);
            }
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "\"Tablename\" for mixed table must not be null!");
        }
    }

    /**
     * Gibt alle Tabellen samt Datensätze zurück
     *
     * @return
     */
    public Map<String, List<iPartsXMLTableDataset>> getTablesWithDatasets() {
        return Collections.unmodifiableMap(datasets);
    }

    /**
     * Gibt die Datensätze für eine bestimmte Tabelle zurück
     *
     * @param tableName
     * @return
     */
    public List<iPartsXMLTableDataset> getDatasetForTableName(String tableName) {
        return Collections.unmodifiableList(datasets.get(tableName));
    }

    /**
     * Gibt die Namen der Tabellen zurück
     *
     * @return
     */
    public List<String> getTableNames() {
        List<String> result = new DwList<String>();
        for (String tableName : datasets.keySet()) {
            result.add(tableName);
        }
        return result;
    }

    /**
     * Anzahl der verschiedenen Tabellen
     *
     * @return
     */
    public int getNumberOfTables() {
        return datasets.size();
    }

    /**
     * Überprüft, ob der gesuchte TableName in den MixedTables enthalten ist
     *
     * @param tableName
     * @return
     */
    public boolean tableNamesContains(String tableName) {
        for (String intTableName : datasets.keySet()) {
            if (intTableName.equals(tableName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Anzahl Datensätze für die übergebene Tabelle
     *
     * @param tablename
     * @return
     */
    public int getNumberOfDatasetsForTable(String tablename) {
        if (datasets.containsKey(tablename)) {
            return datasets.get(tablename).size();
        }
        return 0;
    }

    /**
     * Check, ob es eine Mixed oder Single Tabelle ist
     *
     * @return
     */
    public boolean isMixedTable() {
        return getNumberOfTables() > 1;
    }

    /**
     * Wandelt diese MixedTable in eine Liste mit {@link iPartsXMLTable} Objekten um und gibt sie zurück.
     *
     * @return
     */
    public List<iPartsXMLTable> getAsSingleTablesList() {
        List<iPartsXMLTable> singleTables = new ArrayList<iPartsXMLTable>();
        iPartsXMLTable xmlTable;
        for (String table : getTableNames()) {
            xmlTable = new iPartsXMLTable();
            xmlTable.setTableName(table);
            xmlTable.setOrigin(getOrigin());
            xmlTable.setSchemaVersion(INVALID_SCHEMAVERSION);
            xmlTable.setTrafoTime(getTrafoTime());
            xmlTable.setSourceExportTime(getSourceExportTime());
            xmlTable.setMQChannelType(getMQChannelType());
            for (iPartsXMLTableDataset dataset : getDatasetForTableName(table)) {
                xmlTable.addDataset(dataset);
            }
            singleTables.add(xmlTable);
        }
        return singleTables;
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode mixedTabelNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.MIXED_TABLE.getAlias());
        mixedTabelNode.setAttribute(ATTR_M_NAMESPACE_SCHEMA, DEFAULT_NAMESPACE_SCHEMA);
        mixedTabelNode.setAttribute(DEFAULT_ATTR_NAMESPACE_LOCATION_DIALOG, DEFAULT_NAMESPACE_LOCATION_DIALOG);
        mixedTabelNode.setAttribute(ATTR_TABLE_ORIGIN, origin);
        mixedTabelNode.setAttribute(ATTR_TABLE_SOURCE_EXPORT_TIME, sourceExportTime);
        mixedTabelNode.setAttribute(ATTR_TABLE_TRAFO_TIME, trafoTime);
        for (List<iPartsXMLTableDataset> datasetList : datasets.values()) {
            for (iPartsXMLTableDataset dataset : datasetList) {
                mixedTabelNode.appendChild(dataset.getAsDwXMLNode(namespacePrefix));
            }
        }
        return mixedTabelNode;
    }

    @Override
    public void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.MIXED_TABLE)) {
                if (node.hasAttributes()) {
                    setOrigin(node.getAttribute(ATTR_TABLE_ORIGIN));
                    setSourceExportTime(node.getAttribute(ATTR_TABLE_SOURCE_EXPORT_TIME));
                    setTrafoTime(node.getAttribute(ATTR_TABLE_TRAFO_TIME));
                }
                List<DwXmlNode> childNodes = node.getChildNodes();
                for (DwXmlNode childNode : childNodes) {
                    addDataset(new iPartsXMLTableDataset(this, childNode));
                }
            }
        }
    }
}
