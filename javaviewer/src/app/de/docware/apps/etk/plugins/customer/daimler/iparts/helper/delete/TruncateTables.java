/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper.delete;

import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.misc.EtkEndpointHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.useradmin.EtkUserAdminDbActions;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ImportExportLogHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.apps.etk.viewer.JavaViewerApplication;
import de.docware.framework.combimodules.useradmin.db.UserAdminHistoryDbObject;
import de.docware.framework.combimodules.useradmin.db.UserOrganisationsDbObject;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.modules.gui.session.SessionManager;
import de.docware.framework.modules.gui.session.SessionType;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TruncateTables {

    public enum TruncateStyle {
        DELETE_PRODUCTS("!!Produkte löschen"),
        PARTIAL_TRUNCATE_TABLES("!!Tabelleninhalte partiell löschen"),
        TRUNCATE_TABLES("!!Tabellen komplett leeren"),
        CLEAN_USER_DATA("!!Veraltete Benutzerdaten löschen"),
        PURGE_ALL("!!Komplettes Bereinigen aller Datenbankinhalte");

        private String name;

        TruncateStyle(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public enum TargetCompany {MBAG, DTAG}

    private static final String[] TABLES_TO_TRUNCATE = new String[]{ iPartsConst.TABLE_CUSTPROP,
                                                                     iPartsConst.TABLE_DA_ACCESSORY_CODES,
                                                                     iPartsConst.TABLE_DA_AGG_PART_CODES,
                                                                     iPartsConst.TABLE_DA_AS_CODES,
                                                                     iPartsConst.TABLE_DA_BAD_CODE,
                                                                     iPartsConst.TABLE_DA_CODE_MAPPING,
                                                                     iPartsConst.TABLE_DA_CONFIRM_CHANGES,
                                                                     iPartsConst.TABLE_DA_CONST_KIT_CONTENT,
                                                                     iPartsConst.TABLE_DA_COUNTRY_INVALID_PARTS,
                                                                     iPartsConst.TABLE_DA_COUNTRY_VALID_SERIES,
                                                                     iPartsConst.TABLE_DA_DIALOG,
                                                                     iPartsConst.TABLE_DA_DIALOG_ADD_DATA,
                                                                     iPartsConst.TABLE_DA_DIALOG_CHANGES,
                                                                     iPartsConst.TABLE_DA_DIALOG_DSR,
                                                                     iPartsConst.TABLE_DA_DIALOG_PARTLIST_TEXT,
                                                                     iPartsConst.TABLE_DA_DIALOG_POS_TEXT,
                                                                     iPartsConst.TABLE_DA_DICT_TRANS_JOB,
                                                                     iPartsConst.TABLE_DA_DICT_TRANS_JOB_HISTORY,
                                                                     iPartsConst.TABLE_DA_ERROR_LOCATION,
                                                                     iPartsConst.TABLE_DA_EXPORT_CONTENT,
                                                                     iPartsConst.TABLE_DA_EXPORT_REQUEST,
                                                                     iPartsConst.TABLE_DA_FN_POS,
                                                                     iPartsConst.TABLE_DA_GENERIC_INSTALL_LOCATION,
                                                                     iPartsConst.TABLE_DA_GENERIC_PART,
                                                                     iPartsConst.TABLE_DA_GENVO_PAIRING,
                                                                     iPartsConst.TABLE_DA_GENVO_SUPP_TEXT,
                                                                     iPartsConst.TABLE_DA_HMMSM,
                                                                     iPartsConst.TABLE_DA_HMMSM_KGTU,
                                                                     iPartsConst.TABLE_DA_HMMSMDESC,
                                                                     iPartsConst.TABLE_DA_INCLUDE_CONST_MAT,
                                                                     iPartsConst.TABLE_DA_KEM_MASTERDATA,
                                                                     iPartsConst.TABLE_DA_MESSAGE,
                                                                     iPartsConst.TABLE_DA_MESSAGE_TO,
                                                                     iPartsConst.TABLE_DA_MODEL_BUILDING_CODE,
                                                                     iPartsConst.TABLE_DA_MODEL_DATA,
                                                                     iPartsConst.TABLE_DA_MODEL_OIL,
                                                                     iPartsConst.TABLE_DA_MODEL_OIL_QUANTITY,
                                                                     iPartsConst.TABLE_DA_MODEL_PROPERTIES,
                                                                     iPartsConst.TABLE_DA_OMITTED_PARTS,
                                                                     iPartsConst.TABLE_DA_PIC_REFERENCE,
                                                                     iPartsConst.TABLE_DA_PSEUDO_PEM_DATE,
                                                                     iPartsConst.TABLE_DA_PSK_PRODUCT_VARIANTS,
                                                                     iPartsConst.TABLE_DA_REPLACE_CONST_MAT,
                                                                     iPartsConst.TABLE_DA_REPLACE_CONST_PART,
                                                                     iPartsConst.TABLE_DA_REPORT_CONST_NODES,
                                                                     iPartsConst.TABLE_DA_SERIES,
                                                                     iPartsConst.TABLE_DA_SERIES_CODES,
                                                                     iPartsConst.TABLE_DA_SERIES_EVENTS,
                                                                     iPartsConst.TABLE_DA_SERIES_SOP,
                                                                     iPartsConst.TABLE_DA_SPK_MAPPING,
                                                                     iPartsConst.TABLE_DA_SPRING_MAPPING,
                                                                     iPartsConst.TABLE_DA_SUPPLIER_PARTNO_MAPPING,
                                                                     iPartsConst.TABLE_DA_TOP_TUS,
                                                                     iPartsConst.TABLE_DA_VEHICLE_DATACARD_CODES,
                                                                     iPartsConst.TABLE_DA_VS2US_RELATION,
                                                                     iPartsConst.TABLE_DA_WIRE_HARNESS };
    private static final Map<String, String> PARTIAL_DELETE = new LinkedHashMap<>();
    private static final Map<String, String> CLEAN_USER_DATA = new LinkedHashMap<>();

    static {
        // PARTIAL_DELETE
        PARTIAL_DELETE.put(iPartsConst.TABLE_MAT,
                           "delete from MAT where not M_MATNR = M_BASE_MATNR and exists (select DD_PARTNO from DA_DIALOG where M_MATNR = DD_PARTNO) and not exists (select K_MATNR from KATALOG where M_MATNR = K_MATNR) and not exists (select DCK_SNR from DA_EDS_CONST_KIT where M_MATNR = DCK_SNR) and not exists (select DCK_SUB_SNR from DA_EDS_CONST_KIT where M_MATNR = DCK_SUB_SNR) and not exists (select DSM_SNR from DA_STRUCTURE_MBS where M_MATNR = DSM_SNR) and not exists (select DSM_SUB_SNR from DA_STRUCTURE_MBS where M_MATNR = DSM_SUB_SNR) and not exists (select PRP_SUCCESSOR_PARTNO from DA_PRIMUS_REPLACE_PART where M_MATNR = PRP_SUCCESSOR_PARTNO) and not exists (select PIP_INCLUDE_PART_NO from DA_PRIMUS_INCLUDE_PART where M_MATNR = PIP_INCLUDE_PART_NO) and not exists (select DRP_REPLACE_MATNR from DA_REPLACE_PART where M_MATNR = DRP_REPLACE_MATNR) and not exists (select DIP_INCLUDE_MATNR from DA_INCLUDE_PART where M_MATNR = DIP_INCLUDE_MATNR);\n"
                           + "delete from MAT a where (a.M_AS_ES_1 <> '' or a.M_AS_ES_2 <> '') and not exists (select b.M_MATNR from mat b where a.M_BASE_MATNR = b.M_MATNR);");
        PARTIAL_DELETE.put(iPartsConst.TABLE_SPRACHE,
                           "delete from SPRACHE where S_FELD in ('DA_COLORTABLE_DATA.DCTD_BEM', 'DA_DIALOG_ADD_DATA.DAD_ADD_TEXT', 'DA_DIALOG_ADD_DATA.DAD_TEXT_NEUTRAL', 'DA_DIALOG_DSR.DSR_MK_TEXT', 'DA_DIALOG_DSR.DSR_MK1', 'DA_DIALOG_DSR.DSR_MK3', 'DA_DIALOG_DSR.DSR_MK6', 'DA_DIALOG_DSR.DSR_MK7', 'DA_DIALOG_PARTLIST_TEXT.DD_PLT_TEXT', 'DA_DIALOG_POS_TEXT.DD_POS_TEXTNR', 'DA_GENVO_SUPP_TEXT.DA_GENVO_DESCR', 'DA_PSK_PRODUCT_VARIANTS.DPPV_NAME1', 'DA_PSK_PRODUCT_VARIANTS.DPPV_NAME2', 'DA_SPK_MAPPING.SPKM_LANG_AS', 'DA_SPK_MAPPING.SPKM_LANG_E') or S_TEXTID in (select distinct DA_DICT_META_TEXTID from DA_DICT_META where DA_DICT_META_SOURCE in ('IPARTS-MB', 'IPARTS-SPK', 'IPARTS-GENVO', 'Connect'));");
        PARTIAL_DELETE.put(iPartsConst.TABLE_DA_CODE,
                           "delete from DA_CODE where DC_PGRP = 'C' or DC_PGRP = 'F' or DC_PGRP = 'P' or DC_PGRP = 'G' or DC_PGRP = 'T' or (DC_SOURCE = 'PROVAL' and not (DC_SERIES_NO in ('C956', 'C963', 'C964', 'C965', 'C967', 'C969', 'C983')));");
        PARTIAL_DELETE.put(iPartsConst.TABLE_DA_COLORTABLE_CONTENT,
                           "delete from DA_COLORTABLE_CONTENT where DCTC_TABLE_ID like 'QFT%' and not DCTC_TABLE_ID like 'QFT390%';");
        PARTIAL_DELETE.put(iPartsConst.TABLE_DA_COLORTABLE_DATA,
                           "delete from DA_COLORTABLE_DATA where DCTD_TABLE_ID like 'QFT%' and not DCTD_TABLE_ID like 'QFT390%';");
        PARTIAL_DELETE.put(iPartsConst.TABLE_DA_COLORTABLE_PART,
                           "delete from DA_COLORTABLE_PART where DCTP_TABLE_ID like 'QFT%' and not DCTP_TABLE_ID like 'QFT390%';");
        PARTIAL_DELETE.put(iPartsConst.TABLE_DA_COLORTABLE_FACTORY,
                           "delete from DA_COLORTABLE_FACTORY where DCCF_TABLE_ID not like 'QFT390%';");
        PARTIAL_DELETE.put(iPartsConst.TABLE_DA_DICT_META,
                           "delete from DA_DICT_META where DA_DICT_META_SOURCE = 'IPARTS-MB' or DA_DICT_META_SOURCE = 'IPARTS-GENVO' or DA_DICT_META_SOURCE = 'Connect' or DA_DICT_META_SOURCE = 'IPARTS-SPK';");
        PARTIAL_DELETE.put(iPartsConst.TABLE_DA_DICT_SPRACHE,
                           "delete from DA_DICT_SPRACHE where not exists (select DA_DICT_META_TEXTID from DA_DICT_META where DA_DICT_META_TEXTID = DA_DICT_SPRACHE_TEXTID);");
        PARTIAL_DELETE.put(iPartsConst.TABLE_DA_FACTORY_DATA,
                           "delete from DA_FACTORY_DATA where ((DFD_DATA_ID = 'BCTP' or DFD_DATA_ID = 'VBW' ) and not exists (select K_VARI from KATALOG where K_SOURCE_GUID = DFD_GUID)) or ((DFD_DATA_ID = '' and length(DFD_GUID) > 6) and not exists (select K_VARI from KATALOG where K_VARI = substr(DFD_GUID, 1, length(DFD_GUID) - 6)));");
        PARTIAL_DELETE.put(iPartsConst.TABLE_DA_PIC_TO_ATTACHMENT,
                           "delete from DA_PIC_TO_ATTACHMENT where not exists (select DA_PTA_PICORDER from DA_PICORDER_USAGE where DA_PTA_PICORDER = POU_ORDER_GUID);");
        PARTIAL_DELETE.put(iPartsConst.TABLE_DA_PICORDER,
                           "delete from DA_PICORDER where not exists (select PO_ORDER_GUID from DA_PICORDER_USAGE where PO_ORDER_GUID = POU_ORDER_GUID);");
        PARTIAL_DELETE.put(iPartsConst.TABLE_DA_PICORDER_ATTACHMENTS,
                           "delete from DA_PICORDER_ATTACHMENTS where not exists (select DA_PTA_PICORDER from DA_PIC_TO_ATTACHMENT where DPA_GUID = DA_PTA_ATTACHMENT);");
        PARTIAL_DELETE.put(iPartsConst.TABLE_DA_PICORDER_MODULES,
                           "delete from DA_PICORDER_MODULES where not exists (select POM_ORDER_GUID from DA_PICORDER_USAGE where POM_ORDER_GUID = POU_ORDER_GUID);");
        PARTIAL_DELETE.put(iPartsConst.TABLE_DA_PICORDER_PARTS,
                           "delete from DA_PICORDER_PARTS where not exists (select PPA_ORDER_GUID from DA_PICORDER_USAGE where PPA_ORDER_GUID = POU_ORDER_GUID);");
        PARTIAL_DELETE.put(iPartsConst.TABLE_DA_PICORDER_PICTURES,
                           "delete from DA_PICORDER_PICTURES where not exists (select POP_ORDER_GUID from DA_PICORDER_USAGE where POP_ORDER_GUID = POU_ORDER_GUID);");
        PARTIAL_DELETE.put(iPartsConst.TABLE_DA_RESPONSE_DATA, "drop view if exists TEMP_DFD_PEMA;\n" +
                                                               "drop view if exists TEMP_DFD_PEMB;\n" +
                                                               "drop view if exists TEMP_DCCF_PEMA;\n" +
                                                               "drop view if exists TEMP_DCCF_PEMB;\n" +
                                                               "create view TEMP_DFD_PEMA as select distinct DFD_PEMA, DFD_FACTORY from DA_FACTORY_DATA;\n" +
                                                               "create view TEMP_DFD_PEMB as select distinct DFD_PEMB, DFD_FACTORY from DA_FACTORY_DATA;\n" +
                                                               "create view TEMP_DCCF_PEMA as select distinct DCCF_PEMA, DCCF_FACTORY from DA_COLORTABLE_FACTORY;\n" +
                                                               "create view TEMP_DCCF_PEMB as select distinct DCCF_PEMB, DCCF_FACTORY from DA_COLORTABLE_FACTORY;\n" +
                                                               "delete from DA_RESPONSE_DATA where (DRD_SOURCE != 'ELDAS' and DRD_SOURCE != 'EPC' and DRD_SOURCE != 'IPARTS') and not exists (select DFD_FACTORY from TEMP_DFD_PEMA where DRD_PEM = DFD_PEMA and DRD_FACTORY = DFD_FACTORY) and not exists (select DFD_FACTORY from TEMP_DFD_PEMB where DRD_PEM = DFD_PEMB and DRD_FACTORY = DFD_FACTORY) and not exists (select DCCF_FACTORY from TEMP_DCCF_PEMA where DRD_PEM = DCCF_PEMA and DRD_FACTORY = DCCF_FACTORY) and not exists (select DCCF_FACTORY from TEMP_DCCF_PEMB where DRD_PEM = DCCF_PEMB and DRD_FACTORY = DCCF_FACTORY);\n" +
                                                               "drop view if exists TEMP_DFD_PEMA;\n" +
                                                               "drop view if exists TEMP_DFD_PEMB;\n" +
                                                               "drop view if exists TEMP_DCCF_PEMA;\n" +
                                                               "drop view if exists TEMP_DCCF_PEMB;");
        PARTIAL_DELETE.put(iPartsConst.TABLE_DA_RESPONSE_SPIKES,
                           "delete from DA_RESPONSE_SPIKES where not exists (select DRD_PEM from DA_RESPONSE_DATA where DRD_PEM = DRS_PEM);");
        PARTIAL_DELETE.put(iPartsConst.TABLE_POOL,
                           "delete from POOL where P_VALIDITY_SCOPE = 'IPARTS-MB';");
        PARTIAL_DELETE.put(iPartsConst.TABLE_POOLENTRY,
                           "delete from POOLENTRY where not exists (select P_IMAGES from POOL where P_IMAGES = PE_IMAGES);");
        PARTIAL_DELETE.put(iPartsConst.TABLE_LINKS,
                           "delete from LINKS where not exists (select P_IMAGES from POOL where P_IMAGES = L_IMAGES);");
        PARTIAL_DELETE.put(iPartsConst.TABLE_DA_FN_MAT_REF,
                           "delete from DA_FN_MAT_REF where not exists (select M_MATNR from MAT where M_MATNR = DFNM_MATNR);");
        PARTIAL_DELETE.put(iPartsConst.TABLE_DA_MODEL,
                           "delete from DA_MODEL where (DM_SOURCE = 'DIALOG' or DM_PRODUCT_GRP = 'F' or DM_PRODUCT_GRP = 'P' or DM_PRODUCT_GRP = 'G' or DM_PRODUCT_GRP = 'T') and not exists (select dpm_model_no from da_product_models where (substr(DPM_MODEL_NO, 1, 4) = substr(DM_MODEL_NO, 1, 4)));");
        PARTIAL_DELETE.put(iPartsConst.TABLE_DA_MODELS_AGGS,
                           "delete from DA_MODELS_AGGS where not exists (select DM_MODEL_NO from DA_MODEL where DM_MODEL_NO = DMA_MODEL_NO or DM_MODEL_NO = DMA_AGGREGATE_NO);");
        PARTIAL_DELETE.put(iPartsConst.TABLE_DA_PEM_MASTERDATA,
                           "delete from DA_PEM_MASTERDATA where DPM_SOURCE = 'IPARTS-MB';");
        PARTIAL_DELETE.put(iPartsConst.TABLE_DA_INTERNAL_TEXT,
                           "delete from DA_INTERNAL_TEXT where DIT_DO_TYPE = 'DA_iPartsDialogId' or (DIT_DO_TYPE = 'PartListEntryId' and not exists (select K_VARI from katalog where K_VARI = substr(DIT_DO_ID, 1, length(DIT_DO_ID) - 7)));");
        PARTIAL_DELETE.put(iPartsConst.TABLE_DA_AUTHOR_ORDER,
                           "delete from DA_AUTHOR_ORDER where DAO_BST_ID in (select DWO_BST_ID from DA_WORKORDER where DWO_BRANCH = 'PKW');");
        PARTIAL_DELETE.put(iPartsConst.TABLE_DA_AO_HISTORY,
                           "delete from DA_AO_HISTORY where not exists (select DAO_GUID from DA_AUTHOR_ORDER where DAO_GUID = DAH_GUID);");
        PARTIAL_DELETE.put(iPartsConst.TABLE_DA_CHANGE_SET,
                           "delete from DA_CHANGE_SET where DCS_SOURCE = 'AUTHOR_ORDER' and not exists (select DAO_GUID from DA_AUTHOR_ORDER where DAO_CHANGE_SET_ID = DCS_GUID);");
        PARTIAL_DELETE.put(iPartsConst.TABLE_DA_CHANGE_SET_ENTRY,
                           "delete from DA_CHANGE_SET_ENTRY where not exists (select DCS_GUID from DA_CHANGE_SET where DCE_GUID = DCS_GUID);");
        PARTIAL_DELETE.put(iPartsConst.TABLE_DA_WORKORDER,
                           "delete from DA_WORKORDER where DWO_BRANCH = 'PKW'; ");

        // CLEAN_USER_DATA
        CLEAN_USER_DATA.put(iPartsConst.TABLE_FAVORITES,
                            "delete from FAVORITES where not exists (select U_NAME from UA_USERS where U_NAME = F_USER_ID);");
        CLEAN_USER_DATA.put(iPartsConst.TABLE_USERSETTINGS,
                            "delete from USERSETTINGS where not exists (select U_NAME from UA_USERS where U_NAME = US_USERID);");
        String userAdminHistoryTableName = (EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + UserAdminHistoryDbObject.TABLE_NAME).toUpperCase();
        CLEAN_USER_DATA.put(userAdminHistoryTableName,
                            "delete from " + userAdminHistoryTableName + " where not exists (select U_ID from UA_USERS where U_ID = UAH_USER_ID);");
        String userOrganisationsTableName = (EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + UserOrganisationsDbObject.TABLE_NAME).toUpperCase();
        CLEAN_USER_DATA.put(userOrganisationsTableName,
                            "delete from " + userOrganisationsTableName + " where not exists (select U_ID from UA_USERS where U_ID = UO_USER_ID);");
    }

    public static String getTimeDurationString(long startTime, String language) {
        long deleteDuration = System.currentTimeMillis() - startTime;
        return DateUtils.formatTimeDurationString(deleteDuration, false, false, language);
    }

    private TargetCompany targetCompany;
    private Session sessionForGUI;
    private Session sessionForTruncate;
    private EtkProject projectForTruncate;
    private DeleteTableMessageInterface msgInterface;
    private EtkMessageLogForm progressForm;

    public TruncateTables(TargetCompany targetCompany, EtkProject project) {
        if (targetCompany != TargetCompany.DTAG) { // AKtuell nur DTAG erlaubt
            MessageDialog.showError(TranslationHandler.translate("!!Ungültige Firma für die Bereinigung der Datenbank: %1",
                                                                 targetCompany.name()));
            return;
        }

        this.targetCompany = targetCompany;
        this.sessionForGUI = Session.get();

        sessionForTruncate = EtkEndpointHelper.createSession(SessionType.ENDPOINT, false); // Typ ENDPOINT, weil diese Hintergrund-Session keine GUI hat

        // Attribute der Original-Session übernehmen außer TranslationHandler und MainConnector
        sessionForTruncate.assignAttributes(sessionForGUI, Constants.SESSION_KEY_DEFAULT_TRANSLATION_HANDLER, JavaViewerApplication.SESSION_KEY_MAIN_CONNECTOR);

        projectForTruncate = EtkEndpointHelper.createProjectAndStoreItInEndpointSession(sessionForTruncate);
        if (projectForTruncate == null) {
            SessionManager.getInstance().destroySession(sessionForTruncate);
            MessageDialog.showError("!!Fehler bei der Initialisierung für das Bereinigen der Datenbank im Hintergrund.");
            return;
        }

        projectForTruncate.getConfig().setCurrentViewerLanguage(project.getViewerLanguage());
        projectForTruncate.getConfig().setCurrentDatabaseLanguage(project.getDBLanguage());
    }

    public boolean doDeleteProducts() {
        return doTruncateTablesWithGUI(TruncateStyle.DELETE_PRODUCTS);
    }

    public boolean doTruncateTables() {
        return doTruncateTablesWithGUI(TruncateStyle.TRUNCATE_TABLES);
    }

    public boolean doTruncatePartialTables() {
        return doTruncateTablesWithGUI(TruncateStyle.PARTIAL_TRUNCATE_TABLES);
    }

    public boolean doCleanUserData() {
        return doTruncateTablesWithGUI(TruncateStyle.CLEAN_USER_DATA);
    }

    public boolean doPurgeAll() {
        return doTruncateTablesWithGUI(TruncateStyle.PURGE_ALL);
    }

    public boolean doTruncateTablesWithGUI(TruncateStyle style) {
        String title = TranslationHandler.translate("!!Datenbankinhalte bereinigen für %1", targetCompany.name());
        if (MessageDialog.showYesNo(TranslationHandler.translate("!!Wirklich die Datenbankinhalte bereinigen im Modus \"%1\"?",
                                                                 TranslationHandler.translate(style.getName())) + "\n\n" +
                                    TranslationHandler.translate("!!Aktive Datenbank: %1", projectForTruncate.getDB().getDBConnectionInfoString()),
                                    title) != ModalResult.YES) {
            return false;
        }

        progressForm = new EtkMessageLogForm(title, "!!Fortschritt", null) {
            @Override
            public void closeWindow(ModalResult modalResult, boolean waitForWorkingThread) {
                // Nie auf das Beenden vom Arbeits-Thread warten und diesen aus der GUI-Session entfernen, damit die GUI-Session
                // auch beendet werden kann, obwohl die Bereinigung (in der sessionForTruncate mit waitFinished() in invokeThreadSafeInSessionThread())
                // noch läuft
                EtkMessageLogForm progressFormLocal = progressForm;
                if (progressFormLocal != null) {
                    FrameworkThread workerThread = progressFormLocal.getWorkerThread();
                    if (workerThread != null) {
                        SessionManager.getInstance().deregisterThreadForSession(sessionForGUI, workerThread.getRealThread());
                        sessionForGUI.removeChildThread(workerThread.getRealThread().getId());
                    }
                }
                super.closeWindow(modalResult, false);
            }
        };

        // Button für die Ausführung im Hintergrund
        GuiButtonOnPanel runInBackgroundButton = progressForm.getButtonPanel().addCustomButton("!!Im Hintergrund ausführen",
                                                                                               ModalResult.OK);
        runInBackgroundButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                EtkMessageLogForm progressFormLocal = progressForm;
                if (progressFormLocal != null) {
                    progressFormLocal.closeWindow(ModalResult.OK);
                }
            }
        });

        progressForm.setMessagesTitle("");
        progressForm.getGui().setSize(800, 600);
        VarParam<Boolean> result = new VarParam<>(false);
        progressForm.showModal(thread -> sessionForTruncate.invokeThreadSafeInSessionThread(() -> {
            Thread truncateThread = Thread.currentThread();
            EtkMessageLogForm.CancelListener cancelListener = () -> truncateThread.interrupt(); // CancelListener als Variable halten, damit er nicht entfernt wird (WeakReference!)
            progressForm.addCancelListener(cancelListener);
            result.setValue(doTruncateTablesWithoutGUI(style));
            if (isProgressFormValid()) {
                sessionForGUI.invokeThreadSafeInSessionThread(() -> runInBackgroundButton.setVisible(false));
            }
        }));
        progressForm = null;
        return result.getValue();
    }

    public boolean doTruncateTablesWithoutGUI(TruncateStyle style) {
        try {
            VarParam<Integer> warningCount = new VarParam<>(0);
            VarParam<Integer> errorCount = new VarParam<>(0);
            // Gemeinsamer LogHelper für alle Handler
            ImportExportLogHelper logHelper = ImportExportLogHelper.createLogHelperWithRunningJob(TranslationHandler.translate("!!Datenbankinhalte bereinigen für %1",
                                                                                                                               targetCompany.name()));
            setMsgInterface(logHelper);
            msgInterface.addMessage("!!Datenbankinhalte bereinigen für %1 im Modus \"%2\"", targetCompany.name(), logHelper.translateForLog(style.getName()));
            addNewLine(logHelper);
            boolean noErrors = true;
            long startTime = System.currentTimeMillis();

            if (style == TruncateStyle.PURGE_ALL) {
                addStartMsg(logHelper, TruncateStyle.PURGE_ALL);
            }

            if ((style == TruncateStyle.DELETE_PRODUCTS) || (style == TruncateStyle.PURGE_ALL)) {
                addStartMsg(logHelper, TruncateStyle.DELETE_PRODUCTS);
                // Produkte löschen
                noErrors = deleteCarProducts(warningCount, errorCount);
                addNewLine(logHelper);
            }

            if (noErrors && !Thread.currentThread().isInterrupted()) {
                if ((style == TruncateStyle.PARTIAL_TRUNCATE_TABLES) || (style == TruncateStyle.PURGE_ALL)) {
                    addStartMsg(logHelper, TruncateStyle.PARTIAL_TRUNCATE_TABLES);
                    // Tabellen partiell löschen
                    noErrors = partialDelete(PARTIAL_DELETE, "!!Laufzeit zum partiellen Löschen von Tabelleninhalten für %1 Tabellen: %2",
                                             warningCount, errorCount);
                    addNewLine(logHelper);
                }
            }

            if (noErrors && !Thread.currentThread().isInterrupted()) {
                if ((style == TruncateStyle.TRUNCATE_TABLES) || (style == TruncateStyle.PURGE_ALL)) {
                    addStartMsg(logHelper, TruncateStyle.TRUNCATE_TABLES);
                    // Tabellen komplett leeren
                    noErrors = truncateTables(TABLES_TO_TRUNCATE, warningCount, errorCount);
                    addNewLine(logHelper);
                }
            }

            if (noErrors && !Thread.currentThread().isInterrupted()) {
                if ((style == TruncateStyle.CLEAN_USER_DATA) || (style == TruncateStyle.PURGE_ALL)) {
                    addStartMsg(logHelper, TruncateStyle.CLEAN_USER_DATA);
                    // Veraltete Benutzerdaten löschen
                    noErrors = partialDelete(CLEAN_USER_DATA, "!!Laufzeit zum Löschen von veralteten Benutzerdaten in %1 Tabellen: %2",
                                             warningCount, errorCount);
                    addNewLine(logHelper);
                }
            }

            // Ende Meldung ausgeben
            if ((errorCount.getValue() > 0) || Thread.currentThread().isInterrupted()) {
                noErrors = false;
            }
            String timeDurationString = getTimeDurationString(startTime, logHelper.getLogLanguage());
            if (noErrors) {
                msgInterface.addMessage(buildEndMsg(logHelper, logHelper.translateForLog("!!Bereinigen der Datenbank in %1 beendet", timeDurationString),
                                                    warningCount.getValue(), errorCount.getValue()));
            } else {
                if (Thread.currentThread().isInterrupted()) {
                    msgInterface.addError("!!Abbruch durch Benutzer");
                }
                msgInterface.addMessage(buildEndMsg(logHelper, logHelper.translateForLog("!!Bereinigung der Datenbank nach %1 abgebrochen", timeDurationString),
                                                    warningCount.getValue(), errorCount.getValue()));
            }

            // LogHelper schließen
            if (noErrors) {
                iPartsJobsManager.getInstance().jobProcessed(logHelper.getLogFile());
                CacheHelper.invalidateCaches();
            } else {
                iPartsJobsManager.getInstance().jobError(logHelper.getLogFile());
            }
            return noErrors;
        } finally {
            projectForTruncate.setDBActive(false, false);
            SessionManager sessionManager = SessionManager.getInstance();
            sessionManager.deregisterThreadForSession(sessionForTruncate, Thread.currentThread()); // Damit die Session sauber entfernt werden kann
            sessionManager.destroySession(sessionForTruncate);
        }
    }

    private boolean partialDelete(Map<String, String> partialDeleteMap, String timeDurationMessage, VarParam<Integer> warningCount,
                                  VarParam<Integer> errorCount) {
        TruncateScriptTableHelper helper = new TruncateScriptTableHelper(projectForTruncate);
        helper.setMsgInterface(msgInterface);
        boolean noErrors = true;
        int progress = 0;
        long startTime = System.currentTimeMillis();
        for (Map.Entry<String, String> partialEntry : partialDeleteMap.entrySet()) {
            if (Thread.currentThread().isInterrupted()) {
                return false;
            }
            msgInterface.fireProgress(progress, partialDeleteMap.size());
            helper.setSqlString(partialEntry.getValue());
            noErrors = helper.doDeleteTable(partialEntry.getKey());
            if (!noErrors || (helper.getErrorCount() > 0)) {
                noErrors = false;
                break;
            }
            progress++;
        }
        msgInterface.hideProgress();
        warningCount.setValue(warningCount.getValue() + helper.getWarningCount());
        errorCount.setValue(errorCount.getValue() + helper.getErrorCount());
        helper.addMessage(timeDurationMessage, String.valueOf(partialDeleteMap.size()), getTimeDurationString(startTime, helper.getLogLanguage()));
        return noErrors;
    }

    private boolean deleteCarProducts(VarParam<Integer> warningCount, VarParam<Integer> errorCount) {
        List<iPartsProductId> productsToDelete = getCarProducts();
        DeleteProductHelper helper = new DeleteProductHelper(projectForTruncate);
        helper.setMsgInterface(msgInterface);
        boolean noErrors = helper.doDeleteProducts(productsToDelete);
        warningCount.setValue(warningCount.getValue() + helper.getWarningCount());
        errorCount.setValue(errorCount.getValue() + helper.getErrorCount());
        if (helper.getErrorCount() > 0) {
            noErrors = false;
        }
        // Laufzeit wird in doDeleteProducts() geloggt
        return noErrors;
    }

    private List<iPartsProductId> getCarProducts() {
        List<iPartsProductId> result = new DwList<>();
        for (iPartsProduct product : iPartsProduct.getAllProducts(projectForTruncate)) {
            if (!product.isTruckAndBusProduct()) {
                result.add(product.getAsId());
            }
        }
        return result;
    }

    private boolean truncateTables(String[] tablesToDelete, VarParam<Integer> warningCount, VarParam<Integer> errorCount) {
        TruncateTableHelper helper = new TruncateTableHelper(projectForTruncate);
        helper.setMsgInterface(msgInterface);
        boolean noErrors = true;
        int progress = 0;
        long startTime = System.currentTimeMillis();
        for (String tableName : tablesToDelete) {
            if (Thread.currentThread().isInterrupted()) {
                return false;
            }
            msgInterface.fireProgress(progress, tablesToDelete.length);
            noErrors = helper.doDeleteTable(tableName);
            if (!noErrors || (helper.getErrorCount() > 0)) {
                noErrors = false;
                break;
            }
            progress++;
        }
        msgInterface.hideProgress();
        warningCount.setValue(warningCount.getValue() + helper.getWarningCount());
        errorCount.setValue(errorCount.getValue() + helper.getErrorCount());
        helper.addMessage("!!Laufzeit zum kompletten Leeren von %1 Tabellen: %2", String.valueOf(tablesToDelete.length),
                          getTimeDurationString(startTime, helper.getLogLanguage()));
        return noErrors;
    }

    private void addStartMsg(ImportExportLogHelper logHelper, TruncateStyle style) {
        msgInterface.addMessage(buildStartMsg(logHelper, style));
        addNewLine(logHelper);
    }

    private String buildStartMsg(ImportExportLogHelper logHelper, TruncateStyle style) {
        String msg = logHelper.translateForLog(style.getName());
        if (style != TruncateStyle.PURGE_ALL) {
            msg += " " + logHelper.translateForLog("!!gestartet...");
        }
        return msg;
    }

    private String buildEndMsg(ImportExportLogHelper logHelper, String endMsg, int warningCount, int errorCount) {
        String additional = "";
        if ((warningCount + errorCount) > 0) {
            if (errorCount > 0) {
                additional = logHelper.translateForLog("!!mit %1 Fehlern", String.valueOf(errorCount));
                if (warningCount > 0) {
                    additional += " " + logHelper.translateForLog("!!und %1 Warnungen", String.valueOf(warningCount));
                }
            } else {
                additional = logHelper.translateForLog("!!mit %1 Warnungen", String.valueOf(warningCount));
            }
        }
        if (StrUtils.isValid(additional)) {
            endMsg = logHelper.translateForLog(endMsg) + " " + additional;
        }
        return endMsg;
    }

    private boolean isProgressFormValid() {
        return (progressForm != null) && sessionForGUI.isActive();
    }

    public void addNewLine(ImportExportLogHelper logHelper) {
        logHelper.addNewLine();
        if (isProgressFormValid()) {
            progressForm.getMessageLog().fireMessage("", MessageLogType.tmlMessage);
        }
    }

    private void setMsgInterface(ImportExportLogHelper logHelper) {
        msgInterface = new DeleteTableMessageInterface() {
            @Override
            public void addMessage(String key, String... placeHolderTexts) {
                String msg = logHelper.translateForLog(key, placeHolderTexts);
                logHelper.addLogMsg(msg);
                if (isProgressFormValid()) {
                    progressForm.getMessageLog().fireMessage(msg, MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                }
            }

            @Override
            public void addWarning(String key, String... placeHolderTexts) {
                String msg = logHelper.translateForLog(key, placeHolderTexts);
                logHelper.addLogWarning(msg);
                if (isProgressFormValid()) {
                    progressForm.getMessageLog().fireMessage(msg, MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                }
            }

            @Override
            public void addError(String key, String... placeHolderTexts) {
                String msg = logHelper.translateForLog(key, placeHolderTexts);
                logHelper.addLogError(msg);
                if (isProgressFormValid()) {
                    progressForm.getMessageLog().fireMessage(msg, MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
                }
            }

            @Override
            public String getLogLanguage() {
                return logHelper.getLogLanguage();
            }

            @Override
            public void fireProgress(int pos, int maxPos) {
                if (isProgressFormValid()) {
                    progressForm.getMessageLog().fireProgress(pos, maxPos, "", true, true);
                }
            }

            @Override
            public void hideProgress() {
                if (isProgressFormValid()) {
                    progressForm.getMessageLog().hideProgress();
                }
            }
        };
    }
}
