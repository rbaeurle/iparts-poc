/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts;

import de.docware.apps.etk.base.config.EtkConfigConst;
import de.docware.apps.etk.base.config.EtkStartparameterConst;
import de.docware.apps.etk.base.config.db.datatypes.date.DateConfig;
import de.docware.apps.etk.base.config.db.datatypes.enumtypes.SetOfEnumDataType;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkEbenenDaten;
import de.docware.apps.etk.base.config.partlist.EtkStuecklistenDescription;
import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.db.EtkDbs;
import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.db.sqlbuilder.EtkSqlDbSelect;
import de.docware.apps.etk.base.docu.mainview.forms.AbstractDocuForm;
import de.docware.apps.etk.base.favorite.model.EtkFavorite;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormConnector;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.*;
import de.docware.apps.etk.base.forms.common.components.AbstractGuiTableColumnFilterFactory;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.forms.functions.EtkFunction;
import de.docware.apps.etk.base.forms.toolbar.ToolbarButtonAlias;
import de.docware.apps.etk.base.mechanic.imageview.forms.AssemblyImageForm;
import de.docware.apps.etk.base.mechanic.imageview.forms.AssemblyImageFormIConnector;
import de.docware.apps.etk.base.mechanic.imageview.model.ThumbnailsImageViewerConfig;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListForm;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.misc.StartPageType;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.EtkDbObjectsLayer;
import de.docware.apps.etk.base.project.common.EtkDataObjectArray;
import de.docware.apps.etk.base.project.common.EtkDataTextEntry;
import de.docware.apps.etk.base.project.common.EtkNoteKey;
import de.docware.apps.etk.base.project.docu.EtkDataDocumentHelper;
import de.docware.apps.etk.base.project.edocu.dataobjects.*;
import de.docware.apps.etk.base.project.edocu.ids.*;
import de.docware.apps.etk.base.project.events.*;
import de.docware.apps.etk.base.project.filter.EtkFilterItem;
import de.docware.apps.etk.base.project.filter.EtkFilterTyp;
import de.docware.apps.etk.base.project.filter.FilterArt;
import de.docware.apps.etk.base.project.filter.FilterMode;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.NavigationPath;
import de.docware.apps.etk.base.project.mechanic.drawing.*;
import de.docware.apps.etk.base.project.mechanic.ids.*;
import de.docware.apps.etk.base.relatedinfo.main.forms.AbstractRelatedInfoMainForm;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseForm;
import de.docware.apps.etk.base.relatedinfo.main.model.RelatedInfoDisplayOption;
import de.docware.apps.etk.base.search.model.*;
import de.docware.apps.etk.base.timer.AbstractDayOfWeekHandler;
import de.docware.apps.etk.base.viewermain.forms.JavaViewerMainWindow;
import de.docware.apps.etk.base.viewermain.forms.MainButtonInfo;
import de.docware.apps.etk.base.webservice.endpoints.dwkUpdate.WSDWKUpateConfigGUI;
import de.docware.apps.etk.base.webservice.endpoints.dwkUpdate.WSDWKUpdateEndpoint;
import de.docware.apps.etk.base.webservice.endpoints.dwkUpdate.WSDWKUpdateRequest;
import de.docware.apps.etk.plugins.AbstractJavaViewerSimpleEndpointPlugin;
import de.docware.apps.etk.plugins.EtkPluginConstants;
import de.docware.apps.etk.plugins.customer.daimler.iparts.application.iPartsFrameworkMain;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataGenVoPairingCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictMetaCacheId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsDataCodeList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsOmittedParts;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.iPartsSearchDataset;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualCalcFieldDocuRel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.transit.iPartsTransitMappingCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.events.PreventTransmissionToASPLMEnabledChangeEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.formattedfields.EditExtControlCalendar;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.formattedfields.EditExtControlDateTimeEditPanel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.responsive.iPartsNavigationButtonModelFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.footnotes.iPartsDIALOGFootNotesCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.footnotes.iPartsPartFootnotesCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.footnotes.iPartsStandardFootNotesCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ldap.LDAPHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ldap.LdapSecurityOptions;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ldap.LdapUser;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsPRIMUSReplacementsCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.images.iPartsDefaultImages;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.eds.iPartsEdsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.mbs.iPartsMBSModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPas;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.iPartsDataHmMSm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuForProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuTemplate;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs.MBSStructure;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs.MBSTextEntryCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.modelelementusage.ModelElementUsage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.modelelementusage.ModuleConstructionCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.modelelementusage.SubModuleConstructionCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.ops.Ops;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.ops.OpsScopeCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsStructure;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.mapping.MappingHmMSmToEinPas;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.mapping.MappingKgTuToEinPas;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.mapping.MappingOpsToEinPas;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.iPartsNote;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.dia4u.iPartsDia4UServiceUtils;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.mediaservice.MediaServiceWebserviceUtils;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.mediaservice.mediaobjects.MediaServiceMediaObjectsService;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.proVal.iPartsProValWebserviceUtils;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.iPartsTruckBOMFoundationWebserviceUtils;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.XMLObjectCreationHelper;
import de.docware.apps.etk.plugins.interfaces.*;
import de.docware.apps.etk.plugins.utils.GridFilterReturnType;
import de.docware.apps.etk.util.interappcom.EtkInterAppComHelper;
import de.docware.apps.etk.viewer.FrameworkMain;
import de.docware.apps.etk.viewer.JavaViewerApplication;
import de.docware.apps.etk.viewer.JavaViewerPathsManager;
import de.docware.framework.combimodules.config_gui.ConfigurationWindow;
import de.docware.framework.combimodules.config_gui.UniversalConfigurationPanel;
import de.docware.framework.combimodules.useradmin.config.UserAdminSettingsPanelOptions;
import de.docware.framework.combimodules.useradmin.config.UserAdminTreeCreator;
import de.docware.framework.combimodules.useradmin.db.*;
import de.docware.framework.combimodules.useradmin.db.factory.UserAdminDbActionsBuilder;
import de.docware.framework.combimodules.useradmin.user.UserObject;
import de.docware.framework.modules.config.ConfigBase;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.config.db.EtkFieldType;
import de.docware.framework.modules.config.db.VirtualFieldDefinition;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.config.defaultconfig.UniversalConfigOption;
import de.docware.framework.modules.config.defaultconfig.UniversalConfigOptionType;
import de.docware.framework.modules.config.defaultconfig.UniversalConfiguration;
import de.docware.framework.modules.config.defaultconfig.design.CustomizingSettings;
import de.docware.framework.modules.config.defaultconfig.design.DesignSettings;
import de.docware.framework.modules.config.defaultconfig.system.SystemSettings;
import de.docware.framework.modules.config.license.LicenseConfig;
import de.docware.framework.modules.db.*;
import de.docware.framework.modules.db.serialization.*;
import de.docware.framework.modules.gui.app.AbstractApplication;
import de.docware.framework.modules.gui.app.DWLayoutManager;
import de.docware.framework.modules.gui.app.DWLayoutMode;
import de.docware.framework.modules.gui.controls.*;
import de.docware.framework.modules.gui.controls.filechooser.FileChooserPurpose;
import de.docware.framework.modules.gui.controls.filechooser.GuiFileChooserDialog;
import de.docware.framework.modules.gui.controls.misc.DWFontStyle;
import de.docware.framework.modules.gui.controls.spinner.GuiIntSpinner;
import de.docware.framework.modules.gui.controls.table.GuiTable;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolButton;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolbarManager;
import de.docware.framework.modules.gui.design.DesignCategory;
import de.docware.framework.modules.gui.design.DesignImage;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.framework.modules.gui.misc.endpoint.FrameworkEndpointHelper;
import de.docware.framework.modules.gui.misc.endpoint.FrameworkSimpleEndpoint;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.startparameter.CommandLine;
import de.docware.framework.modules.gui.misc.startparameter.StartParameter;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.misc.validator.default_validators.GuiControlAbstractValidator;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.modules.gui.output.j2ee.misc.BrowserInfo;
import de.docware.framework.modules.gui.output.j2ee.misc.StaticConnectionUpdater;
import de.docware.framework.modules.gui.responsive.base.RButtonImages;
import de.docware.framework.modules.gui.responsive.base.theme.MaterialTheme;
import de.docware.framework.modules.gui.responsive.base.theme.MaterialThemeIconComponent;
import de.docware.framework.modules.gui.responsive.base.theme.Theme;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.framework.modules.gui.responsive.components.navigationmenu.RNavigationButtonModel;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.modules.gui.session.SessionManager;
import de.docware.framework.modules.gui.session.SessionType;
import de.docware.framework.modules.interappcom.ClusterEventInterface;
import de.docware.framework.modules.interappcom.ClusterManagerInterface;
import de.docware.framework.modules.interappcom.DefaultInterAppComMessageManager;
import de.docware.framework.modules.interappcom.transferobjects.GenericResponseDTO;
import de.docware.framework.modules.plugins.AbstractPlugin;
import de.docware.framework.modules.plugins.PluginRegistry;
import de.docware.framework.modules.plugins.interfaces.*;
import de.docware.framework.modules.webservice.restful.RESTfulEndpoint;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.cache.ClearObjectInstanceListInterface;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import de.docware.util.java1_1.Java1_1_Utils;
import de.docware.util.misc.observer.CallbackBinder;
import de.docware.util.misc.observer.ObserverCall;
import de.docware.util.misc.observer.ObserverCallback;
import de.docware.util.security.PasswordString;
import de.docware.util.sql.TableAndFieldName;
import de.docware.util.sql.dbobjects.AbstractDBObject;
import redis.clients.jedis.JedisPooled;

import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * iParts Plug-in, das sowohl für den Viewer als auch das Redaktionssystem verwendet wird und z.B. iParts-spezifische
 * virtuelle Navigationsstrukturen ermöglicht.
 */
