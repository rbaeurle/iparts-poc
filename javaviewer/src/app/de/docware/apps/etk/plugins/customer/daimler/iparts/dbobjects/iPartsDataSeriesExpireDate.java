package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für DA_SERIES_EXPDATE
 */
public class iPartsDataSeriesExpireDate extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DSED_SERIES_NO, FIELD_DSED_AA, FIELD_DSED_FACTORY_NO };

    public iPartsDataSeriesExpireDate(EtkProject project, iPartsSeriesExpireDateId id) {
        super(KEYS);
        tableName = TABLE_DA_SERIES_EXPDATE;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }


    @Override
    public iPartsSeriesExpireDateId createId(String... idValues) {
        return new iPartsSeriesExpireDateId(idValues[0], idValues[1], idValues[2]);
    }

    @Override
    public iPartsSeriesExpireDateId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsSeriesExpireDateId)id;
    }

    public String getDbExpireDate() {
        return getFieldValue(FIELD_DSED_EXP_DATE);
    }

    public void setDbExpireDate(String value, DBActionOrigin origin) {
        setFieldValue(FIELD_DSED_EXP_DATE, value, origin);
    }
}
