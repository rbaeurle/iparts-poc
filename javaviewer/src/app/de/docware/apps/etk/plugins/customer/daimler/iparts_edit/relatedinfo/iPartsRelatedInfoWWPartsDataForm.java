/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.components.DataObjectFilterGrid;
import de.docware.apps.etk.base.forms.common.components.MenuRunnable;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListCellContentFromPlugin;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.base.project.events.GotoPartWithPartialPathEvent;
import de.docware.apps.etk.base.project.mechanic.*;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.AbstractSimpleDataObjectGridForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsUserSettingsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsWWPartsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.RelatedInfoSingleEditHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.iPartsRelatedInfoEditContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.GotoEditPartWithPartialPathEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditWWToPartlistEntryForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.app.DWLayoutManager;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.misc.DWCursor;
import de.docware.framework.modules.gui.controls.toolbar.AbstractGuiToolComponent;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolbar;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.HTMLUtils;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.SQLStringConvert;

import java.util.*;

/**
 * Formular für die Wahlweise-Teile zu einem Stücklisteneintrag innerhalb der RelatedInfo.
 */
public class iPartsRelatedInfoWWPartsDataForm extends AbstractSimpleDataObjectGridForm implements iPartsConst, iPartsSuperEditRelatedInfoInterface {

    public static final String IPARTS_MENU_ITEM_SHOW_WW_PARTS_DATA = "iPartsMenuItemShowWWPartsData";
    public static final String CONFIG_KEY_WW_PARTS_DATA = "Plugin/iPartsEdit/WWParts";

    private static final EnumSet<iPartsModuleTypes> VALID_MODULE_TYPES = iPartsModuleTypes.EDITABLE_MODULE_TYPES;

