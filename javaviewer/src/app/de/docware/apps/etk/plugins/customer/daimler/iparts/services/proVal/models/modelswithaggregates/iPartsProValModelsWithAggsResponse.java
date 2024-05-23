/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.proVal.models.modelswithaggregates;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * ProVal Models Service Response Object. Wrapper für alle Baumuster-Objekte inkl. verknüpfter Aggregate
 */
public class iPartsProValModelsWithAggsResponse implements RESTfulTransferObjectInterface {

    private List<iPartsProValModelWithAggs> models;
    private List<iPartsProValDesignNumber> designnumbers;

    public iPartsProValModelsWithAggsResponse() {
    }

    public List<iPartsProValModelWithAggs> getModels() {
        return models;
    }

    public void setModels(List<iPartsProValModelWithAggs> models) {
        this.models = models;
    }

    public List<iPartsProValDesignNumber> getDesignnumbers() {
        return designnumbers;
    }

    public void setDesignnumbers(List<iPartsProValDesignNumber> designnumbers) {
        this.designnumbers = designnumbers;
    }
}
