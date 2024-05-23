/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.List;

/**
 * Liste von {@link iPartsDataWireHarness}s für Leitungssatzbaukästen.
 */
public class iPartsDataWireHarnessList extends EtkDataObjectList<iPartsDataWireHarness> implements iPartsConst {

    public iPartsDataWireHarnessList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und lädt eine Liste der IDs aller {@link iPartsDataWireHarness}s.
     *
     * @param project
     * @return
     */
    public static iPartsDataWireHarnessList loadAllWireHarness(EtkProject project) {
        iPartsDataWireHarnessList list = new iPartsDataWireHarnessList();
        list.loadAllWireHarnessFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste {@link iPartsDataWireHarness}s für einen einzelnen Leitungssatzbaukasten inkl. Join auf
     * die Materialtabelle sortiert nach Ref, Stecker, Typ, untere Sachnummer.
     * Die Standard-Sortierung ist: REF, Stecker, Typ, untere Sachnummer
     * DAIMLER-12004: Zudem sollen Teile, zu denen es keine "REF" gibt, unterhalb der Datensätze mit REF angezeigt werden.
     *
     * @param project
     * @param wireHarnessNumber Leitungssatz, obere Sachnummer
     * @param selectFields
     * @return
     */
    public static iPartsDataWireHarnessList loadOneWireHarness(EtkProject project, String wireHarnessNumber, EtkDisplayFields selectFields) {
        iPartsDataWireHarnessList list = new iPartsDataWireHarnessList();
        list.loadOneWireHarnessFromDB(project, wireHarnessNumber, selectFields, DBActionOrigin.FROM_DB);
        iPartsDataWireHarnessList resultList = new iPartsDataWireHarnessList();
        // Umsortieren
        List<iPartsDataWireHarness> emptyRefAndConnectorNoList = new DwList<>();
        for (iPartsDataWireHarness dataWireHarness : list.getAsList()) {
            if (dataWireHarness.getAsId().hasEmptyRefAndConnectorNo()) {
                emptyRefAndConnectorNoList.add(dataWireHarness);
            } else {
                resultList.add(dataWireHarness, DBActionOrigin.FROM_DB);
            }
        }
        resultList.addAll(emptyRefAndConnectorNoList, DBActionOrigin.FROM_DB);
        return resultList;
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataWireHarness}s aus der DB.
     *
     * @param project
     * @param origin
     */
    private void loadAllWireHarnessFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_WIRE_HARNESS, null, null, LoadType.ONLY_IDS, origin);
    }

    /**
     * Lädt eine Liste {@link iPartsDataWireHarness}s für einen einzelnen Leitungssatzbaukasten inkl. Join auf die Materialtabelle
     * sortiert nach Ref, Stecker, Typ, untere Sachnummer.
     *
     * @param project
     * @param wireHarnessNumber Leitungssatz, obere Sachnummer
     * @param selectFields
     * @param origin
     */
    private void loadOneWireHarnessFromDB(EtkProject project, String wireHarnessNumber, EtkDisplayFields selectFields, DBActionOrigin origin) {
        clear(origin);

        if (selectFields == null) {
            // Alle Felder von den Tabellen DA_WIRE_HARNESS und MAT hinzufügen
            selectFields = new EtkDisplayFields();
            selectFields.addFelder(project.getAllDisplayFieldsForTable(TABLE_DA_WIRE_HARNESS));
            selectFields.addFelder(project.getAllDisplayFieldsForTable(TABLE_MAT));
        }

        searchSortAndFillWithJoin(project, project.getDBLanguage(), selectFields, new String[]{ TableAndFieldName.make(TABLE_DA_WIRE_HARNESS, FIELD_DWH_SNR) },
                                  new String[]{ wireHarnessNumber }, false, new String[]{ FIELD_DWH_REF, FIELD_DWH_CONNECTOR_NO,
                                                                                          FIELD_DWH_SNR_TYPE, FIELD_DWH_SUB_SNR },
                                  false, null, new JoinData(TABLE_MAT, new String[]{ FIELD_DWH_SUB_SNR }, new String[]{ FIELD_M_MATNR },
                                                            true, false));
    }

    @Override
    protected iPartsDataWireHarness getNewDataObject(EtkProject project) {
        return new iPartsDataWireHarness(project, null);
    }
}
