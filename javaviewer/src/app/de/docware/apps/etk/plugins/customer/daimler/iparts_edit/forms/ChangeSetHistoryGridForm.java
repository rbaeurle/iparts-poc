package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControlFactory;
import de.docware.apps.etk.base.forms.common.EditCreateMode;
import de.docware.apps.etk.base.forms.common.SimpleSelectSearchResultGrid;
import de.docware.apps.etk.base.forms.events.OnDblClickEvent;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.framework.modules.config.db.EtkFieldType;
import de.docware.framework.modules.config.defaultconfig.system.SystemSettings;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.serialization.SerializedDBDataObject;
import de.docware.framework.modules.excel.ExcelOleAutomation;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.table.HtmlTablePageSplitMode;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.util.sql.TableAndFieldName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Grid-Klasse für die Autoren-Auftrags-Historie und die Historienansicht von DataObjectTypes
 */
public class ChangeSetHistoryGridForm extends SimpleMasterDataSearchFilterGrid {

    private int numRowsPerPage = getConfig().getInteger(SystemSettings.XML_CONFIG_PATH_BASE + SystemSettings.XML_CONFIG_SUBPATH_TABLE_SPLIT_NUMBER_OF_ENTRIES, 100);
    private OnDblClickEvent onDblClickEvent;
    private Map<String, String> exchangeFieldsForDisplay;

