package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

/*
 * Copyright (c) 2017 Docware GmbH
 *
 * Tabelle [DA_DIALOG_DSR], Datenobjekt für sicherheits- und zertifizierungsrelevante Teile, DSR-Kenner
 */

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.misc.id.IdWithType;

public class iPartsDSRData extends EtkDataObject implements EtkDbConst, iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DSR_MATNR, FIELD_DSR_TYPE, FIELD_DSR_NO, FIELD_DSR_SDATA, FIELD_DSR_MK4, FIELD_DSR_MK5 };

    public iPartsDSRData(EtkProject project, iPartsDSRDataId id) {
        super(KEYS);
        tableName = TABLE_DA_DIALOG_DSR;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public IdWithType createId(String... idValues) {
        return new iPartsDSRDataId(idValues[0], idValues[1], idValues[2], idValues[3], idValues[4], idValues[5]);
    }

    @Override
    public IdWithType getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsDSRDataId)id;
    }
}
