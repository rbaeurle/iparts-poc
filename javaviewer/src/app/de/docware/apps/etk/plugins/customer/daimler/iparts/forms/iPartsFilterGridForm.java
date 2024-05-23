/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.DataObjectFilterGrid;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.forms.events.OnChangeEvent;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AbstractDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AggregateDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.VehicleDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsCodeDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsDataCode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsDataCodeList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.searchgrids.SelectSearchGridCode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.TwoGridValues;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSA;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.GuiButton;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiTextField;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.controls.table.GuiTableRowSorterInterface;
import de.docware.framework.modules.gui.controls.table.TableRowInterface;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateUtils;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.TableAndFieldName;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * Dialog für die Auswahl von SAAs, Code oder Aggregate-Baumuster im iPartsFilter Dialog {@link iPartsFilterDialog}
 */
public class iPartsFilterGridForm extends AbstractTwoDataObjectGridsForm implements iPartsConst {

    private static final String PLACEHOLDER_CHECKBOX_FIELD = VirtualFieldsUtils.addVirtualFieldMask("CHECKBOX_FIELD");
    private static final String PLACEHOLDER_ENRICH_FIELD = VirtualFieldsUtils.addVirtualFieldMask("ENRICH_FIELD");
    public static final String CONFIG_KEY_FILTER_TOP_SAA = "Plugin/iPartsEdit/FilterSAATop";
    public static final String CONFIG_KEY_FILTER_TOP_CODE = "Plugin/iPartsEdit/FilterCodeTop";
    public static final String CONFIG_KEY_FILTER_TOP_AGG_MODELS = "Plugin/iPartsEdit/FilterAggModelsTop";

    public enum DIALOG_TYPES {SAA, CODE, AGG_MODELS}

    private DIALOG_TYPES dialogType;
    private iPartsModel model;
    private String productGroup;
    private GuiPanel panelInput;
    private GuiTextField textfieldAddValue;
    private GuiButtonOnPanel selectAllButton;
    private GuiButtonOnPanel deselectAllButton;
    private Collection<TwoGridValues.ValueState> selectedValuesTop;
    private Collection<TwoGridValues.ValueState> selectedValuesBottom;
    private List<TwoGridValues.ValueState> currentSelectedValuesTop;
    private List<TwoGridValues.ValueState> currentSelectedValuesBottom;
    private boolean viewing;
    private boolean sortTop = true;
    private boolean sortBottom = true;

