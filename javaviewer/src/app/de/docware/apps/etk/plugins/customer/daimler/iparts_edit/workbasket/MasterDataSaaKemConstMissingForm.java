/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.forms.common.EditControlFactory;
import de.docware.apps.etk.base.forms.common.SimpleSelectSearchResultGrid;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetSource;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsNutzDokKEMId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsNutzDokSAAId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketInternalTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.config.iPartsNutzDokProcessingState;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataNutzDokKEM;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataNutzDokSAA;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditUserMultiChangeControls;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.SimpleMasterDataSearchFilterGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsShowDataObjectsDialog;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.VisualizeSAAHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.iPartsWSWorkBasketItem;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper.WorkBasketInternalTextCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.kemworkbasket.OnUpdateGridEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.kemworkbasket.UpdateGridHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.kemworkbasket.WorkBasketNutzDokRemarkHelper;
import de.docware.framework.modules.config.db.EtkFieldType;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiButton;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.menu.GuiMenu;
import de.docware.framework.modules.gui.controls.misc.DWFontStyle;
import de.docware.framework.modules.gui.controls.table.GuiTable;
import de.docware.framework.modules.gui.controls.table.HtmlTablePageSplitMode;
import de.docware.framework.modules.gui.controls.table.TableCellControlWithTextRepresentation;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerFireOnce;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.TableAndFieldName;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * Dialog zur Darstellung der KEMs und SAAs ohne konstruktive Daten (Arbeitsvorrat)
 */
public class MasterDataSaaKemConstMissingForm extends AbstractJavaViewerForm implements iPartsConst {

    private static final String UNIFY_SAA_MENU_NAME = "UnifySAA";
    private static final String UNIFY_KEM_MENU_NAME = "UnifyKEM";
    private static final String UNIFY_MENU_TEXT = "!!Vereinheitlichen...";
    private static final String SHOW_NUTZDOK_REMARK_DATA_NAME_SAA = "SaaShowNutzdokRemarkData";
    private static final String SHOW_NUTZDOK_REMARK_DATA_NAME_KEM = "KemShowNutzdokRemarkData";
    private static final String SHOW_NUTZDOK_REMARK_MENU_TEXT = "!!Nutzdok-Bemerkungstexte anzeigen";
    private static final String INTERNAL_TEXT_SAA_MENU_NAME = "IntTextSAA";
    private static final String INTERNAL_TEXT_KEM_MENU_NAME = "IntTextKEM";
    private static final String INTERNAL_TEXT_MENU_TEXT = "!!Internen Text/Wiedervorlage-Termin bearbeiten...";

    protected double splitPaneDividerRatio = 0.5d;
    private SaaDataSearchFilterGrid gridSAA;
    private KemDataSearchFilterGrid gridKEM;
    protected GuiMenu unifySaaMenu;
    protected GuiMenu unifyKemMenu;
    protected GuiMenu intTextSaaMenu;
    protected GuiMenu intTextKemMenu;
    protected GuiMenu showNutzdokKemAnnotationMenu;
    protected GuiMenu showNutzdokSaaAnnotationMenu;
    protected GuiMenu showSaaNutzdokRemarkDataMenu;
    protected GuiMenu visualizeSAAMenu;
    protected GuiMenu showKemNutzdokRemarkDataMenu;
    private volatile FrameworkThread kemSearchThread;

    /**
     * Erzeugt eine Instanz von MasterDataSaaKemConstMissingForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public MasterDataSaaKemConstMissingForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.kemSearchThread = null;
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        gridSAA = new SaaDataSearchFilterGrid(getConnector(), this, TABLE_DA_NUTZDOK_SAA);
        mainWindow.panel_saa_center.addChildBorderCenter(gridSAA.getGui());
        GuiLabel label = new GuiLabel("!!Fehlende SAA-NutzDok Elemente:");
        label.setFontStyle(DWFontStyle.BOLD);
        mainWindow.panel_saa_top.addChildBorderWest(label);
        gridSAA.setSearchFields(getSaaSearchFields());

        gridKEM = new KemDataSearchFilterGrid(getConnector(), this, TABLE_DA_NUTZDOK_KEM);
        mainWindow.panel_kem_center.addChildBorderCenter(gridKEM.getGui());
        label = new GuiLabel("!!Fehlende KEM-NutzDok Elemente:");
        label.setFontStyle(DWFontStyle.BOLD);
        mainWindow.panel_kem_top.addChildBorderWest(label);

        addContextMenus();

        // Initialwert setzen
        DBDataObjectAttributes searchAttributes = new DBDataObjectAttributes();
        searchAttributes.addField(FIELD_DNS_PROCESSING_STATE, gridSAA.getAccentuatedNutzDokProcessingState(), DBActionOrigin.FROM_DB);
        gridSAA.setSearchValues(searchAttributes, false);

        mainWindow.splitpane.addEventListener(new EventListenerFireOnce(Event.ON_RESIZE_EVENT, mainWindow.splitpane) {
            @Override
            public void fireOnce(Event event) {
                int height = event.getIntParameter(Event.EVENT_PARAMETER_NEWHEIGHT);
                mainWindow.splitpane.setDividerPosition((int)(height * splitPaneDividerRatio) + 31);
            }
        });
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public ModalResult showModal() {
        ModalResult modalResult = mainWindow.showModal();
        close();
        return modalResult;
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    /**
     * SearchFields noch ohne Konfiguration besetzen
     *
     * @return
     */
    private EtkDisplayFields getSaaSearchFields() {
        EtkDisplayFields searchFields = new EtkDisplayFields();
        // Suchfelder definieren
        EtkDisplayField searchField = new EtkDisplayField(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_PROCESSING_STATE, false, false);

        searchFields.addFeld(searchField);
        return searchFields;
    }

    private void cancelSearchThread() {
        if (kemSearchThread != null) {
            kemSearchThread.cancel();
            kemSearchThread = null;
        }
    }

    private void startKemSearchThread() {
        Session.invokeThreadSafeInSession(() -> {
            gridKEM.doClearGrid();
        });
        kemSearchThread = Session.startChildThreadInSession(thread -> gridKEM.doExecuteExplicitSearch());
    }

