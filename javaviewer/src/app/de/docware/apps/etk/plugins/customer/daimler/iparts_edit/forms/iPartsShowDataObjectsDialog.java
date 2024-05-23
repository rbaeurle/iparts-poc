/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFieldKeyNormal;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.DataObjectFilterGrid;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSaa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataKemResponseList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataNutzDokAnnotationList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataNutzDokRemark;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsNutzDokKEMId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsNutzDokSAAId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketInternalTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataNutzDokKEM;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataNutzDokSAA;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.iPartsWSWorkBasketItem;
import de.docware.framework.modules.config.db.DBConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.table.GuiTable;
import de.docware.framework.modules.gui.controls.table.TableSelectionMode;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerOptions;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.resources.FrameworkImage;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.Collection;
import java.util.List;

/**
 * Dialog zur Anzeige belibiger EtkDataObjects in einem Grid, gesteuert über EtkDisplayFields
 */
public class iPartsShowDataObjectsDialog extends AbstractJavaViewerForm implements iPartsConst {

    public static void showNutzDokAnnotations(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                              String saaOrKemNo, String refType) {
        EtkProject project = dataConnector.getProject();
        List<EtkDataObject> dataObjects = new DwList<>();

        EtkDisplayFields displayFields = new EtkDisplayFields();
        EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_NUTZDOK_ANNOTATION, FIELD_DNA_DATE, false, false);
        displayFields.addFeld(displayField);
        displayField = new EtkDisplayField(TABLE_DA_NUTZDOK_ANNOTATION, FIELD_DNA_ETS, false, false);
        displayField.setColumnFilterEnabled(true);
        displayFields.addFeld(displayField);
        displayField = new EtkDisplayField(TABLE_DA_NUTZDOK_ANNOTATION, FIELD_DNA_LFDNR, false, false);
        displayFields.addFeld(displayField);
        displayField = new EtkDisplayField(TABLE_DA_NUTZDOK_ANNOTATION, FIELD_DNA_AUTHOR, false, false);
        displayFields.addFeld(displayField);
        displayField = new EtkDisplayField(TABLE_DA_NUTZDOK_ANNOTATION, FIELD_DNA_ANNOTATION, false, false);
        displayFields.addFeld(displayField);
        displayFields.loadStandards(project.getConfig());

        iPartsDataNutzDokAnnotationList dataList = iPartsDataNutzDokAnnotationList.getAllEntriesForType(project, saaOrKemNo, refType);
        dataObjects.addAll(dataList.getAsList());

