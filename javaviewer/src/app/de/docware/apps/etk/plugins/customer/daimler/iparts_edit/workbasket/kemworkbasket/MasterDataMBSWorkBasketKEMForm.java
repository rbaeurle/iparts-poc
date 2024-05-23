/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.kemworkbasket;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.forms.common.EditControlFactory;
import de.docware.apps.etk.base.forms.common.SimpleSelectSearchResultGrid;
import de.docware.apps.etk.base.forms.events.OnDblClickEvent;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.ExtendedDataTypeLoadType;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsAuthorOrderStatus;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrderList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetSource;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataBOMConstKitContentList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataMBSPartlistList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsKEMWorkBasketMBSId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketInternalTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ImportExportLogHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.mbs.iPartsMBSModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs.MBSStructureId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataKEMWorkBasketEDS;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataKEMWorkBasketEDSList;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataKEMWorkBasketMBS;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataKEMWorkBasketMBSList;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditUserMultiChangeControls;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.SimpleMasterDataSearchFilterGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsGotoHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsLoadEditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsShowDataObjectsDialog;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditSessionKeyHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.iPartsWSWorkBasketItem;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.WorkBasketCollectedInternalTextForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.WorkbasketAutoCalculationHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.WorkbasketSearchCallback;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.WorkbasketSupplierMapping;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper.AbstractGoToConstructionContainer;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper.MbsKemDataSheetHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper.WorkBasketInternalTextCache;
import de.docware.framework.modules.config.db.EtkFieldType;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiButton;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.filechooser.FileChooserPurpose;
import de.docware.framework.modules.gui.controls.filechooser.GuiFileChooserDialog;
import de.docware.framework.modules.gui.controls.filechooser.MultipleInputToOutputStream;
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
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceStrongLRUList;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.TableAndFieldName;

import java.io.IOException;
import java.util.*;

public class MasterDataMBSWorkBasketKEMForm extends SimpleMasterDataSearchFilterGrid implements WorkbasketSearchCallback {


    private static final String CONFIG_KEY_WORK_BASKET_KEM_MBS = "Plugin/iPartsEdit/WorkBasket_KEM_MBS";

    private static final String TABLE_WORK_BASKET = iPartsDataVirtualFieldsDefinition.TABLE_WORK_BASKET;
    private static final String FIELD_KEM_CALC_DOCU_REL_MBS = iPartsDataVirtualFieldsDefinition.DKWM_CALC_DOCU_REL;
    private static final String FIELD_KEM_CASE_MBS = iPartsDataVirtualFieldsDefinition.DKWM_KEM_CASE;
    private static final String FIELD_KEM_NUTZDOK_REMARKS_AVAILABLE = iPartsDataVirtualFieldsDefinition.WB_REMARK_AVAILABLE;
    private static final String FIELD_KEM_NUTZDOK_INTERNAL_TEXT_AVAILABLE = iPartsDataVirtualFieldsDefinition.WB_INTERNAL_TEXT_AVAILABLE;
    private static final String FIELD_KEM_NUTZDOK_INTERNAL_TEXT = iPartsDataVirtualFieldsDefinition.WB_INTERNAL_TEXT;
    private static final String FIELD_KEM_NUTZDOK_FOLLOWUP_DATE = iPartsDataVirtualFieldsDefinition.WB_FOLLOWUP_DATE;

    private static final String GOTO_CONSTRUCTION_MENU_NAME = "GotoConstructionMBS";
    private static final String GOTO_CONSTRUCTION_WITH_EDIT_MENU_NAME = "GotoConstructionWithEditMBS";
    private static final String GOTO_RETAIL_MENU_NAME = "GotoRetailMBS";
    private static final String UNIFY_MENU_NAME = "UnifyMBS";
    private static final String INTERNAL_TEXT_KEM_MENU_NAME = "IntTextKEM";
    private static final String INTERNAL_TEXT_MENU_TEXT = "!!Internen Text/Wiedervorlage-Termin bearbeiten...";
    private static final String SHOW_NUTZDOK_REMARK_DATA_NAME = "ShowNutzdokRemarkData";
    private static final int MAX_CACHE_SIZE_GOTO_NODES = 100;
    private static final int CACHE_LIFETIME_GOTO_NODES = -1;

