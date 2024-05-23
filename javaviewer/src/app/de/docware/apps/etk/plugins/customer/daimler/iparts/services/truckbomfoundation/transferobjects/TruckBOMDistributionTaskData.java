package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Repräsentiert die Werksverteiler-Daten im JSON für die Teilestammdaten, SAA-Stammdaten, Baukastenstrukturdaten aus TruckBOM.foundation
 */
public class TruckBOMDistributionTaskData implements RESTfulTransferObjectInterface {

    private String id;
    private String distributionDestinationIdentifier;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDistributionDestinationIdentifier() {
        return distributionDestinationIdentifier;
    }

    public void setDistributionDestinationIdentifier(String distributionDestinationIdentifier) {
        this.distributionDestinationIdentifier = distributionDestinationIdentifier;
    }
}
