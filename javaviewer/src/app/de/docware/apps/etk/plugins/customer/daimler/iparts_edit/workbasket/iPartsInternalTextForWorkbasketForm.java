/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.MenuRunnable;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.EtkDbObjectsLayer;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsUserAdminDb;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetSource;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataInternalText;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataInternalTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataInternalTextList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketInternalTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditInternalTextForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsGuiInternalTextField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditDataObjectGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper.WorkBasketInternalTextCache;
import de.docware.framework.modules.config.db.datatypes.DatatypeUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolbar;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog zur Darstellung der Internen Texte für alle Arbeitsvorräte
 */
public class iPartsInternalTextForWorkbasketForm extends AbstractJavaViewerForm implements iPartsConst {

    public static boolean saveDataObjectsWithTechnicalChangeSet(EtkProject project, EtkDataObjectList<? extends EtkDataObject> dataObjectList) {
        final GenericEtkDataObjectList genericList = new GenericEtkDataObjectList();
        genericList.addAll(dataObjectList, DBActionOrigin.FROM_EDIT);

        final VarParam<Boolean> result = new VarParam<>(true);
        if (iPartsRevisionChangeSet.saveDataObjectListWithChangeSet(project, genericList, iPartsChangeSetSource.GENERAL_WORKBASKET)) {
            final EtkDbObjectsLayer dbLayer = project.getDbLayer();

            dbLayer.startTransaction();
            dbLayer.startBatchStatement();
            // direkt in der Tabelle DA_INTERNAL_TEXT ohne aktive ChangeSets speichern
            project.executeWithoutActiveChangeSets(new Runnable() {
                @Override
                public void run() {
                    try {
                        genericList.saveToDB(project);
                        dbLayer.endBatchStatement();
                        dbLayer.commit();
                    } catch (Exception e) {
                        dbLayer.cancelBatchStatement();
                        dbLayer.rollback();
                        Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                        MessageDialog.showError("!!Fehler beim Speichern.");
                        result.setValue(false);
                    }
                }
            }, false); // fireDataChangedEvent ist hier nicht notwendig, weil der Update auf anderem Wege erfolgt
        } else {
            result.setValue(false);
        }
        return result.getValue();
    }

    public static boolean showInternalTextFormForWorkBasket(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                            iPartsWorkBasketInternalTextId wbIntTextId) {
        iPartsInternalTextForWorkbasketForm dlg = new iPartsInternalTextForWorkbasketForm(dataConnector, parentForm,
                                                                                          wbIntTextId);
        dlg.setTitle(TranslationHandler.translate("!!Internen Text bearbeiten bei \"%1\"",
                                                  TranslationHandler.translate(wbIntTextId.getWbType().getTitle())));
        String subTitle = "!!SAA: \"%1\"";
        String value = wbIntTextId.getSaaBkKemValue();
        if (wbIntTextId.isKEM()) {
            subTitle = "!!KEM: \"%1\"";
        } else {
            value = iPartsNumberHelper.formatPartNo(dataConnector.getProject(), value);
        }
        dlg.setSubTitle(TranslationHandler.translate(subTitle, value));
        dlg.fillDataGrid(false);
        dlg.showModal();
        return dlg.isModified();
    }

    private static final boolean DEFAULT_SORT_ASCENDING = false;
    private static final int MAX_TEXT_LENGTH = 100;
    public static final String INTERNAL_TEXT_FOR_WB_DATA_NAME = "/InternalTextForWorkBasket";
    public static final String CONFIG_KEY_INTERNAL_TEXT_FOR_WB_DATA = "Plugin/iPartsEdit" + INTERNAL_TEXT_FOR_WB_DATA_NAME;

    private String currentUser;
    private EditDataObjectGrid dataGrid;
    private iPartsWorkBasketInternalTextId wbIntTextId;
    private boolean isModified;

