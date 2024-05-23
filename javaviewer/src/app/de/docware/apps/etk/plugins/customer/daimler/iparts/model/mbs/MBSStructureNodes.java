package de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.AbstractiPartsNodes;

public class MBSStructureNodes extends AbstractiPartsNodes<MBSStructureNode, MBSStructureType> {

    @Override
    protected MBSStructureNode createNewNode(MBSStructureType type, String key, MBSStructureNode parent) {
        return new MBSStructureNode(type, key, parent);
    }


}
