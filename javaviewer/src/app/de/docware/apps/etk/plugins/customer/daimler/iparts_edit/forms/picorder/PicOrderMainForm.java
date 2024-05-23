/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.picorder;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumValue;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EnumComboBox;
import de.docware.apps.etk.base.forms.common.EnumLoader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsEventsControl;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.MQHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.AbstractXMLMessageListener;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferStates;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.iPartsMQMessageManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractMQMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.iPartsXMLMediaMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditFormComboboxHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo.iPartsRelatedEditHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.xml.iPartsEditXMLResponseSimulator;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.misc.DWFontStyle;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.misc.StaticConnectionUpdater;
import de.docware.framework.modules.gui.responsive.base.theme.ThemeManager;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Hauptdialog zum Anzeigen und Erstellen von Bildaufträgen
 */
public class PicOrderMainForm extends AbstractJavaViewerForm implements iPartsConst {

    public static void viewPictureOrderDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                              EtkDataAssembly assembly, iPartsDataPicOrder dataPicOrder, PRODUCT_STRUCTURING_TYPE productType) {
        EditModuleFormIConnector editConnector = iPartsRelatedEditHelper.createEditConnector(dataConnector, assembly);
        boolean isCurrentAssemblyCarPerspective = EditModuleHelper.isCarPerspectiveAssembly(assembly);
        PicOrderMainForm dlg = new PicOrderMainForm(editConnector, parentForm, dataPicOrder, productType, false, isCurrentAssemblyCarPerspective);
        if (editConnector != dataConnector) {
            dlg.addOwnConnector(editConnector);
        }
        dlg.setEditAllowed(false);
        dlg.setTitle(getTitle(false, false, dataPicOrder.isCopy()));
        dlg.showModal();
    }

    public static boolean showPictureOrderDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                 iPartsDataPicOrder dataPicOrder, PRODUCT_STRUCTURING_TYPE productType,
                                                 EventListener listenerRefreshPictures, boolean isCurrentAssemblyCarPerspective) {
        PicOrderMainForm dlg = new PicOrderMainForm(dataConnector, parentForm, dataPicOrder, productType, false, isCurrentAssemblyCarPerspective);
        String title;
        if (iPartsTransferStates.isSaveToDB_Allowed(dataPicOrder.getStatus())) {
            title = getTitle(true, false, dataPicOrder.isCopy());
        } else {
            dlg.setEditAllowed(false);
            title = getTitle(false, false, dataPicOrder.isCopy());
            dlg.setListenerForPictures(listenerRefreshPictures);
        }
        dlg.setTitle(title);

        return dlg.showModal() == ModalResult.OK;
    }

    public static iPartsDataPicOrder createPictureOrderDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                              iPartsDataPicOrder dataPicOrder, PRODUCT_STRUCTURING_TYPE productType,
                                                              boolean isCurrentAssemblyCarPerspective) {
        iPartsDataPicOrder result = null;
        PicOrderMainForm dlg = new PicOrderMainForm(dataConnector, parentForm, dataPicOrder, productType, true, isCurrentAssemblyCarPerspective);
        String title = getTitle(false, true, dataPicOrder.isCopy());
        dlg.setTitle(title);
        if (dlg.showModal() == ModalResult.OK) {
            result = dlg.getDataPicOrder();
        }
        dlg.dispose();
        return result;
    }

    public static void savePicOrder(EtkProject project, iPartsDataPicOrder dataPicOrder) {
        project.getDbLayer().startTransaction();
        try {
            dataPicOrder.saveToDB();
            project.getDbLayer().commit();
        } catch (Exception e) {
            project.getDbLayer().rollback();
            Logger.getLogger().handleRuntimeException(e);
        }
    }

    private static String getTitle(boolean forEdit, boolean forCreate, boolean isCopy) {
        if (forCreate) {
            return "!!Bildauftrag erstellen";
        }
        if (forEdit) {
            if (isCopy) {
                return "!!Kopierauftrag ändern";
            } else {
                return "!!Bildauftrag ändern";
            }
        }
        if (isCopy) {
            return "!!Kopierauftrag anzeigen";
        } else {
            return "!!Bildauftrag anzeigen";
        }
    }


    private EditEinPas2Dialog einPas2Dialog;
    private EditKGTUDialog kgtuDialog;
    private EditProductSelectDialog productDialog;
    private iPartsGuiLengthLimitedTextField llTextfieldName;
    private iPartsGuiLengthLimitedTextArea llTextareaComment;
    private iPartsGuiLengthLimitedTextArea llTextareaChangeReason;
    private EnumComboBox moduleTypeCombo;
    private EditASPLMContractorForm contractorForm;
    private GuiButtonOnPanel sendButton;
    private GuiButtonOnPanel changeOrderButton;
    private GuiButtonOnPanel viewPicturesButton;
    private boolean isEditAllowed = true;
    private iPartsDataPicOrder dataPicOrder;
    private PRODUCT_STRUCTURING_TYPE productType;
    private iPartsGuiASPLMItemPanel orderPanel;
    private PicOrderAttachmentsForm editAttachmentForm;
    private EventListener listenerForPictures;
    private iPartsGuiCodeTextField codeTextField;
    private iPartsEventsControl eventsControl;
    private GuiLabel requestorLabel;
    private boolean newlyCreatedOrder;
    private boolean isCurrentAssemblyCarPerspective;

    /**
     * Erzeugt eine Instanz von EditPictureOrderForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public PicOrderMainForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                            iPartsDataPicOrder dataPicOrder, PRODUCT_STRUCTURING_TYPE productType,
                            boolean isCreate, boolean isCurrentAssemblyCarPerspective) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.dataPicOrder = dataPicOrder;
        this.productType = productType;
        this.isCurrentAssemblyCarPerspective = isCurrentAssemblyCarPerspective;
        postCreateGui(isCreate);
        handleInvalidOrder();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui(boolean isCreate) {
        if (dataPicOrder == null) {
            dataPicOrder = iPartsDataPicOrder.createEmptyDataPicOrder(getProject(), getConnector().getCurrentAssembly().getAsId(), null);
        }
        //nicht benutzte Elemente unsichtbar setzen
        if (isCreate) {
            mainWindow.panelOrderNo.setVisible(false);
            mainWindow.panelOrderTyp.setVisible(false);
        } else {
            if (dataPicOrder.getOrderIdExtern().isEmpty()) {
                mainWindow.panelOrderNo.setVisible(false);
            }
            if (dataPicOrder.getStatus() == null) {
                mainWindow.panelOrderTyp.setVisible(false);
            }
        }

        switch (productType) {
            case EINPAS:
                mainWindow.panelKGTU.setVisible(false);
                break;
            case KG_TU:
                mainWindow.panelEinPAS.setVisible(false);
                break;
        }

        mainWindow.panelOrderTyp.setTitle("!!Status");
        mainWindow.comboboxOrderType.setVisible(false);
        mainWindow.labelOrderType.setVisible(false);

        if (dataPicOrder.getLastErrorCode().isEmpty()) {
            mainWindow.textareaOrderMessage.setVisible(false);
            mainWindow.labelOrderMessage.setVisible(false);
        }

        mainWindow.textfieldInitiatorGroup.setVisible(false);
        mainWindow.labelInitiatorGroup.setVisible(false);

        setOKButtonText(dataPicOrder.isValid());
        sendButton = mainWindow.buttonPanel.addCustomButton("!!Senden", ModalResult.NONE);
        EventListener listener = new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                onSendButtonClick(event);
            }
        };
        sendButton.addEventListener(listener);
        viewPicturesButton = mainWindow.buttonPanel.addCustomButton("!!Zeichnungen...", ModalResult.NONE);
        viewPicturesButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                if (!MQHelper.checkTransmissionToASPLMConfigWithMessage()) {
                    return;
                }
                onViewButtonClick(event);
            }
        });
        viewPicturesButton.setVisible(isViewMediaContentVisible());

        changeOrderButton = mainWindow.buttonPanel.addCustomButton("!!Bildauftrag ändern...", ModalResult.NONE);
        changeOrderButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                onChangeOrderButtonClicked(event);
            }
        });
        changeOrderButton.setVisible(iPartsTransferStates.canRequestChange(dataPicOrder.getStatus()) && dataPicOrder.hasOnlyInvalidatedChangeOrder());

        if (mainWindow.panelOrderNo.isVisible()) {
            orderPanel = new iPartsGuiASPLMItemPanel();
            switchGuiControls(mainWindow.panelOrderNo, orderPanel);
            orderPanel.setTitle(mainWindow.panelOrderNo.getTitle());
        }

        //Series Dialog aufschnappen
        mainWindow.panelOrderProduct.removeAllChildren();
        mainWindow.panelOrderProduct.setTitle("");
        productDialog = new EditProductSelectDialog(getConnector(), this, null);

        AbstractGuiControl productGui = productDialog.getProductPanel();
        productGui.removeFromParent();
        productGui.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        mainWindow.panelOrderProduct.setLayout(new LayoutBorder());
        mainWindow.panelOrderProduct.addChild(productGui);
        EventListener onProductChangeListener = new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                doEnableButtons();
            }
        };
        productDialog.setOnProductChangeListener(onProductChangeListener);
        String ebene = getConnector().getCurrentAssembly().getEbeneName();
        boolean isProductEditAllowed = isEditAllowed;
        if (isProductEditAllowed) {
            if (ebene.equals(PARTS_LIST_TYPE_DIALOG_RETAIL) ||
                ebene.equals(PARTS_LIST_TYPE_EDS_RETAIL)) {
                isProductEditAllowed = false;
            }
        }
        productDialog.setEditable(isProductEditAllowed);

        AbstractGuiControl gui;
        switch (productType) {
            case EINPAS:
                //EinPAS Dialog aufschnappen
                einPas2Dialog = new EditEinPas2Dialog(getConnector(), this, null);
                gui = einPas2Dialog.getGui();
                gui.removeFromParent();
                gui.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
                mainWindow.panelEinPAS.addChild(gui);
                einPas2Dialog.setStartLanguage(getProject().getDBLanguage());
                einPas2Dialog.setPanelTitle("!!EinPAS Struktur");
                einPas2Dialog.setPanelTitleFontStyle(DWFontStyle.BOLD);
                einPas2Dialog.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
                    @Override
                    public void fire(Event event) {
                        doEnableButtons();
                    }
                });
                break;
            case KG_TU:
                //KG_TU Dialog aufschnappen
                kgtuDialog = new EditKGTUDialog(getConnector(), this, null);
                gui = kgtuDialog.getGui();
                gui.removeFromParent();
                gui.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
                if (isCurrentAssemblyCarPerspective) {
                    mainWindow.panelKGTU.setVisible(false);
                } else {
                    mainWindow.panelKGTU.addChild(gui);
                }
                kgtuDialog.setStartLanguage(getProject().getDBLanguage());
                kgtuDialog.setPanelTitle("!!KG-TU Struktur");
                kgtuDialog.setPanelTitleFontStyle(DWFontStyle.BOLD);
                kgtuDialog.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
                    @Override
                    public void fire(Event event) {
                        doEnableButtons();
                    }
                });
                break;
        }
        iPartsDataAssembly dataAssembly = getAssembly();
        if ((dataAssembly != null) && !dataAssembly.getDocumentationType().isPKWDocumentationType()) {
            mainWindow.labelWorkingContext.setVisible(true);
            mainWindow.textfieldWorkingContext.setVisible(true);
        }
        replaceGuiControls();

        // DAIMLER-833
        mainWindow.calendarOrderDate.setEditable(false);
        mainWindow.calenderInitiatorDateTime.setEditable(false);

        requestorLabel = new GuiLabel();
        switchGuiControls(mainWindow.panelInitiator, requestorLabel);

        //textarea für Änderungsgrund austauschen
        llTextareaChangeReason = new iPartsGuiLengthLimitedTextArea();
        switchGuiControls(mainWindow.textareaReason, llTextareaChangeReason);
        llTextareaChangeReason.setLengthLimit(iPartsTransferConst.ASPLM_CHANGE_REASON_MAX_LENGTH);
        llTextareaChangeReason.setMinimumHeight(mainWindow.textareaReason.getMinimumHeight());
        mainWindow.textareaReason.copyEventListeners(llTextareaChangeReason, Event.ON_CHANGE_EVENT);

        editAttachmentForm = new PicOrderAttachmentsForm(getConnector(), this, dataPicOrder, isEditAllowed);
        AbstractGuiControl attachments = editAttachmentForm.getGui();
        attachments.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        mainWindow.tabbedpaneentryPicAttachments.setLayout(new LayoutBorder());
        mainWindow.tabbedpaneentryPicAttachments.addChild(attachments);
        mainWindow.buttonPartInfo.setVisible(!isCurrentAssemblyCarPerspective);
        dataToGui();

        // Optimale Größe berechnen
        ThemeManager.get().render(mainWindow);
        switch (productType) {
            case EINPAS:
                einPas2Dialog.getGui().setMaximumWidth(mainWindow.getWidth() - 40);
                break;
            case KG_TU:
                kgtuDialog.getGui().setMaximumWidth(mainWindow.getWidth() - 40);
                break;
        }

        mainWindow.panelMain.setPaddingBottom(8);
        mainWindow.panelMain.setMinimumWidth(Math.max(mainWindow.panelMainSub.getPreferredWidth(), 550));
        mainWindow.panelMain.setMinimumHeight(mainWindow.panelMainSub.getPreferredHeight() + mainWindow.panelButton.getPreferredHeight() + 10);
        mainWindow.pack();
        mainWindow.panelMain.setMinimumWidth(200);
        mainWindow.panelMain.setMinimumHeight(200);

        initMQListenerForStateChange();

        doEnableButtons();
    }

    /**
     * Gibt eine Meldung aus und sperrt den Bildauftrag für den Edit, wenn der Bildauftrag "ungültig" markiert wurde.
     */
    private void handleInvalidOrder() {
        if (dataPicOrder != null) {
            String orderType;
            if (dataPicOrder.isChangeOrder()) {
                orderType = TranslationHandler.translate("!!Änderungsauftrag");
            } else if (dataPicOrder.isCopy()) {
                orderType = TranslationHandler.translate("!!Kopierauftrag");
            } else {
                orderType = TranslationHandler.translate("!!Bildauftrag");
            }
            String text = null;
            if (dataPicOrder.isInvalid()) {
                text = TranslationHandler.translate("!!%1 wurde als \"ungültig\" markiert.", orderType);
            } else if (dataPicOrder.isCancelled()) {
                text = TranslationHandler.translate("!!%1 wurde als \"storniert\" markiert.", orderType);
            } else if (dataPicOrder.hasInvalidImageData()) {
                text = TranslationHandler.translate("!!Die erhaltenen Bildinformationen sind fehlerhaft. Bitte Auftrag korrigieren!", orderType);
            }
            if (text == null) {
                return;
            }

            mainWindow.title.setSubtitle(text);
            mainWindow.title.setSubtitleForegroundColor(Colors.clRed);
            mainWindow.title.setSubtitleFontStyle(DWFontStyle.BOLD);
            setEditAllowed(false);
        }
    }

    /**
     * Überträgt die Eigenschaften des übergebenen <code>existingControl</code> auf das <code>newControl</code>
     *
     * @param existingControl
     * @param newControl
     */
    private void switchGuiControls(AbstractGuiControl existingControl, AbstractGuiControl newControl) {
        AbstractGuiControl parent = existingControl.getParent();
        newControl.setConstraints(existingControl.getConstraints());
        newControl.setName(existingControl.getName());
        newControl.__internal_setGenerationDpi(existingControl.getGenerationDpi());
        newControl.registerTranslationHandler(existingControl.getTranslationHandler());
        existingControl.removeFromParent();
        parent.addChild(newControl);
    }

    private void initMQListenerForStateChange() {
        // Listener für das Aktualisieren des Status
        AbstractXMLMessageListener listenerPicOrderStatusChange = new AbstractXMLMessageListener() {
            @Override
            public boolean messageReceived(AbstractMQMessage xmlMQMessage) {
                if (xmlMQMessage.isOfType(iPartsXMLMediaMessage.TYPE)) {
                    iPartsXMLMediaMessage mediaMessage = (iPartsXMLMediaMessage)xmlMQMessage;
                    if (!mediaMessage.isResponse()) {
                        Logger.getLogger().throwRuntimeException("Message type must be an iPartsXMLResponse! Type is: "
                                                                 + mediaMessage.getClass().getName());
                        return true; // keine weitere Verarbeitung sinnvoll
                    }
                    boolean validiPartsId = (mediaMessage.getiPartsRequestId().equals(dataPicOrder.getAsId().getOrderGuid()));
                    boolean validResponseType = iPartsTransferNodeTypes.isValidFinalOperation(mediaMessage.getResponse().getRequestOperation());
                    if (validResponseType && validiPartsId) {
                        dataPicOrder.loadFromDB(dataPicOrder.getAsId());
                        setStatus();
                        doEnableButtons();
                        // Nur bei einer Korrektur muss Gruppe und User angepasst werden
                        if (mediaMessage.getResponse().getRequestOperation() == iPartsTransferNodeTypes.CORRECT_MEDIA_ORDER) {
                            changeGroupAndUser();
                        }
                        // Zeige den neuen Status
                        if (listenerForPictures != null) {
                            listenerForPictures.fire(new Event(Event.GENERIC_TYPE));
                        }
                        StaticConnectionUpdater.updateBrowser(getGui()); // Push der GUI-Aktualisierung vom Server zum Client
                    }
                }
                return false;
            }
        };
        iPartsMQMessageManager.getInstance(iPartsPlugin.XML_MESSAGE_MANAGER_NAME_MEDIA).addXMLMessageListener(listenerPicOrderStatusChange,
                                                                                                              dataPicOrder.getAsId().getOrderGuid());
    }

    private void onChangeOrderButtonClicked(Event event) {
        if (dataPicOrder != null) {
            Optional<iPartsDataPicOrder> picOrderForChangeOrder = iPartsPicOrderEditHelper.getNewestChangeOrderForPicOrderFromSameModule(getProject(), dataPicOrder, getAssembly(), false);
            if (!picOrderForChangeOrder.isPresent()) {
                return;
            }
            // Wird ein Änderungsauftrag erstellt, dann muss der Kontext geleert werden, weil der Änderungsauftrag
            // seinen eigenen haben wird
            clearWorkingContext();
            iPartsDataPicOrder newDataPicOrder = picOrderForChangeOrder.get().createChangeOrder(false);
            // Gültigkeiten abgleichen
            newDataPicOrder.alignPictureAndPicOrderValidities(getConnector().getCurrentAssembly());
            saveCurrentPicOrder();
            dataPicOrder = newDataPicOrder;
            setEditAllowed(true);
            editAttachmentForm.setPictureOrder(dataPicOrder);
            dataPicOrder.resetUsedPictures();
            // Der Änderungsauftrag muss in die DB geschrieben werden, weil sonst in der Zeit des Ausfüllens ein anderer
            // Autor einen Änderungsauftrag erzeugen könnte.
            saveCurrentPicOrder();
            dataToGui();
            // Flag, dass über den internen Button ein Änderungsauftrag erzeugt wurde
            newlyCreatedOrder = true;
        }
    }

    private void clearWorkingContext() {
        iPartsDataAssembly assembly = getAssembly();
        if ((assembly != null) && !assembly.getDocumentationType().isPKWDocumentationType()) {
            mainWindow.textfieldWorkingContext.setText("");
        }
    }

    private boolean isViewMediaContentVisible() {
        boolean isVisibleFromStateMachine = iPartsTransferStates.isViewingMediaContentsAllowed(dataPicOrder.getStatus());
        boolean viewButtonVisibleException = (iPartsTransferStates.isInCorrectionWorkflow(dataPicOrder.getStatus())
                                              && !dataPicOrder.getPictures().isEmpty());
        boolean isInWorkingStateWithPictures = ((dataPicOrder.getStatus() == iPartsTransferStates.IN_BEARBEIT) && !dataPicOrder.getPictures().isEmpty());
        return (isVisibleFromStateMachine || viewButtonVisibleException || isInWorkingStateWithPictures);
    }

    private void dataToGui() {
        if (dataPicOrder != null) {
            //mainWindow.textfieldOrderNo.setText(dataPicOrder.getOrderIdExtern());
            if (orderPanel != null) {
                if (StrUtils.isValid(dataPicOrder.getOrderItemId().getMcItemId())) {
                    orderPanel.setVisible(true);
                    orderPanel.setAsPlmItemId(dataPicOrder.getOrderItemId());
                } else {
                    orderPanel.setVisible(false);
                }
            }
            setStatus();
            if (!dataPicOrder.getUsages().isEmpty()) {
                iPartsDataPicOrderUsage picOrderUsage = dataPicOrder.getUsages().get(0);
                //hier ProductId bestimmen
                iPartsProductId productId = new iPartsProductId(picOrderUsage.getAsId().getProductNo());
                productDialog.setProductId(productId);
                switch (productType) {
                    case EINPAS:
                        EinPasId testEinPasId = picOrderUsage.getEinPASId();
                        if (testEinPasId != null) {
                            einPas2Dialog.setStartEinPasId(testEinPasId);
                        }
                        break;
                    case KG_TU:
                        kgtuDialog.setProductId(productId);
                        KgTuId testKgTuId = picOrderUsage.getKgTuId();
                        // Bei einer freien SA müssen die KG/TU-Werte von außen gesetzt werden.
                        if (iPartsPicOrderEditHelper.isRetailSa(getConnector())) {
                            setKgTuForSA(productId, testKgTuId);
                        } else if (testKgTuId != null) {
                            if (isCurrentAssemblyCarPerspective) {
                                productDialog.setEditable(false);
                                if (StrUtils.isValid(testKgTuId.getTu())) {
                                    kgtuDialog.setKgTuForReadOnly(testKgTuId);
                                } else {
                                    kgtuDialog.setKgTuForReadOnly(new KgTuId(testKgTuId.getKg(), "000"));
                                }
                                kgtuDialog.setNotExistingKgTuNodeInfoVisible(false);
                            } else {
                                kgtuDialog.setStartKgTuId(testKgTuId);
                            }
                        }
                        break;
                }
            }

            //mainWindow.textfieldName.setText(dataPicOrder.getFieldValue(FIELD_DA_PO_PROPOSED_NAME));
            llTextfieldName.setText(dataPicOrder.getFieldValue(FIELD_DA_PO_PROPOSED_NAME));
            setSelectedForModuleTypeCombo(dataPicOrder.getFieldValue(FIELD_DA_PO_PICTURE_TYPE));
            //ASPLM-Contractor User und Group setzen
            contractorForm.setSelectedGroupGuid(dataPicOrder.getFieldValue(FIELD_DA_PO_JOB_GROUP));
            contractorForm.setSelectedUserGuid(dataPicOrder.getFieldValue(FIELD_DA_PO_JOB_USER));

            mainWindow.textareaOrderMessage.setText(prepareErrorMsg());

            String date = dataPicOrder.getAttribute(FIELD_DA_PO_ORDERDATE, false).getAsString();
            mainWindow.calendarOrderDate.setDate(date);
            date = dataPicOrder.getAttribute(FIELD_DA_PO_TARGETDATE, false).getAsString();
            mainWindow.calendarTargetDeadline.setDate(date);
            date = dataPicOrder.getAttribute(FIELD_DA_PO_CREATEDATE, false).getAsString();
            if (date.isEmpty()) {
                mainWindow.calenderInitiatorDateTime.clearDateTime();
            } else {
                mainWindow.calenderInitiatorDateTime.setDateTime(date);
            }

            //mainWindow.textareaComment.setText(dataPicOrder.getAttribute(FIELD_DA_PO_DESCRIPTION, false).getAsString());
            llTextareaComment.setText(dataPicOrder.getAttribute(FIELD_DA_PO_DESCRIPTION, false).getAsString());
            codeTextField.setText(dataPicOrder.getFieldValue(FIELD_DA_PO_CODES));
            String eventId = dataPicOrder.getFieldValue(FIELD_DA_PO_EVENT_FROM);
            if (StrUtils.isValid(eventId)) {
                eventsControl.setSelectedEventFrom(eventId);
            }
            eventId = dataPicOrder.getFieldValue(FIELD_DA_PO_EVENT_TO);
            if (StrUtils.isValid(eventId)) {
                eventsControl.setSelectedEventTo(eventId);
            }
            // Wert für "Nur bei FIN ausgeben" setzen
            mainWindow.checkboxOnlyWithFIN.setSelected(dataPicOrder.getFieldValueAsBoolean(FIELD_PO_ONLY_FIN_VISIBLE));
            // Änderungsgrund setzen (sofern einer angegeben wurde)
            if (dataPicOrder.isChangeOrCopy()) {
                llTextareaChangeReason.setText(dataPicOrder.getChangeReason());
            }
            // User ist der aktuelle Benutzer falls noch nicht gesetzt bzw. bei einem Änderungs- oder Kopierauftrag
            String userId = dataPicOrder.getFieldValue(FIELD_DA_PO_USER_GUID);
            if (userId.isEmpty() || iPartsTransferStates.isCreatedChangeOrCopyOrder(dataPicOrder.getStatus())) {
                userId = FrameworkUtils.getUserName();
            }
            requestorLabel.setText(userId);

            mainWindow.textfieldInitiatorGroup.setText(dataPicOrder.getAttribute(FIELD_DA_PO_USER_GROUP_GUID, false).getAsString());

            editAttachmentForm.updateAttachmentGrid(-1);

        }
    }

    private iPartsModuleId getModuleId() {
        EtkDataAssembly currentAssembly = getConnector().getCurrentAssembly();
        return new iPartsModuleId(currentAssembly.getAsId().getKVari());
    }

    /**
     * Setzt die KG/TU-Informationen zur freien SA
     *
     * @param productId
     * @param kgTuId
     */
    private void setKgTuForSA(iPartsProductId productId, KgTuId kgTuId) {
        if (!iPartsPicOrderEditHelper.isRetailSa(getConnector())) {
            return;
        }
        if (StrUtils.isValid(productId.getProductNumber(), kgTuId.getKg())) {
            productDialog.setProductId(productId);
            productDialog.setEditable(false);
            if (StrUtils.isValid(kgTuId.getTu())) {
                kgtuDialog.setKgTuForReadOnly(kgTuId);
            } else {
                kgtuDialog.setKgTuForReadOnly(new KgTuId(kgTuId.getKg(), "001"));
            }
            kgtuDialog.setNotExistingKgTuNodeInfoVisible(false);
        } else {
            MessageDialog.showWarning(TranslationHandler.translate("!!Der Bildauftrag kann nicht verarbeitet werden, weil die Verortung des SA-TUs \"%1\" nicht geladen werden konnte!",
                                                                   getModuleId().getModuleNumber()), "!!Bildauftrag");
        }
    }

    private void changeGroupAndUser() {
        //ASPLM-Contractor User und Group setzen
        contractorForm.setUserGroupNameFromID(dataPicOrder.getFieldValue(FIELD_DA_PO_JOB_GROUP));
        contractorForm.setUserNameFromID(dataPicOrder.getFieldValue(FIELD_DA_PO_JOB_GROUP),
                                         dataPicOrder.getFieldValue(FIELD_DA_PO_JOB_USER));
    }

    private String prepareErrorMsg() {
        if (dataPicOrder.isASPLMWarning()) {
            mainWindow.labelOrderMessage.setText("!!Warnung");
            return dataPicOrder.getLastErrorText(getProject().getDBLanguage());
        } else if (dataPicOrder.isASPLMComment()) {
            mainWindow.labelOrderMessage.setText("!!Kommentar");
            String result = dataPicOrder.getLastErrorText(getProject().getDBLanguage());
            String userId = dataPicOrder.getLastErrorCode();
            if (!userId.equals(iPartsDataPicOrder.ASPLM_ERROR_IDENTIFIER.COMMENT.getPrefix())) {
                userId = userId.substring(iPartsDataPicOrder.ASPLM_ERROR_IDENTIFIER.COMMENT.getPrefix().length());
                result = userId + ": " + result;
            }
            return result;
        }
        return dataPicOrder.getLastErrorCode() + " " + dataPicOrder.getLastErrorText(getProject().getDBLanguage());
    }

    private void guiToData() {
        iPartsTransferStates orderState = iPartsTransferStates.getStateForSaveToDB(dataPicOrder.getStatus());
        if (orderState != null) {
            // noch kein Wert eingetragen
            dataPicOrder.setStatus(orderState, DBActionOrigin.FROM_EDIT);
        }
        // Die AS-PLM ID kann nicht verändert werden => kein neues Setzen
        //dataPicOrder.setAttributeValue(FIELD_DA_PO_ORDER_ID_EXTERN, mainWindow.textfieldOrderNo.getText(), DBActionOrigin.FROM_EDIT);
        // OrderStatus kann nicht verändert werden => kein neues Setzen
        //dataPicOrder.setAttributeValue(FIELD_DA_PO_STATUS, mainWindow.textfieldOrderState.getText(), DBActionOrigin.FROM_EDIT);

        iPartsProductId productId = productDialog.getProductId();
        // Usage Eintrag aktualisieren
        if (dataPicOrder.getUsages().isEmpty()) {
            switch (productType) {
                case EINPAS:
                    dataPicOrder.addUsage(productId, getEinPASId(), DBActionOrigin.FROM_EDIT);
                    break;
                case KG_TU:
                    dataPicOrder.addUsage(productId, getKgTuId(), DBActionOrigin.FROM_EDIT);
                    break;
            }
        } else {
            String orderGuid = dataPicOrder.getAttribute(FIELD_DA_PO_ORDER_GUID, false).getAsString();
            iPartsDataPicOrderUsage picOrderUsage = dataPicOrder.getUsages().get(0);
            iPartsPicOrderUsageId id = null;
            switch (productType) {
                case EINPAS:
                    id = new iPartsPicOrderUsageId(orderGuid, productId, getEinPASId());
                    break;
                case KG_TU:
                    id = new iPartsPicOrderUsageId(orderGuid, productId, getKgTuId());
                    break;
            }
            if (id != null) {
                picOrderUsage.setId(id, DBActionOrigin.FROM_EDIT);
            }
        }

        // Bildauftrag Name aktualisieren
        //dataPicOrder.setAttributeValue(FIELD_DA_PO_PROPOSED_NAME, mainWindow.textfieldName.getText(), DBActionOrigin.FROM_EDIT);
        dataPicOrder.setFieldValue(FIELD_DA_PO_PROPOSED_NAME, llTextfieldName.getText(), DBActionOrigin.FROM_EDIT);

        // Bildauftrag Picture Type aktualisieren
        dataPicOrder.setFieldValue(FIELD_DA_PO_PICTURE_TYPE, moduleTypeCombo.getActToken(), DBActionOrigin.FROM_EDIT);
        //ASPLM-Contractor User und Group setzen
        String groupGuid = contractorForm.getSelectedGroupGuid();
        if (groupGuid == null) {
            groupGuid = "";
        }
        dataPicOrder.setFieldValue(FIELD_DA_PO_JOB_GROUP, groupGuid, DBActionOrigin.FROM_EDIT);
        String userGuid = contractorForm.getSelectedUserGuid();
        if (userGuid == null) {
            userGuid = "";
        }
        dataPicOrder.setFieldValue(FIELD_DA_PO_JOB_USER, userGuid, DBActionOrigin.FROM_EDIT);

        String date = mainWindow.calendarOrderDate.getDateAsRawString();
        dataPicOrder.setFieldValue(FIELD_DA_PO_ORDERDATE, date, DBActionOrigin.FROM_EDIT);
        date = mainWindow.calendarTargetDeadline.getDateAsRawString();
        dataPicOrder.setFieldValue(FIELD_DA_PO_TARGETDATE, date, DBActionOrigin.FROM_EDIT);
        date = mainWindow.calenderInitiatorDateTime.getDateTimeAsRawString();
        dataPicOrder.setFieldValue(FIELD_DA_PO_CREATEDATE, date, DBActionOrigin.FROM_EDIT);

        // Bildauftrag Beschreibung
        //dataPicOrder.setAttributeValue(FIELD_DA_PO_DESCRIPTION, mainWindow.textareaComment.getText(), DBActionOrigin.FROM_EDIT);
        dataPicOrder.setFieldValue(FIELD_DA_PO_DESCRIPTION, llTextareaComment.getText(), DBActionOrigin.FROM_EDIT);

        // Code-Gültigkeit
        dataPicOrder.setFieldValue(FIELD_DA_PO_CODES, codeTextField.getText(), DBActionOrigin.FROM_EDIT);

        // Ereignissteuerung
        String eventFrom = "";
        if (eventsControl.isEventFromSelected()) {
            eventFrom = eventsControl.getSelectedEventFrom().getEventId();
        }
        dataPicOrder.setFieldValue(FIELD_DA_PO_EVENT_FROM, eventFrom, DBActionOrigin.FROM_EDIT);
        String eventTo = "";
        if (eventsControl.isEventToSelected()) {
            eventTo = eventsControl.getSelectedEventTo().getEventId();
        }
        dataPicOrder.setFieldValue(FIELD_DA_PO_EVENT_TO, eventTo, DBActionOrigin.FROM_EDIT);

        // Wert für "Nur bei FIN ausgeben" setzen
        dataPicOrder.setFieldValueAsBoolean(FIELD_PO_ONLY_FIN_VISIBLE, mainWindow.checkboxOnlyWithFIN.isSelected(), DBActionOrigin.FROM_EDIT);

        // Änderungsgrund
        if (dataPicOrder.isChangeOrCopy() && StrUtils.isValid(getReasonForChange())) {
            dataPicOrder.setChangeReason(getReasonForChange());
        }

        // der User und die Usergroup
        dataPicOrder.setFieldValue(FIELD_DA_PO_USER_GUID, requestorLabel.getText(), DBActionOrigin.FROM_EDIT);
        dataPicOrder.setFieldValue(FIELD_DA_PO_USER_GROUP_GUID, "", DBActionOrigin.FROM_EDIT); // Benutzergruppe ist immer leer

        if (!dataPicOrder.getAttachments().isEmpty()) {
            dataPicOrder.setFieldValueAsBoolean(FIELD_DA_PO_HAS_ATTACHMENTS, true, DBActionOrigin.FROM_EDIT);
        }
    }

    private boolean checkData() {
        // nur überprüfen, falls im EditModus
        if (isEditAllowed) {
            // MUSS-Felder
            // Name
//            if (mainWindow.textfieldName.getText().isEmpty()) {
//                return false;
//            }
            if (llTextfieldName.getText().isEmpty()) {
                return false;
            }
            // Module Type
            if (moduleTypeCombo.getSelectedIndex() == -1) {
                return false;
            }
            // Customer Group
            if (!contractorForm.isValid()) {
                return false;
            }
            // Product
            if (!productDialog.isProductIdValid()) {
                return false;
            }
            switch (productType) {
                case EINPAS:
                    EinPasId einPASId = getEinPASId();
                    if ((einPASId == null) || !einPASId.isValidId()) {
                        return false;
                    }
                    break;
                case KG_TU:
                    KgTuId kgTuId = getKgTuId();
                    if ((kgTuId == null) || !kgTuId.isValidId()) {
                        return false;
                    }
                    break;

            }

            // Wenn es sich um einen Änderungsauftrag oder Kopierauftrag handelt, dann ist der Grund für die Änderung zwingend notwendig
            // und darf nicht länger als iPartsTransferConst.ASPLM_CHANGE_REASON_MAX_LENGTH Zeichen sein.
            if (dataPicOrder.isChangeOrCopy()) {
                String reason = getReasonForChange();
                int newlineCounter = StrUtils.countCharacters(reason, '\n');
                // Unter Windows wird aus einem Zeichen '\n' automatisch zwei Zeichen <CR><LF>
                // Bei reason.length() wird das '\n' mitgezählt.
                // Um das implizit angehängte '\r' auch noch mit zu zählen müssen die '\n' im String verdoppelt werden.
                if (StrUtils.isEmpty(reason) || ((reason.length() + newlineCounter) > iPartsTransferConst.ASPLM_CHANGE_REASON_MAX_LENGTH)) {
                    return false;
                }
            }
            // Description ist optional
            // DateDue ist optional
        }
        return true;
    }

    private void doEnableButtons() {
        boolean isOK = checkData();
        mainWindow.buttonPanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, isOK);
        String ebene = getConnector().getCurrentAssembly().getEbeneName();

        // Zeige die Felder für den Grund einer Änderung nur bei einem Änderungsauftrag an
        mainWindow.labelReason.setVisible(dataPicOrder.isChangeOrCopy());
        mainWindow.scrollpaneReason.setVisible(dataPicOrder.isChangeOrCopy());

        boolean validLevel = !ebene.isEmpty() && (ebene.equals(PARTS_LIST_TYPE_DIALOG_RETAIL)
                                                  || ebene.equals(PARTS_LIST_TYPE_EDS_RETAIL)
                                                  || ebene.equals(PARTS_LIST_TYPE_SA_RETAIL)
                                                  || ebene.equals(PARTS_LIST_TYPE_WORKSHOP_MATERIAL)
                                                  || ebene.equals(PARTS_LIST_TYPE_PSK_PKW)
                                                  || ebene.equals(PARTS_LIST_TYPE_PSK_TRUCK)
                                                  || ebene.equals(PARTS_LIST_TYPE_CAR_PERSPECTIVE));
        if (viewPicturesButton.isVisible()) {
            if (isViewMediaContentVisible()) {
                viewPicturesButton.setEnabled(!dataPicOrder.getPictures().isEmpty());
            } else {
                viewPicturesButton.setVisible(false);
            }
        }
        changeOrderButton.setVisible(getConnector().isAuthorOrderValid() && iPartsTransferStates.canRequestChange(dataPicOrder.getStatus()) && dataPicOrder.hasOnlyInvalidatedChangeOrder());
        enableSendButton(isOK, validLevel);
        setWorkingContext();
    }

    /**
     * Setzt den Bearbeitungscontext für nicht PKW Module
     */
    private void setWorkingContext() {
        if (StrUtils.isEmpty(mainWindow.textfieldWorkingContext.getText())) {
            mainWindow.textfieldWorkingContext.setText(dataPicOrder.getWorkingContextFromParts(getConnector().getCurrentPartListEntries()));
        }
    }

    /**
     * Bestimmt die Sichtbarkeit des "Senden" Buttons
     *
     * @param isOK
     * @param validLevel
     */
    private void enableSendButton(boolean isOK, boolean validLevel) {
        sendButton.setVisible(isEditAllowed());
        boolean buttonEnabled = (isOK && validLevel && iPartsTransferStates.isSaveToMQ_Allowed(dataPicOrder.getStatus()));
        sendButton.setEnabled(buttonEnabled);
    }

    private void setErrorMessage() {
        if (dataPicOrder.getLastErrorCode().isEmpty()) {
            int minusHeight = mainWindow.textareaOrderMessage.getMinimumHeight();
            mainWindow.textareaOrderMessage.setVisible(false);
            mainWindow.labelOrderMessage.setVisible(false);
            mainWindow.setHeight(mainWindow.getHeight() - minusHeight);
        } else {
            mainWindow.textareaOrderMessage.setText(prepareErrorMsg());
        }
    }

    private EinPasId getEinPASId() {
        if (einPas2Dialog != null) {
            return einPas2Dialog.getEinPasId();
        }
        return null;
    }

    private KgTuId getKgTuId() {
        if (kgtuDialog != null) {
            return kgtuDialog.getKgTuId();
        }
        return null;
    }

    /**
     * Statuswert anzeigen
     */
    private void setStatus() {
        iPartsTransferStates orderState = dataPicOrder.getStatus();
        if (orderState != null) {
            String value = orderState.getDBValue();
            List<String> items = new DwList<String>();
            List<String> tokens = new DwList<String>();
            EnumLoader.baseSetEnumTexte(getProject(), TABLE_DA_PICORDER, FIELD_DA_PO_STATUS, getProject().getDBLanguage(),
                                        items, tokens, true, false);
            String stateText = "";
            for (int lfdNr = 0; lfdNr < tokens.size(); lfdNr++) {
                if (value.equals(tokens.get(lfdNr))) {
                    stateText = items.get(lfdNr);
                    break;
                }
            }
            if (!stateText.isEmpty()) {
                if (dataPicOrder.hasEventForInvalidState()) {
                    stateText = stateText + " (" + dataPicOrder.getEventNameForInvalidState().getAsplmValueForText() + ")";
                }
                mainWindow.textfieldOrderState.setText(stateText);
            }
        }
    }

    /**
     * Platzhalter Controls durch die echten Gui Controls ersetzen und initialisieren
     */
    private void replaceGuiControls() {
        // comboboxOrderShowType gegen eine EnumComboBox() austauschen
        EventListener eventListener = new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                onChangeRequiredEvent(event);
            }
        };
        //textfieldName austauschen
        llTextfieldName = new iPartsGuiLengthLimitedTextField();
        switchGuiControls(mainWindow.textfieldName, llTextfieldName);
        llTextfieldName.setLengthLimit(120);
        llTextfieldName.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            public void fire(Event event) {
                onChangeRequiredEvent(event);
            }
        });
        //textareaComment austauschen
        llTextareaComment = new iPartsGuiLengthLimitedTextArea();
        switchGuiControls(mainWindow.textareaComment, llTextareaComment);
        llTextareaComment.setLengthLimit(iPartsTransferConst.ASPLM_MC_DESCRIPTION_LENGTH);
        llTextareaComment.setMinimumHeight(mainWindow.textareaComment.getMinimumHeight());

        // Label-Text aus Tabellen-Spaltentext holen
        EtkDisplayField codeDisplayField = new EtkDisplayField(TABLE_DA_PICORDER, FIELD_DA_PO_CODES, true, false);
        codeDisplayField.loadStandards(getConfig());
        EtkMultiSprache codeValidityText = codeDisplayField.getText();
        mainWindow.labelCode.setText(codeValidityText.getText(getProject().getViewerLanguage()));

        codeTextField = new iPartsGuiCodeTextField();
        codeTextField.setBeautified(false);
        switchGuiControls(mainWindow.textfieldCode, codeTextField);
        iPartsDataAssembly dataAssembly = getAssembly();
        iPartsSeriesId seriesId = null;
        if (dataAssembly != null) {
            String productGroup = "";
            iPartsProductId productId = dataAssembly.getProductIdFromModuleUsage();
            if (productId != null) {
                iPartsProduct product = iPartsProduct.getInstance(getProject(), productId);
                seriesId = product.getReferencedSeries();
                productGroup = product.getProductGroup();
            }
            codeTextField.init(getProject(), dataAssembly.getDocumentationType(), (seriesId != null) ? seriesId.getSeriesNumber() : "",
                               productGroup, "", "", null);
        }

        eventsControl = new iPartsEventsControl();
        switchGuiControls(mainWindow.panelEvents, eventsControl);
        boolean eventsVisible = (seriesId != null) && iPartsDialogSeries.getInstance(getProject(), seriesId).isEventTriggered();
        mainWindow.labelEvents.setVisible(eventsVisible);
        eventsControl.setVisible(eventsVisible);
        if (eventsVisible) {
            eventsControl.init(getProject(), seriesId);
        }

        moduleTypeCombo = EditFormComboboxHelper.replaceComboBoxByEnum(mainWindow.comboboxOrderShowType, mainWindow.panelOrderData,
                                                                       eventListener, getProject(),
                                                                       TABLE_DA_PICORDER, FIELD_DA_PO_PICTURE_TYPE,
                                                                       true);

        if (contractorForm != null) {
            contractorForm.dispose();
        }
        contractorForm = new EditASPLMContractorForm(getConnector(), this);
        AbstractGuiControl gui = contractorForm.getContractorPanelToBeAdded();
        gui.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        mainWindow.panelCustomer.addChild(gui);

        contractorForm.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                onContractorValueChange(event);
            }
        });
        contractorForm.setUserGroupLabelText("!!Gruppe", true);
    }

    private iPartsDataAssembly getAssembly() {
        EtkDataAssembly assembly = getConnector().getCurrentAssembly();
        if (assembly instanceof iPartsDataAssembly) {
            return (iPartsDataAssembly)assembly;
        }
        return null;
    }

    private void setSelectedForModuleTypeCombo(String selectedToken) {
        if ((selectedToken == null) || selectedToken.isEmpty()) {
            // Check, ob der Default-Value existiert. Falls ja, dann setzen
            EnumValue value = getProject().getEtkDbs().getEnumValue(ENUM_KEY_PICTURE_ORDER_TYPE);
            if (value != null) {
                if (value.tokenExists(iPartsTransferConst.DEFAULT_REALIZATION_VALUE)) {
                    selectedToken = iPartsTransferConst.DEFAULT_REALIZATION_VALUE;
                } else if (value.count() > 0) {
                    // Enum exitiert, aber Default-Value ist nicht enthalten -> Nimm den ersten Wert
                    selectedToken = value.values().iterator().next().getToken();
                }
            }
        }
        if (StrUtils.isValid(selectedToken)) {
            moduleTypeCombo.setActToken(selectedToken);
        }
    }

    private String getUserNameById(String userId) {
        // Zukünftiges TODO Hier den Benutzernamen zur userId bestimmen
        return userId;
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public ModalResult showModal() {
        // Falls ein Bildauftrag angelegt werden soll, aber keine gültigen Gruppen vorhanden sind, dann Warnung anzeigen
        // Falls der Bilduftrag angezeigt werden soll, soll das weiterhin möglich sein trotz nicht vorhandener Gruppen
        if (!contractorForm.isUserGroupComboBoxFilled() && contractorForm.isUserGroupMustField() && (contractorForm.getUserGroupName() == null)) {
            MessageDialog.showWarning("!!Keine für den Benutzer gültigen Gruppen gefunden. Es kann kein Bildauftrag angelegt werden!");
            return ModalResult.CANCEL;
        }
        ModalResult modalResult = mainWindow.showModal();
        super.close();
        deleteCancelledOrder();
        return modalResult;
    }

    @Override
    public void dispose() {
        if (contractorForm != null) {
            contractorForm.dispose();
        }
        super.dispose();
    }

    @Override
    public void close() {
        // Zur Sicherheit
        deleteCancelledOrder();
        mainWindow.setVisible(false);
        super.close();
    }

    /**
     * Löscht den Änderungsauftrag, wenn er in diesem Form erzeugt wurde und der Autor auf "Abbrechen" geklickt hat
     */
    private void deleteCancelledOrder() {
        // Wurde der Auftrag über den internen Button erzeugt, muss er beim "Abbrechen" auch gelöscht werden
        if ((mainWindow.getModalResult() == ModalResult.CANCEL) && newlyCreatedOrder && (dataPicOrder != null)) {
            getProject().getDbLayer().startTransaction();
            try {
                dataPicOrder.deleteFromDB();
                getProject().getDbLayer().commit();
            } catch (Exception e) {
                getProject().getDbLayer().rollback();
                Logger.getLogger().handleRuntimeException(e);
            }
            newlyCreatedOrder = false;
        }
    }

    @Override
    public EditModuleFormIConnector getConnector() {
        return (EditModuleFormIConnector)super.getConnector();
    }

    public void setTitle(String title) {
        if ((dataPicOrder != null) && (dataPicOrder.isInvalid() || dataPicOrder.isCancelled())) {
            mainWindow.title.setTitle(getTitle(false, false, dataPicOrder.isCopy()));
        } else {
            mainWindow.title.setTitle(title);
        }
    }

    public boolean isEditAllowed() {
        return isEditAllowed;
    }

    public void setEditAllowed(boolean isEdit) {
        boolean editAllowed = isEdit;
        if (dataPicOrder != null) {
            editAllowed &= dataPicOrder.isValid();
        }
        if (isEditAllowed == editAllowed) {
            return;
        }
        productDialog.setEditable(editAllowed);
        //mainWindow.textfieldName.setEnabled(editAllowed);
        llTextfieldName.setEnabled(editAllowed);
        //mainWindow.comboboxOrderShowType.setEnabled(editAllowed);
        moduleTypeCombo.setEnabled(editAllowed);
        contractorForm.setReadOnly(!editAllowed);
        mainWindow.calendarTargetDeadline.setEnabled(editAllowed);
        mainWindow.calenderInitiatorDateTime.setEnabled(editAllowed);
        mainWindow.calendarOrderDate.setEnabled(editAllowed);
        //mainWindow.textareaComment.setEnabled(editAllowed);
        llTextareaComment.setEnabled(editAllowed);
        llTextareaChangeReason.setEnabled(editAllowed);
        codeTextField.setEnabled(editAllowed);
        eventsControl.setEnabled(editAllowed);
        mainWindow.checkboxOnlyWithFIN.setEnabled(editAllowed);
        mainWindow.buttonPanel.setButtonVisible(GuiButtonOnPanel.ButtonType.CANCEL, editAllowed);
        setOKButtonText(editAllowed);
        switch (productType) {
            case EINPAS:
                einPas2Dialog.setReadOnly(!editAllowed);
                break;
            case KG_TU:
                kgtuDialog.setReadOnly(!editAllowed);
                break;
        }
        if (!editAllowed) {
            mainWindow.buttonPanel.setButtonModalResult(GuiButtonOnPanel.ButtonType.OK, ModalResult.NONE);
            mainWindow.buttonPartInfo.setText("!!Teileinformation anzeigen");
        } else {
            mainWindow.buttonPanel.setButtonModalResult(GuiButtonOnPanel.ButtonType.OK, ModalResult.OK);
            mainWindow.buttonPartInfo.setText("!!Teileinformation bearbeiten");
        }
        isEditAllowed = editAllowed;
        editAttachmentForm.setEditAllowed(editAllowed);
        doEnableButtons();
    }

    private void setOKButtonText(boolean editAllowed) {
        String okText = "!!OK";
        if (editAllowed && iPartsTransferStates.isSaveToDB_Allowed(dataPicOrder.getStatus())) {
            okText = "!!Speichern";
        }
        mainWindow.buttonPanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK).setText(okText);
    }

    public iPartsDataPicOrder getDataPicOrder() {
        return dataPicOrder;
    }

    /**
     * Callback, wenn sich die EinPasId geändert hat
     * Bestzen der notwendigen Felder
     *
     * @param event
     */
    private void onEinPASChange(Event event) {
        EinPasId einPasId = EditEinPas2Dialog.showEinPasDialog(getConnector(), this, getEinPASId());
        if (einPasId != null) {
            einPas2Dialog.setStartEinPasId(einPasId);
        }
        doEnableButtons();
    }

    private void onContractorValueChange(Event event) {
        doEnableButtons();
    }

    /**
     * Callback, wenn sich Produkt geändert hat
     * Bestzen der notwendigen Felder
     *
     * @param event
     */
    private void onProductChange(Event event) {
        doEnableButtons();
    }

    private void onChangeRequiredEvent(Event event) {
        doEnableButtons();
    }

    private void onButtonPartInfoClicked(Event event) {
        PicOrderPartlistEntriesForm dlg = new PicOrderPartlistEntriesForm(getConnector(), this, dataPicOrder);
        if (isEditAllowed) {
            dlg.setTitle("!!Teilepositionen für Bildauftrag");
        } else {
            dlg.setTitle("!!Teilepositionen für Bildauftrag anzeigen");
        }
        dlg.setEditAllowed(isEditAllowed);
        if (dlg.showModal() == ModalResult.OK) {
            // Wenn sich etwas an den Positionen verändert hat, dann muss der WorkingContext neu berechnet werden
            clearWorkingContext();
            doEnableButtons();
        }
    }

    private void onOKButtonClick(Event event) {
        if (isEditAllowed) {
            // Der eigentliche Check und der spezielle Code-Check
            if (checkData() && codeTextField.checkInputWithErrorMessage()) {
                if (iPartsTransferStates.isSaveToDB_Allowed(dataPicOrder.getStatus())) {
                    guiToData();
                    checkDeletedParts();
                    saveCurrentPicOrder();
                }
                mainWindow.setModalResult(ModalResult.OK);
                close();
            }
        } else {
            mainWindow.setModalResult(ModalResult.CANCEL);
            close();
        }
    }

    private void saveCurrentPicOrder() {
        savePicOrder(getProject(), dataPicOrder);
    }

    private void onSendButtonClick(Event event) {
        if (isEditAllowed) {
            // Der eigentliche Check und der spezielle Code-Check
            if (checkData() && codeTextField.checkInputWithErrorMessage()) {
                guiToData();
                if (!editAttachmentForm.checkAttachmentSize(true)) {
                    return;
                }

                // Bauen und verschicken der MQ Nachricht außerhalb der Transaktion
                Throwable mqException = null;
                int simNewPicOrderDelay = iPartsPlugin.getSimAutoResponseDelayForSession(iPartsPlugin.SIM_AUTO_RESPONSE_DELAY) * 1000;
                iPartsXMLMediaMessage xmlMessage = null;
                try {
                    checkDeletedParts();

                    EtkMultiSprache desc = null;
                    switch (productType) {
                        case EINPAS:
                            desc = einPas2Dialog.getTUDescription();
                            break;
                        case KG_TU:
                            desc = kgtuDialog.getTUDescription();
                            break;
                    }
                    xmlMessage = dataPicOrder.getAsMessageObject(getConnector(), productType, desc, dataPicOrder.isChangeOrCopy(), getReasonForChange());

                    if (xmlMessage == null) {
                        return;
                    }
                    // MQ Message versenden
                    iPartsMQMessageManager.getInstance(iPartsPlugin.XML_MESSAGE_MANAGER_NAME_MEDIA).sendXMLMessageWithMQ(iPartsPlugin.MQ_CHANNEL_TYPE_MEDIA,
                                                                                                                         xmlMessage, simNewPicOrderDelay >= 0);
                    for (iPartsDataPicOrderPart picOrderPart : dataPicOrder.getParts()) {
                        picOrderPart.setFieldValueAsBoolean(iPartsConst.FIELD_DA_PPA_SENT, true, DBActionOrigin.FROM_EDIT);
                    }

                    // Status erst nach erfolgreichem Senden auf "versorgt"/"Kopierauftrag verschickt"/"Änderungsauftrag verschickt" setzen
                    if (dataPicOrder.isChangeOrder()) {
                        dataPicOrder.setStatus(iPartsTransferStates.CHANGE_REQUESTED, DBActionOrigin.FROM_EDIT);
                    } else if (dataPicOrder.isCopy()) {
                        dataPicOrder.setStatus(iPartsTransferStates.COPY_REQUESTED, DBActionOrigin.FROM_EDIT);
                    } else {
                        dataPicOrder.setStatus(iPartsTransferStates.VERSORGT, DBActionOrigin.FROM_EDIT);
                    }
                } catch (Throwable mqE) {
                    // Alle Fehler beim Senden von MQ abfangen. Falls hier etwas nicht geht, so soll der Bildauftrag
                    // mit dem Status "angelegt" gespeichert werden.
                    // Falls hier etwas schief gegangen ist, dann darf der Bildauftrag auf keinen Fall mit dem Status
                    // "versorgt" gespeichert werden.
                    mqException = mqE;
                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, mqE);
                }

                saveCurrentPicOrder();

                if (mqException == null) {
                    mainWindow.setModalResult(ModalResult.OK);
                    close();
                    // Erwartete Bildauftrags-Antwort zu Simulationszwecken erzeugen und versenden
                    boolean simNewPicOrderXml = iPartsEditPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsEditPlugin.CONFIG_SIM_NEW_PIC_ORDER_XML);
                    if (simNewPicOrderXml || (simNewPicOrderDelay >= 0)) {
                        iPartsTransferNodeTypes operationType = xmlMessage.getRequestOperationType();
                        iPartsXMLMediaMessage expectedResponseXmlMessage;
                        if (operationType == iPartsTransferNodeTypes.CHANGE_MEDIA_ORDER) {
                            expectedResponseXmlMessage = iPartsEditXMLResponseSimulator.createChangeMediaOrderResponse(getProject(), xmlMessage, dataPicOrder);
                        } else if (operationType == iPartsTransferNodeTypes.UPDATE_MEDIA_ORDER) {
                            expectedResponseXmlMessage = iPartsEditXMLResponseSimulator.createUpdateMediaOrderResponse(xmlMessage);
                        } else {
                            expectedResponseXmlMessage = iPartsEditXMLResponseSimulator.createMediaOrderResponse(xmlMessage);
                        }
                        iPartsEditXMLResponseSimulator.writeAndSendSimulatedMessageResponseFromXML(iPartsPlugin.MQ_CHANNEL_TYPE_MEDIA,
                                                                                                   expectedResponseXmlMessage,
                                                                                                   simNewPicOrderXml,
                                                                                                   simNewPicOrderDelay);
                    }
                } else {
                    //erst jetzt die Meldung, damit die Transaktion nicht so lange offen bleibt, bis die Meldung abgenickt wurde.
                    MessageDialog.showError(mqException.getMessage());
                }
            }
        }
    }

    /**
     * Überprüft, ob am Bildauftrag Bildpositionen hängen, die noch nicht versendet wurden und nicht mehr in der
     * Stückliste existieren. Diese Bildpositionen müssen vom Bildauftrag gelöst werden.
     */
    private void checkDeletedParts() {
        // Alle Bildpositionen zusammensuchen, die am Bildauftrag hängen und noch nicht verschickt wurden
        Map<PartListEntryId, iPartsDataPicOrderPart> partsYetNotSent = new HashMap<>();
        for (iPartsDataPicOrderPart picOrderPart : dataPicOrder.getParts()) {
            if (!picOrderPart.isSent()) {
                partsYetNotSent.put(picOrderPart.getAsId().getPartListEntryId(), picOrderPart);
            }
        }
        if (!partsYetNotSent.isEmpty()) {
            // Stückliste durchlaufen und schauen, ob die Bildposition überhaupt noch existiert
            for (EtkDataPartListEntry partListEntry : getConnector().getCurrentPartListEntries()) {
                partsYetNotSent.remove(partListEntry.getAsId());
                if (partsYetNotSent.isEmpty()) {
                    break;
                }
            }
            // Falls nicht, löschen
            if (!partsYetNotSent.isEmpty()) {
                for (iPartsDataPicOrderPart picOrderPart : partsYetNotSent.values()) {
                    dataPicOrder.getParts().delete(picOrderPart, DBActionOrigin.FROM_EDIT);
                }
            }
        }
    }

    public String getReasonForChange() {
        return llTextareaChangeReason.getText();
    }

    private void onViewButtonClick(Event event) {
        if (isViewMediaContentVisible()) {
            // Bei einem Änderungsauftrag/Kopierauftrag werden die Bilder nur selektiert. Bei einer Korrektur müssen weitere Angaben
            // gemacht werden
            if ((dataPicOrder.isChangeOrder() && (dataPicOrder.getStatus() == iPartsTransferStates.CHANGE_CREATED))
                || (dataPicOrder.isCopy() && (dataPicOrder.getStatus() == iPartsTransferStates.COPY_CREATED))) {
                if (PicOrderMediaContentForm.showASPLMMediaContentsForChange(getConnector(), this, getConnector().getCurrentAssembly().getAsId(),
                                                                             dataPicOrder)) {
                    saveCurrentPicOrder();
                }
            } else {
                if (PicOrderMediaContentForm.showASPLMMediaContents(getConnector(), this, getConnector().getCurrentAssembly().getAsId(),
                                                                    dataPicOrder)) {
                    event.addParameter(EditAssemblyImageForm.RELOAD_PICTURES, true);
                }
            }
            doEnableButtons();
            if (listenerForPictures != null) {
                listenerForPictures.fire(event);
            }
            setStatus();
            changeGroupAndUser();
        }
    }

    public void setListenerForPictures(EventListener listenerForPictures) {
        this.listenerForPictures = listenerForPictures;
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
        private de.docware.framework.modules.gui.controls.GuiPanel panelMainNew;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTabbedPane tabbedpanePicOrder;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry tabbedpaneentryPicOrder;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane scrollpane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMainSub;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelOrderNo;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelOrderTyp;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelOrderType;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<Object> comboboxOrderType;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelOrderState;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfieldOrderState;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelOrderMessage;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextArea textareaOrderMessage;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelOrderProduct;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelEinPAS;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelKGTU;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelOrderData;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelName;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfieldName;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelOrderShowType;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<Object> comboboxOrderShowType;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelCustomer;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelCustomer;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelReason;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane scrollpaneReason;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextArea textareaReason;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelOrderDate;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.calendar.GuiCalendar calendarOrderDate;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelTargetDeadline;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.calendar.GuiCalendar calendarTargetDeadline;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelComment;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane scrollpaneComment;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextArea textareaComment;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelCode;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfieldCode;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelWorkingContext;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfieldWorkingContext;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelEvents;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelEvents;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelOnlyWithFIN;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiCheckbox checkboxOnlyWithFIN;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelOrderInitiator;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelInitiator;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelInitiator;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelInitiatorGroup;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfieldInitiatorGroup;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelInitiatorDate;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.formattedfields.GuiDateTimeEditPanel calenderInitiatorDateTime;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelButton;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton buttonPartInfo;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelCantBeSended;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry tabbedpaneentryPicAttachments;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelAttachments;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setHeight(950);
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
            panelMainNew = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelMainNew.setName("panelMainNew");
            panelMainNew.__internal_setGenerationDpi(96);
            panelMainNew.registerTranslationHandler(translationHandler);
            panelMainNew.setScaleForResolution(true);
            panelMainNew.setMinimumWidth(10);
            panelMainNew.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainNewLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMainNew.setLayout(panelMainNewLayout);
            tabbedpanePicOrder = new de.docware.framework.modules.gui.controls.GuiTabbedPane();
            tabbedpanePicOrder.setName("tabbedpanePicOrder");
            tabbedpanePicOrder.__internal_setGenerationDpi(96);
            tabbedpanePicOrder.registerTranslationHandler(translationHandler);
            tabbedpanePicOrder.setScaleForResolution(true);
            tabbedpanePicOrder.setMinimumWidth(10);
            tabbedpanePicOrder.setMinimumHeight(10);
            tabbedpaneentryPicOrder = new de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry();
            tabbedpaneentryPicOrder.setName("tabbedpaneentryPicOrder");
            tabbedpaneentryPicOrder.__internal_setGenerationDpi(96);
            tabbedpaneentryPicOrder.registerTranslationHandler(translationHandler);
            tabbedpaneentryPicOrder.setScaleForResolution(true);
            tabbedpaneentryPicOrder.setMinimumWidth(10);
            tabbedpaneentryPicOrder.setMinimumHeight(10);
            tabbedpaneentryPicOrder.setTitle("!!Bildauftrag");
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
            scrollpane = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            scrollpane.setName("scrollpane");
            scrollpane.__internal_setGenerationDpi(96);
            scrollpane.registerTranslationHandler(translationHandler);
            scrollpane.setScaleForResolution(true);
            scrollpane.setMinimumWidth(10);
            scrollpane.setMinimumHeight(10);
            panelMainSub = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelMainSub.setName("panelMainSub");
            panelMainSub.__internal_setGenerationDpi(96);
            panelMainSub.registerTranslationHandler(translationHandler);
            panelMainSub.setScaleForResolution(true);
            panelMainSub.setMinimumWidth(10);
            panelMainSub.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelMainSubLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelMainSubLayout.setCentered(false);
            panelMainSub.setLayout(panelMainSubLayout);
            panelOrderNo = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelOrderNo.setName("panelOrderNo");
            panelOrderNo.__internal_setGenerationDpi(96);
            panelOrderNo.registerTranslationHandler(translationHandler);
            panelOrderNo.setScaleForResolution(true);
            panelOrderNo.setMinimumWidth(10);
            panelOrderNo.setMinimumHeight(10);
            panelOrderNo.setTitle("!!AS-PLM Auftragsnummer");
            de.docware.framework.modules.gui.layout.LayoutGridBag panelOrderNoLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelOrderNo.setLayout(panelOrderNoLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelOrderNoConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "c", "h", 4, 8, 4, 8);
            panelOrderNo.setConstraints(panelOrderNoConstraints);
            panelMainSub.addChild(panelOrderNo);
            panelOrderTyp = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelOrderTyp.setName("panelOrderTyp");
            panelOrderTyp.__internal_setGenerationDpi(96);
            panelOrderTyp.registerTranslationHandler(translationHandler);
            panelOrderTyp.setScaleForResolution(true);
            panelOrderTyp.setMinimumWidth(10);
            panelOrderTyp.setMinimumHeight(10);
            panelOrderTyp.setTitle("!!Auftragstyp und Status");
            de.docware.framework.modules.gui.layout.LayoutGridBag panelOrderTypLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelOrderTyp.setLayout(panelOrderTypLayout);
            labelOrderType = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelOrderType.setName("labelOrderType");
            labelOrderType.__internal_setGenerationDpi(96);
            labelOrderType.registerTranslationHandler(translationHandler);
            labelOrderType.setScaleForResolution(true);
            labelOrderType.setMinimumWidth(80);
            labelOrderType.setMinimumHeight(10);
            labelOrderType.setText("!!Auftragstyp");
            labelOrderType.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelOrderTypeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "e", "n", 0, 4, 4, 4);
            labelOrderType.setConstraints(labelOrderTypeConstraints);
            panelOrderTyp.addChild(labelOrderType);
            comboboxOrderType = new de.docware.framework.modules.gui.controls.GuiComboBox<Object>();
            comboboxOrderType.setName("comboboxOrderType");
            comboboxOrderType.__internal_setGenerationDpi(96);
            comboboxOrderType.registerTranslationHandler(translationHandler);
            comboboxOrderType.setScaleForResolution(true);
            comboboxOrderType.setMinimumWidth(10);
            comboboxOrderType.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag comboboxOrderTypeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 100.0, 0.0, "c", "h", 0, 4, 4, 4);
            comboboxOrderType.setConstraints(comboboxOrderTypeConstraints);
            panelOrderTyp.addChild(comboboxOrderType);
            labelOrderState = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelOrderState.setName("labelOrderState");
            labelOrderState.__internal_setGenerationDpi(96);
            labelOrderState.registerTranslationHandler(translationHandler);
            labelOrderState.setScaleForResolution(true);
            labelOrderState.setMinimumWidth(80);
            labelOrderState.setMinimumHeight(10);
            labelOrderState.setText("!!Status");
            labelOrderState.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelOrderStateConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "e", "n", 4, 4, 4, 4);
            labelOrderState.setConstraints(labelOrderStateConstraints);
            panelOrderTyp.addChild(labelOrderState);
            textfieldOrderState = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfieldOrderState.setName("textfieldOrderState");
            textfieldOrderState.__internal_setGenerationDpi(96);
            textfieldOrderState.registerTranslationHandler(translationHandler);
            textfieldOrderState.setScaleForResolution(true);
            textfieldOrderState.setMinimumWidth(200);
            textfieldOrderState.setMinimumHeight(10);
            textfieldOrderState.setBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignBackground"));
            textfieldOrderState.setEditable(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfieldOrderStateConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 1, 1, 1, 100.0, 0.0, "c", "h", 4, 4, 4, 8);
            textfieldOrderState.setConstraints(textfieldOrderStateConstraints);
            panelOrderTyp.addChild(textfieldOrderState);
            labelOrderMessage = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelOrderMessage.setName("labelOrderMessage");
            labelOrderMessage.__internal_setGenerationDpi(96);
            labelOrderMessage.registerTranslationHandler(translationHandler);
            labelOrderMessage.setScaleForResolution(true);
            labelOrderMessage.setMinimumWidth(80);
            labelOrderMessage.setMinimumHeight(10);
            labelOrderMessage.setText("!!Fehlermeldung");
            labelOrderMessage.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelOrderMessageConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 0.0, 0.0, "ne", "n", 4, 4, 0, 4);
            labelOrderMessage.setConstraints(labelOrderMessageConstraints);
            panelOrderTyp.addChild(labelOrderMessage);
            textareaOrderMessage = new de.docware.framework.modules.gui.controls.GuiTextArea();
            textareaOrderMessage.setName("textareaOrderMessage");
            textareaOrderMessage.__internal_setGenerationDpi(96);
            textareaOrderMessage.registerTranslationHandler(translationHandler);
            textareaOrderMessage.setScaleForResolution(true);
            textareaOrderMessage.setMinimumWidth(200);
            textareaOrderMessage.setMinimumHeight(10);
            textareaOrderMessage.setBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignBackground"));
            textareaOrderMessage.setPaddingTop(1);
            textareaOrderMessage.setPaddingLeft(1);
            textareaOrderMessage.setPaddingRight(1);
            textareaOrderMessage.setPaddingBottom(1);
            textareaOrderMessage.setEditable(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textareaOrderMessageConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 2, 1, 1, 100.0, 0.0, "c", "h", 4, 4, 8, 8);
            textareaOrderMessage.setConstraints(textareaOrderMessageConstraints);
            panelOrderTyp.addChild(textareaOrderMessage);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelOrderTypConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 100.0, 0.0, "c", "h", 4, 8, 4, 8);
            panelOrderTyp.setConstraints(panelOrderTypConstraints);
            panelMainSub.addChild(panelOrderTyp);
            panelOrderProduct = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelOrderProduct.setName("panelOrderProduct");
            panelOrderProduct.__internal_setGenerationDpi(96);
            panelOrderProduct.registerTranslationHandler(translationHandler);
            panelOrderProduct.setScaleForResolution(true);
            panelOrderProduct.setMinimumWidth(10);
            panelOrderProduct.setMinimumHeight(10);
            panelOrderProduct.setTitle("!!Produkt");
            de.docware.framework.modules.gui.layout.LayoutGridBag panelOrderProductLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelOrderProduct.setLayout(panelOrderProductLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelOrderProductConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 100.0, 0.0, "c", "h", 4, 8, 4, 8);
            panelOrderProduct.setConstraints(panelOrderProductConstraints);
            panelMainSub.addChild(panelOrderProduct);
            panelEinPAS = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelEinPAS.setName("panelEinPAS");
            panelEinPAS.__internal_setGenerationDpi(96);
            panelEinPAS.registerTranslationHandler(translationHandler);
            panelEinPAS.setScaleForResolution(true);
            panelEinPAS.setMinimumWidth(10);
            panelEinPAS.setMinimumHeight(120);
            panelEinPAS.setPaddingTop(4);
            panelEinPAS.setPaddingLeft(8);
            panelEinPAS.setPaddingRight(8);
            panelEinPAS.setPaddingBottom(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panelEinPASLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelEinPAS.setLayout(panelEinPASLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelEinPASConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 3, 1, 1, 100.0, 0.0, "c", "h", 0, 0, 0, 0);
            panelEinPAS.setConstraints(panelEinPASConstraints);
            panelMainSub.addChild(panelEinPAS);
            panelKGTU = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelKGTU.setName("panelKGTU");
            panelKGTU.__internal_setGenerationDpi(96);
            panelKGTU.registerTranslationHandler(translationHandler);
            panelKGTU.setScaleForResolution(true);
            panelKGTU.setMinimumWidth(10);
            panelKGTU.setMinimumHeight(100);
            panelKGTU.setPaddingTop(4);
            panelKGTU.setPaddingLeft(8);
            panelKGTU.setPaddingRight(8);
            panelKGTU.setPaddingBottom(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panelKGTULayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelKGTU.setLayout(panelKGTULayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelKGTUConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 4, 1, 1, 100.0, 0.0, "c", "h", 0, 0, 0, 0);
            panelKGTU.setConstraints(panelKGTUConstraints);
            panelMainSub.addChild(panelKGTU);
            panelOrderData = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelOrderData.setName("panelOrderData");
            panelOrderData.__internal_setGenerationDpi(96);
            panelOrderData.registerTranslationHandler(translationHandler);
            panelOrderData.setScaleForResolution(true);
            panelOrderData.setMinimumWidth(10);
            panelOrderData.setMinimumHeight(10);
            panelOrderData.setTitle("!!Bildauftrag Daten");
            de.docware.framework.modules.gui.layout.LayoutGridBag panelOrderDataLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelOrderData.setLayout(panelOrderDataLayout);
            labelName = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelName.setName("labelName");
            labelName.__internal_setGenerationDpi(96);
            labelName.registerTranslationHandler(translationHandler);
            labelName.setScaleForResolution(true);
            labelName.setMinimumWidth(10);
            labelName.setMinimumHeight(10);
            labelName.setFontStyle(de.docware.framework.modules.gui.controls.misc.DWFontStyle.BOLD);
            labelName.setText("!!Benennung");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelNameConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "e", "n", 0, 4, 4, 4);
            labelName.setConstraints(labelNameConstraints);
            panelOrderData.addChild(labelName);
            textfieldName = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfieldName.setName("textfieldName");
            textfieldName.__internal_setGenerationDpi(96);
            textfieldName.registerTranslationHandler(translationHandler);
            textfieldName.setScaleForResolution(true);
            textfieldName.setMinimumWidth(200);
            textfieldName.setMinimumHeight(10);
            textfieldName.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onChangeRequiredEvent(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfieldNameConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 100.0, 0.0, "c", "h", 0, 4, 4, 8);
            textfieldName.setConstraints(textfieldNameConstraints);
            panelOrderData.addChild(textfieldName);
            labelOrderShowType = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelOrderShowType.setName("labelOrderShowType");
            labelOrderShowType.__internal_setGenerationDpi(96);
            labelOrderShowType.registerTranslationHandler(translationHandler);
            labelOrderShowType.setScaleForResolution(true);
            labelOrderShowType.setMinimumWidth(10);
            labelOrderShowType.setMinimumHeight(10);
            labelOrderShowType.setFontStyle(de.docware.framework.modules.gui.controls.misc.DWFontStyle.BOLD);
            labelOrderShowType.setText("!!Darstellungsart");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelOrderShowTypeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "e", "n", 4, 4, 4, 4);
            labelOrderShowType.setConstraints(labelOrderShowTypeConstraints);
            panelOrderData.addChild(labelOrderShowType);
            comboboxOrderShowType = new de.docware.framework.modules.gui.controls.GuiComboBox<Object>();
            comboboxOrderShowType.setName("comboboxOrderShowType");
            comboboxOrderShowType.__internal_setGenerationDpi(96);
            comboboxOrderShowType.registerTranslationHandler(translationHandler);
            comboboxOrderShowType.setScaleForResolution(true);
            comboboxOrderShowType.setMinimumWidth(10);
            comboboxOrderShowType.setMinimumHeight(10);
            comboboxOrderShowType.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onChangeRequiredEvent(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag comboboxOrderShowTypeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 1, 1, 1, 100.0, 0.0, "c", "h", 4, 4, 4, 8);
            comboboxOrderShowType.setConstraints(comboboxOrderShowTypeConstraints);
            panelOrderData.addChild(comboboxOrderShowType);
            labelCustomer = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelCustomer.setName("labelCustomer");
            labelCustomer.__internal_setGenerationDpi(96);
            labelCustomer.registerTranslationHandler(translationHandler);
            labelCustomer.setScaleForResolution(true);
            labelCustomer.setMinimumWidth(10);
            labelCustomer.setMinimumHeight(10);
            labelCustomer.setText("!!Auftragnehmer");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelCustomerConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 0.0, 0.0, "e", "n", 4, 4, 4, 4);
            labelCustomer.setConstraints(labelCustomerConstraints);
            panelOrderData.addChild(labelCustomer);
            panelCustomer = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelCustomer.setName("panelCustomer");
            panelCustomer.__internal_setGenerationDpi(96);
            panelCustomer.registerTranslationHandler(translationHandler);
            panelCustomer.setScaleForResolution(true);
            panelCustomer.setMinimumWidth(10);
            panelCustomer.setMinimumHeight(20);
            de.docware.framework.modules.gui.layout.LayoutBorder panelCustomerLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelCustomer.setLayout(panelCustomerLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelCustomerConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 2, 1, 1, 100.0, 0.0, "c", "b", 4, 4, 4, 4);
            panelCustomer.setConstraints(panelCustomerConstraints);
            panelOrderData.addChild(panelCustomer);
            labelReason = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelReason.setName("labelReason");
            labelReason.__internal_setGenerationDpi(96);
            labelReason.registerTranslationHandler(translationHandler);
            labelReason.setScaleForResolution(true);
            labelReason.setMinimumWidth(10);
            labelReason.setMinimumHeight(10);
            labelReason.setVisible(false);
            labelReason.setFontStyle(de.docware.framework.modules.gui.controls.misc.DWFontStyle.BOLD);
            labelReason.setText("!!Grund für Änderung");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelReasonConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 3, 1, 1, 0.0, 0.0, "ne", "n", 4, 4, 4, 4);
            labelReason.setConstraints(labelReasonConstraints);
            panelOrderData.addChild(labelReason);
            scrollpaneReason = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            scrollpaneReason.setName("scrollpaneReason");
            scrollpaneReason.__internal_setGenerationDpi(96);
            scrollpaneReason.registerTranslationHandler(translationHandler);
            scrollpaneReason.setScaleForResolution(true);
            scrollpaneReason.setMinimumWidth(10);
            scrollpaneReason.setMinimumHeight(60);
            scrollpaneReason.setVisible(false);
            scrollpaneReason.setBorderWidth(1);
            scrollpaneReason.setBorderColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignTextFieldBorder"));
            textareaReason = new de.docware.framework.modules.gui.controls.GuiTextArea();
            textareaReason.setName("textareaReason");
            textareaReason.__internal_setGenerationDpi(96);
            textareaReason.registerTranslationHandler(translationHandler);
            textareaReason.setScaleForResolution(true);
            textareaReason.setMinimumWidth(100);
            textareaReason.setMinimumHeight(60);
            textareaReason.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onChangeRequiredEvent(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder textareaReasonConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            textareaReason.setConstraints(textareaReasonConstraints);
            scrollpaneReason.addChild(textareaReason);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag scrollpaneReasonConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 3, 1, 1, 100.0, 0.0, "c", "h", 4, 4, 4, 8);
            scrollpaneReason.setConstraints(scrollpaneReasonConstraints);
            panelOrderData.addChild(scrollpaneReason);
            labelOrderDate = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelOrderDate.setName("labelOrderDate");
            labelOrderDate.__internal_setGenerationDpi(96);
            labelOrderDate.registerTranslationHandler(translationHandler);
            labelOrderDate.setScaleForResolution(true);
            labelOrderDate.setMinimumWidth(10);
            labelOrderDate.setMinimumHeight(10);
            labelOrderDate.setText("!!Beauftragungsdatum");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelOrderDateConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 4, 1, 1, 0.0, 0.0, "e", "n", 4, 4, 4, 4);
            labelOrderDate.setConstraints(labelOrderDateConstraints);
            panelOrderData.addChild(labelOrderDate);
            calendarOrderDate = new de.docware.framework.modules.gui.controls.calendar.GuiCalendar();
            calendarOrderDate.setName("calendarOrderDate");
            calendarOrderDate.__internal_setGenerationDpi(96);
            calendarOrderDate.registerTranslationHandler(translationHandler);
            calendarOrderDate.setScaleForResolution(true);
            calendarOrderDate.setMinimumWidth(10);
            calendarOrderDate.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag calendarOrderDateConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 4, 1, 1, 100.0, 0.0, "c", "h", 4, 4, 4, 8);
            calendarOrderDate.setConstraints(calendarOrderDateConstraints);
            panelOrderData.addChild(calendarOrderDate);
            labelTargetDeadline = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelTargetDeadline.setName("labelTargetDeadline");
            labelTargetDeadline.__internal_setGenerationDpi(96);
            labelTargetDeadline.registerTranslationHandler(translationHandler);
            labelTargetDeadline.setScaleForResolution(true);
            labelTargetDeadline.setMinimumWidth(10);
            labelTargetDeadline.setMinimumHeight(10);
            labelTargetDeadline.setText("!!Gew. Fertigstellungstermin");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelTargetDeadlineConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 5, 1, 1, 0.0, 0.0, "e", "n", 4, 4, 4, 4);
            labelTargetDeadline.setConstraints(labelTargetDeadlineConstraints);
            panelOrderData.addChild(labelTargetDeadline);
            calendarTargetDeadline = new de.docware.framework.modules.gui.controls.calendar.GuiCalendar();
            calendarTargetDeadline.setName("calendarTargetDeadline");
            calendarTargetDeadline.__internal_setGenerationDpi(96);
            calendarTargetDeadline.registerTranslationHandler(translationHandler);
            calendarTargetDeadline.setScaleForResolution(true);
            calendarTargetDeadline.setMinimumWidth(10);
            calendarTargetDeadline.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag calendarTargetDeadlineConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 5, 1, 1, 100.0, 0.0, "c", "h", 4, 4, 4, 8);
            calendarTargetDeadline.setConstraints(calendarTargetDeadlineConstraints);
            panelOrderData.addChild(calendarTargetDeadline);
            labelComment = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelComment.setName("labelComment");
            labelComment.__internal_setGenerationDpi(96);
            labelComment.registerTranslationHandler(translationHandler);
            labelComment.setScaleForResolution(true);
            labelComment.setMinimumWidth(10);
            labelComment.setMinimumHeight(10);
            labelComment.setText("!!Bemerkung");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelCommentConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 6, 1, 1, 0.0, 0.0, "ne", "n", 4, 4, 4, 4);
            labelComment.setConstraints(labelCommentConstraints);
            panelOrderData.addChild(labelComment);
            scrollpaneComment = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            scrollpaneComment.setName("scrollpaneComment");
            scrollpaneComment.__internal_setGenerationDpi(96);
            scrollpaneComment.registerTranslationHandler(translationHandler);
            scrollpaneComment.setScaleForResolution(true);
            scrollpaneComment.setMinimumWidth(10);
            scrollpaneComment.setMinimumHeight(60);
            scrollpaneComment.setBorderWidth(1);
            scrollpaneComment.setBorderColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignTextFieldBorder"));
            textareaComment = new de.docware.framework.modules.gui.controls.GuiTextArea();
            textareaComment.setName("textareaComment");
            textareaComment.__internal_setGenerationDpi(96);
            textareaComment.registerTranslationHandler(translationHandler);
            textareaComment.setScaleForResolution(true);
            textareaComment.setMinimumWidth(100);
            textareaComment.setMinimumHeight(60);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder textareaCommentConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            textareaComment.setConstraints(textareaCommentConstraints);
            scrollpaneComment.addChild(textareaComment);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag scrollpaneCommentConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 6, 1, 1, 100.0, 0.0, "c", "h", 4, 4, 4, 8);
            scrollpaneComment.setConstraints(scrollpaneCommentConstraints);
            panelOrderData.addChild(scrollpaneComment);
            labelCode = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelCode.setName("labelCode");
            labelCode.__internal_setGenerationDpi(96);
            labelCode.registerTranslationHandler(translationHandler);
            labelCode.setScaleForResolution(true);
            labelCode.setMinimumWidth(10);
            labelCode.setMinimumHeight(10);
            labelCode.setName("labelCode");
            labelCode.setText("!!Code-Gültigkeit");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelCodeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 7, 1, 1, 0.0, 0.0, "e", "n", 4, 4, 4, 4);
            labelCode.setConstraints(labelCodeConstraints);
            panelOrderData.addChild(labelCode);
            textfieldCode = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfieldCode.setName("textfieldCode");
            textfieldCode.__internal_setGenerationDpi(96);
            textfieldCode.registerTranslationHandler(translationHandler);
            textfieldCode.setScaleForResolution(true);
            textfieldCode.setMinimumWidth(200);
            textfieldCode.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfieldCodeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 7, 1, 1, 100.0, 0.0, "c", "h", 4, 4, 4, 8);
            textfieldCode.setConstraints(textfieldCodeConstraints);
            panelOrderData.addChild(textfieldCode);
            labelWorkingContext = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelWorkingContext.setName("labelWorkingContext");
            labelWorkingContext.__internal_setGenerationDpi(96);
            labelWorkingContext.registerTranslationHandler(translationHandler);
            labelWorkingContext.setScaleForResolution(true);
            labelWorkingContext.setMinimumWidth(10);
            labelWorkingContext.setMinimumHeight(10);
            labelWorkingContext.setVisible(false);
            labelWorkingContext.setText("!!Bearbeitungskontext");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelWorkingContextConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 8, 1, 1, 0.0, 0.0, "e", "n", 4, 4, 4, 4);
            labelWorkingContext.setConstraints(labelWorkingContextConstraints);
            panelOrderData.addChild(labelWorkingContext);
            textfieldWorkingContext = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfieldWorkingContext.setName("textfieldWorkingContext");
            textfieldWorkingContext.__internal_setGenerationDpi(96);
            textfieldWorkingContext.registerTranslationHandler(translationHandler);
            textfieldWorkingContext.setScaleForResolution(true);
            textfieldWorkingContext.setMinimumWidth(200);
            textfieldWorkingContext.setMinimumHeight(10);
            textfieldWorkingContext.setVisible(false);
            textfieldWorkingContext.setEditable(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfieldWorkingContextConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 8, 1, 1, 100.0, 0.0, "c", "h", 4, 4, 4, 8);
            textfieldWorkingContext.setConstraints(textfieldWorkingContextConstraints);
            panelOrderData.addChild(textfieldWorkingContext);
            labelEvents = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelEvents.setName("labelEvents");
            labelEvents.__internal_setGenerationDpi(96);
            labelEvents.registerTranslationHandler(translationHandler);
            labelEvents.setScaleForResolution(true);
            labelEvents.setMinimumWidth(10);
            labelEvents.setMinimumHeight(10);
            labelEvents.setName("labelEvents");
            labelEvents.setText("!!Ereignissteuerung");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelEventsConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 9, 1, 1, 0.0, 0.0, "e", "n", 4, 4, 4, 4);
            labelEvents.setConstraints(labelEventsConstraints);
            panelOrderData.addChild(labelEvents);
            panelEvents = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelEvents.setName("panelEvents");
            panelEvents.__internal_setGenerationDpi(96);
            panelEvents.registerTranslationHandler(translationHandler);
            panelEvents.setScaleForResolution(true);
            panelEvents.setMinimumWidth(10);
            panelEvents.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelEventsLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelEvents.setLayout(panelEventsLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelEventsConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 9, 1, 1, 100.0, 0.0, "c", "b", 4, 4, 4, 8);
            panelEvents.setConstraints(panelEventsConstraints);
            panelOrderData.addChild(panelEvents);
            labelOnlyWithFIN = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelOnlyWithFIN.setName("labelOnlyWithFIN");
            labelOnlyWithFIN.__internal_setGenerationDpi(96);
            labelOnlyWithFIN.registerTranslationHandler(translationHandler);
            labelOnlyWithFIN.setScaleForResolution(true);
            labelOnlyWithFIN.setMinimumWidth(10);
            labelOnlyWithFIN.setMinimumHeight(10);
            labelOnlyWithFIN.setText("!!Nur bei FIN ausgeben");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelOnlyWithFINConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 10, 1, 1, 0.0, 0.0, "e", "n", 4, 4, 8, 4);
            labelOnlyWithFIN.setConstraints(labelOnlyWithFINConstraints);
            panelOrderData.addChild(labelOnlyWithFIN);
            checkboxOnlyWithFIN = new de.docware.framework.modules.gui.controls.GuiCheckbox();
            checkboxOnlyWithFIN.setName("checkboxOnlyWithFIN");
            checkboxOnlyWithFIN.__internal_setGenerationDpi(96);
            checkboxOnlyWithFIN.registerTranslationHandler(translationHandler);
            checkboxOnlyWithFIN.setScaleForResolution(true);
            checkboxOnlyWithFIN.setMinimumWidth(10);
            checkboxOnlyWithFIN.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag checkboxOnlyWithFINConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 10, 1, 1, 0.0, 0.0, "w", "h", 4, 4, 8, 8);
            checkboxOnlyWithFIN.setConstraints(checkboxOnlyWithFINConstraints);
            panelOrderData.addChild(checkboxOnlyWithFIN);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelOrderDataConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 5, 1, 1, 100.0, 0.0, "c", "h", 4, 8, 4, 8);
            panelOrderData.setConstraints(panelOrderDataConstraints);
            panelMainSub.addChild(panelOrderData);
            panelOrderInitiator = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelOrderInitiator.setName("panelOrderInitiator");
            panelOrderInitiator.__internal_setGenerationDpi(96);
            panelOrderInitiator.registerTranslationHandler(translationHandler);
            panelOrderInitiator.setScaleForResolution(true);
            panelOrderInitiator.setMinimumWidth(10);
            panelOrderInitiator.setMinimumHeight(10);
            panelOrderInitiator.setTitle("!!Auftraggeber Daten");
            de.docware.framework.modules.gui.layout.LayoutGridBag panelOrderInitiatorLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelOrderInitiator.setLayout(panelOrderInitiatorLayout);
            labelInitiator = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelInitiator.setName("labelInitiator");
            labelInitiator.__internal_setGenerationDpi(96);
            labelInitiator.registerTranslationHandler(translationHandler);
            labelInitiator.setScaleForResolution(true);
            labelInitiator.setMinimumWidth(10);
            labelInitiator.setMinimumHeight(10);
            labelInitiator.setText("!!Auftraggeber");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelInitiatorConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "e", "n", 4, 4, 4, 4);
            labelInitiator.setConstraints(labelInitiatorConstraints);
            panelOrderInitiator.addChild(labelInitiator);
            panelInitiator = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelInitiator.setName("panelInitiator");
            panelInitiator.__internal_setGenerationDpi(96);
            panelInitiator.registerTranslationHandler(translationHandler);
            panelInitiator.setScaleForResolution(true);
            panelInitiator.setMinimumWidth(10);
            panelInitiator.setMinimumHeight(20);
            de.docware.framework.modules.gui.layout.LayoutBorder panelInitiatorLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelInitiator.setLayout(panelInitiatorLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelInitiatorConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 100.0, 0.0, "c", "b", 4, 4, 4, 4);
            panelInitiator.setConstraints(panelInitiatorConstraints);
            panelOrderInitiator.addChild(panelInitiator);
            labelInitiatorGroup = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelInitiatorGroup.setName("labelInitiatorGroup");
            labelInitiatorGroup.__internal_setGenerationDpi(96);
            labelInitiatorGroup.registerTranslationHandler(translationHandler);
            labelInitiatorGroup.setScaleForResolution(true);
            labelInitiatorGroup.setMinimumWidth(10);
            labelInitiatorGroup.setMinimumHeight(10);
            labelInitiatorGroup.setText("!!Auftragsgruppe");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelInitiatorGroupConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "e", "n", 4, 4, 4, 4);
            labelInitiatorGroup.setConstraints(labelInitiatorGroupConstraints);
            panelOrderInitiator.addChild(labelInitiatorGroup);
            textfieldInitiatorGroup = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfieldInitiatorGroup.setName("textfieldInitiatorGroup");
            textfieldInitiatorGroup.__internal_setGenerationDpi(96);
            textfieldInitiatorGroup.registerTranslationHandler(translationHandler);
            textfieldInitiatorGroup.setScaleForResolution(true);
            textfieldInitiatorGroup.setMinimumWidth(200);
            textfieldInitiatorGroup.setMinimumHeight(10);
            textfieldInitiatorGroup.setBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignBackground"));
            textfieldInitiatorGroup.setEditable(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfieldInitiatorGroupConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 1, 1, 1, 100.0, 0.0, "c", "h", 4, 4, 4, 8);
            textfieldInitiatorGroup.setConstraints(textfieldInitiatorGroupConstraints);
            panelOrderInitiator.addChild(textfieldInitiatorGroup);
            labelInitiatorDate = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelInitiatorDate.setName("labelInitiatorDate");
            labelInitiatorDate.__internal_setGenerationDpi(96);
            labelInitiatorDate.registerTranslationHandler(translationHandler);
            labelInitiatorDate.setScaleForResolution(true);
            labelInitiatorDate.setMinimumWidth(10);
            labelInitiatorDate.setMinimumHeight(10);
            labelInitiatorDate.setText("!!Auftragsanlagedatum");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelInitiatorDateConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 0.0, 0.0, "e", "n", 4, 4, 8, 4);
            labelInitiatorDate.setConstraints(labelInitiatorDateConstraints);
            panelOrderInitiator.addChild(labelInitiatorDate);
            calenderInitiatorDateTime = new de.docware.framework.modules.gui.controls.formattedfields.GuiDateTimeEditPanel();
            calenderInitiatorDateTime.setName("calenderInitiatorDateTime");
            calenderInitiatorDateTime.__internal_setGenerationDpi(96);
            calenderInitiatorDateTime.registerTranslationHandler(translationHandler);
            calenderInitiatorDateTime.setScaleForResolution(true);
            calenderInitiatorDateTime.setMinimumWidth(10);
            calenderInitiatorDateTime.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag calenderInitiatorDateTimeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 2, 1, 1, 0.0, 0.0, "c", "h", 4, 4, 8, 8);
            calenderInitiatorDateTime.setConstraints(calenderInitiatorDateTimeConstraints);
            panelOrderInitiator.addChild(calenderInitiatorDateTime);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelOrderInitiatorConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 6, 1, 1, 100.0, 0.0, "c", "h", 4, 8, 4, 8);
            panelOrderInitiator.setConstraints(panelOrderInitiatorConstraints);
            panelMainSub.addChild(panelOrderInitiator);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMainSubConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMainSub.setConstraints(panelMainSubConstraints);
            scrollpane.addChild(panelMainSub);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder scrollpaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            scrollpane.setConstraints(scrollpaneConstraints);
            panelMain.addChild(scrollpane);
            panelButton = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelButton.setName("panelButton");
            panelButton.__internal_setGenerationDpi(96);
            panelButton.registerTranslationHandler(translationHandler);
            panelButton.setScaleForResolution(true);
            panelButton.setMinimumWidth(10);
            panelButton.setMinimumHeight(10);
            panelButton.setBorderWidth(4);
            panelButton.setPaddingTop(4);
            panelButton.setPaddingRight(8);
            de.docware.framework.modules.gui.layout.LayoutBorder panelButtonLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelButton.setLayout(panelButtonLayout);
            buttonPartInfo = new de.docware.framework.modules.gui.controls.GuiButton();
            buttonPartInfo.setName("buttonPartInfo");
            buttonPartInfo.__internal_setGenerationDpi(96);
            buttonPartInfo.registerTranslationHandler(translationHandler);
            buttonPartInfo.setScaleForResolution(true);
            buttonPartInfo.setMinimumWidth(100);
            buttonPartInfo.setMinimumHeight(10);
            buttonPartInfo.setMnemonicEnabled(true);
            buttonPartInfo.setText("!!Teileinformation bearbeiten");
            buttonPartInfo.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onButtonPartInfoClicked(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder buttonPartInfoConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            buttonPartInfoConstraints.setPosition("east");
            buttonPartInfo.setConstraints(buttonPartInfoConstraints);
            panelButton.addChild(buttonPartInfo);
            labelCantBeSended = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelCantBeSended.setName("labelCantBeSended");
            labelCantBeSended.__internal_setGenerationDpi(96);
            labelCantBeSended.registerTranslationHandler(translationHandler);
            labelCantBeSended.setScaleForResolution(true);
            labelCantBeSended.setMinimumWidth(10);
            labelCantBeSended.setMinimumHeight(10);
            labelCantBeSended.setFontStyle(de.docware.framework.modules.gui.controls.misc.DWFontStyle.BOLD);
            labelCantBeSended.setPaddingTop(4);
            labelCantBeSended.setPaddingLeft(4);
            labelCantBeSended.setPaddingBottom(4);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelCantBeSendedConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelCantBeSendedConstraints.setPosition("west");
            labelCantBeSended.setConstraints(labelCantBeSendedConstraints);
            panelButton.addChild(labelCantBeSended);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelButtonConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelButtonConstraints.setPosition("north");
            panelButton.setConstraints(panelButtonConstraints);
            panelMain.addChild(panelButton);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMainConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMainConstraints.setPosition("west");
            panelMain.setConstraints(panelMainConstraints);
            tabbedpaneentryPicOrder.addChild(panelMain);
            tabbedpanePicOrder.addChild(tabbedpaneentryPicOrder);
            tabbedpaneentryPicAttachments = new de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry();
            tabbedpaneentryPicAttachments.setName("tabbedpaneentryPicAttachments");
            tabbedpaneentryPicAttachments.__internal_setGenerationDpi(96);
            tabbedpaneentryPicAttachments.registerTranslationHandler(translationHandler);
            tabbedpaneentryPicAttachments.setScaleForResolution(true);
            tabbedpaneentryPicAttachments.setMinimumWidth(10);
            tabbedpaneentryPicAttachments.setMinimumHeight(10);
            tabbedpaneentryPicAttachments.setTitle("!!Anhänge");
            panelAttachments = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelAttachments.setName("panelAttachments");
            panelAttachments.__internal_setGenerationDpi(96);
            panelAttachments.registerTranslationHandler(translationHandler);
            panelAttachments.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.LayoutFlow panelAttachmentsLayout =
                    new de.docware.framework.modules.gui.layout.LayoutFlow();
            panelAttachments.setLayout(panelAttachmentsLayout);
            tabbedpaneentryPicAttachments.addChild(panelAttachments);
            tabbedpanePicOrder.addChild(tabbedpaneentryPicAttachments);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder tabbedpanePicOrderConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            tabbedpanePicOrder.setConstraints(tabbedpanePicOrderConstraints);
            panelMainNew.addChild(tabbedpanePicOrder);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMainNewConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMainNew.setConstraints(panelMainNewConstraints);
            this.addChild(panelMainNew);
            buttonPanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonPanel.setName("buttonPanel");
            buttonPanel.__internal_setGenerationDpi(96);
            buttonPanel.registerTranslationHandler(translationHandler);
            buttonPanel.setScaleForResolution(true);
            buttonPanel.setMinimumWidth(10);
            buttonPanel.setMinimumHeight(10);
            buttonPanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonOKActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onOKButtonClick(event);
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