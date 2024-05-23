/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.SearchMatchHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.AbstractiPartsNodes;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Sortierte Map von Schlüsseln auf {@link KgTuNode}s.
 */

public class KgTuNodes extends AbstractiPartsNodes<KgTuNode, KgTuType> implements EtkDbConst {

    @Override
    protected KgTuNode createNewNode(KgTuType type, String key, KgTuNode parent) {
        return new KgTuNode(type, key, parent);
    }


    /**
     * Suche in allen Unterknoten nach einer Nummer und oder einem Text
     *
     * @param selectFields
     * @param selectValues
     * @param whereFields
     * @param andOrWhereValues
     * @param andOrSearch
     * @param wildCardSettings
     * @param language
     * @return
     */
    public List<KgTuNode> search(EtkDisplayFields selectFields,
                                 List<String> selectValues,
                                 EtkDisplayFields whereFields,
                                 List<String> andOrWhereValues,
                                 boolean andOrSearch,
                                 WildCardSettings wildCardSettings,
                                 String language,
                                 List<String> fallbackLanguages) {
        List<KgTuNode> result = new ArrayList<KgTuNode>();

        internSearch(result, selectFields, selectValues, whereFields, andOrWhereValues, andOrSearch, wildCardSettings, language, fallbackLanguages, getValues());

        return result;
    }

    private void internSearch(List<KgTuNode> result, EtkDisplayFields selectFields,
                              List<String> selectValues,
                              EtkDisplayFields whereFields,
                              List<String> andOrWhereValues,
                              boolean andOrSearch,
                              WildCardSettings wildCardSettings,
                              String language, List<String> fallbackLanguages, Collection<KgTuNode> nodes) {
        for (KgTuNode node : nodes) {
            Map<String, String> fieldsAndValues = new HashMap<String, String>();

            fieldsAndValues.put(TableAndFieldName.make(TABLE_MAT, FIELD_M_BESTNR), node.getNumber());
            fieldsAndValues.put(TableAndFieldName.make(TABLE_MAT, FIELD_M_TEXTNR), node.getTitle().getTextByNearestLanguage(language, fallbackLanguages));

            if (SearchMatchHelper.fieldsMatchSearchOptions(fieldsAndValues, andOrSearch, selectFields, selectValues, whereFields, andOrWhereValues, wildCardSettings)) {
                result.add(node);
            }

            internSearch(result, selectFields, selectValues, whereFields, andOrWhereValues, andOrSearch, wildCardSettings, language, fallbackLanguages, node.getChildren());
        }
    }

}
