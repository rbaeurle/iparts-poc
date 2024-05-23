/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsLockEntryHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditDataObjectFilterGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.collections.dwlist.DwList;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * Formular für die Auswahl zum Zusammenlegen von EDS-Teilepositionen
 */
public class EditMergePartlistEntryForm extends AbstractJavaViewerForm {

    public static List<EtkDataPartListEntry> showMergePartListEntries(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                                      EtkDataPartListEntry selectedEntry) {
        EditMergePartlistEntryForm dlg = new EditMergePartlistEntryForm(dataConnector, parentForm, selectedEntry);
        dlg.setTitle(TranslationHandler.translate("!!Auswahl der Teilepositionen für Zusammenlegen"));
        List<EtkDataPartListEntry> result = null;
        if (dlg.showModal() == ModalResult.OK) {
            result = dlg.getSelectedEntries();
        }
        return result;
    }

    /**
     * Liefert die Liste der Entries, die die gleiche Materialnummer/Menge besitzen wir der selektierte Entry
     *
     * @param currentPartListEntries
     * @param selectedEntry
     * @return
     */
    public static List<EtkDataPartListEntry> getFilteredListForMerging(List<EtkDataPartListEntry> currentPartListEntries, EtkDataPartListEntry selectedEntry) {
        return getFilteredListForMerging(currentPartListEntries, selectedEntry, false, true);
    }

    private static List<EtkDataPartListEntry> getFilteredListForMerging(List<EtkDataPartListEntry> currentPartListEntries, EtkDataPartListEntry selectedEntry,
                                                                        boolean fast, boolean withMessage) {
        List<EtkDataPartListEntry> result = new DwList<>();
        String masterPart = selectedEntry.getPart().getAsId().getMatNr();
        String masterQuantity = selectedEntry.getFieldValue(iPartsConst.FIELD_K_MENGE);
        int lockedEntries = 0;
        for (EtkDataPartListEntry partListEntry : currentPartListEntries) {
            // sich selbst ausschließen
            if (partListEntry.getAsId().equals(selectedEntry.getAsId())) {
                continue;
            }
            if (masterPart.equals(partListEntry.getPart().getAsId().getMatNr())) {
                if (masterQuantity.equals(partListEntry.getFieldValue(iPartsConst.FIELD_K_MENGE))) {
                    // Gesperrte Positionen ausschließen
                    if (iPartsLockEntryHelper.isLockedWithDBCheck(partListEntry)) {
                        lockedEntries++;
                        continue;
                    }

                    result.add(partListEntry);
                    // wenn es nur darum geht, ob es mehrere TeilePositionen gibt => hier aufhören
                    if (fast) {
                        break;
                    }
                }
            }
        }
        // Meldung ausgeben, damit der Benutzer sich nicht wundert
        if (withMessage && (lockedEntries > 0)) {
            if (lockedEntries > 1) {
                MessageDialog.show(TranslationHandler.translate("!!%1 der möglichen Positionen sind für den Edit " +
                                                                "gesperrt und werden in der Auswahlliste nicht aufgeführt!",
                                                                String.valueOf(lockedEntries)), "!!Positionen zusammenlegen");
            } else {
                MessageDialog.show(TranslationHandler.translate("!!Eine der möglichen Positionen ist für den Edit " +
                                                                "gesperrt und wird in der Auswahlliste nicht aufgeführt!"),
                                   "!!Positionen zusammenlegen");
            }
        }
        return result;
    }

    /**
     * Gibt es noch Entries mit gleicher TeileNummer/Menge in den Entries
     *
     * @param currentPartListEntries
     * @param selectedEntry
     * @return
     */
    public static boolean hasOtherEntriesForMerging(List<EtkDataPartListEntry> currentPartListEntries, EtkDataPartListEntry selectedEntry) {
        return !getFilteredListForMerging(currentPartListEntries, selectedEntry, true, false).isEmpty();
    }

    private boolean isEditAllowed = true;
    private EtkDataPartListEntry masterPartListEntry;
    private EditDataObjectFilterGrid masterEntryDataGrid;
    private EditDataObjectFilterGrid selectedDataGrid;
    private EditDataObjectFilterGrid actualDataGrid;
    private EditToolbarButtonMenuHelper toolbarHelper;
    private List<EtkDataPartListEntry> startPartEntryList;
    private Map<PartListEntryId, PartListEntryExtra> completeEntriesMap;

