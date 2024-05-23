/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.picorder;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumEntry;
import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumValue;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsPicOrderPartSourceType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrderPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsPicOrderPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditDataObjectFilterGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditDataObjectGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiSeparator;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.StrUtils;
import de.docware.util.sql.TableAndFieldName;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * Dialog zum Hinzufügen von Stücklisteneinträgen zu einem Bildauftrag
 */
public class PicOrderPartlistEntriesForm extends AbstractJavaViewerForm {

    public static final String CONFIG_KEY_SELECTED_ENTRIES = "Plugin/iPartsEdit/PictureOrderPositions";
    public static final String ID_DELIMITER = "||";

    private EditDataObjectGrid selectedDataGrid;
    private EditDataObjectFilterGrid actualDataGrid;
    private EditToolbarButtonMenuHelper toolbarHelper;
    private Map<String, PartListEntryExtra> completeEntriesMap;
    private final Set<String> startPartEntrySet;
    private boolean forceSelectionHasChanged;
    private final iPartsDataPicOrder dataPicOrder;
    private boolean isEditAllowed = true;
    private GuiMenuItem picPosMarkerMenu;
    private GuiSeparator picPosMarkerSeparator;
    private Map<String, iPartsDataPicOrderPart> createdPicOrderParts;
    private final Map<String, String> positionMarkersBeforeEdit; // Map für alle Positionsmarker, die beim Öffnen des Dialogs gesetzt waren
    private final Map<String, String> positionMarkerInDB; // Map für alle Positionsmarker, die für die selektieren Einträge in der DB existieren

    /**
     * Erzeugt eine Instanz von EditPicOrderToPartlistEntryForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public PicOrderPartlistEntriesForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                       iPartsDataPicOrder dataPicOrder) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.dataPicOrder = dataPicOrder;
        postCreateGui();
        startPartEntrySet = new HashSet<>();
        createdPicOrderParts = new HashMap<>();
        positionMarkersBeforeEdit = new HashMap<>();
        positionMarkerInDB = new HashMap<>();
        initPartListEntries();
        fillGrids();

        Dimension screenSize = FrameworkUtils.getScreenSize();
        mainWindow.setSize(screenSize.width - 20, screenSize.height - 20);
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird, wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        toolbarHelper = new EditToolbarButtonMenuHelper(getConnector(), mainWindow.toolbar);
        // Grid für ausgewählte Einträge (oberes Grid)
        selectedDataGrid = createGridForSelectedPicturePositions();
        ConstraintsBorder constraints = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
        selectedDataGrid.getGui().setConstraints(constraints);
        mainWindow.panelSelectedEntries.addChild(selectedDataGrid.getGui());

        // Grid für auswählbare Einträge (unteres Grid)
        actualDataGrid = createGridForChoosableEntries();
        constraints = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
        actualDataGrid.getGui().setConstraints(constraints);
        mainWindow.panelActualEntries.addChild(actualDataGrid.getGui());

        createToolbarButtons();
        mainWindow.buttonpanel.setButtonModalResult(GuiButtonOnPanel.ButtonType.OK, ModalResult.NONE);
    }

    /**
     * Erzeugt das Grid für die auswählbaren Stücklistenpositionen (unteres Grid)
     *
     * @return
     */
    private EditDataObjectFilterGrid createGridForChoosableEntries() {
        EditDataObjectFilterGrid result = new EditDataObjectFilterGrid(getConnector(), this) {
            @Override
            protected void onTableSelectionChanged(Event event) {
                enableToolbar(this);
            }

            @Override
            protected void onCellDblClicked(int row, int col, Event event) {
                doAdd(event);
            }

            @Override
            protected void createContextMenuItems(GuiContextMenu contextMenu) {
                GuiMenuItem menuItem
                        = getToolbarHelper().createContextMenuEntry(EditToolbarButtonAlias.IMG_UP,
                                                                    "!!Übernehmen", getUITranslationHandler(),
                                                                    new EventListener<>(Event.MENU_ITEM_EVENT) {
                                                                        @Override
                                                                        public void fire(Event event) {
                                                                            doAdd(event);
                                                                        }
                                                                    });
                contextMenu.addChild(menuItem);
            }
        };
        result.showToolbar(false);
        result.setDisplayFields(getConnector().getAssemblyListDisplayFields());
        return result;
    }

    /**
     * Erzeugt das Grid für die ausgewählten Bildpositionen
     *
     * @return
     */
    private EditDataObjectGrid createGridForSelectedPicturePositions() {
        PicturePositionGrid result = new PicturePositionGrid(getConnector(), this);
        result.setPageSplitNumberOfEntriesPerPage(iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE);
        result.showToolbar(false);
        result.setDisplayFields(createDisplayFieldsForSelectedGrid());
        return result;
    }

    /**
     * Erzeugt die {@link EtkDisplayFields} für das Grid mit den ausgewählten Bildpositionen
     *
     * @return
     */
    private EtkDisplayFields createDisplayFieldsForSelectedGrid() {
        EtkDisplayFields displayFields = new EtkDisplayFields();
        displayFields.load(getConfig(), CONFIG_KEY_SELECTED_ENTRIES + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS);
        if (displayFields.size() == 0) {
            displayFields.addFelder(getConnector().getAssemblyListDisplayFields());
            EtkDisplayField displayField = new EtkDisplayField(iPartsConst.TABLE_DA_PICORDER_PARTS, iPartsConst.FIELD_DA_PPA_ZGS, false, false);
            displayFields.addFeld(displayField);
            displayField = new EtkDisplayField(iPartsConst.TABLE_DA_PICORDER_PARTS, iPartsConst.FIELD_DA_PPA_PIC_POSITION_MARKER, false, false);
            displayFields.addFeld(displayField);
            displayField = new EtkDisplayField(iPartsConst.TABLE_DA_PICORDER_PARTS, iPartsConst.FIELD_DA_PPA_SENT, false, false);
            displayFields.addFeld(displayField);
            displayField = new EtkDisplayField(iPartsConst.TABLE_DA_PICORDER_PARTS, iPartsConst.FIELD_DA_PPA_SEQ_NO, false, false);
            displayFields.addFeld(displayField);
            displayFields.loadStandards(getConfig());
        }
        return displayFields;
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    @Override
    public EditModuleFormIConnector getConnector() {
        return (EditModuleFormIConnector)super.getConnector();
    }

    public ModalResult showModal() {
        return mainWindow.showModal();
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public boolean isEditAllowed() {
        return isEditAllowed;
    }

    public void setEditAllowed(boolean editAllowed) {
        this.isEditAllowed = editAllowed;
        mainWindow.buttonpanel.setButtonVisible(GuiButtonOnPanel.ButtonType.CANCEL, isEditAllowed);
        selectedDataGrid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_DOWN, isEditAllowed);
        actualDataGrid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_UP, isEditAllowed);
        enableButtons();
    }

