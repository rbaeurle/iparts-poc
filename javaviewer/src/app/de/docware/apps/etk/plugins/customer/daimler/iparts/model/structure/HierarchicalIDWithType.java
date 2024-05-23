/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure;

import de.docware.util.misc.id.IdWithType;

/**
 * Eine hierarchische {@link IdWithType}, die eine Vater-{@link IdWithType} zurückgeben kann.
 */
public abstract class HierarchicalIDWithType extends IdWithType {

    /**
     * Konstruktor für eine Id mit dem übergebenen Typ und den Werten <i>idValues</i>.
     */
    public HierarchicalIDWithType(String type, String[] idValues) {
        super(type, idValues);
    }

    /**
     * Liefert die Vater-{@link IdWithType} für diese hierarchische {@link IdWithType} zurück.
     *
     * @return {@code null} falls es keine Vater-{@link IdWithType} gibt.
     */
    public abstract HierarchicalIDWithType getParentId();
}
