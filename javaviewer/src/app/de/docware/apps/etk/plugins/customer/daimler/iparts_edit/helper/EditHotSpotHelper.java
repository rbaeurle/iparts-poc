/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleFormIConnector;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;

import java.util.*;

public class EditHotSpotHelper {

    private EditModuleFormIConnector dataConnector;
    private Set<String> posNumberSet;
    private Set<String> hotspotSet;
    private Map<IdWithType, Set<String>> hotSpotSetDividedPerImage;
    private DBDataObjectList<EtkDataImage> imageList;

    public enum KEY_FAULTY_POS_MAP {
        POSNO_WITHOUT_HOTSPOTS("!!Teileposition hat Hotspot ohne Bild (%1)"),
        INVALID_POSNO("!!Ungültige Teilepositionsnummer (%1)"),
        EMPTY_POSNO("!!Stückliste enthält Einträge ohne Positionsnummer");

        private String errorMsg;

        KEY_FAULTY_POS_MAP(String errorMsg) {
            this.errorMsg = errorMsg;
        }

        public String getErrorMsg() {
            return errorMsg;
        }
    }

    public EditHotSpotHelper(EditModuleFormIConnector dataConnector) {
        if (dataConnector != null) {
            this.dataConnector = dataConnector;
            this.posNumberSet = dataConnector.getPosNumberSet();
            this.imageList = dataConnector.getCurrentAssembly().getUnfilteredImages();
        }
        hotspotSet = new HashSet<>();
        hotSpotSetDividedPerImage = new HashMap<>();
    }

    /**
     * Füllt die Map mit Hotspots, aufgeteilt nach Bildtafelnummern
     */
    private void setHotspotSetDividedPerImage() {
        if (imageList != null && !imageList.isEmpty()) {
            for (EtkDataImage image : imageList) {
                Set<String> hotSpotListPerImage = dataConnector.getHotSpotSetPerImage(image);
                hotSpotSetDividedPerImage.put(image.getPoolEntryId(), hotSpotListPerImage);
            }
        }
    }

    /**
     * Setzt hotspotSet anhand eines übergebenen Sets
     *
     * @param hotspotSet
     */
    private void setHotspotSetPerImage(Set<String> hotspotSet) {
        this.hotspotSet = hotspotSet;
    }

    /**
     * Füllt das hotspotSet mit allen Hotspots, nicht aufgeteilt nach Bildtafelnummern
     */
    private void setHotspotSetFromMap() {
        if (hotSpotSetDividedPerImage.isEmpty()) {
            setHotspotSetDividedPerImage();
        }
        for (IdWithType key : hotSpotSetDividedPerImage.keySet()) {
            hotspotSet.addAll(hotSpotSetDividedPerImage.get(key));
        }
    }

    /**
     * Sucht die fehlerhaften Hotspots pro Bildtafelnummer
     *
     * @return
     */
    public Map<IdWithType, List<String>> getFaultyHotspots() {
        Map<IdWithType, List<String>> result = new HashMap<>();
        setHotspotSetDividedPerImage();
        if (!hotSpotSetDividedPerImage.isEmpty()) {
            for (IdWithType key : hotSpotSetDividedPerImage.keySet()) {
                // Setzt die Variable hotspotSet auf das Hotspot Set pro Bild für getWarningForHotSpot
                setHotspotSetPerImage(hotSpotSetDividedPerImage.get(key));
                List<String> hotSpotsWithoutPosNo = new ArrayList<>();
                if (getWarningsForHotSpot(null, hotSpotsWithoutPosNo)) {
                    Collections.sort(hotSpotsWithoutPosNo);
                    result.put(key, hotSpotsWithoutPosNo);
                }
            }
        }
        return result;
    }

