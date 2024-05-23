/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.db.datatypes.date.DateConfig;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.AbstractJavaViewerMainFormContainer;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.forms.common.components.GuiTableClickPositionHelper;
import de.docware.apps.etk.base.forms.common.components.TableClickPosition;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrderList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.OnActivateAuthorOrderEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsLoadEditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.picorder.PicOrdersToModuleGridForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsChangeSetViewerElem;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditValidationHelper;
import de.docware.framework.modules.config.db.datatypes.DatatypeUtils;
import de.docware.framework.modules.db.serialization.SerializedDBDataObjectState;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiSeparator;
import de.docware.framework.modules.gui.controls.GuiWindow;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.table.*;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerOptions;
import de.docware.framework.modules.gui.layout.constraints.AbstractConstraints;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.j2ee.EC;
import de.docware.util.misc.id.IdWithType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dialog zur Anzeige der {@link iPartsChangeSetViewerElem}s zu einem Autoren-Auftrag
 */
public class EditChangeSetViewerForm extends AbstractJavaViewerForm {

    private enum OBJECT_COLS {
        TYPE("!!Objektart"),
        NUMBER("!!Objekt-Nummer"),
        DESCRIPTION("!!Objektbenennung"),
        CHANGE_DATE("!!Letzte Änderung"),
        STATE("!!Status Bildauftrag"),
        DELETED("!!Gelöscht");

        private String description;

