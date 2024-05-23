/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.tests.xml;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.MQChannelType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsMQChannelTypeNames;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsProductTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferSMCAttributes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.iPartsXMLMediaMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response.*;
import de.docware.framework.modules.config.ConfigBase;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.config.containers.XMLConfigContainer;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.test.AbstractTest;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Hauptklasse für die XML-Import und -Export Tests
 */
public abstract class AbstractTestXMLBase extends AbstractTest {

    protected final static String MC_ITEM_ID = "MC000.010.000.007";
    protected final static String MC_ITEM_REVID = "001";
    protected final static String ASPLM = "ASPLM";
    protected final static String IPARTS = "iParts";
    protected final static String REQUEST_ID_SUCCESS_ERROR = "r0001";
    protected final static String ID_RESPONSE_REQUEST = "req_s01";
    protected final static String CMA_ID = "id1";
    protected final static String CMA_NAME = "TestNameCMA";
    protected final static String CMA_TEXT_CONTENT = "Umlaute in UTF-8: ä ü ö ß";
    protected final static String DE = "de";
    protected final static String EN = "en";
    protected final static String DE_TEXT = "Der angegebene User existiert nicht.";
    protected final static String EN_TEXT = "The named User does not exist.";
    protected final static String DE_TEXT_INVALID = "Inkorrekter Request (Invalides XML)";
    protected final static String EN_TEXT_INVALID = "Incorrect Request (invalid XML)";
    protected final static Date DATE_ORDERED = GregorianCalendar.getInstance().getTime();
    protected final static boolean isForTest = true;
    protected final static String DATE_FOR_DIALOG = "2006-05-04T18:13:51.0";
    protected final static String SCHEMA_VERSION = "1.1";
    protected final static String DIALOG_TABLE_PREFIX = "T10R";
    protected final static String CMO_NAME = "TestCase CMO Name";
    protected final static String CMO_REMARK = "This is a remark!";
    protected final static String CMO_DESCRIPTION = "Test description";
    protected final static String CMO_DATE_STRING = "2016-02-22";
    protected final static EinPasId CMO_EINPAS_ID = new EinPasId("35", "76", "33");
    protected final static String CMO_EINPAS_TEXT_EN = "TechnicalScope Engine";
    protected final static String CMO_EINPAS_TEXT_ES = "Machina";
    protected final static KgTuId CMO_KGTU_ID = new KgTuId("18", "567");
    protected final static String CMO_KGTU_TEXT_EN = "Phantasy name";
    protected final static String CMO_KGTU_TEXT_ES = "Phantasia";
    protected final static String CMO_CONTRACTOR_GROUP = "Test GroupId";
    protected final static String CMO_CONTRACTOR_USER = "Test UserId";
    protected final static String CMO_PRODUCT_MODEL = "D123456";
    protected final static String PART_POS_HOTSPOT = "10";
    protected final static String PART_POS_PARTNUMBER = "A9762600198";
    protected final static String PART_POS_QUANTITY = "1";
    protected final static String PART_POS_STRUCTURE_LEVEL = "1";
    protected final static String PART_POS_EXTERNAL_ID = "F0DAD8CEA0414EEB992B0B1572970CF3|30Y_27_012_00001||00002";
    protected final static String PART_POS_FAULT_LOCATION = "111111,222222";
    protected final static String PART_POS_GENERIC_INSTALL_LOCATION = "0004S";
    protected final static EtkMultiSprache PART_POS_PART_NAME = new EtkMultiSprache();

    protected final static String REQUESTOR_USER = "TestCaseUser";
    protected final static String REQUESTOR_GROUP = "TestCaseGroup";
    protected final static String REQUEST_ID = "testReqID";

    static {
        PART_POS_PART_NAME.setText(Language.DE, "SCHALTGERAET");
        PART_POS_PART_NAME.setText(Language.EN, "SWITCHING UNIT");
        PART_POS_PART_NAME.setText(Language.ES, "EQUIPO DE MANDO");
        PART_POS_PART_NAME.setText(Language.PT, "APARELHO DE MANOBRA");
    }

    protected MQChannelType testChannelType = new MQChannelType(iPartsMQChannelTypeNames.TEST, "TestQueueOut", "TestQueueIn");

    protected SimpleDateFormat formatter = new SimpleDateFormat(iPartsTransferConst.DATE_DUE_DATEFORMAT);
    protected Map<String, String> errorTextsWithLang = new HashMap<String, String>();
    protected Map<String, String> errorTextsWithLangInvalid = new HashMap<String, String>();
    protected iPartsXMLMediaMessage testMessageCreateMediaOrderRequest;
    protected iPartsXMLMediaMessage testMessageCreateMediaOrderResponse;
    protected iPartsXMLMediaMessage testMessageSearchMediaContainersRequest;
    protected iPartsXMLMediaMessage testMessageSearchMediaContainersResponse;
    protected Set<String> dialogTables = new HashSet<String>();
    protected Date testDate;

