/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

public class iPartsWSactiveProductDate implements RESTfulTransferObjectInterface {
//    Nicht benötigte Parameter
//    private String ckdApprovalDate;
//    private String confirmationDate;
//    private String dateOfFirstRegistration;
//    private String deliveryDate;
//    private String endOfWarranty;
//    private int objectId;
//    private String productionDate;
//    private String shipmentDate;
//    private String startOfWarranty;
//    private String transferDate;
//    private String validSince;
//    private String value;

    private String technicalApprovalDate;

    public iPartsWSactiveProductDate() {
    }

    public String getTechnicalApprovalDate() {
        return technicalApprovalDate;
    }

    public void setTechnicalApprovalDate(String technicalApprovalDate) {
        this.technicalApprovalDate = technicalApprovalDate;
    }
}
