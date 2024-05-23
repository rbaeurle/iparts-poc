package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.misc.id.IdWithType;

import java.util.Set;

/**
 * Liste mit {@link iPartsDataDIALOGChange} Objekten
 */
public class iPartsDataDIALOGChangeList extends EtkDataObjectList<iPartsDataDIALOGChange> implements iPartsConst {

    /**
     * DataObjects für DIALOG Änderungen für BCTE-Key laden
     *
     * @param bcteKey
     * @param project
     */
    public static iPartsDataDIALOGChangeList loadForBCTEKey(String bcteKey, EtkProject project) {
        iPartsDataDIALOGChangeList dataDIALOGChangeList = new iPartsDataDIALOGChangeList();
        dataDIALOGChangeList.loadDIALOGChangesForBCTEKey(bcteKey, DBActionOrigin.FROM_DB, project);
        return dataDIALOGChangeList;
    }

    /**
     * DataObjects für DIALOG Änderungen für Material in angegebener Baureihe laden
     *
     * @param matNo
     * @param seriesId
     * @param project
     */
    public static iPartsDataDIALOGChangeList loadForMatNo(String matNo, iPartsSeriesId seriesId, EtkProject project) {
        iPartsDataDIALOGChangeList dataDIALOGChangeList = new iPartsDataDIALOGChangeList();
        dataDIALOGChangeList.loadDIALOGChangesForMatNo(matNo, seriesId, DBActionOrigin.FROM_DB, project);
        return dataDIALOGChangeList;
    }

    /**
     * DataObjects für DIALOG Änderungen für ein bestimmtes Datenobjekt laden
     *
     * @param dataObjectId
     * @param project
     * @return
     */
    public static iPartsDataDIALOGChangeList loadDIALOGChangesForDataObject(String dataObjectId, EtkProject project) {
        iPartsDataDIALOGChangeList dataDIALOGChangeList = new iPartsDataDIALOGChangeList();
        dataDIALOGChangeList.loadDIALOGChangesForDataObjectFromDB(dataObjectId, DBActionOrigin.FROM_DB, project);
        return dataDIALOGChangeList;
    }

    /**
     * DataObjects für DIALOG Änderungen für Material und Änderungstyp laden
     *
     * @param matNo
     * @param changeType
     * @param project
     */
    public static iPartsDataDIALOGChangeList loadForMatNoAndType(EtkProject project, String matNo, iPartsDataDIALOGChange.ChangeType changeType) {
        iPartsDataDIALOGChangeList dataDIALOGChangeList = new iPartsDataDIALOGChangeList();
        dataDIALOGChangeList.loadDIALOGChangesForMatNoAndType(matNo, changeType, DBActionOrigin.FROM_DB, project);
        return dataDIALOGChangeList;
    }

    /**
     * DataObjects für DIALOG Änderungen für den Änderungstyp laden
     *
     * @param changeTypeString
     * @param project
     */
    public static iPartsDataDIALOGChangeList loadAndSortForDCType(EtkProject project, String changeTypeString, String[] sortFields) {
        iPartsDataDIALOGChangeList dataDIALOGChangeList = new iPartsDataDIALOGChangeList();
        dataDIALOGChangeList.loadDIALOGChangesForDCType(changeTypeString, project, sortFields);
        return dataDIALOGChangeList;
    }

    public static iPartsDataDIALOGChangeList loadAndSortForDCType(EtkProject project, String[] whereFields, String[] whereValues, String[] sortFields) {
        iPartsDataDIALOGChangeList dataDIALOGChangeList = new iPartsDataDIALOGChangeList();
        dataDIALOGChangeList.loadDIALOGChangesForDCType(project, whereFields, whereValues, sortFields);
        return dataDIALOGChangeList;
    }

    private void loadDIALOGChangesForDCType(String changeTypeString, EtkProject project, String[] sortFields) {
        String[] whereFields = { FIELD_DDC_DO_TYPE };
        String[] whereValues = { changeTypeString };
        loadDIALOGChangesForDCType(project, whereFields, whereValues, sortFields);
    }

    private void loadDIALOGChangesForDCType(EtkProject project, String[] whereFields, String[] whereValues, String[] sortFields) {
        clear(DBActionOrigin.FROM_DB);

        searchAndFillWithLike(project, TABLE_DA_DIALOG_CHANGES, null, whereFields,
                              whereValues, LoadType.COMPLETE, false, DBActionOrigin.FROM_DB);
    }

