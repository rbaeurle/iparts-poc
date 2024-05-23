/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsFactoriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFactories;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper für das verarbeiten von BOM-DB spezifischen Werkskennungen
 */
public class iPartsBomDBFactoriesHelper {

    private static final String PLANT_SUPPLIES_DB_DELIMITER = ",";

    /**
     * Liefert den Datenbank-String für die BOM-DB spezifischen Werkskennungen aus den BOM-DB XML Elementen
     *
     * @param originalValueFromMainImporter
     * @return
     */
    public static String getDBValueForPlantSupplies(String originalValueFromMainImporter) {
        List<String> plantSupplies = StrUtils.splitStringIntoSubstrings(originalValueFromMainImporter, 2, true, false);
        return getDBValueForPlantSupplies(plantSupplies);
    }

    /**
     * Liefert den Datenbank-String für die BOM-DB spezifischen Werkskennungen aus den BOM-DB Urladungsdateien
     *
     * @param originalValuesFromUpdateImporter
     * @return
     */
    public static String getDBValueForPlantSupplies(Collection<String> originalValuesFromUpdateImporter) {
        return StrUtils.makeDelimitedString(PLANT_SUPPLIES_DB_DELIMITER, ArrayUtil.toStringArray(originalValuesFromUpdateImporter));
    }

    /**
     * Extrahiert aus dem übergebenen Datenbank-String die BOM-DB Werkskennungen und liefert eine Map zurück, die die
     * Werkskennbuchstaben und die dazugehörige Bezeichnung enthält
     *
     * @param project
     * @param plantSuppliesDBValue
     * @return
     */
    public static Map<String, String> getFactoryLettersWithDescForBOMDBValue(EtkProject project, String plantSuppliesDBValue) {
        Map<String, String> result = new HashMap<String, String>();
        String[] plantSuppliesValues = StrUtils.toStringArray(plantSuppliesDBValue, PLANT_SUPPLIES_DB_DELIMITER, false);
        for (String platSupplyValue : plantSuppliesValues) {
            String desc = iPartsFactories.getInstance(project).getFactoryDescription(new iPartsFactoriesId(platSupplyValue), project.getDBLanguage());
            if (desc == null) {
                desc = "";
            }
            result.put(platSupplyValue, desc);
        }
        return result;
    }

}
