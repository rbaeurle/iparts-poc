package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für DA_GENVO_SUPP_TEXT.
 */
public class iPartsDataGenVoSuppText extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DA_GENVO_NO };

    public iPartsDataGenVoSuppText(EtkProject project, iPartsGenVoSuppTextId id) {
        super(KEYS);
        tableName = TABLE_DA_GENVO_SUPP_TEXT;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsGenVoSuppTextId createId(String... idValues) {
        return new iPartsGenVoSuppTextId(idValues[0]);
    }

    @Override
    public iPartsGenVoSuppTextId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsGenVoSuppTextId)id;
    }
}
