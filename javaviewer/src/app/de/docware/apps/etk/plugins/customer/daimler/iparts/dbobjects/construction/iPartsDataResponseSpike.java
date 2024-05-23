/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.SQLStringConvert;

import java.util.List;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für DA_RESPONSE_SPIKES
 */
public class iPartsDataResponseSpike extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DRS_FACTORY, FIELD_DRS_SERIES_NO, FIELD_DRS_AA, FIELD_DRS_BMAA,
                                                       FIELD_DRS_IDENT, FIELD_DRS_SPIKE_IDENT, FIELD_DRS_PEM, FIELD_DRS_ADAT,
                                                       FIELD_DRS_AS_DATA };

    public iPartsDataResponseSpike(EtkProject project, iPartsResponseSpikeId id) {
        super(KEYS);
        tableName = TABLE_DA_RESPONSE_SPIKES;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsResponseSpikeId createId(String... idValues) {
        return new iPartsResponseSpikeId(idValues[0], idValues[1], idValues[2], idValues[3], idValues[4], idValues[5], idValues[6],
                                         idValues[7], SQLStringConvert.ppStringToBoolean(idValues[8]));
    }

    @Override
    public iPartsResponseSpikeId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsResponseSpikeId)id;
    }

    @Override
    public iPartsDataResponseSpike cloneMe(EtkProject project) {
        iPartsDataResponseSpike clone = new iPartsDataResponseSpike(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

    public iPartsDataReleaseState getReleaseState() {
        return iPartsDataReleaseState.getTypeByDBValue(getFieldValue(FIELD_DRS_STATUS));
    }

    public boolean isReleased() {
        return (getReleaseState() == iPartsDataReleaseState.RELEASED);
    }

    public iPartsImportDataOrigin getSource() {
        return iPartsImportDataOrigin.getTypeFromCode(getFieldValue(FIELD_DRS_SOURCE));
    }

    /**
     * Sind die Inhalte von diesem Ausreißer identisch zu den Inhalten des übergebenen Ausreißers (optional abgesehen
     * vom ADAT, AfterSales-Flag und Quelle, um AS- und Produktionsdaten vergleichen zu können).
     *
     * @param otherResponseSpike
     * @param compareAfterSalesAndProduction
     * @return
     */
    public boolean equalContent(iPartsDataResponseSpike otherResponseSpike, boolean compareAfterSalesAndProduction) {
        DBDataObjectAttributes attributes = getAttributes();
        DBDataObjectAttributes otherAttributes = otherResponseSpike.getAttributes();
        if ((attributes == null) || (otherAttributes == null)) {
            return false;
        }

        List<String> ignoreFields = new DwList<>();
        if (compareAfterSalesAndProduction) {
            ignoreFields.add(FIELD_DRS_ADAT);
            ignoreFields.add(FIELD_DRS_AS_DATA);
            ignoreFields.add(FIELD_DRS_SOURCE);
        }
        return attributes.isTheSame(otherAttributes, ignoreFields, true, false);
    }
}
