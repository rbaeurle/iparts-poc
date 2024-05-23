/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.webservice.endpoints.WSAbstractEndpoint;
import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSIdentContext;
import de.docware.framework.modules.webservice.restful.WSError;
import de.docware.util.StrUtils;

import java.util.Set;

/**
 * Hilfsklasse für iParts Webservices zur Behandlung von Spezial-Produkten.
 */
public class iPartsWSSpecialProductHelper {

    /**
     * Überprüft, ob es sich beim übergebenen Produkt um ein Spezial-Produkt handelt und wirft bei Ungültigkeit eine
     * {@link de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException}.
     *
     * @param product
     */
    public static void checkIfSpecialProduct(iPartsProduct product) {
        if (!product.isSpecialCatalog()) {
            WSAbstractEndpoint.throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, "Invalid special product '"
                                                                                     + product.getAsId().getProductNumber()
                                                                                     + "'", null);
        }
    }

    /**
     * Aktiviert den Filter für die AS-Produktklassen-Gültigkeit und andere für Spezial-Produkte passende Filter abhängig
     * von den übergebenen Parametern.
     * AS-Produktklassen-Gültigkeit ist dabei der Enum Key (z.B. "L") und nicht der Enum Wert (z.B. "LKW")
     *
     * @param productClassId
     * @param identContext
     * @param country
     * @param project
     */
    public static void setSpecialProductFilterActive(String productClassId, iPartsWSIdentContext identContext, String country,
                                                     EtkProject project) {
        iPartsFilter filter = iPartsFilter.get();
        filter.setFilterValueProductClass(productClassId);
        if (identContext != null) {
            identContext.setFilterForIdentContext(country, true, project); // ruft intern setAllRetailFilterActiveForSpecialProduct() auf
        } else {
            filter.setAllRetailFilterActiveForSpecialProduct(project, null, true);
        }
    }

    /**
     * Die AssortmentClassId wird nur noch in der Übergangsphase, bis alle Konsumenten auf das neue Attribut productClassId
     * migriert haben, verwendet. Die Id wird direkt mit den AS-Produktklassen am Produktstamm verglichen!
     *
     * @param assortmentClassId
     * @param productClassId
     * @param product
     * @return
     */
    public static String handleAssortmentAndProductClassId(String assortmentClassId, String productClassId, iPartsProduct product) {
        if (StrUtils.isValid(assortmentClassId)) {
            iPartsWSSpecialProductHelper.checkIfProductClassIdIsValid("assortmentClassId", assortmentClassId, product);
            return assortmentClassId;
        } else if (StrUtils.isValid(productClassId)) {
            iPartsWSSpecialProductHelper.checkIfProductClassIdIsValid("productClassId", productClassId, product);
            return productClassId;
        }
        return "";
    }

    /**
     * Die productClassId ist nur gültig, falls sie in den AS-Produktklassen am Produktstamm vorhanden ist.
     *
     * @param productClassId
     * @param product
     * @return
     */
    public static void checkIfProductClassIdIsValid(String name, String productClassId, iPartsProduct product) {
        Set<String> asProductClassesFromProduct = product.getAsProductClasses();
        WSRequestTransferObject.checkAttribListContainsValue(null, name, productClassId, asProductClassesFromProduct);
    }
}
