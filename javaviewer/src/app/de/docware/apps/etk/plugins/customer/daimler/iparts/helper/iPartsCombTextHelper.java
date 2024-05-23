/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsCombTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataCombText;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.config.db.DBProject;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Helferklasse mit Methoden um kombinierte Texte verarbeiten zu können
 */
public class iPartsCombTextHelper implements iPartsConst {

    public static final String NEUTRAL_TEXT_FROM_PART_DELIMITER = ";";

    public static void sortCombTextByTextSeqNo(List<iPartsDataCombText> combTextList) {
        // Nach ihrer Reihenfolge sortieren
        Collections.sort(combTextList, new Comparator<iPartsDataCombText>() {
            @Override
            public int compare(iPartsDataCombText o1, iPartsDataCombText o2) {
                return o1.getAsId().getTextSeqNo().compareTo(o2.getAsId().getTextSeqNo());
            }
        });
    }

    public static Map<String, String> createCombTextToSeqNoMapWithMultiLangMap(Map<String, List<EtkMultiSprache>> map, DBProject project) {
        return createCombTextToSeqNoMap(map, null, true, project);
    }

    public static Map<String, String> createCombTextToSeqNoMap(Map<String, List<EtkMultiSprache>> map,
                                                               Map<String, String> neutralTextsFromPartForModule,
                                                               boolean handleMultiLangMap, DBProject project) {
        Map<String, String> combTextMap = new HashMap<>();
        String language = project.getDBLanguage();
        for (Map.Entry<String, List<EtkMultiSprache>> entry : map.entrySet()) {
            String seqNo = entry.getKey();
            List<EtkMultiSprache> tokens = entry.getValue();
            List<String> texts = new DwList<>(tokens.size());
            for (EtkMultiSprache multi : tokens) {
                String additionalText = multi.getText(language);
                texts.add(additionalText);
            }

            // Nur am Ende den sprachneutralen Text vom Material hinzufügen, wenn er nicht mit dem letzten Texteintrag vom
            // Stücklisteneintrag übereinstimmt
            // dazu wird der letzte Additional Text in DE mit dem neutralText aus Mat verglichen
            if (neutralTextsFromPartForModule != null) {
                String neutralTextFromPart = neutralTextsFromPartForModule.get(seqNo);
                if (StrUtils.isValid(neutralTextFromPart)) {
                    if (!tokens.isEmpty()) {
                        int lastIndex = tokens.size() - 1;
                        String lastAdditionalText = tokens.get(lastIndex).getText(Language.DE.getCode());
                        if (!neutralTextFromPart.equals(lastAdditionalText)) {
                            int lastTextIndex = texts.size() - 1;
                            String lastText = texts.get(lastTextIndex);
                            texts.set(lastIndex, lastText + NEUTRAL_TEXT_FROM_PART_DELIMITER); // Strichpunkt als Trenner zum sprachneutralen Text vom Material
                            texts.add(neutralTextFromPart); // Sprachneutralen Text vom Material hinzufügen
                        } else {
                            if (handleMultiLangMap) {
                                // letzter Kombinierter Text und sprachneutraler Text vom Material sind gleich =>
                                // entferne letzten Kombinierter Text
                                tokens.remove(lastIndex);
                            }
                        }
                    }
                }
            }

            String combText = stringListToStringWithoutEmpty(texts, " ").trim();
            combTextMap.put(seqNo, combText);
        }
        addPureNeutralTexts(neutralTextsFromPartForModule, combTextMap);
        return combTextMap;
    }

    private static String stringListToStringWithoutEmpty(Collection<String> list, String delimiter) {
        boolean firstEntry = true;
        StringBuilder sb = new StringBuilder();
        for (String string : list) {
            boolean isEmptyString = string.isEmpty();
            if (firstEntry) {
                firstEntry = false;
            } else {
                if (!isEmptyString) {
                    sb.append(delimiter);
                }
            }
            if (!isEmptyString) {
                sb.append(string);
            }
        }
        return sb.toString();
    }

