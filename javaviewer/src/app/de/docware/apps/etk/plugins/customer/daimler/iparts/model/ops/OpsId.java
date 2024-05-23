/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.ops;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.HierarchicalIDWithType;

/**
 * Id für die EDS Struktur group (Gruppe) / scope (Umfang) Struktur bei Daimler.
 */
public class OpsId extends HierarchicalIDWithType {

    public static final String TYPE = "DA_OpsId";

    private enum INDEX {GROUP, SCOPE}

    /**
     * Der normale Konstruktor
     *
     * @param group
     * @param scope
     */
    public OpsId(String group, String scope) {
        super(TYPE, new String[]{ group, scope });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public OpsId() {
        this("", "");
    }

    public String getGroup() {
        return id[INDEX.GROUP.ordinal()];
    }

    public String getScope() {
        return id[INDEX.SCOPE.ordinal()];
    }


    @Override
    public String toString() {
        return "(" + getGroup() + "/" + getScope() + ") OPS";
    }

    public boolean isGroupNode() {
        return !getGroup().isEmpty() && getScope().isEmpty();
    }

    public boolean isScopeNode() {
        return !getGroup().isEmpty() && !getScope().isEmpty();
    }

    public boolean isValidId() {
        return allValuesFilled();
    }

    @Override
    public OpsId getParentId() {
        if (isScopeNode()) {
            return new OpsId(getGroup(), "");
        } else {
            return null;
        }
    }
}