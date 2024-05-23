/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.ops;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.AbstractiPartsNode;

/**
 * Ein Eds Ops-Knoten mit den Bezeichnungen und sonstigen zukünfigen Attributen.
 */
public class OpsNode extends AbstractiPartsNode<OpsNodes, OpsNode, OpsType> {

    public OpsNode(OpsType type, String number, OpsNode parent) {
        super(type, number, parent, new OpsNodes());
    }

    @Override
    public OpsId getId() {
        if (getType() == OpsType.SCOPE) {
            return new OpsId(getParent().getNumber(), getNumber());
        } else {
            return new OpsId(getNumber(), "");
        }
    }
}
