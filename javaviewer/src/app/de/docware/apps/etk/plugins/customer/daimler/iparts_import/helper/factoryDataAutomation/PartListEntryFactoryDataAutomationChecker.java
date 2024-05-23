/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.factoryDataAutomation;

import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataFactoryData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.ImportFactoryDataAutomationHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Klasse für die Prüfungen der automatischen Werksfreigabe bei Stücklistenpositionen
 */
public class PartListEntryFactoryDataAutomationChecker extends AbstractAutomationChecker {

    private int modelFactoryTimeSliceCheckCount = 0;
    private int modelFilterCheckCount = 0;
    private int check3Count = 0;

    public PartListEntryFactoryDataAutomationChecker(ImportFactoryDataAutomationHelper importFactoryDataAutomationHelper) {
        super(importFactoryDataAutomationHelper);
    }

    @Override
    protected String makeKey(EtkDataObject newFactoryData) {
        if (newFactoryData instanceof iPartsDataFactoryData) {
            String bcteKey = ((iPartsDataFactoryData)newFactoryData).getGUID();
            String factory = ((iPartsDataFactoryData)newFactoryData).getAsId().getFactory();
            return bcteKey + "|-|" + factory;
        }
        return "";
    }

    /**
     * Führt die Prüfungen aus, die bestimmen, on ein Datensatz freigegeben werden kann
     *
     * @param retailPartlistEntries
     * @param newFactoryData
     * @param releaseList
     */
    public void doPartListEntryFactoryDataChecks(List<EtkDataPartListEntry> retailPartlistEntries,
                                                 iPartsDataFactoryData newFactoryData,
                                                 List<EtkDataObject> releaseList) {
        // Prüfung 1: Maximale Werkszeitscheibe je Baumuster
        Map<PartListEntryId, ImportFactoryDataAutomationHelper.ModelsForPartlistEntry> modelsAndPartlistEntriesWithChanges = getHelper().checkMaxFactoryTimeSlice(newFactoryData,
                                                                                                                                                                  retailPartlistEntries);
        if (doModelFactoryTimeSliceCheck(modelsAndPartlistEntriesWithChanges, newFactoryData, releaseList)) {
            return;
        }
        getHelper().logMessageToLogChannel(newFactoryData, "Check 1: Timeslices changed -> executing check 2");
        Collection<iPartsDataPartListEntry> pleWithDifferentModelFilterResult =
                getHelper().checkModelFilterResult(modelsAndPartlistEntriesWithChanges, newFactoryData);
        if (doModelFilterCheck(pleWithDifferentModelFilterResult, newFactoryData, releaseList)) {
            return;
        }
        if (doThirdCheck(pleWithDifferentModelFilterResult, newFactoryData, releaseList)) {
            return;
        }
        // Kann der Datensatz nicht freigegeben werden und ist der Terminverchiebungscheck aktiv, dann füge den Datensatz
        // zu den potentiellen Kandidaten hinzu
        if (getHelper().isThresholdCheckActive()) {
            addToDateThresholdCheckWithLog(newFactoryData);
        }
    }

    private boolean doThirdCheck(Collection<iPartsDataPartListEntry> pleWithDifferentModelFilterResult, iPartsDataFactoryData newFactoryData, List<EtkDataObject> releaseList) {
        // DAIMLER-7921 TODO: Prüfung 3
        check3Count++;
        if (Logger.getLogger().isChannelActive(iPartsImportPlugin.LOG_CHANNEL_FACTORYDATA_AUTO_RELEASE)) {
            DwList<String> entriesForLog = new DwList<>();
            for (iPartsDataPartListEntry entry : pleWithDifferentModelFilterResult) {
                entriesForLog.add("(" + entry.getAsId().toStringForLogMessages() + ")");
            }
            getHelper().logMessageToLogChannel(newFactoryData, "Check 2: Model filter results changed -> executing check 3 for the following partListEntries: ["
                                                               + StrUtils.stringListToString(entriesForLog, ", ") + "]");
        }
        return false;
    }

    private boolean doModelFilterCheck(Collection<iPartsDataPartListEntry> pleWithDifferentModelFilterResult, iPartsDataFactoryData newFactoryData, List<EtkDataObject> releaseList) {
        modelFilterCheckCount++;
        boolean result = pleWithDifferentModelFilterResult.isEmpty();
        if (result) {
            // Keine Abweichung beim Baumusterfilter => freigeben
            releaseList.add(newFactoryData);
            addAlreadyReleasedDataKey(newFactoryData);
            getHelper().logMessageToLogChannel(newFactoryData, "Check 2: No change in model filter results -> auto release factory data");
        }
        return result;
    }

    private boolean doModelFactoryTimeSliceCheck(Map<PartListEntryId, ImportFactoryDataAutomationHelper.ModelsForPartlistEntry> modelsAndPartlistEntriesWithChanges,
                                                 iPartsDataFactoryData newFactoryData, List<EtkDataObject> releaseList) {
        modelFactoryTimeSliceCheckCount++;
        boolean result = modelsAndPartlistEntriesWithChanges.isEmpty();
        if (result) {
            // Keine Änderungen der Zeitscheiben: Daten können freigegeben werden
            releaseList.add(newFactoryData);
            addAlreadyReleasedDataKey(newFactoryData);
            getHelper().logMessageToLogChannel(newFactoryData, "Check 1: No change in timeslices -> auto release factory data");
        }
        return result;
    }

    public int getModelFactoryTimeSliceCheckCount() {
        return modelFactoryTimeSliceCheckCount;
    }

    public int getModelFilterCheckCount() {
        return modelFilterCheckCount;
    }

    public int getCheck3Count() {
        return check3Count;
    }

    @Override
    public void logMessagesToGUI() {
        getHelper().logMessageToGui("!!Anzahl Datensätze für Prüfung 1: %1", String.valueOf(modelFactoryTimeSliceCheckCount));
        getHelper().logMessageToGui("!!Anzahl Datensätze für Prüfung 2: %1", String.valueOf(modelFilterCheckCount));
        getHelper().logMessageToGui("!!Anzahl Datensätze für Prüfung 3: %1", String.valueOf(check3Count));
        super.logMessagesToGUI();
    }

    @Override
    public void logMessagesToLogChannel() {
        getHelper().logMessageToLogChannel("Datasets for check 1: " + modelFactoryTimeSliceCheckCount);
        getHelper().logMessageToLogChannel("Datasets for check 2: " + modelFilterCheckCount);
        getHelper().logMessageToLogChannel("Datasets for check 3: " + check3Count);
        super.logMessagesToLogChannel();
    }
}
