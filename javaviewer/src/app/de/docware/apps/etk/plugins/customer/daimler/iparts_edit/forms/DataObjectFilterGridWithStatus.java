/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.DataObjectFilterGrid;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.sql.TableAndFieldName;

import java.util.List;

/**
 * Erweiterung des {@link DataObjectFilterGrid}s mit Status.
 */
public abstract class DataObjectFilterGridWithStatus extends DataObjectFilterGrid {

    protected GuiMenuItem statusContextMenu;
    private String tableAndFieldName;
    private boolean isReadOnly;

    public DataObjectFilterGridWithStatus(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                          String statusTableName, String statusFieldName) {
        this(dataConnector, parentForm, true, statusTableName, statusFieldName);
    }

    public DataObjectFilterGridWithStatus(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                          boolean createContextMenu, String statusTableName, String statusFieldName) {
        super(dataConnector, parentForm, createContextMenu);
        if ((statusTableName != null) && (statusFieldName != null)) {
            tableAndFieldName = TableAndFieldName.make(statusTableName, statusFieldName);
        }
        createStatusMenuItem();
        setStatusContextMenuText();
    }

    /**
     * Zusätzlich zum Kontextmenüeintrag für den Status auch das entsprechende Feld im Grid hinzufügen, damit der Bezug von
     * Kontextmenü und Datensatz sichtbar ist.
     *
     * @param displayFields
     */
    @Override
    public void setDisplayFields(EtkDisplayFields displayFields) {
        if (isStatusTableAndFieldInitialized() && ((displayFields != null) && !displayFields.contains(tableAndFieldName, false))) {
            EtkDisplayField statusField = new EtkDisplayField(tableAndFieldName, false, false);
            statusField.loadStandards(getConfig());
            displayFields.addFeld(statusField);
        }
        super.setDisplayFields(displayFields);
    }

    /**
     * Setzt den Text für den Kontextmenüeintrag 'Status ändern ...' im oberen und unteren Grid abhängig von den jeweiligen
     * Display Fields.
     * Hier wird davon ausgegangen, dass in beiden Tabellen die gleichen Datenbank-Tabellen und die gleichen Felder für
     * das Kontextmenü verwendet werden.
     */
    protected void setStatusContextMenuText() {
        if (isStatusTableAndFieldInitialized() && (getDisplayFields() != null) && (statusContextMenu != null)) {
            EtkDisplayField displayField = getDisplayFields().getFeldByName(getStatusTableName(), getStatusFieldName(), false);
            if (displayField != null) {
                String text = displayField.getText().getTextByNearestLanguage(getProject().getViewerLanguage(), getProject().getDataBaseFallbackLanguages());
                if (StrUtils.isValid(text)) {
                    text = TranslationHandler.translate("%1 ändern...", text);
                    statusContextMenu.setText(text);
                }
            }
        }
    }

