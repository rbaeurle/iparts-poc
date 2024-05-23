/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getmaterialpartinfo;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsPartlistTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getpartinfo.iPartsWSGetPartInfoResponse;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.iPartsWSAbstractGetPartInfoEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSFilteredPartListsCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSIdentContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserInfo;

import java.util.Set;

/**
 * Endpoint für den GetMaterialPartInfo-Webservice
 * Beispiel siehe Confluence: https://confluence.docware.de/confluence/x/2YF8AQ
 */
public class iPartsWSGetMaterialPartInfoEndpoint extends iPartsWSAbstractGetPartInfoEndpoint<iPartsWSGetMaterialPartInfoRequest> {

    public static final String DEFAULT_ENDPOINT_URI = "/material/GetMaterialPartInfo";

    public iPartsWSGetMaterialPartInfoEndpoint(String endpointUri) {
        super(endpointUri);
    }

    @Override
    protected iPartsWSGetPartInfoResponse executeWebservice(EtkProject project, iPartsWSGetMaterialPartInfoRequest requestObject) {
        iPartsWSUserInfo userInfo = requestObject.getUser();
        setCurrentDatabaseLanguageAndFallbackLanguages(project, userInfo);

        String inputProductId = requestObject.getProductId(); // Produkt
        String inputModuleId = requestObject.getPartContext().getModuleId(); // entspricht k_vari = Baugruppe
        String inputLfdNr = requestObject.getPartContext().getSequenceId(); // entspricht k_lfdnr

        // Relation Produkt - Modul prüfen
        iPartsAssemblyId assemblyId = new iPartsAssemblyId(inputModuleId, "");

        // Damit alle Webservices gleich funktionieren, auch hier die Gültigkeitsprüfung für Token und RetailRelevant
        iPartsProduct product = iPartsProduct.getInstance(project, new iPartsProductId(inputProductId));
        boolean isValid = checkRetailRelevantAndPermissionWithException(project, product, userInfo);
        if (!isValid) {
            return new iPartsWSGetPartInfoResponse();
        }

        checkModuleValid(project, inputProductId, assemblyId, inputModuleId, userInfo.getSpecialPermissions());
        return createResponse(assemblyId, inputLfdNr, inputProductId, requestObject, null, userInfo.getCountry(), project);
    }

    @Override
    protected iPartsWSIdentContext getIdentContext(iPartsWSGetMaterialPartInfoRequest requestObject) {
        return null;
    }

    @Override
    protected Set<String> getFilteredPartListSequenceNumbers(EtkDataAssembly assembly, iPartsProduct product, iPartsWSGetMaterialPartInfoRequest requestObject) {
        // Filtert dea facto aktuell gar nichts, da assortmentClassId kein Inputparameter vom GetMaterialPartInfoRequest ist
        return iPartsWSFilteredPartListsCache.getFilteredPartListSequenceNumbers(assembly, iPartsPartlistTypes.PART_LIST_TYPE_FOR_GET_PARTS,
                                                                                 product, null, requestObject.getIdentContext(),
                                                                                 this);
    }
}