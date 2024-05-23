/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.tests.xml;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferSMCAttributes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractMQMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.dialog.iPartsXMLTable;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.dialog.iPartsXMLTableDataset;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.AbstractMessageType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request.AbstractXMLRequestOperation;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.iPartsXMLMediaMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response.*;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlFile;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Testklasse für das Erzeugen von Objekten aus XML Dateien
 */
public class TestXMLToObjects extends AbstractTestXMLBase {

    private static final String TEST_USER_ID = "migimport";

    // setUp() überschreiben, wenn man die Daten von einem lokalen Verzeichnis in ein temporäres Testverzeichnis kopieren will
//    @Override
//    protected void setUp() throws Exception {
//        DWFile localDirectory = DWFile.get("E:/Testprojekte/Tests");
//        setOverriddenLocalTestcaseBaseDir(localDirectory);
//        super.setUp();
//        // Pfad zum Schema (lokal)
//        iPartsPlugin.XML_SCHEMA_PATH = localDirectory.getChild("de_docware_apps_etk_plugins_customer_daimler_iparts_tests_xml_AbstractTestXMLBase").getAbsolutePath();
//
//    }

    // für neues Schema wichtig
    public void testBuildiPartsMessageFromCreateMediaOrderRequestXML() {
        DWFile testFilesDir = tmpDir();
        for (DWFile request : testFilesDir.listDWFiles()) {
            try {
                iPartsXMLMediaMessage message = (iPartsXMLMediaMessage)XMLImportExportHelper.buildMessageFromXMLFile(new DwXmlFile(request),
                                                                                                                     testChannelType,
                                                                                                                     false, false);
                assertTrue(message.isRequest());
                assertFalse(message.isResponse());

                AbstractMessageType type = message.getTypeObject();
                assertNotNull(type);
                assertEquals(type.getClass(), iPartsXMLRequest.class);
                iPartsXMLRequest requestObject = (iPartsXMLRequest)type;
                assertEquals(requestObject.getFromParticipant(), IPARTS);
                assertEquals(requestObject.getToParticipant(), ASPLM);
                assertEquals(requestObject.getiPartsRequestID(), ID_RESPONSE_REQUEST);

                iPartsXMLRequestor requestor = requestObject.getRequestor();
                assertNotNull(requestor);
                assertEquals(TEST_USER_ID, requestor.getUserId());
                assertNull(requestor.getGroupId());

                AbstractXMLRequestOperation operation = requestObject.getOperation();
                assertNotNull(operation);
                assertEquals(operation.getClass(), iPartsXMLCreateMediaOrder.class);
                checkCMOElement(operation);
//                assertEquals(2, cmo.getPartPositions().size());
            } catch (Exception e) {
                Logger.getLogger().throwRuntimeException(e);
            }
        }
    }

    private void checkCMOElement(AbstractXMLRequestOperation operation) {
        iPartsXMLCreateMediaOrder cmo = (iPartsXMLCreateMediaOrder)operation;
        assertNotNull(cmo);
        assertEquals(CMO_NAME, cmo.getName());
        assertEquals(CMO_DESCRIPTION, cmo.getDescription());
        assertEquals(CMO_REMARK, cmo.getRemark());
        assertEquals(iPartsTransferConst.DEFAULT_REALIZATION_XML_VALUE, cmo.getRealization());
        assertEquals(CMO_DATE_STRING, DateUtils.toISO_Date(cmo.getDateDue()));
        assertEquals(CMO_EINPAS_ID, cmo.getEinPasId());
        assertEquals(CMO_EINPAS_TEXT_EN, cmo.getEinPASText().getText(Language.EN.getCode()));
        assertEquals(CMO_EINPAS_TEXT_ES, cmo.getEinPASText().getText(Language.ES.getCode()));
        assertEquals("", cmo.getEinPASText().getText(Language.DE.getCode()));
        assertEquals(CMO_KGTU_ID, cmo.getKgTuId());
        assertEquals(CMO_KGTU_TEXT_EN, cmo.getKgTuText().getText(Language.EN.getCode()));
        assertEquals(CMO_KGTU_TEXT_ES, cmo.getKgTuText().getText(Language.ES.getCode()));
        assertEquals("", cmo.getKgTuText().getText(Language.DE.getCode()));
        assertEquals(1, cmo.getProducts().size());
        assertEquals(1, cmo.getPartPositions().size());
        checkPartPositionElement(cmo);
    }