    /**
     * Erzeugt eine Instanz von ChangeSetHistoryGridForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     * @param parentForm
     * @param tableName
     * @param specialHeaderFilterNames
     */
    public ChangeSetHistoryGridForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName,
                                    List<String> specialHeaderFilterNames) {
        super(dataConnector, parentForm, tableName, null);
        setColumnFilterFactory(new AuthorOrderDataSearchFilterFactory(getProject(), specialHeaderFilterNames));
        this.exchangeFieldsForDisplay = new HashMap<>();
        setMaxResults(J2EEHandler.isJ2EE() ? iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE : -1);
        showSearchFields(false);
        setEditAllowed(false);
        setModifyAllowed(false);
        showSelectCount(false);
        showToolbar(false);
        setLabelNotFoundText("!!Es liegen keine Ergebnisse vor.");
    }

    public OnDblClickEvent getOnDblClickEvent() {
        return onDblClickEvent;
    }

    @Override
    public void setOnDblClickEvent(OnDblClickEvent onDblClickEvent) {
        this.onDblClickEvent = onDblClickEvent;
    }

    public void setExchangeFieldsForDisplay(Map<String, String> exchangeFieldsForDisplay) {
        if (exchangeFieldsForDisplay == null) {
            exchangeFieldsForDisplay = new HashMap<>();
        }
        this.exchangeFieldsForDisplay = exchangeFieldsForDisplay;
    }

    @Override
    protected void addToolbarButtonsAndContextMenuItems(EditToolbarButtonMenuHelper toolbarHelper, GuiContextMenu contextMenu) {
        if (Constants.DEVELOPMENT) { // Geht nur unter Swing oder im besten Fall lokal unter J2EE im DEVELOPMENT-Modus
            GuiMenuItem menuItemOpenInExcel = new GuiMenuItem();
            menuItemOpenInExcel.setText("!!In Excel öffnen...");
            menuItemOpenInExcel.setIcon(DefaultImages.appExcel.getImage());
            menuItemOpenInExcel.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                public void fire(Event event) {
                    menuItemOpenInExcelClick(event);
                }
            });

            contextMenu.addChild(menuItemOpenInExcel);
        }
    }

    @Override
    protected void doEditOrView(Event event) {
        if ((getTable().getSelectedRowIndices().length > 0) && (onDblClickEvent != null)) {
            onDblClickEvent.onDblClick();
        }
    }

    private void menuItemOpenInExcelClick(Event event) {
        ExcelOleAutomation helper = new ExcelOleAutomation();
        helper.openTableInExcel(getTable(), 0, true);
        helper.release();
    }

    @Override
    protected RowWithAttributesAndSerialized createRow(DBDataObjectAttributes attributes) {
        RowWithAttributesAndSerialized row = new RowWithAttributesAndSerialized();
        row.attributes = attributes;

        for (EtkDisplayField field : displayResultFields.getFields()) {
            if (field.isVisible()) {
                String tableName = field.getKey().getTableName();
                String fieldName = field.getKey().getFieldName();
                String value = getVisualValueOfFieldValue(tableName, fieldName, attributes.getField(fieldName), field.isMultiLanguage());
                GuiLabel label = new GuiLabel(value);
                row.addChild(label);
            }
        }
        return row;
    }

    public int getColByFieldName(String fieldName) {
        int index = 0;
        for (EtkDisplayField displayField : getDisplayResultFields().getVisibleFields()) {
            if (displayField.getKey().getFieldName().equals(fieldName)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public void sortTableByCol(String fieldName) {
        if (getTable().getRowCount() > 0) {
            int index = getColByFieldName(fieldName);
            if (index != -1) {
                getTable().sortRowsAccordingToColumn(index, false);
            }
        }
    }

    @Override
    protected SimpleSelectSearchResultGrid.GuiTableRowWithAttributes addAttributesToGrid(DBDataObjectAttributes attributes) {
        RowWithAttributesAndSerialized row = createRow(attributes);
        if (row != null) {
            getTable().addRow(row);

            // Paginierung verwenden sobald es mehr als numRowsPerPage Zeilen gibt
            if (getTable().getRowCount() == numRowsPerPage + 1) {
                getTable().setHtmlTablePageSplitMode(HtmlTablePageSplitMode.BUTTONS);
            }
        }

        return row;
    }

    @Override
    protected int processResultAttributes(DBDataObjectAttributes attributes) {
        if (doValidateAttributes(attributes)) {
            if (addAttributesToGrid(attributes) != null) {
                showNoResultsLabel(false, false);
                return 1;
            }
        }
        return 0;
    }

    @Override
    protected String getVisualValueOfFieldValue(String tableName, String fieldName, DBDataObjectAttribute
            fieldValue, boolean isMultiLanguage) {
        if (tableName.equals(searchTable)) {
            String replaceTableAndFieldName = exchangeFieldsForDisplay.get(TableAndFieldName.make(tableName, fieldName));
            if ((replaceTableAndFieldName != null) && !TableAndFieldName.getFieldName(replaceTableAndFieldName).equals(replaceTableAndFieldName)) {
                // Datum richtig formatieren
                tableName = TableAndFieldName.getTableName(replaceTableAndFieldName);
                fieldName = TableAndFieldName.getFieldName(replaceTableAndFieldName);
            }
        }
        return super.getVisualValueOfFieldValue(tableName, fieldName, fieldValue, isMultiLanguage);
    }


    public class AuthorOrderDataSearchFilterFactory extends SimpleMasterDataSearchFilterFactory {

        private List<String> specialHeaderFilterNames;

        public AuthorOrderDataSearchFilterFactory(EtkProject project, List<String> specialHeaderFilterNames) {
            super(project);
            this.specialHeaderFilterNames = specialHeaderFilterNames;
        }

        @Override
        protected boolean changeColumnTableFilterValues(int column, EditControlFactory editControl) {
            if (editControl.getValues().editCreateMode == EditCreateMode.ecmTableColumnFilter) {
                boolean handleColumnFilter = specialHeaderFilterNames.contains(editControl.getFieldName());
                if (handleColumnFilter) {
                    // Trick um im Tabellenfilter ein SetOfEnum Eingabefeld zu erzeugen, dass als Tokens
                    // die Werte aus der zugehörigen Spalte der Tabelle enthält
                    editControl.getValues().field = editControl.getField().cloneMe();  // zur Sicherheit, damit die Originalwerte sich nicht ändern
                    editControl.getValues().field.setType(EtkFieldType.feSetOfEnum);  // behaupte, das Feld ist ein SetOfEnum
                    editControl.getOptions().handleAsSetOfEnum = true;  // und soll als SetOfEnum behandelt werden
                    editControl.getOptions().searchDisjunctive = true;
                    // alles weitere übernimmt EditControlFactory und das FilterInterface
                    AbstractGuiControl guiCtrl = EditControlFactory.doCreateEnumCheckBoxForTableColumnFilter(editControl.getValues(), editControl.getOptions());
                    if (guiCtrl != null) {
                        editControl.setControl(guiCtrl);
                    }
                }
            }
            return super.changeColumnTableFilterValues(column, editControl);
        }
    }


    public class RowWithAttributesAndSerialized extends SimpleSelectSearchResultGrid.GuiTableRowWithAttributes {

        public SerializedDBDataObject serializedObject;
    }
}
