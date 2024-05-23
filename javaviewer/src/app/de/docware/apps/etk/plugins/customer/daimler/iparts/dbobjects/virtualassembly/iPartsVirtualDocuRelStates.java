/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly;

import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDocuRelevant;

import java.util.List;

public enum iPartsVirtualDocuRelStates {

    DOCUREL_CALCULATION_MODEL("!!Berechnungsmodell", null, "", 1),
    DOCUREL_SET_BY_AUTHOR("!!Wert Doku-relevant in DB", null, "", 2),
    DOCUREL_MODULE_HIDDEN_NOCALC("!!TU ist ausgeblendet oder nicht berechnungsrelevant", null, "!!Nein", 3),
    DOCUREL_USED_IN_AS("!!Verwendung in AS Stückliste", null, "!!Nein", 4),
    DOCUREL_USED_IN_AS_IN_OTHER_AUTHOR_ORDER("!!In fremden Autorenauftrag dokumentiert", null, "!!Nein", 5),
    DOCU_REL_CONJUNCTIONS_HAS_PERMANENT_BADCODES("!!Alle Teilkonjunktionen enthalten permanente BAD-Code", null, "!!Nein", 6),
    DOCU_REL_CONJUNCTIONS_HAS_VALID_BADCODES("!!Alle Teilkonjunktionen enthalten gültige BAD-Code", null, "!!Nein", 7),
    DOCU_REL_NO_MODEL_WITH_AA("!!Existiert Produkt mit Baumuster zur AA der TP", null, "!!Ja", 8),
    DOCU_REL_ETKZ_FROM_PART("!!Ersatzteil-KZ (TEIL) = E/V/F/S", "!!Ersatzteil-KZ (TEIL) = E/V/F/S/K", "!!Ja", 9),
    DOCU_REL_ETKZ_FROM_PART_ET_PART("!!Position mit Ersatzteil-KZ (TEIL) = \"K\" und Code \";\" und kein Leitungssatz-BK", null, "!!nein", 10),
    DOCU_REL_VALID_WIRE_HARNESS("!!Gültiger Leitungssatz-BK", null, "!!Nein", 11),
    DOKU_REL_EE_CATEGORY("!!E/E-Kategorie = SW / HW und Fußnote 400", null, "!!Nein", 12),
    DOKU_REL_SOP_VALUES("!!KEM bis <= Stichtag", null, "!!Nein", 13),
    DOKU_REL_FED("!!Federführende KF = ZB", null, "!!Nein", 14),
    DOKU_REL_AA_WITHOUT_FACTORY_CHECK("!!Offene Position ohne Werksdaten für markierte Ausführungsart", null, "!!Nein", 15),
    DOKU_REL_HAS_ACC_OR_AS_CODES("!!ACC-AS-Code", null, "!!Nein", 16),
    DOKU_REL_FACTORY_DATA("!!\"Werkseinsatzdaten-Check\" (echter PEM ab Termin)", "!!\"Werkseinsatzdaten-Check\" (echter PEM ab Termin, PEM ab Termin < PEM bis Termin)", "!!Ja", 17),
    DOKU_REL_PEM_DATES_BEFORE_SOP("!!\"PEM bis Datum aller Werkseinsatzdaten älter als SOP der Baureihe\"", null, "!!Nein", 18),
    DOKU_REL_FUTURE_FACTORY_DATA("!!\"Werkseinsatzdaten liegen mehr als 6 Monate in der Zukunft\"", null, "!!Nein", 19),
    DOCU_REL_POSV_RESULTS_CHANGED_ORIGINAL_RESULT("!!Ergebnis unter Berücksichtigung der Positionsvarianten", null, "!!Ja", 20),
    DOCU_REL_CHANGE_DOCU_REL_OMITTED_PART("!!Sonderberechnung Wegfallsachnummern", null, "!!Nein", 21),
    DOCU_REL_WITHOUT_DEFINITE_RESULT("!!Doku-Relevanz Teilprüfungen ohne Ergebnis", null, "!!Nein", 22),
    DOKU_REL_STATUS_CURRENT_POSV("!!Status der aktuellen Stücklistenposition", null, "", 23),
    DOCU_REL_POSV_RESULTS("!!Positionsvarianten und ihre Einzelergebnisse", null, "", 24),
    DOCU_REL_V_POSITION_RESULTS("!!Positionsvarianten zur V-Position", null, "", 25),
    DOCU_REL_OVERALL_RESULT("!!Gesamtergebnis", null, "", 26);

    private final String description;
    private final String specialCalcDescription;
    private final String defaultResultValue;
    private final int showOrder; // Reihenfolge für den Erklärdialog

    iPartsVirtualDocuRelStates(String description, String specialCalcDescription, String defaultResultValue, int showOrder) {
        this.description = description;
        this.specialCalcDescription = specialCalcDescription;
        this.defaultResultValue = defaultResultValue;
        this.showOrder = showOrder;
    }

    public static DocuRelFilterElement findTriggerElement(List<DocuRelFilterElement> docuRelList) {
        if (docuRelList != null) {
            for (DocuRelFilterElement docuRelFilterElement : docuRelList) {
                if (docuRelFilterElement.isTrigger()) {
                    return docuRelFilterElement;
                }
            }
        }
        return null;
    }

    public static boolean containsTriggerElement(List<DocuRelFilterElement> docuRelList) {
        return findTriggerElement(docuRelList) != null;
    }

    public String getDescription() {
        return description;
    }

    public String getSpecialCalcDescription() {
        return specialCalcDescription;
    }

    public String getDefaultResultValue() {
        return defaultResultValue;
    }

    public int getShowOrder() {
        return showOrder;
    }

    public static class DocuRelFilterElement {

        private final iPartsVirtualDocuRelStates type;
        private final iPartsDocuRelevant state;
        private final boolean specialCalc;
        private String extraInfo;
        private boolean trigger;

        public DocuRelFilterElement(iPartsVirtualDocuRelStates type, iPartsDocuRelevant state, boolean specialCalc) {
            this.type = type;
            this.state = state;
            this.specialCalc = specialCalc;
            this.extraInfo = "";
            this.trigger = false;
        }

        public String getExtraInfo() {
            return extraInfo;
        }

        public void setExtraInfo(String extraInfo) {
            this.extraInfo = extraInfo;
        }

        public boolean isTrigger() {
            return trigger;
        }

        public void setTrigger(boolean trigger) {
            this.trigger = trigger;
        }

        public iPartsVirtualDocuRelStates getType() {
            return type;
        }

        public String getDescription() {
            if (isSpecialCalc()) {
                if (type.getSpecialCalcDescription() != null) {
                    return type.getSpecialCalcDescription();
                }
            }
            return type.getDescription();
        }

        public iPartsDocuRelevant getState() {
            return state;
        }

        public boolean isSpecialCalc() {
            return specialCalc;
        }
    }
}