        OBJECT_COLS(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private ObjectTable table;
    private iPartsChangeSetId changeSetId;
    private boolean isAuthOrderActive;
    private boolean isAOActivatable;
    private boolean isAuthOrderInEndState;
    private GuiMenuItem gotoMenu;
    private GuiMenuItem showMenu;
    private GuiMenuItem validateAssemblyMenu;
    private GuiMenuItem menuItemOpenMCInASPLM;
    private OnActivateAuthorOrderEvent onActivateAuthorOrder;

    /**
     * Erzeugt eine Instanz von EditChangeSetViewerForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditChangeSetViewerForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
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
        ToolbarButtonMenuHelper helper = new ToolbarButtonMenuHelper(getConnector(), null);
        // Menüeintrag "Laden" erzeugen
        EventListener listener = new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                doLoadSelectedModules(event);
            }
        };
        gotoMenu = helper.createMenuEntry("gotoMenu", "!!Laden", DefaultImages.module.getImage(), listener, getUITranslationHandler());
        contextMenuTable.addChild(gotoMenu);

        // Menüeintrag "Anzeigen" erzeugen
        listener = new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                doShowSelectedPart(event);
            }
        };
        showMenu = helper.createMenuEntry("showMenu", "!!Anzeigen", DefaultImages.part.getImage(), listener, getUITranslationHandler());
        contextMenuTable.addChild(showMenu);

        // Menüeintrag "TU überprüfen" erzeugen
        listener = new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                doValidateAssembly(event);
            }
        };
        validateAssemblyMenu = helper.createMenuEntry("validateAssemblyMenu", EditToolbarButtonAlias.EDIT_VALIDATE_ASSEMBLY.getTooltip(),
                                                      EditToolbarButtonAlias.EDIT_VALIDATE_ASSEMBLY.getImage(), listener, getUITranslationHandler());
        contextMenuTable.addChild(validateAssemblyMenu);

        menuItemOpenMCInASPLM = helper.createContextMenuEntry(EditToolbarButtonAlias.IMG_OPEN_MC_IN_ASPLM,
                                                              getUITranslationHandler(), new EventListener(Event.MENU_ITEM_EVENT,
                                                                                                           EventListenerOptions.SYNCHRON_EVENT) {
                    @Override
                    public void fire(Event event) {
                        String mediaContainer = getMediaContainerFromChangeSetViewerElem(getSelectedChangeSetViewerElem());
                        PicOrdersToModuleGridForm.openMediaContainerInASPLM(mediaContainer);
                    }
                });
        contextMenuTable.addChild(menuItemOpenMCInASPLM);

        // ObjectTabelle erzeugen und StringGrid erstezen
        AbstractConstraints constraints = mainWindow.tableObjects.getConstraints();
        table = new ObjectTable();
        table.setConstraints(constraints);
        mainWindow.tableObjects.removeFromParent();
        mainWindow.scrollpaneObjects.addChild(table);
        table.setMultiSelect(true);
        setObjectTableHeader();
        table.setPageSplitNumberOfEntriesPerPage(iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE);
        table.setHtmlTablePageSplitMode(HtmlTablePageSplitMode.NO_SPLIT);
        table.setResizeColumnIndex(2); // Spalte für Objektbenennung

        resizeForm(mainWindow.panelMain.getPreferredHeight());
    }

    @Override
    public GuiPanel getGui() {
        return mainWindow.panelMain;
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

    @Override
    public AbstractJavaViewerFormIConnector getConnector() {
        return super.getConnector();
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

    /**
     * Füllen des Grids
     *
     * @param changeSetViewerElemList
     * @param changeSetId
     * @param isAuthOrderActive
     * @param isAOActivatable
     * @param isAuthOrderInEndState
     */
    public void fillGrid(List<iPartsChangeSetViewerElem> changeSetViewerElemList, iPartsChangeSetId changeSetId, boolean isAuthOrderActive,
                         boolean isAOActivatable, boolean isAuthOrderInEndState) {
        this.changeSetId = changeSetId;
        this.isAuthOrderActive = isAuthOrderActive;
        this.isAOActivatable = isAOActivatable;
        this.isAuthOrderInEndState = isAuthOrderInEndState;
        if (isAuthOrderActive) {
            startPseudoTransactionForActiveChangeSet(true);
            try {
                getTable().fillGrid(changeSetViewerElemList, isAuthOrderActive, false);
            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }
        } else {
            getTable().fillGrid(changeSetViewerElemList, isAuthOrderActive, isAuthOrderInEndState);
        }
    }

    public void clearGrid() {
        getTable().clearGrid();
    }

    /**
     * Zeigt bzw. verbirgt das Label zur Anzeige, dass es keine Einträge gibt.
     *
     * @param showNoResults
     */
    public void showNoResultsLabel(boolean showNoResults) {
        if (showNoResults && !mainWindow.labelNotFound.isVisible()) {
            getTable().setHtmlTablePageSplitMode(HtmlTablePageSplitMode.NO_SPLIT);
            GuiLabel label = mainWindow.labelNotFound;

            ConstraintsGridBag constraints = (ConstraintsGridBag)mainWindow.scrollpaneObjects.getConstraints();
            constraints.setWeightx(0);
            constraints.setWeighty(0);
            mainWindow.scrollpaneObjects.setConstraints(constraints);

            constraints = (ConstraintsGridBag)label.getConstraints();
            constraints.setWeightx(1);
            constraints.setWeighty(1);
            label.setConstraints(constraints);
            mainWindow.panelMain.addChild(label);
            label.setVisible(true);
        } else if (!showNoResults && mainWindow.labelNotFound.isVisible()) {
            mainWindow.labelNotFound.removeFromParent();

            mainWindow.labelNotFound.setVisible(false);
            // da beide Komponenten Weight=1 haben, nicht im GuiDesigner setzbar
            ConstraintsGridBag constraints = (ConstraintsGridBag)mainWindow.scrollpaneObjects.getConstraints();
            constraints.setWeightx(1);
            constraints.setWeighty(1);
            mainWindow.scrollpaneObjects.setConstraints(constraints);
        }
    }

    /**
     * Setzt den Text für das Label zur Anzeige, dass es keine Eintäge gibt.
     *
     * @param text
     */
    public void setNoResultsLabelText(String text) {
        mainWindow.labelNotFound.setText(text);
    }

    private void buttonOKClick(Event event) {
        close();
    }

    protected void resizeForm(int totalHeight) {
        totalHeight = Math.min(206, totalHeight + mainWindow.getHeight());
        mainWindow.setHeight(totalHeight);
        mainWindow.setMaximumHeight(totalHeight);
    }

    protected ObjectTable getTable() {
        return table;
    }

    /**
     * Table-Header setzen
     */
    protected void setObjectTableHeader() {
        GuiTableHeader tableHeader = new GuiTableHeader();
        for (OBJECT_COLS cols : OBJECT_COLS.values()) {
            GuiLabel label = new GuiLabel(cols.getDescription());
            tableHeader.addChild(label);
        }
        getTable().setHeader(tableHeader);
    }

    /**
     * Bearbeiten der Menu-Zustände
     */
    protected void doOnTableSelectionChanged() {
        boolean gotoEnabled;
        boolean showEnabled = true;
        boolean validateAssemblyEnabled = true;
        List<GuiTableRow> selectedRows = getTable().getSelectedRows();
        boolean isNoSelection = selectedRows.isEmpty();
        if (isNoSelection) {
            gotoEnabled = false;
            showEnabled = false;
            validateAssemblyMenu.setVisible(false);
            menuItemOpenMCInASPLM.setVisible(false);
        } else {
            if (selectedRows.size() == 1) {
                setMenusVisible(selectedRows.get(0));
            } else {
                gotoMenu.setVisible(true);
                showMenu.setVisible(false);
                validateAssemblyMenu.setVisible(false);
                menuItemOpenMCInASPLM.setVisible(false);
                showEnabled = false;
            }

            // Wenn alle ausgewählten Einträge gelöscht wurden, muss "Laden" disabled werden
            gotoEnabled = false;
            validateAssemblyEnabled = false;
            for (GuiTableRow selectedRow : selectedRows) {
                if (((ObjectRow)selectedRow).getChangeSetViewerElem().getState() != SerializedDBDataObjectState.DELETED) {
                    gotoEnabled = true;
                    validateAssemblyEnabled = true;
                    break;
                }
            }
        }
        gotoMenu.setEnabled(gotoEnabled);
        showMenu.setEnabled(showEnabled);
        validateAssemblyMenu.setEnabled(validateAssemblyEnabled);

/* alte Abfrage
        boolean gotoEnabled = isAuthOrderActive;
        boolean showEnabled = isAuthOrderActive;
        List<GuiTableRow> selectedRows = getTable().getSelectedRows();
        boolean isNoSelection = selectedRows.isEmpty();
        if (isAuthOrderActive) {
            if (isNoSelection) {
                gotoEnabled = false;
                showEnabled = false;
            } else {
                if (selectedRows.size() == 1) {
                    setMenusVisible(selectedRows.get(0));
                } else {
                    gotoMenu.setVisible(true);
                    showMenu.setVisible(false);
                    showEnabled = false;
                }
            }
        } else {
            if (!isNoSelection) {
                if (selectedRows.size() == 1) {
                    setMenusVisible(selectedRows.get(0));
                } else {
                    gotoMenu.setVisible(true);
                    showMenu.setVisible(false);
                    showEnabled = false;
                }
            }
        }
        gotoMenu.setEnabled(gotoEnabled);
        showMenu.setEnabled(showEnabled);
*/
    }

    protected void doOnTableDblClick(Event event) {
        List<GuiTableRow> selectedRows = getTable().getSelectedRows();
        if (selectedRows.size() == 1) {
            if (showMenu.isVisible() && showMenu.isEnabled()) {
                doShowSelectedPart(event);
            } else if (gotoMenu.isEnabled()) {
                doLoadSelectedModules(event);
            }
        } else if (gotoMenu.isEnabled()) {
            doLoadSelectedModules(event);
        }
    }

    private void setMenusVisible(GuiTableRow row) {
        if (row instanceof ObjectRow) {
            iPartsChangeSetViewerElem changeSetViewerElem = ((ObjectRow)row).getChangeSetViewerElem();
            if (changeSetViewerElem.isPart()) {
                gotoMenu.setVisible(false);
                showMenu.setVisible(true);
                validateAssemblyMenu.setVisible(false);
                menuItemOpenMCInASPLM.setVisible(false);
            } else {
                gotoMenu.setVisible(true);
                showMenu.setVisible(false);
                validateAssemblyMenu.setVisible(changeSetViewerElem.isAssembly());
                if (changeSetViewerElem.isPictureOrder()) {
                    menuItemOpenMCInASPLM.setVisible(true);
                    menuItemOpenMCInASPLM.setEnabled(StrUtils.isValid(getMediaContainerFromChangeSetViewerElem(changeSetViewerElem)));
                } else {
                    menuItemOpenMCInASPLM.setVisible(false);
                }
            }
        } else {
            gotoMenu.setVisible(false);
            showMenu.setVisible(false);
            validateAssemblyMenu.setVisible(false);
            menuItemOpenMCInASPLM.setVisible(false);
        }
    }

    /**
     * Laden der selektierten Assemblies im Editor
     *
     * @param event
     */
    private void doLoadSelectedModules(Event event) {
        final List<GuiTableRow> selectedRows = getTable().getSelectedRows();
        if (selectedRows.isEmpty()) {
            return;
        }
        if (!isAuthOrderActive && !isAuthOrderInEndState) {
            if (!isAOActivatable) {
                String msg = "!!Modul kann nicht geladen werden, da der Autoren-Auftrag nicht aktiviert werden kann.";
                if (selectedRows.size() > 1) {
                    msg = "!!Module können nicht geladen werden, da der Autoren-Auftrag nicht aktiviert werden kann.";
                }
                MessageDialog.showWarning(msg);
                return;
            } else {
                if (onActivateAuthorOrder != null) {
                    int[] indices = getTable().getSelectedRowIndices();
                    if (onActivateAuthorOrder.onActivateAuthorOrder()) {
                        getTable().setSelectedRows(indices, false, true, true);
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
            final Set<String> selectedModuleSet = new HashSet<String>();
            String picOrderGuid = "";
            String picModuleNo = "";
            for (GuiTableRow selectedRow : selectedRows) {
                if (selectedRow instanceof ObjectRow) {
                    iPartsChangeSetViewerElem changeSetViewerElem = ((ObjectRow)selectedRow).getChangeSetViewerElem();
                    if ((changeSetViewerElem != null)) {
                        if (changeSetViewerElem.isAssembly() && (changeSetViewerElem.getState() != SerializedDBDataObjectState.DELETED)) {
                            selectedModuleSet.add(changeSetViewerElem.getPkValues()[0]);
                        } else if (changeSetViewerElem.isPictureOrder()) {
                            selectedModuleSet.add(changeSetViewerElem.getPkValues()[1]);
                            picOrderGuid = changeSetViewerElem.getPkValues()[0];
                            picModuleNo = changeSetViewerElem.getPkValues()[1];
                        } else {
                            progressForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Selektierter Eintrag \"%1\" wird ignoriert",
                                                                                                  changeSetViewerElem.getVisualType()));
                        }
                    }

                }
            }
            // Soll zu einem Bildauftrag in einem Modul gesprungen werden?
            boolean jumpToPictureOrder = StrUtils.isValid(picModuleNo, picOrderGuid);
            if (jumpToPictureOrder) {
                if (selectedModuleSet.size() > 1) {
                    progressForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Lade Module und Bildauftrag..."));
                } else {
                    progressForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Lade Bildauftrag..."));
                }
            } else {
                progressForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Lade Module..."));
            }

            if (selectedModuleSet.size() > 1) {
                progressForm.showModal(new FrameworkRunnable() {
                    @Override
                    public void run(FrameworkThread thread) {
                        doLoadModules(selectedModuleSet, progressForm);
                        // Autoclose
                        progressForm.getMessageLog().hideProgress();
                        progressForm.closeWindowIfNotAutoClose(ModalResult.OK);
                    }
                });
            } else {
                doLoadModules(selectedModuleSet, null);
            }

            // Es handelt sich um einen Sprung zu einem Bildauftrag in einem Modul
            // dieser wird i.A. nicht ausgeführt
//            if (jumpToPictureOrder) {
//                showPictureOrderInModule(picModuleNo, picOrderGuid);
//            }
        }
    }

    private void doLoadModules(Set<String> selectedModulList, EtkMessageLogForm progressForm) {
        iPartsLoadEditModuleHelper.doLoadModules(getConnector(), selectedModulList, isAuthOrderInEndState, progressForm);
    }

    private void showPictureOrderInModule(final String moduleNumber, final String picOrderGUID) {
        if (!StrUtils.isValid(moduleNumber, picOrderGUID)) {
            return;
        }
        GuiWindow.showWaitCursorForRootWindow(true);
        try {
            List<AbstractJavaViewerMainFormContainer> editModuleForms = getConnector().getMainWindow().getFormsFromClass(EditModuleForm.class);
            if (!editModuleForms.isEmpty()) {
                final EditModuleForm editModuleForm = (EditModuleForm)editModuleForms.get(0);
                Session.invokeThreadSafeInSession(() -> {
                    // Bildauftrag anzeigen
                    editModuleForm.showPicOrderInActiveModule(moduleNumber, picOrderGUID);
                });
            }
        } finally {
            GuiWindow.showWaitCursorForRootWindow(false);
        }
    }

    /**
     * Anzeige eines selektierten Parts
     *
     * @param event
     */
    private void doShowSelectedPart(Event event) {
        List<GuiTableRow> selectedRows = getTable().getSelectedRows();
        if (selectedRows.isEmpty() || (selectedRows.size() != 1)) {
            return;
        }
        if (!isAuthOrderActive) {
            if (!isAOActivatable) {
                String msg = "!!Stammdaten können nicht angezeigt werden, da der Autoren-Auftrag nicht aktiviert werden kann.";
                MessageDialog.showWarning(msg);
                return;
            } else {
                if (onActivateAuthorOrder != null) {
                    int[] indices = getTable().getSelectedRowIndices();
                    if (onActivateAuthorOrder.onActivateAuthorOrder()) {
                        getTable().setSelectedRows(indices, false, true);
                        doShowSelectedPart(null);
                    }
                }
                return;
            }
        } else {
            iPartsChangeSetViewerElem changeSetViewerElem = getSelectedChangeSetViewerElem();
            if ((changeSetViewerElem != null) && changeSetViewerElem.isPart()) {
                IdWithType id = changeSetViewerElem.createId();
                iPartsPartId partId = new iPartsPartId(id.getValue(1), id.getValue(2));
                EditUserControls eCtrl;
                startPseudoTransactionForActiveChangeSet(true);
                try {
                    eCtrl = new EditUserControls(getConnector(), this, EtkDbConst.TABLE_MAT, partId);
                    eCtrl.setReadOnly(true);
                    eCtrl.setMainTitle("!!Stammdaten Anzeige");
                } finally {
                    stopPseudoTransactionForActiveChangeSet();
                }
                if (eCtrl != null) {
                    eCtrl.showModal();
                }
            }
        }
    }

    /**
     * Selektiertes Modul überprüfen
     *
     * @param event
     */
    private void doValidateAssembly(Event event) {
        if (changeSetId == null) {
            MessageDialog.show("!!Es ist kein gültiger Autoren-Auftrag ausgewählt.", EditToolbarButtonAlias.EDIT_VALIDATE_ASSEMBLY.getTooltip());
            return;
        }

        iPartsChangeSetViewerElem changeSetViewerElem = getSelectedChangeSetViewerElem();
        if ((changeSetViewerElem == null) || !changeSetViewerElem.isAssembly()) {
            MessageDialog.show("!!Es ist kein technischer Umfang ausgewählt.", EditToolbarButtonAlias.EDIT_VALIDATE_ASSEMBLY.getTooltip());
            return;
        }

        // Es muss VOR dem temporären aktivieren des Autorenauftrags geschaut werden, welcher gerade aktiv ist.
        final iPartsDataAuthorOrder activeAuthorOrder = iPartsDataAuthorOrderList.getAuthorOrderByActiveChangeSet(getProject());
        final AssemblyId assemblyId = new AssemblyId(changeSetViewerElem.getPkValues()[0], "");
        Runnable validationRunnable = new Runnable() {
            @Override
            public void run() {
                final EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getProject(), assemblyId);

                // Prüfung auf PSK-Recht
                if (assembly instanceof iPartsDataAssembly) {
                    if (!((iPartsDataAssembly)assembly).checkPSKInSession(false, true)) {
                        return;
                    }
                }

                EditModuleFormConnector connector = new EditModuleFormConnector(getConnector()) {
                    @Override
                    public EtkDataAssembly getCurrentAssembly() {
                        return assembly;
                    }
                };
                connector.setCurrentDataAuthorOrder(activeAuthorOrder);
                try {
                    // Technischen Umfang überprüfen
                    iPartsEditValidationHelper.validateAssembly(connector, EditChangeSetViewerForm.this);
                } finally {
                    connector.dispose();
                }
            }
        };

        if (getRevisionsHelper() != null) {
            getRevisionsHelper().runInChangeSet(getProject(), validationRunnable, isAOActivatable, changeSetId);
        }
    }

    private iPartsChangeSetViewerElem getSelectedChangeSetViewerElem() {
        List<GuiTableRow> selectedRows = table.getSelectedRows();
        if (selectedRows.size() == 1) {
            GuiTableRow selectedRow = selectedRows.get(0);
            if (selectedRow instanceof ObjectRow) {
                return ((ObjectRow)selectedRow).getChangeSetViewerElem();
            }
        }

        return null;
    }

    private String getMediaContainerFromChangeSetViewerElem(iPartsChangeSetViewerElem changeSetViewerElem) {
        if (changeSetViewerElem != null) {
            return changeSetViewerElem.getMediaContainer();
        } else {
            return "";
        }
    }


    private class ObjectRow extends GuiTableRow {

        private iPartsChangeSetViewerElem changeSetViewerElem;

        public ObjectRow(iPartsChangeSetViewerElem changeSetViewerElem) {
            this.changeSetViewerElem = changeSetViewerElem;
        }

        public iPartsChangeSetViewerElem getChangeSetViewerElem() {
            return changeSetViewerElem;
        }
    }

    private class ObjectTable extends GuiTable {

        public ObjectTable() {
            super();
            addEventListener(new EventListener(Event.MOUSE_DOUBLECLICKED_EVENT) {
                @Override
                public void fire(Event event) {
                    onTableDblClicked(event);
                }
            });
            addEventListener(new EventListener(Event.TABLE_SELECTION_EVENT) {
                @Override
                public void fire(Event event) {
                    onTableSelectionChanged(event);
                }
            });
            setContextMenu(contextMenuTable);
            addCopyContextMenuItem();
        }

        public void clearGrid() {
            removeRows();
            setHtmlTablePageSplitMode(HtmlTablePageSplitMode.NO_SPLIT);
        }

        public void setMultiSelect(boolean multiSelect) {
            TableSelectionMode selectionMode = TableSelectionMode.SELECTION_MODE_SINGLE_SELECTION;
            if (multiSelect) {
                selectionMode = TableSelectionMode.SELECTION_MODE_ARBITRARY_SELECTION;
            }
            getTable().setSelectionMode(selectionMode);
        }

        public void scrollToRowIfExists(int selectedRowIndex) {
            // Selektion wiederherstellen
            selectedRowIndex = Math.min(selectedRowIndex, getTable().getRowCount() - 1);
            if (selectedRowIndex >= 0) {
                getTable().setSelectedRow(selectedRowIndex, true);
            } else if (getTable().getRowCount() > 0) {
                getTable().setSelectedRow(0, true);
            }
        }


        protected String getVisualValueOfCol(OBJECT_COLS col, String value) {
            if (StrUtils.isEmpty(value)) {
                value = "";
            }
            switch (col) {
                case TYPE:
                    break;
                case NUMBER:
                    break;
                case DESCRIPTION:
                    break;
                case CHANGE_DATE:
                    DateConfig dateConfig = DateConfig.getInstance(getConfig());
                    value = dateConfig.formatDateTime(getProject().getDBLanguage(), value);
                    break;
                case STATE:
                    value = getProject().getVisObject().asHtml(iPartsConst.TABLE_DA_PICORDER, iPartsConst.FIELD_DA_PO_STATUS, value, getProject().getViewerLanguage()).getStringResult();
                    break;
                case DELETED:
                    if (value.equals(SerializedDBDataObjectState.DELETED.name())) {
                        value = DatatypeUtils.makeImgTag(DatatypeUtils.getImageForValue(4, true), true);
                    } else {
                        value = "";
                    }
                    break;
            }
            value = DatatypeUtils.addHtmlTags(EC.jhsnbsp(value), true);
            return value;
        }

        protected ObjectRow createRow(iPartsChangeSetViewerElem changeSetViewerElem, boolean isAuthOrderActive, boolean isAuthOrderInEndState) {
            ObjectRow row = new ObjectRow(changeSetViewerElem);
            for (OBJECT_COLS cols : OBJECT_COLS.values()) {
                String value = "";
                switch (cols) {
                    case TYPE:
                        value = getVisualValueOfCol(cols, changeSetViewerElem.getVisualType());
                        break;
                    case NUMBER:
                        value = getVisualValueOfCol(cols, changeSetViewerElem.getViewingId());
                        break;
                    case DESCRIPTION:
                        if (isAuthOrderActive || isAuthOrderInEndState) {
                            value = getVisualValueOfCol(cols, changeSetViewerElem.getDescription(getProject()));
                        }
                        break;
                    case CHANGE_DATE:
                        if (!StrUtils.isEmpty(changeSetViewerElem.getDateTime())) {
                            value = getVisualValueOfCol(cols, changeSetViewerElem.getDateTime());
                        }
                        break;
                    case STATE:
                        value = getVisualValueOfCol(cols, changeSetViewerElem.getPicOrderState());
                        break;
                    case DELETED:
                        value = getVisualValueOfCol(cols, changeSetViewerElem.getState().name());
                        break;
                }
                GuiLabel label = new GuiLabel(value);
                row.addChild(label);
            }
            return row;
        }

        public void fillGrid(List<iPartsChangeSetViewerElem> changeSetViewerElemList, boolean isAuthOrderActive, boolean isAuthOrderInEndState) {
            List<IdWithType> selectedElems = new DwList<IdWithType>();
            for (GuiTableRow row : getSelectedRows()) {
                if (row instanceof ObjectRow) {
                    selectedElems.add(((ObjectRow)row).getChangeSetViewerElem().createId());
                }
            }
            clearGrid();
            try {
                switchOffEventListeners();
                for (iPartsChangeSetViewerElem changeSetViewerElem : changeSetViewerElemList) {
                    ObjectRow row = createRow(changeSetViewerElem, isAuthOrderActive, isAuthOrderInEndState);
                    addRow(row);

                    int pageSplitNumberOfEntriesPerPage = getPageSplitNumberOfEntriesPerPage();
                    if ((pageSplitNumberOfEntriesPerPage > 0) && (getRowCount() > pageSplitNumberOfEntriesPerPage)) {
                        setHtmlTablePageSplitMode(HtmlTablePageSplitMode.BUTTONS);
                    }
                }
            } finally {
                switchOnEventListeners();
            }
            if (!selectedElems.isEmpty()) {
                List<Integer> selectedIndices = new DwList<Integer>();
                for (int index = 0; index < getTable().getRowCount(); index++) {
                    GuiTableRow row = getTable().getRow(index);
                    if (row instanceof ObjectRow) {
                        if (selectedElems.contains(((ObjectRow)row).getChangeSetViewerElem().createId())) {
                            selectedIndices.add(index);
                        }
                    }
                }
                if (!selectedIndices.isEmpty()) {
                    getTable().setSelectedRows(Utils.toIntArray(selectedIndices), false, true);
                }
            }
            showNoResultsLabel(changeSetViewerElemList.isEmpty());
        }

        /**
         * Fügt dem Kontextmenü den Standard-Kontextmenüeintrag für das Kopieren des Inhalts hinzu.
         */
        protected void addCopyContextMenuItem() {
            if (!contextMenuTable.getChildren().isEmpty()) {
                GuiSeparator separator = new GuiSeparator();
                separator.setName("menuSeparator");
                contextMenuTable.addChild(separator);
            }
            ToolbarButtonMenuHelper toolbarHelper = new ToolbarButtonMenuHelper(getConnector(), null);
            GuiMenuItem menuItemCopy = toolbarHelper.createCopyMenuForTable(this, getUITranslationHandler());
            contextMenuTable.addChild(menuItemCopy);
        }


        /**
         * Wird aufgerufen, wenn sich die Selektion im Grid geändert hat.
         *
         * @param event
         */
        public void onTableSelectionChanged(Event event) {
            doOnTableSelectionChanged();
        }

        /**
         * Wird aufgerufen, wenn man im Grid auf den Header doppelklickt
         *
         * @param event
         */
        public void onHeaderDblClicked(int col, Event event) {
        }

        protected void onCellDblClicked(Event event) {
            doOnTableDblClick(event);
        }

        /**
         * Zelle oder Header ermitteln und den Event angereichert um diese Parameter weiterleiten
         *
         * @param event
         */
        private void onTableDblClicked(Event event) {
            TableClickPosition clickPos = GuiTableClickPositionHelper.getTableClickPositionFromEvent(event, getTable());
            //Doppelklick auf TableHeader?
            if (clickPos.isHeader()) {
                onHeaderDblClicked(clickPos.getCol(), event);
            }

            if (!clickPos.isHeader() && !clickPos.isFreeSpace()) {
                onCellDblClicked(event);
            }
        }


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
        private de.docware.framework.modules.gui.controls.GuiScrollPane scrollpaneObjects;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.table.GuiTable tableObjects;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelNotFound;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            contextMenuTable = new de.docware.framework.modules.gui.controls.menu.GuiContextMenu();
            contextMenuTable.setName("contextMenuTable");
            contextMenuTable.__internal_setGenerationDpi(96);
            contextMenuTable.registerTranslationHandler(translationHandler);
            contextMenuTable.setScaleForResolution(true);
            contextMenuTable.setMinimumWidth(10);
            contextMenuTable.setMinimumHeight(10);
            contextMenuTable.setMenuName("contextMenuTable");
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
            panelMain.setBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignTableContentBackground"));
            panelMain.setPaddingTop(4);
            panelMain.setPaddingLeft(4);
            panelMain.setPaddingRight(4);
            panelMain.setPaddingBottom(4);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelMain.setLayout(panelMainLayout);
            scrollpaneObjects = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            scrollpaneObjects.setName("scrollpaneObjects");
            scrollpaneObjects.__internal_setGenerationDpi(96);
            scrollpaneObjects.registerTranslationHandler(translationHandler);
            scrollpaneObjects.setScaleForResolution(true);
            scrollpaneObjects.setMinimumWidth(10);
            scrollpaneObjects.setMinimumHeight(42);
            scrollpaneObjects.setBorderWidth(0);
            tableObjects = new de.docware.framework.modules.gui.controls.table.GuiTable();
            tableObjects.setName("tableObjects");
            tableObjects.__internal_setGenerationDpi(96);
            tableObjects.registerTranslationHandler(translationHandler);
            tableObjects.setScaleForResolution(true);
            tableObjects.setMinimumWidth(10);
            tableObjects.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder tableObjectsConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            tableObjects.setConstraints(tableObjectsConstraints);
            scrollpaneObjects.addChild(tableObjects);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag scrollpaneObjectsConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 100.0, "c", "b", 0, 0, 0, 0);
            scrollpaneObjects.setConstraints(scrollpaneObjectsConstraints);
            panelMain.addChild(scrollpaneObjects);
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
            labelNotFound.setText("!!Keine Objekte vorhanden");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelNotFoundConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "nw", "h", 0, 0, 0, 0);
            labelNotFound.setConstraints(labelNotFoundConstraints);
            panelMain.addChild(labelNotFound);
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
            buttonpanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonOKActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    buttonOKClick(event);
                }
            });
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