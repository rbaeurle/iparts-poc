/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.saaworkbasket;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.*;
import de.docware.apps.etk.base.forms.events.OnDblClickEvent;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntryList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsAuthorOrderStatus;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrderList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetSource;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketInternalTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketSaaStatesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.iPartsMainImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataWorkBasketSaaStates;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsWBSaaStatesManagement;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditUserMultiChangeControls;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.SimpleMasterDataSearchFilterGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsGotoHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsLoadEditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsShowDataObjectsDialog;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.VisualizeSAAHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.iPartsWSWorkBasketItem;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.WorkBasketCollectedInternalTextForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.WorkbasketAutoCalculationHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.WorkbasketSupplierMapping;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper.AbstractAttribValuePacker;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper.SaaModelPacker;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper.WorkBasketInternalTextCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.kemworkbasket.OnUpdateGridEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.kemworkbasket.UpdateGridHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.kemworkbasket.WorkBasketNutzDokRemarkHelper;
import de.docware.framework.modules.config.db.EtkFieldType;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
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
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

public abstract class MasterDataAbstractWorkBasketForm extends SimpleMasterDataSearchFilterGrid implements WorkbasketSAASearchCallback {

    protected static final String TABLE_WORK_BASKET = iPartsDataVirtualFieldsDefinition.TABLE_WORK_BASKET;
    protected static final String FIELD_SAA_NUTZDOK_REMARKS_AVAILABLE = iPartsDataVirtualFieldsDefinition.WB_REMARK_AVAILABLE;
    protected static final String FIELD_MODEL_VALIDITY_EXTENSION = VirtualFieldsUtils.addVirtualFieldMask("MODEL_VALIDITY_EXTENSION");
    protected static final String FIELD_KEM_NUTZDOK_INTERNAL_TEXT_AVAILABLE = iPartsDataVirtualFieldsDefinition.WB_INTERNAL_TEXT_AVAILABLE;
    protected static final String FIELD_KEM_NUTZDOK_INTERNAL_TEXT = iPartsDataVirtualFieldsDefinition.WB_INTERNAL_TEXT;
    protected static final String FIELD_KEM_NUTZDOK_FOLLOWUP_DATE = iPartsDataVirtualFieldsDefinition.WB_FOLLOWUP_DATE;
    protected static final String FIELD_KEM_NUTZDOK_ETS_EXTENSION = iPartsDataVirtualFieldsDefinition.WB_ETS_EXTENSION;
    private static final EnumSet<iPartsEDSSaaCase> INVALID_SAA_CASES = EnumSet.of(iPartsEDSSaaCase.EDS_CASE_NOT_SPECIFIED,
                                                                                  iPartsEDSSaaCase.EDS_CASE_CHANGED,
                                                                                  iPartsEDSSaaCase.EDS_CASE_MODEL_VALIDITY_EXPANSION,
                                                                                  iPartsEDSSaaCase.EDS_CASE_SAA_VALIDITY_EXPANSION);

    private static final String GOTO_CONSTRUCTION_MENU_NAME = "GotoConstruction";
    private static final String GOTO_CONSTRUCTION_WITH_EDIT_MENU_NAME = "GotoConstructionWithEdit";
    private static final String GOTO_RETAIL_MENU_NAME = "GotoRetail";
    private static final String UNIFY_MENU_NAME = "Unify";
    private static final String SHOW_NUTZDOK_REMARK_DATA_NAME = "ShowNutzdokRemarkData";
    private static final String EXTEND_MODEL_VALIDITY_NAME = "ExtendModelValidity";
    private static final String INTERNAL_TEXT_SAA_MENU_NAME = "IntTextSAA";
    private static final String INTERNAL_TEXT_MENU_TEXT = "!!Internen Text/Wiedervorlage-Termin bearbeiten...";

    protected static final String ARRAYID_DELIMITER = ",";
    protected static final String MODEL_DELIMITER = ", ";
    private static final String PARTLIST_ENTRY_DELIMITER = "|";

    protected static final int ROW_COUNT_FOR_ADD_RESULTS = 300;
    protected static final int MAX_WAIT_TIME_MS_FOR_ADD_RESULTS = 2000; // 2 Sekunden
    protected static final int MIN_CHAR_COUNT_FOR_MODEL_AND_SAA = 4;

    protected final List<String> virtualFieldsList = new DwList<>();

    protected AbstractSearchWorkBasketHelper searchHelper;
    protected iPartsWorkBasketTypes wbType;
    protected Set<iPartsEDSSaaCase> searchSaaCase;

    protected volatile FrameworkThread changeSetSearchThread;

    protected Map<String, AbstractSearchWorkBasketHelper.KatalogData> attributesKatMap;  // Map für KatalogData-Objekte
    protected Map<AssemblyId, String> assemblyProductMap; // Map für Assembly zu Product
    protected iPartsWBSaaStatesManagement stateManager;
    protected WorkBasketNutzDokRemarkHelper nutzDokRemarkHelper; // Helper für NutzDokRemark
    private SaaModelPacker modelPacker;
    private int selectedIndexAfterUpdate;
    private Map<String, String> selectedFieldNamesValuesAfterUpdate;

    // Goto-Menus
    protected GuiMenu gotoConstructionMenu;
    protected GuiMenu gotoConstructionWithEditMenu;
    protected GuiMenu gotoRetailMenu;
    protected GuiMenu visualizeSAAMenu;
    protected boolean gotoNonModalWindow = true;  // true: Öffne Retail/Construction in eigenem Fenster
    protected GuiMenu unifyMenu;
    protected GuiMenu extendModelValidityMenu;
    protected GuiMenu showNutzdokRemarkDataMenu;
    protected GuiMenu intTextMenu;
    protected GuiMenu showNutzdokAnnotationMenu;

