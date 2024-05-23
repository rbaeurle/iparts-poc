/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getmodel.TruckBOMModelProductGroup;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Enthält alle Daten, die über die "Associations" mit den Hauptdaten verknüpft sind
 */
public class TruckBOMAssociatedData extends TruckBOMBaseData {

    private List<TruckBOMAssociation> association;
    private List<TruckBOMSingleKEM> engineeringOrder;
    private List<TruckBOMModelProductGroup> productGroup;
    private List<TruckBOMDistributionTaskData> distributionTask;

    public TruckBOMAssociatedData() {
    }

    public List<TruckBOMSingleKEM> getEngineeringOrder() {
        return engineeringOrder;
    }

    public void setEngineeringOrder(List<TruckBOMSingleKEM> engineeringOrder) {
        this.engineeringOrder = engineeringOrder;
    }

    public List<TruckBOMAssociation> getAssociation() {
        return association;
    }

    public void setAssociation(List<TruckBOMAssociation> association) {
        this.association = association;
    }

    public List<TruckBOMModelProductGroup> getProductGroup() {
        return productGroup;
    }

    public void setProductGroup(List<TruckBOMModelProductGroup> productGroup) {
        this.productGroup = productGroup;
    }

    public List<TruckBOMDistributionTaskData> getDistributionTask() {
        return distributionTask;
    }

    public void setDistributionTask(List<TruckBOMDistributionTaskData> distributionTask) {
        this.distributionTask = distributionTask;
    }

    /**
     * Liefert eine Map mit Verknüpfung KEM zu KEM Daten
     *
     * @return
     */
    @JsonIgnore
    private Map<String, TruckBOMSingleKEM> getKemToKemDataMap() {
        List<TruckBOMSingleKEM> kemData = getEngineeringOrder();
        if ((kemData == null) || kemData.isEmpty()) {
            return null;
        }
        // Map KEM Id auf die KEM um später besser zu verknüpfen
        return kemData.stream().collect(Collectors.toMap(TruckBOMSingleKEM::getId, value -> value, (value, nextValue) -> {
            int result = value.getReleaseDate().compareTo(nextValue.getReleaseDate());
            TruckBOMSingleKEM resultObject;
            if (result >= 1) {
                resultObject = value;
            } else {
                resultObject = nextValue;
            }
            Logger.log(iPartsPlugin.LOG_CHANNEL_TRUCK_BOM_FOUNDATION, LogType.DEBUG, "Found two KEM datasets " +
                                                                                     "for the same idFrom: " +
                                                                                     "first dataset: \"" + value.getDataAsString() + "\", " +
                                                                                     "second dataset: \"" + nextValue.getDataAsString() + "\"." +
                                                                                     "The dataset with the newer release date will be used for the import: "
                                                                                     + resultObject.getDataAsString());
            return resultObject;
        }));
    }

    /**
     * Liefert eine Map mit allen Id zu KEM Daten Verknüpfungen zur übergebenen Association-Rolle
     */
    @JsonIgnore
    private Map<String, TruckBOMSingleKEM> getAssociationFromIDsToKEMData(String role) {
        List<TruckBOMAssociation> kemAssociations = getAssociation();
        if ((kemAssociations == null) || kemAssociations.isEmpty()) {
            return null;
        }
        Map<String, TruckBOMSingleKEM> kemIdToKemData = getKemToKemDataMap();
        if ((kemIdToKemData == null) || kemIdToKemData.isEmpty()) {
            return null;
        }
        return kemAssociations.stream()
                .filter(entry -> (entry.getRole() != null) && entry.getRole().equals(role) && kemIdToKemData.containsKey(entry.getIdTo()))
                .collect(Collectors.toMap(TruckBOMAssociation::getIdFrom, value -> kemIdToKemData.get(value.getIdTo()), (value, nextValue) -> {
                    if (!value.getDataAsString().equals(nextValue.getDataAsString())) {
                        Logger.log(iPartsPlugin.LOG_CHANNEL_TRUCK_BOM_FOUNDATION, LogType.DEBUG, "Found two different " +
                                                                                                 "KEM datasets for the same idFrom: " +
                                                                                                 "first dataset: \"" + value.getDataAsString() + "\", " +
                                                                                                 "second dataset: \"" + nextValue.getDataAsString() + "\"." +
                                                                                                 " \"" + value.getId() + "\" will be used for the import.");
                    }
                    return value;
                }));
    }

