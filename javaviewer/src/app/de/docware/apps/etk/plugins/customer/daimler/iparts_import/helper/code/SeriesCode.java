package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.code;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * Repräsentiert ein Code im JSON Code-Benennungen Importer
 */
public class SeriesCode implements RESTfulTransferObjectInterface {

    private String shortName;
    private String validFrom;
    private String validTo;
    private CodeType codeType;
    private List<CodeDescription> descriptions;
    private String id;

    public SeriesCode() {
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(String validFrom) {
        this.validFrom = validFrom;
    }

    public String getValidTo() {
        return validTo;
    }

    public void setValidTo(String validTo) {
        this.validTo = validTo;
    }

    public CodeType getCodeType() {
        return codeType;
    }

    public void setCodeType(CodeType codeType) {
        this.codeType = codeType;
    }

    public List<CodeDescription> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<CodeDescription> descriptions) {
        this.descriptions = descriptions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
