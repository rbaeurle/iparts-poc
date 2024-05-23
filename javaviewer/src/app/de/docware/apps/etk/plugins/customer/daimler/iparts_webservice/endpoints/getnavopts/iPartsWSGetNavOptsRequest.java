/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getnavopts;

import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSIdentContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSNavNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserWrapper;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;

import java.util.List;

/**
 * Request Data Transfer Object für den GetNavOpts-Webservice
 *
 * Beispiele:
 * Produkt -> KG/TU KG- bzw. EinPAS HG-Knoten
 *
 * - {"identContext":{"aggTypeId":"F","productClassIds":["P"],"modelId":"C204002","productId":"C204"},"user":{"country":"200","language":"de","userId":"TRKA_tec_00"}}
 *
 * // KG/TU KG -> TU-Knoten
 * - {"identContext":{"aggTypeId":"F","productClassIds":["P"],"modelId":"C204001","productId":"C204_KGTU"},"navContext":[{"type":"cg_group","id":"03"}],"user":{"country":"200","language":"de","userId":"TRKA_tec_00"}}
 *
 * // KG/TU TU -> Modul-Knoten
 * - {"identContext":{"aggTypeId":"F","productClassIds":["P"],"modelId":"C204001","productId":"C204_KGTU"},"navContext":[{"type":"cg_group","id":"03"},{"type":"cg_subgroup","id":"015"}],"user":{"country":"200","language":"de","userId":"TRKA_tec_00"}}
 *
 * // EinPAS HG -> G-Knoten
 * - {"identContext":{"aggTypeId":"F","productClassIds":["P"],"modelId":"C204001","productId":"C204"},"navContext":[{"type":"maingroup","id":"21"}],"user":{"country":"200","language":"de","userId":"TRKA_tec_00"}}
 *
 * // EinPAS TU -> Modul-Knoten
 * {"identContext":{"aggTypeId":"F","productClassIds":["P"],"modelId":"C204001","productId":"C204"},"navContext":[{"type":"maingroup","id":"21"},{"type":"group","id":"15"},{"type":"subgroup","id":"30"}],"user":{"country":"200","language":"de","userId":"TRKA_tec_00"}}
 */
public class iPartsWSGetNavOptsRequest extends iPartsWSUserWrapper {

    private iPartsWSIdentContext identContext;
    private List<iPartsWSNavNode> navContext;
    private boolean integratedNavigation;

    public iPartsWSGetNavOptsRequest() {
    }

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        // nur user und identContext müssen gesetzt sein; navContext ist optional, muss aber auch gültig sein falls gesetzt
        super.checkIfValid(path);
        checkAttribValid(path, "identContext", identContext);
        if (navContext != null) {
            int i = 0;
            for (iPartsWSNavNode navNode : navContext) {
                checkAttribValid(path, "navContext[" + i + "]", navNode);
                i++;
            }
        }
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ identContext, navContext, getUser(), integratedNavigation };
    }

    public iPartsWSIdentContext getIdentContext() {
        return identContext;
    }

    public void setIdentContext(iPartsWSIdentContext identContext) {
        this.identContext = identContext;
    }

    public List<iPartsWSNavNode> getNavContext() {
        return navContext;
    }

    public void setNavContext(List<iPartsWSNavNode> navContext) {
        this.navContext = navContext;
    }

    public boolean isIntegratedNavigation() {
        return integratedNavigation;
    }

    public void setIntegratedNavigation(boolean integratedNavigation) {
        this.integratedNavigation = integratedNavigation;
    }
}