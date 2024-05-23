/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.ppua;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumEntry;
import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumValue;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserWrapper;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.framework.modules.webservice.restful.WSError;
import de.docware.util.StrUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Request Data Transfer Object für den PPUA-Webservice
 **/
public class iPartsWSPPUARequest extends iPartsWSUserWrapper {

    private List<String> parts;
    private String modelTypeId;
    private List<String> regions;
    private String division;

    public iPartsWSPPUARequest() {
    }

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        // nur User
        super.checkIfValid(path);

        // zusätzlich darf die parts-Liste nicht leer oder ungültig sein
        checkAttribValid(path, "parts", parts);
        boolean wildcardMatNrFound = parts.stream().anyMatch(s -> StrUtils.stringContainsWildcards(s));
        if (wildcardMatNrFound) {
            throwRequestError(WSError.REQUEST_PARAMETER_WRONG, "Wildcards ('*' or '?') are not allowed in attribute list 'parts'", path);
        }

        // ab hier ist parts gültig und nicht leer
        int maxAmountPartNumbersInput = iPartsWebservicePlugin.getPluginConfig()
                .getConfigValueAsInteger(iPartsWebservicePlugin.CONFIG_MAX_PART_NUMBERS_INPUT_PPUA_DATA);
        int partNumbersInput = parts.size();
        if ((maxAmountPartNumbersInput > 0) && (partNumbersInput > maxAmountPartNumbersInput)) {
            throwRequestError(WSError.REQUEST_PARAMETER_WRONG, "Maximum amount of part numbers (" + maxAmountPartNumbersInput
                                                               + ") exceeded for attribute list 'parts': " + parts.size(), path);
        }

        // Prüfen ob die Division - wenn übergeben - den Enums entspricht
        if (StrUtils.isValid(division)) {
            String divisionInput = division.toUpperCase();
            EnumValue enumValue = iPartsPlugin.getMqProject().getEtkDbs().getEnumValue("PpuaEntity");
            if (!enumValue.containsKey(divisionInput)) {
                throwRequestError(WSError.REQUEST_PARAMETER_WRONG, "Attribute 'division' must be one of ["
                                                                   + enumValue.values().stream().map(EnumEntry::getToken).collect(Collectors.toList())
                                                                   + "]: " + divisionInput, path);
            }
        }
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ getUser(), parts, modelTypeId, regions, division };
    }

    public List<String> getParts() {
        return parts;
    }

    public void setParts(List<String> parts) {
        this.parts = parts;
    }

    public String getModelTypeId() {
        return modelTypeId;
    }

    public void setModelTypeId(String modelTypeId) {
        this.modelTypeId = modelTypeId;
    }

    public List<String> getRegions() {
        return regions;
    }

    public void setRegions(List<String> regions) {
        this.regions = regions;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }
}