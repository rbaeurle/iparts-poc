/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper;

import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModuleCemat;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModuleCematList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSEinPAS;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.List;
import java.util.Map;

/**
 * Hilfsklasse für die EinPAS-Strukturen aus Cemat
 */
public class iPartsWSEinPasHelper {

    /**
     * Methode zum Befüllen des {@link iPartsWSEinPAS}-Objekts.
     * Gibt eine Liste von aufbereiteten, befüllten {@link iPartsWSEinPAS}-Objekten für den Webservice zurück.
     *
     * @param einPasDataMap
     * @param partListEntry
     * @return {@code null} falls es keine EinPAS-Daten gibt
     */
    public static List<iPartsWSEinPAS> fillEinPasList(Map<PartListEntryId, List<iPartsDataModuleCemat>> einPasDataMap, EtkDataPartListEntry partListEntry) {
        if ((einPasDataMap != null) && (!einPasDataMap.isEmpty())) {
            String matNr = partListEntry.getPart().getAsId().getMatNr();
            List<iPartsDataModuleCemat> einPasDataForModuleList = einPasDataMap.get(partListEntry.getAsId());
            if ((einPasDataForModuleList != null) && (!einPasDataForModuleList.isEmpty())) {
                Map<EinPasId, List<String>> einPasVersionMap = iPartsDataModuleCematList.buildCematEinPasVersionsMap(einPasDataForModuleList, matNr,
                                                                                                                     iPartsDataModuleCematList.VERSIONS_LIMIT);
                if (!einPasVersionMap.isEmpty()) {
                    List<iPartsWSEinPAS> einPASList = new DwList<>();
                    for (Map.Entry<EinPasId, List<String>> einPasData : einPasVersionMap.entrySet()) {
                        einPasData.getValue().stream()
                                .map(version -> {
                                    iPartsWSEinPAS einPAS = new iPartsWSEinPAS();
                                    einPAS.setVersion(version);
                                    einPAS.setHg(einPasData.getKey().getHg());
                                    einPAS.setG(einPasData.getKey().getG());
                                    einPAS.setTu(einPasData.getKey().getTu());
                                    return einPAS;
                                })
                                .forEach(einPASList::add);
                    }

                    if (!einPASList.isEmpty()) {
                        return einPASList;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gibt das Objekt mit der höchsten Versionsnummer in den EinPAS-Daten zurück
     *
     * @param einPASDataList
     * @return {@code null} falls es keine EinPAS-Daten gibt
     */
    public static iPartsWSEinPAS getLatestEinPASNode(List<iPartsWSEinPAS> einPASDataList) {
        iPartsWSEinPAS result = null;
        if (!einPASDataList.isEmpty()) {
            for (iPartsWSEinPAS einPASData : einPASDataList) {
                if (result == null) {
                    result = einPASData;
                } else {
                    if ((StrUtils.strToIntDef(einPASData.getVersion(), -1)) > StrUtils.strToIntDef(result.getVersion(), -1)) {
                        result = einPASData;
                    }
                }
            }
        }
        return result;
    }
}