/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.Collection;
import java.util.List;

/**
 * PartResultWithIdent Data Transfer Object für den iParts SearchPartsWOContext Webservice
 */
public class iPartsWSPartResultWithIdent implements RESTfulTransferObjectInterface {

    private String partNo; // unformatiert = Speicherformat
    private String partNoFormatted;
    private String name; // Benennung
    private List<iPartsWSModelTypeInfo> modelTypes;

    // searchMode=supplierNumber
    private String supplierName;
    private String supplierPartNo;

    // searchMode=masterData
    private String addText;
    private boolean imageAvailable;
    private boolean securitySignRepair;
    private Collection<iPartsWSLanguage> languageData;
    private List<iPartsWSAdditionalPartInformation> additionalPartInformation;

    // DAIMLER-13928: Webservice ppuaData -> zusätzlich PPUA Informationen
    private List<iPartsWSPPUAInformation> ppuaInformation;

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }

    public String getPartNoFormatted() {
        return partNoFormatted;
    }

    public void setPartNoFormatted(String partNoFormatted) {
        this.partNoFormatted = partNoFormatted;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<iPartsWSModelTypeInfo> getModelTypes() {
        return modelTypes;
    }

    public void setModelTypes(List<iPartsWSModelTypeInfo> modelTypes) {
        this.modelTypes = modelTypes;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierPartNo() {
        return supplierPartNo;
    }

    public void setSupplierPartNo(String supplierPartNo) {
        this.supplierPartNo = supplierPartNo;
    }

    public void setAddText(String addText) {
        this.addText = addText;
    }

    public void setImageAvailable(boolean imageAvailable) {
        this.imageAvailable = imageAvailable;
    }

    public void setSecuritySignRepair(boolean securitySignRepair) {
        this.securitySignRepair = securitySignRepair;
    }

    public Collection<iPartsWSLanguage> getLanguageData() {
        return languageData;
    }

    public List<iPartsWSAdditionalPartInformation> getAdditionalPartInformation() {
        return additionalPartInformation;
    }

    public String getAddText() {
        return addText;
    }

    public void setLanguageData(Collection<iPartsWSLanguage> languageData) {
        this.languageData = languageData;
    }

    public boolean isImageAvailable() {
        return imageAvailable;
    }

    public boolean isSecuritySignRepair() {
        return securitySignRepair;
    }

    public void setAdditionalPartInformation(List<iPartsWSAdditionalPartInformation> additionalPartInformation) {
        this.additionalPartInformation = additionalPartInformation;
    }

    public List<iPartsWSPPUAInformation> getPpuaInformation() {
        return ppuaInformation;
    }

    public void setPpuaInformation(List<iPartsWSPPUAInformation> ppuaInformation) {
        this.ppuaInformation = ppuaInformation;
    }
}