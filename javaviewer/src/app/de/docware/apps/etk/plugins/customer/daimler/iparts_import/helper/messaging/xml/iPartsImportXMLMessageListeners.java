/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.messaging.xml;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.AbstractXMLMessageListener;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.iPartsMQMessageManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractMQMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.dialog.iPartsXMLMixedTable;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.dialog.iPartsXMLTable;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.update.*;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.os.OsUtils;

import java.util.List;

/**
 * Verwaltung aller zentralen iParts Import {@link AbstractXMLMessageListener}s.
 */
public class iPartsImportXMLMessageListeners implements iPartsConst {

    private static iPartsImportXMLMessageListeners instance;

    private AbstractXMLMessageListener handleDIALOGImportListener;
    private AbstractXMLMessageListener handleEDSImportListener;
    private PrimusXMLMessageHelper primusXMLMessageHelper;
    private SrmXMLMessageHelper srmXMLMessageHelper;

    private iPartsImportXMLMessageListeners() {
    }

    public static iPartsImportXMLMessageListeners getInstance() {
        if (instance == null) {
            instance = new iPartsImportXMLMessageListeners();
        }
        return instance;
    }

    public void registerXMLMessageListeners(final EtkProject project, final Session session) {
        registerDIALOGListener(project, session);
        registerEDSListener(project, session);
        registerPRIMUSListener(project, session);
        registerSRMListener(project, session);
    }