    private void waitForKemSearchThreadFinished() {
        FrameworkThread kemSearchThreadLocal = kemSearchThread;
        if ((kemSearchThreadLocal != null) && !kemSearchThreadLocal.wasCanceled()) {
            kemSearchThreadLocal.waitFinished();
        }
    }

    private void unifySaaStatus() {
        gridSAA.doUnifySelection();
    }

    private void unifyKemStatus() {
        gridKEM.doUnifySelection();
    }

//    private void editInternalTextSAA() {
//        gridSAA.endSearch();
//
//        DBDataObjectAttributes selectedAttributes = gridSAA.getSingleSelectAttribute();
//        if (selectedAttributes != null) {
//            String saaBkNo = gridSAA.getSaaNoFromSAA(selectedAttributes);
//            iPartsWorkBasketInternalTextId wbIntTextId = new iPartsWorkBasketInternalTextId(gridSAA.getWbType(), saaBkNo);
//            if (iPartsInternalTextForWorkbasketForm.showInternalTextFormForWorkBasket(getConnector(), this,
//                                                                                      wbIntTextId)) {
//                WorkBasketInternalTextCache.getInstance(getProject()).updateCache(getProject(), gridSAA.getWbType());
//                boolean hasInternalText = WorkBasketInternalTextCache.getInstance(getProject()).hasInternalText(gridSAA.getWbType(), saaBkNo);
//                if (gridSAA.isInternalTextSet(selectedAttributes) != hasInternalText) {
//                    EtkEditFields editFields = new EtkEditFields();
////                    EtkEditField eField = new EtkEditField(TABLE_WORK_BASKET, FIELD_KEM_NUTZDOK_INTERNAL_TEXT_AVAILABLE, false);
////                    editFields.addFeld(eField);
//                    DBDataObjectAttributes attributes = new DBDataObjectAttributes();
//                    attributes.addField(FIELD_KEM_NUTZDOK_INTERNAL_TEXT_AVAILABLE,
//                                        SQLStringConvert.booleanToPPString(hasInternalText), true, DBActionOrigin.FROM_DB);
//                    gridSAA.updateGrid(editFields, gridSAA.virtualFieldsList, attributes);
//
//
//                }
//                // toDo Grid updaten
//            }
//        }
//    }
//
//    private void editInternalTextKEM() {
//        cancelSearchThread();
//
//        DBDataObjectAttributes selectedAttributes = gridKEM.getSingleSelectAttribute();
//        if (selectedAttributes != null) {
//            String kemNo = gridKEM.getKemNo(selectedAttributes);
//            iPartsWorkBasketInternalTextId wbIntTextId = new iPartsWorkBasketInternalTextId(gridKEM.getWbType(), kemNo);
//            if (iPartsInternalTextForWorkbasketForm.showInternalTextFormForWorkBasket(getConnector(), this,
//                                                                                      wbIntTextId)) {
//                WorkBasketInternalTextCache.getInstance(getProject()).updateCache(getProject(), gridKEM.getWbType());
//                boolean hasInternalText = WorkBasketInternalTextCache.getInstance(getProject()).hasInternalText(gridKEM.getWbType(), kemNo);
//                if (gridKEM.isInternalTextSet(selectedAttributes) != hasInternalText) {
//                    EtkEditFields editFields = new EtkEditFields();
////                    EtkEditField eField = new EtkEditField(TABLE_WORK_BASKET, FIELD_KEM_NUTZDOK_INTERNAL_TEXT_AVAILABLE, false);
////                    editFields.addFeld(eField);
//                    DBDataObjectAttributes attributes = new DBDataObjectAttributes();
//                    attributes.addField(FIELD_KEM_NUTZDOK_INTERNAL_TEXT_AVAILABLE,
//                                        SQLStringConvert.booleanToPPString(hasInternalText), true, DBActionOrigin.FROM_DB);
//                    gridKEM.updateGrid(editFields, gridKEM.virtualFieldsList, attributes);
//
//
//                }
//                // toDo Grid updaten
//
//            }
//        }
//
//    }

    private void editInternalText(MissingDataSearchFilterGrid grid) {
        DBDataObjectAttributes selectedAttributes = grid.getSingleSelectAttribute();
        if (selectedAttributes != null) {
            String saaBkKemNo = grid.getSaaBkKemNo(selectedAttributes);
            iPartsWorkBasketInternalTextId wbIntTextId = new iPartsWorkBasketInternalTextId(grid.getWbType(), saaBkKemNo);
            DBDataObjectAttribute etsUnconfirmedAttribute = new DBDataObjectAttribute(grid.searchHelper.getEtsUnconfirmedAttribute(selectedAttributes));

            if (WorkBasketCollectedInternalTextForm.showInternalTextFormForWorkBasketEx(getConnector(), this,
                                                                                        wbIntTextId, etsUnconfirmedAttribute)) {
                EtkEditFields editFields = null;
                boolean hasInternalText = WorkBasketInternalTextCache.hasInternalText(getProject(), wbIntTextId.getWbType(), wbIntTextId.getSaaBkKemValue());
                DBDataObjectAttributes attributes = new DBDataObjectAttributes();
                if (grid.isInternalTextSet(selectedAttributes) != hasInternalText) {
                    attributes.addField(ConstMissingSearchHelper.FIELD_KEM_NUTZDOK_INTERNAL_TEXT_AVAILABLE,
                                        SQLStringConvert.booleanToPPString(hasInternalText), true, DBActionOrigin.FROM_DB);
                }
                String firstIntText = WorkBasketInternalTextCache.getFirstInternalText(getProject(), wbIntTextId.getWbType(), wbIntTextId.getSaaBkKemValue());
                if (!grid.getInternalText(selectedAttributes).equals(firstIntText)) {
                    attributes.addField(ConstMissingSearchHelper.FIELD_KEM_NUTZDOK_INTERNAL_TEXT,
                                        firstIntText, true, DBActionOrigin.FROM_DB);
                }
                String followUpDate = WorkBasketInternalTextCache.getFollowUpDate(getProject(), wbIntTextId.getWbType(), wbIntTextId.getSaaBkKemValue());
                if (!grid.getFollowUpDate(selectedAttributes).equals(followUpDate)) {
                    attributes.addField(ConstMissingSearchHelper.FIELD_KEM_FOLLOWUP_DATE,
                                        followUpDate, true, DBActionOrigin.FROM_DB);
                }
                if (!etsUnconfirmedAttribute.getAsString().equals(grid.searchHelper.getEtsUnconfirmedAttribute(selectedAttributes).getAsString())) {
                    attributes.addField(etsUnconfirmedAttribute, DBActionOrigin.FROM_DB);
                    editFields = new EtkEditFields();
                    editFields.addFeld(grid.searchHelper.getEditFieldForEtsExtension());
                }
                if (!attributes.isEmpty()) {
                    grid.updateGrid(editFields, grid.virtualFieldsList, attributes);
                }
            }
        }

    }

