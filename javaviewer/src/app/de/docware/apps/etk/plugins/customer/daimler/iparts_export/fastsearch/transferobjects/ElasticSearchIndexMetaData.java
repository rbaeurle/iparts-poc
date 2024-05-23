/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_export.fastsearch.transferobjects;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.util.StrUtils;

/**
 * JSON Objekt für die Meta-Informationen zwischen einem Teil-Index-JSON
 */
public class ElasticSearchIndexMetaData implements RESTfulTransferObjectInterface {

    private static final String PREFIX_VALUE = "parts_";
    private static final String NAME_SUFFIX = "_temp";

    private ElasticSearchIndexMeta index;

    public ElasticSearchIndexMetaData() {
    }

    public ElasticSearchIndexMeta getIndex() {
        return index;
    }

    public void setIndex(ElasticSearchIndexMeta index) {
        this.index = index;
    }

    @JsonIgnore
    public static ElasticSearchIndexMetaData getMetaForLanguage(String language, boolean addSuffixToIndexName) {
        if (StrUtils.isValid(language)) {
            ElasticSearchIndexMeta elasticSearchIndexMeta = new ElasticSearchIndexMeta();
            elasticSearchIndexMeta.set_index(createIndexName(language, addSuffixToIndexName));
            ElasticSearchIndexMetaData elasticSearchIndexMetaData = new ElasticSearchIndexMetaData();
            elasticSearchIndexMetaData.setIndex(elasticSearchIndexMeta);
            return elasticSearchIndexMetaData;
        }
        return null;
    }

    @JsonIgnore
    public static String createIndexName(String language, boolean addSuffixToIndexName) {
        String name = PREFIX_VALUE + language.toLowerCase();
        if (addSuffixToIndexName) {
            name += NAME_SUFFIX;
        }
        return name;
    }

}
