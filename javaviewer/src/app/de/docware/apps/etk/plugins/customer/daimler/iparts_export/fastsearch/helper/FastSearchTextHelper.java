/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_export.fastsearch.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.fastsearch.transferobjects.ElasticSearchIndex;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.sql.TableAndFieldName;
import org.apache.commons.collections4.map.LRUMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Helfer zum Verarbeiten von mehrsprachigen Texten während der Erzeugung des ElasticSearch Index
 */
public class FastSearchTextHelper {

    // Eine LRUMAp um Text-Ids auf EtkMultiSprache Objekte zu halten (viele gleiche Texte in einer Stückliste
    // bzw. in einem TU bzw. in einer KG, usw.)
    private final LRUMap texts;
    private final EtkProject project;

    public FastSearchTextHelper(EtkProject project) {
        this.texts = new LRUMap(10000);
        this.project = project;
    }

    /**
     * Liefert den Text zur übergebenen Text-Id
     *
     * @param textId
     * @param tableAndFieldName
     * @return
     */
    public EtkMultiSprache getTextForId(String textId, String tableAndFieldName) {
        Object textObject = texts.get(textId);
        if (textObject instanceof EtkMultiSprache) {
            return (EtkMultiSprache)textObject;
        }

        // Wenn der Text nicht im Cache liegt, dann aus der DB laden und ablegen
        EtkMultiSprache text = project.getDbLayer().getLanguagesTextsByTextId(textId, tableAndFieldName);
        if (text == null) {
            text = new EtkMultiSprache();
        }
        texts.put(textId, text);
        return text;
    }

    /**
     * Liefert die Textbausteine des kombinierten Textes
     *
     * @param defaultPartIndex
     * @param additionalDescRefs
     * @param fallbackLanguages
     * @return
     */
    public List<EtkMultiSprache> handleAddTexts(ElasticSearchIndex defaultPartIndex, List<String> additionalDescRefs,
                                                List<String> fallbackLanguages) {
        List<EtkMultiSprache> additionalDescs = new ArrayList<>();
        if (Utils.isValid(additionalDescRefs)) {
            for (String additionalDescTextId : additionalDescRefs) {
                EtkMultiSprache addText = getTextForId(additionalDescTextId,
                                                       TableAndFieldName.make(iPartsConst.TABLE_DA_COMB_TEXT,
                                                                              iPartsConst.FIELD_DCT_DICT_TEXT));
                if (addText.isEmpty()) {
                    additionalDescs.clear();
                    break;
                }
                additionalDescs.add(addText);
            }
        }
        if (!additionalDescs.isEmpty()) {
            String deText = createAddTextForLanguage(additionalDescs, Language.DE.getCode(), fallbackLanguages);
            if (StrUtils.isValid(deText)) {
                defaultPartIndex.setPartdesc_de(deText);
            }
        } else {
            defaultPartIndex.setPartdesc(null);
            defaultPartIndex.setPartdesc_de(null);
        }
        return additionalDescs;
    }

    /**
     * Erzeugt den kombinierten Text für die übergebene Sprache
     *
     * @param addTexts
     * @param language
     * @param fallbackLanguages
     * @return
     */
    public String createAddTextForLanguage(List<EtkMultiSprache> addTexts, String language, List<String> fallbackLanguages) {
        StringBuilder builder = new StringBuilder();
        // Durchlaufe alle Teile der Ergänzungstexte und baue den Ergänzungstext zusammen.
        for (EtkMultiSprache addText : addTexts) {
            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append(addText.getTextByNearestLanguage(language, fallbackLanguages));
        }
        if (builder.length() > 0) {
            return builder.toString();
        }
        return null;
    }

    /**
     * Bestimmt den neutralen Text
     *
     * @param defaultPartIndex
     * @param materialDescRef
     * @return
     */
    public EtkMultiSprache handleMatDesc(ElasticSearchIndex defaultPartIndex, String materialDescRef) {
        EtkMultiSprache matDesc;
        if (StrUtils.isValid(materialDescRef)) {
            // Hier den neutralen Text laden.
            // Hier wird das EtkMultiSprach bestimmt, das alle Texte für alle Sprachen enthält. Gesetzt wird der Text
            // erst ganz zum Schluss, wenn der Datensatz pro Sprache erzeugt wird. Wobei das hier keine Rolle spielen
            // sollte, da der neutrale Text für alle Sprachen gleich ist
            matDesc = getTextForId(materialDescRef, TableAndFieldName.make(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_ADDTEXT));
            if (!matDesc.isEmpty()) {
                // Der deutsche Text für die Meta-Suche kann hier schon gesetzt werden
                defaultPartIndex.setMatdesc_de(matDesc.getText(Language.DE.getCode()));
                return matDesc;
            }
        }
        defaultPartIndex.setMatdesc(null);
        defaultPartIndex.setMatdesc_de(null);
        return null;
    }

    /**
     * Liefert die typische Darstellung der modelId für die FastSearch Einträge analog zur originalen Methode in iparts.js
     *
     * @param modelId
     * @param productNumber
     * @return
     */
    public static String getFastSearchModelId(String modelId, String productNumber) {
        if (StrUtils.isValid(modelId, productNumber)) {
            // Baumuster nach Vorgabe in iparts.js
            String cleanModelId = getCleanModelNo(modelId);
            // Wert für modelid bestimmen (iparts.js)
            return cleanModelId + "_" + productNumber.replace("\r", "");
        }
        return null;
    }

    /**
     * Methode zum Entfernen des Prefix bei Baumuster analog zur originalen Methode in iparts.js bzw. utils.js
     *
     * @param modelId
     * @return
     */
    public static String getCleanModelNo(String modelId) {
        if (modelId.startsWith(iPartsConst.MODEL_NUMBER_PREFIX_CAR) || modelId.startsWith(iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE)) {
            return modelId.substring(1);
        }
        return modelId;
    }
}
