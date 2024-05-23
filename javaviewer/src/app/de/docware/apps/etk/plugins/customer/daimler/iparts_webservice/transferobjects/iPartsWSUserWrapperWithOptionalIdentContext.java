/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;

/**
 * Wrapper für das JAVA/JSON Objekt {@link iPartsWSUserInfo} inkl. optionalem {@link iPartsWSIdentContext}
 */
public class iPartsWSUserWrapperWithOptionalIdentContext extends iPartsWSUserWrapper {

    private iPartsWSIdentContext identContext;

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        super.checkIfValid(path);
        if (identContext != null) {
            checkAttribValid(path, "identContext", identContext);
        }
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ identContext, getUser() };
    }

    public iPartsWSIdentContext getIdentContext() {
        return identContext;
    }

    public void setIdentContext(iPartsWSIdentContext identContext) {
        this.identContext = identContext;
    }
}