    private void checkPartPositionElement(iPartsXMLCreateMediaOrder cmo) {
        iPartsXMLPartPosition partPosition = cmo.getPartPositions().iterator().next();
        assertEquals(PART_POS_HOTSPOT, partPosition.getHotspot());
        assertNotNull(partPosition.getPartNumber());
        assertEquals(PART_POS_PARTNUMBER, partPosition.getPartNumber().getPartNumberText());
        assertEquals(PART_POS_QUANTITY, partPosition.getQuantity());
        assertEquals(PART_POS_STRUCTURE_LEVEL, partPosition.getStructureLevel());
        assertEquals(PART_POS_EXTERNAL_ID, partPosition.getExternalId());
        assertEquals(PART_POS_FAULT_LOCATION, partPosition.getFaultLocation());
        assertEquals(PART_POS_GENERIC_INSTALL_LOCATION, partPosition.getGenericInstallLocation());
        assertNotNull(partPosition.getPartName());
        assertEquals(PART_POS_PART_NAME.getText(Language.DE.getCode()), partPosition.getPartName().getText(Language.DE.getCode()));
        assertEquals(PART_POS_PART_NAME.getText(Language.EN.getCode()), partPosition.getPartName().getText(Language.EN.getCode()));
        assertEquals(PART_POS_PART_NAME.getText(Language.ES.getCode()), partPosition.getPartName().getText(Language.ES.getCode()));
        assertEquals(PART_POS_PART_NAME.getText(Language.PT.getCode()), partPosition.getPartName().getText(Language.PT.getCode()));
    }

    public void testBuildiPartsMessageFromCreateMcAttachmentsRequestXML() {
        DWFile testFilesDir = tmpDir();
        for (DWFile request : testFilesDir.listDWFiles()) {
            try {
                iPartsXMLMediaMessage message = (iPartsXMLMediaMessage)XMLImportExportHelper.buildMessageFromXMLFile(new DwXmlFile(request),
                                                                                                                     testChannelType,
                                                                                                                     false, false);
                assertTrue(message.isRequest());
                assertFalse(message.isResponse());

                AbstractMessageType type = message.getTypeObject();
                assertNotNull(type);
                assertEquals(iPartsXMLRequest.class, type.getClass());
                iPartsXMLRequest requestObject = (iPartsXMLRequest)type;
                assertEquals(IPARTS, requestObject.getFromParticipant());
                assertEquals(ASPLM, requestObject.getToParticipant());
                assertEquals(ID_RESPONSE_REQUEST, requestObject.getiPartsRequestID());

                iPartsXMLRequestor requestor = requestObject.getRequestor();
                assertNotNull(requestor);
                assertEquals(TEST_USER_ID, requestor.getUserId());
                assertNull(requestor.getGroupId());

                AbstractXMLRequestOperation operation = requestObject.getOperation();
                assertNotNull(operation);
                assertEquals(operation.getClass(), iPartsXMLCreateMcAttachments.class);
                iPartsXMLCreateMcAttachments cma = (iPartsXMLCreateMcAttachments)operation;
                assertNotNull(cma);
                assertNotNull(cma.getAttachments());
                assertEquals(1, cma.getAttachments().size());
                iPartsXMLAttachment attachment = cma.getAttachments().get(0);
                assertNotNull(attachment);
                assertEquals(iPartsXMLAttachment.class, attachment.getClass());
                assertEquals(CMA_ID, attachment.getId());
                assertEquals(CMA_NAME, attachment.getName());
                assertNull(attachment.getBinaryFile());
                iPartsXMLAttachmentTextFile textFile = attachment.getTextFile();
                assertNotNull(textFile);
                assertEquals(iPartsXMLAttachmentTextFile.class, textFile.getClass());
                String content = textFile.getContent();
                assertEquals(CMA_TEXT_CONTENT, content);
            } catch (Exception e) {
                Logger.getLogger().throwRuntimeException(e);
            }
        }
    }

