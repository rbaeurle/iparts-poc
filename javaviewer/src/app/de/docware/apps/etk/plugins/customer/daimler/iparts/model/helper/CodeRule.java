/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsCodeDataId;
import de.docware.framework.utils.EtkMultiSprache;

/**
 * Repräsentiert einen Record aus Tabelle DA_CODE
 */
public class CodeRule {

    private iPartsCodeDataId codeDataId;
    private String toDate;
    private EtkMultiSprache desc;

    public CodeRule(iPartsCodeDataId codeDataId, String toDate, EtkMultiSprache desc) {
        this.codeDataId = codeDataId;
        this.toDate = toDate;
        this.desc = desc;
    }

    public iPartsCodeDataId getCodeDataId() {
        return codeDataId;
    }

    public String getToDate() {
        return toDate;
    }

    public EtkMultiSprache getDesc() {
        return desc;
    }
}
