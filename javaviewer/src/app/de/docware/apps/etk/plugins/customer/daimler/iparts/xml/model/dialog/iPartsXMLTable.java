/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.dialog;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.framework.modules.xml.DwXmlNode;

import java.util.List;

/**
 * Repräsentiert das "Tabelle" Element (DIALOG) in der Transfer XML. Die zu der DIALOG Tabelle gehörenden Datensätze werden in der Oberklasse gespeichert.
 * iPartsXMLTable ist eine {@link iPartsXMLMixedTable} mit nur einer Tabelle.
 */
public class iPartsXMLTable extends iPartsXMLMixedTable {

    public static final String TYPE = "iPartsImportTable";
    private static String UNKNOWN_TABLE = "Unknown table";

    private String tableName = UNKNOWN_TABLE;
    private String schemaVersion;

    public iPartsXMLTable() {
        super();
        messageType = TYPE;
        nodeType = iPartsTransferNodeTypes.TABLE;
    }

    public iPartsXMLTable(DwXmlNode node) {
        this();
        loadFromXML(node);
    }


    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Überprüft, ob der tableName identisch ist
     *
     * @param tableName
     * @return
     */
    public boolean isTableNameEqual(String tableName) {
        return this.tableName.equals(tableName);
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    /**
     * Anzahl Datensätze für die übergebene Tabelle
     *
     * @return
     */
    public int getNumberOfDatasets() {
        return super.getNumberOfDatasetsForTable(getTableName());
    }

    /**
     * Gibt die Datensätze zurück
     *
     * @return
     */
    public List<iPartsXMLTableDataset> getDatasets() {
        return super.getDatasetForTableName(getTableName());
    }


    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode tableNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.TABLE.getAlias());
        tableNode.setAttribute(ATTR_M_NAMESPACE_SCHEMA, DEFAULT_NAMESPACE_SCHEMA);
        tableNode.setAttribute(DEFAULT_ATTR_NAMESPACE_LOCATION_DIALOG, DEFAULT_NAMESPACE_LOCATION_DIALOG);
        tableNode.setAttribute(ATTR_TABLE_ORIGIN, getOrigin());
        tableNode.setAttribute(ATTR_TABLE_SOURCE_EXPORT_TIME, getSourceExportTime());
        tableNode.setAttribute(ATTR_TABLE_TRAFO_TIME, getTrafoTime());
        for (iPartsXMLTableDataset dataset : getDatasets()) {
            tableNode.appendChild(dataset.getAsDwXMLNode(namespacePrefix));
        }

        return tableNode;
    }

    @Override
    public void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.TABLE)) {
                if (node.hasAttributes()) {
                    setOrigin(node.getAttribute(ATTR_TABLE_ORIGIN));
                    setSourceExportTime(node.getAttribute(ATTR_TABLE_SOURCE_EXPORT_TIME));
                    setTrafoTime(node.getAttribute(ATTR_TABLE_TRAFO_TIME));
                    setTableName(node.getAttribute(ATTR_NAME));
                    setSchemaVersion(node.getAttribute(ATTR_TABLE_SCHEMA_VERSION));
                }
                List<DwXmlNode> childNodes = node.getChildNodes();
                for (DwXmlNode childNode : childNodes) {
                    addDataset(new iPartsXMLTableDataset(this, childNode));
                }
            }
        }
    }

    @Override
    public boolean isValid() {
        return !tableName.equals(UNKNOWN_TABLE);
    }
}
