/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsPicReferenceState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;

import java.util.*;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_PIC_REFERENCE.
 */
public class iPartsDataPicReference extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DPR_REF_ID, FIELD_DPR_REF_DATE };

    public iPartsDataPicReference(EtkProject project, iPartsPicReferenceId id) {
        super(KEYS);
        tableName = TABLE_DA_PIC_REFERENCE;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsPicReferenceId createId(String... idValues) {
        return new iPartsPicReferenceId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsPicReferenceId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsPicReferenceId)id;
    }

    public iPartsPicReferenceState getStatus() {
        return iPartsPicReferenceState.getFromDBValue(getFieldValue(FIELD_DPR_STATUS));
    }

    public boolean containsRefDate(String pictureDate) {
        if (StrUtils.isEmpty(pictureDate)) {
            return false;
        }
        String date = getFieldValue(FIELD_DPR_REF_DATE).toLowerCase();
        String previousDates = getFieldValue(FIELD_DPR_PREVIOUS_DATES).toLowerCase();
        return date.equals(pictureDate) || previousDates.contains(pictureDate);
    }

    public String getMcItemId() {
        return getFieldValue(FIELD_DPR_MC_ID);
    }

    public String getMcItemRevId() {
        return getFieldValue(FIELD_DPR_MC_REV_ID);
    }

    public String getVarId() {
        return getFieldValue(FIELD_DPR_VAR_ID);
    }

    public String getVarRevId() {
        return getFieldValue(FIELD_DPR_VAR_REV_ID);
    }

    public Calendar getPicRefDate() {
        return getFieldValueAsDate(iPartsConst.FIELD_DPR_REF_DATE);
    }

    public String getPreviousDatesAsString() {
        return getFieldValue(iPartsConst.FIELD_DPR_PREVIOUS_DATES);
    }

    /**
     * Gibt alle mit dem Datensatz verknüpften Daten als Liste zurück
     *
     * @return
     */
    public List<Calendar> getPreviousDates() {
        List<String> previousDates = StrUtils.toStringList(getPreviousDatesAsString(), ",");
        List<Calendar> result = new ArrayList<Calendar>();
        for (String date : previousDates) {
            try {
                result.add(DateUtils.toCalendar_yyyyMMdd(date));
            } catch (Exception e) {
                RuntimeException runtimeException = new RuntimeException("Error while parsing previous date \"" + date + "\" of " + getAsId(), e);
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, runtimeException);
            }
        }
        return result;
    }

    public void setRefDate(Calendar refDate) {
        setFieldValueAsDate(FIELD_DPR_REF_DATE, refDate, DBActionOrigin.FROM_EDIT);
    }

    public void setRefDateAsString(String refDate) {
        setFieldValue(FIELD_DPR_REF_DATE, refDate, DBActionOrigin.FROM_EDIT);
    }

    public void setPreviousDates(Set<Calendar> previousDates) {
        List<String> dates = new ArrayList<String>();
        for (Calendar calendar : previousDates) {
            dates.add(DateUtils.toyyyyMMdd_Calendar(calendar));
        }
        setFieldValue(FIELD_DPR_PREVIOUS_DATES, StrUtils.stringListToString(dates, ","), DBActionOrigin.FROM_EDIT);
    }

    /**
     * Gibt zurück, ob für die Bildreferenz alle MQ Operationen durchgelaufen und die erhaltenen IDs in der Datenbank abgelegt sind.
     *
     * @return
     */
    public boolean isFinishedWithPicture() {
        boolean allIdsValid = StrUtils.isValid(getMcItemId(), getMcItemRevId(), getVarId(), getVarRevId());
        return allIdsValid && ((getStatus() == iPartsPicReferenceState.MEDIA_RECEIVED) || (getStatus() == iPartsPicReferenceState.PREVIEW_RECEIVED)
                               || (getStatus() == iPartsPicReferenceState.DONE));
    }

    public void setStatus(iPartsPicReferenceState status) {
        setFieldValue(FIELD_DPR_STATUS, status.getDbValue(), DBActionOrigin.FROM_EDIT);
    }

    /**
     * Liefert die Nachrichten-GUID ohne MediaContentPrefix zurück
     *
     * @return
     */
    public String getMessageGUID() {
        return getFieldValue(FIELD_DPR_GUID);
    }

    public boolean hasVariantIds() {
        return StrUtils.isValid(getVarId());
    }

    /**
     * Setzt die Zeichnungsreferenz zurück. Bis auf die GUID werden dabei alle Informationen gelöscht.
     */
    public void resetData() {
        setFieldValue(FIELD_DPR_MC_ID, "", DBActionOrigin.FROM_EDIT);
        setFieldValue(FIELD_DPR_MC_REV_ID, "", DBActionOrigin.FROM_EDIT);
        setFieldValue(FIELD_DPR_VAR_ID, "", DBActionOrigin.FROM_EDIT);
        setFieldValue(FIELD_DPR_VAR_REV_ID, "", DBActionOrigin.FROM_EDIT);
        setFieldValue(FIELD_DPR_ERROR_CODE, "", DBActionOrigin.FROM_EDIT);
        setFieldValue(FIELD_DPR_ERROR_TEXT, "", DBActionOrigin.FROM_EDIT);
        setStatus(iPartsPicReferenceState.NEW);
        setFieldValueAsDateTime(FIELD_DPR_LAST_MODIFIED, GregorianCalendar.getInstance(), DBActionOrigin.FROM_EDIT);
        setFieldValue(FIELD_DPR_PREVIOUS_DATES, "", DBActionOrigin.FROM_EDIT);
    }
}