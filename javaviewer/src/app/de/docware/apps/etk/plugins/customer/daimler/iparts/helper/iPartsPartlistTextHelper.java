/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.VirtualMaterialType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDIALOGChange;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDIALOGChangeList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.textkinds.iPartsDialogPartlistTextkind;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.textkinds.iPartsSaaPartlistTextkind;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogPartListTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogPosTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.misc.id.IdWithType;

import java.util.*;

/**
 * Hilfsklasse für das Holen und Verarbeiten von Stücklistentexten
 */
public class iPartsPartlistTextHelper implements iPartsConst {

    public static final String POS_PV_SEPARATOR = "||";

    /**
     * Erzeugt aus den BCTX Texten zu einer DIALOG Konstruktionsstückliste eine Map in der die Texte nach Positionsnummer- und Variante geordnet sind
     *
     * @param project
     * @param hmMSmId
     */
    public static Map<String, Map<IdWithType, PartListTexts>> getPosAndPvTextMap(EtkProject project, HmMSmId hmMSmId) {
        iPartsDataDialogPartListTextList allTextForHmMSm = iPartsDataDialogPartListTextList.loadAllTextForHmMSmId(project, hmMSmId);
        List<iPartsDataDialogPartListText> objectList = allTextForHmMSm.getAsList();
        Map<String, Map<IdWithType, PartListTexts>> result = new HashMap<>();
        for (iPartsDataDialogPartListText dataEntry : objectList) {
            String pos = dataEntry.getAsId().getPos();
            String pv = dataEntry.getAsId().getPV();
            String key = createPosAndPVKey(pos, pv);
            Map<IdWithType, PartListTexts> textsForKeyMap = result.get(key);
            if (textsForKeyMap == null) {
                textsForKeyMap = new HashMap<>();
                result.put(key, textsForKeyMap);
            }

            iPartsDialogPartListTextId idWithoutSdata = dataEntry.getAsId().getIdWithoutSdata();
            PartListTexts partListTexts = textsForKeyMap.get(idWithoutSdata);
            if (partListTexts == null) {
                partListTexts = new PartListTexts(idWithoutSdata);
                textsForKeyMap.put(idWithoutSdata, partListTexts);
            }

            EtkMultiSprache text = dataEntry.getFieldValueAsMultiLanguage(FIELD_DD_PLT_TEXT);
            iPartsDialogPartlistTextkind textkind = iPartsDialogPartlistTextkind.getFromTextkindShort(dataEntry.getFieldValue(FIELD_DD_PLT_TEXTKIND));
            PartListText partListText = new PartListText(textkind, text, dataEntry.getFieldValue(FIELD_DD_PLT_SDATA),
                                                         dataEntry.getFieldValue(FIELD_DD_PLT_SDATB), dataEntry.getAsId());
            partListText.setMaturityLevel(dataEntry.getFieldValue(FIELD_DD_PLT_RFG));
            partListText.setAAMatrix(dataEntry.getFieldValue(FIELD_DD_PLT_AATAB));

            partListTexts.addText(partListText);
        }
        return result;
    }

    /**
     * Erzeugt aus den aktuellen Positions-Texten zu einer DIALOG Konstruktionsstückliste eine Map in der die Texte nach Positionsnummer geordnet sind
     *
     * @param project
     * @param hmMSmId
     */
    public static Map<String, Map<IdWithType, PartListTexts>> getPosTextMap(EtkProject project, HmMSmId hmMSmId) {
        iPartsDataDialogPosTextList allTextForHmMSm = iPartsDataDialogPosTextList.loadAllTextForHmMSmId(project, hmMSmId);
        List<iPartsDataDialogPosText> objectList = allTextForHmMSm.getAsList();
        Map<String, Map<IdWithType, PartListTexts>> result = new TreeMap<>();
        for (iPartsDataDialogPosText dataEntry : objectList) {
            String pos = dataEntry.getAsId().getPos();
            String key = createPosAndPVKey(pos, "");
            Map<IdWithType, PartListTexts> textsForKeyMap = result.get(key);
            if (textsForKeyMap == null) {
                textsForKeyMap = new HashMap<>();
                result.put(key, textsForKeyMap);
            }

            iPartsDialogPosTextId idWithoutSdata = dataEntry.getAsId().getIdWithoutSdata();
            PartListTexts partListTexts = textsForKeyMap.get(idWithoutSdata);
            if (partListTexts == null) {
                partListTexts = new PartListTexts(idWithoutSdata);
                textsForKeyMap.put(idWithoutSdata, partListTexts);
            }

            EtkMultiSprache text = dataEntry.getFieldValueAsMultiLanguage(FIELD_DD_POS_TEXTNR);
            PartListText partListText = new PartListText(iPartsDialogPartlistTextkind.POS_TEXTS, text, dataEntry.getFieldValue(FIELD_DD_POS_SDATA),
                                                         dataEntry.getFieldValue(FIELD_DD_POS_SDATB), dataEntry.getAsId());
            partListTexts.addText(partListText);
        }
        return result;
    }

