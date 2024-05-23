package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für TABLE_DA_STRUCTURE_MBS.
 */
public class iPartsDataMBSStructure extends EtkDataObject implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DSM_SNR, FIELD_DSM_SNR_SUFFIX, FIELD_DSM_POS, FIELD_DSM_SORT, FIELD_DSM_KEM_FROM };

    public iPartsDataMBSStructure(EtkProject project, iPartsMBSStructureId id) {
        super(KEYS);
        tableName = TABLE_DA_STRUCTURE_MBS;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsMBSStructureId createId(String... idValues) {
        return new iPartsMBSStructureId(idValues[0], idValues[1], idValues[2], idValues[3], idValues[4]);
    }

    @Override
    public iPartsMBSStructureId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsMBSStructureId)id;
    }

    @Override
    public iPartsDataMBSStructure cloneMe(EtkProject project) {
        iPartsDataMBSStructure clone = new iPartsDataMBSStructure(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }
}
