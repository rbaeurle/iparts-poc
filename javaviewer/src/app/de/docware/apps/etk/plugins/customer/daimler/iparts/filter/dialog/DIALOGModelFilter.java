/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.filter.dialog;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsOmittedParts;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterPartsEntries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterSwitchboard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.timeslice.iPartsFilterTimeSliceHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.timeslice.iPartsPositionVariantsTimeLine;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.SteeringIdentKeys;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.VarParam;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.booleanfunctionparser.BooleanFunction;
import de.docware.util.misc.booleanfunctionparser.BooleanFunctionSyntaxException;
import de.docware.util.misc.booleanfunctionparser.model.Conjunction;
import de.docware.util.misc.booleanfunctionparser.model.Disjunction;
import de.docware.util.misc.booleanfunctionparser.model.PositiveAndNegativeTerms;

import java.util.*;

/**
 * Kapselt die Funktionalität zur Filterung von Stücklisten aus DIALOG über Baumusterfilter (AA und BM-bildende Codes).
 * Separate Klasse, damit besser testbar.
 */
public class DIALOGModelFilter {

    private static final String HAS_ONLY_BM_CODES = "HAS_ONLY_BM_CODES";

    private String modelNo;
    private String ausfuehrungsArt;
    private boolean filterWithAAModelBuildingCode;
    private Set<String> positiveModelBuildingCodeSet;
    private Set<String> negativeModelBuildingCodeSet;
    private Set<String> modelBuildingCodeSet;

    public DIALOGModelFilter(String modelNo, boolean filterWithAAModelBuildingCode) {
        this.modelNo = modelNo;
        this.filterWithAAModelBuildingCode = filterWithAAModelBuildingCode;
    }

    /**
     * Ausführungsart und baumusterbildende Codes für das im BM-Filter eingestellte BM finden sowie negative baumusterbildende
     * Codes für alle anderen Baumuster der dazugehörige Baureihe.
     *
     * @param project
     */
    public void createModelBuildingCodeSets(EtkProject project) {
        iPartsModel model = iPartsModel.getInstance(project, new iPartsModelId(modelNo));
        if (!model.existsInDB()) { // Keine Filterung möglich
            ausfuehrungsArt = "";
            negativeModelBuildingCodeSet = new HashSet<>();
            positiveModelBuildingCodeSet = new HashSet<>();
            modelBuildingCodeSet = new HashSet<>();
            return;
        }

        // alle Codes aus dem aktuellen BM sind die Positiv-Codes
        ausfuehrungsArt = model.getAusfuehrungsArt();
        positiveModelBuildingCodeSet = new HashSet<>(model.getPositiveModelBuildingCodeSet(filterWithAAModelBuildingCode, project));
        negativeModelBuildingCodeSet = new HashSet<>(model.getNegativeModelBuildingCodeSet(filterWithAAModelBuildingCode, project));

        // alle positiven Codes von den negativen entfernen
        negativeModelBuildingCodeSet.removeAll(positiveModelBuildingCodeSet);

        // Alle baumusterbildenden Codes bestimmen
        modelBuildingCodeSet = new HashSet<>();
        modelBuildingCodeSet.addAll(positiveModelBuildingCodeSet);
        modelBuildingCodeSet.addAll(negativeModelBuildingCodeSet);
    }

    public String getAA() {
        return ausfuehrungsArt;
    }

    public boolean isFilterWithAAModelBuildingCode() {
        return filterWithAAModelBuildingCode;
    }

    public Set<String> getPositiveModelBuildingCodeSet() {
        return positiveModelBuildingCodeSet;
    }

    public Set<String> getNegativeModelBuildingCodeSet() {
        return negativeModelBuildingCodeSet;
    }

    public Set<String> getModelBuildingCodeSet() {
        return modelBuildingCodeSet;
    }

    public boolean isModelBuildingCode(String code) {
        return positiveModelBuildingCodeSet.contains(code) || negativeModelBuildingCodeSet.contains(code);
    }

    /**
     * Stücklisteneintrag nach Coderegel filtern. Dazu alle Positionsvarianten untersuchen, welche der Varianten am
     * besten passt (technische Code Gruppierung + Gewichtung von Codes)
     * siehe auch: Baumuster-Filter für DIALOG-MAD-Kataloge
     * https://confluence.docware.de/confluence/x/KgFGAQ
     *
     * @param filterEntries
     * @param modelBuildingCodeSetFromDataCards Alle tatsächlichen baumusterbildenden Code von allen relevanten Datenkarten
     *                                          für das Baumuster
     * @param buildDate                         Zu diesem Datum ungültige rausfiltern. Für Alle -1
     * @param filter
     * @return true wenn Stücklisteneintrag zur Coderegel passt und dies die beste Positionsvariante ist
     */

