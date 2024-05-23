/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.kemworkbasket;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.forms.common.EditControlFactory;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.forms.common.SimpleSelectSearchResultGrid;
import de.docware.apps.etk.base.forms.events.OnDblClickEvent;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.events.FilterChangedEvent;
import de.docware.apps.etk.base.project.mechanic.NavigationPath;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsAuthorOrderStatus;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrderList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetSource;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataKemResponseList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsKEMWorkBasketEDSId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketInternalTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.HierarchicalIDWithType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.helper.iPartsEdsStructureHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataKEMWorkBasketEDS;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataKEMWorkBasketEDSList;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditUserMultiChangeControls;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.SimpleMasterDataSearchFilterGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsGotoHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsLoadEditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsShowDataObjectsDialog;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.DocuBrowserEDSHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditSessionKeyHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.EcoContentService;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated.GetEcoContentResult;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.iPartsEcoContentServiceForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.iPartsWSWorkBasketItem;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.WorkBasketCollectedInternalTextForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.WorkbasketAutoCalculationHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.WorkbasketSearchCallback;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.WorkbasketSupplierMapping;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper.WorkBasketInternalTextCache;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.config.db.EtkFieldType;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.controls.*;
import de.docware.framework.modules.gui.controls.menu.GuiMenu;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.controls.table.HtmlTablePageSplitMode;
import de.docware.framework.modules.gui.controls.table.TableCellControlWithTextRepresentation;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerOptions;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceStrongLRUList;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

public class MasterDataEDSWorkBasketKEMForm extends SimpleMasterDataSearchFilterGrid implements WorkbasketSearchCallback {

    private static final String CONFIG_KEY_WORK_BASKET_KEM_EDS = "Plugin/iPartsEdit/WorkBasket_KEM_EDS";

    private static final String TABLE_WORK_BASKET = iPartsDataVirtualFieldsDefinition.TABLE_WORK_BASKET;
    private static final String FIELD_KEM_CALC_DOCU_REL = iPartsDataVirtualFieldsDefinition.DKWB_CALC_DOCU_REL;
    private static final String FIELD_KEM_CASE = iPartsDataVirtualFieldsDefinition.DKWB_KEM_CASE;
    private static final String FIELD_KEM_EPEP_RDA = iPartsDataVirtualFieldsDefinition.DKWB_EPEP_RDA;
    private static final String FIELD_KEM_NUTZDOK_REMARKS_AVAILABLE = iPartsDataVirtualFieldsDefinition.WB_REMARK_AVAILABLE;
    private static final String FIELD_KEM_NUTZDOK_INTERNAL_TEXT_AVAILABLE = iPartsDataVirtualFieldsDefinition.WB_INTERNAL_TEXT_AVAILABLE;
    private static final String FIELD_KEM_NUTZDOK_INTERNAL_TEXT = iPartsDataVirtualFieldsDefinition.WB_INTERNAL_TEXT;
    private static final String FIELD_KEM_NUTZDOK_FOLLOWUP_DATE = iPartsDataVirtualFieldsDefinition.WB_FOLLOWUP_DATE;

    private static final String GOTO_CONSTRUCTION_MENU_NAME = "GotoConstruction";
    private static final String GOTO_CONSTRUCTION_WITH_EDIT_MENU_NAME = "GotoConstructionWithEdit";
    private static final String GOTO_RETAIL_MENU_NAME = "GotoRetail";
    private static final String UNIFY_MENU_NAME = "Unify";
    private static final String SHOW_EPEP_KEM_DATA_NAME = "ShowEpepKemData";
    private static final String SHOW_NUTZDOK_REMARK_DATA_NAME = "ShowNutzdokRemarkData";
    private static final String INTERNAL_TEXT_KEM_MENU_NAME = "IntTextKEM";
    private static final String INTERNAL_TEXT_MENU_TEXT = "!!Internen Text/Wiedervorlage-Termin bearbeiten...";
    private static final String SHOW_BOM_DB_KEM_DATA_SHEET = "ShowBomDbKemDataSheet";
    private static final int MAX_CACHE_SIZE_GOTO_NODES = 100;
    private static final int CACHE_LIFETIME_GOTO_NODES = -1;

    private static final String UNIFY_FIELD_NAME = FIELD_DKWB_DOCU_RELEVANT;

    /**
     * Neue Instanz von MasterDataEDSWorkBasketForm erzeugen und Default-Sachen vorbesetzen
     *
     * @param dataConnector
     * @param parentForm
     * @return
     */
    public static MasterDataEDSWorkBasketKEMForm getNewEDSKEMWorkBasketInstance(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        MasterDataEDSWorkBasketKEMForm dlg = new MasterDataEDSWorkBasketKEMForm(dataConnector, parentForm);

        EtkDisplayFields searchFields = getSearchFields(dataConnector);

        dlg.setEditFields(null);
        dlg.setSearchFields(searchFields);
        dlg.setEditAllowed(false);
        dlg.setModifyAllowed(false);
        dlg.setNewAllowed(false);
        dlg.setDeleteAllowed(false);
        dlg.showToolbar(false);
        dlg.showSearchFields(true);
        dlg.setMaxResults(-1);
        dlg.setTitlePrefix("!!EDS-KEM-Arbeitsvorrat");
        dlg.setWindowName("EDSKEMWorkBasketMasterData");
        return dlg;
    }

    /**
     * SearchFields noch ohne Konfiguration besetzen
     *
     * @param dataConnector
     * @return
     */
    private static EtkDisplayFields getSearchFields(AbstractJavaViewerFormIConnector dataConnector) {
        EtkDisplayFields searchFields = new EtkDisplayFields();
        searchFields.load(dataConnector.getConfig(), CONFIG_KEY_WORK_BASKET_KEM_EDS + iPartsEditConfigConst.REL_EDIT_MASTER_SEARCHFIELDS);
        if (searchFields.size() == 0) {
            // Suchfelder definieren
            searchFields.addFeld(createSearchField(dataConnector.getProject(), TABLE_DA_KEM_WORK_BASKET, FIELD_DKWB_PRODUCT_NO, false, false));
            searchFields.addFeld(createSearchField(dataConnector.getProject(), TABLE_DA_KEM_WORK_BASKET, FIELD_DKWB_KG, false, false));
            searchFields.addFeld(createSearchField(dataConnector.getProject(), TABLE_DA_KEM_WORK_BASKET, FIELD_DKWB_SAA, false, false));
            searchFields.addFeld(createSearchField(dataConnector.getProject(), TABLE_DA_KEM_WORK_BASKET, FIELD_DKWB_KEM, false, false));
        }
        return searchFields;
    }