    private static void addPureNeutralTexts(Map<String, String> neutralTextsFromPartForModule, Map<String, String> combTextMap) {
        // Sprachneutralen Text vom Material zurückliefern falls es keinen kombinierten Text gibt
        if ((neutralTextsFromPartForModule != null) && (combTextMap != null)) {
            for (Map.Entry<String, String> neutralTextFromPartEntry : neutralTextsFromPartForModule.entrySet()) {
                String seqNo = neutralTextFromPartEntry.getKey();
                String neutralTextFromPart = neutralTextFromPartEntry.getValue();
                if (StrUtils.isValid(neutralTextFromPart) && !combTextMap.containsKey(seqNo)) {
                    combTextMap.put(seqNo, neutralTextFromPart);
                }
            }
        }
    }

    /**
     * Aktualisiert und Löscht aktuelle {@link iPartsDataCombText} Objekte an einer Stücklistenpositiion. zusätzlich
     * werden neue Texte anelegt, wenn neue benötigt werden. Hier wird vorausgesetzt, dass die Reihenfolge der Objekte
     * in Form der Text-Positionsnummern schon festegelegt wurde.
     *
     * @param project
     * @param partListEntryId
     * @param textToSeqNumber
     * @param originalCombTextList
     * @param resultDataCombTextList
     */
    public static void handleCombTextsWithOrder(final EtkProject project, PartListEntryId partListEntryId, Map<String, EtkMultiSprache> textToSeqNumber,
                                                List<iPartsDataCombText> originalCombTextList, EtkDataObjectList resultDataCombTextList) {

        // Durchlaufe alle bestehenden Text-Objekte und befülle sie mit den dazugehörigen Texten
        for (final iPartsDataCombText originalCombText : originalCombTextList) {
            String existingTextSeqNo = originalCombText.getAsId().getTextSeqNo();
            // Text zur Position bestimmen
            EtkMultiSprache textForSeqNo = textToSeqNumber.remove(existingTextSeqNo);
            // Zur Position gibt es keinen Text, d.h. es gab mehr Ausgangsobjekte als jetzt angezeigt werden
            // -> Objekte löschen
            if (textForSeqNo == null) {
                project.getRevisionsHelper().executeWithoutActiveChangeSets(new Runnable() {
                    @Override
                    public void run() {
                        iPartsCombTextId combTextId = originalCombText.getAsId();
                        iPartsDataCombText combText = new iPartsDataCombText(project, combTextId);
                        if (combText.existsInDB()) {
                            originalCombText.setFieldValueAsMultiLanguage(FIELD_DCT_DICT_TEXT, combText.getFieldValueAsMultiLanguage(FIELD_DCT_DICT_TEXT), DBActionOrigin.FROM_EDIT);
                        }
                    }
                }, false, project);
                resultDataCombTextList.delete(originalCombText, true, DBActionOrigin.FROM_EDIT);
                continue;
            }
            EtkMultiSprache existingText = originalCombText.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DCT_DICT_TEXT);
            // Check, ob neuer Text andere Text-ID hat
            if (!textForSeqNo.getTextId().equals(existingText.getTextId())) {
                originalCombText.setFieldValueAsMultiLanguage(iPartsConst.FIELD_DCT_DICT_TEXT, textForSeqNo,
                                                              DBActionOrigin.FROM_EDIT);
            }
            resultDataCombTextList.add(originalCombText, DBActionOrigin.FROM_EDIT);
        }

