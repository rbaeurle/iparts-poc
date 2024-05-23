/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;


import de.docware.apps.etk.base.filter.forms.common.UserDataSessionManager;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.forms.toolbar.ToolbarButtonAlias;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.events.FilterChangedEvent;
import de.docware.apps.etk.base.project.events.GotoPartWithPartialPathEvent;
import de.docware.apps.etk.base.project.mechanic.NavigationPath;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.search.model.ModuleSearchCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsNodeType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.SupportURLInterpreter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.DatacardIdentOrderElem;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.DatacardIdentOrderTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgSaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuForProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.*;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiTextField;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolButton;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.constraints.AbstractConstraints;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.resources.FrameworkImage;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.misc.validator.GuiControlValidator;
import de.docware.framework.modules.gui.misc.validator.ValidationState;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dialog zum Dekodieren und Anzeigen einer Support URL aus einem XSF Ticket
 */
public class SupportURLInputDialog extends AbstractJavaViewerForm {

    private iPartsGuiFinModelTextField finModelTextField;
    private iPartsGuiTextFieldBackgroundToggle fzgModelTextField;
    private iPartsGuiTextFieldBackgroundToggle fzgProductTextfield;
    private iPartsGuiTextFieldBackgroundToggle aggModelTextfield;
    private iPartsGuiTextFieldBackgroundToggle aggProductTextfield;
    private iPartsGuiTextFieldBackgroundToggle kgTextField;
    private iPartsGuiTextFieldBackgroundToggle tuSaaTextField;
    private GuiContextMenu showContextMenu;
    private GuiToolButton prevButton;
    private GuiToolButton nextButton;

    private final boolean withSyntaxCheck = true;
    private Set<String> validProductNumbers = null;
    private SupportURLInterpreter interpreter;

