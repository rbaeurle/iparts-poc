package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.code;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * Repräsentiert eine Baureihen im JSON Code-Benennungen Importer
 */
public class Series implements RESTfulTransferObjectInterface {

    private String series;
    private List<SeriesCode> codes;

    public Series() {
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public List<SeriesCode> getCodes() {
        return codes;
    }

    public void setCodes(List<SeriesCode> codes) {
        this.codes = codes;
    }
}
