/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsColorTableFactoryId;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sort.BetterSortMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Datenklasse für die Werkseinsatzdaten von einer Farbvariantentabelle bzw. einem Farbvarianteninhalt aufbereitet für den
 * Retail (Filterung und Webservices).
 */
public class iPartsColorFactoryDataForRetail {

    private boolean hasFactoryDataWithInfiniteDates;

    // Werkseinsatzdaten pro Werksnummer (BetterSortMap für Sortierung mit "Andisort")
    // Bei Farbvarianten gibt es im Gegensatz zu Stücklisteneinträgen IMMER Werkseinsatzdaten, da die Farbvarianten im
    // Retail ansonsten komplett ungültig wären
    private Map<String, List<DataForFactory>> factoryDataMap = new BetterSortMap<String, List<DataForFactory>>();

    public iPartsColorFactoryDataForRetail cloneMe() {
        iPartsColorFactoryDataForRetail clone = new iPartsColorFactoryDataForRetail();
        for (Map.Entry<String, List<DataForFactory>> dataForFactoryEntry : factoryDataMap.entrySet()) {
            List<DataForFactory> dataForFactoryCloneList = new DwList<>(dataForFactoryEntry.getValue().size());
            for (DataForFactory dataForFactory : dataForFactoryEntry.getValue()) {
                DataForFactory dataForFactoryClone = new DataForFactory();
                dataForFactoryClone.assign(dataForFactory);
                dataForFactoryCloneList.add(dataForFactoryClone);
            }
            clone.factoryDataMap.put(dataForFactoryEntry.getKey(), dataForFactoryCloneList);
        }
        clone.hasFactoryDataWithInfiniteDates = hasFactoryDataWithInfiniteDates;
        return clone;
    }

    /**
     * Liefert die unveränderliche Map von Werksnummer auf Werkseinsatzdaten für das entsprechende Werk zurück.
     *
     * @return
     */
    public Map<String, List<DataForFactory>> getFactoryDataMap() {
        return Collections.unmodifiableMap(factoryDataMap);
    }

    /**
     * Liefert alle Werkseinsatzdaten für das übergebene Werk zurück.
     *
     * @param factory
     * @return
     */
    public List<DataForFactory> getDataForFactory(String factory) {
        return factoryDataMap.get(factory);
    }

    /**
     * Setzt alle Werkseinsatzdaten für das übergebene Werk.
     *
     * @param factory
     * @param dataForFactory
     */
    public void setDataForFactory(String factory, List<DataForFactory> dataForFactory) {
        factoryDataMap.put(factory, dataForFactory);
    }

    /**
     * Flag, ob es Werkseinsatzdaten mit -/+ unendlich gibt (die ansonsten ignoriert werden und nur im Baumuster-Filter
     * eine Rolle spielen).
     *
     * @return
     */
    public boolean hasFactoryDataWithInfiniteDates() {
        return hasFactoryDataWithInfiniteDates;
    }

    /**
     * Flag, ob es Werkseinsatzdaten mit -/+ unendlich gibt (die ansonsten ignoriert werden und nur im Baumuster-Filter
     * eine Rolle spielen).
     *
     * @param hasFactoryDataWithInfiniteDates
     */
    public void setHasFactoryDataWithInfiniteDates(boolean hasFactoryDataWithInfiniteDates) {
        this.hasFactoryDataWithInfiniteDates = hasFactoryDataWithInfiniteDates;
    }


    /**
     * Werkseinsatzdaten inkl. Rückmeldedaten und Ausreißern für ein konkretes Werk.
     */
    public static class DataForFactory extends iPartsFactoryData.AbstractDataForFactory {

        public iPartsColorTableFactoryId factoryDataId;

        public void assign(DataForFactory source) {
            super.assign(source);
            factoryDataId = source.factoryDataId;
        }
    }
}