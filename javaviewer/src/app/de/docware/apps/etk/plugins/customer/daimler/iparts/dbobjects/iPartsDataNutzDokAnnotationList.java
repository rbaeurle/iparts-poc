/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

public class iPartsDataNutzDokAnnotationList extends EtkDataObjectList<iPartsDataNutzDokAnnotation> implements iPartsConst {

    // Der normale Constructor.
    public iPartsDataNutzDokAnnotationList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Hoöt alle Elemente zur SaaBkNo und refType (SAA/KEM)
     * sortiert nach ET-Sicht und lfdNr
     *
     * @param project
     * @param refId
     * @param refType
     * @return
     */
    public static iPartsDataNutzDokAnnotationList getAllEntriesForType(EtkProject project, String refId, String refType) {
        iPartsDataNutzDokAnnotationList list = new iPartsDataNutzDokAnnotationList();
        list.loadAllTypesFromDB(project, refId, refType, LoadType.COMPLETE);
        return list;
    }

    private void loadAllTypesFromDB(EtkProject project, String refId, String refType, LoadType loadType) {
        clear(DBActionOrigin.FROM_DB);

        String[] whereFields = new String[]{ FIELD_DNA_REF_ID, FIELD_DNA_REF_TYPE };
        String[] whereValues = new String[]{ refId, refType };
        String[] sortFields = new String[]{ FIELD_DNA_DATE, FIELD_DNA_ETS, FIELD_DNA_LFDNR };

        searchSortAndFill(project, TABLE_DA_NUTZDOK_ANNOTATION, whereFields, whereValues, sortFields, loadType, DBActionOrigin.FROM_DB);
    }

    @Override
    protected iPartsDataNutzDokAnnotation getNewDataObject(EtkProject project) {
        return new iPartsDataNutzDokAnnotation(project, null);
    }
}
