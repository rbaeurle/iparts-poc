/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataReportConstNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsReportConstNodeId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualAssemblyDialogBase;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsBusinessCase;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmNode;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.VarParam;

/**
 * Klasse für das Reporting von konstruktiven Stücklisten
 */
public class ReportForConstructionNodes extends AbstractReportForConstructionNodesHelper {


    public ReportForConstructionNodes(EtkProject projectForCalculation, EtkProject projectForGUI) {
        super(projectForCalculation, projectForGUI);
    }

    @Override
    protected boolean makeAndCheckStartNode(String seriesNumber, String errMsg) {
        super.makeAndCheckStartNode(seriesNumber, errMsg);
        lockReportConstNodeId = new iPartsReportConstNodeId(seriesNumber, "", "");
        iPartsDataReportConstNode lockDataReportConstNode = new iPartsDataReportConstNode(projectForCalculation, lockReportConstNodeId);
        if (lockDataReportConstNode.existsInDB()) {
            String calculationDate = lockDataReportConstNode.getCalculationDate();
            if (iPartsVirtualAssemblyDialogBase.isHmMSmNodeCalculationValid(calculationDate)) {
                String calulationDateString = projectForCalculation.getVisObject().asString(iPartsConst.TABLE_DA_REPORT_CONST_NODES,
                                                                                            iPartsConst.FIELD_DRCN_CALCULATION_DATE,
                                                                                            calculationDate,
                                                                                            projectForCalculation.getViewerLanguage());
                MessageDialog.showWarning(TranslationHandler.translate(errMsg, seriesNumber, calulationDateString));
                return false;
            }
            // Falls die Berechnung vor der maximalen Gültigkeitsdauer gestartet wurde, dann wird diese als ungültig
            // angesehen und weiter unten direkt mit dem aktuellen Zeitpunkt überschrieben
        } else {
            lockDataReportConstNode.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }

        // Sperr-Datensatz mit aktuellem Datum schreiben
        lockDataReportConstNode.setCalculationDate(DBActionOrigin.FROM_EDIT);
        lockReportConstNodeCalculationDate = lockDataReportConstNode.getCalculationDate();
        lockDataReportConstNode.saveToDB();
        return true;
    }

    /**
     * Überprüft, ob der übergebene HM/M/SM-Knoten gültig ist für das Reporting dieses Teilbaums.
     *
     * @param hmMSmNode
     * @return
     */
    protected boolean checkIfHmMSmNodeValidForSubTree(HmMSmNode hmMSmNode) {
        return checkIfHmMSmNodeValidForCalculation(hmMSmNode);
    }

    /**
     * Überprüft, ob der übergebene HM/M/SM-Knoten gültig ist für die Berechnung.
     *
     * @param hmMSmNode
     * @return
     */
    protected boolean checkIfHmMSmNodeValidForCalculation(HmMSmNode hmMSmNode) {
        return (hmMSmNode != null) && !hmMSmNode.isHiddenRecursively() && !hmMSmNode.isNoCalcRecursively();
    }

    @Override
    protected boolean calculateSeriesHmMNode(EtkDataAssembly assembly, AssemblyId parentAssemblyId, HmMSmId hmMSmId,
                                             VarParam<Integer> openEntries, VarParam<Integer> changedEntries) {
        int openEntriesCount = 0;
        int changedEntriesCount = 0;

        // Rekursive Berechnung durchführen mit Filterung für ausgeblendete HM/M/SM-Knoten
        for (EtkDataPartListEntry subNodePLE : assembly.getSubAssemblyEntries(false)) {
            if (Thread.currentThread().isInterrupted()) {
                return false;
            }

            EtkDataAssembly subNodeAssembly = EtkDataObjectFactory.createDataAssembly(projectForCalculation, subNodePLE.getDestinationAssemblyId());
            HmMSmNode subHmMSmNode = iPartsVirtualNode.getHmMSmNodeForAssemblyId(subNodeAssembly.getAsId(), projectForCalculation);
            if (checkIfHmMSmNodeValidForSubTree(subHmMSmNode)) { // Kind-HM/M/SM-Knoten gefunden und nicht ausgeblendet?
                if ((hmMSmId == null) || hmMSmId.isHmNode()) { // Bei Baureihe oder HM-Knoten die Kind-HM/M-Knoten berechnen
                    if (!calculateSeriesHmMNode(subNodeAssembly, assembly.getAsId(), subHmMSmNode.getId(), openEntries,
                                                changedEntries)) {
                        return false;
                    }
                } else { // Bei M-Knoten die Kind-SM-Knoten (DIALOG-Konstruktionsstücklisten) berechnen
                    if (!calculateSmNode(subNodeAssembly, assembly.getAsId(), subHmMSmNode.getId(), openEntries,
                                         changedEntries)) {
                        return false;
                    }
                }
                if (checkIfHmMSmNodeValidForCalculation(subHmMSmNode)) { // Berechnung zulässig für den Kind-HM/M/SM-Knoten?
                    openEntriesCount += openEntries.getValue();
                    changedEntriesCount += changedEntries.getValue();
                }
            }
        }

        // Berechnung zulässig für diese Baureihe bzw. HM/M/SM-Knoten?
        if ((hmMSmId == null) || checkIfHmMSmNodeValidForCalculation(hmMSm.getNode(hmMSmId))) {
            saveHmMSmNodeCalculation(parentAssemblyId, hmMSmId, openEntriesCount, changedEntriesCount);
            openEntries.setValue(openEntriesCount);
            changedEntries.setValue(changedEntriesCount);
        }
        return true;
    }

