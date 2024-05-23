/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.truckbomfoundation.helper;

import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getModelElementUsage.TruckBOMModelElementUsageVersion;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.truckbomfoundation.importer.AbstractTruckBOMFoundationJSONImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;

import java.util.*;

/**
 * Hilfsklasse um Daten aus der TruckBOMFoundation Lieferung zu korrigieren bzw. anzugleichen
 */
public class TruckBOMFoundationDataCorrectionHelper {

    private static final Map<String, String> STEERING_MAPPING = new HashMap<>(); // Mapping Ziffern auf Lenkungswert

    static {
        STEERING_MAPPING.put("0", "");
        STEERING_MAPPING.put("1", "L");
        STEERING_MAPPING.put("2", "R");
    }

    private final List<FieldPairAndValues> fieldsAndValues;
    private final AbstractTruckBOMFoundationJSONImporter importer;

    public TruckBOMFoundationDataCorrectionHelper(AbstractTruckBOMFoundationJSONImporter importer,
                                                  String refFromField, String refToField,
                                                  String... additionalInfiniteValues) {
        this.importer = importer;
        this.fieldsAndValues = new ArrayList<>();
        addFields(refFromField, refToField, additionalInfiniteValues);
    }

    public void addFields(String refFromField, String refToField, String... additionalInfiniteValues) {
        fieldsAndValues.add(new FieldPairAndValues(refFromField, refToField, additionalInfiniteValues));
    }

    /**
     * Korrigiert die Kette der Datensätze auf Basis ihrer Änderungsstände
     *
     * @param allDataObjects
     */
    public void correctDBDataRevisionChain(EtkDataObjectList<? extends EtkDataObject> allDataObjects) {
        EtkDataObject previousDataSet = null;
        for (EtkDataObject currentData : allDataObjects) {
            if (previousDataSet != null) {
                // Hat der vorherige Datensatz "Bis" Wert, der nicht zum "Ab" Wert des aktuellen Datensatzes passt,
                // dann muss das beim vorherigen Datensatz korrigiert werden
                final EtkDataObject finalPreviousDataSet = previousDataSet;
                boolean foundInvalidChainValue = fieldsAndValues.stream().anyMatch(fieldsAndValue -> {
                    String fromFieldValueCurrent = currentData.getFieldValue(fieldsAndValue.getFromField());
                    String toFieldValuePrevious = finalPreviousDataSet.getFieldValue(fieldsAndValue.getToField());
                    return !fromFieldValueCurrent.equals(toFieldValuePrevious);
                });

                // Es gab eine Abweichung -> korrigieren
                if (foundInvalidChainValue) {
                    transferData(currentData, previousDataSet);
                    importer.saveToDB(previousDataSet);
                }
            }

            previousDataSet = currentData;
        }
    }

    private void transferDataFromOneObjectToOther(EtkDataObject sourceObject, EtkDataObject targetObject,
                                                  String fromDataField, String toDataField) {
        String fromValue = sourceObject.getFieldValue(fromDataField);
        if (StrUtils.isValid(fromValue)) {
            targetObject.setFieldValue(toDataField, fromValue, DBActionOrigin.FROM_EDIT);
        }
    }

    private void transferData(EtkDataObject currentData, EtkDataObject previousData) {
        fieldsAndValues.forEach(fieldPairAndValue -> transferDataFromOneObjectToOther(currentData, previousData, fieldPairAndValue.getFromField(),
                                                                                      fieldPairAndValue.getToField()));
    }

    public static String extractSteeringValue(TruckBOMModelElementUsageVersion modelElementUsageVersion) {
        return getSteeringType(modelElementUsageVersion.getSteeringType());
    }

    /**
     * Liefert den Lenkungstyp zum übergebenen Lenkungswert
     *
     * @param steeringValue
     * @return
     */
    private static String getSteeringType(String steeringValue) {
        String steeringType = null;
        if (StrUtils.isValid(steeringValue) && STEERING_MAPPING.containsKey(steeringValue)) {
            steeringType = STEERING_MAPPING.get(steeringValue);
        }
        if (steeringType == null) {
            return "";
        }
        return steeringType;
    }

    /**
     * Hilfsklasse mit dem "ab"- und dem "bis"-Feld. Optional können neben "leer" weitere Werte, die als Platzhalter
     * für "unendlich" dienen hinzugefügt werden
     */
    private static class FieldPairAndValues {

        private final String fromField;
        private final String toField;
        private final Set<String> infiniteValues;

        public FieldPairAndValues(String fromField, String toField, String... additionalInfiniteValues) {
            this.fromField = fromField;
            this.toField = toField;
            this.infiniteValues = new HashSet<>();
            if (additionalInfiniteValues != null) {
                infiniteValues.addAll(Arrays.asList(additionalInfiniteValues));
            }
        }

        public String getFromField() {
            return fromField;
        }

        public String getToField() {
            return toField;
        }

        public Set<String> getInfiniteValues() {
            return infiniteValues;
        }
    }
}
