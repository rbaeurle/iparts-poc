package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.transit;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

public class iPartsDataTransitLangMappingList extends EtkDataObjectList<iPartsDataTransitLangMapping> implements iPartsConst {

    public iPartsDataTransitLangMappingList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Liefert alle {@link iPartsDataTransitLangMapping}s aus der Tabelle DA_TRANSIT_LANG_MAPPING
     * sortiert nach FIELD_DA_TLM_TRANSIT_LANGUAGE.
     *
     * @param project
     * @return
     */
    public static iPartsDataTransitLangMappingList getAllMapping(EtkProject project) {
        iPartsDataTransitLangMappingList list = new iPartsDataTransitLangMappingList();
        list.loadAllMappingDataFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadAllMappingDataFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        String[] sortFields = new String[]{ FIELD_DA_TLM_TRANSIT_LANGUAGE };
        searchSortAndFill(project, TABLE_DA_TRANSIT_LANG_MAPPING, null, null, sortFields,
                          LoadType.COMPLETE, true, origin);
    }

    @Override
    protected iPartsDataTransitLangMapping getNewDataObject(EtkProject project) {
        return new iPartsDataTransitLangMapping(project, null);
    }
}
