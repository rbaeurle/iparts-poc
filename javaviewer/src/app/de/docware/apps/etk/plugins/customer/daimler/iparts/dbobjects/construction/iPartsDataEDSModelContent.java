/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.ops.OpsId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_EDS_MODEL.
 */
public class iPartsDataEDSModelContent extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_EDS_MODELNO, FIELD_EDS_GROUP, FIELD_EDS_SCOPE, FIELD_EDS_MODEL_POS, FIELD_EDS_STEERING, FIELD_EDS_MODEL_AA, FIELD_EDS_REVFROM };

    public iPartsDataEDSModelContent(EtkProject project, iPartsEDSModelContentId id) {
        super(KEYS);
        tableName = TABLE_DA_EDS_MODEL;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsEDSModelContentId createId(String... idValues) {
        return new iPartsEDSModelContentId(idValues[0], idValues[1], idValues[2], idValues[3], idValues[4], idValues[5], idValues[6]);
    }

    @Override
    public iPartsEDSModelContentId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsEDSModelContentId)id;
    }

    public OpsId getOpsId() {
        return getAsId().getOpsId();
    }

    public String getSaaKey() {
        return getFieldValue(FIELD_EDS_MODEL_MSAAKEY);
    }

    public String getReleaseFrom() {
        return getFieldValue(FIELD_EDS_RELEASE_FROM);
    }

    public String getReleaseTo() {
        return getFieldValue(FIELD_EDS_RELEASE_TO);
    }
}
