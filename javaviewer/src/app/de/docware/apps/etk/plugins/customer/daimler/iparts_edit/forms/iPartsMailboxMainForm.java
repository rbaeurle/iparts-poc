/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.EtkDbs;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.AbstractJavaViewerMainFormContainer;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.forms.toolbar.EtkToolbarButton;
import de.docware.apps.etk.base.misc.EtkMainToolbarManager;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.viewermain.forms.JavaViewerMainWindow;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.mailbox.iPartsDataMailboxItem;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.mailbox.iPartsDataMailboxRecipient;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.mailbox.iPartsDataMailboxRecipientList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.mailbox.iPartsMailboxHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsMailboxChangedEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditDataObjectFilterGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.framework.combimodules.useradmin.db.UserAdminRoleCache;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.app.DWLayoutManager;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiWindow;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.misc.DWFontStyle;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.controls.toolbar.AbstractGuiToolComponent;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolButton;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolbar;
import de.docware.framework.modules.gui.controls.toolbar.ToolButtonImages;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerFireOnce;
import de.docware.framework.modules.gui.layout.constraints.AbstractConstraints;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.responsive.base.RButtonImages;
import de.docware.framework.modules.gui.responsive.base.theme.ThemeHelper;
import de.docware.framework.modules.gui.responsive.base.theme.ThemeManager;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.observer.ObserverCall;
import de.docware.util.misc.observer.ObserverCallback;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Hauptformular für den iParts Postkorb. Hier werden Nachrichten angezeigt, als gelesen markiert und versendet.
 */
public class iPartsMailboxMainForm extends AbstractJavaViewerMainFormContainer implements iPartsConst {

    public static final String MAIN_TOOLBAR_ALIAS = "iParts4Mailbox";
    private static final int MAX_MESSAGE_PREVIEW_LENGTH = 100;

    private MessagesGrid grid;
    private MessagesGrid gridForSentMessages;

    private Set<iPartsMailboxHelper.MailboxObject> mailboxObjects;
    private Set<iPartsMailboxHelper.MailboxObject> sentMailboxObjects;
    private iPartsMailboxHelper mailboxHelper;

    /**
     * Erzeugt eine Instanz von iPartsMailboxMainForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public iPartsMailboxMainForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        postCreateGui();
        mailboxHelper = new iPartsMailboxHelper(iPartsUserAdminDb.getLoginUserName());

        // initiales Laden der Nachrichten und setzen des Icons
        loadMessages(false);
        loadMessages(true);
        updateMainToolbarIcon();

        // Application Event einhängen um auf Änderungen an Nachrichten zu reagieren
        getProject().addAppEventListener(new ObserverCallback(callbackBinder, iPartsMailboxChangedEvent.class) {
            @Override
            public void callback(ObserverCall call) {
                if (call instanceof iPartsMailboxChangedEvent) {
                    handleMailboxChangedEvent((iPartsMailboxChangedEvent)(call));
                }
            }
        });

        gridForSentMessages.showReadMessagesButton.doClick(); // Initial auch die gelesenen gesendeten Nachrichten anzeigen
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz erzeugt wurde.
     */
    private void postCreateGui() {
        EtkDisplayFields displayFields = createDisplayFields();

        // Empfangene Nachrichten
        grid = new MessagesGrid(false, getConnector(), parentForm);
        grid.setDisplayFields(displayFields, true);
        mainWindow.receivedMessagesPanel.removeFromParent();
        mainWindow.receivedMessagesDockingPanel.addChildBorderCenter(grid.getGui());

        // Gesendete Nachrichten
        displayFields = new EtkDisplayFields(displayFields);
        int creatorIndex = Math.max(0, displayFields.getIndexOfFeld(TABLE_DA_MESSAGE, FIELD_DMSG_CREATION_USER_ID, false));
        addDefaultDisplayField(TABLE_DA_MESSAGE_TO, FIELD_DMT_USER_ID, displayFields, true, creatorIndex);
        addDefaultDisplayField(TABLE_DA_MESSAGE_TO, FIELD_DMT_GROUP_ID, displayFields, true, creatorIndex + 1);
//        addDefaultDisplayField(TABLE_DA_MESSAGE_TO, FIELD_DMT_ROLE_ID, displayFields, true, creatorIndex + 2);
//        addDefaultDisplayField(TABLE_DA_MESSAGE_TO, FIELD_DMT_ORGANISATION_ID, displayFields, true, creatorIndex + 3);
        gridForSentMessages = new MessagesGrid(true, getConnector(), parentForm);
        gridForSentMessages.setDisplayFields(displayFields, true);
        if (iPartsRight.CREATE_MAILBOX_MESSAGES.checkRightInSession()) {
            mainWindow.sentMessagesPanel.removeFromParent();
            mainWindow.sentMessagesDockingPanel.addChildBorderCenter(gridForSentMessages.getGui());
        } else {
            // Gesendete Nachrichten nicht anzeigen
            AbstractConstraints messagesConstraints = mainWindow.messagesGridsSplitPane.getConstraints();
            grid.getGui().setConstraints(messagesConstraints);
            AbstractGuiControl parentControl = mainWindow.messagesGridsSplitPane.getParent();
            mainWindow.messagesGridsSplitPane.removeFromParent();
            parentControl.addChild(grid.getGui());
        }

        mainWindow.sentMessagesDockingPanel.addEventListener(new EventListenerFireOnce(Event.ON_RESIZE_EVENT) {
            @Override
            public void fireOnce(Event event) {
                int height = event.getIntParameter(Event.EVENT_PARAMETER_NEWHEIGHT) - event.getIntParameter(Event.EVENT_PARAMETER_CONTROL_POSITION);
                mainWindow.messagesGridsSplitPane.setDividerPosition(height / 2 + 100);
            }
        });

        ThemeManager.get().render(mainWindow.centerPanel);
    }

