/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.validateparts;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSIdentContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserWrapper;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.framework.modules.webservice.restful.WSError;

/**
 * Request Data Transfer Object für den validateParts-Webservice (Validierung mehrerer Teilenummern gegen eine FIN)
 *
 * Beispiel: finOrVin=WDD2052041F002981&parts=A4273370152,A4273370052,A4273370352
 */
public class iPartsWSValidatePartsRequest extends iPartsWSUserWrapper {

    private String finOrVin;
    private String parts;
    private iPartsWSIdentContext identContext;

    /**
     * Leerer Konstruktur (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSValidatePartsRequest() {
    }

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        super.checkIfValid(path);

        checkAttribValid(path, "finOrVin", finOrVin);
        checkAttribValid(path, "parts", parts);
        if ((parts != null) && (parts.contains("*") || parts.contains("?"))) {
            throwRequestError(WSError.REQUEST_PARAMETER_WRONG, "Attribute 'parts' must not contain wildcards: " + parts, path);
        }
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ finOrVin, parts, getUser() };
    }

    public String getFinOrVin() {
        return finOrVin;
    }

    public void setFinOrVin(String finOrVin) {
        this.finOrVin = finOrVin;
    }

    public String getParts() {
        return parts;
    }

    public void setParts(String parts) {
        this.parts = parts;
    }

    @JsonIgnore
    public iPartsWSIdentContext getIdentContext() {
        return identContext;
    }

    @JsonIgnore
    public void setIdentContext(iPartsWSIdentContext identContext) {
        this.identContext = identContext;
    }
}