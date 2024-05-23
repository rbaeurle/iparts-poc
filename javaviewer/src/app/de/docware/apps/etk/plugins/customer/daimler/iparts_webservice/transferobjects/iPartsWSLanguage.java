package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

public class iPartsWSLanguage implements RESTfulTransferObjectInterface {

    private String language;
    private String desc;

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getLanguage() {
        return language;
    }

    public String getDesc() {
        return desc;
    }
}
