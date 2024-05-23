/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_export.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

public class iPartsDataExportRequest extends EtkDataObject implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DER_JOB_ID };

    public iPartsDataExportRequest(EtkProject project, iPartsExportRequestId id) {
        super(KEYS);
        tableName = TABLE_DA_EXPORT_REQUEST;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public IdWithType createId(String... idValues) {
        return new iPartsExportRequestId(idValues[0]);
    }

    @Override
    public iPartsExportRequestId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsExportRequestId)id;
    }

    public String getJobId() {
        return getAsId().getJobId();
    }

    /**
     * Erzeugt ein {@link iPartsDataExportRequest} Objekt mit einer GUID, die noch nicht vergeben wurde
     *
     * @param project
     * @return
     */
    public static iPartsDataExportRequest createExportRequestWithUnusedGUID(EtkProject project) {
        iPartsDataExportRequest exportRequest;
        do {
            iPartsExportRequestId exportRequestId = new iPartsExportRequestId(StrUtils.makeGUID());
            exportRequest = new iPartsDataExportRequest(project, exportRequestId);
        } while (exportRequest.existsInDB());
        exportRequest.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        return exportRequest;
    }
}
