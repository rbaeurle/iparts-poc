/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsAdditionalPartInformationHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsCustomProperty;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSAdditionalPartInformation;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Hilfsklasse für zusätzliche Informationen aus Cache und DB für den Webservice GetParts
 */
public class iPartsWSAdditionalPartInformationHelper {

    private static final String ENUM_KEY_HAZARDOUS_GOODS_INDICATOR = "HazardousGoodsIndicator";

    /**
     * Methode zum Initialisieren und Befüllen einer {@link iPartsWSAdditionalPartInformation} Liste.
     * Diese enthält - sofern vorhanden:
     * - Custom Properties aus dem CustomPropertyCache und
     * - zusätzliche Werte wie z.B. Höhe, Gewicht oder Volumen aus der Materialtabelle.
     * Gibt eine leere, initialisierte Liste zurück, falls keine Daten vorhanden sind.
     *
     * @param project
     * @param part
     * @param matNr
     * @return
     */
    public static List<iPartsWSAdditionalPartInformation> fillAdditionalPartInformation(EtkProject project, EtkDataObject part, String matNr, String dbLanguage, List<String> dbFallbackLanguages, iPartsCustomProperty iPartsCustomPropertyCache) {
        List<iPartsWSAdditionalPartInformation> additionalPartInformationList = new DwList<>();


        // Custom Properties aus dem Cache bzw. der DB laden und als AdditionalPartInformation hinzufügen
        Collection<iPartsCustomProperty.CustomProperty> customProperties = iPartsCustomPropertyCache.getCustomProperties(matNr);
        if (customProperties != null) {
            List<iPartsWSAdditionalPartInformation> customPropertyList = customProperties.stream()
                    .map(customProperty -> {
                        iPartsWSAdditionalPartInformation additionalPartInformation = new iPartsWSAdditionalPartInformation();
                        additionalPartInformation.setType(customProperty.getType());
                        additionalPartInformation.setDescription(customProperty.getDescription().getTextByNearestLanguage(dbLanguage, dbFallbackLanguages));
                        additionalPartInformation.setValue(customProperty.getValue(dbLanguage, dbFallbackLanguages));
                        return additionalPartInformation;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (!customPropertyList.isEmpty()) {
                additionalPartInformationList.addAll(customPropertyList);
            }
        }

        Map<String, String> additionalDatabaseInfoDescriptionCache = new HashMap<>();
        // Höhe, Breite, Länge, Gewicht und Volumen aus der Material-Tabelle als AdditionalPartInformation hinzufügen.
        // Die Inhalte dieser Felder kommen immer aus Primus, daher wird die Quelle statisch gesetzt
        List<iPartsWSAdditionalPartInformation> additionalDatabaseInformationList
                = iPartsAdditionalPartInformationHelper.ADDITIONAL_PART_INFORMATION_DB_FIELDS.entrySet().stream()
                .map(dbField -> {
                    String fieldName = dbField.getKey();
                    String databaseInformation = part.getFieldValue(fieldName);
                    iPartsWSAdditionalPartInformation additionalPartInformation = null;
                    if (!databaseInformation.isEmpty()) {
                        additionalPartInformation = new iPartsWSAdditionalPartInformation();
                        additionalPartInformation.setType(dbField.getValue());
                        String description = additionalDatabaseInfoDescriptionCache.computeIfAbsent(fieldName, field
                                -> project.getFieldDescription(iPartsConst.TABLE_MAT, field).getUserDescription().getTextByNearestLanguage(dbLanguage, dbFallbackLanguages));
                        additionalPartInformation.setDescription(description);
                        additionalPartInformation.setSourcePrimus();

                        // Das auszugebende "value" muss bei FIELD_M_HAZARDOUS_GOODS_INDICATOR über das Enum [HazardousGoodsIndicator] bestimmt werden.
                        String value = "";
                        if (fieldName.equals(iPartsConst.FIELD_M_HAZARDOUS_GOODS_INDICATOR)) {
                            value = getHazardousGoodsIndicatorText(project, dbLanguage, databaseInformation);
                        } else {
                            value = project.getVisObject().asString(iPartsConst.TABLE_MAT, fieldName, databaseInformation, dbLanguage);
                        }
                        additionalPartInformation.setValue(value);
                    }
                    return additionalPartInformation;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!additionalDatabaseInformationList.isEmpty()) {
            additionalPartInformationList.addAll(additionalDatabaseInformationList);
        }

        return additionalPartInformationList;
    }

    /**
     * Gibt zum übergebenen Datenbankwert für den Gefahrgutkenner "M_HAZARDOUS_GOODS_INDICATOR" den zur Sprache
     * passenden Text aus den Enums zurück. Ist für den Wert kein Enum definiert, wird der Wert alleine wieder
     * zurückgegeben. Dem sprachabhängigen String wird immer der Enum-Wert vorangestellt.
     *
     * Beispiel mit existierendem Enum-Wert:
     * "0.0" ==> "0.0 Gefahrgut-Identifikation noch nicht abgeschlossen. Wird als potentielles Gefahrgut-Teil behandelt"
     * Beispiel ohne existierenden Enum-Wert:
     * "1.5" ==> "1.5"
     *
     * @param project
     * @param dbLanguage
     * @param dbValue
     * @return
     */
    private static String getHazardousGoodsIndicatorText(EtkProject project, String dbLanguage, String dbValue) {
        String result = "";
        if (StrUtils.isValid(dbValue)) {
            String enumText = project.getEnumText(ENUM_KEY_HAZARDOUS_GOODS_INDICATOR, dbValue, dbLanguage, true);
            result = dbValue;
            if (StrUtils.isValid(enumText) && !enumText.equals(dbValue)) {
                result = result + " " + enumText;
            }
        }
        return result;
    }
}
