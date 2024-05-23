/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObject;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;

import java.util.List;

/**
 * SA Code Data Transfer Object für die iParts Webservices
 */
public class iPartsWSSaCode extends WSRequestTransferObject {

    private String code;
    private String description;
    private List<iPartsWSSaaCode> saaCodes;

    /**
     * Leerer Konstruktur (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSSaCode() {
    }

    public iPartsWSSaCode(String code, String description, List<iPartsWSSaaCode> saaCodes) {
        this.code = code;
        this.description = description;
        this.saaCodes = saaCodes;
    }

    // Getter und Setter
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<iPartsWSSaaCode> getSaaCodes() {
        return saaCodes;
    }

    public void setSaaCodes(List<iPartsWSSaaCode> saaCodes) {
        this.saaCodes = saaCodes;
    }

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        // alles außer code darf leer sein
        checkAttribValid(path, "code", code);

        if (saaCodes != null) {
            int i = 0;
            for (iPartsWSSaaCode saaCode : saaCodes) {
                checkAttribValid(path, "saaCodes[" + i + "]", saaCode);
                i++;
            }
        }
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ code, saaCodes };
    }

}