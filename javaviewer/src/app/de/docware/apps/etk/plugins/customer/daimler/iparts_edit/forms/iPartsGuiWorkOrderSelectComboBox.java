/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsUserAdminCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsUserAdminOrgCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst.iPartsDataWorkOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst.iPartsDataWorkOrderList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst.iPartsWorkOrderId;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.util.StrUtils;

import java.util.Map;
import java.util.TreeMap;

/**
 * {@link RComboBox} zur Auswahl von einem Bearbeitungsauftrag.
 */
public class iPartsGuiWorkOrderSelectComboBox extends RComboBox<String> {

    /**
     * Erzeugt eine neue Auswahlliste für den übergebenen Benutzer und wählt optional den übergebenen Bearbeitungsauftrag
     * aus. Der optional ausgewählte Bearbeitungsauftrag ist auf jeden Fall immer in der Liste enthalten
     *
     * @param userId            Benutzer-ID für die Filterung der zur Auswahl stehenden Bearbeitungsaufträge basierend auf
     *                          der Organisation des Benutzers
     * @param selectWorkOrderId Optionale Bearbeitungsauftrags-ID für die Selektion
     * @param project
     */
    public iPartsGuiWorkOrderSelectComboBox(String userId, String selectWorkOrderId, EtkProject project) {
        String orgId = null;
        if (StrUtils.isValid(userId)) {
            orgId = iPartsUserAdminCache.getInstance(userId).getOrgId();
        }
        init(orgId, selectWorkOrderId, project);
    }

    /**
     * Initialisiert die Auswahlliste mit der übergebenen Organisation und wählt optional den übergebenen Bearbeitungsauftrag
     * aus. Der optional ausgewählte Bearbeitungsauftrag ist auf jeden Fall immer in der Liste enthalten
     *
     * @param orgId             Organisations-ID für die Auswahl des Bearbeitungsauftrags
     * @param selectWorkOrderId Optionale Bearbeitungsauftrags-ID für die Selektion
     * @param project
     */
    private void init(String orgId, String selectWorkOrderId, EtkProject project) {
        setMaximumRowCount(20);
        switchOffEventListeners();
        removeAllItems();
        int selectedIndex = -1;

        // Bearbeitungsaufträge laden
        iPartsDataWorkOrderList dataWorkOrderList = null;
        String noWorkOrderText = "";
        if (StrUtils.isValid(orgId)) {
            iPartsUserAdminOrgCache orgCache = iPartsUserAdminOrgCache.getInstance(orgId);
            if (orgCache.isInternalOrganisation()) {
                dataWorkOrderList = iPartsDataWorkOrderList.loadInternalWorkOrderList(project, true);
                if (dataWorkOrderList.isEmpty()) {
                    noWorkOrderText = TranslationHandler.translate("!!<Kein gültiger interner Bearbeitungsauftrag gefunden>");
                }
            } else {
                String bstSupplierId = orgCache.getBSTSupplierId();
                if (!bstSupplierId.isEmpty()) {
                    dataWorkOrderList = iPartsDataWorkOrderList.loadWorkOrderList(project, bstSupplierId, true);
                    if (dataWorkOrderList.isEmpty()) {
                        noWorkOrderText = TranslationHandler.translate("!!<Kein gültiger Bearbeitungsauftrag für die BST Supplier-ID \"%1\" gefunden>",
                                                                       bstSupplierId);
                    }
                } else {
                    noWorkOrderText = TranslationHandler.translate("!!<Organisation \"%1\" hat keine BST Supplier-ID>",
                                                                   TranslationHandler.translate(orgCache.getOrgName(project.getDBLanguage())));
                }
            }
        } else {
            noWorkOrderText = TranslationHandler.translate("!!<Keine Organisation für die Filterung ausgewählt>");
        }

        addItem(null, noWorkOrderText);

        // Benenennungen der Bearbeitungsaufträge bestimmen und Map aufbauen
        boolean selectWorkOrderIdFound = false;
        Map<String, String> workOrderMap = new TreeMap<>(); // Map von Bearbeitungsauftrags-Benennung auf ID
        if ((dataWorkOrderList != null) && !dataWorkOrderList.isEmpty()) {
            boolean isCarAndVan = iPartsRight.checkCarAndVanInSession();
            boolean isTruckAndBus = iPartsRight.checkTruckAndBusInSession();
            for (iPartsDataWorkOrder dataWorkOrder : dataWorkOrderList) {
                // Ist der Bearbeitungsauftrag überhaupt sichtbar für die Eigenschaften des eingeloggten Benutzers?
                if (!dataWorkOrder.isVisibleForUserProperties(isCarAndVan, isTruckAndBus)) {
                    continue;
                }

                String workOrderName = getWorkOrderName(dataWorkOrder);
                String workOrderId = dataWorkOrder.getAsId().getBSTId();
                workOrderMap.put(workOrderName, workOrderId);
                if (workOrderId.equals(selectWorkOrderId)) {
                    selectWorkOrderIdFound = true;
                }
            }
        } else {
            selectedIndex = 0;
        }

        // Bisher ausgewählter Bearbeitungsauftrag ist nicht mehr in der Liste -> explizit hinzufügen
        if (StrUtils.isValid(selectWorkOrderId) && !selectWorkOrderIdFound) {
            // Bei ausgewähltem Bearbeitungsauftrag keine Fehlermeldung anzeigen -> Fehlertext entfernen und leeren Eintrag hinzufügen
            removeItem(0);
            addItem(null, "", null, 0, false);

            iPartsDataWorkOrder dataWorkOrder = new iPartsDataWorkOrder(project, new iPartsWorkOrderId(selectWorkOrderId));
            String workOrderName;
            if (dataWorkOrder.existsInDB()) {
                workOrderName = "<" + getWorkOrderName(dataWorkOrder) + "> ";

                // Prüfen, ob der Bearbeitungsauftrag abgelaufen ist oder es einen anderen Grund dafür gibt, dass er nicht
                // mehr in der Liste enthalten ist
                // "aktuelles Datum" <= "Geplantes Lieferdatum"?
                if (!dataWorkOrder.isValidForCurrentDate()) {
                    workOrderName += TranslationHandler.translate("!!(abgelaufen)");
                } else {
                    workOrderName += TranslationHandler.translate("!!(ungültig für aktuelle Organisation)");
                }
            } else {
                workOrderName = TranslationHandler.translate("!!<Bearbeitungsauftrag mit BST-ID \"%1\" nicht gefunden>",
                                                             selectWorkOrderId);
            }
            workOrderMap.put(workOrderName, selectWorkOrderId);
        }

        // Passende Bearbeitungsaufträge hinzufügen
        for (Map.Entry<String, String> workOrderEntry : workOrderMap.entrySet()) {
            String workOrderId = workOrderEntry.getValue();
            if (workOrderId.equals(selectWorkOrderId)) {
                selectedIndex = getItemCount();
            }
            addItem(workOrderId, workOrderEntry.getKey());
        }

        setSelectedIndex(selectedIndex);
        setEnabled(getItemCount() > 0);
        switchOnEventListeners();
    }

    private String getWorkOrderName(iPartsDataWorkOrder dataWorkOrder) {
        return dataWorkOrder.getTitle() + " (" + dataWorkOrder.getReleaseNo() + ")";
    }
}