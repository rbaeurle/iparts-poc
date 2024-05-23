/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.util.misc.id.IdWithType;


/**
 * Repräsentiert die eindeutige ID für einen EinPAS-Knoten aus CEMaT aus der Tabelle DA_MODULE_CEMAT im iParts Plug-in.
 */
public class iPartsModuleCematId extends IdWithType {

    public static String TYPE = "DA_iPartsCematModuleId";

    protected enum INDEX {MODULE_NO, LFDNR, EINPAS_HG, EINPAS_G, EINPAS_TU}

    /**
     * Der normale Konstruktor
     *
     * @param moduleNo
     * @param lfdNr
     */
    public iPartsModuleCematId(String moduleNo, String lfdNr, String einpas_hg, String einpas_g, String einpas_tu) {
        super(TYPE, new String[]{ moduleNo, lfdNr, einpas_hg, einpas_g, einpas_tu });
    }

    /**
     * Konstuktor, der eine EinPasId verarbeiten kann.
     * Das reduziert den Code beim Import der Daten.
     *
     * @param moduleNo
     * @param lfdNr
     * @param einPasId
     */
    public iPartsModuleCematId(String moduleNo, String lfdNr, EinPasId einPasId) {
        this(moduleNo, lfdNr, einPasId.getHg(), einPasId.getG(), einPasId.getTu());
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsModuleCematId() {
        this("", "", "", "", "");
    }

    public String getModuleNo() {
        return id[INDEX.MODULE_NO.ordinal()];
    }

    public String getLfdNr() {
        return id[INDEX.LFDNR.ordinal()];
    }

    public String getEINPAS_HG() {
        return id[INDEX.EINPAS_HG.ordinal()];
    }

    public String getEINPAS_G() {
        return id[INDEX.EINPAS_G.ordinal()];
    }

    public String getEINPAS_TU() {
        return id[INDEX.EINPAS_TU.ordinal()];
    }

    public EinPasId getEinPasId() {
        return new EinPasId(getEINPAS_HG(), getEINPAS_G(), getEINPAS_TU());
    }

    public PartListEntryId getPartListEntryId() {
        return new PartListEntryId(getModuleNo(), "", getLfdNr());
    }
}
