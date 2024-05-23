/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.HierarchicalIDWithType;
import de.docware.util.StrUtils;

import java.util.List;

/**
 * Id für die HM (Hauptmodul) / M (Modul) / SM (Submodul) Struktur bei Daimler.
 */
public class HmMSmId extends HierarchicalIDWithType {

    public static final String TYPE = "DA_HmMSmId";

    private enum INDEX {SERIES, HM, M, SM}

    /**
     * liefert eine HmMSmId bestehend aus series und raster
     * sollte das raster ungültig sein, so ist die Id null
     *
     * @param series
     * @param raster
     * @return
     */
    public static HmMSmId getIdFromRaster(String series, String raster) {
        List<String> list;
        if ((raster != null) && !raster.isEmpty() && (raster.length() <= 6)) {
            list = StrUtils.splitStringIntoSubstrings(raster, 2);
            while (list.size() < 3) {
                list.add("");
            }
            return new HmMSmId(series, list.get(0), list.get(1), list.get(2));
        }
        return null;
    }

    /**
     * Liefert die HM/M/SM-ID vom übergeben DIALOG Quell-Modul-Kontext zurück.
     *
     * @param dialogSourceContext
     * @return
     */
    public static HmMSmId getHmMSmIdFromDIALOGSourceContext(String dialogSourceContext) {
        String[] split = dialogSourceContext.split(iPartsConst.K_SOURCE_CONTEXT_DELIMITER);
        if (split.length == 4) {
            return new HmMSmId(split[0], split[1], split[2], split[3]);
        }
        return null;
    }

    /**
     * Macht aus der {@link HmMSmId} einen Suchstring für bestimmte Datenbankfelder (z.B. DA_HMMSM_KGTU.DHK_BR_HMMSM)
     *
     * @return z.B. 'C205&02&12&20'
     */
    public String getDIALOGSourceContext() {
        return toString(iPartsConst.K_SOURCE_CONTEXT_DELIMITER, true);
    }

    /**
     * Der normale Konstruktor
     *
     * @param series
     * @param hm
     * @param m
     * @param sm
     */
    public HmMSmId(String series, String hm, String m, String sm) {
        super(TYPE, new String[]{ series, hm, m, sm });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public HmMSmId() {
        this("", "", "", "");
    }

    public String getSeries() {
        return id[INDEX.SERIES.ordinal()];
    }

    public iPartsSeriesId getSeriesId() {
        return new iPartsSeriesId(getSeries());
    }

    public String getHm() {
        return id[INDEX.HM.ordinal()];
    }

    public String getM() {
        return id[INDEX.M.ordinal()];
    }

    public String getSm() {
        return id[INDEX.SM.ordinal()];
    }

    @Override
    public String toString() {
        return "(" + getSeries() + "/" + getHm() + "/" + getM() + "/" + getSm() + ") SERIES/HM/M/SM";
    }

    public boolean isHmNode() {
        return !getSeries().isEmpty() && !getHm().isEmpty() && getM().isEmpty() && getSm().isEmpty();
    }

    public boolean isMNode() {
        return !getSeries().isEmpty() && !getHm().isEmpty() && !getM().isEmpty() && getSm().isEmpty();
    }

    public boolean isSmNode() {
        return !getSeries().isEmpty() && !getHm().isEmpty() && !getM().isEmpty() && !getSm().isEmpty();
    }

    @Override
    public HmMSmId getParentId() {
        if (isSmNode()) {
            return new HmMSmId(getSeries(), getHm(), getM(), "");
        } else if (isMNode()) {
            return new HmMSmId(getSeries(), getHm(), "", "");
        } else {
            return null;
        }
    }
}