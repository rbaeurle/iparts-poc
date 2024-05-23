/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.transit;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMeta;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMetaList;
import de.docware.framework.modules.db.DBActionOrigin;

import java.util.EnumSet;

/**
 * Helfer zum Trennen von Übersetzungstexten nach ihren Gültigkeiten
 */
public class DictMetaListContainer {

    private static final EnumSet<iPartsImportDataOrigin> VALID_CAR_AND_VAN_SOURCES_FOR_TRANSLATION = EnumSet.of(iPartsImportDataOrigin.IPARTS, iPartsImportDataOrigin.IPARTS_MB,
                                                                                                                iPartsImportDataOrigin.IPARTS_GENVO, iPartsImportDataOrigin.PSK);
    private static final EnumSet<iPartsImportDataOrigin> VALID_TRUCK_AND_BUS_SOURCES_FOR_TRANSLATION = EnumSet.of(iPartsImportDataOrigin.IPARTS, iPartsImportDataOrigin.IPARTS_TRUCK);

    public static boolean isCarVanSourceValidForTranslation(String dbValue) {
        return isCarVanSourceValidForTranslation(iPartsImportDataOrigin.getTypeFromCode(dbValue));
    }

    public static boolean isCarVanSourceValidForTranslation(iPartsImportDataOrigin importDataOrigin) {
        return VALID_CAR_AND_VAN_SOURCES_FOR_TRANSLATION.contains(importDataOrigin);
    }

    public static boolean isTruckBusSourceValidForTranslation(String dbValue) {
        return isTruckBusSourceValidForTranslation(iPartsImportDataOrigin.getTypeFromCode(dbValue));
    }

    public static boolean isTruckBusSourceValidForTranslation(iPartsImportDataOrigin importDataOrigin) {
        return VALID_TRUCK_AND_BUS_SOURCES_FOR_TRANSLATION.contains(importDataOrigin);
    }

    private final iPartsDataDictMetaList truckAndBusTexts;
    private final iPartsDataDictMetaList carAndVanTexts;

    public DictMetaListContainer() {
        this.truckAndBusTexts = new iPartsDataDictMetaList();
        this.carAndVanTexts = new iPartsDataDictMetaList();
    }

    /**
     * Füge einen Text hinzu, der als "Truck" Text an den Übersetzer geht.
     *
     * @param dictMeta
     */
    private void addTruckText(iPartsDataDictMeta dictMeta) {
        if (dictMeta != null) {
            truckAndBusTexts.add(dictMeta, DBActionOrigin.FROM_DB);
        }
    }

    /**
     * Fügt den übergebenen Text hinzu und sortiert ihn nach seiner Gültigkeit in die dazugehörige Gruppe ein
     *
     * @param dictMeta
     * @param carAndVanInSession
     * @param truckAndBusInSession
     */
    public void addText(iPartsDataDictMeta dictMeta, boolean carAndVanInSession, boolean truckAndBusInSession) {
        if (dictMeta != null) {
            iPartsImportDataOrigin importDataOrigin = iPartsImportDataOrigin.getTypeFromCode(dictMeta.getSource());
            if (carAndVanInSession && VALID_CAR_AND_VAN_SOURCES_FOR_TRANSLATION.contains(importDataOrigin)) {
                addCarText(dictMeta);
            } else if (truckAndBusInSession && VALID_TRUCK_AND_BUS_SOURCES_FOR_TRANSLATION.contains(importDataOrigin)) {
                addTruckText(dictMeta);
            }
        }
    }

    /**
     * Füge einen Text hinzu, der als "PKW" Text an den Übersetzer geht.
     *
     * @param dictMeta
     */
    private void addCarText(iPartsDataDictMeta dictMeta) {
        if (dictMeta != null) {
            carAndVanTexts.add(dictMeta, DBActionOrigin.FROM_DB);
        }
    }

    public iPartsDataDictMetaList getTruckAndBusTexts() {
        return truckAndBusTexts;
    }

    public iPartsDataDictMetaList getCarAndVanTexts() {
        return carAndVanTexts;
    }

    public iPartsDataDictMetaList getAllTexts() {
        iPartsDataDictMetaList allTexts = new iPartsDataDictMetaList();
        allTexts.addAll(carAndVanTexts, DBActionOrigin.FROM_DB);
        allTexts.addAll(truckAndBusTexts, DBActionOrigin.FROM_DB);
        return allTexts;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        return truckAndBusTexts.size() + carAndVanTexts.size();
    }
}
