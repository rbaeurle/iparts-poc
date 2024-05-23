/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.config.partlist.EtkEbenenDaten;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.components.DataObjectFilterGrid;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.forms.common.components.MenuRunnable;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.forms.events.OnNewEvent;
import de.docware.apps.etk.base.mechanic.imageview.forms.AssemblyImageFormEvents;
import de.docware.apps.etk.base.mechanic.imageview.model.ImageViewerItemMarkedThumbnails;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImage;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkHotspotDestination;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst.iPartsDataWorkOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst.iPartsWorkOrderCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleEinPASId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsSAId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsAssemblyImageForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.MQHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSA;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsColorTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferStates;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.picorder.PicOrderMainForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.picorder.PicOrdersToModuleGridForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as.EditTransferToASHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.db.*;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiSeparator;
import de.docware.framework.modules.gui.controls.GuiWindow;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.table.GuiTable;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.controls.table.TableSelectionMode;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolButton;
import de.docware.framework.modules.gui.controls.viewer.GuiViewerLink;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerFireOnce;
import de.docware.framework.modules.gui.event.EventListenerOptions;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.responsive.base.theme.ThemeManager;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;

import java.awt.*;
import java.util.List;
import java.util.*;

/*
 * Form zur Darstellung der Bilder und Bilderlisten zu einer StüLi für Edit
 */
public class EditAssemblyImageForm extends AbstractJavaViewerForm implements iPartsConst {

    // Parameter zum Neuladen von Bilder im Bilder-Grid
    public static final String RELOAD_PICTURES = "RELOAD_PICTURES";
    private static final boolean MARK_THUMBNAILS_MULTI_SELECT = true; // sollen bei Thumbnail-Darstellung und MultiSelect in der Tabelle

    private final List<AssemblyImageFormEvents> eventObjects = new ArrayList<>(); // Event-Listener
    private final Set<GuiWindow> nonModalChildWindows = new HashSet<>();
    private iPartsEditAssemblyImageForm assemblyImage;
    private PicOrdersToModuleGridForm showPictureOrderForm;
    private boolean isEditAllowed = true;
    private boolean isCurrentAssemblyCarPerspective;
    private EditToolbarButtonMenuHelper toolbarHelper;
    private boolean linkIsNotInPartlist;
    private int splitPanePicOrdersHeight;
    private int dividerPos = -1;
    private DataObjectFilterGrid grid;
    private boolean isInDataChangedEvent;
    private boolean picOrdersDockingPanelInitialized;
    private GuiToolButton filterPicOrdersButton;
    private GuiMenuItem menuItemMultipleUse;

    /**
     * Erzeugt eine Instanz von EditAssemblyImageForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditAssemblyImageForm(EditModuleFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.isInDataChangedEvent = false;
        postCreateGui();
        setEditAllowed(getConnector().isAuthorOrderValid());
        this.isCurrentAssemblyCarPerspective = EditModuleHelper.isCarPerspectiveAssembly(dataConnector.getCurrentAssembly());
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        toolbarHelper = new EditToolbarButtonMenuHelper(getConnector(), mainWindow.toolbarImageEdit);
        toolbarManager = toolbarHelper.getToolbarManager();

        assemblyImage = new iPartsEditAssemblyImageForm();
        assemblyImage.getGui().setConstraints(new ConstraintsBorder(LayoutBorder.POSITION_CENTER));
        mainWindow.splitPane_secondChild.addChild(assemblyImage.getGui());
        mainWindow.buttonPanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, false);
        assemblyImage.addEventListener(new AssemblyImageFormEvents() {
            @Override
            public boolean isInPartsList(EtkHotspotDestination link) {
                for (AssemblyImageFormEvents eventObject : eventObjects) {
                    if (eventObject.isInPartsList(link)) {
                        //merken: Link is in Partlist
                        linkIsNotInPartlist = false;
                        return true;
                    }
                }

                // Merken: Link ist nicht in der Stückliste
                linkIsNotInPartlist = true;

                // Aber bei 2D trotzdem true zurückgeben, damit der Link gezeichnet wird (bei 3D ist dies nicht notwendig
                // bzw. führt sogar zu falschen Hotspot-Daten, wenn die Hotspots hierarchisch angeordnet sind)
                return !link.isImage3D();
            }

            @Override
            public List<EtkDataPartListEntry> getPartListEntriesForLink(EtkHotspotDestination link) {
                for (AssemblyImageFormEvents eventObject : eventObjects) {
                    final List<EtkDataPartListEntry> etkDataPartListEntries = eventObject.getPartListEntriesForLink(link);
                    if (etkDataPartListEntries != null) {
                        return etkDataPartListEntries;
                    }
                }
                return new DwList<>();
            }

            @Override
            public String getColValueLink(EtkHotspotDestination link, String tableName, String fieldName) {
                for (AssemblyImageFormEvents eventObject : eventObjects) {
                    String colValueLink = eventObject.getColValueLink(link, tableName, fieldName);
                    if ((colValueLink != null) && !colValueLink.isEmpty()) {
                        return colValueLink;
                    }
                }
                return "";
            }

            @Override
            public boolean isAssemblyLink(EtkHotspotDestination link) {
                for (AssemblyImageFormEvents eventObject : eventObjects) {
                    if (eventObject.isAssemblyLink(link)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void OnLoaded() {
//                if (doCheck) {
//                    if (mainWindow.dockingpanel_PicOrders.isSplitPaneSizeValid()) {
//                        mainWindow.dockingpanel_PicOrders.setShowing(false);
//                        doCheck = false;
//                    }
//                }
            }

            @Override
            public boolean OnLinkClick(List<GuiViewerLink> links, int imageIndex, boolean imageIs3D, int button) {
                for (AssemblyImageFormEvents eventObject : eventObjects) {
                    if (eventObject.OnLinkClick(links, imageIndex, imageIs3D, button)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean OnLinkDblClick(GuiViewerLink link, int imageIndex, boolean imageIs3D, int button) {
                for (AssemblyImageFormEvents eventObject : eventObjects) {
                    if (eventObject.OnLinkDblClick(link, imageIndex, imageIs3D, button)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public String OnLinkHintTextNeeded(GuiViewerLink link, String hintText) {
                return null;
            }

            @Override
            public void OnZoomed(double zoomFactor) {
            }

            @Override
            public void OnScrollbarVisibilityChanged(boolean horizontalScrollbarVisible, boolean verticalScrollbarVisible) {

            }
        });
        //assemblyImageForm.updateView();

        mainWindow.dockingpanel_PicOrders.setShowing(false);
        initGrid();
        createToolbarButtons();

        showPictureOrderForm = new PicOrdersToModuleGridForm(getConnector(), this, getConnector().getCurrentAssembly().getAsId());
        showPictureOrderForm.setEditAllowed(getConnector().isAuthorOrderValid());
        showPictureOrderForm.showToolbar(false);
        mainWindow.panelPicOrderTable.addChildBorderCenter(showPictureOrderForm.getGui());
        EventListener resizeListener = new EventListenerFireOnce(Event.ON_RESIZE_EVENT, mainWindow.dockingpanel_PicOrders) {
            @Override
            public boolean isFireOnceValid(Event event) {
                return mainWindow.dockingpanel_PicOrders.isSplitPaneSizeValid();
            }

            @Override
            public void fireOnce(Event event) {
                splitPanePicOrdersHeight = event.getIntParameter(Event.EVENT_PARAMETER_NEWHEIGHT);
                updatePictureOrders();
            }
        };
        mainWindow.dockingpanel_PicOrders.addEventListener(resizeListener);
        showPictureOrderForm.setListenerNewPicOrder(new EventListener(Event.GENERIC_TYPE) {
            @Override
            public void fire(Event event) {
                doCreatePicOrder(null, true);
            }
        });
        showPictureOrderForm.setListenerRefreshPicOrder(new EventListener(Event.GENERIC_TYPE) {
            @Override
            public void fire(Event event) {
                doRefreshPicOrder(event, false);
            }
        });

        setTableImages();
        enableButtons();
        updateStatusText();
    }

    /**
     * Liefert zurück, ob irgendwelche Bildaufträge angezeigt werden
     *
     * @return
     */
    private boolean picOrdersVisible() {
        if (filterPicOrdersButton.isPressed()) {
            // Alle anzeigen
            return !getConnector().getPictureOrderList().isEmpty();
        } else {
            // Es werden nur die relevanten Aufträge angezeigt
            return !iPartsPicOrderEditHelper.getPicOrdersSortedAndRelevantForAssembly(getConnector().getPictureOrderList().getAsList(), getConnector().getCurrentAssembly()).isEmpty();
        }
    }

    /**
     * Initialisiert das Grid für die Bildreferenzen
     */
    private void initGrid() {
        grid = new DataObjectFilterGrid(getConnector(), getParentForm());
        getTable().setContextMenu(contextmenuTableImages);
        grid.setPageSplitNumberOfEntriesPerPage(iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE);
        getTable().setName("tableImages");
        getTable().__internal_setGenerationDpi(96);
        getTable().setScaleForResolution(true);
        getTable().setMinimumWidth(10);
        getTable().setMinimumHeight(10);
        getTable().setSelectionMode(TableSelectionMode.SELECTION_MODE_ARBITRARY_SELECTION);
        ConstraintsBorder tableImagesConstraints = new ConstraintsBorder();
        getTable().setConstraints(tableImagesConstraints);
        mainWindow.scrollPaneGrid.addChild(getTable());
        getTable().addEventListener(new EventListener(Event.TABLE_SELECTION_EVENT) {
            @Override
            public void fire(Event event) {
                tableSelectionEvent(event);
            }
        });
        grid.setDisplayFields(getDisplayFields());
    }

    /**
     * Liefert die Anzeigefelder für das Grid
     *
     * @return
     */
    private EtkDisplayFields getDisplayFields() {
        EtkDisplayFields displayResultFields = new EtkDisplayFields();
        displayResultFields.load(getConfig(), iPartsConst.CONFIG_KEY_PICTURE_REFERENCES_DISPLAY_FIELDS);

        if (displayResultFields.size() == 0) {
            List<EtkDisplayField> defaultDisplayFields = createDefaultDisplayFields();
            if (defaultDisplayFields != null) {
                for (EtkDisplayField defaultDisplayField : defaultDisplayFields) {
                    displayResultFields.addFeld(defaultDisplayField);
                }
            }

            displayResultFields.loadStandards(getConfig());
        }

        EtkDataAssembly assembly = getConnector().getCurrentAssembly();
        if (assembly instanceof iPartsDataAssembly) {
            iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)assembly;
            for (EtkDisplayField field : displayResultFields.getVisibleFields()) {
                // Nur anzeigen, wenn die Baureihe zum Produkt Event-gesteuert ist
                String fieldName = field.getKey().getFieldName();
                if (fieldName.equals(iPartsConst.FIELD_I_EVENT_FROM) || fieldName.equals(iPartsConst.FIELD_I_EVENT_TO)) {
                    if (!iPartsAssembly.isSeriesFromProductModuleUsageEventControlled()) {
                        field.setVisible(false);
                    }
                }

                // Nur anzeigen, wenn es sich um ein PSK-Produkt handelt
                if (fieldName.equals(iPartsConst.FIELD_I_PSK_VARIANT_VALIDITY)) {
                    if (!iPartsAssembly.isPSKAssembly()) {
                        field.setVisible(false);
                    }
                }
            }
        }
        // DAIMLER-15226: Feld I_NAVIGATION_PERSPECTIVE anpassen
        boolean isCarPerspective = EditModuleHelper.isCarPerspectiveAssembly(assembly);
        EtkDisplayField displayField = displayResultFields.getFeldByName(TABLE_IMAGES, FIELD_I_NAVIGATION_PERSPECTIVE);
        if (isCarPerspective) {
            if (displayField == null) {
                displayField = new EtkDisplayField(TABLE_IMAGES, FIELD_I_NAVIGATION_PERSPECTIVE, false, false);
                displayField.loadStandards(getConfig());
                displayResultFields.addFeld(displayField);
            }
        } else {
            if (displayField != null) {
                displayField.setVisible(false);
            }
        }


