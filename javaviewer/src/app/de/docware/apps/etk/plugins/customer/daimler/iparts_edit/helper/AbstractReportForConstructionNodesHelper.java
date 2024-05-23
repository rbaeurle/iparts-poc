package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsReportConstNodeId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.framework.utils.VarParam;

/**
 * Abstracte Klasse für Reporting/Export von Konstruktiven Stücklisten
 */
public abstract class AbstractReportForConstructionNodesHelper {

    protected EtkProject projectForCalculation;
    protected EtkProject projectForGUI;
    protected iPartsReportConstNodeId lockReportConstNodeId;
    protected String lockReportConstNodeCalculationDate;
    protected HmMSm hmMSm;

    public AbstractReportForConstructionNodesHelper(EtkProject projectForCalculation, EtkProject projectForGUI) {
        this.projectForCalculation = projectForCalculation;
        this.projectForGUI = projectForGUI;
    }

    /**
     * Report Eintrag (zum Sperren) erzeugen
     * hier können weitere Initialisierungen vorgenommen werden
     *
     * @param seriesNumber
     * @param errMsg
     * @return {@code false} bei bereits laufendem Reporting für diese Baureihe
     */
    protected boolean makeAndCheckStartNode(String seriesNumber, String errMsg) {
        hmMSm = HmMSm.getInstance(projectForCalculation, new iPartsSeriesId(seriesNumber));
        return true;
    }

    /**
     * Reporting rekursiv für alle HM/M-Knoten
     *
     * @param assembly
     * @param parentAssemblyId
     * @param hmMSmId
     * @param openEntries
     * @param changedEntries
     * @return {@code false} bei Abbruch
     */
    protected abstract boolean calculateSeriesHmMNode(EtkDataAssembly assembly, AssemblyId parentAssemblyId, HmMSmId hmMSmId,
                                                      VarParam<Integer> openEntries, VarParam<Integer> changedEntries);

    /**
     * Reporting für einen SM-Knoten und das darin enthaltene Modul
     *
     * @param assembly
     * @param parentAssemblyId
     * @param hmMSmId
     * @param openEntries
     * @param changedEntries
     * @return {@code false} bei Abbruch
     */
    protected abstract boolean calculateSmNode(EtkDataAssembly assembly, AssemblyId parentAssemblyId, HmMSmId hmMSmId,
                                               VarParam<Integer> openEntries, VarParam<Integer> changedEntries);

    /**
     * weitere Aktionen mit einem Stücklisteneintrag durchführen
     *
     * @param partListEntry
     * @return {@code false} bei Abbruch
     */
    protected abstract boolean calculatePartListEntry(EtkDataPartListEntry partListEntry);

    /**
     * einen Report-Knoten mit Ergebnissen speichern
     *
     * @param parentAssemblyId
     * @param hmMSmId
     * @param openEntries
     * @param changedEntries
     */
    protected abstract void saveHmMSmNodeCalculation(AssemblyId parentAssemblyId, HmMSmId hmMSmId, int openEntries, int changedEntries);

    /**
     * wird immer im finally durchlaufen => hier kann aufgräumt werden
     *
     * @param wasCancelled
     * @param hasException
     * @return
     */
    protected abstract boolean removeLockForReport(boolean wasCancelled, boolean hasException);

    protected abstract void doPostCalcOperations(boolean withGuiActions);

    protected abstract void clearAfterCalcFinished();
}