    private void showNutzdokAnnotation(MissingDataSearchFilterGrid grid, iPartsWSWorkBasketItem.TYPE type) {
        DBDataObjectAttributes selectedAttributes = grid.getSingleSelectAttribute();
        if (selectedAttributes != null) {
            String saaBkKemNo = grid.getSaaBkKemNo(selectedAttributes);
            if (StrUtils.isValid(saaBkKemNo)) {
                iPartsShowDataObjectsDialog.showNutzDokAnnotations(getConnector(), MasterDataSaaKemConstMissingForm.this,
                                                                   saaBkKemNo, type.name());
            }
        }
    }

    private Set<String> getSelectedSAAs() {
        return VisualizeSAAHelper.getSelectedSAAsFromAttributesList(gridSAA.getSelectedAttributesList(), gridSAA.getSaaFieldName());
    }

    /**
     * Goto-Menus vor 'Copy' hinzufügen
     */
    private void addContextMenus() {
        gridSAA.addContextMenus();
        gridKEM.addContextMenus();
    }

    private class SaaDataSearchFilterGrid extends MissingDataSearchFilterGrid {

        /**
         * Erzeugt eine Instanz von SimpleMasterDataSearchResultGrid.
         * Den $$internalCreateGui$$() Aufruf nicht ändern!
         *
         * @param dataConnector
         * @param parentForm
         * @param tableName
         */
        public SaaDataSearchFilterGrid(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName) {
            super(dataConnector, parentForm, tableName, true);
            SaaDataSearchFilterFactory filterFactory = new SaaDataSearchFilterFactory(getProject());
            setColumnFilterFactory(filterFactory);
            getTable().setColumnFilterFactory(filterFactory);
            virtualFieldsList.add(TableAndFieldName.make(ConstMissingSearchHelper.TABLE_WORK_BASKET, ConstMissingSearchHelper.FIELD_KEM_NUTZDOK_REMARKS_AVAILABLE));
            virtualFieldsList.add(TableAndFieldName.make(ConstMissingSearchHelper.TABLE_WORK_BASKET, ConstMissingSearchHelper.FIELD_KEM_NUTZDOK_INTERNAL_TEXT_AVAILABLE));
            virtualFieldsList.add(TableAndFieldName.make(ConstMissingSearchHelper.TABLE_WORK_BASKET, ConstMissingSearchHelper.FIELD_KEM_NUTZDOK_INTERNAL_TEXT));
            virtualFieldsList.add(TableAndFieldName.make(ConstMissingSearchHelper.TABLE_WORK_BASKET, ConstMissingSearchHelper.FIELD_KEM_FOLLOWUP_DATE));
            nutzDokRemarkHelper = new WorkBasketNutzDokRemarkHelper(this, getProject(), iPartsWSWorkBasketItem.TYPE.SAA);

            searchHelper = new SaaConstMissingSearchHelper(getProject(), this, nutzDokRemarkHelper);
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
                            WorkbasketAutoCalculationHelper.calculateSaaKemConstMissing(getProject(), tempDir, supplierMapping);
                            WorkbasketAutoCalculationHelper.finishExport(tempDir);
                        }
                    }
                    WorkbasketAutoCalculationHelper.downloadCalculatedData();
                }
            });
        }

        @Override
        protected void addFurtherContextMenus(List<AbstractGuiControl> list) {
            unifySaaMenu = toolbarHelper.createMenuEntry(UNIFY_SAA_MENU_NAME, UNIFY_MENU_TEXT, null, new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    unifySaaStatus();
                }
            }, getUITranslationHandler());
            list.add(unifySaaMenu);

            showSaaNutzdokRemarkDataMenu = toolbarHelper.createMenuEntry(SHOW_NUTZDOK_REMARK_DATA_NAME_SAA, SHOW_NUTZDOK_REMARK_MENU_TEXT, null, null, getUITranslationHandler());
            list.add(showSaaNutzdokRemarkDataMenu);

            intTextSaaMenu = toolbarHelper.createMenuEntry(INTERNAL_TEXT_SAA_MENU_NAME, INTERNAL_TEXT_MENU_TEXT, EditDefaultImages.edit_btn_internal_text.getImage(), new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    gridSAA.endSearch();
                    editInternalText(gridSAA);
                }
            }, getUITranslationHandler());
            list.add(intTextSaaMenu);

            showNutzdokSaaAnnotationMenu = toolbarHelper.createMenuEntry("showNutzdokAnnotationMenu", WorkBasketNutzDokRemarkHelper.NUTZDOK_ANNOTATION_MENU_TEXT, EditDefaultImages.edit_btn_internal_text.getImage(), new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
//                cancelSearchThread();
                    showNutzdokAnnotation(gridSAA, iPartsWSWorkBasketItem.TYPE.SAA);

                }
            }, getUITranslationHandler());
            list.add(showNutzdokSaaAnnotationMenu);

            list.add(toolbarHelper.createMenuSeparator(VisualizeSAAHelper.IPARTS_MENU_ITEM_VISUALIZE_SAA_3D + "Separator", getUITranslationHandler()));
            visualizeSAAMenu = VisualizeSAAHelper.createVisualizeSAAPopupMenuItem(toolbarHelper, getUITranslationHandler(),
                                                                                  new EventListener(Event.MENU_ITEM_EVENT) {
                                                                                      @Override
                                                                                      public void fire(Event event) {
                                                                                          VisualizeSAAHelper.visualizeSAAs(getSelectedSAAs());
                                                                                      }
                                                                                  });
            list.add(visualizeSAAMenu);
        }

        @Override
        public void endSearch() {
            cancelSearchThread();
            super.endSearch();
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
        protected void prepareGuiForSearch() {
            super.prepareGuiForSearch();
            SaaDataSearchFilterFactory filterFactory = new SaaDataSearchFilterFactory(getProject());
            setColumnFilterFactory(filterFactory);
            getTable().setColumnFilterFactory(filterFactory);
        }

        @Override
        protected void onTableSelectionChanged(Event event) {
            boolean isShowNutzDokAnnotationDataEnabled = false;
            if (showSaaNutzdokRemarkDataMenu != null) {
                boolean isShowNutzDokRemarkDataEnabled = false;
                DBDataObjectAttributes selectedAttrib = getSingleSelectAttribute();
                boolean isSingleSelected = selectedAttrib != null;
                if (isSingleSelected) {
                    isShowNutzDokRemarkDataEnabled = nutzDokRemarkHelper.prepareNutzDokSubMenus(showSaaNutzdokRemarkDataMenu, searchHelper.getRemarkRefId(selectedAttrib));
                    isShowNutzDokAnnotationDataEnabled = nutzDokRemarkHelper.isNutzDokAnnotationMenuEnabled(searchHelper.getRemarkRefId(selectedAttrib));
                }
                showSaaNutzdokRemarkDataMenu.setEnabled(isShowNutzDokRemarkDataEnabled);
            }
            if (visualizeSAAMenu != null) {
                visualizeSAAMenu.setEnabled(!getSelectedSAAs().isEmpty());
            }
            DBDataObjectAttributesList multiSelection = getMultiSelection();
            intTextSaaMenu.setEnabled((multiSelection != null) && (multiSelection.size() == 1));
            showNutzdokSaaAnnotationMenu.setEnabled(isShowNutzDokAnnotationDataEnabled);
        }

        @Override
        protected DBDataObjectAttributes calculateVirtualFields(DBDataObjectAttributes attributes) {
            return searchHelper.calculateVirtualFields(attributes);
        }

        @Override
        protected void doLoadFromDB(DBDataObjectAttributesList resultAttributesList) {
            cancelSearchThread();
            // aus den SearchFields der GUI die whereFields bestimmen
            calcWhereFieldsAndValuesFromSearchFields(searchHelper);

            // parallele Suche mit Thread
            startKemSearchThread();
            loadFromDB(resultAttributesList);
            if (!searchWasCanceled()) {
                waitForKemSearchThreadFinished();
            }

        }


        private void calcWhereFieldsAndValuesFromSearchFields(ConstMissingSearchHelper searchHelper) {
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
                            if (ctrl.getFieldName().equals(getSaaFieldName())) {
                                iPartsNumberHelper numberHelper = new iPartsNumberHelper();
                                value = numberHelper.unformatSaaBkForEdit(getProject(), value);
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

            // TEST
//            whereTableAndFields = mergeArrays(whereTableAndFields, new String[]{ TableAndFieldName.make(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_SAA) });
//            whereValues = mergeArrays(whereValues, new String[]{ "xZ50405301" });
            // Test Ende
        }

        @Override
        protected List<EtkDataObject> getSelectedDataObjects() {
            DBDataObjectAttributesList attributeList = getSelectedAttributesList();
            if (attributeList != null) {

                List<EtkDataObject> dataNutzDokSAAList = new DwList<>(attributeList.size());
                for (DBDataObjectAttributes attrib : attributeList) {
                    iPartsNutzDokSAAId id = new iPartsNutzDokSAAId(getSaaNoFromSAA(attrib));
                    iPartsDataNutzDokSAA dataNutzDokSAA = new iPartsDataNutzDokSAA(getProject(), id);
                    dataNutzDokSAA.assignAttributes(getProject(), attrib, true, DBActionOrigin.FROM_DB);
                    dataNutzDokSAA.removeForeignTablesAttributes();
                    dataNutzDokSAAList.add(dataNutzDokSAA);
                }
                return dataNutzDokSAAList;
            }
            return null;
        }

        protected DBDataObjectAttributes callUnifyControl(EtkEditFields editFields, List<EtkDataObject> dataNutzDokList) {
            // Anzeige des Unify-Dialogs
            return EditUserMultiChangeControls.showEditUserMultiChangeControlsForMissingSaa(getConnector(), editFields,
                                                                                            dataNutzDokList);
        }

        private String getSaaNoFromSAA(DBDataObjectAttributes attributes) {
            return attributes.getFieldValue(FIELD_DNS_SAA);
        }

        protected String getSaaBkKemNo(DBDataObjectAttributes attributes) {
            return getSaaNoFromSAA(attributes);
        }

        @Override
        protected String getUnifyFieldName() {
            return FIELD_DNS_PROCESSING_STATE;
        }

        @Override
        protected String getSaaFieldName() {
            return FIELD_DNS_SAA;
        }

        /**
         * eigene FilterFactory für Spaltenfilter und FilterValues
         * Wird auch bei mehrfachen Filtern benötigt
         */
        public class SaaDataSearchFilterFactory extends SimpleMasterDataSearchFilterFactory {

            public SaaDataSearchFilterFactory(EtkProject project) {
                super(project);
            }

            @Override
            protected String getFilterValueFromVisObject(String value, EditControlFactory control) {
                // Hier können die Werte für einzelne Felder formatiert werden
                if (control.getTableFieldName().equals(TableAndFieldName.make(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_SAA))) {
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


    private class KemDataSearchFilterGrid extends MissingDataSearchFilterGrid {

        /**
         * Erzeugt eine Instanz von SimpleMasterDataSearchResultGrid.
         * Den $$internalCreateGui$$() Aufruf nicht ändern!
         *
         * @param dataConnector
         * @param parentForm
         * @param tableName
         */
        public KemDataSearchFilterGrid(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName) {
            super(dataConnector, parentForm, tableName, false);
            KemDataSearchFilterFactory filterFactory = new KemDataSearchFilterFactory(getProject());
            setColumnFilterFactory(filterFactory);
            getTable().setColumnFilterFactory(filterFactory);
            nutzDokRemarkHelper = new WorkBasketNutzDokRemarkHelper(this, getProject(), iPartsWSWorkBasketItem.TYPE.KEM);
            virtualFieldsList.add(TableAndFieldName.make(ConstMissingSearchHelper.TABLE_WORK_BASKET, ConstMissingSearchHelper.FIELD_KEM_NUTZDOK_REMARKS_AVAILABLE));
            virtualFieldsList.add(TableAndFieldName.make(ConstMissingSearchHelper.TABLE_WORK_BASKET, ConstMissingSearchHelper.FIELD_KEM_NUTZDOK_INTERNAL_TEXT_AVAILABLE));
            virtualFieldsList.add(TableAndFieldName.make(ConstMissingSearchHelper.TABLE_WORK_BASKET, ConstMissingSearchHelper.FIELD_KEM_NUTZDOK_INTERNAL_TEXT));
            virtualFieldsList.add(TableAndFieldName.make(ConstMissingSearchHelper.TABLE_WORK_BASKET, ConstMissingSearchHelper.FIELD_KEM_FOLLOWUP_DATE));
            searchHelper = new KemConstMissingSearchHelper(getProject(), this, nutzDokRemarkHelper);
            setDisplayResultFields(searchHelper.buildDisplayFields());
        }

        @Override
        protected void addFurtherContextMenus(List<AbstractGuiControl> list) {
            unifyKemMenu = toolbarHelper.createMenuEntry(UNIFY_KEM_MENU_NAME, UNIFY_MENU_TEXT, null, new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    unifyKemStatus();
                }
            }, getUITranslationHandler());
            list.add(unifyKemMenu);

            showKemNutzdokRemarkDataMenu = toolbarHelper.createMenuEntry(SHOW_NUTZDOK_REMARK_DATA_NAME_KEM, SHOW_NUTZDOK_REMARK_MENU_TEXT, null, null, getUITranslationHandler());
            list.add(showKemNutzdokRemarkDataMenu);

            intTextKemMenu = toolbarHelper.createMenuEntry(INTERNAL_TEXT_KEM_MENU_NAME, INTERNAL_TEXT_MENU_TEXT, EditDefaultImages.edit_btn_internal_text.getImage(), new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    cancelSearchThread();
                    editInternalText(gridKEM);
                }
            }, getUITranslationHandler());
            list.add(intTextKemMenu);

            showNutzdokKemAnnotationMenu = toolbarHelper.createMenuEntry("showNutzdokAnnotationMenu", WorkBasketNutzDokRemarkHelper.NUTZDOK_ANNOTATION_MENU_TEXT, EditDefaultImages.edit_btn_internal_text.getImage(), new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    cancelSearchThread();
                    showNutzdokAnnotation(gridKEM, iPartsWSWorkBasketItem.TYPE.KEM);
                }
            }, getUITranslationHandler());
            list.add(showNutzdokKemAnnotationMenu);

        }

        @Override
        protected void onTableSelectionChanged(Event event) {
            if (showKemNutzdokRemarkDataMenu != null) {
                boolean isShowNutzDokRemarkDataEnabled = false;
                boolean isShowNutzDokAnnotationDataEnabled = false;
                DBDataObjectAttributes selectedAttrib = getSingleSelectAttribute();
                boolean isSingleSelected = selectedAttrib != null;
                if (isSingleSelected) {
                    isShowNutzDokRemarkDataEnabled = nutzDokRemarkHelper.prepareNutzDokSubMenus(showKemNutzdokRemarkDataMenu, searchHelper.getRemarkRefId(selectedAttrib));
                    isShowNutzDokAnnotationDataEnabled = nutzDokRemarkHelper.isNutzDokAnnotationMenuEnabled(searchHelper.getRemarkRefId(selectedAttrib));
                }
                showKemNutzdokRemarkDataMenu.setEnabled(isShowNutzDokRemarkDataEnabled);
                DBDataObjectAttributesList multiSelection = getMultiSelection();
                intTextKemMenu.setEnabled((multiSelection != null) && (multiSelection.size() == 1));
                showNutzdokKemAnnotationMenu.setEnabled(isShowNutzDokAnnotationDataEnabled);
            }
        }

        @Override
        public boolean searchWasCanceled() {
            return gridSAA.searchWasCanceled();
        }

        @Override
        protected void doLoadFromDB(DBDataObjectAttributesList attributesList) {
        }

        public boolean doExecuteExplicitSearch() {
            KemDataSearchFilterFactory filterFactory = new KemDataSearchFilterFactory(getProject());
            setColumnFilterFactory(filterFactory);
            getTable().setColumnFilterFactory(filterFactory);

            // SearchFields aus dem SAA Grid übernehmen und auf die KEM Felder anpassen
            searchHelper.copyAndModifyWhereFieldsAndValues(gridSAA.searchHelper);

            DBDataObjectAttributesList attributesList = new DBDataObjectAttributesList();
            loadFromDB(attributesList);

            if (J2EEHandler.isJ2EE()) {
                Session.invokeThreadSafeInSession(() -> doFillGrid(attributesList));
            } else {
                doFillGrid(attributesList);
            }

            return true;
        }

        @Override
        protected DBDataObjectAttributes calculateVirtualFields(DBDataObjectAttributes attributes) {
            return searchHelper.calculateVirtualFields(attributes);
        }


        @Override
        protected List<EtkDataObject> getSelectedDataObjects() {
            DBDataObjectAttributesList attributeList = getSelectedAttributesList();
            if (attributeList != null) {

                List<EtkDataObject> dataNutzDokKEMList = new DwList<>(attributeList.size());
                for (DBDataObjectAttributes attrib : attributeList) {
                    iPartsNutzDokKEMId id = new iPartsNutzDokKEMId(getKemNo(attrib));
                    iPartsDataNutzDokKEM dataNutzDokKEM = new iPartsDataNutzDokKEM(getProject(), id);
                    dataNutzDokKEM.assignAttributes(getProject(), attrib, true, DBActionOrigin.FROM_DB);
                    dataNutzDokKEM.removeForeignTablesAttributes();
                    dataNutzDokKEMList.add(dataNutzDokKEM);
                }
                return dataNutzDokKEMList;
            }
            return null;
        }

        protected DBDataObjectAttributes callUnifyControl(EtkEditFields editFields, List<EtkDataObject> dataNutzDokList) {
            // Anzeige des Unify-Dialogs
            return EditUserMultiChangeControls.showEditUserMultiChangeControlsForMissingKem(getConnector(), editFields,
                                                                                            dataNutzDokList);
        }


        private String getKemNo(DBDataObjectAttributes attributes) {
            return attributes.getFieldValue(FIELD_DNK_KEM);
        }

        protected String getSaaBkKemNo(DBDataObjectAttributes attributes) {
            return getKemNo(attributes);
        }

        @Override
        protected String getSaaFieldName() {
            return "";
        }

        @Override
        protected String getUnifyFieldName() {
            return FIELD_DNK_PROCESSING_STATE;
        }

        /**
         * eigene FilterFactory für Spaltenfilter und FilterValues
         * Wird auch bei mehrfachen Filtern benötigt
         */
        public class KemDataSearchFilterFactory extends SimpleMasterDataSearchFilterFactory {

            public KemDataSearchFilterFactory(EtkProject project) {
                super(project);
            }

            @Override
            protected String getFilterValueFromVisObject(String value, EditControlFactory control) {
                // Hier können die Werte für einzelne Felder formatiert werden
                // im Moment nichts zu tun
//                if (control.getTableFieldName().equals(TableAndFieldName.make(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_SAA))) {
//                    iPartsNumberHelper numberHelper = new iPartsNumberHelper();
//                    return numberHelper.unformatSaaBkForEdit(getProject(), value);
//                }
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

    private abstract class MissingDataSearchFilterGrid extends SimpleMasterDataSearchFilterGrid implements WorkbasketSearchCallback {

        protected List<String> virtualFieldsList;
        protected WorkBasketNutzDokRemarkHelper nutzDokRemarkHelper;
        protected ConstMissingSearchHelper searchHelper;
//        protected iPartsWorkBasketTypes wbType;

        /**
         * Erzeugt eine Instanz von SimpleMasterDataSearchResultGrid.
         * Den $$internalCreateGui$$() Aufruf nicht ändern!
         *
         * @param dataConnector
         * @param parentForm
         * @param tableName
         */
        public MissingDataSearchFilterGrid(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName,
                                           boolean withSearchFields) {
            super(dataConnector, parentForm, tableName, null);
            setEditFields(null);
            setEditAllowed(false);
            setModifyAllowed(false);
            setNewAllowed(false);
            setDeleteAllowed(false);
            showToolbar(false);
            showSearchFields(true);
            setMaxResults(-1);
            showSearchFields(withSearchFields);
            if (!withSearchFields) {
                setSearchFields(null);
            }
            virtualFieldsList = new DwList<>();
        }

        public GuiTable getGuiTable() {
            return getTable();
        }

        public iPartsWorkBasketTypes getWbType() {
            return searchHelper.getWbType();
        }

        public void addContextMenus() {
            List<AbstractGuiControl> list = new DwList<>();
            addFurtherContextMenus(list);

            if (!list.isEmpty()) {
                list.addAll(getTable().getContextMenu().getChildren());
                getTable().getContextMenu().removeAllChildren();
                for (AbstractGuiControl guiCtrl : list) {
                    getTable().getContextMenu().addChild(guiCtrl);
                }
            }
        }

        @Override
        public void setMaxResults(int maxResults) {
            super.setMaxResults(maxResults);
            if ((maxResults == -1) && J2EEHandler.isJ2EE()) {
                getTable().setPageSplitNumberOfEntriesPerPage(iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE);
            }
        }

        protected abstract void addFurtherContextMenus(List<AbstractGuiControl> list);

        public void doClearGrid() {
            clearGrid();
        }

        public void doUnifySelection() {
            endSearch();

            List<EtkDataObject> dataNutzDokList = getSelectedDataObjects();
            if (dataNutzDokList != null) {
                EtkEditFields editFields = new EtkEditFields();
                EtkEditField eField = new EtkEditField(searchTable, getUnifyFieldName(), false);
                editFields.addFeld(eField);
                DBDataObjectAttributes attributes = callUnifyControl(editFields, dataNutzDokList);
                if (attributes != null) {
                    for (EtkEditField editField : editFields.getFields()) {
                        String fieldName = editField.getKey().getFieldName();
                        String fieldValue = attributes.getFieldValue(fieldName);
                        for (EtkDataObject dataNutzDok : dataNutzDokList) {
                            dataNutzDok.setFieldValue(fieldName, fieldValue, DBActionOrigin.FROM_EDIT);
                        }
                    }
                    GenericEtkDataObjectList genericList = new GenericEtkDataObjectList();
                    genericList.addAll(dataNutzDokList, DBActionOrigin.FROM_EDIT);

                    // techn. ChangeSet speichern und selektierte Grid-Zeilen updaten
                    if (iPartsRevisionChangeSet.saveDataObjectListWithChangeSet(getProject(), genericList, iPartsChangeSetSource.WORK_BASKET_MISSING)) {
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
         * Joins zusammenbauen und via searchSortAndFillWithJoin() suchen
         */
        protected void loadFromDB(DBDataObjectAttributesList resultAttributesList) {
            String[] sortFields = searchHelper.getSortFields();

            // für Reset-Sorting
            LinkedHashMap<String, Boolean> tableSortFields = new LinkedHashMap<>();
            for (String tableAndFieldName : sortFields) {
                tableSortFields.put(TableAndFieldName.getFieldName(tableAndFieldName), false);
            }
            setSortFields(tableSortFields);

            searchHelper.loadFromDB(resultAttributesList);
        }

        protected abstract List<EtkDataObject> getSelectedDataObjects();

        protected DBDataObjectAttributes getSingleSelectAttribute() {
            DBDataObjectAttributesList selectedAttribList = getSelectedAttributesList();
            if ((selectedAttribList != null) && !selectedAttribList.isEmpty()) {
                if (selectedAttribList.size() == 1) {
                    return selectedAttribList.get(0);
                }
            }
            return null;
        }

        protected abstract DBDataObjectAttributes callUnifyControl(EtkEditFields editFields, List<EtkDataObject> dataNutzDokList);

        protected void updateGrid(EtkEditFields editFields, List<String> virtualFieldNamesList, DBDataObjectAttributes attributes) {
            UpdateGridHelper helper = new UpdateGridHelper(getProject(), getTable(), getDisplayResultFields(), createUpdateEvent());
            helper.updateGrid(editFields, attributes, virtualFieldNamesList, null);
        }

        protected OnUpdateGridEvent createUpdateEvent() {
            return new OnUpdateGridEvent() {

                @Override
                public DBDataObjectAttributes doCalculateVirtualFields(EtkProject project, DBDataObjectAttributes attributes) {
                    return calculateVirtualFields(attributes);
                }

                @Override
                public TableCellControlWithTextRepresentation doCalcGuiElemForCell(EtkProject project, EtkDisplayField field, DBDataObjectAttributes attributes) {
                    VarParam<String> resultValue = new VarParam<>("");
                    VarParam<String> resultHintValue = new VarParam<>("");
                    calcValueAndHint(field, attributes, resultValue, resultHintValue);
                    return calcGuiElemForCell(field, resultValue.getValue(), resultHintValue.getValue());
                }
            };
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

        public void showProgress(int progress) {
            if ((progress % 50) == 0) { // Modulo, damits weniger Calls gibt
                Session.invokeThreadSafeInSession(() -> showHitCount(progress));
            }
        }

        protected DBDataObjectAttributes calculateVirtualFields(DBDataObjectAttributes attributes) {
            return attributes;
        }

        protected void calcValueAndHint(EtkDisplayField field, DBDataObjectAttributes attributes,
                                        VarParam<String> resultValue, VarParam<String> resultHintValue) {
            String value = "";
            String hintValue = "";
            if (field.isVisible()) {
                String tableName = field.getKey().getTableName();
                String fieldName = field.getKey().getFieldName();
                if (fieldName.equals(getSaaFieldName())) {
                    value = iPartsNumberHelper.formatPartNo(getProject(), attributes.getFieldValue(fieldName));
                } else if (fieldName.equals(ConstMissingSearchHelper.FIELD_KEM_FOLLOWUP_DATE)) {
                    value = getVisualValueOfFieldValue(TABLE_DA_INTERNAL_TEXT, FIELD_DIT_CHANGE_DATE, attributes.getField(fieldName), field.isMultiLanguage());
                } else {
                    value = getVisualValueOfFieldValue(tableName, fieldName, attributes.getField(fieldName), field.isMultiLanguage());
                }

            }
            resultValue.setValue(value);
            resultHintValue.setValue(hintValue);
        }

        protected TableCellControlWithTextRepresentation calcGuiElemForCell(EtkDisplayField field, String value, String hintValue) {
            String fieldName = field.getKey().getFieldName();

            GuiLabel label = new GuiLabel(value);
//            if (fieldName.equals(getFieldDocuRel()) || fieldName.equals(getFieldManualStatus()) || fieldName.equals(getFieldSaaCase())) {
//                GuiPanel panel = new GuiPanel(new LayoutGridBag(false));
//                panel.setBackgroundColor(Colors.clTransparent.getColor());
//                label.setConstraints(new ConstraintsGridBag(0, 0, 1, 1, 0, 1, LayoutGridBag.ANCHOR_CENTER, LayoutGridBag.FILL_HORIZONTAL, 0, 2, 0, 0));
//                panel.addChild(label);
//                if (StrUtils.isValid(hintValue)) {
//                    panel.setTooltip(hintValue);
//                    label.setTooltip(hintValue);
//                }
//                return panel;
//            }
            return new TableCellControlWithTextRepresentation(label, () -> label.getTextRepresentation());
        }

        @Override
        protected boolean executeExplicitSearch() {
            DBDataObjectAttributesList resultAttributesList = new DBDataObjectAttributesList();
            doLoadFromDB(resultAttributesList);

            if (J2EEHandler.isJ2EE()) {
                Session.invokeThreadSafeInSession(() -> doFillGrid(resultAttributesList));
            } else {
                doFillGrid(resultAttributesList);
            }

            return true;
        }

        protected abstract void doLoadFromDB(DBDataObjectAttributesList attributesList);

        public boolean searchWasCanceled() {
            FrameworkThread searchThreadLocal = searchThread;
            if ((searchThreadLocal != null) && !searchThreadLocal.wasCanceled()) {
                return false;
            }
            return true;
        }

        protected void doFillGrid(DBDataObjectAttributesList attributesList) {
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
        protected void calculateOneLine(DBDataObjectAttributes attributes) {
            // hier, falls nötig, virtuelle Felder berechnen

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

        protected String[] mergeArrays(String[] array1, String[] array2) {
            if (array1 != null) {
                return StrUtils.mergeArrays(array1, array2);
            }
            return array2;
        }

        protected abstract String getSaaFieldName();

        protected abstract String getUnifyFieldName();

        protected abstract String getSaaBkKemNo(DBDataObjectAttributes attributes);

        protected boolean isInternalTextSet(DBDataObjectAttributes attributes) {
            return attributes.getField(ConstMissingSearchHelper.FIELD_KEM_NUTZDOK_INTERNAL_TEXT_AVAILABLE).getAsBoolean();
        }

        protected String getInternalText(DBDataObjectAttributes attributes) {
            return attributes.getField(ConstMissingSearchHelper.FIELD_KEM_NUTZDOK_INTERNAL_TEXT).getAsString();
        }

        protected String getFollowUpDate(DBDataObjectAttributes attributes) {
            return attributes.getField(ConstMissingSearchHelper.FIELD_KEM_FOLLOWUP_DATE).getAsString();
        }

        protected String getAccentuatedNutzDokProcessingState() {
            return iPartsNutzDokProcessingState.NEW.getDBValue();
        }
    }


    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitpane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpane_saa;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_saa_top;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_saa_center;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_saa_bottom;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpane_kem;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_kem_top;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_kem_center;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_kem_bottom;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
            title = new de.docware.framework.modules.gui.controls.GuiTitle();
            title.setName("title");
            title.__internal_setGenerationDpi(96);
            title.registerTranslationHandler(translationHandler);
            title.setScaleForResolution(true);
            title.setMinimumWidth(10);
            title.setMinimumHeight(50);
            title.setTitle("...");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleConstraints.setPosition("north");
            title.setConstraints(titleConstraints);
            this.addChild(title);
            panelMain = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelMain.setName("panelMain");
            panelMain.__internal_setGenerationDpi(96);
            panelMain.registerTranslationHandler(translationHandler);
            panelMain.setScaleForResolution(true);
            panelMain.setMinimumWidth(10);
            panelMain.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMain.setLayout(panelMainLayout);
            splitpane = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitpane.setName("splitpane");
            splitpane.__internal_setGenerationDpi(96);
            splitpane.registerTranslationHandler(translationHandler);
            splitpane.setScaleForResolution(true);
            splitpane.setMinimumWidth(10);
            splitpane.setMinimumHeight(10);
            splitpane.setHorizontal(false);
            splitpane.setDividerPosition(197);
            splitpane_saa = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpane_saa.setName("splitpane_saa");
            splitpane_saa.__internal_setGenerationDpi(96);
            splitpane_saa.registerTranslationHandler(translationHandler);
            splitpane_saa.setScaleForResolution(true);
            splitpane_saa.setMinimumWidth(0);
            splitpane_saa.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.LayoutBorder splitpane_saaLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpane_saa.setLayout(splitpane_saaLayout);
            panel_saa_top = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_saa_top.setName("panel_saa_top");
            panel_saa_top.__internal_setGenerationDpi(96);
            panel_saa_top.registerTranslationHandler(translationHandler);
            panel_saa_top.setScaleForResolution(true);
            panel_saa_top.setMinimumWidth(10);
            panel_saa_top.setMinimumHeight(10);
            panel_saa_top.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_saa_topLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_saa_top.setLayout(panel_saa_topLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panel_saa_topConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panel_saa_topConstraints.setPosition("north");
            panel_saa_top.setConstraints(panel_saa_topConstraints);
            splitpane_saa.addChild(panel_saa_top);
            panel_saa_center = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_saa_center.setName("panel_saa_center");
            panel_saa_center.__internal_setGenerationDpi(96);
            panel_saa_center.registerTranslationHandler(translationHandler);
            panel_saa_center.setScaleForResolution(true);
            panel_saa_center.setMinimumWidth(10);
            panel_saa_center.setMinimumHeight(10);
            panel_saa_center.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_saa_centerLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_saa_center.setLayout(panel_saa_centerLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panel_saa_centerConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panel_saa_center.setConstraints(panel_saa_centerConstraints);
            splitpane_saa.addChild(panel_saa_center);
            panel_saa_bottom = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_saa_bottom.setName("panel_saa_bottom");
            panel_saa_bottom.__internal_setGenerationDpi(96);
            panel_saa_bottom.registerTranslationHandler(translationHandler);
            panel_saa_bottom.setScaleForResolution(true);
            panel_saa_bottom.setMinimumWidth(10);
            panel_saa_bottom.setMinimumHeight(10);
            panel_saa_bottom.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_saa_bottomLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_saa_bottom.setLayout(panel_saa_bottomLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panel_saa_bottomConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panel_saa_bottomConstraints.setPosition("south");
            panel_saa_bottom.setConstraints(panel_saa_bottomConstraints);
            splitpane_saa.addChild(panel_saa_bottom);
            splitpane.addChild(splitpane_saa);
            splitpane_kem = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpane_kem.setName("splitpane_kem");
            splitpane_kem.__internal_setGenerationDpi(96);
            splitpane_kem.registerTranslationHandler(translationHandler);
            splitpane_kem.setScaleForResolution(true);
            splitpane_kem.setMinimumWidth(0);
            splitpane_kem.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.LayoutBorder splitpane_kemLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpane_kem.setLayout(splitpane_kemLayout);
            panel_kem_top = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_kem_top.setName("panel_kem_top");
            panel_kem_top.__internal_setGenerationDpi(96);
            panel_kem_top.registerTranslationHandler(translationHandler);
            panel_kem_top.setScaleForResolution(true);
            panel_kem_top.setMinimumWidth(10);
            panel_kem_top.setMinimumHeight(10);
            panel_kem_top.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_kem_topLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_kem_top.setLayout(panel_kem_topLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panel_kem_topConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panel_kem_topConstraints.setPosition("north");
            panel_kem_top.setConstraints(panel_kem_topConstraints);
            splitpane_kem.addChild(panel_kem_top);
            panel_kem_center = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_kem_center.setName("panel_kem_center");
            panel_kem_center.__internal_setGenerationDpi(96);
            panel_kem_center.registerTranslationHandler(translationHandler);
            panel_kem_center.setScaleForResolution(true);
            panel_kem_center.setMinimumWidth(10);
            panel_kem_center.setMinimumHeight(10);
            panel_kem_center.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_kem_centerLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_kem_center.setLayout(panel_kem_centerLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panel_kem_centerConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panel_kem_center.setConstraints(panel_kem_centerConstraints);
            splitpane_kem.addChild(panel_kem_center);
            panel_kem_bottom = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_kem_bottom.setName("panel_kem_bottom");
            panel_kem_bottom.__internal_setGenerationDpi(96);
            panel_kem_bottom.registerTranslationHandler(translationHandler);
            panel_kem_bottom.setScaleForResolution(true);
            panel_kem_bottom.setMinimumWidth(10);
            panel_kem_bottom.setMinimumHeight(10);
            panel_kem_bottom.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_kem_bottomLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_kem_bottom.setLayout(panel_kem_bottomLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panel_kem_bottomConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panel_kem_bottomConstraints.setPosition("south");
            panel_kem_bottom.setConstraints(panel_kem_bottomConstraints);
            splitpane_kem.addChild(panel_kem_bottom);
            splitpane.addChild(splitpane_kem);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder splitpaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            splitpane.setConstraints(splitpaneConstraints);
            panelMain.addChild(splitpane);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMainConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMain.setConstraints(panelMainConstraints);
            this.addChild(panelMain);
            buttonpanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonpanel.setName("buttonpanel");
            buttonpanel.__internal_setGenerationDpi(96);
            buttonpanel.registerTranslationHandler(translationHandler);
            buttonpanel.setScaleForResolution(true);
            buttonpanel.setMinimumWidth(10);
            buttonpanel.setMinimumHeight(10);
            buttonpanel.setDialogStyle(de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel.DialogStyle.CLOSE);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder buttonpanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            buttonpanelConstraints.setPosition("south");
            buttonpanel.setConstraints(buttonpanelConstraints);
            this.addChild(buttonpanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
//</editor-fold>
}