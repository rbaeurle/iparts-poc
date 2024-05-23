package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für TABLE_DA_PARTSLIST_MBS.
 */
public class iPartsDataMBSPartlist extends EtkDataObject implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DPM_SNR, FIELD_DPM_POS, FIELD_DPM_SORT, FIELD_DPM_KEM_FROM };

    public iPartsDataMBSPartlist(EtkProject project, iPartsMBSPartlistId id) {
        super(KEYS);
        tableName = TABLE_DA_PARTSLIST_MBS;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsMBSPartlistId createId(String... idValues) {
        return new iPartsMBSPartlistId(idValues[0], idValues[1], idValues[2], idValues[3]);
    }

    @Override
    public iPartsMBSPartlistId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsMBSPartlistId)id;
    }

    public String getKemFrom() {
        return getAsId().getKemFrom();
    }

    public String getKemTo() {
        return getFieldValue(FIELD_DPM_KEM_TO);
    }

    public String getReleaseFrom() {
        return getFieldValue(FIELD_DPM_RELEASE_FROM);
    }

    public String getReleaseTo() {
        return getFieldValue(FIELD_DPM_RELEASE_TO);
    }
}
