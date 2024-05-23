/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_export.endpoints.exportpartslist;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumValue;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.webservice.transferobjects.JWTDefaultClaims;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSaList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsSAId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsWSError;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.FinId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.endpoints.iPartsWSAbstractExportEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.exporter.iPartsExportState;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.iPartsExportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.transferobjects.ExportRequestFINItem;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.transferobjects.ExportRequestModelItem;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.transferobjects.ExportRequestSARangeItem;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.framework.modules.webservice.restful.WSError;
import de.docware.util.StrUtils;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Endpoint für den ExportPartsList-Webservice des Export-Plugins. Die Anfrage selbst wird in {@link #TABLE_DA_EXPORT_REQUEST}
 * gespeichert und die einzelnen Export-Job-Inhalte, die sich aus der Anfrage ergeben, werden sofort in {@link #TABLE_DA_EXPORT_CONTENT}
 * gespeichert. Erst danach werden sie tatsächlich asynchron angestoßen.
 */
public class iPartsWSExportPartsListEndpoint extends iPartsWSAbstractExportEndpoint<iPartsWSExportPartsListRequest> implements iPartsConst {

    public static final String DEFAULT_ENDPOINT_URI = "/iPartsExport/ExportPartsList";

    public iPartsWSExportPartsListEndpoint(String endpointUri) {
        super(endpointUri);
        setResponseCacheSize(0); // Kein Response-Cache, da bei jeder Anfrage Export-Aufträge angelegt werden müssen.
    }

    @Override
    protected RESTfulTransferObjectInterface executeWebservice(EtkProject project, iPartsWSExportPartsListRequest requestObject) throws RESTfulWebApplicationException {
        iPartsDataExportRequest exportRequestDataObject = createExportRequestDataObject(project, requestObject);

        // Durch die Validierung ist sichergestellt, dass nur eine der Anfrage-Listen befüllt ist.
        iPartsExportRequestId exportRequestId = exportRequestDataObject.getAsId();

        Set<iPartsExportContentId> newExportContentIds = null;
        Set<ExportRequestModelItem> reqModelList = requestObject.getReqModelList();
        // Baumuster oder SAs, die keinem Produkt zugeordnet sind.
        Set<String> dataWithoutProduct = new TreeSet<>();
        // SAs die keinem Modul zugeordnet sind
        Set<String> sasWithoutModules = new TreeSet<>();
        // Baumuster oder SAs, die unbekannt sind
        Set<String> dataWithoutMasterData = new TreeSet<>();
        if ((reqModelList != null) && !reqModelList.isEmpty()) {
            // Map von Baumustern, für welche kein Produkt im Request angegeben wurde, zu allen zugeordneten Produkten.
            Map<String, Set<String>> modelsWithAmbiguousProducts = new TreeMap<>();
            // Parameter, ob ein BM zu allen verknüpften Produkten exportiert werden soll
            boolean exportAllProducts = requestObject.isAllProducts();
            boolean pskProductsAllowed = iPartsExportPlugin.isExportPSKCustomerID(exportRequestDataObject.getFieldValue(FIELD_DER_CUSTOMER_ID));
            newExportContentIds = createModelExportContentIds(project, exportRequestId, reqModelList, dataWithoutProduct,
                                                              dataWithoutMasterData, modelsWithAmbiguousProducts, exportAllProducts, pskProductsAllowed);

            if (requestObject.isIncludeAggs()) {
                Set<iPartsExportContentId> aggregateModelExportContents = createAggregateModelExportContentIds(project,
                                                                                                               exportRequestId,
                                                                                                               newExportContentIds,
                                                                                                               dataWithoutProduct,
                                                                                                               modelsWithAmbiguousProducts,
                                                                                                               exportAllProducts,
                                                                                                               pskProductsAllowed);
                newExportContentIds.addAll(aggregateModelExportContents);
            }

            String errorMessage = createModelErrorMessage(modelsWithAmbiguousProducts);
            if (!errorMessage.isEmpty()) {
                throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, errorMessage, null);
            }
        }
        if ((requestObject.getReqFINList() != null) && !requestObject.getReqFINList().isEmpty()) {
            newExportContentIds = createFINExportContentIds(project, exportRequestId,
                                                            requestObject.getReqFINList());
        }

        if ((requestObject.getReqSAList() != null) && !requestObject.getReqSAList().isEmpty()) {
            newExportContentIds = createSAExportContentIds(project, exportRequestId, requestObject.getReqSAList(),
                                                           dataWithoutProduct, sasWithoutModules, dataWithoutMasterData);
        }
        if ((requestObject.getReqSARangeList() != null) && !requestObject.getReqSARangeList().isEmpty()) {
            newExportContentIds = createSARangeExportContentIds(project, exportRequestId, requestObject.getReqSARangeList(),
                                                                dataWithoutProduct, sasWithoutModules, dataWithoutMasterData);
        }

        if ((newExportContentIds == null) || newExportContentIds.isEmpty()) {
            // keines der angefragten SAs ist einem Modul zugeordnet
            // Nur gefüllt bei Export von SA
            if (!sasWithoutModules.isEmpty()) {
                String message = "No modules found for all requested data";
                // Außerdem wurden keine Stammdaten gefunden
                if (!dataWithoutMasterData.isEmpty()) {
                    message = "No modules/data found for all requested data";
                }
                throwBadRequestError(iPartsWSError.REQUEST_NO_PRODUCT_FOUND, message, null);
                return null;
            }

            // Für alle angeforderten Baumuster/SAs wurde kein Produkt gefunden
            if (!dataWithoutProduct.isEmpty()) {
                String message = "No product found for all requested data";
                // Außerdem wurden keine Stammdaten gefunden
                if (!dataWithoutMasterData.isEmpty()) {
                    message = "No product/data found for all requested data";
                }
                throwBadRequestError(iPartsWSError.REQUEST_NO_PRODUCT_FOUND, message, null);
                return null;
            }

            // Für alle angeforderten Baumuster/SAs wurden keine Stammdaten gefunden
            if (!dataWithoutMasterData.isEmpty()) {
                throwBadRequestError(iPartsWSError.REQUEST_NO_PRODUCT_FOUND, "No data found for all requested data", null);
                return null;
            }
            // Kann niemals passieren, da bei fehlerhaften Anfrage-Listen (z.B das Baumuster wurde nicht gefunden)
            // immer vorher ein Fehler zurückgeliefert wird.
            throwProcessingError(WSError.INTERNAL_ERROR, "Unexpected error: No export dataset created although" +
                                                         " input values are valid.", null);
            return null;
        }

        iPartsDataExportContentList exportContentList = new iPartsDataExportContentList();
        for (iPartsExportContentId newExportContentId : newExportContentIds) {
            iPartsDataExportContent exportContent = new iPartsDataExportContent(project, newExportContentId);
            exportContent.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            exportContent.setFieldValue(FIELD_DEC_STATE, iPartsExportState.NEW.getDbValue(), DBActionOrigin.FROM_EDIT);
            exportContentList.add(exportContent, DBActionOrigin.FROM_EDIT);
        }

        project.getDbLayer().startTransaction();
        project.getDbLayer().startBatchStatement();
        try {
            exportRequestDataObject.saveToDB();
            exportContentList.saveToDB(project);
            project.getDbLayer().endBatchStatement();
            project.getDbLayer().commit();
        } catch (RuntimeException e) {
            project.getDbLayer().cancelBatchStatement();
            project.getDbLayer().rollback();
            throw e;
        }

        iPartsWSExportPartsListResponse exportPartsListResponse = new iPartsWSExportPartsListResponse();
        exportPartsListResponse.setJobId(exportRequestDataObject.getJobId());
        exportPartsListResponse.setStatusUrl("not yet implemented");
        if (!sasWithoutModules.isEmpty()) {
            dataWithoutProduct.addAll(sasWithoutModules);
        }
        if (!dataWithoutMasterData.isEmpty()) {
            dataWithoutProduct.addAll(dataWithoutMasterData);
        }
        if (!dataWithoutProduct.isEmpty()) {
            String[] missingDataArray = new String[dataWithoutProduct.size()];
            exportPartsListResponse.setMissingData(dataWithoutProduct.toArray(missingDataArray));
        }
        return exportPartsListResponse;
    }

    /**
     * Erstellt ein neues {@link iPartsDataExportRequest}-Datenobjekt mit einer neuen {@link iPartsExportRequestId}
     * aus dem übergebenen {@link iPartsWSExportPartsListRequest}-Request-DTO. Ist der customer aus dem DTO nicht im Enum
     * zum Feld {@link #FIELD_DER_CUSTOMER_ID} enthalten, dann wird eine {@link RESTfulWebApplicationException} geworfen.
     *
     * @param project
     * @param requestObject
     * @return
     */
    private iPartsDataExportRequest createExportRequestDataObject(EtkProject project, iPartsWSExportPartsListRequest requestObject) {
        iPartsDataExportRequest exportRequest = iPartsDataExportRequest.createExportRequestWithUnusedGUID(project);

        String exportCustomerEnumKey = project.getEtkDbs().getEnum(TableAndFieldName.make(TABLE_DA_EXPORT_REQUEST, FIELD_DER_CUSTOMER_ID));
        EnumValue enumValues = project.getEtkDbs().getEnumValue(exportCustomerEnumKey);
        if ((enumValues == null) || !enumValues.containsKey(requestObject.getCustomerId())) {
            throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, "Unknown customer ID: '" + requestObject.getCustomerId() + "'", null);
        }
        exportRequest.setFieldValue(FIELD_DER_CUSTOMER_ID, requestObject.getCustomerId(), DBActionOrigin.FROM_EDIT);

        if (StrUtils.isValid(requestObject.getExternalJobId())) {
            exportRequest.setFieldValue(FIELD_DER_JOB_ID_EXTERN, requestObject.getExternalJobId(), DBActionOrigin.FROM_EDIT);
        }

        List<String> requestLanguages = requestObject.getLanguages();
        checkIfDBLanguagesExist(requestLanguages, null, project);
        exportRequest.setFieldValueAsSetOfEnum(FIELD_DER_LANGUAGES, requestLanguages, DBActionOrigin.FROM_EDIT);

        exportRequest.setFieldValueAsBoolean(FIELD_DER_INCLUDE_SAS, requestObject.isIncludeSAs(), DBActionOrigin.FROM_EDIT);
        exportRequest.setFieldValueAsBoolean(FIELD_DER_INCLUDE_PICTURES, requestObject.isIncludePictures(), DBActionOrigin.FROM_EDIT);
        exportRequest.setFieldValue(FIELD_DER_PICTURE_FORMAT, requestObject.getPictureFormatAsEnum().getDbValue(), DBActionOrigin.FROM_EDIT);
        exportRequest.setFieldValueAsBoolean(FIELD_DER_INCLUDE_AGGS, requestObject.isIncludeAggs(), DBActionOrigin.FROM_EDIT);
        exportRequest.setFieldValueAsBoolean(FIELD_DER_DIRECT_DOWNLOAD, requestObject.isDirectDownload(), DBActionOrigin.FROM_EDIT);
        exportRequest.setFieldValue(FIELD_DER_OUTPUT_FORMAT, requestObject.getOutputFormatAsEnum().name(), DBActionOrigin.FROM_EDIT);

        // Issuer aus dem JWT Token als den Ersteller setzen.
        JWTDefaultClaims token = getGenson().deserialize(requestObject.getSecurePayload(), JWTDefaultClaims.class);
        exportRequest.setFieldValue(FIELD_DER_CREATION_USER_ID, token.getIss(), DBActionOrigin.FROM_EDIT);

        exportRequest.setFieldValueAsDateTime(FIELD_DER_CREATION_DATE, Calendar.getInstance(), DBActionOrigin.FROM_EDIT);
        exportRequest.setFieldValue(FIELD_DER_STATE, iPartsExportState.NEW.getDbValue(), DBActionOrigin.FROM_EDIT);

        exportRequest.setFieldValueAsBoolean(FIELD_DER_INCLUDE_MAT_PROPERTIES, requestObject.isIncludeAdditionalPartInformation(), DBActionOrigin.FROM_EDIT);
        exportRequest.setFieldValueAsBoolean(FIELD_DER_INCLUDE_EINPAS, requestObject.isIncludeEinPAS(), DBActionOrigin.FROM_EDIT);
        exportRequest.setFieldValueAsBoolean(FIELD_DER_INCLUDE_VISUAL_NAV, requestObject.isIncludeVisualNav(), DBActionOrigin.FROM_EDIT);


        return exportRequest;
    }

    private Set<iPartsExportContentId> createModelExportContentIds(EtkProject project, iPartsExportRequestId requestId,
                                                                   Set<ExportRequestModelItem> requestModelItems,
                                                                   Set<String> modelsWithoutProduct, Set<String> modelsWithoutMasterData,
                                                                   Map<String, Set<String>> modelsWithAmbiguousProducts,
                                                                   boolean exportAllProducts, boolean pskProductsAllowed) {
        Set<ExportRequestModelItem> validRequestModelItem = new HashSet<>();
        for (ExportRequestModelItem requestModelItem : requestModelItems) {
            iPartsModelId modelId = new iPartsModelId(requestModelItem.getModel());
            if (!iPartsModel.isModelNumberValid(modelId.getModelNumber(), true)) {
                throwBadRequestError(WSError.REQUEST_SYNTAX_ERROR, "Wrong format for model: '" + modelId.getModelNumber() + "'", null);
            }
            if (!iPartsModel.isVehicleModel(modelId.getModelNumber()) && !iPartsModel.isAggregateModel(modelId.getModelNumber())) {
                // Baumuster ohne Kennbuchstabe => überprüfe C-BM
                iPartsModelId testModelId = new iPartsModelId(iPartsConst.MODEL_NUMBER_PREFIX_CAR + modelId.getModelNumber());
                if (!(new iPartsDataModel(project, testModelId)).existsInDB()) {
                    // versuchs mit D-BM (Überprüfung erfolgt weiter unten)
                    testModelId = new iPartsModelId(iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE + modelId.getModelNumber());
                }
                modelId = testModelId;
            }

            if (!(new iPartsDataModel(project, modelId)).existsInDB()) {
                modelsWithoutMasterData.add(modelId.getModelNumber());
                continue;
            }
            // falls BM ohne Kennbuchstabe => Setzen im requestModelIterm
            if (!modelId.getModelNumber().equals(requestModelItem.getModel())) {
                requestModelItem.setModel(modelId.getModelNumber());
            }
            validRequestModelItem.add(requestModelItem);
        }
        return createModelExportContentIdsIfValid(project, requestId, validRequestModelItem, modelsWithoutProduct,
                                                  modelsWithAmbiguousProducts, exportAllProducts, pskProductsAllowed);
    }

    private Set<iPartsExportContentId> createAggregateModelExportContentIds(EtkProject project, iPartsExportRequestId exportRequestId,
                                                                            Set<iPartsExportContentId> modelExportContentIds,
                                                                            Set<String> modelsWithoutProduct,
                                                                            Map<String, Set<String>> modelsWithAmbiguousProducts,
                                                                            boolean exportAllProducts, boolean pskProductsAllowed) {
        Set<ExportRequestModelItem> requestAggregateModelItems = new HashSet<>();
        // Pro Fahrzeug-BM kann es mehrere BM <-> Produkt Beziehungen geben. Die Agg-BM sollen aber nur einmal pro
        // Fahrzeug BM und nicht pro BM <-> Produkt Beziehung bestimmt werden
        Set<String> handledVarModels = new HashSet<>();
        for (iPartsExportContentId modelExportContentId : modelExportContentIds) {
            iPartsModelId modelId = modelExportContentId.getDataObjectId(iPartsModelId.class);
            if (!modelId.isAggregateModel() && !handledVarModels.contains(modelId.getModelNumber())) {
                iPartsDataModelsAggsList modelsAggsList = iPartsDataModelsAggsList.loadDataModelsAggsListForModel(project,
                                                                                                                  modelId.getModelNumber());
                for (iPartsDataModelsAggs modelsAggs : modelsAggsList) {
                    ExportRequestModelItem requestAggregateModelItem = new ExportRequestModelItem();
                    requestAggregateModelItem.setModel(modelsAggs.getAsId().getAggregateModelNumber());
                    requestAggregateModelItems.add(requestAggregateModelItem);
                }
                handledVarModels.add(modelId.getModelNumber());
            }
        }
        return createModelExportContentIdsIfValid(project, exportRequestId, requestAggregateModelItems, modelsWithoutProduct,
                                                  modelsWithAmbiguousProducts, exportAllProducts, pskProductsAllowed);
    }

    /**
     * Erstellt aus den übergebenen {@code requestModelItems} jeweils eine {@link de.docware.apps.etk.plugins.customer.daimler.iparts_export.dbobjects.iPartsExportContentId}, in der immer alle
     * Werte gesetzt sind. Also auch das Produkt, da dieses automatisch bestimmt wird, falls nicht angegeben.
     * Wenn kein Produkt gefunden oder das Produkt nicht eindeutig festgestellt werden kann, dann wird {@code modelsWithoutProduct}
     * bzw. {@code modelsWithAmbiguousProducts} entsprechend befüllt.
     *
     * @param project
     * @param exportRequestId
     * @param requestModelItems
     * @param modelsWithoutProduct        Baumuster, die keinem Produkt zugeordnet sind.
     * @param modelsWithAmbiguousProducts Map von Baumustern, für welche kein Produkt im Request angegeben wurde, zu allen zugeordneten Produkten.
     * @param exportAllProducts
     * @param pskProductsAllowed
     * @return
     */
    private Set<iPartsExportContentId> createModelExportContentIdsIfValid(EtkProject project, iPartsExportRequestId exportRequestId,
                                                                          Set<ExportRequestModelItem> requestModelItems,
                                                                          Set<String> modelsWithoutProduct,
                                                                          Map<String, Set<String>> modelsWithAmbiguousProducts,
                                                                          boolean exportAllProducts, boolean pskProductsAllowed) {
        Set<iPartsExportContentId> exportContentIds = new HashSet<>();
        for (ExportRequestModelItem requestModelItem : requestModelItems) {
            iPartsModelId modelId = new iPartsModelId(requestModelItem.getModel());
            List<iPartsProduct> productsForModel = iPartsProductHelper.getProductsForModel(project, modelId,
                                                                                           null, null, false);
            // Wenn der anfragende Benutzer keine PSK Rechte hat, die PSK Produkte aussortieren
            if (!pskProductsAllowed) {
                productsForModel = productsForModel.stream()
                        .filter(product -> !product.isPSK())
                        .collect(Collectors.toList());
            }

            if (productsForModel.isEmpty()) {
                modelsWithoutProduct.add(modelId.getModelNumber());
            } else {
                iPartsProductId productId = getProductIdForModelIfValid(modelsWithAmbiguousProducts, modelId, requestModelItem.getProduct(),
                                                                        productsForModel);
                if (productId != null) {
                    // Wurde das gewünschte Produkt gefunden oder existiert nur genau ein Produkt zum BM, dann diese
                    // Beziehung exportieren
                    addContentId(project, productId, exportRequestId.getJobId(), modelId, exportContentIds);
                } else if (exportAllProducts && !productsForModel.isEmpty()) {
                    // Ist der Kenner "allProducts" gesetzt und esitieren mehr als ein produkt zum BM, dann pro
                    // BM <-> Produkt Beziehung einen Export Auftrag anlegen
                    productsForModel.forEach(product -> addContentId(project, product.getAsId(), exportRequestId.getJobId(), modelId, exportContentIds));
                    modelsWithAmbiguousProducts.remove(modelId.getModelNumber());
                }
            }
        }

        return exportContentIds;
    }

    private void addContentId(EtkProject project, iPartsProductId productId, String jobId,
                              iPartsModelId modelId, Set<iPartsExportContentId> exportContentIds) {
        checkProductExists(project, productId);
        iPartsExportContentId exportContentId = new iPartsExportContentId(jobId, modelId,
                                                                          productId);
        exportContentIds.add(exportContentId);
    }

    private iPartsProductId getProductIdForModelIfValid(Map<String, Set<String>> modelsWithAmbiguousProducts, iPartsModelId modelId,
                                                        String requestedProductNumber, List<iPartsProduct> productsForModel) {
        if (StrUtils.isValid(requestedProductNumber)) {
            for (iPartsProduct productForModel : productsForModel) {
                if (productForModel.getAsId().getProductNumber().equals(requestedProductNumber)) {
                    return new iPartsProductId(requestedProductNumber);
                }
            }
        } else {
            if (productsForModel.size() == 1) {
                String productNumber = productsForModel.get(0).getAsId().getProductNumber();
                return new iPartsProductId(productNumber);
            }
        }

        // Das Produkt ist nicht in der Liste der Produkte zum Baumuster enthalten oder nicht angegeben und es gibt mehr als eines
        // Wir können nicht wissen, welches Produkt exportiert werden soll. Also nur Info für die Fehlermeldung festhalten.
        Set<String> productStrings = modelsWithAmbiguousProducts.computeIfAbsent(modelId.getModelNumber(), key -> new TreeSet<>());
        for (iPartsProduct productForModel : productsForModel) {
            productStrings.add(productForModel.getAsId().getProductNumber());
        }
        return null;
    }

    private String createModelErrorMessage(Map<String, Set<String>> modelsWithAmbiguousProducts) {
        String errorMessage = "";
        if (!modelsWithAmbiguousProducts.isEmpty()) {
            errorMessage += "No matching or more than one product found for the following models: ";
            errorMessage += modelsWithAmbiguousProducts.entrySet().stream()
                    .map(entry -> {
                        String modelNumber = entry.getKey();
                        Set<String> productNumbers = entry.getValue();
                        return modelNumber + " (" + StrUtils.stringListToString(productNumbers, ", ") + ")";
                    })
                    .collect(Collectors.joining(", "));
        }
        return errorMessage;
    }

    private Set<iPartsExportContentId> createFINExportContentIds(EtkProject project, iPartsExportRequestId requestId,
                                                                 List<ExportRequestFINItem> requestFINItems) {
        Set<iPartsExportContentId> newExportContentIds = new HashSet<>();
        for (ExportRequestFINItem requestFINItem : requestFINItems) {
            FinId finId = new FinId(requestFINItem.getFin());
            if (!finId.isValidId()) { // TODO Auch VINs?
                throwBadRequestError(WSError.REQUEST_SYNTAX_ERROR, "Wrong format for FIN: '" + requestFINItem.getFin() + "'", null);
            }
            iPartsProductId productId = null;
            if (StrUtils.isValid(requestFINItem.getProduct())) {
                productId = new iPartsProductId(requestFINItem.getProduct());
                checkProductExists(project, productId);
            }
            iPartsExportContentId exportContentId = new iPartsExportContentId(requestId.getJobId(), finId, productId);
            newExportContentIds.add(exportContentId);
        }
        return newExportContentIds;
    }

    private Set<iPartsExportContentId> createSAExportContentIds(EtkProject project, iPartsExportRequestId requestId,
                                                                List<String> requestSAs, Set<String> sasWithoutProduct,
                                                                Set<String> sasWithoutModules, Set<String> sasWithoutMasterData) {
        Set<iPartsExportContentId> newExportContentIds = new HashSet<>();
        iPartsNumberHelper numberHelper = new iPartsNumberHelper();
        for (String requestSA : requestSAs) {
            String dbFormattedSA = "";
            try {
                dbFormattedSA = numberHelper.unformatSaForDB(requestSA);
            } catch (RuntimeException e) {
                throwBadRequestError(WSError.REQUEST_SYNTAX_ERROR, "Wrong format for SA: '" + requestSA + "'", null);
            }
            iPartsSaId saId = new iPartsSaId(dbFormattedSA);
            if (!(new iPartsDataSa(project, saId)).existsInDB()) {
                sasWithoutMasterData.add(dbFormattedSA);
                continue;
            }
            fillSetsForSa(project, saId, requestId, newExportContentIds, sasWithoutProduct, sasWithoutModules);
        }
        return newExportContentIds;
    }

    private Set<iPartsExportContentId> createSARangeExportContentIds(EtkProject project, iPartsExportRequestId requestId,
                                                                     List<ExportRequestSARangeItem> requestSARanges, Set<String> sasWithoutProduct,
                                                                     Set<String> sasWithoutModules, Set<String> sasWithoutMasterdata) {
        Set<iPartsExportContentId> newExportContentIds = new HashSet<>();
        for (ExportRequestSARangeItem requestSARange : requestSARanges) {
            iPartsDataSaList dataSAsInRange;
            if (StrUtils.isValid(requestSARange.getTo())) {
                dataSAsInRange = getDataSAListForClosedRange(project, requestSARange);
            } else {
                dataSAsInRange = getDataSAListForWholeRange(project, requestSARange);
            }
            if (dataSAsInRange.isEmpty()) {
                sasWithoutMasterdata.add(requestSARange.toString());
            } else {
                for (iPartsDataSa dataSA : dataSAsInRange) {
                    fillSetsForSa(project, dataSA.getAsId(), requestId, newExportContentIds, sasWithoutProduct, sasWithoutModules);
                }
            }
        }
        return newExportContentIds;
    }

    private void fillSetsForSa(EtkProject project, iPartsSaId saId, iPartsExportRequestId requestId,
                               Set<iPartsExportContentId> newExportContentIds, Set<String> sasWithoutProduct, Set<String> sasWithoutModules) {
        String dbFormattedSA = saId.getSaNumber();
        iPartsSAId SAId = new iPartsSAId(dbFormattedSA);

        iPartsDataSAModulesList modulesSAList = iPartsDataSAModulesList.loadDataForSA(project, SAId);
        // Kein Export falls es für die SA kein Modul gibt
        if (modulesSAList.isEmpty()) {
            sasWithoutModules.add(dbFormattedSA);
            return;
        }

        // Kein Export falls es für die SA kein Produkt gibt
        Map<iPartsProductId, Set<String>> productIdsToKGsMap = iPartsSA.getInstance(project, SAId).getProductIdsToKGsMap(project);
        if (productIdsToKGsMap.isEmpty()) {
            sasWithoutProduct.add(dbFormattedSA);
        } else {
            iPartsExportContentId exportContentId = new iPartsExportContentId(requestId.getJobId(), saId, null);
            newExportContentIds.add(exportContentId);
        }
    }

    /**
     * Darf NUR für {@link ExportRequestSARangeItem}s mit befülltem "to" Wert benutzt werden. Die Funktion liefert
     * alle {@link iPartsDataSa}s, deren SA-Nummern in der übergebenen Range liegen.
     *
     * @param project
     * @param requestSARange
     * @return
     */
    private iPartsDataSaList getDataSAListForClosedRange(EtkProject project, ExportRequestSARangeItem requestSARange) {
        iPartsNumberHelper numberHelper = new iPartsNumberHelper();
        String dbFormattedFromSA = "";
        String dbFormattedToSA = "";
        try {
            dbFormattedFromSA = numberHelper.unformatSaForDB(requestSARange.getFrom());
            dbFormattedToSA = numberHelper.unformatSaForDB(requestSARange.getTo());
        } catch (RuntimeException e) {
            throwBadRequestError(WSError.REQUEST_SYNTAX_ERROR, "Wrong format for SA range: '" + requestSARange + "'", null);
        }

        String saSearchString = "";
        for (int i = 0; i < dbFormattedFromSA.length(); i++) {
            if (dbFormattedFromSA.charAt(i) == dbFormattedToSA.charAt(i)) {
                saSearchString += dbFormattedFromSA.charAt(i);
            } else {
                break;
            }
        }
        if ((saSearchString.length() <= 1) || (dbFormattedFromSA.compareTo(dbFormattedToSA) > 0)) { // Z am Anfang ist immer gleich
            throwBadRequestError(WSError.REQUEST_SYNTAX_ERROR, "SA range does not describe a valid range: '" + requestSARange + "'", null);
        }

        iPartsDataSaList saListInRange = new iPartsDataSaList();
        iPartsDataSaList allSAsWithCommonPrefix = iPartsDataSaList.loadAllSAsStartingWith(project, saSearchString);
        for (iPartsDataSa dataSa : allSAsWithCommonPrefix) {
            String saNumber = dataSa.getAsId().getSaNumber();
            if ((dbFormattedFromSA.compareTo(saNumber) <= 0) && (saNumber.compareTo(dbFormattedToSA) <= 0)) {
                saListInRange.add(dataSa, DBActionOrigin.FROM_EDIT);
            }
        }
        return saListInRange;
    }

    /**
     * Darf NUR für {@link ExportRequestSARangeItem}s mit leerem "to" Wert benutzt werden. Bei diesen stehen im
     * "from" Wert alle SAs, die mit eben diesem Wert anfangen aber OHNE die letzten drei Stellen.
     * Dabei werden die gängigen SA-Eingabe-Formate unterstützt.
     * Z.B. "Z 504." ergibt als Suchstring für die DB  "Z504%", " 10" ergibt "Z 10%".
     * Die Funktion liefert dann alle zum Suchstring gefundenen {@link iPartsDataSa}s.
     *
     * @param requestSARange
     * @return
     */
    private iPartsDataSaList getDataSAListForWholeRange(EtkProject project, ExportRequestSARangeItem requestSARange) {
        iPartsNumberHelper numberHelper = new iPartsNumberHelper();
        String dbFormattedFromSA = "";
        try {
            dbFormattedFromSA = numberHelper.unformatSaForDB(StrUtils.trimRight(requestSARange.getFrom()).concat("000"));
        } catch (RuntimeException e) {
            throwBadRequestError(WSError.REQUEST_SYNTAX_ERROR, "Wrong format for SA range: '" + requestSARange + "'", null);
        }
        String saSearchString = dbFormattedFromSA.substring(0, dbFormattedFromSA.length() - 3);
        return iPartsDataSaList.loadAllSAsStartingWith(project, saSearchString);
    }

    private void checkProductExists(EtkProject project, iPartsProductId productId) {
        if (!(new iPartsDataProduct(project, productId)).existsInDB()) {
            throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, "Unknown product: '" + productId.getProductNumber() + "'", null);
        }
    }
}