    public void testBuildiPartsMessageFromCreateMediaOrderResponseXML() {

        DWFile testFilesDir = tmpDir();
        for (DWFile responseFile : testFilesDir.listDWFiles()) {
            try {
                iPartsXMLMediaMessage message = (iPartsXMLMediaMessage)XMLImportExportHelper.buildMessageFromXMLFile(new DwXmlFile(responseFile),
                                                                                                                     testChannelType,
                                                                                                                     false, false);
                assertTrue(message.isResponse());
                assertFalse(message.isRequest());
                AbstractMessageType type = message.getTypeObject();
                assertNotNull(type);
                assertEquals(type.getClass(), iPartsXMLResponse.class);

                iPartsXMLResponse response = (iPartsXMLResponse)message.getTypeObject();
                assertEquals(iPartsTransferNodeTypes.CREATE_MEDIA_ORDER, response.getRequestOperation());
                assertEquals(response.getFromParticipant(), ASPLM);
                assertEquals(response.getiPartsRequestID(), ID_RESPONSE_REQUEST);
                assertEquals(response.getToParticipant(), IPARTS);
                assertNotNull(response.getSuccess());

                // Success Element
                iPartsXMLSuccess success = response.getSuccess();
                assertEquals(success.getErrorCode(), iPartsTransferConst.ATTR_SUC_DEFAULT_ERRORCODE);
                assertTrue(success.isErrorFree());
                assertNotNull(success.getErrors());
                assertNotNull(response.getResult());
                assertEquals(response.getResult().getClass(), iPartsXMLResCreateMediaOrder.class);
                // ReCreateMediaOrder Element
                assertEquals(response.getResult().getResultType(), iPartsTransferNodeTypes.RES_CREATE_MEDIA_ORDER);
                iPartsXMLResCreateMediaOrder rcmo = (iPartsXMLResCreateMediaOrder)response.getResult();
                assertNotNull(rcmo.getMContainer());
                assertEquals(rcmo.getMContainer().getClass(), iPartsXMLMediaContainer.class);

                // MediaContainer Element
                iPartsXMLMediaContainer mc = rcmo.getMContainer();
                assertEquals(mc.getMcItemId(), MC_ITEM_ID);
                assertEquals(mc.getMcItemRevId(), MC_ITEM_REVID);

                // History
                assertNotNull(message.getHistory());
                iPartsXMLHistory history = message.getHistory();
                if (history.getRequest() == null) {
                    assertEquals(4, history.getTimeStamps().size());
                    assertEquals("Broker", history.getTimeStamps().get(0).getCreator());
                    assertEquals("reqReceived", history.getTimeStamps().get(0).getEvent());
                    assertEquals("2015-09-04T18:13:51.0", history.getTimeStamps().get(0).getDateTime());

                    assertEquals("AppServer", history.getTimeStamps().get(1).getCreator());
                    assertEquals("reqReceived", history.getTimeStamps().get(1).getEvent());
                    assertEquals("2015-09-04T18:13:52.0", history.getTimeStamps().get(1).getDateTime());

                    assertEquals("AppServer", history.getTimeStamps().get(2).getCreator());
                    assertEquals("resSent", history.getTimeStamps().get(2).getEvent());
                    assertEquals("2015-09-04T18:13:53.0", history.getTimeStamps().get(2).getDateTime());

                    assertEquals("Broker", history.getTimeStamps().get(3).getCreator());
                    assertEquals("resSent", history.getTimeStamps().get(3).getEvent());
                    assertEquals("2015-09-04T18:13:54.0", history.getTimeStamps().get(3).getDateTime());
                } else {
                    assertNotNull(history.getRequest());
                    assertNull(history.getResponse());

                    // Request Element
                    iPartsXMLRequest request = history.getRequest();
                    assertEquals(request.getFromParticipant(), IPARTS);
                    assertEquals(request.getToParticipant(), ASPLM);
                    assertEquals(request.getiPartsRequestID(), ID_RESPONSE_REQUEST);

                    iPartsXMLRequestor requestor = request.getRequestor();
                    assertNotNull(requestor);
                    assertEquals(TEST_USER_ID, requestor.getUserId());
                    assertNull(requestor.getGroupId());

                    AbstractXMLRequestOperation operation = request.getOperation();
                    assertNotNull(operation);
                    assertEquals(operation.getClass(), iPartsXMLCreateMediaOrder.class);
                    checkCMOElement(operation);
                }
            } catch (Exception e) {
                Logger.getLogger().throwRuntimeException(e);
            }
        }
    }

