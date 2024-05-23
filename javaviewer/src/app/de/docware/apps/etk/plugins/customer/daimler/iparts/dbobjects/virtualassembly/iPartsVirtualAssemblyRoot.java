/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsStructure;
import de.docware.framework.modules.db.DBDataObjectList;

import java.util.List;

/**
 * Virtuelle Siche auf den Rootknoten
 */

public class iPartsVirtualAssemblyRoot extends iPartsVirtualAssemblyStructureBase {

    public iPartsVirtualAssemblyRoot(EtkProject project, List<iPartsVirtualNode> virtualIds, iPartsAssemblyId assemblyId) {
        super(project, virtualIds, assemblyId);
    }

    @Override
    public DBDataObjectList<EtkDataPartListEntry> loadPartList(boolean subAssembliesOnly, EtkDisplayFields fields) {
        // Erste Ebene der Struktur
        iPartsStructure structure = iPartsStructure.getInstance(getEtkProject());
        return loadVirtualStructureNode(structure.getChildren(), structure.getProductList(), structure.getSeriesList(), structure.getModelList());
    }

    @Override
    public SubAssemblyState getSubAssemblyState() {
        return SubAssemblyState.HAS_ALWAYS;
    }

    @Override
    public String getPartsListType() {
        return PARTS_LIST_TYPE_STRUCTURE;
    }

    @Override
    public void getParentAssemblyEntries(boolean filtered, List<EtkDataPartListEntry> result) {
        // Root hat keine ParentAssemblyEntries
    }
}
