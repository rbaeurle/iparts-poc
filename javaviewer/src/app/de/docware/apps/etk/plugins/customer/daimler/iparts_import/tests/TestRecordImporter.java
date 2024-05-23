/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.tests;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.KeyValueRecordFileReader;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.MQChannelType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsMQChannelTypeNames;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractMQMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.dialog.iPartsXMLMixedTable;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.dialog.iPartsXMLTable;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsDIALOGImportFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.iPartsKeyValueRecordReader;
import de.docware.framework.modules.config.ConfigBase;
import de.docware.framework.modules.config.containers.XMLConfigContainer;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlFile;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import de.docware.util.test.AbstractTest;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestRecordImporter extends AbstractTest {

    @Override
    protected String getTestSuite() {
        return iPartsPlugin.INTERNAL_PLUGIN_NAME;
    }

    @Override
    protected void setUp() throws Exception {
        useTestCaseFilesFromVCS();
        super.setUp();

        // iParts Plug-in initialisieren, damit die PluginConfig existiert
        iPartsPlugin iPartsPlugin = new iPartsPlugin();
        ConfigBase pluginConfig = new ConfigBase(XMLConfigContainer.getInstanceInMemory());
        iPartsPlugin.initPlugin(pluginConfig);
        iPartsPlugin.XML_SCHEMA_PATH = AbstractTest.DEFAULT_TESTCASE_BASE_DIR.getChild("de_docware_apps_etk_plugins_customer_daimler_iparts_tests_xml_AbstractTestXMLBase").getAbsolutePath();

        // Pfad für die kopierten MQ-XML-Dateien
        String xmlCopyDataFilesPath = tmpDir().getChild("ASPLM_Simulation").getPath();
        pluginConfig.setFileName(iPartsPlugin.getPluginConfig().getPath() + iPartsPlugin.CONFIG_XML_FILES_DIR.getKey(), xmlCopyDataFilesPath);
    }

    private static final String EDS_E_USNR = "E_USNR";
    private static final String EDS_BEN = "BEN";
    private static final String EDS_ZGS = "ZGS";
    private static final String EDS_HINWEISZ = "HINWEISZ";
    private static final String EDS_ZDAT = "ZDAT";
    private static final String EDS_GEWOGEN = "GEWOGEN";
    private static final String EDS_BERECHNET = "BERECHNET";
    private static final String EDS_PROGNOSE = "PROGNOSE";
    private static final String EDS_USNR_AS = "USNR_AS";
    private static final String EDS_ME = "ME";
    private static final String EDS_ETDOK = "ETDOK";
    private static final String EDS_ET_KZ_GESAMT = "ET_KZ_GESAMT";
    private static final String EDS_BEM1 = "BEM1";
    private static final String EDS_BEM2 = "BEM2";
    private static final String EDS_WERKSTOFF = "WERKSTOFF";

    public void testFileImporter() {
        DWFile testFile = tmpDir().getChild("EDS Stammdaten.xlsx");
        String[] mustExists = new String[]{ EDS_E_USNR };
        String[] mustHaveData = new String[]{ EDS_E_USNR };
        Map<String, String> partMapping = initMapping();
        final int RECORD_COUNT = 24; // Anzahl Datenzeilen

        KeyValueRecordFileReader importer = new KeyValueRecordFileReader(testFile, "TABLE_MAT", true, null);
        boolean result = false;
        try {
            result = importer.open();
        } catch (Exception e) {
            Logger.getLogger().throwRuntimeException(e);
        }
        assertEquals(true, result);
        if (result) {
            importer.setMustExists(mustExists);
            importer.setMustHaveData(mustHaveData);
            int maxRow = importer.getRecordCount();
            assertEquals(RECORD_COUNT, maxRow);
            List<String> errors = new DwList<String>();
            Map<String, String> attributes = importer.getNextRecord();
            assertNotNull(attributes);
            while (attributes != null) {
                int actRecord = importer.getRecordNo();
                errors.clear();
                result = importer.isRecordValid(attributes, errors);
                assertEquals(true, result);
                if (result) {
                    // jetzt Record bilden und abspeichern
                    String partNo = attributes.get(EDS_E_USNR);
                }
                attributes = importer.getNextRecord();
                if (importer.getRecordNo() > RECORD_COUNT) {
                    assertNull(attributes);
                }
            }
        }

    }

    public void testMQImporter() {
        MQChannelType testChannelType = new MQChannelType(iPartsMQChannelTypeNames.TEST, "TestQueueOut", "TestQueueIn");
        DWFile testFile = tmpDir().getChild("BCTE.xml");
        String[] mustExists = new String[]{ "BCTE_PG" };
        String[] mustHaveData = new String[]{ "BCTE_PG" };
        String tableName = "T10RBCTE";

        iPartsKeyValueRecordReader importer = new iPartsKeyValueRecordReader(testFile, testChannelType);

        boolean result = false;
        try {
            result = importer.open();
        } catch (Exception e) {
            Logger.getLogger().throwRuntimeException(e);
        }
        assertEquals(true, result);
        if (result) {
            result = importer.getTableNames().get(0).equals(tableName);
            assertEquals(true, result);
            if (result) {
                importer.setMustExists(mustExists);
                importer.setMustHaveData(mustHaveData);
                int maxRow = importer.getRecordCount();
                assertEquals(100, maxRow);
                List<String> errors = new DwList<String>();
                Map<String, String> attributes = importer.getNextRecord();
                assertNotNull(attributes);
                while (attributes != null) {
                    int actRecord = importer.getRecordNo();
                    errors.clear();
                    result = importer.isRecordValid(attributes, errors);
                    assertEquals(true, result);
                    if (result) {
                        // jetzt Record bilden und abspeichern
                        String partNo = attributes.get("BCTE_PG");
                        partNo = partNo + "!!";
                    }
                    attributes = importer.getNextRecord();
                    if (importer.getRecordNo() > 100) {
                        assertNull(attributes);
                    }
                }
            }
        }
    }

    private Map<String, String> initMapping() {
        Map<String, String> mapping = new HashMap<String, String>();
        mapping.put(EDS_E_USNR, EtkDbConst.FIELD_M_MATNR);
        mapping.put(EDS_BEN, iPartsConst.FIELD_M_CONST_DESC);
        mapping.put(EDS_ZGS, iPartsConst.FIELD_M_IMAGESTATE);
        mapping.put(EDS_ZDAT, iPartsConst.FIELD_M_IMAGEDATE);
        mapping.put(EDS_GEWOGEN, iPartsConst.FIELD_M_WEIGHTREAL);
        mapping.put(EDS_BERECHNET, iPartsConst.FIELD_M_WEIGHTCALC);
        mapping.put(EDS_PROGNOSE, iPartsConst.FIELD_M_WEIGHTPROG);
        mapping.put(EDS_ME, iPartsConst.FIELD_M_QUANTUNIT);
        mapping.put(EDS_ET_KZ_GESAMT, iPartsConst.FIELD_M_ETKZ);
        mapping.put(EDS_BEM1, iPartsConst.FIELD_M_NOTEONE);
        mapping.put(EDS_BEM2, iPartsConst.FIELD_M_NOTETWO);
        mapping.put(EDS_WERKSTOFF, iPartsConst.FIELD_M_MATERIALFINITESTATE);
        mapping.put(EDS_HINWEISZ, iPartsConst.FIELD_M_REFSER);

        return mapping;
    }

    public void testMQImporterWithMixedTables() {
        MQChannelType testChannelType = new MQChannelType(iPartsMQChannelTypeNames.TEST, "TestQueueOut", "TestQueueIn");
        DWFile testDir = tmpDir().getChild("mixedTableFiles");
        for (int i = 0; i < testDir.listDWFiles().size(); i++) {
            DWFile testMixedTableFile = testDir.getChild("testMixedTable_" + (i + 1) + ".xml");
            iPartsKeyValueRecordReader ri = new iPartsKeyValueRecordReader(testMixedTableFile, testChannelType);
            boolean result = false;
            try {
                result = ri.open();
            } catch (Exception e) {
                Logger.getLogger().throwRuntimeException(e);
            }
            assertTrue(result);
            assertTrue(ri.isXMLMixedTable());
            assertEquals(i + 2, ri.getTableNames().size());
            if (result) {
                for (String table : ri.getTableNames()) {
                    ri.setMustExists(iPartsDIALOGImportFields.getInstance().getMustExistForTable(table));
                    ri.setMustHaveData(iPartsDIALOGImportFields.getInstance().getMustHaveForTable(table));
                    List<String> errors = new DwList<String>();
                    Map<String, String> attributes = ri.getNextRecord(table);
                    assertNotNull(attributes);
                    while (attributes != null) {
                        errors.clear();
                        result = ri.isRecordValid(attributes, errors);
                        assertTrue(result);
                        assertFalse(attributes.isEmpty());
                        attributes = ri.getNextRecord(table);
                    }
                }
            }
        }
    }

    public void testMixedTablesToListOfTables() {
        MQChannelType testChannelType = new MQChannelType(iPartsMQChannelTypeNames.TEST, "TestQueueOut", "TestQueueIn");
        DWFile testDir = tmpDir().getChild("mixedTableFiles");
        for (int i = 0; i < testDir.listDWFiles().size(); i++) {
            DWFile testMixedTableFile = testDir.getChild("testMixedTable_" + (i + 1) + ".xml");
            DwXmlFile mixedTableXML = null;
            AbstractMQMessage mqMessage = null;
            try {
                mixedTableXML = new DwXmlFile(testMixedTableFile);
                mqMessage = XMLImportExportHelper.buildMessageFromXMLFile(mixedTableXML, testChannelType, false, false);
                mqMessage.setMQChannelType(testChannelType);
            } catch (IOException | SAXException e) {
                Logger.getLogger().throwRuntimeException(e);
            }
            assertTrue(mqMessage.isOfType(iPartsXMLMixedTable.TYPE));
            iPartsXMLMixedTable mixedTable = (iPartsXMLMixedTable)mqMessage;
            assertTrue(mixedTable.isMixedTable());
            assertEquals(i + 2, mixedTable.getNumberOfTables());
            assertEquals(i + 2, mixedTable.getAsSingleTablesList().size());
            for (iPartsXMLTable table : mixedTable.getAsSingleTablesList()) {
                assertTrue(table.isOfType(iPartsXMLTable.TYPE));
                assertEquals(mixedTable.getDatasetForTableName(table.getTableName()).size(), table.getDatasets().size());
                assertEquals(iPartsXMLTable.INVALID_SCHEMAVERSION, table.getSchemaVersion());
                assertEquals(mixedTable.getOrigin(), table.getOrigin());
                assertEquals(mixedTable.getSourceExportTime(), table.getSourceExportTime());
                assertEquals(mixedTable.getTrafoTime(), table.getTrafoTime());
                assertEquals(mixedTable.getMQChannelType().getChannelName(), table.getMQChannelType().getChannelName());
                assertEquals(iPartsXMLTable.TYPE, table.getMessageType());
                assertEquals(iPartsTransferNodeTypes.TABLE, table.getNodeType());
            }
        }
    }

}