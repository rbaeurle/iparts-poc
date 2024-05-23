/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.util.StrUtils;
import de.docware.util.sql.TableAndFieldName;

import java.util.HashMap;
import java.util.Map;

/**
 * Liste von {@link iPartsDataGenInstallLocation}.
 */
public class iPartsDataGenInstallLocationList extends EtkDataObjectList<iPartsDataGenInstallLocation> implements iPartsConst {

    public static final Map<String, String> DIALOG_MAPPING = new HashMap<>();

    static {
        DIALOG_MAPPING.put(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GENVO, FIELD_DGIL_GEN_INSTALL_LOCATION);
        DIALOG_MAPPING.put(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SPLITSIGN, FIELD_DGIL_SPLIT_SIGN);
    }

    public iPartsDataGenInstallLocationList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Liefert 'leere' DBDataObjectAttributes für die Abbildung DIALOG-Entry <- Wert aus GenInstallLocation
     *
     * @return
     */
    public static DBDataObjectAttributes getDefaultGenInstallLocationAttributes() {
        DBDataObjectAttributes attributes = new DBDataObjectAttributes();
        for (String virtualFieldName : DIALOG_MAPPING.keySet()) {
            attributes.addField(virtualFieldName, "", true, DBActionOrigin.FROM_DB);
        }
        return attributes;
    }

    /**
     * Bestimmt für einen {@param bcteKey} die Attributwerte der GenInstallLocation für die DIALOG-Stückliste
     *
     * @param genInstallLocationMap Map von Schlüssel für die GenInstallLocation auf Attributwerte der GenInstallLocation
     * @param bcteKey
     * @return
     */
    public static DBDataObjectAttributes getGenInstallLocationAttributesForBcteKey(Map<String, iPartsDataGenInstallLocation> genInstallLocationMap,
                                                                                   iPartsDialogBCTEPrimaryKey bcteKey) {
        if ((genInstallLocationMap == null) || genInstallLocationMap.isEmpty() || (bcteKey == null)) {
            return getDefaultGenInstallLocationAttributes();
        }

        String key = iPartsGenInstallLocationId.calcGenInstallMappingKeyFromBCTEKey(bcteKey);
        if (StrUtils.isValid(key)) {
            iPartsDataGenInstallLocation dataGenInstallLocation = genInstallLocationMap.get(key);
            if (dataGenInstallLocation != null) {
                DBDataObjectAttributes attributes = new DBDataObjectAttributes();
                for (Map.Entry<String, String> entry : DIALOG_MAPPING.entrySet()) {
                    attributes.addField(entry.getKey(), dataGenInstallLocation.getFieldValue(entry.getValue()), true, DBActionOrigin.FROM_DB);
                }
                return attributes;
            }
        }

        return getDefaultGenInstallLocationAttributes();
    }

    /**
     * Lädt alle freigegebenen generischen Verbauorte mit den Attributen vom Mapping {@link #DIALOG_MAPPING} für eine {@link HmMSmId}.
     * Liefert eine Map mit {@code key = HmMSmId + PosE} (siehe {@link iPartsGenInstallLocationId#getGenInstallMappingKey()}).
     * Freigegeben heißt SDB = Leer
     *
     * @param project
     * @param hmMSmId
     * @return
     */
    public static Map<String, iPartsDataGenInstallLocation> loadAllReleasedDataForHmMSmIdAsMap(EtkProject project, HmMSmId hmMSmId) {
        iPartsDataGenInstallLocationList list = new iPartsDataGenInstallLocationList();
        return list.loadAllDataAsMap(project, hmMSmId.getSeries(), hmMSmId, true);
    }

    /**
     * Lädt alle freigegebenen generischen Verbauorte mit den Attributen vom Mapping {@link #DIALOG_MAPPING} für eine Baureihe.
     * Liefert eine Map mit {@code key = HmMSmId + PosE} (siehe {@link iPartsGenInstallLocationId#getGenInstallMappingKey()}).
     * Freigegeben heißt SDB = Leer
     *
     * @param project
     * @param seriesId
     * @return
     */
    public static Map<String, iPartsDataGenInstallLocation> loadAllReleasedDataForSeriesAsMap(EtkProject project, iPartsSeriesId seriesId) {
        iPartsDataGenInstallLocationList list = new iPartsDataGenInstallLocationList();
        return list.loadAllDataAsMap(project, seriesId.getSeriesNumber(), null, true);
    }