    /**
     * Erstellt die benötigten Anzeigefelder
     *
     * @return
     */
    private EtkDisplayFields createDisplayFields() {
        EtkDisplayFields displayFields = new EtkDisplayFields();

        // Entweder aus der Konfiguration ...
        displayFields.load(getConfig(), iPartsEditConfigConst.iPARTS_EDIT_MAILBOX_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS);

        // ... oder die Default-Fields:
        if (displayFields.size() == 0) {
            addDefaultDisplayField(TABLE_DA_MESSAGE, FIELD_DMSG_SUBJECT, displayFields, true, -1);

            addDefaultDisplayField(TABLE_DA_MESSAGE, FIELD_DMSG_CREATION_USER_ID, displayFields, true, -1);

            addDefaultDisplayField(TABLE_DA_MESSAGE, FIELD_DMSG_TYPE, displayFields, true, -1);

            addDefaultDisplayField(TABLE_DA_MESSAGE, FIELD_DMSG_SERIES_NO, displayFields, true, -1);

            addDefaultDisplayField(TABLE_DA_MESSAGE_TO, FIELD_DMT_READ_BY_USER_ID, displayFields, true, -1);

            addDefaultDisplayField(TABLE_DA_MESSAGE_TO, FIELD_DMT_READ_DATE, displayFields, false, -1);

            addDefaultDisplayField(TABLE_DA_MESSAGE, FIELD_DMSG_RESUBMISSION_DATE, displayFields, false, -1);

            EtkDisplayField displayField = addDefaultDisplayField(TABLE_DA_MESSAGE, FIELD_DMSG_MESSAGE, displayFields, true, -1);
            displayField.setGrowColumn(true);
        }
        return displayFields;
    }

    private EtkDisplayField addDefaultDisplayField(String tableName, String fieldName, EtkDisplayFields displayFields, boolean isColumnFilterEnabled,
                                                   int creatorIndex) {
        EtkDisplayField displayField = new EtkDisplayField(tableName, fieldName, false, false);
        displayField.setColumnFilterEnabled(isColumnFilterEnabled);
        displayField.setDefaultWidth(false);
        displayField.setSizeToFit(true);
        displayField.loadStandards(getConfig());
        if (creatorIndex >= 0) {
            displayFields.addFeldIfNotExists(creatorIndex, displayField);
        } else {
            displayFields.addFeldIfNotExists(displayField);
        }
        return displayField;
    }

    private synchronized void fillGrid(boolean fillGridForSentMessages) {
        MessagesGrid messagesGrid = fillGridForSentMessages ? gridForSentMessages : grid;

        // Spaltenfilter/Sortierung merken
        // Filterung/Sortierung merken
        Object storageSelected = messagesGrid.getFilterAndSortSettings(true);

        // Selektion merken
        List<String> selectedMsgGUIDs = getSelectedMessageGUIDs(messagesGrid);

        // Grid vollständig löschen
        messagesGrid.getTable().clearAllFilterValues();
        messagesGrid.updateFilters();
        if (messagesGrid.getDisplayFields() == null) {
            messagesGrid.setDisplayFields(createDisplayFields(), true);
        } else {
            messagesGrid.clearGrid();
        }

        // Nachrichten zum Grid hinzufügen
        Set<iPartsMailboxHelper.MailboxObject> gridMailboxObjects = fillGridForSentMessages ? sentMailboxObjects : mailboxObjects;
        if (gridMailboxObjects != null) {
            for (iPartsMailboxHelper.MailboxObject mailboxObject : gridMailboxObjects) {
                messagesGrid.addObjectToGrid(mailboxObject.recipient, mailboxObject.mailboxItem);
            }
        }
        messagesGrid.showNoResultsLabel((gridMailboxObjects == null) || gridMailboxObjects.isEmpty());

        // Spaltenfilter/Sortierung wiederherstellen
        messagesGrid.restoreFilterAndSortSettings(storageSelected);

        // Selektion wiederherstellen
        setSelectedMessageGUIDs(selectedMsgGUIDs, messagesGrid);

        doEnableButtons(messagesGrid);
    }

    private void setSelectedMessageGUIDs(List<String> selectedMsgGUIDs, MessagesGrid messagesGrid) {
        if ((selectedMsgGUIDs != null) && !selectedMsgGUIDs.isEmpty()) {
            List<Integer> selectedRowIndices = new DwList<>();
            for (int rowIndex = 0; rowIndex < messagesGrid.getTable().getRowCount(); rowIndex++) {
                GuiTableRow row = messagesGrid.getTable().getRow(rowIndex);
                if (row instanceof DataObjectGrid.GuiTableRowWithObjects) {
                    DataObjectGrid.GuiTableRowWithObjects guiTableRowWithObjects = (DataObjectGrid.GuiTableRowWithObjects)(row);
                    iPartsDataMailboxRecipient mailboxRecipient = iPartsDataMailboxRecipient.getMailboxRecipient(guiTableRowWithObjects.dataObjects);
                    if ((mailboxRecipient != null) && selectedMsgGUIDs.contains(mailboxRecipient.getMsgID())) {
                        selectedRowIndices.add(rowIndex);
                    }
                }
            }
            messagesGrid.getTable().setSelectedRows(Utils.toIntArray(selectedRowIndices), false, true, true);
        }
    }

