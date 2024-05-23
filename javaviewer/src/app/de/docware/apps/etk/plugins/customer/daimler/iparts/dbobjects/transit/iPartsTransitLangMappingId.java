package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.transit;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Transit-Lang-Mapping-ID im iParts Plug-in (DA_TRANSIT_LANG_MAPPING).
 */
public class iPartsTransitLangMappingId extends IdWithType {

    public static String TYPE = "DA_iPartsTransitLangMappingId";

    protected enum INDEX {TRANSIT_LANGUAGE}

    private static final String TRANSIT_LANG_DELIMITER = "_";

    public static String extractNeutralTransitLang(String transitLang) {
        String[] split = transitLang.split(TRANSIT_LANG_DELIMITER);
        if (split.length >= 1) {
            return split[0];
        }
        return transitLang;
    }

    /**
     * Der normale Konstruktor
     *
     * @param transitLanguage
     */
    public iPartsTransitLangMappingId(String transitLanguage) {
        super(TYPE, new String[]{ transitLanguage });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsTransitLangMappingId() {
        this("");
    }

    public String getTransitLanguage() {
        return id[INDEX.TRANSIT_LANGUAGE.ordinal()];
    }

    public String getNeutralTransitLanguage() {
        return extractNeutralTransitLang(getTransitLanguage());
    }
}
