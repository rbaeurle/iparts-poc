/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.picorder;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.DataObjectFilterGrid;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.forms.common.components.MenuRunnable;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModuleEinPASList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProductModulesList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.MQHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.MQPicScheduler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.AbstractXMLMessageListener;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.iPartsMQMessageManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractMQMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.XMLObjectCreationHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.event.AbstractXMLChangeEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.event.iPartsXMLEventReleaseStatusChange;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.event.iPartsXMLTcObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.iPartsXMLMediaMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.iPartsXMLAbortMediaOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.iPartsXMLRequestor;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.iPartsXMLSearchMediaContainers;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.PicOrderStatusChangeEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditAssemblyImageForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.RequestPicturesForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiWindow;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.table.GuiTable;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolbar;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogButtons;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogIcon;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerOptions;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.dwr.DocwareDwrLogger;
import de.docware.framework.modules.gui.output.j2ee.misc.StaticConnectionUpdater;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.j2ee.EC;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.misc.observer.ObserverCall;
import de.docware.util.misc.observer.ObserverCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Form zur Darstellung der Bildaufträge für ein Modul
 */
public class PicOrdersToModuleGridForm extends AbstractJavaViewerForm implements iPartsConst {

    private final AssemblyId assemblyId;
    private PicOrderDataObjectGrid dataGrid;
    private boolean isEditAllowed;
    private AbstractXMLMessageListener xmlMessageListener;
    private AbstractXMLMessageListener updatePicOrderEventListener;
    private String[] messageIdsForPicOrderUpdates;
    private List<iPartsASPLMItemId> messageIdsForEvents;
    private EventListener listenerNewPicOrder = null;
    private EventListener listenerRefreshPicOrder = null;
    private GuiMenuItem menuItemOrderWork;
    private GuiMenuItem menuItemDeleteOrder;
    private GuiMenuItem menuItemResendMCSearch;
    private GuiMenuItem menuItemCreateChangeOrder;
    private GuiMenuItem menuItemAbortMediaOrder;
    private GuiMenuItem menuItemOpenMCInASPLM;
    private GuiMenuItem menuItemRetrievePicsForPicOrder;
    private GuiMenuItem menuItemCopyPicOrder;
    private boolean showAllPicOrder;

    /**
     * Sucht und öffnet den übergebenen Mediencontainer in AS-PLM in einem nicht-modalen Fenster.
     *
     * @param mediaContainer
     */
    public static void openMediaContainerInASPLM(String mediaContainer) {
        if (!MQHelper.checkTransmissionToASPLMConfigWithMessage()) {
            return;
        }
        if (StrUtils.isEmpty(mediaContainer)) {
            MessageDialog.show("!!Kein Mediencontainer selektiert.", EditToolbarButtonAlias.IMG_OPEN_MC_IN_ASPLM.getTooltip());
            return;
        }

        String openUri = iPartsEditPlugin.getPluginConfig().getConfigValueAsString(iPartsEditPlugin.CONFIG_ASPLM_OPEN_MEDIA_CONTAINER_URI);
        if (!openUri.contains("{{MC}}")) {
            MessageDialog.showError("!!In der URI für das Öffnen eines Mediencontainers in AS-PLM fehlt der Platzhalter \"{{MC}}\".",
                                    EditToolbarButtonAlias.IMG_OPEN_MC_IN_ASPLM.getTooltip());
            return;
        }

        // Platzhalter ersetzen und nicht-modales Fenster wie bei der Online-Hilfe direkt mit einer URL öffnen
        openUri = openUri.replace("{{MC}}", mediaContainer);
        Session session = Session.get();
        if (session != null) {
            GuiWindow masterRootWindow = session.getMasterRootWindow();
            if (masterRootWindow != null) {
                DocwareDwrLogger guiLogger = masterRootWindow.getGuiLogger();
                if (guiLogger != null) {
                    String windowId = session.getId() + "_ASPLM_MC_" + mediaContainer;
                    guiLogger.addAjaxCommand_evaluateJavascript("dwFrameworkOpenWindow('" + EC.jjs(windowId) + "', '"
                                                                + EC.jjs(openUri) + "', 1024, 768, true);");
                }
            }
        }
    }

    /**
     * Erzeugt eine Instanz von EditShowPictureOrderTwoForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public PicOrdersToModuleGridForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, AssemblyId assemblyId) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.assemblyId = assemblyId;
        postCreateGui();

        setEditAllowed(true);
        // Listener für die Antwort von GetMediaOrder, GetMediaContent, CreateMcAttachment oder CorrectMediaOrder
        addXMLListener();
        addChangeOrderListener();
        updateTablePictureOrder();
    }

    /**
     * Erzeugt die Listener für XML Nachrichten von AS-PLM
     */
    private void addXMLListener() {
        xmlMessageListener = new AbstractXMLMessageListener() {
            @Override
            public boolean messageReceived(AbstractMQMessage xmlMQMessage) {
                updateTablePictureOrder();
                StaticConnectionUpdater.updateBrowser(getGui()); // Push der GUI-Aktualisierung vom Server zum Client
                // Kam eine Suchanfrage zurück, dann muss das Grid mit den Zeichnungen aktualisiert werden (MC Nummer setzen)
                if (xmlMQMessage instanceof iPartsXMLMediaMessage) {
                    iPartsXMLMediaMessage mediaMessage = (iPartsXMLMediaMessage)xmlMQMessage;
                    if (mediaMessage.isResponse() && (mediaMessage.getResponse().getRequestOperation() == iPartsTransferNodeTypes.SEARCH_MEDIA_CONTAINERS)) {
                        // Event erzeugen, damit die MC Nummer im Grid der Zeichnungen auftaucht
                        Event event = new Event(Event.GENERIC_TYPE);
                        event.addParameter(EditAssemblyImageForm.RELOAD_PICTURES, true);
                        listenerRefreshPicOrder.fire(event);
                    }
                }
                return false;
            }
        };
        // Listener für EventReleaseStatusChange-Meldungen
        updatePicOrderEventListener = new AbstractXMLMessageListener() {
            @Override
            public boolean messageReceived(AbstractMQMessage xmlMQMessage) {
                if (xmlMQMessage.isOfType(iPartsXMLMediaMessage.TYPE)) {
                    iPartsXMLMediaMessage mediaMessage = (iPartsXMLMediaMessage)xmlMQMessage;
                    if (mediaMessage.isEvent()) {
                        AbstractXMLChangeEvent actualEvent = mediaMessage.getEvent().getActualEvent();
                        iPartsXMLTcObject tcObject = actualEvent.getTcObject();
                        iPartsASPLMItemId itemId = new iPartsASPLMItemId(tcObject.getMcItemId(), tcObject.getMcItemRevId());
                        boolean doUpdate;
                        synchronized (PicOrdersToModuleGridForm.this) {
                            doUpdate = (messageIdsForEvents != null) && messageIdsForEvents.contains(itemId);
                        }
                        if (doUpdate) {
                            if (isEventWithRequiredPicRefresh(actualEvent, itemId)) {
                                doRefreshPicOrder(createReloadPicturesEvent());
                            } else {
                                updateTablePictureOrder();
                            }
                            StaticConnectionUpdater.updateBrowser(getGui()); // Push der GUI-Aktualisierung vom Server zum Client
                        }
                    }
                }
                return false;
            }
        };
    }

