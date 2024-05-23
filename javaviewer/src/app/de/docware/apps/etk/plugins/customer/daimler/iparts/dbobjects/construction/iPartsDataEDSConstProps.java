/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_EDS_CONST_PROPS.
 */
public class iPartsDataEDSConstProps extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DCP_SNR, FIELD_DCP_PARTPOS, FIELD_DCP_KEMFROM };

    public iPartsDataEDSConstProps(EtkProject project, iPartsEDSConstPropsId id) {
        super(KEYS);
        tableName = TABLE_DA_EDS_CONST_PROPS;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsEDSConstPropsId createId(String... idValues) {
        return new iPartsEDSConstPropsId(idValues[0], idValues[1], idValues[2]);
    }

    @Override
    public iPartsEDSConstPropsId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsEDSConstPropsId)id;
    }

}
