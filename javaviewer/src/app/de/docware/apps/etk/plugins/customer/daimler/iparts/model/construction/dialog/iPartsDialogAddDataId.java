/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert den Schlüssel für einen DIALOG Zusatzdatensatz für die Konstruktionsstückliste aus DA_DIALOG_ADD_DATA
 */
public class iPartsDialogAddDataId extends IdWithType {

    public static final String TYPE = "DA_DialogAddDataId";

    private enum INDEX {
        GUID, ADAT
    }

    public iPartsDialogAddDataId(String guid, String adat) {
        super(TYPE, new String[]{ guid, adat });
    }

    public String getGUID() {
        return id[INDEX.GUID.ordinal()];
    }

    public String getAdat() {
        return id[INDEX.ADAT.ordinal()];
    }

}
