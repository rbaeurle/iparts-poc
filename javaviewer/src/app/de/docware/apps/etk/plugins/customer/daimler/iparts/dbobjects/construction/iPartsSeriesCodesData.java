package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.misc.id.IdWithType;

/*
 * Copyright (c) 2018 Docware GmbH
 *
 * Tabelle [DA_SERIES_CODES], Datenobjekt für die DIALOG Baubarkeit (gültige Code zu Baureihe).
 *
 *       FIELD_DSC_SERIES_NO    // PK // Baureihe "C205", "D6519"
 *       FIELD_DSC_GROUP        // PK // Gruppe (3-stellig) "AAM", "CAG"
 *       FIELD_DSC_POS          // PK // Position "0100"
 *       FIELD_DSC_POSV         // PK // Positionsvariante  "0001"
 *       FIELD_DSC_AA           // PK // AA der BR (z.B. Hubraumcode) "FW", "FS", "M20"
 *       FIELD_DSC_SDATA        // PK // KEM-Status+Datum- AB
 */

public class iPartsSeriesCodesData extends EtkDataObject implements EtkDbConst, iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DSC_SERIES_NO, FIELD_DSC_GROUP, FIELD_DSC_POS, FIELD_DSC_POSV, FIELD_DSC_AA, FIELD_DSC_SDATA };

    public iPartsSeriesCodesData(EtkProject project, iPartsSeriesCodesDataId id) {
        super(KEYS);
        tableName = TABLE_DA_SERIES_CODES;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public IdWithType createId(String... idValues) {
        return new iPartsSeriesCodesDataId(idValues[0], idValues[1], idValues[2], idValues[3], idValues[4], idValues[5]);
    }

    @Override
    public iPartsSeriesCodesDataId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsSeriesCodesDataId)id;
    }

    public String getRegulation() {
        return getFieldValue(FIELD_DSC_REGULATION);
    }
}