    public void filterByCodeRule(iPartsFilterPartsEntries filterEntries, Set<String> modelBuildingCodeSetFromDataCards,
                                 long buildDate, iPartsFilter filter) {
        // Zeitscheiben über alle PVs getrennt nach DIALOG-Positionen. Jede Zeitscheibe enthält die jeweiligen PVs.
        Collection<iPartsPositionVariantsTimeLine> positionVariantsInTimeLine = new DwList<>();
        for (List<iPartsDataPartListEntry> pvPartListEntries : filterEntries.getAllPositionsVariants()) {
            positionVariantsInTimeLine.addAll(iPartsFilterTimeSliceHelper.calcTimeSliceMapFromPVList(pvPartListEntries, filterEntries,
                                                                                                     iPartsFilterHelper.ignoreInvalidFactories(filter)));
        }
        if (positionVariantsInTimeLine.isEmpty()) {
            // Die Zeitscheiben der Positionsvarianten können nicht ermittelt werden, wahrscheinlich kein BCTE-Schlüssel
            // In diesem Fall ganz einfach den Code filtern und raus
            for (iPartsDataPartListEntry partListEntry : filterEntries.getVisibleEntries()) {
                // Für die Code-Prüfung nur die tatsächlichen baumusterbildenden Code von allen relevanten Datenkarten für
                // das Baumuster verwenden
                if (!iPartsFilterHelper.basicCheckCodeFilter(partListEntry.getFieldValue(iPartsConst.FIELD_K_CODES),
                                                             modelBuildingCodeSetFromDataCards, null, modelBuildingCodeSet,
                                                             null)) {
                    if ((filter != null) && filter.isWithFilterReason()) {
                        filter.setFilterReasonForPartListEntry(partListEntry, iPartsFilterSwitchboard.FilterTypes.MODEL,
                                                               "!!Code nicht gültig für die Code %1",
                                                               iPartsFilterHelper.getFilterReasonSourceName(true));
                    }
                    filterEntries.hideEntry(partListEntry);
                }
            }
        } else {
            // Hole alle gültigen Positionsvarianten für alle Zeitscheiben mit denen die eigentliche Teileposition verglichen wird
            Set<EtkDataPartListEntry> validPositionVariants = calculatePositionVariantsToCompare(filterEntries.getEtkProject(),
                                                                                                 modelBuildingCodeSetFromDataCards,
                                                                                                 positionVariantsInTimeLine,
                                                                                                 buildDate, filter);
            if (iPartsFilterHelper.ignoreInvalidFactories(filter)) {
                // PV mit nur ungültigen Werksdaten sollen nicht ausgefiltert werden
                for (iPartsDataPartListEntry partListEntry : filterEntries.getVisibleEntries()) {
                    if ((partListEntry.getFactoryDataForRetail() != null) && (partListEntry.getFactoryDataForRetail().getFactoryDataMap() == null)) {
                        validPositionVariants.add(partListEntry);
                    }
                }
            }

            // Nur die validen PVs sollen angezeigt werden -> Verstecke alle nicht validen Stücklisteneinträge
            // Der Aufruf von iPartsFilter.setFilterReasonForPartListEntry() innerhalb von setVisibleEntries() sollte eigentlich
            // unnötig sein, da innerhalb von calculatePositionVariantsToCompare() bereits die Filtergründe gesetzt worden sein
            // müssten. -> Hier nur nochmal zur Sicherheit mit einem generischen Filtergrund
            filterEntries.setVisibleEntries(validPositionVariants, filter, iPartsFilterSwitchboard.FilterTypes.MODEL,
                                            "!!Ungültige bzw. verdrängte DIALOG-Positionsvariante bzgl. der Code %1",
                                            iPartsFilterHelper.getFilterReasonSourceName(true));
        }
    }

    /**
     * Überprüft, ob mindestens eine Positionsvariante der übergebenen Zeitscheibe bm-bildenden Code enthält.
     *
     * @param pvTimeLine
     * @param modelBuildingCodes
     * @return
     */