    /**
     * Erzeugt einen Event, der dafür sorgt, dass die Bilder in der Edit-Bildtabelle neu geladen werden
     *
     * @return
     */
    private Event createReloadPicturesEvent() {
        Event event = new Event(Event.GENERIC_TYPE);
        event.addParameter(EditAssemblyImageForm.RELOAD_PICTURES, true);
        return event;
    }

    /**
     * Erzeugt einen Listener für Änderungen nachdem ein Änderungsauftrag erzeugt wurde
     */
    private void addChangeOrderListener() {
        // Wenn eine Auftrags-Id des Vorgängers existiert, dann nur aktualisieren, wenn dieser Vorgänger
        // in der Liste enthalten ist
        ObserverCallback changeListener = new ObserverCallback(getCallbackBinder(), PicOrderStatusChangeEvent.class) {
            @Override
            public void callback(ObserverCall call) {
                PicOrderStatusChangeEvent event = (PicOrderStatusChangeEvent)call;
                // Wenn eine Auftrags-Id des Vorgängers existiert, dann nur aktualisieren, wenn dieser Vorgänger
                // in der Liste enthalten ist
                if (StrUtils.isValid(event.getPreviousOrderId())) {
                    boolean foundOrder = false;
                    for (iPartsDataPicOrder picorder : getPicOrders()) {
                        if (picorder.getAsId().getOrderGuid().equals(event.getPreviousOrderId())) {
                            foundOrder = true;
                            break;
                        }
                    }
                    if (!foundOrder) {
                        return;
                    }
                }
                if (event.isReloadPictures()) {
                    doRefreshPicOrder(createReloadPicturesEvent());
                } else {
                    doRefreshPicOrder(null);
                }
            }
        };
        getProject().addAppEventListener(changeListener);
    }

    /**
     * Prüft, ob der übergebene Event ein Neuladen der Bilder triggert
     *
     * @param actualEvent
     * @param itemId
     * @return
     */
    private boolean isEventWithRequiredPicRefresh(AbstractXMLChangeEvent actualEvent, iPartsASPLMItemId itemId) {
        boolean result = false;
        if (actualEvent.getEventType() == iPartsTransferNodeTypes.EVENT_RELEASE_STATUS_CHANGE) {
            iPartsEventStates eventState = ((iPartsXMLEventReleaseStatusChange)actualEvent).getNewEventState();
            if (eventState == iPartsEventStates.OBSOLETE) {
                iPartsDataPicOrder picOrderForItemId = findPicOrderForItemId(itemId);
                if (picOrderForItemId != null) {
                    result = picorderHasNonASPLMPicture(picOrderForItemId);
                }
            }
        }
        return result;
    }