    private static final String UNIFY_FIELD_NAME = FIELD_DKWM_DOCU_RELEVANT;

    /**
     * Neue Instanz von MasterDataEDSWorkBasketForm erzeugen und Default-Sachen vorbesetzen
     *
     * @param dataConnector
     * @param parentForm
     * @return
     */
    public static MasterDataMBSWorkBasketKEMForm getNewMBSKEMWorkBasketInstance(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        MasterDataMBSWorkBasketKEMForm dlg = new MasterDataMBSWorkBasketKEMForm(dataConnector, parentForm);

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
        dlg.setTitlePrefix("!!MBS-KEM-Arbeitsvorrat");
        dlg.setWindowName("MBSKEMWorkBasketMasterData");
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
        searchFields.load(dataConnector.getConfig(), CONFIG_KEY_WORK_BASKET_KEM_MBS + iPartsEditConfigConst.REL_EDIT_MASTER_SEARCHFIELDS);
        if (searchFields.size() == 0) {
            // Suchfelder definieren
            searchFields.addFeld(createSearchField(dataConnector.getProject(), TABLE_DA_KEM_WORK_BASKET_MBS, FIELD_DKWM_PRODUCT_NO, false, false));
            searchFields.addFeld(createSearchField(dataConnector.getProject(), TABLE_DA_KEM_WORK_BASKET_MBS, FIELD_DKWM_KG, false, false));
            searchFields.addFeld(createSearchField(dataConnector.getProject(), TABLE_DA_KEM_WORK_BASKET_MBS, FIELD_DKWM_SAA, false, false));
            searchFields.addFeld(createSearchField(dataConnector.getProject(), TABLE_DA_KEM_WORK_BASKET_MBS, FIELD_DKWM_KEM, false, false));
        }
        return searchFields;
    }

    // Goto-Menus
    protected GuiMenu gotoConstructionMenu;
    protected GuiMenu gotoConstructionWithEditMenu;
    protected GuiMenu gotoRetailMenu;
    protected GuiMenu unifyMenu;
    protected GuiMenu intTextMenu;
    protected GuiMenu showNutzdokAnnotationMenu;
    protected GuiMenu showNutzdokRemarkDataMenu;
    protected GuiMenu showKemDataSheetMenu;
    protected boolean gotoNonModalWindow = true;  // true: Öffne Retail/Construction in eigenem Fenster
    protected iPartsWorkBasketTypes wbType;

    protected ObjectInstanceStrongLRUList<String, GotoConstructionContainer> gotoConstructionMap;

    //    protected Map<String, Boolean> saaBkDocuRelMap;       // Map ob SAA/BK DocuRel ist (SA-Flag)
//    protected Map<String, Boolean> saDocuRelMap;          // Map ob SA DocuRel ist
    protected Map<String, Boolean> mbsModelValidMap;      // Map ob Model in MBS valide ist
    protected WorkBasketNutzDokRemarkHelper nutzDokRemarkHelper; // Helper für NutzDokRemark
    protected SearchWorkBasketMBSKEMHelper searchHelper;

    private static final List<String> virtualFieldsList = new DwList<>();

    static {
        virtualFieldsList.add(TableAndFieldName.make(TABLE_DA_KEM_WORK_BASKET_MBS, FIELD_KEM_CALC_DOCU_REL_MBS));
        virtualFieldsList.add(TableAndFieldName.make(TABLE_DA_KEM_WORK_BASKET_MBS, FIELD_KEM_CASE_MBS));
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
    public MasterDataMBSWorkBasketKEMForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm, TABLE_DA_KEM_WORK_BASKET_MBS, null);
        MbsWorkBasketKEMSearchFilterFactory filterFactory = new MbsWorkBasketKEMSearchFilterFactory(getProject());
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
        gotoConstructionMap = new ObjectInstanceStrongLRUList<>(MAX_CACHE_SIZE_GOTO_NODES, CACHE_LIFETIME_GOTO_NODES);

