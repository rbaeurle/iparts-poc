/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.misc.id.IdWithType;

/**
 * Tabelle [DA_SERIES_EVENTS], Datenobjekt für die Ereignissteurung.
 */
public class iPartsDataSeriesEvent extends EtkDataObject implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DSE_SERIES_NO, FIELD_DSE_EVENT_ID, FIELD_DSE_SDATA };

    public iPartsDataSeriesEvent(EtkProject project, iPartsSeriesEventId id) {
        super(KEYS);
        tableName = TABLE_DA_SERIES_EVENTS;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }


    @Override
    public IdWithType createId(String... idValues) {
        return new iPartsSeriesEventId(idValues[0], idValues[1], idValues[2]);
    }

    @Override
    public iPartsSeriesEventId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsSeriesEventId)id;
    }

    public String getEventId() {
        return getAsId().getEventID();
    }

    public String getPreviousEventId() {
        return getFieldValue(FIELD_DSE_PREVIOUS_EVENT_ID);
    }
}