    private PartListEntryId loadedPartListEntryId;
    private EtkDataPartListEntry partListEntry;
    private boolean isEditRelatedInfo;
    private Map<PartListEntryId, String> wwSetMap;
    private Map<PartListEntryId, Collection<EtkDataPartListEntry>> extraWWOfPartListEntryMap;
    private Collection<EtkDataPartListEntry> originalWWParts;
    private List<PartListEntryId> originalWWPartIds;
    private boolean suppressReloadData;
    private boolean isFilterActive;

    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, final AssemblyListFormIConnector connector) {
        modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_WW_PARTS_DATA, "!!Wahlweise-Teile anzeigen",
                                EditDefaultImages.edit_ww_parts.getImage(), iPartsConst.CONFIG_KEY_RELATED_INFO_WW_PARTS_DATA);
    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        // Ist die aktuelle Stückliste gültig für den gewünschten Modultyp?
        boolean menuItemVisible = false;
        if (connector.getProject().isEditModeActive() && (connector.getSelectedPartListEntries() != null) && (connector.getSelectedPartListEntries().size() == 1)) {
            boolean isEditRelatedInfo = isEditContext(connector, false);
            boolean isFilterActive = !isEditRelatedInfo && iPartsAssemblyListSelectFormConnectorWithFilterSettings.isFilterActive(connector);
            menuItemVisible = relatedInfoIsVisible(connector.getSelectedPartListEntries().get(0),
                                                   isEditContext(connector, true), isFilterActive);
        }

        // Im Edit nur Menüeinträge zur Bearbeitung anzeigen
        if (menuItemVisible && isEditContext(connector, true)) {
            menuItemVisible = false;
        }

        // Separator und Menüeintrag aktualisieren
        updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_SHOW_WW_PARTS_DATA, menuItemVisible);
    }

    public static boolean relatedInfoIsVisible(EtkDataPartListEntry entry, boolean isEditRelatedInfo, boolean isFilterActive) {
        if ((entry != null) && relatedInfoIsVisible(entry.getOwnerAssembly(), VALID_MODULE_TYPES)) {
            if (entry instanceof iPartsDataPartListEntry) {
                if (isEditRelatedInfo) {
                    if (iPartsUserSettingsHelper.isSingleEdit(entry.getEtkProject())) {
                        return true;
                    }
                } else {
                    return iPartsWWPartsHelper.hasWWParts((iPartsDataPartListEntry)entry, isFilterActive);
                }
            }
        }
        return false;
    }

    public static AssemblyListCellContentFromPlugin getRelatedInfoIcon(iPartsDataPartListEntry iPartsEntry, boolean isFilterActive, boolean isEditMode) {
        // Prüfen ob auch mit aktueller Filterung Wahlweise-Teile vorhanden sind
        if (iPartsWWPartsHelper.hasWWParts(iPartsEntry, isFilterActive)) {
            String pathName = iPartsConst.CONFIG_KEY_RELATED_INFO_WW_PARTS_DATA;
            if (isEditMode) {
                pathName = RelatedInfoSingleEditHelper.getActiveRelatedInfo(iPartsEntry.getEtkProject(), pathName);
            }

            AssemblyListCellContentFromPlugin iconWW = new AssemblyListCellContentFromPlugin(pathName,
                                                                                             EditDefaultImages.edit_ww_parts.getImage());
            iconWW.setHint(TranslationHandler.translate(RELATED_INFO_WW_PARTS_DATA_TEXT));
            iconWW.setCursor(DWCursor.Hand);
            iconWW.setIconType(PartsListIconOrder.PartsListIconType.itPlugin);
            return iconWW;
        }
        return null;
    }

    public static String buildKeyValue(EtkDisplayField displayField, EtkDataPartListEntry partListEntry, String language, EtkDisplayField nextDisplayField) {
        String result = displayField.getText().getText(language) + ": " + partListEntry.getDisplayValue(displayField.getKey().getFieldName(), language);
        if (nextDisplayField != null) {
            result += ", ";
        }
        return result;
    }

    public static String getPartListExtraText(EtkProject project, String matNameKey, EtkDataPartListEntry partListEntry) {
        StringBuilder str = new StringBuilder();
        if (partListEntry != null) {
            String language = project.getViewerLanguage();
            str.append(TranslationHandler.translate(matNameKey, partListEntry.getPart().getDisplayValue(iPartsDataVirtualFieldsDefinition.DA_MAPPED_MATNR, language)));
            EtkDisplayField posField = partListEntry.getOwnerAssembly().getEbene().getFeldByName(TABLE_KATALOG, FIELD_K_POS);
            EtkDisplayField lfdNrField = partListEntry.getOwnerAssembly().getEbene().getFeldByName(TABLE_KATALOG, FIELD_K_LFDNR);
            if ((posField != null) || (lfdNrField != null)) {
                str.append(" (");
            }
            if (posField != null) {
                str.append(buildKeyValue(posField, partListEntry, language, lfdNrField));
            }
            if (lfdNrField != null) {
                str.append(buildKeyValue(lfdNrField, partListEntry, language, null));
            }
            if ((posField != null) || (lfdNrField != null)) {
                str.append(")");
            }
        } else {
            str.append(TranslationHandler.translate(matNameKey, ""));
        }
        return str.toString();
    }

    /**
     * Erzeugt ein neues RelatedInfoForm für die Anzeige der SAA/BK-Gültigkeiten zu einem EDS-Baumuster.
     *
     * @param dataConnector
     * @param parentForm
     * @param relatedInfo
     */
    public iPartsRelatedInfoWWPartsDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                            IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo, CONFIG_KEY_WW_PARTS_DATA, null);
        originalWWParts = null;
        wwSetMap = new HashMap<>();
        extraWWOfPartListEntryMap = new HashMap<>();
    }

    private void reloadData() {
        if (suppressReloadData) {
            return;
        }

        title = null;
        partListEntry = getConnector().getRelatedInfoData().getAsPartListEntry(getProject());
        if (partListEntry != null) {
            // Im Edit muss der Stücklisteneintrag aus einer neuen Assembly genommen werden, da dieser manipuliert wird
            // und es Änderungen in aktiven ChangeSets geben könnte im Vergleich zum Stücklisteneintrag von der RelatedInfoData
            if (isEditRelatedInfo) {
                EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getProject(), partListEntry.getOwnerAssemblyId());
                partListEntry = assembly.getPartListEntryFromKLfdNrUnfiltered(partListEntry.getAsId().getKLfdnr());
            }
            if (partListEntry != null) {
                title = getPartListExtraText(getProject(), "!!zu Materialnummer \"%1\"", partListEntry);
            }
        }
        setDataObjectGridTitle();
        originalWWParts = null;
        wwSetMap.clear();
        dataToGrid();
    }

    @Override
    public void dataChanged() {
        super.dataChanged();

        if (getConnector().getEditContext() instanceof iPartsRelatedInfoEditContext) {
            if (((iPartsRelatedInfoEditContext)getConnector().getEditContext()).isUpdateWWParts()) {
                reloadData();
            }
        }
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        PartListEntryId currentPartListEntryId = getConnector().getRelatedInfoData().getAsPartListEntryId();
        if (forceUpdateAll || ((getConnector().getActiveRelatedSubForm() == this) && !Utils.objectEquals(currentPartListEntryId, loadedPartListEntryId))) {
            if (Utils.objectEquals(currentPartListEntryId, loadedPartListEntryId)) {
                if (!suppressReloadData) { // StackOverflow durch rekursiven Aufruf von hideRelatedInfoCalled() vermeiden
                    // Aktuelle Änderungen sichern, da reloadData() auch wwSetMap zurücksetzt und alle bisherigen Änderungen
                    // dadurch verloren gehen würden
                    suppressReloadData = true; // doppeltes reloadData() vermeiden
                    try {
                        hideRelatedInfoCalled();
                    } finally {
                        suppressReloadData = false;
                    }
                }
            } else {
                loadedPartListEntryId = currentPartListEntryId;
            }
            reloadData();
        }
    }

    @Override
    protected boolean hideRelatedInfoCalled() {
        if (!getConnector().isEditContext()) {
            return true;
        }

        if ((getConnector().getActiveRelatedSubForm() == this) || (getConnector().getActiveRelatedSubForm() == getParentForm())) {
            if (partListEntry == null) {
                return true;
            }
            iPartsDataPartListEntry iPartsListEntry = (iPartsDataPartListEntry)partListEntry;
            boolean isModified = false;
            iPartsDataAssembly assembly = iPartsListEntry.getOwnerAssembly();
            for (Map.Entry<PartListEntryId, String> entry : wwSetMap.entrySet()) {
                EtkDataPartListEntry newPartListEntry = assembly.getPartListEntryFromKLfdNrUnfiltered(entry.getKey().getKLfdnr());
                if (!getWWSetNo(newPartListEntry).equals(entry.getValue())) {
                    isModified = true;
                    break;
                }
            }

            GenericEtkDataObjectList changeSetDataObjectList = new GenericEtkDataObjectList();
            if (isModified) {
                setModifiedByEdit(true);
                String currentWWSetNo = wwSetMap.get(iPartsListEntry.getAsId());
                originalWWParts.clear();
                originalWWPartIds = null;
                for (Map.Entry<PartListEntryId, String> entry : wwSetMap.entrySet()) {
                    EtkDataPartListEntry newPartListEntry = assembly.getPartListEntryFromKLfdNrUnfiltered(entry.getKey().getKLfdnr());
                    setWWSetNo(newPartListEntry, entry.getValue());
                    if (iPartsListEntry.getAsId() == entry.getKey()) {
                        // Flag "Automatisch erzeugt" muss bei Edit-Aktionen zurückgesetzt werden, aber nur beim selektierten Stücklisteneintrag
                        iPartsDataPartListEntry.resetAutoCreatedFlag(newPartListEntry);
                    }
                    changeSetDataObjectList.add(newPartListEntry, DBActionOrigin.FROM_EDIT);
                    // originalWWParts neu besetzen
                    if (!currentWWSetNo.isEmpty() && currentWWSetNo.equals(entry.getValue()) &&
                        !newPartListEntry.getAsId().equals(iPartsListEntry.getAsId())) {
                        originalWWParts.add(newPartListEntry);
                    }
                }
                if (originalWWParts.isEmpty()) {
                    // das WW-Set besteht nur noch aus dem selektierten Eintrag => löschen
                    EtkDataPartListEntry newPartListEntry = assembly.getPartListEntryFromKLfdNrUnfiltered(iPartsListEntry.getAsId().getKLfdnr());
                    setWWSetNo(newPartListEntry, "");
                }
            }

            // Haben sich Extra-Wahlweise geändert
            Collection<EtkDataPartListEntry> extraWWPartNumbers = iPartsWWPartsHelper.getExtraWWParts(iPartsListEntry, isFilterActive);
            Collection<EtkDataPartListEntry> extraWWOfPartListEntrySetValues = extraWWOfPartListEntryMap.get(loadedPartListEntryId);
            // Gibt es weniger WW in der lokalen Map, als in der DB dann wurden WW gelöscht -> speichern
            if (extraWWOfPartListEntrySetValues.size() != extraWWPartNumbers.size()) {
                List<String> extraWWMatNrs = new ArrayList<>();
                for (EtkDataPartListEntry extraWWPart : extraWWOfPartListEntrySetValues) {
                    extraWWMatNrs.add(extraWWPart.getFieldValue(FIELD_K_MATNR));
                }
                EtkDataPartListEntry newPartListEntry = assembly.getPartListEntryFromKLfdNrUnfiltered(iPartsListEntry.getAsId().getKLfdnr());
                newPartListEntry.setFieldValue(iPartsConst.FIELD_K_WW_EXTRA_PARTS, StrUtils.stringListToString(extraWWMatNrs, ","), DBActionOrigin.FROM_EDIT);
                // Flag "Automatisch erzeugt" muss bei Edit-Aktionen zurückgesetzt werden
                iPartsDataPartListEntry.resetAutoCreatedFlag(newPartListEntry);
                changeSetDataObjectList.add(newPartListEntry, DBActionOrigin.FROM_EDIT);
            }

            if (changeSetDataObjectList.isModifiedWithChildren()) {
                addDataObjectListToActiveChangeSetForEdit(changeSetDataObjectList);

                // Assembly (für das aktive ChangeSet) aus dem Cache entfernen
                EtkDataAssembly.removeDataAssemblyFromCache(getProject(), assembly.getAsId());

                // DataChangedEvent ist hier notwendig, damit eine evtl. geöffnete AS-Stückliste im Katalog auch aktualisiert wird
                if (getConnector().getEditContext() instanceof iPartsRelatedInfoEditContext) {
                    iPartsRelatedInfoEditContext editContext = (iPartsRelatedInfoEditContext)getConnector().getEditContext();
                    editContext.setUpdateWWParts(true);

                    // Edit-Stückliste updaten
                    editContext.setUpdateEditAssemblyData(true);
                    editContext.setUpdateModuleMasterData(true);
                    editContext.setFireDataChangedEvent(true);

                    // RelatedInfo-Daten updaten
                    // reloadData() wird indirekt durch dataChanged() aufgrund des gesetzten Flags updateWWParts im EditContext aufgerufen
                    getConnector().dataChanged(null);
                } else {
                    getProject().fireProjectEvent(new DataChangedEvent(null), true);
                }
            }
        }
        return true;
    }

    @Override
    public boolean hasElementsToShow() {
        return grid.getTable().getRowCount() > 0;
    }


    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields() {
        List<EtkDisplayField> defaultDisplayFields = new ArrayList<EtkDisplayField>();
        EtkDisplayField displayField = new EtkDisplayField(TABLE_MAT, iPartsDataVirtualFieldsDefinition.DA_MAPPED_MATNR, false, false);
        displayField.setColumnFilterEnabled(true);
        displayField.setDefaultText(false); // ansonsten würde ein hier gesetzter Text überschrieben weil weiter unten loadStandards() aufgerufen wird
        displayField.setText(new EtkMultiSprache("!!Teilenummer", new String[]{ TranslationHandler.getUiLanguage() }));
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(TABLE_MAT, FIELD_M_TEXTNR, true, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT, false, false);
        defaultDisplayFields.add(displayField);

        return defaultDisplayFields;
    }

    @Override
    protected EtkDisplayFields getDisplayFields() {
        EtkDisplayFields displayFields = super.getDisplayFields();
        if (!displayFields.contains(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DA_FILTER_REASON_FILTERED, false)) {
            // virtuelles Feld für die Anzeige der Extra-Wahlweise Teile benützen
            EtkDisplayField displayField = new EtkDisplayField(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DA_FILTER_REASON_FILTERED, false, false);
            displayField.loadStandards(getProject().getConfig());
            displayField.setDefaultWidth(false);
            displayField.setWidth(10);
            displayField.setColumnFilterEnabled(true);
            displayField.setDefaultText(false); // ansonsten würde ein hier gesetzter Text überschrieben weil weiter unten loadStandards() aufgerufen wird
            displayField.setText(new EtkMultiSprache("!!Extra-Wahlweise", new String[]{ TranslationHandler.getUiLanguage() }));
            displayField.setInRelatedInfoAnzeigen(true);
            displayFields.addFeld(displayField);
        }
        return displayFields;
    }

    @Override
    protected DBDataObjectList<? extends EtkDataObject> createDataObjectList() {
        EtkDataPartListEntryList list = new EtkDataPartListEntryList();
        if (loadedPartListEntryId != null) {
            if (partListEntry instanceof iPartsDataPartListEntry) {
                isFilterActive = !isEditRelatedInfo && iPartsRelatedInfoFormConnectorWithFilterSettings.isFilterActive(getConnector());
                iPartsDataPartListEntry iPartsDataPartListEntry = (iPartsDataPartListEntry)partListEntry;
                // damit alle Caches gesetzt sind
                iPartsDataPartListEntry.getOwnerAssembly().getPartListUnfiltered(iPartsDataPartListEntry.getOwnerAssembly().getEbene());
                if (wwSetMap.get(iPartsDataPartListEntry.getAsId()) == null) {
                    wwSetMap.put(iPartsDataPartListEntry.getAsId(), getWWSetNo(iPartsDataPartListEntry));
                }
                List<PartListEntryId> doneList = new DwList<PartListEntryId>();
                if (originalWWParts == null) {
                    // Alle Wahlweise-Teile zu diesem Eintrag holen
                    originalWWParts = iPartsWWPartsHelper.getRealWWParts(iPartsDataPartListEntry, isFilterActive);
                }
                if (!StrUtils.isEmpty(wwSetMap.get(iPartsDataPartListEntry.getAsId()))) {
                    String loadedWWSetNo = wwSetMap.get(partListEntry.getAsId());
                    for (EtkDataPartListEntry wwPart : originalWWParts) {
                        String wwSetNo = wwSetMap.get(wwPart.getAsId());
                        if (wwSetNo == null) {
                            String currentWWSetNo = getWWSetNo(wwPart);
                            if (StrUtils.isValid(loadedWWSetNo, currentWWSetNo) && loadedWWSetNo.equals(currentWWSetNo)) {
                                wwPart.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DA_FILTER_REASON_FILTERED, SQLStringConvert.booleanToPPString(false),
                                                                true, DBActionOrigin.FROM_DB);
                                wwSetMap.put(wwPart.getAsId(), getWWSetNo(wwPart));
                                list.add(wwPart, DBActionOrigin.FROM_DB);
                                doneList.add(wwPart.getAsId());
                            }
                        } else {
                            if (!wwSetNo.isEmpty()) {
                                wwPart.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DA_FILTER_REASON_FILTERED, SQLStringConvert.booleanToPPString(false),
                                                                true, DBActionOrigin.FROM_DB);
                                list.add(wwPart, DBActionOrigin.FROM_DB);
                            }
                            doneList.add(wwPart.getAsId());
                        }
                    }
                    boolean doSort = false;
                    for (Map.Entry<PartListEntryId, String> entry : wwSetMap.entrySet()) {
                        if (!doneList.contains(entry.getKey()) && !entry.getKey().equals(iPartsDataPartListEntry.getAsId()) &&
                            !StrUtils.isEmpty(entry.getValue())) {
                            EtkDataPartListEntry newPartListEntry = iPartsDataPartListEntry.getOwnerAssembly().getPartListEntryFromKLfdNrUnfiltered(entry.getKey().getKLfdnr());
                            newPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DA_FILTER_REASON_FILTERED, SQLStringConvert.booleanToPPString(false),
                                                                      true, DBActionOrigin.FROM_DB);
                            list.add(newPartListEntry, DBActionOrigin.FROM_DB);
                            doSort = true;
                        }
                    }
                    sortResultList(list, doSort);
                }

                if (extraWWOfPartListEntryMap.get(loadedPartListEntryId) == null) {
                    Collection<EtkDataPartListEntry> wwParts = iPartsWWPartsHelper.getExtraWWParts(iPartsDataPartListEntry, isFilterActive);
                    for (EtkDataPartListEntry wwPart : wwParts) {
                        wwPart.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DA_FILTER_REASON_FILTERED, SQLStringConvert.booleanToPPString(true),
                                                        true, DBActionOrigin.FROM_DB);
                    }
                    extraWWOfPartListEntryMap.put(loadedPartListEntryId, wwParts);
                }
                list.addAll(extraWWOfPartListEntryMap.get(loadedPartListEntryId), DBActionOrigin.FROM_DB);
            }
        }
        doEnableButtons();
        return list;
    }

    private String getWWSetNo(EtkDataPartListEntry partListEntry) {
        return partListEntry.getFieldValue(FIELD_K_WW);
    }

    private void setWWSetNo(EtkDataPartListEntry partListEntry, String wwSetNo) {
        partListEntry.setFieldValue(FIELD_K_WW, wwSetNo, DBActionOrigin.FROM_EDIT);
    }

    private List<PartListEntryId> getOriginalWWPartIdList() {
        if (originalWWPartIds == null) {
            originalWWPartIds = new DwList<PartListEntryId>();
            if (originalWWParts != null) {
                for (EtkDataPartListEntry wwPartListEntry : originalWWParts) {
                    originalWWPartIds.add(wwPartListEntry.getAsId());
                }
            }
        }
        return originalWWPartIds;
    }

    private void sortResultList(EtkDataPartListEntryList list, boolean doSort) {
        if (doSort) {
            List<EtkDataPartListEntry> sortList = new DwList<EtkDataPartListEntry>(list.getAsList());
            Collections.sort(sortList, new Comparator<EtkDataPartListEntry>() {
                @Override
                public int compare(EtkDataPartListEntry o1, EtkDataPartListEntry o2) {
                    return o1.getAsId().getKLfdnr().compareTo(o2.getAsId().getKLfdnr());
                }
            });
            list.clear(DBActionOrigin.FROM_DB);
            list.addAll(sortList, DBActionOrigin.FROM_DB);
        }
    }

    @Override
    protected void postCreateGui() {
        isEditRelatedInfo = (getConnector().getActiveForm() instanceof EditModuleForm) &&
                            AbstractRelatedInfoPartlistDataForm.isEditContext(getConnector(), true);
        if (isEditRelatedInfo) {
            setDataObjectGridTitle();
            grid = createGrid();
            grid.setPageSplitNumberOfEntriesPerPage(iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE);
            ConstraintsBorder constraints = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
            grid.getGui().setConstraints(constraints);
            getPanelDataObjectGrid().addChild(grid.getGui());
        } else {
            super.postCreateGui();
        }
        doEnableButtons();
    }

    @Override
    protected DataObjectFilterGrid createGrid() {
        DataObjectFilterGrid dataGrid = new DataObjectFilterGrid(getConnector(), this) {
            @Override
            protected void createToolbarButtons(GuiToolbar toolbar) {
                if (isEditRelatedInfo) {
                    ToolbarButtonMenuHelper.ToolbarMenuHolder holder;
                    holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_NEW, getUITranslationHandler(), new MenuRunnable() {
                        @Override
                        public void run(Event event) {
                            doAdd(event);
                        }
                    });
                    getContextMenu().addChild(holder.menuItem);
                    holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_DELETE, getUITranslationHandler(), new MenuRunnable() {
                        @Override
                        public void run(Event event) {
                            doRemove(event);
                        }
                    });
                    getContextMenu().addChild(holder.menuItem);
                    holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_DELETE_OPTIONAL_PART, getUITranslationHandler(),
                                                                         new MenuRunnable() {
                                                                             @Override
                                                                             public void run(Event event) {
                                                                                 doRemoveSelf(event);
                                                                             }
                                                                         });
                    getContextMenu().addChild(holder.menuItem);
                }
            }

            @Override
            protected void createContextMenuItems(GuiContextMenu contextMenu) {
                super.createContextMenuItems(contextMenu);

                // Popup-Menüeintrag für "Gehe zu"
                GuiMenuItem gotoMenuItem = new GuiMenuItem();
                gotoMenuItem.setText("!!Gehe zu Wahlweise-Teil");
                contextMenu.addChild(gotoMenuItem);

                gotoMenuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                    @Override
                    public void fire(Event event) {
                        gotoSelectedWWPartListEntry();
                    }
                });
            }

            @Override
            protected void onTableSelectionChanged(Event event) {
                doEnableButtons();
            }

        };

        // Doppelklick für "Gehe zu"
        dataGrid.getTable().addEventListener(new EventListener(Event.MOUSE_DOUBLECLICKED_EVENT) {
            @Override
            public void fire(Event event) {
                gotoSelectedWWPartListEntry();
            }
        });

        return dataGrid;
    }

    private void doEnableButtons() {
        if (isEditRelatedInfo) {
            List<EtkDataObject> selectedList = grid.getSelection();
            boolean isSingleSelected = grid.isSingleSelected();
            boolean isMultiSelected = grid.isMultiSelected();
            grid.showToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW);
            if (wwSetMap != null) {
                grid.changeToolbarButtonAndMenuTooltip(EditToolbarButtonAlias.EDIT_NEW,
                                                       TranslationHandler.translate((wwSetMap.size() == 1) ? EditToolbarButtonAlias.EDIT_NEW.getTooltip() : "!!Bearbeiten"));
            }
            if (isSingleSelected || isMultiSelected) {
                boolean onlyExtraWWSelected = true;
                for (EtkDataObject dataObject : selectedList) {
                    if (!dataObject.getAttribute(iPartsDataVirtualFieldsDefinition.DA_FILTER_REASON_FILTERED).getAsBoolean()) {
                        onlyExtraWWSelected = false;
                        break;
                    }
                }

                boolean isEnableDeleteButton = !onlyExtraWWSelected;
                if (onlyExtraWWSelected) {
                    EtkDataAssembly etkDataAssembly = partListEntry.getOwnerAssembly();
                    if (etkDataAssembly instanceof iPartsDataAssembly) {
                        iPartsDataAssembly iPartsDataAssembly = (iPartsDataAssembly)etkDataAssembly;
                        // TRUCK-Autoren sollen Extra-Wahlweise löschen können
                        // Extra-Wahlweise kommt nur bei TRUCk vor. Nur zur Sicherheit abfragen
                        isEnableDeleteButton = iPartsDataAssembly.getDocumentationType().isTruckDocumentationType();
                    }
                }
                grid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, isEnableDeleteButton);
                AbstractGuiToolComponent component = grid.getToolbarManager().getButton(EditToolbarButtonAlias.EDIT_DELETE.getAlias());
                if (component != null) {
                    component.setTooltip(TranslationHandler.translate(!isEnableDeleteButton ? "!!Nur im Stücklisten-Editor löschbar" : EditToolbarButtonAlias.EDIT_DELETE.getTooltip()));
                }
            } else {
                grid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, false);
            }
            boolean isEnabled = true;
            if (wwSetMap != null) {
                isEnabled = wwSetMap.size() > 1;
                if (isEnabled) {
                    isEnabled = !StrUtils.isEmpty(wwSetMap.get(partListEntry.getAsId()));
                }
            }
            grid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE_OPTIONAL_PART, isEnabled);
        }
    }

    private void gotoSelectedWWPartListEntry() {
        List<EtkDataObject> selection = grid.getSelection();
        if (selection != null) {
            EtkDataPartListEntry selectedWWPartListEntry = (EtkDataPartListEntry)selection.get(0);
            if (selectedWWPartListEntry.getAsId().getKLfdnr().equals(iPartsWWPartsHelper.K_LFDNR_NOT_IN_MODULE)) { // Dummy-Stücklisteneintrag -> kein Sprung möglich
                MessageDialog.show(TranslationHandler.translate("!!Das Wahlweise-Teil \"%1\" ist in der aktuellen Stückliste nicht vorhanden.",
                                                                selectedWWPartListEntry.getDisplayValue(FIELD_K_MATNR, getProject().getDBLanguage())),
                                   "!!Gehe zu Wahlweise-Teil");
            } else { // Sprung zum Wahlweise-Stücklisteneintrag
                NavigationPath path = this.getConnector().getRelatedInfoData().getNavigationPath();
                if (path.isEmpty()) {
                    path = new NavigationPath(); // Neuen Pfad erzeugen, damit der Pfad aus der RelatedInfoData nicht verändert wird
                    path.addAssembly(selectedWWPartListEntry.getOwnerAssemblyId());
                }
                if (!closeRelatedInfoFormIfNotEmbedded()) { // Erst schließen, dann springen
                    return;
                }

                // befinden wir uns im Edit Context? (Mit oder ohne aktiven Autorenauftrag) (isEditRelatedInfo ist ohne aktiven Autorenauftrag false)
                if (AbstractRelatedInfoPartlistDataForm.isEditContext(getConnector(), false)) {
                    GotoEditPartWithPartialPathEvent partWithPartialPathEvent = new GotoEditPartWithPartialPathEvent(path,
                                                                                                                     selectedWWPartListEntry.getOwnerAssemblyId(),
                                                                                                                     selectedWWPartListEntry.getAsId().getKLfdnr(),
                                                                                                                     false, false, this);
                    getProject().fireProjectEvent(partWithPartialPathEvent);
                } else {
                    GotoPartWithPartialPathEvent partWithPartialPathEvent = new GotoPartWithPartialPathEvent(path,
                                                                                                             selectedWWPartListEntry.getOwnerAssemblyId(),
                                                                                                             selectedWWPartListEntry.getAsId().getKLfdnr(),
                                                                                                             false, false, this);
                    getProject().fireProjectEvent(partWithPartialPathEvent);
                }
            }
        }
    }

    private void doAdd(Event event) {
        iPartsDataPartListEntry iPartsListEntry = (iPartsDataPartListEntry)partListEntry;
        List<EtkDataPartListEntry> wwList = new DwList<EtkDataPartListEntry>();
        List<PartListEntryId> doneList = new DwList<PartListEntryId>();
        wwList.add(iPartsListEntry);
        if (!StrUtils.isEmpty(wwSetMap.get(iPartsListEntry.getAsId()))) {
            for (EtkDataPartListEntry wwPartListEntry : originalWWParts) {
                String wwSetNo = wwSetMap.get(wwPartListEntry.getAsId());
                if (!StrUtils.isEmpty(wwSetNo)) {
                    wwList.add(wwPartListEntry);
                    doneList.add(wwPartListEntry.getAsId());
                } else {
                    if (wwSetNo != null) {
                        doneList.add(wwPartListEntry.getAsId());
                    }
                }
            }
            for (Map.Entry<PartListEntryId, String> entry : wwSetMap.entrySet()) {
                if (!doneList.contains(entry.getKey()) && !entry.getKey().equals(iPartsListEntry.getAsId()) &&
                    !StrUtils.isEmpty(entry.getValue())) {
                    EtkDataPartListEntry newPartListEntry = iPartsListEntry.getOwnerAssembly().getPartListEntryFromKLfdNrUnfiltered(entry.getKey().getKLfdnr());
                    wwList.add(newPartListEntry);
                }
            }
        }
        EditModuleFormIConnector editConnector = iPartsRelatedEditHelper.createEditConnector(getConnector(), iPartsListEntry.getOwnerAssembly());
        Map<PartListEntryId, String> wwSetResultMap = EditWWToPartlistEntryForm.showWWToPartlistForm(editConnector, this,
                                                                                                     wwList, iPartsListEntry);
        if (editConnector != getConnector()) {
            editConnector.dispose();
        }
        if (wwSetResultMap != null) {
            doneList.clear();
            if ((wwSetResultMap.size() == 1) && (wwSetResultMap.get(iPartsListEntry.getAsId()) != null)) {
                wwSetMap.clear();
                for (PartListEntryId partListEntryId : getOriginalWWPartIdList()) {
                    wwSetMap.put(partListEntryId, "");
                }
            } else {
                for (Map.Entry<PartListEntryId, String> entry : wwSetMap.entrySet()) {
                    if (wwSetResultMap.containsKey(entry.getKey())) {
                        String wwSetNo = wwSetResultMap.get(entry.getKey());
                        wwSetMap.put(entry.getKey(), wwSetNo);
                        wwSetResultMap.remove(entry.getKey());
                        doneList.add(entry.getKey());
                    } else {
                        wwSetMap.put(entry.getKey(), "");
                    }
                }
                for (Map.Entry<PartListEntryId, String> entry : wwSetResultMap.entrySet()) {
                    wwSetMap.put(entry.getKey(), entry.getValue());
                    doneList.add(entry.getKey());
                }
                List<PartListEntryId> deleteList = new DwList<PartListEntryId>();
                for (Map.Entry<PartListEntryId, String> entry : wwSetMap.entrySet()) {
                    if (!doneList.contains(entry.getKey()) && !getOriginalWWPartIdList().contains(entry.getKey())) {
                        deleteList.add(entry.getKey());
                    }
                }
                for (PartListEntryId partListEntryId : deleteList) {
                    wwSetMap.remove(partListEntryId);
                }
            }
            dataToGrid();
            iPartsRelatedInfoSuperEditDataForm.refreshSuperEdit(this);
        }
    }

    private void doRemove(Event event) {
        List<List<EtkDataObject>> selection = grid.getMultiSelection();
        if (selection != null) {
            for (List<EtkDataObject> dataObjectList : selection) {
                for (EtkDataObject dataObject : dataObjectList) {
                    if (dataObject.getAsId() instanceof PartListEntryId) {
                        PartListEntryId partListEntryId = (PartListEntryId)dataObject.getAsId();
                        String wwSetNo = wwSetMap.get(partListEntryId);
                        if (wwSetNo != null) {
                            if (getOriginalWWPartIdList().contains(partListEntryId)) {
                                wwSetMap.put((PartListEntryId)dataObject.getAsId(), "");
                            } else {
                                wwSetMap.remove(partListEntryId);
                            }
                        } else {
                            // Ist Selektion ein Extra-Wahlweise
                            if (dataObject.getFieldValueAsBoolean(iPartsDataVirtualFieldsDefinition.DA_FILTER_REASON_FILTERED)) {
                                Collection<EtkDataPartListEntry> extraWWOfPartListEntry = extraWWOfPartListEntryMap.get(loadedPartListEntryId);
                                if (dataObject instanceof EtkDataPartListEntry) {
                                    extraWWOfPartListEntry.remove(dataObject);
                                }
                            }
                        }
                    }
                }
            }
            if (wwSetMap.size() == 1) {
                wwSetMap.put(partListEntry.getAsId(), "");
            }
            dataToGrid();
            iPartsRelatedInfoSuperEditDataForm.refreshSuperEdit(this);
        }
    }

    private void doRemoveSelf(Event event) {
        String wwSetNo = wwSetMap.get(partListEntry.getAsId());
        wwSetMap.put(partListEntry.getAsId(), "");
        setWWSetNo(partListEntry, "");
        if (StrUtils.isValid(wwSetNo)) {
            List<PartListEntryId> toDeleteList = new DwList<>();
            for (Map.Entry<PartListEntryId, String> entry : wwSetMap.entrySet()) {
                if (entry.getValue().equals(wwSetNo)) {
                    toDeleteList.add(entry.getKey());
                }
            }
            for (PartListEntryId partListEntryId : toDeleteList) {
                wwSetMap.remove(partListEntryId);
            }
        }
        if (originalWWParts.size() == 1) {
            // falls vom WW-Set nur noch einer übrig ist => diesen auch zurücksetzen
            wwSetMap.put(getOriginalWWPartIdList().get(0), "");
        }
        dataToGrid();
        iPartsRelatedInfoSuperEditDataForm.refreshSuperEdit(this);
    }

    @Override
    public int calculateOptimalHeight() {
        int titleAndBorderHeight = HTMLUtils.getTextDimension(getLabelDataObjectGridTitle().getFont(), "Wg").getHeight()
                                   + (DWLayoutManager.get().isResponsiveMode() ? 16 : 10);
        int optimalHeight = titleAndBorderHeight + getGrid().getToolBar().getPreferredHeight() + 6 + getGrid().getTable().getPreferredHeight()
                            + ((getGrid().getTable().getRowCount() == 0) ? titleAndBorderHeight : 0);
        return optimalHeight;
    }
}