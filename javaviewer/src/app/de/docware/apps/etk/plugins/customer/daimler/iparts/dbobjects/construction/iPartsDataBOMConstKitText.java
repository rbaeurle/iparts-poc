package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_EDS_CONST_PROPS.
 */
public class iPartsDataBOMConstKitText extends EtkDataObject implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DCP_SNR, FIELD_DCP_PARTPOS, FIELD_DCP_BTX_FLAG, FIELD_DCP_REVFROM };

    public iPartsDataBOMConstKitText(EtkProject project, iPartsBOMConstKitTextId id) {
        super(KEYS);
        tableName = TABLE_DA_EDS_CONST_PROPS;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsBOMConstKitTextId createId(String... idValues) {
        return new iPartsBOMConstKitTextId(idValues[0], idValues[1], idValues[2], idValues[3]);
    }

    @Override
    public iPartsBOMConstKitTextId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsBOMConstKitTextId)id;
    }
}
