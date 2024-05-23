/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_FACTORY_MODEL.
 */
public class iPartsDataFactoryModel extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DFM_WMI, FIELD_DFM_FACTORY_SIGN, FIELD_DFM_FACTORY, FIELD_DFM_MODEL_PREFIX,
                                                       FIELD_DFM_ADD_FACTORY, FIELD_DFM_AGG_TYPE, FIELD_DFM_BELT_SIGN, FIELD_DFM_BELT_GROUPING };

    public iPartsDataFactoryModel(EtkProject project, iPartsFactoryModelId id) {
        super(KEYS);
        tableName = TABLE_DA_FACTORY_MODEL;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsFactoryModelId createId(String... idValues) {
        return new iPartsFactoryModelId(idValues[0], idValues[1], idValues[2], idValues[3], idValues[4], idValues[5],
                                        idValues[6], idValues[7]);
    }

    @Override
    public iPartsFactoryModelId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsFactoryModelId)id;
    }

    /**
     * Handelt es sich um einen Datensatz mit Bandsteuerung?
     *
     * @return
     */
    public boolean hasBeltControl() {
        // Bandsteuerung vorhanden falls eine Bandkennzahl existiert
        return !getAsId().getBeltSign().isEmpty();
    }

    /**
     * Handelt es sich um einen Datensatz mit WKB-Gruppierung?
     *
     * @return
     */
    public boolean hasFactorySignGrouping() {
        return !getFactorySignGroup().isEmpty();
    }

    public String getFactorySignGroup() {
        return getFieldValue(FIELD_DFM_FACTORY_SIGN_GROUPING);
    }
}
