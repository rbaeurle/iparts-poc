/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.viewer.ClearAllCachesEvent;
import de.docware.util.Utils;
import de.docware.util.misc.observer.ObserverCall;

import java.util.EnumSet;

/**
 * iParts-sepzifischer Event, um alle oder auch nur spezifische Caches zu löschen.
 */
public class iPartsClearAllCachesEvent extends ClearAllCachesEvent {

    private EnumSet<iPartsCacheType> cacheTypes;

    /**
     * Sollen durch das übergebene EnumSet effektiv alle Caches gelöscht werden?
     *
     * @param cacheTypes
     * @return
     */
    public static boolean isClearAllCaches(EnumSet<iPartsCacheType> cacheTypes) {
        return (cacheTypes == null) || Utils.objectEquals(cacheTypes, EnumSet.allOf(iPartsCacheType.class));
    }

    public iPartsClearAllCachesEvent() {
    }

    public iPartsClearAllCachesEvent(EnumSet<iPartsCacheType> cacheTypes) {
        setCacheTypes(cacheTypes);
    }

    @Override
    protected Class<? extends ObserverCall> getClassForObserverRegistration() {
        return ClearAllCachesEvent.class; // Dieser Event soll wie ein ClearAllCachesEvent ausgewertet werden
    }

    public EnumSet<iPartsCacheType> getCacheTypes() {
        return cacheTypes;
    }

    public void setCacheTypes(EnumSet<iPartsCacheType> cacheTypes) {
        this.cacheTypes = cacheTypes;
        setCreateNewCacheIdentifier(isClearAllCaches(cacheTypes));
    }
}