    /**
     * Erzeugt eine Instanz von iPartsInternalTextForWorkbasketForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public iPartsInternalTextForWorkbasketForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                               iPartsWorkBasketInternalTextId wbIntTextId) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.wbIntTextId = wbIntTextId;
        this.isModified = false;
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        // Den angemeldeten Benutzer für die Vergleiche zum Aktivieren der Buttons nur einmalig ermitteln.
        currentUser = iPartsUserAdminDb.getLoginUserName();

        dataGrid = new EditDataObjectGrid(getConnector(), this) {
            @Override
            protected void onTableSelectionChanged(Event event) {
                doEnableButtons();
            }

            // Doppelklick in eine Zeile, die dem angemeldeten Benutzer gehört, startet die Edit-Funktion.
            @Override
            protected void onCellDblClicked(int row, int col, Event event) {
                doEditInternalText(null);
            }

            @Override
            protected String getVisualValueOfField(String tableName, String fieldName, EtkDataObject objectForTable) {
                if (tableName.equals(iPartsConst.TABLE_DA_INTERNAL_TEXT)) {
                    if (fieldName.equals(iPartsConst.FIELD_DIT_ATTACHMENT)) {
                        byte[] attachment = objectForTable.getFieldValueAsBlob(iPartsConst.FIELD_DIT_ATTACHMENT);
                        boolean hasAttachments = (attachment != null) && (attachment.length > 0);
                        if (hasAttachments) {
                            return DatatypeUtils.makeImgTag(DatatypeUtils.getImageForValue(4, true), true);
                        }
                        return "";
                    } else if (fieldName.equals(iPartsConst.FIELD_DIT_TEXT)) {
                        if (objectForTable != null) {
                            return getVisObject().asHtml(tableName, fieldName,
                                                         StrUtils.makeAbbreviation(objectForTable.getAttributeForVisObject(fieldName).getAsString(), MAX_TEXT_LENGTH),
                                                         getProject().getDBLanguage()).getStringResult();
                        }
                        return "";
                    }
                }
                return super.getVisualValueOfField(tableName, fieldName, objectForTable);
            }

            @Override
            protected void createToolbarButtons(GuiToolbar toolbar) {
                boolean isEditable = isEditAllowed();
                ToolbarButtonMenuHelper.ToolbarMenuHolder holder;
                holder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_NEW, getUITranslationHandler(), new MenuRunnable() {
                    @Override
                    public void run(Event event) {
                        doCreateInternalText(event);
                    }
                });
                getContextMenuTable().addChild(holder.menuItem);
                if (!isEditable) {
                    getToolbarHelper().hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW, getContextMenuTable());
                }

                holder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_WORK, getUITranslationHandler(), new MenuRunnable() {
                    @Override
                    public void run(Event event) {
                        doEditInternalText(event);
                    }
                });
                getContextMenuTable().addChild(holder.menuItem);
                if (!isEditable) {
                    holder.menuItem.setText("!!Anzeigen");
                    holder.toolbarButton.setTooltip(TranslationHandler.translate("!!Anzeigen"));
                }

                holder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_DELETE, getUITranslationHandler(), new MenuRunnable() {
                    @Override
                    public void run(Event event) {
                        doDeleteInternalText(event);
                    }
                });
                getContextMenuTable().addChild(holder.menuItem);
                if (!isEditable) {
                    getToolbarHelper().hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, getContextMenuTable());
                }
            }
        };

        dataGrid.setPageSplitNumberOfEntriesPerPage(iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE);

        ConstraintsBorder constraints = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
        dataGrid.getGui().setConstraints(constraints);
        mainWindow.panelMain.addChild(dataGrid.getGui());
        doEnableButtons();

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

    public void setSubTitle(String subTitle) {
        mainWindow.title.setSubtitle(subTitle);
    }


    /**
     * Sucht sich aus den DisplayFields die Zeitstempelspalte (zum späteren Sortieren).
     *
     * @return
     */
    private int getDefaultSortColumn() {
        // Die Daten immer initial nach Änderungsdatum sortiert anzeigen, falls die Spalte vorhanden ist
        EtkDisplayFields displayFields = dataGrid.getDisplayFields();
        return displayFields.getIndexOfFeld(iPartsConst.TABLE_DA_INTERNAL_TEXT, iPartsConst.FIELD_DIT_CHANGE_DATE, false);
    }

    /**
     * Füllt das Grid mit DisplayFields, Daten, Sortierung.
     *
     * @param withUpdatePartListEntry
     */
    public void fillDataGrid(boolean withUpdatePartListEntry) {
        // Die Sortierung merken
        int sortColumn = dataGrid.getTable().getSortColumn();
        boolean isAscending = dataGrid.getTable().isSortAscending();

        // Wenn die DisplayFields noch nicht gesetzt sind, ...
        if (dataGrid.getDisplayFields() == null) {
            // ...  das Grid neu aufbauen ...
            EtkDisplayFields displayFields = getDisplayFields(CONFIG_KEY_INTERNAL_TEXT_FOR_WB_DATA);
            dataGrid.setDisplayFields(displayFields);
            // ... die Default-Sortierung festlegen ...
            sortColumn = getDefaultSortColumn();
            isAscending = false;
            // ... und die Daten aus der Datenbank laden.
        }

        // Sonderfall: leeres Grid!
        if (sortColumn < 0) {
            sortColumn = getDefaultSortColumn();
            isAscending = DEFAULT_SORT_ASCENDING;
        }

        List<iPartsDataInternalText> internalTextList = WorkBasketInternalTextCache.getInstance(getProject()).getInternalText(wbIntTextId);
        fillGrid(internalTextList, sortColumn, isAscending);
        doEnableButtons();
    }