    /**
     * Check, ob der übergeben Bildauftrag ein migriertes Bild hat
     *
     * @param picOrderForItemId
     * @return
     */
    private boolean picorderHasNonASPLMPicture(iPartsDataPicOrder picOrderForItemId) {
        if (picOrderForItemId == null) {
            return false;
        }
        for (iPartsDataPicOrderPicture picture : picOrderForItemId.getPictures()) {
            if (!picture.isASPLMPicture()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Liefert den Bildauftrag zur übergebenen {@link iPartsASPLMItemId}
     *
     * @param itemId
     * @return
     */
    private iPartsDataPicOrder findPicOrderForItemId(iPartsASPLMItemId itemId) {
        if (itemId == null) {
            return null;
        }
        for (iPartsDataPicOrder picorder : getPicOrders()) {
            if (picorder.getOrderItemId().equals(itemId)) {
                return picorder;
            }
        }
        return null;
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        dataGrid = new PicOrderDataObjectGrid(getConnector(), this) {
            @Override
            protected void onTableSelectionChanged(Event event) {
                doSetMenuEntries(event);
                doEnableButtons();
            }

            @Override
            protected void onCellDblClicked(int row, int col, Event event) {
                onMouseDblClickEvent(event);
            }

            @Override
            protected void createToolbarButtons(GuiToolbar toolbar) {
                ToolbarButtonMenuHelper.ToolbarMenuHolder holder;
                holder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.IMG_PICORDER, getUITranslationHandler(), new MenuRunnable() {
                    public void run(Event event) {
                        doNewPicOrder(event);
                    }
                });
                contextmenuTablePictureOrder.addChild(holder.menuItem);
                getToolbarHelper().hideToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_PICORDER, contextmenuTablePictureOrder);
                holder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.IMG_REFRESH, getUITranslationHandler(), new MenuRunnable() {
                    public void run(Event event) {
                        doRefreshPicOrder(event);
                    }
                });
                contextmenuTablePictureOrder.addChild(holder.menuItem);

                menuItemOrderWork = getToolbarHelper().createMenuEntry("menuOrderWork", "!!Bildauftrag bearbeiten", null, new EventListener(Event.MENU_ITEM_EVENT) {
                    @Override
                    public void fire(Event event) {
                        doEditOrder(event);
                    }
                }, getUITranslationHandler());
                contextmenuTablePictureOrder.addChild(menuItemOrderWork);
                menuItemDeleteOrder = getToolbarHelper().createMenuEntry("menuOrderDelete", "!!Bildauftrag löschen", null, new EventListener(Event.MENU_ITEM_EVENT) {
                    @Override
                    public void fire(Event event) {
                        doDeleteOrder(event);
                    }
                }, getUITranslationHandler());
                contextmenuTablePictureOrder.addChild(menuItemDeleteOrder);
                menuItemResendMCSearch = getToolbarHelper().createMenuEntry("menuResendSearchRequest", "!!Suchanfrage erneut senden", null, new EventListener(Event.MENU_ITEM_EVENT) {
                    @Override
                    public void fire(Event event) {
                        doResendSearchRequest(event);
                    }
                }, getUITranslationHandler());
                contextmenuTablePictureOrder.addChild(menuItemResendMCSearch);
                menuItemCreateChangeOrder = getToolbarHelper().createMenuEntry(EditToolbarButtonAlias.IMG_PIC_CHANGE_ORDER, new EventListener(Event.MENU_ITEM_EVENT) {
                    @Override
                    public void fire(Event event) {
                        doCreateChangePicOrder(event);
                    }
                }, getUITranslationHandler());
                contextmenuTablePictureOrder.addChild(menuItemCreateChangeOrder);
                menuItemCopyPicOrder = getToolbarHelper().createMenuEntry(EditToolbarButtonAlias.IMG_COPY_PIC_ORDER, new EventListener(Event.MENU_ITEM_EVENT) {
                    @Override
                    public void fire(Event event) {
                        copyPicOrder(event);
                    }
                }, getUITranslationHandler());
                contextmenuTablePictureOrder.addChild(menuItemCopyPicOrder);
                menuItemAbortMediaOrder = getToolbarHelper().createMenuEntry("menuAbortOrder", "!!Bildauftrag stornieren", null, new EventListener(Event.MENU_ITEM_EVENT) {
                    @Override
                    public void fire(Event event) {
                        doAbortMediaOrder(event);
                    }
                }, getUITranslationHandler());
                contextmenuTablePictureOrder.addChild(menuItemAbortMediaOrder);

                menuItemOpenMCInASPLM = getToolbarHelper().createContextMenuEntry(EditToolbarButtonAlias.IMG_OPEN_MC_IN_ASPLM,
                                                                                  getUITranslationHandler(), new EventListener(Event.MENU_ITEM_EVENT,
                                                                                                                               EventListenerOptions.SYNCHRON_EVENT) {
                            @Override
                            public void fire(Event event) {
                                String mediaContainer = getMediaContainerFromSelectedPicOrder();
                                openMediaContainerInASPLM(mediaContainer);
                            }
                        });
                contextmenuTablePictureOrder.addChild(menuItemOpenMCInASPLM);

                if (iPartsRight.RETRIEVE_PICTURES_FROM_PIC_ORDER.checkRightInSession()) {
                    menuItemRetrievePicsForPicOrder = getToolbarHelper().createMenuEntry("menuRetrievePicsForPicOrder", "!!Bilder nachfordern", null, new EventListener(Event.MENU_ITEM_EVENT) {
                        @Override
                        public void fire(Event event) {
                            doRetrievePicsForPicOrder(event);
                        }
                    }, getUITranslationHandler());
                    contextmenuTablePictureOrder.addChild(menuItemRetrievePicsForPicOrder);
                }
            }

            @Override
            protected void createContextMenuItems(GuiContextMenu contextMenu) {
                List<AbstractGuiControl> menuList = new DwList<>(contextmenuTablePictureOrder.getChildren());
                for (AbstractGuiControl menu : menuList) {
                    contextmenuTablePictureOrder.removeChild(menu);
                    contextMenu.addChild(menu);
                }
            }
        };

        // Listener für Sort-Events in der Tabelle
        getTable().addEventListener(new EventListener(Event.TABLE_COLUMN_SORTED_EVENT) {
            @Override
            public void fire(Event event) {
                // Grid enthält Bildaufträge und "Sortierung aufheben" wurde gewählt
                // -> Aufträge aktualiseren (Default Sortierung herstellen)
                if ((getTable().getRowCount() > 0) && (getTable().getSortColumn() == -1)) {
                    refreshTableView();
                }
            }
        });
        dataGrid.setPageSplitNumberOfEntriesPerPage(MAX_SELECT_SEARCH_RESULTS_SIZE);
        dataGrid.setDisplayFields(buildDisplayFields());

        ConstraintsBorder constraints = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
        dataGrid.getGui().setConstraints(constraints);
        mainWindow.panelMain.addChild(dataGrid.getGui());
    }

    private void copyPicOrder(Event event) {
        createAndShowChangeOrder(true, event);
    }

    /**
     * Überprüft, ob die selektierten Bildaufträge nachgefordert werden können
     *
     * @return
     */
    private boolean checkSelectedPicOrdersContainsPictures() {
        List<iPartsDataPicOrder> selectedPicOrders = getSelectedPicOrderEntries();
        if ((selectedPicOrders == null) || selectedPicOrders.isEmpty()) {
            return false;
        }
        // Nur möglich, wenn kein Bildauftrag ungültige Bilder besitzt
        return selectedPicOrders.stream().noneMatch(iPartsDataPicOrder::hasInvalidImageData)
               && selectedPicOrders.stream().anyMatch(this::picOrderReadyForRetrieval);
    }

    /**
     * Check, ob der übergebene Bildauftrag nachgefordert werden kann.
     * Checks:
     * - MC Nummer und Revision muss vorhanden sein
     * - Bildauftrag muss Bilder enthalten
     * - Eines dieser Bilder muss eine gültige PV Nummer und Revision haben
     *
     * @param picOrder
     * @return
     */
    private boolean picOrderReadyForRetrieval(iPartsDataPicOrder picOrder) {
        if ((picOrder != null) && StrUtils.isValid(picOrder.getOrderIdExtern(), picOrder.getOrderRevisionExtern()) && !picOrder.getPictures().isEmpty()) {
            return picOrder.getPictures().getAsList().stream().anyMatch(this::isPictureValidForRetrieval);
        }
        return false;
    }

    /**
     * Check, ob das übergebene Bild eine gültige PV Nummer und Revision hat
     *
     * @param picture
     * @return
     */
    private boolean isPictureValidForRetrieval(iPartsDataPicOrderPicture picture) {
        return (picture != null) && StrUtils.isValid(picture.getAsId().getPicItemId(), picture.getAsId().getPicItemRevId());
    }

    /**
     * Fordert die Bilder der selektierten Bildaufträge nach
     *
     * @param event
     */
    private void doRetrievePicsForPicOrder(Event event) {
        if (!MQHelper.checkTransmissionToASPLMConfigWithMessage()) {
            return;
        }
        getSelectedPicOrderEntries().forEach(picOrder -> {
            if (picOrderReadyForRetrieval(picOrder)) {
                iPartsASPLMItemId mcInfo = new iPartsASPLMItemId(picOrder.getOrderIdExtern(), picOrder.getOrderRevisionExtern());
                iPartsXMLRequestor requestor = new iPartsXMLRequestor();
                picOrder.getPictures().forEach(picture -> RequestPicturesForm.requestNewPictureForMCData(mcInfo, picture.getAsId().getPicItemId(),
                                                                                                         picture.getAsId().getPicItemRevId(), requestor));
            }
        });
    }

