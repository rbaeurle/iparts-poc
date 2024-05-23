/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_export.fastsearch.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Meta-Informationen zum Index (Index Kenner)
 */
public class ElasticSearchIndexMeta implements RESTfulTransferObjectInterface {

    private String _index;

    public ElasticSearchIndexMeta() {
    }

    public String get_index() {
        return _index;
    }

    public void set_index(String _index) {
        this._index = _index;
    }
}

