/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.AbstractSearchQueryCancelable;
import de.docware.apps.etk.base.db.EtkDbsSearchQueryCancelable;
import de.docware.apps.etk.base.db.sqlbuilder.EtkSqlCommonDbSelect;
import de.docware.apps.etk.base.extnav.config.ExtNavBaseConsts;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.*;
import de.docware.apps.etk.base.forms.common.components.MenuRunnable;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.forms.events.*;
import de.docware.apps.etk.base.mechanic.mainview.forms.AbstractMechanicForm;
import de.docware.apps.etk.base.mechanic.mainview.forms.MechanicFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.NavigationPath;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.OnEditChangeRecordEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditUserControlsHelper;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.config.db.EtkFieldType;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.*;
import de.docware.framework.modules.gui.controls.*;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.table.*;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.Keys;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.framework.utils.GuiUtils;
import de.docware.framework.utils.VarParam;
import de.docware.util.ArrayUtil;
import de.docware.util.CanceledException;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * Einfacher Dialog um eine Schnellsuche über definierte Felder in einer Tabelle zu haben
 */
public class SimpleMasterDataSearchResultGrid extends AbstractJavaViewerForm implements iPartsConst {

    public enum SCREEN_SIZES {MAXIMIZE, SCALE_FROM_PARENT, DONT_CARE}

    public final static String SEPERATOR_ALIAS = "menuSeparator";

    protected String searchTable;
    protected boolean exactSearch = false;
    protected boolean caseInsensitive = true;
    protected int minCharForSearch = 1;
    protected int maxResults = -1;
    protected String linkVarColumn = ExtNavBaseConsts.FIELD_U_MODNO;
    protected String linkVerColumn = ExtNavBaseConsts.FIELD_U_MODVER;
    protected EtkDisplayFields searchFields;
    protected LinkedHashMap<String, Boolean> sortFields; // Map Feldname -> absteigende Sortierung
    protected EtkDisplayFields displayResultFields;
    private boolean useConfiguredColumnWidths = false;
    protected EtkDisplayFields requiredResultFields;
    protected EtkEditFields editFields;
    protected SimpleGridTableRowSorter sorter;
    protected OnChangeEvent onChangeEvent = null;
    protected OnDblClickEvent onDblClickEvent = null;
    protected OnSearchFinishedEvent onSearchFinishedEvent = null;
    protected OnStartSearchEvent onStartSearchEvent = null;
    protected OnCreateAttributesEvent onCreateEvent = null;
    protected OnEditOrViewAttributesEvent onEditOrViewEvent = null;
    protected OnChangeSearchValueEvent onChangeSearchValueEvent = null;
    protected OnEditChangeRecordEvent onEditChangeRecordEvent;
    protected boolean active;
    protected boolean isEditAllowed;
    protected boolean isModifyAllowed;
    protected boolean isDeleteAllowed;
    protected boolean isNewAllowed;
    protected boolean isSearchFieldVisible;
    protected int maxSearchControlsPerRow;
    protected volatile boolean searchIsRunning = false;
    protected volatile boolean interruptedByMaxResults = false;
    protected String titleForEdit;
    protected String titleForView;
    protected String titleForCreate;
    protected String editControlsWindowName;
    protected EditControls editControls;
    protected EditToolbarButtonMenuHelper toolbarHelper;
    protected volatile FrameworkThread searchThread = null;
    private int initialFocusSearchFieldIndex = 0;
    private boolean allEditFieldsEditableForCreation = true; // Flag, ob bei einer Neuanlage alle Felder editierbar sein sollen
    private boolean doSearchWithNewAttributesAfterCreation = true; // Flag, ob nach einer Neuanalge die Suche mit den Suchattributen des neuen Datensatzes starten soll
    private HtmlTablePageSplitMode lastSplitMode;

    private String viewerLanguage;
    private List<String> fallbackLanguages;
    private boolean carAndVanInSession = iPartsRight.checkCarAndVanInSession();
    private boolean truckAndBusInSession = iPartsRight.checkTruckAndBusInSession();

    /**
     * Erzeugt eine Instanz von SimpleMasterDataSearchResultGrid.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public SimpleMasterDataSearchResultGrid(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName,
                                            OnEditChangeRecordEvent onEditChangeRecordEvent) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.editControls = new EditControls();
        this.searchTable = tableName;
        this.isEditAllowed = true;
        this.isModifyAllowed = true;
        this.isDeleteAllowed = true;
        this.isNewAllowed = true;
        this.isSearchFieldVisible = true;
        this.maxSearchControlsPerRow = Integer.MAX_VALUE; // keine Beschränkung
        this.titleForEdit = "!!Ändern";
        this.titleForView = "!!Anzeigen";
        this.titleForCreate = "!!Anlegen";
        this.editControlsWindowName = "";
        this.onEditChangeRecordEvent = onEditChangeRecordEvent;

        viewerLanguage = getConfig().getCurrentViewerLanguage();
        fallbackLanguages = getConfig().getDataBaseFallbackLanguages();

        postCreateGui();
        endSearch();
        sorter = new SimpleGridTableRowSorter(getProject(), getTable(), displayResultFields);
    }

    /**
     * Bestimmt im Baum, ob sich die Selektion auf oder unterhalb einer Baugruppe befindet und liefert den String Wert
     * für die ID des Knoten, z.B. die Baureihe "C204"
     *
     * @param activeForm
     * @param nodeType
     * @return
     */
    public static String getIdFromTreeSelectionForType(AbstractJavaViewerForm activeForm, String nodeType) {
        String idValue = null;
        if (activeForm instanceof AbstractMechanicForm) {
            // Connector der aktive Form
            MechanicFormIConnector connector = ((AbstractMechanicForm)activeForm).getConnector();
            // Check, ob irgendwo im AssemblyPath der übergebene Knotentyp enthalten ist.
            NavigationPath assemblyPath = connector.getCurrentNavigationPath();
            if (assemblyPath != null) {
                for (PartListEntryId entryId : assemblyPath) {
                    iPartsDataAssembly assembly = (iPartsDataAssembly)EtkDataObjectFactory.createDataAssembly(connector.getProject(), entryId.getOwnerAssemblyId());
                    List<iPartsVirtualNode> virtualNodes = assembly.getVirtualNodesPath();
                    if (iPartsVirtualNode.isProductNode(virtualNodes) && nodeType.equals(iPartsProductId.TYPE)) {
                        idValue = virtualNodes.get(0).getId().getValue(1);
                    } else if (iPartsVirtualNode.isSeriesNode(virtualNodes) && nodeType.equals(iPartsSeriesId.TYPE)) {
                        idValue = virtualNodes.get(0).getId().getValue(1);
                    } else if (iPartsVirtualNode.isModelNode(virtualNodes) && nodeType.equals(iPartsModelId.TYPE)) {
                        idValue = virtualNodes.get(0).getId().getValue(1);
                    } else if (iPartsVirtualNode.isKgSaNode(virtualNodes) && nodeType.equals(iPartsSaId.TYPE)) {
                        idValue = virtualNodes.get(1).getId().getValue(2);
                    }
                }
            }
        }
        return idValue;
    }

    protected static EtkDisplayField createSearchField(EtkProject project, String tablename, String fieldname, boolean mehrSprachig, boolean isArray) {
        EtkDisplayField searchField = new EtkDisplayField(tablename, fieldname, mehrSprachig, isArray);
        searchField.setSprache(project.getDBLanguage());
        return searchField;
    }

    protected static EtkDisplayField createDisplayField(EtkProject project, String tablename, String fieldname, boolean mehrSprachig, boolean isArray) {
        EtkDisplayField displayField = new EtkDisplayField(tablename, fieldname, mehrSprachig, isArray);
        displayField.loadStandards(project.getConfig());
        return displayField;
    }

    protected static EtkEditField createEditField(EtkProject project, String tablename, String fieldname, boolean mehrSprachig) {
        EtkEditField editField = new EtkEditField(tablename, fieldname, mehrSprachig);
        return editField;
    }

    protected static EtkDisplayField addSearchField(String tableName, String fieldName, String labelText,
                                                    EtkProject project, EtkDisplayFields searchFields) {
        EtkDisplayField searchField = createSearchField(project, tableName, fieldName, false, false);
        return addField(labelText, searchFields, searchField);
    }

    protected static EtkDisplayField addDisplayField(String tableName, String fieldName, boolean multiLanguage, boolean isArray,
                                                     String labelText, EtkProject project, EtkDisplayFields displayFields) {
        EtkDisplayField displayField = createDisplayField(project, tableName, fieldName, multiLanguage, isArray);
        return addField(labelText, displayFields, displayField);
    }

    protected static EtkEditField addEditField(String tableName, String fieldName, boolean multiLanguage,
                                               String labelText, EtkProject project, EtkEditFields editFields) {
        EtkEditField editField = createEditField(project, tableName, fieldName, multiLanguage);
        if (labelText != null) {
            editField.setDefaultText(false);
            editField.setText(new EtkMultiSprache(labelText, new String[]{ TranslationHandler.getUiLanguage() }));
        }
        editFields.addFeld(editField);
        return editField;
    }

    protected static EtkDisplayField addField(String labelText, EtkDisplayFields fields, EtkDisplayField field) {
        // Jetzt erst den virtuellen Feldnamen bei vorhandenem virtualFieldContext setzen, weil ansonsten die Felddefinition
        // nicht aus der DBDatabaseDescription geladen werden kann
        if (labelText != null) {
            field.setDefaultText(false);
            field.setText(new EtkMultiSprache(labelText, new String[]{ TranslationHandler.getUiLanguage() }));
        }
        if (fields != null) {
            fields.addFeld(field);
        }
        return field;
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    protected void postCreateGui() {
        toolbarHelper = new EditToolbarButtonMenuHelper(getConnector(), mainWindow.toolbar);
        toolbarManager = toolbarHelper.getToolbarManager();
        mainWindow.buttonPanel.setButtonVisible(GuiButtonOnPanel.ButtonType.CANCEL, false);
        mainWindow.buttonPanel.setButtonModalResult(GuiButtonOnPanel.ButtonType.OK, ModalResult.CANCEL);
        mainWindow.buttonPanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK).setExecuteHotkeys(J2EEHandler.isJ2EE() ? Keys.KEY_ESC_html : Keys.KEY_ESC_swing);

        createToolbarButtons();
        getTable().setContextMenu(contextMenuTable);
        getTable().setOnTableHeaderClickedEvent(new OnTableHeaderClickedEvent() {
            @Override
            public void onTableHeaderClicked(int column) {
                if (searchIsRunning) {
                    endSearch();
                }
            }
        });
        setMultiSelect(true);

        setEditFields(null);
        enableButtons();
        mainWindow.buttonStartStopSearch.setDefaultButton(true);
    }

