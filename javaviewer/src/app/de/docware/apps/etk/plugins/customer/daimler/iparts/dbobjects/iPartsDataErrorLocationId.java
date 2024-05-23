package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.util.misc.id.IdWithType;

/**
 * ID für die Tabelle TABLE_DA_ERROR_LOCATION
 */
public class iPartsDataErrorLocationId extends IdWithType {

    public static final String TYPE = "DA_iPartsErrorLocationId";

    private enum INDEX {BR, HM, M, SM, POSE, PART, SCT, SDA}

    /**
     * Der normale Konstruktor
     *
     * @param seriesNo Baureihe
     * @param hm       Submodul der Stückliste -> ras
     * @param m        Submodul der Stückliste -> ras
     * @param sm       Submodul der Stückliste -> ras
     * @param pose     Positionsnummer
     * @param partNo   Teile-Sachnummer
     * @param sct      Schadensteil
     * @param sda      S-Datum KEM-ab
     */
    public iPartsDataErrorLocationId(String seriesNo, String hm, String m, String sm, String pose, String partNo,
                                     String sct, String sda) {
        super(TYPE, new String[]{ seriesNo, hm, m, sm, pose, partNo, sct, sda });
    }

    public iPartsDataErrorLocationId(HmMSmId hmMSmId, String pose, String partNo, String sct, String sda) {
        this(hmMSmId.getSeries(), hmMSmId.getHm(), hmMSmId.getM(), hmMSmId.getSm(), pose, partNo, sct, sda);
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsDataErrorLocationId() {
        this("", "", "", "", "", "", "", "");
    }

    public String getSeriesNo() {
        return id[iPartsDataErrorLocationId.INDEX.BR.ordinal()];
    }

    public String getHM() {
        return id[iPartsDataErrorLocationId.INDEX.HM.ordinal()];
    }

    public String getM() {
        return id[iPartsDataErrorLocationId.INDEX.M.ordinal()];
    }

    public String getSM() {
        return id[iPartsDataErrorLocationId.INDEX.SM.ordinal()];
    }

    public String getPosE() {
        return id[iPartsDataErrorLocationId.INDEX.POSE.ordinal()];
    }

    public String getPartNo() {
        return id[iPartsDataErrorLocationId.INDEX.PART.ordinal()];
    }

    public String getSCT() {
        return id[iPartsDataErrorLocationId.INDEX.SCT.ordinal()];
    }

    public String getSDA() {
        return id[iPartsDataErrorLocationId.INDEX.SDA.ordinal()];
    }

    public HmMSmId getHmMSmId() {
        return new HmMSmId(getSeriesNo(), getHM(), getM(), getSM());
    }

}
