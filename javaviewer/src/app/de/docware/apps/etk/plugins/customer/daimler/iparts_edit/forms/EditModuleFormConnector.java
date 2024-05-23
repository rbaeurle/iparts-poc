/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.config.partlist.EtkEbenenDaten;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormConnector;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.ChangeFlag;
import de.docware.apps.etk.base.print.base.printConfigSimpleTypes;
import de.docware.apps.etk.base.print.wrapper.PrintCreateImageWithHotspotCallback;
import de.docware.apps.etk.base.project.ItemsOnImageUserSettings;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.NavigationPath;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataHotspot;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImage;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataPool;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsAuthorOrderId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrderList;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.filter.iPartsEditFilter;
import de.docware.framework.modules.gui.controls.viewer.GuiViewerUtils;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.utils.NotImplementedCode;
import de.docware.util.Utils;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Connector für das Editieren von Modulen in iParts.
 */
public class EditModuleFormConnector extends AbstractJavaViewerFormConnector implements EditModuleFormIConnector {

    private EtkDataAssembly currentAssembly = EtkDataObjectFactory.createDataAssembly(); // Selektierter Assembly JAVA_VIEWER-1057 fixme_ms solle hier nicht nur die AssemblyId stehen?
    private iPartsAuthorOrderId currentDataAuthorOrderId;
    private String additionalTextForHeader;
    private List<EtkDataPartListEntry> currentSelectedPartListEntries = new ArrayList<>();
    private List<EtkDataPartListEntry> sortedVisiblePartListEntries = new ArrayList<>();
    private List<EtkDataPartListEntry> currentPartListEntries = null; // von außen gesetzte PartlistEntries, falls null, alle der Assembly
    private int imageIndex = 0;
    private boolean tableSortingActive = false;
    private boolean tableFilteringActive = false;
    private iPartsDataPicOrderList pictureOrderList;
    private boolean isAlphaNumHotspotAllowed = true;
    private EtkDisplayFields assemblyListDisplayField;
    private int thumbnailImageIndex = -1;
    private boolean thumbnailViewActive = false;
    private boolean imagesNextLevelActive = false;
    private boolean showOnlyModifiedPartListEntries = false;
    private boolean preferSVGImages = false;
    private ItemsOnImageUserSettings itemsOnImageUserSettings;

    protected ChangeFlag flagCurrentAssemblyChanged = new ChangeFlag(this, "CurrentAssembly");
    protected ChangeFlag flagCurrentAssemblyIdChanged = new ChangeFlag(this, "CurrentAssemblyId");
    protected ChangeFlag flagSelectedPartListEntryChanged = new ChangeFlag(this, "SelectedPartListEntry");
    protected ChangeFlag flagImageIndexChanged = new ChangeFlag(this, "ImageIndex");
    protected ChangeFlag flagPosNumberChanged = new ChangeFlag(this, "PosNumberChanged");
    protected ChangeFlag flagViewerOptionsChanged = new ChangeFlag(this, "ViewerOptions");
    protected ChangeFlag flagPictureOrderChanged = new ChangeFlag(this, "PictureOrder");
    protected ChangeFlag flagPartListEntriesModifiedChanged = new ChangeFlag(this, "PartListEntriesModified");
    protected ChangeFlag flagThumbnailActiveChanged = new ChangeFlag(this, "ThumbnailActive");
    protected ChangeFlag flagThumbnailImageIndexChanged = new ChangeFlag(this, "ThumbnailImageIndex");
    protected ChangeFlag flagImagesNextLevelActiveChanged = new ChangeFlag(this, "ImagesNextLevelActive");

    /**
     * @param owner
     */
    public EditModuleFormConnector(AbstractJavaViewerFormIConnector owner) {
        super(owner);
        setCurrentDataAuthorOrder(null);
        itemsOnImageUserSettings = getProject().getTempUserSettings().getItemsOnImageUserSettings();
    }

