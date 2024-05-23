package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsNutzDokKEMId;
import de.docware.framework.modules.db.DBActionOrigin;

import java.util.List;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_NUTZDOK_KEM.
 */
public class iPartsDataNutzDokKEM extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DNK_KEM };

    public iPartsDataNutzDokKEM(EtkProject project, iPartsNutzDokKEMId id) {
        super(KEYS);
        tableName = TABLE_DA_NUTZDOK_KEM;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsNutzDokKEMId createId(String... idValues) {
        return new iPartsNutzDokKEMId(idValues[0]);
    }

    @Override
    public iPartsNutzDokKEMId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsNutzDokKEMId)id;
    }

    public List<String> getEtsEnumList() {
        return getFieldValueAsSetOfEnum(FIELD_DNK_ETS);
    }

    public List<String> getEtsUnconfirmedEnumList() {
        return getFieldValueAsSetOfEnum(FIELD_DNK_ETS_UNCONFIRMED);
    }

    public boolean hasEtsUnconfirmedValues() {
        return !getEtsUnconfirmedEnumList().isEmpty();
    }
}
