/*
 * Copyright (c) 2020 Docware GmbH
 */

/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.factoryDataAutomation;

import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataFactoryData;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.ImportFactoryDataAutomationHelper;

import java.util.*;

/**
 * Abstrakte Klasse für die gemeinsamen Prüfungen der automatischen Werksfreigabe, wie z.B. die Prüfung auf Basis
 * einer Terminverschiebung
 */
public abstract class AbstractAutomationChecker {

    private int dataForThresholdCheck = 0;
    private Set<String> releasedDataFactories;
    private Map<String, List<EtkDataObject>> dateThresholdDataMap;
    private ImportFactoryDataAutomationHelper helper;

    public AbstractAutomationChecker(ImportFactoryDataAutomationHelper importFactoryDataAutomationHelper) {
        this.helper = importFactoryDataAutomationHelper;
        this.releasedDataFactories = new HashSet<>();
        this.dateThresholdDataMap = new HashMap<>();
    }


    /**
     * Fügt den übergebenen Datensatz zu den potentiellen Kandidaten für eine Freigabe durch die Terminverschiebung
     * hinzu und logt den Vorgang.
     *
     * @param newFactoryData
     */
    protected void addToDateThresholdCheckWithLog(iPartsDataFactoryData newFactoryData) {
        if (!helper.isThresholdCheckActive()) {
            return;
        }
        helper.logMessageToLogChannel(newFactoryData, "Did not pass main checks -> do date threshold check");
        addToDateThresholdCheck(newFactoryData);
    }

    /**
     * Fügt den übergebenen Datensatz zu den potentiellen Kandidaten für eine Freigabe durch die Terminverschiebung
     * hinzu.
     *
     * @param newFactoryData
     */
    public void addToDateThresholdCheck(EtkDataObject newFactoryData) {
        if (!helper.isThresholdCheckActive()) {
            return;
        }
        String dataKey = makeKey(newFactoryData);
        if (!getReleasedDataFactories().contains(dataKey)) {
            List<EtkDataObject> dataForBCTEAndFactory = dateThresholdDataMap.computeIfAbsent(dataKey, k -> new ArrayList<>());
            dataForBCTEAndFactory.add(newFactoryData);
            dataForThresholdCheck++;
        }
    }

    /**
     * Fügt den Schlüssel eines Datensatzes hinzu, der durch andere Prüfungen auf "freigegeben" gesetzt wird
     *
     * @param newFactoryData
     */
    public void addAlreadyReleasedDataKey(EtkDataObject newFactoryData) {
        releasedDataFactories.add(makeKey(newFactoryData));
    }

    protected ImportFactoryDataAutomationHelper getHelper() {
        return helper;
    }

    public Set<String> getReleasedDataFactories() {
        return releasedDataFactories;
    }

    public void logMessagesToGUI() {
        if (getHelper().isThresholdCheckActive()) {
            getHelper().logMessageToGui("!!Anzahl Datensätze für Terminverschiebung: %1", String.valueOf(dataForThresholdCheck));
        }
    }

    public void logMessagesToLogChannel() {
        if (getHelper().isThresholdCheckActive()) {
            getHelper().logMessageToLogChannel("Datasets for DateThreshold check: " + dataForThresholdCheck);
        }
    }

    public void clearData() {
        releasedDataFactories.clear();
        dateThresholdDataMap.clear();
    }

    /**
     * Fügt der übergebenen Liste alle Datensätze hinzu, die durch die Terminverschiebungsprüfung freigegeben
     * werden könnten.
     *
     * @param dateThresholdData
     */
    public void addDataForDateThresholdCheck(List<List<EtkDataObject>> dateThresholdData) {
        if (!helper.isThresholdCheckActive()) {
            return;
        }
        // Werksdaten, die für einen BCTE und Werks Schlüssel durch die bisherigen Prüfungen freigegeben wurden,
        // sollen bei diesem Durchlauf nicht berücksichtigt werden
        dateThresholdDataMap.keySet().removeAll(releasedDataFactories);
        dateThresholdData.addAll(dateThresholdDataMap.values());
    }


    protected abstract String makeKey(EtkDataObject newFactoryData);
}
