/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.validation;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DataCardRetrievalException;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.VehicleDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsWWPartsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFootNote;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacement;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo.iPartsRelatedInfoModelEvaluationForm;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkDataArray;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;


public class iPartsFINValidationForm extends iPartsEditAssemblyListValidationForm {

    private static final String FIN_VALIDATION_KEY_DELIMITER = "&";

    private List<VehicleDataCard> dataCards = new DwList<>();
    private String dataCardErrorMessage;
    private Map<EtkDataPartListEntry, String> partListEntry2ErrorMap = new LinkedHashMap<>();

    private ValidationResult totalValidationResult = ValidationResult.OK;

    public iPartsFINValidationForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, boolean removeAdditionalInfoPanel, boolean minimizeAdditionalInfoPanel) {
        super(dataConnector, parentForm, removeAdditionalInfoPanel, minimizeAdditionalInfoPanel);
    }

    @Override
    protected boolean logPerformanceMessages() {
        return false;
    }

    @Override
    public EtkDisplayFields getDisplayFields(String configKey) {
        return super.getDisplayFields(configKey);
    }

    @Override
    protected void beforeValidatePartList(List<EtkDataPartListEntry> partList, iPartsDataAssembly assembly) {
        totalValidationResult = ValidationResult.OK;
        if (dataCards != null) {
            for (VehicleDataCard dataCard : dataCards) {
                if (isCancelled()) {
                    return;
                }

                // gefilterte Stückliste pro Datenkarte erzeugen
                filterForModelEvaluation.setAllRetailFilterActiveForDataCard(getProject(), dataCard, true);
                List<EtkDataPartListEntry> partListFiltered = new DwList<>(partList.size());

                for (EtkDataPartListEntry partListEntry : partList) {
                    if (filterForModelEvaluation.checkFilter(partListEntry)) {
                        partListFiltered.add(partListEntry);
                    }
                }

                // Prüfungen aufrufen
                ValidationResult validationResultDuplicates = doValidationDuplicateEntries(partList, partListFiltered, dataCard);
                ValidationResult validationResultInvalidEntries = doValidationNoValidEntries(partList, partListFiltered, dataCard);
                setTotalValidationResult(validationResultDuplicates, validationResultInvalidEntries);

                // Gefilterte Werkseinsatzdaten und darin v.a. auch das für die Filterung verwendete Baumuster der FIN zurücksetzen
                if (assembly != null) {
                    assembly.clearAllFactoryDataForRetailForPartList();
                }
            }
        }

        // Zum Schluss den Filtergrund wieder löschen (lieber in einer eigenen Schleife, da Aufrufe von checkFilter()
        // durchaus auch den Filtergrund von anderen Stücklisteneinträgen setzen können
        for (EtkDataPartListEntry partListEntry : partList) {
            filterForModelEvaluation.clearFilterReasonForDataObject(partListEntry, true);
        }
    }

    private void setTotalValidationResult(ValidationResult validationResultDuplicates, ValidationResult validationResultInvalidEntries) {
        if (validationResultDuplicates == ValidationResult.ERROR) {
            totalValidationResult = ValidationResult.ERROR;
            return;
        }
        if (validationResultInvalidEntries == ValidationResult.WARNING) {
            if (totalValidationResult != ValidationResult.ERROR) {
                totalValidationResult = ValidationResult.WARNING;
            }
            return;
        }
        if ((totalValidationResult != ValidationResult.ERROR) && (totalValidationResult != ValidationResult.WARNING)) {
            totalValidationResult = ValidationResult.OK;
        }
    }

    private ValidationResult doValidationDuplicateEntries(List<EtkDataPartListEntry> partlist, List<EtkDataPartListEntry> partlistFiltered, VehicleDataCard dataCard) {
        // Kriterien für Doppeltreffer:
        // Hotspot, Lenkung, komb. Text, WW, Ersetzung (gleicher Nachfolger), Fußnoten ID

        // erstmal alle aus der ungefilterten Liste auf ungültig setzen
        String fin = dataCard.getFinId().getFIN();
        String finField = createVirtualFieldNameForModelOrFINEvaluation(fin);
        for (EtkDataPartListEntry partListEntry : partlist) {
            partListEntry.setFieldValue(finField, ValidationResult.MODEL_INVALID.getDbValue(), DBActionOrigin.FROM_DB);
        }

        // dann alle aus der gefilterten Liste auf gültig setzen
        for (EtkDataPartListEntry partListEntry : partlistFiltered) {
            partListEntry.setFieldValue(finField, ValidationResult.OK.getDbValue(), DBActionOrigin.FROM_DB);
        }

        Map<String, List<EtkDataPartListEntry>> collectedEntries = new LinkedHashMap<>();

        for (EtkDataPartListEntry partListEntry : partlistFiltered) {
            iPartsDataPartListEntry iPartsEntry = partListEntry instanceof iPartsDataPartListEntry ? ((iPartsDataPartListEntry)partListEntry) : null;
            if (iPartsEntry != null) {
                String hotspot = iPartsEntry.getFieldValue(FIELD_K_POS);
                String steering = iPartsEntry.getFieldValue(FIELD_K_STEERING);
                Collection<EtkDataPartListEntry> wwParts = iPartsWWPartsHelper.getRealWWParts(iPartsEntry, filterForModelEvaluation);

                // Alle Wahlweise Teile aufsammeln
                List<String> wwPartsList = new DwList<>(wwParts.size());
                wwParts.forEach(wwPart -> wwPartsList.add(wwPart.getPart().getAsId().getMatNr()));
                String ww = StrUtils.stringListToString(wwPartsList, ",");

                String combinedText = iPartsEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT);

                String footNoteKey = "";
                Collection<iPartsFootNote> footNotesForRetail = iPartsEntry.getFootNotesForRetail();
                if (footNotesForRetail != null) {
                    List<String> footNodeIds = new ArrayList<>(footNotesForRetail.size());
                    footNotesForRetail.forEach(iPartsFootNote -> footNodeIds.add(iPartsFootNote.getFootNoteId().getFootNoteId()));
                    footNoteKey = StrUtils.stringListToString(footNodeIds, ",");
                }

                String nextSuccessorPartNo = "";
                List<iPartsReplacement> successors = iPartsEntry.getSuccessors(filterForModelEvaluation);
                if ((successors != null) && !successors.isEmpty()) {
                    nextSuccessorPartNo = successors.get(0).successorPartNumber;
                }

                String partListEntryKey = createValidationKey(hotspot, steering, ww, combinedText, footNoteKey, nextSuccessorPartNo);

                collectedEntries.computeIfAbsent(partListEntryKey, s -> new ArrayList<>());
                collectedEntries.get(partListEntryKey).add(partListEntry);
            }
        }

        ValidationResult duplicatesResult = ValidationResult.OK;
        // alle Einträge die Doppeltreffer haben wieder auf ungültig setzen
        for (Map.Entry<String, List<EtkDataPartListEntry>> entry : collectedEntries.entrySet()) {
            List<EtkDataPartListEntry> partListEntries = entry.getValue();
            if (partListEntries.size() > 1) {
                List<String> entryIDs = new DwList<>();
                for (EtkDataPartListEntry partListEntry : partListEntries) {
                    entryIDs.add(partListEntry.getAsId().getKLfdnr());
                    partListEntry.setFieldValue(finField, ValidationResult.ERROR.getDbValue(), DBActionOrigin.FROM_DB);
                }
                String errorText = createDuplicateEntriesText(entryIDs, fin);
                for (EtkDataPartListEntry partListEntry : partListEntries) {
                    addError(partListEntry, errorText);
                }
                duplicatesResult = ValidationResult.ERROR;
            }
        }
        return duplicatesResult;
    }

    private String createValidationKey(String hotspot, String steering, String ww, String combinedText, String footNoteKey, String nextSuccessorPartNo) {
        StringBuilder sb = new StringBuilder();
        sb.append(hotspot);
        sb.append(FIN_VALIDATION_KEY_DELIMITER);
        sb.append(steering);
        sb.append(FIN_VALIDATION_KEY_DELIMITER);
        sb.append(combinedText);
        sb.append(FIN_VALIDATION_KEY_DELIMITER);
        sb.append(footNoteKey);
        sb.append(FIN_VALIDATION_KEY_DELIMITER);
        sb.append(nextSuccessorPartNo);
        sb.append(FIN_VALIDATION_KEY_DELIMITER);
        sb.append(ww);
        return sb.toString();
    }

    private String createDuplicateEntriesText(List<String> entryIDs, String fin) {
        String collectedIds = StrUtils.stringListToString(entryIDs, ", ");
        return TranslationHandler.translate("!!Doppeltreffer für %1 mit laufender Nummer: \"%2\"", fin, collectedIds);
    }

    private ValidationResult doValidationNoValidEntries(List<EtkDataPartListEntry> partList, List<EtkDataPartListEntry> partListFiltered, VehicleDataCard dataCard) {
        // Alle Hotspots aufsammeln und lfdNr der Stücklisteneinträge pro Hotspot merken
        Map<String, DwList<EtkDataPartListEntry>> hotspot2partListEntries = new LinkedHashMap<>();
        for (EtkDataPartListEntry partListEntry : partList) {
            String hotspot = partListEntry.getFieldValue(EtkDbConst.FIELD_K_POS);
            hotspot2partListEntries.putIfAbsent(hotspot, new DwList<>());
            hotspot2partListEntries.get(hotspot).add(partListEntry);
        }

        // nach Filterung die Hotspots der noch sichtbaren Stücklisteneinträge entfernen, die verbleibenden Hotspots haben keine Treffer
        for (EtkDataPartListEntry partListEntry : partListFiltered) {
            String hotspot = partListEntry.getFieldValue(EtkDbConst.FIELD_K_POS);
            hotspot2partListEntries.remove(hotspot);
        }

        String fin = dataCard.getFinId().getFIN();
        String virtualFieldName = createVirtualFieldNameForModelOrFINEvaluation(fin);
        ValidationResult totalResult = ValidationResult.OK;
        for (Map.Entry<String, DwList<EtkDataPartListEntry>> mapEntry : hotspot2partListEntries.entrySet()) {
            List<EtkDataPartListEntry> errorEntries = mapEntry.getValue();
            for (EtkDataPartListEntry entry : errorEntries) {
                DBDataObjectAttribute attribute = entry.getAttribute(virtualFieldName);
                // Eigenen Fehler nur setzen, wenn bei der 1. Prüfung (doValidationDuplicateEntries) alles korrekt war
                if (StrUtils.stringEquals(attribute.getAsString(), ValidationResult.MODEL_INVALID.getDbValue())) {
                    String validationResult = ValidationResult.INVALID_HOTSPOT.getDbValue();
                    attribute.setAsString(validationResult, DBActionOrigin.FROM_DB);
                }
                addError(entry, TranslationHandler.translate("!!Ungültiger Hotspot '%1' für %2 (alle Teilepositionen ausgefiltert)",
                                                             mapEntry.getKey(), fin));
                totalResult = ValidationResult.WARNING;
            }
        }
        return totalResult;
    }

    private void addError(EtkDataPartListEntry entry, String error) {
        String newError = error;
        String existingError = partListEntry2ErrorMap.get(entry);
        if (StrUtils.isValid(existingError)) {
            newError = existingError + "\n" + newError;
        }
        partListEntry2ErrorMap.put(entry, newError);
    }

    @Override
    protected void validatePartList(List<EtkDataPartListEntry> partlist, iPartsDataAssembly assembly, String selectedModel) {
        // wird nicht aufgerufen, weil keine Models ausgewählt wurden -> eigentlich Auswertung erfolgt in beforeValidatePartlist
    }

    @Override
    protected void updateDisplayFields(boolean getSelectedModelsFromSession) {
        if (loadDataCards()) {
            // es gibt mind. eine FIN am Produkt; alle erfolgreich geladenen FINS anzeigen
            Set<String> finSet = new LinkedHashSet<>(); // Reihenfolge aus den Produkt-Stammdaten beibehalten
            for (VehicleDataCard dataCard : dataCards) {
                finSet.add(dataCard.getFinId().getFIN());
            }
            additionalDisplayFields = getVirtualModelOrFINFields(finSet, true);
            // falls beim Laden mind. einer Datenkarte ein Fehler aufgetreten ist, die Meldung anzeigen
            if (StrUtils.isValid(dataCardErrorMessage)) {
                EtkMessageLog externalMessageLog = getExternalMessageLog();
                if (externalMessageLog != null) {
                    externalMessageLog.fireMessage(dataCardErrorMessage);
                } else {
                    MessageDialog.showWarning(dataCardErrorMessage);
                }
            }
        } else {
            // falls das Laden aller Datenkarten fehlgeschlagen ist, den Dialog leeren und stattdessen eine Meldung anzeigen
            dataCards = null;
            Session.invokeThreadSafeInSession(() -> {
                getGui().removeAllChildren();
                GuiLabel noValidationPossibleLabel = new GuiLabel(dataCardErrorMessage);
                noValidationPossibleLabel.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
                noValidationPossibleLabel.setPadding(8);
                getGui().addChild(noValidationPossibleLabel);
            });
        }
    }

    @Override
    public String createVirtualFieldNameForModelOrFINEvaluation(String modelNumber) {
        return VirtualFieldsUtils.addVirtualFieldMask(iPartsDataVirtualFieldsDefinition.DA_FIN_EVALUATION
                                                      + iPartsDataVirtualFieldsDefinition.DA_FIN_EVALUATION_SPACER
                                                      + modelNumber);
    }

    @Override
    protected String getDisplayFieldConfigKey(String partListType) {
        if ((partListType.equals(iPartsConst.PARTS_LIST_TYPE_DIALOG_RETAIL))) {
            return iPartsRelatedInfoModelEvaluationForm.CONFIG_KEY_MODEL_EVALUATION_DIALOG;
        } else if (partListType.equals(iPartsConst.PARTS_LIST_TYPE_EDS_RETAIL)) {
            return iPartsRelatedInfoModelEvaluationForm.CONFIG_KEY_MODEL_EVALUATION_EDS;
        }

        return null;
    }

    @Override
    protected boolean isValidationForPartListPossible(iPartsDataAssembly assembly) {
        return true; // Nur simple Baumuster-Auswertung
    }

    public void startFINValidation() {
        updateData(this.parentForm, true);
    }

    /**
     * Vorprüfung zu {@link #loadDataCards()}.
     *
     * @return <code>true</code> Falls am aktuellen Produkt mind. eine FINs/VINs gespeichert ist.
     */
    public boolean FINsExistForProduct() {
        EtkDataAssembly assembly = EditModuleHelper.getAssemblyFromConnector(getConnector());
        if (assembly != null) {
            assembly = assembly.getLastHiddenSingleSubAssemblyOrThis(null);
            if (assembly instanceof iPartsDataAssembly) {
                iPartsProductId productId = ((iPartsDataAssembly)assembly).getProductIdFromModuleUsage();
                if (productId != null) {
                    iPartsDataProduct dataProduct = new iPartsDataProduct(getProject(), productId);
                    EtkDataArray finVinArray = dataProduct.getFieldValueAsArray(FIELD_DP_FINS);
                    return !finVinArray.isEmpty();
                }
            }
        }
        return false;
    }

    /**
     * Lädt alle Datenkarten zu den am Produkt gespeicherten FINs.
     * Sollte es dabei bei einer Datenkarte zu einem Fehler kommen wird ein entsprechender Hinweis als MessageDialog
     * angezeigt, und diese FINs/VINs nicht für die Auswertung verwendet.
     * Falls keine FINs/VINs am Produkt gespeichert sind, oder alle gespeicherten nicht geladen werden konnten, wird außerdem
     * <code>false</code> zurück geliefert, damit darauf entsprechend reagiert werden kann.
     *
     * @return <code>false</code> Falls es am Produkt keine FINs/VINs gibt, oder alle Datenkarten zu den gespeicherten FINs
     * nicht geladen werden konnten.
     */
    public boolean loadDataCards() {
        iPartsProductId productId = getProductIdForModelStorage();
        if (productId != null) {
            iPartsDataProduct dataProduct = new iPartsDataProduct(getProject(), productId);
            EtkDataArray finOrVinArray = dataProduct.getFieldValueAsArray(FIELD_DP_FINS);
            if ((finOrVinArray == null) || finOrVinArray.isEmpty()) {
                dataCardErrorMessage = TranslationHandler.translate("!!Für Produkt \"%1\" konnten keine gespeicherten FINs oder VINs gefunden werden",
                                                                    productId.getProductNumber());
                return false;
            }
            dataCards.clear();
            Set<String> errorFins = new HashSet<>();
            List<String> finVinList = finOrVinArray.getArrayAsStringList();
            for (String finVin : finVinList) {
                try {
                    VehicleDataCard vehicleDataCard = VehicleDataCard.getVehicleDataCard(
                            finVin, false, true, false,
                            null, getProject(), true);
                    if (vehicleDataCard.isDataCardLoaded()) {
                        dataCards.add(vehicleDataCard);
                    } else {
                        errorFins.add(finVin);
                    }
                } catch (DataCardRetrievalException e) {
                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DATACARD_SERVICE, LogType.ERROR, e);
                    errorFins.add(finVin);
                }
            }
            if (!errorFins.isEmpty()) {
                if (errorFins.size() == finVinList.size()) {
                    dataCardErrorMessage = TranslationHandler.translate("!!Es konnte keine Datenkarte zu den hinterlegten FINs oder VINs für Produkt \"%1\" geladen werden",
                                                                        productId.getProductNumber());
                    return false;
                } else {
                    dataCardErrorMessage = TranslationHandler.translate("!!Fehler beim Laden der Datenkarten für folgende FINs bzw. VINs:%1%2",
                                                                        "\n", StrUtils.stringListToString(errorFins, ", "));
                }
            }
        }
        return true;
    }

    @Override
    protected String getAdditionalInfoText(EtkDataPartListEntry partListEntry, String model) {
        return partListEntry2ErrorMap.get(partListEntry);
    }

    /**
     * Wird für "Überprüfung aller TUs in einem Produkt" benötigt
     *
     * @return
     */
    public Map<EtkDataPartListEntry, String> getPartListEntry2ErrorMap() {
        return partListEntry2ErrorMap;
    }

    public ValidationResult getTotalValidationResult() {
        return totalValidationResult;
    }
}

