/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config;

/**
 * Enum mit Werten für das AS-PLM XML Element "Relation"
 */
public enum iPartsRelationType {
    ORIGINAL("original"),
    DERIVED("derived"),
    ADDITIONAL("additional");

    private String relationValue;

    iPartsRelationType(String relationValue) {
        this.relationValue = relationValue;
    }

    public String getRelationValue() {
        return relationValue;
    }

    public static iPartsRelationType getFromAlias(String relationValue) {
        for (iPartsRelationType relationType : values()) {
            if (relationType.relationValue.equals(relationValue)) {
                return relationType;
            }
        }
        return null;
    }
}
