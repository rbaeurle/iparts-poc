package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataAssortmentClassesMapping}.
 */
public class iPartsDataAssortmentClassesMappingList extends EtkDataObjectList<iPartsDataAssortmentClassesMapping> implements iPartsConst {

    public void iPartsDataAssortmentClassesMappingList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataAssortmentClassesMapping}s.
     *
     * @param project
     * @return
     */
    public static iPartsDataAssortmentClassesMappingList loadAllAcPcMappings(EtkProject project) {
        iPartsDataAssortmentClassesMappingList list = new iPartsDataAssortmentClassesMappingList();
        list.loadAllAcPcMappingsFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataAssortmentClassesMapping}s.
     *
     * @param project
     * @param origin
     */
    private void loadAllAcPcMappingsFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_AC_PC_MAPPING, null, null, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataAssortmentClassesMapping getNewDataObject(EtkProject project) {
        return new iPartsDataAssortmentClassesMapping(project, null);
    }
}
