/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import;

import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormConnector;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.forms.functions.EtkFunction;
import de.docware.apps.etk.base.misc.MenuManager;
import de.docware.apps.etk.base.misc.StartPageType;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.*;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartList;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImage;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImageList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.translation.TranslationKeys;
import de.docware.apps.etk.plugins.AbstractJavaViewerPlugin;
import de.docware.apps.etk.plugins.EtkPluginConstants;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataMaterialRemarkList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSaaRemarksList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.events.RFTSxEnabledChangedEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.JobsMainForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsReplacementHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.AbstractFilesImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.iPartsMainImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsProductModels;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacement;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacementConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.iPartsMQMessageManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.events.DIALOGDeltaEnabledChangedEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.forms.AutoImportSettingsForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.forms.EditImportFilesForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.forms.SearchMADDataAfterDIALOGImportForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.messaging.text.iPartsImportTextContentMessageListeners;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.messaging.text.iPartsTextToDIALOGMapper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.messaging.xml.iPartsImportXMLMessageListeners;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.rftsx.RFTSXImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.translation.TranslationsExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.translation.TranslationsImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.translation.TranslationsObjectStoreHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.images.ImportDefaultImages;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.KgTuTemplateImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.cemat.CematModuleDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.code.ProvalCodeImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.connect.WireHarnessDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.devel.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSRemarkTextHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.initial.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.truckbomfoundation.forms.TruckBOMFoundationImportForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.truckbomfoundation.importer.TruckBOMStructureMappingImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.update.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.epep.update.ePEPIdentResponseImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.genvo.GenVoSuppTextImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.iPartsEqualPartsImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.iPartsTopTUsImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.images.iPartsReferenceImagesImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.MBSDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.form.MBSDataSelectionForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.helper.MBSImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.dialog.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc.helper.EPCImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.ppua.PPUAAlternateImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.primus.PrimusPartImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.primus.PrimusWWPartImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.proVal.iPartsProValImportScheduler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.proVal.iPartsProValModelAggImportScheduler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.reman.RemanVariantImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.singlepicparts.SinglePicPartsImportScheduler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.spk.SpkMappingImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.spk.SpkTextImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.srm.SrmPartImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.srm.SrmSupplierPartNoMappingDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.translations.TranslationsImporter;
import de.docware.apps.etk.plugins.interfaces.ModifyMenuInterface;
import de.docware.apps.etk.plugins.interfaces.ModifySecondToolbarInterface;
import de.docware.apps.etk.plugins.interfaces.ShowStartWindowInterface;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.apps.etk.viewer.FrameworkMain;
import de.docware.apps.etk.viewer.JavaViewerApplication;
import de.docware.framework.combimodules.config_gui.ConfigurationWindow;
import de.docware.framework.combimodules.config_gui.UniversalConfigurationPanel;
import de.docware.framework.modules.config.ConfigBase;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.config.defaultconfig.UniversalConfigOption;
import de.docware.framework.modules.config.defaultconfig.UniversalConfiguration;
import de.docware.framework.modules.config.license.LicenseConfig;
import de.docware.framework.modules.db.*;
import de.docware.framework.modules.db.etkrecord.EtkRecord;
import de.docware.framework.modules.gui.app.AbstractApplication;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiTextField;
import de.docware.framework.modules.gui.controls.calendar.CalendarUtils;
import de.docware.framework.modules.gui.controls.filechooser.ClientModeFileChooserWindow;
import de.docware.framework.modules.gui.controls.filechooser.FileChooserPurpose;
import de.docware.framework.modules.gui.controls.filechooser.GuiFileChooserDialog;
import de.docware.framework.modules.gui.controls.filechooser.GuiFileChooserTextfield;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolButton;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolbarManager;
import de.docware.framework.modules.gui.controls.toolbar.ToolButtonType;
import de.docware.framework.modules.gui.design.DesignImage;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.inputdialog.InputDialog;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.endpoint.FrameworkEndpointHelper;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.startparameter.CommandLine;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.misc.validator.GuiControlValidator;
import de.docware.framework.modules.gui.misc.validator.ValidationState;
import de.docware.framework.modules.gui.responsive.base.theme.MaterialTheme;
import de.docware.framework.modules.gui.responsive.base.theme.MaterialThemeIconComponent;
import de.docware.framework.modules.gui.responsive.base.theme.Theme;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.modules.gui.session.SessionManager;
import de.docware.framework.modules.gui.session.SessionType;
import de.docware.framework.modules.plugins.AbstractPlugin;
import de.docware.framework.modules.plugins.PluginRegistry;
import de.docware.framework.modules.plugins.interfaces.NeedsStaticConnectionUpdatesInterface;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.cache.ObjectInstanceLRUList;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import de.docware.util.misc.observer.CallbackBinder;
import de.docware.util.misc.observer.ObserverCall;
import de.docware.util.misc.observer.ObserverCallback;
import de.docware.util.security.PasswordString;
import de.docware.util.sql.TableAndFieldName;
import de.docware.util.sql.terms.Condition;
import de.docware.util.sql.terms.Fields;
import de.docware.util.sql.terms.InnerJoin;
import de.docware.util.sql.terms.Tables;
import org.apache.commons.collections4.map.LRUMap;

import java.io.File;
import java.util.*;

import static de.docware.framework.modules.db.DBDatabaseDomain.MAIN;

/**
 * iParts Plug-in für die Importe über Dateien bzw. MQ.
 */
