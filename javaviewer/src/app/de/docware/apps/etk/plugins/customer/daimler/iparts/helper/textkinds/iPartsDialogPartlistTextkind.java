/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper.textkinds;

import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Textarten für die DIALOG Konstruktionsstückliste
 */
public enum iPartsDialogPartlistTextkind {

    MATURITY("01", "RG", "!!Reifegrad Text"),
    USAGE("02", "VW", "!!Verwendungstext"),
    ALTERNATIVE("03", "WW", "!!Wahlweise Text"),
    REMARK("04", "BM", "!!Bemerkungstext"),
    TURNING_MOMENT("05", "MA", "!!Drehmoment Text"),
    WELDING_MATERIAL("06", "SM", "!!Schweißmittel"),
    EXTRA_ITEM("07", "BP", "!!Beipack"),
    ZBU_TEXT("10", "ZB", "!!ZBU Text"),
    GROUPS_SNC("11", "SC", "!!Gruppen SCN"),
    POS_TEXTS("", "POS", "!!Positionstexte"),
    NOT_A_TEXT("", "", "");

    private static final String TEXTKIND_DELIMITER = ",";

    private String txtKindNo;
    private String txtKindShort;
    private String txtKindDescription;

    iPartsDialogPartlistTextkind(String txtKindNo, String txtKindShort, String txtKindDescription) {
        this.txtKindNo = txtKindNo;
        this.txtKindShort = txtKindShort;
        this.txtKindDescription = txtKindDescription;
    }

    public String getTxtKindNo() {
        return txtKindNo;
    }

    public String getTxtKindShort() {
        return txtKindShort;
    }

    public String getTxtKindDescription() {
        return txtKindDescription;
    }

    public static iPartsDialogPartlistTextkind getFromTextkindShort(String value) {
        if (StrUtils.isValid(value)) {
            for (iPartsDialogPartlistTextkind textkind : values()) {
                if (textkind.getTxtKindShort().equals(value)) {
                    return textkind;
                }
            }
        }
        return NOT_A_TEXT;

    }

    /**
     * Liefert alle validen Stücklistentextarten (ohne NOT_A_TEXT)
     *
     * @return
     */
    public static List<iPartsDialogPartlistTextkind> getValidValues() {
        List<iPartsDialogPartlistTextkind> validTextkinds = new ArrayList<iPartsDialogPartlistTextkind>();
        for (iPartsDialogPartlistTextkind textkind : values()) {
            if (textkind == NOT_A_TEXT) {
                continue;
            }
            validTextkinds.add(textkind);
        }
        return validTextkinds;
    }

    /**
     * Liefert eine sortierte Liste zurück (sortiert nach Prefix)
     *
     * @return
     */
    public static List<iPartsDialogPartlistTextkind> getValidValuesSortedByPrefix() {
        List<iPartsDialogPartlistTextkind> result = getValidValues();
        Collections.sort(result, new Comparator<iPartsDialogPartlistTextkind>() {
            @Override
            public int compare(iPartsDialogPartlistTextkind o1, iPartsDialogPartlistTextkind o2) {
                return o1.getTxtKindShort().compareTo(o2.getTxtKindShort());
            }
        });
        return result;
    }

    public static String getTextKindsAsString(Set<iPartsDialogPartlistTextkind> selectedTextkinds) {
        if (!selectedTextkinds.isEmpty()) {
            List<String> txtKindShortList = new DwList<>();
            for (iPartsDialogPartlistTextkind textkind : selectedTextkinds) {
                if (textkind == NOT_A_TEXT) {
                    continue;
                }
                txtKindShortList.add(textkind.getTxtKindShort());
            }
            return StrUtils.stringListToString(txtKindShortList, TEXTKIND_DELIMITER);
        }
        return "";
    }

    public static Set<iPartsDialogPartlistTextkind> getTextKindsFromString(String textKindStr) {
        Set<iPartsDialogPartlistTextkind> result = new TreeSet<>(new Comparator<iPartsDialogPartlistTextkind>() {
            @Override
            public int compare(iPartsDialogPartlistTextkind o1, iPartsDialogPartlistTextkind o2) {
                return o1.getTxtKindShort().compareTo(o2.getTxtKindShort());
            }
        });
        List<String> textKindShortList = StrUtils.toStringList(textKindStr, TEXTKIND_DELIMITER, false);
        for (String textKindShort : textKindShortList) {
            result.add(getFromTextkindShort(textKindShort));
        }
        return result;
    }

    /**
     * Liefert die Textartbeschreibung samt Prefix
     *
     * @return
     */
    public String getTxtKindDescriptionWithPrefix() {
        return getTxtKindShort() + ": " + TranslationHandler.translate(getTxtKindDescription());
    }
}
