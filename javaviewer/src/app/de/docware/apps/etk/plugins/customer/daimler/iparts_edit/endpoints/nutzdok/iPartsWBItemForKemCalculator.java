/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.nutzdok;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataBOMConstKitContentList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataMBSPartlistList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ImportExportLogHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs.MBSStructureId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataKEMWorkBasketEDS;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataKEMWorkBasketEDSList;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataKEMWorkBasketMBS;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataKEMWorkBasketMBSList;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.iPartsWSAbstractEndpointNutzDok;
import de.docware.framework.modules.db.DBActionOrigin;

import java.util.Map;
import java.util.Set;

/**
 * Helper für die Berechnung des Arbeitsvorrat für aufgesammelte KEMs
 */
public class iPartsWBItemForKemCalculator {

    private final EtkProject project;
    private final ImportExportLogHelper logHelper;

    public iPartsWBItemForKemCalculator(EtkProject project, ImportExportLogHelper logHelper) {
        this.project = project;
        this.logHelper = logHelper;
    }

    /**
     * Arbeitsvorrat für aufgesammelte KEMs berechnen
     *
     * @param kemNoSet             Liste der neu importierten KEMs
     * @param dataObjectsToBeSaved Zum Speichern der neu angelegten DataObjects
     *                             kann null sein (dann wird am Ende der Routine direkt gespeichert)
     * @return
     */
    public boolean calcWorkBasketItemsForKem(Set<String> kemNoSet, GenericEtkDataObjectList<EtkDataObject> dataObjectsToBeSaved) {
        if (!kemNoSet.isEmpty()) {
            boolean saveAtEnd = false;
            if (dataObjectsToBeSaved == null) {
                dataObjectsToBeSaved = new GenericEtkDataObjectList<>();
                saveAtEnd = true;
            }
            fireMessage("!!Berechne Arbeitsvorrat für %1 KEMs", Integer.toString(kemNoSet.size()));
            calcWorkBasketItemsForKemEdsAndMBS(kemNoSet, dataObjectsToBeSaved);
            fireMessage("!!Berechnung KEM-Arbeitsvorrat beendet");
            if (saveAtEnd) {
                iPartsWSAbstractEndpointNutzDok.saveInTransaction(project, dataObjectsToBeSaved);
            }
            return true;
        }
        return false;
    }

    private void calcWorkBasketItemsForKemEdsAndMBS(Set<String> kemNoSet,
                                                    GenericEtkDataObjectList<EtkDataObject> dataObjectsToBeSaved) {
        // 1.Schritt: Bestimme aus den importierten KEMNos die Liste der KemNos, die in MBS vorkommen
        Set<String> usedKemNoSetInMBS = iPartsDataMBSPartlistList.getUsedKemNosFromMBSPartlistBig(project, kemNoSet);
        // 2.Schritt: entferne aus der KemNoSet alle MBS-KEMs
        kemNoSet.removeIf(usedKemNoSetInMBS::contains);
        // 3.Schritt Suche EDS Verwendung in Retail
        if (!kemNoSet.isEmpty()) {
            fireMessage("!!Davon %1 KEMs für EDS", Integer.toString(kemNoSet.size()));
            calcWorkBasketItemsForKemEdsOnly(kemNoSet, dataObjectsToBeSaved);
        }

        if (!usedKemNoSetInMBS.isEmpty()) {
            fireMessage("!!Davon %1 KEMs für MBS", Integer.toString(usedKemNoSetInMBS.size()));
            // 4.Schritt: bestimme die SAAs aus MBS
            Map<String, Set<MBSStructureId>> kemSaaMap = iPartsDataMBSPartlistList.getValidSaasFromKEM(project, usedKemNoSetInMBS, true);
            if (!kemSaaMap.isEmpty()) {
                // 5.Schritt Suche MBS Verwendung in Retail
                iPartsDataKEMWorkBasketMBSList list = iPartsDataKEMWorkBasketMBSList.checkUsageInRetail(project, kemSaaMap);
                // und speichern in allgemeiner Liste
                for (iPartsDataKEMWorkBasketMBS dataKEMWorkBasket : list) {
                    dataObjectsToBeSaved.add(dataKEMWorkBasket, DBActionOrigin.FROM_EDIT);
                }
            }
        }
    }

    private void calcWorkBasketItemsForKemEdsOnly(Set<String> kemNoSet,
                                                  GenericEtkDataObjectList<EtkDataObject> dataObjectsToBeSaved) {
        Map<String, Set<String>> kemSaaMap = iPartsDataBOMConstKitContentList.getValidSaasFromKEM(project, kemNoSet);
        iPartsDataKEMWorkBasketEDSList list = iPartsDataKEMWorkBasketEDSList.checkUsageInRetail(project, kemSaaMap);
        for (iPartsDataKEMWorkBasketEDS dataKEMWorkBasket : list) {
            dataObjectsToBeSaved.add(dataKEMWorkBasket, DBActionOrigin.FROM_EDIT);
        }
    }

    private void fireMessage(String key, String... placeHolderTexts) {
        if (logHelper != null) {
            logHelper.addLogMsgWithTranslation(key, placeHolderTexts);
        }
    }
}
