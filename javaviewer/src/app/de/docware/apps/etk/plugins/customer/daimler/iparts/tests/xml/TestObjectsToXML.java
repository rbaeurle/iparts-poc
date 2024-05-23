/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.tests.xml;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferSMCAttributes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request.AbstractXMLRequestOperation;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.iPartsXMLMediaMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response.*;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlFile;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Testklasse für das Schreiben von XML Dateien aus Objekten
 */
public class TestObjectsToXML extends AbstractTestXMLBase {

    public void testWriteIPartsCreateMediaOrderRequestMessageToFile() {

        DwXmlFile xmlFile = XMLImportExportHelper.writeXMLFileFromMessageObject(testMessageCreateMediaOrderRequest,
                                                                                testChannelType, "iPartsOut", null, isForTest);

        iPartsXMLMediaMessage newMessage = null;
        try {
            newMessage = (iPartsXMLMediaMessage)XMLImportExportHelper.buildMessageFromXMLFile(xmlFile, testChannelType, false, false);
        } catch (Exception e) {
            Logger.getLogger().throwRuntimeException(e);
        }
        if (newMessage == null) {
            fail();
        }

        assertEquals(testMessageCreateMediaOrderRequest.isResponse(), newMessage.isResponse());
        assertEquals(testMessageCreateMediaOrderRequest.isRequest(), newMessage.isRequest());
        assertEquals(testMessageCreateMediaOrderRequest.getTypeObject().getClass(), newMessage.getTypeObject().getClass());

        iPartsXMLRequest request = (iPartsXMLRequest)testMessageCreateMediaOrderRequest.getTypeObject();
        iPartsXMLRequest newRequest = (iPartsXMLRequest)newMessage.getTypeObject();

        assertEquals(request.getFromParticipant(), newRequest.getFromParticipant());
        assertEquals(request.getiPartsRequestID(), newRequest.getiPartsRequestID());
        assertEquals(request.getToParticipant(), newRequest.getToParticipant());

        iPartsXMLRequestor requestor = request.getRequestor();
        iPartsXMLRequestor newRequestor = newRequest.getRequestor();

        assertEquals(requestor.getUserId(), newRequestor.getUserId());
        assertEquals(requestor.getGroupId(), newRequestor.getGroupId());

        AbstractXMLRequestOperation operation = request.getOperation();
        AbstractXMLRequestOperation newOperation = newRequest.getOperation();

        assertEquals(operation.getClass(), newOperation.getClass());
        assertEquals(operation.getOperationType(), newOperation.getOperationType());

        checkCMOElement(operation, newOperation);
    }

    private void checkCMOElement(AbstractXMLRequestOperation operation, AbstractXMLRequestOperation newOperation) {
        iPartsXMLCreateMediaOrder cmo = (iPartsXMLCreateMediaOrder)operation;
        iPartsXMLCreateMediaOrder newCmo = (iPartsXMLCreateMediaOrder)newOperation;

        assertEquals(cmo.getEinPASText(), newCmo.getEinPASText());
        assertEquals(cmo.getName(), newCmo.getName());
        assertEquals(cmo.getEinPasId(), newCmo.getEinPasId());
        assertEquals(cmo.getDescription(), newCmo.getDescription());
        assertEquals(cmo.getProducts().size(), newCmo.getProducts().size());
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(cmo.getDateDue());
        Calendar newCal = GregorianCalendar.getInstance();
        newCal.setTime(newCmo.getDateDue());
        assertEquals(cal.get(Calendar.YEAR), newCal.get(Calendar.YEAR));
        assertEquals(cal.get(Calendar.MONTH), newCal.get(Calendar.MONTH));
        assertEquals(cal.get(Calendar.DAY_OF_MONTH), newCal.get(Calendar.DAY_OF_MONTH));
        assertEquals(cmo.getRemark(), newCmo.getRemark());
        assertEquals(cmo.getRealization(), newCmo.getRealization());
        assertEquals(cmo.getContractor().getUserId(), newCmo.getContractor().getUserId());
        assertEquals(cmo.getContractor().getGroupId(), newCmo.getContractor().getGroupId());
        assertEquals(cmo.getEinPasId().toString(), newCmo.getEinPasId().toString());
        assertEquals(cmo.getKgTuId().toString(), newCmo.getKgTuId().toString());
        assertEquals(cmo.getEinPASText().getText(Language.EN.getCode()), newCmo.getEinPASText().getText(Language.EN.getCode()));
        assertEquals(cmo.getEinPASText().getText(Language.IT.getCode()), newCmo.getEinPASText().getText(Language.IT.getCode()));
        assertEquals(cmo.getKgTuText().getText(Language.EN.getCode()), newCmo.getKgTuText().getText(Language.EN.getCode()));
        assertEquals(cmo.getKgTuText().getText(Language.IT.getCode()), newCmo.getKgTuText().getText(Language.IT.getCode()));
        assertEquals(1, cmo.getPartPositions().size());
        assertEquals(1, newCmo.getPartPositions().size());
        checkPartPositionElement(cmo, newCmo);
    }

