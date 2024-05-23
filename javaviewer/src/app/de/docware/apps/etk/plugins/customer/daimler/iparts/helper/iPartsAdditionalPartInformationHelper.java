/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsCustomProperty;
import de.docware.framework.utils.EtkMultiSprache;

import java.util.*;
import java.util.stream.Collectors;

import static de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst.*;

/**
 * Helfer für die zusätzlichen PRIMUS Eigenschaften an einem Material
 */
public class iPartsAdditionalPartInformationHelper {

    /**
     * Die Inhalte dieser Felder kommen immer aus Primus, daher wird in
     * {@link de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSAdditionalPartInformationHelper#fillAdditionalPartInformation(EtkProject, EtkDataObject, String, String, List, iPartsCustomProperty)}
     * für diese Felder die Source hardcoded ausgegeben. Sollten in Zukunft hier Felder verwendet werden, die nicht aus
     * Primus kommen, muss dieses Vorgehen angepasst werden.
     */
    public static final Map<String, String> ADDITIONAL_PART_INFORMATION_DB_FIELDS;

    static {
        ADDITIONAL_PART_INFORMATION_DB_FIELDS = new TreeMap<>();
        ADDITIONAL_PART_INFORMATION_DB_FIELDS.put(FIELD_M_HEIGHT, "height");
        ADDITIONAL_PART_INFORMATION_DB_FIELDS.put(FIELD_M_WIDTH, "width");
        ADDITIONAL_PART_INFORMATION_DB_FIELDS.put(FIELD_M_LENGTH, "length");
        ADDITIONAL_PART_INFORMATION_DB_FIELDS.put(FIELD_M_WEIGHT, "weight");
        ADDITIONAL_PART_INFORMATION_DB_FIELDS.put(FIELD_M_VOLUME, "volume");
        ADDITIONAL_PART_INFORMATION_DB_FIELDS.put(FIELD_M_HAZARDOUS_GOODS_INDICATOR, "hazardousGoodsIndicator");
    }

    /**
     * Fügt der übergebenen <code>additionalPartInformationList</code> die PRIMUS Eigenschaften des <code>dataObjectWithPartFields</code>
     * als {@link iPartsCustomProperty.CustomProperty} hinzu
     *
     * @param project
     * @param dataObjectWithPartFields
     * @param additionalPartInformationList
     */
    public static void addAdditionalPartInformationAsCustomProperties(EtkProject project, EtkDataObject dataObjectWithPartFields,
                                                                      List<iPartsCustomProperty.CustomProperty> additionalPartInformationList) {
        String language = project.getDBLanguage();
        Map<String, EtkMultiSprache> additionalDatabaseInformationDescriptionCache = new HashMap<>();
        // Höhe, Breite, Länge, Gewicht und Volumen aus der Material-Tabelle als Custom Property sammeln und hinzufügen
        List<iPartsCustomProperty.CustomProperty> additionalDatabaseInformationList = ADDITIONAL_PART_INFORMATION_DB_FIELDS.entrySet().stream()
                .map(dbField -> {
                    String fieldName = dbField.getKey();
                    String databaseInformation = dataObjectWithPartFields.getFieldValue(fieldName);
                    iPartsCustomProperty.CustomProperty additionalPartInformation = null;
                    if (!databaseInformation.isEmpty()) {
                        additionalPartInformation = new iPartsCustomProperty.CustomProperty();
                        additionalPartInformation.setType(dbField.getValue());
                        EtkMultiSprache description = additionalDatabaseInformationDescriptionCache.computeIfAbsent(fieldName, field
                                -> project.getFieldDescription(TABLE_MAT, field).getUserDescription());
                        additionalPartInformation.setDescription(description);
                        additionalPartInformation.setValue(language, project.getVisObject().asString(TABLE_MAT, fieldName, databaseInformation, language));
                    }
                    return additionalPartInformation;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!additionalDatabaseInformationList.isEmpty()) {
            additionalPartInformationList.addAll(additionalDatabaseInformationList);
        }
    }

}