    @Override
    protected boolean calculateSmNode(EtkDataAssembly assembly, AssemblyId parentAssemblyId, HmMSmId hmMSmId,
                                      VarParam<Integer> openEntries, VarParam<Integer> changedEntries) {
        int openEntriesCount = 0;
        int changedEntriesCount = 0;

        // Zunächst die ungefilterte DIALOG-Konstruktions-Stückliste betrachten und dann explizit nach AS-Relevanz filtern
        DBDataObjectList<EtkDataPartListEntry> partListEntries = assembly.getPartListUnfiltered(assembly.getEbene());

        // DIALOG-Konstruktions-Stückliste gleich wieder aus dem Cache entfernen, damit der Heap nicht so sehr belastet wird
        EtkDataAssembly.removeDataAssemblyFromCache(projectForCalculation, assembly.getAsId());

        boolean isValidForCalculation = checkIfHmMSmNodeValidForCalculation(hmMSm.getNode(hmMSmId));
        for (EtkDataPartListEntry partListEntry : partListEntries) {
            if (Thread.currentThread().isInterrupted()) {
                return false;
            }

            // Stücklisteneintrag muss AS-relevant sein
            // "offene Teilepostionen = Geschäftsfall neu" und "geänderte Teilepositionen = Geschäftsfall geändert"
            if (isValidForCalculation && partListEntry.getFieldValueAsBoolean(iPartsDataVirtualFieldsDefinition.DD_CALCULATED_AS_RELEVANT)) {
                String businessCaseDbValue = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DD_CALCULATED_BUSINESS_CASE);
                switch (iPartsBusinessCase.getFromDBValue(businessCaseDbValue)) {
                    case BUSINESS_CASE_NEW:
                        openEntriesCount++;
                        break;
                    case BUSINESS_CASE_CHANGED:
                        changedEntriesCount++;
                        break;
                }
            }
            if (!calculatePartListEntry(partListEntry)) {
                return false;
            }
        }

        // Berechnung zulässig für diesen SM-Knoten?
        if (isValidForCalculation) {
            saveHmMSmNodeCalculation(parentAssemblyId, hmMSmId, openEntriesCount, changedEntriesCount);
            openEntries.setValue(openEntriesCount);
            changedEntries.setValue(changedEntriesCount);
        }
        return true;
    }

    @Override
    protected boolean calculatePartListEntry(EtkDataPartListEntry partListEntry) {
        return true;
    }

    @Override
    protected void saveHmMSmNodeCalculation(AssemblyId parentAssemblyId, HmMSmId hmMSmId, int openEntries, int changedEntries) {
        if (hmMSmId != null) { // Für Baureihe mit hmMSmId = null kann die Berechnung nicht in der DB gespeichert werden
            iPartsDataReportConstNode dataReportConstNode = new iPartsDataReportConstNode(projectForCalculation,
                                                                                          new iPartsReportConstNodeId(hmMSmId,
                                                                                                                      projectForCalculation));
            if (!dataReportConstNode.existsInDB()) {
                dataReportConstNode.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }
            dataReportConstNode.setOpenEntries(openEntries, DBActionOrigin.FROM_EDIT);
            dataReportConstNode.setChangedEntries(changedEntries, DBActionOrigin.FROM_EDIT);
            dataReportConstNode.setCalculationDate(DBActionOrigin.FROM_EDIT);
            dataReportConstNode.saveToDB();
        }

        EtkDataAssembly.removeDataAssemblyFromCache(projectForCalculation, parentAssemblyId);

        // Den Cache-Eintrag auch für das EtkProject der GUI entfernen
        if (projectForCalculation != projectForGUI) {
            EtkDataAssembly.removeDataAssemblyFromCache(projectForGUI, parentAssemblyId);
        }
    }

    @Override
    protected boolean removeLockForReport(boolean wasCancelled, boolean hasException) {
        if ((lockReportConstNodeId != null) && (lockReportConstNodeCalculationDate != null)) {
            // Sperre für die Baureihe wieder freigeben falls der Berechnungszeitpunkt noch übereinstimmst
            // (falls nicht, dann hat die Berechnung zu lange gedauert und wurde zwischenzeitlich als
            // ungültig angesehen)
            iPartsDataReportConstNode lockDataReportConstNode = new iPartsDataReportConstNode(projectForCalculation,
                                                                                              lockReportConstNodeId);
            if (lockDataReportConstNode.existsInDB()) {
                if (lockDataReportConstNode.getCalculationDate().equals(lockReportConstNodeCalculationDate)) {
                    lockDataReportConstNode.deleteFromDB();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void doPostCalcOperations(boolean withGuiActions) {

    }

    @Override
    protected void clearAfterCalcFinished() {

    }
}
