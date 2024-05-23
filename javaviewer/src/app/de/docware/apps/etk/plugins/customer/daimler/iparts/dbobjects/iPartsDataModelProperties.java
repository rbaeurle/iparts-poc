/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelPropertiesId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_MODEL_PROPERTIES
 */
public class iPartsDataModelProperties extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DMA_MODEL_NO, FIELD_DMA_DATA };

    public iPartsDataModelProperties(EtkProject project, iPartsModelPropertiesId id) {
        super(KEYS);
        tableName = TABLE_DA_MODEL_PROPERTIES;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsModelPropertiesId createId(String... idValues) {
        return new iPartsModelPropertiesId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsModelPropertiesId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsModelPropertiesId)id;
    }
}
