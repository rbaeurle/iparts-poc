/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsMQChannelTypeNames;
import de.docware.apps.etk.plugins.customer.daimler.iparts.events.PreventTransmissionToASPLMEnabledChangeEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.events.RFTSxEnabledChangedEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.MQChannel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.MQHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.events.DIALOGDeltaEnabledChangedEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.events.RFTSxRunningChangedEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.rftsx.RFTSXImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.images.ImportDefaultImages;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.misc.DWFontStyle;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolButton;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolbarManager;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.misc.observer.ObserverCall;
import de.docware.util.misc.observer.ObserverCallback;

/**
 * Dialog für die Einstellungen der RFTS/x Importe sowie DIALOG Delta Importe und Hilfsmethoden für den Button für die
 * Einstellungen von automatischen Importen in Toolbars.
 */
public class AutoImportSettingsForm extends AbstractJavaViewerForm {

    public static final String RFTSX_IMPORT_ACTIVE_BUTTON = "buttoniPartsRFTSxImportActive";

    public static void showAutoImportSettingsForm(AbstractJavaViewerForm parentForm) {
        boolean isRFTSxImportEnabledConfig = iPartsImportPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsImportPlugin.CONFIG_RFTSX_HANDLE_IMPORT);
        boolean isDIALOGDeltaImportEnabledConfig = iPartsImportPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsImportPlugin.CONFIG_DIALOG_DELTA_HANDLE_IMPORT);
        boolean isPreventTransmissionToASPLM = MQHelper.isPreventTransmissionToASPLM();
        AutoImportSettingsForm dlg = new AutoImportSettingsForm(parentForm.getConnector(), parentForm, isRFTSxImportEnabledConfig, isDIALOGDeltaImportEnabledConfig,
                                                                isPreventTransmissionToASPLM);
        if (dlg.showModal() == ModalResult.OK) {
            if (isRFTSxImportEnabledConfig != dlg.isRFTSxImportEnabled()) {
                ApplicationEvents.fireEventInAllProjectsAndAllClusters(new RFTSxEnabledChangedEvent(dlg.isRFTSxImportEnabled()));
            }
            if (isDIALOGDeltaImportEnabledConfig != dlg.isDIALOGDeltaImportEnabled()) {
                ApplicationEvents.fireEventInAllProjectsAndAllClusters(new DIALOGDeltaEnabledChangedEvent(dlg.isDIALOGDeltaImportEnabled()));
            }

            if (isPreventTransmissionToASPLM != dlg.isPreventTransmissionToASPLMEnabled()) {
                ApplicationEvents.fireEventInAllProjectsAndAllClusters(new PreventTransmissionToASPLMEnabledChangeEvent(dlg.isPreventTransmissionToASPLMEnabled()));
            }
        }
    }

    /**
     * Erzeugt einen {@link GuiToolButton}, um die Aktivität von RFTS/x anzuzeigen und steuern zu können.
     *
     * @param connector
     * @param parentForm
     * @return
     */
    public static GuiToolButton createAutoImportSettingsToolButton(AbstractJavaViewerFormIConnector connector, final AbstractJavaViewerForm parentForm) {
        final GuiToolButton button = GuiToolbarManager.createButton(RFTSX_IMPORT_ACTIVE_BUTTON, "", "!!Einstellungen für automatische Importe...",
                                                                    ImportDefaultImages.importToolbarButton.getImage(),
                                                                    new EventListener(Event.ACTION_PERFORMED_EVENT) {
                                                                        @Override
                                                                        public void fire(Event event) {
                                                                            showAutoImportSettingsForm(parentForm);
                                                                        }
                                                                    });

        connector.getProject().addAppEventListener(new ObserverCallback(connector.getCallbackBinder(), RFTSxEnabledChangedEvent.class) {
            @Override
            public void callback(ObserverCall call) {
                AutoImportSettingsForm.updateAutoImportSettingsToolButton(button);
            }
        });

        connector.getProject().addAppEventListener(new ObserverCallback(connector.getCallbackBinder(), RFTSxRunningChangedEvent.class) {
            @Override
            public void callback(ObserverCall call) {
                updateAutoImportSettingsToolButton(button);
            }
        });

        AutoImportSettingsForm.updateAutoImportSettingsToolButton(button);
        return button;
    }

    /**
     * Aktualisiert den Zustand vom übergebenen Button für die Einstellungen für automatische Importe.
     *
     * @param button
     */
    private static void updateAutoImportSettingsToolButton(GuiToolButton button) {
        if (RFTSXImportHelper.getInstance() != null) {
            if (RFTSXImportHelper.getInstance().isRFTSxImportEnabled()) {
                button.setGlyph(ImportDefaultImages.importToolbarButton.getImage());
            } else {
                button.setGlyph(ImportDefaultImages.importStoppedToolbarButton.getImage());
            }

            if (RFTSXImportHelper.getInstance().isRFTSxRunning()) {
                button.setText("!!Aktiv");
                button.setFontStyle(DWFontStyle.BOLD);
                button.setForegroundColor(Colors.clGreen.getColor());
            } else {
                button.setText("");
            }
        }
    }

    /**
     * Erzeugt eine Instanz von AutoImportSettingsForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public AutoImportSettingsForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                  boolean isRFTSxImportEnabled, boolean isDIALOGDeltaImportEnabled, boolean isPreventTransmissionToASPLM) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        postCreateGui(isRFTSxImportEnabled, isDIALOGDeltaImportEnabled, isPreventTransmissionToASPLM);
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     *
     * @param isRFTSxImportEnabled
     */
    private void postCreateGui(boolean isRFTSxImportEnabled, boolean isDIALOGDeltaImportEnabled, boolean isPreventTransmissionToASPLM) {
        mainWindow.checkboxRFTSxImporter.setSelected(isRFTSxImportEnabled);
        if (RFTSXImportHelper.isRFTSxDirectoryMonitorConfigured()) {
            mainWindow.buttonCancelRFTSxImport.setVisible(isRFTSxImportEnabled);
        } else {
            mainWindow.checkboxRFTSxImporter.setEnabled(false);
            mainWindow.checkboxRFTSxImporter.setTooltip("!!RFTS/x ist nicht korrekt konfiguriert.");
            mainWindow.buttonCancelRFTSxImport.setVisible(false);
        }

        mainWindow.checkboxDIALOGDeltaImporter.setSelected(isDIALOGDeltaImportEnabled);
        MQChannel dialogDeltaChannel = MQHelper.getInstance().getChannel(iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_DELTA_IMPORT);
        if ((dialogDeltaChannel == null) || !dialogDeltaChannel.isReadyForCommunication()) {
            mainWindow.checkboxDIALOGDeltaImporter.setEnabled(false);
            mainWindow.checkboxDIALOGDeltaImporter.setTooltip(TranslationHandler.translate("!!Der MQ Kanal \"%1\" ist nicht initialisiert.",
                                                                                           iPartsMQChannelTypeNames.DIALOG_DELTA_IMPORT.getTypeName()));
        }
        mainWindow.checkboxPreventTransmissionToASPLM.setSelected(isPreventTransmissionToASPLM);

        boolean isCancelEnabled = false;
        if (isRFTSxImportEnabled && (RFTSXImportHelper.getInstance() != null)) {
            isCancelEnabled = RFTSXImportHelper.getInstance().isRFTSxRunning();
        }
        mainWindow.buttonCancelRFTSxImport.setEnabled(isCancelEnabled);
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

    public boolean isRFTSxImportEnabled() {
        return mainWindow.checkboxRFTSxImporter.isSelected();
    }

    public boolean isDIALOGDeltaImportEnabled() {
        return mainWindow.checkboxDIALOGDeltaImporter.isSelected();
    }

    public boolean isPreventTransmissionToASPLMEnabled() {
        return mainWindow.checkboxPreventTransmissionToASPLM.isSelected();
    }

    private void rftsxCancelButtonClicked(Event event) {
        if (RFTSXImportHelper.getInstance() != null) {
            RFTSXImportHelper.getInstance().cancelRFTSxImport("!!Abbruch durch Benutzer");
            mainWindow.buttonCancelRFTSxImport.setEnabled(false);
        }
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
        private de.docware.framework.modules.gui.controls.GuiCheckbox checkboxRFTSxImporter;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiCheckbox checkboxDIALOGDeltaImporter;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSeparator separatorPreventTransmissionToASPLM;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiCheckbox checkboxPreventTransmissionToASPLM;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton buttonCancelRFTSxImport;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setWidth(400);
            this.setHeight(300);
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
            title.setTitle("!!Einstellungen für automatische Importe");
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
            de.docware.framework.modules.gui.layout.LayoutGridBag panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelMain.setLayout(panelMainLayout);
            checkboxRFTSxImporter = new de.docware.framework.modules.gui.controls.GuiCheckbox();
            checkboxRFTSxImporter.setName("checkboxRFTSxImporter");
            checkboxRFTSxImporter.__internal_setGenerationDpi(96);
            checkboxRFTSxImporter.registerTranslationHandler(translationHandler);
            checkboxRFTSxImporter.setScaleForResolution(true);
            checkboxRFTSxImporter.setMinimumWidth(10);
            checkboxRFTSxImporter.setMinimumHeight(10);
            checkboxRFTSxImporter.setPaddingTop(2);
            checkboxRFTSxImporter.setPaddingBottom(2);
            checkboxRFTSxImporter.setText("!!RFTS/x Importe durchführen");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag checkboxRFTSxImporterConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "w", "n", 0, 0, 0, 0);
            checkboxRFTSxImporter.setConstraints(checkboxRFTSxImporterConstraints);
            panelMain.addChild(checkboxRFTSxImporter);
            checkboxDIALOGDeltaImporter = new de.docware.framework.modules.gui.controls.GuiCheckbox();
            checkboxDIALOGDeltaImporter.setName("checkboxDIALOGDeltaImporter");
            checkboxDIALOGDeltaImporter.__internal_setGenerationDpi(96);
            checkboxDIALOGDeltaImporter.registerTranslationHandler(translationHandler);
            checkboxDIALOGDeltaImporter.setScaleForResolution(true);
            checkboxDIALOGDeltaImporter.setMinimumWidth(10);
            checkboxDIALOGDeltaImporter.setMinimumHeight(10);
            checkboxDIALOGDeltaImporter.setPaddingTop(2);
            checkboxDIALOGDeltaImporter.setPaddingBottom(2);
            checkboxDIALOGDeltaImporter.setText("!!DIALOG Delta Importe durchführen");
            checkboxDIALOGDeltaImporter.setSelected(true);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag checkboxDIALOGDeltaImporterConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 10, 1, 1, 0.0, 0.0, "w", "n", 0, 0, 4, 0);
            checkboxDIALOGDeltaImporter.setConstraints(checkboxDIALOGDeltaImporterConstraints);
            panelMain.addChild(checkboxDIALOGDeltaImporter);
            separatorPreventTransmissionToASPLM = new de.docware.framework.modules.gui.controls.GuiSeparator();
            separatorPreventTransmissionToASPLM.setName("separatorPreventTransmissionToASPLM");
            separatorPreventTransmissionToASPLM.__internal_setGenerationDpi(96);
            separatorPreventTransmissionToASPLM.registerTranslationHandler(translationHandler);
            separatorPreventTransmissionToASPLM.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag separatorPreventTransmissionToASPLMConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 14, 1, 1, 0.0, 0.0, "c", "h", 0, 0, 0, 0);
            separatorPreventTransmissionToASPLM.setConstraints(separatorPreventTransmissionToASPLMConstraints);
            panelMain.addChild(separatorPreventTransmissionToASPLM);
            checkboxPreventTransmissionToASPLM = new de.docware.framework.modules.gui.controls.GuiCheckbox();
            checkboxPreventTransmissionToASPLM.setName("checkboxPreventTransmissionToASPLM");
            checkboxPreventTransmissionToASPLM.__internal_setGenerationDpi(96);
            checkboxPreventTransmissionToASPLM.registerTranslationHandler(translationHandler);
            checkboxPreventTransmissionToASPLM.setScaleForResolution(true);
            checkboxPreventTransmissionToASPLM.setMinimumWidth(10);
            checkboxPreventTransmissionToASPLM.setMinimumHeight(10);
            checkboxPreventTransmissionToASPLM.setText("!!Bild-/Änderungsaufträge an ASPLM verhindern");
            checkboxPreventTransmissionToASPLM.setSelected(true);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag checkboxPreventTransmissionToASPLMConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 15, 1, 1, 0.0, 0.0, "w", "n", 4, 0, 0, 0);
            checkboxPreventTransmissionToASPLM.setConstraints(checkboxPreventTransmissionToASPLMConstraints);
            panelMain.addChild(checkboxPreventTransmissionToASPLM);
            buttonCancelRFTSxImport = new de.docware.framework.modules.gui.controls.GuiButton();
            buttonCancelRFTSxImport.setName("buttonCancelRFTSxImport");
            buttonCancelRFTSxImport.__internal_setGenerationDpi(96);
            buttonCancelRFTSxImport.registerTranslationHandler(translationHandler);
            buttonCancelRFTSxImport.setScaleForResolution(true);
            buttonCancelRFTSxImport.setMinimumWidth(100);
            buttonCancelRFTSxImport.setMinimumHeight(10);
            buttonCancelRFTSxImport.setMnemonicEnabled(true);
            buttonCancelRFTSxImport.setText("!!RFTS/x Import abbrechen");
            buttonCancelRFTSxImport.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    rftsxCancelButtonClicked(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag buttonCancelRFTSxImportConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 20, 1, 1, 100.0, 100.0, "sw", "n", 0, 0, 0, 0);
            buttonCancelRFTSxImport.setConstraints(buttonCancelRFTSxImportConstraints);
            panelMain.addChild(buttonCancelRFTSxImport);
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