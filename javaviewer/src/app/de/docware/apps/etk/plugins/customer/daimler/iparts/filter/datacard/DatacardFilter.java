/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.filter.datacard;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsOmittedParts;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsColorTable;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.VarParam;
import de.docware.util.misc.booleanfunctionparser.BooleanFunction;
import de.docware.util.misc.booleanfunctionparser.BooleanFunctionSyntaxException;
import de.docware.util.misc.booleanfunctionparser.BooleanFunctionTermTreeAnalyzer;
import de.docware.util.misc.booleanfunctionparser.model.Conjunction;
import de.docware.util.misc.booleanfunctionparser.model.Disjunction;

import java.util.*;

/**
 * Eigene Unterklasse für Datenkarten spezifische Filter
 */
public class DatacardFilter {

    private static final double OMITTED_PART_SCORE = 99999000000d;

    /**
     * Erweiterter Code Filter
     * Zusätzlich zum "klassischen" Code Filter wird hier nochmal weiter mit einem Trefferscoring unterschieden
     * Es wird jeweils pro Positionsvariante und gleichem Hotspot die Coderegel ausgewertet. Dabei wird pro Teilkonjunktion
     * jeder positive Code mit 1 und jeder negative Code mit 0 gewichtet. Nur Teilkonjunktionen, die true ergeben, werden berücksichtigt.
     * Es werden pro Positionsvariante jeweils die höchsten Teilkonjuktions-scores vergleichen. Nur die Positionsvariante(n)
     * mit dem höchsten Score wird/werden ausgegeben.
     *
     * @param filterEntries
     * @param dataCardCodes
     * @param skipCodesForScoring
     * @param filter
     */
    public static void extendedCodeFilter(iPartsFilterPartsEntries filterEntries, Set<String> dataCardCodes, Set<String> skipCodesForScoring,
                                          iPartsFilter filter) {
        iPartsOmittedParts omittedParts = (filter != null) ? filter.getOmittedPartsCache(filterEntries.getEtkProject())
                                                           : iPartsOmittedParts.getInstance(filterEntries.getEtkProject());
        for (List<iPartsDataPartListEntry> positionsVariants : filterEntries.getAllPositionsVariants()) {
            // ermittle alle enthaltenen Hotspots in der Positionsvariante
            Set<String> allHotpots = new HashSet<>();
            for (iPartsDataPartListEntry posVariant : positionsVariants) {
                allHotpots.add(posVariant.getFieldValue(iPartsConst.FIELD_K_POS));
            }

            // führe die Filertung für jeden Hotpot getrennt durch
            for (String hotspot : allHotpots) {
                // prüfe ob leere Hotspots dabei sind, und nehme die vom scoring aus
                if (hotspot.isEmpty() || hotspot.startsWith("-")) {
                    extendedCodeFilterNoScoring(filterEntries, positionsVariants, dataCardCodes, filter);
                } else {
                    extendedCodeFilterScoring(filterEntries, positionsVariants, dataCardCodes, skipCodesForScoring, hotspot,
                                              omittedParts, filter);
                }
            }
        }
    }

    /**
     * Erw. Codefilter ohne Scoring für die PVs eines Hotspots
     *
     * @param filterEntries     hier werden die Entries markiert, die ausgefiltert werden
     * @param positionsVariants Diese Positionsvarianten sollen geprüft werden
     * @param dataCardCodes
     * @param filter
     */
    private static void extendedCodeFilterNoScoring(iPartsFilterPartsEntries filterEntries, Collection<iPartsDataPartListEntry> positionsVariants,
                                                    Set<String> dataCardCodes, iPartsFilter filter) {
        for (iPartsDataPartListEntry partListEntry : positionsVariants) {
            if (filterEntries.isEntryVisible(partListEntry)) {
                // standard Codefilter
                if (!iPartsFilterHelper.basicCheckCodeFilterForDatacard(partListEntry, dataCardCodes, true, filter)) {
                    filterEntries.hideEntry(partListEntry);
                }
            }
        }
    }

