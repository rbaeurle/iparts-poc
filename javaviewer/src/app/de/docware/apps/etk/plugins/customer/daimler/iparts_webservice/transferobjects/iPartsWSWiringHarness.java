/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * Leitungssatzbaukasten Data Transfer Object für die iParts Webservices
 */
public class iPartsWSWiringHarness implements RESTfulTransferObjectInterface {

    private String partNumber;
    private String partNumberFormatted;
    private String partNumberType;
    private String name;
    private String nameRef;
    private String es1Key;
    private String es2Key;
    private boolean dsr;
    private boolean pictureAvailable;
    private String referenceNumber;
    private String connectorNumber;
    private String contactAdditionalText;
    private String contactAdditionalTextRef;
    private String materialDesc;
    private String materialDescRef;
    private List<iPartsWSAdditionalPartInformation> additionalPartInformation;

    public iPartsWSWiringHarness() {
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getPartNumberFormatted() {
        return partNumberFormatted;
    }

    public void setPartNumberFormatted(String partNumberFormatted) {
        this.partNumberFormatted = partNumberFormatted;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEs1Key() {
        return es1Key;
    }

    public void setEs1Key(String es1Key) {
        this.es1Key = es1Key;
    }

    public String getEs2Key() {
        return es2Key;
    }

    public void setEs2Key(String es2Key) {
        this.es2Key = es2Key;
    }

    public boolean isDsr() {
        return dsr;
    }

    public void setDsr(boolean dsr) {
        this.dsr = dsr;
    }

    public boolean isPictureAvailable() {
        return pictureAvailable;
    }

    public void setPictureAvailable(boolean pictureAvailable) {
        this.pictureAvailable = pictureAvailable;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getConnectorNumber() {
        return connectorNumber;
    }

    public void setConnectorNumber(String connectorNumber) {
        this.connectorNumber = connectorNumber;
    }

    public String getContactAdditionalText() {
        return contactAdditionalText;
    }

    public void setContactAdditionalText(String contactAdditionalText) {
        this.contactAdditionalText = contactAdditionalText;
    }

    public String getMaterialDesc() {
        return materialDesc;
    }

    public void setMaterialDesc(String materialDesc) {
        this.materialDesc = materialDesc;
    }

    public List<iPartsWSAdditionalPartInformation> getAdditionalPartInformation() {
        return additionalPartInformation;
    }

    public void setAdditionalPartInformation(List<iPartsWSAdditionalPartInformation> additionalPartInformation) {
        this.additionalPartInformation = additionalPartInformation;
    }

    public void setNameRef(String nameRef) {
        this.nameRef = nameRef;
    }

    public void setContactAdditionalTextRef(String contactAdditionalTextRef) {
        this.contactAdditionalTextRef = contactAdditionalTextRef;
    }

    public void setMaterialDescRef(String materialDescRef) {
        this.materialDescRef = materialDescRef;
    }

    public String getNameRef() {
        return nameRef;
    }

    public String getContactAdditionalTextRef() {
        return contactAdditionalTextRef;
    }

    public String getMaterialDescRef() {
        return materialDescRef;
    }

    public String getPartNumberType() {
        return partNumberType;
    }

    public void setPartNumberType(String partNumberType) {
        this.partNumberType = partNumberType;
    }
}