    /**
     * Speichert den aktuellen Bildauftrag in der DB und aktualisiert die Bildauftrags-Anzeige
     *
     * @param picOrder
     * @param event
     */
    private void saveAndRefreshOrder(iPartsDataPicOrder picOrder, Event event, boolean doRefresh) {
        getDbLayer().startTransaction();
        try {
            picOrder.saveToDB();
            getDbLayer().commit();
            if (doRefresh) {
                refreshAfterSave(event);
            }
        } catch (Exception e) {
            getDbLayer().rollback();
            Logger.getLogger().handleRuntimeException(e);
        }
    }

    private void refreshAfterSave(Event event) {
        doRefreshPicOrder(event);
        getConnector().updateAllViews(null, false);
    }

    /**
     * Einen oder mehrere Bildaufträge stornieren
     *
     * @param event
     */
    private void doAbortMediaOrder(Event event) {
        if (!MQHelper.checkTransmissionToASPLMConfigWithMessage()) {
            return;
        }
        List<List<EtkDataObject>> selectionList = dataGrid.getMultiSelection();
        if ((selectionList == null) || selectionList.isEmpty()) {
            return;
        }
        if (ModalResult.YES == MessageDialog.showYesNo("!!Sollen wirklich alle ausgewählten Bildaufträge storniert werden?",
                                                       "!!Bildauftrag stornieren")) {
            for (List<EtkDataObject> picOrderObjectList : selectionList) {
                for (EtkDataObject picOrderObject : picOrderObjectList) {
                    if (picOrderObject instanceof iPartsDataPicOrder) {
                        iPartsDataPicOrder picOrder = (iPartsDataPicOrder)picOrderObject;
                        if (picOrder.isValid() && iPartsTransferStates.canCancelMediaOrder(picOrder.getStatus())) {
                            picOrder.setStatus(iPartsTransferStates.CANCEL_REQUEST, DBActionOrigin.FROM_EDIT);
                            saveAndRefreshOrder(picOrder, event, false);
                            sendAbortRequest(picOrder);
                        }
                    }
                }
            }
            refreshAfterSave(event);
        }
    }

    /**
     * Sendet eine Stornierungsanfrage an AS-PLM
     *
     * @param dataPicOrder
     * @return
     */
    private boolean sendAbortRequest(iPartsDataPicOrder dataPicOrder) {
        if (dataPicOrder == null) {
            return false;
        }
        iPartsXMLAbortMediaOrder abortMediaOrder = new iPartsXMLAbortMediaOrder(dataPicOrder.getOrderIdExtern(),
                                                                                dataPicOrder.getOrderRevisionExtern(),
                                                                                "Abort message for media container " +
                                                                                dataPicOrder.getOrderIdExtern() + " - " +
                                                                                dataPicOrder.getOrderRevisionExtern());
        iPartsXMLRequestor requestor = new iPartsXMLRequestor();
        String messageID = dataPicOrder.getAsId().getOrderGuid();
        iPartsXMLMediaMessage xmlMessage = XMLObjectCreationHelper.getInstance().createDefaultPicOrderXMLMessage(abortMediaOrder, requestor, messageID);

        xmlMessage.getRequest().setXmlRequestID(messageID);
        int simPicSearchDelay = iPartsPlugin.getSimAutoResponseDelayForSession(iPartsPlugin.SIM_AUTO_RESPONSE_DELAY_SEARCH) * 1000;
        return MQPicScheduler.getInstance().sendMediaMessage(xmlMessage, iPartsTransferNodeTypes.RES_ABORT_MEDIA_ORDER, simPicSearchDelay, isErrorSimulation(dataPicOrder));
    }

    private boolean isErrorSimulation(iPartsDataPicOrder dataPicOrder) {
        return dataPicOrder.getFieldValue(FIELD_DA_PO_DESCRIPTION).toLowerCase().contains("error");
    }

    /**
     * Kopiert den aktuellen Bildauftrag und öffnet den Änderungsauftrags-Dialog mit der Kopie.
     *
     * @param event
     */
    private void doCreateChangePicOrder(Event event) {
        createAndShowChangeOrder(false, event);
    }

    private void createAndShowChangeOrder(boolean isCopy, Event event) {
        if (!MQHelper.checkTransmissionToASPLMConfigWithMessage()) {
            return;
        }
        iPartsDataPicOrder picOrder = getSelectedPicOrderEntry();
        if ((picOrder != null) && iPartsTransferStates.canRequestChange(picOrder.getStatus())
            && picOrder.hasOnlyInvalidatedChangeOrder()) {
            if (picOrder.getAttributes() == null) {
                MessageDialog.showError("!!Der Bildauftrag existiert nicht in der Datenbank.", "!!Fehler");
                return;
            }

            saveAndRefreshOrder(picOrder, event, true); // zur Sicherheit, weil hier der Original-Bildauftrag eigentlich schon in der DB gespeichert sein sollte.
            EtkDataAssembly assembly = getConnector().getCurrentAssembly();
            if (!(assembly instanceof iPartsDataAssembly)) {
                return;
            }
            Optional<iPartsDataPicOrder> picOrderForChange = iPartsPicOrderEditHelper.getNewestChangeOrderForPicOrderFromSameModule(getProject(), picOrder, (iPartsDataAssembly)assembly, isCopy);
            if (!picOrderForChange.isPresent()) {
                return;
            }
            iPartsDataPicOrder changeOrder = picOrderForChange.get().createChangeOrder(isCopy);
            // Fall die Bilder mittlerweile Gültigkeiten haben, müssen diese an den Änderungsauftrag geschrieben werden.
            changeOrder.alignPictureAndPicOrderValidities(getConnector().getCurrentAssembly());
            changeOrder.setStatus(isCopy ? iPartsTransferStates.COPY_CREATED : iPartsTransferStates.CHANGE_CREATED, DBActionOrigin.FROM_EDIT);
            // Der Änderungsauftrag muss in die DB geschrieben werden, weil sonst in der Zeit des Ausfüllens ein anderer
            // Autor einen Änderungsauftrag erzeugen könnte.
            saveAndRefreshOrder(changeOrder, null, true);
            if (!openPictureOrderForm(changeOrder)) {
                deletePicOrder(changeOrder, null, true);
            }
        }
    }

