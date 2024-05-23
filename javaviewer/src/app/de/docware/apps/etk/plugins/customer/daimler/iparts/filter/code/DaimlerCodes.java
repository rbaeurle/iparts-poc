/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsAccAndAsCodeCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.cache.ObjectInstanceLRUList;
import de.docware.util.misc.booleanfunctionparser.*;
import de.docware.util.misc.booleanfunctionparser.model.AbstractTerm;
import de.docware.util.misc.booleanfunctionparser.model.Conjunction;
import de.docware.util.misc.booleanfunctionparser.model.Disjunction;

import java.util.*;

/**
 * Von AH aus Delphi nach Java teilweise per autom. Codegenerierung erzeugt.
 * Dient zur Auswertung von Daimler/EvoBus Codebedingungen.
 */
public class DaimlerCodes {

    // Zwischengespeicherte schon berechnete DNFs
    static private ObjectInstanceLRUList<String, Disjunction> cachedDnfs = new ObjectInstanceLRUList<>(iPartsConst.MAX_CACHE_SIZE_CODES,
                                                                                                       iPartsConst.MAX_CACHE_LIFE_TIME_CORE);

    final static private char[] VALID_CODE_DELIMITER = new char[]{ '+', '-', '/', ';', '(', ')' };
    final static public char[] VALID_COLUMN_FILTER_CODE_DELIMITER = new char[]{ '+', '/', ';' };

    /**
     * Wandelt einen vom Benutzer eingegebenen Code in die übliche Darstellung
     *
     * @param code
     * @return
     */
    public static String beautifyCodeString(String code) {
        return StrUtils.addCharacterIfLastCharacterIsNot(code.trim(), ';');
    }

    public static Set<String> getCodeSet(String codeString) {
        if (!isEmptyCodeString(codeString)) {
            BooleanFunction func = getFunctionParser(codeString);
            return func.getVariableNames();
        } else {
            return new LinkedHashSet<String>();
        }
    }

