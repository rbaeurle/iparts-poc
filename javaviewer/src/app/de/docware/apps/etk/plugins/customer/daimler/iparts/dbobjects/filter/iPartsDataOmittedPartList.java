/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributesList;

/**
 * Liste mit {@link iPartsDataOmittedPart} Objekten
 */
public class iPartsDataOmittedPartList extends EtkDataObjectList<iPartsDataOmittedPart> implements iPartsConst {

    public iPartsDataOmittedPartList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataOmittedPart}s.
     *
     * @param project
     * @return
     */
    public static iPartsDataOmittedPartList loadOmittedParts(EtkProject project) {
        iPartsDataOmittedPartList list = new iPartsDataOmittedPartList();
        list.loadData(project, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataOmittedPart)s.
     *
     * @param project
     * @param origin
     * @return
     */
    public void loadData(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        DBDataObjectAttributesList attributesList = project.getDbLayer().getAttributesList(TABLE_DA_OMITTED_PARTS);
        fillAndAddDataObjectsFromAttributesList(project, attributesList, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataOmittedPart getNewDataObject(EtkProject project) {
        return new iPartsDataOmittedPart(project, null);
    }
}
