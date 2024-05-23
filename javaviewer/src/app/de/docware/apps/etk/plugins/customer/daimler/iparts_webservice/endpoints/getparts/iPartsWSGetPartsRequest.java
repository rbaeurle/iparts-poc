/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getparts;

import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSNavHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSIdentContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSNavNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserWrapper;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;

import java.util.List;

/**
 * Request Data Transfer Object für den GetParts-Webservice
 *
 * Beispiele:
 *
 * -- Korrekte JSON-Response für KG/TU 15/105 für Produkt "C204_KGTU" mit einem Bild
 * - {"identContext":{"aggTypeId":"F","productClassIds":["P"],"modelId":"C208344","productId":"45Y"},"navContext":[{"type":"cg_group","id":"21"},{"type":"cg_subgroup","id":"050"}],"user":{"country":"200","language":"de","userId":"userId"}}
 *
 * -- Korrekte JSON-Response für EinPAS-Modul 39/15/12 für Produkt "C204" mit keinem Bild
 * - {"identContext":{"aggTypeId":"F","productClassIds":["P"],"modelId":"C204001","productId":"C204"},"navContext":[{"type":"maingroup","id":"39"},{"type":"group","id":"15"},{"type":"subgroup","id":"12"}],"user":{"country":"200","language":"de","userId":"TRKA_tec_00"}}
 *
 * -- Korrekte JSON-Response für KG/TU 25/015 und Modul C204_25_15 für Produkt "C204_KGTU" mit zwei Bildern
 * - {"identContext":{"aggTypeId":"F","productClassIds":["P"],"modelId":"C204001","productId":"C204_KGTU"},"navContext":[{"type":"cg_group","id":"25"},{"type":"cg_subgroup","id":"015"},{"type":"module","id":"C204_25_15"}],"user":{"country":"200","language":"de","userId":"userId"}}
 *
 * -- 400 Bad Request (kein Modul gefunden)
 * - {"identContext":{"aggTypeId":"F","productClassIds":["P"],"modelId":"C204001","productId":"C204_KGTU"},"navContext":[{"type":"cg_group","id":"25"},{"type":"cg_subgroup","id":"016"}],"user":{"country":"200","language":"de","userId":"userId"}}
 *
 * -- 400 Bad Request (mehr als ein Modul gefunden)
 * - {"identContext":{"aggTypeId":"F","productClassIds":["P"],"modelId":"C204001","productId":"C204_KGTU"},"navContext":[{"type":"cg_group","id":"25"},{"type":"cg_subgroup","id":"015"}],"user":{"country":"200","language":"de","userId":"userId"}}
 *
 * -- 400 Bad Request (ungültiger letzter NavNode-Typ)
 * - {"identContext":{"aggTypeId":"F","productClassIds":["P"],"modelId":"C204001","productId":"C204_KGTU"},"navContext":[{"type":"cg_group","id":"25"}],"user":{"country":"200","language":"de","userId":"userId"}}
 *
 * -- 400 Bad Request (Inputparameter navContext fehlt)
 * - {"identContext":{"aggTypeId":"F","productClassIds":["P"],"modelId":"C204001","productId":"C204_KGTU"},"user":{"country":"200","language":"de","userId":"userId"}}
 */
public class iPartsWSGetPartsRequest extends iPartsWSUserWrapper {

    private iPartsWSIdentContext identContext;
    private List<iPartsWSNavNode> navContext;
    private boolean integratedNavigation;
    private boolean includeReplacementChain;
    private boolean includeAlternativeParts;

    public iPartsWSGetPartsRequest() {
    }

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        // nur user, identContext und navContext müssen gesetzt sein
        super.checkIfValid(path);
        checkAttribValid(path, "identContext", identContext);

        iPartsWSNavHelper.checkIfNavContextValid(navContext, false, path);
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        // Eventuell reicht auch der navNode des letzten Knotens in navContext. Bei einer nicht-virtuellen Stückliste wäre das
        // jedenfalls so und bei Wiederverwendung hätte man hier einen Vorteil. Bei Daimler ist das noch nicht klar.
        return new Object[]{ identContext, navContext, getUser(), integratedNavigation, includeReplacementChain, includeAlternativeParts };
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

    public boolean isIncludeReplacementChain() {
        return includeReplacementChain;
    }

    public void setIncludeReplacementChain(boolean includeReplacementChain) {
        this.includeReplacementChain = includeReplacementChain;
    }

    public boolean isIncludeAlternativeParts() {
        return includeAlternativeParts;
    }

    public void setIncludeAlternativeParts(boolean includeAlternativeParts) {
        this.includeAlternativeParts = includeAlternativeParts;
    }
}