/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getproductgroups;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.iPartsWSAbstractEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSAggregateType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSProductGroup;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserInfo;

import java.util.*;

/**
 * Endpoint für den GetProductGroups-Webservice
 */
public class iPartsWSGetProductGroupsEndpoint extends iPartsWSAbstractEndpoint<iPartsWSGetProductGroupsRequest> {

    public static final String DEFAULT_ENDPOINT_URI = "/ident/GetProductGroups";

    public iPartsWSGetProductGroupsEndpoint(String endpointUri) {
        super(endpointUri);
    }

    @Override
    protected iPartsWSGetProductGroupsResponse executeWebservice(EtkProject project, iPartsWSGetProductGroupsRequest requestObject) {
        // Map von Produktgruppe auf Set von Aggregatetypen erstellen
        TreeMap<String, TreeSet<String>> productGroupsMap = new TreeMap<String, TreeSet<String>>();
        List<iPartsProduct> productList = iPartsProduct.getAllProducts(project);
        iPartsWSUserInfo userInfo = requestObject.getUser();
        boolean onlyRetailRelevantProducts = iPartsWebservicePlugin.isOnlyRetailRelevantProducts();
        boolean permissionErrorDetected = false;
        for (iPartsProduct product : productList) {
            if (onlyRetailRelevantProducts && !product.isRetailRelevant()) {
                // Produkt ist nicht retail relevant, es sollen aber nur retail relevante Produkte ausgegeben werden
                continue;
            }

            boolean validForPermissions = product.isValidForPermissions(project, userInfo.getCountryForValidation(), userInfo.getPermissionsAsMapForValidation());
            permissionErrorDetected |= !validForPermissions;
            if (!validForPermissions) {
                // Benutzer hat keine Berechtigung für das Produkt
                continue;
            }

            String productGroup = product.getProductGroup();
            if (!productGroup.isEmpty()) {
                TreeSet<String> aggregateTypes = productGroupsMap.get(productGroup);
                if (aggregateTypes == null) {
                    aggregateTypes = new TreeSet<String>();
                    productGroupsMap.put(productGroup, aggregateTypes);
                }

                String aggregateType = product.getAggregateType();
                if (!aggregateType.isEmpty()) {
                    aggregateTypes.add(aggregateType);
                }
            }
        }
        // Das Ergebnis ist leer, aber es hat Daten gegeben die durch Berechtigungsprüfung ausgefiltert wurden
        if ((!productList.isEmpty() && productGroupsMap.isEmpty()) && permissionErrorDetected) {
            throwPermissionsError();
        }

        // Sprache bestimmen
        String language = userInfo.getLanguage();

        // Liste von Produktgruppen mit Benennungen usw. erzeugen
        List<iPartsWSProductGroup> productGroupsList = new ArrayList<iPartsWSProductGroup>(productGroupsMap.size());
        for (Map.Entry<String, TreeSet<String>> productGroupEntry : productGroupsMap.entrySet()) {
            iPartsWSProductGroup productGroup = new iPartsWSProductGroup();
            productGroupsList.add(productGroup);
            String productGroupEnumToken = productGroupEntry.getKey();
            productGroup.setProductGroupId(productGroupEnumToken);
            productGroup.setProductGroupName(project.getEnumText(iPartsConst.ENUM_KEY_PRODUCT_GROUP,
                                                                 productGroupEnumToken, language, true));
            productGroup.setPowerSystem(false); // TODO Woher bekommen wir diese Info?

            // Aggregatetypen für die Produktgruppe
            TreeSet<String> aggregateTypes = productGroupEntry.getValue();
            if (!aggregateTypes.isEmpty()) {
                List<iPartsWSAggregateType> aggregateTypeList = new ArrayList<iPartsWSAggregateType>(aggregateTypes.size());
                for (String aggregateTypeEnumToken : aggregateTypes) {
                    iPartsWSAggregateType aggregateType = new iPartsWSAggregateType();
                    aggregateTypeList.add(aggregateType);
                    aggregateType.setAggTypeId(aggregateTypeEnumToken);
                    aggregateType.setAggTypeName(project.getEnumText(iPartsConst.ENUM_KEY_AGGREGATE_TYPE,
                                                                     aggregateTypeEnumToken, language, true));
                }
                productGroup.setAggregateTypes(aggregateTypeList);
            }
        }

        // eigentliches Response Object erzeugen und zurückliefern
        iPartsWSGetProductGroupsResponse productGroups = new iPartsWSGetProductGroupsResponse();
        productGroups.setProductGroups(productGroupsList);
        return productGroups;
    }

    @Override
    public void dataChangedByEdit(iPartsDataChangedEventByEdit.DataType dataType) {
        if ((dataType == iPartsDataChangedEventByEdit.DataType.PRODUCT) || (dataType == iPartsDataChangedEventByEdit.DataType.SA)) {
            clearCaches();
        }
    }
}