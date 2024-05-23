package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.AbstractJavaViewerMainFormContainer;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataConfirmChanges;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataConfirmChangesList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsFactoryDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.OnActivateAuthorOrderEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.AuthorOrderHistoryFormatter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.ChangeSetShowTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditDataObjectGrid;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiWindow;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolbar;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.VarParam;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Dialog zur Anzeige der {@link iPartsDataConfirmChanges}s zu einem Autoren-Auftrag
 */
public class EditConfirmChangesViewerForm extends AbstractJavaViewerForm implements iPartsConst {

    private static final String TITLE = "!!Anzeige der zu bestätigenden Änderungen";
    private static final String ROOT_KEY = iPartsEditConfigConst.iPARTS_EDIT_CONFIRM_CHANGES_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS;

    private iPartsChangeSetId changeSetId;
    private boolean isAuthOrderActive;
    private boolean isAOActivatable;
    private boolean isAuthOrderInEndState;
    private EditDataObjectGrid dataGrid;
    private String tableName = TABLE_DA_CONFIRM_CHANGES;
    private boolean showOnlyOpenConfirmations = false;
    private AuthorOrderHistoryFormatter formatter;
    private OnActivateAuthorOrderEvent onActivateAuthorOrder;
    private GuiMenuItem gotoMenu;
    private GuiMenuItem confirmMenu;