    private void checkPartPositionElement(iPartsXMLCreateMediaOrder cmo, iPartsXMLCreateMediaOrder newCmo) {
        iPartsXMLPartPosition partPosition = cmo.getPartPositions().iterator().next();
        iPartsXMLPartPosition newPartPosition = newCmo.getPartPositions().iterator().next();
        assertEquals(partPosition.getHotspot(), newPartPosition.getHotspot());
        assertEquals(partPosition.getQuantity(), newPartPosition.getQuantity());
        assertEquals(partPosition.getStructureLevel(), newPartPosition.getStructureLevel());
        assertEquals(partPosition.getExternalId(), newPartPosition.getExternalId());
        assertEquals(partPosition.getFaultLocation(), newPartPosition.getFaultLocation());
        assertEquals(partPosition.getPartName().getText(Language.DE.getCode()), newPartPosition.getPartName().getText(Language.DE.getCode()));
        assertEquals(partPosition.getPartName().getText(Language.EN.getCode()), newPartPosition.getPartName().getText(Language.EN.getCode()));
        assertEquals(partPosition.getPartName().getText(Language.ES.getCode()), newPartPosition.getPartName().getText(Language.ES.getCode()));
        assertEquals(partPosition.getPartName().getText(Language.PT.getCode()), newPartPosition.getPartName().getText(Language.PT.getCode()));
        assertNotNull(partPosition.getPartNumber());
        assertNotNull(newPartPosition.getPartNumber());
        assertEquals(partPosition.getPartNumber().getPartNumberText(), newPartPosition.getPartNumber().getPartNumberText());
    }

