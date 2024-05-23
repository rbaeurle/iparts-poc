/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.products;

import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;

import java.util.HashSet;
import java.util.Set;

/**
 * Die Verweise von einem Modul einer Baureihe in den verschiedenen Strukturen (EinPAS, HM/M/SM und KG/TU).
 */
public class iPartsModuleReferences {

    protected AssemblyId assemblyId;

    // Diesen Strukturen ist dieses Modul zugeordnet
    protected Set<EinPasId> referencesEinPas;
    protected Set<HmMSmId> referencesHmMSm;
    protected Set<KgTuId> referencesKgTu;

    public iPartsModuleReferences(AssemblyId assemblyId) {
        this.assemblyId = assemblyId;
        referencesEinPas = new HashSet<EinPasId>();
        referencesHmMSm = new HashSet<HmMSmId>();
        referencesKgTu = new HashSet<KgTuId>();
    }

    public void addReference(EinPasId einPasId) {
        referencesEinPas.add(einPasId);
    }

    public void addReference(KgTuId kgTuId) {
        referencesKgTu.add(kgTuId);
    }

    public void addReference(HmMSmId hmMSmId) {
        referencesHmMSm.add(hmMSmId);
    }

    public AssemblyId getAssemblyId() {
        return assemblyId;
    }

    public Set<EinPasId> getReferencesEinPas() {
        return referencesEinPas;
    }

    public Set<HmMSmId> getReferencesHmMSm() {
        return referencesHmMSm;
    }

    public Set<KgTuId> getReferencesKgTu() {
        return referencesKgTu;
    }
}