    private void fillGrid(List<iPartsDataInternalText> internalTextList, int sortColumn, boolean isAscending) {

        // Nun das Grid leeren und neu befüllen
        dataGrid.clearGrid();
        for (iPartsDataInternalText dataInternalText : internalTextList) {
            dataGrid.addObjectToGrid(dataInternalText);
        }
        dataGrid.showNoResultsLabel(internalTextList.isEmpty());

        // Sortierung wiederherstellen
        if (sortColumn >= 0) {
            dataGrid.getTable().sortRowsAccordingToColumn(sortColumn, isAscending);
        }
    }

    /**
     * (De-)aktiviert Steuerelemente der Oberfläche in Abhängigkeit der gewählten Einträge,
     * des angemeldeten Benutzers und des Autorenauftrags.
     */
    private void doEnableButtons() {
        boolean isEditAllowed = isEditAllowed();

        // Der Neu-Button ist immer aktiv, wenn ein Autorenauftrag aktiv ist.
        dataGrid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW, isEditAllowed);

        // Einmalig die selektierte Liste holen und mehrfach darüber iterieren
        List<List<EtkDataObject>> selectedInternalTexts = dataGrid.getMultiSelection();

        // (De-)Aktivieren des Bearbeiten-Buttons.
        // Den Bearbeiten-Button nur aktivieren, wenn genau eine Zeile des aktuellen Benutzers ausgewählt ist.
        boolean editInternalTextAllowed = false;
        if (selectedInternalTexts.size() == 1) {
            if (isEditAllowed) {
                editInternalTextAllowed = true;
                checkEditLoop:
                for (List<EtkDataObject> dataObjectList : selectedInternalTexts) {
                    for (EtkDataObject oneDataObject : dataObjectList) {
                        if (oneDataObject instanceof iPartsDataInternalText) {
                            if (!((iPartsDataInternalText)oneDataObject).getUserId().equals(currentUser)) {
                                editInternalTextAllowed = false;
                                break checkEditLoop;
                            }
                        }
                    }
                }
            }
        }
        // Bei aktivem Auftrag Auftrag den eigenen Text bearbeiten, sonst nur Anzeigen
        dataGrid.changeToolbarButtonAndMenuTooltip(EditToolbarButtonAlias.EDIT_WORK, editInternalTextAllowed
                                                                                     ? EditToolbarButtonAlias.EDIT_WORK.getTooltip()
                                                                                     : "!!Anzeigen");

