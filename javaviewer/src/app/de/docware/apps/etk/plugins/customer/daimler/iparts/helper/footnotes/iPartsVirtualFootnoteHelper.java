/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper.footnotes;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsPRIMUSReplacementsLoader;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFootNote;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacement;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.util.collections.dwlist.DwList;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * Hilfsklasse zur Berechnung von virtuellen Fußnoten. Also Fußnoten, die NICHT über einen in der DB existierenden Eintrag
 * in {@link iPartsDataFootNoteCatalogueRef} am {@link iPartsDataPartListEntry} hängen, sondern durch Businesslogik
 * mit diesem verknüpft sind.
 */
public class iPartsVirtualFootnoteHelper {

    /**
     * Fügt der übergebenen Sammlung von echten Fußnoten virtuelle Fußnoten hinzu, sofern die Sammlung die virtuellen Fußnoten
     * nicht schon als echte Fußnoten enthält.
     *
     * @param partListEntry
     * @param realFootNotes
     * @param primusReplacementsLoader
     * @return
     */
    public static Collection<iPartsFootNote> addVirtualFootnotes(iPartsDataPartListEntry partListEntry, Collection<iPartsFootNote> realFootNotes,
                                                                 iPartsPRIMUSReplacementsLoader primusReplacementsLoader) {
        iPartsFootNote virtualFootNote = createVirtualFootNoteFromReplacements(partListEntry, primusReplacementsLoader);

        List<iPartsFootNote> allFootNotes = new DwList<>();
        if (realFootNotes != null) {
            allFootNotes.addAll(realFootNotes);
        }

        if ((virtualFootNote != null) && !iPartsFootNoteHelper.containsFootnote(allFootNotes, virtualFootNote.getFootNoteId(), null)) {
            int index = findInsertIndexForVirtualFootnote(allFootNotes);
            if (index != -1) {
                allFootNotes.add(index, virtualFootNote);
            }
        }

        if (!allFootNotes.isEmpty()) {
            return Collections.unmodifiableList(allFootNotes);
        } else {
            return null;
        }
    }

    /**
     * Erstellt für den übergebenen Stücklisteneintrag die virtuelle Fußnote, die sich aus seinen Ersetzungen ergibt.
     * Falls er keine echte After-Sales Ersetzung hat, wird in den PRIMUS-Ersetzungs-Hinweisen nach Ersetzungen
     * ohne Nachfolger gesucht.
     *
     * @param partListEntry
     * @param primusReplacementsLoader
     * @return {@code null}, falls keine der Ersetzungen zu einer virtuellen Fußnote führt, oder keine Ersetzungen
     * am Stücklisteneintrag vorhanden sind.
     */
    public static iPartsFootNote createVirtualFootNoteFromReplacements(iPartsDataPartListEntry partListEntry, iPartsPRIMUSReplacementsLoader primusReplacementsLoader) {
        // Virtuelle Stücklisten haben keine virtuellen Fußnoten
        if (partListEntry.getOwnerAssemblyId().isVirtual()) {
            return null;
        }

        Collection<iPartsReplacement> successorReplacements = partListEntry.getSuccessors();
        if ((successorReplacements == null) || successorReplacements.isEmpty()) {
            iPartsReplacement replacementWithoutSuccessor = primusReplacementsLoader.loadReplacementWithoutSuccessor(partListEntry);
            if (replacementWithoutSuccessor == null) {
                return null;
            }
            successorReplacements = new DwList<>();
            successorReplacements.add(replacementWithoutSuccessor);
        }

        for (iPartsReplacement successorReplacement : successorReplacements) {
            iPartsFootNote virtualFootNote = iPartsVirtualFootNoteFactory.createFromReplacement(successorReplacement);
            if (virtualFootNote != null) {
                return virtualFootNote;
            }
        }

        return null;
    }

    /**
     * Wenn eine virtuelle Fußnote am Stücklisteneintrag existiert, wird ein virtuelles (also nur im Speicher existierendes)
     * {@link iPartsDataFootNoteCatalogueRef} für diese Fußnote zurückgeliefert. Wird für die Anzeige im Edit benötigt.
     *
     * @param partListEntry
     * @return
     */
    public static iPartsDataFootNoteCatalogueRefList createVirtualFootNoteCatalogueRefListForEdit(iPartsDataPartListEntry partListEntry) {
        iPartsDataFootNoteCatalogueRefList virtualFootNoteCatalogueRefs = new iPartsDataFootNoteCatalogueRefList();
        Collection<iPartsFootNote> partListEntryFootNotes = partListEntry.getFootNotes();
        for (iPartsDefaultStandardFootNote defaultStandardFootNote : iPartsDefaultStandardFootNote.values()) {
            if (iPartsFootNoteHelper.containsFootnote(partListEntryFootNotes, defaultStandardFootNote.getFootNoteId(),
                                                      EnumSet.of(defaultStandardFootNote.getFootNoteType()))) {
                iPartsDataFootNoteCatalogueRef virtualFootNoteCatalogueRef =
                        createVirtualFootNoteCatalogueRefForEdit(partListEntry, defaultStandardFootNote);
                virtualFootNoteCatalogueRefs.add(virtualFootNoteCatalogueRef, DBActionOrigin.FROM_EDIT);
            }
        }
        return virtualFootNoteCatalogueRefs;
    }

