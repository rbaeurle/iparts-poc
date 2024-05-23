/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;

import java.util.HashMap;
import java.util.Map;

/**
 * Liste von {@link iPartsDataResponseSpike}.
 */
public class iPartsDataResponseSpikeList extends EtkDataObjectList<iPartsDataResponseSpike> implements iPartsConst {

    /**
     * Alle Ausreißer zu einer PEM (DIALOG und MAD) aus DB lesen. Wir verwenden eine Hashtable damit wir schnell über die
     * ID darauf zugreifen können.
     *
     * @param project
     * @param pemNumber
     * @return
     */
    public static Map<iPartsResponseSpikeId, iPartsDataResponseSpike> loadResponseSpikesMapForPEMAndSeries(EtkProject project, String pemNumber, String series) {
        iPartsDataResponseSpikeList list = new iPartsDataResponseSpikeList();
        list.loadResponseSpikesForPEMAndSeries(project, pemNumber, series, DBActionOrigin.FROM_DB);
        Map<iPartsResponseSpikeId, iPartsDataResponseSpike> map = new HashMap<iPartsResponseSpikeId, iPartsDataResponseSpike>();
        for (iPartsDataResponseSpike responseSpike : list.getAsList()) {
            map.put(responseSpike.getAsId(), responseSpike);
        }
        return map;
    }

    /**
     * Alle Ausreißer zu einer PEM und "Fahrzeugident ab" (DIALOG und MAD) aus DB lesen.
     *
     * @param project
     * @param pemNumber
     * @param identFrom
     * @return
     */
    public static iPartsDataResponseSpikeList loadResponseSpikesListForPEMAndIdentFrom(EtkProject project, String pemNumber, String identFrom) {
        iPartsDataResponseSpikeList list = new iPartsDataResponseSpikeList();
        list.loadResponseSpikesForPEMAndIdentFrom(project, pemNumber, identFrom, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataResponseSpike}s für die übergebene Baureihe und Quelle
     *
     * @param project
     * @param seriesNo
     * @param dataOrigin
     * @return
     */
    public static iPartsDataResponseSpikeList loadResponseSpikesForSeriesAndSource(EtkProject project, String seriesNo, iPartsImportDataOrigin dataOrigin) {
        iPartsDataResponseSpikeList responseSpikeList = new iPartsDataResponseSpikeList();
        responseSpikeList.loadAllDataForSeriesAndOrigin(project, seriesNo, dataOrigin, DBActionOrigin.FROM_DB);
        return responseSpikeList;
    }

    private void loadAllDataForSeriesAndOrigin(EtkProject project, String seriesNo, iPartsImportDataOrigin dataOrigin, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_RESPONSE_SPIKES, new String[]{ FIELD_DRS_SERIES_NO, FIELD_DRS_SOURCE },
                      new String[]{ seriesNo, dataOrigin.getOrigin() }, LoadType.ONLY_IDS, origin);
    }

    /**
     * Alle Ausreißer zu einer PEM (DIALOG und MAD) aus der DB lesen.
     *
     * @param project
     * @param pemNumber
     * @param origin
     */
    public void loadResponseSpikesForPEMAndSeries(EtkProject project, String pemNumber, String series, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_RESPONSE_SPIKES, new String[]{ FIELD_DRS_PEM, FIELD_DRS_SERIES_NO },
                      new String[]{ pemNumber, series },
                      LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    /**
     * Alle Ausreißer zu einer PEM und "Fahrzeugident ab" (DIALOG und MAD) aus der DB lesen.
     *
     * @param project
     * @param pemNumber
     * @param identFrom
     * @param origin
     */
    public void loadResponseSpikesForPEMAndIdentFrom(EtkProject project, String pemNumber, String identFrom, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_RESPONSE_SPIKES, new String[]{ FIELD_DRS_PEM, FIELD_DRS_IDENT },
                      new String[]{ pemNumber, identFrom },
                      LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    /**
     * Lädt alle Daten aus TABLE_DA_RESPONSE_SPIKES unsortiert
     *
     * @param project
     */
    public void load(EtkProject project) {
        super.searchAndFill(project, TABLE_DA_RESPONSE_SPIKES, null, null, DBDataObjectList.LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }


    @Override
    protected iPartsDataResponseSpike getNewDataObject(EtkProject project) {
        return new iPartsDataResponseSpike(project, null);
    }
}