/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper.footnotes;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFootNote;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Hilfsklasse zur Berechnung von echten Fußnoten. Also Fußnoten, die über einen in der DB existierenden Eintrag in
 * {@link iPartsDataFootNoteCatalogueRef} am {@link iPartsDataPartListEntry} hängen.
 */
public class iPartsFootNoteHelper {

    private static final String INFINITE_VALUE = String.valueOf(Long.MAX_VALUE);

    /**
     * Überprüfung, ob die Fußnote mit der übergebenen Id bereits in der Sammlung von Fußnoten vorhanden ist
     *
     * @param footNotes
     * @param footNoteId
     * @param footNoteTypes Optionale Fußnoten-Typen; bei {@code null} ist der Fußnoten-Typ egal
     * @return
     */
    public static boolean containsFootnote(Collection<iPartsFootNote> footNotes, iPartsFootNoteId footNoteId,
                                           EnumSet<iPartsFootnoteType> footNoteTypes) {
        if (footNotes != null) {
            for (iPartsFootNote footnote : footNotes) {
                if (footnote.getFootNoteId().equals(footNoteId)) {
                    if ((footNoteTypes == null) || footNoteTypes.contains(footnote.getFootnoteType())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Bestimmt die gecachten Teilestamm und DIALOG Fußnoten der übergebenen Stücklistenposition und fügt sie der
     * übergebenen <code>allFootnotes</code> Collection hinzu.
     *
     * @param partListEntry
     * @param allFootnotes
     * @param alreadyCollectedFootNotes
     * @param partFootNotesCache
     * @param dialogFootNotesCache
     */
    public static void addPartAndDIALOGFootnotes(EtkDataPartListEntry partListEntry, Collection<iPartsFootNote> allFootnotes,
                                                 Set<iPartsFootNoteId> alreadyCollectedFootNotes, iPartsPartFootnotesCache partFootNotesCache,
                                                 iPartsDIALOGFootNotesCache dialogFootNotesCache) {
        EtkProject project = partListEntry.getEtkProject();
        // Teilestamm-Fußnoten
        List<iPartsFootNote> footnotesForPart = partFootNotesCache.getFootnotesForPart(project, partListEntry.getPart().getAsId());
        addCachedFootnotes(footnotesForPart, allFootnotes, alreadyCollectedFootNotes);
        // DIALOG Fußnoten (VBFN)
        List<iPartsFootNote> footnotesForBCTEKey = dialogFootNotesCache.getFootnotesForBCTEKey(project, partListEntry);
        addCachedFootnotes(footnotesForBCTEKey, allFootnotes, alreadyCollectedFootNotes);
    }

    private static void addCachedFootnotes(List<iPartsFootNote> footnotes, Collection<iPartsFootNote> allFootnotes,
                                           Set<iPartsFootNoteId> alreadyCollectedFootnotes) {
        if ((footnotes != null) && !footnotes.isEmpty()) {
            for (iPartsFootNote footnote : footnotes) {
                iPartsFootNoteId footnoteId = footnote.getFootNoteId();
                if (!alreadyCollectedFootnotes.contains(footnoteId)) {
                    allFootnotes.add(footnote);
                    alreadyCollectedFootnotes.add(footnoteId);
                }
            }
        }
    }

    /**
     * Liefert alle DIALOG Fußnotenreferenzobjekte samt Fußnoteninhalte für den Edit von Fußnoten
     *
     * @param project
     * @param partListEntry
     * @return
     */
    public static iPartsDataFootNotePosRefList getDIALOGFootnoteContentsForPartListEntryWithJoinedFields(EtkProject project,
                                                                                                         iPartsDataPartListEntry partListEntry) {
        iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(partListEntry);
        iPartsDataFootNotePosRefList result = new iPartsDataFootNotePosRefList();
        if (bctePrimaryKey != null) {
            String sDataFromEntry = bctePrimaryKey.sData;
            String sDatbFromEntry = getSdatbFromEntry(partListEntry);
            bctePrimaryKey.sData = "*";
            iPartsDataFootNotePosRefList footnotePosRefs = new iPartsDataFootNotePosRefList();
            EtkDisplayFields selectFields = project.getAllDisplayFieldsForTable(iPartsConst.TABLE_DA_FN_CONTENT);
            selectFields.addFelder(project.getAllDisplayFieldsForTable(iPartsConst.TABLE_DA_FN_POS));
            selectFields.addFelder(project.getAllDisplayFieldsForTable(iPartsConst.TABLE_DA_FN));
            footnotePosRefs.searchSortAndFillWithMultiLangValueForAllLanguagesAndJoin(project, selectFields,
                                                                                      TableAndFieldName.make(iPartsConst.TABLE_DA_FN_CONTENT, iPartsConst.FIELD_DFNC_TEXT),
                                                                                      new String[]{ TableAndFieldName.make(iPartsConst.TABLE_DA_FN_POS, iPartsConst.FIELD_DFNP_GUID) },
                                                                                      new String[]{ bctePrimaryKey.createDialogGUID() }, false, null, false, true,
                                                                                      new EtkDataObjectList.JoinData(iPartsConst.TABLE_DA_FN_CONTENT,
                                                                                                                     new String[]{ iPartsConst.FIELD_DFNP_FN_NO },
                                                                                                                     new String[]{ iPartsConst.FIELD_DFNC_FNID },
                                                                                                                     false, false),
                                                                                      new EtkDataObjectList.JoinData(iPartsConst.TABLE_DA_FN,
                                                                                                                     new String[]{ iPartsConst.FIELD_DFNP_FN_NO },
                                                                                                                     new String[]{ iPartsConst.FIELD_DFN_ID },
                                                                                                                     false, false));
            // Konstruktionsfußnoten können auch Tabellenfußnoten sein. Daher werden hier über den Join auf
            // DA_FN_CONTENT zu einer POS-Referenz mehrere Datensätze geliefert. Da wir aber nur einen brauchen, werden
            // alle anderen übersprungen.
            Set<iPartsFootNotePosRefId> createdRefs = new HashSet<>();
            for (iPartsDataFootNotePosRef footnotePosRef : footnotePosRefs) {
                if (createdRefs.contains(footnotePosRef.getAsId())) {
                    continue;
                }
                createdRefs.add(footnotePosRef.getAsId());
                iPartsDialogBCTEPrimaryKey primaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(footnotePosRef.getAsId().getBCTEKey());
                if (primaryKey != null) {
                    if (isDIALOGFootnoteDatesValidForPartListEntryDates(sDataFromEntry, sDatbFromEntry, primaryKey.sData,
                                                                        footnotePosRef.getFieldValue(iPartsConst.FIELD_DFNP_SDATB))) {
                        result.add(footnotePosRef, DBActionOrigin.FROM_DB);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Überprüft, ob das Zeitintervall der Fußnote gegenüber dem Zeitintervall der Position gültig ist
     *
     * @param sDataFromEntry
     * @param sDatbFromEntry
     * @return
     */
    public static boolean isDIALOGFootnoteDatesValidForPartListEntryDates(String sDataFromEntry, String sDatbFromEntry,
                                                                          String sdataFromFN, String sdatbFromFN) {
        String sdataEntry = getSdatValue(sDataFromEntry);
        String sdatbEntry = getSdatValue(sDatbFromEntry);
        String sdataFN = getSdatValue(sdataFromFN);
        String sdatbFN = getSdatValue(sdatbFromFN);
        boolean entryEarlierOrEquals = (sdataEntry.compareTo(sdataFN) <= 0) && ((sdatbEntry.compareTo(sdataFN) > 0));
        if (entryEarlierOrEquals) {
            return true;
        }
        return (sdataEntry.compareTo(sdataFN) > 0) && (sdataEntry.compareTo(sdatbFN) < 0);

    }

    private static String getSdatValue(String sDataValue) {
        return StrUtils.isValid(sDataValue) ? sDataValue : INFINITE_VALUE;
    }

    public static String getSdatbFromEntry(EtkDataPartListEntry partListEntry) {
        EtkDataAssembly assembly = partListEntry.getOwnerAssembly();
        if ((assembly instanceof iPartsDataAssembly) && ((iPartsDataAssembly)assembly).isDialogSMConstructionAssembly()) {
            return ((iPartsDataPartListEntry)partListEntry).getSDATB();
        } else {
            return partListEntry.getFieldValue(iPartsConst.FIELD_K_DATETO);
        }
    }
}
