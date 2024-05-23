/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine ID aus der Tabelle DA_REPORT_CONST_NODES im iParts Plug-in.
 */
public class iPartsReportConstNodeId extends IdWithType {

    public static final String TYPE = "DA_iPartsReportConstNodeId";

    protected enum INDEX {SERIES_NO, NODE_ID, CHANGESET_GUID}

    public iPartsReportConstNodeId(String seriesNo, String nodeId, String changeSetGuid) {
        super(TYPE, new String[]{ seriesNo, nodeId, changeSetGuid });
    }

    /**
     * Erzeugt eine neue ID mit dem (optional) aktiven {@link de.docware.apps.etk.base.db.AbstractRevisionChangeSet} als
     * ChangeSet-GUID.
     *
     * @param seriesNo
     * @param nodeId
     * @param project
     */
    public iPartsReportConstNodeId(String seriesNo, String nodeId, EtkProject project) {
        this(seriesNo, nodeId, project.getActiveChangeSetGuidAsDbValue());
    }

    public iPartsReportConstNodeId(HmMSmId hmMSmId, EtkProject project) {
        this(hmMSmId.getSeries(), hmMSmId.toDBString(), project);
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsReportConstNodeId() {
        this("", "", "");
    }

    public String getSeriesNo() {
        return id[INDEX.SERIES_NO.ordinal()];
    }

    public String getNodeId() {
        return id[INDEX.NODE_ID.ordinal()];
    }

    public String getChangesetGuid() {
        return id[INDEX.CHANGESET_GUID.ordinal()];
    }
}
