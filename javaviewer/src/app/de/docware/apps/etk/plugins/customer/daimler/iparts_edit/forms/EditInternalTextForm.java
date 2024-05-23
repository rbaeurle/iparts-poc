/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.ZipFilesListDialog;
import de.docware.apps.etk.base.forms.events.OnChangeEvent;
import de.docware.apps.etk.base.relatedinfo.note.forms.NoteEditDialog;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataInternalText;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.Utils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWOutputStream;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import static de.docware.framework.modules.db.DBDatabaseDomain.MAIN;

public class EditInternalTextForm extends AbstractJavaViewerForm {

    public static iPartsDataInternalText editInternalTextForm(AbstractJavaViewerForm parentForm, iPartsDataInternalText dataInternalText) {
        return startInternalTextForm(parentForm, dataInternalText, NoteEditDialog.NoteEditState.EDIT);
    }

    public static void showInternalTextForm(AbstractJavaViewerForm parentForm, iPartsDataInternalText dataInternalText) {
        startInternalTextForm(parentForm, dataInternalText, NoteEditDialog.NoteEditState.SHOW);
    }

    public static iPartsDataInternalText createInternalTextForm(AbstractJavaViewerForm parentForm, iPartsDataInternalText dataInternalText) {
        return startInternalTextForm(parentForm, dataInternalText, NoteEditDialog.NoteEditState.NEW);
    }

    private static iPartsDataInternalText startInternalTextForm(AbstractJavaViewerForm parentForm, iPartsDataInternalText dataInternalText,
                                                                NoteEditDialog.NoteEditState editState) {
        EditInternalTextForm dlg = new EditInternalTextForm(parentForm.getConnector(), parentForm, dataInternalText, editState);
        if (dlg.showModal() == ModalResult.OK) {
            iPartsDataInternalText result = dlg.getDataInternalText();

            // Änderungsdatum initial setzen bzw. aktualisieren
            if (result != null) {
                if (editState == NoteEditDialog.NoteEditState.NEW) {
                    result.setChangeTimeStamp(result.getCreationTimeStamp(), DBActionOrigin.FROM_EDIT);
                } else if (editState == NoteEditDialog.NoteEditState.EDIT) {
                    result.setChangeTimeStamp(Calendar.getInstance(), DBActionOrigin.FROM_EDIT);
                }
            }
            return result;
        }
        return null;
    }

    private ZipFilesListDialog zipDialog;
    private iPartsDataInternalText dataInternalText;
    private DWFile zipFile;
    private boolean isOK;


