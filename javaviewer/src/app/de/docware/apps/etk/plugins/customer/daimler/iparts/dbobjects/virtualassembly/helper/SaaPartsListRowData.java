/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.helper;

import de.docware.framework.modules.db.DBDataObjectAttributes;

public class SaaPartsListRowData {

    private final DBDataObjectAttributes constKitAttributes = new DBDataObjectAttributes();
    private final DBDataObjectAttributes matAttributes = new DBDataObjectAttributes();
    private boolean hasSubStruct = false;
    private String saaNo = "";
    private String constKitPath;
    private SaaPartsListConstKitData subPartListEntries;

    public DBDataObjectAttributes getConstKitAttributes() {
        return constKitAttributes;
    }

    public DBDataObjectAttributes getMatAttributes() {
        return matAttributes;
    }

    public void setHasSubStruct(boolean hasSubStruct) {
        this.hasSubStruct = hasSubStruct;
    }

    public boolean hasSubStruct() {
        return hasSubStruct;
    }

    public void setSaaNo(String saaNo) {
        this.saaNo = saaNo;
    }

    public String getSaaNo() {
        return saaNo;
    }

    public void setConstKitPath(String constKitPath) {
        this.constKitPath = constKitPath;
    }

    public String getConstKitPath() {
        return constKitPath;
    }

    public void setSubPartListEntries(SaaPartsListConstKitData subPartListEntries) {
        this.subPartListEntries = subPartListEntries;
    }

    public SaaPartsListConstKitData getSubPartListEntries() {
        return subPartListEntries;
    }
}