    /**
     * Erzeugt eine Instanz von EditMergePartlistEntryForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditMergePartlistEntryForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                      EtkDataPartListEntry selectedEntry) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        masterPartListEntry = selectedEntry;
        postCreateGui();

        initPartListEntries();
        fillGrids();

        Dimension screenSize = FrameworkUtils.getScreenSize();
        mainWindow.setSize(screenSize.width - 20, screenSize.height - 20);
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        toolbarHelper = new EditToolbarButtonMenuHelper(getConnector(), mainWindow.toolbar);
        // Grid für die selektierte Teilposition (Grid ganz oben)
        masterEntryDataGrid = createGridForMasterEntry();
        mainWindow.panelExtraEntry.addChildBorderCenter(masterEntryDataGrid.getGui());

        // Grid für ausgewählte Einträge (oberes Grid)
        selectedDataGrid = createGridForSelectedEntries();
        mainWindow.panelSelectedEntries.addChildBorderCenter(selectedDataGrid.getGui());

        // Grid für auswählbare Einträge (unteres Grid)
        actualDataGrid = createGridForChoosableEntries();
        mainWindow.panelActualEntries.addChildBorderCenter(actualDataGrid.getGui());

        createToolbarButtons();
        mainWindow.buttonpanel.setButtonModalResult(GuiButtonOnPanel.ButtonType.OK, ModalResult.NONE);
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    @Override
    public EditModuleFormIConnector getConnector() {
        return (EditModuleFormIConnector)super.getConnector();
    }

    public ModalResult showModal() {
        return mainWindow.showModal();
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public boolean isEditAllowed() {
        return isEditAllowed;
    }

    public void setEditAllowed(boolean editAllowed) {
        this.isEditAllowed = editAllowed;
        mainWindow.buttonpanel.setButtonVisible(GuiButtonOnPanel.ButtonType.CANCEL, isEditAllowed);
        selectedDataGrid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_DOWN, isEditAllowed);
        actualDataGrid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_UP, isEditAllowed);
        enableButtons();
    }

    public List<EtkDataPartListEntry> getSelectedEntries() {
        List<EtkDataPartListEntry> result = null;
        if (mainWindow.getModalResult() == ModalResult.OK) {
            result = new DwList<>();
            for (PartListEntryExtra partListEntryExtra : completeEntriesMap.values()) {
                if (partListEntryExtra.isSelected) {
                    result.add(partListEntryExtra.partListEntry);
                }
            }
            if (result.isEmpty()) {
                result = null;
            }
        }
        return result;
    }

    /**
     * Erzeugt das Grid für die ausgewählten Teileporsitionnen (oberes Grid)
     *
     * @return
     */
    private EditDataObjectFilterGrid createGridForSelectedEntries() {
        EditDataObjectFilterGrid result = new EditDataObjectFilterGrid(getConnector(), this) {
            @Override
            protected void onTableSelectionChanged(Event event) {
                enableToolbar(this);
            }

            @Override
            protected void onCellDblClicked(int row, int col, Event event) {
                doRemove(event);
            }

            @Override
            protected void createContextMenuItems(GuiContextMenu contextMenu) {
                GuiMenuItem menuItem
                        = getToolbarHelper().createContextMenuEntry(EditToolbarButtonAlias.IMG_DOWN,
                                                                    "!!Entfernen", getUITranslationHandler(),
                                                                    new EventListener(Event.MENU_ITEM_EVENT) {
                                                                        @Override
                                                                        public void fire(Event event) {
                                                                            doRemove(event);
                                                                        }
                                                                    });
                contextMenu.addChild(menuItem);
            }
        };
        result.showToolbar(false);
        result.setDisplayFields(getConnector().getAssemblyListDisplayFields());
        return result;
    }

    /**
     * Erzeugt das Grid für die auswählbaren Stücklistenpositionen (unteres Grid)
     *
     * @return
     */
    private EditDataObjectFilterGrid createGridForChoosableEntries() {
        EditDataObjectFilterGrid result = new EditDataObjectFilterGrid(getConnector(), this) {
            @Override
            protected void onTableSelectionChanged(Event event) {
                enableToolbar(this);
            }

            @Override
            protected void onCellDblClicked(int row, int col, Event event) {
                doAdd(event);
            }

            @Override
            protected void createContextMenuItems(GuiContextMenu contextMenu) {
                GuiMenuItem menuItem
                        = getToolbarHelper().createContextMenuEntry(EditToolbarButtonAlias.IMG_UP,
                                                                    "!!Übernehmen", getUITranslationHandler(),
                                                                    new EventListener(Event.MENU_ITEM_EVENT) {
                                                                        @Override
                                                                        public void fire(Event event) {
                                                                            doAdd(event);
                                                                        }
                                                                    });
                contextMenu.addChild(menuItem);
            }
        };
        result.showToolbar(false);
        result.setDisplayFields(getConnector().getAssemblyListDisplayFields());
        return result;
    }