    /**
     * Erzeugt eine Instanz von SimpleMasterDataSearchResultGrid.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     * @param parentForm
     * @param virtualTableName
     * @param source
     */
    public MasterDataAbstractWorkBasketForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                            String virtualTableName, iPartsImportDataOrigin source) {
        super(dataConnector, parentForm, virtualTableName, null);
        EdSWorkBasketSearchFilterFactory filterFactory = new EdSWorkBasketSearchFilterFactory(getProject());
        setColumnFilterFactory(filterFactory);
        getTable().setColumnFilterFactory(filterFactory);
        doResizeWindow(SCREEN_SIZES.MAXIMIZE);

        if (Constants.DEVELOPMENT && !Constants.DEVELOPMENT_QFTEST) {
            gotoNonModalWindow = false;  // zum Debuggen ohne eigenes Fenster
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

        stateManager = new iPartsWBSaaStatesManagement(getProject(), source);
        attributesKatMap = Collections.synchronizedMap(new HashMap<>());
        assemblyProductMap = Collections.synchronizedMap(new HashMap<>());
        selectedIndexAfterUpdate = -1;
        selectedFieldNamesValuesAfterUpdate = null;


        addContextMenus();

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
                        WorkbasketAutoCalculationHelper.calculateSaaWorkbasket(getProject(), tempDir, getSource(), supplierMapping);
                        WorkbasketAutoCalculationHelper.finishExport(tempDir);
                    }
                }
                WorkbasketAutoCalculationHelper.downloadCalculatedData();
            }
        });
    }

    protected void setSearchHelper(AbstractSearchWorkBasketHelper searchHelper) {
        this.searchHelper = searchHelper;
        this.wbType = searchHelper.getWbType();
        virtualFieldsList.add(TableAndFieldName.make(getVirtualTableName(), getFieldDocuRel()));
        virtualFieldsList.add(TableAndFieldName.make(getVirtualTableName(), getFieldSaaCase()));
        virtualFieldsList.add(TableAndFieldName.make(getVirtualTableName(), getFieldManualStatus()));
        virtualFieldsList.add(TableAndFieldName.make(getVirtualTableName(), getFieldAuthorOrder()));
        virtualFieldsList.add(TableAndFieldName.make(TABLE_WORK_BASKET, getFieldSaaNutzdokRemarksAvailable()));
        virtualFieldsList.add(TableAndFieldName.make(TABLE_WORK_BASKET, FIELD_KEM_NUTZDOK_INTERNAL_TEXT_AVAILABLE));
        virtualFieldsList.add(TableAndFieldName.make(TABLE_WORK_BASKET, FIELD_KEM_NUTZDOK_INTERNAL_TEXT));
        virtualFieldsList.add(TableAndFieldName.make(TABLE_WORK_BASKET, FIELD_KEM_NUTZDOK_FOLLOWUP_DATE));
        virtualFieldsList.add(TableAndFieldName.make(TABLE_WORK_BASKET, FIELD_KEM_NUTZDOK_ETS_EXTENSION));
    }

    /**
     * Goto-Menus vor 'Copy' hinzufügen
     */
    protected void addContextMenus() {
        List<AbstractGuiControl> list = new DwList<>();
        gotoConstructionMenu = toolbarHelper.createMenuEntry(GOTO_CONSTRUCTION_MENU_NAME, "!!Gehe zu Konstruktion", null, new EventListener(Event.MENU_ITEM_EVENT,
                                                                                                                                            EventListenerOptions.SYNCHRON_EVENT) {
            @Override
            public void fire(Event event) {
                gotoConstruction(gotoNonModalWindow, false);
            }
        }, getUITranslationHandler());
        list.add(gotoConstructionMenu);

        gotoConstructionWithEditMenu = toolbarHelper.createMenuEntry(GOTO_CONSTRUCTION_WITH_EDIT_MENU_NAME, "!!Gehe zur Konstruktion mit Laden der TUs", null, new EventListener(Event.MENU_ITEM_EVENT,
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

        if (iPartsRight.AUTO_MODEL_VALIDITY_EXTENSION.checkRightInSession()) {
            extendModelValidityMenu = toolbarHelper.createMenuEntry(EXTEND_MODEL_VALIDITY_NAME, "!!Automatisierte Baumustererweiterung",
                                                                    null, new EventListener(Event.MENU_ITEM_EVENT) {
                        @Override
                        public void fire(Event event) {
                            extendModelValidity();
                        }
                    }, getUITranslationHandler());
            list.add(extendModelValidityMenu);
        }

        showNutzdokRemarkDataMenu = toolbarHelper.createMenuEntry(SHOW_NUTZDOK_REMARK_DATA_NAME, "!!Nutzdok-Bemerkungstexte anzeigen", null, null, getUITranslationHandler());
        list.add(showNutzdokRemarkDataMenu);

        intTextMenu = toolbarHelper.createMenuEntry(INTERNAL_TEXT_SAA_MENU_NAME, INTERNAL_TEXT_MENU_TEXT, EditDefaultImages.edit_btn_internal_text.getImage(), new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                editInternalText();
            }
        }, getUITranslationHandler());
        list.add(intTextMenu);

        showNutzdokAnnotationMenu = toolbarHelper.createMenuEntry("showNutzdokAnnotationMenu", WorkBasketNutzDokRemarkHelper.NUTZDOK_ANNOTATION_MENU_TEXT, EditDefaultImages.edit_btn_internal_text.getImage(), new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
//                cancelSearchThread();
                showNutzdokAnnotation();
            }
        }, getUITranslationHandler());
        list.add(showNutzdokAnnotationMenu);

        list.add(toolbarHelper.createMenuSeparator(VisualizeSAAHelper.IPARTS_MENU_ITEM_VISUALIZE_SAA_3D + "Separator", getUITranslationHandler()));
        visualizeSAAMenu = VisualizeSAAHelper.createVisualizeSAAPopupMenuItem(toolbarHelper, getUITranslationHandler(),
                                                                              new EventListener(Event.MENU_ITEM_EVENT) {
                                                                                  @Override
                                                                                  public void fire(Event event) {
                                                                                      VisualizeSAAHelper.visualizeSAAs(getSelectedSAAs());
                                                                                  }
                                                                              });
        list.add(visualizeSAAMenu);

        addCustomContextMenus(list);

        list.addAll(getTable().getContextMenu().getChildren());
        getTable().getContextMenu().removeAllChildren();
        for (AbstractGuiControl guiCtrl : list) {
            getTable().getContextMenu().addChild(guiCtrl);
        }
    }

    protected void addCustomContextMenus(List<AbstractGuiControl> list) {
    }

    public iPartsImportDataOrigin getSource() {
        return stateManager.getSource();
    }

    public iPartsWorkBasketTypes getWbType() {
        return wbType;
    }

    public void updateData() {
        endSearch();
        getTable().removeRows();
        setDisplayResultFields(getDisplayResultFields());
        clearGrid();
        searchHelper.updateData();
    }

    @Override
    public void setMaxResults(int maxResults) {
        super.setMaxResults(maxResults);
        if ((maxResults == -1) && J2EEHandler.isJ2EE()) {
            getTable().setPageSplitNumberOfEntriesPerPage(iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE);
        }
    }

    @Override
    protected void modifySearchControl(EtkDisplayField searchField, EditControl ctrl) {
        if (searchField.getKey().getName().equals(TableAndFieldName.make(getVirtualTableName(), getFieldSaaCase()))) {
            // in der ComboBox die Werte anpassen
            if (ctrl.getAbstractGuiControl() instanceof EnumRComboBox) {
                EnumRComboBox rCombo = (EnumRComboBox)ctrl.getAbstractGuiControl();
                rCombo.removeAllItems();
                rCombo.addToken("", "");
                for (iPartsEDSSaaCase saaCase : iPartsEDSSaaCase.values()) {
                    if (!INVALID_SAA_CASES.contains(saaCase)) {
                        rCombo.addToken(saaCase.getDbValue(), saaCase.getDisplayValue(getProject()));
                    }
                }
            }
        }
    }

    @Override
    protected boolean checkControlChange() {
        boolean isEnabled = super.checkControlChange();
        if (isEnabled) {
            boolean saaCaseHasValue = false;
            boolean otherSearchFieldsHasValue = false;
            for (int lfdNr = 0; lfdNr < editControls.size(); lfdNr++) {
                EditControlFactory ctrl = editControls.getControlByFeldIndex(lfdNr).getEditControl();
                if (ctrl.getTableFieldName().equals(TableAndFieldName.make(getVirtualTableName(), getFieldSaaCase()))) {
                    saaCaseHasValue = !ctrl.getText().isEmpty();
                } else {
                    if (!ctrl.getText().isEmpty()) {
                        otherSearchFieldsHasValue = true;
                    }
                }
            }
            if (saaCaseHasValue && !otherSearchFieldsHasValue) {
                isEnabled = false;
            }
        }
        return isEnabled;
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
        VarParam<String> resultValue = new VarParam<>();
        VarParam<String> resultHintValue = new VarParam<>();
        for (EtkDisplayField field : displayResultFields.getVisibleFields()) {
            calcValueAndHint(field, attributes, resultValue, resultHintValue);
            row.addChild(calcGuiElemForCell(field, resultValue.getValue(), resultHintValue.getValue()));
        }
        return row;
    }

    private void calcValueAndHint(EtkDisplayField field, DBDataObjectAttributes attributes,
                                  VarParam<String> resultValue, VarParam<String> resultHintValue) {
        String value = "";
        String hintValue = "";
        if (field.isVisible()) {
            String tableName = field.getKey().getTableName();
            String fieldName = field.getKey().getFieldName();
            if (isSaaDisplayFieldName(fieldName)) {
                value = iPartsNumberHelper.formatPartNo(getProject(), attributes.getFieldValue(getFieldSaa()));
            } else if (fieldName.equals(FIELD_DWA_ARRAYID)) {
                boolean isUsedInAS = false;
                if (StrUtils.isValid(attributes.getFieldValue(fieldName))) {
                    isUsedInAS = true;
                }
                DBDataObjectAttribute dummyAttrib = new DBDataObjectAttribute(FIELD_DPM_MODEL_VISIBLE, DBDataObjectAttribute.TYPE.STRING, false);
                dummyAttrib.setValueAsBoolean(isUsedInAS, DBActionOrigin.FROM_DB);
                value = getVisualValueOfFieldValue(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_MODEL_VISIBLE, dummyAttrib, field.isMultiLanguage());
            } else if (tableName.equals(getVirtualTableName())) {
                if (fieldName.equals(getFieldDocuRel()) || fieldName.equals(getFieldSaaCase())) {
                    value = getVisualValueOfFieldValue(tableName, fieldName, attributes.getField(fieldName), field.isMultiLanguage());
                    if (StrUtils.isValid(attributes.getFieldValue(getFieldDocuRelReason()))) {
                        hintValue = attributes.getFieldValue(getFieldDocuRelReason());
                    }
                } else if (fieldName.equals(getFieldAuthorOrder())) {
                    value = attributes.getFieldValue(fieldName);
                } else {
                    value = getVisualValueOfFieldValue(tableName, fieldName, attributes.getField(fieldName), field.isMultiLanguage());
                }
            } else if (fieldName.equals(FIELD_DM_NAME) || fieldName.equals(FIELD_DM_SALES_TITLE) || fieldName.equals(FIELD_DM_ADD_TEXT)) {
                value = searchHelper.getModelValueForHint(fieldName, attributes);
                attributes.addField(fieldName, value, DBActionOrigin.FROM_DB);
            } else {
                value = getVisualValueOfFieldValue(tableName, fieldName, attributes.getField(fieldName), field.isMultiLanguage());
            }

        }
        resultValue.setValue(value);
        resultHintValue.setValue(hintValue);
    }

    private TableCellControlWithTextRepresentation calcGuiElemForCell(EtkDisplayField field, String value, String hintValue) {
        String fieldName = field.getKey().getFieldName();

        GuiLabel label = new GuiLabel(value);
        if (fieldName.equals(getFieldDocuRel()) || fieldName.equals(getFieldManualStatus()) || fieldName.equals(getFieldSaaCase())) {
            GuiPanel panel = new GuiPanel(new LayoutGridBag(false));
            panel.setBackgroundColor(Colors.clTransparent.getColor());
            label.setConstraints(new ConstraintsGridBag(0, 0, 1, 1, 0, 1, LayoutGridBag.ANCHOR_CENTER, LayoutGridBag.FILL_HORIZONTAL, 0, 2, 0, 0));
            panel.addChild(label);
            if (StrUtils.isValid(hintValue)) {
                panel.setTooltip(hintValue);
                label.setTooltip(hintValue);
            }
            return new TableCellControlWithTextRepresentation(panel, () -> label.getTextRepresentation());
        }
        return new TableCellControlWithTextRepresentation(label, () -> label.getTextRepresentation());
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
     * Callback für Goto-Construction
     *
     * @param withOwnDialog
     */
    protected abstract void gotoConstruction(boolean withOwnDialog, boolean withLoadInEdit);

    /**
     * Callback füt Goto-Retail
     *
     * @param withOwnDialog
     */
    private void gotoRetail(boolean withOwnDialog) {
        DBDataObjectAttributes attributes = getSelectedAttributes();
        if (attributes != null) {
            PartListEntryId partListEntryId = getPartListEntryId(attributes);
            if (partListEntryId != null) {
                iPartsGotoHelper.gotoRetail(getConnector(), this, withOwnDialog, partListEntryId);
            }
        }
    }

    private void unifyDocuRel() {
        endSearch();

        DBDataObjectAttributesList attributeList = getSelectedAttributesList();
        if (attributeList != null) {
            List<iPartsWorkBasketSaaStatesId> idList = new DwList<>(attributeList.size());
            for (DBDataObjectAttributes attrib : attributeList) {
                List<String> models = getPackModelNumbers(attrib);
                String productNo = getProductNo(attrib);
                String saaBkNo = getSaaBkNo(attrib);
                for (String modelNo : models) {
                    idList.add(new iPartsWorkBasketSaaStatesId(modelNo, productNo, saaBkNo, getSource().getOrigin()));
                }
            }
            List<iPartsDataWorkBasketSaaStates> dataWBSStateList = stateManager.getWBSStateListForUnify(idList);
            EtkEditFields editFields = new EtkEditFields();
            addEditField(TABLE_DA_WB_SAA_STATES, FIELD_WBS_DOCU_RELEVANT, false, null, getProject(), editFields);
            // Anzeige des Unify-Dialogs
            DBDataObjectAttributes attributes =
                    EditUserMultiChangeControls.showEditUserMultiChangeControlsForSaaWorkBasket(getConnector(), editFields,
                                                                                                dataWBSStateList);
            if (attributes != null) {
                GenericEtkDataObjectList genericList = stateManager.addUnifyResult(dataWBSStateList, attributes);

                if (iPartsRevisionChangeSet.saveDataObjectListWithChangeSet(getProject(), genericList, iPartsChangeSetSource.SAA_WORKBASKET)) {
                    Set<String> modelNoList = new TreeSet<>();
                    dataWBSStateList.forEach((wbsState) -> {
                        modelNoList.add(wbsState.getAsId().getModelNo());
                    });
                    stateManager.addModels(modelNoList);

                    Session.invokeThreadSafeInSession(() -> {
                        boolean packUpdate = (getTable().getSortColumn() != -1) || !getFilteredColumns().isEmpty();

                        String fieldValue = attributes.getFieldValue(iPartsConst.FIELD_WBS_DOCU_RELEVANT);
                        iPartsDocuRelevantTruck docuRel = iPartsDocuRelevantTruck.getFromDBValue(fieldValue);
                        if (docuRel != iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NOT_SPECIFIED) {
                            DBDataObjectAttributes selectedAttrib = attributeList.get(0);
                            selectedFieldNamesValuesAfterUpdate = new HashMap<>();
                            selectedFieldNamesValuesAfterUpdate.put(FIELD_DPM_PRODUCT_NO, getProductNo(selectedAttrib));
                            selectedFieldNamesValuesAfterUpdate.put(getFieldSaa(), getSaaBkNo(selectedAttrib));
                            selectedFieldNamesValuesAfterUpdate.put(getFieldDocuRel(), docuRel.getDbValue());
                        }
                        int[] selectedIndices = getTable().getSelectedRowIndices();
                        if (selectedIndices.length > 0) {
                            selectedIndexAfterUpdate = selectedIndices[0];
                        }
                        DBDataObjectAttributesList attribList = null;
                        if (!packUpdate) {
                            attribList = modelPacker.rebuildHitList(searchHelper.getSortFields());
                        }
                        if ((attribList != null) && !attribList.isEmpty()) {
                            // die gebildete nicht verdichtete Attributes-Liste ist gültig => fülle Grid neu
                            modelPacker.clear();
                            getTable().switchOffEventListeners();
                            try {
                                String today = SQLStringConvert.calendarToPPDateTimeString(Calendar.getInstance());
                                getTable().removeRows();
                                for (DBDataObjectAttributes attrib : attribList) {
                                    calculateOneLine(attrib, attributesKatMap, assemblyProductMap, today);
                                }
                            } finally {
                                getTable().switchOnEventListeners();
                            }
                            selectedFieldNamesValuesAfterUpdate = null;
                            setSelectionAfterUpdate();
                        } else {
                            if (modelPacker.isPackingDisabled()) {
                                // keine Verdichtung => direktes Update der Werte
                                updateGridDirect(editFields, attributes);
                                selectedFieldNamesValuesAfterUpdate = null;
                                selectedIndexAfterUpdate = -1;
                            } else {
                                // Verdichtung aktiv, ohne alles
                                startSearch();
                            }
                        }
                    });
//                    updateGridDirect(editFields, attributes);

// Bitte stehen lassen als Gedächtnisstütze
// updateGrid() durch erneute Suche und Filter/Sortierung wieder setzen
//                    DBDataObjectAttributes[] markArray = ArrayUtil.toArray(attributeList);
//                    final Object storedFilterAndSorting = getFilterAndSortSettings(true);
//                    OnSearchFinishedEvent onSearchFinishedSelection = new OnSearchFinishedEvent() {
//                        @Override
//                        public void OnSearchFinishedEvent() {
//                            restoreFilterAndSortSettings(storedFilterAndSorting);
//                            setSelection(true, markArray);
//                        }
//                    };
//                    setSelectionAfterSearch(onSearchFinishedSelection);
//                    startSearch();
                }
            }
        }
    }

    private void extendModelValidity() {
        DBDataObjectAttributesList attributeList = getSelectedAttributesList();
        if (attributeList != null) {
            // Nur BM-Gültigkeitserweiterung ist relevant
            DBDataObjectAttributesList relevantAttributesList = new DBDataObjectAttributesList();
            for (DBDataObjectAttributes attributes : attributeList) {
                if (getSaaCase(attributes) == iPartsEDSSaaCase.EDS_CASE_MODEL_VALIDITY_EXPANSION) {
                    relevantAttributesList.add(attributes);
                }
            }
            if (relevantAttributesList.isEmpty()) {
                return;
            }

            if (MessageDialog.showYesNo(TranslationHandler.translate("!!Sollen die Baumuster-Gültigkeiten von %1 ausgewählten Datensätzen automatisiert erweitert werden?",
                                                                     String.valueOf(relevantAttributesList.size())),
                                        "!!Automatisierte Baumustererweiterung") != ModalResult.YES) {
                return;
            }

            EtkMessageLogForm messageLogForm = new EtkMessageLogForm("!!Automatisierte Baumustererweiterung", "!!Fortschritt", null);
            messageLogForm.setAutoClose(true);
            messageLogForm.getGui().setHeight(250);
            messageLogForm.showModal(getRootParentWindow(), new FrameworkRunnable() {
                @Override
                public void run(FrameworkThread thread) {
                    int currentPos = 0;
                    int maxPos = 3 * relevantAttributesList.size();
                    fireMessage("!!Erweitere Baumuster-Gültigkeiten von %1 ausgewählten Datensätzen in Retail-Stücklisten...",
                                String.valueOf(relevantAttributesList.size()));
                    fireProgress(currentPos, maxPos, false);

                    // Retail-Verwendungen suchen und BM-Gültigkeiten erweitern
                    EtkProject project = getProject();
                    Set<AssemblyId> retailAssemblyNeedsModelExtensionSet = new HashSet<>();
                    GenericEtkDataObjectList<EtkDataPartListEntry> modifiedPartListEntries = new GenericEtkDataObjectList<>();
                    for (DBDataObjectAttributes attributes : relevantAttributesList) {
                        String productNo = getProductNo(attributes);
                        String saaBkNo = getSaaBkNo(attributes);
                        String modelNumber = getModelNumber(attributes);
                        String arrayIds = getArrayId(attributes);
                        List<String> arrayIdList = getArrayIdList(arrayIds);
                        for (String arrayId : arrayIdList) {
                            AbstractSearchWorkBasketHelper.KatalogData katalogData = attributesKatMap.get(arrayId);
                            if (katalogData != null) {
                                PartListEntryId partListEntryId = katalogData.getPartListEntryId();

                                // Die BM-Gültigkeit nur in Modulen des Produkts vom aktuellen Datensatz verändern
                                String productNoFromPLE = searchHelper.getProductNumberFromPLE(partListEntryId.getOwnerAssemblyId(), null, project);
                                if (Utils.objectEquals(productNo, productNoFromPLE)) {
                                    if (katalogData.hasTUModelValidityExtension(saaBkNo, modelNumber)) {
                                        retailAssemblyNeedsModelExtensionSet.add(partListEntryId.getOwnerAssemblyId());
                                        extendModelValidityForPLE(partListEntryId, katalogData, modelNumber, modifiedPartListEntries,
                                                                  project);
                                    }
                                }
                            }
                        }

                        currentPos++;
                        fireProgress(currentPos, maxPos, true);
                    }
                    fireProgress(currentPos, maxPos, false);

                    boolean updateGrid = false;
                    if (!modifiedPartListEntries.isEmpty()) {
                        // Speichern mit technischem ChangeSet
                        if (iPartsRevisionChangeSet.saveDataObjectListWithChangeSet(getProject(), modifiedPartListEntries,
                                                                                    iPartsChangeSetSource.SAA_WORKBASKET)) {
                            updateGrid = true;
                        }
                        currentPos += modifiedPartListEntries.size();
                        maxPos += modifiedPartListEntries.size();
                        fireProgress(currentPos, maxPos, false);
                    }

                    // ChangeSets suchen, die angepasst werden müssen
                    Map<String, List<ExtendModelValidityChangeSetModificatorData>> relevantChangeSetIdsMap = new TreeMap<>();
                    for (DBDataObjectAttributes attributes : relevantAttributesList) {
                        String productNo = getProductNo(attributes);
                        String saaBkNo = getSaaBkNo(attributes);
                        String modelNo = getModelNumber(attributes);

                        // Die gefundenen WorkBasketAuthorOrderDatas inkl. der darin enthaltenen PartListEntryChangeSetEntries
                        // sind über productNo schon auf das Produkt des Datensatzes eingeschränkt
                        Set<AbstractSearchWorkBasketHelper.WorkBasketAuthorOrderData> authorOrderDataSet = searchHelper.getWorkBasketAuthorOrderData(productNo, saaBkNo,
                                                                                                                                                     modelNo);
                        if (authorOrderDataSet != null) {
                            for (AbstractSearchWorkBasketHelper.WorkBasketAuthorOrderData workBasketAuthorOrderData : authorOrderDataSet) {
                                List<ExtendModelValidityChangeSetModificatorData> changeSetModificatorDataList = relevantChangeSetIdsMap.computeIfAbsent(workBasketAuthorOrderData.getChangeSetId(),
                                                                                                                                                         key -> new ArrayList<>());
                                changeSetModificatorDataList.add(new ExtendModelValidityChangeSetModificatorData(modelNo,
                                                                                                                 saaBkNo,
                                                                                                                 workBasketAuthorOrderData.getPLEIdsWithoutModelValidityMatch()));
                            }
                        }
                    }

                    if (!relevantChangeSetIdsMap.isEmpty()) {
                        fireMessage("!!Erweitere Baumuster-Gültigkeiten von %1 ausgewählten Datensätzen in %2 relevanten nicht freigegebenen Autoren-Aufträgen...",
                                    String.valueOf(relevantAttributesList.size()),
                                    String.valueOf(relevantChangeSetIdsMap.size()));

                        List<ChangeSetModificator.ChangeSetModificationTask> changeSetModificationTasks = new DwList<>();
                        for (Map.Entry<String, List<ExtendModelValidityChangeSetModificatorData>> changeSetAttributesEntry : relevantChangeSetIdsMap.entrySet()) {
                            ChangeSetModificator.ChangeSetModificationTask changeSetModificationTask = new ChangeSetModificator.ChangeSetModificationTask(changeSetAttributesEntry.getKey()) {
                                @Override
                                public void modifyChangeSet(EtkProject projectForChangeSet, iPartsRevisionChangeSet authorOrderChangeSet,
                                                            GenericEtkDataObjectList dataObjectListForChangeSet) {
                                    // Nur die neuen bzw. veränderten Stücklisteneinträge ins ChangeSet laden
                                    authorOrderChangeSet.loadSerializedDataObjectsBySourceGUID("", PartListEntryId.TYPE);

                                    // Pro ChangeSet bei allen relevanten Stücklisteneinträgen aus diesem ChangeSet die
                                    // BM-Gültigkeiten erweitern pro relevantem selektierten Datensatz
                                    for (ExtendModelValidityChangeSetModificatorData changeSetModificatorData : changeSetAttributesEntry.getValue()) {
                                        Map<AssemblyId, Boolean> assemblyNeedsModelExtensionMap = new HashMap<>();
                                        for (PartListEntryId partListEntryId : changeSetModificatorData.getPLEIdsWithoutModelValidityMatch()) {
                                            Boolean assemblyNeedsModelExtension = assemblyNeedsModelExtensionMap.computeIfAbsent(partListEntryId.getOwnerAssemblyId(), assemblyId -> {
                                                // Wenn für eine Retail-Stückliste bereits die Baumuster-Gültigkeitserweiterung
                                                // durchgeführt wurde, dann muss dies auch in den nicht freigegebenen Autoren-Aufträgen
                                                // gemacht werden
                                                if (retailAssemblyNeedsModelExtensionSet.contains(assemblyId)) {
                                                    return true;
                                                }

                                                VarParam<Boolean> modelValidityFound = new VarParam<>(false);
                                                EtkDataObjectList.FoundAttributesCallback callback = new EtkDataObjectList.FoundAttributesCallback() {
                                                    @Override
                                                    public boolean foundAttributes(DBDataObjectAttributes attributes) {
                                                        // Abkürzung falls schon klar ist, dass eine BM-Gültigkeit gefunden wurde
                                                        if (modelValidityFound.getValue()) {
                                                            return false;
                                                        }

                                                        EtkDataArray modelValidityArray = null;
                                                        String modelValidityArrayId = attributes.getFieldValue(FIELD_K_MODEL_VALIDITY);
                                                        if (!modelValidityArrayId.isEmpty()) {
                                                            modelValidityArray = projectForChangeSet.getEtkDbs().getArrayById(TABLE_KATALOG, FIELD_K_MODEL_VALIDITY,
                                                                                                                              modelValidityArrayId);
                                                        }

                                                        // Bei leerer BM-Gültigkeit oder falls das Baumuster des Datensatzes
                                                        // explizit in der BM-Gültigkeit vorhanden ist, dann benötigt das
                                                        // Modul keine BM-Gültigkeitserweiterung
                                                        if ((modelValidityArray == null) || modelValidityArray.isEmpty()
                                                            || modelValidityArray.containsValue(changeSetModificatorData.getModelNumber())) {
                                                            modelValidityFound.setValue(true);
                                                        }

                                                        return false;
                                                    }
                                                };

                                                // Nur Stücklisteneinträge betrachten, die die SAA/BK aus dem aktuellen
                                                // Datensatz in der SAA-Gültigkeit enthalten haben
                                                EtkDisplayFields selectFields = new EtkDisplayFields();
                                                selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_LFDNR, false, false));
                                                selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_MODEL_VALIDITY, false, true));

                                                EtkDataPartListEntryList partListEntryList = new EtkDataPartListEntryList();
                                                partListEntryList.searchSortAndFillWithJoin(projectForChangeSet, null, selectFields,
                                                                                            new String[]{ TableAndFieldName.make(TABLE_KATALOG,
                                                                                                                                 FIELD_K_VARI),
                                                                                                          TableAndFieldName.make(TABLE_DWARRAY,
                                                                                                                                 FIELD_DWA_FELD),
                                                                                                          TableAndFieldName.make(TABLE_DWARRAY,
                                                                                                                                 FIELD_DWA_TOKEN) },
                                                                                            new String[]{ partListEntryId.getKVari(),
                                                                                                          TableAndFieldName.make(TABLE_KATALOG,
                                                                                                                                 FIELD_K_SA_VALIDITY),
                                                                                                          changeSetModificatorData.getSaaBkNumber() },
                                                                                            false, new String[]{ FIELD_K_LFDNR },
                                                                                            false, false, callback,
                                                                                            new EtkDataObjectList.JoinData(TABLE_DWARRAY,
                                                                                                                           new String[]{ FIELD_K_SA_VALIDITY },
                                                                                                                           new String[]{ FIELD_DWA_ARRAYID },
                                                                                                                           false, false));

                                                return !modelValidityFound.getValue();
                                            });

                                            if (assemblyNeedsModelExtension) {
                                                extendModelValidityForPLE(partListEntryId, null, changeSetModificatorData.getModelNumber(),
                                                                          dataObjectListForChangeSet, projectForChangeSet);
                                            }
                                        }
                                    }
                                }
                            };
                            changeSetModificationTasks.add(changeSetModificationTask);
                        }
                        ChangeSetModificator changeSetModificator = new ChangeSetModificator(iPartsPlugin.LOG_CHANNEL_CHANGE_SETS);
                        changeSetModificator.executeChangesInAllChangeSets(changeSetModificationTasks, false, TECHNICAL_USER_EXTEND_MODEL_VALIDITY);

                    } else {
                        fireMessage("!!Keine relevanten nicht freigegebenen Autoren-Aufträge für die Erweiterung der Baumuster-Gültigkeiten gefunden.",
                                    String.valueOf(relevantAttributesList.size()),
                                    String.valueOf(relevantChangeSetIdsMap.size()));
                    }
                    fireProgress(maxPos, maxPos, false);
                    fireMessage("!!Fertig");

                    if (updateGrid) {
                        Session.invokeThreadSafeInSession(() -> {
                            int[] selectedIndices = getTable().getSelectedRowIndices();
                            if (selectedIndices.length > 0) {
                                selectedIndexAfterUpdate = selectedIndices[0];
                            }
                            if (modelPacker.isPackingDisabled()) {
                                updateGridDirect();
                            } else {
                                startSearch();
                            }
                        });
                    }
                }

                private void extendModelValidityForPLE(PartListEntryId partListEntryId, AbstractSearchWorkBasketHelper.KatalogData katalogData,
                                                       String modelNumber, GenericEtkDataObjectList<EtkDataPartListEntry> modifiedPartListEntries,
                                                       EtkProject project) {
                    EtkDataPartListEntry partListEntry = EtkDataObjectFactory.createDataPartListEntry(project, partListEntryId);
                    if (partListEntry.existsInDB()) {
                        EtkDataArray modelValidityArray;
                        if (katalogData != null) {
                            modelValidityArray = katalogData.getModelValidityArray(project);
                        } else {
                            modelValidityArray = partListEntry.getFieldValueAsArray(FIELD_K_MODEL_VALIDITY);
                        }

                        // Wenn die Baumuster-Gültigkeit nicht leer ist und das neue Baumuster noch nicht enthält,
                        // dann das neue Baumuster einsortieren und den Stücklisteneintrag für das Speichern im
                        // technischem ChangeSet merken
                        if ((modelValidityArray != null) && !modelValidityArray.isEmpty() && !modelValidityArray.containsValue(modelNumber)) {
                            // BM-Gültigkeiten sortieren und neues Baumuster hinzufügen
                            Set<String> sortedModelNumbers = new TreeSet<>(modelValidityArray.getArrayAsStringList());
                            sortedModelNumbers.add(modelNumber);
                            modelValidityArray.clear(false);
                            modelValidityArray.add(sortedModelNumbers);

                            partListEntry.setFieldValueAsArray(FIELD_K_MODEL_VALIDITY, modelValidityArray, DBActionOrigin.FROM_EDIT);
                            modifiedPartListEntries.add(partListEntry, DBActionOrigin.FROM_EDIT);
                        }
                    }
                }

                private void fireMessage(String key, String... placeHolderTexts) {
                    messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate(key, placeHolderTexts));
                }

                private void fireProgress(int pos, int maxPos, boolean lazyUpdate) {
                    messageLogForm.getMessageLog().fireProgress(pos, maxPos, "", false, lazyUpdate);
                }
            });
        }
    }

    private void editInternalText() {
        endSearch();

        DBDataObjectAttributesList attributeList = getSelectedAttributesList();
        if ((attributeList != null) && !attributeList.isEmpty()) {
            DBDataObjectAttributes selectedAttributes = attributeList.get(0);
            String saaBkKemNo = searchHelper.getSaaBkNo(selectedAttributes);
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
                    List<GuiTableRow> selectedRows = getRowsWithFieldValueEquals(searchHelper.getFieldSaa(), wbIntTextId.getSaaBkKemValue());
                    updateGridDirect(editFields, attributes, selectedRows);
                }
            }
        }
    }

    private void showNutzdokAnnotation() {
        endSearch();

        DBDataObjectAttributesList attributeList = getSelectedAttributesList();
        if ((attributeList != null) && !attributeList.isEmpty()) {
            DBDataObjectAttributes selectedAttributes = attributeList.get(0);
            String saaBkKemNo = searchHelper.getSaaBkNo(selectedAttributes);
            if (StrUtils.isValid(saaBkKemNo)) {
                iPartsWorkBasketInternalTextId wbIntTextId = new iPartsWorkBasketInternalTextId(getWbType(), saaBkKemNo);
                iPartsWSWorkBasketItem.TYPE refType = iPartsWSWorkBasketItem.TYPE.SAA;
                if (wbIntTextId.isKEM()) {
                    refType = iPartsWSWorkBasketItem.TYPE.KEM;
                }
                iPartsShowDataObjectsDialog.showNutzDokAnnotations(getConnector(), MasterDataAbstractWorkBasketForm.this,
                                                                   saaBkKemNo, refType.name());
            }
        }
    }

    private Set<String> getSelectedSAAs() {
        return VisualizeSAAHelper.getSelectedSAAsFromAttributesList(getMultiSelection(), getFieldSaa());
    }

    /**
     * Grid direkt updaten durch erneute Berechnung der virtuellen Felder
     */
    private void updateGridDirect() {
        updateGridDirect(null, null);
    }

    private void updateGridDirect(EtkEditFields editFields, DBDataObjectAttributes attributes) {
        updateGridDirect(editFields, attributes, null);
    }

    /**
     * Grid direkt updaten durch erneute Berechnung der virtuellen Felder und direktem Austausch der Zellen
     *
     * @param editFields
     * @param attributes
     */
    private void updateGridDirect(EtkEditFields editFields, DBDataObjectAttributes attributes, List<GuiTableRow> selectedRows) {
        OnUpdateGridEvent updateEvent = new OnUpdateGridEvent() {

            @Override
            public DBDataObjectAttributes doCalculateVirtualFields(EtkProject project, DBDataObjectAttributes attributes) {
                String today = SQLStringConvert.calendarToPPDateTimeString(Calendar.getInstance());
                return searchHelper.calculateVirtualFields(attributes, attributesKatMap, assemblyProductMap, today);
            }

            @Override
            public TableCellControlWithTextRepresentation doCalcGuiElemForCell(EtkProject project, EtkDisplayField field, DBDataObjectAttributes attributes) {
                VarParam<String> resultValue = new VarParam<>("");
                VarParam<String> resultHintValue = new VarParam<>("");
                calcValueAndHint(field, attributes, resultValue, resultHintValue);
                return calcGuiElemForCell(field, resultValue.getValue(), resultHintValue.getValue());
            }
        };
        UpdateGridHelper helper = new UpdateGridHelper(getProject(), getTable(), getDisplayResultFields(), updateEvent);
        helper.updateGrid(editFields, attributes, virtualFieldsList, null, selectedRows);
    }

    /**
     * Goto-Retail Menu neu aufbauen und enablen
     *
     * @param event
     */
    @Override
    protected void onTableSelectionChanged(Event event) {
        super.onTableSelectionChanged(event);
        boolean isRetailEnabled = false;
        boolean isConstructionEnabled = false;
        boolean isConstructionWithEditEnabled = false;
        boolean isExtendModelValidityEnabled = false;
        boolean isShowNutzDokRemarkDataEnabled = false;
        boolean isShowNutzDokAnnotationDataEnabled = false;
        boolean isUnifiedEnabled = true;

        gotoRetailMenu.removeAllChildren();
        DBDataObjectAttributesList selectedAttribList = getSelectedAttributesList();
        if ((selectedAttribList != null) && !selectedAttribList.isEmpty()) {
            if (selectedAttribList.size() == 1) {
                if (getWbType() == iPartsWorkBasketTypes.CTT_SAA_WB) {
                    isConstructionEnabled = StrUtils.isValid(getModelNumber(selectedAttribList.get(0)));
                    isUnifiedEnabled = isConstructionEnabled;
                } else {
                    isConstructionEnabled = true;
                }
                isConstructionWithEditEnabled = getProject().isRevisionChangeSetActiveForEdit();
                DBDataObjectAttributes selectedAttrib = selectedAttribList.get(0);

                String arrayIds = getArrayId(selectedAttrib);
                isRetailEnabled = StrUtils.isValid(arrayIds);
                if (isRetailEnabled) {
                    List<String> idListe = getArrayIdList(arrayIds);
                    String saaBkNo = getSaaBkNo(selectedAttrib);
                    String masterProductNo = getProductNo(selectedAttrib);
                    String masterModelNo = getModelNumber(selectedAttrib);
                    isRetailEnabled = createAndAddSubMenuAssemblyExtra(idListe, saaBkNo, masterProductNo, masterModelNo);
                }

                isShowNutzDokRemarkDataEnabled = nutzDokRemarkHelper.prepareNutzDokSubMenus(showNutzdokRemarkDataMenu, getSaaBkNo(selectedAttrib));
                isShowNutzDokAnnotationDataEnabled = nutzDokRemarkHelper.isNutzDokAnnotationMenuEnabled(getSaaBkNo(selectedAttrib));
            } else if (getWbType() == iPartsWorkBasketTypes.CTT_SAA_WB) {
                int notEmptyModels = 0;
                for (DBDataObjectAttributes attrib : selectedAttribList) {
                    if (!getPackModelNumbers(attrib).isEmpty()) {
                        notEmptyModels++;
                    }
                }
                isUnifiedEnabled = notEmptyModels > 1;
            }

            if (extendModelValidityMenu != null) {
                for (DBDataObjectAttributes attributes : selectedAttribList) {
                    // Nur BM-Gültigkeitserweiterung ist relevant für extendModelValidityMenu
                    if (getSaaCase(attributes) == iPartsEDSSaaCase.EDS_CASE_MODEL_VALIDITY_EXPANSION) {
                        isExtendModelValidityEnabled = true;
                        break;
                    }
                }
            }
        }

        gotoConstructionMenu.setEnabled(isConstructionEnabled);
        gotoConstructionWithEditMenu.setEnabled(isConstructionWithEditEnabled && isRetailEnabled);
        gotoRetailMenu.setEnabled(isRetailEnabled);
        unifyMenu.setEnabled(isUnifiedEnabled);
        showNutzdokRemarkDataMenu.setEnabled(isShowNutzDokRemarkDataEnabled);

        if (extendModelValidityMenu != null) {
            extendModelValidityMenu.setEnabled(isExtendModelValidityEnabled);
        }
        intTextMenu.setEnabled((selectedAttribList != null) && (selectedAttribList.size() == 1));
        showNutzdokAnnotationMenu.setEnabled(isShowNutzDokAnnotationDataEnabled);
        visualizeSAAMenu.setEnabled(!getSelectedSAAs().isEmpty());
    }

    private void addSpecialSubMenuExtra(String text, AbstractJavaViewerForm parentForm, final Collection<AbstractSearchWorkBasketHelper.KatalogData> containerList,
                                        String saaBkNo, String masterModelNo, boolean showModelValidityExtension) {
        List<EtkDataPartListEntry> showList = new DwList<>();
        for (AbstractSearchWorkBasketHelper.KatalogData container : containerList) {
            EtkDataPartListEntry partListEntry = EtkDataObjectFactory.createDataPartListEntry(getProject(), container.getPartListEntryId());
            partListEntry.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            if (showModelValidityExtension) {
                String modelValidityExtension = "";
                if (container.hasTUModelValidityExtension(saaBkNo, masterModelNo)) {
                    modelValidityExtension = TranslationHandler.translate("!!BM-Gültigkeitserweiterung");
                }
                partListEntry.getAttributes().addField(FIELD_MODEL_VALIDITY_EXTENSION, modelValidityExtension, DBActionOrigin.FROM_DB);
            }
            showList.add(partListEntry);
        }

        List<String> displayTableFieldNames = new DwList<>();
        displayTableFieldNames.add(TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VARI));
        if (showModelValidityExtension) {
            displayTableFieldNames.add(TableAndFieldName.make(TABLE_KATALOG, FIELD_MODEL_VALIDITY_EXTENSION));
        }

        List<String> displayFieldText = new DwList<>();
        displayFieldText.add("!!TU im Retail");
        if (showModelValidityExtension) {
            displayFieldText.add("!!BM-Gültigkeitserweiterung");
        }

        GuiMenu menu = toolbarHelper.createMenuEntry("specialGotoMenu", text,
                                                     null, new EventListener(Event.MENU_ITEM_EVENT) {
                    @Override
                    public void fire(Event event) {
                        PartListEntryId selectedPartListEntryId = iPartsShowDataObjectsDialog.showGototRetailElements(getConnector(), parentForm,
                                                                                                                      showList, displayTableFieldNames,
                                                                                                                      displayFieldText, true);

                        if (selectedPartListEntryId != null) {
                            iPartsGotoHelper.gotoRetail(getConnector(), MasterDataAbstractWorkBasketForm.this, true, selectedPartListEntryId);
                        }
                    }
                }, getUITranslationHandler());
        gotoRetailMenu.addChild(menu);
    }

    private boolean createAndAddSubMenuAssemblyExtra(List<String> idListe, String saaBkNo, String masterProductNo, String masterModelNo) {
        Set<AssemblyId> masterAssemblyIdSet = new TreeSet<>();
        Map<AssemblyId, AbstractSearchWorkBasketHelper.KatalogData> assemblyMap = new LinkedHashMap<>();
        boolean isInRetail = false;

        searchHelper.fillAssemblyToKatalogDataMapAndAssemblyIdSet(idListe, masterProductNo, masterAssemblyIdSet, assemblyMap);
        if (!masterAssemblyIdSet.isEmpty()) {
            // zuerst die Menü-Einträge für das angezeigte Produkt
            masterAssemblyIdSet.forEach((masterAssemblyId) -> {
                AbstractSearchWorkBasketHelper.KatalogData catalog = assemblyMap.get(masterAssemblyId);
                if (catalog != null) {
                    PartListEntryId productPartListEntryId = catalog.getPartListEntryId();
                    if (productPartListEntryId != null) {
                        addAssemblySubMenu(masterAssemblyId, productPartListEntryId, catalog.hasTUModelValidityExtension(saaBkNo,
                                                                                                                         masterModelNo));
                        assemblyMap.remove(masterAssemblyId);
                    }
                }
            });
            if (!assemblyMap.isEmpty()) {
                // Separator
                GuiSeparator separator = new GuiSeparator();
                separator.setName("gotomenu_separator");
                gotoRetailMenu.addChild(separator);
            }
            isInRetail = true;
        }

        // restliche TU's ins SubMenu eintragen (hier keine Anzeige der BM-Gültigkeitserweiterung)
        if (!assemblyMap.isEmpty()) {
            int maxValue = iPartsMainImportHelper.MAX_ELEMS_FOR_SHOW;
            if (Constants.DEVELOPMENT && !Constants.DEVELOPMENT_QFTEST) {
                maxValue = 1;
            }
            if (assemblyMap.values().size() > maxValue) {
                addSpecialSubMenuExtra("!!Retail-TU Anzeige", this, assemblyMap.values(), saaBkNo, masterModelNo, false);
            } else {
                assemblyMap.forEach((assemblyId, container) -> {
                    addAssemblySubMenu(assemblyId, container.getPartListEntryId(), false);
                });
            }
            isInRetail = true;
        }

        return isInRetail;
    }


    /**
     * Lädt die dazugehörigen Module im Edit im Hintergrund
     *
     * @param attributes
     * @param withOwnDialog
     */
    protected void loadAssembliesFromAttributeIdListInEdit(DBDataObjectAttributes attributes, boolean withOwnDialog) {
        if (attributes != null) {
            Set<AssemblyId> masterAssemblyIdSet = new TreeSet<>();
            Map<AssemblyId, AbstractSearchWorkBasketHelper.KatalogData> assemblyMap = new LinkedHashMap<>();
            String arrayIds = getArrayId(attributes);
            if (StrUtils.isValid(arrayIds)) {
                List<String> idListe = getArrayIdList(arrayIds);
                String masterProductNo = getProductNo(attributes);
                searchHelper.fillAssemblyToKatalogDataMapAndAssemblyIdSet(idListe, masterProductNo, masterAssemblyIdSet,
                                                                          assemblyMap);

                boolean loadReadOnly = true;
                iPartsDataAuthorOrder currentAuthorOrder = iPartsDataAuthorOrderList.getAuthorOrderByActiveChangeSetForEdit(getProject());
                if (currentAuthorOrder != null) {
                    loadReadOnly = iPartsAuthorOrderStatus.isEndState(currentAuthorOrder.getStatus());
                }

                // Falls TUs zum Produkt vorhanden sind, sollen alle geöffnet werden
                // Sonst soll der erste TU mit anderen Produkt geöffnet werden
                // Wenn es keine TUs zum Produkt gibt sind in assemblyMap nur produktfremde TUs
                TreeSet<AssemblyId> assemblyIdsSet = new TreeSet<>();
                if (!masterAssemblyIdSet.isEmpty()) {
                    assemblyIdsSet.addAll(masterAssemblyIdSet);
                } else if (!assemblyMap.isEmpty()) {
                    assemblyIdsSet.addAll(assemblyMap.keySet());
                    AssemblyId firstAssemblyId = assemblyIdsSet.first();
                    assemblyIdsSet.clear();
                    assemblyIdsSet.add(firstAssemblyId);
                }

                if (!assemblyIdsSet.isEmpty()) {
                    Set<String> selectedModulList = new HashSet<>();
                    assemblyIdsSet.forEach(eachAssemblyId -> selectedModulList.add(eachAssemblyId.getKVari()));
                    String saaBkNo = getSaaBkNo(attributes);
                    String additionalTextForHeader = TranslationHandler.translate("!!Bezug zur SAA-Nummer: \"%1\"",
                                                                                  iPartsNumberHelper.formatPartNo(getProject(), saaBkNo));
                    iPartsLoadEditModuleHelper.doLoadModules(getConnector(), selectedModulList, additionalTextForHeader,
                                                             loadReadOnly, withOwnDialog, null);
                }
            }
        }
    }

    private void addAssemblySubMenu(final AssemblyId assemblyId, final PartListEntryId partListEntryId, boolean isModelExtension) {
        String text = assemblyId.getKVari();
        if (isModelExtension) {
            text += " " + TranslationHandler.translate("!!(BM-Gültigkeitserweiterung)");
        }
        GuiMenu menu = toolbarHelper.createMenuEntry(assemblyId.getKVari(),
                                                     text, null, new EventListener(Event.MENU_ITEM_EVENT,
                                                                                   EventListenerOptions.SYNCHRON_EVENT) {
                    @Override
                    public void fire(Event event) {
                        if (partListEntryId != null) {
                            iPartsGotoHelper.gotoRetail(getConnector(), MasterDataAbstractWorkBasketForm.this, true, partListEntryId);
                        } else {
                            iPartsGotoHelper.gotoRetail(getConnector(), MasterDataAbstractWorkBasketForm.this, true, assemblyId);
                        }
                    }
                }, getUITranslationHandler());
        Session.invokeThreadSafeInSession(() -> gotoRetailMenu.addChild(menu));
    }

    @Override
    protected void prepareGuiForSearch() {
        super.prepareGuiForSearch();
        EdSWorkBasketSearchFilterFactory filterFactory = new EdSWorkBasketSearchFilterFactory(getProject());
        setColumnFilterFactory(filterFactory);
        getTable().setColumnFilterFactory(filterFactory);
    }

    @Override
    protected boolean executeExplicitSearch() {
        if (changeSetSearchThread != null) {
            changeSetSearchThread.cancel();
            changeSetSearchThread = null;
        }

        assemblyProductMap.clear();
        attributesKatMap.clear();
        stateManager.clear();

        searchHelper.reset(assemblyProductMap, attributesKatMap, stateManager);

        // Map von Produktnummer auf Map von SAAs auf Map von Baumustern (inkl. leer für keine Baumuster-Gültigkeit) auf
        // Set von Autoren-Auftrags-Namen
        String productNumberSearchValue = getSearchValue(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_PRODUCT_NO, false);
        String saaSearchValue = getSearchValue(getSaaModelTable(), getFieldSaa(), false);
        String modelNumberSearchValue = getSearchValue(getSaaModelTable(), getFieldModelNo(), false);
        String manualSearchString = "manual search for product \"" + productNumberSearchValue + "\", SAA \"" + saaSearchValue
                                    + "\" and model \"" + modelNumberSearchValue + "\" (" + wbType.name() + ")";
        Logger.log(iPartsEditPlugin.LOG_CHANNEL_WORKBASKETS_CALC_AND_EXPORT, LogType.DEBUG, "Starting " + manualSearchString + "...");

        Set<String> modelNumbers = new HashSet<>();
        if (!calcWhereFieldsAndValuesFromSearchFields(searchHelper, modelNumbers)) {
            // keine eigene Meldung nötig; falls hier false rauskommt wurde der Fehler bereits angezeigt
            return true;
        }

        if ((searchSaaCase != null) && ((searchHelper.searchValuesAndFields.isEmpty()) && StrUtils.isEmpty(searchHelper.searchProductNo))) {
            MessageDialog.showWarning("!!Geschäftsfall-Suche nur mit einem weiteren Suchkriterium erlaubt.");
            return true;
        }

        changeSetSearchThread = Session.startChildThreadInSession(searchHelper.createChangeSetSearchThread(
            assemblyProductMap, productNumberSearchValue, saaSearchValue, modelNumberSearchValue, getSource()));


        searchHelper.addSourceToSearchValueAndFields(getSource().getOrigin());

        String[] sortFields = searchHelper.getSortFields();

        // für Reset-Sorting
        LinkedHashMap<String, Boolean> tableSortFields = new LinkedHashMap<>();
        for (String tableAndFieldName : sortFields) {
            tableSortFields.put(TableAndFieldName.getFieldName(tableAndFieldName), false);
        }
        setSortFields(tableSortFields);

        Set<String> usedModelNumbers = new HashSet<>();
        Map<String, EtkDataObject> attribJoinMap = new LinkedHashMap<>();

        modelPacker = new SaaModelPacker(ARRAYID_DELIMITER, MODEL_DELIMITER, getFieldModelNo());
        // für den eindeutigen Key
        modelPacker.presetKeyFieldNames(getFieldDocuRel(), getFieldSaaCase(), FIELD_DPM_PRODUCT_NO,
                                        searchHelper.getFieldSaa());
        // Geschäftsfall BM-Gültigkeitserweiterung nicht verdichten, für den Key stattdessen das ModelNo-Feld nehmen
        modelPacker.setExcludeValue(getFieldSaaCase(), iPartsEDSSaaCase.EDS_CASE_MODEL_VALIDITY_EXPANSION.getDbValue(), getFieldModelNo());
//        modelPacker.disablePacking(true);


        searchHelper.loadFromDB(modelNumbers, searchSaaCase, usedModelNumbers, attribJoinMap, null);
        Logger.log(iPartsEditPlugin.LOG_CHANNEL_WORKBASKETS_CALC_AND_EXPORT, LogType.DEBUG, "Search in database finished for "
                                                                                            + manualSearchString);

        if (!searchWasCanceled()) {
            addResults(true, usedModelNumbers, attribJoinMap);
        } else {
            attribJoinMap.clear();
            usedModelNumbers.clear();
            addResults(true, usedModelNumbers, attribJoinMap);
        }
        Logger.log(iPartsEditPlugin.LOG_CHANNEL_WORKBASKETS_CALC_AND_EXPORT, LogType.DEBUG, "Calculation finished with "
                                                                                            + getTable().getRowCount()
                                                                                            + " results for " + manualSearchString);
        return true;
    }

    public void addResults(boolean lastResults, Set<String> usedModelNumbers, Map<String, EtkDataObject> attribJoinMap) {
        // die manuellen Status laden
        stateManager.addModels(usedModelNumbers);

        FrameworkThread changeSetSearchThreadLocal = getChangesetSearchThread();
        if ((changeSetSearchThreadLocal != null) && !changeSetSearchThreadLocal.wasCanceled()) {
            if (lastResults) {
                // Auf den Such-Thread der ChangeSets warten
                changeSetSearchThreadLocal.waitFinished();
            } else if (changeSetSearchThreadLocal.isRunning()) {
                // Solange der Such-Thread der ChangeSets noch läuft, keine Ergebnisse anzeigen
                return;
            }
        }

        List<EtkDataObject> resultList = new ArrayList<>(attribJoinMap.values());
        attribJoinMap.clear(); // Bisherige Ergebnisse entfernen, da diese über resultList gleich angezeigt werden

        if (!resultList.isEmpty()) {
            Session session = Session.get();
            try {
                fillGrid(resultList);
            } catch (Exception e) {
                FrameworkThread searchThreadLocal = searchThread;
                if (searchThreadLocal != null) {
                    searchThreadLocal.cancel(false);
                }
                Logger.getLogger().throwRuntimeException(e);
            }
            if (lastResults) {
                // und Refresh
                session.invokeThreadSafe(() -> {
                    modelPacker.updateGrid(this, this.getTable());
                    setSelectionAfterUpdate();
                });
            }
        } else if (searchWasCanceled()) {
            // und Refresh
            Session.invokeThreadSafeInSession(() -> {
                modelPacker.updateGrid(this, this.getTable());
                setSelectionAfterUpdate();
            });
        }
    }

    protected void setSelectionAfterUpdate() {
        boolean isSelectionSet = false;
        if (selectedFieldNamesValuesAfterUpdate != null) {
            List<GuiTableRow> selectedTableRows = getRowsWithFieldValueEquals(selectedFieldNamesValuesAfterUpdate);
            isSelectionSet = setSelectionByTableRows(selectedTableRows);
            selectedFieldNamesValuesAfterUpdate = null;
        }
        if (!isSelectionSet) {
            if (selectedIndexAfterUpdate != -1) {
                getTable().setSelectedRowFuzzy(selectedIndexAfterUpdate);
            }
            selectedIndexAfterUpdate = -1;
        }
    }

    protected boolean calcWhereFieldsAndValuesFromSearchFields(AbstractSearchWorkBasketHelper searchHelper, Set<String> modelNumbers) {
        EtkProject project = getProject();
        searchHelper.searchValuesAndFields.clear();
        searchSaaCase = null;
        searchHelper.searchProductNo = null;

        String searchModelValue = null;
        String searchModelParamName = null;
        String searchSaaBkValue = null;
        String searchSaaBkParamName = null;
        // Vorbereitung der Suchfelder. Alle Suchfelder bekommen ein * angehängt, außer Produktnummer. SAAs werden unformatiert.
        for (int lfdNr = 0; lfdNr < getSearchFields().size(); lfdNr++) {
            EditControl editCtrl = getEditControlFeldByIndex(lfdNr);//   editControls.getControlByFeldIndex(lfdNr);
            if (editCtrl != null) {
                EditControlFactory ctrl = editCtrl.getEditControl();
                if (ctrl != null) {
                    String value = ctrl.getText().trim();
                    if (StrUtils.isValid(value)) {
                        // Länge von searchValue auf die maximale Feldlänge beschränken, um DB-Fehler zu vermeiden
                        value = project.getConfig().getDBDescription().cutValueIfLongerThanFieldLength(value,
                                                                                                       ctrl.getTableName(),
                                                                                                       ctrl.getFieldName());
                        if (ctrl.getFieldName().equals(getFieldSaaCase())) {
                            searchSaaCase = new HashSet<>();
                            searchSaaCase.add(iPartsEDSSaaCase.getFromDBValue(value));
                            continue;
                        }
                        if (ctrl.getFieldName().equals(searchHelper.getFieldSaa())) {
                            iPartsNumberHelper numberHelper = new iPartsNumberHelper();
                            value = numberHelper.unformatSaaBkForEdit(project, value);
                            searchSaaBkValue = value.trim();
                            searchSaaBkParamName = ctrl.getField().getDisplayText(project.getViewerLanguage(), project.getAvailLanguages());

                            // Unterschiedliche Retail-SAA-Nummer vorhanden für die Konstruktions-SAA-Nummer?
                            // TODO Einkommentieren sobald auch in EDS "Z0*" durch "Z *" ersetzt wurde
//                            String retailSAA = numberHelper.getDifferentRetailSAA(value);
//                            if (retailSAA != null) {
//                                value = retailSAA;
//                            }
                        } else if (ctrl.getFieldName().equals(searchHelper.getModelField())) {
                            searchModelValue = value.trim();
                            searchModelParamName = ctrl.getField().getDisplayText(project.getViewerLanguage(), project.getAvailLanguages());
                        }
                        if (!ctrl.getFieldName().equals(iPartsConst.FIELD_DPM_PRODUCT_NO)) {
                            EtkFieldType fieldType = project.getFieldDescription(ctrl.getTableName(), ctrl.getFieldName()).getType();
                            EtkDisplayField searchField = getSearchFields().getFeld(lfdNr);
                            if (fieldType.isWildCardType() && !searchField.isSearchExact()) {
                                //eigenes WildCardSetting mit * am Ende
                                WildCardSettings wildCardSettings = new WildCardSettings();
                                wildCardSettings.addWildCardEnd();
                                value = wildCardSettings.makeWildCard(value);
                            }
                            // bei Produkten kein Upper, bei den anderen Felder schon
                            value = value.toUpperCase();
                        } else {
                            if (StrUtils.stringContainsWildcards(value)) {
                                MessageDialog.showWarning("!!Produktsuche nur ohne Wildcards erlaubt.");
                                return false;
                            }
                            searchHelper.searchProductNo = value;
                        }
                        searchHelper.searchValuesAndFields.put(ctrl.getTableFieldName(), value);
                    }
                }
            }
        }

        if (StrUtils.isValid(searchHelper.searchProductNo)) {
            if (modelNumbers != null) {
                Set<String> modelList = searchHelper.getProduct(searchHelper.searchProductNo).getModelNumbers(project);
                modelNumbers.clear();
                modelNumbers.addAll(modelList);
            }
        } else {
            if (!checkSearchValueForEmptyProduct(searchModelValue, searchModelParamName)) {
                return false;
            }
            if (!checkSearchValueForEmptyProduct(searchSaaBkValue, searchSaaBkParamName)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Überprüft, ob der übergebene Suchwert gültig ist bei fehlendem explizitem Produkt.
     *
     * @param searchValue
     * @param searchParamName
     * @return
     */
    protected boolean checkSearchValueForEmptyProduct(String searchValue, String searchParamName) {
        return true;
    }

    protected String[] mergeArrays(String[] array1, String[] array2) {
        if (array1 != null) {
            return StrUtils.mergeArrays(array1, array2);
        }
        return array2;
    }

    /**
     * Suche neu starten mit loadFromDB()
     */
    @Override
    protected synchronized void internalStartSearch() {
        super.internalStartSearch();
    }

    @Override
    public boolean searchWasCanceled() {
        FrameworkThread searchThreadLocal = searchThread;
        if ((searchThreadLocal != null) && !searchThreadLocal.wasCanceled()) {
            return false;
        }
        return true;
    }

    @Override
    public void showProgress(int size, VarParam<Long> lastUpdateResultsCountTime) {
        if ((size > 0) && (((size % ROW_COUNT_FOR_ADD_RESULTS) == 0) // Modulo, damits weniger Calls gibt
                           || ((System.currentTimeMillis() - lastUpdateResultsCountTime.getValue()) > MAX_WAIT_TIME_MS_FOR_ADD_RESULTS))) { // Nicht zu lange warten
            Session.invokeThreadSafeInSession(() -> showHitCount(size));
            lastUpdateResultsCountTime.setValue(System.currentTimeMillis());
        }
    }

    @Override
    public String getVisualValueOfDbValue(String tableName, String fieldName, String dbValue) {
        return getVisObject().asText(tableName, fieldName, dbValue, getProject().getDBLanguage());
    }

    protected FrameworkThread getChangesetSearchThread() {
        return changeSetSearchThread;
    }

    /**
     * Bereits über die DB sortierte Ergebnisse aus searchSortAndFillWithJoin() weiter auswerten (docuRel) und anzeigen
     *
     * @param resultList
     */
    protected void fillGrid(List<EtkDataObject> resultList) {
        Session session = Session.get();
        getTable().switchOffEventListeners();
        try {
            String today = SQLStringConvert.calendarToPPDateTimeString(Calendar.getInstance());
            final int ROW_COUNT_FOR_THREAD_SAFE = getTable().getPageSplitNumberOfEntriesPerPage();
            for (int i = 0; i < Math.ceil((double)resultList.size() / ROW_COUNT_FOR_THREAD_SAFE); i++) {
                if (searchWasCanceled()) {
                    break;
                }

                int offset = i * ROW_COUNT_FOR_THREAD_SAFE;
                session.invokeThreadSafe(new Runnable() {
                    @Override
                    public void run() {
                        for (int resultIndex = offset; resultIndex < Math.min(offset + ROW_COUNT_FOR_THREAD_SAFE, resultList.size()); resultIndex++) {
                            if (searchWasCanceled()) {
                                break;
                            }

                            // eine Zeile vorbereiten
                            EtkDataObject dataProductModels = resultList.get(resultIndex);
                            calculateOneLine(dataProductModels.getAttributes(), attributesKatMap, assemblyProductMap, today);
                        }
                    }
                });
            }
        } finally {
            session.invokeThreadSafe(new Runnable() {
                @Override
                public void run() {
                    showNoResultsLabel(getTable().getRowCount() == 0, false);
                    showResultCount();
                }
            });
            getTable().switchOnEventListeners();
        }
    }

    /**
     * Eine Zeile für die Ausgabe vorbereiten
     *
     * @param attributes
     * @param attributesKatMap
     * @param assemblyProductMap
     * @param today
     */
    private void calculateOneLine(DBDataObjectAttributes attributes, Map<String, AbstractSearchWorkBasketHelper.KatalogData> attributesKatMap,
                                  Map<AssemblyId, String> assemblyProductMap, String today) {
        attributes = searchHelper.calculateVirtualFields(attributes, attributesKatMap, assemblyProductMap, today);
        boolean doAdd = attributes != null;  // zur Sicherheit
        if (doAdd) {
            if (searchSaaCase != null) {
                iPartsEDSSaaCase currentSaaCase = getSaaCase(attributes);
                if ((currentSaaCase == iPartsEDSSaaCase.EDS_CASE_MODEL_VALIDITY_EXPANSION) || (currentSaaCase == iPartsEDSSaaCase.EDS_CASE_SAA_VALIDITY_EXPANSION)) {
                    currentSaaCase = iPartsEDSSaaCase.EDS_CASE_VALIDITY_EXPANSION;
                }
                if (!searchSaaCase.contains(currentSaaCase)) {
                    doAdd = false;
                }
            }
        }
        if (doAdd) {
            // mit life-Update
            doAdd = !modelPacker.wasPacked(attributes, this, this.getTable());
        }
        if (doAdd) {
            processResultAttributes(attributes);
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

    //======= Getter für Tabellen und Feldnamen ====
    // virtuelle Felder
    protected abstract String getVirtualTableName();

    protected String getFieldDocuRel() {
        return searchHelper.getFieldDocuRel();
    }

    protected String getFieldDocuRelReason() {
        return searchHelper.getFieldDocuRelReason();
    }

    protected String getFieldPartEntryId() {
        return searchHelper.getFieldPartEntryId();
    }

    protected String getFieldSaaCase() {
        return searchHelper.getFieldSaaCase();
    }

    protected String getFieldModelStatus() {
        return searchHelper.getFieldModelStatus();
    }

    protected String getFieldSaaBkStatus() {
        return searchHelper.getFieldSaaBkStatus();
    }

    protected String getFieldManualStatus() {
        return searchHelper.getFieldManualStatus();
    }

    protected String getFieldAuthorOrder() {
        return searchHelper.getFieldAuthorOrder();
    }

    // echte Felder
    protected String getSaaModelTable() {
        return TABLE_DA_WB_SAA_CALCULATION;
    }

    protected String getFieldSaa() {
        return FIELD_WSC_SAA;
    }

    protected abstract boolean isSaaDisplayFieldName(String fieldName);

    protected String getFieldModelNo() {
        return FIELD_WSC_MODEL_NO;
    }

    protected String getPackFieldModelNo() {
        return FIELD_DM_MODEL_NO;
    }

    protected String getFieldModelReleaseFrom() {
        return FIELD_WSC_MIN_RELEASE_FROM;
    }

    protected String getFieldModelReleaseTo() {
        return FIELD_WSC_MAX_RELEASE_TO;
    }

    //====== viele Getter und Setter zur Vereinfachung =====

    protected static String getFieldSaaNutzdokRemarksAvailable() {
        return FIELD_SAA_NUTZDOK_REMARKS_AVAILABLE;
    }

    protected String getSaaBkNo(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(getFieldSaa());
    }

    protected String getArrayId(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(FIELD_DWA_ARRAYID);
    }

    protected void setArrayId(DBDataObjectAttributes attributes, String value) {
        attributes.addField(FIELD_DWA_ARRAYID, value, DBActionOrigin.FROM_DB);
    }

    protected String getArrayFeld(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(FIELD_DWA_FELD);
    }

    protected String getProductNo(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(FIELD_DPM_PRODUCT_NO);
    }

    protected String getKemToDate(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(getFieldModelReleaseTo());
    }

    protected String getKemFromDate(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(getFieldModelReleaseFrom());
    }

    protected String getModelNumber(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(getFieldModelNo());
    }

    protected List<String> getPackModelNumbers(DBDataObjectAttributes attributes) {
        return AbstractAttribValuePacker.getValueAsList(attributes.getFieldValue(getPackFieldModelNo()), MODEL_DELIMITER);
    }

    protected String getPartListEntryPos(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(getFieldPartEntryId());
    }

    private iPartsEDSSaaCase getSaaCase(DBDataObjectAttributes attributes) {
        DBDataObjectAttribute attrib = attributes.getField(getFieldSaaCase(), false);
        if (attrib != null) {
            return iPartsEDSSaaCase.getFromDBValue(attrib.getAsString());
        }
        return iPartsEDSSaaCase.EDS_CASE_NOT_SPECIFIED;
    }

    private PartListEntryId getPartListEntryId(DBDataObjectAttributes attributes) {
        String partListEntryPos = getPartListEntryPos(attributes);
        if (StrUtils.isValid(partListEntryPos)) {
            List<String> elems = StrUtils.toStringList(partListEntryPos, PARTLIST_ENTRY_DELIMITER, true, false);
            if (elems.size() >= 3) {
                return new PartListEntryId(elems.get(0), elems.get(1), elems.get(2));
            }
        }
        return null;
    }

    protected List<String> getArrayIdList(String arrayId) {
        return StrUtils.toStringList(arrayId, ARRAYID_DELIMITER, false, false);
    }

    /**
     * eigene FilterFactory für Spaltenfilter und FilterValues
     * Wird auch bei mehrfachen Filtern benötigt wegen der Pseudo-Tabelle (TABLE_WORK_BASKET_EDS)
     */
    public class EdSWorkBasketSearchFilterFactory extends SimpleMasterDataSearchFilterFactory {

        public EdSWorkBasketSearchFilterFactory(EtkProject project) {
            super(project);
        }

        @Override
        protected String getFilterValueFromVisObject(String value, EditControlFactory control) {
            // Hier können die Werte für einzelne Felder formatiert werden
            if (control.getTableFieldName().equals(TableAndFieldName.make(getSaaModelTable(), getFieldSaa()))) {
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
     * Datenklasse für die BM-Gültigkeitserweiterung in nicht freigegebenen Autoren-Aufträgen
     */
    protected class ExtendModelValidityChangeSetModificatorData {

        private String modelNumber;
        private String saaBkNumber;
        private List<PartListEntryId> pleIdsWithoutModelValidityMatch;

        public ExtendModelValidityChangeSetModificatorData(String modelNumber, String saaBkNumber, List<PartListEntryId> pleIdsWithoutModelValidityMatch) {
            this.modelNumber = modelNumber;
            this.saaBkNumber = saaBkNumber;
            this.pleIdsWithoutModelValidityMatch = pleIdsWithoutModelValidityMatch;
        }

        public String getModelNumber() {
            return modelNumber;
        }

        public String getSaaBkNumber() {
            return saaBkNumber;
        }

        public List<PartListEntryId> getPLEIdsWithoutModelValidityMatch() {
            return pleIdsWithoutModelValidityMatch;
        }
    }
}