/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;

/**
 * Liste von {@link iPartsDataHmoSaaMapping}.
 */
public class iPartsDataHmoSaaMappingList extends EtkDataObjectList<iPartsDataHmoSaaMapping> implements iPartsConst {

    @Override
    protected iPartsDataHmoSaaMapping getNewDataObject(EtkProject project) {
        return new iPartsDataHmoSaaMapping(project, null);
    }
}
