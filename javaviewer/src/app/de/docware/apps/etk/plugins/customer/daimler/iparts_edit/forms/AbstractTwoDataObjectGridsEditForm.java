/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.forms.common.components.MenuRunnable;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.forms.toolbar.EtkToolbarManager;
import de.docware.apps.etk.base.forms.toolbar.ToolbarButtonAlias;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.AbstractTwoDataObjectGridsForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.GuiSeparator;
import de.docware.framework.modules.gui.controls.table.GuiTable;
import de.docware.framework.modules.gui.controls.toolbar.AbstractGuiToolComponent;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;

import java.util.List;

/**
 * Abstraktes Formular um Edit-Funktionen in der Zwei-Grid-Anzeige {@link DataObjectGrid}s zu ermöglichen
 */
public abstract class AbstractTwoDataObjectGridsEditForm extends AbstractTwoDataObjectGridsForm {

    protected EditToolbarButtonMenuHelper toolbarHelperTop;
    protected EditToolbarButtonMenuHelper toolbarHelperBottom;
    protected EtkToolbarManager toolbarManagerBottom;
    protected boolean isReadOnly;

    protected AbstractTwoDataObjectGridsEditForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                 IEtkRelatedInfo relatedInfo, String configKeyTop, String dataObjectGridTopTitle,
                                                 String configKeyBottom, String dataObjectGridBottomTitle, boolean enableEditMode) {
        super(dataConnector, parentForm, relatedInfo, configKeyTop, dataObjectGridTopTitle, configKeyBottom, dataObjectGridBottomTitle, enableEditMode);
    }

    protected AbstractTwoDataObjectGridsEditForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                 String configKeyTop, String dataObjectGridTopTitle, String configKeyBottom,
                                                 String dataObjectGridBottomTitle, boolean isEditMode) {
        super(dataConnector, parentForm, configKeyTop, dataObjectGridTopTitle, configKeyBottom, dataObjectGridBottomTitle, isEditMode);
    }

    @Override
    protected void postCreateGui() {
        super.postCreateGui();

        if (editMode) {
            // Toolbar und Kontextmenu für beide Grids im Edit Modus einfügen
            toolbarHelperTop = new EditToolbarButtonMenuHelper(getConnector(), gridTop.getToolBar());
            toolbarManager = toolbarHelperTop.getToolbarManager();
            toolbarHelperBottom = new EditToolbarButtonMenuHelper(getConnector(), gridBottom.getToolBar());
            toolbarManagerBottom = toolbarHelperBottom.getToolbarManager();
            if (gridTop.getContextMenu().getChildren().size() > 1) { // Kopieren ist immer drin
                GuiSeparator separatorTop = toolbarHelperTop.createMenuSeparator("menuSeparatorT", getUITranslationHandler());
                gridTop.getContextMenu().addChild(separatorTop);
            }
            if (gridBottom.getContextMenu().getChildren().size() > 1) { // Kopieren ist immer drin
                GuiSeparator separatorBottom = toolbarHelperBottom.createMenuSeparator("menuSeparatorB", getUITranslationHandler());
                gridBottom.getContextMenu().addChild(separatorBottom);
            }

            // NEU als Button in der Toolbar und als Kontextmenu im oberen Grid
            ToolbarButtonMenuHelper.ToolbarMenuHolder holder = toolbarHelperTop.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_NEW, getUITranslationHandler(), new MenuRunnable() {
                @Override
                public void run(Event event) {
                    doNew(true);
                }
            });
            gridTop.getContextMenu().addChild(holder.menuItem);

            // ... und im unteren Grid
            holder = toolbarHelperBottom.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_NEW, getUITranslationHandler(), new MenuRunnable() {
                @Override
                public void run(Event event) {
                    doNew(false);
                }
            });
            gridBottom.getContextMenu().addChild(holder.menuItem);

            addSpecialToolbarAndMenuEntries(EditToolbarButtonAlias.EDIT_NEW, toolbarHelperTop, gridTop, toolbarHelperBottom, gridBottom);

            // EDIT als Button in der Toolbar und als Kontextmenu im oberen Grid ...
            holder = toolbarHelperTop.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_WORK, getUITranslationHandler(), new MenuRunnable() {
                @Override
                public void run(Event event) {
                    doEdit(true);
                }
            });
            gridTop.getContextMenu().addChild(holder.menuItem);
            toolbarHelperTop.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, gridTop.getContextMenu(), false);

            // ... und im unteren Grid
            holder = toolbarHelperBottom.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_WORK, getUITranslationHandler(), new MenuRunnable() {
                @Override
                public void run(Event event) {
                    doEdit(false);
                }
            });
            gridBottom.getContextMenu().addChild(holder.menuItem);
            toolbarHelperBottom.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, gridBottom.getContextMenu(), false);

            addSpecialToolbarAndMenuEntries(EditToolbarButtonAlias.EDIT_WORK, toolbarHelperTop, gridTop, toolbarHelperBottom, gridBottom);

            // DELETE als Button in der Toolbar und als Kontextmenu im oberen Grid ...
            holder = toolbarHelperTop.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_DELETE, getUITranslationHandler(), new MenuRunnable() {
                @Override
                public void run(Event event) {
                    doDelete(true);
                }
            });
            toolbarHelperTop.insertMenuBeforeTableCopyMenu(gridTop.getContextMenu(), holder.menuItem, true);
            toolbarHelperTop.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, gridTop.getContextMenu(), false);

            // ... und als Kontextmenu im unteren Grid
            holder = toolbarHelperBottom.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_DELETE, getUITranslationHandler(), new MenuRunnable() {
                @Override
                public void run(Event event) {
                    doDelete(false);
                }
            });
            toolbarHelperBottom.insertMenuBeforeTableCopyMenu(gridBottom.getContextMenu(), holder.menuItem, true);
            toolbarHelperBottom.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, gridBottom.getContextMenu(), false);

            addSpecialToolbarAndMenuEntries(EditToolbarButtonAlias.EDIT_DELETE, toolbarHelperTop, gridTop, toolbarHelperBottom, gridBottom);

            if (!isContextMenuEntryForNewObjectVisible(true)) {
                toolbarHelperTop.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW, gridTop.getContextMenu());
            }
            if (!isContextMenuEntryForEditObjectVisible(true)) {
                toolbarHelperTop.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, gridTop.getContextMenu());
            }
            if (!isContextMenuEntryForDeleteObjectVisible(true)) {
                toolbarHelperTop.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, gridTop.getContextMenu());
            }

            if (!isContextMenuEntryForNewObjectVisible(false)) {
                toolbarHelperBottom.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW, gridBottom.getContextMenu());
            }
            if (!isContextMenuEntryForEditObjectVisible(false)) {
                toolbarHelperBottom.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, gridBottom.getContextMenu());
            }
            if (!isContextMenuEntryForDeleteObjectVisible(false)) {
                toolbarHelperBottom.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, gridBottom.getContextMenu());
            }

            gridTop.modifyContextMenu(toolbarHelperTop);
            gridBottom.modifyContextMenu(toolbarHelperBottom);

            addDoubleClickEventListener(true);
            addDoubleClickEventListener(false);
        }
    }

    private void addDoubleClickEventListener(final boolean isTop) {
        getGrid(isTop).getTable().addEventListener(new EventListener(Event.MOUSE_DOUBLECLICKED_EVENT) {
            @Override
            public void fire(Event event) {
                onDoubleClick(isTop);
            }
        });
    }

    protected EditToolbarButtonMenuHelper getToolbarHelper(boolean top) {
        if (top) {
            return toolbarHelperTop;
        } else {
            return toolbarHelperBottom;
        }
    }

    @Override
    protected void doTableSelectionChanged(boolean isTop) {
        GuiTable otherTable = getGrid(!isTop).getTable();
        otherTable.switchOffEventListeners();
        otherTable.clearSelection();
        otherTable.switchOnEventListeners();
        enableButtonsAndMenu();
    }

    /**
     * Kann überschrieben werden, wenn sich das Standardverhalten, dass der Edit-Dialog beim Doppelklick aufgeht,
     * geändert werden soll.
     *
     * @param isTop
     */
    public void onDoubleClick(boolean isTop) {
        doEdit(isTop);
    }

    /**
     * Buttons in der Toolbar und Menüeinträge im Kontextmenü einfügen hinter dem angegebenen {@link ToolbarButtonAlias}.
     *
     * @param insertAfterButtonAlias Nach diesem Button soll eingefügt werden
     * @param toolbarHelper
     * @param gridTop
     * @param toolbarHelperBottom
     * @param gridBottom
     */
    protected void addSpecialToolbarAndMenuEntries(ToolbarButtonAlias insertAfterButtonAlias, ToolbarButtonMenuHelper toolbarHelper,
                                                   DataObjectGrid gridTop, ToolbarButtonMenuHelper toolbarHelperBottom,
                                                   DataObjectGrid gridBottom) {
    }

    protected abstract void doNew(boolean top);

    protected abstract void doEdit(boolean top);

    protected abstract void doDelete(boolean top);

    /**
     * Löscht alle selektierten {@link EtkDataObject}s zur passenden Klasse aus dem oberen oder unteren Grid.
     *
     * @param top
     * @param dataObjectClass
     * @return
     */
    protected GenericEtkDataObjectList doDeleteDataObjects(boolean top, Class<? extends EtkDataObject> dataObjectClass) {
        GenericEtkDataObjectList deletedDataObjects = new GenericEtkDataObjectList();
        for (List<EtkDataObject> selectedDataObjectList : getGrid(top).getMultiSelection()) {
            for (EtkDataObject selectedDataObject : selectedDataObjectList) {
                if (dataObjectClass.isAssignableFrom(selectedDataObject.getClass())) {
                    deletedDataObjects.delete(selectedDataObject, true, DBActionOrigin.FROM_EDIT);
                }
            }
        }
        saveDataObjectsWithUpdate(deletedDataObjects);

        return deletedDataObjects;
    }

    /**
     * Speichert alle veränderten selektierten {@link EtkDataObject}s zur passenden Klasse aus dem oberen oder unteren Grid
     * (z.B. nach dem Ändern vom Statuswert).
     *
     * @param top
     * @param dataObjectClass
     * @return War mindestens ein {@link EtkDataObject} verändert und wurde deswegen gespeichert?
     */
    protected boolean doSaveDataObjects(boolean top, Class<? extends EtkDataObject> dataObjectClass) {
        GenericEtkDataObjectList modifiedDataObjects = new GenericEtkDataObjectList();
        for (List<EtkDataObject> selectedDataObjectList : getGrid(top).getMultiSelection()) {
            for (EtkDataObject selectedDataObject : selectedDataObjectList) {
                if (dataObjectClass.isAssignableFrom(selectedDataObject.getClass())) {
                    modifiedDataObjects.add(selectedDataObject, DBActionOrigin.FROM_EDIT);
                }
            }
        }
        return saveDataObjectsWithUpdate(modifiedDataObjects);
    }

    protected abstract String getTableName();

    protected void setReadOnly(boolean isReadOnly) {
        this.isReadOnly = isReadOnly;

        if (toolbarHelperTop != null) {
            toolbarHelperTop.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW, gridTop.getContextMenu(), !isReadOnly);
        }
        if (toolbarHelperBottom != null) {
            toolbarHelperBottom.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW, gridBottom.getContextMenu(), !isReadOnly);
        }

        gridTop.updateContextMenu(toolbarHelperTop, isReadOnly);
        gridBottom.updateContextMenu(toolbarHelperBottom, isReadOnly);
    }

    /**
     * (De)aktivert im oberen und unteren Grid jeweils separat, anhand der dort gesetzeten Selektion, die Toolbarbuttons und
     * Kontextmenüeinträge des Grids. Löschen ist nur bei Datensätzen mit Quelle == {@link iPartsImportDataOrigin#IPARTS} aktiv.
     */
    protected void enableButtonsAndMenu() {
        doEnableButtonsAndMenu(true);
        doEnableButtonsAndMenu(false);
    }

    protected void doEnableButtonsAndMenu(boolean isTop) {
        boolean enabled = false;
        boolean enabledSingleSelection = false;
        boolean deleteEnabled = false;
        if (editMode && !isReadOnly) {
            List<List<EtkDataObject>> multiSelection = getGrid(isTop).getMultiSelection();
            if ((multiSelection != null) && !multiSelection.isEmpty()) {
                List<EtkDataObject> selectedDataObjectList = multiSelection.get(0);
                if ((selectedDataObjectList != null) && !selectedDataObjectList.isEmpty()) {
                    EtkDataObject selectedItem = selectedDataObjectList.get(0);
                    enabled = (selectedItem != null);
                }
                enabledSingleSelection = enabled && (multiSelection.size() == 1);

                if (enabled) {
                    deleteEnabled = true;
                    if (getSourceFieldName() != null) {
                        // Es dürfen nur Einträge mit Quelle iParts gelöscht werden
                        checkDeleteLoop:
                        for (List<EtkDataObject> dataObjectList : multiSelection) {
                            for (EtkDataObject dataObject : dataObjectList) {
                                if (dataObject.getTableName().equals(getTableName())) {
                                    String source = dataObject.getFieldValue(getSourceFieldName());
                                    if (!source.equals(iPartsImportDataOrigin.IPARTS.getOrigin())) {
                                        deleteEnabled = false;
                                        break checkDeleteLoop;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if ((toolbarHelperTop != null) && (toolbarHelperBottom != null)) {
            getToolbarHelper(isTop).enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, getGrid(isTop).getContextMenu(), enabledSingleSelection);
            // Zusätzlicher Status für den Löschen Button, damit der Tooltip nur angezeigt wird wenn das Löschen auf Grund
            // der Quelle != iParts deaktiviert wurde
            AbstractGuiToolComponent deleteButton = getToolbarHelper(isTop).getToolbarManager().getButton(EditToolbarButtonAlias.EDIT_DELETE.getAlias());
            if (deleteButton != null) {
                if (enabled && !deleteEnabled) {
                    deleteButton.setTooltip(TranslationHandler.translate("!!Es können nur Daten mit Quelle %s gelöscht werden", iPartsImportDataOrigin.IPARTS.name()));
                } else {
                    deleteButton.setTooltip(TranslationHandler.translate(EditToolbarButtonAlias.EDIT_DELETE.getTooltip()));
                }
            }
            getToolbarHelper(isTop).enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, getGrid(isTop).getContextMenu(), deleteEnabled);
        }
    }


    protected boolean isContextMenuEntryForNewObjectVisible(boolean top) {
        return true;
    }

    protected boolean isContextMenuEntryForEditObjectVisible(boolean top) {
        return true;
    }

    protected boolean isContextMenuEntryForDeleteObjectVisible(boolean top) {
        return true;
    }
}
