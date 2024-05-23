/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDialogDateTimeHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ColorTableHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsColorTable;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsEditGridValidationForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditValidationHelper;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;


public class iPartsRelatedInfoVariantsModelEvaluationForm extends iPartsRelatedInfoModelEvaluationForm {

    private static final String CONFIG_KEY_VARIANT_MODEL_EVALUATION = "Plugin/iPartsEdit/VariantModelEvaluation" +
                                                                      iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS;
    private static final String CACHE_KEY_DELIMITER = "||";

    private Set<String> allModelsForEvaluation;

    /**
     * Erzeugt einen neuen Dialog für die Anzeige der Baumusterauswertung
     *
     * @param dataConnector
     * @param parentForm
     * @param relatedInfo
     */
    public iPartsRelatedInfoVariantsModelEvaluationForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm, IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo);
        allModelsForEvaluation = null;
    }

    @Override
    protected void postCreateGui() {
        validationContentForm = new VariantValidationGrid(getConnector(), this);

        // speichern der selektierten Baumuster in der Session aktivieren
        validationContentForm.saveModelSelectionInSession(true);
        createModelSelectionHeaderPanel();
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        if (forceUpdateAll || ((getConnector().getActiveRelatedSubForm() == this) || (getConnector().getActiveRelatedSubForm() == parentForm))) {
            if (validationContentForm instanceof VariantValidationGrid) {
                VariantValidationGrid contentForm = (VariantValidationGrid)(validationContentForm);
                contentForm.clearCachedValidationResult();
            }
        }
    }

    public void updateValidation() {
        if (validationContentForm instanceof VariantValidationGrid) {
            VariantValidationGrid contentForm = (VariantValidationGrid)(validationContentForm);
            contentForm.updateValidationContent(true, true, true);
            updateSelectedModelsLabel();
        }
    }

    protected iPartsDataPartListEntry getPartlistEntry() {
        EtkDataPartListEntry partListEntry = getConnector().getRelatedInfoData().getAsPartListEntry(getProject());
        if (partListEntry instanceof iPartsDataPartListEntry) {
            return (iPartsDataPartListEntry)(partListEntry);
        }
        return null;
    }

    public Set<String> getAllModelsForEvaluation() {
        if (allModelsForEvaluation != null) {
            return allModelsForEvaluation;
        }
        iPartsDataPartListEntry partlistEntry = getPartlistEntry();
        if (partlistEntry != null) {
            allModelsForEvaluation = iPartsEditValidationHelper.getAllModelsForAssembly(partlistEntry.getOwnerAssembly());
        }
        return allModelsForEvaluation;
    }


    protected class VariantValidationGrid extends iPartsEditGridValidationForm {

        private Map<iPartsColorTableToPartId, Map<String, Boolean>> colorTableModelTimeSliceFilterResult;
        private List<? extends ValidationItem> cachedValidationResult;

        public VariantValidationGrid(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
            super(dataConnector, parentForm, false, false);
            colorTableModelTimeSliceFilterResult = new HashMap<>();
            cachedValidationResult = null;
        }

        @Override
        public Set<String> getModelsForEvaluation() {
            Set<String> allModelsForEvaluation = getAllModelsForEvaluation();
            if (allModelsForEvaluation != null) {
                return allModelsForEvaluation;
            }
            return super.getModelsForEvaluation();
        }

        @Override
        public String createVirtualFieldNameForModelOrFINEvaluation(String modelNumber) {
            return VirtualFieldsUtils.addVirtualFieldMask(iPartsDataVirtualFieldsDefinition.DA_MODEL_EVALUATION
                                                          + iPartsDataVirtualFieldsDefinition.VIRTFIELD_SPACER
                                                          + iPartsDataVirtualFieldsDefinition.DA_MODEL_EVALUATION_COLORS
                                                          + iPartsDataVirtualFieldsDefinition.DA_MODEL_EVALUATION_SPACER
                                                          + modelNumber);
        }

        @Override
        protected String getDisplayFieldConfigKey(String partListType) {
            return CONFIG_KEY_VARIANT_MODEL_EVALUATION;
        }

        @Override
        public List<EtkDisplayField> createDefaultDisplayFields() {
            List<EtkDisplayField> defaultDisplayFields = new ArrayList<>();
            addDefaultDisplayField(iPartsConst.TABLE_DA_COLORTABLE_DATA, iPartsConst.FIELD_DCTD_TABLE_ID, false, false, defaultDisplayFields);
            addDefaultDisplayField(iPartsConst.TABLE_DA_COLORTABLE_CONTENT, iPartsConst.FIELD_DCTC_POS, false, false, defaultDisplayFields);
            addDefaultDisplayField(iPartsConst.TABLE_DA_COLORTABLE_CONTENT, iPartsConst.FIELD_DCTC_COLOR_VAR, false, false, defaultDisplayFields);
            addDefaultDisplayField(iPartsConst.TABLE_DA_COLOR_NUMBER, iPartsConst.FIELD_DCN_DESC, true, false, defaultDisplayFields);
            addDefaultDisplayField(iPartsConst.TABLE_DA_COLORTABLE_CONTENT, iPartsConst.FIELD_DCTC_CODE, false, false, defaultDisplayFields);
            addDefaultDisplayField(iPartsConst.TABLE_DA_COLORTABLE_CONTENT, iPartsConst.FIELD_DCTC_CODE_AS, false, false, defaultDisplayFields);

            return defaultDisplayFields;
        }

        @Override
        public iPartsProductId getProductIdForModelStorage() {
            if ((getPartlistEntry() != null) && (getPartlistEntry().getOwnerAssembly() != null)) {
                return getPartlistEntry().getOwnerAssembly().getProductIdFromModuleUsage();
            }
            return null;
        }

        private Boolean getModelTimeSliceFilterResult(iPartsColorTableToPartId colorTableToPartId, String model) {
            if (colorTableModelTimeSliceFilterResult != null) {
                Map<String, Boolean> filterResultForModel = colorTableModelTimeSliceFilterResult.get(colorTableToPartId);
                if (filterResultForModel == null) {
                    return null;
                } else {
                    return filterResultForModel.get(model);
                }
            }
            return null;
        }

        private void setModelTimeSliceFilterResult(iPartsColorTableToPartId colorTableToPartId, String model, boolean filterResult) {
            if (colorTableModelTimeSliceFilterResult != null) {
                Map<String, Boolean> filterResultForModel = colorTableModelTimeSliceFilterResult.get(colorTableToPartId);
                if (filterResultForModel == null) {
                    filterResultForModel = new HashMap<>();
                    colorTableModelTimeSliceFilterResult.put(colorTableToPartId, filterResultForModel);
                }
                filterResultForModel.put(model, filterResult);
            }
        }

        private boolean isValidPreFilter(ColortableValidationItem validationItem) {
            if (validationItem.isValid()) {
                iPartsSeriesId seriesFromColorTable =
                        ColorTableHelper.getSeriesFromColorTableOrPartListEntry(validationItem.colorTableData, "");
                String seriesFromPartlistEntry =
                        getPartlistEntry().getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_SERIES_NO);

                // Nur Farbvariantentabellen anzeigen, die zur Baureihe des Stücklisteneintrags passen
                if (!seriesFromColorTable.getSeriesNumber().equals(seriesFromPartlistEntry)) {
                    return false;
                }

                // Nur ET-relevante Farbvariantentabellen anzeigen
                if (!etkzAndStatusValid(validationItem.colorTableToPart.getET_KZ(), validationItem.colorTableToPart.getFieldValue(FIELD_DCTP_STATUS))) {
                    return false;
                }

                // Nur ET-relevante Farbvarianten anzeigen
                if (!etkzAndStatusValid(validationItem.colorTableContent.getET_KZ(), validationItem.colorTableContent.getFieldValue(FIELD_DCTC_STATUS))) {
                    return false;
                }

                return true;
            }
            return false;
        }

        private boolean etkzAndStatusValid(String et_kz, String status) {
            return (et_kz.isEmpty() && status.equals(iPartsDataReleaseState.RELEASED.getDbValue()));
        }

        private List<ColortableValidationItem> compressColorTables(List<? extends ValidationItem> validationItems) {
            List<ColortableValidationItem> filteredResult = new DwList<>();
            Map<String, Map<String, List<ColortableValidationItem>>> entriesForColortable = new TreeMap<>();

            // Map< Farbtabelle -> Map< Datum ab -> Liste der Einträge> >
            for (ValidationItem validationItem : validationItems) {
                if (validationItem instanceof ColortableValidationItem) {
                    ColortableValidationItem colortableValidationItem = (ColortableValidationItem)(validationItem);

                    if (colortableValidationItem.isValid()) {
                        String colorTableId = colortableValidationItem.colorTableData.getAsId().getColorTableId();
                        Map<String, List<ColortableValidationItem>> dateMap = entriesForColortable.get(colorTableId);
                        if (dateMap == null) {
                            dateMap = new TreeMap<>(new Comparator<String>() {
                                @Override
                                public int compare(String o1, String o2) {
                                    return o2.compareTo(o1); // absteigende Sortierung für das Datum (neuester Wert zuerst)
                                }
                            });
                            entriesForColortable.put(colorTableId, dateMap);
                        }

                        String sdata = colortableValidationItem.colorTableToPart.getAsId().getSDATA();
                        if (!colortableValidationItem.colorTableToPart.isHistoryRecord()) {
                            sdata = iPartsDialogDateTimeHandler.FINAL_STATE_DATETIME;
                        }

                        List<ColortableValidationItem> items = dateMap.get(sdata);
                        if (items == null) {
                            items = new DwList<>();
                            dateMap.put(sdata, items);
                        }
                        items.add(colortableValidationItem);
                    }
                }
            }

            // Pro Farbtabelle nur die neusten Werte als Ergbnis zurückliefern
            for (Map<String, List<ColortableValidationItem>> dateMap : entriesForColortable.values()) {
                // das aktuellste Datum ist das an erster Stelle
                if (!dateMap.isEmpty()) {
                    filteredResult.addAll(dateMap.values().iterator().next());
                }
            }

            return filteredResult;
        }

        public void clearCachedValidationResult() {
            if (cachedValidationResult != null) {
                cachedValidationResult.clear();
                cachedValidationResult = null;
            }
        }

        @Override
        public void updateValidationContent(boolean getSelectedModelsFromSession, boolean forceReloadAssembly, boolean updateValidationGUI) {
            // Filterung wird nur einmal initial für alle Baumuster durchgeführt, danach wird nur noch die Anzeige
            // angepasst
            if (validationContent instanceof iPartsDataObjectGridForValidation) {
                iPartsDataObjectGridForValidation content = (iPartsDataObjectGridForValidation)validationContent;
                int sortColumn = content.getTable().getSortColumn();
                boolean sortAscending = content.getTable().isSortAscending();
                if (cachedValidationResult == null) {
                    super.updateValidationContent(getSelectedModelsFromSession, forceReloadAssembly, updateValidationGUI);
                } else {
                    updateDisplayFields(getSelectedModelsFromSession);
                    content.clearGrid();
                    content.updateGridHeader(true);
                    for (ValidationItem validationItem : cachedValidationResult) {
                        content.addValidationObjectToGrid(validationItem);
                    }
                }
                if (sortColumn < 0) {
                    sortColumn = 0;
                    sortAscending = true;
                }
                content.getTable().sortRowsAccordingToColumn(sortColumn, sortAscending);
            }
        }

        @Override
        protected List<? extends ValidationItem> beforeValidateGrid(List<? extends ValidationItem> validationItems) {
            colorTableModelTimeSliceFilterResult.clear();
            DwList<ColortableValidationItem> result = new DwList<>();

            // Als ersten Schritt Daten mit unpassendem ETKZ und Baureihe entfernen
            for (ValidationItem validationItem : validationItems) {
                if (validationItem instanceof ColortableValidationItem) {
                    ColortableValidationItem colortableValidationItem = (ColortableValidationItem)(validationItem);
                    if (colortableValidationItem.isValid() && isValidPreFilter(colortableValidationItem)) {
                        result.add(colortableValidationItem);
                    }

                }
            }
            return super.beforeValidateGrid(result);
        }

        @Override
        protected void validateGrid(List<? extends ValidationItem> validationItems, String selectedModel) {
            iPartsDataPartListEntry partlistEntry = getPartlistEntry();
            iPartsColorTable colorTableForRetailWithoutFilter = partlistEntry.getColorTableForRetailWithoutFilter();
            if (colorTableForRetailWithoutFilter == null) {
                return;
            }

            // Ermitteln, ob an einer relevanten Farbvariantentabelle für den Stücklisteneintrag PEM-Flags gesetzt sind
            // (analog zu iPartsEditValidationHelper.executeColortableQualityChecks())
            Set<iPartsColorTable.ColorTable> colorTablesForPemFlagCheck = iPartsEditValidationHelper.executeColortableQualityCheck0NoVariantTable(partlistEntry);
            colorTablesForPemFlagCheck = iPartsEditValidationHelper.executeColortableQualityCheck1VariantTableInvalid(partlistEntry, colorTablesForPemFlagCheck);
            VarParam<Boolean> hasColorTableWithPemFlags = new VarParam<>(false);
            if (colorTablesForPemFlagCheck != null) {
                iPartsEditValidationHelper.executeColortablePemFlagsChecks(colorTablesForPemFlagCheck, hasColorTableWithPemFlags);
            }

            iPartsColorTable colorTableForRetail =
                    filterForModelEvaluation.getColorTableForRetailFiltered(colorTableForRetailWithoutFilter, partlistEntry);

            if (!hasColorTableWithPemFlags.getValue()) {
                // Ohne gesetzte PEM-Flags alle Farbvariantentabellen zusammen prüfen
                validateValidationItems(validationItems, partlistEntry, selectedModel, colorTableForRetailWithoutFilter, colorTableForRetail);
            } else {
                // Mit gesetzten PEM-Flags jede Farbvariantentabelle einzeln prüfen. Dazu diese nach Farbvariantentabellen gruppieren.
                Map<String, List<ValidationItem>> colorTableToValidationItemsMap = new TreeMap<>();
                for (ValidationItem validationItem : validationItems) {
                    if (validationItem instanceof ColortableValidationItem) {
                        iPartsDataColorTableData colorTableData = ((ColortableValidationItem)validationItem).colorTableData;
                        if (colorTableData != null) {
                            String colorTableId = colorTableData.getAsId().getColorTableId();
                            List<ValidationItem> validationItemsForColorTable = colorTableToValidationItemsMap.computeIfAbsent(colorTableId,
                                                                                                                               id -> new ArrayList<>());
                            validationItemsForColorTable.add(validationItem);
                        }
                    }
                }

                // Jetzt einzeln pro Farbvariantentabelle die dazugehörigen ValidationItems prüfen
                for (List<ValidationItem> validationItemsForColorTable : colorTableToValidationItemsMap.values()) {
                    validateValidationItems(validationItemsForColorTable, partlistEntry, selectedModel, colorTableForRetailWithoutFilter,
                                            colorTableForRetail);
                }
            }
        }

        private void validateValidationItems(List<? extends ValidationItem> validationItems, iPartsDataPartListEntry partlistEntry,
                                             String selectedModel, iPartsColorTable colorTableForRetailWithoutFilter,
                                             iPartsColorTable colorTableForRetail) {
            iPartsDataAssembly ownerAssembly = partlistEntry.getOwnerAssembly();
            Map<String, List<ColortableValidationItem>> evalMapForCheck3and4 = new HashMap<>();
            for (ValidationItem validationItem : validationItems) {
                if (validationItem instanceof ColortableValidationItem) {
                    ColortableValidationItem colortableValidationItem = (ColortableValidationItem)(validationItem);
                    iPartsColorTableToPartId colorTableToPartId = colortableValidationItem.colorTableToPart.getAsId();

                    Boolean filterResult = getModelTimeSliceFilterResult(colorTableToPartId, selectedModel);
                    if (filterResult == null) {
                        // Farbtabelle wurde noch nicht gefiltert -> Baumuster-Zeitscheiben-Filter durchführen
                        filterResult = false;
                        iPartsColorTable.ColorTable colorTable = colorTableForRetailWithoutFilter.getColorTable(colorTableToPartId.getColorTableId());
                        if (colorTable != null) {
                            iPartsColorTable.ColorTableToPart colorTableToPart = colorTable.colorTableToPartsMap.get(colorTableToPartId);
                            if (colorTableToPart != null) {
                                filterResult = iPartsFilter.isColorTableValidForModelTimeSlice(ownerAssembly, colorTableToPart, null);
                            }
                        }
                        setModelTimeSliceFilterResult(colorTableToPartId, selectedModel, filterResult);
                        // Es kann passieren, dass es keine Entsprechung in colorTableForRetailWithoutFilter gibt. Das
                        // ist der Fall, wenn die Farbtabelle zwar auf Grund von Baureihe und ETKZ
                        // gültig war, aber keine passenden Werkseinsatzdaten hatte oder die PEM ab/bis
                        // Termine der Werksdaten der Farbtabelle nicht zu denen des Stücklisteneintrags
                        // gepasst haben. Dann wird sie bereits beim Laden über die Funktion
                        // checkDialogPartListForColorTableData() ausgefiltert.
                    }

                    if (!filterResult) {
                        // Farbtabelle für den Baumuster-Zeitscheiben-Filter nicht gültig -> nicht weiter filtern
                        validationItem.setValidationResultForModel(selectedModel, ValidationResult.MODEL_INVALID);
                    } else {
                        // Farbtabelle ist für den Baumuster-Zeitscheiben-Filter gültig
                        colortableValidationItem.isValidForAnyModelTimeSlice = true;

                        // Jetzt noch die konkreten Farbtabelleninhalte filtern
                        validationItem.setValidationResultForModel(selectedModel, ValidationResult.MODEL_INVALID);
                        if (colorTableForRetail != null) {
                            iPartsColorTable.ColorTable filteredColorTable =
                                    colorTableForRetail.getColorTable(colorTableToPartId.getColorTableId());
                            if (filteredColorTable != null) {
                                for (iPartsColorTable.ColorTableContent filteredColortableContent : filteredColorTable.colorTableContents) {
                                    if (filteredColortableContent.colorTableContentId.equals(colortableValidationItem.colorTableContent.getAsId())) {
                                        colortableValidationItem.isColortableContentValidForAnyModel = true;
                                        String colorNumber = colortableValidationItem.colorTableContent.getColorNumber();
                                        List<ColortableValidationItem> colortableValidationItems = evalMapForCheck3and4.get(colorNumber);
                                        if (colortableValidationItems == null) {
                                            colortableValidationItems = new DwList<>();
                                            evalMapForCheck3and4.put(colorNumber, colortableValidationItems);
                                        }
                                        colortableValidationItem.retailColortableContent = filteredColortableContent;
                                        colortableValidationItems.add(colortableValidationItem);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Prüfung 3
            iPartsSeriesId seriesIdFromPartlistEntry = getPartlistEntry().getSeriesId();
            iPartsDialogSeries series = null;
            if (seriesIdFromPartlistEntry != null) {
                series = iPartsDialogSeries.getInstance(getProject(), seriesIdFromPartlistEntry);
            }
            for (List<ColortableValidationItem> validationItemsForES2 : evalMapForCheck3and4.values()) {
                if (validationItemsForES2.size() == 1) {
                    // Es gibt für diesen ES2 Schlüssel nur eine gültige Farbvariante -> direkt OK setzen
                    validationItemsForES2.get(0).setValidationResultForModel(selectedModel, ValidationResult.OK);
                } else {
                    for (int currentIndex = 0; currentIndex < validationItemsForES2.size(); currentIndex++) {
                        for (int compareIndex = currentIndex + 1; compareIndex < validationItemsForES2.size(); compareIndex++) {
                            ColortableValidationItem currentEntry = validationItemsForES2.get(currentIndex);
                            ColortableValidationItem compareEntry = validationItemsForES2.get(compareIndex);
                            // Nur solche Farbvarianten prüfen, die nicht bereits geprüft wurden
                            if (checkColorTableContents(currentEntry, compareEntry, series)) {
                                setValidationResultOK(currentEntry, compareEntry, selectedModel);
                            } else {
                                // genauen Fehlergrund bestimmen
                                String errorCode = getColorTableContentsPartialConjunctionOverlap(currentEntry, compareEntry);
                                setValidationResult3or4Failed(currentEntry, compareEntry, selectedModel, errorCode, true);
                            }
                        }
                    }
                }
            }

            // Prüfung 4
            Set<String> alreadyCheckedPairs = new HashSet<>(); // Zur Vermeidung von doppelten Überprüfungen
            for (Map.Entry<String, List<ColortableValidationItem>> compareGroup : evalMapForCheck3and4.entrySet()) {
                String colorNumber = compareGroup.getKey();
                // suche alle Einträge mit anderem ES2 Schlüssel zusammen
                List<ColortableValidationItem> otherES2 = new DwList<>();
                for (Map.Entry<String, List<ColortableValidationItem>> entry : evalMapForCheck3and4.entrySet()) {
                    if (!entry.getKey().equals(colorNumber)) {
                        otherES2.addAll(entry.getValue());
                    }
                }

                // jeden Eintrag aus dieser Liste mit jedem aus otherES2 vergleichen
                for (ColortableValidationItem validationItem : compareGroup.getValue()) {
                    for (ColortableValidationItem compareValidationItem : otherES2) {
                        String validationItemPairKey;
                        String validationItemString = validationItem.retailColortableContent.colorTableContentId.toDBString();
                        String compareValidationItemString = compareValidationItem.retailColortableContent.colorTableContentId.toDBString();
                        if (validationItemString.compareTo(compareValidationItemString) <= 0) {
                            validationItemPairKey = validationItemString + CACHE_KEY_DELIMITER + compareValidationItemString;
                        } else {
                            validationItemPairKey = compareValidationItemString + CACHE_KEY_DELIMITER + validationItemString;
                        }
                        if (!alreadyCheckedPairs.contains(validationItemPairKey)) {
                            if (checkColorTableContents(validationItem, compareValidationItem, series)) {
                                setValidationResultOK(validationItem, compareValidationItem, selectedModel);
                            } else {
                                // genauen Fehlergrund bestimmen
                                String errorCode = getColorTableContentsPartialConjunctionOverlap(validationItem, compareValidationItem);
                                setValidationResult3or4Failed(validationItem, compareValidationItem, selectedModel, errorCode, false);
                            }
                            alreadyCheckedPairs.add(validationItemPairKey);
                        }
                    }
                }
            }
        }

        private void setValidationResult3or4Failed(ColortableValidationItem currentEntry, ColortableValidationItem compareEntry,
                                                   String model, String errorCode, boolean isCheck3) {

            String colorTableId = currentEntry.colorTableToPart.getAsId().getColorTableId();
            boolean isUserManual = ColorTableHelper.isUserManualColorTable(colorTableId);

            ValidationResult validationResult;
            if (isCheck3 || isUserManual) {
                validationResult = ValidationResult.WARNING;
            } else {
                validationResult = ValidationResult.ERROR;
            }

            currentEntry.setValidationResultForModel(model, validationResult);
            compareEntry.setValidationResultForModel(model, validationResult);

            currentEntry.setAdditionalErrorText(currentEntry, compareEntry, model, errorCode, isCheck3, isUserManual);
            compareEntry.setAdditionalErrorText(compareEntry, currentEntry, model, errorCode, isCheck3, isUserManual);
        }

        private void setValidationResultOK(ColortableValidationItem currentEntry, ColortableValidationItem compareEntry,
                                           String selectedModel) {
            currentEntry.setValidationResultForModel(selectedModel, ValidationResult.OK);
            compareEntry.setValidationResultForModel(selectedModel, ValidationResult.OK);
        }

        private String getColorTableContentsPartialConjunctionOverlap(ColortableValidationItem firstEntry, ColortableValidationItem secondEntry) {
            iPartsColorTable.ColorTableContent firstRetailData = firstEntry.retailColortableContent;
            iPartsColorTable.ColorTableContent secondRetailData = secondEntry.retailColortableContent;
            if ((firstRetailData == null) || (secondRetailData == null)) {
                return null;
            }
            return iPartsEditValidationHelper.isPartialConjunctionOverlapWithErrorMessage(firstRetailData.code,
                                                                                          secondRetailData.code);
        }

        private boolean checkColorTableContents(ColortableValidationItem firstEntry, ColortableValidationItem secondEntry,
                                                iPartsDialogSeries series) {
            iPartsColorTable.ColorTableContent firstRetailData = firstEntry.retailColortableContent;
            iPartsColorTable.ColorTableContent secondRetailData = secondEntry.retailColortableContent;
            if ((firstRetailData == null) || (secondRetailData == null)) {
                return false;
            }

            return iPartsEditValidationHelper.executeColortableQualityCheck3and4Overlap(firstRetailData, secondRetailData,
                                                                                        series);
        }

        @Override
        protected List<? extends ValidationItem> afterValidateGrid(List<? extends ValidationItem> validationItems) {
            // Gleiche Farbtabellen mit unteschiedlichem KEM Stand zusammenführen
            validationItems = compressColorTables(validationItems);

            // Alles, was das Flag isValidForAnyModelTimeSlice == false hat, entfernen
            Map<String, ColortableValidationItem> removedColorTables = new HashMap<>();
            Set<String> validColorTables = new HashSet<>();
            List<ColortableValidationItem> result = new DwList<>();
            for (ValidationItem validationItem : validationItems) {
                if (validationItem instanceof ColortableValidationItem) {
                    ColortableValidationItem colortableValidationItem = (ColortableValidationItem)(validationItem);
                    String colorTableId = colortableValidationItem.colorTableToPart.getAsId().getColorTableId();
                    if (colortableValidationItem.isValidForAnyModelTimeSlice) {
                        result.add(colortableValidationItem);
                        validColorTables.add(colorTableId);
                    } else {
                        removedColorTables.put(colorTableId, colortableValidationItem);
                    }
                }
            }

            // Jetzt die oben entfernten Farbtabellen als einzelne Zeilen ohne Farbtabelleninhalt hinzufügen
            for (ColortableValidationItem removedItem : removedColorTables.values()) {
                if (!validColorTables.contains(removedItem.colorTableToPart.getAsId().getColorTableId())) {
                    ColortableValidationItem copy = new ColortableValidationItem(removedItem.colorTableData, removedItem.colorTableToPart);
                    for (String model : getModelsForEvaluation()) {
                        copy.setValidationResultForModel(model, ValidationResult.MODEL_INVALID);
                    }
                    result.add(copy);
                }
            }

            List<? extends ValidationItem> finalValidationResult = super.afterValidateGrid(result);
            cachedValidationResult = finalValidationResult;
            return finalValidationResult;
        }

        @Override
        protected List<? extends ValidationItem> createValidationItemList() {
            List<ColortableValidationItem> result = new DwList<>();
            iPartsDataPartListEntry partlistEntry = getPartlistEntry();
            List<ColorTableHelper.VariantTablesDataStructure> variantTablesDataStructures =
                    ColorTableHelper.getVariantTablesDataForPartListEntry(getProject(), partlistEntry, null, null, true, false);

            for (ColorTableHelper.VariantTablesDataStructure variantTablesDataStructure : variantTablesDataStructures) {
                iPartsColorTableDataId variantTableId =
                        new iPartsColorTableDataId(variantTablesDataStructure.colorTableData.getFieldValue(iPartsConst.FIELD_DCTD_TABLE_ID));

                List<ColorTableHelper.VariantsDataStructure> variantsDataStructures =
                        ColorTableHelper.getVariantsDataForVariantTableId(getProject(), partlistEntry, variantTableId, null,
                                                                          variantTablesDataStructure.colorTableData, true, false);

                for (ColorTableHelper.VariantsDataStructure variantsDataStructure : variantsDataStructures) {
                    result.add(new ColortableValidationItem(variantTablesDataStructure.colorTableData,
                                                            variantTablesDataStructure.colorTableToPart,
                                                            variantsDataStructure.colorTableContent,
                                                            variantsDataStructure.colorNumber));
                }
            }
            return result;
        }

        @Override
        protected String getVisualValueOfValidationResult(ValidationResult result) {
            if (result != null) {
                return result.getDbValue();
            }
            return "";
        }

        @Override
        protected String getAdditionalInfoText(ValidationItem validationItem, String model) {
            if (validationItem instanceof ColortableValidationItem) {
                ColortableValidationItem colortableValidationItem = (ColortableValidationItem)validationItem;
                if (!colortableValidationItem.isValidForAnyModelTimeSlice) {
                    if (colortableValidationItem.colorTableToPart != null) {
                        return TranslationHandler.translate("!!Prüfung 1: Variantentabelle \"%1\" ist zu keinem Baumuster gültig",
                                                            colortableValidationItem.colorTableToPart.getAsId().getColorTableId());
                    } else {
                        return TranslationHandler.translate("!!Prüfung 1: Variantentabelle ist zu keinem Baumuster gültig");
                    }
                } else {
                    if (!colortableValidationItem.isColortableContentValidForAnyModel) {
                        if (colortableValidationItem.colorTableContent != null) {
                            return TranslationHandler.translate("!!Prüfung 2: Variante \"%1\" ist zu keinem Baumuster gültig",
                                                                colortableValidationItem.colorTableContent.getColorNumber());
                        } else {
                            return TranslationHandler.translate("!!Prüfung 2: Variante ist zu keinem Baumuster gültig");
                        }
                    }
                }

                if (StrUtils.isValid(model)) {
                    ValidationResult validationResultForModel = colortableValidationItem.getValidationResultForModel(model);
                    if (validationResultForModel != ValidationResult.OK) {
                        return colortableValidationItem.getResultTextForModel(model);
                    }
                }
            }
            return "";
        }

        private class ColortableValidationItem extends GenericValidationItem {

            private iPartsDataColorTableData colorTableData;
            private iPartsDataColorTableToPart colorTableToPart;
            private iPartsDataColorTableContent colorTableContent;
            private iPartsDataColorNumber colorNumber;
            private boolean isValidForAnyModelTimeSlice; // Prüfung 1
            private boolean isColortableContentValidForAnyModel; // Prüfung 2
            private iPartsColorTable.ColorTableContent retailColortableContent; // Prüfungen 3 und 4 arbeiten mit den Retaildaten
            private Map<String, String> validationResultTextForModel; // Infotext für Prüfungen 3 und 4

            ColortableValidationItem(iPartsDataColorTableData colorTableData, iPartsDataColorTableToPart colorTableToPart) {
                this(colorTableData, colorTableToPart,
                     new iPartsDataColorTableContent(getProject(), new iPartsColorTableContentId()),
                     new iPartsDataColorNumber(getProject(), new iPartsColorNumberId()));
            }

            ColortableValidationItem(iPartsDataColorTableData colorTableData, iPartsDataColorTableToPart colorTableToPart,
                                     iPartsDataColorTableContent colorTableContent, iPartsDataColorNumber colorNumber) {
                super(null);
                this.colorTableData = colorTableData;
                this.colorTableToPart = colorTableToPart;
                this.colorTableContent = colorTableContent;
                this.colorNumber = colorNumber;
                this.validationResultTextForModel = new HashMap<>();
            }

            @Override
            public List<EtkDataObject> getDataObjects() {
                DwList<EtkDataObject> result = new DwList<>(4);
                if (colorTableData != null) {
                    result.add(colorTableData);
                }
                if (colorTableToPart != null) {
                    result.add(colorTableToPart);
                }
                if (colorTableContent != null) {
                    result.add(colorTableContent);
                }
                if (colorNumber != null) {
                    result.add(colorNumber);
                }
                return result;
            }

            public boolean isValid() {
                return (colorNumber != null) && (colorTableContent != null) && (colorTableToPart != null) && (colorTableData != null);
            }

            public String getResultTextForModel(String model) {
                return validationResultTextForModel.get(model);
            }

            private void setAdditionalErrorText(ColortableValidationItem currentEntry, ColortableValidationItem compareEntry,
                                                String model, String errorCode, boolean isCheck3, boolean isUserManual) {
                if ((currentEntry.retailColortableContent == null) || (compareEntry.retailColortableContent == null)) {
                    return;
                }
                String existingText = validationResultTextForModel.get(model);

                StringBuilder message = new StringBuilder();
                if (existingText != null) {
                    message.append(existingText);
                    message.append("\n\n");
                }
                if (isCheck3) {
                    message.append(createAdditionalTextForCheck3(currentEntry, model, errorCode));
                } else {
                    message.append(createAdditionalTextForCheck4(compareEntry, model, errorCode));
                }

                message.append("\n");
                message.append(TranslationHandler.translate("!!- Code der ausgewählten Variante: %1",
                                                            currentEntry.retailColortableContent.code));
                message.append("\n");
                message.append(TranslationHandler.translate("!!- Code der Vergleichs-Variante: %1",
                                                            compareEntry.retailColortableContent.code));

                if (isUserManual) {
                    message.append("\n");
                    message.append(TranslationHandler.translate("!!Es handelt sich um eine Bedienungsanleitung"));
                }

                validationResultTextForModel.put(model, message.toString());
            }

            private String createAdditionalTextForCheck3(ColortableValidationItem currentEntry, String model, String errorCode) {
                return TranslationHandler.translate("!!Prüfung 3: Doppelte Teilkonjunktion \"%1\" für " +
                                                    "Baumuster \"%2\" mit gleichem ES2-Schlüssel \"%3\" gefunden", errorCode,
                                                    model, currentEntry.retailColortableContent.colorNumber);
            }

            private String createAdditionalTextForCheck4(ColortableValidationItem compareEntry, String model, String errorCode) {
                return TranslationHandler.translate("!!Prüfung 4: Doppelte Teilkonjunktion \"%1\" für " +
                                                    "Baumuster \"%2\" mit unterschiedlichem ES2-Schlüssel \"%3\" gefunden",
                                                    errorCode, model, compareEntry.retailColortableContent.colorNumber);
            }

            @Override
            public void setValidationResultForModel(String model, ValidationResult result) {
                if (result == ValidationResult.OK) {
                    ValidationResult existingResult = getValidationResultForModel(model);
                    if ((existingResult != ValidationResult.WARNING) && (existingResult != ValidationResult.ERROR)) {
                        super.setValidationResultForModel(model, result);
                    }
                } else {
                    super.setValidationResultForModel(model, result);
                }
            }
        }
    }
}