    /**
     * Schickt eine erneute Suchanfrage bezüglich der Mediacontainer Nummer los. Ist nur relevant für Bilder, die
     * über die Migration importiert wurden
     *
     * @param event
     */
    private void doResendSearchRequest(Event event) {
        iPartsDataPicOrder picOrder = getSelectedPicOrderEntry();
        if ((picOrder != null) && iPartsTransferStates.canResendMCSearchRequest(picOrder.getStatus()) && picOrder.isValid()) {
            saveAndRefreshOrder(picOrder, event, false);
            iPartsDataPicOrder orderWithNewGUID = picOrder.createPicOrderWithNewGUID();
            deletePicOrder(picOrder, event, false);
            orderWithNewGUID.setStatus(iPartsTransferStates.MC_NUMBER_REQUESTED, DBActionOrigin.FROM_EDIT);
            saveAndRefreshOrder(orderWithNewGUID, event, true);
            if (orderWithNewGUID.getPictures().size() == 1) {
                iPartsDataPicOrderPicture picture = orderWithNewGUID.getPictures().get(0);
                if (!startMCNumberSearch(orderWithNewGUID, picture.getAsId().getPicItemId())) {
                    handleInternalErrorForMCRequest(orderWithNewGUID);
                    saveAndRefreshOrder(orderWithNewGUID, event, true);
                }
            }
        }
    }

    public void handleInternalErrorForMCRequest(iPartsDataPicOrder picorder) {
        picorder.setStatus(iPartsTransferStates.MC_NUMBER_REQUEST_ERROR, DBActionOrigin.FROM_EDIT);
        picorder.setInfoTextForAllLanguages(TranslationHandler.translate("!!Interner Fehler beim Senden der MediaContainer-Anfrage"));
    }


    /**
     * Schickt eine MediaContainer Anfrage an AS-PLM um einen Änderungsauftrag auf Basis der MC Nummer zu erzeugen.
     *
     * @param dataPicOrder
     * @param originalPicNumber
     * @return
     */
    public boolean startMCNumberSearch(iPartsDataPicOrder dataPicOrder, String originalPicNumber) {
        if ((dataPicOrder == null) || StrUtils.isEmpty(originalPicNumber)) {
            return false;
        }
        iPartsXMLSearchMediaContainers container = new iPartsXMLSearchMediaContainers();
        container.setMaxResultFromIParts(10);
        container.addSearchCriterion(iPartsTransferSMCAttributes.SMC_ALTERNATE_ID, originalPicNumber);
        iPartsXMLRequestor requestor = new iPartsXMLRequestor();
        iPartsXMLMediaMessage xmlMessage = XMLObjectCreationHelper.getInstance().createDefaultPicSearchXMLMessage(container, requestor,
                                                                                                                  "",
                                                                                                                  false);
        String messageID = dataPicOrder.getAsId().getOrderGuid();
        xmlMessage.getRequest().setXmlRequestID(messageID);
        int simPicSearchDelay = iPartsPlugin.getSimAutoResponseDelayForSession(iPartsPlugin.SIM_AUTO_RESPONSE_DELAY_SEARCH) * 1000;
        return MQPicScheduler.getInstance().sendMediaMessage(xmlMessage, iPartsTransferNodeTypes.RES_SEARCH_MEDIA_CONTAINERS, simPicSearchDelay, isErrorSimulation(dataPicOrder));
    }

