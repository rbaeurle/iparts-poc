package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.picorder;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsAttachmentStatus;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.attachments.iPartsDataPicOrderAttachment;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.attachments.iPartsDataPicOrderAttachmentList;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.attachments.iPartsPicOrderAttachmentId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleFormIConnector;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.filechooser.FileChooserPurpose;
import de.docware.framework.modules.gui.controls.filechooser.GuiFileOpenValidatorForExtensions;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.List;

/**
 * Dialog zum Anzeigen, Erstellen, Bearbeiten und Löschen von Bildaufträgen
 */
public class PicOrderAttachmentInfoForm extends AbstractJavaViewerForm implements iPartsConst {

    private iPartsDataPicOrderAttachment dataPicOrderAttachment;
    private DWFile currentAttachmentFile;
    private boolean editMode;
    private boolean viewOnlyMode;

    /**
     * Erzeugt eine Instanz von EditPictureOrderAttachmentForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public PicOrderAttachmentInfoForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        postCreateGui();
    }

    public void setViewOnlyMode(boolean viewOnlyMode) {
        this.viewOnlyMode = viewOnlyMode;
        mainWindow.buttonpanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.CANCEL).setVisible(false);
        mainWindow.textareaDesc.setEnabled(false);
        mainWindow.textfieldName.setEnabled(false);

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
        enableAttachmentPanel(false);
        mainWindow.fileChooserTextField.setPurpose(FileChooserPurpose.OPEN);
        mainWindow.fileChooserTextField.setEditable(false);
        mainWindow.fileChooserTextField.setMultiSelectionMode(false);
        mainWindow.fileChooserTextField.setServerMode(false);
        mainWindow.fileChooserTextField.addFileOpenValidator(new GuiFileOpenValidatorForExtensions(StrUtils.mergeArrays(iPartsTransferConst.AttachmentBinaryFileTypes.getAsExtensionArray(),
                                                                                                                        iPartsTransferConst.AttachmentTextFileTypes.getAsExtensionArray())));
        mainWindow.fileChooserTextField.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                if (!StrUtils.isEmpty(mainWindow.fileChooserTextField.getSelectedFileName())) {
                    fillAttInformationFromFile(false);
                }
            }
        });
    }

    /**
     * Befüllt den Dialog mit den Daten der Datei
     *
     * @param preChosenFileExist
     */
    private void fillAttInformationFromFile(boolean preChosenFileExist) {
        // Infopanel freigeben
        enableAttachmentPanel(true);
        // Datei von FileChooser holen
        if (!preChosenFileExist) {
            currentAttachmentFile = mainWindow.fileChooserTextField.getSelectedFile();
        }
        // Name setzen
        mainWindow.textfieldName.setText(StrUtils.removeAllLastCharacterIfCharacterIs(currentAttachmentFile.getName(), "." + currentAttachmentFile.getExtension()));
        // Dateiendung setzen
        mainWindow.textfieldFileType.setText(currentAttachmentFile.getExtension());
        // Größe berechnen
        computeFileSize();
        mainWindow.textfieldStatus.setText(iPartsAttachmentStatus.NEW.getStatus());
    }

    private void enableAttachmentPanel(boolean enabled) {
        if (!viewOnlyMode) {
            mainWindow.attachmentInfoPanel.setEnabled(enabled);
        } else {
            mainWindow.attachmentInfoPanel.setEnabled(true);
            mainWindow.textfieldName.setEditable(false);
            mainWindow.textareaDesc.setEditable(false);
        }
    }

    private void enableFileChooserPanel() {
        if (!viewOnlyMode) {
            mainWindow.filechooserPanel.setEnabled(true);
        } else {
            mainWindow.filechooserPanel.setVisible(false);
        }
    }