    /**
     * Erzeugt eine Instanz von EditConfirmChangesViewerForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditConfirmChangesViewerForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.changeSetId = null;
        this.formatter = new AuthorOrderHistoryFormatter(getProject());
        this.isAuthOrderActive = false;
        this.isAOActivatable = false;
        this.isAuthOrderInEndState = false;
        this.onActivateAuthorOrder = null;
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        mainWindow.setTitle(TITLE);
        dataGrid = new EditDataObjectGrid(getConnector(), this) {

            @Override
            protected void postCreateGui() {
                super.postCreateGui();
                LinkedHashMap<String, Boolean> sortFields = new LinkedHashMap<>();
                sortFields.put(TableAndFieldName.make(tableName, FIELD_DCC_DO_TYPE), false);
                sortFields.put(TableAndFieldName.make(tableName, FIELD_DCC_PARTLIST_ENTRY_ID), false);
                sortFields.put(TableAndFieldName.make(tableName, FIELD_DCC_DO_ID), false);
                installDefaultResetSortEvent(sortFields);
            }

            @Override
            protected void onTableSelectionChanged(Event event) {
                doEnableButtons();
            }

            @Override
            protected void onCellDblClicked(int row, int col, Event event) {
                doLoadSelectedModules(event);
            }

            @Override
            protected void onHeaderDblClicked(int col, Event event) {
//                if (searchRunning) {
//                    stopSearch();
//                }
//                doGridHeaderDoubleClick(event);
            }


            @Override
            protected void createToolbarButtons(GuiToolbar toolbar) {
            }

            @Override
            protected void createContextMenuItems(GuiContextMenu contextMenu) {
                ToolbarButtonMenuHelper helper = getToolbarHelper();
                // Menüeintrag "Laden" erzeugen
                EventListener listener = new EventListener(Event.MENU_ITEM_EVENT) {
                    @Override
                    public void fire(Event event) {
                        doLoadSelectedModules(event);
                    }
                };
                gotoMenu = helper.createMenuEntry("gotoMenu", "!!Gehe zum Stücklisteneintrag", DefaultImages.module.getImage(), listener, getUITranslationHandler());
                contextMenu.addChild(gotoMenu);

                // Menüeintrag "Anzeigen" erzeugen
                listener = new EventListener(Event.MENU_ITEM_EVENT) {
                    @Override
                    public void fire(Event event) {
                        doConfirmSelectedEntries(event);
                    }
                };
                confirmMenu = helper.createMenuEntry("confirmMenu", "!!Bestätigen", DefaultImages.part.getImage(), listener, getUITranslationHandler());
                contextMenu.addChild(confirmMenu);
            }

            @Override
            protected String getVisualValueOfField(String tableName, String fieldName, EtkDataObject objectForTable) {
                if (objectForTable.getTableName().equals(TABLE_DA_CONFIRM_CHANGES)) {
                    iPartsDataConfirmChanges confirmChanges = (iPartsDataConfirmChanges)objectForTable;
                    if (fieldName.equals(FIELD_DCC_DO_TYPE)) {
                        return ChangeSetShowTypes.getTranslatedDescriptionFromObjectType(confirmChanges.getAsId().getDataObjectType());
                    } else if (fieldName.equals(FIELD_DCC_DO_ID)) {
                        ChangeSetShowTypes.SHOW_TYPES showType = ChangeSetShowTypes.SHOW_TYPES.getShowTypeByIdType(confirmChanges.getAsId().getDataObjectType());
                        switch (showType) {
                            case PART_LIST_ENTRY:
                                IdWithType id = IdWithType.fromDBString(showType.getType(), confirmChanges.getAsId().getDataObjectId());
                                PartListEntryId partListId = new PartListEntryId(id.toStringArrayWithoutType());
                                return formatter.formatModuleAndLfdNo(partListId);
                            case FACTORY_DATA:
                                id = IdWithType.fromDBString(showType.getType(), confirmChanges.getAsId().getDataObjectId());
                                iPartsFactoryDataId factoryId = new iPartsFactoryDataId(id.toStringArrayWithoutType());
                                return formatter.formatFactoryData(factoryId);
                            default:
                                return confirmChanges.getAsId().getDataObjectId();
                        }
                    } else if (fieldName.equals(FIELD_DCC_DO_SOURCE_GUID)) {
                        return formatter.formatDIALOGKey(confirmChanges.getFieldValue(fieldName));
                    } else if (fieldName.equals(FIELD_DCC_PARTLIST_ENTRY_ID)) {
                        PartListEntryId partListEntryId = confirmChanges.getAsId().getAsPartListEntryId();
                        if (partListEntryId != null) {
                            return formatter.formatModuleAndLfdNo(partListEntryId);
                        } else {
                            return "";
                        }
                    }
                }
                return super.getVisualValueOfField(tableName, fieldName, objectForTable);
            }
        };
        dataGrid.setDisplayFields(buildDisplayFields());

        ConstraintsBorder constraints = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
        dataGrid.getGui().setConstraints(constraints);
        mainWindow.panelMain.addChild(dataGrid.getGui());

        // Den Dialog etwas größer machen
//        Dimension screenSize = FrameworkUtils.getScreenSize();
//        mainWindow.setWidth(Math.max(600, (int)(screenSize.getWidth() * 0.6)));
//        mainWindow.setHeight(Math.max(400, (int)(screenSize.getHeight() * 0.9)));
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    protected void updateTable() {
        dataGrid.setDisplayFields(buildDisplayFields());
        fillGrid();
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public OnActivateAuthorOrderEvent getOnActivateAuthorOrder() {
        return onActivateAuthorOrder;
    }

    public void setOnActivateAuthorOrder(OnActivateAuthorOrderEvent onActivateAuthorOrder) {
        this.onActivateAuthorOrder = onActivateAuthorOrder;
    }

    public void setChangeSetId(iPartsChangeSetId changeSetId, boolean isAuthOrderActive,
                               boolean isAOActivatable, boolean isAuthOrderInEndState) {
        this.changeSetId = changeSetId;
        this.isAuthOrderActive = isAuthOrderActive;
        this.isAOActivatable = isAOActivatable;
        this.isAuthOrderInEndState = isAuthOrderInEndState;
        fillGrid();
    }

    public void setShowOnlyOpenConfirmations(boolean showOnlyOpenConfirmations) {
        this.showOnlyOpenConfirmations = showOnlyOpenConfirmations;
        dataGrid.setDisplayFields(null);
        fillGrid();
    }

    private void doEnableButtons() {
        List<iPartsDataConfirmChanges> confirmChangesList = getSelectedConfirmDatas();
        if (!confirmChangesList.isEmpty()) {
            boolean isGoToAllowed = false;
            boolean isConfirmAllowed = false;
            for (iPartsDataConfirmChanges dataConfirmChanges : confirmChangesList) {
                if (!dataConfirmChanges.getAsId().getPartListEntryId().isEmpty()) {
                    isGoToAllowed = true;
                }
                if (dataConfirmChanges.getFieldValue(FIELD_DCC_CONFIRMATION_USER).isEmpty()) {
                    isConfirmAllowed = true;
                }
            }
            gotoMenu.setEnabled(isGoToAllowed);
            confirmMenu.setEnabled(isConfirmAllowed);
        } else {
            gotoMenu.setEnabled(false);
            confirmMenu.setEnabled(false);
        }
//        mainWindow.buttonpanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, !dataGrid.getTable().getSelectedRows().isEmpty());
//        addAllButton.setEnabled(dataGrid.getTable().getRowCount() != dataGrid.getMultiSelection().size());
    }

    public ModalResult showModal() {
        ModalResult modalResult = mainWindow.showModal();
        super.close();
        return modalResult;
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }

    protected void fillGrid() {
        dataGrid.clearGrid();
        if (dataGrid.getDisplayFields().size() == 0) {
            dataGrid.setDisplayFields(buildDisplayFields());
        }
        if (changeSetId != null) {
            dataGrid.getTable().switchOffEventListeners();
            iPartsDataConfirmChangesList confirmChangesList =
                    iPartsDataConfirmChangesList.loadConfirmChangesForChangeSet(getProject(), changeSetId,
                                                                                showOnlyOpenConfirmations);
            for (iPartsDataConfirmChanges dataConfirmChanges : confirmChangesList) {
                dataGrid.addObjectToGrid(dataConfirmChanges);
            }

            dataGrid.getTable().switchOnEventListeners();
        }
        if (dataGrid.getTable().getRowCount() == 0) {
            dataGrid.showNoResultsLabel(dataGrid.getTable().getRowCount() == 0);
        } else {
//            dataGrid.getTable().sortRowsAccordingToColumn(0, true);
//            dataGrid.getTable().selectAllRows();
        }
        doEnableButtons();
    }

    private void doConfirmSelectedEntries(Event event) {
        List<iPartsDataConfirmChanges> confirmChangesList = getSelectedConfirmDatas(true);
        if (confirmChangesList.isEmpty()) {
            return;
        }
        String msg = "!!Eintrag bestätigen?";
        if (confirmChangesList.size() > 1) {
            msg = "!!Einträge bestätigen?";
        }
        if (ModalResult.YES == MessageDialog.showYesNo(TranslationHandler.translate(msg),
                                                       TranslationHandler.translate("!!Bestätigung"))) {
            iPartsDataConfirmChangesList dataConfirmChangesSaveList = new iPartsDataConfirmChangesList();
            for (iPartsDataConfirmChanges dataConfirmChanges : confirmChangesList) {
                dataConfirmChanges.confirmChanges();
                dataConfirmChangesSaveList.add(dataConfirmChanges, DBActionOrigin.FROM_EDIT);
            }
            getProject().getDbLayer().startTransaction();
            try {
                dataConfirmChangesSaveList.saveToDB(getProject());
                getDbLayer().commit();
                fillGrid();
            } catch (Exception e) {
                getDbLayer().rollback();
                Logger.getLogger().handleRuntimeException(e);
            }
        }
    }

    private void doLoadSelectedModules(Event event) {
        final List<iPartsDataConfirmChanges> confirmChangesList = getSelectedConfirmDatas();
        if (confirmChangesList.isEmpty()) {
            return;
        }
        if (!isAuthOrderActive && !isAuthOrderInEndState) {
            if (!isAOActivatable) {
                String msg = "!!Modul kann nicht geladen werden, da der Autoren-Auftrag nicht aktiviert werden kann.";
                if (confirmChangesList.size() > 1) {
                    msg = "!!Module können nicht geladen werden, da der Autoren-Auftrag nicht aktiviert werden kann.";
                }
                MessageDialog.showWarning(msg);
                return;
            } else {
                if (onActivateAuthorOrder != null) {
                    int[] indices = dataGrid.getTable().getSelectedRowIndices();
                    if (onActivateAuthorOrder.onActivateAuthorOrder()) {
                        dataGrid.getTable().setSelectedRows(indices, false, true, true);
                        doLoadSelectedModules(null);
                    }
                }
                return;
            }
        } else {
            final EtkMessageLogForm progressForm = new EtkMessageLogForm("!!Im Editor laden", "!!Fortschritt", null);
            progressForm.disableButtons(true);
            progressForm.setMessagesTitle("");
            progressForm.getGui().setSize(600, 250);
            progressForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Lade Module..."));
            if (confirmChangesList.size() > 1) {
                progressForm.showModal(new FrameworkRunnable() {
                    @Override
                    public void run(FrameworkThread thread) {
                        doLoadModules(confirmChangesList, progressForm);
                        // Autoclose
                        progressForm.getMessageLog().hideProgress();
                        progressForm.closeWindowIfNotAutoClose(ModalResult.OK);
                    }
                });
            } else {
                doLoadModules(confirmChangesList, null);
                progressForm.closeWindowIfNotAutoClose(ModalResult.OK);
            }
        }
    }

    private void doLoadModules(List<iPartsDataConfirmChanges> confirmChangesList, EtkMessageLogForm progressForm) {
        List<AbstractJavaViewerMainFormContainer> editModuleForms = getConnector().getMainWindow().getFormsFromClass(EditModuleForm.class);
        if (!editModuleForms.isEmpty()) {
            final EditModuleForm editModuleForm = (EditModuleForm)editModuleForms.get(0);
            GuiWindow.showWaitCursorForRootWindow(true);
            try {
                int count = 1;
                int total = confirmChangesList.size();
                final VarParam<Boolean> oneModuleLoaded = new VarParam<>(false);
                for (final iPartsDataConfirmChanges confirmChanges : confirmChangesList) {
                    if (confirmChanges.getAsId().getPartListEntryId().isEmpty()) {
                        count++;
                        continue;
                    }
                    final PartListEntryId partListEntryId = confirmChanges.getAsId().getAsPartListEntryId();
                    if (partListEntryId == null) {
                        count++;
                        continue;
                    }
                    if (total > 1) {
                        if (progressForm != null) {
                            progressForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Lade Modul \"%1\" (%2 von %3)",
                                                                                                  partListEntryId.getKVari(),
                                                                                                  String.valueOf(count), String.valueOf(total)));
                        }
                    }

                    Runnable loadRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if (editModuleForm.loadModule(partListEntryId.getKVari(), partListEntryId.getKLfdnr(), isAuthOrderInEndState)) {
                                oneModuleLoaded.setValue(true);
                            }
                        }
                    };
                    if (J2EEHandler.isJ2EE()) {
                        Session.invokeThreadSafeInSession(loadRunnable);
                    } else {
                        loadRunnable.run();
                    }
                    count++;
                }

                // Wenn alle (aber mindestens eines) Module geladen sind am Ende zum EditModuleForm wechseln
                if (oneModuleLoaded.getValue()) {
                    Session.invokeThreadSafeInSession(() -> getConnector().getMainWindow().displayForm(editModuleForm));
                }
            } finally {
                GuiWindow.showWaitCursorForRootWindow(false);
            }
        }
    }

    private List<iPartsDataConfirmChanges> getSelectedConfirmDatas() {
        return getSelectedConfirmDatas(false);
    }

    private List<iPartsDataConfirmChanges> getSelectedConfirmDatas(boolean onlyNotConfirmedEntries) {
        List<iPartsDataConfirmChanges> confirmChangesList = new DwList<>();
        List<List<EtkDataObject>> selectedObjects = dataGrid.getMultiSelection();
        if (!selectedObjects.isEmpty()) {
            for (List<EtkDataObject> objectList : selectedObjects) {
                for (EtkDataObject dataObject : objectList) {
                    EtkDataObject confirmData = dataObject.getDataObjectByTableName(tableName, false);
                    if (confirmData != null) {
                        if (onlyNotConfirmedEntries) {
                            if (confirmData.getFieldValue(FIELD_DCC_CONFIRMATION_USER).isEmpty()) {
                                confirmChangesList.add((iPartsDataConfirmChanges)confirmData);
                            }
                        } else {
                            confirmChangesList.add((iPartsDataConfirmChanges)confirmData);
                        }
                        break;
                    }
                }
            }
        }
        return confirmChangesList;
    }

    /**
     * Erstellt die benötigten Anzeigefelder
     *
     * @return
     */
    private EtkDisplayFields buildDisplayFields() {
        EtkDisplayFields displayFields = new EtkDisplayFields();
        displayFields.load(getConfig(), ROOT_KEY);
        if (displayFields.size() == 0) {
//            displayFields.addFeld(makeDisplayField(FIELD_DCC_CHANGE_SET_ID, false, false));
            displayFields.addFeld(makeDisplayField(FIELD_DCC_DO_TYPE, false, false, true));
            displayFields.addFeld(makeDisplayField(FIELD_DCC_DO_ID, false, false, false));
            displayFields.addFeld(makeDisplayField(FIELD_DCC_DO_SOURCE_GUID, false, false, true));
            displayFields.addFeld(makeDisplayField(FIELD_DCC_PARTLIST_ENTRY_ID, false, false, true));
            if (!showOnlyOpenConfirmations) {
                displayFields.addFeld(makeDisplayField(FIELD_DCC_CONFIRMATION_USER, false, false, true));
                displayFields.addFeld(makeDisplayField(FIELD_DCC_CONFIRMATION_DATE, false, false, true));
            }
            displayFields.loadStandards(getConfig());
        }
        return displayFields;
    }

    private EtkDisplayField makeDisplayField(String fieldName, boolean multiLang, boolean isArray, boolean filterEnabled) {
        EtkDisplayField displayField = new EtkDisplayField(tableName, fieldName, multiLang, isArray);
        displayField.setColumnFilterEnabled(filterEnabled);
        return displayField;
    }

    private EtkDisplayField makeDisplayFieldWithText(String fieldName, String text, boolean multiLang, boolean isArray, boolean filterEnabled) {
        EtkDisplayField displayField = makeDisplayField(fieldName, multiLang, isArray, filterEnabled);
        displayField.setDefaultText(false);
        displayField.setText(new EtkMultiSprache(text, new String[]{ TranslationHandler.getUiLanguage() }));
        return displayField;
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
            panelMain.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMain.setLayout(panelMainLayout);
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