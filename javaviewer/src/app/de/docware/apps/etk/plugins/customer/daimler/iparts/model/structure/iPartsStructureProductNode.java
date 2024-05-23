/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure;

import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsNodeType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;

/**
 * Knoten in der Struktur, der ein Produkt enthält.
 */
public class iPartsStructureProductNode {

    protected iPartsProductId productId;
    protected iPartsNodeType structureType;

    public iPartsStructureProductNode(iPartsProductId productId, iPartsNodeType structureType) {
        this.productId = productId;
        this.structureType = structureType;
    }

    public iPartsProductId getProductId() {
        return productId;
    }

    public iPartsNodeType getStructureType() {
        return structureType;
    }
}
