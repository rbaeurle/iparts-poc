/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataGenVoSuppText}.
 */
public class iPartsDataSpkMappingList extends EtkDataObjectList<iPartsDataSpkMapping> {

    /**
     * Lädt alle Werte aus der SPK Mapping Tabelle zur angegebenen Baureihe.
     *
     * @param project
     * @param seriesId
     * @param steering
     * @return
     */
    public static iPartsDataSpkMappingList loadSPKMappingForSeries(EtkProject project, iPartsSeriesId seriesId, String steering) {
        iPartsDataSpkMappingList list = new iPartsDataSpkMappingList();
        list.loadMappingForSeries(project, seriesId, steering, DBActionOrigin.FROM_DB);
        return list;
    }

    public static iPartsDataSpkMappingList loadMappingForShortNameAS(EtkProject project, String shortNameAS) {
        iPartsDataSpkMappingList list = new iPartsDataSpkMappingList();
        list.loadMappingForShortNameAS(project, shortNameAS, DBActionOrigin.FROM_DB);
        return list;
    }

    @Override
    protected iPartsDataSpkMapping getNewDataObject(EtkProject project) {
        return new iPartsDataSpkMapping(project, null);
    }

    private void loadMappingForShortNameAS(EtkProject project, String shortNameAS, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ iPartsConst.FIELD_SPKM_KURZ_AS };
        String[] whereValues = new String[]{ shortNameAS };

        // Die Felder SPKM_LANG_AS und SPK_LANG_E sind in der DB mehrsprachig, wobei SPK_LANG_E nur DE Texte enthält
        // Deshalb wird auch hier nur SPK_LANG_AS als Multilang abgefragt
        searchSortAndFillWithMultiLangValueForAllLanguages(project, null, iPartsConst.FIELD_SPKM_LANG_AS, whereFields, whereValues,
                                                           false, null, false);
    }

    private void loadMappingForSeries(EtkProject project, iPartsSeriesId seriesId, String steering, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ iPartsConst.FIELD_SPKM_SERIES_NO, iPartsConst.FIELD_SPKM_STEERING };
        String[] whereValues = new String[]{ seriesId.getSeriesNumber(), steering };

        // Die Felder SPKM_LANG_AS und SPK_LANG_E sind in der DB mehrsprachig, wobei SPK_LANG_E nur DE Texte enthält
        // Deshalb wird auch hier nur SPK_LANG_AS als Multilang abgefragt
        searchSortAndFillWithMultiLangValueForAllLanguages(project, null, iPartsConst.FIELD_SPKM_LANG_AS, whereFields, whereValues,
                                                           false, null, false);
    }
}
