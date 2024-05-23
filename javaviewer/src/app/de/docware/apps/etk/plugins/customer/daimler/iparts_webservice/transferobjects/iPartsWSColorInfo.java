/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * ColorInfo Data Transfer Object für die iParts Webservices
 */
public class iPartsWSColorInfo implements RESTfulTransferObjectInterface {

    private String es2Key;
    private String name;
    private String codeValidity;
    private List<iPartsWSCodeValidityDetail> codeValidityDetails;
    private List<List<iPartsWSCodeMatrixElement>> codeValidityMatrix;
    private List<iPartsWSPlantInformation> plantInformation;

    /**
     * Leerer Konstruktor (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSColorInfo() {
    }

    public void assignRMIValues(String es2Key, List<iPartsWSPlantInformation> plantInformation) {
        setEs2Key(es2Key);
        setPlantInformation(plantInformation);
    }

    public void assignNonRMIValues(String name, String codeValidity, List<iPartsWSCodeValidityDetail> codeValidityDetails,
                                   List<List<iPartsWSCodeMatrixElement>> codeValidityMatrix) {
        setName(name);
        setCodeValidity(codeValidity);
        setCodeValidityDetails(codeValidityDetails);
        setCodeValidityMatrix(codeValidityMatrix);
    }

    // Getter and Setter
    public List<iPartsWSPlantInformation> getPlantInformation() {
        return plantInformation;
    }

    public void setPlantInformation(List<iPartsWSPlantInformation> plantInformation) {
        this.plantInformation = plantInformation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEs2Key() {
        return es2Key;
    }

    public void setEs2Key(String es2Key) {
        this.es2Key = es2Key;
    }

    public List<iPartsWSCodeValidityDetail> getCodeValidityDetails() {
        return codeValidityDetails;
    }

    public void setCodeValidityDetails(List<iPartsWSCodeValidityDetail> codeValidityDetails) {
        this.codeValidityDetails = codeValidityDetails;
    }

    public String getCodeValidity() {
        return codeValidity;
    }

    public void setCodeValidity(String codeValidity) {
        this.codeValidity = codeValidity;
    }

    public List<List<iPartsWSCodeMatrixElement>> getCodeValidityMatrix() {
        return codeValidityMatrix;
    }

    public void setCodeValidityMatrix(List<List<iPartsWSCodeMatrixElement>> codeValidityMatrix) {
        this.codeValidityMatrix = codeValidityMatrix;
    }
}