    /**
     * Erzeugt das Grid für die selektierten Teileposition (nur Anzeige)
     *
     * @return
     */
    private EditDataObjectFilterGrid createGridForMasterEntry() {
        EditDataObjectFilterGrid result = new EditDataObjectFilterGrid(getConnector(), this) {
            @Override
            protected void onTableSelectionChanged(Event event) {
            }

            @Override
            protected void onCellDblClicked(int row, int col, Event event) {
            }
        };
        result.showToolbar(false);
        result.setDisplayFields(getConnector().getAssemblyListDisplayFields());
        return result;
    }


    /**
     * Befüllt beide Grids
     */
    private void fillGrids() {
        boolean selectedIsEmpty = true;
        boolean actualIsEmpty = true;
        // Filterung/Sortierung merken
        Object storageSelected = selectedDataGrid.getFilterAndSortSettings();
        Object storageActual = actualDataGrid.getFilterAndSortSettings();
        selectedDataGrid.clearGrid();
        actualDataGrid.clearGrid();

        // Durchlaufe alle Stücklistenpositionen
        for (PartListEntryExtra partListEntryExtra : completeEntriesMap.values()) {
            if (partListEntryExtra.isSelected) {
                selectedDataGrid.addObjectToGrid(partListEntryExtra.partListEntry);
                selectedIsEmpty = false;
            } else {
                actualDataGrid.addObjectToGrid(partListEntryExtra.partListEntry);
                actualIsEmpty = false;
            }
        }
        actualDataGrid.showNoResultsLabel(actualIsEmpty);
        selectedDataGrid.showNoResultsLabel(selectedIsEmpty);
        // Filterung/Sortierung wieder setzen
        actualDataGrid.restoreFilterAndSortSettings(storageActual);
        selectedDataGrid.restoreFilterAndSortSettings(storageSelected);
        enableToolbar(null);
        enableButtons();
    }

    /**
     * Maps für die Grids erstellen
     */
    private void initPartListEntries() {
        startPartEntryList = getFilteredListForMerging(getConnector().getCurrentPartListEntries(), masterPartListEntry);
        completeEntriesMap = new LinkedHashMap<>();
        for (EtkDataPartListEntry partListEntry : startPartEntryList) {
            completeEntriesMap.put(partListEntry.getAsId(), new PartListEntryExtra(partListEntry, false));
        }
        masterEntryDataGrid.clearGrid();
        masterEntryDataGrid.addObjectToGrid(masterPartListEntry);

    }