    private void registerDIALOGListener(EtkProject project, Session session) {
        // XMLMessageListener für DIALOG Import
        handleDIALOGImportListener = new AbstractXMLMessageListener(session) {
            @Override
            public boolean messageReceived(AbstractMQMessage xmlMQMessage) {
                // DIALOG Importe für XMLTable
                if (xmlMQMessage.isOfType(iPartsXMLTable.TYPE)) {
                    iPartsXMLTable xmlTable = (iPartsXMLTable)xmlMQMessage;

                    if (xmlTable.isTableNameEqual(MasterDataDialogSeriesImporter.IMPORT_TABLENAME)) {                              // BRS
                        MasterDataDialogSeriesImporter seriesImporter = new MasterDataDialogSeriesImporter(project);
                        return seriesImporter.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(MasterDataDialogModelImporter.IMPORT_TABLENAME)) {                        // BMS
                        MasterDataDialogModelImporter modelImporter = new MasterDataDialogModelImporter(project);
                        return modelImporter.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(MasterDataDialogModelSeriesImporter.IMPORT_TABLENAME)) {                  // X2E
                        MasterDataDialogModelSeriesImporter modelImporter = new MasterDataDialogModelSeriesImporter(project);
                        return modelImporter.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(ModuleRelationImporter.IMPORT_TABLENAME_X6E)) {                               // X6E
                        ModuleRelationImporter modelRelImporter = new ModuleRelationImporter(project, ModuleRelationImporter.IMPORT_TABLENAME_X6E);
                        return modelRelImporter.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(HmMSmStructureImporter.IMPORT_TABLENAME)) {                               // KGVZ
                        HmMSmStructureImporter hmMSmImporter = new HmMSmStructureImporter(project);
                        return hmMSmImporter.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(PartListDataImporter.IMPORT_TABLENAME_BCTE)) {                                 // BCTE
                        PartListDataImporter partListDataImporter = new PartListDataImporter(project, PartListDataImporter.IMPORT_TABLENAME_BCTE);
                        return partListDataImporter.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(GenericPartImporter.IMPORT_TABLENAME_BCTG)) {                                 // BCTG
                        GenericPartImporter genericPartImporter = new GenericPartImporter(project);
                        return genericPartImporter.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(DIALOGPosTextImporter.IMPORT_TABLENAME)) {                                // POSX
                        DIALOGPosTextImporter importer = new DIALOGPosTextImporter(project);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(GenericInstallLocationImporter.IMPORT_TABLENAME)) {                        // POS
                        GenericInstallLocationImporter importer = new GenericInstallLocationImporter(project);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(PartListTextDataImporter.IMPORT_TABLENAME)) {                             // BCTX
                        PartListTextDataImporter importer = new PartListTextDataImporter(project);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(PartListAddDataImporter.IMPORT_TABLENAME_VBCA)) {                              // VBCA
                        PartListAddDataImporter importer = new PartListAddDataImporter(project, PartListAddDataImporter.IMPORT_TABLENAME_VBCA);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(FactoryDataImporter.IMPORT_TABLENAME_WBCT)) {                                    // WBCT
                        FactoryDataImporter importer = new FactoryDataImporter(project, FactoryDataImporter.IMPORT_TABLENAME_WBCT);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(FactoryDataImporter.IMPORT_TABLENAME_VBW)) {                                     // VBW
                        FactoryDataImporter importer = new FactoryDataImporter(project, FactoryDataImporter.IMPORT_TABLENAME_VBW);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(ColorTableImporter.IMPORT_TABLENAME)) {                                   // FTS
                        ColorTableImporter importer = new ColorTableImporter(project);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if ((xmlTable.isTableNameEqual(MasterDataDialogImporter.IMPORT_TABLENAME_TS1)) ||
                               (xmlTable.isTableNameEqual(MasterDataDialogImporter.IMPORT_TABLENAME_TS2)) ||
                               (xmlTable.isTableNameEqual(MasterDataDialogImporter.IMPORT_TABLENAME_TS6)) ||                       // Selbsterklärend
                               (xmlTable.isTableNameEqual(MasterDataDialogImporter.IMPORT_TABLENAME_VTNR)) ||
                               (xmlTable.isTableNameEqual(MasterDataDialogImporter.IMPORT_TABLENAME_GEWS))) {
                        MasterDataDialogImporter importer = new MasterDataDialogImporter(project);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(ReplacementPartsConstImporter.IMPORT_TABLENAME)) {                        // TS7
                        ReplacementPartsConstImporter importer = new ReplacementPartsConstImporter(project);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(ColorTablePartOrContentImporter.IMPORT_TABLENAME_X10E)) {                 // X10E
                        ColorTablePartOrContentImporter importer = new ColorTablePartOrContentImporter(project, ColorTablePartOrContentImporter.IMPORT_TABLENAME_X10E);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(ColorTablePartOrContentImporter.IMPORT_TABLENAME_X9E)) {                  // X9E
                        ColorTablePartOrContentImporter importer = new ColorTablePartOrContentImporter(project, ColorTablePartOrContentImporter.IMPORT_TABLENAME_X9E);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(ColorNumberImporter.IMPORT_TABLENAME)) {                                  // FNR
                        ColorNumberImporter importer = new ColorNumberImporter(project);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(ColorTableFactoryDataImporter.TABLENAME_WX10)) {                          // WX10
                        ColorTableFactoryDataImporter importer = new ColorTableFactoryDataImporter(project, ColorTableFactoryDataImporter.TABLENAME_WX10);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(ColorTableFactoryDataImporter.TABLENAME_WX9)) {                           // WX9
                        ColorTableFactoryDataImporter importer = new ColorTableFactoryDataImporter(project, ColorTableFactoryDataImporter.TABLENAME_WX9);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(ColorTableFactoryDataImporter.TABLENAME_VX10)) {                          // VX10
                        ColorTableFactoryDataImporter importer = new ColorTableFactoryDataImporter(project, ColorTableFactoryDataImporter.TABLENAME_VX10);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(ColorTableFactoryDataImporter.TABLENAME_VX9)) {                           // VX9
                        ColorTableFactoryDataImporter importer = new ColorTableFactoryDataImporter(project, ColorTableFactoryDataImporter.TABLENAME_VX9);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(ResponseDataImporter.TABLENAME_RMDA)) {                                   // RMDA
                        ResponseDataImporter importer = new ResponseDataImporter(project, ResponseDataImporter.TABLENAME_RMDA);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(ResponseDataImporter.TABLENAME_RMID)) {                                   // RMID
                        ResponseDataImporter importer = new ResponseDataImporter(project, ResponseDataImporter.TABLENAME_RMID);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(CodeMasterDataImporter.IMPORT_TABLENAME)) {                               // RES
                        CodeMasterDataImporter importer = new CodeMasterDataImporter(project);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(FootNoteMatRefImporter.IMPORT_TABLENAME)) {                               // VTFN
                        FootNoteMatRefImporter importer = new FootNoteMatRefImporter(project);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(DSRDataImporter.IMPORT_TABLENAME)) {                                      // TMK
                        DSRDataImporter importer = new DSRDataImporter(project);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(ZBVEDataImporter.IMPORT_TABLENAME)) {                                     // ZBVE
                        ZBVEDataImporter importer = new ZBVEDataImporter(project);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(VTNVDataImporter.IMPORT_TABLENAME)) {                                     // VTNV
                        VTNVDataImporter importer = new VTNVDataImporter(project);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(SeriesCodesImporter.IMPORT_TABLENAME_X4E)) {                                  // X4E
                        SeriesCodesImporter importer = new SeriesCodesImporter(project, SeriesCodesImporter.IMPORT_TABLENAME_X4E);
                        return importer.startImportFromMQMessage(xmlMQMessage);                                                    // Ereignis-Importer
                    } else if (xmlTable.isTableNameEqual(EventDataImporter.IMPORT_TABLENAME)) {                                      // EREI
                        EventDataImporter importer = new EventDataImporter(project);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(PartListDataImporter.IMPORT_TABLENAME_BRTE)) {                                   // BRTE
                        PartListDataImporter importer = new PartListDataImporter(project, PartListDataImporter.IMPORT_TABLENAME_BRTE);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(FactoryDataImporter.IMPORT_TABLENAME_WBRT)) {                                    // WBRT
                        FactoryDataImporter importer = new FactoryDataImporter(project, FactoryDataImporter.IMPORT_TABLENAME_WBRT);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(PartListAddDataImporter.IMPORT_TABLENAME_VBRT)) {                                // VBRT
                        PartListAddDataImporter importer = new PartListAddDataImporter(project, PartListAddDataImporter.IMPORT_TABLENAME_VBRT);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(ColorTablePartOrContentImporter.IMPORT_TABLENAME_Y9E)) {                         // Y9E
                        ColorTablePartOrContentImporter importer = new ColorTablePartOrContentImporter(project, ColorTablePartOrContentImporter.IMPORT_TABLENAME_Y9E);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(ColorTableFactoryDataImporter.TABLENAME_WY9)) {                                  // WY9
                        ColorTableFactoryDataImporter importer = new ColorTableFactoryDataImporter(project, ColorTableFactoryDataImporter.TABLENAME_WY9);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(SeriesCodesImporter.IMPORT_TABLENAME_Y4E)) {                                     // Y4E
                        SeriesCodesImporter importer = new SeriesCodesImporter(project, SeriesCodesImporter.IMPORT_TABLENAME_Y4E);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(ModuleRelationImporter.IMPORT_TABLENAME_Y6E)) {                                  // Y6E
                        ModuleRelationImporter importer = new ModuleRelationImporter(project, ModuleRelationImporter.IMPORT_TABLENAME_Y6E);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(KemMasterDataImporter.IMPORT_TABLENAME)) {                                       // KES
                        KemMasterDataImporter importer = new KemMasterDataImporter(project);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(FootNotePosRefImporter.IMPORT_TABLENAME)) {                                      // VBFN
                        FootNotePosRefImporter importer = new FootNotePosRefImporter(project);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(VFNDataImporter.IMPORT_TABLENAME)) {                                             // VFN
                        VFNDataImporter importer = new VFNDataImporter(project);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(SCTVDataImporter.IMPORT_TABLENAME)) {                                            // SCTV
                        SCTVDataImporter importer = new SCTVDataImporter(project);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(DialogEndMessageWorker.XML_TABLENAME)) {                                         // EndMessage
                        DialogEndMessageWorker worker = new DialogEndMessageWorker(project);
                        return worker.handleEndMessageFromMQ(xmlMQMessage);
                    } else {
                        Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.INFO, "Unkown DIALOG dataset received. Table name: " + xmlTable.getTableName());
                        xmlMQMessage.moveSavedMQXmlFile(iPartsConst.SUBDIR_UNKNOWN + OsUtils.FILESEPARATOR + xmlTable.getTableName());
                        return false;
                    }
                } else if (xmlMQMessage.isOfType(iPartsXMLMixedTable.TYPE)) {
                    iPartsXMLMixedTable xmlMixedTable = (iPartsXMLMixedTable)xmlMQMessage;
                    List<String> tableNames = xmlMixedTable.getTableNames();

                    // DIALOG-Teilestamm
                    boolean doDialogMasterDataImport = false;
                    for (String dialogMasterDataTableName : tableNames) {
                        if (dialogMasterDataTableName.equals(MasterDataDialogImporter.IMPORT_TABLENAME_TS1)
                            || dialogMasterDataTableName.equals(MasterDataDialogImporter.IMPORT_TABLENAME_TS2)
                            || dialogMasterDataTableName.equals(MasterDataDialogImporter.IMPORT_TABLENAME_TS6)
                            || dialogMasterDataTableName.equals(MasterDataDialogImporter.IMPORT_TABLENAME_VTNR)
                            || dialogMasterDataTableName.equals(MasterDataDialogImporter.IMPORT_TABLENAME_GEWS)) {
                            doDialogMasterDataImport = true;
                        } else {
                            doDialogMasterDataImport = false;
                            break;
                        }
                    }

                    if (doDialogMasterDataImport) {
                        MasterDataDialogImporter importer = new MasterDataDialogImporter(project);
                        return importer.startImportFromMQMessage(xmlMQMessage);
                    }

                    xmlMQMessage.moveSavedMQXmlFile(iPartsConst.SUBDIR_UNKNOWN + OsUtils.FILESEPARATOR + "mixedTable");
                    return false;
                }

                xmlMQMessage.moveSavedMQXmlFile(iPartsConst.SUBDIR_UNKNOWN);
                return false;
            }
        };
        iPartsMQMessageManager.getInstance(iPartsImportPlugin.XML_MESSAGE_MANAGER_NAME_IMPORT_DATA).addXMLMessageListenerForChannelTypes(handleDIALOGImportListener,
                                                                                                                                         iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_IMPORT,
                                                                                                                                         iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_DELTA_IMPORT);
    }

