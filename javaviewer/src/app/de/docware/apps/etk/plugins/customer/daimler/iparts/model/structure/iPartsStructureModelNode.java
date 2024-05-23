/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure;

import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsNodeType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;

/**
 * Knoten in der Struktur, der ein (Konstruktions-)Baumuster enthält.
 */
public class iPartsStructureModelNode {

    protected iPartsModelId modelId;
    protected iPartsNodeType structureType;

    public iPartsStructureModelNode(iPartsModelId modelId, iPartsNodeType structureType) {
        this.modelId = modelId;
        this.structureType = structureType;
    }

    public iPartsModelId getModelId() {
        return modelId;
    }

    public iPartsNodeType getStructureType() {
        return structureType;
    }
}
