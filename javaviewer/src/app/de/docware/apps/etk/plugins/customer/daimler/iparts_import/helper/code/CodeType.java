package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.code;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Repräsentiert einen Code-Typ im JSON Code-Benennungen Importer
 */
public class CodeType implements RESTfulTransferObjectInterface {

    private String name;
    private String shortName;
    private String id;

    public CodeType() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
