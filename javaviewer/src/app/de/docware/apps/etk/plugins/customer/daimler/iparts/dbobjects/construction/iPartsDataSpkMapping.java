package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für DA_SPK_MAPPING.
 */
public class iPartsDataSpkMapping extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_SPKM_SERIES_NO, FIELD_SPKM_HM, FIELD_SPKM_M,
                                                       FIELD_SPKM_KURZ_E, FIELD_SPKM_KURZ_AS, FIELD_SPKM_STEERING };

    public iPartsDataSpkMapping(EtkProject project, iPartsSpkMappingId id) {
        super(KEYS);
        tableName = TABLE_DA_SPK_MAPPING;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsSpkMappingId createId(String... idValues) {
        return new iPartsSpkMappingId(idValues[0], idValues[1], idValues[2], idValues[3], idValues[4], idValues[5]);
    }

    @Override
    public iPartsSpkMappingId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsSpkMappingId)id;
    }
}
