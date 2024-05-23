/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;

public class iPartsDataCortexImportList extends EtkDataObjectList<iPartsDataCortexImport> implements iPartsConst {

    public static iPartsDataCortexImportList loadNewEntriesForEndpoint(EtkProject project, iPartsCortexImportEndpointNames endPointName) {
        iPartsDataCortexImportList list = new iPartsDataCortexImportList();
        list.loadAllEntriesWithStatusFromDB(project, iPartsCortexImportProcessingState.NEW, endPointName, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadAllEntriesWithStatusFromDB(EtkProject project, iPartsCortexImportProcessingState statusValue,
                                                iPartsCortexImportEndpointNames endPointName, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DCI_STATUS };
        String[] whereValues = new String[]{ statusValue.getDBValue() };
        String[] sortFields = new String[]{ FIELD_DCI_CREATION_TS };

        if (endPointName != null) {
            whereFields = StrUtils.mergeArrays(whereFields, FIELD_DCI_ENDPOINT_NAME);
            whereValues = StrUtils.mergeArrays(whereValues, endPointName.getDBValue());
        }

        searchSortAndFill(project, TABLE_DA_CORTEX_IMPORT_DATA, whereFields, whereValues, sortFields,
                          LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataCortexImport getNewDataObject(EtkProject project) {
        return new iPartsDataCortexImport(project, null);
    }
}
