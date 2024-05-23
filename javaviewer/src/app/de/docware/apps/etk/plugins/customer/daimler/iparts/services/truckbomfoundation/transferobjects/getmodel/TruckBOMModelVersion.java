/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getmodel;

import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.TruckBOMMultiLangData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.TruckBOMText;

import java.util.List;

/**
 * Repräsentiert eine Baumusterversion im JSON für die Baumuster-Stammdaten aus der TruckBOM.foundation
 */
public class TruckBOMModelVersion extends TruckBOMMultiLangData {

    private String id;
    private String version;
    private List<TruckBOMText> salesDescription;
    private List<TruckBOMText> technicalData;

    public TruckBOMModelVersion() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<TruckBOMText> getSalesDescription() {
        return salesDescription;
    }

    public void setSalesDescription(List<TruckBOMText> salesDescription) {
        this.salesDescription = salesDescription;
    }

    public List<TruckBOMText> getTechnicalData() {
        return technicalData;
    }

    public void setTechnicalData(List<TruckBOMText> technicalData) {
        this.technicalData = technicalData;
    }

}
