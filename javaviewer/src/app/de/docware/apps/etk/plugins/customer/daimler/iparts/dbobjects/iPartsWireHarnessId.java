/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert die eindeutige ID für eine Position eines Leitungssatzbaukastens aus der Tabelle DA_WIRE_HARNESS im iParts Plug-in.
 */
public class iPartsWireHarnessId extends IdWithType {

    public static String TYPE = "DA_iPartsWireHarnessId";

    protected enum INDEX {SNR, REF, CONNECTOR_NO, SUB_SNR, POS}

    /**
     * Der normale Konstruktor
     *
     * @param snr         Leitungssatz, obere Sachnummer
     * @param refNo       Referenznummer
     * @param connectorNo Steckernummer
     * @param subSnr      Untere Sachnummer
     * @param pos         Positionsnummer
     */
    public iPartsWireHarnessId(String snr, String refNo, String connectorNo, String subSnr, String pos) {
        super(TYPE, new String[]{ snr, refNo, connectorNo, subSnr, pos });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsWireHarnessId() {
        this("", "", "", "", "");
    }

    public String getSnr() {
        return id[INDEX.SNR.ordinal()];
    }

    public String getReferenceNo() {
        return id[INDEX.REF.ordinal()];
    }

    public String getConnectorNo() {
        return id[INDEX.CONNECTOR_NO.ordinal()];
    }

    public String getSubSnr() {
        return id[INDEX.SUB_SNR.ordinal()];
    }

    public String getPos() {
        return id[INDEX.POS.ordinal()];
    }

    public boolean hasEmptyRefAndConnectorNo() {
        return StrUtils.isEmpty(getReferenceNo()) && StrUtils.isEmpty(getConnectorNo());
    }
}
