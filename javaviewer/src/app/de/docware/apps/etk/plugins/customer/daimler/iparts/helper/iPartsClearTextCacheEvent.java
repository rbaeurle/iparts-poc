/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.events.AbstractEtkClusterEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;

/**
 * Event für das gezielte Löschen von Textcaches, die während der Importe genutzt werden (Lexikon)
 */
public class iPartsClearTextCacheEvent extends AbstractEtkClusterEvent {

    private DictTextKindTypes textKindType;
    private boolean warmUpCache;

    public iPartsClearTextCacheEvent(DictTextKindTypes textKindType, boolean warmUpCache) {
        this.textKindType = textKindType;
        this.warmUpCache = warmUpCache;
    }

    public iPartsClearTextCacheEvent(DictTextKindTypes textKindType) {
        this(textKindType, (textKindType == null) || DictTextCache.isTextKindWithCache(textKindType));
    }

    public iPartsClearTextCacheEvent() {
        this(DictTextKindTypes.UNKNOWN, true);
    }

    public DictTextKindTypes getTextKindType() {
        return textKindType;
    }

    public void setTextKindType(DictTextKindTypes textKindType) {
        this.textKindType = textKindType;
    }

    public boolean isWarmUpCache() {
        return warmUpCache;
    }

    public void setWarmUpCache(boolean warmUpCache) {
        this.warmUpCache = warmUpCache;
    }
}