    private void fillAttInformationFromDBObject(iPartsDataPicOrderAttachment attachment) {
        dataPicOrderAttachment = attachment;
        enableFileChooserPanel();
        enableAttachmentPanel(true);
        // Name setzen
        mainWindow.textfieldName.setText(attachment.getName());
        // Dateiendung setzen
        mainWindow.textfieldFileType.setText(attachment.getFileType());
        // Größe berechnen
        mainWindow.textfieldSize.setText(XMLImportExportHelper.makeViewForSize(attachment.getSize()));
        // Status setzen
        mainWindow.textfieldStatus.setText(attachment.getStatus().getStatus());
        // Status setzen
        mainWindow.textareaDesc.setText(attachment.getDesc());
        // Errors anzeigen
        if (attachment.hasErrors()) {
            mainWindow.textfieldErrorCode.setText(attachment.getErrorCode());
            mainWindow.textfieldErrorText.setText(attachment.getErrorText());
        }
    }

    /**
     * Berechnet die Dateigröße und gibt sie als String zurück
     */
    private void computeFileSize() {
        mainWindow.textfieldSize.setText(XMLImportExportHelper.makeViewForSize(String.valueOf(currentAttachmentFile.length())));
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public ModalResult showModal() {
        mainWindow.pack();
        ModalResult modalResult = mainWindow.showModal();
        super.close();
        return modalResult;
    }

    /**
     * Zeigt den Dialog zum Erstellen eines Anhangs
     *
     * @param connector
     * @param parentForm
     * @param dataPicOrder
     * @param dwFile
     */
    public static void showCreateAttachmentDialog(EditModuleFormIConnector connector, AbstractJavaViewerForm parentForm,
                                                  iPartsDataPicOrder dataPicOrder, DWFile dwFile) {
        PicOrderAttachmentInfoForm dlg = new PicOrderAttachmentInfoForm(connector, parentForm);
        if (dwFile != null) {
            dlg.setPreChosenFile(dwFile);
        }
        if (dlg.showModal() == ModalResult.OK) {
            dataPicOrder.addAttachment(dlg.getDataPicOrderAttachment(), DBActionOrigin.FROM_EDIT);
        }
        dlg.dispose();
    }

    public static void showEditAttachmentDialog(EditModuleFormIConnector connector, AbstractJavaViewerForm parentForm,
                                                iPartsDataPicOrderAttachment attachment, boolean attachmentEditable) {
        PicOrderAttachmentInfoForm dlg = new PicOrderAttachmentInfoForm(connector, parentForm);
        if (attachmentEditable) {
            dlg.setTitle("Anhang bearbeiten");
        } else {
            dlg.setTitle("Informationen zum Anhang");
            dlg.setViewOnlyMode(true);
        }
        dlg.setEditMode(true);
        if (attachment != null) {
            dlg.fillAttInformationFromDBObject(attachment);
        }
        dlg.showModal();
    }

    /**
     * Verarbeitet die ausgewählten Dateien ohne einen Dialog anzuzeigen
     *
     * @param connector
     * @param parentForm
     * @param dataPicOrder
     * @param selectedFiles
     */
    public static void handleFilesWithoutDialog(EditModuleFormIConnector connector, AbstractJavaViewerForm parentForm, iPartsDataPicOrder dataPicOrder, List<DWFile> selectedFiles) {
        if ((selectedFiles != null) && !selectedFiles.isEmpty()) {
            PicOrderAttachmentInfoForm dlg = new PicOrderAttachmentInfoForm(connector, parentForm);
            dataPicOrder.addAttachments(dlg.handleAttachmentsWithoutDialog(selectedFiles), DBActionOrigin.FROM_EDIT);
        }
    }

    /**
     * Erstellt Bildauftrag-Anhänge aus den übergebenen Dateien
     *
     * @param selectedFiles
     * @return
     */
    private List<iPartsDataPicOrderAttachment> handleAttachmentsWithoutDialog(List<DWFile> selectedFiles) {
        iPartsDataPicOrderAttachmentList list = new iPartsDataPicOrderAttachmentList();
        for (DWFile file : selectedFiles) {
            setPreChosenFile(file);
            guiToData();
            list.add(dataPicOrderAttachment, DBActionOrigin.FROM_EDIT);
            dataPicOrderAttachment = null;
            currentAttachmentFile = null;
        }
        return list.getAsList();

    }

    private void onOKButtonClick(Event event) {
        if (checkData()) {
            guiToData();
            mainWindow.setModalResult(ModalResult.OK);
            close();
        }
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }

    private boolean checkData() {
        if (StrUtils.isEmpty(mainWindow.textfieldName.getText())) {
            MessageDialog.showWarning(TranslationHandler.translate("!!Das Feld \"Name\" darf nicht leer sein!"));
            return false;
        }
        return true;
    }

    /**
     * Überträgt die Daten aus dem Dialog in ein {@link iPartsDataPicOrderAttachment} Objekt
     */
    private void guiToData() {
        if (!editMode) {
            iPartsPicOrderAttachmentId id = new iPartsPicOrderAttachmentId(StrUtils.makeGUID());
            dataPicOrderAttachment = new iPartsDataPicOrderAttachment(getProject(), id);
            dataPicOrderAttachment.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }
        // currentAttachmentFile ist nur gesetzt wenn eine neue Datei geladen wurde
        if (currentAttachmentFile != null) {
            dataPicOrderAttachment.setFieldValue(FIELD_DPA_SIZE, String.valueOf(currentAttachmentFile.length()), DBActionOrigin.FROM_EDIT);
            dataPicOrderAttachment.setFieldValueAsBlob(FIELD_DPA_CONTENT, currentAttachmentFile.readByteArray(), DBActionOrigin.FROM_EDIT);
            byte[] fileSizeBase64 = XMLImportExportHelper.convertDWFileToBase64String(currentAttachmentFile).getBytes();
            dataPicOrderAttachment.setFieldValue(FIELD_DPA_SIZE_BASE64, String.valueOf(fileSizeBase64.length), DBActionOrigin.FROM_EDIT);
            dataPicOrderAttachment.setFieldValue(FIELD_DPA_STATUS, iPartsAttachmentStatus.NEW.getDBStatus(), DBActionOrigin.FROM_EDIT);
        }
        dataPicOrderAttachment.setFieldValue(FIELD_DPA_FILETYPE, mainWindow.textfieldFileType.getText(), DBActionOrigin.FROM_EDIT);
        dataPicOrderAttachment.setFieldValue(FIELD_DPA_NAME, mainWindow.textfieldName.getText(), DBActionOrigin.FROM_EDIT);
        dataPicOrderAttachment.setFieldValue(FIELD_DPA_DESC, mainWindow.textareaDesc.getText(), DBActionOrigin.FROM_EDIT);
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    /**
     * Setzt eine zuvor vom Benutzer ausgewählte Datei
     *
     * @param preChosenFile
     */
    public void setPreChosenFile(DWFile preChosenFile) {
        if (preChosenFile != null) {
            currentAttachmentFile = preChosenFile;
            fillAttInformationFromFile(true);
        }
    }

    public iPartsDataPicOrderAttachment getDataPicOrderAttachment() {
        return dataPicOrderAttachment;
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
        private de.docware.framework.modules.gui.controls.GuiPanel attachmentInfoPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel nameLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfieldName;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel sizeLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfieldSize;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel filetypeLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfieldFileType;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel statusLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfieldStatus;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel errorCodeLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfieldErrorCode;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel errortextLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfieldErrorText;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel descLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane scrollpaneDesc;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextArea textareaDesc;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel filechooserPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel fileChooserLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.filechooser.GuiFileChooserTextfield fileChooserTextField;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setBorderWidth(2);
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
            title.setTitle("!!Anhang zu Bildauftrag hinzufügen");
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
            de.docware.framework.modules.gui.layout.LayoutGridBag panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelMain.setLayout(panelMainLayout);
            attachmentInfoPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            attachmentInfoPanel.setName("attachmentInfoPanel");
            attachmentInfoPanel.__internal_setGenerationDpi(96);
            attachmentInfoPanel.registerTranslationHandler(translationHandler);
            attachmentInfoPanel.setScaleForResolution(true);
            attachmentInfoPanel.setMinimumWidth(10);
            attachmentInfoPanel.setMinimumHeight(10);
            attachmentInfoPanel.setTitle("!!Informationen zum Anhang");
            de.docware.framework.modules.gui.layout.LayoutGridBag attachmentInfoPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            attachmentInfoPanel.setLayout(attachmentInfoPanelLayout);
            nameLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            nameLabel.setName("nameLabel");
            nameLabel.__internal_setGenerationDpi(96);
            nameLabel.registerTranslationHandler(translationHandler);
            nameLabel.setScaleForResolution(true);
            nameLabel.setMinimumWidth(10);
            nameLabel.setMinimumHeight(10);
            nameLabel.setFontStyle(1);
            nameLabel.setText("!!Name:");
            nameLabel.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag nameLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "e", "n", 4, 8, 4, 4);
            nameLabel.setConstraints(nameLabelConstraints);
            attachmentInfoPanel.addChild(nameLabel);
            textfieldName = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfieldName.setName("textfieldName");
            textfieldName.__internal_setGenerationDpi(96);
            textfieldName.registerTranslationHandler(translationHandler);
            textfieldName.setScaleForResolution(true);
            textfieldName.setMinimumWidth(200);
            textfieldName.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfieldNameConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, "c", "h", 4, 4, 4, 8);
            textfieldName.setConstraints(textfieldNameConstraints);
            attachmentInfoPanel.addChild(textfieldName);
            sizeLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            sizeLabel.setName("sizeLabel");
            sizeLabel.__internal_setGenerationDpi(96);
            sizeLabel.registerTranslationHandler(translationHandler);
            sizeLabel.setScaleForResolution(true);
            sizeLabel.setMinimumWidth(10);
            sizeLabel.setMinimumHeight(10);
            sizeLabel.setText("!!Dateigröße:");
            sizeLabel.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag sizeLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "e", "n", 4, 8, 4, 4);
            sizeLabel.setConstraints(sizeLabelConstraints);
            attachmentInfoPanel.addChild(sizeLabel);
            textfieldSize = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfieldSize.setName("textfieldSize");
            textfieldSize.__internal_setGenerationDpi(96);
            textfieldSize.registerTranslationHandler(translationHandler);
            textfieldSize.setScaleForResolution(true);
            textfieldSize.setMinimumWidth(200);
            textfieldSize.setMinimumHeight(10);
            textfieldSize.setEnabled(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfieldSizeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 1, 1, 1, 0.0, 0.0, "c", "h", 4, 4, 4, 8);
            textfieldSize.setConstraints(textfieldSizeConstraints);
            attachmentInfoPanel.addChild(textfieldSize);
            filetypeLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            filetypeLabel.setName("filetypeLabel");
            filetypeLabel.__internal_setGenerationDpi(96);
            filetypeLabel.registerTranslationHandler(translationHandler);
            filetypeLabel.setScaleForResolution(true);
            filetypeLabel.setMinimumWidth(10);
            filetypeLabel.setMinimumHeight(10);
            filetypeLabel.setText("!!Dateiendung:");
            filetypeLabel.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag filetypeLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 0.0, 0.0, "e", "n", 4, 8, 4, 4);
            filetypeLabel.setConstraints(filetypeLabelConstraints);
            attachmentInfoPanel.addChild(filetypeLabel);
            textfieldFileType = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfieldFileType.setName("textfieldFileType");
            textfieldFileType.__internal_setGenerationDpi(96);
            textfieldFileType.registerTranslationHandler(translationHandler);
            textfieldFileType.setScaleForResolution(true);
            textfieldFileType.setMinimumWidth(200);
            textfieldFileType.setMinimumHeight(10);
            textfieldFileType.setEnabled(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfieldFileTypeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 2, 1, 1, 0.0, 0.0, "c", "h", 4, 4, 4, 8);
            textfieldFileType.setConstraints(textfieldFileTypeConstraints);
            attachmentInfoPanel.addChild(textfieldFileType);
            statusLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            statusLabel.setName("statusLabel");
            statusLabel.__internal_setGenerationDpi(96);
            statusLabel.registerTranslationHandler(translationHandler);
            statusLabel.setScaleForResolution(true);
            statusLabel.setMinimumWidth(10);
            statusLabel.setMinimumHeight(10);
            statusLabel.setText("!!Status:");
            statusLabel.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag statusLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 3, 1, 1, 0.0, 0.0, "e", "n", 4, 8, 4, 4);
            statusLabel.setConstraints(statusLabelConstraints);
            attachmentInfoPanel.addChild(statusLabel);
            textfieldStatus = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfieldStatus.setName("textfieldStatus");
            textfieldStatus.__internal_setGenerationDpi(96);
            textfieldStatus.registerTranslationHandler(translationHandler);
            textfieldStatus.setScaleForResolution(true);
            textfieldStatus.setMinimumWidth(200);
            textfieldStatus.setMinimumHeight(10);
            textfieldStatus.setEnabled(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfieldStatusConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 3, 1, 1, 0.0, 0.0, "c", "h", 4, 4, 4, 8);
            textfieldStatus.setConstraints(textfieldStatusConstraints);
            attachmentInfoPanel.addChild(textfieldStatus);
            errorCodeLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            errorCodeLabel.setName("errorCodeLabel");
            errorCodeLabel.__internal_setGenerationDpi(96);
            errorCodeLabel.registerTranslationHandler(translationHandler);
            errorCodeLabel.setScaleForResolution(true);
            errorCodeLabel.setMinimumWidth(10);
            errorCodeLabel.setMinimumHeight(10);
            errorCodeLabel.setText("!!Fehlernummer:");
            errorCodeLabel.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag errorCodeLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 4, 1, 1, 0.0, 0.0, "e", "n", 4, 8, 4, 4);
            errorCodeLabel.setConstraints(errorCodeLabelConstraints);
            attachmentInfoPanel.addChild(errorCodeLabel);
            textfieldErrorCode = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfieldErrorCode.setName("textfieldErrorCode");
            textfieldErrorCode.__internal_setGenerationDpi(96);
            textfieldErrorCode.registerTranslationHandler(translationHandler);
            textfieldErrorCode.setScaleForResolution(true);
            textfieldErrorCode.setMinimumWidth(200);
            textfieldErrorCode.setMinimumHeight(10);
            textfieldErrorCode.setEnabled(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfieldErrorCodeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 4, 1, 1, 0.0, 0.0, "c", "h", 4, 4, 4, 8);
            textfieldErrorCode.setConstraints(textfieldErrorCodeConstraints);
            attachmentInfoPanel.addChild(textfieldErrorCode);
            errortextLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            errortextLabel.setName("errortextLabel");
            errortextLabel.__internal_setGenerationDpi(96);
            errortextLabel.registerTranslationHandler(translationHandler);
            errortextLabel.setScaleForResolution(true);
            errortextLabel.setMinimumWidth(10);
            errortextLabel.setMinimumHeight(10);
            errortextLabel.setText("!!Fehlertext:");
            errortextLabel.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag errortextLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 5, 1, 1, 0.0, 0.0, "e", "n", 4, 8, 4, 4);
            errortextLabel.setConstraints(errortextLabelConstraints);
            attachmentInfoPanel.addChild(errortextLabel);
            textfieldErrorText = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfieldErrorText.setName("textfieldErrorText");
            textfieldErrorText.__internal_setGenerationDpi(96);
            textfieldErrorText.registerTranslationHandler(translationHandler);
            textfieldErrorText.setScaleForResolution(true);
            textfieldErrorText.setMinimumWidth(200);
            textfieldErrorText.setMinimumHeight(10);
            textfieldErrorText.setEnabled(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfieldErrorTextConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 5, 1, 1, 0.0, 0.0, "c", "h", 4, 4, 4, 8);
            textfieldErrorText.setConstraints(textfieldErrorTextConstraints);
            attachmentInfoPanel.addChild(textfieldErrorText);
            descLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            descLabel.setName("descLabel");
            descLabel.__internal_setGenerationDpi(96);
            descLabel.registerTranslationHandler(translationHandler);
            descLabel.setScaleForResolution(true);
            descLabel.setMinimumWidth(10);
            descLabel.setMinimumHeight(10);
            descLabel.setText("!!Beschreibung:");
            descLabel.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag descLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 6, 1, 1, 0.0, 0.0, "ne", "n", 4, 8, 8, 4);
            descLabel.setConstraints(descLabelConstraints);
            attachmentInfoPanel.addChild(descLabel);
            scrollpaneDesc = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            scrollpaneDesc.setName("scrollpaneDesc");
            scrollpaneDesc.__internal_setGenerationDpi(96);
            scrollpaneDesc.registerTranslationHandler(translationHandler);
            scrollpaneDesc.setScaleForResolution(true);
            scrollpaneDesc.setMinimumWidth(200);
            scrollpaneDesc.setMinimumHeight(100);
            scrollpaneDesc.setBorderWidth(1);
            scrollpaneDesc.setBorderColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignTextFieldBorder"));
            textareaDesc = new de.docware.framework.modules.gui.controls.GuiTextArea();
            textareaDesc.setName("textareaDesc");
            textareaDesc.__internal_setGenerationDpi(96);
            textareaDesc.registerTranslationHandler(translationHandler);
            textareaDesc.setScaleForResolution(true);
            textareaDesc.setMinimumWidth(200);
            textareaDesc.setMinimumHeight(100);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder textareaDescConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            textareaDesc.setConstraints(textareaDescConstraints);
            scrollpaneDesc.addChild(textareaDesc);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag scrollpaneDescConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 6, 1, 1, 0.0, 0.0, "c", "h", 4, 4, 8, 8);
            scrollpaneDesc.setConstraints(scrollpaneDescConstraints);
            attachmentInfoPanel.addChild(scrollpaneDesc);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag attachmentInfoPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "c", "h", 8, 8, 4, 8);
            attachmentInfoPanel.setConstraints(attachmentInfoPanelConstraints);
            panelMain.addChild(attachmentInfoPanel);
            filechooserPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            filechooserPanel.setName("filechooserPanel");
            filechooserPanel.__internal_setGenerationDpi(96);
            filechooserPanel.registerTranslationHandler(translationHandler);
            filechooserPanel.setScaleForResolution(true);
            filechooserPanel.setMinimumWidth(10);
            filechooserPanel.setMinimumHeight(10);
            filechooserPanel.setTitle("!!Datei ersetzen");
            de.docware.framework.modules.gui.layout.LayoutGridBag filechooserPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            filechooserPanel.setLayout(filechooserPanelLayout);
            fileChooserLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            fileChooserLabel.setName("fileChooserLabel");
            fileChooserLabel.__internal_setGenerationDpi(96);
            fileChooserLabel.registerTranslationHandler(translationHandler);
            fileChooserLabel.setScaleForResolution(true);
            fileChooserLabel.setMinimumWidth(10);
            fileChooserLabel.setMinimumHeight(10);
            fileChooserLabel.setText("!!Datei:");
            fileChooserLabel.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag fileChooserLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "e", "n", 4, 8, 4, 4);
            fileChooserLabel.setConstraints(fileChooserLabelConstraints);
            filechooserPanel.addChild(fileChooserLabel);
            fileChooserTextField = new de.docware.framework.modules.gui.controls.filechooser.GuiFileChooserTextfield();
            fileChooserTextField.setName("fileChooserTextField");
            fileChooserTextField.__internal_setGenerationDpi(96);
            fileChooserTextField.registerTranslationHandler(translationHandler);
            fileChooserTextField.setScaleForResolution(true);
            fileChooserTextField.setMinimumWidth(200);
            fileChooserTextField.setMinimumHeight(10);
            fileChooserTextField.setApproveButtonText("!!Anhang auswählen");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag fileChooserTextFieldConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 100.0, 0.0, "c", "h", 4, 4, 4, 8);
            fileChooserTextField.setConstraints(fileChooserTextFieldConstraints);
            filechooserPanel.addChild(fileChooserTextField);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag filechooserPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "c", "h", 4, 8, 8, 8);
            filechooserPanel.setConstraints(filechooserPanelConstraints);
            panelMain.addChild(filechooserPanel);
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
                    onOKButtonClick(event);
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