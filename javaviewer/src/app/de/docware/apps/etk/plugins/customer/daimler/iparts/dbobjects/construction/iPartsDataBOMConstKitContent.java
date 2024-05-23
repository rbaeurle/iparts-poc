/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_EDS_CONST_KIT.
 */
public class iPartsDataBOMConstKitContent extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DCK_SNR, FIELD_DCK_PARTPOS, FIELD_DCK_REVFROM };

    public iPartsDataBOMConstKitContent(EtkProject project, iPartsBOMConstKitContentId id) {
        super(KEYS);
        tableName = TABLE_DA_EDS_CONST_KIT;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsBOMConstKitContentId createId(String... idValues) {
        return new iPartsBOMConstKitContentId(idValues[0], idValues[1], idValues[2]);
    }

    @Override
    public iPartsBOMConstKitContentId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsBOMConstKitContentId)id;
    }

    // Convenience Methods
    public String getKemFrom() {
        return getFieldValue(FIELD_DCK_KEMFROM);
    }

    public String getKemTo() {
        return getFieldValue(FIELD_DCK_KEMTO);
    }

    public String getReleaseDateFrom() {
        return getFieldValue(FIELD_DCK_RELEASE_FROM);
    }

    public String getReleaseDateTo() {
        return getFieldValue(FIELD_DCK_RELEASE_TO);
    }
}
