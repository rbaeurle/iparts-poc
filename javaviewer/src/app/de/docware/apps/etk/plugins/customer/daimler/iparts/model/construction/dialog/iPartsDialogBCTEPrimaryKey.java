/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog;

import com.owlike.genson.annotation.JsonProperty;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEntrySourceType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.iPartsConstructionPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.util.StrUtils;

import java.util.Arrays;

/**
 * Einfache Klasse, die alle Felder eines DIALOG-Konstruktionsdatensatzes enthält.
 * Aus dem {@link EtkDataPartListEntry} werden die Daten im Konstruktor ermittelt.
 */
public class iPartsDialogBCTEPrimaryKey extends iPartsConstructionPrimaryKey implements iPartsConst, Comparable<iPartsDialogBCTEPrimaryKey>,
                                                                                        RESTfulTransferObjectInterface {

    public static final String DIALOG_GUID_DELIMITER = GUID_DELIMITER;
    public static final String IPARTS_CREATED_MARKER_POSV = "i";

    public static boolean isIPartsCreatedPosV(String posV) {
        return posV.endsWith(IPARTS_CREATED_MARKER_POSV);
    }

    public static String makeIPartsCreatedPosV(String posV) {
        if (!isIPartsCreatedPosV(posV)) {
            return posV + IPARTS_CREATED_MARKER_POSV;
        }
        return posV;
    }

    public static String normalizeIPartsCreatedPosV(String posV) {
        if (isIPartsCreatedPosV(posV)) {
            return StrUtils.removeAllLastCharacterIfCharacterIs(posV, IPARTS_CREATED_MARKER_POSV);
        }
        return posV;
    }

    //Baureihe
    @JsonProperty
    public String seriesNo;
    //HM-Knoten
    @JsonProperty
    public String hm;
    //M-Knoten
    @JsonProperty
    public String m;
    //SM-Knoten
    @JsonProperty
    public String sm;
    //Position
    @JsonProperty
    public String posE;
    //Positionsvariante
    @JsonProperty
    public String posV;
    //Wahlweise
    @JsonProperty
    public String ww;
    //E-Kennzeichen
    @JsonProperty
    public String et;
    //Ausführungsart
    @JsonProperty
    public String aa;
    //Änderungsdatum
    @JsonProperty
    public String sData;

    /**
     * Ermittelt aus einem {@link EtkDataPartListEntry} die DIALOG-GUID des DIALOG-Konstruktionsdatensatzes falls möglich.
     *
     * @param entry
     * @return
     */
    public static String getDialogGUID(EtkDataPartListEntry entry) {
        // beginne mit Konstruktion
        boolean isConstructionPartListEntry = false;
        EtkDataAssembly assembly = entry.getOwnerAssembly();
        if (assembly instanceof iPartsDataAssembly) {
            isConstructionPartListEntry = ((iPartsDataAssembly)assembly).isDialogSMConstructionAssembly();
        }
        if (isConstructionPartListEntry) {
            // Key aus Konstruktion lesen
            return entry.getAsId().getKLfdnr();
        } else if (entry.getFieldValue(iPartsConst.FIELD_K_SOURCE_TYPE).equals(iPartsEntrySourceType.DIALOG.getDbValue())) {
            // versuche AfterSales
            return entry.getFieldValue(iPartsConst.FIELD_K_SOURCE_GUID);
        }

        return "";
    }

    /**
     * Ermittelt aus einem {@link EtkDataPartListEntry} den Primärschlüssel des DIALOG-Konstruktionsdatensatzes falls möglich.
     *
     * @param entry
     * @return {@code null} falls der Primärschlüssel nicht ermittelt werden konnte
     */
    public static iPartsDialogBCTEPrimaryKey createFromDIALOGPartListEntry(EtkDataPartListEntry entry) {
        if (entry instanceof iPartsDataPartListEntry) {
            iPartsDialogBCTEPrimaryKey dialogBCTEPrimaryKey = ((iPartsDataPartListEntry)entry).getDialogBCTEPrimaryKey();
            if (dialogBCTEPrimaryKey != null) {
                return dialogBCTEPrimaryKey.cloneMe(); // Klon, da die Felder vom iPartsDialogBCTEPrimaryKey veränderbar sind
            }
        }

        return null;
    }

    public iPartsDialogBCTEPrimaryKey() {
    }

    public iPartsDialogBCTEPrimaryKey(String seriesNo, String hm, String m, String sm, String posE, String posV,
                                      String ww, String et, String aa, String sData) {
        this.type = Type.DIALOG;
        this.seriesNo = seriesNo;
        this.hm = hm;
        this.m = m;
        this.sm = sm;
        this.posE = posE;
        this.posV = posV;
        this.ww = ww;
        this.et = et;
        this.aa = aa;
        this.sData = sData;
    }

    public iPartsDialogBCTEPrimaryKey(HmMSmId hmMSmId, String posE, String posV, String ww, String et, String aa, String sData) {
        this(hmMSmId.getSeries(), hmMSmId.getHm(), hmMSmId.getM(), hmMSmId.getSm(), posE, posV, ww, et, aa, sData);
    }

    public iPartsDialogBCTEPrimaryKey cloneMe() {
        return new iPartsDialogBCTEPrimaryKey(seriesNo, hm, m, sm, posE, posV, ww, et, aa, sData);
    }

    /**
     * Erzeugt den BCTE-Schlüssel aus der übergebenen DIALOG-GUID.
     *
     * @param dialogGuid
     * @return {@code null} falls der BCTE-Schlüssel nicht erzeugt werden konnte (DIALOG-GUID ist leer oder ungültig)
     */
    public static iPartsDialogBCTEPrimaryKey createFromDialogGuid(String dialogGuid) {
        if (StrUtils.isValid(dialogGuid)) {
            String[] tokens = StrUtils.toStringArray(dialogGuid, DIALOG_GUID_DELIMITER, true);
            if (tokens.length >= 10) {
                return new iPartsDialogBCTEPrimaryKey(tokens[0], tokens[1], tokens[2], tokens[3], tokens[4],
                                                      tokens[5], tokens[6], tokens[7], tokens[8], tokens[9]);
            }
        }
        return null;
    }

    public HmMSmId getHmMSmId() {
        return new HmMSmId(seriesNo, hm, m, sm);
    }

    public String getPosE() {
        return posE;
    }

    public String getPosV() {
        return posV;
    }

    public boolean isIPartsCreatedBCTEKey() {
        return getPosV().endsWith(IPARTS_CREATED_MARKER_POSV);
    }

    public String getWW() {
        return ww;
    }

    public String getET() {
        return et;
    }

    public String getAA() {
        return aa;
    }

    public String getSData() {
        return sData;
    }

    /**
     * Ist ein BCTE Schlüssel eine Positionsvariante?
     *
     * @param otherKey
     * @return true wenn beide BCTE Keys identisch sind bis auf PV, WW, ET und SDATA
     */
    public boolean isPositionVariant(iPartsDialogBCTEPrimaryKey otherKey) {
        if (otherKey == null) {
            return false;
        }
        return otherKey.seriesNo.equals(seriesNo) && otherKey.hm.equals(hm) && otherKey.m.equals(m) && otherKey.sm.equals(sm) &&
               otherKey.posE.equals(posE) && otherKey.aa.equals(aa);
    }

    /**
     * Ist ein BCTE Schlüssel identisch zu diesem ohne AA?
     *
     * @param otherKey
     * @return true wenn beide BCTE Keys identisch sind bis auf AA
     */
    public boolean isTheSameWithoutAA(iPartsDialogBCTEPrimaryKey otherKey) {
        if (otherKey == null) {
            return false;
        }
        return otherKey.seriesNo.equals(seriesNo) && otherKey.hm.equals(hm) && otherKey.m.equals(m) && otherKey.sm.equals(sm) &&
               otherKey.posE.equals(posE) && otherKey.posV.equals(posV) && otherKey.ww.equals(ww) && otherKey.et.equals(et)
               && otherKey.sData.equals(sData);
    }

    /**
     * Prüft ob BR-HM-M-SM-POS-PV-AA übereinstimmen
     *
     * @param otherKey
     * @return
     */
    public boolean isBCTEPredecessorCandidate(iPartsDialogBCTEPrimaryKey otherKey) {
        if (otherKey == null) {
            return false;
        }
        return otherKey.seriesNo.equals(seriesNo) && otherKey.hm.equals(hm) && otherKey.m.equals(m) && otherKey.sm.equals(sm) &&
               otherKey.posE.equals(posE) && otherKey.posV.equals(posV) && otherKey.aa.equals(aa);
    }

    @Override
    public String createGUID() {
        return createDialogGUID();
    }

    /**
     * Erzeugt eine deterministische GUID als Referenz in das Feld {@code DA_DIALOG.DD_GUID} einer DIALOG Konstruktionsstückliste.
     *
     * @return
     */
    public String createDialogGUID() {
        return toString(DIALOG_GUID_DELIMITER, false);
    }

    @Override
    public String toString() {
        return toString("/", false);
    }

    public String toString(String delimiter, boolean spaceForEmptyValue) {
        StringBuilder sb = new StringBuilder();
        sb.append(getSpaceForEmptyValue(seriesNo, spaceForEmptyValue));
        sb.append(delimiter);
        sb.append(getSpaceForEmptyValue(hm, spaceForEmptyValue));
        sb.append(delimiter);
        sb.append(getSpaceForEmptyValue(m, spaceForEmptyValue));
        sb.append(delimiter);
        sb.append(getSpaceForEmptyValue(sm, spaceForEmptyValue));
        sb.append(delimiter);
        sb.append(getSpaceForEmptyValue(posE, spaceForEmptyValue));
        sb.append(delimiter);
        sb.append(getSpaceForEmptyValue(posV, spaceForEmptyValue));
        sb.append(delimiter);
        sb.append(getSpaceForEmptyValue(ww, spaceForEmptyValue));
        sb.append(delimiter);
        sb.append(getSpaceForEmptyValue(et, spaceForEmptyValue));
        sb.append(delimiter);
        sb.append(getSpaceForEmptyValue(aa, spaceForEmptyValue));
        sb.append(delimiter);
        sb.append(getSpaceForEmptyValue(sData, spaceForEmptyValue));
        return sb.toString();
    }

    /**
     * Liefert die DIALOG-Position inkl. Ausführungsart als einen Schlüssel z.B. zur Verwendung in Maps zurück.
     * Dieser Key ist ein Sub-Key des BCTE-Keys und dient zum Finden der Positionsvarianten (PV) innerhalb der Stückliste.
     *
     * @return
     */
    public String getPositionKeyWithoutAA() {
        StringBuilder sb = new StringBuilder();
        sb.append(seriesNo);
        sb.append(DIALOG_GUID_DELIMITER);
        sb.append(hm);
        sb.append(DIALOG_GUID_DELIMITER);
        sb.append(m);
        sb.append(DIALOG_GUID_DELIMITER);
        sb.append(sm);
        sb.append(DIALOG_GUID_DELIMITER);
        sb.append(posE);
        return sb.toString();
    }

    public String getPositionKeyWithAA() {
        return getPositionKeyWithoutAA() + DIALOG_GUID_DELIMITER + aa;
    }

    public String getPositionVariantKeyWithAA() {
        return getPositionKeyWithoutAA() + DIALOG_GUID_DELIMITER + posV + DIALOG_GUID_DELIMITER + aa;
    }

    /**
     * Liefert die DIALOG-Position ohne Ausführungsart als neuen {@link iPartsDialogBCTEPrimaryKey} zurück.
     *
     * @return
     */
    public iPartsDialogBCTEPrimaryKey getPositionBCTEPrimaryKey() {
        return new iPartsDialogBCTEPrimaryKey(seriesNo, hm, m, sm, posE, "", "", "", "", "");
    }

    /**
     * Liefert die DIALOG-Position ohne SDA als neuen {@link iPartsDialogBCTEPrimaryKey} zurück.
     *
     * @return
     */
    public iPartsDialogBCTEPrimaryKey getPositionBCTEPrimaryKeyWithoutSDA() {
        iPartsDialogBCTEPrimaryKey clone = cloneMe();
        clone.sData = "";
        return clone;
    }

    /**
     * Compare Funktion zur Sortierung bei Übernahme in AS
     * Sortierkriterien: AA, POS, PV, WW, ETZ, KEM-Datum ab (aufsteigend)
     *
     * @param o1
     * @param o2
     * @return
     */
    public static int compareForTransferList(iPartsDialogBCTEPrimaryKey o1, iPartsDialogBCTEPrimaryKey o2) {
        if ((o1 == null) && (o2 == null)) {
            return 0;
        }
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }
        if (o1.type != o2.type) {
            return -1;
        }

        int result = o1.aa.compareTo(o2.aa);
        if (result == 0) {
            result = o1.posE.compareTo(o2.posE);
            if (result == 0) {
                result = o1.posV.compareTo(o2.posV);
                if (result == 0) {
                    result = o1.ww.compareTo(o2.ww);
                    if (result == 0) {
                        result = o1.et.compareTo(o2.et);
                        if (result == 0) {
                            result = o1.sData.compareTo(o2.sData);
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        iPartsDialogBCTEPrimaryKey o2 = (iPartsDialogBCTEPrimaryKey)o;

        if (!Arrays.equals(toArray(), o2.toArray())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(toArray());
    }

    private String[] toArray() {
        return new String[]{ seriesNo,
                             hm,
                             m,
                             sm,
                             posE,
                             posV,
                             ww,
                             et,
                             aa,
                             sData };
    }

    @Override
    public int compareTo(iPartsDialogBCTEPrimaryKey o) {
        int result = seriesNo.compareTo(o.seriesNo);
        if (result != 0) {
            return result;
        }
        result = hm.compareTo(o.hm);
        if (result != 0) {
            return result;
        }
        result = m.compareTo(o.m);
        if (result != 0) {
            return result;
        }
        result = sm.compareTo(o.sm);
        if (result != 0) {
            return result;
        }
        result = posE.compareTo(o.posE);
        if (result != 0) {
            return result;
        }
        result = posV.compareTo(o.posV);
        if (result != 0) {
            return result;
        }
        result = ww.compareTo(o.ww);
        if (result != 0) {
            return result;
        }
        result = et.compareTo(o.et);
        if (result != 0) {
            return result;
        }
        result = aa.compareTo(o.aa);
        if (result != 0) {
            return result;
        }
        result = sData.compareTo(o.sData);
        return result;
    }
}
