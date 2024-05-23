/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.model.modelelementusage;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.HierarchicalIDWithType;

/**
 * Id für die EDS/BCS Struktur Modul (MODULE) / Sub-Modul (SUB_MODULE) aus der TruckBOM.foundation.
 */

public class ModelElementUsageId extends HierarchicalIDWithType {

    public static final String TYPE = "DA_ModelElementUsageId";

    private enum INDEX {MODULE, SUB_MODULE}

    /**
     * Der normale Konstruktor
     *
     * @param module
     * @param subModule
     */
    public ModelElementUsageId(String module, String subModule) {
        super(TYPE, new String[]{ module, subModule });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public ModelElementUsageId() {
        this("", "");
    }

    public String getModule() {
        return id[INDEX.MODULE.ordinal()];
    }

    public String getSubModule() {
        return id[INDEX.SUB_MODULE.ordinal()];
    }


    @Override
    public String toString() {
        return "(" + getModule() + "/" + getSubModule() + ") ModelElementUsage";
    }

    public boolean isModuleNode() {
        return !getModule().isEmpty() && getSubModule().isEmpty();
    }

    public boolean isSubModuleNode() {
        return !getModule().isEmpty() && !getSubModule().isEmpty();
    }

    public boolean isValidId() {
        return allValuesFilled();
    }

    @Override
    public ModelElementUsageId getParentId() {
        if (isSubModuleNode()) {
            return new ModelElementUsageId(getModule(), "");
        } else {
            return null;
        }
    }
}
