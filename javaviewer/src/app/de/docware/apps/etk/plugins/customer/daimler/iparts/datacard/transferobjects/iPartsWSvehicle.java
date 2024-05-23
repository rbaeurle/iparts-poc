/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * Eigentlicher DTO mit den Inhalten der Befestigungsteile vom Befestigungsteile-Webservice.
 */
public class iPartsWSvehicle implements RESTfulTransferObjectInterface {

    private List<iPartsWSchassisHoleInformation> chassisHoleInformation;

    public iPartsWSvehicle() {
    }

    public List<iPartsWSchassisHoleInformation> getChassisHoleInformation() {
        return chassisHoleInformation;
    }

    public void setChassisHoleInformation(List<iPartsWSchassisHoleInformation> chassisHoleInformation) {
        this.chassisHoleInformation = chassisHoleInformation;
    }
}
