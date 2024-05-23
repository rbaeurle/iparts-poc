/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction;


import com.owlike.genson.annotation.JsonProperty;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.helper.iPartsSaaPartsListPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.mbs.iPartsMBSPrimaryKey;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

public abstract class iPartsConstructionPrimaryKey implements iPartsConst, RESTfulTransferObjectInterface {

    public static final String GUID_DELIMITER = "|";

    public enum Type {
        DIALOG,
        SAA_PARTSLIST,
        MBS
    }

    @JsonProperty
    protected Type type;

    public boolean isDialog() {
        return type == Type.DIALOG;
    }

    public boolean isSaaPartsList() {
        return type == Type.SAA_PARTSLIST;
    }

    public boolean isMBS() {
        return type == Type.MBS;
    }

    public static iPartsConstructionPrimaryKey createFromSaaConstPartListEntry(EtkDataPartListEntry entry, Type type) {
        if (type == Type.DIALOG) {
            return iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(entry);
        } else if (type == Type.SAA_PARTSLIST) {
            return iPartsSaaPartsListPrimaryKey.createFromEDSPartListEntry(entry);
        }
        // Absichtlich kein Fall für MBS da hier im Retail eine K_SOURCE_GUID aus kVari_kLfdNr verwendet wird, die nicht
        // mehr auf die Konstruktionswerte zurückgeführt werden kann
        return null;
    }

    // wird aktuell nur für DIALOG verwendet
    public static int compareForTransferList(iPartsConstructionPrimaryKey o1, iPartsConstructionPrimaryKey o2, Type type) {
        if (type == Type.DIALOG) {
            return iPartsDialogBCTEPrimaryKey.compareForTransferList((iPartsDialogBCTEPrimaryKey)o1, (iPartsDialogBCTEPrimaryKey)o2);
        } else if (type == Type.SAA_PARTSLIST) {
            return iPartsSaaPartsListPrimaryKey.compareForTransferList((iPartsSaaPartsListPrimaryKey)o1, (iPartsSaaPartsListPrimaryKey)o2);
        }
        return 0;
    }

    public iPartsDialogBCTEPrimaryKey getAsDialogBCTEPrimaryKey() {
        if (isDialog()) {
            return (iPartsDialogBCTEPrimaryKey)this;
        }
        return null;
    }

    public iPartsSaaPartsListPrimaryKey getAsSaaPartsListPrimaryKey() {
        if (isSaaPartsList()) {
            return (iPartsSaaPartsListPrimaryKey)this;
        }
        return null;
    }

    public iPartsMBSPrimaryKey getAsMBSPrimaryKey() {
        if (isMBS()) {
            return (iPartsMBSPrimaryKey)this;
        }
        return null;
    }

    protected String getSpaceForEmptyValue(String value, boolean spaceForEmptyValue) {
        if (spaceForEmptyValue && value.isEmpty()) {
            return " ";
        } else {
            return value;
        }
    }

    public abstract iPartsConstructionPrimaryKey cloneMe();

    public abstract String toString(String delimiter, boolean spaceForEmptyValue);

    public abstract String createGUID();
}
