/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsReplaceConstMatId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObject;
import de.docware.framework.modules.db.DBDataObjectList;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_REPLACE_CONST_MAT.
 */
public class iPartsDataReplaceConstMat extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DRCM_PART_NO, FIELD_DRCM_SDATA };

    public static final String CHILDREN_NAME_INCLUDES = "iPartsDataReplaceConstMat.includeConstMats";

    protected iPartsDataIncludeConstMatList includeConstMatList;

    public iPartsDataReplaceConstMat(EtkProject project, iPartsReplaceConstMatId id) {
        super(KEYS);
        tableName = TABLE_DA_REPLACE_CONST_MAT;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsReplaceConstMatId createId(String... idValues) {
        return new iPartsReplaceConstMatId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsReplaceConstMatId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsReplaceConstMatId)id;
    }

    @Override
    public void setChildren(String childrenName, DBDataObjectList<? extends DBDataObject> children) {
        super.setChildren(childrenName, children);
        if (childrenName.equals(CHILDREN_NAME_INCLUDES)) {
            includeConstMatList = (iPartsDataIncludeConstMatList)children;
        }
    }

    @Override
    public iPartsDataReplaceConstMat cloneMe(EtkProject project) {
        iPartsDataReplaceConstMat clone = new iPartsDataReplaceConstMat(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

    @Override
    public void clear(DBActionOrigin origin) {
        super.clear(origin);
        setChildren(CHILDREN_NAME_INCLUDES, null);
    }

    protected synchronized void loadIncludeConstMat() {
        if (includeConstMatList != null) {
            return;
        }

        setChildren(CHILDREN_NAME_INCLUDES, iPartsDataIncludeConstMatList.loadIncludeList(getEtkProject(), getAsId()));
    }

    public iPartsDataIncludeConstMatList getIncludeConstMats() {
        loadIncludeConstMat();
        return includeConstMatList;
    }

    public synchronized void addIncludeConstMat(iPartsDataIncludeConstMat dataIncludeConstMat, DBActionOrigin origin) {
        getIncludeConstMats().add(dataIncludeConstMat, origin);
    }
}