    /**
     * Sucht die fehlerhaften POS
     *
     * @return
     */
    public Map<KEY_FAULTY_POS_MAP, List<String>> getFaultyPOS() {
        List<String> posNoWithoutHotSpots = new ArrayList<>();
        List<String> invalidPosNo = new ArrayList<>();
        Map<KEY_FAULTY_POS_MAP, List<String>> result = new HashMap<>();
        // Setzt die Variable hotspotSet auf die Hotspot Sets aller Bilder für getWarningsForPOS
        setHotspotSetFromMap();
        if (getWarningsForPOS(null, posNoWithoutHotSpots, invalidPosNo)) {

            // DAIMLER-15137
            if (!posNoWithoutHotSpots.isEmpty() && !isSkipHotspotWithoutImageCheck()) {
                Collections.sort(posNoWithoutHotSpots);
                result.put(KEY_FAULTY_POS_MAP.POSNO_WITHOUT_HOTSPOTS, posNoWithoutHotSpots);
            }

            if (!invalidPosNo.isEmpty()) {
                Collections.sort(invalidPosNo);
                result.put(KEY_FAULTY_POS_MAP.INVALID_POSNO, invalidPosNo);
            }

            if (posNumberSet.contains("") && !isSkipEmptyPosCheck()) {
                DwList<String> emptyPos = new DwList<>(new String[]{ "" });
                result.put(KEY_FAULTY_POS_MAP.EMPTY_POSNO, emptyPos);
            }
        }
        return result;
    }

    /**
     * Führt beide Methoden aus um warning zu füllen
     *
     * @param warning
     * @return
     */
    public boolean getFaultyHotspotsAndPOS(List<String> warning) {
        setHotspotSetFromMap();
        boolean result = getWarningsForHotSpot(warning, null);
        boolean posResult = getWarningsForPOS(warning, null, null);
        // Wenn result nach Hotspot Prüfung immer noch false, dann das Ergebnis von der POS Prüfung nehmen
        if (!result) {
            return posResult;
        }

        return result;
    }


    /**
     * Testet, ob es Hotspots gibt ohne zugehörige Teilepostion in der Stückliste
     *
     * @param warnings             Nachricht, die gezeigt werden soll
     * @param hotSpotsWithoutPosNo Fehlerhaften Hotspots
     * @return Nachricht
     */
    private boolean getWarningsForHotSpot(List<String> warnings, List<String> hotSpotsWithoutPosNo) {
        boolean result = false;

        Set<String> temp = new HashSet<>();
        for (String hotspot : hotspotSet) {
            if (!hotspot.isEmpty()) {
                if (!posNumberSet.contains(hotspot)) {
                    temp.add(hotspot);
                }
            }
        }

        if (!temp.isEmpty()) {
            result = true;
            if (warnings != null) {
                warnings.add(TranslationHandler.translate("!!Die Zeichnungen des Moduls enthalten Positionsnummern, die in der Stückliste nicht vorkommen."));
                warnings.add(buildNumberString(temp));
            }
        }

        if (hotSpotsWithoutPosNo != null) {
            hotSpotsWithoutPosNo.addAll(temp);
        }

        return result;
    }

    /**
     * Testet, ob es eine Teileposition gibt ohne dazugehörigem Hotspot
     * , ob es eine leere Teileposition gibt
     * , ob es eine ungültige Teilepostion gibt
     *
     * @param warnings             Nachricht, die gezeigt werden soll
     * @param posNoWithoutHotSpots Liste mit den Teilepositionen ohne Hotspots
     * @param invalidPosNo         Liste mit den ungültigen teilepositionen
     * @return Nachricht
     */
    public boolean getWarningsForPOS(List<String> warnings, List<String> posNoWithoutHotSpots, List<String> invalidPosNo) {
        boolean result = false;
        boolean hasEmptyPosNo = false;
        Set<String> posNoWithoutHotSpotsTemp = new HashSet<>();
        Set<String> invalidPosNoTemp = new HashSet<>();
        for (String posNo : posNumberSet) {
            if (!posNo.isEmpty()) {
                if (!dataConnector.isAlphaNumHotspotAllowed() && !StrUtils.isInteger(posNo)) {
                    invalidPosNoTemp.add(posNo);
                } else {
                    if (!hotspotSet.contains(posNo)) {
                        posNoWithoutHotSpotsTemp.add(posNo);
                    }
                }
            } else {
                hasEmptyPosNo = true;
            }
        }
        if (hasEmptyPosNo && !isSkipEmptyPosCheck()) {
            result = true;
            if (warnings != null) {
                warnings.add(TranslationHandler.translate("!!Die Stückliste enthält Einträge ohne Positionsnummer."));
            }
        }
        if (!posNoWithoutHotSpotsTemp.isEmpty()) {
            result = true;
            if (warnings != null) {
                warnings.add(TranslationHandler.translate("!!Die Stückliste enthält Positionsnummern, die in den Zeichnungen des Moduls nicht vorkommen."));
                warnings.add(buildNumberString(posNoWithoutHotSpotsTemp));
            }
        }

        if (!invalidPosNoTemp.isEmpty()) {
            result = true;
            if (warnings != null) {
                warnings.add(TranslationHandler.translate("!!Die Stückliste enthält ungültige Positionsnummern."));
                warnings.add(buildNumberString(invalidPosNoTemp));
            }
        }

        if (invalidPosNo != null) {
            invalidPosNo.addAll(invalidPosNoTemp);
        }

        if (posNoWithoutHotSpots != null) {
            posNoWithoutHotSpots.addAll(posNoWithoutHotSpotsTemp);
        }

        return result;
    }