        dataGrid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, selectedInternalTexts.size() == 1);

        // (De-)Aktivieren des Lösch-Buttons.
        boolean setDeleteButtonEnabled = false;
        if (isEditAllowed) {
            if (!selectedInternalTexts.isEmpty()) {
                setDeleteButtonEnabled = true;
                checkDeleteLoop:
                for (List<EtkDataObject> dataObjectList : selectedInternalTexts) {
                    for (EtkDataObject oneDataObject : dataObjectList) {
                        if (oneDataObject instanceof iPartsDataInternalText) {
                            if (!((iPartsDataInternalText)oneDataObject).getUserId().equals(currentUser)) {
                                setDeleteButtonEnabled = false;
                                break checkDeleteLoop;
                            }
                        }
                    }
                }
            }
        }
        // Lösch-Button nur auf enabled setzen, wenn ein Autorenauftrag aktiv ist.
        dataGrid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, setDeleteButtonEnabled);
    }

    /**
     * Checkt den Konfig-Key und gibt ggf. die Default-Display-Fields zurück.
     *
     * @param configKey
     * @return
     */
    protected List<EtkDisplayField> createDefaultDisplayFields(String configKey) {
        if (configKey.equals(CONFIG_KEY_INTERNAL_TEXT_FOR_WB_DATA)) {
            return createInternalTextTableDataDefaultDisplayFields();
        }
        return null;
    }

    /**
     * Gibt die Default-Display-Fields zurück.
     *
     * @return
     */
    private List<EtkDisplayField> createInternalTextTableDataDefaultDisplayFields() {
        List<EtkDisplayField> defaultDisplayFields = new ArrayList<EtkDisplayField>();

        EtkDisplayField displayField = new EtkDisplayField(iPartsConst.TABLE_DA_INTERNAL_TEXT, iPartsConst.FIELD_DIT_U_ID, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_INTERNAL_TEXT, iPartsConst.FIELD_DIT_CHANGE_DATE, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_INTERNAL_TEXT, iPartsConst.FIELD_DIT_TEXT, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_INTERNAL_TEXT, iPartsConst.FIELD_DIT_ATTACHMENT, false, false);
        defaultDisplayFields.add(displayField);

        return defaultDisplayFields;
    }

    /**
     * Neu Anlegen eines {@link iPartsDataInternalText} internen Text-Objektes über das ChangeSet.
     *
     * @param event
     */
    private void doCreateInternalText(Event event) {
        iPartsDataInternalTextId id;
        id = new iPartsDataInternalTextId(currentUser, wbIntTextId);
        iPartsDataInternalText dataInternalText = new iPartsDataInternalText(parentForm.getProject(), id);
        dataInternalText.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        dataInternalText = EditInternalTextForm.createInternalTextForm(parentForm, dataInternalText);
        if (dataInternalText != null) {
            saveDataObjectWithUpdate(dataInternalText);
        }
    }

    /**
     * Editieren {@link iPartsDataInternalText} internen Text-Objektes.
     * Per Definition kann nur (!)EINE(!) Zeile selektiert sein!
     *
     * @param event
     */
    private void doEditInternalText(Event event) {
        boolean isEdit = isEditAllowed();
        // Den gewählten Eintrag editieren, es kann nur einen geben!
        List<EtkDataObject> selectedInternalText = dataGrid.getSelection();
        for (EtkDataObject oneDataObject : selectedInternalText) {
            if (oneDataObject instanceof iPartsDataInternalText) {
                iPartsDataInternalText selectedDataInternalText = (iPartsDataInternalText)oneDataObject;
                iPartsDataInternalText dataInternalText = iPartsGuiInternalTextField.editShowInternalText(this, selectedDataInternalText,
                                                                                                          isEdit && selectedDataInternalText.getUserId().equals(currentUser));
                if (dataInternalText != null) {
                    saveDataObjectWithUpdate(dataInternalText);
                }
                break;
            }
        }
    }

    /**
     * Löschen {@link iPartsDataInternalText} interner Text-Objektes über das ChangeSet.
     * Es kann mehrere selektierte Zeilen geben.
     *
     * @param event
     */
    private void doDeleteInternalText(Event event) {
        // Die gewählten Einträge löschen, bzw. in die delete-Liste hängen.
        iPartsDataInternalTextList deletedDataInternalTexts = new iPartsDataInternalTextList();
        List<List<EtkDataObject>> selectedInternalTexts = dataGrid.getMultiSelection();
        for (List<EtkDataObject> dataObjectList : selectedInternalTexts) {
            for (EtkDataObject oneDataObject : dataObjectList) {
                if (oneDataObject instanceof iPartsDataInternalText) {
                    deletedDataInternalTexts.delete((iPartsDataInternalText)oneDataObject, true, DBActionOrigin.FROM_EDIT);
                }
            }
        }

        saveDataObjectsWithUpdate(deletedDataInternalTexts);
    }

    protected boolean saveDataObjectWithUpdate(EtkDataObject dataObject) {
        // Konstruktions-Stückliste
        GenericEtkDataObjectList modifiedDataObjects = new GenericEtkDataObjectList();
        modifiedDataObjects.add(dataObject, DBActionOrigin.FROM_EDIT);
        // Techn.ChangeSet anlegen
        return saveDataObjectsWithUpdate(modifiedDataObjects);
    }

    protected boolean saveDataObjectsWithUpdate(EtkDataObjectList<? extends EtkDataObject> dataObjectList) {
        if (saveDataObjectsWithTechnicalChangeSet(getProject(), dataObjectList)) {
            WorkBasketInternalTextCache.updateWorkBasketCache(getProject(), wbIntTextId.getWbType());
            fillDataGrid(true);
            isModified = true;
            return true;
        } else {
            return false;
        }
    }

    public boolean isEditAllowed() {
        return true;  //isEditRelatedInfo || isEditableInConstruction;
    }

    public boolean isModified() {
        return isModified;
    }

    protected EtkDisplayFields getDisplayFields(String configKey) {
        EtkDisplayFields displayResultFields = new EtkDisplayFields();
        displayResultFields.load(getConfig(), configKey);

        if (displayResultFields.size() == 0) {
            List<EtkDisplayField> defaultDisplayFields = createDefaultDisplayFields(configKey);
            if (defaultDisplayFields != null) {
                for (EtkDisplayField defaultDisplayField : defaultDisplayFields) {
                    displayResultFields.addFeld(defaultDisplayField);
                }
            }

            displayResultFields.loadStandards(getConfig());
        }

        return displayResultFields;
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