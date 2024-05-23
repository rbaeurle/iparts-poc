/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.PlantInformationType;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.util.StrUtils;

import java.util.List;

/**
 * PlantInformation Data Transfer Object für die iParts Webservices
 */
public class iPartsWSPlantInformation implements RESTfulTransferObjectInterface {

    private String type;
    private String plant;  // jetzt kommasep. Liste aller Werkskennzeichen zu einem Werk
    private String wmi;
    private String ident;
    private String date;
    private String modelYear;
    private List<iPartsWSCodeValidityDetail> modelYearDetails;
    private List<String> exceptionIdents;
    private String text;
    private String aggTypeId;

    /**
     * Leerer Konstruktor (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSPlantInformation() {
    }

    public iPartsWSPlantInformation(EtkProject project, PlantInformationType type) {
        setType(project, type);
    }

    public void assignRMIValues(String wmi, String ident, String date, String modelYear) {
        setWmi(wmi);
        setIdent(ident);
        setDate(date);
        setModelYear(modelYear);
    }

    /**
     * ELDAS Werkseinsatzdaten haben keinen WMI und auch sonst keine Infos, die die normalen Werksdaten-DTOs im RMI Modus
     * enhtalten. Deshalb wird bei ELDAS stattdessen der Text befüllt, sodass das DTO nicht ganz leer ist.
     *
     * @param text
     */
    public void assignELDASRMIValues(String text) {
        setText(text);
    }

    public void assignNonRMIValues(String plant, List<iPartsWSCodeValidityDetail> modelYearDetails,
                                   List<String> exceptionIdents, String text, String aggTypeId) {
        setPlant(plant);
        setModelYearDetails(modelYearDetails);
        setExceptionIdents(exceptionIdents);
        setText(text);
        setAggTypeId(aggTypeId);
    }

    public void assignELDASNonRMIValues(String plant) {
        setPlant(plant);
    }

    public boolean hasContent() {
        return StrUtils.isValid(type) && ((plant != null) || (date != null) || (exceptionIdents != null) || (ident != null)
                                          || (wmi != null) || (text != null));
    }

    // Getter and Setter
    public String getType() {
        return type;
    }

    public void setType(EtkProject project, PlantInformationType type) {
        this.type = type.getDisplayValue(project);
    }

    public void setType(String type) { // Für Genson notwendig
        this.type = type;
    }

    public String getPlant() {
        return plant;
    }

    public void setPlant(String plant) {
        this.plant = plant;
    }

    public String getWmi() {
        return wmi;
    }

    public void setWmi(String wmi) {
        this.wmi = wmi;
    }

    public String getIdent() {
        return ident;
    }

    public void setIdent(String ident) {
        this.ident = ident;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getModelYear() {
        return modelYear;
    }

    public void setModelYear(String modelYear) {
        this.modelYear = modelYear;
    }

    public List<iPartsWSCodeValidityDetail> getModelYearDetails() {
        return modelYearDetails;
    }

    public void setModelYearDetails(List<iPartsWSCodeValidityDetail> modelYearDetails) {
        this.modelYearDetails = modelYearDetails;
    }

    public List<String> getExceptionIdents() {
        return exceptionIdents;
    }

    public void setExceptionIdents(List<String> exceptionIdents) {
        this.exceptionIdents = exceptionIdents;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAggTypeId() {
        return aggTypeId;
    }

    public void setAggTypeId(String aggTypeId) {
        this.aggTypeId = aggTypeId;
    }
}