    /**
     * Erzeugt eine Instanz von MQTestForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public SupportURLInputDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        postCreateGui();

        // den letzten verwendeten Pfad wiederherstellen
        String lastSupportURLFromSession = UserDataSessionManager.getLastSupportURLFromSession();
        if (StrUtils.isValid(lastSupportURLFromSession)) {
            mainWindow.inputTextArea.setText(lastSupportURLFromSession);
        }
        enableMoveButtons();
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow;
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
//        Dimension screenSize = FrameworkUtils.getScreenSize();
//        // Größe auf mindestens 80% des Bildschirms/Browsers festlegen
//        mainWindow.setMinimumHeight((int)(0.8 * screenSize.getHeight()));
//        mainWindow.setMinimumWidth((int)(0.8 * screenSize.getWidth()));

        if (withSyntaxCheck) {
            GuiControlValidator modelValidator = new GuiControlValidator() {
                @Override
                public ValidationState validate(AbstractGuiControl control) {
                    boolean isValid = true;
                    if (control instanceof GuiTextField) {
                        String text = control.getText();
                        // Eingabe ist Valid, wenn leer oder ModelNo gültig
                        isValid = text.isEmpty() || iPartsModel.isModelNumberValid(text, true);
                    }
                    return new ValidationState(isValid);
                }
            };
            GuiControlValidator productValidator = new GuiControlValidator() {
                @Override
                public ValidationState validate(AbstractGuiControl control) {
                    boolean isValid = true;
                    if (control instanceof GuiTextField) {
                        String text = control.getText();
                        // Eingabe ist Valid, wenn leer oder ProductNo gültig
                        isValid = text.isEmpty() || getValidProductNumbers().contains(text);
                    }
                    return new ValidationState(isValid);
                }
            };

            finModelTextField = new iPartsGuiFinModelTextField(true);
            replaceTextField(mainWindow.finTextfield, finModelTextField);
            mainWindow.finPanel.addChild(finModelTextField);
            showContextMenu = new GuiContextMenu();
            showContextMenu.setName("aggIdentContextMenu");
            showContextMenu.__internal_setGenerationDpi(96);
            showContextMenu.registerTranslationHandler(getUITranslationHandler());
            showContextMenu.setScaleForResolution(true);
            showContextMenu.setMinimumWidth(10);
            showContextMenu.setMinimumHeight(10);
            GuiMenuItem showMenuItem = new GuiMenuItem();
            showMenuItem.setName("showMenuItem");
            showMenuItem.__internal_setGenerationDpi(96);
            showMenuItem.registerTranslationHandler(getUITranslationHandler());
            showMenuItem.setScaleForResolution(true);
            showMenuItem.setMinimumWidth(10);
            showMenuItem.setMinimumHeight(10);
            showMenuItem.setMnemonicEnabled(true);
            showMenuItem.setText("!!Ident anzeigen");
            //copyMenuItem.setIcon(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignToClipboard"));
            showMenuItem.addEventListener(new EventListener("menuItemEvent") {
                @Override
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    doShowIdent(event);
                }
            });
            showContextMenu.addChild(showMenuItem);


            fzgModelTextField = replaceTextFieldWithCreate(mainWindow.fzgBMTextfield);
            mainWindow.fzgPanel.addChild(fzgModelTextField);
            fzgModelTextField.setValidator(modelValidator);

            fzgProductTextfield = replaceTextFieldWithCreate(mainWindow.fzgProductTextfield);
            mainWindow.fzgPanel.addChild(fzgProductTextfield);
            fzgProductTextfield.setValidator(productValidator);

            aggModelTextfield = replaceTextFieldWithCreate(mainWindow.aggBMTextfield);
            mainWindow.aggPanel.addChild(aggModelTextfield);
            aggModelTextfield.setValidator(modelValidator);

            aggProductTextfield = replaceTextFieldWithCreate(mainWindow.aggProductTextfield);
            mainWindow.aggPanel.addChild(aggProductTextfield);
            aggProductTextfield.setValidator(productValidator);

            kgTextField = replaceTextFieldWithCreate(mainWindow.kgTextfield);
            mainWindow.navPanel.addChild(kgTextField);
            kgTextField.setValidator(new GuiControlValidator() {
                @Override
                public ValidationState validate(AbstractGuiControl control) {
                    boolean isValid = true;
                    if (control instanceof GuiTextField) {
                        String text = control.getText();
                        // Eingabe ist Valid, wenn leer oder KG gültig
                        isValid = SupportURLInterpreter.isKgValid(text);
                    }
                    return new ValidationState(isValid);
                }
            });

            tuSaaTextField = replaceTextFieldWithCreate(mainWindow.tuTextfield);
            mainWindow.navPanel.addChild(tuSaaTextField);
            tuSaaTextField.setValidator(new GuiControlValidator() {
                @Override
                public ValidationState validate(AbstractGuiControl control) {
                    boolean isValid = true;
                    if (control instanceof GuiTextField) {
                        String text = control.getText();
                        // Eingabe ist Valid, wenn leer oder TU/SAANo gültig
                        isValid = SupportURLInterpreter.isTuSaaValid(text);
                    }
                    return new ValidationState(isValid);
                }
            });
            ToolbarButtonMenuHelper toolbarHelper = new ToolbarButtonMenuHelper(getConnector(), mainWindow.toolbar);
            prevButton = toolbarHelper.addToolbarButton(ToolbarButtonAlias.DOUBLE_LIST_SELECTION_UP, "!!Vorherige URL", new EventListener(de.docware.framework.modules.gui.event.Event.ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    String lastSupportURLFromSession = UserDataSessionManager.getPreviousSupportURLFromSession();
                    if (StrUtils.isValid(lastSupportURLFromSession)) {
                        mainWindow.inputTextArea.setText(lastSupportURLFromSession);
                    }
                    enableMoveButtons();
                }
            });
            nextButton = toolbarHelper.addToolbarButton(ToolbarButtonAlias.DOUBLE_LIST_SELECTION_DOWN, "!!Nächste URL", new EventListener(de.docware.framework.modules.gui.event.Event.ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    String lastSupportURLFromSession = UserDataSessionManager.getNextSupportURLFromSession();
                    if (StrUtils.isValid(lastSupportURLFromSession)) {
                        mainWindow.inputTextArea.setText(lastSupportURLFromSession);
                    }
                    enableMoveButtons();
                }
            });
        } else {
            mainWindow.toolbar.setVisible(false);
        }

        final GuiButtonOnPanel okButton = mainWindow.buttonPanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK);
        okButton.setText(TranslationHandler.translate("!!Navigiere zum ausgewählten Pfad"));
        GuiButtonOnPanel cancelButton = mainWindow.buttonPanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.CANCEL);
        if (cancelButton != null) {
            cancelButton.setText("!!Schließen");
        }
        okButton.setEnabled(false);

        interpreter = new SupportURLInterpreter();
        mainWindow.pack();
        mainWindow.inputTextArea.requestFocus();
        mainWindow.inputTextArea.selectAll();

        mainWindow.inputTextArea.addEventListener(new EventListener(Event.KEY_PRESSED_EVENT) {
            @Override
            public void fire(Event event) {
                // Auf Enter und ESC reagieren im Eingabetextfeld
                int keyCode = event.getIntParameter(Event.EVENT_PARAMETER_KEY_CODE);
                if (keyCode == KeyEvent.VK_ESCAPE) {
                    close();
                }
            }
        });

        mainWindow.inputTextArea.addEventListener(new EventListener(Event.KEY_RELEASED_EVENT) {
            @Override
            public void fire(Event event) {
                // Newlines im Eingabetext entfernen
                int keyCode = event.getIntParameter(Event.EVENT_PARAMETER_KEY_CODE);
                if (keyCode == KeyEvent.VK_ENTER) {
                    mainWindow.inputTextArea.setText(getInputText());
                    if (okButton.isEnabled()) {
                        okButton.doClick();
                    }
                }
            }
        });
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }

    private void closeWindow(Event event) {
        close();
    }

    public void show() {
        mainWindow.showModal();
    }

    private class XSFMessageLogForm extends EtkMessageLogForm {

        protected boolean hasErrors;

        public XSFMessageLogForm(String windowTitle, String title, FrameworkImage titleImage) {
            super(windowTitle, title, titleImage);
            hasErrors = false;
        }

        public XSFMessageLogForm(String windowTitle, String title, FrameworkImage titleImage, boolean marqueeOnly) {
            super(windowTitle, title, titleImage, marqueeOnly);
            hasErrors = false;
        }

        @Override
        public ModalResult showModal(final FrameworkRunnable runnable) {
            if (runnable != null) {
                getButtonPanel().setButtonVisible(GuiButtonOnPanel.ButtonType.CANCEL, true);
                getButtonPanel().setButtonVisible(GuiButtonOnPanel.ButtonType.CLOSE, false);

                final Session session = Session.get();
                workerThread = session.startChildThread(new FrameworkRunnable() {
                    @Override
                    public void run(FrameworkThread thread) {
                        runnable.run(thread);
                        session.invokeThreadSafe(new Runnable() {
                            @Override
                            public void run() {
                                if (!hasErrors) {
                                    getButtonPanel().setButtonVisible(GuiButtonOnPanel.ButtonType.CLOSE, true);
                                    getButtonPanel().setButtonVisible(GuiButtonOnPanel.ButtonType.CANCEL, false);
                                } else {
                                    getButtonPanel().setEnabled(true);
                                    getButtonPanel().setButtonVisible(GuiButtonOnPanel.ButtonType.CLOSE, false);
                                    getButtonPanel().setButtonVisible(GuiButtonOnPanel.ButtonType.CANCEL, true);
                                    getButtonPanel().getButtonOnPanel(GuiButtonOnPanel.ButtonType.CANCEL).setText(getButtonPanel().getButtonOnPanel(GuiButtonOnPanel.ButtonType.CLOSE).getText());
                                }
                            }
                        });
                        workerThread = null;
                    }
                });
            }

            if (workerThread != null) {
                ModalResult modalResult = getGui().showModal();
                return modalResult;
            } else {
                return ModalResult.OK;
            }
        }
    }

    private void enableMoveButtons() {
        if (withSyntaxCheck) {
            prevButton.setEnabled(UserDataSessionManager.isPreviousURLPossible());
            nextButton.setEnabled(UserDataSessionManager.isNextURLPossible());
        }
    }

    private void onButtonOk(Event event) {

        // Eingabe in der Session merken damit sie beim nächsten Start wiederhergestellt werden kann
        String inputText = getInputText();
        if (StrUtils.isValid(inputText)) {
            UserDataSessionManager.saveLastSupportURLInSession(inputText);
            enableMoveButtons();
        }

        final XSFMessageLogForm progressForm = new XSFMessageLogForm("!!Support URL laden", "!!Fortschritt", null);
        progressForm.disableButtons(false);
        progressForm.setMessagesTitle("");
        progressForm.getGui().setSize(600, 250);
        progressForm.showModal(new FrameworkRunnable() {
            @Override
            public void run(FrameworkThread thread) {
                EtkProject project = getConnector().getProject();

                boolean isFilterActive = false;
                AbstractDataCard dataCardForFilter = null;
                // wenn eine FIN oder VIN angegeben ist, diese verwenden um eine Datenkarte zu laden
                if (interpreter.isFinOrVinIdValid()) {
                    VehicleDataCard vehicleDataCard = loadVehicleDatacard(project, progressForm);
                    // Wenn eine echte Datenkarte vorhanden ist, dann muss hier mit einem Klon gearbeitet werden. Sonst
                    // wird bei "adoptDCToModelDC()" die Original-Datenkarte verändert. Da diese aber im Cache gehalten
                    // wird, darf sie nicht von außen verändert werden.
                    if (vehicleDataCard != null) {
                        dataCardForFilter = vehicleDataCard.cloneMe();
                    }
                }
                // Lade die Baumuster-Datenkarte. Entweder als Ergänzung zur echten Datenkarte (geladen via VIS) oder als
                // Fallback, wenn keine gültige FIN oder VIN
                AbstractDataCard modelDataCard = loadModelDatacard(project, dataCardForFilter, progressForm);

                // Konnte keine "echte" Datenkarte geladen werden, setze die Datenkarte, die über das Baumuster befüllt wurde
                if ((dataCardForFilter == null) && (modelDataCard != null)) {
                    dataCardForFilter = modelDataCard;
                }

                // Ist die Variable immer noch "null", dann konnte keine echte und keine Baumusterdatenkarte geladen werden
                if (dataCardForFilter == null) {
                    showErrorMsg(progressForm, "!!Es konnte keine Datenkarte für die Filterung geladen werden!");
                } else {
                    if (!dataCardForFilter.isDataCardLoaded() && interpreter.isFinOrVinIdValid()) {
                        showErrorMsg(progressForm, "!!Es konnte keine Datenkarte zur %1 geladen werden; Fallback auf Baumuster \"%2\"",
                                     interpreter.isFinIdValid() ? "FIN" : "VIN", dataCardForFilter.getModelNo());
                    } else {
                        // Daten aus BM-Datenkarte in Fahrzeug-Datenkarte übernehmen
                        if ((dataCardForFilter instanceof VehicleDataCard) && (modelDataCard instanceof VehicleDataCard)) {
                            iPartsFilterHelper.adoptDCToModelDC(getProject(), (VehicleDataCard)modelDataCard, (VehicleDataCard)dataCardForFilter);
                        } else if ((dataCardForFilter instanceof AggregateDataCard) && (modelDataCard instanceof AggregateDataCard)) {
                            iPartsFilterHelper.adoptDCToModelDC((AggregateDataCard)modelDataCard, (AggregateDataCard)dataCardForFilter);
                        } else {
                            showErrorMsg(progressForm, "!!Geladene und Baumuster-Datenkarte passen nicht zusammen!");
                        }
                    }
                    // bevor der Filter aktiviert wird auf den Katalog Root springen um Fehlermeldungen zu vermeiden
                    // dass die aktuelle Baugruppe nicht mehr gültig ist
                    AssemblyId catalogRootId = project.getRootNodes().getRootAssemblyId();
                    jumpToAssemblyId(catalogRootId);

                    showMsg(progressForm, "!!Aktiviere Filter...");

                    // Filter setzen und aktivieren
                    iPartsFilter filter = iPartsFilter.get();
                    filter.setCurrentDataCard(dataCardForFilter, getProject());
                    filter.setAllRetailFilterActiveForDataCard(project, dataCardForFilter, true);

                    getConnector().getProject().fireProjectEvent(new FilterChangedEvent());

                    iPartsPlugin.updateFilterButton(getConnector(), null);
                    isFilterActive = true;
                }

                // Produkt für Sprung bestimmen
                // wenn ein Aggregate Produkt angegeben ist, dieses verwenden, sonst das Fahrzeugprodukt, sonst APS Produkt von Datenkarte
                List<String> productNumbers = getProductNumbers(project, dataCardForFilter, progressForm);
                if (productNumbers.isEmpty()) {
                    showErrorMsg(progressForm, "!!Es konnte kein Produkt bestimmt werden");
                } else {
                    if (productNumbers.size() > 1) {
                        showMsg(progressForm, "!!Produkt kann nicht eindeutig bestimmt werden (%1)", StrUtils.stringListToString(productNumbers, ", "));
                    }
                    List<iPartsProduct> products = checkProductNumbers(project, productNumbers, progressForm);
                    if (!products.isEmpty()) {
                        for (iPartsProduct product : products) {
                            iPartsVirtualNode productNode = new iPartsVirtualNode(iPartsNodeType.getProductKgTuType(product, project),
                                                                                  product.getAsId());
                            String kg = interpreter.getKg();
                            String tuOrSa = interpreter.getTu();
                            VirtualNodeWithMessageText kgTuSaaNodeWithMessage = null;
                            if (StrUtils.isValid(kg, tuOrSa)) {
                                kgTuSaaNodeWithMessage = getKgTuSaNode(project, product.getAsId(), kg, tuOrSa, progressForm);
                            }
                            String virtualIdString;
                            String jumpMessage;
                            if ((kgTuSaaNodeWithMessage == null) || (kgTuSaaNodeWithMessage.virtualNode == null)) {
                                virtualIdString = iPartsVirtualNode.getVirtualIdString(productNode);
                                jumpMessage = TranslationHandler.translate("!!Springe zu Produkt: %1...", product.getAsId().getProductNumber());
                            } else {
                                virtualIdString = iPartsVirtualNode.getVirtualIdString(productNode, kgTuSaaNodeWithMessage.virtualNode);
                                jumpMessage = TranslationHandler.translate("!!Springe zu Produkt: %1, KG: %2, %3: %4...",
                                                                           product.getAsId().getProductNumber(), kg,
                                                                           SupportURLInterpreter.isTuValid(tuOrSa) ? "TU" : "SA", tuOrSa);
                            }
                            AssemblyId virtualAssemblyId = new AssemblyId(virtualIdString, "");

                            // Nachsehen, ob das Sprung Ziel aktuell sichtbar ist
                            if (isAssemblyVisible(project, virtualAssemblyId)) {
                                if ((kgTuSaaNodeWithMessage != null) && (kgTuSaaNodeWithMessage.message != null)) {
                                    showMsg(progressForm, kgTuSaaNodeWithMessage.message);
                                }
                                showMsg(progressForm, jumpMessage);
                                jumpToAssemblyId(virtualAssemblyId);
                                if (!progressForm.hasErrors) {
                                    // Alles OK, der Dialog kann geschlossen werden
                                    progressForm.getMessageLog().hideProgress();

                                    Session.invokeThreadSafeInSession(() -> progressForm.closeWindow(ModalResult.OK));
                                } else {
                                    showMsg(progressForm, "!!Sprung ausgeführt");
                                }
                                break;
                            } else {
                                if (isFilterActive) {
                                    showErrorMsg(progressForm, "!!Pfad zu Produkt: %1, KG: %2, %3: %4 ist mit der aktuellen Filterung nicht erreichbar",
                                                 product.getAsId().getProductNumber(), kg,
                                                 SupportURLInterpreter.isTuValid(tuOrSa) ? "TU" : "SA", tuOrSa);
                                } else {
                                    showErrorMsg(progressForm, "!!Pfad zu Produkt: %1, KG: %2, %3: %4 existiert nicht",
                                                 product.getAsId().getProductNumber(), kg,
                                                 SupportURLInterpreter.isTuValid(tuOrSa) ? "TU" : "SA", tuOrSa);
                                }
                            }
                        }
                    } else {
                        showErrorMsg(progressForm, "!!Es konnte kein Produkt bestimmt werden");
                    }
                }
            }
        });

        ModalResult modalResult = progressForm.getModalResult();
        if (modalResult == ModalResult.OK) {
            closeWindow(event);
        } else {
            mainWindow.inputTextArea.requestFocus();
        }
    }

    public void showMsg(XSFMessageLogForm progressForm, String key, String... placeHolderTexts) {
        progressForm.getMessageLog().fireMessage(TranslationHandler.translate(key, placeHolderTexts));
    }

    public void showErrorMsg(XSFMessageLogForm progressForm, String key, String... placeHolderTexts) {
        showMsg(progressForm, key, placeHolderTexts);
        progressForm.hasErrors = true;
    }

    private boolean isAssemblyVisible(EtkProject project, AssemblyId assemblyId) {
        AssemblyId catalogRootId = project.getRootNodes().getRootAssemblyId();
        ModuleSearchCache moduleCache = ModuleSearchCache.getModuleSearchCacheFromPool(project, catalogRootId, false);
        try {
            return moduleCache.isAssemblyInChilds(assemblyId, true);
        } finally {
            ModuleSearchCache.putModuleSearchCacheToPool(moduleCache);
        }
    }

    private VirtualNodeWithMessageText getKgTuSaNode(EtkProject project, iPartsProductId productId, String kg, String tuOrSa, XSFMessageLogForm progressForm) {
        String productNo = productId.getProductNumber();
        KgTuForProduct kgTuForProduct = KgTuForProduct.getInstance(project, productId);
        VirtualNodeWithMessageText returnValue = new VirtualNodeWithMessageText();
        if (kgTuForProduct == null) {
            progressForm.getMessageLog().fireMessage(TranslationHandler.translate("!!KG/TU Struktur für Produkt %1 unbekannt. Es wird nur zum Produkt gesprungen...", productNo));
        } else {
            KgTuNode kgNode = kgTuForProduct.getKgNode(kg);
            if (kgNode == null) {
                returnValue.message = TranslationHandler.translate("!!KG %1 für Produkt %2 existiert nicht. Es wird nur zum Produkt gesprungen...", kg, productNo);
            } else {
                if (SupportURLInterpreter.isTuValid(tuOrSa)) {
                    KgTuNode tuNode = kgTuForProduct.getTuNode(kg, tuOrSa);
                    if (tuNode == null) {
                        returnValue.message = TranslationHandler.translate("!!TU %1 für KG %2 und Produkt %3 existiert nicht. Es wird nur zur KG gesprungen...", tuOrSa, kg, productNo);
                    }
                    returnValue.virtualNode = new iPartsVirtualNode(iPartsNodeType.KGTU, new KgTuId(kg, tuOrSa));
                } else {
                    iPartsNumberHelper h = new iPartsNumberHelper();
                    try {
                        String sa = h.unformatSaForDB(tuOrSa);
                        returnValue.virtualNode = new iPartsVirtualNode(iPartsNodeType.KGSA, new KgSaId(kg, sa));
                    } catch (RuntimeException e) {
                        returnValue.message = TranslationHandler.translate("!!%1 ist weder eine gültige TU noch eine gültige SA", tuOrSa);
                    }
                }
            }
        }
        return returnValue;
    }

    private List<String> getProductNumbers(EtkProject project, AbstractDataCard dataCardForFilter, XSFMessageLogForm progressForm) {
        List<String> productNumbers = new DwList<>();
        String productNo = interpreter.getAggProduct();
        if (!productNo.isEmpty()) {
            productNumbers.add(productNo);
            return productNumbers;
        }
        productNo = interpreter.getProduct();
        if (!productNo.isEmpty()) {
            productNumbers.add(productNo);
            return productNumbers;
        }
        // wenn kein Produkt angegeben ist, prüfen ob eines auf der Datenkarte steht, falls geladen
        iPartsModelId modelForAPS;
        if (!interpreter.getAggBM().isEmpty()) {
            modelForAPS = new iPartsModelId(interpreter.getAggBM());
        } else {
            modelForAPS = interpreter.getModelId(project);
        }
        if (!modelForAPS.isValidId() && (dataCardForFilter != null)) {
            modelForAPS = new iPartsModelId(dataCardForFilter.getModelNo());
        }
        if (modelForAPS.isValidId()) {
            // Alle Produkte zum Baumuster bestimmen (auch nicht sichtbare Baumuster berücksichtigen).
            List<iPartsProduct> products = iPartsProductHelper.getProductsForModel(project, modelForAPS, null,
                                                                                   iPartsProductHelper.PRODUCTS_FOR_SERIES_MODE.FILTER_ALL, false);

            // Auto-Product-Select
            if ((dataCardForFilter != null) && (dataCardForFilter instanceof VehicleDataCard)) {
                VehicleDataCard vehicleDataCard = (VehicleDataCard)dataCardForFilter;
                products = iPartsFilterHelper.getAutoSelectProductsForFIN(project, products, vehicleDataCard.getFinId(),
                                                                          new iPartsModelId(vehicleDataCard.getModelNo()),
                                                                          vehicleDataCard.getCodes().getAllCheckedValues());
            }
            if (!products.isEmpty()) {
                for (iPartsProduct product : products) {
                    // Beim Sprung von einem XSF-Ticket alle Produkte berücksichtigen
                    productNumbers.add(product.getAsId().getProductNumber());
                }
            }
        }
        return productNumbers;
    }

    private List<iPartsProduct> checkProductNumbers(EtkProject project, List<String> productNumbers, XSFMessageLogForm progressForm) {
        List<iPartsProduct> result = new DwList<>();
        for (String productNo : productNumbers) {
            if (productNo.isEmpty()) {
                showErrorMsg(progressForm, "Produkt darf nicht leer sein");
            }
            iPartsProductId productId = new iPartsProductId(productNo);
            iPartsProduct product = iPartsProduct.getInstance(project, productId);
            if (product == null) {
                showErrorMsg(progressForm, "!!Produkt %1 existiert nicht", productNo);
            } else {
                result.add(product);
            }
        }
        return result;
    }

    private void jumpToAssemblyId(final AssemblyId assemblyId) {
        Session.invokeThreadSafeInSession(() -> {
            NavigationPath navPath = new NavigationPath();
            navPath.addAssembly(assemblyId);
            getProject().fireProjectEvent(new GotoPartWithPartialPathEvent(navPath, assemblyId,
                                                                           "", false,
                                                                           false, SupportURLInputDialog.this));
        });
    }

    private AbstractDataCard loadModelDatacard(EtkProject project, AbstractDataCard currentDatacardForFilter, XSFMessageLogForm progressForm) {
        iPartsModelId modelId = interpreter.getModelId(project);
        // Falls Interpreter keine Baumusternummer auslesen konnte (bei VIN) und eine echte Datenkarte von VIS geliefert
        // wurde, dann bekommt man die Baumusternummer von der echten Datenkarte.
        boolean datacardHasModel = (currentDatacardForFilter != null) && StrUtils.isValid(currentDatacardForFilter.getModelNo());
        if (!modelId.isValidId() && datacardHasModel) {
            modelId = new iPartsModelId(currentDatacardForFilter.getModelNo());
        }
        if (modelId.isValidId()) {
            String modelNumber = modelId.getModelNumber();
            if (!datacardHasModel) {
                showMsg(progressForm, "!!Lade Datenkarte für Baumuster: %1...", modelNumber);
            }
            AbstractDataCard dataCardForFilter = new VehicleDataCard(true);
            dataCardForFilter.fillByModel(project, modelNumber);
            return dataCardForFilter;
        } else {
            String aggModelNo = interpreter.getAggBM();
            if (StrUtils.isValid(aggModelNo)) {
                iPartsModelId aggModelId = new iPartsModelId(aggModelNo);
                if (aggModelId.isModelNumberValid(false)) {
                    String modelNumber = aggModelId.getModelNumber();
                    if (!modelNumber.startsWith(iPartsConst.MODEL_NUMBER_PREFIX_CAR)) {
                        modelNumber = iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE + modelNumber;
                    }
                    if (!datacardHasModel) {
                        showMsg(progressForm, "!!Lade Datenkarte für Aggregate Baumuster: %1...", modelNumber);
                    }
                    AbstractDataCard dataCardForFilter = new AggregateDataCard(true);
                    dataCardForFilter.fillByModel(project, modelNumber);
                    return dataCardForFilter;
                }
            }
        }
        return null;
    }

    private VehicleDataCard loadVehicleDatacard(EtkProject project, XSFMessageLogForm progressForm) {
        String finOrVin;
        String finOrVinInfoText;
        boolean tryToLoadBMDataCard = false;
        if (interpreter.getFinId().isValidId()) {
            finOrVin = interpreter.getFinId().getFIN();
            finOrVinInfoText = "FIN";
        } else {
            finOrVin = interpreter.getVinId().getVIN();
            finOrVinInfoText = "VIN";
        }
        if (!finOrVin.isEmpty()) {
            showMsg(progressForm, "!!Lade Datenkarte für %1: %2...", finOrVinInfoText, finOrVin);
            iPartsDataCardRetrievalHelper.LoadDataCardCallback loadDataCardCallback = new iPartsDataCardRetrievalHelper.LoadDataCardCallback() {
                @Override
                public void loadDataCard(Runnable loadDataCardRunnable) {
                    loadDataCardRunnable.run();
                }
            };

            try {
                return VehicleDataCard.getVehicleDataCard(finOrVin, false, true, true, loadDataCardCallback, project, true);

            } catch (DataCardRetrievalException e) {
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DATACARD_SERVICE, LogType.ERROR, e);
                showErrorMsg(progressForm, e.getMessage());
                progressForm.hasErrors = true;
                tryToLoadBMDataCard = true;
            }
        } else {
            showErrorMsg(progressForm, "!!Fehler beim Interpretieren der FIN/VIN: %1", interpreter.getFinOrVinOrBm());
        }
        if (tryToLoadBMDataCard) {
            try {
                return VehicleDataCard.getVehicleDataCard(finOrVin, false, false, true, null, project, true);
            } catch (DataCardRetrievalException e) {
            }
        }
        return null;
    }

    private void onButtonCancel(Event event) {
        closeWindow(event);
    }

    private void doShowIdent(Event event) {
        if (interpreter.isFinOrVinIdValid()) {
            DatacardIdentOrderTypes type = DatacardIdentOrderTypes.FIN;
            if (!interpreter.isFinIdValid()) {
                type = DatacardIdentOrderTypes.VIN;
            }
            DatacardIdentOrderElem identOrderElem = new DatacardIdentOrderElem(getProject(), type);
            identOrderElem.getAggregateIdent().setIdent(interpreter.getFinOrVinOrBm());
            iPartsShowAggIdentDialog.showIdentMembers(parentForm, identOrderElem);
        }
        mainWindow.inputTextArea.requestFocus();
    }

    private iPartsGuiTextFieldBackgroundToggle replaceTextFieldWithCreate(GuiTextField sourceTextField) {
        iPartsGuiTextFieldBackgroundToggle result = new iPartsGuiTextFieldBackgroundToggle();
        replaceTextField(sourceTextField, result);
        return result;
    }

    private void replaceTextField(GuiTextField sourceTextField, iPartsGuiTextFieldBackgroundToggle destTextField) {
        AbstractConstraints constraints = sourceTextField.getConstraints();
        sourceTextField.removeFromParent();
        destTextField.setMinimumWidth(sourceTextField.getMinimumWidth());
        destTextField.setMinimumHeight(sourceTextField.getMinimumHeight());
        destTextField.setEditable(sourceTextField.isEditable());
        destTextField.setIgnoreEditableForCheck(!destTextField.isEditable());
        destTextField.setConstraints(constraints);
    }

    private Set<String> getValidProductNumbers() {
        if (validProductNumbers == null) {
            validProductNumbers = new HashSet<String>();
            for (iPartsProduct product : iPartsProduct.getAllProducts(getProject())) {
                validProductNumbers.add(product.getAsId().getProductNumber());
            }
        }
        return validProductNumbers;
    }

    private GuiTextField getFINTextField() {
        if (withSyntaxCheck) {
            return finModelTextField;
        }
        return mainWindow.finTextfield;
    }

    private GuiTextField getModelTextField() {
        if (withSyntaxCheck) {
            return fzgModelTextField;
        }
        return mainWindow.fzgBMTextfield;
    }

    private GuiTextField getFzgProductTextfield() {
        if (withSyntaxCheck) {
            return fzgProductTextfield;
        }
        return mainWindow.fzgProductTextfield;
    }

    private GuiTextField getAggModelTextfield() {
        if (withSyntaxCheck) {
            return aggModelTextfield;
        }
        return mainWindow.aggBMTextfield;
    }

    private GuiTextField getAggProductTextfield() {
        if (withSyntaxCheck) {
            return aggProductTextfield;
        }
        return mainWindow.aggProductTextfield;
    }

    private GuiTextField getKgTextField() {
        if (withSyntaxCheck) {
            return kgTextField;
        }
        return mainWindow.kgTextfield;
    }

    private GuiTextField getTuTextField() {
        if (withSyntaxCheck) {
            return tuSaaTextField;
        }
        return mainWindow.tuTextfield;
    }

    private String getInputText() {
        return mainWindow.inputTextArea.getText().replace("\n", "").trim();
    }

    private void clearOutputTextfields() {
        getFINTextField().setText("");
        mainWindow.finLabel.setText("!!FIN/ VIN");
        getModelTextField().setText("");
        getFzgProductTextfield().setText("");
        getAggModelTextfield().setText("");
        getAggProductTextfield().setText("");
        getKgTextField().setText("");
        getTuTextField().setText("");
        mainWindow.tuLabel.setText("!!TU/ freie SA");
        mainWindow.title.setSubtitle("");
        if (finModelTextField != null) {
            finModelTextField.setContextMenu(null);
        }
    }

    private void onInputChanged(Event event) {
        clearOutputTextfields();
        boolean isValid = false;
        String input = getInputText();
        if (StrUtils.isValid(input)) {
            interpreter.setRealURL(input);

            if (interpreter.isValid()) {
                isValid = true;
                if (interpreter.isFinOrVinIdValid()) {
                    if (interpreter.isFinIdValid()) {
                        mainWindow.finLabel.setText("!!FIN");
                        getFINTextField().setText(interpreter.getFinId().getFIN());
                    } else {
                        mainWindow.finLabel.setText("!!VIN");
                        getFINTextField().setText(interpreter.getVinId().getVIN());
                    }
                    getModelTextField().setText(interpreter.getModelId(getProject()).getModelNumber());
                } else {
                    mainWindow.finLabel.setText("!!Baumuster");
                    String modelNo = interpreter.getModelId(getProject()).getModelNumber();
                    if (!StrUtils.isValid(modelNo)) {
                        // BM nicht in der DB vorhanden - trotzdem was anzeigen
                        modelNo = interpreter.getFinOrVinOrBm();
                    }
                    getFINTextField().setText(modelNo);
                }
                getFzgProductTextfield().setText(interpreter.getProduct());

                if (interpreter.isAggSet()) {
                    getAggModelTextfield().setText(interpreter.getAggBM());
                    getAggProductTextfield().setText(interpreter.getAggProduct());
                } else {
//                    getAggModelTextfield().setText(TranslationHandler.translate("!!<nicht angegeben>"));
//                    getAggProductTextfield().setText(TranslationHandler.translate("!!<nicht angegeben>"));
                }

                if (interpreter.isNavSet()) {
                    getKgTextField().setText(interpreter.getKg());
                    getTuTextField().setText(interpreter.getTu());
                    if (!interpreter.getTu().isEmpty()) {
                        if (SupportURLInterpreter.isTuValid(interpreter.getTu())) {
                            mainWindow.tuLabel.setText("!!TU");
                        } else {
                            mainWindow.tuLabel.setText("!!freie SA");
                        }
                    }
                } else {
//                    getKgTextField().setText(TranslationHandler.translate("!!<nicht angegeben>"));
//                    getTuTextField().setText(TranslationHandler.translate("!!<nicht angegeben>"));
                }
            } else {
                //mainWindow.title.setSubtitle("!!Ungültige URL");
                mainWindow.title.setSubtitle(TranslationHandler.translate("!!Ungültige URL (%1)", interpreter.getParseErrors().get(0)));
            }
        }
        mainWindow.buttonPanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, isValid);
        if ((finModelTextField != null) && isValid && interpreter.isFinOrVinIdValid()) {
            finModelTextField.setContextMenu(showContextMenu);
        }
    }

    protected class VirtualNodeWithMessageText {

        protected iPartsVirtualNode virtualNode;
        protected String message;
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
        private final de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private final de.docware.framework.modules.gui.controls.GuiPanel contentPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private final de.docware.framework.modules.gui.controls.GuiPanel inputPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private final de.docware.framework.modules.gui.controls.GuiScrollPane inputScrollpane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private final de.docware.framework.modules.gui.controls.GuiTextArea inputTextArea;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private final de.docware.framework.modules.gui.controls.toolbar.GuiToolbar toolbar;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private final de.docware.framework.modules.gui.controls.GuiPanel outputPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private final de.docware.framework.modules.gui.controls.GuiPanel finPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private final de.docware.framework.modules.gui.controls.GuiLabel finLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private final de.docware.framework.modules.gui.controls.GuiTextField finTextfield;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private final de.docware.framework.modules.gui.controls.GuiPanel fzgPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private final de.docware.framework.modules.gui.controls.GuiLabel fzgBMLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private final de.docware.framework.modules.gui.controls.GuiTextField fzgBMTextfield;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private final de.docware.framework.modules.gui.controls.GuiLabel fzgProductLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private final de.docware.framework.modules.gui.controls.GuiTextField fzgProductTextfield;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private final de.docware.framework.modules.gui.controls.GuiPanel aggPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private final de.docware.framework.modules.gui.controls.GuiLabel aggBMLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private final de.docware.framework.modules.gui.controls.GuiTextField aggBMTextfield;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private final de.docware.framework.modules.gui.controls.GuiLabel aggProductLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private final de.docware.framework.modules.gui.controls.GuiTextField aggProductTextfield;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private final de.docware.framework.modules.gui.controls.GuiPanel navPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private final de.docware.framework.modules.gui.controls.GuiLabel kgLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private final de.docware.framework.modules.gui.controls.GuiTextField kgTextfield;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private final de.docware.framework.modules.gui.controls.GuiLabel tuLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private final de.docware.framework.modules.gui.controls.GuiTextField tuTextfield;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private final de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setResizable(false);
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
            title = new de.docware.framework.modules.gui.controls.GuiTitle();
            title.setName("title");
            title.__internal_setGenerationDpi(96);
            title.registerTranslationHandler(translationHandler);
            title.setScaleForResolution(true);
            title.setMinimumWidth(5);
            title.setMinimumHeight(50);
            title.setTitle("!!Support URL Eingabe");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleConstraints.setPosition("north");
            title.setConstraints(titleConstraints);
            this.addChild(title);
            contentPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            contentPanel.setName("contentPanel");
            contentPanel.__internal_setGenerationDpi(96);
            contentPanel.registerTranslationHandler(translationHandler);
            contentPanel.setScaleForResolution(true);
            contentPanel.setMinimumWidth(10);
            contentPanel.setMinimumHeight(10);
            contentPanel.setPaddingTop(4);
            contentPanel.setPaddingLeft(8);
            contentPanel.setPaddingRight(8);
            contentPanel.setPaddingBottom(4);
            de.docware.framework.modules.gui.layout.LayoutBorder contentPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            contentPanel.setLayout(contentPanelLayout);
            inputPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            inputPanel.setName("inputPanel");
            inputPanel.__internal_setGenerationDpi(96);
            inputPanel.registerTranslationHandler(translationHandler);
            inputPanel.setScaleForResolution(true);
            inputPanel.setMinimumWidth(400);
            inputPanel.setMinimumHeight(80);
            inputPanel.setBorderWidth(1);
            inputPanel.setPaddingTop(4);
            inputPanel.setPaddingLeft(4);
            inputPanel.setPaddingRight(4);
            inputPanel.setPaddingBottom(4);
            inputPanel.setTitle("!!XSF Ticket URL");
            de.docware.framework.modules.gui.layout.LayoutBorder inputPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            inputPanel.setLayout(inputPanelLayout);
            inputScrollpane = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            inputScrollpane.setName("inputScrollpane");
            inputScrollpane.__internal_setGenerationDpi(96);
            inputScrollpane.registerTranslationHandler(translationHandler);
            inputScrollpane.setScaleForResolution(true);
            inputScrollpane.setBorderWidth(1);
            inputScrollpane.setHorizontalScrollEnabled(false);
            inputTextArea = new de.docware.framework.modules.gui.controls.GuiTextArea();
            inputTextArea.setName("inputTextArea");
            inputTextArea.__internal_setGenerationDpi(96);
            inputTextArea.registerTranslationHandler(translationHandler);
            inputTextArea.setScaleForResolution(true);
            inputTextArea.setLineWrap(true);
            inputTextArea.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onInputChanged(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder inputTextAreaConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            inputTextArea.setConstraints(inputTextAreaConstraints);
            inputScrollpane.addChild(inputTextArea);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder inputScrollpaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            inputScrollpane.setConstraints(inputScrollpaneConstraints);
            inputPanel.addChild(inputScrollpane);
            toolbar = new de.docware.framework.modules.gui.controls.toolbar.GuiToolbar();
            toolbar.setName("toolbar");
            toolbar.__internal_setGenerationDpi(96);
            toolbar.registerTranslationHandler(translationHandler);
            toolbar.setScaleForResolution(true);
            toolbar.setMinimumWidth(10);
            toolbar.setMinimumHeight(10);
            toolbar.setBackgroundImage(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignToolbarBigBackground"));
            toolbar.setButtonOrientation(de.docware.framework.modules.gui.controls.misc.DWOrientation.VERTICAL);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder toolbarConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            toolbarConstraints.setPosition("east");
            toolbar.setConstraints(toolbarConstraints);
            inputPanel.addChild(toolbar);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder inputPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            inputPanel.setConstraints(inputPanelConstraints);
            contentPanel.addChild(inputPanel);
            outputPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            outputPanel.setName("outputPanel");
            outputPanel.__internal_setGenerationDpi(96);
            outputPanel.registerTranslationHandler(translationHandler);
            outputPanel.setScaleForResolution(true);
            outputPanel.setMinimumWidth(10);
            outputPanel.setMinimumHeight(10);
            outputPanel.setMaximumWidth(400);
            outputPanel.setBorderWidth(1);
            outputPanel.setPaddingTop(4);
            outputPanel.setPaddingBottom(4);
            outputPanel.setTitle("!!Extrahierte Werte");
            de.docware.framework.modules.gui.layout.LayoutGridBag outputPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            outputPanelLayout.setCentered(false);
            outputPanel.setLayout(outputPanelLayout);
            finPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            finPanel.setName("finPanel");
            finPanel.__internal_setGenerationDpi(96);
            finPanel.registerTranslationHandler(translationHandler);
            finPanel.setScaleForResolution(true);
            finPanel.setMinimumWidth(10);
            finPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag finPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            finPanel.setLayout(finPanelLayout);
            finLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            finLabel.setName("finLabel");
            finLabel.__internal_setGenerationDpi(96);
            finLabel.registerTranslationHandler(translationHandler);
            finLabel.setScaleForResolution(true);
            finLabel.setMinimumWidth(81);
            finLabel.setMinimumHeight(10);
            finLabel.setText("!!FIN/ VIN");
            finLabel.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag finLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "e", "n", 4, 4, 4, 4);
            finLabel.setConstraints(finLabelConstraints);
            finPanel.addChild(finLabel);
            finTextfield = new de.docware.framework.modules.gui.controls.GuiTextField();
            finTextfield.setName("finTextfield");
            finTextfield.__internal_setGenerationDpi(96);
            finTextfield.registerTranslationHandler(translationHandler);
            finTextfield.setScaleForResolution(true);
            finTextfield.setMinimumWidth(200);
            finTextfield.setMinimumHeight(10);
            finTextfield.setEditable(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag finTextfieldConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, "w", "b", 4, 7, 4, 8);
            finTextfield.setConstraints(finTextfieldConstraints);
            finPanel.addChild(finTextfield);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag finPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "w", "h", 0, 0, 0, 0);
            finPanel.setConstraints(finPanelConstraints);
            outputPanel.addChild(finPanel);
            fzgPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            fzgPanel.setName("fzgPanel");
            fzgPanel.__internal_setGenerationDpi(96);
            fzgPanel.registerTranslationHandler(translationHandler);
            fzgPanel.setScaleForResolution(true);
            fzgPanel.setMinimumWidth(10);
            fzgPanel.setMinimumHeight(10);
            fzgPanel.setTitle("!!Fahrzeug");
            de.docware.framework.modules.gui.layout.LayoutGridBag fzgPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            fzgPanel.setLayout(fzgPanelLayout);
            fzgBMLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            fzgBMLabel.setName("fzgBMLabel");
            fzgBMLabel.__internal_setGenerationDpi(96);
            fzgBMLabel.registerTranslationHandler(translationHandler);
            fzgBMLabel.setScaleForResolution(true);
            fzgBMLabel.setMinimumWidth(80);
            fzgBMLabel.setMinimumHeight(10);
            fzgBMLabel.setText("!!Baumuster");
            fzgBMLabel.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag fzgBMLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "e", "n", 4, 4, 4, 4);
            fzgBMLabel.setConstraints(fzgBMLabelConstraints);
            fzgPanel.addChild(fzgBMLabel);
            fzgBMTextfield = new de.docware.framework.modules.gui.controls.GuiTextField();
            fzgBMTextfield.setName("fzgBMTextfield");
            fzgBMTextfield.__internal_setGenerationDpi(96);
            fzgBMTextfield.registerTranslationHandler(translationHandler);
            fzgBMTextfield.setScaleForResolution(true);
            fzgBMTextfield.setMinimumWidth(200);
            fzgBMTextfield.setMinimumHeight(10);
            fzgBMTextfield.setEditable(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag fzgBMTextfieldConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, "w", "b", 4, 4, 4, 4);
            fzgBMTextfield.setConstraints(fzgBMTextfieldConstraints);
            fzgPanel.addChild(fzgBMTextfield);
            fzgProductLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            fzgProductLabel.setName("fzgProductLabel");
            fzgProductLabel.__internal_setGenerationDpi(96);
            fzgProductLabel.registerTranslationHandler(translationHandler);
            fzgProductLabel.setScaleForResolution(true);
            fzgProductLabel.setMinimumWidth(80);
            fzgProductLabel.setMinimumHeight(10);
            fzgProductLabel.setText("!!Produkt");
            fzgProductLabel.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag fzgProductLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "e", "n", 4, 4, 4, 4);
            fzgProductLabel.setConstraints(fzgProductLabelConstraints);
            fzgPanel.addChild(fzgProductLabel);
            fzgProductTextfield = new de.docware.framework.modules.gui.controls.GuiTextField();
            fzgProductTextfield.setName("fzgProductTextfield");
            fzgProductTextfield.__internal_setGenerationDpi(96);
            fzgProductTextfield.registerTranslationHandler(translationHandler);
            fzgProductTextfield.setScaleForResolution(true);
            fzgProductTextfield.setMinimumWidth(200);
            fzgProductTextfield.setMinimumHeight(10);
            fzgProductTextfield.setEditable(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag fzgProductTextfieldConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 1, 1, 1, 0.0, 0.0, "w", "b", 4, 4, 4, 4);
            fzgProductTextfield.setConstraints(fzgProductTextfieldConstraints);
            fzgPanel.addChild(fzgProductTextfield);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag fzgPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "w", "h", 0, 0, 0, 0);
            fzgPanel.setConstraints(fzgPanelConstraints);
            outputPanel.addChild(fzgPanel);
            aggPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            aggPanel.setName("aggPanel");
            aggPanel.__internal_setGenerationDpi(96);
            aggPanel.registerTranslationHandler(translationHandler);
            aggPanel.setScaleForResolution(true);
            aggPanel.setMinimumWidth(10);
            aggPanel.setMinimumHeight(10);
            aggPanel.setTitle("!!Aggregat");
            de.docware.framework.modules.gui.layout.LayoutGridBag aggPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            aggPanel.setLayout(aggPanelLayout);
            aggBMLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            aggBMLabel.setName("aggBMLabel");
            aggBMLabel.__internal_setGenerationDpi(96);
            aggBMLabel.registerTranslationHandler(translationHandler);
            aggBMLabel.setScaleForResolution(true);
            aggBMLabel.setMinimumWidth(80);
            aggBMLabel.setMinimumHeight(10);
            aggBMLabel.setText("!!Baumuster");
            aggBMLabel.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag aggBMLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "e", "n", 4, 4, 4, 4);
            aggBMLabel.setConstraints(aggBMLabelConstraints);
            aggPanel.addChild(aggBMLabel);
            aggBMTextfield = new de.docware.framework.modules.gui.controls.GuiTextField();
            aggBMTextfield.setName("aggBMTextfield");
            aggBMTextfield.__internal_setGenerationDpi(96);
            aggBMTextfield.registerTranslationHandler(translationHandler);
            aggBMTextfield.setScaleForResolution(true);
            aggBMTextfield.setMinimumWidth(200);
            aggBMTextfield.setMinimumHeight(10);
            aggBMTextfield.setEditable(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag aggBMTextfieldConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, "w", "b", 4, 4, 4, 4);
            aggBMTextfield.setConstraints(aggBMTextfieldConstraints);
            aggPanel.addChild(aggBMTextfield);
            aggProductLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            aggProductLabel.setName("aggProductLabel");
            aggProductLabel.__internal_setGenerationDpi(96);
            aggProductLabel.registerTranslationHandler(translationHandler);
            aggProductLabel.setScaleForResolution(true);
            aggProductLabel.setMinimumWidth(80);
            aggProductLabel.setMinimumHeight(10);
            aggProductLabel.setText("!!Produkt");
            aggProductLabel.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag aggProductLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "e", "n", 4, 4, 4, 4);
            aggProductLabel.setConstraints(aggProductLabelConstraints);
            aggPanel.addChild(aggProductLabel);
            aggProductTextfield = new de.docware.framework.modules.gui.controls.GuiTextField();
            aggProductTextfield.setName("aggProductTextfield");
            aggProductTextfield.__internal_setGenerationDpi(96);
            aggProductTextfield.registerTranslationHandler(translationHandler);
            aggProductTextfield.setScaleForResolution(true);
            aggProductTextfield.setMinimumWidth(200);
            aggProductTextfield.setMinimumHeight(10);
            aggProductTextfield.setEditable(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag aggProductTextfieldConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 1, 1, 1, 0.0, 0.0, "w", "b", 4, 4, 4, 4);
            aggProductTextfield.setConstraints(aggProductTextfieldConstraints);
            aggPanel.addChild(aggProductTextfield);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag aggPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 0.0, 0.0, "w", "h", 0, 0, 0, 0);
            aggPanel.setConstraints(aggPanelConstraints);
            outputPanel.addChild(aggPanel);
            navPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            navPanel.setName("navPanel");
            navPanel.__internal_setGenerationDpi(96);
            navPanel.registerTranslationHandler(translationHandler);
            navPanel.setScaleForResolution(true);
            navPanel.setMinimumWidth(10);
            navPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag navPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            navPanel.setLayout(navPanelLayout);
            kgLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            kgLabel.setName("kgLabel");
            kgLabel.__internal_setGenerationDpi(96);
            kgLabel.registerTranslationHandler(translationHandler);
            kgLabel.setScaleForResolution(true);
            kgLabel.setMinimumWidth(81);
            kgLabel.setMinimumHeight(10);
            kgLabel.setText("!!KG");
            kgLabel.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag kgLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 3, 1, 1, 0.0, 0.0, "e", "n", 4, 4, 4, 4);
            kgLabel.setConstraints(kgLabelConstraints);
            navPanel.addChild(kgLabel);
            kgTextfield = new de.docware.framework.modules.gui.controls.GuiTextField();
            kgTextfield.setName("kgTextfield");
            kgTextfield.__internal_setGenerationDpi(96);
            kgTextfield.registerTranslationHandler(translationHandler);
            kgTextfield.setScaleForResolution(true);
            kgTextfield.setMinimumWidth(200);
            kgTextfield.setMinimumHeight(10);
            kgTextfield.setEditable(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag kgTextfieldConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 3, 1, 1, 0.0, 0.0, "w", "b", 4, 7, 4, 8);
            kgTextfield.setConstraints(kgTextfieldConstraints);
            navPanel.addChild(kgTextfield);
            tuLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            tuLabel.setName("tuLabel");
            tuLabel.__internal_setGenerationDpi(96);
            tuLabel.registerTranslationHandler(translationHandler);
            tuLabel.setScaleForResolution(true);
            tuLabel.setMinimumWidth(81);
            tuLabel.setMinimumHeight(10);
            tuLabel.setText("!!TU/ freie SA");
            tuLabel.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag tuLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 4, 1, 1, 0.0, 0.0, "e", "n", 4, 4, 4, 4);
            tuLabel.setConstraints(tuLabelConstraints);
            navPanel.addChild(tuLabel);
            tuTextfield = new de.docware.framework.modules.gui.controls.GuiTextField();
            tuTextfield.setName("tuTextfield");
            tuTextfield.__internal_setGenerationDpi(96);
            tuTextfield.registerTranslationHandler(translationHandler);
            tuTextfield.setScaleForResolution(true);
            tuTextfield.setMinimumWidth(200);
            tuTextfield.setMinimumHeight(10);
            tuTextfield.setEditable(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag tuTextfieldConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 4, 1, 1, 0.0, 0.0, "w", "b", 4, 7, 4, 8);
            tuTextfield.setConstraints(tuTextfieldConstraints);
            navPanel.addChild(tuTextfield);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag navPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 3, 1, 1, 0.0, 0.0, "w", "h", 0, 0, 0, 0);
            navPanel.setConstraints(navPanelConstraints);
            outputPanel.addChild(navPanel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder outputPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            outputPanelConstraints.setPosition("south");
            outputPanel.setConstraints(outputPanelConstraints);
            contentPanel.addChild(outputPanel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder contentPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            contentPanel.setConstraints(contentPanelConstraints);
            this.addChild(contentPanel);
            buttonPanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonPanel.setName("buttonPanel");
            buttonPanel.__internal_setGenerationDpi(96);
            buttonPanel.registerTranslationHandler(translationHandler);
            buttonPanel.setScaleForResolution(true);
            buttonPanel.setMinimumWidth(10);
            buttonPanel.setMinimumHeight(10);
            buttonPanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonOKActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onButtonOk(event);
                }
            });
            buttonPanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonCancelActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onButtonCancel(event);
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