    public void testBuildiPartsMessageFromErrorXML() {
        DWFile testFilesDir = tmpDir();
        for (DWFile error : testFilesDir.listDWFiles()) {
            try {
                iPartsXMLMediaMessage message = (iPartsXMLMediaMessage)XMLImportExportHelper.buildMessageFromXMLFile(new DwXmlFile(error),
                                                                                                                     testChannelType,
                                                                                                                     false, false);
                assertTrue(message.isResponse());
                assertFalse(message.isRequest());
                AbstractMessageType type = message.getTypeObject();
                assertNotNull(type);
                assertEquals(type.getClass(), iPartsXMLResponse.class);

                iPartsXMLResponse response = (iPartsXMLResponse)message.getTypeObject();
                assertEquals(iPartsTransferNodeTypes.CREATE_MEDIA_ORDER, response.getRequestOperation());
                assertEquals(response.getFromParticipant(), ASPLM);
                assertEquals(response.getToParticipant(), IPARTS);
                assertEquals(response.getiPartsRequestID(), REQUEST_ID_SUCCESS_ERROR);

                assertNull(response.getResult());
                assertNotNull(response.getSuccess());
                if (message.getHistory() == null) {

                    iPartsXMLSuccess success = response.getSuccess();
                    assertFalse(success.isErrorFree());
                    assertEquals(success.getErrorCode(), 27);

                    List<iPartsXMLErrorText> errors = success.getErrors();
                    assertNotNull(errors);
                    assertFalse(errors.isEmpty());

                    for (iPartsXMLErrorText errorText : errors) {
                        assertEquals(errorText.getTextID(), "error003");
                        assertEquals(errorText.getText(), errorTextsWithLang.get(errorText.getLanguage()));
                    }
                } else {
                    iPartsXMLSuccess success = response.getSuccess();
                    assertFalse(success.isErrorFree());
                    assertEquals(success.getErrorCode(), 1);

                    List<iPartsXMLErrorText> errors = success.getErrors();
                    assertNotNull(errors);
                    assertFalse(errors.isEmpty());

                    for (iPartsXMLErrorText errorText : errors) {
                        assertEquals(errorText.getTextID(), "error003");
                        assertEquals(errorText.getText(), errorTextsWithLangInvalid.get(errorText.getLanguage()));
                    }
                    iPartsXMLHistory history = message.getHistory();
                    assertFalse(history.isValidRequest());
                    assertNotNull(history.getInvalidRequestAsText());
                }

            } catch (Exception e) {
                Logger.getLogger().throwRuntimeException(e);
            }
        }
    }

    //
    public void testBuildiPartsMessageFromSuccessXML() {
        DWFile testFilesDir = tmpDir();
        for (DWFile successFile : testFilesDir.listDWFiles()) {
            try {
                iPartsXMLMediaMessage message = (iPartsXMLMediaMessage)XMLImportExportHelper.buildMessageFromXMLFile(new DwXmlFile(successFile),
                                                                                                                     testChannelType,
                                                                                                                     false, false);
                assertTrue(message.isResponse());
                assertFalse(message.isRequest());
                AbstractMessageType type = message.getTypeObject();
                assertNotNull(type);
                assertEquals(type.getClass(), iPartsXMLResponse.class);

                iPartsXMLResponse response = (iPartsXMLResponse)message.getTypeObject();
                assertEquals(iPartsTransferNodeTypes.CREATE_MEDIA_ORDER, response.getRequestOperation());
                assertEquals(response.getFromParticipant(), ASPLM);
                assertEquals(response.getToParticipant(), IPARTS);
                assertEquals(response.getiPartsRequestID(), REQUEST_ID_SUCCESS_ERROR);

                assertNull(response.getResult());
                assertNotNull(response.getSuccess());

                iPartsXMLSuccess success = response.getSuccess();
                assertTrue(success.isErrorFree());
                assertNotNull(success.getErrors());
                assertEquals(success.getErrorCode(), iPartsTransferConst.ATTR_SUC_DEFAULT_ERRORCODE);
            } catch (Exception e) {
                Logger.getLogger().throwRuntimeException(e);
            }
        }
    }