    private void registerEDSListener(EtkProject project, Session session) {
        handleEDSImportListener = new AbstractXMLMessageListener(session) {
            @Override
            public boolean messageReceived(AbstractMQMessage xmlMQMessage) {
                if (xmlMQMessage.isOfType(iPartsXMLTable.TYPE)) {
                    iPartsXMLTable xmlTable = (iPartsXMLTable)xmlMQMessage;

                    if (xmlTable.isTableNameEqual(EDSModelMasterContentUpdateImporter.IMPORT_TABLENAME)) {                               // B2I
                        EDSModelMasterContentUpdateImporter modelContentImporter = new EDSModelMasterContentUpdateImporter(project);
                        return modelContentImporter.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(EDSModelUpdateImporter.IMPORT_TABLENAME)) {                                     // BM
                        EDSModelUpdateImporter modelUpdatingImporter = new EDSModelUpdateImporter(project);
                        return modelUpdatingImporter.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(EDSModelGroupUpdateImporter.IMPORT_TABLENAME)) {                                // BMAG
                        EDSModelGroupUpdateImporter modelGroupUpdateImporter = new EDSModelGroupUpdateImporter(project);
                        return modelGroupUpdateImporter.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(EDSModelScopeUpdateImporter.IMPORT_TABLENAME)) {                                // UMF
                        EDSModelScopeUpdateImporter modelGroupUpdateImporter = new EDSModelScopeUpdateImporter(project);
                        return modelGroupUpdateImporter.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(EDSSaaMasterDataUpdateImporter.IMPORT_TABLENAME)) {                             // SAAE
                        EDSSaaMasterDataUpdateImporter edsSaaMasterDataUpdateImporter = new EDSSaaMasterDataUpdateImporter(project);
                        return edsSaaMasterDataUpdateImporter.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(BCSMasterDataSaaUpdateImporter.IMPORT_TABLENAME)) {                             // SAA
                        BCSMasterDataSaaUpdateImporter bcsSaaMasterDataUpdateImporter = new BCSMasterDataSaaUpdateImporter(project);
                        return bcsSaaMasterDataUpdateImporter.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(BOMConstructionKitContentUpdateImporter.IMPORT_TABLENAME)) {                    // BK
                        BOMConstructionKitContentUpdateImporter constructionKitContentUpdateImporter = new BOMConstructionKitContentUpdateImporter(project);
                        return constructionKitContentUpdateImporter.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(BOMConstructionKitTextUpdateImporter.IMPORT_TABLENAME)) {                    // BK
                        BOMConstructionKitTextUpdateImporter constructionKitTextUpdateImporter = new BOMConstructionKitTextUpdateImporter(project);
                        return constructionKitTextUpdateImporter.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(BOMPartMasterDataUpdateImporter.IMPORT_TABLENAME)) {                            // TEIL
                        // Für die BOM-DB T43RTEIL Tabelle haben wir zwei Importer, die jeweils bestimmte Daten importieren.
                        // Hierfür muss die Importdatei an beide Importer weitergegeben werden.
                        BOMPartMasterDataUpdateImporter partMasterDataUpdateImporter = new BOMPartMasterDataUpdateImporter(project);
                        boolean masterDataUpdateResult = partMasterDataUpdateImporter.startImportFromMQMessage(xmlMQMessage);
                        BOMPartHistoryUpdateImporter partHistoryUpdateImporter = new BOMPartHistoryUpdateImporter(project);
                        return masterDataUpdateResult && partHistoryUpdateImporter.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(BOMSparePartSignsUpdateImporter.IMPORT_TABLENAME)) {                             // TEID
                        BOMSparePartSignsUpdateImporter bomSparePartSignsUpdateImporter = new BOMSparePartSignsUpdateImporter(project);
                        return bomSparePartSignsUpdateImporter.startImportFromMQMessage(xmlMQMessage);
                    } else if (xmlTable.isTableNameEqual(EDSMaterialRemarksUpdateImporter.IMPORT_TABLENAME)) {                             // TEIE
                        EDSMaterialRemarksUpdateImporter edsMaterialRemarksUpdateImporter = new EDSMaterialRemarksUpdateImporter(project);
                        return edsMaterialRemarksUpdateImporter.startImportFromMQMessage(xmlMQMessage);
                    } else {
                        Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.INFO, "Unkown EDS dataset received. Table name: " + xmlTable.getTableName());
                        xmlMQMessage.moveSavedMQXmlFile(iPartsConst.SUBDIR_UNKNOWN + OsUtils.FILESEPARATOR + xmlTable.getTableName());
                        return false;
                    }
                }

                xmlMQMessage.moveSavedMQXmlFile(iPartsConst.SUBDIR_UNKNOWN);
                return false;
            }
        };
        iPartsMQMessageManager.getInstance(iPartsImportPlugin.XML_MESSAGE_MANAGER_NAME_IMPORT_DATA).addXMLMessageListenerForChannelTypes(handleEDSImportListener,
                                                                                                                                         iPartsImportPlugin.MQ_CHANNEL_TYPE_EDS_IMPORT);
    }

    private void registerPRIMUSListener(EtkProject project, Session session) {
        if (primusXMLMessageHelper == null) {
            primusXMLMessageHelper = new PrimusXMLMessageHelper();
        }
        primusXMLMessageHelper.registerListener(project, session);
    }

    private void registerSRMListener(EtkProject project, Session session) {
        if (srmXMLMessageHelper == null) {
            srmXMLMessageHelper = new SrmXMLMessageHelper();
        }
        srmXMLMessageHelper.registerListener(project, session);
    }

    public void deregisterXMLMessageListeners() {
        if (handleDIALOGImportListener != null) {
            iPartsMQMessageManager.getInstance(iPartsImportPlugin.XML_MESSAGE_MANAGER_NAME_IMPORT_DATA).removeXMLMessageListenerForChannelTypes(getInstance().handleDIALOGImportListener,
                                                                                                                                                iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_IMPORT,
                                                                                                                                                iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_DELTA_IMPORT);
            handleDIALOGImportListener = null;
        }
        if (handleEDSImportListener != null) {
            iPartsMQMessageManager.getInstance(iPartsImportPlugin.XML_MESSAGE_MANAGER_NAME_IMPORT_DATA).removeXMLMessageListenerForChannelTypes(getInstance().handleEDSImportListener,
                                                                                                                                                iPartsImportPlugin.MQ_CHANNEL_TYPE_EDS_IMPORT);
            handleEDSImportListener = null;
        }
        if (primusXMLMessageHelper != null) {
            primusXMLMessageHelper.deregisterListener();
        }
        if (srmXMLMessageHelper != null) {
            srmXMLMessageHelper.deregisterListener();
        }

    }
}