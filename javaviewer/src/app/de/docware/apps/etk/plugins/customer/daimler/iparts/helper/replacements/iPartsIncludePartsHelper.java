package de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFieldKeyNormal;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataReservedPKList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataIncludePart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataIncludePartList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataReplacePart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsIncludePartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsScoringHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacement;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hilfsklasse für das Editieren von Mitlieferteilen
 */
public class iPartsIncludePartsHelper {

    // Für die anderen Felder in DA_INCLUDE_PART gibt es kein Mapping, da sich die Daten nicht direkt auf den
    // Stücklisteneintrag beziehen. Da aber nur Stücklisteneinträge angezeigt werden können, werden diese ignoriert.
    private static final Map<String, String> INCLUDE_PART_FIELD_MAPPING = new HashMap<>();

    static {
        INCLUDE_PART_FIELD_MAPPING.put(iPartsConst.FIELD_DIP_INCLUDE_MATNR, iPartsConst.FIELD_K_MATNR);
        INCLUDE_PART_FIELD_MAPPING.put(iPartsConst.FIELD_DIP_INCLUDE_QUANTITY, iPartsConst.FIELD_K_MENGE);
    }

    public static boolean containsSameIncludePart(iPartsDataIncludePart includePart, iPartsDataIncludePartList includePartsRetail) {
        for (iPartsDataIncludePart currentIncludePart : includePartsRetail) {
            if (includePart.isDuplicateOf(currentIncludePart)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Berechnet die nächste höchste SeqNo für die Mitlieferteile anhand der geladenen ({@link iPartsDataIncludePartList})
     * und reserviert den Primärschlüssel dafür.
     *
     * @param project
     * @param dataReplacement Ersetzung, zu der das neue Mitlieferteil hinzugefügt werden soll.
     * @return
     */
    public static String getNextIncludeSeqNo(EtkProject project, iPartsDataReplacePart dataReplacement) {
        int maxSeqNo = 0;
        // Gleicher Primärschlüssel bis auf die Sequenznummer, die wir gerade bestimmen
        iPartsDataIncludePartList includePartsRetail = iPartsDataIncludePartList.loadIncludePartsForReplacement(project, dataReplacement);
        for (iPartsDataIncludePart includePart : includePartsRetail) {
            iPartsIncludePartId includePartId = includePart.getAsId();
            int seqNo = StrUtils.strToIntDef(includePartId.getIncludeSeqNo(), 0);
            if (seqNo > maxSeqNo) {
                maxSeqNo = seqNo;
            }
        }

        maxSeqNo++;
        while (true) { // Abbruch durch return
            String formattedSeqNo = StrUtils.prefixStringWithCharsUpToLength(Integer.toString(maxSeqNo), '0',
                                                                             iPartsIncludePartId.SEQNO_LENGTH, false);
            if (iPartsDataReservedPKList.reservePrimaryKey(project, new iPartsIncludePartId(dataReplacement, formattedSeqNo))) {
                return formattedSeqNo;
            } else {
                maxSeqNo++;
            }
        }
    }

    /**
     * Liefert für das übergebene Mitlieferteil einen Klon des Stücklisteneintrags aus der Stückliste, der die gleiche Menge und
     * Materialnummer wie das Mitlieferteil hat und laut Scoring am ehesten zur übergebenen Ersetzung des Mitlieferteils passt.
     * Falls es keinen Treffer gibt, wird ein leerer {@link EtkDataPartListEntry} mit der Materialnummer zurückgeliefert.
     *
     * @param replacement
     * @param includePart
     * @param partList
     * @return
     */
    public static EtkDataPartListEntry getPartListEntryForIncludePart(iPartsReplacement replacement, EtkDataObject includePart,
                                                                      List<EtkDataPartListEntry> partList, String matnrField) {
        // Der Nachfolger ist besser für das Scoring, notfalls der Vorgänger
        EtkDataPartListEntry partListEntryForScoring;
        if (replacement.successorEntry != null) {
            partListEntryForScoring = replacement.successorEntry;
        } else {
            partListEntryForScoring = replacement.predecessorEntry;
        }

        String includeMatNr = includePart.getFieldValue(matnrField);

        // Flag logLoadFieldIfNeeded temporär für alle Stücklisteneinträge auf false setzen, um unnötige Performance-
        // Logausgaben zu vermeiden
        Map<EtkDataPartListEntry, Boolean> oldLogLoadFieldIfNeededMap = new HashMap<>();
        oldLogLoadFieldIfNeededMap.put(partListEntryForScoring, partListEntryForScoring.isLogLoadFieldIfNeeded());
        List<EtkDataPartListEntry> foundPartListEntries;
        try {
            partListEntryForScoring.setLogLoadFieldIfNeeded(false);

            // Mögliche Stücklisteneinträge mit passender includeMatNr suchen und über Scoring die besten herausfinden
            List<EtkDataPartListEntry> possiblePartListEntries = new DwList<>();
            for (EtkDataPartListEntry entry : partList) {
                if (entry.getPart().getAsId().getMatNr().equals(includeMatNr)) {
                    possiblePartListEntries.add(entry);
                    oldLogLoadFieldIfNeededMap.put(entry, entry.isLogLoadFieldIfNeeded());
                    entry.setLogLoadFieldIfNeeded(false);
                }
            }

            foundPartListEntries = iPartsScoringHelper.getMostEqualPartListEntries(partListEntryForScoring, possiblePartListEntries);
        } finally {
            for (Map.Entry<EtkDataPartListEntry, Boolean> oldLogFieldIfNeededEntry : oldLogLoadFieldIfNeededMap.entrySet()) {
                oldLogFieldIfNeededEntry.getKey().setLogLoadFieldIfNeeded(oldLogFieldIfNeededEntry.getValue());
            }
        }

        EtkDataPartListEntry resultPartListEntry;
        // Falls kein passender Stücklisteneintrag gefunden wurde, ein leeres Teil erzeugen
        if (foundPartListEntries.isEmpty()) {
            PartListEntryId dummyPartListEntryId = new PartListEntryId();
            resultPartListEntry = EtkDataObjectFactory.createDataPartListEntry(includePart.getEtkProject(),
                                                                               dummyPartListEntryId);
            resultPartListEntry.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            resultPartListEntry.setFieldValue(EtkDbConst.FIELD_K_MATNR, includeMatNr, DBActionOrigin.FROM_DB);
            EtkDataPart part = resultPartListEntry.getPart();
            if (!part.existsInDB()) {
                part.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                part.setFieldValue(EtkDbConst.FIELD_M_BESTNR, includeMatNr, DBActionOrigin.FROM_DB);
            }
        } else {
            // Einen Klon des ersten Treffers der am besten passendsten Stücklisteneinträge nehmen
            resultPartListEntry = foundPartListEntries.get(0).cloneMe(includePart.getEtkProject());
        }

        return resultPartListEntry;
    }

    /**
     * Übernimmt die Werte des übergebenen Mitlieferteils in die entsprechenden Felder des übergebenen Stücklisteneintrags.
     *
     * @param includePart
     * @param partListEntryForIncludePart
     */
    public static void mergeValuesIntoPartListEntry(iPartsDataIncludePart includePart, EtkDataPartListEntry partListEntryForIncludePart) {
        for (Map.Entry<String, String> entry : INCLUDE_PART_FIELD_MAPPING.entrySet()) {
            String includePartField = entry.getKey();
            String partListEntryField = entry.getValue();
            partListEntryForIncludePart.setFieldValue(partListEntryField, includePart.getFieldValue(includePartField),
                                                      DBActionOrigin.FROM_DB);
        }
    }

    /**
     * Konvertiert den übergebenen Stücklisteneintrag zu einem Mitlieferteil der übergebenen Ersetzung.
     *
     * @param project
     * @param dataReplacePart
     * @param partListEntryWithIncludePart
     * @return
     */
    public static iPartsDataIncludePart convertPartListEntryToIncludePart(EtkProject project, iPartsDataReplacePart dataReplacePart,
                                                                          EtkDataPartListEntry partListEntryWithIncludePart) {
        iPartsIncludePartId tmpIncludePartId = new iPartsIncludePartId(dataReplacePart, "");
        iPartsDataIncludePart newIncludePart = new iPartsDataIncludePart(project, tmpIncludePartId);
        newIncludePart.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        for (Map.Entry<String, String> entry : INCLUDE_PART_FIELD_MAPPING.entrySet()) {
            String includePartField = entry.getKey();
            String partListEntryField = entry.getValue();
            newIncludePart.setFieldValue(includePartField, partListEntryWithIncludePart.getFieldValue(partListEntryField),
                                         DBActionOrigin.FROM_EDIT);
        }
        return newIncludePart;
    }

    public static EtkDisplayField convertToPartListDisplayField(EtkDisplayField includePartDisplayField) {
        String fieldName = INCLUDE_PART_FIELD_MAPPING.get(includePartDisplayField.getKey().getFieldName());
        if (fieldName != null) {
            EtkDisplayField convertedDisplayField = includePartDisplayField.cloneMe();
            convertedDisplayField.setKey(new EtkDisplayFieldKeyNormal(iPartsConst.TABLE_KATALOG, fieldName));
            return convertedDisplayField;
        }
        return null;
    }
}
