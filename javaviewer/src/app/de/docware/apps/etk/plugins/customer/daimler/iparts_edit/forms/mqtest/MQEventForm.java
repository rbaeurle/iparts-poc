/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.mqtest;

import de.docware.apps.etk.base.forms.AbstractJavaViewerFormConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.MQChannel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.MQHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsEventStates;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferStates;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.event.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.iPartsXMLMediaMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrderList;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditASPLMContractorForm;
import de.docware.apps.etk.viewer.JavaViewerApplication;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiTextField;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.modules.xml.DwXmlFile;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.java1_1.Java1_1_Utils;
import de.docware.util.sort.SortStringCache;

import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;

public class MQEventForm {

    private static final String DEFAULT_USER = "MQ EVENT SIMULATION";
    private static final String DEFAULT_WORKFLOWNAME = "Medienerstellung";

    private EditASPLMContractorForm contractorForm;
    private StatusEventPanel statusEventPanel;
    private MQChannel mediaChannel;
    private Session session;
    private RComboBox<iPartsTransferNodeTypes> eventTypeComboBox;
    private RComboBox<iPartsDataPicOrder> mcComboBox;

    /**
     * Erzeugt eine Instanz von MQEventForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public MQEventForm() {
        $$internalCreateGui$$(null);
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        eventTypeComboBox = RComboBox.replaceGuiComboBox(mainWindow.eventTypeCombobox);
        eventTypeComboBox.setFilterable(false);
        mcComboBox = RComboBox.replaceGuiComboBox(mainWindow.mcCombobox);
        for (iPartsTransferNodeTypes eventType : iPartsTransferNodeTypes.getAllEventTypes()) {
            eventTypeComboBox.addItem(eventType, eventType.getAlias());
        }
        fillEventSpecificParameters(null);
        mcComboBox.requestFocus();
    }

    /**
     * Befüllt die Event Parameter abhängig vom aktuell ausgewählten Event
     *
     * @param event
     */
    private void fillEventSpecificParameters(Event event) {
        mainWindow.eventParameterPanel.removeAllChildren();
        iPartsTransferNodeTypes eventType = eventTypeComboBox.getSelectedUserObject();
        if (eventType != null) {
            mcComboBox.removeAllItems();
            // Hole alle Bildaufträge aus der DB, die den Status "ANGENOMMEN", "IN_BEARBEIT", "ÄNDERUNG VERSCHICKT", "KOPIERAUFTRAG VERSCHICKT",
            // "ÄNDERUNG ANGENOMMEN", "KORREKTUR VERSCHICKT", "KORREKTUR ANGENOMMEN", "ANHÄNGE VERSCHICKT", "ANHÄNGE ANGENOMMEN"
            // oder "ANHÄNGE ABGELEHNT" haben (nur diese können auf Events reagieren)
            List<iPartsDataPicOrder> allPicOrders = new DwList<>();
            iPartsDataPicOrderList list = iPartsDataPicOrderList.loadPicOrderListForSpecificStates(JavaViewerApplication.getInstance().getProject(),
                                                                                                   iPartsTransferStates.ANGENOMMEN,
                                                                                                   iPartsTransferStates.IN_BEARBEIT,
                                                                                                   iPartsTransferStates.ATTACHMENTS_ACCEPTED,
                                                                                                   iPartsTransferStates.ATTACHMENTS_DENIED,
                                                                                                   iPartsTransferStates.ATTACHMENTS_SENT,
                                                                                                   iPartsTransferStates.CORRECTION_MO_RECEIVED,
                                                                                                   iPartsTransferStates.CORRECTION_MO_REQUESTED,
                                                                                                   iPartsTransferStates.CHANGE_REQUESTED,
                                                                                                   iPartsTransferStates.CHANGE_RECEIVED,
                                                                                                   iPartsTransferStates.CANCEL_RESPONSE,
                                                                                                   iPartsTransferStates.CANCEL_ERROR,
                                                                                                   iPartsTransferStates.COPY_REQUESTED,
                                                                                                   iPartsTransferStates.COPY_RECEIVED);
            allPicOrders.addAll(list.getAsList());
            // Sortieren nach Modulnr.
            sortList(allPicOrders);
            // Combobox mit Modulen füllen
            for (iPartsDataPicOrder picOrder : allPicOrders) {
                mcComboBox.addItem(picOrder, createMediaContainerName(picOrder));
            }

            if (contractorForm != null) {
                contractorForm.dispose();
                contractorForm = null;
            }

            // Abhängig vom ausgewählten Event werden event-spezifische Parameter eingeblendet
            switch (eventType) {
                case EVENT_ASSIGNMENT_CHANGE:
                    AbstractJavaViewerFormConnector connector = new AbstractJavaViewerFormConnector(null);
                    contractorForm = new EditASPLMContractorForm(connector, null);
                    contractorForm.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                        public void fire(de.docware.framework.modules.gui.event.Event event) {
                            checkParameter(event);
                        }
                    });
                    AbstractGuiControl gui = contractorForm.getContractorPanelToBeAdded();
                    gui.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
                    mainWindow.eventParameterPanel.addChild(gui);

                    contractorForm.setUserGroupLabelText("!!Group-ID");
                    contractorForm.setUserLabelText("!!User-ID");
                    break;
                case EVENT_RELEASE_STATUS_CHANGE:
                    ConstraintsBorder constraints = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
                    statusEventPanel = new StatusEventPanel();
                    statusEventPanel.setConstraints(constraints);
                    mainWindow.eventParameterPanel.addChild(statusEventPanel);
                    statusEventPanel.setVisible(true);
                    break;
                default:
                    Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Event simulation can only use Evnts! Selected type: " + eventType);
            }
            checkParameter(null);
        }
    }

    private void sortList(List<iPartsDataPicOrder> allPicOrders) {
        final SortStringCache cache = new SortStringCache();
        Collections.sort(allPicOrders, new Comparator<iPartsDataPicOrder>() {
                             @Override
                             public int compare(iPartsDataPicOrder o1, iPartsDataPicOrder o2) {
                                 String[] o1Values = { o1.getProposedName(), o1.getOrderIdExtern(), o1.getOrderRevisionExtern() };
                                 String[] o2Values = { o2.getProposedName(), o2.getOrderIdExtern(), o2.getOrderRevisionExtern() };
                                 for (int i = 0; i < o1Values.length; i++) {
                                     String s1 = o1Values[i];
                                     String s2 = o2Values[i];

                                     s1 = cache.getSortString(s1, true);
                                     s2 = cache.getSortString(s2, true);
                                     int result = s1.compareTo(s2);
                                     if (result != 0) {
                                         return result;
                                     }
                                 }
                                 return 0;
                             }
                         }

        );
    }

    /**
     * Erstellt den Modul Anzeigenamen für die Combobox
     *
     * @param picOrder
     * @return
     */
    private String createMediaContainerName(iPartsDataPicOrder picOrder) {
        return picOrder.getProposedName() + " - " + picOrder.getOrderIdExtern() + " / " + picOrder.getOrderRevisionExtern();
    }

    public void show() {
        mainWindow.showModal();
        if (contractorForm != null) {
            contractorForm.dispose();
        }
    }

    /**
     * Versendet den Event via Media Channel
     *
     * @param event
     */
    private void sendEvent(Event event) {
        if (checkParameter(null)) {
            if (session == null) {
                session = Session.get();
            }
            iPartsXMLMediaMessage mqMessage = buildMQMessage();
            final DwXmlFile messageAsFile = XMLImportExportHelper.writeXMLFileFromMessageObject(mqMessage, iPartsPlugin.MQ_CHANNEL_TYPE_MEDIA,
                                                                                                "iPartsOut", "", true);
            if (mediaChannel == null) {
                mediaChannel = MQHelper.getInstance().getChannel(iPartsPlugin.MQ_CHANNEL_TYPE_MEDIA);
                if (mediaChannel == null) {
                    Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "MQ Media Channel can not be retrieved.");
                    return;
                }
            }
            session.startChildThread(new FrameworkRunnable() {
                @Override
                public void run(FrameworkThread thread) {
                    int delay = StrUtils.strToIntDef(mainWindow.delayTextfield.getText(), 0);
                    if (!Java1_1_Utils.sleep(delay * 1000)) {
                        try {
                            mediaChannel.sendMessage(messageAsFile.getContentAsString(), null, true);
                        } catch (Throwable mqE) {
                            Logger.getLogger().logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, mqE);
                        }
                    }
                }
            });

            showHints("Event versendet!");
        }
    }

    /**
     * Versendet den Event via Media Channel und schließt den Dialog.
     *
     * @param event
     */
    private void sendAndCloseEvent(Event event) {
        sendEvent(event);
        closeWindow(event);
    }

    private void showHints(String hint) {
        if ((hint != null) && !hint.isEmpty()) {
            mainWindow.errorLabel.setText(hint);
            mainWindow.errorLabel.setVisible(true);
        } else {
            mainWindow.errorLabel.setVisible(false);
        }
    }

    /**
     * Überprüft, ob alle notwendigen Parameter gesetzt wurden. Falls nicht, wird ein Fehlertext angezeigt
     *
     * @return
     */
    private boolean checkParameter(Event event) {
        List<String> errors = new DwList<>();
        iPartsTransferNodeTypes eventType = eventTypeComboBox.getSelectedUserObject();
        switch (eventType) {
            case EVENT_ASSIGNMENT_CHANGE:
                if (contractorForm != null) {
                    boolean groupIDValid = (contractorForm.getSelectedASPLMGroupId() != null) && !contractorForm.getSelectedASPLMGroupId().isEmpty();
                    if (!groupIDValid) {
                        errors.add("Die Group-ID darf nicht leer sein!");
                    }
                }
                break;
            case EVENT_RELEASE_STATUS_CHANGE:
                if (statusEventPanel != null) {
                    if (statusEventPanel.hasSimilarContent()) {
                        errors.add("Alter Status und neuer Status dürfen nicht identisch sein!");
                    }
                    if (!statusEventPanel.isValid()) {
                        errors.add("Die event-spezifischen Parameter müssen ausgefüllt sein!");
                    }
                }
                break;
        }
        if (mainWindow.itemIdTextfield.getText().isEmpty()) {
            errors.add("Die MC item Id darf nicht leer sein!");
        }
        if (mainWindow.itemRevIdTextfield.getText().isEmpty()) {
            errors.add("Die MC Item Rev Id darf nicht leer sein!");
        }

        String errorText = "";
        for (String error : errors) {
            errorText = errorText + error + "\n";
        }
        boolean errorFree = errors.isEmpty();
        if (!errorFree) {
            showHints(errorText);
        } else {
            showHints("");
        }
        return errorFree;
    }

    /**
     * Erstellt das {@link iPartsXMLMediaMessage} Objekt, abhängig vom ausgewählten Event
     *
     * @return
     */
    private iPartsXMLMediaMessage buildMQMessage() {
        iPartsXMLEvent event = new iPartsXMLEvent(iPartsTransferConst.PARTICIPANT_ASPLM, GregorianCalendar.getInstance().getTime());
        iPartsTransferNodeTypes eventType = eventTypeComboBox.getSelectedUserObject();
        String userID = null;
        switch (eventType) {
            case EVENT_ASSIGNMENT_CHANGE:
                iPartsXMLEventAssingmentChange assingmentChange = new iPartsXMLEventAssingmentChange(contractorForm.getSelectedASPLMGroupId());
                userID = contractorForm.getSelectedASPLMUserId();
                assingmentChange.setWorkflowName(DEFAULT_WORKFLOWNAME);
                event.setActualEvent(assingmentChange);
                break;
            case EVENT_RELEASE_STATUS_CHANGE:
                iPartsXMLEventReleaseStatusChange releaseStatusChange = new iPartsXMLEventReleaseStatusChange(statusEventPanel.getNewStatus().getAsplmValue());
                releaseStatusChange.setOldStatus(statusEventPanel.getOldStatus().getAsplmValue());
                userID = statusEventPanel.getUserId();
                event.setActualEvent(releaseStatusChange);
                break;
            default:
                Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Event simulation can only use Events! Selected type: " + eventType);
        }

        if (StrUtils.isValid(userID)) {
            event.getActualEvent().setUserId(userID);
        }

        // Kommentar
        String commentText = mainWindow.commentTextarea.getText();
        if (StrUtils.isValid(commentText)) {
            iPartsXMLComment comment = new iPartsXMLComment(commentText);
            event.getActualEvent().addComment(comment);
        }

        // TcObject
        iPartsXMLTcObject tcObject = new iPartsXMLTcObject(mainWindow.itemIdTextfield.getText(), mainWindow.itemRevIdTextfield.getText());
        event.getActualEvent().setTcObject(tcObject);

        iPartsXMLMediaMessage mediaMessage = new iPartsXMLMediaMessage(true);
        mediaMessage.setTypeObject(event);

        return mediaMessage;
    }

    private void closeWindow(Event event) {
        mainWindow.setVisible(false);
    }

    /**
     * Befüllt die benötigten ID Felder.
     *
     * @param event
     */
    private void fillIdFields(Event event) {
        iPartsDataPicOrder picOrder = mcComboBox.getSelectedUserObject();
        if (picOrder != null) {
            mainWindow.itemRevIdTextfield.setText(picOrder.getOrderRevisionExtern());
            mainWindow.itemIdTextfield.setText(picOrder.getOrderIdExtern());
        }
    }

    /**
     * Hilfsklasse für den Status-Change-Event
     */
    private class StatusEventPanel extends GuiPanel {

        private RComboBox<iPartsEventStates> oldStatus;
        private RComboBox<iPartsEventStates> newStatus;
        private GuiTextField userId;

        public StatusEventPanel() {
            initPanel();
        }

        private void initPanel() {
            __internal_setGenerationDpi(96);
            registerTranslationHandler(translationHandler);
            setScaleForResolution(true);
            setMinimumWidth(10);
            setMinimumHeight(10);
            LayoutGridBag mainpanelLayout = new LayoutGridBag();
            mainpanelLayout.setCentered(false);
            setLayout(mainpanelLayout);

            ConstraintsGridBag constraints = new ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, ConstraintsGridBag.ANCHOR_EAST,
                                                                    ConstraintsGridBag.FILL_NONE, 4, 4, 0, 4);
            GuiLabel label = new GuiLabel("User ID:");
            label.setConstraints(constraints);
            addChild(label);

            userId = new GuiTextField();
            userId.setMinimumWidth(200);
            userId.setScaleForResolution(true);
            constraints = new ConstraintsGridBag(1, 0, 1, 1, 100.0, 0.0, ConstraintsGridBag.ANCHOR_CENTER, ConstraintsGridBag.FILL_HORIZONTAL,
                                                 4, 4, 0, 4);
            userId.setConstraints(constraints);
            addChild(userId);


            oldStatus = new RComboBox();
            newStatus = new RComboBox();

            for (iPartsEventStates state : iPartsEventStates.getAsASPLMList()) {
                if (state == iPartsEventStates.UNKNOWN) {
                    continue;
                }
                String comboBoxValue = state.getAsplmValueForText();
                oldStatus.addItem(state, comboBoxValue);
                newStatus.addItem(state, comboBoxValue);
            }

            constraints = new ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, ConstraintsGridBag.ANCHOR_EAST, ConstraintsGridBag.FILL_NONE,
                                                 4, 4, 0, 4);
            label = new GuiLabel("Alter Status:");
            label.setConstraints(constraints);
            addChild(label);

            oldStatus.setFilterable(false);
            oldStatus.setEnabled(false); // Ändern vom alten Status macht eigentlich keinen Sinn und stimmt auch nicht mit der DB überein
            oldStatus.setMinimumWidth(200);
            oldStatus.setName("oldStateComboBox");
            oldStatus.setScaleForResolution(true);
            constraints = new ConstraintsGridBag(1, 1, 1, 1, 0.0, 0.0, ConstraintsGridBag.ANCHOR_CENTER, ConstraintsGridBag.FILL_HORIZONTAL,
                                                 4, 4, 0, 4);
            oldStatus.setConstraints(constraints);
            oldStatus.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    checkParameter(event);
                }
            });
            addChild(oldStatus);

            constraints = new ConstraintsGridBag(0, 2, 1, 1, 0.0, 0.0, ConstraintsGridBag.ANCHOR_EAST, ConstraintsGridBag.FILL_NONE,
                                                 4, 4, 0, 4);
            label = new GuiLabel("Neuer Status:");
            label.setConstraints(constraints);
            addChild(label);

            newStatus.setFilterable(false);
            newStatus.setName("newStateComboBox");
            newStatus.setMinimumWidth(200);
            newStatus.setScaleForResolution(true);
            constraints = new ConstraintsGridBag(1, 2, 1, 1, 0.0, 0.0, ConstraintsGridBag.ANCHOR_CENTER, ConstraintsGridBag.FILL_HORIZONTAL,
                                                 4, 4, 0, 4);
            newStatus.setConstraints(constraints);
            addChild(newStatus);
            newStatus.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    checkParameter(event);
                }
            });

        }

        public String getUserId() {
            if (userId.getText() == null || userId.getText().isEmpty()) {
                return DEFAULT_USER;
            }
            return userId.getText();
        }

        public iPartsEventStates getOldStatus() {
            return oldStatus.getSelectedUserObject();
        }

        public iPartsEventStates getNewStatus() {
            return newStatus.getSelectedUserObject();
        }

        public boolean isValid() {
            boolean oldStatusValid = ((oldStatus != null) && (oldStatus.getSelectedUserObject() != null));
            boolean newStatusValid = ((newStatus != null) && (newStatus.getSelectedUserObject() != null));

            return oldStatusValid && newStatusValid;
        }

        public boolean hasSimilarContent() {
            iPartsEventStates oldState = oldStatus.getSelectedUserObject();
            iPartsEventStates newState = newStatus.getSelectedUserObject();

            return isValid() && oldState.getAsplmValue().equals(newState.getAsplmValue());
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
        private de.docware.framework.modules.gui.controls.GuiTitle eventWindowTitle;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel mainpanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel eventTypeLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes> eventTypeCombobox;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel mcLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrder> mcCombobox;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel itemIdLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField itemIdTextfield;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel itemRevIdLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField itemRevIdTextfield;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel commentLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextArea commentTextarea;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel eventParameterPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel eventParameterLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel errorLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel hintsLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel delayLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField delayTextfield;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel buttonPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton sendButton;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton sendAndCloseButton;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton closeButton;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
            eventWindowTitle = new de.docware.framework.modules.gui.controls.GuiTitle();
            eventWindowTitle.setName("eventWindowTitle");
            eventWindowTitle.__internal_setGenerationDpi(96);
            eventWindowTitle.registerTranslationHandler(translationHandler);
            eventWindowTitle.setScaleForResolution(true);
            eventWindowTitle.setMinimumWidth(10);
            eventWindowTitle.setMinimumHeight(50);
            eventWindowTitle.setTitle("MQ Event Simulation");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder eventWindowTitleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            eventWindowTitleConstraints.setPosition("north");
            eventWindowTitle.setConstraints(eventWindowTitleConstraints);
            this.addChild(eventWindowTitle);
            mainpanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            mainpanel.setName("mainpanel");
            mainpanel.__internal_setGenerationDpi(96);
            mainpanel.registerTranslationHandler(translationHandler);
            mainpanel.setScaleForResolution(true);
            mainpanel.setMinimumWidth(10);
            mainpanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag mainpanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            mainpanelLayout.setCentered(false);
            mainpanel.setLayout(mainpanelLayout);
            eventTypeLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            eventTypeLabel.setName("eventTypeLabel");
            eventTypeLabel.__internal_setGenerationDpi(96);
            eventTypeLabel.registerTranslationHandler(translationHandler);
            eventTypeLabel.setScaleForResolution(true);
            eventTypeLabel.setMinimumWidth(10);
            eventTypeLabel.setMinimumHeight(10);
            eventTypeLabel.setText("!!Event Typ:");
            eventTypeLabel.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag eventTypeLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "e", "n", 4, 8, 0, 4);
            eventTypeLabel.setConstraints(eventTypeLabelConstraints);
            mainpanel.addChild(eventTypeLabel);
            eventTypeCombobox = new de.docware.framework.modules.gui.controls.GuiComboBox<de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes>();
            eventTypeCombobox.setName("eventTypeCombobox");
            eventTypeCombobox.__internal_setGenerationDpi(96);
            eventTypeCombobox.registerTranslationHandler(translationHandler);
            eventTypeCombobox.setScaleForResolution(true);
            eventTypeCombobox.setMinimumWidth(10);
            eventTypeCombobox.setMinimumHeight(10);
            eventTypeCombobox.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    fillEventSpecificParameters(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag eventTypeComboboxConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, "e", "h", 4, 0, 0, 8);
            eventTypeCombobox.setConstraints(eventTypeComboboxConstraints);
            mainpanel.addChild(eventTypeCombobox);
            mcLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            mcLabel.setName("mcLabel");
            mcLabel.__internal_setGenerationDpi(96);
            mcLabel.registerTranslationHandler(translationHandler);
            mcLabel.setScaleForResolution(true);
            mcLabel.setMinimumWidth(10);
            mcLabel.setMinimumHeight(10);
            mcLabel.setText("!!Media Container:");
            mcLabel.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag mcLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "e", "n", 4, 8, 0, 4);
            mcLabel.setConstraints(mcLabelConstraints);
            mainpanel.addChild(mcLabel);
            mcCombobox = new de.docware.framework.modules.gui.controls.GuiComboBox<de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrder>();
            mcCombobox.setName("mcCombobox");
            mcCombobox.__internal_setGenerationDpi(96);
            mcCombobox.registerTranslationHandler(translationHandler);
            mcCombobox.setScaleForResolution(true);
            mcCombobox.setMinimumWidth(10);
            mcCombobox.setMinimumHeight(10);
            mcCombobox.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    fillIdFields(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag mcComboboxConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 1, 1, 1, 0.0, 0.0, "e", "h", 4, 0, 0, 8);
            mcCombobox.setConstraints(mcComboboxConstraints);
            mainpanel.addChild(mcCombobox);
            itemIdLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            itemIdLabel.setName("itemIdLabel");
            itemIdLabel.__internal_setGenerationDpi(96);
            itemIdLabel.registerTranslationHandler(translationHandler);
            itemIdLabel.setScaleForResolution(true);
            itemIdLabel.setMinimumWidth(10);
            itemIdLabel.setMinimumHeight(10);
            itemIdLabel.setText("!!MC Item Id:");
            itemIdLabel.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag itemIdLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 0.0, 0.0, "e", "n", 4, 8, 0, 4);
            itemIdLabel.setConstraints(itemIdLabelConstraints);
            mainpanel.addChild(itemIdLabel);
            itemIdTextfield = new de.docware.framework.modules.gui.controls.GuiTextField();
            itemIdTextfield.setName("itemIdTextfield");
            itemIdTextfield.__internal_setGenerationDpi(96);
            itemIdTextfield.registerTranslationHandler(translationHandler);
            itemIdTextfield.setScaleForResolution(true);
            itemIdTextfield.setMinimumWidth(200);
            itemIdTextfield.setMinimumHeight(10);
            itemIdTextfield.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    checkParameter(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag itemIdTextfieldConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 2, 1, 1, 0.0, 0.0, "e", "h", 4, 0, 0, 8);
            itemIdTextfield.setConstraints(itemIdTextfieldConstraints);
            mainpanel.addChild(itemIdTextfield);
            itemRevIdLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            itemRevIdLabel.setName("itemRevIdLabel");
            itemRevIdLabel.__internal_setGenerationDpi(96);
            itemRevIdLabel.registerTranslationHandler(translationHandler);
            itemRevIdLabel.setScaleForResolution(true);
            itemRevIdLabel.setMinimumWidth(10);
            itemRevIdLabel.setMinimumHeight(10);
            itemRevIdLabel.setText("!!MC Item Rev Id:");
            itemRevIdLabel.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag itemRevIdLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 3, 1, 1, 0.0, 0.0, "e", "n", 4, 8, 0, 4);
            itemRevIdLabel.setConstraints(itemRevIdLabelConstraints);
            mainpanel.addChild(itemRevIdLabel);
            itemRevIdTextfield = new de.docware.framework.modules.gui.controls.GuiTextField();
            itemRevIdTextfield.setName("itemRevIdTextfield");
            itemRevIdTextfield.__internal_setGenerationDpi(96);
            itemRevIdTextfield.registerTranslationHandler(translationHandler);
            itemRevIdTextfield.setScaleForResolution(true);
            itemRevIdTextfield.setMinimumWidth(50);
            itemRevIdTextfield.setMinimumHeight(10);
            itemRevIdTextfield.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    checkParameter(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag itemRevIdTextfieldConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 3, 1, 1, 0.0, 0.0, "w", "n", 4, 0, 0, 8);
            itemRevIdTextfield.setConstraints(itemRevIdTextfieldConstraints);
            mainpanel.addChild(itemRevIdTextfield);
            commentLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            commentLabel.setName("commentLabel");
            commentLabel.__internal_setGenerationDpi(96);
            commentLabel.registerTranslationHandler(translationHandler);
            commentLabel.setScaleForResolution(true);
            commentLabel.setMinimumWidth(10);
            commentLabel.setMinimumHeight(10);
            commentLabel.setText("!!Kommentar:");
            commentLabel.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag commentLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 4, 1, 1, 0.0, 0.0, "e", "n", 4, 8, 0, 4);
            commentLabel.setConstraints(commentLabelConstraints);
            mainpanel.addChild(commentLabel);
            commentTextarea = new de.docware.framework.modules.gui.controls.GuiTextArea();
            commentTextarea.setName("commentTextarea");
            commentTextarea.__internal_setGenerationDpi(96);
            commentTextarea.registerTranslationHandler(translationHandler);
            commentTextarea.setScaleForResolution(true);
            commentTextarea.setMinimumWidth(200);
            commentTextarea.setMinimumHeight(100);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag commentTextareaConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 4, 1, 1, 0.0, 0.0, "e", "h", 4, 0, 0, 8);
            commentTextarea.setConstraints(commentTextareaConstraints);
            mainpanel.addChild(commentTextarea);
            eventParameterPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            eventParameterPanel.setName("eventParameterPanel");
            eventParameterPanel.__internal_setGenerationDpi(96);
            eventParameterPanel.registerTranslationHandler(translationHandler);
            eventParameterPanel.setScaleForResolution(true);
            eventParameterPanel.setMinimumWidth(10);
            eventParameterPanel.setMinimumHeight(20);
            de.docware.framework.modules.gui.layout.LayoutBorder eventParameterPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            eventParameterPanel.setLayout(eventParameterPanelLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag eventParameterPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 6, 1, 1, 100.0, 0.0, "c", "b", 10, 4, 4, 4);
            eventParameterPanel.setConstraints(eventParameterPanelConstraints);
            mainpanel.addChild(eventParameterPanel);
            eventParameterLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            eventParameterLabel.setName("eventParameterLabel");
            eventParameterLabel.__internal_setGenerationDpi(96);
            eventParameterLabel.registerTranslationHandler(translationHandler);
            eventParameterLabel.setScaleForResolution(true);
            eventParameterLabel.setMinimumWidth(10);
            eventParameterLabel.setMinimumHeight(10);
            eventParameterLabel.setText("!!Event Parameter:");
            eventParameterLabel.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag eventParameterLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 6, 1, 1, 0.0, 0.0, "ne", "n", 12, 8, 0, 4);
            eventParameterLabel.setConstraints(eventParameterLabelConstraints);
            mainpanel.addChild(eventParameterLabel);
            errorLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            errorLabel.setName("errorLabel");
            errorLabel.__internal_setGenerationDpi(96);
            errorLabel.registerTranslationHandler(translationHandler);
            errorLabel.setScaleForResolution(true);
            errorLabel.setMinimumWidth(10);
            errorLabel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag errorLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 7, 1, 1, 0.0, 0.0, "w", "h", 14, 0, 4, 8);
            errorLabel.setConstraints(errorLabelConstraints);
            mainpanel.addChild(errorLabel);
            hintsLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            hintsLabel.setName("hintsLabel");
            hintsLabel.__internal_setGenerationDpi(96);
            hintsLabel.registerTranslationHandler(translationHandler);
            hintsLabel.setScaleForResolution(true);
            hintsLabel.setMinimumWidth(10);
            hintsLabel.setMinimumHeight(10);
            hintsLabel.setText("!!Hinweise:");
            hintsLabel.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag hintsLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 7, 1, 1, 0.0, 0.0, "ne", "n", 14, 8, 0, 4);
            hintsLabel.setConstraints(hintsLabelConstraints);
            mainpanel.addChild(hintsLabel);
            delayLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            delayLabel.setName("delayLabel");
            delayLabel.__internal_setGenerationDpi(96);
            delayLabel.registerTranslationHandler(translationHandler);
            delayLabel.setScaleForResolution(true);
            delayLabel.setMinimumWidth(10);
            delayLabel.setMinimumHeight(10);
            delayLabel.setText("!!Verzögerung (Sek):");
            delayLabel.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag delayLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 5, 1, 1, 0.0, 0.0, "e", "n", 0, 8, 0, 4);
            delayLabel.setConstraints(delayLabelConstraints);
            mainpanel.addChild(delayLabel);
            delayTextfield = new de.docware.framework.modules.gui.controls.GuiTextField();
            delayTextfield.setName("delayTextfield");
            delayTextfield.__internal_setGenerationDpi(96);
            delayTextfield.registerTranslationHandler(translationHandler);
            delayTextfield.setScaleForResolution(true);
            delayTextfield.setMinimumWidth(50);
            delayTextfield.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag delayTextfieldConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 5, 1, 1, 0.0, 0.0, "w", "n", 4, 0, 4, 8);
            delayTextfield.setConstraints(delayTextfieldConstraints);
            mainpanel.addChild(delayTextfield);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder mainpanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            mainpanel.setConstraints(mainpanelConstraints);
            this.addChild(mainpanel);
            buttonPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            buttonPanel.setName("buttonPanel");
            buttonPanel.__internal_setGenerationDpi(96);
            buttonPanel.registerTranslationHandler(translationHandler);
            buttonPanel.setScaleForResolution(true);
            buttonPanel.setMinimumWidth(10);
            buttonPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag buttonPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            buttonPanel.setLayout(buttonPanelLayout);
            sendButton = new de.docware.framework.modules.gui.controls.GuiButton();
            sendButton.setName("sendButton");
            sendButton.__internal_setGenerationDpi(96);
            sendButton.registerTranslationHandler(translationHandler);
            sendButton.setScaleForResolution(true);
            sendButton.setMinimumWidth(100);
            sendButton.setMinimumHeight(20);
            sendButton.setMnemonicEnabled(true);
            sendButton.setText("Senden");
            sendButton.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    sendEvent(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag sendButtonConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "c", "n", 0, 8, 8, 4);
            sendButton.setConstraints(sendButtonConstraints);
            buttonPanel.addChild(sendButton);
            sendAndCloseButton = new de.docware.framework.modules.gui.controls.GuiButton();
            sendAndCloseButton.setName("sendAndCloseButton");
            sendAndCloseButton.__internal_setGenerationDpi(96);
            sendAndCloseButton.registerTranslationHandler(translationHandler);
            sendAndCloseButton.setScaleForResolution(true);
            sendAndCloseButton.setMinimumWidth(100);
            sendAndCloseButton.setMinimumHeight(20);
            sendAndCloseButton.setMnemonicEnabled(true);
            sendAndCloseButton.setText("Senden und schließen");
            sendAndCloseButton.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    sendAndCloseEvent(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag sendAndCloseButtonConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, "c", "n", 0, 8, 8, 4);
            sendAndCloseButton.setConstraints(sendAndCloseButtonConstraints);
            buttonPanel.addChild(sendAndCloseButton);
            closeButton = new de.docware.framework.modules.gui.controls.GuiButton();
            closeButton.setName("closeButton");
            closeButton.__internal_setGenerationDpi(96);
            closeButton.registerTranslationHandler(translationHandler);
            closeButton.setScaleForResolution(true);
            closeButton.setMinimumWidth(100);
            closeButton.setMinimumHeight(20);
            closeButton.setMnemonicEnabled(true);
            closeButton.setText("Schließen");
            closeButton.setCancelButton(true);
            closeButton.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    closeWindow(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag closeButtonConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(2, 0, 1, 1, 0.0, 0.0, "e", "n", 0, 0, 8, 8);
            closeButton.setConstraints(closeButtonConstraints);
            buttonPanel.addChild(closeButton);
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