    /**
     * Erw. Codefilter mit Scoring für die PVs eines Hotspots
     *
     * @param filterEntries
     * @param positionsVariants   Positionsvarianten, nur die gültigen bleiben übrig
     * @param dataCardCodes
     * @param skipCodesForScoring
     * @param hotspot
     * @param omittedParts
     * @param filter
     */
    private static void extendedCodeFilterScoring(iPartsFilterPartsEntries filterEntries, List<iPartsDataPartListEntry> positionsVariants,
                                                  Set<String> dataCardCodes, Set<String> skipCodesForScoring, String hotspot,
                                                  iPartsOmittedParts omittedParts, iPartsFilter filter) {
        // man muss die Wegfall-Teile zuerst bestimmen (könnte sonst sein, dass zwei Teile nicht durch den Wegfall verdrängt werden,
        // das Teil mit dem höheren Score das andere Teil verdrängt; dann kommt ein weiteres Wegfall-Teil, welches nur das Teil mit dem höheren Score
        // verdrängt, so dass eigentlich das andere Teil wieder sichtbar gemacht werden müßte)
        Map<iPartsDataPartListEntry, Disjunction> omittedPartsListEntriesMap = getAllOmittedEntriesForHotspot(omittedParts,
                                                                                                              positionsVariants,
                                                                                                              filterEntries,
                                                                                                              hotspot, dataCardCodes,
                                                                                                              skipCodesForScoring);

        // Höchster Trefferscore über alle Positionsvarianten
        double positionVariantHighScore = 0;
        for (int i = positionsVariants.size() - 1; i >= 0; i--) {
            iPartsDataPartListEntry partListEntry = positionsVariants.get(i);
            if (filterEntries.isEntryVisible(partListEntry)) {
                // Nur weiter machen wenn der aktuelle Hotspot auch geprüft werden soll
                String currentHotspot = partListEntry.getFieldValue(iPartsConst.FIELD_K_POS);
                if (currentHotspot.equals(hotspot)) {
                    String codes = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.RETAIL_CODES_REDUCED);
                    double positionVariantScore;
                    // Falls das aktuelle Teil eine Wegfallsachnummer hat, steht es beim Scoring schonmal fast an der Spitze
                    if (omittedPartsListEntriesMap.containsKey(partListEntry)) {
                        positionVariantScore = OMITTED_PART_SCORE;
                    } else {
                        // für false wird -1 zurück gegeben, bei true der score
                        positionVariantScore = scoreConjunctions(codes, dataCardCodes, skipCodesForScoring, 1, 0);
                        if (!omittedPartsListEntriesMap.isEmpty()) {
                            // Prüfen, ob der aktuelle Stücklisteneintrag überhaupt vom Wegfall-Teil verdrängt wird
                            if (!entryHiddenByOmittedParts(omittedPartsListEntriesMap, codes, dataCardCodes, skipCodesForScoring)) {
                                // Alle Einträge, die trotz Wegfalls nicht verdrängt werden, haben einen Score > OMITTED_PART_SCORE
                                positionVariantScore += OMITTED_PART_SCORE;
                                filterEntries.setHigherScoringThanOmittedPart(partListEntry, true);
                            }
                        }
                    }
                    if (positionVariantScore > -1) {
                        // die Positionsvariante mit dem höchsten score soll ausgegeben werden
                        if (positionVariantScore > positionVariantHighScore) {
                            // alle alten einträge löschen (einträge in der Liste nach dem aktuellen)
                            for (int j = i + 1; j < positionsVariants.size(); j++) {
                                iPartsDataPartListEntry partListEntry2 = positionsVariants.get(j);
                                if (filterEntries.isEntryVisible(partListEntry2)) {
                                    // nur die Einträge mit gleichem Hotpsot löschen
                                    if (partListEntry2.getFieldValue(iPartsConst.FIELD_K_POS).equals(currentHotspot)) {
                                        if ((filter != null) && filter.isWithFilterReason()) {
                                            String positionVariantScoreString;
                                            if (omittedPartsListEntriesMap.containsKey(partListEntry2)) {
                                                continue;
                                            } else {
                                                positionVariantScoreString = getDisplayStringForHighScore(positionVariantScore, positionVariantScore);
                                            }
                                            String displayHighScore = getDisplayStringForHighScore(positionVariantHighScore, positionVariantHighScore);
                                            filter.setFilterReasonForPartListEntry(partListEntry2, iPartsFilterSwitchboard.FilterTypes.EXTENDED_CODE,
                                                                                   "!!Scoring für Code (<= %1) < neues Maximum %2 für gleiche DIALOG-Position",
                                                                                   displayHighScore, positionVariantScoreString);
                                        }
                                        filterEntries.hideEntry(partListEntry2);
                                    }
                                }
                            }
                            positionVariantHighScore = positionVariantScore;
                        } else if (positionVariantScore < positionVariantHighScore) {
                            // wenn der score für diese positionsvariante kleiner als der Highscore ist, dann lösche sie
                            if ((filter != null) && filter.isWithFilterReason()) {
                                String positionVariantHighScoreString = getDisplayStringForHighScore(positionVariantScore, positionVariantHighScore);
                                String displayScore = getDisplayStringForScore(positionVariantScore);
                                filter.setFilterReasonForPartListEntry(partListEntry, iPartsFilterSwitchboard.FilterTypes.EXTENDED_CODE,
                                                                       "!!Scoring für Code %1 < bisheriges Maximum %2 für gleiche DIALOG-Position",
                                                                       displayScore, positionVariantHighScoreString);
                            }
                            filterEntries.hideEntry(partListEntry);
                        }
                    } else {
                        // der gesamte code ist false, also auch löschen
                        if ((filter != null) && filter.isWithFilterReason()) {
                            filter.setFilterReasonForPartListEntry(partListEntry, iPartsFilterSwitchboard.FilterTypes.EXTENDED_CODE,
                                                                   "!!Code nicht gültig für die Code %1",
                                                                   iPartsFilterHelper.getFilterReasonSourceName(false));
                        }
                        filterEntries.hideEntry(partListEntry);
                    }
                }
            }
        }
    }

    private static String getDisplayStringForScore(double positionVariantScore) {
        return getDisplayStringForHighScore(Double.MAX_VALUE, positionVariantScore);
    }

    private static String getDisplayStringForHighScore(double positionVariantScore, double positionVariantHighScore) {
        if (positionVariantScore <= OMITTED_PART_SCORE) {
            if (positionVariantHighScore >= OMITTED_PART_SCORE) {
                return TranslationHandler.translate("!!(Entfallteil)");  // für dieses Teil zählt der Wegfall
            }
        } else if (positionVariantHighScore >= OMITTED_PART_SCORE) { // nicht vom Wegfall betroffen, normalen Score zur Anzeige
            return String.valueOf(positionVariantHighScore - OMITTED_PART_SCORE);
        }
        return String.valueOf(positionVariantHighScore);
    }

    /**
     * Prüfen, ob die Teileposition durch die Wegfall-SNRs verdrängt wird.
     *
     * @param omittedPartListEntriesMap
     * @param codesForPartListEntry
     * @param dataCardCodes
     * @param skipCodesForScoring
     * @return
     */
    private static boolean entryHiddenByOmittedParts(Map<iPartsDataPartListEntry, Disjunction> omittedPartListEntriesMap,
                                                     String codesForPartListEntry, Set<String> dataCardCodes, Set<String> skipCodesForScoring) {
        // Anforderung Gerd Müller:
        // Falls eine Teileposition die komplette Teilkonjunktion der Wegfall-SNR mit zusätzlichen Codes enthält,
        // dann soll die Teileposition nicht verdrängt werden.

        // Algorithmus:
        // Der Stücklisteneintrag mit den codes wird nur dann nicht von den Wegfall-Teilen verdrängt, wenn pro
        // Wegfall-Teil alle dort (entsprechend der Datenkarte) gültigen Teilkonjunktionen komplett in allen gültigen
        // Teilkonjunktionen der codes vorhanden sind und dabei dort jeweils noch zusätzliche Codes vorkommen.

        Disjunction checkEntryDisjunction;
        try {
            checkEntryDisjunction = DaimlerCodes.getDnfCodeOriginal(codesForPartListEntry);
        } catch (BooleanFunctionSyntaxException e) {
            RuntimeException runtimeException = new RuntimeException("Error in code \"" + codesForPartListEntry + "\": "
                                                                     + e.getMessage(), e);
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_CODES, LogType.ERROR, runtimeException);
            return false;
        }

        if (checkEntryDisjunction != null) {
            // erstmal alle gültigen Teilkonjunktionen des zu prüfenden Stücklisteneintrags sammeln
            Set<Conjunction> validCheckConjunctions = new HashSet();
            for (Conjunction checkConjunction : checkEntryDisjunction) {
                boolean validCheckConjunction = iPartsFilterHelper.basicCheckCodeFilter(checkConjunction, dataCardCodes,
                                                                                        skipCodesForScoring, null, null, null);
                if (validCheckConjunction) {
                    validCheckConjunctions.add(checkConjunction);
                }
            }
            if (validCheckConjunctions.isEmpty() && !checkEntryDisjunction.isEmpty()) {
                return true; // Stücklisteneintrag laut Datenkarte ungültig
            }

            checkEntryDisjunction = new Disjunction(validCheckConjunctions);

            for (Disjunction omittedPLEDisjunction : omittedPartListEntriesMap.values()) {
                try {
                    if (!checkEntryDisjunction.isAllPartialConjunctionTermsIncluded(omittedPLEDisjunction, true)) {
                        return true;
                    }
                } catch (BooleanFunctionSyntaxException e) {
                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_CODES, LogType.ERROR, e);
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * Liefert eine Map mit allen Stücklisteneinträgen, die eine Wegfall-Teilenummer beinhalten, auf die dazugehörige {@link Disjunction}
     * mit allen gültigen Teilkonjunktionen zurück für den gewünschten Hotspot und Positionsvarianten.
     *
     * @param omittedParts
     * @param positionsVariants
     * @param filterEntries
     * @param hotspot
     * @return
     */
    private static Map<iPartsDataPartListEntry, Disjunction> getAllOmittedEntriesForHotspot(iPartsOmittedParts omittedParts,
                                                                                            List<iPartsDataPartListEntry> positionsVariants,
                                                                                            iPartsFilterPartsEntries filterEntries,
                                                                                            String hotspot, Set<String> dataCardCodes,
                                                                                            Set<String> skipCodesForScoring) {
        Map<iPartsDataPartListEntry, Disjunction> omittedPartListEntries = new HashMap<>();

        for (iPartsDataPartListEntry partListEntry : positionsVariants) {
            if (filterEntries.isEntryVisible(partListEntry)) {
                // Nur weiter machen wenn der aktuelle Hotspot auch geprüft werden soll
                String currentHotspot = partListEntry.getFieldValue(iPartsConst.FIELD_K_POS);
                if (currentHotspot.equals(hotspot)) {
                    if (omittedParts.isOmittedPart(partListEntry)) {
                        Disjunction omittDisjunction;
                        String omittCode = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.RETAIL_CODES_REDUCED);
                        try {
                            omittDisjunction = DaimlerCodes.getDnfCodeOriginal(omittCode);

                            // Prüfen, welche Teilkonjunktionen der Wegfall-Einträge überhaupt mit der Datenkarte matchen
                            Set<Conjunction> validConjunctions = new HashSet<>();
                            for (Conjunction omittConjunction : omittDisjunction) {
                                boolean validOmittConjunction = iPartsFilterHelper.basicCheckCodeFilter(omittConjunction,
                                                                                                        dataCardCodes,
                                                                                                        skipCodesForScoring,
                                                                                                        null, null, null);
                                if (validOmittConjunction) {
                                    validConjunctions.add(omittConjunction);
                                }
                            }

                            omittDisjunction = new Disjunction(validConjunctions);
                        } catch (BooleanFunctionSyntaxException e) {
                            RuntimeException runtimeException = new RuntimeException("Error in code \"" + omittCode + "\": "
                                                                                     + e.getMessage(), e);
                            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_CODES, LogType.ERROR, runtimeException);
                            omittDisjunction = new Disjunction();
                        }

                        // Ist die Wegfallposition laut Datenkarte gültig?
                        if (!omittDisjunction.isEmpty() || DaimlerCodes.isEmptyCodeString(omittCode)) {
                            omittedPartListEntries.put(partListEntry, omittDisjunction);
                        }
                    }
                }
            }
        }

        return omittedPartListEntries;
    }

    /**
     * Erw. Farbfilter mit Scoring.
     *
     * @param colorTableContents
     * @param dataCardCodes
     * @param skipCodesForScoring
     * @param filter
     * @param project
     */
    public static void extendedCodeFilterScoringForColor(List<iPartsColorTable.ColorTableContent> colorTableContents,
                                                         Set<String> dataCardCodes, Set<String> skipCodesForScoring,
                                                         iPartsFilter filter, EtkProject project) {
        // Höchster Trefferscore über alle Farbinhalte
        float positionVariantHighScore = 0;
        String positionVariantHighScoreString = String.valueOf(positionVariantHighScore);
        for (int i = colorTableContents.size() - 1; i >= 0; i--) {
            String codes = colorTableContents.get(i).code;
            // für false wird -1 zurück gegeben, bei true der score
            float positionVariantScore = scoreConjunctions(codes, dataCardCodes, skipCodesForScoring, 1, 0.5f);
            if (positionVariantScore > -1) {
                String positionVariantScoreString = String.valueOf(positionVariantScore);

                // der Farbinhalt mit dem höchsten score soll ausgegeben werden
                if (positionVariantScore > positionVariantHighScore) {
                    positionVariantHighScore = positionVariantScore;
                    positionVariantHighScoreString = String.valueOf(positionVariantHighScore);
                    // alle alten einträge löschen (alle Einträge in der Liste nach dem aktuellen)
                    for (int j = i + 1; j < colorTableContents.size(); j++) {
                        iPartsColorTable.ColorTableContent removedColorTableContent = colorTableContents.remove(j);
                        if ((removedColorTableContent != null) && filter.isWithFilterReason()) {
                            filter.setFilterReasonForColorTableContent(removedColorTableContent.getDataColorTableContent(false, project),
                                                                       iPartsColorFilter.ColorTableContentFilterTypes.EXTENDED_COLOR,
                                                                       "!!Scoring für Code (<= %1) < neues Maximum %2 für gleiche Variantentabelle",
                                                                       positionVariantHighScoreString, positionVariantScoreString);
                        }
                    }
                } else if (positionVariantScore < positionVariantHighScore) {
                    // wenn der Score für diesen Farbinhalt kleiner als der Highscore ist, dann lösche ihn
                    iPartsColorTable.ColorTableContent removedColorTableContent = colorTableContents.remove(i);
                    if ((removedColorTableContent != null) && filter.isWithFilterReason()) {
                        filter.setFilterReasonForColorTableContent(removedColorTableContent.getDataColorTableContent(false, project),
                                                                   iPartsColorFilter.ColorTableContentFilterTypes.EXTENDED_COLOR,
                                                                   "!!Scoring für Code %1 < bisheriges Maximum %2 für gleiche DIALOG-Position",
                                                                   positionVariantScoreString, positionVariantHighScoreString);
                    }
                }
            } else {
                // der gesamte Code ist false, also auch löschen
                iPartsColorTable.ColorTableContent removedColorTableContent = colorTableContents.remove(i);
                if ((removedColorTableContent != null) && filter.isWithFilterReason()) {
                    filter.setFilterReasonForColorTableContent(removedColorTableContent.getDataColorTableContent(false, project),
                                                               iPartsColorFilter.ColorTableContentFilterTypes.EXTENDED_COLOR,
                                                               "!!Code nicht gültig für die Code %1",
                                                               iPartsFilterHelper.getFilterReasonSourceName(false));
                }
            }
        }
    }

    /**
     * Berechnet das Maximum vom Score pro Teilkonjunktion. Dabei werden die positiven und negativen Matches gezählt und
     * anschließend mit Gewichtung addiert. Der Score wird nur bei Teilkonjunktionen die true ergeben ausgewertet.
     * Ist die Teilkonjunktion leer oder ";" wird emptyWeight zurück geliefert
     *
     * @param codes          der Codestring der ausgewertet werden soll
     * @param dataCardCodes  die Positiv Codes von der Datenkarte
     * @param positiveWeight Gewicht für positive Matches
     * @param emptyWeight    Gewicht für leere Code
     * @return -1 falls keine Teilkonjunktion true ist, sonst der gewichtete Score
     */
    public static float scoreConjunctions(String codes, Set<String> dataCardCodes, Set<String> skipCodesForScoring, float positiveWeight,
                                          float emptyWeight) {
        try {
            float positionVariantScore = -1;
            codes = codes.trim(); // damit leere Strings mit Leerzeichen wie leer behandelt werden
            BooleanFunction parser = DaimlerCodes.getFunctionParser(codes);
            // DNF erzeugen und Teilkonjuktionen extrahieren
            // Klonen der DNF findet in basicCheckCodeFilter() statt
            Disjunction disjunction = DaimlerCodes.getDnfCodeOriginal(codes);
            VarParam<Integer> positiveCounter = new VarParam<>(0);
            // Falls der Code keine Teilkonjunktionen hat, den gesamten Code auswerten (z.B. bei ";")
            if (parser.isEmpty() || disjunction.isEmpty()) {
                if (parser.isEmpty()) {
                    // wenn der Code " " oder ";" ist soll nochmal anders gewichtet werden
                    return emptyWeight;
                } else {
                    // Wenn der Code sich nicht weiter zerlegen lässt, aber nicht nur leer ist, dann soll er als ganzes ausgewertet werden
                    // Dieser Fall kommt eigentlich nur vor, wenn etwas bei der DNF-Bildung nicht funktioniert und sollte eigentlich nie vorkommen
                    // Es wird deshalb eine die originale Funktion genommen und mit dieser weitergearbeitet
                    disjunction = new BooleanFunctionTermTreeAnalyzer(parser).getTerms();
                }
            }
            for (Conjunction conjunction : disjunction) {
                boolean eval = true;

                // Damit die 'RemovedTerms', also die wegoptimierten Terme beim Scoring berücksichtigt werden können,
                // müssen wir hier eine temporäre Disjunction mit nur einem Element machen und an diese Disjunction die
                // wegoptimierten Codes dranhängen.
                // Es wird so getan, dass in jeder Conjunction auch die wegoptimierten gültig waren.
                Disjunction tempDisjunction = new Disjunction(conjunction);
                tempDisjunction.addToRemovedTerms(disjunction.getRemovedTerms());
                if (!conjunction.isEmpty()) {
                    // setze die code von der Datenkarte als true in jede Teilkonjuktion ein (alle anderen sind false)
                    eval = iPartsFilterHelper.basicCheckCodeFilter(tempDisjunction, dataCardCodes, skipCodesForScoring, null,
                                                                   null, positiveCounter);
                }
                if (eval) {
                    // Falls die Teilkonjunktion true ist, den Score berechnen und das Maximum aller bisherigen Scores merken
                    positionVariantScore = Math.max(positionVariantScore, positiveCounter.getValue() * positiveWeight);
                }
            }
            return positionVariantScore;
        } catch (BooleanFunctionSyntaxException e) {
            RuntimeException runtimeException = new RuntimeException("Error in code \"" + codes + "\": " + e.getMessage(), e);
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_CODES, LogType.ERROR, runtimeException);
            return 0;
        }

    }

    public static float scoreConjunctions(String code, Set<String> datacardCodes, float posWeight, float emptyWeight) {
        return scoreConjunctions(code, datacardCodes, null, posWeight, emptyWeight);
    }

    /**
     * Implementierung des Wegfallsachnummern-Filters im Datenkarten-Filter
     * Hier werden alle Positionsvarianten ausgefiltert (im Gegensatz zur Variante aus dem Baumuster-Filter)
     * {@link iPartsFilterHelper#basicCheckOmittedPartsInModelFilter(EtkDataPartListEntry, Set, iPartsDocumentationType, iPartsOmittedParts, iPartsFilter)}
     * Ausgefiltert wird, wenn der Dokumentationstyp DIALOG_IPARTS ist und es technische Code gibt.
     *
     * @param filterEntries
     * @param allModelBuildingCodes Alle relevanten baumusterbildenden Code um zu identifizieren, ob das Teil technische Code hat
     * @param filter
     */
    public static void omittedPartsInDatacardFilter(iPartsFilterPartsEntries filterEntries,
                                                    Set<String> allModelBuildingCodes, iPartsFilter filter) {
        EtkProject project = filterEntries.getEtkProject();
        iPartsOmittedParts omittedParts = (filter != null) ? filter.getOmittedPartsCache(project) : iPartsOmittedParts.getInstance(project);
        for (List<iPartsDataPartListEntry> positionVariants : filterEntries.getAllPositionsVariants()) {
            for (iPartsDataPartListEntry partListEntry : positionVariants) {
                if (filterEntries.isEntryVisible(partListEntry) && omittedParts.isOmittedPart(partListEntry)) {
                    Set<String> technicalCodes = DaimlerCodes.getCodeSet(partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.RETAIL_CODES_REDUCED));
                    technicalCodes.removeAll(allModelBuildingCodes);

                    if (!technicalCodes.isEmpty()) {
                        // Für das Entfallteil den Filtergrund setzen (ausgefiltert wird es unten in der Schleife)
                        String partNumberFormatted = null;
                        if ((filter != null) && filter.isWithFilterReason()) {
                            partNumberFormatted = iPartsNumberHelper.formatPartNo(project, partListEntry.getPart().getAsId().getMatNr());
                            filter.setFilterReasonForPartListEntry(partListEntry, iPartsFilterSwitchboard.FilterTypes.OMITTED_PARTS,
                                                                   "!!Entfallteil \"%1\" im %2 entfernt (Dokumentationstyp %3 und technische Code vorhanden)",
                                                                   partNumberFormatted, iPartsFilterSwitchboard.FilterTypes.DATACARD_CODE.getDescription(project),
                                                                   iPartsDocumentationType.DIALOG_IPARTS.getExportValue());
                        }

                        // Alle Positionsvarianten entfallen
                        for (iPartsDataPartListEntry entryToHide : positionVariants) {
                            if (!filterEntries.isHigherScoringThanOmittedPart(entryToHide)) {
                                if ((filter != null) && filter.isWithFilterReason() && (entryToHide != partListEntry)) {
                                    // An allen anderen Positionsvarianten den Filtergrund setzen (überschreibt den zuvor gesetzten Grund nicht)
                                    filter.setFilterReasonForPartListEntry(entryToHide, iPartsFilterSwitchboard.FilterTypes.OMITTED_PARTS,
                                                                           "!!Entfallteil \"%1\" auf gleicher DIALOG-Position: Teil im %2 entfernt (Dokumentationstyp %3 und technische Code vorhanden)",
                                                                           partNumberFormatted, iPartsFilterSwitchboard.FilterTypes.DATACARD_CODE.getDescription(project),
                                                                           iPartsDocumentationType.DIALOG_IPARTS.getExportValue());
                                }
                                filterEntries.hideEntry(entryToHide);
                            }
                        }

                        break;
                    }
                }
            }
        }
    }
}