        nutzDokRemarkHelper = new WorkBasketNutzDokRemarkHelper(this, getProject(), iPartsWSWorkBasketItem.TYPE.KEM);

        searchHelper = new SearchWorkBasketMBSKEMHelper(getProject(), this, nutzDokRemarkHelper);
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
                        WorkbasketAutoCalculationHelper.calculateMbsKemWorkbasket(getProject(), tempDir, supplierMapping);
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
        int insertIndex = 0;
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

        showNutzdokRemarkDataMenu = toolbarHelper.createMenuEntry(SHOW_NUTZDOK_REMARK_DATA_NAME, "!!Nutzdok-Bemerkungstexte anzeigen", null, null, getUITranslationHandler());
        list.add(showNutzdokRemarkDataMenu);

        showKemDataSheetMenu = toolbarHelper.createMenuEntry(EditToolbarButtonAlias.EDIT_IMPORT_CTT.getAlias(),
                                                             "!!KEM-Blatt anzeigen", null, new EventListener(Event.MENU_ITEM_EVENT) {
                    @Override
                    public void fire(Event event) {
                        showKemDataSheet();
                    }
                }, getUITranslationHandler());
        list.add(showKemDataSheetMenu);

        intTextMenu = toolbarHelper.createMenuEntry(INTERNAL_TEXT_KEM_MENU_NAME, INTERNAL_TEXT_MENU_TEXT, EditDefaultImages.edit_btn_internal_text.getImage(), new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
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

        list.addAll(getTable().getContextMenu().getChildren());
        getTable().getContextMenu().removeAllChildren();
        for (AbstractGuiControl guiCtrl : list) {
            getTable().getContextMenu().addChild(guiCtrl);
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
        if (fieldName.equals(FIELD_DKWM_SAA)) {
            value = iPartsNumberHelper.formatPartNo(getProject(), attributes.getFieldValue(fieldName));
        } else {
            value = getVisualValueOfFieldValue(tableName, fieldName, attributes.getField(fieldName), field.isMultiLanguage());
        }
        if (fieldName.equals(FIELD_DKWM_DOCU_RELEVANT) || fieldName.equals(FIELD_KEM_CALC_DOCU_REL_MBS) ||
            fieldName.equals(FIELD_KEM_CASE_MBS)) {
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

    public iPartsWorkBasketTypes getWbType() {
        return wbType;
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
        boolean isShowNutzDokRemarkDataEnabled = false;
        boolean isShowNutzDokAnnotationDataEnabled = false;
        boolean isShowKemDataSheetEnabled = false;

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
                isShowNutzDokRemarkDataEnabled = nutzDokRemarkHelper.prepareNutzDokSubMenus(showNutzdokRemarkDataMenu, searchHelper.getKemNo(selectedAttrib));
                isShowNutzDokAnnotationDataEnabled = nutzDokRemarkHelper.isNutzDokAnnotationMenuEnabled(searchHelper.getKemNo(selectedAttrib));
                isShowKemDataSheetEnabled = MbsKemDataSheetHelper.isKemDataSheetEnabled() &&
                                            MbsKemDataSheetHelper.isKemDataSheetDirValid(false);
            }
        }
        gotoConstructionMenu.setEnabled(isSingleConstructionSelected);
        gotoConstructionWithEditMenu.setEnabled(isSingleConstructionWithEditSelected && isSingleRetailSelected);
        gotoRetailMenu.setEnabled(isSingleRetailSelected);
        unifyMenu.setEnabled(isUnifyEnabled);
        intTextMenu.setEnabled((selectedAttribList != null) && (selectedAttribList.size() == 1));
        showNutzdokRemarkDataMenu.setEnabled(isShowNutzDokRemarkDataEnabled);
        showNutzdokAnnotationMenu.setEnabled(isShowNutzDokAnnotationDataEnabled);
        showKemDataSheetMenu.setVisible(isShowKemDataSheetEnabled);
        showKemDataSheetMenu.setEnabled(isShowKemDataSheetEnabled);
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
            String conGroup = searchHelper.getConGroup(attributes);
            if (StrUtils.isValid(saaBkNo)) {
                String foundModelNo = findModelForGoto(getProject(), saaBkNo, searchHelper.getProductNo(attributes),
                                                       searchHelper.getModuleNo(attributes));
                if (StrUtils.isValid(foundModelNo)) {
                    String key = foundModelNo + "&" + saaBkNo + "&" + conGroup;
                    GotoConstructionContainer gotoContainer = gotoConstructionMap.get(key);
                    if (gotoContainer == null) {
                        gotoContainer = new GotoConstructionContainer(getProject(), saaBkNo, searchHelper.getKemNo(attributes), conGroup, foundModelNo);
                        gotoConstructionMap.put(key, gotoContainer);
                    }
                    gotoContainer.gotoMBSConstruction(withOwnDialog, withLoadInEdit);
                }
            }
        }
    }