    /**
     * DataObjects für DIALOG Änderungen für Material und Änderungstyp laden
     *
     * @param matNo
     * @param changeType
     * @param origin
     * @param project
     */
    private void loadDIALOGChangesForMatNoAndType(String matNo, iPartsDataDIALOGChange.ChangeType changeType, DBActionOrigin origin, EtkProject project) {
        String[] whereFields = { FIELD_DDC_MATNR, FIELD_DDC_DO_TYPE };
        String[] whereValues = { matNo, changeType.getDbKey() };
        searchSortAndFill(project, TABLE_DA_DIALOG_CHANGES, whereFields, whereValues, null, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataDIALOGChange getNewDataObject(EtkProject project) {
        return new iPartsDataDIALOGChange(project, null);
    }

    /**
     * DataObjects für DIALOG Änderungen für teilweisen BCTE-Key in angegebener Baureihe laden
     *
     * @param seriesId
     * @param project
     */
    public void loadDIALOGChangesForPartialBCTEKeyWithSeries(String partialBcteKey, iPartsSeriesId seriesId, EtkProject project) {
        clear(DBActionOrigin.FROM_DB);
        searchAndFillWithLike(project, TABLE_DA_DIALOG_CHANGES, null, new String[]{ FIELD_DDC_SERIES_NO, FIELD_DDC_BCTE },
                              new String[]{ seriesId.getSeriesNumber(), partialBcteKey }, LoadType.COMPLETE, false,
                              DBActionOrigin.FROM_DB);
    }

    /**
     * DataObjects für DIALOG Änderungen für ein beliebiges Material für angegebene Baureihe laden
     *
     * @param seriesId
     * @param project
     */
    public void loadDIALOGChangesForMatWithSeries(iPartsSeriesId seriesId, EtkProject project) {
        clear(DBActionOrigin.FROM_DB);
        searchSortAndFill(project, TABLE_DA_DIALOG_CHANGES, new String[]{ FIELD_DDC_SERIES_NO }, new String[]{ seriesId.getSeriesNumber() },
                          new String[]{ FIELD_DDC_MATNR }, new String[]{ "" }, null, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    /**
     * DataObjects für DIALOG Änderungen für BCTE-Key laden
     *
     * @param bcteKey
     * @param origin
     * @param project
     */
    private void loadDIALOGChangesForBCTEKey(String bcteKey, DBActionOrigin origin, EtkProject project) {
        searchSortAndFill(project, TABLE_DA_DIALOG_CHANGES, new String[]{ FIELD_DDC_BCTE }, new String[]{ bcteKey },
                          null, LoadType.COMPLETE, origin);
    }

    /**
     * DataObjects für DIALOG Änderungen für Material in angegebener Baureihe laden
     *
     * @param matNo
     * @param seriesId
     * @param origin
     * @param project
     */
    private void loadDIALOGChangesForMatNo(String matNo, iPartsSeriesId seriesId, DBActionOrigin origin, EtkProject project) {
        searchSortAndFill(project, TABLE_DA_DIALOG_CHANGES, new String[]{ FIELD_DDC_MATNR, FIELD_DDC_SERIES_NO },
                          new String[]{ matNo, seriesId.getSeriesNumber() }, null, LoadType.COMPLETE, origin);
    }

    public void loadDIALOGChangesForDataObjectFromDB(String dataObjectId, DBActionOrigin origin, EtkProject project) {
        searchSortAndFill(project, TABLE_DA_DIALOG_CHANGES, new String[]{ FIELD_DDC_DO_ID },
                          new String[]{ dataObjectId }, null, LoadType.COMPLETE, origin);
    }

    /**
     * Liefert eine gefilterte Liste von {@link iPartsDataDIALOGChange}s für die übergebene {@link PartListEntryId} zurück.
     *
     * @param partListEntryId
     * @return {@code null} falls nach der Filterung keine Einträge übrigbleiben
     */
    public iPartsDataDIALOGChangeList filterForPartListEntry(PartListEntryId partListEntryId) {
        String partListEntryIdDBString = partListEntryId.toDBString();
        iPartsDataDIALOGChangeList dataDIALOGChangesForPLE = null;
        for (iPartsDataDIALOGChange dataDIALOGChange : this) {
            String dataDIALOGChangePleId = dataDIALOGChange.getFieldValue(FIELD_DDC_KATALOG_ID);
            if (dataDIALOGChangePleId.isEmpty() || dataDIALOGChangePleId.equals(partListEntryIdDBString)) {
                if (dataDIALOGChangesForPLE == null) {
                    dataDIALOGChangesForPLE = new iPartsDataDIALOGChangeList();
                }
                dataDIALOGChangesForPLE.add(dataDIALOGChange, DBActionOrigin.FROM_DB);
            }
        }
        return dataDIALOGChangesForPLE;
    }

    /**
     * Filtert die aktuelle Liste von {@link iPartsDataDIALOGChange}s für die übergebenen Werke.
     * Dabei werden nur die {@link iPartsDataDIALOGChange}s für Werkseinsatzdaten von Farben/ Varianten bzw.
     * Varianten zu Teil berücksichtigt. Es werden alle Einträge für Werke, die nicht zum aktuellen Produkt gültig sind
     * ausgefiltert. D.h. es bleiben nur solche Einträge übrig die auch in der Related Info angezeigt werden würden.
     *
     * @param productFactories Alle zum Produkt gültigen Werke
     * @return {@code null} falls nach der Filterung keine Einträge übrigbleiben
     */
    public iPartsDataDIALOGChangeList filterForColorTableFactories(Set<String> productFactories) {
        if (productFactories == null || productFactories.isEmpty()) {
            return this;
        }
        iPartsDataDIALOGChangeList dataDIALOGChangesForColorTable = null;

        for (iPartsDataDIALOGChange dataDIALOGChange : this) {
            if (iPartsDataDIALOGChange.ChangeType.getChangeType(dataDIALOGChange.getAsId().getDoType()) == iPartsDataDIALOGChange.ChangeType.COLORTABLE_FACTORY_DATA) {
                IdWithType doId = IdWithType.fromDBString(iPartsColorTableFactoryId.TYPE, dataDIALOGChange.getAsId().getDoId());
                if (doId != null) {
                    iPartsColorTableFactoryId colorTableFactoryDataId = new iPartsColorTableFactoryId(doId.toStringArrayWithoutType());
                    if (colorTableFactoryDataId.isValidId()) {
                        String factory = colorTableFactoryDataId.getFactory();
                        if (productFactories.contains(factory)) {
                            if (dataDIALOGChangesForColorTable == null) {
                                dataDIALOGChangesForColorTable = new iPartsDataDIALOGChangeList();
                            }
                            dataDIALOGChangesForColorTable.add(dataDIALOGChange, DBActionOrigin.FROM_DB);
                        }
                    }
                }
            }
        }
        return dataDIALOGChangesForColorTable;
    }
}
