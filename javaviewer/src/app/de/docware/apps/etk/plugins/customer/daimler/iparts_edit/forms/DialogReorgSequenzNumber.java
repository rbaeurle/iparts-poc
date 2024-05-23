/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditDatabaseHelper;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.db.etkrecord.EtkRecord;
import de.docware.framework.modules.db.etkrecord.EtkRecords;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;


/**
 * Dialog zum Reorganisieren der Sequenznummer in der Stückliste
 */
public class DialogReorgSequenzNumber extends AbstractJavaViewerForm {

    boolean flagBreak = false;

    public static void showForm(AbstractJavaViewerForm parentForm) {
        DialogReorgSequenzNumber dlg = new DialogReorgSequenzNumber(parentForm.getConnector(), parentForm);
        dlg.showModal();
    }

    /**
     * Erzeugt eine Instanz von DialogReorgSequenzNumber.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public DialogReorgSequenzNumber(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        mainWindow.buttonPanel.setButtonVisible(EnumSet.of(GuiButtonOnPanel.ButtonType.START, GuiButtonOnPanel.ButtonType.CLOSE));
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

    private void cancelButtonClicked(Event event) {
        flagBreak = true;
    }


    private void startButtonClicked(Event event) {
        flagBreak = false;
        mainWindow.buttonPanel.setButtonVisible(EnumSet.of(GuiButtonOnPanel.ButtonType.CANCEL));

        Session.startChildThreadInSession(thread -> {
            List<AssemblyId> assemblies = getSelectedAssemblies();

            for (AssemblyId id : assemblies) {
                if (flagBreak) {
                    messageLog(TranslationHandler.translate("!!Reorganisation wurde vom Benutzer abgebrochen."));
                    break;
                }

                iPartsDataAssembly assembly = (iPartsDataAssembly)EtkDataObjectFactory.createDataAssembly(getProject(), id);

                if (assembly.existsInDB()) {
                    getEtkDbs().startTransaction();
                    try {
                        EditDatabaseHelper.reorgSeqenceNumbers(assembly);
                        messageLog(TranslationHandler.translate("!!Modul %1 wurde reorganisiert.", id.getKVari()));
                        getEtkDbs().commit();
                    } catch (Throwable t) {
                        getEtkDbs().rollback();
                        messageLog(TranslationHandler.translate("!!Fehler bei Modul %1:", id.getKVari()) + " " + t.getMessage());
                    }
                }
            }

            if (!assemblies.isEmpty()) {
                CacheHelper.invalidateCaches();
            } else {
                messageLog(TranslationHandler.translate("!!Keine Module gefunden."));
            }

            doOnFinished();

        });

    }

    private void doOnFinished() {
        // Doku siehe https://confluence.docware.de/confluence/display/DR/Threads+und+Synchronisierung
        Session.invokeThreadSafeInSession(() -> {
            mainWindow.buttonPanel.setButtonVisible(EnumSet.of(GuiButtonOnPanel.ButtonType.START, GuiButtonOnPanel.ButtonType.CLOSE));
            if (!flagBreak) {
                messageLog(TranslationHandler.translate("!!Reorganisation abgeschlossen."));
            }
        });
    }

    private void messageLog(final String value) {
        // Doku siehe https://confluence.docware.de/confluence/display/DR/Threads+und+Synchronisierung
        Session.invokeThreadSafeInSession(() -> mainWindow.textareaMessageLog.appendLine(value));
    }

    /**
     * Aus der Modultabelle alle Module die matchen zurückliefern
     *
     * @return
     */
    private List<AssemblyId> getSelectedAssemblies() {
        List<AssemblyId> result = new ArrayList<AssemblyId>();

        List<String> lines = mainWindow.textareaInput.getTexts();

        for (String s : lines) {

            EtkRecords recs = getEtkDbs().getRecordsLike(iPartsConst.TABLE_DA_MODULE, new String[]{ iPartsConst.FIELD_DM_MODULE_NO }, new String[]{ s });

            for (EtkRecord rec : recs) {
                result.add(new AssemblyId(rec.getField(iPartsConst.FIELD_DM_MODULE_NO).getAsString(), ""));
            }
        }

        return result;
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
        private de.docware.framework.modules.gui.controls.GuiPanel panelChild;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelInput;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelInput;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextArea textareaInput;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelLog;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelLog;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane messagesScrollPane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextArea textareaMessageLog;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setWidth(600);
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
            title.setTitle("!!Reihenfolgenummern reorganisieren");
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
            panelMain.setPaddingTop(4);
            panelMain.setPaddingLeft(8);
            panelMain.setPaddingRight(8);
            panelMain.setPaddingBottom(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMain.setLayout(panelMainLayout);
            panelChild = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelChild.setName("panelChild");
            panelChild.__internal_setGenerationDpi(96);
            panelChild.registerTranslationHandler(translationHandler);
            panelChild.setScaleForResolution(true);
            panelChild.setMinimumWidth(10);
            panelChild.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelChildLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelChild.setLayout(panelChildLayout);
            panelInput = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelInput.setName("panelInput");
            panelInput.__internal_setGenerationDpi(96);
            panelInput.registerTranslationHandler(translationHandler);
            panelInput.setScaleForResolution(true);
            panelInput.setMinimumWidth(10);
            panelInput.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelInputLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelInput.setLayout(panelInputLayout);
            labelInput = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelInput.setName("labelInput");
            labelInput.__internal_setGenerationDpi(96);
            labelInput.registerTranslationHandler(translationHandler);
            labelInput.setScaleForResolution(true);
            labelInput.setMinimumWidth(10);
            labelInput.setMinimumHeight(10);
            labelInput.setPaddingBottom(8);
            labelInput.setText("!!Diese Module reorganisieren:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelInputConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelInputConstraints.setPosition("north");
            labelInput.setConstraints(labelInputConstraints);
            panelInput.addChild(labelInput);
            textareaInput = new de.docware.framework.modules.gui.controls.GuiTextArea();
            textareaInput.setName("textareaInput");
            textareaInput.__internal_setGenerationDpi(96);
            textareaInput.registerTranslationHandler(translationHandler);
            textareaInput.setScaleForResolution(true);
            textareaInput.setMinimumWidth(200);
            textareaInput.setMinimumHeight(100);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder textareaInputConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            textareaInput.setConstraints(textareaInputConstraints);
            panelInput.addChild(textareaInput);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelInputConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelInputConstraints.setPosition("north");
            panelInput.setConstraints(panelInputConstraints);
            panelChild.addChild(panelInput);
            panelLog = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelLog.setName("panelLog");
            panelLog.__internal_setGenerationDpi(96);
            panelLog.registerTranslationHandler(translationHandler);
            panelLog.setScaleForResolution(true);
            panelLog.setMinimumWidth(10);
            panelLog.setMinimumHeight(10);
            panelLog.setPaddingTop(8);
            de.docware.framework.modules.gui.layout.LayoutBorder panelLogLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelLog.setLayout(panelLogLayout);
            labelLog = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelLog.setName("labelLog");
            labelLog.__internal_setGenerationDpi(96);
            labelLog.registerTranslationHandler(translationHandler);
            labelLog.setScaleForResolution(true);
            labelLog.setMinimumWidth(10);
            labelLog.setMinimumHeight(10);
            labelLog.setPaddingBottom(8);
            labelLog.setText("!!Meldungen");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelLogConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelLogConstraints.setPosition("north");
            labelLog.setConstraints(labelLogConstraints);
            panelLog.addChild(labelLog);
            messagesScrollPane = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            messagesScrollPane.setName("messagesScrollPane");
            messagesScrollPane.__internal_setGenerationDpi(96);
            messagesScrollPane.registerTranslationHandler(translationHandler);
            messagesScrollPane.setScaleForResolution(true);
            messagesScrollPane.setMinimumWidth(10);
            messagesScrollPane.setMinimumHeight(10);
            messagesScrollPane.setBorderWidth(1);
            messagesScrollPane.setBorderColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignTextFieldBorder"));
            textareaMessageLog = new de.docware.framework.modules.gui.controls.GuiTextArea();
            textareaMessageLog.setName("textareaMessageLog");
            textareaMessageLog.__internal_setGenerationDpi(96);
            textareaMessageLog.registerTranslationHandler(translationHandler);
            textareaMessageLog.setScaleForResolution(true);
            textareaMessageLog.setMinimumWidth(200);
            textareaMessageLog.setMinimumHeight(100);
            textareaMessageLog.setEditable(false);
            textareaMessageLog.setScrollToVisible(true);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder textareaMessageLogConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            textareaMessageLog.setConstraints(textareaMessageLogConstraints);
            messagesScrollPane.addChild(textareaMessageLog);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder messagesScrollPaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            messagesScrollPane.setConstraints(messagesScrollPaneConstraints);
            panelLog.addChild(messagesScrollPane);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelLogConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelLog.setConstraints(panelLogConstraints);
            panelChild.addChild(panelLog);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelChildConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelChild.setConstraints(panelChildConstraints);
            panelMain.addChild(panelChild);
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
            buttonPanel.setDialogStyle(de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel.DialogStyle.WIZARD);
            buttonPanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonCancelActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    cancelButtonClicked(event);
                }
            });
            buttonPanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonStartActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    startButtonClicked(event);
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