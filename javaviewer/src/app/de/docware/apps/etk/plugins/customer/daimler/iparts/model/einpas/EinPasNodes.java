/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.AbstractiPartsNodes;

/**
 * Sortierte Map von Schlüsseln auf {@link EinPasNode}s.
 */
public class EinPasNodes extends AbstractiPartsNodes<EinPasNode, EinPasType> {

    @Override
    protected EinPasNode createNewNode(EinPasType type, String key, EinPasNode parent) {
        return new EinPasNode(type, key, parent);
    }
}
