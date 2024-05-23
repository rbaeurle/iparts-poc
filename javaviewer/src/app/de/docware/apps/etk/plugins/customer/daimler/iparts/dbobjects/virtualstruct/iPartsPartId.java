/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct;

import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine (virtuelle) Teile-ID im iParts Plug-in.
 */
public class iPartsPartId extends PartId {

    /**
     * Konstruktor für eine {@link iPartsPartId}.
     *
     * @param matNr
     * @param mVer
     */
    public iPartsPartId(String matNr, String mVer) {
        super(matNr, mVer);
        setVirtual(iPartsVirtualNode.isVirtualId(this));
    }

    public iPartsPartId() {
        this("", "");
    }

    /**
     * Konstruktor für eine {@link iPartsPartId} basierend auf einer anderen {@link IdWithType}.
     *
     * @param id
     */
    public iPartsPartId(IdWithType id) {
        // id.getValue(0) ist der Typ
        this(id.getIdLength() >= 1 ? id.getValue(1) : "", id.getIdLength() >= 2 ? id.getValue(2) : "");
    }
}