public class iPartsPlugin extends AbstractJavaViewerSimpleEndpointPlugin implements iPartsConst, EtkDataObjectProviderInterface,
                                                                                    SerializedDBDataObjectProviderInterface,
                                                                                    VirtualFieldsInterface,
                                                                                    ModifyPartListTypesInterface, ModifyProgramNameInterface,
                                                                                    ShowStartWindowInterface, ReceiveEtkProjectEventInterface,
                                                                                    NeedsStaticConnectionUpdatesInterface,
                                                                                    ClusterManagerProviderInterface,
                                                                                    AdditionalModuleHeaderFieldsInterface,
                                                                                    ModifySecondToolbarInterface,
                                                                                    FilterInterface, FilterExtraInterface,
                                                                                    ModifyEditControlFactoryInterface,
                                                                                    ModifyUserNameInterface,
                                                                                    ModifyStatusBarInterface, SessionStartInterface,
                                                                                    ModifyRelatedInfoNotesInterface,
                                                                                    FireProjectEventVetoInterface,
                                                                                    AddHTMLToGuiWindowInterface,
                                                                                    ModifyMainToolbarInterface,
                                                                                    ModifyStartParameterInterface,
                                                                                    ModifyUserAdminCreationInterface,
                                                                                    ModifyMenuItemConfigClickInterface,
                                                                                    RestoreFavoritesFilterInterface,
                                                                                    CreateEtkFormInterface,
                                                                                    RelatedInfoForModifyDisplayTextInterface,
                                                                                    ModifyRMechanicNavigationMenuActionsInterface,
                                                                                    CheckSearchResultEntryValidityInterface {

    public static final String INTERNAL_PLUGIN_NAME = "iParts";
    public static final String OFFICIAL_PLUGIN_NAME = "DAIMLER iParts Plug-in"; // absichtlich kein Übersetzungstext
    public static final String PLUGIN_VERSION = "1.0";

    public static final String SESSION_KEY_LDAP_USER = "iparts_ldap_user";
    public static final String SESSION_KEY_SHOW_EINPAS_MAPPING = "iparts_show_einpas_mapping";
    public static final String SESSION_KEY_SELECT_EDS_CONST_MODEL = "iparts_select_eds_const_models"; // EDS Baumuster für die EDS Konstruktionsanzeige
    public static final String SESSION_KEY_SELECT_MBS_CONST_MODEL = "iparts_select_mbs_const_models"; // MBS Baumuster für die MBS Konstruktionsanzeige
    public static final String SESSION_KEY_SELECT_CTT_CONST_MODEL = "iparts_select_ctt_const_models"; // CTT Baumuster für die CTT Konstruktionsanzeige
    public static final String SESSION_KEY_DELAYED_DATA_CHANGED_EVENT = "iparts_delayed_data_changed_event"; // Boolean für einen verzögerten DataChangedEvent
    public static final String SESSION_KEY_MARK_EDS_KEM = "iparts_mark_eds_kem"; // KEM in EDS-Modulen markieren
    public static final String SESSION_KEY_MARK_MBS_KEM = "iparts_mark_mbs_kem"; // KEM in MBS-Modulen markieren
    public static final String SESSION_KEY_DBDATE_FOR_MBS_CONSTRUCTION = "iparts_dbDate_mbs_construction"; // DB-Datum für MSB-Konstruktion
    public static final String SESSION_KEY_SUPER_EDIT_REPLACEMENTS_FACTORY_DATA_POS = "iparts_super_edit_replacements_factory_data_pos"; //
    public static final String SESSION_KEY_SUPER_EDIT_PART_LIST_ENTRY_DATA_POS = "iparts_super_edit_part_list_entry_data_pos"; //

    protected static final String[] COLUMN_FILTER_VALUE_SEPARATORS = new String[]{ ",", "|" };

    public static final LogChannels LOG_CHANNEL_DEBUG = new LogChannels("DEBUG", true, true);
    public static final LogChannels LOG_CHANNEL_PERFORMANCE = new LogChannels("PERFORMANCE", true, true);
    public static final LogChannels LOG_CHANNEL_PART_LISTS_STACKTRACE = new LogChannels("PART_LISTS_STACKTRACE", true, true);
    public static final LogChannels LOG_CHANNEL_CODES = new LogChannels("CODES", true, true);
    public static final LogChannels LOG_CHANNEL_MQ = new LogChannels("MQ", true, true);
    public static final LogChannels LOG_CHANNEL_XML_MQ = new LogChannels("XML_MQ", true, true);
    public static final LogChannels LOG_CHANNEL_MODELS = new LogChannels("MODELS", true, true);
    public static final LogChannels LOG_CHANNEL_DATACARD_SERVICE = new LogChannels("DATACARD_SERVICE", true, true);
    public static final LogChannels LOG_CHANNEL_INTER_APP_COM_REGISTRY = new LogChannels("IAC_REGISTRY", false, true);
    public static final LogChannels LOG_CHANNEL_INTER_APP_COM_CLIENT = new LogChannels("IAC_CLIENT", false, true);
    public static final LogChannels LOG_CHANNEL_INTER_APP_COM_PING = new LogChannels("IAC_PING", false, true);
    public static final LogChannels LOG_CHANNEL_INTER_APP_COM_WEBSERVICE = new LogChannels("IAC_WEBSERVICE", false, true);
    public static final LogChannels LOG_CHANNEL_LDAP = new LogChannels("LDAP", true, true);
    public static final LogChannels LOG_CHANNEL_CHANGE_SETS = new LogChannels("CHANGE_SETS", true, true);
    public static final LogChannels LOG_CHANNEL_CHANGE_SET_SIM_FOR_JOINS = new LogChannels("CHANGE_SET_SIM_FOR_JOINS", true, true);
    public static final LogChannels LOG_CHANNEL_USER_ADMIN = new LogChannels("USER_ADMIN", true, true);
    public static final LogChannels LOG_CHANNEL_BUFFERED_SAVE = new LogChannels("BUFFERED_SAVE", true, true);
    public static final LogChannels LOG_CHANNEL_PUBLISHING = new LogChannels("PUBLISHING", true, true);
    public static final LogChannels LOG_CHANNEL_SINGLE_PIC_PARTS = new LogChannels("SINGLE_PIC_PARTS", true, true);
    public static final LogChannels LOG_CHANNEL_TRUCK_BOM_FOUNDATION = new LogChannels("TRUCK_BOM_FOUNDATION", true, true);
    public static final LogChannels LOG_CHANNEL_DIA_4_U = new LogChannels("DIA_4_U", true, true);
    public static final LogChannels LOG_CHANNEL_PROVAL = new LogChannels("PROVAL", true, true);
    public static final LogChannels LOG_CHANNEL_MAILBOX = new LogChannels("MAILBOX", true, true);

    public static final UniversalConfigOption CONFIG_SHOW_ANIMATIONS = UniversalConfigOption.getBooleanOption("/showAnimations", false);
    public static final UniversalConfigOption CONFIG_CACHES_WARM_UP = UniversalConfigOption.getBooleanOption("/cachesWarmUp", true);
    public static final UniversalConfigOption CONFIG_CACHES_PROVIDER_ACTIVE = UniversalConfigOption.getBooleanOption("/cachesProviderActive", true);
    public static final UniversalConfigOption CONFIG_CACHES_PROVIDER_IAC_ID = UniversalConfigOption.getStringOption("/cachesProviderIacId", "");
    public static final UniversalConfigOption CONFIG_CACHES_LIFE_TIME = UniversalConfigOption.getIntegerOption("/cachesLifeTime", -1);
    public static final UniversalConfigOption CONFIG_CACHES_REDIS_URL = UniversalConfigOption.getStringOption("/cachesRedisURL", "");
    public static final UniversalConfigOption CONFIG_CACHES_REDIS_PORT = UniversalConfigOption.getIntegerOption("/cachesRedisPort", 6379);
    public static final UniversalConfigOption CONFIG_CACHES_REDIS_USER_NAME = UniversalConfigOption.getStringOption("/cachesRedisUserName", "");
    public static final UniversalConfigOption CONFIG_CACHES_REDIS_PASSWORD = UniversalConfigOption.getPasswordOption("/cachesRedisPassword", PasswordString.EMPTY);
    public static final UniversalConfigOption CONFIG_CACHES_REDIS_LIFE_TIME = UniversalConfigOption.getIntegerOption("/cachesRedisLifeTime", 24 * 60); // 1 Tag

    public static final UniversalConfigOption CONFIG_MQ_INIT_DELAY = UniversalConfigOption.getIntegerOption("/mqInitDelay", 20);
    public static final UniversalConfigOption CONFIG_AUTO_IMPORTS_ENABLED = UniversalConfigOption.getBooleanOption("/autoImportsEnabled", true);

    public static final UniversalConfigOption CONFIG_SAVE_XML_FILES = UniversalConfigOption.getBooleanOption("/saveXMLFiles", false);
    public static final UniversalConfigOption CONFIG_SAVE_XML_BINARY_CONTENT_FILES = UniversalConfigOption.getBooleanOption("/saveXMLBinaryContentFiles", false);
    public static final UniversalConfigOption CONFIG_USE_RELEASED_STATE_AS_SUPPLIED_STATE = UniversalConfigOption.getBooleanOption("/useReleasedStateAsSuppliedState", true);
    public static final UniversalConfigOption CONFIG_XML_FILES_DIR = UniversalConfigOption.getFileOption("/xmlDir", new File("mqXmlFiles"));
    public static final UniversalConfigOption CONFIG_MQ_RECONNECT_ATTEMPTS = UniversalConfigOption.getIntegerOption("/mqReconnectAttempts", -1);
    public static final UniversalConfigOption CONFIG_MQ_RECONNECT_TIME = UniversalConfigOption.getIntegerOption("/mqReconnectTime", 10);
    public static final UniversalConfigOption CONFIG_PIC_REF_VALID_FILES = UniversalConfigOption.getStringOption("/picRefValidFiles", "png;sen");
    public static final UniversalConfigOption CONFIG_PIC_ORDER_REQUEST_DERIVED_PICS = UniversalConfigOption.getBooleanOption("/mqRequestDerivedPics", true);
    public static final UniversalConfigOption CONFIG_PIC_ORDER_ALLOW_PICORDER_COPIES = UniversalConfigOption.getBooleanOption("/allowPicOrderCopies", false);
    public static final UniversalConfigOption CONFIG_PIC_ORDER_PROJECT_ASSIGNMENT_PSK_PRODUCT = UniversalConfigOption.getStringOption("/mqProjectAssignmentPSKProduct", "AS_001_");
    public static final UniversalConfigOption CONFIG_PIC_ORDER_PROJECT_ASSIGNMENT_SPECIAL_PRODUCT = UniversalConfigOption.getStringOption("/mqProjectAssignmentSpecialProduct", "AS_000");
    public static final UniversalConfigOption CONFIG_PREVENT_TRANSMISSION_TO_ASPLM = UniversalConfigOption.getBooleanOption("/preventTransmissionToASPLM", false);


    public static final UniversalConfigOption CONFIG_INTER_APP_COM_CHANNEL_NAME = UniversalConfigOption.getStringOption("/interAppComChannelName", "iParts");
    public static final UniversalConfigOption CONFIG_INTER_APP_COM_IS_REGISTRY_SERVER = UniversalConfigOption.getBooleanOption("/interAppComIsRegistryServer", false);
    public static final UniversalConfigOption CONFIG_INTER_APP_COM_REGISTRY_SERVER_URL = UniversalConfigOption.getStringOption("/interAppComRegistryServerUrl", "");
    public static final UniversalConfigOption CONFIG_INTER_APP_COM_CLIENT_URL = UniversalConfigOption.getStringOption("/interAppComClientUrl", "");
    public static final UniversalConfigOption CONFIG_INTER_APP_COM_CLIENT_REGISTRY_ID = UniversalConfigOption.getStringOption("/interAppComClientRegistryId", "");
    public static final UniversalConfigOption CONFIG_INTER_APP_COM_PING_TIME = UniversalConfigOption.getIntegerOption("/interAppComPingTime", 60);
    public static final UniversalConfigOption CONFIG_INTER_APP_COM_RECONNECT_ATTEMPTS = UniversalConfigOption.getIntegerOption("/interAppComReconnectAttempts", -1);
    public static final UniversalConfigOption CONFIG_INTER_APP_COM_RECONNECT_TIME = UniversalConfigOption.getIntegerOption("/interAppComReconnectTime", 10);

    public static final UniversalConfigOption CONFIG_ADDITIONAL_GUIWINDOW_SCRIPT = UniversalConfigOption.getStringAreaOption("/additionalGuiWindowScript", "");

    public static final UniversalConfigOption CONFIG_SQL_PERFORMANCE_TESTS_DIR = UniversalConfigOption.getFileOption("/sqlPerformanceTestsDir", new File("sqlPerformanceTests"));

    public static final UniversalConfigOption CONFIG_WEBSERVICES_DATACARDS_BASE_URI = UniversalConfigOption.getStringOption("/webservicesDatacardsBaseURI", iPartsPlugin.WEBSERVICE_URI_DATACARDS_SIM_BASE);
    public static final UniversalConfigOption CONFIG_WEBSERVICES_DATACARDS_USE_V3 = UniversalConfigOption.getBooleanOption("/webservicesDatacardsUseV3", false);
    public static final UniversalConfigOption CONFIG_WEBSERVICES_DATACARDS_USE_RETAIL_NAME = UniversalConfigOption.getBooleanOption("/webservicesDatacardsUseRetailName", false);
    public static final UniversalConfigOption CONFIG_WEBSERVICES_DATACARDS_CACHE_LIFE_TIME = UniversalConfigOption.getIntegerOption("/webservicesDatacardsCacheLifeTime", 60 * 60); // Default 1h
    public static final UniversalConfigOption CONFIG_WEBSERVICES_DATACARDS_TIMEOUT = UniversalConfigOption.getIntegerOption("/webservicesDatacardsTimeout", 30); // Default 30s

    public static final UniversalConfigOption CONFIG_WEBSERVICES_SINGLE_PIC_PARTS_BASE_URI = UniversalConfigOption.getStringOption("/webservicesSinglePicPartsBaseURI", "");
    public static final UniversalConfigOption CONFIG_WEBSERVICES_SINGLE_PIC_VIEW_ACTIVE = UniversalConfigOption.getBooleanOption("/webservicesSinglePicViewActive", false);
    public static final UniversalConfigOption CONFIG_WEBSERVICES_SINGLE_PIC_PARTS_TOKEN_URL = UniversalConfigOption.getStringOption("/webservicesSinglePicPartsTokenURL", "https://sso-int.daimler.com/as/token.oauth2?grant_type=client_credentials");
    public static final UniversalConfigOption CONFIG_WEBSERVICES_SINGLE_PIC_PARTS_TOKEN_CLIENT_ID = UniversalConfigOption.getStringOption("/webservicesSinglePicPartsTokenClientId", "");
    public static final UniversalConfigOption CONFIG_WEBSERVICES_SINGLE_PIC_PARTS_TOKEN_CLIENT_SECRET = UniversalConfigOption.getStringOption("/webservicesSinglePicPartsTokenClientSecret", "");
    public static final UniversalConfigOption CONFIG_WEBSERVICES_SINGLE_PIC_PARTS_TOKEN_NAME = UniversalConfigOption.getStringOption("/webservicesSinglePicPartsTokenName", "Authorization"); // Authorization
    public static final UniversalConfigOption CONFIG_WEBSERVICES_SINGLE_PIC_PARTS_TOKEN_TYPE = UniversalConfigOption.getStringOption("/webservicesSinglePicPartsTokenType", "Bearer"); // Bearer

    public static final UniversalConfigOption CONFIG_WEBSERVICES_DATACARDS_TOKEN_URL = UniversalConfigOption.getStringOption("/webservicesDatacardTokenURL", "https://sso-int.daimler.com/as/token.oauth2?grant_type=client_credentials");
    public static final UniversalConfigOption CONFIG_WEBSERVICES_DATACARDS_TOKEN_CLIENT_ID = UniversalConfigOption.getStringOption("/webservicesDatacardTokenClientId", "");
    public static final UniversalConfigOption CONFIG_WEBSERVICES_DATACARDS_TOKEN_CLIENT_SECRET = UniversalConfigOption.getStringOption("/webservicesDatacardTokenClientSecret", "");
    public static final UniversalConfigOption CONFIG_WEBSERVICES_DATACARDS_TOKEN_NAME = UniversalConfigOption.getStringOption("/webservicesDatacardsTokenName", "Authorization"); // Authorization
    public static final UniversalConfigOption CONFIG_WEBSERVICES_DATACARDS_TOKEN_TYPE = UniversalConfigOption.getStringOption("/webservicesDatacardsTokenType", "Bearer"); // Bearer
    public static final UniversalConfigOption CONFIG_WEBSERVICES_DATACARDS_TOKEN = UniversalConfigOption.getStringOption("/webservicesDatacardsToken", "");

    public static final UniversalConfigOption CONFIG_WEBSERVICES_TRUCK_BOM_FOUNDATION_BASE_URI = UniversalConfigOption.getStringOption("/webservicesTruckBomFoundationBaseURI", "");
    public static final UniversalConfigOption CONFIG_WEBSERVICES_TRUCK_BOM_FOUNDATION_TOKEN_URL = UniversalConfigOption.getStringOption("/webservicesTruckBomFoundationTokenURL", "https://sso-int.daimler.com/as/token.oauth2?grant_type=client_credentials");
    public static final UniversalConfigOption CONFIG_WEBSERVICES_TRUCK_BOM_FOUNDATION_TOKEN_CLIENT_ID = UniversalConfigOption.getStringOption("/webservicesTruckBomFoundationTokenClientId", "");
    public static final UniversalConfigOption CONFIG_WEBSERVICES_TRUCK_BOM_FOUNDATION_TOKEN_CLIENT_SECRET = UniversalConfigOption.getStringOption("/webservicesTruckBomFoundationTokenClientSecret", "");
    public static final UniversalConfigOption CONFIG_WEBSERVICES_TRUCK_BOM_FOUNDATION_TOKEN_NAME = UniversalConfigOption.getStringOption("/webservicesTruckBomFoundationTokenName", "Authorization");
    public static final UniversalConfigOption CONFIG_WEBSERVICES_TRUCK_BOM_FOUNDATION_TOKEN_TYPE = UniversalConfigOption.getStringOption("/webservicesTruckBomFoundationTokenType", "Bearer");
    public static final UniversalConfigOption CONFIG_WEBSERVICES_TRUCK_BOM_FOUNDATION_USE_AS_CODE_SOURCE = UniversalConfigOption.getBooleanOption("/webservicesTruckBomFoundationUseAsCodeSource", false);

    public static final UniversalConfigOption CONFIG_WEBSERVICES_DIA_4_U_BASE_URI = UniversalConfigOption.getStringOption("/webservicesDia4UBaseURI", "");
    public static final UniversalConfigOption CONFIG_WEBSERVICES_DIA_4_U_TOKEN_URL = UniversalConfigOption.getStringOption("/webservicesDia4UBaseTokenURL", "https://sso-int.daimler.com/as/token.oauth2?grant_type=client_credentials");
    public static final UniversalConfigOption CONFIG_WEBSERVICES_DIA_4_U_TOKEN_CLIENT_ID = UniversalConfigOption.getStringOption("/webservicesDia4UTokenClientId", "");
    public static final UniversalConfigOption CONFIG_WEBSERVICES_DIA_4_U_TOKEN_CLIENT_SECRET = UniversalConfigOption.getStringOption("/webservicesDia4UTokenClientSecret", "");
    public static final UniversalConfigOption CONFIG_WEBSERVICES_DIA_4_U_TOKEN_NAME = UniversalConfigOption.getStringOption("/webservicesDia4UTokenName", "Authorization");
    public static final UniversalConfigOption CONFIG_WEBSERVICES_DIA_4_U_TOKEN_TYPE = UniversalConfigOption.getStringOption("/webservicesDia4UTokenType", "Bearer");

    public static final UniversalConfigOption CONFIG_WEBSERVICES_PROVAL_BASE_URI = UniversalConfigOption.getStringOption("/webservicesProValBaseURI", "");
    public static final UniversalConfigOption CONFIG_WEBSERVICES_PROVAL_TOKEN_URL = UniversalConfigOption.getStringOption("/webservicesProValTokenURL", "https://sso-int.daimler.com/as/token.oauth2?grant_type=client_credentials");
    public static final UniversalConfigOption CONFIG_WEBSERVICES_PROVAL_TOKEN_CLIENT_ID = UniversalConfigOption.getStringOption("/webservicesProValTokenClientId", "");
    public static final UniversalConfigOption CONFIG_WEBSERVICES_PROVAL_TOKEN_CLIENT_SECRET = UniversalConfigOption.getStringOption("/webservicesProValTokenClientSecret", "");
    public static final UniversalConfigOption CONFIG_WEBSERVICES_PROVAL_TOKEN_NAME = UniversalConfigOption.getStringOption("/webservicesProValTokenName", "Authorization");
    public static final UniversalConfigOption CONFIG_WEBSERVICES_PROVAL_TOKEN_TYPE = UniversalConfigOption.getStringOption("/webservicesProValTokenType", "Bearer");

    public static final UniversalConfigOption CONFIG_WEBSERVICES_XMLEXPORT_SEPARATE_ES12KEYS = UniversalConfigOption.getBooleanOption("/webservicesXmlExportSeparateEs12Keys", false);

    public static final UniversalConfigOption CONFIG_LDAP_AUTH_ACTIVE = UniversalConfigOption.getBooleanOption("/ldapAuthActive", false);
    public static final UniversalConfigOption CONFIG_LDAP_USE_SECURE_CONNECTION = UniversalConfigOption.getBooleanOption("/ldapUseSecureConnection", false);
    public static final UniversalConfigOption CONFIG_LDAP_HOST = UniversalConfigOption.getStringOption("/ldapHost", "");
    public static final UniversalConfigOption CONFIG_LDAP_PORT = UniversalConfigOption.getStringOption("/ldapPort", "");
    public static final UniversalConfigOption CONFIG_LDAP_USER = UniversalConfigOption.getStringOption("/ldapUser", "");
    public static final UniversalConfigOption CONFIG_LDAP_PASSWORD = UniversalConfigOption.getPasswordOption("/ldapPassword", PasswordString.EMPTY);
    public static final UniversalConfigOption CONFIG_LDAP_SECURITY = UniversalConfigOption.getStringListOptionSingleSelection("/ldapSecurity", LdapSecurityOptions.SIMPLE.getDescription());
    public static final UniversalConfigOption CONFIG_LDAP_SUB_SEARCHTREE = UniversalConfigOption.getStringOption("/ldapSubSearchTree", "");
    public static final UniversalConfigOption CONFIG_LDAP_SEARCH_WITH_FALLBACK = UniversalConfigOption.getBooleanOption("/ldapSearchFallback", true);
    public static final UniversalConfigOption CONFIG_LDAP_IPARTS_ROLE = UniversalConfigOption.getStringOption("/ldapiPartsRole", ""); // nur temporär, um zu zeigen, dass bestimmte Rollen gesucht werden können
    public static final UniversalConfigOption CONFIG_LDAP_SEARCH_ATTRIBUTE = UniversalConfigOption.getStringOption("/ldapSearchAttribute", LDAPHelper.LDAP_ATTRIBUTE_KEY_ROLE);
    public static final UniversalConfigOption CONFIG_LDAP_SEARCH_VALUE = UniversalConfigOption.getStringOption("/ldapSearchValue", "IPARTS.*");

    public static final UniversalConfigOption CONFIG_USER_ADMIN_ENABLED = UniversalConfigOption.getBooleanOption("/userAdminEnabled", true);
    public static final UniversalConfigOption CONFIG_LDAP_USER_REQUIRED_FOR_LOGIN = UniversalConfigOption.getBooleanOption("/ldapUserRequiredForLogin", false);
    public static final UniversalConfigOption CONFIG_GUEST_LOGIN = UniversalConfigOption.getBooleanOption("/guestLogin", true);
    public static final UniversalConfigOption CONFIG_LOGIN_NEEDED_FOR_ADMIN_MODE = UniversalConfigOption.getBooleanOption("/loginNeededForAdminMode", false);

    public static final UniversalConfigOption.UniversalConfigCustomOption CONFIG_LDAP_SYNC_INTERVAL = UniversalConfigOption.getCustomOption("/ldapSyncInterval", iPartsTimeIntervalCustomOptionWithControl.getTimeIntervalCustomOption(), "");
    public static final UniversalConfigOption CONFIG_LDAP_SYNC_DELAY = UniversalConfigOption.getIntegerOption("/ldapSyncDelay", 2 * 60); // 2 h

    public static final UniversalConfigOption CONFIG_SHOW_SAS_ONLY_FOR_FIN = UniversalConfigOption.getBooleanOption("/showSAsOnlyForFin", true);

    public static final UniversalConfigOption CONFIG_DISPLAY_PROVAL_CODE_DESC = UniversalConfigOption.getBooleanOption("/displayProvalCodeDesc", true);

    // DAIMLER-10211, Verarbeitung von PRIMUS-Hinweisen bei Truck
    public static final UniversalConfigOption CONFIG_PRIMUS_HINT_HANDLING = UniversalConfigOption.getBooleanOption("/handlingOfPrimusHints", true);

    // DAIMLER-14566: Truck: Zuordnung Produkt zu Supplier - Berechnung Arbeitsvorrat anpassen
    public static final UniversalConfigOption CONFIG_USE_PRODUCT_SUPPLIER = UniversalConfigOption.getBooleanOption("/useSupplierMapping", false);

    public static final UniversalConfigOption CONFIG_SHOW_ALTERNATIVE_PARTS_FOR_PRIMUS = UniversalConfigOption.getBooleanOption("/showAlternativePartsForPrimus", false);

    public static final UniversalConfigOption CONFIG_SHOW_EQUAL_PARTS = UniversalConfigOption.getBooleanOption("/showEqualParts", false);

    public static final UniversalConfigOption CONFIG_USE_SPK_MAPPING = UniversalConfigOption.getBooleanOption("/useSPKMapping", false);

    public static final UniversalConfigOption CONFIG_USE_CONFIG_DISPLAY_FIELD_WIDTH_AS_MAX = UniversalConfigOption.getBooleanOption("/useConfigDisplayFieldWidthAsMax", true);

    public static final UniversalConfigOption CONFIG_MAX_RESULTS_FOR_COMBOBOX_SEARCH = UniversalConfigOption.getIntegerOption("/maxResultsForComboBoxSearch", 50);

    public static final UniversalConfigOption CONFIG_MAX_VALIDITY_REPORT_CONST_NODE_CALCULATIONS = UniversalConfigOption.getIntegerOption("/maxValidityReportConstNodeCalculations", 60);

    public static final UniversalConfigOption CONFIG_CHECK_PUBLISHING_POLLING_DELAY = UniversalConfigOption.getIntegerOption("/checkPublishingPollingDelay", 1);

    public static final UniversalConfigOption CONFIG_IGNORE_INVALID_FACTORIES_IN_FILTER = UniversalConfigOption.getBooleanOption("/ignoreInvalidFactoriesInFilter", false);
    public static final UniversalConfigOption CONFIG_FILTER_WIRE_HARNESS_PARTS = UniversalConfigOption.getBooleanOption("/filterWireHarnessParts", false);
    public static final UniversalConfigOption CONFIG_WIRE_HARNESS_DUMMY_PART_NO = UniversalConfigOption.getStringOption("/wireHarnessDummyPartNo", "Q000000000001");
    public static final UniversalConfigOption CONFIG_LIMIT_FOR_CODE_SIMPLIFICATION = UniversalConfigOption.getIntegerOption("/limitCodeSimplification", 500);

    // Das Running Verzeichnis über die Convenience Methode "getRunningJobDir()" holen (siehe weiter unten)und nicht direkt über die UniversalConfig
    public static final UniversalConfigOption CONFIG_LIMIT_JOB_LOGS_IN_TABLE = UniversalConfigOption.getIntegerOption("/limitJobLogsInTable", 500);
    public static final UniversalConfigOption CONFIG_JOB_LOGS_DIR_RUNNING = UniversalConfigOption.getFileOption("/importLogsDirRunning", new File(""));
    public static final UniversalConfigOption CONFIG_JOB_LOGS_DIR = UniversalConfigOption.getFileOption("/importLogsDir", new File("importLogs"));
    public static final UniversalConfigOption CONFIG_JOB_LOGS_ARCHIVE_DIR = UniversalConfigOption.getFileOption("/importLogsArchiveDir", new File("importLogsArchive"));

    public static final UniversalConfigOption CONFIG_USE_CHANGESET_SIM_FOR_JOINS = UniversalConfigOption.getBooleanOption("/useChangeSetSimForJoins", true);
    public static final UniversalConfigOption CONFIG_MAX_NEW_DATA_OBJECTS_IN_CHANGESET_SIM_FOR_JOINS = UniversalConfigOption.getIntegerOption("/maxNewDataObjectsInChangeSetSimForJoins", 100);
    public static final UniversalConfigOption CONFIG_CHECK_MODEL_VISIBILITY = UniversalConfigOption.getBooleanOption("/checkModelVisibility", true);

    public static final UniversalConfigOption CONFIG_DIALOG_CONVERT_SAA = UniversalConfigOption.getBooleanOption("/dialogConvertSAA", false);

    public static final UniversalConfigOption CONFIG_SVG_FONT_NAME = UniversalConfigOption.getStringOption("/SVGfontName", "");
    public static final UniversalConfigOption CONFIG_SVG_OUTLINE_VERSION_2 = UniversalConfigOption.getBooleanOption("/SVGoutlineVersion2", false);

    public static WSDWKUpateConfigGUI wsDWKUpdateGUI;

    public static final String SIM_AUTO_RESPONSE_DELAY = "/simAutoResponseDelay";
    public static final String SIM_AUTO_RESPONSE_DELAY_SEARCH = "/simAutoResponseDelaySearch";
    public static final String XML_SCHEMA_ASPLM_MEDIA = "asplm_media_schema.xsd";
    public static final String XML_SCHEMA_ASPLM_DIALOG = "asplm_dialog_schema.xsd";
    public static final String XML_SCHEMA_ASPLM_EDS = "asplm_eds_schema.xsd";
    public static final String XML_SCHEMA_AS_CODEDATA = "as_codedata_schema.xsd";
    public static final String XML_SCHEMA_PRIMUS_PARTS = "schemas/primus/part.xsd";

    public static final MQChannelType MQ_CHANNEL_TYPE_MEDIA = new MQChannelType(iPartsMQChannelTypeNames.MEDIA, "jms/MediaQueueOut", "jms/MediaQueueIn");
    public static final String XML_MESSAGE_MANAGER_NAME_MEDIA = "xmlMessageManagerForMQMedia";

    public static final Colors clPlugin_iParts_FilterSelectInvalidForegroundColor = new Colors(DesignCategory.PLUGIN, "FilterSelectInvalidForeground",
                                                                                               Color.red, true);
    public static final Colors clPlugin_iParts_CodeMatrixBackground_positive = new Colors(DesignCategory.PLUGIN, "CodeMatrixBackgroundPositive",
                                                                                          new Color(0xe0ffe0), true);
    public static final Colors clPlugin_iParts_CodeMatrixBackground_negative = new Colors(DesignCategory.PLUGIN, "CodeMatrixBackgroundNegative",
                                                                                          new Color(0xffe0e0), true);
    public static final Colors clPlugin_iParts_CodeMatrixFontColor_positive = new Colors(DesignCategory.PLUGIN, "CodeMatrixFontColorPositive",
                                                                                         Color.black, true);
    public static final Colors clPlugin_iParts_CodeMatrixFontColor_negative = new Colors(DesignCategory.PLUGIN, "CodeMatrixFontColorNegative",
                                                                                         Color.black, true);

    public static final Colors clPlugin_iParts_SVG_inactiveTextColor = new Colors(DesignCategory.PLUGIN, "SVGinactiveTextColor",
                                                                                  Color.gray, true);
    public static final Colors clPlugin_iParts_SVG_highlightPathColor = new Colors(DesignCategory.PLUGIN, "SVGhighlightPathColor",
                                                                                   new Color(0xca7b2c), true);
    public static final Colors clPlugin_iParts_SVG_basePathColor = new Colors(DesignCategory.PLUGIN, "SVGbasePathColor",
                                                                              new Color(0xa8bacc), true);
    public static final Colors clPlugin_iParts_SVG_highlightBackgroundColor = new Colors(DesignCategory.PLUGIN, "SVGhighlightBackgroundColor",
                                                                                         new Color(0xca7b2c), true);
    public static final Colors clPlugin_iParts_SVG_highlightTextColor = new Colors(DesignCategory.PLUGIN, "SVGhighlightTextColor",
                                                                                   new Color(0x555511), true);
    public static final Colors clPlugin_iParts_SVG_inCartPathColor = new Colors(DesignCategory.PLUGIN, "SVGinCartPathColor",
                                                                                new Color(0x33bdf2), true);
    public static final Colors clPlugin_iParts_SVG_inCartBackgroundColor = new Colors(DesignCategory.PLUGIN, "SVGinCartBackgroundColor",
                                                                                      new Color(0x00adef), true);
    public static final Colors clPlugin_iParts_SVG_inCartTextColor = new Colors(DesignCategory.PLUGIN, "SVGinCartTextColor",
                                                                                new Color(0x237FA0), true);

    public static String XML_SCHEMA_PATH = ".";

    public static final String TABLE_FOR_EVALUATION_RESULTS = "VIRTUAL_EVAL_TABLE";

    public static final String RELATEDINFO_DISPLAYTEXT_DELIMITER = "; ";

    // --------- Variablen nur für die Unittests -----------
    public static boolean forceRevisionsHelperForTesting;
    public static boolean forceWSPluginActiveForTesting;
    // -----------------------------------------------------

    private static UniversalConfiguration pluginConfig;
    private static boolean isEditPluginActive;
    private static boolean isImportPluginActive;
    private static boolean isExportPluginActive;
    private static boolean isWebservicePluginActive;
    private static boolean isWebservicePluginPresent;
    private static boolean isCachesProviderActive;
    private static String cachesProviderIacId;
    private static boolean isWebservicesXMLExportSeparateES12Keys;
    private static boolean isShowAlternativePartsForPRIMUS;
    private static boolean isUseSPKMapping;
    private static boolean isPrimusHintHandling;
    private static boolean isFilterWireHarnessParts;
    private static String wireHarnessDummyPartNo;
    private static boolean isWebservicesSinglePicViewActive;
    private static String webservicesSinglePicPartsBaseURI;

    // Bei diesen Spalten werden die leeren Enum Werte für den Tabellen Spaltenfilter entfernt weil im Enum selbst ein ensprechender Wert bereitgestellt wird
    private static final String[] specialEnumsWithoutBlankElement = new String[]{ TableAndFieldName.make(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DD_CALCULATED_BUSINESS_CASE),
                                                                                  TableAndFieldName.make(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_RETAILUSE),
                                                                                  TableAndFieldName.make(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_DOCU_RELEVANT),
                                                                                  TableAndFieldName.make(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA),
                                                                                  TableAndFieldName.make(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA_SOE),
                                                                                  TableAndFieldName.make(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.RETAIL_CHANGED_CODE),
                                                                                  TableAndFieldName.make(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.RETAIL_MODIFIED_STATE),
                                                                                  TableAndFieldName.make(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DA_QUALITY_CHECK_ERROR) };

    // Die Namen mqSession und mqProject sind nicht mehr aktuell, da beide mittlerweile für allgemeine Zwecke verwendet werden,
    // z.B. f.d. Benutzerverwaltung
    private static Session mqSession;
    private static EtkProject mqProject;
    private boolean mqInitialized;

    private static Session ldapSession;
    private static EtkProject ldapProject;

    private static iPartsPublishingHelper publishingHelper;
    private static boolean isCheckModelVisibility;

    private final CallbackBinder callbackBinder = new CallbackBinder();
    private static ObserverCallback preventTransmissionToASPLMEnabledChangeEvent;

    private EtkInterAppComHelper interAppComHelper;
    private final DefaultInterAppComMessageManager interAppComMessageManager = new DefaultInterAppComMessageManager();

    private boolean warmUpCachesRequested;
    private final Object warmUpCachesRequestedSyncObject = new Object();
    private final Object warmUpCachesSyncObject = new Object();
    private static JedisPooled redisPool;
    private static Integer redisCacheLifeTime;

    private String host;
    private int port;

    public static UniversalConfiguration getPluginConfig() {
        return pluginConfig;
    }

    /**
     * Liefert die Lebensdauer für die meisten Caches in Sekunden zurück.
     *
     * @return
     */
    public static int getCachesLifeTime() {
        if (pluginConfig == null) { // Wird für die Unittests benötigt
            Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, "iParts pluginConfig is null -> using infinite cache life time");
            return -1;
        }

        int cachesLifeTimeInMinutes = pluginConfig.getConfigValueAsInteger(CONFIG_CACHES_LIFE_TIME);
        if (cachesLifeTimeInMinutes > 0) {
            return cachesLifeTimeInMinutes * 60;
        } else {
            return -1;
        }
    }

    /**
     * Liefert die (maximale) Lebensdauer für den Redis-Cache in Sekunden zurück.
     *
     * @return
     */
    public static int getRedisCacheLifeTime() {
        Integer redisCacheLifeTimeLocal = redisCacheLifeTime;
        if (redisCacheLifeTimeLocal == null) {
            if (pluginConfig == null) { // Wird für die Unittests benötigt
                Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, "iParts pluginConfig is null -> using 1 hour Redis cache life time");
                redisCacheLifeTimeLocal = 60 * 60;
            } else {
                int redisCacheLifeTimeInMinutes = pluginConfig.getConfigValueAsInteger(CONFIG_CACHES_REDIS_LIFE_TIME);
                if (redisCacheLifeTimeInMinutes > 0) {
                    redisCacheLifeTimeLocal = redisCacheLifeTimeInMinutes * 60;
                } else {
                    redisCacheLifeTimeLocal = (int)CONFIG_CACHES_REDIS_LIFE_TIME.getDefaultValue();
                }
            }
            redisCacheLifeTime = redisCacheLifeTimeLocal;
        }
        return redisCacheLifeTimeLocal;
    }

    public static JedisPooled getRedisPool() {
        return redisPool;
    }

    /**
     * Erzeugt einen Cache mit den Daten von einem Caches-Provider {@code T} anstatt die Daten lokal aus der DB zu laden.
     *
     * @param <T>
     * @param cache
     * @param cacheParameters
     * @param cacheInstances
     * @param hashObject
     * @return
     */
    public static <T extends CacheForGetCacheDataEvent> T createCacheInstanceWithCachesProvider(T cache, Map<String, String> cacheParameters,
                                                                                                ClearObjectInstanceListInterface<Object, T> cacheInstances,
                                                                                                Object hashObject) {
        String cachesProviderRegistryId = getCachesProviderIacId();
        if (!isCachesProviderActive() && !cachesProviderRegistryId.isEmpty()) {
            String cacheName = cache.getCacheName();
            Logger.log(LOG_CHANNEL_PERFORMANCE, LogType.DEBUG, "Creating cache \"" + cacheName + "\" with data from the caches provider with registry ID \""
                                                               + cachesProviderRegistryId + "\"...");

            T result = null;
            int cacheInstancesCount = cacheInstances.size();
            GetCacheDataEvent getCacheDataEvent = new GetCacheDataEvent(cacheName, cacheParameters);
            GenericResponseDTO response = ApplicationEvents.fireEventInAllProjectsAndClusters(getCacheDataEvent, false, false, true,
                                                                                              cachesProviderRegistryId, null, false);
            if ((response != null) && response.isSuccess()) {
                // Auf das Erzeugen vom Cache warten
                try {
                    synchronized (cacheInstances) {
                        cacheInstances.wait(MAX_WAIT_TIME_FOR_CACHE_PROVIDER);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            if (hashObject != null) {
                result = cacheInstances.get(hashObject);
            } else if (cacheInstances.size() > cacheInstancesCount) {
                // Ohne HashObject den aufrufenden Cache zurückgeben, sobald ein neuer Cache-Eintrag hinzugefügt wurde
                result = cache;
            }

            if ((response != null) && !response.isSuccess()) {
                isCachesProviderActive = true; // Bei einer Fehlerantwort den eigenen Cluster-Knoten zumindest temporär als Caches-Provider konfigurieren
                Logger.log(LOG_CHANNEL_PERFORMANCE, LogType.ERROR, "Error response for creating the cache \"" + cacheName
                                                                   + "\" with data from the caches provider with registry ID \""
                                                                   + cachesProviderRegistryId + "\": " + response.getResponseMessage());
            } else if (result == null) {
                isCachesProviderActive = true; // Bei einem Timout den eigenen Cluster-Knoten zumindest temporär als Caches-Provider konfigurieren
                Logger.log(LOG_CHANNEL_PERFORMANCE, LogType.ERROR, "Timeout after " + (MAX_WAIT_TIME_FOR_CACHE_PROVIDER / 1000)
                                                                   + " s for creating the cache \"" + cacheName + "\" with data from the caches provider with registry ID \""
                                                                   + cachesProviderRegistryId + "\"");
            } else {
                Logger.log(LOG_CHANNEL_PERFORMANCE, LogType.DEBUG, "Cache \"" + cacheName + "\" created successfully with data from the caches provider with registry ID \""
                                                                   + cachesProviderRegistryId + "\"");
            }
            return result;
        }
        return null;
    }

    public static boolean isEditPluginActive() {
        return isEditPluginActive;
    }

    public static boolean isImportPluginActive() {
        return isImportPluginActive;
    }

    public static boolean isExportPluginActive() {
        return isExportPluginActive;
    }

    public static boolean isWebservicePluginActive() {
        return isWebservicePluginActive || forceWSPluginActiveForTesting;
    }

    public static boolean isWebservicePluginPresent() {
        return isWebservicePluginPresent;
    }

    public static boolean isCachesProviderActive() {
        return isCachesProviderActive;
    }

    public static String getCachesProviderIacId() {
        return cachesProviderIacId;
    }

    public static boolean isWebservicesXMLExportSeparateES12Keys() {
        return isWebservicesXMLExportSeparateES12Keys;
    }

    public static boolean isShowAlternativePartsForPRIMUS() {
        return isShowAlternativePartsForPRIMUS;
    }

    public static boolean isCheckModelVisibility() {
        return isCheckModelVisibility;
    }

    public static boolean isUseSPKMapping() {
        return isUseSPKMapping;
    }

    public static boolean isPrimusHintHandling() {
        return isPrimusHintHandling;
    }

    public static boolean isFilterWireHarnessParts() {
        return isFilterWireHarnessParts;
    }

    public static String getWireHarnessDummyPartNo() {
        return wireHarnessDummyPartNo;
    }

    public static boolean isWebservicesSinglePicViewActive() {
        return isWebservicesSinglePicViewActive;
    }

    public static String getWebservicesSinglePicPartsBaseURI() {
        return webservicesSinglePicPartsBaseURI;
    }

    public static boolean isUseProductSupplier() {
        return pluginConfig.getConfigValueAsBoolean(CONFIG_USE_PRODUCT_SUPPLIER);
    }

    /**
     * Liefert die zentrale {@link Session} ohne GUI für die Kommunikation über MQ und andere zentrale Aktionen, die eine
     * sets verfügbare {@link Session} benötigen.
     *
     * @return
     */
    public static Session getMqSession() {
        return mqSession;
    }

    /**
     * Liefert die zentrale {@link Session} ohne GUI für die Synchronisierung der LDAP-Benutzer.
     *
     * @return
     */
    public static Session getLDAPSession() {
        return ldapSession;
    }

    /**
     * Liefert das zentrale {@link EtkProject} ohne GUI für die Kommunikation über MQ und andere zentrale Aktionen, die
     * ein sets verfügbares {@link EtkProject} benötigen.
     *
     * @return
     */
    public static EtkProject getMqProject() {
        return mqProject;
    }

    /**
     * Setzt das zentrale {@link EtkProject} ohne GUI für die Kommunikation über MQ und andere zentrale Aktionen, die
     * ein sets verfügbares {@link EtkProject} benötigen.
     * <br/><b>Diese Methode darf NUR für Tests aufgerufen werden!</b>
     *
     * @param project
     */
    public static void __internal_setMqProject(EtkProject project) {
        if (mqProject == null) {
            mqProject = project;
        }
    }

    /**
     * Liefert das zentrale {@link EtkProject} ohne GUI für die Synchronisierung mit LDAP.
     *
     * @return
     */
    public static EtkProject getLDAPProject() {
        return ldapProject;
    }

    /**
     * Stellt sicher, dass die DB-Verbindung von dem übergebenen {@link EtkProject} (primär MQ Project) aktiv ist und
     * stellt sie im Zweifelsfall wieder her.
     *
     * @param project
     */
    public static void assertProjectDbIsActive(EtkProject project, String projectName, LogChannels logChannel) {
        // Aktiv-Zustand der DB-Verbindung vom EtkProject überprüfen und im Zweifelsfall wiederherstellen
        if (project != null) {
            project.assertDBConnectionIsActive(projectName, logChannel);
        }
    }

    /**
     * Setzt für die übergebene Session die Wartezeit in Sekunden für simulierte automatische AS-PLM Antworten für den übergebenen
     * Schlüssel auf den angegebenen Wert.
     *
     * @param session
     * @param simAutoResponseDelayKey
     * @param value
     */
    public static void setSimAutoResponseDelayForSession(Session session, String simAutoResponseDelayKey, int value) {
        if (session != null) {
            session.setAttribute(INTERNAL_PLUGIN_NAME + simAutoResponseDelayKey, value);
        }
    }

    /**
     * Liefert für die aktive Session die Wartezeit in Sekunden für simulierte automatische AS-PLM Antworten für den übergebenen
     * Schlüssel zurück mit Fallback auf die Einstellungen im Admin-Modus.
     *
     * @param simAutoResponseDelayKey
     * @return
     */
    public static int getSimAutoResponseDelayForSession(String simAutoResponseDelayKey) {
        Session session = Session.get();
        if (session != null) {
            Integer value = (Integer)session.getAttribute(INTERNAL_PLUGIN_NAME + simAutoResponseDelayKey);
            if (value != null) {
                return value;
            }
        }
        return -1;
    }

    /**
     * Liefert den {@link LdapUser} für die aktuelle Session zurück.
     *
     * @return
     */
    public static LdapUser getLdapUserForSession() {
        Session session = Session.get();
        if (session != null) {
            return (LdapUser)session.getAttribute(SESSION_KEY_LDAP_USER);
        }
        return null;
    }

    /**
     * Liefert den {@link iPartsPublishingHelper} für das Publizieren und Aktualisieren von Retail-Daten zurück.
     *
     * @return
     */
    public static iPartsPublishingHelper getPublishingHelper() {
        return publishingHelper;
    }

    public static void restartLDAPSyncThread() {
        try {
            getLDAPProject().getEtkDbs().runCheckDbConnectionQuery();
            getLDAPProject().setDBConnectionWatchDogActive(true, LOG_CHANNEL_LDAP);
        } catch (Exception e) {
            Logger.log(LOG_CHANNEL_LDAP, LogType.ERROR, "Check database connection for LDAP project failed while restarting LDAP sync thread: " + e);
        }
        LDAPHelper.getInstance().startLdapSyncThread(getLDAPProject(), getLDAPSession());
    }

    public static void restartCheckPublishingPollingThread() {
        try {
            getMqProject().getEtkDbs().runCheckDbConnectionQuery();
            getMqProject().setDBConnectionWatchDogActive(true, LOG_CHANNEL_DEBUG);
        } catch (Exception e) {
            Logger.log(LOG_CHANNEL_PUBLISHING, LogType.ERROR, "Check database connection for project failed while restarting check publishing polling thread: " + e);
        }
        if (publishingHelper != null) {
            publishingHelper.startPollingThread();
        }
    }

    public static iPartsPlugin getPluginInstanceWithErrorMessage() {
        iPartsPlugin iPartsPlugin = (iPartsPlugin)PluginRegistry.getRegistry().getRegisteredPlugin(iPartsConst.PLUGIN_CLASS_NAME_IPARTS);
        if (iPartsPlugin == null) {
            MessageDialog.showError(TranslationHandler.translate("!!Plug-in \"%1\" konnte nicht gefunden werden!", iPartsConst.PLUGIN_CLASS_NAME_IPARTS));
        }
        return iPartsPlugin;
    }

    public static EtkFunction showMQTestDialog() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                MQDemoForm mqWindow = new MQDemoForm(MQ_CONNECTION_FACTORY_JNDI);
                mqWindow.show();
            }
        };
    }

    /**
     * Feuert einen {@link iPartsClearAllCachesEvent} in allen Cluster-Knoten, um die übergebenen spezifischen Caches zu löschen.
     *
     * @param cacheTypes
     */
    public static void fireClearGlobalCaches(EnumSet<iPartsCacheType> cacheTypes) {
        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsClearAllCachesEvent(cacheTypes));
    }

    public static EtkFunction clearCaches(final boolean clearOnlyRetailRelevantCaches, EnumSet<iPartsCacheType> cacheTypes) {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                String message;
                if (clearOnlyRetailRelevantCaches) {
                    message = "!!Retail-relevante Caches wurden gelöscht.";
                    boolean clearResponseDataCaches = MessageDialog.showYesNo("!!Sollen auch die Caches für die Idents und Ausreißer gelöscht werden?",
                                                                              "!!Retail-relevante Caches löschen") == ModalResult.YES;
                    ClearRetailRelevantCachesEvent.invalidateRetailRelevantCaches(clearResponseDataCaches, clearResponseDataCaches);
                } else {
                    if ((cacheTypes != null) && cacheTypes.isEmpty()) {
                        MessageDialog.show("!!Keine Caches zum Löschen übergeben.", "!!Caches löschen");
                        return;
                    }

                    if (iPartsClearAllCachesEvent.isClearAllCaches(cacheTypes)) {
                        message = "!!Alle Caches wurden gelöscht.";
                    } else {
                        message = TranslationHandler.translate("!!Die folgenden Caches wurden gelöscht:") + "\n- "
                                  + cacheTypes.stream()
                                          .map(iPartsCacheType::name)
                                          .collect(Collectors.joining("\n- "));
                    }
                    fireClearGlobalCaches(cacheTypes);
                }

                // Erreichbarkeit der Favoriten zurücksetzen
                ModuleSearchCache.clearCacheForSession(Session.get());
                JavaViewerMainWindow mainWindow = owner.getConnector().getMainWindow();
                if (mainWindow != null) {
                    mainWindow.getFavoritesManager().markFavoritesReachableInfoAsDirty();
                }

                // Ein evtl. aktives ChangeSet neu laden
                EtkRevisionsHelper revisionsHelper = owner.getRevisionsHelper();
                if (revisionsHelper != null) {
                    AbstractRevisionChangeSet activeChangeset = revisionsHelper.getActiveRevisionChangeSetForEdit();
                    if (activeChangeset instanceof iPartsRevisionChangeSet) {
                        ((iPartsRevisionChangeSet)activeChangeset).loadFromDB();
                    }
                }

                // Auch im Edit geöffnete Module neu laden
                reloadAllModulesInEdit(owner.getConnector());

                MessageDialog.show(message, "!!Caches löschen");
            }
        };
    }

    public static void reloadAllModulesInEdit(AbstractJavaViewerFormIConnector connector) {
        if (iPartsPlugin.isEditPluginActive()) {
            de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin.reloadAllModulesInEdit(connector);
        }
    }

    public static EtkFunction showInterAppComStatus() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                iPartsPlugin iPartsPlugin = getPluginInstanceWithErrorMessage();
                if (iPartsPlugin != null) {
                    iPartsPlugin.interAppComMessageManager.showStatusDialog();
                }
            }
        };
    }

    public static EtkFunction executeSQLPerformanceTests() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                iPartsPlugin iPartsPlugin = getPluginInstanceWithErrorMessage();
                if (iPartsPlugin != null) {
                    EtkProject project = iPartsPlugin.getMqProject();
                    if (project != null) {
                        assertProjectDbIsActive(project, "SQL Performance Tests", LOG_CHANNEL_DEBUG);
                        SQLPerformanceTestForm sqlPerformanceTestForm = new SQLPerformanceTestForm(project);
                        sqlPerformanceTestForm.show();
                    }
                } else {
                    MessageDialog.showError("!!Fehler: EtkProject ist null!", "!!Fehler");
                }
            }
        };
    }

    /**
     * Zeigt den iParts-spezifischen Filterdialog an.
     *
     * @param connector
     * @return {@code true} falls sich die Filtereinstellungen geändert haben
     */
    public static boolean showiPartsFilterDialog(AbstractJavaViewerFormIConnector connector) {
        boolean filterChanged = false;
        iPartsFilterDialog dlg = new iPartsFilterDialog(connector, connector.getActiveForm());
        if (dlg.showModal() == ModalResult.OK) {
            if (dlg.hasChanged()) {
                connector.getProject().fireProjectEvent(new FilterChangedEvent());
                filterChanged = true;
            }
        }

        updateFilterButton(connector, null);
        return filterChanged;
    }

    /**
     * Aktualisiert den Text vom übergebenen Filter-ToolbarButton.
     *
     * @param connector
     * @param button
     */
    public static void updateFilterButton(AbstractJavaViewerFormIConnector connector, GuiToolButton button) {
        if (button == null) {
            button = (GuiToolButton)connector.getToolbarManager().getButton(iPartsToolbarButtonAlias.FILTER_IPARTS.getAlias());
        }
        if (button != null) {
            if (iPartsFilter.get().isFilterActive()) {
                button.setText("!!Aktiv");
                button.setFontStyle(DWFontStyle.BOLD);
                button.setForegroundColor(Colors.clRed.getColor());
            } else {
                button.setText("");
            }
        }
    }

    /**
     * Liefert die Einstellung für die Konvertierung der SA/SAA's bzgl "Z " nach "Z0" zurück
     *
     * @return
     */
    public static boolean isSaaConvert() {
        return getPluginConfig().getConfigValueAsBoolean(CONFIG_DIALOG_CONVERT_SAA);
    }

    /**
     * Liefert die Anzahl Einzel-Code (Terme) in einer Coderegel, ab der eine Code-Vereinfachung gemacht werden soll.
     *
     * @return
     */
    public static int getThresholdForDNFSimplification() {
        return getPluginConfig().getConfigValueAsInteger(CONFIG_LIMIT_FOR_CODE_SIMPLIFICATION);
    }

    /**
     * Liefert die maximale Anzahl Job-Logs, die in den jeweiligen Tabellen angezeigt werden sollen.
     *
     * @return
     */
    public static int getJobsLogLimitPerTable() {
        return getPluginConfig().getConfigValueAsInteger(CONFIG_LIMIT_JOB_LOGS_IN_TABLE);
    }

    /**
     * Liefert das Verzeichnis für die aktiven Log-Files. Sollte in den Admin-Optionen kein Verzeichnis angegeben sein,
     * oder kann der Ordner nicht angelegt werden, wird ein Verzeichnis im Temp Ordner angelegt. Bei den laufenden Jobs,
     * wird nur in diesem temporären Ordner, in ein Unterverzeichnis geschrieben. Im Normalfall, wird hier der Pfad direkt
     * so verwendet wie er im Adminmodus eingestellt ist. Dieser muss separat einstellbar sein, da die Logs von laufenden
     * Jobs oft auf lokalen Verzeichnissen liegen, da es zu teuer wäre dauernd auf Netzwerklaufwerke zuzugreifen.
     *
     * @return
     */
    public static DWFile getRunningJobLogsDir() {
        DWFile runningJobLogsDir = iPartsPlugin.getDirForConfigOption(getPluginConfig(), CONFIG_JOB_LOGS_DIR_RUNNING, LOG_CHANNEL_DEBUG);
        if (runningJobLogsDir == null) {
            runningJobLogsDir = getJobsSubDir(runningJobLogsDir, SUBDIR_RUNNING);
        }
        return runningJobLogsDir;
    }

    public static DWFile getProcessedJobLogsDir() {
        DWFile processedJobLogsDir = iPartsPlugin.getDirForConfigOption(getPluginConfig(), CONFIG_JOB_LOGS_DIR, LOG_CHANNEL_DEBUG);
        return getJobsSubDir(processedJobLogsDir, SUBDIR_PROCESSED);
    }

    public static DWFile getErrorJobsLogsDir() {
        DWFile errorJobLogsDir = iPartsPlugin.getDirForConfigOption(getPluginConfig(), CONFIG_JOB_LOGS_DIR, LOG_CHANNEL_DEBUG);
        return getJobsSubDir(errorJobLogsDir, SUBDIR_ERROR);
    }

    public static DWFile getCancelledJobsDir() {
        DWFile cancelledJobLogsDir = iPartsPlugin.getDirForConfigOption(getPluginConfig(), CONFIG_JOB_LOGS_DIR, LOG_CHANNEL_DEBUG);
        return getJobsSubDir(cancelledJobLogsDir, SUBDIR_CANCELLED);
    }

    private static DWFile getJobsSubDir(DWFile runningJobsDir, String subdirName) {
        if (runningJobsDir == null) {
            runningJobsDir = DWFile.get(System.getProperty("java.io.tmpdir")).getChild(INTERNAL_PLUGIN_NAME).getChild("importLogs");
        }
        if (StrUtils.isValid(subdirName)) {
            DWFile jobSubDir = runningJobsDir.getChild(subdirName);
            if (jobSubDir.exists(1000) || jobSubDir.mkDirsWithRepeat()) {
                return jobSubDir;
            }
        }
        return runningJobsDir;
    }

    /**
     * Fügt das übergebene Baumuster zum Filter für die EDS Konstruktions-Baumuster hinzu.
     *
     * @param modelNumber
     * @return {@code true} falls dieses Baumuster nicht bereits im Filter für die Konstruktions-Baumuster enthalten war
     */
    public static boolean addEDSConstructionModelToFilter(String modelNumber) {
        return SessionKeyHelper.addEDSConstructionModelToFilter(modelNumber);
    }

    /**
     * Fügt das übergebene Baumuster zum Filter für die MBS Konstruktions-Baumuster hinzu.
     *
     * @param modelNumber
     * @return {@code true} falls dieses Baumuster nicht bereits im Filter für die Konstruktions-Baumuster enthalten war
     */
    public static boolean addMBSConstructionModelToFilter(String modelNumber) {
        return SessionKeyHelper.addMBSConstructionModelToFilter(modelNumber);
    }

    /**
     * Fügt das übergebene Baumuster zum Filter für die CTT Konstruktions-Baumuster hinzu.
     *
     * @param modelNumber
     * @return {@code true} falls dieses Baumuster nicht bereits im Filter für die Konstruktions-Baumuster enthalten war
     */
    public static boolean addCTTConstructionModelToFilter(String modelNumber) {
        return SessionKeyHelper.addCTTConstructionModelToFilter(modelNumber);
    }


    /**
     * Fügt eine Admin-Option hinzu, die es erlaubt ein Zeitintervall zu definieren
     *
     * @param configurationPanel
     * @param customOption
     * @param text
     */
    public static void addTimeIntervalControl(UniversalConfigurationPanel configurationPanel, UniversalConfigOption.UniversalConfigCustomOption customOption, String text, String tooltip) {
        iPartsTimeIntervalCustomOptionWithControl customOptionWithControl = new iPartsTimeIntervalCustomOptionWithControl(customOption, text, true);
        configurationPanel.addCustomOption(customOptionWithControl, true, false).setTooltip(tooltip);
    }


    /**
     * Fügt das übergebene Baumuster zum Filter für die Konstruktions-Baumuster hinzu.
     *
     * @param modelNumber
     * @param assemblyId
     * @return {@code true} falls dieses Baumuster nicht bereits im Filter für die Konstruktions-Baumuster enthalten war
     */
    private boolean addConstructionModelToFilter(String modelNumber, AssemblyId assemblyId) {
        List<iPartsVirtualNode> virtualNodes = iPartsVirtualNode.parseVirtualIds(assemblyId);
        if (iPartsVirtualNode.isNodeWithinEdsBcsConstStructure(virtualNodes)) {
            return addEDSConstructionModelToFilter(modelNumber);
        }
        if (iPartsVirtualNode.isNodeWithinMBSConstStructure(virtualNodes)) {
            return addMBSConstructionModelToFilter(modelNumber);
        }
        if (iPartsVirtualNode.isNodeWithinCTTConstStructure(virtualNodes)) {
            return addCTTConstructionModelToFilter(modelNumber);
        }

        return false;
    }


    /**
     * Erzeugt eine Konfigurationsgruppe mit "Aktiv"-Schalter und der Möglichkeit Wochentage und eine Uhrzeit anzugeben
     *
     * @param configurationPanel
     * @param groupName
     * @param configActiveCheckBox
     * @param configDays
     * @param configTime
     */
    public static void createDayOfWeekTimerGroup(UniversalConfigurationPanel configurationPanel, String groupName,
                                                 UniversalConfigOption configActiveCheckBox, UniversalConfigOption configDays,
                                                 UniversalConfigOption configTime, boolean endGroup) {
        configurationPanel.startGroup(groupName);
        configurationPanel.addBooleanOption(configActiveCheckBox, "!!Aktiv", false);
        if (configDays != null) {
            configurationPanel.addStringListOptionMultipleSelection(configDays, "!!Tage", false, DateUtils.DayOfWeek.getDisplayNames(), false);
        }
        configurationPanel.addTimeOption(configTime, "!!Uhrzeit", true, true);
        if (endGroup) {
            configurationPanel.endGroup();
        }
    }


    /**
     * Liefert das Verzeichnis zur übergebenen Adminoption
     *
     * @param dirConfig
     * @param logChannel
     * @return
     */
    public static DWFile getDirForConfigOption(UniversalConfiguration pluginConfig, UniversalConfigOption dirConfig, LogChannels logChannel) {
        if (dirConfig.getType() == UniversalConfigOptionType.FILE) {
            // Check, ob ein valides Verzeichnis angegeben wurde
            String directoryString = pluginConfig.getConfigValueAsRawString(dirConfig, "");
            if (StrUtils.isValid(directoryString)) {
                DWFile directory = pluginConfig.getConfigValueAsDWFile(dirConfig);
                if (!directory.exists(1000) && !directory.mkDirsWithRepeat()) {
                    Logger.log(logChannel, LogType.ERROR, "Could not create directory: "
                                                          + directory.getAbsolutePath());
                    return null;
                }
                return directory;
            } else {
                Logger.log(logChannel, LogType.ERROR, "Directory for configuration key \"" + dirConfig.getKey() + "\" must not be empty or null!");
            }
        }
        return null;
    }

    public static void restartTimerThread(AbstractDayOfWeekHandler timerObject, UniversalConfiguration pluginConfig,
                                          UniversalConfigOption activeConfig, UniversalConfigOption daysOfWeekConfig,
                                          UniversalConfigOption timeOfDayConfig, LogChannels logChannel) {
        if (timerObject == null) {
            return;
        }

        if (pluginConfig.getConfigValueAsBoolean(activeConfig)) {
            String threadName = timerObject.getThreadName();
            if (iPartsPlugin.getMqProject() == null) {
                Logger.log(logChannel, LogType.ERROR, "EtkProject for restarting " + threadName + " thread is null");
                return;
            }

            try {
                iPartsPlugin.getMqProject().getEtkDbs().runCheckDbConnectionQuery();
            } catch (Exception e) {
                Logger.log(logChannel, LogType.ERROR, "Check database connection for project failed while restarting scheduled " + threadName + " thread: " + e);
            }

            EnumSet<DateUtils.DayOfWeek> daysOfWeek;
            if (daysOfWeekConfig != null) {
                String[] daysOfWeekStrings = pluginConfig.getConfigValueAsStringArray(daysOfWeekConfig);
                if (daysOfWeekStrings.length == 0) {
                    Logger.log(logChannel, LogType.INFO, "Could not start scheduled " + threadName + " timer. Days of week selection must not be empty");
                    return;
                }
                daysOfWeek = DateUtils.DayOfWeek.getDaysOfWeek(daysOfWeekStrings);
            } else {
                daysOfWeek = EnumSet.allOf(DateUtils.DayOfWeek.class); // Alle Tage
            }

            Date timeOfDay = pluginConfig.getConfigValueAsTime(timeOfDayConfig);
            timerObject.startThread(daysOfWeek, timeOfDay);
        } else {
            timerObject.stopThread();
        }
    }

    /**
     * Korrigiert den übergebenen {@link NavigationPath} bzgl. der Produkt-Strukturknoten (für Fahrzeug-Produkte mit bzw.
     * ohne dazugemischte Aggregate-Produkte)
     *
     * @param navigationPath
     * @return
     */
    public static NavigationPath correctNavigationPathForProductStructures(NavigationPath navigationPath) {
        // Favoriten-Pfad korrigieren bzgl. dem Flag für Fahrzeug-Produkte mit Aggregaten
        NavigationPath correctedNavigationPath = new NavigationPath();
        boolean productStructureWithAggregatesForSession = iPartsProduct.isProductStructureWithAggregatesForSession();
        for (PartListEntryId partListEntryId : navigationPath) {
            if (iPartsVirtualNode.isVirtualId(partListEntryId.getOwnerAssemblyId())) {
                List<iPartsVirtualNode> virtualNodes = iPartsVirtualNode.parseVirtualIds(partListEntryId.getOwnerAssemblyId());
                if (!virtualNodes.isEmpty()) {
                    boolean pathEntryCorrected = false;
                    iPartsVirtualNode[] correctedVirtualNodes = new iPartsVirtualNode[virtualNodes.size()];
                    int virtualNodeIndex = 0;
                    for (iPartsVirtualNode virtualNode : virtualNodes) {
                        iPartsNodeType nodeType = virtualNode.getType();
                        if (nodeType.isProductType() && (nodeType != iPartsNodeType.PRODUCT_KGTU_COMMON) && (nodeType != iPartsNodeType.PRODUCT_EINPAS_COMMON)) {
                            if (productStructureWithAggregatesForSession != nodeType.isProductStructureWithAggregates()) {
                                pathEntryCorrected = true;
                                if (nodeType.isProductKgTuType()) {
                                    nodeType = iPartsNodeType.getProductKgTuType(false, productStructureWithAggregatesForSession);
                                } else {
                                    nodeType = iPartsNodeType.getProductEinPASType(false, productStructureWithAggregatesForSession);
                                }
                            }
                        }
                        virtualNode = new iPartsVirtualNode(nodeType, virtualNode.getId());
                        correctedVirtualNodes[virtualNodeIndex] = virtualNode;
                        virtualNodeIndex++;
                    }
                    if (pathEntryCorrected) {
                        partListEntryId = new PartListEntryId(iPartsVirtualNode.getVirtualIdString(correctedVirtualNodes),
                                                              partListEntryId.getKVer(), partListEntryId.getKLfdnr());
                    }
                }
            }
            correctedNavigationPath.add(partListEntryId);
        }

        return correctedNavigationPath;
    }


    @Override
    public UserAdminTreeCreator modifyUserAdminTreeCreator(UserAdminTreeCreator userAdminTreeCreator) {
        return new UserAdminTreeCreator(iPartsUserAdminDb.APP_ID, true, false,
                                        Arrays.asList(PropertyType.values()), // Alle Eigenschaften-Typen erlauben
                                        EnumSet.of(UserAdminSettingsPanelOptions.SHOW_ADMIN_OPTIONS));
    }


    @Override
    public UserAdminDbActionsBuilder getUserAdminDbBuilder() {
        return new iPartsUserAdminDbBuilder();
    }

    public static void initConfigurationSettingsVariables() {
        isEditPluginActive = PluginRegistry.isPluginRegisteredAndActive(PLUGIN_CLASS_NAME_IPARTS_EDIT);
        isImportPluginActive = PluginRegistry.isPluginRegisteredAndActive(PLUGIN_CLASS_NAME_IPARTS_IMPORT);
        isExportPluginActive = PluginRegistry.isPluginRegisteredAndActive(PLUGIN_CLASS_NAME_IPARTS_EXPORT);
        isWebservicePluginActive = PluginRegistry.isPluginRegisteredAndActive(PLUGIN_CLASS_NAME_IPARTS_WEBSERVICE);
        isWebservicePluginPresent = isWebservicePluginActive || (PluginRegistry.getRegistry().getRegisteredPlugin(PLUGIN_CLASS_NAME_IPARTS_WEBSERVICE) != null);
        if (pluginConfig != null) {
            isCachesProviderActive = pluginConfig.getConfigValueAsBoolean(CONFIG_CACHES_PROVIDER_ACTIVE);
            cachesProviderIacId = pluginConfig.getConfigValueAsString(CONFIG_CACHES_PROVIDER_IAC_ID);
            isWebservicesXMLExportSeparateES12Keys = pluginConfig.getConfigValueAsBoolean(CONFIG_WEBSERVICES_XMLEXPORT_SEPARATE_ES12KEYS);
            iPartsEqualPartsHelper.SHOW_EQUAL_PARTS = pluginConfig.getConfigValueAsBoolean(CONFIG_SHOW_EQUAL_PARTS);
            isShowAlternativePartsForPRIMUS = pluginConfig.getConfigValueAsBoolean(CONFIG_SHOW_ALTERNATIVE_PARTS_FOR_PRIMUS);
            isCheckModelVisibility = pluginConfig.getConfigValueAsBoolean(CONFIG_CHECK_MODEL_VISIBILITY);
            isUseSPKMapping = pluginConfig.getConfigValueAsBoolean(CONFIG_USE_SPK_MAPPING);
            isPrimusHintHandling = pluginConfig.getConfigValueAsBoolean(iPartsPlugin.CONFIG_PRIMUS_HINT_HANDLING);
            isFilterWireHarnessParts = pluginConfig.getConfigValueAsBoolean(iPartsPlugin.CONFIG_FILTER_WIRE_HARNESS_PARTS);
            wireHarnessDummyPartNo = pluginConfig.getConfigValueAsString(iPartsPlugin.CONFIG_WIRE_HARNESS_DUMMY_PART_NO);
            isWebservicesSinglePicViewActive = pluginConfig.getConfigValueAsBoolean(iPartsPlugin.CONFIG_WEBSERVICES_SINGLE_PIC_VIEW_ACTIVE);
            webservicesSinglePicPartsBaseURI = pluginConfig.getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_SINGLE_PIC_PARTS_BASE_URI);

            // Redis-Cache
            redisCacheLifeTime = null; // Wird lazy neu ermittelt
            JedisPooled oldRedisPool = getRedisPool();
            String redisURL = pluginConfig.getConfigValueAsString(CONFIG_CACHES_REDIS_URL);
            if (StrUtils.isValid(redisURL) && PluginRegistry.isPluginRegisteredAndActive(PLUGIN_CLASS_NAME_IPARTS)) {
                String redisUserName = pluginConfig.getConfigValueAsString(CONFIG_CACHES_REDIS_USER_NAME);
                if (redisUserName.isEmpty()) {
                    redisPool = new JedisPooled(redisURL, pluginConfig.getConfigValueAsInteger(CONFIG_CACHES_REDIS_PORT));
                } else {
                    redisPool = new JedisPooled(redisURL, pluginConfig.getConfigValueAsInteger(CONFIG_CACHES_REDIS_PORT),
                                                redisUserName,
                                                pluginConfig.getConfigValueAsPassword(CONFIG_CACHES_REDIS_PASSWORD).decrypt());
                }
            } else {
                redisPool = null;
            }
            if (oldRedisPool != null) {
                oldRedisPool.close();
            }
        }
    }

    public iPartsPlugin() {
    }

    /**
     * Konstruktor für die Unit-Tests.
     *
     * @param host
     * @param port
     */
    public iPartsPlugin(String host, int port) {
        this.host = host;
        this.port = port;
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
    public void initPlugin(ConfigBase config) {
        initPluginBase(config, EtkPluginConstants.XML_CONFIG_PATH_BASE + '/' + INTERNAL_PLUGIN_NAME);
        pluginConfig = new UniversalConfiguration(config, getConfigPath());
        initConfigurationSettingsVariables();
        wsDWKUpdateGUI = new WSDWKUpateConfigGUI(pluginConfig);
        XML_SCHEMA_PATH = JavaViewerPathsManager.getWorkingPath().getAbsolutePath();

        if (isActive()) {
            EditControlFactory.USE_NEW_COMBO_BOX_OVERRIDE_FROM_PLUGIN = true;
            interAppComHelper = new EtkInterAppComHelper(pluginConfig.getConfig(), pluginConfig.getPath() + CONFIG_INTER_APP_COM_PING_TIME.getKey(),
                                                         pluginConfig.getPath() + CONFIG_INTER_APP_COM_RECONNECT_ATTEMPTS.getKey(),
                                                         pluginConfig.getPath() + CONFIG_INTER_APP_COM_RECONNECT_TIME.getKey(),
                                                         LOG_CHANNEL_INTER_APP_COM_REGISTRY, LOG_CHANNEL_INTER_APP_COM_CLIENT,
                                                         LOG_CHANNEL_INTER_APP_COM_PING, LOG_CHANNEL_INTER_APP_COM_WEBSERVICE);
            interAppComMessageManager.setInterAppComHelper(interAppComHelper);
        }
    }

    @Override
    public void applicationStarted(boolean firstInit) {
        super.applicationStarted(firstInit);
        initConfigurationSettingsVariables();

        if (isActive()) {
            // ENDPOINT-Session für AS-PLM MQ Receiver in einem neuen Thread erzeugen und registrieren (weil sich die Session
            // im Thread registriert)
            // Die Namen mqSession und mqProject sind nicht mehr aktuell, da beide mittlerweile für allgemeine Zwecke verwendet werden,
            // z.B. f.d. Benutzerverwaltung
            FrameworkThread mqThread = new FrameworkThread("iParts background session thread", Thread.NORM_PRIORITY, new Runnable() {
                @Override
                public void run() {
                    mqSession = FrameworkEndpointHelper.createSession(SessionType.ENDPOINT);
                    if (mqSession != null) {
                        JavaViewerApplication.getInstance().startSession(mqSession, new CommandLine(new String[0]));
                        if (mqSession != null) { // Wenn bei der Initialisierung der Session ein Fehler auftritt, wird mqSession wieder null

                            // Das Design muss explizit gesetzt werden, damit z.B. Importe von SVGs die korrekten Design-Einstellungen verwenden
                            DesignSettings.activateSelectedThemeOrDesign(getConfig(), CustomizingSettings.XML_CONFIG_PATH_BASE, true);

                            mqProject = (EtkProject)mqSession.getAttribute(JavaViewerApplication.SESSION_KEY_PROJECT);
                            if (mqProject != null) {
                                mqProject.getDB().setLogNameForSetActiveStacktrace("iParts background project");
                                mqProject.setDBConnectionWatchDogActive(true, LOG_CHANNEL_DEBUG);
                            }
                        }
                    }
                }
            });
            mqThread.__internal_start();
            mqThread.waitFinished();

            // Benutzerverwaltung hier explizit initialisieren, weil diese z.B. vom LDAP-Thread benötigt wird
            if (pluginConfig.getConfigValueAsBoolean(CONFIG_USER_ADMIN_ENABLED)) {
                // Bei H2 ist zu diesem Zeitpunkt die Haupt-DB durch das mqProject noch in Verwendung und würde daher readOnly
                // initialisiert werden -> als Workaround für die Sourcecodehinterlegung deswegen hier etwas warten
                if ((mqProject != null) && (mqProject.getEtkDbs().getDatabaseType(DBDatabaseDomain.MAIN) == DatabaseType.H2)) {
                    Java1_1_Utils.sleep(12000); // nach 10 s wird die DB vom mqProject getrennt -> noch etwas länger warten
                }

                UserAdminDbActions.createInstance(getConfig());
            }

            if (AbstractApplication.isOnline() && (isEditPluginActive || isImportPluginActive)) {
                if (mqProject != null) {
                    MQPicScheduler.getInstance().registerMQListener(mqProject, mqSession);
                }

                // MQ initialisieren
                if (firstInit) {
                    // Verzögerter Start der Initialisierung der MQ Channels. Die Receiver Zuweisungen der
                    // eigentlichen ChannelTypes werden in den jeweiligen Plugins in initPlugin() erzeugt.
                    int mqInitDelay = getPluginConfig().getConfigValueAsInteger(CONFIG_MQ_INIT_DELAY);
                    if (mqInitDelay > 0) {
                        Logger.log(LOG_CHANNEL_MQ, LogType.INFO, "Waiting " + mqInitDelay + " s before MQ initialization...");
                    }
                    if ((mqInitDelay <= 0) || !Java1_1_Utils.sleep(mqInitDelay * 1000)) {
                        MQHelper.getInstance().initConnectionAndActors(MQ_CONNECTION_FACTORY_JNDI);
                    }
                } else {
                    MQHelper.getInstance().reinitAllChannels(true);
                }
                mqInitialized = true;
            } else {
                mqInitialized = false;
            }

            // ENDPOINT-Session und EtkProject für LDAP in einem neuen Thread erzeugen (weil sich die Session im Thread registriert)
            FrameworkThread ldapThread = new FrameworkThread("LDAP session thread", Thread.NORM_PRIORITY, new Runnable() {
                @Override
                public void run() {
                    ldapSession = FrameworkEndpointHelper.createSession(SessionType.ENDPOINT);
                    if (ldapSession != null) {
                        JavaViewerApplication.getInstance().startSession(ldapSession, new CommandLine(new String[0]));
                        if (ldapSession != null) { // Wenn bei der Initialisierung der Session ein Fehler auftritt, wird ldapSession wieder null
                            ldapProject = (EtkProject)ldapSession.getAttribute(JavaViewerApplication.SESSION_KEY_PROJECT);
                            if (ldapProject != null) {
                                ldapProject.getDB().setLogNameForSetActiveStacktrace("LDAP project");
                                ldapProject.setDBConnectionWatchDogActive(true, LOG_CHANNEL_LDAP);
                            }
                            LDAPHelper.getInstance().startLdapSyncThread(ldapProject, ldapSession);
                        }
                    }
                }
            });
            ldapThread.__internal_start();
            ldapThread.waitFinished();

            if ((mqProject != null) && (mqSession != null)) { // Publizieren ohne gültiges mqProject oder mqSession funktioniert nicht
                publishingHelper = new iPartsPublishingHelper(mqProject, mqSession);
                restartCheckPublishingPollingThread();
            }

            preventTransmissionToASPLMEnabledChangeEvent = new ObserverCallback(callbackBinder, PreventTransmissionToASPLMEnabledChangeEvent.class) {
                @Override
                public void callback(ObserverCall call) {
                    boolean enabled = ((PreventTransmissionToASPLMEnabledChangeEvent)call).isEnabled();
                    writeBooleanPluginConfig(getPluginConfig(), CONFIG_PREVENT_TRANSMISSION_TO_ASPLM, enabled);
                }
            };
            ApplicationEvents.addEventListener(preventTransmissionToASPLMEnabledChangeEvent);
            warmUpCaches(false);
        }
    }

    /**
     * Schreibt für die übergebene {@link UniversalConfigOption} den übergebenen <code>enabled</code> Wert in die
     * übergebene {@link UniversalConfiguration}.
     *
     * @param pluginConfig
     * @param configOption
     * @param enabled
     */
    public static void writeBooleanPluginConfig(UniversalConfiguration pluginConfig, UniversalConfigOption configOption,
                                                boolean enabled) {
        // Den Zustand nur in der Standard-Umgebungsvariante setzen
        synchronized (pluginConfig.getConfig()) {
            String oldMultiConfigNameWrite = pluginConfig.getConfig().getMultiConfigNameWrite();
            try {
                pluginConfig.getConfig().startWriting();
                pluginConfig.getConfig().setMultiConfigNameWrite("");
                pluginConfig.getConfig().setBoolean(pluginConfig.getPath() + configOption.getKey(), enabled);
            } finally {
                pluginConfig.getConfig().setMultiConfigNameWrite(oldMultiConfigNameWrite);
                pluginConfig.getConfig().commitWriting();
            }
        }
    }

    @Override
    public UniversalConfigurationPanel getConfigurationPanel(ConfigurationWindow host) {
        // VarParam notwendig, um den später erst definierten EventListener in configToScreen() aufzurufen
        final VarParam<EventListener> interAppComEventListenerVarParam = new VarParam<EventListener>(null);
        final UniversalConfigurationPanel configurationPanel = new UniversalConfigurationPanel(host, pluginConfig, OFFICIAL_PLUGIN_NAME, true) {
            @Override
            protected void configToScreen() {
                super.configToScreen();
                if (interAppComEventListenerVarParam.getValue() != null) {
                    interAppComEventListenerVarParam.getValue().fire(null);
                }
            }
        };

        // Performance
        configurationPanel.addLabel("!!Performance:", null).setFontStyle(DWFontStyle.BOLD);
        configurationPanel.addBooleanOption(CONFIG_SHOW_ANIMATIONS, "!!Animationen beim Öffnen von Dialogen anzeigen", false);
        configurationPanel.addBooleanOption(CONFIG_CACHES_WARM_UP, "!!Caches aufwärmen", false);
        configurationPanel.addBooleanOption(CONFIG_CACHES_PROVIDER_ACTIVE, "!!Provider für große Caches", false);
        configurationPanel.addStringOption(CONFIG_CACHES_PROVIDER_IAC_ID, "!!Registrierungs-ID vom Provider für große Caches", false);
        configurationPanel.addIntegerSpinnerOption(CONFIG_CACHES_LIFE_TIME, "!!Lebensdauer für die meisten Caches (in Minuten)", false, -1, 10000, 1).
                setTooltip("!!0 oder -1 für \"unendlich\"");
        configurationPanel.addLabel("!!Die Änderungen erfordern einen Neustart der Anwendung.", null).setFontStyle(DWFontStyle.BOLD);
        configurationPanel.startGroup("!!Optionaler Redis-Cache");
        configurationPanel.addStringOption(CONFIG_CACHES_REDIS_URL, "!!URL", false);
        configurationPanel.addIntegerSpinnerOption(CONFIG_CACHES_REDIS_PORT, "!!Port", false, 1, 10000, 1);
        configurationPanel.addStringOption(CONFIG_CACHES_REDIS_USER_NAME, "!!Benutzername", false);
        configurationPanel.addPasswordOption(CONFIG_CACHES_REDIS_PASSWORD, "!!Passwort", false);
        configurationPanel.addIntegerSpinnerOption(CONFIG_CACHES_REDIS_LIFE_TIME, "!!Maximale Lebensdauer für den Redis-Cache (in Minuten)", false, -1, 10000, 1);
        configurationPanel.endGroup();

        // Filter
        configurationPanel.addSeparator();
        configurationPanel.startGroup("!!Filter");
        configurationPanel.addBooleanOption(CONFIG_SHOW_SAS_ONLY_FOR_FIN, "!!Freie SAs nur bei FIN/Aggregateident-Einstieg ausgeben", false);
        // Temporärer Switch zum Umschalten der Logik im Baumuster- und im Endnummernfilter
        configurationPanel.addBooleanOption(CONFIG_IGNORE_INVALID_FACTORIES_IN_FILTER, "!!Ungültige Werke in der Filterung ignorieren", false);
        configurationPanel.addBooleanOption(CONFIG_FILTER_WIRE_HARNESS_PARTS, "!!Leitungssatz-Baukasten Teilepositionen filtern", false);
        configurationPanel.addStringOption(CONFIG_WIRE_HARNESS_DUMMY_PART_NO, "!!Dummy-Sachnummer für Leitungssatz-Baukasten Filterung", true);

        // mind. Anzahl Code zum Vereinfachen der DNF
        configurationPanel.addIntegerSpinnerOption(CONFIG_LIMIT_FOR_CODE_SIMPLIFICATION, "!!Mindestanzahl Code für DNF-Vereinfachung",
                                                   false, -1, 10000, 1).setTooltip("!!Keine Vereinfachung bei -1. Bei 0 wird immer vereinfacht.");
        configurationPanel.addLabel("!!Änderungen erfordern einen Neustart der Anwendung", null).setFontStyle(DWFontStyle.BOLD);
        configurationPanel.endGroup();

        configurationPanel.addSeparator();
        configurationPanel.addBooleanOption(CONFIG_DISPLAY_PROVAL_CODE_DESC, "!!Anzeige von PROVAL-Codebenennungen", false);
        // DAIMLER-10211, Verarbeitung von PRIMUS-Hinweisen bei Truck
        configurationPanel.addBooleanOption(CONFIG_PRIMUS_HINT_HANDLING, "!!Verarbeitung von PRIMUS-Hinweisen bei Truck", false);

        // DAIMLER-14566: Truck: Zuordnung Produkt zu Supplier - Berechnung Arbeitsvorrat anpassen
        configurationPanel.addBooleanOption(CONFIG_USE_PRODUCT_SUPPLIER, "!!Bestimmung Lieferantenzuordnung direkt aus Produkt bei Truck", false);

        configurationPanel.addBooleanOption(CONFIG_SHOW_ALTERNATIVE_PARTS_FOR_PRIMUS, "!!Alternativteile aus PRIMUS bei PRIMUS-Ersetzungen ausgeben", false);

        configurationPanel.addBooleanOption(CONFIG_SHOW_EQUAL_PARTS, "!!Gemappte Teilenummern von Gleichteilen anzeigen", false);

        configurationPanel.addBooleanOption(CONFIG_USE_SPK_MAPPING, "!!SPK-Mapping-Texte für Leitungssatzbaukästen verwenden", false);

        configurationPanel.addSeparator();
        configurationPanel.addBooleanOption(CONFIG_CHECK_MODEL_VISIBILITY, "!!Baumuster-Sichtbarkeit berücksichtigen", false).
                setTooltip("!!Falls deaktiviert: Es werden ALLE Baumuster berücksichtigt unabhängig vom Flag \"Baumuster anzeigen\"");

        configurationPanel.addSeparator();
        configurationPanel.addBooleanOption(CONFIG_USE_CONFIG_DISPLAY_FIELD_WIDTH_AS_MAX, "!!Konfigurierte Spaltenbreite als Maximalbreite verwenden", false);
        configurationPanel.addSeparator();

        configurationPanel.startGroup("!!SA/SAA's Konvertierung");
        GuiLabel label = configurationPanel.addLabel("!!ACHTUNG: Nur Setzen, wenn die Datenbank vorher konvertiert wurde!", null /*DesignImage.boolTrue.getImage()*/);
        label.setFontStyle(DWFontStyle.BOLD);
        label.setForegroundColor(Color.RED);
        configurationPanel.addBooleanOption(CONFIG_DIALOG_CONVERT_SAA, "!!\"Z \" durch \"Z0\" ersetzen", false);
        configurationPanel.endGroup();

        configurationPanel.startGroup("SVG");
        configurationPanel.addStringOption(CONFIG_SVG_FONT_NAME, "!!Schriftart für importierte SVGs", false).
                setTooltip("!!Falls leer, wird die im Original-SVG vorhandene Schriftart beibehalten");
        configurationPanel.addBooleanOption(CONFIG_SVG_OUTLINE_VERSION_2, "!!Neues Highlighting für SVGs inkl. Pfade", false).
                setTooltip("!!Soll beim Import der SVGs das neue Highlighting inkl. Pfade verwendet werden?");
        configurationPanel.endGroup();

        // Datenkarten-Webservices
        configurationPanel.addSeparator();
        configurationPanel.startGroup("!!Datenkarten-Webservices");
        // Basis-URI für die Datenkarten-Webservices
        configurationPanel.addStringOption(CONFIG_WEBSERVICES_DATACARDS_BASE_URI, "!!Basis-URI für die Datenkarten-Webservices", true).
                setTooltip("!!Für Fahrzeug-Datenkarten wird automatisch \"/vehicledatacards\" an die Basis-URI angehängt und für Aggregate-Datenkarten \"/aggregatedatacards\"");
        final GuiCheckbox visV3Checkbox = configurationPanel.addBooleanOption(CONFIG_WEBSERVICES_DATACARDS_USE_V3, "!!VIS v3 verwenden", false);
        visV3Checkbox.setTooltip(TranslationHandler.translate("!!Falls aktiv, wird die Basis-URI um '/v3' erweitert, falls nicht um '/v2'"));
        configurationPanel.addBooleanOption(CONFIG_WEBSERVICES_DATACARDS_USE_RETAIL_NAME, "!!After-Sales-Benennungen verwenden", false).
                setTooltip(TranslationHandler.translate("!!Falls nicht aktiv, wird kein Paramter hinzugefügt. Falls aktiv, wird der Paramter \"%1\" hinzugefügt.",
                                                        iPartsDataCardRetrievalHelper.VIS_USE_RETAIL_NAME));
        configurationPanel.addIntegerSpinnerOption(CONFIG_WEBSERVICES_DATACARDS_CACHE_LIFE_TIME, "!!Cache-Lebensdauer für die Datenkarten-Webservices in Sekunden",
                                                   false, -1, 999999, 1).setTooltip("!!-1 für \"unendlich\"");
        configurationPanel.addIntegerSpinnerOption(CONFIG_WEBSERVICES_DATACARDS_TIMEOUT, "!!Timeout für die Datenkarten-Webservices in Sekunden",
                                                   false, 1, 999999, 1);

        configurationPanel.startGroup("!!Zugangsdaten zur Erzeugung des Access-Tokens");
        configurationPanel.addStringOption(CONFIG_WEBSERVICES_DATACARDS_TOKEN_URL, "!!Token-URL", false);
        configurationPanel.addStringOption(CONFIG_WEBSERVICES_DATACARDS_TOKEN_CLIENT_ID, "!!Client ID", false);
        configurationPanel.addStringOption(CONFIG_WEBSERVICES_DATACARDS_TOKEN_CLIENT_SECRET, "!!Client Secret", false);
        configurationPanel.addStringOption(CONFIG_WEBSERVICES_DATACARDS_TOKEN_NAME, "!!Name", true).setMaximumWidth(500);
        configurationPanel.addStringOption(CONFIG_WEBSERVICES_DATACARDS_TOKEN_TYPE, "!!Typ", true).setMaximumWidth(500);
        configurationPanel.addSeparator();
        GuiTextField datacardTokenTextfield = configurationPanel.addStringOption(CONFIG_WEBSERVICES_DATACARDS_TOKEN, "!!Token für VIS v2", false);
        datacardTokenTextfield.setMaximumWidth(500);
        datacardTokenTextfield.setTooltip("!!Falls leer, wird kein Token beim Aufruf der Datenkarten-Webservices verwendet");
        configurationPanel.endGroup();
        configurationPanel.endGroup();

        configurationPanel.startGroup("!!Einzelteilbilder-Webservice");
        configurationPanel.addStringOption(CONFIG_WEBSERVICES_SINGLE_PIC_PARTS_BASE_URI, "!!Webservice Basis-URI", false);
        configurationPanel.addBooleanOption(CONFIG_WEBSERVICES_SINGLE_PIC_VIEW_ACTIVE, "!!Anzeige von Einzelteilbildern aktiv", false);
        configurationPanel.startGroup("!!Zugangsdaten zur Erzeugung des Access-Tokens");
        configurationPanel.addStringOption(CONFIG_WEBSERVICES_SINGLE_PIC_PARTS_TOKEN_URL, "!!Token-URL", false);
        configurationPanel.addStringOption(CONFIG_WEBSERVICES_SINGLE_PIC_PARTS_TOKEN_CLIENT_ID, "!!Client ID", false);
        configurationPanel.addStringOption(CONFIG_WEBSERVICES_SINGLE_PIC_PARTS_TOKEN_CLIENT_SECRET, "!!Client Secret", false);
        configurationPanel.addStringOption(CONFIG_WEBSERVICES_SINGLE_PIC_PARTS_TOKEN_NAME, "!!Name", true).setMaximumWidth(500);
        configurationPanel.addStringOption(CONFIG_WEBSERVICES_SINGLE_PIC_PARTS_TOKEN_TYPE, "!!Typ", true).setMaximumWidth(500);
        configurationPanel.endGroup();
        configurationPanel.endGroup();

        // TruckBOM.foundation Webservices
        configurationPanel.startGroup("!!TruckBOM.foundation Webservices");
        configurationPanel.addStringOption(CONFIG_WEBSERVICES_TRUCK_BOM_FOUNDATION_BASE_URI, "!!Webservice Basis-URI", false);
        configurationPanel.addBooleanOption(CONFIG_WEBSERVICES_TRUCK_BOM_FOUNDATION_USE_AS_CODE_SOURCE, "!!EDS/BCS Codebenennungen über TB.f beziehen", false);
        configurationPanel.startGroup("!!Zugangsdaten zur Erzeugung des Access-Tokens");
        configurationPanel.addStringOption(CONFIG_WEBSERVICES_TRUCK_BOM_FOUNDATION_TOKEN_URL, "!!Token-URL", false);
        configurationPanel.addStringOption(CONFIG_WEBSERVICES_TRUCK_BOM_FOUNDATION_TOKEN_CLIENT_ID, "!!Client ID", false);
        configurationPanel.addStringOption(CONFIG_WEBSERVICES_TRUCK_BOM_FOUNDATION_TOKEN_CLIENT_SECRET, "!!Client Secret", false);
        configurationPanel.addStringOption(CONFIG_WEBSERVICES_TRUCK_BOM_FOUNDATION_TOKEN_NAME, "!!Name", true).setMaximumWidth(500);
        configurationPanel.addStringOption(CONFIG_WEBSERVICES_TRUCK_BOM_FOUNDATION_TOKEN_TYPE, "!!Typ", true).setMaximumWidth(500);
        configurationPanel.endGroup();
        configurationPanel.endGroup();

        // DIA4U Webservices
        configurationPanel.startGroup("!!DIA4U Webservices");
        configurationPanel.addStringOption(CONFIG_WEBSERVICES_DIA_4_U_BASE_URI, "!!Webservice Basis-URI", false);
        configurationPanel.startGroup("!!Zugangsdaten zur Erzeugung des Access-Tokens");
        configurationPanel.addStringOption(CONFIG_WEBSERVICES_DIA_4_U_TOKEN_URL, "!!Token-URL", false);
        configurationPanel.addStringOption(CONFIG_WEBSERVICES_DIA_4_U_TOKEN_CLIENT_ID, "!!Client ID", false);
        configurationPanel.addStringOption(CONFIG_WEBSERVICES_DIA_4_U_TOKEN_CLIENT_SECRET, "!!Client Secret", false);
        configurationPanel.addStringOption(CONFIG_WEBSERVICES_DIA_4_U_TOKEN_NAME, "!!Name", true).setMaximumWidth(500);
        configurationPanel.addStringOption(CONFIG_WEBSERVICES_DIA_4_U_TOKEN_TYPE, "!!Typ", true).setMaximumWidth(500);
        configurationPanel.endGroup();
        configurationPanel.endGroup();

        // ProVal Webservices
        configurationPanel.startGroup("!!ProVal Webservices");
        configurationPanel.addStringOption(CONFIG_WEBSERVICES_PROVAL_BASE_URI, "!!Webservice Basis-URI", false);
        configurationPanel.startGroup("!!Zugangsdaten zur Erzeugung des Access-Tokens");
        configurationPanel.addStringOption(CONFIG_WEBSERVICES_PROVAL_TOKEN_URL, "!!Token-URL", false);
        configurationPanel.addStringOption(CONFIG_WEBSERVICES_PROVAL_TOKEN_CLIENT_ID, "!!Client ID", false);
        configurationPanel.addStringOption(CONFIG_WEBSERVICES_PROVAL_TOKEN_CLIENT_SECRET, "!!Client Secret", false);
        configurationPanel.addStringOption(CONFIG_WEBSERVICES_PROVAL_TOKEN_NAME, "!!Name", true).setMaximumWidth(500);
        configurationPanel.addStringOption(CONFIG_WEBSERVICES_PROVAL_TOKEN_TYPE, "!!Typ", true).setMaximumWidth(500);
        configurationPanel.endGroup();
        configurationPanel.endGroup();

        // Separate ES1 & ES2 Schlüssel Ausgabe in Webservices und XML Export verwenden
        configurationPanel.startGroup("!!ES1 und ES2 Schlüssel in Webservices und XML Export");
        final GuiCheckbox separateES12Keys = configurationPanel.addBooleanOption(CONFIG_WEBSERVICES_XMLEXPORT_SEPARATE_ES12KEYS, "!!Separate ES1 und ES2 Schlüssel ausgeben", false);
        separateES12Keys.setTooltip("!!Falls aktiv, werden ES1 und ES2 Schlüssel in separaten Attributen, getrennt von der Grundsachnummer ausgegeben.");
        configurationPanel.endGroup();

        // MQ Einstellungen
        configurationPanel.addSeparator();
        configurationPanel.addLabel("!!MQ und RFTS/x Einstellungen", null).setFontStyle(DWFontStyle.BOLD);
        AbstractGuiControl guiControl = configurationPanel.addBooleanOption(CONFIG_AUTO_IMPORTS_ENABLED, "!!Automatische Importe über MQ und RFTS/x sowie Empfang von MQ Media Nachrichten", false);
        guiControl.setTooltip("!!In einer Cluster-Umgebung sollte nur bei einem Cluster-Knoten diese Option aktiviert sein");
        guiControl.setName("AutoImportMQAndRFTSx");

        configurationPanel.addIntegerSpinnerOption(CONFIG_MQ_INIT_DELAY, "!!Verzögerung der MQ Initialisierung (in Sekunden):", false, 0, 60, 1).
                setTooltip("!!Änderungen werden erst nach Neustart wirksam!");

        // Kommunikation: Media-Dateien über MQ
        configurationPanel.addSeparator();
        configurationPanel.addLabel("!!Kommunikation über MQ für Media-Dateien", null).setFontStyle(DWFontStyle.BOLD);
        configurationPanel.startGroup("!!Bildauftrag");
        configurationPanel.addBooleanOption(CONFIG_PREVENT_TRANSMISSION_TO_ASPLM, "!!Bild-/Änderungsaufträge an ASPLM verhindern", false);
        configurationPanel.addBooleanOption(CONFIG_SAVE_XML_FILES, "!!MQ Media XML-Dateien speichern", false);
        configurationPanel.addBooleanOption(CONFIG_SAVE_XML_BINARY_CONTENT_FILES, "!!Binärinhalte aus MQ-XML Dateien speichern", false);
        configurationPanel.addBooleanOption(CONFIG_USE_RELEASED_STATE_AS_SUPPLIED_STATE, "!!Status \"Released\" als Status \"Supplied\" verarbeiten", false);
        configurationPanel.addBooleanOption(CONFIG_PIC_ORDER_REQUEST_DERIVED_PICS, "!!Reduzierte SVGs anfordern", false);
        configurationPanel.addBooleanOption(CONFIG_PIC_ORDER_ALLOW_PICORDER_COPIES, "!!Kopien von Bild-/Änderungsaufträgen erlauben", false);
        configurationPanel.addFileOption(CONFIG_XML_FILES_DIR, "!!Speicherort für MQ-XML Dateien", false, GuiFileChooserDialog.FILE_MODE_DIRECTORIES,
                                         FileChooserPurpose.SAVE, null);
        configurationPanel.addIntegerSpinnerOption(CONFIG_MQ_RECONNECT_TIME, "!!Dauer bis zum nächsten Status Check in Sekunden", false, 1, 10000, 1);
        configurationPanel.addIntegerSpinnerOption(CONFIG_MQ_RECONNECT_ATTEMPTS, "!!Anzahl Versuche für Verbindungsaufbau", false, -1, 10000, 1).setTooltip("!!0 für keinen Verbindungsaufbau; -1 für unendlich viele Versuche");
        GuiTextField validPicFilesTextfield = configurationPanel.addStringOption(CONFIG_PIC_REF_VALID_FILES, "!!MQ Bilddateien einschränken auf folgende Dateitypen:", false);
        validPicFilesTextfield.setTooltip("!!Bilddateiendungen ohne Punkt und durch \";\" getrennt; Standardwert: png;sen");
        validPicFilesTextfield.setValidator(new GuiControlAbstractValidator("!!MQ Bilddateien einschränken auf folgende Dateitypen:") {
            @Override
            public String validate(String value) {
                List<String> enteredFileTypesList = StrUtils.toStringList(value, ";", false, true);
                if (!XMLObjectCreationHelper.getInstance().isFileTypesInputValidForGetMediaGontents(enteredFileTypesList)) {
                    String validFileTypes = StrUtils.makeDelimitedString(", ", iPartsTransferConst.MediaFileTypes.getAliasAsStringArray());
                    return TranslationHandler.translate("!!Ungültige Bilddateiendungen!") + "\n"
                           + TranslationHandler.translate("!!Für MQ Bilddateien sind nur folgende Endungen erlaubt: %1", validFileTypes)
                           + "\n\n" + TranslationHandler.translate("!!Aktuelle Eingabe:");
                }
                return null;
            }
        });
        configurationPanel.addStringOption(CONFIG_PIC_ORDER_PROJECT_ASSIGNMENT_PSK_PRODUCT, "!!Projektzuweisung für PSK Produkte (Präfix)", false);
        configurationPanel.addStringOption(CONFIG_PIC_ORDER_PROJECT_ASSIGNMENT_SPECIAL_PRODUCT, "!!Projektzuweisung für Spezialprodukte", false);
        configurationPanel.endGroup();

        // Inter Application Communication
        configurationPanel.addSeparator();
        configurationPanel.startGroup("!!Kommunikation zwischen WebApps");
        configurationPanel.addStringOption(CONFIG_INTER_APP_COM_CHANNEL_NAME, "!!Kanal", true);
        configurationPanel.addStringOption(CONFIG_INTER_APP_COM_CLIENT_REGISTRY_ID, "!!Registrierungs-ID von diesem Client/Server", false).setTooltip("!!Leer für keine Registrierung");

        final GuiCheckbox interAppComIsRegistryServerCheckBox = configurationPanel.addBooleanOption(CONFIG_INTER_APP_COM_IS_REGISTRY_SERVER, "!!Registrierungs-Server", false);
        final GuiTextField interAppComRegistryServerUrlTextField = configurationPanel.addStringOption(CONFIG_INTER_APP_COM_REGISTRY_SERVER_URL, "!!URL vom Registrierungs-Server", false);
        final GuiTextField interAppComClientUrlTextField = configurationPanel.addStringOption(CONFIG_INTER_APP_COM_CLIENT_URL, "!!URL von diesem Client", false);
        final GuiIntSpinner interAppComPingTimeSpinner = configurationPanel.addIntegerSpinnerOption(CONFIG_INTER_APP_COM_PING_TIME, "!!Dauer zwischen Pings in Sekunden", false, 1, 10000, 1);
        final GuiIntSpinner interAppComReconnectTimeSpinner = configurationPanel.addIntegerSpinnerOption(CONFIG_INTER_APP_COM_RECONNECT_TIME, "!!Dauer bis zum nächsten Status Check in Sekunden", false, 1, 10000, 1);
        final GuiIntSpinner interAppComReconnectAttemptsSpinner = configurationPanel.addIntegerSpinnerOption(CONFIG_INTER_APP_COM_RECONNECT_ATTEMPTS, "!!Anzahl Versuche für Verbindungsaufbau", false, -1, 10000, 1);
        interAppComReconnectAttemptsSpinner.setTooltip("!!0 für keinen Verbindungsaufbau; -1 für unendlich viele Versuche");

        EventListener interAppComEventListener = new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                // RegistryServerUrl und ClientUrl automatisch bestimmen
                BrowserInfo browserInfo = BrowserInfo.get();
                if (browserInfo != null) {
                    if (interAppComIsRegistryServerCheckBox.isSelected() && interAppComRegistryServerUrlTextField.getText().isEmpty()) {
                        interAppComRegistryServerUrlTextField.setText(browserInfo.getOfficialUrl());
                    } else if (!interAppComIsRegistryServerCheckBox.isSelected() && interAppComClientUrlTextField.getText().isEmpty()) {
                        interAppComClientUrlTextField.setText(browserInfo.getOfficialUrl());
                    }
                }
                interAppComClientUrlTextField.setEnabled(!interAppComIsRegistryServerCheckBox.isSelected());
                interAppComPingTimeSpinner.setEnabled(!interAppComIsRegistryServerCheckBox.isSelected());
                interAppComReconnectTimeSpinner.setEnabled(!interAppComIsRegistryServerCheckBox.isSelected());
                interAppComReconnectAttemptsSpinner.setEnabled(!interAppComIsRegistryServerCheckBox.isSelected());
            }
        };
        interAppComIsRegistryServerCheckBox.addEventListener(interAppComEventListener);
        interAppComEventListenerVarParam.setValue(interAppComEventListener);
        configurationPanel.endGroup();

        configurationPanel.addSeparator();
        configurationPanel.startGroup("!!LDAP-Anbindung");
        configurationPanel.addBooleanOption(CONFIG_LDAP_AUTH_ACTIVE, "!!Authentifizierung aktivieren", true);
        configurationPanel.addBooleanOption(CONFIG_LDAP_USE_SECURE_CONNECTION, "!!Sichere LDAPS Verbindung nutzen", true);
        configurationPanel.addStringOption(CONFIG_LDAP_HOST, "!!Host", false);
        configurationPanel.addStringOption(CONFIG_LDAP_PORT, "!!Port", false);
        configurationPanel.addStringOption(CONFIG_LDAP_USER, "!!Benutzer", false);
        configurationPanel.addPasswordOption(CONFIG_LDAP_PASSWORD, "!!Passwort", false);
        configurationPanel.addStringListOptionSingleSelection(CONFIG_LDAP_SECURITY, "!!Sicherheitsebene", false,
                                                              LdapSecurityOptions.getDescriptions(), false);
        configurationPanel.addStringOption(CONFIG_LDAP_SUB_SEARCHTREE, "!!Teilbaum in dem gesucht werden soll", false);
        configurationPanel.addBooleanOption(CONFIG_LDAP_SEARCH_WITH_FALLBACK, "!!Fallback-Suche im ganzen Verzeichnis", true);
        configurationPanel.addStringOption(CONFIG_LDAP_IPARTS_ROLE, "!!Rolle nach der im Directory gesucht werden soll", false);
        addTimeIntervalControl(configurationPanel, CONFIG_LDAP_SYNC_INTERVAL, "!!Zeitraum für die Benutzer-Synchronisierung", "!!Bei leerer Endzeit wird ab Startzeitpunkt kontinuierlich synchronisiert.");
        configurationPanel.addIntegerSpinnerOption(CONFIG_LDAP_SYNC_DELAY, "!!Wartezeit bis zur nächsten Benutzer-Synchronisierung in Minuten",
                                                   false, 1, 10000, 10);
        configurationPanel.addStringOption(CONFIG_LDAP_SEARCH_ATTRIBUTE, "!!LDAP-Attributname für die Suche", true);
        configurationPanel.addStringOption(CONFIG_LDAP_SEARCH_VALUE, "!!Wert für die Suche im LDAP-Verzeichnis", true).setTooltip("!!Wildcards sind möglich; Groß- und Kleinschreibung muss beachtet werden");
        configurationPanel.endGroup();

        // Benutzerverwaltung
        configurationPanel.addSeparator();
        configurationPanel.startGroup("!!Benutzerverwaltung");
        configurationPanel.addBooleanOption(CONFIG_USER_ADMIN_ENABLED, "!!Aktiviert", false).setTooltip("!!GUI-Sessions (außer Administrator-Modus) sind nur mit aktiver Benutzerverwaltung möglich");
        configurationPanel.addBooleanOption(CONFIG_LDAP_USER_REQUIRED_FOR_LOGIN, "!!LDAP-Benutzer muss für das Login (nicht Administrator-Modus) im Siteminder-Token übergeben werden", false).
                setTooltip("!!Falls deaktiviert: Fallback auf Benutzer im Startparameter \"userId\" bzw. Browser-Cookie bei fehlendem LDAP-Benutzer im Siteminder-Token");
        configurationPanel.addBooleanOption(CONFIG_GUEST_LOGIN, TranslationHandler.translate("!!Gast-Zugang mit dem Benutzer \"%1\" erlaubt als Fallback bei fehlgeschlagenem Login",
                                                                                             iPartsUserAdminDb.USER_NAME_GUEST), false);
        configurationPanel.addBooleanOption(CONFIG_LOGIN_NEEDED_FOR_ADMIN_MODE, "!!Login benötigt für den Administrator-Modus", false);
        configurationPanel.endGroup();

        // Job-Logs
        configurationPanel.addSeparator();
        configurationPanel.startGroup("!!Einstellungen für Logs");
        configurationPanel.addIntegerSpinnerOption(CONFIG_LIMIT_JOB_LOGS_IN_TABLE, "!!Maximale Anzahl Jobs pro Tabelle",
                                                   false, 1, 100000, 1);
        configurationPanel.addFileOption(CONFIG_JOB_LOGS_DIR_RUNNING, "!!Speicherort für die Logs von laufenden Jobs",
                                         false, GuiFileChooserDialog.FILE_MODE_DIRECTORIES,
                                         FileChooserPurpose.SAVE, null).setTooltip("!!Leer für Temp-Verzeichnis");
        configurationPanel.addFileOption(CONFIG_JOB_LOGS_DIR, "!!Speicherort für die Logs von durchgeführten und fehlerhaften Jobs",
                                         true, GuiFileChooserDialog.FILE_MODE_DIRECTORIES,
                                         FileChooserPurpose.SAVE, null);
        configurationPanel.addFileOption(CONFIG_JOB_LOGS_ARCHIVE_DIR, "!!Speicherort für das Logs-Archiv", true,
                                         GuiFileChooserDialog.FILE_MODE_DIRECTORIES, FileChooserPurpose.SAVE, null);
        configurationPanel.endGroup();

        // Einstellungen für GUI-Controls
        configurationPanel.addSeparator();
        configurationPanel.addIntegerSpinnerOption(CONFIG_MAX_RESULTS_FOR_COMBOBOX_SEARCH, "!!Maximale Anzahl Treffer für die Suche in ComboBoxen",
                                                   false, 1, 1000, 1);

        // Maximale Gültigkeitsdauer für die Auswertungen für Teilepositionen in der Konstruktion
        configurationPanel.addSeparator();
        configurationPanel.addIntegerSpinnerOption(CONFIG_MAX_VALIDITY_REPORT_CONST_NODE_CALCULATIONS, "!!Maximale Gültigkeitsdauer für die Auswertungen für Teilepositionen in der Konstruktion in Minuten",
                                                   false, 1, 10000, 1);

        // Polling für das Zurücksetzen der Caches nach einer Publikation
        configurationPanel.addSeparator();
        configurationPanel.addIntegerSpinnerOption(CONFIG_CHECK_PUBLISHING_POLLING_DELAY, "!!Wartezeit für die Überprüfung zum Zurücksetzen der Caches aufgrund einer Publikation in Minuten",
                                                   false, -1, 10000, 1).setTooltip("!!-1 oder 0 für kein automatisches Zurücksetzen der Caches");

        // Zusätzliches Script in GuiWindow für jede ausgelieferte HTML-Seite (z.B. für Performance-Messungen)
        configurationPanel.addSeparator();
        GuiTextArea additionalSciptTextArea = configurationPanel.addStringAreaOption(CONFIG_ADDITIONAL_GUIWINDOW_SCRIPT, "!!Zusätzliches Script für jede ausgelieferte HTML-Seite",
                                                                                     false, true);
        additionalSciptTextArea.setMaximumWidth(600);
        additionalSciptTextArea.setLineWrap(true);

        // SQL Performance Tests
        configurationPanel.addSeparator();
        configurationPanel.addFileOption(CONFIG_SQL_PERFORMANCE_TESTS_DIR, "!!Verzeichnis für SQL Performance Tests", false,
                                         GuiFileChooserDialog.FILE_MODE_DIRECTORIES, FileChooserPurpose.OPEN, null);
        GuiButton startSQLPerformanceTestsButton = new GuiButton("!!SQL Performance Tests...");
        startSQLPerformanceTestsButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                executeSQLPerformanceTests().run(null);
            }
        });
        configurationPanel.addButton(startSQLPerformanceTestsButton);

        configurationPanel.addSeparator();
        configurationPanel.addBooleanOption(CONFIG_USE_CHANGESET_SIM_FOR_JOINS, "!!Änderungsset-Simulation für einfache Joins anstatt Pseudo-Transaktionen", false);
        configurationPanel.addIntegerSpinnerOption(CONFIG_MAX_NEW_DATA_OBJECTS_IN_CHANGESET_SIM_FOR_JOINS, "!!Maximale Anzahl neuer Datenobjekte pro Tabelle in der Änderungsset-Simulation für einfache Joins",
                                                   false, 0, 5000, 1);

        wsDWKUpdateGUI.addToConfigPanel(configurationPanel);

        return configurationPanel;
    }

    @Override
    public void configurationChanged() {
        initConfigurationSettingsVariables();
        super.configurationChanged();

        if (AbstractApplication.isOnline() && (mqInitialized || isEditPluginActive || isImportPluginActive)) {
            MQHelper.getInstance().startOrStopAllConsumers();
        }

        if (isActive()) {
            if (pluginConfig.getConfigValueAsBoolean(CONFIG_USER_ADMIN_ENABLED)) {
                if (!iPartsUserAdminDb.isActive()) {
                    try {
                        UserAdminDbActions.disposeInstance();
                        UserAdminDbActions.createInstance(getConfig());
                    } catch (Exception e) {
                        Logger.logExceptionWithoutThrowing(LOG_CHANNEL_USER_ADMIN, LogType.ERROR, e);
                    }
                }
            } else {
                iPartsUserAdminDb.disposeInstance();
            }
            if (mqProject != null) {
                mqProject.updateDBConnectionIfNecessary("iParts background project", LOG_CHANNEL_DEBUG);
            }
            if (ldapProject != null) {
                ldapProject.updateDBConnectionIfNecessary("LDAP project", LOG_CHANNEL_LDAP);
            }

            // LDAP-Synchronisierung neu starten (speziell, um das Warten im LDAP-Thread zu unterbrechen)
            restartLDAPSyncThread();

            // Thread für das Polling zum Überprüfen auf eine Publikation neu starten (speziell, um das Warten im Polling-Thread
            // zu unterbrechen)
            restartCheckPublishingPollingThread();

            ApplicationEvents.fireEventInAllProjects(new PreventTransmissionToASPLMEnabledChangeEvent(getPluginConfig().getConfigValueAsBoolean(CONFIG_PREVENT_TRANSMISSION_TO_ASPLM)),
                                                     true, true, null);
        }
    }

    @Override
    public boolean setActiveState(boolean active) {
        boolean activeStateChanged = super.setActiveState(active);

        if (active) {
            if (!iPartsFrameworkMain.class.isAssignableFrom(AbstractApplication.getApplication().getClass()) && !AbstractApplication.unit_tests_running) {
                throw new RuntimeException("Wrong main class! The following iParts specific main class must be used: "
                                           + iPartsFrameworkMain.class.getName());
            }

            // Animationen (de-)aktivieren
            BrowserInfo.SHOW_ANIMATIONS = getPluginConfig().getConfigValueAsBoolean(CONFIG_SHOW_ANIMATIONS);

            // Sonst haben wir ein Performance-Loch...
            EtkDataObject.getFieldValueAsMultiLanguageWithLoadingAllLanguages = false;

            // Änderungsset-Simulation von einfachen Joins über getRecords() oder Pseudo-Transaktionen?
            EtkDataObjectList.PSEUDO_TRANSACTIONS_FOR_SIMPLE_JOINS = !getPluginConfig().getConfigValueAsBoolean(CONFIG_USE_CHANGESET_SIM_FOR_JOINS);
            EtkDataObjectList.MAX_NEW_DATA_OBJECTS_FOR_SIMPLE_JOINS_SIM = getPluginConfig().getConfigValueAsInteger(CONFIG_MAX_NEW_DATA_OBJECTS_IN_CHANGESET_SIM_FOR_JOINS);
            EtkDataObjectList.LOG_CHANNEL_CHANGE_SET_JOIN_SIMULATION = LOG_CHANNEL_CHANGE_SET_SIM_FOR_JOINS;

            iPartsDataCodeList.SHOW_PROVAL_CODE_DESC = getPluginConfig().getConfigValueAsBoolean(CONFIG_DISPLAY_PROVAL_CODE_DESC);
            DataObjectGrid.USE_CONFIG_DISPLAY_FIELD_WIDTH_AS_MAX = getPluginConfig().getConfigValueAsBoolean(CONFIG_USE_CONFIG_DISPLAY_FIELD_WIDTH_AS_MAX);
            SimpleSelectSearchResultGrid.MAX_SELECT_SEARCH_RESULTS_SIZE = iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE;
            SelectSearchGridMaterial.MAX_MATERIAL_SEARCH_RESULTS_SIZE = iPartsConst.MAX_MATERIAL_SEARCH_RESULTS_SIZE;
            RelatedInfoBaseForm.CASCADING_WINDOW_OFFSET_WIDTH = iPartsConst.CASCADING_WINDOW_OFFSET_WIDTH;
            RelatedInfoBaseForm.CASCADING_WINDOW_OFFSET_HEIGHT = iPartsConst.CASCADING_WINDOW_OFFSET_HEIGHT;

            // Visualisierung für iParts anpassen
            AbstractGuiControl.VISUALIZE_EDITABLE_LIKE_ENABLED = true;

            // Übernehmen im Admin-Modus soll die Caches nicht löschen
            ConfigurationWindow.APPLY_WITHOUT_CLEARING_CACHES = true;

            initDatabaseOptions();

            int numberOfEntries = getConfig().getInteger(SystemSettings.XML_CONFIG_PATH_BASE + SystemSettings.XML_CONFIG_SUBPATH_TABLE_SPLIT_NUMBER_OF_ENTRIES, 100);
            AssemblyListForm.setHowMuchMaxRowsPerPage(numberOfEntries);
            AssemblyListForm.setStoreColumnFilterInSession(true);
            AssemblyListForm.setDelayedUpdatesForNewInstances(true);
            AssemblyListForm.setMenuItemConfigVisible(true);
            AssemblyListForm.LOG_CHANNEL_PART_LISTS_STACKTRACE = LOG_CHANNEL_PART_LISTS_STACKTRACE;
            AssemblyListForm.USE_UNIQUE_TABLE_NAMES = true; // Damit die autom. Test im iParts die Listen identifizieren können.
            AssemblyImageForm.setExplicitThumbnailsImageViewerConfig(new ThumbnailsImageViewerConfig() {
                @Override
                public int getThumbnailMinButtonSize() {
                    return 220; // Ergibt etwa 200 Pixel für die eigentliche Zeichnung, weil hier auch Ränder enthalten sind
                }

                @Override
                public int getThumbnailMinButtonSizeOnMobile() {
                    return getThumbnailMinButtonSize();
                }

                @Override
                public int getThumbnailMaxButtonSize() {
                    return Integer.MAX_VALUE;
                }

                @Override
                public int getThumbnailPadding() {
                    return 8;
                }
            });

            // Unnötige updateData()-Aktionen mit Neuladen der Doku vermeiden
            AbstractDocuForm.docuDataHasNoFilter = true;
            AbstractDocuForm.docuDataIsReadOnly = true;

            AbstractRelatedInfoMainForm.setInitialWindowSize(-1, -1);
            GuiTable.setHoldVerticalPositionByPaging(true);
        }

        if (interAppComHelper != null) {
            // interAppComHelper überprüft selbst, ob er schon aktiv ist oder nicht
            boolean iacActiveChanged = interAppComHelper.setActive(active);

            // Periodische Aufrufe mit setActiveState(true) können über interAppComHelper.addMessageReceiver()
            // bzw. interAppComHelper.removeMessageReceiver() zu Deadlocks führen -> nur bei Änderung vom Aktiv-Zustand aufrufen
            if (iacActiveChanged) {
                if (active) {
                    interAppComHelper.addMessageReceiver(interAppComMessageManager);
                } else {
                    interAppComHelper.removeMessageReceiver(interAppComMessageManager);
                }
            }
        }

        if (activeStateChanged && (publishingHelper != null)) {
            if (active) {
                restartCheckPublishingPollingThread();
            } else {
                publishingHelper.stopPollingThread();
            }
        }

        return activeStateChanged;
    }

    /**
     * Initialisiert einige iParts-spezifische Datenbank-Optionen.
     */
    public static void initDatabaseOptions() {
        // Dauert unter Oracle einfach zu lange und brauchen wir eh nicht
        DBDatabase.GET_CASE_INSENSITIVE_FIELDS_ENABLED = false;

        // Alle Tabellen der Benutzerverwaltung sollen auch ein T_STAMP-Feld haben
        AbstractDBObject.WITH_STAMP_FIELD = true;

        // Ist bei den Millionen von Texten in iParts völlig unpraktikabel (Performance-Grab) und wird auch nicht benötigt
        EtkSqlDbSelect.SEARCH_IN_LONG_TEXTS_ENABLED = false;
    }

    @Override
    protected void releaseEndpoints() {
        if (interAppComHelper != null) {
            // Disconnect aller Clients vom Server bzw. Deregistrierung vom Client nur dann durchführen, wenn die Anwendung
            // nicht gerade beendet wird bzw. wenn nicht der IAC QuickShutDown aktiv ist
            interAppComHelper.releaseEndpoints(FrameworkMain.getApplication().isRunning()
                                               || !StartParameter.getSystemPropertyBoolean(Constants.FRAMEWORK_VM_PARAMETER_INTER_APP_COM_QUICK_SHUT_DOWN, false));
        }
        super.releaseEndpoints();
    }

    @Override
    public void releaseReferences() {
        if (isActive()) {
            if (AbstractApplication.isOnline() && mqInitialized) {
                MQHelper.getInstance().closeConnection();
                MQPicScheduler.getInstance().stopWorkerThread();
                MQPicScheduler.getInstance().deregisterMQListener();
            }

            if (mqProject != null) {
                mqProject.setDBConnectionWatchDogActive(false, LOG_CHANNEL_DEBUG);
            }
            if (mqSession != null) {
                SessionManager.getInstance().destroySession(mqSession);
                mqSession = null;
            }
            mqProject = null;

            if (ldapProject != null) {
                ldapProject.setDBConnectionWatchDogActive(false, LOG_CHANNEL_LDAP);
            }
            if (ldapSession != null) {
                SessionManager.getInstance().destroySession(ldapSession);
                ldapSession = null;
            }
            ldapProject = null;

            iPartsUserAdminDb.disposeInstance();
            preventTransmissionToASPLMEnabledChangeEvent = null;

            JedisPooled redisPoolLocal = getRedisPool();
            if (redisPoolLocal != null) {
                redisPoolLocal.close();
                redisPool = null;
            }
        }
        super.releaseReferences();
    }

    @Override
    public String getRequiredInternalAppName() {
        return FrameworkMain.INTERNAL_APP_NAME;
    }

    @Override
    public Colors[] getPluginColors() {
        return new Colors[]{ clPlugin_iParts_FilterSelectInvalidForegroundColor,
                             clPlugin_iParts_CodeMatrixBackground_positive,
                             clPlugin_iParts_CodeMatrixBackground_negative,
                             clPlugin_iParts_CodeMatrixFontColor_positive,
                             clPlugin_iParts_CodeMatrixFontColor_negative,
                             clPlugin_iParts_SVG_inactiveTextColor,
                             clPlugin_iParts_SVG_highlightPathColor,
                             clPlugin_iParts_SVG_basePathColor,
                             clPlugin_iParts_SVG_highlightBackgroundColor,
                             clPlugin_iParts_SVG_highlightTextColor,
                             clPlugin_iParts_SVG_inCartPathColor,
                             clPlugin_iParts_SVG_inCartBackgroundColor,
                             clPlugin_iParts_SVG_inCartTextColor };
    }

    @Override
    public DesignImage[] getPluginImages() {
        Collection<? extends DesignImage> images = iPartsDefaultImages.getImages();
        return images.toArray(new DesignImage[images.size()]);
    }

    @Override
    public LogChannels[] getPluginLogChannels() {
        return new LogChannels[]{ LOG_CHANNEL_DEBUG, LOG_CHANNEL_PERFORMANCE, LOG_CHANNEL_PART_LISTS_STACKTRACE, LOG_CHANNEL_CODES,
                                  LOG_CHANNEL_MQ, LOG_CHANNEL_XML_MQ, LOG_CHANNEL_MODELS, LOG_CHANNEL_DATACARD_SERVICE,
                                  LOG_CHANNEL_INTER_APP_COM_REGISTRY, LOG_CHANNEL_INTER_APP_COM_CLIENT, LOG_CHANNEL_INTER_APP_COM_PING,
                                  LOG_CHANNEL_INTER_APP_COM_WEBSERVICE, LOG_CHANNEL_LDAP, LOG_CHANNEL_CHANGE_SETS, LOG_CHANNEL_CHANGE_SET_SIM_FOR_JOINS,
                                  LOG_CHANNEL_USER_ADMIN, LOG_CHANNEL_BUFFERED_SAVE, LOG_CHANNEL_PUBLISHING, LOG_CHANNEL_SINGLE_PIC_PARTS,
                                  LOG_CHANNEL_TRUCK_BOM_FOUNDATION, LOG_CHANNEL_DIA_4_U, LOG_CHANNEL_PROVAL, LOG_CHANNEL_MAILBOX };
    }

    @Override
    public void adjustTheme(Theme theme) {
        if (theme instanceof MaterialTheme) {
            MaterialTheme materialTheme = (MaterialTheme)theme;

            // Icons
            String materialSubDir = "material";
            materialTheme.addMaterialIconsToHashSet(iPartsDefaultImages.class, materialSubDir, "AttachmentTableHeader_16px",
                                                    new MaterialThemeIconComponent(DefaultImages.noteAttachmentTableHeader, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(iPartsDefaultImages.class, materialSubDir, "img_button_down_16px",
                                                    new MaterialThemeIconComponent(iPartsDefaultImages.edit_btn_down, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(iPartsDefaultImages.class, materialSubDir, "img_button_down_all_16px",
                                                    new MaterialThemeIconComponent(iPartsDefaultImages.edit_btn_down_all, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(iPartsDefaultImages.class, materialSubDir, "img_button_left_16px",
                                                    new MaterialThemeIconComponent(iPartsDefaultImages.edit_btn_left, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(iPartsDefaultImages.class, materialSubDir, "img_button_left_all_16px",
                                                    new MaterialThemeIconComponent(iPartsDefaultImages.edit_btn_left_all, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(iPartsDefaultImages.class, materialSubDir, "img_button_move_to_position_16px",
                                                    new MaterialThemeIconComponent(iPartsDefaultImages.edit_btn_move_to_position, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(iPartsDefaultImages.class, materialSubDir, "img_button_right_16px",
                                                    new MaterialThemeIconComponent(iPartsDefaultImages.edit_btn_right, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(iPartsDefaultImages.class, materialSubDir, "img_button_right_all_16px",
                                                    new MaterialThemeIconComponent(iPartsDefaultImages.edit_btn_right_all, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(iPartsDefaultImages.class, materialSubDir, "img_button_up_16px",
                                                    new MaterialThemeIconComponent(iPartsDefaultImages.edit_btn_up, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(iPartsDefaultImages.class, materialSubDir, "img_button_up_all_16px",
                                                    new MaterialThemeIconComponent(iPartsDefaultImages.edit_btn_up_all, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(iPartsDefaultImages.class, materialSubDir, "img_edit_cut_16px",
                                                    new MaterialThemeIconComponent(iPartsDefaultImages.edit_btn_cut, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(iPartsDefaultImages.class, materialSubDir, "img_edit_fromclipboard_16px",
                                                    new MaterialThemeIconComponent(iPartsDefaultImages.edit_btn_fromClipboard, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(iPartsDefaultImages.class, materialSubDir, "img_edit_new_multiple_16px",
                                                    new MaterialThemeIconComponent(iPartsDefaultImages.edit_btn_new_multiple, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(iPartsDefaultImages.class, materialSubDir, "img_edit_selectall_16px",
                                                    new MaterialThemeIconComponent(iPartsDefaultImages.edit_btn_selectAll, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(iPartsDefaultImages.class, materialSubDir, "img_edit_toclipboard_16px",
                                                    new MaterialThemeIconComponent(iPartsDefaultImages.edit_btn_toClipboard, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(iPartsDefaultImages.class, materialSubDir, "img_export_16px",
                                                    new MaterialThemeIconComponent(iPartsDefaultImages.edit_export, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(iPartsDefaultImages.class, materialSubDir, "img_import_16px",
                                                    new MaterialThemeIconComponent(iPartsDefaultImages.edit_import, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(iPartsDefaultImages.class, materialSubDir, "img_edit_authororder_activate_16px",
                                                    new MaterialThemeIconComponent(iPartsDefaultImages.edit_btn_ao_activate, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(iPartsDefaultImages.class, materialSubDir, "img_edit_authororder_deactivate_16px",
                                                    new MaterialThemeIconComponent(iPartsDefaultImages.edit_btn_ao_deactivate, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(iPartsDefaultImages.class, materialSubDir, "img_clear_16px",
                                                    new MaterialThemeIconComponent(iPartsDefaultImages.edit_clear, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(iPartsDefaultImages.class, materialSubDir, "img_lang_selectall_16px",
                                                    new MaterialThemeIconComponent(iPartsDefaultImages.edit_lang_selectall, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(iPartsDefaultImages.class, materialSubDir, "img_move_to_parentwindow_16px",
                                                    new MaterialThemeIconComponent(iPartsDefaultImages.btn_move_to_parentwindow,
                                                                                   iPartsDefaultImages.btn_move_to_parentwindow_hover,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(iPartsDefaultImages.class, materialSubDir, "img_refresh_view_16px",
                                                    new MaterialThemeIconComponent(iPartsDefaultImages.edit_refresh_view,
                                                                                   iPartsDefaultImages.edit_refresh_view_hover,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(iPartsDefaultImages.class, materialSubDir, "img_lock_entry_16px",
                                                    new MaterialThemeIconComponent(iPartsDefaultImages.edit_lock_entry, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(iPartsDefaultImages.class, materialSubDir, "img_unlock_entry_16px",
                                                    new MaterialThemeIconComponent(iPartsDefaultImages.edit_unlock_entry, null,
                                                                                   null, null));
        }
    }

    @Override
    public boolean checkLicense() {
        // iParts Lizenzoption
        return (LicenseConfig.getInstance() == null) || LicenseConfig.getInstance().licenseFunctionExists(iPartsConst.LICENSE_KEY_IPARTS);
    }

    @Override
    public EtkDbObjectsLayer createDbObjectsLayer(EtkProject project) {
        return new iPartsDbObjectsLayer(project.getEtkDbs());
    }

    @Override
    public EtkRevisionsHelper createRevisionsHelper() {
        // iPartsRevisionsHelper nur bei aktivem iPartsEditPlugin verwenden
        if (isEditPluginActive || forceRevisionsHelperForTesting) {
            Session session = Session.get();
            if (forceRevisionsHelperForTesting || ((session != null) && session.canHandleGui())) { // ChangeSets gibt es nur bei Sessions mit GUI
                return new iPartsRevisionsHelper();
            }
        }

        return null;
    }

    @Override
    public EtkDataAssembly createDataAssembly(EtkProject project, String kVari, String kVer, boolean cacheAssemblyEntries) {
        return new iPartsDataAssembly(project, kVari, kVer, cacheAssemblyEntries);
    }

    @Override
    public EtkDataPartListEntry createDataPartListEntry(EtkProject project, DBDataObjectAttributes catalogAttributes) {
        return new iPartsDataPartListEntry(project, catalogAttributes);
    }

    @Override
    public EtkDataPartListEntry createDataPartListEntry(EtkProject project, EtkDataAssembly ownerAssembly, DBDataObjectAttributes catalogAttributes) {
        return new iPartsDataPartListEntry(project, ownerAssembly, catalogAttributes);
    }

    @Override
    public EtkDataPartListEntry createDataPartListEntry(EtkProject project, PartListEntryId id) {
        return new iPartsDataPartListEntry(project, id);
    }

    @Override
    public EtkDataPart createDataPart(EtkProject project, PartId partId) {
        return new iPartsDataPart(project, partId);
    }

    @Override
    public EtkDataImage createDataImage(EtkProject project, AssemblyId assemblyId, String imageIndex, String imagePoolNo,
                                        String imagePoolVer) {
        return new iPartsDataImage(project, assemblyId, imageIndex, imagePoolNo, imagePoolVer);
    }

    @Override
    public EtkDataImageList createDataImageList() {
        return new iPartsDataImageList();
    }

    @Override
    public EtkDataHotspot createDataHotspot() {
        return new iPartsDataHotspot();
    }

    @Override
    public EtkDataHotspotList createDataHotspotList() {
        return new iPartsDataHotspotList();
    }

    @Override
    public EtkDataPool createDataPool() {
        return new iPartsDataPool();
    }

    @Override
    public EtkDataPoolVariants createDataPoolVariants() {
        return new iPartsDataPoolVariants();
    }

    @Override
    public EtkDataPoolEntry createDataPoolEntry() {
        return new iPartsDataPoolEntry();
    }

    @Override
    public EtkDataIcon createDataIconEntry() {
        return null;
    }

    @Override
    public EtkDataObject createDataObject(String type, Object... parameters) {
        return null;
    }

    @Override
    public EtkPartsSearchDataset createSearchDataset(EtkProject project) {
        return new iPartsSearchDataset(project);
    }

    @Override
    public EtkSubstitutionSearchDataset createSubstitutionSearchDataset(EtkProject project) {
        return null;
    }

    @Override
    public EtkSubstitutionSearchIntegrationLocationsDataset createSubstitutionSearchIntegrationLocationsDataset(EtkProject project) {
        return null;
    }

    @Override
    public EtkDataDocumentHelper createDataDocumentHelper(EtkProject project) {
        return null;
    }

    @Override
    public void clearPluginCaches(ClusterEventInterface event) {
        clearPluginCaches(event, true, true, true, null);
    }

    public void clearPluginCaches(ClusterEventInterface event, boolean clearGlobalCaches, boolean clearResponseDataCache,
                                  boolean clearResponseSpikesCache, Set<iPartsProductId> productStructureIdsToClearInCache) {
        EtkProject project = null;
        EnumSet<iPartsCacheType> cacheTypes = null;
        boolean clearAllSmallCaches = true;
        if (isActiveState()) {
            if (event instanceof iPartsClearAllCachesEvent) {
                cacheTypes = ((iPartsClearAllCachesEvent)event).getCacheTypes();
            }
            clearAllSmallCaches = (cacheTypes == null) || cacheTypes.contains(iPartsCacheType.ALL_SMALL_CACHES);

            String clearCacheTypeString;
            List<String> clearCaches = new DwList<>();
            if (clearAllSmallCaches) {
                boolean clearAllCaches = clearGlobalCaches && clearResponseDataCache && clearResponseSpikesCache && (productStructureIdsToClearInCache == null);
                if (!clearAllCaches) {
                    clearCacheTypeString = "retail relevant";
                    if (clearGlobalCaches) {
                        clearCaches.add("global caches");
                    }
                    if (clearResponseDataCache) {
                        clearCaches.add("iPartsResponseData");
                    }
                    if (clearResponseSpikesCache) {
                        clearCaches.add("iPartsResponseSpikes");
                    }

                    if (productStructureIdsToClearInCache == null) {
                        clearCaches.add("all products");
                    } else if (!productStructureIdsToClearInCache.isEmpty()) {
                        String productIdsString = "products [";
                        for (iPartsProductId productId : productStructureIdsToClearInCache) {
                            productIdsString += productId.getProductNumber() + ", ";
                        }
                        productIdsString = StrUtils.removeLastCharacterIfCharacterIs(productIdsString, ", ");
                        productIdsString += "]";
                        clearCaches.add(productIdsString);
                    }

                } else {
                    clearCacheTypeString = iPartsClearAllCachesEvent.isClearAllCaches(cacheTypes) ? "all" : "all small";
                }
            } else {
                clearCacheTypeString = "specific";
            }

            if (cacheTypes != null) {
                EnumSet<iPartsCacheType> specificCacheTypes = EnumSet.copyOf(cacheTypes);
                specificCacheTypes.remove(iPartsCacheType.ALL_SMALL_CACHES);
                for (iPartsCacheType specificCacheType : specificCacheTypes) {
                    clearCaches.add(specificCacheType.name());
                }
            }

            String clearCachesString = "";
            if (!clearCaches.isEmpty()) {
                clearCachesString = " (including " + StrUtils.stringListToString(clearCaches, ", ") + ")";
            }

            if (JavaViewerApplication.getInstance() != null) {
                project = getProject();
            }
            Logger.log(LOG_CHANNEL_PERFORMANCE, LogType.INFO, "Clear " + clearCacheTypeString + " caches requested by "
                                                              + iPartsUserAdminDb.getUserNameForLogging(project)
                                                              + clearCachesString);

            if (publishingHelper != null) {
                // Gleich am Anfang aufrufen, um unnötiges doppeltes Cache-Löschen und v.a. WarmUp zu vermeiden durch zufälliges
                // Ablaufen der Polling-Wartezeit
                publishingHelper.loadPublishingDataFromDB();
            }
        }

        super.clearPluginCaches(event);

        if (clearGlobalCaches) {
            iPartsCachesProviderCache.clearCache();

            // Flag neu aus der Config auslesen, weil z.B. evtl. Verbindunsprobleme zum Caches-Provider inzwischen behoben sein könnten
            isCachesProviderActive = (pluginConfig == null) || pluginConfig.getConfigValueAsBoolean(CONFIG_CACHES_PROVIDER_ACTIVE);
            JedisPooled redisPoolLocal = getRedisPool();
            if (redisPoolLocal != null) {
                // Alle Daten im Redis-Cache löschen (Fehler ignorieren, aber loggen)
                try {
                    redisPoolLocal.flushAll();
                } catch (Exception e) {
                    Logger.logExceptionWithoutThrowing(LOG_CHANNEL_PERFORMANCE, LogType.ERROR, e);
                }
            }
        }

        if (clearGlobalCaches && clearAllSmallCaches) { // Alle "kleinen" globalen Caches löschen?
            iPartsDataVirtualFieldsDefinition.clearCache();
            EinPas.clearCache();
            iPartsDialogSeries.clearCache();
            iPartsEdsModel.clearCache();
            iPartsModel.clearCache();
            FactoriesHelper.clearCache();
            KgTuTemplate.clearCache();
            HmMSm.clearCache();
            Ops.clearCache();
            OpsScopeCache.clearCache();
            ModelElementUsage.clearCache();
            ModuleConstructionCache.clearCache();
            SubModuleConstructionCache.clearCache();
            MBSStructure.clearCache();
            MBSTextEntryCache.clearCache();
            iPartsMBSModel.clearCache();
            MappingHmMSmToEinPas.clearCache();
            MappingKgTuToEinPas.clearCache();
            MappingOpsToEinPas.clearCache();
            DictTxtKindIdByMADId.clearCache();
            iPartsOmittedParts.clearCache();
            iPartsFactories.clearCache();
            iPartsFactoryModel.clearCache();
            iPartsES1.clearCache();
            iPartsAggCodeMappingCache.clearCache();
            iPartsVehicleToAggregateCodeCache.clearCache();
            iPartsCodeMappingCache.clearCache();
            iPartsSpringMapping.clearCache();
            iPartsVINModelMappingCache.clearCache();
            iPartsBranchProductClassCache.clearCache();
            iPartsNote.clearCache();
            iPartsDIALOGSeriesValidityCache.clearCache();
            iPartsAccAndAsCodeCache.clearCache();
            iPartsVirtualCalcFieldDocuRel.clearBadCodes();
            iPartsConstructionKits.clearCache();
            iPartsWireHarnessSimplifiedParts.clearCache();
            iPartsCustomProperty.clearCache();
            iPartsUserAdminDb.clearAllUserAdminCaches();
            AggregateIdent.clearCache();
            iPartsProductModels.clearCache();
            DictImportTextCache.clearAllCaches();
            DictHashtagTextsCache.clearCache();
            iPartsStandardFootNotesCache.clearCache();
            iPartsPartFootnotesCache.clearCache();
            iPartsChangeSetInfoDefinitions.clearCache();
            iPartsTransitMappingCache.clearCache();
            iPartsDataErrorLocationLRUCache.clearCache();
            iPartsDataGenInstallLocationLRUCache.clearCache();
            iPartsModelBuildingCode.clearCache();
            iPartsCountryValidSeriesCache.clearCache();
            iPartsCountryInvalidPartsCache.clearCache();
            iPartsDataAssortmentClassesMappingCache.clearCache();
            iPartsDataAssortmentPermissionsMappingCache.clearCache();
            MediaServiceWebserviceUtils.clearCache();
            MediaServiceMediaObjectsService.clearCache();
            iPartsTruckBOMFoundationWebserviceUtils.clearCache();
            iPartsDia4UServiceUtils.clearCache();
            iPartsProValWebserviceUtils.clearCache();
            iPartsPEMPseudoDateCache.clearCache();
            iPartsDataScopeKgMappingCache.clearCache();
            iPartsAggTypeMappingCache.clearCache();
            iPartsGenVoTextsCache.clearCache();
            iPartsSPKMappingCache.clearCache();
        }

        if (clearGlobalCaches) {
            // Große Spezial-Caches löschen?
            if ((cacheTypes == null) || cacheTypes.contains(iPartsCacheType.WIRE_HARNESS)) {
                iPartsWireHarness.clearCache();
            }
            if ((cacheTypes == null) || cacheTypes.contains(iPartsCacheType.PRIMUS_REPLACEMENTS)) {
                iPartsPRIMUSReplacementsCache.clearCache();
            }
            if ((cacheTypes == null) || cacheTypes.contains(iPartsCacheType.DIALOG_FOOT_NOTES)) {
                iPartsDIALOGFootNotesCache.clearCache();
            }
        }

        if (clearAllSmallCaches) {
            iPartsStructure.clearCache();
            iPartsSA.clearCache();
            iPartsDataAssembly.clearAssemblyMetaDataCaches();
            iPartsFilter.clearCache();
            iPartsDataCardRetrievalHelper.clearCache(); // evtl. nur bei clearGlobalCaches -> aber sicher ist sicher
            iPartsDataGenVoPairingCache.clearCache();

            // Cache für Produktstrukturen komplett bzw. teilweise löschen
            if ((productStructureIdsToClearInCache == null) || (project == null)) {
                iPartsProduct.clearCache();
                KgTuForProduct.clearCache();
            } else {
                for (iPartsProductId productId : productStructureIdsToClearInCache) {
                    // Nur die Produktstruktur sowie die KG/TU-Struktur müssen für die relevanten Produkte aus dem Cache gelöscht werden
                    iPartsProductStructures.removeProductFromCache(project, productId);
                    KgTuForProduct.removeKgTuForProductFromCache(project, productId);

                    // Alle virtuellen Baugruppen für das Produkt (sowohl ohne als auch mit dazugemischten Aggregate-Produkten)
                    // müssen ebenfalls aus dem Cache entfernt werden
                    iPartsProduct product = iPartsProduct.getInstance(project, productId);
                    String virtualAssemblyIdPrefix = iPartsVirtualNode.getVirtualIdString(product, false, project);
                    EtkDataAssembly.removeDataAssembliesFromAllCaches(project, virtualAssemblyIdPrefix, true);
                    if (!product.isCommonProduct(project)) {
                        virtualAssemblyIdPrefix = iPartsVirtualNode.getVirtualIdString(product, true, project);
                        EtkDataAssembly.removeDataAssembliesFromAllCaches(project, virtualAssemblyIdPrefix, true);
                    }
                }
            }

            if (clearResponseDataCache) {
                iPartsResponseData.clearCache();
            }

            if (clearResponseSpikesCache) {
                iPartsResponseSpikes.clearCache();
            }
        }

        if (isActiveState()) {
            warmUpCaches(clearGlobalCaches);
        }
    }

    @Override
    public EtkDatabaseField getVirtualFieldDescription(String tableName, String virtualFieldName) {
        VirtualFieldDefinition definition = iPartsDataVirtualFieldsDefinition.findField(tableName, virtualFieldName);
        if (definition != null) {
            return definition.getDBDescription(getProject());
        }
        return null;
    }

    @Override
    public void modifyPartListTypes(EtkStuecklistenDescription partListDescription) {
        hideSingleSubAssemblyNodeForPartListType(partListDescription, iPartsConst.PARTS_LIST_TYPE_EINPAS_TU);
        hideSingleSubAssemblyNodeForPartListType(partListDescription, iPartsConst.PARTS_LIST_TYPE_STRUCT_TU);
        hideSingleSubAssemblyNodeForPartListType(partListDescription, iPartsConst.PARTS_LIST_TYPE_STRUCT_SA);
        hideSingleSubAssemblyNodeForPartListType(partListDescription, iPartsConst.PARTS_LIST_TYPE_STRUCT_SPECIAL_CAT_KG);
        hideSingleSubAssemblyNodeForPartListType(partListDescription, iPartsConst.PARTS_LIST_TYPE_CAR_PERSPECTIVE);
    }

    private void hideSingleSubAssemblyNodeForPartListType(EtkStuecklistenDescription partListDescription, String partListTypeName) {
        EtkEbenenDaten partListType = partListDescription.getEbene(partListTypeName);

        // Fallback auf Standard-Ebene durch nachträgliche Überprüfung vom Ebenen-Namen vermeiden
        if ((partListType != null) && (partListType.getName().equals(partListTypeName))) {
            partListType.setHideSingleSubAssemblyNode(true);
        } else {
            Logger.log(LOG_CHANNEL_DEBUG, LogType.ERROR, "Part list type '" + partListTypeName + "' not found.");
        }
    }

    @Override
    public String getProgramName() {
        String result = null;
        if (mqProject != null) {
            final String dwkFileName = mqProject.getConfig().getStorageInfo();
            if (mqProject.isEditModeActive()) { // Redaktionssystem (EditMode)
                result = TranslationHandler.translate("!!iParts Autorensystem") + (!dwkFileName.isEmpty() ? " - " + DWFile.extractFileName(dwkFileName, true) : "");
            } else {
                result = TranslationHandler.translate("!!iParts Retail");
            }
        }
        return result;
    }

    @Override
    public EtkDataEConnection createDataEConnection(EtkProject project, EDocuConnectionId id) {
        return null;
    }

    @Override
    public EtkDataEStructEntry createDataEStructEntry(EtkProject project, EDocuPositionElement id) {
        return null;
    }

    @Override
    public EtkDataEStructTree createDataEStructTree(EtkProject project, EDocuTreeId id) {
        return null;
    }

    @Override
    public EtkDataESchematicEntry createDataESchematicEntry(EtkProject project, EDocuSchematicEntryId id) {
        return null;
    }

    @Override
    public EtkDataESchematic createDataESchematic(EtkProject project, EDocuSchematicId id) {
        return null;
    }

    @Override
    public EtkDataESheet createDataESheet(EtkProject project, EDocuSheetId id) {
        return null;
    }

    @Override
    public EtkDataESheetImage createDataESheetImage(EtkProject project, EDocuSheetId id) {
        return null;
    }

    @Override
    public EtkDataETranslation createDataETranslation(EtkProject project, EDocuTranslationId id) {
        return null;
    }

    @Override
    public EtkDataEItem createDataEItem(EtkProject project, EDocuItemId id) {
        return null;
    }

    @Override
    public EtkDataEItemData createDataEItemData(EtkProject project, EDocuItemDataId id) {
        return null;
    }

    @Override
    public EtkDataEPartLink createDataEPartLink(EtkProject project, EDocuLinkId id) {
        return null;
    }

    @Override
    public EtkDataEPart createDataEPart(EtkProject project, EDocuPartId id) {
        return null;
    }

    @Override
    public EtkDataEPartData createDataEPartData(EtkProject project, EDocuPartId id) {
        return null;
    }

    @Override
    public EtkDataEMechLink createDataEMechLink(EtkProject project, AssemblyId id) {
        return null;
    }

    @Override
    public EtkDataELink createDataELink(EtkProject project, EDocuLinkId id) {
        return null;
    }

    @Override
    public EtkDataEHotspot createDataEHotspot(EtkProject project, EDocuHotspotId id) {
        return null;
    }

    @Override
    public EtkDataObjectArray createDataArray(EtkProject project, ArrayId id) {
        return null;
    }

    @Override
    public EtkDataTextEntry createDataTextEntry(EtkProject project, TextEntryId id) {
        return null;
    }

    @Override
    public SerializedDBDataObject createSerializedDBDataObject() {
        return new iPartsSerializedDBDataObject();
    }

    @Override
    public SerializedDBDataObject createSerializedDBDataObject(DBDataObject dbDataObject, SerializedDBDataObjectState state,
                                                               boolean modifiedOnly, boolean isDeleted, boolean isNew,
                                                               boolean resetModifiedFlags, boolean serializeVirtualFields) {
        return new iPartsSerializedDBDataObject(dbDataObject, state, modifiedOnly, isDeleted, isNew, resetModifiedFlags,
                                                serializeVirtualFields);
    }

    @Override
    public SerializedDBDataObjectList createSerializedDBDataObjectList() {
        return new iPartsSerializedDBDataObjectList();
    }

    @Override
    public SerializedDBDataObjectList createSerializedDBDataObjectList(List<? extends DBDataObject> list, List<? extends DBDataObject> deletedList,
                                                                       SerializedDBDataObjectState state, boolean modifiedOnly,
                                                                       boolean isDeleted, boolean resetModifiedFlags, boolean isCommitted,
                                                                       boolean serializeVirtualFields) {
        return new iPartsSerializedDBDataObjectList(list, deletedList, state, modifiedOnly, isDeleted, resetModifiedFlags,
                                                    isCommitted, serializeVirtualFields);
    }

    @Override
    public SerializedDBDataObjectHistory createSerializedDBDataObjectHistory() {
        return new iPartsSerializedDBDataObjectHistory();
    }

    @Override
    public Class<? extends SerializedDBDataObject> getSerializedDBDataObjectClass() {
        return iPartsSerializedDBDataObject.class;
    }

    @Override
    public Class<? extends SerializedDBDataObjectList> getSerializedDBDataObjectListClass() {
        return iPartsSerializedDBDataObjectList.class;
    }

    @Override
    public Class<? extends SerializedDBDataObjectHistory> getSerializedDBDataObjectHistoryClass() {
        return iPartsSerializedDBDataObjectHistory.class;
    }

    @Override
    public ShowStartWindowMoment getShowStartWindowMoment() {
        return ShowStartWindowMoment.BEFORE_LANGUAGE_SELECTION;
    }

    @Override
    public boolean showStartWindow() {
        if (!getProject().isEditModeActive() && !Constants.DEVELOPMENT) {
            MessageDialog.showError("!!Für die iParts Publikation stehen nur Web-Services aber keine separate grafische Oberfläche zur Verfügung!");
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<FrameworkSimpleEndpoint> createSimpleEndpoints() {
        List<FrameworkSimpleEndpoint> endpoints = new ArrayList<>();
        if (wsDWKUpdateGUI.isDWKUpdateServiceActive()) {
            String dwkUpdateServiceUri = wsDWKUpdateGUI.getURI();
            WSDWKUpdateEndpoint endpoint = new WSDWKUpdateEndpoint(wsDWKUpdateGUI) {
                @Override
                protected RESTfulTransferObjectInterface executeWebservice(EtkProject project, WSDWKUpdateRequest requestObject) throws RESTfulWebApplicationException {
                    RESTfulTransferObjectInterface response = super.executeWebservice(project, requestObject);
                    // TODO Code aktuell so nicht korrekt, weil description ähnlich wie in iPartsEtkConfig behandelt werden
                    // müsste (description.clear() und Merken der TypeConfigurations und Texte)
//                    if (response instanceof WSDWKUpdateResponse) {
//                        // The existence of the response means that the DWK switch went through, hence we have to apply
//                        // the current java definition to the (new) DWK
//                        EtkDatabaseDescription description = project.getConfig().getDBDescription();
//                        iPartsDBMigrations migrationsHelper = new iPartsDBMigrations(project.getConfig());
//
//                        // Ausführung in einem eigenen Thread, weil der aktuelle Thread bereits zur Session vom Webservice
//                        // gehört und in addStructureDefinitionToDatabaseDescription() eine neue Session erzeugt wird für
//                        // den aktuellen Thread
//                        FrameworkThread thread = new FrameworkThread("ApplyDatabaseDefinitionChangesThread", Thread.NORM_PRIORITY, () -> {
//                            try {
//                                migrationsHelper.addStructureDefinitionToDatabaseDescription(description, true);
//                                migrationsHelper.applyDefinitionChanges();
//                            } catch (Exception e) {
//                                Logger.log(LogChannels.DB_MIGRATION, LogType.ERROR, "Could not apply java definition after DWK update via webservice");
//                            } finally {
//                                migrationsHelper.clearReferences();
//                            }
//                        });
//                        thread.__internal_start();
//                    }
                    return response;
                }
            };
            addEndpoint(endpoint, wsDWKUpdateGUI.getURIConfigOption(), endpoints);
            Logger.log(LOG_CHANNEL_DEBUG, LogType.INFO, "DWK update webservice started with URI: " + dwkUpdateServiceUri);
        }

        if (interAppComHelper != null) {
            interAppComHelper.releaseEndpoints(true);

            boolean isRegistryServer = pluginConfig.getConfigValueAsBoolean(CONFIG_INTER_APP_COM_IS_REGISTRY_SERVER);
            String clientRegistryId = pluginConfig.getConfigValueAsString(CONFIG_INTER_APP_COM_CLIENT_REGISTRY_ID);

            endpoints.addAll(interAppComHelper.createEndpoints(pluginConfig.getConfigValueAsString(CONFIG_INTER_APP_COM_CHANNEL_NAME),
                                                               isRegistryServer, pluginConfig.getConfigValueAsString(CONFIG_INTER_APP_COM_REGISTRY_SERVER_URL),
                                                               pluginConfig.getConfigValueAsString(CONFIG_INTER_APP_COM_CLIENT_URL), clientRegistryId));
        }

        return endpoints;
    }

    private void addEndpoint(RESTfulEndpoint endpoint, UniversalConfigOption configOption, List<FrameworkSimpleEndpoint> endpoints) {
        FrameworkSimpleEndpoint.addEndpointWithEmptyURICheck(endpoint, configOption, endpoints, host, port, iPartsPlugin.LOG_CHANNEL_DEBUG);
    }

    @Override
    public void receiveProjectEvent(AbstractEtkProjectEvent event) {
        if (event instanceof ClearRetailRelevantCachesEvent) {
            getMqSession().invokeThreadSafeInSessionThread(() -> {
                ClearRetailRelevantCachesEvent clearRetailRelevantCachesEvent = (ClearRetailRelevantCachesEvent)event;
                assertProjectDbIsActive(getMqProject(), "ClearRetailRelevantCachesEvent", LOG_CHANNEL_DEBUG);
                clearRetailRelevantCachesEvent.clearRetailRelevantCaches(getMqProject());
            });
        } else if (event instanceof iPartsDataChangedEventByEdit) {
            // Caches (teilweise) löschen
            iPartsDataChangedEventByEdit editEvent = (iPartsDataChangedEventByEdit)event;
            boolean hasElementIds = Utils.isValid(editEvent.getElementIds());
            switch (editEvent.getDataType()) {
                case PRODUCT:
                    Collection<iPartsProductId> elementIds = ((iPartsDataChangedEventByEdit<iPartsProductId>)event).getElementIds();
                    if (editEvent.isClearDataTypeCache()) {
                        iPartsProduct.clearCache();
                        iPartsProductModels.clearCache();
                    } else if (hasElementIds && (editEvent.getAction() != iPartsDataChangedEventByEdit.Action.NEW)) {
                        EtkProject project = getProject();
                        for (iPartsProductId id : elementIds) {
                            iPartsProduct.removeProductFromCache(project, id);
                        }
                    }

                    // Nachträglich kann die ProductId nicht mehr verändert werden -> Cache mit den ProductIds muss
                    // nicht gelöscht werden
                    if (editEvent.getAction() != iPartsDataChangedEventByEdit.Action.MODIFIED) {
                        iPartsProduct.clearAllProductsCache();

                        // Cache für die Zuordnung Produkte zu Baumuster für alle gelöschten oder neuen Produkte aktualisieren
                        // Veränderte Produkte rufen diesen Code nur aus den Produkt-Stammdaten direkt auf
                        iPartsProductModels productModelsCache = iPartsProductModels.getInstance(getProject());
                        if (hasElementIds) {
                            for (iPartsProductId id : elementIds) {
                                productModelsCache.updateCacheByProduct(getProject(), id);
                            }
                        }
                    }

                    iPartsStructure.clearCache();
                    FactoriesHelper.clearCache();
                    EtkDataAssembly.clearGlobalEntriesCache();

                    // Cache für Verwendung der Module in Produkten löschen, wenn ein Produkt gelöscht wurde
                    if (editEvent.getAction() == iPartsDataChangedEventByEdit.Action.DELETED) {
                        iPartsDataAssembly.clearProductIdFromModuleUsageCache();
                    }

                    iPartsFilter.clearCache();
                    iPartsDataCardRetrievalHelper.clearCache();
                    break;
                case SA:
                    if (editEvent.getAction() != iPartsDataChangedEventByEdit.Action.NEW) {
                        iPartsProductStructures.clearCache();
                        if (hasElementIds) {
                            EtkProject project = getProject();
                            for (iPartsSaId id : ((iPartsDataChangedEventByEdit<iPartsSaId>)event).getElementIds()) {
                                iPartsSA.removeSAFromCache(project, id);
                            }
                        } else {
                            iPartsSA.clearCache();
                        }
                    }
                    iPartsFilter.clearCache();
                    break;
                case SAA:
                    if (editEvent.getAction() != iPartsDataChangedEventByEdit.Action.NEW) {
                        if (hasElementIds) {
                            for (iPartsSaaId id : ((iPartsDataChangedEventByEdit<iPartsSaaId>)event).getElementIds()) {
                                iPartsSA.removeSAADescriptionFromCache(id);
                            }
                        } else {
                            iPartsSA.clearSaSaaDescriptionsCache();
                        }
                    }
                    break;
                case SERIES:
                    if (editEvent.isClearDataTypeCache()) {
                        iPartsDialogSeries.clearCache();
                    } else if (hasElementIds && (editEvent.getAction() != iPartsDataChangedEventByEdit.Action.NEW)) {
                        EtkProject project = getProject();
                        for (iPartsSeriesId id : ((iPartsDataChangedEventByEdit<iPartsSeriesId>)event).getElementIds()) {
                            iPartsDialogSeries.removeSeriesFromCache(project, id);

                            // Alle Produkte für die Baureihe bestimmen und darin alle Module aus dem Cache entfernen
                            List<iPartsProduct> products = iPartsProductHelper.getProductsForSeries(project, id, null, null);
                            for (iPartsProduct product : products) {
                                Set<AssemblyId> moduleIds = iPartsProductStructures.getInstance(project, product.getAsId()).getModuleIds(project);
                                for (AssemblyId moduleId : moduleIds) {
                                    EtkDataAssembly.removeDataAssemblyFromAllCaches(project, moduleId);
                                }
                            }
                        }
                    }
                    iPartsDIALOGSeriesValidityCache.clearCache();
                    iPartsDataCardRetrievalHelper.clearCache();
                    break;
                case MODEL:
                    if (editEvent.isClearDataTypeCache()) {
                        iPartsModel.clearCache();
                    } else if (hasElementIds) {
                        Set<String> modifiedModelNumbers = new HashSet<>();
                        Set<iPartsSeriesId> affectedSeriesIds = new HashSet<>(); // Set für alle betroffenen Baureihen
                        EtkProject project = getProject();
                        for (iPartsModelId id : ((iPartsDataChangedEventByEdit<iPartsModelId>)event).getElementIds()) {
                            modifiedModelNumbers.add(id.getModelNumber());
                            iPartsSeriesId seriesId = iPartsModel.getInstance(project, id).getSeriesId();
                            if (seriesId.isValidId()) {
                                affectedSeriesIds.add(seriesId);
                            }

                            // Cache für das Baumuster löschen falls es nicht sowieso neu ist
                            if (editEvent.getAction() != iPartsDataChangedEventByEdit.Action.NEW) {
                                iPartsModel.removeModelFromCache(project, id);
                            }
                        }

                        // Alle Baumuster der betroffenen Baureihen aller veränderten/neuen Baumuster neu bestimmen
                        Set<String> affectedModelNumbers = new HashSet<>();
                        for (iPartsSeriesId affectedSeriesId : affectedSeriesIds) {
                            iPartsDialogSeries series = iPartsDialogSeries.getInstance(project, affectedSeriesId);
                            series.clearModelNumbersCache();
                            affectedModelNumbers.addAll(series.getModelNumbers(project));
                        }
                        affectedModelNumbers.removeAll(modifiedModelNumbers); // Gerade aus dem Cache entfernte Baumuster ignorieren

                        // Cache für die negativen BM-bildenden Code löschen bei allen betroffenen Baumustern
                        for (String affectedModelNumber : affectedModelNumbers) {
                            iPartsModel.getInstance(project, new iPartsModelId(affectedModelNumber)).clearNegativeModelBuildingCodeCache();
                        }
                    }
                    iPartsFilter.clearCache();
                    iPartsDataCardRetrievalHelper.clearCache();
                    break;
                case MATERIAL:
                    EtkDataAssembly.clearGlobalEntriesCache();
                    break;
                case PART_LIST:
                    boolean clearGlobalEntriesCache = false;
                    if (editEvent.isClearDataTypeCache()) {
                        clearGlobalEntriesCache = true;
                        iPartsDataAssembly.clearAssemblyMetaDataCaches();
                    } else if (hasElementIds) {
                        EtkProject project = getProject();
                        if (editEvent.getAction() != iPartsDataChangedEventByEdit.Action.NEW) {
                            for (AssemblyId id : ((iPartsDataChangedEventByEdit<AssemblyId>)event).getElementIds()) {
                                // Bei virtuellen Stücklisten sind die AssemblyMetaData irrelevant, so dass diese nicht
                                // aus allen Caches für alle Changesets entfernt werden müssen
                                iPartsDataAssembly.removeAssemblyMetaDataFromCache(project, id);

                                // Bei gelöschten Stücklisten muss der Cache für alle Stücklisten gelöscht werden, weil
                                // die Vater-Baugruppen nicht mehr bestimmt werden können, die aber auf jeden Fall aus dem
                                // Cache entfernt werden müssen
                                if (editEvent.getAction() == iPartsDataChangedEventByEdit.Action.DELETED) {
                                    if (project.isRevisionChangeSetActiveForEdit()) {
                                        // Bei aktivem Edit-Changeset muss nicht der globale Cache gelöscht werden, weil
                                        // nur das aktive Changeset betroffen ist
                                        EtkDataAssembly.removeCacheForActiveChangeSets(project);
                                    } else {
                                        clearGlobalEntriesCache = true;
                                    }
                                } else {
                                    // Virtuelle Stücklisten aus allen Caches entfernen, weil z.B. Daten in Konstruktions-
                                    // Stücklisten teilweise unabhängig von Changesets verändert werden können
                                    if (iPartsVirtualNode.isVirtualId(id)) {
                                        EtkDataAssembly.removeDataAssemblyFromAllCaches(project, id);
                                    } else {
                                        EtkDataAssembly.removeDataAssemblyFromCache(project, id);
                                    }
                                }
                            }
                        } else { // NEW
                            for (AssemblyId id : ((iPartsDataChangedEventByEdit<AssemblyId>)event).getElementIds()) {
                                // Bei neuen Stücklisten müssen alle Vater-Baugruppen ebenfalls aus dem Cache entfernt werden
                                EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, id);
                                List<EtkDataPartListEntry> parentAssemblies = assembly.getParentAssemblyEntries(false);
                                for (EtkDataPartListEntry parentAssembly : parentAssemblies) {
                                    // Die Vater-Baugruppen von virtuellen Stücklisten aus allen Caches entfernen, damit
                                    // deren Änderungen unabhängig von Changesets angezeigt werden können
                                    AssemblyId parentAssemblyId = parentAssembly.getAsId().getOwnerAssemblyId();
                                    if (iPartsVirtualNode.isVirtualId(id)) {
                                        EtkDataAssembly.removeDataAssemblyFromAllCaches(project, parentAssemblyId);
                                    } else {
                                        EtkDataAssembly.removeDataAssemblyFromCache(project, parentAssemblyId);
                                    }
                                }
                            }
                        }
                    }

                    if (clearGlobalEntriesCache) {
                        EtkDataAssembly.clearGlobalEntriesCache();
                    }
                    iPartsFilter.clearCache();
                    break;
                case DRAWING:
                    // aktuell muss hier nichts gemacht werden (EtkDataAssembly.clearGlobalEntriesCache() ist nicht notwendig,
                    // weil nur die Stücklisteneinträge, nicht aber die Zeichnungen im Cache gehalten werden)
                    break;
                case HMMSM:
                    if (editEvent.isClearDataTypeCache()) {
                        HmMSm.clearCache();
                    } else if (hasElementIds && (editEvent.getAction() == iPartsDataChangedEventByEdit.Action.MODIFIED)) {
                        // Aktuell wird nur MODIFIED zur Aktualisierung vom Flag DH_HIDDEN unterstützt
                        EtkProject project = getProject();
                        for (HmMSmId id : ((iPartsDataChangedEventByEdit<HmMSmId>)event).getElementIds()) {
                            HmMSmNode hmMSmNode = HmMSm.getInstance(project, new iPartsSeriesId(id.getSeries())).getNode(id);
                            if (hmMSmNode != null) {
                                iPartsDataHmMSm dataHmMSm = new iPartsDataHmMSm(project, id);
                                if (dataHmMSm.existsInDB()) {
                                    hmMSmNode.setHidden(dataHmMSm.getFieldValueAsBoolean(FIELD_DH_HIDDEN));
                                }
                            }
                        }
                    }

                    // DataChangedEvent in diesem Cluster-Knoten ist notwendig, wenn sich etwas an den HM/M/SM-Knoten ändert
                    // Dort wird dann auch iPartsFilter.clearCache() aufgerufen
                    ApplicationEvents.fireEventInAllProjects(new DataChangedEvent(), true, true, null);
                    break;
                case DICTIONARY:
                    if (editEvent.isClearDataTypeCache()) {
                        EtkProject project = getProject();
                        if (hasElementIds) {
                            // spezielle Caches löschen
                            boolean clearDictHashtagTextsCache = false;
                            boolean warmUpCache = false;
                            Set<DictTextKindTypes> textKindTypesToClear = new HashSet<>();
                            for (iPartsDictMetaCacheId id : ((iPartsDataChangedEventByEdit<iPartsDictMetaCacheId>)event).getElementIds()) {
                                textKindTypesToClear.add(id.getTextKindId());
                            }
                            for (DictTextKindTypes textKindType : textKindTypesToClear) {
                                DictImportTextCache.clearCacheForType(project, textKindType, true);
                                DictTextCache.clearCacheForTextKind(textKindType, false);
                                if (DictTextCache.isTextKindWithCache(textKindType)) {
                                    warmUpCache = true;
                                }
                                if ((textKindType == DictTextKindTypes.FOOTNOTE) || (textKindType == DictTextKindTypes.UNKNOWN)) {
                                    clearDictHashtagTextsCache = true;
                                }
                            }
                            if (clearDictHashtagTextsCache) {
                                DictHashtagTextsCache.clearCache();
                                DictHashtagTextsCache.warmUpCache(project);
                            }
                            if (warmUpCache) {
                                DictTextCache.warmUpCache();
                            }
                        } else {
                            // alle Caches löschen
                            DictTextCache.clearCache();
                            DictImportTextCache.clearAllCaches();
                            DictHashtagTextsCache.clearCache();
                            DictTextCache.warmUpCache();
                        }
                    } else if (hasElementIds) {
                        for (iPartsDictMetaCacheId id : ((iPartsDataChangedEventByEdit<iPartsDictMetaCacheId>)event).getElementIds()) {
                            DictTextKindTypes textKindType = id.getTextKindId();
                            boolean checkNotReleasedTexts = id.isNotReleasedTexts();
                            DictTextCache cache;
                            if (checkNotReleasedTexts) {
                                cache = DictTextCache.getInstanceWithAllTextStates(textKindType, id.getTextLanguage(), false);
                            } else {
                                cache = DictTextCache.getInstance(textKindType, id.getTextLanguage(), false);
                            }
                            if (cache != null) {
                                switch (editEvent.getAction()) {
                                    case DELETED:
                                        cache.removeText(id.getText(), id.getTextId(), id.getOriginType());
                                        break;
                                    case NEW:
                                        cache.addText(id.getText(), id.getTextId(), id.getOriginType(), true);
                                        break;
                                    case MODIFIED:
                                        cache.changeText(id.getText(), id.getTextId(), id.getOriginType());
                                        break;
                                }
                            }
                        }
                    }
                    break;
            }
        } else if (event instanceof NotesChangedEvent) {
            iPartsNote.clearCache();
            if (isWarmUpCaches()) {
                iPartsNote.warmUpCache(mqProject);
            }
        } else if (event instanceof DataChangedEvent) {
            iPartsFilter.clearCache();
        } else if (event instanceof iPartsPublishingEvent) {
            if (publishingHelper != null) {
                iPartsPublishingEvent publishingEvent = (iPartsPublishingEvent)event;
                publishingHelper.setPublishingData(publishingEvent.getPublishingGUID(), publishingEvent.getPublishingDate());

                DateConfig dateConfig = DateConfig.getInstance(mqProject.getConfig());
                String formattedPublishingDate = dateConfig.formatDateTime(Language.EN.getCode(), publishingEvent.getPublishingDate());
                Logger.log(iPartsPlugin.LOG_CHANNEL_PUBLISHING, LogType.DEBUG, "Publishing event received with date " + formattedPublishingDate);
            }
        } else if (event instanceof iPartsUserAdminChangedEvent) {
            iPartsUserAdminDb.clearAllUserAdminCaches();
        } else if (event instanceof iPartsClearTextCacheEvent) {
            iPartsClearTextCacheEvent textCacheEvent = (iPartsClearTextCacheEvent)event;
            DictImportTextCache.clearCacheForType(getProject(), textCacheEvent.getTextKindType(), textCacheEvent.isWarmUpCache());
            DictTextCache.clearCacheForTextKind(textCacheEvent.getTextKindType(), false); // warmUpIfNecessary würde nur ein WarmUp machen, wenn die Textart in der WarmUp-Liste enthalten ist
            if (textCacheEvent.isWarmUpCache()) {
                DictTextCache.warmUpCache();
            }
            if ((textCacheEvent.getTextKindType() == DictTextKindTypes.FOOTNOTE) || (textCacheEvent.getTextKindType() == DictTextKindTypes.UNKNOWN)) {
                DictHashtagTextsCache.clearCache();
                if (textCacheEvent.isWarmUpCache()) {
                    DictHashtagTextsCache.warmUpCache(getProject());
                }
            }
        } else if (event instanceof GetCacheDataEvent) {
            GetCacheDataEvent getCacheDataEvent = (GetCacheDataEvent)event;
            String cacheName = getCacheDataEvent.getCacheName();
            Map<String, String> cacheParameters = getCacheDataEvent.getCacheParameters();
            SetCacheDataEvent setCacheDataEvent = new SetCacheDataEvent(cacheName, cacheParameters);
            iPartsCachesProviderCache.getInstanceAndFillCacheData(getMqProject(), setCacheDataEvent);
            if (setCacheDataEvent.getCacheDataZipped() != null) {
                ApplicationEvents.fireEventInAllProjectsAndClusters(setCacheDataEvent, false, false, true, getCacheDataEvent.getSenderId(), null);
            }
        } else if (event instanceof SetCacheDataEvent) {
            SetCacheDataEvent setCacheDataEvent = (SetCacheDataEvent)event;
            iPartsCachesProviderCache.checkCachesForCreateCacheInstance(setCacheDataEvent, getMqProject());
        }
    }

    @Override
    public boolean needsStaticConnectionUpdates() {
        return interAppComHelper != null; // notwendig z.B. für Broadcasts
    }

    @Override
    public ClusterManagerInterface getClusterManager() {
        return interAppComHelper;
    }

    @Override
    public String getAdditionalModuleHeaderFields(EtkDataAssembly assembly, NavigationPath navPath, String categoryName, String fieldName) {
        if (assembly instanceof iPartsDataAssembly) {
            if (categoryName.equals(IPARTS_CATEGORY_HEADING) && fieldName.equals(IPARTS_HEADING_FIELD_AGG_NO)) {
                return ((iPartsDataAssembly)assembly).getAggNoForHeading(navPath);
            }
        }
        return null;
    }

    @Override
    public void modifySecondToolbar(final AbstractJavaViewerFormIConnector connector, StartPageType activeFormType) {
        GuiToolbarManager manager = connector.getToolbarManager();
        if (manager == null) {
            return;
        }
        if ((activeFormType == StartPageType.PARTS) || (activeFormType == StartPageType.SEARCH)) {
            // Filter Toolbar-Feld
            iPartsGuiToolFilterField component = new iPartsGuiToolFilterField(connector);
            component.setEditable(true);
            manager.insertButtonAfter(component, iPartsToolbarButtonAlias.TOOLBAR_FILTER_FIELD.getAlias(),
                                      ToolbarButtonAlias.PRINT.getAlias());

            // Filter-Button
            GuiToolButton button = iPartsToolbarButtonAlias.FILTER_IPARTS.createToolButton(
                    new EventListener(Event.ACTION_PERFORMED_EVENT) {
                        @Override
                        public void fire(Event event) {
                            showiPartsFilterDialog(connector);
                        }
                    });
            manager.insertButtonAfter(button, iPartsToolbarButtonAlias.FILTER_IPARTS.getAlias(),
                                      iPartsToolbarButtonAlias.TOOLBAR_FILTER_FIELD.getAlias());
            updateFilterButton(connector, button);
        }
    }
    /*=== FilterInterface ===*/

    /**
     * Gibt es einen Filter im Plugin?
     *
     * @return {@code true}, falls das Plugin Filtermöglichkeiten bereitstellt
     */
    @Override
    public boolean isFilterActive() {
        return true;
    }

    /**
     * Durchführung einer Filterung
     *
     * @param tableName  Tabelle, aus welcher der zu prüfende Datensatz kommt
     * @param attributes Datensatz, der gefiltert werden soll
     * @return {@code true}, falls keine Filterung durchgeführt wurde; {@code false}, wenn der Datensatz ausgefiltert wurde
     */
    @Override
    public boolean checkFilter(String tableName, DBDataObjectAttributes attributes) {
        return true;
    }

    @Override
    public GridFilterReturnType checkFilterInGrid(EtkFilterTyp filterTyp, String tableAndFieldName, DBDataObjectAttributes attributes, String language) {
        return GridFilterReturnType.NOT_FILTERED;
    }

    @Override
    public boolean verifyNegativeFilterResultForFilterArtAndLogic(DBDataObjectAttributes attributes, EtkFilterTyp filterType,
                                                                  EtkFilterItem filterItem, List<String> filterValues,
                                                                  String language) {
        String tablename = filterItem.getTableName();
        String fieldname = filterItem.getLowFieldName();
        if (tablename.equals(TABLE_KATALOG) && fieldname.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA_SOE)) {
            return attributes.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA_SOE).isEmpty();
        } else if (isSpecialEnumDataType(filterItem, filterType)) {
            return attributes.getFieldValue(filterItem.getLowFieldName()).isEmpty() && (filterValues.contains("") || filterValues.contains(" "));
        }
        return false;
    }

    private boolean isSpecialEnumDataType(EtkFilterItem filterItem, EtkFilterTyp filterType) {
        FilterArt filterArt = filterType.getFilterArt();
        boolean result = (filterItem.getFilterArt() == FilterArt.DTENUM) && (filterArt.name().equals(FilterArt.DTSOE.name()));
        if (!result) {
            if (filterType.getFilterTypus() == EtkFilterTyp.FilterTypus.GRIDFILTER) {
                result = filterArt.name().equals(FilterArt.DTSOE.name());
            }
        }
        return result;
    }

    /**
     * Durchführung einer Filterung
     *
     * @param etkDataObject Datensatz, der gefiltert werden soll
     * @param filterMode
     * @return {@code true}, falls keine Filterung durchgeführt wurde; {@code false}, wenn der Datensatz ausgefiltert wurde
     */
    @Override
    public boolean checkFilter(EtkDataObject etkDataObject, FilterMode filterMode) {
        return iPartsFilter.get().checkFilter(etkDataObject);
    }

    /**
     * Für Filterung benötigte Felder.
     * Wir brauchen diese Info weil die Felder im Zweifel erst Mal geladen werden müssen.
     *
     * @param neededTables Liste der Tabellen für die Felder zurück geliefert werden soll
     * @return Liste von Feldern (Format <TabellenName>.<FeldName>))
     */
    @Override
    public Set<String> getActiveFilterFields(Set<String> neededTables) {
        return iPartsFilter.get().getActiveFilterFields(neededTables);
    }
    /*=== FilterInterface Ende ===*/

    /*=== FilterExtraInterface ===*/

    @Override
    public List<String> getExtraValueSeparators() {
        return new DwList<>(COLUMN_FILTER_VALUE_SEPARATORS);
    }
    /*=== FilterInterface Ende ===*/

    public static boolean isWarmUpCaches() {
        return (mqSession != null) && (mqProject != null) && getPluginConfig().getConfigValueAsBoolean(CONFIG_CACHES_WARM_UP)
               && AbstractApplication.getApplication().isRunning() && !iPartsFrameworkMain.isSkipDBValidation();
    }

    /**
     * Wärmt alle relevanten Caches vor nach dem Neustart der WebApp bzw. dem Löschen aller Caches.
     *
     * @param globalCachesCleared Flag, ob vorher die globalen Caches gelöscht wurden
     */
    public void warmUpCaches(boolean globalCachesCleared) {
        if (isWarmUpCaches()) {
            Logger.log(LogChannels.APPLICATION, LogType.INFO, "iPartsPlugin.warmUpCaches");
            final String userName = iPartsUserAdminDb.getUserNameForLogging(getProject());

            if (globalCachesCleared) {
                synchronized (warmUpCachesRequestedSyncObject) {
                    if (!warmUpCachesRequested) {
                        warmUpCachesRequested = true;
                    } else {
                        Logger.log(LOG_CHANNEL_PERFORMANCE, LogType.INFO, "Skipped multiple warming up global caches started by "
                                                                          + userName + "...");
                        return;
                    }
                }
            }

            final EtkProject project = mqProject;
            mqSession.startChildThread(new FrameworkRunnable() {

                private long lastCacheWarmUpTime;

                @Override
                public void run(FrameworkThread thread) {
                    // Warten bis der EtkDbs sowie Enums-Cache geladen wurden
                    long startTime = System.currentTimeMillis();
                    while (((mqProject.getEtkDbs().getCache() == null) || (mqProject.getEtkDbs().getEnums() == null))
                           && (System.currentTimeMillis() - startTime < MAX_WAIT_TIME_FOR_CACHE_PROVIDER)) {
                        if (Java1_1_Utils.sleep(10)) {
                            return; // Thread wurde abgebrochen -> kein WarmUp mehr notwendig
                        }
                    }

                    synchronized (warmUpCachesSyncObject) { // Sicherstellen, dass nicht mehrere WarmUps parallel stattfinden
                        try {
                            if (globalCachesCleared) {
                                synchronized (warmUpCachesRequestedSyncObject) {
                                    warmUpCachesRequested = false;
                                }
                            }

                            Logger.log(LOG_CHANNEL_PERFORMANCE, LogType.INFO, "Warming up " + (globalCachesCleared ? "global " : "")
                                                                              + "caches started by " + userName + "...");
                            startTime = System.currentTimeMillis();
                            lastCacheWarmUpTime = startTime;
                            assertProjectDbIsActive(project, "Caches warm up", iPartsPlugin.LOG_CHANNEL_DEBUG);
                            iPartsProduct.warmUpCache(project); // Alle Produkte laden
                            logWarmingUpCacheFinished("iPartsProduct");
                            iPartsProductModels.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsProductModels");
                            iPartsModel.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsModel");
                            iPartsModelBuildingCode.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsModelBuildingCode");
                            iPartsOmittedParts.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsOmittedParts");
                            iPartsFactories.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsFactories");
                            iPartsFactoryModel.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsFactoryModel");
                            iPartsES1.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsES1");
                            iPartsAggCodeMappingCache.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsAggCodeMappingCache");
                            iPartsVehicleToAggregateCodeCache.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsVehicleToAggregateCodeCache");
                            iPartsCodeMappingCache.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsCodeMappingCache");
                            iPartsResponseData.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsResponseData");
                            iPartsResponseSpikes.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsResponseSpikes");
                            iPartsSpringMapping.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsSpringMapping");
                            iPartsVINModelMappingCache.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsVINModelMappingCache");
                            iPartsBranchProductClassCache.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsBranchProductClassCache");
                            iPartsNote.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsNote");
                            iPartsDIALOGSeriesValidityCache.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsDIALOGSeriesValidityCache");
                            iPartsAccAndAsCodeCache.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsAccAndAsCodeCache");
                            iPartsConstructionKits.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsConstructionKits");
                            iPartsWireHarness.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsWireHarness");
                            iPartsWireHarnessSimplifiedParts.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsWireHarnessSimplifiedParts");
                            iPartsCustomProperty.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsCustomProperty");
                            if (isEditPluginActive) {
                                OpsScopeCache.warmUpCache(project);
                                logWarmingUpCacheFinished("OpsScopeCache");
                                ModuleConstructionCache.warmUpCache(project);
                                logWarmingUpCacheFinished("ModuleConstructionCache");
                                SubModuleConstructionCache.warmUpCache(project);
                                logWarmingUpCacheFinished("SubModuleConstructionCache");
                                KgTuTemplate.warmUpCache(project);
                                logWarmingUpCacheFinished("KgTuTemplate");
                                MBSStructure.warmUpCache(project);
                                logWarmingUpCacheFinished("MBSStructure");
                                MBSTextEntryCache.warmUpCache(project);
                                logWarmingUpCacheFinished("MBSTextEntryCache");
                                iPartsGenVoTextsCache.warmUpCache(project);
                                logWarmingUpCacheFinished("iPartsGenVoTextsCache");
                            }
                            iPartsStructure.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsStructure");
                            DictHashtagTextsCache.warmUpCache(project);
                            logWarmingUpCacheFinished("DictHashtagTextsCache");
                            iPartsPRIMUSReplacementsCache.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsPRIMUSReplacementsCache");
                            iPartsStandardFootNotesCache.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsStandardFootNotesCache");
                            iPartsPartFootnotesCache.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsPartFootnotesCache");
                            iPartsDIALOGFootNotesCache.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsDIALOGFootNotesCache");
                            iPartsChangeSetInfoDefinitions.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsChangeSetInfoDefinitions");
                            DictTxtKindIdByMADId.warmUpCache(project);
                            logWarmingUpCacheFinished("DictTxtKindIdByMADId");
                            iPartsCountryValidSeriesCache.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsCountryValidSeriesCache");
                            iPartsCountryInvalidPartsCache.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsCountryInvalidPartsCache");
                            iPartsDataAssortmentClassesMappingCache.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsDataAssortmentClassesMappingCache");
                            iPartsDataAssortmentPermissionsMappingCache.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsDataAssortmentPermissionsMappingCache");
                            iPartsPEMPseudoDateCache.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsPEMPseudoDateCache");
                            iPartsDataScopeKgMappingCache.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsDataScopeKgMappingCache");
                            iPartsAggTypeMappingCache.warmUpCache(project);
                            logWarmingUpCacheFinished("iPartsAggTypeMappingCache");

                            String timeDurationString = DateUtils.formatTimeDurationString(System.currentTimeMillis() - startTime, true,
                                                                                           false, Language.EN.getCode());
                            Logger.log(LOG_CHANNEL_PERFORMANCE, LogType.INFO, "Warming up " + (globalCachesCleared ? "global " : "")
                                                                              + "caches (started by " + userName
                                                                              + ") finished in " + timeDurationString);
                        } catch (Exception e) {
                            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                        }
                    }
                }

                private void logWarmingUpCacheFinished(String cacheName) {
                    if (Logger.getLogger().isChannelActive(LOG_CHANNEL_PERFORMANCE)) {
                        long currentTime = System.currentTimeMillis();
                        String timeDurationString = DateUtils.formatTimeDurationString(currentTime - lastCacheWarmUpTime, true,
                                                                                       false, Language.EN.getCode());
                        Logger.log(LOG_CHANNEL_PERFORMANCE, LogType.DEBUG, "Warming up cache " + cacheName + " finished in "
                                                                           + timeDurationString);
                        lastCacheWarmUpTime = currentTime;
                    }
                }
            });
        }
    }

    /**
     * Macht aus dem {@link LdapUser} der aktuellen Session ein {@link GuiLabel}.
     *
     * @return
     */
    private GuiLabel createStatusBarInfoForUser() {
        GuiLabel label = new GuiLabel() {
            @Override
            public void languageChanged() {
                super.languageChanged();
                setStatusBarInfoForUser(this);
            }
        };

        label.setName("ldapUserLabel");
        label.__internal_setGenerationDpi(96);
        label.setScaleForResolution(true);
        label.setForegroundColor(JavaViewerApplication.statusForeground.getColor());
        setStatusBarInfoForUser(label);
        return label;
    }

    private void setStatusBarInfoForUser(GuiLabel label) {
        String statusBarInfoForUserText;
        LdapUser ldapUser = getLdapUserForSession();
        if (ldapUser != null) {
            statusBarInfoForUserText = ldapUser.getStatusText();
        } else { // Effektiven Benutzer (kann auch Gast-Benutzer sein) auch bei deaktiviertem LDAP anzeigen
            statusBarInfoForUserText = TranslationHandler.translate("!!Effektiver Benutzer (ohne LDAP): %1", iPartsUserAdminDb.getLoginUserName());
        }

        // Organisationsname und PKW/Van bzw. Truck/Bus-Eigenschaften anzeigen
        String organisationAndPropertiesText;
        try {
            OrganisationDbObject organisationDbObject = iPartsUserAdminDb.getLoginActiveOrganisationDbForSession(iPartsUserAdminDb.get().getConnectionPool(false));
            organisationAndPropertiesText = TranslationHandler.translate(organisationDbObject.getOrganisationName()) + ": ";
        } catch (SQLException e) {
            organisationAndPropertiesText = "";
            Logger.logExceptionWithoutThrowing(LOG_CHANNEL_USER_ADMIN, LogType.ERROR, e);
        }

        boolean carAndVan = iPartsRight.checkCarAndVanInSession();
        if (carAndVan) {
            organisationAndPropertiesText += TranslationHandler.translate("!!PKW/Van");
        }
        boolean truckAndBus = iPartsRight.checkTruckAndBusInSession();
        if (truckAndBus) {
            if (carAndVan) {
                organisationAndPropertiesText += " & ";
            }
            organisationAndPropertiesText += TranslationHandler.translate("!!Truck/Bus");
        } else if (!carAndVan) {
            organisationAndPropertiesText += TranslationHandler.translate("!!Fehlendes Recht zur Anzeige der meisten Daten");
        }

        label.setText(statusBarInfoForUserText + " - " + organisationAndPropertiesText);
    }

    @Override
    public boolean sessionStarting(Session session) {
        // Während der Wartung die Benutzerverwaltung nicht verwenden
        if (AbstractApplication.isInMaintenanceMode()) {
            return false;
        }

        // Anwendungsname setzen, da beim Start einer neuen Session dieser Name immer auf den Standardnamen zurückgesetzt
        // wird und evtl. Dialoge ohne JavaViewerMainWindow in diesem Fall nicht den korrekten Titel anzeigen würden
        Constants.OFFICIAL_APP_NAME = getProgramName();

        if (!session.canHandleGui()) {
            // Sessions ohne GUI benötigen keinen echten Login -> einfach als Gast in der Root-Organisation einloggen
            fakeUserLogin(session, iPartsUserAdminDb.USER_ID_GUEST, iPartsUserAdminDb.APP_ID);
            return false;
        }

        boolean isAdminMode = session.getStartParameter().getParameterBoolean(Constants.FRAMEWORK_PARAMETER_ADMIN, false);
        if (!pluginConfig.getConfigValueAsBoolean(CONFIG_USER_ADMIN_ENABLED)) {
            if (isAdminMode) {
                // Admin-Modus ohne aktive Benutzerverwaltung muss intern den SU verwenden -> einfach als SU in der Root-Organisation einloggen
                fakeUserLogin(session, UserDbObject.CONST_USER_ID_SUPERUSER, AppDbObject.CONST_APP_ID_USER_ADMIN);
                return false;
            } else {
                MessageDialog.showError("!!Login nicht möglich, da die Benutzerverwaltung deaktiviert ist.", "!!Login");
                return true;
            }
        }

        if (!iPartsUserAdminDb.isActive()) {
            MessageDialog.showError("!!Login nicht möglich, da die Benutzerverwaltung noch nicht konfiguriert ist.", "!!Login");
            return !isAdminMode; // Der Admin-Modus soll weiterhin aufrufbar sein -> Session nicht abbrechen
        }

        String userName = "";
        if (J2EEHandler.isJ2EE() && getPluginConfig().getConfigValueAsBoolean(CONFIG_LDAP_AUTH_ACTIVE)) {
            // LDAP Benutzer in der Session speichern
            LDAPHelper ldapHelper = LDAPHelper.getInstance();
            String externalUserId = ldapHelper.extractLdapUserFromStartRequest(session.getStartParameter());
            if (StrUtils.isValid(externalUserId)) {
                userName = externalUserId;
            }
            LdapUser ldapUser = ldapHelper.getiPartsLdapUserFromDirectory(externalUserId, getPluginConfig().getConfigValueAsString(CONFIG_LDAP_IPARTS_ROLE),
                                                                          null);
            session.setAttribute(SESSION_KEY_LDAP_USER, ldapUser);
            if (ldapHelper.checkExternalLdapUser(ldapUser, externalUserId)) {
                String userId = ldapUser.getUid();
                if (StrUtils.isValid(userId)) {
                    Logger.log(LOG_CHANNEL_USER_ADMIN, LogType.DEBUG, "Login with LDAP user id \"" + userId + "\"");
                    return iPartsUserAdminDb.login(userId) == null;
                }
            } else if (StrUtils.isValid(ldapUser.getUid())) {
                ldapUser.setStatusText("!!Benutzer \"%1\" besitzt nicht die nötigen iParts-Rollen.", ldapUser.getUid());
                // Eigentlich ist das hier auch gleichzeitig die externalUserId. Sollte Daimler aber unter irgendwelchen
                // Umständen das Such-Attribut ändern, würden wir hier weiterhin die eindeutige uid erhalten.
                userName = ldapUser.getUid();
            }
        }

        // Fallback auf den Benutzernamen aus dem Cookie bzw. Startparameter (falls erlaubt)
        if (StrUtils.isEmpty(userName)) {
            userName = FrameworkUtils.getUserName();
        }
        if (isAdminMode) {
            if (getPluginConfig().getConfigValueAsBoolean(CONFIG_LOGIN_NEEDED_FOR_ADMIN_MODE)) {
                // Nur Level DEBUG, weil es im ITR normalerweise keinen Siteminder-Token und damit auch keinen LDAP-Benutzer gibt
                Logger.log(LOG_CHANNEL_USER_ADMIN, LogType.DEBUG, "Fallback login for admin mode with user name \"" + userName
                                                                  + "\" because of missing LDAP user");
            }
        } else {
            if (getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_LDAP_USER_REQUIRED_FOR_LOGIN)) {
                Logger.log(LOG_CHANNEL_USER_ADMIN, LogType.ERROR, "No fallback login for user name \"" + userName + "\" because of missing LDAP user");
                MessageDialog.showError(TranslationHandler.translate("!!Kein LDAP-Benutzer vorhanden im Siteminder-Token.", userName),
                                        "!!Login");
                return true;
            }

            Logger.log(LOG_CHANNEL_USER_ADMIN, LogType.INFO, "Fallback login with user name \"" + userName + "\" because of missing LDAP user");
        }
        return iPartsUserAdminDb.login(userName) == null;
    }

    private void fakeUserLogin(Session session, String userId, String appId) {
        session.setAttribute(UserAdminDbActions.SESSION_KEY_LOGIN_USER, new UserObject(userId));
        session.setAttribute(UserAdminDbActions.SESSION_KEY_LOGIN_APP_ID, appId);
        session.setAttribute(UserAdminDbActions.SESSION_KEY_LOGIN_ACTIVE_ORGANISATION_ID, OrganisationDbObject.CONST_ORGANISATION_ID_ROOT_SIMPLE);
        iPartsUserAdminDb.setUserIdInBrowserInfo(userId);
    }

    @Override
    public void sessionStarted(Session session) {
    }

    @Override
    public boolean sessionStartFailed(Throwable throwable) {
        return false;
    }

    @Override
    public List<AbstractGuiControl> getAdditionalControlsForStatusBar() {
        List<AbstractGuiControl> additionalStatusBarControls = new DwList<>();
        additionalStatusBarControls.add(createStatusBarInfoForUser());
        return additionalStatusBarControls;
    }

    @Override
    public String getModifiedUsername(StartParameter originalStartParameters) {
        LdapUser ldapUser = getLdapUserForSession();
        if (ldapUser != null) {
            String userId = ldapUser.getUid();
            if (StrUtils.isValid(userId)) {
                return userId;
            }
        }

        return null;
    }

    @Override
    public EnumSet<RelatedInfoDisplayOption> modifyRelatedInfoNotesOptions(EnumSet<RelatedInfoDisplayOption> options) {
        // WORKBENCH zu den Sichtbarkeits-Optionen für die Notizen in der Related Info hinzufügen
        EnumSet<RelatedInfoDisplayOption> optionsWithWorkbench = EnumSet.copyOf(options);
        optionsWithWorkbench.add(RelatedInfoDisplayOption.WORKBENCH);
        return optionsWithWorkbench;
    }

    @Override
    public List<EtkNoteKey> modifyValidNoteKeys(List<EtkNoteKey> validNoteKeys) {
        List<EtkNoteKey> modifiedValidNoteKeys = new DwList<>(validNoteKeys);

        Iterator<EtkNoteKey> validNoteKeysIterator = modifiedValidNoteKeys.iterator();
        while (validNoteKeysIterator.hasNext()) {
            EtkNoteKey noteKey = validNoteKeysIterator.next();

            // Notiz-Typ Stückliste bei virtuellen Stücklisten entfernen (nur Material ist gültig)
            if (noteKey.getTyp().equals(EtkConfigConst.NOTIZ_KATALOG) && new iPartsAssemblyId(noteKey.getAssembly(),
                                                                                              noteKey.getVersion()).isVirtual()) {
                validNoteKeysIterator.remove();
            }
        }

        return modifiedValidNoteKeys;
    }

    @Override
    public EtkNoteKey modifyNoteKeyForLink(EtkNoteKey noteKeyForLink) {
        // Virtuelle Materialien in virtuelle Stücklisteneinträge konvertieren
        if (noteKeyForLink.getTyp().equals(EtkConfigConst.NOTIZ_MATERIAL) && new iPartsPartId(noteKeyForLink.getAssembly(),
                                                                                              noteKeyForLink.getVersion()).isVirtual()) {
            return new EtkNoteKey(EtkConfigConst.NOTIZ_KATALOG, noteKeyForLink.getAssembly(), noteKeyForLink.getVersion(),
                                  noteKeyForLink.getLanguage(), "", noteKeyForLink.getDVer());
        }
        return noteKeyForLink;
    }

    @Override
    public String getLinkStringForNoteKey(EtkNoteKey noteKeyForLink) {
        noteKeyForLink = modifyNoteKeyForLink(noteKeyForLink);
        if (noteKeyForLink.getTyp().equals(EtkConfigConst.NOTIZ_KATALOG)) {
            iPartsAssemblyId assemblyId = new iPartsAssemblyId(noteKeyForLink.getAssembly(), noteKeyForLink.getVersion());
            if (assemblyId.isVirtual()) {
                EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getProject(), assemblyId);
                if (noteKeyForLink.getKLfdNr().isEmpty()) {
                    return assembly.getHeading1(-1, null); // Baugruppenbaum-Überschrift der virtuellen Assembly
                } else {
                    EtkDataPartListEntry partListEntry = assembly.getPartListEntryFromKLfdNrUnfiltered(noteKeyForLink.getKLfdNr());
                    if (partListEntry != null) {
                        return partListEntry.getPart().getUsageDisplayText(); // Name vom (virtuellen) Material des Stücklisteneintrags
                    } else {
                        return ""; // Stücklisteneintrag nicht gefunden
                    }
                }
            }
        }

        return null;
    }

    @Override
    public ModifyRelatedInfoNotesInterface.HasNotes hasNotes(EtkDataPartListEntry partListEntry) {
        // Notiz am Stücklisteneintrag und Notiz am Material überprüfen
        if (iPartsNote.hasNote(partListEntry.getAsId(), getProject()) || iPartsNote.hasNote(partListEntry.getPart().getAsId(), getProject())) {
            return ModifyRelatedInfoNotesInterface.HasNotes.YES;
        } else {
            return ModifyRelatedInfoNotesInterface.HasNotes.NO;
        }
    }

    @Override
    public boolean isNotesEditAllowed() {
        return iPartsRight.EDIT_MASTER_DATA.checkRightInSession();
    }

    /*=== ModifyEditControlFactoryInterface Methoden ===*/
    @Override
    public AbstractGuiControl modifyEditControlFactory(EditControlFactoryValues values, EditControlFactoryOptions options) {
        if (!options.isEdit && options.isSearch && (values.editCreateMode == EditCreateMode.ecmTableColumnFilter)) {
            if (values.field.getType() == EtkFieldType.feEnum) {
                // Bei einigen speziellen Enums soll für den Tabellenspalten Filter der Leer-Wert entfernt werden
                String currentTableAndFieldName = TableAndFieldName.make(values.tableName, values.fieldName);
                for (String specialEnum : specialEnumsWithoutBlankElement) {
                    if (specialEnum.equals(currentTableAndFieldName)) {
                        options.enumIgnoreBlankTexts = true;
                        break;
                    }
                }
                // Die Enums in den Spaltenfiltern sollen sich genauso darstellen wie in der Stückliste (Bild / Text)
                if (values.editCreateMode == EditCreateMode.ecmTableColumnFilter) {
                    options.enumIgnoreDisplayOptions = false;
                }
                // Für die virtuellen Enums im Spaltenfilter auch die Texte anzeigen, egal ob eigentlich nur das Bild
                // angezeigt werden soll
                if (TableAndFieldName.getTableName(currentTableAndFieldName).equals(TABLE_KATALOG) ||
                    TableAndFieldName.getTableName(currentTableAndFieldName).equals(TABLE_FOR_EVALUATION_RESULTS)) {
                    String prefix = VirtualFieldsUtils.VIRTUAL_FIELD_MARKER_PREFIX + iPartsDataVirtualFieldsDefinition.DA_MODEL_EVALUATION;
                    String finPrefix = VirtualFieldsUtils.VIRTUAL_FIELD_MARKER_PREFIX + iPartsDataVirtualFieldsDefinition.DA_FIN_EVALUATION;
                    if (TableAndFieldName.getFieldName(currentTableAndFieldName).startsWith(prefix)
                        || TableAndFieldName.getFieldName(currentTableAndFieldName).startsWith(finPrefix)
                        || TableAndFieldName.getFieldName(currentTableAndFieldName).equals(iPartsDataVirtualFieldsDefinition.DA_QUALITY_CHECK_ERROR)) {
                        options.enumIgnoreDisplayOptions = true;
                        options.enumIgnoreBlankTexts = true;
                    }
                }
                // Tabellenfilter soll Enums als SetOfEnum darstellen
                return EditControlFactory.createEnumCheckBoxForTableColumnFilter(values, options);
            } else if (values.field.getType() == EtkFieldType.feDate) {
                EditExtControlCalendar calendar = new EditExtControlCalendar();
                EditControlFactory.setDefaultLayout(calendar);
                calendar.init(values.project.getConfig(), values.tableName, values.fieldName, values.dbLanguage);
                if (!values.initValue.isEmpty()) {
                    calendar.setDate(values.initValue);
                }
                return calendar;
            } else if (values.field.getType() == EtkFieldType.feDateTime) {
                EditExtControlDateTimeEditPanel dateTimeEditPanel = new EditExtControlDateTimeEditPanel();
                EditControlFactory.setDefaultLayout(dateTimeEditPanel);
                dateTimeEditPanel.init(values.project.getConfig(), values.tableName, values.fieldName, values.dbLanguage);
                if (!values.initValue.isEmpty()) {
                    dateTimeEditPanel.setDateTime(values.initValue);
                }
                return dateTimeEditPanel;
            }
        }
        return null;
    }

    @Override
    public boolean modifyTableColumnFilterControl(int column, EditControlFactory editControl, AbstractJavaViewerFormIConnector connector) {
        if (connector instanceof AssemblyListFormIConnector) {
            AssemblyListFormIConnector listFormConnector = (AssemblyListFormIConnector)connector;
            EtkFieldType fieldType = editControl.getField().getType();
            if (editControl.isHandleAsSetOfEnum() || (fieldType == EtkFieldType.feEnum) || (fieldType == EtkFieldType.feSetOfEnum)) {
                EtkDataAssembly assembly = listFormConnector.getCurrentAssembly();
                if (assembly instanceof iPartsDataAssembly) {
                    iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)assembly;
                    if (iPartsAssembly.isDialogSMConstructionAssembly()) {
                        return handleDIALOGConstructionEnums(editControl, listFormConnector, iPartsAssembly);
                    } else if (iPartsAssembly.isPartListEditable()) {
                        return handleASEnums(editControl, listFormConnector, iPartsAssembly);
                    }
                }
            }
        }
        return false;
    }

    private void handleAsAutoEnum(EditControlFactory editControl, AssemblyListFormIConnector connector, iPartsDataAssembly assembly) {
        Set<String> valueSet = new TreeSet<>();
        EtkFieldType fieldType = editControl.getField().getType();
        for (EtkDataPartListEntry partListEntry : assembly.getPartList(assembly.getEbene())) {
            String value = partListEntry.getFieldValue(editControl.getFieldName());
            if ((fieldType == EtkFieldType.feSetOfEnum) && !editControl.isHandleAsSetOfEnum()) {
                valueSet.addAll(SetOfEnumDataType.parseSetofEnum(value, true, false));
            } else {
                valueSet.add(value);
            }
        }
        AbstractGuiTableColumnFilterFactory.setColumnTableFilterValuesForEnum(editControl, valueSet, connector.getProject());
    }

    private boolean handleASEnums(EditControlFactory editControl, AssemblyListFormIConnector connector, iPartsDataAssembly assembly) {
        String fieldName = editControl.getFieldName();
        if (fieldName.equals(FIELD_K_AA)) {
            // AS-Stückliste: hole AA's aus Product-Baumuster
            iPartsProductId productIdFromModuleUsage = assembly.getProductIdFromModuleUsage();
            if (productIdFromModuleUsage != null) {
                iPartsProduct product = iPartsProduct.getInstance(connector.getProject(), productIdFromModuleUsage);
                Set<String> modelNumbers = product.getModelNumbers(connector.getProject());
                Set<String> valueSet = new TreeSet<>();
                for (String modelNumber : modelNumbers) {
                    iPartsModel model = iPartsModel.getInstance(connector.getProject(), new iPartsModelId(modelNumber));
                    //Achtung correctEnumValues() braucht Tokens ohne SetOfEnumTags
                    valueSet.add(model.getAusfuehrungsArt());
                }
                if (!valueSet.isEmpty()) {
                    correctEnumValues(editControl, connector, valueSet);
                    return true;
                }
            }
        } else if (fieldName.equals(FIELD_K_EVENT_FROM) || fieldName.equals(FIELD_K_EVENT_TO)) {
            handleAsAutoEnum(editControl, connector, assembly);
            return true;
        }
        return false;
    }

    private boolean handleDIALOGConstructionEnums(EditControlFactory editControl, AssemblyListFormIConnector connector,
                                                  iPartsDataAssembly assembly) {
        String fieldName = editControl.getField().getName();
        if (fieldName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA) || fieldName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA_SOE)) {
            // DIALOG Konstruktion: hole AA's aus X4E
            HmMSmId hmMSmId = null;
            List<iPartsVirtualNode> virtualNodes = assembly.getVirtualNodesPath();
            if ((virtualNodes != null) && iPartsVirtualNode.isHmMSmNode(virtualNodes)) {
                hmMSmId = (HmMSmId)virtualNodes.get(1).getId();
            }
            if (hmMSmId != null) {
                // Liste der gültigen AA's aus X4E (DA_SERIES_CODES) holen
                Collection<String> list = iPartsDialogSeries.getInstance(connector.getProject(), new iPartsSeriesId(hmMSmId.getSeries())).getValidAAForSeries(connector.getProject());
                if (!list.isEmpty()) {
                    //Achtung correctEnumValues() braucht Tokens ohne SetOfEnumTags
                    Set<String> valueSet = new TreeSet<>(list);
                    correctEnumValues(editControl, connector, valueSet);
                    return true;
                }
            }
        } else if (fieldName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_EVENT_FROM) || fieldName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_EVENT_TO)) {
            handleAsAutoEnum(editControl, connector, assembly);
            return true;
        } else if (fieldName.equals(iPartsDataVirtualFieldsDefinition.DD_CALCULATED_DOCU_RELEVANT)) {
            // Beim Spaltenfilter soll der Wert "nicht definiert" nicht mehr angezeigt werden, weil dieser für das
            // berechnete Feld nicht vorkommen kann, und den Anwender verwirrt
            AbstractGuiControl control = editControl.getControl();
            if (control instanceof RComboBox) {
                RComboBox comboBox = (RComboBox)(control);
                int index = comboBox.getIndexOfItem(iPartsDocuRelevant.DOCU_RELEVANT_NOT_SPECIFIED.getDisplayValue(getProject()));
                if (index > -1) {
                    comboBox.removeItem(index);
                }
            } else if (control instanceof EnumComboBox) {
                EnumComboBox enumComboBox = (EnumComboBox)control;
                int index = enumComboBox.getIndexOfItem(iPartsDocuRelevant.DOCU_RELEVANT_NOT_SPECIFIED.getDisplayValue(getProject()));
                if (index > -1) {
                    enumComboBox.removeItem(index);
                }
            }
        }
        return false;
    }

    /**
     * @param editControl
     * @param connector
     * @param valueSet    enumValues ohne SetOfEnumTags, da sonst Dupletten entstehen durch getTokenList
     */
    private void correctEnumValues(EditControlFactory editControl, AssemblyListFormIConnector connector, Set<String> valueSet) {
        if (!valueSet.isEmpty()) {
            AbstractGuiControl control = editControl.getControl();
            if (control instanceof EnumCheckComboBox) {
                EnumCheckComboBox comboBox = (EnumCheckComboBox)control;
                valueSet.addAll(comboBox.getTokenList(""));
                comboBox.removeAllItems();
                if (!valueSet.isEmpty()) {
                    EtkDbs etkDbs = connector.getEtkDbs();
                    String enumKey = etkDbs.getEnum(TableAndFieldName.make(editControl.getTableName(), editControl.getField().getName()));
                    for (String token : valueSet) {
                        // Versuchen, eine Benennung für das Token zu ermitteln mit Fallback auf den Token selbst
                        String tokenValue = SetOfEnumDataType.getSetOfEnumToken(token);
                        String enumText = etkDbs.getEnums().getEnumText(enumKey, tokenValue, connector.getProject().getDBLanguage(),
                                                                        tokenValue, getProject(), true);
                        comboBox.addItem(token, enumText);
                        comboBox.getTokens().add(SetOfEnumDataType.getSetOfEnumTag(token));
                    }
                }
                comboBox.setActToken(editControl.getInitialValue());
            } else if (control instanceof EnumCheckRComboBox) {
                EnumCheckRComboBox comboBox = (EnumCheckRComboBox)control;
                valueSet.addAll(comboBox.getTokenList(""));
                comboBox.removeAllItems();
                if (!valueSet.isEmpty()) {
                    EtkDbs etkDbs = connector.getEtkDbs();
                    String enumKey = etkDbs.getEnum(TableAndFieldName.make(editControl.getTableName(), editControl.getField().getName()));
                    for (String token : valueSet) {
                        // Versuchen, eine Benennung für das Token zu ermitteln mit Fallback auf den Token selbst
                        String tokenValue = SetOfEnumDataType.getSetOfEnumToken(token);
                        String enumText = etkDbs.getEnums().getEnumText(enumKey, tokenValue, connector.getProject().getDBLanguage(),
                                                                        tokenValue, getProject(), true);
                        comboBox.addItem(token, enumText);
                        comboBox.getTokens().add(SetOfEnumDataType.getSetOfEnumTag(token));
                    }
                }
                comboBox.setActToken(editControl.getInitialValue());
            }
        }
    }
    /*=== ModifyEditControlFactoryInterface Methoden End ===*/

    @Override
    public boolean hasVeto(AbstractEtkProjectEvent event, boolean firedFromOtherSession, final EtkProject project, final Session session) {
        // Wenn Daten für die Anzeige von einer anderen Session aus mittels eines DataChangedEvents geändert wurden, diese
        // nicht sofort sondern erst verzögert nach Bestätigung durch den Benutzer ausführen bei aktivem EditMode. Der ClearAllCachesEvent
        // macht effektiv gar nichts in der GUI sondern löscht nur die Caches unter der Haube -> ist hier irrelevant
        // Events von der eigenen Session sind durch eigene Edit-Aktionen ausgelöst und sollten daher sofort ausgewertet
        // werden, damit z.B. nicht unnötig viele Pseudo-Transaktionen durch verzögerte Aktionen stattfinden
        if (project.isEditModeActive() && firedFromOtherSession && (event instanceof DataChangedEvent)) {
            Boolean delayedDataChangedEvent = (Boolean)session.getAttribute(SESSION_KEY_DELAYED_DATA_CHANGED_EVENT);
            if (!Utils.objectEquals(delayedDataChangedEvent, Boolean.TRUE)) {
                session.setAttribute(SESSION_KEY_DELAYED_DATA_CHANGED_EVENT, true);

                // Control zum Aktualisieren der Ansicht zur Statusleiste hinzufügen
                final AbstractJavaViewerFormIConnector mainConnector = (AbstractJavaViewerFormIConnector)session.getAttribute(JavaViewerApplication.SESSION_KEY_MAIN_CONNECTOR);
                if (mainConnector != null) {
                    // Es muss jeweils mit Threads in der konkreten Session sowie invokeThreadSafe() gearbeitet werden,
                    // damit die GUI-Aktionen Thread-sicher in der korrekten Session durchgeführt werden inkl. TranslationHandler
                    session.invokeThreadSafeWithThread(() -> {
                        final JavaViewerMainWindow mainWindow = mainConnector.getMainWindow();
                        if (mainWindow == null) { // Session hat kein JavaViewerMainWindow (z.B. Session für Webservices)
                            return;
                        }

                        final GuiButton refreshViewButton = new GuiButton("!!Ansicht aktualisieren", true);
                        refreshViewButton.setName("refreshViewButton");
                        boolean responsiveMode = DWLayoutManager.get().isResponsiveMode();
                        refreshViewButton.setIcon(responsiveMode ? iPartsDefaultImages.edit_refresh_view_hover.getImage()
                                                                 : iPartsDefaultImages.edit_refresh_view.getImage());
                        refreshViewButton.setForegroundColor(JavaViewerApplication.statusForeground.getColor());
                        refreshViewButton.setFontStyle(responsiveMode ? DWFontStyle.SEMI_BOLD : DWFontStyle.BOLD);
                        refreshViewButton.setPaddingTop(1);

                        refreshViewButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
                            @Override
                            public void fire(Event event1) {
                                session.invokeThreadSafeWithThread(() -> {
                                    project.fireProjectEvent(new DataChangedEvent(null), false, false);

                                    // Auch im Edit geöffnete Module neu laden
                                    reloadAllModulesInEdit(mainConnector);

                                    mainWindow.removeAdditionalControlFromStatusBar(refreshViewButton);
                                    session.setAttribute(SESSION_KEY_DELAYED_DATA_CHANGED_EVENT, false);
                                });
                            }
                        });

                        mainWindow.addAdditionalControlToStatusBar(refreshViewButton, true, true);
                        StaticConnectionUpdater.updateBrowser(session);
                    });
                }
            }
            return true;
        }

        return false;
    }

    @Override
    public String addHTMLToGuiWindowHeadTag() {
        if (!Session.get().getStartParameter().getParameterBoolean(Constants.FRAMEWORK_PARAMETER_ADMIN, false)) {
            return getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_ADDITIONAL_GUIWINDOW_SCRIPT).trim();
        } else {
            return null;
        }
    }

    @Override
    public EnumSet<State> modifyButtonState(String buttonAlias, JavaViewerMainWindow mainWindow) {
        return null;
    }

    @Override
    public List<MainButtonInfo> addToolbarButtons(JavaViewerMainWindow mainWindow) {
        List<MainButtonInfo> result = new ArrayList<>();

        // ToolbarButtons nur hinzufügen, wenn das Redaktionssystem (EditMode) aktiv ist
        if (!JavaViewerApplication.getInstance().getProject().isEditModeActive() || !iPartsRight.VIEW_LOG_DATA.checkRightInSession()) {
            return result;
        }

        if (isEditPluginActive() || isImportPluginActive() || isExportPluginActive()) {
            // Button für Job-Log im Edit-, Import- oder Export-Plugin anzeigen, da diese alle Job-Logs schreiben.
            AbstractJavaViewerFormConnector mainConnector = JavaViewerApplication.getInstance().getMainConnector();
            JobsMainForm jobsMainForm = new JobsMainForm(mainConnector, mainConnector.getMainWindow());
            RButtonImages jobsImages = RButtonImages.createButtonImages(iPartsDefaultImages.btn_jobsGray, iPartsDefaultImages.btn_jobs,
                                                                        iPartsDefaultImages.btn_jobsWhite);
            MainButtonInfo jobsButtonInfo = new MainButtonInfo(jobsMainForm, jobsImages, iPartsConst.IPARTS_MAIN_TOOLBAR_BUTTON_JOBS,
                                                               EnumSet.of(MainButtonInfo.Options.SHOW_IN_VIEW_MENU),
                                                               null, "!!&Jobs");
            result.add(jobsButtonInfo);
        }
        return result;
    }

    @Override
    public boolean modifyStartParameterAfterLanguageSelection(StartParameter startParameter) {
        // Wenn sich im Startparameter k_vari ein Konstruktions-Baumuster befindet, dann dieses direkt auswählen
        String kVari = startParameter.getParameter(EtkStartparameterConst.PARAM_K_VARI, null);
        if (StrUtils.isValid(kVari)) {
            AssemblyId assemblyId = new AssemblyId(kVari, "");
            if (iPartsVirtualNode.isVirtualId(assemblyId)) {
                String modelNumber = iPartsVirtualNode.getModelNumberFromAssemblyId(assemblyId);
                if (StrUtils.isValid(modelNumber)) {
                    addConstructionModelToFilter(modelNumber, assemblyId);
                    return true;
                } else {
                    // Muss die virtuelle Navigations-ID korrigiert werden?
                    NavigationPath navigationPath = new NavigationPath();
                    navigationPath.addAssembly(new AssemblyId(kVari, ""));
                    NavigationPath correctedNavigationPath = correctNavigationPathForProductStructures(navigationPath);
                    if (!navigationPath.isLike(correctedNavigationPath)) {
                        startParameter.setParameterValue(EtkStartparameterConst.PARAM_K_VARI, correctedNavigationPath.get(0).getKVari());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean restoreFilterFromFavorite(EtkFavorite favorite) {
        NavigationPath favoriteNavigationPath = new NavigationPath();
        favoriteNavigationPath.fromVariVerPathString(favorite.getFavoriteKey());
        PartListEntryId lastPartListEntryInPath = favoriteNavigationPath.getLastPartListEntryInPath();
        if (lastPartListEntryInPath != null) {
            AssemblyId ownerAssemblyId = lastPartListEntryInPath.getOwnerAssemblyId();
            if (iPartsVirtualNode.isVirtualId(ownerAssemblyId)) {
                String modelNumber = iPartsVirtualNode.getModelNumberFromAssemblyId(ownerAssemblyId);
                return addConstructionModelToFilter(modelNumber, ownerAssemblyId);
            }
        }
        return false;
    }

    @Override
    public NavigationPath restoreNavigationPathFromFavorite(EtkFavorite favorite) {
        NavigationPath navigationPath = new NavigationPath();
        navigationPath.fromVariVerPathString(favorite.getFavoriteKey());
        return correctNavigationPathForProductStructures(navigationPath);
    }

    @Override
    public AssemblyListForm createAssemblyListForm(AssemblyListFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                   DWLayoutMode mode) {
        return new iPartsAssemblyListForm(dataConnector, parentForm);
    }

    @Override
    public AssemblyImageForm createAssemblyImageForm(AssemblyImageFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                     DWLayoutMode mode) {
        if (mode == DWLayoutMode.RESPONSIVE) {
            return new iPartsResponsiveAssemblyImageForm(dataConnector, parentForm);
        } else {
            return new iPartsAssemblyImageForm(dataConnector, parentForm);
        }
    }

    public static String getModifyRelatedInfoDisplayText(EtkProject project, EtkDataPartListEntry etkPartListEntry, String defaultText) {
        List<AbstractPlugin> plugins = PluginRegistry.getRegistry().getActivePluginsForInterface(RelatedInfoForModifyDisplayTextInterface.class);
        for (AbstractPlugin plugin : plugins) {
            if (plugin.getInternalPluginName().equals(INTERNAL_PLUGIN_NAME)) {
                RelatedInfoForModifyDisplayTextInterface relInfoInterface = (RelatedInfoForModifyDisplayTextInterface)plugin;
                return relInfoInterface.modifyRelatedInfoDisplayText(project, etkPartListEntry, defaultText);
            }
        }
        return defaultText;
    }

    @Override
    public String modifyRelatedInfoDisplayText(EtkProject project, EtkDataPartListEntry etkPartListEntry, String defaultText) {
        if ((etkPartListEntry != null) && etkPartListEntry.getAsId().isValidId()) {
            String lang = project.getDBLanguage();
            boolean isVirtualAssembly = iPartsVirtualNode.isVirtualId(etkPartListEntry.getAsId().getOwnerAssemblyId());
            StringBuilder strValues = new StringBuilder();
            if (!iPartsVirtualNode.isVirtualId(etkPartListEntry.getPart().getAsId())) {
                String matNr = iPartsNumberHelper.formatPartNo(project, etkPartListEntry.getPart().getFieldValue(iPartsDataVirtualFieldsDefinition.DA_MAPPED_MATNR));
                if (!matNr.isEmpty()) {
                    strValues.append(matNr);
                    strValues.append(RELATEDINFO_DISPLAYTEXT_DELIMITER);
                }
            }
            String desc = etkPartListEntry.getPart().getDisplayValue(iPartsConst.FIELD_M_TEXTNR, lang);
            if (!StrUtils.isValid(desc)) {
                boolean oldLogLoadFieldIfNeeded = etkPartListEntry.getPart().isLogLoadFieldIfNeeded();
                etkPartListEntry.getPart().setLogLoadFieldIfNeeded(false);
                try {
                    desc = etkPartListEntry.getPart().getDisplayValue(iPartsConst.FIELD_M_CONST_DESC, lang);
                } finally {
                    etkPartListEntry.getPart().setLogLoadFieldIfNeeded(oldLogLoadFieldIfNeeded);
                }
            }
            strValues.append(desc);
            if (!isVirtualAssembly) {
                strValues.append(RELATEDINFO_DISPLAYTEXT_DELIMITER);
                strValues.append(etkPartListEntry.getFieldValue(iPartsConst.FIELD_K_POS));
                strValues.append(RELATEDINFO_DISPLAYTEXT_DELIMITER);
                strValues.append(etkPartListEntry.getAsId().getKLfdnr());
            }

            iPartsDialogBCTEPrimaryKey bcteKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(etkPartListEntry);

            if (!isVirtualAssembly) {
                iPartsDocumentationType documentationType = EditModuleHelper.getDocumentationTypeFromPartListEntry(etkPartListEntry);
                if (documentationType.isPKWDocumentationType()) {
                    appendBcteKey(project, strValues, bcteKey, lang);
                }
            } else {
                appendBcteKey(project, strValues, bcteKey, lang);
            }
            if (strValues.length() > 0) {
                return strValues.toString();
            }
        }
        return defaultText;
    }

    private void appendBcteKey(EtkProject project, StringBuilder str, iPartsDialogBCTEPrimaryKey bcteKey, String language) {
        if (bcteKey != null) {
            str.append(RELATEDINFO_DISPLAYTEXT_DELIMITER);
            String dialogGuid = bcteKey.createDialogGUID();
            String visGuid = project.getVisObject().asString(iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_SOURCE_GUID,
                                                             dialogGuid, language);
            if (visGuid.equals(dialogGuid)) {
                // keine Visualisierung bei FIELD_K_SOURCE_GUID hinterlegt => Rückfall-Formatierung
                HmMSmId hmMSmId = bcteKey.getHmMSmId();
                str.append(hmMSmId.getSeries());
                str.append(" ");
                str.append(hmMSmId.getHm());
                str.append(hmMSmId.getM());
                str.append(hmMSmId.getSm());
                str.append(RELATEDINFO_DISPLAYTEXT_DELIMITER);
                str.append(bcteKey.getPosE());
                str.append(RELATEDINFO_DISPLAYTEXT_DELIMITER);
                str.append(bcteKey.getPosV());
            } else {
                str.append(visGuid);
            }
        }
    }

    @Override
    public void modifyRMechanicNavigationMenuActions(List<RNavigationButtonModel> models, AbstractJavaViewerForm parentForm) {
        // iParts-spezifischer FIN/VIN/BM-Filter
        models.add(new iPartsNavigationButtonModelFilter(parentForm));
    }

    @Override
    public boolean isSearchResultEntryValid(EtkSearchBaseResult searchResult) {
        if (searchResult instanceof EtkPartResult) {
            // Suchergebnis ausblenden, wenn CarPerspective-TU existiert, aber im Produkt das Flag nicht gesetzt ist
            EtkPartResult partResult = (EtkPartResult)searchResult;
            EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(partResult.getProject(), partResult.getAssemblyId());
            if (EditModuleHelper.isCarPerspectiveAssembly(assembly)) {
                if (assembly instanceof iPartsDataAssembly) {
                    iPartsProductId productId = ((iPartsDataAssembly)assembly).getProductIdFromModuleUsage();
                    if (productId != null) {
                        iPartsProduct product = iPartsProduct.getInstance(getProject(), productId);
                        return product.isCarPerspective();
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean modifyMenuItemConfigClick(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String ebeneAndSourceName,
                                             String extraConfigKeyForDisplayFields, List<EtkDisplayField> localDisplayFieldList,
                                             boolean arefixedColumnsSupported, int localFixedColumnsCount) {
        if (iPartsSimpleDoubleListSelectForm.showDoubleListForm(dataConnector, parentForm, ebeneAndSourceName, extraConfigKeyForDisplayFields,
                                                                localDisplayFieldList, arefixedColumnsSupported, localFixedColumnsCount)) {
            dataConnector.dataChanged(null);
        }
        return true;
    }
}