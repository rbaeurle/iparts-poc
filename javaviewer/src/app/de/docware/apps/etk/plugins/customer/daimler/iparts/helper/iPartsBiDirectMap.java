/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import java.util.HashMap;

/**
 * Eine bidirektionale Map, bei der man zum Wert auch den Schlüssel bekommt
 * Bei der Verwendung zu berücksichtigen:
 * Alle Keys sollten eindeutig sein, alle Values müssen eindeutig sein
 * Also: es sollten keine Keys und es dürfen keine Values doppelt sein
 *
 * @param <K>
 * @param <V>
 */
public class iPartsBiDirectMap<K, V> {

    private final HashMap<K, V> mainMap;
    private final HashMap<V, K> reverseMap;

    public iPartsBiDirectMap() {
        mainMap = new HashMap<>();
        reverseMap = new HashMap<>();
    }

    public void put(K key, V value) {
        // Abfangen von doppelten Keys
        if (mainMap.containsKey(key)) {
            reverseMap.remove(mainMap.get(key));
        }
        mainMap.put(key, value);
        reverseMap.put(value, key);
    }

    public V get(K key) {
        return mainMap.get(key);
    }

    public K getKey(V value) {
        return reverseMap.get(value);
    }

    public int size() {
        return mainMap.size();
    }
}
