/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferStates;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.ArrayUtil;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Liste von {@link iPartsDataPicOrderPart}s.
 */
public class iPartsDataPicOrderPartsList extends EtkDataObjectList<iPartsDataPicOrderPart> implements iPartsConst {

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataPicOrderPicture}s für die übergebene Auftrags-GUID.
     *
     * @param project
     * @param orderGuid
     * @return
     */
    public static iPartsDataPicOrderPartsList loadPicOrderPartsList(EtkProject project, String orderGuid) {
        iPartsDataPicOrderPartsList list = new iPartsDataPicOrderPartsList();
        list.loadPicOrderPartsFromDB(project, orderGuid, DBActionOrigin.FROM_DB);
        return list;
    }

    public static iPartsDataPicOrderPartsList loadPicOrderPartsListByPart(EtkProject project, PartListEntryId partListEntryId) {
        iPartsDataPicOrderPartsList list = new iPartsDataPicOrderPartsList();
        list.loadPicOrderPartsByPartEntryFromDB(project, partListEntryId, DBActionOrigin.FROM_DB);
        return list;
    }

    public static List<iPartsPicOrderId> loadPicOrderListByPart(EtkProject project, PartListEntryId partListEntryId) {
        iPartsDataPicOrderPartsList list = loadPicOrderPartsListByPart(project, partListEntryId);
        List<iPartsPicOrderId> guidList = new DwList<iPartsPicOrderId>();
        //doppelte Einträge entfernen
        for (iPartsDataPicOrderPart picOrderPart : list) {
            iPartsPicOrderId id = new iPartsPicOrderId(picOrderPart.getAsId().getOrderGuid());
            if (!guidList.contains(id)) {
                guidList.add(id);
            }
        }
        return guidList;
    }

    public static iPartsDataPicOrderPartsList loadPicOrderPartsListByModul(EtkProject project, AssemblyId moduleId) {
        iPartsDataPicOrderPartsList list = new iPartsDataPicOrderPartsList();
        list.loadPicOrderPartsByModulFromDB(project, moduleId, DBActionOrigin.FROM_DB);
        return list;
    }

    public static Map<PartListEntryId, Boolean> loadPicOrderPartsListByModulForIcons(EtkProject project, AssemblyId moduleId, iPartsDataPicOrderList picOrderList) {
        //Liste der Bildaufträge für dieses Modul holen
        iPartsDataPicOrderPartsList list = loadPicOrderPartsListByModul(project, moduleId);
        Map<iPartsPicOrderId, Boolean> guidList = new HashMap<iPartsPicOrderId, Boolean>();
        //doppelte Einträge entfernen
        for (iPartsDataPicOrderPart picOrderPart : list) {
            iPartsPicOrderId id = new iPartsPicOrderId(picOrderPart.getAsId().getOrderGuid());
            if (!guidList.containsKey(id)) {
                guidList.put(id, Boolean.TRUE);
            }
        }
        //Status der Bildaufträge überprüfen (fertige und abgelehnte liefern keinen Beitrag)
        Iterator<Map.Entry<iPartsPicOrderId, Boolean>> iterator = guidList.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<iPartsPicOrderId, Boolean> guidEntry = iterator.next();
            iPartsPicOrderId id = guidEntry.getKey();
            iPartsDataPicOrder picOrder = null;
            if (picOrderList != null) {
                picOrder = picOrderList.findById(id);
            } else {
                picOrder = new iPartsDataPicOrder(project, id);
                if (!picOrder.loadFromDB(id)) {
                    picOrder = null;
                }
            }
            if (picOrder != null) {
                if (!iPartsTransferStates.isSaveToDB_Allowed(picOrder.getStatus())) {
                    guidList.put(id, Boolean.FALSE);
                }
            } else {
                iterator.remove();
            }
        }
        Map<PartListEntryId, Boolean> result = new HashMap<PartListEntryId, Boolean>();
        //betroffene PartListEntries sammeln
        for (int lfdNr = list.size() - 1; lfdNr >= 0; lfdNr--) {
            iPartsPicOrderId id = new iPartsPicOrderId(list.get(lfdNr).getAsId().getOrderGuid());
            if (guidList.containsKey(id)) {
                PartListEntryId partListEntryId = list.get(lfdNr).getAsId().getPartListEntryId();
                if (!result.containsKey(partListEntryId)) {
                    result.put(partListEntryId, guidList.get(id));
                } else {
                    if (!result.get(partListEntryId) && guidList.get(id)) {
                        result.put(partListEntryId, guidList.get(id));
                    }
                }
            }
        }
        return result;
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataPicOrderPicture} für die übergebene Auftrags-GUID (samt gespeicherten Stücklistenpositionen als Blob).
     *
     * @param project
     * @param orderGuid
     * @param origin
     */
    public void loadPicOrderPartsFromDB(EtkProject project, String orderGuid, DBActionOrigin origin) {
        clear(origin);

        Set<String> fields = new HashSet<>(project.getEtkDbs().getExistingFieldNamesWithoutBlobs(TABLE_DA_PICORDER_PARTS));
        fields.addAll(project.getEtkDbs().getExistingFieldNamesForBlobs(TABLE_DA_PICORDER_PARTS));

        String[] whereFields = new String[]{ FIELD_DA_PPA_ORDER_GUID };
        String[] whereValues = new String[]{ orderGuid };

        searchSortAndFill(project, TABLE_DA_PICORDER_PARTS, ArrayUtil.toStringArray(fields), whereFields, whereValues,
                          null, null, null, LoadType.COMPLETE, false, DBActionOrigin.FROM_DB);
    }

    public void loadPicOrderPartsByPartEntryFromDB(EtkProject project, PartListEntryId partListEntryId, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DA_PPA_VARI, FIELD_DA_PPA_VER, FIELD_DA_PPA_LFDNR };
        String[] whereValues = new String[]{ partListEntryId.getKVari(), partListEntryId.getKVer(), partListEntryId.getKLfdnr() };

        searchAndFill(project, whereFields, whereValues, origin);
    }

    public void loadPicOrderPartsByModulFromDB(EtkProject project, AssemblyId moduleId, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DA_PPA_VARI, FIELD_DA_PPA_VER };
        String[] whereValues = new String[]{ moduleId.getKVari(), moduleId.getKVer() };

        searchAndFill(project, whereFields, whereValues, origin);
    }

    private void searchAndFill(EtkProject project, String[] whereFields, String[] whereValues, DBActionOrigin origin) {
        searchAndFill(project, TABLE_DA_PICORDER_PARTS, whereFields, whereValues, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataPicOrderPart getNewDataObject(EtkProject project) {
        return new iPartsDataPicOrderPart(project, null);
    }
}