    public void testBuildiPartsMessageFromSearchMediaContainersResponseXML() {

        DWFile testFilesDir = tmpDir();
        for (DWFile responseFile : testFilesDir.listDWFiles()) {
            try {
                iPartsXMLMediaMessage message = (iPartsXMLMediaMessage)XMLImportExportHelper.buildMessageFromXMLFile(new DwXmlFile(responseFile),
                                                                                                                     testChannelType,
                                                                                                                     false, false);
                assertTrue(message.isResponse());
                assertFalse(message.isRequest());
                AbstractMessageType type = message.getTypeObject();
                assertNotNull(type);
                assertEquals(type.getClass(), iPartsXMLResponse.class);

                iPartsXMLResponse response = (iPartsXMLResponse)message.getTypeObject();
                assertEquals(response.getFromParticipant(), ASPLM);
                assertEquals(response.getiPartsRequestID(), ID_RESPONSE_REQUEST);
                assertEquals(response.getToParticipant(), IPARTS);
                assertEquals(iPartsTransferNodeTypes.SEARCH_MEDIA_CONTAINERS, response.getRequestOperation());
                assertNotNull(response.getSuccess());

                // Success Element
                iPartsXMLSuccess success = response.getSuccess();
                assertEquals(success.getErrorCode(), iPartsTransferConst.ATTR_SUC_DEFAULT_ERRORCODE);
                assertTrue(success.isErrorFree());
                assertNotNull(success.getErrors());
                assertNotNull(response.getResult());
                assertEquals(response.getResult().getClass(), iPartsXMLResSearchMediaContainers.class);
                // ResSearchMEdiaConatiners Element
                assertEquals(response.getResult().getResultType(), iPartsTransferNodeTypes.RES_SEARCH_MEDIA_CONTAINERS);
                iPartsXMLResSearchMediaContainers rsmc = (iPartsXMLResSearchMediaContainers)response.getResult();
                assertNotNull(rsmc.getMContainers());
                assertEquals(rsmc.getNumResultsDelivered(), 2);
                assertEquals(rsmc.getNumResultsFound(), -1);
                List<iPartsXMLMediaContainer> mContainers = rsmc.getMContainers();
                for (iPartsXMLMediaContainer c : mContainers) {
                    // MediaContainer Element
                    assertNotNull(c.getMcItemId());
                    assertFalse(c.getMcItemId().isEmpty());
                    assertTrue(c.getMcItemId().startsWith("MC"));
                    assertEquals(c.getMcItemId().length(), 17);

                    assertNotNull(c.getMcItemRevId());
                    assertFalse(c.getMcItemRevId().isEmpty());
                    assertEquals(c.getMcItemRevId().length(), 3);
                }
                // History
                assertNotNull(message.getHistory());
                iPartsXMLHistory history = message.getHistory();
                assertNotNull(history.getRequest());
                assertNull(history.getResponse());

                // Request Element
                iPartsXMLRequest request = history.getRequest();
                assertEquals(request.getFromParticipant(), IPARTS);
                assertEquals(request.getToParticipant(), ASPLM);
                assertEquals(request.getiPartsRequestID(), ID_RESPONSE_REQUEST);

                iPartsXMLRequestor requestor = request.getRequestor();
                assertNotNull(requestor);
                assertEquals(TEST_USER_ID, requestor.getUserId());
                assertNull(requestor.getGroupId());

                AbstractXMLRequestOperation operation = request.getOperation();
                assertNotNull(operation);
                assertEquals(operation.getClass(), iPartsXMLSearchMediaContainers.class);
                iPartsXMLSearchMediaContainers smc = (iPartsXMLSearchMediaContainers)operation;
                assertNotNull(smc);
                assertTrue(smc.hasMaxResult());
                assertEquals(smc.getMaxResultFromIParts(), 300);
                assertTrue(smc.hasSearchCriteria());
                assertTrue(smc.hasResultAttributes());
                assertEquals(smc.getSearchCriteria().size(), 1);
                iPartsXMLSearchCriterion sC = smc.getSearchCriterionByAttributeName(iPartsTransferSMCAttributes.getValidatedAttributeByDescription("name"));
                assertNotNull(sC);
                assertEquals(sC.getAttributeName(), iPartsTransferSMCAttributes.getValidatedAttributeByDescription("name"));
                assertEquals(sC.getAttributeValue(), "B*");
                Set<iPartsTransferSMCAttributes> resultAttributes = smc.getResultAttributes();
                assertNotNull(resultAttributes);
                assertEquals(resultAttributes.size(), 2);
                assertTrue(resultAttributes.contains(iPartsTransferSMCAttributes.getValidatedAttributeByDescription("name")));
                assertTrue(resultAttributes.contains(iPartsTransferSMCAttributes.getValidatedAttributeByDescription("description")));

            } catch (Exception e) {
                Logger.getLogger().throwRuntimeException(e);
            }
        }
    }

