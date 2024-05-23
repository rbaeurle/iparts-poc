/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualAssemblyFactory;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.framework.utils.EtkMultiSprache;

import java.util.List;

/**
 * Helper um die Bezeichnungen, die Nummern und evtl. noch andere Felder der virtuellen Knoten zu ermitteln
 */
public class iPartsDataVirtualFieldsHelper implements iPartsConst {


    public static iPartsAssemblyId getAssemblyIdFromVirtualKey(String virtualKey) {
        return new iPartsAssemblyId(virtualKey, "");
    }

    public static String getOrderNumberFromVirtualKey(String virtualKey, EtkProject project) {
        List<iPartsVirtualNode> nodes = iPartsVirtualNode.parseVirtualIds(virtualKey);
        if (!nodes.isEmpty()) {
            iPartsVirtualAssembly virtualAssembly = iPartsVirtualAssemblyFactory.createVirtualAssembly(project, nodes, getAssemblyIdFromVirtualKey(virtualKey));

            if (virtualAssembly != null) {
                return virtualAssembly.getOrderNumber();
            }
        }
        return "";
    }

    public static EtkMultiSprache getTextsFromVirtualKey(String virtualKey, EtkProject project) {
        List<iPartsVirtualNode> nodes = iPartsVirtualNode.parseVirtualIds(virtualKey);
        if (!nodes.isEmpty()) {
            iPartsVirtualAssembly virtualAssembly = iPartsVirtualAssemblyFactory.createVirtualAssembly(project, nodes, getAssemblyIdFromVirtualKey(virtualKey));

            if (virtualAssembly != null) {
                return virtualAssembly.getTexts();
            }
        }
        return null;
    }

    public static String getPictureNameFromVirtualKey(String virtualKey, EtkProject project) {
        List<iPartsVirtualNode> nodes = iPartsVirtualNode.parseVirtualIds(virtualKey);
        if (!nodes.isEmpty()) {
            iPartsVirtualAssembly virtualAssembly = iPartsVirtualAssemblyFactory.createVirtualAssembly(project, nodes, getAssemblyIdFromVirtualKey(virtualKey));

            if (virtualAssembly != null) {
                return virtualAssembly.getPictureName();
            }
        }
        return "";
    }
}