    public EditModuleFormConnector cloneMe(boolean cloneSelectedPartListEntries) {
        EditModuleFormConnector clone = new EditModuleFormConnector(owner);
        clone.currentAssembly = getCurrentAssembly(); // getCurrentAssembly() wird in Ableitungen überschrieben
        clone.currentDataAuthorOrderId = currentDataAuthorOrderId;
        clone.additionalTextForHeader = additionalTextForHeader;
        if (cloneSelectedPartListEntries && (currentSelectedPartListEntries != null)) {
            clone.currentSelectedPartListEntries = new ArrayList<>(currentSelectedPartListEntries);
        }
        if (sortedVisiblePartListEntries != null) {
            clone.sortedVisiblePartListEntries = new ArrayList<>(sortedVisiblePartListEntries);
        }
        if (currentPartListEntries != null) {
            clone.currentPartListEntries = new ArrayList<>(currentPartListEntries);
        }
        clone.imageIndex = imageIndex;
        clone.tableSortingActive = tableSortingActive;
        clone.tableFilteringActive = tableFilteringActive;
        clone.pictureOrderList = pictureOrderList;
        clone.isAlphaNumHotspotAllowed = isAlphaNumHotspotAllowed;
        clone.assemblyListDisplayField = assemblyListDisplayField;
        clone.thumbnailImageIndex = thumbnailImageIndex;
        clone.thumbnailViewActive = thumbnailViewActive;
        clone.imagesNextLevelActive = imagesNextLevelActive;
        clone.showOnlyModifiedPartListEntries = showOnlyModifiedPartListEntries;
        clone.itemsOnImageUserSettings = itemsOnImageUserSettings;
        return clone;
    }

    @Override
    public String getPosFieldName() {
        EtkEbenenDaten partsListType = getProject().getConfig().getPartsDescription().getEbene(getCurrentAssembly().getEbeneName());
        String fieldName = "";
        if (partsListType != null) {
            fieldName = TableAndFieldName.getFieldName(partsListType.getKeyFeldName());
        }
        if (fieldName.isEmpty()) {
            fieldName = EtkDbConst.FIELD_K_POS;
        }
        return fieldName;
    }

    @Override
    public Set<String> getHotspotSet() {
        Set<String> hotspotList = new LinkedHashSet<>();
        for (EtkDataImage image : getCurrentAssembly().getUnfilteredImages()) {
            hotspotList.addAll(getHotSpotSetPerImage(image));
        }
        return hotspotList;
    }

    @Override
    public Set<String> getHotSpotSetPerImage(EtkDataImage image) {
        Set<String> hotspotList = new LinkedHashSet<>();
        String imgUsage = isImageIs3D() ? EtkDataImage.IMAGE_USAGE_3D : EtkDataImage.IMAGE_USAGE_2D;

        EtkDataPool imageVariant = image.getBestImageVariant(getProject().getDBLanguage(), imgUsage);
        if (imageVariant != null) {
            for (EtkDataHotspot hotSpot : imageVariant.getHotspots()) {
                String lText = hotSpot.getFieldValue(EtkDbConst.FIELD_L_TEXT);
                hotspotList.add(lText);
            }
        }
        return hotspotList;
    }

    @Override
    public Set<String> getPosNumberSet() {
        Set<String> posNoList = new LinkedHashSet<>();
        String fieldName = getPosFieldName();
        for (EtkDataPartListEntry partListEntry : getCurrentPartListEntries()) {
            if (!partListEntry.getFieldValueAsBoolean(iPartsConst.FIELD_K_OMIT)) {
                String kPos = partListEntry.getFieldValue(fieldName);
                if (kPos.isEmpty()) {
                    posNoList.add("");
                } else {
                    posNoList.addAll(GuiViewerUtils.splitPosNumber(kPos));
                }
            }
        }
        return posNoList;
    }

    @Override
    public void updatePictureOrderList() {
        setPictureOrderList(iPartsDataPicOrderList.loadPicOrderList(getProject(), currentAssembly.getAsId().getKVari()));
    }

