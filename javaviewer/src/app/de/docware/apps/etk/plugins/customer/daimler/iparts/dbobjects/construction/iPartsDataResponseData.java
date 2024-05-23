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
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für DA_RESPONSE_DATA
 */
public class iPartsDataResponseData extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DRD_FACTORY, FIELD_DRD_SERIES_NO, FIELD_DRD_AA, FIELD_DRD_BMAA,
                                                       FIELD_DRD_PEM, FIELD_DRD_ADAT, FIELD_DRD_IDENT, FIELD_DRD_AS_DATA };

    public iPartsDataResponseData() {
        this(null, null);
    }

    public iPartsDataResponseData(EtkProject project, iPartsResponseDataId id) {
        super(KEYS);
        tableName = TABLE_DA_RESPONSE_DATA;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsResponseDataId createId(String... idValues) {
        return new iPartsResponseDataId(idValues[0], idValues[1], idValues[2], idValues[3], idValues[4], idValues[5], idValues[6],
                                        SQLStringConvert.ppStringToBoolean(idValues[7]));
    }

    @Override
    public iPartsResponseDataId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsResponseDataId)id;
    }

    @Override
    public iPartsDataResponseData cloneMe(EtkProject project) {
        iPartsDataResponseData clone = new iPartsDataResponseData(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

    /**
     * Liefert die korrigierte Baumuster-Nummer aus dem Feld {@link #FIELD_DRD_BMAA} zurück (Sachnummernkennbuchstabe C/D
     * und danach 6 bzw. 7 Stellen, wobei 7 nachfolgende Stellen auf 6 gekürzt werden).
     *
     * @return
     */
    public String getCorrectedModelNumber() {
        String modelNumber = getFieldValue(FIELD_DRD_BMAA).toUpperCase();

        // Gültig sind Baumuster mit Sachnummernkennbuchstabe C/D und danach 6 bzw. 7 Stellen, wobei 7 nachfolgende
        // Stellen auf 6 gekürzt werden sollen
        String correctedModelNumber = "";
        if ((modelNumber.length() == 7) || (modelNumber.length() == 8)) {
            char firstChar = modelNumber.charAt(0);
            if ((firstChar == 'C') || (firstChar == 'D')) {
                correctedModelNumber = modelNumber.substring(0, 7);
            }
        }

        return correctedModelNumber;
    }

    public iPartsDataReleaseState getReleaseState() {
        return iPartsDataReleaseState.getTypeByDBValue(getFieldValue(FIELD_DRD_STATUS));
    }

    public boolean isReleased() {
        return (getReleaseState() == iPartsDataReleaseState.RELEASED);
    }

    public iPartsImportDataOrigin getSource() {
        return iPartsImportDataOrigin.getTypeFromCode(getFieldValue(FIELD_DRD_SOURCE));
    }

    /**
     * Sind die Inhalte von diesen Rückmeldedaten identisch zu den Inhalten der übergebenen Rückmeldedaten (optional abgesehen
     * vom ADAT, AfterSales-Flag und Quelle, um AS- und Produktionsdaten vergleichen zu können).
     *
     * @param otherResponseData
     * @param compareAfterSalesAndProduction
     * @return
     */
    public boolean equalContent(iPartsDataResponseData otherResponseData, boolean compareAfterSalesAndProduction) {
        DBDataObjectAttributes attributes = getAttributes();
        DBDataObjectAttributes otherAttributes = otherResponseData.getAttributes();
        if ((attributes == null) || (otherAttributes == null)) {
            return false;
        }

        List<String> ignoreFields = new DwList<>();
        if (compareAfterSalesAndProduction) {
            ignoreFields.add(FIELD_DRD_ADAT);
            ignoreFields.add(FIELD_DRD_AS_DATA);
            ignoreFields.add(FIELD_DRD_SOURCE);
        }
        return attributes.isTheSame(otherAttributes, ignoreFields, true, false);
    }
}