    private String buildNumberString(Set<String> numberSet) {
        if (!numberSet.isEmpty()) {
            Comparator<String> numberComparator = new Comparator<String>() {
                @Override
                public int compare(String s1, String s2) {
                    // () und {} für den Vergleich entfernen, damit die Zahlen mit vorangestellten Nullen vergleichbar sind
                    s1 = StrUtils.prefixStringWithCharsUpToLength(s1.replace("(", "").replace(")", "").replace("{", "").replace("}", ""), '0', 20);
                    s2 = StrUtils.prefixStringWithCharsUpToLength(s2.replace("(", "").replace(")", "").replace("{", "").replace("}", ""), '0', 20);
                    return s1.compareTo(s2);
                }
            };
            TreeSet<String> sortedNumberSet = new TreeSet<String>(numberComparator);
            sortedNumberSet.addAll(numberSet);
            Iterator<String> numberIterator = sortedNumberSet.iterator();

            StringBuilder str = new StringBuilder();
            str.append("    (");
            str.append(numberIterator.next());
            int maxNum = Math.min(15, sortedNumberSet.size());
            for (int lfdNr = 1; lfdNr < maxNum; lfdNr++) {
                str.append(", ");
                str.append(numberIterator.next());
            }
            if (maxNum < sortedNumberSet.size()) {
                str.append(", ...");
            }
            str.append(")");
            return str.toString();
        }
        return "";
    }

    /**
     * Der Parameter wird in den Modulstammdaten gesetzt.
     * "Stückliste enthält Einträge ohne Positionsnummer" deaktivieren
     *
     * @return <code>true</code> wenn der Parameter gesetzt ist, und damit die Prüfung nicht laufen soll
     * <code>false</code> wenn der Parameter nicht gesetzt ist, und damit die Prüfung laufen soll (default)
     */
    private boolean isSkipEmptyPosCheck() {
        if (dataConnector != null) {
            EtkDataAssembly assembly = dataConnector.getCurrentAssembly();
            if (assembly instanceof iPartsDataAssembly) {
                return ((iPartsDataAssembly)assembly).getModuleMetaData().skipEmptyHotspotCheck();
            }
        }
        return false;
    }

    /**
     * Der Parameter wird in den Modulstammdaten gesetzt.
     * "Teileposition hat Hotspot ohne Bild" deaktivieren
     *
     * @return <code>true</code> wenn der Parameter gesetzt ist, und damit die Prüfung nicht laufen soll
     * <code>false</code> wenn der Parameter nicht gesetzt ist, und damit die Prüfung laufen soll (default)
     */
    private boolean isSkipHotspotWithoutImageCheck() {
        if (dataConnector != null) {
            EtkDataAssembly assembly = dataConnector.getCurrentAssembly();
            if (assembly instanceof iPartsDataAssembly) {
                return ((iPartsDataAssembly)assembly).getModuleMetaData().skipHotspotWithoutImageCheck();
            }
        }
        return false;
    }
}