    public void testWriteIPartsCreateMediaOrderResponseMessageToFile() {

        DwXmlFile xmlFile = XMLImportExportHelper.writeXMLFileFromMessageObject(testMessageCreateMediaOrderResponse,
                                                                                testChannelType, "iPartsOut", null, isForTest);
        // Message Element
        iPartsXMLMediaMessage newMessage = null;
        try {
            newMessage = (iPartsXMLMediaMessage)XMLImportExportHelper.buildMessageFromXMLFile(xmlFile, testChannelType, false, false);
        } catch (Exception e) {
            Logger.getLogger().throwRuntimeException(e);
        }
        if (newMessage == null) {
            fail();
        }

        assertEquals(testMessageCreateMediaOrderResponse.isResponse(), newMessage.isResponse());
        assertEquals(testMessageCreateMediaOrderResponse.isRequest(), newMessage.isRequest());
        assertEquals(testMessageCreateMediaOrderResponse.getTypeObject().getClass(), newMessage.getTypeObject().getClass());
        // Response
        iPartsXMLResponse response = (iPartsXMLResponse)testMessageCreateMediaOrderResponse.getTypeObject();
        iPartsXMLResponse newResponse = (iPartsXMLResponse)newMessage.getTypeObject();

        assertEquals(response.getRequestOperation(), newResponse.getRequestOperation());
        assertEquals(response.getFromParticipant(), newResponse.getFromParticipant());
        assertEquals(response.getiPartsRequestID(), newResponse.getiPartsRequestID());
        assertEquals(response.getToParticipant(), newResponse.getToParticipant());
        // Success Element
        iPartsXMLSuccess success = response.getSuccess();
        iPartsXMLSuccess newSuccess = newResponse.getSuccess();

        assertEquals(success.getErrorCode(), newSuccess.getErrorCode());
        assertEquals(success.isErrorFree(), newSuccess.isErrorFree());
        assertEquals(success.getErrors(), newSuccess.getErrors());
        // ReCreateMediaOrder Element
        assertEquals(response.getResult().getResultType(), newResponse.getResult().getResultType());
        iPartsXMLResCreateMediaOrder rcmo = (iPartsXMLResCreateMediaOrder)response.getResult();
        iPartsXMLResCreateMediaOrder newRcmo = (iPartsXMLResCreateMediaOrder)newResponse.getResult();

        assertEquals(rcmo.getMContainer().getClass(), newRcmo.getMContainer().getClass());
        assertEquals(rcmo.getMOrder().getClass(), newRcmo.getMOrder().getClass());
        // MediaContainer Element
        iPartsXMLMediaContainer mc = rcmo.getMContainer();
        iPartsXMLMediaContainer newMc = newRcmo.getMContainer();

        assertEquals(mc.getMcItemId(), newMc.getMcItemId());
        assertEquals(mc.getMcItemRevId(), newMc.getMcItemRevId());

        // MediaOrder Element
        iPartsXMLMediaOrder mediaOrder = rcmo.getMOrder();
        iPartsXMLMediaOrder newMediaOrder = newRcmo.getMOrder();

        assertEquals(mediaOrder.getDateOrderedAsASPLMValue(), newMediaOrder.getDateOrderedAsASPLMValue());

        // History Element
        assertNotNull(testMessageCreateMediaOrderResponse.getHistory());
        assertNotNull(newMessage.getHistory());
        assertEquals(testMessageCreateMediaOrderResponse.getHistory().getClass(), newMessage.getHistory().getClass());

        iPartsXMLHistory history = testMessageCreateMediaOrderResponse.getHistory();
        iPartsXMLHistory newHistory = newMessage.getHistory();

        assertEquals(history.getRequest().getClass(), newHistory.getRequest().getClass());
        assertEquals(history.getResponse(), newHistory.getResponse());
        // Request Element
        iPartsXMLRequest request = history.getRequest();
        iPartsXMLRequest newRequest = newHistory.getRequest();

        assertEquals(request.getFromParticipant(), newRequest.getFromParticipant());
        assertEquals(request.getiPartsRequestID(), newRequest.getiPartsRequestID());
        assertEquals(request.getToParticipant(), newRequest.getToParticipant());
        // Requestor Element
        iPartsXMLRequestor requestor = request.getRequestor();
        iPartsXMLRequestor newRequestor = newRequest.getRequestor();

        assertEquals(requestor.getUserId(), newRequestor.getUserId());
        assertEquals(requestor.getGroupId(), newRequestor.getGroupId());
        // Operation Element
        AbstractXMLRequestOperation operation = request.getOperation();
        AbstractXMLRequestOperation newOperation = newRequest.getOperation();

        assertEquals(operation.getClass(), newOperation.getClass());
        assertEquals(operation.getOperationType(), newOperation.getOperationType());
        checkCMOElement(operation, newOperation);
    }

