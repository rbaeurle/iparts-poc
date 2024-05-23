package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsNutzDokSAAId;
import de.docware.framework.modules.db.DBActionOrigin;

import java.util.List;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_NUTZDOK_SAA.
 */
public class iPartsDataNutzDokSAA extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DNS_SAA };

    public iPartsDataNutzDokSAA(EtkProject project, iPartsNutzDokSAAId id) {
        super(KEYS);
        tableName = TABLE_DA_NUTZDOK_SAA;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsNutzDokSAAId createId(String... idValues) {
        return new iPartsNutzDokSAAId(idValues[0]);
    }

    @Override
    public iPartsNutzDokSAAId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsNutzDokSAAId)id;
    }

    public List<String> getEtsEnumList() {
        return getFieldValueAsSetOfEnum(FIELD_DNS_ETS);
    }

    public List<String> getEtsUnconfirmedEnumList() {
        return getFieldValueAsSetOfEnum(FIELD_DNS_ETS_UNCONFIRMED);
    }

    public boolean hasEtsUnconfirmedValues() {
        return !getEtsUnconfirmedEnumList().isEmpty();
    }
}
