/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model;

import java.util.Collection;
import java.util.Collections;
import java.util.TreeMap;

/**
 * Sortierte Map von Schlüsseln auf iParts-Knotenelemente <i>E</i> (abgeleitet von {@link AbstractiPartsNode}) und Enum-Typ
 * <i>T</i>.
 */
public abstract class AbstractiPartsNodes<E extends AbstractiPartsNode, T> {

    // Damit die Sortierung der Knoten gewährleistet ist hier TreeMap verwenden
    protected TreeMap<String, E> nodes = new TreeMap<String, E>();

    public void add(String key, E node) {
        nodes.put(key, node);
    }

    public E get(String key) {
        return nodes.get(key);
    }

    public Collection<E> getValues() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    public E getOrCreate(T type, String key, E parent) {
        E result = get(key);
        if (result == null) {
            result = createNewNode(type, key, parent);
            add(key, result);
        }
        return result;
    }

    protected abstract E createNewNode(T type, String key, E parent);
}