    private String createPartlistEntryIdWithHotspotAndPartNumber(iPartsDataPicOrderPart picOrderPart) {
        return createPartlistEntryIdWithHotspotAndPartNumber(picOrderPart.getAsId().getPartListEntryId(), picOrderPart.getHotSpot(), picOrderPart.getPartNumber());
    }

    private String createPartlistEntryIdWithHotspotAndPartNumber(EtkDataPartListEntry partListEntry) {
        return createPartlistEntryIdWithHotspotAndPartNumber(partListEntry.getAsId(), partListEntry.getFieldValue(iPartsConst.FIELD_K_POS), partListEntry.getFieldValue(iPartsConst.FIELD_K_MATNR));
    }

    /**
     * Erzeugt eine String ID auf Basis der {@link PartListEntryId}, dem Hotspot und der Teilenummer
     *
     * @param partListEntryId
     * @param hotSpot
     * @param partNumber
     * @return
     */
    private String createPartlistEntryIdWithHotspotAndPartNumber(PartListEntryId partListEntryId, String hotSpot, String partNumber) {
        return partListEntryId.toString(ID_DELIMITER) + ID_DELIMITER + hotSpot + ID_DELIMITER + partNumber;
    }

    private void initPartListEntries() {
        List<EtkDataPartListEntry> list = getConnector().getCurrentPartListEntries();
        completeEntriesMap = new LinkedHashMap<>();

        Map<String, iPartsDataPicOrderPart> storedAndSentEntries = new LinkedHashMap<>();
        for (iPartsDataPicOrderPart picOrderPart : dataPicOrder.getParts()) {
            // Klone erzeugen, weil Werte verändert werden können. Bei "Abbruch" dürfen die neu gesetzten Werte ja nicht
            // übernommen werden
            iPartsDataPicOrderPart clonedPicOrderPart = picOrderPart.cloneMe(getProject());

            // Ist es ein Kopierauftrag, muss bei allen Bildpositionen des Originalauftrags der Sequenzzähler 1 zu
            // sehen sein. Nur beim Anlegen eines Auftrags und nicht bei der Ansicht eines fertigen Kopierauftrags. Dort
            // sind die richtigen Nummern schon in der Datenbank
            if (dataPicOrder.isCopy() && ((PicOrderMainForm)parentForm).isEditAllowed()) {
                clonedPicOrderPart.setFieldValueAsInteger(iPartsConst.FIELD_DA_PPA_SEQ_NO, 1, DBActionOrigin.FROM_EDIT);
            }

            // Die initialen Positionsmarker setzen
            fillInitialMarkers(picOrderPart);

            String partListEntryIdWithHotSpotAndPartNumber = createPartlistEntryIdWithHotspotAndPartNumber(picOrderPart);
            if (picOrderPart.isSent()) {
                storedAndSentEntries.put(partListEntryIdWithHotSpotAndPartNumber, clonedPicOrderPart);
            } else {
                // Wurde die Position noch nicht abgeschickt, dann wurde sie von außen an den Bildauftrag gehängt, z.B.
                // via Menüpunkt an der Stückliste
                createdPicOrderParts.put(partListEntryIdWithHotSpotAndPartNumber, clonedPicOrderPart);
            }
        }

        for (EtkDataPartListEntry partListEntry : list) {
            // Check, ob die Stücklistenposition schon selektiert war
            String partListEntryIdWithhotSpotAndPartNumber = createPartlistEntryIdWithHotspotAndPartNumber(partListEntry);
            iPartsDataPicOrderPart storedAndSentPicOrderPart = storedAndSentEntries.remove(partListEntryIdWithhotSpotAndPartNumber);

            if (storedAndSentPicOrderPart != null) {
                // Stücklistenposition wurde schon selektiert (steht im oberen Grid)
                // → Check, ob es eine gespeicherte Stücklistenposition gibt
                EtkDataPartListEntry storedEntry = storedAndSentPicOrderPart.getStoredRelatedPartListEntry();
                if (storedEntry != null) {
                    // Gespeicherte Stücklistenposition existiert → Daten werden aus der gespeicherten Stücklistenposition geholt
                    createEntryExtraObject(partListEntry, storedEntry, storedAndSentPicOrderPart, true, false);
                } else {
                    // Gespeicherte Stücklistenposition existiert nicht → Daten werden aus dem iPartsDataPicOrderPart Objekt geholt
                    createEntryExtraObject(partListEntry, null, storedAndSentPicOrderPart, true, false);
                }
            } else if (createdPicOrderParts.containsKey(partListEntryIdWithhotSpotAndPartNumber)) {
                // Zur Bildposition existiert ein temporäres iPartsDataPicOrderPart Objekt, z.B. via Menüpunkt an der
                // Stückliste oder durch Auswählen aus dem unteren Grid
                createEntryExtraObject(partListEntry, null, null, true, false);
            } else {
                // Die Stücklistenposition wurde nicht selektiert → Hilfsobjekt für das untere Grid erzeugen
                createEntryExtraObject(partListEntry, null, null, false, false);
            }
        }

        // Check, ob es noch Einträge am Bildauftrag gibt, die in der aktuellen Stückliste nicht auftauchen. Falls ja,
        // handelt es sich um entfernte Stücklistenpositionen, die in einem vorherigen Bildauftrag verschickt wurden.
        if (!storedAndSentEntries.isEmpty()) {
            for (iPartsDataPicOrderPart removedPicOrderPart : storedAndSentEntries.values()) {
                EtkDataPartListEntry removedEntry = removedPicOrderPart.getStoredRelatedPartListEntry();
                if (removedEntry != null) {
                    createEntryExtraObject(null, removedEntry, removedPicOrderPart, true, true);
                } else {
                    createEntryExtraObject(null, null, removedPicOrderPart, true, true);
                }
            }
        }
    }