    public void doResizeWindow(SCREEN_SIZES kind) {
        switch (kind) {
            case MAXIMIZE:
                Dimension screenSize = FrameworkUtils.getScreenSize();
                mainWindow.setSize(screenSize.width - 20, screenSize.height - 20);
                break;
            case SCALE_FROM_PARENT:
                if (parentForm != null) {
                    int height = parentForm.getGui().getParentWindow().getHeight();
                    int width = parentForm.getGui().getParentWindow().getWidth();
                    mainWindow.setSize(width - CASCADING_WINDOW_OFFSET_WIDTH, height - CASCADING_WINDOW_OFFSET_HEIGHT);
                }
                break;
        }
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    protected GuiPanel getPanelMain() {
        return mainWindow.panelMain;
    }

    /**
     * Setzt den Namen des Fenster und der {@link EditControls} für die automatischen GUI-Tests.
     *
     * @param windowName
     */
    public void setWindowName(String windowName) {
        mainWindow.setName(windowName);
        setEditControlsWindowName(windowName + "EditControls");
    }

    public ModalResult showModal() {
        return showModal(null);
    }

    public void showNonModal() {
        addCloseNonModalWindowListener(mainWindow);
        mainWindow.showNonModal(GuiWindow.NonModalStyle.OPEN_IN_NEW_WINDOW);
    }

    public ModalResult showModal(GuiWindow parentWindow) {
        // Damit die Fenster immer einen Test-Namen haben. Normalerweise sollte dieser sinnvoll von aussen gesetzt werden,
        // als Fallback wird der in der Oberfläche angezeigte Titel vewendet
        if (mainWindow.getName().isEmpty()) {
            mainWindow.setName(mainWindow.title.getTitle());
        }
        ModalResult modalResult = mainWindow.showModal(parentWindow);
        super.close();
        return modalResult;
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }

    /**
     * Schließe den Dialog mit dem übergebenen {@link ModalResult}
     *
     * @param modalResult
     */
    protected void closeWithModalResult(ModalResult modalResult) {
        mainWindow.closeWithModalResult(modalResult);
        super.close();
    }

    @Override
    public AbstractJavaViewerFormIConnector getConnector() {
        return super.getConnector();
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public void setWindowTitle(String title) {
        mainWindow.setTitle(title);
    }

    /**
     * @return
     */
    public DBDataObjectAttributes getSelectedAttributes() {
        return getSelection();
    }

    /**
     * @return
     */
    public DBDataObjectAttributesList getSelectedAttributesList() {
        return getMultiSelection();
    }

    //=== Getter and Setter ===
    public String getSearchTable() {
        return searchTable;
    }

    public void setSearchTable(String searchTable) {
        this.searchTable = searchTable;
    }

    public EtkDisplayFields getSearchFields() {
        return searchFields;
    }

    public void setSearchFields(EtkDisplayFields searchFields) {
        this.searchFields = searchFields;
        if (searchFields != null) {
            // damit Enums keinen WildCard-Stern erhalten
            for (EtkDisplayField searchField : this.searchFields.getFields()) {
                EtkFieldType fieldType = searchField.getEtkDatabaseFieldType(getProject().getConfig());
                if ((fieldType == EtkFieldType.feEnum) || (fieldType == EtkFieldType.feSetOfEnum)) {
                    searchField.setSearchExact(true);
                }
            }
        }
        createSearchControls();
    }

    /**
     * Setzt die optionalen Felder für eine auf- oder absteigende Sortierung.
     *
     * @param sortFields Optionale Map mit {@code Feldname -> Flag für absteigende Sortierung} zur Sortierung der Suchergebnisse
     */
    public void setSortFields(LinkedHashMap<String, Boolean> sortFields) {
        this.sortFields = sortFields;
    }

    /**
     * Sortiert die Tabelle nach dem übergebenen Feld (inkl. optionaler Tabelle mit Fallback auf die Such-Tabelle) aufsteigend.
     * <br/>Darf erst nach dem Setzen der Ergebnisfelder über {@link #setDisplayResultFields(EtkDisplayFields)} aufgerufen
     * werden, damit die Sortierung angewandt werden kann.
     *
     * @param tableAndFieldName
     */
    public void setSortTableByField(String tableAndFieldName) {
        String tableName = TableAndFieldName.getTableName(tableAndFieldName);
        if (tableName.isEmpty()) {
            tableName = getSearchTable();
        }
        int fieldIndex = getDisplayResultFields().getIndexOfVisibleFeld(tableName, TableAndFieldName.getFieldName(tableAndFieldName), false);
        if (fieldIndex >= 0) {
            // sortRowsAccordingToColumn() funktioniert nicht, wenn danach Zeilen hinzugefügt werden (was hier bei der Suche
            // ja der Fall ist)
            getTable().setDefaultSortOrderAndSort(new int[]{ fieldIndex });
        } else {
            getTable().setDefaultSortOrderAndSort(new int[0]);
        }
    }

    public void showSearchFields(boolean value) {
        mainWindow.panelForContainer.setVisible(value);
        isSearchFieldVisible = value;
    }

    public void showKitLight(boolean value) {
        if (!isSearchFieldVisible) {
            mainWindow.labelSearchResult.setVisible(!value);
            mainWindow.labelResultCount.setVisible(!value);
            mainWindow.panelForContainer.setVisible(value);
        }
        mainWindow.kitLight.setVisible(value);
    }

    public boolean isExactSearch() {
        return exactSearch;
    }

    public void setExactSearch(boolean exactSearch) {
        this.exactSearch = exactSearch;
    }

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    public void setCaseInsensitive(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public String getLinkVarColumn() {
        return linkVarColumn;
    }

    public void setLinkVarColumn(String linkVarColumn) {
        this.linkVarColumn = linkVarColumn;
    }

    public String getLinkVerColumn() {
        return linkVerColumn;
    }

    public void setLinkVerColumn(String linkVerColumn) {
        this.linkVerColumn = linkVerColumn;
    }

    public EtkDisplayFields getDisplayResultFields() {
        return displayResultFields;
    }

    public void setDisplayResultFields(EtkDisplayFields displayResultFields) {
        setDisplayResultFields(displayResultFields, false);
    }

    public void setDisplayResultFields(EtkDisplayFields displayResultFields, boolean useConfiguredColumnWidths) {
        endSearch();
        this.displayResultFields = displayResultFields;
        setGridHeader(displayResultFields, useConfiguredColumnWidths);
        sorter.setDisplayResultFields(displayResultFields);
    }

    public EtkDisplayFields getRequiredResultFields() {
        return requiredResultFields;
    }

    public void setRequiredResultFields(EtkDisplayFields requiredResultFields) {
        endSearch();

        // Primärschlüsselfelder müssen unbedingt enthalten sein
        if (requiredResultFields != null) {
            EtkDatabaseTable tableDef = getProject().getConfig().getDBDescription().findTable(searchTable);
            if (tableDef != null) {
                requiredResultFields = new EtkDisplayFields(requiredResultFields);
                for (String primaryKeyField : tableDef.getPrimaryKeyFields()) {
                    if (!requiredResultFields.contains(searchTable, primaryKeyField, false)) {
                        requiredResultFields.addFeld(new EtkDisplayField(searchTable, primaryKeyField, false, false));
                    }
                }
            }
        }

        this.requiredResultFields = requiredResultFields;
    }

    public EtkEditFields getEditFields() {
        return editFields;
    }

    public void setEditFields(EtkEditFields editFields) {
        this.editFields = editFields;
        setEditAllowed(isEditAllowed);
    }

    public boolean isEditAllowed() {
        return isEditAllowed;
    }

    public void setEditAllowed(boolean editAllowed) {
        isEditAllowed = editAllowed;
        if (isEditAllowed) {
            if (editFields != null) {
                toolbarHelper.showToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW, contextMenuTable);
                if (isModifyAllowed) {
                    toolbarHelper.showToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, contextMenuTable);
                    toolbarHelper.resetToolbarButtonAndMenuTooltip(EditToolbarButtonAlias.EDIT_WORK, contextMenuTable);
                } else {
                    toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, contextMenuTable);
                }
            } else {
                toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW, contextMenuTable);
                toolbarHelper.showToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, contextMenuTable);
                toolbarHelper.changeToolbarButtonAndMenuTooltip(EditToolbarButtonAlias.EDIT_WORK, contextMenuTable, "!!Anzeigen");
            }
            if (isDeleteAllowed) {
                toolbarHelper.showToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, contextMenuTable);
            } else {
                toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, contextMenuTable);
            }
            if (isNewAllowed) {
                toolbarHelper.showToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW, contextMenuTable);
            } else {
                toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW, contextMenuTable);
            }
        } else {
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW, contextMenuTable);
            toolbarHelper.showToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, contextMenuTable);
            toolbarHelper.changeToolbarButtonAndMenuTooltip(EditToolbarButtonAlias.EDIT_WORK, contextMenuTable, "!!Anzeigen");
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, contextMenuTable);
        }
    }

    protected void hideWorkToolbarButtonAndMenu() {
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, contextMenuTable);
    }

    /**
     * Flag, ob das Bearbeiten von Datensätzen erlaubt ist oder nicht (Bearbeiten ist nur dann möglich, wenn auch {@link #isEditAllowed}
     * erlaubt ist.
     *
     * @return
     */
    public boolean isModifyAllowed() {
        return isModifyAllowed;
    }

    /**
     * Flag, ob das Bearbeiten von Datensätzen erlaubt ist oder nicht (Bearbeiten ist nur dann möglich, wenn auch {@link #isEditAllowed}
     * erlaubt ist.
     *
     * @param modifyAllowed
     */
    public void setModifyAllowed(boolean modifyAllowed) {
        isModifyAllowed = modifyAllowed;
        if (isModifyAllowed && isEditAllowed && (editFields != null)) {
            toolbarHelper.showToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, contextMenuTable);
            toolbarHelper.resetToolbarButtonAndMenuTooltip(EditToolbarButtonAlias.EDIT_WORK, contextMenuTable);
        } else {
            toolbarHelper.showToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, contextMenuTable);
            toolbarHelper.changeToolbarButtonAndMenuTooltip(EditToolbarButtonAlias.EDIT_WORK, contextMenuTable, "!!Anzeigen");
        }
    }

    /**
     * Flag, ob das Löschen von Datensätzen erlaubt ist oder nicht (Löschen ist nur dann möglich, wenn auch {@link #isEditAllowed}
     * erlaubt ist.
     *
     * @return
     */
    public boolean isDeleteAllowed() {
        return isDeleteAllowed;
    }

    /**
     * Flag, ob das Löschen von Datensätzen erlaubt ist oder nicht (Löschen ist nur dann möglich, wenn auch {@link #isEditAllowed}
     * erlaubt ist.
     *
     * @param deleteAllowed
     */
    public void setDeleteAllowed(boolean deleteAllowed) {
        isDeleteAllowed = deleteAllowed;
        if (isDeleteAllowed && isEditAllowed) {
            toolbarHelper.showToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, contextMenuTable);
        } else {
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, contextMenuTable);
        }
    }

    public boolean isNewAllowed() {
        return isNewAllowed;
    }

    /**
     * Flag, ob das Anlegen von Datensätzen erlaubt ist oder nicht (Anlegen ist nur dann möglich, wenn auch {@link #isEditAllowed}
     * erlaubt ist.
     *
     * @param newAllowed
     */
    public void setNewAllowed(boolean newAllowed) {
        isNewAllowed = newAllowed;
        if (isNewAllowed && isEditAllowed && (editFields != null)) {
            toolbarHelper.showToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW, contextMenuTable);
        } else {
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW, contextMenuTable);
        }
    }

    public void showSelectCount(boolean visible) {
        mainWindow.panelTop.setVisible(visible);
    }

    public boolean isSelectCountVisible() {
        return mainWindow.panelTop.isVisible();
    }

    public void showToolbar(boolean visible) {
        mainWindow.toolbar.setVisible(visible);
        if (visible) {
            toolbarHelper.showToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, contextMenuTable);
        } else {
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, contextMenuTable);
        }
    }

    public int getMaxSearchControlsPerRow() {
        return maxSearchControlsPerRow;
    }

    public void setMaxSearchControlsPerRow(int maxSearchControlsPerRow) {
        this.maxSearchControlsPerRow = maxSearchControlsPerRow;
        createSearchControls();
    }

    public int getMinCharForSearch() {
        return minCharForSearch;
    }

    public void setMinCharForSearch(int minCharForSearch) {
        this.minCharForSearch = minCharForSearch;
    }

    public boolean isMultiSelect() {
        return getTable().getSelectionMode() == TableSelectionMode.SELECTION_MODE_ARBITRARY_SELECTION;
    }

    public void setMultiSelect(boolean value) {
        TableSelectionMode selectionMode = TableSelectionMode.SELECTION_MODE_SINGLE_SELECTION;
        if (value) {
            selectionMode = TableSelectionMode.SELECTION_MODE_ARBITRARY_SELECTION;
        }
        getTable().setSelectionMode(selectionMode);
    }

    public String getTitleForEdit() {
        return titleForEdit;
    }

    public void setTitleForEdit(String titleForEdit) {
        this.titleForEdit = titleForEdit;
    }

    public String getTitleForView() {
        return titleForView;
    }

    public void setTitleForView(String titleForView) {
        this.titleForView = titleForView;
    }

    public String getTitleForCreate() {
        return titleForCreate;
    }

    public void setTitleForCreate(String titleForCreate) {
        this.titleForCreate = titleForCreate;
    }

    /**
     * Setzt basierend auf dem übergebenen Präfix alle Titel für Anlegen, Bearbeiten, Anzeigen und Überschrift (für die
     * Unterscheidung bzgl. Editor / Anzeige muss VORHER bereits {@link #setEditAllowed(boolean)} aufgerufen worden sein).
     *
     * @param titlePrefix
     */
    public void setTitlePrefix(String titlePrefix) {
        setTitleForCreate(TranslationHandler.translate("!!%1 anlegen", TranslationHandler.translate(titlePrefix)));
        setTitleForEdit(TranslationHandler.translate("!!%1 bearbeiten", TranslationHandler.translate(titlePrefix)));
        setTitleForView(TranslationHandler.translate("!!%1 anzeigen", TranslationHandler.translate(titlePrefix)));
        if (isEditAllowed()) {
            setTitle(TranslationHandler.translate("!!%1 Editor", TranslationHandler.translate(titlePrefix)));
        } else {
            setTitle(TranslationHandler.translate("!!%1 Anzeige", TranslationHandler.translate(titlePrefix)));
        }
    }

    public void setEditControlsWindowName(String name) {
        this.editControlsWindowName = name;
    }

    public boolean searchIsRunning() {
        return searchIsRunning;
    }

    public void setOnCreateEvent(OnCreateAttributesEvent onCreateEvent) {
        this.onCreateEvent = onCreateEvent;
    }

    public void setOnEditOrViewEvent(OnEditOrViewAttributesEvent onEditOrViewEvent) {
        this.onEditOrViewEvent = onEditOrViewEvent;
    }

    public void setOnChangeEvent(OnChangeEvent onChangeEvent) {
        this.onChangeEvent = onChangeEvent;
    }

    public void setOnDblClickEvent(OnDblClickEvent onDblClickEvent) {
        this.onDblClickEvent = onDblClickEvent;
    }

    public void setOnSearchFinishedEvent(OnSearchFinishedEvent onSearchFinishedEvent) {
        this.onSearchFinishedEvent = onSearchFinishedEvent;
    }

    public void setOnStartSearchEvent(OnStartSearchEvent onStartSearchEvent) {
        this.onStartSearchEvent = onStartSearchEvent;
    }

    public void setOnChangeSearchValueEvent(OnChangeSearchValueEvent onChangeSearchValueEvent) {
        this.onChangeSearchValueEvent = onChangeSearchValueEvent;
    }

    public int getInitialFocusSearchFieldIndex() {
        return initialFocusSearchFieldIndex;
    }

    /**
     * Set the Index of the Search Text Field that should have initial focus
     *
     * @param initialFocusSearchFieldIndex -1 to disable initial focus
     */
    public void setInitialFocusSearchFieldIndex(int initialFocusSearchFieldIndex) {
        if (this.initialFocusSearchFieldIndex != initialFocusSearchFieldIndex) {
            this.initialFocusSearchFieldIndex = initialFocusSearchFieldIndex;
            createSearchControls();
        }
    }

    //=== Getter and Setter Ende ===

    public void fillByAttributesList(List<DBDataObjectAttributes> attributesList) {
        endSearch();
        for (DBDataObjectAttributes attributes : attributesList) {
            processResultAttributes(attributes);
        }
        showNoResultsLabel(getTable().getRowCount() == 0, false);
    }

    private void createToolbarButtons() {
        ToolbarButtonMenuHelper.ToolbarMenuHolder holder;
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_NEW, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doNew(event);
            }
        });
        contextMenuTable.addChild(holder.menuItem);
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_WORK, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doEditOrView(event);
            }
        });
        contextMenuTable.addChild(holder.menuItem);
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_DELETE, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doDelete(event);
            }
        });
        contextMenuTable.addChild(holder.menuItem);

        addToolbarButtonsAndContextMenuItems(toolbarHelper, contextMenuTable);

        GuiSeparator separator = new de.docware.framework.modules.gui.controls.GuiSeparator();
        separator.setName(SEPERATOR_ALIAS);
        separator.setUserObject(SEPERATOR_ALIAS);
        contextMenuTable.addChild(separator);

        GuiMenuItem menuItemCopy = toolbarHelper.createCopyMenuForTable(getTable(), getUITranslationHandler());
        contextMenuTable.addChild(menuItemCopy);
    }

    /**
     * Wird aufgerufen, damit zusätzliche ToolbarButtons und/oder Kontextmenüeinträge hinzugefügt werden können.
     *
     * @param toolbarHelper
     * @param contextMenu
     */
    protected void addToolbarButtonsAndContextMenuItems(EditToolbarButtonMenuHelper toolbarHelper, GuiContextMenu contextMenu) {
    }

    /**
     * @return
     */
    public GuiTable getTable() {
        return mainWindow.resultGrid;
    }

    /**
     * Setzt den Tabellen Header.
     * Wenn <code>useConfiguredColumnWidths</code> true ist, dann werden die Spaltenbreiten aus der DWK verwendet.
     * Sollte dort die Breite 0 für ein sichtbares Feld konfiguriert sein, wird die Spaltenbreite wieder automatisch
     * aus dem Spalteninhalt bestimmt (als ob <code>useConfiguredColumnWidths</code> <code>false</code> wäre).
     *
     * @param resultFields
     * @param useConfiguredColumnWidths Spaltenbreite aus der Konfiguration verwenden
     */
    protected void setGridHeader(EtkDisplayFields resultFields, boolean useConfiguredColumnWidths) {
        if (resultFields != null) {
            this.useConfiguredColumnWidths = useConfiguredColumnWidths;
            int factor = GuiUtils.getTextWidth_A(new GuiLabel("").getFont());
            GuiTableHeader tableHeader = new GuiTableHeader();
            for (EtkDisplayField field : resultFields.getFields()) {
                if (field.isVisible()) {
                    String txt;
                    EtkDatabaseField dbField = getProject().getFieldDescription(field.getKey().getTableName(), field.getKey().getFieldName());
                    if (!field.isDefaultText() || dbField.isUserDefined()) {
                        txt = field.getText().getTextByNearestLanguage(viewerLanguage, fallbackLanguages);
                    } else {
                        txt = dbField.getDisplayName();
                    }
                    GuiLabel label = new GuiLabel(txt);
                    int width = -1;
                    if (useConfiguredColumnWidths) {
                        int configuredWidth = field.getWidth();
                        if (configuredWidth > 0) { // Breite 0 bedeutet dass die Breite automatisch ermittelt wird
                            width = configuredWidth * factor;
                        }
                    }
                    tableHeader.addChild(label, width);
                }
            }
            getTable().setHeader(tableHeader);
        }
    }

    protected DBDataObjectAttributes getSelection() {
        List<GuiTableRow> selectedRows = getTable().getSelectedRows();

        if (!selectedRows.isEmpty()) {
            SimpleSelectSearchResultGrid.GuiTableRowWithAttributes rowWithAttributes = (SimpleSelectSearchResultGrid.GuiTableRowWithAttributes)selectedRows.get(0);
            return rowWithAttributes.attributes;
        } else {
            return null;
        }
    }

    protected void setSelection(boolean scrollToFirstSelection, DBDataObjectAttributes... attributesToBeSelected) {
        getTable().clearSelection();
        if ((attributesToBeSelected != null) && (attributesToBeSelected.length > 0)) {
            // IDs für die auszuwählenden Attributes erzeugen
            Set<IdWithType> idsToBeSelected = new HashSet<>(attributesToBeSelected.length);
            for (DBDataObjectAttributes attributes : attributesToBeSelected) {
                IdWithType id = buildIdFromAttributes(attributes);
                if (id != null) {
                    idsToBeSelected.add(id);
                }
            }

            for (int rowIndex = 0; rowIndex < getTable().getRowCount(); rowIndex++) {
                SimpleSelectSearchResultGrid.GuiTableRowWithAttributes row = (SimpleSelectSearchResultGrid.GuiTableRowWithAttributes)getTable().getRow(rowIndex);
                IdWithType rowId = buildIdFromAttributes(row.attributes);
                if ((rowId != null) && idsToBeSelected.contains(rowId)) {
                    getTable().addRowToSelection(rowIndex);
                    if (scrollToFirstSelection && (idsToBeSelected.size() == attributesToBeSelected.length)) { // zum ersten gefundenen Eintrag scrollen
                        getTable().scrollToCell(rowIndex, 0);
                    }
                    idsToBeSelected.remove(rowId);
                }

                // alle DBDataObjectAttributes wurden bereits gefunden
                if (idsToBeSelected.isEmpty()) {
                    break;
                }
            }
        }
    }

    protected void setSelectionAfterSearch(final OnSearchFinishedEvent externalOnSearchFinishedSelection) {
        final OnSearchFinishedEvent originalOnSearchFinishedEvent = onSearchFinishedEvent;
        OnSearchFinishedEvent onSearchFinishedSelection = new OnSearchFinishedEvent() {
            @Override
            public void OnSearchFinishedEvent() {
                if (externalOnSearchFinishedSelection != null) {
                    externalOnSearchFinishedSelection.OnSearchFinishedEvent();
                }
                setOnSearchFinishedEvent(originalOnSearchFinishedEvent);
                if (originalOnSearchFinishedEvent != null) {
                    originalOnSearchFinishedEvent.OnSearchFinishedEvent();
                }
            }
        };
        setOnSearchFinishedEvent(onSearchFinishedSelection);
    }

    protected void setSelectionAfterSearch(final DBDataObjectAttributes... attributes) {
        OnSearchFinishedEvent onSearchFinishedSelection = new OnSearchFinishedEvent() {
            @Override
            public void OnSearchFinishedEvent() {
                setSelection(true, attributes);
            }
        };
        setSelectionAfterSearch(onSearchFinishedSelection);
    }

    protected DBDataObjectAttributesList getMultiSelection() {
        List<GuiTableRow> selectedRows = getTable().getSelectedRows();

        if (!selectedRows.isEmpty()) {
            DBDataObjectAttributesList result = new DBDataObjectAttributesList();
            for (GuiTableRow selRow : selectedRows) {
                SimpleSelectSearchResultGrid.GuiTableRowWithAttributes rowWithAttributes = (SimpleSelectSearchResultGrid.GuiTableRowWithAttributes)selRow;
                result.add(rowWithAttributes.attributes);
            }
            return result;
        }
        return null;
    }


    /**
     *
     */
    protected void enableButtons() {
        int selectionRowCount = getTable().getSelectedRows().size();
        boolean singleSelectionEnabled = selectionRowCount == 1;
        boolean multiSelectionEnabled = selectionRowCount > 0;

        if (isEditAllowed) {
            toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW, contextMenuTable, true);
            toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, contextMenuTable, singleSelectionEnabled);
            toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, contextMenuTable, multiSelectionEnabled);

            if ((displayResultFields != null) && (searchFields != null)) {
                doControlChange(null);
            } else {
                mainWindow.buttonStartStopSearch.setEnabled(false);
            }
        } else {
            toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, contextMenuTable, singleSelectionEnabled);
        }
    }

    protected void endSearch() {
        FrameworkThread searchThreadLocal = searchThread;
        if (searchThreadLocal != null) {
            searchThreadLocal.cancel(); // cancel() wartet bis zur Beendigung vom Thread
            searchThread = null;
        }
        mainWindow.buttonPanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, true);
        enableButtons();
        mainWindow.buttonStartStopSearch.setText("!!Suche starten");

        setSearchIsRunning(false);

        if (mainWindow.kitLight.isVisible()) {
            if (getTable().getRowCount() == 0) {
                showNoResultsLabel(true, false);
            }
            mainWindow.kitLight.setVisible(false);
        }
    }

    /**
     * Eine explizite Suche z.B. mittels {@link EtkDataObjectList} ausführen anstatt {@link #buildQuery()} zu verwenden.
     *
     * @return
     */
    protected boolean executeExplicitSearch() {
        return false;
    }

    /**
     * Felder für das Select Statement zusammen sammeln.
     * Dazu werden alle nicht-virtuellen Displayfields aus der searchTable plus zusätzliche requiredfields zusammen gemischt
     *
     * @return
     */
    protected EtkDisplayFields getSelectFields() {
        // Nur solche Ergebnisfelder übernehmen, die sich auch in der searchTable befinden
        EtkDisplayFields displayResultFieldsForQuery = new EtkDisplayFields();
        for (EtkDisplayField displayResultField : displayResultFields.getFields()) {
            if (displayResultField.getKey().getTableName().equals(searchTable) && !VirtualFieldsUtils.isVirtualField(displayResultField.getKey().getFieldName())) {
                displayResultFieldsForQuery.addFeld(displayResultField);
            }
        }

        // Zusätzliche benötigte Ergebnisfelder übernehmen, die sich auch in der searchTable befinden
        if (requiredResultFields != null) {
            for (EtkDisplayField requiredResultField : requiredResultFields.getFields()) {
                if (requiredResultField.getKey().getTableName().equals(searchTable)) {
                    if (!displayResultFieldsForQuery.contains(requiredResultField.getKey().getName(), false)
                        && !VirtualFieldsUtils.isVirtualField(requiredResultField.getKey().getFieldName())) {
                        displayResultFieldsForQuery.addFeld(requiredResultField);
                    }
                }
            }
        }
        return displayResultFieldsForQuery;
    }

    /**
     * Ermittelt die Suchfelder inkl. der Suchwerte
     *
     * @param filterEmptyValues     wenn <code>true</code> werden Felder deren searchValue leer ist nicht zurückgeliefert
     * @param applyWildcardSettings wenn <code>true</code> werden die searchValue inkl. Wildcards je nach Setting zurückgeliefert
     * @return Map von Suchfeld auf Suchwert
     */
    protected Map<EtkDisplayField, String> getSearchFieldsAndValuesForQuery(boolean filterEmptyValues, boolean applyWildcardSettings) {
        WildCardSettings wildCardSettings = null;
        if (applyWildcardSettings) {
            wildCardSettings = new WildCardSettings();
            wildCardSettings.load(getProject().getConfig());
        }

        Map<EtkDisplayField, String> searchFieldsAndValues = new LinkedHashMap<>();
        // Nur solche Suchfelder (und dazugehörigen Suchwerte) übernehmen, die sich auch in der searchTable befinden
        List<String> searchValues = getSearchValues();
        int index = 0;
        for (EtkDisplayField searchField : searchFields.getFields()) {
            if (!VirtualFieldsUtils.isVirtualField(searchField.getKey().getFieldName())
                && searchField.getKey().getTableName().equals(searchTable)) {
                String searchValue = searchValues.get(index);
                if (!filterEmptyValues || !StrUtils.isEmpty(searchValue)) {

                    // Länge von searchValue auf die maximale Feldlänge beschränken, um DB-Fehler zu vermeiden
                    searchValue = getProject().getConfig().getDBDescription().cutValueIfLongerThanFieldLength(searchValue,
                                                                                                              searchField.getKey().getTableName(),
                                                                                                              searchField.getKey().getFieldName());

                    if (applyWildcardSettings && !searchField.isSearchExact()) {
                        searchValue = wildCardSettings.makeWildCard(searchValue);
                    }
                    searchFieldsAndValues.put(searchField, searchValue);

                }
            }
            index++;
        }
        return searchFieldsAndValues;
    }

    /**
     * Erzeugt eine Suchabfrage, das nur die Ergebnisfelder für die Suchtabelle {@link #searchTable} berücksichtigt.
     *
     * @return
     */
    protected EtkSqlCommonDbSelect buildQuery() {
        EtkDisplayFields displayResultFieldsForQuery = getSelectFields();

        EtkSqlCommonDbSelect sqlSelect = new EtkSqlCommonDbSelect(getProject(), searchTable);
        sqlSelect.setUpperCanBeApplied(caseInsensitive);

        Map<EtkDisplayField, String> searchFieldsAndValuesForQuery = getSearchFieldsAndValuesForQuery(false, false);
        EtkDisplayFields searchFieldsForQuery = new EtkDisplayFields();
        List<String> searchValuesForQuery = new ArrayList<>();

        for (Map.Entry<EtkDisplayField, String> entry : searchFieldsAndValuesForQuery.entrySet()) {
            searchFieldsForQuery.addFeld(entry.getKey());
            searchValuesForQuery.add(entry.getValue());
        }

        sqlSelect.buildSelect(searchTable, searchFieldsForQuery, searchValuesForQuery, displayResultFieldsForQuery, sortFields);
        if ((maxResults > 0) && useMaxResultsForSQLHitLimit()) {
            sqlSelect.setHitLimit(maxResults + 1); // + 1, damit angezeigt wird, dass es mehr Treffer gäbe
        }
        return sqlSelect;
    }

    /**
     * Soll ein evtl. gesetztes {@link #maxResults} auch im SQL-Select als HitLimit verwendet werden? Bei nachträglicher
     * Filterung der Suchergebnisse z.B. in {@link #doValidateAttributes(DBDataObjectAttributes)} muss hier {@code false}
     * zurückgegeben werden, da es ansonsten passieren kann, dass effektiv nicht {@link #maxResults} Suchergebnisse auch
     * tatsächlich angezeigt werden und auch die Information fehlt, dass es eigentlich noch mehr Suchergebnisse geben würde.
     *
     * @return
     */
    protected boolean useMaxResultsForSQLHitLimit() {
        return true;
    }

    /**
     * @param sqlSelect
     * @return
     */
    protected AbstractSearchQueryCancelable setQuery(EtkSqlCommonDbSelect sqlSelect) throws CanceledException {
        return new EtkDbsSearchQueryCancelable(sqlSelect.createAbfrageCancelable());
    }

    protected synchronized void startSearch() {
        startSearch(false);
    }

    protected synchronized void startSearch(boolean withSaveFilterSettings) {
        if (!mainWindow.buttonStartStopSearch.isEnabled()) {
            return;
        }

        endSearch();
        if (withSaveFilterSettings && (this instanceof SimpleMasterDataSearchFilterGrid)) {
            ((SimpleMasterDataSearchFilterGrid)this).setFilterAndSortSettings();
        }
        clearGrid();

        showNoResultsLabel(false, false);
        internalStartSearch();
    }

    protected void prepareGuiForSearch() {
        mainWindow.kitLight.setVisible(true);
        mainWindow.buttonPanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, false);