    private List<String> getSelectedMessageGUIDs(MessagesGrid messagesGrid) {
        List<String> result = new DwList<>();
        for (List<EtkDataObject> dataObjects : messagesGrid.getMultiSelection()) {
            iPartsDataMailboxRecipient mailboxRecipient = iPartsDataMailboxRecipient.getMailboxRecipient(dataObjects);
            if (mailboxRecipient != null) {
                result.add(mailboxRecipient.getMsgID());
            }
        }
        return result;
    }

    private void doEnableButtons(MessagesGrid messagesGrid) {
        List<List<EtkDataObject>> multiSelection = messagesGrid.getMultiSelection();

        // Sichtbarkeit für Anzeigen: nur enabled bei Einzelselektion
        boolean isSingleSelection = (multiSelection != null) && (multiSelection.size() == 1);
        messagesGrid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_VIEW_MESSAGE, isSingleSelection);

        // Sichtbarkeit für Antworten: nur enabled bei Einzelselektion, passendem Recht und echtem Absender
        messagesGrid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_ANSWER_MESSAGE, isAnswerPossible(multiSelection));

        // Sichtbarkeit für Weiterleiten: nur enabled bei Einzelselektion und passendem Recht
        messagesGrid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_FORWARD_MESSAGE, isForwardPossible(multiSelection));
    }

    private boolean isAnswerPossible(List<List<EtkDataObject>> multiSelection) {
        boolean answerPossible = false;
        if ((multiSelection != null) && (multiSelection.size() == 1)) {
            iPartsDataMailboxItem mailboxItem = iPartsDataMailboxItem.getMailboxItem(multiSelection.get(0));
            if (mailboxItem != null) {
                answerPossible = mailboxItem.isAnswerPossible();
            }
        }
        return answerPossible;
    }

    private boolean isForwardPossible(List<List<EtkDataObject>> multiSelection) {
        boolean forwardPossible = false;
        if ((multiSelection != null) && (multiSelection.size() == 1)) {
            iPartsDataMailboxItem mailboxItem = iPartsDataMailboxItem.getMailboxItem(multiSelection.get(0));
            if (mailboxItem != null) {
                forwardPossible = mailboxItem.isForwardPossible();
            }
        }
        return forwardPossible;
    }

    @Override
    public boolean isSecondToolbarVisible() {
        return false;
    }

    @Override
    public void activeFormChanged(AbstractJavaViewerForm newActiveForm, AbstractJavaViewerForm lastActiveForm) {
        if (newActiveForm == this) {
            fillGrid(false);
            fillGrid(true);
        }
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        if (forceUpdateAll || getConnector().isFlagUserInterfaceLanguageChanged() || getConnector().isFlagDatabaseLanguageChanged()) {
            grid.clear();
            loadMessages(false);
            fillGrid(false);

            gridForSentMessages.clear();
            loadMessages(true);
            fillGrid(true);

            updateMainToolbarIcon();
        }
    }

    private synchronized int getUnreadMessagesCount() {
        if (mailboxObjects == null) {
            return 0;
        }
        int unreadCount = 0;
        for (iPartsMailboxHelper.MailboxObject mailboxObject : mailboxObjects) {
            iPartsDataMailboxRecipient recipient = mailboxObject.recipient;
            if ((recipient != null) && !recipient.isMessageRead()) {
                unreadCount++;
            }
        }
        return unreadCount;
    }

    private synchronized void loadMessages(boolean loadSentMessages) {
        if (mailboxHelper.isUserDataValid()) {
            if (loadSentMessages) {
                sentMailboxObjects = mailboxHelper.loadAllSentMessagesForCurrentUser(getProject(), gridForSentMessages.showReadMessagesButton.isPressed());
            } else {
                mailboxObjects = mailboxHelper.loadAllMessagesForCurrentUser(getProject(), grid.showReadMessagesButton.isPressed());
            }
        }
    }

    private void updateMainToolbarIcon() {
        // Main Toolbar Icon auch aktualisieren
        if (getMainForm() != null) {
            if (DWLayoutManager.get().isResponsiveMode()) { // Responsive
                JavaViewerMainWindow.MainButtonInfoWithActionItem mainButtonInfo = getMainForm().getMainButtonInfo(MAIN_TOOLBAR_ALIAS);
                if (mainButtonInfo != null) {
                    mainButtonInfo.getMainButtonInfo().setImages(() -> getMainToolbarIcon().toRButtonImages());
                }
            } else { // Legacy
                if (getMainForm().getMainHeader() != null) {
                    EtkMainToolbarManager mainToolbarManager = getMainForm().getMainHeader().getMainToolbarManager();
                    if (mainToolbarManager != null) {
                        AbstractGuiToolComponent guiToolComponent = mainToolbarManager.getButton(MAIN_TOOLBAR_ALIAS);
                        if (guiToolComponent instanceof EtkToolbarButton) {
                            EtkToolbarButton toolbarButton = (EtkToolbarButton)guiToolComponent;
                            toolbarButton.setGlyphs(getMainToolbarIcon());
                        }
                    }
                }
            }
        }
    }

    public ToolButtonImages getMainToolbarIcon() {
        boolean isResponsive = DWLayoutManager.get().isResponsiveMode();
        int unreadMessagesCount = getUnreadMessagesCount();
        if (unreadMessagesCount > 0) {
            RButtonImages images = new ToolButtonImages(isResponsive ? EditDefaultImages.edit_mailbox_ToolbarButtonGray.getImage()
                                                                     : EditDefaultImages.edit_mailbox_ToolbarButton.getImage(),
                                                        isResponsive ? EditDefaultImages.edit_mailbox_ToolbarButton.getImage()
                                                                     : null,
                                                        isResponsive ? EditDefaultImages.edit_mailbox_ToolbarButton.getImage()
                                                                     : EditDefaultImages.edit_mailbox_ToolbarButtonWhite.getImage(),
                                                        null).toRButtonImages();

            ThemeHelper helper = new ThemeHelper();
            helper.initBadgeParameter(10, 8, iPartsEditPlugin.clPlugin_iPartsEdit_UnreadMessagesBadgeBackgroundColor.getColor(),
                                      iPartsEditPlugin.clPlugin_iPartsEdit_UnreadMessagesBadgeForegroundColor.getColor());

            RButtonImages imagesWithNumber = helper.createNumberOnImages(images, unreadMessagesCount);
            return new ToolButtonImages(imagesWithNumber.getImage(), imagesWithNumber.getImageHover(), imagesWithNumber.getImageChosen(),
                                        imagesWithNumber.getImageHover());
        } else {
            return new ToolButtonImages(EditDefaultImages.edit_mailboxEmpty_ToolbarButton.getImage(),
                                        isResponsive ? EditDefaultImages.edit_mailbox_ToolbarButton.getImage()
                                                     : null,
                                        isResponsive ? EditDefaultImages.edit_mailbox_ToolbarButton.getImage()
                                                     : null,
                                        null);
        }
    }

    private void handleMailboxChangedEvent(iPartsMailboxChangedEvent event) {
        if (mailboxHelper.isUserDataValid()) {
            // Empfangene Nachrichten
            Set<String> relevantMessageGUIDs = mailboxHelper.filterRelevantReceivedMessagesFromEvent(event);
            handleMailboxChangedEventMessages(event, relevantMessageGUIDs, false);

            // Gesendete Nachrichten
            relevantMessageGUIDs = mailboxHelper.filterRelevantSentMessagesFromEvent(event);
            handleMailboxChangedEventMessages(event, relevantMessageGUIDs, true);
        }
    }

    private synchronized void handleMailboxChangedEventMessages(iPartsMailboxChangedEvent event, Set<String> relevantMessageGUIDs,
                                                                boolean handleSentMessages) {
        if (!relevantMessageGUIDs.isEmpty()) {
            if (relevantMessageGUIDs.size() > 1) {
                loadMessages(handleSentMessages);
            } else {
                String messageGUID = relevantMessageGUIDs.iterator().next();
                iPartsDataMailboxRecipientList recipients = new iPartsDataMailboxRecipientList();
                recipients.loadAllMailboxMessagesForGUID(getProject(), messageGUID, DBActionOrigin.FROM_DB);
                Set<iPartsMailboxHelper.MailboxObject> modifiedMailboxObjects = mailboxHelper.createEmptyMailboxObjectsSet();
                iPartsMailboxHelper.addAll(recipients, modifiedMailboxObjects);
                modifiedMailboxObjects = handleSentMessages ? mailboxHelper.filterRelevantSentMessagesForCurrentUser(modifiedMailboxObjects)
                                                            : mailboxHelper.filterRelevantReceivedMessagesForCurrentUser(modifiedMailboxObjects);

                if (handleSentMessages) {
                    if (sentMailboxObjects == null) {
                        sentMailboxObjects = mailboxHelper.createEmptyMailboxObjectsSet();
                    }
                } else {
                    if (mailboxObjects == null) {
                        mailboxObjects = mailboxHelper.createEmptyMailboxObjectsSet();
                    }
                }
                Set<iPartsMailboxHelper.MailboxObject> gridMailboxObjects = handleSentMessages ? sentMailboxObjects : mailboxObjects;
                if (event.getMailboxItemState() == iPartsMailboxChangedEvent.MailboxItemState.NEW) {
                    gridMailboxObjects.addAll(modifiedMailboxObjects);
                } else {
                    Iterator<iPartsMailboxHelper.MailboxObject> mailboxObjectIterator = gridMailboxObjects.iterator();
                    while (mailboxObjectIterator.hasNext()) {
                        iPartsMailboxHelper.MailboxObject mailboxObject = mailboxObjectIterator.next();
                        if (mailboxObject.recipient.getMsgID().equals(messageGUID)) {
                            mailboxObjectIterator.remove();
                        }
                    }

                    MessagesGrid messagesGrid = handleSentMessages ? gridForSentMessages : grid;
                    if (messagesGrid.showReadMessagesButton.isPressed() || (event.getMailboxItemState() == iPartsMailboxChangedEvent.MailboxItemState.UNREAD)) {
                        gridMailboxObjects.addAll(modifiedMailboxObjects);
                    }
                }
            }

            if (getConnector().getActiveForm() == this) {
                fillGrid(handleSentMessages);
            }
            if (!handleSentMessages) {
                updateMainToolbarIcon();
            }
        }
    }

    private void toggleMailboxItemRead(List<List<EtkDataObject>> selection) {
        VarParam<Boolean> isRead = new VarParam<>();
        boolean distinctValidReadState = iPartsMailboxHelper.isDistinctValidReadState(selection, isRead);
        if (distinctValidReadState) {
            GenericEtkDataObjectList objectsForSave = new GenericEtkDataObjectList();
            iPartsMailboxChangedEvent event = new iPartsMailboxChangedEvent();
            for (List<EtkDataObject> dataObjects : selection) {
                iPartsDataMailboxItem mailboxItem = iPartsDataMailboxItem.getMailboxItem(dataObjects);
                if (isRead.getValue()) { // Bisher gelesen -> ungelesen
                    mailboxItem.setFieldValue(FIELD_DMSG_RESUBMISSION_DATE, "", DBActionOrigin.FROM_EDIT);
                    if (mailboxItem.isModified()) {
                        objectsForSave.add(mailboxItem, DBActionOrigin.FROM_EDIT);
                    }
                }
                iPartsDataMailboxRecipient mailboxRecipient = iPartsDataMailboxRecipient.getMailboxRecipient(dataObjects);
                if ((mailboxItem != null) && (mailboxRecipient != null)) {
                    mailboxRecipient.setMessageRead(!isRead.getValue());
                    objectsForSave.add(mailboxRecipient, DBActionOrigin.FROM_EDIT);
                    event.addMailboxRecipient(mailboxItem.getFieldValue(FIELD_DMSG_CREATION_USER_ID), mailboxRecipient);
                }
            }

            EtkDbs etkDbs = getProject().getEtkDbs();
            etkDbs.startTransaction(); // BatchStatement wird innerhalb von objectsForSave.saveToDB() sowieso verwendet
            try {
                objectsForSave.saveToDB(getProject());
                etkDbs.commit();
            } catch (Exception e) {
                etkDbs.rollback();
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                MessageDialog.showError("!!Fehler beim Speichern.", "!!Postkorb");
                return;
            }
            event.setMailboxItemReadState(!isRead.getValue());

            ApplicationEvents.fireEventInAllProjectsAndAllClusters(event);
        }
    }

    private void doCreateMailboxItem(Event event) {
        EditUserControlForMailboxItem.createMailboxItem("!!Neue Nachricht erstellen", getConnector(), this);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public GuiPanel getGui() {
        return mainWindow.mainPanel;
    }

    public GuiWindow getMainWindow() {
        return mainWindow;
    }


    /**
     * Grid für Nachrichten inkl. ToolBarButtons und Popup-Menüeinträgen
     */
    private class MessagesGrid extends EditDataObjectFilterGrid {

        private boolean gridForSentMessages;
        private GuiToolButton showReadMessagesButton;
        private GuiMenuItem contextMenuAnswer;
        private GuiMenuItem contextMenuForward;
        private GuiMenuItem contextMenuSetAllRead;
        private GuiMenuItem contextMenuSetAllUnread;

        public MessagesGrid(boolean gridForSentMessages, AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
            super(dataConnector, parentForm);
            this.gridForSentMessages = gridForSentMessages;
            if (gridForSentMessages) {
                getToolbarHelper().hideToolbarButton(EditToolbarButtonAlias.EDIT_ANSWER_MESSAGE);
                contextMenuAnswer.setVisible(false);
                contextMenuSetAllRead.setVisible(false);
                contextMenuSetAllUnread.setVisible(false);
            }
            if (!iPartsRight.CREATE_MAILBOX_MESSAGES.checkRightInSession()) {
                getToolbarHelper().hideToolbarButton(EditToolbarButtonAlias.EDIT_ANSWER_MESSAGE);
                getToolbarHelper().hideToolbarButton(EditToolbarButtonAlias.EDIT_FORWARD_MESSAGE);
                contextMenuAnswer.setVisible(false);
                contextMenuForward.setVisible(false);
            }
            showToolbar(true);
            setColumnFilterFactory(new VirtualUserGroupDataObjectColumnFilterFactory(dataConnector.getProject(), iPartsDataMailboxRecipient.class,
                                                                                     new String[]{ FIELD_DMT_GROUP_ID },
                                                                                     new String[]{ FIELD_DMT_USER_ID, FIELD_DMT_READ_BY_USER_ID,
                                                                                                   FIELD_DMSG_CREATION_USER_ID },
                                                                                     new String[]{ FIELD_DMT_ROLE_ID },
                                                                                     new String[]{ FIELD_DMT_ORGANISATION_ID }));
        }

        @Override
        protected void createToolbarButtons(GuiToolbar toolbar) {
            getToolbarHelper().addToolbarButton(EditToolbarButtonAlias.EDIT_VIEW_MESSAGE, new EventListener(Event.ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(Event event) {
                    doViewMailboxItem();
                }
            });

            getToolbarHelper().addToolbarButton(EditToolbarButtonAlias.EDIT_ANSWER_MESSAGE, new EventListener(Event.ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(Event event) {
                    doAnswerMailboxItem();
                }
            });

            getToolbarHelper().addToolbarButton(EditToolbarButtonAlias.EDIT_FORWARD_MESSAGE, new EventListener(Event.ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(Event event) {
                    doForwardMailboxItem();
                }
            });

            showReadMessagesButton = getToolbarHelper().addToolbarButton(EditToolbarButtonAlias.EDIT_SHOW_READ_MESSAGES, new EventListener(Event.ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(Event event) {
                    GuiToolButton source = (GuiToolButton)event.getSource();
                    if (source.isPressed()) {
                        source.setTooltip("!!Nur ungelesene Nachrichten anzeigen");
                    } else {
                        source.setTooltip("!!Auch gelesene Nachrichten anzeigen");
                    }
                    loadMessages(gridForSentMessages);
                    fillGrid(gridForSentMessages);
                }
            });

            if (iPartsRight.CREATE_MAILBOX_MESSAGES.checkRightInSession()) {
                GuiToolButton guiToolButton = getToolbarHelper().addToolbarButton(EditToolbarButtonAlias.IMG_NEW, new EventListener(Event.ACTION_PERFORMED_EVENT) {
                    @Override
                    public void fire(Event event) {
                        doCreateMailboxItem(event);
                    }
                });
                guiToolButton.setTooltip("!!Neue Nachricht erstellen");
            }

            GuiToolButton guiToolButton = getToolbarHelper().addToolbarButton(EditToolbarButtonAlias.IMG_REFRESH, new EventListener(Event.ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(Event event) {
                    loadMessages(gridForSentMessages);
                    fillGrid(gridForSentMessages);
                    updateMainToolbarIcon();
                }
            });
            guiToolButton.setTooltip("!!Nachrichten aktualisieren");
        }

        @Override
        protected void onCellDblClicked(int row, int col, Event event) {
            doViewMailboxItem();
        }

        @Override
        protected void onTableSelectionChanged(Event event) {
            //super.onTableSelectionChanged(event);
            updateContextMenuForSelection();
            doEnableButtons(this);
        }

        @Override
        protected void createContextMenuItems(GuiContextMenu contextMenu) {
            contextMenuAnswer = new GuiMenuItem();
            contextMenuAnswer.setText("!!Antworten");
            contextMenuAnswer.setIcon(EditDefaultImages.edit_mailbox_answer.getImage());
            contextMenuAnswer.setEnabled(false);
            contextMenuAnswer.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    doAnswerMailboxItem();
                }
            });
            contextMenu.addChild(contextMenuAnswer);

            contextMenuForward = new GuiMenuItem();
            contextMenuForward.setText("!!Weiterleiten");
            contextMenuForward.setIcon(EditDefaultImages.edit_mailbox_forward.getImage());
            contextMenuForward.setEnabled(false);
            contextMenuForward.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    doForwardMailboxItem();
                }
            });
            contextMenu.addChild(contextMenuForward);

            contextMenuSetAllRead = new GuiMenuItem();
            contextMenuSetAllRead.setText("!!Als gelesen markieren");
            contextMenuSetAllRead.setIcon(EditDefaultImages.edit_mailbox_read.getImage());
            contextMenuSetAllRead.setVisible(false);
            contextMenuSetAllRead.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    toggleMailboxItemRead(getMultiSelection());
                }
            });
            contextMenu.addChild(contextMenuSetAllRead);

            contextMenuSetAllUnread = new GuiMenuItem();
            contextMenuSetAllUnread.setText("!!Als ungelesen markieren");
            contextMenuSetAllUnread.setIcon(EditDefaultImages.edit_mailbox_unread.getImage());
            contextMenuSetAllUnread.setVisible(false);
            contextMenuSetAllUnread.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    toggleMailboxItemRead(getMultiSelection());
                }
            });
            contextMenu.addChild(contextMenuSetAllUnread);
        }

        private void updateContextMenuForSelection() {
            List<List<EtkDataObject>> multiSelection = getMultiSelection();
            if (!gridForSentMessages) {
                VarParam<Boolean> isRead = new VarParam<>();
                if (iPartsMailboxHelper.isDistinctValidReadState(multiSelection, isRead)) {
                    if (isRead.getValue()) {
                        // Alle Nachrichten sind gelesen -> Menüeintrag "als ungelesen markieren"
                        contextMenuSetAllUnread.setVisible(true);
                        contextMenuSetAllUnread.setEnabled(true);
                        contextMenuSetAllRead.setVisible(false);
                    } else {
                        // Alle Nachrichten sind ungelesen -> Menüeintrag "als gelesen markieren"
                        contextMenuSetAllUnread.setVisible(false);
                        contextMenuSetAllRead.setVisible(true);
                        contextMenuSetAllRead.setEnabled(true);
                    }
                } else {
                    // Mischzustand oder leere Selektion -> beide Menüeinträge disabled anzeigen
                    contextMenuSetAllUnread.setVisible(true);
                    contextMenuSetAllUnread.setEnabled(false);
                    contextMenuSetAllRead.setVisible(true);
                    contextMenuSetAllRead.setEnabled(false);
                }

                contextMenuAnswer.setEnabled(isAnswerPossible(multiSelection));
            }
            contextMenuForward.setEnabled((multiSelection != null) && (multiSelection.size() == 1));
        }

        @Override
        protected DataObjectGrid.GuiTableRowWithObjects createRow(List<EtkDataObject> dataObjects) {
            boolean messageRead = false;
            iPartsDataMailboxRecipient mailboxRecipient = iPartsDataMailboxRecipient.getMailboxRecipient(dataObjects);
            if (mailboxRecipient != null) {
                messageRead = mailboxRecipient.isMessageRead();
            }

            DataObjectGrid.GuiTableRowWithObjects row = new DataObjectGrid.GuiTableRowWithObjects(dataObjects);

            for (EtkDisplayField field : displayFields.getFields()) {
                if (field.isVisible()) {
                    String fieldName = field.getKey().getFieldName();
                    String tableName = field.getKey().getTableName();

                    // Welches Object ist für diese Tabelle zuständig?
                    EtkDataObject objectForTable = row.getObjectForTable(tableName);

                    String value = getVisualValueOfField(tableName, fieldName, objectForTable);
                    GuiLabel label = new GuiLabel(value);
                    if (!messageRead) {
                        label.setFontStyle(DWFontStyle.BOLD);
                    }
                    row.addChild(label);
                }
            }
            return row;
        }

        @Override
        protected String getVisualValueOfField(String tableName, String fieldName, EtkDataObject objectForTable) {
            if (tableName.equals(TABLE_DA_MESSAGE_TO)) {
                if (fieldName.equals(FIELD_DMT_USER_ID) || fieldName.equals(FIELD_DMT_READ_BY_USER_ID)) {
                    return iPartsMailboxHelper.getFullUserNameOrTechnicalUser(objectForTable.getFieldValue(fieldName), getProject().getDBLanguage());
                } else if (fieldName.equals(FIELD_DMT_GROUP_ID)) {
                    String virtualUserGroupId = objectForTable.getFieldValue(fieldName);
                    return iPartsVirtualUserGroup.getVirtualUserGroupName(virtualUserGroupId);
                } else if (fieldName.equals(FIELD_DMT_ROLE_ID)) {
                    String roleId = objectForTable.getFieldValue(fieldName);
                    if (StrUtils.isValid(roleId)) {
                        return UserAdminRoleCache.getInstance(roleId).getRoleName(getProject().getDBLanguage());
                    } else {
                        return "";
                    }
                } else if (fieldName.equals(FIELD_DMT_ORGANISATION_ID)) {
                    String orgId = objectForTable.getFieldValue(fieldName);
                    if (StrUtils.isValid(orgId)) {
                        return iPartsUserAdminOrgCache.getInstance(orgId).getOrgName(getProject().getDBLanguage());
                    } else {
                        return "";
                    }
                }
            } else if (tableName.equals(TABLE_DA_MESSAGE)) {
                if (fieldName.equals(FIELD_DMSG_CREATION_USER_ID)) {
                    return iPartsMailboxHelper.getFullUserNameOrTechnicalUser(objectForTable.getFieldValue(fieldName), getProject().getDBLanguage());
                } else if (fieldName.equals(FIELD_DMSG_SUBJECT)) {
                    String message = objectForTable.getFieldValue(fieldName);
                    return StrUtils.makeAbbreviationWithSuffix(message, MAX_MESSAGE_PREVIEW_LENGTH);
                } else if (fieldName.equals(FIELD_DMSG_MESSAGE)) {
                    String message = objectForTable.getFieldValue(fieldName);
                    return StrUtils.makeAbbreviationWithSuffix(message, MAX_MESSAGE_PREVIEW_LENGTH);
                }
            }
            return super.getVisualValueOfField(tableName, fieldName, objectForTable);
        }

        private void doViewMailboxItem() {
            iPartsDataMailboxItem mailboxItem = iPartsDataMailboxItem.getMailboxItem(getSelection());
            if (mailboxItem != null) {
                iPartsDataMailboxRecipient mailboxRecipient = iPartsDataMailboxRecipient.getMailboxRecipient(getSelection());
                if (EditUserControlForMailboxItem.showMailboxItem(getConnector(), this, mailboxItem, mailboxRecipient, gridForSentMessages)
                    && !gridForSentMessages) {
                    toggleMailboxItemRead(getMultiSelection());
                }
            }
        }

        private void doAnswerMailboxItem() {
            iPartsDataMailboxItem mailboxItem = iPartsDataMailboxItem.getMailboxItem(getSelection());
            if (mailboxItem != null) {
                iPartsDataMailboxRecipient mailboxRecipient = iPartsDataMailboxRecipient.getMailboxRecipient(getSelection());
                EditUserControlForMailboxItem.answerMailboxItem(mailboxItem, mailboxRecipient, getConnector(), getParentForm());
            }
        }

        private void doForwardMailboxItem() {
            iPartsDataMailboxItem mailboxItem = iPartsDataMailboxItem.getMailboxItem(getSelection());
            if (mailboxItem != null) {
                iPartsDataMailboxRecipient mailboxRecipient = iPartsDataMailboxRecipient.getMailboxRecipient(getSelection());
                EditUserControlForMailboxItem.forwardMailboxItem(mailboxItem, mailboxRecipient, getConnector(), getParentForm());
            }
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
        private de.docware.framework.modules.gui.controls.GuiPanel centerPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel mainPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel topPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel contentPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane messagesGridsSplitPane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiDockingPanel receivedMessagesDockingPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel receivedMessagesPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiDockingPanel sentMessagesDockingPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel sentMessagesPanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setTitle("!!Postkorb");
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
            centerPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            centerPanel.setName("centerPanel");
            centerPanel.__internal_setGenerationDpi(96);
            centerPanel.registerTranslationHandler(translationHandler);
            centerPanel.setScaleForResolution(true);
            centerPanel.setMinimumWidth(10);
            centerPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder centerPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            centerPanel.setLayout(centerPanelLayout);
            title = new de.docware.framework.modules.gui.controls.GuiTitle();
            title.setName("title");
            title.__internal_setGenerationDpi(96);
            title.registerTranslationHandler(translationHandler);
            title.setScaleForResolution(true);
            title.setMinimumWidth(10);
            title.setMinimumHeight(50);
            title.setTitle("!!Postkorb");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleConstraints.setPosition("north");
            title.setConstraints(titleConstraints);
            centerPanel.addChild(title);
            mainPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            mainPanel.setName("mainPanel");
            mainPanel.__internal_setGenerationDpi(96);
            mainPanel.registerTranslationHandler(translationHandler);
            mainPanel.setScaleForResolution(true);
            mainPanel.setMinimumWidth(10);
            mainPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder mainPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            mainPanel.setLayout(mainPanelLayout);
            topPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            topPanel.setName("topPanel");
            topPanel.__internal_setGenerationDpi(96);
            topPanel.registerTranslationHandler(translationHandler);
            topPanel.setScaleForResolution(true);
            topPanel.setMinimumWidth(10);
            topPanel.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.LayoutBorder topPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            topPanel.setLayout(topPanelLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder topPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            topPanelConstraints.setPosition("north");
            topPanel.setConstraints(topPanelConstraints);
            mainPanel.addChild(topPanel);
            contentPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            contentPanel.setName("contentPanel");
            contentPanel.__internal_setGenerationDpi(96);
            contentPanel.registerTranslationHandler(translationHandler);
            contentPanel.setScaleForResolution(true);
            contentPanel.setMinimumWidth(10);
            contentPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder contentPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            contentPanel.setLayout(contentPanelLayout);
            messagesGridsSplitPane = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            messagesGridsSplitPane.setName("messagesGridsSplitPane");
            messagesGridsSplitPane.__internal_setGenerationDpi(96);
            messagesGridsSplitPane.registerTranslationHandler(translationHandler);
            messagesGridsSplitPane.setScaleForResolution(true);
            messagesGridsSplitPane.setMinimumWidth(10);
            messagesGridsSplitPane.setMinimumHeight(10);
            messagesGridsSplitPane.setHorizontal(false);
            messagesGridsSplitPane.setDividerPosition(197);
            messagesGridsSplitPane.setResizeWeight(0.5);
            receivedMessagesDockingPanel = new de.docware.framework.modules.gui.controls.GuiDockingPanel();
            receivedMessagesDockingPanel.setName("receivedMessagesDockingPanel");
            receivedMessagesDockingPanel.__internal_setGenerationDpi(96);
            receivedMessagesDockingPanel.registerTranslationHandler(translationHandler);
            receivedMessagesDockingPanel.setScaleForResolution(true);
            receivedMessagesDockingPanel.setMinimumWidth(10);
            receivedMessagesDockingPanel.setMinimumHeight(10);
            receivedMessagesDockingPanel.setTextHide("!!Empfangene Nachrichten");
            receivedMessagesDockingPanel.setTextShow("!!Empfangene Nachrichten anzeigen");
            receivedMessagesDockingPanel.setImageHide(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelNorth"));
            receivedMessagesDockingPanel.setImageShow(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelSouth"));
            receivedMessagesDockingPanel.setDockPosition(de.docware.framework.modules.gui.controls.misc.DWBorderPosition.NORTH);
            receivedMessagesDockingPanel.setButtonBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignButtonDisabledBackgroundGradient1"));
            receivedMessagesDockingPanel.setButtonForegroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignButtonBorderSelected"));
            receivedMessagesDockingPanel.setButtonFill(true);
            receivedMessagesDockingPanel.setStartWithArrow(false);
            receivedMessagesPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            receivedMessagesPanel.setName("receivedMessagesPanel");
            receivedMessagesPanel.__internal_setGenerationDpi(96);
            receivedMessagesPanel.registerTranslationHandler(translationHandler);
            receivedMessagesPanel.setScaleForResolution(true);
            receivedMessagesPanel.setMinimumWidth(10);
            receivedMessagesPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder receivedMessagesPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            receivedMessagesPanel.setLayout(receivedMessagesPanelLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder receivedMessagesPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            receivedMessagesPanel.setConstraints(receivedMessagesPanelConstraints);
            receivedMessagesDockingPanel.addChild(receivedMessagesPanel);
            messagesGridsSplitPane.addChild(receivedMessagesDockingPanel);
            sentMessagesDockingPanel = new de.docware.framework.modules.gui.controls.GuiDockingPanel();
            sentMessagesDockingPanel.setName("sentMessagesDockingPanel");
            sentMessagesDockingPanel.__internal_setGenerationDpi(96);
            sentMessagesDockingPanel.registerTranslationHandler(translationHandler);
            sentMessagesDockingPanel.setScaleForResolution(true);
            sentMessagesDockingPanel.setMinimumWidth(10);
            sentMessagesDockingPanel.setMinimumHeight(10);
            sentMessagesDockingPanel.setTextHide("!!Gesendete Nachrichten");
            sentMessagesDockingPanel.setTextShow("!!Gesendete Nachrichten anzeigen");
            sentMessagesDockingPanel.setImageHide(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelNorth"));
            sentMessagesDockingPanel.setImageShow(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelSouth"));
            sentMessagesDockingPanel.setDockPosition(de.docware.framework.modules.gui.controls.misc.DWBorderPosition.NORTH);
            sentMessagesDockingPanel.setButtonBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignButtonDisabledBackgroundGradient1"));
            sentMessagesDockingPanel.setButtonForegroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignButtonBorderSelected"));
            sentMessagesDockingPanel.setButtonFill(true);
            sentMessagesDockingPanel.setStartWithArrow(false);
            sentMessagesPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            sentMessagesPanel.setName("sentMessagesPanel");
            sentMessagesPanel.__internal_setGenerationDpi(96);
            sentMessagesPanel.registerTranslationHandler(translationHandler);
            sentMessagesPanel.setScaleForResolution(true);
            sentMessagesPanel.setMinimumWidth(10);
            sentMessagesPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder sentMessagesPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            sentMessagesPanel.setLayout(sentMessagesPanelLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder sentMessagesPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            sentMessagesPanel.setConstraints(sentMessagesPanelConstraints);
            sentMessagesDockingPanel.addChild(sentMessagesPanel);
            messagesGridsSplitPane.addChild(sentMessagesDockingPanel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder messagesGridsSplitPaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            messagesGridsSplitPane.setConstraints(messagesGridsSplitPaneConstraints);
            contentPanel.addChild(messagesGridsSplitPane);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder contentPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            contentPanel.setConstraints(contentPanelConstraints);
            mainPanel.addChild(contentPanel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder mainPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            mainPanel.setConstraints(mainPanelConstraints);
            centerPanel.addChild(mainPanel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder centerPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            centerPanel.setConstraints(centerPanelConstraints);
            this.addChild(centerPanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}
