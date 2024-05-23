/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.partslist;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSIdentContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserWrapper;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.framework.modules.webservice.restful.WSError;
import de.docware.util.StrUtils;

/**
 * Request Data Transfer Object für den partsList-Webservice (Stücklisten-Aufruf für FIN/VIN oder BM6)
 *
 * Beispiele:
 * - Für ein Baumuster mit Sachnummernkennbuchstabe mit 1 Produkt: ?model=C205003
 * - Für ein Baumuster ohne Sachnummernkennbuchstabe mit 1 Produkt: ?model=132910
 * - Für ein Baumuster mit mehreren Produkten: ?model=C205004
 * - Für ein Baumuster mit mehreren Produkten und Produktangabe: ?model=C205004&productId=C01
 * - Für eine FIN mit 1 Produkt: ?finOrVin=WDD2052401F236482
 * - Für eine FIN mit mehreren Produkten: ?finOrVin=WDD2050041F004362
 * - Für eine FIN mit mehreren Produkten und Produktangabe: ?finOrVin=WDD2050041F004362&productId=C01
 * - Für eine FIN mit mehreren Produkten und Produktangabe inkl. Aggregate: ?finOrVin=WDD2050041F004362&productId=C01&includeAggs=true
 * - Für eine VIN mit 1 Produkt: ?finOrVin=WD4PG2EE3G3138415
 */
public class iPartsWSPartsListRequest extends iPartsWSUserWrapper {

    private String model;
    private String finOrVin;
    private String productClassId;
    private String productId;
    private boolean includeAggs;
    private boolean includeVisualNav;
    private boolean extendedDescriptions;
    private boolean reducedInformation;
    private iPartsWSIdentContext identContext;
    private boolean images;

    /**
     * Leerer Konstruktur (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSPartsListRequest() {
    }

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        super.checkIfValid(path);

        // Es muss exklusiv model ODER finOrVin ODER productClassId gesetzt sein
        checkExactlyOneAttribValid(path, new String[]{ "model", "finOrVin", "productClassId" }, new String[]{ model, finOrVin,
                                                                                                              productClassId });

        // Die Inputparameter extendedDescriptions und reducedInformation dürfen nicht zeitgleich aktiv (true) sein
        checkNotMoreThanOneAttribTrue(path, new String[]{ "extendedDescriptions", "reducedInformation" },
                                      new boolean[]{ extendedDescriptions, reducedInformation });

        // model muss ein Baumuster sein
        if (StrUtils.isValid(model) && !(new iPartsModelId(model).isModelNumberValid(false))) {
            String message = "The attribute 'model' must be a valid model number";
            throwRequestError(WSError.REQUEST_PARAMETER_WRONG, message, path);
        }

        // finOrVin darf kein Baumuster sein
        if (StrUtils.isValid(finOrVin) && new iPartsModelId(finOrVin).isModelNumberValid(false)) {
            String message = "The attribute 'finOrVin' must not be a model number";
            throwRequestError(WSError.REQUEST_PARAMETER_WRONG, message, path);
        }

        if (StrUtils.isValid(productClassId) && StrUtils.isEmpty(productId)) {
            String message = "The attribute 'productId' must not be empty in combination with the attribute 'productClassId'";
            throwRequestError(WSError.REQUEST_SEMANTIC_ERROR, message, path);
        }

        if (StrUtils.isValid(productClassId) && (identContext != null)) {
            String message = "The attribute 'identContext' is not valid in combination with the attribute 'productClassId'";
            throwRequestError(WSError.REQUEST_SEMANTIC_ERROR, message, path);
        }

        if (includeAggs && StrUtils.isValid(model)) {
            String message = "The attribute 'includeAggs' is not valid in combination with the attribute 'model'";
            throwRequestError(WSError.REQUEST_SEMANTIC_ERROR, message, path);
        }

        if (includeAggs && StrUtils.isValid(productClassId)) {
            String message = "The attribute 'includeAggs' is not valid in combination with the attribute 'productClassId'";
            throwRequestError(WSError.REQUEST_SEMANTIC_ERROR, message, path);
        }
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ model, finOrVin, productClassId, productId, includeAggs, includeVisualNav, extendedDescriptions,
                             reducedInformation, images, getUser() };
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getFinOrVin() {
        return finOrVin;
    }

    public void setFinOrVin(String finOrVin) {
        this.finOrVin = finOrVin;
    }

    public String getProductClassId() {
        return productClassId;
    }

    public void setProductClassId(String productClassId) {
        this.productClassId = productClassId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public boolean isIncludeAggs() {
        return includeAggs;
    }

    public void setIncludeAggs(boolean includeAggs) {
        this.includeAggs = includeAggs;
    }

    public boolean isIncludeVisualNav() {
        return includeVisualNav;
    }

    public void setIncludeVisualNav(boolean includeVisualNav) {
        this.includeVisualNav = includeVisualNav;
    }

    @JsonIgnore
    public iPartsWSIdentContext getIdentContext() {
        return identContext;
    }

    @JsonIgnore
    public void setIdentContext(iPartsWSIdentContext identContext) {
        this.identContext = identContext;
    }

    public boolean isExtendedDescriptions() {
        return extendedDescriptions;
    }

    public void setExtendedDescriptions(boolean extendedDescriptions) {
        this.extendedDescriptions = extendedDescriptions;
    }

    public boolean isReducedInformation() {
        return reducedInformation;
    }

    public void setReducedInformation(boolean reducedInformation) {
        this.reducedInformation = reducedInformation;
    }

    public boolean isImages() {
        return images;
    }

    public void setImages(boolean images) {
        this.images = images;
    }
}