    /**
     * Liefert eine Map mit allen Id zu KEM ab Daten Verknüpfungen
     */
    @JsonIgnore
    public Map<String, TruckBOMSingleKEM> getAssociationFromIDsToKEMFromDataMap() {
        return getAssociationFromIDsToKEMData("engineeringChangeOrderFrom");
    }

    /**
     * Liefert eine Map mit allen Id zu KEM bis Daten Verknüpfungen
     */
    @JsonIgnore
    public Map<String, TruckBOMSingleKEM> getAssociationToIDsToKEMToDataMap() {
        return getAssociationFromIDsToKEMData("engineeringChangeOrderTo");
    }

    /**
     * Liefert eine Map mit allen Model-Ids zu Produktgruppen Verknüpfungen
     */
    @JsonIgnore
    public Map<String, String> getAssociationFromIDsToProductGroupDataMap() {
        List<TruckBOMAssociation> kemAssociations = getAssociation();
        if ((kemAssociations == null) || kemAssociations.isEmpty()) {
            return null;
        }
        if ((productGroup == null) || productGroup.isEmpty()) {
            return null;
        }
        // Erst eine Map mit Produktgruppen-ID auf Produktgruppe
        Map<String, String> productGroupMap = productGroup.stream()
                .collect(Collectors.toMap(TruckBOMModelProductGroup::getId, TruckBOMModelProductGroup::getIdentifier, (value, nextValue) -> {
                    if (!value.equals(nextValue)) {
                        Logger.log(iPartsPlugin.LOG_CHANNEL_TRUCK_BOM_FOUNDATION, LogType.DEBUG, "Found two different product groups with the same product group ID!");

                    }
                    return nextValue;
                }));
        // Als Ergebnis wird eine Map geliefert mit der FromId aus der Assoziation auf die Produktgruppe, die hinter
        // ToId aus der Assoziation steht
        return kemAssociations.stream()
                .filter(entry -> ((entry.getRole() == null) || entry.getRole().isEmpty()) && productGroupMap.containsKey(entry.getIdTo()))
                .collect(Collectors.toMap(TruckBOMAssociation::getIdFrom, value -> productGroupMap.get(value.getIdTo()), (value, nextValue) -> {
                    if (!value.equals(nextValue)) {
                        Logger.log(iPartsPlugin.LOG_CHANNEL_TRUCK_BOM_FOUNDATION, LogType.DEBUG, "Found two different product groups for the same model ID!");
                    }
                    return value;
                }));
    }

    /**
     * Liefert eine Map mit allen AssociationFromId zu Werksverteiler Verknüpfung
     */
    @JsonIgnore
    public Map<String, Set<String>> getAssociationFromIDsToDistributionTaskDataMap() {
        List<TruckBOMAssociation> kemAssociations = getAssociation();
        if ((kemAssociations == null) || kemAssociations.isEmpty()) {
            return null;
        }
        if ((distributionTask == null) || distributionTask.isEmpty()) {
            return null;
        }
        // Erst eine Map mit Werksverteiler-ID zu Werksverteiler
        Map<String, String> distributionTaskDataMap = distributionTask.stream()
                .collect(Collectors.toMap(TruckBOMDistributionTaskData::getId, TruckBOMDistributionTaskData::getDistributionDestinationIdentifier, (value, nextValue) -> {
                    if (!value.equals(nextValue)) {
                        Logger.log(iPartsPlugin.LOG_CHANNEL_TRUCK_BOM_FOUNDATION, LogType.DEBUG, "Found two different distribution tasks" +
                                                                                                 " with the same association ID!");

                    }
                    return nextValue;
                }));
        // Als Ergebnis wird eine Map geliefert mit der FromId aus der Assoziation auf den Werksverteiler, die hinter
        // ToId aus der Assoziation steht
        return kemAssociations.stream()
                .filter(entry -> ((entry.getRole() == null) || entry.getRole().isEmpty()) && distributionTaskDataMap.containsKey(entry.getIdTo()))
                .collect(Collectors.groupingBy(TruckBOMAssociation::getIdFrom,
                                               Collectors.mapping(value -> distributionTaskDataMap.get(value.getIdTo()),
                                                                  Collectors.toCollection(TreeSet::new))));
    }
}
