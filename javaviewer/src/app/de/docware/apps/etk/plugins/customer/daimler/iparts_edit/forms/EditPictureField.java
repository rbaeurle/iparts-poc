/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.db.datatypes.picture.PictureDataType;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.SimpleSelectSearchResultGrid;
import de.docware.apps.etk.base.forms.common.components.MenuRunnable;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.forms.events.OnChangeEvent;
import de.docware.apps.etk.base.forms.events.OnDblClickEvent;
import de.docware.apps.etk.base.forms.events.OnSearchFinishedEvent;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataIcon;
import de.docware.apps.etk.base.project.mechanic.ids.IconId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.config.db.datatypes.DataTypesUsageType;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiSeparator;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.misc.DWOrientation;
import de.docware.framework.modules.gui.controls.table.GuiTable;
import de.docware.framework.modules.gui.controls.table.GuiTableHeader;
import de.docware.framework.modules.gui.controls.table.HtmlTablePageSplitMode;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.responsive.components.list.gui.RList;
import de.docware.util.ArrayUtil;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dialog zum Editieren von Zusatzgrafiken
 */
public class EditPictureField extends AbstractJavaViewerForm {

    /**
     * Editieren und veränderen der PictureIds von Zusatzgrafiken
     *
     * @param dataConnector
     * @param parentForm
     * @param pictureIds
     * @return
     */
    public static String editPictureIds(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String pictureIds) {
        EditPictureField dlg = new EditPictureField(dataConnector, parentForm, pictureIds);
        dlg.setTitle("!!Bearbeiten eines Feldes mit Zusatzgrafiken");

        if (dlg.showModal() == ModalResult.OK) {
            pictureIds = dlg.getPictureIds();
        }
        return pictureIds;
    }

    public static String showPictureIds(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String pictureIds) {
        EditPictureField dlg = new EditPictureField(dataConnector, parentForm, pictureIds);
        dlg.setTitle("!!Auswahl von Zusatzgrafiken");

        if (dlg.showModal() == ModalResult.OK) {
            pictureIds = dlg.getPictureIds();
        }
        return pictureIds;
    }

    private class SelectSearchGridIcons extends SimpleSelectSearchResultGrid {

        private PictureDataType pDataType;
        private String iconName;
        private IdWithType idToSelect;

        /**
         * Erzeugt eine Instanz von SimpleSelectSearchResultGrid.
         * Den $$internalCreateGui$$() Aufruf nicht ändern!
         */
        public SelectSearchGridIcons(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
            super(dataConnector, parentForm, EtkDbConst.TABLE_ICONS, EtkDbConst.FIELD_I_ICON);

            EtkDisplayFields displayResultFields = calculateDisplayResultFields();
            setDisplayResultFields(displayResultFields);

            showSearchField(false);
            idToSelect = null;
            setOnSearchFinishedEvent(new OnSearchFinishedEvent() {
                @Override
                public void OnSearchFinishedEvent() {
                    setSelection(true, idToSelect);
                }
            });
            setSearchValue("*");
        }

        public GuiTable getTable() {
            return super.getTable();
        }


        private EtkDisplayFields calculateDisplayResultFields() {
            EtkDisplayFields displayResultFields = new EtkDisplayFields();
            EtkDatabaseTable tableDef = getConfig().getDBDescription().findTable(searchTable);

            displayResultFields.addFeld(createDisplayField(tableDef, EtkDbConst.FIELD_I_DATA));
            displayResultFields.addFeld(createDisplayField(tableDef, EtkDbConst.FIELD_I_ICON));
            displayResultFields.addFeld(createDisplayField(tableDef, EtkDbConst.FIELD_I_HINT));

            displayResultFields.loadStandards(getConfig());
            return displayResultFields;
        }

        private EtkDisplayField createDisplayField(EtkDatabaseTable tableDef, String fieldName) {
            EtkDatabaseField dbField = tableDef.getField(fieldName);
            return new EtkDisplayField(TableAndFieldName.make(searchTable, dbField.getName()), dbField.isMultiLanguage(), dbField.isArray());
        }