        // Wenn mehr Texte angezeitg werden als Ausgangstexte vorhanden waren, müssen diese hier erzeugt werden
        if (!textToSeqNumber.isEmpty()) {
            // Füge alle neuen Texte hinzu und entferne gleiche Texteinträge mit fleicher Positionsnummer aus den existierenden Texten
            for (Map.Entry<String, EtkMultiSprache> newCombText : textToSeqNumber.entrySet()) {
                iPartsCombTextId combTextId = new iPartsCombTextId(partListEntryId, newCombText.getKey());
                iPartsDataCombText combText = new iPartsDataCombText(project, combTextId);
                if (!combText.existsInDB()) {
                    combText.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                }
                combText.setFieldValueAsMultiLanguage(iPartsConst.FIELD_DCT_DICT_TEXT, newCombText.getValue(), DBActionOrigin.FROM_EDIT);
                resultDataCombTextList.add(combText, DBActionOrigin.FROM_EDIT);
            }
        }
    }

    public static void extractAddTextAndNeutralTextWithoutExistingCache(DBProject project, String kLfdnr, List<EtkMultiSprache> combinedMultiTextList,
                                                                        VarParam<String> addText, VarParam<String> neutralText
    ) {
        extractAddTextAndNeutralText(project, kLfdnr, combinedMultiTextList, null, addText, neutralText, null, null);
    }

    public static void extractAddTextAndNeutralText(DBProject project, String kLfdnr, List<EtkMultiSprache> combinedMultiTextList,
                                                    DictTextCache neutralCache, VarParam<String> addText, VarParam<String> neutralText,
                                                    Set<String> alreadyFoundAddTexts, Set<String> alreadyFoundNeutralTexts) {
        if (!Utils.isValid(combinedMultiTextList)) {
            return;
        }

        if (neutralCache == null) {
            neutralCache = DictTextCache.getInstanceWithAllTextStates(DictTextKindTypes.NEUTRAL_TEXT, project.getDBLanguage());
        }
        List<EtkMultiSprache> addTexts = new DwList<>();
        List<EtkMultiSprache> neutralTexts = new DwList<>();
        for (EtkMultiSprache etkMultiSprache : combinedMultiTextList) {
            // Finde ich einen Text über seine Text-ID im Cache für sprachneutrale Texte, dann ist es ein neutraler Text.
            // Ansonsten ein Ergänzungstext. Außerdem werden zwei zusätzliche Caches verwendet für identische Text-IDs in
            // einer Stückliste.
            String textId = etkMultiSprache.getTextId();
            if (isAlreadyFoundText(textId, etkMultiSprache, alreadyFoundAddTexts, addTexts)
                || isAlreadyFoundText(textId, etkMultiSprache, alreadyFoundNeutralTexts, neutralTexts)) {
                continue;
            }

            String text = etkMultiSprache.getText(project.getDBLanguage());
            Map<String, String> foundEntries = neutralCache.searchTexts(text);
            if ((foundEntries == null) || foundEntries.isEmpty() || foundEntries.values().stream().noneMatch(value -> value.equals(textId))) {
                addTexts.add(etkMultiSprache);
                if (alreadyFoundAddTexts != null) {
                    alreadyFoundAddTexts.add(textId);
                }
            } else {
                neutralTexts.add(etkMultiSprache);
                if (alreadyFoundNeutralTexts != null) {
                    alreadyFoundNeutralTexts.add(textId);
                }
            }
        }

        // Hier den Text auf Basis der Sequenznummern zusammenbauen lassen. Einmal für die Ergänzungstexte und
        // einmal für die neutralen Texte
        Map<String, List<EtkMultiSprache>> tempMap = new HashMap<>();
        if (!addTexts.isEmpty()) {
            tempMap.put(kLfdnr, addTexts);
            Map<String, String> createdText = iPartsCombTextHelper.createCombTextToSeqNoMapWithMultiLangMap(tempMap, project);
            addText.setValue(createdText.get(kLfdnr));
            tempMap.clear();
        } else {
            addText.setValue("");
        }
        if (!neutralTexts.isEmpty()) {
            tempMap.put(kLfdnr, neutralTexts);
            Map<String, String> createdText = iPartsCombTextHelper.createCombTextToSeqNoMapWithMultiLangMap(tempMap, project);
            neutralText.setValue(createdText.get(kLfdnr));
        } else {
            neutralText.setValue("");
        }
    }

    private static boolean isAlreadyFoundText(String key, EtkMultiSprache multiSprache, Set<String> alreadyFoundTextKeys,
                                              List<EtkMultiSprache> texts) {
        if ((alreadyFoundTextKeys != null) && alreadyFoundTextKeys.contains(key)) {
            texts.add(multiSprache);
            return true;
        }
        return false;
    }
}