    // Goto-Menus
    protected GuiMenu gotoConstructionMenu;
    protected GuiMenu gotoConstructionWithEditMenu;
    protected GuiMenu gotoRetailMenu;
    protected GuiMenu unifyMenu;
    protected GuiMenu docuBrowserMenu;
    protected GuiMenu intTextMenu;
    protected GuiMenu showNutzdokAnnotationMenu;
    protected GuiMenu showEpepKemDataMenu;
    protected GuiMenu showNutzdokRemarkDataMenu;
    protected GuiMenu showBomDbKemDataSheetMenu;
    protected GuiSeparator showBomDbKemDataSheetMenuSeparator;
    protected boolean gotoNonModalWindow = true;  // true: Öffne Retail/Construction in eigenem Fenster
    protected ObjectInstanceStrongLRUList<String, List<iPartsVirtualNode>> saaConstructionNodeMap;
    protected WorkBasketNutzDokRemarkHelper nutzDokRemarkHelper; // Helper für NutzDokRemark
    protected SearchWorkBasketEDSKEMHelper searchHelper;
    protected iPartsWorkBasketTypes wbType;

    private static final List<String> virtualFieldsList = new DwList<>();

    static {
        virtualFieldsList.add(TableAndFieldName.make(TABLE_DA_KEM_WORK_BASKET, FIELD_KEM_CALC_DOCU_REL));
        virtualFieldsList.add(TableAndFieldName.make(TABLE_DA_KEM_WORK_BASKET, FIELD_KEM_CASE));
        virtualFieldsList.add(TableAndFieldName.make(TABLE_DA_KEM_WORK_BASKET, FIELD_KEM_EPEP_RDA));
        virtualFieldsList.add(TableAndFieldName.make(TABLE_WORK_BASKET, FIELD_KEM_NUTZDOK_REMARKS_AVAILABLE));
        virtualFieldsList.add(TableAndFieldName.make(TABLE_WORK_BASKET, FIELD_KEM_NUTZDOK_INTERNAL_TEXT_AVAILABLE));
        virtualFieldsList.add(TableAndFieldName.make(TABLE_WORK_BASKET, FIELD_KEM_NUTZDOK_INTERNAL_TEXT));
        virtualFieldsList.add(TableAndFieldName.make(TABLE_WORK_BASKET, FIELD_KEM_NUTZDOK_FOLLOWUP_DATE));
    }

