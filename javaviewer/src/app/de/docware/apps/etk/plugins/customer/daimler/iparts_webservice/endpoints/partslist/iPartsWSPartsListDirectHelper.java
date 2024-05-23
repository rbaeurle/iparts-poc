/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.partslist;

import com.owlike.genson.Genson;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsLanguage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsBranchProductClassCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSPermissions;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserInfo;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.framework.utils.JSONUtils;

import java.util.Set;

/**
 * Hilfsklasse für den direkten Aufruf vom WebService partsList
 */
public class iPartsWSPartsListDirectHelper {

    public static final String REQUEST_USER = "crawler";
    public static final String REQUEST_COUNTRY = "DE";
    private static iPartsWSPartsListEndpoint endPoint = new iPartsWSPartsListEndpoint("modelPartsListExport");


    /**
     * Direkter Aufruf des WebService partsList, um als Ergebnis einen JSON-String zu bekommen.
     * Als Eingabe-Parameter werden benutzt:
     * Baumuster oder AS-ProduktKlasse, Produkt, keine FIN/VIN
     * extendedDescriptions = true
     * reducedInformation = false
     * images = false
     *
     * @param modelOrPC  Baumuster bzw. AS-Produktklasse
     * @param productId
     * @param logChannel
     * @param project
     * @return {@code null} falls es keine gültige Response gab
     */
    public static String callWSPartsListDirectJSON(String modelOrPC, iPartsProductId productId, LogChannels logChannel,
                                                   EtkProject project) {
        iPartsWSPartsListResponse response = callWSPartsListDirectDTO(modelOrPC, productId, logChannel, project);
        if (response != null) {
            return getAsJSON(response);
        }
        return null;
    }

    /**
     * Direkter Aufruf des WebService partsList, um als Ergebnis das DTO {@link iPartsWSPartsListResponse} zu bekommen.
     * Als Eingabe-Parameter werden benutzt:
     * Baumuster oder AS-ProduktKlasse, Produkt, keine FIN/VIN
     * extendedDescriptions = true
     * reducedInformation = false
     * images = false
     *
     * @param modelOrPC  Baumuster bzw. AS-Produktklasse
     * @param productId
     * @param logChannel
     * @param project
     * @return {@code null} falls es keine gültige Response gab
     */
    public static iPartsWSPartsListResponse callWSPartsListDirectDTO(String modelOrPC, iPartsProductId productId, LogChannels logChannel,
                                                                     EtkProject project) {
        iPartsWSUserInfo userInfo = new iPartsWSUserInfo();
        userInfo.setUserId(REQUEST_USER);

        // Gültiges Land für den Request ermitteln
        String requestCountry = REQUEST_COUNTRY;
        iPartsProduct product = iPartsProduct.getInstance(project, productId);
        Set<String> validCountries = product.getValidCountries();
        if (!validCountries.isEmpty()) { // Bei gültigen Ländern zu einem Produkt das erste gültige Land nehmen
            requestCountry = validCountries.iterator().next();
        } else {
            Set<String> invalidCountries = product.getInvalidCountries();
            if (invalidCountries.contains(requestCountry)) { // Bei ungültigen Ländern zu einem Produkt das erste nicht ungültige Land nehmen
                for (String countryCode : iPartsLanguage.getDaimlerIsoCountryCodes(project)) {
                    if (!invalidCountries.contains(countryCode)) {
                        requestCountry = countryCode;
                        break;
                    }
                }
            }
        }
        userInfo.setCountry(requestCountry);

        userInfo.setLang1(Language.DE.getCode());
        userInfo.setLang2(Language.EN.getCode());
        userInfo.setLang3(Language.FR.getCode());
        userInfo.setExp(Long.MAX_VALUE);

        // Volle Permissions
        Set<String> allBranches = iPartsBranchProductClassCache.getInstance(project).getAllBranches();
        iPartsWSPermissions permissions = new iPartsWSPermissions();
        permissions.setBranchesMercedesBenz(allBranches);
        permissions.setBranchesMaybach(allBranches);
        permissions.setBranchesSmart(allBranches);
        userInfo.setPermissions(permissions);

        String securePayload = getAsJSON(userInfo);

        boolean isSpecialProduct = product.isSpecialCatalog();
        try {
            RESTfulTransferObjectInterface responseObject = callPartsList(endPoint, isSpecialProduct ? null : modelOrPC,
                                                                          isSpecialProduct ? modelOrPC : null, productId,
                                                                          false, true, false, false, securePayload);
            if (responseObject instanceof iPartsWSPartsListResponse) {
                return (iPartsWSPartsListResponse)responseObject;
            }
        } catch (RESTfulWebApplicationException e) {
            Logger.log(logChannel, LogType.ERROR, "Error during parts lists export of " + (isSpecialProduct ? "AS product class "
                                                                                                            : "model ")
                                                  + modelOrPC + " for product " + productId + ": " + e.getErrorMessage());
            Logger.logExceptionWithoutThrowing(logChannel, LogType.ERROR, e);
        }
        return null;
    }

    private static RESTfulTransferObjectInterface callPartsList(iPartsWSPartsListEndpoint endPoint, String model, String productClassId,
                                                                iPartsProductId productId, boolean includeAggs, boolean extendedDescriptions,
                                                                boolean reducedInformation, boolean images, String securePayload) {
        iPartsWSPartsListRequest partsListRequest = new iPartsWSPartsListRequest();
        partsListRequest.setModel(model);
        partsListRequest.setProductClassId(productClassId);
        partsListRequest.setProductId(productId.getProductNumber());
        partsListRequest.setIncludeAggs(includeAggs);
        partsListRequest.setExtendedDescriptions(extendedDescriptions);
        partsListRequest.setReducedInformation(reducedInformation);
        partsListRequest.setImages(images);
        partsListRequest.setSecurePayload(securePayload);
        return endPoint.handleWebserviceRequestIntern(partsListRequest);
    }

    /**
     * Erzeugt aus dem übergebenen {@link RESTfulTransferObjectInterface} einen JSON-String.
     *
     * @param object
     * @return
     */
    private static String getAsJSON(Object object, String... fieldsToOmit) {
        Genson genson = JSONUtils.createGensonWithOmittedFields(true, fieldsToOmit);
        return genson.serialize(object);
    }
}
