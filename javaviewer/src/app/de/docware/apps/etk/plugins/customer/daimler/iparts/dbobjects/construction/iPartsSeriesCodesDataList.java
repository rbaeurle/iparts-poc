/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;

import java.util.HashSet;
import java.util.Set;

public class iPartsSeriesCodesDataList extends EtkDataObjectList<iPartsSeriesCodesData> implements iPartsConst {

    private static final String SPECIAL_GROUP = "AAM";
    private static final String FIRST_REGULATION_PREFIX = "I";
    private static final int REGULATION_PREFIX_LENGTH = 2;
    private static final boolean withRegulationTest = false;  // laut DAIMLER-6329 ist der Test der RegulationPrefix nicht nötig

    public iPartsSeriesCodesDataList() {
        setSearchWithoutActiveChangeSets(true);
    }

    public static String normalizeCode(String regulation) {
        if (StrUtils.isValid(regulation) && (regulation.length() >= REGULATION_PREFIX_LENGTH)) {
            return regulation.substring(REGULATION_PREFIX_LENGTH);
        }
        return regulation;
    }

    /**
     * Lädt alle Elemente zu <@link #seriesNo> und <@link #groupNo>
     * Sortiert nach allen PK's
     *
     * @param project
     * @param seriesNo
     * @param groupNo
     * @return
     */
    public static iPartsSeriesCodesDataList loadSeriesCodesDataForSeriesAndGroup(EtkProject project, String seriesNo, String groupNo) {
        iPartsSeriesCodesDataList list = new iPartsSeriesCodesDataList();
        list.loadSeriesCodesDataForSeries(project, seriesNo, groupNo, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle aktuell gültigen Elemente zu <@link #seriesNo> (SDatB leer)
     * Sortiert nach allen PK's
     *
     * @param project
     * @param seriesNo
     * @return
     */
    public static iPartsSeriesCodesDataList loadCurrentValidSeriesCodesDataForSeries(EtkProject project, String seriesNo) {
        iPartsSeriesCodesDataList list = new iPartsSeriesCodesDataList();
        list.loadCurrentValidSeriesCodesDataForSeries(project, seriesNo, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle Elemente zu <@link #seriesNo>
     * Sortiert nach allen PK's
     *
     * @param project
     * @param seriesNo
     * @return
     */
    public static iPartsSeriesCodesDataList loadAllSeriesCodesDataForSeries(EtkProject project, String seriesNo) {
        iPartsSeriesCodesDataList list = new iPartsSeriesCodesDataList();
        list.loadAllSeriesCodesDataForSeries(project, seriesNo, null, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Liefert die Liste aller gültigen Code zu einer Series (und Ausfuehrungsart)
     * Sortiert nach allen PK's
     *
     * @param project
     * @param seriesNo
     * @return
     */
    public static Set<String> loadAllSeriesCodesForSeries(EtkProject project, String seriesNo, String ausfuehrungsArt) {
        iPartsSeriesCodesDataList list = new iPartsSeriesCodesDataList();
        if (StrUtils.isValid(seriesNo)) {
            list.loadAllSeriesCodesDataForSeries(project, seriesNo, ausfuehrungsArt, DBActionOrigin.FROM_DB);
        }
        return normalizeAaOrCode(project, seriesNo, list);
    }

    private static String getRegulationPrefix(EtkProject project, String seriesNo) {
        String secondRegulationPrefix = "P";
        iPartsDialogSeries series = iPartsDialogSeries.getInstance(project, new iPartsSeriesId(seriesNo));
        if (series != null) {
            secondRegulationPrefix = series.getProductGroup();
        }
        return FIRST_REGULATION_PREFIX + secondRegulationPrefix;
    }

    /**
     * Liefert alle Ausführungsarten zur <link #seriesNo> die man z.B. bei einem Stücklistenbearbeitung auswählen darf.
     * Sortiert nach allen PK's
     *
     * @param project
     * @param seriesNo
     * @return
     */
    public static Set<String> loadAllAusfuehrungsarten(EtkProject project, String seriesNo) {
        iPartsSeriesCodesDataList list = new iPartsSeriesCodesDataList();
        list.loadSeriesCodesDataForSeries(project, seriesNo, SPECIAL_GROUP, DBActionOrigin.FROM_DB);
        return normalizeAaOrCode(project, seriesNo, list);
    }

    private static Set<String> normalizeAaOrCode(EtkProject project, String seriesNo, iPartsSeriesCodesDataList list) {
        Set<String> modelAASet = new HashSet<>();
        if (withRegulationTest) {
            String regulationPrefix = getRegulationPrefix(project, seriesNo);
            for (iPartsSeriesCodesData seriesCodesData : list) {
                String regulation = seriesCodesData.getRegulation();
                if (regulation.startsWith(regulationPrefix)) {
                    modelAASet.add(regulation.substring(regulationPrefix.length()));
                }
            }
        } else {
            for (iPartsSeriesCodesData seriesCodesData : list) {
                modelAASet.add(normalizeCode(seriesCodesData.getRegulation()));
            }
        }
        return modelAASet;
    }


    private void loadAllSeriesCodesDataForSeries(EtkProject project, String seriesNo, String ausfuehrungsArt, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DSC_SERIES_NO };
        String[] whereValues = new String[]{ seriesNo };

        if (StrUtils.isValid(ausfuehrungsArt)) {
            whereFields = StrUtils.mergeArrays(whereFields, new String[]{ FIELD_DSC_AA });
            whereValues = StrUtils.mergeArrays(whereValues, new String[]{ ausfuehrungsArt });
        }
        searchSortAndFillSeriesCodes(project, whereFields, whereValues, origin);
    }

    private void loadCurrentValidSeriesCodesDataForSeries(EtkProject project, String seriesNo, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DSC_SERIES_NO, FIELD_DSC_SDATB };
        String[] whereValues = new String[]{ seriesNo, "" };

        searchSortAndFillSeriesCodes(project, whereFields, whereValues, origin);
    }

    private void loadSeriesCodesDataForSeries(EtkProject project, String seriesNo, String groupNo, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DSC_SERIES_NO, FIELD_DSC_GROUP };
        String[] whereValues = new String[]{ seriesNo, groupNo };
        searchSortAndFillSeriesCodes(project, whereFields, whereValues, origin);
    }

    private void searchSortAndFillSeriesCodes(EtkProject project, String[] whereFields, String[] whereValues, DBActionOrigin origin) {
        String[] sortFields = new String[]{ FIELD_DSC_SERIES_NO, FIELD_DSC_GROUP, FIELD_DSC_POS, FIELD_DSC_POSV,
                                            FIELD_DSC_AA, FIELD_DSC_SDATA };
        searchSortAndFill(project, TABLE_DA_SERIES_CODES, whereFields, whereValues, sortFields, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsSeriesCodesData getNewDataObject(EtkProject project) {
        return new iPartsSeriesCodesData(project, null);
    }
}
