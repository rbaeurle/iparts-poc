/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.util.misc.id.IdWithType;

/**
 * Klasse für die ID des Caches der Fehlerorte für EINE Baureihe aus der Tabelle DA_ERROR_LOCATION
 */
public class iPartsDataErrorLocationCacheId extends IdWithType {

    public static final String TYPE = "DA_iPartsErrorLocationCacheId";
    public static final String DESCRIPTION = "!!Cache für Fehlerort zur Baureihe";

    private enum INDEX {
        SERIES,
        HM,
        M,
        SM,
        POS,
        PARTNUMBER
    }

    /**
     * Der normale Konstruktor
     *
     * @param seriesNo
     * @param hm
     * @param m
     * @param sm
     * @param pos
     * @param partNo
     */
    public iPartsDataErrorLocationCacheId(String seriesNo, String hm, String m, String sm, String pos, String partNo) {
        super(TYPE, new String[]{ seriesNo, hm, m, sm, pos, partNo });
    }

    /**
     * Weiterer Konstruktor
     *
     * @param bcteKey
     * @param partNo
     */
    public iPartsDataErrorLocationCacheId(iPartsDialogBCTEPrimaryKey bcteKey, String partNo) {
        this(bcteKey.getHmMSmId().getSeries(),
             bcteKey.getHmMSmId().getHm(),
             bcteKey.getHmMSmId().getM(),
             bcteKey.getHmMSmId().getSm(),
             bcteKey.getPosE(),
             partNo);
    }

    /**
     * Konsrtujtor mit {@link iPartsDataErrorLocationId}
     *
     * @param errorLocationId
     */
    public iPartsDataErrorLocationCacheId(iPartsDataErrorLocationId errorLocationId) {
        this(errorLocationId.getSeriesNo(),
             errorLocationId.getHM(),
             errorLocationId.getM(),
             errorLocationId.getSM(),
             errorLocationId.getPosE(),
             errorLocationId.getPartNo());
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsDataErrorLocationCacheId() {
        this("", "", "", "", "", "");
    }


    public String getSeriesNo() {
        return id[iPartsDataErrorLocationCacheId.INDEX.SERIES.ordinal()];
    }

    public iPartsSeriesId getSeriesId() {
        return new iPartsSeriesId(getSeriesNo());
    }

    public String getHm() {
        return id[iPartsDataErrorLocationCacheId.INDEX.HM.ordinal()];
    }

    public String getM() {
        return id[iPartsDataErrorLocationCacheId.INDEX.M.ordinal()];
    }

    public String getSm() {
        return id[iPartsDataErrorLocationCacheId.INDEX.SM.ordinal()];
    }

    public String getPos() {
        return id[iPartsDataErrorLocationCacheId.INDEX.POS.ordinal()];
    }

    public String getPartNo() {
        return id[iPartsDataErrorLocationCacheId.INDEX.PARTNUMBER.ordinal()];
    }

    /**
     * Kreiert aus einem {@Link EtkDataPartListEntry} eine Id vom Typ {@Link iPartsDataErrorLocationCacheId}
     *
     * @param entry
     * @return
     */
    public static iPartsDataErrorLocationCacheId createFromDIALOGPartListEntry(EtkDataPartListEntry entry) {
        // Den BCTE-Schlüssel für den Stücklisteneintrag bestimmen
        iPartsDialogBCTEPrimaryKey bcteKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(entry);
        iPartsDataErrorLocationCacheId resultId;
        if (bcteKey != null) {
            resultId = new iPartsDataErrorLocationCacheId(bcteKey, entry.getPart().getAsId().getMatNr());
        } else {
            resultId = new iPartsDataErrorLocationCacheId();
        }
        return resultId;
    }
}