        return displayResultFields;
    }

    private List<EtkDisplayField> createDefaultDisplayFields() {
        List<EtkDisplayField> defaultDisplayFields = new ArrayList<>();
        EtkDisplayField displayField = new EtkDisplayField(iPartsConst.TABLE_IMAGES, iPartsConst.FIELD_I_IMAGES, false, false);
        displayField.setColumnFilterEnabled(true);
        defaultDisplayFields.add(displayField);
        displayField = new EtkDisplayField(iPartsConst.TABLE_IMAGES, iPartsConst.FIELD_I_PVER, false, false);
        displayField.setColumnFilterEnabled(true);
        defaultDisplayFields.add(displayField);
        displayField = new EtkDisplayField(iPartsConst.TABLE_IMAGES, iPartsDataVirtualFieldsDefinition.DA_PICTURE_ORDER_ID, false, false);
        defaultDisplayFields.add(displayField);
        displayField = new EtkDisplayField(iPartsConst.TABLE_IMAGES, iPartsDataVirtualFieldsDefinition.DA_IS_MULTIPLE_USED, false, false);
        defaultDisplayFields.add(displayField);
        displayField = new EtkDisplayField(iPartsConst.TABLE_IMAGES, iPartsConst.FIELD_I_CODES, false, false);
        defaultDisplayFields.add(displayField);
        displayField = new EtkDisplayField(iPartsConst.TABLE_IMAGES, iPartsConst.FIELD_I_MODEL_VALIDITY, false, true);
        defaultDisplayFields.add(displayField);
        displayField = new EtkDisplayField(iPartsConst.TABLE_IMAGES, iPartsConst.FIELD_I_SAA_CONSTKIT_VALIDITY, false, true);
        defaultDisplayFields.add(displayField);
        displayField = new EtkDisplayField(iPartsConst.TABLE_IMAGES, iPartsConst.FIELD_I_EVENT_FROM, false, true);
        defaultDisplayFields.add(displayField);
        displayField = new EtkDisplayField(iPartsConst.TABLE_IMAGES, iPartsConst.FIELD_I_EVENT_TO, false, true);
        defaultDisplayFields.add(displayField);
        displayField = new EtkDisplayField(iPartsConst.TABLE_IMAGES, iPartsConst.FIELD_I_PSK_VARIANT_VALIDITY, false, true);
        defaultDisplayFields.add(displayField);

        return defaultDisplayFields;
    }

    @Override
    public GuiPanel getGui() {
        return mainWindow.panelMain;
    }

    public ModalResult showModal() {
        return mainWindow.showModal();
    }

    @Override
    public void close() {
        // Schließen in einem Thread mit invokeThreadSafe(), weil es ansonsten zu Race-Conditions kommen kann und nicht
        // alle Fenster sauber geschlossen werden
        Session.invokeThreadSafeInSessionWithChildThread(() -> {
            // Kopie mit ArrayList, um ConcurrentModificationExceptions zu vermeiden
            for (GuiWindow nonModalWindow : new ArrayList<>(nonModalChildWindows)) {
                nonModalWindow.setVisible(false);
            }
        });

        showPictureOrderForm.close();
        mainWindow.setVisible(false);
        super.close();
    }

    public EditModuleFormIConnector getConnector() {
        return (EditModuleFormIConnector)super.getConnector();
    }

    public void doUpdateView() {
        assemblyImage.updateView();
    }

    public boolean isEditAllowed() {
        return isEditAllowed;
    }

    public void setEditAllowed(boolean isEditAllowed) {
        if (this.isEditAllowed == isEditAllowed) {
            return;
        }

        this.isEditAllowed = isEditAllowed;
        enableButtons();
    }

    public void handleDividerPos(boolean dockingPanelIsClosed) {
        if (dockingPanelIsClosed) {
            dividerPos = mainWindow.splitPanePicOrders.getDividerPosition();
        } else {
            if (mainWindow.dockingpanel_PicOrders.isShowing()) {
                if (dividerPos != -1) {
                    mainWindow.splitPanePicOrders.setDividerPosition(dividerPos);
                }
            }
            dividerPos = -1;
        }
    }

    @Override
    public void modifyConnectorBeforeUpdateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        super.modifyConnectorBeforeUpdateData(sender, forceUpdateAll);
        assemblyImage.modifyConnectorBeforeUpdateData(sender, forceUpdateAll);
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        if (getConnector().isFlagUserInterfaceLanguageChanged()) {
            grid.setDisplayFields(getDisplayFields()); // Tabellen-Überschriften neu auslesen mit der Viewer-Sprache
        }
        if (forceUpdateAll || getConnector().isFlagDataChanged() || getConnector().isAnyLanguageChanged()) {
            updateTableImages(-1);
        }
        if (getConnector().isFlagThumbnailImageIndexChanged()) {
            getTable().setSelectedRow(getConnector().getThumbnailImageIndex(), true, true);
        }
    }

    @Override
    public void afterUpdateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        super.afterUpdateData(sender, forceUpdateAll);
        assemblyImage.afterUpdateData(sender, forceUpdateAll);
    }

    public void addEventListener(AssemblyImageFormEvents eventObject) {
        eventObjects.add(eventObject);
    }

    public void removeEventListener(AssemblyImageFormEvents eventObject) {
        eventObjects.remove(eventObject);
    }

    private GuiTable getTable() {
        return grid.getTable();
    }

    private void createToolbarButtons() {
        ToolbarButtonMenuHelper.ToolbarMenuHolder holder;
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.IMG_NEW, getUITranslationHandler(), new MenuRunnable() {
            public void run(Event event) {
                doNewImage(event);
            }
        });
        contextmenuTableImages.addChild(holder.menuItem);
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.IMG_EDIT, getUITranslationHandler(), new MenuRunnable() {
            public void run(Event event) {
                doEditPictureAttributes(event);
            }
        });
        contextmenuTableImages.addChild(holder.menuItem);
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.IMG_DELETE, getUITranslationHandler(), new MenuRunnable() {
            public void run(Event event) {
                doDeleteImage(event);
            }
        });
        contextmenuTableImages.addChild(holder.menuItem);
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.IMG_REPLACE, getUITranslationHandler(), new MenuRunnable() {
            public void run(Event event) {
                doReplaceImage(event);
            }
        });
        contextmenuTableImages.addChild(holder.menuItem);
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.IMG_UP, getUITranslationHandler(), new MenuRunnable() {
            public void run(Event event) {
                doMoveImageUp(event);
            }
        });
        contextmenuTableImages.addChild(holder.menuItem);
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.IMG_DOWN, getUITranslationHandler(), new MenuRunnable() {
            public void run(Event event) {
                doMoveImageDown(event);
            }
        });

        if (assemblyImage.isWithPreviewConfig()) {
            contextmenuTableImages.addChild(holder.menuItem);
            holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_VIEW_ALL_PAGES, getUITranslationHandler(), new MenuRunnable() {
                public void run(Event event) {
                    doToggleThumbnails(event);
                }
            });
            contextmenuTableImages.addChild(holder.menuItem);
        }

        GuiMenuItem menuItemOpenInNewWindow = toolbarHelper.createContextMenuEntry(EditToolbarButtonAlias.IMG_OPEN_IN_NEW_WINDOW,
                                                                                   getUITranslationHandler(), new EventListener(Event.MENU_ITEM_EVENT,
                                                                                                                                EventListenerOptions.SYNCHRON_EVENT) {
                    @Override
                    public void fire(Event event) {
                        doOpenImagesInNewWindow();
                    }
                });
        contextmenuTableImages.addChild(menuItemOpenInNewWindow);

        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.IMG_PICORDER, getUITranslationHandler(), new MenuRunnable() {
            public void run(Event event) {
                doCreatePicOrder(null, true);
            }
        });
        contextmenuTableImages.addChild(holder.menuItem);
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.IMG_REFRESH, getUITranslationHandler(), new MenuRunnable() {
            public void run(Event event) {
                showPictureOrderForm.refreshTableView();
                doRefreshPicOrder(event, true);
            }
        });
        contextmenuTableImages.addChild(holder.menuItem);

        // Änderungsauftrag für Bilder aus der Migration
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.IMG_PIC_CHANGE_ORDER, getUITranslationHandler(), new MenuRunnable() {
            public void run(Event event) {
                doChangeOrderForExistingPicture();
            }
        });
        contextmenuTableImages.addChild(holder.menuItem);

        // Kopierauftrag auf Basis eines Bildes
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.IMG_COPY_PIC_ORDER, getUITranslationHandler(), new MenuRunnable() {
            public void run(Event event) {
                doCopyOrderForExistingPicture();
            }
        });
        contextmenuTableImages.addChild(holder.menuItem);

        GuiMenuItem menuItemOpenMCInASPLM = toolbarHelper.createContextMenuEntry(EditToolbarButtonAlias.IMG_OPEN_MC_IN_ASPLM,
                                                                                 getUITranslationHandler(), new EventListener(Event.MENU_ITEM_EVENT,
                                                                                                                              EventListenerOptions.SYNCHRON_EVENT) {
                    @Override
                    public void fire(Event event) {
                        String mediaContainer = iPartsPicOrderEditHelper.getMediaContainerFromImage(getSelectedImage());
                        PicOrdersToModuleGridForm.openMediaContainerInASPLM(mediaContainer);
                    }
                });
        contextmenuTableImages.addChild(menuItemOpenMCInASPLM);


        // Toolbar-Button für das Anzeigen aller Bildaufträge
        filterPicOrdersButton = toolbarHelper.addToolbarButton(EditToolbarButtonAlias.IMG_FILTER_PIC_ORDERS,
                                                               new EventListener(Event.ACTION_PERFORMED_EVENT,
                                                                                 EventListenerOptions.SYNCHRON_EVENT) {
                                                                   @Override
                                                                   public void fire(Event event) {
                                                                       showPictureOrderForm.handlePicOrdersVisibility(filterPicOrdersButton.isPressed());
                                                                       updatePictureOrders();
                                                                   }
                                                               });

        menuItemMultipleUse = toolbarHelper.createContextMenuEntry(EditToolbarButtonAlias.IMG_MULTIPLE_USE, getUITranslationHandler(), null);
        contextmenuTableImages.addChild(menuItemMultipleUse);


        GuiSeparator separator = toolbarHelper.createMenuSeparator("menuSeparator", getUITranslationHandler());
        contextmenuTableImages.addChild(separator);

        GuiMenuItem menuItemCopy = toolbarHelper.createCopyMenuForTable(getTable(), getUITranslationHandler());
        contextmenuTableImages.addChild(menuItemCopy);
    }

    private void doCopyOrderForExistingPicture() {
        createChangeOrderForExistingPicture(true);
    }

    /**
     * Erzeugt einen Änderungsauftrag für Bilder aus der Migration
     */
    private void doChangeOrderForExistingPicture() {
        createChangeOrderForExistingPicture(false);
    }

    private void createChangeOrderForExistingPicture(boolean isCopy) {
        if (!MQHelper.checkTransmissionToASPLMConfigWithMessage()) {
            return;
        }
        EtkDataImage image = getSelectedImage();
        if (image == null) {
            return;
        }
        // Check, ob es schon einen Bild-/Änderungsauftrag mit dem Bild gibt
        Optional<iPartsDataPicOrder> existingPicOrderForImage = iPartsPicOrderEditHelper.getNewestPicOrderForPicture(getProject(), image);
        if (existingPicOrderForImage.isPresent()) {
            // Es gibt schonen einen Bildauftrag zur Bildnummer -> auf diesem versuchen einen Bildauftrag zu erzeugen
            handleChangeOrderForPicInDifferentModule(existingPicOrderForImage.get().getOrderIdExtern(), isCopy);
        } else if (isASPLMPicture()) {
            // Handelt es sich um ein AS-PLM Bild, dass nicht in der DA_PICORDER_PICTURES vorkommt, dann kann es nur
            // über den veralteten Migrations-Sync-Import hereingekommen sein. Hier muss die MC Nummer via DA_PIC_REFERENCES
            // bestimmt werden
            createChangeOrderForMigratedPicture(image, isCopy);
        } else {
            createChangeOrderForNotYetProcessedPicture(image, isCopy);
        }
    }

    /**
     * Erzeugt einen Änderungsauftrag für eine Bildnummer, die bei der Migartion über den AS-PLM Service
     * abgerufen wurde.
     *
     * @param image
     * @param isCopy Flag, falls es ein Kopierauftrag ist
     */
    private void createChangeOrderForMigratedPicture(EtkDataImage image, boolean isCopy) {
        iPartsDataPicReferenceList picReferenceList = iPartsDataPicReferenceList.loadPicRefWithSortedContainerIdForVariantId(getProject(), image);
        if (picReferenceList.isEmpty()) {
            MessageDialog.show(TranslationHandler.translate("!!Zur Bildnummer \"%1\" konnte keine MediaContainer Nummer" +
                                                            " gefunden werden!", image.getFieldValue(FIELD_I_IMAGES)));
        } else {
            Optional<iPartsDataPicOrder> dataPicOrder = createDataPicOrderForNotYetProcessedPicture(image,
                                                                                                    iPartsTransferStates.CHANGE_CREATED,
                                                                                                    isCopy);
            dataPicOrder.ifPresent(picOrder -> {
                // MC Nummer und Revision setzen
                iPartsDataPicReference picReference = picReferenceList.getLast();
                String mcItemId = picReference.getMcItemId();
                String mcItemRevId = picReference.getMcItemRevId();
                if (StrUtils.isValid(mcItemId, mcItemRevId)) {
                    picOrder.setFieldValue(FIELD_DA_PO_ORDER_ID_EXTERN, mcItemId, DBActionOrigin.FROM_EDIT);
                    picOrder.setFieldValue(FIELD_DA_PO_ORDER_REVISION_EXTERN, mcItemRevId, DBActionOrigin.FROM_EDIT);
                    // Zeig den validen Änderungsauftrag an
                    showCreatedChangeOrderForNotYetProcessedPicture(picOrder);
                    // Aktualisieren damit die Referenz zum Bild aufgebaut wird
                    updateTableImages(-1);
                } else {
                    // MC Nummer nicht vorhanden -> Anfrage an AS-PLM nicht möglich. Meldung ausgeben und Auftrag löschen
                    MessageDialog.show(TranslationHandler.translate("!!Zur Bildnummer \"%1\" wurde ein Eintrag gefunden," +
                                                                    " der keine MediaContainer Nummer enthält!",
                                                                    image.getFieldValue(FIELD_I_IMAGES)));
                    showPictureOrderForm.deletePicOrder(picOrder, null, true);
                }
            });
        }
    }

    /**
     * Erzeugt einen Änderungsauftrag zu einem existierenden Bild, dass schon in einem Bildauftrag in einem anderen TU
     * verarbeitet wurde
     *
     * @param mcItemId
     * @param isCopy   Flag, falls es ein Kopierauftrag ist
     */
    private void handleChangeOrderForPicInDifferentModule(String mcItemId, boolean isCopy) {
        EtkDataAssembly assembly = getConnector().getCurrentAssembly();
        if (!(assembly instanceof iPartsDataAssembly)) {
            return;
        }

        // Alle Aufträge zur MC Nummer laden. Es muss mind. einen geben, weil wir ja den übergebenen über die Bild zu Auftrag
        // Tabelle bestimmt haben
        List<iPartsDataPicOrder> picOrders = iPartsDataPicOrderList.loadPicOrdersForMCItemId(getProject(), mcItemId).getAsList();
        if (picOrders.isEmpty()) {
            // Kann nie passieren -> nur zur Sicherheit
            return;
        }
        // Wenn zur Bildnummer nur ungültige oder stornierte Aufträge gibt, muss ein FAKE-Änderungsauftrag erzeugt werden
        if (!hasAtLeastOneValidOrder(picOrders)) {
            EtkDataImage image = getSelectedImage();
            if (image != null) {
                createChangeOrderForNotYetProcessedPicture(image, isCopy);
                return;
            }
        }
        // Überprüfen, ob es neuere Aufträge gibt, die zum Bild gehören und die wir als Basis für einen Änderungsauftrag
        // nehmen können
        int pictureCountBeforeChangeOrderCreation = assembly.getImageCount();
        Optional<iPartsDataPicOrder> existingPicOrderForImage = iPartsPicOrderEditHelper.checkExistingPicOrdersForChangeOrder(getProject(),
                                                                                                                              picOrders, null,
                                                                                                                              (iPartsDataAssembly)assembly, isCopy);
        if (!existingPicOrderForImage.isPresent()) {
            // Es gibt keinen neueren bzw. der neueste Auftrag ist nicht abgeschlossen
            // -> Anzeige laden (es kanns sein, das bestehende Bilder übernommen wurden
            if (assembly.getImageCount() != pictureCountBeforeChangeOrderCreation) {
                fireDataChangedEventAndUpdateThumbnails(true);
                updateTableImages(-1);
            }
            return;
        }
        // Änderungsauftrag auf dem "fremden" Auftrag erzeugen. Da dieser nicht aus dem aktuellen TU kommt, muss die
        // Verwendung und das verknüpfte Modul neu gesetzt werden
        iPartsDataPicOrder changeOrder = existingPicOrderForImage.get().createChangeOrder(isCopy);
        changeOrder.getUsages().clear(DBActionOrigin.FROM_EDIT);
        if (!addUsage(changeOrder).isPresent()) {
            return;
        }
        // Selektiertes Bild holen und die Gültigkeiten des Bildes kopieren
        EtkDataImage image = getSelectedImage();
        changeOrder.copyValuesFromImageToPicOrder(image);
        changeOrder.getModules().clear(DBActionOrigin.FROM_EDIT);
        changeOrder.addModule(assembly.getAsId().getKVari(), DBActionOrigin.FROM_EDIT);
        // Der Änderungsauftrag muss in die DB geschrieben werden, weil sonst in der Zeit des Ausfüllens ein anderer
        // Autor einen Änderungsafutrag erzeugen könnte.
        PicOrderMainForm.savePicOrder(getProject(), changeOrder);
        showPictureOrderForm.doRefreshPicOrder(null);
        showCreatedChangeOrderForNotYetProcessedPicture(changeOrder);
    }

    /**
     * Zeigt den Änderungsafutrag-Dialog für eine Bildnummer, die in keinem Bildauftrag vorkommt (B-Bildnummer oder
     * PV Nummer aus der Migraton).
     *
     * @param changeOrder
     */
    private void showCreatedChangeOrderForNotYetProcessedPicture(iPartsDataPicOrder changeOrder) {
        if (showPictureOrder(changeOrder)) {
            // Den neuen Änderungsauftrag speichern, alles aktualisieren und den Auftrag anzeigen
            PicOrderMainForm.savePicOrder(getProject(), changeOrder);
            refreshAfterPicOrderCreation(changeOrder);
        } else {
            // Löschen, da der Änderungsafutrag abgebrochen wurde
            showPictureOrderForm.deletePicOrder(changeOrder, null, true);
        }
    }

    private boolean hasAtLeastOneValidOrder(List<iPartsDataPicOrder> picOrders) {
        if ((picOrders == null) || picOrders.isEmpty()) {
            return false;
        }
        for (iPartsDataPicOrder picorder : picOrders) {
            if (picorder.isValid() && !picorder.isCancelled()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Erzeugt einen Änderungsauftrag zu einem existierenden Bild, dass noch in keinem anderen Bildauftrag verarbeitet
     * wurde
     *
     * @param image
     * @param isCopy Flag, falls es ein Kopierauftrag ist
     */
    private void createChangeOrderForNotYetProcessedPicture(EtkDataImage image, boolean isCopy) {
        Optional<iPartsDataPicOrder> dataPicOrder = createDataPicOrderForNotYetProcessedPicture(image,
                                                                                                iPartsTransferStates.MC_NUMBER_REQUESTED,
                                                                                                isCopy);
        dataPicOrder.ifPresent(picOrder -> {
            // Suchanfrage erzeugen
            if (!showPictureOrderForm.startMCNumberSearch(picOrder, image.getImagePoolNo())) {
                showPictureOrderForm.handleInternalErrorForMCRequest(picOrder);
                PicOrderMainForm.savePicOrder(getProject(), picOrder);
                refreshAfterPicOrderCreation(picOrder);
            }
        });
    }

    /**
     * Erzeugt einen Bildauftrag für eine Bildnummer, die noch in keinem Bildauftrag vorkommt
     *
     * @param image
     * @param state
     * @param isCopy Flag, falls es ein Kopierauftrag ist
     * @return
     */
    private Optional<iPartsDataPicOrder> createDataPicOrderForNotYetProcessedPicture(EtkDataImage image, iPartsTransferStates state,
                                                                                     boolean isCopy) {
        Map<PartListEntryId, EtkDataPartListEntry> selectedPartListEntries = getPartListEntriesForImage(image);
        if (selectedPartListEntries == null) {
            return Optional.empty();
        }

        // Bildauftrag erzeugen und Bild anhängen
        iPartsDataPicOrder dataPicOrder = doCreatePicOrder(selectedPartListEntries.values(), false);
        if (dataPicOrder == null) {
            return Optional.empty();
        }
        for (iPartsDataPicOrderPart partPosition : dataPicOrder.getParts()) {
            // Alle Bildpositionen durchgehen und SENT = true und Sequenznummer = 1 setzen
            // So tun, als ob die Positionen schon einmal in einem Bidlauftrag Workflow waren:
            // - SENT Feld auf true setzen
            // - Bild Sequenznummer auf "1" setzen
            // - Den aktuellen Stücklisteneintrag als JSON am aktuellen iPartsDataPicOrderPart speichern
            partPosition.setFieldValueAsBoolean(FIELD_DA_PPA_SENT, true, DBActionOrigin.FROM_EDIT);
            partPosition.setFieldValue(FIELD_DA_PPA_SEQ_NO, "1", DBActionOrigin.FROM_EDIT);
            PartListEntryId entryId = partPosition.getAsId().getPartListEntryId();
            EtkDataPartListEntry dataPartListEntry = selectedPartListEntries.get(entryId);
            if (dataPartListEntry != null) {
                partPosition.fillFromRealEntry(dataPartListEntry);
            }
        }
        dataPicOrder.copyValuesFromImageToPicOrder(image);
        iPartsDataPicOrderPicture picture = dataPicOrder.addPicture(image.getImagePoolNo(), image.getImagePoolVer(), DBActionOrigin.FROM_EDIT);
        // Fake Infos setzen (echter Designer ist nicht bekannt -> aktuellen Benutzer als Designer setzen)
        picture.setFieldValue(FIELD_DA_POP_DESIGNER, FrameworkUtils.getUserName(), DBActionOrigin.FROM_EDIT);
        picture.setFieldValue(FIELD_DA_POP_VAR_TYPE, iPartsColorTypes.NEUTRAL.getDbValue(), DBActionOrigin.FROM_EDIT);
        picture.setFieldValueAsDateTime(FIELD_DA_POP_LAST_MODIFIED, Calendar.getInstance(), DBActionOrigin.FROM_EDIT);
        // Status setzen
        dataPicOrder.setStatus(state, DBActionOrigin.FROM_EDIT);
        // Fake-Vorgänger setzen
        dataPicOrder.setFakeOriginalPicOrder();
        // Handelt es sich um einen Kopierauftrag?
        dataPicOrder.setFieldValueAsBoolean(FIELD_PO_IS_COPY, isCopy, DBActionOrigin.FROM_EDIT);

        // 4. Bildauftrag speichern
        PicOrderMainForm.savePicOrder(getProject(), dataPicOrder);
        refreshAfterPicOrderCreation(dataPicOrder);
        return Optional.of(dataPicOrder);
    }

    /**
     * Bestimmt die Stücklistenpositionen, die mit dem Bildauftrag für eine migrierte Zeichnung, verknüpft werden.
     *
     * @param image
     * @return
     */
    private Map<PartListEntryId, EtkDataPartListEntry> getPartListEntriesForImage(EtkDataImage image) {
        Map<PartListEntryId, EtkDataPartListEntry> result = null;
        if (image != null) {
            result = new LinkedHashMap<>();
            Map<String, List<EtkDataPartListEntry>> hotSpotToEntriesWithoutKemTo = new LinkedHashMap<>();
            Map<String, List<EtkDataPartListEntry>> hotSpotToEntriesWithKemTo = new LinkedHashMap<>();
            // HotSpots bestimmen
            Set<String> hotSpots = getConnector().getHotSpotSetPerImage(image);
            // Alle Positionen durchlaufen und die herausziehen, die einen der gesuchten HotSpots haben.
            for (EtkDataPartListEntry partListEntry : getConnector().getCurrentPartListEntries()) {
                String hotSpot = partListEntry.getFieldValue(iPartsConst.FIELD_K_POS);
                if (hotSpots.contains(hotSpot)) {
                    // Prüfen, ob ein KEM bis Datum existiert. Positionen nach diesem Kriterium in die jeweilige Map legen
                    String kemToDate = partListEntry.getFieldValue(FIELD_K_DATETO);
                    if (StrUtils.isEmpty(kemToDate)) {
                        addEntryToMap(partListEntry, hotSpot, hotSpotToEntriesWithoutKemTo);
                        // Positionen ohne Datum werden bevorzugt -> Entferne Einträge für den HotSpot aus der anderen Map
                        hotSpotToEntriesWithKemTo.remove(hotSpot);
                    } else {
                        // Positionen ohne Datum werden bevorzugt -> Einträge für HotSpots, die schon in der anderen Map
                        // sind sollen nicht angelegt werden
                        if (!hotSpotToEntriesWithoutKemTo.containsKey(hotSpot)) {
                            addEntryToMap(partListEntry, hotSpot, hotSpotToEntriesWithKemTo);
                        }
                    }
                }
            }
            // Erst alle Hotspots mit Positionen befüllen, die kein KEM bis Datum haben
            findValidEntryForHotSpot(result, hotSpotToEntriesWithoutKemTo.values());
            // Falls zu einem Hotspot nur Positionen existieren, die ein KEM bis Datum haben
            findValidEntryForHotSpot(result, hotSpotToEntriesWithKemTo.values());
        }
        return result;
    }

    /**
     * Bestimmt pro übergebener Liste den idealen Stücklisteneintrag für den erzeugten Änderungsauftrag
     * <p>
     * Kriterien sind:
     * - Datensatz enthält kein "KEM bis" bzw. nur "KEM ab" (wurde schon in getPartListEntriesForImage() aufgeteilt)
     * - ist kein Datensatz oder sind mehrere Datensätze mir nur "KEM ab" vorhanden, wird der jüngste Stand zur längsten/höchsten Code-Regel ausgewählt
     *
     * @param result
     * @param entriesForOneHotspot
     */
    private void findValidEntryForHotSpot(Map<PartListEntryId, EtkDataPartListEntry> result, Collection<List<EtkDataPartListEntry>> entriesForOneHotspot) {
        for (List<EtkDataPartListEntry> entries : entriesForOneHotspot) {
            // Wenn in der übergebenen Liste nur ein Eintrag existiert, dann ist der Stücklisteneintrag gültig
            if (entries.size() == 1) {
                result.put(entries.get(0).getAsId(), entries.get(0));
                continue;
            }
            // Aufsteigend nach Kem ab Datum sortieren
            entries.sort((o1, o2) -> {
                String o1KemFromDate = o1.getFieldValue(FIELD_K_DATEFROM);
                String o2KemFromDate = o2.getFieldValue(FIELD_K_DATEFROM);
                long o1DateFrom = StrUtils.strToLongDef(o1KemFromDate, -1);
                long o2DateFrom = StrUtils.strToLongDef(o2KemFromDate, -1);
                return Long.compare(o2DateFrom, o1DateFrom);
            });
            String earliestDate = null;
            String longestCode = null;
            EtkDataPartListEntry currentEntry = null;
            // Sortierte Liste durchlaufen
            for (EtkDataPartListEntry entry : entries) {
                // Datum und Code des aktuellen Eintrags
                String currentDateFrom = entry.getFieldValue(FIELD_K_DATEFROM);
                String currentCode = entry.getFieldValue(FIELD_K_CODES);
                // Ist es der erste Eintrag, dann wird dieser zwischengespeichert
                if (earliestDate == null) {
                    earliestDate = currentDateFrom;
                    longestCode = currentCode;
                    currentEntry = entry;
                    continue;
                }
                // Gibt es schon einen Eintrag und der aktuelle ist älter, dann springe hier raus und nehme den
                // zwischengespeicherten Stücklisteneintrag
                if (!earliestDate.equals(currentDateFrom)) {
                    break;
                }
                // Haben bisheriger und aktueller Eintrag das gleiche Datum, dann prüfe die Länge der Code
                // Code der aktuellen Position ist länger als der bisherige Code -> aktuelle Position zwischenspeichern.
                // Ansonsten weitersuchen nach einem längeren Code.
                if (longestCode.length() < currentCode.length()) {
                    longestCode = currentCode;
                    currentEntry = entry;
                }
            }
            if (currentEntry != null) {
                result.put(currentEntry.getAsId(), currentEntry);
            }
        }
    }

    private void addEntryToMap(EtkDataPartListEntry partListEntry, String hotSpot, Map<String, List<EtkDataPartListEntry>> hotSpotToEntriesWithoutKemTo) {
        List<EtkDataPartListEntry> entries = hotSpotToEntriesWithoutKemTo.computeIfAbsent(hotSpot, k -> new ArrayList<>());
        entries.add(partListEntry);
    }

    /**
     * Ermöglicht es die Attribute einer Bildreferenz zu editieren
     *
     * @param event
     */
    private void doEditPictureAttributes(Event event) {
        EtkDataImage image = getSelectedImage();
        if (image != null) {
            if (EditUserControlForPictureReference.editPictureReferenceAttributes(getConnector(), this, image.getAsId())) {
                // Damit die Stückliste samt Filterung neu geladen wird
                fireDataChangedEventAndUpdateThumbnails(true);
                updateTableImages(-1, false);
            }
        }
    }

    /**
     * Liefert das selektierte Bild
     *
     * @return
     */
    private EtkDataImage getSelectedImage() {
        int imageIndex = findImageIndexByBlattNumber(getSelectedBlattNr());
        if (imageIndex != -1) {
            return getConnector().getCurrentAssembly().getImage(imageIndex, false);
        }
        return null;
    }

    private void setTableImages() {
        DBDataObjectList<EtkDataImage> images = getConnector().getCurrentAssembly().getUnfilteredImages();
        iPartsPicOrderEditHelper.addMCNumbersToPictures(getProject(), images);
        images.forEach(image -> grid.addObjectToGrid(image));
    }

    private void clearTableImages() {
        getTable().switchOffEventListeners();

        // Virtuelle Felder für Mehrfachverwendung entfernen, damit diese neu berechnet werden
        for (EtkDataImage dataImage : grid.getDataObjectList(EtkDataImage.class)) {
            dataImage.getAttributes().deleteField(iPartsDataVirtualFieldsDefinition.DA_IS_MULTIPLE_USED, false, DBActionOrigin.FROM_DB);
        }

        grid.clearGrid();
        getTable().switchOnEventListeners();
    }

    private void fireDataChangedEventAndUpdateThumbnails(boolean fireEvent) {
        // Änderungen an den Bildern (moveUp/Down, neue Bilder oder Bilder löschen)
        // funktioniert nicht, wenn die Thumbnail-Einstellung aktiv ist
        // => erstmal abschalten
        assemblyImage.resetImages();
        boolean isThumbnailViewActive = getConnector().isThumbnailViewActive();
        int imageIndex = getConnector().getImageIndex();
        if (!isThumbnailViewActive) {
            // ImageIndex ohne Thumbnail-Anzeige zwischenzeitlich auf -1 setzen, damit das Einzelbild nach obigem resetImages()
            // neu gezeichnet wird
            assemblyImage.setImageIndex(-1, false, false);

            // GUI nur ohne Thumbnail-Anzeige sofort aktualisieren, weil mit Thumbnail-Anzeige wird das unten bei setShowThumbnails() gemacht
            assemblyImage.setImageIndex(imageIndex, false, true);
        }

        try {
            if (fireEvent) { // DataChangedEvent nur feuern wenn nötig
                isInDataChangedEvent = true;
                // => Änderungen übernehmen
                getProject().fireProjectEvent(new DataChangedEvent(this), true);
            }
        } finally {
            isInDataChangedEvent = false;
            // => ggf. Thumbnail-Darstellung wieder einschalten
            if (isThumbnailViewActive && (getConnector().getCurrentAssembly().getImageCount(false) > 1)) {
                assemblyImage.setShowThumbnails(true, true);
            } else {
                updateViewThumbnailsButton();
            }
        }
    }

    private void updateTableImages(int selectedImageRowIndex) {
        updateTableImages(selectedImageRowIndex, true);
    }

    /**
     * Aktualisiert die Tabelle mit den Zeichnungen und setzt die Auswahl auf den angegebenen Index bzw. behält bei {@code -1}
     * die Auswahl bei.
     *
     * @param selectedImageRowIndex
     */
    private void updateTableImages(int selectedImageRowIndex, boolean withReload) {
        if (selectedImageRowIndex < 0) {
            selectedImageRowIndex = getTable().getSelectedRowIndex();
        }
        if (withReload) {
            clearTableImages();
            setTableImages();
        }
        selectedImageRowIndex = Math.min(selectedImageRowIndex, getTable().getRowCount() - 1);
        if (selectedImageRowIndex >= 0) {
            getTable().setSelectedRow(selectedImageRowIndex, true, !isInDataChangedEvent);
        } else if (getTable().getRowCount() > 0) {
            getTable().setSelectedRow(0, true, !isInDataChangedEvent);
        } else {
            assemblyImage.setImageIndex(-1, false, true);
        }
        enableButtons();
        updateStatusText();
    }


    private String getSelectedBlattNr() {
        List<EtkDataObject> selectedRows = grid.getSelection();

        if ((selectedRows != null) && !selectedRows.isEmpty()) {
            EtkDataObject objectFromTable = selectedRows.get(0);
            return objectFromTable.getFieldValue(EtkDbConst.FIELD_I_BLATT);
        } else {
            return "";
        }
    }

    private Collection<String> getSelectedBlattNumbers() {
        List<List<EtkDataObject>> selectedRows = grid.getMultiSelection();
        Collection<String> result = new LinkedHashSet<>();

        if ((selectedRows != null) && !selectedRows.isEmpty()) {
            for (List<EtkDataObject> dataObjectList : selectedRows) {
                EtkDataObject objectFromTable = dataObjectList.get(0);
                result.add(objectFromTable.getFieldValue(EtkDbConst.FIELD_I_BLATT));
            }
        }
        return result;
    }

    private void setSelectedRow(int rowIndex) {
        getTable().setSelectedRow(rowIndex, true);
    }

    private void updatePictureOrders() {
        if (picOrdersVisible()) {
            if (!mainWindow.dockingpanel_PicOrders.isShowing()) {
                mainWindow.dockingpanel_PicOrders.setShowing(true);

                // DividerPosition setzen falls die Bildaufträge sichtbar werden
                mainWindow.splitPanePicOrders.setDividerPosition(getSplitPanePicOrdersDivierAutoPosition());
            }
        } else {
            if (!picOrdersDockingPanelInitialized) {
                picOrdersDockingPanelInitialized = true;

                // Temporär das DockingPanel auf showing setzen, damit setShowing(false) weiter unten die korrekte Divider-Position
                // ermitteln kann
                mainWindow.dockingpanel_PicOrders.setShowing(true);
            }

            if (mainWindow.dockingpanel_PicOrders.isShowing()) {
                mainWindow.dockingpanel_PicOrders.setShowing(false);
            }
        }
    }

    private int getSplitPanePicOrdersDivierAutoPosition() {
        // + 30 wegen Paddings und möglicher horizontaler Scrollbar
        return Math.min(splitPanePicOrdersHeight - 100, Math.max(mainWindow.toolbarHolderPanel.getPreferredHeight() + grid.getTable().getPreferredHeight() + 30,
                                                                 splitPanePicOrdersHeight / 2));
    }

    private void enableButtons() {
        int rowCount = getTable().getRowCount();
        int selectedIndex = getTable().getSelectedRowIndex();
        int selectionRowCount = getTable().getSelectedRows().size();
        boolean singleSelectionEnabled = selectionRowCount == 1;
        boolean multiSelectionEnabled = selectionRowCount > 0;

        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_REFRESH, contextmenuTableImages, true);
        int imageCount = getConnector().getCurrentAssembly().getImageCount(false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_VIEW_ALL_PAGES, contextmenuTableImages, imageCount > 1);
        toolbarHelper.enableMenu(EditToolbarButtonAlias.IMG_OPEN_IN_NEW_WINDOW, contextmenuTableImages, multiSelectionEnabled);
        EtkDataImage selectedImage = getSelectedImage();
        toolbarHelper.enableMenu(EditToolbarButtonAlias.IMG_OPEN_MC_IN_ASPLM, contextmenuTableImages, singleSelectionEnabled
                                                                                                      && !iPartsPicOrderEditHelper.getMediaContainerFromImage(selectedImage).isEmpty());
        if (isEditAllowed) {
            toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_NEW, contextmenuTableImages, true);
            toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_EDIT, contextmenuTableImages, singleSelectionEnabled);
            toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_DELETE, contextmenuTableImages, multiSelectionEnabled);
            toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_REPLACE, contextmenuTableImages, singleSelectionEnabled);
            toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_COPY, contextmenuTableImages, singleSelectionEnabled || multiSelectionEnabled);
            toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_UP, contextmenuTableImages, singleSelectionEnabled && (selectedIndex > 0));
            toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_DOWN, contextmenuTableImages, singleSelectionEnabled && (selectedIndex < (rowCount - 1)));
            toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_PICORDER, contextmenuTableImages, true);
            boolean isChangeOrderForMigrationPicAllowed = isChangeOrderForMigrationPicAllowed();
            toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_PIC_CHANGE_ORDER, contextmenuTableImages, singleSelectionEnabled && isChangeOrderForMigrationPicAllowed);
            toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_COPY_PIC_ORDER, contextmenuTableImages, singleSelectionEnabled && isChangeOrderForMigrationPicAllowed);
        } else {
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_NEW, contextmenuTableImages);
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_EDIT, contextmenuTableImages);
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_DELETE, contextmenuTableImages);
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_REPLACE, contextmenuTableImages);
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_COPY, contextmenuTableImages);
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_UP, contextmenuTableImages);
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_DOWN, contextmenuTableImages);
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_PICORDER, contextmenuTableImages);
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_PIC_CHANGE_ORDER, contextmenuTableImages);
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_COPY_PIC_ORDER, contextmenuTableImages);
        }
        toolbarHelper.enableMenu(EditToolbarButtonAlias.IMG_FILTER_PIC_ORDERS, contextmenuTableImages, !getConnector().getPictureOrderList().isEmpty());

        boolean isMultipleUseVisible = singleSelectionEnabled && (selectedImage != null) && selectedImage.getFieldValueAsBoolean(iPartsDataVirtualFieldsDefinition.DA_IS_MULTIPLE_USED);
        toolbarHelper.setToolbarButtonAndMenuVisible(EditToolbarButtonAlias.IMG_MULTIPLE_USE, contextmenuTableImages, isMultipleUseVisible);
        if (isMultipleUseVisible) {
            updateMultipleUseSubMenuItems(selectedImage);
        }

        ThemeManager.get().render(mainWindow);
    }

    private void updateMultipleUseSubMenuItems(EtkDataImage selectedImage) {
        if (selectedImage instanceof iPartsDataImage) {
            iPartsDataImage image = (iPartsDataImage)selectedImage;
            Set<String> multipleUseModuleNumbers = image.getMultipleUseModuleNumbers(false);
            multipleUseModuleNumbers.remove(getConnector().getCurrentAssembly().getAsId().getKVari());
            menuItemMultipleUse.removeAllChildren();
            for (String moduleNumber : multipleUseModuleNumbers) {
                GuiMenuItem menuItemGotoModule = toolbarHelper.createMenuEntry("gotoMultipleUseModule" + moduleNumber, moduleNumber,
                                                                               null, new EventListener(Event.MENU_ITEM_EVENT) {
                            @Override
                            public void fire(Event event) {
                                EditModuleForm editModuleForm = EditTransferToASHelper.getEditModuleForm(getConnector());
                                if (editModuleForm != null) {
                                    editModuleForm.loadModule(moduleNumber);
                                }
                            }
                        }, getUITranslationHandler());
                menuItemMultipleUse.addChild(menuItemGotoModule);
            }
        }
    }


    /**
     * Checkt, ob der PopUp Menüpunkt und der Toolbarbutton für migrierte Bildnummern auswählbar ist
     * Kriterien:
     * - Es muss ein migriertes Bild sein (noch keine MC Nummer)
     * - Das Bild darf in keinem Bildauftrag vorkommen (z.B. bei einer fehlerhaften Anfrage)
     *
     * @return
     */
    private boolean isChangeOrderForMigrationPicAllowed() {
        EtkDataImage image = getSelectedImage();
        if (image == null) {
            return false;
        }
        for (iPartsDataPicOrder picOrder : showPictureOrderForm.getPicOrders()) {
            if (iPartsTransferStates.canResendMCSearchRequest(picOrder.getStatus())) {
                for (iPartsDataPicOrderPicture picture : picOrder.getPictures()) {
                    if (picture.getAsId().getPicItemId().equals(image.getImagePoolNo())) {
                        // Falls der Bildauftrag auf ungültig gesetzt wurde und was schief gegangen ist, soll ein
                        // neuer Änderungsauftrag möglich sein
                        if (picOrder.isValid()) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Prüft, ob das aktuell ausgewählte Bild ein Bild von AS-PLM ist. Ein Bild stammt von AS-PLM, wenn im virteullen
     * Feld für die MC Nummer ein Eintrag existiert. Als Fallback wird geprüft, ob der Bildname mit "PV" anfängt und
     * drei Punkte enthält. Falls nein, ist es ein Bild aus der Migration.
     *
     * @return
     */
    private boolean isASPLMPicture() {
        if (getTable().getSelectedRows().size() == 1) {
            EtkDataImage image = getSelectedImage();
            if (image != null) {
                if (!iPartsPicOrderEditHelper.getMediaContainerFromImage(image).isEmpty()) {
                    return true;
                } else {
                    String imageName = image.getImagePoolNo();
                    return XMLImportExportHelper.isASPLMPictureNumber(imageName);
                }
            }
        }
        return false;
    }

    private void tableSelectionEvent(Event event) {
        int imageIndex = findImageIndexByBlattNumber(getSelectedBlattNr());
        if (imageIndex < 0) {
            assemblyImage.markThumbnailImage(imageIndex);
            enableButtons();
            return;
        }
        if (getConnector().isMultiImageViewActive()) {
            if (MARK_THUMBNAILS_MULTI_SELECT) {
                List<Integer> imageIndexList = findImageIndecesByBlattNumberList(getSelectedBlattNumbers());
                assemblyImage.markThumbnailImages(imageIndexList);
            } else {
                assemblyImage.markThumbnailImage(imageIndex);
            }
        } else {
            if (getConnector().getImageIndex() != imageIndex) {
                assemblyImage.setImageIndex(imageIndex, false, true);
            }
        }
        enableButtons();
    }

    private void doNewImage(Event event) {
        final VarParam<Boolean> newWasClicked = new VarParam<>(false);
        OnNewEvent onNewEvent = null;
        onNewEvent = new OnNewEvent() {
            @Override
            public String onNewEvent(String startValue) {
                newWasClicked.setValue(true);
                return null;
            }

            @Override
            public String getButtonText() {
                return TranslationHandler.translate("!!Bildauftrag erstellen");
            }
        };
        DBDataObjectAttributesList attributesList = SelectSearchGridImage.searchImage(this, getConnector().getCurrentAssembly().getAsId(),
                                                                                      "!!Auswahl Zeichnungen", true, onNewEvent);
        if (!newWasClicked.getValue()) {
            if (attributesList != null) {
                // neue Zeichnungen hinzufügen
                List<EtkDataImage> imagesList = new DwList<>();
                for (DBDataObjectAttributes imageAttributes : attributesList) {
                    imagesList.add(getConnector().getCurrentAssembly().addImage(imageAttributes, DBActionOrigin.FROM_EDIT));
                }
                if (isRevisionChangeSetActiveForEdit()) {
                    addDataObjectToActiveChangeSetForEdit(getConnector().getCurrentAssembly());

                    // Nach dem Hinzufügen der neuen Zeichnungen den Gültigkeitsbereich der Zeichnungen aktualisieren
                    iPartsDataImage.updateAndSaveValidityScopeForImages(imagesList, getProject());
                }
                fireDataChangedEventAndUpdateThumbnails(true);
                updateTableImages(getTable().getRowCount(), false); // neue Zeile wird erst durch updateTableImages() hinzugefügt -> nicht getRowCount() - 1

                // DividerPosition setzen falls die Bildaufträge sichtbar sind
                if (mainWindow.dockingpanel_PicOrders.isShowing()) {
                    mainWindow.splitPanePicOrders.setDividerPosition(Math.max(mainWindow.splitPanePicOrders.getDividerPosition(),
                                                                              getSplitPanePicOrdersDivierAutoPosition()));
                }
            }
        } else {
            doCreatePicOrder(null, true);
        }
        //nur zum Testen
        //updatePictureOrders(mainWindow.splitpane_0.getDividerSize() == 0);
    }

    public void updateStatusText() {
        assemblyImage.updateStatusText();
    }

    private int findImageIndexByBlattNumber(String blattNr) {
        if (!blattNr.isEmpty()) {
            List<String> blattNrList = new DwList<>(1);
            blattNrList.add(blattNr);
            List<Integer> imageIndeces = findImageIndecesByBlattNumberList(blattNrList);
            if (!imageIndeces.isEmpty()) {
                return imageIndeces.get(0);
            }
        }
        return -1;
    }

    private List<Integer> findImageIndecesByBlattNumberList(Collection<String> blattNrList) {
        List<Integer> result = new DwList<>();
        if (!blattNrList.isEmpty()) {
            int imageCount = getConnector().getCurrentAssembly().getImageCount(false);
            DBDataObjectList<EtkDataImage> images = getConnector().getCurrentAssembly().getUnfilteredImages();
            for (int lfdNr = 0; lfdNr < imageCount; lfdNr++) {
                String blattNr = images.get(lfdNr).getBlattNr();
                if (blattNrList.contains(blattNr)) {
                    result.add(lfdNr);
                }
            }
        }
        return result;
    }

    private void deleteImages(List<GuiTableRow> selectedRows) {
        boolean changed = false;
        for (int i = selectedRows.size() - 1; i >= 0; i--) {
            GuiTableRow row = selectedRows.get(i);
            if (row instanceof DataObjectGrid.GuiTableRowWithObjects) {
                DataObjectGrid.GuiTableRowWithObjects rowWithObjects = (DataObjectGrid.GuiTableRowWithObjects)row;
                EtkDataObject image = rowWithObjects.getObjectForTable(iPartsConst.TABLE_IMAGES);
                if (image instanceof iPartsDataImage) {
                    int index = findImageIndexByBlattNumber(image.getFieldValue(iPartsConst.FIELD_I_BLATT));
                    if (index != -1) {
                        if (getConnector().getCurrentAssembly().deleteImage(index, false, DBActionOrigin.FROM_EDIT)) {
                            changed = true;
                        }
                    }
                }
            }
        }
        if (changed) {
            if (getConnector().getCurrentAssembly().reorderImageIndexNumbers(DBActionOrigin.FROM_EDIT)) {
                // Falls sich durch das Löschen die Blattnummern ändern, müssen die Array-IDs nachgezogen werden,
                // weil sie sonst auf Arrays zeigen die zur OldId gehörten.
                for (EtkDataImage image : getConnector().getCurrentAssembly().getUnfilteredImages()) {
                    // Nur Arrays berücksichtigen, die auch Werte besitzen
                    for (DBDataObjectAttribute attribute : image.getAttributes().getFields()) {
                        if ((attribute.getType() == DBDataObjectAttribute.TYPE.ARRAY) && !attribute.isEmpty()) {
                            // Die neue ID erzeugen
                            String newId = image.getAsId().toString("|");
                            String currentId = attribute.getArrayId();
                            // Ist die neue ID ungleich der aktuellen, dann hat sich das Bild bezüglich seiner Position verändert
                            // -> neue ID setzen
                            if (!newId.equals(currentId)) {
                                image.setIdForArray(attribute.getName(), newId, DBActionOrigin.FROM_EDIT);
                            }
                        }
                    }
                }
            }
            if (isRevisionChangeSetActiveForEdit()) {
                addDataObjectToActiveChangeSetForEdit(getConnector().getCurrentAssembly());
            }

            // temporär ImageIndex auf -1 setzen, damit bei Setzen vom evtl. gleichen ImageIndex nach dem Löschen die
            // neue Zeichnung an diesem (gleichen) Index z.B. von der Stückliste berücksichtigt werden kann
            getConnector().setImageIndex(-1);
            fireDataChangedEventAndUpdateThumbnails(true);

            updateTableImages(-1, false); // -1 für bisherige Selektion beibehalten
            // Das Löschen kann unter Umständen die Anzeige der Bildaufträge beeinflussen -> Anzeige der Aufträge aktualisieren
            showPictureOrderForm.refreshTableView();
        }
    }

    private void doDeleteImage(Event event) {
        List<GuiTableRow> selectedRows = getTable().getSelectedRows();
        if (!selectedRows.isEmpty() && (MessageDialog.showYesNo("!!Zeichnungszuordnung wirklich entfernen?") == ModalResult.YES)) {
            deleteImages(selectedRows);
        }
    }

    private void replaceAnImage(DBDataObjectAttributesList attributesList, int selectedIndex) {
        if ((attributesList != null) && (attributesList.size() == 1)) {
            // Zeichnung ersetzen
            DBDataObjectAttributes imageAttributes = attributesList.get(0);
            String blattNr = getSelectedBlattNr();
            if (StrUtils.isValid(blattNr)) {
                int index = findImageIndexByBlattNumber(blattNr);
                if (index != -1) {
                    EtkDataImage dataImage = getConnector().getCurrentAssembly().replaceImage(index, imageAttributes, DBActionOrigin.FROM_EDIT);
                    if (dataImage != null) {
                        if (isRevisionChangeSetActiveForEdit()) {
                            addDataObjectToActiveChangeSetForEdit(getConnector().getCurrentAssembly());

                            // Nach dem Ersetzen der Zeichnung den Gültigkeitsbereich der neuen Zeichnung aktualisieren
                            List<EtkDataImage> imagesList = new ArrayList<>(1);
                            imagesList.add(dataImage);
                            iPartsDataImage.updateAndSaveValidityScopeForImages(imagesList, getProject());
                        }

                        // temporär ImageIndex auf -1 setzen, damit bei Setzen vom evtl. gleichen ImageIndex nach dem
                        // Löschen die neue Zeichnung an diesem (gleichen) Index berücksichtigt werden kann
                        getConnector().setImageIndex(-1);
                        fireDataChangedEventAndUpdateThumbnails(true);

                        updateTableImages(selectedIndex, false);
                    }
                }
            }
        }

    }

    private void doReplaceImage(Event event) {
        int selectedIndex = getTable().getSelectedRowIndex();
        if (selectedIndex >= 0) {
            DBDataObjectAttributesList attributesList = SelectSearchGridImage.searchImage(this, getConnector().getCurrentAssembly().getAsId(),
                                                                                          "!!Auswahl Zeichnung", false, null);
            replaceAnImage(attributesList, selectedIndex);
        }
    }

    private void doMoveImageDown(Event event) {
        String blattNr = getSelectedBlattNr();
        if (!blattNr.isEmpty()) {
            int imageIndex = findImageIndexByBlattNumber(blattNr);
            if ((imageIndex != -1) && (imageIndex < (getConnector().getCurrentAssembly().getImageCount(false) - 1))) {
                if (getConnector().getCurrentAssembly().swapImage(imageIndex, imageIndex + 1, DBActionOrigin.FROM_EDIT)) {
                    if (isRevisionChangeSetActiveForEdit()) {
                        addDataObjectToActiveChangeSetForEdit(getConnector().getCurrentAssembly());
                    }
                    fireDataChangedEventAndUpdateThumbnails(true);
                    updateTableImages(imageIndex + 1, false);
                }
            }
        }
    }

    private void doMoveImageUp(Event event) {
        String blattNr = getSelectedBlattNr();
        if (!blattNr.isEmpty()) {
            int imageIndex = findImageIndexByBlattNumber(blattNr);
            if ((imageIndex > 0)) {
                if (getConnector().getCurrentAssembly().swapImage(imageIndex, imageIndex - 1, DBActionOrigin.FROM_EDIT)) {
                    if (isRevisionChangeSetActiveForEdit()) {
                        addDataObjectToActiveChangeSetForEdit(getConnector().getCurrentAssembly());
                    }
                    fireDataChangedEventAndUpdateThumbnails(true);
                    updateTableImages(imageIndex - 1, false);
                }
            }
        }
    }

    private void doToggleThumbnails(Event event) {
        if (getConnector().getCurrentAssembly().getImageCount(false) > 1) {
            GuiToolButton button = (GuiToolButton)toolbarManager.getButton(EditToolbarButtonAlias.EDIT_VIEW_ALL_PAGES.getAlias());
            button.setPressed(!button.isPressed());
            assemblyImage.setShowThumbnails(button.isPressed(), true);
            updateTableImages(-1);
        }
    }

    private void doOpenImagesInNewWindow() {
        List<EtkDataImage> dataImageList = new ArrayList<>();
        List<List<EtkDataObject>> selectedRows = grid.getMultiSelection();
        if ((selectedRows != null) && !selectedRows.isEmpty()) {
            for (List<EtkDataObject> dataObjectList : selectedRows) {
                EtkDataObject objectFromTable = dataObjectList.get(0); // analog getSelectedBlattNumbers()
                if (objectFromTable instanceof EtkDataImage) {
                    dataImageList.add((EtkDataImage)objectFromTable);
                }
            }
        }

        Dimension screenSize = FrameworkUtils.getScreenSize();
        EditShowImagesInWindow imagesWindow = new EditShowImagesInWindow(TranslationHandler.translate("!!Ausgewählte Bildtafeln für TU \"%1\"",
                                                                                                      String.valueOf(getConnector().getCurrentAssembly().getAsId().getKVari())),
                                                                         screenSize.width - 20, screenSize.height - 20,
                                                                         dataImageList, true, -1, getProject());

        imagesWindow.addEventListener(new EventListener(Event.SUB_WINDOW_CLOSED_EVENT) {
            @Override
            public void fire(Event event) {
                nonModalChildWindows.remove(imagesWindow);
            }
        });

        imagesWindow.showNonModal(GuiWindow.NonModalStyle.OPEN_IN_NEW_WINDOW);
        nonModalChildWindows.add(imagesWindow);
    }

    public void updateViewThumbnailsButton() {
        GuiToolButton allButton = (GuiToolButton)toolbarManager.getButton(EditToolbarButtonAlias.EDIT_VIEW_ALL_PAGES.getAlias());
        if (allButton != null) {
            allButton.setPressed(getConnector().isThumbnailViewActive());
        }
    }

    /**
     * Erzeugt einen vorbefüllten Bildauftrag. Mit <code>withDialog</code> kann bestimmt werden, ob sich der Dialog zum
     * ergänzen von weiteren Attributen öffnen soll.
     *
     * @param selectedPartListEntries
     * @param withDialog
     * @return
     */
    protected iPartsDataPicOrder doCreatePicOrder(Collection<EtkDataPartListEntry> selectedPartListEntries, boolean withDialog) {
        iPartsDataPicOrder dataPicOrder = iPartsDataPicOrder.createEmptyDataPicOrder(getProject(), getConnector().getCurrentAssembly().getAsId(), selectedPartListEntries);
        Optional<PRODUCT_STRUCTURING_TYPE> productType;
        // Bei freien SAs soll die Verortung fest vorbelegt werden. Hierfür wird das erste Produkt mit der ersten KG verwendet.
        // Zusätzlich wird der künstliche technische Umfang "001" angehängt
        productType = addUsage(dataPicOrder);
        if (!productType.isPresent()) {
            return null;
        }
        // in DAIMLER-15133 nicht gefordert
//        if (isCurrentAssemblyCarPerspective) {
//            EtkDataImage dataImage = getSelectedImage();
//            if (dataImage != null) {
//                String navPerspective = getProject().getVisObject().asText(TABLE_IMAGES, FIELD_I_NAVIGATION_PERSPECTIVE, dataImage.getAttributeForVisObject(FIELD_I_NAVIGATION_PERSPECTIVE), getProject().getDBLanguage(), true);
//                dataPicOrder.setFieldValue(FIELD_DA_PO_PROPOSED_NAME, navPerspective, DBActionOrigin.FROM_EDIT);
//            }
//        }
        if (withDialog) {
            iPartsDataPicOrder newDataPicOrder = PicOrderMainForm.createPictureOrderDialog(getConnector(),
                                                                                           this, dataPicOrder,
                                                                                           productType.get(), isCurrentAssemblyCarPerspective);
            refreshAfterPicOrderCreation(newDataPicOrder);
            return newDataPicOrder;
        }
        return dataPicOrder;
    }

    /**
     * Fügt dem übergebenen Bildauftrag seine Verwendung hinzu (KGTU/EinPAS/freie SA)
     *
     * @param dataPicOrder
     * @return
     */
    private Optional<PRODUCT_STRUCTURING_TYPE> addUsage(iPartsDataPicOrder dataPicOrder) {
        if (iPartsPicOrderEditHelper.isRetailSa(getConnector())) {
            // Wenn keine Verortung existiert, dann kann der Bildauftrag auch nicht erzeugt werden
            if (!addRetailSaUsage(dataPicOrder)) {
                return Optional.empty();
            }
            return Optional.of(PRODUCT_STRUCTURING_TYPE.KG_TU);
        } else {
            return Optional.of(addNonSaRetailUsage(dataPicOrder));
        }
    }

    private PRODUCT_STRUCTURING_TYPE addNonSaRetailUsage(iPartsDataPicOrder dataPicOrder) {
        iPartsConst.PRODUCT_STRUCTURING_TYPE productType = iPartsConst.PRODUCT_STRUCTURING_TYPE.KG_TU;
        // Produkt-ID und EinPASId/KgTuId eintragen, falls möglich
        iPartsDataModuleEinPASList moduleEinPASList = iPartsDataModuleEinPASList.loadForModule(getProject(), getConnector().getCurrentAssembly().getAsId());
        iPartsDataModuleEinPAS moduleEinPAS = null;
        if (moduleEinPASList.size() == 1) {
            moduleEinPAS = moduleEinPASList.get(0);
        } else {
            boolean useDummyEntry = false;
            if (moduleEinPASList.isEmpty()) {
                // Keine EinPAS- oder KG/TU-Verortung vorhanden -> erstes passendes Produkt für das Modul verwenden
                useDummyEntry = true;
            } else {
                // Bei mehr als einem Eintrag prüfen, ob alle Einträge denselben Typ und/oder ID haben (aufgrund von fehlerhaften
                // Daten in der DB mit doppelten Einträgen)
                IdWithType moduleAssignmentId = null;
                for (iPartsDataModuleEinPAS dataModuleEinPAS : moduleEinPASList) {
                    IdWithType currentModuleAssignmentId = getModuleAssignmentId(dataModuleEinPAS);
                    if ((moduleAssignmentId != null) && !moduleAssignmentId.equals(currentModuleAssignmentId)) {
                        // Die Einträge haben einen unterschiedlichen Typ und/oder ID -> erstes passendes Produkt für das
                        // Modul verwenden ohne konkrete Verortung
                        useDummyEntry = true;
                        break;
                    }
                    moduleAssignmentId = currentModuleAssignmentId;
                }
                if (!useDummyEntry) {
                    moduleEinPAS = moduleEinPASList.get(0); // Alle Einträge haben den gleichen Typ und ID -> ersten nehmen
                }
            }

            if (useDummyEntry) { // Dummy-Eintrag erzeugen ohne konkrete Verortung
                iPartsDataProductModulesList productModulesList = iPartsDataProductModulesList.loadDataProductModulesList(getProject(),
                                                                                                                          getConnector().getCurrentAssembly().getAsId());
                if (!productModulesList.isEmpty()) {
                    iPartsDataProductModules dataProductModules = productModulesList.get(0);

                    // Dummy-Eintrag für moduleEinPAS im Speicher erzeugen
                    moduleEinPAS = new iPartsDataModuleEinPAS(getProject(), new iPartsModuleEinPASId(dataProductModules.getAsId().getProductNumber(),
                                                                                                     dataProductModules.getAsId().getModuleNumber(),
                                                                                                     ""));
                }
            }
        }

        if (moduleEinPAS != null) {
            iPartsProductId productId = new iPartsProductId(moduleEinPAS.getAsId().getProductNumber());
            IdWithType moduleAssignmentId = getModuleAssignmentId(moduleEinPAS);
            if (moduleAssignmentId instanceof EinPasId) {
                productType = PRODUCT_STRUCTURING_TYPE.EINPAS;
                dataPicOrder.addUsage(productId, (EinPasId)moduleAssignmentId, DBActionOrigin.FROM_EDIT);
            } else if (moduleAssignmentId instanceof KgTuId) {
                productType = PRODUCT_STRUCTURING_TYPE.KG_TU;
                dataPicOrder.addUsage(productId, (KgTuId)moduleAssignmentId, DBActionOrigin.FROM_EDIT);
            }
        }
        return productType;
    }

    private IdWithType getModuleAssignmentId(iPartsDataModuleEinPAS moduleEinPAS) {
        iPartsProductId productId = new iPartsProductId(moduleEinPAS.getAsId().getProductNumber());
        // DAIMLER-490
        iPartsProduct product = iPartsProduct.getInstance(getProject(), productId);
        iPartsConst.PRODUCT_STRUCTURING_TYPE productType = product.getProductStructuringType();
        if (productType == null) {
            // Produkt nicht gefunden => KG/TU
            productType = PRODUCT_STRUCTURING_TYPE.KG_TU;
        }
        switch (productType) {
            case EINPAS:
                return new EinPasId(moduleEinPAS.getFieldValue(iPartsConst.FIELD_DME_EINPAS_HG),
                                    moduleEinPAS.getFieldValue(iPartsConst.FIELD_DME_EINPAS_G),
                                    moduleEinPAS.getFieldValue(iPartsConst.FIELD_DME_EINPAS_TU));
            case KG_TU:
                KgTuId kgTuId = new KgTuId(moduleEinPAS.getFieldValue(iPartsConst.FIELD_DME_SOURCE_KG),
                                           moduleEinPAS.getFieldValue(iPartsConst.FIELD_DME_SOURCE_TU));
                if (isCurrentAssemblyCarPerspective && EditModuleHelper.isCarPerspectiveKgTuId(kgTuId)) {
                    kgTuId = new KgTuId("00", "000");
                }
                return kgTuId;
        }

        return null;
    }

    /**
     * Setzt die fest vorbelegte Verortung für freie SAs. Hierbei wird das erste Produkt samt KG verwendet, in dem die
     * freie SA vorkommt. Zusätzlich wird der künstliche technische Umfang "001" gesetzt.
     *
     * @param dataPicOrder
     * @return {@code true} falls es eine gültige Verortung für die freie SA gibt
     */
    private boolean addRetailSaUsage(iPartsDataPicOrder dataPicOrder) {
        EtkDataAssembly currentAssembly = getConnector().getCurrentAssembly();
        iPartsModuleId moduleId = new iPartsModuleId(currentAssembly.getAsId().getKVari());
        EtkProject project = getProject();
        iPartsDataSAModulesList saModules = iPartsDataSAModulesList.loadDataForModule(project, moduleId);
        // Aktueller Bearbeitungsauftrag
        iPartsDataWorkOrder currentWorkOrder = iPartsWorkOrderCache.getWorkOrderForAuthorOrder(getProject());
        if (currentWorkOrder != null) {
            for (iPartsDataSAModules saModule : saModules) {
                iPartsSAId saId = new iPartsSAId(saModule.getAsId().getSaNumber());
                Map<iPartsProductId, Set<String>> productIdsToKGsMap = iPartsSA.getInstance(project, saId).getProductIdsToKGsMap(project);
                for (Map.Entry<iPartsProductId, Set<String>> productIdToKGsEntry : productIdsToKGsMap.entrySet()) {
                    iPartsProductId productId = productIdToKGsEntry.getKey();
                    iPartsProduct product = iPartsProduct.getInstance(getProject(), productId);
                    // Das Produkt nur verwenden, wenn es zur Beauftragung passt (LKW xor PKW)
                    if (currentWorkOrder.isVisibleForUserProperties(product.isCarAndVanProduct(), product.isTruckAndBusProduct())) {
                        String kg = productIdToKGsEntry.getValue().iterator().next();
                        if (StrUtils.isValid(productId.getProductNumber(), kg)) {
                            KgTuId kgTuId = new KgTuId(kg, "001");
                            dataPicOrder.addUsage(productId, kgTuId, DBActionOrigin.FROM_EDIT);
                            return true;
                        }
                    }
                }
            }
        }
        MessageDialog.showWarning(TranslationHandler.translate("!!Der Bildauftrag kann nicht erzeugt werden, weil der SA-TU \"%1\" in keinem Produkt mit passender Unternehmenszugehörigkeit verortet ist!",
                                                               moduleId.getModuleNumber()), "!!Bildauftrag erstellen");
        return false;
    }

    /**
     * Aktualisiert nach dem Erzeugen eines Bildauftrags die Grids
     *
     * @param picOrder
     */
    private void refreshAfterPicOrderCreation(iPartsDataPicOrder picOrder) {
        if (picOrder != null) {
            getConnector().updatePictureOrderList();
            showPictureOrderForm.doRefreshPicOrder(null);
            showPictureOrderForm.setSelectedEntryById(picOrder.getAsId());
            getConnector().updateAllViews(null, false);
            enableButtons();
        }
    }

    /**
     * Aktualisiert das Grid mit den Bildaufträgen
     *
     * @param event
     */
    public void doRefreshPicOrder(Event event, boolean fireEvent) {
        if ((event != null) && event.hasParameter(RELOAD_PICTURES)) {
            if (event.getBooleanParameter(RELOAD_PICTURES)) {
                fireDataChangedEventAndUpdateThumbnails(fireEvent);
                updateTableImages(-1);

                // DividerPosition setzen falls die Bildaufträge sichtbar werden
                if (mainWindow.dockingpanel_PicOrders.isShowing()) {
                    mainWindow.splitPanePicOrders.setDividerPosition(getSplitPanePicOrdersDivierAutoPosition());
                }
            }
        }
        showPictureOrderForm.refreshTableView();
        updatePictureOrders();
        getConnector().updateAllViews(null, false);
    }

    /**
     * Zeigt den übergebenen Bildauftrag im dazugehörigen Dialog an.
     *
     * @param picOrder
     */
    public boolean showPictureOrder(iPartsDataPicOrder picOrder) {
        return showPictureOrderForm.openPictureOrderForm(picOrder);
    }

    public void updatePictureOrder(iPartsDataPicOrder picOrder) {
        PicOrderMainForm.savePicOrder(getProject(), picOrder);
        getConnector().updatePictureOrderList();
        showPictureOrderForm.doRefreshPicOrder(null);
        getConnector().updateAllViews(null, false);
    }

    private class iPartsEditAssemblyImageForm extends iPartsAssemblyImageForm {

        public iPartsEditAssemblyImageForm() {
            super(EditAssemblyImageForm.this.getConnector(), EditAssemblyImageForm.this);
            setForceThumbnailsWithViewerFunctionality(true);
        }

        @Override
        public EditModuleFormIConnector getConnector() {
            return (EditModuleFormIConnector)super.getConnector();
        }

        @Override
        public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
            super.updateData(sender, forceUpdateAll);
            enableButtons();
        }

        protected GuiViewerLink createLinkWithColorsAndStyle(boolean isAssembly, String key, String keyVer, String text,
                                                             String linkType, String extInfo, Rectangle linkRect, EtkEbenenDaten partListType) {
            GuiViewerLink link = super.createLinkWithColorsAndStyle(isAssembly, key, keyVer, text, linkType, extInfo, linkRect, partListType);
            //hier sind Modifikationen an Link möglich
            if (linkIsNotInPartlist) {
                GuiViewerLink.LinkStyle linkStyle = link.getLinkStyle();
                Color color = iPartsEditPlugin.clPlugin_iPartsEdit_HotspotNotLinkedBackgroundColor.getColor();
                String newText = TranslationHandler.translate("!!'%1' Nicht in Stückliste vorhanden", key);
                newText = getProject().getVisObject().asHtml("", "", newText, getProject().getDBLanguage()).getStringResult();

                if ((linkStyle == GuiViewerLink.LinkStyle.Ellipse) || (linkStyle == GuiViewerLink.LinkStyle.EllipseFull)) {
                    linkStyle = GuiViewerLink.LinkStyle.Full;
                } else {
                    linkStyle = GuiViewerLink.LinkStyle.EllipseFull;
                }

                link = new GuiViewerLink(link.getColorHint(), link.getColorHintText(), color, link.getColorsLinkColorsMarked(),
                                         linkStyle, link.getLinkStyleMarked(), link.getDisplayRect(), link.getKey(), link.getKeyVer(),
                                         newText, link.getType(), link.getExtInfo());
            }
            return link;
        }

        @Override
        protected void imageIndexChanged(Event event) {
            super.imageIndexChanged(event);
            Integer newIndex = (Integer)event.getParameter(Event.EVENT_PARAMETER_IMAGE_INDEX);
            getTable().setSelectedRow(newIndex, true, false);
            enableButtons();
        }

        @Override
        public void updateDefaultImageViewerButtons() {
            updateViewThumbnailsButton();
        }

        protected void markThumbnailImages(List<Integer> imageIndexList) {
            ImageViewerItemMarkedThumbnails thumbnailMarker = new ImageViewerItemMarkedThumbnails();
            thumbnailMarker.setClickedThumbnails(new ArrayList<>(imageIndexList));
            markThumbnails(thumbnailMarker);
        }

        protected void markThumbnailImage(int imageIndex) {
            List<Integer> imageIndexList = new DwList<>(1);
            imageIndexList.add(imageIndex);
            markThumbnailImages(imageIndexList);
        }

        @Override
        protected List<EtkDataPartListEntry> getPartsListEntriesForHotspots(EtkDataAssembly assembly) {
            // Im Edit ist immer die ungefilterte Stückliste relevant
            return assembly.getPartListUnfiltered(assembly.getEbene()).getAsList();
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    private de.docware.framework.modules.gui.controls.menu.GuiContextMenu contextmenuTableImages;

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitPane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitPane_firstChild;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitPanePicOrders;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelImages;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel toolbarHolderPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.toolbar.GuiToolbar toolbarImageEdit;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelGrid;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane scrollPaneGrid;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiDockingPanel dockingpanel_PicOrders;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelPicOrderTable;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitPane_secondChild;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            contextmenuTableImages = new de.docware.framework.modules.gui.controls.menu.GuiContextMenu();
            contextmenuTableImages.setName("contextmenuTableImages");
            contextmenuTableImages.__internal_setGenerationDpi(96);
            contextmenuTableImages.registerTranslationHandler(translationHandler);
            contextmenuTableImages.setScaleForResolution(true);
            contextmenuTableImages.setMinimumWidth(10);
            contextmenuTableImages.setMinimumHeight(10);
            contextmenuTableImages.setMenuName("contextmenuTableImages");
            contextmenuTableImages.setParentControl(this);
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
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMain.setLayout(panelMainLayout);
            splitPane = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitPane.setName("splitPane");
            splitPane.__internal_setGenerationDpi(96);
            splitPane.registerTranslationHandler(translationHandler);
            splitPane.setScaleForResolution(true);
            splitPane.setMinimumWidth(10);
            splitPane.setMinimumHeight(10);
            splitPane.setDividerPosition(350);
            splitPane_firstChild = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitPane_firstChild.setName("splitPane_firstChild");
            splitPane_firstChild.__internal_setGenerationDpi(96);
            splitPane_firstChild.registerTranslationHandler(translationHandler);
            splitPane_firstChild.setScaleForResolution(true);
            splitPane_firstChild.setMinimumWidth(0);
            de.docware.framework.modules.gui.layout.LayoutBorder splitPane_firstChildLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitPane_firstChild.setLayout(splitPane_firstChildLayout);
            splitPanePicOrders = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitPanePicOrders.setName("splitPanePicOrders");
            splitPanePicOrders.__internal_setGenerationDpi(96);
            splitPanePicOrders.registerTranslationHandler(translationHandler);
            splitPanePicOrders.setScaleForResolution(true);
            splitPanePicOrders.setMinimumWidth(10);
            splitPanePicOrders.setMinimumHeight(10);
            splitPanePicOrders.setHorizontal(false);
            splitPanePicOrders.setDividerPosition(393);
            panelImages = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelImages.setName("panelImages");
            panelImages.__internal_setGenerationDpi(96);
            panelImages.registerTranslationHandler(translationHandler);
            panelImages.setScaleForResolution(true);
            panelImages.setMinimumWidth(0);
            panelImages.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.LayoutBorder panelImagesLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelImages.setLayout(panelImagesLayout);
            toolbarHolderPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            toolbarHolderPanel.setName("toolbarHolderPanel");
            toolbarHolderPanel.__internal_setGenerationDpi(96);
            toolbarHolderPanel.registerTranslationHandler(translationHandler);
            toolbarHolderPanel.setScaleForResolution(true);
            toolbarHolderPanel.setMinimumWidth(10);
            toolbarHolderPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder toolbarHolderPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            toolbarHolderPanel.setLayout(toolbarHolderPanelLayout);
            toolbarImageEdit = new de.docware.framework.modules.gui.controls.toolbar.GuiToolbar();
            toolbarImageEdit.setName("toolbarImageEdit");
            toolbarImageEdit.__internal_setGenerationDpi(96);
            toolbarImageEdit.registerTranslationHandler(translationHandler);
            toolbarImageEdit.setScaleForResolution(true);
            toolbarImageEdit.setMinimumWidth(10);
            toolbarImageEdit.setMinimumHeight(10);
            toolbarImageEdit.setBackgroundImage(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignToolbarBigBackground"));
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder toolbarImageEditConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            toolbarImageEdit.setConstraints(toolbarImageEditConstraints);
            toolbarHolderPanel.addChild(toolbarImageEdit);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder toolbarHolderPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            toolbarHolderPanelConstraints.setPosition("north");
            toolbarHolderPanel.setConstraints(toolbarHolderPanelConstraints);
            panelImages.addChild(toolbarHolderPanel);
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
            scrollPaneGrid = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            scrollPaneGrid.setName("scrollPaneGrid");
            scrollPaneGrid.__internal_setGenerationDpi(96);
            scrollPaneGrid.registerTranslationHandler(translationHandler);
            scrollPaneGrid.setScaleForResolution(true);
            scrollPaneGrid.setMinimumWidth(10);
            scrollPaneGrid.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder scrollPaneGridConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            scrollPaneGrid.setConstraints(scrollPaneGridConstraints);
            panelGrid.addChild(scrollPaneGrid);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelGridConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelGrid.setConstraints(panelGridConstraints);
            panelImages.addChild(panelGrid);
            splitPanePicOrders.addChild(panelImages);
            dockingpanel_PicOrders = new de.docware.framework.modules.gui.controls.GuiDockingPanel();
            dockingpanel_PicOrders.setName("dockingpanel_PicOrders");
            dockingpanel_PicOrders.__internal_setGenerationDpi(96);
            dockingpanel_PicOrders.registerTranslationHandler(translationHandler);
            dockingpanel_PicOrders.setScaleForResolution(true);
            dockingpanel_PicOrders.setMinimumWidth(10);
            dockingpanel_PicOrders.setMinimumHeight(18);
            dockingpanel_PicOrders.setTextHide("!!Bildaufträge");
            dockingpanel_PicOrders.setTextShow("!!Bildaufträge anzeigen");
            dockingpanel_PicOrders.setImageHide(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelNorth"));
            dockingpanel_PicOrders.setImageShow(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelSouth"));
            dockingpanel_PicOrders.setDockPosition(de.docware.framework.modules.gui.controls.misc.DWBorderPosition.NORTH);
            dockingpanel_PicOrders.setButtonBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignButtonDisabledBackgroundGradient1"));
            dockingpanel_PicOrders.setButtonForegroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignButtonBorderSelected"));
            dockingpanel_PicOrders.setButtonFill(true);
            panelPicOrderTable = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelPicOrderTable.setName("panelPicOrderTable");
            panelPicOrderTable.__internal_setGenerationDpi(96);
            panelPicOrderTable.registerTranslationHandler(translationHandler);
            panelPicOrderTable.setScaleForResolution(true);
            panelPicOrderTable.setMinimumWidth(10);
            panelPicOrderTable.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.LayoutBorder panelPicOrderTableLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelPicOrderTable.setLayout(panelPicOrderTableLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelPicOrderTableConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelPicOrderTable.setConstraints(panelPicOrderTableConstraints);
            dockingpanel_PicOrders.addChild(panelPicOrderTable);
            splitPanePicOrders.addChild(dockingpanel_PicOrders);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder splitPanePicOrdersConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            splitPanePicOrders.setConstraints(splitPanePicOrdersConstraints);
            splitPane_firstChild.addChild(splitPanePicOrders);
            splitPane.addChild(splitPane_firstChild);
            splitPane_secondChild = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitPane_secondChild.setName("splitPane_secondChild");
            splitPane_secondChild.__internal_setGenerationDpi(96);
            splitPane_secondChild.registerTranslationHandler(translationHandler);
            splitPane_secondChild.setScaleForResolution(true);
            splitPane_secondChild.setMinimumWidth(0);
            de.docware.framework.modules.gui.layout.LayoutBorder splitPane_secondChildLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitPane_secondChild.setLayout(splitPane_secondChildLayout);
            splitPane.addChild(splitPane_secondChild);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder splitPaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            splitPane.setConstraints(splitPaneConstraints);
            panelMain.addChild(splitPane);
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