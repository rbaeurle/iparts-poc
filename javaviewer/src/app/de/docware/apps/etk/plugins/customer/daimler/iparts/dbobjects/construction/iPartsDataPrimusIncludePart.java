package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für {@link #TABLE_DA_PRIMUS_INCLUDE_PART}.
 */
public class iPartsDataPrimusIncludePart extends EtkDataObject implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_PIP_PART_NO, FIELD_PIP_INCLUDE_PART_NO };

    public iPartsDataPrimusIncludePart(EtkProject project, iPartsPrimusIncludePartId id) {
        super(KEYS);
        tableName = TABLE_DA_PRIMUS_INCLUDE_PART;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsPrimusIncludePartId createId(String... idValues) {
        return new iPartsPrimusIncludePartId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsPrimusIncludePartId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsPrimusIncludePartId)id;
    }

}