    private String findModelForGoto(EtkProject project, String saaNumber, String productNo, String moduleNo) {
        boolean isProductValid = StrUtils.isValid(productNo);
        Set<String> productModels = null;
        if (isProductValid) {
            if (StrUtils.isValid(moduleNo)) {
                EtkDataAssembly etkAssembly = EtkDataObjectFactory.createDataAssembly(getProject(), new AssemblyId(moduleNo, ""));
                if (etkAssembly instanceof iPartsDataAssembly) {
                    iPartsDataAssembly assembly = (iPartsDataAssembly)etkAssembly;
                    if (assembly.isSAAssembly()) {
                        // bei verorteten SA-TU die BM-Überprüfung ausschalten
                        isProductValid = false;
                    }

                }
            }
            if (isProductValid) {
                // zur Suche des 'besten' MBS-SAA die BM des Produkts holen
                productModels = iPartsProduct.getInstance(project, new iPartsProductId(productNo)).getModelNumbers(project);
            }
        }
        DBDataObjectAttributesList modelEntries = loadModelsForSaa(getProject(), saaNumber);
        if (!modelEntries.isEmpty()) {
            // Suche nach dem 'geeignetsten' Model
            for (DBDataObjectAttributes attrib : modelEntries) {
                String modelNo = attrib.getFieldValue(FIELD_DSM_SNR);
                Boolean isModelValid = mbsModelValidMap.get(modelNo);
                if (isModelValid == null) {
                    // BM-Nummer muss syntaktisch richtig sein (keine UHU-Nummern) und sollte in
                    // 1. Näherung auch in MBS-Konstruktion vorhanden sein
                    isModelValid = iPartsModel.isModelNumberValid(modelNo) && iPartsMBSModel.isValidMBSModel(modelNo, getProject());
                    mbsModelValidMap.put(modelNo, isModelValid);
                }
                if (isModelValid) {
                    if (isProductValid) {
                        if (productModels.contains(modelNo)) {
                            return modelNo;
                        }
                    } else {
                        return modelNo;
                    }
                }
            }
        }
        return null;
    }