    /**
     * Erzeugt eine Liste mit POS Texten, wobei von jedem Text nur der neueste Stand geliefert wird
     *
     * @param allTextForHmMSm
     * @return
     */
    private static List<iPartsDataDialogPosText> getAsListWithNewestDataForPosTexts(iPartsDataDialogPosTextList allTextForHmMSm) {
        Map<String, iPartsDataDialogPosText> resultMap = new HashMap<String, iPartsDataDialogPosText>();
        for (iPartsDataDialogPosText dataEntry : allTextForHmMSm) {
            String key = getKeyForDataEntry(dataEntry);
            iPartsDataDialogPosText currentDataEntry = resultMap.get(key);
            if (isNewerDataEntry(currentDataEntry, dataEntry, FIELD_DD_POS_SDATA)) {
                resultMap.put(key, dataEntry);
            }
        }
        return new ArrayList<iPartsDataDialogPosText>(resultMap.values());

    }

    /**
     * Erzeugt eine Liste mit BCTX Texten, wobei von jedem Text nur der neueste Stand geliefert wird
     *
     * @param allTextForHmMSm
     * @return
     */
    private static List<iPartsDataDialogPartListText> getAsListWithNewestDataForPosPvTexts(iPartsDataDialogPartListTextList allTextForHmMSm) {
        Map<String, iPartsDataDialogPartListText> resultMap = new HashMap<String, iPartsDataDialogPartListText>();
        for (iPartsDataDialogPartListText dataEntry : allTextForHmMSm) {
            String key = getKeyForDataEntry(dataEntry);
            iPartsDataDialogPartListText currentDataEntry = resultMap.get(key);
            if (isNewerDataEntry(currentDataEntry, dataEntry, FIELD_DD_PLT_SDATA)) {
                resultMap.put(key, dataEntry);
            }
        }
        return new ArrayList<iPartsDataDialogPartListText>(resultMap.values());
    }

    /**
     * Erstellt einen Schlüssel aus den Schlüsselattributen des übergebenen Datenobjekts
     *
     * @param dataEntry
     * @return
     */
    private static String getKeyForDataEntry(EtkDataObject dataEntry) {
        String delimiter = "||";
        String key = "";
        if (dataEntry instanceof iPartsDataDialogPartListText) {
            iPartsDataDialogPartListText entry = (iPartsDataDialogPartListText)dataEntry;
            String hmmsmWithSeries = entry.getAsId().getHmMSmId().toString(delimiter);
            String pose = entry.getAsId().getPos();
            String posv = entry.getAsId().getPV();
            String ww = entry.getAsId().getWW();
            String etz = entry.getAsId().getEtz();
            String textkind = entry.getAsId().getTextArt();
            key = StrUtils.makeDelimitedString(delimiter, hmmsmWithSeries, pose, posv, ww, etz, textkind);
        } else if (dataEntry instanceof iPartsDataDialogPosText) {
            iPartsDataDialogPosText entry = (iPartsDataDialogPosText)dataEntry;
            String hmmsmWithSeries = entry.getAsId().getHmMSmId().toString(delimiter);
            String pose = entry.getAsId().getPos();
            key = StrUtils.makeDelimitedString(delimiter, hmmsmWithSeries, pose);
        }
        return key;
    }

