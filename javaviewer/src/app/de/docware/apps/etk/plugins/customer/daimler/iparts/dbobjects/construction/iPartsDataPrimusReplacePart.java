package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObject;
import de.docware.framework.modules.db.DBDataObjectList;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für {@link #TABLE_DA_PRIMUS_REPLACE_PART}.
 */
public class iPartsDataPrimusReplacePart extends EtkDataObject implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_PRP_PART_NO };

    public static final String CHILDREN_NAME_INCLUDE_PARTS = "iPartsDataPrimusReplacePart.includeParts";

    private iPartsDataPrimusIncludePartList includeParts;

    public iPartsDataPrimusReplacePart(EtkProject project, iPartsPrimusReplacePartId id) {
        super(KEYS);
        tableName = TABLE_DA_PRIMUS_REPLACE_PART;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsPrimusReplacePartId createId(String... idValues) {
        return new iPartsPrimusReplacePartId(idValues[0]);
    }

    @Override
    public iPartsPrimusReplacePartId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsPrimusReplacePartId)id;
    }

    @Override
    public void setChildren(String childrenName, DBDataObjectList<? extends DBDataObject> children) {
        super.setChildren(childrenName, children);
        if (childrenName.equals(CHILDREN_NAME_INCLUDE_PARTS)) {
            includeParts = (iPartsDataPrimusIncludePartList)children;
        }
    }

    @Override
    public iPartsDataPrimusReplacePart cloneMe(EtkProject project) {
        iPartsDataPrimusReplacePart clone = new iPartsDataPrimusReplacePart(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

    @Override
    public void clear(DBActionOrigin origin) {
        super.clear(origin);
        setChildren(CHILDREN_NAME_INCLUDE_PARTS, null);
    }

    protected synchronized void loadIncludeParts() {
        if (includeParts != null) {
            return;
        }
        setChildren(CHILDREN_NAME_INCLUDE_PARTS, iPartsDataPrimusIncludePartList.loadIncludePartsForReplacement(getEtkProject(), getAsId()));
    }

    public iPartsDataPrimusIncludePartList getIncludeParts() {
        loadIncludeParts();
        return includeParts;
    }

}
