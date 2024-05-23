package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle TABLE_DA_ERROR_LOCATION.
 */
public class iPartsDataErrorLocation extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DEL_SERIES_NO, FIELD_DEL_HM, FIELD_DEL_M, FIELD_DEL_SM, FIELD_DEL_POSE,
                                                       FIELD_DEL_PARTNO, FIELD_DEL_DAMAGE_PART, FIELD_DEL_SDA };


    public iPartsDataErrorLocation(EtkProject project, iPartsDataErrorLocationId id) {
        super(KEYS);
        tableName = TABLE_DA_ERROR_LOCATION;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsDataErrorLocationId createId(String... idValues) {
        return new iPartsDataErrorLocationId(idValues[0], idValues[1], idValues[2], idValues[3], idValues[4], idValues[5],
                                             idValues[6], idValues[7]);
    }

    @Override
    public iPartsDataErrorLocationId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsDataErrorLocationId)id;
    }

    public String getSDatA() {
        return getAsId().getSDA();
    }

    public String getSDatB() {
        return getFieldValue(FIELD_DEL_SDB);
    }
}
