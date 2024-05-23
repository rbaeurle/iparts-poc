/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImageList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.framework.modules.db.DBDataObjectAttributesList;

/**
 * Erweiterung von {@link EtkDataImageList} um iParts-spezifische Methoden und Daten.
 */
public class iPartsDataImageList extends EtkDataImageList {

    @Override
    public DBDataObjectAttributesList getImageAttributesList(EtkProject project, AssemblyId assemblyId) {
        return loadImagesAttributesList(project, assemblyId, true);
    }

    @Override
    public DBDataObjectAttributesList getUnfilteredImageAttributesList(EtkProject project, AssemblyId assemblyId) {
        return loadImagesAttributesList(project, assemblyId, false);
    }

    private DBDataObjectAttributesList loadImagesAttributesList(EtkProject project, AssemblyId assemblyId, boolean filtered) {
        if (iPartsVirtualNode.isVirtualId(assemblyId)) {
            // hier Zeichnungen von virtuellen Knoten bestimmen
            return DBDataObjectAttributesList.EMPTY_DB_DATA_OBJECT_ATTRIBUTES_LIST;
        } else {
            return filtered ? super.getImageAttributesList(project, assemblyId) : super.getUnfilteredImageAttributesList(project, assemblyId);
        }
    }

    @Override
    public void loadImages(EtkProject project, AssemblyId assemblyId, boolean filtered) {
        EtkRevisionsHelper revisionsHelper = project.getEtkDbs().getRevisionsHelper();
        if (revisionsHelper != null) {
            revisionsHelper.startPseudoTransactionForActiveChangeSet(project, true);
        }
        try {
            super.loadImages(project, assemblyId, filtered);
        } finally {
            if (revisionsHelper != null) {
                revisionsHelper.stopPseudoTransactionForActiveChangeSet(project);
            }
        }
    }
}
