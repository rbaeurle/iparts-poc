/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.AbstractiPartsNode;

/**
 * Ein EinPAS-Knoten mit den Bezeichnungen.
 */
public class EinPasNode extends AbstractiPartsNode<EinPasNodes, EinPasNode, EinPasType> {

    public EinPasNode(EinPasType type, String number, EinPasNode parent) {
        super(type, number, parent, new EinPasNodes());
    }

    @Override
    public EinPasId getId() {
        if (getType() == EinPasType.TU) {
            return new EinPasId(getParent().getParent().getNumber(), getParent().getNumber(), getNumber());
        } else if (getType() == EinPasType.G) {
            return new EinPasId(getParent().getNumber(), getNumber(), "");
        } else {
            return new EinPasId(getNumber(), "", "");
        }
    }
}