public class iPartsImportPlugin extends AbstractJavaViewerPlugin implements iPartsConst, ShowStartWindowInterface, NeedsStaticConnectionUpdatesInterface,
                                                                            ModifyMenuInterface, ModifySecondToolbarInterface {

    public static final String INTERNAL_PLUGIN_NAME = "iPartsImport";
    public static final String OFFICIAL_PLUGIN_NAME = "DAIMLER iParts Import Plug-in"; // absichtlich kein Übersetzungstext
    public static final String PLUGIN_VERSION = "1.0";

    public static final String START_PARAMETER_IMPORT_MODE = "importMode";

    public static final LogChannels LOG_CHANNEL_DEBUG = new LogChannels("DEBUG", true, true);
    public static final LogChannels LOG_CHANNEL_RFTSX = new LogChannels("RFTSX", true, true);
    public static final LogChannels LOG_CHANNEL_FACTORYDATA_AUTO_RELEASE = new LogChannels("FACTORYDATA_AUTO_RELEASE", true, true);
    public static final LogChannels LOG_CHANNEL_TRUCK_BOM_FOUNDATION_IMPORT_SCHEDULER = new LogChannels("TBF_IMPORT_SCHEDULER", false, true);
    public static final LogChannels LOG_CHANNEL_TRUCK_TRANSLATIONS = new LogChannels("TRUCK_TRANSLATIONS", false, true);
    public static final LogChannels LOG_CHANNEL_CTT_PRECALC = new LogChannels("CTT_PRECALC", false, true);

    public static final MQChannelType MQ_CHANNEL_TYPE_DIALOG_IMPORT = new MQChannelType(iPartsMQChannelTypeNames.DIALOG_IMPORT, "jms/DIALOGQueueOut", "jms/DIALOGQueueIn");
    public static final MQChannelType MQ_CHANNEL_TYPE_DIALOG_DELTA_IMPORT = new MQChannelType(iPartsMQChannelTypeNames.DIALOG_DELTA_IMPORT, "jms/DIALOGDeltaQueueOut", "jms/DIALOGDeltaQueueIn");
    public static final MQChannelType MQ_CHANNEL_TYPE_DIALOG_DIRECT_IMPORT = new MQChannelType(iPartsMQChannelTypeNames.DIALOG_DIRECT_IMPORT, "jms/DIALOGDirectQueueOut", "jms/DIALOGDirectQueueIn");
    public static final MQChannelType MQ_CHANNEL_TYPE_DIALOG_DIRECT_DELTA_IMPORT = new MQChannelType(iPartsMQChannelTypeNames.DIALOG_DIRECT_DELTA_IMPORT, "jms/DIALOGDirectDeltaQueueOut", "jms/DIALOGDirectDeltaQueueIn");
    public static final MQChannelType MQ_CHANNEL_TYPE_EDS_IMPORT = new MQChannelType(iPartsMQChannelTypeNames.EDS_IMPORT, "jms/EDSQueueOut", "jms/EDSQueueIn");
    public static final MQChannelType MQ_CHANNEL_TYPE_PRIMUS_IMPORT = new MQChannelType(iPartsMQChannelTypeNames.PRIMUS_IMPORT, "jms/PRIMUSQueueOut", "jms/PRIMUSQueueIn");
    public static final MQChannelType MQ_CHANNEL_TYPE_SRM_IMPORT = new MQChannelType(iPartsMQChannelTypeNames.SRM_IMPORT, "jms/SRMQueueOut", "jms/SRMQueueIn");
    public static final String XML_MESSAGE_MANAGER_NAME_IMPORT_DATA = "xmlMessageManagerForMQImportData";
    public static final String TEXT_MESSAGE_MANAGER_NAME_IMPORT_DATA = "textMessageManagerForMQImportData";

    public static final UniversalConfigOption CONFIG_CLEAR_CACHE_WAIT_TIME = UniversalConfigOption.getIntegerOption("/clearCacheWaitTime", 10);
    public static final UniversalConfigOption CONFIG_DIALOG_INITIAL_IMPORT_CLEAR_CACHES = UniversalConfigOption.getBooleanOption("/dialogInitialImportClearCaches", true);

    public static final UniversalConfigOption CONFIG_DIALOG_DELTA_HANDLE_IMPORT = UniversalConfigOption.getBooleanOption("/dialogDeltaHandleImport", true);
    public static final UniversalConfigOption CONFIG_DIALOG_DIRECT_DELTA_MAX_MESSAGES = UniversalConfigOption.getIntegerOption("/dialogDirectDeltaMaxMessages", 5000);
    public static final UniversalConfigOption CONFIG_DIALOG_DIRECT_DELTA_IMPORT_WAIT_TIME = UniversalConfigOption.getIntegerOption("/dialogDirectDeltaImportWaitTime", 30);

    public static final UniversalConfigOption CONFIG_MQ_PRIMUS_COLLECT_TIME = UniversalConfigOption.getIntegerOption("/mqPrimusCollectTime", 10 * 60); // 10 Minuten
    public static final UniversalConfigOption.UniversalConfigCustomOption CONFIG_MQ_PRIMUS_IMPORT_TIME_INTERVAL = UniversalConfigOption.getCustomOption("/mqPrimusImportTimeInterval", iPartsTimeIntervalCustomOptionWithControl.getTimeIntervalCustomOption(), "");
    public static final UniversalConfigOption CONFIG_MQ_SRM_COLLECT_TIME = UniversalConfigOption.getIntegerOption("/mqSRMCollectTime", 10 * 60); // 10 Minuten
    public static final UniversalConfigOption.UniversalConfigCustomOption CONFIG_MQ_SRM_IMPORT_TIME_INTERVAL = UniversalConfigOption.getCustomOption("/mqSrmImportTimeInterval", iPartsTimeIntervalCustomOptionWithControl.getTimeIntervalCustomOption(), "");
    public static final UniversalConfigOption.UniversalConfigCustomOption CONFIG_MQ_BOMDB_IMPORT_TIME_INTERVAL = UniversalConfigOption.getCustomOption("/mqBOMDBImportTimeInterval", iPartsTimeIntervalCustomOptionWithControl.getTimeIntervalCustomOption(), "");
    public static final UniversalConfigOption.UniversalConfigCustomOption CONFIG_MQ_DIALOG_IMPORT_TIME_INTERVAL = UniversalConfigOption.getCustomOption("/mqDIALOGImportTimeInterval", iPartsTimeIntervalCustomOptionWithControl.getTimeIntervalCustomOption(), "");

    // Scheduler für TruckBOM.foundation Import
    public static final UniversalConfigOption CONFIG_TRUCK_BOM_FOUNDATION_IMPORT_SCHEDULER_ACTIVE = UniversalConfigOption.getBooleanOption("/truckBomFoundationImportSchedulerActive", false);
    public static final UniversalConfigOption CONFIG_TRUCK_BOM_FOUNDATION_IMPORT_SCHEDULER_DAYS = UniversalConfigOption.getStringListOptionMultipleSelection("/truckBomFoundationImportSchedulerDays", new String[]{ DateUtils.DayOfWeek.SUNDAY.getDisplayName() }); // initial Sonntag
    public static final UniversalConfigOption CONFIG_TRUCK_BOM_FOUNDATION_IMPORT_SCHEDULER_TIME = UniversalConfigOption.getTimeOption("/truckBomFoundationImportSchedulerTime", new Date(0)); // initial 00:00 GMT

    // Scheduler für ProVal Import
    public static final UniversalConfigOption CONFIG_PROVAL_IMPORT_SCHEDULER_ACTIVE = UniversalConfigOption.getBooleanOption("/proValImportSchedulerActive", false);
    public static final UniversalConfigOption CONFIG_PROVAL_IMPORT_SCHEDULER_DAYS = UniversalConfigOption.getStringListOptionMultipleSelection("/proValImportSchedulerDays", new String[]{ DateUtils.DayOfWeek.SUNDAY.getDisplayName() }); // initial Sonntag
    public static final UniversalConfigOption CONFIG_PROVAL_IMPORT_SCHEDULER_TIME = UniversalConfigOption.getTimeOption("/proValImportSchedulerTime", new Date(0)); // initial 00:00 GMT

    // Scheduler für ProVal Baubarkeit Import
    public static final UniversalConfigOption CONFIG_PROVAL_MODEL_AGG_IMPORT_SCHEDULER_ACTIVE = UniversalConfigOption.getBooleanOption("/proValModelAggImportSchedulerActive", false);
    public static final UniversalConfigOption CONFIG_PROVAL_MODEL_AGG_IMPORT_SCHEDULER_DAYS = UniversalConfigOption.getStringListOptionMultipleSelection("/proValModelAggImportSchedulerDays", new String[]{ DateUtils.DayOfWeek.SUNDAY.getDisplayName() }); // initial Sonntag
    public static final UniversalConfigOption CONFIG_PROVAL_MODEL_AGG_IMPORT_SCHEDULER_TIME = UniversalConfigOption.getTimeOption("/proValModelAggImportSchedulerTime", new Date(0)); // initial 00:00 GMT

    public static final UniversalConfigOption CONFIG_IMPORT_FILE_DIR = UniversalConfigOption.getFileOption("/importFileDir", new File("importFiles"));
    public static final UniversalConfigOption CONFIG_IMPORT_FILE_SERVER_DIR = UniversalConfigOption.getFileOption("/importFileServerDir", new File("importServerFiles"));

    // Parameter für AS-PLM (inkl. Simulation)
    public static final UniversalConfigOption CONFIG_IMAGE_IMPORT_SOURCE = UniversalConfigOption.getStringListOptionSingleSelection("/imageImportSource", ImageImportSource.NONE.getDescription());
    public static final UniversalConfigOption CONFIG_SIM_AUTO_RESPONSE_DELAY_PIC_REF = UniversalConfigOption.getIntegerOption("/simAutoResponseDelayPicRef", -1);
    public static final UniversalConfigOption CONFIG_IMPORT_IMAGES_SERVER_DIR = UniversalConfigOption.getFileOption("/importImagesServerDir", new File("importImagesDir"));
    public static final UniversalConfigOption CONFIG_IMPORT_RFTSX_IMAGES = UniversalConfigOption.getBooleanOption("/importRFTSxSearchEnabled", true);
    public static final UniversalConfigOption CONFIG_IMPORT_RFTSX_IMAGES_UNZIP_ROOT_DIR = UniversalConfigOption.getFileOption("/importRFTSxImagesUnpackRootDir", new File("importRFTSxImagesRootDir"));

    // RFTS/x
    private static final String RFTSX_FILE_EXTENSIONS_DEFAULT_VALUE = "gz;txt;csv;xml";
    public static final UniversalConfigOption.UniversalConfigCustomOption CONFIG_RFTSX_IMPORT_TIME_INTERVAL = UniversalConfigOption.getCustomOption("/rftsxImportTimeInterval", iPartsTimeIntervalCustomOptionWithControl.getTimeIntervalCustomOption(), "");
    public static final UniversalConfigOption CONFIG_RFTSX_IMPORTER_TYPES = UniversalConfigOption.getStringListOptionMultipleSelection("/importTypes", ImporterTypes.getAllEnumNames()); // initial Sonntag
    public static final UniversalConfigOption CONFIG_RFTSX_FILE_EXTENSIONS = UniversalConfigOption.getStringOption("/rftsxFileExtensions", RFTSX_FILE_EXTENSIONS_DEFAULT_VALUE);
    public static final UniversalConfigOption CONFIG_RFTSX_ACCEPT_FILES_WITHOUT_EXTENSION = UniversalConfigOption.getBooleanOption("/rftsxAcceptFilesWithoutExtension", false);
    public static final UniversalConfigOption CONFIG_RFTSX_COPY_TO_ARCHIVE = UniversalConfigOption.getBooleanOption("/rftsxCopyToArchive", true);
    public static final UniversalConfigOption CONFIG_RFTSX_HANDLE_IMPORT = UniversalConfigOption.getBooleanOption("/rftsxHandleImport", true);
    public static final UniversalConfigOption CONFIG_RFTSX_INPUT_DIR = UniversalConfigOption.getFileOption("/rftsxInputDir", new File(""));  // Default nicht sinnvoll
    public static final UniversalConfigOption CONFIG_RFTSX_WORK_DIR = UniversalConfigOption.getFileOption("/rftsxWorkDir", new File(""));  // Default nicht sinnvoll
    public static final UniversalConfigOption CONFIG_RFTSX_ARCHIVE_DIR = UniversalConfigOption.getFileOption("/rftsxArchiveDir", new File(""));  // Default nicht sinnvoll
    public static final UniversalConfigOption CONFIG_RFTSX_ENFORCE_POLLING = UniversalConfigOption.getBooleanOption("/rftsxEnforcePolling", false);
    public static final UniversalConfigOption CONFIG_RFTSX_POLLING_TIME = UniversalConfigOption.getIntegerOption("/rftsxPollingTime", 10);

    // Optionen für Übersetzungstexte
    public static final UniversalConfigOption CONFIG_TRANSIT_USE_PREFIX = UniversalConfigOption.getBooleanOption("/transitUsePrefix", false);
    public static final UniversalConfigOption CONFIG_TRANSIT_DIR_NAME_PREFIX = UniversalConfigOption.getStringOption("/transitDirNamePrefix", "XPRT_");
    public static final UniversalConfigOption CONFIG_TRANSIT_ROOT_DIR = UniversalConfigOption.getFileOption("/transitRootDir", new File(""));
    public static final UniversalConfigOption CONFIG_TRANSIT_DIR_NAME_TEXT_INCOMING = UniversalConfigOption.getStringOption("/transitTextIncoming", "21_DAA_Nutzdaten_von_Uebers_an_AG");
    public static final UniversalConfigOption CONFIG_TRANSIT_DIR_NAME_TEXT_OUTGOING = UniversalConfigOption.getStringOption("/transitTextOutgoing", "11_DAE_Nutzdaten_von_AG_an_Uebers");
    public static final UniversalConfigOption CONFIG_TRANSIT_DIR_NAME_LOGS_INCOMING = UniversalConfigOption.getStringOption("/transitLogsIncoming", "12_DAE_Logs_von_Uebers_an_AG");
    public static final UniversalConfigOption CONFIG_TRANSIT_DIR_NAME_LOGS_OUTGOING = UniversalConfigOption.getStringOption("/transitTogsOutgoing", "22_DAA_Logs_von_AG_an_Uebers");
    public static final UniversalConfigOption CONFIG_TRANSIT_MAX_ENTRIES = UniversalConfigOption.getIntegerOption("/transitMaxEntries", 5000);
    public static final UniversalConfigOption CONFIG_TRANSIT_SCHEMA_FILE = UniversalConfigOption.getFileOption("/transitSchemaFile", new File(""));
    public static final UniversalConfigOption CONFIG_TRANSIT_STYLE_SHEET_FILE = UniversalConfigOption.getFileOption("/transitStyleSheetFile", new File(""));
    public static final UniversalConfigOption CONFIG_TRANSIT_ARCHIVE_DIR = UniversalConfigOption.getFileOption("/transitArchiveDir", new File(""));
    public static final UniversalConfigOption CONFIG_TRANSIT_POLLING_TIME = UniversalConfigOption.getIntegerOption("/transitPollingTime", 10);
    public static final UniversalConfigOption CONFIG_TRANSIT_CUSTOM_USER_ID_CAR = UniversalConfigOption.getStringOption("/customUserIdCar", "");
    public static final UniversalConfigOption CONFIG_TRANSIT_CONTACT_PERSON_USER_ID_CAR = UniversalConfigOption.getStringOption("/contactPersonUserIdCar", "");
    public static final UniversalConfigOption CONFIG_TRANSIT_ADDITIONAL_CONTACT_EMAIL_CAR = UniversalConfigOption.getStringOption("/additionalContactEmailCar", "");

    // Truck
    public static final UniversalConfigOption CONFIG_TRANSLATION_TRUCK_ACTIVE = UniversalConfigOption.getBooleanOption("/translationTruckActive", false);
    public static final UniversalConfigOption CONFIG_TRANSLATION_TRUCK_BUCKET_NAME = UniversalConfigOption.getStringOption("/translationTruckBucketName", "iparts-data-exchange-dev");
    public static final UniversalConfigOption CONFIG_TRANSLATION_TRUCK_ACCESS_KEY = UniversalConfigOption.getStringOption("/translationTruckAccessKey", "");
    public static final UniversalConfigOption CONFIG_TRANSLATION_TRUCK_SECRET_ACCESS_KEY = UniversalConfigOption.getPasswordOption("/translationTruckSecretAccessKey", PasswordString.EMPTY);
    public static final UniversalConfigOption CONFIG_TRANSLATION_TRUCK_POLLING_TIME = UniversalConfigOption.getIntegerOption("/translationTruckPollingTime", 10);
    public static final UniversalConfigOption CONFIG_TRANSLATION_TRUCK_MAX_DOWNLOAD_COUNT = UniversalConfigOption.getIntegerOption("/translationTruckMaxDownloadCount", 200);
    public static final UniversalConfigOption CONFIG_TRANSLATION_TRUCK_RECONNECT_ATTEMPTS = UniversalConfigOption.getIntegerOption("/translationtruckReconnectAttempts", -1);
    public static final UniversalConfigOption CONFIG_TRANSLATION_TRUCK_RECONNECT_TIME = UniversalConfigOption.getIntegerOption("/translationTruckReconnectTime", 10);
    public static final UniversalConfigOption CONFIG_TRANSLATION_TRUCK_DIR_NAME_TEXT_INCOMING = UniversalConfigOption.getStringOption("/translationTruckTextIncoming", "21_DAA_Nutzdaten_von_Uebers_an_AG");
    public static final UniversalConfigOption CONFIG_TRANSLATION_TRUCK_DIR_NAME_TEXT_OUTGOING = UniversalConfigOption.getStringOption("/translationTruckTextOutgoing", "11_DAE_Nutzdaten_von_AG_an_Uebers");
    public static final UniversalConfigOption CONFIG_TRANSLATION_TRUCK_DIR_NAME_LOGS_INCOMING = UniversalConfigOption.getStringOption("/translationTruckLogsIncoming", "12_DAE_Logs_von_Uebers_an_AG");
    public static final UniversalConfigOption CONFIG_TRANSLATION_TRUCK_DIR_NAME_LOGS_OUTGOING = UniversalConfigOption.getStringOption("/translationTruckTogsOutgoing", "22_DAA_Logs_von_AG_an_Uebers");
    public static final UniversalConfigOption CONFIG_TRANSLATION_TRUCK_MAX_ENTRIES = UniversalConfigOption.getIntegerOption("/translationTruckMaxEntries", 5000);
    public static final UniversalConfigOption CONFIG_TRANSLATION_TRUCK_SCHEMA_FILE = UniversalConfigOption.getFileOption("/translationTruckSchemaFile", new File(""));
    public static final UniversalConfigOption CONFIG_TRANSLATION_TRUCK_STYLE_SHEET_FILE = UniversalConfigOption.getFileOption("/translationTruckStyleSheetFile", new File(""));
    public static final UniversalConfigOption CONFIG_TRANSLATION_TRUCK_ARCHIVE_DIR = UniversalConfigOption.getFileOption("/translationTruckArchiveDir", new File(""));
    public static final UniversalConfigOption CONFIG_TRANSLATION_TRUCK_CUSTOM_USER_ID = UniversalConfigOption.getStringOption("/customUserIdTRUCK", "");
    public static final UniversalConfigOption CONFIG_TRANSLATION_TRUCK_CONTACT_PERSON_USER_ID = UniversalConfigOption.getStringOption("/contactPersonUserIdTRUCK", "");
    public static final UniversalConfigOption CONFIG_TRANSLATION_TRUCK_ADDITIONAL_CONTACT_EMAIL = UniversalConfigOption.getStringOption("/additionalContactEmailTRUCK", "");

    // Konfig Option um die Automatische Freigabe der Werksdaten bei Erhalt der DIALOG Ende-Nachricht ausschalten zu können
    public static final UniversalConfigOption CONFIG_IMPORT_EXECUTE_AUTORELEASE = UniversalConfigOption.getBooleanOption("/executeFactoryDataAutoRelease", true);
    public static final UniversalConfigOption CONFIG_IMPORT_AUTO_RELEASE_DATE_THRESHOLD = UniversalConfigOption.getIntegerOption("/autoReleaseDateThreshold", 0);

    // Scheduler für Einzelteilbilder
    public static final UniversalConfigOption CONFIG_SINGLE_PIC_PARTS_ACTIVE = UniversalConfigOption.getBooleanOption("/singlePicPartsActive", false);
    public static final UniversalConfigOption CONFIG_SINGLE_PIC_PARTS_DAYS = UniversalConfigOption.getStringListOptionMultipleSelection("/singlePicPartsDays", new String[]{ DateUtils.DayOfWeek.SUNDAY.getDisplayName() }); // initial Sonntag
    public static final UniversalConfigOption CONFIG_SINGLE_PIC_PARTS_TIME = UniversalConfigOption.getTimeOption("/singlePicPartsTime", new Date(0)); // initial 00:00 GMT

    // Scheduler für CTT Arbeitsvorrat vorverdichten
    public static final UniversalConfigOption CONFIG_CALC_SINGLE_CTT_WORKBASKET_ACTIVE = UniversalConfigOption.getBooleanOption("/singleCttWorkBasketActive", false);
    public static final UniversalConfigOption CONFIG_CALC_SINGLE_CTT_WORKBASKET_DAYS = UniversalConfigOption.getStringListOptionMultipleSelection("/singleCttWorkBasketDays", new String[]{ DateUtils.DayOfWeek.SUNDAY.getDisplayName() }); // initial Sonntag
    public static final UniversalConfigOption CONFIG_CALC_SINGLE_CTT_WORKBASKET_TIME = UniversalConfigOption.getTimeOption("/singleCttWorkBasketTime", new Date(0)); // initial 00:00 GMT
    public static final UniversalConfigOption CONFIG_CALC_SINGLE_CTT_WORKBASKET_DELETE = UniversalConfigOption.getBooleanOption("/singleCttWorkBasketDelete", false);

    private static UniversalConfiguration pluginConfig;
    private static Session rftsxSession;
    private static EtkProject rftsxProject;
    private static List<iPartsMQMessageManager> mqMessageManagers;
    private static SinglePicPartsImportScheduler singlePicPartsImportScheduler;
    private static iPartsTruckBOMFoundationImportScheduler truckBOMFoundationImportScheduler;
    private static iPartsProValImportScheduler proValImportScheduler;
    private static iPartsProValModelAggImportScheduler proValModelAggImportScheduler;
    private static CTTWorkbasketPrecalculationScheduler cttPrecalculationScheduler;
    private final CallbackBinder callbackBinder = new CallbackBinder();
    private ObserverCallback rftsxEnabledChangedEventListener;
    private ObserverCallback dialogDeltaEnabledChangedEventListener;
    private boolean saveToDB = true;

    public static UniversalConfiguration getPluginConfig() {
        return pluginConfig;
    }

    /**
     * Überprüft, ob das iParts Import Plug-in eigenständig ohne das iParts Edit Plug-in läuft.
     *
     * @return
     */
    public static boolean isImportPluginStandalone() {
        if (Session.get().getStartParameter().getParameterBoolean(START_PARAMETER_IMPORT_MODE, false)) {
            return true;
        }

        return !iPartsPlugin.isEditPluginActive();
    }

    /**
     * Liefert die zentrale {@link Session} ohne GUI für die Kommunikation über RFTS/x.
     *
     * @return
     */
    public static Session getRFTSxSession() {
        return rftsxSession;
    }

    /**
     * Liefert das zentrale {@link EtkProject} ohne GUI für die Kommunikation über RFTS/x.
     *
     * @return
     */
    public static EtkProject getRFTSxProject() {
        return rftsxProject;
    }

    public static ImageImportSource getSelectedImageImportSource() {
        String imageImportSource = getPluginConfig().getConfigValueAsString(CONFIG_IMAGE_IMPORT_SOURCE);
        return ImageImportSource.getFromDescription(imageImportSource);
    }

    public static Set<String> getValidRFTSXImportTypes() {
        String[] validTypesFromConfig = getPluginConfig().getConfigValueAsStringArray(CONFIG_RFTSX_IMPORTER_TYPES);
        return new HashSet<>(Arrays.asList(validTypesFromConfig));
    }

    public static void restartSinglePicPartsImportThread() {
        iPartsPlugin.restartTimerThread(singlePicPartsImportScheduler, pluginConfig, CONFIG_SINGLE_PIC_PARTS_ACTIVE,
                                        CONFIG_SINGLE_PIC_PARTS_DAYS, CONFIG_SINGLE_PIC_PARTS_TIME,
                                        iPartsPlugin.LOG_CHANNEL_SINGLE_PIC_PARTS);
    }

    public static void restartTruckBOMFoundationImportThread() {
        iPartsPlugin.restartTimerThread(truckBOMFoundationImportScheduler, pluginConfig, CONFIG_TRUCK_BOM_FOUNDATION_IMPORT_SCHEDULER_ACTIVE,
                                        CONFIG_TRUCK_BOM_FOUNDATION_IMPORT_SCHEDULER_DAYS, CONFIG_TRUCK_BOM_FOUNDATION_IMPORT_SCHEDULER_TIME,
                                        LOG_CHANNEL_TRUCK_BOM_FOUNDATION_IMPORT_SCHEDULER);
    }

    public static void restartProValImportThread() {
        iPartsPlugin.restartTimerThread(proValImportScheduler, pluginConfig, CONFIG_PROVAL_IMPORT_SCHEDULER_ACTIVE,
                                        CONFIG_PROVAL_IMPORT_SCHEDULER_DAYS, CONFIG_PROVAL_IMPORT_SCHEDULER_TIME,
                                        iPartsPlugin.LOG_CHANNEL_PROVAL);
    }

    public static void restartProValModelAggImportThread() {
        iPartsPlugin.restartTimerThread(proValModelAggImportScheduler, pluginConfig, CONFIG_PROVAL_MODEL_AGG_IMPORT_SCHEDULER_ACTIVE,
                                        CONFIG_PROVAL_MODEL_AGG_IMPORT_SCHEDULER_DAYS, CONFIG_PROVAL_MODEL_AGG_IMPORT_SCHEDULER_TIME,
                                        iPartsPlugin.LOG_CHANNEL_PROVAL);
    }

    public static void restartCttPreCalculationThread() {
        iPartsPlugin.restartTimerThread(cttPrecalculationScheduler, pluginConfig, CONFIG_CALC_SINGLE_CTT_WORKBASKET_ACTIVE,
                                        CONFIG_CALC_SINGLE_CTT_WORKBASKET_DAYS, CONFIG_CALC_SINGLE_CTT_WORKBASKET_TIME,
                                        LOG_CHANNEL_CTT_PRECALC);
    }

    @Override
    public String getInternalPluginName() {
        return INTERNAL_PLUGIN_NAME;
    }

    @Override
    public String getOfficialPluginName() {
        return OFFICIAL_PLUGIN_NAME;
    }

    @Override
    public String getPluginVersion() {
        return PLUGIN_VERSION;
    }

    @Override
    public UniversalConfigurationPanel getConfigurationPanel(ConfigurationWindow host) {
        UniversalConfigurationPanel configurationPanel = new iPartsImportUniversalConfigurationPanel(host, pluginConfig, OFFICIAL_PLUGIN_NAME, true);

        configurationPanel.startGroup("!!Cache-Einstellungen");
        configurationPanel.addIntegerSpinnerOption(CONFIG_CLEAR_CACHE_WAIT_TIME, "!!Wartezeit in Sekunden vor dem Cache-Löschen bei automatischen Importen",
                                                   true, 0, 1000, 1).setTooltip("!!Die Wartezeit verlängert sich bei jedem Import desselben Typs um die eingestellte Zeit.");
        configurationPanel.addBooleanOption(CONFIG_DIALOG_INITIAL_IMPORT_CLEAR_CACHES, "!!Caches nach DIALOG-Urladungsimporten automatisch löschen", false);
        configurationPanel.endGroup();

        addIntervalOptionGroup(configurationPanel, "!!MQ PRIMUS Importe", CONFIG_MQ_PRIMUS_COLLECT_TIME, "MQ PRIMUS", CONFIG_MQ_PRIMUS_IMPORT_TIME_INTERVAL);
        addIntervalOptionGroup(configurationPanel, "!!MQ SRM Importe", CONFIG_MQ_SRM_COLLECT_TIME, "MQ SRM", CONFIG_MQ_SRM_IMPORT_TIME_INTERVAL);
        addIntervalOptionGroup(configurationPanel, "!!MQ BOM-DB Änderungsdienst Importe", null, "MQ BOM-DB Änderungsdienst", CONFIG_MQ_BOMDB_IMPORT_TIME_INTERVAL);
        addIntervalOptionGroup(configurationPanel, "!!MQ DIALOG Änderungsdienst Importe", null, "MQ DIALOG Änderungsdienst", CONFIG_MQ_DIALOG_IMPORT_TIME_INTERVAL);

        // TruckBOM.foundation Import Scheduler
        iPartsPlugin.createDayOfWeekTimerGroup(configurationPanel, "!!Automatische TruckBOM.foundation Importe",
                                               CONFIG_TRUCK_BOM_FOUNDATION_IMPORT_SCHEDULER_ACTIVE, CONFIG_TRUCK_BOM_FOUNDATION_IMPORT_SCHEDULER_DAYS,
                                               CONFIG_TRUCK_BOM_FOUNDATION_IMPORT_SCHEDULER_TIME, true);

        // ProVal Import Scheduler
        iPartsPlugin.createDayOfWeekTimerGroup(configurationPanel, "!!Automatische ProVal Importe",
                                               CONFIG_PROVAL_IMPORT_SCHEDULER_ACTIVE, CONFIG_PROVAL_IMPORT_SCHEDULER_DAYS,
                                               CONFIG_PROVAL_IMPORT_SCHEDULER_TIME, true);
        // ProVal Model Agg Import Scheduler
        iPartsPlugin.createDayOfWeekTimerGroup(configurationPanel, "!!Automatische ProVal Baubarkeit Importe",
                                               CONFIG_PROVAL_MODEL_AGG_IMPORT_SCHEDULER_ACTIVE, CONFIG_PROVAL_MODEL_AGG_IMPORT_SCHEDULER_DAYS,
                                               CONFIG_PROVAL_MODEL_AGG_IMPORT_SCHEDULER_TIME, true);

        configurationPanel.addSeparator();
        configurationPanel.addFileOption(CONFIG_IMPORT_FILE_DIR, "!!Speicherort für Import-Dateien", true, GuiFileChooserDialog.FILE_MODE_DIRECTORIES,
                                         FileChooserPurpose.SAVE, null);
        configurationPanel.addFileOption(CONFIG_IMPORT_FILE_SERVER_DIR, "!!Importverzeichnis für große Dateien auf dem Server", true, GuiFileChooserDialog.FILE_MODE_DIRECTORIES,
                                         FileChooserPurpose.OPEN, null);

        configurationPanel.addSeparator();
        configurationPanel.startGroup("!!Automatische Freigabe der Werksdaten");
        configurationPanel.addBooleanOption(CONFIG_IMPORT_EXECUTE_AUTORELEASE, "!!Bei Erhalt der DIALOG Ende-Nachricht ausführen", false);
        configurationPanel.addIntegerSpinnerOption(CONFIG_IMPORT_AUTO_RELEASE_DATE_THRESHOLD, "Terminverschiebung für Werkstermine (max. +/- Tage)").
                setTooltip("!!0 für keine automatische Freigabe");
        configurationPanel.endGroup();
        configurationPanel.addSeparator();

        configurationPanel.startGroup("!!Bildreferenzen");
        configurationPanel.addStringListOptionSingleSelection(CONFIG_IMAGE_IMPORT_SOURCE, "!!Mediensuche", false, ImageImportSource.getDescriptions(), false);
        configurationPanel.addIntegerSpinnerOption(CONFIG_SIM_AUTO_RESPONSE_DELAY_PIC_REF, "!!Simulation: Automatische Antwort nach x Sekunden",
                                                   false, -1, 1000, 1).setTooltip("!!-1 für keine automatische Antwort");
        configurationPanel.addFileOption(CONFIG_IMPORT_IMAGES_SERVER_DIR, "!!Importverzeichnis für Referenzzeichnungen", false, GuiFileChooserDialog.FILE_MODE_DIRECTORIES,
                                         FileChooserPurpose.OPEN, null);
        configurationPanel.addBooleanOption(CONFIG_IMPORT_RFTSX_IMAGES, "!!DASTi Bildarchive über RFTS/x empfangen", false).
                setTooltip("!!Schaltet die Suche und den Import von DASTi Bildarchiven im RFTS/x Importverzeichnis ein bzw. aus");
        configurationPanel.addFileOption(CONFIG_IMPORT_RFTSX_IMAGES_UNZIP_ROOT_DIR, "!!Zielverzeichnis für entpackte RFTS/x DASTi Bildarchive", false, GuiFileChooserDialog.FILE_MODE_DIRECTORIES,
                                         FileChooserPurpose.SAVE, null);
        configurationPanel.endGroup();

        // DIALOG Delta Importer
        configurationPanel.startGroup("!!DIALOG Importer");
        configurationPanel.addBooleanOption(CONFIG_DIALOG_DELTA_HANDLE_IMPORT, "!!Delta Importe durchführen (unabhängig von Umgebungs-Varianten)", true).
                setTooltip("!!Diese Option ist unabhängig von Umgebungs-Varianten und gilt somit für alle Cluster-Knoten");
        configurationPanel.addIntegerSpinnerOption(CONFIG_DIALOG_DIRECT_DELTA_MAX_MESSAGES, "!!Anzahl an Nachrichten, ab denen der Importer startet (DIALOG Direct)", true, 1, 100000, 1);
        configurationPanel.addIntegerSpinnerOption(CONFIG_DIALOG_DIRECT_DELTA_IMPORT_WAIT_TIME, "!!Wartezeit in Sekunden vor Start des automatischen Imports (DIALOG Direct)",
                                                   true, 0, 1000, 1).setTooltip("!!Die Wartezeit verlängert sich bei jedem Import desselben Typs um die eingestellte Zeit.");
        configurationPanel.endGroup();

        // RFTS/x
        configurationPanel.startGroup("!!RFTS/x");
        configurationPanel.addBooleanOption(CONFIG_RFTSX_HANDLE_IMPORT, "!!Importe durchführen (unabhängig von Umgebungs-Varianten)", true).
                setTooltip("!!Diese Option ist unabhängig von Umgebungs-Varianten und gilt somit für alle Cluster-Knoten");
        iPartsPlugin.addTimeIntervalControl(configurationPanel, CONFIG_RFTSX_IMPORT_TIME_INTERVAL, "!!Zeitraum für den Import von RFTS/x Dateien", "!!Sind Start- und Endzeit gleich oder eine Zeitangabe leer, wird kontinuierlich importiert");
        configurationPanel.addStringListOptionMultipleSelection(CONFIG_RFTSX_IMPORTER_TYPES, "!!Importerart", false, ImporterTypes.getAllEnumNames(), false);
        configurationPanel.addStringOption(CONFIG_RFTSX_FILE_EXTENSIONS, "!!Gültige Dateiendungen", true).setTooltip(TranslationHandler.translate("!!Dateiendungen ohne Punkt und durch \";\" getrennt; Standardwert: %1",
                                                                                                                                                  RFTSX_FILE_EXTENSIONS_DEFAULT_VALUE));
        configurationPanel.addBooleanOption(CONFIG_RFTSX_ACCEPT_FILES_WITHOUT_EXTENSION, "!!Dateien ohne Dateiendung ebenfalls akzeptieren.", false);
        configurationPanel.addFileOption(CONFIG_RFTSX_INPUT_DIR, "!!Eingangsverzeichnis (Share)", true, GuiFileChooserDialog.FILE_MODE_DIRECTORIES,
                                         FileChooserPurpose.OPEN, null);
        configurationPanel.addFileOption(CONFIG_RFTSX_WORK_DIR, "!!Arbeitsverzeichnis (lokal)", true, GuiFileChooserDialog.FILE_MODE_DIRECTORIES,
                                         FileChooserPurpose.OPEN, null);
        configurationPanel.addFileOption(CONFIG_RFTSX_ARCHIVE_DIR, "!!Archivverzeichnis (Share)", true, GuiFileChooserDialog.FILE_MODE_DIRECTORIES,
                                         FileChooserPurpose.OPEN, null);
        configurationPanel.addBooleanOption(CONFIG_RFTSX_COPY_TO_ARCHIVE, "!!Dateien archivieren", false);
        configurationPanel.addSeparator();
        configurationPanel.addBooleanOption(CONFIG_RFTSX_ENFORCE_POLLING, "!!Polling erzwingen (nötig z.B. für NFS Shares)", false);
        configurationPanel.addIntegerSpinnerOption(CONFIG_RFTSX_POLLING_TIME, "!!Polling Intervall (in min)", false, 1, 1000, 1);
        configurationPanel.endGroup();

        // PKW Übersetzungsprozess
        configurationPanel.startGroup("!!Übersetzungsprozess");
        configurationPanel.addIntegerOption(CONFIG_TRANSIT_MAX_ENTRIES, "!!Maximale Anzahl Einträge pro XML Datei", true).setTooltip("!!Bei -1 oder 0 findet kein Export statt.");
        configurationPanel.addIntegerSpinnerOption(CONFIG_TRANSIT_POLLING_TIME, "!!Polling Intervall (in min)", true, 1, 1000, 1);
        configurationPanel.startGroup("!!Verzeichnisse");
        configurationPanel.addFileOption(CONFIG_TRANSIT_ROOT_DIR, "!!Hauptverzeichnis", false, GuiFileChooserDialog.FILE_MODE_DIRECTORIES,
                                         FileChooserPurpose.OPEN, null);
        configurationPanel.addStringOption(CONFIG_TRANSIT_DIR_NAME_TEXT_OUTGOING, "!!Verzeichnisname für Nutzdaten von AG an Übersetzer", true);
        configurationPanel.addStringOption(CONFIG_TRANSIT_DIR_NAME_TEXT_INCOMING, "!!Verzeichnisname für Nutzdaten von Übersetzer an AG", true);
        configurationPanel.addStringOption(CONFIG_TRANSIT_DIR_NAME_LOGS_OUTGOING, "!!Verzeichnisname für Logs von AG an Übersetzer", true);
        configurationPanel.addStringOption(CONFIG_TRANSIT_DIR_NAME_LOGS_INCOMING, "!!Verzeichnisname für Logs von Übersetzer an AG", true);
        configurationPanel.addFileOption(CONFIG_TRANSIT_ARCHIVE_DIR, "!!Verzeichnis zum Archivieren der Dateien",
                                         false, GuiFileChooserDialog.FILE_MODE_DIRECTORIES, FileChooserPurpose.OPEN, null);
        configurationPanel.endGroup();
        configurationPanel.addSeparator();
        // Externe Dateien für das ZIP-Archiv
        configurationPanel.startGroup("!!Dateien für die ZIP Archive");
        // Schemadatei
        addSchemaFileOptionToTranslationOptions(configurationPanel, CONFIG_TRANSIT_SCHEMA_FILE);
        // Stylesheetdatei
        addStyleSheetFileOptionToTranslationOptions(configurationPanel, CONFIG_TRANSIT_STYLE_SHEET_FILE);
        configurationPanel.endGroup();
        configurationPanel.addSeparator();
        configurationPanel.startGroup("!!Support-Informationen für die TOC Datei");
        configurationPanel.addStringOption(CONFIG_TRANSIT_CUSTOM_USER_ID_CAR, "!!CustomerUserID für Mercedes Benz AG", false);
        configurationPanel.addStringOption(CONFIG_TRANSIT_CONTACT_PERSON_USER_ID_CAR, "!!ContactpersonUserID für Mercedes Benz AG", false);
        configurationPanel.addStringOption(CONFIG_TRANSIT_ADDITIONAL_CONTACT_EMAIL_CAR, "!!AdditionalContactEmail für Mercedes Benz AG", false);
        configurationPanel.endGroup();
        configurationPanel.addSeparator();
        addTestModeGroupToTranslationOptions(configurationPanel, CONFIG_TRANSIT_DIR_NAME_PREFIX, CONFIG_TRANSIT_USE_PREFIX);
        configurationPanel.endGroup();

        // Truck Übersetzungsprozess
        configurationPanel.startGroup("!!Truck Übersetzungsprozess (S3 Object Store)");
        configurationPanel.addBooleanOption(CONFIG_TRANSLATION_TRUCK_ACTIVE, "!!Aktiv", false);
        configurationPanel.startGroup("!!Object Store Zugangsdaten");
        configurationPanel.addStringOption(CONFIG_TRANSLATION_TRUCK_BUCKET_NAME, "!!Bucket Name", false).setTooltip("!!Bucket Name ohne Prefix");
        configurationPanel.addStringOption(CONFIG_TRANSLATION_TRUCK_ACCESS_KEY, "!!Access Key", false);
        configurationPanel.addPasswordOption(CONFIG_TRANSLATION_TRUCK_SECRET_ACCESS_KEY, "!!Secret Access Key", false);
        configurationPanel.endGroup();
        configurationPanel.addIntegerSpinnerOption(CONFIG_TRANSLATION_TRUCK_RECONNECT_TIME, "!!Dauer bis zum nächsten Status Check in Sekunden", false, 1, 10000, 1);
        configurationPanel.addIntegerSpinnerOption(CONFIG_TRANSLATION_TRUCK_RECONNECT_ATTEMPTS, "!!Anzahl Versuche für Verbindungsaufbau", false, -1, 10000, 1).setTooltip("!!0 für keinen Verbindungsaufbau; -1 für unendlich viele Versuche");
        configurationPanel.addIntegerOption(CONFIG_TRANSLATION_TRUCK_MAX_ENTRIES, "!!Maximale Anzahl Einträge pro XML Datei", true).setTooltip("!!Bei -1 oder 0 findet kein Export statt.");
        configurationPanel.addIntegerSpinnerOption(CONFIG_TRANSLATION_TRUCK_POLLING_TIME, "!!Polling Intervall (in min)", true, 1, 1000, 1);
        configurationPanel.addIntegerSpinnerOption(CONFIG_TRANSLATION_TRUCK_MAX_DOWNLOAD_COUNT, "!!Anzahl Dateien ab wann der Import gestartet werden soll", true, 1, 1000, 1);
        configurationPanel.startGroup("!!Verzeichnisse");
        configurationPanel.addStringOption(CONFIG_TRANSLATION_TRUCK_DIR_NAME_TEXT_OUTGOING, "!!Verzeichnisname für Nutzdaten von AG an Übersetzer", true);
        configurationPanel.addStringOption(CONFIG_TRANSLATION_TRUCK_DIR_NAME_TEXT_INCOMING, "!!Verzeichnisname für Nutzdaten von Übersetzer an AG", true);
        configurationPanel.addStringOption(CONFIG_TRANSLATION_TRUCK_DIR_NAME_LOGS_OUTGOING, "!!Verzeichnisname für Logs von AG an Übersetzer", true);
        configurationPanel.addStringOption(CONFIG_TRANSLATION_TRUCK_DIR_NAME_LOGS_INCOMING, "!!Verzeichnisname für Logs von Übersetzer an AG", true);
        configurationPanel.addFileOption(CONFIG_TRANSLATION_TRUCK_ARCHIVE_DIR, "!!Verzeichnis zum Archivieren der Dateien",
                                         false, GuiFileChooserDialog.FILE_MODE_DIRECTORIES, FileChooserPurpose.OPEN, null);
        configurationPanel.endGroup();
        configurationPanel.addSeparator();
        // Externe Dateien für das ZIP-Archiv
        configurationPanel.startGroup("!!Dateien für die ZIP Archive");
        // Schemadatei
        addSchemaFileOptionToTranslationOptions(configurationPanel, CONFIG_TRANSLATION_TRUCK_SCHEMA_FILE);
        // Stylesheetdatei
        addStyleSheetFileOptionToTranslationOptions(configurationPanel, CONFIG_TRANSLATION_TRUCK_STYLE_SHEET_FILE);
        configurationPanel.endGroup();
        configurationPanel.addSeparator();
        configurationPanel.startGroup("!!Support-Informationen für die TOC Datei");
        configurationPanel.addStringOption(CONFIG_TRANSLATION_TRUCK_CUSTOM_USER_ID, "!!CustomerUserID für Daimler Truck AG", false);
        configurationPanel.addStringOption(CONFIG_TRANSLATION_TRUCK_CONTACT_PERSON_USER_ID, "!!ContactpersonUserID für Daimler Truck AG", false);
        configurationPanel.addStringOption(CONFIG_TRANSLATION_TRUCK_ADDITIONAL_CONTACT_EMAIL, "!!AdditionalContactEmail für Daimler Truck AG", false);
        configurationPanel.endGroup();
        configurationPanel.endGroup();

        // Einzelteilbilder
        iPartsPlugin.createDayOfWeekTimerGroup(configurationPanel, "!!Einzelteilbilder Teilestamm-Abgleich",
                                               CONFIG_SINGLE_PIC_PARTS_ACTIVE, CONFIG_SINGLE_PIC_PARTS_DAYS, CONFIG_SINGLE_PIC_PARTS_TIME, true);

        // Scheduler für CTT Arbeitsvorrat vorverdichten
        iPartsPlugin.createDayOfWeekTimerGroup(configurationPanel, "!!CTT Arbeitsvorrat vorverdichten",
                                               CONFIG_CALC_SINGLE_CTT_WORKBASKET_ACTIVE, CONFIG_CALC_SINGLE_CTT_WORKBASKET_DAYS, CONFIG_CALC_SINGLE_CTT_WORKBASKET_TIME, false);
        configurationPanel.addBooleanOption(CONFIG_CALC_SINGLE_CTT_WORKBASKET_DELETE, "!!Daten löschen", false);
        configurationPanel.endGroup();

        addValidatorsToConfigurationPanel(configurationPanel);

        return configurationPanel;
    }

    /**
     * Fügt dem übergebenen {@link UniversalConfigurationPanel} die Validatoren hinzu, die auf Basis der Controls des
     * Panels ihre Prüfungen durchführen
     *
     * @param configurationPanel
     */
    private static void addValidatorsToConfigurationPanel(UniversalConfigurationPanel configurationPanel) {
        configurationPanel.getPanel().setValidator(new GuiControlValidator() {
            @Override
            public ValidationState validate(AbstractGuiControl control) {
                ValidationState result = isConfigurationValid(configurationPanel);
                if (result == null) {
                    result = new ValidationState(true);
                }
                return result;
            }

            private ValidationState isConfigurationValid(UniversalConfigurationPanel configurationPanel) {
                ValidationState validationState = isTranslationExportConfigurationValid(configurationPanel);
                if (validationState != null) {
                    return validationState;
                }
                return null;
            }

            private ValidationState isTranslationExportConfigurationValid(UniversalConfigurationPanel configurationPanel) {
                // Wenn die Übersetzungen über den Object Store aktiv sind, muss die Konfiguration stimmen
                if (configurationPanel.getControlValueAsBoolean(CONFIG_TRANSLATION_TRUCK_ACTIVE.getKey())) {
                    if (!loginDataValid(configurationPanel)) {
                        return new ValidationState(false, null, "!!Für den Truck Übersetzungslauf müssen die Object Store Zugangsdaten gesetzt sein!");
                    }
                    if (StrUtils.isEmpty(configurationPanel.getControlValueAsString(iPartsImportPlugin.CONFIG_TRANSLATION_TRUCK_DIR_NAME_TEXT_OUTGOING.getKey()))) {
                        return new ValidationState(false, null, "!!Truck: Hauptverzeichnis für Übersetzungen nicht richtig konfiguriert");
                    }
                    if (!checkTranslationConfigFile(CONFIG_TRANSLATION_TRUCK_SCHEMA_FILE, configurationPanel, "xsd")) {
                        return new ValidationState(false, null, "!!Truck: Schemadatei nicht vorhanden oder fehlerhaft");
                    }
                    if (!checkTranslationConfigFile(CONFIG_TRANSLATION_TRUCK_STYLE_SHEET_FILE, configurationPanel, "xsl")) {
                        return new ValidationState(false, null, "!!Truck: Stylesheet nicht vorhanden oder fehlerhaft");
                    }
                    if (configurationPanel.getControlValueAsFile(CONFIG_TRANSLATION_TRUCK_ARCHIVE_DIR.getKey()) == null) {
                        return new ValidationState(false, null, "!!Truck: Das Archiv zum Ablegen der Dateien muss angegeben werden");
                    }

                    int maxEntries = configurationPanel.getControlValueAsInteger(CONFIG_TRANSLATION_TRUCK_MAX_ENTRIES.getKey());
                    if (maxEntries <= 0) {
                        return new ValidationState(false, null, TranslationHandler.translate("!!Truck: Konfiguration: maximale Anzahl Einträge pro " +
                                                                                             "XML Datei steht auf %1 und muss größer als 0 sein", String.valueOf(maxEntries)));
                    }
                }

                // Normaler Übersetzungsprozess
                if (configurationPanel.getControlValueAsFile(CONFIG_TRANSIT_ROOT_DIR.getKey()) == null) {
                    return new ValidationState(false, null, "!!Hauptverzeichnis für Übersetzungen nicht richtig konfiguriert");
                }
                if (!checkTranslationConfigFile(CONFIG_TRANSIT_SCHEMA_FILE, configurationPanel, "xsd")) {
                    return new ValidationState(false, null, "!!Schemadatei nicht vorhanden oder fehlerhaft");
                }
                if (!checkTranslationConfigFile(CONFIG_TRANSIT_STYLE_SHEET_FILE, configurationPanel, "xsl")) {
                    return new ValidationState(false, null, "!!Stylesheet nicht vorhanden oder fehlerhaft");
                }

                int maxEntries = configurationPanel.getControlValueAsInteger(CONFIG_TRANSIT_MAX_ENTRIES.getKey());
                if (maxEntries <= 0) {
                    return new ValidationState(false, null, TranslationHandler.translate("!!Konfiguration: maximale Anzahl Einträge pro " +
                                                                                         "XML Datei steht auf %1 und muss größer als 0 sein", String.valueOf(maxEntries)));
                }
                return null;
            }

            private boolean loginDataValid(UniversalConfigurationPanel configurationPanel) {
                Object passwordString = configurationPanel.getControlValue(CONFIG_TRANSLATION_TRUCK_SECRET_ACCESS_KEY.getKey());
                return (passwordString instanceof PasswordString) && !((PasswordString)passwordString).isEmpty()
                       && StrUtils.isValid(configurationPanel.getControlValueAsString(CONFIG_TRANSLATION_TRUCK_BUCKET_NAME.getKey()),
                                           configurationPanel.getControlValueAsString(CONFIG_TRANSLATION_TRUCK_ACCESS_KEY.getKey()));
            }
        });
    }

    public static boolean checkTranslationConfigFile(UniversalConfigOption configOption, UniversalConfigurationPanel configurationPanel, String extension) {
        File file = configurationPanel.getControlValueAsFile(configOption.getKey());
        if (file != null) {
            DWFile configFile = DWFile.get(file);
            return configFile.isFile(DWFile.DEFAULT_FILE_EXISTS_TIMEOUT) && configFile.extractExtension(false).equalsIgnoreCase(extension);
        }
        return false;
    }

    private void addTestModeGroupToTranslationOptions(UniversalConfigurationPanel configurationPanel, UniversalConfigOption dirNamePrefix, UniversalConfigOption usePrefix) {
        configurationPanel.startGroup("!!Testmodus");
        configurationPanel.addStringOption(dirNamePrefix, "!!Präfix für Testmodus-Verzeichnisse",
                                           false);
        configurationPanel.addBooleanOption(usePrefix, "!!Testmodus aktivieren (Verzeichnisse mit eingestellten Präfix verwenden)", false);
        configurationPanel.endGroup();
    }

    private void addStyleSheetFileOptionToTranslationOptions(UniversalConfigurationPanel configurationPanel, UniversalConfigOption universalConfigOption) {
        List<String[]> fileFilters = new ArrayList<>();
        GuiFileChooserTextfield configFile = configurationPanel.addFileOption(universalConfigOption, "!!Stylesheet Datei für den Export", false, GuiFileChooserDialog.FILE_MODE_FILES, FileChooserPurpose.OPEN, null);
        configFile.setTooltip("!!Dateiendung: \".xsl\"");
        fileFilters.add(new String[]{ TranslationHandler.translate("!!XSL-Dateien (*.xsl)"), "*.xsl" });
        configFile.setChoosableFileFilters(fileFilters);
    }

    private void addSchemaFileOptionToTranslationOptions(UniversalConfigurationPanel configurationPanel, UniversalConfigOption universalConfigOption) {
        List<String[]> fileFilters = new ArrayList<>();
        GuiFileChooserTextfield configFile = configurationPanel.addFileOption(universalConfigOption, "!!Schema Datei für den Export", false, GuiFileChooserDialog.FILE_MODE_FILES, FileChooserPurpose.OPEN, null);
        configFile.setTooltip("!!Dateiendung: \".xsd\"");
        fileFilters.add(new String[]{ TranslationHandler.translate("!!XSD-Dateien (*.xsd)"), "*.xsd" });
        configFile.setChoosableFileFilters(fileFilters);
    }

    /**
     * Fügt eine Intervalloption hinzu
     *
     * @param configurationPanel
     * @param groupName
     * @param configCollectTime
     * @param optionSource
     * @param configMqTimeInterval
     */
    private void addIntervalOptionGroup(UniversalConfigurationPanel configurationPanel, String groupName,
                                        UniversalConfigOption configCollectTime, String optionSource,
                                        UniversalConfigOption.UniversalConfigCustomOption configMqTimeInterval) {
        configurationPanel.startGroup(groupName);
        if (configCollectTime != null) {
            String spinnerText = TranslationHandler.translate("!!Zeit in Sekunden für das Sammeln von %1 Importen in einem Import-Log", optionSource);
            configurationPanel.addIntegerSpinnerOption(configCollectTime, spinnerText,
                                                       true, 0, 100000, 60);
        }
        String timeIntervalText = TranslationHandler.translate("!!Zeitraum für den Import von %1 Nachrichten", optionSource);
        iPartsPlugin.addTimeIntervalControl(configurationPanel, configMqTimeInterval, timeIntervalText, "!!Zum Deaktivieren Start- und Endzeit gleichsetzen oder leer lassen");
        configurationPanel.endGroup();
    }

    @Override
    public Collection<String> getRequiredPluginClassNames() {
        AbstractPlugin iPartsEditPlugin = PluginRegistry.getRegistry().getRegisteredPlugin(PLUGIN_CLASS_NAME_IPARTS_EDIT);

        if (iPartsEditPlugin != null) { // iPartsEdit Plug-in vorhanden -> Abhängigkeit wegen Reihenfolgen hinzufügen
            return Arrays.asList(PLUGIN_CLASS_NAME_IPARTS,
                                 PLUGIN_CLASS_NAME_IPARTS_EDIT);
        } else {
            return Arrays.asList(PLUGIN_CLASS_NAME_IPARTS);
        }
    }

    @Override
    public void initPlugin(ConfigBase config) {
        initPluginBase(config, EtkPluginConstants.XML_CONFIG_PATH_BASE + '/' + INTERNAL_PLUGIN_NAME);
        pluginConfig = new UniversalConfiguration(config, getConfigPath());
        mqMessageManagers = new DwList<>();
        if (isActive()) {
            boolean dialogDeltaImportAllowed = iPartsImportPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsImportPlugin.CONFIG_DIALOG_DELTA_HANDLE_IMPORT);
            MQChannelType.registerChannelType(MQ_CHANNEL_TYPE_DIALOG_IMPORT);
            registerDIALOGDeltaMQChannel(MQ_CHANNEL_TYPE_DIALOG_DELTA_IMPORT, dialogDeltaImportAllowed);
            MQChannelType.registerChannelType(MQ_CHANNEL_TYPE_DIALOG_DIRECT_IMPORT);
            registerDIALOGDeltaMQChannel(MQ_CHANNEL_TYPE_DIALOG_DIRECT_DELTA_IMPORT, dialogDeltaImportAllowed);
            MQChannelType.registerChannelType(MQ_CHANNEL_TYPE_EDS_IMPORT);
            MQChannelType.registerChannelType(MQ_CHANNEL_TYPE_PRIMUS_IMPORT);
            MQChannelType.registerChannelType(MQ_CHANNEL_TYPE_SRM_IMPORT);
        }
    }

    /**
     * Registriert einen DIALOG Delta MQ Kanal inklusive Setzen des Wertes aus der Admin-Option
     *
     * @param mqChannelType
     * @param importAllowed
     */
    private void registerDIALOGDeltaMQChannel(MQChannelType mqChannelType, boolean importAllowed) {
        mqChannelType.setImportAllowed(importAllowed);
        MQChannelType.registerChannelType(mqChannelType);
    }

    @Override
    public void applicationStarted(boolean firstInit) {
        super.applicationStarted(firstInit);

        if (isActive()) {
            if (AbstractApplication.isOnline()) {
                ClientModeFileChooserWindow.MAX_FILE_SIZE = Math.max(ClientModeFileChooserWindow.MAX_FILE_SIZE, MAX_UPLOAD_FILE_SIZE_IMPORT);

                // zentrale MQ XMLMessageListener registrieren (iPartsPlugin wird immer vor dem iPartsEditPlugin initialisiert)
                EtkProject mqProject = iPartsPlugin.getMqProject();
                if (mqProject != null) {
                    // AS-PLM XML-MQMessageManager für die XML-Importdaten (EDS, DIALOG, ...) erzeugen
                    iPartsMQMessageManager xmlMessageManager = iPartsMQMessageManager.getInstance(XML_MESSAGE_MANAGER_NAME_IMPORT_DATA);
                    xmlMessageManager.setSynchronizedMode(true);
                    setMQChannelSpecificParameters();
                    MQHelper.getInstance().addMQMessageReceiver(MQ_CHANNEL_TYPE_DIALOG_IMPORT, xmlMessageManager);
                    MQHelper.getInstance().addMQMessageReceiver(MQ_CHANNEL_TYPE_DIALOG_DELTA_IMPORT, xmlMessageManager);
                    MQHelper.getInstance().addMQMessageReceiver(MQ_CHANNEL_TYPE_EDS_IMPORT, xmlMessageManager);
                    MQHelper.getInstance().addMQMessageReceiver(MQ_CHANNEL_TYPE_PRIMUS_IMPORT, xmlMessageManager);
                    MQHelper.getInstance().addMQMessageReceiver(MQ_CHANNEL_TYPE_SRM_IMPORT, xmlMessageManager);
                    mqMessageManagers.add(xmlMessageManager);

                    // Text-MQMessageManager für die DIALOG Direct Importdaten erzeugen
                    iPartsMQMessageManager textMessageManager = iPartsMQMessageManager.getInstance(TEXT_MESSAGE_MANAGER_NAME_IMPORT_DATA);
                    textMessageManager.setSynchronizedMode(true);
                    MQHelper.getInstance().addMQMessageReceiver(MQ_CHANNEL_TYPE_DIALOG_DIRECT_IMPORT, textMessageManager);
                    MQHelper.getInstance().addMQMessageReceiver(MQ_CHANNEL_TYPE_DIALOG_DIRECT_DELTA_IMPORT, textMessageManager);
                    mqMessageManagers.add(textMessageManager);

                    Session mqSession = iPartsPlugin.getMqSession();
                    iPartsImportXMLMessageListeners.getInstance().registerXMLMessageListeners(mqProject, mqSession);
                    iPartsImportTextContentMessageListeners.getInstance().registerTextMessageListeners(mqSession);
                    iPartsTextToDIALOGMapper.initImporterMap(mqProject);
                    singlePicPartsImportScheduler = new SinglePicPartsImportScheduler(mqProject, mqSession);
                    truckBOMFoundationImportScheduler = new iPartsTruckBOMFoundationImportScheduler(mqProject, iPartsPlugin.getMqSession());
                    proValImportScheduler = new iPartsProValImportScheduler(mqProject, iPartsPlugin.getMqSession());
                    proValModelAggImportScheduler = new iPartsProValModelAggImportScheduler(mqProject, iPartsPlugin.getMqSession());
                    cttPrecalculationScheduler = new CTTWorkbasketPrecalculationScheduler(mqProject, iPartsPlugin.getMqSession());
                } else {
                    Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "EtkProject in MQ web application session does not exist. Either the iParts plug-in is not active or not initialized yet.");
                }
            }
            // ENDPOINT-Session und EtkProject für RFTS/x in einem neuen Thread erzeugen (weil sich die Session im Thread registriert)
            FrameworkThread rftsxThread = new FrameworkThread("RFTS/x session thread", Thread.NORM_PRIORITY, new Runnable() {
                @Override
                public void run() {
                    rftsxSession = FrameworkEndpointHelper.createSession(SessionType.ENDPOINT);
                    JavaViewerApplication.getInstance().startSession(rftsxSession, new CommandLine(new String[0]));
                    rftsxProject = (EtkProject)rftsxSession.getAttribute(JavaViewerApplication.SESSION_KEY_PROJECT);
                    if (rftsxProject != null) {
                        rftsxProject.getDB().setLogNameForSetActiveStacktrace("RFTS/x project");
                        rftsxProject.setDBConnectionWatchDogActive(true, LOG_CHANNEL_RFTSX);
                    }

                    RFTSXImportHelper.createInstance(rftsxProject);
                    TranslationsImportHelper.createInstance(rftsxProject).startAllMonitors();
                    TranslationsObjectStoreHelper.getInstance();
                    TranslationsExportHelper.createInstance(rftsxProject);
                }
            });
            rftsxThread.__internal_start();
            rftsxThread.waitFinished();

            // RFTSxEnabledChangedEvent registrieren und darauf reagieren
            rftsxEnabledChangedEventListener = new ObserverCallback(callbackBinder, RFTSxEnabledChangedEvent.class) {
                public void callback(ObserverCall call) {
                    boolean enabled = ((RFTSxEnabledChangedEvent)call).isEnabled();
                    // Den RFTS/x-Zustand nur in der Standard-Umgebungsvariante setzen
                    iPartsPlugin.writeBooleanPluginConfig(getPluginConfig(), CONFIG_RFTSX_HANDLE_IMPORT, enabled);

                    // RFTS/x Verzeichnis-Monitor starten bzw. stoppen (falls jeweils notwendig) abhängig von AUTO_IMPORTS_ENABLED
                    RFTSXImportHelper rftsxInstance = RFTSXImportHelper.getInstance();
                    TranslationsImportHelper translationsInstance = TranslationsImportHelper.getInstance();
                    if (iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_AUTO_IMPORTS_ENABLED)) {
                        if (rftsxInstance != null) {
                            if (rftsxInstance.getMonitorID() == null) {
                                // Wenn noch kein Monitor existiert -> neuen erzeugen
                                rftsxInstance.startRFTSxDirectoryMonitor();
                            } else if (rftsxInstance.hasToDoFiles() && !rftsxInstance.isRFTSxRunning()) {
                                // Wenn einer existiert und Dateien im todo-Verzeichnis existieren, Monitor neustarten
                                rftsxInstance.setRFTSxImportEnabled(true); // auf true setzen, damit die todo Dateien geholt werden
                                rftsxInstance.restartMonitor();
                            }
                        }
                        if (translationsInstance != null) {
                            translationsInstance.startAllMonitors();
                        }
                        TranslationsObjectStoreHelper.getInstance();
                    } else {
                        if (rftsxInstance != null) {
                            rftsxInstance.stopRFTSxDirectoryMonitor();
                        }
                        if (translationsInstance != null) {
                            translationsInstance.haltAllMonitors();
                        }
                        TranslationsObjectStoreHelper.destroyInstance();
                    }

                    if (rftsxInstance != null) {
                        rftsxInstance.setRFTSxImportEnabled(enabled);
                    }
                }
            };
            ApplicationEvents.addEventListener(rftsxEnabledChangedEventListener);
            // Den Importer erst hier starten, weil beim Intervall-Check eventuell der oben erzeugte Listener genutzt
            // wird
            RFTSXImportHelper rftsxImportHelper = RFTSXImportHelper.getInstance();
            if (rftsxImportHelper != null) {
                rftsxImportHelper.startRFTSxDirectoryMonitorWithIntervalCheck();
            }

            // RFTSxEnabledChangedEvent registrieren und darauf reagieren
            dialogDeltaEnabledChangedEventListener = new ObserverCallback(callbackBinder, DIALOGDeltaEnabledChangedEvent.class) {
                public void callback(ObserverCall call) {
                    boolean enabled = ((DIALOGDeltaEnabledChangedEvent)call).isEnabled();
                    // Den Zustand nur in der Standard-Umgebungsvariante setzen
                    iPartsPlugin.writeBooleanPluginConfig(getPluginConfig(), CONFIG_DIALOG_DELTA_HANDLE_IMPORT, enabled);
                    handleDeltaQueueAfterEnabledChangedEvent(MQ_CHANNEL_TYPE_DIALOG_DELTA_IMPORT, enabled);
                    handleDeltaQueueAfterEnabledChangedEvent(MQ_CHANNEL_TYPE_DIALOG_DIRECT_DELTA_IMPORT, enabled);
                }
            };
            ApplicationEvents.addEventListener(dialogDeltaEnabledChangedEventListener);

            restartSinglePicPartsImportThread();
            restartTruckBOMFoundationImportThread();
            restartProValImportThread();
            restartProValModelAggImportThread();
            restartCttPreCalculationThread();
        }
    }

    /**
     * Startet/Stopp das Importieren des übergebenen MQ Kanals
     *
     * @param mqChannelType
     * @param enabled
     */
    private void handleDeltaQueueAfterEnabledChangedEvent(MQChannelType mqChannelType, boolean enabled) {
        mqChannelType.setImportAllowed(enabled);
        // Consumer Thread für Delta Ladung starten/stoppen, damit eingehende Nachrichten angestaut werden.
        // Wenn der Consumer wieder aktiviert wird, startet auch die Bearbeitung wieder
        MQChannel dialogDeltaImportChannel = MQHelper.getInstance().getChannel(mqChannelType);
        if (dialogDeltaImportChannel != null) {
            dialogDeltaImportChannel.startOrStopConsumer();
        }
    }

    private void setMQChannelSpecificParameters() {
        addTimeIntervalsToChannel();
        addMessageStorageParametersForChannels();
        addOmitLogMessageParameterForChannels();
    }

    /**
     * Setzt definierte, kanalspezifische Zeitintervalle für das Abholen von MQ Nachrichten
     */
    private void addTimeIntervalsToChannel() {
        // Intervalle für die Importer aus der Admin-Konfiguration auslesen
        setTimeInterval(getTimeIntervalForPRIMUS(), MQ_CHANNEL_TYPE_PRIMUS_IMPORT);
        setTimeInterval(getTimeIntervalForSRM(), MQ_CHANNEL_TYPE_SRM_IMPORT);
        setTimeInterval(getTimeIntervalForBOMDB(), MQ_CHANNEL_TYPE_EDS_IMPORT);
        setTimeInterval(getTimeIntervalForDIALOG(), MQ_CHANNEL_TYPE_DIALOG_DIRECT_DELTA_IMPORT);
    }

    /**
     * Setzt das Zeitintervall für den übergebenen {@link MQChannelType}
     *
     * @param timeInterval
     * @param mqChannelType
     */
    private void setTimeInterval(iPartsTimeInterval timeInterval, MQChannelType mqChannelType) {
        if ((mqChannelType != null) && (timeInterval != null)) {
            mqChannelType.setImportIntervalStart(timeInterval.getStartTimeInSeconds());
            mqChannelType.setImportIntervalEnd(timeInterval.getEndTimeInSeconds());
        }
    }

    public static iPartsTimeInterval getTimeIntervalForConfigOption(UniversalConfigOption.UniversalConfigCustomOption configOption) {
        return (iPartsTimeInterval)getPluginConfig().getConfigValue(configOption);
    }

    public static iPartsTimeInterval getTimeIntervalForPRIMUS() {
        return getTimeIntervalForConfigOption(CONFIG_MQ_PRIMUS_IMPORT_TIME_INTERVAL);
    }

    public static iPartsTimeInterval getTimeIntervalForSRM() {
        return getTimeIntervalForConfigOption(CONFIG_MQ_SRM_IMPORT_TIME_INTERVAL);
    }

    public static iPartsTimeInterval getTimeIntervalForRFTSX() {
        return getTimeIntervalForConfigOption(CONFIG_RFTSX_IMPORT_TIME_INTERVAL);
    }

    public static iPartsTimeInterval getTimeIntervalForBOMDB() {
        return getTimeIntervalForConfigOption(CONFIG_MQ_BOMDB_IMPORT_TIME_INTERVAL);
    }

    public static iPartsTimeInterval getTimeIntervalForDIALOG() {
        return getTimeIntervalForConfigOption(CONFIG_MQ_DIALOG_IMPORT_TIME_INTERVAL);
    }

    /**
     * Legt fest, ob MQ Kanäle die Original-Nachrichten speichern sollen
     */
    private void addMessageStorageParametersForChannels() {
        // Aktuell werden nur bei PRIMUS die Nachrichten nicht gespeichert
        MQ_CHANNEL_TYPE_PRIMUS_IMPORT.setStoreOriginalMessage(false);
        MQ_CHANNEL_TYPE_SRM_IMPORT.setStoreOriginalMessage(false);
    }

    /**
     * Legt fest, ob MQ Kanäle die Standard-Log-Ausgaben pro empfangener Nachricht ausgeben sollen
     */
    private void addOmitLogMessageParameterForChannels() {
        MQ_CHANNEL_TYPE_DIALOG_DIRECT_IMPORT.setOmitLogMessageForEverySingleMessage(true);
        MQ_CHANNEL_TYPE_DIALOG_DIRECT_DELTA_IMPORT.setOmitLogMessageForEverySingleMessage(true);
    }

    @Override
    public void releaseReferences() {
        if (isActive()) {
            if (mqMessageManagers != null) {
                for (iPartsMQMessageManager manager : mqMessageManagers) {
                    MQHelper.getInstance().removeMQMessageReceiverFromAllChannels(manager);
                }
                mqMessageManagers = null;
            }
            iPartsImportXMLMessageListeners.getInstance().deregisterXMLMessageListeners();
            iPartsImportTextContentMessageListeners.getInstance().deregisterTextMessageListeners();

            ApplicationEvents.clearEventListeners(callbackBinder);
            rftsxEnabledChangedEventListener = null;
            dialogDeltaEnabledChangedEventListener = null;

            RFTSXImportHelper rftsxInstance = RFTSXImportHelper.getInstance();
            if (rftsxInstance != null) {
                rftsxInstance.stopRFTSxDirectoryMonitor();
            }

            TranslationsImportHelper translationsImportInstance = TranslationsImportHelper.getInstance();
            if (translationsImportInstance != null) {
                translationsImportInstance.stopAllMonitors();
            }
            TranslationsObjectStoreHelper.destroyInstance();
            TranslationsExportHelper translationsExportInstance = TranslationsExportHelper.getInstance();
            if (translationsExportInstance != null) {
                translationsExportInstance.deleteTempDirectories();
            }

            if (rftsxProject != null) {
                rftsxProject.setDBConnectionWatchDogActive(false, LOG_CHANNEL_RFTSX);
            }
            if (rftsxSession != null) {
                SessionManager.getInstance().destroySession(rftsxSession);
                rftsxSession = null;
            }
            rftsxProject = null;
        }
        super.releaseReferences();
    }

    @Override
    public String getRequiredInternalAppName() {
        return FrameworkMain.INTERNAL_APP_NAME;
    }

    @Override
    public boolean checkLicense() {
        // iParts Lizenzoption
        return (LicenseConfig.getInstance() == null) || LicenseConfig.getInstance().licenseFunctionExists(LICENSE_KEY_IPARTS);
    }

    @Override
    public LogChannels[] getPluginLogChannels() {
        return new LogChannels[]{ LOG_CHANNEL_DEBUG, LOG_CHANNEL_RFTSX, LOG_CHANNEL_FACTORYDATA_AUTO_RELEASE,
                                  LOG_CHANNEL_TRUCK_BOM_FOUNDATION_IMPORT_SCHEDULER, LOG_CHANNEL_TRUCK_TRANSLATIONS,
                                  LOG_CHANNEL_CTT_PRECALC };
    }

    @Override
    public DesignImage[] getPluginImages() {
        Collection<? extends DesignImage> images = ImportDefaultImages.getImages();
        return images.toArray(new DesignImage[images.size()]);
    }

    @Override
    public void adjustTheme(Theme theme) {
        if (theme instanceof MaterialTheme) {
            MaterialTheme materialTheme = (MaterialTheme)theme;

            String materialSubDir = "material";
            materialTheme.addMaterialIconsToHashSet(ImportDefaultImages.class, materialSubDir, "import_active_16px",
                                                    new MaterialThemeIconComponent(ImportDefaultImages.importToolbarButton, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(ImportDefaultImages.class, materialSubDir, "import_inactive_16px",
                                                    new MaterialThemeIconComponent(ImportDefaultImages.importStoppedToolbarButton, null,
                                                                                   null, null));
        }
    }

    @Override
    public void modifySecondToolbar(final AbstractJavaViewerFormIConnector connector, StartPageType activeFormType) {
        if (!iPartsRight.IMPORT_MASTER_DATA.checkRightInSession()) {
            return;
        }

        GuiToolbarManager manager = connector.getToolbarManager();
        if (manager != null) {
            if ((activeFormType == StartPageType.PARTS) || (activeFormType == StartPageType.SEARCH)) {
                final GuiToolButton button = AutoImportSettingsForm.createAutoImportSettingsToolButton(connector, connector.getActiveForm());
                manager.insertButtonAfter(new GuiToolButton(ToolButtonType.SEPARATOR), button.getAlias() + "Separator",
                                          iPartsToolbarButtonAlias.FILTER_IPARTS.getAlias());
                manager.insertButtonAfter(button, button.getAlias(), button.getAlias() + "Separator");
            }
        }
    }

    @Override
    public boolean needsStaticConnectionUpdates() {
        return ((getProject() != null) && getProject().isEditModeActive()) || isImportPluginStandalone();
    }

    @Override
    public ShowStartWindowMoment getShowStartWindowMoment() {
        return ShowStartWindowMoment.BEFORE_LANGUAGE_SELECTION;
    }

    @Override
    public boolean showStartWindow() {
        if (isImportPluginStandalone()) {
            JobsMainForm jobsMainForm = new JobsMainForm(new AbstractJavaViewerFormConnector(null), null);
            jobsMainForm.getMainWindow().setVisible(true);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void configurationChanged() {
        super.configurationChanged();
        // Annahme: Das Plugin wird nie beendet.
        if (isActive() && AbstractApplication.isOnline()) {
            if (rftsxProject != null) {
                rftsxProject.updateDBConnectionIfNecessary("RFTS/x project", LOG_CHANNEL_RFTSX);
            }

            RFTSXImportHelper rftsxInstance = RFTSXImportHelper.getInstance();
            if (rftsxInstance != null) {
                rftsxInstance.configurationChanged();
            }

            TranslationsImportHelper translationsInstance = TranslationsImportHelper.getInstance();
            if (translationsInstance != null) {
                translationsInstance.configurationChanged();
            }
            TranslationsObjectStoreHelper.destroyInstance();
            TranslationsObjectStoreHelper.getInstance();

            // createDirectoryMonitor() wird im Listener des Events aufgerufen, wenn nicht bereits in Verzeichnis-Monitor läuft
            ApplicationEvents.fireEventInAllProjects(new RFTSxEnabledChangedEvent(getPluginConfig().getConfigValueAsBoolean(CONFIG_RFTSX_HANDLE_IMPORT)),
                                                     true, true, null);

            ApplicationEvents.fireEventInAllProjects(new DIALOGDeltaEnabledChangedEvent(getPluginConfig().getConfigValueAsBoolean(CONFIG_DIALOG_DELTA_HANDLE_IMPORT)),
                                                     true, true, null);

            setMQChannelSpecificParameters();
        }

        if (isActive()) {
            restartSinglePicPartsImportThread();
            restartTruckBOMFoundationImportThread();
            restartProValImportThread();
            restartProValModelAggImportThread();
            restartCttPreCalculationThread();
        }
    }

    @Override
    public boolean setActiveState(boolean active) {
        boolean activeStateChanged = super.setActiveState(active);

        if (activeStateChanged) {
            if (singlePicPartsImportScheduler != null) {
                if (active) {
                    restartSinglePicPartsImportThread();
                } else {
                    singlePicPartsImportScheduler.stopThread();
                }
            }

            if (truckBOMFoundationImportScheduler != null) {
                if (active) {
                    restartTruckBOMFoundationImportThread();
                } else {
                    truckBOMFoundationImportScheduler.stopThread();
                }
            }

            if (proValImportScheduler != null) {
                if (active) {
                    restartProValImportThread();
                } else {
                    proValImportScheduler.stopThread();
                }
            }

            if (proValModelAggImportScheduler != null) {
                if (active) {
                    restartProValModelAggImportThread();
                } else {
                    proValModelAggImportScheduler.stopThread();
                }
            }

            if (cttPrecalculationScheduler != null) {
                if (active) {
                    restartCttPreCalculationThread();
                } else {
                    cttPrecalculationScheduler.stopThread();
                }
            }
        }

        return activeStateChanged;
    }

    @Override
    public void modifyMenu(MenuManager manager) {
        modifyImportMenu(manager);

        boolean isEditMasterDataAllowed = iPartsRight.EDIT_MASTER_DATA.checkRightInSession();
        if (AbstractApplication.isOnline() && isEditMasterDataAllowed) {
            // Separator
            manager.addMenuFunctionAfter(IPARTS_MENU_NAME_TEST, null, null, false, null, MenuManager.MENU_NAME_HELP);
            manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, "!!RFTS/x Monitor neu starten", null, false, restartRFTSx(),
                                          MenuManager.MENU_NAME_HELP);
        }

        if (iPartsRight.VIEW_DATABASE_TOOLS.checkRightInSession()) {
            // Separator
            GuiMenuItem dbToolsMenuItem = manager.getMenuItem(IPARTS_MENU_NAME_TEST, "!!Datenbank-Tools");
            if (dbToolsMenuItem == null) {
                dbToolsMenuItem = manager.addMenuFunction(IPARTS_MENU_NAME_TEST, "!!Datenbank-Tools", null);
            }

            dbToolsMenuItem.addChild(manager.createMenuItem("!!Leere Module aus Datenbank entfernen...", deleteEmptyModules()));
            dbToolsMenuItem.addChild(manager.createMenuItem("!!DA_PRODUCT_MODELs bereinigen (DAIMLER-5102)...", moveDataFromProductModelsToModel()));
            dbToolsMenuItem.addChild(manager.createMenuItem("!!ES1 und/oder ES2 am Teilestamm bereinigen...", convertEs1AndEs2Keys()));
            dbToolsMenuItem.addChild(manager.createMenuItem("!!Suche MAD Daten nach DIALOG Import (pro Baureihe)", searchMADDataAfterDIALOGImport()));
            dbToolsMenuItem.addChild(manager.createMenuItem("!!Zeichnungen in DIALOG Modulen aufsteigend sortieren", reorderImagesForAssemblies()));
            dbToolsMenuItem.addChild(manager.createMenuItem("!!Ersetzungen an Vorgänger/ Nachfolger-Ständen bereinigen...", cleanupReplacements()));

            GuiMenuItem workbasketCalculationMenu = manager.addMenuFunction(IPARTS_MENU_NAME_TEST, "!!SAA-Arbeitsvorrat vorverdichten", null);
            dbToolsMenuItem.addChild(workbasketCalculationMenu);

            workbasketCalculationMenu.addChild(manager.createMenuItem("!!EDS/BCS Arbeitsvorrat vorverdichten...", calculateInitialWorkbasketSingle(iPartsImportDataOrigin.EDS)));
            workbasketCalculationMenu.addChild(manager.createMenuItem("!!MBS Arbeitsvorrat vorverdichten...", calculateInitialWorkbasketSingle(iPartsImportDataOrigin.SAP_MBS)));
            workbasketCalculationMenu.addChild(manager.createMenuItem("!!CTT Arbeitsvorrat vorverdichten...", calculateInitialWorkbasketSingle(iPartsImportDataOrigin.SAP_CTT)));
            workbasketCalculationMenu.addChild(manager.createMenuItem("!!EDS/BCS, MBS und CTT Arbeitsvorrat vorverdichten...", calculateInitialSAAWorkbasketForEDSandMBSandCTT()));

            dbToolsMenuItem.addChild(manager.createMenuItem("!!EDS/BCS Bemerkungen konvertieren...", convertEDSBCSRemarksToMultiLang()));
        }

        if (isEditMasterDataAllowed) {
            // Separator
            manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, null, null, false, null, MenuManager.MENU_NAME_HELP);

            // EPC Menü unten anhängen
            GuiMenuItem dbEPCMenuItem = manager.getMenuItem(IPARTS_MENU_NAME_TEST, "!!EPC Funktionen");
            if (dbEPCMenuItem == null) {
                dbEPCMenuItem = manager.addMenuFunction(IPARTS_MENU_NAME_TEST, "!!EPC Funktionen", null);
            }
            dbEPCMenuItem.addChild(manager.createMenuItem("!!Korrigiere alle Titel-Texte aller EPC Produkte", correctProductTitle()));
            dbEPCMenuItem.addChild(manager.createMenuItem("!!Bestimme Aggregatetyp für EPC Produkte via Baumuster", calcAggTypeForEPCProductFromModels()));

            manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, null, null, false, null, MenuManager.MENU_NAME_HELP);
            manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, "!!Automatische Freigabe von Werkseinsatzdaten...", null,
                                          false, autoReleaseModifiedFactoryData(), MenuManager.MENU_NAME_HELP);
        }
    }

    /**
     * Konvertiert die EDS/BCS Bemerkungen in sprachspezifische DB Felder. Hierbei werden die Texte aus DESR_REMARK und
     * DEMR_REMARK als deutscher Text in DESR_TEXT und DEMR_TEXT abgelegt
     *
     * @return
     */
    private EtkFunction convertEDSBCSRemarksToMultiLang() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                ModalResult result = MessageDialog.showYesNo("!!Möchten Sie wirklich die festen EDS/BCS Bemerkungen in mehrsprachige DB-Felder konvertieren?");
                if (result != ModalResult.YES) {
                    return;
                }
                final EtkProject project = owner.getProject();
                final EtkMessageLogForm messageLogForm = new EtkMessageLogForm("!!Bemerkungen konvertieren",
                                                                               "!!Konvertiere alle SAA/Baukasten Bemerkungen...",
                                                                               null);
                messageLogForm.showModal(thread -> {
                    messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Starte Konvertierung: %1", CalendarUtils.format(System.currentTimeMillis(),
                                                                                                                                               CalendarUtils.DEFAULT_STYLE_LOCALE_PATTERN,
                                                                                                                                               Language.DE.getCode())));
                    int totalRemarkCount = handleSaaRemarks(project, messageLogForm);
                    totalRemarkCount += handleConstKitRemarks(project, messageLogForm);

                    messageLogForm.getMessageLog().hideProgress();
                    messageLogForm.getMessageLog().fireMessageWithSeparators(TranslationHandler.translate("!!Konvertierung von %1 Bemerkungen abgeschlossen: %2",
                                                                                                          String.valueOf(totalRemarkCount),
                                                                                                          CalendarUtils.format(System.currentTimeMillis(),
                                                                                                                               CalendarUtils.DEFAULT_STYLE_LOCALE_PATTERN,
                                                                                                                               Language.DE.getCode())));
                });
            }

            /**
             * Konvertiert die EDS/BCS Bemerkungen zu SAAs
             *
             * @param project
             * @param messageLogForm
             * @return
             */
            private int handleSaaRemarks(EtkProject project, EtkMessageLogForm messageLogForm) {
                messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Lade alle SAA Bemerkungen..."));
                // Alle Einzel-SAAs bestimmen, die in der Tabelle vorkommen
                Set<String> saaSet = retrieveDistinctDBValues(project, TABLE_DA_EDS_SAA_REMARKS, FIELD_DESR_SAA);
                messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Es wurden Bemerkungen zu %1 " +
                                                                                        "SAAs gefunden. Starte Konvertierung " +
                                                                                        "der SAA Bemerkungen...",
                                                                                        String.valueOf(saaSet.size())));

                int saaCount = 0;
                int remarkCount = 0;
                LRUMap textCache = new LRUMap(10000);
                iPartsDataSaaRemarksList remarksToStore = new iPartsDataSaaRemarksList();
                // Pro SAA alle Bemerkungen laden und den Text aus DESR_REMARK als DE Text in DESR_TEXT ablegen
                for (String saa : saaSet) {
                    if (messageLogForm.isCancelled()) {
                        break;
                    }
                    // Alle Bemerkungen zur SAA laden
                    iPartsDataSaaRemarksList remarksForSaa = iPartsDataSaaRemarksList.loadAllRemarksForSaa(project, saa);
                    // Jede Bemerkung durchlaufen
                    remarksForSaa.forEach(remark -> {
                        if (!remark.getFieldValue(FIELD_DESR_TEXT).isEmpty()) { // Bemerkung wurde schon konvertiert
                            return;
                        }

                        // Handelt es sich um eine Bemerkung, die aus der TB.f kommt, dann die Bemerkung überspringen,
                        // da der TB.f Importer die Texte schon richtig anlegt
                        if (remark.getAsId().getRemarkNo().length() == 6) {
                            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Bemerkung für %1 " +
                                                                                                    "übersprungen, da der " +
                                                                                                    "Text aus der TB.f kommt.",
                                                                                                    remark.getAsId().toStringForLogMessages()));
                            return;
                        }
                        // Wenn kein Text existiert, dann brauch man auch nicht zu konvertieren
                        String text = remark.getFieldValue(FIELD_DESR_REMARK);
                        if (StrUtils.isEmpty(text)) {
                            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Bemerkung für %1 " +
                                                                                                    "übersprungen, da kein " +
                                                                                                    "Text existiert.",
                                                                                                    remark.getAsId().toStringForLogMessages()));
                            return;
                        }
                        // Text konvertieren
                        addMultiLangToList(project, text, remark, TABLE_DA_EDS_SAA_REMARKS, FIELD_DESR_TEXT,
                                           remarksToStore, textCache);
                    });
                    remarkCount += remarksToStore.size();
                    // Angepasste Bemerkungen speichern
                    if (!saveDataObjects(project, remarksToStore)) {
                        messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Die Bemerkungen für %1" +
                                                                                                " konnten nicht gespeichert " +
                                                                                                "werden!", saa));
                    }
                    saaCount++;
                    messageLogForm.getMessageLog().fireProgress(saaCount, saaSet.size(), "", true, true);
                }
                messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Konvertierung von %1 Bemerkungen " +
                                                                                        "zu %2 SAAs abgeschlossen",
                                                                                        String.valueOf(remarkCount),
                                                                                        String.valueOf(saaCount)));
                return remarkCount;
            }

            private int handleConstKitRemarks(EtkProject project, EtkMessageLogForm messageLogForm) {
                messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Lade alle Baukasten Bemerkungen..."));
                // Alle Einzel-Baukasten bestimmen, die in der Tabelle vorkommen
                Set<String> constKitSet = retrieveDistinctDBValues(project, TABLE_DA_EDS_MAT_REMARKS, FIELD_DEMR_PART_NO);
                messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Es wurden Bemerkungen zu " +
                                                                                        "%1 Baukästen gefunden. Starte " +
                                                                                        "Konvertierung der Baukasten " +
                                                                                        "Bemerkungen...",
                                                                                        String.valueOf(constKitSet.size())));

                iPartsDataMaterialRemarkList remarksToStore = new iPartsDataMaterialRemarkList();
                int constKitCount = 0;
                int remarkCount = 0;
                LRUMap textCache = new LRUMap(10000);
                for (String constKit : constKitSet) {
                    if (messageLogForm.isCancelled()) {
                        break;
                    }
                    // Alle Bemerkungen zum Baukasten laden
                    iPartsDataMaterialRemarkList remarksForConstKit = iPartsDataMaterialRemarkList.loadAllRemarksForConstKit(project, constKit);
                    // Jede Bemerkung durchlaufen
                    remarksForConstKit.forEach(remark -> {
                        if (!remark.getFieldValue(FIELD_DEMR_TEXT).isEmpty()) { // Bemerkung wurde schon konvertiert
                            return;
                        }

                        // Handelt es sich um eine Bemerkung, die aus der TB.f kommt, dann die Bemerkung überspringen,
                        // da der TB.f Importer die Texte schon richtig anlegt
                        if (remark.getAsId().getRemarkNo().length() == 6) {
                            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Bemerkung für " +
                                                                                                    "%1 übersprungen, da " +
                                                                                                    "der Text aus der TB.f " +
                                                                                                    "kommt.",
                                                                                                    remark.getAsId().toStringForLogMessages()));
                            return;
                        }
                        // Wenn kein Text existiert, dann brauch man auch nicht zu konvertieren
                        String text = remark.getFieldValue(FIELD_DEMR_REMARK);
                        if (StrUtils.isEmpty(text)) {
                            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Bemerkung für" +
                                                                                                    " %1 übersprungen, da " +
                                                                                                    "kein Text existiert.",
                                                                                                    remark.getAsId().toStringForLogMessages()));
                            return;
                        }
                        // Text konvertieren
                        addMultiLangToList(project, text, remark, TABLE_DA_EDS_MAT_REMARKS, FIELD_DEMR_TEXT,
                                           remarksToStore, textCache);
                    });
                    remarkCount += remarksToStore.size();
                    // Angepasste Bemerkungen speichern
                    if (!saveDataObjects(project, remarksToStore)) {
                        messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Die Bemerkungen " +
                                                                                                "für %1 konnten nicht " +
                                                                                                "gespeichert werden!", constKit));
                    }
                    constKitCount++;
                    messageLogForm.getMessageLog().fireProgress(constKitCount, constKitSet.size(), "", true, true);
                }
                messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Konvertierung von %1 " +
                                                                                        "Bemerkungen zu %2 Baukästen " +
                                                                                        "abgeschlossen",
                                                                                        String.valueOf(remarkCount),
                                                                                        String.valueOf(constKitCount)));
                return remarkCount;
            }

            /**
             * Bestimmt die eindeutigen Werte zur übergebenen Spalte in der übergebenen Tabelle
             *
             * @param project
             * @param tableName
             * @param fieldName
             * @return
             */
            private Set<String> retrieveDistinctDBValues(EtkProject project, String tableName, String fieldName) {
                DBSQLQuery query = project.getDB().getDBForTable(tableName).getNewQuery();
                query.selectDistinct(fieldName).from(tableName);
                query.orderByDescending(fieldName);
                Set<String> dbValues = new TreeSet<>();
                try (DBDataSet dbSet = query.executeQuery()) {
                    String[] fieldNames = { fieldName };
                    while (dbSet.next()) {
                        EtkRecord rec = dbSet.getRecord(fieldNames);
                        String value = rec.getField(fieldName).getAsString();
                        dbValues.add(value);
                    }
                }
                return dbValues;
            }

            /**
             * Setzt den übergebenen Text als DE Text in das übergebene sprachspezifischen Feld. Bevor der Text als
             * neues {@link EtkMultiSprache} Objekt (inkl. neuer Text-Id) angelegt wird, wird geprüft, ob der Text schon
             * einmal während der Konvertierung vorkam oder schon in der DB existiert.
             *
             * @param project
             * @param text
             * @param remark
             * @param tableName
             * @param textField
             * @param remarksToStore
             * @param textCache
             * @param <T>
             */
            private <T extends EtkDataObject> void addMultiLangToList(EtkProject project, String text, T remark,
                                                                      String tableName, String textField,
                                                                      EtkDataObjectList<T> remarksToStore, LRUMap textCache) {

                EtkMultiSprache multiLang = EDSRemarkTextHelper.determineMultiLangForEDSBCSRemark(project, text,
                                                                                                  textCache, tableName,
                                                                                                  textField);
                // DE Text setzen
                multiLang.setText(Language.DE, text);
                // Im Cache ablegen
                textCache.put(text, multiLang);
                // Die Bemerkung aktualisieren und ablegen
                remark.setFieldValueAsMultiLanguage(textField, multiLang, DBActionOrigin.FROM_EDIT);
                remarksToStore.add(remark, DBActionOrigin.FROM_EDIT);
            }
        };
    }

    /**
     * Sortiert die Zeichnungen in DIALOG Modulen aufsteigend nach Bildnummer
     *
     * @return
     */
    private EtkFunction reorderImagesForAssemblies() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                final EtkProject project = owner.getProject();
                final EtkMessageLogForm messageLogForm = new EtkMessageLogForm("!!Modulzeichnungen aufsteigend sortieren",
                                                                               "!!Suche und verarbeite alle Zeichnungen zu vorhandenen DIALOG Modulen...",
                                                                               null);

                messageLogForm.showModal(new FrameworkRunnable() {
                    @Override
                    public void run(FrameworkThread thread) {
                        messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Lade alle DIALOG Module..."));
                        // Alle DIALOG Module bestimmen
                        iPartsDataModuleList allDIALOGModules = new iPartsDataModuleList();
                        allDIALOGModules.searchAndFill(project, TABLE_DA_MODULE, new String[]{ FIELD_DM_DOCUTYPE },
                                                       new String[]{ iPartsDocumentationType.DIALOG.getDBValue() },
                                                       DBDataObjectList.LoadType.ONLY_IDS, DBActionOrigin.FROM_DB);
                        messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!%1 DIALOG Module geladen",
                                                                                                String.valueOf(allDIALOGModules.size())));
                        EtkDataImageList imagesToStore = EtkDataObjectFactory.createDataImageList();
                        int moduleCounter = 0; // Zähler für alle Module
                        int moduleCounterWithNewImagesOrder = 0; // Zähler für Module, in denen die Reihenfolge der Zeichnungen angepasst wurde
                        messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Starte Korrektur der Zeichnungsreihenfolgen..."));

                        // Durchlaufe alle Module und schaue, ob die Zeichnungen aufsteigend nach Bildnummer sortiert sind
                        for (iPartsDataModule dialogModule : allDIALOGModules) {
                            // Check, ob der Benutzer die Korrektur abgebrochen hat
                            if (Thread.currentThread().isInterrupted()) {
                                break;
                            }

                            // Liste für alle Zeichnungen, die gelöscht und neu angelegt werden
                            EtkDataImageList images = EtkDataObjectFactory.createDataImageList();
                            // Alle Zeichnungen für das aktuelle Modul laden
                            images.loadImages(project, new AssemblyId(dialogModule.getAsId().getModuleNumber(), ""), false);
                            moduleCounter++;
                            messageLogForm.getMessageLog().fireProgress(moduleCounter, allDIALOGModules.size(), "", true, true);

                            // Korrektur macht nur Sinn, wenn mind. zwei Zeichnungen existieren
                            if (images.size() < 2) {
                                continue;
                            }

                            List<EtkDataImage> imagesList = images.getAsList();
                            // Zeichnungen nach Bildnummer sortieren
                            Collections.sort(imagesList, new Comparator<EtkDataImage>() {
                                @Override
                                public int compare(EtkDataImage o1, EtkDataImage o2) {
                                    String firstSortString = getSortStringForImage(project, o1);
                                    String secondSortString = getSortStringForImage(project, o2);
                                    return firstSortString.compareTo(secondSortString);
                                }
                            });

                            // Überprüfung, ob die Reihenfolge der Zeichnungen stimmt, mit Korrektur falls notwendig
                            boolean foundImagesInWrongOrder = false;
                            for (int i = 0; i < imagesList.size(); i++) {
                                // Das aktuelle Bild
                                EtkDataImage image = imagesList.get(i);
                                // Die aktuelle Blattnummer
                                int imageOrder = StrUtils.strToIntDef(image.getBlattNr(), -1);
                                // Reihenfolge in der sortierten Liste
                                int correctOrder = i + 1;

                                // Falls unterschiedlich, dann muss die Reihenfolge angepasst werden
                                if (imageOrder != correctOrder) {
                                    foundImagesInWrongOrder = true;
                                    // einen Klon erzeugen, damit das Original gelöscht werden kann
                                    EtkDataImage clone = image.cloneMe(project);
                                    imagesToStore.delete(image, true, DBActionOrigin.FROM_EDIT);
                                    // neue Blattnummer vergeben
                                    clone.setFieldValue(FIELD_I_BLATT, EtkDbsHelper.formatLfdNr(correctOrder), DBActionOrigin.FROM_EDIT);
                                    // false setzen, damit ein vorher neu erzeugter Datensatz nicht versehentlich gelöscht wird
                                    clone.setDeleteOldId(false);
                                    imagesToStore.add(clone, DBActionOrigin.FROM_EDIT);
                                }
                            }

                            if (foundImagesInWrongOrder) {
                                moduleCounterWithNewImagesOrder++;
                            }
                            if (imagesToStore.size() > 500) {
                                saveDataObjects(project, imagesToStore);
                            }
                        }

                        saveDataObjects(project, imagesToStore);
                        messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Korrektur beendet. In %1 DIALOG Modulen wurde" +
                                                                                                " die Reihenfolge der Zeichnungen korrigiert.",
                                                                                                String.valueOf(moduleCounterWithNewImagesOrder)));
                        if (moduleCounterWithNewImagesOrder > 0) {
                            ApplicationEvents.fireEventInAllProjectsAndAllClusters(new DataChangedEvent());
                        }
                    }
                });
            }

            /**
             * Liefert den Sortier-String basierend auf der Bildnummer. Handelt es sich um eine PV Nummer (aus AS-PLM),
             * dann wird in der Referenztabelle nach der passenden DASTi Nummer gesucht.
             * @param project
             * @param image
             * @return
             */
            private String getSortStringForImage(EtkProject project, EtkDataImage image) {
                String imageNumber = image.getImagePoolNo();
                if (StrUtils.isValid(imageNumber) && !imageNumber.startsWith("B")) {
                    iPartsDataPicReferenceList picRefList = iPartsDataPicReferenceList.loadWithPicRefNumberAndStates(project, imageNumber,
                                                                                                                     image.getImagePoolVer(), null);
                    if (!picRefList.isEmpty()) {
                        imageNumber = picRefList.get(0).getAsId().getPicReferenceNumber();
                    }
                }
                return Utils.toSortString(imageNumber);
            }
        };
    }

    /**
     * Speichert die übergebenen Objekte in der Datenbank und leer danach die übergebene Liste
     *
     * @param project
     * @param dataObjects
     */
    private boolean saveDataObjects(EtkProject project, EtkDataObjectList<? extends EtkDataObject> dataObjects) {
        project.getDbLayer().startTransaction();
        project.getDbLayer().startBatchStatement();
        try {
            if (!dataObjects.isEmpty()) {
                dataObjects.saveToDB(project);
                dataObjects.clear(DBActionOrigin.FROM_EDIT);
            }
            project.getDbLayer().endBatchStatement();
            project.getDbLayer().commit();
        } catch (Exception e) {
            project.getDbLayer().cancelBatchStatement();
            project.getDbLayer().rollback();
            return false;
        }
        return true;
    }

    private EtkFunction correctProductTitle() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                final EtkProject project = owner.getProject();
                final EtkMessageLogForm messageLogForm = new EtkMessageLogForm("!!EPC Produkttitel Korrektur",
                                                                               "!!Suche und verarbeite alle EPC Produkte...", null);

                messageLogForm.showModal(new FrameworkRunnable() {
                    @Override
                    public void run(FrameworkThread thread) {
                        messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Lade alle EPC " +
                                                                                                "Produkte..."));
                        iPartsDataProductList allEPCProducts = iPartsDataProductList.loadAllEPCProductList(project);
                        messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!%1 EPC Produkte" +
                                                                                                " geladen.",
                                                                                                String.valueOf(allEPCProducts.size())));
                        iPartsMainImportHelper mainImportHelper = new iPartsMainImportHelper(project, new HashMap<String, String>(), null);
                        project.getDbLayer().startTransaction();
                        project.getDbLayer().startBatchStatement();
                        try {
                            int productCounter = 0;
                            for (iPartsDataProduct product : allEPCProducts) {
                                mainImportHelper.setProductTitle(product, "");
                                product.saveToDB();
                                productCounter++;
                                messageLogForm.getMessageLog().fireProgress(productCounter, allEPCProducts.size(), "", true, true);
                            }

                            messageLogForm.getMessageLog().hideProgress();
                            messageLogForm.getMessageLog().fireMessageWithSeparators(TranslationHandler.translate("!!Korrektur der Titel-Texte abgeschlossen"));
                            // Produkt-Cache löschen
                            ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PRODUCT));
                            ApplicationEvents.fireEventInAllProjectsAndAllClusters(new DataChangedEvent());

                            project.getDbLayer().endBatchStatement();
                            project.getDbLayer().commit();
                        } catch (Exception e) {
                            project.getDbLayer().cancelBatchStatement();
                            project.getDbLayer().rollback();
                            throw e;
                        }
                    }
                });
            }
        };
    }

    private EtkFunction calcAggTypeForEPCProductFromModels() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                final EtkProject project = owner.getProject();
                final EtkMessageLogForm messageLogForm = new EtkMessageLogForm("!!EPC Produkte um Aggregatetyp " +
                                                                               "erweitern",
                                                                               "!!Suche und verarbeite Baumuster zu " +
                                                                               "allen EPC Produkten...", null);
                messageLogForm.showModal(new FrameworkRunnable() {
                    @Override
                    public void run(FrameworkThread thread) {
                        EPCImportHelper.calculateAggTypesForEPCProductsFromModels(project, messageLogForm.getMessageLog(), true);
                    }
                });

            }
        };
    }

    private EtkFunction convertEs1AndEs2Keys() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                if (MessageDialog.showYesNo("!!Möchten Sie die ES1/ES2 Teilenummern wirklich konvertieren?") == ModalResult.YES) {
                    final EtkProject project = owner.getProject();
                    final EtkMessageLogForm messageLogForm = new EtkMessageLogForm("!!Teilestammdatenformat konvertieren",
                                                                                   "Suche und verarbeite Teilenummern mit ES1 und/oder ES2 Schlüssel", null);
                    messageLogForm.showModal(new FrameworkRunnable() {
                        @Override
                        public void run(FrameworkThread thread) {
                            EtkDataPartList dataPartList = new EtkDataPartList();
                            iPartsNumberHelper helper = new iPartsNumberHelper();
                            // Erster Durchlauf: Alle Datensätze mit Grundsachnummer und nur befüllten ES1 Schlüssel (ohne SMART QV Sachnummern)
                            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Suche alle Datensätze " +
                                                                                                    "mit vorhandener Grundsachnummer, " +
                                                                                                    "vorhandenem ES1 Schlüssel und keine SMART Sachnummern..."),
                                                                       MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                            String[] whereFields = new String[]{ FIELD_M_AS_ES_2 };
                            String[] whereValues = new String[]{ "" };
                            String[] whereNotFields = new String[]{ FIELD_M_BASE_MATNR, FIELD_M_AS_ES_1, FIELD_M_BASE_MATNR };
                            String[] whereNotValues = new String[]{ "", "", "Q_______V___" };
                            dataPartList.searchSortAndFill(project, TABLE_MAT, whereFields, whereValues, whereNotFields,
                                                           whereNotValues, null, DBDataObjectList.LoadType.COMPLETE,
                                                           DBActionOrigin.FROM_DB);
                            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!%1 Datensätze gefunden",
                                                                                                    String.valueOf(dataPartList.size())),
                                                                       MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                            convertPartNoInDB(project, messageLogForm, dataPartList, helper);

                            // Zweiter Durchlauf: Alle Datensätze mit Grundsachnummer und nur befüllten ES2 Schlüssel (ohne SMART QV Sachnummern)
                            dataPartList.clear(DBActionOrigin.FROM_DB);
                            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Suche alle Datensätze mit" +
                                                                                                    " vorhandener Grundsachnummer, " +
                                                                                                    "vorhandenem ES2 Schlüssel und keine SMART Sachnummern..."),
                                                                       MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                            whereFields = new String[]{ FIELD_M_AS_ES_1 };
                            whereValues = new String[]{ "" };
                            whereNotFields = new String[]{ FIELD_M_BASE_MATNR, FIELD_M_AS_ES_2, FIELD_M_BASE_MATNR };
                            whereNotValues = new String[]{ "", "", "Q_______V___" };
                            dataPartList.searchSortAndFill(project, TABLE_MAT, whereFields, whereValues, whereNotFields,
                                                           whereNotValues, null, DBDataObjectList.LoadType.COMPLETE,
                                                           DBActionOrigin.FROM_DB);
                            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!%1 Datensätze gefunden",
                                                                                                    String.valueOf(dataPartList.size())), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                            convertPartNoInDB(project, messageLogForm, dataPartList, helper);

                            // Dritter Durchlauf: Alle Datensätze mit Grundsachnummer, befüllten ES1 und ES2 Schlüssel (ohne SMART QV Sachnummern)
                            dataPartList.clear(DBActionOrigin.FROM_DB);
                            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Suche alle Datensätze " +
                                                                                                    "mit vorhandener Grundsachnummer, " +
                                                                                                    "vorhandenem ES1 Schlüssel, vorhandenem " +
                                                                                                    "ES2 Schlüssel und keine SMART Sachnummern..."),
                                                                       MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                            whereFields = null;
                            whereValues = null;
                            whereNotFields = new String[]{ FIELD_M_BASE_MATNR, FIELD_M_AS_ES_1, FIELD_M_AS_ES_2, FIELD_M_BASE_MATNR };
                            whereNotValues = new String[]{ "", "", "", "Q_______V___" };
                            dataPartList.searchSortAndFill(project, TABLE_MAT, whereFields, whereValues, whereNotFields,
                                                           whereNotValues, null, DBDataObjectList.LoadType.COMPLETE,
                                                           DBActionOrigin.FROM_DB);
                            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!%1 Datensätze gefunden",
                                                                                                    String.valueOf(dataPartList.size())),
                                                                       MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                            convertPartNoInDB(project, messageLogForm, dataPartList, helper);

                            messageLogForm.getMessageLog().fireMessageWithSeparators(TranslationHandler.translate("!!Konvertierung abgeschlossen",
                                                                                                                  String.valueOf(dataPartList.size())),
                                                                                     MessageLogOption.TIME_STAMP);
                        }
                    });
                }
            }
        };
    }

    private EtkFunction searchMADDataAfterDIALOGImport() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                final EtkProject project = owner.getProject();
                final Set<String> chosenSeries = SearchMADDataAfterDIALOGImportForm.getSeriesFromUser(owner);
                if ((chosenSeries != null) && !chosenSeries.isEmpty()) {
                    iPartsMainImportHelper.searchMADDatAfterDIALOGImport(project, chosenSeries);
                }
            }
        };
    }

    private EtkFunction autoReleaseModifiedFactoryData() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                ImportFactoryDataAutomationHelper.autoReleaseFactoryDataWithMessageLog(getProject());
            }
        };
    }

    /**
     * Prüft ob es zur eingegebenen Baureihe Ersetzungen gibt die nach den neuen Ladelogik überflüssig sind, weil sie
     * mittlerweile als virtuelle/ verebte Ersetzungen beim Laden der Stückliste erzeugt werden
     * Wird eine solche Ersetzung gefunden, so wird ihr echtes Spiegelbild aus der Datenbank gelöscht, damit danach keine
     * doppelten Ersetzungen angezeigt werden, und diese auch nicht mehr durch die Autoren separat gepflegt werden müssen.
     */
    private EtkFunction cleanupReplacements() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                String title = "!!Bereinigung von Ersetzungen";
                final EtkProject project = owner.getProject();
                final DBBase db = project.getDB();
                if (db == null) {
                    MessageDialog.showError("!!Fehler: DBBase ist null!", title);
                    return;
                }
                GuiPanel inputPanel = new GuiPanel(new LayoutGridBag(false));
                GuiLabel seriesLabel = new GuiLabel(TranslationHandler.translate("!!Baureihe"));
                seriesLabel.setConstraints(new ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0,
                                                                  ConstraintsGridBag.ANCHOR_EAST, ConstraintsGridBag.FILL_NONE,
                                                                  4, 4, 4, 4));
                inputPanel.addChild(seriesLabel);
                GuiTextField seriesInput = new GuiTextField();
                seriesInput.setMinimumWidth(200);
                seriesInput.setConstraints(new ConstraintsGridBag(1, 0, 1, 1, 0.0, 100.0,
                                                                  ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_HORIZONTAL,
                                                                  4, 4, 4, 4));
                inputPanel.addChild(seriesInput);
                seriesInput.requestFocus();

                if (InputDialog.show(title, "!!Baureihe", inputPanel, null, seriesInput) != ModalResult.OK) {
                    return;
                }

                // Baureihe muss vorhanden und versorgungsrelevant sein und die DIALOG Urladung muss stattgefunden haben,
                // da die Bereinigung von Ersetzungen ansonsten fehlerhaft sein könnte.
                final String series = seriesInput.getText();
                if (StrUtils.isEmpty(series)) {
                    MessageDialog.showWarning(TranslationHandler.translate("!!Die Bereinigung von Ersetzungen kann nicht durchgeführt werden.") +
                                              "\n" + TranslationHandler.translate("Es wurde keine Baureihe angegeben."), title);
                    return;
                }
                if (!iPartsDIALOGSeriesValidityCache.getInstance(project).isSeriesValidForDIALOGImport(series)) {
                    MessageDialog.showWarning(TranslationHandler.translate("!!Die Bereinigung von Ersetzungen kann nicht durchgeführt werden.") +
                                              "\n" + TranslationHandler.translate("Die Baureihe \"%1\" ist nicht versorgungsrelevant.",
                                                                                  series), title);
                    return;
                }

                if (MessageDialog.showYesNo(TranslationHandler.translate("!!Wurde für die Baureihe \"%1\" bereits eine DIALOG-Urladung durchgeführt?",
                                                                         series), title) != ModalResult.YES) {
                    MessageDialog.showWarning(TranslationHandler.translate("!!Die Bereinigung von Ersetzungen kann nicht durchgeführt werden.") +
                                              "\n" + TranslationHandler.translate("Für die Baureihe \"%1\" wurde noch keine DIALOG-Urladung durchgeführt.",
                                                                                  series), title);
                    return;
                }

                final VarParam<Boolean> cancelVarParam = new VarParam<>(false);
                final EtkMessageLogForm messageLogForm = new EtkMessageLogForm("", title, null) {
                    @Override
                    protected void cancel(Event event) {
                        cancelVarParam.setValue(true);
                        getMessageLog().fireMessage(TranslationHandler.translate("!!Abbruch durch Benutzer"));
                        super.cancel(event);
                    }
                };
                String logFileTitle = TranslationHandler.translateForLanguage("!!Bereinigung von Ersetzungen", iPartsConst.LOG_FILES_LANGUAGE);
                final DWFile finalLogFile = iPartsJobsManager.getInstance().addDefaultLogFileToMessageLog(messageLogForm.getMessageLog(), logFileTitle,
                                                                                                          iPartsPlugin.LOG_CHANNEL_DEBUG);
                ModalResult modalResult = messageLogForm.showModal(new FrameworkRunnable() {
                    @Override
                    public void run(FrameworkThread thread) {
                        project.getRevisionsHelper().executeWithoutActiveChangeSets(new Runnable() {
                            @Override
                            public void run() {
                                // Alle Assemblies bestimmen die Ersetzungen haben und denen ein Produkt zu dieser Baureihe zugeordnet ist
                                messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Bereinung der Ersetzungen für Baureihe %1 gestartet", series.toUpperCase()),
                                                                           MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);

                                DBSQLQuery query = project.getEtkDbs().getDBForDomain(MAIN).getNewQuery();
                                query.selectDistinct(new Fields(TableAndFieldName.make(TABLE_DA_REPLACE_PART, FIELD_DRP_VARI))).from(TABLE_DA_REPLACE_PART);
                                query.join(new InnerJoin(new Tables(TABLE_DA_PRODUCT_MODULES),
                                                         new Condition(TableAndFieldName.make(TABLE_DA_REPLACE_PART, FIELD_DRP_VARI),
                                                                       Condition.OPERATOR_EQUALS,
                                                                       new Fields(TableAndFieldName.make(TABLE_DA_PRODUCT_MODULES, FIELD_DPM_MODULE_NO)))));
                                query.join(new InnerJoin(new Tables(TABLE_DA_PRODUCT),
                                                         new Condition(TableAndFieldName.make(TABLE_DA_PRODUCT_MODULES, FIELD_DPM_PRODUCT_NO),
                                                                       Condition.OPERATOR_EQUALS,
                                                                       new Fields(TableAndFieldName.make(TABLE_DA_PRODUCT, FIELD_DP_PRODUCT_NO)))));

                                query.where(new Condition(TableAndFieldName.make(TABLE_DA_PRODUCT, FIELD_DP_SERIES_REF),
                                                          Condition.OPERATOR_EQUALS,
                                                          series.toUpperCase()));

                                DBDataSet dbSet = query.executeQuery();

                                Set<String> assemblyIds = new TreeSet<>();
                                while (dbSet.next()) {
                                    EtkRecord record = dbSet.getRecord(new String[]{ FIELD_DRP_VARI });
                                    DBDataObjectAttributes attributes = DBDataObjectAttributes.getFromRecord(record, DBActionOrigin.FROM_DB);
                                    String kVari = attributes.getFieldValue(FIELD_DRP_VARI);
                                    assemblyIds.add(kVari);
                                }
                                dbSet.close();

                                int max = assemblyIds.size();

                                messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Betroffene Module: %1", String.valueOf(max)),
                                                                           MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                                messageLogForm.getMessageLog().fireProgress(0, max, "", true, true);
                                messageLogForm.getMessageLog().fireMessage(TranslationKeys.LINE_SEPARATOR);

                                db.startTransaction();
                                db.startBatchStatement();

                                try {
                                    int countNotModified = 0;
                                    int countModified = 0;
                                    int countAll = 0;
                                    cancelVarParam.setValue(false);
                                    iPartsDIALOGPositionsHelper missingDialogPositionsHelper = new iPartsDIALOGPositionsHelper(null);
                                    ObjectInstanceLRUList<HmMSmId, iPartsDIALOGPositionsHelper> dialogPositionsHelperMap = new ObjectInstanceLRUList<>(500,
                                                                                                                                                       iPartsConst.MAX_CACHE_LIFE_TIME_CORE);
                                    for (String assemblyId : assemblyIds) {
                                        messageLogForm.getMessageLog().fireProgress(countAll++, max, "", true, true);
                                        if (cancelVarParam.getValue()) {
                                            db.cancelBatchStatement();
                                            db.rollback();
                                            return;
                                        }

                                        // Stückliste nicht in den Cache legen, weil die Ersetzungen ansonsten nicht korrekt
                                        // wären nach der Korrektur und die Baureihe außerdem evtl. im Cache noch gar nicht
                                        // versorgungsrelevant ist, was hier aber temporär notwendig ist für das Erzeugen
                                        // der virtuellen Ersetzungen.
                                        iPartsDataAssembly dataAssembly = (iPartsDataAssembly)EtkDataObjectFactory.createDataAssembly(project,
                                                                                                                                      new AssemblyId(assemblyId, ""),
                                                                                                                                      false);
                                        DBDataObjectList<EtkDataPartListEntry> partListUnfiltered = dataAssembly.getPartListUnfiltered(null, false, false);

                                        // Alle in der Datenbank als echte Datensätze existierenden Ersetzungs-Datenobjekte
                                        iPartsDataReplacePartList replacementsForAssemblyInDB = iPartsDataReplacePartList.loadReplacementsForAssembly(project,
                                                                                                                                                      dataAssembly.getAsId());
                                        // Map um doppeltes Zählen von Datensätzen zu vermeiden
                                        Map<iPartsReplacePartId, iPartsDataReplacePart> replacementsToDelete = new HashMap<>();

                                        // Map für alle Konstruktions-Ersetzungen pro DIALOG-Position und KEM-Kette
                                        Map<String, Set<iPartsReplacementConst>> kemChainToConstReplacementsMap = new HashMap<>();

                                        // Alle gerade geladenen Ersetzungen holen und doppelte vermeiden, indem nur die Nachfolger-Ersetzungen geholt werden,
                                        // die nochmals in gleicher Form am Nachfolger als Vorgänger-Ersetzung hängen bzw. alleine stehen, wenn der Nachfolger
                                        // nicht in der Stückliste ist. Durch Vermeiden der doppelten Datensätze werden die PEM-Auswerten-Flags richtig bestimmt
                                        // und es kommen keine doppelten Logausgaben.
                                        List<iPartsReplacement> allLoadedReplacementsOfAssembly = new DwList<>();
                                        for (EtkDataPartListEntry partListEntry : partListUnfiltered) {
                                            // DIALOG-Stückliste für den BCTE-Schlüssel des Stücklisteneintrags ermitteln,
                                            // um das minimale KEM-Datum ab und das maximale KEM-Datum bis für die KEM-Kette
                                            // zu berechnen
                                            String dialogGUID = partListEntry.getFieldValue(iPartsConst.FIELD_K_SOURCE_GUID);
                                            iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(dialogGUID);
                                            if (bctePrimaryKey != null) {
                                                HmMSmId hmMSmId = bctePrimaryKey.getHmMSmId();
                                                iPartsDIALOGPositionsHelper dialogPositionsHelper = dialogPositionsHelperMap.get(hmMSmId);
                                                if (dialogPositionsHelper == null) {
                                                    PartListEntryId constructionPLEId = EditConstructionToRetailHelper.getVirtualConstructionPartlistEntryIdFromRetailPartlistEntry(partListEntry);
                                                    EtkDataAssembly constructionAssembly = EtkDataObjectFactory.createDataAssembly(project,
                                                                                                                                   constructionPLEId.getOwnerAssemblyId(),
                                                                                                                                   false);
                                                    if (constructionAssembly.existsInDB()) {
                                                        dialogPositionsHelper = new iPartsDIALOGPositionsHelper(constructionAssembly.getPartListUnfiltered(null));
                                                        dialogPositionsHelperMap.put(hmMSmId, dialogPositionsHelper);
                                                    } else {
                                                        messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!DIALOG-Stückliste fehlt für HM/M/SM-Knoten %1",
                                                                                                                                hmMSmId.toString("/")),
                                                                                                   MessageLogType.tmlWarning,
                                                                                                   MessageLogOption.TIME_STAMP);
                                                        dialogPositionsHelper = missingDialogPositionsHelper;
                                                        dialogPositionsHelperMap.put(hmMSmId, missingDialogPositionsHelper);
                                                    }
                                                }

                                                // KEM-Kette berechnen und setzen
                                                if (dialogPositionsHelper != missingDialogPositionsHelper) {
                                                    EtkDataPartListEntry constructionPLE = dialogPositionsHelper.getPositionVariantByBCTEKey(bctePrimaryKey);
                                                    if (constructionPLE != null) {
                                                        Set<EtkDataPartListEntry> constPLEsForKemChain = EditConstructionToRetailHelper.calculateMinMaxKEMDatesWithoutCache(constructionPLE,
                                                                                                                                                                            dialogPositionsHelper);
                                                        // Alle Konstruktions-Ersetzungen pro DIALOG-Position zzgl. "minimalem
                                                        // KEM-Datum ab" aufsammeln, damit über diese im Nachgang an den
                                                        // Retail-Ersetzungen die echten Vorgänger und Nachfolger gesetzt
                                                        // werden können
                                                        String minKemDateFrom = constructionPLE.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CALCULATED_MIN_KEM_DATE_FROM);
                                                        String constReplacementsMapKey = bctePrimaryKey.getPositionBCTEPrimaryKeyWithoutSDA().createDialogGUID()
                                                                                         + iPartsDialogBCTEPrimaryKey.DIALOG_GUID_DELIMITER
                                                                                         + minKemDateFrom;
                                                        for (EtkDataPartListEntry constPLEForKemChain : constPLEsForKemChain) {
                                                            if (constPLEForKemChain instanceof iPartsDataPartListEntry) {
                                                                iPartsDataPartListEntry iPartsSourcePLE = (iPartsDataPartListEntry)constPLEForKemChain;

                                                                // Es reicht, die Nachfolger zu betrachten, weil ja sowieso
                                                                // die gesamte Retail-Stückliste betrachtet wird
                                                                if (iPartsSourcePLE.hasSuccessorsConst()) {
                                                                    Collection<iPartsReplacementConst> successorsConst = iPartsSourcePLE.getSuccessorsConst();
                                                                    for (iPartsReplacementConst replacementConst : successorsConst) {
                                                                        EditConstructionToRetailHelper.calculateMinMaxKEMDates(replacementConst.successorEntry,
                                                                                                                               dialogPositionsHelper);
                                                                    }

                                                                    Set<iPartsReplacementConst> constReplacements = kemChainToConstReplacementsMap.get(constReplacementsMapKey);
                                                                    if (constReplacements == null) {
                                                                        constReplacements = new HashSet<>();
                                                                        kemChainToConstReplacementsMap.put(constReplacementsMapKey,
                                                                                                           constReplacements);
                                                                    }
                                                                    constReplacements.addAll(successorsConst);
                                                                }
                                                            }
                                                        }

                                                        partListEntry.setFieldValue(FIELD_K_MIN_KEM_DATE_FROM, minKemDateFrom,
                                                                                    DBActionOrigin.FROM_EDIT);
                                                        partListEntry.setFieldValue(FIELD_K_MAX_KEM_DATE_TO,
                                                                                    constructionPLE.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CALCULATED_MAX_KEM_DATE_TO),
                                                                                    DBActionOrigin.FROM_EDIT);
                                                    } else {
                                                        messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!BCTE-Schlüssel fehlt in der DIALOG-Stückliste für HM/M/SM-Knoten %1: %2",
                                                                                                                                hmMSmId.toString("/"),
                                                                                                                                bctePrimaryKey.toString(", ", false)),
                                                                                                   MessageLogType.tmlWarning,
                                                                                                   MessageLogOption.WRITE_ONLY_IN_LOG_FILE,
                                                                                                   MessageLogOption.TIME_STAMP);
                                                    }
                                                }
                                            }
                                        }
                                        partListUnfiltered.saveToDB(project, false); // Stückliste mit berechneten KEM-Ketten speichern

                                        // Ersetzungen explizit nochmal neu laden, da die KEM-Ketten erst jetzt bekannt sind
                                        // und alle geladenen Ersetzungen zu allLoadedReplacementsOfAssembly hinzufügen
                                        dataAssembly.loadAllReplacementsForPartList(partListUnfiltered, true);
                                        for (EtkDataPartListEntry partListEntry : partListUnfiltered) {
                                            if (partListEntry instanceof iPartsDataPartListEntry) {
                                                Collection<iPartsReplacement> successors = ((iPartsDataPartListEntry)partListEntry).getSuccessors();
                                                if (successors != null) {
                                                    allLoadedReplacementsOfAssembly.addAll(successors);
                                                }
                                            }
                                        }

                                        for (iPartsReplacement replacement : allLoadedReplacementsOfAssembly) {
                                            if (replacement.source == iPartsReplacement.Source.PRIMUS) {
                                                continue;
                                            }

                                            if (replacement.isVirtual()) {
                                                iPartsDataReplacePart virtualReplacementAsDBObject = replacement.getAsDataReplacePart(project, false);
                                                for (iPartsDataReplacePart replacementInDB : replacementsForAssemblyInDB) {
                                                    if (virtualReplacementAsDBObject.isSameReplacement(replacementInDB)) {
                                                        // Eine vom Vorgänger her gleiche Ersetzung existiert in der Datenbank UND hat auch den selben Nachfolger
                                                        // (der nicht Teil vom Primärschlüssel ist). Sequenznummer muss ignoriert werden, da diese bei
                                                        // den virtuellen Ersetzungen nicht mit der eines in der DB existierenden Gegenstücks übereinstimmen könnte.
                                                        replacementsToDelete.put(replacementInDB.getAsId(), replacementInDB);
                                                    }
                                                }
                                            } else {
                                                // Passende Konstruktions-Ersetzung über den Vorgänger suchen
                                                String predecessorSourceGUID = replacement.predecessorEntry.getFieldValue(iPartsConst.FIELD_K_SOURCE_GUID);
                                                String successorSourceGUID = replacement.successorEntry.getFieldValue(iPartsConst.FIELD_K_SOURCE_GUID);
                                                String realPredecessorSourceGUID = null;
                                                String realSuccessorSourceGUID = null;

                                                iPartsDialogBCTEPrimaryKey predecessorBctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(predecessorSourceGUID);
                                                if (predecessorBctePrimaryKey != null) {
                                                    String predecessorMinKemDateFrom = replacement.predecessorEntry.getFieldValue(FIELD_K_MIN_KEM_DATE_FROM);
                                                    String constReplacementsMapKey = predecessorBctePrimaryKey.getPositionBCTEPrimaryKeyWithoutSDA().createDialogGUID()
                                                                                     + iPartsDialogBCTEPrimaryKey.DIALOG_GUID_DELIMITER
                                                                                     + predecessorMinKemDateFrom;
                                                    Set<iPartsReplacementConst> constReplacements = kemChainToConstReplacementsMap.get(constReplacementsMapKey);
                                                    if (constReplacements != null) {
                                                        // BCTE-Schlüssel vom echten Vorgänger und Nachfolger bestimmen
                                                        iPartsDialogBCTEPrimaryKey successorBctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(successorSourceGUID);
                                                        if (successorBctePrimaryKey != null) {
                                                            successorBctePrimaryKey = successorBctePrimaryKey.getPositionBCTEPrimaryKeyWithoutSDA();
                                                            String successorMinKemDateFrom = replacement.successorEntry.getFieldValue(FIELD_K_MIN_KEM_DATE_FROM);
                                                            for (iPartsReplacementConst constReplacement : constReplacements) {
                                                                String constSuccessorDialogGUID = constReplacement.successorEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GUID);
                                                                iPartsDialogBCTEPrimaryKey constSuccessorBctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(constSuccessorDialogGUID);
                                                                if (constSuccessorBctePrimaryKey != null) {
                                                                    constSuccessorBctePrimaryKey = constSuccessorBctePrimaryKey.getPositionBCTEPrimaryKeyWithoutSDA();
                                                                    if (constSuccessorBctePrimaryKey.equals(successorBctePrimaryKey)
                                                                        && constReplacement.successorEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CALCULATED_MIN_KEM_DATE_FROM).equals(successorMinKemDateFrom)
                                                                        && constReplacement.preRFMEFlags.equals(replacement.rfmeaFlags)
                                                                        && constReplacement.RFMEFlags.equals(replacement.rfmenFlags)) {
                                                                        realPredecessorSourceGUID = constReplacement.predecessorEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GUID);
                                                                        realSuccessorSourceGUID = constSuccessorDialogGUID;
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                // Fallback auf die in der Retail-Ersetzung verwendeten Vorgänger und Nachfolger
                                                // falls keine passende Konstruktions-Ersetzung gefunden werden kann
                                                if (realPredecessorSourceGUID == null) {
                                                    realPredecessorSourceGUID = predecessorSourceGUID;
                                                    realSuccessorSourceGUID = successorSourceGUID;
                                                    messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Keine passende Konstruktions-Ersetzung gefunden für die Ersetzung von %1 auf %2 mit den RFMEA-Flags \"%3\" und RFMEN-Flags \"%4\"",
                                                                                                                            replacement.predecessorEntry.getAsId().toStringForLogMessages(),
                                                                                                                            replacement.successorEntry.getAsId().toStringForLogMessages(),
                                                                                                                            replacement.rfmeaFlags,
                                                                                                                            replacement.rfmenFlags),
                                                                                               MessageLogType.tmlWarning,
                                                                                               MessageLogOption.WRITE_ONLY_IN_LOG_FILE,
                                                                                               MessageLogOption.TIME_STAMP);
                                                }

                                                // BCTE-Schlüssel vom Vorgänger und Nachfolger setzen
                                                iPartsDataReplacePart replacementAsDBObject = replacement.getAsDataReplacePart(project, false);
                                                replacementAsDBObject.setFieldValue(FIELD_DRP_SOURCE_GUID, realPredecessorSourceGUID,
                                                                                    DBActionOrigin.FROM_EDIT);
                                                replacementAsDBObject.setFieldValue(FIELD_DRP_REPLACE_SOURCE_GUID, realSuccessorSourceGUID,
                                                                                    DBActionOrigin.FROM_EDIT);
                                                replacementAsDBObject.saveToDB(false, DBDataObject.PrimaryKeyExistsInDB.TRUE);
                                            }
                                        }

                                        int countDeletedReplacements = 0;
                                        int countDeletedIncludeParts = 0;
                                        for (iPartsDataReplacePart replacePartToDelete : replacementsToDelete.values()) {
                                            countDeletedReplacements++;
                                            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Lösche Ersetzung: %1",
                                                                                                                    replacePartToDelete.getAsId().toStringForLogMessages()),
                                                                                       MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE,
                                                                                       MessageLogOption.TIME_STAMP);
                                            replacePartToDelete.deleteFromDB(true);
                                            iPartsDataIncludePartList includePartsToDelete = iPartsDataIncludePartList.loadIncludePartsForReplacement(project,
                                                                                                                                                      replacePartToDelete.getPredecessorPartListEntryId(),
                                                                                                                                                      replacePartToDelete.getSuccessorPartListEntryId());
                                            for (iPartsDataIncludePart includePartToDelete : includePartsToDelete) {
                                                countDeletedIncludeParts++;
                                                messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Lösche Mitlieferteil: %1",
                                                                                                                        includePartToDelete.getAsId().toStringForLogMessages()),
                                                                                           MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE,
                                                                                           MessageLogOption.TIME_STAMP);
                                            }
                                            includePartsToDelete.deleteFromDB(project, true);
                                        }

                                        // Ersetzungen an der Stückliste neu laden, damit die bereinigten Ersetzungen geladen werden.
                                        dataAssembly.loadAllReplacementsForPartList(partListUnfiltered, true);

                                        List<iPartsReplacement> replacementsOfAssemblyAfterCleanUp = new DwList<>();
                                        for (EtkDataPartListEntry etkDataPartListEntry : partListUnfiltered) {
                                            iPartsDataPartListEntry partListEntry = (iPartsDataPartListEntry)etkDataPartListEntry;
                                            if (partListEntry.getSuccessors() != null) {
                                                replacementsOfAssemblyAfterCleanUp.addAll(partListEntry.getSuccessors());
                                            }
                                        }

                                        // Wenn die Berechnung eines PEM-Auswerte-Flags true ergibt, dann muss das Auswerteflag auf false gesetzt werden.
                                        // Das muss gemacht werden, da die PEM-Auswerte-Flags beim Laden der Stückliste verodert werden, aus dem per Hand
                                        // in der Datenbank eingestellten Wert und dem Wert, den die RFME-Flags ergeben. Wenn man sie auf true lässt,
                                        // würde der veroderte Wert beim Löschen/Ändern der Ersetzung auf true bleiben. Deshalb muss in dem Fall auf
                                        // false gesetzt werden. Im anderen Fall, dass die RFME-Flags an den PEM-Auswerte-Flags false ergeben,
                                        // muss ein eventuell manuell gesetzter true Wert stehenbleiben.
                                        int countModifiedParlistEntries = 0;
                                        for (iPartsReplacement replacement : replacementsOfAssemblyAfterCleanUp) {
                                            iPartsDataPartListEntry predecessorEntry = (iPartsDataPartListEntry)replacement.predecessorEntry;
                                            iPartsDataPartListEntry successorEntry = (iPartsDataPartListEntry)replacement.successorEntry;
                                            boolean oldEvalPemTo = predecessorEntry.getFieldValueAsBoolean(FIELD_K_EVAL_PEM_TO);
                                            boolean oldEvalPemFrom = successorEntry.getFieldValueAsBoolean(FIELD_K_EVAL_PEM_FROM);
                                            iPartsReplacementHelper.calculateEvalPEMToAndPEMFrom(predecessorEntry, successorEntry,
                                                                                                 new iPartsDataReplacePart(project, new iPartsReplacePartId()),
                                                                                                 true, true);
                                            boolean savePredecessor = false;
                                            if (predecessorEntry.getFieldValueAsBoolean(FIELD_K_EVAL_PEM_TO)) {
                                                predecessorEntry.setFieldValueAsBoolean(FIELD_K_EVAL_PEM_TO, false, DBActionOrigin.FROM_EDIT);
                                                if (oldEvalPemTo) {
                                                    savePredecessor = true;
                                                }
                                            } else { // Original-Wert wiederherstellen
                                                predecessorEntry.setFieldValueAsBoolean(FIELD_K_EVAL_PEM_TO, oldEvalPemTo, DBActionOrigin.FROM_EDIT);
                                            }

                                            boolean saveSuccessor = false;
                                            if (successorEntry.getFieldValueAsBoolean(FIELD_K_EVAL_PEM_FROM)) {
                                                successorEntry.setFieldValueAsBoolean(FIELD_K_EVAL_PEM_FROM, false, DBActionOrigin.FROM_EDIT);
                                                if (oldEvalPemFrom) {
                                                    saveSuccessor = true;
                                                }
                                            } else { // Original-Wert wiederherstellen
                                                successorEntry.setFieldValueAsBoolean(FIELD_K_EVAL_PEM_FROM, oldEvalPemFrom, DBActionOrigin.FROM_EDIT);
                                            }

                                            // Jetzt erst den Vorgänger und Nachfolger speichern, weil ansonsten falsche
                                            // Werte in der DB landen würden, wenn Vorgänger und Nachfolger identisch sind
                                            if (savePredecessor) {
                                                predecessorEntry.saveToDB(false, DBDataObject.PrimaryKeyExistsInDB.TRUE);
                                                countModifiedParlistEntries++;
                                                messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!PEM-bis wird zurückgesetzt am Vorgänger: %1",
                                                                                                                        predecessorEntry.getAsId().toStringForLogMessages()),
                                                                                           MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE,
                                                                                           MessageLogOption.TIME_STAMP);
                                            }
                                            if (saveSuccessor) {
                                                successorEntry.saveToDB(false, DBDataObject.PrimaryKeyExistsInDB.TRUE);
                                                countModifiedParlistEntries++;
                                                messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!PEM-ab wird zurückgesetzt am Nachfolger: %1",
                                                                                                                        successorEntry.getAsId().toStringForLogMessages()),
                                                                                           MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE,
                                                                                           MessageLogOption.TIME_STAMP);
                                            }
                                        }

                                        if (countDeletedReplacements > 0) {
                                            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!In Modul %1 wurden %2 Ersetzungen gelöscht",
                                                                                                                    assemblyId, String.valueOf(countDeletedReplacements)),
                                                                                       MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                                        }
                                        if (countDeletedIncludeParts > 0) {
                                            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!In Modul %1 wurden %2 Mitlieferteile gelöscht",
                                                                                                                    assemblyId, String.valueOf(countDeletedIncludeParts)),
                                                                                       MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                                        }
                                        if (countModifiedParlistEntries > 0) {
                                            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!In Modul %1 wurden %2 Stücklisteneinträge angepasst",
                                                                                                                    assemblyId, String.valueOf(countModifiedParlistEntries)),
                                                                                       MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                                        }
                                        if ((countDeletedIncludeParts > 0) || (countDeletedReplacements > 0) || (countModifiedParlistEntries > 0)) {
                                            countModified++;
                                        } else {
                                            countNotModified++;
                                        }
                                    }

                                    messageLogForm.getMessageLog().fireMessage(TranslationKeys.LINE_SEPARATOR);
                                    messageLogForm.getMessageLog().fireProgress(100, 100, "", true, false);
                                    messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!%1 Module wurden nicht verändert",
                                                                                                            String.valueOf(countNotModified)),
                                                                               MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                                    messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!In %1 Modulen wurden Anpassungen vorgenommen",
                                                                                                            String.valueOf(countModified)),
                                                                               MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                                    db.endBatchStatement();
                                    db.commit();

                                    if (countModified > 0) {
                                        ClearRetailRelevantCachesEvent.invalidateRetailRelevantCaches(false, false);
                                    }

                                    messageLogForm.closeWindow(ModalResult.OK);
                                } catch (Exception e) { // Bei Fehlern die Rolle rückwärts machen
                                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                                    messageLogForm.getMessageLog().fireMessage(Logger.getLogger().exceptionToString(e), MessageLogType.tmlError);

                                    db.cancelBatchStatement();
                                    db.rollback();

                                    messageLogForm.closeWindow(ModalResult.ABORT);
                                }
                            }
                        }, true, project);
                    }
                });

                if (finalLogFile != null) {
                    switch (modalResult) {
                        case OK:
                            iPartsJobsManager.getInstance().jobProcessed(finalLogFile);
                            break;
                        case CANCEL:
                            iPartsJobsManager.getInstance().jobCancelled(finalLogFile, true);
                            break;
                        case ABORT:
                            iPartsJobsManager.getInstance().jobError(finalLogFile);
                            break;
                    }
                }
            }
        };
    }

    private EtkFunction calculateInitialWorkbasketSingle(iPartsImportDataOrigin source) {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                String displayName = WorkbasketPrecalculationHelper.getDisplayname(source);
                if (StrUtils.isEmpty(displayName)) {
                    return;
                }
                // Noch eine Sicherheitsabfrage, damit's nicht einfach losläuft
                if (MessageDialog.showYesNo(TranslationHandler.translate("!!Möchten Sie den SAA Arbeitsvorrat für %1 neu vorverdichten?",
                                                                         displayName)) == ModalResult.YES) {
                    String title = TranslationHandler.translate("!!Vorverdichtung SAA Arbeitsvorrat für %1", displayName);
                    // Das Project einmalig holen und merken, es wird mehrfach gebraucht.
                    EtkProject project = getProject();
                    final DBBase db = project.getDB();
                    if (db == null) {
                        MessageDialog.showError("!!Fehler: DBBase ist null!", title);
                        return;
                    }

                    final VarParam<Boolean> cancelVarParam = new VarParam<>(false);
                    final EtkMessageLogForm messageLogForm = new EtkMessageLogForm("", title, null) {
                        @Override
                        protected void cancel(Event event) {
                            cancelVarParam.setValue(true);
                            getMessageLog().fireMessage(TranslationHandler.translate("!!Abbruch durch Benutzer"));
                            super.cancel(event);
                        }
                    };
                    messageLogForm.showModal(thread -> WorkbasketPrecalculationHelper.doCalculateSingleWorkbasket(project, messageLogForm,
                                                                                                                  cancelVarParam, source));
                }
            }
        };
    }

    private EtkFunction calculateInitialSAAWorkbasketForEDSandMBSandCTT() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                // Noch eine Sicherheitsabfrage, damit's nicht einfach losläuft
                if (MessageDialog.showYesNo("!!Möchten Sie den SAA Arbeitsvorrat für EDS, MBS und CTT neu vorverdichten?") == ModalResult.YES) {
                    String title = "!!Vorverdichtung SAA Arbeitsvorrat für EDS, MBS und CTT";
                    // Das Project einmalig holen und merken, es wird mehrfach gebraucht.
                    EtkProject project = getProject();
                    final DBBase db = project.getDB();
                    if (db == null) {
                        MessageDialog.showError("!!Fehler: DBBase ist null!", title);
                        return;
                    }

                    final VarParam<Boolean> cancelVarParam = new VarParam<>(false);
                    final EtkMessageLogForm messageLogForm = new EtkMessageLogForm("", title, null) {
                        @Override
                        protected void cancel(Event event) {
                            cancelVarParam.setValue(true);
                            getMessageLog().fireMessage(TranslationHandler.translate("!!Abbruch durch Benutzer"));
                            super.cancel(event);
                        }
                    };
                    messageLogForm.showModal(new FrameworkRunnable() {
                        @Override
                        public void run(FrameworkThread thread) {
                            WorkbasketPrecalculationHelper.doCalculateAllWorkbaskets(project, messageLogForm, cancelVarParam);
                        }
                    });
                }
            }
        };
    }

    /**
     * Konvertiert eine falsch formatierte Teilenummer in der MAT Tabelle
     *
     * @param project
     * @param messageLogForm
     * @param dataPartList
     * @param helper
     */
    private void convertPartNoInDB(EtkProject project, EtkMessageLogForm messageLogForm, EtkDataPartList dataPartList, iPartsNumberHelper helper) {
        project.getDbLayer().startTransaction();
        project.getDbLayer().startBatchStatement();
        try {
            int counter = 0;
            int overallCounter = 0;
            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Starte Konvertierung..."), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            for (EtkDataPart dataPart : dataPartList) {
                overallCounter++;
                messageLogForm.getMessageLog().fireProgress(overallCounter, dataPartList.size(), "", false, true);
                String currentPartNo = dataPart.getAsId().getMatNr();
                if (helper.isSMARTPartNo(currentPartNo)) {
                    continue;
                }
                String baseNo = dataPart.getFieldValue(FIELD_M_BASE_MATNR);
                String es1 = dataPart.getFieldValue(FIELD_M_AS_ES_1);
                String es2 = dataPart.getFieldValue(FIELD_M_AS_ES_2);
                String newPartNo = helper.getPartNoWithES1AndESKeys(baseNo, es1, es2);
                if (currentPartNo.equals(newPartNo)) {
                    continue;
                }
                dataPart.setId(new iPartsPartId(newPartNo, ""), DBActionOrigin.FROM_EDIT);
                dataPart.setFieldValue(FIELD_M_BESTNR, newPartNo, DBActionOrigin.FROM_EDIT);
                dataPart.saveToDB();
                counter++;
                // Zur Sicherheit nach jeweils 500 angepassten Teilenummern, die Datensätze speichern
                if ((counter % 500) == 0) {
                    project.getDbLayer().endBatchStatement();
                    project.getDbLayer().commit();
                    project.getDbLayer().startTransaction();
                    project.getDbLayer().startBatchStatement();
                }
            }
            messageLogForm.getMessageLog().hideProgress();
            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!%1 von %2 Teilenummern wurden angepasst",
                                                                                    String.valueOf(counter),
                                                                                    String.valueOf(dataPartList.size())),
                                                       MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            project.getDbLayer().endBatchStatement();
            project.getDbLayer().commit();
        } catch (Exception e) {
            project.getDbLayer().cancelBatchStatement();
            project.getDbLayer().rollback();
            throw e;
        }
    }

    private void modifyImportMenu(MenuManager manager) {
        if (!iPartsRight.IMPORT_MASTER_DATA.checkRightInSession()) {
            return;
        }

        // "Import" - Menüpunkt in die Menüleiste addieren

        // Import

        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT, "!!DASTi Zeichnungen...", null, false, importImagesForReferences(getProject()), IPARTS_MENU_NAME_DICTIONARY);

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT, "!!KG/TU Templates...", null, false, new EtkFunctionHelper("KgTuTemplateImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new KgTuTemplateImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT, "!!Hashtag-Texte...", null, false, new EtkFunctionHelper("HashTagTextsImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new HashTagTextsImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT, "!!KG/TU Stücklistenmapping für Erstdokumentation (KI)...", null, false, new EtkFunctionHelper("iPartsMigrationDIALOG_KgTu_KI_Mapping_Importer") {
            @Override
            public AbstractDataImporter createImporter() {
                return new DialogKgTuPredictionImporter(getProject());
            }
        });

        // Separator
        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT, null, null, false, null, IPARTS_MENU_NAME_DICTIONARY);

        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT, "!!Einstellungen für automatische Importe...", null, false, new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                AutoImportSettingsForm.showAutoImportSettingsForm(owner);
            }
        }, IPARTS_MENU_NAME_DICTIONARY);

        // Import DIALOG-Ereignisse (Hinzugefügt wird der Menüeintrag erst später
        GuiMenuItem eventImporterMenu = manager.createMenuItem("!!DIALOG Ereignissteuerung", null, false, false, null);
        eventImporterMenu.addChild(manager.createMenuItem("!!Ereignisdaten (EREI)...", new EtkFunctionHelper("iPartsEreignisDIALOG_EREI_Importer") {
            @Override
            public AbstractFilesImporter createImporter() {
                return new EventDataImporter(getProject());
            }
        }));
        eventImporterMenu.addChild(manager.createMenuItem("!!Konstruktionsstückliste (BRTE)...", new EtkFunctionHelper("iPartsEreignisDIALOG_BRTE_Importer") {
            @Override
            public AbstractFilesImporter createImporter() {
                return new PartListDataImporter(getProject(), PartListDataImporter.IMPORT_TABLENAME_BRTE);
            }
        }));
        eventImporterMenu.addChild(manager.createMenuItem("!!Werkseinsatzdaten (WBRT)...", new EtkFunctionHelper("iPartsEreignisDIALOG_WBRT_Importer") {

            @Override
            public AbstractFilesImporter createImporter() {
                return new FactoryDataImporter(getProject(), FactoryDataImporter.IMPORT_TABLENAME_WBRT);
            }
        }));
        eventImporterMenu.addChild(manager.createMenuItem("!!Zusatzinformationen zur Konstruktionsstückliste (VBRT)...", new EtkFunctionHelper("iPartsEreignisDIALOG_VBRT_Importer") {

            @Override
            public AbstractFilesImporter createImporter() {
                return new PartListAddDataImporter(getProject(), PartListAddDataImporter.IMPORT_TABLENAME_VBRT);
            }
        }));
        eventImporterMenu.addChild(manager.createMenuItem("!!Farbtabelleninhalt (Y9E)...", new EtkFunctionHelper("iPartsEreignisDIALOG_Y9E_Importer") {

            @Override
            public AbstractFilesImporter createImporter() {
                return new ColorTablePartOrContentImporter(getProject(), ColorTablePartOrContentImporter.IMPORT_TABLENAME_Y9E);
            }
        }));
        eventImporterMenu.addChild(manager.createMenuItem("!!Werkseinsatzdaten für Farbtabelleninhalt (WY9)...", new EtkFunctionHelper("iPartsEreignisDIALOG_WY9_Importer") {

            @Override
            public AbstractFilesImporter createImporter() {
                return new ColorTableFactoryDataImporter(getProject(), ColorTableFactoryDataImporter.TABLENAME_WY9);
            }
        }));
        eventImporterMenu.addChild(manager.createMenuItem("!!Baubarkeit (gültige Code zu Baureihe) (Y4E)...", new EtkFunctionHelper("iPartsSeriesCodes_Y4E_Importer") {

            @Override
            public AbstractFilesImporter createImporter() {
                return new SeriesCodesImporter(getProject(), SeriesCodesImporter.IMPORT_TABLENAME_Y4E);
            }
        }));
        eventImporterMenu.addChild(manager.createMenuItem("!!Zuordnung Fahrzeugbaureihe zur Aggregatebaureihe (Y6E)...", new EtkFunctionHelper("iPartsModuleRelation_Y6E_Importer") {

            @Override
            public AbstractFilesImporter createImporter() {
                return new ModuleRelationImporter(getProject(), ModuleRelationImporter.IMPORT_TABLENAME_Y6E);
            }
        }));

        // TRANSIT Konsolidierte Texte
        // Separator
        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT, null, null, false, null, IPARTS_MENU_NAME_DICTIONARY);

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT, "!!Konsolidierte Texte...", null, false, new EtkFunctionHelper("TransitTextsImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new TranslationsImporter(getProject(), "!!Konsolidierte Texte");
            }
        });

        // TruckBOM.foundation manueller Import
        // Separator
        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT, null, null, false, null, IPARTS_MENU_NAME_DICTIONARY);

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT, "!!TruckBOM.foundation...", null, false, new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                TruckBOMFoundationImportForm importForm = new TruckBOMFoundationImportForm();
                importForm.show();
            }
        });

        // Neue Struktur für EDS/BCS aus TB.f
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT, "!!Produktstruktur aus TB.f urladen...", null, false, new EtkFunctionHelper("TruckBOMStructureMappingImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new TruckBOMStructureMappingImporter(getProject());
            }
        });

        // PROVAL Code Benennungen
        // Separator
        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT, null, null, false, null, IPARTS_MENU_NAME_DICTIONARY);

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT, "!!ProVal Code-Benennungen...", null, false, new EtkFunctionHelper("CodeDescImporter") {
            @Override
            public AbstractFilesImporter createImporter() {
                return new ProvalCodeImporter(getProject());
            }
        });
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT, "!!ProVal Baubarkeit...", null, false, new EtkFunction() {

            @Override
            public void run(AbstractJavaViewerForm owner) {
                iPartsProValModelAggImportScheduler.doProValModelAggImport(getProject(), Session.get());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT, "!!Import Referenz auf hoch-frequentierte TUs...", null, false, new EtkFunctionHelper("iPartsTopTUsImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new iPartsTopTUsImporter(getProject());
            }
        });

        // Gleichteile
        // Separator
        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT, null, null, false, null, IPARTS_MENU_NAME_DICTIONARY);
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT, "!!Gleichteile...", null, false, new EtkFunctionHelper("iPartsEqualPartsImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new iPartsEqualPartsImporter(getProject());
            }
        });

        // Leitungssätze aus Connect
        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT, null, null, false, null, IPARTS_MENU_NAME_DICTIONARY);
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT, "!!Leitungssätze (Connect)...", null, false, new EtkFunctionHelper("iPartsWireHarnessImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new WireHarnessDataImporter(getProject());
            }
        });

        // DAIMLER-15536, Import von Reman Varianten zur ZB Sachnummer, Austauschmotorvarianten
        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT, null, null, false, null, IPARTS_MENU_NAME_DICTIONARY);
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT, "!!Austauschmotorvarianten...", null, false, new EtkFunctionHelper("iPartsRemanVariantImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new RemanVariantImporter(getProject());
            }
        });

        // DAIMLER-13396, Import Sachnummer zu Lieferantennummer aus SRM über RFTS/x
        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT, null, null, false, null, IPARTS_MENU_NAME_DICTIONARY);
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT, "!!Sachnummer zu Lieferantensachnummer aus SRM...", null, false, new EtkFunctionHelper("SnrToSupplierSnrDataImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new SrmSupplierPartNoMappingDataImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT, "!!PPUA Import...", null, false, new EtkFunctionHelper("PPUAImporter") {
            @Override
            public AbstractDataImporter createImporter() {
//                return new PPUAImporter(getProject());
                return new PPUAAlternateImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT, "!!SPK-Mapping...", null, false, new EtkFunctionHelper("GenVoSpkMappingImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new SpkMappingImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT, "!!SPK-Texte...", null, false, new EtkFunctionHelper("GenVoSpkTextImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new SpkTextImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT, "!!GenVo Ergänzungstexte...", null, false, new EtkFunctionHelper("GenVoAdditionalTextImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new GenVoSuppTextImporter(getProject());
            }
        });

        // Import DIALOG

        // Entfernt im Zuge von DAIMLER-3585
//        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!Daten von Excel...", null, false, new EtkFunctionHelper("iPartsDialogExcel") {
//            @Override
//            public AbstractFilesImporter createImporter() {
//                return new ConstructionDataDialogImporter(getProject());
//            }
//        }, IPARTS_MENU_NAME_IMPORT);
//
//        // Separator
//        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT_DIALOG, null, null, false, null, IPARTS_MENU_NAME_IMPORT);

        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!Baureihenstammdaten (BRS)...", null, false, new EtkFunctionHelper("iPartsSeriesImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new MasterDataDialogSeriesImporter(getProject());
            }
        }, IPARTS_MENU_NAME_IMPORT);

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!Baumusterstammdaten (BMS)...", null, false, new EtkFunctionHelper("iPartsModelImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new MasterDataDialogModelImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!Stammdaten Baumuster zu Baureihen (X2E)...", null, false, new EtkFunctionHelper("iPartsModelSeriesImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new MasterDataDialogModelSeriesImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!Zuordnung Fahrzeugbaureihe zur Aggregatebaureihe (X6E)...", null, false, new EtkFunctionHelper("iPartsVS2USImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new ModuleRelationImporter(getProject(), ModuleRelationImporter.IMPORT_TABLENAME_X6E);
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!Teilestammdaten (TS1/TS2/TS6/VTNR/GEWS)...", null, false, new EtkFunctionHelper("iPartsMasterDataDialogImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new MasterDataDialogImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!Ersetzungen Änderungstexte (TS7)...", null, false, new EtkFunctionHelper("iPartsReplacementPartsConstImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new ReplacementPartsConstImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!Codestamm (RES)...", null, false, new EtkFunctionHelper("iPartsCodeMasterdataImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new CodeMasterDataImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!Farbnummern (FNR)...", null, false, new EtkFunctionHelper("iPartsColorNumberImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new ColorNumberImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!HM/M/SM Struktur (KGVZ)...", null, false, new EtkFunctionHelper("iPartsHMMSMImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new HmMSmStructureImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!Konstruktionsstückliste (BCTE)...", null, false, new EtkFunctionHelper("iPartsPartListDataImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new PartListDataImporter(getProject(), PartListDataImporter.IMPORT_TABLENAME_BCTE);
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!Generic Part (BCTG)...", null, false, new EtkFunctionHelper("iPartsGenericPartImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new GenericPartImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!Zusatzinformationen zur Konstruktionsstückliste (VBCA)...", null, false, new EtkFunctionHelper("iPartsPartListAddDataImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new PartListAddDataImporter(getProject(), PartListAddDataImporter.IMPORT_TABLENAME_VBCA);
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!Konstruktionsstückliste Zusatztexte (BCTX)...", null, false, new EtkFunctionHelper("iPartsPartListTextDataImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new PartListTextDataImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!POS-Texte zu Submodulen (POSX)...", null, false, new EtkFunctionHelper("iPartsPosTextImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new DIALOGPosTextImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!Generischer Verbauort (POS)...", null, false, new EtkFunctionHelper("iPartsGenericInstallLocationImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new GenericInstallLocationImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!Werkseinsatzdaten (WBCT)...", null, false, new EtkFunctionHelper("iPartsFactoryDataImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new FactoryDataImporter(getProject(), FactoryDataImporter.IMPORT_TABLENAME_WBCT);
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!Farbtabellen Stammdaten (FTS)...", null, false, new EtkFunctionHelper("iPartsColorTableDataImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new ColorTableImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!Farbtabelleninhalt (X9E)...", null, false, new EtkFunctionHelper("iPartsColorTableContentImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new ColorTablePartOrContentImporter(getProject(), ColorTablePartOrContentImporter.IMPORT_TABLENAME_X9E);
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!Teil zu Farbtabelle (X10E)...", null, false, new EtkFunctionHelper("iPartsColorTablePartImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new ColorTablePartOrContentImporter(getProject(), ColorTablePartOrContentImporter.IMPORT_TABLENAME_X10E);
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!Werkseinsatzdaten für Farbtabelleninhalt (WX9)...", null, false, new EtkFunctionHelper("iPartsColorTableFactoryContentImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new ColorTableFactoryDataImporter(getProject(), ColorTableFactoryDataImporter.TABLENAME_WX9);
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!Werkseinsatzdaten für Teil zu Farbtabelle (WX10)...", null, false, new EtkFunctionHelper("iPartsColorTableFactoryPartImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new ColorTableFactoryDataImporter(getProject(), ColorTableFactoryDataImporter.TABLENAME_WX10);
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!After-Sales Verwendungsdaten für Farbtabelleninhalt (VX9)...", null, false, new EtkFunctionHelper("iPartsColorTableAfterSalesFactoryContentImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new ColorTableFactoryDataImporter(getProject(), ColorTableFactoryDataImporter.TABLENAME_VX9);
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!After-Sales Verwendungsdaten für Teil zu Farbtabelle (VX10)...", null, false, new EtkFunctionHelper("iPartsColorTableAfterSalesFactoryPartImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new ColorTableFactoryDataImporter(getProject(), ColorTableFactoryDataImporter.TABLENAME_VX10);
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!Werkseinsatzdaten AS (VBW)...", null, false, new EtkFunctionHelper("iPartsFactoryDataAfterSalesImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new FactoryDataImporter(getProject(), FactoryDataImporter.IMPORT_TABLENAME_VBW);
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!Rückmeldedaten (RMDA)...", null, false, new EtkFunctionHelper("iPartsResponseDataImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new ResponseDataImporter(getProject(), ResponseDataImporter.TABLENAME_RMDA);
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!Rückmeldedaten Ausreißer (RMID)...", null, false, new EtkFunctionHelper("iPartsResponseSpikeImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new ResponseDataImporter(getProject(), ResponseDataImporter.TABLENAME_RMID);
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!Zuordnung Fußnoten zu Material (VTFN) ...", null, false, new EtkFunctionHelper("iPartsMigrationDIALOG_VTFN_Importer") {
            @Override
            public AbstractDataImporter createImporter() {
                return new FootNoteMatRefImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!Sicherheits- und zertifizierungsrelevante Teile (TMK) ...", null, false, new EtkFunctionHelper("iPartsMigrationDIALOG_DSR_Importer") {
            @Override
            public AbstractDataImporter createImporter() {
                return new DSRDataImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!Baukasteninhalt zu Teilenummer (ZBVE) ...", null, false, new EtkFunctionHelper("iPartsMigrationDIALOG_ZBVE_Importer") {
            @Override
            public AbstractDataImporter createImporter() {
                return new ZBVEDataImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!Ersetzungen und Mitlieferteile am Teilestamm (VTNV) ...", null, false, new EtkFunctionHelper("iPartsMigrationDIALOG_VTFN_Importer") {
            @Override
            public AbstractDataImporter createImporter() {
                return new VTNVDataImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!Baubarkeit (gültige Code zu Baureihe) (X4E) ...", null, false, new EtkFunctionHelper("iPartsMigrationDIALOG_X4E_Importer") {
            @Override
            public AbstractDataImporter createImporter() {
                return new SeriesCodesImporter(getProject(), SeriesCodesImporter.IMPORT_TABLENAME_X4E);
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!KEM Stammdaten (KES) ...", null, false, new EtkFunctionHelper("iPartsMigrationDIALOG_KES_Importer") {
            @Override
            public AbstractDataImporter createImporter() {
                return new KemMasterDataImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!Fehlerorte (SCTV) ...", null, false, new EtkFunctionHelper("iPartsSCTVImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new SCTVDataImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_DIALOG, "!!Endenachricht...", null, false, new EtkFunctionHelper("iPartsDIALOGEndMessageImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new DialogEndMessageWorker(getProject());
            }
        });


        // Import EDS

        // Entfernt im Zuge von DAIMLER-3585
//        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT_EDS, "!!EDS/BCS-Baumusterinhalt von Excel...", null, false, new EtkFunctionHelper("iPartsEdsExcel") {
//            @Override
//            public AbstractFilesImporter createImporter() {
//                return new ConstructionDataEdsImporter(getProject());
//            }
//        }, IPARTS_MENU_NAME_IMPORT_DIALOG);
//
//        // Separator
//        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT_EDS, null, null, false, null, IPARTS_MENU_NAME_IMPORT_DIALOG);

        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT_BOM_DB, "!!Baumusterstammdaten (BM)...", null, false, new EtkFunctionHelper("iPartsEDSModelMasterImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new EDSModelImporter(getProject());
            }
        }, IPARTS_MENU_NAME_IMPORT_DIALOG);

        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT_BOM_DB, "!!Baumusterinhalt (B2I)...", null, false, new EtkFunctionHelper("iPartsEDSModelMasterContentImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new EDSModelMasterContentImporter(getProject());
            }
        }, IPARTS_MENU_NAME_IMPORT_DIALOG);

        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT_BOM_DB, "!!Baumustergruppen-Stammdaten (OPS) (BMAG)...", null, false, new EtkFunctionHelper("iPartsEDSModelGroupMasterImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new EDSModelGroupImporter(getProject());
            }
        }, IPARTS_MENU_NAME_IMPORT_DIALOG);

        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT_BOM_DB, "!!Baumusterumfang-Stammdaten (OPS) (UMF)...", null, false, new EtkFunctionHelper("iPartsEDSModelScopeMasterImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new EDSModelScopeImporter(getProject());
            }
        }, IPARTS_MENU_NAME_IMPORT_DIALOG);

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_BOM_DB, "!!BCS SAA Stammdaten (SAA)...", null, false, new EtkFunctionHelper("iPartsBCSSAAImport") {

            @Override
            public AbstractDataImporter createImporter() {
                return new BCSMasterDataSaaImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_BOM_DB, "!!EDS SAA Stammdaten (SAAE)...", null, false, new EtkFunctionHelper("iPartsEDSSAAEImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new EDSSaaMasterDataImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_BOM_DB, "!!Teilestammdaten (TEIL)...", null, false, new EtkFunctionHelper("iPartsBOMDBTEILImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new BOMPartMasterDataImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_BOM_DB, "!!Teilestammdaten für Baukasten (TEIL)...", null, false, new EtkFunctionHelper("iPartsBOMDBConstKitTEILImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new BOMPartHistoryImporter(getProject());
            }
        });
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_BOM_DB, "!!Ersatzteilkennzeichnung (TEID)...", null, false, new EtkFunctionHelper("iPartsBOMDBTEIDImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new BOMSparePartSignsImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_BOM_DB, "!!Baukasteninhalt (BK)...", null, false, new EtkFunctionHelper("iPartsBOMDBBKImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new BOMConstructionKitContentImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_BOM_DB, "!!Baukastenverwendungsstellentexte (BKV)...", null, false, new EtkFunctionHelper("iPartsBOMDBBKVImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new BOMConstructionKitTextImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_BOM_DB, "!!Wahlweise-Kennbuchstabe und Bemerkungsziffer (TEIE)...", null, false, new EtkFunctionHelper("iPartsBOMDBTEIEImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new EDSMaterialRemarksImporter(getProject());
            }
        });

        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT_BOM_DB, null, null, false, null, IPARTS_MENU_NAME_IMPORT_DIALOG);

        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT_BOM_DB, "!!XML Baumusterstammdaten (BM)...", null, false, new EtkFunctionHelper("iPartsEDSModelUpdatingImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new EDSModelUpdateImporter(getProject());
            }
        }, IPARTS_MENU_NAME_IMPORT_DIALOG);

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_BOM_DB, "!!XML Baumusterinhalt (B2I)...", null, false, new EtkFunctionHelper("iPartsEDSModelContentImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new EDSModelMasterContentUpdateImporter(getProject());
            }
        });
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_BOM_DB, "!!XML Baumustergruppen OPS (BMAG)...", null, false, new EtkFunctionHelper("iPartsEDSModelGroupUpdateImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new EDSModelGroupUpdateImporter(getProject());
            }
        });
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_BOM_DB, "!!XML Baumusterumfang OPS (UMF)...", null, false, new EtkFunctionHelper("iPartsEDSModelScopeUpdateImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new EDSModelScopeUpdateImporter(getProject());
            }
        });
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_BOM_DB, "!!XML BCS SAA Stammdaten (SAA)...", null, false, new EtkFunctionHelper("iPartsBCSMasterDataSaaUpdateImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new BCSMasterDataSaaUpdateImporter(getProject());
            }
        });
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_BOM_DB, "!!XML EDS SAA Stammdaten (SAAE)...", null, false, new EtkFunctionHelper("iPartsEDSSaaMasterDataUpdateImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new EDSSaaMasterDataUpdateImporter(getProject());
            }
        });
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_BOM_DB, "!!XML Teilestammdaten (TEIL)...", null, false, new EtkFunctionHelper("iPartsBOMPartMasterDataUpdateImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new BOMPartMasterDataUpdateImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_BOM_DB, "!!XML Teilestammdaten für Baukasten (TEIL)...", null, false, new EtkFunctionHelper("iPartsBOMPartMasterDataUpdateImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new BOMPartHistoryUpdateImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_BOM_DB, "!!XML Ersatzteilkennzeichnung (TEID)...", null, false, new EtkFunctionHelper("iPartsBOMSparePartSignsUpdateImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new BOMSparePartSignsUpdateImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_BOM_DB, "!!XML Baukasteninhalt (BK)...", null, false, new EtkFunctionHelper("iPartsBOMConstructionKitContentUpdateImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new BOMConstructionKitContentUpdateImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_BOM_DB, "!!XML Baukastenverwendungsstellentexte (BKV)...", null, false, new EtkFunctionHelper("iPartsBOMConstructionKitTextUpdateImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new BOMConstructionKitTextUpdateImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_BOM_DB, "!!XML Wahlweise-Kennbuchstabe und Bemerkungsziffer (TEIE)...", null, false, new EtkFunctionHelper("iPartsBOMDBTEIEUpdateImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new EDSMaterialRemarksUpdateImporter(getProject());
            }
        });


