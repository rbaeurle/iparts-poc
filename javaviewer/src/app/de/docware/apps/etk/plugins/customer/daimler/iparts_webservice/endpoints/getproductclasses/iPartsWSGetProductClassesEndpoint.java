/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getproductclasses;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.iPartsWSAbstractEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSAggregateType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSProductClass;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserInfo;

import java.util.*;

/**
 * Endpoint für den GetProductClasses-Webservice
 */
public class iPartsWSGetProductClassesEndpoint extends iPartsWSAbstractEndpoint<iPartsWSGetProductClassesRequest> {

    public static final String DEFAULT_ENDPOINT_URI = "/ident/GetProductClasses";

    public iPartsWSGetProductClassesEndpoint(String endpointUri) {
        super(endpointUri);
    }

    @Override
    protected iPartsWSGetProductClassesResponse executeWebservice(EtkProject project, iPartsWSGetProductClassesRequest requestObject) {
        // Map von Set von AS-Produktklassen auf Set von Aggregatetypen erstellen
        TreeMap<String, TreeSet<String>> asProductClassesMap = new TreeMap<>();
        List<iPartsProduct> productList = iPartsProduct.getAllProducts(project);
        iPartsWSUserInfo userInfo = requestObject.getUser();
        boolean onlyVisibleProducts = iPartsWebservicePlugin.isOnlyRetailRelevantProducts();
        boolean permissionErrorDetected = false;

        // Wenn der Schalter für die Prüfung über die Permissions eingeschaltet ist, aber in den UserInfos des Aufrufes
        // keine Berechtigungen gesetzt sind, gleich hier abbrechen.
        Map<String, Set<String>> userPermissionsMap = null;
        if (iPartsWebservicePlugin.isCheckTokenPermissions()) {
            if (userInfo.getPermissions() == null) {
                throwPermissionsError();
            } else {
                userPermissionsMap = userInfo.getPermissionsAsMapForValidation();
            }
        }

        for (iPartsProduct product : productList) {
            if (onlyVisibleProducts && !product.isRetailRelevant()) {
                continue;
            }
            // Token Gültigkeiten prüfen
            boolean validForPermissions = product.isValidForPermissions(project, userInfo.getCountryForValidation(), userPermissionsMap);
            permissionErrorDetected |= !validForPermissions;
            if (!validForPermissions) {
                continue;
            }

            if (product.isSpecialCatalog()) {
                // Spezialkataloge (Lacke und Betriebstoffe) nicht berücksichtigen
                continue;
            }

            Set<String> asProductClasses = product.getAsProductClasses();
            for (String asProductClass : asProductClasses) {
                // Auch wenn ein Produkt mehrere AS-Produktklassen angehört, nur die Aggregate durchlassen,
                // die tatsächlich auch über die Permissions abgedeckt sind, aber auch nur,
                // wenn die Prüfung über die Berechtigungen überhaupt eingeschaltet ist.
                if ((userPermissionsMap == null) || product.isASProductClassValidForAssortmentPermissions(project, asProductClass, userPermissionsMap)) {
                    TreeSet<String> aggregateTypes = asProductClassesMap.get(asProductClass);
                    if (aggregateTypes == null) {
                        aggregateTypes = new TreeSet<>();
                        asProductClassesMap.put(asProductClass, aggregateTypes);
                    }

                    String aggregateType = product.getAggregateType();
                    if (!aggregateType.isEmpty()) {
                        aggregateTypes.add(aggregateType);
                    }
                }
            }
        }

        // Das Ergebnis ist leer, aber es hat Daten gegeben die durch Berechtigungsprüfung ausgefiltert wurden
        if ((!productList.isEmpty() && asProductClassesMap.isEmpty()) && permissionErrorDetected) {
            throwPermissionsError();
        }

        // Sprache bestimmen
        String language = userInfo.getLanguage();

        // Liste von AS-Produktklassen mit Benennungen usw. erzeugen
        List<iPartsWSProductClass> productClassesList = new ArrayList<>(asProductClassesMap.size());
        for (Map.Entry<String, TreeSet<String>> productGroupEntry : asProductClassesMap.entrySet()) {
            iPartsWSProductClass productClass = new iPartsWSProductClass();
            productClassesList.add(productClass);
            String productClassEnumToken = productGroupEntry.getKey();

            productClass.setProductClassId(productClassEnumToken);
            productClass.setProductClassName(project.getEnumText(iPartsConst.ENUM_KEY_ASPRODUCT_CLASS, productClassEnumToken, language, true));

            // Powersystems über check auf AS Produktklassen setzen
            productClass.setPowerSystem(productClassEnumToken.equals(iPartsConst.AS_PRODUCT_CLASS_POWERSYSTEMS));

            // Aggregatetypen für die Produktgruppe
            TreeSet<String> aggregateTypes = productGroupEntry.getValue();
            if (!aggregateTypes.isEmpty()) {
                List<iPartsWSAggregateType> aggregateTypeList = new ArrayList<>(aggregateTypes.size());
                for (String aggregateTypeEnumToken : aggregateTypes) {
                    iPartsWSAggregateType aggregateType = new iPartsWSAggregateType();
                    aggregateTypeList.add(aggregateType);
                    aggregateType.setAggTypeId(aggregateTypeEnumToken);
                    aggregateType.setAggTypeName(project.getEnumText(iPartsConst.ENUM_KEY_AGGREGATE_TYPE,
                                                                     aggregateTypeEnumToken, language, true));
                }
                productClass.setAggregateTypes(aggregateTypeList);
            }
        }

        // eigentliches Response Object erzeugen und zurückliefern
        iPartsWSGetProductClassesResponse productClasses = new iPartsWSGetProductClassesResponse();
        productClasses.setProductClasses(productClassesList);
        return productClasses;
    }

    @Override
    public void dataChangedByEdit(iPartsDataChangedEventByEdit.DataType dataType) {
        if ((dataType == iPartsDataChangedEventByEdit.DataType.PRODUCT) || (dataType == iPartsDataChangedEventByEdit.DataType.SA)) {
            clearCaches();
        }
    }
}