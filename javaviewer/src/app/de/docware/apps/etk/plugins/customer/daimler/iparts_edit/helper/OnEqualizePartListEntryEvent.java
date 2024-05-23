package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.framework.modules.db.DBDataObjectAttributes;

/**
 * Interface für die Vererbung von speziellen Feldern (z.B. kombinierter Text)
 */
public interface OnEqualizePartListEntryEvent {

    /**
     * Überprüft, ob das zum {@code fieldName} gehörende Attribut modifiziert ist.
     *
     * @param project
     * @param fieldName
     * @param partListEntry
     * @return
     */
    boolean isModifiedSpecialAttribute(EtkProject project, String fieldName, EtkDataPartListEntry partListEntry);

    /**
     * Vererbt die über {@code fieldName} und {@code sourcePartListEntry} angesprochenen Änderungen an den {@code destPartListEntry}
     * bzw. die {@code destAttributes}.
     * Sind andere Tabellen betroffen, so können die Änderungen hier durchgeführt werden.
     *
     * @param project
     * @param fieldName
     * @param sourcePartListEntry
     * @param destPartListEntry
     * @param destAttributes
     */
    void onEqualizePartListEntry(EtkProject project, String fieldName, EtkDataPartListEntry sourcePartListEntry,
                                 EtkDataPartListEntry destPartListEntry, DBDataObjectAttributes destAttributes);
}
