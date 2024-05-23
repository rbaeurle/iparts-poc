/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure;

import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsNodeType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;

/**
 * Knoten in der Struktur, der eine (Konstruktions-)Baureihe enthält.
 */
public class iPartsStructureSeriesNode {

    protected iPartsSeriesId seriesId;
    protected iPartsNodeType structureType;

    public iPartsStructureSeriesNode(iPartsSeriesId seriesId, iPartsNodeType structureType) {
        this.seriesId = seriesId;
        this.structureType = structureType;
    }

    public iPartsSeriesId getSeriesId() {
        return seriesId;
    }

    public iPartsNodeType getStructureType() {
        return structureType;
    }
}
