/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * PartInfo Data Transfer Object für die iParts Webservices
 * Ausgabe für den GetPartInfo Webservice
 */
public class iPartsWSPartInfo implements RESTfulTransferObjectInterface {

    private List<iPartsWSCodeValidityDetail> codeValidityDetails;
    private List<iPartsWSSaCode> saaValidityDetails;
    private List<iPartsWSColorInfo> colors;
    private List<iPartsWSAlternativePart> alternativeParts;
    private List<iPartsWSPlantInformation> plantInformation;
    private List<List<iPartsWSCodeMatrixElement>> codeValidityMatrix;
    private List<iPartsWSWiringHarness> wiringHarnessKit;
    private List<iPartsWSEinPAS> einPAS;

    /**
     * Leerer Konstruktor (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSPartInfo() {
    }

    public void assignRMIValues(List<iPartsWSColorInfo> colors, List<iPartsWSPlantInformation> plantInformation) {
        setColors(colors);
        setPlantInformation(plantInformation);
    }

    public void assignNonRMIValues(List<iPartsWSCodeValidityDetail> codeValidityDetails, List<iPartsWSSaCode> saaValidityDetails,
                                   List<iPartsWSAlternativePart> alternativeParts, List<List<iPartsWSCodeMatrixElement>> codeValidityMatrix) {
        setCodeValidityDetails(codeValidityDetails);
        setSaaValidityDetails(saaValidityDetails);
        setAlternativeParts(alternativeParts);
        setCodeValidityMatrix(codeValidityMatrix);
    }

    // Getter and Setter
    public List<iPartsWSCodeValidityDetail> getCodeValidityDetails() {
        return codeValidityDetails;
    }

    public void setCodeValidityDetails(List<iPartsWSCodeValidityDetail> codeValidityDetails) {
        this.codeValidityDetails = codeValidityDetails;
    }

    public List<iPartsWSSaCode> getSaaValidityDetails() {
        return saaValidityDetails;
    }

    public void setSaaValidityDetails(List<iPartsWSSaCode> saaValidityDetails) {
        this.saaValidityDetails = saaValidityDetails;
    }

    public List<iPartsWSColorInfo> getColors() {
        return colors;
    }

    public void setColors(List<iPartsWSColorInfo> colors) {
        this.colors = colors;
    }

    public List<iPartsWSAlternativePart> getAlternativeParts() {
        return alternativeParts;
    }

    public void setAlternativeParts(List<iPartsWSAlternativePart> alternativeParts) {
        this.alternativeParts = alternativeParts;
    }

    public List<iPartsWSPlantInformation> getPlantInformation() {
        return plantInformation;
    }

    public void setPlantInformation(List<iPartsWSPlantInformation> plantInformation) {
        this.plantInformation = plantInformation;
    }

    public List<List<iPartsWSCodeMatrixElement>> getCodeValidityMatrix() {
        return codeValidityMatrix;
    }

    public void setCodeValidityMatrix(List<List<iPartsWSCodeMatrixElement>> codeValidityMatrix) {
        this.codeValidityMatrix = codeValidityMatrix;
    }

    public List<iPartsWSWiringHarness> getWiringHarnessKit() {
        return wiringHarnessKit;
    }

    public void setWiringHarnessKit(List<iPartsWSWiringHarness> wiringHarnessKit) {
        this.wiringHarnessKit = wiringHarnessKit;
    }

    public List<iPartsWSEinPAS> getEinPAS() {
        return einPAS;
    }

    public void setEinPAS(List<iPartsWSEinPAS> einPAS) {
        this.einPAS = einPAS;
    }
}
