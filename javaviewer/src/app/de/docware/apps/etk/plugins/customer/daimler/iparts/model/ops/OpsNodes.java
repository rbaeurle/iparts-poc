/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.ops;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.AbstractiPartsNodes;

/**
 * Sortierte Map von Schlüsseln auf {@link OpsNode}s.
 */
public class OpsNodes extends AbstractiPartsNodes<OpsNode, OpsType> {

    @Override
    protected OpsNode createNewNode(OpsType type, String key, OpsNode parent) {
        return new OpsNode(type, key, parent);
    }
}