    /**
     * Speichert alle geänderten {@link EtkDataPartListEntry}s. Die eigentliche {@link EtkDataAssembly} wird nicht gespeichert.
     *
     * @param changeEventSender
     * @param fireDataChangedEvent
     */
    @Override
    public void savePartListEntries(AbstractJavaViewerForm changeEventSender, boolean fireDataChangedEvent) {
        boolean somethingWasSaved;
        if (getCurrentAssembly() instanceof iPartsDataAssembly) {
            iPartsDataAssembly assembly = (iPartsDataAssembly)getCurrentAssembly();
            somethingWasSaved = assembly.savePartListEntries();
        } else {
            throw new RuntimeException("Assembly must be an iPartsDataAssembly");
        }

        if (somethingWasSaved && fireDataChangedEvent) {
            // Benachrichtigungen der Änderungen verschicken.
            if (getProject().isRevisionChangeSetActiveForEdit()) {
                // Im Changeset nur mich selbst benachrichtigen
                getProject().fireProjectEvent(new DataChangedEvent(changeEventSender), true);
            } else {
                // Falls es direkt in die Datenbank gespeichert wurde auch alle anderen Sessions und Cluster-Knoten benachrichtigen
                ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PART_LIST,
                                                                                                          iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                                          getCurrentAssembly().getAsId(), false));
                ApplicationEvents.fireEventInAllProjectsAndAllClusters(new DataChangedEvent(changeEventSender));
            }
        }
    }


    @Override
    public void setPictureOrderList(iPartsDataPicOrderList pictureOrderList) {
        if (!Utils.objectEquals(this.pictureOrderList, pictureOrderList)) {
            this.pictureOrderList = pictureOrderList;

            flagPictureOrderChanged.setChanged();
        }
    }

    @Override
    public iPartsDataPicOrderList getPictureOrderList() {
        return pictureOrderList;
    }

    @Override
    public boolean isFlagPictureOrderChanged() {
        return flagPictureOrderChanged.isChanged();
    }

    @Override
    public EtkDataAssembly getCurrentAssembly() {
        return currentAssembly;
    }

    @Override
    public void setCurrentAssembly(EtkDataAssembly value) {
        if (!Utils.objectEquals(currentAssembly, value)) {
            if ((currentAssembly != null) && (value != null) && !Utils.objectEquals(currentAssembly.getAsId(), value.getAsId())) {
                flagCurrentAssemblyIdChanged.setChanged();
            }

            currentSelectedPartListEntries.clear();
            currentAssembly = value;

            // Stückliste ist im Edit-Modus
            if (currentAssembly instanceof iPartsDataAssembly) {
                ((iPartsDataAssembly)currentAssembly).setEditMode(true);

            }

            flagCurrentAssemblyChanged.setChanged();
        }
    }

    @Override
    public iPartsDataAuthorOrder getCurrentDataAuthorOrder() {
        if (isAuthorOrderValid()) {
            iPartsDataAuthorOrder dataAuthorOrder = new iPartsDataAuthorOrder(getProject(), currentDataAuthorOrderId);
            if (dataAuthorOrder.loadFromDB(currentDataAuthorOrderId)) {
                return dataAuthorOrder;
            }
        }
        return null;
    }

    @Override
    public void setCurrentDataAuthorOrder(iPartsDataAuthorOrder currentDataAuthorOrder) {
        if (currentDataAuthorOrder != null) {
            this.currentDataAuthorOrderId = currentDataAuthorOrder.getAsId();
        } else {
            this.currentDataAuthorOrderId = null;
        }
    }

    @Override
    public boolean isAuthorOrderValid() {
        return (currentDataAuthorOrderId != null) && currentDataAuthorOrderId.isValidId() && iPartsRight.EDIT_PARTS_DATA.checkRightInSession();
    }

    @Override
    public String getAdditionalTextForHeader() {
        return additionalTextForHeader;
    }

    @Override
    public void setAdditionalTextForHeader(String additionalTextForHeader) {
        this.additionalTextForHeader = additionalTextForHeader;
    }

    @Override
    public List<EtkDataPartListEntry> getUnfilteredPartListEntries() {
        if (currentPartListEntries == null) {
            // Alle ungefilterten Entries der Baugruppe sollen angezeigt werden
            List<EtkDataPartListEntry> unfilteredList = getCurrentAssembly().getPartListUnfiltered(getConfig().getPartsDescription().getEbene(getCurrentAssembly().getEbeneName())).getAsList();
            if (showOnlyModifiedPartListEntries) {
                Iterator<EtkDataPartListEntry> iter = unfilteredList.iterator();
                while (iter.hasNext()) {
                    EtkDataPartListEntry dataPartListEntry = iter.next();
                    if (!iPartsEditFilter.get().checkEditFilter(dataPartListEntry)) {
                        iter.remove();
                    }
                }
                setCurrentPartListEntries(unfilteredList);
            }
            return unfilteredList;
        } else {
            // Es sollen nur spezielle angezeigt werden
            return currentPartListEntries;
        }
    }

    @Override
    public List<EtkDataPartListEntry> getCurrentPartListEntries() {
        return getUnfilteredPartListEntries(); // immer nur die ungefilterte Stückliste editieren
    }

    public void setCurrentPartListEntries(List<EtkDataPartListEntry> value) {
        this.currentPartListEntries = value;
    }

    @Override
    public void clearFilteredEditPartListEntries() {
        setCurrentPartListEntries(null);
    }

    public boolean isShowOnlyModifiedPartListEntries() {
        return showOnlyModifiedPartListEntries;
    }

    public void setShowOnlyModifiedPartListEntries(boolean showOnlyModifiedPartListEntries) {
        this.showOnlyModifiedPartListEntries = showOnlyModifiedPartListEntries;
        clearFilteredEditPartListEntries();
    }

    @Override
    public List<EtkDataPartListEntry> getSelectedPartListEntries() {
        return currentSelectedPartListEntries;
    }

    @Override
    public void setSelectedPartListEntries(List<EtkDataPartListEntry> value) {
        if (!Utils.objectEquals(currentSelectedPartListEntries, value)) {
            currentSelectedPartListEntries = value;
            flagSelectedPartListEntryChanged.setChanged();
        }
    }

    @Override
    public List<EtkDataPartListEntry> getSortedVisiblePartListEntries() {
        return sortedVisiblePartListEntries;
    }

    @Override
    public void setSortedVisiblePartListEntries(List<EtkDataPartListEntry> partListEntries) {
        this.sortedVisiblePartListEntries = partListEntries;
    }

    @Override
    public AssemblyId getRootAssemblyId() {
        // Im Editor ist immer der oberste Knoten der Root, damit z.B. auch alle Verwendungen gefunden werden
        return AssemblyId.getRootId();
    }

    @Override
    public NavigationPath getCurrentNavigationPath() {
        return new NavigationPath();
    }

    @Override
    public void setCurrentNavigationPath(NavigationPath path) {
    }

    @Override
    public int getImageIndex() {
        return Math.min(imageIndex, getImageCount() - 1);
    }

    @Override
    public int getThumbnailImageIndex() {
        return thumbnailImageIndex;
    }


    @Override
    public void setImageIndex(int value) {
        if (imageIndex != value) {
            imageIndex = value;
            flagImageIndexChanged.setChanged();
        }
    }

    @Override
    public void setThumbnailImageIndex(int value) {
        if (thumbnailImageIndex != value) {
            thumbnailImageIndex = value;
            flagThumbnailImageIndexChanged.setChanged();
        }
    }

    @Override
    public void setImageRotation(int rotation) {

    }

    @Override
    public int getImageRotation() {
        return 0;
    }

    @Override
    public boolean isFlagImageRotationChanged() {
        return false;
    }

    @Override
    public boolean isImageIs3D() {
        return false;
    }

    @Override
    public void setImageIs3D(boolean value) {
    }

    @Override
    public boolean isShow3DImages() {
        return false;
    }

    @Override
    public void setShow3DImages(boolean value) {
    }

    @Override
    public boolean isPreferSVGImages() {
        return preferSVGImages;
    }

    @Override
    public void setPreferSVGImages(boolean preferSVGImages) {
        this.preferSVGImages = preferSVGImages;
    }

    @Override
    public boolean isResponsiveMultiselectionActive() {
        return false;
    }

    @Override
    public void setResponsiveMultiselectionActive(boolean value) {

    }

    @Override
    public double getImageZoomFactor() {
        return 0;
    }

    @Override
    public void setImageZoomFactor(double zoomFactor) {
    }

    @Override
    public boolean isThumbnailViewActive() {
        return thumbnailViewActive;
    }

    @Override
    public void setThumbnailViewActive(boolean value) {
        if (thumbnailViewActive != value) {
            thumbnailViewActive = value;
            flagThumbnailActiveChanged.setChanged();
        }
    }

    @Override
    public boolean isImagesNextLevelActive() {
        return imagesNextLevelActive;
    }

    @Override
    public void setImagesNextLevelActive(boolean value) {
        if (imagesNextLevelActive != value) {
            imagesNextLevelActive = value;
            flagImagesNextLevelActiveChanged.setChanged();
        }
    }

    @Override
    public boolean isMultiImageViewActive() {
        return isThumbnailViewActive() || isImagesNextLevelActive();
    }

    @Override
    public boolean isFlagImagesNextLevelActiveChanged() {
        return flagImagesNextLevelActiveChanged.isChanged();
    }

    @Override
    public void setTableSortingActive(boolean value) {
        if (tableSortingActive != value) {
            tableSortingActive = value;
            flagViewerOptionsChanged.setChanged();
        }
    }

    @Override
    public boolean isTableSortingActive() {
        return tableSortingActive;
    }

    @Override
    public void setTableFilteringActive(boolean value) {
        if (tableFilteringActive != value) {
            tableFilteringActive = value;
            flagViewerOptionsChanged.setChanged();
        }
    }

    @Override
    public boolean isTableFilteringActive() {
        return tableFilteringActive;
    }

    @Override
    public boolean isAlphaNumHotspotAllowed() {
        return isAlphaNumHotspotAllowed;
    }

    @Override
    public void setShowUnmarkedHotspotsOnImage(boolean value) {
    }

    @Override
    public boolean isShowUnmarkedHotspotsOnImage() {
        return true;
    }

    @Override
    public void setOverlayFadingActive(boolean active) {
        // Gibts im EditModuleForm nicht
    }

    @Override
    public boolean isOverlayFadingActive() {
        return false;
    }

    @Override
    public int getImageCount() {
        return getCurrentAssembly().getImageCount(false);
    }

    @Override
    public EtkDataImage getImage(int imageIndex) {
        return getCurrentAssembly().getImage(imageIndex, false);
    }

    @Override
    public boolean isFlagHotspotVisibilityChanged() {
        return false;
    }

    @Override
    public EtkDisplayFields getAssemblyListDisplayFields() {
        return assemblyListDisplayField;
    }

    public void setAssemblyListDisplayFields(EtkDisplayFields displayFields) {
        this.assemblyListDisplayField = displayFields;
    }

    @Override
    public void print(boolean printCompleteAssembly, boolean printImage, boolean printPartsList, printConfigSimpleTypes.FloatRect imageClipping, List<PartListEntryId> selectedEntries, PrintCreateImageWithHotspotCallback selectedDrawingCallback, String subTitleForProgressDialog) {
        NotImplementedCode.execute(NotImplementedCode.IPARTS_EDITOR + " " + "print");
        MessageDialog.show("!!Funktion noch nicht implementiert.");
    }

    @Override
    public void setPartListEntriesModified() {
        flagPartListEntriesModifiedChanged.setChanged();
    }

    @Override
    public boolean isFlagSelectedPartListEntryChanged() {
        return flagSelectedPartListEntryChanged.isChanged();
    }

    @Override
    public boolean isFlagRootAssemblyChanged() {
        return false;
    }

    @Override
    public boolean isFlagPosNumberChanged() {
        return flagPosNumberChanged.isChanged();
    }

    @Override
    public void posNumberChanged() {
        this.flagPosNumberChanged.setChanged();
    }

    @Override
    public boolean isFlagImageIndexChanged() {
        return flagImageIndexChanged.isChanged();
    }

    @Override
    public boolean isFlagThumbnailImageIndexChanged() {
        return flagThumbnailImageIndexChanged.isChanged();
    }

    @Override
    public boolean isFlagResponsiveMultiselectionChanged() {
        return false;
    }

    @Override
    public boolean isFlagCurrentAssemblyChanged() {
        return flagCurrentAssemblyChanged.isChanged();
    }

    @Override
    public boolean isFlagCurrentAssemblyIdChanged() {
        return flagCurrentAssemblyIdChanged.isChanged();
    }

    @Override
    public boolean isFlagShowOnlyPartsOnPageChanged() {
        return false;
    }

    @Override
    public boolean isFlagCurrentAssemblyPathChanged() {
        return false;
    }

    @Override
    public boolean isFlagThumbnailActiveChanged() {
        return flagThumbnailActiveChanged.isChanged();
    }

    @Override
    public boolean isFlagImageIs3DChanged() {
        return false;
    }

    @Override
    public boolean isSelectionOnly() {
        return false;
    }

    @Override
    public boolean isFlagTableOptionsChanged() {
        return flagViewerOptionsChanged.isChanged();
    }

    @Override
    public boolean isFlagPartListEntriesModifiedChanged() {
        return flagPartListEntriesModifiedChanged.isChanged();
    }

    @Override
    public boolean isUpdatePendingNoIconThread() {
        return false;
    }

    @Override
    public void setUpdatePendingNoIconThread(boolean updatePendingNoIconThread) {

    }

    @Override
    public void setPositionPartListHorizontal(boolean value) {

    }

    @Override
    public boolean getPositionPartListHorizontal() {
        return false;
    }

    @Override
    public boolean isShowOnlyPartsOnPage() {
        return false;
    }

    @Override
    public void setShowOnlyPartsOnPage(boolean showOnlyPartsOnPage) {
    }

    @Override
    public boolean isViewerWithItemsOnImageChangeableByUser() {
        return itemsOnImageUserSettings.isViewerWithItemsOnImageChangeableByUser();
    }

    @Override
    public void setViewerWithItemsOnImageActive(boolean viewerWithItemsOnImageActive) {

    }

    @Override
    public boolean isViewerWithItemsOnImageActive() {
        return itemsOnImageUserSettings.isViewerWithItemsOnImageActive();
    }

    @Override
    public void setCurrentImageHasAtLeastOneHotspot(boolean hasAtLeastOneHotspot) {

    }

    @Override
    public boolean hasCurrentImageAtLeastOneHotspot() {
        return false;
    }
}