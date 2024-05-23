/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit;

import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.db.EtkDbs;
import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormConnector;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.AbstractJavaViewerMainFormContainer;
import de.docware.apps.etk.base.forms.common.*;
import de.docware.apps.etk.base.forms.functions.EtkFunction;
import de.docware.apps.etk.base.forms.toolbar.EtkToolbarButton;
import de.docware.apps.etk.base.forms.toolbar.ToolbarButtonAlias;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListCellContent;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListCellContentFromPlugin;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListForm;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.mechanic.mainview.connector.MechanicFormConnector;
import de.docware.apps.etk.base.mechanic.mainview.forms.MechanicFormIConnector;
import de.docware.apps.etk.base.mechanic.treeview.forms.AbstractAssemblyTreeForm;
import de.docware.apps.etk.base.mechanic.treeview.forms.AssemblyTreeFormIConnector;
import de.docware.apps.etk.base.misc.BookmarkHelper;
import de.docware.apps.etk.base.misc.EtkMainToolbarManager;
import de.docware.apps.etk.base.misc.MenuManager;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.events.*;
import de.docware.apps.etk.base.project.filter.EtkFilterItem;
import de.docware.apps.etk.base.project.filter.EtkFilterTyp;
import de.docware.apps.etk.base.project.filter.FilterMode;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.EtkHotspotLinkHelper;
import de.docware.apps.etk.base.project.mechanic.NavigationPath;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.relatedinfo.main.forms.AbstractRelatedInfoMainForm;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.EtkRelatedInfoLocation;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.base.search.connector.SearchFormIConnector;
import de.docware.apps.etk.base.search.forms.SearchBaseForm;
import de.docware.apps.etk.base.search.forms.SearchForm;
import de.docware.apps.etk.base.search.forms.SearchMechanicForm;
import de.docware.apps.etk.base.search.model.EtkPartResult;
import de.docware.apps.etk.base.search.model.EtkSearchBaseResult;
import de.docware.apps.etk.base.search.model.ModuleSearchCache;
import de.docware.apps.etk.base.viewermain.forms.JavaViewerMainWindow;
import de.docware.apps.etk.base.viewermain.forms.MainButtonInfo;
import de.docware.apps.etk.base.viewermain.forms.ToolbarControlPlacement;
import de.docware.apps.etk.plugins.AbstractJavaViewerSimpleEndpointPlugin;
import de.docware.apps.etk.plugins.EtkPluginConstants;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AbstractDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.VehicleDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst.iPartsWorkOrderCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictTransJobHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictClusterEventHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.transit.DictMetaListContainer;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.delete.TruncateTables;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.footnotes.iPartsPartFootnotesCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ldap.LDAPHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.ImageFileImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogPosTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.FinId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsResponseData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsResponseSpikes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuForProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductStructures;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.helper.iPartsEdsStructureHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsStructure;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.iPartsRelatedInfoEditContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.iPartsMQMessageManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.iPartsXMLMessageEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.iPartsXMLResponseSimulator;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.bst.iPartsWSBSTEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.nutzdok.iPartsWSConstructionKitsEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.nutzdok_remarks.iPartsWSConstructionKitsRemarksEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.nutzdok_remarks.iPartsWSDeleteConstructionKitsKemRemarkEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.nutzdok_remarks.iPartsWSDeleteConstructionKitsSaaRemarkEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.supply_to_bst_simulation.iPartsWSSupplyToBSTSimulationEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.versioninfo.iPartsWSVersionInfoBSTEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.filter.iPartsEditFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.construction.AbstractConstModelSelectionForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.dictionary.DictShowTextForTranslation;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.dictionary.DictShowTextKindForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.dictionary.DictShowTransJobs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsConstModelHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.mqtest.MQEventForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper.MbsKemDataSheetHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.iPartsEDSWorkBasketMainForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.xml.iPartsEditXMLMessageListeners;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.xml.iPartsEditXMLResponseSimulator;
import de.docware.apps.etk.plugins.interfaces.*;
import de.docware.apps.etk.plugins.utils.GridFilterReturnType;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.apps.etk.viewer.FrameworkMain;
import de.docware.apps.etk.viewer.JavaViewerApplication;
import de.docware.apps.etk.viewer.usersettings.EtkUserSettings;
import de.docware.apps.etk.viewer.webapp.deploytool.forms.BaseSettingsPanel;
import de.docware.apps.etk.viewer.webapp.deploytool.forms.EditModeSettingsPanel;
import de.docware.framework.combimodules.config_gui.ConfigurationTreeCreator;
import de.docware.framework.combimodules.config_gui.ConfigurationWindow;
import de.docware.framework.combimodules.config_gui.UniversalConfigurationPanel;
import de.docware.framework.combimodules.config_gui.defaultpanels.db.SQLToolsPanel;
import de.docware.framework.combimodules.useradmin.config.UserAdminSettingsPanelOptions;
import de.docware.framework.combimodules.useradmin.config.UserAdminTreeCreator;
import de.docware.framework.combimodules.useradmin.db.PropertyType;
import de.docware.framework.combimodules.useradmin.db.Right;
import de.docware.framework.combimodules.useradmin.news.model.NewsFilter;
import de.docware.framework.combimodules.useradmin.news.reader.legacy.NewsListReader;
import de.docware.framework.modules.config.ConfigBase;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.config.db.EtkFieldType;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.config.defaultconfig.UniversalConfigOption;
import de.docware.framework.modules.config.defaultconfig.UniversalConfiguration;
import de.docware.framework.modules.config.defaultconfig.system.SystemSettings;
import de.docware.framework.modules.config.license.LicenseConfig;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.app.AbstractApplication;
import de.docware.framework.modules.gui.app.DWLayoutManager;
import de.docware.framework.modules.gui.controls.*;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel;
import de.docware.framework.modules.gui.controls.calendar.CalendarUtils;
import de.docware.framework.modules.gui.controls.calendar.GuiCalendar;
import de.docware.framework.modules.gui.controls.filechooser.ClientModeFileChooserWindow;
import de.docware.framework.modules.gui.controls.filechooser.FileChooserPurpose;
import de.docware.framework.modules.gui.controls.filechooser.GuiFileChooserDialog;
import de.docware.framework.modules.gui.controls.menu.*;
import de.docware.framework.modules.gui.controls.misc.DWFontStyle;
import de.docware.framework.modules.gui.controls.misc.DWOrientation;
import de.docware.framework.modules.gui.controls.spinner.GuiIntSpinner;
import de.docware.framework.modules.gui.controls.toolbar.ToolButtonImages;
import de.docware.framework.modules.gui.design.DesignCategory;
import de.docware.framework.modules.gui.design.DesignImage;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerFireOnce;
import de.docware.framework.modules.gui.event.EventListenerOptions;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.LayoutFlow;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsFlow;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.framework.modules.gui.misc.endpoint.FrameworkSimpleEndpoint;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.resources.FrameworkImage;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.misc.validator.default_validators.GuiControlEndpointUriPrefixValidator;
import de.docware.framework.modules.gui.output.swing.SwingHandler;
import de.docware.framework.modules.gui.responsive.base.RButtonImages;
import de.docware.framework.modules.gui.responsive.base.actionitem.ActionItem;
import de.docware.framework.modules.gui.responsive.base.theme.MaterialTheme;
import de.docware.framework.modules.gui.responsive.base.theme.MaterialThemeIconComponent;
import de.docware.framework.modules.gui.responsive.base.theme.Theme;
import de.docware.framework.modules.gui.responsive.components.checkbox.RCheckbox;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.modules.gui.session.SessionManager;
import de.docware.framework.modules.interappcom.ClusterEventInterface;
import de.docware.framework.modules.plugins.interfaces.AddHTMLToGuiWindowInterface;
import de.docware.framework.modules.plugins.interfaces.NeedsStaticConnectionUpdatesInterface;
import de.docware.framework.modules.plugins.interfaces.SessionStartInterface;
import de.docware.framework.modules.webservice.restful.RESTfulEndpoint;
import de.docware.framework.modules.webservice.restful.jwt.JWTKeystoreManager;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.framework.utils.HTMLUtils;
import de.docware.framework.utils.VarParam;
import de.docware.framework.utils.forms.CopyTextWindow;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import de.docware.util.java1_1.Java1_1_Utils;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.misc.observer.ObserverCall;
import de.docware.util.misc.observer.ObserverCallback;
import de.docware.util.security.PasswordString;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.TableAndFieldName;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.*;

import static de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin.TABLE_FOR_EVALUATION_RESULTS;

/**
 * iParts Plug-in für das Redaktionssystem (EditMode), das iParts-spezifische Editiermöglichkeiten zur Verfügung stellt.
 */