    /**
     * Setzt die initialen Positionsmarker für alle selektieren Stücklistenpositionen (oberes Grid). Hierbei werden die
     * aktuellen Marker am PicOrderPart Objekt und die aktuellen Marker aus der DB gesetzt.
     *
     * @param picOrderPart
     */
    private void fillInitialMarkers(iPartsDataPicOrderPart picOrderPart) {
        String currentPositionMarker = picOrderPart.getPicturePositionMarker();
        String dbPositionMarker = null;
        // Wenn das PicOrderPart neu ist, dann wurde die Position neu hinzugefügt → Es gibt keinen Original-Wert in der DB
        if (!picOrderPart.isNew()) {
            // Wenn das Attribut für den Positionskenner verändert ist, dann wurde vom Benutzer ein neuer Wert gesetzt
            // → Bestimme den Wert, der in der DB steht (Original-Wert seit der letzten Versorgung)
            if (isPositionMarkerModified(picOrderPart)) {
                iPartsDataPicOrderPart picOrderPartFromDB = new iPartsDataPicOrderPart(picOrderPart.getEtkProject(), (iPartsPicOrderPartId)picOrderPart.getOldId());
                if (picOrderPartFromDB.existsInDB()) {
                    dbPositionMarker = picOrderPartFromDB.getPicturePositionMarker();
                }
            } else {
                dbPositionMarker = currentPositionMarker;
            }
        }

        String partListEntryIdWithHotSpotAndPartNumber = createPartlistEntryIdWithHotspotAndPartNumber(picOrderPart);
        positionMarkersBeforeEdit.put(partListEntryIdWithHotSpotAndPartNumber, currentPositionMarker);
        if (StrUtils.isValid(dbPositionMarker)) {
            positionMarkerInDB.put(partListEntryIdWithHotSpotAndPartNumber, dbPositionMarker);
        }
    }


