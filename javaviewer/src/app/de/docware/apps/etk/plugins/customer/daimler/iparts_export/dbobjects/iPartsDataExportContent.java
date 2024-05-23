/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_export.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.misc.id.IdWithType;

public class iPartsDataExportContent extends EtkDataObject implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DEC_JOB_ID, FIELD_DEC_DO_TYPE, FIELD_DEC_DO_ID, FIELD_DEC_PRODUCT_NO };

    public iPartsDataExportContent(EtkProject project, iPartsExportContentId id) {
        super(KEYS);
        tableName = TABLE_DA_EXPORT_CONTENT;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public IdWithType createId(String... idValues) {
        return new iPartsExportContentId(idValues[0], idValues[1], idValues[2], idValues[3]);
    }

    @Override
    public iPartsExportContentId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsExportContentId)id;
    }

}