        iPartsShowDataObjectsDialog dlg = new iPartsShowDataObjectsDialog(dataConnector, parentForm,
                                                                          displayFields, dataObjects, false);
        String displaySaaOrKemNo = saaOrKemNo;
        if (refType.equals(iPartsWSWorkBasketItem.TYPE.SAA.name())) {
            displaySaaOrKemNo = iPartsNumberHelper.formatPartNo(dataConnector.getProject(), saaOrKemNo);
        }
        dlg.setTitle(TranslationHandler.translate("!!Nutzdok Bemerkungen für %1 \"%2\"", refType, displaySaaOrKemNo));
        dlg.showModal();
    }

    public static void showNutzDokKemMetadatas(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                               iPartsWorkBasketInternalTextId wbIntTextId) {
        EtkProject project = dataConnector.getProject();
        List<EtkDataObject> dataObjects = new DwList<>();
        String tableName;
        String key;
        if (wbIntTextId.isSaaOrBk()) {
            iPartsDataNutzDokSAA saaData = new iPartsDataNutzDokSAA(project, new iPartsNutzDokSAAId(wbIntTextId.getSaaBkKemValue()));
            if (saaData.existsInDB()) {
                dataObjects.add(saaData);
            }
            tableName = saaData.getTableName();
            key = "!!Nutzdok-SAA Metadaten";
        } else {
            iPartsDataNutzDokKEM kemData = new iPartsDataNutzDokKEM(project, new iPartsNutzDokKEMId(wbIntTextId.getSaaBkKemValue()));
            if (kemData.existsInDB()) {
                dataObjects.add(kemData);
            }
            tableName = kemData.getTableName();
            key = "!!Nutzdok-KEM Metadaten";
        }

        EtkDisplayFields displayFields = getDisplayFieldsFromTableDef(project, tableName);

        iPartsShowDataObjectsDialog dlg = new iPartsShowDataObjectsDialog(dataConnector, parentForm,
                                                                          displayFields, dataObjects, false);
        dlg.setTitle(TranslationHandler.translate(key));
        dlg.showModal();
    }

    public static iPartsDataNutzDokRemark showNutzDokRemarks(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                             List<iPartsDataNutzDokRemark> nutzDokRemarkList) {
        EtkProject project = dataConnector.getProject();
        List<EtkDataObject> dataObjects = new DwList<>();
        dataObjects.addAll(nutzDokRemarkList);

        EtkDisplayFields displayFields = new EtkDisplayFields();
        EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_NUTZDOK_REMARK, FIELD_DNR_ID, false, false);
        displayFields.addFeld(displayField);
        displayField = new EtkDisplayField(TABLE_DA_NUTZDOK_REMARK, FIELD_DNR_LAST_MODIFIED, false, false);
        displayFields.addFeld(displayField);
        displayFields.loadStandards(project.getConfig());

        iPartsShowDataObjectsDialog dlg = new iPartsShowDataObjectsDialog(dataConnector, parentForm,
                                                                          displayFields, dataObjects, false);
        dlg.setTitle(TranslationHandler.translate("!!Nutzdok-Bemerkungstexte"));
        dlg.changeButtonPanelDialogStyle(GuiButtonPanel.DialogStyle.DIALOG);
        ModalResult modalResult = dlg.showModal();
        if (modalResult == ModalResult.OK) {
            EtkDataObject dataObject = dlg.getSelectedObject();
            if ((dataObject != null) && (dataObject instanceof iPartsDataNutzDokRemark)) {
                return ((iPartsDataNutzDokRemark)dataObject);
            }
        }
        return null;
    }

    public static void showEpepResponse(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                        String kemNo, iPartsDataKemResponseList kemResponseList) {
        List<EtkDataObject> dataObjects = new DwList<>();
        dataObjects.addAll(kemResponseList.getAsList());
        iPartsShowDataObjectsDialog dlg = new iPartsShowDataObjectsDialog(dataConnector, parentForm,
                                                                          getDisplayFieldsFromTableDef(dataConnector.getProject(), TABLE_DA_KEM_RESPONSE_DATA),
                                                                          dataObjects, false);
        dlg.setTitle(TranslationHandler.translate("!!(ePEP) Rückmeldedaten zur KEM \"%1\"", kemNo));
        dlg.showModal();
    }

    public static PartListEntryId showGototRetailElements(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                          Collection<PartListEntryId> partListEntryIdList, boolean synchronEvent) {
        EtkProject project = dataConnector.getProject();
        List<EtkDataObject> showList = new DwList<>();
        partListEntryIdList.forEach((partlistEntryId) -> {
            EtkDataPartListEntry partListEntry = EtkDataObjectFactory.createDataPartListEntry(project, partlistEntryId);
            partListEntry.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            showList.add(partListEntry);
        });

        EtkDisplayFields displayFields = new EtkDisplayFields();
        EtkDisplayField displayField = new EtkDisplayField(TABLE_KATALOG, FIELD_K_VARI, false, false);
        EtkMultiSprache text = new EtkMultiSprache("!!TUs im Retail", project.getConfig().getDatabaseLanguages());
        displayField.setText(text);
        displayField.setDefaultText(false);
        displayFields.addFeld(displayField);
        displayFields.loadStandards(project.getConfig());

        iPartsShowDataObjectsDialog dlg = new iPartsShowDataObjectsDialog(dataConnector, parentForm, displayFields, showList,
                                                                          synchronEvent);
        dlg.setTitle("!!TUs im Retail");
        dlg.addContextMenu("gotoRetailMenu", "!!Gehe zum Retail-TU", null);
        dlg.changeButtonPanelDialogStyle(GuiButtonPanel.DialogStyle.DIALOG);
        ModalResult modalResult = dlg.showModal();
        if (modalResult == ModalResult.OK) {
            EtkDataObject dataObject = dlg.getSelectedObject();
            if ((dataObject != null) && (dataObject instanceof EtkDataPartListEntry)) {
                return ((EtkDataPartListEntry)dataObject).getAsId();
            }
        }
        return null;
    }

    public static PartListEntryId showGototRetailElements(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                          List<EtkDataPartListEntry> partListEntryList, List<String> displayTableFieldNames,
                                                          List<String> displayFieldText, boolean synchronEvent) {
        EtkProject project = dataConnector.getProject();
        List<EtkDataObject> showList = new DwList<>();
        showList.addAll(partListEntryList);

        EtkDisplayFields displayFields = new EtkDisplayFields();
        int cnt = 0;
        for (String tableFieldName : displayTableFieldNames) {
            EtkDisplayField displayField = new EtkDisplayField(TableAndFieldName.getTableName(tableFieldName),
                                                               TableAndFieldName.getFieldName(tableFieldName), false, false);
            EtkMultiSprache text = new EtkMultiSprache(displayFieldText.get(cnt), project.getConfig().getDatabaseLanguages());
            displayField.setText(text);
            displayField.setDefaultText(false);
            displayFields.addFeld(displayField);
            cnt++;
        }
        displayFields.loadStandards(project.getConfig());


        iPartsShowDataObjectsDialog dlg = new iPartsShowDataObjectsDialog(dataConnector, parentForm, displayFields, showList,
                                                                          synchronEvent);
        dlg.setTitle("!!TUs im Retail");
        dlg.addContextMenu("gotoRetailMenu", "!!Gehe zum Retail-TU", null);
        dlg.changeButtonPanelDialogStyle(GuiButtonPanel.DialogStyle.DIALOG);
        ModalResult modalResult = dlg.showModal();
        if (modalResult == ModalResult.OK) {
            EtkDataObject dataObject = dlg.getSelectedObject();
            if ((dataObject != null) && (dataObject instanceof EtkDataPartListEntry)) {
                return ((EtkDataPartListEntry)dataObject).getAsId();
            }
        }
        return null;
    }

    public static iPartsSaaId showGotoSaaElements(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                  List<String> saaStrList, boolean synchronEvent) {
        EtkProject project = dataConnector.getProject();
        List<EtkDataObject> showList = new DwList<>();
        saaStrList.forEach((saaNumber) -> {
            iPartsDataSaa saa = new iPartsDataSaa(project, new iPartsSaaId(saaNumber));
            saa.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            showList.add(saa);
        });

        EtkDisplayFields displayFields = new EtkDisplayFields();
        EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_SAA, FIELD_DS_SAA, false, false);
        EtkMultiSprache text = new EtkMultiSprache("!!SAA/BK Nummern", project.getConfig().getDatabaseLanguages());
        displayField.setText(text);
        displayField.setDefaultText(false);
        displayFields.addFeld(displayField);
        displayFields.loadStandards(project.getConfig());

        iPartsShowDataObjectsDialog dlg = new iPartsShowDataObjectsDialog(dataConnector, parentForm, displayFields, showList,
                                                                          synchronEvent);
        dlg.setTitle("!!SAA/BK Auswahl");
        dlg.addContextMenu("gotoSaaMenu", "!!Gehe zur Konstuktionsstückliste", null);
        dlg.changeButtonPanelDialogStyle(GuiButtonPanel.DialogStyle.DIALOG);
        ModalResult modalResult = dlg.showModal();
        if (modalResult == ModalResult.OK) {
            EtkDataObject dataObject = dlg.getSelectedObject();
            if ((dataObject != null) && (dataObject instanceof iPartsDataSaa)) {
                return ((iPartsDataSaa)dataObject).getAsId();
            }
        }
        return null;
    }

    public static void showDIALOGTransferToAS(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                              List<EtkDataObject> showList, AssemblyId assemblyId, List<EtkDisplayField> displayFieldList, boolean synchronEvent) {
        EtkDisplayFields displayFields = new EtkDisplayFields();
        for (EtkDisplayField displayField : displayFieldList) {
            displayFields.addFeld(displayField);
        }

        iPartsShowDataObjectsDialog dlg = new iPartsShowDataObjectsDialog(dataConnector, parentForm, displayFields, showList,
                                                                          synchronEvent);
        String msg = "!!Übernahme DIALOG-Positionen";
        if (assemblyId != null) {
            msg = TranslationHandler.translate("!!Übernahme DIALOG-Positionen in den TU %1", assemblyId.getKVari());
        }
        dlg.setTitle(msg);
        dlg.showModal();
    }

    public static void showModelList(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                     List<iPartsDataModel> showList, EtkDisplayFields displayFields, String title, boolean synchronEvent) {
        iPartsShowDataObjectsDialog dlg = new iPartsShowDataObjectsDialog(dataConnector, parentForm, displayFields, showList,
                                                                          synchronEvent);
        dlg.setTitle(title);
        dlg.showModal();
    }

    public static EtkDisplayFields getDisplayFieldsFromTableDef(EtkProject project, String tableName) {
        EtkDisplayFields displayFields = project.getAllDisplayFieldsForTable(tableName);
        int index = displayFields.getIndexOfFeld(tableName, DBConst.FIELD_STAMP, false);
        if (index != -1) {
            displayFields.removeField(index);
        }
        displayFields.loadStandards(project.getConfig());

        return displayFields;
    }

    private ShowDataObjectFilterGrid grid;
    private boolean isKeyValueShowing;
    private EtkDisplayFields keyValueDisplayFields;

    public iPartsShowDataObjectsDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                       EtkDisplayFields displayFields, List<? extends EtkDataObject> dataObjects,
                                       boolean synchronEvent) {
        this(dataConnector, parentForm, displayFields, dataObjects, synchronEvent, false, false, -1);
    }

    /**
     * Erzeugt eine Instanz von iPartsShowDataObjectsDialog.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public iPartsShowDataObjectsDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                       EtkDisplayFields displayFields, List<? extends EtkDataObject> dataObjects,
                                       boolean synchronEvent, boolean isKeyValueShowing, boolean useDisplayFieldsForKeyValue,
                                       int maxKeyColumnWidth) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        postCreateGui(synchronEvent);
        if (!isKeyValueShowing) {
            if (displayFields != null) {
                setDisplayFields(displayFields);
            }
        } else {
            if (useDisplayFieldsForKeyValue) {
                this.keyValueDisplayFields = displayFields;
            }

            if (useDisplayFieldsForKeyValue || (displayFields == null)) {
                // Zwei künstliche Spalten für Name und Wert erzeugen
                String tableName;
                String fieldName;
                if ((displayFields != null) && (displayFields.size() > 0)) {
                    EtkDisplayFieldKeyNormal key = displayFields.getFeld(0).getKey();
                    tableName = key.getTableName();
                    fieldName = key.getFieldName();
                } else if (!dataObjects.isEmpty()) {
                    EtkDataObject firstDataObject = dataObjects.get(0);
                    tableName = firstDataObject.getTableName();
                    fieldName = firstDataObject.getPKFields()[0];
                } else {
                    tableName = "";
                    fieldName = "";
                }
                EtkDisplayFields keyValueHeaderDisplayFields = new EtkDisplayFields();
                EtkDisplayField keyField = keyValueHeaderDisplayFields.addFeld(tableName, fieldName, false, false, "!!Name", getProject());
                keyValueHeaderDisplayFields.addFeld(tableName, fieldName, true, false, "!!Wert", getProject());
                keyValueHeaderDisplayFields.loadStandards(getConfig());
                if (maxKeyColumnWidth > 0) {
                    keyField.setWidth(maxKeyColumnWidth);
                    keyField.setDefaultWidth(false);
                }
                setDisplayFields(keyValueHeaderDisplayFields);
            }
        }
        this.isKeyValueShowing = isKeyValueShowing;
        init(dataObjects);

        if (synchronEvent) {
            // OK-Button muss synchron abgearbeitet werden, damit nicht-modale Fenster ohne Popup-Blocker geöffnet werden können
            GuiButtonOnPanel okButton = mainWindow.buttonpanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK);
            okButton.removeEventListeners(Event.ACTION_PERFORMED_EVENT);
            okButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT, EventListenerOptions.SYNCHRON_EVENT) {
                @Override
                public void fire(Event event) {
                    mainWindow.setModalResult(ModalResult.OK);
                    mainWindow.setVisible(false);
                }
            });
        }
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui(boolean synchronEvent) {
        grid = new ShowDataObjectFilterGrid(getConnector(), this, synchronEvent);

        grid.getGui().setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        mainWindow.panelMain.addChild(grid.getGui());
        getTable().setSelectionMode(TableSelectionMode.SELECTION_MODE_SINGLE_SELECTION);
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

    public void setDisplayFields(EtkDisplayFields displayFields) {
        grid.setDisplayFields(displayFields);
    }

    public void changeButtonPanelDialogStyle(GuiButtonPanel.DialogStyle style) {
        mainWindow.buttonpanel.setDialogStyle(style);
        enableOKButton();
    }

    public GuiButtonPanel.DialogStyle getButtonPanelDialogStyle() {
        return mainWindow.buttonpanel.getDialogStyle();
    }

    public void addControlToPanelMain(AbstractGuiControl control, String position) {
        if (position.equals(ConstraintsBorder.POSITION_CENTER)) {
            position = ConstraintsBorder.POSITION_SOUTH;
        }
        mainWindow.panelMain.addChildBorder(control, position);
    }

    public void setAutoSize() {
        setSize(calcWidth(), calcHeight());
    }

    public void setSize(int width, int height) {
        mainWindow.setSize(width, height);
    }

    private int calcHeight() {
        int rowCount = getTable().getRowCount();
        if (rowCount == 0) {
            rowCount = 3;
        } else {
            rowCount++;
        }
        int calculatedHeight = (getTable().getHeader().getHeight() + 10) * rowCount;
        calculatedHeight += mainWindow.title.getPreferredHeight() + mainWindow.buttonpanel.getPreferredHeight() + 40;
        calculatedHeight = Math.max(calculatedHeight, mainWindow.getMinimumHeight());

        return Math.min((int)FrameworkUtils.getScreenSize().getHeight() - 20, calculatedHeight);
    }

    private int calcWidth() {
        int calculatedWidth = Math.max(getTable().getTableWidth() + 100, mainWindow.getMinimumWidth());
        return Math.min((int)FrameworkUtils.getScreenSize().getWidth() - 20, calculatedWidth);
    }

    public void addContextMenu(String menuItemName, String menuItemText, FrameworkImage menuItemIcon) {
        GuiMenuItem contextMenuItem = createMenuItem(menuItemName, menuItemText, menuItemIcon);
        addContextMenu(contextMenuItem);
    }

    public void addContextMenu(String menuItemName, String menuItemText, FrameworkImage menuItemIcon, EventListener eventListener) {
        GuiMenuItem contextMenuItem = createMenuItem(menuItemName, menuItemText, menuItemIcon);
        contextMenuItem.addEventListener(eventListener);
        grid.addContextMenu(contextMenuItem);
    }

    private GuiMenuItem createMenuItem(String menuItemName, String menuItemText, FrameworkImage menuItemIcon) {
        GuiMenuItem contextMenuItem = new GuiMenuItem();
        contextMenuItem.setUserObject(menuItemName);
        contextMenuItem.setName(menuItemName);
        contextMenuItem.setText(menuItemText);
        contextMenuItem.setIcon(menuItemIcon);
        return contextMenuItem;
    }

    public void addContextMenu(GuiMenuItem menuItem) {
        menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {

            @Override
            public void fire(Event event) {
                onOKButtonClicked(event);
            }
        });

        grid.addContextMenu(menuItem);
    }

    public EtkDataObject getSelectedObject() {
        if (mainWindow.getModalResult() == ModalResult.OK) {
            List<EtkDataObject> selectedList = grid.getSelection();
            if ((selectedList != null) && !selectedList.isEmpty()) {
                return selectedList.get(0);
            }
        }
        return null;
    }

    public List<List<EtkDataObject>> getGridList() {
        return grid.getDataObjectList();
    }

    /**
     * Initialisierung mit {@link iPartsDataAuthorOrder}
     *
     * @param dataObjects
     */
    public void init(List<? extends EtkDataObject> dataObjects) {
        fillGrid(dataObjects);
        setAutoSize();
    }


    private GuiTable getTable() {
        return grid.getTable();
    }

    protected void enableOKButton() {
        if (getButtonPanelDialogStyle() == GuiButtonPanel.DialogStyle.DIALOG) {
            mainWindow.buttonpanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, isEnabled());
        }
    }

    protected boolean isEnabled() {
        return grid.isSomethingSelected();
    }

    private void onOKButtonClicked(Event event) {
        GuiButtonOnPanel okButton = mainWindow.buttonpanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK);
        if (okButton.isVisible()) {
            okButton.doClick();
        }
    }

    /**
     * Grid füllen
     *
     * @param dataObjects
     */
    private <E extends EtkDataObject> void fillGrid(List<E> dataObjects) {
        grid.clearGrid();
        if ((dataObjects == null) || dataObjects.isEmpty()) {
            grid.showNoResultsLabel(true);
            return;
        }
        if (isKeyValueShowing) {
            E dataObject = dataObjects.get(0);
            dataObjects.clear();
            grid.initForKeyValue(getProject(), dataObject.getTableName(), keyValueDisplayFields);
            grid.getTable().setSortEnabled(false);
            int cnt = grid.keyValueDisplayFields.size();
            for (int index = 0; index < cnt; index++) {
                dataObjects.add(dataObject);
            }
        }
        for (EtkDataObject dataObject : dataObjects) {
            grid.addObjectToGrid(dataObject);
        }
        grid.showNoResultsLabel(dataObjects.isEmpty());
    }

    protected class ShowDataObjectFilterGrid extends DataObjectFilterGrid {

        private int keyValueCounter;
        protected EtkDisplayFields keyValueDisplayFields;

        public ShowDataObjectFilterGrid(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                        boolean synchronEvent) {
            super(dataConnector, parentForm);

            if (synchronEvent) {
                // Doppelklick muss synchron abgearbeitet werden, damit nicht-modale Fenster ohne Popup-Blocker geöffnet werden können
                getTable().removeEventListeners(Event.MOUSE_DOUBLECLICKED_EVENT);
                getTable().addEventListener(new EventListener(Event.MOUSE_DOUBLECLICKED_EVENT, EventListenerOptions.SYNCHRON_EVENT) {
                    @Override
                    public void fire(Event event) {
                        onTableDblClicked(event);
                    }
                });
            }
        }

        @Override
        protected void onTableSelectionChanged(Event event) {
            enableOKButton();
        }

        @Override
        protected void onCellDblClicked(int row, int col, Event event) {
            onOKButtonClicked(event);
        }

        public void addContextMenu(GuiMenuItem menuItem) {
            getToolbarHelper().insertMenuBeforeTableCopyMenu(getContextMenu(), menuItem, true);
        }

        public void initForKeyValue(EtkProject project, String tableName, EtkDisplayFields keyValueDisplayFields) {
            keyValueCounter = 0;
            if (keyValueDisplayFields != null) {
                this.keyValueDisplayFields = keyValueDisplayFields;
            } else {
                this.keyValueDisplayFields = getDisplayFieldsFromTableDef(project, tableName);
            }
        }

        @Override
        protected GuiTableRowWithObjects createRow(List<EtkDataObject> dataObjects) {
            if (!isKeyValueShowing) {
                return super.createRow(dataObjects);
            }
            GuiTableRowWithObjects row = new GuiTableRowWithObjects(dataObjects);

            EtkDisplayField field = keyValueDisplayFields.getFeld(keyValueCounter);
            if (field != null) {
                String fieldName = field.getKey().getFieldName();
                String tableName = field.getKey().getTableName();

                // Welches Object ist für diese Tabelle zuständig?
                EtkDataObject objectForTable = row.getObjectForTable(tableName);

                String key = field.getText().getTextByNearestLanguage(getProject().getViewerLanguage(), getProject().getDataBaseFallbackLanguages());
                GuiLabel label = new GuiLabel(key);
                row.addChild(label);

                String value;
                value = getVisualValueOfField(tableName, fieldName, objectForTable);
                label = new GuiLabel(value);
                row.addChild(label);
                keyValueCounter++;
            }
            return row;
        }
    }


    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler
                                                 translationHandler) {
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
            this.setMinimumWidth(250);
            this.setMinimumHeight(200);
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