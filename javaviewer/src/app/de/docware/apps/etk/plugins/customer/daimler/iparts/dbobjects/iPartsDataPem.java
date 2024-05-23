package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_PEM_MASTERDATA.
 */
public class iPartsDataPem extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DPM_PEM, FIELD_DPM_FACTORY_NO };

    public iPartsDataPem(EtkProject project, iPartsPemId id) {
        super(KEYS);
        tableName = TABLE_DA_PEM_MASTERDATA;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    protected iPartsDataPem(String[] pkKeys) {
        super(pkKeys);
    }

    @Override
    public iPartsPemId createId(String... idValues) {
        return new iPartsPemId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsPemId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsPemId)id;
    }

    public iPartsImportDataOrigin getSource() {
        return iPartsImportDataOrigin.getTypeFromCode(getFieldValue(FIELD_DPM_SOURCE));
    }
}
