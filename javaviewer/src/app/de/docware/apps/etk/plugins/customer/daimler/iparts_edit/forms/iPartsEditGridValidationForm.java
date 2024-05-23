/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.AbstractSortingObject;
import de.docware.apps.etk.base.forms.common.components.DataObjectFilterGrid;
import de.docware.apps.etk.base.forms.common.components.DataObjectGridRowSorter;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.filter.EtkFilterItem;
import de.docware.apps.etk.base.project.filter.EtkFilterTyp;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AbstractDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterSwitchboard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.config.db.datatypes.DatatypeHtmlResult;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.table.HtmlTablePageSplitMode;
import de.docware.framework.modules.gui.controls.table.TableRowInterface;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.util.StrUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin.TABLE_FOR_EVALUATION_RESULTS;

public abstract class iPartsEditGridValidationForm extends iPartsEditBaseValidationForm {

    public iPartsEditGridValidationForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, boolean removeAdditionalInfoPanel, boolean minimizeAdditionalInfoPanel) {
        super(dataConnector, parentForm, removeAdditionalInfoPanel, minimizeAdditionalInfoPanel, TABLE_FOR_EVALUATION_RESULTS);
    }

    @Override
    protected AbstractJavaViewerForm createValidationContent(iPartsFilterSwitchboard filterForModelEvaluationSwitchboard) {
        iPartsDataObjectGridForValidation grid =
                new iPartsDataObjectGridForValidation(getConnector(), this, true);
        grid.setDisplayFields(getDisplayFields(getDisplayFieldConfigKey(null)));
        return grid;
    }

    @Override
    public void updateValidationContent(boolean getSelectedModelsFromSession, boolean forceReloadAssembly, boolean updateValidationGUI) {
        updateDisplayFields(getSelectedModelsFromSession);
        if (validationContent instanceof iPartsDataObjectGridForValidation) {
            iPartsDataObjectGridForValidation content = (iPartsDataObjectGridForValidation)validationContent;
            content.clearGrid();
            content.updateGridHeader(true);
            List<? extends ValidationItem> validationItems = createValidationItemList();

            validationItems = beforeValidateGrid(validationItems);

            // Für alle Baumuster aus selectedModels eine virtuelle Baumuster-Datenkarte erzeugen, damit filtern und
            // das Filterergebnis über den Filtergrund an das virtuelle Feld für die Baumusterauswertung übertragen
            EtkProject project = getProject();
            for (String selectedModel : getModelsForEvaluation()) {
                // Baumuster-Datenkarte erzeugen und im Filter setzen
                AbstractDataCard dataCard = filterForModelEvaluation.setDataCardByModel(selectedModel, project);
                filterForInvisibleEntries.setCurrentDataCard(dataCard, project);

                validateGrid(validationItems, selectedModel);
            }

            validationItems = afterValidateGrid(validationItems);

            for (ValidationItem validationItem : validationItems) {
                content.addValidationObjectToGrid(validationItem);
            }
        }
    }

    /**
     * Wird aufgerufen bevor die Auswertung pro Baumuster läuft
     *
     * @param validationItems
     */
    protected List<? extends ValidationItem> beforeValidateGrid(List<? extends ValidationItem> validationItems) {
        return validationItems;
    }

    /**
     * Wird aufgerufen nachdem die Auswertung pro Baumuster gelaufen ist
     *
     * @param validationItems
     */
    protected List<? extends ValidationItem> afterValidateGrid(List<? extends ValidationItem> validationItems) {
        return validationItems;
    }

    /**
     * Auswertung pro Baumuster für alle Zeilen des Grids
     *
     * @param validationItems
     * @param selectedModel
     */
    protected abstract void validateGrid(List<? extends ValidationItem> validationItems, String selectedModel);

    /**
     * Hier wird die initiale Liste zur Auswertung erzeugt
     *
     * @return
     */
    protected abstract List<? extends ValidationItem> createValidationItemList();

    public DataObjectFilterGrid getGrid() {
        if (validationContent instanceof DataObjectFilterGrid) {
            return (DataObjectFilterGrid)(validationContent);
        }
        return null;
    }

    /**
     * String representation des Auswertungsergebnisses. Kann überschrieben werden, wenn die Darstellung als
     * anderer Datentyp erfolgen soll
     *
     * @param result
     * @return
     */
    protected abstract String getVisualValueOfValidationResult(ValidationResult result);

    protected String getAdditionalInfoText(ValidationItem validationItem, String model) {
        return "";
    }

    protected void handleCellClick(ValidationItem validationItem, String model) {
        setAdditionalInfoText(getAdditionalInfoText(validationItem, model));
    }

    protected interface ValidationItem {

        void setValidationResultForModel(String model, ValidationResult result);

        ValidationResult getValidationResultForModel(String model);

        List<EtkDataObject> getDataObjects();
    }

    protected class GenericValidationItem implements ValidationItem {

        protected List<EtkDataObject> dataObjects;
        protected Map<String, ValidationResult> validationResults;

        public GenericValidationItem(List<EtkDataObject> dataObjects) {
            this.dataObjects = dataObjects;
            validationResults = new HashMap<>();
        }

        public void setValidationResultForModel(String model, ValidationResult result) {
            validationResults.put(model, result);
        }

        public ValidationResult getValidationResultForModel(String model) {
            return validationResults.get(model);
        }

        public List<EtkDataObject> getDataObjects() {
            return dataObjects;
        }
    }

    protected class ValidationGridSortingObject extends AbstractSortingObject {

        private ValidationItem validationItem;

        ValidationGridSortingObject(ValidationItem validationItem, int index) {
            super((EtkDataObject)null, index);
            this.validationItem = validationItem;
        }

        @Override
        public String getFieldValue(EtkProject project, String tableName, String fieldName) {
            if (validationItem != null) {
                String model = getModelOrFINFromVirtualFieldName(fieldName);
                ValidationResult validationResultForModel = validationItem.getValidationResultForModel(model);
                if (validationResultForModel != null) {
                    return validationResultForModel.getDbValue();
                }
            }
            return null;
        }
    }

    protected class iPartsDataObjectGridForValidation extends DataObjectFilterGrid {

        iPartsDataObjectGridForValidation(AbstractJavaViewerFormIConnector dataConnector,
                                          AbstractJavaViewerForm parentForm, boolean createContextMenu) {
            super(dataConnector, parentForm, createContextMenu);
            sorter = new DataObjectGridRowSorter(getProject(), getTable(), getDisplayFields()) {
                @Override
                protected AbstractSortingObject getSortingObject(EtkProject project, String sortTableName, TableRowInterface row, int lfdNr) {
                    if (sortTableName.equals(TABLE_FOR_EVALUATION_RESULTS)) {
                        Object additionalData = row.getAdditionalData();
                        if (additionalData instanceof ValidationItem) {
                            ValidationItem validationItem = (ValidationItem)additionalData;
                            return new ValidationGridSortingObject(validationItem, lfdNr);

                        }

                        return null;
                    } else {
                        return super.getSortingObject(project, sortTableName, row, lfdNr);
                    }
                }
            };
            setColumnFilterFactory(new ValidationGridFilterFactory(getProject()));
        }

        @Override
        public EtkDisplayFields getDisplayFields() {
            EtkDisplayFields displayFields = super.getDisplayFields();
            if ((additionalDisplayFields != null) && (firstAdditionalFieldIndex != -1)) {
                // erst alle zusätzlichen Felder unsichtbar machen
                for (int i = firstAdditionalFieldIndex; i < displayFields.size(); i++) {
                    displayFields.getFeld(i).setVisible(false);
                }
                // und dann die benötigten wieder sichtbar machen
                for (EtkDisplayField additionalDisplayField : additionalDisplayFields) {
                    EtkDisplayField foundField = displayFields.getFeldByKey(additionalDisplayField.getKey(), additionalDisplayField.isUsageField());
                    if (foundField != null) {
                        foundField.setVisible(true);
                    }
                }
            }
            return displayFields;
        }

        @Override
        public void setDisplayFields(EtkDisplayFields displayFields) {
            EtkDisplayFields copyDisplayFields = new EtkDisplayFields(displayFields);
            if (firstAdditionalFieldIndex == -1) {
                firstAdditionalFieldIndex = displayFields.size();

                iPartsProductId productId = getProductIdForModelStorage();
                if ((productId != null) && productId.isValidId()) {
                    Set<String> productModels = iPartsProduct.getInstance(getProject(), productId).getModelNumbers(getProject());
                    List<EtkDisplayField> virtualModelFields = getVirtualModelOrFINFields(productModels, false);
                    for (EtkDisplayField virtualModelField : virtualModelFields) {
                        virtualModelField.setVisible(false);
                        copyDisplayFields.addFeldIfNotExists(virtualModelField);
                    }
                }
            }
            super.setDisplayFields(copyDisplayFields, true);
        }

        public void addValidationObjectToGrid(ValidationItem validationItem) {
            GuiTableRowWithObjects row = createValidationRow(validationItem);
            getTable().addRow(row);

            int pageSplitNumberOfEntriesPerPage = getTable().getPageSplitNumberOfEntriesPerPage();
            if ((pageSplitNumberOfEntriesPerPage > 0) && (getTable().getRowCount() > pageSplitNumberOfEntriesPerPage)) {
                getTable().setHtmlTablePageSplitMode(HtmlTablePageSplitMode.BUTTONS);
            }
        }

        protected GuiTableRowWithObjects createValidationRow(final ValidationItem validationItem) {
            GuiTableRowWithObjects row = new GuiTableRowWithObjects(validationItem.getDataObjects());
            row.setAdditionalData(validationItem);
            for (EtkDisplayField field : displayFields.getFields()) {
                if (field.isVisible()) {
                    String fieldName = field.getKey().getFieldName();
                    String tableName = field.getKey().getTableName();

                    // Welches Object ist für diese Tabelle zuständig?
                    EtkDataObject objectForTable = row.getObjectForTable(tableName);
                    if (objectForTable != null) {
                        String value;
                        value = getVisualValueOfField(tableName, fieldName, objectForTable);
                        GuiLabel label = new GuiLabel(value);
                        label.addEventListener(new EventListener(Event.MOUSE_CLICKED_EVENT) {
                            @Override
                            public void fire(Event event) {
                                if (event.getIntParameter(Event.EVENT_PARAMETER_MOUSE_BUTTON) <= 1) { // nur bei Linksklick
                                    handleCellClick(validationItem, null);
                                }
                            }
                        });
                        row.addChild(label);
                    } else if (tableName.equals(TABLE_FOR_EVALUATION_RESULTS)) {
                        // Es handelt sich um ein Ergebnisfeld
                        final String model = getModelOrFINFromVirtualFieldName(fieldName);
                        GuiLabel label = new GuiLabel();
                        if (StrUtils.isValid(model)) {
                            ValidationResult validationResult = validationItem.getValidationResultForModel(model);
                            String validationResultString = getVisualValueOfValidationResult(validationResult);
                            DatatypeHtmlResult datatypeHtmlResult = getProject().getVisObject().asHtml(tableName, fieldName, validationResultString, getProject().getDBLanguage());
                            if (validationResult != null) {
                                label.setText(datatypeHtmlResult.getStringResult());
                            }
                        }
                        label.setHorizontalAlignment(GuiLabel.HorizontalAlignment.CENTER);
                        label.addEventListener(new EventListener(Event.MOUSE_CLICKED_EVENT) {
                            @Override
                            public void fire(Event event) {
                                if (event.getIntParameter(Event.EVENT_PARAMETER_MOUSE_BUTTON) <= 1) { // nur bei Linksklick
                                    handleCellClick(validationItem, model);
                                }
                            }
                        });
                        row.addChild(label);
                    }
                }
            }
            return row;
        }


        private class ValidationGridFilterFactory extends DataObjectColumnFilterFactory {

            ValidationGridFilterFactory(EtkProject project) {
                super(project);
            }

            private boolean checkFilterValidationItem(ValidationItem validationItem) {
                for (EtkFilterTyp etkFilterTyp : getAssemblyListFilter().getActiveFilter()) {
                    if (etkFilterTyp.getFilterTypus() == EtkFilterTyp.FilterTypus.GRIDFILTER) {
                        for (int i = 0; i < etkFilterTyp.getFilterItems().size(); i++) {
                            EtkFilterItem filterItem = etkFilterTyp.getFilterItems().get(i);
                            if (filterItem.getTableName().equals(TABLE_FOR_EVALUATION_RESULTS)) {
                                String modelNumberForField = getModelOrFINFromVirtualFieldName(filterItem.getLowFieldName());
                                String filterValue = etkFilterTyp.getFilterValues().get(i);
                                ValidationResult validationResultForModel = validationItem.getValidationResultForModel(modelNumberForField);
                                if ((validationResultForModel != null) &&
                                    !getVisualValueOfValidationResult(validationResultForModel).equals(filterValue)) {
                                    return false;
                                }
                            }
                        }
                    }
                }
                return true;
            }

            @Override
            protected boolean tableRowIsValidForFilter(GuiTableRowWithObjects entry) {
                // In den Validierungsergebnissen und in den DataObjects filtern
                if (!super.tableRowIsValidForFilter(entry)) {
                    return false;
                }

                if (entry.getAdditionalData() instanceof ValidationItem) {
                    ValidationItem validationItem = (ValidationItem)(entry.getAdditionalData());
                    return checkFilterValidationItem(validationItem);
                }

                return true;
            }

            @Override
            protected void addRowToGrid(GuiTableRowWithObjects rowWithObjects) {
                Object additionalData = rowWithObjects.getAdditionalData();
                if (additionalData instanceof ValidationItem) {
                    addValidationObjectToGrid((ValidationItem)additionalData);
                }
            }
        }
    }
}
