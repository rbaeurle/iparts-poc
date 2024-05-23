/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper.footnotes;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.EnumSet;

/**
 * Fußnoten-Typen
 */
public enum iPartsFootnoteType implements RESTfulTransferObjectInterface {
    PARTLIST("PL"),
    FACTORY_DATA("FD"),
    COLOR_TABLEFOOTNOTE("CT"),
    REPLACE_FOOTNOTE("XX"),
    CONSTRUCTION_FOOTNOTE("YY"),
    PART("VIRTUAL_PART"),
    NO_TYPE("");

    private static EnumSet<iPartsFootnoteType> realFootNoteTypes = EnumSet.of(PARTLIST, NO_TYPE);

    private String value;

    iPartsFootnoteType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public boolean isRealFootnote() {
        return getRealFootNoteTypes().contains(this);
    }

    public static EnumSet<iPartsFootnoteType> getRealFootNoteTypes() {
        return realFootNoteTypes;
    }

    public static iPartsFootnoteType getFromDBValue(String dbValue) {
        for (iPartsFootnoteType footnoteType : values()) {
            if (footnoteType.getValue().equals(dbValue)) {
                return footnoteType;
            }
        }
        return NO_TYPE;
    }
}