    protected iPartsFilterGridForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String configKeyTop, String dataObjectGridTopTitle, String configKeyBottom, String dataObjectGridBottomTitle) {
        super(dataConnector, parentForm, configKeyTop, dataObjectGridTopTitle, configKeyBottom, dataObjectGridBottomTitle);
        initWindow();
        viewing = false;
    }

    public boolean isViewing() {
        return viewing;
    }

    public void setViewing(boolean viewing) {
        this.viewing = viewing;
        selectAllButton.setVisible(!viewing);
        deselectAllButton.setVisible(!viewing);
        getButtonPanel().setButtonVisible(GuiButtonOnPanel.ButtonType.OK, !viewing);
        if (viewing) {
            getButtonPanel().getButtonOnPanel(GuiButtonOnPanel.ButtonType.CANCEL).setText("!!OK");
        } else {
            getButtonPanel().getButtonOnPanel(GuiButtonOnPanel.ButtonType.CANCEL).setText("!!Abbrechen");
        }
        panelInput.setVisible(!viewing);
        ((FilterDataObjectGrid)gridBottom).showDeleteMenu(!viewing);
    }

    public boolean isSortTop() {
        return sortTop;
    }

    public void setSortTop(boolean sortTop) {
        this.sortTop = sortTop;
    }

    public boolean isSortBottom() {
        return sortBottom;
    }

    public void setSortBottom(boolean sortBottom) {
        this.sortBottom = sortBottom;
    }

    private void initWindow() {
        setSplitPaneDividerRatio(0.6d);
        getButtonPanel().setDialogStyle(GuiButtonPanel.DialogStyle.DIALOG);
        selectAllButton = getButtonPanel().addCustomButton("!!Alle auswählen");
        selectAllButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                onSelectAllEntries(true);
            }
        });
        deselectAllButton = getButtonPanel().addCustomButton("!!Alle abwählen");
        deselectAllButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                onSelectAllEntries(false);
            }
        });
        addInputArea();
    }

    /**
     * Gibt die spezifischen DisplayFields für Aggregatebaumuster (Tabelle DA_MODEL) zurück
     *
     * @param top
     * @return
     */
    private List<EtkDisplayField> getDefaultAggModelsDisplayFields(boolean top) {
        EtkDisplayFields displayResultFields = new EtkDisplayFields();
        EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_MODEL, FIELD_DM_MODEL_NO, false, false);
        displayResultFields.addFeld(displayField);
        if (top) {
            displayField = new EtkDisplayField(TABLE_DA_MODEL, FIELD_DM_MODEL_TYPE, false, false);
            displayField.setColumnFilterEnabled(true);
            displayResultFields.addFeld(displayField);
            displayField = new EtkDisplayField(TABLE_DA_MODEL, FIELD_DM_SALES_TITLE, true, false);
            displayResultFields.addFeld(displayField);
            displayField = new EtkDisplayField(TABLE_DA_MODEL, FIELD_DM_AA, false, false);
            displayResultFields.addFeld(displayField);
            displayField = new EtkDisplayField(TABLE_DA_MODEL, FIELD_DM_CODE, true, false);
            displayResultFields.addFeld(displayField);
            displayField = new EtkDisplayField(TABLE_DA_MODEL, FIELD_DM_MODEL_VISIBLE, false, false);
            displayField.setColumnFilterEnabled(true);
            displayResultFields.addFeld(displayField);
            displayResultFields.loadStandards(getConfig());

        }
        return displayResultFields.getFields();
    }

    /**
     * Gibt die spezifischen DisplayFields für Code (Tabelle DA_CODE) zurück
     *
     * @param top
     * @return
     */
    private List<EtkDisplayField> getDefaultCodeDisplayFields(boolean top) {
        EtkDisplayFields displayResultFields = new EtkDisplayFields();
        EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_CODE, FIELD_DC_CODE_ID, false, false);
        displayField.setColumnFilterEnabled(true);
        displayResultFields.addFeld(displayField);
        displayField = new EtkDisplayField(TABLE_DA_CODE, FIELD_DC_DESC, true, false);
        displayResultFields.addFeld(displayField);
        displayField = new EtkDisplayField(TABLE_DA_CODE, FIELD_DC_SERIES_NO, false, false);
        displayResultFields.addFeld(displayField);
        displayField = new EtkDisplayField(TABLE_DA_CODE, FIELD_DC_SDATA, false, false);
        displayResultFields.addFeld(displayField);
        displayResultFields.loadStandards(getConfig());
        return displayResultFields.getFields();
    }

    public String getTableName() {
        switch (dialogType) {
            case CODE:
                return TABLE_DA_CODE;
            case AGG_MODELS:
                return TABLE_DA_MODEL;
            case SAA:
                return TABLE_DA_EDS_SAA_MODELS;
        }
        return "";
    }

    /**
     * Gibt die Default Spalten für SAAs (Tabelle DA_SAA) zurück.
     *
     * @param top
     * @return
     */
    private List<EtkDisplayField> getDefaultSaaDisplayFields(boolean top) {
        EtkDisplayFields displayResultFields = new EtkDisplayFields();
        EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_SAA_NO, false, false);
        displayField.setColumnFilterEnabled(true);
        displayResultFields.addFeld(displayField);
        if (top) {
            displayField = new EtkDisplayField(TABLE_DA_EDS_SAA_MODELS, iPartsDataVirtualFieldsDefinition.DESM_DESCRIPTION, true, false);
            displayResultFields.addFeld(displayField);
            displayResultFields.loadStandards(getConfig());

        }
        return displayResultFields.getFields();
    }

    /**
     * Erstellt das Checkbox Feld und gibt es zurück
     *
     * @return
     */
    public EtkDisplayField makeCheckboxField() {
        EtkDisplayField displayField = new EtkDisplayField(getTableName(), PLACEHOLDER_CHECKBOX_FIELD, false, false);
        List<String> languages = getConfig().getViewerLanguages();
        EtkMultiSprache texte = new EtkMultiSprache();
        for (String language : languages) {
            texte.setText(language, TranslationHandler.translateForLanguage("!!Übernehmen?", language));
        }
        displayField.setText(texte);
        displayField.setWidth(20);
        return displayField;
    }

    public EtkDisplayField makeEnrichField() {
        EtkDisplayField displayField = new EtkDisplayField(getTableName(), PLACEHOLDER_ENRICH_FIELD, false, false);
        List<String> languages = getConfig().getViewerLanguages();
        EtkMultiSprache texte = new EtkMultiSprache();
        for (String language : languages) {
            texte.setText(language, TranslationHandler.translateForLanguage("!!Anreicherung", language));
        }
        displayField.setText(texte);
        displayField.setVisible(false);
        displayField.setWidth(30);
        return displayField;
    }

    public TwoGridValues getSelectedValues() {
        return new TwoGridValues(getSelectedValuesForGrid(true), getSelectedValuesForGrid(false));
    }

    public void setSelectedValues(Set<String> selectedValues, boolean top) {
        TwoGridValues twoGridValues;
        if (top) {
            twoGridValues = new TwoGridValues(selectedValues, null);
            setSelectedValues(twoGridValues.getTopGridValues(), top);
        } else {
            twoGridValues = new TwoGridValues(null, selectedValues);
            setSelectedValues(twoGridValues.getBottomGridValues(), top);
        }
    }

    public void setSelectedValues(Collection<TwoGridValues.ValueState> selectedValues, boolean top) {
        if (top) {
            this.selectedValuesTop = selectedValues;
            this.currentSelectedValuesTop = new DwList<>(selectedValues);
            boolean isEnriched = isEnriched(selectedValuesTop);
            ((FilterDataObjectGrid)gridTop).showEnrichMenu(isEnriched);
            addToGridTopTitle(isEnriched ? "!!angereichert" : null);
        } else {
            this.selectedValuesBottom = selectedValues;
            this.currentSelectedValuesBottom = new DwList<>(selectedValues);
            boolean isEnriched = isEnriched(selectedValuesBottom);
            ((FilterDataObjectGrid)gridBottom).showEnrichMenu(isEnriched);
            addToGridBottomTitle(isEnriched ? "!!angereichert" : null);
        }
    }

    public void doSort(boolean topGrid) {
        final DataObjectGrid tempGrid = topGrid ? gridTop : gridBottom;
        tempGrid.getTable().setSortEnabled(true);
        GuiTableRowSorterInterface rowSorter = new GuiTableRowSorterInterface() {
            @Override
            public List<TableRowInterface> sortRows(List<TableRowInterface> unsortedRows, int sortColumnIndex, final boolean sortAscending,
                                                    boolean sortCaseInsensitive) {
                String dbTable = getTableName();
                int tempIndex = sortColumnIndex;
                // Unsichtbare Felder sind auch enthalten, werden aber bei der Index-Ermittlung nicht berücksichtigt.
                // Daher wird hier pro unsichtbaren Feld zwischen 0 und dem aktuellen Index der temporäre Index erhöht
                for (int i = 0; i <= sortColumnIndex; i++) {
                    if (!tempGrid.getDisplayFields().getFeld(i).isVisible()) {
                        tempIndex++;
                    }
                }
                String dbField = tempGrid.getDisplayFields().getFeld(tempIndex).getKey().getFieldName();
                if (!StrUtils.isEmpty(dbField) && !StrUtils.isEmpty(dbTable)) {
                    List<TableRowInterface> sortedRows = new DwList<>(unsortedRows);
                    final String dbSearchTable = dbTable;
                    final String dbSearchField = dbField;
                    Collections.sort(sortedRows, (o1, o2) -> {
                        EtkDataObject dataObject1 = ((DataObjectGrid.GuiTableRowWithObjects)o1).getObjectForTable(dbSearchTable);
                        EtkDataObject dataObject2 = ((DataObjectGrid.GuiTableRowWithObjects)o2).getObjectForTable(dbSearchTable);
                        int result = 0;
                        if ((dataObject1 != null) && (dataObject2 != null)) {
                            String value1 = dataObject1.getFieldValue(dbSearchField, getProject().getDBLanguage(), true);
                            String value2 = dataObject2.getFieldValue(dbSearchField, getProject().getDBLanguage(), true);

                            value1 = Utils.toSortString(value1);
                            value2 = Utils.toSortString(value2);

                            result = value1.compareTo(value2);
                            if (result != 0) {
                                return sortAscending ? result : -result;
                            }
                        }
                        return result;
                    });
                    return sortedRows;
                }
                return null;
            }
        };
        tempGrid.getTable().setRowSorter(rowSorter);
        if (tempGrid.getDisplayFields() != null) {
            tempGrid.getTable().sortRowsAccordingToColumn(getIndexOfSortField(tempGrid), true);
        }
    }

    private int getIndexOfSortField(DataObjectGrid tempGrid) {
        String initialSortField = "";
        switch (dialogType) {
            case SAA:
                initialSortField = FIELD_DA_ESM_SAA_NO;
                break;
            case CODE:
                initialSortField = FIELD_DC_CODE_ID;
                break;
            case AGG_MODELS:
                initialSortField = FIELD_DM_MODEL_NO;
                break;
        }
        if (StrUtils.isEmpty(initialSortField)) {
            return 0;
        }
        int index = 0;
        // Suche den Index des Inital-Sortierungs-Feldes (ohne unsichtbare Felder)
        if (tempGrid.getDisplayFields() != null) {
            for (EtkDisplayField displayField : tempGrid.getDisplayFields().getFields()) {
                if (displayField.getKey().getFieldName().equals(initialSortField)) {
                    break;
                }
                if (displayField.isVisible()) {
                    index++;
                }
            }
        }
        return index;
    }

    /**
     * Gibt die ausgewählten Zeilen für das übergebene Grid zurück
     *
     * @param topGrid
     * @return
     */
    private List<TwoGridValues.ValueState> getSelectedValuesForGrid(boolean topGrid) {
        List<TwoGridValues.ValueState> result = new LinkedList<TwoGridValues.ValueState>();
        String dbField = "";
        String dbTable = "";
        switch (dialogType) {
            case SAA:
                dbField = FIELD_DA_ESM_SAA_NO;
                dbTable = TABLE_DA_EDS_SAA_MODELS;
                break;
            case CODE:
                dbField = FIELD_DC_CODE_ID;
                dbTable = TABLE_DA_CODE;
                break;
            case AGG_MODELS:
                dbField = FIELD_DM_MODEL_NO;
                dbTable = TABLE_DA_MODEL;
                break;
        }
        if (!StrUtils.isEmpty(dbField) && !StrUtils.isEmpty(dbTable)) {
            DataObjectGrid tempGrid = topGrid ? gridTop : gridBottom;
            //List<TwoGridValues.ValueState> selectedValues = topGrid ? selectedValuesTop : selectedValuesBottom;
            List<TwoGridValues.ValueState> selectedValues = topGrid ? currentSelectedValuesTop : currentSelectedValuesBottom;
            for (int i = 0; i < tempGrid.getTable().getRowCount(); i++) {
                GuiTableRow row = tempGrid.getTable().getRow(i);
                iPartsGuiPanelWithCheckbox panelWithCheckbox = getCheckBoxFromRow(row);
                if (panelWithCheckbox != null) {
                    EtkDataObject dataObject = ((DataObjectGrid.GuiTableRowWithObjects)row).getObjectForTable(dbTable);
                    if (dataObject != null) {
                        if (dialogType == DIALOG_TYPES.CODE) {
                            String value = dataObject.getFieldValue(dbField);
                            TwoGridValues.ValueState valueState = findValueState(selectedValues, value);
                            if (valueState != null) {
                                valueState = valueState.cloneMe(); // ValueState klonen, damit nicht das Original verändert wird
                                valueState.checked = panelWithCheckbox.isSelected();
                                result.add(valueState);
                            } else {
                                result.add(new TwoGridValues.ValueState(value, panelWithCheckbox.isSelected()));
                            }
                        } else {
                            result.add(new TwoGridValues.ValueState(dataObject.getFieldValue(dbField), panelWithCheckbox.isSelected()));
                        }
                    }
                }
            }
        }
        return result;
    }

    private TwoGridValues.ValueState findValueState(Collection<TwoGridValues.ValueState> selectedValues, String searchValue) {
        if (!selectedValues.isEmpty()) {
            for (TwoGridValues.ValueState valueState : selectedValues) {
                if (valueState.value.equals(searchValue)) {
                    return valueState;
                }
            }
        }
        return null;
    }

    /**
     * Befüllt die {@link de.docware.apps.etk.base.project.base.EtkDataObjectList} für das untere Grid
     *
     * @param list
     */
    private void fillBottomGridList(DBDataObjectList list) {
        if ((list == null) || (selectedValuesBottom == null) || selectedValuesBottom.isEmpty()) {
            return;
        }
        List<iPartsDataCode> tempList = new ArrayList<>();
        String compareDate = getCompareDate();
        for (TwoGridValues.ValueState valueState : selectedValuesBottom) {
            switch (dialogType) {
                case SAA:
                    String modelNumber = "";
                    if (model != null) {
                        modelNumber = model.getModelId().getModelNumber();
                    }
                    iPartsSAAModelsId saaModelsId = new iPartsSAAModelsId(valueState.value, modelNumber);
                    iPartsDataSAAModels dataSAAModels = new iPartsDataSAAModels(getProject(), saaModelsId);
                    if (!dataSAAModels.existsInDB()) {
                        dataSAAModels.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                    }
                    dataSAAModels.getAttributes().addField(PLACEHOLDER_CHECKBOX_FIELD, SQLStringConvert.booleanToPPString(valueState.checked),
                                                           true, DBActionOrigin.FROM_DB);
                    dataSAAModels.getAttributes().addField(PLACEHOLDER_ENRICH_FIELD, valueState.getEnrichText(), true, DBActionOrigin.FROM_DB);
                    list.add(dataSAAModels, DBActionOrigin.FROM_DB);
                    break;
                case CODE:
                    getFittingDateTimeCode(valueState.value, valueState, false, compareDate, tempList);
                    list.addAll(tempList, DBActionOrigin.FROM_DB);
                    break;
                case AGG_MODELS:
                    iPartsModelId modelId = new iPartsModelId(valueState.value);
                    list.add(new iPartsDataModel(getProject(), modelId), DBActionOrigin.FROM_DB);
                    break;
            }
        }
    }

    /**
     * Bestimmt zu dem übergebenen Baumuster die verknüpften SAAs.
     *
     * @return
     */
    private DBDataObjectList<? extends EtkDataObject> retrieveSaas() {
        iPartsDataSAAModelsList saaModelsList = new iPartsDataSAAModelsList();
        if (model != null) {
            if (isViewing()) {
                retrieveSaasFromTopGridValues(saaModelsList, false);
            } else {
                saaModelsList = iPartsDataSAAModelsList.loadAllSaasAndBKsForModel(getProject(), model.getModelId());
                retrieveSaasFromTopGridValues(saaModelsList, true); // Hier kommen z.B. noch SAAs aus den Befestigungsteilen dazu
            }
        }
        return saaModelsList;
    }

    private void retrieveSaasFromTopGridValues(iPartsDataSAAModelsList saaModelsList, boolean addNewSAAsToSelectedValuesBottom) {
        String modelNo = model.getModelId().getModelNumber();
        boolean addVirtualDescription = false;
        for (EtkDisplayField displayField : gridTop.getDisplayFields().getFields()) {
            if (displayField.isVisible()) {
                if (TableAndFieldName.isVirtual(displayField.getKey().getFieldName()) &&
                    iPartsDataVirtualFieldsDefinition.DESM_DESCRIPTION.equals(displayField.getKey().getFieldName())) {
                    addVirtualDescription = true;
                    break;
                }
            }
        }

        boolean selectedValuesBottomChanged = false;
        iPartsNumberHelper numberHelper = new iPartsNumberHelper();
        for (TwoGridValues.ValueState valueState : selectedValuesTop) {
            iPartsSAAModelsId saaModelsId = new iPartsSAAModelsId(valueState.value, modelNo);
            if (!addNewSAAsToSelectedValuesBottom) {
                iPartsDataSAAModels dataSAAModels = new iPartsDataSAAModels(getProject(), saaModelsId);
                if (addVirtualDescription) { // virtuelle Benennung für SAA bzw. Baukasten hinzufügen
                    if (!dataSAAModels.existsInDB()) {
                        dataSAAModels.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                    }

                    EtkMultiSprache multiSprache;
                    if (numberHelper.isValidSaa(saaModelsId.getSAANumber())) {
                        String saaDescription = iPartsSA.getSaSaaDescription(getProject(), saaModelsId.getSAANumber(),
                                                                             getProject().getDBLanguage());
                        multiSprache = new EtkMultiSprache();
                        multiSprache.setText(getProject().getDBLanguage(), saaDescription);
                    } else {
                        PartId partId = new PartId(saaModelsId.getSAANumber(), "");
                        EtkDataPart dataPart = EtkDataObjectFactory.createDataPart(getProject(), partId);
                        multiSprache = dataPart.getFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR);
                    }

                    DBDataObjectAttribute attribute = new DBDataObjectAttribute(iPartsDataVirtualFieldsDefinition.DESM_DESCRIPTION,
                                                                                DBDataObjectAttribute.TYPE.MULTI_LANGUAGE, true);
                    attribute.setValueAsMultiLanguage(multiSprache, DBActionOrigin.FROM_DB);
                    dataSAAModels.getAttributes().addField(attribute, DBActionOrigin.FROM_DB);
                }

                saaModelsList.add(dataSAAModels, DBActionOrigin.FROM_DB);
            } else if (!saaModelsList.containsId(saaModelsId)) {
                valueState = valueState.cloneMe();
                valueState.checked = true;
                selectedValuesBottom.add(valueState);
                selectedValuesBottomChanged = true;
            }
        }

        if (selectedValuesBottomChanged) {
            setSelectedValues(selectedValuesBottom, false);
        }
    }

    /**
     * Bestimmt zum aktuellen Baumuster die baumusterbildenden Code
     *
     * @return
     */
    private DBDataObjectList<? extends EtkDataObject> retrieveCodes() {
        iPartsDataCodeList codeList = new iPartsDataCodeList();
        List<iPartsDataCode> tempList = new ArrayList<>();
        Set<String> codeSet;
        if (isViewing()) {
            codeSet = new LinkedHashSet<>();
            for (TwoGridValues.ValueState valueState : selectedValuesTop) {
                codeSet.add(valueState.value);
            }
        } else {
            if (!StrUtils.isEmpty(getSeriesNo())) {
                codeSet = DaimlerCodes.getCodeSet(model.getCodes());
            } else {
                codeSet = new LinkedHashSet<>();
            }
        }

        // Zusätzlich soll die Ausführungsart als "normaler" Code interpretiert werden
        if (!isViewing() && (model != null) && !StrUtils.isEmpty(model.getAusfuehrungsArt())) {
            codeSet.add(model.getAusfuehrungsArt());
        }

        // Weil die Anzeige aller Code ein Performancegrab ist, werden nur die baumusterbildenden Code angezeigt
        String compareDate = getCompareDate();
        for (String code : codeSet) {
            TwoGridValues.ValueState valueState = findValueState(selectedValuesTop, code);
            getFittingDateTimeCode(code, valueState, true, compareDate, tempList);
        }
        // Codes sortieren
        Collections.sort(tempList, (o1, o2) -> {
            String codeId1 = o1.getAsId().getCodeId();
            String codeId2 = o2.getAsId().getCodeId();
            if (codeId1.equals(codeId2)) {
                return 0;
            } else {
                return Utils.toSortString(codeId1).compareTo(Utils.toSortString(codeId2));
            }
        });

        codeList.addAll(tempList, DBActionOrigin.FROM_DB);

        return codeList;
    }

    private void getFittingDateTimeCode(String codeId, TwoGridValues.ValueState valueState, boolean emptyProductGroup,
                                        String compareDate, List<iPartsDataCode> resultList) {
        String seriesNo = getSeriesNo();
        String productGroup = getProductGroup();
        if (valueState != null) {
            if (StrUtils.isValid(valueState.sourceSeriesNumber, valueState.sourceProductGroup)) {
                seriesNo = valueState.sourceSeriesNumber;
                productGroup = valueState.sourceProductGroup;
            }
        }
        iPartsDataCode code = null;
        if (!productGroup.isEmpty()) {
            code = iPartsDataCodeList.getFittingDateTimeCodeWithAddSearch(getProject(), getProject().getDBLanguage(), codeId, seriesNo, productGroup, compareDate);
        }

        if (code == null) {
            productGroup = emptyProductGroup ? "" : productGroup;
            iPartsDataCode dataCode = new iPartsDataCode(getProject(), new iPartsCodeDataId(codeId, seriesNo, productGroup, "", iPartsImportDataOrigin.UNKNOWN));
            resultList.add(dataCode);
        } else {
            resultList.add(code);
        }
    }

    /**
     * Datum zum Filtern der Code nach der Regel Datum-Ab-Code <= Vergleichsdatum < Datum-Bis-Code
     * TTZ nehmen, falls vorhanden
     * Falls Aggregatedatenkarte TTZ von Fahrzeugdatenkarte nehmen, falls vorhanden
     * Falls TTZ nicht vorhanden, Schlussabnahmedatum nehmen
     * Falls Schlussabnahmedatum nicht vorhanden, aktuelles Datum nehmen
     *
     * @return ermitteltes Datum
     */
    private String getCompareDate() {
        String compareDate = "";
        AbstractDataCard dataCard = null;
        if (parentForm instanceof iPartsDataCardDialog) {
            iPartsDataCardDialog dataCardDialog = (iPartsDataCardDialog)parentForm;
            dataCard = dataCardDialog.getDataCard();
        } else if (parentForm instanceof iPartsFilterAdminDialog) {
            iPartsFilterAdminDialog dataCardDialog = (iPartsFilterAdminDialog)parentForm;
            dataCard = dataCardDialog.getFilledDataCard();
        }
        if (dataCard != null) {
            if (dataCard instanceof VehicleDataCard) {
                VehicleDataCard vehicleDataCard = (VehicleDataCard)dataCard;
                compareDate = vehicleDataCard.getDateOfTechnicalState();
            } else if (dataCard instanceof AggregateDataCard) {
                AggregateDataCard aggregateDataCard = (AggregateDataCard)dataCard;
                VehicleDataCard parentDataCard = aggregateDataCard.getParentDatacard();
                if (parentDataCard != null) {
                    compareDate = parentDataCard.getDateOfTechnicalState();
                }
            }
            if (StrUtils.isEmpty(compareDate)) {
                compareDate = dataCard.getTechnicalApprovalDate();
            }
            if (StrUtils.isValid(compareDate)) {
                return compareDate;
            }
        }
        return DateUtils.getCurrentDateFormatted(DateUtils.simpleDateFormatyyyyMMdd);
    }

    /**
     * Bestimmt zum aktuellen Baumuster die abhängigen Aggregatebaumuster
     *
     * @return
     */
    private DBDataObjectList<? extends EtkDataObject> retrieveAggModels() {
        iPartsDataModelList modelList = new iPartsDataModelList();
        if (model != null) {
            // Laut Confluence nur nach abhängigen Aggregaten suchen, wenn das Baumuster mit "C" beginnt
            if (model.getModelId().getModelNumber().startsWith(MODEL_NUMBER_PREFIX_CAR)) {
                if (isViewing()) {
                    for (TwoGridValues.ValueState valueState : selectedValuesTop) {
                        iPartsModelId modelId = new iPartsModelId(valueState.value);
                        iPartsDataModel dataModel = new iPartsDataModel(getProject(), modelId);
                        if (!dataModel.existsInDB()) {
                            dataModel.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                        }
                        modelList.add(dataModel, DBActionOrigin.FROM_DB);
                    }

                } else {
                    // Wird über die DisplayFields - ohne Checkbox Spalte - bestimmt
                    // Join auf DA_MODELS_AGGS
                    EtkDisplayFields displayFields = new EtkDisplayFields(gridTop.getDisplayFields());
                    displayFields.deleteFeld(0); // Entferne das Checkbox-Feld
                    displayFields.deleteFeld(0); // Entferne das Enriched-Feld
                    modelList.searchSortAndFillWithJoin(getProject(), getProject().getDBLanguage(), displayFields,
                                                        new String[]{ FIELD_DM_MODEL_NO },                 // Feld(er) der Tabelle für den Join
                                                        TABLE_DA_MODELS_AGGS,                              // Join-Tabelle
                                                        new String[]{ FIELD_DMA_AGGREGATE_NO },                  // Join-Tabellenfeld(er)
                                                        false,                                                // LeftOuterJoin?
                                                        false,                                                // Sind die Bedingungen vom Join ODER-verknüpft?
                                                        new String[]{ TableAndFieldName.make(TABLE_DA_MODELS_AGGS, FIELD_DMA_MODEL_NO) }, // Where-Felder inkl. Tabellennamen
                                                        new String[]{ model.getModelId().getModelNumber() },             // Where-Werte
                                                        false,                                                // Sind die Where-Bedingungen ODER-verknüpft?
                                                        new String[]{ FIELD_DM_MODEL_NO },                 // Feld(er) der Tabelle zum Sortieren
                                                        false);                                               // nicht caseInsensitive);
                }
            }
        }
        return modelList;
    }

    private String getSeriesNo() {
        if (model == null) {
            return "";
        }
        return model.getSeriesId().getSeriesNumber();
    }

    public void setDialogType(DIALOG_TYPES dialogType) {
        handleAdditionalGUIChanges(dialogType);
        this.dialogType = dialogType;
    }

    /**
     * Abhängig vom Typ werden bestimmte Veränderungen am Dialog gemacht
     *
     * @param dialogType
     */
    private void handleAdditionalGUIChanges(DIALOG_TYPES dialogType) {
        switch (dialogType) {
            case CODE:
                GuiButton buttonAddValue = new GuiButton();
                buttonAddValue.setName("buttonSearchValue");
                buttonAddValue.__internal_setGenerationDpi(96);
                buttonAddValue.registerTranslationHandler(getUITranslationHandler());
                buttonAddValue.setScaleForResolution(true);
                buttonAddValue.setMinimumWidth(100);
                buttonAddValue.setMinimumHeight(10);
                buttonAddValue.setMnemonicEnabled(true);
                buttonAddValue.setText("!!Code suchen...");
                buttonAddValue.setModalResult(ModalResult.NONE);
                buttonAddValue.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
                    @Override
                    public void fire(Event event) {
                        searchAdditionalValues();
                    }
                });
                ConstraintsGridBag gridbagConstraints = new ConstraintsGridBag(3, 0, 1, 1, 0.0, 0.0, "w", "n", 8, 4, 8, 8);
                buttonAddValue.setConstraints(gridbagConstraints);
                panelInput.addChild(buttonAddValue);
                break;
        }
    }

    private boolean isEnriched(Collection<TwoGridValues.ValueState> valueStateList) {
        if ((valueStateList == null) || valueStateList.isEmpty()) {
            return false; // Liste ist leer
        }
        for (TwoGridValues.ValueState valueState : valueStateList) {
            if (!valueState.enrichReasons.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Öffnet den Suchdialog für die Suche nach zusätzlichen Werten für das untere Grid
     */
    private void searchAdditionalValues() {
        String extraValue = textfieldAddValue.getText();
        switch (dialogType) {
            case CODE:
                SelectSearchGridCode selectSearchGridCode = new SelectSearchGridCode(this, getProductGroup(), getCompareDate());
                selectSearchGridCode.setFilterFieldNames(new String[]{ FIELD_DC_SERIES_NO });
                selectSearchGridCode.setFilterValues(new String[]{ getSeriesNo() });
                extraValue = selectSearchGridCode.showGridSelectionDialog(extraValue);
                break;
        }
        if (!StrUtils.isEmpty(extraValue)) {
            addAdditionalValuesToGrid(extraValue);
        }
    }

    /**
     * Setzt die {@link iPartsModelId} und die abhängigen Beschriftungen
     *
     * @param modelId
     */
    public void setModelId(iPartsModelId modelId) {
        boolean modelCheck = !StrUtils.isEmpty(modelId.getModelNumber());
        if (modelCheck) {
            model = iPartsModel.getInstance(getProject(), modelId);
        }
        switch (dialogType) {
            case SAA:
                setGridWindowTitles("!!SAA/BK Gültigkeiten", "!!SAA/BK Gültigkeiten zu Baumuster \"%1\"", new String[]{ modelId.getModelNumber() },
                                    "!!SAA/BK Gültigkeiten ohne Baumuster", null, modelCheck);
                break;
            case AGG_MODELS:
                setGridWindowTitles("!!Aggregatebaumuster Gültigkeiten", "!!Abhängige Aggregatebaumuster zu Baumuster \"%1\"", new String[]{ modelId.getModelNumber() },
                                    "!!Keine verknüpften Aggregatebaumuster vorhanden", null, modelCheck);
                break;
        }
    }

    private String getProductGroupText() {
        if (!StrUtils.isEmpty(productGroup)) {
            return getProject().getVisObject().asText(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_PRODUCT_GRP, productGroup, getProject().getViewerLanguage());
        }
        return "";
    }

    /**
     * Setzt die Produkgruppe
     *
     * @param productGroupEnumValue
     */
    public void setProductGroup(String productGroupEnumValue) {
        boolean productCheck = !StrUtils.isEmpty(productGroupEnumValue);
        if (productCheck) {
            this.productGroup = productGroupEnumValue;
        }
        boolean seriesCheck = model != null;
        String seriesNo = "";
        if (seriesCheck) {
            seriesNo = model.getSeriesId().getSeriesNumber();
            seriesCheck = !StrUtils.isEmpty(seriesNo);
        }
        String productGroupEnumText = getProductGroupText();
        setGridWindowTitles("!!Code Gültigkeiten", "!!Produktgruppe \"%1\" und Baureihe \"%2\"", new String[]{ productGroupEnumText, seriesNo },
                            "!!Code mit Produktgruppe \"%1\" und ohne Baureihe zu Baumuster", new String[]{ productGroupEnumText }, seriesCheck);
    }

    public String getProductGroup() {
        if (productGroup != null) {
            return productGroup;
        } else {
            return "";
        }
    }

    /**
     * Setzt den Titel und den Untertitel des Dialogs
     *
     * @param windowTitle               - Haupttitel
     * @param subTitlePositive          - positver Untertitel
     * @param positivePlaceholderValues - Werte für mögliche Platzhalter im positiven Untertitel
     * @param subTitleNegative          - negativer Untertitel
     * @param negativePlaceholderValues - Werte für mögliche Platzhalter im negativen Untertitel
     * @param checkValue                - Wert, ob positiver oder negativer Untertitel angezeigt werden soll
     */
    private void setGridWindowTitles(String windowTitle, String subTitlePositive, String[] positivePlaceholderValues,
                                     String subTitleNegative, String[] negativePlaceholderValues, boolean checkValue) {
        String subTitle;
        String[] placeholderValues;
        if (checkValue) {
            placeholderValues = positivePlaceholderValues;
            subTitle = subTitlePositive;
        } else {
            placeholderValues = negativePlaceholderValues;
            subTitle = subTitleNegative;
        }
        if (placeholderValues == null) {
            subTitle = TranslationHandler.translate(subTitle);
        } else {
            subTitle = TranslationHandler.translate(subTitle, placeholderValues);
        }
        setWindowTitle(TranslationHandler.translate(windowTitle), subTitle);
    }

    /**
     * Gibt das Panel mit der Checkbox von
     *
     * @param row
     * @return
     */
    public iPartsGuiPanelWithCheckbox getCheckBoxFromRow(GuiTableRow row) {
        if (row instanceof DataObjectGrid.GuiTableRowWithObjects) {
            Object childObject = row.getChildForColumn(0);
            if (childObject instanceof iPartsGuiPanelWithCheckbox) {
                return (iPartsGuiPanelWithCheckbox)childObject;
            }
        }
        return null;
    }

    /**
     * Fügt ein zusätzliches {@link EtkDataObject} zum unteren Grid hinzu. Das DataObject wird entweder aus dem übergebenen
     * Wert oder aus dem Wert des festgelegten Textfields erzeugt.
     */
    private void addAdditionalValuesToGrid(String additionalValue) {
        String additionalInput = additionalValue;
        if (StrUtils.isEmpty(additionalInput)) {
            additionalInput = textfieldAddValue.getText();
        }
        if (StrUtils.isEmpty(additionalInput)) {
            return;
        }
        EtkDataObject input = null;
        switch (dialogType) {
            case SAA:
                String modelNumber = "";
                if (model != null) {
                    // Falls das Baumuster nicht angegeben wurde, dann können keine Texte ermittelt werden
                    modelNumber = model.getModelId().getModelNumber();
                }
                iPartsNumberHelper numberHelper = new iPartsNumberHelper();
                String additionalInputUnformated = numberHelper.unformatSaaBkForEdit(getProject(), additionalInput); // Formatierung entfernen
                if (!numberHelper.isValidSaaOrBk(additionalInputUnformated, true)) {
                    MessageDialog.showError(TranslationHandler.translate("!!Der Eingabewert \"%1\" wurde zu \"%2\" formatiert. " +
                                                                         "Das Ergebnis entspricht nicht dem vorgegebenen SAA-Format.",
                                                                         additionalInput, additionalInputUnformated));
                    return;
                }
                iPartsSAAModelsId id = new iPartsSAAModelsId(additionalInputUnformated, modelNumber);
                input = new iPartsDataSAAModels(getProject(), id);
                break;
            case CODE:
                String compareDate = getCompareDate();
                String productGroup = getProductGroup();
                iPartsDataCode dataCode = null;
                if (!productGroup.isEmpty()) {
                    dataCode = iPartsDataCodeList.getFittingDateTimeCodeWithAddSearch(getProject(), getProject().getDBLanguage(), additionalInput,
                                                                                      getSeriesNo(), productGroup, compareDate);
                }
                if (dataCode == null) {
                    iPartsCodeDataId codeDataId = new iPartsCodeDataId(additionalInput, getSeriesNo(), productGroup, "", iPartsImportDataOrigin.UNKNOWN);
                    input = new iPartsDataCode(getProject(), codeDataId);
                } else {
                    input = dataCode;
                }
                break;
            case AGG_MODELS:
                iPartsModelId modelId = new iPartsModelId(additionalInput);
                input = new iPartsDataModel(getProject(), modelId);
                break;
        }
        addExtraFields(input);
        if (input != null) {
            gridBottom.showNoResultsLabel(false);
            gridBottom.addObjectToGrid(input);
            doEnableButtons();
        }
    }

    private void onCheckboxSelected(Event event) {
        currentSelectedValuesTop = getSelectedValuesForGrid(true);
        currentSelectedValuesBottom = getSelectedValuesForGrid(false);
        doEnableButtons();
    }

    /**
     * (De-)Selektiert alle Zeilen eines Grids
     *
     * @param select
     */
    private void onSelectAllEntries(boolean select) {
        iPartsGuiPanelWithCheckbox panelWithCheckbox;
        for (int i = 0; i < gridTop.getTable().getRowCount(); i++) {
            GuiTableRow row = gridTop.getTable().getRow(i);
            panelWithCheckbox = getCheckBoxFromRow(row);
            if (panelWithCheckbox != null) {
                panelWithCheckbox.setSelected(select);
                panelWithCheckbox.repaint();
            }
        }
        for (int i = 0; i < gridBottom.getTable().getRowCount(); i++) {
            GuiTableRow row = gridBottom.getTable().getRow(i);
            panelWithCheckbox = getCheckBoxFromRow(row);
            if (panelWithCheckbox != null) {
                panelWithCheckbox.setSelected(select);
                panelWithCheckbox.repaint();
            }
        }

    }

    /**
     * Passt das Buttonverhalten abhängig vom aktuellen Zustand an
     */
    private void doEnableButtons() {
        if ((gridTop.getTable().getRowCount() == 0) && (gridBottom.getTable().getRowCount() == 0)) {
            getButtonPanel().setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, false);
            selectAllButton.setEnabled(false);
            deselectAllButton.setEnabled(false);
        } else {
            boolean filtered = gridTop.getTable().hasFilterValueForAnyColumn();
            if (!filtered) {
                filtered = gridBottom.getTable().hasFilterValueForAnyColumn();
            }
            getButtonPanel().setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, !filtered);
            selectAllButton.setEnabled(true);
            deselectAllButton.setEnabled(true);
        }
    }


    @Override
    protected DataObjectGrid createGrid(boolean top) {
        return new FilterDataObjectGrid(getConnector(), this, top);
    }

    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields(boolean top) {
        List<EtkDisplayField> result = new ArrayList<EtkDisplayField>();
        switch (dialogType) {
            case SAA:
                result.addAll(getDefaultSaaDisplayFields(top));
                break;
            case CODE:
                result.addAll(getDefaultCodeDisplayFields(top));
                break;
            case AGG_MODELS:
                result.addAll(getDefaultAggModelsDisplayFields(top));
                break;
        }

        return result;
    }

    @Override
    public ModalResult showModal() {
        dataToGridWithSort();
        return super.showModal();
    }

    @Override
    protected EtkDisplayFields getDisplayFields(String configKey) {
        EtkDisplayFields displayFields = super.getDisplayFields(configKey);
        if (isViewing()) {
            EtkDisplayField displayField = displayFields.getFeldByName(getTableName(), PLACEHOLDER_CHECKBOX_FIELD);
            if (displayField != null) {
                displayField.setVisible(false);
            }

        } else {
            if (!displayFields.contains(getTableName(), PLACEHOLDER_CHECKBOX_FIELD, false)) {
                displayFields.addFeld(0, makeCheckboxField());
            }
        }
        if (!displayFields.contains(getTableName(), PLACEHOLDER_ENRICH_FIELD, false)) {
            displayFields.addFeld(1, makeEnrichField());
        }
        return displayFields;
    }

    @Override
    protected void dataToGrid() {
        super.dataToGrid();
        Dimension screenSize = FrameworkUtils.getScreenSize();
        int overallWidth = gridTop.getOverallWidth();
        if (isGridVisible(false)) {
            overallWidth = Math.max(overallWidth, gridBottom.getOverallWidth());
        }
        overallWidth += 38; // Ränder + Scollbalken
        overallWidth = Math.max(overallWidth, 600); // Mindestbreite 600 Pixel
        if (overallWidth <= getBottomPanel().getPreferredWidth()) {
            overallWidth = getBottomPanel().getPreferredWidth();
        } else {
            overallWidth = Math.min(overallWidth, (screenSize.width - 20));
        }
        int heigthWithoutTopPanel = getWindow().getHeight() - getTopPanel().getPreferredHeight();
        int actualHeight = gridTop.getOverallHeight() + heigthWithoutTopPanel;
        if (actualHeight > (screenSize.height - 20)) {
            getWindow().setSize(overallWidth, screenSize.height - 20);
        } else {
            getWindow().setSize(overallWidth, Math.max(actualHeight, getWindow().getHeight()));
        }
        doEnableButtons();
    }

    protected void dataToGridWithSort() {
        dataToGrid();
        if (sortTop) {
            this.doSort(true);
        }
        if (sortBottom) {
            this.doSort(false);
        }
    }

    @Override
    protected void createAndAddDataObjectsToGrid(boolean top) {
        DBDataObjectList<? extends EtkDataObject> result = new DBDataObjectList<EtkDataObject>();
        if (top) {
            switch (dialogType) {
                case SAA:
                    result = retrieveSaas();
                    break;
                case CODE:
                    result = retrieveCodes();
                    break;
                case AGG_MODELS:
                    result = retrieveAggModels();
                    break;
            }
        } else {
            DBDataObjectList list = null;
            switch (dialogType) {
                case SAA:
                    list = new iPartsDataSAAModelsList();
                    break;
                case CODE:
                    list = new iPartsDataCodeList();
                    break;
                case AGG_MODELS:
                    list = new iPartsDataModelList();
                    break;
            }
            fillBottomGridList(list);
            if ((list != null) && !list.isEmpty()) {
                result = list;
            }
        }
        addExtraFields(result);
        addDataObjectListToGrid(top, result);
    }

    private void addExtraFields(EtkDataObject input) {
        GenericEtkDataObjectList list = new GenericEtkDataObjectList();
        list.add(input, DBActionOrigin.FROM_DB);
        addExtraFields(list);
    }

    /**
     * Fügt jedem DataObject die für die Darstellung benötigten "virtuellen" Spalten hinzu.
     *
     * @param dataObjectList
     */
    private void addExtraFields(DBDataObjectList<? extends EtkDataObject> dataObjectList) {
        String falseString = SQLStringConvert.booleanToPPString(false);
        for (EtkDataObject dataObject : dataObjectList) {
            if (dataObject.getAttributes() == null) {
                dataObject.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            }
            if (!dataObject.attributeExists(PLACEHOLDER_CHECKBOX_FIELD)) {
                dataObject.getAttributes().addField(PLACEHOLDER_CHECKBOX_FIELD, falseString, true, DBActionOrigin.FROM_DB);
            }
            if (!dataObject.attributeExists(PLACEHOLDER_ENRICH_FIELD)) {
                dataObject.getAttributes().addField(PLACEHOLDER_ENRICH_FIELD, "", true, DBActionOrigin.FROM_DB);
            }
        }

    }

    /*
       if (currentDataObject.getAttributes() == null) {
                                currentDataObject.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                            }
                            if (currentDataObject.attributeExists(PLACEHOLDER_CHECKBOX_FIELD)) {
                                currentDataObject.getAttributes().addField(PLACEHOLDER_CHECKBOX_FIELD, "", DBActionOrigin.FROM_DB);
                            }
                            if (currentDataObject.attributeExists(PLACEHOLDER_ENRICH_FIELD)) {
                                currentDataObject.getAttributes().addField(PLACEHOLDER_ENRICH_FIELD, "", DBActionOrigin.FROM_DB);
                            }
     */

    private static iPartsFilterGridForm prepareFilterGridForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                              iPartsModelId modelId, TwoGridValues twoGridValues, DIALOG_TYPES dialogType,
                                                              boolean isViewing) {
        String configKeyTop = "";
        String gridTopTitle = "";
        String gridBottomTitle = "";
        switch (dialogType) {
            case SAA:
                configKeyTop = CONFIG_KEY_FILTER_TOP_SAA;
                gridTopTitle = "!!SAAs zu Baumuster";
                gridBottomTitle = "!!Zusätzliche SAAs";
                break;
            case AGG_MODELS:
                configKeyTop = CONFIG_KEY_FILTER_TOP_AGG_MODELS;
                gridTopTitle = "!!Aggregatebaumuster";
                gridBottomTitle = "!!Zusätzliche Aggregatebaumuster";
                break;
            case CODE:
                configKeyTop = CONFIG_KEY_FILTER_TOP_CODE;
                gridTopTitle = "!!Code zu Produktgruppe";
                gridBottomTitle = "!!Zusätzliche Code";
                break;
        }
        if (!configKeyTop.isEmpty()) {
            iPartsFilterGridForm gridForm = new iPartsFilterGridForm(dataConnector, parentForm, configKeyTop,
                                                                     gridTopTitle, "", gridBottomTitle);
            gridForm.setDialogType(dialogType);
            gridForm.setModelId(modelId);
            gridForm.setViewing(isViewing);
            if (twoGridValues != null) {
                List<TwoGridValues.ValueState> topValues;
                List<TwoGridValues.ValueState> bottomValues;
                if (isViewing) {
                    topValues = twoGridValues.getCheckedValueStates(true);
                    bottomValues = twoGridValues.getCheckedValueStates(false);
                } else {
                    topValues = new DwList<TwoGridValues.ValueState>(twoGridValues.getTopGridValues());
                    bottomValues = new DwList<TwoGridValues.ValueState>(twoGridValues.getBottomGridValues());
                }

                gridForm.setSelectedValues(topValues, true);
                gridForm.setSelectedValues(bottomValues, false);
            }
            if (dialogType == DIALOG_TYPES.CODE) {
                gridForm.setGridBottomVisible(true);
            } else {
                gridForm.setGridBottomVisible(!isViewing);
            }
            return gridForm;
        }
        return null;
    }

    /**
     * Öffnet den TwoGrid Dialog für SAAs
     *
     * @param dataConnector
     * @param parentForm
     * @param modelId
     * @param saaNumbers
     * @param isViewing
     */
    public static TwoGridValues showSaaSelectionTwoGridDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                              iPartsModelId modelId, TwoGridValues saaNumbers, boolean isViewing) {
        iPartsFilterGridForm gridForm = prepareFilterGridForm(dataConnector, parentForm, modelId, saaNumbers, DIALOG_TYPES.SAA, isViewing);
        if (gridForm != null) {
            gridForm.showModal();
            if (gridForm.getWindow().getModalResult() == ModalResult.OK) {
                return gridForm.getSelectedValues();
            }
        }
        return null;
    }

    /**
     * Öffnet den TwoGrid Dialog für Aggregatebaumuster
     *
     * @param dataConnector
     * @param parentForm
     * @param modelId
     * @param aggModels
     * @param isViewing
     */
    public static TwoGridValues showAggModelsSelectionTwoGridDialog(AbstractJavaViewerFormIConnector dataConnector,
                                                                    AbstractJavaViewerForm parentForm,
                                                                    iPartsModelId modelId, TwoGridValues aggModels, boolean isViewing) {
        iPartsFilterGridForm gridForm = prepareFilterGridForm(dataConnector, parentForm, modelId, aggModels, DIALOG_TYPES.AGG_MODELS, isViewing);
        if (gridForm != null) {
            gridForm.showModal();
            if (gridForm.getWindow().getModalResult() == ModalResult.OK) {
                return gridForm.getSelectedValues();
            }
        }
        return null;
    }

    /**
     * Öffnet den TwoGrid Dialog für Code
     *
     * @param dataConnector
     * @param parentForm
     * @param productGroupEnumValue
     * @param modelId
     * @param codes
     * @param isViewing
     */
    public static TwoGridValues showCodeSelectionTwoGridDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                               String productGroupEnumValue, iPartsModelId modelId, TwoGridValues codes, boolean isViewing) {
        iPartsFilterGridForm gridForm = prepareFilterGridForm(dataConnector, parentForm, modelId, codes, DIALOG_TYPES.CODE, isViewing);
        if (gridForm != null) {
            gridForm.setProductGroup(productGroupEnumValue);
            gridForm.showModal();
            if (gridForm.getWindow().getModalResult() == ModalResult.OK) {
                return gridForm.getSelectedValues();
            }
        }
        return null;
    }


    /**
     * DataObjectGrid für den Filterdialog
     */
    private class FilterDataObjectGrid extends DataObjectFilterGrid {

        private boolean topGrid;
        private GuiMenuItem menuItemDeleteEntry;
        private GuiMenuItem menuItemEnrichEntry;

        public FilterDataObjectGrid(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, boolean topGrid) {
            super(dataConnector, parentForm);
            this.topGrid = topGrid;
            initGrid();
            setFilterEndEvent(() -> doEnableButtons());
        }

        private void initGrid() {
            getTable().addEventListener(new EventListener(Event.MOUSE_DOUBLECLICKED_EVENT) {
                @Override
                public void fire(Event event) {
                    iPartsGuiPanelWithCheckbox panelWithCheckbox = getCheckBoxFromRow(getTable().getSelectedRow());
                    if (panelWithCheckbox != null) {
                        panelWithCheckbox.setSelected(!panelWithCheckbox.isSelected());
                    }
                }
            });
            if (!topGrid) {
                menuItemDeleteEntry = getToolbarHelper().createMenuEntry("menuEntryDelete", "!!Element löschen", null, new EventListener(Event.MENU_ITEM_EVENT) {
                    @Override
                    public void fire(Event event) {
                        doDeleteEntry(event);
                    }
                }, getUITranslationHandler());
                getContextMenu().addChild(menuItemDeleteEntry);
            }
            menuItemEnrichEntry = getToolbarHelper().createMenuEntry("menuEntryEnrich", "!!Anreicherung anzeigen", null, new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    doShowEnrichColumn(event);
                }
            }, getUITranslationHandler());
            menuItemEnrichEntry.setVisible(false);
            getContextMenu().addChild(menuItemEnrichEntry);
        }

        private void doDeleteEntry(Event event) {
            iPartsGuiPanelWithCheckbox panelWithCheckbox = getCheckBoxFromRow(getTable().getSelectedRow());
            if (panelWithCheckbox != null) {
                getTable().removeRow(getTable().getSelectedRow());
                selectedValuesBottom = getSelectedValuesForGrid(false);
                dataToGrid(false);
                onCheckboxSelected(null);
            }
        }

        private void doShowEnrichColumn(Event event) {
            EtkDisplayField displayField = getDisplayFields().getFeldByName(getTableName(), PLACEHOLDER_ENRICH_FIELD);
            if (displayField != null) {
                if (displayField.isVisible()) {
                    displayField.setVisible(false);
                    menuItemEnrichEntry.setText("!!Anreicherung anzeigen");
                } else {
                    displayField.setVisible(true);
                    menuItemEnrichEntry.setText("!!Anreicherung ausblenden");
                }
                int topIndex = gridTop.getTable().getSelectedRowIndex();
                int bottomIndex = gridBottom.getTable().getSelectedRowIndex();
                gridTop.clear();
                gridBottom.clear();
                dataToGridWithSort();
                gridTop.scrollToRowIfExists(topIndex);
                gridBottom.scrollToRowIfExists(bottomIndex);
            }
        }

        public void showEnrichMenu(boolean visible) {
            menuItemEnrichEntry.setVisible(visible);
        }

        public void showDeleteMenu(boolean visible) {
            menuItemDeleteEntry.setVisible(visible);
        }

        /**
         * Eine Zeile mit Objekten hinzufügen.
         *
         * @param dataObjects Für den Inhalt einer Zeile können mehrere Objekte verantwortlich sein -> z.B. Entry und Part,
         *                    deshalb eine Liste von Objekten
         */
        @Override
        public void addObjectToGrid(EtkDataObject... dataObjects) {
            if (getTable().getRowCount() != 0) {
                // Überprüfen, ob die Zeile schon existiert
                for (int i = 0; i < getTable().getRowCount(); i++) {
                    GuiTableRowWithObjects row = (GuiTableRowWithObjects)getTable().getRow(i);
                    // Gleiche Anzahl DataObjects
                    if (row.dataObjects.size() == dataObjects.length) {
                        boolean sameDataObjectIds = true;
                        // Durchlaufe alle DataObjects der Zeile und überprüfe, ob die IDs gleich sind
                        for (EtkDataObject currentDataObject : dataObjects) {
                            EtkDataObject dataObjectInRow = row.getObjectForTable(currentDataObject.getTableName());
                            // Wenn das DataObject in der Zeile nicht existiert -> nicht gleich
                            if (dataObjectInRow == null) {
                                sameDataObjectIds = false;
                                break;
                            } else if (!dataObjectInRow.getAsId().equals(currentDataObject.getAsId())) {
                                // Wenn die IDs nicht übereinstimmen -> nicht gleich
                                sameDataObjectIds = false;
                                break;
                            }
                        }
                        // Wenn die Anzahl und die IDs gleich sind -> Treffer
                        if (sameDataObjectIds) {
                            return;
                        }
                    }
                }
            }
            super.addObjectToGrid(dataObjects);
        }


        @Override
        protected GuiTableRowWithObjects createRow(List<EtkDataObject> dataObjects) {
            String dbFieldForCheck = "";
            switch (dialogType) {
                case SAA:
                    dbFieldForCheck = FIELD_DA_ESM_SAA_NO;
                    break;
                case CODE:
                    dbFieldForCheck = FIELD_DC_CODE_ID;
                    break;
                case AGG_MODELS:
                    dbFieldForCheck = FIELD_DM_MODEL_NO;
                    break;
            }

            GuiTableRowWithObjects row = new GuiTableRowWithObjects(dataObjects);
            OnChangeEvent onChangeEvent = new OnChangeEvent() {
                @Override
                public void onChange() {
                    onCheckboxSelected(null);
                }
            };
            iPartsGuiPanelWithCheckbox panelWithCheckbox = new iPartsGuiPanelWithCheckbox(true, TableAndFieldName.make(getTableName(), PLACEHOLDER_CHECKBOX_FIELD),
                                                                                          row, onChangeEvent);
            List<TwoGridValues.ValueState> selectedValueStates = topGrid ? currentSelectedValuesTop : currentSelectedValuesBottom;
            for (EtkDisplayField field : displayFields.getFields()) {
                if (field.isVisible()) {
                    String fieldName = field.getKey().getFieldName();

                    if (fieldName.equals(PLACEHOLDER_CHECKBOX_FIELD)) {
                        // Setze die Checkbox an der vorgegebene Stelle
                        row.addChild(panelWithCheckbox, () -> panelWithCheckbox.getTextRepresentation());
                    } else {
                        EtkDataObject objectForTable = row.getObjectForTable(getTableName());
                        String rawValue = objectForTable.getFieldValue(dbFieldForCheck);
                        TwoGridValues.ValueState valueState = findValueState(selectedValueStates, rawValue);
                        if (fieldName.equals(PLACEHOLDER_ENRICH_FIELD)) {
                            if (valueState != null) {
                                objectForTable.setFieldValue(PLACEHOLDER_ENRICH_FIELD, valueState.getEnrichText(), DBActionOrigin.FROM_DB);
                            }
                        } else {
                            // Welches Object ist für diese Tabelle zuständig?
                            if (fieldName.equalsIgnoreCase(dbFieldForCheck)) {
                                if (valueState == null) {
                                    // Bei SAAs sind fehlende SAAs in der Datenkarte nicht selektiert
                                    valueState = new TwoGridValues.ValueState(rawValue, dialogType != DIALOG_TYPES.SAA);
                                }

                                panelWithCheckbox.switchOffEventListeners();
                                panelWithCheckbox.setSelected(valueState.checked);
                                panelWithCheckbox.switchOnEventListeners();
                            }
                        }
                        String value = getVisualValueOfField(getTableName(), fieldName, objectForTable);
                        GuiLabel label = new GuiLabel(value);
                        row.addChild(label);
                    }
//                    if (tableName.equals(PLACEHOLDER_CHECKBOX_TABLE) && fieldName.equals(PLACEHOLDER_CHECKBOX_FIELD)) {
//                        // Setze die Checkbox an der vorgegebene Stelle
//                        row.addChild(panelWithCheckbox);
//                    } else {
//                        // Welches Object ist für diese Tabelle zuständig?
//                        EtkDataObject objectForTable = row.getObjectForTable(tableName);
//                        if (fieldName.equalsIgnoreCase(dbFieldForCheck)) {
//                            String rawValue = objectForTable.getFieldValue(dbFieldForCheck);
//                            for (TwoGridValues.ValueState valueState : selectedValueStates) {
//                                if (valueState.value.equals(rawValue)) {
//                                    panelWithCheckbox.setSelected(valueState.checked);
//                                    break;
//                                }
//                            }
//                        }
//
//                        String value = getVisualValueOfField(tableName, fieldName, objectForTable);
//                        GuiLabel label = new GuiLabel(value);
//                        row.addChild(label);
//                    }
                }
            }
            return row;
        }
    }

    /**
     * Fügt den Bereich für die manuelle Eingabe hinzu
     */
    private void addInputArea() {
        panelInput = new GuiPanel();
        panelInput.setName("panelInput");
        panelInput.__internal_setGenerationDpi(96);
        panelInput.registerTranslationHandler(getUITranslationHandler());
        panelInput.setScaleForResolution(true);
        panelInput.setMinimumWidth(10);
        panelInput.setMinimumHeight(10);
        LayoutGridBag panelInputLayout = new LayoutGridBag();
        panelInput.setLayout(panelInputLayout);

        GuiLabel labelAddSaa = new GuiLabel();
        labelAddSaa.setName("labelAddSaa");
        labelAddSaa.__internal_setGenerationDpi(96);
        labelAddSaa.registerTranslationHandler(getUITranslationHandler());
        labelAddSaa.setScaleForResolution(true);
        labelAddSaa.setMinimumWidth(10);
        labelAddSaa.setMinimumHeight(10);
        labelAddSaa.setText("!!Manuelle Eingabe:");
        ConstraintsGridBag gridbagConstraints = new ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "w", "n", 8, 8, 8, 4);
        labelAddSaa.setConstraints(gridbagConstraints);
        panelInput.addChild(labelAddSaa);

        textfieldAddValue = new de.docware.framework.modules.gui.controls.GuiTextField();
        textfieldAddValue.setName("textfieldAddSaa");
        textfieldAddValue.__internal_setGenerationDpi(96);
        textfieldAddValue.registerTranslationHandler(getUITranslationHandler());
        textfieldAddValue.setScaleForResolution(true);
        textfieldAddValue.setMinimumWidth(200);
        textfieldAddValue.setMinimumHeight(10);
        gridbagConstraints = new ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, "w", "n", 8, 4, 8, 4);
        textfieldAddValue.setConstraints(gridbagConstraints);
        panelInput.addChild(textfieldAddValue);

        GuiButton buttonAddValue = new GuiButton();
        buttonAddValue.setName("buttonAddValue");
        buttonAddValue.__internal_setGenerationDpi(96);
        buttonAddValue.registerTranslationHandler(getUITranslationHandler());
        buttonAddValue.setScaleForResolution(true);
        buttonAddValue.setMinimumWidth(100);
        buttonAddValue.setMinimumHeight(10);
        buttonAddValue.setMnemonicEnabled(true);
        buttonAddValue.setText("!!Hinzufügen");
        buttonAddValue.setModalResult(ModalResult.NONE);
        buttonAddValue.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                addAdditionalValuesToGrid(null);
            }
        });
        gridbagConstraints = new ConstraintsGridBag(2, 0, 1, 1, 100.0, 0.0, "w", "n", 8, 4, 8, 8);
        buttonAddValue.setConstraints(gridbagConstraints);
        panelInput.addChild(buttonAddValue);

        ConstraintsBorder panelInputConstraints = new ConstraintsBorder();
        panelInputConstraints.setPosition("south");
        panelInput.setConstraints(panelInputConstraints);
        getBottomPanel().addChild(panelInput);
    }
}