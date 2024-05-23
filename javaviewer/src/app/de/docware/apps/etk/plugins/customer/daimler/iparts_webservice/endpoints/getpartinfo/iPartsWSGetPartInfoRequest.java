/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getpartinfo;

import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSIdentContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSPartContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserWrapper;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;

/**
 * Request Data Transfer Object für den GetPartInfo-Webservice
 *
 * Beispiele:
 * -- codeValidityDetails, Colors, plantInformation
 * - {"identContext":{"aggTypeId":"F","productClassIds":["P"],"modelId":"C205002","productId":"C01","modelTypeId":"C205"}, "partContext" : {"moduleId" : "C01_68_485_00001","sequenceId" : "00019"}, "user":{"country":"200","language":"de","userId":"userId"} }
 *
 * -- saaValidityDetails
 * - {"user" : {"country" : "200","language" : "de","userId" : "userId"}, "partContext" : {"moduleId" : "30H_29_090_00001","sequenceId" : "00029"}, "identContext" : {"aggTypeId" : "F","productClassIds" : ["L"],"modelId" : "C950003","productId" : "30H"} }
 *
 * -- plantInformation mit exceptionIdents
 * - {"user" : {"country" : "200","language" : "de","userId" : "userId"}, "partContext" : {"moduleId" : "C01_82_120_00001","sequenceId" : "00008"}, "identContext" : {"aggTypeId" : "F","productClassIds" : ["P"],"modelId" : "C205002","productId" : "C01"} }
 *
 * -- sequenceId: 00035 wird durch Baumuster Filter ausgefiltert (sequenceId: 00036) nicht
 * - { "user" : {"country" : "200","language" : "de","userId" : "userId"}, "partContext" : {"moduleId" : "C01_29_015_00001","sequenceId" : "00035"}, "identContext" : {"aggTypeId" : "F","productClassIds" : ["P"],"modelId" : "C205003","productId" : "C01"} }
 */
public class iPartsWSGetPartInfoRequest extends iPartsWSUserWrapper {

    protected iPartsWSPartContext partContext;
    private iPartsWSIdentContext identContext;
    private boolean integratedNavigation;

    public iPartsWSGetPartInfoRequest() {
    }

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        // user, partContext und identContext müssen gefüllt sein
        super.checkIfValid(path);
        checkAttribValid(path, "partContext", partContext);
        checkAttribValid(path, "identContext", identContext);
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ partContext, identContext, getUser(), integratedNavigation };
    }

    public iPartsWSPartContext getPartContext() {
        return partContext;
    }

    public void setPartContext(iPartsWSPartContext partContext) {
        this.partContext = partContext;
    }

    public iPartsWSIdentContext getIdentContext() {
        return identContext;
    }

    public void setIdentContext(iPartsWSIdentContext identContext) {
        this.identContext = identContext;
    }

    public boolean isIntegratedNavigation() {
        return integratedNavigation;
    }

    public void setIntegratedNavigation(boolean integratedNavigation) {
        this.integratedNavigation = integratedNavigation;
    }
}