/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObjectInterface;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterSwitchboard;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;

/**
 * FilterOptions Data Transfer Object für die iParts Webservices
 * Mit diesem DTO wird den Webservices mitgeteilt welche Filter aktiv sein sollen; Teil von IdentContext
 */
public class iPartsWSFilterOptions implements WSRequestTransferObjectInterface {

    private boolean model;
    private boolean datacard;
    private boolean saVersion;
    private boolean steering; // benötigt FIN
    private boolean serial; // benötigt FIN
    private boolean transmission; // benötigt Getriebe Aggregat
    private boolean color; // benötigt Datenkarte
    private boolean codes; // benötigt Datenkarte
    private boolean spring; // benötigt Datenkarte

    /**
     * Leerer Konstruktur (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSFilterOptions() {
    }

    // Getter and Setter
    public boolean isModel() {
        return model;
    }

    public void setModel(boolean model) {
        this.model = model;
    }

    public boolean isDatacard() {
        return datacard;
    }

    public void setDatacard(boolean datacard) {
        this.datacard = datacard;
    }

    public boolean isSaVersion() {
        return saVersion;
    }

    public void setSaVersion(boolean saVersion) {
        this.saVersion = saVersion;
    }

    public boolean isSteering() {
        return steering;
    }

    public void setSteering(boolean steering) {
        this.steering = steering;
    }

    public boolean isSerial() {
        return serial;
    }

    public void setSerial(boolean serial) {
        this.serial = serial;
    }

    public boolean isTransmission() {
        return transmission;
    }

    public void setTransmission(boolean transmission) {
        this.transmission = transmission;
    }

    public boolean isColor() {
        return color;
    }

    public void setColor(boolean color) {
        this.color = color;
    }

    public boolean isCodes() {
        return codes;
    }

    public void setCodes(boolean codes) {
        this.codes = codes;
    }

    public boolean isSpring() {
        return spring;
    }

    public void setSpring(boolean spring) {
        this.spring = spring;
    }

    @JsonIgnore
    public iPartsFilterSwitchboard getAsFilterSwitchboardState() {
        // Konfiguration siehe https://confluence.docware.de/confluence/x/JwCFAQ
        iPartsFilterSwitchboard allFilters = new iPartsFilterSwitchboard();
        if (model) {
            allFilters.setFilterActivated(iPartsFilterSwitchboard.FilterTypes.MODEL, true);
        }
        if (saVersion) {
            allFilters.setFilterActivated(iPartsFilterSwitchboard.FilterTypes.SA_STRICH, true);
        }
        if (steering) {
            allFilters.setFilterActivated(iPartsFilterSwitchboard.FilterTypes.STEERING, true);
        }
        if (serial) {
            allFilters.setFilterActivated(iPartsFilterSwitchboard.FilterTypes.END_NUMBER, true);
        }
        if (transmission) {
            allFilters.setFilterActivated(iPartsFilterSwitchboard.FilterTypes.GEARBOX, true);
        }
        if (color) {
            allFilters.setFilterActivated(iPartsFilterSwitchboard.FilterTypes.EXTENDED_COLOR, true);
        }
        if (codes) {
            allFilters.setFilterActivated(iPartsFilterSwitchboard.FilterTypes.EXTENDED_CODE, true);
        }
        if (spring) {
            allFilters.setFilterActivated(iPartsFilterSwitchboard.FilterTypes.SPRING, true);
        }
        if (datacard) {
            allFilters.setFilterActivated(iPartsFilterSwitchboard.FilterTypes.DATACARD_SA, true);
            allFilters.setFilterActivated(iPartsFilterSwitchboard.FilterTypes.DATACARD_CODE, true);
        }

        // Aggregate-Filter, Ländergültigkeits-Filter, Spezifikations-Filter und Verdichtungs-Filter immer aktivieren
        allFilters.setFilterActivated(iPartsFilterSwitchboard.FilterTypes.AGG_MODELS, true);
        allFilters.setFilterActivated(iPartsFilterSwitchboard.FilterTypes.COUNTRY_VALIDITY_FILTER, true);
        allFilters.setFilterActivated(iPartsFilterSwitchboard.FilterTypes.SPECIFICATION_FILTER, true);
        allFilters.setFilterActivated(iPartsFilterSwitchboard.FilterTypes.REMOVE_DUPLICATES, true);

        // Filter-Hauptschalter ebenfalls aktivieren
        allFilters.setMainSwitchActive(true);

        return allFilters;
    }

    /**
     * Ist kein Filter aktiv?
     *
     * @return
     */
    @JsonIgnore
    public boolean isEmpty() {
        return !model && !datacard && !saVersion && !steering && !serial && !transmission && !color && !codes && !spring;
    }

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        // immer valid, weil alle parameter fehlen, wenn sie false sind
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ isCodes(), isColor(), isDatacard(), isModel(), isSaVersion(), isSerial(), isSpring(),
                             isSteering(), isTransmission() };
    }
}
