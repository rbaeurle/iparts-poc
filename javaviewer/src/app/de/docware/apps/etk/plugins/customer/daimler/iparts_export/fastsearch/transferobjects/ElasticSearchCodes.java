/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_export.fastsearch.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.Set;

/**
 * Code für den ElasticSearch Teil-Index
 */
public class ElasticSearchCodes implements RESTfulTransferObjectInterface {

    private Set<String> must;
    private Set<String> not;

    public ElasticSearchCodes() {
    }

    public Set<String> getMust() {
        return must;
    }

    public void setMust(Set<String> must) {
        this.must = must;
    }

    public Set<String> getNot() {
        return not;
    }

    public void setNot(Set<String> not) {
        this.not = not;
    }
}
