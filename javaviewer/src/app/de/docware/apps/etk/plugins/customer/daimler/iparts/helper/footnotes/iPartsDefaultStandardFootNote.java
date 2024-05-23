package de.docware.apps.etk.plugins.customer.daimler.iparts.helper.footnotes;

import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteId;

/**
 * Enum für Standardfußnoten mit besonderer Bedeutung, da aus ihnen virtuelle Fußnoten erzeugt werden. Deshalb sind sie
 * fest codiert und mit Standardwerten belegt, so dass auch dann alles funktioniert, falls sie in der Datenbank fehlen würden.
 */
public enum iPartsDefaultStandardFootNote {

    FN_NO_LONGER_DELIVERED("402", iPartsFootnoteType.REPLACE_FOOTNOTE, "WIRD NICHT MEHR GELIEFERT",
                           "NO LONGER AVAILABLE"),
    FN_NO_LONGER_DELIVERED_ORDER_PARTS("412", iPartsFootnoteType.REPLACE_FOOTNOTE, "WIRD NICHT MEHR GELIEFERT, EINZELTEILE BESTELLEN",
                                       "NO LONGER AVAILABLE; ORDER INDIVIDUAL PARTS"),
    FN_DONT_USE_OLD_PART("414", iPartsFootnoteType.REPLACE_FOOTNOTE, "ALTES TEIL DARF NICHT MEHR EINGEBAUT WERDEN",
                         "THE OLD PART MUST NO LONGER BE INSTALLED"),
    FN_NO_LONGER_DELIVERED_ORDER_CONTENTS("417", iPartsFootnoteType.REPLACE_FOOTNOTE, "WIRD NICHT MEHR GELIEFERT, LIEFERUMFANG BESTELLEN",
                                          "IS NO LONGER DELIVERED, ORDER DELIVERY CONTENTS");

    private final String footNoteId;
    private final iPartsFootnoteType footNoteType;
    private final String defaultGermanText;
    private final String defaultEnglishText;

    iPartsDefaultStandardFootNote(String footNoteId, iPartsFootnoteType footNoteType, String defaultGermanText, String defaultEnglishText) {
        this.footNoteId = footNoteId;
        this.footNoteType = footNoteType;
        this.defaultGermanText = defaultGermanText;
        this.defaultEnglishText = defaultEnglishText;
    }

    public iPartsFootNoteId getFootNoteId() {
        return new iPartsFootNoteId(footNoteId);
    }

    public iPartsFootnoteType getFootNoteType() {
        return footNoteType;
    }

    public String getDefaultGermanText() {
        return defaultGermanText;
    }

    public String getDefaultEnglishText() {
        return defaultEnglishText;
    }
}
