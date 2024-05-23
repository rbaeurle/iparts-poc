/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;


public class iPartsWSactiveAssignedFpd implements RESTfulTransferObjectInterface {

    private List<iPartsWSequipmentCodes> equipmentCodes;
    private List<iPartsWSsaa> saa;
    private List<iPartsWSvpd> vpd;

    public iPartsWSactiveAssignedFpd() {
    }

    public List<iPartsWSequipmentCodes> getEquipmentCodes() {
        return equipmentCodes;
    }

    public void setEquipmentCodes(List<iPartsWSequipmentCodes> equipmentCodes) {
        this.equipmentCodes = equipmentCodes;
    }

    public List<iPartsWSsaa> getSaa() {
        return saa;
    }

    public void setSaa(List<iPartsWSsaa> saa) {
        this.saa = saa;
    }

    public List<iPartsWSvpd> getVpd() {
        return vpd;
    }

    public void setVpd(List<iPartsWSvpd> vpd) {
        this.vpd = vpd;
    }
}
