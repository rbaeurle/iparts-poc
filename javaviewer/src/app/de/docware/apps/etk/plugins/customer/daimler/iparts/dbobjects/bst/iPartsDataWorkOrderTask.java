package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.misc.id.IdWithType;

public class iPartsDataWorkOrderTask extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DWT_BST_ID, FIELD_DWT_LFDNR };

    public iPartsDataWorkOrderTask(EtkProject project, iPartsWorkOrderTaskId id) {
        super(KEYS);
        tableName = TABLE_DA_WORKORDER_TASKS;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public IdWithType createId(String... idValues) {
        return new iPartsWorkOrderTaskId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsWorkOrderTaskId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsWorkOrderTaskId)id;
    }
}