    private String getMediaContainerFromSelectedPicOrder() {
        iPartsDataPicOrder picOrder = getSelectedPicOrderEntry();
        if (picOrder != null) {
            return picOrder.getOrderIdExtern();
        } else {
            return "";
        }
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public ModalResult showModal() {
        return mainWindow.showModal();
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }


    @Override
    public void dispose() {
        removeXmlMessageListener();
        super.dispose();
    }


    @Override
    public EditModuleFormIConnector getConnector() {
        return (EditModuleFormIConnector)super.getConnector();
    }

    public AssemblyId getAssemblyId() {
        return assemblyId;
    }

    public boolean isEditAllowed() {
        return isEditAllowed;
    }

    public void setEditAllowed(boolean editAllowed) {
        if (this.isEditAllowed == editAllowed) {
            return;
        }
        isEditAllowed = editAllowed;
        mainWindow.buttonPanel.setButtonVisible(GuiButtonOnPanel.ButtonType.CANCEL, isEditAllowed);
        if (isEditAllowed) {
            mainWindow.buttonPanel.setButtonModalResult(GuiButtonOnPanel.ButtonType.OK, ModalResult.OK);
        } else {
            mainWindow.buttonPanel.setButtonModalResult(GuiButtonOnPanel.ButtonType.OK, ModalResult.CANCEL);
        }
    }

    public void setListenerNewPicOrder(EventListener listenerNewPicOrder) {
        this.listenerNewPicOrder = listenerNewPicOrder;
        if (this.listenerNewPicOrder != null) {
            dataGrid.showToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_PICORDER);
        } else {
            dataGrid.hideToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_PICORDER);
        }
    }

    public void setListenerRefreshPicOrder(EventListener listenerRefreshPicOrder) {
        this.listenerRefreshPicOrder = listenerRefreshPicOrder;
    }

    public boolean hasPictureOrders() {
        return isAssemblyIdValid() && (getTable().getRowCount() > 0);
    }

    public void doNewPicOrder(Event event) {
        if (listenerNewPicOrder != null) {
            listenerNewPicOrder.fire(event);
        }
    }

    public void refreshTableView() {
        updateTablePictureOrder();
    }

    public void doRefreshPicOrder(Event event) {
        refreshTableView();
        if (listenerRefreshPicOrder != null) {
            listenerRefreshPicOrder.fire(event);
        }
    }

    public void showToolbar(boolean value) {
        dataGrid.showToolbar(value);
    }

    public boolean isToolbarVisible() {
        return dataGrid.isToolbarVisible();
    }

    public void setSelectedEntryById(iPartsPicOrderId picOrderId) {
        dataGrid.setSelectedEntryById(picOrderId);
    }


    private EtkDisplayFields buildDisplayFields() {
        EtkDisplayFields displayFields = new EtkDisplayFields();
        displayFields.load(getConfig(), iPartsEditConfigConst.iPARTS_EDIT_PICTURE_ORDER_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS);
        if (displayFields.size() == 0) {
            EtkDisplayFields defaultDisplayFields = new EtkDisplayFields();
            EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_PICORDER, FIELD_DA_PO_ORDER_ID_EXTERN, false, false);
            defaultDisplayFields.addFeld(displayField);

            displayField = new EtkDisplayField(TABLE_DA_PICORDER, FIELD_DA_PO_ORDER_REVISION_EXTERN, false, false);
            defaultDisplayFields.addFeld(displayField);

            displayField = new EtkDisplayField(TABLE_DA_PICORDER, FIELD_DA_PO_PROPOSED_NAME, true, false);
            defaultDisplayFields.addFeld(displayField);

            displayField = new EtkDisplayField(TABLE_DA_PICORDER, FIELD_DA_PO_STATUS, false, false);
            defaultDisplayFields.addFeld(displayField);

            displayField = new EtkDisplayField(TABLE_DA_PICORDER, FIELD_DA_PO_ORDER_INVALID, false, false);
            defaultDisplayFields.addFeld(displayField);

            displayField = new EtkDisplayField(TABLE_DA_PICORDER, FIELD_DA_PO_AUTOMATION_LEVEL, false, false);
            defaultDisplayFields.addFeld(displayField);

            displayField = new EtkDisplayField(TABLE_DA_PICORDER, FIELD_DA_PO_IS_TEMPLATE, false, false);
            defaultDisplayFields.addFeld(displayField);

            defaultDisplayFields.loadStandards(getConfig());
            displayFields = defaultDisplayFields;
        }

        return displayFields;
    }

    private void doEnableButtons() {
        List<List<EtkDataObject>> multiSelection = dataGrid.getMultiSelection();
        boolean isMultiSelection = multiSelection.size() > 1;
        if (isEditAllowed) {
            dataGrid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_PICORDER, true);
            menuItemCreateChangeOrder.setEnabled(!isMultiSelection);
            menuItemCopyPicOrder.setEnabled(!isMultiSelection);
            menuItemResendMCSearch.setEnabled(!isMultiSelection);
            menuItemDeleteOrder.setEnabled(!isMultiSelection);
            menuItemOrderWork.setEnabled(!isMultiSelection);
            menuItemAbortMediaOrder.setEnabled(checkIfAbortMediaOrderPossible());
        } else {
            dataGrid.hideToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_PICORDER);
            menuItemCreateChangeOrder.setEnabled(false);
            menuItemCopyPicOrder.setEnabled(false);
            menuItemResendMCSearch.setEnabled(false);
            menuItemDeleteOrder.setEnabled(false);
            menuItemOrderWork.setEnabled(false);
            menuItemAbortMediaOrder.setEnabled(false);
        }
        menuItemOpenMCInASPLM.setEnabled(!isMultiSelection && !getMediaContainerFromSelectedPicOrder().isEmpty());
        if (menuItemRetrievePicsForPicOrder != null) {
            menuItemRetrievePicsForPicOrder.setVisible(checkSelectedPicOrdersContainsPictures());
        }
    }

    /**
     * Überprüft, ob die Bildaufträge in der aktuellen Auswahl storniert werden dürfen
     *
     * @return
     */
    private boolean checkIfAbortMediaOrderPossible() {
        List<List<EtkDataObject>> selectionList = dataGrid.getMultiSelection();
        if ((selectionList == null) || selectionList.isEmpty()) {
            return false;
        }
        for (List<EtkDataObject> picOrderObjectList : selectionList) {
            for (EtkDataObject picOrderObject : picOrderObjectList) {
                if (picOrderObject instanceof iPartsDataPicOrder) {
                    iPartsDataPicOrder picOrder = (iPartsDataPicOrder)picOrderObject;
                    if (!iPartsTransferStates.canCancelMediaOrder(picOrder.getStatus())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private GuiTable getTable() {
        return dataGrid.getTable();
    }

    /**
     * Liefert den ersten selektierten Bildauftrag zurück
     *
     * @return
     */
    public iPartsDataPicOrder getSelectedPicOrderEntry() {
        List<iPartsDataPicOrder> selectionList = getSelectedPicOrderEntries();
        if ((selectionList != null) && !selectionList.isEmpty()) {
            return selectionList.get(0);
        }
        return null;
    }

    /**
     * Liefert alle selektierten Bildaufträge zurück
     *
     * @return
     */
    public List<iPartsDataPicOrder> getSelectedPicOrderEntries() {
        List<EtkDataObject> selectionList = dataGrid.getSelection();
        if ((selectionList != null) && !selectionList.isEmpty()) {
            List<iPartsDataPicOrder> selectedPicOrders = new ArrayList<>();
            selectionList.forEach(dataObject -> {
                if (dataObject instanceof iPartsDataPicOrder) {
                    selectedPicOrders.add((iPartsDataPicOrder)dataObject);
                }
            });
            return selectedPicOrders;
        }
        return null;
    }

    public List<iPartsDataPicOrder> getPicOrders() {
        List<iPartsDataPicOrder> resultList = new DwList<>();
        for (GuiTableRow tableRow : getTable().getAllRows()) {
            DataObjectGrid.GuiTableRowWithObjects rowWithAttributes = (DataObjectGrid.GuiTableRowWithObjects)tableRow;
            EtkDataObject dataObject = rowWithAttributes.getObjectForTable(TABLE_DA_PICORDER);
            if (dataObject instanceof iPartsDataPicOrder) {
                resultList.add((iPartsDataPicOrder)dataObject);
            }
        }
        return resultList;
    }

    public List<iPartsDataPicOrder> getPicOrdersForEdit() {
        List<iPartsDataPicOrder> resultList = new DwList<>();
        List<iPartsDataPicOrder> totalList = getPicOrders();
        for (iPartsDataPicOrder dataPicOrder : totalList) {
            if (iPartsTransferStates.canEditAttachments(dataPicOrder.getStatus()) && (dataPicOrder.getAttributes() != null)) {
                resultList.add(dataPicOrder);
            }
        }
        return resultList;
    }

    private boolean isAssemblyIdValid() {
        if (assemblyId != null) {
            return assemblyId.isValidId();
        }
        return false;
    }

    private void doEditOrder(Event event) {
        onMouseDblClickEvent(event);
    }

    private void doDeleteOrder(Event event) {
        iPartsDataPicOrder picOrder = getSelectedPicOrderEntry();
        if (picOrder != null) {
            //Sicherheitsabfrage
            if (MessageDialog.show(TranslationHandler.translate("!!Wollen Sie wirklich den Bildauftrag löschen?"), "!!Löschen",
                                   MessageDialogIcon.CONFIRMATION,
                                   MessageDialogButtons.YES, MessageDialogButtons.NO) != ModalResult.YES) {
                return;
            }
            deletePicOrder(picOrder, event, true);
        }
    }

    public void deletePicOrder(iPartsDataPicOrder picOrder, Event event, boolean doRefresh) {
        if (picOrder == null) {
            return;
        }
        getDbLayer().startTransaction();
        try {
            // zur Sicherheit (damit auch alles geladen ist)
            picOrder.getModules();
            picOrder.getUsages();
            picOrder.getParts();
            picOrder.getPictures();
            picOrder.getAttachments();
            // Die eigentlichen Anhänge sollen nicht gelöscht werden
            if (picOrder.isChangeOrCopy()) {
                picOrder.setChildren(iPartsDataPicOrder.CHILDREN_NAME_ATTACHMENTS, null);
            }
            picOrder.deleteFromDB(true);
            getDbLayer().commit();
            if (doRefresh) {
                if (event != null) {
                    // Bilder sollen neu geladen werden, da sich dadurch die MC Nummer ändern kann
                    event.addParameter(EditAssemblyImageForm.RELOAD_PICTURES, true);
                }
                doRefreshPicOrder(event);
                getConnector().updateAllViews(null, false);
            }
        } catch (Exception e) {
            getDbLayer().rollback();
            Logger.getLogger().throwRuntimeException(e);
        }
    }


    public synchronized void removeXmlMessageListener() {
        // XMLListener für alle bisherigen Bildauftrag-IDs austragen
        if (messageIdsForPicOrderUpdates != null) {
            iPartsMQMessageManager.getInstance(iPartsPlugin.XML_MESSAGE_MANAGER_NAME_MEDIA).removeXMLMessageListener(xmlMessageListener,
                                                                                                                     messageIdsForPicOrderUpdates);
            messageIdsForPicOrderUpdates = null;
        }
        if ((messageIdsForEvents != null) && !messageIdsForEvents.isEmpty()) {
            iPartsMQMessageManager.getInstance(iPartsPlugin.XML_MESSAGE_MANAGER_NAME_MEDIA).removeXMLMessageListenerForMessageTypes(updatePicOrderEventListener,
                                                                                                                                    iPartsTransferNodeTypes.EVENT_ASSIGNMENT_CHANGE,
                                                                                                                                    iPartsTransferNodeTypes.EVENT_RELEASE_STATUS_CHANGE);
            messageIdsForEvents = null;
        }
    }

    private synchronized void loadPictureOrders() {
        removeXmlMessageListener();

        getConnector().updatePictureOrderList();

        // XMLListener für alle relevanten Bildauftrag-IDs registrieren
        iPartsDataPicOrderList picOrderList = getConnector().getPictureOrderList();
        List<String> messageIds = new DwList<>();
        messageIdsForEvents = new DwList<>();
        for (iPartsDataPicOrder picOrder : picOrderList) {
            iPartsTransferStates state = picOrder.getStatus();
            if (iPartsTransferStates.canReceiveEventsFromMQ(state)) {
                // Warten auf beliebige Event-Meldungen
                messageIdsForEvents.add(picOrder.getOrderItemId());
            } else if (!iPartsTransferStates.isReplacedByChangeOrder(state) && !picOrder.isCancelled()) {
                // Ansonsten reagiere auf alle Veränderungen bezüglich aller Bildaufträge (außer Bildaufträge, die durch
                // Änderungsaufträge ersetzt wurden oder Aufträge, die storniert wurden)
                messageIds.add(picOrder.getAsId().getOrderGuid());
            }

        }
        if (!messageIds.isEmpty()) {
            // Anmelden für Antworten von GetMediaOrder oder GetMediaContent
            messageIdsForPicOrderUpdates = messageIds.toArray(new String[0]);
            iPartsMQMessageManager.getInstance(iPartsPlugin.XML_MESSAGE_MANAGER_NAME_MEDIA).addXMLMessageListener(xmlMessageListener,
                                                                                                                  messageIdsForPicOrderUpdates);
        }
        if (!messageIdsForEvents.isEmpty()) {
            // Anmelden für Event-Meldungen
            iPartsMQMessageManager.getInstance(iPartsPlugin.XML_MESSAGE_MANAGER_NAME_MEDIA).addXMLMessageListenerForMessageTypes(updatePicOrderEventListener,
                                                                                                                                 iPartsTransferNodeTypes.EVENT_ASSIGNMENT_CHANGE,
                                                                                                                                 iPartsTransferNodeTypes.EVENT_RELEASE_STATUS_CHANGE);
        }
    }

    /**
     * PopupMenu Einträge modifizieren
     *
     * @param event
     */
    private void doSetMenuEntries(Event event) {
        iPartsDataPicOrder picOrder = getSelectedPicOrderEntry();
        if (picOrder != null) {
            // Ein Auftrag, der aus einer MedienContainer-Suchanfrage heraus erzeugt und nicht positiv quittiert wurde,
            // darf nicht geändert werden
            if (iPartsTransferStates.isSaveToDB_Allowed(picOrder.getStatus()) && !iPartsTransferStates.isMCNumberDeleteState(picOrder.getStatus()) && picOrder.isValid()) {
                menuItemOrderWork.setText("!!Bildauftrag ändern");
            } else {
                menuItemOrderWork.setText("!!Bildauftrag anzeigen");
            }
            boolean changeOrderPossible = getConnector().isAuthorOrderValid() && iPartsTransferStates.canRequestChange(picOrder.getStatus())
                                          && picOrder.hasOnlyInvalidatedChangeOrder();
            menuItemCreateChangeOrder.setVisible(changeOrderPossible);
            menuItemCopyPicOrder.setVisible(MQHelper.isCopyOfPicOrdersAllowed() && changeOrderPossible);
            menuItemResendMCSearch.setVisible(iPartsTransferStates.canResendMCSearchRequest(picOrder.getStatus()) && picOrder.isValid());
        }
        menuItemDeleteOrder.setVisible((picOrder != null) && iPartsTransferStates.canDeletePicOrder(picOrder.getStatus()));
        menuItemAbortMediaOrder.setVisible(checkIfAbortMediaOrderPossible());
    }

    private void onMouseDblClickEvent(Event event) {
        updateTablePictureOrder(); // vor dem Bearbeiten die Liste aktualisieren, weil im Hintergrund jemand den Bildauftrag bearbeitet haben könnte
        iPartsDataPicOrder picOrder = getSelectedPicOrderEntry();
        openPictureOrderForm(picOrder);

    }

    /**
     * Zeigt den übergebenen Bildauftrag {@link iPartsDataPicOrder} im Bildafutragseditor an
     *
     * @param picOrder
     */
    public boolean openPictureOrderForm(iPartsDataPicOrder picOrder) {
        if (picOrder != null) {
            if (picOrder.getAttributes() == null) {
                MessageDialog.showError("!!Der Bildauftrag existiert nicht in der Datenbank.", "!!Fehler");
                return false;
            }
            if (isEditAllowed()) {
                if (PicOrderMainForm.showPictureOrderDialog(getConnector(), this, picOrder, getProductStructureType(), listenerRefreshPicOrder,
                                                            EditModuleHelper.isCarPerspectiveAssembly(getConnector().getCurrentAssembly()))) {
                    doRefreshPicOrder(null);
                    getConnector().updateAllViews(null, false);
                    return true;
                }
            } else {
                PicOrderMainForm.viewPictureOrderDialog(getConnector(), this, getConnector().getCurrentAssembly(), picOrder,
                                                        getProductStructureType());
            }
        }
        return false;
    }

    public PRODUCT_STRUCTURING_TYPE getProductStructureType() {
        PRODUCT_STRUCTURING_TYPE productType = PRODUCT_STRUCTURING_TYPE.KG_TU; //Standard is jetzt KG_TU
        // SeriesId und EinPASId/KgTuId eintragen, falls möglich
        iPartsDataModuleEinPASList moduleEinPASList = iPartsDataModuleEinPASList.loadForModule(getProject(), getConnector().getCurrentAssembly().getAsId());
        if (!moduleEinPASList.isEmpty()) {
            //Sobald ein Modul gefunden wurde, muss auch über das Produkt der Strukturtyp bestimmt werden, damit der Dialog die richtigen Felder anzeigt
            iPartsProductId productId = new iPartsProductId(moduleEinPASList.get(0).getAsId().getProductNumber());
            iPartsProduct product = iPartsProduct.getInstance(getProject(), productId);
            productType = product.getProductStructuringType();
        } else {
            // Strukturtyp über das Produkt bestimmen
            iPartsDataProductModulesList productModulesList = iPartsDataProductModulesList.loadDataProductModulesList(getProject(), getConnector().getCurrentAssembly().getAsId());
            if (!productModulesList.isEmpty()) {
                iPartsProductId productId = new iPartsProductId(productModulesList.get(0).getAsId().getProductNumber());
                iPartsProduct product = iPartsProduct.getInstance(getProject(), productId);
                productType = product.getProductStructuringType();
            }
        }
        return productType;
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        if (getConnector().isFlagUserInterfaceLanguageChanged()) {
            dataGrid.setDisplayFields(buildDisplayFields()); // Tabellen-Überschriften neu auslesen mit der Viewer-Sprache
        }
    }

    private void updateTablePictureOrder() {
        // Selektion merken
        List<IdWithType> selectedIds = dataGrid.getSelectedObjectIds(TABLE_DA_PICORDER);
        // Sortierung merken (falls der Nutzer eine manuelle Sortierung gesetzt hat)
        int oldSortColumn = getTable().getSortColumn();
        boolean sortAscending = getTable().isSortAscending();

        // Grid leeren und sortiert befüllen
        dataGrid.clearGrid();
        if (isAssemblyIdValid()) {
            loadPictureOrders();
            setTablePictureOrder();
        }

        // Sortierung wiederherstellen
        if (oldSortColumn != -1) {
            getTable().sortRowsAccordingToColumn(oldSortColumn, sortAscending);
        }
        // Selektion wiederherstellen
        dataGrid.setSelectedObjectIds(selectedIds, TABLE_DA_PICORDER);

        doEnableButtons();
    }

    private void setTablePictureOrder() {
        List<EtkDataObject> objectList;
        if (showAllPicOrder) {
            objectList = iPartsPicOrderEditHelper.getPicOrdersSorted(getConnector().getPictureOrderList().getAsList());
        } else {
            objectList = iPartsPicOrderEditHelper.getPicOrdersSortedAndRelevantForAssembly(getConnector().getPictureOrderList().getAsList(), getConnector().getCurrentAssembly());
        }
        if (objectList.isEmpty()) {
            dataGrid.showNoResultsLabel(true);
        } else {
            getTable().switchOffEventListeners();
            objectList.forEach(picOrder -> dataGrid.addObjectToGrid(picOrder));
            getTable().switchOnEventListeners();
        }
    }

    /**
     * Verwaltet die Sichtbarkeit aller Bildaufträge
     *
     * @param showAll - alle Bildaufträge sollen angezeigt werden
     */
    public void handlePicOrdersVisibility(boolean showAll) {
        showAllPicOrder = showAll;
        refreshTableView();
    }

    /**
     * Eigenes DataObjectGrid für weitere Methoden
     */
    private static class PicOrderDataObjectGrid extends DataObjectFilterGrid {

        public PicOrderDataObjectGrid(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
            super(dataConnector, parentForm);
            getTable().setSortByPopupMenu(true);
        }

        @Override
        protected void postCreateGui() {
            super.postCreateGui();
            getTable().setSortByPopupMenu(true);
        }

        public void setSelectedEntryById(iPartsPicOrderId picOrderId) {
            super.scrollToIdIfExists(picOrderId, TABLE_DA_PICORDER);
        }
    }


    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    private de.docware.framework.modules.gui.controls.menu.GuiContextMenu contextmenuTablePictureOrder;

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            contextmenuTablePictureOrder = new de.docware.framework.modules.gui.controls.menu.GuiContextMenu();
            contextmenuTablePictureOrder.setName("contextmenuTablePictureOrder");
            contextmenuTablePictureOrder.__internal_setGenerationDpi(96);
            contextmenuTablePictureOrder.registerTranslationHandler(translationHandler);
            contextmenuTablePictureOrder.setScaleForResolution(true);
            contextmenuTablePictureOrder.setMinimumWidth(10);
            contextmenuTablePictureOrder.setMinimumHeight(10);
            contextmenuTablePictureOrder.setMenuName("contextmenuTablePictureOrder");
            contextmenuTablePictureOrder.setParentControl(this);
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
            title.setTitle("!!Anzeige Bildaufträge für Modul");
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
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMain.setLayout(panelMainLayout);
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