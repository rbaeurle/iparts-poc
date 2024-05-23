package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.code;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Repräsentiert ein Code-Benennung im JSON Code-Benennungen Importer
 */
public class CodeDescription implements RESTfulTransferObjectInterface {

    private String content;
    private String lang;

    public CodeDescription() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
}