    /**
     * Erzeugt eine Stücklistenposition-Fußnotenreferenz {@link iPartsDataFootNoteCatalogueRef} für die übergebene Standard-Fußnote.
     *
     * @param partListEntry
     * @param standardFootNote
     * @return
     */
    private static iPartsDataFootNoteCatalogueRef createVirtualFootNoteCatalogueRefForEdit(iPartsDataPartListEntry partListEntry,
                                                                                           iPartsDefaultStandardFootNote standardFootNote) {
        // Dummy iPartsDataFootNoteCatalogueRef erzeugen und einhängen
        iPartsFootNoteCatalogueRefId refId = new iPartsFootNoteCatalogueRefId(partListEntry.getAsId(), standardFootNote.getFootNoteId().getFootNoteId());
        iPartsDataFootNoteCatalogueRef footNoteCatalogueRef = new iPartsDataFootNoteCatalogueRef(partListEntry.getEtkProject(), refId);
        footNoteCatalogueRef.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        footNoteCatalogueRef.setFieldValueAsBoolean(iPartsConst.FIELD_DFNK_FN_MARKED, false, DBActionOrigin.FROM_DB);
        footNoteCatalogueRef.setFieldValueAsBoolean(iPartsConst.FIELD_DFNK_COLORTABLEFOOTNOTE, false, DBActionOrigin.FROM_DB);

        // Im Editor werden die Fußnoten via Join geladen => mische Werte von DA_FN hinzu
        DBDataObjectAttributes attributes = footNoteCatalogueRef.getAttributes();
        iPartsStandardFootNotesCache standardFootnotesCache = iPartsStandardFootNotesCache.getInstance(partListEntry.getEtkProject());
        iPartsDataFootNote dataFootNoteWithContent = standardFootnotesCache.getStandardFootNoteWithContent(standardFootNote, partListEntry.getEtkProject());
        attributes.addFields(dataFootNoteWithContent.getAttributes(), DBActionOrigin.FROM_DB);

        iPartsFootNoteContentId dataFootNoteContentId = new iPartsFootNoteContentId(dataFootNoteWithContent.getAsId().getFootNoteId(),
                                                                                    dataFootNoteWithContent.getFieldValue(iPartsConst.FIELD_DFNC_LINE_NO));
        iPartsDataFootNoteContent dataFootNoteContent = new iPartsDataFootNoteContent(partListEntry.getEtkProject(), dataFootNoteContentId);
        dataFootNoteContent.setAttributes(dataFootNoteWithContent.getAttributes(), DBActionOrigin.FROM_DB);
        dataFootNoteContent.removeForeignTablesAttributes();
        iPartsDataFootNoteContentList dataFootNoteContentAsList = new iPartsDataFootNoteContentList();
        dataFootNoteContentAsList.add(dataFootNoteContent, DBActionOrigin.FROM_DB);
        footNoteCatalogueRef.setFootNoteList(dataFootNoteContentAsList);

        return footNoteCatalogueRef;
    }

    /**
     * Index bestimmen, bei dem die virtuelle Fußnote eingehängt wird (nach Materialfußnote, vor den echten Fußnoten)
     *
     * @param currentFootNotes
     * @return
     */
    private static int findInsertIndexForVirtualFootnote(List<iPartsFootNote> currentFootNotes) {
        int index = 0;
        iPartsFootNote previousFootNote = null;
        for (iPartsFootNote footnote : currentFootNotes) {
            if ((previousFootNote != null) && previousFootNote.isPartFootnote() && !footnote.isPartFootnote()) {
                // die Teilefußnoten sind zu Ende, also den Index des Endes returnen.
                return index;
            }
            if (footnote.isRealFootnote()) {
                // es gab keine Teilefußnoten, aber die Echten fangen jetzt an, also diesen Index returnen.
                return index;
            }
            index++;
            previousFootNote = footnote;
        }
        return index;
    }

}
