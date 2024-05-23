/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.proVal.models.modelswithaggregates;

import de.docware.apps.etk.plugins.customer.daimler.iparts.services.proVal.models.iPartsProValModelsServiceResponseObject;

import java.util.List;

/**
 * ProVal Baumuster inkl. aller Aggregate Referenzen
 */
public class iPartsProValModelWithAggs extends iPartsProValModelsServiceResponseObject {

    private List<iPartsProValAggDesignRef> aggregateDesignRefs;

    public iPartsProValModelWithAggs() {
    }

    public List<iPartsProValAggDesignRef> getAggregateDesignRefs() {
        return aggregateDesignRefs;
    }

    public void setAggregateDesignRefs(List<iPartsProValAggDesignRef> aggregateDesignRefs) {
        this.aggregateDesignRefs = aggregateDesignRefs;
    }
}

