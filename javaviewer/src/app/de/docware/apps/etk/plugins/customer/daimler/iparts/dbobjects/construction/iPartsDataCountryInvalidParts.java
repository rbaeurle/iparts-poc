/*
 * Copyright (c) 2020 Quanos
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link de.docware.apps.etk.base.project.base.EtkDataObject} um iParts-spezifische Methoden und Daten für TABLE_DA_COUNTRY_INVALID_PARTS.
 * StarParts-Teile nur noch in erlaubten Ländern ausgeben, Bauteile pro Land, die (!)NICHT(!) ausgegeben werden dürfen!
 */
public class iPartsDataCountryInvalidParts extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DCIP_PART_NO, FIELD_DCIP_COUNTRY_CODE };

    public iPartsDataCountryInvalidParts(EtkProject project, iPartsCountryInvalidPartsId id) {
        super(KEYS);
        tableName = TABLE_DA_COUNTRY_INVALID_PARTS;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsCountryInvalidPartsId createId(String... idValues) {
        return new iPartsCountryInvalidPartsId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsCountryInvalidPartsId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsCountryInvalidPartsId)id;
    }

    public String getPartNo() {
        return getAsId().getPartNo();
    }

    public String getCountryCode() {
        return getAsId().getCountryCode();
    }
}