    public static boolean isNewerDataEntry(EtkDataObject currentDataEntry, EtkDataObject dataEntry, String sdataField) {
        if (currentDataEntry != null) {
            String sortStringCurrentObject = Utils.toSortString(currentDataEntry.getFieldValue(sdataField));
            String sortStringNewObject = Utils.toSortString(dataEntry.getFieldValue(sdataField));
            if (sortStringCurrentObject.compareTo(sortStringNewObject) > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Liefert alle Positions- und Positionsvariantentexte für die DIALOG Konstruktionsstückliste. Dabei werden die
     * Tabellen DA_DIALOG_POS_TEXT und DA_DIALOG_PARTLIST_TEXT ausgelesen und die Texte in der Ergebnis-Map zusammengeführt.
     * Der Schlüssel für die jeweiligen Texte ist die Kombination aus DIALOG-Positions und DIALOG-Positionsvariante.
     *
     * @param project
     * @param hmMSmId
     * @return
     */
    public static Map<String, Map<IdWithType, PartListTexts>> getAllTextsForAllTextkinds(EtkProject project, HmMSmId hmMSmId) {
        // Positions- und Positionsvariantentexte aus DA_DIALOG_PARTLIST_TEXT
        Map<String, Map<IdWithType, PartListTexts>> posAndPvTexts = getPosAndPvTextMap(project, hmMSmId);
        // Positionstexte aus DA_DIALOG_POS_TEXT
        Map<String, Map<IdWithType, PartListTexts>> posTexts = getPosTextMap(project, hmMSmId);
        // Hier werden die Einträge aus DA_DIALOG_PARTLIST_TEXT mit den Einträgen aus DA_DIALOG_POS_TEXT vermischt
        // 1. Durchlaufe alle Einträge aus DA_DIALOG_PARTLIST_TEXT
        for (Map.Entry<String, Map<IdWithType, PartListTexts>> textsWithIdsForPosAndPv : posAndPvTexts.entrySet()) {
            // 2. Hole zum aktuellen Eintrag die dazugehörige Map aus den reinen Positionstexten
            Map<IdWithType, PartListTexts> textsForPos = posTexts.get(textsWithIdsForPosAndPv.getKey());
            // 3a. Wenn für den Schlüssel keine Map existiert (z.B. bei Positionsvariantentexten - weil diese nicht in
            // DA_DIALOG_POS_TEXT vorhanden sind) dann lege die Map unter dem Schlüssel ab.
            if (textsForPos == null) {
                posTexts.put(textsWithIdsForPosAndPv.getKey(), textsWithIdsForPosAndPv.getValue());
            } else {
                // 3b. Falls zum Schlüssel schon ein Eintrag existiert (z.B. Positionstexte aus beiden Tabellen zur gleichen Position),
                // dann durchlaufe die Map und lege den Text unter der abstrakten ID (ohne SDATA) in der zugehörigen Gruppierung ab.
                // Texte mit gleicher abstrakter ID (verschiedene Stände zu einem Text) werden im gleichen PartListTexts Objekt abgelegt.
                for (Map.Entry<IdWithType, PartListTexts> posAndPvTextsForID : textsWithIdsForPosAndPv.getValue().entrySet()) {
                    PartListTexts posPartListTexts = textsForPos.get(posAndPvTextsForID.getKey());
                    PartListTexts posAndPvPartListTexts = posAndPvTextsForID.getValue();
                    // Zur abstrakten ID gab es keine Gruppierung
                    if (posPartListTexts == null) {
                        posPartListTexts = new PartListTexts(posAndPvPartListTexts.getIdWithoutSdata());
                        textsForPos.put(posAndPvTextsForID.getKey(), posPartListTexts);
                    }
                    // Der eigentliche Text aus DA_DIALOG_PARTLIST_TEXT wird in einer neuen oder einer bestehenden Gruppierung hinzugefügt.
                    posPartListTexts.addPartListTexts(posAndPvPartListTexts);
                }
            }
        }
        return posTexts;
    }

    /**
     * Liefert alle vom Benutzer ausgewählten Textarten für DIALOG Stücklistentexte
     *
     * @return
     */
    public static Set<iPartsDialogPartlistTextkind> getSelectedDIALOGTextkinds(EtkProject project) {
        return iPartsDialogPartlistTextkind.getTextKindsFromString(iPartsUserSettingsHelper.getDIALOGPartListTextKinds(project));
    }

    /**
     * Liefert alle vom Benutzer ausgewählten Textarten für EDS/BCS Stücklistentexte
     *
     * @return
     */
    public static Set<iPartsSaaPartlistTextkind> getSelectedEDSBCSTextkinds(EtkProject project) {
        return iPartsSaaPartlistTextkind.getTextKindsFromString(iPartsUserSettingsHelper.getEDSBCSPartListTextKinds(project));
    }

    /**
     * Liefert alle vom Benutzer ausgewählten Textarten für CTT Stücklistentexte
     *
     * @return
     */
    public static Set<iPartsSaaPartlistTextkind> getSelectedCTTTextkinds(EtkProject project) {
        return iPartsSaaPartlistTextkind.getTextKindsFromString(iPartsUserSettingsHelper.getCTTPartListTextKinds(project));
    }

    /**
     * Gibt an, ob die übergebene DIALOG Stücklistentextart zu den vom Benutzer ausgewählten Textarten gehört
     *
     * @param textkind
     * @return
     */
    public static boolean isTextForSelectedDIALOGTextkind(EtkProject project, iPartsDialogPartlistTextkind textkind) {
        Set<iPartsDialogPartlistTextkind> selectedTextkinds = getSelectedDIALOGTextkinds(project);
        if ((selectedTextkinds == null) || selectedTextkinds.isEmpty()) {
            return false;
        }
        return selectedTextkinds.contains(textkind);
    }

    /**
     * Gibt an, ob die übergebene EDS/BCS Stücklistentextart zu den vom Benutzer ausgewählten Textarten gehört
     *
     * @param textkind
     * @return
     */
    public static boolean isTextForSelectedEDSBCSTextkind(EtkProject project, iPartsSaaPartlistTextkind textkind) {
        Set<iPartsSaaPartlistTextkind> selectedTextkinds = getSelectedEDSBCSTextkinds(project);
        if (selectedTextkinds.isEmpty()) {
            return false;
        }
        return selectedTextkinds.contains(textkind);
    }

    /**
     * Gibt an, ob die übergebene CTT Stücklistentextart zu den vom Benutzer ausgewählten Textarten gehört
     *
     * @param textkind
     * @return
     */
    public static boolean isTextForSelectedCTTTextkind(EtkProject project, iPartsSaaPartlistTextkind textkind) {
        Set<iPartsSaaPartlistTextkind> selectedTextkinds = getSelectedCTTTextkinds(project);
        if (selectedTextkinds.isEmpty()) {
            return false;
        }
        return selectedTextkinds.contains(textkind);
    }

    /**
     * Erzeugt Text für virtuelles Feld für DIALOG Änderungsgrund (kommaseparierte Liste der Änderungsarten).
     * Evtl. muss man später mal die BCTE-Änderungen und Material-Änderungen unterscheiden können. Für den aktuellen
     * Anzeigezweck könnte man beide Listen zusammenfassen.
     *
     * @param dataDIALOGChangesForBCTE Liste der DIALOG Änderungssätze an BCTE-Key
     * @param dataDIALOGChangesForMat  Liste der DIALOG Änderungssätze an Materialnummer
     * @return
     */
    public static String getDIALOGChangeReason(iPartsDataDIALOGChangeList dataDIALOGChangesForBCTE,
                                               iPartsDataDIALOGChangeList dataDIALOGChangesForMat) {
        if (((dataDIALOGChangesForBCTE == null) || dataDIALOGChangesForBCTE.isEmpty()) &&
            ((dataDIALOGChangesForMat == null) || dataDIALOGChangesForMat.isEmpty())) {
            return "";
        }

        // Änderungsarten ermitteln: Anzeigestring ist kommasep. Liste der Änderungsarten
        Set<String> displayedChangeReasonSet = new TreeSet<String>(); // Änderungsarten sortieren
        appendToChangeReason(dataDIALOGChangesForBCTE, displayedChangeReasonSet);
        appendToChangeReason(dataDIALOGChangesForMat, displayedChangeReasonSet);

        StringBuilder changeReasons = new StringBuilder();
        for (String displayedChangeReason : displayedChangeReasonSet) {
            if (changeReasons.length() > 0) {
                changeReasons.append(", ");
            }
            changeReasons.append(displayedChangeReason);
        }

        return changeReasons.toString();
    }

    private static void appendToChangeReason(iPartsDataDIALOGChangeList dataDIALOGChanges, Set<String> displayedChangeReasonSet) {
        if (dataDIALOGChanges != null) {
            for (iPartsDataDIALOGChange dataChange : dataDIALOGChanges) {
                iPartsDataDIALOGChange.ChangeType changeType = iPartsDataDIALOGChange.ChangeType.getChangeType(dataChange.getAsId().getDoType());
                displayedChangeReasonSet.add(TranslationHandler.translate(changeType.getDisplayKey()));
            }
        }
    }

    public static long getTextSdatValueAsLong(String sdatString) {
        long sdat = StrUtils.strToLongDef(sdatString, -1);

        // leerer String bedeutet unendlich
        if (sdat == -1) {
            if (sdatString.isEmpty()) {
                sdat = Long.MAX_VALUE;
            } else {
                return -1;
            }
        }
        return sdat;
    }

    /**
     * Filter alle Stücklistentexte aus, deren Stücklistenpositionen ausgefiltert wurden
     *
     * @param allEntries
     * @return
     */
    public static List<EtkDataPartListEntry> filterPositionTextsWithoutVisibleEntry(EtkProject project, List<EtkDataPartListEntry> allEntries) {
        if (allEntries != null) {
            // Benutzer hat keine Textarten ausgewählt -> hier raus, da Prüfungen nicht benötigt
            if (StrUtils.isEmpty(iPartsUserSettingsHelper.getDIALOGPartListTextKinds(project))) {
                return allEntries;
            }

            // 1. Prüfung: BCTX Texte (Positionsvariantentexte mit PV-Nummer)
            boolean hasPosText = false; // Kenner für die zweite Prüfung, ob Positionstexte ohne Stücklistenpositionen vorhanden sind
            EtkDataPartListEntry currentEntry = null;
            Iterator<EtkDataPartListEntry> iterator = allEntries.iterator();
            Set<PartListEntryId> existingIds = new HashSet<>();
            while (iterator.hasNext()) {
                EtkDataPartListEntry partListEntry = iterator.next();
                // Ist die aktuelle Position ein Stücklistentext?
                if (VirtualMaterialType.isPartListTextEntry(partListEntry)) {
                    String currentPosV = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_POSV);
                    // Wenn es ein Text ist und keine Positionsvariante vorhanden ist, dann handelt es sich um einen
                    // Positionstext -> Bedingung für die zweite Prüfung
                    hasPosText |= StrUtils.isEmpty(currentPosV);
                    if (StrUtils.isValid(currentPosV) && (existingIds.contains(partListEntry.getAsId()) || checkPartListPOSVTextValidForEntry(currentEntry, partListEntry))) {
                        iterator.remove();
                    } else {
                        // Positionsvariantentexte stehen direkt nach der Stücklistenposition. Es kann vorkommen, dass der
                        // gleiche Text hinter mehreren verschiedenen Stücklistenpositionen steht. Sollten nachfolgende Psotionen
                        // entfallen, würden alle (die gleichen) Texte hinter einer Stücklistenposition stehen. Um das zu verhindern
                        // werden die IDs aller Texte nach einer Stücklistenposition gehalten. Doppelte werden somit entfernt.
                        existingIds.add(partListEntry.getAsId());
                    }
                } else {
                    // Kein Text, Stücklisteneintrag merken
                    currentEntry = partListEntry;
                    existingIds.clear();
                }
            }

            // Keine Positionstexte vorhanden-> aktuelles Ergebnis zurückliefern
            if (!hasPosText) {
                return allEntries;
            }

            List<EtkDataPartListEntry> result = new ArrayList<>();
            // 2. Prüfung: POSX Texte (Positionstexte ohne PV-Nummer)
            String entryPos = "";
            // Positionstexte stehen vor eine Stücklistenposition. Um zu bestimmen, ob ein Text entfernt werden soll, muss
            // die Liste aller rechtlichen Stücklistenpositionen von hinten durchlaufen werden.
            for (int index = allEntries.size() - 1; index >= 0; index--) {
                boolean addEntry;
                EtkDataPartListEntry partListEntry = allEntries.get(index);
                // Ist die aktuelle Position ein Stücklistentext?
                String textPos = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_POSE);
                if (VirtualMaterialType.isPartListTextEntry(partListEntry)) {
                    String textPosV = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_POSV);
                    // Eintrag übernehmen, wenn der Stücklistentext einen PV-Nummer hat (Positionsvariantentext) oder die
                    // gleiche Positionsnummer wie der Stücklisteneintrag danach hat. Positionsnummer der Stücklistenposition
                    // muss vorhanden sein. Falls nicht, ist der letzte Eintrag in der Liste ein Positionstext
                    addEntry = StrUtils.isValid(textPosV) || (StrUtils.isValid(entryPos) && textPos.equals(entryPos));
                } else {
                    // kein Stücklistentext -> Stücklistenposition übernehmen und Positionsnummer merken
                    entryPos = textPos;
                    addEntry = true;
                }
                // Einträge ganz vorne einfügen, da die Liste von hinten durchlaufen wird
                if (addEntry) {
                    result.add(0, partListEntry);
                }
            }
            return result;
        } else {
            return new ArrayList<>();
        }

    }