    /**
     * Erzeugt eine Instanz von EditInternalTextForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditInternalTextForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                iPartsDataInternalText dataInternalText, NoteEditDialog.NoteEditState editState) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.dataInternalText = dataInternalText;
        this.isOK = false;
        postCreateGui();

        setEditState(editState);
        loadData();
        mainWindow.pack();
        mainWindow.textarea.requestFocus();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        zipDialog = new ZipFilesListDialog(getConnector(), this);
        zipDialog.setTableWidth(420);
        zipDialog.getGui().setMinimumHeight(50);
        zipDialog.getGui().setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        mainWindow.panelAttachment.addChild(zipDialog.getGui());
        zipDialog.setOnChangeEvent(new OnChangeEvent() {
            @Override
            public void onChange() {
                enableButtons();
            }
        });
    }

    private void setEditState(NoteEditDialog.NoteEditState editState) {
        switch (editState) {
            case NEW:
                setTitle("!!Neuen Internen Text anlegen");
                mainWindow.buttonpanel.setDialogStyle(GuiButtonPanel.DialogStyle.DIALOG);
                zipDialog.setReadOnly(false);
                break;
            case EDIT:
                setTitle("!!Internen Text bearbeiten");
                mainWindow.buttonpanel.setDialogStyle(GuiButtonPanel.DialogStyle.DIALOG);
                zipDialog.setReadOnly(false);
                break;

            case SHOW:
                setTitle("!!Internen Text ansehen");
                mainWindow.buttonpanel.setDialogStyle(GuiButtonPanel.DialogStyle.CLOSE);
                zipDialog.setReadOnly(true);

                mainWindow.textarea.setEditable(false);
                break;
        }
    }

    public iPartsDataInternalText getDataInternalText() {
        if (isOK) {
            return dataInternalText;
        }
        return null;
    }

    public DWFile getZipFile() {
        if (zipFile == null) {
            try {
                zipFile = DWFile.get(File.createTempFile("internalText", ".zip"));
            } catch (IOException e) {
                Logger.getLogger().throwRuntimeException(e);
            }
        }
        return zipFile;
    }

    public void emptyZipFile() {
        zipFile.delete();
        zipFile = null;
        getZipFile();
    }

    private void loadData() {
        if (dataInternalText == null) {
            dataInternalText = new iPartsDataInternalText(getProject(), null);
            dataInternalText.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        }
        loadAttachment();
        zipDialog.setZipFileName(getZipFile());
        mainWindow.textarea.setText(dataInternalText.getText());
    }

    private boolean loadAttachment() {
        byte[] byteArray = dataInternalText.getAttachment();
        if (byteArray != null) {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(byteArray);
            if (byteStream != null) {
                DWOutputStream fileOut = null;
                try {
                    fileOut = getZipFile().getOutputStream();
                    Utils.copyInputStreamToOutputStream(byteStream, fileOut, true, true);
                    return true;
                } catch (IOException e) {
                    Logger.getLogger().throwRuntimeException(e);
                    return false;
                } finally {
                    try {
                        if (fileOut != null) {
                            fileOut.flush();
                            fileOut.close();
                        }
                        byteStream.close();
                    } catch (IOException e) {
                        Logger.getLogger().throwRuntimeException(e);
                    }
                }
            }
        }
        return false;
    }

    private boolean saveAttachment(iPartsDataInternalText dataInternalText) {
        zipDialog.saveData(true);
        if ((getZipFile().exists() && (getZipFile().length() > 0) && (getZipFile().length() < getProject().getDB().getDBForDomain(MAIN).getMaxBlobSize()))) {
            dataInternalText.setAttachment(getZipFile().readByteArray());
            return true;
        } else {
            dataInternalText.setAttachment(new byte[]{});
            return false;
        }
    }

    private boolean isModified() {
        iPartsDataInternalText testDataInternalText = new iPartsDataInternalText(getProject(), dataInternalText.getAsId());
        testDataInternalText.setAttributes(dataInternalText.getAttributes(), DBActionOrigin.FROM_DB);
        for (DBDataObjectAttribute attribute : testDataInternalText.getAttributes().values()) {
            attribute.setLoaded(true); // setzt automatisch das modified-Flag zurück
        }
        testDataInternalText.setText(mainWindow.textarea.getText());
        saveAttachment(testDataInternalText);
        return testDataInternalText.isModified();
    }

    private void enableButtons() {
        mainWindow.buttonpanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, isModified());
    }

    private void onButtonOkClick(Event event) {
        dataInternalText.setText(mainWindow.textarea.getText());
        saveAttachment(dataInternalText);

        isOK = true;
        mainWindow.setVisible(false);
//        mainWindow.setModalResult(ModalResult.OK);
    }

    private void onTextAreaChangeEvent(Event event) {
        enableButtons();
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public ModalResult showModal() {
        ModalResult modalResult = mainWindow.showModal();
        zipFile.delete();
        close();
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
        private de.docware.framework.modules.gui.controls.GuiLabel labelAttachment;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelAttachment;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane scrollerForEdit;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextArea textarea;

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
            de.docware.framework.modules.gui.layout.LayoutGridBag panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelMain.setLayout(panelMainLayout);
            labelAttachment = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelAttachment.setName("labelAttachment");
            labelAttachment.__internal_setGenerationDpi(96);
            labelAttachment.registerTranslationHandler(translationHandler);
            labelAttachment.setScaleForResolution(true);
            labelAttachment.setMinimumWidth(10);
            labelAttachment.setMinimumHeight(10);
            labelAttachment.setText("!!Anlagen:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelAttachmentConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "w", "n", 0, 8, 0, 8);
            labelAttachment.setConstraints(labelAttachmentConstraints);
            panelMain.addChild(labelAttachment);
            panelAttachment = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelAttachment.setName("panelAttachment");
            panelAttachment.__internal_setGenerationDpi(96);
            panelAttachment.registerTranslationHandler(translationHandler);
            panelAttachment.setScaleForResolution(true);
            panelAttachment.setMinimumWidth(10);
            panelAttachment.setMinimumHeight(100);
            de.docware.framework.modules.gui.layout.LayoutBorder panelAttachmentLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelAttachment.setLayout(panelAttachmentLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelAttachmentConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 100.0, 0.0, "c", "h", 0, 0, 0, 8);
            panelAttachment.setConstraints(panelAttachmentConstraints);
            panelMain.addChild(panelAttachment);
            scrollerForEdit = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            scrollerForEdit.setName("scrollerForEdit");
            scrollerForEdit.__internal_setGenerationDpi(96);
            scrollerForEdit.registerTranslationHandler(translationHandler);
            scrollerForEdit.setScaleForResolution(true);
            scrollerForEdit.setMinimumWidth(500);
            scrollerForEdit.setMinimumHeight(130);
            scrollerForEdit.setBorderWidth(1);
            scrollerForEdit.setBorderColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignTextFieldBorder"));
            textarea = new de.docware.framework.modules.gui.controls.GuiTextArea();
            textarea.setName("textarea");
            textarea.__internal_setGenerationDpi(96);
            textarea.registerTranslationHandler(translationHandler);
            textarea.setScaleForResolution(true);
            textarea.setMinimumWidth(500);
            textarea.setMinimumHeight(63);
            textarea.setLineWrap(true);
            textarea.setPlaceHolderText("!!Geben Sie hier Ihren Text ein");
            textarea.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onTextAreaChangeEvent(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder textareaConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            textarea.setConstraints(textareaConstraints);
            scrollerForEdit.addChild(textarea);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag scrollerForEditConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 2, 1, 100.0, 100.0, "c", "b", 8, 8, 8, 8);
            scrollerForEdit.setConstraints(scrollerForEditConstraints);
            panelMain.addChild(scrollerForEdit);
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
                    onButtonOkClick(event);
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