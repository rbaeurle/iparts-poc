/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_CONST_KIT_CONTENT.
 */
public class iPartsDataConstKitContent extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DCKC_PART_NO, FIELD_DCKC_DCKC_POSE, FIELD_DCKC_WW, FIELD_DCKC_SDA };

    public iPartsDataConstKitContent(EtkProject project, iPartsConstKitContentId id) {
        super(KEYS);
        tableName = TABLE_DA_CONST_KIT_CONTENT;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsConstKitContentId createId(String... idValues) {
        return new iPartsConstKitContentId(idValues[0], idValues[1], idValues[2], idValues[3]);
    }

    @Override
    public iPartsConstKitContentId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsConstKitContentId)id;
    }

    public String getPartNo() {
        return getAsId().getPartNo();
    }

    public PartId getPartId() {
        return new PartId(getPartNo(), "");
    }

    public String getSubPartNo() {
        return getFieldValue(FIELD_DCKC_SUB_PART_NO);
    }

    public PartId getSubPartId() {
        return new PartId(getSubPartNo(), "");
    }

    public String getSDA() {
        return getAsId().getsSDA();
    }

    public String getSDB() {
        return getFieldValue(FIELD_DCKC_SDB);
    }

    public String getQuantity() {
        return getFieldValue(FIELD_DCKC_QUANTITY);
    }
}
