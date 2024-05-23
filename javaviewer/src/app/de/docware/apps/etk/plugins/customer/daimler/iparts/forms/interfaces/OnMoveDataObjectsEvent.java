package de.docware.apps.etk.plugins.customer.daimler.iparts.forms.interfaces;

import de.docware.apps.etk.base.project.base.EtkDataObject;

import java.util.List;

/**
 * Interface für EditSelectDataObjectsForm
 */
public interface OnMoveDataObjectsEvent {

    /**
     * Wird aufgerufen, bevor ein oder mehrere {@link EtkDataObject}s von links in das rechte Grid übernommen wird
     *
     * @param selectedList
     * @return false: die gesamte Aktion wird abgebrochen
     */
    public boolean doBeforeAddEntries(List<EtkDataObject> selectedList);

    /**
     * Wird aufgerufen, bevor ein oder mehrere {@link EtkDataObject}s von rechts entfernt wird
     *
     * @param selectedList
     * @return false: die gesamte Aktion wird abgebrochen
     */
    public boolean doBeforeRemoveEntries(List<EtkDataObject> selectedList);

}