//        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT_BOM_DB, null, null, false, null, IPARTS_MENU_NAME_IMPORT_DIALOG);
//        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT_BOM_DB, "!!Baukasteninhalt (BK/BKV)...", null, false, new EtkFunctionHelper("iPartsEDSConstKitImport") {
//            @Override
//            public AbstractDataImporter createImporter() {
//                return new MasterDataEdsConstructionKitImporter(getProject());
//            }
//        }, IPARTS_MENU_NAME_IMPORT_DIALOG);


        // Import Migration

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!RSK Lexikon...", null, false, new EtkFunctionHelper("iPartsRSKLexikonImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new RSKLexikonImporter(getProject());
            }
        });

        // Separator
        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT_MIGRATION, null, null, false, null, IPARTS_MENU_NAME_IMPORT_BOM_DB);

        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!MAD BM-Kataloge BL (TAL40A)...", null, false, new EtkFunctionHelper("iPartsMadTal40AImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new MadTal40AImporter(getProject());
            }
        }, IPARTS_MENU_NAME_IMPORT_BOM_DB);

        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!MAD SA-Kataloge BL (TAL46A)...", null, false, new EtkFunctionHelper("iPartsMadTal46AImport") {
            @Override
            public AbstractDataImporter createImporter() {
                return new MadTal46AImporter(getProject());
            }
        }, IPARTS_MENU_NAME_IMPORT_BOM_DB);

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!MAD Code-Stammdaten (TAL95A)...", null, false, new EtkFunctionHelper("iPartsMadTal95AImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new MADTal95AImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!MAD SAA/BK-Gültigkeit zu Baumuster (TAL83A)...", null, false, new EtkFunctionHelper("iPartsMadTal83AImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new MadTal83AImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!MAD Auto-Product-Select (TAL47S)...", null, false, new EtkFunctionHelper("iPartsTAL47SImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new MADTal47SImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!MAD Stammdaten (TAL31A)...", null, false, new EtkFunctionHelper("iPartsMigrationMasterDataMainImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new MADMasterDataMainImporter(getProject());
            }
        });

        // Separator
        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT_MIGRATION, null, null, false, null, IPARTS_MENU_NAME_IMPORT_BOM_DB);

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!MAD konsolidiertes Lexikon (Test)...", null, false, new EtkFunctionHelper("iPartsMadConsolidatedDictionaryImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new MADConsolidatedDictionaryImporter(getProject(), true);
            }
        });

        // Separator
        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT_MIGRATION, null, null, false, null, IPARTS_MENU_NAME_IMPORT_BOM_DB);

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!MAD Farbnummern-Stammdaten (ES2)...", null, false, new EtkFunctionHelper("iPartsMadEs2Importer") {
            @Override
            public AbstractDataImporter createImporter() {
                return new MADEs2Importer(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!MAD SA/SAA-Stammdaten...", null, false, new EtkFunctionHelper("iPartsMadSaSaaImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new MADSaSaaImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!MAD KG/TU-Referenzen...", null, false, new EtkFunctionHelper("iPartsMadKgTuImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new MADKgTuImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!MAD Baumuster...", null, false, new EtkFunctionHelper("iPartsMadModelMasterImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new MADModelMasterImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!MAD Lexikon...", null, false, new EtkFunctionHelper("iPartsMadDictionaryImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new MADDictionaryImporter(getProject());
            }
        });
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!MAD Teilestamm...", null, false, new EtkFunctionHelper("iPartsMadPartMasterDataImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new MADPartMasterDataImporter(getProject());
            }
        });
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!MAD FBM-ABM-Referenz (VFBM)...", null, false, new EtkFunctionHelper("iPartsMadVFBMImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new MADFAGGImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!MAD Ergänzungstexte...", null, false, new EtkFunctionHelper("iPartsMadETextImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new MADETexteImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!MAD Fußnotentexte...", null, false, new EtkFunctionHelper("iPartsMadFNTextImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new MADFootNoteTexteImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!MAD Applikationsliste...", null, false, new EtkFunctionHelper("iPartsMadApplicationListImporter") {
            @Override
            public AbstractFilesImporter createImporter() {
                return new MADApplicationListImporter(getProject());
            }
        });

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!MAD Feder-Mapping...", null, false, new EtkFunctionHelper("iPartsMadSpringImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new MADSpringMappingImporter(getProject());
            }
        });

        // Separator
        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT_MIGRATION, null, null, false, null, IPARTS_MENU_NAME_IMPORT_BOM_DB);

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!DIALOG Baureihe...", null, false, new EtkFunctionHelper("iPartsMigrationDIALOGSeriesMainImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new MigrationDialogSeriesMainImporter(getProject());
            }
        });


        // Separator
        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT_MIGRATION, null, null, false, null, IPARTS_MENU_NAME_IMPORT_BOM_DB);

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!PRIMUS Teilestamm...", null, false, new EtkFunctionHelper("iPartsPrimusPartImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new PrimusPartImporter(getProject());
            }
        });
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!PRIMUS Wahlweise-Hinweise...", null, false, new EtkFunctionHelper("iPartsPrimusWWPartImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new PrimusWWPartImporter(getProject());
            }
        });

        // Separator
        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT_MIGRATION, null, null, false, null, IPARTS_MENU_NAME_IMPORT_BOM_DB);

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!SRM Teilestamm...", null, false, new EtkFunctionHelper("iPartsSRMPartImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new SrmPartImporter(getProject());
            }
        });

        if (Constants.DEVELOPMENT) {
            // Separator
            manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT_MIGRATION, null, null, false, null, IPARTS_MENU_NAME_IMPORT_BOM_DB);

            manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!Devel-Mad-TAL40A-nach-Excel exportieren...", null, false, new EtkFunctionHelper("iPartsMadTal40AToExcelExport") {
                @Override
                public AbstractDataImporter createImporter() {
                    return new MadDevelTal40AToXlsConverter(getProject());
                }
            });


            manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!Devel-Mad-TAL46A-nach-Excel exportieren...", null, false, new EtkFunctionHelper("iPartsMadTal46AToExcelExport") {
                @Override
                public AbstractDataImporter createImporter() {
                    return new MadDevelTal46AToXlsConverter(getProject());
                }
            });


            manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!Devel-Mad-TAL40A-Menge analysieren...", null, false, new EtkFunctionHelper("iPartsMadTal40AAnalyseQuant") {
                @Override
                public AbstractDataImporter createImporter() {
                    return new MadDevelAnalyzeQuantityFieldTal40A(getProject());
                }
            });


            manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!Devel-Mad-TAL46A-Menge analysieren...", null, false, new EtkFunctionHelper("iPartsMadTal46AAnalyseQuant") {
                @Override
                public AbstractDataImporter createImporter() {
                    return new MadDevelAnalyzeQuantityFieldTal46A(getProject());
                }
            });


            manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!Devel-Mad-TAL46A-Intervalle analysieren...", null, false, new EtkFunctionHelper("iPartsMadTal46AAnalyseIntervall") {
                @Override
                public AbstractDataImporter createImporter() {
                    return new MadDevelAnalyzeIntervallTal46(getProject());
                }
            });

            // Separator
            manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT_MIGRATION, null, null, false, null, IPARTS_MENU_NAME_IMPORT_BOM_DB);

            manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!Devel-DIALOG Katalog importieren...", null, false, new EtkFunctionHelper("iPartsMigrationDIALOG_Catalogue_Importer") {
                @Override
                public AbstractDataImporter createImporter() {
                    // Importdatei: XYZ_produkt_steuer.del
                    return new MigrationDialogProductImporter(getProject(), false);
                }
            });

            manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!Devel-DIALOG BMRE/TDAT importieren...", null, false, new EtkFunctionHelper("iPartsMigrationDIALOG_BMRE_TDAT_Importer") {
                @Override
                public AbstractDataImporter createImporter() {
                    // Importdatei: XYZ_produkt_bm.del
                    return new MigrationDialogProductModelsImporter(getProject(), false);
                }
            });

            manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!Devel-DIALOG BTDP importieren...", null, false, new EtkFunctionHelper("iPartsMigrationDIALOG_BTDP_Importer") {
                @Override
                public AbstractDataImporter createImporter() {
                    // Importdatei: XYZ_baureihe_btdp.del
                    return new MigrationDialogBTDPImporter(getProject(), false);
                }
            });

            manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!Devel-DIALOG PODW importieren...", null, false, new EtkFunctionHelper("iPartsMigrationDIALOG_PODW_Importer") {
                @Override
                public AbstractDataImporter createImporter() {
                    // Importdatei: XYZ_baureihe_podw.del
                    return new MigrationDialogPODWImporter(getProject(), false);
                }
            });

            manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!Devel-DIALOG PEMQ importieren...", null, false, new EtkFunctionHelper("iPartsMigrationDIALOG_PEMQ_Importer") {
                @Override
                public AbstractDataImporter createImporter() {
                    // Importdatei: XYZ_baureihe_pemq.del
                    return new MigrationDialogResponseIdentsImporter(getProject(), false);
                }
            });

            manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!Devel-DIALOG PEMZ importieren...", null, false, new EtkFunctionHelper("iPartsMigrationDIALOG_PEMZ_Importer") {
                @Override
                public AbstractDataImporter createImporter() {
                    // Importdatei: XYZ_baureihe_pemq.del
                    return new MigrationDialogResponseSpikesImporter(getProject(), false);
                }
            });

            manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!Devel-DIALOG POSD importieren...", null, false, new EtkFunctionHelper("iPartsMigrationDIALOG_POSD_Importer") {
                @Override
                public AbstractDataImporter createImporter() {
                    // Importdatei: XYZ_baureihe_posd.del
                    return new MigrationDialogPosDImporter(getProject(), false);
                }
            });

            manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!Devel-DIALOG (####_baureihe_bm.del) (BMRE) importieren...", null, false, new EtkFunctionHelper("iPartsMigrationDIALOG_BMRE_Importer") {
                @Override
                public AbstractDataImporter createImporter() {
                    return new MigrationDialogProductSeriesModelImporter(getProject(), false);
                }
            });


            // C205_produkt_werke.del
            manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!Devel-DIALOG (####_produkt_werke.del) (KATW) importieren...", null, false, new EtkFunctionHelper("iPartsMigrationDIALOG_KATW_Importer") {
                @Override
                public AbstractDataImporter createImporter() {
                    return new MigrationDialogProductFactoriesImporter(getProject(), false);
                }
            });

            manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!Devel-DIALOG FTTE importieren...", null, false, new EtkFunctionHelper("iPartsMigrationDIALOG_FTTE_Importer") {
                @Override
                public AbstractDataImporter createImporter() {
                    // Importdatei: XYZ_baureihe_ftte.del
                    return new MigrationDialogProductColorTableFactoryImporter(getProject(), false);
                }
            });

            manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!Devel-DIALOG FTAB importieren...", null, false, new EtkFunctionHelper("iPartsMigrationDIALOG_FTAB_Importer") {
                @Override
                public AbstractDataImporter createImporter() {
                    // Importdatei: XYZ_baureihe_ftab.del
                    return new MigrationDialogProductColorTableFactoryFTabImporter(getProject(), false);
                }
            });

            manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!Devel-DIALOG DIAF importieren...", null, false, new EtkFunctionHelper("iPartsMigrationDIALOG_DIAF_Importer") {
                @Override
                public AbstractDataImporter createImporter() {
                    // Importdatei: XYZ_baureihe_diaf.del
                    return new MigrationDialogDIAFImporter(getProject(), false);
                }
            });

            manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!Devel-DIALOG SCTD importieren...", null, false, new EtkFunctionHelper("iPartsMigrationDIALOG_SCTD_Importer") {
                @Override
                public AbstractDataImporter createImporter() {
                    // Importdatei: XYZ_baureihe_sctd.del
                    return new MigrationDialogSCTDImporter(getProject(), false);
                }
            });

            manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!Devel-DIALOG RPOS importieren...", null, false, new EtkFunctionHelper("iPartsMigrationDIALOG_RPOS_Importer") {
                @Override
                public AbstractDataImporter createImporter() {
                    // Importdatei: XYZ_baureihe_rpos.del
                    return new MigrationDialogRPosImporter(getProject(), false);
                }
            });

            manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!Devel-DIALOG TTEL importieren...", null, false, new EtkFunctionHelper("iPartsMigrationDIALOG_TTEL_Importer") {
                @Override
                public AbstractDataImporter createImporter() {
                    // Importdatei: XYZ_baureihe_ttel.del
                    return new MigrationDialogTTelImporter(getProject(), false);
                }
            });

            manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!Devel-DIALOG FBST importieren...", null, false, new EtkFunctionHelper("iPartsMigrationDIALOG_FBST_Importer") {
                @Override
                public AbstractDataImporter createImporter() {
                    // Importdatei: XYZ_baureihe_fbst.del
                    return new MigrationDialogFBSTImporter(getProject(), false);
                }
            });
        }


        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT_EPC, "!!BM-Dictionary...", null, false, new EtkFunctionHelper("iPartsEPCModelDictionaryImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new EPCModelDictionaryImporter(getProject());
            }
        }, IPARTS_MENU_NAME_IMPORT_MIGRATION);

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_EPC, "!!BM-Footnotes-Dictionary...", null, false, new EtkFunctionHelper("iPartsEPCBMFootnotesDictionaryImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new EPCModelFootnoteImporter(getProject());
            }
        });
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_EPC, "!!SA-Dictionary...", null, false, new EtkFunctionHelper("iPartsEPCSADictionaryImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new EPCSaKgDescriptionImporter(getProject());
            }
        });
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_EPC, "!!SA-Footnotes-Dictionary...", null, false, new EtkFunctionHelper("iPartsEPCSAFootnotesDictionaryImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new EPCSaFootnoteImporter(getProject());
            }
        });
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_EPC, "!!Part-Nouns...", null, false, new EtkFunctionHelper("iPartsEPCPartDescriptionImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new EPCPartDescriptionImporter(getProject());
            }
        });
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_EPC, "!!Part-Desc...", null, false, new EtkFunctionHelper("iPartsEPCAddTextImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new EPCAddTextImporter(getProject());
            }
        });
        // Separator
        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT_EPC, null, null, false, null, IPARTS_MENU_NAME_IMPORT_MIGRATION);

        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_EPC, "!!BM-Footnotes (Referenzen)...", null, false, new EtkFunctionHelper("iPartsEPCBMFootnotesImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new EPCModelFootnoteRefImporter(getProject());
            }
        });
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_EPC, "!!SA-Footnotes (Referenzen)...", null, false, new EtkFunctionHelper("iPartsEPCSAFootnotesImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new EPCSaFootnoteRefImporter(getProject());
            }
        });

        // Separator
        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT_EPC, null, null, false, null, IPARTS_MENU_NAME_IMPORT_MIGRATION);
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_EPC, "!!SA-Masterdata...", null, false, new EtkFunctionHelper("iPartsEPCSaMasterDataImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new EPCSaMasterDataImporter(getProject()) {
                };
            }
        });
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_EPC, "!!SAA-Masterdata...", null, false, new EtkFunctionHelper("iPartsEPCSaaMasterDataImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new EPCSaaMasterDataImporter(getProject()) {
                };
            }
        });
        // Separator
        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT_EPC, null, null, false, null, IPARTS_MENU_NAME_IMPORT_MIGRATION);
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_EPC, "!!BM-Agg-Struktur...", null, false, new EtkFunctionHelper("iPartsEPCModelAggregateImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new EPCModelAggregateImporter(getProject());
            }
        });
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_EPC, "!!SA-Produkt-Struktur...", null, false, new EtkFunctionHelper("iPartsEPCSaProductStructureImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new EPCSaProductStructureImporter(getProject()) {
                };
            }
        });
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_EPC, "!!Produktstruktur (KG/TU)...", null, false, new EtkFunctionHelper("iPartsEPCProductStructureImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new EPCProductStructureImporter(getProject());
            }
        });
        // Separator
        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT_EPC, null, null, false, null, IPARTS_MENU_NAME_IMPORT_MIGRATION);
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_EPC, "!!BM-Parts...", null, false, new EtkFunctionHelper("iPartsEPCBMPartsImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new EPCModelPartListImporter(getProject());
            }
        });
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_EPC, "!!SA-Parts...", null, false, new EtkFunctionHelper("iPartsEPCSaPartsImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new EPCSaPartListImporter(getProject());
            }
        });
        // Separator
        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT_EPC, null, null, false, null, IPARTS_MENU_NAME_IMPORT_MIGRATION);
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_EPC, "!!BM-NPG (Bildreferenzen)...", null, false, new EtkFunctionHelper("iPartsEPCBMNPGImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new EPCModelPicReferenceImporter(getProject());
            }
        });
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_EPC, "!!SA-NPG (Bildreferenzen)...", null, false, new EtkFunctionHelper("iPartsEPCSANPGImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new EPCSaPicReferenceImporter(getProject());
            }
        });

        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT_SAP, "!!MBS Stammdaten...", null, false, new EtkFunctionHelper("iPartsMBSMasterDataImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                // Importer erzeugen
                MBSDataImporter importer = new MBSDataImporter(getProject(), "!!MBS Stammdaten");
                // Über die Form alle gewünschten Handler auswählen
                MBSDataSelectionForm form = new MBSDataSelectionForm("!!MBS Stammdaten Import", "!!Stammdaten für Import auswählen", "!!Alle Stammdaten");
                if (form.setSelectedHandler(importer, MBSImportHelper.createAllMasterDataHandlers(getProject(), importer))) {
                    return importer;
                } else {
                    return null;
                }
            }
        }, IPARTS_MENU_NAME_IMPORT_EPC);
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_SAP, "!!MBS Strukturdaten...", null, false, new EtkFunctionHelper("iPartsMBSStructureDataImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                // Importer erzeugen
                MBSDataImporter importer = new MBSDataImporter(getProject(), "!!MBS Strukturdaten");
                // Über die Form alle gewünschten Handler auswählen
                MBSDataSelectionForm form = new MBSDataSelectionForm("!!MBS Strukturdaten Import", "!!Strukturdaten für Import auswählen", "!!Alle Strukturdaten");
                if (form.setSelectedHandler(importer, MBSImportHelper.createAllStructureDataHandlers(getProject(), importer, false))) {
                    return importer;
                } else {
                    return null;
                }
            }
        });
        // DAIMLER-10318, Ident-Rückmeldungen aus ePEP (elektronischer ProduktionsEinsatzProzess)
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_EPEP, "!!ePEP (Ident-Rückmeldungen)...", null, false, new EtkFunctionHelper("iPartsePEPResponseImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new ePEPIdentResponseImporter(getProject());
            }
        });

        // DAIMLER-11961, CEMaT: Importer für die EinPAS-Knoten aus CEMaT
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT_CEMAT, "!!EinPAS-Knoten aus CEMaT...", null, false, new EtkFunctionHelper("iPartsCematModuleImporter") {
            @Override
            public AbstractDataImporter createImporter() {
                return new CematModuleDataImporter(getProject());
            }
        });

        combineItems(manager, eventImporterMenu);

    }

    /**
     * Führt die Import-Menüpunkte zusammen
     *
     * @param manager
     * @param eventImporterMenu
     */
    private void combineItems(MenuManager manager, GuiMenuItem eventImporterMenu) {
        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT, null, null, false, null, IPARTS_MENU_NAME_DICTIONARY);
        addItemsFromExistingMenu(manager, IPARTS_MENU_NAME_IMPORT_MIGRATION, "!!Migration");
        addItemsFromExistingMenu(manager, IPARTS_MENU_NAME_IMPORT_DIALOG, "!!DIALOG");
        manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT, eventImporterMenu);
        addItemsFromExistingMenu(manager, IPARTS_MENU_NAME_IMPORT_BOM_DB, "!!BOM-DB");
        addItemsFromExistingMenu(manager, IPARTS_MENU_NAME_IMPORT_EPEP, "!!ePEP");
        addItemsFromExistingMenu(manager, IPARTS_MENU_NAME_IMPORT_EPC, "!!EPC");
        addItemsFromExistingMenu(manager, IPARTS_MENU_NAME_IMPORT_SAP, "!!SAP");
        addItemsFromExistingMenu(manager, IPARTS_MENU_NAME_IMPORT_CEMAT, "!!CEMaT");
    }

    private void addItemsFromExistingMenu(MenuManager manager, String menuName, String subMenuName) {
        GuiMenuItem dialogSubItem = manager.addMenuFunction(IPARTS_MENU_NAME_IMPORT, manager.createMenuItem(subMenuName, null, false, false, null));
        List<AbstractGuiControl> items = new ArrayList<>(manager.getOrAddMainMenu(menuName).getChildren());
        for (AbstractGuiControl item : items) {
            dialogSubItem.addChild(item);
        }
        manager.removeMenuEntry(menuName);
    }

    private EtkFunction deleteEmptyModules() {
        EtkFunction deleteEmptyModulesFunction = new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                final iPartsDataModuleList allModulesList = iPartsDataModuleList.loadAllData(getProject());
                String title = TranslationHandler.translate("!!Suche leere Module... (%1 Module vorhanden)", String.valueOf(allModulesList.size()));
                final EtkMessageLogForm logForm = new EtkMessageLogForm("!!Leere Module aus Datenbank entfernen", title, null);
                logForm.showModal(new FrameworkRunnable() {
                    @Override
                    public void run(FrameworkThread thread) {
                        int count = 0;
                        int deletedModules = 0;
                        int maxCount = allModulesList.size();
                        for (iPartsDataModule module : allModulesList) {
                            if (Thread.currentThread().isInterrupted()) {
                                return;
                            }
                            count++;
                            EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getProject(), new AssemblyId(module.getAsId().getModuleNumber(), ""));
                            if (assembly instanceof iPartsDataAssembly) {
                                iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)assembly;
                                if (iPartsAssembly.getPartListUnfiltered(null).isEmpty()) {
                                    logForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Lösche leeres Modul \"%1\"", iPartsAssembly.getAsId().getKVari()));
                                    deletedModules++;
                                    iPartsAssembly.delete_iPartsAssembly(true);
                                }
                            }
                            logForm.getMessageLog().fireProgress(count, maxCount, "", false, true);
                        }
                        logForm.getMessageLog().fireMessage(TranslationHandler.translate("!!%1 Module durchsucht: %2 leere" +
                                                                                         " Module gefunden und gelöscht",
                                                                                         String.valueOf(maxCount),
                                                                                         String.valueOf(deletedModules)));
                        if (deletedModules > 0) {
                            ClearRetailRelevantCachesEvent.invalidateRetailRelevantCaches(false, false);
                        }
                    }
                });
            }
        };


        // Das Löschen der Module muss ohne aktive ChangeSets durchgeführt werden
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                owner.getProject().executeWithoutActiveChangeSets(() -> deleteEmptyModulesFunction.run(owner), true);
            }
        };
    }

    /**
     * Migration von bestimmten Feldern aus DA_PRODUCT_MODEL nach DA_MODEL.
     * Es sollen pro Baumuster die DA_PRODUCT_MODEL Einträge auf Gleichheit dieser Felder untersucht werden.
     * Wenn alle Einträge gleich sind, sollen diese Felder an das Baumuster 'verschoben' werden.
     * Das darf nur passieren solange das Baumuster noch nicht das Flag trägt, dass es in iParts bearbeitet wurde.
     *
     * @return
     */
    private EtkFunction moveDataFromProductModelsToModel() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                String title = TranslationHandler.translate("!!Meldungen");
                final EtkMessageLogForm logForm = new EtkMessageLogForm("!!Felder aus DA_PRODUCT_MODELS nach DA_MODEL verschieben", title, null);
                logForm.showModal(new FrameworkRunnable() {
                    @Override
                    public void run(FrameworkThread thread) {

                        logForm.getMessageLog().fireMessage(
                                TranslationHandler.translate("!!Lade %1 Datensätze aus %2",
                                                             String.valueOf(getProject().getDB().getRecordCount(TABLE_DA_PRODUCT_MODELS)),
                                                             TABLE_DA_PRODUCT_MODELS));

                        EtkDbObjectsLayer dbLayer = getProject().getDbLayer();

                        // Zur Transaktionssteuerung
                        dbLayer.startTransaction();
                        dbLayer.startBatchStatement();

                        try {
                            // Die Liste aller Produkt-Models holen ...
                            iPartsProductModels productModelsCache = iPartsProductModels.getInstance(getProject());
                            TreeSet<iPartsModelId> modelSet = productModelsCache.getAllModelIdsAsTreeSet();

                            // Ein paar Zähler für statistische Ausgaben
                            int modelCount = 0;
                            int modifiedModelCount = 0;
                            int modifiedProductModelCount = 0;

                            // Jetzt über die Liste der Baumuster-Produkte iterieren
                            Iterator<iPartsModelId> iterator = modelSet.iterator();
                            while (iterator.hasNext()) {

                                modelCount++;

                                iPartsModelId modelId = iterator.next();
                                iPartsDataModel dataModel = new iPartsDataModel(getProject(), modelId);
                                if (!dataModel.loadFromDB(modelId)) {
                                    // Fehlermeldung
                                    logForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Baumuster %1 nicht gefunden, wird übersprungen!",
                                                                                                     modelId.getModelNumber()));
                                    continue;
                                }

                                // Zu jedem Model die Produkt-Models suchen
                                Set<iPartsDataProductModels> productModelsByModel = productModelsCache.getProductModelsByModel(getProject(),
                                                                                                                               modelId.getModelNumber());
                                iPartsDataProductModelsList productModelsList = new iPartsDataProductModelsList();
                                if (productModelsByModel != null) {
                                    productModelsList.addAll(productModelsByModel, DBActionOrigin.FROM_DB);
                                }
                                // den Algorithmus zum Schaufeln der Daten anwenden ...
                                iPartsProductModelHelper.syncProductModelsWithModel(dataModel, productModelsList);
                                // ... bei Bedarf die Models speichern ...
                                if (dataModel.isNew() || dataModel.isModifiedWithChildren()) {
                                    dataModel.saveToDB();
                                    modifiedModelCount++;
                                }
                                // ... und bei Bedarf auch die Product-Models speichern.
                                for (iPartsDataProductModels dataProductModel : productModelsList) {
                                    if (dataProductModel.isNew() || dataProductModel.isModifiedWithChildren()) {
                                        dataProductModel.saveToDB();
                                        modifiedProductModelCount++;
                                    }
                                }

                                // Alle 100 Models eine Meldung ausgeben
                                if (modelCount % 100 == 0) {
                                    logForm.getMessageLog().fireMessage(TranslationHandler.translate("!!%1 Baumuster verarbeitet",
                                                                                                     String.valueOf(modelCount)));
                                }
                            }

                            // Zum Debuggen über den Schalter die Transaktionssteuerung benutzen um das (Zer-)Schreiben der Daten zu unterbinden.
                            if (saveToDB) {
                                dbLayer.endBatchStatement();
                                dbLayer.commit();
                            } else {
                                dbLayer.cancelBatchStatement();
                                dbLayer.rollback();
                            }
                            logForm.getMessageLog().fireMessage(TranslationHandler.translate("!!%1 Records verarbeitet. %2 Baumuster und %3 Produktbaumuster wurden modifiziert",
                                                                                             String.valueOf(modelCount),
                                                                                             String.valueOf(modifiedModelCount),
                                                                                             String.valueOf(modifiedProductModelCount)));

                            CacheHelper.invalidateCaches();
                        } catch (Exception e) {
                            dbLayer.cancelBatchStatement();
                            dbLayer.rollback();
                            Logger.getLogger().throwRuntimeException(e);
                        }
                    }
                });
            }
        };
    }

    private EtkFunction restartRFTSx() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                RFTSXImportHelper rftsxInstance = RFTSXImportHelper.getInstance();
                String rftsxMessage;
                if (rftsxInstance == null) {
                    rftsxMessage = TranslationHandler.translate("!!%1 Monitor konnte nicht neugestartet werden, da er nicht initialisiert wurde.",
                                                                MonitorTypes.RFTSX.getType());
                } else {
                    rftsxMessage = rftsxInstance.restartMonitor();
                    if (StrUtils.isEmpty(rftsxMessage)) {
                        rftsxMessage = TranslationHandler.translate("!!%1 Monitor konnte nicht neugestartet werden. Projektkonfiguration existiert nicht.",
                                                                    MonitorTypes.RFTSX.getType());
                    }
                }

                TranslationsImportHelper translationsInstance = TranslationsImportHelper.getInstance();
                String translationsMessage;
                if (translationsInstance == null) {
                    translationsMessage = TranslationHandler.translate("!!%1 Monitor konnte nicht neugestartet werden, da er nicht initialisiert wurde.",
                                                                       MonitorTypes.TRANSLATIONS.getType());
                } else {
                    translationsMessage = translationsInstance.restartMonitors();
                    if (StrUtils.isEmpty(translationsMessage)) {
                        translationsMessage = TranslationHandler.translate("!!%1 Monitor konnte nicht neugestartet werden. Projektkonfiguration existiert nicht.",
                                                                           MonitorTypes.TRANSLATIONS.getType());
                    }
                }
                TranslationsObjectStoreHelper.destroyInstance();
                TranslationsObjectStoreHelper.getInstance();

                MessageDialog.show(rftsxMessage + "\n\n" + translationsMessage);
            }
        };
    }

    private EtkFunction importImagesForReferences(final EtkProject project) {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                // Der Importer wird in startImageReferencesImport() mit einem extra erzeugten EtkMessageLog initialisiert.
                // Daher hier der Parameter "false".
                iPartsReferenceImagesImporter imagesImporter = new iPartsReferenceImagesImporter(getProject());
                DWFile fileDir = imagesImporter.getImportFileDir();
                if (fileDir != null) {
                    if (MessageDialog.showYesNo(TranslationHandler.translate("!!Möchten Sie wirklich die DASTi Referenzzeichnungen aus dem Verzeichnis \"%1\" manuell importieren?",
                                                                             fileDir.getAbsolutePath())) != ModalResult.YES) {
                        return;
                    }
                    imagesImporter.startImageReferencesImport(true);
                }
            }
        };
    }


    /**
     * Liefert das Root Verzeichnis für den Übersetzungsumfang
     *
     * @return
     */
    public static DWFile getTranslationRootDirectory() {
        String rootDirValue = iPartsImportPlugin.getPluginConfig().getConfigValueAsRawString(iPartsImportPlugin.CONFIG_TRANSIT_ROOT_DIR, "");
        if (StrUtils.isValid(rootDirValue)) {
            return iPartsImportPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsImportPlugin.CONFIG_TRANSIT_ROOT_DIR);
        }
        return null;
    }

    /**
     * Liefert das gewünschte Verzeichnis mit optionalen Prefix
     *
     * @param configOptionForDirectoryName
     * @return
     */
    public static DWFile getTranslationDirectory(UniversalConfigOption configOptionForDirectoryName) {
        if (configOptionForDirectoryName != null) {
            DWFile rootDirectory = getTranslationRootDirectory();
            if (rootDirectory != null) {
                String directoryName = iPartsImportPlugin.getPluginConfig().getConfigValueAsString(configOptionForDirectoryName);
                if (iPartsImportPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsImportPlugin.CONFIG_TRANSIT_USE_PREFIX)) {
                    String prefix = iPartsImportPlugin.getPluginConfig().getConfigValueAsString(iPartsImportPlugin.CONFIG_TRANSIT_DIR_NAME_PREFIX);
                    if (StrUtils.isValid(prefix)) {
                        directoryName = prefix + directoryName;
                    }
                }
                DWFile targetDirectory = rootDirectory.getChild(directoryName);
                if (targetDirectory.exists(1000)) {
                    return targetDirectory;
                }
            }
        }
        return null;
    }


    /**
     * Ermöglicht das Erzeugen einer anonymen Klasse und implementiert für alle Importer die run()-Funktion
     */
    private abstract class EtkFunctionHelper extends EtkFunction {

        public String importAliasName;

        // Hier wird der Importer verkabelt.
        public EtkFunctionHelper(String importAliasName) {
            this.importAliasName = importAliasName;
        }

        @Override
        public void run(AbstractJavaViewerForm owner) {

            AbstractFilesImporter importer = createImporter();
            if (importer == null) {
                return;
            }

            EditImportFilesForm editImportPartListsForm = new EditImportFilesForm(getProject(), importer);
            DWFile runningLogFile = importer.importJobRunning();
            if (editImportPartListsForm.showModal() != ModalResult.CANCEL) {
                if (importer.getErrorCount() == 0) { // -> ProcessedLogs
                    importer.setLogFile(iPartsJobsManager.getInstance().jobProcessed(runningLogFile), false);
                } else { // -> ErrorLogs
                    importer.setLogFile(iPartsJobsManager.getInstance().jobError(runningLogFile), false);
                }
            } else { // -> Abbruch
                iPartsJobsManager.getInstance().jobCancelled(runningLogFile, true);
                importer.setLogFile(null, false);
            }

        }

        // Hier ist die Verknüpfung zur anonymen Klasse; Diese wird auf diese Weise gezwungen createImporter() zu implementieren.
        abstract public AbstractFilesImporter createImporter();

    }

    /**
     * Eigene Klasse von {@link UniversalConfigurationPanel} um Varianten der Konfiguration zu verarbeiten
     */
    private static class iPartsImportUniversalConfigurationPanel extends UniversalConfigurationPanel {

        public iPartsImportUniversalConfigurationPanel(ConfigurationWindow host, UniversalConfiguration universalConfig, String panelTitle, boolean withScrollPane) {
            super(host, universalConfig, panelTitle, withScrollPane);
        }

        @Override
        public UniversalConfigurationPanel createVariant(String path) {
            // Wenn eine Variante der Konfiguration erzeugt wird, muss diese eigen Validatoren bekommen
            UniversalConfigurationPanel variant = super.createVariant(path);
            addValidatorsToConfigurationPanel(variant);
            return variant;
        }

    }
}
