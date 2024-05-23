/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.model.modelelementusage;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.AbstractiPartsNode;

/**
 * Ein EDS/BCS Konstruktionsknoten mit den Bezeichnungen und sonstigen zukünftigen Attributen.
 */
public class ModelElementUsageNode extends AbstractiPartsNode<ModelElementUsageNodes, ModelElementUsageNode, ModelElementUsageType> {

    public ModelElementUsageNode(ModelElementUsageType type, String number, ModelElementUsageNode parent) {
        super(type, number, parent, new ModelElementUsageNodes());
    }

    @Override
    public ModelElementUsageId getId() {
        if (getType() == ModelElementUsageType.SUB_MODULE) {
            return new ModelElementUsageId(getParent().getNumber(), getNumber());
        } else {
            return new ModelElementUsageId(getNumber(), "");
        }
    }
}
