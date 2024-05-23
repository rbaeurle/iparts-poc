/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.forms.common.EditControlFactory;
import de.docware.apps.etk.base.forms.common.SimpleSelectSearchResultGrid;
import de.docware.apps.etk.base.forms.events.OnDblClickEvent;
import de.docware.apps.etk.base.forms.events.OnStartSearchEvent;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.common.EtkDataObjectArray;
import de.docware.apps.etk.base.project.common.EtkDataObjectArrayList;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsAuthorOrderStatus;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrderList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.OnEditChangeRecordEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsGotoHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsLoadEditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.VisualizeSAAHelper;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.table.HtmlTablePageSplitMode;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerOptions;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Formular für die Anzeige SAA/BK-Verwendung in den Stammdaten als nicht modales Fenster (Tabellen MAT und KATALOG).
 */
public class MasterDataSearchSaaBkUsageForm extends SimpleMasterDataSearchFilterGrid {

    protected static boolean gotoNonModalWindow = true;  // true: Öffne den Dialog in eigenem Fenster

    private static final int MIN_SAA_DIGITS = 7;

    // K_VARI ist für den Sprung in die Retailstückliste zwingend erforderlich.
    // K_LFDNR ist ebenfalls für den Sprung erforderlich und wird bei Bedarf unsichtbar hinzugefügt.
    private static final String[] MUST_HAVE_FIELDS = new String[]{ TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VARI),
                                                                   TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VER),
                                                                   TableAndFieldName.make(TABLE_KATALOG, FIELD_K_SACH),
                                                                   TableAndFieldName.make(TABLE_KATALOG, FIELD_K_SVER),
                                                                   TableAndFieldName.make(TABLE_KATALOG, FIELD_K_LFDNR),
                                                                   TableAndFieldName.make(TABLE_MAT, FIELD_M_MATNR) };


    /**
     * Static-Methode, die den Dialog öffnet mit abgespeckten Parametern.
     *
     * @param owner
     */
    public static void showSaaBkUsage(AbstractJavaViewerForm owner) {
        // Aktive Form holen
        AbstractJavaViewerForm activeForm = owner.getConnector().getActiveForm();

        // Die ggf. gewählte SAA holen.
        String treeValue = getIdFromTreeSelectionForType(activeForm, iPartsSaaId.TYPE);
        iPartsSaaId saaId = new iPartsSaaId(treeValue);
        if (!saaId.isValidId()) {
            saaId = null;
        }

        // Und auch das ggf. gewählte Produkt holen.
        treeValue = getIdFromTreeSelectionForType(activeForm, iPartsProductId.TYPE);
        iPartsProductId productId = new iPartsProductId(treeValue);
        if (!productId.isValidId()) {
            productId = null;
        }

        showSaaBkUsage(activeForm.getConnector(), activeForm, saaId, productId, null);
    }

    /**
     * Klassenmethode die den Dialog öffnet.
     *
     * @param dataConnector
     * @param parentForm
     * @param saaId
     * @param productId
     * @param onEditChangeRecordEvent
     */
    private static void showSaaBkUsage(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                       iPartsSaaId saaId, iPartsProductId productId, OnEditChangeRecordEvent onEditChangeRecordEvent) {

        MasterDataSearchSaaBkUsageForm dlg = new MasterDataSearchSaaBkUsageForm(dataConnector, parentForm, TABLE_DWARRAY, onEditChangeRecordEvent);
        EtkProject project = dataConnector.getProject();

        // Suchfelder definieren
        EtkDisplayFields searchFields = getSearchFieldsForSaaBkUsageForm(project);
        // Anzeigefelder definieren
        EtkDisplayFields displayFields = getDisplayFieldsForSaaBkUsageForm(project);
        // Nur Anzeige, keine Editfelder.
        EtkEditFields editFields = null;

        dlg.setSearchFields(searchFields);
        dlg.setDisplayResultFields(displayFields);
        dlg.setEditFields(editFields);

        // Momentan sind keinerlei Edit-Aktionen erwünscht.
        boolean editMasterDataAllowed = false;
        boolean deleteMasterDataAllowed = false;
        boolean modifyMasterDataAllowed = false;

        dlg.setEditAllowed(editMasterDataAllowed || deleteMasterDataAllowed || modifyMasterDataAllowed);
        dlg.setNewAllowed(editMasterDataAllowed);
        dlg.setModifyAllowed(modifyMasterDataAllowed);
        dlg.setDeleteAllowed(deleteMasterDataAllowed);
        dlg.showToolbar(false);

        dlg.setWindowTitle("!!Suche nach SAA/BK Verwendung");
        dlg.setTitlePrefix("!!Suche nach SAA/BK Verwendung");
        dlg.setWindowName("MasterDataSearchSaaBkUsage");
        // nicht bereits für SQL ein Limit setzen
//        dlg.setMaxResults(J2EEHandler.isJ2EE() ? MAX_SELECT_SEARCH_RESULTS_SIZE : -1);

        // Wurde eine SAA/BK oder Produkt übergeben, dann direkt danach suchen
        DBDataObjectAttributes searchAttributes = new DBDataObjectAttributes();
        if ((saaId != null) && saaId.isValidId()) {
            // Suchwerte setzen und Suche starten
            searchAttributes.addField(FIELD_DS_SAA, saaId.getSaaNumber(), DBActionOrigin.FROM_DB);
        }
        if ((productId != null) && productId.isValidId()) {
            searchAttributes.addField(FIELD_DP_PRODUCT_NO, productId.getProductNumber(), DBActionOrigin.FROM_DB);
        }
        if (!searchAttributes.isEmpty()) {
            dlg.setSearchValues(searchAttributes);
        }

        // Den Dialog entweder modal oder nicht-modal anzeigen, je nach Schalter
        if (dlg.gotoNonModalWindow) {
            dlg.showNonModal();
        } else {
            dlg.showModal();
        }
    }

    /**
     * Liefert die Suchfelder zurück.
     *
     * @param project
     * @return
     */
    public static EtkDisplayFields getSearchFieldsForSaaBkUsageForm(EtkProject project) {
        EtkDisplayFields searchFields = new EtkDisplayFields();

        if (searchFields.size() == 0) {
            // Suchfelder definieren
            EtkDisplayField searchField = createSearchField(project, TABLE_DA_SAA, FIELD_DS_SAA, false, false);
            EtkMultiSprache multi = new EtkMultiSprache();
            multi.setText(project.getViewerLanguage(), "!!SAA/BK Nummer");
            searchField.setText(multi);
            searchField.setDefaultText(false);
            searchFields.addFeld(searchField);
            searchFields.addFeld(createSearchField(project, TABLE_DA_PRODUCT, FIELD_DP_PRODUCT_NO, false, false));
        }
        return searchFields;
    }

    /**
     * Liefert die anzuzeigenden Spalten zurück.
     * Entweder die konfigirierten Spalten, oder die Default-Spalten.
     *
     * @param project
     * @return
     */
    public static EtkDisplayFields getDisplayFieldsForSaaBkUsageForm(EtkProject project) {
        EtkDisplayFields displayFields = new EtkDisplayFields();
        displayFields.load(project.getConfig(), iPartsEditConfigConst.iPARTS_EDIT_MASTER_SAA_BK_USAGE + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS);

        EtkDisplayField oneDisplayField;
        // Die Default-Felder, falls nichts konfiguriert ist
        if (displayFields.size() == 0) {
            oneDisplayField = addDisplayField(TABLE_KATALOG, FIELD_K_VARI, false, false, null, project, displayFields);
            EtkMultiSprache multi = new EtkMultiSprache();
            multi.setText(project.getViewerLanguage(), "!!Produkt/freie SA_KG_TU");
            oneDisplayField.setText(multi);
            oneDisplayField.setDefaultText(false);
            oneDisplayField.setColumnFilterEnabled(true);
            oneDisplayField = addDisplayField(TABLE_KATALOG, FIELD_K_LFDNR, false, false, null, project, displayFields);
            oneDisplayField.setColumnFilterEnabled(true);
            oneDisplayField = addDisplayField(TABLE_KATALOG, FIELD_K_MATNR, false, false, null, project, displayFields);
            oneDisplayField.setColumnFilterEnabled(true);
            oneDisplayField = addDisplayField(TABLE_MAT, FIELD_M_TEXTNR, true, false, null, project, displayFields);
            oneDisplayField.setColumnFilterEnabled(true);
            oneDisplayField = addDisplayField(TABLE_KATALOG, FIELD_K_MENGE, false, false, null, project, displayFields);
            oneDisplayField.setColumnFilterEnabled(true);
            oneDisplayField = addDisplayField(TABLE_KATALOG, FIELD_K_HIERARCHY, false, false, null, project, displayFields);
            oneDisplayField.setColumnFilterEnabled(true);
            oneDisplayField = addDisplayField(TABLE_DWARRAY, FIELD_DWA_TOKEN, false, false, null, project, displayFields);
            oneDisplayField.setColumnFilterEnabled(true);
            // nur zum Testen
            if (Constants.DEVELOPMENT & !Constants.DEVELOPMENT_QFTEST) {
                oneDisplayField = addDisplayField(TABLE_KATALOG, FIELD_K_SA_VALIDITY, false, true, null, project, displayFields);
                oneDisplayField.setColumnFilterEnabled(true);
            }
        }

        // Bei DWARRAY.DWA_TOKEN die anzuzeigende Spaltenüberschrift überschreiben
        oneDisplayField = displayFields.getFeldByName(TABLE_DWARRAY, FIELD_DWA_TOKEN);
        if (oneDisplayField != null) {
            EtkDatabaseField dbFieldForHeading = project.getFieldDescription(TABLE_DA_SAA, FIELD_DS_SAA);
            if (dbFieldForHeading != null) {
                oneDisplayField.setText(dbFieldForHeading.getDisplayTexts(project.getAvailLanguages()));
                oneDisplayField.setDefaultText(false);
            }
        }
        return displayFields;
    }


    private GuiMenuItem gotoUsageMenuItem;
    private GuiMenuItem loadTUsInEditMenuItem;
    private GuiMenuItem visualizeSAAMenu;
    private boolean testProducts = false;  // Testweises Eiblenden der Produktspalte
    private Set<String> arrayFieldNames;
    private final boolean isPSKAllowed = iPartsRight.checkPSKInSession();
    private final boolean hasBothCarAndTruckRights = iPartsRight.checkUserHasBothVehicleTypeRightsInSession();
    private final boolean hasNeitherCarNorTruckRights = !iPartsRight.checkCarAndVanInSession() && !iPartsRight.checkTruckAndBusInSession();
    private final HashMap<iPartsProductId, Boolean> productValidityMap;

    /**
     * Erzeugt eine Instanz von SimpleMasterDataSearchResultGrid.
     *
     * @param dataConnector
     * @param parentForm
     * @param tableName
     * @param onEditChangeRecordEvent
     */
    public MasterDataSearchSaaBkUsageForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName, OnEditChangeRecordEvent onEditChangeRecordEvent) {
        super(dataConnector, parentForm, tableName, onEditChangeRecordEvent);

        SearchSaaBkUsageFilterFactory filterFactory = new SearchSaaBkUsageFilterFactory(getProject());
        setColumnFilterFactory(filterFactory);
        getTable().setColumnFilterFactory(filterFactory);

        // Schließen-Button anstatt OK-Button anzeigen
        getButtonPanel().setButtonVisible(GuiButtonOnPanel.ButtonType.OK, false);
        getButtonPanel().setButtonVisible(GuiButtonOnPanel.ButtonType.CLOSE, true);
        getButtonPanel().getButtonOnPanel(GuiButtonOnPanel.ButtonType.CLOSE).addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                close();
            }
        });

        doResizeWindow(SCREEN_SIZES.MAXIMIZE);
        setOnStartSearchEvent(new OnStartSearchEvent() {
            @Override
            public void onStartSearch() {
            }
        });


        OnDblClickEvent onDblClickEvent = new OnDblClickEvent() {
            @Override
            public void onDblClick() {
                gotoSaaBkUsage(null);
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

        initVirtualMapping();
        // Table in den Page-Mode setzen
        getTable().setPageSplitNumberOfEntriesPerPage(J2EEHandler.isJ2EE() ? MAX_SELECT_SEARCH_RESULTS_SIZE : -1);
        getTable().setHtmlTablePageSplitMode(HtmlTablePageSplitMode.BUTTONS);

        productValidityMap = new HashMap<>();
    }

    @Override
    public void setSearchFields(EtkDisplayFields searchFields) {
        super.setSearchFields(searchFields);
        // Tooltips bei Suchfeldern setzen
        EditControlFactory editControl = getEditControl(TABLE_DA_SAA, FIELD_DS_SAA);
        if (editControl != null) {
            editControl.getControl().setTooltip(TranslationHandler.translate("!!Mussfeld, mindestens %1 Zeichen ohne Wildcards, \'*\' wird automatisch angehängt.", Integer.toString(MIN_SAA_DIGITS)));
        }

        editControl = getEditControl(TABLE_DA_PRODUCT, FIELD_DP_PRODUCT_NO);
        if (editControl != null) {
            editControl.getControl().setTooltip(TranslationHandler.translate("!!Eingabe optional, \'*\' wird automatisch angehängt."));
        }
    }

    @Override
    public void setDisplayResultFields(EtkDisplayFields displayResultFields) {
        if (testProducts) {
            int position = displayResultFields.getIndexOfFeld(TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VARI), false);
            if (position < 0) {
                position = 0;
            }
            EtkDisplayField displayField = createDisplayField(getProject(), TABLE_DA_PRODUCT, FIELD_DP_PRODUCT_NO, false, false);
            displayField.loadStandards(getConfig());
            displayResultFields.addFeld(position + 1, displayField);
        }
        super.setDisplayResultFields(displayResultFields);
        arrayFieldNames = new HashSet<>();
        if (displayResultFields != null) {
            for (EtkDisplayField displayField : displayResultFields.getVisibleFields()) {
                if (displayField.isArray() && !VirtualFieldsUtils.isVirtualField(displayField.getKey().getFieldName())) {
                    arrayFieldNames.add(displayField.getKey().getName());
                }
            }
        }
    }

    /**
     * Überprüfung des Mussfeldes auf Eingabewert, in dessen Abhängigkeit dann der "Suche starten"-Button
     * aktiviert oder deaktiviert wird. Der eingegebene SAA-String hat auch noch eine Mindestlänge.
     * <p>
     * Wenn sich an einem der Eingabefelder etwas ändert, landen wir hier.
     *
     * @return
     */
    @Override
    protected boolean checkControlChange() {
        // Die Eingabe einer SAA ist zwingend erforderlich.
        // Ohne einen Wert wird der "Suche starten"-Button nicht aktiviert.
        boolean isEnabled = super.checkControlChange();
        if (isEnabled) {
            String saaInputValue = getValueFromEditControl(TABLE_DA_SAA, FIELD_DS_SAA);
            isEnabled = StrUtils.isValid(saaInputValue);
            if (isEnabled) {
                // Die Wildcard-Suche wird unter 5 Zeichen SAA/BK unerträglich langsam, daher eine sinnvolle Mindestlänge von MIN_SAA_DIGITS.
                String searchValueWithoutWildcards = saaInputValue.replace("*", "").replace("?", "");
                isEnabled = searchValueWithoutWildcards.length() >= MIN_SAA_DIGITS;
            }
        }
        return isEnabled;
    }


    /**
     * Den Menüpunkt zum Sprung zur Verwendung einhängen.
     *
     * @param toolbarHelper
     * @param contextMenu
     */
    @Override
    protected void addToolbarButtonsAndContextMenuItems(EditToolbarButtonMenuHelper toolbarHelper, GuiContextMenu contextMenu) {
        super.addToolbarButtonsAndContextMenuItems(toolbarHelper, contextMenu);
        gotoUsageMenuItem = toolbarHelper.createMenuEntry("goToSaaBkUsage", "!!Gehe zur Verwendung...", DefaultImages.module.getImage(),
                                                          new EventListener(Event.MENU_ITEM_EVENT, EventListenerOptions.SYNCHRON_EVENT) {
                                                              @Override
                                                              public void fire(Event event) {
                                                                  endSearch();
                                                                  gotoSaaBkUsage(event);
                                                              }
                                                          }, getUITranslationHandler());
        contextMenu.addChild(gotoUsageMenuItem);

        loadTUsInEditMenuItem = toolbarHelper.createMenuEntry("loadTUsInEdit", "!!TU laden", DefaultImages.module.getImage(),
                                                              new EventListener(Event.MENU_ITEM_EVENT, EventListenerOptions.SYNCHRON_EVENT) {
                                                                  @Override
                                                                  public void fire(Event event) {
                                                                      loadTUsInEdit(event);
                                                                  }
                                                              }, getUITranslationHandler());
        contextMenu.addChild(loadTUsInEditMenuItem);

        contextMenu.addChild(toolbarHelper.createMenuSeparator(VisualizeSAAHelper.IPARTS_MENU_ITEM_VISUALIZE_SAA_3D + "Separator",
                                                               getUITranslationHandler()));
        visualizeSAAMenu = VisualizeSAAHelper.createVisualizeSAAPopupMenuItem(toolbarHelper, getUITranslationHandler(),
                                                                              new EventListener(Event.MENU_ITEM_EVENT) {
                                                                                  @Override
                                                                                  public void fire(Event event) {
                                                                                      VisualizeSAAHelper.visualizeSAAs(getSelectedSAAs());
                                                                                  }
                                                                              });
        contextMenu.addChild(visualizeSAAMenu);
    }

    @Override
    protected void enableButtons() {
        super.enableButtons();

        int selectionRowCount = getTable().getSelectedRows().size();
        boolean singleSelection = selectionRowCount == 1;

        gotoUsageMenuItem.setEnabled(singleSelection);

        Set<String> selectedModulSet = getSelectedModuleSet();
        if (!selectedModulSet.isEmpty()) {
            String text = "!!TU laden";
            boolean loadReadOnly = isLoadTUsForReadOnly();
            if (selectedModulSet.size() > 1) {
                if (loadReadOnly) {
                    text = "!!TUs laden";
                } else {
                    text = "!!TUs bearbeiten";
                }
            } else {
                if (!loadReadOnly) {
                    text = "!!TU bearbeiten";
                }
            }
            loadTUsInEditMenuItem.setEnabled(true);
            loadTUsInEditMenuItem.setText(text);
        } else {
            loadTUsInEditMenuItem.setEnabled(false);
        }

        visualizeSAAMenu.setEnabled(!getSelectedSAAs().isEmpty());
    }

    /**
     * Liefert zurück, ob die TUs beim Laden für die "readOnly"-Sicht geladen werden sollen
     *
     * @return
     */
    private boolean isLoadTUsForReadOnly() {
        boolean result = true;
        iPartsDataAuthorOrder currentAuthorOrder = iPartsDataAuthorOrderList.getAuthorOrderByActiveChangeSetForEdit(getProject());
        if (currentAuthorOrder != null) {
            result = iPartsAuthorOrderStatus.isEndState(currentAuthorOrder.getStatus());
        }
        return result;
    }


    @Override
    protected String getVisualValueOfFieldValue(String tableName, String fieldName, DBDataObjectAttribute fieldValue, boolean isMultiLanguage) {
        if (fieldName.equals(FIELD_DWA_TOKEN)) {
            return iPartsNumberHelper.formatPartNo(getProject(), fieldValue.getAsString());
        } else if ((arrayFieldNames != null) && !arrayFieldNames.isEmpty() && arrayFieldNames.contains(TableAndFieldName.make(tableName, fieldName))) {
            return iPartsNumberHelper.formatArrayValues(getProject(), tableName, fieldName, fieldValue);
        }

        return super.getVisualValueOfFieldValue(tableName, fieldName, fieldValue, isMultiLanguage);
    }

    /**
     * Der Sprung zur Verwendung
     *
     * @param event
     */
    private void gotoSaaBkUsage(Event event) {
        DBDataObjectAttributes attributes = getSelectedAttributes();
        if ((attributes != null) && !attributes.isEmpty()) {
            String moduleNo = getModuleNo(attributes);
            String lfdNr = getLfdNr(attributes);
            iPartsGotoHelper.gotoModuleWithDifferentOptions(getConnector(), this, getProject(), moduleNo, lfdNr);
        }
    }

    /**
     * Selektierte TUs im Editor laden
     *
     * @param event
     */
    private void loadTUsInEdit(Event event) {
        Set<String> selectedModulList = getSelectedModuleSet();
        if (!selectedModulList.isEmpty()) {
            iPartsLoadEditModuleHelper.doLoadModules(getConnector(), selectedModulList, "",
                                                     isLoadTUsForReadOnly(), false, null);
        }
    }

    private Set<String> getSelectedModuleSet() {
        Set<String> selectedModulSet = new HashSet<>();
        DBDataObjectAttributesList selectedAttributesList = getSelectedAttributesList();
        if ((selectedAttributesList != null) && !selectedAttributesList.isEmpty()) {
            for (DBDataObjectAttributes attributes : selectedAttributesList) {
                String moduleNo = getModuleNo(attributes);
                if (StrUtils.isValid(moduleNo)) {
                    selectedModulSet.add(moduleNo);
                }
            }
        }
        return selectedModulSet;
    }

    private Set<String> getSelectedSAAs() {
        return VisualizeSAAHelper.getSelectedSAAsFromAttributesList(getSelectedAttributesList(), FIELD_DWA_TOKEN);
    }

    protected String getModuleNo(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(FIELD_K_VARI);
    }

    protected String getLfdNr(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(FIELD_K_LFDNR);
    }

    /**
     * Mapping für virtuelle Felder (Abhängigkeit bezüglich BK und SAA)
     */
    private void initVirtualMapping() {

        // -----------------------------------------
        // IN VORBEREITUNG:
        // Momentan gibt es keine virtuellen Felder.
        // -----------------------------------------

        // virtualFieldMapping = new HashMap<String, String>();
        // virtualFieldMapping.put(makeVirtualMappinKey(TABLE_MAT, iPartsDataVirtualFieldsDefinition.DESM_DESCRIPTION), FIELD_M_TEXTNR);
        // virtualFieldMapping.put(makeVirtualMappinKey(TABLE_MAT, iPartsDataVirtualFieldsDefinition.DESM_DESCRIPTION_CONST), FIELD_M_CONST_DESC);
    }

    /**
     * Funktion, die den in einem bestimmten EditControl eingegebenen String auf maximale Datenbankfeldlänge kürzt
     * und zurückgibt.
     *
     * @param tablename Tabellenname des gesuchten EditControls
     * @param fieldname Feldname des gesuchten EditControls
     * @return der ggf. passend zur Datenbankfeldlänge gekürzte Wert oder null.
     */
    private String getValueFromEditControl(String tablename, String fieldname) {
        String searchTableAndFieldname = TableAndFieldName.make(tablename, fieldname);
        if ((searchFields != null) && (editControls != null)) {
            for (int i = 0; i < searchFields.size(); i++) {
                EditControl editCtrl = editControls.getControlByFeldIndex(i);
                if (editCtrl != null) {
                    EditControlFactory ctrl = editCtrl.getEditControl();
                    if (ctrl != null) {
                        if (ctrl.getTableFieldName().equals(searchTableAndFieldname)) {
                            String value = ctrl.getText().trim().toUpperCase();
                            if (StrUtils.isValid(value)) {
                                // Länge von searchValue auf die maximale Feldlänge beschränken, um DB-Fehler zu vermeiden
                                value = getProject().getConfig().getDBDescription().
                                        cutValueIfLongerThanFieldLength(value, ctrl.getTableName(), ctrl.getFieldName());
                                return value;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Holt sich aus den Edit-Controls der Form das zum Tabellen und Feldnamen passende Eingabefeld/-control.
     *
     * @param tablename
     * @param fieldname
     * @return das gefundene Conrol oder null
     */
    private EditControlFactory getEditControl(String tablename, String fieldname) {
        String searchTableAndFieldname = TableAndFieldName.make(tablename, fieldname);
        if (editControls != null) {
            for (int i = 0; i < editControls.size(); i++) {
                EditControl editCtrl = editControls.getControlByFeldIndex(i);
                if (editCtrl != null) {
                    EditControlFactory ctrl = editCtrl.getEditControl();
                    if (ctrl != null) {
                        if (ctrl.getTableFieldName().equals(searchTableAndFieldname)) {
                            return ctrl;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Wildcards sind nur am Stringende zugelassen, im String nicht!
     * Überprüfung mit Warnmeldung, falls gewünscht.
     *
     * @param saaInputValue
     * @param tablename
     * @param fieldname
     * @param showWarning
     * @return
     */
    private boolean checkWildcardValidity(String saaInputValue, String tablename, String fieldname, boolean showWarning) {
        String searchValueWithoutTrailingWildcard = StrUtils.removeLastCharacterIfCharacterIs(saaInputValue, '*');
        searchValueWithoutTrailingWildcard = StrUtils.removeLastCharacterIfCharacterIs(searchValueWithoutTrailingWildcard, '?');
        String searchValueWithoutWildcards = saaInputValue.replace("*", "").replace("?", "");
        if (searchValueWithoutTrailingWildcard.length() > searchValueWithoutWildcards.length()) {
            if (showWarning) {
                EtkProject project = getProject();
                EtkDatabaseField dbField = project.getFieldDescription(tablename, fieldname);
                showWarning("!!Wildcards sind nur am Ende der %1 zulässig.",
                            dbField.getDisplayText(project.getViewerLanguage(), project.getAvailLanguages()));
            }
            return false;
        }
        return true;
    }

    /**
     * Sicherstellen, dass die Primärschlüsselfelder für den Stücklisteneintrag und das Material in den selectFields
     * vorhanden sind, um später virtuelle Felder berechnen zu können
     *
     * @param selectFields
     */
    private void addMustFields(EtkDisplayFields selectFields) {
        for (String tableAndFieldName : MUST_HAVE_FIELDS) {
            selectFields.addFeldIfNotExists(new EtkDisplayField(tableAndFieldName, false, false));
        }
    }

    @Override
    protected void prepareGuiForSearch() {
        super.prepareGuiForSearch();
        SearchSaaBkUsageFilterFactory filterFactory = new SearchSaaBkUsageFilterFactory(getProject());
        setColumnFilterFactory(filterFactory);
        getTable().setColumnFilterFactory(filterFactory);
        productValidityMap.clear();
    }

    /**
     * Die Suche der darzustellenden Daten in der Datenbank.
     *
     * @return
     */
    @Override
    protected boolean executeExplicitSearch() {
        return doLoadFromDBTwice();
    }

    /**
     * SAA/BK Nummer oder freie SA mit Verwendungen in Produkten mit den AS-Produktklassen PKW, Transporter, Geländewagen,
     * Smart werden Anwendern mit Eigenschaft PKW/Van angezeigt
     * SAA/BK Nummer oder freie SA mit Verwendungen in Produkte mit den AS-Produktklassen LKW, MB Trac, BUS, Powersystems,
     * Unimog werden Anwendern mit Eigenschaft Truck/Bus angezeigt
     * Freie SA, die nicht verortet sind, werden Anwendern mit Eigenschaft Truck/Bus angezeigt
     *
     * @param attributes
     * @return
     */
    @Override
    protected boolean doValidateAttributes(DBDataObjectAttributes attributes) {
        // Logik wie bei TU laden
        String moduleNumber = getModuleNo(attributes);
        EtkProject project = getProject();
        return iPartsFilterHelper.isModuleVisibleForUserSession(project, isPSKAllowed, hasBothCarAndTruckRights,
                                                                hasNeitherCarNorTruckRights,
                                                                moduleNumber, productValidityMap);
    }

    private void showWarning(String key, String... placeHolderTexts) {
        MessageDialog.showWarning(TranslationHandler.translate(key, placeHolderTexts));
    }

    private boolean doLoadFromDBTwice() {
        String saaInputValue = getValueFromEditControl(TABLE_DA_SAA, FIELD_DS_SAA);
        // Die SAA ist zwingend erforderlich für die Suche
        if (saaInputValue == null) {
            showWarning("!!Die Eingabe einer SAA Nummer ist zwingend erforderlich");
            return true;
        }

        // Wildcards sind im Suchstring nicht zulässig, nur am Ende.
        if (!checkWildcardValidity(saaInputValue, TABLE_DA_SAA, FIELD_DS_SAA, true)) {
            return true;
        }

        // Ggf. die formatierte SAA/BK-Nummer in das in der Datenbank gespeicherte Format umwandeln.
        iPartsNumberHelper numberHelper = new iPartsNumberHelper();
        saaInputValue = numberHelper.unformatSaaBkForEdit(getProject(), saaInputValue);

        // Ans Ende der SAA-Nummer automatisch einen '*' anhängen.
        WildCardSettings wildCardSettings = new WildCardSettings();
        wildCardSettings.addNoAutoWildCard();
        wildCardSettings.addWildCardEnd();
        saaInputValue = wildCardSettings.makeWildCard(saaInputValue);

        // Die SAA-Nummer ist zwingend erforderlich, die Produktnummer ist optional.
        // [productAvailable] steuert auch den left (outer/nicht outer) Joint auf DA_PRODUCT_MODULES.
        String productInputValue = getValueFromEditControl(TABLE_DA_PRODUCT, FIELD_DP_PRODUCT_NO);
        boolean productAvailable = productInputValue != null;
        if (productAvailable) {
            // Ans Ende der Produktnummer automatisch einen '*' anhängen.
            productInputValue = wildCardSettings.makeWildCard(productInputValue);
        }

        String[] sortFields = new String[]{ TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VARI),
                                            TableAndFieldName.make(TABLE_KATALOG, FIELD_K_SEQNR)/*,
                                   TableAndFieldName.make(TABLE_DWARRAY, FIELD_DWA_TOKEN)*/ };

        // Die Ergebnisliste anlegen und passend initialisieren
        EtkDataObjectArrayList list = new EtkDataObjectArrayList();
        list.setSearchWithoutActiveChangeSets(false);
        list.clear(DBActionOrigin.FROM_DB);

        EtkDisplayFields selectFields = getDisplayFieldsForSaaBkUsageForm(getProject());

        // Virtuelle Felder entfernen aus den selectFields
        EtkDisplayFields resultDisplayFields = new EtkDisplayFields();
        for (EtkDisplayField field : selectFields.getFields()) {
            if (!VirtualFieldsUtils.isVirtualField(field.getKey().getFieldName())) {
                resultDisplayFields.addFeld(field);
            }
        }
        selectFields = resultDisplayFields;

        // Sicherstellen, dass die Primärschlüsselfelder für den Stücklisteneintrag und das Material in den selectFields
        // vorhanden sind, um später virtuelle Felder berechnen zu können
        addMustFields(selectFields);

        EtkDataObjectList.JoinData[] joinDatas = buildJoinDatasTwice(true, productAvailable);

        String[] whereTableAndFields = new String[]{ TableAndFieldName.make(TABLE_DWARRAY, FIELD_DWA_FELD),
                                                     TableAndFieldName.make(TABLE_DWARRAY, FIELD_DWA_TOKEN) };
        String[] whereValues = new String[]{ TableAndFieldName.make(TABLE_KATALOG, FIELD_K_SA_VALIDITY),
                                             saaInputValue };
        if (productAvailable) {
            whereTableAndFields = StrUtils.mergeArrays(whereTableAndFields, new String[]{ TableAndFieldName.make(TABLE_DA_PRODUCT_MODULES, FIELD_DPM_PRODUCT_NO) });
            whereValues = StrUtils.mergeArrays(whereValues, new String[]{ productInputValue });
        }

        // Die Daten mit eingegebenem Produkt holen.
        list.searchSortAndFillWithJoin(getProject(), getProject().getDBLanguage(),
                                       selectFields,
                                       whereTableAndFields,
                                       whereValues,
                                       false,
                                       sortFields,
                                       false, true,
                                       null,
                                       joinDatas);
        if (!productAvailable) {
            // ohne Produkt-Suche liefert die Suche über FIELD_DWA_TOKEN bereits alle Treffer
            if (!list.isEmpty()) {
                for (EtkDataObjectArray dataObject : list) {
                    DBDataObjectAttributes attributes = dataObject.getAttributes();
                    addFoundAttributes(attributes);
                }
            }
            return true;
        }


        // mit Produkt-Suche muss für die freien SAs noch die Verwendung gesucht werden
        joinDatas = buildJoinDatasTwice(false, productAvailable);

        whereTableAndFields = new String[]{ TableAndFieldName.make(TABLE_DWARRAY, FIELD_DWA_FELD),
                                            TableAndFieldName.make(TABLE_DWARRAY, FIELD_DWA_TOKEN) };
        if (productAvailable) {
            whereTableAndFields = StrUtils.mergeArrays(whereTableAndFields, new String[]{ TableAndFieldName.make(TABLE_DA_PRODUCT_SAS, FIELD_DPS_PRODUCT_NO) });
        }

        EtkDataObjectList.FoundAttributesCallback callback = createFoundAttributesCallbackTwice(productAvailable);

        list.searchSortAndFillWithJoin(getProject(), getProject().getDBLanguage(),
                                       selectFields,
                                       whereTableAndFields,
                                       whereValues,
                                       false,
                                       sortFields,
                                       false, true,
                                       callback,
                                       joinDatas);
        if (!list.isEmpty()) {
            for (EtkDataObjectArray dataObject : list) {
                DBDataObjectAttributes attributes = dataObject.getAttributes();
                addFoundAttributes(attributes);
            }
        }

        return true;
    }

    protected EtkDataObjectList.JoinData[] buildJoinDatasTwice(boolean isTuSearch, boolean needsProductJoin) {
        List<EtkDataObjectList.JoinData> helper = new DwList<>();
        // Die Join-Felder aus KATALOG
        EtkDataObjectList.JoinData joinKatalog = new EtkDataObjectList.JoinData(TABLE_KATALOG,
                                                                                new String[]{ FIELD_DWA_ARRAYID },
                                                                                new String[]{ FIELD_K_SA_VALIDITY },
                                                                                false, false);
        helper.add(joinKatalog);

        // Die Join-Felder aus MAT
        EtkDataObjectList.JoinData joinMat = new EtkDataObjectList.JoinData(TABLE_MAT,
                                                                            new String[]{ TableAndFieldName.make(TABLE_KATALOG, FIELD_K_MATNR) },
                                                                            new String[]{ FIELD_M_BESTNR },
                                                                            false, false);
        helper.add(joinMat);

        if (isTuSearch) {
            if (needsProductJoin) {
                // Die Join-Felder aus DA_PRODUCT_MODULES (werden nur mit eingegebenem Produkt benötigt).
                EtkDataObjectList.JoinData joinProductModules = new EtkDataObjectList.JoinData(TABLE_DA_PRODUCT_MODULES,
                                                                                               new String[]{ TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VARI) },
                                                                                               new String[]{ FIELD_DPM_MODULE_NO },
                                                                                               true, false);
                helper.add(joinProductModules);
            }
        } else {
            // bei Suche nach SAs
            EtkDataObjectList.JoinData joinModuleSas = new EtkDataObjectList.JoinData(TABLE_DA_SA_MODULES,
                                                                                      new String[]{ TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VARI) },
                                                                                      new String[]{ FIELD_DSM_MODULE_NO },
                                                                                      true, false);
            helper.add(joinModuleSas);

            if (needsProductJoin) {
                EtkDataObjectList.JoinData joinProductSas = new EtkDataObjectList.JoinData(TABLE_DA_PRODUCT_SAS,
                                                                                           new String[]{ TableAndFieldName.make(TABLE_DA_SA_MODULES, FIELD_DSM_SA_NO) },
                                                                                           new String[]{ FIELD_DPS_SA_NO },
                                                                                           true, false);
                helper.add(joinProductSas);
            }
        }
        return ArrayUtil.toArray(helper);
    }

    protected EtkDataObjectList.FoundAttributesCallback createFoundAttributesCallbackTwice(boolean productAvailable) {
        // Freie SAs können beliebig häugi vorkommen => beschränke die Suche auf den ersten Treffer
        Set<String> usedModules = new HashSet<>();

        return new EtkDataObjectList.FoundAttributesCallback() {

            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                String key = buildKey(attributes);
                if (usedModules.contains(key)) {
                    return false;
                }
                usedModules.add(key);
                return true;
            }

            private String buildKey(DBDataObjectAttributes attributes) {
                return getModuleNo(attributes) + "&" + getLfdNr(attributes);
            }
        };
    }

    @Override
    protected String getValueForVirtualField(EtkDisplayField virtualField, DBDataObjectAttributes attributes) {
        // Virtuelles Feld über das entsprechende EtkDataObject berechnen
        EtkProject project = getProject();
        String tableName = virtualField.getKey().getTableName();
        String fieldName = virtualField.getKey().getFieldName();
        EtkDataObject dataObject = null;
        if (tableName.equals(TABLE_KATALOG)) {
            PartListEntryId partListEntryId = new PartListEntryId(attributes.getFieldValue(FIELD_K_VARI), attributes.getFieldValue(FIELD_K_VER),
                                                                  attributes.getFieldValue(FIELD_K_LFDNR));
            dataObject = EtkDataObjectFactory.createDataPartListEntry(project, partListEntryId);
        } else if (tableName.equals(TABLE_MAT)) {
            PartId partId = new PartId(attributes.getFieldValue(FIELD_M_MATNR), "");
            dataObject = EtkDataObjectFactory.createDataPart(project, partId);
        }

        if (dataObject != null) {
            return dataObject.getFieldValue(fieldName, project.getDBLanguage(), true);
        } else {
            return super.getValueForVirtualField(virtualField, attributes);
        }
    }

    /**
     * eigene FilterFactory für Spaltenfilter und FilterValues
     * Wird auch bei mehrfachen Filtern benötigt wegen mehrfachen konfigurierbaren Tabellen
     */
    public class SearchSaaBkUsageFilterFactory extends SimpleMasterDataSearchFilterFactory {

        public SearchSaaBkUsageFilterFactory(EtkProject project) {
            super(project);
        }

        @Override
        protected String getFilterValueFromVisObject(String value, EditControlFactory control) {
            // Hier können die Werte für einzelne Felder formatiert werden
            if (control.getTableFieldName().equals(TableAndFieldName.make(TABLE_DWARRAY, FIELD_DWA_TOKEN))) {
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
