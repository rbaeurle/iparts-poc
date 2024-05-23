/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsDataCode;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.util.date.DateUtils;

import java.util.Calendar;

/**
 * CodeValidityDetail Data Transfer Object für die iParts Webservices
 * Wird verwendet von {@link iPartsWSPartInfo}
 */
public class iPartsWSCodeValidityDetail implements RESTfulTransferObjectInterface {

    private String code;
    private String name;
    private String dateFrom;

    /**
     * Leerer Konstruktor (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSCodeValidityDetail() {
    }

    public iPartsWSCodeValidityDetail(EtkProject project, iPartsDataCode dataCode) {
        this.code = dataCode.getFieldValue(iPartsConst.FIELD_DC_CODE_ID);
        this.name = dataCode.getFieldValue(iPartsConst.FIELD_DC_DESC, project.getDBLanguage(), true);
        Calendar dateFrom = dataCode.getFieldValueAsDate(iPartsConst.FIELD_DC_SDATA);
        if (dateFrom != null) {
            this.dateFrom = DateUtils.toISO_Calendar(dateFrom);
        }
    }

    // Getter and Setter
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }
}
