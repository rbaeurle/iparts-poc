/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;

/**
 * Liste von {@link iPartsDataWireHarnessSimplifiedPart}s für Leitungssatzbaukästen.
 */
public class iPartsDataWireHarnessSimplifiedPartList extends EtkDataObjectList<iPartsDataWireHarnessSimplifiedPart> implements iPartsConst {

    public iPartsDataWireHarnessSimplifiedPartList() {
        setSearchWithoutActiveChangeSets(true);
    }

    @Override
    protected iPartsDataWireHarnessSimplifiedPart getNewDataObject(EtkProject project) {
        return new iPartsDataWireHarnessSimplifiedPart(project, null);
    }
}
