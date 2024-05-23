/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.utils.EtkMultiSprache;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_OPS_GROUP.
 */
public class iPartsDataOPSGroup extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DOG_MODEL_NO, FIELD_DOG_GROUP };

    public iPartsDataOPSGroup(EtkProject project, iPartsOPSGroupId id) {
        super(KEYS);
        tableName = TABLE_DA_OPS_GROUP;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsOPSGroupId createId(String... idValues) {
        return new iPartsOPSGroupId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsOPSGroupId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsOPSGroupId)id;
    }

    public String getRevisionStateFrom() {
        return getFieldValue(FIELD_DOG_AS_FROM);
    }

    public EtkMultiSprache getDescription() {
        return getFieldValueAsMultiLanguage(FIELD_DOG_DESC);
    }

    public String getPictureName() {
        return getFieldValue(FIELD_DOG_PICTURE);
    }

    public boolean isInvalidDataSet() {
        return getFieldValueAsBoolean(FIELD_DOG_INVALID);
    }
}