    /**
     * Lädt alle generischen Verbauorte mit den Attributen vom Mapping {@link #DIALOG_MAPPING} für eine Baureihe und optional
     * {@link HmMSmId}.
     * Liefert eine Map mit {@code key = HmMSmId + PosE} (siehe {@link iPartsGenInstallLocationId#getGenInstallMappingKey()}).
     *
     * @param project
     * @param seriesNo
     * @param hmMSmId
     * @param released
     * @return
     */
    public Map<String, iPartsDataGenInstallLocation> loadAllDataAsMap(EtkProject project, String seriesNo, HmMSmId hmMSmId,
                                                                      boolean released) {
        clear(DBActionOrigin.FROM_DB);

        EtkDisplayFields selectFields = new EtkDisplayFields();
        for (String selectFieldName : DIALOG_MAPPING.values()) {
            selectFields.addFeld(new EtkDisplayField(TABLE_DA_GENERIC_INSTALL_LOCATION, selectFieldName, false, false));
        }

        String[] whereTableAndFields = new String[]{ TableAndFieldName.make(TABLE_DA_GENERIC_INSTALL_LOCATION, FIELD_DGIL_SERIES) };
        String[] whereValues = new String[]{ seriesNo };
        if (hmMSmId != null) {
            whereTableAndFields = StrUtils.mergeArrays(whereTableAndFields, new String[]{ TableAndFieldName.make(TABLE_DA_GENERIC_INSTALL_LOCATION,
                                                                                                                 FIELD_DGIL_HM),
                                                                                          TableAndFieldName.make(TABLE_DA_GENERIC_INSTALL_LOCATION,
                                                                                                                 FIELD_DGIL_M),
                                                                                          TableAndFieldName.make(TABLE_DA_GENERIC_INSTALL_LOCATION,
                                                                                                                 FIELD_DGIL_SM) });
            whereValues = StrUtils.mergeArrays(whereValues, new String[]{ hmMSmId.getHm(), hmMSmId.getM(), hmMSmId.getSm() });
        }

        if (released) {
            whereTableAndFields = StrUtils.mergeArrays(whereTableAndFields, new String[]{ TableAndFieldName.make(TABLE_DA_GENERIC_INSTALL_LOCATION,
                                                                                                                 FIELD_DGIL_SDB) });
            whereValues = StrUtils.mergeArrays(whereValues, new String[]{ "" });
        }

        Map<String, iPartsDataGenInstallLocation> resultMap = new HashMap<>();
        FoundAttributesCallback foundAttributesCallback = createCallback(project, resultMap, DBActionOrigin.FROM_DB);

        searchSortAndFillWithJoin(project, null, selectFields,
                                  whereTableAndFields, whereValues,
                                  null, null,
                                  false, null, false,
                                  false, foundAttributesCallback);
        return resultMap;
    }

    private FoundAttributesCallback createCallback(EtkProject project, Map<String, iPartsDataGenInstallLocation> resultMap,
                                                   DBActionOrigin origin) {
        return new FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                iPartsDataGenInstallLocation dataObject = createDataGenInstallLocation(project, attributes, origin);
                if (dataObject != null) {
                    resultMap.put(dataObject.getAsId().getGenInstallMappingKey(),
                                  dataObject);
                }
                return false;
            }

            private iPartsDataGenInstallLocation createDataGenInstallLocation(EtkProject project,
                                                                              DBDataObjectAttributes attributes,
                                                                              DBActionOrigin origin) {
                // Generischen Verbauort nur dann zurückliefern falls mindestens eines der relevanten Attribute nicht leer ist
                boolean genInstallLocationIsValid = false;
                for (String attributeName : DIALOG_MAPPING.values()) {
                    if (!attributes.getFieldValue(attributeName).isEmpty()) {
                        genInstallLocationIsValid = true;
                        break;
                    }
                }
                if (!genInstallLocationIsValid) {
                    return null;
                }

                iPartsDataGenInstallLocation dataObject = getNewDataObject(project);
                if (dataObject != null) {
                    // nur bei LoadType.COMPLETE darf das isLoaded-Flag im EtkDataObject gesetzt werden, weil bei LoadType.ONLY_IDS
                    // die restlichen Attribute außer den Primary Keys noch nicht geladen wurden
                    dataObject.setAttributes(attributes, true, false, origin);
                }
                return dataObject;
            }
        };
    }

    @Override
    protected iPartsDataGenInstallLocation getNewDataObject(EtkProject project) {
        return new iPartsDataGenInstallLocation(project, null);
    }
}
