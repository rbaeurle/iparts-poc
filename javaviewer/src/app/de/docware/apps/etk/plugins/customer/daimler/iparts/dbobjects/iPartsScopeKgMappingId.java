/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/**
 * Zuordnung Scope-ID zu KG im iParts Plug-in.
 * Cortex liefert für die EDS-Umfänge nicht mehr die KG, sondern den neuen Wert "ScopeID" auf EDS/BCS.
 * Diese Klasse spiegelt die Id wider.
 */
public class iPartsScopeKgMappingId extends IdWithType {

    public static String TYPE = "DA_iPartsScopeKgMapping";

    // DSKM_SCOPE_ID,DSKM_KG
    protected enum INDEX {SCOPE, KG}

    /**
     * Der normale Konstruktor
     *
     * @param scopeId Scope-ID aus Cortex für EDS-Umfänge
     * @param kg      Die referenzierte/ge-mapp-te KG
     */
    public iPartsScopeKgMappingId(String scopeId, String kg) {
        super(TYPE, new String[]{ scopeId, kg });
    }

    /**
     * Für Instanzen basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsScopeKgMappingId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsScopeKgMappingId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsScopeKgMappingId() {
        this("", "");
    }

    public String getScope() {
        return id[INDEX.SCOPE.ordinal()];
    }

    public String getKg() {
        return id[INDEX.KG.ordinal()];
    }
}
