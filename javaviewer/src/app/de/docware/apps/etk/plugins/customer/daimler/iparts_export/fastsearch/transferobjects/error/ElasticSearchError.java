/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_export.fastsearch.transferobjects.error;

import java.util.List;

/**
 * Fehler, die ElasticSearch als JSON zurückliefern kann
 */
public class ElasticSearchError extends ElasticSearchErrorRootCause {

    private List<ElasticSearchErrorRootCause> rootCause;

    public ElasticSearchError() {
    }

    public List<ElasticSearchErrorRootCause> getRootCause() {
        return rootCause;
    }

    public void setRootCause(List<ElasticSearchErrorRootCause> rootCause) {
        this.rootCause = rootCause;
    }

}