    public void testWriteIPartsSearchMediaContainersRequestMessageToFile() {

        DwXmlFile xmlFile = XMLImportExportHelper.writeXMLFileFromMessageObject(testMessageSearchMediaContainersRequest,
                                                                                testChannelType, "iPartsOut", null, isForTest);

        iPartsXMLMediaMessage newMessage = null;
        try {
            newMessage = (iPartsXMLMediaMessage)XMLImportExportHelper.buildMessageFromXMLFile(xmlFile, testChannelType, false, false);
        } catch (Exception e) {
            Logger.getLogger().throwRuntimeException(e);
        }
        if (newMessage == null) {
            fail();
        }

        assertEquals(testMessageSearchMediaContainersRequest.isResponse(), newMessage.isResponse());
        assertEquals(testMessageSearchMediaContainersRequest.isRequest(), newMessage.isRequest());
        assertEquals(testMessageSearchMediaContainersRequest.getTypeObject().getClass(), newMessage.getTypeObject().getClass());

        iPartsXMLRequest request = (iPartsXMLRequest)testMessageSearchMediaContainersRequest.getTypeObject();
        iPartsXMLRequest newRequest = (iPartsXMLRequest)newMessage.getTypeObject();

        assertEquals(request.getFromParticipant(), newRequest.getFromParticipant());
        assertEquals(request.getiPartsRequestID(), newRequest.getiPartsRequestID());
        assertEquals(request.getToParticipant(), newRequest.getToParticipant());

        iPartsXMLRequestor requestor = request.getRequestor();
        iPartsXMLRequestor newRequestor = newRequest.getRequestor();

        assertEquals(requestor.getUserId(), newRequestor.getUserId());
        assertEquals(requestor.getGroupId(), newRequestor.getGroupId());

        AbstractXMLRequestOperation operation = request.getOperation();
        AbstractXMLRequestOperation newOperation = newRequest.getOperation();

        assertEquals(operation.getClass(), newOperation.getClass());
        assertEquals(operation.getOperationType(), newOperation.getOperationType());

        iPartsXMLSearchMediaContainers smc = (iPartsXMLSearchMediaContainers)operation;
        iPartsXMLSearchMediaContainers newSmc = (iPartsXMLSearchMediaContainers)newOperation;

        assertEquals(smc.getMaxResultFromIParts(), newSmc.getMaxResultFromIParts());
        assertEquals(smc.getOperationType(), newSmc.getOperationType());
        assertEquals(smc.hasMaxResult(), newSmc.hasMaxResult());
        assertEquals(smc.hasResultAttributes(), newSmc.hasResultAttributes());
        assertEquals(smc.hasSearchCriteria(), newSmc.hasSearchCriteria());
        assertEquals(smc.getSearchCriteria().size(), newSmc.getSearchCriteria().size());
        assertEquals(smc.getSearchCriterionByAttributeName(iPartsTransferSMCAttributes.SMC_NAME).getAttributeName(),
                     newSmc.getSearchCriterionByAttributeName(iPartsTransferSMCAttributes.SMC_NAME).getAttributeName());
        assertEquals(smc.getSearchCriterionByAttributeName(iPartsTransferSMCAttributes.SMC_NAME).getAttributeValue(),
                     newSmc.getSearchCriterionByAttributeName(iPartsTransferSMCAttributes.SMC_NAME).getAttributeValue());
        assertEquals(smc.getSearchCriterionByAttributeName(iPartsTransferSMCAttributes.SMC_DESCRIPTION).getAttributeValue(),
                     newSmc.getSearchCriterionByAttributeName(iPartsTransferSMCAttributes.SMC_DESCRIPTION).getAttributeValue());
        assertEquals(smc.getSearchCriterionByAttributeName(iPartsTransferSMCAttributes.SMC_DESCRIPTION).getAttributeName(),
                     newSmc.getSearchCriterionByAttributeName(iPartsTransferSMCAttributes.SMC_DESCRIPTION).getAttributeName());

        for (iPartsTransferSMCAttributes att : smc.getResultAttributes()) {
            assertTrue(newSmc.getResultAttributes().contains(att));
        }
        for (iPartsTransferSMCAttributes att : newSmc.getResultAttributes()) {
            assertTrue(smc.getResultAttributes().contains(att));
        }

    }

