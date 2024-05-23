/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper.textkinds;

import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * ENUM für die möglichen Textarten eines Stücklistentextes in einer SAA Konstruktionsstückliste
 */
public enum iPartsSaaPartlistTextkind {
    WW_TEXT("WW", "!!Wahlweise Text"),
    REMARK("BM", "!!Bemerkungstext"),
    POS_TEXTS("BKV", "!!Verwendungsstellentext"),
    NOT_A_TEXT("", "");

    private static final String TEXTKIND_DELIMITER = ",";

    private final String txtKindShort;
    private final String txtKindDescription;

    iPartsSaaPartlistTextkind(String txtKindShort, String txtKindDescription) {
        this.txtKindShort = txtKindShort;
        this.txtKindDescription = txtKindDescription;
    }

    public static Set<iPartsSaaPartlistTextkind> getTextKindsFromString(String textKindStr) {
        Set<iPartsSaaPartlistTextkind> result = new TreeSet<>(Comparator.comparing(iPartsSaaPartlistTextkind::getTxtKindShort));
        List<String> textKindShortList = StrUtils.toStringList(textKindStr, TEXTKIND_DELIMITER, false);
        for (String textKindShort : textKindShortList) {
            result.add(getFromTextkindShort(textKindShort));
        }
        return result;
    }

    public static iPartsSaaPartlistTextkind getFromTextkindShort(String value) {
        if (StrUtils.isValid(value)) {
            for (iPartsSaaPartlistTextkind textkind : values()) {
                if (textkind.getTxtKindShort().equals(value)) {
                    return textkind;
                }
            }
        }
        return NOT_A_TEXT;
    }

    public static List<iPartsSaaPartlistTextkind> getAllValidValues() {
        List<iPartsSaaPartlistTextkind> validTextkinds = new ArrayList<>();
        for (iPartsSaaPartlistTextkind textkind : values()) {
            if (textkind == NOT_A_TEXT) {
                continue;
            }
            validTextkinds.add(textkind);
        }
        return validTextkinds;
    }

    public static List<iPartsSaaPartlistTextkind> getValidValuesForCTT() {
        List<iPartsSaaPartlistTextkind> validTextkinds = new ArrayList<>();
        validTextkinds.add(POS_TEXTS);
        return validTextkinds;
    }

    public static List<iPartsSaaPartlistTextkind> getAllValidValuesSortedByPrefix() {
        List<iPartsSaaPartlistTextkind> result = getAllValidValues();
        result.sort(Comparator.comparing(iPartsSaaPartlistTextkind::getTxtKindShort));
        return result;
    }

    public String getTxtKindDescriptionWithPrefix() {
        return getTxtKindShort() + ": " + TranslationHandler.translate(getTxtKindDescription());
    }

    public static String getTextKindsAsString(Set<iPartsSaaPartlistTextkind> selectedTextkinds) {
        if (!selectedTextkinds.isEmpty()) {
            List<String> txtKindShortList = new DwList<>();
            for (iPartsSaaPartlistTextkind textkind : selectedTextkinds) {
                if (textkind == NOT_A_TEXT) {
                    continue;
                }
                txtKindShortList.add(textkind.getTxtKindShort());
            }
            return StrUtils.stringListToString(txtKindShortList, TEXTKIND_DELIMITER);
        }
        return "";
    }

    public String getTxtKindShort() {
        return txtKindShort;
    }

    public String getTxtKindDescription() {
        return txtKindDescription;
    }
}
