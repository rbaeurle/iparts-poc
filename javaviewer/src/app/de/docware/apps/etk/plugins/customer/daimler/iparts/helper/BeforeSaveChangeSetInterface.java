package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataChangeSet;

/**
 * Interface, das vor dem Speichern (commit mit Status COMMITTED) des {@link iPartsDataChangeSet}s aufgerufen wird.
 * Die Routine {@link #beforeSaveChangeSet(EtkProject, iPartsDataChangeSet)} wird innerhalb der Transaktion aufgerufen.
 */
public interface BeforeSaveChangeSetInterface {

    /**
     * Wird aufgerufen, bevor {@link iPartsDataChangeSet} gespeichert wird
     *
     * @param project
     * @param dataChangeSet
     */
    void beforeSaveChangeSet(EtkProject project, iPartsDataChangeSet dataChangeSet);
}
