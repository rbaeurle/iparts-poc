/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.connect;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataWireHarness;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataWireHarnessList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsCacheType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsClearTextCacheEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.iPartsMainImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.AbstractSAXPushHandlerImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.handler.multihandler.AbstractMappedHandlerWithRecord;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.connect.handler.WireHarnessModuleListHandler;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.utils.EtkMultiSprache;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Importer für Leitungssatzbaukasten aus Connect
 */
public class WireHarnessDataImporter extends AbstractSAXPushHandlerImporter {

    private static final String TYPE = "WireHarnessImportType";

    private WireHarnessModuleListHandler wireHarnessModuleListHandler;
    private boolean saveToDB = true;

    public WireHarnessDataImporter(EtkProject project) {
        super(project, CONNECT_WIRE_HARNESS, null, new FilesImporterFileListType(TYPE, CONNECT_WIRE_HARNESS,
                                                                                 true, false, false,
                                                                                 new String[]{ MimeTypes.EXTENSION_XML }));
        initImporter();
    }

    private void initImporter() {
        setBufferedSave(true);
        AbstractMappedHandlerWithRecord[] dataHandlers = new AbstractMappedHandlerWithRecord[1];
        wireHarnessModuleListHandler = new WireHarnessModuleListHandler(getProject(), this);
        dataHandlers[0] = wireHarnessModuleListHandler;
        setDataHandlers(dataHandlers);
    }

    @Override
    public boolean isAutoImport() {
        return true;
    }

    @Override
    protected void clearCaches() {
        // Neben den kleinen Caches auch den großen Cache WIRE_HARNESS löschen
        iPartsPlugin.fireClearGlobalCaches(EnumSet.of(iPartsCacheType.ALL_SMALL_CACHES, iPartsCacheType.WIRE_HARNESS));
        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsClearTextCacheEvent(DictTextKindTypes.ADD_TEXT));
    }

    @Override
    protected String getType() {
        return TYPE;
    }

    @Override
    protected void postImportTask() {
        if (!saveToDB) {
            cancelImport();
        } else {
            super.postImportTask();

            if (wireHarnessModuleListHandler != null) {
                setBufferedSave(true);
                Map<String, iPartsDataWireHarnessList> harnessListMap = wireHarnessModuleListHandler.getHarnessListMap();
                Set<String> harnessNos = harnessListMap.keySet();
                if (!harnessNos.isEmpty()) {
                    Set<String> formattedHarnessNos = new HashSet<>();
                    harnessNos.forEach(num -> formattedHarnessNos.add(iPartsNumberHelper.formatPartNo(getProject(), num)));
                    getMessageLog().fireMessage(translateForLog("!!Speichere %1 Leitungssätze...", String.valueOf(formattedHarnessNos.size())),
                                                MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                    getMessageLog().fireMessage(translateForLog("!!Folgende Leitungssatzbaukästen werden gespeichert:"),
                                                MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                    getMessageLog().fireMessage(iPartsMainImportHelper.buildNumberListForLogFile(formattedHarnessNos).toString(),
                                                MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                }
                int count = 1;
                int maxCount = harnessNos.size();
                for (Map.Entry<String, iPartsDataWireHarnessList> entry : harnessListMap.entrySet()) {
                    String wireHarnessNo = entry.getKey();
                    iPartsDataWireHarnessList wireHarnessList = entry.getValue();

                    // Vor dem Schreiben den Leitungssatzbaukasten nur löschen, wenn es nicht der aus der Datenbank gelesene ist.
                    // DELETE FROM DA_WIRE_HARNESS WHERE DWH_SNR = 'XYZ';
                    if (wireHarnessList.isNew()) {
                        getProject().getDB().delete(TABLE_DA_WIRE_HARNESS, new String[]{ FIELD_DWH_SNR }, new String[]{ wireHarnessNo });
                    }
                    // ... und den neuen Leitungssatzbaukasten speichern.
                    saveToDB(wireHarnessList);
                    getMessageLog().fireProgress(count, maxCount, "", false, false);
                    count++;
                }
                getMessageLog().hideProgress();

                Map<String, String> materialStammMap = wireHarnessModuleListHandler.getMaterialStammMap();
                // Die rudimentären Materialstammdatensätze mit Quelle "Connect" anlegen, falls für die Sachnummer noch kein Materialstammdatensatz existiert.
                if (!materialStammMap.isEmpty()) {
                    count = 1;
                    maxCount = materialStammMap.size();
                    getMessageLog().fireMessage(translateForLog("!!Lege rudimentäre Materialien an...", String.valueOf(maxCount)),
                                                MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                    Set<String> createdParts = new HashSet<>();
                    for (Map.Entry<String, String> entry : materialStammMap.entrySet()) {
                        String partNo = entry.getKey();
                        String partDescr = entry.getValue();

                        iPartsPartId partId = new iPartsPartId(partNo, "");
                        EtkDataPart part = EtkDataObjectFactory.createDataPart(getProject(), partId);
                        if (!part.existsInDB()) {
                            part.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                            part.setFieldValue(FIELD_M_BESTNR, partNo, DBActionOrigin.FROM_EDIT);
                            // Quelle setzen
                            part.addSetOfEnumValueToFieldValue(FIELD_M_SOURCE, iPartsImportDataOrigin.CONNECT.getOrigin(), DBActionOrigin.FROM_EDIT);

                            // Die  (!)IMMER DEUTSCHE(!)  Beschreibung übernehmen.
                            EtkMultiSprache multiSprache = part.getFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR);
                            multiSprache.setText(Language.DE.getCode(), partDescr);
                            part.setFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR, multiSprache, DBActionOrigin.FROM_EDIT);

                            saveToDB(part);
                            createdParts.add(iPartsNumberHelper.formatPartNo(getProject(), partNo));
                        }
                        getMessageLog().fireProgress(count, maxCount, "", false, false);
                        count++;
                    }
                    getMessageLog().hideProgress();
                    getMessageLog().fireMessage(translateForLog("!!Es wurden %1 rudimentäre Materialien angelegt", String.valueOf(createdParts.size())),
                                                MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                    Set<String> formattedCreatedParts = new HashSet<>();
                    createdParts.forEach(num -> formattedCreatedParts.add(iPartsNumberHelper.formatPartNo(getProject(), num)));
                    getMessageLog().fireMessage(translateForLog("!!Folgende Materialien wurden rudimentär angelegt:"),
                                                MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                    getMessageLog().fireMessage(iPartsMainImportHelper.buildNumberListForLogFile(formattedCreatedParts).toString(),
                                                MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                }
                super.postImportTask();
                // Maps im Handler leeren damit nachfolgende Importdateien bereits importierte Datensätze nicht löschen
                wireHarnessModuleListHandler.clearMaps();
            }
        }
    }

    /**
     * Speichert die Elemente eines Leitungssatzbaukasten mit buffered Save.
     *
     * @param wireHarnessList
     */
    private void saveToDB(iPartsDataWireHarnessList wireHarnessList) {
        for (iPartsDataWireHarness dataWireHarness : wireHarnessList) {
            saveToDB(dataWireHarness);
        }
    }
}