    @Override
    protected String getTestSuite() {
        return iPartsPlugin.INTERNAL_PLUGIN_NAME;
    }

    @Override
    protected void setUp() throws Exception {
        useTestCaseFilesFromVCS();
        super.setUp();
        // iParts Plug-in initialisieren, damit die PluginConfig existiert
        iPartsPlugin iPartsPlugin = new iPartsPlugin();
        ConfigBase pluginConfig = new ConfigBase(XMLConfigContainer.getInstanceInMemory());
        iPartsPlugin.initPlugin(pluginConfig);

        DWFile schemaMainPath = getOverriddenLocalTestcaseBaseDir();
        if (schemaMainPath == null) {
            schemaMainPath = AbstractTest.DEFAULT_TESTCASE_BASE_DIR;
        }

        iPartsPlugin.XML_SCHEMA_PATH = schemaMainPath.getChild("de_docware_apps_etk_plugins_customer_daimler_iparts_tests_xml_AbstractTestXMLBase").getAbsolutePath();

        testDate = GregorianCalendar.getInstance().getTime();

        // Aufbau Request
        // CreateMEdiaOrder
        EtkMultiSprache text = new EtkMultiSprache();
        text.setText(Language.DE, "");
        text.setText(Language.EN, CMO_EINPAS_TEXT_EN);
        text.setText(Language.ES, CMO_EINPAS_TEXT_ES);
        text.setText(Language.PT, "");
        iPartsXMLCreateMediaOrder cmo = new iPartsXMLCreateMediaOrder(iPartsTransferConst.DEFAULT_REALIZATION_XML_VALUE, testDate);
        cmo.setDescription(CMO_DESCRIPTION);
        cmo.setEinPasData(CMO_EINPAS_ID, text);
        String modelTypeAsAssignedProject = StrUtils.copySubString(CMO_PRODUCT_MODEL, 1, 3); // Project-ID = Typkennzahl ohne Sachnummernkennbuchstaben
        cmo.setAssignedProjects(modelTypeAsAssignedProject);
        cmo.setName(CMO_NAME);
        cmo.setRemark(CMO_REMARK);
        iPartsXMLContractor contractor = new iPartsXMLContractor(CMO_CONTRACTOR_GROUP);
        contractor.setUserId(CMO_CONTRACTOR_USER);
        cmo.setContractor(contractor);
        iPartsXMLProduct product = new iPartsXMLProduct(CMO_PRODUCT_MODEL, iPartsProductTypes.VEHICLE);
        cmo.addProduct(product);
        text = new EtkMultiSprache();
        text.setText(Language.DE, "");
        text.setText(Language.EN, CMO_KGTU_TEXT_EN);
        text.setText(Language.ES, CMO_KGTU_TEXT_ES);
        text.setText(Language.PT, "");
        cmo.setKgTuData(CMO_KGTU_ID, text);

        iPartsXMLPartPosition partPosition = new iPartsXMLPartPosition();
        partPosition.setHotspot(PART_POS_HOTSPOT);
        iPartsXMLPartNumber partNumber = new iPartsXMLPartNumber("", null, PART_POS_PARTNUMBER);
        partPosition.setPartNumber(partNumber);
        partPosition.setPartName(PART_POS_PART_NAME);
        partPosition.setQuantity(PART_POS_QUANTITY);
        partPosition.setStructureLevel(PART_POS_STRUCTURE_LEVEL);
        partPosition.setExternalId(PART_POS_EXTERNAL_ID);
        partPosition.setFaultLocation(PART_POS_FAULT_LOCATION);
        partPosition.setGenericInstallLocation(PART_POS_GENERIC_INSTALL_LOCATION);
        cmo.addPartPosition(partPosition);

        iPartsXMLRequestor requestor = new iPartsXMLRequestor(REQUESTOR_USER);
        requestor.setGroupId(REQUESTOR_GROUP);

        iPartsXMLRequest request = new iPartsXMLRequest(REQUEST_ID, iPartsTransferConst.PARTICIPANT_IPARTS, iPartsTransferConst.PARTICIPANT_ASPLM);
        request.setOperation(cmo);
        request.setRequestor(requestor);

        testMessageCreateMediaOrderRequest = new iPartsXMLMediaMessage(false);
        testMessageCreateMediaOrderRequest.setTypeObject(request);

        // SearchMediaContainers
        iPartsXMLSearchMediaContainers smc = new iPartsXMLSearchMediaContainers();
        smc.setMaxResultFromIParts(123);
        smc.addSearchCriterion(iPartsTransferSMCAttributes.SMC_NAME, "t*");
        smc.addSearchCriterion(iPartsTransferSMCAttributes.SMC_DESCRIPTION, "testD");
        smc.addResultAttribut(iPartsTransferSMCAttributes.SMC_EINPAS_HG);


        iPartsXMLRequestor requestorSMC = new iPartsXMLRequestor(REQUESTOR_USER);
        requestorSMC.setGroupId(REQUESTOR_GROUP);

        iPartsXMLRequest requestSMC = new iPartsXMLRequest("testReqID", iPartsTransferConst.PARTICIPANT_IPARTS, iPartsTransferConst.PARTICIPANT_ASPLM);
        requestSMC.setOperation(smc);
        requestSMC.setRequestor(requestorSMC);

        testMessageSearchMediaContainersRequest = new iPartsXMLMediaMessage(false);
        testMessageSearchMediaContainersRequest.setTypeObject(requestSMC);


        // Aufbau Response
        // ResCreateMediaOrder
        testMessageCreateMediaOrderResponse = new iPartsXMLMediaMessage(false);

        iPartsXMLHistory history = new iPartsXMLHistory();
        history.setRequest(request);
        testMessageCreateMediaOrderResponse.setHistory(history);

        iPartsXMLResponse response = new iPartsXMLResponse(REQUEST_ID, iPartsTransferNodeTypes.CREATE_MEDIA_ORDER,
                                                           iPartsTransferConst.PARTICIPANT_ASPLM, iPartsTransferConst.PARTICIPANT_IPARTS);

        iPartsXMLSuccess success = new iPartsXMLSuccess(true);
        response.setSuccess(success);

        iPartsXMLResCreateMediaOrder rcmo = new iPartsXMLResCreateMediaOrder(null);
        iPartsXMLMediaContainer mc = new iPartsXMLMediaContainer(MC_ITEM_ID, MC_ITEM_REVID);
        rcmo.setMContainer(mc);
        iPartsXMLMediaOrder mediaOrder = new iPartsXMLMediaOrder(DATE_ORDERED);
        rcmo.setMOrder(mediaOrder);
        response.setResult(rcmo);
        testMessageCreateMediaOrderResponse.setTypeObject(response);

        // ResSearchMediaConatiners
        testMessageSearchMediaContainersResponse = new iPartsXMLMediaMessage(false);

        iPartsXMLHistory historySMC = new iPartsXMLHistory();
        historySMC.setRequest(requestSMC);
        testMessageSearchMediaContainersResponse.setHistory(historySMC);

        iPartsXMLResponse responseSMC = new iPartsXMLResponse(REQUEST_ID, iPartsTransferNodeTypes.SEARCH_MEDIA_CONTAINERS,
                                                              iPartsTransferConst.PARTICIPANT_ASPLM, iPartsTransferConst.PARTICIPANT_IPARTS);

        iPartsXMLSuccess successSMC = new iPartsXMLSuccess(true);
        responseSMC.setSuccess(successSMC);

        iPartsXMLResSearchMediaContainers rsmc = new iPartsXMLResSearchMediaContainers(123);

        for (int i = 0; i < 3; i++) {
            iPartsXMLMediaContainer mContainer = new iPartsXMLMediaContainer("00000" + i, "00" + i);
            mContainer.addAttrElement("attValue" + i, "textValue" + i);
            rsmc.addMediaContainer(mContainer);
        }
        responseSMC.setResult(rsmc);

        testMessageSearchMediaContainersResponse.setTypeObject(responseSMC);

        //Füllen der Error Texte
        errorTextsWithLang.put(DE, DE_TEXT);
        errorTextsWithLang.put(EN, EN_TEXT);

        errorTextsWithLangInvalid.put(DE, DE_TEXT_INVALID);
        errorTextsWithLangInvalid.put(EN, EN_TEXT_INVALID);

        dialogTables.add(DIALOG_TABLE_PREFIX + "BCTE");
        dialogTables.add(DIALOG_TABLE_PREFIX + "FTS");
        dialogTables.add(DIALOG_TABLE_PREFIX + "FNR");
        dialogTables.add(DIALOG_TABLE_PREFIX + "BMS");
        dialogTables.add(DIALOG_TABLE_PREFIX + "BRS");
        dialogTables.add(DIALOG_TABLE_PREFIX + "WBCT");
        dialogTables.add(DIALOG_TABLE_PREFIX + "BCTX");
        dialogTables.add(DIALOG_TABLE_PREFIX + "KGVZ");


    }


}