    /**
     * aus der STRUCTURE_MBS die BM-Nummern holen mit {@param saaNumber) als SUB_SNR
     *
     * @param project
     * @param saaNumber
     * @return
     */
    public static DBDataObjectAttributesList loadModelsForSaa(EtkProject project, String saaNumber) {
        String[] fields = new String[]{ FIELD_DSM_SNR, FIELD_DSM_SUB_SNR, FIELD_DSM_RELEASE_TO };
        DBDataObjectAttributesList attributesList = project.getDbLayer().getAttributesList(TABLE_DA_STRUCTURE_MBS, fields,
                                                                                           new String[]{ FIELD_DSM_SUB_SNR },
                                                                                           new String[]{ saaNumber },
                                                                                           ExtendedDataTypeLoadType.NONE,
                                                                                           false, true);
        attributesList.sort(true, new String[]{ FIELD_DSM_RELEASE_TO });
        return attributesList;
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

            List<iPartsDataKEMWorkBasketMBS> kemWorkBasketList = new DwList<>(attributeList.size());
            for (DBDataObjectAttributes attrib : attributeList) {
                iPartsKEMWorkBasketMBSId id = new iPartsKEMWorkBasketMBSId(attrib);
                iPartsDataKEMWorkBasketMBS dataKEMWorkBasket = new iPartsDataKEMWorkBasketMBS(getProject(), id);
                dataKEMWorkBasket.assignAttributes(getProject(), attrib, true, DBActionOrigin.FROM_DB);
                dataKEMWorkBasket.removeForeignTablesAttributes();
                kemWorkBasketList.add(dataKEMWorkBasket);
            }

            EtkEditFields editFields = new EtkEditFields();
            addEditField(searchTable, UNIFY_FIELD_NAME, false, null, getProject(), editFields);
            // Anzeige des Unify-Dialogs
            DBDataObjectAttributes attributes =
                    EditUserMultiChangeControls.showEditUserMultiChangeControlsForKemWorkBasketMBS(getConnector(), editFields,
                                                                                                   kemWorkBasketList);
            if (attributes != null) {
                for (EtkEditField editField : editFields.getFields()) {
                    String fieldName = editField.getKey().getFieldName();
                    String fieldValue = attributes.getFieldValue(fieldName);
                    for (iPartsDataKEMWorkBasketMBS dataKEMWorkBasket : kemWorkBasketList) {
                        dataKEMWorkBasket.setFieldValue(fieldName, fieldValue, DBActionOrigin.FROM_EDIT);
                    }
                }
                GenericEtkDataObjectList genericList = new GenericEtkDataObjectList();
                genericList.addAll(kemWorkBasketList, DBActionOrigin.FROM_EDIT);

                if (iPartsRevisionChangeSet.saveDataObjectListWithChangeSet(getProject(), genericList, iPartsChangeSetSource.KEM_WORKBASKET_MBS)) {
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

        DBDataObjectAttributesList attributeList = getSelectedAttributesList();
        if ((attributeList != null) && !attributeList.isEmpty()) {
            DBDataObjectAttributes selectedAttributes = attributeList.get(0);
            String saaBkKemNo = searchHelper.getSaaBkKemValue(selectedAttributes);
            if (StrUtils.isValid(saaBkKemNo)) {
                iPartsWorkBasketInternalTextId wbIntTextId = new iPartsWorkBasketInternalTextId(getWbType(), saaBkKemNo);
                iPartsWSWorkBasketItem.TYPE refType = iPartsWSWorkBasketItem.TYPE.SAA;
                if (wbIntTextId.isKEM()) {
                    refType = iPartsWSWorkBasketItem.TYPE.KEM;
                }
                iPartsShowDataObjectsDialog.showNutzDokAnnotations(getConnector(), MasterDataMBSWorkBasketKEMForm.this,
                                                                   saaBkKemNo, refType.name());
            }
        }
    }

    private void showKemDataSheet() {
        endSearch();

        DBDataObjectAttributesList attributeList = getSelectedAttributesList();
        if ((attributeList != null) && !attributeList.isEmpty()) {
            DBDataObjectAttributes selectedAttributes = attributeList.get(0);
            String saaBkKemNo = searchHelper.getSaaBkKemValue(selectedAttributes);

            MbsKemDataSheetHelper kemHelper = new MbsKemDataSheetHelper(saaBkKemNo);
            DWFile root = MbsKemDataSheetHelper.getKemDataSheetRootDir();
            DWFile file = DWFile.get(root, kemHelper.getKemFileName());
            if (file.isFile()) {
                try {
                    MultipleInputToOutputStream stream = new MultipleInputToOutputStream(file);
                    GuiFileChooserDialog fileChooserDialog = new GuiFileChooserDialog(getParentForm().getGui(), FileChooserPurpose.SAVE, GuiFileChooserDialog.FILE_MODE_FILES, null, false);
                    fileChooserDialog.addChoosableFileFilter(MbsKemDataSheetHelper.KEM_SHEET_FILE_DESCRIPTION, MbsKemDataSheetHelper.KEM_SHEET_FILE_EXTENSION);
                    fileChooserDialog.setActiveFileFilter(MbsKemDataSheetHelper.KEM_SHEET_FILE_DESCRIPTION);
                    fileChooserDialog.setVisible(stream);
                } catch (IOException e) {
                    Logger.getLogger().throwRuntimeException(e);
                }
            } else {
                String msg = TranslationHandler.translate("!!KEM-Blatt \"%1\" für KEM \"%2\" ist nicht vorhanden.", file.getName(), saaBkKemNo);
                msg += "\n\n  (" + kemHelper.getKemFileName() + ")";
                MessageDialog.showError(msg);
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
        MbsWorkBasketKEMSearchFilterFactory filterFactory = new MbsWorkBasketKEMSearchFilterFactory(getProject());
        setColumnFilterFactory(filterFactory);
        getTable().setColumnFilterFactory(filterFactory);
    }

    @Override
    protected boolean executeExplicitSearch() {
        DBDataObjectAttributesList attributesList = new DBDataObjectAttributesList();
        gotoConstructionMap.clear();
        mbsModelValidMap = new HashMap<>();

        calcWhereFieldsAndValuesFromSearchFields(searchHelper);

        String[] sortFields = searchHelper.getSortFields();

        // für Reset-Sorting
        LinkedHashMap<String, Boolean> tableSortFields = new LinkedHashMap<>();
        for (String tableAndFieldName : sortFields) {
            tableSortFields.put(TableAndFieldName.getFieldName(tableAndFieldName), false);
        }
        setSortFields(tableSortFields);

        if (Constants.DEVELOPMENT && !Constants.DEVELOPMENT_QFTEST) {
            // TEST-Routine für die Erzeugung des MBS-KEM-WorkBaskets
//            dryTestForCalcMbsKemWorkBasket(getProject(), false);
        }

        searchHelper.loadFromDB(attributesList);

        if (!searchWasCanceled()) {
            sortAndFillGrid(attributesList);
        }

        return true;
    }

    private void calcWhereFieldsAndValuesFromSearchFields(SearchWorkBasketMBSKEMHelper searchHelper) {
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
                        if (ctrl.getFieldName().equals(FIELD_DKWM_SAA)) {
                            iPartsNumberHelper numberHelper = new iPartsNumberHelper();
                            value = numberHelper.unformatSaaBkForEdit(getProject(), value);

                            // Unterschiedliche Retail-SAA-Nummer vorhanden für die Konstruktions-SAA-Nummer?
                            String retailSAA = numberHelper.getDifferentRetailSAA(value);
                            if (retailSAA != null) {
                                value = retailSAA;
                            }
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

    public void showProgress(int size) {
        if ((size % 50) == 0) { // Modulo, damits weniger Calls gibt
            Session.invokeThreadSafeInSession(() -> showHitCount(size));
        }
    }

    public boolean searchWasCanceled() {
        FrameworkThread searchThreadLocal = searchThread;
        if ((searchThreadLocal != null) && !searchThreadLocal.wasCanceled()) {
            return false;
        }
        return true;
    }

    /**
     * Test-Routine für die Erzeugung des MBS-KEM-WorkBaskets
     * da die Original-Routine am Ende eines Importers läuft, hier die Möglichkeit gezielt zu debuggen
     * (es ist die (keicht) modifizierte Routine aus iPartsWSConstructionKitsEndpoint
     *
     * @param project
     * @param doSave
     */
    private void dryTestForCalcMbsKemWorkBasket(EtkProject project, boolean doSave) {
        // simulierte KemNo aus dem Import
        Set<String> kemNoSet = new HashSet<>();
//        kemNoSet.add("ULS706");
//        kemNoSet.add("UFS12511N02");
////            kemNoSet.add("ZAM159111N08");
//        kemNoSet.add("UFS2999N01");
//        kemNoSet.add("UAS12410N07");
////        kemNoSet.add("ULS115N15");
//        kemNoSet.add("LUS208N04");
//        kemNoSet.add("UFS5410N03");
        kemNoSet.add("ZAA350006N08");

        if (!kemNoSet.isEmpty()) {
            // 1.Schritt: Bestimme aus den importierten KEMNos die Liste der KemNos, die in MBS vorkommen
            Set<String> usedKemNoSetInMBS = iPartsDataMBSPartlistList.getUsedKemNosFromMBSPartlistBig(project, kemNoSet);

            // Erzeugung des EDS WorkBaskets überspringen
//        // 2.Schritt: entferne aus der KemNoSet alle MBS-KEMs
//        kemNoSet.removeIf(kemNo -> usedKemNoSetInMBS.contains(kemNo));
//        // 3.Schritt Suche EDS Verwendung in Retail
//        if (!kemNoSet.isEmpty()) {
//            String message = TranslationHandler.translateForLanguage("!!Davon %1 KEMs für EDS", iPartsConst.LOG_FILES_LANGUAGE,
//                                                                     Integer.toString(kemNoSet.size()));
//            logHelper.fireEvent(new MessageEventData(message, MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP));
//
//            calcWorkBasketItemsForKemEdsOnly(project, dataObjectsToBeSaved, kemNoSet, logHelper);
//        }

            if (!usedKemNoSetInMBS.isEmpty()) {
//                String message = TranslationHandler.translateForLanguage("!!Davon %1 KEMs für MBS", iPartsConst.LOG_FILES_LANGUAGE,
//                                                                         Integer.toString(kemNoSet.size()));
//                logHelper.fireEvent(new MessageEventData(message, MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP));

                // 4.Schritt: bestimme die SAAs aus MBS
                Map<String, Set<MBSStructureId>> kemSaaMap = iPartsDataMBSPartlistList.getValidSaasFromKEM(project, usedKemNoSetInMBS, true);
                if (!kemSaaMap.isEmpty()) {
                    // 5.Schritt Suche MBS Verwendung in Retail
                    iPartsDataKEMWorkBasketMBSList list = iPartsDataKEMWorkBasketMBSList.checkUsageInRetail(project, kemSaaMap);
                    // und speichern in allgemeiner Liste
                    if (!list.isEmpty() && doSave) {
                        list.saveToDB(project);
                    }
//                    for (iPartsDataKEMWorkBasketMBS dataKEMWorkBasket : list) {
//                        dataObjectsToBeSaved.add(dataKEMWorkBasket, DBActionOrigin.FROM_EDIT);
//                    }
                }
            } else {
                calcWorkBasketItemsForKemEdsOnly(project, null, kemNoSet, null);
            }
        }
    }

    private void calcWorkBasketItemsForKemEdsOnly(EtkProject project, GenericEtkDataObjectList<EtkDataObject> dataObjectsToBeSaved,
                                                  Set<String> kemNoSet, ImportExportLogHelper logHelper) {
        Map<String, Set<String>> kemSaaMap = iPartsDataBOMConstKitContentList.getValidSaasFromKEM(project, kemNoSet);
        iPartsDataKEMWorkBasketEDSList list = iPartsDataKEMWorkBasketEDSList.checkUsageInRetail(project, kemSaaMap);
        for (iPartsDataKEMWorkBasketEDS dataKEMWorkBasket : list) {
//            dataObjectsToBeSaved.add(dataKEMWorkBasket, DBActionOrigin.FROM_EDIT);
        }
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
    public class MbsWorkBasketKEMSearchFilterFactory extends SimpleMasterDataSearchFilterFactory {

        public MbsWorkBasketKEMSearchFilterFactory(EtkProject project) {
            super(project);
        }

        @Override
        protected String getFilterValueFromVisObject(String value, EditControlFactory control) {
            // Hier können die Werte für einzelne Felder formatiert werden
            if (control.getTableFieldName().equals(TableAndFieldName.make(TABLE_DA_KEM_WORK_BASKET_MBS, FIELD_DKWM_SAA))) {
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

    /**
     * Container für die gotoMBS-Sprünge (mit Erweiterung aus DAIMLER-11259)
     */
    private class GotoConstructionContainer extends AbstractGoToConstructionContainer {

        private final String kemNo;  // aktuelle KEM-No
        private AssemblyId kemMarkAssemblyId;  // reale AssemblyId, die eigentlich angesprungen werden soll

        /**
         * Constructor
         *
         * @param saaBkNo
         * @param kemNo
         * @param conGroup
         * @param foundModelNo
         */
        public GotoConstructionContainer(EtkProject project, String saaBkNo, String kemNo, String conGroup, String foundModelNo) {
            super(project, foundModelNo, saaBkNo, conGroup);
            this.kemNo = kemNo;
        }


        /**
         * Laden der TUs im Editor
         *
         * @param withOwnDialog
         */
        private void loadInEdit(boolean withOwnDialog) {
            // alle zu KEM und SAA passenden Module im Retail laden
            boolean loadReadOnly = true;
            iPartsDataAuthorOrder currentAuthorOrder = iPartsDataAuthorOrderList.getAuthorOrderByActiveChangeSetForEdit(getProject());
            if (currentAuthorOrder != null) {
                loadReadOnly = iPartsAuthorOrderStatus.isEndState(currentAuthorOrder.getStatus());
            }
            List<AssemblyId> assemblyIdList = iPartsDataKEMWorkBasketMBSList.loadModulesByKEMAndSaaBk(getProject(),
                                                                                                      kemNo,
                                                                                                      getSaaBkNumber(),
                                                                                                      true);
            if (!assemblyIdList.isEmpty()) {
                // im Editor laden und kemNo übergeben
                // wenn KonstruktionsModul im eigenen Fenster geladen wurde => in Editor umschalten
                Set<String> selectedModulList = new HashSet<>();
                assemblyIdList.forEach(eachAssemblyId -> selectedModulList.add(eachAssemblyId.getKVari()));
                iPartsLoadEditModuleHelper.doLoadModules(getConnector(), selectedModulList, TranslationHandler.translate("!!Bezug zur KEM-Nummer: %1", kemNo),
                                                         loadReadOnly, withOwnDialog, null);
            }
        }

        @Override
        protected void setGoToAssemblyIdFromConGroup() {
            kemMarkAssemblyId = createAssemblyIdForConGroup();
            super.setGoToAssemblyIdFromConGroup();
        }

        /**
         * KemNo für Markierung in Assembly setzen
         */
        private void setKeyForMarkMBS() {
            if (kemMarkAssemblyId != null) {
                EditSessionKeyHelper.setSessionKeyForMarkMBS(kemMarkAssemblyId, kemNo);
            }
        }

        /**
         * KemNo für Markierung in Assembly zurücksetzen
         */
        private void resetKeyForMarkMBS() {
            if (kemMarkAssemblyId != null) {
                EditSessionKeyHelper.resetSessionKeyForMarkMBS(kemMarkAssemblyId);
            }
        }

        @Override
        protected boolean doGotoMBSConstruction(boolean withOwnDialog, boolean withLoadInEdit) {
            // KEM und Assembly für Markierung in der Stückliste merken
            setKeyForMarkMBS();
            boolean foundPath = findGoToPath(getConnector(), MasterDataMBSWorkBasketKEMForm.this, withOwnDialog);
            if (!foundPath) {
                resetKeyForMarkMBS();
            }
            if (withLoadInEdit) {
                loadInEdit(withOwnDialog);
            }
            return foundPath;
        }
    }
}
