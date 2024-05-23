/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.model.modelelementusage;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.AbstractiPartsNodes;

/**
 * Sortierte Map von Schlüsseln auf {@link ModelElementUsageNode}s.
 */
public class ModelElementUsageNodes extends AbstractiPartsNodes<ModelElementUsageNode, ModelElementUsageType> {

    @Override
    protected ModelElementUsageNode createNewNode(ModelElementUsageType type, String key, ModelElementUsageNode parent) {
        return new ModelElementUsageNode(type, key, parent);
    }
}
