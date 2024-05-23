package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.DataObjectFilterGrid;
import de.docware.apps.etk.base.forms.events.OnChangeEvent;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.AbstractSimpleDataObjectGridForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditFilterDateGuiHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditFilterDateObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.ChangeSetShowTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditDataObjectFilterGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.*;
import de.docware.framework.modules.db.etkrecord.EtkRecord;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerFireOnce;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.terms.AbstractCondition;
import de.docware.util.sql.terms.Condition;
import de.docware.util.sql.terms.ConditionList;
import de.docware.util.sql.terms.Fields;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShowTechnicalChangeSetForm extends AbstractSimpleDataObjectGridForm implements iPartsConst {

    private static final String SESSION_KEY_TECH_CHANGESET_FILTER_DATE = "sessionKeyTechChangeSetFilterDate";

    private iPartsDataChangeSetList dataChangeSetList;
    private iPartsDataChangeSetEntryList dataChangeSetEntryList;
    private boolean showDataObjectId;
    private ChangeSetShowTypes changeSetShowTypes;

    public static void showTechnicalChangeSets(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        ShowTechnicalChangeSetForm dlg = new ShowTechnicalChangeSetForm(dataConnector, parentForm, "!!Anzeige",
                                                                        "!!Anzeige Technische Änderungssets");
        dlg.showModal();
    }

    protected ShowTechnicalChangeSetForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                         String dataObjectGridTitle, String title) {
        super(dataConnector, parentForm, "", dataObjectGridTitle, "", title);
        this.dataChangeSetList = new iPartsDataChangeSetList();
        this.dataChangeSetEntryList = new iPartsDataChangeSetEntryList();
        this.showDataObjectId = true;
        this.changeSetShowTypes = new ChangeSetShowTypes(dataConnector.getProject());
        grid.setNoResultsLabelText("!!Suche läuft...");
        grid.getTable().addEventListener(new EventListener(Event.TABLE_COLUMN_FILTER_CHANGED_EVENT) {
            @Override
            public void fire(Event event) {
                showResultCount();
            }
        });
        OnChangeEvent changeEvent = () -> doSearch();
        GuiPanel filterDatePanel = EditFilterDateGuiHelper.createDefaultFilterDatePanelWithSessionValuesForCurrentMonth(SESSION_KEY_TECH_CHANGESET_FILTER_DATE, changeEvent, changeEvent);
        addExtraPanelToNorthPanel(filterDatePanel);

        dataToGrid();
        getButtonPanel().getButtonOnPanel(GuiButtonOnPanel.ButtonType.CLOSE).requestFocus();

        // Da die Suche längern dauern könnte, nicht direkt im Konstruktor anstoßen (oder mit OPENED-Event), sondern
        // wenn die GUI aufgebaut wird im ON_RESIZE)
        getGui().addEventListener(new EventListenerFireOnce(Event.ON_RESIZE_EVENT) {
            @Override
            public void fireOnce(Event event) {
                grid.setNoResultsLabelText("!!Keine Elemente gefunden...");
                doSearch();
            }
        });
    }

    private void doSearch() {
        EditFilterDateObject filterDateObject = EditFilterDateGuiHelper.getDateFilterObjectFromSession(SESSION_KEY_TECH_CHANGESET_FILTER_DATE);
        if (filterDateObject != null) {
            doSearch(filterDateObject.getDateFromForDbSearch(), filterDateObject.getDateToForDbSearch());
            return;
        }
        dataChangeSetList.clear(DBActionOrigin.FROM_DB);
        dataChangeSetEntryList.clear(DBActionOrigin.FROM_DB);

        String[] whereFields = new String[]{ FIELD_DCS_STATUS };
        String[] whereValues = new String[]{ iPartsChangeSetStatus.COMMITTED.name() };
        String[] whereNotFields = new String[]{ FIELD_DCS_SOURCE };
        String[] whereNotValues = new String[]{ iPartsChangeSetSource.AUTHOR_ORDER.name() };
        String[] sortFields = new String[]{ FIELD_DCS_COMMIT_DATE };

        dataChangeSetList.searchSortAndFill(getProject(), TABLE_DA_CHANGE_SET, null, whereFields, whereValues,
                                            whereNotFields, whereNotValues, sortFields, DBDataObjectList.LoadType.COMPLETE,
                                            true, DBActionOrigin.FROM_DB);
        if (showDataObjectId) {
            for (iPartsDataChangeSet dataChangeSet : dataChangeSetList) {
                calcDataObjectId(dataChangeSet);
            }
        }
        dataToGrid();
    }

    private void doSearch(String dateFrom, String dateTo) {
        dataChangeSetList.clear(DBActionOrigin.FROM_DB);
        dataChangeSetEntryList.clear(DBActionOrigin.FROM_DB);

        // Alle Spalten aus DA_AUTHOR_ORDER bestimmen
        EtkDatabaseTable tableDef = getProject().getConfig().getDBDescription().findTable(TABLE_DA_CHANGE_SET);
        if (tableDef == null) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "No table definition found for " + TABLE_DA_CHANGE_SET);
            dataToGrid();
            return;
        }
        Set<String> selectFields = new HashSet<>(tableDef.getAllFieldsNoBlob());
        selectFields.remove(FIELD_STAMP);

        DBSQLQuery query = getProject().getEtkDbs().getDBForDomain(DBDatabaseDomain.MAIN).getNewQuery();
        query.select(new Fields(selectFields)).from(TABLE_DA_CHANGE_SET);

        List<AbstractCondition> conditions = new ArrayList<>();
        conditions.add(new Condition(FIELD_DCS_STATUS, Condition.OPERATOR_EQUALS, iPartsChangeSetStatus.COMMITTED.name()));
        conditions.add(new Condition(FIELD_DCS_SOURCE, Condition.OPERATOR_NOT_EQUALS, iPartsChangeSetSource.AUTHOR_ORDER.name()));
        // Condition sieht so aus: FIELD_DAO_RELDATE between 'dateFrom' and 'dateTo'
        // Das 'and' wird durch die Aneindanderreihung der Conditions hinzugefügt
        conditions.add(new Condition(FIELD_DCS_COMMIT_DATE, "between", dateFrom));
        conditions.add(new Condition("", "", dateTo));

        ConditionList whereConditions = new ConditionList(conditions);
        query.where(whereConditions);
        query.orderByDescending(FIELD_DCS_COMMIT_DATE);

        try (DBDataSet dataSet = query.executeQuery()) {
            if (dataSet != null) {
                while (dataSet.next()) {
                    EtkRecord currentRecord = dataSet.getRecord(selectFields);
                    DBDataObjectAttributes attributes = DBDataObjectAttributes.getFromRecord(currentRecord, DBActionOrigin.FROM_DB);
                    if (attributes != null) {
                        if (showDataObjectId) {
                            calcDataObjectId(attributes);
                        }
                        dataChangeSetList.fillAndAddObjectFromAttributes(getProject(), attributes, DBActionOrigin.FROM_DB);
                    }
                }
            }

        }
        dataToGrid();
    }

    @Override
    protected void dataToGrid() {
        super.dataToGrid();
        showResultCount();
    }

    protected void showResultCount() {
        title = TranslationHandler.translate("!!%1 Datensätze gefunden", String.valueOf(grid.getTable().getRowCount()));
        setDataObjectGridTitle();
    }

    @Override
    protected DataObjectFilterGrid createGrid() {
        return new EditDataObjectFilterGrid(getConnector(), this) {
            private GuiMenuItem showChangeSetMenuItem;

            @Override
            protected String getVisualValueOfField(String tableName, String fieldName, EtkDataObject objectForTable) {
                if (tableName.equals(TABLE_DA_CHANGE_SET_ENTRY)) {
                    if (fieldName.equals(FIELD_DCE_DO_ID)) {
                        String doId = objectForTable.getFieldValue(fieldName);
                        if (StrUtils.isValid(doId)) {
                            String doType = objectForTable.getFieldValue(FIELD_DCE_DO_TYPE);
                            IdWithType id = IdWithType.fromDBString(doType, doId);

                            // ID visualisieren falls möglich
                            String idString;
                            try {
                                idString = changeSetShowTypes.calculateObjectId(ChangeSetShowTypes.SHOW_TYPES.getShowTypeByIdType(doType),
                                                                                id.toStringArrayWithoutType(), null);
                            } catch (Exception e) {
                                idString = null; // Kommt z.B. vor, wenn die Primärschlüssellängen sich geändert haben
                            }
                            if (StrUtils.isValid(idString)) {
                                return idString;
                            } else {
                                return id.toString(", "); // Fallback auf die unformatierte ID
                            }
                        }
                    }
                }
                return super.getVisualValueOfField(tableName, fieldName, objectForTable);
            }

            @Override
            protected void createContextMenuItems(GuiContextMenu contextMenu) {
                EventListener listener = new EventListener(Event.MENU_ITEM_EVENT) {
                    @Override
                    public void fire(Event event) {
                        onCellDblClicked(0, 0, event);
                    }
                };
                GuiMenuItem menuItem = toolbarHelper.createMenuEntry("showChangeSetHistory", "!!Komplette Historie", null, listener, getUITranslationHandler());
                contextMenu.addChild(menuItem);

                listener = new EventListener(Event.MENU_ITEM_EVENT) {
                    @Override
                    public void fire(Event event) {
                        List<IdWithType> selectedIds = getSelectedObjectIds(TABLE_DA_CHANGE_SET);
                        if (selectedIds.size() == 1) {
                            IdWithType id = selectedIds.get(0);
                            if (id.getType().equals(iPartsChangeSetId.TYPE)) {
                                iPartsChangeSetId changeSteId = (iPartsChangeSetId)id;
                                iPartsEditPlugin.showChangeSetContentsInCopyWindow(changeSteId.getGUID(), false, true);
                            }
                        }
                    }
                };
                showChangeSetMenuItem = toolbarHelper.createMenuEntry("showChangeSet", "!!Änderungsset-Inhalt anzeigen...", null, listener, getUITranslationHandler());
                contextMenu.addChild(showChangeSetMenuItem);
            }

            @Override
            protected void onHeaderDblClicked(int col, Event event) {
                // HeaderDblClick wird ignoriert
            }

            @Override
            protected void onCellDblClicked(int row, int col, Event event) {
                List<IdWithType> selectedIds = getSelectedObjectIds(TABLE_DA_CHANGE_SET);
                if (!selectedIds.isEmpty()) {
                    List<iPartsChangeSetId> tcsList = new DwList<>();
                    for (IdWithType id : selectedIds) {
                        if (id.getType().equals(iPartsChangeSetId.TYPE)) {
                            iPartsChangeSetId changeSetId = (iPartsChangeSetId)id;
                            tcsList.add(changeSetId);
                        }
                    }
                    // Anzeige Historie für technisches ChangeSet
                    EditAuthorOrderView.showTechChangeSetHistoryView(getConnector(), this, tcsList);
                }
            }

            @Override
            protected void onTableSelectionChanged(Event event) {
                enableButtons();
            }

            @Override
            protected void enableButtons() {
                List<GuiTableRow> selectedRows = getTable().getSelectedRows();
                boolean isSingleSelect = selectedRows.size() == 1;
                showChangeSetMenuItem.setEnabled(isSingleSelect);
            }
        };
    }

    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields() {
        EtkDisplayFields displayFields;
        if (Constants.DEVELOPMENT && !Constants.DEVELOPMENT_QFTEST) {
            displayFields = iPartsShowDataObjectsDialog.getDisplayFieldsFromTableDef(getProject(), TABLE_DA_CHANGE_SET);
        } else {
            displayFields = new EtkDisplayFields();
            EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_CHANGE_SET, FIELD_DCS_COMMIT_DATE, false, false);
            displayFields.addFeld(displayField);
            displayField = new EtkDisplayField(TABLE_DA_CHANGE_SET, FIELD_DCS_SOURCE, false, false);
            displayFields.addFeld(displayField);
        }
        EtkDisplayField displayField = displayFields.getFeldByName(TABLE_DA_CHANGE_SET, FIELD_DCS_COMMIT_DATE, false);
        if (displayField != null) {
            displayField.setColumnFilterEnabled(true);
        }
        displayField = displayFields.getFeldByName(TABLE_DA_CHANGE_SET, FIELD_DCS_SOURCE, false);
        if (displayField != null) {
            displayField.setColumnFilterEnabled(true);
        }
        if (showDataObjectId) {
            displayField = new EtkDisplayField(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_ID, false, false);
            displayField.setColumnFilterEnabled(true);
            displayFields.addFeld(displayField);
        }
        return displayFields.getFields();
    }

    private void calcDataObjectId(iPartsDataChangeSet dataChangeSet) {
        calcDataObjectId(dataChangeSet.getAttributes());
    }

    /**
     * Den Wert für die Spalte Datenonjekt-ID bestimmen
     *
     * @param attributes
     */
    private void calcDataObjectId(DBDataObjectAttributes attributes) {
        String guid = attributes.getFieldValue(FIELD_DCS_GUID);
        // die 'Master-ID' aus dem TCS-Typ bestimmen
        String doType = getTypeFromSource(attributes.getFieldValue(FIELD_DCS_SOURCE));
        iPartsDataChangeSetEntry dataChangeSetEntry;
        if (StrUtils.isValid(doType)) {
            // Suche in ChangeSetEntries
            dataChangeSetEntry = searchDataObjectId(guid, doType);
        } else {
            // Dummy-Eintrag anlegen
            dataChangeSetEntry = createChangeSetEntry(guid, doType, "");
        }
        dataChangeSetEntryList.add(dataChangeSetEntry, DBActionOrigin.FROM_DB);
    }

    /**
     * Suche in DA_CHANGE_SET_ENTRY nach dem 'Master-Eintrag' zum TCS
     *
     * @param guid
     * @param doType
     * @return
     */
    private iPartsDataChangeSetEntry searchDataObjectId(String guid, String doType) {
        String[] whereFields = new String[]{ FIELD_DCE_GUID, FIELD_DCE_DO_TYPE };
        String[] whereValues = new String[]{ guid, doType };

        EtkRecord rec = getProject().getEtkDbs().getRecord(TABLE_DA_CHANGE_SET_ENTRY, whereFields, whereValues);
        if (rec != null) {
            DBDataObjectAttributes attributes = DBDataObjectAttributes.getFromRecord(rec, DBActionOrigin.FROM_DB);
            iPartsDataChangeSetEntry dataChangeSetEntry = createChangeSetEntry(guid, doType, attributes.getFieldValue(FIELD_DCE_DO_ID));
            dataChangeSetEntry.assignAttributes(getProject(), attributes, false, DBActionOrigin.FROM_DB);
            return dataChangeSetEntry;
        }
        // nichts gefunden => erzeuge Dummy-Eintrag
        return createChangeSetEntry(guid, doType, "");
    }

    private iPartsDataChangeSetEntry createChangeSetEntry(String guid, String doType, String doId) {
        iPartsChangeSetEntryId csId = new iPartsChangeSetEntryId(guid, doType, doId);
        iPartsDataChangeSetEntry dataChangeSetEntry = new iPartsDataChangeSetEntry(getProject(), csId);
        dataChangeSetEntry.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        return dataChangeSetEntry;
    }

    /**
     * Umsetzung des TCS-Typs auf die Master-ID in der Tabelle DA_CHANGE_SET_ENTRY
     *
     * @param source
     * @return
     */
    private String getTypeFromSource(String source) {
        iPartsChangeSetSource changeSetSource = iPartsChangeSetSource.getSourceByDbValue(source);
        if (changeSetSource != null) {
            return changeSetSource.getMasterDataObjectType();
        }
        return "";
    }

    @Override
    protected List<DBDataObjectList<? extends EtkDataObject>> createMultipleDataObjectList() {
        if (showDataObjectId) {
            if (Utils.isValid(dataChangeSetList)) {
                List<DBDataObjectList<? extends EtkDataObject>> dataObjectsAllRows = new ArrayList<>(2);
                dataObjectsAllRows.add(dataChangeSetList);
                dataObjectsAllRows.add(dataChangeSetEntryList);
                // beide DataObjectListen zurückliefern
                return dataObjectsAllRows;
            }
        }
        return null;
    }

    @Override
    protected DBDataObjectList<? extends EtkDataObject> createDataObjectList() {
        if (!showDataObjectId) {
            return dataChangeSetList;
        }
        return null;
    }
}
