/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;

/**
 * Enum für hierarchische Attribute, die an den HM/M/SM Knoten gesetzt werden
 */
public enum HmMSmMenuItemType {
    HIDDEN("iPartsMenuItemHideHmMSmNode", "!!HM/M/SM-Knoten ausblenden", "!!HM/M/SM-Knoten einblenden",
           iPartsConst.FIELD_DH_HIDDEN),
    NO_CALC("iPartsMenuItemNotCalcHmMSmNode", "!!HM/M/SM-Knoten nicht berechnungsrelevant setzen",
            "!!HM/M/SM-Knoten berechnungsrelevant setzen", iPartsConst.FIELD_DH_NO_CALCULATION),
    CHANGE_DOCU_REL_OMITTED_PART("iPartsMenuItemChangeDocuRelOmittedPart", "!!Sonderberechnung Wegfallsachnummer aktivieren",
                                 "!!Sonderberechnung Wegfallsachnummer deaktivieren", iPartsConst.FIELD_DH_SPECIAL_CALC_OMITTED_PARTS);

    private final String menuItemName;
    private final String menuItemTextActive;
    private final String menuItemTextInactive;
    private final String hmMSmDBFieldname;

    HmMSmMenuItemType(String menuItemName, String menuItemTextActive, String menuItemTextInactive, String hmMSmDBFieldname) {
        this.menuItemName = menuItemName;
        this.menuItemTextActive = menuItemTextActive;
        this.menuItemTextInactive = menuItemTextInactive;
        this.hmMSmDBFieldname = hmMSmDBFieldname;
    }

    public String getMenuItemName() {
        return menuItemName;
    }

    public String getMenuItemTextActive() {
        return menuItemTextActive;
    }

    public String getMenuItemTextInactive() {
        return menuItemTextInactive;
    }

    public String getHmMSmDBFieldname() {
        return hmMSmDBFieldname;
    }
}