    /**
     * Erzeugt ein {@link PartListEntryExtra} Objekt mit dem übergebenen Parameter
     *
     * @param partListEntry der eigentliche Stücklisteneintrag
     * @param storedEntry   ein evtl. bereits in der DB mit dem Bildauftrag gespeicherter Stücklisteneintrag
     * @param picOrderPart  das verknüpfte iPartsPicOrderPart (Falls der Stücklisteneintrag schon am Bildauftrag hängt)
     * @param isSelected    Flag, ob die Stücklistenposition im oberen Grid steht (also selektiert wurde)
     * @param isDeleted     Flag, ob die Stücklistenposition selektiert und mittlerweile vom Autor gelöscht wurde
     */
    private void createEntryExtraObject(EtkDataPartListEntry partListEntry, EtkDataPartListEntry storedEntry,
                                        iPartsDataPicOrderPart picOrderPart, boolean isSelected, boolean isDeleted) {
        PartListEntryExtra partListEntryExtra = new PartListEntryExtra();
        partListEntryExtra.partListEntry = partListEntry;
        partListEntryExtra.storedEntry = storedEntry;
        partListEntryExtra.isSelected = isSelected;
        partListEntryExtra.isDeleted = isDeleted;
        partListEntryExtra.picOrderPart = picOrderPart;

        String partListEntryIdWithHotSpotAndPartNumber;
        if (partListEntry != null) {
            partListEntryIdWithHotSpotAndPartNumber = createPartlistEntryIdWithHotspotAndPartNumber(partListEntry);
        } else if (picOrderPart != null) {
            partListEntryIdWithHotSpotAndPartNumber = createPartlistEntryIdWithHotspotAndPartNumber(picOrderPart);
        } else if (storedEntry != null) {
            partListEntryIdWithHotSpotAndPartNumber = createPartlistEntryIdWithHotspotAndPartNumber(storedEntry);
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, "Can not create picture position without partListEntry reference");
            return;
        }
        if (isSelected) {
            startPartEntrySet.add(partListEntryIdWithHotSpotAndPartNumber);
        }
        partListEntryExtra.partListEntryIdWithHotSpotAndPartNumber = partListEntryIdWithHotSpotAndPartNumber;
        completeEntriesMap.put(partListEntryIdWithHotSpotAndPartNumber, partListEntryExtra);
    }

    private Map<String, PartListEntryExtra> getSelectedPartListEntriesAsMap() {
        Map<String, PartListEntryExtra> result = new LinkedHashMap<>();
        for (PartListEntryExtra partListEntryExtra : completeEntriesMap.values()) {
            if (partListEntryExtra.isSelected) {
                result.put(partListEntryExtra.partListEntryIdWithHotSpotAndPartNumber, partListEntryExtra);
            }
        }
        return result;
    }

    /**
     * Befüllt beide Grids
     */
    private void fillGrids() {
        boolean selectedIsEmpty = true;
        boolean actualIsEmpty = true;
        // Filterung/Sortierung merken
        Object storageSelected = selectedDataGrid.getFilterAndSortSettings();
        Object storageActual = actualDataGrid.getFilterAndSortSettings();
        selectedDataGrid.clearGrid();
        actualDataGrid.clearGrid();

        // Durchlaufe alle Stücklistenpositionen
        for (PartListEntryExtra partListEntryExtra : completeEntriesMap.values()) {
            boolean oldLogLoadFieldIfNeeded = disableLogLoadFieldIfNeeded(partListEntryExtra);
            try {
                // Check, ob es eine selektierte Bildposition ist
                if (partListEntryExtra.isSelected) {
                    Color color = null;
                    boolean hasPicOrderPart = partListEntryExtra.picOrderPart != null;
                    List<EtkDataObject> dataObjects = new ArrayList<>();

                    // Wenn eine gespeicherte Bildposition existiert, dann muss die immer zuerst verwendet werden
                    if (partListEntryExtra.storedEntry != null) {
                        dataObjects.add(partListEntryExtra.storedEntry);
                    } else if ((partListEntryExtra.partListEntry != null) && !hasPicOrderPart) {
                        // Hier handelt es sich um eine Bildposition, die neu hinzugefügt wurde (via Menüpunkt oder unteres Grid)
                        dataObjects.add(partListEntryExtra.partListEntry);
                        color = iPartsPlugin.clPlugin_iParts_CodeMatrixBackground_positive.getColor();
                    }

                    // Hat eine Position einen Kenner ungleich dem DB Wert, dann hat sich seit der letzten Versorgung
                    // dieser Kenner geändert → grün anzeigen
                    // Wurde eine Position neu hinzugefügt oder verändert und dann gespeichert, steht die Sequenznummer
                    // auf "0". Ist das der Fall, muss die Position ebenfalls grün hervorgehoben werden.
                    if (((color == null) && !hasSamePositionMarkerAsInDB(partListEntryExtra.picOrderPart)) || !hasSequenceNumber(partListEntryExtra.picOrderPart)) {
                        color = iPartsPlugin.clPlugin_iParts_CodeMatrixBackground_positive.getColor();
                    }

                    // Falls ja, prüfen, ob ein iPartsPicOrderPart dazu existiert. Bei neu hinzugefügten existieren nur
                    // temporäre iPartsPicOrderPart Objekte
                    if (hasPicOrderPart) {
                        dataObjects.add(partListEntryExtra.picOrderPart);
                    } else if (createdPicOrderParts.containsKey(partListEntryExtra.partListEntryIdWithHotSpotAndPartNumber)) {
                        dataObjects.add(createdPicOrderParts.get(partListEntryExtra.partListEntryIdWithHotSpotAndPartNumber));
                    }
                    selectedDataGrid.addObjectToGrid(dataObjects);

                    // Gelöschte Positionen farblich unterlegen
                    if (partListEntryExtra.isDeleted) {
                        color = iPartsPlugin.clPlugin_iParts_CodeMatrixBackground_negative.getColor();
                    }
                    if (color != null) {
                        GuiTableRow tableRow = selectedDataGrid.getTable().getRow(selectedDataGrid.getTable().getRowCount() - 1);
                        tableRow.setBackgroundColor(color);
                    }
                    selectedIsEmpty = false;
                } else {
                    actualDataGrid.addObjectToGrid(partListEntryExtra.partListEntry);
                    actualIsEmpty = false;
                }
            } finally {
                enableLogLoadFieldIfNeeded(partListEntryExtra, oldLogLoadFieldIfNeeded);
            }
        }
        actualDataGrid.showNoResultsLabel(actualIsEmpty);
        selectedDataGrid.showNoResultsLabel(selectedIsEmpty);
        // Filterung/Sortierung wieder setzen
        actualDataGrid.restoreFilterAndSortSettings(storageActual);
        selectedDataGrid.restoreFilterAndSortSettings(storageSelected);

        enableToolbar(null);
        enableButtons();
    }

    /**
     * Liefert zurück, ob die Position eine gültige (größer 0) Sequenznummer hat.
     *
     * @param picOrderPart
     * @return
     */
    private boolean hasSequenceNumber(iPartsDataPicOrderPart picOrderPart) {
        if (picOrderPart != null) {
            return picOrderPart.getPicPosSeqNo() > 0;
        }
        return false;
    }

    /**
     * Check, ob das Positionskenner-Attribut vom DB Wert abweicht (modified).
     *
     * @param picOrderPart
     * @return
     */
    private boolean isPositionMarkerModified(iPartsDataPicOrderPart picOrderPart) {
        return (picOrderPart != null)
               && picOrderPart.attributeExists(iPartsConst.FIELD_DA_PPA_PIC_POSITION_MARKER)
               && picOrderPart.getAttribute(iPartsConst.FIELD_DA_PPA_PIC_POSITION_MARKER).isModified();
    }

    private void enableLogLoadFieldIfNeeded(PartListEntryExtra partListEntryExtra, boolean oldLogLoadFieldIfNeeded) {
        if (partListEntryExtra.isDeleted) {
            return;
        }
        partListEntryExtra.partListEntry.setLogLoadFieldIfNeeded(oldLogLoadFieldIfNeeded);
    }

    private boolean disableLogLoadFieldIfNeeded(PartListEntryExtra partListEntryExtra) {
        if (partListEntryExtra.isDeleted) {
            return false;
        }
        boolean oldLogLoadFieldIfNeeded = partListEntryExtra.partListEntry.isLogLoadFieldIfNeeded();
        partListEntryExtra.partListEntry.setLogLoadFieldIfNeeded(false);
        return oldLogLoadFieldIfNeeded;
    }

    private void createToolbarButtons() {
        toolbarHelper.addToolbarButton(EditToolbarButtonAlias.IMG_UP, "!!Übernehmen", new EventListener<>(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doAdd(event);
            }
        });
        toolbarHelper.addToolbarButton(EditToolbarButtonAlias.IMG_DOWN, "!!Entfernen", new EventListener<>(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doRemove(event);
            }
        });
        enableToolbar(null);
    }

    private void enableToolbar(DataObjectGrid sender) {
        boolean upEnabled = false;
        boolean downEnabled = false;
        if ((sender != null) && isEditAllowed) {
            List<EtkDataObject> list = actualDataGrid.getSelection();
            upEnabled = ((list != null) && !list.isEmpty());
            list = selectedDataGrid.getSelection();
            downEnabled = ((list != null) && !list.isEmpty());
        }
        toolbarHelper.enableToolbarButton(EditToolbarButtonAlias.IMG_UP, upEnabled);
        toolbarHelper.enableToolbarButton(EditToolbarButtonAlias.IMG_DOWN, downEnabled);
    }

    private void enableButtons() {
        boolean enabled = true;
        // Bei forceSelectionHasChanged wurde ein veränderter Stücklisteneintrag aus der Selektion entfernt
        // Liefert checkPositionMarkersChanged "true" zurück, dann wurde ein Bildpositionskenner geändert → aktive Änderung
        if (isEditAllowed && !forceSelectionHasChanged && !checkPositionMarkersChanged()) {
            Map<String, PartListEntryExtra> selectedMap = getSelectedPartListEntriesAsMap();
            if (selectedMap.size() == startPartEntrySet.size()) {
                if (selectedMap.isEmpty()) {
                    enabled = false;
                } else {
                    Set<String> selectedEntryIds = selectedMap.keySet();
                    enabled = !selectedEntryIds.containsAll(startPartEntrySet);
                }
            }
        }
        mainWindow.buttonpanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, enabled);
    }

    /**
     * Check, ob die Positionskenner der selektierten Positionen (oberes Grid) von ihren ursprünglichen Werten (beim
     * Öffnen des Dialogs) abweichen.
     *
     * @return
     */
    private boolean checkPositionMarkersChanged() {
        for (Map.Entry<String, PartListEntryExtra> selectedEntry : getSelectedPartListEntriesAsMap().entrySet()) {
            iPartsDataPicOrderPart picOrderPart = selectedEntry.getValue().picOrderPart;
            // Existiert kein iPartsDataPicOrderPart im oberen Grid, dann handelt es sich um eine neue Position
            // → Hole das iPartsDataPicOrderPart Objekt aus der Map der erzeugten Objekte
            if (picOrderPart == null) {
                picOrderPart = createdPicOrderParts.get(selectedEntry.getKey());
            }
            if (checkSinglePositionMarkerChanged(picOrderPart)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check, ob der übergebene Positionskenner von seinem ursprünglichen Wert (beim Öffnen des Dialogs) abweicht.
     *
     * @return
     */
    private boolean checkSinglePositionMarkerChanged(iPartsDataPicOrderPart picOrderPart) {
        if (picOrderPart != null) {
            String positionMarkerBeforeEdit = positionMarkersBeforeEdit.get(createPartlistEntryIdWithHotspotAndPartNumber(picOrderPart));
            if (positionMarkerBeforeEdit == null) {
                return true;
            }
            return !picOrderPart.getPicturePositionMarker().equals(positionMarkerBeforeEdit);
        }

        return false;
    }

    /**
     * Ändert die Selektionsstatus der übergebenen Stücklistenpositionen.
     *
     * @param selectedList
     * @param isSelected
     */
    private void modifySelectionInGrids(List<List<EtkDataObject>> selectedList, boolean isSelected) {
        for (List<EtkDataObject> dataObjectList : selectedList) {
            String partListEntryWithHotSpotAndPartNumber = null;
            // Erst die PartListEntryId bestimmen
            for (EtkDataObject dataObject : dataObjectList) {
                if (dataObject instanceof EtkDataPartListEntry) {
                    partListEntryWithHotSpotAndPartNumber = createPartlistEntryIdWithHotspotAndPartNumber((EtkDataPartListEntry)dataObject);
                    break;
                } else if (dataObject instanceof iPartsDataPicOrderPart) {
                    partListEntryWithHotSpotAndPartNumber = createPartlistEntryIdWithHotspotAndPartNumber((iPartsDataPicOrderPart)dataObject);
                    break;
                }
            }

            // Jetzt die Selektion ändern. Gelöschte Positionen wandern nicht vom oberen Grid ins untere, sie werden
            // direkt entfernt.
            if (partListEntryWithHotSpotAndPartNumber != null) {
                PartListEntryExtra partListEntryExtra = completeEntriesMap.get(partListEntryWithHotSpotAndPartNumber);
                if (partListEntryExtra != null) {
                    if (partListEntryExtra.isDeleted) {
                        completeEntriesMap.remove(partListEntryExtra.partListEntryIdWithHotSpotAndPartNumber);
                    } else {
                        partListEntryExtra.isSelected = isSelected;
                        if (!isSelected && partListEntryExtra.storedAndRealEntriesDifferent()) {
                            // Werte in der echten Stücklistenposition sind anders als die Werte in der gespeicherten
                            // → Entferne die verknüpften Objekte, damit bei einer neuen Selektion des veränderten
                            // Stücklisteneintrags neue verknüpfte Objekte erzeugt werden.
                            partListEntryExtra.storedEntry = null;
                            partListEntryExtra.picOrderPart = null;
                            createdPicOrderParts.remove(partListEntryExtra.partListEntryIdWithHotSpotAndPartNumber);
                            forceSelectionHasChanged = true; // Ab sofort ist die Selektion auf jeden Fall verändert
                        } else if (isSelected) {
                            // Bildpositionen aus dem unteren Grid, werden für das obere Grid ausgewählt → temporäres
                            // iPartsDataPicOrderPart Objekt erzeugen
                            iPartsDataPicOrderPart picOrderPart = createDataPicOrderPart(getProject(), dataPicOrder, partListEntryExtra.partListEntry);

                            // Wenn schon ein iPartsDataPicOrderPart Objekt existiert, dann dessen Daten verwenden
                            iPartsDataPicOrderPart existingPicOrderPart = partListEntryExtra.picOrderPart;
                            if ((existingPicOrderPart != null) && existingPicOrderPart.existsInDB()) {
                                picOrderPart.assignRecursively(getProject(), existingPicOrderPart, DBActionOrigin.FROM_EDIT);
                            }

                            createdPicOrderParts.put(createPartlistEntryIdWithHotspotAndPartNumber(picOrderPart), picOrderPart);
                        }
                    }
                }
            }
        }
        fillGrids();
    }

    /**
     * Überträgt und entfernt Einträge aus dem Grid der selektierten Stücklistenpositionen
     *
     * @return
     */
    private boolean transferDataToPicOrder() {
        Map<String, iPartsDataPicOrderPart> remainingPicOrderParts = new LinkedHashMap<>();
        List<iPartsDataPicOrderPart> picOrderPartsForDeletion = new ArrayList<>();

        // 1. Schritt: alle gelöschten identifizieren und bestehende unter Umständen anpassen
        // Map mit allen Einträgen, die im oberen Grid stehen (egal welcher Status)
        Map<String, PartListEntryExtra> selectedMap = getSelectedPartListEntriesAsMap();
        for (iPartsDataPicOrderPart picOrderPart : dataPicOrder.getParts()) {
            // 1.Fall: Wenn Bildposition im oberen Grid nicht vorkommt, dann löschen.
            // 2.Fall: Liegt eine bestehende Bildposition in der Map mit den neu erzeugten Bildpositionen, dann handelt
            // es sich um Bildpositionen, die sich entweder geändert haben oder vorher schon dem Auftrag hinzugefügt
            // wurden (z.B. Menüpunkt in Stückliste). Zur Sicherheit werden die Attribute der erzeugten Instanzen
            // auf die bestehenden Objekte vererbt.
            // 3.Fall: Bildposition kam vorher rein und hat sich nicht verändert (in createdPicOrderParts nicht vorhanden)
            String partListEntryIdWithHotSpotAndPartNumber = createPartlistEntryIdWithHotspotAndPartNumber(picOrderPart);
            if (!selectedMap.containsKey(partListEntryIdWithHotSpotAndPartNumber)) {
                picOrderPartsForDeletion.add(picOrderPart);
            } else if (createdPicOrderParts.containsKey(partListEntryIdWithHotSpotAndPartNumber)) {
                picOrderPart.assignRecursively(getProject(), createdPicOrderParts.get(partListEntryIdWithHotSpotAndPartNumber), DBActionOrigin.FROM_EDIT);
                picOrderPart.clearStoredEntry();
                remainingPicOrderParts.put(partListEntryIdWithHotSpotAndPartNumber, picOrderPart);
            } else {
                // Falls sich bei einer bestehenden Bildposition der Kenner geändert hat, dann muss er hier gesetzt
                // werden. Hierfür wird der Wert des Klons ins Original geschrieben.
                PartListEntryExtra cloneWithNewValue = selectedMap.get(partListEntryIdWithHotSpotAndPartNumber);
                if (checkSinglePositionMarkerChanged(cloneWithNewValue.picOrderPart)) {
                    picOrderPart.setFieldValue(iPartsConst.FIELD_DA_PPA_PIC_POSITION_MARKER, cloneWithNewValue.picOrderPart.getPicturePositionMarker(),
                                               DBActionOrigin.FROM_EDIT);
                    // Positionskenner hat sich geändert → Position wird als neue Info versorgt
                    picOrderPart.setFieldValueAsInteger(iPartsConst.FIELD_DA_PPA_SEQ_NO, 0, DBActionOrigin.FROM_EDIT);
                }
                remainingPicOrderParts.put(partListEntryIdWithHotSpotAndPartNumber, picOrderPart);
            }
        }

        // 2.Schritt: alle gelöschten entfernen
        for (iPartsDataPicOrderPart picOrderPart : picOrderPartsForDeletion) {
            dataPicOrder.getParts().delete(picOrderPart, DBActionOrigin.FROM_EDIT);
        }

        // 3. Schritt: Alle neu hinzugekommenen hinzufügen
        // Durchlaufe alle, die selektiert sind (im oberen Grid).
        // Hat ein Eintrag kein iPartsPicOrderPart Objekt, dann hängt er noch nicht am Bildauftrag → Objekt erzeugen
        // bzw. temporäres Objekt holen und hinzufügen
        for (PartListEntryExtra selectedPartListEntryExtra : selectedMap.values()) {
            iPartsDataPicOrderPart picOrderPart = remainingPicOrderParts.get(selectedPartListEntryExtra.partListEntryIdWithHotSpotAndPartNumber);
            if (picOrderPart == null) {
                if (createdPicOrderParts.containsKey(selectedPartListEntryExtra.partListEntryIdWithHotSpotAndPartNumber)) {
                    picOrderPart = createdPicOrderParts.get(selectedPartListEntryExtra.partListEntryIdWithHotSpotAndPartNumber);
                } else {
                    // Zur Sicherheit, da die Objekte eigentlich in modifySelectionInGrids() erzeugt werden
                    picOrderPart = createDataPicOrderPart(getProject(), dataPicOrder, selectedPartListEntryExtra.partListEntry);
                }
                dataPicOrder.getParts().add(picOrderPart, DBActionOrigin.FROM_EDIT);
            }
        }
        return true;
    }

    private void doAdd(Event event) {
        if (isEditAllowed) {
            modifySelectionInGrids(actualDataGrid.getMultiSelection(), true);
        }
    }

    private void doRemove(Event event) {
        if (isEditAllowed) {
            modifySelectionInGrids(selectedDataGrid.getMultiSelection(), false);
        }
    }

    private void buttonOKClicked(Event event) {
        if (isEditAllowed) {
            if (transferDataToPicOrder()) {
                mainWindow.setModalResult(ModalResult.OK);
                close();
            }
        } else {
            mainWindow.setModalResult(ModalResult.CANCEL);
            close();
        }
    }

    private class PartListEntryExtra {

        public boolean isSelected;
        public boolean isDeleted;
        public EtkDataPartListEntry partListEntry;
        public EtkDataPartListEntry storedEntry;
        public iPartsDataPicOrderPart picOrderPart;
        public String partListEntryIdWithHotSpotAndPartNumber;

        public boolean storedAndRealEntriesDifferent() {
            if (picOrderPart == null) {
                return true;
            }
            EtkDataPartListEntry clone = partListEntry.cloneMe(getProject());
            clone.assignAttributesValues(getProject(), picOrderPart.getSerializedDBAttributes(), false, DBActionOrigin.FROM_EDIT);
            return clone.isModified();
        }
    }

    private class PicturePositionGrid extends EditDataObjectGrid {

        public PicturePositionGrid(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
            super(dataConnector, parentForm);
        }

        @Override
        protected void onTableSelectionChanged(Event event) {
            if ((picPosMarkerMenu != null)) {
                boolean picPosMarkerChangeVisible = false;
                if (isEditAllowed) {
                    List<List<EtkDataObject>> selection = getMultiSelection();
                    picPosMarkerChangeVisible = true;
                    String positionMarker = null;
                    for (List<EtkDataObject> singleRowWithDataObjects : selection) {
                        String picOrderPositionMarker = getPicOrderPositionMarker(singleRowWithDataObjects);
                        if (StrUtils.isEmpty(picOrderPositionMarker)) {
                            // Einer der ausgewählten Einträge hat gar keinen Marker → es sind alle erlaubt
                            positionMarker = null;
                            break;
                        }
                        if (StrUtils.isEmpty(positionMarker)) {
                            positionMarker = picOrderPositionMarker;
                        } else {
                            if ((positionMarker != null) && !positionMarker.equals(picOrderPositionMarker)) {
                                positionMarker = null;
                                break;
                            }
                        }
                    }
                    for (AbstractGuiControl statusSubMenuItem : picPosMarkerMenu.getChildren()) {
                        boolean subMenuVisible = true;
                        Object picPosMaerkerMenuItemUserObject = statusSubMenuItem.getUserObject();
                        if (picPosMaerkerMenuItemUserObject instanceof EnumEntry) {
                            EnumEntry enumEntry = (EnumEntry)picPosMaerkerMenuItemUserObject;
                            if (StrUtils.isValid(positionMarker)) {
                                if (enumEntry.getToken().equals(positionMarker)) {
                                    subMenuVisible = false;
                                }
                            }
                        }
                        statusSubMenuItem.setVisible(subMenuVisible);
                    }
                }
                picPosMarkerMenu.setVisible(picPosMarkerChangeVisible);
                picPosMarkerSeparator.setVisible(picPosMarkerChangeVisible);
            }
            enableToolbar(this);
        }

        private String getPicOrderPositionMarker(List<EtkDataObject> singleRowWithDataObjects) {
            for (EtkDataObject etkDataObject : singleRowWithDataObjects) {
                if (etkDataObject instanceof iPartsDataPicOrderPart) {
                    iPartsDataPicOrderPart picOrderPart = (iPartsDataPicOrderPart)etkDataObject;
                    return picOrderPart.getPicturePositionMarker();
                }
            }
            return null;
        }

        @Override
        protected void onCellDblClicked(int row, int col, Event event) {
            doRemove(event);
        }

        @Override
        protected void createContextMenuItems(GuiContextMenu contextMenu) {
            picPosMarkerMenu = getToolbarHelper().createMenuEntry("picPosMarkerChange", "!!Positionskenner ändern...", null, null, getUITranslationHandler());
            String enumKey = getProject().getEtkDbs().getEnum(TableAndFieldName.make(iPartsConst.TABLE_DA_PICORDER_PARTS, iPartsConst.FIELD_DA_PPA_PIC_POSITION_MARKER));
            EnumValue enumValue = getProject().getEtkDbs().getEnumValue(enumKey);
            boolean isChangeOrder = dataPicOrder.isChangeOrCopy();
            for (final EnumEntry value : enumValue.values()) {
                // Bei einem initialen Bildauftrag darf keine Position auf "D" gesetzt werden
                if (!isChangeOrder && value.getToken().equals(iPartsDataPicOrderPart.PIC_POS_MARKER_DELETED_VALUE)) {
                    continue;
                }
                GuiMenuItem item = new GuiMenuItem();
                item.setUserObject(value);
                item.setText(value.getEnumText().getText(getProject().getDBLanguage()));
                item.addEventListener(new EventListener<>(Event.MENU_ITEM_EVENT) {
                    @Override
                    public void fire(Event event) {
                        changePicPosMarker(value);
                    }
                });
                picPosMarkerMenu.addChild(item);
            }
            contextMenu.addChild(picPosMarkerMenu);
            picPosMarkerSeparator = new GuiSeparator();
            picPosMarkerSeparator.setName("picPosMarkerChangeSeparator");
            contextMenu.addChild(picPosMarkerSeparator);
            GuiMenuItem menuItem = getToolbarHelper().createContextMenuEntry(EditToolbarButtonAlias.IMG_DOWN, "!!Entfernen", getUITranslationHandler(), new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    doRemove(event);
                }
            });
            contextMenu.addChild(menuItem);
        }

        private void changePicPosMarker(EnumEntry value) {
            List<List<EtkDataObject>> selection = getMultiSelection();
            for (List<EtkDataObject> singleRowWithDataObjects : selection) {
                for (EtkDataObject dataObject : singleRowWithDataObjects) {
                    if (dataObject instanceof iPartsDataPicOrderPart) {
                        String chosenMarker = value.getToken();
                        // Setze den neuen Kenner
                        dataObject.setFieldValue(iPartsConst.FIELD_DA_PPA_PIC_POSITION_MARKER, chosenMarker, DBActionOrigin.FROM_EDIT);
                    }
                }
            }
            fillGrids();
        }
    }

    /**
     * Check, ob der übergebene Positionsmarker im übergebenen {@link iPartsDataPicOrderPart} von seinem DB Wert abweicht.
     *
     * @param picOrderPart
     * @return
     */
    private boolean hasSamePositionMarkerAsInDB(iPartsDataPicOrderPart picOrderPart) {
        String partListEntryWithHotSpotAndPartNumber = createPartlistEntryIdWithHotspotAndPartNumber(picOrderPart);
        if (!positionMarkerInDB.containsKey(partListEntryWithHotSpotAndPartNumber)) {
            return false;
        }
        return positionMarkerInDB.get(partListEntryWithHotSpotAndPartNumber).equals(picOrderPart.getPicturePositionMarker());
    }

    /**
     * Erzeugt ein {@link iPartsDataPicOrderPart} Objekt mit den nötigsten Informationen.
     *
     * @param project
     * @param dataPicOrder
     * @param partListEntry
     * @return
     */
    public static iPartsDataPicOrderPart createDataPicOrderPart(EtkProject project, iPartsDataPicOrder dataPicOrder, EtkDataPartListEntry partListEntry) {
        iPartsPicOrderPartId id = new iPartsPicOrderPartId(dataPicOrder.getAsId().getOrderGuid(), partListEntry.getAsId(),
                                                           partListEntry.getFieldValue(iPartsConst.FIELD_K_POS), partListEntry.getFieldValue(iPartsConst.FIELD_K_MATNR));
        iPartsDataPicOrderPart picOrderPart = new iPartsDataPicOrderPart(project, id);
        picOrderPart.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);

        // Setzen des SOURCE_TYPE abhängig von der Assembly
        String partListType = partListEntry.getOwnerAssembly().getEbeneName();
        picOrderPart.setFieldValue(iPartsConst.FIELD_DA_PPA_SRC_KEY, iPartsPicOrderPartSourceType.getFromPartListType(partListType).getDbValue(),
                                   DBActionOrigin.FROM_EDIT);

        picOrderPart.setFieldValueAsBoolean(iPartsConst.FIELD_DA_PPA_CONTEXT, false, DBActionOrigin.FROM_EDIT);  // KontextInfo oder zum Bild gehörend
        picOrderPart.setFieldValueAsBoolean(iPartsConst.FIELD_DA_PPA_SENT, false, DBActionOrigin.FROM_EDIT);
        return picOrderPart;
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
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitpane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpane_firstChild;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelSelectedEntrie;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelSelectedEntries;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpane_secondChild;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_0;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMovement;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.toolbar.GuiToolbar toolbar;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_1;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelActualEntries;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelActualEntries;

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
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMain.setLayout(panelMainLayout);
            splitpane = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitpane.setName("splitpane");
            splitpane.__internal_setGenerationDpi(96);
            splitpane.registerTranslationHandler(translationHandler);
            splitpane.setScaleForResolution(true);
            splitpane.setMinimumWidth(10);
            splitpane.setMinimumHeight(10);
            splitpane.setHorizontal(false);
            splitpane.setDividerPosition(197);
            splitpane_firstChild = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpane_firstChild.setName("splitpane_firstChild");
            splitpane_firstChild.__internal_setGenerationDpi(96);
            splitpane_firstChild.registerTranslationHandler(translationHandler);
            splitpane_firstChild.setScaleForResolution(true);
            splitpane_firstChild.setMinimumWidth(0);
            splitpane_firstChild.setMinimumHeight(0);
            splitpane_firstChild.setBorderWidth(8);
            de.docware.framework.modules.gui.layout.LayoutBorder splitpane_firstChildLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpane_firstChild.setLayout(splitpane_firstChildLayout);
            labelSelectedEntrie = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelSelectedEntrie.setName("labelSelectedEntrie");
            labelSelectedEntrie.__internal_setGenerationDpi(96);
            labelSelectedEntrie.registerTranslationHandler(translationHandler);
            labelSelectedEntrie.setScaleForResolution(true);
            labelSelectedEntrie.setMinimumWidth(10);
            labelSelectedEntrie.setMinimumHeight(10);
            labelSelectedEntrie.setPaddingTop(4);
            labelSelectedEntrie.setPaddingLeft(8);
            labelSelectedEntrie.setPaddingRight(8);
            labelSelectedEntrie.setPaddingBottom(4);
            labelSelectedEntrie.setText("!!Selektierte Einträge");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelSelectedEntrieConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelSelectedEntrieConstraints.setPosition("north");
            labelSelectedEntrie.setConstraints(labelSelectedEntrieConstraints);
            splitpane_firstChild.addChild(labelSelectedEntrie);
            panelSelectedEntries = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelSelectedEntries.setName("panelSelectedEntries");
            panelSelectedEntries.__internal_setGenerationDpi(96);
            panelSelectedEntries.registerTranslationHandler(translationHandler);
            panelSelectedEntries.setScaleForResolution(true);
            panelSelectedEntries.setMinimumWidth(10);
            panelSelectedEntries.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelSelectedEntriesLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelSelectedEntries.setLayout(panelSelectedEntriesLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelSelectedEntriesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelSelectedEntries.setConstraints(panelSelectedEntriesConstraints);
            splitpane_firstChild.addChild(panelSelectedEntries);
            splitpane.addChild(splitpane_firstChild);
            splitpane_secondChild = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpane_secondChild.setName("splitpane_secondChild");
            splitpane_secondChild.__internal_setGenerationDpi(96);
            splitpane_secondChild.registerTranslationHandler(translationHandler);
            splitpane_secondChild.setScaleForResolution(true);
            splitpane_secondChild.setMinimumWidth(0);
            splitpane_secondChild.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.LayoutBorder splitpane_secondChildLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpane_secondChild.setLayout(splitpane_secondChildLayout);
            panel_0 = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_0.setName("panel_0");
            panel_0.__internal_setGenerationDpi(96);
            panel_0.registerTranslationHandler(translationHandler);
            panel_0.setScaleForResolution(true);
            panel_0.setMinimumWidth(10);
            panel_0.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_0Layout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_0.setLayout(panel_0Layout);
            panelMovement = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelMovement.setName("panelMovement");
            panelMovement.__internal_setGenerationDpi(96);
            panelMovement.registerTranslationHandler(translationHandler);
            panelMovement.setScaleForResolution(true);
            panelMovement.setMinimumWidth(10);
            panelMovement.setMinimumHeight(28);
            panelMovement.setBorderWidth(8);
            de.docware.framework.modules.gui.layout.LayoutBorder panelMovementLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMovement.setLayout(panelMovementLayout);
            toolbar = new de.docware.framework.modules.gui.controls.toolbar.GuiToolbar();
            toolbar.setName("toolbar");
            toolbar.__internal_setGenerationDpi(96);
            toolbar.registerTranslationHandler(translationHandler);
            toolbar.setScaleForResolution(true);
            toolbar.setMinimumWidth(10);
            toolbar.setMinimumHeight(10);
            toolbar.setBackgroundImage(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignToolbarBigBackground"));
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder toolbarConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            toolbar.setConstraints(toolbarConstraints);
            panelMovement.addChild(toolbar);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMovementConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMovementConstraints.setPosition("north");
            panelMovement.setConstraints(panelMovementConstraints);
            panel_0.addChild(panelMovement);
            panel_1 = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_1.setName("panel_1");
            panel_1.__internal_setGenerationDpi(96);
            panel_1.registerTranslationHandler(translationHandler);
            panel_1.setScaleForResolution(true);
            panel_1.setMinimumWidth(10);
            panel_1.setMinimumHeight(10);
            panel_1.setBorderWidth(8);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_1Layout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_1.setLayout(panel_1Layout);
            labelActualEntries = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelActualEntries.setName("labelActualEntries");
            labelActualEntries.__internal_setGenerationDpi(96);
            labelActualEntries.registerTranslationHandler(translationHandler);
            labelActualEntries.setScaleForResolution(true);
            labelActualEntries.setMinimumWidth(10);
            labelActualEntries.setMinimumHeight(10);
            labelActualEntries.setPaddingTop(4);
            labelActualEntries.setPaddingLeft(8);
            labelActualEntries.setPaddingRight(8);
            labelActualEntries.setPaddingBottom(4);
            labelActualEntries.setText("!!Aktuelle Einträge");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelActualEntriesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelActualEntriesConstraints.setPosition("north");
            labelActualEntries.setConstraints(labelActualEntriesConstraints);
            panel_1.addChild(labelActualEntries);
            panelActualEntries = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelActualEntries.setName("panelActualEntries");
            panelActualEntries.__internal_setGenerationDpi(96);
            panelActualEntries.registerTranslationHandler(translationHandler);
            panelActualEntries.setScaleForResolution(true);
            panelActualEntries.setMinimumWidth(10);
            panelActualEntries.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelActualEntriesLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelActualEntries.setLayout(panelActualEntriesLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelActualEntriesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelActualEntries.setConstraints(panelActualEntriesConstraints);
            panel_1.addChild(panelActualEntries);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panel_1Constraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panel_1.setConstraints(panel_1Constraints);
            panel_0.addChild(panel_1);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panel_0Constraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panel_0.setConstraints(panel_0Constraints);
            splitpane_secondChild.addChild(panel_0);
            splitpane.addChild(splitpane_secondChild);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder splitpaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            splitpane.setConstraints(splitpaneConstraints);
            panelMain.addChild(splitpane);
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
                    buttonOKClicked(event);
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