        protected void setSelection(boolean scrollToFirstSelection, IdWithType idToSelect) {
            getTable().clearSelection();
            if ((idToSelect != null) && idToSelect.isValidId()) {
                // IDs für die auszuwählenden Attributes erzeugen
                Set<IdWithType> idsToBeSelected = new HashSet<IdWithType>(1);
                idsToBeSelected.add(new IdWithType("xx", idToSelect.toStringArrayWithoutType()));

                for (int rowIndex = 0; rowIndex < getTable().getRowCount(); rowIndex++) {
                    SimpleSelectSearchResultGrid.GuiTableRowWithAttributes row = (SimpleSelectSearchResultGrid.GuiTableRowWithAttributes)getTable().getRow(rowIndex);
                    IdWithType rowId = buildIdFromAttributes(row.attributes);
                    if (idsToBeSelected.contains(rowId)) {
                        getTable().addRowToSelection(rowIndex);
                        if (scrollToFirstSelection) { // zum ersten gefundenen Eintrag scrollen
                            getTable().scrollToCell(rowIndex, 0);
                            break;
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

        private IdWithType buildIdFromAttributes(DBDataObjectAttributes attributes) {
            EtkDatabaseTable tableDef = getProject().getConfig().getDBDescription().findTable(searchTable);
            List<String> pkFields = tableDef.getPrimaryKeyFields();
            List<String> pkValues = new DwList<String>();
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

        @Override
        protected void setGridHeader(EtkDisplayFields resultFields) {
            GuiTableHeader tableHeader = new GuiTableHeader();
            for (EtkDisplayField field : resultFields.getFields()) {
                if (field.isVisible()) {
                    String txt = field.getText().getText(TranslationHandler.getUiLanguage());
                    GuiLabel label = new GuiLabel(txt);
                    tableHeader.addChild(label, 150);
                }
            }
            getTable().setHeader(tableHeader);
        }

        @Override
        protected GuiTableRowWithAttributes setRow(DBDataObjectAttributes attributes) {
            GuiTableRowWithAttributes row = new GuiTableRowWithAttributes();
            pDataType = new PictureDataType(searchTable, EtkDbConst.FIELD_I_ICON);
            pDataType.loadConfig(getConfig(), "");
            iconName = attributes.getField(EtkDbConst.FIELD_I_ICON).getAsString();

            for (EtkDisplayField field : displayResultFields.getFields()) {
                if (field.isVisible()) {
                    String fieldName = field.getKey().getFieldName();
                    String value = getVisualValueOfFieldValue(fieldName, attributes.getField(fieldName), field.isMultiLanguage());
                    GuiLabel label = new GuiLabel(value);
                    row.addChild(label);
                }
            }
            return row;
        }

        @Override
        protected String getVisualValueOfFieldValue(String fieldName, DBDataObjectAttribute fieldValue, boolean isMultiLanguage) {
            if (fieldName.equals(EtkDbConst.FIELD_I_DATA)) {
                return getHTMLPictureValue(iconName);
            } else {
                return super.getVisualValueOfFieldValue(fieldName, fieldValue, isMultiLanguage);
            }
        }

        public String getHTMLPictureValue(String pictures) {
            return pDataType.formatPictureOutputForHtml(getProject(), pictures, DataTypesUsageType.ORIGINAL).getStringResult();
        }

        public void reload(IdWithType idToSelect) {
            this.idToSelect = null;
            setSearchValue("");
            this.idToSelect = idToSelect;
            setSearchValue("*");
        }

        public void removePictureElementFromCache(String picId) {
            pDataType = new PictureDataType(searchTable, EtkDbConst.FIELD_I_ICON);
            pDataType.removePictureElementFromCache(getProject(), picId);
        }
    }

    private RList listSelectedEntries;
    private EditToolbarButtonMenuHelper toolbarHelper;
    private EditToolbarButtonMenuHelper toolbarHelperSelected;
    private SelectSearchGridIcons searchGrid;
    private EditPicturePreviewForm previewGrid;
    private String pictureIds;
    private boolean isEditAllowed;
    private boolean onlySinglePicturesAllowed;

    /**
     * Erzeugt eine Instanz von EditPictureField.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditPictureField(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String pictureIds) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.pictureIds = pictureIds;
        this.isEditAllowed = true;
        this.onlySinglePicturesAllowed = false;
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        listSelectedEntries = RList.replaceGuiList(mainWindow.listSelectedEntries);
        toolbarHelper = new EditToolbarButtonMenuHelper(getConnector(), mainWindow.toolbarExistingEntries);
        toolbarManager = toolbarHelper.getToolbarManager();
        mainWindow.toolbarSelectedEntries.setButtonOrientation(DWOrientation.VERTICAL);
        toolbarHelperSelected = new EditToolbarButtonMenuHelper(getConnector(), mainWindow.toolbarSelectedEntries);

        searchGrid = new SelectSearchGridIcons(getConnector(), this);
        searchGrid.setOnChangeEvent(new OnChangeEvent() {
            @Override
            public void onChange() {
                enableButtons();
            }
        });
        searchGrid.setOnDblClickEvent(new OnDblClickEvent() {
            @Override
            public void onDblClick() {
                doAdd(null);
            }
        });

        createToolbarButtons(searchGrid.getTable());

        AbstractGuiControl gui = searchGrid.getGui();
        mainWindow.panelForGridEntries.addChildBorderCenter(gui);
        searchGrid.getTable().setContextMenu(contextMenuExistingEntries);

        listSelectedEntries.setContextMenu(contextMenuSelectedEntries);

        mainWindow.panelPreview.removeAllChildren();
        previewGrid = new EditPicturePreviewForm(getConnector(), this, /*pictureIds, */true);
        gui = previewGrid.getGui();
        mainWindow.panelPreview.addChildBorderCenter(gui);
        previewGrid.showPreview();

        //normiere pictureIds
        pictureIds = previewGrid.makePicIds(previewGrid.makePicIdList(pictureIds));
        previewGrid.setPictureIds(pictureIds);
        //pictureIds = searchGrid.getPictureDataType().makePidIds(searchGrid.getPictureDataType().makePicIdList(pictureIds));

        fillChoosenEntries(pictureIds);
        enableButtons();
    }

    @Override
    public AbstractGuiControl getGui() {
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

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public boolean isEditAllowed() {
        return isEditAllowed;
    }

    public void setEditAllowed(boolean editAllowed) {
        isEditAllowed = editAllowed;
        enableButtons();
    }

    public boolean isOnlySinglePicturesAllowed() {
        return onlySinglePicturesAllowed;
    }

    public void setOnlySinglePicturesAllowed(boolean onlySinglePicturesAllowed) {
        this.onlySinglePicturesAllowed = onlySinglePicturesAllowed;
    }

    public String getPictureIds() {
        return pictureIds;
    }

    /**
     * @return
     */
    protected GuiTable getTablePreview() {
        return mainWindow.tablePreview;
    }

    private String getIconName(DBDataObjectAttributes attributes) {
        return attributes.getField(EtkDbConst.FIELD_I_ICON).getAsString();
    }

    private void createToolbarButtons(GuiTable table) {
        ToolbarButtonMenuHelper.ToolbarMenuHolder holder;
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_NEW, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doCreate(event);
            }
        });
        contextMenuExistingEntries.addChild(holder.menuItem);
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_WORK, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doEdit(event);
            }
        });
        contextMenuExistingEntries.addChild(holder.menuItem);
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_DELETE, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doDelete(event);
            }
        });
        contextMenuExistingEntries.addChild(holder.menuItem);

        GuiSeparator separator = toolbarHelper.createMenuSeparator("menuSeparator", getUITranslationHandler());
        contextMenuExistingEntries.addChild(separator);

        GuiMenuItem menuItemCopy = toolbarHelper.createCopyMenuForTable(table, getUITranslationHandler());
        contextMenuExistingEntries.addChild(menuItemCopy);

        holder = toolbarHelperSelected.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.IMG_RIGHT, "!!Übernehmen", getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doAdd(event);
            }
        });
        contextMenuSelectedEntries.addChild(holder.menuItem);
        holder = toolbarHelperSelected.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.IMG_LEFT, "!!Entfernen", getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doRemove(event);
            }
        });
        contextMenuSelectedEntries.addChild(holder.menuItem);
        holder = toolbarHelperSelected.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.IMG_UP, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doMoveUp(event);
            }
        });
        contextMenuSelectedEntries.addChild(holder.menuItem);
        holder = toolbarHelperSelected.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.IMG_DOWN, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doMoveDown(event);
            }
        });
        contextMenuSelectedEntries.addChild(holder.menuItem);
    }

    private int getSelectedIndex() {
        return listSelectedEntries.getSelectedIndex();
    }

    private void setSelectedIndex(int index) {
        listSelectedEntries.setSelectedIndex(index);
    }

    private List<String> getPicIdList() {
        return listSelectedEntries.getTexts();
    }

    private void fillChoosenEntries(String pictures) {
        listSelectedEntries.switchOffEventListeners();
        int selectedIndex = getSelectedIndex();
        listSelectedEntries.removeAllItems();
        //List<String> picIdList = searchGrid.getPictureDataType().makePicIdList(pictures);
        List<String> picIdList = previewGrid.makePicIdList(pictures);
        for (String picId : picIdList) {
            listSelectedEntries.addItem(picId);
        }
        setSelectedIndex(selectedIndex);
        listSelectedEntries.switchOnEventListeners();
    }

    private String getChoosenEntries() {
        List<String> picIdList = getPicIdList();
        return previewGrid.makePicIds(picIdList);
    }

    private void refresh(List<String> picIdList) {
        String pictures = previewGrid.makePicIds(picIdList);
        fillChoosenEntries(pictures);
        previewGrid.setPictureIds(pictures);
        enableButtons();
    }

    private boolean isIconIdInSelected(String iconName) {
        List<String> picIdList = getPicIdList();
        return picIdList.contains(iconName);
    }

    private void replaceIconIdInSelected(String oldIconName, String newIconName) {
        List<String> picIdList = new DwList<String>(getPicIdList());
        int index = picIdList.indexOf(oldIconName);
        if (index != -1) {
            picIdList.remove(index);
            picIdList.add(index, newIconName);
            refresh(picIdList);
            setSelectedIndex(index);
        }
    }

    private void addNewIconIdToSelected(String iconName) {
        List<String> picIdList = new DwList<String>(getPicIdList());
        if (onlySinglePicturesAllowed) {
            if (picIdList.contains(iconName)) {
                MessageDialog.showWarning(TranslationHandler.translate("!!Die Zusatzgrafik '%1' ist bereits ausgewählt", iconName));
                return;
            }
        }
        picIdList.add(iconName);
        refresh(picIdList);
        setSelectedIndex(picIdList.size() - 1);
    }

    private void removeIconIdFromSelected(String iconName) {
        List<String> picIdList = new DwList<String>(getPicIdList());
        picIdList.remove(iconName);
        refresh(picIdList);
    }

    private void moveIconIdUp(String iconName) {
        List<String> picIdList = new DwList<String>(getPicIdList());
        int index = picIdList.indexOf(iconName);
        if (index > 0) {
            String scndIconName = picIdList.get(index - 1);
            picIdList.set(index - 1, iconName);
            picIdList.set(index, scndIconName);
            setSelectedIndex(index - 1);
            refresh(picIdList);
        }
    }

    private void moveIconIdDown(String iconName) {
        List<String> picIdList = new DwList<String>(getPicIdList());
        int index = picIdList.indexOf(iconName);
        if ((index != -1) && ((index + 1) < picIdList.size())) {
            String scndIconName = picIdList.get(index + 1);
            picIdList.set(index, scndIconName);
            picIdList.set(index + 1, iconName);
            setSelectedIndex(index + 1);
            refresh(picIdList);
        }
    }

    private void enableButtons() {
        boolean isGridSelected = searchGrid.getSelectedAttributes() != null;
        boolean isChoosenSelected = getSelectedIndex() != -1;
        boolean isNewAllowed = true;
        boolean pictureIdsChanged = !pictureIds.equals(getChoosenEntries());

        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW, contextMenuExistingEntries, isEditAllowed && isNewAllowed);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, contextMenuExistingEntries, isEditAllowed && isGridSelected);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, contextMenuExistingEntries, isEditAllowed && isGridSelected);

        toolbarHelperSelected.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_RIGHT, contextMenuSelectedEntries, isGridSelected);
        toolbarHelperSelected.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_LEFT, contextMenuSelectedEntries, isChoosenSelected);
        if (isChoosenSelected) {
            int itemCount = listSelectedEntries.getItemCount();
            if (itemCount > 1) {
                int selectedIndex = getSelectedIndex();
                toolbarHelperSelected.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_UP, contextMenuSelectedEntries, selectedIndex > 0);
                toolbarHelperSelected.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_DOWN, contextMenuSelectedEntries, (selectedIndex + 1) < itemCount);
            } else {
                toolbarHelperSelected.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_UP, contextMenuSelectedEntries, false);
                toolbarHelperSelected.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_DOWN, contextMenuSelectedEntries, false);
            }
        } else {
            toolbarHelperSelected.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_UP, contextMenuSelectedEntries, isChoosenSelected);
            toolbarHelperSelected.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_DOWN, contextMenuSelectedEntries, isChoosenSelected);
        }

        mainWindow.buttonPanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, pictureIdsChanged);
    }

    private void doCreate(Event event) {
        IconId iconId = EditPictureLoadEditForm.showCreatePictureLoadForm(getConnector(), this);
        if (iconId != null) {
            searchGrid.reload(iconId);
        }
    }

    private void doEdit(Event event) {
        DBDataObjectAttributes attributes = searchGrid.getSelectedAttributes();
        if (attributes != null) {
            IconId iconId = new IconId(getIconName(attributes));
            boolean isInSelectedList = isIconIdInSelected(iconId.getIconNo());
            IconId newIconId = EditPictureLoadEditForm.showEditPictureLoadForm(getConnector(), this, iconId);
            if (newIconId != null) {
                searchGrid.removePictureElementFromCache(iconId.getIconNo());
                searchGrid.reload(newIconId);
                if (isInSelectedList && !newIconId.getIconNo().equals(iconId.getIconNo())) {
                    replaceIconIdInSelected(iconId.getIconNo(), newIconId.getIconNo());
                }
            }
        }
    }

    private void doDelete(Event event) {
        DBDataObjectAttributes attributes = searchGrid.getSelectedAttributes();
        if (attributes != null) {
            if (MessageDialog.showYesNo("!!Zusatzgrafik wirklich löschen?") == ModalResult.YES) {
                String iconName = getIconName(attributes);
                if (deleteIcon(iconName)) {
                    //aus Cache löschen
                    searchGrid.removePictureElementFromCache(iconName);
                    removeIconIdFromSelected(iconName);
                    searchGrid.reload(null);
                }
            }
        }
    }

    private boolean deleteIcon(String iconName) {
        getDbLayer().startTransaction();
        try {
            IconId iconId = new IconId(iconName);
            EtkDataIcon dataIcon = EtkDataObjectFactory.createDataIcon();
            dataIcon.init(getProject());
            dataIcon.setId(iconId, DBActionOrigin.FROM_DB);
            dataIcon.deleteFromDB(true);
            getDbLayer().commit();
            return true;
        } catch (RuntimeException e) {
            getDbLayer().rollback();
            Logger.getLogger().handleRuntimeException(e);
        }
        return false;
    }

    private void doAdd(Event event) {
        DBDataObjectAttributes attributes = searchGrid.getSelectedAttributes();
        if (attributes != null) {
            String iconName = getIconName(attributes);
            if (!iconName.isEmpty()) {
                addNewIconIdToSelected(iconName);
            }
        }
    }

    private String getSelectedIconName() {
        int selectedIndex = getSelectedIndex();
        if (selectedIndex != -1) {
            return listSelectedEntries.getText(selectedIndex);
        }
        return null;
    }

    private void doRemove(Event event) {
        String iconName = getSelectedIconName();
        if (iconName != null) {
            removeIconIdFromSelected(iconName);
        }
    }

    private void doMoveUp(Event event) {
        String iconName = getSelectedIconName();
        if (iconName != null) {
            moveIconIdUp(iconName);
        }
    }

    private void doMoveDown(Event event) {
        String iconName = getSelectedIconName();
        if (iconName != null) {
            moveIconIdDown(iconName);
        }
    }

    private void onButtonOKClick(Event event) {
        pictureIds = getChoosenEntries();
        close();
    }

    private void onChangeSelectedEntries(Event event) {
        enableButtons();
    }

    private void listSelectedEntriesDoubleClickEvent(Event event) {
        doRemove(event);
    }

    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    private de.docware.framework.modules.gui.controls.menu.GuiContextMenu contextMenuExistingEntries;

    @SuppressWarnings({ "FieldCanBeLocal" })
    private de.docware.framework.modules.gui.controls.menu.GuiContextMenu contextMenuSelectedEntries;

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelEntries;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitpaneEntries;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpaneEntries_firstChild;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelExistingEntries;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.toolbar.GuiToolbar toolbarExistingEntries;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelForGridEntries;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpaneEntries_secondChild;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelSelectedEntries;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.toolbar.GuiToolbar toolbarSelectedEntries;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiList<Object> listSelectedEntries;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelPreview;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelPreview;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane scrollpanePreview;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.table.GuiTable tablePreview;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            contextMenuExistingEntries = new de.docware.framework.modules.gui.controls.menu.GuiContextMenu();
            contextMenuExistingEntries.setName("contextMenuExistingEntries");
            contextMenuExistingEntries.__internal_setGenerationDpi(96);
            contextMenuExistingEntries.registerTranslationHandler(translationHandler);
            contextMenuExistingEntries.setScaleForResolution(true);
            contextMenuExistingEntries.setMinimumWidth(10);
            contextMenuExistingEntries.setMinimumHeight(10);
            contextMenuExistingEntries.setMenuName("contextMenu");
            contextMenuExistingEntries.setParentControl(this);
            contextMenuSelectedEntries = new de.docware.framework.modules.gui.controls.menu.GuiContextMenu();
            contextMenuSelectedEntries.setName("contextMenuSelectedEntries");
            contextMenuSelectedEntries.__internal_setGenerationDpi(96);
            contextMenuSelectedEntries.registerTranslationHandler(translationHandler);
            contextMenuSelectedEntries.setScaleForResolution(true);
            contextMenuSelectedEntries.setMinimumWidth(10);
            contextMenuSelectedEntries.setMinimumHeight(10);
            contextMenuSelectedEntries.setMenuName("contextMenuSelectedEntries");
            contextMenuSelectedEntries.setParentControl(this);
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setHeight(700);
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
            title.setTitle(",,,");
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
            panelEntries = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelEntries.setName("panelEntries");
            panelEntries.__internal_setGenerationDpi(96);
            panelEntries.registerTranslationHandler(translationHandler);
            panelEntries.setScaleForResolution(true);
            panelEntries.setMinimumWidth(10);
            panelEntries.setMinimumHeight(10);
            panelEntries.setPaddingLeft(8);
            panelEntries.setPaddingRight(8);
            de.docware.framework.modules.gui.layout.LayoutBorder panelEntriesLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelEntries.setLayout(panelEntriesLayout);
            splitpaneEntries = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitpaneEntries.setName("splitpaneEntries");
            splitpaneEntries.__internal_setGenerationDpi(96);
            splitpaneEntries.registerTranslationHandler(translationHandler);
            splitpaneEntries.setScaleForResolution(true);
            splitpaneEntries.setMinimumWidth(10);
            splitpaneEntries.setMinimumHeight(10);
            splitpaneEntries.setDividerPosition(397);
            splitpaneEntries_firstChild = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpaneEntries_firstChild.setName("splitpaneEntries_firstChild");
            splitpaneEntries_firstChild.__internal_setGenerationDpi(96);
            splitpaneEntries_firstChild.registerTranslationHandler(translationHandler);
            splitpaneEntries_firstChild.setScaleForResolution(true);
            splitpaneEntries_firstChild.setMinimumWidth(0);
            de.docware.framework.modules.gui.layout.LayoutGridBag splitpaneEntries_firstChildLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            splitpaneEntries_firstChild.setLayout(splitpaneEntries_firstChildLayout);
            labelExistingEntries = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelExistingEntries.setName("labelExistingEntries");
            labelExistingEntries.__internal_setGenerationDpi(96);
            labelExistingEntries.registerTranslationHandler(translationHandler);
            labelExistingEntries.setScaleForResolution(true);
            labelExistingEntries.setMinimumWidth(10);
            labelExistingEntries.setMinimumHeight(10);
            labelExistingEntries.setPaddingTop(4);
            labelExistingEntries.setPaddingLeft(10);
            labelExistingEntries.setPaddingBottom(4);
            labelExistingEntries.setText("!!Vorhandene Einträge:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelExistingEntriesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "c", "h", 0, 0, 0, 0);
            labelExistingEntries.setConstraints(labelExistingEntriesConstraints);
            splitpaneEntries_firstChild.addChild(labelExistingEntries);
            toolbarExistingEntries = new de.docware.framework.modules.gui.controls.toolbar.GuiToolbar();
            toolbarExistingEntries.setName("toolbarExistingEntries");
            toolbarExistingEntries.__internal_setGenerationDpi(96);
            toolbarExistingEntries.registerTranslationHandler(translationHandler);
            toolbarExistingEntries.setScaleForResolution(true);
            toolbarExistingEntries.setMinimumWidth(10);
            toolbarExistingEntries.setMinimumHeight(20);
            toolbarExistingEntries.setBackgroundImage(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignToolbarBigBackground"));
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag toolbarExistingEntriesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 100.0, 0.0, "c", "h", 0, 0, 0, 0);
            toolbarExistingEntries.setConstraints(toolbarExistingEntriesConstraints);
            splitpaneEntries_firstChild.addChild(toolbarExistingEntries);
            panelForGridEntries = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelForGridEntries.setName("panelForGridEntries");
            panelForGridEntries.__internal_setGenerationDpi(96);
            panelForGridEntries.registerTranslationHandler(translationHandler);
            panelForGridEntries.setScaleForResolution(true);
            panelForGridEntries.setMinimumWidth(10);
            panelForGridEntries.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelForGridEntriesLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelForGridEntries.setLayout(panelForGridEntriesLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelForGridEntriesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 100.0, 100.0, "c", "b", 0, 0, 0, 0);
            panelForGridEntries.setConstraints(panelForGridEntriesConstraints);
            splitpaneEntries_firstChild.addChild(panelForGridEntries);
            splitpaneEntries.addChild(splitpaneEntries_firstChild);
            splitpaneEntries_secondChild = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpaneEntries_secondChild.setName("splitpaneEntries_secondChild");
            splitpaneEntries_secondChild.__internal_setGenerationDpi(96);
            splitpaneEntries_secondChild.registerTranslationHandler(translationHandler);
            splitpaneEntries_secondChild.setScaleForResolution(true);
            splitpaneEntries_secondChild.setMinimumWidth(0);
            de.docware.framework.modules.gui.layout.LayoutBorder splitpaneEntries_secondChildLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpaneEntries_secondChild.setLayout(splitpaneEntries_secondChildLayout);
            labelSelectedEntries = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelSelectedEntries.setName("labelSelectedEntries");
            labelSelectedEntries.__internal_setGenerationDpi(96);
            labelSelectedEntries.registerTranslationHandler(translationHandler);
            labelSelectedEntries.setScaleForResolution(true);
            labelSelectedEntries.setMinimumWidth(10);
            labelSelectedEntries.setMinimumHeight(10);
            labelSelectedEntries.setPaddingTop(4);
            labelSelectedEntries.setPaddingLeft(10);
            labelSelectedEntries.setPaddingBottom(4);
            labelSelectedEntries.setText("!!Ausgewählte Einträge:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelSelectedEntriesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelSelectedEntriesConstraints.setPosition("north");
            labelSelectedEntries.setConstraints(labelSelectedEntriesConstraints);
            splitpaneEntries_secondChild.addChild(labelSelectedEntries);
            toolbarSelectedEntries = new de.docware.framework.modules.gui.controls.toolbar.GuiToolbar();
            toolbarSelectedEntries.setName("toolbarSelectedEntries");
            toolbarSelectedEntries.__internal_setGenerationDpi(96);
            toolbarSelectedEntries.registerTranslationHandler(translationHandler);
            toolbarSelectedEntries.setScaleForResolution(true);
            toolbarSelectedEntries.setMinimumWidth(20);
            toolbarSelectedEntries.setMinimumHeight(10);
            toolbarSelectedEntries.setBackgroundImage(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignToolbarBigBackground"));
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder toolbarSelectedEntriesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            toolbarSelectedEntriesConstraints.setPosition("west");
            toolbarSelectedEntries.setConstraints(toolbarSelectedEntriesConstraints);
            splitpaneEntries_secondChild.addChild(toolbarSelectedEntries);
            listSelectedEntries = new de.docware.framework.modules.gui.controls.GuiList<Object>();
            listSelectedEntries.setName("listSelectedEntries");
            listSelectedEntries.__internal_setGenerationDpi(96);
            listSelectedEntries.registerTranslationHandler(translationHandler);
            listSelectedEntries.setScaleForResolution(true);
            listSelectedEntries.setMinimumWidth(200);
            listSelectedEntries.setMinimumHeight(100);
            listSelectedEntries.addEventListener(new de.docware.framework.modules.gui.event.EventListener("mouseDoubleClickedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    listSelectedEntriesDoubleClickEvent(event);
                }
            });
            listSelectedEntries.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onChangeSelectedEntries(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder listSelectedEntriesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            listSelectedEntries.setConstraints(listSelectedEntriesConstraints);
            splitpaneEntries_secondChild.addChild(listSelectedEntries);
            splitpaneEntries.addChild(splitpaneEntries_secondChild);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder splitpaneEntriesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            splitpaneEntries.setConstraints(splitpaneEntriesConstraints);
            panelEntries.addChild(splitpaneEntries);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelEntriesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelEntries.setConstraints(panelEntriesConstraints);
            panelMain.addChild(panelEntries);
            panelPreview = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelPreview.setName("panelPreview");
            panelPreview.__internal_setGenerationDpi(96);
            panelPreview.registerTranslationHandler(translationHandler);
            panelPreview.setScaleForResolution(true);
            panelPreview.setMinimumWidth(10);
            panelPreview.setMinimumHeight(150);
            panelPreview.setPaddingLeft(8);
            panelPreview.setPaddingRight(8);
            de.docware.framework.modules.gui.layout.LayoutBorder panelPreviewLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelPreview.setLayout(panelPreviewLayout);
            labelPreview = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelPreview.setName("labelPreview");
            labelPreview.__internal_setGenerationDpi(96);
            labelPreview.registerTranslationHandler(translationHandler);
            labelPreview.setScaleForResolution(true);
            labelPreview.setMinimumWidth(10);
            labelPreview.setMinimumHeight(10);
            labelPreview.setPaddingTop(4);
            labelPreview.setPaddingLeft(8);
            labelPreview.setPaddingBottom(4);
            labelPreview.setText("!!Vorschau");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelPreviewConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelPreviewConstraints.setPosition("north");
            labelPreview.setConstraints(labelPreviewConstraints);
            panelPreview.addChild(labelPreview);
            scrollpanePreview = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            scrollpanePreview.setName("scrollpanePreview");
            scrollpanePreview.__internal_setGenerationDpi(96);
            scrollpanePreview.registerTranslationHandler(translationHandler);
            scrollpanePreview.setScaleForResolution(true);
            scrollpanePreview.setMinimumWidth(10);
            scrollpanePreview.setMinimumHeight(10);
            tablePreview = new de.docware.framework.modules.gui.controls.table.GuiTable();
            tablePreview.setName("tablePreview");
            tablePreview.__internal_setGenerationDpi(96);
            tablePreview.registerTranslationHandler(translationHandler);
            tablePreview.setScaleForResolution(true);
            tablePreview.setMinimumWidth(10);
            tablePreview.setMinimumHeight(10);
            tablePreview.setHtmlTablePageSplitMode(HtmlTablePageSplitMode.NO_SPLIT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder tablePreviewConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            tablePreview.setConstraints(tablePreviewConstraints);
            scrollpanePreview.addChild(tablePreview);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder scrollpanePreviewConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            scrollpanePreview.setConstraints(scrollpanePreviewConstraints);
            panelPreview.addChild(scrollpanePreview);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelPreviewConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelPreviewConstraints.setPosition("south");
            panelPreview.setConstraints(panelPreviewConstraints);
            panelMain.addChild(panelPreview);
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
                    onButtonOKClick(event);
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