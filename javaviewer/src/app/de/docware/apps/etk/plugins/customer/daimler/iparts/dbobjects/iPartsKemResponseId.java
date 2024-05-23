/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Id einer Ident-Rückmeldung aus ePEP (elektronischer ProduktionsEinsatzProzess) (Tabelle {@link iPartsConst#TABLE_DA_KEM_RESPONSE_DATA}).
 * DAIMLER-10318, Ident-Rückmeldungen aus ePEP
 */
public class iPartsKemResponseId extends IdWithType {

    public static final String TYPE = "DA_iPartsKemResponseId";

    protected enum INDEX {FACTORY_ID, KEM, FIN}

    public iPartsKemResponseId(String factoryId, String kem, String fin) {
        super(TYPE, new String[]{ factoryId, kem, fin });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsKemResponseId() {
        this("", "", "");
    }

    public String getFactoryId() {
        return id[INDEX.FACTORY_ID.ordinal()];
    }

    public String getKem() {
        return id[INDEX.KEM.ordinal()];
    }

    public String getFin() {
        return id[INDEX.FIN.ordinal()];
    }
}
