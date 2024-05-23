package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * Ergebnis des SearchComponent Webservice
 */
public class iPartsWSSAAResult implements RESTfulTransferObjectInterface {

    private iPartsWSIdentContext identContext;
    private List<iPartsWSNavNode> navContext; //optional

    /**
     * Leerer Konstruktor (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSSAAResult() {
    }

    public iPartsWSSAAResult(iPartsWSIdentContext identContext, List<iPartsWSNavNode> navContext) {
        this.navContext = navContext;
        this.identContext = identContext;
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
}
