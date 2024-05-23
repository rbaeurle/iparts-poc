/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.MenuRunnable;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.AbstractSimpleDataObjectGridForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;

import java.util.List;

/**
 * Abstrakte Einzel-Grid Darstellung für alle möglichen {@link EtkDataObject} inkl. Edit-Operationen
 */
public abstract class AbstractSimpleDataObjectGridEditForm extends AbstractSimpleDataObjectGridForm {

    protected EditToolbarButtonMenuHelper toolbarHelper;
    protected boolean isEditAllowed;

    protected AbstractSimpleDataObjectGridEditForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                   String configKey, String dataObjectGridTitle, String windowTitle, String title, boolean isEditAllowed) {
        super(dataConnector, parentForm, configKey, dataObjectGridTitle, windowTitle, title);
        this.isEditAllowed = isEditAllowed;
        addEditFunctions();
    }

    /**
     * Fügt die Edit-Operationen der Toolbar hinzu
     */
    private void addEditFunctions() {
        if (isEditAllowed) {
            getButtonPanel().setDialogStyle(GuiButtonPanel.DialogStyle.DIALOG);
            toolbarHelper = new EditToolbarButtonMenuHelper(getConnector(), grid.getToolBar());
            toolbarManager = getToolbarHelper().getToolbarManager();

            // NEU als Button in der Toolbar und als Kontextmenu im Grid
            ToolbarButtonMenuHelper.ToolbarMenuHolder holder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_NEW, getUITranslationHandler(), new MenuRunnable() {
                @Override
                public void run(Event event) {
                    doNew();
                }
            });
            grid.getContextMenu().addChild(holder.menuItem);

            // BEARBEITEN als Button in der Toolbar und als Kontextmenu im Grid
            holder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_WORK, getUITranslationHandler(), new MenuRunnable() {
                @Override
                public void run(Event event) {
                    doEdit();
                }
            });
            grid.getContextMenu().addChild(holder.menuItem);
            getToolbarHelper().enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, grid.getContextMenu(), false);

            // LÖSCHEN als Button in der Toolbar und als Kontextmenu im Grid
            holder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_DELETE, getUITranslationHandler(), new MenuRunnable() {
                @Override
                public void run(Event event) {
                    doDelete();
                }
            });
            getToolbarHelper().enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, grid.getContextMenu(), false);
            // Vor den Kopieren-Eintrag setzen
            getToolbarHelper().insertMenuBeforeTableCopyMenu(grid.getContextMenu(), holder.menuItem, true);

            addDoubleClickEventListener();
            addSelectionChangedListener();
        }
    }


    /**
     * Fügt den Selektions-Listener an der Tabelle
     */
    private void addSelectionChangedListener() {
        EventListener tableSelectedListener = new EventListener(Event.TABLE_SELECTION_EVENT) {
            @Override
            public void fire(Event event) {
                if ((event != null) && (event.getSource() != null)) {
                    doTableSelectionChanged();
                }
            }
        };
        grid.getTable().addEventListener(tableSelectedListener);
    }

    /**
     * Setzt den Doppelklick-Listener an der Tabelle
     */
    private void addDoubleClickEventListener() {
        grid.getTable().addEventListener(new EventListener(Event.MOUSE_DOUBLECLICKED_EVENT) {
            @Override
            public void fire(Event event) {
                doEdit();
            }
        });
    }

    private void doTableSelectionChanged() {
        doEnableButtonsAndMenu();
    }

    @Override
    protected void dataToGrid() {
        super.dataToGrid();
        doEnableButtonsAndMenu();
    }

    protected void doEnableButtonsAndMenu() {
        boolean enabled = false;
        boolean enabledSingleSelection = false;
        if (isEditAllowed) {
            List<List<EtkDataObject>> multiSelection = grid.getMultiSelection();
            if ((multiSelection != null) && !multiSelection.isEmpty()) {
                List<EtkDataObject> selectedDataObjectList = multiSelection.get(0);
                if ((selectedDataObjectList != null) && !selectedDataObjectList.isEmpty()) {
                    EtkDataObject selectedItem = selectedDataObjectList.get(0);
                    enabled = (selectedItem != null);
                }
                enabledSingleSelection = enabled && (multiSelection.size() == 1);
            }
        }

        if (getToolbarHelper() != null) {
            getToolbarHelper().enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, grid.getContextMenu(), enabledSingleSelection);
            boolean deleteEnabled = isDeletionEnabled();
            getToolbarHelper().enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, grid.getContextMenu(), deleteEnabled);
        }
    }

    public EditToolbarButtonMenuHelper getToolbarHelper() {
        return toolbarHelper;
    }

    public boolean isEditAllowed() {
        return isEditAllowed;
    }

    /**
     * Check, ob das Löschen der aktuellen Selektion erlaubt ist
     *
     * @return
     */
    protected abstract boolean isDeletionEnabled();

    protected abstract void doDelete();

    protected abstract void doEdit();

    protected abstract void doNew();
}
