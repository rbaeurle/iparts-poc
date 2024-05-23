package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogId;
import de.docware.util.misc.id.IdWithType;

public class iPartsGenericPartId extends IdWithType {

    public static final String TYPE = "DA_GenericPart";
    public static final String DESCRIPTION = "!!DIALOG Generic Part";

    protected enum INDEX {GUID}

    /**
     * Der normale Konstruktor
     *
     * @param guid
     */
    public iPartsGenericPartId(String guid) {
        super(TYPE, new String[]{ guid });
    }


    /**
     * Liegt eine gültige ID vor (orderGuid ist nicht leer)
     *
     * @return true, falls gültige Id
     */
    public boolean isValidId() {
        return !getGuid().isEmpty();
    }

    public String getGuid() {
        return id[iPartsGenericPartId.INDEX.GUID.ordinal()];
    }

    public iPartsDialogId getAsDialogId() {
        return new iPartsDialogId(getGuid());
    }
}
