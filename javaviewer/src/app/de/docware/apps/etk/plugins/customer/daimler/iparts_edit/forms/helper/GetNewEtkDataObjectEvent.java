package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;

/**
 * Interface für das Erzeugen einer neuen Instanz eines {@link EtkDataObject}s für den Import solcher Objekte.
 */
public interface GetNewEtkDataObjectEvent {

    EtkDataObject createNewDataObject(EtkProject project);
}
