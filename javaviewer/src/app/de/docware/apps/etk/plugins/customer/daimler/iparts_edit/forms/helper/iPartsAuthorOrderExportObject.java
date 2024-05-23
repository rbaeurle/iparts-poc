/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst.iPartsDataWorkOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst.iPartsDataWorkOrderTask;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsColorTableContentId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrder;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Hilfsklasse für den Export von abrechnungsrelevanten Objektinformationen. Jede Klasse enthält die Kopfdaten und
 * abrechnungsrelevanten Einträge pro Autorenauftrag.
 */
public class iPartsAuthorOrderExportObject implements iPartsConst {

    private iPartsDataChangeSet changeSet;
    private iPartsDataAuthorOrder authorOrder;
    private iPartsDataWorkOrder dataWorkOrder;
    private iPartsDataWorkOrderTask dataWorkOrderTask;
    private List<iPartsDataPicOrder> picOrders;
    private Set<iPartsColorTableContentId> colorTableContentIds;
    private Set<PartListEntryId> partListEntryIds;
    private Map<iPartsDialogBCTEPrimaryKey, Boolean> bcteKeyToAutoCreatedMap;
    private Map<iPartsDialogBCTEPrimaryKey, iPartsAuthorOrderExportFormatter.AUTO_STATE> bcteKeyForAutoStateMap;
    private Set<String> statusChangedData;

    public iPartsAuthorOrderExportObject(EtkProject projectForCalculation, iPartsDataAuthorOrder authorOrder, iPartsDataWorkOrder dataWorkOrder) {
        this.authorOrder = authorOrder;
        this.dataWorkOrder = dataWorkOrder;
        this.changeSet = new iPartsDataChangeSet(projectForCalculation, authorOrder.getChangeSetId());
    }

    public void addPicOrders(List<iPartsDataPicOrder> picOrders) {
        if ((picOrders != null) && !picOrders.isEmpty()) {
            this.picOrders = picOrders;
        }
    }

    public void addPartListEntryIds(Set<PartListEntryId> partListEntryIdsForExport) {
        if ((partListEntryIdsForExport != null) && !partListEntryIdsForExport.isEmpty()) {
            partListEntryIds = partListEntryIdsForExport;
        }
    }

    public iPartsDataChangeSet getChangeSet() {
        return changeSet;
    }

    public iPartsDataAuthorOrder getAuthorOrder() {
        return authorOrder;
    }

    public iPartsDataWorkOrder getDataWorkOrder() {
        return dataWorkOrder;
    }

    public iPartsDataWorkOrderTask getDataWorkOrderTask() {
        return dataWorkOrderTask;
    }

    public List<iPartsDataPicOrder> getPicOrders() {
        return picOrders;
    }

    public Set<PartListEntryId> getPartListEntryIds() {
        return partListEntryIds;
    }

    public void addColorTableContents(Set<iPartsColorTableContentId> colorTableContentIds) {
        this.colorTableContentIds = colorTableContentIds;
    }

    public Set<iPartsColorTableContentId> getColorTableContentIds() {
        return colorTableContentIds;
    }

    public boolean hasBCTEKeys() {
        return (bcteKeyToAutoCreatedMap != null) && !bcteKeyToAutoCreatedMap.isEmpty();
    }

    public boolean hasPicOrders() {
        return (picOrders != null) && !picOrders.isEmpty();
    }

    public boolean hasColorTableContentIds() {
        return (colorTableContentIds != null) && !colorTableContentIds.isEmpty();
    }

    public boolean hasPartListEntryIds() {
        return (partListEntryIds != null) && !partListEntryIds.isEmpty();
    }


    public boolean isEmpty() {
        return !hasBCTEKeys() && !hasColorTableContentIds() && !hasPicOrders() && !hasPartListEntryIds();
    }

    public Map<iPartsDialogBCTEPrimaryKey, Boolean> getBcteKeyToAutoCreatedMap() {
        return bcteKeyToAutoCreatedMap;
    }

    public void setBcteKeyToAutoCreatedMap(Map<iPartsDialogBCTEPrimaryKey, Boolean> bcteKeyToAutoCreatedMap) {
        if ((bcteKeyToAutoCreatedMap != null) && !bcteKeyToAutoCreatedMap.isEmpty()) {
            this.bcteKeyToAutoCreatedMap = bcteKeyToAutoCreatedMap;
        }
    }

    public Map<iPartsDialogBCTEPrimaryKey, iPartsAuthorOrderExportFormatter.AUTO_STATE> getBcteKeyForAutoState() {
        return bcteKeyForAutoStateMap;
    }

    public void setBcteKeyForAutoStateMap(Map<iPartsDialogBCTEPrimaryKey, iPartsAuthorOrderExportFormatter.AUTO_STATE> bcteKeyForAutoStateMap) {
        if ((bcteKeyForAutoStateMap != null) && !bcteKeyForAutoStateMap.isEmpty()) {
            this.bcteKeyForAutoStateMap = bcteKeyForAutoStateMap;
        }
    }

    public Set<String> getStatusChangedData() {
        return statusChangedData;
    }

    public void setStatusChangedData(Set<String> statusChangedData) {
        this.statusChangedData = statusChangedData;
    }
}