    public static boolean syntaxIsOK(String codeString) {
        if (!isEmptyCodeString(codeString)) {
            // Semikolon nur am Ende valide
            if (codeString.contains(";")) {
                if (codeString.indexOf(";") != codeString.length() - 1) {
                    return false;
                }
            }
            BooleanFunction func = getFunctionParser(codeString);
            // Aufrufen, um bei Syntaxfehlern hier eine Exception zurück zu bekommen
            try {
                func.calculateWithSyntaxCheck();
            } catch (BooleanFunctionSyntaxException e) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEmptyCodeString(String codeString) {
        return (codeString == null) || codeString.isEmpty() || codeString.trim().equals(";");
    }

    public static boolean codeContainsCodeDelimiter(String codeString) {
        if (codeString.isEmpty()) {
            return false;
        }
        return containsCodeDelimiter(codeString, VALID_CODE_DELIMITER);
    }

    public static boolean codeContainsCodeDelimiterForColumnFilter(String codeString) {
        if (codeString.isEmpty()) {
            return false;
        }
        return containsCodeDelimiter(codeString, VALID_COLUMN_FILTER_CODE_DELIMITER);
    }

    private static boolean containsCodeDelimiter(String codeString, char[] validDelimiters) {
        for (int i = 0; i < codeString.length(); i++) {
            char actChar = codeString.charAt(i);
            if (ArrayUtil.indexOf(validDelimiters, actChar) > -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ermittelt, ob der Code bei den angegebenen Codes des Fahrzeugs trifft oder nicht.
     * Die Funktion kann auch aufgerufen werden, um nur die in der Coderegel verwendeten Codes zurückzuliefern
     *
     * @param vehicleCodes Kann null sein, wenn nur die Liste der codes ermittelt werden soll
     * @param codeString
     * @return
     */
    public static boolean isCodeMatch(Set<String> vehicleCodes, String codeString) throws BooleanFunctionSyntaxException {
        if ((codeString.isEmpty()) || (codeString.equals(";"))) {
            return true;
        } else {
            BooleanFunction func = getFunctionParser(codeString);

            if (vehicleCodes != null) {
                for (String vehicleCode : vehicleCodes) {
                    func.setVariableValue(vehicleCode, DaimlerCodes.codeMatches(vehicleCode, vehicleCodes));
                }
            }

            try {
                return func.calculateWithSyntaxCheck();
            } catch (BooleanFunctionSyntaxException e) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_CODES, LogType.DEBUG, "Code error: codeString ='" + codeString + "', func='"
                                                                          + func.toString() + "'");
                throw e;
            }
        }
    }

    private static boolean codeMatches(String code, Set<String> vehicleCodes) {
        if (code.equals("ET")) {
            return true;
        }
        if (code.startsWith("TZ")) {
            return true;
        }

        //Jetzt nachsehen, ob das Fahrzeug den Code hat
        return vehicleCodes.contains(code);
    }

    /**
     * Komplementäre Methode zu fromFunctionParser()
     *
     * @param codeString
     * @return
     */
    public static BooleanFunction getFunctionParser(String codeString) {
        BooleanFunction result = new BooleanFunction();

        //Zerlege den String
        String currentCode = "";
        for (int i = 0; i < codeString.length(); i++) {
            char currentChar = codeString.charAt(i);
            if (currentChar == ' ') {
                // Leerzeichen sollten eigentlich nicht vorkommen, sind aber trotzdem drin
                continue;
            }
            if ((currentChar == '+') || (currentChar == '-') || (currentChar == '/') || (currentChar == ';') || (currentChar == '(') || (currentChar == ')')) {
                //Aufgesammelten Code einschreiben
                if (!currentCode.isEmpty()) {
                    result.addBoolField(currentCode, false);
                    currentCode = "";
                }
                switch (currentChar) {
                    case '+': {
                        if (!result.isEmpty()) {
                            // And ist eigentlich am Anfang nicht erlaubt, es gibt aber Codes in der Form +809
                            // in diesem Fall kann das '+' einfach ignoriert werden.
                            // Das '+' wird also nur als 'and' Operator eingefügt, wenn es nicht am Anfang ist
                            result.addOperator(BooleanOperator.AND);
                        }
                    }
                    break;
                    case '-': {
                        if (!result.isEmpty()) {
                            char lastChar = codeString.charAt(i - 1);
                            if ((lastChar != '+') && (lastChar != '-') && (lastChar != '/') && (lastChar != ';') && (lastChar != '(')) {
                                //Verkürzte Form ala -ABS-ABC/(OTTO-FOO)
                                //deshalb hier das plus vor dem not einfügen
                                result.addOperator(BooleanOperator.AND);
                            }
                        }

                        result.addOperator(BooleanOperator.NOT);
                    }
                    break;
                    case '/': {
                        result.addOperator(BooleanOperator.OR);
                    }
                    break;
                    case '(': {
                        result.addOperator(BooleanOperator.BRACKET_OPEN);
                    }
                    break;
                    case ')': {
                        result.addOperator(BooleanOperator.BRACKET_CLOSE);
                    }
                    break;
                    default: {
                    }
                }
            } else {
                currentCode = currentCode + currentChar;
            }
        }
        if (!currentCode.isEmpty()) {
            result.addBoolField(currentCode, false);
        }
        return result;
    }

    /**
     * Komplementäre Methode zu getFunctionParser()
     *
     * @param booleanFunction
     * @return
     */
    public static String fromFunctionParser(BooleanFunction booleanFunction) {
        StringBuilder sb = new StringBuilder();
        for (BooleanFunctionElement element : booleanFunction.getFunctionElements()) {
            if (element instanceof BooleanFunctionOperator) {
                sb.append(((BooleanFunctionOperator)element).getValue().getValue());
            } else {
                sb.append(element.toString());
            }
        }
        sb.append(";");
        return sb.toString();
    }

    /**
     * AS- und Zubehörcodes aus Code-String entfernen
     *
     * @param project
     * @param codeString
     * @param codesRemoved hier sammeln wir die tatsächlich entfernten Codes auf
     * @param logMessages  für Ausgabe in Konsole des Importers bzw. MessageDialog bei manueller Übernahme
     * @return reduzierter Codestring
     */
    public static String reduceCodeString(EtkProject project, final String codeString, Set<String> codesRemoved, List<String> logMessages) {
        Set<String> codesToRemove = iPartsAccAndAsCodeCache.getInstance(project).getAllAccAndAsCodes();
        try {
            // DAIMLER-15435: Besitzt die Coderegel nach der Bildung der DNF eine Teilkonjunktion mit einem AS-Code
            // (Code aus der DB-Tabelle DA_AS_CODES, DA_ACC_CODES) und alle weiteren Teilkonjunktionen besitzen ebenfalls
            // eine positiven AS-Code in der Teilkonjunktion, dann soll die gekürzte Coderegel Semikolon ergeben.
            // DAIMLER-15712:  besitzt die Coderegel nach der Bildung der DNF eine Teilkonjunktion NUR mit einem AS-Code
            // (Code aus der DB-Tabelle DA_AS_CODES, DA_ACC_CODES), unabhängig der weiteren Teilkonjunktionen,
            // dann soll die gekürzte Coderegel Semikolon ergeben. (parameter checkForSingleOccurence=true)
            if (hasASOrACCCodeInEveryConjunction(codeString, codesToRemove, true)) {
                return ";";
            }
            return removeCodesSafe(codeString, codesToRemove, codesRemoved);
        } catch (DaimlerCodesException e) {
            try {
                String resultCode = removeCodes(codeString, codesToRemove, codesRemoved);
                if (syntaxIsOK(resultCode)) {
                    if (logMessages != null) {
                        logMessages.add(TranslationHandler.translate("!!Modifizierter Codestring \"%1\" wird übernommen.%2Das fehlerhafte Original: \"%3\".",
                                                                     resultCode, "\n", codeString));
                    }
                    return resultCode;
                } else {
                    if (logMessages != null) {
                        logMessages.add(e.getLocalizedMessage() + " " + TranslationHandler.translate("!!Original Codestring \"%1\" wird unverändert übernommen.",
                                                                                                     codeString));
                    }
                    return codeString;
                }
            } catch (DaimlerCodesException ee) {
                if (logMessages != null) {
                    logMessages.add(ee.getLocalizedMessage() + " " + TranslationHandler.translate("!!Original Codestring \"%1\" wird unverändert übernommen.",
                                                                                                  codeString));
                }
                return codeString;
            }
        }
    }

    /**
     * Überprüft, ob alle Teilkonjunktionen den gleichen AS oder ACC Code besitzen und eine Teilkonjunktion explizit nur
     * diesen Code als einzelnen Term hält.
     *
     * @param codeString
     * @param asAndACCCodes
     * @return
     * @throws DaimlerCodesException
     */
    private static boolean hasASOrACCCodeInEveryConjunction(String codeString, Set<String> asAndACCCodes,
                                                            boolean checkForSingleOccurence) throws DaimlerCodesException {
        if (StrUtils.isEmpty(codeString) || (asAndACCCodes == null) || asAndACCCodes.isEmpty()) {
            return false;
        }
        try {
            // DNF bilden um alle Teilkonjunktionen zu bekommen
            Disjunction disjunction = getDnfCode(codeString);
            Set<String> singleASCodes = new HashSet<>(); // Map mit allen AS/ACC Code, die einzeln vorkommen
            Map<String, Integer> codeCounterMap = new HashMap<>(); // Map mit allen AS/ACC Code und die Anzahlt der Teilkonjunktionen in denen sie vorkommen
            for (Conjunction conjunction : disjunction) {
                // Check, ob die Teilkonjunktion nur einen Term hat
                if (conjunction.size() == 1) {
                    // Falls ja, prüfen, ob es sich um einen AS/ACC Code handelt
                    String termString = conjunction.get(0).toString();
                    // Wir haben einen Treffer. Den Code in das Set mit den Einzelcode und in die Map mit den Treffer legen
                    if (asAndACCCodes.contains(termString)) {
                        singleASCodes.add(termString);
                        incCodeCounter(codeCounterMap, termString);
                        if (checkForSingleOccurence) {
                            return true;
                        }
                    }
                } else {
                    // Alle Terme in der Teilkonjunktion durchlaufen und prüfen, ob ein AC/ASS Code vorhanden ist
                    for (AbstractTerm term : conjunction) {
                        String termString = term.toString();
                        // Wir haben einen Treffer. Den Code in die Map mit den Treffer legen
                        if (asAndACCCodes.contains(termString)) {
                            incCodeCounter(codeCounterMap, termString);
                        }
                    }
                }
            }
            // Falls keine AS/ACC Code gefunden wurden -> raus
            if (singleASCodes.isEmpty() || codeCounterMap.isEmpty()) {
                return false;
            }
            // Jetzt alle gefundenen AS/ACC Code durchlaufen und prüfen, ob es einen Code gibt, der in jeder Teilkonjunktion
            // vorkommt und zusätzlich als Einzelcode vorhanden ist
            int conjunctionsAmount = disjunction.size();
            Optional<Map.Entry<String, Integer>> foundASCode = codeCounterMap.entrySet().stream().filter(entry -> (entry.getValue() == conjunctionsAmount) && singleASCodes.contains(entry.getKey())).findAny();
            return foundASCode.isPresent();
        } catch (BooleanFunctionSyntaxException e) {
            throw new DaimlerCodesException("!!Ungültiger Codestring \"%s\" für das Reduzieren der AS und ACC Code zu \";\".", codeString);
        }
    }

    private static void incCodeCounter(Map<String, Integer> codeCounterMap, String termString) {
        Integer countForTerm = codeCounterMap.computeIfAbsent(termString, k -> 0);
        countForTerm++;
        codeCounterMap.put(termString, countForTerm);
    }

    /**
     * Codes aus Codestring entfernen
     *
     * @param codeString
     * @param codesToRemove zu entfernende Codes
     * @param codesRemoved  entfernte Codes
     * @return
     * @throws DaimlerCodesException bei behandelten Fehlern
     */
    public static String removeCodes(final String codeString, Set<String> codesToRemove, Set<String> codesRemoved) throws DaimlerCodesException {
        // Zu entfernende Codes suchen
        boolean codeToRemoveFound = false;
        for (String codeToRemove : codesToRemove) {
            if (codeString.contains(codeToRemove)) {
                codeToRemoveFound = true;
                break;
            }
        }

        // Keine zu entfernenden Codes gefunden -> Original-Code-Regel zurückliefern
        if (!codeToRemoveFound) {
            return codeString;
        }

        // zunächst validieren wir den Original-String. Auch der könnte ja schon invalide sein.
        if (!syntaxIsOK(codeString)) {
            throw new DaimlerCodesException("!!Original Codestring \"%s\" invalide!", codeString);
        }

        String newCodeString = codeString;

        try {
            // Der Code wird in einen BooleanFunctionTermTreeAnalyzer für den Term-Baum umgewandelt.
            // In diesem Baum lassen sich die einzelnen Codes recht einfach und sicher löschen.
            // Später wird dann aus dem Baum wieder eine Daimler-Code-Regel generiert.
            BooleanFunction booleanFunction = DaimlerCodes.getFunctionParser(newCodeString);
            BooleanFunctionTermTreeAnalyzer termTree = new BooleanFunctionTermTreeAnalyzer(booleanFunction);

            // Lösche die Codes zunächst nur in den SimpleTerms der Disjunktion des Term-Baums
            for (String codeToRemove : codesToRemove) {
                if (termTree.removeBoolField(codeToRemove, false)) {
                    codesRemoved.add(codeToRemove);
                }
            }

            // Aus dem Term-Baum wieder eine eine Daimler-Code-Regel generieren
            booleanFunction.setFunctionElements(termTree.getBooleanFunctionElements());
            newCodeString = DaimlerCodes.fromFunctionParser(booleanFunction);

            // Wenn die Code-Regeln identisch sind, dann wurde in den SimpleTerms der Disjunktion des Term-Baums auf keinen
            // Fall ein zu entfernender Code gefunden -> die Suche nach zu entfernenden Code kann man sich sparen, da zu
            // Beginn dieser Methode ja bereits festgestellt wurde, dass es definitiv zu entfernende Code gibt
            codeToRemoveFound = newCodeString.equals(codeString);

            if (!codeToRemoveFound) {
                // Zu entfernende Codes in der bereits reduzierten Code-Regel suchen (wenn es jetzt noch Treffer gibt, dann
                // müssen sich diese in einer komplexen Teilkonjunktion befinden)
                for (String codeToRemove : codesToRemove) {
                    if (newCodeString.contains(codeToRemove)) {
                        codeToRemoveFound = true;
                        break;
                    }
                }
            }

            if (codeToRemoveFound) {
                // Mindestens ein zu entfernender Code wurde noch gefunden -> dieser muss sich in einer komplexen Teilkonjunktion
                // befinden -> Aus dem Code muss eine DNF gebildet und daraus ein BooleanFunctionTermTreeAnalyzer für den
                // Term-Baum erzeugt werden. Danach nochmal die Codes auch in komplexen Termen entfernen.
                // Nicht DaimlerCodes.getDnfCodeFunctionOriginal() verwenden, weil die DNF über die booleanFunction und
                // den termTree verändert wird
                booleanFunction = new BooleanFunction(DaimlerCodes.getDnfCode(codeString));
                termTree = new BooleanFunctionTermTreeAnalyzer(booleanFunction);

                // Lösche die Codes im gesamten Term-Baum inkl. komplexen Termen
                for (String codeToRemove : codesToRemove) {
                    if (termTree.removeBoolField(codeToRemove, true)) {
                        codesRemoved.add(codeToRemove);
                    }
                }

                // Aus dem Term-Baum wieder eine eine Daimler-Code-Regel generieren
                booleanFunction.setFunctionElements(termTree.getBooleanFunctionElements());
                newCodeString = DaimlerCodes.fromFunctionParser(booleanFunction);
            }

            if (DaimlerCodes.syntaxIsOK(newCodeString)) {
                return newCodeString;
            } else {
                throw new DaimlerCodesException("!!Ungültiger Codestring nach Entfernen von Codes \"%s\".", newCodeString);
            }
        } catch (BooleanFunctionSyntaxException e) {
            throw new DaimlerCodesException("!!Ungültiger Codestring nach Entfernen von Codes \"%s\".", newCodeString);
        }

    }


    /**
     * Sicheres Code-Entfernen durch Vergleich zweier Varianten
     * siehe https://jira.docware.de/jira/browse/DAIMLER-2772?focusedCommentId=75428&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-75428
     *
     * @param codeString
     * @param codesToRemove
     * @param codesRemoved
     * @return
     * @throws DaimlerCodesException
     */
    public static String removeCodesSafe(final String codeString, Set<String> codesToRemove, Set<String> codesRemoved) throws DaimlerCodesException {
        try {
            // Codes entfernen

            String codeStringReduced = removeCodes(codeString, codesToRemove, codesRemoved);
            if (codesRemoved.isEmpty()) { // es wurden gar keine Codes entfernt -> Original Coderegel zurückliefern
                return codeString;
            }

            /**
             * booleanFunction1: Variante mit dem Entfernen der Codes vor dem Bilden der disjunktiven Normalform
             */

            // disjunktive Normalform bilden
            // booleanFunction1 und booleanFunction2 werden nicht verändert -> kein Klon der DNF notwendig
            BooleanFunction booleanFunction1 = DaimlerCodes.getDnfCodeFunctionOriginal(codeStringReduced);

            /**
             * booleanFunction2: Variante mit dem Entfernen der Codes nach dem Bilden der disjunktiven Normalform
             */

            // disjunktive Normalform aus Original Codestring bilden
            BooleanFunction booleanFunction2 = DaimlerCodes.getDnfCodeFunctionOriginal(codeString);
            String codeString2 = fromFunctionParser(booleanFunction2);

            // auf negative zu entfernende Codes prüfen
            int i = 0;
            for (BooleanFunctionElement element : booleanFunction2.getFunctionElements()) {
                if (i > 0) {
                    if (element instanceof BooleanFunctionBoolField) {
                        String code = element.getVariableName();
                        if (codesToRemove.contains(code)) {
                            BooleanFunctionElement previousElement = booleanFunction2.getFunctionElements().get(i - 1);
                            if (previousElement instanceof BooleanFunctionOperator) {
                                if (((BooleanFunctionOperator)previousElement).getValue().getValue().equals("-")) {
                                    throw new DaimlerCodesException("!!Sicheres Entfernen von Codes führte zu negativem Code in disjunktiver Normalform: \"%s\"", codeString2);
                                }
                            }
                        }
                    }
                }
                i++;
            }


            String codeString2Reduced = removeCodes(codeString2, codesToRemove, new HashSet<String>());
            booleanFunction2 = DaimlerCodes.getFunctionParser(codeString2Reduced);

            /**
             * Beide Varianten bzgl. Logik vergleichen
             */
            if (booleanFunction1.calculate() == booleanFunction2.calculate()) {
                return codeStringReduced;
            } else {
                throw new DaimlerCodesException("!!Sicheres Entfernen von Codes führte zu Validierungsfehler");
            }
        } catch (BooleanFunctionSyntaxException e) {
            throw new DaimlerCodesException(e.getMessage());
        }
    }


    /**
     * Den Original-Daimlercode als disjunktive Normalform im Format von Daimler. Hier wird extra das Daimlerformat für den Cache verwendet, da das wandeln in ein BooleanFunction recht schnell geht
     * und die BooleanFunction auch nur gecloned aus dem Cache entnommen werden dürfte. Clone und  DaimlerCodes.getFunctionParser dürfte einen ziemlich identischen Zeitaufand haben.
     * Für den Cachekey müsste eh eine Stringrepresentaion der BooleanFunction gemacht werden ->
     * Deshalb der einfache Cache String zu String
     * <br/><b>Die zurückgelieferte {@link Disjunction} darf auf keinen Fall verändert oder Boolean-Werte darin gesetzt
     * werden, weil anaonsten der Cache verändert wird!</b>
     *
     * @param code
     * @return
     * @throws BooleanFunctionSyntaxException
     */
    public static Disjunction getDnfCodeOriginal(String code) throws BooleanFunctionSyntaxException {
        Disjunction result;
        synchronized (cachedDnfs) {
            result = cachedDnfs.get(code);
        }
        if (result == null) {
            BooleanFunction parser = DaimlerCodes.getFunctionParser(code);

            // Die Konvertierung in die disjunktive Normalform findet nur noch dann statt, wenn sich Klammern in der Code-Regel
            // befinden (ohne Klammern handelt es sich zumindest um eine simple Disjunktion), um das Scoring nicht negativ
            // zu beeinflussen -> auch bei der Code-Erklärung in der RelatedInfo macht das Sinn, weil diese bei solchen
            // einfachen Disjunktionen der sichtbaren Code-Regel entspricht.
            if (code.contains(BooleanOperator.BRACKET_OPEN.asString()) || code.contains(BooleanOperator.BRACKET_CLOSE.asString())) {
                result = parser.convertToDisjunctiveNormalForm(iPartsPlugin.getThresholdForDNFSimplification());
            } else {
                BooleanFunctionTermTreeAnalyzer termTreeAnalyzer = new BooleanFunctionTermTreeAnalyzer(parser);
                Disjunction terms = termTreeAnalyzer.getTerms();
                termTreeAnalyzer.removeDuplicates(terms); // Gleiche Teilkonjunktionen entfernen
                result = terms;
            }

            synchronized (cachedDnfs) {
                cachedDnfs.put(code, result);
            }
        }
        return result;
    }

    /**
     * Ein Klon vom Daimlercode als disjunktive Normalform im Format von Daimler. Hier wird extra das Daimlerformat für den Cache verwendet, da das wandeln in ein BooleanFunction recht schnell geht
     * und die BooleanFunction auch nur gecloned aus dem Cache entnommen werden dürfte. Clone und  DaimlerCodes.getFunctionParser dürfte einen ziemlich identischen Zeitaufand haben.
     * Für den Cachekey müsste eh eine Stringrepresentaion der BooleanFunction gemacht werden ->
     * Deshalb der einfache Cache String zu String
     *
     * @param code
     * @return
     * @throws BooleanFunctionSyntaxException
     */
    public static Disjunction getDnfCode(String code) throws BooleanFunctionSyntaxException {
        return getDnfCodeOriginal(code).cloneMe();
    }

    /**
     * Helperfunktion, die gleich die BooleanFunction zurückliefert mit der Original-{@link Disjunction}.
     * <br/><b>Die zurückgelieferte {@link BooleanFunction} darf auf keinen Fall verändert oder Boolean-Werte darin gesetzt
     * werden, weil anaonsten der DNF-Cache verändert wird!</b>
     *
     * @param code
     * @return
     * @throws BooleanFunctionSyntaxException
     */
    public static BooleanFunction getDnfCodeFunctionOriginal(String code) throws BooleanFunctionSyntaxException {
        return new BooleanFunction(getDnfCodeOriginal(code));
    }

    /**
     * Vergleicht, ob die beiden Code-Regeln identisch sind nach Entfernen von semantisch irrelevanten Formatierungen.
     *
     * @param codeString1
     * @param codeString2
     * @return
     */
    public static boolean equalsCodeString(String codeString1, String codeString2) {
        if (codeString1 == null) {
            codeString1 = "";
        }
        if (codeString2 == null) {
            codeString2 = "";
        }

        // Check auf identischen Code-Regel-Text ohne Leerzeichen, mit simplem Minuszeichen und ohne Strichpunkt am Ende
        codeString1 = StrUtils.removeLastCharacterIfCharacterIs(codeString1.replace(" ", "").replace("++", "+").replace("+-", "-"), ';');
        codeString2 = StrUtils.removeLastCharacterIfCharacterIs(codeString2.replace(" ", "").replace("++", "+").replace("+-", "-"), ';');
        return codeString1.equals(codeString2);
    }

    public static String addEventsCodes(String codeString, iPartsEvent eventFrom, iPartsEvent eventTo, EtkProject project) {
        int ordinalFrom = -1;
        int ordinalTo = -1;
        Collection<iPartsEvent> events = null;
        if (eventFrom != null) {
            iPartsSeriesId seriesId = eventFrom.getSeriesId();
            iPartsDialogSeries series = iPartsDialogSeries.getInstance(project, seriesId);
            if (series.isEventTriggered()) {
                events = series.getEventsMap().values();

                ordinalFrom = eventFrom.getOrdinal();
                if (eventTo != null) {
                    if (!Utils.objectEquals(seriesId, eventTo.getSeriesId())) {
                        Logger.getLogger().throwRuntimeException("Series number \"" + seriesId.getSeriesNumber() + "\" of eventFrom is different from series number \""
                                                                 + eventTo.getSeriesId().getSeriesNumber() + "\" of eventTo for code rule \""
                                                                 + codeString + "\"");
                    }
                    ordinalTo = eventTo.getOrdinal(); // Ereignis-bis ist exklusiv
                } else {
                    ordinalTo = events.size(); // Ereignis-bis ist exklusiv
                }
            }
        } else if (eventTo != null) {
            iPartsSeriesId seriesId = eventTo.getSeriesId();
            iPartsDialogSeries series = iPartsDialogSeries.getInstance(project, seriesId);
            if (series.isEventTriggered()) {
                events = series.getEventsMap().values();
                ordinalFrom = 0;
                ordinalTo = eventTo.getOrdinal(); // Ereignis-bis ist exklusiv
            }
        }

        // Mindestens ein Ereignis vorhanden?
        if (events != null) {
            if (ordinalTo <= ordinalFrom) { // Ereignis-bis muss echt größer sein als Ereignis-ab (weil exklusiv)
                return codeString;
            }

            // Strichpunkte am Ende von der getrimmten Code-Regel entfernen
            String codeStringWithoutSemicolons = StrUtils.removeAllLastCharacterIfCharacterIs(codeString.trim(), ";").trim();
            StringBuilder completeCodeString = new StringBuilder(codeStringWithoutSemicolons);

            boolean codeStringEmpty = codeStringWithoutSemicolons.isEmpty();
            if (!codeStringEmpty) {
                // Herausfinden, ob die gesamte Code-Regel geklammert ist
                boolean needsBrackets = true;
                if (codeStringWithoutSemicolons.startsWith("(") && codeStringWithoutSemicolons.endsWith(")")) {
                    needsBrackets = false;
                    int openingBracketsCounter = 0;
                    for (int i = 1; i < codeStringWithoutSemicolons.length() - 1; i++) { // Klammern vorne und hinten nicht berücksichtigen
                        char currentChar = codeStringWithoutSemicolons.charAt(i);
                        if (currentChar == '(') {
                            openingBracketsCounter++;
                        } else if (currentChar == ')') {
                            if (openingBracketsCounter > 0) {
                                openingBracketsCounter--;
                            } else {
                                needsBrackets = true;
                                break;
                            }
                        }
                    }
                }

                // Klammern hinzufügen falls notwendig
                if (needsBrackets) {
                    completeCodeString.insert(0, '(');
                    completeCodeString.append(')');
                }

                completeCodeString.append('+');
            }

            StringBuilder eventsCodeString = new StringBuilder();
            if (!codeStringEmpty) {
                eventsCodeString.append('(');
            }

            // Code-Regeln von den Ereignissen mit ordinalFrom <= eventOrdinal < ordinalTo hinzufügen
            for (iPartsEvent event : events) {
                if ((event.getOrdinal() >= ordinalFrom) && (event.getOrdinal() < ordinalTo)) { // Ereignis-bis ist exklusiv
                    if (eventsCodeString.length() > 1) { // Klammer auf am Anfang berücksichtigen
                        eventsCodeString.append("/");
                    }
                    eventsCodeString.append(StrUtils.removeAllLastCharacterIfCharacterIs(event.getCode(), ";").trim());
                }
            }

            if (!codeStringEmpty) {
                eventsCodeString.append(");");

                // Gesamte Code-Regel zusammensetzen
                completeCodeString.append(eventsCodeString);
                return completeCodeString.toString();
            } else {
                eventsCodeString.append(';');
                return eventsCodeString.toString();
            }
        } else { // Weder Ereignis-ab noch Ereignis-bis sind vorhanden
            return codeString;
        }
    }
}
