/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;
import de.docware.util.sql.SQLStringConvert;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für DA_BAD_CODE.
 */
public class iPartsDataBadCode extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DBC_SERIES_NO, FIELD_DBC_AA, FIELD_DBC_CODE_ID };

    public static boolean isExpired(String expiringDate) {
        // Bei leerem Verfallsdaten -> Ewig haltbar -> muss nicht abgeprüft werden
        boolean codeExpired = false;
        if (StrUtils.isValid(expiringDate)) {
            // Es ist abgelaufen, wenn das Ablaufdatum kleiner (älter) als das jetzige Datum ist
            if (expiringDate.compareTo(DateUtils.toyyyyMMdd_currentDate()) < 0) {
                codeExpired = true;
            }
        }
        return codeExpired;
    }

    public iPartsDataBadCode(EtkProject project, iPartsBadCodeId id) {
        super(KEYS);
        tableName = TABLE_DA_BAD_CODE;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsBadCodeId createId(String... idValues) {
        return new iPartsBadCodeId(idValues[0], idValues[1], idValues[2]);
    }

    @Override
    public iPartsBadCodeId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsBadCodeId)id;
    }

    public String getExpiryDate() {
        return getFieldValue(FIELD_DBC_EXPIRY_DATE);
    }

    public void addExpiredField() {
        attributes.addField(iPartsDataVirtualFieldsDefinition.DBC_BAD_CODE_EXPIRED,
                            SQLStringConvert.booleanToPPString(isExpired(getExpiryDate())),
                            true, DBActionOrigin.FROM_DB);
    }

    public boolean isPermanent() {
        return getFieldValueAsBoolean(FIELD_DBC_PERMANENT_BAD_CODE);
    }

    public boolean isExpired() {
        if (!attributes.fieldExists(iPartsDataVirtualFieldsDefinition.DBC_BAD_CODE_EXPIRED)) {
            addExpiredField();
        }
        return getFieldValueAsBoolean(iPartsDataVirtualFieldsDefinition.DBC_BAD_CODE_EXPIRED);
    }
}
