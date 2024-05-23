/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.AbstractiPartsNodes;

/**
 * Sortierte Map von Schlüsseln auf {@link HmMSmNode}s.
 */
public class HmMSmNodes extends AbstractiPartsNodes<HmMSmNode, HmMSmType> {

    @Override
    protected HmMSmNode createNewNode(HmMSmType type, String key, HmMSmNode parent) {
        return new HmMSmNode(type, key, parent);
    }
}