    /**
     * Erzeugt eine Instanz von SimpleMasterDataSearchResultGrid.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     * @param parentForm
     */
    public MasterDataEDSWorkBasketKEMForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm, TABLE_DA_KEM_WORK_BASKET, null);
        EdSWorkBasketKEMSearchFilterFactory filterFactory = new EdSWorkBasketKEMSearchFilterFactory(getProject());
        setColumnFilterFactory(filterFactory);
        getTable().setColumnFilterFactory(filterFactory);
        doResizeWindow(SCREEN_SIZES.MAXIMIZE);

        if (Constants.DEVELOPMENT && !Constants.DEVELOPMENT_QFTEST) {
            gotoNonModalWindow = false;
        }

        OnDblClickEvent onDblClickEvent = new OnDblClickEvent() {
            @Override
            public void onDblClick() {
                gotoConstruction(gotoNonModalWindow, false);
            }
        };

        // Der Doppelklick zum Öffnen der Konstruktions-Stückliste in einem nicht-modalen Fenster muss synchron abgearbeitet
        // werden, um Popup-Blocker zu vermeiden
        setOnDblClickEvent(onDblClickEvent);
        getTable().removeEventListeners(Event.MOUSE_DOUBLECLICKED_EVENT);
        getTable().addEventListener(new EventListener(Event.MOUSE_DOUBLECLICKED_EVENT, EventListenerOptions.SYNCHRON_EVENT) {
            @Override
            public void fire(Event event) {
                onTableDoubleClicked(event);
            }
        });

        addContextMenus();
        saaConstructionNodeMap = new ObjectInstanceStrongLRUList<>(MAX_CACHE_SIZE_GOTO_NODES, CACHE_LIFETIME_GOTO_NODES);

        nutzDokRemarkHelper = new WorkBasketNutzDokRemarkHelper(this, getProject(), iPartsWSWorkBasketItem.TYPE.KEM);

        searchHelper = new SearchWorkBasketEDSKEMHelper(getProject(), this, nutzDokRemarkHelper);
        this.wbType = searchHelper.getWbType();
        setDisplayResultFields(searchHelper.buildDisplayFields());

        GuiButton downloadButton = new GuiButton(TranslationHandler.translate(WorkbasketAutoCalculationHelper.DOWNLOAD_BUTTON_TEXT));
        getSearchButtonPanel().addChildGridBag(downloadButton, 1, 0, 1, 1, 0.0, 0.0,
                                               ConstraintsGridBag.ANCHOR_NORTH, ConstraintsGridBag.FILL_HORIZONTAL,
                                               4, 4, 4, 4);
        downloadButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                if (Constants.DEVELOPMENT && !Constants.DEVELOPMENT_QFTEST) {
                    ModalResult modalResult = MessageDialog.showYesNo("Soll zuerst berechnet werden?");
                    if (modalResult == ModalResult.YES) {
                        DWFile tempDir = DWFile.createTempDirectory("daim");
                        if (tempDir == null) {
                            return;
                        }
                        WorkbasketSupplierMapping supplierMapping = new WorkbasketSupplierMapping(getProject());
                        WorkbasketAutoCalculationHelper.calculateEdsKemWorkbasket(getProject(), tempDir, supplierMapping);
                        WorkbasketAutoCalculationHelper.finishExport(tempDir);
                    }
                }
                WorkbasketAutoCalculationHelper.downloadCalculatedData();
            }
        });
    }

    /**
     * Goto-Menus vor 'Copy' hinzufügen
     */
    private void addContextMenus() {
        List<AbstractGuiControl> list = new DwList<>();
        gotoConstructionMenu = toolbarHelper.createMenuEntry(GOTO_CONSTRUCTION_MENU_NAME, "!!Gehe zu Konstruktion", null, new EventListener(Event.MENU_ITEM_EVENT,
                                                                                                                                            EventListenerOptions.SYNCHRON_EVENT) {
            @Override
            public void fire(Event event) {
                gotoConstruction(gotoNonModalWindow, false);
            }
        }, getUITranslationHandler());
        list.add(gotoConstructionMenu);

        gotoConstructionWithEditMenu = toolbarHelper.createMenuEntry(GOTO_CONSTRUCTION_WITH_EDIT_MENU_NAME, "!!Gehe zu Konstruktion mit Laden der TUs", null, new EventListener(Event.MENU_ITEM_EVENT,
                                                                                                                                                                                EventListenerOptions.SYNCHRON_EVENT) {
            @Override
            public void fire(Event event) {
                gotoConstruction(gotoNonModalWindow, true);
            }
        }, getUITranslationHandler());
        list.add(gotoConstructionWithEditMenu);

        gotoRetailMenu = toolbarHelper.createMenuEntry(GOTO_RETAIL_MENU_NAME, "!!Gehe zum Retail-TU", null, new EventListener(Event.MENU_ITEM_EVENT,
                                                                                                                              EventListenerOptions.SYNCHRON_EVENT) {
            @Override
            public void fire(Event event) {
                gotoRetail(gotoNonModalWindow);
            }
        }, getUITranslationHandler());
        list.add(gotoRetailMenu);

        list.add(toolbarHelper.createMenuSeparator(GOTO_RETAIL_MENU_NAME + "Separator", getUITranslationHandler()));

        unifyMenu = toolbarHelper.createMenuEntry(UNIFY_MENU_NAME, "!!Vereinheitlichen...", null, new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                unifyDocuRel();
            }
        }, getUITranslationHandler());
        list.add(unifyMenu);

        // DAIMLER-10342, ePEP: Anzeige Rückmeldedaten
        showEpepKemDataMenu = toolbarHelper.createMenuEntry(SHOW_EPEP_KEM_DATA_NAME, "!!(ePEP) Rückmeldedaten anzeigen", null, new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                showEpepKemData();
            }
        }, getUITranslationHandler());
        list.add(showEpepKemDataMenu);

        showNutzdokRemarkDataMenu = toolbarHelper.createMenuEntry(SHOW_NUTZDOK_REMARK_DATA_NAME, "!!Nutzdok-Bemerkungstexte anzeigen", null, null, getUITranslationHandler());
        list.add(showNutzdokRemarkDataMenu);

        intTextMenu = toolbarHelper.createMenuEntry(INTERNAL_TEXT_KEM_MENU_NAME, INTERNAL_TEXT_MENU_TEXT, EditDefaultImages.edit_btn_internal_text.getImage(), new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
//                cancelSearchThread();
                editInternalText();
            }
        }, getUITranslationHandler());
        list.add(intTextMenu);

        showNutzdokAnnotationMenu = toolbarHelper.createMenuEntry("showNutzdokAnnotationMenu", WorkBasketNutzDokRemarkHelper.NUTZDOK_ANNOTATION_MENU_TEXT, EditDefaultImages.edit_btn_internal_text.getImage(), new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                showNutzdokAnnotation();
            }
        }, getUITranslationHandler());
        list.add(showNutzdokAnnotationMenu);

        // BOM-DB KEM-Daten: Menüpunkt und Separator erstellen und einfügen
        showBomDbKemDataSheetMenuSeparator = toolbarHelper.createMenuSeparator(SHOW_BOM_DB_KEM_DATA_SHEET + "Separator", getUITranslationHandler());
        showBomDbKemDataSheetMenu = toolbarHelper.createMenuEntry(SHOW_BOM_DB_KEM_DATA_SHEET, "!!BOM-DB KEM-Datenblatt anzeigen",
                                                                  null, new EventListener(Event.MENU_ITEM_EVENT, EventListenerOptions.SYNCHRON_EVENT) {
                    @Override
                    public void fire(Event event) {
                        showBomDbKemDataSheetDialog();
                    }
                }, getUITranslationHandler());
        list.add(showBomDbKemDataSheetMenuSeparator);
        list.add(showBomDbKemDataSheetMenu);

        docuBrowserMenu = DocuBrowserEDSHelper.createDocuBrowserPopupMenuItem(toolbarHelper, iPartsWorkBasketTypes.EDS_KEM_WB,
                                                                              getUITranslationHandler(), null);
        docuBrowserMenu.setVisible(false);
        list.add(docuBrowserMenu);

        list.addAll(getTable().getContextMenu().getChildren());
        getTable().getContextMenu().removeAllChildren();
        for (AbstractGuiControl guiCtrl : list) {
            getTable().getContextMenu().addChild(guiCtrl);
        }
    }

    public iPartsWorkBasketTypes getWbType() {
        return wbType;
    }


    /**
     * Zeigt das KEM-Datenblatt auf Basis der KEM-Nummer des ausgewählten Datensatzes.
     */
    private void showBomDbKemDataSheetDialog() {
        DBDataObjectAttributes attributes = getSelectedAttributes();
        if (attributes != null) {
            String kemNo = searchHelper.getKemNo(attributes);
            if (StrUtils.isValid(kemNo)) {
                EcoContentService ecoContentService = new EcoContentService();
                final String title = TranslationHandler.translate("!!BOM-DB KEM-Datenblatt für KEM-Nummer \"%1\"", kemNo);
                final EtkMessageLogForm messageLogForm = new EtkMessageLogForm(title, "!!Suche KEM-Datenblatt in BOM-DB", null);
                messageLogForm.getGui().setSize(600, 250);
                messageLogForm.showMarquee();
                messageLogForm.showModal(new FrameworkRunnable() {
                    @Override
                    public void run(FrameworkThread thread) {
                        GetEcoContentResult ecoContent;
                        messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Lade BOM-DB KEM-Datenblatt für KEM-Nummer \"%1\"...", kemNo));
                        try {
                            String language = Language.EN.getCode();
                            if ((getProject() != null) && (getProject().getViewerLanguage() != null)) {
                                language = getProject().getViewerLanguage();
                            }
                            ecoContent = ecoContentService.getEcoContentByKem(kemNo, language);
                            if ((ecoContent == null) || (ecoContent.getEco() == null)) {
                                messageLogForm.disableMarquee();
                                messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Fehlender Inhalt beim Aufruf vom Webservice \"%1\" für KEM-Nummer '%2'.",
                                                                                                        TranslationHandler.translate("!!BOM-DB KEM-Daten"), kemNo));
                            } else {
                                messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!BOM-DB KEM-Datenblatt vom Webservice \"%1\" vollständig geladen.",
                                                                                                        TranslationHandler.translate("!!BOM-DB KEM-Daten")));
                                messageLogForm.closeWindow(ModalResult.OK);

                                iPartsEcoContentServiceForm.showEcoContentServiceForm(getConnector(), MasterDataEDSWorkBasketKEMForm.this,
                                                                                      kemNo, ecoContent);
                            }
                        } catch (Exception e) {
                            Logger.logExceptionWithoutThrowing(iPartsEditPlugin.LOG_CHANNEL_BOM_DB_SOAP_WEBSERVICE, LogType.ERROR, e);
                            Session.invokeThreadSafeInSession(() -> messageLogForm.disableMarquee());
                            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Fehler beim Aufruf vom Webservice \"%1\" für KEM-Nummer \"%2\":%3",
                                                                                                    TranslationHandler.translate("!!BOM-DB KEM-Daten"),
                                                                                                    kemNo, "\n" + e.getMessage()));
                        }
                    }
                });
            }
        }
    }

    /**
     * Besetzen der Anzeigewerte und Hints
     *
     * @param attributes
     * @return
     */
    @Override
    protected SimpleSelectSearchResultGrid.GuiTableRowWithAttributes createRow(DBDataObjectAttributes attributes) {
        SimpleSelectSearchResultGrid.GuiTableRowWithAttributes row = new SimpleSelectSearchResultGrid.GuiTableRowWithAttributes();
        row.attributes = attributes;
        for (EtkDisplayField field : displayResultFields.getVisibleFields()) {
            if (field.isVisible()) {
                row.addChild(calcGuiElemForCell(field, attributes));
            }
        }
        return row;
    }

    private TableCellControlWithTextRepresentation calcGuiElemForCell(EtkDisplayField field, DBDataObjectAttributes attributes) {
        String tableName = field.getKey().getTableName();
        String fieldName = field.getKey().getFieldName();
        String value = "";
        String hintValue = "";
        if (fieldName.equals(FIELD_DKWB_SAA)) {
            value = iPartsNumberHelper.formatPartNo(getProject(), attributes.getFieldValue(fieldName));
        } else {
            value = getVisualValueOfFieldValue(tableName, fieldName, attributes.getField(fieldName), field.isMultiLanguage());
        }
        if (fieldName.equals(FIELD_DKWB_DOCU_RELEVANT) || fieldName.equals(FIELD_KEM_CALC_DOCU_REL) ||
            fieldName.equals(FIELD_KEM_CASE)) {
            if (StrUtils.isValid(searchHelper.getDocuRelReason(attributes))) {
                hintValue = searchHelper.getDocuRelReason(attributes);
            }
        }

        AbstractGuiControl result = new GuiLabel(value);
        if (StrUtils.isValid(hintValue)) {
            GuiPanel panel = new GuiPanel(new LayoutGridBag(false));
            panel.setBackgroundColor(Colors.clTransparent.getColor());
            result.setConstraints(new ConstraintsGridBag(0, 0, 1, 1, 0, 1, LayoutGridBag.ANCHOR_CENTER, LayoutGridBag.FILL_HORIZONTAL, 0, 2, 0, 0));
            panel.addChild(result);
            if (StrUtils.isValid(hintValue)) {
                panel.setTooltip(hintValue);
                result.setTooltip(hintValue);
            }
            return new TableCellControlWithTextRepresentation(panel, () -> result.getTextRepresentation());
        }
        return new TableCellControlWithTextRepresentation(result, () -> result.getTextRepresentation());
    }

    @Override
    public SimpleSelectSearchResultGrid.GuiTableRowWithAttributes addAttributesToGrid(DBDataObjectAttributes attributes) {
        SimpleSelectSearchResultGrid.GuiTableRowWithAttributes row = super.addAttributesToGrid(attributes);
        if (J2EEHandler.isJ2EE()) {
            if ((getMaxResults() == -1) && getTable().getHtmlTablePageSplitMode() != HtmlTablePageSplitMode.BUTTONS) {
                int pageSplitNumberOfEntriesPerPage = getTable().getPageSplitNumberOfEntriesPerPage();
                if ((pageSplitNumberOfEntriesPerPage > 0) && (getTable().getRowCount() > pageSplitNumberOfEntriesPerPage)) {
                    getTable().setHtmlTablePageSplitMode(HtmlTablePageSplitMode.BUTTONS);
                }
            }
        }

        return row;
    }

    /**
     * Goto-Retail Menu neu aufbauen und enablen
     *
     * @param event
     */
    @Override
    protected void onTableSelectionChanged(Event event) {
        super.onTableSelectionChanged(event);
        boolean isSingleRetailSelected = false;
        boolean isSingleConstructionSelected = false;
        boolean isSingleConstructionWithEditSelected = false;
        boolean isUnifyEnabled = false;
        boolean isShowEpepKemDataEnabled = false;
        boolean isShowNutzDokRemarkDataEnabled = false;
        boolean isShowNutzDokAnnotationDataEnabled = false;
        boolean isShowBomDbKemDataSheetMenu;

        DBDataObjectAttributesList selectedAttribList = getSelectedAttributesList();
        if ((selectedAttribList != null) && !selectedAttribList.isEmpty()) {
            isUnifyEnabled = true;
            if (selectedAttribList.size() == 1) {
                DBDataObjectAttributes selectedAttrib = selectedAttribList.get(0);
                isSingleRetailSelected = StrUtils.isValid(searchHelper.getModuleNo(selectedAttrib));
                isSingleConstructionSelected = StrUtils.isValid(searchHelper.getSaaBkNo(selectedAttrib));
                if (isSingleConstructionSelected) {
                    isSingleConstructionWithEditSelected = getProject().isRevisionChangeSetActiveForEdit();
                }
                isShowEpepKemDataEnabled = searchHelper.getEpepResponseDataAvailable(selectedAttrib);
                isShowNutzDokRemarkDataEnabled = nutzDokRemarkHelper.prepareNutzDokSubMenus(showNutzdokRemarkDataMenu, searchHelper.getKemNo(selectedAttrib));
                isShowNutzDokAnnotationDataEnabled = nutzDokRemarkHelper.isNutzDokAnnotationMenuEnabled(searchHelper.getKemNo(selectedAttrib));
            }
        }
        gotoConstructionMenu.setEnabled(isSingleConstructionSelected);
        gotoConstructionWithEditMenu.setEnabled(isSingleConstructionWithEditSelected && isSingleRetailSelected);
        gotoRetailMenu.setEnabled(isSingleRetailSelected);
        unifyMenu.setEnabled(isUnifyEnabled);
        intTextMenu.setEnabled((selectedAttribList != null) && (selectedAttribList.size() == 1));
        showNutzdokAnnotationMenu.setEnabled(isShowNutzDokAnnotationDataEnabled);
        showEpepKemDataMenu.setEnabled(isShowEpepKemDataEnabled);
        showNutzdokRemarkDataMenu.setEnabled(isShowNutzDokRemarkDataEnabled);

        // Anzeige des BOM-DB KEM-Daten Menüpunktes auf Basis der gewählten Admin-Option
        isShowBomDbKemDataSheetMenu = iPartsEditPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsEditPlugin.CONFIG_BOMDB_WEBSERVICE_STATUS);
        showBomDbKemDataSheetMenuSeparator.setVisible(isShowBomDbKemDataSheetMenu);
        showBomDbKemDataSheetMenu.setVisible(isShowBomDbKemDataSheetMenu);

        // DokuBrowser Submenüs für KEM und SAA erzeugen
        if (docuBrowserMenu != null) {
            docuBrowserMenu.setVisible(iPartsEditPlugin.isDocuBrowserActive());
            if (docuBrowserMenu.isVisible()) {
                if ((selectedAttribList != null) && (selectedAttribList.size() == 1)) {
                    DBDataObjectAttributes selectedAttributes = selectedAttribList.get(0);
                    String kemNo = searchHelper.getKemNo(selectedAttributes);
                    String saaBkNo = searchHelper.getSaaBkNo(selectedAttributes);
                    DocuBrowserEDSHelper.addDocuBrowserSubMenus(getConnector(), docuBrowserMenu, saaBkNo, kemNo);
                } else {
                    docuBrowserMenu.setEnabled(false);
                }
            }
        }
    }

    /**
     * Callback für Goto-Construction
     *
     * @param withOwnDialog
     */
    private void gotoConstruction(boolean withOwnDialog, boolean withLoadInEdit) {
        DBDataObjectAttributes attributes = getSelectedAttributes();
        if (attributes != null) {
            String saaBkNo = searchHelper.getSaaBkNo(attributes);
            if (StrUtils.isValid(saaBkNo)) {
                String productNo = searchHelper.getProductNo(attributes);
                String key = saaBkNo + "&" + productNo;
                List<iPartsVirtualNode> nodes = saaConstructionNodeMap.get(key);
                if (nodes == null) {
                    nodes = new ArrayList<>();
                    iPartsEdsStructureHelper structureHelper = iPartsEdsStructureHelper.getInstance();
                    EtkDataObject foundEdsStructureContent = findEdsStructureContent(productNo, saaBkNo, structureHelper);
                    // Pfad für den Sprung in die Konstruktions zusammenbauen inkl. setzen des BM für den EDS-BM-Filter
                    if (foundEdsStructureContent != null) {
                        String modelNo = foundEdsStructureContent.getFieldValue(structureHelper.getModelNumberField());
                        if (iPartsPlugin.addEDSConstructionModelToFilter(modelNo)) {
                            getProject().fireProjectEvent(new FilterChangedEvent());
                        }
                        HierarchicalIDWithType structureId = structureHelper.createStructureIdFromDataObject(foundEdsStructureContent);
                        nodes.addAll(iPartsGotoHelper.createVirtualNodePathForEDSConstruction(modelNo, structureId,
                                                                                              foundEdsStructureContent.getFieldValue(structureHelper.getSubElementField())));
                        saaConstructionNodeMap.put(key, nodes);
                    }
                }
                // KEM und Assembly für Markierung in der Stückliste merken
                boolean found = false;
                AssemblyId assemblyId = null;
                if (!nodes.isEmpty()) {
                    String kemNo = searchHelper.getKemNo(attributes);
                    NavigationPath path = new NavigationPath();
                    assemblyId = new iPartsAssemblyId(iPartsVirtualNode.getVirtualIdString(ArrayUtil.toArray(nodes)), "");
                    path.addAssembly(assemblyId);
                    EditSessionKeyHelper.setSessionKeyForMark(assemblyId, kemNo);
                    found = iPartsGotoHelper.gotoPath(getConnector(), this, withOwnDialog, false, path, assemblyId, null);
                }
                if (!found) {
                    if (assemblyId != null) {
                        EditSessionKeyHelper.resetSessionKeyForMark(assemblyId);
                    }
                    MessageDialog.showWarning(TranslationHandler.translate("!!Keine Konstruktionsdaten für \"%1\" gefunden!",
                                                                           iPartsNumberHelper.formatPartNo(getProject(), saaBkNo)));
                }

                if (withLoadInEdit) {
                    // alle zu KEM und SAA passenden Module im Retail laden
                    boolean loadReadOnly = true;
                    iPartsDataAuthorOrder currentAuthorOrder = iPartsDataAuthorOrderList.getAuthorOrderByActiveChangeSetForEdit(getProject());
                    if (currentAuthorOrder != null) {
                        loadReadOnly = iPartsAuthorOrderStatus.isEndState(currentAuthorOrder.getStatus());
                    }

                    String kemNo = searchHelper.getKemNo(attributes);
                    List<AssemblyId> assemblyIdList = iPartsDataKEMWorkBasketEDSList.loadModulesByKEMAndSaaBk(getProject(),
                                                                                                              kemNo, saaBkNo, true);
                    if (!assemblyIdList.isEmpty()) {
                        // im Editor laden und kemNo übergeben
                        // wenn KonstruktionsModul im eigenen Fenster geladen wurde => in Editor umschalten
                        Set<String> selectedModulList = new HashSet<>();
                        assemblyIdList.forEach(eachAssemblyId -> selectedModulList.add(eachAssemblyId.getKVari()));
                        iPartsLoadEditModuleHelper.doLoadModules(getConnector(), selectedModulList, TranslationHandler.translate("!!Bezug zur KEM-Nummer: %1", kemNo),
                                                                 loadReadOnly, withOwnDialog, null);
                    }
                }
            }
        }
    }

    /**
     * Lädt die EDS Strukturinformationen in denen die SAA oder Baukasten vorkommen
     *
     * @param productNo
     * @param saaBkNo
     * @param structureHelper
     * @return
     */
    private EtkDataObject findEdsStructureContent(String productNo, String saaBkNo, iPartsEdsStructureHelper structureHelper) {
        EtkDataObjectList<? extends EtkDataObject> list = structureHelper.loadAllStructureEntriesForSaaOrBK(getProject(), saaBkNo);
        EtkDataObject foundEdsStructureContent = null;
        if (!list.isEmpty()) {
            Set<String> modelNoList = iPartsProduct.getInstance(getProject(), new iPartsProductId(productNo)).getModelNumbers(getProject());
            for (EtkDataObject dataObject : list) {
                if (modelNoList.contains(dataObject.getFieldValue(structureHelper.getModelNumberField()))) {
                    foundEdsStructureContent = dataObject;
                    break;
                }
            }
            if (foundEdsStructureContent == null) {
                foundEdsStructureContent = list.get(0);
            }
        }
        return foundEdsStructureContent;
    }

    /**
     * Callback füt Goto-Retail
     *
     * @param withOwnDialog
     */
    private void gotoRetail(boolean withOwnDialog) {
        DBDataObjectAttributes attributes = getSelectedAttributes();
        if (attributes != null) {
            String moduleNo = searchHelper.getModuleNo(attributes);
            if (StrUtils.isValid(moduleNo)) {
                AssemblyId id = new AssemblyId(moduleNo, "");
                if (!iPartsGotoHelper.gotoRetail(getConnector(), this, withOwnDialog, id)) {
                    MessageDialog.showWarning(TranslationHandler.translate("!!Keine Retaildaten für \"%1\" gefunden!",
                                                                           iPartsNumberHelper.formatPartNo(getProject(), moduleNo)));
                }
            }
        }
    }

    private void unifyDocuRel() {
        endSearch();

        DBDataObjectAttributesList attributeList = getSelectedAttributesList();
        if (attributeList != null) {
            List<iPartsDataKEMWorkBasketEDS> kemWorkBasketList = new DwList<>(attributeList.size());
            for (DBDataObjectAttributes attrib : attributeList) {
                iPartsKEMWorkBasketEDSId id = new iPartsKEMWorkBasketEDSId(attrib);
                iPartsDataKEMWorkBasketEDS dataKEMWorkBasket = new iPartsDataKEMWorkBasketEDS(getProject(), id);
                dataKEMWorkBasket.assignAttributes(getProject(), attrib, true, DBActionOrigin.FROM_DB);
                dataKEMWorkBasket.removeForeignTablesAttributes();
                kemWorkBasketList.add(dataKEMWorkBasket);
            }

            EtkEditFields editFields = new EtkEditFields();
            addEditField(searchTable, UNIFY_FIELD_NAME, false, null, getProject(), editFields);
            // Anzeige des Unify-Dialogs
            DBDataObjectAttributes attributes =
                    EditUserMultiChangeControls.showEditUserMultiChangeControlsForKemWorkBasket(getConnector(), editFields,
                                                                                                kemWorkBasketList);
            if (attributes != null) {
                for (EtkEditField editField : editFields.getFields()) {
                    String fieldName = editField.getKey().getFieldName();
                    String fieldValue = attributes.getFieldValue(fieldName);
                    for (iPartsDataKEMWorkBasketEDS dataKEMWorkBasket : kemWorkBasketList) {
                        dataKEMWorkBasket.setFieldValue(fieldName, fieldValue, DBActionOrigin.FROM_EDIT);
                    }
                }
                GenericEtkDataObjectList genericList = new GenericEtkDataObjectList();
                genericList.addAll(kemWorkBasketList, DBActionOrigin.FROM_EDIT);

                if (iPartsRevisionChangeSet.saveDataObjectListWithChangeSet(getProject(), genericList, iPartsChangeSetSource.KEM_WORKBASKET)) {
                    getProject().getDbLayer().startTransaction();
                    try {
                        genericList.saveToDB(getProject());
                        getProject().getDbLayer().commit();
                        updateGrid(editFields, virtualFieldsList, attributes);
                    } catch (Exception e) {
                        getProject().getDbLayer().rollback();
                        Logger.getLogger().handleRuntimeException(e);
                    } finally {
//                        dataObjectList.clear();
                    }
                }
            }
        }
    }

    /**
     * DAIMLER-10342, ePEP: Anzeige Rückmeldedaten
     */
    private void showEpepKemData() {
        DBDataObjectAttributes attributes = getSelectedAttributes();
        if (attributes != null) {
            String kemNo = searchHelper.getKemNo(attributes);
            if (StrUtils.isValid(kemNo)) {
                iPartsDataKemResponseList kemResponseList = iPartsDataKemResponseList.loadKemResponseListForKemFromDB(getProject(), kemNo);
                if (kemResponseList.isEmpty()) {
                    MessageDialog.showWarning(TranslationHandler.translate("!!Keine (ePEP) Rückmeldedaten zur KEM \"%1\" gefunden!", kemNo));
                } else {
                    iPartsShowDataObjectsDialog.showEpepResponse(getConnector(), this, kemNo, kemResponseList);
                }
            }
        }
    }

    private void editInternalText() {
        endSearch();

        DBDataObjectAttributesList attributeList = getSelectedAttributesList();
        if ((attributeList != null) && !attributeList.isEmpty()) {
            DBDataObjectAttributes selectedAttributes = attributeList.get(0);
            String saaBkKemNo = searchHelper.getSaaBkKemValue(selectedAttributes);
            iPartsWorkBasketInternalTextId wbIntTextId = new iPartsWorkBasketInternalTextId(getWbType(), saaBkKemNo);
            DBDataObjectAttribute etsUnconfirmedAttribute = new DBDataObjectAttribute(searchHelper.getEtsUnconfirmedAttribute(selectedAttributes));

            if (WorkBasketCollectedInternalTextForm.showInternalTextFormForWorkBasketEx(getConnector(), this,
                                                                                        wbIntTextId, etsUnconfirmedAttribute)) {
                EtkEditFields editFields = null;
                boolean hasInternalText = WorkBasketInternalTextCache.hasInternalText(getProject(), wbIntTextId.getWbType(), wbIntTextId.getSaaBkKemValue());
                DBDataObjectAttributes attributes = new DBDataObjectAttributes();
                if (searchHelper.isInternalTextSet(selectedAttributes) != hasInternalText) {
                    attributes.addField(searchHelper.getVirtualFieldNameInternalTextAvailable(),
                                        SQLStringConvert.booleanToPPString(hasInternalText), true, DBActionOrigin.FROM_DB);
                }
                String firstInternalText = WorkBasketInternalTextCache.getFirstInternalText(getProject(), wbIntTextId.getWbType(), wbIntTextId.getSaaBkKemValue());
                if (!searchHelper.getFirstInternalText(selectedAttributes).equals(firstInternalText)) {
                    attributes.addField(searchHelper.getVirtualFieldNameInternalText(),
                                        firstInternalText, true, DBActionOrigin.FROM_DB);
                }
                String followUpDate = WorkBasketInternalTextCache.getFollowUpDate(getProject(), wbIntTextId.getWbType(), wbIntTextId.getSaaBkKemValue());
                if (!searchHelper.getFollowUpDate(selectedAttributes).equals(followUpDate)) {
                    // Grid updaten
                    attributes.addField(searchHelper.getVirtualFieldNameFollowUpDate(),
                                        followUpDate, true, DBActionOrigin.FROM_DB);
                }
                if (!etsUnconfirmedAttribute.getAsString().equals(searchHelper.getEtsUnconfirmedAttribute(selectedAttributes).getAsString())) {
                    attributes.addField(etsUnconfirmedAttribute, DBActionOrigin.FROM_DB);
                    editFields = new EtkEditFields();
                    editFields.addFeld(searchHelper.getEditFieldForEtsUnconfirmed());
                }
                if (!attributes.isEmpty()) {
                    // alle Rows mit saaBkKemNo bestimmen
                    List<GuiTableRow> selectedRows = getRowsWithFieldValueEquals(searchHelper.getFieldNameSaaBkKem(), wbIntTextId.getSaaBkKemValue());
                    updateGrid(editFields, virtualFieldsList, attributes, selectedRows);
                }
            }
        }

    }

    private void showNutzdokAnnotation() {
        endSearch();

        DBDataObjectAttributes attributes = getSelectedAttributes();
        if (attributes != null) {
            String kemNo = searchHelper.getKemNo(attributes);
            if (StrUtils.isValid(kemNo)) {
                iPartsShowDataObjectsDialog.showNutzDokAnnotations(getConnector(), MasterDataEDSWorkBasketKEMForm.this,
                                                                   kemNo, iPartsWSWorkBasketItem.TYPE.KEM.name());
            }
        }
    }

    private void updateGrid(EtkEditFields editFields, List<String> virtualFieldNamesList, DBDataObjectAttributes attributes) {
        updateGrid(editFields, virtualFieldNamesList, attributes, null);
    }

    private void updateGrid(EtkEditFields editFields, List<String> virtualFieldNamesList, DBDataObjectAttributes attributes,
                            List<GuiTableRow> selectedRows) {
        OnUpdateGridEvent updateEvent = new OnUpdateGridEvent() {

            @Override
            public DBDataObjectAttributes doCalculateVirtualFields(EtkProject project, DBDataObjectAttributes attributes) {
                searchHelper.calculateVirtualFields(attributes);
                return attributes;
            }

            @Override
            public TableCellControlWithTextRepresentation doCalcGuiElemForCell(EtkProject project, EtkDisplayField field, DBDataObjectAttributes attributes) {
                return calcGuiElemForCell(field, attributes);
            }
        };
        UpdateGridHelper helper = new UpdateGridHelper(getProject(), getTable(), displayResultFields, updateEvent);
        helper.updateGrid(editFields, attributes, virtualFieldNamesList, null, selectedRows);
    }

    @Override
    protected void prepareGuiForSearch() {
        super.prepareGuiForSearch();
        EdSWorkBasketKEMSearchFilterFactory filterFactory = new EdSWorkBasketKEMSearchFilterFactory(getProject());
        setColumnFilterFactory(filterFactory);
        getTable().setColumnFilterFactory(filterFactory);
    }

    @Override
    protected boolean executeExplicitSearch() {
        DBDataObjectAttributesList attributesList = new DBDataObjectAttributesList();
        saaConstructionNodeMap.clear();

        calcWhereFieldsAndValuesFromSearchFields(searchHelper);

        String[] sortFields = searchHelper.getSortFields();

        // für Reset-Sorting
        LinkedHashMap<String, Boolean> tableSortFields = new LinkedHashMap<>();
        for (String tableAndFieldName : sortFields) {
            tableSortFields.put(TableAndFieldName.getFieldName(tableAndFieldName), false);
        }
        setSortFields(tableSortFields);

        searchHelper.loadFromDB(attributesList);

        if (!searchWasCanceled()) {
            sortAndFillGrid(attributesList);
        }

        return true;
    }

    private void calcWhereFieldsAndValuesFromSearchFields(SearchWorkBasketEDSKEMHelper searchHelper) {
        String[] localWhereTableAndFields = null;
        String[] localWhereValues = null;

        // Vorbereitung der Suchfelder. Alle Suchfelder bekommen ein * angehängt. SAAs werden unformatiert.
        for (int lfdNr = 0; lfdNr < searchFields.size(); lfdNr++) {
            EditControl editCtrl = editControls.getControlByFeldIndex(lfdNr);
            if (editCtrl != null) {
                EditControlFactory ctrl = editCtrl.getEditControl();
                if (ctrl != null) {
                    String value = ctrl.getText().trim().toUpperCase();
                    if (StrUtils.isValid(value)) {
                        // Länge von searchValue auf die maximale Feldlänge beschränken, um DB-Fehler zu vermeiden
                        value = getProject().getConfig().getDBDescription().cutValueIfLongerThanFieldLength(value,
                                                                                                            ctrl.getTableName(),
                                                                                                            ctrl.getFieldName());
                        if (ctrl.getFieldName().equals(FIELD_DKWB_SAA)) {
                            iPartsNumberHelper numberHelper = new iPartsNumberHelper();
                            value = numberHelper.unformatSaaBkForEdit(getProject(), value);

                            // Unterschiedliche Retail-SAA-Nummer vorhanden für die Konstruktions-SAA-Nummer?
                            // TODO Einkommentieren sobald auch in EDS "Z0*" durch "Z *" ersetzt wurde
//                            String retailSAA = numberHelper.getDifferentRetailSAA(value);
//                            if (retailSAA != null) {
//                                value = retailSAA;
//                            }
                        }
                        EtkFieldType fieldType = getProject().getFieldDescription(ctrl.getTableName(), ctrl.getFieldName()).getType();
                        EtkDisplayField searchField = searchFields.getFeld(lfdNr);
                        if (fieldType.isWildCardType() && !searchField.isSearchExact()) {
                            //eigenes WildCardSetting mit * am Ende
                            WildCardSettings wildCardSettings = new WildCardSettings();
                            wildCardSettings.addWildCardEnd();
                            value = wildCardSettings.makeWildCard(value);
                        }
                        localWhereTableAndFields = mergeArrays(localWhereTableAndFields, new String[]{ ctrl.getTableFieldName() });
                        localWhereValues = mergeArrays(localWhereValues, new String[]{ value });
                    }
                }
            }
        }

        searchHelper.setWhereFieldsAndValues(localWhereTableAndFields, localWhereValues);
    }

    @Override
    public void showProgress(int size) {
        if ((size % 50) == 0) { // Modulo, damits weniger Calls gibt
            Session.invokeThreadSafeInSession(() -> showHitCount(size));
        }
    }

    @Override
    public boolean searchWasCanceled() {
        FrameworkThread searchThreadLocal = searchThread;
        if ((searchThreadLocal != null) && !searchThreadLocal.wasCanceled()) {
            return false;
        }
        return true;
    }

    /**
     * Ergebnisse aus searchSortAndFillWithJoin() sortieren, weiter auswerten (docuRel) und anzeigen
     *
     * @param attributesList
     */
    private void sortAndFillGrid(DBDataObjectAttributesList attributesList) {
        if (J2EEHandler.isJ2EE()) {
            Session.invokeThreadSafeInSession(() -> doFillGrid(attributesList));
        } else {
            doFillGrid(attributesList);
        }
    }

    private void doFillGrid(DBDataObjectAttributesList attributesList) {
        getTable().switchOffEventListeners();
        try {
            for (DBDataObjectAttributes attributes : attributesList) {
                if (searchWasCanceled()) {
                    break;
                }
                // eine Zeile vorbereiten
                calculateOneLine(attributes);
            }
        } finally {
            showNoResultsLabel(getTable().getRowCount() == 0, false);
            if (!J2EEHandler.isJ2EE()) {
                Session.invokeThreadSafeInSession(() -> showResultCount());
            } else {
                showResultCount();
            }
            getTable().switchOnEventListeners();
        }
    }

    /**
     * Eine Zeile für die Ausgabe vorbereiten
     *
     * @param attributes
     */
    private void calculateOneLine(DBDataObjectAttributes attributes) {
        if (J2EEHandler.isJ2EE()) {
            processResultAttributes(attributes);
        } else {
            Session.invokeThreadSafeInSession(() -> processResultAttributes(attributes));
        }
    }

    @Override
    protected int processResultAttributes(DBDataObjectAttributes attributes) {
        showNoResultsLabel(false, false);

        if (doValidateAttributes(attributes)) {
            addAttributesToGrid(attributes);
            return 1;
        }
        return 0;
    }

    private String[] mergeArrays(String[] array1, String[] array2) {
        if (array1 != null) {
            return StrUtils.mergeArrays(array1, array2);
        }
        return array2;
    }

    /**
     * eigene FilterFactory für Spaltenfilter und FilterValues
     * Wird auch bei mehrfachen Filtern benötigt wegen der Pseudo-Tabelle (TABLE_WORK_BASKET_EDS)
     */
    public class EdSWorkBasketKEMSearchFilterFactory extends SimpleMasterDataSearchFilterFactory {

        public EdSWorkBasketKEMSearchFilterFactory(EtkProject project) {
            super(project);
        }

        @Override
        protected String getFilterValueFromVisObject(String value, EditControlFactory control) {
            // Hier können die Werte für einzelne Felder formatiert werden
            if (control.getTableFieldName().equals(TableAndFieldName.make(TABLE_DA_KEM_WORK_BASKET, FIELD_DKWB_SAA))) {
                iPartsNumberHelper numberHelper = new iPartsNumberHelper();
                return numberHelper.unformatSaaBkForEdit(getProject(), value);
            }
            return super.getFilterValueFromVisObject(value, control);
        }

        @Override
        protected List<SimpleSelectSearchResultGrid.GuiTableRowWithAttributes> calculateFilteredList() {
            List<SimpleSelectSearchResultGrid.GuiTableRowWithAttributes> entries = calculateFilteredListWithTableNameFromFilterItems();
            showHitCount(entries.size());
            return entries;
        }
    }
}
