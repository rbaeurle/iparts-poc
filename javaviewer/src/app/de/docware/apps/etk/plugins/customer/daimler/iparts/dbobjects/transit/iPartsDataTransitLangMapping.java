package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.transit;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_TRANSIT_LANG_MAPPING.
 */
public class iPartsDataTransitLangMapping extends EtkDataObject implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DA_TLM_TRANSIT_LANGUAGE };

    public iPartsDataTransitLangMapping(EtkProject project, iPartsTransitLangMappingId id) {
        super(KEYS);
        tableName = TABLE_DA_TRANSIT_LANG_MAPPING;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsTransitLangMappingId createId(String... idValues) {
        return new iPartsTransitLangMappingId(idValues[0]);
    }

    @Override
    public iPartsTransitLangMappingId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsTransitLangMappingId)id;
    }

    public String getIsoLang() {
        return getFieldValue(FIELD_DA_TLM_ISO_LANGUAGE);
    }
}
