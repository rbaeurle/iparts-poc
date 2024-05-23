/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.picorder;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.MenuRunnable;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsAttachmentStatus;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferStates;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.attachments.iPartsDataPicOrderAttachment;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditDataObjectGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.filechooser.FileChooserPurpose;
import de.docware.framework.modules.gui.controls.filechooser.GuiFileChooserDialog;
import de.docware.framework.modules.gui.controls.filechooser.GuiFileOpenValidatorForExtensions;
import de.docware.framework.modules.gui.controls.filechooser.MultipleInputToOutputStream;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.table.GuiTable;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolbar;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import de.docware.util.misc.CompressionUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog zum Hinzufügen von Anhängen zu einen Bildauftrag
 */
public class PicOrderAttachmentsForm extends AbstractJavaViewerForm {

    private static final String VALUE_LARGER_THAN_MAX_SIZE = ">20 MB";

    private EditDataObjectGrid attachmentGrid;
    private GuiContextMenu contextmenuTableAttachments;
    private iPartsDataPicOrder dataPicOrder;
    private boolean isEditAllowed;

    /**
     * Erzeugt eine Instanz von EditAttachmentForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public PicOrderAttachmentsForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                   iPartsDataPicOrder dataPicOrder, boolean isEditAllowed) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.dataPicOrder = dataPicOrder;
        this.isEditAllowed = isEditAllowed;
        postCreateGui();
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        addAttachmentGrid();
    }

    /**
     * Fügt die Liste mit Anhängen hinzu
     */
    private void addAttachmentGrid() {
        contextmenuTableAttachments = new GuiContextMenu();
        attachmentGrid = new EditDataObjectGrid(getConnector(), this) {
            @Override
            protected String getVisualValueOfField(String tableName, String fieldName, EtkDataObject objectForTable) {
                if (tableName.equals(iPartsConst.TABLE_DA_PICORDER_ATTACHMENTS) && fieldName.equals(iPartsConst.FIELD_DPA_SIZE)) {
                    String value = XMLImportExportHelper.makeViewForSize(objectForTable.getFieldValue(fieldName, getProject().getDBLanguage(), false));
                    return getVisObject().asHtml(tableName, fieldName, value,
                                                 getProject().getDBLanguage()).getStringResult();
                } else {
                    return super.getVisualValueOfField(tableName, fieldName, objectForTable);
                }
            }

            @Override
            protected void onTableSelectionChanged(Event event) {
                doEnableButtons();
            }

            @Override
            protected void onCellDblClicked(int row, int col, Event event) {
                doEditAttachment(event);
            }

            @Override
            protected void createToolbarButtons(GuiToolbar toolbar) {
                if (isAttachmentEditable(true)) {
                    getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_NEW, getUITranslationHandler(), new MenuRunnable() {
                        public void run(Event event) {
                            doCreateNewAttachment(event);
                        }
                    });
                    ToolbarButtonMenuHelper.ToolbarMenuHolder holder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_WORK, getUITranslationHandler(), new MenuRunnable() {
                        public void run(Event event) {
                            doEditAttachment(event);
                        }
                    });
                    contextmenuTableAttachments.addChild(holder.menuItem);


                    holder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_DELETE, getUITranslationHandler(), new MenuRunnable() {
                        public void run(Event event) {
                            doDeleteAttachment(event);
                        }
                    });
                    contextmenuTableAttachments.addChild(holder.menuItem);
                    holder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_DOWNLOAD, getUITranslationHandler(), new MenuRunnable() {
                        public void run(Event event) {
                            try {
                                doDownloadAttachment(event);
                            } catch (IOException e) {
                                Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "Could not download MQ Attachments to local machine. Error:" + e.getMessage());
                            }
                        }
                    });
                    contextmenuTableAttachments.addChild(holder.menuItem);
                }
            }

            @Override
            protected void createContextMenuItems(GuiContextMenu contextMenu) {
                if (isAttachmentEditable(true)) {
                    List<AbstractGuiControl> menuList = new DwList<>(contextmenuTableAttachments.getChildren());
                    for (AbstractGuiControl menu : menuList) {
                        contextmenuTableAttachments.removeChild(menu);
                        contextMenu.addChild(menu);
                    }
                }
            }
        };
        attachmentGrid.setPageSplitNumberOfEntriesPerPage(iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE);
        attachmentGrid.setDisplayFields(buildAttachmentDisplayFields());

        ConstraintsBorder constraints = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
        attachmentGrid.getGui().setConstraints(constraints);
        mainWindow.panelGrid.addChild(attachmentGrid.getGui());
        attachmentGrid.showToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW);
    }

    private void doDeleteAttachment(Event event) {
        List<List<EtkDataObject>> selectedAttachments = attachmentGrid.getMultiSelection();
        if (!selectedAttachments.isEmpty()) {
            for (List<EtkDataObject> dataObjectList : selectedAttachments) {
                for (EtkDataObject attachment : dataObjectList) {
                    dataPicOrder.deleteAttachment((iPartsDataPicOrderAttachment)attachment, DBActionOrigin.FROM_EDIT);
                }
            }
            updateAttachmentGrid(attachmentGrid.getTable().getSelectedRowIndex());
        }
    }

    private void doEditAttachment(Event event) {
        if (attachmentGrid.getTable().getSelectedRows().size() == 1) {
            iPartsDataPicOrderAttachment attachment = (iPartsDataPicOrderAttachment)attachmentGrid.getSelection().get(0);
            if (attachment != null) {
                PicOrderAttachmentInfoForm.showEditAttachmentDialog(getConnector(), this, attachment, isAttachmentEditable(false));
            }
        }
        updateAttachmentGrid(attachmentGrid.getTable().getSelectedRowIndex());

    }

    /**
     * Aktualisieren der Liste mit Anhängen
     *
     * @param selectedImageRowIndex
     */
    public void updateAttachmentGrid(int selectedImageRowIndex) {
        GuiTable table = attachmentGrid.getTable();
        if (selectedImageRowIndex < 0) {
            selectedImageRowIndex = table.getSelectedRowIndex();
        }

        int oldSortColumn = table.getSortColumn(); // Sortierung merken
        attachmentGrid.clearGrid();
        fillAttachmentGridWithObjects();

        // Sortierung wiederherstellen
        if (oldSortColumn >= 0) {
            table.sortRowsAccordingToColumn(oldSortColumn, table.isSortAscending());
        }

        // Selektion wiederherstellen
        attachmentGrid.scrollToRowIfExists(selectedImageRowIndex);

        checkAttachmentSize(false);

        doEnableButtons();
    }

    /**
     * Neuen Anhang erstellen
     *
     * @param event
     */
    private void doCreateNewAttachment(Event event) {
        List<DWFile> selectedFiles = getFilesFromFileChooser();
        if ((selectedFiles != null) && !selectedFiles.isEmpty()) {
            if (selectedFiles.size() > 1) {
                PicOrderAttachmentInfoForm.handleFilesWithoutDialog(getConnector(), this, dataPicOrder, selectedFiles);
            } else {
                PicOrderAttachmentInfoForm.showCreateAttachmentDialog(getConnector(), this, dataPicOrder, selectedFiles.get(0));
            }
        }
        updateAttachmentGrid(attachmentGrid.getTable().getSelectedRowIndex());
        checkAttachmentSize(true);
    }

    private void doDownloadAttachment(Event event) throws IOException {
        List<List<EtkDataObject>> selectedAttachments = attachmentGrid.getMultiSelection();
        GuiFileChooserDialog fileChooserDialog = new GuiFileChooserDialog(FileChooserPurpose.SAVE, GuiFileChooserDialog.FILE_MODE_DIRECTORIES, null, false);
        fileChooserDialog.setServerMode(false);
        MultipleInputToOutputStream inputToOutputStream;
        if (!selectedAttachments.isEmpty()) {
            String fileName = dataPicOrder.getOrderIdExtern() + " Anhänge";
            iPartsDataPicOrderAttachment attachmentObject;
            DWFile directory = DWFile.createTempDirectory("daim");
            if (directory != null) {
                if (selectedAttachments.size() == 1) {
                    attachmentObject = (iPartsDataPicOrderAttachment)selectedAttachments.get(0).get(0);
                    DWFile singleFile = makeFileFromAttachment(directory, attachmentObject);
                    inputToOutputStream = new MultipleInputToOutputStream(singleFile, singleFile.getName());
                } else {
                    List<String> files = new ArrayList<String>();
                    for (List<EtkDataObject> dataObjectList : selectedAttachments) {
                        for (EtkDataObject attachment : dataObjectList) {
                            attachmentObject = (iPartsDataPicOrderAttachment)attachment;
                            files.add(makeFileFromAttachment(directory, attachmentObject).getAbsolutePath());
                        }
                    }
                    File zipFile = DWFile.createTempFile(fileName, ".zip");
                    CompressionUtils.zipFileList(zipFile.getAbsolutePath(), files);
                    inputToOutputStream = new MultipleInputToOutputStream(DWFile.get(zipFile), fileName + ".zip");
                }
                fileChooserDialog.setVisible(inputToOutputStream);
                directory.deleteDirContentRecursivelyWithRepeat();
                directory.deleteRecursively();
            }
        }
    }

    private DWFile makeFileFromAttachment(DWFile directory, iPartsDataPicOrderAttachment attachmentObject) {
        DWFile singleFile = DWFile.get(directory.getChild(attachmentObject.getName() + "." + attachmentObject.getFileType()));
        singleFile.saveByteArray(attachmentObject.getContent());
        return singleFile;
    }

    /**
     * Liste für Anhänge mit Anhängen füllen
     */
    private void fillAttachmentGridWithObjects() {
        List<iPartsDataPicOrderAttachment> chosenAttachments = dataPicOrder.getAttachments();
        for (iPartsDataPicOrderAttachment attachment : chosenAttachments) {
            attachmentGrid.addObjectToGrid(attachment);
        }
        attachmentGrid.showNoResultsLabel(chosenAttachments.isEmpty());
    }

    /**
     * Reaktiviert das Grid für die Anhänge eines Bildauftrags samt Toolbar-Buttons (z.B. wenn der komplette Bildauftrag
     * reaktiviert wird). Der Aufruf von {@link #doEnableButtons()} stellt dann den gewünschten Zustand wieder her.
     */
    public void reactivateAttachments() {
        attachmentGrid.showToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW);
        attachmentGrid.showToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK);
        attachmentGrid.showToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE);
        attachmentGrid.showToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DOWNLOAD);
        doEnableButtons();
    }

    private void doEnableButtons() {
        if (isEditAllowed && iPartsTransferStates.canEditAttachments(dataPicOrder.getStatus())) {
            attachmentGrid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW, true);
            if (attachmentGrid.getTable().getSelectedRow() == null) {
                attachmentGrid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, false);
                attachmentGrid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, false);
                attachmentGrid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DOWNLOAD, false);
            } else {
                if (attachmentGrid.getMultiSelection().size() > 1) {
                    attachmentGrid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, false);
                } else {
                    attachmentGrid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, true);
                }
                attachmentGrid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, canAttachmentsBeDeleted());
                attachmentGrid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DOWNLOAD, true);
            }

        } else {
            attachmentGrid.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW);
            attachmentGrid.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK);
            attachmentGrid.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE);
            attachmentGrid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DOWNLOAD, attachmentGrid.getTable().getSelectedRow() != null);
        }
    }

    /**
     * Überprüft, ob Anhänge gelöscht werden dürfen
     *
     * @return
     */
    private boolean canAttachmentsBeDeleted() {
        if (!isEditAllowed || !iPartsTransferStates.canEditAttachments(dataPicOrder.getStatus())) {
            return false;
        }
        // Nur neue Anhänge dürfen gelöscht werden
        for (List<EtkDataObject> selection : attachmentGrid.getMultiSelection()) {
            for (EtkDataObject dataObject : selection) {
                if ((dataObject instanceof iPartsDataPicOrderAttachment) && (((iPartsDataPicOrderAttachment)dataObject).getStatus() != iPartsAttachmentStatus.NEW)) {
                    return false;
                }
            }
        }
        return true;

    }

    @Override
    public EditModuleFormIConnector getConnector() {
        return (EditModuleFormIConnector)super.getConnector();
    }

    /**
     * Überprüft, ob man die Anhänge zu diesem Bildauftrag bearbeiten / löschen kann
     *
     * @param isCreate
     * @return
     */
    public boolean isAttachmentEditable(boolean isCreate) {
        if (!isCreate) {
            if (!iPartsTransferStates.isAttachmentEditableState(dataPicOrder.getStatus()) && !dataPicOrder.isNew()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Displayfields für die Liste der Anhänge
     *
     * @return
     */
    private EtkDisplayFields buildAttachmentDisplayFields() {
        EtkDisplayFields defaultDisplayFields = new EtkDisplayFields();
        EtkDisplayField displayField = new EtkDisplayField(iPartsConst.TABLE_DA_PICORDER_ATTACHMENTS, iPartsConst.FIELD_DPA_STATUS, false, false);
        defaultDisplayFields.addFeld(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_PICORDER_ATTACHMENTS, iPartsConst.FIELD_DPA_NAME, true, false);
        defaultDisplayFields.addFeld(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_PICORDER_ATTACHMENTS, iPartsConst.FIELD_DPA_FILETYPE, false, false);
        defaultDisplayFields.addFeld(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_PICORDER_ATTACHMENTS, iPartsConst.FIELD_DPA_SIZE, false, false);
        defaultDisplayFields.addFeld(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_PICORDER_ATTACHMENTS, iPartsConst.FIELD_DPA_DESC, false, false);
        defaultDisplayFields.addFeld(displayField);

        defaultDisplayFields.loadStandards(getConfig());
        return defaultDisplayFields;
    }

    /**
     * Gibt eine Liste zurück, die alle Dateien enthält, die der Benutzer ausgewählt hat
     *
     * @return
     */
    public List<DWFile> getFilesFromFileChooser() {
        GuiFileChooserDialog fileChooserDialog = new GuiFileChooserDialog(null, FileChooserPurpose.OPEN, GuiFileChooserDialog.FILE_MODE_FILES, null, false);
        fileChooserDialog.addFileOpenValidator(new GuiFileOpenValidatorForExtensions(StrUtils.mergeArrays(iPartsTransferConst.AttachmentBinaryFileTypes.getAsExtensionArray(),
                                                                                                          iPartsTransferConst.AttachmentTextFileTypes.getAsExtensionArray())));
        fileChooserDialog.setApproveButtonText("!!Anhänge auswählen");
        fileChooserDialog.setServerMode(false);
        fileChooserDialog.setMultiSelectionMode(true);
        fileChooserDialog.setVisible(true);
        return fileChooserDialog.getSelectedFiles();
    }

    /**
     * Überprüft die Gesamtgröße und die Anzahl der Anhänge. Es wird eine Warnung ausgegeben, falls die Gesamtgröße der Anhänge größer als der eingestellte max Wert ist
     * oder es mehr Anhänge sind, als zugelassen.
     *
     * @return
     */
    protected boolean checkAttachmentSize(boolean withMessage) {
        long attachmentSize = dataPicOrder.getAttachmentsByteSize();
        long attachmentSizeDifference = dataPicOrder.getAttachmentsByteSize() - dataPicOrder.MAX_ATTACHMENT_FILE_SIZE_IN_BYTES;
        int attachmentCount = attachmentGrid.getTable().getRowCount();
        handleAttachmentInfoPanel(attachmentSize, attachmentCount, attachmentSizeDifference);
        boolean oneOfTheLimitsExceeded = false;
        if (dataPicOrder.getAttachments().size() > iPartsDataPicOrder.MAX_AMOUNT_ATTACHMENT_FILES) {
            if (withMessage) {
                MessageDialog.showWarning(TranslationHandler.translate("!!Es dürfen nicht mehr als %1 Anhänge pro Bildauftrag verschickt werden. Aktuell sind es %2 Anhänge.",
                                                                       String.valueOf(iPartsDataPicOrder.MAX_AMOUNT_ATTACHMENT_FILES),
                                                                       String.valueOf(dataPicOrder.getAttachments().size())) +
                                          "\n\n" + TranslationHandler.translate("Bitte die Anzahl reduzieren."),
                                          "!!Gesamtgröße Anhänge");
            }
            oneOfTheLimitsExceeded = true;
        }
        if (!dataPicOrder.isAttachmentSizeValid()) {
            if (withMessage) {
                MessageDialog.showWarning(TranslationHandler.translate("!!Die Gesamtgröße der Anhänge darf %1 MB nicht überschreiten, liegt aber darüber.",
                                                                       String.valueOf(dataPicOrder.MAX_ATTACHMENT_FILE_SIZE_IN_MB)) + "\n\n"
                                          + TranslationHandler.translate("!!Bitte reduzieren Sie die Gesamtgröße der Anhänge um %1.",
                                                                         XMLImportExportHelper.makeViewForSize(String.valueOf(attachmentSizeDifference))));
            }
            oneOfTheLimitsExceeded = true;
        }
        return (!(oneOfTheLimitsExceeded));
    }

    private void handleAttachmentInfoPanel(long attachmentSize, int attachmentCount, long attachmentSizeDifference) {
        mainWindow.labelCountValue.setText(String.valueOf(attachmentCount));
        if (attachmentCount > dataPicOrder.MAX_AMOUNT_ATTACHMENT_FILES) {
            mainWindow.labelCountValue.setForegroundColor(Colors.clRed);
        } else {
            mainWindow.labelCountValue.setForegroundColor(Colors.clDefault);
        }

        if (attachmentSize > dataPicOrder.MAX_ATTACHMENT_FILE_SIZE_IN_BYTES) {
            mainWindow.labelSizeValue.setText(VALUE_LARGER_THAN_MAX_SIZE);
            mainWindow.labelSizeValue.setForegroundColor(Colors.clRed);
            mainWindow.labelRemoveSize.setVisible(true);
            mainWindow.labelRemoveSizeValue.setVisible(true);
            mainWindow.labelRemoveSizeValue.setForegroundColor(Colors.clRed);
            mainWindow.labelRemoveSizeValue.setText(XMLImportExportHelper.makeViewForSize(String.valueOf(attachmentSizeDifference)));
        } else {
            mainWindow.labelSizeValue.setText(XMLImportExportHelper.makeViewForSize(String.valueOf(attachmentSize)));
            mainWindow.labelRemoveSize.setVisible(false);
            mainWindow.labelRemoveSizeValue.setVisible(false);
            mainWindow.labelSizeValue.setForegroundColor(Colors.clDefault);
            mainWindow.labelRemoveSizeValue.setForegroundColor(Colors.clDefault);
        }
    }

    public void setPictureOrder(iPartsDataPicOrder pictureOrder) {
        this.dataPicOrder = pictureOrder;
        reactivateAttachments();
    }

    public void setEditAllowed(boolean isEditAllowed) {
        if (this.isEditAllowed != isEditAllowed) {
            this.isEditAllowed = isEditAllowed;
            doEnableButtons();
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    private de.docware.framework.modules.gui.controls.menu.GuiContextMenu contextMenuTableAttachments;

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelGrid;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelStatus;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelCount;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelCountValue;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelSize;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelSizeValue;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelRemoveSize;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelRemoveSizeValue;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            contextMenuTableAttachments = new de.docware.framework.modules.gui.controls.menu.GuiContextMenu();
            contextMenuTableAttachments.setName("contextMenuTableAttachments");
            contextMenuTableAttachments.__internal_setGenerationDpi(96);
            contextMenuTableAttachments.registerTranslationHandler(translationHandler);
            contextMenuTableAttachments.setScaleForResolution(true);
            contextMenuTableAttachments.setMinimumWidth(10);
            contextMenuTableAttachments.setMinimumHeight(10);
            contextMenuTableAttachments.setMenuName("contextMenuTableAttachments");
            contextMenuTableAttachments.setParentControl(this);
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
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
            panelGrid = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelGrid.setName("panelGrid");
            panelGrid.__internal_setGenerationDpi(96);
            panelGrid.registerTranslationHandler(translationHandler);
            panelGrid.setScaleForResolution(true);
            panelGrid.setMinimumWidth(10);
            panelGrid.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelGridLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelGrid.setLayout(panelGridLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelGridConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelGrid.setConstraints(panelGridConstraints);
            panelMain.addChild(panelGrid);
            panelStatus = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelStatus.setName("panelStatus");
            panelStatus.__internal_setGenerationDpi(96);
            panelStatus.registerTranslationHandler(translationHandler);
            panelStatus.setScaleForResolution(true);
            panelStatus.setMinimumWidth(10);
            panelStatus.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelStatusLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelStatus.setLayout(panelStatusLayout);
            labelCount = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelCount.setName("labelCount");
            labelCount.__internal_setGenerationDpi(96);
            labelCount.registerTranslationHandler(translationHandler);
            labelCount.setScaleForResolution(true);
            labelCount.setMinimumWidth(10);
            labelCount.setMinimumHeight(10);
            labelCount.setText("!!Anzahl Anhänge:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelCountConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "c", "n", 8, 8, 8, 4);
            labelCount.setConstraints(labelCountConstraints);
            panelStatus.addChild(labelCount);
            labelCountValue = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelCountValue.setName("labelCountValue");
            labelCountValue.__internal_setGenerationDpi(96);
            labelCountValue.registerTranslationHandler(translationHandler);
            labelCountValue.setScaleForResolution(true);
            labelCountValue.setMinimumWidth(10);
            labelCountValue.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelCountValueConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, "c", "n", 8, 0, 8, 8);
            labelCountValue.setConstraints(labelCountValueConstraints);
            panelStatus.addChild(labelCountValue);
            labelSize = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelSize.setName("labelSize");
            labelSize.__internal_setGenerationDpi(96);
            labelSize.registerTranslationHandler(translationHandler);
            labelSize.setScaleForResolution(true);
            labelSize.setMinimumWidth(10);
            labelSize.setMinimumHeight(10);
            labelSize.setText("!!Gesamtgröße:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelSizeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(2, 0, 1, 1, 0.0, 0.0, "c", "n", 8, 8, 8, 4);
            labelSize.setConstraints(labelSizeConstraints);
            panelStatus.addChild(labelSize);
            labelSizeValue = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelSizeValue.setName("labelSizeValue");
            labelSizeValue.__internal_setGenerationDpi(96);
            labelSizeValue.registerTranslationHandler(translationHandler);
            labelSizeValue.setScaleForResolution(true);
            labelSizeValue.setMinimumWidth(10);
            labelSizeValue.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelSizeValueConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(3, 0, 1, 1, 0.0, 0.0, "c", "n", 8, 0, 8, 8);
            labelSizeValue.setConstraints(labelSizeValueConstraints);
            panelStatus.addChild(labelSizeValue);
            labelRemoveSize = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelRemoveSize.setName("labelRemoveSize");
            labelRemoveSize.__internal_setGenerationDpi(96);
            labelRemoveSize.registerTranslationHandler(translationHandler);
            labelRemoveSize.setScaleForResolution(true);
            labelRemoveSize.setMinimumWidth(10);
            labelRemoveSize.setMinimumHeight(10);
            labelRemoveSize.setVisible(false);
            labelRemoveSize.setText("!!Übertrag:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelRemoveSizeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(4, 0, 1, 1, 0.0, 0.0, "c", "n", 8, 8, 8, 4);
            labelRemoveSize.setConstraints(labelRemoveSizeConstraints);
            panelStatus.addChild(labelRemoveSize);
            labelRemoveSizeValue = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelRemoveSizeValue.setName("labelRemoveSizeValue");
            labelRemoveSizeValue.__internal_setGenerationDpi(96);
            labelRemoveSizeValue.registerTranslationHandler(translationHandler);
            labelRemoveSizeValue.setScaleForResolution(true);
            labelRemoveSizeValue.setMinimumWidth(10);
            labelRemoveSizeValue.setMinimumHeight(10);
            labelRemoveSizeValue.setVisible(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelRemoveSizeValueConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(5, 0, 1, 1, 0.0, 0.0, "c", "n", 8, 0, 8, 8);
            labelRemoveSizeValue.setConstraints(labelRemoveSizeValueConstraints);
            panelStatus.addChild(labelRemoveSizeValue);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelStatusConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelStatusConstraints.setPosition("south");
            panelStatus.setConstraints(panelStatusConstraints);
            panelMain.addChild(panelStatus);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMainConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMain.setConstraints(panelMainConstraints);
            this.addChild(panelMain);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}