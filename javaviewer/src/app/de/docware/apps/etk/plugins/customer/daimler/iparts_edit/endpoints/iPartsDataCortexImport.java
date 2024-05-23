/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle TABLE_DA_CORTEX_IMPORT_DATA.
 */
public class iPartsDataCortexImport extends EtkDataObject implements iPartsConst {

    public static iPartsDataCortexImport createNewDataCortexImport(EtkProject project, iPartsCortexImportEndpointNames endPointName,
                                                                   iPartsCortexImportMethod importMethod,
                                                                   String dataAsJSON) {
        iPartsCortexImportId cortexImportId = new iPartsCortexImportId(endPointName.getDBValue());
        iPartsDataCortexImport dataCortexImport = new iPartsDataCortexImport(project, cortexImportId);
        dataCortexImport.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        dataCortexImport.setImportMethod(importMethod, DBActionOrigin.FROM_EDIT);
        dataCortexImport.setProcessingState(iPartsCortexImportProcessingState.NEW, DBActionOrigin.FROM_EDIT);
        dataCortexImport.setCurrentData(dataAsJSON, DBActionOrigin.FROM_EDIT);

        return dataCortexImport;
    }


    private static final String[] KEYS = new String[]{ FIELD_DCI_CREATION_TS, FIELD_DCI_ENDPOINT_NAME };

    public iPartsDataCortexImport(EtkProject project, iPartsCortexImportId id) {
        super(KEYS);
        tableName = TABLE_DA_CORTEX_IMPORT_DATA;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsCortexImportId createId(String... idValues) {
        return new iPartsCortexImportId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsCortexImportId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsCortexImportId)id;
    }

    public iPartsCortexImportProcessingState getProcessingState() {
        return iPartsCortexImportProcessingState.getFromDBValue(getFieldValue(FIELD_DCI_STATUS));
    }

    public void setProcessingState(iPartsCortexImportProcessingState state, DBActionOrigin origin) {
        setFieldValue(FIELD_DCI_STATUS, state.getDBValue(), origin);
    }

    public iPartsCortexImportEndpointNames getEndpointName() {
        return iPartsCortexImportEndpointNames.getFromDBValue(getFieldValue(FIELD_DCI_ENDPOINT_NAME));
    }

    public void setEndpointName(iPartsCortexImportEndpointNames endpointName, DBActionOrigin origin) {
        setFieldValue(FIELD_DCI_ENDPOINT_NAME, endpointName.getDBValue(), origin);
    }

    public iPartsCortexImportMethod getImportMethod() {
        return iPartsCortexImportMethod.getFromDBValue(getFieldValue(FIELD_DCI_IMPORT_METHOD));
    }

    public void setImportMethod(iPartsCortexImportMethod importMethod, DBActionOrigin origin) {
        setFieldValue(FIELD_DCI_IMPORT_METHOD, importMethod.getDBValue(), origin);
    }

    public void setCurrentData(String currentData, DBActionOrigin origin) {
        setFieldValueAsZippedBlobFromString(FIELD_DCI_DATA, currentData, origin);
    }

    public String getCurrentData() {
        return getFieldValueAsStringFromZippedBlob(FIELD_DCI_DATA);
    }


}
