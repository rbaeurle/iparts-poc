/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import com.owlike.genson.annotation.JsonProperty;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSeriesEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.framework.utils.EtkMultiSprache;

import java.util.Objects;

/**
 * Repräsentation von einem Ereignis in iParts.
 */
public class iPartsEvent implements RESTfulTransferObjectInterface {

    private static final String NOT_RELEVANT_EVENT_ID = "NOT_RELEVANT";
    private static final String NOT_RELEVANT_EVENT_TEXT = "!!Nicht relevant";

    @JsonProperty
    private iPartsSeriesId seriesId;
    @JsonProperty
    private String eventId;
    @JsonProperty
    private String previousEventId;
    @JsonProperty
    private EtkMultiSprache title;
    @JsonProperty
    private EtkMultiSprache remark;
    @JsonProperty
    private boolean relevantForConversion;
    @JsonProperty
    private String status;
    @JsonProperty
    private String code;
    @JsonProperty
    private int ordinal;
    @JsonProperty
    private boolean isNotRelevantEvent;

    public iPartsEvent() {
    }

    public iPartsEvent(iPartsSeriesId seriesId, String eventId, String previousEventId, EtkMultiSprache title, EtkMultiSprache remark,
                       boolean relevantForConversion, String status, String code, int ordinal) {
        this.seriesId = seriesId;
        this.eventId = eventId;
        this.previousEventId = previousEventId;
        this.title = title;
        this.remark = remark;
        this.relevantForConversion = relevantForConversion;
        this.status = status;
        this.code = code;
        this.ordinal = ordinal;
        this.isNotRelevantEvent = isNotRelevantEventId(eventId);
    }

    public iPartsEvent(iPartsDataSeriesEvent dataEvent, int ordinal) {
        this(new iPartsSeriesId(dataEvent.getAsId().getSeriesNumber()), dataEvent.getEventId(), dataEvent.getPreviousEventId(),
             dataEvent.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DSE_DESC), dataEvent.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DSE_REMARK),
             dataEvent.getFieldValueAsBoolean(iPartsConst.FIELD_DSE_CONV_RELEVANT), dataEvent.getFieldValue(iPartsConst.FIELD_DSE_STATUS),
             dataEvent.getFieldValue(iPartsConst.FIELD_DSE_CODES), ordinal);
    }

    public iPartsSeriesId getSeriesId() {
        return seriesId;
    }

    public String getEventId() {
        return eventId;
    }

    public String getPreviousEventId() {
        return previousEventId;
    }

    public EtkMultiSprache getTitle() {
        return title;
    }

    public EtkMultiSprache getRemark() {
        return remark;
    }

    public boolean isRelevantForConversion() {
        return relevantForConversion;
    }

    public String getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    /**
     * Liefert die Reihenfolge innerhalb der Ereigniskette zurück beginnend bei {@code 0}.
     *
     * @return
     */
    public int getOrdinal() {
        return ordinal;
    }

    public boolean isNotRelevantEvent() {
        return isNotRelevantEvent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        iPartsEvent that = (iPartsEvent)o;
        return (relevantForConversion == that.relevantForConversion) &&
               (ordinal == that.ordinal) &&
               Objects.equals(seriesId, that.seriesId) &&
               Objects.equals(eventId, that.eventId) &&
               Objects.equals(previousEventId, that.previousEventId) &&
               Objects.equals(title, that.title) &&
               Objects.equals(remark, that.remark) &&
               Objects.equals(status, that.status) &&
               Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seriesId, eventId, previousEventId, title, remark, relevantForConversion, status, code, ordinal);
    }

    public static iPartsEvent createNotRelevantEvent(EtkProject project) {
        EtkMultiSprache title = new EtkMultiSprache(NOT_RELEVANT_EVENT_TEXT, project.getConfig().getDatabaseLanguages());
        iPartsEvent notRelevantEvent = new iPartsEvent(null, NOT_RELEVANT_EVENT_ID, "", title, null, false, "", "", -1);
        return notRelevantEvent;
    }

    public static boolean isNotRelevantEventId(String selectedEventId) {
        return NOT_RELEVANT_EVENT_ID.equals(selectedEventId);
    }
}