    private boolean hasBMCodes(iPartsPositionVariantsTimeLine pvTimeLine, Set<String> modelBuildingCodes) {
        for (EtkDataPartListEntry pvPartListEntry : pvTimeLine.getPositionVariants()) {
            String codeRule = pvPartListEntry.getFieldValue(iPartsConst.FIELD_K_CODES);
            // hat die aktuelle PV bm-bildende Code
            BooleanFunction pvFunctionParser = DaimlerCodes.getFunctionParser(codeRule);
            for (String pvCode : pvFunctionParser.getVariableNames()) {
                if (modelBuildingCodes.contains(pvCode)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Berechnet die Baumuster-gültigen Positionsvarianten für die übergebenen Zeitscheiben. Pro Zeitscheibe werden die
     * gütligen Positionsvarianten berechnet und in einer Liste gespeichert. Zum Schluss wird die Liste mit den
     * gültigen PVs (für alle Zeitscheiben) zurückgegeben.
     *
     * @param project
     * @param modelBuildingCodeSetFromDataCards Alle tatsächlichen baumusterbildenden Code von allen relevanten Datenkarten
     *                                          für das Baumuster
     * @param positionVariantsInTimeLine
     * @param buildDate
     * @param filter
     * @return
     */
    private Set<EtkDataPartListEntry> calculatePositionVariantsToCompare(EtkProject project, Set<String> modelBuildingCodeSetFromDataCards,
                                                                         Collection<iPartsPositionVariantsTimeLine> positionVariantsInTimeLine,
                                                                         long buildDate, iPartsFilter filter) {
        iPartsOmittedParts omittedParts = (filter != null) ? filter.getOmittedPartsCache(project) : iPartsOmittedParts.getInstance(project);

        // Es sollen nur die "echten" bm-bildenden Code verwendet werden (keine Beimischung von AA - DAIMLER-6841)
        iPartsModel model = iPartsModel.getInstance(project, new iPartsModelId(modelNo));
        Set<String> modelBuildingCodes = model.getCodeSetWithoutAA();
        int maxModelBuildingCodes = modelBuildingCodes.size();

        Set<EtkDataPartListEntry> overallResult = new LinkedHashSet<>();

        // Über alle gültigen Zeitscheiben iterieren. In jeder dieser Zeitscheiben sind andere Positionsvarianten gültig.
        // Falls das Teil in einer Zeitscheibe gültig ist, dann ist das Teil auch sichtbar
        for (iPartsPositionVariantsTimeLine pvTimeLine : positionVariantsInTimeLine) {
            // Falls auf ein bestimmtes Datum gefiltert wird (Datenkarte), dann teste die Zeitscheibe, ob das Datum darin liegt
            if (buildDate >= 0) {
                if (!pvTimeLine.isValidDate(buildDate)) {
                    // Falsche Zeit, nächste Zeitscheibe
                    if ((filter != null) && filter.isWithFilterReason() && !pvTimeLine.getPositionVariants().isEmpty()) {
                        for (EtkDataPartListEntry partListEntry : pvTimeLine.getPositionVariants()) {
                            filter.setFilterReasonForPartListEntry(partListEntry, iPartsFilterSwitchboard.FilterTypes.MODEL,
                                                                   "!!Zeitscheibe der DIALOG-Positionsvarianten \"%1\" bis \"%2\" enthält nicht den Termin \"%3\" %4",
                                                                   iPartsFilterHelper.getFilterReasonDate(pvTimeLine.getFromDate(), project),
                                                                   iPartsFilterHelper.getFilterReasonDate(pvTimeLine.getToDate(), project),
                                                                   iPartsFilterHelper.getFilterReasonDate(buildDate, project),
                                                                   iPartsFilterHelper.getFilterReasonSourceName(true));
                        }
                    }
                    continue;
                }
            }

            List<EtkDataPartListEntry> validPositionvariantsForTimeline = new DwList<>(); // Liste mit gültigen Positionsvarianten für diese Zeitscheibe

            // Falls keine Positionsvariante bm-bildende Code hat, dann sind alle Teilepositionen im Zeitintervall gültig.
            // Teilepositionen in die Ergebnisliste legen und mit der nächsten Zeitscheibe weitermachen
            if (!hasBMCodes(pvTimeLine, modelBuildingCodeSet)) {
                overallResult.addAll(pvTimeLine.getPositionVariants());
                continue;
            } else {
                validPositionvariantsForTimeline.addAll(pvTimeLine.getPositionVariants());
            }

            // Berechne die Gruppierungen der technischen Code.
            Map<String, Set<PartialConjunctionWithPart>> technicalCodeSets = calculateTechnicalCodeSets(validPositionvariantsForTimeline,
                                                                                                        modelBuildingCodeSetFromDataCards,
                                                                                                        filter, maxModelBuildingCodes);

            // Lösche alle bisher gültigen PVs für diese Zeitscheibe, weil die Gültigkeit im nachfolgenden Abschnitt via
            // technische Code-Gruppierung und Scoring neu berechnet wird.
            validPositionvariantsForTimeline.clear();
            for (Set<PartialConjunctionWithPart> technicalCodeSet : technicalCodeSets.values()) {
                if (technicalCodeSet.isEmpty()) {
                    continue;
                }
                // Falls in einer Gruppierung nur eine Teileposition liegt, braucht man die bm-bildende Codetreffer
                // nicht zählen (in dieser Gruppe kann nur diese Position gültig sein)
                if (technicalCodeSet.size() > 1) {
                    VarParam<Integer> codeMatches = new VarParam<>(0);
                    // Untersuche alle Positionsvarianten der technischen Codegruppen.
                    // Zähle die bm-bildenden Code in den Teilkonjunktionen, die für die Gruppeneinteilung verantwortlich sind.
                    // Die Positionsvariante, bei der die zu untersuchende Teilkonjunktion die meisten Treffer hat, wird
                    // als gültige PV zurückgegeben.
                    // Bei gleichen Treffern werden alle Positionsvarianten zurückgeliefert.
                    // Confluence: Codetreffer-Filter in https://confluence.docware.de/confluence/x/KgFGAQ
                    // Seit DAIMLER-6709 muss das Scoring für Links- und Rechtslenker getrennt betrachtet werden
                    int bestScoreForSteeringLeft = 0;
                    int bestScoreForSteeringRight = 0;
                    // Set für die validen Stücklistenpositionen einer technischen Gruppe.
                    Set<PartialConjunctionWithPart> resultForTechnicalGroupForSteeringLeft = new LinkedHashSet<>();
                    Set<PartialConjunctionWithPart> resultForTechnicalGroupForSteeringRight = new LinkedHashSet<>();
                    boolean bestScoreFromOmittedPart = false;
                    iPartsDocumentationType documentationType = null;
                    for (PartialConjunctionWithPart partialConjunctionWithPart : technicalCodeSet) {
                        codeMatches.setValue(partialConjunctionWithPart.getMatchedModelBuildingCodes());
                        EtkDataPartListEntry partListEntry = partialConjunctionWithPart.getPartListEntry();
                        String steering = partListEntry.getFieldValue(iPartsConst.FIELD_K_STEERING);

                        // Bei einer Wegfallsachnummer nur dann die maximale Anzahl bm-bildender Code setzen, wenn diese
                        // auch mindestens einen bm-bildenden Code besitzt
                        if ((codeMatches.getValue() > 0) && omittedParts.isOmittedPart(partListEntry)) {
                            codeMatches.setValue(maxModelBuildingCodes);
                            bestScoreFromOmittedPart = true;
                        }

                        // Linkslenker
                        if (steering.trim().isEmpty() || steering.equals(SteeringIdentKeys.STEERING_LEFT)) {
                            bestScoreForSteeringLeft = getBestScoreForSteering(partialConjunctionWithPart, bestScoreForSteeringLeft,
                                                                               codeMatches, resultForTechnicalGroupForSteeringLeft,
                                                                               filter, bestScoreFromOmittedPart, omittedParts);
                        }

                        // Rechtslenker
                        if (steering.trim().isEmpty() || steering.equals(SteeringIdentKeys.STEERING_RIGHT)) {
                            bestScoreForSteeringRight = getBestScoreForSteering(partialConjunctionWithPart, bestScoreForSteeringRight,
                                                                                codeMatches, resultForTechnicalGroupForSteeringRight,
                                                                                filter, bestScoreFromOmittedPart, omittedParts);

                        }

                        if (documentationType == null) {
                            EtkDataAssembly ownerAssembly = partListEntry.getOwnerAssembly();
                            if (ownerAssembly instanceof iPartsDataAssembly) {
                                documentationType = ((iPartsDataAssembly)ownerAssembly).getDocumentationType();
                            }
                        }

                        // DAIMLER-8726 Bei Doku-Methode DIALOG_IPARTS: Wegfallsachnummer mit nur technischen Code in einer
                        // Teilkonjunktion müssen den Baumuster-Filter immer überleben
                        if (documentationType == iPartsDocumentationType.DIALOG_IPARTS) {
                            // Gibt es keine BM-bildenden Code, dann hat die Wegfallsachnummer nur technische Code in dieser
                            // Teilkonjunktion und darf nicht weggefiltert werden
                            if ((partialConjunctionWithPart.getMatchedModelBuildingCodes() == 0) && !overallResult.contains(partListEntry)
                                && omittedParts.isOmittedPart(partListEntry)) {
                                overallResult.add(partListEntry);
                            }
                        }
                    }

                    // Die validen PVs für die aktuelle Gruppe in das Ergebnisset der Methode legen
                    // Doppelte Einträge in "overallResult" bei einem leeren Lenkungswert können nicht entstehen, weil
                    // "overallResult" ein Set ist und in diesem Fall direkt die (identischen) EtkDataPartListEntries
                    // zum Set hinzugefügt werden würden
                    addAllRemainingPartListEntriesToOverallResult(overallResult, resultForTechnicalGroupForSteeringLeft);
                    addAllRemainingPartListEntriesToOverallResult(overallResult, resultForTechnicalGroupForSteeringRight);
                } else {
                    // Es gab nur eine PV für die technische Gruppe -> also ist diese PV gültig
                    overallResult.add(technicalCodeSet.iterator().next().getPartListEntry());
                }
            }
        }

        return overallResult;
    }

    /**
     * Fügt die Positionen, die innerhalb einer techn. Gruppe gewonnen haben, zum Gesamtergebnis hinzu.
     *
     * @param overallResult
     * @param resultForTechnicalGroupForSteering
     */
    private void addAllRemainingPartListEntriesToOverallResult(Set<EtkDataPartListEntry> overallResult, Set<PartialConjunctionWithPart> resultForTechnicalGroupForSteering) {
        if ((resultForTechnicalGroupForSteering != null) && !resultForTechnicalGroupForSteering.isEmpty()) {
            for (PartialConjunctionWithPart conjunctionWithPart : resultForTechnicalGroupForSteering) {
                overallResult.add(conjunctionWithPart.getPartListEntry());
            }
        }
    }

    /**
     * Bestimmt das Scoring-Ergebnis für eine aktuelle Position (gültig vs. nicht gültig) im Verhältnis zu den bisher
     * gültigen Positionen.
     *
     * @param currentPartialConjunctionWithPart
     * @param bestScoreForSteering
     * @param codeMatches
     * @param resultForTechnicalGroup
     * @param filter
     * @param bestScoreFromOmittedPart
     * @param omittedParts
     * @return
     */
    private int getBestScoreForSteering(PartialConjunctionWithPart currentPartialConjunctionWithPart, int bestScoreForSteering,
                                        VarParam<Integer> codeMatches, Set<PartialConjunctionWithPart> resultForTechnicalGroup,
                                        iPartsFilter filter, boolean bestScoreFromOmittedPart, iPartsOmittedParts omittedParts) {
        if (codeMatches.getValue() < bestScoreForSteering) {
            // Die aktuelle Teilkonjunktion hat weniger Treffer als das bisherige Maximum. Eigentlich wäre hier jetzt Schluss,
            // da die bisher gültigen Teilkonjunktionen gewinnen würden.
            // Ausnahme: Teilkonjunktionen mit "bm-bildenden Code UND techn. Code":
            // Diese Konstellation darf nur von Teilkonjunktionen mit der gleichen Konstellation (und mehr bm-bildende Code)
            // und Wegfallsachnummern (diese bekommen automatisch das Maximum) verdrängt werden. Falls die aktuelle
            // Teilkonjunktion also so eine Konstellation aufweist, muss geprüft werden, ob die bisherigen Teilkonjunktionen
            // die aktuelle Teilkonjunktion überhaupt verdrängen dürfen.
            //
            // HINWEIS: Seit DAIMLER-8305 dürfen Teilkonjunktionen mit techn. und bm-bildenden Code von Teilkonjunktionen
            // mit nur bm-bildenden Code gar nicht mehr verdrängt werden. Zuvor durften Teilkonjunktionen mit der maximal
            // Anzahl von bm-bildenden Code diese speziellen Teilkojunktionen ebenfalls verdrängen.
            //
            // Daher:
            boolean addToSurvivingEntries = true;
            boolean foundPCWithTechnicalCodeAndLessModelBuildingCodes = false;
            Set<PartialConjunctionWithPart> validPartialConjunctions = null;
            // 1. Check, ob die aktuelle Teilkonjunktion bm-bildenden und techn. Code hat
            if (currentPartialConjunctionWithPart.isTechnicalAndModelBuildingCodes()) {
                // 1.1 Sollte eine der gültigen Teilkonjunktionen identische techn. und weniger bm-bildende Code als die aktuelle
                // Teilkonjunktion haben, dann muss die bisherige Teilkonjunktion entfernt werden. Um das zu erreichen,
                // werden beim Durchlaufen des Sets alle gültigen Teilkonjunktionen in einem temporären Set gehalten.
                validPartialConjunctions = new HashSet<>();
                // 2. Durchlaufe alle bisher gültigen Teilkonjunktionen
                for (PartialConjunctionWithPart alreadyValidPartialConjunction : resultForTechnicalGroup) {
                    // 3. Falls eine der gültigen Teilkonjunktionen also eine Wegfallsachnummer (diese muss mindestens einen
                    // BM-bildenden Code haben, sonst wäre sie nicht in resultForTechnicalGroup bei einem bestScoreForSteering >= 1),
                    // dann ist die aktuelle Teilkonjunktion nicht gültig.
                    //
                    // Hat eine der gültigen Teilkonjunktionen techn. und weniger bm-bildende Code als die aktuelle
                    // Teilkonjunktion, dann muss die gültige durch die aktuelle ersetzt werden (da mehr Treffer).
                    // Deswegen wird die im Moment gültige Teilkonjunktion nicht in das temporäre Set der gültigen
                    // Teilkonjunktionen gelegt. Hat eine gültige Teilkonjunktion techn. und mehr bm-bildende Code, dann
                    // darf die aktuelle Teilkonjunktion nicht als gültig markiert werden
                    //
                    // Falls keine der bisher gültigen Teilkonjunktionen diese Kriterien erfüllt, dann füge die
                    // aktuelle Teilkonjunktion zu den gültigen hinzu.
                    if (omittedParts.isOmittedPart(alreadyValidPartialConjunction.getPartListEntry())) {
                        addToSurvivingEntries = false;
                        break;
                    } else if (alreadyValidPartialConjunction.isTechnicalAndModelBuildingCodes()) {
                        if (alreadyValidPartialConjunction.getMatchedModelBuildingCodes() < codeMatches.getValue()) {
                            // Hier wurde eine bisher gültige Teilkonjunktion gefunden, die zwar identische techn. aber weniger
                            // bm-bildende Code hat -> Füge sie nicht zum temporären Set hinzu
                            foundPCWithTechnicalCodeAndLessModelBuildingCodes = true;
                            continue;
                        } else if (alreadyValidPartialConjunction.getMatchedModelBuildingCodes() > codeMatches.getValue()) {
                            // Hier wurde eine bisher gültige Teilkonjunktion gefunden, die zwar identische techn. aber mehr
                            // bm-bildende Code hat -> Füge sie aktuelle nicht zu den gültigen hinzu
                            addToSurvivingEntries = false;
                            break;
                        }
                    }
                    validPartialConjunctions.add(alreadyValidPartialConjunction);
                }
            } else {
                addToSurvivingEntries = false;
            }

            if (addToSurvivingEntries) {
                // Die aktuelle wurde also nicht durch eine der drei oben genannten Teilkonjunktionstypen verdrängt.
                // Letzter Check: Wurde eine gültige Teilkonjunktion gefunden, die identische techn. aber weniger bm-bildende Code
                // hat? Falls ja, bisher gültige entfernen.
                if (foundPCWithTechnicalCodeAndLessModelBuildingCodes) {
                    resultForTechnicalGroup.clear();
                    resultForTechnicalGroup.addAll(validPartialConjunctions);
                }
                resultForTechnicalGroup.add(currentPartialConjunctionWithPart);
            } else {
                // Codebedingung erfüllt, aber die Anzahl an Treffer in den Code ist kleiner als bei den
                // vorher getesteten Einträgen -> nichts machen bzw. Filtergrund schreiben
                if ((filter != null) && filter.isWithFilterReason()) {
                    String bestScoreForSteeringString;
                    if (bestScoreForSteering == Float.MAX_VALUE) {
                        bestScoreForSteeringString = TranslationHandler.translate("!!(Entfallteil)");
                    } else {
                        bestScoreForSteeringString = String.valueOf(bestScoreForSteering);
                    }

                    if (bestScoreFromOmittedPart) {
                        filter.setFilterReasonForPartListEntry(currentPartialConjunctionWithPart.getPartListEntry(),
                                                               iPartsFilterSwitchboard.FilterTypes.MODEL,
                                                               "!!Durch Entfallteil verdrängt (Anzahl Treffer für baumusterbildende Code %1 < Maximum %2)",
                                                               String.valueOf(codeMatches.getValue()), bestScoreForSteeringString);
                    } else {
                        filter.setFilterReasonForPartListEntry(currentPartialConjunctionWithPart.getPartListEntry(),
                                                               iPartsFilterSwitchboard.FilterTypes.MODEL,
                                                               "!!Anzahl Treffer für baumusterbildende Code %1 < bisheriges Maximum %2 für gleiche DIALOG-Position",
                                                               String.valueOf(codeMatches.getValue()), bestScoreForSteeringString);
                    }
                }
            }
        } else if (codeMatches.getValue() > bestScoreForSteering) {
            // Mehr Code-Treffer als der beste Score. Alle bisher getesteten sind damit schlechter und müssen aus
            // dem Ergebnisset (für die aktuelle Gruppe) entfernt werden. Wenn der Filtergrund angegeben
            // werden soll, dann hier die entfernten PVs mit Grund versehen.
            // Ausnahme: Teilkonjunktionen mit "bm-bildenden Code UND techn. Code":
            // Diese Konstellation darf nur von Teilkonjunktionen mit der gleichen Konstellation (und mehr bm-bildende Code),
            // Wegfallsachnummern (diese bekommen automatisch das Maximum) und durch Teilkonjunktion mit der maximalen Anzahl
            // an bm-bildenden Code und OHNE techn. Code verdrängt werden.
            Set<PartialConjunctionWithPart> survivorEntries = new HashSet<>();
            boolean withFilterReason = (filter != null) && filter.isWithFilterReason();
            for (PartialConjunctionWithPart entry : resultForTechnicalGroup) {
                if (entry.isTechnicalAndModelBuildingCodes()) {
                    // In den bisher gültigen Teilkonjunktionen wurde eine gefunden, die identischen techn. (weil gleiche Gruppe)
                    // und weniger bm-bildende Code hat. Prüfe, ob diese Teilkonjunktion von der aktuellen verdrängt werden
                    // darf. Falls nicht, wird sie in einem temporären Set gehalten und später zu den gültigen Teilkonjunktionen
                    // hinzugefügt. Sie bleibt erhalten, wenn der beste Score nicht von einer Wegfallsachnummer kommt und
                    // die aktuelle Teilkonjunktion nur bm-bildende Code hat.
                    if (!bestScoreFromOmittedPart && !currentPartialConjunctionWithPart.isTechnicalAndModelBuildingCodes()) {
                        survivorEntries.add(entry);
                        continue;
                    }
                }
                if (withFilterReason) {
                    String codeMatchesString;
                    if (bestScoreForSteering == Float.MAX_VALUE) {
                        codeMatchesString = TranslationHandler.translate("!!(Entfallteil)");
                    } else {
                        codeMatchesString = String.valueOf(codeMatches.getValue());
                    }

                    if (bestScoreFromOmittedPart) {
                        filter.setFilterReasonForPartListEntry(entry.getPartListEntry(),
                                                               iPartsFilterSwitchboard.FilterTypes.MODEL,
                                                               "!!Durch Entfallteil verdrängt (Anzahl Treffer für baumusterbildende Code (<= %1) < Maximum %2)",
                                                               String.valueOf(bestScoreForSteering), codeMatchesString);
                    } else {
                        filter.setFilterReasonForPartListEntry(entry.getPartListEntry(), iPartsFilterSwitchboard.FilterTypes.MODEL,
                                                               "!!Anzahl Treffer für baumusterbildende Code (<= %1) < neues Maximum %2 für gleiche DIALOG-Position",
                                                               String.valueOf(bestScoreForSteering), codeMatchesString);
                    }
                }
            }


            // Entferne die nicht mehr validen PVs (für die aktuelle Gruppe)
            resultForTechnicalGroup.clear();
            // Füge die aktuell valide PV hinzu
            resultForTechnicalGroup.add(currentPartialConjunctionWithPart);
            if (!survivorEntries.isEmpty()) {
                resultForTechnicalGroup.addAll(survivorEntries);
            }
            bestScoreForSteering = codeMatches.getValue();
        } else {
            // Codebedingung gleich und die Codetreffer sind identisch zu den vorher getesteten Einträgen
            // Dieses Teil bleibt als zusätzliche Positionsvariante stehen.
            resultForTechnicalGroup.add(currentPartialConjunctionWithPart);
        }

        return bestScoreForSteering;
    }

    /**
     * Berechnet die Gruppierung der technischen Code bezogen auf alle Teilkonjunktionen aller PVs, die man in dieser
     * Zeitscheibe vergleichen muss. Negative bm-bildende Code werden ebenfalls als techn. Code gewertet und fließen in
     * die Gruppenbildung ein.
     *
     * Bsp:
     * M005 und M006 sind bm-bildende Code am Filter-Baumuster. M008 is ein bm-bildender Code an einem anderen
     * Baumuster zur Baueihe.
     *
     * T1: M005 + 480 + 650
     * T2: M006 + 480 + 752
     * T3: -M008 + 650 + 480
     * T4: 650 + 480
     *
     * Gruppierung:
     * 480 + 650 -> T1,T4
     * 480 + 752 -> T2
     * 480 + 650 - M008 -> T3
     *
     * @param entriesToCompare
     * @param modelBuildingCodeSetFromDataCards Alle tatsächlichen baumusterbildenden Code von allen relevanten Datenkarten
     *                                          für das Baumuster
     * @param filter
     * @param maxModelBuildingCodes             Maximale Anzahl bm-bildende Code am Baumuster
     * @return
     */
    private Map<String, Set<PartialConjunctionWithPart>> calculateTechnicalCodeSets(List<EtkDataPartListEntry> entriesToCompare,
                                                                                    Set<String> modelBuildingCodeSetFromDataCards,
                                                                                    iPartsFilter filter, int maxModelBuildingCodes) {
        // Map für das Endergebnis (alle erzeugten Gruppen)
        Map<String, Set<PartialConjunctionWithPart>> resultMapWithAllGroups = new HashMap<>();

        for (EtkDataPartListEntry pvPartListEntry : entriesToCompare) {
            String codeRule = pvPartListEntry.getFieldValue(iPartsConst.FIELD_K_CODES);

            try {

                Disjunction partialConjunctions = null;
                if (!DaimlerCodes.isEmptyCodeString(codeRule)) { // Teilkonjunktionen müssen für einen leeren Code nicht berechnet werden
                    // Klonen der DNF findet in basicCheckCodeFilter() statt
                    partialConjunctions = DaimlerCodes.getDnfCodeOriginal(codeRule);
                }

                // Spezialfall für ein leeres Ergebnis für die Teilkonjunktionen -> das Teil ist immer gültig
                // und muss genauso behandelt werden, als hätte es keine technischen Codes sondern nur baumusterbildende Codes
                if ((partialConjunctions == null) || partialConjunctions.isEmpty()) {
                    List<String> technicalCodesForKey = new DwList<>();
                    technicalCodesForKey.add(HAS_ONLY_BM_CODES);
                    createPartialConjunctionWithPart(technicalCodesForKey, resultMapWithAllGroups, pvPartListEntry,
                                                     new Disjunction(), false, false, 0);
                    continue;
                }
                boolean notValidForBMCode = true;
                for (Conjunction partialConjunction : partialConjunctions) {
                    // Ist die Teilkonjunktion true? (Positiv-Codes auf true, Negative Codes auf false, technische Code
                    // auf true). Falls nein, nicht weiter betrachten.
                    // Für die Code-Prüfung nur die tatsächlichen baumusterbildenden Code von allen relevanten Datenkarten
                    // für das Baumuster verwenden.
                    // Hier nur die positiven Matches zählen, wobei bei einem negativen Match ja die gesamte Teilkonjunktion
                    // "false" wäre und somit das Ergebnis "modelBuildingMatches" gar nicht berücksichtigt werden würde.
                    // Außerdem zählen negative BM-bildende Code inzwischen wie technische Code.
                    // Treffer-Ergebnis wird gehalten, um später unterscheiden zu können, ob bm-bildende Code in der Teilkonjunktion
                    // enthalten sind.
                    VarParam<Integer> modelBuildingMatches = new VarParam<>();
                    if (iPartsFilterHelper.basicCheckCodeFilter(partialConjunction, modelBuildingCodeSetFromDataCards, null,
                                                                modelBuildingCodeSet, null, modelBuildingMatches)) {
                        if (notValidForBMCode) {
                            notValidForBMCode = false;
                        }

                        List<String> technicalCodesForKey = new DwList<>(); // Schlüssel für die Gruppierung auf Basis gleicher Code

                        PositiveAndNegativeTerms terms = partialConjunction.getPositiveAndNegativeTerms(false);
                        boolean technicalAndModelBuildingCodes = false;
                        for (String code : terms.getPositiveTerms()) {
                            if (!modelBuildingCodeSet.contains(code)) {
                                String codeWithBool = "+" + code;
                                technicalCodesForKey.add(codeWithBool);

                                // An dieser Stelle haben wir mind. einen techn. Code. Wenn "modelBuildingMatches" größer
                                // "0" ist, dann handelt es sich um eine Teilkonjunktion mit techn. und bm-bildenden Code.
                                // -> Diese Info in der Variablen "technicalAndModelBuildingCodes" halten
                                if (modelBuildingMatches.getValue() > 0) {
                                    technicalAndModelBuildingCodes = true;
                                }
                            }
                        }

                        // Beim Durchlaufen der negativen Code in der Teilkonjunktion müssen auch negative bm-bildende
                        // Code berücksichtigt werden, da diese laut DAIMLER-7654 ebenfalls als techn. Code verarbeitet
                        // werden sollen.
                        for (String code : terms.getNegativeTerms()) {
                            String codeWithBool = "-" + code;
                            technicalCodesForKey.add(codeWithBool);
                        }

                        // Sind nur bm-bildende Code enthalten -> leerer Eintrag in der technische Code Liste
                        boolean onlyModelBuildingCodes = false;
                        if (technicalCodesForKey.isEmpty()) {
                            onlyModelBuildingCodes = true;
                            technicalCodesForKey.add(HAS_ONLY_BM_CODES);
                        }

                        // Damit die 'RemovedTerms', also die wegoptimierten Terme beim Scoring berücksichtigt werden können,
                        // müssen wir hier eine temporäre Disjunction mit nur einem Element machen und an diese Disjunction die
                        // wegoptimierten Codes dranhängen.
                        // Es wird so getan, dass in jeder Conjunction auch die wegoptimierten gültig waren.
                        // Relevant sind nur die BM-bildenden Codes -> deswegen muss die Disjunction erst jetzt erzeugt werden.
                        Disjunction tempDisjunction = new Disjunction(partialConjunction);
                        tempDisjunction.addToRemovedTerms(partialConjunctions.getRemovedTerms());
                        createPartialConjunctionWithPart(technicalCodesForKey, resultMapWithAllGroups, pvPartListEntry,
                                                         tempDisjunction, technicalAndModelBuildingCodes,
                                                         (modelBuildingMatches.getValue() == maxModelBuildingCodes) && onlyModelBuildingCodes,
                                                         modelBuildingMatches.getValue());
                    }
                }
                // Den Filtergrund erst setzten, wenn wirklich alle Teilkonjunktionen zu den BM-Code ungültig sind
                if (notValidForBMCode && (filter != null) && filter.isWithFilterReason()) {
                    filter.setFilterReasonForPartListEntry(pvPartListEntry, iPartsFilterSwitchboard.FilterTypes.MODEL,
                                                           "!!Code nicht gültig für die Code %1",
                                                           iPartsFilterHelper.getFilterReasonSourceName(true));
                }

            } catch (BooleanFunctionSyntaxException e) {
                RuntimeException runtimeException = new RuntimeException("Error in code \"" + codeRule + "\": " + e.getMessage(), e);
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_CODES, LogType.ERROR, runtimeException);
            }
        }

        // DAIMLER-7654: Teilkonjunktionen mit nur bm-bildenden Code sollen mit allen Teilkonjunktionen aus den anderen
        // Gruppen verglichen werden. Eine spezielle Ausnahme sind Teilkonjunktionen, die techn. und bm-bildende Code besitzen.
        // Da dieser Speziallfall nicht auf Basis der Gruppenbildung abgearbeitet werden kann, wurde die Sonderlogik
        // in das eigentlich Scoring verschoben (folgt direkt nach der Gruppenbildung).
        Set<PartialConjunctionWithPart> onlyBmCodeData = resultMapWithAllGroups.get(HAS_ONLY_BM_CODES);
        if (onlyBmCodeData != null) {
            for (Set<PartialConjunctionWithPart> technicalCodeSet : resultMapWithAllGroups.values()) {
                technicalCodeSet.addAll(onlyBmCodeData);
            }
        }

        return resultMapWithAllGroups;
    }

    /**
     * Erstellt ein Objekt von {@link de.docware.apps.etk.plugins.customer.daimler.iparts.filter.dialog.DIALOGModelFilter.PartialConjunctionWithPart}
     * und legt es abhängig vom Code-Schlüssel in der übergebenen Map ab. Das Objekt wird für den späteren Vergleich benötigt.
     *
     * @param technicalCodesForKey           String-Elemente, um daraus den Gruppenschlüssel zu generieren
     * @param technicalCodes                 Map mit allen bisherigen Gruppen
     * @param pvPartListEntry                Der aktuelle Stücklisteneintrag
     * @param partialConjunction             Die Teilkonjunktion ist vom Typ her eine {@link Disjunction} mit genau einer Konjunktion,
     *                                       damit auch wegoptimierte Terme beim Scoring berücksichtigt werden können
     * @param technicalAndModelBuildingCodes Handelt es sich bei der Teilkonjunktion um den Sonderfall "techn. und bm-bildende Code"?
     * @param onlyAndAllModelBuildingCodes   Enthält die Teilkonjunktion nur und vor allem alle bm-bildende Code?
     * @param matchedModelBuildingCodes      Anzahl bm-bildende Code in der Teilkonjunktion
     */
    private void createPartialConjunctionWithPart(List<String> technicalCodesForKey, Map<String, Set<PartialConjunctionWithPart>> technicalCodes,
                                                  EtkDataPartListEntry pvPartListEntry, Disjunction partialConjunction,
                                                  boolean technicalAndModelBuildingCodes, boolean onlyAndAllModelBuildingCodes,
                                                  int matchedModelBuildingCodes) {
        if (!technicalCodesForKey.isEmpty()) {
            String technicalCodeKey = createTechnicalCodeKey(technicalCodesForKey);
            Set<PartialConjunctionWithPart> tempList = technicalCodes.get(technicalCodeKey);
            if (tempList == null) {
                tempList = new LinkedHashSet<>();
            }
            tempList.add(new PartialConjunctionWithPart(pvPartListEntry, partialConjunction, technicalAndModelBuildingCodes,
                                                        onlyAndAllModelBuildingCodes, matchedModelBuildingCodes));
            technicalCodes.put(technicalCodeKey, tempList);
        }
    }

    /**
     * Erstellt den Schlüssel für die Gruppierung von technischen Coden. Der Schlüssel besteht aus allen technischen Coden
     * einer Teilkonjunktion in aufsteigender Reihenfolge.
     *
     * @param technicalCodesForKey
     * @return
     */
    private String createTechnicalCodeKey(List<String> technicalCodesForKey) {
        Collections.sort(technicalCodesForKey, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                String s1 = Utils.toSortString(o1);
                String s2 = Utils.toSortString(o2);
                return s1.compareTo(s2);
            }
        });
        return StrUtils.makeDelimitedString("\t", ArrayUtil.toStringArray(technicalCodesForKey));
    }


    /**
     * Stücklisteneintrag mit dazugehöriger Teilkonjunktion. Die Teilkonjunktion ist vom Typ her eine {@link Disjunction}
     * mit genau einer Konjunktion, damit auch wegoptimierten Terme beim Scoring berücksichtigt werden können.
     */
    class PartialConjunctionWithPart {

        private EtkDataPartListEntry partListEntry;
        private Disjunction partialConjunctionForBMScore;
        private boolean technicalAndModelBuildingCodes;
        private boolean matchesOnlyAndAllModelBuldingCodes;
        private int matchedModelBuildingCodes;

        public PartialConjunctionWithPart(EtkDataPartListEntry partListEntry, Disjunction partialConjunctionForBMScore,
                                          boolean technicalAndModelBuildingCodes, boolean matchesOnlyAndAllModelBuldingCodes,
                                          int matchedModelBuildingCodes) {
            this.partListEntry = partListEntry;
            this.partialConjunctionForBMScore = partialConjunctionForBMScore;
            this.technicalAndModelBuildingCodes = technicalAndModelBuildingCodes;
            this.matchesOnlyAndAllModelBuldingCodes = matchesOnlyAndAllModelBuldingCodes;
            this.matchedModelBuildingCodes = matchedModelBuildingCodes;
        }

        public EtkDataPartListEntry getPartListEntry() {
            return partListEntry;
        }

        public Disjunction getPartialConjunctionForBMScore() {
            return partialConjunctionForBMScore;
        }

        public boolean isTechnicalAndModelBuildingCodes() {
            return technicalAndModelBuildingCodes;
        }

        public boolean isMatchesOnlyAndAllModelBuldingCodes() {
            return matchesOnlyAndAllModelBuldingCodes;
        }

        public int getMatchedModelBuildingCodes() {
            return matchedModelBuildingCodes;
        }
    }

}
