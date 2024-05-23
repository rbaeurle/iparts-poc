/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntryList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsStructure;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsStructureId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsStructureNode;
import de.docware.framework.modules.db.DBDataObjectList;

import java.util.List;

/**
 * Virtuelle Stückliste für einen einfachen Strukturknoten
 */
public class iPartsVirtualAssemblyStructure extends iPartsVirtualAssemblyStructureBase {

    public iPartsVirtualAssemblyStructure(EtkProject project, List<iPartsVirtualNode> virtualIds, iPartsAssemblyId assemblyId) {
        super(project, virtualIds, assemblyId);
    }

    @Override
    public DBDataObjectList<EtkDataPartListEntry> loadPartList(boolean subAssembliesOnly, EtkDisplayFields fields) {
        return loadVirtualStructureNode((iPartsStructureId)getRootNode().getId());
    }

    private DBDataObjectList<EtkDataPartListEntry> loadVirtualStructureNode(iPartsStructureId structureId) {
        // Erste Ebene der Struktur
        iPartsStructure structure = iPartsStructure.getInstance(getEtkProject());
        iPartsStructureNode node = structure.findNodeInAllChilds(structureId);
        if (node != null) {
            return loadVirtualStructureNode(node.getChildren(), node.getProductList(), node.getSeriesList(), node.getModelList());
        } else {
            // Leere Liste zurück
            return new EtkDataPartListEntryList();
        }
    }
}
