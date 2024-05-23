/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.visualNav;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductStructures;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.iPartsWSAbstractEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.ident.iPartsWSIdentEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.ident.iPartsWSIdentRequest;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.ident.iPartsWSIdentResponse;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSImageHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSCgSubgroup;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSIdentContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSImage;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserInfo;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.framework.modules.webservice.restful.annotations.Produces;
import de.docware.framework.modules.webservice.restful.annotations.QueryParam;
import de.docware.framework.modules.webservice.restful.annotations.SecurePayloadParam;
import de.docware.framework.modules.webservice.restful.annotations.methods.GET;

import java.util.*;

/**
 * Endpoint für den visualNav-Webservice
 */
public class iPartsWSVisualNavEndpoint extends iPartsWSAbstractEndpoint<iPartsWSVisualNavRequest> {

    public static final String DEFAULT_ENDPOINT_URI = "/parts/visualNav";

    public iPartsWSVisualNavEndpoint(String endpointUri) {
        super(endpointUri);
    }

    @GET
    @Produces(MimeTypes.MIME_TYPE_JSON)
    public RESTfulTransferObjectInterface getVisualNav(@QueryParam("fin") String fin,
                                                       @SecurePayloadParam String securePayload) {
        // Request mit den Parametern erzeugen für die weitere Verarbeitung analog zu POST-Requests
        iPartsWSVisualNavRequest request = new iPartsWSVisualNavRequest();
        request.setFinOrVin(fin);
        request.setSecurePayload(securePayload); // SecurePayload muss bei GET explizit gesetzt werden
        request.checkIfValid(""); // checkIfValid() muss bei GET explizit aufgerufen werden
        return handleWebserviceRequestIntern(request);

    }
    @Override
    protected iPartsWSVisualNavResponse executeWebservice(EtkProject project, iPartsWSVisualNavRequest requestObject) {
        return executeWebservice(project, requestObject, true);
    }

    public iPartsWSVisualNavResponse executeWebservice(EtkProject project, iPartsWSVisualNavRequest requestObject, boolean forceDatacardExists) {
        iPartsWSUserInfo userInfo = requestObject.getUser();
        setCurrentDatabaseLanguageAndFallbackLanguages(project, userInfo);

        iPartsWSVisualNavResponse response = new iPartsWSVisualNavResponse();

        iPartsWSIdentEndpoint identEndpoint = new iPartsWSIdentEndpoint("");
        iPartsWSIdentRequest identRequest = new iPartsWSIdentRequest();
        identRequest.setIdentCode(requestObject.getFinOrVin());
        identRequest.setUser(userInfo);
        iPartsWSIdentResponse identResponse = identEndpoint.executeWebservice(project, identRequest);
        List<iPartsWSIdentContext> identContexts = identResponse.getIdentContexts();

        if ((identContexts == null) || identContexts.isEmpty()) {
            throwResourceNotFoundError("Unable to resolve vehicle/ datacard not found");
        }

        iPartsWSIdentContext identContext = identContexts.get(0);
        if (forceDatacardExists && !identContext.isDatacardExists()) {
            throwResourceNotFoundError(identContext.getErrorText());
        }

        iPartsProductId productId = new iPartsProductId(identContext.getProductId());
        iPartsProduct product = iPartsProduct.getInstance(project, productId);
        if (!product.containsCarPerspectiveTU(project)) {
            throwResourceNotFoundError("Unable to resolve vehicle");
        }

        EtkDataAssembly carPerspectiveDataAssembly = EditModuleHelper.createCarPerspectiveDataAssembly(project, productId);

        String requestLanguage = requestObject.getUser().getLanguage();

        iPartsFilter filter = identContext.setFilterForIdentContext(userInfo.getCountry(), false, project);
        List<EtkDataPartListEntry> partList = carPerspectiveDataAssembly.getPartList(null);
        List<iPartsWSCgSubgroup> subgroupList = new ArrayList<>(partList.size());

        List<iPartsWSImage> imagesForCarNavigationModule = iPartsWSImageHelper.getImagesForModule(carPerspectiveDataAssembly,
                                                                                                  partList, project, requestLanguage,
                                                                                                  true, true);
        response.setNavImages(imagesForCarNavigationModule);

        Map<iPartsProductId, iPartsProductStructures> productStructures = new HashMap<>();

        Set<String> datacardModelNumbers = filter.getCurrentDataCard().getFilterModelNumbers(project);
        List<String> dataBaseFallbackLanguages = project.getDataBaseFallbackLanguages();
        for (EtkDataPartListEntry partListEntry : partList) {
            AssemblyId assemblyId = partListEntry.getDestinationAssemblyId();
            if (assemblyId.isValidId()) {
                iPartsWSCgSubgroup subgroup = new iPartsWSCgSubgroup();
                subgroup.setCalloutId(partListEntry.getFieldValue(EtkDbConst.FIELD_K_POS));
                subgroup.setModuleId(assemblyId.getKVari());
                subgroup.setDescription(iPartsDataAssembly.getAssemblyText(project, assemblyId).getTextByNearestLanguage(project.getDBLanguage(),
                                                                                                                         dataBaseFallbackLanguages));

                EtkDataAssembly subAssembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);

                List<iPartsWSImage> imagesForSubAssembly = iPartsWSImageHelper.getImagesForModule(subAssembly, null, project,
                                                                                                  requestLanguage, false);
                if (!imagesForSubAssembly.isEmpty()) {
                    subgroup.setImages(imagesForSubAssembly);
                }

                if (subAssembly instanceof iPartsDataAssembly) {
                    EditModuleHelper.CarPerspectiveSubModuleData data = EditModuleHelper.getCarPerspectiveSubModuleData(project,
                                                                                                                        (iPartsDataAssembly)subAssembly,
                                                                                                                        productStructures,
                                                                                                                        datacardModelNumbers);
                    subgroup.setModelId(data.modelNumber);
                    subgroup.setProductId(data.productNumber);
                    subgroup.setCg(data.cg);
                    subgroup.setCsg(data.csg);
                }

                subgroupList.add(subgroup);
            }
        }
        response.setCgSubgroups(subgroupList);

        return response;
    }


    @Override
    public void dataChangedByEdit(iPartsDataChangedEventByEdit.DataType dataType) {
        if ((dataType == iPartsDataChangedEventByEdit.DataType.PRODUCT) || (dataType == iPartsDataChangedEventByEdit.DataType.SERIES)
            || (dataType == iPartsDataChangedEventByEdit.DataType.MODEL) || (dataType == iPartsDataChangedEventByEdit.DataType.PART_LIST) ||
            (dataType == iPartsDataChangedEventByEdit.DataType.SA) || dataType == iPartsDataChangedEventByEdit.DataType.DRAWING) {
            clearCaches();
        }
    }
}