    /**
     * Erstellt den Kontextmenüeintrag zum Ändern von Statuswerten.
     */
    protected void createStatusMenuItem() {
        if (isStatusTableAndFieldInitialized() && (toolbarHelper != null)) {
            GuiMenuItem menuItem = toolbarHelper.createMenuEntry("statusMenu", "!!Status ändern...", null, null, getUITranslationHandler());
            // Kontextmenu zum Status Ändern mit allen möglichen Werte erzeugen. Diese werden dann je nach Selektion ausgeblendet
            for (final iPartsDataReleaseState releaseState : iPartsDataReleaseState.values()) {
                if ((releaseState != iPartsDataReleaseState.IMPORT_DEFAULT) && (releaseState != iPartsDataReleaseState.UNKNOWN)
                    && !releaseState.isReadOnly()) {
                    GuiMenuItem statusSubMenu = new GuiMenuItem();
                    statusSubMenu.setUserObject(releaseState);
                    statusSubMenu.setText(releaseState.getDescription(getProject()));
                    statusSubMenu.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                        @Override
                        public void fire(Event event) {
                            changeStatus(releaseState);
                        }
                    });
                    menuItem.addChild(statusSubMenu);
                }
            }
            statusContextMenu = menuItem;
        }
    }

    /**
     * Setzt den übergebenen Statuswert am ausgewählten DataObject und benachrichtigt das Vater-{@link AbstractJavaViewerForm}
     * per {@link AbstractJavaViewerForm#updateData(AbstractJavaViewerForm, boolean)} über Änderungen.
     *
     * @param releaseState
     */
    public void changeStatus(iPartsDataReleaseState releaseState) {
        // Anzeige aktualisieren
        if (isStatusTableAndFieldInitialized()) {
            boolean updateData = false;
            int[] oldSelectedRowIndices = getTable().getSelectedRowIndices();
            for (List<EtkDataObject> etkDataObjectList : getMultiSelection()) {
                for (EtkDataObject etkDataObject : etkDataObjectList) {
                    if (etkDataObject.getTableName().equals(getStatusTableName())) {
                        if (etkDataObject.attributeExists(getStatusFieldName())) {
                            if (setStatus(etkDataObject, releaseState)) {
                                updateData = true;
                            }
                        }
                    }
                }
            }

            if (updateData) {
                statusChanged();
                getTable().setSelectedRows(oldSelectedRowIndices, true, true, true); // Selektion wiederherstellen
            }
        }
    }

    /**
     * Setzt den Status vom übergebenen {@code dataObject} auf {@code releaseState}.
     *
     * @param dataObject
     * @param releaseState
     * @return {@code true} falls das {@code dataObject} durch das Setzen vom Status verändert wurde
     */
    protected boolean setStatus(EtkDataObject dataObject, iPartsDataReleaseState releaseState) {
        dataObject.setFieldValue(getStatusFieldName(), releaseState.getDbValue(), DBActionOrigin.FROM_EDIT);
        return dataObject.isModified();
    }

    /**
     * Ermittelt für die aktuelle Selektion den Status. Dieser wird verwendet, um die nächsten möglichen Status zu ermitteln.
     *
     * @param selection
     * @return <code>null</code> wenn die Datensätze verschiedene Status haben oder der Status readOnly ist
     */
    protected iPartsDataReleaseState getStatusFromSelection(List<List<EtkDataObject>> selection) {
        if ((selection == null) || selection.isEmpty()) {
            return null;
        }

        iPartsDataReleaseState resultState = null;
        String statusTableName = getStatusTableName();
        String statusFieldName = getStatusFieldName();
        for (List<EtkDataObject> etkDataObjects : selection) {
            for (EtkDataObject etkDataObject : etkDataObjects) {
                if (etkDataObject.getTableName().equals(statusTableName)) {
                    if (etkDataObject.attributeExists(statusFieldName)) {
                        String fieldValue = etkDataObject.getFieldValue(statusFieldName);
                        iPartsDataReleaseState currentState = iPartsDataReleaseState.getTypeByDBValue(fieldValue);
                        if (currentState.isReadOnly()) { // Status darf nicht verändert werden
                            return null;
                        }
                        if ((resultState != null) && (resultState != currentState)) { // Status ist unterschiedlich
                            return null;
                        }
                        resultState = currentState;
                    }
                }
            }
        }
        return resultState;
    }

    /**
     * Passt das Status-Kontextmenü entsprechend der aktuellen Tabellen-Selektion und der möglichen Statusübergänge an
     */
    public void handleTableSelectionChangeForStatusContextMenu() {
        if (((statusContextMenu != null) && !isReadOnly) && isStatusTableAndFieldInitialized()) {
            // Prüfen, ob eine Statusänderung zulässig ist und ggf. das Kontextmenü disablen
            // Statusänderungen an Varianten zu Teil und Varianten werden in den entsprechenden TwoGridForms nochmal
            // unabhängig von dieser Funktion abgehandelt (siehe enableVariantTablesButtonsAndMenu() und enableVariantsButtonsAndMenu())

            iPartsDataReleaseState state = getStatusFromSelection(getMultiSelection());
            if (state != null) {
                List<iPartsDataReleaseState> nextEditStates = state.getNextEditStates();
                for (AbstractGuiControl statusSubMenuItem : statusContextMenu.getChildren()) {
                    boolean subMenuVisible = false;
                    Object statusMenuItemUserObject = statusSubMenuItem.getUserObject();
                    if ((statusMenuItemUserObject instanceof iPartsDataReleaseState) &&
                        nextEditStates.contains(statusMenuItemUserObject)) {
                        subMenuVisible = true;
                    }
                    statusSubMenuItem.setVisible(subMenuVisible);
                }
            }
            statusContextMenu.setEnabled(state != null);
        }
    }

    @Override
    protected void onTableSelectionChanged(Event event) {
        super.onTableSelectionChanged(event);
        handleTableSelectionChangeForStatusContextMenu();
    }

    public boolean isStatusTableAndFieldInitialized() {
        if (tableAndFieldName != null) {
            return StrUtils.isValid(getStatusFieldName(), getStatusTableName());
        }
        return false;
    }

    public GuiMenuItem getStatusContextMenu() {
        return statusContextMenu;
    }

    public String getStatusFieldName() {
        return TableAndFieldName.getFieldName(tableAndFieldName);
    }

    public String getStatusTableName() {
        return TableAndFieldName.getTableName(tableAndFieldName);
    }

    /**
     * Wird aufgerufen, wenn sich der Status für mindestens einen Eintrag geändert hat, damit diese Änderungen gespeichert
     * werden können.
     */
    protected abstract void statusChanged();

    @Override
    public void modifyContextMenu(ToolbarButtonMenuHelper toolbarHelper) {
        GuiMenuItem statusContextMenu = getStatusContextMenu();
        if (statusContextMenu != null) {
            toolbarHelper.insertMenuBeforeTableCopyMenu(getContextMenu(), statusContextMenu, false);
        }
    }

    @Override
    public void updateContextMenu(ToolbarButtonMenuHelper toolbarHelper, boolean formIsReadOnly) {
        this.isReadOnly = formIsReadOnly;
        GuiMenuItem statusContextMenu = getStatusContextMenu();
        if (statusContextMenu != null) {
            if (formIsReadOnly) {
                statusContextMenu.setEnabled(false);
            }
        }
    }
}
