/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.ops.OpsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine EDS Baumusterinhalt-ID aus der Tabelle DA_EDS_MODEL im iParts Plug-in.
 */
public class iPartsEDSModelContentId extends IdWithType {

    public static String TYPE = "DA_iPartsEDSModelContentId";

    protected enum INDEX {MODEL_NO, GROUP, SCOPE, POS, STEERING, AA, REVFROM}

    /**
     * Der normale Konstruktor
     */
    public iPartsEDSModelContentId(String modelNo, String group, String scope, String pos, String steering, String aa, String revFrom) {

        super(TYPE, new String[]{ modelNo, group, scope, pos, steering, aa, revFrom });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsEDSModelContentId() {
        this("", "", "", "", "", "", "");
    }

    public String getModelNumber() {
        return id[INDEX.MODEL_NO.ordinal()];
    }

    public String getGroup() {
        return id[INDEX.GROUP.ordinal()];
    }

    public String getScope() {
        return id[INDEX.SCOPE.ordinal()];
    }

    public String getPosition() {
        return id[INDEX.POS.ordinal()];
    }

    public String getSteering() {
        return id[INDEX.STEERING.ordinal()];
    }

    public String getAusfuehrungsArt() {
        return id[INDEX.AA.ordinal()];
    }

    public String getRevisionFrom() {
        return id[INDEX.REVFROM.ordinal()];
    }

    public OpsId getOpsId() {
        return new OpsId(getGroup(), getScope());
    }

    public iPartsModelId getModelId() {
        return new iPartsModelId(getModelNumber());
    }
}