public class iPartsEditPlugin extends AbstractJavaViewerSimpleEndpointPlugin implements iPartsConst, ModifyMainToolbarInterface, ModifyMenuInterface,
                                                                                        ModifyEditControlFactoryInterface, ModifyStartScreenInterface,
                                                                                        ModifyMainWindowInterface, ModifyPartListPopupMenuInterface,
                                                                                        ModifyTreePopUpInterface, RelatedInfoInterface,
                                                                                        NeedsStaticConnectionUpdatesInterface, ModifyPartListPanelInterface,
                                                                                        FilterInterface, ReceiveEtkProjectEventInterface,
                                                                                        SessionStartInterface, OpenAndCloseRelatedInfoInterface,
                                                                                        ModifyPartlistEntryAppearanceInterface, UserSettingsInterface,
                                                                                        AssemblyPathChangedEventInterface,
                                                                                        GetVirtualEnumLinkInterface, FireProjectEventVetoInterface,
                                                                                        AddSearchFormContextMenuItemInterface,
                                                                                        ModifyStatusBarInterface, AddHTMLToGuiWindowInterface {

    public static final String INTERNAL_PLUGIN_NAME = "iPartsEdit";
    public static final String OFFICIAL_PLUGIN_NAME = "DAIMLER iParts Edit Plug-in"; // absichtlich kein Übersetzungstext
    public static final String PLUGIN_VERSION = "1.0";

    public static final LogChannels LOG_CHANNEL_EDIT_PERFORMANCE = new LogChannels("EDIT_PERFORMANCE", true, true);

    public static final LogChannels LOG_CHANNEL_WS_DEBUG = new LogChannels("WS_DEBUG", false, true);
    public static final LogChannels LOG_CHANNEL_WS_PERFORMANCE = new LogChannels("WS_PERFORMANCE", false, true);
    public static final LogChannels LOG_CHANNEL_WS_TOKEN = new LogChannels("WS_TOKEN", false, true);
    public static final LogChannels LOG_CHANNEL_SERIES_CALC_AND_EXPORT = new LogChannels("SERIES_CALC_AND_EXPORT", true, true);
    public static final LogChannels LOG_CHANNEL_WORKBASKETS_CALC_AND_EXPORT = new LogChannels("WORKBASKET_CALC_AND_EXPORT", true, true);
    public static final LogChannels LOG_CHANNEL_PUBLISH_REMINDER_SCHEDULER = new LogChannels("PUBLISH_REMINDER_SCHEDULER", true, true);
    public static final LogChannels LOG_CHANNEL_SMARAGD = new LogChannels("SMARAGD", false, true);
    public static final LogChannels LOG_CHANNEL_SUPPLY_TO_BST = new LogChannels("SUPPLY_TO_BST", false, true);
    public static final LogChannels LOG_CHANNEL_BOM_DB_SOAP_WEBSERVICE = new LogChannels("BOM_DB_SOAP_WEBSERVICE", false, true);
    public static final LogChannels LOG_CHANNEL_NUTZDOK_CORTEX_SCHEDULER = new LogChannels("NUTZDOK_CORTEX_SCHEDULER", true, true);
    public static final LogChannels LOG_CHANNEL_QUALITY_CHECK = new LogChannels("QUALITY_CHECK", true, true);

    public static final DesignImage IMAGE_DAIMLER = new DesignImage(DesignCategory.PLUGIN, "DAIMLER", FrameworkImage.getRelativeToClass(EditDefaultImages.class, "daimler.png"), true);

    public static final Colors clPlugin_iPartsEdit_TableNotLinkedBackgroundColor = new Colors(DesignCategory.PLUGIN, "TableNotLinkedBackground",
                                                                                              Colors.clDesignErrorBackground.getColor(), true);
    public static final Colors clPlugin_iPartsEdit_HotspotNotLinkedBackgroundColor = new Colors(DesignCategory.PLUGIN, "HotspotNotLinkedBackground",
                                                                                                Colors.clSkyBlue.getColor(), true);
    public static final Colors clPlugin_iPartsEdit_SecurityNoticeLabelForegroundColor = new Colors(DesignCategory.PLUGIN, "SecurityNoticeLabelForeground",
                                                                                                   Colors.clMaroon.getColor(), true);
    public static final Colors clPlugin_iPartsEdit_DialogPositionTextBackground = new Colors(DesignCategory.PLUGIN, "DialogPositionTextBackground",
                                                                                             new Color(0xe0ffe0), true);
    public static final Colors clPlugin_iPartsEdit_DialogPositionVariantTextBackground = new Colors(DesignCategory.PLUGIN, "DialogPositionVariantTextBackground",
                                                                                                    new Color(0xA5D9FF), true);
    public static final Colors clPlugin_iPartsEdit_EdsBcsPositionTextBackground = new Colors(DesignCategory.PLUGIN, "EdsBcsPositionTextBackground",
                                                                                             new Color(0xe0ffe0), true);
    public static final Colors clPlugin_iPartsEdit_EdsBcsAdditionalTextBackground = new Colors(DesignCategory.PLUGIN, "EdsBcsAdditionalTextBackground",
                                                                                               new Color(0xA5D9FF), true);
    public static final Colors clPlugin_iPartsEdit_CttPositionTextBackground = new Colors(DesignCategory.PLUGIN, "CttPositionTextBackground",
                                                                                          new Color(0xe0ffe0), true);
    public static final Colors clPlugin_iPartsEdit_EdsKemMarkTextBackground = new Colors(DesignCategory.PLUGIN, "EdsKemMarkTextBackground",
                                                                                         new Color(0xF9F074), true);
    public static final Colors clPlugin_iPartsEdit_ColorFootnoteMarkedLine = new Colors(DesignCategory.PLUGIN, "ColorFootnoteMarkedLine",
                                                                                        Colors.clBlue.getColor(), true);
    public static final Colors clPlugin_iPartsEdit_ColorFootnoteBackgroundMarkedLine = new Colors(DesignCategory.PLUGIN, "ColorFootnoteBackgroundMarkedLine",
                                                                                                  Colors.clLtGray.getColor(), true);
    public static final Colors clPlugin_iPartsEdit_ColorFootnoteBackgroundSelectedLine = new Colors(DesignCategory.PLUGIN, "ColorFootnoteBackgroundSelectedLine",
                                                                                                    new Color(128, 255, 0), true);

    public static final Colors clPlugin_iPartsEdit_UnreadMessagesBadgeBackgroundColor = new Colors(DesignCategory.PLUGIN, "UnreadMessagesBadgeBackgroundColor",
                                                                                                   new Color(150, 150, 150), true);
    public static final Colors clPlugin_iPartsEdit_UnreadMessagesBadgeForegroundColor = new Colors(DesignCategory.PLUGIN, "UnreadMessagesBadgeForegroundColor",
                                                                                                   new Color(255, 255, 255), true);
    public static final Colors clPlugin_iPartsEdit_MultistageReplacementChainBackgroundColor = new Colors(DesignCategory.PLUGIN, "MultistageReplacementChainBackground",
                                                                                                          new Color(0xba, 0xc7, 0xd2), true);

    // Parameter für den individuellen Start Screen
    public static final UniversalConfigOption CONFIG_FORCED_STARTSCREEN = UniversalConfigOption.getBooleanOption("/forcedStartScreen", true);
    public static final UniversalConfigOption CONFIG_OPTIONAL_LANGUAGE_SELECTION = UniversalConfigOption.getBooleanOption("/languageSelectionOptional", true);

    public static final UniversalConfigOption CONFIG_SECURITY_NOTICE_RIGHT_OF_LOGO = UniversalConfigOption.getBooleanOption("/securityNoticeRight", false);

    // DIALOG Konstruktion
    public static final UniversalConfigOption CONFIG_DIALOG_HOTSPOT_HIERARCHY_CORRECTION = UniversalConfigOption.getBooleanOption("/dialogHotspotHierarchyCorrection", false);
    // EDS/BCS Konstruktion
    // Umschalten zwischen neuer und alter Struktur in EDS/BCS
    public static final UniversalConfigOption CONFIG_USE_NEW_EDS_BCS_STRUCTURE = UniversalConfigOption.getBooleanOption("/useNewEDSBCSStructure", false);
    // EDS/BCS Strukturstufe für die Stücklistenanzeige
    public static final UniversalConfigOption CONFIG_EDS_SAA_MAX_STRUCTURE_LEVEL = UniversalConfigOption.getIntegerOption("/maxEDSSAAStructureLevel", 9);
    public static final UniversalConfigOption CONFIG_EDS_SAA_DEFAULT_STRUCTURE_LEVEL = UniversalConfigOption.getIntegerOption("/defaultEDSSAAStructureLevel", 5);

    // MBS Strukturstufe für die Stücklistenanzeige
    public static final UniversalConfigOption CONFIG_MBS_MAX_STRUCTURE_LEVEL = UniversalConfigOption.getIntegerOption("/maxMBSStructureLevel", 9);
    public static final UniversalConfigOption CONFIG_MBS_DEFAULT_STRUCTURE_LEVEL = UniversalConfigOption.getIntegerOption("/defaultMBSStructureLevel", 5);

    // CTT Konstruktion
    public static final UniversalConfigOption CONFIG_CTT_MAX_STRUCTURE_LEVEL = UniversalConfigOption.getIntegerOption("/maxCTTStructureLevel", 9);
    public static final UniversalConfigOption CONFIG_CTT_DEFAULT_STRUCTURE_LEVEL = UniversalConfigOption.getIntegerOption("/defaultCTTStructureLevel", 5);

    // Qualitätsprüfung
    public static final UniversalConfigOption CONFIG_CHECK_EQUAL_FIELDS_IN_QUALITY_CHECK = UniversalConfigOption.getBooleanOption("/checkEqualFieldsInQualityCheck", true);
    public static final UniversalConfigOption CONFIG_CHECK_INVALID_ENTRIES_IN_QUALITY_CHECK = UniversalConfigOption.getBooleanOption("/checkInvalidEntriesInQualityCheck", true);
    // Qualitätsprüfung erfolgt über mehrere Threads parallel
    public static final UniversalConfigOption CONFIG_QUALITY_CHECK_PARALLEL_THREAD_COUNT = UniversalConfigOption.getIntegerOption("/qualityCheckParallelThreadCount", 4);

    // DocuBrowser
    public static final UniversalConfigOption CONFIG_ENABLE_DOCUBROWSER_DEEPLINK = UniversalConfigOption.getBooleanOption("/enableDocubrowserDeeplink", false);
    public static final UniversalConfigOption CONFIG_DOCUBROWSER_SAA_URI = UniversalConfigOption.getStringAreaOption("/docubrowserSAAUri", "https://sap-mobility.e.corpintra.net/sap/bc/ui2/flp/FioriLaunchpad.html?code=KmB49ozPYm-V9StPHWorTPVlivNGvqcxlWwAAAEt#Z_TBOM_20_DOCU_BRO-display?sap-system=TRUCKBOM_MAINT&/Detail/{{SAA}}%7C0%7C000%7CThu%2520Jan%252001%25201970%7CFri%2520Dec%252031%25209999%7Ctrue%7C0%7C{{SAA}}%7Ctrue,%7C%7C{{SAA}}%7CZ%2520540.002%252F06%7C%7C%7C%7C%7C%7C%7C%2520%7C%2520%7C%2520%7C%2520,tabMaterial");
    public static final UniversalConfigOption CONFIG_DOCUBROWSER_KEM_URI = UniversalConfigOption.getStringAreaOption("/docubrowserKEMUri", "https://sap-mobility.e.corpintra.net/sap/bc/ui2/flp/FioriLaunchpad.html?sap-client=010#Z_TBOM_20_DOCU_BRO-display?sap-system=TRUCKBOM_MAINT&/DetailPemKem/{{KEM}}%7C2%7C000%7CThu%2520Jan%252001%25201970%7CFri%2520Dec%252031%25209999%7Ctrue%7C0%7C{{KEM}}%7Cfalse,KemE");

    // Congree Web Interface
    public static final UniversalConfigOption CONFIG_CONGREE_ACTIVE = UniversalConfigOption.getBooleanOption("/congreeActive", false);
    public static final UniversalConfigOption CONFIG_CONGREE_URI = UniversalConfigOption.getStringOption("/congreeURI", "https://dlt-system-test.emea.isn.corpintra.net");
    public static final UniversalConfigOption CONFIG_CONGREE_JS_PATH = UniversalConfigOption.getStringOption("/congreeJSPath", "CWI_iParts/app/cwi-integration.js");
    public static final UniversalConfigOption CONFIG_CONGREE_USER_NAME = UniversalConfigOption.getStringOption("/congreeUserName", "");
    public static final UniversalConfigOption CONFIG_CONGREE_PASSWORD = UniversalConfigOption.getPasswordOption("/congreePassword", PasswordString.EMPTY);
    public static final UniversalConfigOption CONFIG_CONGREE_LOGIN_TYPE = UniversalConfigOption.getStringOption("/congreeLoginType", "cms");
    public static final UniversalConfigOption CONFIG_CONGREE_SYSTEMS_IDENTIFIER_MBAG = UniversalConfigOption.getStringOption("/congreeSystemsIdentifierMBAG", "iparts");
    public static final UniversalConfigOption CONFIG_CONGREE_SYSTEMS_IDENTIFIER_DTAG = UniversalConfigOption.getStringOption("/congreeSystemsIdentifierDTAG", "iparts_lkw");
    public static final UniversalConfigOption CONFIG_CONGREE_RULE_SET_MBAG_DE = UniversalConfigOption.getStringOption("/congreeRuleSetMBAG", "iParts_DE");
    public static final UniversalConfigOption CONFIG_CONGREE_RULE_SET_MBAG_EN = UniversalConfigOption.getStringOption("/congreeRuleSetMBAG_EN", "iParts_EN");
    public static final UniversalConfigOption CONFIG_CONGREE_RULE_SET_DTAG_DE = UniversalConfigOption.getStringOption("/congreeRuleSetDTAG", "iParts_LKW_DE");
    public static final UniversalConfigOption CONFIG_CONGREE_RULE_SET_DTAG_EN = UniversalConfigOption.getStringOption("/congreeRuleSetDTAG_EN", "iParts_LKW_DE"); // Default DE, weil es vermutlich kein LKW_EN gibt
    public static final UniversalConfigOption CONFIG_CONGREE_CULTURE_DE = UniversalConfigOption.getStringOption("/congreeCulture", "de-DE");
    public static final UniversalConfigOption CONFIG_CONGREE_CULTURE_EN = UniversalConfigOption.getStringOption("/congreeCulture_EN", "en-US");
    public static final UniversalConfigOption CONFIG_CONGREE_SEMANTICS_DE = UniversalConfigOption.getStringOption("/congreeSemantics", "iPartsDictionary");
    public static final UniversalConfigOption CONFIG_CONGREE_SEMANTICS_EN = UniversalConfigOption.getStringOption("/congreeSemantics_EN", "iPartsDictionary");

    // Publikation
    public static final UniversalConfigOption CONFIG_PUBLISHING_ACTIVE = UniversalConfigOption.getBooleanOption("/publishingActive", false);
    public static final UniversalConfigOption CONFIG_PUBLISHING_DAYS = UniversalConfigOption.getStringListOptionMultipleSelection("/publishingDays", new String[]{ DateUtils.DayOfWeek.SUNDAY.getDisplayName() }); // initial Sonntag
    public static final UniversalConfigOption CONFIG_PUBLISHING_TIME = UniversalConfigOption.getTimeOption("/publishingTime", new Date(0)); // initial 00:00 GMT

    // Automatischer Export
    public static final UniversalConfigOption CONFIG_AUTO_CALC_AND_EXPORT_ACTIVE = UniversalConfigOption.getBooleanOption("/autoSeriesCalcAndExportActive", false);
    public static final UniversalConfigOption CONFIG_AUTO_CALC_AND_EXPORT_DAYS = UniversalConfigOption.getStringListOptionMultipleSelection("/autoSeriesCalcAndExportDays", new String[]{ DateUtils.DayOfWeek.SUNDAY.getDisplayName() }); // initial Sonntag
    public static final UniversalConfigOption CONFIG_AUTO_CALC_AND_EXPORT_TIME = UniversalConfigOption.getTimeOption("/autoSeriesCalcAndExportTime", new Date(0)); // initial 00:00 GMT
    public static final UniversalConfigOption CONFIG_AUTO_CALC_AND_EXPORT_DIR = UniversalConfigOption.getFileOption("/autoSeriesCalcAndExportDir", new File("seriesCalculationExport"));
    public static final UniversalConfigOption CONFIG_AUTO_CALC_AND_EXPORT_THREAD_COUNT = UniversalConfigOption.getIntegerOption("/autoSeriesCalcAndExportThreadCount", 4);

    // Automatische Berechnung und Export der offenen Arbeitsvorräte
    public static final UniversalConfigOption CONFIG_AUTO_CALC_WORKBASKETS_ACTIVE = UniversalConfigOption.getBooleanOption("/autoCalcWorkbasketsActive", false);
    public static final UniversalConfigOption CONFIG_AUTO_CALC_WORKBASKETS_DAYS = UniversalConfigOption.getStringListOptionMultipleSelection("/autoCalcWorkbasketsDays", new String[]{ DateUtils.DayOfWeek.SUNDAY.getDisplayName() }); // initial Sonntag
    public static final UniversalConfigOption CONFIG_AUTO_CALC_WORKBASKETS_TIME = UniversalConfigOption.getTimeOption("/autoCalcWorkbasketsTime", new Date(0)); // initial 00:00 GMT
    public static final UniversalConfigOption CONFIG_AUTO_CALC_WORKBASKETS_DIR = UniversalConfigOption.getFileOption("/autoCalcWorkbasketsDir", new File("workbasketCalculationExport"));
    public static final UniversalConfigOption CONFIG_AUTO_CALC_WORKBASKETS_THREAD_COUNT = UniversalConfigOption.getIntegerOption("/autoCalcWorkbasketsThreadCount", 4);

    // Erinnerungsfunktion zum Veröffentlichen von Produkten und AS-Baumustern
    public static final UniversalConfigOption CONFIG_PUBLISH_PRODUCT_REMINDER_ACTIVE = UniversalConfigOption.getBooleanOption("/publishProductReminderActive", false);
    public static final UniversalConfigOption CONFIG_PUBLISH_PRODUCT_REMINDER_DAYS = UniversalConfigOption.getStringListOptionMultipleSelection("/publishProductReminderDays", new String[]{ DateUtils.DayOfWeek.SUNDAY.getDisplayName() }); // initial Sonntag
    public static final UniversalConfigOption CONFIG_PUBLISH_PRODUCT_REMINDER_TIME = UniversalConfigOption.getTimeOption("/publishProductReminderTime", new Date(0)); // initial 00:00 GMT
    public static final UniversalConfigOption CONFIG_PUBLISH_PRODUCT_REMINDER_DAYS_AFTER_SOP = UniversalConfigOption.getIntegerOption("/publishProductReminderDaysAfterSOP", 30); // 1 Monat
    public static final UniversalConfigOption CONFIG_PUBLISH_MODEL_REMINDER_ACTIVE = UniversalConfigOption.getBooleanOption("/publishModelReminderActive", false);
    public static final UniversalConfigOption CONFIG_PUBLISH_MODEL_REMINDER_DAYS = UniversalConfigOption.getStringListOptionMultipleSelection("/publishModelReminderDays", new String[]{ DateUtils.DayOfWeek.SUNDAY.getDisplayName() }); // initial Sonntag
    public static final UniversalConfigOption CONFIG_PUBLISH_MODEL_REMINDER_TIME = UniversalConfigOption.getTimeOption("/publishModelReminderTime", new Date(0)); // initial 00:00 GMT
    public static final UniversalConfigOption CONFIG_PUBLISH_MODEL_REMINDER_DAYS_AFTER_VALID_FROM = UniversalConfigOption.getIntegerOption("/publishModelReminderDaysAfterValidFrom", 365); // 1 Jahr

    public static final UniversalConfigOption CONFIG_MAILBOX_RESUBMISSION_ACTIVE = UniversalConfigOption.getBooleanOption("/mailboxResubmissionActive", false);
    public static final UniversalConfigOption CONFIG_MAILBOX_RESUBMISSION_TIME = UniversalConfigOption.getTimeOption("/mailboxResubmissionTime", new Date(3 * 60 * 60 * 1000L)); // initial 03:00 GMT

    // AS-PLM Parameter
    public static final UniversalConfigOption CONFIG_ASPLM_OPEN_MEDIA_CONTAINER_URI = UniversalConfigOption.getStringAreaOption("/ASPLMOpenMediaContainerURI", "https://asplm.es.corpintra.net/awc/#/teamcenter.search.search?searchCriteria={{MC}}&filter=POM_application_object.owning_user%3D~~Categorization.category%3DMediencontainer&refresh=true");

    // Parameter für die AS-PLM Simulation
    public static final UniversalConfigOption CONFIG_SIM_AUTO_RESPONSE_DELAY = UniversalConfigOption.getIntegerOption(iPartsPlugin.SIM_AUTO_RESPONSE_DELAY, -1);
    public static final UniversalConfigOption CONFIG_SIM_AUTO_RESPONSE_DELAY_SEARCH = UniversalConfigOption.getIntegerOption(iPartsPlugin.SIM_AUTO_RESPONSE_DELAY_SEARCH, -1);

    public static final UniversalConfigOption CONFIG_SIM_NEW_PIC_ORDER_XML = UniversalConfigOption.getBooleanOption("/simNewPicOrderXml", false);
    public static final UniversalConfigOption CONFIG_SIM_PIC_SEARCH_XML = UniversalConfigOption.getBooleanOption("/simPicSearchXml", false);
    public static final UniversalConfigOption CONFIG_SIM_PIC_PREVIEW_XML = UniversalConfigOption.getBooleanOption("/simPicPreviewXml", false);
    public static final UniversalConfigOption CONFIG_SIM_PIC_PREVIEW_DIR = UniversalConfigOption.getFileOption("/simPicPreviewDir", new File(iPartsEditXMLResponseSimulator.DEFAULT_PICTURE_PREVIEW_DIR));
    public static final UniversalConfigOption CONFIG_SIM_PIC_CONTENT_XML = UniversalConfigOption.getBooleanOption("/simPicContentXml", false);
    public static final UniversalConfigOption CONFIG_SIM_PIC_CONTENT_DIR = UniversalConfigOption.getFileOption("/simPicContentDir", new File(iPartsXMLResponseSimulator.DEFAULT_PICTURE_CONTENT_DIR));

    // Administration Share und Download des KEM Blatts
    public static final UniversalConfigOption CONFIG_KEM_DATA_SHEET = UniversalConfigOption.getBooleanOption("/kemFile", false);
    public static final UniversalConfigOption CONFIG_KEM_DATA_SHEET_CONTENT_DIR = UniversalConfigOption.getFileOption("/kemFileContentDir", new File("10_MBS-EDocu"));
    public static final UniversalConfigOption CONFIG_KEM_DATA_SHEET_YEAR_PREFIX = UniversalConfigOption.getStringOption("/kemFileYearPrefix", "Änderungsaufträge 20");

    // 2D Visualisierung
    public static final UniversalConfigOption CONFIG_2D_VIS_ACTIVE = UniversalConfigOption.getBooleanOption("/active2DVis", true);

    // Smaragd Visualisierung
    public static final UniversalConfigOption CONFIG_SMARAGD_URI = UniversalConfigOption.getStringOption("/smaragdURI", "cec://smaragd/BASE64/");
    public static final UniversalConfigOption CONFIG_SMARAGD_DELAY = UniversalConfigOption.getIntegerOption("/smaragdDelay", 1000);

    // BST Webservices Authentifizierung
    public static final UniversalConfigOption CONFIG_BST_HEADER_TOKEN_NAME = UniversalConfigOption.getStringOption("/bstHeaderTokenName", "authentication");
    public static final UniversalConfigOption CONFIG_BST_HEADER_TOKEN_TYPE = UniversalConfigOption.getStringOption("/bstHeaderTokenType", "Bearer");
    public static final UniversalConfigOption CONFIG_BST_HEADER_TOKEN_PASSWORD = UniversalConfigOption.getPasswordOption("/bstHeaderTokenPassword", PasswordString.EMPTY);
    public static final UniversalConfigOption CONFIG_BST_HEADER_TOKEN_PUB_KEY_DIR = UniversalConfigOption.getFileOption("/bstHeaderTokenPubKeyDir", new File("jwtPublicKeysBST"));
    public static final UniversalConfigOption CONFIG_BST_HEADER_TOKEN_PUB_KEY_DIR_ENFORCE_POLLING = UniversalConfigOption.getBooleanOption("/bstHeaderTokenPubKeyDirEnforcePolling", false);
    public static final UniversalConfigOption CONFIG_BST_HEADER_TOKEN_PUB_KEY_DIR_POLLING_TIME = UniversalConfigOption.getIntegerOption("/bstHeaderTokenPubKeyDirPollingTime", 10);
    public static final UniversalConfigOption CONFIG_BST_HEADER_TOKEN_EXPIRES = UniversalConfigOption.getIntegerOption("/bstHeaderTokenExpires", 60 * 60); // 1 Stunde

    // BST Webservices
    public static final UniversalConfigOption CONFIG_URI_BST_VERSION_INFO = UniversalConfigOption.getStringOption("/bstVersioninfoURI", iPartsWSVersionInfoBSTEndpoint.DEFAULT_ENDPOINT_URI);
    public static final UniversalConfigOption CONFIG_URI_BST = UniversalConfigOption.getStringOption("/bstURI", iPartsWSBSTEndpoint.DEFAULT_ENDPOINT_URI);

    // Versorgung von Autoren-Aufträgen an BST
    public static final UniversalConfigOption CONFIG_SUPPLY_AUTHOR_ORDERS_TO_BST_ACTIVE = UniversalConfigOption.getBooleanOption("/supplyAOsToBSTActive", true);
    public static final UniversalConfigOption CONFIG_URI_SUPPLY_AUTHOR_ORDERS_TO_BST = UniversalConfigOption.getStringOption("/bstURISupplyAOsToBST", "/bst/simulation");
    public static final UniversalConfigOption CONFIG_SUPPLY_AUTHOR_ORDERS_TO_BST_TIMEOUT = UniversalConfigOption.getIntegerOption("/supplyAOsToBSTTimeout", 30); // Default 30s
    public static final UniversalConfigOption CONFIG_SUPPLY_AUTHOR_ORDERS_TO_BST_SIMULATION = UniversalConfigOption.getBooleanOption("/supplyAOsToBSTSimulation", false);

    public static final UniversalConfigOption CONFIG_SUPPLY_AUTHOR_ORDERS_TO_BST_TOKEN_NAME = UniversalConfigOption.getStringOption("/supplyAOsToBSTTokenName", "Authorization"); // Authorization
    public static final UniversalConfigOption CONFIG_SUPPLY_AUTHOR_ORDERS_TO_BST_TOKEN_TYPE = UniversalConfigOption.getStringOption("/supplyAOsToBSTTokenType", "Bearer"); // Bearer
    public static final UniversalConfigOption CONFIG_SUPPLY_AUTHOR_ORDERS_TO_BST_TOKEN = UniversalConfigOption.getStringAreaOption("/supplyAOsToBSTToken", "");

    // NutzDok Webservices Authentifizierung
    public static final UniversalConfigOption CONFIG_NUTZDOK_HEADER_TOKEN_NAME = UniversalConfigOption.getStringOption("/nutzdokHeaderTokenName", "authentication");
    public static final UniversalConfigOption CONFIG_NUTZDOK_HEADER_TOKEN_TYPE = UniversalConfigOption.getStringOption("/nutzdokHeaderTokenType", "Bearer");
    public static final UniversalConfigOption CONFIG_NUTZDOK_HEADER_TOKEN_PASSWORD = UniversalConfigOption.getPasswordOption("/nutzdokHeaderTokenPassword", PasswordString.EMPTY);
    public static final UniversalConfigOption CONFIG_NUTZDOK_HEADER_TOKEN_PUB_KEY_DIR = UniversalConfigOption.getFileOption("/nutzdokHeaderTokenPubKeyDir", new File("jwtPublicKeysNutzDok"));
    public static final UniversalConfigOption CONFIG_NUTZDOK_HEADER_TOKEN_PUB_KEY_DIR_ENFORCE_POLLING = UniversalConfigOption.getBooleanOption("/nutzdokHeaderTokenPubKeyDirEnforcePolling", false);
    public static final UniversalConfigOption CONFIG_NUTZDOK_HEADER_TOKEN_PUB_KEY_DIR_POLLING_TIME = UniversalConfigOption.getIntegerOption("/nutzdokHeaderTokenPubKeyDirPollingTime", 10);
    public static final UniversalConfigOption CONFIG_NUTZDOK_HEADER_TOKEN_EXPIRES = UniversalConfigOption.getIntegerOption("/nutzdokHeaderTokenExpires", 60 * 60); // 1 Stunde

    // NutzDok Webservices
    public static final UniversalConfigOption CONFIG_URI_NUTZDOK_CONSTRUCTION_KITS = UniversalConfigOption.getStringOption("/nutzdokConstructionKitsURI", iPartsWSConstructionKitsEndpoint.DEFAULT_ENDPOINT_URI);
    public static final UniversalConfigOption CONFIG_URI_NUTZDOK_CONSTRUCTION_KITS_REMARKS = UniversalConfigOption.getStringOption("/nutzdokConstructionKitsRemarksURI", iPartsWSConstructionKitsRemarksEndpoint.DEFAULT_ENDPOINT_URI);

    // Scheduler für NutzDok Cortex-Aktivierung
    public static final UniversalConfigOption CONFIG_NUTZDOK_CORTEX_ACTIVE = UniversalConfigOption.getBooleanOption("/nutzdokCortexActive", false);
    public static final UniversalConfigOption CONFIG_NUTZDOK_CORTEX_DAYS = UniversalConfigOption.getStringListOptionMultipleSelection("/nutzdokCortexDays", new String[]{ DateUtils.DayOfWeek.SUNDAY.getDisplayName() }); // initial Sonntag
    public static final UniversalConfigOption CONFIG_NUTZDOK_CORTEX_TIME = UniversalConfigOption.getTimeOption("/nutzdokCortexTime", new Date(0)); // initial 00:00 GMT

    // BOM-DB: KEM-Daten SOAP Webservice
    public static final UniversalConfigOption CONFIG_BOMDB_WEBSERVICE_STATUS = UniversalConfigOption.getBooleanOption("/bomDbWebserviceActive", false);
    public static final UniversalConfigOption CONFIG_BOMDB_WEBSERVICE_ENDPOINT_URI = UniversalConfigOption.getStringOption("/bomDbWebserviceEndpointURI", "https://engbus-int.app.corpintra.net/soap/EngBus_BomDbService");
    public static final UniversalConfigOption CONFIG_BOMDB_WEBSERVICE_USERNAME = UniversalConfigOption.getStringOption("/bomDbWebserviceUsername", "EtU_iParts");
    public static final UniversalConfigOption CONFIG_BOMDB_WEBSERVICE_PASSWORD = UniversalConfigOption.getPasswordOption("/bomDbWebservicePassword", PasswordString.EMPTY);
    public static final UniversalConfigOption CONFIG_BOMDB_WEBSERVICE_APPTOKEN = UniversalConfigOption.getStringAreaOption("/bomDbWebserviceAppToken", "");
    public static final UniversalConfigOption CONFIG_PURGE_DATABASE_TARGET_COMPANY = UniversalConfigOption.getStringListOptionSingleSelection("/purgeDatabaseTargetCompany", "");

    public static final String SESSION_KEY_EDITING = "iparts_editing"; // Es wird gerade editiert
    public static final String SESSION_KEY_DELAYED_EVENTS_DURING_EDIT = "iparts_delayed_events_during_edit"; // Map von Event-Klassen auf Events, die erst nach dem Edit verzögert ausgeführt werden sollen

    private static final String[] KNOWN_CODE_FIELDS = { TableAndFieldName.make(TABLE_KATALOG, FIELD_K_CODES),
                                                        TableAndFieldName.make(TABLE_KATALOG, FIELD_K_CODES_REDUCED),
                                                        TableAndFieldName.make(TABLE_KATALOG, FIELD_K_CODES_CONST),
                                                        TableAndFieldName.make(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.RETAIL_CODES_REDUCED),
                                                        TableAndFieldName.make(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.RETAIL_CODES_FILTERED),
                                                        TableAndFieldName.make(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.RETAIL_CODES_WITH_EVENTS),
                                                        TableAndFieldName.make(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_CODES), // für Konstruktion DIALOG
                                                        TableAndFieldName.make(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.MEU_SUB_MODULE_CODE),  // für Konstruktion EDS
                                                        TableAndFieldName.make(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.MBS_CODE),  // für Konstruktion MBS
                                                        TableAndFieldName.make(TABLE_DA_DIALOG, FIELD_DD_CODES),
                                                        TableAndFieldName.make(TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_CODE),
                                                        TableAndFieldName.make(TABLE_DA_EDS_MODEL, FIELD_EDS_MODEL_CODE),
                                                        TableAndFieldName.make(TABLE_DA_VS2US_RELATION, FIELD_VUR_CODES),
                                                        TableAndFieldName.make(TABLE_DA_FACTORY_DATA, FIELD_DFD_CRN),
                                                        TableAndFieldName.make(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_CODE),
                                                        TableAndFieldName.make(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_CODE_AS),
                                                        TableAndFieldName.make(TABLE_DA_DIALOG_ADD_DATA, FIELD_DAD_CODE),
                                                        TableAndFieldName.make(TABLE_DA_MODEL, FIELD_DM_CODE),
                                                        TableAndFieldName.make(TABLE_IMAGES, FIELD_I_CODES),
                                                        TableAndFieldName.make(TABLE_DA_BAD_CODE, FIELD_DBC_CODE_ID),
                                                        TableAndFieldName.make(TABLE_DA_SERIES_CODES, FIELD_DSC_CODES),
                                                        TableAndFieldName.make(TABLE_DA_SERIES_EVENTS, FIELD_DSE_CODES),
                                                        TableAndFieldName.make(TABLE_DA_PICORDER, FIELD_DA_PO_CODES),
                                                        TableAndFieldName.make(TABLE_DA_MODULES_EINPAS, FIELD_DME_CODE_VALIDITY) };
    private static final String[] KNOWN_EDITABLE_STATUS_FIELDS = { TableAndFieldName.make(TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_STATUS),
                                                                   TableAndFieldName.make(TABLE_DA_DIALOG, FIELD_DD_STATUS),
                                                                   TableAndFieldName.make(TABLE_DA_FACTORY_DATA, FIELD_DFD_STATUS),
                                                                   TableAndFieldName.make(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_STATUS),
                                                                   TableAndFieldName.make(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_STATUS),
                                                                   TableAndFieldName.make(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_STATUS),
                                                                   TableAndFieldName.make(TABLE_DA_RESPONSE_DATA, FIELD_DRD_STATUS),
                                                                   TableAndFieldName.make(TABLE_DA_RESPONSE_SPIKES, FIELD_DRS_STATUS) };
    // Kenner, dass eine Übernahme neuer Texte in den Übersetzungsumfang läuft
    private static final String KEY_TRANSLATIONS_RUNNING = "iParts_translations_running";
    private static final int MAX_ROWS_PER_PAGE_LIMIT = 500;
    private static UniversalConfiguration pluginConfig;
    private static List<iPartsMQMessageManager> mqXMLMessageManagers;
    private static JWTKeystoreManager keystoreManagerBST;
    private static JWTKeystoreManager keystoreManagerNutzDok;
    private static iPartsSeriesAutoCalcAndExport autoCalcAndExportHelper;
    private static iPartsWorkbasketCalcAndExport workbasketCalcAndExportHelper;
    private static iPartsPublishProductReminderScheduler publishProductReminderScheduler;
    private static iPartsPublishModelReminderScheduler publishModelReminderScheduler;
    private static iPartsNutzDokCortexImportScheduler singleNutzDokCortexImportScheduler;
    private static iPartsMailboxResubmissionScheduler mailboxResubmissionScheduler;
    private static int maxStructureLevelForEDSBCSConstPartList;
    private static int maxStructureLevelForMBSConstPartList;
    private static int maxStructureLevelForCTTConstPartList;

    private String host;
    private int port;

    public static UniversalConfiguration getPluginConfig() {
        return pluginConfig;
    }

    /**
     * Startet das Editieren in der aktuellen {@link Session} (z.B. in der RelatedEdit).
     *
     * @return Konnte das Editieren gestartet werden?
     */
    public static boolean startEditing() {
        return startEditing(Session.get());
    }

    /**
     * Startet das Editieren in der übergebenen {@link Session} (z.B. in der RelatedEdit).
     *
     * @return Konnte das Editieren gestartet werden?
     */
    public static boolean startEditing(Session session) {
        if ((session == null) || !session.isActive()) {
            return true;
        }

        // Paralleles Bearbeiten verhindern
        if (isEditing(session)) {
            MessageDialog.show("!!Es werden bereits in einem anderen Fenster Daten bearbeitet. Bitte den anderen Vorgang zuerst abschließen, da paralleles Bearbeiten nicht möglich ist.",
                               "!!Parallele Bearbeitung nicht möglich");
            return false;
        }

        session.setAttribute(SESSION_KEY_EDITING, Boolean.TRUE);
        return true;
    }

    /**
     * Beendet das Editieren in der aktuellen {@link Session} (z.B. in der RelatedEdit).
     *
     * @return
     */
    public static void stopEditing() {
        stopEditing(Session.get());
    }

    /**
     * Beendet das Editieren in der übergebenen {@link Session} (z.B. in der RelatedEdit).
     *
     * @return
     */
    public static void stopEditing(Session session) {
        if ((session != null) && session.isActive()) {
            session.setAttribute(SESSION_KEY_EDITING, Boolean.FALSE);

            // Verzögerte Events jetzt ausführen
            Map<Class<? extends AbstractEtkProjectEvent>, AbstractEtkProjectEvent> delayedEventsMap = (Map)session.getAttribute(SESSION_KEY_DELAYED_EVENTS_DURING_EDIT);
            if (delayedEventsMap != null) {
                EtkProject project = (EtkProject)session.getAttribute(JavaViewerApplication.SESSION_KEY_PROJECT);
                if (project != null) {
                    for (AbstractEtkProjectEvent event : delayedEventsMap.values()) {
                        project.fireProjectEvent(event, false, false);
                    }
                }
                session.removeAttribute(SESSION_KEY_DELAYED_EVENTS_DURING_EDIT);
            }
        }
    }

    /**
     * Wird in der aktuellen {@link Session} gerade editiert (z.B. in der RelatedEdit).
     *
     * @return
     */
    public static boolean isEditing() {
        return isEditing(Session.get());
    }

    /**
     * Wird in der übergebenen {@link Session} gerade editiert (z.B. in der RelatedEdit).
     *
     * @return
     */
    public static boolean isEditing(Session session) {
        if ((session == null) || !session.isActive()) {
            return false;
        }

        return Utils.objectEquals(session.getAttribute(SESSION_KEY_EDITING), Boolean.TRUE);
    }

    /**
     * Lädt alle Module mit den übergebenen {@link AssemblyId}s innerhalb von "Modul bearbeiten" neu, falls diese gerade
     * im Edit geöffnet sind. Über <code>considerAllModules</code> können auch Module neu geladen werden, die nicht im
     * Edit-Modus sind.
     *
     * @param assemblyIds
     * @param connector
     */
    public static void reloadModulesInEdit(Collection<AssemblyId> assemblyIds, AbstractJavaViewerFormIConnector connector,
                                           boolean considerAllModules) {
        if (assemblyIds.isEmpty()) {
            return;
        }

        List<AbstractJavaViewerMainFormContainer> editModuleForms = connector.getMainWindow().getFormsFromClass(EditModuleForm.class);
        if (!editModuleForms.isEmpty()) {
            EditModuleForm editModuleForm = (EditModuleForm)editModuleForms.get(0);
            Collection<EditModuleForm.EditModuleInfo> editModuleInfos = editModuleForm.getEditModuleInfoList(considerAllModules);
            for (EditModuleForm.EditModuleInfo editModuleInfo : editModuleInfos) {
                if (assemblyIds.contains(editModuleInfo.getAssemblyId())) {
                    editModuleForm.reloadModule(editModuleInfo);
                }
            }
        }
    }

    /**
     * Lädt alle Module mit den übergebenen {@link AssemblyId}s innerhalb von "Modul bearbeiten" neu, falls diese gerade
     * im Edit geöffnet sind.
     *
     * @param assemblyIds
     * @param connector
     */
    public static void reloadModulesInEdit(Collection<AssemblyId> assemblyIds, AbstractJavaViewerFormIConnector connector) {
        reloadModulesInEdit(assemblyIds, connector, false);
    }

    /**
     * Lädt alle Module innerhalb von "Modul bearbeiten" neu.
     *
     * @param connector
     */
    public static void reloadAllModulesInEdit(AbstractJavaViewerFormIConnector connector) {
        List<AbstractJavaViewerMainFormContainer> editModuleForms = connector.getMainWindow().getFormsFromClass(EditModuleForm.class);
        if (!editModuleForms.isEmpty()) {
            EditModuleForm editModuleForm = (EditModuleForm)editModuleForms.get(0);
            Collection<EditModuleForm.EditModuleInfo> editModuleInfos = editModuleForm.getEditModuleInfoList(true);
            for (EditModuleForm.EditModuleInfo editModuleInfo : editModuleInfos) {
                editModuleForm.reloadModule(editModuleInfo);
            }
        }
    }

    public static boolean isMaterialEditable() {
        return iPartsRight.EDIT_VEHICLE_PARTS_DATA.checkRightInSession();
    }

    public static boolean isModuleMasterDataEditable() {
        return iPartsRight.EDIT_PARTS_DATA.checkRightInSession();
    }

    public static void restartPublishingThread() {
        if (iPartsPlugin.getMqProject() == null) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_PUBLISHING, LogType.ERROR, "EtkProject for iPartsEditPlugin.restartPublishingThread() is null");
            return;
        }

        try {
            iPartsPlugin.getMqProject().getEtkDbs().runCheckDbConnectionQuery();
        } catch (Exception e) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_PUBLISHING, LogType.ERROR, "Check database connection for project failed while restarting publishing thread: " + e);
        }
        if (pluginConfig.getConfigValueAsBoolean(CONFIG_PUBLISHING_ACTIVE) && (iPartsPlugin.getPublishingHelper() != null)) {
            iPartsPlugin.getPublishingHelper().startPublishingThread(DateUtils.DayOfWeek.getDaysOfWeek(pluginConfig.getConfigValueAsStringArray(CONFIG_PUBLISHING_DAYS)),
                                                                     pluginConfig.getConfigValueAsTime(CONFIG_PUBLISHING_TIME));
        }
    }

    public static void restartAutoCalcAndExportThread() {
        iPartsPlugin.restartTimerThread(autoCalcAndExportHelper, pluginConfig, CONFIG_AUTO_CALC_AND_EXPORT_ACTIVE,
                                        CONFIG_AUTO_CALC_AND_EXPORT_DAYS, CONFIG_AUTO_CALC_AND_EXPORT_TIME,
                                        LOG_CHANNEL_SERIES_CALC_AND_EXPORT);
    }

    public static void restartWorkbasketsCalcAndExportThread() {
        iPartsPlugin.restartTimerThread(workbasketCalcAndExportHelper, pluginConfig, CONFIG_AUTO_CALC_WORKBASKETS_ACTIVE,
                                        CONFIG_AUTO_CALC_WORKBASKETS_DAYS, CONFIG_AUTO_CALC_WORKBASKETS_TIME,
                                        LOG_CHANNEL_WORKBASKETS_CALC_AND_EXPORT);
    }

    public static void restartPublishReminderSchedulerThreads() {
        iPartsPlugin.restartTimerThread(publishProductReminderScheduler, pluginConfig, CONFIG_PUBLISH_PRODUCT_REMINDER_ACTIVE,
                                        CONFIG_PUBLISH_PRODUCT_REMINDER_DAYS, CONFIG_PUBLISH_PRODUCT_REMINDER_TIME,
                                        LOG_CHANNEL_PUBLISH_REMINDER_SCHEDULER);
        iPartsPlugin.restartTimerThread(publishModelReminderScheduler, pluginConfig, CONFIG_PUBLISH_MODEL_REMINDER_ACTIVE,
                                        CONFIG_PUBLISH_MODEL_REMINDER_DAYS, CONFIG_PUBLISH_MODEL_REMINDER_TIME,
                                        LOG_CHANNEL_PUBLISH_REMINDER_SCHEDULER);
    }

    public static void restartMailboxResubmissionThread() {
        iPartsPlugin.restartTimerThread(mailboxResubmissionScheduler, pluginConfig, CONFIG_MAILBOX_RESUBMISSION_ACTIVE,
                                        null, CONFIG_MAILBOX_RESUBMISSION_TIME, iPartsPlugin.LOG_CHANNEL_MAILBOX);
    }

    /**
     * Liefert den {@link JWTKeystoreManager} für die BST Webservices zurück.
     *
     * @return
     */
    public static JWTKeystoreManager getKeystoreManagerBST() {
        return keystoreManagerBST;
    }

    /**
     * Liefert den {@link JWTKeystoreManager} für die NutzDok Webservices zurück.
     *
     * @return
     */
    public static JWTKeystoreManager getKeystoreManagerNutzDok() {
        return keystoreManagerNutzDok;
    }


    /**
     * Feuert einen {@link FilterChangedEvent} und lädt die Anzeige neu nachdem ein Edit-Filter für die Stückliste verändert wurde.
     *
     * @param dataConnector
     */
    public static void assemblyListEditFilterChanged(AbstractJavaViewerFormIConnector dataConnector) {
        // Innerhalb von einer RelatedInfo über den OwnerConnector an den MechanicFormConnector rankommen
        if (dataConnector instanceof RelatedInfoFormIConnector) {
            dataConnector = dataConnector.getOwnerConnector();
        }

        if (dataConnector instanceof MechanicFormConnector) {
            // Lösche erst die gecachten Stücklistenpositionen
            ((MechanicFormConnector)dataConnector).getCurrentAssembly().clearFilteredPartLists();

            // FilterChangedEvent feuern
            dataConnector.getProject().fireProjectEvent(new FilterChangedEvent());

            // Ansicht aktualisieren
            for (AbstractJavaViewerForm viewForm : dataConnector.getConnectedViews()) {
                if (viewForm instanceof AssemblyListForm) {
                    // Finde und aktualisiere die AssemblyListForm falls suppressAutomaticDelayedUpdates gesetzt ist (andernfalls
                    // wird das AssemblyListForm bereits durch den FilterChangedEvent aktualisiert)
                    AssemblyListForm assemblyListeForm = (AssemblyListForm)viewForm;
                    if (assemblyListeForm.isSuppressAutomaticDelayedUpdates()) {
                        assemblyListeForm.updateAssemblyWhileShowingLabel();
                    }
                } else if (viewForm instanceof iPartsMechanicFormWindow) {
                    // Dadurch wird der "Fenster aktualisieren"-Button wieder unsichtbar gemacht, der durch den
                    // FilterChangedEvent sichtbar wurde
                    viewForm.updateView();
                }
            }
        }
    }

    public static void initConfigurationSettingsVariables() {
        if (pluginConfig != null) {
            maxStructureLevelForEDSBCSConstPartList = getMaxStructureLevelForConstPartList(CONFIG_EDS_SAA_MAX_STRUCTURE_LEVEL);
            maxStructureLevelForMBSConstPartList = getMaxStructureLevelForConstPartList(CONFIG_MBS_MAX_STRUCTURE_LEVEL);
            maxStructureLevelForCTTConstPartList = getMaxStructureLevelForConstPartList(CONFIG_CTT_MAX_STRUCTURE_LEVEL);
        }
    }

    public iPartsEditPlugin() {
    }

    /**
     * Konstruktor für die Unit-Tests.
     *
     * @param host
     * @param port
     */
    public iPartsEditPlugin(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static boolean isAutoCalcAndExportActive() {
        return getPluginConfig().getConfigValueAsBoolean(CONFIG_AUTO_CALC_AND_EXPORT_ACTIVE);
    }

    public static DWFile getDirForAutoCalcAndExport() {
        if (isAutoCalcAndExportActive()) {
            return iPartsPlugin.getDirForConfigOption(getPluginConfig(), CONFIG_AUTO_CALC_AND_EXPORT_DIR, LOG_CHANNEL_SERIES_CALC_AND_EXPORT);
        }
        return null;
    }

    public static boolean isWorkbasketsCalcAndExportActive() {
        return getPluginConfig().getConfigValueAsBoolean(CONFIG_AUTO_CALC_WORKBASKETS_ACTIVE);
    }

    public static DWFile getDirForWorkbasketsCalcAndExport() {
        if (isWorkbasketsCalcAndExportActive()) {
            return iPartsPlugin.getDirForConfigOption(getPluginConfig(), CONFIG_AUTO_CALC_WORKBASKETS_DIR, LOG_CHANNEL_WORKBASKETS_CALC_AND_EXPORT);
        }
        return null;
    }

    public static void restartNutzDokCortexImportThread() {
        iPartsPlugin.restartTimerThread(singleNutzDokCortexImportScheduler, pluginConfig, CONFIG_NUTZDOK_CORTEX_ACTIVE,
                                        CONFIG_NUTZDOK_CORTEX_DAYS, CONFIG_NUTZDOK_CORTEX_TIME,
                                        LOG_CHANNEL_NUTZDOK_CORTEX_SCHEDULER);
    }

    public static boolean isNutzDokCortexImportActive() {
        return pluginConfig.getConfigValueAsBoolean(CONFIG_NUTZDOK_CORTEX_ACTIVE);
    }

    public static boolean isDocuBrowserActive() {
        return pluginConfig.getConfigValueAsBoolean(iPartsEditPlugin.CONFIG_ENABLE_DOCUBROWSER_DEEPLINK);
    }

    public static String getDocuBrowserSaaUriTemplate() {
        return pluginConfig.getConfigValueAsString(iPartsEditPlugin.CONFIG_DOCUBROWSER_SAA_URI);
    }

    public static String getDocuBrowseKemUriTemplate() {
        return pluginConfig.getConfigValueAsString(iPartsEditPlugin.CONFIG_DOCUBROWSER_KEM_URI);
    }

    public static boolean isQualityCheckParallelActive() {
        return getQualityCheckThreadCount() > 1;
    }

    public static int getQualityCheckThreadCount() {
        return getPluginConfig().getConfigValueAsInteger(CONFIG_QUALITY_CHECK_PARALLEL_THREAD_COUNT);
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
        UniversalConfigurationPanel configurationPanel = new UniversalConfigurationPanel(host, pluginConfig, OFFICIAL_PLUGIN_NAME, true);
        configurationPanel.addBooleanOption(CONFIG_FORCED_STARTSCREEN, "!!Splash Screen zwingend anzeigen", false);
        configurationPanel.addBooleanOption(CONFIG_OPTIONAL_LANGUAGE_SELECTION, "!!Sprachauswahl optional anzeigen", false);
        configurationPanel.addSeparator();
        configurationPanel.addBooleanOption(CONFIG_SECURITY_NOTICE_RIGHT_OF_LOGO, "!!Vertraulichkeitshinweis rechts vom Logo", false);

        // DIALOG Konstruktion
        configurationPanel.addSeparator();
        configurationPanel.startGroup("!!DIALOG Konstruktion");
        configurationPanel.addBooleanOption(CONFIG_DIALOG_HOTSPOT_HIERARCHY_CORRECTION, "!!HotSpot- und AS-Strukturstufenkorrektur anwenden", false);
        configurationPanel.endGroup();

        // EDS SAA Strukturstufe
        configurationPanel.addSeparator();
        configurationPanel.startGroup("!!EDS/BCS Konstruktion");
        configurationPanel.addBooleanOption(CONFIG_USE_NEW_EDS_BCS_STRUCTURE, "!!Neue Struktur aus TB.f verwenden", false);
        addStructureLevelOption(configurationPanel, CONFIG_EDS_SAA_MAX_STRUCTURE_LEVEL,
                                CONFIG_EDS_SAA_DEFAULT_STRUCTURE_LEVEL, "!!Maximale EDS SAA-Strukturstufe", "!!Standard EDS SAA-Strukturstufe");
        configurationPanel.endGroup();
        // MBS Strukturstufe
        configurationPanel.startGroup("!!SAP.MBS Konstruktion");
        addStructureLevelOption(configurationPanel, CONFIG_MBS_MAX_STRUCTURE_LEVEL,
                                CONFIG_MBS_DEFAULT_STRUCTURE_LEVEL, "!!Maximale MBS-Strukturstufe", "!!Standard MBS-Strukturstufe");
        configurationPanel.endGroup();
        configurationPanel.addSeparator();

        // CTT Strukturstufe
        configurationPanel.startGroup("!!SAP.CTT Konstruktion");
        addStructureLevelOption(configurationPanel, CONFIG_CTT_MAX_STRUCTURE_LEVEL,
                                CONFIG_CTT_DEFAULT_STRUCTURE_LEVEL, "!!Maximale CTT-Strukturstufe", "!!Standard CTT-Strukturstufe");
        configurationPanel.endGroup();
        configurationPanel.addSeparator();

        configurationPanel.startGroup("!!Qualitätsprüfung");
        configurationPanel.addBooleanOption(CONFIG_CHECK_EQUAL_FIELDS_IN_QUALITY_CHECK, "!!Prüfung von vererbten Attributen in der Qualitätsprüfung", false);
        configurationPanel.addBooleanOption(CONFIG_CHECK_INVALID_ENTRIES_IN_QUALITY_CHECK, "!!Prüfung von ungültigen DIALOG-Teilepositionen in der Qualitätsprüfung", false);
        // Qualitätsprüfung über mehrere Threads parallel
        configurationPanel.addIntegerSpinnerOption(CONFIG_QUALITY_CHECK_PARALLEL_THREAD_COUNT, "!!Maximale Anzahl an parallelen Berechnungs-Threads",
                                                   true, 1, 128, 1);
        configurationPanel.endGroup();

        // Congree Web Interface
        configurationPanel.startGroup("!!Congree Web Interface Integration");
        configurationPanel.addBooleanOption(CONFIG_CONGREE_ACTIVE, "!!Aktiv", false);
        configurationPanel.addStringOption(CONFIG_CONGREE_URI, "!!Basis-URI", false);
        configurationPanel.addStringOption(CONFIG_CONGREE_JS_PATH, "!!JavaScript-Pfad", false);
        configurationPanel.addStringOption(CONFIG_CONGREE_USER_NAME, "!!Benutzername", false).setTooltip("!!Falls leer, wird die Benutzer-ID vom eingeloggten Benutzer verwendet");
        configurationPanel.addPasswordOption(CONFIG_CONGREE_PASSWORD, "!!Passwort", false);
        configurationPanel.addStringOption(CONFIG_CONGREE_LOGIN_TYPE, "!!Login-Typ", false);
        configurationPanel.addStringOption(CONFIG_CONGREE_SYSTEMS_IDENTIFIER_MBAG, "!!System-ID PKW/Van", false);
        configurationPanel.addStringOption(CONFIG_CONGREE_SYSTEMS_IDENTIFIER_DTAG, "!!System-ID Truck/Bus", false);
        configurationPanel.addStringOption(CONFIG_CONGREE_RULE_SET_MBAG_DE, TranslationHandler.translate("!!Regel-Set PKW/Van") + " DE", false);
        configurationPanel.addStringOption(CONFIG_CONGREE_RULE_SET_MBAG_EN, TranslationHandler.translate("!!Regel-Set PKW/Van") + " EN", false);
        configurationPanel.addStringOption(CONFIG_CONGREE_RULE_SET_DTAG_DE, TranslationHandler.translate("!!Regel-Set Truck/Bus") + " DE", false);
        configurationPanel.addStringOption(CONFIG_CONGREE_RULE_SET_DTAG_EN, TranslationHandler.translate("!!Regel-Set Truck/Bus") + " EN", false);
        configurationPanel.addStringOption(CONFIG_CONGREE_CULTURE_DE, TranslationHandler.translate("!!Kultur (Sprache und Land)") + " DE", false);
        configurationPanel.addStringOption(CONFIG_CONGREE_CULTURE_EN, TranslationHandler.translate("!!Kultur (Sprache und Land)") + " EN", false);
        configurationPanel.addStringOption(CONFIG_CONGREE_SEMANTICS_DE, TranslationHandler.translate("!!Semantik") + " DE", false);
        configurationPanel.addStringOption(CONFIG_CONGREE_SEMANTICS_EN, TranslationHandler.translate("!!Semantik") + " EN", false);
        configurationPanel.endGroup();

        // Publikation
        iPartsPlugin.createDayOfWeekTimerGroup(configurationPanel, "!!Automatisches Zurücksetzen der Retail-Caches (Publikation)",
                                               CONFIG_PUBLISHING_ACTIVE, CONFIG_PUBLISHING_DAYS, CONFIG_PUBLISHING_TIME, true);

        // Automatischer Export
        iPartsPlugin.createDayOfWeekTimerGroup(configurationPanel, "!!Automatische Berechnung und Bereitstellung offener Stände",
                                               CONFIG_AUTO_CALC_AND_EXPORT_ACTIVE, CONFIG_AUTO_CALC_AND_EXPORT_DAYS, CONFIG_AUTO_CALC_AND_EXPORT_TIME, false);
        configurationPanel.addFileOption(CONFIG_AUTO_CALC_AND_EXPORT_DIR, "!!Verzeichnis für berechnete Baureihen", true,
                                         GuiFileChooserDialog.FILE_MODE_DIRECTORIES, FileChooserPurpose.SAVE, "!!Auswählen");
        configurationPanel.addIntegerSpinnerOption(CONFIG_AUTO_CALC_AND_EXPORT_THREAD_COUNT, "!!Maximale Anzahl an Berechnungs-Threads",
                                                   true, 1, 128, 1);
        configurationPanel.endGroup();

        // Automatische Berechnung Arbeitsvorrat
        iPartsPlugin.createDayOfWeekTimerGroup(configurationPanel, "!!Automatische Berechnung und Bereitstellung offener Arbeitsvorräte",
                                               CONFIG_AUTO_CALC_WORKBASKETS_ACTIVE, CONFIG_AUTO_CALC_WORKBASKETS_DAYS,
                                               CONFIG_AUTO_CALC_WORKBASKETS_TIME, false);
        configurationPanel.addFileOption(CONFIG_AUTO_CALC_WORKBASKETS_DIR, "!!Verzeichnis für berechnete Arbeitsvorräte", true,
                                         GuiFileChooserDialog.FILE_MODE_DIRECTORIES, FileChooserPurpose.SAVE, "!!Auswählen");
        configurationPanel.addIntegerSpinnerOption(CONFIG_AUTO_CALC_WORKBASKETS_THREAD_COUNT, "!!Maximale Anzahl an Berechnungs-Threads",
                                                   true, 1, 128, 1);
        configurationPanel.endGroup();

        // Erinnerungsfunktion zum Veröffentlichen von Produkten
        iPartsPlugin.createDayOfWeekTimerGroup(configurationPanel, "!!Erinnerungsfunktion zum Veröffentlichen von Produkten",
                                               CONFIG_PUBLISH_PRODUCT_REMINDER_ACTIVE, CONFIG_PUBLISH_PRODUCT_REMINDER_DAYS,
                                               CONFIG_PUBLISH_PRODUCT_REMINDER_TIME, false);
        configurationPanel.addIntegerSpinnerOption(CONFIG_PUBLISH_PRODUCT_REMINDER_DAYS_AFTER_SOP, "!!Tage nach \"SOP\"",
                                                   true, 1, 9999, 1);
        configurationPanel.endGroup();

        // Erinnerungsfunktion zum Veröffentlichen von AS-Baumustern
        iPartsPlugin.createDayOfWeekTimerGroup(configurationPanel, "!!Erinnerungsfunktion zum Veröffentlichen von AS-Baumustern",
                                               CONFIG_PUBLISH_MODEL_REMINDER_ACTIVE, CONFIG_PUBLISH_MODEL_REMINDER_DAYS,
                                               CONFIG_PUBLISH_MODEL_REMINDER_TIME, false);
        configurationPanel.addIntegerSpinnerOption(CONFIG_PUBLISH_MODEL_REMINDER_DAYS_AFTER_VALID_FROM, "!!Tage nach \"Gültig ab\"",
                                                   true, 1, 9999, 1);
        configurationPanel.endGroup();

        // Wiedervorlage von Nachrichten
        iPartsPlugin.createDayOfWeekTimerGroup(configurationPanel, "!!Wiedervorlage von Nachrichten (täglich)",
                                               CONFIG_MAILBOX_RESUBMISSION_ACTIVE, null,
                                               CONFIG_MAILBOX_RESUBMISSION_TIME, true);

        // AS-PLM
        configurationPanel.startGroup("!!AS-PLM");
        GuiTextArea asplmOpenMCTextField = configurationPanel.addStringAreaOption(CONFIG_ASPLM_OPEN_MEDIA_CONTAINER_URI,
                                                                                  "!!URI für das Öffnen eines Mediencontainers",
                                                                                  false, true);
        asplmOpenMCTextField.setMaximumWidth(500);
        asplmOpenMCTextField.setMinimumHeight(50);
        asplmOpenMCTextField.setMaximumHeight(50);
        asplmOpenMCTextField.setLineWrap(true);
        asplmOpenMCTextField.setTooltip("!!Platzhalter {{MC}} für den Mediencontainer");
        configurationPanel.endGroup();

        // AS-PLM Simulation
        configurationPanel.startGroup("!!AS-PLM Simulation");
        configurationPanel.addIntegerSpinnerOption(CONFIG_SIM_AUTO_RESPONSE_DELAY, "!!Automatische Antwort nach x Sekunden",
                                                   false, -1, 1000, 1).setTooltip("!!-1 für keine automatische Antwort");
        configurationPanel.addIntegerSpinnerOption(CONFIG_SIM_AUTO_RESPONSE_DELAY_SEARCH, "!!Automatische Antwort nach x Sekunden für Suchen",
                                                   false, -1, 1000, 1).setTooltip("!!-1 für keine automatische Antwort");
        configurationPanel.addSection("!!Neuer Bildauftrag", null);
        configurationPanel.addBooleanOption(CONFIG_SIM_NEW_PIC_ORDER_XML, "!!Antwort-XML erzeugen", false);
        configurationPanel.addSection("!!Bildrecherche", null);
        configurationPanel.addBooleanOption(CONFIG_SIM_PIC_SEARCH_XML, "!!Antwort-XML erzeugen", false);
        configurationPanel.addSection("!!Bildvorschau", null);
        configurationPanel.addBooleanOption(CONFIG_SIM_PIC_PREVIEW_XML, "!!Antwort-XML erzeugen", false);
        configurationPanel.addFileOption(CONFIG_SIM_PIC_PREVIEW_DIR, "!!Ordner mit Vorschaubildern", false, GuiFileChooserDialog.FILE_MODE_DIRECTORIES,
                                         FileChooserPurpose.OPEN, null);
        configurationPanel.addSection("!!Bildübernahme", null);
        configurationPanel.addBooleanOption(CONFIG_SIM_PIC_CONTENT_XML, "!!Antwort-XML erzeugen", false);
        configurationPanel.addFileOption(CONFIG_SIM_PIC_CONTENT_DIR, "!!Ordner mit neuen Bildern", false, GuiFileChooserDialog.FILE_MODE_DIRECTORIES,
                                         FileChooserPurpose.OPEN, null);
        configurationPanel.endGroup();

        // Administration Share und Download des KEM Blatts
        configurationPanel.startGroup("!!MBS-KEM Blatt");
        configurationPanel.addBooleanOption(CONFIG_KEM_DATA_SHEET, "!!MBS-KEM Blatt aktivieren", false);
        configurationPanel.addStringOption(CONFIG_KEM_DATA_SHEET_YEAR_PREFIX, "!!KEM Jahr Verzeichnis Präfix", false);
        configurationPanel.addFileOption(CONFIG_KEM_DATA_SHEET_CONTENT_DIR, "!!Rootverzeichnis", false, GuiFileChooserDialog.FILE_MODE_DIRECTORIES,
                                         FileChooserPurpose.OPEN, null);
        configurationPanel.endGroup();

        // 2D Visualisierung
        configurationPanel.startGroup("!!2D-Visualisierung");
        configurationPanel.addBooleanOption(CONFIG_2D_VIS_ACTIVE, "!!Aktiv", false);
        configurationPanel.endGroup();

        // Smaragd 3D Visualisierung
        configurationPanel.startGroup("!!3D-Visualisierung in Smaragd");
        String configFieldName = "!!Basis URI für Smaragd Visualisierung";
        configurationPanel.addStringOption(CONFIG_SMARAGD_URI, configFieldName, false).
                setValidator(new GuiControlEndpointUriPrefixValidator(configFieldName, true));
        configurationPanel.addIntegerSpinnerOption(CONFIG_SMARAGD_DELAY, "!!Wartezeit zwischen zwei Aufrufen in Millisekunden", false, 0, 50000, 100);
        configurationPanel.endGroup();

        // BST Webservices Authentifizierung
        configurationPanel.startGroup("!!BST Webservice-Token im Request-Header");
        configurationPanel.addStringOption(CONFIG_BST_HEADER_TOKEN_NAME, "!!Name", true);
        configurationPanel.addStringOption(CONFIG_BST_HEADER_TOKEN_TYPE, "!!Typ", true);
        configurationPanel.addPasswordOption(CONFIG_BST_HEADER_TOKEN_PASSWORD, "!!Passwort (für HS256 Verfahren; wenn leer ist HS256 nicht zugelassen)", false);
        configurationPanel.addIntegerSpinnerOption(CONFIG_BST_HEADER_TOKEN_EXPIRES, "!!Erlaubte Differenz in Sekunden", true,
                                                   1, 100000, 10);
        {
            configurationPanel.startGroup("!!Überwachtes Verzeichnis für Public Key Definitionen im JSON-Format (für RS256 Verfahren)");
            configurationPanel.addFileOption(CONFIG_BST_HEADER_TOKEN_PUB_KEY_DIR, "!!Verzeichnis", false,
                                             GuiFileChooserDialog.FILE_MODE_DIRECTORIES, FileChooserPurpose.OPEN, "!!Auswählen");
            configurationPanel.addBooleanOption(CONFIG_BST_HEADER_TOKEN_PUB_KEY_DIR_ENFORCE_POLLING, "!!Polling erzwingen (nötig z.B. für NFS Shares)", false);
            configurationPanel.addIntegerSpinnerOption(CONFIG_BST_HEADER_TOKEN_PUB_KEY_DIR_POLLING_TIME, "!!Polling Intervall (in min)", false, 1, 1000, 1);
            configurationPanel.endGroup();
        }

        configurationPanel.endGroup();

        // BST Webservices
        configurationPanel.startGroup("!!BST Webservices");
        addEndpointURIConfigOption(CONFIG_URI_BST_VERSION_INFO, "VersionInfo", configurationPanel);
        addEndpointURIConfigOption(CONFIG_URI_BST, "bst", configurationPanel);
        configurationPanel.endGroup();

        // Versorgung von Autoren-Aufträgen an BST
        configurationPanel.startGroup("!!Versorgung von Autoren-Aufträgen an BST");
        configurationPanel.addBooleanOption(CONFIG_SUPPLY_AUTHOR_ORDERS_TO_BST_ACTIVE, "!!Automatische Versorgung aktiv", false);
        configurationPanel.addStringOption(CONFIG_URI_SUPPLY_AUTHOR_ORDERS_TO_BST, "!!URI für BST Webservice zur Versorgung", true);
        configurationPanel.addIntegerSpinnerOption(CONFIG_SUPPLY_AUTHOR_ORDERS_TO_BST_TIMEOUT, "!!Timeout für BST Webservice zur Versorgung in Sekunden",
                                                   false, 1, 999999, 1);
        configurationPanel.addBooleanOption(CONFIG_SUPPLY_AUTHOR_ORDERS_TO_BST_SIMULATION, "!!Simulation aktiv", false).setTooltip("!!Simuliert den BST Webservice zur Versorgung von Autoren-Aufträgen mit obiger URI");

        {
            configurationPanel.startGroup("!!Token für den Aufruf vom BST Webservice zur Versorgung");
            configurationPanel.addStringOption(CONFIG_SUPPLY_AUTHOR_ORDERS_TO_BST_TOKEN_NAME, "!!Name", true);
            configurationPanel.addStringOption(CONFIG_SUPPLY_AUTHOR_ORDERS_TO_BST_TOKEN_TYPE, "!!Typ", true);
            GuiTextArea bstWebserviceTokenTextArea = configurationPanel.addStringAreaOption(CONFIG_SUPPLY_AUTHOR_ORDERS_TO_BST_TOKEN, "!!Token", false, true);
            bstWebserviceTokenTextArea.setMaximumWidth(600);
            bstWebserviceTokenTextArea.setMinimumHeight(50);
            bstWebserviceTokenTextArea.setMaximumHeight(50);
            bstWebserviceTokenTextArea.setLineWrap(true);
            bstWebserviceTokenTextArea.setTooltip("!!Falls leer, wird kein Token beim Aufruf vom BST Webservice zur Versorgung verwendet");
            configurationPanel.endGroup();
        }

        configurationPanel.endGroup();

        // NutzDok Webservices Authentifizierung
        configurationPanel.startGroup("!!NutzDok Webservice-Token im Request-Header");
        configurationPanel.addStringOption(CONFIG_NUTZDOK_HEADER_TOKEN_NAME, "!!Name", true);
        configurationPanel.addStringOption(CONFIG_NUTZDOK_HEADER_TOKEN_TYPE, "!!Typ", true);
        configurationPanel.addPasswordOption(CONFIG_NUTZDOK_HEADER_TOKEN_PASSWORD, "!!Passwort (für HS256 Verfahren; wenn leer ist HS256 nicht zugelassen)", false);
        configurationPanel.addIntegerSpinnerOption(CONFIG_NUTZDOK_HEADER_TOKEN_EXPIRES, "!!Erlaubte Differenz in Sekunden", true,
                                                   1, 100000, 10);
        {
            configurationPanel.startGroup("!!Überwachtes Verzeichnis für Public Key Definitionen im JSON-Format (für RS256 Verfahren)");
            configurationPanel.addFileOption(CONFIG_NUTZDOK_HEADER_TOKEN_PUB_KEY_DIR, "!!Verzeichnis", false,
                                             GuiFileChooserDialog.FILE_MODE_DIRECTORIES, FileChooserPurpose.OPEN, "!!Auswählen");
            configurationPanel.addBooleanOption(CONFIG_NUTZDOK_HEADER_TOKEN_PUB_KEY_DIR_ENFORCE_POLLING, "!!Polling erzwingen (nötig z.B. für NFS Shares)", false);
            configurationPanel.addIntegerSpinnerOption(CONFIG_NUTZDOK_HEADER_TOKEN_PUB_KEY_DIR_POLLING_TIME, "!!Polling Intervall (in min)", false, 1, 1000, 1);
            configurationPanel.endGroup();
        }

        configurationPanel.endGroup();

        // NutzDok Webservices
        configurationPanel.startGroup("!!NutzDok Webservices");
        addEndpointURIConfigOption(CONFIG_URI_NUTZDOK_CONSTRUCTION_KITS, "constructionKits", configurationPanel);
        addEndpointURIConfigOption(CONFIG_URI_NUTZDOK_CONSTRUCTION_KITS_REMARKS, "constructionKits/annotation", configurationPanel);
        configurationPanel.endGroup();

        // Scheduler für NutzDok Cortex-Aktivierung
        iPartsPlugin.createDayOfWeekTimerGroup(configurationPanel, "!!NutzDok Cortex Aktivierungstermine",
                                               CONFIG_NUTZDOK_CORTEX_ACTIVE, CONFIG_NUTZDOK_CORTEX_DAYS, CONFIG_NUTZDOK_CORTEX_TIME, true);

        // BOM DB: KEM Daten SOAP Webservice
        configurationPanel.startGroup("!!BOM-DB Webservices");
        configurationPanel.addBooleanOption(CONFIG_BOMDB_WEBSERVICE_STATUS, TranslationHandler.translate("!!Webservice '%1' aktiv",
                                                                                                         TranslationHandler.translate("!!BOM-DB KEM-Daten")), false);
        configurationPanel.addStringOption(CONFIG_BOMDB_WEBSERVICE_ENDPOINT_URI, TranslationHandler.translate("!!URI für Webservices '%1'",
                                                                                                              TranslationHandler.translate("!!BOM-DB")), false);
        configurationPanel.addStringOption(CONFIG_BOMDB_WEBSERVICE_USERNAME, "!!Benutzername", false);
        configurationPanel.addPasswordOption(CONFIG_BOMDB_WEBSERVICE_PASSWORD, "!!Passwort", false);
        GuiTextArea bomDbWebserviceAppTokenTextArea = configurationPanel.addStringAreaOption(CONFIG_BOMDB_WEBSERVICE_APPTOKEN,
                                                                                             "!!Application Token", false, true);
        bomDbWebserviceAppTokenTextArea.setMaximumWidth(600);
        bomDbWebserviceAppTokenTextArea.setMinimumHeight(50);
        bomDbWebserviceAppTokenTextArea.setMaximumHeight(50);
        bomDbWebserviceAppTokenTextArea.setLineWrap(true);

        configurationPanel.endGroup();

        configurationPanel.startGroup("!!DocuBrowser");
        configurationPanel.addBooleanOption(CONFIG_ENABLE_DOCUBROWSER_DEEPLINK, "!!DocuBrowser-Anzeige für EDS KEM- und SAA-Arbeitsvorrat aktivieren", false);
        GuiTextArea docuBrowserSaaOption = configurationPanel.addStringAreaOption(CONFIG_DOCUBROWSER_SAA_URI, "!!DocuBrowser URI für SAA", false, true);
        docuBrowserSaaOption.setMaximumWidth(500);
        docuBrowserSaaOption.setMinimumHeight(50);
        docuBrowserSaaOption.setMaximumHeight(50);
        docuBrowserSaaOption.setLineWrap(true);
        docuBrowserSaaOption.setTooltip("!!Platzhalter {{SAA}} für die SAA");

        GuiTextArea docuBrowserKemOption = configurationPanel.addStringAreaOption(CONFIG_DOCUBROWSER_KEM_URI, "!!DocuBrowser URI für KEM", false, true);
        docuBrowserKemOption.setMaximumWidth(500);
        docuBrowserKemOption.setMinimumHeight(50);
        docuBrowserKemOption.setMaximumHeight(50);
        docuBrowserKemOption.setLineWrap(true);
        docuBrowserKemOption.setTooltip("!!Platzhalter {{KEM}} für die KEM");
        configurationPanel.endGroup();

        configurationPanel.addStringListOptionSingleSelection(CONFIG_PURGE_DATABASE_TARGET_COMPANY, "!!Bereinigung der Datenbank aktivieren für Firma",
                                                              false, new String[]{ "", TruncateTables.TargetCompany.DTAG.name() }, false);

        return configurationPanel;
    }

    /**
     * Fügt eine Konfigurationsmöglichkeit für Sturkturstufen in Konstruktionsstücklisten hinzu
     *
     * @param configurationPanel
     * @param maxStructureLevelConfig
     * @param defaultStructureLevelConfig
     * @param maxText
     * @param defaultText
     */
    private void addStructureLevelOption(UniversalConfigurationPanel configurationPanel,
                                         UniversalConfigOption maxStructureLevelConfig,
                                         UniversalConfigOption defaultStructureLevelConfig, String maxText, String defaultText) {
        final GuiIntSpinner maxStructureLevelIntSpinner = configurationPanel.addIntegerSpinnerOption(maxStructureLevelConfig,
                                                                                                     maxText,
                                                                                                     false, 1, 100, 1);
        final GuiIntSpinner defaultStructureLevelIntSpinner = configurationPanel.addIntegerSpinnerOption(defaultStructureLevelConfig,
                                                                                                         defaultText,
                                                                                                         false, 1, 100, 1);
        maxStructureLevelIntSpinner.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                defaultStructureLevelIntSpinner.setMaxValue(maxStructureLevelIntSpinner.getValue());
            }
        });
    }

    @Override
    public Collection<String> getRequiredPluginClassNames() {
        return Arrays.asList(PLUGIN_CLASS_NAME_IPARTS);
    }

    @Override
    public void initPlugin(ConfigBase config) {
        initPluginBase(config, EtkPluginConstants.XML_CONFIG_PATH_BASE + '/' + INTERNAL_PLUGIN_NAME);
        pluginConfig = new UniversalConfiguration(config, getConfigPath());
        initConfigurationSettingsVariables();
        mqXMLMessageManagers = new DwList<>();
        if (isActive()) {
            MQChannelType.registerChannelType(iPartsPlugin.MQ_CHANNEL_TYPE_MEDIA);
        }
        keystoreManagerBST = new JWTKeystoreManager("BST webservices", pluginConfig, CONFIG_BST_HEADER_TOKEN_PUB_KEY_DIR,
                                                    CONFIG_BST_HEADER_TOKEN_PUB_KEY_DIR_ENFORCE_POLLING,
                                                    CONFIG_BST_HEADER_TOKEN_PUB_KEY_DIR_POLLING_TIME, LOG_CHANNEL_WS_TOKEN);
        keystoreManagerNutzDok = new JWTKeystoreManager("NutzDok webservices", pluginConfig, CONFIG_NUTZDOK_HEADER_TOKEN_PUB_KEY_DIR,
                                                        CONFIG_NUTZDOK_HEADER_TOKEN_PUB_KEY_DIR_ENFORCE_POLLING,
                                                        CONFIG_NUTZDOK_HEADER_TOKEN_PUB_KEY_DIR_POLLING_TIME, LOG_CHANNEL_WS_TOKEN);
    }

    @Override
    public void applicationStarted(boolean firstInit) {
        super.applicationStarted(firstInit);
        initConfigurationSettingsVariables();

        if (isActive() && AbstractApplication.isOnline()) {
            ClientModeFileChooserWindow.MAX_FILE_SIZE = Math.max(ClientModeFileChooserWindow.MAX_FILE_SIZE, MAX_UPLOAD_FILE_SIZE);

            // Für die MQ Session die AS-PLM Simulationswerte setzen
            iPartsPlugin.setSimAutoResponseDelayForSession(iPartsPlugin.getMqSession(), iPartsPlugin.SIM_AUTO_RESPONSE_DELAY,
                                                           pluginConfig.getConfigValueAsInteger(CONFIG_SIM_AUTO_RESPONSE_DELAY));
            iPartsPlugin.setSimAutoResponseDelayForSession(iPartsPlugin.getMqSession(), iPartsPlugin.SIM_AUTO_RESPONSE_DELAY_SEARCH,
                                                           pluginConfig.getConfigValueAsInteger(CONFIG_SIM_AUTO_RESPONSE_DELAY_SEARCH));

            // zentrale MQ XMLMessageListener registrieren (iPartsPlugin wird immer vor dem iPartsEditPlugin initialisiert)
            EtkProject mqProject = iPartsPlugin.getMqProject();
            if (mqProject != null) {
                // AS-PLM MQ XMLMessageManager für Medien (Bildauftrag) erzeugen
                iPartsMQMessageManager xmlMessageManager = iPartsMQMessageManager.getInstance(iPartsPlugin.XML_MESSAGE_MANAGER_NAME_MEDIA);
                MQHelper.getInstance().addMQMessageReceiver(iPartsPlugin.MQ_CHANNEL_TYPE_MEDIA, xmlMessageManager);
                mqXMLMessageManagers.add(xmlMessageManager);
                iPartsEditXMLMessageListeners.getInstance().registerXMLMessageListeners(mqProject, iPartsPlugin.getMqSession());
                autoCalcAndExportHelper = new iPartsSeriesAutoCalcAndExport(mqProject, iPartsPlugin.getMqSession());
                workbasketCalcAndExportHelper = new iPartsWorkbasketCalcAndExport(mqProject, iPartsPlugin.getMqSession());
                publishProductReminderScheduler = new iPartsPublishProductReminderScheduler(mqProject, iPartsPlugin.getMqSession());
                publishModelReminderScheduler = new iPartsPublishModelReminderScheduler(mqProject, iPartsPlugin.getMqSession());
                singleNutzDokCortexImportScheduler = new iPartsNutzDokCortexImportScheduler(mqProject, iPartsPlugin.getMqSession());
                mailboxResubmissionScheduler = new iPartsMailboxResubmissionScheduler(mqProject, iPartsPlugin.getMqSession());
            } else {
                Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "EtkProject in MQ web application session does not exist. Either the MQ initialization failed upon web application start or the iParts plug-in is not active or not initialized yet.");
            }

            keystoreManagerBST.startKeystoreDirectoryMonitor();
            keystoreManagerNutzDok.startKeystoreDirectoryMonitor();
        }

        if (isActive()) {
            DictTextCache.warmUpCache();
            restartPublishingThread();
            restartAutoCalcAndExportThread();
            restartWorkbasketsCalcAndExportThread();
            restartPublishReminderSchedulerThreads();
            restartNutzDokCortexImportThread();
            restartMailboxResubmissionThread();
        }
    }

    @Override
    public boolean sessionStarting(Session session) {
        if (AbstractApplication.isOnline()) {
            iPartsPlugin.setSimAutoResponseDelayForSession(session, iPartsPlugin.SIM_AUTO_RESPONSE_DELAY,
                                                           pluginConfig.getConfigValueAsInteger(CONFIG_SIM_AUTO_RESPONSE_DELAY));
            iPartsPlugin.setSimAutoResponseDelayForSession(session, iPartsPlugin.SIM_AUTO_RESPONSE_DELAY_SEARCH,
                                                           pluginConfig.getConfigValueAsInteger(CONFIG_SIM_AUTO_RESPONSE_DELAY_SEARCH));
        }
        return false;
    }

    @Override
    public void sessionStarted(Session session) {
        // Bei gültigem identCode die Datenkarte laden und im Filter setzen
        String identCode = session.getStartParameter().getParameter(START_PARAMETER_IDENT_CODE, null);
        if (StrUtils.isValid(identCode)) {
            AbstractJavaViewerFormConnector dataConnector = (AbstractJavaViewerFormConnector)session.getAttribute(JavaViewerApplication.SESSION_KEY_MAIN_CONNECTOR);
            if (dataConnector != null) {
                session.invokeThreadSafe(() -> IdentToDataCardHelper.activateFilterForIdentWithDataCard(identCode.trim(),
                                                                                                        dataConnector));
            }
        }
    }

    @Override
    public boolean sessionStartFailed(Throwable throwable) {
        return false;
    }

    @Override
    public void configurationChanged() {
        initConfigurationSettingsVariables();
        super.configurationChanged();

        keystoreManagerBST.stopKeystoreDirectoryMonitor();
        keystoreManagerNutzDok.stopKeystoreDirectoryMonitor();

        // Für die MQ Session die AS-PLM Simulationswerte setzen
        if (isActive() && AbstractApplication.isOnline()) {
            iPartsPlugin.setSimAutoResponseDelayForSession(iPartsPlugin.getMqSession(), iPartsPlugin.SIM_AUTO_RESPONSE_DELAY,
                                                           pluginConfig.getConfigValueAsInteger(CONFIG_SIM_AUTO_RESPONSE_DELAY));
            iPartsPlugin.setSimAutoResponseDelayForSession(iPartsPlugin.getMqSession(), iPartsPlugin.SIM_AUTO_RESPONSE_DELAY_SEARCH,
                                                           pluginConfig.getConfigValueAsInteger(CONFIG_SIM_AUTO_RESPONSE_DELAY_SEARCH));

            keystoreManagerBST.startKeystoreDirectoryMonitor();
            keystoreManagerNutzDok.startKeystoreDirectoryMonitor();
        }

        if (isActive()) {
            // Thread für die automatische Publikation neu starten (speziell, um das Warten im Publikations-Thread zu unterbrechen)
            restartPublishingThread();
            restartAutoCalcAndExportThread();
            restartWorkbasketsCalcAndExportThread();
            restartPublishReminderSchedulerThreads();
            restartNutzDokCortexImportThread();
            restartMailboxResubmissionThread();
        }
        // EDS Strukturtyp setzen
        iPartsEdsStructureHelper.setConfigurationValue();
    }

    @Override
    public boolean setActiveState(boolean active) {
        boolean activeStateChanged = super.setActiveState(active);

        if (active) {
            AbstractRelatedInfoMainForm.logChannelPerformance = LOG_CHANNEL_EDIT_PERFORMANCE;
        }

        if (activeStateChanged) {
            if (iPartsPlugin.getPublishingHelper() != null) {
                if (active) {
                    restartPublishingThread();
                } else {
                    iPartsPlugin.getPublishingHelper().stopPublishingThread();
                }
            }
            if (autoCalcAndExportHelper != null) {
                if (active) {
                    restartAutoCalcAndExportThread();
                } else {
                    autoCalcAndExportHelper.stopThread();
                }
            }
            if (workbasketCalcAndExportHelper != null) {
                if (active) {
                    restartWorkbasketsCalcAndExportThread();
                } else {
                    workbasketCalcAndExportHelper.stopThread();
                }
            }
            if ((publishProductReminderScheduler != null) && (publishModelReminderScheduler != null)) {
                if (active) {
                    restartPublishReminderSchedulerThreads();
                } else {
                    publishProductReminderScheduler.stopThread();
                    publishModelReminderScheduler.stopThread();
                }
            }
            if (singleNutzDokCortexImportScheduler != null) {
                if (active) {
                    restartNutzDokCortexImportThread();
                } else {
                    singleNutzDokCortexImportScheduler.stopThread();
                }
            }
            if (mailboxResubmissionScheduler != null) {
                if (active) {
                    restartMailboxResubmissionThread();
                } else {
                    mailboxResubmissionScheduler.stopThread();
                }
            }
        }

        return activeStateChanged;
    }

    @Override
    public void releaseReferences() {
        if (isActive()) {
            if (mqXMLMessageManagers != null) {
                for (iPartsMQMessageManager manager : mqXMLMessageManagers) {
                    MQHelper.getInstance().removeMQMessageReceiverFromAllChannels(manager);
                }
                mqXMLMessageManagers = null;
            }
            iPartsEditXMLMessageListeners.getInstance().deregisterXMLMessageListeners();
        }
        if (keystoreManagerBST != null) {
            keystoreManagerBST.stopKeystoreDirectoryMonitor();
        }
        if (keystoreManagerNutzDok != null) {
            keystoreManagerNutzDok.stopKeystoreDirectoryMonitor();
        }
        super.releaseReferences();
    }

    @Override
    public String getRequiredInternalAppName() {
        return FrameworkMain.INTERNAL_APP_NAME;
    }

    @Override
    public Colors[] getPluginColors() {
        return new Colors[]{ clPlugin_iPartsEdit_TableNotLinkedBackgroundColor,
                             clPlugin_iPartsEdit_HotspotNotLinkedBackgroundColor,
                             clPlugin_iPartsEdit_SecurityNoticeLabelForegroundColor,
                             clPlugin_iPartsEdit_DialogPositionVariantTextBackground,
                             clPlugin_iPartsEdit_DialogPositionTextBackground,
                             clPlugin_iPartsEdit_EdsBcsPositionTextBackground,
                             clPlugin_iPartsEdit_EdsBcsAdditionalTextBackground,
                             clPlugin_iPartsEdit_CttPositionTextBackground,
                             clPlugin_iPartsEdit_EdsKemMarkTextBackground,
                             clPlugin_iPartsEdit_ColorFootnoteMarkedLine,
                             clPlugin_iPartsEdit_ColorFootnoteBackgroundMarkedLine,
                             clPlugin_iPartsEdit_ColorFootnoteBackgroundSelectedLine,
                             clPlugin_iPartsEdit_UnreadMessagesBadgeBackgroundColor,
                             clPlugin_iPartsEdit_UnreadMessagesBadgeForegroundColor };
    }

    @Override
    public boolean checkLicense() {
        // iParts Lizenzoption
        return (LicenseConfig.getInstance() == null) || LicenseConfig.getInstance().licenseFunctionExists(LICENSE_KEY_IPARTS);
    }

    @Override
    public boolean isActive() {
        // nur aktiv, falls Redaktionssystem (EditMode) auch zumindest in der Konfiguration aktiv ist
        // Abfrage auf JavaViewerApplication.getInstance().getProject().isEditModeActive() hier nicht möglich, weil
        // isActive() sich auf alle Sessions bezieht und isEditModeActive() Session-abhängig ist
        if (super.isActive()) {
            if (getConfig() == null) { // Spezialfall ohne Konfiguration
                return true;
            }

            return getConfig().getBoolean(EditModeSettingsPanel.XML_CONFIG_PATH_BASE + EditModeSettingsPanel.XML_CONFIG_SUBPATH_EDIT_MODE_ACTIVE,
                                          SwingHandler.isSwing());
        } else {
            return false;
        }
    }

    @Override
    public DesignImage[] getPluginImages() {
        ArrayList<DesignImage> pluginImages = new ArrayList<DesignImage>(EditDefaultImages.getImages());
        pluginImages.add(IMAGE_DAIMLER);
        return pluginImages.toArray(new DesignImage[pluginImages.size()]);
    }

    @Override
    public LogChannels[] getPluginLogChannels() {
        return new LogChannels[]{ LOG_CHANNEL_EDIT_PERFORMANCE, LOG_CHANNEL_WS_DEBUG, LOG_CHANNEL_WS_PERFORMANCE,
                                  LOG_CHANNEL_WS_TOKEN, LOG_CHANNEL_SUPPLY_TO_BST, LOG_CHANNEL_SERIES_CALC_AND_EXPORT,
                                  LOG_CHANNEL_WORKBASKETS_CALC_AND_EXPORT, LOG_CHANNEL_PUBLISH_REMINDER_SCHEDULER, LOG_CHANNEL_SMARAGD,
                                  LOG_CHANNEL_BOM_DB_SOAP_WEBSERVICE, LOG_CHANNEL_NUTZDOK_CORTEX_SCHEDULER,
                                  LOG_CHANNEL_QUALITY_CHECK };
    }

    @Override
    public void adjustTheme(Theme theme) {
        if (theme instanceof MaterialTheme) {
            MaterialTheme materialTheme = (MaterialTheme)theme;

            String materialSubDir = "material";
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_copy_fd_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_btn_copy_factory_data_toClipboard, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_edit_add_to_picture_order_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_btn_add_to_picture_order, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_edit_copy_tu_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_copy_tu, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_edit_dict_copy_text_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_dict_CopyText, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_edit_material_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_btn_edit_material, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_edit_module_master_data_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_module_master_data, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_edit_multiple_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_btn_edit_multiple, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_edit_order_move_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_btn_order_assign, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_edit_pages_all_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_btn_view_all_pages, null,
                                                                                   EditDefaultImages.edit_btn_view_all_pages_selected, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_edit_partlistentry_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_btn_edit_partslistentry, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_edit_partlistentry_modified_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_btn_partlistentry_modified, null,
                                                                                   EditDefaultImages.edit_btn_partlistentry_modified_selected, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_edit_picture_change_order_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_btn_picture_change_order, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_edit_picture_order_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_btn_picture_order, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_edit_picture_order_closed_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_btn_picture_order_closed, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_edit_refresh_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_btn_refresh, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_edit_show_all_picorder_active_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_btn_show_all_pic_orders, null,
                                                                                   EditDefaultImages.edit_btn_show_all_pic_orders_pressed, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_edit_undo_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_btn_undo, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_order_partial_undo_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_order_partial_undo, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_edit_validate_assembly_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_validate_assembly, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_edit_validate_hotspot_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_validate_hotspot, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_history_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_history, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_import_ctt_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_import_ctt, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_mailbox_view_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_mailbox_view, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_mailbox_read_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_mailbox_read, null,
                                                                                   EditDefaultImages.edit_mailbox_read_selected, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_mailbox_unread_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_mailbox_unread, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_mailbox_answer_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_mailbox_answer, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_mailbox_forward_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_mailbox_forward, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_module_in_conflict_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_btn_module_in_conflict, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_module_in_edit_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_btn_module_in_edit, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_paste_and_link_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_btn_copy_factory_data_toClipboard_and_link, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_paste_fd_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_btn_paste_factory_data_toClipboard, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_open_non_modal_mechanic_window_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_openNonModalMechanicWindow, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_optimize_north_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_btn_optimize_north, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_optimize_south_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_btn_optimize_south, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_order_status_history_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_order_status_history, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_replace_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_btn_replace, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_sort_by_hotspot_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_sort_by_hotspot, null,
                                                                                   null, null));
            materialTheme.addMaterialIconsToHashSet(EditDefaultImages.class, materialSubDir, "img_unlink_16px",
                                                    new MaterialThemeIconComponent(EditDefaultImages.edit_btn_unlink_factory_data, null,
                                                                                   null, null));
        }
    }

    @Override
    public void clearPluginCaches(ClusterEventInterface event) {
        clearPluginCaches(true);
    }

    public void clearPluginCaches(boolean clearGlobalCaches) {
        if (clearGlobalCaches) {
            iPartsWorkOrderCache.clearCache();
            BillableDataCache.clearCache();
            EditCombinedTextForm.clearCache();
        }
    }

    @Override
    public EnumSet<State> modifyButtonState(String buttonAlias, JavaViewerMainWindow mainWindow) {
        return null;
    }

    @Override
    public List<MainButtonInfo> addToolbarButtons(JavaViewerMainWindow mainWindow) {
        List<MainButtonInfo> result = new ArrayList<MainButtonInfo>();

        // ToolbarButtons nur hinzufügen, wenn das Redaktionssystem (EditMode) aktiv ist
        boolean isViewPartsDataRight = iPartsRight.VIEW_PARTS_DATA.checkRightInSession();
        if (!JavaViewerApplication.getInstance().getProject().isEditModeActive() || !isViewPartsDataRight) {
            return result;
        }

        boolean isEditPartsDataRight = iPartsRight.EDIT_PARTS_DATA.checkRightInSession();
        if (isViewPartsDataRight || isEditPartsDataRight) {
            MainButtonInfo editModuleButtonInfo = createNewEditMainToolbarEntry(isEditPartsDataRight ? "!!&Technischen Umfang bearbeiten" : "!!&Technischen Umfang laden",
                                                                                "iParts1EditModules");
            if (editModuleButtonInfo != null) {
                result.add(editModuleButtonInfo);
            }
        }

        if (iPartsRight.VIEW_AUTHOR_ORDERS.checkRightInSession()) {
            MainButtonInfo workEditButtonInfo = createNewWorkEditMainToolbarEntry("!!&Aufträge", "iParts2EditWork");
            if (workEditButtonInfo != null) {
                result.add(workEditButtonInfo);
            }
        }

        // Postkorb nur anzeigen, wenn man PKW/VAN Rechte hat
        if (iPartsRight.checkCarAndVanInSession()) {
            MainButtonInfo mailboxButtonInfo = createNewMailboxMainToolbarEntry();
            result.add(mailboxButtonInfo);
        }

        // EDS Arbeitsvorrat nur anzeigen, wenn man Truck/Bus Rechte hat
        if (iPartsRight.VIEW_EDS_WORK_BASKET.checkRightInSession() && iPartsRight.checkTruckAndBusInSession()) {
            MainButtonInfo workBasketButtonInfo = createNewEDSWorkBasketToolbarEntry();
            result.add(workBasketButtonInfo);
        }

        if (DWLayoutManager.get().isResponsiveMode()) {
            result.add(createLogoutButtonInfo(mainWindow));
        }

        return result;
    }

    /**
     * EditModuleForm erzeugen und in MainToolbar einhängen
     *
     * @param text
     * @param alias
     * @return
     */
    private MainButtonInfo createNewEditMainToolbarEntry(final String text, String alias) {
        final AbstractJavaViewerFormConnector mainConnector = JavaViewerApplication.getInstance().getMainConnector();
        EditModuleFormConnector editModuleFormConnector = new EditModuleFormConnector(mainConnector);
        final EditModuleForm editModuleForm = new EditModuleForm(editModuleFormConnector, mainConnector.getMainWindow(),
                                                                 iPartsEditPlugin.this);
        boolean isResponsive = DWLayoutManager.get().isResponsiveMode();
        RButtonImages images = new ToolButtonImages(isResponsive ? EditDefaultImages.edit_editModulesToolbarButtonGray.getImage()
                                                                 : EditDefaultImages.edit_editModulesToolbarButton.getImage(),
                                                    isResponsive ? EditDefaultImages.edit_editModulesToolbarButton.getImage()
                                                                 : null,
                                                    isResponsive ? EditDefaultImages.edit_editModulesToolbarButton.getImage()
                                                                 : EditDefaultImages.edit_editModulesToolbarButtonWhite.getImage(),
                                                    null).toRButtonImages();
        return new MainButtonInfo(editModuleForm, images, alias, EnumSet.of(MainButtonInfo.Options.SHOW_IN_VIEW_MENU), null, text);
    }

    private MainButtonInfo createNewWorkEditMainToolbarEntry(final String title, String alias) {
        AbstractJavaViewerFormConnector mainConnector = JavaViewerApplication.getInstance().getMainConnector();
        EditWorkMainForm editMainForm = new EditWorkMainForm(mainConnector, mainConnector.getMainWindow());
        boolean isResponsive = DWLayoutManager.get().isResponsiveMode();
        RButtonImages images = new ToolButtonImages(isResponsive ? EditDefaultImages.edit_workEditToolbarButtonGray.getImage()
                                                                 : EditDefaultImages.edit_workEditToolbarButton.getImage(),
                                                    isResponsive ? EditDefaultImages.edit_workEditToolbarButton.getImage()
                                                                 : null,
                                                    isResponsive ? EditDefaultImages.edit_workEditToolbarButton.getImage()
                                                                 : EditDefaultImages.edit_workEditToolbarButtonWhite.getImage(),
                                                    null).toRButtonImages();
        return new MainButtonInfo(editMainForm, images, alias, EnumSet.of(MainButtonInfo.Options.SHOW_IN_VIEW_MENU), null, title);
    }

    private MainButtonInfo createNewMailboxMainToolbarEntry() {
        AbstractJavaViewerFormConnector mainConnector = JavaViewerApplication.getInstance().getMainConnector();
        iPartsMailboxMainForm mailboxForm = new iPartsMailboxMainForm(mainConnector, mainConnector.getMainWindow());
        return new MainButtonInfo(mailboxForm, mailboxForm.getMainToolbarIcon().toRButtonImages(), iPartsMailboxMainForm.MAIN_TOOLBAR_ALIAS, EnumSet.of(MainButtonInfo.Options.SHOW_IN_VIEW_MENU),
                                  IPARTS_MAIN_TOOLBAR_BUTTON_JOBS, "!!&Postkorb");
    }

    private MainButtonInfo createNewEDSWorkBasketToolbarEntry() {
        AbstractJavaViewerFormConnector mainConnector = JavaViewerApplication.getInstance().getMainConnector();
        iPartsEDSWorkBasketMainForm workBasketForm = new iPartsEDSWorkBasketMainForm(mainConnector, mainConnector.getMainWindow());
        return new MainButtonInfo(workBasketForm, workBasketForm.getMainToolbarIcon().toRButtonImages(), iPartsEDSWorkBasketMainForm.MAIN_TOOLBAR_ALIAS,
                                  EnumSet.of(MainButtonInfo.Options.SHOW_IN_VIEW_MENU),
                                  null, "!!T&ruck Arbeitsvorrat");
    }

    @Override
    public void modifyMenu(MenuManager manager) {
        // Menüpunkt nur hinzufügen, wenn das Redaktionssystem (EditMode) aktiv ist
        EtkProject project = JavaViewerApplication.getInstance().getProject();
        if (!project.isEditModeActive()) {
            return;
        }

        // Stammdaten
        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_MASTER_DATA, "!!Produkte...", null, false, showProductMasterData(), MenuManager.MENU_NAME_PRINT);
        manager.addMenuFunction(IPARTS_MENU_NAME_MASTER_DATA, "!!Freie SAs...", null, false, showFreeSAsMasterData());
        // Menüpunkte "Baureihen" und "Baumuster (Konstruktion)" nur anzeigen, wenn man PKW/VAN Rechte hat
        if (iPartsRight.checkCarAndVanInSession()) {
            manager.addMenuFunction(IPARTS_MENU_NAME_MASTER_DATA, "!!Baureihen...", null, false, showSeriesMasterData());
            manager.addMenuFunction(IPARTS_MENU_NAME_MASTER_DATA, "!!Baumuster (Konstruktion)...", null, false, showModelConstructionMasterData());
        }
        manager.addMenuFunction(IPARTS_MENU_NAME_MASTER_DATA, "!!Baumuster (After-Sales)...", null, false, showModelAfterSalesMasterData());
        // ToDo freigeben bei DAIMLER-5920
        //manager.addMenuFunction(IPARTS_MENU_NAME_MASTER_DATA, "!!Varianten...", null, false, showVariantsMasterData());
        manager.addMenuFunction(IPARTS_MENU_NAME_MASTER_DATA, "!!Zuordnung Fahrzeugbaumuster / Aggregatebaumuster (After-Sales)...", null, false, showModelsAggsMasterData());
        manager.addMenuFunction(IPARTS_MENU_NAME_MASTER_DATA, "!!SAA/BK-Gültigkeiten zu Baumuster...", null, false, showSAAsModelsMasterData());

        manager.addMenuFunction(IPARTS_MENU_NAME_MASTER_DATA, "!!Produkte zu Baumuster...", null, false, showProductModelsMasterData());
        manager.addMenuFunction(IPARTS_MENU_NAME_MASTER_DATA, "!!PEMs...", null, false, showPEMMasterData());

        GuiMenuBarEntry menuBarEntry = manager.getOrAddMainMenu(IPARTS_MENU_NAME_MASTER_DATA);
        menuBarEntry.addChild(new GuiSeparator());

        manager.addMenuFunction(IPARTS_MENU_NAME_MASTER_DATA, "!!Teilestamm...", null, false, showMatMasterData());
        // DAIMLER-11067, Suche nach SAA/BK Verwendung
        manager.addMenuFunction(IPARTS_MENU_NAME_MASTER_DATA, "!!SAA/BK-Verwendung...", null, false, showSaaBkUsage());

        if (iPartsRight.checkCarAndVanInSession()) {
            manager.addMenuFunction(IPARTS_MENU_NAME_MASTER_DATA, "!!Nach Einzelteilen in Baukästen suchen...", null, false, showSearchPartInConstructionKits());
        }

        // Lexikon
        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_DICTIONARY, "!!Lexikon anzeigen...", null, false, showDictionary(), IPARTS_MENU_NAME_MASTER_DATA);
        if (iPartsRight.REQUEST_TRANSLATIONS.checkRightInSession()) {
            // Separator
            menuBarEntry = manager.getOrAddMainMenu(IPARTS_MENU_NAME_DICTIONARY);
            menuBarEntry.addChild(new GuiSeparator());
            manager.addMenuFunction(IPARTS_MENU_NAME_DICTIONARY, "!!Neue Texte für Übersetzungsumfang...", null, false, searchCreatedDictionaryEntries());
            menuBarEntry.addChild(new GuiSeparator());
            manager.addMenuFunction(IPARTS_MENU_NAME_DICTIONARY, "!!Verwaltungstabelle für Übersetzungsprozess...", null, false, showTransJobs());
        }

        // Import
        if (iPartsRight.IMPORT_MASTER_DATA.checkRightInSession()) {
            manager.addMenuFunctionAfter(IPARTS_MENU_NAME_IMPORT, "!!Neue Zeichnung...", null, false, importNewImage(), IPARTS_MENU_NAME_DICTIONARY);
        }

        // Notizen
        boolean isEditMasterDataAllowed = iPartsRight.EDIT_MASTER_DATA.checkRightInSession();
        if (isEditMasterDataAllowed) {
            manager.setForceNotesImportExport(true);
        }

        // Optionen
        GuiMenu optionsMenu = manager.getOrAddMainMenu(MenuManager.MENU_NAME_OPTIONS);
        final String showAggregatesInAllProductsText = "!!Aggregate in Fahrzeug-Produkten anzeigen";
        GuiMenuItem showAggregatesInAllProductsMenu = manager.addMenuFunction(MenuManager.MENU_NAME_OPTIONS, showAggregatesInAllProductsText,
                                                                              null, false, true, setShowAggregatesInAllProducts(showAggregatesInAllProductsText));
        if (showAggregatesInAllProductsMenu instanceof GuiCheckboxMenuItem) {
            ((GuiCheckboxMenuItem)showAggregatesInAllProductsMenu).setSelected(project.getUserSettings().getBoolValues(iPartsUserSettingsConst.REL_SHOW_AGGREGATES_IN_ALL_PRODUCTS));
        }

        final String hideEmptyTUsText = "!!Leere TUs ausblenden";
        GuiMenuItem hideEmptyTUsMenu = manager.addMenuFunction(MenuManager.MENU_NAME_OPTIONS, hideEmptyTUsText,
                                                               null, false, true, setHideEmptyTUs(hideEmptyTUsText));
        if (hideEmptyTUsMenu instanceof GuiCheckboxMenuItem) {
            ((GuiCheckboxMenuItem)hideEmptyTUsMenu).setSelected(project.getUserSettings().getBoolValues(iPartsUserSettingsConst.REL_HIDE_EMPTY_TUS));
        }

        final String showPreferSVGText = "!!SVG bevorzugen";
        GuiMenuItem showPreferSVGMenu = manager.addMenuFunction(MenuManager.MENU_NAME_OPTIONS, showPreferSVGText, null, false,
                                                                true, setSvgIsPreferred(showPreferSVGText));
        if (showPreferSVGMenu instanceof GuiCheckboxMenuItem) {
            ((GuiCheckboxMenuItem)showPreferSVGMenu).setSelected(project.getUserSettings().getBoolValues(iPartsUserSettingsConst.REL_SVG_IS_PREFERRED));
        }

        final String showHmMSmNodesText = "!!Ausgeblendete HM/M/SM-Knoten anzeigen";
        GuiMenuItem showHmMSMMenu = manager.addMenuFunction(MenuManager.MENU_NAME_OPTIONS, showHmMSmNodesText, null, false,
                                                            true, showHmMSmNodes(showHmMSmNodesText));
        if (showHmMSMMenu instanceof GuiCheckboxMenuItem) {
            ((GuiCheckboxMenuItem)showHmMSMMenu).setSelected(project.getUserSettings().getBoolValues(iPartsUserSettingsConst.REL_SHOW_HIDDEN_HMMSM_NODES));
        }

        if (Constants.DEVELOPMENT && iPartsRight.EDIT_MASTER_DATA.checkRightInSession()) { // Matrix-Darstellung nur bei DEVELOPMENT und mit dem Recht 'Stammdaten bearbeiten'
            boolean fDebug = false; // zum Debuggen zurücksetzen
            // Separator
            GuiSeparator sep = new GuiSeparator();
            sep.setVisible(fDebug);
            optionsMenu.addChild(sep);

            String setUltraEditViewText = "!!Matrix-Darstellung für Stücklistendaten und Materialstamm";
            String allowEmptyColsText = "!!Matrix-Darstellung: Mit leeren Spalten";
            String allowEmptyRowsText = "!!Matrix-Darstellung: Mit leeren Zeilen";

            GuiMenuItem ultraEditMenu = manager.addMenuFunction(MenuManager.MENU_NAME_OPTIONS, setUltraEditViewText, null, false, true,
                                                                setUltraEditView(setUltraEditViewText, allowEmptyColsText, allowEmptyRowsText));
            boolean isUltraEditMenuSelected = true;
            if (ultraEditMenu instanceof GuiCheckboxMenuItem) {
                isUltraEditMenuSelected = project.getUserSettings().getBoolValues(iPartsUserSettingsConst.REL_ULTRA_EDIT_VIEW);
                ((GuiCheckboxMenuItem)ultraEditMenu).setSelected(isUltraEditMenuSelected);
                ultraEditMenu.setVisible(fDebug);
            }

            GuiMenuItem allowEmptyCols = manager.addMenuFunction(MenuManager.MENU_NAME_OPTIONS, allowEmptyColsText, null, false, true,
                                                                 setUltraEditViewAllow(allowEmptyColsText, iPartsUserSettingsConst.REL_ULTRA_EDIT_VIEW_COLS));
            if (allowEmptyCols instanceof GuiCheckboxMenuItem) {
                ((GuiCheckboxMenuItem)allowEmptyCols).setSelected(project.getUserSettings().getBoolValues(iPartsUserSettingsConst.REL_ULTRA_EDIT_VIEW_COLS));
                if (ultraEditMenu.isVisible()) {
                    allowEmptyCols.setVisible(isUltraEditMenuSelected);
                } else {
                    allowEmptyCols.setVisible(false);
                }
            }
            GuiMenuItem allowEmptyRows = manager.addMenuFunction(MenuManager.MENU_NAME_OPTIONS, allowEmptyRowsText, null, false, true,
                                                                 setUltraEditViewAllow(allowEmptyRowsText, iPartsUserSettingsConst.REL_ULTRA_EDIT_VIEW_ROWS));
            if (allowEmptyRows instanceof GuiCheckboxMenuItem) {
                ((GuiCheckboxMenuItem)allowEmptyRows).setSelected(project.getUserSettings().getBoolValues(iPartsUserSettingsConst.REL_ULTRA_EDIT_VIEW_ROWS));
                if (ultraEditMenu.isVisible()) {
                    allowEmptyRows.setVisible(isUltraEditMenuSelected);
                } else {
                    allowEmptyRows.setVisible(false);
                }
            }
        }

        // Separator
        optionsMenu.addChild(new GuiSeparator());

        String setSingleEditText = "!!Einzeleditoren aktivieren";
        GuiMenuItem singleEditMenu = manager.addMenuFunction(MenuManager.MENU_NAME_OPTIONS, setSingleEditText, null, false, true,
                                                             setSingleEdit(setSingleEditText));
        if (singleEditMenu instanceof GuiCheckboxMenuItem) {
            boolean isSingleEditMenuSelected = project.getUserSettings().getBoolValues(iPartsUserSettingsConst.REL_SINGLE_EDIT_VIEW);
            ((GuiCheckboxMenuItem)singleEditMenu).setSelected(isSingleEditMenuSelected);
        }

        // Separator
        optionsMenu.addChild(new GuiSeparator());

        boolean menuItemsAfterSeparator = false;
        if (Right.SHOW_USER_ADMIN.checkRightInSession() && iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_USER_ADMIN_ENABLED)) {
            manager.addMenuFunction(MenuManager.MENU_NAME_OPTIONS, "!!Benutzerverwaltung...", null, false, showUserAdmin());
            menuItemsAfterSeparator = true;
        }
        if ((iPartsPlugin.getPublishingHelper() != null) && iPartsRight.DELETE_RETAIL_CACHES.checkRightInSession()) {
            manager.addMenuFunction(MenuManager.MENU_NAME_OPTIONS, "!!Retail-Caches zurücksetzen", null, false, publishDataForRetail());
            menuItemsAfterSeparator = true;
        }

        // Separator
        if (menuItemsAfterSeparator) {
            optionsMenu.addChild(new GuiSeparator());
        }

        // Test
        if (iPartsRight.VIEW_PARTS_DATA.checkRightInSession()) {
            manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, "!!XSF Support URL Eingabe...", null, false, showSupportURLInput(), MenuManager.MENU_NAME_HELP);
            manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, null, null, false, null, MenuManager.MENU_NAME_HELP);
        }

        // MQ-Menüpunkte nur bei JEE Online im EditMode
        if (AbstractApplication.isOnline() && getProject().isEditModeActive() && isEditMasterDataAllowed) {
            manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, "!!MQ Test", null, false, iPartsPlugin.showMQTestDialog(),
                                          MenuManager.MENU_NAME_HELP);
            manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, "!!MQ Event Simulation", null, false, showMQEventDialog(),
                                          MenuManager.MENU_NAME_HELP);
        }

        // TruckBOM.foundation Webservices - Bedingungen analog zu MQ Test
        if (AbstractApplication.isOnline() && getProject().isEditModeActive() && isEditMasterDataAllowed) {
            // Separator
            manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, null, null, false, null, MenuManager.MENU_NAME_HELP);
            manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, "!!TruckBOM.foundation Webservices", null, false, showTruckBOMFoundationWebservicesForm(),
                                          MenuManager.MENU_NAME_HELP);
        }

        if (isEditMasterDataAllowed) {
            // Separator
            manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, null, null, false, null, MenuManager.MENU_NAME_HELP);
        }

        manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, "!!Bookmark für aktuelle Teile-Selektion...", null, false,
                                      showBookmarkForSelection(), MenuManager.MENU_NAME_HELP);

        // Separator
        manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, null, null, false, null, MenuManager.MENU_NAME_HELP);

        final String showEinPASMappingText = "!!EinPAS-Mapping anzeigen";
        manager.addMenuFunction(IPARTS_MENU_NAME_TEST, showEinPASMappingText, null, false, true,
                                showEinPASMapping(showEinPASMappingText));

        // Separator
        manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, null, null, false, null, MenuManager.MENU_NAME_HELP);

        manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, "!!Technische Änderungssets anzeigen...", null, false,
                                      showTechnicalChangeSets(), MenuManager.MENU_NAME_HELP);

        manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, "!!ChangeSet-Inhalt anzeigen...", null, false,
                                      showChangeLogInCopyWindow(), MenuManager.MENU_NAME_HELP);

        manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, "!!KEM-Blatt Pfadberechnung...", null, false,
                                      showKemSheetCalculation(), MenuManager.MENU_NAME_HELP);

        GuiMenuItem searchAuthorOrderItem = manager.addMenuFunction(IPARTS_MENU_NAME_TEST, "!!Suche in nicht-freigegebenen Autoren-Aufträgen...", null);
        searchAuthorOrderItem.addChild(manager.createMenuItem("!!Suche nach Materialnummer", EditDatabaseHelper.searchChangeSetsForMaterialNumber(getProject())));
        searchAuthorOrderItem.addChild(manager.createMenuItem("!!Suche nach BCTE-Schlüssel", EditDatabaseHelper.searchChangeSetsForBCTEkey(getProject())));

        if (iPartsRight.PURGE_DATABASE.checkRightInSession()) {
            String targetCompanyString = pluginConfig.getConfigValueAsString(CONFIG_PURGE_DATABASE_TARGET_COMPANY);
            if (StrUtils.isValid(targetCompanyString)) {
                TruncateTables.TargetCompany targetCompany = TruncateTables.TargetCompany.valueOf(targetCompanyString);
                if (targetCompany != null) {
                    GuiMenuItem dbDay2Item = manager.addMenuFunction(IPARTS_MENU_NAME_TEST, TranslationHandler.translate("!!iParts DB Day2 für %1", targetCompany.name()), null);
                    dbDay2Item.addChild(manager.createMenuItem(TruncateTables.TruncateStyle.DELETE_PRODUCTS.getName(), iPartsDbDay2DeleteProducts(targetCompany)));
                    dbDay2Item.addChild(manager.createMenuItem(TruncateTables.TruncateStyle.PARTIAL_TRUNCATE_TABLES.getName(), iPartsDbDay2TruncatePartialTables(targetCompany)));
                    dbDay2Item.addChild(manager.createMenuItem(TruncateTables.TruncateStyle.TRUNCATE_TABLES.getName(), iPartsDbDay2TruncateTables(targetCompany)));
                    dbDay2Item.addChild(manager.createMenuItem(TruncateTables.TruncateStyle.CLEAN_USER_DATA.getName(), iPartsDbDay2CleanUserData(targetCompany)));
                    dbDay2Item.addChild(new GuiSeparator());
                    dbDay2Item.addChild(manager.createMenuItem(TruncateTables.TruncateStyle.PURGE_ALL.getName(), iPartsDbDay2PurgeAll(targetCompany)));
                }
            }
        }

        if (iPartsRight.VIEW_DATABASE_TOOLS.checkRightInSession()) {
            GuiMenuItem dbToolsMenuItem = manager.addMenuFunction(IPARTS_MENU_NAME_TEST, "!!Datenbank-Tools", null);

            dbToolsMenuItem.addChild(manager.createMenuItem("!!SQL Performance Tests...", iPartsPlugin.executeSQLPerformanceTests()));

            // Separator
            dbToolsMenuItem.addChild(new GuiSeparator());

            // SQL Tools NUR für Admins
            String loginUserIdForSession = iPartsUserAdminDb.getLoginUserIdForSession();
            if (StrUtils.isValid(loginUserIdForSession) && iPartsUserAdminCache.getInstance(loginUserIdForSession).isUserRole(iPartsUserAdminDb.ROLE_ID_ADMIN)) {
                dbToolsMenuItem.addChild(manager.createMenuItem("!!SQL Tools...", iPartsEditPlugin.showSQLTools()));

                // Separator
                dbToolsMenuItem.addChild(new GuiSeparator());
            }

            dbToolsMenuItem.addChild(manager.createMenuItem("!!Verwaltungstabelle der Änderungsstände bereinigen",
                                                            deleteNotRequiredDialogChangesData()));

            // Separator
            dbToolsMenuItem.addChild(new GuiSeparator());


            dbToolsMenuItem.addChild(manager.createMenuItem("!!Tabellendefinitionen als CSV exportieren...",
                                                            EditDatabaseHelper.exportTableDefinitionsAsCSVFile(getProject())));

            // Separator
            dbToolsMenuItem.addChild(new GuiSeparator());

            //        Ist aktuell ohne Funktion -> auskommentiert
            //        manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, "!!Datenbank-Konsistenz überprüfen...", null, false, checkDBConsistency(),
            //                                      MenuManager.MENU_NAME_HELP);

            dbToolsMenuItem.addChild(manager.createMenuItem("!!Reihenfolgenummern reorganisieren...", reorgSequenzNumber()));

            dbToolsMenuItem.addChild(manager.createMenuItem("!!Datenbank-Konsistenz Text-IDs überprüfen...", checkTextIdConsistency()));

            dbToolsMenuItem.addChild(manager.createMenuItem("!!Term-IDs überprüfen...", checkTermIdConsistency()));

            dbToolsMenuItem.addChild(manager.createMenuItem("!!Alle DIALOG-Konstruktionsdaten löschen...", eraseDialogConstructionData()));
            dbToolsMenuItem.addChild(manager.createMenuItem("!!PEM-Stammdaten aus bestehenden Daten erzeugen", generatePemMasterdata()));
            dbToolsMenuItem.addChild(manager.createMenuItem("!!Quell-GUIDs korrigieren für Nicht-DIALOG-Stücklisteneinträge",
                                                            correctSourceGUIDForNonDIALOGPartListEntries()));
            dbToolsMenuItem.addChild(manager.createMenuItem("!!SAA/BK-Gültigkeiten für TUs ermitteln und speichern",
                                                            EditDatabaseHelper.saveAllModuleSAAValiditiesForFilter(getProject())));
            dbToolsMenuItem.addChild(manager.createMenuItem("!!Gültigkeitsbereich von Bildtafeln ermitteln und speichern",
                                                            EditDatabaseHelper.saveAllValidityScopesForImages(getProject())));
            dbToolsMenuItem.addChild(manager.createMenuItem("!!Materialnummer in ChangeSet-Einträgen ergänzen",
                                                            EditDatabaseHelper.extractAndSaveMaterialNumberInChangeSetEntry(getProject())));
            dbToolsMenuItem.addChild(manager.createMenuItem("!!NutzDok-Tabellen konvertieren...", convertNutzDokTabs()));
            dbToolsMenuItem.addChild(manager.createMenuItem("!!Minimales KEM-Datum-ab und maximales KEM-Datum-bis neu berechnen...",
                                                            EditDatabaseHelper.recalculateMinMaxKEMDates(getProject())));
        }

        // Separator
        manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, null, null, false, null, MenuManager.MENU_NAME_HELP);
        manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, "!!Nachrichten...", null, false, new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                Language currentLanguage = Language.findLanguage(owner.getConnector().getProject().getViewerLanguage());
                NewsListReader.showReadersWindowInSession(false, NewsFilter.readersFilters(), currentLanguage.getLocale());
            }
        }, MenuManager.MENU_NAME_HELP);

        if (isEditMasterDataAllowed) {
            // Separator
            manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, null, null, false, null, MenuManager.MENU_NAME_HELP);

            manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, "!!Retail-relevante Caches löschen", null, false, iPartsPlugin.clearCaches(true, null),
                                          MenuManager.MENU_NAME_HELP);
            manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, "!!Lexikon-Caches löschen", null, false, clearDictionaryCaches(),
                                          MenuManager.MENU_NAME_HELP);
            manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, "!!Alle kleineren Caches löschen", null, false,
                                          iPartsPlugin.clearCaches(false, EnumSet.of(iPartsCacheType.ALL_SMALL_CACHES)),
                                          MenuManager.MENU_NAME_HELP);
            manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, "!!Alle Caches löschen", null, false, iPartsPlugin.clearCaches(false, null),
                                          MenuManager.MENU_NAME_HELP);

            // InterAppCom ist nur bei JEE Online sinnvoll
            if (AbstractApplication.isOnline()) {
                // Separator
                manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, null, null, false, null, MenuManager.MENU_NAME_HELP);

                manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, "!!Kommunikation zwischen WebApps...", null, false,
                                              iPartsPlugin.showInterAppComStatus(), MenuManager.MENU_NAME_HELP);
                // Separator
                manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, null, null, false, null, MenuManager.MENU_NAME_HELP);
                manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, "!!Thread für LDAP-Benutzer-Synchronisierung neu starten", null,
                                              false, createRestartLDAPThreadFunction(), MenuManager.MENU_NAME_HELP);
                manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, "!!Threads für automatische Aktionen im Hintergrund neu starten", null,
                                              false, createRestartSchedulerThreadsFunction(), MenuManager.MENU_NAME_HELP);
                if (Constants.DEVELOPMENT && !Constants.DEVELOPMENT_QFTEST) {
                    manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, "!!Cortex Scheduler starten...", null, false,
                                                  startCortexScheduler(), MenuManager.MENU_NAME_HELP);

                }
            }
        }
    }

    private EtkFunction reorgSequenzNumber() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                DialogReorgSequenzNumber.showForm(owner);
            }
        };
    }

    private EtkFunction showDictionary() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                DictShowTextKindForm.showDictTextKind(owner);
            }
        };
    }

    private EtkFunction searchCreatedDictionaryEntries() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                EtkDbs etkDbs = owner.getProject().getEtkDbs();
                boolean resetFlag = false;
                try {
                    // In der KEYVALUE Tabelle nachschauen, ob die Übernahme schon von einem andere Autor ausgeführt wurde
                    boolean transAlreadyRunning = SQLStringConvert.ppStringToBoolean(etkDbs.getKeyValue(KEY_TRANSLATIONS_RUNNING));
                    if (!transAlreadyRunning) {
                        // Noch nicht, also Flag setzen, dass es jetzt ausgeführt wird
                        etkDbs.setKeyValue(KEY_TRANSLATIONS_RUNNING, SQLStringConvert.booleanToPPString(true));
                        resetFlag = true;
                        DictMetaListContainer dictMetaListContainer = DictShowTextForTranslation.showTexteForTranslation(owner);
                        if (dictMetaListContainer != null) {
                            iPartsDictTransJobHelper.storeTransJob(owner, dictMetaListContainer);
                        }
//                        boolean isPSKErrorHandling = true;
//                        DictMetaListContainer dictMetaListContainer = DictShowTextForTranslation.showTexteForTranslation(owner, true);
//                        if (dictMetaListContainer != null) {
//                            if (!isPSKErrorHandling) {
//                                iPartsDictTransJobHelper.storeTransJob(owner, dictMetaListContainer);
//                            }
//                        }
                    } else {
                        // Wird schon ausgeführt -> Meldung, dass Übernahme jetzt nicht möglich ist
                        MessageDialog.show("!!Die Übernahme neuer Texte in den Übersetzungsumfang wird bereits von einem anderen Benutzer ausgeführt.",
                                           TranslationHandler.translate(DictShowTextForTranslation.TITLE) + "...");
                    }
                } finally {
                    if (resetFlag) {
                        etkDbs.setKeyValue(KEY_TRANSLATIONS_RUNNING, SQLStringConvert.booleanToPPString(false));
                    }
                }
            }
        };
    }

    private EtkFunction showTransJobs() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                DictShowTransJobs.showDictTransJobData(owner);
            }
        };
    }

    private EtkFunction importNewImage() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                EditImportImageForm editImportImageForm = new EditImportImageForm(JavaViewerApplication.getInstance().getProject(),
                                                                                  new ImageFileImporter(JavaViewerApplication.getInstance().getProject()));
                editImportImageForm.showModal();
            }
        };
    }

    private EtkFunction setShowAggregatesInAllProducts(String menuItemText) {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                GuiMenuItem menuItem = owner.getConnector().getMenuManager().getMenuItem(MenuManager.MENU_NAME_OPTIONS, menuItemText);
                if (menuItem instanceof GuiCheckboxMenuItem) {
                    // in UserConfig abspeichern
                    boolean isSelected = ((GuiCheckboxMenuItem)menuItem).isSelected();
                    owner.getConnector().getProject().getUserSettings().setBoolValues(iPartsUserSettingsConst.REL_SHOW_AGGREGATES_IN_ALL_PRODUCTS,
                                                                                      isSelected);

                    // In iPartsProduct die Anzeige der Aggregate für diese Session auf den Wert der Checkbox setzen,
                    // danach die Favoriten-Erreichbarkeit zurücksetzen sowie einen FilterChangedEvent feuern, damit sich
                    // der Baugruppenbaum und die Stückliste neu aufbauen
                    iPartsProduct.setProductStructureWithAggregatesForSession(isSelected);
                    ModuleSearchCache.clearCacheForSession(Session.get());

                    // Bisherigen NavigationPath merken und später den korrigierten NavigationPath wieder setzen
                    MechanicFormIConnector mechanicFormConnector = null;
                    NavigationPath correctedNavigationPath = null;
                    List<EtkDataPartListEntry> selectedPartListEntries = null;
                    JavaViewerMainWindow mainWindow = owner.getConnector().getMainWindow();
                    if (mainWindow != null) {
                        List<MechanicFormIConnector> mechanicConnectors = mainWindow.getMechanicConnectors();
                        if (!mechanicConnectors.isEmpty()) {
                            mechanicFormConnector = mechanicConnectors.get(0);
                            NavigationPath navigationPath = mechanicFormConnector.getCurrentNavigationPath();
                            correctedNavigationPath = iPartsPlugin.correctNavigationPathForProductStructures(navigationPath);
                            if (navigationPath.isLike(correctedNavigationPath)) {
                                // Navigationspfad musste nicht korrigiert werden
                                correctedNavigationPath = null;
                            } else {
                                selectedPartListEntries = new DwList<>(mechanicFormConnector.getSelectedPartListEntries());
                            }
                        }
                        mainWindow.getFavoritesManager().markFavoritesReachableInfoAsDirty();
                    }

                    iPartsFilter.get().clearCacheData();

                    // Temporär die Root-Baugruppe setzen, um die Meldung zu unterdrücken, dass die aktuelle Baugruppe für
                    // den Filter nicht gültig wäre
                    if (correctedNavigationPath != null) {
                        NavigationPath rootNavigationPath = new NavigationPath();
                        rootNavigationPath.addAssembly(AssemblyId.getRootId());
                        mechanicFormConnector.setCurrentNavigationPath(rootNavigationPath);
                    }

                    owner.getConnector().getProject().fireProjectEvent(new FilterChangedEvent(), true);

                    // Korrigierten Navigationspfad wieder setzen
                    if (correctedNavigationPath != null) {
                        NavigationPathValidEvent navigationPathValidEvent = new NavigationPathValidEvent(mechanicFormConnector.getRootAssemblyId(),
                                                                                                         correctedNavigationPath,
                                                                                                         mainWindow);
                        // Der Event wird vom MechanicConnector bearbeitet und korrigiert die laufenden Nummern
                        owner.getProject().fireProjectEvent(navigationPathValidEvent);

                        AssemblyId assemblyId = correctedNavigationPath.getLastPartListEntryInPath().getOwnerAssemblyId();
                        String kLfdnr = "";
                        if (!selectedPartListEntries.isEmpty()) {
                            kLfdnr = selectedPartListEntries.get(0).getAsId().getKLfdnr();
                        }
                        GotoPartWithPartialPathEvent gotoEvent = new GotoPartWithPartialPathEvent(correctedNavigationPath,
                                                                                                  assemblyId, kLfdnr, false,
                                                                                                  true, true, owner);
                        owner.getProject().fireProjectEvent(gotoEvent);
                    }

                    // "Suche in" in allen Suchformularen zurücksetzen
                    if (mainWindow != null) {
                        for (AbstractJavaViewerMainFormContainer searchForm : mainWindow.getFormsFromClass(SearchForm.class)) {
                            for (SearchBaseForm searchBaseForm : ((SearchForm)searchForm).getSearchForms()) {
                                searchBaseForm.resetSelectorCatalogRoot();
                            }
                        }
                    }
                }
            }
        };
    }

    private EtkFunction setHideEmptyTUs(String menuItemText) {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                GuiMenuItem menuItem = owner.getConnector().getMenuManager().getMenuItem(MenuManager.MENU_NAME_OPTIONS, menuItemText);
                if (menuItem instanceof GuiCheckboxMenuItem) {
                    // in UserConfig abspeichern
                    boolean isSelected = ((GuiCheckboxMenuItem)menuItem).isSelected();
                    owner.getConnector().getProject().getUserSettings().setBoolValues(iPartsUserSettingsConst.REL_HIDE_EMPTY_TUS,
                                                                                      isSelected);

                    // Die Favoriten-Erreichbarkeit zurücksetzen sowie einen FilterChangedEvent feuern, damit sich der Baugruppenbaum
                    // und die Stückliste neu aufbauen
                    ModuleSearchCache.clearCacheForSession(Session.get());
                    JavaViewerMainWindow mainWindow = owner.getConnector().getMainWindow();
                    if (mainWindow != null) {
                        mainWindow.getFavoritesManager().markFavoritesReachableInfoAsDirty();
                    }

                    iPartsFilter.get().clearCacheData();
                    owner.getConnector().getProject().fireProjectEvent(new FilterChangedEvent(), true);
                }
            }
        };
    }

    private EtkFunction showHmMSmNodes(final String menuItemText) {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                GuiMenuItem menuItem = owner.getConnector().getMenuManager().getMenuItem(MenuManager.MENU_NAME_OPTIONS, menuItemText);
                if (menuItem instanceof GuiCheckboxMenuItem) {
                    owner.getConnector().getProject().getUserSettings().setBoolValues(iPartsUserSettingsConst.REL_SHOW_HIDDEN_HMMSM_NODES,
                                                                                      ((GuiCheckboxMenuItem)menuItem).isSelected());
                    owner.getConnector().getProject().fireProjectEvent(new DataChangedEvent(null), true);
                }
            }
        };
    }

    private EtkFunction showEinPASMapping(final String menuItemText) {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                GuiMenuItem menuItem = owner.getConnector().getMenuManager().getMenuItem(IPARTS_MENU_NAME_TEST, menuItemText);
                if (menuItem instanceof GuiCheckboxMenuItem) {
                    Session.get().setAttribute(iPartsPlugin.SESSION_KEY_SHOW_EINPAS_MAPPING, ((GuiCheckboxMenuItem)menuItem).isSelected());
                    owner.getConnector().getProject().fireProjectEvent(new DataChangedEvent(null), true);
                }
            }
        };
    }

    private EtkFunction showNonAsRelevantEntries(final String menuItemText) {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                GuiMenuItem menuItem = owner.getConnector().getMenuManager().getMenuItem(MenuManager.MENU_NAME_OPTIONS, menuItemText);
                if (menuItem instanceof GuiCheckboxMenuItem) {
                    // in UserConfig abspeichern
                    owner.getConnector().getProject().getUserSettings().setBoolValues(iPartsUserSettingsConst.REL_DIALOG_HIDE_NON_AS_REL,
                                                                                      ((GuiCheckboxMenuItem)menuItem).isSelected());
                    owner.getConnector().getProject().fireProjectEvent(new DataChangedEvent(null), true);
                }
            }
        };
    }

    private EtkFunction showLastApprovedEntries(final String menuItemText) {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                GuiMenuItem menuItem = owner.getConnector().getMenuManager().getMenuItem(MenuManager.MENU_NAME_OPTIONS, menuItemText);
                if (menuItem instanceof GuiCheckboxMenuItem) {
                    if (menuItem.isVisible()) {
                        // in UserConfig abspeichern
                        owner.getConnector().getProject().getUserSettings().setBoolValues(iPartsUserSettingsConst.REL_DIALOG_SHOW_LAST_APPROVED,
                                                                                          ((GuiCheckboxMenuItem)menuItem).isSelected());
                        owner.getConnector().getProject().fireProjectEvent(new DataChangedEvent(null), true);
                    }
                }
            }
        };
    }

    private EtkFunction setUltraEditView(final String menuItemText, final String subMenuColsText, final String subMenuRowsText) {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                GuiMenuItem menuItem = owner.getConnector().getMenuManager().getMenuItem(MenuManager.MENU_NAME_OPTIONS, menuItemText);
                if (menuItem instanceof GuiCheckboxMenuItem) {
                    // in UserConfig abspeichern
                    boolean isSelected = ((GuiCheckboxMenuItem)menuItem).isSelected();
                    owner.getConnector().getProject().getUserSettings().setBoolValues(iPartsUserSettingsConst.REL_ULTRA_EDIT_VIEW,
                                                                                      isSelected);
                    menuItem = owner.getConnector().getMenuManager().getMenuItem(MenuManager.MENU_NAME_OPTIONS, subMenuColsText);
                    if (menuItem != null) {
                        menuItem.setVisible(isSelected);
                    }
                    menuItem = owner.getConnector().getMenuManager().getMenuItem(MenuManager.MENU_NAME_OPTIONS, subMenuRowsText);
                    if (menuItem != null) {
                        menuItem.setVisible(isSelected);
                    }
                }
            }
        };
    }

    private EtkFunction setUltraEditViewAllow(final String menuItemText, final String cfgName) {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                GuiMenuItem menuItem = owner.getConnector().getMenuManager().getMenuItem(MenuManager.MENU_NAME_OPTIONS, menuItemText);
                if (menuItem instanceof GuiCheckboxMenuItem) {
                    // in UserConfig abspeichern
                    owner.getConnector().getProject().getUserSettings().setBoolValues(cfgName,
                                                                                      ((GuiCheckboxMenuItem)menuItem).isSelected());
                }
            }
        };
    }

    private EtkFunction setSvgIsPreferred(String menuItemText) {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                GuiMenuItem menuItem = owner.getConnector().getMenuManager().getMenuItem(MenuManager.MENU_NAME_OPTIONS, menuItemText);
                if (menuItem instanceof GuiCheckboxMenuItem) {
                    // in UserConfig abspeichern
                    boolean isSelected = ((GuiCheckboxMenuItem)menuItem).isSelected();
                    owner.getConnector().getProject().getUserSettings().setBoolValues(iPartsUserSettingsConst.REL_SVG_IS_PREFERRED,
                                                                                      isSelected);
                    // Module im Edit müssen neu geladen werden
                    // Alles andere reagiert auf den DataChangedEvent
                    owner.getConnector().getProject().fireProjectEvent(new DataChangedEvent(null), true);
                    iPartsEditPlugin.reloadAllModulesInEdit(owner.getConnector());
                }
            }
        };
    }

    private EtkFunction setSingleEdit(final String menuItemText) {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                GuiMenuItem menuItem = owner.getConnector().getMenuManager().getMenuItem(MenuManager.MENU_NAME_OPTIONS, menuItemText);
                if (menuItem instanceof GuiCheckboxMenuItem) {
                    // in UserConfig abspeichern
                    boolean isSelected = ((GuiCheckboxMenuItem)menuItem).isSelected();
                    owner.getConnector().getProject().getUserSettings().setBoolValues(iPartsUserSettingsConst.REL_SINGLE_EDIT_VIEW,
                                                                                      isSelected);
                    owner.getConnector().getProject().fireProjectEvent(new DataChangedEvent(null), true);
                }
            }
        };
    }

    private EtkFunction showMQEventDialog() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                MQEventForm eventWindow = new MQEventForm();
                eventWindow.show();
            }
        };
    }

    private EtkFunction showTruckBOMFoundationWebservicesForm() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                TruckBOMFoundationWebservicesForm webservicesWindow = new TruckBOMFoundationWebservicesForm(getProject());
                webservicesWindow.show();
            }
        };
    }

    private EtkFunction showBookmarkForSelection() {
        return new EtkFunction(EventListenerOptions.SYNCHRON_EVENT) {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                Map<String, String> additionalStartParametersMap = null;
                iPartsFilter filter = iPartsFilter.get();
                if (filter.getSwitchboardState().isMainSwitchActive()) { // IdentCode nur bei aktivem Filter-Hauptschalter zur Bookmark-URL hinzufügen
                    // IdentCode ermitteln (VIN, FIN oder Baumuster)
                    String identCode = null;
                    AbstractDataCard dataCard = filter.getCurrentDataCard();
                    if (dataCard.isVehicleDataCard()) {
                        VehicleDataCard vehicleDataCard = (VehicleDataCard)dataCard;
                        String vin = vehicleDataCard.getVin();
                        if (StrUtils.isValid(vin)) {
                            identCode = vin;
                        } else {
                            FinId finId = vehicleDataCard.getFinId();
                            if ((finId != null) && finId.isValidId()) {
                                identCode = finId.getFIN();
                            }
                        }
                    }
                    if (identCode == null) {
                        String filterModelNo = dataCard.getFilterModelNo();
                        if (StrUtils.isValid(filterModelNo)) {
                            identCode = filterModelNo;
                        }
                    }

                    if (identCode != null) {
                        additionalStartParametersMap = new LinkedHashMap<>();
                        additionalStartParametersMap.put(START_PARAMETER_IDENT_CODE, identCode);
                    }
                }

                BookmarkHelper.copyCurrentBookmarkToClipboard(owner.getConnector().getActiveForm(), additionalStartParametersMap,
                                                              "http://s-daimler:8080/iparts");
            }
        };
    }

    private EtkFunction showSupportURLInput() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                SupportURLInputDialog dialog = new SupportURLInputDialog(owner.getConnector(), owner.getConnector().getActiveForm());
                dialog.show();
            }
        };
    }

    @Override
    public AbstractGuiControl modifyEditControlFactory(EditControlFactoryValues values, EditControlFactoryOptions options) {
        if (options.isEdit || options.isSearch) {
            if (values.field.isMultiLanguage() && options.isEdit) {
                if (values.field.getName().equals(FIELD_M_ADDTEXT) && options.isEdit) {
                    iPartsGuiNeutralTextCompleteEditControl control = new iPartsGuiNeutralTextCompleteEditControl();
                    EditControlFactory.setDefaultLayout(control);
                    return control;
                } else {
                    GuiMultiLangEdit control = new GuiMultiLangEdit();
                    EditControlFactory.setDefaultLayout(control);
                    control.setStartLanguage(Language.findLanguage(values.dbLanguage));
                    return control;
                }
            } else if (isCodeField(values)) {
                // Im Search Modus, soll normales Textfeld genommen werden, da man mit Wildcards suchen kann, die im
                // iPartsGuiCodeTextField nicht erlaubt sind
                if (options.isSearch) {
                    if (values.editCreateMode == EditCreateMode.ecmTableColumnFilter) {
                        // spezial TextField for Code bei Gridspaltenfilter
                        iPartsGuiCodeGridFilterTextField control = new iPartsGuiCodeGridFilterTextField(values.initValue);
                        EditControlFactory.setDefaultLayout(control);
                        return control;
                    } else {
                        GuiTextField control = new GuiTextField(values.initValue);
                        EditControlFactory.setDefaultLayout(control);
                        return control;
                    }
                } else {
                    iPartsGuiCodeTextField control = new iPartsGuiCodeTextField(values.initValue);
                    control.setBeautified(!options.isSearch);
                    EditControlFactory.setDefaultLayout(control);
                    return control;
                }
            } else if (isStatusField(values)) {
                AbstractGuiControl guiControl = null;
                if (options.isEdit) {
                    // Nur noch die möglichen nächsten Status Werte übrig lassen inkl. dem aktuell ausgewählten
                    EditControlFactory factory = EditControlFactory.createDefault(values.project, values.tableName, values.fieldName,
                                                                                  values.dbLanguage, values.viewerLanguage,
                                                                                  values.initValue, null);
                    guiControl = factory.getControl();
                    if ((guiControl instanceof EnumRComboBox) || (guiControl instanceof EnumComboBox)) {
                        iPartsDataReleaseState releaseState = iPartsDataReleaseState.getTypeByDBValue(values.initValue);
                        List<String> nextEditStatesDBValues = releaseState.getNextEditStatesDBValues();
                        nextEditStatesDBValues.add(releaseState.getDbValue());

                        if (guiControl instanceof EnumRComboBox) {
                            EnumRComboBox control = (EnumRComboBox)guiControl;
                            List<String> tokenCopy = new DwList<>(control.getTokens());
                            for (String token : tokenCopy) {
                                if (!nextEditStatesDBValues.contains(token)) {
                                    int indexByToken = control.getIndexByToken(token);
                                    control.removeItem(indexByToken);
                                }
                            }
                        } else {
                            // Kopierter Part, der wieder entfernt werden kann, wenn GuiCombobox und RCombobox zusammengeführt wurden
                            EnumComboBox control = (EnumComboBox)guiControl;
                            List<String> tokenCopy = new DwList<>(control.getTokens());
                            for (String token : tokenCopy) {
                                if (!nextEditStatesDBValues.contains(token)) {
                                    int indexByToken = control.getIndexByToken(token);
                                    control.removeItem(indexByToken);
                                }
                            }
                        }
                    }
                }
                return guiControl;
            } else if (values.field.getType() == EtkFieldType.fePicture) {
                iPartsGuiPictureIdsTextField control = new iPartsGuiPictureIdsTextField();
                EditControlFactory.setDefaultLayout(control);
                control.setConnector(JavaViewerApplication.getInstance().getMainConnector());
                control.setText(values.initValue);
                return control;
            } else if (values.field.getType() == EtkFieldType.feMemo) {
                final GuiTextArea edit = new GuiTextArea();
                EditControlFactory.setDefaultLayout(edit);
                edit.setLineWrap(true);
                edit.setScrollToVisible(true);
                edit.setName(values.tableName + "." + values.fieldName);
                GuiScrollPane pane = new GuiScrollPane(true, true, edit);
                pane.setMinimumHeight(3 * HTMLUtils.getTextDimension(pane.getFont(), "Wg").getHeight()); // ungefähr 3 Zeilen
                edit.setText(values.initValue);
                final GuiScrollPane parentPane = pane;
                // Eventlistener für TextArea, wenn sie den Focus bekommt
                EventListener listener = new EventListenerFireOnce(Event.ON_FOCUS_GAINED_EVENT, edit) {
                    @Override
                    public boolean isFireOnceValid(Event event) {
                        // hat die ScrollPane einen ON_CHANGE_EVENT erhalten?
                        return parentPane.hasEventListener(Event.ON_CHANGE_EVENT);
                    }

                    @Override
                    public void fireOnce(Event event) {
                        // kopiere alle ON_CHANGE_EVENT-Listener in die TextArea
                        parentPane.copyEventListeners(edit, Event.ON_CHANGE_EVENT);
                        // entferne den ON_CHANGE_EVENT aus der ScrollPane
                        parentPane.removeEventListeners(Event.ON_CHANGE_EVENT);
                    }
                };
                edit.addEventListener(listener);
                return pane;
            } else if (values.field.getName().equals(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT) && options.isEdit) {
                iPartsGuiCombTextCompleteEditControl control = new iPartsGuiCombTextCompleteEditControl();
                EditControlFactory.setDefaultLayout(control);
                control.setText(values.initValue);
                return control;
            } else if (values.field.getName().equals(iPartsDataVirtualFieldsDefinition.RETAIL_ADD_TEXT) && options.isEdit) {
                iPartsMultiLangGuiCombTextButtonField control = new iPartsMultiLangGuiCombTextButtonField();
                control.setEditable(false);
                EditControlFactory.setDefaultLayout(control);
                control.setText(values.initValue);
                return control;
            } else if (values.field.getName().equals(iPartsDataVirtualFieldsDefinition.RETAIL_TEXT_NEUTRAL) && options.isEdit) {
                iPartsNeutralGuiCombTextButtonField control = new iPartsNeutralGuiCombTextButtonField();
                control.setEditable(false);
                EditControlFactory.setDefaultLayout(control);
                control.setText(values.initValue);
                return control;
            } else if (values.fieldName.equals(iPartsConst.FIELD_K_POS) && options.isEdit) {
                iPartsGuiHotSpotTextField control = new iPartsGuiHotSpotTextField(values.initValue, true);
                EditControlFactory.setDefaultLayout(control);
                return control;
            } else if ((values.fieldName.equals(iPartsConst.FIELD_K_MODEL_VALIDITY) || values.fieldName.equals(iPartsConst.FIELD_I_MODEL_VALIDITY)
                        || values.fieldName.equals(iPartsConst.FIELD_DME_MODEL_VALIDITY)) && options.isEdit) {
                iPartsGuiModelSelectTextField control = new iPartsGuiModelSelectTextField(values.project);
                EditControlFactory.setDefaultLayout(control);
                return control;
            } else if ((values.fieldName.equals(iPartsConst.FIELD_K_SA_VALIDITY) || values.fieldName.equals(iPartsConst.FIELD_I_SAA_CONSTKIT_VALIDITY)
                        || values.fieldName.equals(iPartsConst.FIELD_DME_SAA_VALIDITY)) && options.isEdit) {
                iPartsGuiSAABkSelectTextField control = new iPartsGuiSAABkSelectTextField(values.project);
                EditControlFactory.setDefaultLayout(control);
                return control;
            } else if ((values.fieldName.equals(iPartsConst.FIELD_K_PSK_VARIANT_VALIDITY) || values.fieldName.equals(iPartsConst.FIELD_I_PSK_VARIANT_VALIDITY))
                       && options.isEdit) {
                iPartsGuiPSKVariantsSelectTextField control = new iPartsGuiPSKVariantsSelectTextField(values.project);
                EditControlFactory.setDefaultLayout(control);
                return control;
            } else if (values.fieldName.equals(iPartsConst.FIELD_K_COUNTRY_VALIDITY) && options.isEdit) {
                iPartsGuiCountrySelectionBox control = new iPartsGuiCountrySelectionBox(values.project, GuiComboBoxMode.Mode.CHECKBOX, "");
                EditControlFactory.setDefaultLayout(control);
                control.setActToken(values.initValue);
                return control;
            } else if (values.fieldName.equals(iPartsConst.FIELD_K_SPEC_VALIDITY) && options.isEdit) {
                iPartsGuiDoubleDelimitedTextField control = new iPartsGuiDoubleDelimitedTextField();
                EditControlFactory.setDefaultLayout(control);
                control.setText(values.initValue);
                return control;
            } else if (values.fieldName.equals(iPartsConst.FIELD_DPF_FACTORY_NO)
                       || values.fieldName.equals(iPartsDataVirtualFieldsDefinition.DP_VALID_FACTORIES)) {
                iPartsGuiFactorySelectTextField control = new iPartsGuiFactorySelectTextField(values.project);
                EditControlFactory.setDefaultLayout(control);
                return control;
            } else if (values.fieldName.equals(iPartsDataVirtualFieldsDefinition.DP_VARIANTS)) {
                iPartsGuiProductVariantsSelectTextField control = new iPartsGuiProductVariantsSelectTextField(values.project);
                EditControlFactory.setDefaultLayout(control);
                return control;
            } else if (values.fieldName.equals(FIELD_DFD_PEMA) || values.fieldName.equals(FIELD_DFD_PEMB) || values.fieldName.equals(FIELD_DCCF_PEMA)
                       || values.fieldName.equals(FIELD_DCCF_PEMB)) {
                iPartsGuiPEMSelectionButtonTextField control = new iPartsGuiPEMSelectionButtonTextField(values.project, values.initValue);
                EditControlFactory.setDefaultLayout(control);
                return control;
            } else if (values.fieldName.equals(iPartsConst.FIELD_DAO_BST_ID)) {
                return new iPartsGuiWorkOrderSelectComboBox(iPartsUserAdminDb.getLoginUserIdForSession(), values.initValue,
                                                            getProject());
            } else if (values.fieldName.equals(MasterDataSeriesForm.FIELD_DS_SOP)) {
                return new iPartsGuiSeriesSOPField();
            } else if (values.fieldName.equals(FIELD_DS_AA_WO_FACTORY_DATA)) {
                iPartsGuiSeriesAACheckComboBox seriesComboBox = new iPartsGuiSeriesAACheckComboBox();
                seriesComboBox.setIgnoreBlankTexts(true);
                return seriesComboBox;
            } else if (values.editCreateMode == EditCreateMode.ecmTableColumnFilter) {
                // Nur für Column-Filter
                List<String> filterableFieldList = null;
                // nur wenn Tabellenfilter angefordert
                if (values.tableName.equals(TABLE_DA_SERIES_EVENTS)) {
                    filterableFieldList = new DwList<>(EditSeriesEventsForm.SPECIAL_TABLEFILTER_FIELDS);
                } else if (values.tableName.equals(TABLE_KATALOG)) {
                    filterableFieldList = new DwList<>();
                    filterableFieldList.add(FIELD_K_EVENT_FROM);
                    filterableFieldList.add(FIELD_K_EVENT_TO);
                    filterableFieldList.add(iPartsDataVirtualFieldsDefinition.DIALOG_DD_EVENT_FROM);
                    filterableFieldList.add(iPartsDataVirtualFieldsDefinition.DIALOG_DD_EVENT_TO);
                } else if (values.tableName.equals(TABLE_DA_COLORTABLE_CONTENT)) {
                    filterableFieldList = new DwList<>(iPartsRelatedInfoVariantsToPartDataForm.SPECIAL_TABLEFILTER_FIELDS);
                } else if (values.tableName.equals(TABLE_DA_AUTHOR_ORDER)) {
                    filterableFieldList = new DwList<>();
                    filterableFieldList.add(iPartsDataVirtualFieldsDefinition.DAO_TOTAL_PICORDERS_STATE);
                }
                if (filterableFieldList != null) {
                    if (filterableFieldList.contains(values.fieldName)) {
                        // Trick um im Tabellenfilter ein SetOfEnum Eingabefeld zu erzeugen, dass als Tokens
                        // die Werte aus der zugehörigen Spalte der Tabelle enthält
                        values.field = values.field.cloneMe();  // zur Sicherheit, damit die Originalwerte sich nicht ändern
                        values.field.setType(EtkFieldType.feSetOfEnum);  // behaupte, das Feld ist ein SetOfEnum
                        options.handleAsSetOfEnum = true;  // und soll als SetOfEnum behandelt werden
                        options.searchDisjunctive = true;
                        // alles weitere übernimmt EditControlFactory und das FilterInterface
                    }
                }
                // Ende Nur für Column-Filter
            } else if (values.fieldName.equals(FIELD_K_EVENT_FROM) || values.fieldName.equals(FIELD_K_EVENT_TO)
                       || values.fieldName.equals(FIELD_I_EVENT_FROM) || values.fieldName.equals(FIELD_I_EVENT_TO)
                       || values.fieldName.equals(FIELD_DCTC_EVENT_FROM_AS) || values.fieldName.equals(FIELD_DCTC_EVENT_TO_AS)) {
                // iPartsGuiEventSelectComboBox benötigt eine Baureihe, die in der Suche (Tabellenspaltenfilter) nicht bekannt
                // ist -> nur im Edit die iPartsGuiEventSelectComboBox verwenden
                if (options.isEdit) {
                    return new iPartsGuiEventSelectComboBox();
                }
            } else if (values.fieldName.equals(FIELD_DS_HIERARCHY)) {
                EnumRComboBox comboBox = new EnumRComboBox();
                EditControlFactory.setDefaultLayout(comboBox);
                comboBox.setEnumTexte(values.project, "Hierarchy", values.project.getDBLanguage());
                // Erst leeren Wert entfernen
                comboBox.removeItem("");
                // Leeren Wert mit neuem Text setzen
                comboBox.addToken("", TranslationHandler.translate(ADDITIONAL_ENUM_VALUE_FOR_SERIES_HIERARCHY), null, 0, false);
                comboBox.setSelectedItem(values.initValue);
                return comboBox;
            } else if (values.fieldName.equals(FIELD_DP_FINS)) {
                iPartsGuiFinSelectTextField finField = new iPartsGuiFinSelectTextField(getProject());
                finField.setText(values.initValue);
                return finField;
            }
        }
        return null;
    }

    private boolean isCodeField(EditControlFactoryValues values) {
        return isCodeField(TableAndFieldName.make(values.tableName, values.fieldName));
    }

    public static boolean isCodeField(String tableAndFieldName) {
        return StrUtils.arrayIndexOf(KNOWN_CODE_FIELDS, tableAndFieldName) >= 0;
    }

    private boolean isStatusField(EditControlFactoryValues values) {
        return isStatusField(TableAndFieldName.make(values.tableName, values.fieldName));
    }

    public static boolean isStatusField(String tableAndFieldName) {
        return StrUtils.arrayIndexOf(KNOWN_EDITABLE_STATUS_FIELDS, tableAndFieldName) >= 0;
    }

    @Override
    public String getTextFromEditControl(AbstractGuiControl control, EditControlFactoryValues values, EditControlFactoryOptions options) {
        if (values.field.getName().equals(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT) && (control instanceof iPartsGuiCombTextCompleteEditControl)) {
            iPartsGuiCombTextCompleteEditControl combTextControl = (iPartsGuiCombTextCompleteEditControl)control;
            return combTextControl.getText();
        } else if ((values.field.getType() == EtkFieldType.feMemo) && (control instanceof GuiScrollPane)) {
            if (!control.getChildren().isEmpty() && (control.getChildren().get(0) instanceof GuiTextArea)) {
                GuiTextArea textArea = (GuiTextArea)control.getChildren().get(0);
                return textArea.getText();
            }
        } else if (values.fieldName.equals(EditAuthorOrderView.OBJECT_COLS.OBJECT_TYPE.getFieldName())) {
            return ((RComboBox)control).getSelectedItem();
        }

        return null;
    }

    @Override
    public void saveAdditionalDataForEditControl(AbstractGuiControl control, EditControlFactoryValues values, EditControlFactoryOptions options,
                                                 DBDataObjectAttributes attributes) {
        String fieldName = values.field.getName();
        if (fieldName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT) && (control instanceof iPartsGuiCombTextCompleteEditControl)) {
            iPartsEditCombTextHelper.storeDataCombList(values.project, (iPartsGuiCombTextCompleteEditControl)control, null);
        } else if (fieldName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_EVAL_PEM_FROM_CALCULATED) && (control instanceof GuiCheckbox)) {
            if (control.isEnabled()) { // Disabled, falls das "PEM ab auswerten"-Flag durch Ersetzungen gesetzt wird
                attributes.getField(FIELD_K_EVAL_PEM_FROM).setValueAsBoolean(((GuiCheckbox)control).isSelected(), DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
            }
        } else if (fieldName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_EVAL_PEM_TO_CALCULATED) && (control instanceof GuiCheckbox)) {
            if (control.isEnabled()) { // Disabled, falls das "PEM bis auswerten"-Flag durch Ersetzungen gesetzt wird
                attributes.getField(FIELD_K_EVAL_PEM_TO).setValueAsBoolean(((GuiCheckbox)control).isSelected(), DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
            }
        } else if (fieldName.equals(FIELD_M_ADDTEXT) && (control instanceof iPartsGuiNeutralTextCompleteEditControl)) {
            if (control.isEnabled()) {
                if (attributes.getField(FIELD_M_ADDTEXT).isModified()) {
                    attributes.getField(FIELD_M_ADDTEXT_EDITED).setValueAsBoolean(true, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                }
            }
        }
    }

    @Override
    public boolean setTextForEditControl(AbstractGuiControl control, EditControlFactoryValues values, EditControlFactoryOptions options,
                                         String text, boolean withEventListeners) {
        if ((values.field.getType() == EtkFieldType.feMemo) && (control instanceof GuiScrollPane)) {
            if (!control.getChildren().isEmpty() && (control.getChildren().get(0) instanceof GuiTextArea)) {
                GuiTextArea textArea = (GuiTextArea)control.getChildren().get(0);
                textArea.setText(text);
                return true;
            }
        }
        return false;
    }

    @Override
    public EtkMultiSprache getTextFromMultiLangEditControl(AbstractGuiControl control, EditControlFactoryValues values,
                                                           EditControlFactoryOptions options) {
        if (control instanceof GuiMultiLangEdit) {
            return ((GuiMultiLangEdit)control).getMultiLanguage();
        } else if (control instanceof iPartsGuiNeutralTextCompleteEditControl) {
            return ((iPartsGuiNeutralTextCompleteEditControl)control).getMultiLanguage();
        }
        return null;
    }

    @Override
    public boolean setTextForMultiLangControl(AbstractGuiControl control, EditControlFactoryValues values, EditControlFactoryOptions options,
                                              String text, String language, boolean withEventListeners) {
        if (control instanceof GuiMultiLangEdit) {
            ((GuiMultiLangEdit)control).setMultiText(text, language);
            return true;
        } else if (control instanceof iPartsGuiNeutralTextCompleteEditControl) {
            ((iPartsGuiNeutralTextCompleteEditControl)control).setMultiText(text, language);
            return true;
        }
        return false;
    }

    @Override
    public boolean isReadOnlyForEditControl(AbstractGuiControl control, EditControlFactoryValues values, EditControlFactoryOptions options,
                                            boolean value, DBDataObjectAttributes attributes) {
        String fieldName = values.field.getName();
        if (fieldName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_EVAL_PEM_FROM_CALCULATED)) {
            // Berechnetes Flag "PEM ab auswerten" ist true, aber das in der DB nicht -> Flag wird durch Ersetzung gesetzt
            // und darf nicht verändert werden
            if (!attributes.getField(FIELD_K_EVAL_PEM_FROM).getAsBoolean()) {
                // Virtuelles Feld kann fehlen (z.B. bei Erzeugen eines neues Stücklisteneintrags aus einem Material)
                DBDataObjectAttribute evalPemFromCalcAttr = attributes.getField(iPartsDataVirtualFieldsDefinition.RETAIL_EVAL_PEM_FROM_CALCULATED, false);
                if ((evalPemFromCalcAttr != null) && evalPemFromCalcAttr.getAsBoolean()) {
                    control.setTooltip("!!Unveränderbar gesetzt aufgrund von Ersetzungen");
                    return true;
                }
            }
        } else if (fieldName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_EVAL_PEM_TO_CALCULATED)) {
            // Berechnetes Flag "PEM bis auswerten" ist true, aber das in der DB nicht -> Flag wird durch Ersetzung gesetzt
            // und darf nicht verändert werden
            if (!attributes.getField(FIELD_K_EVAL_PEM_TO).getAsBoolean()) {
                // Virtuelles Feld kann fehlen (z.B. bei Erzeugen eines neues Stücklisteneintrags aus einem Material)
                DBDataObjectAttribute evalPemToCalcAttr = attributes.getField(iPartsDataVirtualFieldsDefinition.RETAIL_EVAL_PEM_TO_CALCULATED, false);
                if ((evalPemToCalcAttr != null) && evalPemToCalcAttr.getAsBoolean()) {
                    control.setTooltip("!!Unveränderbar gesetzt aufgrund von Ersetzungen");
                    return true;
                }
            }
        } else if (fieldName.equals(FIELD_K_ENTRY_LOCKED)) {
            // Positionen für den Edit sperren darf man nur außerhalb vom Edit und auch nur, wenn man das Recht hat.
            // Gesperrt wird somit nicht über den Edit Dialog, sondern über Menüpunkte.
            return true;
        }

        return false;
    }

    @Override
    public boolean setReadOnlyForEditControl(AbstractGuiControl control, EditControlFactoryValues values, EditControlFactoryOptions options,
                                             boolean value) {
        if (control instanceof GuiMultiLangEdit) {
            ((GuiMultiLangEdit)control).setReadOnly(value);
            return true;
        } else if ((values.field.getType() == EtkFieldType.feMemo) && (control instanceof GuiScrollPane)) {
            if (!control.getChildren().isEmpty() && (control.getChildren().get(0) instanceof GuiTextArea)) {
                GuiTextArea textArea = (GuiTextArea)control.getChildren().get(0);
                textArea.setEditable(!value);
                return true;
            }
        } else if (control instanceof iPartsGuiPictureIdsTextField) {
            ((iPartsGuiPictureIdsTextField)control).setEditable(!value);
            return true;
        } else if (control instanceof iPartsGuiFactorySelectTextField) {
            ((iPartsGuiFactorySelectTextField)control).setEditable(!value);
            return true;
        }
        return false;
    }

    @Override
    public boolean hasEditorForVirtualField(String tableName, String fieldName) {
        if (fieldName != null) {
            if (fieldName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT)
                || fieldName.equals(iPartsDataVirtualFieldsDefinition.DP_VALID_FACTORIES)
                || fieldName.equals(iPartsDataVirtualFieldsDefinition.DP_VARIANTS)
                || fieldName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_EVAL_PEM_FROM_CALCULATED)
                || fieldName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_EVAL_PEM_TO_CALCULATED)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isForcedStartScreen() {
        if (pluginConfig.getConfigValueAsBoolean(CONFIG_FORCED_STARTSCREEN)) {
            return getProject().isEditModeActive();
        } else {
            return false;
        }
    }

    @Override
    public boolean isLanguageSelectionOptional() {
        return pluginConfig.getConfigValueAsBoolean(CONFIG_OPTIONAL_LANGUAGE_SELECTION);
    }

    @Override
    public boolean isStartScreenClickable() {
        return false;
    }

    @Override
    public boolean calculateLanguageSelectionNecessity(boolean missingParameters, boolean missingStartScreenParameters, boolean alwaysShowStartScreen, boolean showStartScreenWhenMissingParameters) {
        boolean languageSelection = false;
        if (missingParameters) {
            languageSelection = true;
            if (!alwaysShowStartScreen && !showStartScreenWhenMissingParameters) {
                languageSelection = false;
            }
        } else if (alwaysShowStartScreen) {
            languageSelection = true;
        }
        return languageSelection;
    }

    @Override
    public List<AbstractGuiControl> getToolbarControls(JavaViewerMainWindow mainWindow) {
        if (!getProject().isEditModeActive()) {
            return null;
        }

        List<AbstractGuiControl> controls = new ArrayList();

        GuiLabel label = new GuiLabel();
        label.setName("securityNoticeLabel");
        label.setText("!!Dieses System enthält vertrauliche Informationen");
        label.__internal_setGenerationDpi(96);
        label.setScaleForResolution(true);
        label.setForegroundColor(clPlugin_iPartsEdit_SecurityNoticeLabelForegroundColor.getColor());
        label.setFontSize(14);
        label.setFontStyle(DWFontStyle.BOLD_ITALIC);
        label.setPaddingRight(8); // sonst wird der BOLD + ITALIC Text zumindest im IE rechts abgeschnitten
        controls.add(label);

        if (DWLayoutManager.get().isLegacyMode()) {
            controls.add(createLogoutButton(mainWindow));
        }

        return controls;
    }

    @Override
    public ToolbarControlPlacement getControlPlacement(AbstractGuiControl control) {
        ToolbarControlPlacement placement = new ToolbarControlPlacement();

        if (control instanceof EtkToolbarButton) {
            placement.isRightOfLogo = true;
            placement.insetsLeft = 0;
        } else {
            placement.isRightOfLogo = pluginConfig.getConfigValueAsBoolean(CONFIG_SECURITY_NOTICE_RIGHT_OF_LOGO);
        }
        return placement;
    }

    private MainButtonInfo createLogoutButtonInfo(JavaViewerMainWindow mainWindow) {
        RButtonImages logoutImages = RButtonImages.createButtonImages(DefaultImages.logoutNormal, DefaultImages.logoutSelected,
                                                                      DefaultImages.logoutNormal);
        String title = DWLayoutManager.get().isResponsiveMode() ? "!!Logout" : "";
        return new MainButtonInfo(logoutImages, iPartsConst.IPARTS_MAIN_TOOLBAR_BUTTON_LOGOUT,
                                  EnumSet.of(MainButtonInfo.Options.FUNCTION_BUTTON),
                                  ToolbarButtonAlias.FORM_LOGIN.getAlias(), title, () -> doLogout(mainWindow));
    }

    private EtkToolbarButton createLogoutButton(JavaViewerMainWindow mainWindow) {
        MainButtonInfo logoutButtonInfo = createLogoutButtonInfo(mainWindow);
        ActionItem logoutActionItem = mainWindow.createMainToolbarActionItem(logoutButtonInfo, false);
        EtkToolbarButton logoutButton = EtkMainToolbarManager.createToolbarButton(logoutActionItem, mainWindow.getProject().getConfig());
        logoutButton.setTooltip("!!Logout");
        // Da der Button als Control in die Toolbar eingehangen wird und nicht über JavaViewerMainHeader.addToMainToolbar() muss man
        // sich um die EventListener selbst kümmern.
        EtkMainToolbarManager.addEventListenersToButton(logoutButton, logoutActionItem);
        // falls ein Text angezeigt werden soll, braucht man den zugehörigen Listener für den Sprachwechsel
        mainWindow.getProject().addAppEventListener(new ObserverCallback(mainWindow.getCallbackBinder(), UserInterfaceLanguageChangedEvent.class) {
            @Override
            public void callback(ObserverCall call) {
                logoutButton.setText(logoutActionItem.getText());
            }
        });
        return logoutButton;
    }

    private void doLogout(JavaViewerMainWindow mainWindow) {
        Session session = Session.get();
        if (session != null) {
            // Alternativen wären gewesen:
            // Umleitung: mainWindow.getGui().getGuiLogger().addAjaxCommand_evaluateJavascript("window.location.replace(\"http:////www.mercedes.de\");");
            // leere weiße Seite: mainWindow.getGui().getGuiLogger().addAjaxCommand_evaluateJavascript("window.location.replace(\"about:blank\");");
            String iconDE = DesignImage.getFlag("DE").getImage().getServeUrl();
            String iconEN = DesignImage.getFlag("EN").getImage().getServeUrl();
            String innerHTML = "<div style='font-size:18px; margin-top:150px'><img style='vertical-align:middle; margin-left:75px; margin-right:30px;' src='"
                               + iconDE + "'</img>Sie wurden erfolgreich abgemeldet.</div><br>" +
                               "<div style='font-size:18px; margin-top:30px'><img style='vertical-align:middle; margin-left:75px; margin-right:30px;' src='"
                               + iconEN + "'</img>You have been successfully logged out.</div>";
            mainWindow.getGui().getGuiLogger().addAjaxCommand_evaluateJavascript("document.body.innerHTML=\"" + innerHTML + "\"");
            mainWindow.getGui().getGuiLogger().addAjaxCommand_evaluateJavascript("window.onerror=\"\"");    // keine weiteren Fehler-Dialoge wegen abgelaufener Session bringen

            // Kommt der Logout zu schnell, wird die englische Flagge manchmal nicht angezeigt
            new Thread(() -> {
                Java1_1_Utils.sleep(1000);
                SessionManager.getInstance().destroySession(session);
            }).start();
        }
    }

    @Override
    public void modifyPartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        if (getProject().isEditModeActive()) {
            AssemblyListWithHistoryForm.modifyPartListPopupMenu(popupMenu, connector);
            EDSLowerStructureWithHistoryForm.modifyPartListPopupMenu(popupMenu, connector);
            VisualizeMatNrForm.modifyPartListPopupMenu(popupMenu, connector);
            VisualizeSAAHelper.modifyPartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoMasterDataForm.modifyPartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoMasterDataForm.modifyPartListPopupMenuForSA(popupMenu, connector);
            iPartsRelatedInfoCodeMasterDataForm.modifyPartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoInternalTextForPartDataForm.modifyPartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoModelMasterDataForm.modifyPartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoSAAsModelsDataForm.modifyPartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoProductFactoryDataForm.modifyPartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoWWPartsDataForm.modifyPartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoFootNoteDataForm.modifyPartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoVariantsToPartDataForm.modifyPartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoFactoryDataForm.modifyPartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoReplacementsDataForm.modifyPartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoReplacementsConstDataForm.modifyPartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoPicOrdersToPartDataForm.modifyPartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoAlternativeMaterialsForm.modifyPartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoBadCodeDataForm.modifyPartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoDialogUsageDataForm.modifyPartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoSeriesEventsDataForm.modifyPartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoConstructionKitDataForm.modifyPartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoWireHarnessDataForm.modifyPartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoSeriesCodesDataForm.modifyPartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoPrimusReplacementChainForm.modifyPartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoDialogMultistageRetailReplacementChainForm.modifyPartListPopupMenu(popupMenu, connector);
            ReportForConstructionNodesHelper.modifyPartListPopupMenu(popupMenu, connector);
            HmMSmNodeEditHelper.modifyPartListPopupMenu(popupMenu, connector);
            iPartsEditValidationHelper.modifyPartListPopupMenu(popupMenu, connector);
            EditModuleForm.modifyRetailPartListPopupMenu(popupMenu, connector);
            EditAuthorOrderView.modifyPartListPopupMenu(popupMenu, connector);
            iPartsMechanicFormWindow.modifyPartListPopupMenu(popupMenu, connector);
            EditConstPartListEntryContextMenu.modifyPartListPopupMenu(popupMenu, connector);
            AutoTransferPartListEntries.modifyPartListPopupMenu(popupMenu, connector);
            AutoTransferPartListEntriesExtended.modifyPartListPopupMenu(popupMenu, connector);
            AutoTransferPLEsExtendedWholeProduct.modifyPartListPopupMenu(popupMenu, connector);
        }
    }

    @Override
    public void updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        if (getProject().isEditModeActive()) {
            AssemblyListWithHistoryForm.updatePartListPopupMenu(popupMenu, connector);
            EDSLowerStructureWithHistoryForm.updatePartListPopupMenu(popupMenu, connector);
            VisualizeMatNrForm.updatePartListPopupMenu(popupMenu, connector);
            VisualizeSAAHelper.updatePartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoMasterDataForm.updatePartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoMasterDataForm.updatePartListPopupMenuForSA(popupMenu, connector);
            iPartsRelatedInfoCodeMasterDataForm.updatePartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoInternalTextForPartDataForm.updatePartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoModelMasterDataForm.updatePartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoSAAsModelsDataForm.updatePartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoProductFactoryDataForm.updatePartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoWWPartsDataForm.updatePartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoFootNoteDataForm.updatePartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoVariantsToPartDataForm.updatePartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoFactoryDataForm.updatePartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoReplacementsDataForm.updatePartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoReplacementsConstDataForm.updatePartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoPicOrdersToPartDataForm.updatePartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoAlternativeMaterialsForm.updatePartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoBadCodeDataForm.updatePartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoSeriesEventsDataForm.updatePartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoDialogUsageDataForm.updatePartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoConstructionKitDataForm.updatePartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoWireHarnessDataForm.updatePartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoSeriesCodesDataForm.updatePartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoPrimusReplacementChainForm.updatePartListPopupMenu(popupMenu, connector);
            iPartsRelatedInfoDialogMultistageRetailReplacementChainForm.updatePartListPopupMenu(popupMenu);
            ReportForConstructionNodesHelper.updatePartListPopupMenu(popupMenu, connector);
            HmMSmNodeEditHelper.updatePartListPopupMenu(popupMenu, connector);
            iPartsEditValidationHelper.updatePartListPopupMenu(popupMenu, connector);
            EditModuleForm.updateRetailPartListPopupMenu(popupMenu, connector);
            EditAuthorOrderView.updatePartListPopupMenu(popupMenu, connector);
            iPartsMechanicFormWindow.updatePartListPopupMenu(popupMenu, connector);
            EditConstPartListEntryContextMenu.updatePartListPopupMenu(popupMenu, connector);
            AutoTransferPartListEntries.updatePartListPopupMenu(popupMenu, connector);
            AutoTransferPartListEntriesExtended.updatePartListPopupMenu(popupMenu, connector);
            AutoTransferPLEsExtendedWholeProduct.updatePartListPopupMenu(popupMenu, connector);
        }
    }

    @Override
    public void modifyTreePopupMenu(GuiContextMenu popupMenu, AbstractAssemblyTreeForm formWithTree) {
        if (getProject().isEditModeActive() && !formWithTree.getConnector().treeIsOnlyForSelection()) {
            iPartsRelatedInfoMasterDataForm.modifyTreePopupMenu(popupMenu, formWithTree);
            iPartsRelatedInfoMasterDataForm.modifyTreePopupMenuForSA(popupMenu, formWithTree);
            iPartsRelatedInfoModelMasterDataForm.modifyTreePopupMenu(popupMenu, formWithTree);
            iPartsRelatedInfoSAAsModelsDataForm.modifyTreePopupMenu(popupMenu, formWithTree);
            iPartsRelatedInfoBadCodeDataForm.modifyTreePopupMenu(popupMenu, formWithTree);
            iPartsRelatedInfoProductFactoryDataForm.modifyTreePopupMenu(popupMenu, formWithTree);
            iPartsRelatedInfoSeriesEventsDataForm.modifyTreePopupMenu(popupMenu, formWithTree);
            iPartsRelatedInfoFilterValidationForm.modifyTreePopupMenu(popupMenu, formWithTree);
            AbstractConstModelSelectionForm.modifyTreePopupMenu(popupMenu, formWithTree);
            iPartsRelatedInfoDialogUsageDataForm.modifyTreePopupMenu(popupMenu, formWithTree);
            iPartsEditValidationHelper.modifyTreePopupMenu(popupMenu, formWithTree);
            EditModuleForm.modifyRetailPartListTreePopupMenu(popupMenu, formWithTree);
            EditAuthorOrderView.modifyTreePopupMenu(popupMenu, formWithTree);
            iPartsRelatedInfoSeriesCodesDataForm.modifyTreePopupMenu(popupMenu, formWithTree);
            ReportForConstructionNodesHelper.modifyTreePopupMenu(popupMenu, formWithTree);
            HmMSmNodeEditHelper.modifyTreePopupMenu(popupMenu, formWithTree);
            VisualizeSAAHelper.modifyTreePopupMenu(popupMenu, formWithTree);
            ReportForEditOfAutoTransferEntriesHelper.modifyTreePopupMenu(popupMenu, formWithTree);
            AutoTransferPartListEntries.modifyTreePopupMenu(popupMenu, formWithTree);
            AutoTransferPartListEntriesExtended.modifyTreePopupMenu(popupMenu, formWithTree);
            AutoTransferPLEsExtendedWholeProduct.modifyTreePopupMenu(popupMenu, formWithTree);

            // Diesen Popup-Menüeintrag immer als letzten anzeigen
            iPartsMechanicFormWindow.modifyTreePopupMenu(popupMenu, formWithTree);
        }
    }

    @Override
    public void updatePopUpForTree(GuiContextMenu popupMenu, AssemblyTreeFormIConnector connector) {
        if (getProject().isEditModeActive()) {
            iPartsRelatedInfoMasterDataForm.updateTreePopupMenu(popupMenu, connector);
            iPartsRelatedInfoMasterDataForm.updateTreePopupMenuForSA(popupMenu, connector);
            iPartsRelatedInfoModelMasterDataForm.updateTreePopupMenu(popupMenu, connector);
            iPartsRelatedInfoSAAsModelsDataForm.updateTreePopupMenu(popupMenu, connector);
            iPartsRelatedInfoBadCodeDataForm.updateTreePopupMenu(popupMenu, connector);
            iPartsRelatedInfoProductFactoryDataForm.updateTreePopupMenu(popupMenu, connector);
            iPartsRelatedInfoSeriesEventsDataForm.updateTreePopupMenu(popupMenu, connector);
            iPartsRelatedInfoFilterValidationForm.updateTreePopupMenu(popupMenu, connector);
            AbstractConstModelSelectionForm.updateTreePopupMenu(popupMenu, connector);
            iPartsRelatedInfoDialogUsageDataForm.updateTreePopupMenu(popupMenu, connector);
            iPartsEditValidationHelper.updateTreePopupMenu(popupMenu, connector);
            EditModuleForm.updateRetailPartListTreePopupMenu(popupMenu, connector);
            EditAuthorOrderView.updateTreePopupMenu(popupMenu, connector);
            iPartsRelatedInfoSeriesCodesDataForm.updateTreePopupMenu(popupMenu, connector);
            ReportForConstructionNodesHelper.updateTreePopupMenu(popupMenu, connector);
            HmMSmNodeEditHelper.updateTreePopupMenu(popupMenu, connector);
            VisualizeSAAHelper.updateTreePopupMenu(popupMenu, connector);
            ReportForEditOfAutoTransferEntriesHelper.updateTreePopupMenu(popupMenu, connector);
            AutoTransferPartListEntries.updateTreePopupMenu(popupMenu, connector);
            AutoTransferPartListEntriesExtended.updateTreePopupMenu(popupMenu, connector);
            AutoTransferPLEsExtendedWholeProduct.updateTreePopupMenu(popupMenu, connector);
        }
    }

    @Override
    public List<IEtkRelatedInfo> getRelatedInfos(EtkProject project, EtkRelatedInfoLocation location) {
        List<IEtkRelatedInfo> result = new ArrayList<>();
        if (location == EtkRelatedInfoLocation.PARTLIST) {
            result.add(new iPartsRelatedInfoMasterData());                  // Stammdaten
            result.add(new iPartsRelatedInfoSAMasterData());                // SA-Stammdaten
            result.add(new iPartsRelatedInfoEditDetailsData());             // Stücklistendaten (zusätzlich enthalten: Fußnoten, Wahlweise-Teile)
            result.add(new iPartsRelatedInfoFootNote());                    // Fußnoten
            result.add(new iPartsRelatedInfoWWPartsData());                 // Wahlweise-Teile
            result.add(new iPartsRelatedInfoSuperEditData());               // Ersetzungen/Werksdaten
            result.add(new iPartsRelatedInfoReplacementsData());            // Ersetzungen
            result.add(new iPartsRelatedInfoFactoryData());                 // Werksdaten
            result.add(new iPartsRelatedInfoVariantsToPartData());          // Variantenzuordnung zu Teil
            result.add(new iPartsRelatedInfoConstructionKitData());         // Baukasten zu Teil
            result.add(new iPartsRelatedInfoWireHarnessData());             // Leitungssatzbaukasten
            result.add(new iPartsRelatedInfoInternalTextForPartData());     // Interner Text
            result.add(new iPartsRelatedInfoAlternativeMaterials());        // Alternativteile
            result.add(new iPartsRelatedInfoCodeMasterData());              // Code-Erklärung
            result.add(new iPartsRelatedInfoLongCodeRuleData());            // Lange Coderegel
            result.add(new iPartsRelatedInfoPrimusReplacementChain());      // Primus Ersatzkette
            result.add(new iPartsRelatedInfoDialogMultistageRetailReplacementChain()); // Dialog mehrstufige Retail-Ersatzkette

            result.add(new iPartsRelatedInfoModelMasterData());
            result.add(new iPartsRelatedInfoSAAsModelsData());
            result.add(new iPartsRelatedInfoReplacementsConstData());
            result.add(new iPartsRelatedInfoProductFactoryData());
//!!        result.add(new iPartsRelatedInfoPicOrdersToPartData());    // Bildauftragzuordnung zu Teil (entfällt mit DAIMLER-7902)
            result.add(new iPartsRelatedInfoBadCodeData());
            result.add(new iPartsRelatedInfoSeriesEventsData());
            result.add(new iPartsRelatedInfoDialogUsageData());
            result.add(new iPartsRelatedInfoSeriesCodesData());
            result.add(new iPartsRelatedInfoFilterValidation());
        }

        return result;
    }

    @Override
    public List<AssemblyListCellContent> getRelatedInfoIcons(AbstractJavaViewerFormIConnector connector, EtkDataPartListEntry entry) {
        boolean isVirtualPartList = iPartsVirtualNode.isVirtualId(entry.getOwnerAssemblyId());
        List<AssemblyListCellContent> result = new ArrayList<>();
        if (entry.getOwnerAssembly().isRoot()) { // In der Wurzel-Baugruppe gibt es keine zusätzlichen Icons
            return result;
        }

        // Bbefinden wir uns in "Module bearbeiten"?
        boolean isEditModuleActive = AbstractRelatedInfoPartlistDataForm.isEditContext(connector, false);

        boolean isFilterActive = !isEditModuleActive && iPartsAssemblyListSelectFormConnectorWithFilterSettings.isFilterActive(connector);

        boolean retailMode = true;
        boolean isConstruction = false;
        boolean isDialogSMConstruction = false;
        EtkDataAssembly ownerAssembly = entry.getOwnerAssembly();
        if (ownerAssembly != null) {
            iPartsModuleTypes moduleType = iPartsModuleTypes.getType(ownerAssembly.getEbeneName());
            if (moduleType != null) {
                isConstruction = moduleType.isConstructionRelevant();
                isDialogSMConstruction = moduleType == iPartsModuleTypes.Dialog_SM_Construction;
                retailMode = !isConstruction && !isEditModuleActive;
            }
        }
        iPartsDataPartListEntry partListEntry = null;
        if (entry instanceof iPartsDataPartListEntry) {
            partListEntry = (iPartsDataPartListEntry)entry;
        }

        AssemblyListCellContentFromPlugin iconInfo;
        if (partListEntry != null) {
            // Interner Text für Konstruktion
            iconInfo = iPartsRelatedInfoInternalTextForPartDataForm.getRelatedInfoIcon(entry, isConstruction);
            if (iconInfo != null) {
                result.add(iconInfo);
            }

            // Fußnoten, Wahlweise und Ersetzungen für AS-Stücklisten und teilweise auch DIALOG Konstruktion
            if (!isConstruction || isDialogSMConstruction) {
                iconInfo = iPartsRelatedInfoFootNoteDataForm.getRelatedInfoIcon(entry, isEditModuleActive);
                if (iconInfo != null) {
                    result.add(iconInfo);
                }

                if (!isVirtualPartList) {
                    // Prüfen ob auch mit aktueller Filterung Wahlweise-Teile vorhanden sind
                    iconInfo = iPartsRelatedInfoWWPartsDataForm.getRelatedInfoIcon(partListEntry, isFilterActive, isEditModuleActive);
                    if (iconInfo != null) {
                        result.add(iconInfo);
                    }
                }

                // Ersetzungen für Retail und DIALOG Konstruktion
                iconInfo = iPartsRelatedInfoReplacementsDataForm.getRelatedInfoIcon(partListEntry, isVirtualPartList,
                                                                                    isFilterActive, isEditModuleActive);
                if (iconInfo != null) {
                    result.add(iconInfo);
                }
            }
        }

        // Werkseinsatzdaten und Farbvarianten für AS-Stücklisten und DIALOG Konstruktion
        if (!isConstruction || isDialogSMConstruction) {
            // Werkseinsatzdaten
            iconInfo = iPartsRelatedInfoFactoryDataForm.getRelatedInfoIcon(entry, retailMode, isConstruction);
            if (iconInfo != null) {
                result.add(iconInfo);
            }

            // Farbvarianten
            iconInfo = iPartsRelatedInfoVariantsToPartDataForm.getRelatedInfoIcon(entry, isConstruction, isEditModuleActive);
            if (iconInfo != null) {
                result.add(iconInfo);
            }
        }

        // Baukästen
        iconInfo = iPartsRelatedInfoConstructionKitDataForm.getRelatedInfoIconForRetail(connector, entry);
        if (iconInfo != null) {
            result.add(iconInfo);
        }

        // Leitungssatzbaukästen
        iconInfo = iPartsRelatedInfoWireHarnessDataForm.getRelatedInfoIcon(connector, entry, isConstruction);
        if (iconInfo != null) {
            result.add(iconInfo);
        }

        // interner Text
        // wird in EditAssemblyListForm.buildIconColumn() behandelt


        if (partListEntry != null) {
            if (!isVirtualPartList) {
                // Primus Zusatzmaterialien
                if (iPartsRelatedInfoAlternativeMaterialsForm.relatedInfoIsVisible(partListEntry, isEditModuleActive)) {
                    iconInfo = iPartsRelatedInfoAlternativeMaterialsForm.getRelatedInfoIcon();
                    if (iconInfo != null) {
                        result.add(iconInfo);
                    }
                }
            }

            // Einzelteilbild vorhanden?
            if (partListEntry.getPart().getFieldValueAsBoolean(FIELD_M_IMAGE_AVAILABLE)) {
                iconInfo = iPartsSinglePicForPartForm.getRelatedInfoIcon(connector);
                if (iconInfo != null) {
                    result.add(iconInfo);
                }
            }

            if (!isVirtualPartList) {
                // Neuer/Modifizierter Stücklisteneintrag im Editor
                // nur Anzeige im Editor
                if (!retailMode) {
                    iconInfo = EditAssemblyListForm.getPartListEntryModifiedIcon(partListEntry);
                    if (iconInfo != null) {
                        result.add(iconInfo);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public AssemblyListCellContentFromPlugin overwriteRelatedInfoIconInAssemblyList(AbstractJavaViewerFormIConnector connector, EtkDataPartListEntry entry) {
        return null;
    }

    @Override
    public String overwriteEmbeddedRelatedInfoForm() {
        return null;
    }

    @Override
    public boolean needsStaticConnectionUpdates() {
        return (getProject() != null) && getProject().isEditModeActive();
    }

    @Override
    public void modifyPartListPanel(GuiPanel partListPanel, AssemblyListFormIConnector connector) {
        if (connector.getCurrentAssembly() != null) {
            GuiPanel northPanel = new GuiPanel();
            northPanel.setLayout(new LayoutFlow(4, 4, LayoutFlow.ORIENTATION_LEFT));
            northPanel.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_NORTH));
            String ebeneName = connector.getCurrentAssembly().getEbeneName();
            if (ebeneName.equals(PARTS_LIST_TYPE_EDS_SAA) || ebeneName.equals(PARTS_LIST_TYPE_CTT_SAA)) {
                partListPanel.addChild(modifyForSaaPartListPanel(northPanel, connector, ebeneName));
            } else if (ebeneName.equals(PARTS_LIST_TYPE_STRUCTURE_MODEL)) {
                partListPanel.addChild(iPartsConstModelHelper.modifyForStructureModelPartListPanel(northPanel, connector));
            } else if (ebeneName.equals(PARTS_LIST_TYPE_DIALOG_SM)) {
                partListPanel.addChild(modifyForDialogSMPartListPanel(northPanel, connector));
            } else if (ebeneName.equals(PARTS_LIST_TYPE_MBS_CON_GROUP)) {
                partListPanel.addChild(modifyForMBSPartListPanel(northPanel, connector));
            }
        }
    }

    /**
     * Fügt dem MBS Stücklistenpanel spezielle Elemente hinzu
     *
     * @param northPanel
     * @param connector
     * @return
     */
    private AbstractGuiControl modifyForMBSPartListPanel(GuiPanel northPanel, AssemblyListFormIConnector connector) {
        // Strukturstufe
        int currentStructureLevel = iPartsUserSettingsHelper.getMBSStructureLevel(getProject());
        RComboBox<Integer> structureLevelComboBox = addStructureLevelComboBox(northPanel, CONFIG_MBS_MAX_STRUCTURE_LEVEL, currentStructureLevel);
        structureLevelComboBox.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                iPartsUserSettingsHelper.setMBSStructureLevel(getProject(), structureLevelComboBox.getSelectedUserObject());
                assemblyListEditFilterChanged(connector);
            }
        });
        int fallBackStructureLevel = checkStructureLevelFallback(structureLevelComboBox, CONFIG_MBS_DEFAULT_STRUCTURE_LEVEL);
        if (fallBackStructureLevel != -1) {
            iPartsUserSettingsHelper.setMBSStructureLevel(getProject(), fallBackStructureLevel);
        }

        // Änderungsstände für die SAA und die Freigabedatum Calendar Gui (nicht innerhalb der Änderungsstände für die SAA anzeigen)
        if (!(connector instanceof AssemblyListWithHistoryFormConnector) || StrUtils.isValid(((AssemblyListWithHistoryFormConnector)connector).getPartNumber())) {
            GuiSeparator separatorCalendar = new GuiSeparator(DWOrientation.VERTICAL);
            separatorCalendar.setMinimumHeight(structureLevelComboBox.getPreferredHeight());
            northPanel.addChild(separatorCalendar);
            addValidityDateCalendarGui(northPanel, "calendarValidityDateMBSPartsList", connector);

            GuiButton saaHistoryButton = new GuiButton("!!SAA/BK-Änderungsstände");
            GuiSeparator separator = new GuiSeparator(DWOrientation.VERTICAL);
            separator.setMinimumHeight(saaHistoryButton.getPreferredHeight());
            northPanel.addChild(separator);
            saaHistoryButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(Event event) {
                    AssemblyListWithHistoryForm.showAssemblyListWithHistoryForm(connector, null);
                }
            });
            northPanel.addChild(saaHistoryButton);
        }

        return northPanel;
    }

    /**
     * Fügt dem MBS Panel auf Baumuster- und SAA/GS-Ebene spezielle Controls hinzu
     *
     * @param northPanel
     * @param connector
     * @return
     */
    private AbstractGuiControl modifyForMBSListNumberPanel(GuiPanel northPanel, AssemblyListFormIConnector connector) {
        addValidityDateCalendarGui(northPanel, "calendarValidityDateMBSListNumberPanel", connector);
        return northPanel;
    }

    /**
     * Elemente für SAA Konstruktionsstückliste zum PartListPanel hinzufügen
     *
     * @param northPanel
     * @param connector
     * @param partListType
     * @return
     */
    private GuiPanel modifyForSaaPartListPanel(GuiPanel northPanel, final AssemblyListFormIConnector connector, String partListType) {
        boolean isEds = partListType.equals(PARTS_LIST_TYPE_EDS_SAA);
        // Stücklistentexte
        GuiButton textkindButton = addPartlistTextButton(northPanel, new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                if (isEds) {
                    iPartsTextkindForConstPartlistForm.showEDSBCSTextkindSelection(connector, connector.getActiveForm());
                } else {
                    iPartsTextkindForConstPartlistForm.showCTTTextkindSelection(connector, connector.getActiveForm());
                }
            }
        }, "buttonShowTextkindsEDSBCS");

        GuiSeparator separator = new GuiSeparator(DWOrientation.VERTICAL);
        separator.setMinimumHeight(textkindButton.getPreferredHeight());
        northPanel.addChild(separator);
        // Strukturstufe
        int currentStructureLevel = isEds ? iPartsUserSettingsHelper.getEdsSaaStructureLevel(getProject())
                                          : iPartsUserSettingsHelper.getCTTSaaStructureLevel(getProject());
        RComboBox<Integer> structureLevelComboBox = addStructureLevelComboBox(northPanel, isEds ? CONFIG_EDS_SAA_MAX_STRUCTURE_LEVEL
                                                                                                : CONFIG_CTT_MAX_STRUCTURE_LEVEL,
                                                                              currentStructureLevel);
        structureLevelComboBox.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                if (isEds) {
                    iPartsUserSettingsHelper.setEdsSaaStructureLevel(getProject(), structureLevelComboBox.getSelectedUserObject());
                } else {
                    iPartsUserSettingsHelper.setCTTSaaStructureLevel(getProject(), structureLevelComboBox.getSelectedUserObject());
                }
                assemblyListEditFilterChanged(connector);
            }
        });
        int fallBackStructureLevel = checkStructureLevelFallback(structureLevelComboBox, isEds ? CONFIG_EDS_SAA_DEFAULT_STRUCTURE_LEVEL
                                                                                               : CONFIG_CTT_DEFAULT_STRUCTURE_LEVEL);
        if (fallBackStructureLevel != -1) {
            if (isEds) {
                iPartsUserSettingsHelper.setEdsSaaStructureLevel(getProject(), fallBackStructureLevel);
            } else {
                iPartsUserSettingsHelper.setCTTSaaStructureLevel(getProject(), fallBackStructureLevel);
            }
        }

        // Marktspezifischer ET-Kenner
        GuiLabel edsMarketEtkzLabel = new GuiLabel("!!ET-Sicht:");
        edsMarketEtkzLabel.setPaddingLeft(4);
        northPanel.addChild(edsMarketEtkzLabel);

        final EnumRComboBox marketEtkzComboBox = new EnumRComboBox();
        marketEtkzComboBox.setMaximumRowCount(10);
        marketEtkzComboBox.setIgnoreBlankTexts(true);
        marketEtkzComboBox.setEnumTexte(getProject(), ENUM_KEY_EDS_MARKET_ETKZ, getProject().getDBLanguage());

        // Anzeige in der Form  "ET-Sicht-Nummer - ET-Sicht-Name" (z.B. "01 - MBZEUROP")
        List<String> tokens = new ArrayList<>(marketEtkzComboBox.getTokens());
        List<String> items = marketEtkzComboBox.getItems();
        marketEtkzComboBox.removeAllItems();
        Map<String, String> tokensToItemsMap = new TreeMap<>(); // Map für die Sortierung der Einträge
        int index = 0;
        for (String token : tokens) {
            String displayValue = token;
            if (items.size() > index) {
                displayValue += " - " + items.get(index);
            }
            tokensToItemsMap.put(token, displayValue);
            index++;
        }

        // Sortierte Einträge zur ComboBox hinzufügen
        for (Map.Entry<String, String> tokenToItemEntry : tokensToItemsMap.entrySet()) {
            marketEtkzComboBox.addToken(tokenToItemEntry.getKey(), tokenToItemEntry.getValue());
            index++;
        }

        String marketEtkzComboBoxSavedItem = isEds ? iPartsUserSettingsHelper.getEdsMarketEtkz(getProject())
                                                   : iPartsUserSettingsHelper.getCTTMarketEtkz(getProject());
        if (StrUtils.isValid(marketEtkzComboBoxSavedItem)) {
            marketEtkzComboBox.setSelectedUserObject(marketEtkzComboBoxSavedItem);
        } else {
            marketEtkzComboBox.setSelectedIndex(0);
            if (isEds) {
                iPartsUserSettingsHelper.setEdsMarketEtkz(getProject(), marketEtkzComboBox.getActToken());
            } else {
                iPartsUserSettingsHelper.setCTTMarketEtkz(getProject(), marketEtkzComboBox.getActToken());
            }
        }
        marketEtkzComboBox.setMinimumWidth(marketEtkzComboBox.getPreferredFrameWidth() + 4);
        marketEtkzComboBox.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                if (isEds) {
                    iPartsUserSettingsHelper.setEdsMarketEtkz(getProject(), marketEtkzComboBox.getActToken());
                } else {
                    iPartsUserSettingsHelper.setCTTMarketEtkz(getProject(), marketEtkzComboBox.getActToken());
                }
                assemblyListEditFilterChanged(connector);
            }
        });
        northPanel.addChild(marketEtkzComboBox);

        // Sicht auf Serie eingrenzen
        AssemblyId assemblyId = connector.getCurrentAssembly().getAsId();
        RCheckbox checkbox = new RCheckbox();
        checkbox.setScaleForResolution(true);
        checkbox.setSelectionChangedRunnable(() -> {
            iPartsUserSettingsHelper.setSeriesViewActive(getProject(), assemblyId, checkbox.isSelected(), isEds);
            assemblyListEditFilterChanged(connector);
        });
        // Den aktuellen Wert für die Stückliste aus dem Zwischenspeicher holen
        checkbox.setSelected(isEds ? iPartsUserSettingsHelper.isEDSSeriesViewActiveForAssembly(getProject(), assemblyId)
                                   : iPartsUserSettingsHelper.isCTTSeriesViewActiveForAssembly(getProject(), assemblyId));
        separator = new GuiSeparator(DWOrientation.VERTICAL);
        separator.setMinimumHeight(checkbox.getPreferredHeight());
        northPanel.addChild(separator);
        northPanel.addChild(checkbox);
        GuiLabel label = new GuiLabel("!!Auf Serienumfang reduzieren");
        northPanel.addChild(label);

        // Änderungsstände für die SAA (nicht innerhalb der Änderungsstände für die SAA anzeigen)
        if (!(connector instanceof AssemblyListWithHistoryFormConnector) || StrUtils.isValid(((AssemblyListWithHistoryFormConnector)connector).getPartNumber())) {
            GuiButton saaHistoryButton = new GuiButton("!!SAA/BK-Änderungsstände");
            separator = new GuiSeparator(DWOrientation.VERTICAL);
            separator.setMinimumHeight(saaHistoryButton.getPreferredHeight());
            northPanel.addChild(separator);
            saaHistoryButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(Event event) {
                    AssemblyListWithHistoryForm.showAssemblyListWithHistoryForm(connector, null);
                }
            });
            northPanel.addChild(saaHistoryButton);
        }

        return northPanel;
    }

    /**
     * Überprüft, ob der Strukturstufen Fallback aus der Konfiguration gesetzt werden muss
     *
     * @param structureLevelComboBox
     * @param defaultStructureLevelConfigOption
     * @return
     */
    private int checkStructureLevelFallback(RComboBox<Integer> structureLevelComboBox, UniversalConfigOption defaultStructureLevelConfigOption) {
        int result = -1;
        if (structureLevelComboBox.getSelectedIndex() == -1) { // Fallback falls die Strukturstufe nicht mehr verfügbar ist
            structureLevelComboBox.setSelectedUserObject(pluginConfig.getConfigValueAsInteger(defaultStructureLevelConfigOption));
            result = structureLevelComboBox.getSelectedUserObject();
        }
        if (structureLevelComboBox.getSelectedIndex() == -1) { // Fallback falls die Standard-Strukturstufe nicht verfügbar ist
            structureLevelComboBox.setSelectedIndex(0);
            result = structureLevelComboBox.getSelectedUserObject();
        }
        return result;
    }

    /**
     * Erzeugt eine ComboBox zum Einstellen der Strukturstufen in Konstruktionsstücklisten
     *
     * @param northPanel
     * @param maxStructureLevelConfigOption
     * @param currentStructureLevel
     * @return
     */
    private RComboBox<Integer> addStructureLevelComboBox(GuiPanel northPanel, UniversalConfigOption maxStructureLevelConfigOption,
                                                         int currentStructureLevel) {
        // Strukturstufe
        GuiLabel structureLevelLabel = new GuiLabel("!!Anzeige bis inkl. Strukturstufe:");
        northPanel.addChild(structureLevelLabel);
        final RComboBox<Integer> structureLevelComboBox = new RComboBox<Integer>(RComboBox.Mode.STANDARD);
        structureLevelComboBox.setFilterable(false);
        structureLevelComboBox.setMaximumRowCount(10);
        for (int i = 1; i <= getMaxStructureLevelForConstPartList(maxStructureLevelConfigOption); i++) {
            structureLevelComboBox.addItem(i, String.valueOf(i));
        }
        structureLevelComboBox.setMinimumWidth(DWLayoutManager.get().isResponsiveMode() ? 55 : 40);
        structureLevelComboBox.setSelectedUserObject(currentStructureLevel);

        northPanel.addChild(structureLevelComboBox);
        return structureLevelComboBox;
    }

    public static boolean isDIALOGHotSpotAndHierarchyCorrectionActive() {
        return getPluginConfig().getConfigValueAsBoolean(CONFIG_DIALOG_HOTSPOT_HIERARCHY_CORRECTION);
    }

    public static boolean isNewEDSBCSStructureActive() {
        return getPluginConfig().getConfigValueAsBoolean(CONFIG_USE_NEW_EDS_BCS_STRUCTURE);
    }

    public static int getMaxStructureLevelForEDSBCSConstPartList() {
        return maxStructureLevelForEDSBCSConstPartList;
    }

    public static int getMaxStructureLevelForMBSConstPartList() {
        return maxStructureLevelForMBSConstPartList;
    }

    public static int getMaxStructureLevelForCTTConstPartList() {
        return maxStructureLevelForCTTConstPartList;
    }

    public static int getMaxStructureLevelForConstPartList(UniversalConfigOption maxStructureLevelConfigOption) {
        return getPluginConfig().getConfigValueAsInteger(maxStructureLevelConfigOption);
    }

    private GuiButton addPartlistTextButton(GuiPanel northPanel, EventListener eventListener, String name) {
        GuiButton button = new GuiButton("!!Stücklistentexte auswählen");
        button.setName(name);
        button.addEventListener(eventListener);
        northPanel.addChild(button);
        return button;
    }

    /**
     * Erzeugt einen {@link GuiCalendar} und setzt das in der Session gespeicherte Datum.
     * Falls das Datum geändert wird, wird das Datum in der Session gespeichert und {@link #assemblyListEditFilterChanged(AbstractJavaViewerFormIConnector)}
     * aufgerufen. GUI wird hinzugefügt.
     *
     * @param northPanel
     * @param name
     * @param connector
     */
    public void addValidityDateCalendarGui(GuiPanel northPanel, String name, AssemblyListFormIConnector connector) {
        GuiLabel label = new GuiLabel("!!Freigabedatum:");
        northPanel.addChild(label);
        GuiCalendar guiCalendar = new GuiCalendar();

        // In der Session gespeichertes Datum verwenden. Falls keines gesetzt ist,
        // wird das aktuelle zurückgeliefert
        Calendar chosenDateTime = SessionKeyHelper.getMbsConstructionDate();
        guiCalendar.setDate(chosenDateTime);

        // Eine kurze Zeit warten, ob noch weitere Eingaben erfolgen, bevor die Datumseingabe als Freigabedatum übernommen
        // wird, was die Stückliste und auch den GuiCalander für das Freigabedatum neu aufbaut
        guiCalendar.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {

            private volatile FrameworkThread waitThread;

            @Override
            public void fire(Event event) {
                String rawDate = guiCalendar.getDateAsRawString();
                if (DateUtils.isValidDate_yyyyMMdd(rawDate)) {
                    if (waitThread != null) {
                        waitThread.cancel();
                    }

                    final Session session = Session.get();
                    waitThread = session.startChildThread(new FrameworkRunnable() {
                        @Override
                        public void run(FrameworkThread thread) {
                            if (Java1_1_Utils.sleep(iPartsGuiDelayTextField.DEFAULT_DELAYTIME)) {
                                return;
                            }

                            session.invokeThreadSafe(() -> {
                                SessionKeyHelper.setMbsConstructionDate(guiCalendar.getDate());
                                assemblyListEditFilterChanged(connector);
                            });
                        }
                    });
                }
            }
        });

        guiCalendar.setName(name);
        northPanel.addChild(guiCalendar);
    }

    /**
     * Elemnte für DIALOG_SM zum PartListPanel hinzufügen
     * EbeneName == PARTS_LIST_TYPE_DIALOG_SM
     *
     * @param northPanel
     * @param connector
     * @return
     */
    private GuiPanel modifyForDialogSMPartListPanel(GuiPanel northPanel, final AssemblyListFormIConnector connector) {
        addPartlistTextButton(northPanel, new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                iPartsTextkindForConstPartlistForm.showDIALOGTextkindSelection(connector, connector.getActiveForm());
            }
        }, "buttonShowTextkindsDIALOG");

        // eigenes Panel für den Pos-Filter, da 2 Buttons rechts nötig
        GuiPanel middlePanel = new GuiPanel();
        middlePanel.setLayout(new LayoutFlow(4, 4, LayoutFlow.ORIENTATION_CENTER));
        middlePanel.setConstraints(new ConstraintsFlow());
        GuiLabel label = new GuiLabel();
        label.setName("posNoLable");
        label.setText("!!POS ab");
        middlePanel.addChild(label);
        final iPartsGuiPosFilterTextField filterTextField = new iPartsGuiPosFilterTextField();
        filterTextField.setName("filterTextField");
        filterTextField.addEventListener(new EventListener(AbstractGuiButtonTextField.ALTBUTTON_ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                togglePosFilter(connector, filterTextField, true);
            }
        });
        filterTextField.addEventListener(new EventListener(AbstractGuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                togglePosFilter(connector, filterTextField, false);
            }
        });
        filterTextField.switchOffEventListeners();
        if (connector.isFlagCurrentAssemblyIdChanged()) {
            iPartsUserSettingsHelper.clearConstPartListFilterValue(connector.getProject());
        }
        filterTextField.setText(iPartsUserSettingsHelper.getConstPartListFilterValue(connector.getProject()));
        if (iPartsUserSettingsHelper.isConstPartListFilterActive(connector.getProject())) {
            filterTextField.doAltButtonClick();
        }
        filterTextField.switchOnEventListeners();

        middlePanel.addChild(filterTextField);
        northPanel.addChild(middlePanel);

        // Eigenes Panel für Checkboxen damit diese auch unter Swing immer nebeneinander angezeigt werden
        GuiPanel rightPanel = new GuiPanel();
        rightPanel.setLayout(new LayoutFlow(4, 4, LayoutFlow.ORIENTATION_CENTER));
        rightPanel.setConstraints(new ConstraintsFlow());

        GuiCheckbox checkBoxASRelevant = createFilterCheckBox(connector, iPartsUserSettingsConst.REL_DIALOG_HIDE_NON_AS_REL,
                                                              "!!Nur relevante Stände anzeigen");
        rightPanel.addChild(checkBoxASRelevant);

        GuiCheckbox checkBoxLastApproved = createFilterCheckBox(connector, iPartsUserSettingsConst.REL_DIALOG_SHOW_LAST_APPROVED,
                                                                "!!Letzten freigegebenen Stand anzeigen");
        rightPanel.addChild(checkBoxLastApproved);

        // Änderungshistorie
        GuiButton showHistoryButton = new GuiButton("!!Änderungshistorie", EditDefaultImages.edit_history.getImage(), null);
        showHistoryButton.setTooltip(EditAuthorOrderView.IPARTS_MENU_ITEM_SHOW_SM_HISTORY_TEXT);
        showHistoryButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                EditAuthorOrderView.showSMHistoryView(connector, connector.getActiveForm(), connector.getCurrentAssembly());
            }
        });
        rightPanel.addChild(showHistoryButton);

        northPanel.addChild(rightPanel);


        return northPanel;
    }

    private void togglePosFilter(AssemblyListFormIConnector connector, iPartsGuiPosFilterTextField filterTextField, boolean isActive) {
        String filterValue = filterTextField.getFilterText();
        if (!isActive) {
            filterValue = "";
        }
//        // in UserConfig abspeichern
        iPartsUserSettingsHelper.setConstPartListFilterValue(connector.getProject(), filterValue);
//        // Ansicht aktualisieren
        if (connector instanceof MechanicFormConnector) {
            // Lössche erst die gecachten Stücklistenpositionen
            ((MechanicFormConnector)connector).getCurrentAssembly().clearFilteredPartLists();
            // einmal den Filter durchlaufen
            int defaultNumberOfEntries = getConfig().getInteger(SystemSettings.XML_CONFIG_PATH_BASE + SystemSettings.XML_CONFIG_SUBPATH_TABLE_SPLIT_NUMBER_OF_ENTRIES, 100);
            List<EtkDataPartListEntry> partListEntryList = connector.getCurrentPartListEntries();
            int numberPosEntries = getFirstPosVariantsCount(connector, partListEntryList);
            if (numberPosEntries >= 0) {
                numberPosEntries = Math.max(defaultNumberOfEntries, numberPosEntries);
                numberPosEntries = Math.min(numberPosEntries, MAX_ROWS_PER_PAGE_LIMIT);
            }

            // Finde und Aktualisiere die AssemblyListForm
            for (AbstractJavaViewerForm viewForm : connector.getConnectedViews()) {
                if (viewForm instanceof AssemblyListForm) {
                    if (numberPosEntries >= 0) {
                        ((AssemblyListForm)viewForm).setTempHowMuchMaxRowsPerPage(numberPosEntries);
                    }
                    viewForm.updateView();
                }
            }
        }
    }

    private int getFirstPosVariantsCount(AssemblyListFormIConnector connector, List<EtkDataPartListEntry> partListEntryList) {
        EtkDataPartListEntry partListEntry = getFirstRealPartListEntry(partListEntryList);
        if (partListEntry != null) {
            List<EtkDataPartListEntry> pvList = ((iPartsDataAssembly)connector.getCurrentAssembly()).getAllPositionVariants(partListEntry, false);
            if (pvList != null) {
                int sum = pvList.size();
                sum = addPosTextCount(connector, partListEntry, sum);
                return sum;
            }
        }
        return -1;
    }

    private int addPosTextCount(AssemblyListFormIConnector connector, EtkDataPartListEntry partListEntry, int sum) {
        iPartsDialogBCTEPrimaryKey bcteKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(partListEntry);
        if (bcteKey != null) {
            Map<String, Map<IdWithType, iPartsPartlistTextHelper.PartListTexts>> posTextMap =
                    iPartsPartlistTextHelper.getPosTextMap(connector.getProject(), bcteKey.getHmMSmId());
            if (!posTextMap.isEmpty()) {
                String key = iPartsPartlistTextHelper.createPosAndPVKey(bcteKey.getPosE(), "");
                Map<IdWithType, iPartsPartlistTextHelper.PartListTexts> posMap = posTextMap.get(key);
                if (posMap != null) {
                    iPartsDialogPosTextId idWithoutSdata = new iPartsDialogPosTextId(bcteKey.getHmMSmId(), bcteKey.getPosE(), "");
                    iPartsPartlistTextHelper.PartListTexts texte = posMap.get(idWithoutSdata);
                    if (texte != null) {
                        sum += texte.getTexts().size();
                    }
                }
            }
        }
        return sum;
    }

    private EtkDataPartListEntry getFirstRealPartListEntry(List<EtkDataPartListEntry> partListEntryList) {
        if (!partListEntryList.isEmpty()) {
            for (EtkDataPartListEntry psrtListEntry : partListEntryList) {
                if (VirtualMaterialType.getFromDbValue(psrtListEntry.getFieldValue(iPartsConst.FIELD_K_VIRTUAL_MAT_TYPE)) == VirtualMaterialType.NONE) {
                    return psrtListEntry;
                }
            }
        }
        return null;
    }

    private GuiCheckbox createFilterCheckBox(final AssemblyListFormIConnector connector, final String userSettingsKey, String text) {
        final GuiCheckbox checkBox = new GuiCheckbox(text, false);
        checkBox.setPaddingLeft(4);
        checkBox.setPaddingRight(4);
        // wichtig: Default-Selected erst setzen, dann Eventlistener einhängen
        checkBox.setSelected(connector.getProject().getUserSettings().getBoolValues(userSettingsKey));
        checkBox.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                toggleFilterEntries(connector, checkBox.isSelected(), userSettingsKey);
            }
        });
        return checkBox;
    }

    private void toggleFilterEntries(AssemblyListFormIConnector connector, boolean isSelected, String userSettingsKey) {
        // in UserConfig abspeichern
        connector.getProject().getUserSettings().setBoolValues(userSettingsKey, isSelected);
        assemblyListEditFilterChanged(connector);
    }

    private EtkFunction generatePemMasterdata() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                PEMDataHelper.generatePemMasterdata(owner.getProject());
            }
        };
    }

    private EtkFunction showProductMasterData() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                MasterDataProductForm.showProductMasterData(owner);
            }
        };
    }

    private EtkFunction showSeriesMasterData() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                MasterDataSeriesForm.showSeriesMasterData(owner);
            }
        };
    }

    private EtkFunction showVariantsMasterData() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                MasterDataVariantsForm.showVariantsMasterData(owner);
            }
        };
    }

    private EtkFunction showModelConstructionMasterData() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                MasterDataModelConstructionForm.showModelConstructionMasterData(owner);
            }
        };
    }

    private EtkFunction showModelAfterSalesMasterData() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                MasterDataModelAfterSalesForm.showModelAfterSalesMasterData(owner);
            }
        };
    }

    private EtkFunction showModelsAggsMasterData() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                MasterDataModelsAggsForm.showModelsAggsMasterData(owner);
            }
        };
    }

    private EtkFunction showSAAsModelsMasterData() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                MasterDataSAAsModelsForm.showSAAsModelsMasterData(owner);
            }
        };
    }

    private EtkFunction showSaaBkUsage() {
        return new EtkFunction(EventListenerOptions.SYNCHRON_EVENT) { // Synchron wegen dem nicht-modalen Fenster
            @Override
            public void run(AbstractJavaViewerForm owner) {
                MasterDataSearchSaaBkUsageForm.showSaaBkUsage(owner);
            }
        };
    }

    private EtkFunction showProductModelsMasterData() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                MasterDataProductModelsForm.showProductModelsMasterData(owner);
            }
        };
    }

    private EtkFunction showPEMMasterData() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                MasterDataPEMForm.showPEMMasterData(owner);
            }
        };
    }

    private EtkFunction showMatMasterData() {
        return new EtkFunction(EventListenerOptions.SYNCHRON_EVENT) { // Synchron wegen dem nicht-modalen Fenster
            @Override
            public void run(AbstractJavaViewerForm owner) {
                MasterDataMatForm.showMatMasterData(owner);
            }
        };
    }

    private EtkFunction showSearchPartInConstructionKits() {
        return new EtkFunction(EventListenerOptions.SYNCHRON_EVENT) { // Synchron wegen dem nicht-modalen Fenster
            @Override
            public void run(AbstractJavaViewerForm owner) {
                SearchPartInConstructionKitsForm.showSearchPartInConstructionKitsForm(owner);
            }
        };
    }

    private EtkFunction showFreeSAsMasterData() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                MasterDataFreeSAsForm.showFreeSAsMasterData(owner);
            }
        };
    }

    private EtkFunction checkDBConsistency() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                EditDatabaseHelper.checkDBConsistency(owner.getConnector().getProject());
            }
        };
    }

    private EtkFunction checkTextIdConsistency() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                EditDatabaseHelper.checkTextIdConsistency(owner.getConnector().getProject());
            }
        };
    }

    private EtkFunction checkTermIdConsistency() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                EditDatabaseHelper.checkTermIdConsistency(owner.getConnector().getProject());
            }
        };
    }

    private EtkFunction eraseDialogConstructionData() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                EditDatabaseHelper.eraseDialogConstructionDataFromDB(owner.getConnector().getProject());
            }
        };
    }

    private EtkFunction correctSourceGUIDForNonDIALOGPartListEntries() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                EditDatabaseHelper.correctSourceGUIDForNonDIALOGPartListEntriesInDB(owner.getConnector().getProject());
            }
        };
    }

    private EtkFunction convertNutzDokTabs() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                ModalResult result = MessageDialog.showYesNo("!!Möchten Sie wirklich die NutzDok-Tabellen konvertieren?");
                if (result != ModalResult.YES) {
                    return;
                }
                final EtkProject project = owner.getProject();
                final EtkMessageLogForm messageLogForm = new EtkMessageLogForm("!!NutzDok-Tabellen konvertieren",
                                                                               "!!Konvertiere alle Nutzdok-Tabellen...",
                                                                               null);
                messageLogForm.showModal(thread -> {
                    messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Starte Konvertierung: %1", CalendarUtils.format(System.currentTimeMillis(),
                                                                                                                                               CalendarUtils.DEFAULT_STYLE_LOCALE_PATTERN,
                                                                                                                                               Language.DE.getCode())));
                    iPartsNutzDokConverter converter = new iPartsNutzDokConverter(project, messageLogForm);
                    converter.doConvert();
                    messageLogForm.getMessageLog().hideProgress();
                    messageLogForm.getMessageLog().fireMessageWithSeparators(TranslationHandler.translate("!!Konvertierung abgeschlossen: %1",
                                                                                                          CalendarUtils.format(System.currentTimeMillis(),
                                                                                                                               CalendarUtils.DEFAULT_STYLE_LOCALE_PATTERN,
                                                                                                                               Language.DE.getCode())));
                });
            }
        };
    }


    private EtkFunction deleteNotRequiredDialogChangesData() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                iPartsDIALOGChangesFilterForm.showFilteredDialogChangesData(owner);
            }
        };
    }

    private EtkFunction publishDataForRetail() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                if (MessageDialog.showYesNo("!!Sollen die Retail-Caches wirklich zurückgesetzt werden (Publikation mit aktuellem Zeitstempel)?",
                                            "!!Retail-Caches zurücksetzen") == ModalResult.YES) {
                    iPartsPlugin.getPublishingHelper().publishDataForRetail();
                }
            }
        };
    }

    private EtkFunction showUserAdmin() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                UserAdminTreeCreator userAdminTreeCreator = new UserAdminTreeCreator(iPartsUserAdminDb.APP_ID,
                                                                                     false, true,
                                                                                     Arrays.asList(PropertyType.values()), // Alle Eigenschaften-Typen erlauben
                                                                                     UserAdminSettingsPanelOptions.emptySet());

                ConfigurationWindow configurationWindow = new ConfigurationWindow(TranslationHandler.getUiTranslationHandler(),
                                                                                  userAdminTreeCreator, getConfig()) {
                    @Override
                    protected void initConfigurationWindow(TranslationHandler translationHandler, ConfigurationTreeCreator treeCreator, ConfigBase config) {
                        super.initConfigurationWindow(translationHandler, treeCreator, config);

                        // Der OK-Button darf kein DefaultButton sein, weil ansonsten bei Return z.B. bei der Suche nach
                        // Benutzern in der Benutzer-Tabelle der Benutzerverwaltungs-Dialog unter bestimmten Umständen (z.B.
                        // nach einem LDAP-Sync) geschlossen werden würde
                        getConfigButtonPanel().getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK).setDefaultButton(false);

                        getConfigButtonPanel().setButtonVisible(GuiButtonOnPanel.ButtonType.CANCEL, false);
                        getConfigButtonPanel().setButtonVisible(GuiButtonOnPanel.ButtonType.APPLY, false);
                        GuiButtonOnPanel syncButton = getConfigButtonPanel().addCustomButton("!!LDAP-Benutzer synchronisieren");
                        syncButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
                            @Override
                            public void fire(Event event) {
                                final EtkMessageLogForm messageLogForm = new EtkMessageLogForm("!!LDAP-Benutzer synchronisieren",
                                                                                               "!!Synchronisierung mit LDAP läuft...",
                                                                                               null, true);
                                messageLogForm.disableButtons(true);
                                final VarParam<Boolean> syncResult = new VarParam<Boolean>(false);
                                messageLogForm.showModal(thread -> {
                                    try {
                                        syncResult.setValue(LDAPHelper.getInstance().syncLdapDirectoryWithIParts());
                                    } finally {
                                        Session.invokeThreadSafeInSession(() -> messageLogForm.closeWindowIfNotAutoClose(ModalResult.OK));
                                    }
                                });

                                if (syncResult.getValue()) {
                                    apply();
                                    MessageDialog.show("!!LDAP-Benutzer-Synchronisierung erfolgreich durchgeführt.");
                                } else {
                                    MessageDialog.showError("!!Fehler bei der LDAP-Benutzer-Synchronisierung. Siehe Logdatei für Details.");
                                }
                            }
                        });
                        syncButton.setEnabled(iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_LDAP_AUTH_ACTIVE));
                    }

                    @Override
                    protected void writeBack(boolean clearCaches) {
                        // Keine Konfiguration schreiben (die Benutzerverwaltung schreibt ihre Daten abgesehen vom hier
                        // nicht angezeigten UserAdminSettingsPanel nur in die DB) und auch nicht
                        // AbstractApplication.getApplication().configurationChanged() aufrufen
                        // Dafür aber einen iPartsUserAdminChangedEvent in allen Cluster-Knoten feuern
                        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsUserAdminChangedEvent());
                    }
                };

                Dimension screenSize = FrameworkUtils.getScreenSize();
                configurationWindow.getWindow().setSize(screenSize.width - 20, screenSize.height - 20);
                configurationWindow.showModal();
            }
        };
    }

    public EtkFunction clearDictionaryCaches() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                boolean clearLocalDictCaches = MessageDialog.showYesNo("!!Sollen die Lexikon-Caches nur lokal gelöscht werden?",
                                                                       "!!Lexikon-Caches löschen") == ModalResult.YES;
                DictClusterEventHelper.fireClearAllDictionaryCachesClusterEvent(null, !clearLocalDictCaches);
                String msg;
                if (clearLocalDictCaches) {
                    msg = "!!Alle lokalen Lexikon-Caches wurden gelöscht.";
                } else {
                    msg = "!!Alle Lexikon-Caches wurden gelöscht.";
                }
                MessageDialog.show(msg, "!!Lexikon-Caches löschen");
            }
        };
    }

    private EtkFunction createRestartLDAPThreadFunction() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                iPartsPlugin.restartLDAPSyncThread();
                MessageDialog.show("!!Thread für LDAP-Benutzer-Synchronisierung wurde neu gestartet.");
            }
        };
    }

    private EtkFunction createRestartSchedulerThreadsFunction() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                restartPublishingThread();
                restartAutoCalcAndExportThread();
                restartWorkbasketsCalcAndExportThread();
                restartPublishReminderSchedulerThreads();
                restartNutzDokCortexImportThread();
                restartMailboxResubmissionThread();
                restartAutoCalcAndExportThread();
                if (iPartsPlugin.isExportPluginActive()) {
                    de.docware.apps.etk.plugins.customer.daimler.iparts_export.iPartsExportPlugin.restartSaaDataExportThread();
                }
                if (iPartsPlugin.isImportPluginActive()) {
                    de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin.restartSinglePicPartsImportThread();
                    de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin.restartTruckBOMFoundationImportThread();
                    de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin.restartProValImportThread();
                    de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin.restartProValModelAggImportThread();
                }
                MessageDialog.show("!!Threads für automatische Aktionen im Hintergrund wurden neu gestartet.");
            }
        };
    }

    private EtkFunction startCortexScheduler() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                if (iPartsEditPlugin.isNutzDokCortexImportActive()) {
                    singleNutzDokCortexImportScheduler.stopThread();
                    iPartsNutzDokCortexImportScheduler.doTestCortexScheduler(iPartsPlugin.getMqProject(), iPartsPlugin.getMqSession());
                    MessageDialog.show("!!doTestCortexScheduler ist durchgelaufen.");
                    restartNutzDokCortexImportThread();
                } else {
                    MessageDialog.show("!!CortexScheduler ist nicht aktiv.");
                }
            }
        };
    }

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

    /**
     * Durchführung einer Filterung innerhalb einer Table
     *
     * @param filterTyp         der aktuelle Filter
     * @param tableAndFieldName Table.FieldName, aus welcher der zu prüfende Datensatz kommt
     * @param attributes        Datensatz, der gefiltert werden soll
     * @param language
     * @return {@code true}, falls keine Filterung durchgeführt wurde; {@code false}, wenn der Datensatz ausgefiltert wurde
     */
    @Override
    public GridFilterReturnType checkFilterInGrid(EtkFilterTyp filterTyp, String tableAndFieldName,
                                                  DBDataObjectAttributes attributes, String language) {
        if (isCodeField(tableAndFieldName)) {
            return iPartsEditFilter.get().checkGridFilter(filterTyp,
                                                          TableAndFieldName.getTableName(tableAndFieldName), TableAndFieldName.getFieldName(tableAndFieldName),
                                                          attributes, language);
        }
        return GridFilterReturnType.NOT_FILTERED;
    }

    @Override
    public boolean verifyNegativeFilterResultForFilterArtAndLogic(DBDataObjectAttributes attributes, EtkFilterTyp filterType,
                                                                  EtkFilterItem filterItem, List<String> filterValues, String language) {
        return false;
    }

    /**
     * Durchführung einer Filterung
     *
     * @param etkDataObject Datensatz, der gefiltert werden soll
     * @param filterMode
     * @return
     */
    @Override
    public boolean checkFilter(EtkDataObject etkDataObject, FilterMode filterMode) {
        return iPartsEditFilter.get().checkFilter(etkDataObject, filterMode);
    }

    /**
     * Filterung der EtkRecords in den Plugins
     *
     * @param neededTables Liste der Tabellen für die Felder zurück geliefert werden soll
     * @return Liste von Feldern (Format <TabellenName>.<FeldName>))
     */
    @Override
    public Set<String> getActiveFilterFields(Set<String> neededTables) {
        return iPartsEditFilter.get().getActiveFilterFields(neededTables);
    }

    @Override
    public void receiveProjectEvent(AbstractEtkProjectEvent event) {
        if (event instanceof iPartsXMLMessageEvent) { // MQ XML Message
            iPartsXMLMessageEvent xmlMessageEvent = (iPartsXMLMessageEvent)event;
            if (xmlMessageEvent.getChannelTypeName() == iPartsMQChannelTypeNames.MEDIA) {
                try {
                    if (xmlMessageEvent.isFiredInOtherCluster()) {
                        Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "MQ Channel " + xmlMessageEvent.getChannelTypeName().getTypeName()
                                                                                   + ": MQ Message received from another cluster node: "
                                                                                   + MQHelper.getLogTextFromMessageText(xmlMessageEvent.getXmlContent(), true));
                    }
                    iPartsMQMessageManager.getInstance(iPartsPlugin.XML_MESSAGE_MANAGER_NAME_MEDIA).xmlMessageReceived(xmlMessageEvent.getXmlContent(),
                                                                                                                       iPartsPlugin.MQ_CHANNEL_TYPE_MEDIA,
                                                                                                                       xmlMessageEvent.isNotificationOnly(),
                                                                                                                       false);
                } catch (Exception e) {
                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, e);
                }
            }
        }
    }

    @Override
    public boolean openingRelatedInfo(RelatedInfoBaseFormIConnector dataConnector) {
        if (dataConnector.isEditContext()) {
            if (!startEditing()) { // Paralleles Bearbeiten verhindern
                return true;
            }

            try {
                EtkProject project = dataConnector.getProject();
                if (project == null) {
                    return true;
                }

                EtkRevisionsHelper revisionsHelper = project.getRevisionsHelper();
                if (revisionsHelper != null) {
                    // Temporäres ChangeSet für RelatedEdit erzeugen
                    AbstractRevisionChangeSet changeSetForRelatedEdit = revisionsHelper.createTempChangeSet(project);
                    if (dataConnector.getEditContext() instanceof iPartsRelatedInfoEditContext) {
                        iPartsRelatedInfoEditContext editContext = (iPartsRelatedInfoEditContext)dataConnector.getEditContext();

                        // Aktuelle Attribute vom bearbeiteten Stücklisteneintrag im EditContext merken
                        EditFormIConnector editModuleFormConnector = editContext.getEditFormConnector();
                        if (editModuleFormConnector != null) {
                            PartListEntryId editPartListEntryId = dataConnector.getRelatedInfoData().getAsPartListEntryId();
                            if (editPartListEntryId != null) {
                                EtkDataPartListEntry editPartListEntry = editModuleFormConnector.getCurrentAssembly().getPartListEntryFromKLfdNrUnfiltered(editPartListEntryId.getKLfdnr());
                                if (editPartListEntry != null) {
                                    editContext.setOldEditPartListEntryAttributes(editPartListEntry.getAttributes().cloneMe(DBActionOrigin.FROM_DB));
                                }
                            }
                        }

                        editContext.setChangeSetForEdit(changeSetForRelatedEdit);

                        // Ursprüngliches ChangeSet für Edit vom aktiven Autoren-Auftrag im EditContext merken
                        editContext.setAuthorOrderChangeSetForEdit(revisionsHelper.getActiveRevisionChangeSetForEdit());
                    }

                    // Temporäres ChangeSet für RelatedEdit hinzufügen ohne DataChangedEvents (das neue ChangeSet ist ja leer)
                    Collection<AbstractRevisionChangeSet> activeChangeSets = revisionsHelper.getActiveRevisionChangeSets();
                    Collection<AbstractRevisionChangeSet> activeChangeSetsForRelatedEdit = new DwList<>(activeChangeSets);
                    activeChangeSetsForRelatedEdit.add(changeSetForRelatedEdit);

                    // Aufwändige ChangeSet-abhängige Caches für das temporäre Edit-ChangeSet kopieren
                    String toKey = CacheHelper.getRevisionChangeSetsKey(activeChangeSetsForRelatedEdit);
                    iPartsResponseData.referenceActiveChangeSetCache(project, toKey);
                    iPartsResponseSpikes.referenceActiveChangeSetCache(project, toKey);
                    iPartsPartFootnotesCache.referenceActiveChangeSetCache(project, toKey);

                    // ChangeSet-abhängige Caches durch Referenzierung direkt verwenden, die durch die RelatedEdit nicht verändert
                    // werden können (aber aufwändig sind in der Erzeugung)
                    iPartsStructure.useCacheForActiveChangeSets(project, toKey);
                    iPartsProductStructures.useCacheForActiveChangeSets(project, toKey);
                    KgTuForProduct.useCacheForActiveChangeSets(project, toKey);

                    // Edit-ChangeSet aktivieren
                    revisionsHelper.setActiveRevisionChangeSets(activeChangeSetsForRelatedEdit, changeSetForRelatedEdit, false,
                                                                project);
                }
            } catch (Throwable t) {
                // Im Fehlerfall das Editieren wieder zulassen
                stopEditing();
                throw t;
            }
        }

        return false;
    }

    @Override
    public boolean closingRelatedInfo(RelatedInfoBaseFormIConnector dataConnector, ModalResult modalResult) {
        if (dataConnector.getEditContext() instanceof iPartsRelatedInfoEditContext) {
            EtkRevisionsHelper revisionsHelper = dataConnector.getProject().getRevisionsHelper();
            if ((revisionsHelper != null)) {
                iPartsRelatedInfoEditContext editContext = (iPartsRelatedInfoEditContext)dataConnector.getEditContext();
                AbstractRevisionChangeSet changeSetForRelatedEdit = editContext.getChangeSetForEdit();
                if ((modalResult != ModalResult.OK) && (changeSetForRelatedEdit != null) && !changeSetForRelatedEdit.isEmpty()) {
                    if (MessageDialog.showYesNo("!!Sollen die Änderungen wirklich verworfen werden?", "!!Änderungen verwerfen") != ModalResult.YES) {
                        return true; // Veto zum Schließen
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void closedRelatedInfo(RelatedInfoBaseFormIConnector dataConnector, ModalResult modalResult) {
        if (dataConnector.isEditContext()) {
            try {
                internalClosedRelatedInfo(dataConnector, modalResult);
            } finally {
                // EditContext nach der Edit-Aktion zurücksetzen
                if (dataConnector.getEditContext() instanceof iPartsRelatedInfoEditContext) {
                    ((iPartsRelatedInfoEditContext)dataConnector.getEditContext()).clear();
                }
                stopEditing();
            }
        }
    }

    private void internalClosedRelatedInfo(RelatedInfoBaseFormIConnector dataConnector, ModalResult modalResult) {
        if (dataConnector.getEditContext() instanceof iPartsRelatedInfoEditContext) {
            EtkProject project = dataConnector.getProject();
            if (project == null) {
                return;
            }

            EtkRevisionsHelper revisionsHelper = project.getRevisionsHelper();
            if (revisionsHelper != null) {
                iPartsRelatedInfoEditContext editContext = (iPartsRelatedInfoEditContext)dataConnector.getEditContext();
                AbstractRevisionChangeSet changeSetForRelatedEdit = editContext.getChangeSetForEdit();
                if (changeSetForRelatedEdit != null) {
                    EditFormIConnector editModuleFormConnector = editContext.getEditFormConnector();

                    // Ursprüngliches ChangeSet für Edit vom aktiven Autoren-Auftrag aus dem EditContext bestimmen
                    AbstractRevisionChangeSet authorOrderChangeSetForEdit = editContext.getAuthorOrderChangeSetForEdit();

                    if (modalResult == ModalResult.OK) { // Änderungen im Edit-ChangeSet speichern
                        try {
                            // Jetzt alle Runnables ausführen, die erst beim Speichern der RelatedEdit ausgeführt werden können
                            for (Runnable runnable : editContext.getSaveEditRunnables()) {
                                runnable.run();
                            }

                            if (!changeSetForRelatedEdit.isEmpty()) {
                                // Bearbeitetes Modul immer ins EditChangeSet aufnehmen, wenn etwas bearbeitet wurde
                                if (editModuleFormConnector != null) {
                                    editModuleFormConnector.getCurrentAssembly().markAssemblyInChangeSetAsChanged();
                                    editModuleFormConnector.clearFilteredEditPartListEntries();
                                }

                                // Temporäres ChangeSet für RelatedEdit in das echte aktive ChangeSet des Autoren-Auftrags mergen
                                if (authorOrderChangeSetForEdit != null) {
                                    authorOrderChangeSetForEdit.addSerializedDataObjectList(changeSetForRelatedEdit.getSerializedDataObjectsMap().values());
                                }
                            }
                        } catch (Exception e) {
                            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_CHANGE_SETS, LogType.ERROR, e);
                            MessageDialog.showError(TranslationHandler.translate("!!Fehler beim Speichern.") + "\n" + e.getMessage());
                            modalResult = ModalResult.CANCEL; // Im weiteren Verlauf die Aktion wie einen Abbruch behandeln
                        }
                    }

                    // Caches aufräumen
                    iPartsStructure.removeCacheForActiveChangeSets(project);
                    iPartsProductStructures.removeCacheForActiveChangeSets(project);
                    KgTuForProduct.removeCacheForActiveChangeSets(project);
                    EtkDataAssembly.removeCacheForActiveChangeSets(project);

                    // Temporäres ChangeSet für RelatedEdit aus den aktiven ChangeSets entfernen
                    Collection<AbstractRevisionChangeSet> activeChangeSets = revisionsHelper.getActiveRevisionChangeSets();
                    Collection<AbstractRevisionChangeSet> activeChangeSetsWithoutRelatedEdit = new DwList<>(activeChangeSets);
                    activeChangeSetsWithoutRelatedEdit.remove(changeSetForRelatedEdit);

                    // Rückmeldedaten-Cache aktualisieren
                    // Dieser Schritt muss erledigt werden bevor das aktive Changeset umgesetzt wird
                    if ((modalResult == ModalResult.OK) && editContext.isUpdateResponseData()) {
                        // Rückmeldedaten vom temporären Edit-ChangeSet-Cache in den "normalen" ChangeSet-Cache verschieben
                        String toKey = CacheHelper.getRevisionChangeSetsKey(activeChangeSetsWithoutRelatedEdit);
                        iPartsResponseData.moveActiveChangeSetCache(project, toKey);
                    } else {
                        // Rückmeldedaten-Cache aufräumen
                        iPartsResponseData.removeCacheForActiveChangeSets(project);
                    }

                    // Ausreißer-Cache aktualisieren
                    // Dieser Schritt muss erledigt werden bevor das aktive Changeset umgesetzt wird
                    if ((modalResult == ModalResult.OK) && editContext.isUpdateResponseSpikes()) {
                        // Ausreißer vom temporären Edit-ChangeSet-Cache in den "normalen" ChangeSet-Cache verschieben
                        String toKey = CacheHelper.getRevisionChangeSetsKey(activeChangeSetsWithoutRelatedEdit);
                        iPartsResponseSpikes.moveActiveChangeSetCache(project, toKey);
                    } else {
                        // Ausreißer-Cache aufräumen
                        iPartsResponseSpikes.removeCacheForActiveChangeSets(project);
                    }

                    // Materialstamm-Fußnoten-Cache aktualisieren
                    // Dieser Schritt muss erledigt werden bevor das aktive Changeset umgesetzt wird
                    if ((modalResult == ModalResult.OK) /**&& editContext.isUpdateMatFootNotes()**/) {
                        // Materialstamm-Fußnoten vom temporären Edit-ChangeSet-Cache in den "normalen" ChangeSet-Cache verschieben
                        String toKey = CacheHelper.getRevisionChangeSetsKey(activeChangeSetsWithoutRelatedEdit);
                        iPartsPartFootnotesCache.moveActiveChangeSetCache(project, toKey);
                    } else {
                        // Materialstamm-Fußnoten-Cache aufräumen
                        iPartsPartFootnotesCache.removeCacheForActiveChangeSets(project);
                    }

                    // "Normale" ChangeSets aktivieren
                    revisionsHelper.setActiveRevisionChangeSets(activeChangeSetsWithoutRelatedEdit, authorOrderChangeSetForEdit,
                                                                false, project);

                    // Verzögerte Pseudo-Transaktion ist z.B. für das Nachladen von kombinierten Texten notwendig
                    project.startPseudoTransactionForActiveChangeSet(true);
                    try {
                        // Falls das ChangeSet Änderungen enthält, dann müssen wir einen DataChangedEvent unabhängig von
                        // Abbruch oder OK feuern, weil z.B. in nicht-modalen Fenstern während der geöffneten RelatedEdit die
                        // eigentlich verworfenen Änderungen bereits beim Laden von Stücklisten ausgewertet worden sein können
                        // (oder beim Löschen von Caches im Hintergrund)
                        boolean fireDataChangedEvent = !changeSetForRelatedEdit.isEmpty();

                        // Gerade editierten Stücklisteneintrag bestimmen
                        iPartsDataPartListEntry editPartListEntry = null;
                        PartListEntryId editPartListEntryId = dataConnector.getRelatedInfoData().getAsPartListEntryId();
                        if (editPartListEntryId != null) {
                            EtkDataAssembly currentAssembly = editModuleFormConnector.getCurrentAssembly();
                            if (currentAssembly != null) {
                                EtkDataPartListEntry partListEntry = currentAssembly.getPartListEntryFromKLfdNrUnfiltered(editPartListEntryId.getKLfdnr());
                                if (partListEntry instanceof iPartsDataPartListEntry) {
                                    editPartListEntry = (iPartsDataPartListEntry)partListEntry;
                                }
                            }
                        }

                        // Update-Aktionen durchführen
                        if (modalResult == ModalResult.OK) {
                            PartId updatePartId = editContext.getUpdatePartId();

                            // Material hat sich geändert
                            if (updatePartId != null) {
                                iPartsDataChangedEventByEdit<PartId> editMaterialEvent = new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.MATERIAL,
                                                                                                                            iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                                                            updatePartId, false);
                                project.fireProjectEvent(editMaterialEvent);

                                // Als Ersatz für MaterialChanged-Flag die Materialien an betroffenen Stücklisteneinträgen neu laden
                                if (editModuleFormConnector != null) {
                                    EtkDataAssembly editAssembly = editModuleFormConnector.getCurrentAssembly();
                                    for (EtkDataPartListEntry partListEntry : editAssembly.getPartListUnfiltered(null)) {
                                        if (partListEntry.getPart().getAsId().equals(updatePartId)) {
                                            // Kombinierten Text und Teil an betroffenen Stücklisteneinträgen zurücksetzen bzw. neu laden
                                            partListEntry.getPart().loadFromDB(updatePartId);
                                            partListEntry.getAttributes().remove(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT);
                                            partListEntry.getAttributes().remove(iPartsDataVirtualFieldsDefinition.RETAIL_ADD_TEXT);
                                            partListEntry.getAttributes().remove(iPartsDataVirtualFieldsDefinition.RETAIL_TEXT_NEUTRAL);
                                        }
                                    }

                                    editAssembly.clearPartListEntriesForEdit();
                                }
                            }

                            // DataChangedEvent für das gesamte Projekt ist notwendig
                            if (editContext.isFireDataChangedEvent()) {
                                fireDataChangedEvent = true;
                            }

                            if (editModuleFormConnector != null) {
                                // Modul-Stammdaten haben sich geändert
                                if (editContext.isUpdateModuleMasterData()) {
                                    // Cache vom bearbeiteten Modul muss gelöscht werden z.B. für die Anzeige vom Farbvariantentabellen-Icon
                                    EtkDataAssembly editAssembly = editModuleFormConnector.getCurrentAssembly();
                                    editAssembly.clearCache();
                                    iPartsDataAssembly.removeAssemblyMetaDataFromCache(project, editAssembly.getAsId());
                                }

                                if (editContext.isUpdateReplacements()) {
                                    EtkDataAssembly editAssembly = editModuleFormConnector.getCurrentAssembly();
                                    if (editAssembly instanceof iPartsDataAssembly) {
                                        iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)editAssembly;
                                        DBDataObjectList<EtkDataPartListEntry> partListUnfiltered = iPartsAssembly.getPartListUnfiltered(null);

                                        // Wegen neuen, veränderten und gelöschten Ersetzungen -> berechnet indirekt auch die Werkseinsatzdaten neu
                                        iPartsAssembly.loadAllReplacementsForPartList(partListUnfiltered);

                                        iPartsAssembly.loadAllFootNotesForPartList(partListUnfiltered); // Wegen 414er Fußnoten
                                    }

                                    // In der Edit-Stückliste die virtuellen Felder für die DIALOG-Änderungen bei allen
                                    // Stücklisteneinträgen löschen für die Neuberechnung, da beliebig viele Stücklisteneinträge
                                    // betroffen sein können bei Status-Änderungen
                                    clearDIALOGChangesAttributes(editModuleFormConnector);
                                }

                                // Da Farbvarianten pro Materialnr. gespeichert werden, müssen hier bei Änderungen alle
                                // Module, in denen die Materialnr. vorkommt, aus dem Cache gelöscht werden.
                                // Solange noch mit Pseudo-Transaktionen gearbeitet wird, wird der globale Cache für
                                // Stücklisten nicht verwendet und ein Löschen wäre theoretisch nicht nötig.
                                if (editContext.isUpdateRetailColortableData()) {
                                    // Alle Module aus dem Cache entfernen, da Änderungen an den Farbvarianten bzw. deren
                                    // Werkseinsatzdaten über die verschiedenen Materialnummern für eine Farbavariantentabelle
                                    // Auswirkungen auf beliebig viele Module haben kann
                                    EtkDataAssembly.removeCacheForActiveChangeSets(project);

                                    // In der Edit-Stückliste die virtuellen Felder für die DIALOG-Änderungen bei allen
                                    // Stücklisteneinträgen löschen für die Neuberechnung, da beliebig viele Stücklisteneinträge
                                    // betroffen sein können über die verschiedenen Materialnummern für eine Farbavariantentabelle
                                    clearDIALOGChangesAttributes(editModuleFormConnector);
                                }

                                // Retail-Werkseinsatzdaten müssen neu berechnet werden
                                if (editContext.isUpdateRetailFactoryData()) {
                                    // "PEM ab/bis auswerten"-Flags neu berechnen
                                    if (editPartListEntry != null) {
                                        editPartListEntry.updatePEMFlagsFromReplacements();
                                    }

                                    // Alle betroffenen Module aus dem Cache entfernen, da Änderungen an den Retail-Werkseinsatzdaten
                                    // sich über den BCTE-Schlüssel an mehreren Modulen auswirken kann
                                    iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(dataConnector.getRelatedInfoData().getAsPartListEntry(project).getFieldValue(FIELD_K_SOURCE_GUID));
                                    if (bctePrimaryKey != null) {
                                        ASUsageHelper usageHelper = new ASUsageHelper(project);
                                        List<PartListEntryId> partListEntryIds = usageHelper.getPartListEntryIdsUsedInAS(bctePrimaryKey);
                                        if (partListEntryIds != null) {
                                            Set<AssemblyId> assemblyIdsToClear = new HashSet<>();
                                            for (PartListEntryId partListEntryId : partListEntryIds) {
                                                assemblyIdsToClear.add(partListEntryId.getOwnerAssemblyId());
                                            }
                                            for (AssemblyId assemblyId : assemblyIdsToClear) {
                                                EtkDataAssembly.removeDataAssemblyFromCache(project, assemblyId);
                                            }
                                        }
                                    }

                                    // Alle Werkseinsatzdaten für den Retail MIT Berücksichtigung von Ersetzungen müssen für
                                    // alle Stücklisteneinträge neu berechnet werden
                                    EtkDataAssembly editAssembly = editModuleFormConnector.getCurrentAssembly();
                                    if (editAssembly instanceof iPartsDataAssembly) {
                                        ((iPartsDataAssembly)editAssembly).clearAllFactoryDataForRetailForPartList();
                                    }
                                }

                                // DataChangedEvent für bearbeitetes Modul ist notwendig
                                if (editContext.isUpdateEditAssemblyData()) {
                                    // In der Edit-Stückliste die virtuellen Felder für die DIALOG-Änderungen des selektierten
                                    // Stücklisteneintrags löschen für die Neuberechnung
                                    if (editPartListEntry != null) {
                                        editPartListEntry.clearDIALOGChangesAttributes();
                                    }

                                    if (!fireDataChangedEvent) { // Aktualisierung wäre sonst doppelt
                                        editModuleFormConnector.dataChanged(null);
                                    }
                                }

                                // Hotspot/POS hat sich geändert
                                if (editContext.isUpdateEditAssemblyPosNumber()) {
                                    if (editModuleFormConnector instanceof EditModuleFormIConnector) {
                                        EditModuleFormIConnector editModuleConnector = (EditModuleFormIConnector)editModuleFormConnector;
                                        editModuleConnector.posNumberChanged();

                                        // Die Ersetzungen von allen Stücklisteneinträgen der betroffenen Hotspots (alter
                                        // und neuer Wert) müssen aktualisiert werden
                                        Set<String> hotspots = new HashSet<>();
                                        DBDataObjectAttributes oldEditPartListEntryAttributes = editContext.getOldEditPartListEntryAttributes();
                                        if (oldEditPartListEntryAttributes != null) {
                                            hotspots.add(oldEditPartListEntryAttributes.getFieldValue(FIELD_K_POS));
                                        }
                                        if (editPartListEntry != null) {
                                            hotspots.add(editPartListEntry.getFieldValue(FIELD_K_POS));
                                        }
                                        EditModuleHelper.updateReplacementsAndFailLocationsForPLEsForHotspots(editModuleConnector,
                                                                                                              hotspots);
                                    }
                                }

                                EtkDataAssembly.removeDataAssemblyFromCache(project, editModuleFormConnector.getCurrentAssembly().getAsId());

                                // Veränderte Module müssen aus dem Cache gelöscht und im Edit befindliche Module müssen neu
                                // geladen werden
                                Set<AssemblyId> modifiedAssemblyIds = editContext.getModifiedAssemblyIds();
                                if (!modifiedAssemblyIds.isEmpty()) {
                                    for (AssemblyId modifiedAssemblyId : modifiedAssemblyIds) {
                                        EtkDataAssembly.removeDataAssemblyFromCache(project, modifiedAssemblyId);
                                    }
                                    iPartsEditPlugin.reloadModulesInEdit(modifiedAssemblyIds, editModuleFormConnector);
                                }
                            }
                        } else { // Abbruch
                            // Jetzt alle Runnables ausführen, die erst beim Abbrechen der RelatedEdit ausgeführt werden können
                            for (Runnable runnable : editContext.getCancelEditRunnables()) {
                                runnable.run();
                            }

                            // Temporär geänderte Daten wieder korrigieren
                            if (editModuleFormConnector != null) {
                                // Vorherige Attribute vom bearbeiteten Stücklisteneintrag aus dem EditContext wiederherstellen
                                DBDataObjectAttributes oldEditPartListEntryAttributes = editContext.getOldEditPartListEntryAttributes();
                                if (oldEditPartListEntryAttributes != null) {
                                    if (editPartListEntry != null) {
                                        editPartListEntry.setAttributes(oldEditPartListEntryAttributes, DBActionOrigin.FROM_DB);
                                    }
                                }

                                if (editContext.isUpdateReplacements()) {
                                    EtkDataAssembly editAssembly = editModuleFormConnector.getCurrentAssembly();
                                    if (editAssembly instanceof iPartsDataAssembly) {
                                        // Werkseinsatzdaten werden indirekt durch das Laden der Ersetzungen neu berechnet
                                        ((iPartsDataAssembly)editAssembly).loadAllReplacementsForPartList(editAssembly.getPartListUnfiltered(null));
                                    }

                                    // Fußnoten müssen am Stücklisteneintrag ebenfalls neu geladen werden (wegen 414er)
                                    editContext.setUpdateFootNotes(true);
                                }

                                // Farbtabellen für diese Assembly wurden verändert -> komplett neu laden für das Zurücksetzen.
                                // Dazu werden zuerst die gefilterten Farbtabellen gelöscht und danach die Retail-Farbtabellen
                                // neu geladen. Dadurch wird beim nächsten Zugriff die Filterung erneut durchgeführt.
                                if (editContext.isUpdateRetailColortableData()) {
                                    EtkDataAssembly editAssembly = editModuleFormConnector.getCurrentAssembly();
                                    if (editAssembly instanceof iPartsDataAssembly) {
                                        ((iPartsDataAssembly)editAssembly).clearAllColortableDataForRetailFilteredForPartList();
                                        ((iPartsDataAssembly)editAssembly).loadAllColorTableForRetailForPartList(editAssembly.getPartListUnfiltered(null));
                                    }
                                }

                                // Retail-Werkseinsatzdaten wurden verändert -> zurücksetzen und Werkseinsatzdaten neu berechnen
                                if (editContext.isUpdateRetailFactoryData()) {
                                    // Werkseinsatzdaten vom Original-Stücklisteneintrag aus der Edit-Stückliste neu laden
                                    // (beinhaltet auch das Korrigieren der Flags "PEM ab/bis auswerten" an den Werkseinsatzdaten)
                                    EtkDataAssembly editAssembly = editModuleFormConnector.getCurrentAssembly();
                                    if (editAssembly instanceof iPartsDataAssembly) {
                                        if (editPartListEntry != null) {
                                            // "PEM ab/bis auswerten"-Flags neu berechnen
                                            editPartListEntry.updatePEMFlagsFromReplacements();

                                            DBDataObjectList<EtkDataPartListEntry> partListEntries = new DBDataObjectList<>();
                                            partListEntries.add(editPartListEntry, DBActionOrigin.FROM_DB);

                                            // Dieser Aufruf (mit nur einem Element in der Liste) führt zu einer Sonderbehandlung in der Lade-Funktion,
                                            // wodurch hier keine Pseudo-Transaktionen gebraucht werden
                                            ((iPartsDataAssembly)editAssembly).loadAllFactoryDataForRetailForPartList(partListEntries);
                                        }
                                    }
                                }
                            }
                        }

                        // Fußnoten wurden verändert -> zurücksetzen und Fußnoten neu laden
                        if (editContext.isUpdateFootNotes()) {
                            if (editPartListEntry != null) {
                                editPartListEntry.reloadFootNotes();
                            }
                        }
                        // Material-Fußnoten wurden verändert ->
                        // zurücksetzen und Fußnoten neu laden für alle Teilepositionen mit gleichem Material
                        if (editContext.isUpdateMatFootNotes()) {
                            if (editPartListEntry != null) {
                                PartId partId = editPartListEntry.getPart().getAsId();
                                EtkDataAssembly editAssembly = editModuleFormConnector.getCurrentAssembly();
                                DBDataObjectList<EtkDataPartListEntry> modifiedPartlistEntries = new DBDataObjectList<>();
                                for (EtkDataPartListEntry currentPartListEntry : editAssembly.getPartList(null)) {
                                    if (currentPartListEntry.getPart().getAsId().equals(partId)) {
                                        modifiedPartlistEntries.add(currentPartListEntry, DBActionOrigin.FROM_DB);
                                        if (currentPartListEntry instanceof iPartsDataPartListEntry) {
                                            iPartsDataPartListEntry partlistEntry = (iPartsDataPartListEntry)(currentPartListEntry);
                                            partlistEntry.reloadFootNotes();
                                            // erstmal alle Alternativteile an den betroffenen Einträgen löschen
                                            partlistEntry.setAlternativeParts(null);
                                        }
                                    }
                                }
                                if (editAssembly instanceof iPartsDataAssembly) {
                                    iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)editAssembly;
//                                    // erstmal alle Alternativteile an den betroffenen Einträgen löschen
//                                    for (EtkDataPartListEntry modifiedPartlistEntry : modifiedPartlistEntries) {
//                                        if (modifiedPartlistEntry instanceof iPartsDataPartListEntry) {
//                                            iPartsDataPartListEntry partlistEntry = (iPartsDataPartListEntry)(modifiedPartlistEntry);
//                                            partlistEntry.setAlternativeParts(null);
//                                        }
//                                    }
                                    // und dann neu laden
                                    iPartsAssembly.loadAllAlternativePartsForPartList(modifiedPartlistEntries);
                                }
                            }
                        }

                        if (fireDataChangedEvent) {
                            project.fireProjectEvent(new DataChangedEvent(null), true);
                        }
                    } finally {
                        project.stopPseudoTransactionForActiveChangeSet();
                    }
                }

                // Sicherstellen, dass nach der RelatedEdit nur mindestens ein echtes iPartsRevisionChangeSets aktiv ist
                // (normalerweise nur ein Edit-ChangeSet sofern keine weiteren ChangeSets readOnly aktiviert wurden)
                boolean changeSetsValid = true;
                Collection<AbstractRevisionChangeSet> activeChangeSets = revisionsHelper.getActiveRevisionChangeSets();
                if (!activeChangeSets.isEmpty()) {
                    List<AbstractRevisionChangeSet> validChangeSets = new ArrayList<>();
                    for (AbstractRevisionChangeSet activeChangeSet : activeChangeSets) {
                        if (activeChangeSet instanceof iPartsRevisionChangeSet) {
                            validChangeSets.add(activeChangeSet);
                        } else {
                            Logger.log(iPartsPlugin.LOG_CHANNEL_CHANGE_SETS, LogType.ERROR, "Changeset with ID \"" + activeChangeSet.getChangeSetId().getGUID()
                                                                                            + "\" of class \"" + activeChangeSet.getClass().getName()
                                                                                            + "\" is no valid author order changeset but it was active after a related edit action");
                        }
                    }

                    // War ein ungültiges ChangeSet aktiv?
                    if (activeChangeSets.size() != validChangeSets.size()) {
                        List<String> changeSetIds = new ArrayList<>();
                        for (AbstractRevisionChangeSet validChangeSet : validChangeSets) {
                            changeSetIds.add(validChangeSet.getChangeSetId().getGUID());
                        }
                        Logger.log(iPartsPlugin.LOG_CHANNEL_CHANGE_SETS, LogType.ERROR, "Active changesets fixed after related edit action. New active valid changeset IDs: "
                                                                                        + StrUtils.stringListToString(changeSetIds, ", "));
                        AbstractRevisionChangeSet activeChangeSetForEdit = revisionsHelper.getActiveRevisionChangeSetForEdit();
                        if (!validChangeSets.contains(activeChangeSetForEdit)) {
                            if (!validChangeSets.isEmpty()) {
                                activeChangeSetForEdit = validChangeSets.get(0); // Erstes ChangeSet ist immer das Edit-ChangeSet vom Autoren-Auftrag
                                Logger.log(iPartsPlugin.LOG_CHANNEL_CHANGE_SETS, LogType.ERROR, "Active edit changeset fixed after related edit action. New active edit changeset ID: "
                                                                                                + activeChangeSetForEdit.getChangeSetId().getGUID());
                            } else {
                                activeChangeSetForEdit = null;
                                Logger.log(iPartsPlugin.LOG_CHANNEL_CHANGE_SETS, LogType.ERROR, "Active edit changeset fixed after related edit action. No new active edit changeset available!");
                            }
                        }
                        revisionsHelper.setActiveRevisionChangeSets(validChangeSets, activeChangeSetForEdit, true, project);
                        changeSetsValid = false;
                    }
                } else {
                    Logger.log(iPartsPlugin.LOG_CHANNEL_CHANGE_SETS, LogType.ERROR, "No active edit changeset after related edit action!");
                    changeSetsValid = false;
                }
                if (!changeSetsValid) {
                    MessageDialog.showError("!!Bei der Edit-Aktion ist ein Fehler bzgl. des aktiven Änderungssets aufgetreten. Bitte den Autoren-Auftrag deaktivieren, erneut aktivieren und die durchgeführten Änderungen auf Vollständigkeit prüfen!");
                }
            }
        }
    }

    private void clearDIALOGChangesAttributes(EditFormIConnector editModuleFormConnector) {
        EtkDataAssembly currentAssembly = editModuleFormConnector.getCurrentAssembly();
        if (currentAssembly != null) {
            for (EtkDataPartListEntry editPartListEntry : currentAssembly.getPartListUnfiltered(null)) {
                if (editPartListEntry instanceof iPartsDataPartListEntry) {
                    ((iPartsDataPartListEntry)editPartListEntry).clearDIALOGChangesAttributes();
                }
            }
        }
    }

    @Override
    public Color getBackgroundColorForPartlistEntry(EtkDataPartListEntry partListEntry) {
        if (partListEntry instanceof iPartsDataPartListEntry) {
            if (partListEntry.getOwnerAssembly() instanceof iPartsDataAssembly) {
                iPartsDataAssembly assembly = (iPartsDataAssembly)partListEntry.getOwnerAssembly();
                if (assembly.getAsId().isVirtual()) {
                    String levelName = assembly.getEbeneName();
                    VirtualMaterialType virtMatType = VirtualMaterialType.getFromDbValue(partListEntry.getFieldValue(iPartsConst.FIELD_K_VIRTUAL_MAT_TYPE));
                    if (levelName.equals(PARTS_LIST_TYPE_DIALOG_SM)) {
                        if (virtMatType == VirtualMaterialType.TEXT_HEADING) {
                            // Positionstext
                            return clPlugin_iPartsEdit_DialogPositionTextBackground.getColor();
                        } else if (virtMatType == VirtualMaterialType.TEXT_SUB_HEADING) {
                            // Positionsvariantentext
                            return clPlugin_iPartsEdit_DialogPositionVariantTextBackground.getColor();
                        }
                    } else if (levelName.equals(PARTS_LIST_TYPE_CTT_SAA)) {
                        if (virtMatType == VirtualMaterialType.TEXT_HEADING) {
                            // Positionstext
                            return clPlugin_iPartsEdit_CttPositionTextBackground.getColor();
                        }
                    } else if (levelName.equals(PARTS_LIST_TYPE_EDS_SAA)) {
                        if (virtMatType == VirtualMaterialType.TEXT_HEADING) {
                            // Positionstext
                            return clPlugin_iPartsEdit_EdsBcsPositionTextBackground.getColor();
                        } else if (virtMatType == VirtualMaterialType.TEXT_SUB_HEADING) {
                            // Positionsvariantentext
                            return clPlugin_iPartsEdit_EdsBcsAdditionalTextBackground.getColor();
                        }
                        Set<String> kemSet = EditSessionKeyHelper.getKemNoSetFromSessionKeyForMark(assembly.getAsId());
                        if ((kemSet != null) && !kemSet.isEmpty()) {
                            String kemFrom = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.EDS_KEMFROM);
                            String kemTo = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.EDS_KEMTO);
                            for (String kemNo : kemSet) {
                                if ((StrUtils.isValid(kemFrom) && kemFrom.equals(kemNo)) ||
                                    (StrUtils.isValid(kemTo) && kemTo.equals(kemNo))) {
                                    return clPlugin_iPartsEdit_EdsKemMarkTextBackground.getColor();
                                }
                            }
                        }
                    } else if (levelName.equals(PARTS_LIST_TYPE_MBS_CON_GROUP)) {
                        String subSNR = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.MBS_SUB_SNR);
                        // Texte aussortieren
                        if (StrUtils.isValid(subSNR)) {
                            Set<String> kemSet = EditSessionKeyHelper.getKemNoSetFromSessionKeyForMarkMBS(assembly.getAsId());
                            if ((kemSet != null) && !kemSet.isEmpty()) {
                                String kemFrom = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.MBS_KEM_FROM);
                                String kemTo = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.MBS_KEM_TO);
                                for (String kemNo : kemSet) {
                                    if ((StrUtils.isValid(kemFrom) && kemFrom.equals(kemNo)) ||
                                        (StrUtils.isValid(kemTo) && kemTo.equals(kemNo))) {
                                        return clPlugin_iPartsEdit_EdsKemMarkTextBackground.getColor();
                                    }
                                }
                            }
                        } else {
                            return clPlugin_iPartsEdit_EdsBcsPositionTextBackground.getColor();
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Color getForegroundColorForPartlistEntry(EtkDataPartListEntry partListEntry) {
        return null;
    }

    @Override
    public Color getBackgroundColorForPartlistEntrySearchResult(EtkDataPartListEntry partListEntry) {
        return null;
    }

    @Override
    public Color getForegroundColorForPartlistEntrySearchResult(EtkDataPartListEntry partListEntry) {
        return null;
    }

    @Override
    public Color getBackgroundColorForImageIndexState(EtkDataPartListEntry partListEntry,
                                                      EtkHotspotLinkHelper.PartsStateResult imageIndexState) {
        return null;
    }

    private EtkFunction showChangeLogInCopyWindow() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                GuiWindow window = new GuiWindow("!!ChangeSet-Inhalt anzeigen", 300, 100);
                window.setLayout(new LayoutBorder());
                window.setResizable(false);

                GuiPanel panel = new GuiPanel(new LayoutGridBag(false));
                panel.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
                panel.setBorderWidth(8);

                GuiLabel labelChangeSetGUID = new GuiLabel("!!ChangeSet-GUID");
                labelChangeSetGUID.setConstraints(new ConstraintsGridBag(0, 0, 1, 1, 0, 0, ConstraintsGridBag.ANCHOR_EAST,
                                                                         ConstraintsGridBag.FILL_NONE, 0, 0, 8, 8));
                labelChangeSetGUID.setHorizontalAlignment(GuiLabel.HorizontalAlignment.RIGHT);
                panel.addChild(labelChangeSetGUID);

                final GuiTextField textFieldChangeSetGUID = new GuiTextField();
                textFieldChangeSetGUID.setMinimumWidth(300);
                textFieldChangeSetGUID.setConstraints(new ConstraintsGridBag(1, 0, 1, 1, 100, 0, ConstraintsGridBag.ANCHOR_WEST,
                                                                             ConstraintsGridBag.FILL_HORIZONTAL, 0, 0, 8, 0));
                panel.addChild(textFieldChangeSetGUID);

                GuiLabel labelHistory = new GuiLabel("!!Komplette Historie");
                labelHistory.setConstraints(new ConstraintsGridBag(0, 1, 1, 1, 0, 0, ConstraintsGridBag.ANCHOR_EAST,
                                                                   ConstraintsGridBag.FILL_NONE, 0, 0, 0, 8));
                labelHistory.setHorizontalAlignment(GuiLabel.HorizontalAlignment.RIGHT);
                panel.addChild(labelHistory);

                final GuiCheckbox checkBoxHistory = new GuiCheckbox();
                checkBoxHistory.setConstraints(new ConstraintsGridBag(1, 1, 1, 1, 0, 0, ConstraintsGridBag.ANCHOR_WEST,
                                                                      ConstraintsGridBag.FILL_NONE, 0, 0, 0, 0));
                panel.addChild(checkBoxHistory);

                window.addChild(panel);

                GuiButtonPanel buttonPanel = new GuiButtonPanel();
                buttonPanel.setDialogStyle(GuiButtonPanel.DialogStyle.CLOSE);
                buttonPanel.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_SOUTH));
                window.addChild(buttonPanel);

                GuiButton buttonShowChangeSetContent = buttonPanel.addCustomButton("!!ChangeSet-Inhalt anzeigen");
                buttonShowChangeSetContent.setDefaultButton(true);
                buttonShowChangeSetContent.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
                    @Override
                    public void fire(Event event) {
                        showChangeSetContentsInCopyWindow(textFieldChangeSetGUID.getText(), checkBoxHistory.isSelected(),
                                                          !checkBoxHistory.isSelected());
                        textFieldChangeSetGUID.requestFocus();
                    }
                });

                textFieldChangeSetGUID.requestFocus();
                window.pack();
                window.showModal();
            }
        };
    }

    public static void showChangeSetContentsInCopyWindow(String changeSetGUID, boolean historyData,
                                                         boolean merged) {
        iPartsChangeSetId changeSetId = new iPartsChangeSetId(changeSetGUID);
        iPartsRevisionChangeSet changeSet = new iPartsRevisionChangeSet(changeSetId, iPartsPlugin.getMqProject());
        String logTitle = TranslationHandler.translate("!!ChangeSet: %1", changeSet.getChangeSetId().getGUID());
        String changeSetLog = iPartsRevisionsLogger.createRevisionChangeSetLog(logTitle, changeSet, historyData, merged,
                                                                               iPartsPlugin.getMqProject());
        CopyTextWindow copyTextWindow = new CopyTextWindow(changeSetLog);
        copyTextWindow.maximize();
        copyTextWindow.showModal();
    }

    private EtkFunction iPartsDbDay2TruncateTables(TruncateTables.TargetCompany targetCompany) {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                TruncateTables helper = new TruncateTables(targetCompany, owner.getConnector().getProject());
                helper.doTruncateTables();
            }
        };
    }

    private EtkFunction iPartsDbDay2TruncatePartialTables(TruncateTables.TargetCompany targetCompany) {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                TruncateTables helper = new TruncateTables(targetCompany, owner.getConnector().getProject());
                helper.doTruncatePartialTables();
            }
        };
    }

    private EtkFunction iPartsDbDay2CleanUserData(TruncateTables.TargetCompany targetCompany) {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                TruncateTables helper = new TruncateTables(targetCompany, owner.getConnector().getProject());
                helper.doCleanUserData();
            }
        };
    }

    private EtkFunction iPartsDbDay2PurgeAll(TruncateTables.TargetCompany targetCompany) {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                TruncateTables helper = new TruncateTables(targetCompany, owner.getConnector().getProject());
                helper.doPurgeAll();
            }
        };
    }

    private EtkFunction iPartsDbDay2DeleteProducts(TruncateTables.TargetCompany targetCompany) {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                TruncateTables helper = new TruncateTables(targetCompany, owner.getConnector().getProject());
                helper.doDeleteProducts();
            }
        };
    }

    private EtkFunction showTechnicalChangeSets() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                // Neues Formular für Anzeige vom technischen ChangeSet
                ShowTechnicalChangeSetForm.showTechnicalChangeSets(owner.getConnector(), owner);
            }
        };
    }

    private EtkFunction showKemSheetCalculation() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                GuiWindow window = new GuiWindow("!!KEM-Blatt Pfadberechnung", 300, 100);
                window.setLayout(new LayoutBorder());
                window.setResizable(false);

                GuiPanel panel = new GuiPanel(new LayoutGridBag(false));
                panel.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
                panel.setBorderWidth(8);

                GuiLabel labelKemNumber = new GuiLabel("!!KEM");
                labelKemNumber.setConstraints(new ConstraintsGridBag(0, 0, 1, 1, 0, 0, ConstraintsGridBag.ANCHOR_EAST,
                                                                     ConstraintsGridBag.FILL_NONE, 0, 0, 8, 8));
                labelKemNumber.setHorizontalAlignment(GuiLabel.HorizontalAlignment.RIGHT);
                panel.addChild(labelKemNumber);

                final GuiTextField textFieldKemNumber = new GuiTextField();
                textFieldKemNumber.setMinimumWidth(250);
                textFieldKemNumber.setConstraints(new ConstraintsGridBag(1, 0, 1, 1, 100, 0, ConstraintsGridBag.ANCHOR_WEST,
                                                                         ConstraintsGridBag.FILL_HORIZONTAL, 0, 0, 8, 0));
                panel.addChild(textFieldKemNumber);

                window.addChild(panel);

                GuiButtonPanel buttonPanel = new GuiButtonPanel();
                buttonPanel.setDialogStyle(GuiButtonPanel.DialogStyle.CLOSE);
                buttonPanel.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_SOUTH));
                window.addChild(buttonPanel);

                String messageTitle = "!!Berechneter KEM Pfad";
                GuiButton buttonShowKemSheetCalculation = buttonPanel.addCustomButton("!!KEM Pfad anzeigen");
                buttonShowKemSheetCalculation.setDefaultButton(true);
                buttonShowKemSheetCalculation.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
                    @Override
                    public void fire(Event event) {
                        String kemNo = textFieldKemNumber.getText().trim();
                        if (StrUtils.isValid(kemNo)) {
                            MbsKemDataSheetHelper helper = new MbsKemDataSheetHelper(kemNo);
                            if (helper.isInit()) {
                                String msg = TranslationHandler.translate("!!Berechneter relativer KEM-Pfad für KEM \"%1\":", kemNo) +
                                             "\n\n  " + helper.getKemFileName();
                                MessageDialog.show(msg, messageTitle);
                            } else {
                                String msg = TranslationHandler.translate("!!Ungültige KEM \"%1\"!", kemNo);
                                MessageDialog.showError(msg, messageTitle);
                            }
                        } else {
                            MessageDialog.showError("!!Leere KEM-Nummer!", messageTitle);
                        }
                        textFieldKemNumber.requestFocus();
                    }
                });

                textFieldKemNumber.requestFocus();
                window.pack();
                window.showModal();
            }
        };
    }

    @Override
    public void savePluginUserSettings(EtkUserSettings userSettings) {
        userSettings.saveConfigBooleanValue(iPartsUserSettingsConst.REL_SHOW_HIDDEN_HMMSM_NODES);
        userSettings.saveConfigBooleanValue(iPartsUserSettingsConst.REL_DIALOG_HIDE_NON_AS_REL);
        userSettings.saveConfigBooleanValue(iPartsUserSettingsConst.REL_DIALOG_SHOW_LAST_APPROVED);
        userSettings.saveConfigBooleanValue(iPartsUserSettingsConst.REL_SINGLE_EDIT_VIEW);
        userSettings.saveConfigBooleanValue(iPartsUserSettingsConst.REL_SHOW_AGGREGATES_IN_ALL_PRODUCTS);
        userSettings.saveConfigBooleanValue(iPartsUserSettingsConst.REL_HIDE_EMPTY_TUS);
        userSettings.saveConfigBooleanValue(iPartsUserSettingsConst.REL_SVG_IS_PREFERRED);
        userSettings.saveConfigStringValue(iPartsUserSettingsConst.REL_DIALOG_PARTLIST_TEXT_KINDS);
        userSettings.saveConfigStringValue(iPartsUserSettingsConst.REL_EDS_BCS_PARTLIST_TEXT_KINDS);
        userSettings.saveConfigIntegerValue(iPartsUserSettingsConst.REL_EDS_SAA_STRUCTURE_LEVEL);
        userSettings.saveConfigStringValue(iPartsUserSettingsConst.REL_EDS_MARKET_ETKZ);
        userSettings.saveConfigIntegerValue(iPartsUserSettingsConst.REL_MBS_STRUCTURE_LEVEL);
        userSettings.saveConfigStringValue(iPartsUserSettingsConst.REL_CTT_PARTLIST_TEXT_KINDS);
        userSettings.saveConfigIntegerValue(iPartsUserSettingsConst.REL_CTT_SAA_STRUCTURE_LEVEL);
        userSettings.saveConfigStringValue(iPartsUserSettingsConst.REL_CTT_MARKET_ETKZ);
        if (Constants.DEVELOPMENT) {
            userSettings.saveConfigStringValue(iPartsUserSettingsConst.REL_EDS_CONST_MODELS_CAR_VALUE);
            userSettings.saveConfigStringValue(iPartsUserSettingsConst.REL_EDS_CONST_MODELS_AGGREGATE_VALUE);
            userSettings.saveConfigStringValue(iPartsUserSettingsConst.REL_MBS_CONST_MODELS_CAR_VALUE);
            userSettings.saveConfigStringValue(iPartsUserSettingsConst.REL_MBS_CONST_MODELS_AGGREGATE_VALUE);
            userSettings.saveConfigStringValue(iPartsUserSettingsConst.REL_CTT_CONST_MODELS_CAR_VALUE);
            userSettings.saveConfigStringValue(iPartsUserSettingsConst.REL_CTT_CONST_MODELS_AGGREGATE_VALUE);
        }
    }

    @Override
    public void loadPluginUserSettings(EtkUserSettings userSettings) {
        userSettings.loadConfigBool(iPartsUserSettingsConst.REL_SHOW_HIDDEN_HMMSM_NODES, iPartsUserSettingsConst.SHOW_HIDDEN_HMMSM_NODES_DEFAULT);
        userSettings.loadConfigBool(iPartsUserSettingsConst.REL_DIALOG_HIDE_NON_AS_REL, iPartsUserSettingsConst.DIALOG_HIDE_NON_AS_REL_DEFAULT);
        userSettings.loadConfigBool(iPartsUserSettingsConst.REL_DIALOG_SHOW_LAST_APPROVED, iPartsUserSettingsConst.DIALOG_SHOW_LAST_APPROVED_DEFAULT);
        userSettings.loadConfigBool(iPartsUserSettingsConst.REL_SINGLE_EDIT_VIEW, iPartsUserSettingsConst.SINGLE_EDIT_VIEW_DEFAULT);
        boolean isShowAggregatesInAllProducts = userSettings.loadConfigBool(iPartsUserSettingsConst.REL_SHOW_AGGREGATES_IN_ALL_PRODUCTS,
                                                                            iPartsUserSettingsConst.SHOW_AGGREGATES_IN_ALL_PRODUCTS_DEFAULT);
        iPartsProduct.setProductStructureWithAggregatesForSession(isShowAggregatesInAllProducts);
        userSettings.loadConfigBool(iPartsUserSettingsConst.REL_HIDE_EMPTY_TUS, iPartsUserSettingsConst.HIDE_EMPTY_TUS_DEFAULT);
        userSettings.loadConfigBool(iPartsUserSettingsConst.REL_SVG_IS_PREFERRED, iPartsUserSettingsConst.SVG_IS_PREFERRED_DEFAULT);
        userSettings.loadConfigStr(iPartsUserSettingsConst.REL_DIALOG_PARTLIST_TEXT_KINDS, iPartsUserSettingsConst.DIALOG_PARTLIST_TEXT_KINDS_DEFAULT);
        userSettings.loadConfigStr(iPartsUserSettingsConst.REL_EDS_BCS_PARTLIST_TEXT_KINDS, iPartsUserSettingsConst.EDS_BCS_PARTLIST_TEXT_KINDS_DEFAULT);
        userSettings.loadConfigInteger(iPartsUserSettingsConst.REL_EDS_SAA_STRUCTURE_LEVEL, pluginConfig.getConfigValueAsInteger(CONFIG_EDS_SAA_DEFAULT_STRUCTURE_LEVEL));
        userSettings.loadConfigStr(iPartsUserSettingsConst.REL_EDS_MARKET_ETKZ, iPartsUserSettingsConst.EDS_MARKET_ETKZ_DEFAULT);
        userSettings.loadConfigInteger(iPartsUserSettingsConst.REL_MBS_STRUCTURE_LEVEL, pluginConfig.getConfigValueAsInteger(CONFIG_MBS_DEFAULT_STRUCTURE_LEVEL));
        userSettings.loadConfigStr(iPartsUserSettingsConst.REL_CTT_PARTLIST_TEXT_KINDS, iPartsUserSettingsConst.CTT_PARTLIST_TEXT_KINDS_DEFAULT);
        userSettings.loadConfigInteger(iPartsUserSettingsConst.REL_CTT_SAA_STRUCTURE_LEVEL, pluginConfig.getConfigValueAsInteger(CONFIG_CTT_DEFAULT_STRUCTURE_LEVEL));
        userSettings.loadConfigStr(iPartsUserSettingsConst.REL_CTT_MARKET_ETKZ, iPartsUserSettingsConst.CTT_MARKET_ETKZ_DEFAULT);
        if (Constants.DEVELOPMENT) {
            userSettings.setBoolValues(iPartsUserSettingsConst.REL_ULTRA_EDIT_VIEW, iPartsUserSettingsConst.ULTRA_EDIT_VIEW_DEFAULT);
            userSettings.setBoolValues(iPartsUserSettingsConst.REL_ULTRA_EDIT_VIEW_COLS, iPartsUserSettingsConst.ULTRA_EDIT_VIEW_COLS_DEFAULT);
            userSettings.setBoolValues(iPartsUserSettingsConst.REL_ULTRA_EDIT_VIEW_ROWS, iPartsUserSettingsConst.ULTRA_EDIT_VIEW_ROWS_DEFAULT);
            userSettings.loadConfigStr(iPartsUserSettingsConst.REL_EDS_CONST_MODELS_CAR_VALUE, "");
            userSettings.loadConfigStr(iPartsUserSettingsConst.REL_EDS_CONST_MODELS_AGGREGATE_VALUE, "");
            userSettings.loadConfigStr(iPartsUserSettingsConst.REL_MBS_CONST_MODELS_CAR_VALUE, "");
            userSettings.loadConfigStr(iPartsUserSettingsConst.REL_MBS_CONST_MODELS_AGGREGATE_VALUE, "");
            userSettings.loadConfigStr(iPartsUserSettingsConst.REL_CTT_CONST_MODELS_CAR_VALUE, "");
            userSettings.loadConfigStr(iPartsUserSettingsConst.REL_CTT_CONST_MODELS_AGGREGATE_VALUE, "");
        }
    }

    @Override
    public void notifyPluginAssemblyPathChanged(final MechanicFormIConnector connector) {
        if (connector.getCurrentAssembly().getEbeneName().equals(PARTS_LIST_TYPE_STRUCTURE_MODEL)) {
            List<iPartsVirtualNode> virtNodes = iPartsVirtualNode.parseVirtualIds(connector.getCurrentAssembly().getPart().getAsId().getMatNr());
            if (virtNodes != null) {
                // Verarbeitung MBS/EDS Struktur
                iPartsConstModelHelper.handleFilterValuesAfterPathChanged(connector, virtNodes);
            }
        }
    }

    @Override
    public String getVirtualEnumLink(String tableAndFieldName) {
        // Virtueller Enum-Link für die virtuellen Baumuster-Felder bei der Überprüfung auf Überlappung von Teilkonjunktionen
        String tableName = TableAndFieldName.getTableName(tableAndFieldName);
        if (tableName.equals(TABLE_KATALOG)) {
            String validationPrefix = VirtualFieldsUtils.VIRTUAL_FIELD_MARKER_PREFIX + iPartsDataVirtualFieldsDefinition.DA_MODEL_EVALUATION;
            String modelValidationPrefix = validationPrefix + iPartsDataVirtualFieldsDefinition.VIRTFIELD_SPACER
                                           + iPartsDataVirtualFieldsDefinition.DA_MODEL_EVALUATION_CHECK_OVERLAP;
            String finValidationPrefix = VirtualFieldsUtils.VIRTUAL_FIELD_MARKER_PREFIX + iPartsDataVirtualFieldsDefinition.DA_FIN_EVALUATION;

            if (TableAndFieldName.getFieldName(tableAndFieldName).startsWith(modelValidationPrefix)) {
                return ENUM_KEY_VALIDATION_RESULT;
            } else if (TableAndFieldName.getFieldName(tableAndFieldName).startsWith(validationPrefix)) {
                return ENUM_KEY_MODEL_VALIDATION_RESULT;
            } else if (TableAndFieldName.getFieldName(tableAndFieldName).startsWith(finValidationPrefix)) {
                return ENUM_KEY_VALIDATION_RESULT;
            }
        } else if (tableName.equals(TABLE_FOR_EVALUATION_RESULTS)) {
            String prefix = VirtualFieldsUtils.VIRTUAL_FIELD_MARKER_PREFIX + iPartsDataVirtualFieldsDefinition.DA_MODEL_EVALUATION +
                            iPartsDataVirtualFieldsDefinition.VIRTFIELD_SPACER + iPartsDataVirtualFieldsDefinition.DA_MODEL_EVALUATION_COLORS +
                            iPartsDataVirtualFieldsDefinition.DA_MODEL_EVALUATION_SPACER;
            if (TableAndFieldName.getFieldName(tableAndFieldName).startsWith(prefix)) {
                return ENUM_KEY_VALIDATION_RESULT;
            } else if (TableAndFieldName.getFieldName(tableAndFieldName).equals(iPartsDataVirtualFieldsDefinition.DA_PICTURE_AND_TU_QUALITY_CHECK)) {
                return ENUM_KEY_VALIDATION_RESULT;
            }
        } else if (tableName.equals(iPartsDataVirtualFieldsDefinition.TABLE_WORK_BASKET_EDS)) {
            String fieldName = TableAndFieldName.getFieldName(tableAndFieldName);
            if (fieldName.equals(iPartsDataVirtualFieldsDefinition.WBE_DOCU_REL) || fieldName.equals(iPartsDataVirtualFieldsDefinition.WBE_MANUAL_STATUS)) {
                return ENUM_KEY_EDS_DOCU_REL;
            } else if (fieldName.equals(iPartsDataVirtualFieldsDefinition.WBE_SAA_CASE)) {
                return ENUM_KEY_EDS_SAA_CASE;
            }
        }
        return null;
    }

    @Override
    public List<FrameworkSimpleEndpoint> createSimpleEndpoints() {
        List<FrameworkSimpleEndpoint> endpoints = new ArrayList<>();
        iPartsWSVersionInfoBSTEndpoint versionInfoEndpoint = new iPartsWSVersionInfoBSTEndpoint(pluginConfig.getConfigValueAsString(CONFIG_URI_BST_VERSION_INFO));
        addEndpoint(versionInfoEndpoint, CONFIG_URI_BST_VERSION_INFO, endpoints);
        iPartsWSBSTEndpoint bstEndpoint = new iPartsWSBSTEndpoint(pluginConfig.getConfigValueAsString(CONFIG_URI_BST));
        addEndpoint(bstEndpoint, CONFIG_URI_BST, endpoints);

        // Optionale Simulation vom BST-Webservice für die Versorgung von Autoren-Aufträgen
        if (pluginConfig.getConfigValueAsBoolean(CONFIG_SUPPLY_AUTHOR_ORDERS_TO_BST_SIMULATION)) {
            String bstURI = pluginConfig.getConfigValueAsString(CONFIG_URI_SUPPLY_AUTHOR_ORDERS_TO_BST);
            try {
                if (!Utils.isURL(bstURI)) {
                    // relative URL muss mit / beginnen
                    if (!bstURI.startsWith("/")) {
                        bstURI = "/" + bstURI;
                    }
                    iPartsWSSupplyToBSTSimulationEndpoint supplyToBSTSimulationEndpoint = new iPartsWSSupplyToBSTSimulationEndpoint(bstURI);
                    addEndpoint(supplyToBSTSimulationEndpoint, CONFIG_URI_SUPPLY_AUTHOR_ORDERS_TO_BST, endpoints);
                    Logger.log(LOG_CHANNEL_SUPPLY_TO_BST, LogType.INFO, "Simulation of BST webservice to supply author orders started with URI: "
                                                                        + bstURI);
                } else {
                    throw new RuntimeException("URI must not be absolute");
                }
            } catch (Exception e) {
                Logger.logExceptionWithoutThrowing(LOG_CHANNEL_SUPPLY_TO_BST, LogType.ERROR, new RuntimeException("Error starting the simulation of BST webservice to supply author orders with URI: "
                                                                                                                  + bstURI, e));
            }
        }

        String uriNutzdokConstructionKits = pluginConfig.getConfigValueAsString(CONFIG_URI_NUTZDOK_CONSTRUCTION_KITS);
        iPartsWSConstructionKitsEndpoint constructionKitsEndpoint = new iPartsWSConstructionKitsEndpoint(uriNutzdokConstructionKits);
        addEndpoint(constructionKitsEndpoint, CONFIG_URI_NUTZDOK_CONSTRUCTION_KITS, endpoints);

        iPartsWSConstructionKitsRemarksEndpoint constructionKitsRemarksEndpoint = new iPartsWSConstructionKitsRemarksEndpoint(pluginConfig.getConfigValueAsString(CONFIG_URI_NUTZDOK_CONSTRUCTION_KITS_REMARKS));
        addEndpoint(constructionKitsRemarksEndpoint, CONFIG_URI_NUTZDOK_CONSTRUCTION_KITS_REMARKS, endpoints);

        // DAIMLER-10558: Webservicees für KEMs/SAAs: Löschen von Bemerkungstexten (Basis-URI von constructionKits verwenden)
        String uriNutzdokConstructionKitsDeleteKemRemark = uriNutzdokConstructionKits + iPartsWSDeleteConstructionKitsKemRemarkEndpoint.ENDPOINT_URI_SUFFIX;
        UniversalConfigOption configUriNutzdokConstructionKitsDeleteKemRemark = UniversalConfigOption.getStringOption("/deleteKemRemarkURI",
                                                                                                                      uriNutzdokConstructionKitsDeleteKemRemark);
        iPartsWSDeleteConstructionKitsKemRemarkEndpoint constructionKitsDeleteKemRemarkEndpoint = new iPartsWSDeleteConstructionKitsKemRemarkEndpoint(uriNutzdokConstructionKitsDeleteKemRemark);
        addEndpoint(constructionKitsDeleteKemRemarkEndpoint, configUriNutzdokConstructionKitsDeleteKemRemark, endpoints);
        String uriNutzdokConstructionKitsDeleteSaaRemark = uriNutzdokConstructionKits + iPartsWSDeleteConstructionKitsSaaRemarkEndpoint.ENDPOINT_URI_SUFFIX;
        UniversalConfigOption configUriNutzdokConstructionKitsDeleteSaaRemark = UniversalConfigOption.getStringOption("/deleteSaaRemarkURI",
                                                                                                                      uriNutzdokConstructionKitsDeleteSaaRemark);
        iPartsWSDeleteConstructionKitsSaaRemarkEndpoint constructionKitsDeleteSaaRemarkEndpoint = new iPartsWSDeleteConstructionKitsSaaRemarkEndpoint(uriNutzdokConstructionKitsDeleteSaaRemark);
        addEndpoint(constructionKitsDeleteSaaRemarkEndpoint, configUriNutzdokConstructionKitsDeleteSaaRemark, endpoints);

        return endpoints;
    }

    private void addEndpointURIConfigOption(UniversalConfigOption endpointURIConfigOption, String endpointName, UniversalConfigurationPanel configPanel) {
        String webserviceUriText = TranslationHandler.translate("!!URI für Webservice '%1'", TranslationHandler.translate(endpointName));
        configPanel.addStringOption(endpointURIConfigOption, webserviceUriText, false).setValidator(new GuiControlEndpointUriPrefixValidator(webserviceUriText, true));
    }

    private void addEndpoint(RESTfulEndpoint endpoint, UniversalConfigOption configOption, List<FrameworkSimpleEndpoint> endpoints) {
        FrameworkSimpleEndpoint.addEndpointWithEmptyURICheck(endpoint, configOption, endpoints, host, port, iPartsPlugin.LOG_CHANNEL_DEBUG);
    }

    @Override
    public boolean hasVeto(AbstractEtkProjectEvent event, boolean firedFromOtherSession, EtkProject project, Session session) {
        // Während dem Edit alle Events, die Daten für die Anzeige von einer anderen Session aus ändern, nicht sofort sondern
        // erst verzögert nach dem Beenden vom Edit ausführen (siehe stopEditing())
        // Events von der eigenen Session sind durch eigene Edit-Aktionen ausgelöst und sollten daher sofort ausgewertet
        // werden, damit z.B. nicht unnötig viele Pseudo-Transaktionen durch verzögerte Aktionen stattfinden
        // Der DataChangedEvent wird bereits im iPartsPlugin behandelt.
        if ((session != null) && Utils.objectEquals(session.getAttribute(SESSION_KEY_EDITING), Boolean.TRUE)) {
            if (firedFromOtherSession && event.isDataChanged() && !(event instanceof DataChangedEvent)) {
                Map<Class<? extends AbstractEtkProjectEvent>, AbstractEtkProjectEvent> delayedEventsMap = (Map)session.getAttribute(SESSION_KEY_DELAYED_EVENTS_DURING_EDIT);
                if (delayedEventsMap == null) {
                    delayedEventsMap = new LinkedHashMap<>();
                    session.setAttribute(SESSION_KEY_DELAYED_EVENTS_DURING_EDIT, delayedEventsMap);
                }
                Class<? extends AbstractEtkProjectEvent> eventClass = event.getClass();
                AbstractEtkProjectEvent oldEvent = delayedEventsMap.remove(eventClass);

                // Bei iPartsExternalModifiedAuthorOrderEvents müssen die extern veränderten Autoren-Auftrag-IDs zusammengeführt
                // werden, damit keine ID verlorengeht
                if ((oldEvent instanceof iPartsExternalModifiedChangeSetEvent) && (event instanceof iPartsExternalModifiedChangeSetEvent)) {
                    Set<String> mergedModifiedAuthorOrderGUIDs = new TreeSet<>(((iPartsExternalModifiedChangeSetEvent)oldEvent).getModifiedChangeSetIds());
                    mergedModifiedAuthorOrderGUIDs.addAll(((iPartsExternalModifiedChangeSetEvent)event).getModifiedChangeSetIds());
                    event = new iPartsExternalModifiedChangeSetEvent(mergedModifiedAuthorOrderGUIDs);
                }

                delayedEventsMap.put(eventClass, event);
                return true;
            }
        }

        return false;
    }

    @Override
    public List<SearchBaseForm.SearchFormContextMenuItem> createSearchFormContextMenuItems(SearchBaseForm searchForm) {
        if (!(searchForm instanceof SearchMechanicForm)) {
            return null;
        }

        // Kontextmenüeintrag für das Laden der Module aller selektierten Suchergebnisse
        SearchBaseForm.SearchFormContextMenuItem openModuleMenuItem = new SearchBaseForm.SearchFormContextMenuItem() {
            @Override
            public void updateMenuItem(List<EtkSearchBaseResult> selectedSearchResults, SearchFormIConnector connector) {
                super.updateMenuItem(selectedSearchResults, connector);

                // Es muss mindestens ein nicht-virtuelles Modul in den Suchergebnissen selektiert sein
                boolean retailModuleFound = false;
                if (selectedSearchResults != null) {
                    for (EtkSearchBaseResult searchResult : selectedSearchResults) {
                        if (searchResult instanceof EtkPartResult) {
                            if (!iPartsVirtualNode.isVirtualId(((EtkPartResult)searchResult).getAssemblyId())) {
                                retailModuleFound = true;
                                break;
                            }
                        }
                    }
                }

                setEnabled(retailModuleFound);

                if (connector.getProject().isRevisionChangeSetActiveForEdit()) {
                    setText(EditModuleForm.IPARTS_MENU_ITEM_EDIT_RETAIL_MODULE_TEXT_EDIT);
                } else {
                    setText(EditModuleForm.IPARTS_MENU_ITEM_EDIT_RETAIL_MODULE_TEXT_VIEW);
                }
            }
        };

        openModuleMenuItem.setText(EditModuleForm.IPARTS_MENU_ITEM_EDIT_RETAIL_MODULE_TEXT_VIEW);
        openModuleMenuItem.setIcon(DefaultImages.module.getImage());

        openModuleMenuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                // Alle nicht-virtuellen Module laden/bearbeiten
                Set<AssemblyId> assemblyIds = new HashSet<>();
                List<PartListEntryId> partListEntryIds = new DwList<>();
                List<EtkSearchBaseResult> selectedSearchResults = openModuleMenuItem.getSelectedSearchResults();
                if (selectedSearchResults != null) {
                    for (EtkSearchBaseResult searchResult : selectedSearchResults) {
                        if (searchResult instanceof EtkPartResult) {
                            EtkPartResult partSearchResult = (EtkPartResult)searchResult;
                            AssemblyId assemblyId = partSearchResult.getAssemblyId();
                            if (!iPartsVirtualNode.isVirtualId(assemblyId)) {
                                if (!assemblyIds.contains(assemblyId)) {
                                    PartListEntryId partListEntryId = partSearchResult.getEntryId();
                                    if ((partListEntryId != null) && partListEntryId.isValidIdWithkLfdnrNotNull()) {
                                        partListEntryIds.add(partSearchResult.getEntryId());
                                        assemblyIds.add(assemblyId);
                                    }
                                }
                            }
                        }
                    }
                }

                if (!partListEntryIds.isEmpty()) {
                    EditModuleForm.editOrViewModulesByPartListEntries(partListEntryIds, searchForm.getConnector().getMainWindow());
                }
            }
        });

        List<SearchBaseForm.SearchFormContextMenuItem> searchFormContextMenuItems = new ArrayList<>(1);
        searchFormContextMenuItems.add(openModuleMenuItem);
        return searchFormContextMenuItems;
    }

    public static EtkFunction showSQLTools() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                ConfigBase config = getPluginConfig().getConfig();
                SQLToolsPanel.showSQLToolsPanel(config, getPluginConfig().getPath() + "/SQLTools/",
                                                config.getString(BaseSettingsPanel.XML_CONFIG_PATH_BASE + BaseSettingsPanel.XML_CONFIG_SUBPATH_DB_ALIAS, ""),
                                                "select * from KEYVALUE");
            }
        };
    }

    @Override
    public List<AbstractGuiControl> getAdditionalControlsForStatusBar() {
        AbstractJavaViewerFormConnector mainConnector = JavaViewerApplication.getInstance().getMainConnector();
        if (mainConnector != null) {
            JavaViewerMainWindow mainWindow = mainConnector.getMainWindow();
            if (mainWindow != null) {
                List<AbstractJavaViewerMainFormContainer> authorOderForms = mainWindow.getFormsFromClass(EditWorkMainForm.class);
                if (!authorOderForms.isEmpty()) {
                    EditWorkMainForm authorOrderForm = (EditWorkMainForm)authorOderForms.get(0);
                    return authorOrderForm.getAdditionalControlsForStatusBar();
                }
            }
        }

        return null;
    }

    @Override
    public String addHTMLToGuiWindowHeadTag() {
        // Keine Congree Web Interface Integration im Admin-Modus
        if (Session.get().getStartParameter().getParameterBoolean(Constants.FRAMEWORK_PARAMETER_ADMIN, false)) {
            return null;
        }

        return CongreeWebInterfaceHelper.createJavaScriptToInitCongree();
    }
}