    public void testWriteIPartsSearchMediaContainersResponseMessageToFile() {
//
        DwXmlFile xmlFile = XMLImportExportHelper.writeXMLFileFromMessageObject(testMessageSearchMediaContainersResponse,
                                                                                testChannelType, "iPartsOut", null, isForTest);
        // Message Element
        iPartsXMLMediaMessage newMessage;
        try {
            newMessage = (iPartsXMLMediaMessage)XMLImportExportHelper.buildMessageFromXMLFile(xmlFile, testChannelType, false, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertEquals(testMessageSearchMediaContainersResponse.isResponse(), newMessage.isResponse());
        assertEquals(testMessageSearchMediaContainersResponse.isRequest(), newMessage.isRequest());
        assertEquals(testMessageSearchMediaContainersResponse.getTypeObject().getClass(), newMessage.getTypeObject().getClass());
        // Response
        iPartsXMLResponse response = (iPartsXMLResponse)testMessageSearchMediaContainersResponse.getTypeObject();
        iPartsXMLResponse newResponse = (iPartsXMLResponse)newMessage.getTypeObject();

        assertEquals(response.getFromParticipant(), newResponse.getFromParticipant());
        assertEquals(response.getiPartsRequestID(), newResponse.getiPartsRequestID());
        assertEquals(response.getToParticipant(), newResponse.getToParticipant());
        // Success Element
        iPartsXMLSuccess success = response.getSuccess();
        iPartsXMLSuccess newSuccess = newResponse.getSuccess();

        assertEquals(success.getErrorCode(), newSuccess.getErrorCode());
        assertEquals(success.isErrorFree(), newSuccess.isErrorFree());
        assertEquals(success.getErrors(), newSuccess.getErrors());
        // ResSearchMediaContainers Element
        assertEquals(response.getResult().getResultType(), newResponse.getResult().getResultType());
        iPartsXMLResSearchMediaContainers rsmo = (iPartsXMLResSearchMediaContainers)response.getResult();
        iPartsXMLResSearchMediaContainers newRsmo = (iPartsXMLResSearchMediaContainers)newResponse.getResult();

        assertEquals(rsmo.getNumResultsDelivered(), newRsmo.getNumResultsDelivered());
        assertEquals(rsmo.getNumResultsFound(), newRsmo.getNumResultsFound());
        assertEquals(rsmo.getMContainers().size(), newRsmo.getMContainers().size());

        Map<String, iPartsXMLMediaContainer> tempContainers = new HashMap<>();

        for (iPartsXMLMediaContainer container : rsmo.getMContainers()) {
            tempContainers.put(container.getMcItemId(), container);
        }

        for (iPartsXMLMediaContainer container : newRsmo.getMContainers()) {
            assertNotNull(tempContainers.get(container.getMcItemId()));
            iPartsXMLMediaContainer tempContainer = tempContainers.get(container.getMcItemId());
            assertEquals(tempContainer.getMcItemRevId(), container.getMcItemRevId());
            assertEquals(tempContainer.getAttElements().size(), container.getAttElements().size());

            for (String key : container.getAttElements().keySet()) {
                assertNotNull(tempContainer.getAttElements().get(key));
                assertEquals(tempContainer.getAttElements().get(key), container.getAttElements().get(key));
            }
            for (String key : tempContainer.getAttElements().keySet()) {
                assertNotNull(container.getAttElements().get(key));
                assertEquals(container.getAttElements().get(key), tempContainer.getAttElements().get(key));
            }
        }
        tempContainers.clear();
        for (iPartsXMLMediaContainer container : newRsmo.getMContainers()) {
            tempContainers.put(container.getMcItemId(), container);
        }

        for (iPartsXMLMediaContainer container : rsmo.getMContainers()) {
            assertNotNull(tempContainers.get(container.getMcItemId()));
            iPartsXMLMediaContainer tempContainer = tempContainers.get(container.getMcItemId());
            assertEquals(tempContainer.getMcItemRevId(), container.getMcItemRevId());
            assertEquals(tempContainer.getAttElements().size(), container.getAttElements().size());

            for (String key : container.getAttElements().keySet()) {
                assertNotNull(tempContainer.getAttElements().get(key));
                assertEquals(tempContainer.getAttElements().get(key), container.getAttElements().get(key));
            }
            for (String key : tempContainer.getAttElements().keySet()) {
                assertNotNull(container.getAttElements().get(key));
                assertEquals(container.getAttElements().get(key), tempContainer.getAttElements().get(key));
            }
        }

        // History Element
        assertNotNull(testMessageSearchMediaContainersResponse.getHistory());
        assertNotNull(newMessage.getHistory());
        assertEquals(testMessageSearchMediaContainersResponse.getHistory().getClass(), newMessage.getHistory().getClass());

        iPartsXMLHistory history = testMessageSearchMediaContainersResponse.getHistory();
        iPartsXMLHistory newHistory = newMessage.getHistory();

        assertEquals(history.getRequest().getClass(), newHistory.getRequest().getClass());
        assertEquals(history.getResponse(), newHistory.getResponse());
        // Request Element
        iPartsXMLRequest request = history.getRequest();
        iPartsXMLRequest newRequest = newHistory.getRequest();

        assertEquals(request.getFromParticipant(), newRequest.getFromParticipant());
        assertEquals(request.getiPartsRequestID(), newRequest.getiPartsRequestID());
        assertEquals(request.getToParticipant(), newRequest.getToParticipant());
        // Requestor Element
        iPartsXMLRequestor requestor = request.getRequestor();
        iPartsXMLRequestor newRequestor = newRequest.getRequestor();

        assertEquals(requestor.getUserId(), newRequestor.getUserId());
        assertEquals(requestor.getGroupId(), newRequestor.getGroupId());
        // Operation Element
        AbstractXMLRequestOperation operation = request.getOperation();
        AbstractXMLRequestOperation newOperation = newRequest.getOperation();

        assertEquals(operation.getClass(), newOperation.getClass());
        assertEquals(operation.getOperationType(), newOperation.getOperationType());
        // SearchMediaContainers Element
        iPartsXMLSearchMediaContainers smc = (iPartsXMLSearchMediaContainers)operation;
        iPartsXMLSearchMediaContainers newSmc = (iPartsXMLSearchMediaContainers)newOperation;

        assertEquals(smc.getMaxResultFromIParts(), newSmc.getMaxResultFromIParts());
        assertEquals(smc.getOperationType(), newSmc.getOperationType());
        assertEquals(smc.hasMaxResult(), newSmc.hasMaxResult());
        assertEquals(smc.hasResultAttributes(), newSmc.hasResultAttributes());
        assertEquals(smc.hasSearchCriteria(), newSmc.hasSearchCriteria());
        assertEquals(smc.getSearchCriteria().size(), newSmc.getSearchCriteria().size());
        assertEquals(smc.getSearchCriterionByAttributeName(iPartsTransferSMCAttributes.SMC_NAME).getAttributeName(),
                     newSmc.getSearchCriterionByAttributeName(iPartsTransferSMCAttributes.SMC_NAME).getAttributeName());
        assertEquals(smc.getSearchCriterionByAttributeName(iPartsTransferSMCAttributes.SMC_NAME).getAttributeValue(),
                     newSmc.getSearchCriterionByAttributeName(iPartsTransferSMCAttributes.SMC_NAME).getAttributeValue());
        assertEquals(smc.getSearchCriterionByAttributeName(iPartsTransferSMCAttributes.SMC_DESCRIPTION).getAttributeValue(),
                     newSmc.getSearchCriterionByAttributeName(iPartsTransferSMCAttributes.SMC_DESCRIPTION).getAttributeValue());
        assertEquals(smc.getSearchCriterionByAttributeName(iPartsTransferSMCAttributes.SMC_DESCRIPTION).getAttributeName(),
                     newSmc.getSearchCriterionByAttributeName(iPartsTransferSMCAttributes.SMC_DESCRIPTION).getAttributeName());

        for (iPartsTransferSMCAttributes att : smc.getResultAttributes()) {
            assertTrue(newSmc.getResultAttributes().contains(att));
        }
        for (iPartsTransferSMCAttributes att : newSmc.getResultAttributes()) {
            assertTrue(smc.getResultAttributes().contains(att));
        }
    }

}
