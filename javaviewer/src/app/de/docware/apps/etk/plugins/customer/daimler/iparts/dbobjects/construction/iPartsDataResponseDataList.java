/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNote;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;

import java.util.HashMap;
import java.util.Map;

/**
 * Liste von {@link iPartsDataResponseData}.
 */
public class iPartsDataResponseDataList extends EtkDataObjectList<iPartsDataResponseData> implements iPartsConst {

    /**
     * Alle Rückmeldedaten zu einer PEM (DIALOG oder MAD) aus DB lesen. Wir verwenden eine Hashtable damit wir schnell über die
     * ID darauf zugreifen können.
     *
     * @param project
     * @param pemNumber
     * @return
     */
    public static Map<iPartsResponseDataId, iPartsDataResponseData> loadResponseDataMapForPEMAndSeries(EtkProject project, String pemNumber, String series) {
        iPartsDataResponseDataList list = new iPartsDataResponseDataList();
        list.loadResponseDataForPEMAndSeries(project, pemNumber, series, DBActionOrigin.FROM_DB);
        Map<iPartsResponseDataId, iPartsDataResponseData> map = new HashMap<iPartsResponseDataId, iPartsDataResponseData>();
        for (iPartsDataResponseData responseData : list.getAsList()) {
            map.put(responseData.getAsId(), responseData);
        }
        return map;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataResponseData}s für die übergebene Baureihe und Quelle
     *
     * @param project
     * @param seriesNo
     * @param dataOrigin
     * @return
     */
    public static iPartsDataResponseDataList loadResponseDataForSeriesAndSource(EtkProject project, String seriesNo, iPartsImportDataOrigin dataOrigin) {
        iPartsDataResponseDataList responseDataList = new iPartsDataResponseDataList();
        responseDataList.loadAllDataForSeriesAndOrigin(project, seriesNo, dataOrigin, DBActionOrigin.FROM_DB);
        return responseDataList;
    }

    private void loadAllDataForSeriesAndOrigin(EtkProject project, String seriesNo, iPartsImportDataOrigin dataOrigin, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_RESPONSE_DATA, new String[]{ FIELD_DRD_SERIES_NO, FIELD_DRD_SOURCE },
                      new String[]{ seriesNo, dataOrigin.getOrigin() }, LoadType.ONLY_IDS, origin);
    }


    public void loadResponseDataForPEMAndSeries(EtkProject project, String pemNumber, String series, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_RESPONSE_DATA, new String[]{ FIELD_DRD_PEM, FIELD_DRD_SERIES_NO }, new String[]{ pemNumber, series }, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    /**
     * Alle Rückmeldedaten zu einer PEM aus DB lesen.
     *
     * @param project
     * @param pemNumber
     * @return
     */
    public static iPartsDataResponseDataList loadResponseDataListForPEM(EtkProject project, String pemNumber) {
        iPartsDataResponseDataList list = new iPartsDataResponseDataList();
        list.loadResponseDataForPEM(project, pemNumber, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle Daten aus TABLE_DA_RESPONSE_DATA unsortiert
     *
     * @param project
     */
    public void load(EtkProject project) {
        super.searchAndFill(project, TABLE_DA_RESPONSE_DATA, null, null, DBDataObjectList.LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    /**
     * @param project
     * @param pemNumber
     * @param origin
     */
    public void loadResponseDataForPEM(EtkProject project, String pemNumber, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_RESPONSE_DATA, new String[]{ FIELD_DRD_PEM }, new String[]{ pemNumber }, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    /**
     * Lädt eine komplette Liste aller ELDAS-{@link iPartsDataResponseData}s zum angegebenen Produkt oder SA.
     *
     * @param project
     * @param productOrSANumber
     * @return
     */
    public void loadELDASResponseDataListForProductOrSAFromDB(EtkProject project, String productOrSANumber) {
        loadSpecialCreatedResponseDataListForProductOrSAFromDB(project, productOrSANumber, iPartsDataFootNote.FOOTNOTE_PREFIX_ELDAS);
    }

    /**
     * Lädt eine komplette Liste aller EPC-{@link iPartsDataResponseData}s zum angegebenen Produkt oder SA.
     *
     * @param project
     * @param productOrSANumber
     * @return
     */
    public void loadEPCResponseDataListForProductOrSAFromDB(EtkProject project, String productOrSANumber) {
        loadSpecialCreatedResponseDataListForProductOrSAFromDB(project, productOrSANumber, iPartsDataFootNote.FOOTNOTE_PREFIX_EPC);
    }

    public void loadSpecialCreatedResponseDataListForProductOrSAFromDB(EtkProject project, String productOrSANumber, String prefix) {
        clear(DBActionOrigin.FROM_DB);
        // Pattern PseudoPEM ist von Fußnoten übernommen
        String responseDataIdPattern = prefix + iPartsDataFootNote.FOOTNOTE_ID_DELIMITER
                                       + productOrSANumber + iPartsDataFootNote.FOOTNOTE_ID_DELIMITER + "*";
        searchWithWildCardsSortAndFill(project, new String[]{ FIELD_DRD_PEM }, new String[]{ responseDataIdPattern }, null,
                                       LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }


    @Override
    protected iPartsDataResponseData getNewDataObject(EtkProject project) {
        return new iPartsDataResponseData(project, null);
    }

}
