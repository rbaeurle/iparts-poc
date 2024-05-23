/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct;

import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;

/**
 * Repräsentiert eine (virtuelle) Baugruppen-ID im iParts Plug-in.
 */
public class iPartsAssemblyId extends AssemblyId {

    /**
     * Konstruktor für eine {@link iPartsAssemblyId}.
     */
    public iPartsAssemblyId(String kVari, String kVer) {
        super(kVari, kVer);
        setVirtual(iPartsVirtualNode.isVirtualId(this));
    }
}
