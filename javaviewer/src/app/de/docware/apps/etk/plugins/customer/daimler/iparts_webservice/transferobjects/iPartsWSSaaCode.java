/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObject;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;

/**
 * SAA Code Data Transfer Object für die iParts Webservices
 * Wird verwendet von {@link iPartsWSSaCode}
 */
public class iPartsWSSaaCode extends WSRequestTransferObject {

    private String code;
    private String description;

    /**
     * Leerer Konstruktor (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSSaaCode() {
    }

    public iPartsWSSaaCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    // Getter and Setter
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

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        // alles außer code darf leer sein
        checkAttribValid(path, "code", code);
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ code };
    }
}
