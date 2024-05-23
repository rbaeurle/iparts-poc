/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.misc.MessageLogType;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_SERIES.
 */
public class iPartsDataSeries extends EtkDataObject implements EtkDbConst, iPartsConst {

    public static final String CHILDREN_NAME_SERIES_SOP = "iPartsDataSeries.seriesSOP";
    public static final String CHILDREN_NAME_SERIES_EXP_DATES = "iPartsDataSeries.seriesExpDates";

    static private final String[] KEYS = new String[]{ FIELD_DS_SERIES_NO };

    public iPartsDataSeries(EtkProject project, iPartsSeriesId id) {
        super(KEYS);
        tableName = TABLE_DA_SERIES;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsSeriesId createId(String... idValues) {
        return new iPartsSeriesId(idValues[0]);
    }

    @Override
    public iPartsSeriesId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsSeriesId)id;
    }

    public void clear() {
        setChildren(CHILDREN_NAME_SERIES_SOP, null);
        setChildren(CHILDREN_NAME_SERIES_EXP_DATES, null);
    }

    public void loadChildren() {
        getSeriesSOPList();
        getSeriesExpireDateList();
    }

    public DBDataObjectList<iPartsDataSeriesSOP> getSeriesSOPList() {
        DBDataObjectList<iPartsDataSeriesSOP> seriesSOPList = (DBDataObjectList<iPartsDataSeriesSOP>)getChildren(CHILDREN_NAME_SERIES_SOP);
        if (seriesSOPList == null) {
            seriesSOPList = iPartsDataSeriesSOPList.getAllSeriesSOP(getEtkProject(), getAsId());
            setChildren(CHILDREN_NAME_SERIES_SOP, seriesSOPList);
        }
        return seriesSOPList;
    }

    public DBDataObjectList<iPartsDataSeriesExpireDate> getSeriesExpireDateList() {
        DBDataObjectList<iPartsDataSeriesExpireDate> seriesExpireDateList = (DBDataObjectList<iPartsDataSeriesExpireDate>)getChildren(CHILDREN_NAME_SERIES_EXP_DATES);
        if (seriesExpireDateList == null) {
            seriesExpireDateList = iPartsDataSeriesExpireDateList.getAllSeriesExpireDate(getEtkProject(), getAsId());
            setChildren(CHILDREN_NAME_SERIES_EXP_DATES, seriesExpireDateList);
        }
        return seriesExpireDateList;
    }

    public DBDataObjectList<iPartsDataSeriesExpireDate> getSeriesExpireDateForAAList(String seriesAA) {
        DBDataObjectList<iPartsDataSeriesExpireDate> seriesExpireDateList = getSeriesExpireDateList();
        DBDataObjectList<iPartsDataSeriesExpireDate> resultList = new DBDataObjectList<iPartsDataSeriesExpireDate>();
        for (iPartsDataSeriesExpireDate dataSeriesExpireDate : seriesExpireDateList) {
            if (dataSeriesExpireDate.getAsId().getSeriesAA().equals(seriesAA)) {
                resultList.add(dataSeriesExpireDate, DBActionOrigin.FROM_DB);
            }
        }
        return resultList;
    }

    @Override
    public void deleteFromDB(boolean forceDelete) {
        if (forceDelete || !isNew()) { // ein neuer Datensatz muss keine Kindelemente aus der DB laden
            loadChildren();
        }
        super.deleteFromDB(forceDelete);
    }

    /**
     * Überprüfung, ob die ID noch in weiteren Tabellen verwendet wird
     *
     * @return
     */
    public boolean hasDependencies() {
        // Ist die Baureihe einem Produkt als referenzierte Baureihe zugeordnet?
        if (!iPartsProductHelper.getProductsForSeries(getEtkProject(), getAsId(), null, null).isEmpty()) {
            return true;
        }

        // Gibt es ein Baumuster für die Baureihe?
        // Auskommentiert, weil wir aktuell das Löschen von Baureihen mit Baumustern zulassen und die dazugehörigen Baumuster
        // sogar explizit löschen
//        dataSet = getEtkProject().getDB().createQueryEx(TABLE_DA_MODEL, new String[]{ FIELD_DM_SERIES_NO },
//                                                        new String[]{ FIELD_DM_SERIES_NO }, new String[]{ getAsId().getSeriesNumber() },
//                                                        null, 1, false, false);
//        try {
//            if (dataSet.next()) {
//                return true;
//            }
//        } finally {
//            dataSet.close();
//        }

        return false;
    }

    /**
     * Löscht die Baureihe aus der Datenbank inkl. aller Konstruktions- und After-Sales-Baumuster für diese Baureihe und
     * weiterer Referenzen. Das Löschen kann auch abgebrochen werden, wenn der aufrufende Thread bei {@link Thread#isInterrupted()})
     * {@code true} zurückliefert; in diesem Fall liefert diese Methode {@code false} zurück.
     *
     * @param forceDelete
     * @param messageLog  Optionales {@link de.docware.apps.etk.base.project.base.EtkMessageLog} für Fortschrittsausgaben
     * @return {@code true} falls das Produkt vollständig gelöscht wurde
     */
    public boolean deleteFromDBWithModels(boolean forceDelete, EtkMessageLog messageLog) {
        String seriesNumber = getAsId().getSeriesNumber();
        iPartsDataModelPropertiesList modelPropertiesList = iPartsDataModelPropertiesList.loadDataModelPropertiesListForSeries(getEtkProject(),
                                                                                                                               seriesNumber,
                                                                                                                               DBDataObjectList.LoadType.ONLY_IDS);
        iPartsDataModelList modelList = iPartsDataModelList.loadDataModelList(getEtkProject(), seriesNumber, DBDataObjectList.LoadType.ONLY_IDS);

        getEtkProject().getDbLayer().startBatchStatement();
        try {
            // * 2, um für das Löschen der restlichen Referenzen auch schon einen Fortschritt anzuzeigen
            int progressMaxValue = modelList.size() + modelPropertiesList.size() * 2;
            if (messageLog != null) {
                messageLog.fireProgress(0, progressMaxValue, "", true, false);
            }

            super.deleteFromDB(forceDelete); // Baureihe löschen

            // Alle Baureihen-Referenzen, die von Dialog kommen löschen
            // nicht von Dialog kommen: TABLE_DA_EINPASHMMSM (Mapping Einpas, benutzerdefinierter Import), TABLE_DA_PRODUCT_SERIES (Editor Zuordnung Produkt zu Baureihe)

            getEtkProject().getDbLayer().delete(TABLE_DA_CODE, new String[]{ FIELD_DC_SERIES_NO }, new String[]{ seriesNumber });
            getEtkProject().getDbLayer().delete(TABLE_DA_COLORTABLE_DATA, new String[]{ FIELD_DCTD_VALID_SERIES }, new String[]{ seriesNumber });
            getEtkProject().getDbLayer().delete(TABLE_DA_DIALOG, new String[]{ FIELD_DD_SERIES_NO }, new String[]{ seriesNumber });
            getEtkProject().getDbLayer().delete(TABLE_DA_DIALOG_PARTLIST_TEXT, new String[]{ FIELD_DD_PLT_BR }, new String[]{ seriesNumber });
            getEtkProject().getDbLayer().delete(TABLE_DA_DIALOG_POS_TEXT, new String[]{ FIELD_DD_POS_BR }, new String[]{ seriesNumber });
            getEtkProject().getDbLayer().delete(TABLE_DA_FACTORY_DATA, new String[]{ FIELD_DFD_SERIES_NO }, new String[]{ seriesNumber });
            getEtkProject().getDbLayer().delete(TABLE_DA_HMMSM, new String[]{ FIELD_DH_SERIES_NO }, new String[]{ seriesNumber });
            getEtkProject().getDbLayer().delete(TABLE_DA_HMMSMDESC, new String[]{ FIELD_DH_SERIES_NO }, new String[]{ seriesNumber });
            getEtkProject().getDbLayer().delete(TABLE_DA_RESPONSE_DATA, new String[]{ FIELD_DRD_SERIES_NO }, new String[]{ seriesNumber });
            getEtkProject().getDbLayer().delete(TABLE_DA_RESPONSE_SPIKES, new String[]{ FIELD_DRS_SERIES_NO }, new String[]{ seriesNumber });
            getEtkProject().getDbLayer().delete(TABLE_DA_SERIES_AGGS, new String[]{ FIELD_DSA_SERIES_NO }, new String[]{ seriesNumber });
            getEtkProject().getDbLayer().delete(TABLE_DA_VS2US_RELATION, new String[]{ FIELD_VUR_VEHICLE_SERIES }, new String[]{ seriesNumber });

            // 50% Fortschritt
            int progress = progressMaxValue / 2;
            if (messageLog != null) {
                messageLog.fireProgress(progress, progressMaxValue, "", true, true);
            }

            // Konstruktions-Baumuster löschen
            for (iPartsDataModelProperties dataModel : modelPropertiesList) {
                if (Thread.currentThread().isInterrupted()) {
                    if (messageLog != null) {
                        messageLog.hideProgress();
                        messageLog.fireMessage("!!Löschen von der Baureihe abgebrochen, da der Thread frühzeitig beendet wurde",
                                               MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
                    }
                    return false;
                }

                dataModel.deleteFromDB(forceDelete);

                // Fahrzeug- und Aggregatebaumuster aus DA_MODELS_AGGS löschen
                String modelNumber = dataModel.getAsId().getModelNumber();
                getEtkProject().getDbLayer().delete(TABLE_DA_MODELS_AGGS, new String[]{ FIELD_DMA_MODEL_NO }, new String[]{ modelNumber });
                getEtkProject().getDbLayer().delete(TABLE_DA_MODELS_AGGS, new String[]{ FIELD_DMA_AGGREGATE_NO }, new String[]{ modelNumber });

                // Baumusterdaten aus DA_MODEL_DATA löschen
                getEtkProject().getDbLayer().delete(TABLE_DA_MODEL_DATA, new String[]{ FIELD_DMD_MODEL_NO }, new String[]{ modelNumber });

                progress++;
                if (messageLog != null) {
                    messageLog.fireProgress(progress, progressMaxValue, "", true, true);
                }
            }

            // After-Sales-Baumuster löschen
            for (iPartsDataModel dataModel : modelList) {
                if (Thread.currentThread().isInterrupted()) {
                    if (messageLog != null) {
                        messageLog.hideProgress();
                        messageLog.fireMessage("!!Löschen von der Baureihe abgebrochen, da der Thread frühzeitig beendet wurde",
                                               MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
                    }
                    return false;
                }

                dataModel.deleteFromDB(forceDelete);

                // Fahrzeug- und Aggregatebaumuster aus DA_MODELS_AGGS löschen
                String modelNumber = dataModel.getAsId().getModelNumber();
                getEtkProject().getDbLayer().delete(TABLE_DA_MODELS_AGGS, new String[]{ FIELD_DMA_MODEL_NO }, new String[]{ modelNumber });
                getEtkProject().getDbLayer().delete(TABLE_DA_MODELS_AGGS, new String[]{ FIELD_DMA_AGGREGATE_NO }, new String[]{ modelNumber });

                // Baumuster-Referenzen aus DA_PRODUCT_MODELS löschen
                getEtkProject().getDbLayer().delete(TABLE_DA_PRODUCT_MODELS, new String[]{ FIELD_DPM_MODEL_NO }, new String[]{ modelNumber });

                progress++;
                if (messageLog != null) {
                    messageLog.fireProgress(progress, progressMaxValue, "", true, true);
                }
            }

            if (messageLog != null) {
                messageLog.hideProgress();
            }

            return true;
        } finally {
            getEtkProject().getDbLayer().endBatchStatement();
        }
    }

    public boolean isImportRelevant() {
        return getFieldValueAsBoolean(FIELD_DS_IMPORT_RELEVANT);
    }
}
