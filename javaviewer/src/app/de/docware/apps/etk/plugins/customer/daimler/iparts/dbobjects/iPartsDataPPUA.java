package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

public class iPartsDataPPUA extends EtkDataObject {

    static private final String[] KEYS = new String[]{ iPartsConst.FIELD_DA_PPUA_PARTNO, iPartsConst.FIELD_DA_PPUA_REGION,
                                                       iPartsConst.FIELD_DA_PPUA_SERIES, iPartsConst.FIELD_DA_PPUA_ENTITY,
                                                       iPartsConst.FIELD_DA_PPUA_TYPE, iPartsConst.FIELD_DA_PPUA_YEAR };

    public iPartsDataPPUA(EtkProject project, iPartsPPUAId id) {
        super(KEYS);
        tableName = iPartsConst.TABLE_DA_PPUA;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsPPUAId createId(String... idValues) {
        return new iPartsPPUAId(idValues[0], idValues[1], idValues[2], idValues[3], idValues[4], idValues[5]);
    }

    @Override
    public iPartsPPUAId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsPPUAId)id;
    }

    public String getYearValueToDbString() {
        return getFieldValue(iPartsConst.FIELD_DA_PPUA_VALUE);
    }

    public void setYearValueFromDbString(String dbValue, DBActionOrigin origin) {
        setFieldValue(iPartsConst.FIELD_DA_PPUA_VALUE, dbValue, origin);
    }
}
