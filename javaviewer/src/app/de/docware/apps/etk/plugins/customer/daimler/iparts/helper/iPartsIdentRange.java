/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import com.owlike.genson.annotation.JsonProperty;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.util.StrUtils;

/**
 * Hilfsklasse für ein Ident-Intervall (Ab- und Bis-Ident)
 */
public class iPartsIdentRange implements RESTfulTransferObjectInterface {

    @JsonProperty
    private String fromIdent;
    @JsonProperty
    private String toIdent;

    public iPartsIdentRange() {
    }

    public iPartsIdentRange(String fromIdent, String toIdent) {
        if (StrUtils.isValid(fromIdent)) {
            this.fromIdent = fromIdent;
        } else {
            this.fromIdent = "";
        }
        if (StrUtils.isValid(toIdent)) {
            this.toIdent = toIdent;
        } else {
            this.toIdent = "";
        }
    }

    public String getFromIdent() {
        return fromIdent;
    }

    public String getToIdent() {
        return toIdent;
    }
}