    /**
     * Toolbar Einträge erstellen
     */
    private void createToolbarButtons() {
        toolbarHelper.addToolbarButton(EditToolbarButtonAlias.IMG_UP, "!!Übernehmen", new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doAdd(event);
            }
        });
        toolbarHelper.addToolbarButton(EditToolbarButtonAlias.IMG_DOWN, "!!Entfernen", new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doRemove(event);
            }
        });
        toolbarHelper.addToolbarButton(EditToolbarButtonAlias.IMG_UP_ALL, "!!Alle Übernehmen", new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doAddAll(event);
            }
        });
        toolbarHelper.addToolbarButton(EditToolbarButtonAlias.IMG_DOWN_ALL, "!!Alle Entfernen", new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doRemoveAll(event);
            }
        });

        enableToolbar(null);
    }

    /**
     * Toolbar-Buttons nach Selektion in den beiden Grids enablen/disablen
     *
     * @param sender
     */
    private void enableToolbar(DataObjectGrid sender) {
        boolean upEnabled = false;
        boolean downEnabled = false;
        if ((sender != null) && isEditAllowed) {
            List<EtkDataObject> list = actualDataGrid.getSelection();
            upEnabled = ((list != null) && !list.isEmpty());
            list = selectedDataGrid.getSelection();
            downEnabled = ((list != null) && !list.isEmpty());
        }
        toolbarHelper.enableToolbarButton(EditToolbarButtonAlias.IMG_UP, upEnabled);
        toolbarHelper.enableToolbarButton(EditToolbarButtonAlias.IMG_DOWN, downEnabled);

        toolbarHelper.enableToolbarButton(EditToolbarButtonAlias.IMG_UP_ALL,
                                          isEditAllowed && actualDataGrid.getTable().getRowCount() > 0);
        toolbarHelper.enableToolbarButton(EditToolbarButtonAlias.IMG_DOWN_ALL,
                                          isEditAllowed && selectedDataGrid.getTable().getRowCount() > 0);
    }

    /**
     * Ändert die Selektionsstatus der übergebenen Stücklistenpositionen.
     *
     * @param selectedList
     * @param isSelected
     */
    private void modifySelectionInGrids(List<List<EtkDataObject>> selectedList, boolean isSelected) {
        for (List<EtkDataObject> dataObjectList : selectedList) {
            PartListEntryId partListEntryId = null;
            // Erst die PartListEntryId bestimmen
            for (EtkDataObject dataObject : dataObjectList) {
                if (dataObject instanceof EtkDataPartListEntry) {
                    partListEntryId = ((EtkDataPartListEntry)dataObject).getAsId();
                    break;
                }
            }

            // Jetzt die Selektion ändern. Gelöschte Positionen wandern nicht vom oberen Grid ins untere, sie werden
            // direkt entfernt.
            if (partListEntryId != null) {
                PartListEntryExtra partListEntryExtra = completeEntriesMap.get(partListEntryId);
                if (partListEntryExtra != null) {
                    partListEntryExtra.isSelected = isSelected;
                }
            }
        }
        fillGrids();
    }

    /**
     * Eine Teileposition hinzufügen (vom unteren ins oberte Grid)
     *
     * @param event
     */
    private void doAdd(Event event) {
        if (isEditAllowed) {
            modifySelectionInGrids(actualDataGrid.getMultiSelection(), true);
        }
    }

    private void doAddAll(Event event) {
        if (isEditAllowed) {
            modifySelectionInGrids(actualDataGrid.getDataObjectList(), true);
        }
    }

    /**
     * Eine Teileposition entfernen (vom oberen ins untere Grid)
     *
     * @param event
     */
    private void doRemove(Event event) {
        if (isEditAllowed) {
            modifySelectionInGrids(selectedDataGrid.getMultiSelection(), false);
        }
    }

    private void doRemoveAll(Event event) {
        if (isEditAllowed) {
            modifySelectionInGrids(selectedDataGrid.getDataObjectList(), false);
        }
    }

    /**
     * OK-Button behandeln
     */
    private void enableButtons() {
        boolean enabled = false;
        if (isEditAllowed) {
            for (PartListEntryExtra partListEntryExtra : completeEntriesMap.values()) {
                if (partListEntryExtra.isSelected) {
                    enabled = true;
                    break;
                }
            }
        } else {
            enabled = true;
        }
        mainWindow.buttonpanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, enabled);
    }

    /**
     * Aktion bei OK-Button drücken
     *
     * @param event
     */
    private void buttonOKClicked(Event event) {
        if (isEditAllowed) {
            mainWindow.setModalResult(ModalResult.OK);
            close();
        } else {
            mainWindow.setModalResult(ModalResult.CANCEL);
            close();
        }
    }

    /**
     * Container-Hilfs-Klasse (welche Teileposition ist ausgewählt)
     */
    private class PartListEntryExtra {

        public boolean isSelected;
        public EtkDataPartListEntry partListEntry;

        public PartListEntryExtra(EtkDataPartListEntry partListEntry, boolean isSelected) {
            this.partListEntry = partListEntry;
            this.isSelected = isSelected;
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
        private de.docware.framework.modules.gui.controls.GuiPanel panelExtra;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelExtraElem;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelExtraEntry;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitpane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpane_firstChild;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelSelectedEntrie;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelSelectedEntries;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpane_secondChild;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_0;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMovement;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.toolbar.GuiToolbar toolbar;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_1;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelActualEntries;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelActualEntries;

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
            panelExtra = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelExtra.setName("panelExtra");
            panelExtra.__internal_setGenerationDpi(96);
            panelExtra.registerTranslationHandler(translationHandler);
            panelExtra.setScaleForResolution(true);
            panelExtra.setMinimumWidth(10);
            panelExtra.setMinimumHeight(106);
            panelExtra.setBorderWidth(8);
            de.docware.framework.modules.gui.layout.LayoutBorder panelExtraLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelExtra.setLayout(panelExtraLayout);
            labelExtraElem = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelExtraElem.setName("labelExtraElem");
            labelExtraElem.__internal_setGenerationDpi(96);
            labelExtraElem.registerTranslationHandler(translationHandler);
            labelExtraElem.setScaleForResolution(true);
            labelExtraElem.setMinimumWidth(10);
            labelExtraElem.setMinimumHeight(10);
            labelExtraElem.setPaddingTop(4);
            labelExtraElem.setPaddingLeft(8);
            labelExtraElem.setPaddingRight(8);
            labelExtraElem.setPaddingBottom(4);
            labelExtraElem.setText("!!Selektierte Teileposition");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelExtraElemConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelExtraElemConstraints.setPosition("north");
            labelExtraElem.setConstraints(labelExtraElemConstraints);
            panelExtra.addChild(labelExtraElem);
            panelExtraEntry = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelExtraEntry.setName("panelExtraEntry");
            panelExtraEntry.__internal_setGenerationDpi(96);
            panelExtraEntry.registerTranslationHandler(translationHandler);
            panelExtraEntry.setScaleForResolution(true);
            panelExtraEntry.setMinimumWidth(10);
            panelExtraEntry.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelExtraEntryLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelExtraEntry.setLayout(panelExtraEntryLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelExtraEntryConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelExtraEntry.setConstraints(panelExtraEntryConstraints);
            panelExtra.addChild(panelExtraEntry);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelExtraConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelExtraConstraints.setPosition("north");
            panelExtra.setConstraints(panelExtraConstraints);
            panelMain.addChild(panelExtra);
            splitpane = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitpane.setName("splitpane");
            splitpane.__internal_setGenerationDpi(96);
            splitpane.registerTranslationHandler(translationHandler);
            splitpane.setScaleForResolution(true);
            splitpane.setMinimumWidth(10);
            splitpane.setMinimumHeight(10);
            splitpane.setHorizontal(false);
            splitpane.setDividerPosition(194);
            splitpane_firstChild = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpane_firstChild.setName("splitpane_firstChild");
            splitpane_firstChild.__internal_setGenerationDpi(96);
            splitpane_firstChild.registerTranslationHandler(translationHandler);
            splitpane_firstChild.setScaleForResolution(true);
            splitpane_firstChild.setMinimumWidth(0);
            splitpane_firstChild.setMinimumHeight(0);
            splitpane_firstChild.setBorderWidth(8);
            de.docware.framework.modules.gui.layout.LayoutBorder splitpane_firstChildLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpane_firstChild.setLayout(splitpane_firstChildLayout);
            labelSelectedEntrie = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelSelectedEntrie.setName("labelSelectedEntrie");
            labelSelectedEntrie.__internal_setGenerationDpi(96);
            labelSelectedEntrie.registerTranslationHandler(translationHandler);
            labelSelectedEntrie.setScaleForResolution(true);
            labelSelectedEntrie.setMinimumWidth(10);
            labelSelectedEntrie.setMinimumHeight(10);
            labelSelectedEntrie.setPaddingTop(4);
            labelSelectedEntrie.setPaddingLeft(8);
            labelSelectedEntrie.setPaddingRight(8);
            labelSelectedEntrie.setPaddingBottom(4);
            labelSelectedEntrie.setText("!!Gewählte Einträge für Zusammenlegen");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelSelectedEntrieConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelSelectedEntrieConstraints.setPosition("north");
            labelSelectedEntrie.setConstraints(labelSelectedEntrieConstraints);
            splitpane_firstChild.addChild(labelSelectedEntrie);
            panelSelectedEntries = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelSelectedEntries.setName("panelSelectedEntries");
            panelSelectedEntries.__internal_setGenerationDpi(96);
            panelSelectedEntries.registerTranslationHandler(translationHandler);
            panelSelectedEntries.setScaleForResolution(true);
            panelSelectedEntries.setMinimumWidth(10);
            panelSelectedEntries.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelSelectedEntriesLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelSelectedEntries.setLayout(panelSelectedEntriesLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelSelectedEntriesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelSelectedEntries.setConstraints(panelSelectedEntriesConstraints);
            splitpane_firstChild.addChild(panelSelectedEntries);
            splitpane.addChild(splitpane_firstChild);
            splitpane_secondChild = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpane_secondChild.setName("splitpane_secondChild");
            splitpane_secondChild.__internal_setGenerationDpi(96);
            splitpane_secondChild.registerTranslationHandler(translationHandler);
            splitpane_secondChild.setScaleForResolution(true);
            splitpane_secondChild.setMinimumWidth(0);
            splitpane_secondChild.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.LayoutBorder splitpane_secondChildLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpane_secondChild.setLayout(splitpane_secondChildLayout);
            panel_0 = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_0.setName("panel_0");
            panel_0.__internal_setGenerationDpi(96);
            panel_0.registerTranslationHandler(translationHandler);
            panel_0.setScaleForResolution(true);
            panel_0.setMinimumWidth(10);
            panel_0.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_0Layout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_0.setLayout(panel_0Layout);
            panelMovement = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelMovement.setName("panelMovement");
            panelMovement.__internal_setGenerationDpi(96);
            panelMovement.registerTranslationHandler(translationHandler);
            panelMovement.setScaleForResolution(true);
            panelMovement.setMinimumWidth(10);
            panelMovement.setMinimumHeight(28);
            panelMovement.setBorderWidth(8);
            de.docware.framework.modules.gui.layout.LayoutBorder panelMovementLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMovement.setLayout(panelMovementLayout);
            toolbar = new de.docware.framework.modules.gui.controls.toolbar.GuiToolbar();
            toolbar.setName("toolbar");
            toolbar.__internal_setGenerationDpi(96);
            toolbar.registerTranslationHandler(translationHandler);
            toolbar.setScaleForResolution(true);
            toolbar.setMinimumWidth(10);
            toolbar.setMinimumHeight(10);
            toolbar.setBackgroundImage(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignToolbarBigBackground"));
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder toolbarConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            toolbar.setConstraints(toolbarConstraints);
            panelMovement.addChild(toolbar);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMovementConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMovementConstraints.setPosition("north");
            panelMovement.setConstraints(panelMovementConstraints);
            panel_0.addChild(panelMovement);
            panel_1 = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_1.setName("panel_1");
            panel_1.__internal_setGenerationDpi(96);
            panel_1.registerTranslationHandler(translationHandler);
            panel_1.setScaleForResolution(true);
            panel_1.setMinimumWidth(10);
            panel_1.setMinimumHeight(10);
            panel_1.setBorderWidth(8);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_1Layout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_1.setLayout(panel_1Layout);
            labelActualEntries = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelActualEntries.setName("labelActualEntries");
            labelActualEntries.__internal_setGenerationDpi(96);
            labelActualEntries.registerTranslationHandler(translationHandler);
            labelActualEntries.setScaleForResolution(true);
            labelActualEntries.setMinimumWidth(10);
            labelActualEntries.setMinimumHeight(10);
            labelActualEntries.setPaddingTop(4);
            labelActualEntries.setPaddingLeft(8);
            labelActualEntries.setPaddingRight(8);
            labelActualEntries.setPaddingBottom(4);
            labelActualEntries.setText("!!Aktuelle Einträge (gleiche Teilenummer/Menge)");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelActualEntriesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelActualEntriesConstraints.setPosition("north");
            labelActualEntries.setConstraints(labelActualEntriesConstraints);
            panel_1.addChild(labelActualEntries);
            panelActualEntries = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelActualEntries.setName("panelActualEntries");
            panelActualEntries.__internal_setGenerationDpi(96);
            panelActualEntries.registerTranslationHandler(translationHandler);
            panelActualEntries.setScaleForResolution(true);
            panelActualEntries.setMinimumWidth(10);
            panelActualEntries.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelActualEntriesLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelActualEntries.setLayout(panelActualEntriesLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelActualEntriesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelActualEntries.setConstraints(panelActualEntriesConstraints);
            panel_1.addChild(panelActualEntries);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panel_1Constraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panel_1.setConstraints(panel_1Constraints);
            panel_0.addChild(panel_1);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panel_0Constraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panel_0.setConstraints(panel_0Constraints);
            splitpane_secondChild.addChild(panel_0);
            splitpane.addChild(splitpane_secondChild);
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
            buttonpanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonOKActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    buttonOKClicked(event);
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