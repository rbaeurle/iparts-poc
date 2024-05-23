/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects;

/**
 * Container DTO für den eigentlichen DTO {@link iPartsWSactiveGeneralMajAssy} mit den Inhalten einer Aggregate-Datenkarte
 * vom Datenkarten-Webservice. Der auch eine {@link iPartsWSDataCardException} enthalten kann.
 */
public class iPartsWSmajAssyRestrictedResponse extends AbstractiPartsDatacardContainerResponse {

    private iPartsWSactiveGeneralMajAssy activeGeneralMajAssy;

    public iPartsWSmajAssyRestrictedResponse() {
    }

    public iPartsWSactiveGeneralMajAssy getActiveGeneralMajAssy() {
        return activeGeneralMajAssy;
    }

    public void setActiveGeneralMajAssy(iPartsWSactiveGeneralMajAssy activeGeneralMajAssy) {
        this.activeGeneralMajAssy = activeGeneralMajAssy;
    }
}