    /**
     * Überprüft, ob der übergebene Positionsvariantentext zur übergebenen Stücklistenposition gehört
     *
     * @param currentEntry
     * @param posvTextEntry
     * @return
     */
    private static boolean checkPartListPOSVTextValidForEntry(EtkDataPartListEntry currentEntry, EtkDataPartListEntry posvTextEntry) {
        if (currentEntry == null) {
            return true;
        }
        List<String> validAAsFromText = posvTextEntry.getFieldValueAsSetOfEnum(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA_SOE);
        String entryAA = currentEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA);
        if (validAAsFromText.isEmpty() || validAAsFromText.contains(entryAA)) {
            // Werte für Vergleich vom Positionsvarianten Text
            String posvTextPosE = posvTextEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_POSE);
            String posvTextPosV = posvTextEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_POSV);
            String posvTextETZ = posvTextEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_ETZ);
            String posvTextWW = posvTextEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_WW);
            String posvTextSDATA = posvTextEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SDATA);
            String posvTextSDATB = posvTextEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SDATB);
            // Werte für Vergleich vom Stücklistenposition
            String entryPosE = currentEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_POSE);
            String entryPosV = currentEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_POSV);
            String entryETZ = currentEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_ETZ);
            String entryWW = currentEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_WW);
            String entrySDATB = currentEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SDATB);
            // Beim Erzeugen der Positionsvariantentexte wird geprüft, ob POSE, POSV, ETZ, WW beim Texteintrag und bei
            // der Stücklistenposition gleich sind. Zusätzlich muss die Ausführungsart Teil der AA-Matrix des
            // Textes sein und die Gültigkeitsbereiche von beiden müssen sich überschneiden.
            // Die gleichen Prüfungen müssen hier ebenfalls durchgeführt werden.
            if (!posvTextPosE.equals(entryPosE) || !posvTextPosV.equals(entryPosV) ||
                !checkSameETZAndWWValues(posvTextETZ, posvTextWW, entryETZ, entryWW) ||
                !checkPOSVAndEntryDateValues(entrySDATB, posvTextSDATA, posvTextSDATB)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check, ob Ersatzteilzähler und Wahlweise von Stücklistentext und Stücklistenposition gleich sind
     *
     * @param posvTextETZ
     * @param posvTextWW
     * @param entryETZ
     * @param entryWW
     * @return
     */
    public static boolean checkSameETZAndWWValues(String posvTextETZ, String posvTextWW, String entryETZ, String entryWW) {
        return posvTextETZ.equals(entryETZ) && posvTextWW.equals(entryWW);
    }

    /**
     * Check, ob der Stücklistentext zu den Datumsangaben der Stücklistenposition passt. Die Gültigkeitsbereiche müssen sich überschneiden
     *
     * @param entrySDATB
     * @param posvTextSDATA
     * @param posvTextSDATB
     * @return
     */
    public static boolean checkPOSVAndEntryDateValues(String entrySDATB, String posvTextSDATA, String posvTextSDATB) {
        long sdatbFromEntry = getTextSdatValueAsLong(entrySDATB);
        long sdataFromText = getTextSdatValueAsLong(posvTextSDATA);
        long sdatbFromText = getTextSdatValueAsLong(posvTextSDATB);
        return (sdataFromText < sdatbFromEntry) && (sdatbFromText >= sdatbFromEntry);
    }

    public static class PartListTexts {

        private Map<IdWithType, PartListText> texts;
        private IdWithType idWithoutSdata;

        public PartListTexts(IdWithType idWithoutSdata) {
            this.idWithoutSdata = idWithoutSdata;
            this.texts = new TreeMap<>();
        }

        public void addText(PartListText text) {
            texts.put(text.getOriginalTextId(), text);
        }

        public Map<IdWithType, PartListText> getTexts() {
            return texts;
        }

        public PartListText getPartListTextForId(IdWithType id) {
            return texts.get(id);
        }

        public IdWithType getIdWithoutSdata() {
            return idWithoutSdata;
        }

        public String getWW() {
            if (idWithoutSdata instanceof iPartsDialogPartListTextId) {
                return ((iPartsDialogPartListTextId)idWithoutSdata).getWW();
            }
            return "";
        }

        public String getETZ() {
            if (idWithoutSdata instanceof iPartsDialogPartListTextId) {
                return ((iPartsDialogPartListTextId)idWithoutSdata).getEtz();
            }
            return "";
        }

        public void addPartListTexts(PartListTexts posAndPvPartListTexts) {
            if (posAndPvPartListTexts != null) {
                for (PartListText text : posAndPvPartListTexts.getTexts().values()) {
                    addText(text);
                }
            }
        }
    }

    /**
     * Hilfsklasse, die Informationen zu einem Stücklistentext hält
     */
    public static class PartListText {

        private iPartsDialogPartlistTextkind textKind;
        private EtkMultiSprache text;
        private IdWithType originalTextId;
        private String sdata;
        private String sdatb;
        private String aaMatrix;
        private Set<String> aaMatrixSet;
        private String maturityLevel;

        public PartListText(iPartsDialogPartlistTextkind textKind, EtkMultiSprache text, String sdata, String sdatb, IdWithType originalTextId) {
            this.textKind = textKind;
            this.text = text;
            this.originalTextId = originalTextId;
            this.sdata = sdata;
            this.sdatb = sdatb;
            this.maturityLevel = "";
        }

        public iPartsDialogPartlistTextkind getTextKind() {
            return textKind;
        }

        public EtkMultiSprache getText() {
            return text;
        }

        public EtkMultiSprache getTextWithPrefix() {
            EtkMultiSprache result = new EtkMultiSprache();
            for (Map.Entry<String, String> entry : text.getLanguagesAndTexts().entrySet()) {
                result.setText(entry.getKey(), getTextKind().getTxtKindShort() + ": " + entry.getValue());
            }
            return result;
        }

        public IdWithType getOriginalTextId() {
            return originalTextId;
        }

        public String getSdata() {
            return sdata;
        }

        public String getSdatb() {
            return sdatb;
        }

        public String getAAMatrixAsString() {
            return aaMatrix;
        }

        public String getMaturity() {
            return maturityLevel;
        }

        public void setAAMatrix(String aaMatrix) {
            if (StrUtils.isValid(aaMatrix)) {
                aaMatrixSet = new HashSet<>(StrUtils.toStringList(aaMatrix, " ", false));
                this.aaMatrix = aaMatrix;
            }
        }

        public boolean hasAAValue(String aaValue) {
            if (aaMatrixSet != null) {
                return aaMatrixSet.contains(aaValue);
            }
            return false;
        }

        public void setMaturityLevel(String maturityLevel) {
            if (maturityLevel != null) {
                this.maturityLevel = maturityLevel;
            }
        }
    }

    public static String createPosAndPVKey(String pos, String pv) {
        return pos + POS_PV_SEPARATOR + pv;
    }
}