    public void testBuildiPartsMessageFromSearchMediaContainersRequestXML() {
        DWFile testFilesDir = tmpDir();
        for (DWFile request : testFilesDir.listDWFiles()) {
            try {
                iPartsXMLMediaMessage message = (iPartsXMLMediaMessage)XMLImportExportHelper.buildMessageFromXMLFile(new DwXmlFile(request),
                                                                                                                     testChannelType,
                                                                                                                     false, false);
                assertTrue(message.isRequest());
                assertFalse(message.isResponse());

                AbstractMessageType type = message.getTypeObject();
                assertNotNull(type);
                assertEquals(type.getClass(), iPartsXMLRequest.class);
                iPartsXMLRequest requestObject = (iPartsXMLRequest)type;
                assertEquals(requestObject.getFromParticipant(), IPARTS);
                assertEquals(requestObject.getToParticipant(), ASPLM);
                assertEquals(requestObject.getiPartsRequestID(), ID_RESPONSE_REQUEST);

                iPartsXMLRequestor requestor = requestObject.getRequestor();
                assertNotNull(requestor);
                assertEquals(TEST_USER_ID, requestor.getUserId());
                assertNull(requestor.getGroupId());

                AbstractXMLRequestOperation operation = requestObject.getOperation();
                assertNotNull(operation);
                assertEquals(operation.getClass(), iPartsXMLSearchMediaContainers.class);
                iPartsXMLSearchMediaContainers smc = (iPartsXMLSearchMediaContainers)operation;
                assertNotNull(smc);
                assertTrue(smc.hasMaxResult());
                assertEquals(smc.getMaxResultFromIParts(), 300);
                assertTrue(smc.hasSearchCriteria());
                assertTrue(smc.hasResultAttributes());
                assertEquals(smc.getSearchCriteria().size(), 1);
                iPartsXMLSearchCriterion sC = smc.getSearchCriterionByAttributeName(iPartsTransferSMCAttributes.getValidatedAttributeByDescription("name"));
                assertNotNull(sC);
                assertEquals(sC.getAttributeName(), iPartsTransferSMCAttributes.getValidatedAttributeByDescription("name"));
                assertEquals(sC.getAttributeValue(), "B*");
                Set<iPartsTransferSMCAttributes> resultAttributes = smc.getResultAttributes();
                assertNotNull(resultAttributes);
                assertEquals(resultAttributes.size(), 2);
                assertTrue(resultAttributes.contains(iPartsTransferSMCAttributes.getValidatedAttributeByDescription("name")));
                assertTrue(resultAttributes.contains(iPartsTransferSMCAttributes.getValidatedAttributeByDescription("description")));
            } catch (Exception e) {
                Logger.getLogger().throwRuntimeException(e);
            }
        }
    }

    public void testDIALOGTableXML() {
        DWFile testFilesDir = tmpDir();
        for (DWFile request : testFilesDir.listDWFiles()) {
            try {
                AbstractMQMessage message = XMLImportExportHelper.buildMessageFromXMLFile(new DwXmlFile(request),
                                                                                          testChannelType,
                                                                                          false, false);
                assertTrue(message instanceof iPartsXMLTable);
                iPartsXMLTable table = (iPartsXMLTable)message;
                assertTrue(dialogTables.contains(table.getTableName()));
                assertEquals(DATE_FOR_DIALOG, table.getTrafoTime());
                assertEquals(DATE_FOR_DIALOG, table.getSourceExportTime());
                assertEquals(SCHEMA_VERSION, table.getSchemaVersion());
                assertEquals(iPartsXMLTable.TYPE, table.getMessageType());
                assertNotNull(table.getDatasets());
                assertFalse(table.getDatasets().isEmpty());
                for (iPartsXMLTableDataset dataset : table.getDatasets()) {
                    assertEquals("", dataset.getKem());
                    assertEquals("0", dataset.getSeqNo());
                    assertNotNull(dataset.getTagsAndValues());
                    assertFalse(dataset.getTagsAndValues().isEmpty());
                }
            } catch (IOException | SAXException e) {
                Logger.getLogger().throwRuntimeException(e);
            }

        }
    }


}
