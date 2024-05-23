package de.docware.apps.etk.plugins.customer.daimler.iparts.helper.footnotes;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFootNote;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsRFMEA;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsRFMEN;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacement;

/**
 * Factory, die aufgrund der RFMEA-Flags oder Code-Vorwärts (bei PRIMUS) einer Ersetzung die entsprechende virtuelle
 * Fußnote zurückliefert. Diese kann nur eine der Standardfußnoten aus {@link iPartsDefaultStandardFootNote} sein.
 */
public class iPartsVirtualFootNoteFactory {

    public static iPartsFootNote createFromReplacement(iPartsReplacement successorReplacement) {
        iPartsFootNote virtualFootNote = null;
        iPartsDefaultStandardFootNote replacementFootNote = null;
        if (successorReplacement.source == iPartsReplacement.Source.PRIMUS) {
            if (successorReplacement.primusCodeForward.equals("01") || successorReplacement.primusCodeForward.equals("02")) {
                replacementFootNote = iPartsDefaultStandardFootNote.FN_NO_LONGER_DELIVERED;
            } else if (successorReplacement.primusCodeForward.equals("11")) {
                replacementFootNote = iPartsDefaultStandardFootNote.FN_NO_LONGER_DELIVERED_ORDER_CONTENTS;
            } else if (successorReplacement.primusCodeForward.equals("25")) {
                replacementFootNote = iPartsDefaultStandardFootNote.FN_NO_LONGER_DELIVERED_ORDER_PARTS;
            }
        } else {
            // RFMEA Flags auswerten
            iPartsRFMEA rfmea = new iPartsRFMEA(successorReplacement.rfmeaFlags);
            if (rfmea.isUsePredecessorForbidden()) {  // V0
                // zur Sicherheit das RFMEN Flag kontrollieren
                iPartsRFMEN rfmen = new iPartsRFMEN(successorReplacement.rfmenFlags);
                if (rfmen.isPredecessorDirectReplaceable()) {  // X
                    replacementFootNote = iPartsDefaultStandardFootNote.FN_DONT_USE_OLD_PART;
                }
            }
        }

        if (replacementFootNote != null) {
            virtualFootNote = createVirtualFootNote(replacementFootNote, successorReplacement.predecessorEntry.getEtkProject());
        }

        return virtualFootNote;
    }

    private static iPartsFootNote createVirtualFootNote(iPartsDefaultStandardFootNote standardFootNote, EtkProject project) {
        iPartsStandardFootNotesCache standardFootNotesCache = iPartsStandardFootNotesCache.getInstance(project);
        iPartsFootNote footNote = standardFootNotesCache.getStandardFootNote(standardFootNote, project);
        // footNote ist bereits ein Klon (damit der Fußnotentyp ohne Nebeneffekte im Cache gesetzt werden kann)
        footNote.setFootnoteType(iPartsFootnoteType.REPLACE_FOOTNOTE);
        return footNote;
    }
}