//        buttonNew.setEnabled(false);
        mainWindow.buttonStartStopSearch.setText("!!Suche abbrechen");
        mainWindow.labelResultCount.setText("");
        interruptedByMaxResults = false;
    }

    protected synchronized void internalStartSearch() {
        FrameworkThread searchThreadLocal = searchThread;
        if (searchThreadLocal != null) {
            searchThreadLocal.cancel(); // cancel() wartet bis zur Beendigung vom Thread
            searchThread = null;
        }

        prepareGuiForSearch();

        // Doku siehe https://confluence.docware.de/confluence/display/DR/Threads+und+Synchronisierung
        searchThread = Session.startChildThreadInSession(thread -> {
            List<String> fieldNames = new ArrayList<>();
            for (EtkDisplayField field : displayResultFields.getFields()) {
                String fieldName = field.getKey().getFieldName();
                if (!VirtualFieldsUtils.isVirtualField(fieldName)) {
                    fieldNames.add(fieldName);
                }
            }
            if (requiredResultFields != null) {
                for (EtkDisplayField requiredField : requiredResultFields.getFields()) {
                    String requiredFieldName = requiredField.getKey().getFieldName();
                    if (!fieldNames.contains(requiredFieldName) && !VirtualFieldsUtils.isVirtualField(requiredFieldName)) {
                        fieldNames.add(requiredFieldName);
                    }
                }
            }
            setSearchIsRunning(true);
            AbstractSearchQueryCancelable searchQueryForThread = null;
            try {
                // Suche über explizite Such-Methode (z.B. mittels EtkDataObjectList.searchSortAndFillWithJoin()) vorhanden?
                if (!executeExplicitSearch()) {
                    // Falls nicht, dann die automatisch erzeugte Suche mit zusammengebauter SQL Query verwenden
                    EtkSqlCommonDbSelect sqlSelect = buildQuery();
                    searchQueryForThread = setQuery(sqlSelect); // hier wird die Query abgeschickt

                    int processedRecords = 0;
                    while (!thread.wasCanceled() && searchQueryForThread.next()) {
                        if (checkMaxResultsExceeded(processedRecords)) {
                            break;
                        }

                        final DBDataObjectAttributes attributes = searchQueryForThread.loadAttributes(getProject(), fieldNames);
                        processedRecords += addFoundAttributes(attributes);
                    }
                }
            } catch (Exception e) {
                if (e instanceof CanceledException) {
                    // Query wurde unterbrochen, ist in der Suche ok
                } else {
                    // Es gab eine andere Exception -> Loggen und Suche beenden
                    Session.invokeThreadSafeInSessionWithChildThread(() -> {
                        endSearch();
                        Logger.getLogger().handleRuntimeException(e);
                    });

                }
            } finally {
                if (searchQueryForThread != null) {
                    searchQueryForThread.closeQuery();
                }
                searchThread = null;
            }

            Session.invokeThreadSafeInSession(() -> endSearch());
        });
    }

    /**
     * Überprüft, ob die maximale Anzahl an Ergebnissen überschritten ist, wenn noch ein Ergebnis dazukommen würde.
     *
     * @param processedRecords
     * @return {@code true} falls die maximale Anzahl an Ergebnissen überschritten ist, wenn noch ein Ergebnis dazukommen würde
     */
    protected boolean checkMaxResultsExceeded(int processedRecords) {
        if (interruptedByMaxResults) {
            return true;
        }

        if ((maxResults > 0) && (processedRecords >= maxResults)) {
            interruptedByMaxResults = true;
            Session.invokeThreadSafeInSession(() -> showResultCount());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Fügt die gefundenen {@link DBDataObjectAttributes} zum Suchergebnis hinzu.
     *
     * @param attributes
     * @return Anzahl gefundener Datensätze
     */
    protected int addFoundAttributes(DBDataObjectAttributes attributes) {
        if (attributes != null) {
            // Ab hier GUI und wieder ThreadSafe
            // Doku siehe https://confluence.docware.de/confluence/display/DR/Threads+und+Synchronisierung
            VarParam<Integer> foundAttributesCount = new VarParam<>(0);
            Session.invokeThreadSafeInSession(() -> foundAttributesCount.setValue(processResultAttributes(attributes)));

            return foundAttributesCount.getValue();
        } else {
            return 0;
        }
    }

    /**
     * Fügt die gefundenen {@link DBDataObject}s der übergebenen {@code dataObjectList} zum Suchergebnis hinzu.
     *
     * @param dataObjectList
     */
    protected void addFoundDataObjectList(final DBDataObjectList<? extends DBDataObject> dataObjectList) {
        // Ergebnisse der dataObjectList unter Berücksichtigung der maximalen Anzahl an Ergebnissen hinzufügen
        Session.invokeThreadSafeInSession(() -> {
            int processedRecords = 0;
            for (DBDataObject dataObject : dataObjectList) {
                if (checkMaxResultsExceeded(processedRecords)) {
                    return;
                }

                processedRecords += processResultAttributes(dataObject.getAttributes());
            }
        });
    }

    private void setSearchIsRunning(boolean value) {
        if (value) {
            if (!searchIsRunning) {
                searchIsRunning = true;
                getTable().setSortEnabled(false);
                if (onStartSearchEvent != null) {
                    // Ab hier GUI und wieder ThreadSafe
                    // Doku siehe https://confluence.docware.de/confluence/display/DR/Threads+und+Synchronisierung
                    Session.invokeThreadSafeInSession(() -> onStartSearchEvent.onStartSearch());
                }
            }
        } else {
            if (searchIsRunning) {
                searchIsRunning = false;
                getTable().setSortEnabled(true);
                if (onSearchFinishedEvent != null) {
                    // Ab hier GUI und wieder ThreadSafe
                    // Doku siehe https://confluence.docware.de/confluence/display/DR/Threads+und+Synchronisierung
                    Session.invokeThreadSafeInSession(() -> onSearchFinishedEvent.OnSearchFinishedEvent());
                }
            }
        }
    }

    protected void setLabelNotFoundText(String text) {
        if (StrUtils.isValid(text)) {
            mainWindow.labelNotFound.setText(text);
        } else {
            mainWindow.labelNotFound.setText("!!Die Suche ist abgeschlossen. Es liegen keine Ergebnisse vor.");
        }
    }

    protected void clearGrid() {
        getTable().removeRows();
        setGridHeader(displayResultFields, useConfiguredColumnWidths);
        showNoResultsLabel(false, false);
        enableButtons();
    }

    public void doClearGrid() {
        clearGrid();
    }

    /**
     * Erzeugt eine Zeile mit den übergebenen {@link DBDataObjectAttributes}.
     * Diese Methode muss überschrieben werden, wenn neben der Suchtabelle {@link #searchTable} auch noch weitere
     * Attribute von anderen Tabellen angezeigt werden sollen.
     *
     * @param attributes
     * @return Bei {@code null} wird keine Zeile zur Ergebnistabelle hinzugefügt.
     */
    protected SimpleSelectSearchResultGrid.GuiTableRowWithAttributes createRow(DBDataObjectAttributes attributes) {
        SimpleSelectSearchResultGrid.GuiTableRowWithAttributes row = new SimpleSelectSearchResultGrid.GuiTableRowWithAttributes();
        row.attributes = attributes;

        for (EtkDisplayField field : displayResultFields.getFields()) {
            if (field.isVisible()) {
                String tableName = field.getKey().getTableName();
                String fieldName = field.getKey().getFieldName();
                String value;
                DBDataObjectAttribute attribute = attributes.getField(fieldName, false);
                if (attribute != null) {
                    value = getVisualValueOfFieldValue(tableName, fieldName, attribute, field.isMultiLanguage());
                } else {
                    if (VirtualFieldsUtils.isVirtualField(fieldName)) {
                        value = getValueForVirtualField(field, attributes);
                    } else {
                        value = TranslationHandler.translate("!!Feld nicht gefunden: %1", field.getKey().getName());
                    }

                    // Fehlendes Attribut hinzufügen, damit es z.B. beim Sortieren und Filtern nicht kracht
                    attributes.addField(fieldName, value, DBActionOrigin.FROM_DB);
                }
                AbstractGuiControl control = createCellContent(tableName, fieldName, attribute, value);
                row.addChild(control, () -> control.getTextRepresentation());
            }
        }
        return row;
    }

    /**
     * Liefert das Gui-Elemnt für den jeweiligen {@param value} zurück.
     * Die Parameter tableName, fieldName und attributes sind die gleichen wie bei getVisualValueOfField()
     * und können für die exclisive Erzeugung benutzt werden
     *
     * @param tableName
     * @param fieldName
     * @param attribute
     * @param value
     * @return im Normalfall GuiLabel
     */
    protected AbstractGuiControl createCellContent(String tableName, String fieldName, DBDataObjectAttribute attribute, String value) {
        return new GuiLabel(value);
    }


    /**
     * Liefert den Wert für das übergebene virtuelle Feld zurück basierend auf den übergebenen {@link DBDataObjectAttributes};
     *
     * @param virtualField
     * @param attributes
     * @return
     */
    protected String getValueForVirtualField(EtkDisplayField virtualField, DBDataObjectAttributes attributes) {
        return "";
    }

    protected void setAttributeToMultiLang(DBDataObjectAttribute attrib, String tableName) {
        if (attrib == null) {
            return;
        }
        if (attrib.getType() == DBDataObjectAttribute.TYPE.MULTI_LANGUAGE) {
            if (attrib.getValue() == null) {
                DBExtendedDataTypeProvider tempLanguageProvider = EtkDataObject.getTempExtendedDataTypeProvider(getProject(), tableName);
                attrib.getAsMultiLanguage(tempLanguageProvider, true);
            }
            return;
        }
        attrib.setValueAsMultiLanguage(new EtkMultiSprache(), DBActionOrigin.FROM_DB);
        EtkMultiSprache multi = getProject().getDbLayer().loadMultiLanguageAttribute(attrib, tableName);

        // Dieser Umweg muss gegangen werden, da sonst nicht alle bereits geänderten aber noch nicht in der DB abgespeicherten
        // Texte übernommen werden
        for (Map.Entry<String, String> languageText : multi.getLanguagesAndTexts().entrySet()) {
            String lang = languageText.getKey();
            // nur Text aus DB setzen, wenn nicht schon ein Text vorhanden ist
            attrib.setPreloadValueForMultiLanguage(lang, languageText.getValue(), true);
        }
        attrib.setMultiLanguageCompleteLoaded(true);
    }

    /**
     * @param tableName
     * @param fieldName
     * @param fieldValue
     * @param isMultiLanguage
     * @return
     */
    protected String getVisualValueOfFieldValue(String tableName, String fieldName, DBDataObjectAttribute fieldValue, boolean isMultiLanguage) {
        if (isMultiLanguage) {
            setAttributeToMultiLang(fieldValue, tableName);
        }
        return getVisObject().asHtml(tableName, fieldName, fieldValue, getProject().getDBLanguage(), true).getStringResult();
    }

    /**
     * @param attributes
     * @return
     */
    protected SimpleSelectSearchResultGrid.GuiTableRowWithAttributes addAttributesToGrid(DBDataObjectAttributes attributes) {
        SimpleSelectSearchResultGrid.GuiTableRowWithAttributes row = createRow(attributes);
        if (row != null) {
            getTable().addRow(row);
        }

        return row;
    }

    public void addAttributesListToGrid(DBDataObjectAttributesList attributesList) {
        for (DBDataObjectAttributes attributes : attributesList) {
            addAttributesToGrid(attributes);
        }
    }

    /**
     * Liefert alle DBDataObjectAttributes aus der Table - nur sichtbare!
     * Ergebnis ist unterschiedlich, falls Sortierung/Filterung gesetzt ist!
     *
     * @return
     */
    public DBDataObjectAttributesList getAttributesListFromTable() {
        DBDataObjectAttributesList attributesList = new DBDataObjectAttributesList();
        for (int rowNo = 0; rowNo < getTable().getRowCount(); rowNo++) {
            GuiTableRow row = getTable().getRow(rowNo);
            if (row instanceof SimpleSelectSearchResultGrid.GuiTableRowWithAttributes) {
                attributesList.add(((SimpleSelectSearchResultGrid.GuiTableRowWithAttributes)row).attributes);
            }
        }
        return attributesList;
    }

    /**
     * @return
     */
    protected boolean doValidateAttributes(DBDataObjectAttributes attributes) {
        return true;
    }

    /**
     * @param attributes
     * @return
     */
    protected int processResultAttributes(DBDataObjectAttributes attributes) {
        if (doValidateAttributes(attributes)) {
            if (addAttributesToGrid(attributes) != null) {
                showNoResultsLabel(false, false);
                showResultCount();
                return 1;
            }
        }
        return 0;
    }

    protected void showNoResultsLabel(boolean showNoResults, boolean showMinChar) {
        if (showNoResults || showMinChar) {
            GuiLabel label = showNoResults ? mainWindow.labelNotFound : mainWindow.labelMinChar;
            lastSplitMode = mainWindow.resultGrid.getHtmlTablePageSplitMode();
            mainWindow.resultGrid.setHtmlTablePageSplitMode(HtmlTablePageSplitMode.NO_SPLIT);

            ConstraintsGridBag constraints = (ConstraintsGridBag)mainWindow.scrollPane.getConstraints();
            constraints.setWeightx(0);
            constraints.setWeighty(0);
            mainWindow.scrollPane.setConstraints(constraints);

            constraints = (ConstraintsGridBag)label.getConstraints();
            constraints.setWeightx(1);
            constraints.setWeighty(1);
            label.setConstraints(constraints);
            mainWindow.panelGrid.addChild(label);
            label.setVisible(true);
            mainWindow.labelResultCount.setText("");
        } else if (mainWindow.labelNotFound.isVisible() || mainWindow.labelMinChar.isVisible()) {
            mainWindow.labelNotFound.removeFromParent();
            mainWindow.labelMinChar.removeFromParent();

            mainWindow.labelNotFound.setVisible(false);
            mainWindow.labelMinChar.setVisible(false);
            // da beide Komponenten Weight=1 haben, nicht im GuiDesigner setzbar
            ConstraintsGridBag constraints = (ConstraintsGridBag)mainWindow.scrollPane.getConstraints();
            constraints.setWeightx(1);
            constraints.setWeighty(1);
            mainWindow.scrollPane.setConstraints(constraints);
            mainWindow.resultGrid.setHtmlTablePageSplitMode(lastSplitMode);
        }
    }

    public void showHitCount(int count) {
        String str;
        if (interruptedByMaxResults) {
            str = "!!Mehr als %d Datensätze gefunden";
        } else {
            if (count == 1) {
                str = "!!%d Datensatz gefunden";
            } else {
                str = "!!%d Datensätze gefunden";
            }
        }
        str = "(" + String.format(TranslationHandler.translate(str), count) + ")";
        mainWindow.labelResultCount.setText(str);
    }

    protected void showResultCount() {
        showHitCount(getTable().getRowCount());
    }

    private void createSearchControls() {
        int gridY = 0;
        int gridX = 0;
        int index = 0;
        mainWindow.panelContainer.removeAllChildren();
        mainWindow.panelContainer.setLayout(new LayoutGridBag());
        EventListener listener = new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                doControlChange(event);
            }
        };

        if (searchFields == null) {
            return;
        }

        int maxSearchControlsRowWidth = mainWindow.getWidth() - mainWindow.buttonStartStopSearch.getPreferredWidth() - 60; // -60px für Fensterrand und Insets
        int searchControlsRowWidth = 0;
        int fillerGridX = 0;
        for (int lfdNr = 0; lfdNr < searchFields.size(); lfdNr++) {
            EtkDisplayField field = searchFields.getFeld(lfdNr);
            String labelText = field.isDefaultText() ? null : field.getText().getTextByNearestLanguage(viewerLanguage, fallbackLanguages);
            EditControl ctrl = editControls.createForSearch(null, getProject(), field.getKey().getTableName(), field.getKey().getFieldName(), getProject().getDBLanguage(),
                                                            getProject().getViewerLanguage(), "", labelText, index);
            modifySearchControl(field, ctrl);
            ctrl.getEditControl().getControl().setMinimumWidth(200);
            ctrl.getEditControl().getControl().addEventListener(listener);

            // Überprüfung, ob das neue Suchfeld (mit Label) noch in die Zeile passt
            int newSearchControlWidth = ctrl.getLabel().getPreferredWidth() + ctrl.getEditControl().getControl().getPreferredWidth() + 8;
            if ((gridX / 2 >= maxSearchControlsPerRow) || (searchControlsRowWidth + newSearchControlWidth > maxSearchControlsRowWidth)) {
                searchControlsRowWidth = 0;
                gridX = 0;
                gridY++;
            }

            ctrl.getLabel().setConstraints(createLabelConstraints(gridX, gridY));
            ctrl.getEditControl().getControl().setConstraints(createValueConstraints(gridX + 1, gridY));
            addEditFieldChild(ctrl.getLabel());
            addEditFieldChild(ctrl.getEditControl().getControl());

            if (lfdNr == initialFocusSearchFieldIndex) {
                ctrl.getEditControl().getControl().requestFocus();
            }

            searchControlsRowWidth += newSearchControlWidth;
            gridX += 2;
            fillerGridX = Math.max(fillerGridX, gridX + 1);

            index++;
        }
        GuiLabel labelFiller = new GuiLabel();
        labelFiller.setName("labelFiller");
        labelFiller.setMinimumWidth(0);
        labelFiller.setMinimumHeight(0);
        ConstraintsGridBag labelFillerConstraints = new ConstraintsGridBag(fillerGridX, 0, 1, 1, 100.0, 0.0,
                                                                           ConstraintsGridBag.ANCHOR_CENTER, ConstraintsGridBag.FILL_NONE,
                                                                           0, 0, 0, 0);
        labelFiller.setConstraints(labelFillerConstraints);
        addEditFieldChild(labelFiller);
    }

    protected void setButtonStartStopVisible(boolean visible) {
        mainWindow.buttonStartStopSearch.setVisible(visible);
    }

    protected void setSearchFieldWithScrollpane() {
        GuiPanel searchPanel = mainWindow.panelContainer;
        mainWindow.panelContainer.removeFromParent();

        GuiPanel helpPanel = new GuiPanel();
        helpPanel.setName("helpPanel");
        helpPanel.__internal_setGenerationDpi(96);
        helpPanel.registerTranslationHandler(getUITranslationHandler());
        helpPanel.setScaleForResolution(true);
        helpPanel.setMinimumWidth(10);
        helpPanel.setMinimumHeight(10);
        LayoutGridBag panelContainerLayout = new LayoutGridBag();
        panelContainerLayout.setCentered(false);
        helpPanel.setLayout(panelContainerLayout);
        helpPanel.setMinimumHeight(searchPanel.getPreferredFrameHeight() + 16);
        mainWindow.panelForContainer.addChildBorderCenter(helpPanel);
        GuiScrollPane extraScrollPane = new GuiScrollPane();
        extraScrollPane.addChild(searchPanel);
        ConstraintsGridBag scrollPaneConstraints = new ConstraintsGridBag(0, 0, 1, 1, 100.0, 100.0, "c", "b", 0, 0, 0, 0);
        extraScrollPane.setConstraints(scrollPaneConstraints);
        helpPanel.addChild(extraScrollPane);
        getGui().setMinimumHeight(500);
        EditControl eCtrl = getEditControlFeldByIndex(0);
        if (eCtrl != null) {
            getGui().setMinimumWidth(eCtrl.getLabel().getPreferredWidth() + eCtrl.getEditControl().getControl().getPreferredWidth() * 2);
        }
    }

    protected void modifySearchControl(EtkDisplayField searchField, EditControl ctrl) {
        // nichts zu tun
    }

    protected List<String> getSearchValues() {
        List<String> liste = new DwList<String>();
        for (int lfdNr = 0; lfdNr < searchFields.size(); lfdNr++) {
            EditControlFactory ctrl = editControls.getControlByFeldIndex(lfdNr).getEditControl();
            String value = ctrl.getText();
            liste.add(value);
        }
        return liste;
    }

    public void setSearchValues(DBDataObjectAttributes searchAttributes) {
        setSearchValues(searchAttributes, true);
    }

    public void setSearchValues(DBDataObjectAttributes searchAttributes, boolean withAutoSearch) {
        if ((searchFields != null) && (searchAttributes != null)) {
            endSearch();
            boolean doStartSearch = false;
            for (int lfdNr = 0; lfdNr < searchFields.size(); lfdNr++) {
                DBDataObjectAttribute attribute = searchAttributes.getField(searchFields.getFeld(lfdNr).getKey().getFieldName(), false);
                if (attribute != null) {
                    EditControlFactory ctrl = editControls.getControlByFeldIndex(lfdNr).getEditControl();
                    ctrl.setText(attribute.getAsString());
                    doStartSearch = true;
                }
            }
            if (doStartSearch && withAutoSearch) {
                startSearch();
            }
        }
    }

    /**
     * Liefert den eingegebenen Suchwert für das gewünschte Feld zurück.
     *
     * @param tableName
     * @param fieldName
     * @param usage
     * @return {@code null} falls es für das gewünschte Feld keinen Suchwert gibt
     */
    protected String getSearchValue(String tableName, String fieldName, boolean usage) {
        int index = searchFields.getIndexOfFeld(tableName, fieldName, usage);
        if ((index >= 0) && (editControls.size() > index)) {
            EditControlFactory ctrl = editControls.getControlByFeldIndex(index).getEditControl();
            return ctrl.getText();
        }

        return null;
    }

    protected void addEditFieldChild(AbstractGuiControl child) {
        mainWindow.panelContainer.addChild(child);
    }

    protected ConstraintsGridBag createLabelConstraints(int gridx, int gridy) {
        return new ConstraintsGridBag(gridx, gridy, 1, 1, 0.0, 0.0, ConstraintsGridBag.ANCHOR_EAST, ConstraintsGridBag.FILL_NONE, 4, 4, 4, 4);
    }

    protected ConstraintsGridBag createValueConstraints(int gridx, int gridy) {
        return new ConstraintsGridBag(gridx, gridy, 1, 1, 0.0, 0.0, ConstraintsGridBag.ANCHOR_CENTER, ConstraintsGridBag.FILL_NONE, 4, 4, 4, 4);
    }

    protected boolean checkControlChange() {
        boolean isEnabled = false;
        for (int lfdNr = 0; lfdNr < editControls.size(); lfdNr++) {
            EditControlFactory ctrl = editControls.getControlByFeldIndex(lfdNr).getEditControl();
            if (!ctrl.getText().isEmpty()) {
                isEnabled = true;
                break;
            }
        }
        return isEnabled;
    }

    private void doControlChange(Event event) {
        mainWindow.buttonStartStopSearch.setEnabled(checkControlChange());
    }

    private void buttonCancelActionPerformed(Event event) {
        if (searchIsRunning) {
            endSearch();
        } else {
            startSearch();
        }

    }

    protected void onTableSelectionChanged(Event event) {
        enableButtons();
    }

    private void onCancelButtonClick(Event event) {
        close();
    }

    private void onOKButtonClick(Event event) {
        endSearch();
        close();
    }

    protected void doNew(Event event) {
        endSearch();
        EtkDatabaseTable tableDef = getProject().getConfig().getDBDescription().findTable(searchTable);
        if (tableDef == null) {
            return;
        }
        List<String> pkFields = tableDef.getPrimaryKeyFields();

        String[] emptyPkValues = new String[pkFields.size()];
        Arrays.fill(emptyPkValues, "");
        IdWithType id = new IdWithType("xx", emptyPkValues);

        // Beim Neu anlegen sind alle Felder editierbar => deswegen Kopie
        EtkEditFields editNewFields = new EtkEditFields();
        editNewFields.assign(editFields);
        // Nur wenn es gewünscht ist, alle Edit-Felder bei der Erzeugung editierbar setzen
        if (allEditFieldsEditableForCreation) {
            for (EtkEditField field : editNewFields.getFields()) {
                field.setEditierbar(true);
            }
        }

        DBDataObjectAttributes initialAttributes = null;
        if (onCreateEvent != null) {
            initialAttributes = onCreateEvent.onCreateAttributesEvent();
        }

        EditUserControlForCreate eCtrl = createUserControlForCreate(getConnector(), this, searchTable, id, initialAttributes,
                                                                    editNewFields);
        eCtrl.setTitle(titleForCreate);
        eCtrl.setWindowName(editControlsWindowName);
        if (eCtrl.showModal() == ModalResult.OK) {
            if (onEditChangeRecordEvent != null) {
                id = buildIdFromAttributes(eCtrl.getAttributes());
                if ((id != null) && onEditChangeRecordEvent.onEditCreateRecordEvent(getConnector(), searchTable, id, eCtrl.getAttributes())) {
                    setSelectionAfterSearch(eCtrl.getAttributes());
                    // Check, ob der neue Datensatz als Such-Objekt verwendet werden soll
                    if (doSearchWithNewAttributesAfterCreation) {
                        setSearchValues(eCtrl.getAttributes());
                    } else {
                        startSearch();
                    }
                }
            }
        }
    }

    /**
     * Einsprungpunkt um eigene EditUserControls zu verwenden
     *
     * @param connector
     * @param parentForm
     * @param searchTable
     * @param id
     * @param initialAttributes
     * @param editNewFields
     * @return
     */
    protected EditUserControlForCreate createUserControlForCreate(AbstractJavaViewerFormIConnector connector,
                                                                  AbstractJavaViewerForm parentForm,
                                                                  String searchTable, IdWithType id,
                                                                  DBDataObjectAttributes initialAttributes, EtkEditFields editNewFields) {
        return new EditUserControlForCreate(connector, parentForm, searchTable, id, initialAttributes, editNewFields);
    }

    protected void doEditOrView(Event event) {
        endSearch();
        DBDataObjectAttributes attributes = getSelectedAttributes();
        if (attributes != null) {
            boolean editAndModifyAllowed = isEditAllowed() && isModifyAllowed();
            if (onEditOrViewEvent != null) {
                attributes = onEditOrViewEvent.onEditOrViewAttributesEvent(attributes, editAndModifyAllowed);
                if (attributes == null) {
                    return;
                }
            }

            IdWithType id = buildIdFromAttributes(attributes);
            if (id == null) {
                return;
            }
            EditUserControls eCtrl = createUserControlForEditOrView(getConnector(), this, searchTable, id, attributes, editFields);
            eCtrl.setReadOnly(!editAndModifyAllowed);
            eCtrl.setTitle(editAndModifyAllowed ? titleForEdit : titleForView);
            eCtrl.setWindowName(editControlsWindowName);
            if (eCtrl.showModal() == ModalResult.OK) {
                if (onEditChangeRecordEvent != null) {
                    if (onEditChangeRecordEvent.onEditModifyRecordEvent(getConnector(), searchTable, id, eCtrl.getAttributes())) {
                        // Suche nochmals starten als Refresh für Table
                        setSelectionAfterSearch(eCtrl.getAttributes());
                        startSearch(true);
                    }
                }
            }
        }
    }

    /**
     * Einsprungpunkt um eigene EditUserControls zu verwenden
     *
     * @param connector
     * @param parentForm
     * @param searchTable
     * @param id
     * @param attributes
     * @param editFields
     * @return
     */
    protected EditUserControls createUserControlForEditOrView(AbstractJavaViewerFormIConnector connector, AbstractJavaViewerForm parentForm,
                                                              String searchTable, IdWithType id, DBDataObjectAttributes attributes, EtkEditFields editFields) {
        return new EditUserControls(connector, parentForm, searchTable, id, attributes, editFields);

    }

    protected IdWithType buildIdFromAttributes(int rowIndex) {
        GuiTableRow guiRow = getTable().getRow(rowIndex);
        if ((guiRow != null) && (guiRow instanceof SimpleSelectSearchResultGrid.GuiTableRowWithAttributes)) {
            return buildIdFromAttributes(((SimpleSelectSearchResultGrid.GuiTableRowWithAttributes)guiRow).attributes);
        }
        return null;
    }

    protected IdWithType buildIdFromAttributes(DBDataObjectAttributes attributes) {
        EtkDatabaseTable tableDef = getProject().getConfig().getDBDescription().findTable(searchTable);
        List<String> pkValues = new DwList<>();
        if (tableDef != null) {
            List<String> pkFields = tableDef.getPrimaryKeyFields();
            for (String pkField : pkFields) {
                DBDataObjectAttribute attribute = attributes.getField(pkField, false);
                if (attribute != null) {
                    pkValues.add(attribute.getAsString());
                } else {
                    pkValues.add("");
                }
            }
            return new IdWithType("xx", ArrayUtil.toArray(pkValues));
        }
        return null;
    }

    protected void doDelete(Event event) {
        endSearch();
        DBDataObjectAttributesList attributeList = getSelectedAttributesList();
        if (onEditChangeRecordEvent != null) {
            if (onEditChangeRecordEvent.onEditAskForDelete(getConnector(), searchTable, attributeList)) {
                if (onEditChangeRecordEvent.onEditDeleteRecordEvent(getConnector(), searchTable, attributeList)) {
                    // Die gelöschten Elemente aus der Selektion rausnehmen. Wichtig, damit z.B die Sichtbarkeit von Kontextmenüeinträgen
                    // nachfolgend nicht mehr für die selektierten Einträge ausgewertet wird. Diese Einträge sind ja gerade
                    // eben gelöscht worden.
                    getTable().clearSelection();
                    // Suche nochmals starten als Refresh für Table
                    startSearch();
                }
            }
        }
    }

    /**
     * Setzt das Control zum übergebenen Feldnamen auf enabled/disabled
     *
     * @param fieldname
     * @param enabled
     */
    public void setEditControlEnabled(String fieldname, boolean enabled) {
        EditControl editControl = iPartsEditUserControlsHelper.findControlByFieldname(editControls, fieldname);
        if (editControl != null) {
            editControl.getEditControl().getControl().setEnabled(enabled);
        }
    }

    public EditControl getEditControlFeldByIndex(int lfdNr) {
        return editControls.getControlByFeldIndex(lfdNr);
    }

    protected GuiButtonPanel getButtonPanel() {
        return mainWindow.buttonPanel;
    }

    protected MainWindowClass getMainWindow() {
        return mainWindow;
    }

    protected GuiPanel getSearchButtonPanel() {
        return mainWindow.panelForCancel;
    }

    protected void setToolbarButtonAndMenuEnabled(iPartsToolbarButtonAlias buttonAlias, boolean enabled) {
        toolbarHelper.enableToolbarButtonAndMenu(buttonAlias, contextMenuTable, enabled);
    }

    /**
     * Sollen bei einer Neuanlage alle Felder editierbar sein
     *
     * @param isEditable
     */
    protected void setAllEditFieldsEditableForCreation(boolean isEditable) {
        this.allEditFieldsEditableForCreation = isEditable;
    }

    /**
     * Soll nach einer Neuanlage der neue Datensatz als Such-Objekt genutzt werden
     *
     * @param doSearchWithNewAttributesAfterCreation
     */
    public void setDoSearchWithNewAttributesAfterCreation(boolean doSearchWithNewAttributesAfterCreation) {
        this.doSearchWithNewAttributesAfterCreation = doSearchWithNewAttributesAfterCreation;
    }

    protected void onTableDoubleClicked(Event event) {
        if (onDblClickEvent != null) {
            onDblClickEvent.onDblClick();
        } else {
            doEditOrView(event);
        }
    }

    protected void setSize(int width, int height) {
        mainWindow.setSize(width, height);
    }

    public boolean isCarAndVanInSession() {
        return carAndVanInSession;
    }

    public boolean isTruckAndBusInSession() {
        return truckAndBusInSession;
    }

    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    private de.docware.framework.modules.gui.controls.menu.GuiContextMenu contextMenuTable;

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelForContainer;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelContainer;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_0;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfield_0;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_1;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfield_1;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_2;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelForCancel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton buttonStartStopSearch;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSeparator separator;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelGrid;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelTop;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelSearchResult;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelResultCount;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiProgressBar kitLight;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.toolbar.GuiToolbar toolbar;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane scrollPane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.table.GuiTable resultGrid;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelNotFound;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelMinChar;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            contextMenuTable = new de.docware.framework.modules.gui.controls.menu.GuiContextMenu();
            contextMenuTable.setName("contextMenuTable");
            contextMenuTable.__internal_setGenerationDpi(96);
            contextMenuTable.registerTranslationHandler(translationHandler);
            contextMenuTable.setScaleForResolution(true);
            contextMenuTable.setMinimumWidth(10);
            contextMenuTable.setMinimumHeight(10);
            contextMenuTable.setMenuName("contextMenu");
            contextMenuTable.setParentControl(this);
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
            de.docware.framework.modules.gui.layout.LayoutGridBag panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelMain.setLayout(panelMainLayout);
            panelForContainer = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelForContainer.setName("panelForContainer");
            panelForContainer.__internal_setGenerationDpi(96);
            panelForContainer.registerTranslationHandler(translationHandler);
            panelForContainer.setScaleForResolution(true);
            panelForContainer.setMinimumWidth(10);
            panelForContainer.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelForContainerLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelForContainer.setLayout(panelForContainerLayout);
            panelContainer = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelContainer.setName("panelContainer");
            panelContainer.__internal_setGenerationDpi(96);
            panelContainer.registerTranslationHandler(translationHandler);
            panelContainer.setScaleForResolution(true);
            panelContainer.setMinimumWidth(10);
            panelContainer.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelContainerLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelContainerLayout.setCentered(false);
            panelContainer.setLayout(panelContainerLayout);
            label_0 = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_0.setName("label_0");
            label_0.__internal_setGenerationDpi(96);
            label_0.registerTranslationHandler(translationHandler);
            label_0.setScaleForResolution(true);
            label_0.setMinimumWidth(0);
            label_0.setMinimumHeight(0);
            label_0.setMaximumWidth(2147483647);
            label_0.setMaximumHeight(2147483647);
            label_0.setText("!!Suche");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_0Constraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "c", "n", 4, 4, 4, 4);
            label_0.setConstraints(label_0Constraints);
            panelContainer.addChild(label_0);
            textfield_0 = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfield_0.setName("textfield_0");
            textfield_0.__internal_setGenerationDpi(96);
            textfield_0.registerTranslationHandler(translationHandler);
            textfield_0.setScaleForResolution(true);
            textfield_0.setMinimumWidth(200);
            textfield_0.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfield_0Constraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, "c", "h", 4, 4, 4, 4);
            textfield_0.setConstraints(textfield_0Constraints);
            panelContainer.addChild(textfield_0);
            label_1 = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_1.setName("label_1");
            label_1.__internal_setGenerationDpi(96);
            label_1.registerTranslationHandler(translationHandler);
            label_1.setScaleForResolution(true);
            label_1.setMinimumWidth(0);
            label_1.setMinimumHeight(0);
            label_1.setMaximumWidth(2147483647);
            label_1.setMaximumHeight(2147483647);
            label_1.setText("Suche");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_1Constraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(2, 0, 1, 1, 0.0, 0.0, "c", "n", 4, 4, 4, 4);
            label_1.setConstraints(label_1Constraints);
            panelContainer.addChild(label_1);
            textfield_1 = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfield_1.setName("textfield_1");
            textfield_1.__internal_setGenerationDpi(96);
            textfield_1.registerTranslationHandler(translationHandler);
            textfield_1.setScaleForResolution(true);
            textfield_1.setMinimumWidth(200);
            textfield_1.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfield_1Constraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(3, 0, 1, 1, 0.0, 0.0, "c", "h", 4, 4, 4, 4);
            textfield_1.setConstraints(textfield_1Constraints);
            panelContainer.addChild(textfield_1);
            label_2 = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_2.setName("label_2");
            label_2.__internal_setGenerationDpi(96);
            label_2.registerTranslationHandler(translationHandler);
            label_2.setScaleForResolution(true);
            label_2.setMinimumWidth(10);
            label_2.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_2Constraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(4, 0, 1, 1, 100.0, 0.0, "c", "h", 0, 0, 0, 0);
            label_2.setConstraints(label_2Constraints);
            panelContainer.addChild(label_2);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelContainerConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelContainer.setConstraints(panelContainerConstraints);
            panelForContainer.addChild(panelContainer);
            panelForCancel = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelForCancel.setName("panelForCancel");
            panelForCancel.__internal_setGenerationDpi(96);
            panelForCancel.registerTranslationHandler(translationHandler);
            panelForCancel.setScaleForResolution(true);
            panelForCancel.setMinimumWidth(10);
            panelForCancel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelForCancelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelForCancel.setLayout(panelForCancelLayout);
            buttonStartStopSearch = new de.docware.framework.modules.gui.controls.GuiButton();
            buttonStartStopSearch.setName("buttonStartStopSearch");
            buttonStartStopSearch.__internal_setGenerationDpi(96);
            buttonStartStopSearch.registerTranslationHandler(translationHandler);
            buttonStartStopSearch.setScaleForResolution(true);
            buttonStartStopSearch.setMinimumWidth(130);
            buttonStartStopSearch.setMinimumHeight(10);
            buttonStartStopSearch.setMnemonicEnabled(true);
            buttonStartStopSearch.setText("!!Suche abbrechen");
            buttonStartStopSearch.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    buttonCancelActionPerformed(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag buttonStartStopSearchConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 100.0, "n", "h", 4, 4, 4, 4);
            buttonStartStopSearch.setConstraints(buttonStartStopSearchConstraints);
            panelForCancel.addChild(buttonStartStopSearch);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelForCancelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelForCancelConstraints.setPosition("east");
            panelForCancel.setConstraints(panelForCancelConstraints);
            panelForContainer.addChild(panelForCancel);
            separator = new de.docware.framework.modules.gui.controls.GuiSeparator();
            separator.setName("separator");
            separator.__internal_setGenerationDpi(96);
            separator.registerTranslationHandler(translationHandler);
            separator.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder separatorConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            separatorConstraints.setPosition("south");
            separator.setConstraints(separatorConstraints);
            panelForContainer.addChild(separator);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelForContainerConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "c", "h", 0, 0, 0, 0);
            panelForContainer.setConstraints(panelForContainerConstraints);
            panelMain.addChild(panelForContainer);
            panelGrid = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelGrid.setName("panelGrid");
            panelGrid.__internal_setGenerationDpi(96);
            panelGrid.registerTranslationHandler(translationHandler);
            panelGrid.setScaleForResolution(true);
            panelGrid.setMinimumWidth(10);
            panelGrid.setMinimumHeight(10);
            panelGrid.setBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignTableContentBackground"));
            de.docware.framework.modules.gui.layout.LayoutGridBag panelGridLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelGrid.setLayout(panelGridLayout);
            panelTop = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelTop.setName("panelTop");
            panelTop.__internal_setGenerationDpi(96);
            panelTop.registerTranslationHandler(translationHandler);
            panelTop.setScaleForResolution(true);
            panelTop.setMinimumWidth(10);
            panelTop.setMinimumHeight(39);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelTopLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelTop.setLayout(panelTopLayout);
            labelSearchResult = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelSearchResult.setName("labelSearchResult");
            labelSearchResult.__internal_setGenerationDpi(96);
            labelSearchResult.registerTranslationHandler(translationHandler);
            labelSearchResult.setScaleForResolution(true);
            labelSearchResult.setMinimumWidth(10);
            labelSearchResult.setMinimumHeight(10);
            labelSearchResult.setText("!!Suchergebnis");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelSearchResultConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "c", "n", 4, 4, 4, 4);
            labelSearchResult.setConstraints(labelSearchResultConstraints);
            panelTop.addChild(labelSearchResult);
            labelResultCount = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelResultCount.setName("labelResultCount");
            labelResultCount.__internal_setGenerationDpi(96);
            labelResultCount.registerTranslationHandler(translationHandler);
            labelResultCount.setScaleForResolution(true);
            labelResultCount.setMinimumWidth(10);
            labelResultCount.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelResultCountConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 100.0, 0.0, "c", "h", 4, 4, 4, 4);
            labelResultCount.setConstraints(labelResultCountConstraints);
            panelTop.addChild(labelResultCount);
            kitLight = new de.docware.framework.modules.gui.controls.GuiProgressBar();
            kitLight.setName("kitLight");
            kitLight.__internal_setGenerationDpi(96);
            kitLight.registerTranslationHandler(translationHandler);
            kitLight.setScaleForResolution(true);
            kitLight.setMinimumWidth(130);
            kitLight.setMinimumHeight(10);
            kitLight.setVisible(false);
            kitLight.setMarquee(true);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag kitLightConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(2, 0, 1, 1, 0.0, 0.0, "e", "n", 4, 4, 4, 4);
            kitLight.setConstraints(kitLightConstraints);
            panelTop.addChild(kitLight);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelTopConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "c", "h", 0, 0, 0, 0);
            panelTop.setConstraints(panelTopConstraints);
            panelGrid.addChild(panelTop);
            toolbar = new de.docware.framework.modules.gui.controls.toolbar.GuiToolbar();
            toolbar.setName("toolbar");
            toolbar.__internal_setGenerationDpi(96);
            toolbar.registerTranslationHandler(translationHandler);
            toolbar.setScaleForResolution(true);
            toolbar.setMinimumWidth(10);
            toolbar.setMinimumHeight(10);
            toolbar.setBackgroundImage(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignToolbarBigBackground"));
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag toolbarConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 100.0, 0.0, "c", "h", 0, 0, 0, 0);
            toolbar.setConstraints(toolbarConstraints);
            panelGrid.addChild(toolbar);
            scrollPane = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            scrollPane.setName("scrollPane");
            scrollPane.__internal_setGenerationDpi(96);
            scrollPane.registerTranslationHandler(translationHandler);
            scrollPane.setScaleForResolution(true);
            scrollPane.setMinimumWidth(10);
            scrollPane.setMinimumHeight(42);
            resultGrid = new de.docware.framework.modules.gui.controls.table.GuiTable();
            resultGrid.setName("resultGrid");
            resultGrid.__internal_setGenerationDpi(96);
            resultGrid.registerTranslationHandler(translationHandler);
            resultGrid.setScaleForResolution(true);
            resultGrid.setMinimumWidth(10);
            resultGrid.setMinimumHeight(10);
            resultGrid.addEventListener(new de.docware.framework.modules.gui.event.EventListener("mouseDoubleClickedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onTableDoubleClicked(event);
                }
            });
            resultGrid.setHtmlTablePageSplitMode(HtmlTablePageSplitMode.NO_SPLIT);
            resultGrid.addEventListener(new de.docware.framework.modules.gui.event.EventListener("tableSelectionEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onTableSelectionChanged(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder resultGridConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            resultGrid.setConstraints(resultGridConstraints);
            scrollPane.addChild(resultGrid);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag scrollPaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 100.0, 100.0, "c", "b", 0, 0, 0, 0);
            scrollPane.setConstraints(scrollPaneConstraints);
            panelGrid.addChild(scrollPane);
            labelNotFound = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelNotFound.setName("labelNotFound");
            labelNotFound.__internal_setGenerationDpi(96);
            labelNotFound.registerTranslationHandler(translationHandler);
            labelNotFound.setScaleForResolution(true);
            labelNotFound.setMinimumWidth(10);
            labelNotFound.setMinimumHeight(10);
            labelNotFound.setVisible(false);
            labelNotFound.setPaddingTop(4);
            labelNotFound.setPaddingLeft(15);
            labelNotFound.setText("!!Die Suche ist abgeschlossen. Es liegen keine Ergebnisse vor.");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelNotFoundConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 3, 1, 1, 0.0, 0.0, "nw", "h", 0, 0, 0, 0);
            labelNotFound.setConstraints(labelNotFoundConstraints);
            panelGrid.addChild(labelNotFound);
            labelMinChar = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelMinChar.setName("labelMinChar");
            labelMinChar.__internal_setGenerationDpi(96);
            labelMinChar.registerTranslationHandler(translationHandler);
            labelMinChar.setScaleForResolution(true);
            labelMinChar.setMinimumWidth(10);
            labelMinChar.setMinimumHeight(10);
            labelMinChar.setVisible(false);
            labelMinChar.setPaddingTop(4);
            labelMinChar.setPaddingLeft(15);
            labelMinChar.setText("!!Minimale Anzahl von Zeichen für Suche nicht erreicht");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelMinCharConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 4, 1, 1, 0.0, 0.0, "nw", "h", 0, 0, 0, 0);
            labelMinChar.setConstraints(labelMinCharConstraints);
            panelGrid.addChild(labelMinChar);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelGridConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 100.0, 100.0, "c", "b", 0, 0, 0, 0);
            panelGrid.setConstraints(panelGridConstraints);
            panelMain.addChild(panelGrid);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMainConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMain.setConstraints(panelMainConstraints);
            this.addChild(panelMain);
            buttonPanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonPanel.setName("buttonPanel");
            buttonPanel.__internal_setGenerationDpi(96);
            buttonPanel.registerTranslationHandler(translationHandler);
            buttonPanel.setScaleForResolution(true);
            buttonPanel.setMinimumWidth(10);
            buttonPanel.setMinimumHeight(10);
            buttonPanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonOKActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onOKButtonClick(event);
                }
            });
            buttonPanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonCancelActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onCancelButtonClick(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder buttonPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            buttonPanelConstraints.setPosition("south");
            buttonPanel.setConstraints(buttonPanelConstraints);
            this.addChild(buttonPanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}