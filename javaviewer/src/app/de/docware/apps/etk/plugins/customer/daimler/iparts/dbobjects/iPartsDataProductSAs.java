/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsProductSAsId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_PRODUCT_SAS.
 */
public class iPartsDataProductSAs extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DPS_PRODUCT_NO, FIELD_DPS_SA_NO, FIELD_DPS_KG };

    public iPartsDataProductSAs(EtkProject project, iPartsProductSAsId id) {
        super(KEYS);
        tableName = TABLE_DA_PRODUCT_SAS;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsProductSAsId createId(String... idValues) {
        return new iPartsProductSAsId(idValues[0], idValues[1], idValues[2]);
    }

    @Override
    public iPartsProductSAsId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsProductSAsId)id;
    }
}
