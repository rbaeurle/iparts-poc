/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.AbstractJavaViewerPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsSpecType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataResponseSpike;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsResponseSpikeId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteContentId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EnrichReasons;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.TwoGridValues;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsWireHarnessHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsPRIMUSReplacementsCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.tests.AbstractTestiPartsWSEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.datacardsSimulation.iPartsWSDatacardsSimulationEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getmaterialnav.iPartsWSGetMaterialNavEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getmaterialpartinfo.iPartsWSGetMaterialPartInfoEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getmaterialparts.iPartsWSGetMaterialPartsEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getmedia.iPartsWSGetMediaEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getmodels.iPartsWSGetModelsEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getmodeltypes.iPartsWSGetModelTypesEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getnavopts.iPartsWSGetNavOptsEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getpartinfo.iPartsWSGetPartInfoEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getparts.iPartsWSGetPartsEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getproductclasses.iPartsWSGetProductClassesEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getproductgroups.iPartsWSGetProductGroupsEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.ident.iPartsWSIdentEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.partslist.iPartsWSPartsListEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.searchcomponent.iPartsWSsearchComponentEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.searchmaterialparts.iPartsWSSearchMaterialPartsEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.searchparts.iPartsWSSearchPartsEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.searchpartswocontext.iPartsWSSearchPartsWOContextEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.validateparts.iPartsWSValidatePartsEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.visualNav.iPartsWSVisualNavEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSWireHarnessHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSIdentContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSWiringHarness;
import de.docware.framework.modules.config.ConfigBase;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.config.defaultconfig.UniversalConfigOption;
import de.docware.framework.modules.config.defaultconfig.UniversalConfiguration;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObject;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.http.HttpConstants;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.file.DWFile;
import de.docware.util.test.FrameworkTestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.*;

/**
 * Unit-Tests für die iParts Webservices mit der Standard Oracle DB für Webservices Unit-Tests.
 */
public class TestiPartsWebservices extends AbstractTestiPartsWSEndpoint implements iPartsConst {

    /**
     * Eine manuelle {@link TestSuite} und {@link FrameworkTestSetup} erzeugen, um komplette Tests mit einem globalen SetUp
     * und TearDown zu ermöglichen inkl. Nachtests.
     *
     * @return
     */
    public static Test suite() {
        return createTestSuite(TestiPartsWebservices.class);

        // Einzelmethoden ausführen (Liste von Methoden angeben)
//        return createTestSuite(TestiPartsWebservices.class, "testGetPartsOmittedPartsFilterInDatacardFilter");
    }

    /**
     * Standardkonstruktor.
     */
    public TestiPartsWebservices() {
        super();
    }

    /**
     * Konstruktor für den kompletten Test über eine manuell erzeugte {@link TestSuite}.
     *
     * @param globalTest
     * @param methodName
     */
    public TestiPartsWebservices(TestiPartsWebservices globalTest, String methodName) {
        super(globalTest, methodName);
    }

    @Override
    public void globalSetUp() throws Exception {
        super.globalSetUp();

        // MqProject wird benötigt, um Dummy-Leitungssatzbaukästen vollständig nachzuladen
        iPartsPlugin.__internal_setMqProject(getProject());

        // Unittest-Modus in iPartsWSAbstractGetPartsEndpoint setzen, damit nicht alle GetParts-Responses angepasst werden müssen
        iPartsWSAbstractGetPartsEndpoint.IN_UNITTEST_MODE = true;

        // Unittest-Modus in iPartsWSIdentContext setzen, damit nicht alle Ident-Responses und andere mit IdentContext angepasst werden müssen
        iPartsWSIdentContext.IN_UNITTEST_MODE = true;

        // IAC Client URL setzen, damit der iPartsDataCardRetrievalHelper korrekt funktioniert
        String iacClientURL = "http://" + getWebserviceHost() + ":" + getWebservicePort();

        // WebservicePlugin als explizit aktiv markieren für die Unittests
        iPartsPlugin.forceWSPluginActiveForTesting = true;

        // Zwingend notwendige Einstellungen in der Konfiguration setzen
        ConfigBase config = iPartsPlugin.getPluginConfig().getConfig();
        synchronized (config) {
            config.startWriting();
            try {
                config.setString(iPartsPlugin.getPluginConfig().getPath() + iPartsPlugin.CONFIG_INTER_APP_COM_CLIENT_URL.getKey(),
                                 iacClientURL);
                config.setBoolean(iPartsPlugin.getPluginConfig().getPath() + iPartsPlugin.CONFIG_PRIMUS_HINT_HANDLING.getKey(),
                                  false);

                // Gleichteile-Mapping und Alternativteile für PRIMUS-Ersetzungen für alle Unittests deaktivieren und nur gezielt aktivieren
                config.setBoolean(iPartsPlugin.getPluginConfig().getPath() + iPartsPlugin.CONFIG_SHOW_EQUAL_PARTS.getKey(),
                                  false);
                config.setBoolean(iPartsWebservicePlugin.getPluginConfig().getPath() + iPartsPlugin.CONFIG_SHOW_ALTERNATIVE_PARTS_FOR_PRIMUS.getKey(),
                                  false);

                // Nur sichtbare Baumuster ausgeben
                config.setBoolean(iPartsPlugin.getPluginConfig().getPath() + iPartsPlugin.CONFIG_CHECK_MODEL_VISIBILITY.getKey(),
                                  true);

                // Keine Filterung von Leitungssatzbaukästen und kein SPK-Mapping
                config.setBoolean(iPartsPlugin.getPluginConfig().getPath() + iPartsPlugin.CONFIG_FILTER_WIRE_HARNESS_PARTS.getKey(),
                                  false);
                config.setBoolean(iPartsPlugin.getPluginConfig().getPath() + iPartsPlugin.CONFIG_USE_SPK_MAPPING.getKey(),
                                  false);
            } finally {
                config.commitWriting();
            }
        }
        config = iPartsWebservicePlugin.getPluginConfig().getConfig();
        synchronized (config) {
            config.startWriting();
            try {
                config.setBoolean(iPartsWebservicePlugin.getPluginConfig().getPath() + iPartsWebservicePlugin.CONFIG_USER_IN_PAYLOAD_FALLBACK.getKey(),
                                  true);
                config.setBoolean(iPartsWebservicePlugin.getPluginConfig().getPath() + iPartsWebservicePlugin.CONFIG_HEADER_ATTRIBUTES_FOR_PERMISSIONS.getKey(),
                                  false);
                config.setBoolean(iPartsWebservicePlugin.getPluginConfig().getPath() + iPartsWebservicePlugin.CONFIG_NO_AUTHENTIFICATION_GET_MEDIA.getKey(),
                                  true);
                config.setBoolean(iPartsWebservicePlugin.getPluginConfig().getPath() + iPartsWebservicePlugin.CONFIG_ONLY_RETAIL_RELEVANT_PRODUCTS.getKey(),
                                  true);
                config.setBoolean(iPartsWebservicePlugin.getPluginConfig().getPath() + iPartsWebservicePlugin.CONFIG_PARTS_LIST_ACTIVE.getKey(),
                                  true);
                config.setBoolean(iPartsWebservicePlugin.getPluginConfig().getPath() + iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_PERMISSIONS.getKey(),
                                  true);
                config.setBoolean(iPartsWebservicePlugin.getPluginConfig().getPath() + iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_COUNTRY_VALIDITY.getKey(),
                                  true);
                config.setBoolean(iPartsWebservicePlugin.getPluginConfig().getPath() + iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI.getKey(),
                                  false);
                config.setBoolean(iPartsWebservicePlugin.getPluginConfig().getPath() + iPartsWebservicePlugin.CONFIG_ONLY_FIN_BASED_REQUESTS_RMI.getKey(),
                                  false);
            } finally {
                config.commitWriting();
            }
        }

        clearWebservicePluginsCaches();
    }

    @Override
    protected void globalTearDown() throws Exception {
        iPartsPlugin.forceWSPluginActiveForTesting = false;
        super.globalTearDown();
    }

    @Override
    protected AbstractJavaViewerPlugin[] createWebservicePlugins() {
        return new AbstractJavaViewerPlugin[]{ new iPartsWebservicePlugin(getWebserviceHost(), getWebservicePort()) };
    }

    @Override
    protected void clearWebservicePluginsCaches() {
        super.clearWebservicePluginsCaches();
        iPartsPlugin.initConfigurationSettingsVariables();
        iPartsEditPlugin.initConfigurationSettingsVariables();
        iPartsWebservicePlugin.initConfigurationSettingsVariables();
    }

    // Alle Testmethoden müssen mit "test" anfangen

    // Unittests für einzelne Methoden
    public void testGetFootNoteContentText() {
        String dbLanguage = Language.DE.getCode();
        List<String> dbFallbackLanguages = getProject().getDataBaseFallbackLanguages();

        iPartsDataFootNoteContent dataFootNoteContent = new iPartsDataFootNoteContent(getProject(), new iPartsFootNoteContentId());
        DBDataObjectAttributes attributes = new DBDataObjectAttributes();
        attributes.addField(iPartsConst.FIELD_DFNC_FNID, "", DBActionOrigin.FROM_DB);
        attributes.addField(iPartsConst.FIELD_DFNC_LINE_NO, "", DBActionOrigin.FROM_DB);

        // Attribut für den Fußnotentext hinzufügen
        DBDataObjectAttribute attribute = new DBDataObjectAttribute(iPartsConst.FIELD_DFNC_TEXT, DBDataObjectAttribute.TYPE.MULTI_LANGUAGE,
                                                                    false, false);
        EtkMultiSprache multiLang = new EtkMultiSprache();
        attribute.setValueAsMultiLanguage(multiLang, DBActionOrigin.FROM_DB);
        attributes.addField(attribute, DBActionOrigin.FROM_DB);

        dataFootNoteContent.setAttributes(attributes, DBActionOrigin.FROM_DB);

        // multiLang von dataFootNoteContent neu holen (wird geklont beim Aufruf von setAttributes())
        multiLang = dataFootNoteContent.getAttribute(iPartsConst.FIELD_DFNC_TEXT).getAsMultiLanguage(dataFootNoteContent, false);

        multiLang.setText(dbLanguage, "");
        assertEquals("", dataFootNoteContent.getText(dbLanguage, dbFallbackLanguages));

        multiLang.setText(dbLanguage, "#AF");
        assertEquals("Ab Ident-Nr.:", dataFootNoteContent.getText(dbLanguage, dbFallbackLanguages));

        multiLang.setText(dbLanguage, " #BV");
        assertEquals(" Bis Vorderachse:", dataFootNoteContent.getText(dbLanguage, dbFallbackLanguages));

        multiLang.setText(dbLanguage, "#AF 123456");
        assertEquals("Ab Ident-Nr.: 123456", dataFootNoteContent.getText(dbLanguage, dbFallbackLanguages));

        multiLang.setText(dbLanguage, "#AF123456");
        assertEquals("Ab Ident-Nr.: 123456", dataFootNoteContent.getText(dbLanguage, dbFallbackLanguages));

        multiLang.setText(dbLanguage, "Text vorher #AF");
        assertEquals("Text vorher Ab Ident-Nr.:", dataFootNoteContent.getText(dbLanguage, dbFallbackLanguages));

        multiLang.setText(dbLanguage, "Text vorher #AF Text danach");
        assertEquals("Text vorher Ab Ident-Nr.: Text danach", dataFootNoteContent.getText(dbLanguage, dbFallbackLanguages));

        multiLang.setText(dbLanguage, "Text vorher #AFText danach ohne Leerzeichen");
        assertEquals("Text vorher Ab Ident-Nr.: Text danach ohne Leerzeichen", dataFootNoteContent.getText(dbLanguage, dbFallbackLanguages));

        multiLang.setText(dbLanguage, "Text vorher ohne Leerzeichen#AF");
        assertEquals("Text vorher ohne Leerzeichen#AF", dataFootNoteContent.getText(dbLanguage, dbFallbackLanguages));

        multiLang.setText(dbLanguage, "LUZ DE SE#ALIZACION LATERAL");
        assertEquals("LUZ DE SE#ALIZACION LATERAL", dataFootNoteContent.getText(dbLanguage, dbFallbackLanguages));

        multiLang.setText(dbLanguage, "WIS-NET#AF09.40-W-1100AG#");
        assertEquals("WIS-NET#AF09.40-W-1100AG#", dataFootNoteContent.getText(dbLanguage, dbFallbackLanguages));

        multiLang.setText(dbLanguage, "#A");
        assertEquals("#A", dataFootNoteContent.getText(dbLanguage, dbFallbackLanguages));

        multiLang.setText(dbLanguage, "##AF");
        assertEquals("##AF", dataFootNoteContent.getText(dbLanguage, dbFallbackLanguages));

        multiLang.setText(dbLanguage, "# #AF");
        assertEquals("# Ab Ident-Nr.:", dataFootNoteContent.getText(dbLanguage, dbFallbackLanguages));

        multiLang.setText(dbLanguage, "#AF 123456 und #AF 234567");
        assertEquals("Ab Ident-Nr.: 123456 und Ab Ident-Nr.: 234567", dataFootNoteContent.getText(dbLanguage, dbFallbackLanguages));

        multiLang.setText(dbLanguage, "#AF 123456, #BF 234567");
        assertEquals("Ab Ident-Nr.: 123456, Bis Ident-Nr.: 234567", dataFootNoteContent.getText(dbLanguage, dbFallbackLanguages));
    }

    /**
     * Liefert die Datenkarte zur übergebenen FIN
     *
     * @param fin
     * @return
     */
    private VehicleDataCard getVehicleDataCard(String fin) {
        try {
            // Datenkarte laden
            FinId id = new FinId(fin);
            String baseURI = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_DATACARDS_BASE_URI);
            String datacardJson = iPartsDataCardRetrievalHelper.getJsonFromWebservice(baseURI, id.getFIN(), getProject().getDBLanguage(), iPartsDataCardRetrievalHelper.DatacardType.VEHICLE);
            // Check Datenkarte vorhanden
            assertNotNull(datacardJson);
            assertFalse(datacardJson.isEmpty());
            iPartsWSvehicleInclMasterData responseAsVehicleDatacardJSONObject = iPartsDataCardRetrievalHelper.getResponseAsVehicleDatacardJSONObject(datacardJson, id);
            VehicleDataCard vehicleDataCard = new VehicleDataCard();
            vehicleDataCard.loadFromJSONObject(responseAsVehicleDatacardJSONObject, getProject(), false);
            // Fahrzeug-Datenkarte geladen
            assertTrue(vehicleDataCard.isDataCardLoaded());
            return vehicleDataCard;
        } catch (Exception e) {
            System.err.println(e);
            fail();
        }
        return null;
    }

    /**
     * Test für die Anreicherung von festen Fahrzeugcode auf Aggregate (DAIMLER-9429)
     */
    public void testEnrichAggregateDataCardsWithVehicleCacheCodes() {
        VehicleDataCard dataCard = getVehicleDataCard("WDC1660241A618433");
        assertNotNull(dataCard);
        // Check, dass die festen Code "055" und "805" auf der Fahrzeugdatenkarte existieren
        String testCodeOne = "055";
        String testCodeTwo = "805";
        checkVehicleCacheCodeExists(dataCard, testCodeOne);
        checkVehicleCacheCodeExists(dataCard, testCodeTwo);
        // Durchlaufe, alle Code der Datenkarte und prüfe, ob die Code auf die Datenkarte geschrieben wurden
        for (AggregateDataCard aggregateDataCard : dataCard.getActiveAggregates()) {
            if (aggregateDataCard.getAggregateType() == DCAggregateTypes.ENGINE) {
                TwoGridValues.ValueState value = aggregateDataCard.getCodes().getValueState(testCodeOne, false);
                checkForEnrichCode(value, testCodeOne, EnrichReasons.VEHICLE_TO_AGGREGAT);
                value = aggregateDataCard.getCodes().getValueState(testCodeTwo, false);
                checkForEnrichCode(value, testCodeTwo, EnrichReasons.VEHICLE_TO_AGGREGAT);
            }
        }
    }

    /**
     * Test für die Erzeugung und Anreicherung einer Brennstoffzellen-Datenkarte aus VPD Daten
     */
    public void testFuelCellAggregateDatacard() {
        VehicleDataCard dataCard = getVehicleDataCard("WDC2539931F684419");
        assertNotNull(dataCard);
        AggregateDataCard aggregateDataCard = dataCard.getAggregateDataCard(DCAggregateTypes.FUEL_CELL, DCAggregateTypeOf.FUEL_CELL1);
        assertNotNull(aggregateDataCard);
        String testCode = "XYZXYZ";
        TwoGridValues.ValueState value = aggregateDataCard.getCodes().getValueState(testCode, false);
        checkForEnrichCode(value, testCode, EnrichReasons.APARTNO_TO_CODE);

        // Datenkarte über Ident WS prüfen
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");
        executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identCode\":\"WDC2539931F684419\"}", additionalRequestProperties,
                          "{\"identContexts\":[{\"aggregates\":[{\"aggregateNumber\":\"785020300001930\",\"aggSubType\":\"fuelCell1\",\"aggTypeId\":\"N\",\"modelDesc\":\".\",\"modelId\":\"D785020\",\"modelTypeId\":\"D785\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"BRE123\"}],\"aggTypeId\":\"F\",\"connectWireHarnessEnabled\":true,\"datacardExists\":true,\"fin\":\"WDC2539931F684419\",\"integratedNavigationAvailable\":true,\"modelDesc\":\"Test\",\"modelId\":\"C253993\",\"modelTypeId\":\"C253\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"C253_FC\"}]}", 200);
    }

    /**
     * Check, ob es sich um einen Fahrzeug-Code aus dem Cache handelt und ob der Code auf der Fahrzeugdatenkarte vorkommt
     *
     * @param dataCard
     * @param code
     */
    private void checkVehicleCacheCodeExists(VehicleDataCard dataCard, String code) {
        assertTrue(iPartsVehicleToAggregateCodeCache.getInstance(getProject()).isVehicleToAggregateCode(code));
        TwoGridValues.ValueState value = dataCard.getCodes().getValueState(code, false);
        assertNotNull(value);
        assertTrue(value.checked);
        assertEquals(code, value.value);
    }

    /**
     * Anstelle des Datenkartentyps TRANSFER_CASE soll der Typ TRANSMISSION angefragt und auch vom VIS geliefert werden.
     */
    public void testLoadAggregateDataCardTransferCase() {
        // Angefragt wird: Verteilergetriebe TRANSFER_CASE
        AggregateDataCard aggregateDataCard = getAggregateDataCard("71168001095329", DCAggregateTypes.TRANSFER_CASE);
        assertNotNull(aggregateDataCard);

        // Das ist das wesentliche Ergebnis: TRANSMISSION zurückgeliefert statt des angeforderten TRANSFER_CASE.
        assertEquals(DCAggregateTypes.TRANSMISSION, aggregateDataCard.getAggregateType());

        // Die Kür: noch ein paar zusätzliche Werte überprüfen:
        assertEquals(true, aggregateDataCard.isDataCardLoaded());
        assertEquals("71168001095329", aggregateDataCard.getAggregateIdent());
        assertEquals("D711680", aggregateDataCard.getModelNo());
        assertEquals("A9062602401", aggregateDataCard.getObjectNo());
        assertEquals("20160926", aggregateDataCard.getTechnicalApprovalDate());
        assertEquals("GM", aggregateDataCard.getGearboxValue());
    }

    /**
     * Es gibt keine Aggregatedatenkarten vom Typ STEERING.
     * Das muss per Definition zu einem NULL-Wert im Ergebnis führen.
     */
    public void testLoadAggregateDataCardSteering() {
        AggregateDataCard aggregateDataCard = getAggregateDataCard("WDC1234567X123456", DCAggregateTypes.STEERING);
        assertNull(aggregateDataCard);
    }

    /**
     * Liefert die Aggregatedatenkarte zum übergebenen Ident und Datenkartentyp
     *
     * @param ident Nummer der Datenkarte
     * @param type  Typ der Datenkarte
     * @return Die geladene Datenkarte oder null
     */
    private AggregateDataCard getAggregateDataCard(String ident, DCAggregateTypes type) {
        try {
            // Aggregatedatenkarte laden
            AggregateDataCard aggregateDataCard = iPartsDataCardRetrievalHelper.getAggregateDataCard(getProject(), type, ident, null);
            if ((aggregateDataCard != null) && aggregateDataCard.isDataCardLoaded()) {
                return aggregateDataCard;
            }
        } catch (Exception e) {
            System.err.println(e);
        }
        return null;
    }

    public void testGetPartsInAggregateIdentWithTransferCaseDatacard() {
        // Angefragt wird: Verteilergetriebe TRANSFER_CASE
        // Das wird in [getAggregateDataCard()] umgesetzt in TRANSMISSION.
        // Sieht man am Vernünftigsten nur im Debugger.
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggregateNumber\": \"71168001095328\",\"aggTypeId\": \"VG\",\"datacardExists\": true,\"modelId\": \"D711680\",\"productClassIds\":[\"T\"],\"productId\": \"11Q\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"26\"},{\"type\":\"cg_subgroup\",\"id\":\"100\"}]}",
                          DWFile.get(getTestWorkingDir(), "resultPartsAggregateDataCardWithTransferCase.txt"));
    }

    public void testUseCodesForDateWithoutFactorySign() {
        VehicleDataCard dataCard = getVehicleDataCard("WDC4632611X325887");
        assertNotNull(dataCard);
        AggregateDataCard aggregate = null;
        for (AggregateDataCard agg : dataCard.getActiveAggregates()) {
            if (agg.getAggregateType() == DCAggregateTypes.TRANSMISSION) {
                aggregate = agg;
                break;
            }
        }
        // Aggregat existiert
        assertNotNull(aggregate);
        // Alle Code-Datensätze für die ObjectNo des Aggregats holen
        iPartsAggCodeMappingCache.ObjectNoData objectsForObjectNumber = iPartsAggCodeMappingCache.getInstance(getProject()).getAggCodeMappingDataByPartNo(aggregate.getObjectNoAsAPartNo());
        // Check, dass welche existieren
        assertNotNull(objectsForObjectNumber);
        // Werkskennbuchstabe vom Aggregat
        String factorySignFromAggregate = aggregate.getFactorySign(getProject());
        // Check, dass es ein valider Werkskennbuchstabe ist
        assertTrue(StrUtils.isValid(factorySignFromAggregate));
        // Check, dass es für den Werkskennbuchstaben keine Einträge gibt
        assertTrue(objectsForObjectNumber.getDataForFactorySign(factorySignFromAggregate).isEmpty());
        // Und jetzt prüfen, ob die Daten trotz unterschiedlichen Werkskennbuchstaben auf die Datenkarte geschrieben wurden
        // -> Somit spielt der Werkskennbuchtabe keine Rolle (DAIMLER-9440)
        for (String codeFromData : objectsForObjectNumber.getCode()) {
            TwoGridValues.ValueState value = aggregate.getCodes().getValueState(codeFromData, false);
            if (value == null) {
                value = aggregate.getCodes().getValueState(codeFromData, true);
                assertNotNull(value);
                continue;
            }
            checkForEnrichCode(value, codeFromData, EnrichReasons.APARTNO_TO_CODE);
        }
    }

    /**
     * Überprüft, ob der übergebene Code ein angereicherter Code ist
     *
     * @param value
     * @param code
     */
    private void checkForEnrichCode(TwoGridValues.ValueState value, String code, EnrichReasons enrichReason) {
        assertNotNull(value);
        assertTrue(value.checked);
        assertEquals(code, value.value);
        assertEquals(TranslationHandler.translate(enrichReason.getDescription()), value.getEnrichText());
    }

    /**
     * Testmethode bei der geprüft wird, ob Motor- und Getriebe-Aggregate-Datenkarten richtig angereichert werden.
     * <p>
     * siehe DAIMLER-7644
     */
    public void testEnrichAggregateDataCards() {
        // Test zur Story DAIMLER-7644
        try {
            VehicleDataCard vehicleDataCard = getVehicleDataCard("WDB9071311N032959");
            assertNotNull(vehicleDataCard);
            // Fahrzeugdatenkarte enthält drei Aggregatedatenkarten
            assertEquals(3, vehicleDataCard.getActiveAggregates().size());
            // Fahrzeugdatenkarte enthält genau 90 Code
            Collection<TwoGridValues.ValueState> vehicleCodes = vehicleDataCard.getCodes().getBottomGridValues();
            assertNotNull(vehicleCodes);
            assertTrue(!vehicleCodes.isEmpty());
            assertEquals(90, vehicleCodes.size());
            AggregateDataCard engineDataCard = vehicleDataCard.getAggregateDataCard(DCAggregateTypes.ENGINE, DCAggregateTypeOf.NONE);
            // Fahrzeug-Datenkarte enthält eine Motor-Datenkarte
            assertNotNull(engineDataCard);
            // Motor-Datenkarte ist geladen
            assertTrue(engineDataCard.isDataCardLoaded());
            // Motor-Datenkarte hat genau einen (angereicherten) Code
            Collection<TwoGridValues.ValueState> engineCodes = engineDataCard.getCodes().getBottomGridValues();
            assertNotNull(engineCodes);
            assertTrue(!engineCodes.isEmpty());
            assertEquals(1, engineCodes.size());
            assertTrue(engineDataCard.getCodes().isEnriched(false));
            // Die ZB-Sachnummer "A6510109719" auf der Motor-Datenkarte zeigt in der Tabelle DA_AGG_PART_CODES auf genau
            // einen Code mit der Baureihe "C907". Da diese Baureihe mit einem "C" anfängt, dürfen die Code des Fahrzeugs
            // der Motor-Datenkarte nicht beigemischt werden (siehe DAIMLER-7644 bzw. DAIMLER-7911). Daher hier der Check,
            // ob die Code der Fahrzeug-Datenkarte in den Code der Motor-Datenkarte vorkommen
            assertFalse(engineDataCard.getCodes().getBottomGridValues().containsAll(vehicleDataCard.getCodes().getBottomGridValues()));
            // Check, dass es sich bei dem Code um den beigemischten Code aus der Tabelle DA_AGG_PART_CODES handelt
            TwoGridValues.ValueState valueStateSingleAggPartCode = engineDataCard.getCodes().getValueState("EDDIE", false);
            assertNotNull(valueStateSingleAggPartCode);
            assertEquals(1, valueStateSingleAggPartCode.enrichReasons.size());
            assertEquals(EnrichReasons.APARTNO_TO_CODE, valueStateSingleAggPartCode.enrichReasons.iterator().next());
        } catch (Exception e) {
            System.err.println(e);
            fail();
        }
    }


    // Explizite Tests für die Filterung
    public void testGetPartInfoForFilterDAIMLER6571() {
        // FIN WDC1641201A170000 ist auch nach Baumuster- und Lenkungs-Filterung der Idents noch enthalten, aber mit deutlich
        // weniger Idents
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"partContext\":{\"moduleId\":\"63J_42_060_00001\",\"sequenceId\":\"00025\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C164120\",\"productId\":\"63J\",\"fin\":\"WDC1641201A170000\"}," +
                          "\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultPartsForFilterDAIMLER6571.txt"));
    }

    public void testGetPartInfoForFilterDAIMLER6571Error() {
        // FIN WDC1641201A169000 ist nach Baumuster- und Lenkungs-Filterung der Idents nicht mehr enthalten
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"partContext\":{\"moduleId\":\"63J_42_060_00001\",\"sequenceId\":\"00025\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C164120\",\"productId\":\"63J\",\"fin\":\"WDC1641201A169000\"}," +
                          "\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"code\":4001,\"message\":\"Path 'partContext': SequenceId '00025' is invalid in module '63J_42_060_00001' for current context and filter options\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testGetPartsForFilterDAIMLER6672() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C156942\",\"productId\":\"P06\",\"fin\":\"WDC1569421J400312\",\"datacardExists\":true}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"91\"},{\"type\":\"cg_subgroup\",\"id\":\"070\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultPartsForFilterDAIMLER6672.txt"));
    }

    public void testGetPartsForFilterDAIMLER6701() {
        // Das Admin-Flag "Ungültige Werke in der Filterung ignorieren" muss für diesen Testfall temporär aktiviert werden
        UniversalConfiguration pluginConfig = iPartsPlugin.getPluginConfig();
        boolean oldIgnoreInvalidFactoriesInFilter = pluginConfig.getConfig().getBoolean(pluginConfig.getPath() + iPartsPlugin.CONFIG_IGNORE_INVALID_FACTORIES_IN_FILTER.getKey(),
                                                                                        false);
        writeBooleanConfigValues(pluginConfig, true, iPartsPlugin.CONFIG_IGNORE_INVALID_FACTORIES_IN_FILTER);

        try {
            clearWebservicePluginsCaches(); // Caches müssen gelöscht werden, damit die Admin-Änderung auch auf jeden Fall berücksichtigt wird
            executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C203042\",\"productId\":\"69N\",\"fin\":\"WDB2030421A826776\",\"datacardExists\":true}," +
                              "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"32\"},{\"type\":\"cg_subgroup\",\"id\":\"054\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                              DWFile.get(getTestWorkingDir(), "resultPartsForFilterDAIMLER6701.txt"));
        } finally {
            writeBooleanConfigValues(pluginConfig, oldIgnoreInvalidFactoriesInFilter, iPartsPlugin.CONFIG_IGNORE_INVALID_FACTORIES_IN_FILTER);
            clearWebservicePluginsCaches(); // Caches müssen gelöscht werden, damit die Admin-Änderung auch auf jeden Fall berücksichtigt wird
        }
    }

    public void testGetPartsForFilterDAIMLER6709() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C164122\",\"productId\":\"63J\",\"fin\":\"WDC1641221A535248\",\"datacardExists\":true}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"32\"},{\"type\":\"cg_subgroup\",\"id\":\"105\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultPartsForFilterDAIMLER6709.txt"));
    }

    public void testGetPartsForFilterDAIMLER6809() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C216394\",\"productId\":\"65V\"}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"33\"}, {\"type\":\"cg_subgroup\",\"id\":\"030\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultPartsForFilterDAIMLER6809.txt"));
    }

    public void testGetPartsForFilterDAIMLER6825() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C219356\",\"productId\":\"66W\",\"fin\":\"WDD2193562A046914\",\"datacardExists\":true}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"83\"},{\"type\":\"cg_subgroup\",\"id\":\"190\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultPartsForFilterDAIMLER6825.txt"));
    }

    public void testGetPartsForFilterDAIMLER6991() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C246200\",\"productId\":\"P01\",\"fin\":\"WDD2462001J111252\",\"datacardExists\":true}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"42\"},{\"type\":\"cg_subgroup\",\"id\":\"045\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultPartsForFilterDAIMLER6991.txt"));
    }

    public void testGetPartsForSpecialZBFilterDAIMLER15366() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"E\",\"productClassIds\":[\"L\"],\"modelId\":\"D934911\",\"productId\":\"M10\",\"fin\":\"WDB96700210033607\",\"datacardExists\":true}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"14\"},{\"type\":\"cg_subgroup\",\"id\":\"030\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartsForSpecialZBFilterDAIMLER15366.txt"));
    }

    /**
     * Testet, ob PROVAL Benennungen vor anderen Benennungen für Code verwendet werden. Dafür wurde die Coderegel der
     * laufenden Nummer 00206 in 62T_47_035_00001 um den künstlichen Code "PVL" ergänzt. PVL gibt es zweimal in der DB.
     * Einmal mit der Quelle "PROVAL" und einmal mit der Quelle "MAD".
     */
    public void testGetPartInfoWithPROVALCode() {
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"partContext\":{\"moduleId\":\"62T_47_035_00001\",\"sequenceId\":\"00206\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C463202\",\"productId\":\"62T\"},\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"partInfo\":{\"codeValidityDetails\":[{\"code\":\"H12\",\"dateFrom\":\"2002-04-15\",\"name\":\"STANDHEIZUNG\"},{\"code\":\"PVL\",\"dateFrom\":\"2002-04-15\",\"name\":\"PROVAL Benennung vor MAD\"}],\"codeValidityMatrix\":[[{\"code\":\"H12\"}],[{\"code\":\"PVL\"}]]}}");

    }


    public void testGetPartInfoForModelScoringDAIMLER6999Part1() {
        // FIN WDC1648281A289204
        // Positionen, die aufgrund des Scorings mit nur bm-bildenden Teilkonjunktionen in der Stückliste bleiben
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"partContext\":{\"moduleId\":\"63L_73_060_00001\",\"sequenceId\":\"00005\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C164828\",\"productId\":\"63L\",\"fin\":\"WDC1648281A289204\",\"datacardExists\":true}," +
                          "\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"partInfo\":{\"codeValidityDetails\":[{\"code\":\"M273\",\"dateFrom\":\"2000-03-03\",\"name\":\"V8-OTTOMOTOR M273\"},{\"code\":\"M46\",\"dateFrom\":\"2001-05-16\",\"name\":\"HUBRAUM 4,6 LITER\"},{\"code\":\"M629\",\"dateFrom\":\"2002-12-03\",\"name\":\"V8-DIESELMOTOR OM629\"}],\"codeValidityMatrix\":[[{\"code\":\"M629\"}],[{\"code\":\"M273\"},{\"code\":\"M46\"}]],\"plantInformation\":[{\"date\":\"2007-06-26\",\"ident\":\"A296453\",\"modelYear\":\"808\",\"modelYearDetails\":[{\"code\":\"808\",\"dateFrom\":\"2004-08-06\",\"name\":\"AEJ 07/1\"}],\"plant\":\"A, T\",\"type\":\"bis\"}]}}");
    }

    public void testGetPartInfoForModelScoringDAIMLER6999Part2() {
        // FIN WDC1648281A289204
        // Positionen, die aufgrund des Scorings mit nur bm-bildenden Teilkonjunktionen in der Stückliste bleiben
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"partContext\":{\"moduleId\":\"63L_73_060_00001\",\"sequenceId\":\"00026\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C164828\",\"productId\":\"63L\",\"fin\":\"WDC1648281A289204\",\"datacardExists\":true}," +
                          "\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"partInfo\":{\"codeValidityDetails\":[{\"code\":\"M273\",\"dateFrom\":\"2000-03-03\",\"name\":\"V8-OTTOMOTOR M273\"},{\"code\":\"M46\",\"dateFrom\":\"2001-05-16\",\"name\":\"HUBRAUM 4,6 LITER\"},{\"code\":\"M629\",\"dateFrom\":\"2002-12-03\",\"name\":\"V8-DIESELMOTOR OM629\"}],\"codeValidityMatrix\":[[{\"code\":\"M629\"}],[{\"code\":\"M273\"},{\"code\":\"M46\"}]],\"plantInformation\":[{\"date\":\"2007-06-26\",\"ident\":\"A296453\",\"modelYear\":\"808\",\"modelYearDetails\":[{\"code\":\"808\",\"dateFrom\":\"2004-08-06\",\"name\":\"AEJ 07/1\"}],\"plant\":\"A, T\",\"type\":\"bis\"}]}}");
    }

    public void testGetPartInfoForModelScoringDAIMLER6999Part3() {
        // FIN WDC1648281A289204
        // Positionen, die aufgrund des Scorings mit nur bm-bildenden Teilkonjunktionen aus der Stückliste fliegen
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"partContext\":{\"moduleId\":\"63L_73_060_00001\",\"sequenceId\":\"00007\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C164828\",\"productId\":\"63L\",\"fin\":\"WDC1648281A289204\",\"datacardExists\":true}," +
                          "\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"code\":4001,\"message\":\"Path 'partContext': SequenceId '00007' is invalid in module '63L_73_060_00001' for current context and filter options\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testGetPartInfoForModelScoringDAIMLER6999Part4() {
        // FIN WDC1648281A289204
        // Positionen, die aufgrund des Scorings mit nur bm-bildenden Teilkonjunktionen aus der Stückliste fliegen
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"partContext\":{\"moduleId\":\"63L_73_060_00001\",\"sequenceId\":\"00027\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C164828\",\"productId\":\"63L\",\"fin\":\"WDC1648281A289204\",\"datacardExists\":true}," +
                          "\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"code\":4001,\"message\":\"Path 'partContext': SequenceId '00027' is invalid in module '63L_73_060_00001' for current context and filter options\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testGetPartsForFilterDAIMLER7002() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"M\",\"productClassIds\":[\"P\"],\"modelId\":\"D651955\",\"productId\":\"D80\"}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"18\"},{\"type\":\"cg_subgroup\",\"id\":\"075\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultPartsForFilterDAIMLER7002.txt"));
    }

    public void testGetPartsForFilterDAIMLER7041() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C156952\",\"productId\":\"P06\"}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"54\"}, {\"type\":\"cg_subgroup\",\"id\":\"640\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultPartsForFilterDAIMLER7041.txt"));
    }

    public void testGetPartInfoForFilterDAIMLER7068() {
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"partContext\":{\"moduleId\":\"D61_69_018_00001\",\"sequenceId\":\"00001\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C166024\",\"productId\":\"D61\",\"fin\":\"WDC1660241B087026\",\"datacardExists\":true}," +
                          "\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"partInfo\":{\"colors\":[{\"codeValidity\":\"55U/65U;\",\"codeValidityDetails\":[{\"code\":\"55U\",\"dateFrom\":\"2009-09-29\",\"name\":\"DACHINNENVERKLEIDUNG STOFF BEIGE\"},{\"code\":\"65U\",\"dateFrom\":\"2013-07-22\",\"name\":\"DACHINNENVERKLEIDUNG MICROFASER DINAMICA BEIGE\"}],\"codeValidityMatrix\":[[{\"code\":\"55U\"}],[{\"code\":\"65U\"}]],\"es2Key\":\"1B88\",\"plantInformation\":[{\"date\":\"2016-08-17\",\"plant\":\"A, B\",\"type\":\"von\"},{\"date\":\"2016-07-31\",\"ident\":\"L013410\",\"plant\":\"L, M (CKD)\",\"type\":\"von\"},{\"date\":\"2016-07-31\",\"ident\":\"M013410\",\"plant\":\"L, M (CKD)\",\"type\":\"von\"}]}]}}");
    }

    public void testGetPartsForFilterDAIMLER7200() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\": \"GM\",\"productClassIds\":[\"P\"],\"datacardExists\": true,\"fin\": \"WDD2040481A426155\",\"modelId\": \"D711655\",\"productId\": \"00P\"}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"26\"},{\"type\":\"cg_subgroup\",\"id\":\"600\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultPartsForFilterDAIMLER7200.txt"));
    }

    public void testGetPartsForFilterDAIMLER7217() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C169008\",\"productId\":\"66B\",\"fin\":\"WDD1690081J065292\",\"datacardExists\":true}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"54\"}, {\"type\":\"cg_subgroup\",\"id\":\"062\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultPartsForFilterDAIMLER7217.txt"));
    }

    public void testGetPartsForFilterDAIMLER7642() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\": \"GA\",\"productClassIds\":[\"P\"],\"datacardExists\": true,\"fin\": \"WDB2032041F408292\",\"modelId\": \"D611962\",\"productId\": \"19U\"}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"07\"},{\"type\":\"cg_subgroup\",\"id\":\"122\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultPartsForFilterDAIMLER7642.txt"));
    }

    public void testGetPartsForFilterDAIMLER7803() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C251163\",\"productId\":\"63U\",\"fin\":\"WDC2511631E004487\",\"datacardExists\":true}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"74\"},{\"type\":\"cg_subgroup\",\"id\":\"015\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultPartsForFilterDAIMLER7803.txt"));
    }

    public void testGetPartsForDAIMLER9715() {
        // prüfen ob für dieses Baumuster auch wirklich  DM_FILTER_RELEVANT = false ist
        iPartsDataModel model = new iPartsDataModel(getProject(), new iPartsModelId("D651915"));
        assertTrue(model.existsInDB());
        assertFalse(model.getFieldValueAsBoolean(iPartsConst.FIELD_DM_FILTER_RELEVANT));

        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggTypeId\":\"M\",\"productClassIds\":[\"P\"],\"fin\":\"WDF44770313606192\",\"datacardExists\":true,\"modelId\":\"D651950\",\"productId\":\"D80\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"54\"},{\"type\":\"cg_subgroup\",\"id\":\"120\"}]}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartsDAIMLER9715.txt"));
    }

    public void testGetPartsForDAIMLER7506() {
        // Test für Verdichtung bei Ersetzungen:
        // Auf Pos 100 sind 3 Stücklisteneinträge die nicht verdichtet werden dürfen, weil sie sich in ihren
        // Ersetzungen unterscheiden
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"fin\":\"XDN9096321B126337\",\"datacardExists\":true,\"modelId\":\"C909632\",\"productId\":\"K11\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"33\"},{\"type\":\"cg_subgroup\",\"id\":\"010\"}]}",
                          DWFile.get(getTestWorkingDir(), "resultPartsForFilterDAIMLER7506.txt"));
    }

    public void testGetPartsForDAIMLER9716() {
        // QSL Teilenummern werden ohne das SL ausgegeben
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C212002\",\"productId\":\"D01\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"21\"},{\"type\":\"cg_subgroup\",\"id\":\"230\"}]}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartsDAIMLER9716.txt"));
    }

    public void testGetPartInfoForFilterDAIMLER7187Part1() {
        // Teil wurde vor BM-Scoring Änderung verdrängt, jetzt nicht mehr
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"partContext\":{\"moduleId\":\"D61_82_062_00001\",\"sequenceId\":\"00021\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C166004\",\"productId\":\"D61\",\"fin\":\"WDC1660041A904156\",\"datacardExists\":true}," +
                          "\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"partInfo\":{\"codeValidityDetails\":[{\"code\":\"228\",\"dateFrom\":\"2006-11-03\",\"name\":\"ZUSATZHEIZUNG\"},{\"code\":\"430\",\"dateFrom\":\"2006-11-17\",\"name\":\"OFFROAD-PAKET\"},{\"code\":\"489\",\"dateFrom\":\"2006-11-03\",\"name\":\"AIRMATIC DC / LUFTFEDERUNG SEMIAKTIV\"},{\"code\":\"531\",\"dateFrom\":\"2011-09-06\",\"name\":\"COMAND APS NTG5/NTG5.5\"},{\"code\":\"800\",\"dateFrom\":\"1995-10-02\",\"name\":\"MODELLJAHRWECHSEL\"},{\"code\":\"806\",\"dateFrom\":\"2008-10-15\",\"name\":\"AEJ 15/1\"},{\"code\":\"807\",\"dateFrom\":\"2010-12-03\",\"name\":\"AEJ 16/1\"},{\"code\":\"808\",\"dateFrom\":\"2013-12-10\",\"name\":\"AEJ 17/1\"},{\"code\":\"809\",\"dateFrom\":\"2014-08-20\",\"name\":\"AEJ 18/1\"},{\"code\":\"M007\",\"dateFrom\":\"2007-11-16\",\"name\":\"FAHRZEUGE MIT 4X2 ANTRIEB\"},{\"code\":\"M157\",\"dateFrom\":\"2010-05-12\",\"name\":\"V8-OTTOMOTOR M157 - AMG\"}],\"codeValidityMatrix\":[[{\"code\":\"430\",\"negative\":true},{\"code\":\"489\"},{\"code\":\"531\"},{\"code\":\"806\"},{\"code\":\"M007\",\"negative\":true},{\"code\":\"M157\",\"negative\":true}],[{\"code\":\"228\"},{\"code\":\"430\",\"negative\":true},{\"code\":\"489\"},{\"code\":\"531\"},{\"code\":\"806\"},{\"code\":\"M007\",\"negative\":true},{\"code\":\"M157\",\"negative\":true}],[{\"code\":\"430\",\"negative\":true},{\"code\":\"489\"},{\"code\":\"531\"},{\"code\":\"807\"},{\"code\":\"M007\",\"negative\":true},{\"code\":\"M157\",\"negative\":true}],[{\"code\":\"228\"},{\"code\":\"430\",\"negative\":true},{\"code\":\"489\"},{\"code\":\"531\"},{\"code\":\"807\"},{\"code\":\"M007\",\"negative\":true},{\"code\":\"M157\",\"negative\":true}],[{\"code\":\"430\",\"negative\":true},{\"code\":\"489\"},{\"code\":\"531\"},{\"code\":\"808\"},{\"code\":\"M007\",\"negative\":true},{\"code\":\"M157\",\"negative\":true}],[{\"code\":\"228\"},{\"code\":\"430\",\"negative\":true},{\"code\":\"489\"},{\"code\":\"531\"},{\"code\":\"808\"},{\"code\":\"M007\",\"negative\":true},{\"code\":\"M157\",\"negative\":true}],[{\"code\":\"430\",\"negative\":true},{\"code\":\"489\"},{\"code\":\"531\"},{\"code\":\"809\"},{\"code\":\"M007\",\"negative\":true},{\"code\":\"M157\",\"negative\":true}],[{\"code\":\"228\"},{\"code\":\"430\",\"negative\":true},{\"code\":\"489\"},{\"code\":\"531\"},{\"code\":\"809\"},{\"code\":\"M007\",\"negative\":true},{\"code\":\"M157\",\"negative\":true}],[{\"code\":\"430\",\"negative\":true},{\"code\":\"489\"},{\"code\":\"531\"},{\"code\":\"800\"},{\"code\":\"M007\",\"negative\":true},{\"code\":\"M157\",\"negative\":true}],[{\"code\":\"228\"},{\"code\":\"430\",\"negative\":true},{\"code\":\"489\"},{\"code\":\"531\"},{\"code\":\"800\"},{\"code\":\"M007\",\"negative\":true},{\"code\":\"M157\",\"negative\":true}]]}}");
    }

    public void testGetPartInfoForFilterDAIMLER7187Part2() {
        // Wurde bisher fälschlicherweise von einer anderen Teileposition verdängt
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"partContext\":{\"moduleId\":\"69N_32_054_00001\",\"sequenceId\":\"00056\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C203081\",\"productId\":\"69N\",\"fin\":\"WDB2030811F713884\",\"datacardExists\":true}," +
                          "\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"partInfo\":{\"codeValidityDetails\":[{\"code\":\"494\",\"dateFrom\":\"1998-03-02\",\"name\":\"USA-AUSFUEHRUNG\"},{\"code\":\"M005\",\"dateFrom\":\"1999-09-03\",\"name\":\"FAHRZEUGE MIT 4-MATIC-/ALLRAD-ANTRIEB\"}],\"codeValidityMatrix\":[[{\"code\":\"494\"},{\"code\":\"M005\"}]]}}");
    }

    public void testGetPartInfoForFilterDAIMLER7187Part3() {
        // Wurde bisher fälschlicherweise von einer anderen Teileposition verdängt
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"partContext\":{\"moduleId\":\"69N_32_054_00001\",\"sequenceId\":\"00059\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C203081\",\"productId\":\"69N\",\"fin\":\"WDB2030811F713884\",\"datacardExists\":true}," +
                          "\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"partInfo\":{\"codeValidityDetails\":[{\"code\":\"494\",\"dateFrom\":\"1998-03-02\",\"name\":\"USA-AUSFUEHRUNG\"},{\"code\":\"M005\",\"dateFrom\":\"1999-09-03\",\"name\":\"FAHRZEUGE MIT 4-MATIC-/ALLRAD-ANTRIEB\"}],\"codeValidityMatrix\":[[{\"code\":\"494\"},{\"code\":\"M005\"}]]}}");
    }

    public void testGetPartInfoForFilterDAIMLER7187Part4() {
        // Eine der Teilepositionen die vorher fälschlicherweise angezeigt wurden
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"partContext\":{\"moduleId\":\"69N_32_054_00001\",\"sequenceId\":\"00054\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C203081\",\"productId\":\"69N\",\"fin\":\"WDB2030811F713884\",\"datacardExists\":true}," +
                          "\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"code\":4001,\"message\":\"Path 'partContext': SequenceId '00054' is invalid in module '69N_32_054_00001' for current context and filter options\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testGetPartInfoForFilterDAIMLER7187Part5() {
        // Eine der Teilepositionen die vorher fälschlicherweise angezeigt wurden
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"partContext\":{\"moduleId\":\"69N_32_054_00001\",\"sequenceId\":\"00057\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C203081\",\"productId\":\"69N\",\"fin\":\"WDB2030811F713884\",\"datacardExists\":true}," +
                          "\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"code\":4001,\"message\":\"Path 'partContext': SequenceId '00057' is invalid in module '69N_32_054_00001' for current context and filter options\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testDIALOGVehicleELDASAggregateDAIMLER7491() {
        // D78 ist ein DIALOG Produkt, 55L ein ELDAS Produkt. Bei der Anfrage, werden die ELDAS spezifischen Filter nicht deaktiviert
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"iparts_content_user\",\"language\":\"de\",\"country\":\"200\"},\"identContext\":{\"fin\":\"WDD2052421F257914\",\"productClassIds\":[\"P\",\"T\"],\"aggTypeId\":\"M\",\"modelId\":\"D274920\",\"datacardExists\":true,\"productId\":\"D78\"},\"navContext\":[{\"id\":\"20\",\"type\":\"cg_group\"},{\"id\":\"015\",\"type\":\"cg_subgroup\"}]}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartsDIALOGVehicleELDASAggregat.txt"));
    }

    public void testImageOnlyValidWithFIN() {
        // An C22_92_010 hängt ein gültiges Bild, dass nur angezeigt wird, wenn eine Datenkarte geladen wurde.
        // bei dataCardExists = true wird die Datenkarte WDD2052421F257914 geladen
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"iparts_content_user\",\"language\":\"de\",\"country\":\"200\"},\"identContext\":{\"fin\":\"WDD2052421F257914\",\"productClassIds\":[\"P\",\"T\"],\"aggTypeId\":\"F\",\"modelId\":\"C205242\",\"datacardExists\":true,\"productId\":\"C22\"},\"navContext\":[{\"id\":\"92\",\"type\":\"cg_group\"},{\"id\":\"010\",\"type\":\"cg_subgroup\"}]}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartsImageOnlyValidWithFIN.txt"));
    }

    public void testImageNotValidWithModel() {
        // An C22_92_010 hängt ein gültiges Bild, dass nur angezeigt wird, wenn eine Datenkarte geladen wurde.
        // bei dataCardExists = false wird die Datenkarte WDD2052421F257914 nicht geladen und es wird nur mit dem BM
        // C205242 gefiltert -> Das Bild wird ausgefiltert
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"iparts_content_user\",\"language\":\"de\",\"country\":\"200\"},\"identContext\":{\"fin\":\"WDD2052421F257914\",\"productClassIds\":[\"P\",\"T\"],\"aggTypeId\":\"F\",\"modelId\":\"C205242\",\"datacardExists\":false,\"productId\":\"C22\"},\"navContext\":[{\"id\":\"92\",\"type\":\"cg_group\"},{\"id\":\"010\",\"type\":\"cg_subgroup\"}]}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartsNotValidWithModel.txt"));
    }

    public void testGetPartsForFilterDAIMLER7360() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C217379\",\"productId\":\"D13\",\"fin\":\"WDD2173791A030477\",\"datacardExists\":true}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"33\"}, {\"type\":\"cg_subgroup\",\"id\":\"030\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultPartsForFilterDAIMLER7360.txt"));
    }

    public void testGetPartInfoForModelScoringDAIMLER7365() {
        // Filterung mit Baumuster C205003
        // Die Position wurde bisher gefiltert. Nun wird die AA nicht als bm-bildender Code verarbeitet. Somit hat die
        // Position gar keine bm-bildenden Code. Und weil nun alle Positionen in der Zeitscheibe der ausgewählten
        // Position keine bmb-Code haben, sind sie alle gültig (Baumusterfilter).
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"partContext\":{\"moduleId\":\"C01_91_225_00001\",\"sequenceId\":\"00012\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205003\",\"productId\":\"C01\"}," +
                          "\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartInfoForModelScoringDAIMLER7365.txt"));
    }

    public void testGetPartsForFilterDAIMLER7493() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"M\",\"productClassIds\":[\"P\"],\"modelId\":\"D651924\",\"productId\":\"69L\",\"fin\":\"WDD2122031A297563\",\"datacardExists\":true}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"15\"}, {\"type\":\"cg_subgroup\",\"id\":\"105\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultPartsForFilterDAIMLER7493.txt"));
    }

    public void testGetPartsTSAsDAIMLER7893() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"K\"],\"modelId\":\"C440167\",\"productId\":\"34K\",\"fin\":\"WDB44016700091426\",\"datacardExists\":true}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"25\"},{\"type\":\"sa_number\",\"id\":\"Z T35.814\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartsDAIMLER7893.txt"));
    }

    public void testGetPartInfoForColorFilterDAIMLER8299() {
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"partContext\":{\"moduleId\":\"D42_40_015_00001\",\"sequenceId\":\"00232\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C463273\",\"productId\":\"D42\",\"fin\":\"WDC4632731X213268\",\"datacardExists\":true}," +
                          "\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartInfoForColorFilterDAIMLER8299.txt"));
    }

    public void testIdentForInvisibleModelDAIMLER8341() {
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"C212006\"} ",
                                                      "{\"identContexts\":[]}");
    }

    public void testGetPartsInvalidModelTimesliceDAIMLER8434() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C203052\",\"productId\":\"69N\",\"fin\":\"WDC2030522R245536\",\"datacardExists\":true}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"88\"},{\"type\":\"cg_subgroup\",\"id\":\"030\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultPartsForFilterDAIMLER8434.txt"));
    }

    public void testIdentForInvisibleProductDAIMLER8684() {
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"C463236\"} ",
                                                      "{\"identContexts\":[{\"aggTypeId\":\"F\",\"modelId\":\"C463236\",\"modelTypeId\":\"C463\",\"productClassIds\":[\"G\"],\"productClassNames\":[\"Cross-country vehicle\"],\"productId\":\"62U\",\"productRemarks\":\"As of ident. no. 144226\"}]}");
    }

    public void testGetPartsOmittedPartForDIALOGiPartsDAIMLER8726() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C253303\",\"productId\":\"C253_FC\",\"fin\":\"WDC2533031F000001\",\"datacardExists\":true}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"32\"},{\"type\":\"cg_subgroup\",\"id\":\"060\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultPartsForFilterDAIMLER8726.txt"));
    }

    public void testGetPartsAdditionalModelBuildingCodeDAIMLER9274Part1() {
        // Stücklisteneintrag muss ausgefiltert werden durch den zusätzlichen BM-bildenden Code M177 für C223 FW
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"O\"],\"modelId\":\"C223000\",\"productId\":\"Test_DAIMLER-9274\"}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"08\"},{\"type\":\"cg_subgroup\",\"id\":\"010\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"images\":[],\"parts\":[]}");

    }

    public void testGetPartsAdditionalModelBuildingCodeDAIMLER9274Part2() {
        // Stücklisteneintrag muss ausgegeben werden, da das Baumuster C223076 den BM-bildenden Code M176 enthält
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"O\"],\"modelId\":\"C223076\",\"productId\":\"Test_DAIMLER-9274\"}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"08\"},{\"type\":\"cg_subgroup\",\"id\":\"010\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"images\":[],\"parts\":[{\"codeValidity\":\"M176/M177;\",\"level\":\"02\",\"name\":\"\",\"partContext\":{\"moduleId\":\"Test_DAIMLER-9274_08_010_00001\",\"sequenceId\":\"00001\"},\"partNo\":\"A0009070000\",\"partNoFormatted\":\"A 000 907 00 00\",\"quantity\":\"NB\"}]}");
    }

    public void testGetPartsStarPartsDAIMLER9412Part1() {
        // StarPart muss ausgegeben werden für Land DE
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C212095\",\"productId\":\"D05\"}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"82\"},{\"type\":\"cg_subgroup\",\"id\":\"120\"}],\"user\":{\"country\":\"DE\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartsStarPartsDAIMLER9412DE.txt"));
    }

    public void testGetPartsStarPartsDAIMLER9412Part2() {
        // StarPart darf nicht ausgegeben werden für Land HU (weil kein Eintrag für StarPart-Teil dieser Baureihe für FR)
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C212095\",\"productId\":\"D05\"}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"82\"},{\"type\":\"cg_subgroup\",\"id\":\"120\"}],\"user\":{\"country\":\"HU\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartsStarPartsDAIMLER9412HU.txt"));
    }

    public void testGetPartsStarPartsDAIMLER9412Part3() {
        // StarPart darf nicht ausgegeben werden für Land FR (weil StarPart-Teil ungültig für FR)
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C212095\",\"productId\":\"D05\"}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"82\"},{\"type\":\"cg_subgroup\",\"id\":\"120\"}],\"user\":{\"country\":\"FR\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartsStarPartsDAIMLER9412FR.txt"));
    }

    public void testGetPartsStarPartsDAIMLER9412Part4() {
        // StarPart darf nicht ausgegeben werden für ein fehlendes Land
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C212095\",\"productId\":\"D05\"}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"82\"},{\"type\":\"cg_subgroup\",\"id\":\"120\"}],\"user\":{\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartsStarPartsDAIMLER9412noCountry.txt"));
    }

    public void testGetPartsForSvgDAIMLER12311() {
        iPartsProductId productId = new iPartsProductId("C253_FC");
        iPartsDataProduct dataProduct = new iPartsDataProduct(getProject(), productId);
        if (!dataProduct.existsInDB()) {
            fail("Product \"C253_FC\" does not exist in the DB");
        }

        boolean oldUseSVGs = dataProduct.getFieldValueAsBoolean(iPartsConst.FIELD_DP_USE_SVGS);
        try {
            // SVG-Flag deaktivieren am Produkt C253_FC
            dataProduct.setFieldValueAsBoolean(iPartsConst.FIELD_DP_USE_SVGS, false, DBActionOrigin.FROM_EDIT);
            dataProduct.saveToDB(false, DBDataObject.PrimaryKeyExistsInDB.TRUE);
            iPartsProduct.removeProductFromCache(getProject(), productId);
            clearWebservicePluginsCaches(); // Caches müssen gelöscht werden, damit die Admin-Änderung auch auf jeden Fall berücksichtigt wird
            executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C253303\",\"productId\":\"C253_FC\"}," +
                              "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"21\"}, {\"type\":\"cg_subgroup\",\"id\":\"050\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                              DWFile.get(getTestWorkingDir(), "resultPartsForSvgDAIMLER12311PNG.txt"));

            // SVG-Flag aktivieren am Produkt C253_FC
            dataProduct.setFieldValueAsBoolean(iPartsConst.FIELD_DP_USE_SVGS, true, DBActionOrigin.FROM_EDIT);
            dataProduct.saveToDB(false, DBDataObject.PrimaryKeyExistsInDB.TRUE);
            iPartsProduct.removeProductFromCache(getProject(), productId);
            clearWebservicePluginsCaches(); // Caches müssen gelöscht werden, damit die Admin-Änderung auch auf jeden Fall berücksichtigt wird
            executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C253303\",\"productId\":\"C253_FC\"}," +
                              "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"21\"}, {\"type\":\"cg_subgroup\",\"id\":\"050\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                              DWFile.get(getTestWorkingDir(), "resultPartsForSvgDAIMLER12311SVG.txt"));
        } finally {
            // SVG-Flag auf den ursprünglichen Wert setzen am Produkt C253_FC
            dataProduct.setFieldValueAsBoolean(iPartsConst.FIELD_DP_USE_SVGS, oldUseSVGs, DBActionOrigin.FROM_EDIT);
            dataProduct.saveToDB(false, DBDataObject.PrimaryKeyExistsInDB.TRUE);
            clearWebservicePluginsCaches(); // Caches müssen gelöscht werden, damit die Admin-Änderung auch auf jeden Fall berücksichtigt wird
        }
    }

    public void testGetPartsAdditionalPartInformationDAIMLER12393() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}," +
                          "\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205036\",\"productId\":\"C01\",\"filterOptions\":{}}," +
                          "\"navContext\":[{\"id\":\"68\",\"type\":\"cg_group\"},{\"id\":\"193\",\"type\":\"cg_subgroup\"}]}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartsAdditionalPartInformationDAIMLER12393.txt"));
    }

    public void testGetPartInfoStarPartsDAIMLER9412Part1() {
        // StarPart muss ausgegeben werden für Land DE
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C212095\",\"productId\":\"D05\"}," +
                          "\"partContext\":{\"moduleId\":\"D05_82_120_00001\",\"sequenceId\":\"00018\"},\"user\":{\"country\":\"DE\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartInfoStarPartsDAIMLER9412DE.txt"));
    }

    public void testGetPartInfoStarPartsDAIMLER9412Part2() {
        // StarPart darf nicht ausgegeben werden für Land HU (weil kein Eintrag für StarPart-Teil dieser Baureihe für FR)
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C212095\",\"productId\":\"D05\"}," +
                          "\"partContext\":{\"moduleId\":\"D05_82_120_00001\",\"sequenceId\":\"00018\"},\"user\":{\"country\":\"HU\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartInfoStarPartsDAIMLER9412HU.txt"));
    }

    public void testGetPartInfoStarPartsDAIMLER9412Part3() {
        // StarPart darf nicht ausgegeben werden für Land FR (weil StarPart-Teil ungültig für FR)
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C212095\",\"productId\":\"D05\"}," +
                          "\"partContext\":{\"moduleId\":\"D05_82_120_00001\",\"sequenceId\":\"00018\"},\"user\":{\"country\":\"FR\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartInfoStarPartsDAIMLER9412FR.txt"));
    }

    public void testGetPartInfoStarPartsDAIMLER9412Part4() {
        // StarPart darf nicht ausgegeben werden für ein fehlendes Land
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C212095\",\"productId\":\"D05\"}," +
                          "\"partContext\":{\"moduleId\":\"D05_82_120_00001\",\"sequenceId\":\"00018\"},\"user\":{\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartInfoStarPartsDAIMLER9412noCountry.txt"));
    }

    public void testGetPartInfoEinPasDAIMLER11967() {
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C166824\",\"productId\":\"D81\",\"filterOptions\":{}},\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00162\"}}",
                          "{\"partInfo\":{\"codeValidityDetails\":[{\"code\":\"610\",\"dateFrom\":\"2007-04-02\",\"name\":\"NACHTSICHTSYSTEM\"},{\"code\":\"804\",\"dateFrom\":\"2008-10-15\",\"name\":\"AEJ 13/1\"}],\"codeValidityMatrix\":[[{\"code\":\"610\"},{\"code\":\"804\"}]],\"einPAS\":[{\"g\":\"15\",\"hg\":\"30\",\"tu\":\"12\",\"version\":\"001\"},{\"g\":\"30\",\"hg\":\"36\",\"tu\":\"12\",\"version\":\"006\"},{\"g\":\"30\",\"hg\":\"36\",\"tu\":\"12\",\"version\":\"005\"},{\"g\":\"30\",\"hg\":\"36\",\"tu\":\"12\",\"version\":\"003\"}],\"plantInformation\":[{\"date\":\"2013-05-03\",\"plant\":\"A, B\",\"type\":\"bis\"}]}}");
    }

    public void testGetNavOptsForProductWithDataCardDAIMLER11650() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C967027\",\"productId\":\"S10\",\"fin\":\"WDB9670271L896656\",\"datacardExists\":true}," +
                          "\"user\":{\"country\":\"DE\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"nextNodes\":[{\"id\":\"24\",\"label\":\"MOTORAUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"25\",\"label\":\"KUPPLUNG\",\"type\":\"cg_group\"},{\"id\":\"26\",\"label\":\"GETRIEBE\",\"type\":\"cg_group\"},{\"id\":\"31\",\"label\":\"RAHMEN\",\"type\":\"cg_group\"},{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"40\",\"label\":\"RAEDER\",\"type\":\"cg_group\"},{\"id\":\"41\",\"label\":\"GELENKWELLE\",\"type\":\"cg_group\"},{\"id\":\"42\",\"label\":\"BREMSANLAGE\",\"type\":\"cg_group\"},{\"id\":\"46\",\"label\":\"LENKUNG\",\"type\":\"cg_group\"},{\"id\":\"47\",\"label\":\"KRAFTSTOFFANLAGE\",\"type\":\"cg_group\"},{\"id\":\"49\",\"label\":\"AUSPUFFANLAGE\",\"type\":\"cg_group\"},{\"id\":\"50\",\"label\":\"KUEHLER\",\"type\":\"cg_group\"},{\"id\":\"52\",\"label\":\"BLECHTEILE,LUFTANSAUGUNG\",\"type\":\"cg_group\"},{\"id\":\"54\",\"label\":\"ELEKTR. AUSRUESTUNG UND INSTRUMENTE\",\"type\":\"cg_group\"},{\"id\":\"55\",\"label\":\"HYDRAULIKANLAGE\",\"type\":\"cg_group\"},{\"id\":\"58\",\"label\":\"WERKZEUG UND ZUBEHOER\",\"type\":\"cg_group\"},{\"id\":\"60\",\"label\":\"PRITSCHE\",\"type\":\"cg_group\"},{\"id\":\"66\",\"label\":\"EINBAUTEN\",\"type\":\"cg_group\"},{\"id\":\"82\",\"label\":\"ELEKTRISCHE ANLAGE\",\"type\":\"cg_group\"},{\"id\":\"88\",\"label\":\"ANBAUTEILE\",\"type\":\"cg_group\"},{\"id\":\"98\",\"label\":\"ZUBEHOER\",\"type\":\"cg_group\"},{\"id\":\"99\",\"label\":\"NACHTRAEGLICHER EINBAU\",\"type\":\"cg_group\"}]}");
    }

    public void testGetNavOptsForProductWithoutDataCardDAIMLER11650() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C967027\",\"productId\":\"S10\",\"fin\":\"WDB9670271L896656\",\"datacardExists\":false}," +
                          "\"user\":{\"country\":\"DE\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"nextNodes\":[{\"id\":\"24\",\"label\":\"MOTORAUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"25\",\"label\":\"KUPPLUNG\",\"type\":\"cg_group\"},{\"id\":\"26\",\"label\":\"GETRIEBE\",\"type\":\"cg_group\"},{\"id\":\"28\",\"label\":\"VERTEILERGETRIEBE\",\"type\":\"cg_group\"},{\"id\":\"31\",\"label\":\"RAHMEN\",\"type\":\"cg_group\"},{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"40\",\"label\":\"RAEDER\",\"type\":\"cg_group\"},{\"id\":\"41\",\"label\":\"GELENKWELLE\",\"type\":\"cg_group\"},{\"id\":\"42\",\"label\":\"BREMSANLAGE\",\"type\":\"cg_group\"},{\"id\":\"46\",\"label\":\"LENKUNG\",\"type\":\"cg_group\"},{\"id\":\"47\",\"label\":\"KRAFTSTOFFANLAGE\",\"type\":\"cg_group\"},{\"id\":\"49\",\"label\":\"AUSPUFFANLAGE\",\"type\":\"cg_group\"},{\"id\":\"50\",\"label\":\"KUEHLER\",\"type\":\"cg_group\"},{\"id\":\"52\",\"label\":\"BLECHTEILE,LUFTANSAUGUNG\",\"type\":\"cg_group\"},{\"id\":\"54\",\"label\":\"ELEKTR. AUSRUESTUNG UND INSTRUMENTE\",\"type\":\"cg_group\"},{\"id\":\"55\",\"label\":\"HYDRAULIKANLAGE\",\"type\":\"cg_group\"},{\"id\":\"58\",\"label\":\"WERKZEUG UND ZUBEHOER\",\"type\":\"cg_group\"},{\"id\":\"60\",\"label\":\"PRITSCHE\",\"type\":\"cg_group\"},{\"id\":\"66\",\"label\":\"EINBAUTEN\",\"type\":\"cg_group\"},{\"id\":\"82\",\"label\":\"ELEKTRISCHE ANLAGE\",\"type\":\"cg_group\"},{\"id\":\"86\",\"label\":\"SCHEINWERFER-REINIGUNGSANLAGE\",\"type\":\"cg_group\"},{\"id\":\"88\",\"label\":\"ANBAUTEILE\",\"type\":\"cg_group\"},{\"id\":\"98\",\"label\":\"ZUBEHOER\",\"type\":\"cg_group\"},{\"id\":\"99\",\"label\":\"NACHTRAEGLICHER EINBAU\",\"type\":\"cg_group\"}]}");
    }

    public void testGetNavOptsForKGWithDataCardDAIMLER11650() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C967027\",\"productId\":\"S10\",\"fin\":\"WDB9670271L896656\",\"datacardExists\":true}," +
                          "\"user\":{\"country\":\"DE\",\"language\":\"de\",\"userId\":\"userId\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"26\"}]}",
                          "{\"nextNodes\":[{\"id\":\"300\",\"label\":\"MECHANISCHE SCHALTUNG\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"},{\"id\":\"Z 504.636\",\"label\":\"SV GEBERGERAET KABELZUG SCHALTUNG\",\"partsAvailable\":true,\"saaNavNodeInfos\":[{\"code\":\"Z 504.636/61\",\"description\":\"KABELZUGSCHALTUNG\"},{\"code\":\"Z 504.636/67\",\"description\":\"MIT MANSCHETTE\"},{\"code\":\"Z 504.636/70\",\"description\":\"9-GANG\"}],\"type\":\"sa_number\"}]}");
    }

    public void testGetNavOptsForKGWithoutDataCardDAIMLER11650() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C967027\",\"productId\":\"S10\",\"fin\":\"WDB9670271L896656\",\"datacardExists\":false}," +
                          "\"user\":{\"country\":\"DE\",\"language\":\"de\",\"userId\":\"userId\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"26\"}]}",
                          "{\"nextNodes\":[{\"id\":\"300\",\"label\":\"MECHANISCHE SCHALTUNG\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"},{\"id\":\"350\",\"label\":\"KIPPERPUMPE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}]}");
    }

    public void testGetNavOptsForKGWithModelValidityDAIMLER12141() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\n\"identContext\":{\n\"aggTypeId\":\"M\",\n\"modelId\":\"D651921\",\n\"productClassIds\":[\n\"P\"\n],\n\"productId\":\"69L\"\n},\n\"user\":{\n\"country\":\"200\",\n\"language\":\"de\",\n\"userId\":\"TRKA_tec_00\"\n}\n}",
                          "{\"nextNodes\":[{\"id\":\"01\",\"label\":\"MOTORGEHAEUSE\",\"type\":\"cg_group\"},{\"id\":\"03\",\"label\":\"TRIEBWERKTEILE\",\"type\":\"cg_group\"},{\"id\":\"05\",\"label\":\"MOTORSTEUERUNG\",\"type\":\"cg_group\"},{\"id\":\"07\",\"label\":\"KRAFTSTOFFEINSPRITZUNG\",\"type\":\"cg_group\"},{\"id\":\"09\",\"label\":\"LUFTFILTER UND MOTORAUFLADUNG\",\"type\":\"cg_group\"},{\"id\":\"13\",\"label\":\"LENKHELFPUMPE, KAELTEKOMPRESSOR UND\",\"type\":\"cg_group\"},{\"id\":\"14\",\"label\":\"SAUGROHR UND AUSPUFFKRUEMMER\",\"type\":\"cg_group\"},{\"id\":\"18\",\"label\":\"MOTORSCHMIERUNG\",\"type\":\"cg_group\"},{\"id\":\"20\",\"label\":\"MOTORKUEHLUNG\",\"type\":\"cg_group\"},{\"id\":\"22\",\"label\":\"MOTORAUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"54\",\"label\":\"MOTORLEITUNGSSAETZE\",\"type\":\"cg_group\"}]}");
    }

    public void testGetNavOptsForKGWithDataCardAndCodeDAIMLER11955() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205004\",\"productId\":\"C01\",\"fin\":\"WDD2050042R042987\",\"datacardExists\":true}," +
                          "\"user\":{\"country\":\"DE\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"nextNodes\":[{\"id\":\"24\",\"label\":\"MOTORAUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"25\",\"label\":\"KUPPLUNG\",\"type\":\"cg_group\"},{\"id\":\"26\",\"label\":\"SCHALTUNG\",\"type\":\"cg_group\"},{\"id\":\"27\",\"label\":\"AUTOMATISCHES MB-GETRIEBE\",\"type\":\"cg_group\"},{\"id\":\"29\",\"label\":\"PEDALANLAGE\",\"type\":\"cg_group\"},{\"id\":\"30\",\"label\":\"REGULIERUNG\",\"type\":\"cg_group\"},{\"id\":\"31\",\"label\":\"ANHAENGEVORRICHTUNG\",\"type\":\"cg_group\"},{\"id\":\"32\",\"label\":\"FEDERN,AUFHAENGUNG UND HYDRAULIK\",\"type\":\"cg_group\"},{\"id\":\"33\",\"label\":\"VORDERACHSE\",\"type\":\"cg_group\"},{\"id\":\"35\",\"label\":\"HINTERACHSE\",\"type\":\"cg_group\"},{\"id\":\"40\",\"label\":\"RAEDER\",\"type\":\"cg_group\"},{\"id\":\"41\",\"label\":\"GELENKWELLE\",\"type\":\"cg_group\"},{\"id\":\"42\",\"label\":\"BREMSANLAGE\",\"type\":\"cg_group\"},{\"id\":\"46\",\"label\":\"LENKUNG\",\"type\":\"cg_group\"},{\"id\":\"47\",\"label\":\"KRAFTSTOFFANLAGE\",\"type\":\"cg_group\"},{\"id\":\"49\",\"label\":\"AUSPUFFANLAGE\",\"type\":\"cg_group\"},{\"id\":\"50\",\"label\":\"KUEHLER\",\"type\":\"cg_group\"},{\"id\":\"52\",\"label\":\"FAHRGESTELLBLECHTEILE / LUFTANSAUGUNG\",\"type\":\"cg_group\"},{\"id\":\"54\",\"label\":\"ELEKTRISCHE AUSRUESTUNG UND INSTRUMENTE\",\"type\":\"cg_group\"},{\"id\":\"58\",\"label\":\"WERKZEUG UND ZUBEHOER\",\"type\":\"cg_group\"},{\"id\":\"60\",\"label\":\"ROHBAU\",\"type\":\"cg_group\"},{\"id\":\"61\",\"label\":\"UNTERBAU\",\"type\":\"cg_group\"},{\"id\":\"62\",\"label\":\"VORBAU,VORDERWAND\",\"type\":\"cg_group\"},{\"id\":\"63\",\"label\":\"SEITENWAENDE\",\"type\":\"cg_group\"},{\"id\":\"64\",\"label\":\"HECK\",\"type\":\"cg_group\"},{\"id\":\"65\",\"label\":\"DACH\",\"type\":\"cg_group\"},{\"id\":\"67\",\"label\":\"FENSTERANLAGE\",\"type\":\"cg_group\"},{\"id\":\"68\",\"label\":\"VERKLEIDUNG\",\"type\":\"cg_group\"},{\"id\":\"69\",\"label\":\"VERKLEIDUNG UND AUSSCHLAG\",\"type\":\"cg_group\"},{\"id\":\"72\",\"label\":\"FAHRERTUEREN\",\"type\":\"cg_group\"},{\"id\":\"73\",\"label\":\"FONDTUEREN\",\"type\":\"cg_group\"},{\"id\":\"75\",\"label\":\"HECKDECKEL\",\"type\":\"cg_group\"},{\"id\":\"78\",\"label\":\"SCHIEBEDACH\",\"type\":\"cg_group\"},{\"id\":\"82\",\"label\":\"ELEKTRISCHE ANLAGE\",\"type\":\"cg_group\"},{\"id\":\"83\",\"label\":\"HEIZUNG UND LUEFTUNG\",\"type\":\"cg_group\"},{\"id\":\"86\",\"label\":\"WASCHANLAGE\",\"type\":\"cg_group\"},{\"id\":\"88\",\"label\":\"ANBAUTEILE\",\"type\":\"cg_group\"},{\"id\":\"91\",\"label\":\"FAHRERSITZ\",\"type\":\"cg_group\"},{\"id\":\"92\",\"label\":\"FONDSITZ\",\"type\":\"cg_group\"},{\"id\":\"98\",\"label\":\"ZUBEHOER\",\"type\":\"cg_group\"},{\"id\":\"99\",\"label\":\"SONDEREINBAUTEN\",\"type\":\"cg_group\"}]}");
    }

    public void testGetNavOptsForKGWithDataCardWithoutCodeDAIMLER11955() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205004\",\"productId\":\"C01\",\"fin\":\"WDD2050041F019438\",\"datacardExists\":true}," +
                          "\"user\":{\"country\":\"DE\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"nextNodes\":[{\"id\":\"25\",\"label\":\"KUPPLUNG\",\"type\":\"cg_group\"},{\"id\":\"26\",\"label\":\"SCHALTUNG\",\"type\":\"cg_group\"},{\"id\":\"27\",\"label\":\"AUTOMATISCHES MB-GETRIEBE\",\"type\":\"cg_group\"},{\"id\":\"29\",\"label\":\"PEDALANLAGE\",\"type\":\"cg_group\"},{\"id\":\"30\",\"label\":\"REGULIERUNG\",\"type\":\"cg_group\"},{\"id\":\"31\",\"label\":\"ANHAENGEVORRICHTUNG\",\"type\":\"cg_group\"},{\"id\":\"32\",\"label\":\"FEDERN,AUFHAENGUNG UND HYDRAULIK\",\"type\":\"cg_group\"},{\"id\":\"33\",\"label\":\"VORDERACHSE\",\"type\":\"cg_group\"},{\"id\":\"35\",\"label\":\"HINTERACHSE\",\"type\":\"cg_group\"},{\"id\":\"40\",\"label\":\"RAEDER\",\"type\":\"cg_group\"},{\"id\":\"41\",\"label\":\"GELENKWELLE\",\"type\":\"cg_group\"},{\"id\":\"42\",\"label\":\"BREMSANLAGE\",\"type\":\"cg_group\"},{\"id\":\"46\",\"label\":\"LENKUNG\",\"type\":\"cg_group\"},{\"id\":\"47\",\"label\":\"KRAFTSTOFFANLAGE\",\"type\":\"cg_group\"},{\"id\":\"49\",\"label\":\"AUSPUFFANLAGE\",\"type\":\"cg_group\"},{\"id\":\"50\",\"label\":\"KUEHLER\",\"type\":\"cg_group\"},{\"id\":\"52\",\"label\":\"FAHRGESTELLBLECHTEILE / LUFTANSAUGUNG\",\"type\":\"cg_group\"},{\"id\":\"54\",\"label\":\"ELEKTRISCHE AUSRUESTUNG UND INSTRUMENTE\",\"type\":\"cg_group\"},{\"id\":\"58\",\"label\":\"WERKZEUG UND ZUBEHOER\",\"type\":\"cg_group\"},{\"id\":\"60\",\"label\":\"ROHBAU\",\"type\":\"cg_group\"},{\"id\":\"61\",\"label\":\"UNTERBAU\",\"type\":\"cg_group\"},{\"id\":\"62\",\"label\":\"VORBAU,VORDERWAND\",\"type\":\"cg_group\"},{\"id\":\"63\",\"label\":\"SEITENWAENDE\",\"type\":\"cg_group\"},{\"id\":\"64\",\"label\":\"HECK\",\"type\":\"cg_group\"},{\"id\":\"65\",\"label\":\"DACH\",\"type\":\"cg_group\"},{\"id\":\"67\",\"label\":\"FENSTERANLAGE\",\"type\":\"cg_group\"},{\"id\":\"68\",\"label\":\"VERKLEIDUNG\",\"type\":\"cg_group\"},{\"id\":\"69\",\"label\":\"VERKLEIDUNG UND AUSSCHLAG\",\"type\":\"cg_group\"},{\"id\":\"72\",\"label\":\"FAHRERTUEREN\",\"type\":\"cg_group\"},{\"id\":\"73\",\"label\":\"FONDTUEREN\",\"type\":\"cg_group\"},{\"id\":\"75\",\"label\":\"HECKDECKEL\",\"type\":\"cg_group\"},{\"id\":\"78\",\"label\":\"SCHIEBEDACH\",\"type\":\"cg_group\"},{\"id\":\"82\",\"label\":\"ELEKTRISCHE ANLAGE\",\"type\":\"cg_group\"},{\"id\":\"83\",\"label\":\"HEIZUNG UND LUEFTUNG\",\"type\":\"cg_group\"},{\"id\":\"86\",\"label\":\"WASCHANLAGE\",\"type\":\"cg_group\"},{\"id\":\"88\",\"label\":\"ANBAUTEILE\",\"type\":\"cg_group\"},{\"id\":\"91\",\"label\":\"FAHRERSITZ\",\"type\":\"cg_group\"},{\"id\":\"92\",\"label\":\"FONDSITZ\",\"type\":\"cg_group\"},{\"id\":\"98\",\"label\":\"ZUBEHOER\",\"type\":\"cg_group\"},{\"id\":\"99\",\"label\":\"SONDEREINBAUTEN\",\"type\":\"cg_group\"}]}");
    }

    public void testGetNavOptsForKGWithDataCardWithC205005DAIMLER11955() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205005\",\"productId\":\"C01\",\"fin\":\"WDD2050051R367645\",\"datacardExists\":true}," +
                          "\"user\":{\"country\":\"DE\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"nextNodes\":[{\"id\":\"21\",\"label\":\"AGGREGATE-ANBAUTEILE\",\"notes\":[{\"text\":\"Notiz zu C01 21\"}],\"type\":\"cg_group\"},{\"id\":\"25\",\"label\":\"KUPPLUNG\",\"type\":\"cg_group\"},{\"id\":\"26\",\"label\":\"SCHALTUNG\",\"type\":\"cg_group\"},{\"id\":\"27\",\"label\":\"AUTOMATISCHES MB-GETRIEBE\",\"type\":\"cg_group\"},{\"id\":\"29\",\"label\":\"PEDALANLAGE\",\"type\":\"cg_group\"},{\"id\":\"30\",\"label\":\"REGULIERUNG\",\"type\":\"cg_group\"},{\"id\":\"31\",\"label\":\"ANHAENGEVORRICHTUNG\",\"type\":\"cg_group\"},{\"id\":\"32\",\"label\":\"FEDERN,AUFHAENGUNG UND HYDRAULIK\",\"type\":\"cg_group\"},{\"id\":\"33\",\"label\":\"VORDERACHSE\",\"type\":\"cg_group\"},{\"id\":\"35\",\"label\":\"HINTERACHSE\",\"type\":\"cg_group\"},{\"id\":\"40\",\"label\":\"RAEDER\",\"type\":\"cg_group\"},{\"id\":\"41\",\"label\":\"GELENKWELLE\",\"type\":\"cg_group\"},{\"id\":\"42\",\"label\":\"BREMSANLAGE\",\"type\":\"cg_group\"},{\"id\":\"46\",\"label\":\"LENKUNG\",\"type\":\"cg_group\"},{\"id\":\"47\",\"label\":\"KRAFTSTOFFANLAGE\",\"type\":\"cg_group\"},{\"id\":\"49\",\"label\":\"AUSPUFFANLAGE\",\"type\":\"cg_group\"},{\"id\":\"50\",\"label\":\"KUEHLER\",\"type\":\"cg_group\"},{\"id\":\"52\",\"label\":\"FAHRGESTELLBLECHTEILE / LUFTANSAUGUNG\",\"type\":\"cg_group\"},{\"id\":\"54\",\"label\":\"ELEKTRISCHE AUSRUESTUNG UND INSTRUMENTE\",\"type\":\"cg_group\"},{\"id\":\"58\",\"label\":\"WERKZEUG UND ZUBEHOER\",\"type\":\"cg_group\"},{\"id\":\"60\",\"label\":\"ROHBAU\",\"type\":\"cg_group\"},{\"id\":\"61\",\"label\":\"UNTERBAU\",\"type\":\"cg_group\"},{\"id\":\"62\",\"label\":\"VORBAU,VORDERWAND\",\"type\":\"cg_group\"},{\"id\":\"63\",\"label\":\"SEITENWAENDE\",\"type\":\"cg_group\"},{\"id\":\"64\",\"label\":\"HECK\",\"type\":\"cg_group\"},{\"id\":\"65\",\"label\":\"DACH\",\"type\":\"cg_group\"},{\"id\":\"67\",\"label\":\"FENSTERANLAGE\",\"type\":\"cg_group\"},{\"id\":\"68\",\"label\":\"VERKLEIDUNG\",\"type\":\"cg_group\"},{\"id\":\"69\",\"label\":\"VERKLEIDUNG UND AUSSCHLAG\",\"type\":\"cg_group\"},{\"id\":\"72\",\"label\":\"FAHRERTUEREN\",\"type\":\"cg_group\"},{\"id\":\"73\",\"label\":\"FONDTUEREN\",\"type\":\"cg_group\"},{\"id\":\"75\",\"label\":\"HECKDECKEL\",\"type\":\"cg_group\"},{\"id\":\"78\",\"label\":\"SCHIEBEDACH\",\"type\":\"cg_group\"},{\"id\":\"82\",\"label\":\"ELEKTRISCHE ANLAGE\",\"type\":\"cg_group\"},{\"id\":\"83\",\"label\":\"HEIZUNG UND LUEFTUNG\",\"type\":\"cg_group\"},{\"id\":\"86\",\"label\":\"WASCHANLAGE\",\"type\":\"cg_group\"},{\"id\":\"88\",\"label\":\"ANBAUTEILE\",\"type\":\"cg_group\"},{\"id\":\"91\",\"label\":\"FAHRERSITZ\",\"type\":\"cg_group\"},{\"id\":\"92\",\"label\":\"FONDSITZ\",\"type\":\"cg_group\"},{\"id\":\"98\",\"label\":\"ZUBEHOER\",\"type\":\"cg_group\"},{\"id\":\"99\",\"label\":\"SONDEREINBAUTEN\",\"type\":\"cg_group\"}]}");
    }

    public void testGetNavOptsForTUWithDataCardWithC205005DAIMLER11955() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205005\",\"productId\":\"C01\",\"fin\":\"WDD2050051R367645\",\"datacardExists\":true},\"navContext\":[{\"id\":\"21\",\"type\":\"cg_group\"}]," +
                          "\"user\":{\"country\":\"DE\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"nextNodes\":[{\"id\":\"300\",\"label\":\"ABSCHIRMUNGEN UND ABDAEMPFUNGEN AN MOTOR,GETRIEBE,VORDERACHSGETRIEBE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}]}");
    }

    public void testGetNavOptsForKGWithDataCardForEngineDAIMLER11955() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"M\",\"productClassIds\":[\"P\"],\"modelId\":\"D651921\",\"productId\":\"69L\",\"fin\":\"WDD2050041F019438\",\"datacardExists\":true}," +
                          "\"user\":{\"country\":\"DE\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"nextNodes\":[{\"id\":\"01\",\"label\":\"MOTORGEHAEUSE\",\"type\":\"cg_group\"},{\"id\":\"03\",\"label\":\"TRIEBWERKTEILE\",\"type\":\"cg_group\"},{\"id\":\"05\",\"label\":\"MOTORSTEUERUNG\",\"type\":\"cg_group\"},{\"id\":\"07\",\"label\":\"KRAFTSTOFFEINSPRITZUNG\",\"type\":\"cg_group\"},{\"id\":\"09\",\"label\":\"LUFTFILTER UND MOTORAUFLADUNG\",\"type\":\"cg_group\"},{\"id\":\"13\",\"label\":\"LENKHELFPUMPE, KAELTEKOMPRESSOR UND\",\"type\":\"cg_group\"},{\"id\":\"14\",\"label\":\"SAUGROHR UND AUSPUFFKRUEMMER\",\"type\":\"cg_group\"},{\"id\":\"18\",\"label\":\"MOTORSCHMIERUNG\",\"type\":\"cg_group\"},{\"id\":\"20\",\"label\":\"MOTORKUEHLUNG\",\"type\":\"cg_group\"},{\"id\":\"22\",\"label\":\"MOTORAUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"54\",\"label\":\"MOTORLEITUNGSSAETZE\",\"type\":\"cg_group\"}]}");
    }

    public void testGetNavOptsForKGWithModelValidityDAIMLER12305Part1() {
        // Fake-Datenkarte XXX9670271L896656 ohne SAAs und ohne Code
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C967607\",\"productId\":\"S10\",\"fin\":\"XXX9670271L896656\",\"datacardExists\":true}," +
                          "\"user\":{\"country\":\"DE\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"nextNodes\":[{\"id\":\"24\",\"label\":\"MOTORAUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"25\",\"label\":\"KUPPLUNG\",\"type\":\"cg_group\"},{\"id\":\"26\",\"label\":\"GETRIEBE\",\"type\":\"cg_group\"},{\"id\":\"28\",\"label\":\"VERTEILERGETRIEBE\",\"type\":\"cg_group\"},{\"id\":\"31\",\"label\":\"RAHMEN\",\"type\":\"cg_group\"},{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"40\",\"label\":\"RAEDER\",\"type\":\"cg_group\"},{\"id\":\"41\",\"label\":\"GELENKWELLE\",\"type\":\"cg_group\"},{\"id\":\"42\",\"label\":\"BREMSANLAGE\",\"type\":\"cg_group\"},{\"id\":\"46\",\"label\":\"LENKUNG\",\"type\":\"cg_group\"},{\"id\":\"47\",\"label\":\"KRAFTSTOFFANLAGE\",\"type\":\"cg_group\"},{\"id\":\"49\",\"label\":\"AUSPUFFANLAGE\",\"type\":\"cg_group\"},{\"id\":\"50\",\"label\":\"KUEHLER\",\"type\":\"cg_group\"},{\"id\":\"52\",\"label\":\"BLECHTEILE,LUFTANSAUGUNG\",\"type\":\"cg_group\"},{\"id\":\"54\",\"label\":\"ELEKTR. AUSRUESTUNG UND INSTRUMENTE\",\"type\":\"cg_group\"},{\"id\":\"55\",\"label\":\"HYDRAULIKANLAGE\",\"type\":\"cg_group\"},{\"id\":\"58\",\"label\":\"WERKZEUG UND ZUBEHOER\",\"type\":\"cg_group\"},{\"id\":\"60\",\"label\":\"PRITSCHE\",\"type\":\"cg_group\"},{\"id\":\"66\",\"label\":\"EINBAUTEN\",\"type\":\"cg_group\"},{\"id\":\"82\",\"label\":\"ELEKTRISCHE ANLAGE\",\"type\":\"cg_group\"},{\"id\":\"86\",\"label\":\"SCHEINWERFER-REINIGUNGSANLAGE\",\"type\":\"cg_group\"},{\"id\":\"88\",\"label\":\"ANBAUTEILE\",\"type\":\"cg_group\"},{\"id\":\"98\",\"label\":\"ZUBEHOER\",\"type\":\"cg_group\"},{\"id\":\"99\",\"label\":\"NACHTRAEGLICHER EINBAU\",\"type\":\"cg_group\"}]}");
    }

    public void testGetNavOptsForKGWithModelValidityDAIMLER12305Part2() {
        // Fake-Datenkarte XXX2050041F019438 ohne Code
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205004\",\"productId\":\"C01\",\"fin\":\"XXX2050041F019438\",\"datacardExists\":true}," +
                          "\"user\":{\"country\":\"DE\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"nextNodes\":[{\"id\":\"24\",\"label\":\"MOTORAUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"25\",\"label\":\"KUPPLUNG\",\"type\":\"cg_group\"},{\"id\":\"26\",\"label\":\"SCHALTUNG\",\"type\":\"cg_group\"},{\"id\":\"27\",\"label\":\"AUTOMATISCHES MB-GETRIEBE\",\"type\":\"cg_group\"},{\"id\":\"29\",\"label\":\"PEDALANLAGE\",\"type\":\"cg_group\"},{\"id\":\"30\",\"label\":\"REGULIERUNG\",\"type\":\"cg_group\"},{\"id\":\"31\",\"label\":\"ANHAENGEVORRICHTUNG\",\"type\":\"cg_group\"},{\"id\":\"32\",\"label\":\"FEDERN,AUFHAENGUNG UND HYDRAULIK\",\"type\":\"cg_group\"},{\"id\":\"33\",\"label\":\"VORDERACHSE\",\"type\":\"cg_group\"},{\"id\":\"35\",\"label\":\"HINTERACHSE\",\"type\":\"cg_group\"},{\"id\":\"40\",\"label\":\"RAEDER\",\"type\":\"cg_group\"},{\"id\":\"41\",\"label\":\"GELENKWELLE\",\"type\":\"cg_group\"},{\"id\":\"42\",\"label\":\"BREMSANLAGE\",\"type\":\"cg_group\"},{\"id\":\"46\",\"label\":\"LENKUNG\",\"type\":\"cg_group\"},{\"id\":\"47\",\"label\":\"KRAFTSTOFFANLAGE\",\"type\":\"cg_group\"},{\"id\":\"49\",\"label\":\"AUSPUFFANLAGE\",\"type\":\"cg_group\"},{\"id\":\"50\",\"label\":\"KUEHLER\",\"type\":\"cg_group\"},{\"id\":\"52\",\"label\":\"FAHRGESTELLBLECHTEILE / LUFTANSAUGUNG\",\"type\":\"cg_group\"},{\"id\":\"54\",\"label\":\"ELEKTRISCHE AUSRUESTUNG UND INSTRUMENTE\",\"type\":\"cg_group\"},{\"id\":\"58\",\"label\":\"WERKZEUG UND ZUBEHOER\",\"type\":\"cg_group\"},{\"id\":\"60\",\"label\":\"ROHBAU\",\"type\":\"cg_group\"},{\"id\":\"61\",\"label\":\"UNTERBAU\",\"type\":\"cg_group\"},{\"id\":\"62\",\"label\":\"VORBAU,VORDERWAND\",\"type\":\"cg_group\"},{\"id\":\"63\",\"label\":\"SEITENWAENDE\",\"type\":\"cg_group\"},{\"id\":\"64\",\"label\":\"HECK\",\"type\":\"cg_group\"},{\"id\":\"65\",\"label\":\"DACH\",\"type\":\"cg_group\"},{\"id\":\"67\",\"label\":\"FENSTERANLAGE\",\"type\":\"cg_group\"},{\"id\":\"68\",\"label\":\"VERKLEIDUNG\",\"type\":\"cg_group\"},{\"id\":\"69\",\"label\":\"VERKLEIDUNG UND AUSSCHLAG\",\"type\":\"cg_group\"},{\"id\":\"72\",\"label\":\"FAHRERTUEREN\",\"type\":\"cg_group\"},{\"id\":\"73\",\"label\":\"FONDTUEREN\",\"type\":\"cg_group\"},{\"id\":\"75\",\"label\":\"HECKDECKEL\",\"type\":\"cg_group\"},{\"id\":\"78\",\"label\":\"SCHIEBEDACH\",\"type\":\"cg_group\"},{\"id\":\"82\",\"label\":\"ELEKTRISCHE ANLAGE\",\"type\":\"cg_group\"},{\"id\":\"83\",\"label\":\"HEIZUNG UND LUEFTUNG\",\"type\":\"cg_group\"},{\"id\":\"86\",\"label\":\"WASCHANLAGE\",\"type\":\"cg_group\"},{\"id\":\"88\",\"label\":\"ANBAUTEILE\",\"type\":\"cg_group\"},{\"id\":\"91\",\"label\":\"FAHRERSITZ\",\"type\":\"cg_group\"},{\"id\":\"92\",\"label\":\"FONDSITZ\",\"type\":\"cg_group\"},{\"id\":\"98\",\"label\":\"ZUBEHOER\",\"type\":\"cg_group\"},{\"id\":\"99\",\"label\":\"SONDEREINBAUTEN\",\"type\":\"cg_group\"}]}");
    }

    public void testGetPartsListFinOrVinDAIMLER14253() {
        // Hier reicht die Fake-Datenkarte XXX2050041F019438 ohne Code
        String testMatNo = "N910105008014";
        EtkDataPart part = EtkDataObjectFactory.createDataPart(getProject(), testMatNo, "");
        if (!part.existsInDB()) {
            fail("Material \"" + testMatNo + "\" does not exist in the DB");
        }

        try {
            // Erst den Gefahrgutkenner auf einen Wert setzen, der über die Enums ein Ergebnis bringt.
            part.setFieldValue(iPartsConst.FIELD_M_HAZARDOUS_GOODS_INDICATOR, "1.4", DBActionOrigin.FROM_EDIT);
            // In die DB schreiben
            part.saveToDB();
            clearWebservicePluginsCaches();
            EtkDataAssembly.clearGlobalEntriesCache();
            Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");
            executeWebservice(iPartsWSPartsListEndpoint.DEFAULT_ENDPOINT_URI + "?finOrVin=XXX2050041F019438&productId=C01&includeAggs=true",
                              null, additionalRequestProperties,
                              DWFile.get(getTestWorkingDir(), "resultPartsListFinOrVinDAIMLER14253.txt"));
        } finally {
            // Die für den Test manipulierten Daten wieder zurücksetzen.
            part.setFieldValue(iPartsConst.FIELD_M_HAZARDOUS_GOODS_INDICATOR, "", DBActionOrigin.FROM_EDIT);
            part.saveToDB();
            clearWebservicePluginsCaches();
            EtkDataAssembly.clearGlobalEntriesCache();
        }
    }

    public void testEqualPartsForDAIMLER12290() {
        // Gleichteile-Mapping aktivieren
        UniversalConfiguration pluginConfig = iPartsPlugin.getPluginConfig();
        ConfigBase config = pluginConfig.getConfig();
        boolean oldShowEqualParts = config.getBoolean(pluginConfig.getPath() + iPartsPlugin.CONFIG_SHOW_EQUAL_PARTS.getKey(),
                                                      false);
        try {
            writeBooleanConfigValues(pluginConfig, true, iPartsPlugin.CONFIG_SHOW_EQUAL_PARTS);

            clearWebservicePluginsCaches(); // Caches müssen gelöscht werden, damit die Änderung am Gleichteile-Mapping auch auf jeden Fall berücksichtigt wird

            // Analoger Test zu testGetPartInfoAlternativeParts() mit MBAG-Gleichteil A1416780110 für A9416780110
            executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"partContext\":{\"moduleId\":\"F10_67_060_00001\",\"sequenceId\":\"00016\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"D967820\",\"productId\":\"F10\",\"filterOptions\":{}}}",
                              "{\"partInfo\":{\"alternativeParts\":[{\"es1Key\":\"79\",\"es2Key\":\"7065\",\"name\":\"GLASSCHEIBE\",\"partNo\":\"A1416780110\",\"partNoFormatted\":\"A 141 678 01 10\",\"type\":\"01\"},{\"es1Key\":\"92\",\"es2Key\":\"7025\",\"name\":\"GLASSCHEIBE\",\"partNo\":\"A1416780110\",\"partNoFormatted\":\"A 141 678 01 10\",\"type\":\"01\"}],\"saaValidityDetails\":[{\"code\":\"Z 508.529\",\"description\":\"SV FENSTERANLAGE HINTEN\",\"saaCodes\":[{\"code\":\"Z 508.529/05\",\"description\":\"SV FENSTERANLAGE HINTEN\"}]}]}}");

            // Analoger Test zu testGetPartsAlternativeParts() mit MBAG-Gleichteil A1416780110 für A9416780110 an lfdNr 00016
            // Manipulierte Stückliste F10_67_060_00001 inkl. Wahlweise-Set, Nachfolger auf Stücklisteneintrag sowie Stücklisteneintrag
            // selbst mit lfdNr 00016 und obigem MBAG-Gleichteil sowie Nachfolger auf Materialnummer N000000000009 mit Gleichteil
            // N100000000009
            executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"D967820\"," +
                              "\"productId\":\"F10\",\"filterOptions\":{}},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"67\"},{\"type\":\"cg_subgroup\",\"id\":\"060\"}]}",
                              DWFile.get(getTestWorkingDir(), "resultPartsAlternativesEqualPartsDAIMLER12290.txt"));

        } finally {
            writeBooleanConfigValues(pluginConfig, oldShowEqualParts, iPartsPlugin.CONFIG_SHOW_EQUAL_PARTS);
            clearWebservicePluginsCaches(); // Caches müssen gelöscht werden, damit die Änderung am Gleichteile-Mapping auch auf jeden Fall berücksichtigt wird
        }
    }

    public void testSearchEqualPartsForDAIMLER12291() {
        // Daten aus dem Test testEqualPartsForDAIMLER12290(), aber ohne aktives Gleichtteile-Mapping -> kein Ergebnis
        executeWebservice(iPartsWSSearchPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"D967820\",\"productId\":\"F10\"},\"includeSAs\":false,\"searchText\":\"A1416780110\"," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"67\"},{\"type\":\"cg_subgroup\",\"id\":\"060\"}]}",
                          "{\"searchResults\":[]}");

        // Gleichteile-Mapping aktivieren
        UniversalConfiguration pluginConfig = iPartsPlugin.getPluginConfig();
        ConfigBase config = pluginConfig.getConfig();
        boolean oldShowEqualParts = config.getBoolean(pluginConfig.getPath() + iPartsPlugin.CONFIG_SHOW_EQUAL_PARTS.getKey(),
                                                      false);
        try {
            writeBooleanConfigValues(pluginConfig, true, iPartsPlugin.CONFIG_SHOW_EQUAL_PARTS);

            clearWebservicePluginsCaches(); // Caches müssen gelöscht werden, damit die Änderung am Gleichteile-Mapping auch auf jeden Fall berücksichtigt wird

            // Daten aus dem Test testEqualPartsForDAIMLER12290() -> Ergebnis
            executeWebservice(iPartsWSSearchPartsEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"D967820\",\"productId\":\"F10\"},\"includeSAs\":false,\"searchText\":\"A1416780110\"," +
                              "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"67\"},{\"type\":\"cg_subgroup\",\"id\":\"060\"}]}",
                              "{\"searchResults\":[{\"name\":\"\",\"navContext\":[{\"id\":\"67\",\"label\":\"FENSTERANLAGE\",\"type\":\"cg_group\"},{\"id\":\"060\",\"label\":\"VERGLASUNG\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}],\"partNo\":\"A1416780110\",\"partNoFormatted\":\"A 141 678 01 10\"}]}");
        } finally {
            writeBooleanConfigValues(pluginConfig, oldShowEqualParts, iPartsPlugin.CONFIG_SHOW_EQUAL_PARTS);
            clearWebservicePluginsCaches(); // Caches müssen gelöscht werden, damit die Änderung am Gleichteile-Mapping auch auf jeden Fall berücksichtigt wird
        }
    }

    public void testGetPartsGenVOForDAIMLER12403() {
        // StarPart muss ausgegeben werden für Land DE
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C253303\",\"productId\":\"C253_FC\"}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"24\"},{\"type\":\"cg_subgroup\",\"id\":\"015\"}],\"user\":{\"country\":\"DE\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartsGenVODAIMLER12403.txt"));
    }

    public void testPartsListGenVOForDAIMLER12403Part1() {
        // Token enthält Sprachen de, en, fr, d.h. de ist die Hauptsprache und en, fr sind die Rückfallsprachen
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");

        executeWebservice(iPartsWSPartsListEndpoint.DEFAULT_ENDPOINT_URI + "?model=C253303&productId=C253_FC",
                          null, additionalRequestProperties,
                          DWFile.get(getTestWorkingDir(), "resultPartsListGenVODAIMLER12403.txt"));
    }

    public void testPartsListGenVOForDAIMLER12403Part2() {
        // Token enthält Sprachen de, en, fr, d.h. de ist die Hauptsprache und en, fr sind die Rückfallsprachen
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");

        executeWebservice(iPartsWSPartsListEndpoint.DEFAULT_ENDPOINT_URI + "?model=C253303&productId=C253_FC&reducedInformation=true",
                          null, additionalRequestProperties,
                          DWFile.get(getTestWorkingDir(), "resultPartsListGenVODAIMLER12403Reduced.txt"));
    }

    public void testGetPartsCountryAndSpecForDAIMLER13781Part1() {
        // Stückliste für FIN WDD2050021F651760 und Land DE
        executeWebservice(iPartsWSGetMaterialPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"productId\":\"598\",\"productClassId\":\"P\",\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205002\",\"productId\":\"C01\",\"fin\":\"WDD2050021F651760\",\"datacardExists\":true}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"18\"}],\"user\":{\"country\":\"DE\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"images\":[],\"parts\":[{\"calloutId\":\"-----\",\"countryValidity\":[\"DE\"],\"description\":\"MOBIL SAE 10W-40\",\"footNotes\":[{\"id\":\"050\",\"text\":\"BEI TRANSPORT, LAGERUNG UND VERARBEITUNG GEFAHRGUT VORSCHRIFT BZW. GEFAHRSTOFFVERORDNUNG BEACHTEN\",\"type\":\"text\"}],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"598_18_001_00001\",\"sequenceId\":\"00012\"},\"partNo\":\"Q0007292V000000000\",\"partNoFormatted\":\"Q 0007292V000000000\",\"quantity\":\"1\"}]}");
    }

    public void testGetPartsCountryAndSpecForDAIMLER13781Part2() {
        // Stückliste für FIN WDD2050021F651760 und Land DK
        executeWebservice(iPartsWSGetMaterialPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"productId\":\"598\",\"productClassId\":\"P\",\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205002\",\"productId\":\"C01\",\"fin\":\"WDD2050021F651760\",\"datacardExists\":true}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"18\"}],\"user\":{\"country\":\"DK\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"images\":[],\"parts\":[{\"calloutId\":\"-----\",\"countryValidity\":[\"DK\"],\"description\":\"100 ML; SAE 10W-40\",\"footNotes\":[{\"id\":\"050\",\"text\":\"BEI TRANSPORT, LAGERUNG UND VERARBEITUNG GEFAHRGUT VORSCHRIFT BZW. GEFAHRSTOFFVERORDNUNG BEACHTEN\",\"type\":\"text\"}],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"598_18_001_00001\",\"sequenceId\":\"00013\"},\"partNo\":\"Q0012966V000000000\",\"partNoFormatted\":\"Q 0012966V000000000\",\"quantity\":\"1\"}]}");

    }

    public void testGetPartsCountryAndSpecForDAIMLER13781Part3() {
        // Stückliste für FIN WDD2050021F651760 (aber ohne geladene Datenkarte) und kein Land
        executeWebservice(iPartsWSGetMaterialPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"productId\":\"598\",\"productClassId\":\"P\",\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205002\",\"productId\":\"C01\",\"fin\":\"WDD2050021F651760\",\"datacardExists\":false}," + "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"18\"}],\"user\":{\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"images\":[],\"parts\":[{\"calloutId\":\"-----\",\"description\":\"209 L; SAE 10W-40\",\"footNotes\":[{\"id\":\"012\",\"text\":\"BEI BESTELLUNG ERGAENZUNGSSCHLUESSEL ANGEBEN: 12\",\"type\":\"text\"},{\"id\":\"050\",\"text\":\"BEI TRANSPORT, LAGERUNG UND VERARBEITUNG GEFAHRGUT VORSCHRIFT BZW. GEFAHRSTOFFVERORDNUNG BEACHTEN\",\"type\":\"text\"}],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"598_18_001_00001\",\"sequenceId\":\"00001\"},\"partNo\":\"A0009896001\",\"partNoFormatted\":\"A 000 989 60 01\",\"quantity\":\"1\",\"specValidity\":[\"229.51\"]},{\"calloutId\":\"-----\",\"description\":\"1 L; SAE 0W-30 BLUETEC\",\"footNotes\":[{\"id\":\"009\",\"text\":\"BEI BESTELLUNG ERGAENZUNGSSCHLUESSEL ANGEBEN: 09\",\"type\":\"text\"},{\"id\":\"050\",\"text\":\"BEI TRANSPORT, LAGERUNG UND VERARBEITUNG GEFAHRGUT VORSCHRIFT BZW. GEFAHRSTOFFVERORDNUNG BEACHTEN\",\"type\":\"text\"},{\"id\":\"071\",\"text\":\"NACH BETRIEBSSTOFFVORSCHRIFT BLATT 229.51\",\"type\":\"text\"}],\"level\":\"01\",\"name\":\"MOTORENOEL\",\"partContext\":{\"moduleId\":\"598_18_001_00001\",\"sequenceId\":\"00005\"},\"partNo\":\"A0019891601\",\"partNoFormatted\":\"A 001 989 16 01\",\"quantity\":\"1\",\"specValidity\":[\"229.52\"]},{\"calloutId\":\"-----\",\"description\":\"5 L; SAE 0W-30 BLUETEC\",\"footNotes\":[{\"id\":\"011\",\"text\":\"BEI BESTELLUNG ERGAENZUNGSSCHLUESSEL ANGEBEN: 11\",\"type\":\"text\"},{\"id\":\"050\",\"text\":\"BEI TRANSPORT, LAGERUNG UND VERARBEITUNG GEFAHRGUT VORSCHRIFT BZW. GEFAHRSTOFFVERORDNUNG BEACHTEN\",\"type\":\"text\"},{\"id\":\"071\",\"text\":\"NACH BETRIEBSSTOFFVORSCHRIFT BLATT 229.51\",\"type\":\"text\"}],\"level\":\"01\",\"name\":\"MOTORENOEL\",\"partContext\":{\"moduleId\":\"598_18_001_00001\",\"sequenceId\":\"00006\"},\"partNo\":\"A0019891601\",\"partNoFormatted\":\"A 001 989 16 01\",\"quantity\":\"1\",\"specValidity\":[\"229.52\"]},{\"calloutId\":\"-----\",\"description\":\"1 L; SAE  5W-40\",\"footNotes\":[{\"id\":\"050\",\"text\":\"BEI TRANSPORT, LAGERUNG UND VERARBEITUNG GEFAHRGUT VORSCHRIFT BZW. GEFAHRSTOFFVERORDNUNG BEACHTEN\",\"type\":\"text\"}],\"level\":\"01\",\"name\":\"MOTORENOEL\",\"partContext\":{\"moduleId\":\"598_18_001_00001\",\"sequenceId\":\"00007\"},\"partNo\":\"A0009898201\",\"partNoFormatted\":\"A 000 989 82 01\",\"quantity\":\"1\",\"specValidity\":[\"229.56\"]},{\"calloutId\":\"-----\",\"description\":\"60 L; SAE 5W-40\",\"footNotes\":[{\"id\":\"009\",\"text\":\"BEI BESTELLUNG ERGAENZUNGSSCHLUESSEL ANGEBEN: 09\",\"type\":\"text\"},{\"id\":\"050\",\"text\":\"BEI TRANSPORT, LAGERUNG UND VERARBEITUNG GEFAHRGUT VORSCHRIFT BZW. GEFAHRSTOFFVERORDNUNG BEACHTEN\",\"type\":\"text\"}],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"598_18_001_00001\",\"sequenceId\":\"00008\"},\"partNo\":\"A0009898301\",\"partNoFormatted\":\"A 000 989 83 01\",\"quantity\":\"1\",\"specValidity\":[\"229.52\"]},{\"calloutId\":\"-----\",\"description\":\"205 L; SAE 5W-40\",\"footNotes\":[{\"id\":\"012\",\"text\":\"BEI BESTELLUNG ERGAENZUNGSSCHLUESSEL ANGEBEN: 12\",\"type\":\"text\"},{\"id\":\"050\",\"text\":\"BEI TRANSPORT, LAGERUNG UND VERARBEITUNG GEFAHRGUT VORSCHRIFT BZW. GEFAHRSTOFFVERORDNUNG BEACHTEN\",\"type\":\"text\"}],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"598_18_001_00001\",\"sequenceId\":\"00009\"},\"partNo\":\"A0009898301\",\"partNoFormatted\":\"A 000 989 83 01\",\"quantity\":\"1\",\"specValidity\":[\"229.56\"]},{\"calloutId\":\"-----\",\"description\":\"1 L ; 5W-50\",\"footNotes\":[{\"id\":\"010\",\"text\":\"BEI BESTELLUNG ERGAENZUNGSSCHLUESSEL ANGEBEN: 10\",\"type\":\"text\"},{\"id\":\"050\",\"text\":\"BEI TRANSPORT, LAGERUNG UND VERARBEITUNG GEFAHRGUT VORSCHRIFT BZW. GEFAHRSTOFFVERORDNUNG BEACHTEN\",\"type\":\"text\"}],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"598_18_001_00001\",\"sequenceId\":\"00010\"},\"partNo\":\"A0009898801\",\"partNoFormatted\":\"A 000 989 88 01\",\"quantity\":\"1\",\"specValidity\":[\"228.51\"]},{\"calloutId\":\"-----\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"598_18_001_00001\",\"sequenceId\":\"00011\"},\"partNo\":\"Q10303949\",\"partNoFormatted\":\"Q 10303949\",\"quantity\":\"1\",\"specValidity\":[\"228.51\"]},{\"calloutId\":\"-----\",\"countryValidity\":[\"DE\"],\"description\":\"MOBIL SAE 10W-40\",\"footNotes\":[{\"id\":\"050\",\"text\":\"BEI TRANSPORT, LAGERUNG UND VERARBEITUNG GEFAHRGUT VORSCHRIFT BZW. GEFAHRSTOFFVERORDNUNG BEACHTEN\",\"type\":\"text\"}],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"598_18_001_00001\",\"sequenceId\":\"00012\"},\"partNo\":\"Q0007292V000000000\",\"partNoFormatted\":\"Q 0007292V000000000\",\"quantity\":\"1\"},{\"calloutId\":\"-----\",\"countryValidity\":[\"DK\"],\"description\":\"100 ML; SAE 10W-40\",\"footNotes\":[{\"id\":\"050\",\"text\":\"BEI TRANSPORT, LAGERUNG UND VERARBEITUNG GEFAHRGUT VORSCHRIFT BZW. GEFAHRSTOFFVERORDNUNG BEACHTEN\",\"type\":\"text\"}],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"598_18_001_00001\",\"sequenceId\":\"00013\"},\"partNo\":\"Q0012966V000000000\",\"partNoFormatted\":\"Q 0012966V000000000\",\"quantity\":\"1\"}]}");
    }

    public void testIdentNoOilQuantityForDAIMLER14243() {
        // Keine Ausgabe der Öl-Nachfüllmenge, weil es zwei passende Datensätze geben würde
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"country\":\"de\"},\"identCode\":\"WDD1724032F086455\"}",
                                                      "{\"identContexts\":[{\"aggregates\":[{\"aggregateNumber\":\"65198032011335\",\"aggTypeId\":\"M\",\"modelId\":\"D651980\",\"modelTypeId\":\"D651\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PC\"],\"productId\":\"69L\"}],\"aggTypeId\":\"F\",\"datacardExists\":true,\"fin\":\"WDD1724032F086455\",\"integratedNavigationAvailable\":true,\"mbOilSpecs\":[{\"spec\":\"222.2\",\"text\":\"Siehe SI18.00-P-1311A; Einschränkung: Nur SAE 0W-30 oder SAE 5W-30 verwenden!\",\"type\":\"ENGINE_OIL\"}],\"mbSpecs\":[{\"spec\":\"222.2\",\"text\":\"Siehe SI18.00-P-1311A; Einschränkung: Nur SAE 0W-30 oder SAE 5W-30 verwenden!\",\"type\":\"ENGINE_OIL\"}],\"modelId\":\"C172403\",\"modelTypeId\":\"C172\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PC\"],\"productId\":\"D43\"}]}");
    }

    public void testGetPartsSpringFilterWithCodeFilterForDAIMLER14649() {
        // Für Hotspot 30 gibt es nach manuellen Anpassungen im TU D43_32_054_00001 nur noch einen gültigen Stücklisteneintrag
        // aufgrund vom Code-Filter im Feder-Filter
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C172403\",\"fin\":\"WDD1724032F086455\",\"datacardExists\":true,\"productId\":\"D43\"}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"32\"},{\"type\":\"cg_subgroup\",\"id\":\"054\"}],\"user\":{\"country\":\"DE\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartsSpringFilterWithCodeFilterForDAIMLER14649.txt"));
    }

    public void testGetPartInfoUnfilteredAlternativePartsForDAIMLER15217() {
        // Das Alternativteil mit BaseMatNr=A4473406501, ES1=80 und ES2=7101 wird NICHT ausgefiltert, weil PRIMUS-Ersetzungen
        // standardmäßig in den Webservice Unittests nicht geladen werden
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C129058\",\"productId\":\"515\"}," +
                          "\"partContext\":{\"moduleId\":\"515_24_005_00001\",\"sequenceId\":\"00002\"},\"user\":{\"country\":\"DE\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"partInfo\":{\"alternativeParts\":[{\"es1Key\":\"80\",\"name\":\"O-RING\",\"partNo\":\"A4473406501\",\"partNoFormatted\":\"A 447 340 65 01\",\"type\":\"01\"},{\"es1Key\":\"80\",\"es2Key\":\"7101\",\"name\":\"O-RING\",\"partNo\":\"A4473406501\",\"partNoFormatted\":\"A 447 340 65 01\",\"type\":\"01\"}]}}");
    }

    public void testGetPartInfoFilteredAlternativePartsForDAIMLER15217() {
        // PRIMUS-Ersetzungen explizit temporär aktivieren (für A4473406501)
        AssemblyId assemblyId = new AssemblyId("515_24_005_00001", ""); // Dieses Modul muss auch jeweils aus dem Cache gelöscht werden
        executeTestWithBooleanConfigChanges(iPartsPlugin.getPluginConfig(), iPartsPlugin.CONFIG_PRIMUS_HINT_HANDLING, true, () -> {
            iPartsPRIMUSReplacementsCache.clearCache(); // Auch der Cache für die PRIMUS-Ersetzungen muss neu aufgebaut werden
            EtkDataAssembly.removeDataAssemblyFromCache(getProject(), assemblyId);
            // Das Alternativteil mit BaseMatNr=A4473406501, ES1=80 und ES2=7101 wird ausgefiltert, weil es dafür auch eine
            // PRIMUS-Ersetzung gibt
            executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C129058\",\"productId\":\"515\"}," +
                              "\"partContext\":{\"moduleId\":\"515_24_005_00001\",\"sequenceId\":\"00002\"},\"user\":{\"country\":\"DE\",\"language\":\"de\",\"userId\":\"userId\"}}",
                              "{\"partInfo\":{\"alternativeParts\":[{\"es1Key\":\"80\",\"name\":\"O-RING\",\"partNo\":\"A4473406501\",\"partNoFormatted\":\"A 447 340 65 01\",\"type\":\"01\"}]}}");
        });
        iPartsPRIMUSReplacementsCache.clearCache(); // Auch der Cache für die PRIMUS-Ersetzungen muss neu aufgebaut werden
        EtkDataAssembly.removeDataAssemblyFromCache(getProject(), assemblyId);
    }

    public void testIdentVINisValidFINForDAIMLER15096() {
        // VIN ist auch eine gültige FIN, die aber zu keinem Ergebnis führen würde
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"country\":\"de\"},\"identCode\":\"W1T9634081C050941\"}",
                                                      DWFile.get(getTestWorkingDir(), "resultWSIdentVINisValidFINForDAIMLER15096.txt"));
    }

    public void testIdentVINisValidFINForDAIMLER15148() {
        // VIN ist auch eine gültige FIN und es gäbe leider sogar ein gültiges Baumuster-Fallback
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"country\":\"de\"},\"identCode\":\"W1T9634051C052212\"}",
                                                      DWFile.get(getTestWorkingDir(), "resultWSIdentVINisValidFINForDAIMLER15148.txt"));
    }

    public void testGetPartsPrimusCode74AvailableDAIMLER15125() {
        // primusCode74Available ist true für Teilenummer A0014902992
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"productId\": \"L01\",\"productClassIds\": [ \"L\" ],\"modelId\": \"D930720\",\"aggTypeId\": \"AS\"}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"49\"},{\"type\":\"cg_subgroup\",\"id\":\"060\"}],\"user\":{\"country\":\"DE\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartsPrimusCode74AvailableDAIMLER15125.txt"));
    }

    public void testGetPartsFactoryFromAggregateDAIMLER15308() {
        // Werk 010 von der Motor-Datenkarte
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"M\",\"productClassIds\":[\"P\"],\"modelId\":\"D651924\",\"productId\":\"69L\",\"fin\":\"WDD2122031A297563\",\"datacardExists\":true}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"03\"},{\"type\":\"cg_subgroup\",\"id\":\"015\"}],\"user\":{\"country\":\"DE\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartsFactoryFromAggregateDAIMLER15308.txt"));
    }

    public void testGetPartsFactoryFromVehicleDAIMLER15308() {
        // Werk 050 von der Fahrzeug-Datenkarte durch Aktivieren vom Flag DA_PRODUCT.DP_USE_FACTORY
        iPartsProductId productId = new iPartsProductId("69L");
        try {
            iPartsDataProduct product = new iPartsDataProduct(getProject(), productId);
            product.setFieldValueAsBoolean(FIELD_DP_USE_FACTORY, true, DBActionOrigin.FROM_EDIT);
            product.saveToDB(false, DBDataObject.PrimaryKeyExistsInDB.TRUE);
            iPartsProduct.removeProductFromCache(getProject(), productId);
            clearWebservicePluginsCaches();
            iPartsDataCardRetrievalHelper.clearCache();
            executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"identContext\":{\"aggTypeId\":\"M\",\"productClassIds\":[\"P\"],\"modelId\":\"D651924\",\"productId\":\"69L\",\"fin\":\"WDD2122031A297563\",\"datacardExists\":true}," +
                              "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"03\"},{\"type\":\"cg_subgroup\",\"id\":\"015\"}],\"user\":{\"country\":\"DE\",\"language\":\"de\",\"userId\":\"userId\"}}",
                              DWFile.get(getTestWorkingDir(), "resultGetPartsFactoryFromVehicleDAIMLER15308.txt"));
        } finally {
            iPartsDataProduct product = new iPartsDataProduct(getProject(), productId);
            product.setFieldValueAsBoolean(FIELD_DP_USE_FACTORY, false, DBActionOrigin.FROM_EDIT);
            product.saveToDB(false, DBDataObject.PrimaryKeyExistsInDB.TRUE);
            iPartsProduct.removeProductFromCache(getProject(), productId);
            clearWebservicePluginsCaches();
            iPartsDataCardRetrievalHelper.clearCache();
        }
    }

    public void testGetPartInfoEngineRemanVariantAlternativePartsForDAIMLER15820() {
        // Es gibt für Materialnummer A6510103148 neben einem echten Alternativteil auch Motor-Reman-Varianten, die als
        // Alternativteile ausgegeben werden
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C166063\",\"productId\":\"D99\"}," +
                          "\"partContext\":{\"moduleId\":\"D99_21_050_00001\",\"sequenceId\":\"00011\"},\"user\":{\"country\":\"DE\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"partInfo\":{\"alternativeParts\":[{\"es1Key\":\"80\",\"name\":\"DIESELMOTOR\",\"partNo\":\"A6510103148\",\"partNoFormatted\":\"A 651 010 31 48\",\"type\":\"01\"},{\"es1Key\":\"80\",\"name\":\"RUMPFMOTOR\",\"partNo\":\"A6510107511\",\"partNoFormatted\":\"A 651 010 75 11\",\"type\":\"10\"},{\"es1Key\":\"80\",\"name\":\"TEILMOTOR\",\"partNo\":\"A6510104226\",\"partNoFormatted\":\"A 651 010 42 26\",\"type\":\"13\"}]}}");
    }

    public void testGetPartsOmittedPartsScoringDAIMLER16004() {
        // A0008272000 an Hotspot 320 darf in dem TU P01_98_080_00001 nicht ausgefiltert werden, weil die Teileposition
        // höheres Scoring hat als die Wegfallposition (anders als A0008271900 an Hotspot 310)
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C246200\",\"productId\":\"P01\",\"fin\":\"WDD2462001J111252\",\"datacardExists\":true}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"98\"},{\"type\":\"cg_subgroup\",\"id\":\"080\"}],\"user\":{\"country\":\"FR\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"images\":[],\"parts\":[{\"calloutId\":\"320\",\"codeValidity\":\"M013+260+290;\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"P01_98_080_00001\",\"sequenceId\":\"00038\"},\"partNo\":\"A0008272000\",\"partNoFormatted\":\"A 000 827 20 00\",\"quantity\":\"NB\"}]}");
    }

    public void testGetPartsForFilterDAIMLER16107a() {
        // Für Baumuster C216371 ist lfdnr 00165 (Nachfolger) nicht gültig, weswegen die PRIMUS-Ersetzung an lfdnr 00059
        // angezeigt wird
        executeTestWithBooleanConfigChanges(iPartsPlugin.getPluginConfig(), iPartsPlugin.CONFIG_PRIMUS_HINT_HANDLING, true, () ->
                executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                                  "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C216371\",\"productId\":\"65V\"}," +
                                  "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"32\"},{\"type\":\"cg_subgroup\",\"id\":\"290\"}],\"user\":{\"country\":\"FR\",\"language\":\"de\",\"userId\":\"userId\"}}",
                                  DWFile.get(getTestWorkingDir(), "resultGetPartsForFilterDAIMLER16107a.txt"))
        );
    }

    public void testGetPartsForFilterDAIMLER16107b() {
        // Für Baumuster C216373 ist lfdnr 00165 (Nachfolger) gültig, weswegen diese echte Ersetzung an lfdnr 00059
        // angezeigt wird
        executeTestWithBooleanConfigChanges(iPartsPlugin.getPluginConfig(), iPartsPlugin.CONFIG_PRIMUS_HINT_HANDLING, true, () ->
                executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                                  "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C216373\",\"productId\":\"65V\"}," +
                                  "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"32\"},{\"type\":\"cg_subgroup\",\"id\":\"290\"}],\"user\":{\"country\":\"FR\",\"language\":\"de\",\"userId\":\"userId\"}}",
                                  DWFile.get(getTestWorkingDir(), "resultGetPartsForFilterDAIMLER16107b.txt"))
        );
    }

    // Explizite Tests für die Ereignis-Filterung
    public void testGetPartInfoForEventFilterPart1() {
        // FIN WDB2030061F261383 hat basierend auf den Code das Ereignis "1E0"
        // Stücklistenposition mit Ereignis-Ab "1E5" nach dem Datenkarten-Ereignis -> Position wird gefiltert
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"partContext\":{\"moduleId\":\"65F_42_145_00001\",\"sequenceId\":\"00003\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C203006\",\"productId\":\"65F\",\"fin\":\"WDB2030061F261383\",\"datacardExists\":true}," +
                          "\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"code\":4001,\"message\":\"Path 'partContext': SequenceId '00003' is invalid in module '65F_42_145_00001' for current context and filter options\"}", HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testGetPartInfoForEventFilterPart2() {
        // FIN WDB2030061F261383 hat basierend auf den Code das Ereignis "1E0"
        // Stücklistenpositione mit Ereignis-Bis "9E5" vor dem Datenkarten-Ereignis -> Position wird gefiltert
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"partContext\":{\"moduleId\":\"65F_42_145_00001\",\"sequenceId\":\"00004\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C203006\",\"productId\":\"65F\",\"fin\":\"WDB2030061F261383\",\"datacardExists\":true}," +
                          "\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"code\":4001,\"message\":\"Path 'partContext': SequenceId '00004' is invalid in module '65F_42_145_00001' for current context and filter options\"}", HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testGetPartInfoForEventFilterPart3() {
        // FIN WDB2030061F261383 hat basierend auf den Code das Ereignis "1E0"
        // Code-Regeln der Ereignisse hinzufügen bei ereignisgesteuerten Baureihen für Stücklistenpositionen mit Ereignis-Ab "1E5"
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C203006\",\"productId\":\"65F\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"42\"},{\"type\":\"cg_subgroup\",\"id\":\"145\"}]," +
                          "\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartsFilterForEvents.txt"));
    }

    public void testGetPartInfoForEventFilterPart4() {
        // FIN WDB2030061F261383 hat basierend auf den Code das Ereignis "1E0"
        // ... und die passende PartInfo zu Part3 für die Code-Matrix
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"partContext\":{\"moduleId\":\"65F_42_145_00001\",\"sequenceId\":\"00003\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C203006\",\"productId\":\"65F\"}," +
                          "\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"partInfo\":{\"codeValidityDetails\":[{\"code\":\"052\",\"dateFrom\":\"1998-05-04\",\"name\":\"AEJ 01/2\"},{\"code\":\"053\",\"dateFrom\":\"2001-07-31\",\"name\":\"AEJ 12/0; 12/2; 13/0\"},{\"code\":\"802\",\"dateFrom\":\"1998-08-10\",\"name\":\"AEJ 01/1\"},{\"code\":\"803\",\"dateFrom\":\"1998-08-10\",\"name\":\"AEJ 02/1\"},{\"code\":\"M001\",\"dateFrom\":\"1996-03-29\",\"name\":\"MOTOREN MIT MECHANISCHEN LADER\"},{\"code\":\"M112\",\"dateFrom\":\"1996-03-29\",\"name\":\"V6-OTTOMOTOR M112\"}],\"codeValidityMatrix\":[[{\"code\":\"052\"},{\"code\":\"802\"},{\"code\":\"M112\",\"negative\":true}],[{\"code\":\"053\",\"negative\":true},{\"code\":\"803\"},{\"code\":\"M112\",\"negative\":true}],[{\"code\":\"053\"},{\"code\":\"803\"},{\"code\":\"M112\",\"negative\":true}],[{\"code\":\"052\"},{\"code\":\"802\"},{\"code\":\"M001\",\"negative\":true}],[{\"code\":\"053\",\"negative\":true},{\"code\":\"803\"},{\"code\":\"M001\",\"negative\":true}],[{\"code\":\"053\"},{\"code\":\"803\"},{\"code\":\"M001\",\"negative\":true}]]}}");
    }

    public void testGetPartInfoForEventFilterColorPart1a() {
        // Ereignis-Filterung für Farben. Farben haben Konstruktionsereignisse und AS-Ereignisse. AS-Ereignisse
        // überschreiben Konstruktionsereignisse. Konstruktionsereignisse können mit dem AS-Ereignis "nicht relevant"
        // unterdrückt werden.
        //
        // Aufbau für alle drei folgenden Farben
        // Datenkartenereignis: "1E0"
        // Konstruktionsereignis-Ab: "7E0"
        // Konstruktionsereignis-Bis: "0E0"
        // Reihenfolger der Ereignisse: "7E0" -> "0E0" -> "1E0"

        // 1. Test:
        // Farbe wird ausgefiltert, weil das Datenkartenereignis außerhalt der Subereigniskette "7E0" <-> "0E0" liegt
        // 1a. ohne Datenkarte -> Farbe zu Teil wird ausgegeben
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"partContext\":{\"moduleId\":\"65F_42_145_00001\",\"sequenceId\":\"00029\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C203006\",\"productId\":\"65F\",\"fin\":\"WDB2030061F261383\"}," +
                          "\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultColorFilterForEvents1.txt"));
    }

    public void testGetPartInfoForEventFilterColorPart1b() {
        // 1b. mit Datenkarte -> Farbe wird gefiltert
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"partContext\":{\"moduleId\":\"65F_42_145_00001\",\"sequenceId\":\"00029\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C203006\",\"productId\":\"65F\",\"fin\":\"WDB2030061F261383\",\"datacardExists\":true}," +
                          "\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"partInfo\":{}}");
    }

    public void testGetPartInfoForEventFilterColorPart2() {
        // 2.Test:
        // Farbe wird nicht ausgefiltert, weil das AS-Ereignis-Bis "2E0" die Subereigniskette erweitert und das Datenkartenereignis
        // Die Reihenfolge der Ereignis ist: "7E0" -> "0E0" -> "1E0" -> "2E0"
        // somit in der Subereigniskette liegt. Farbe ist somit gültig
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"partContext\":{\"moduleId\":\"65F_58_015_00001\",\"sequenceId\":\"00052\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C203006\",\"productId\":\"65F\",\"fin\":\"WDB2030061F261383\",\"datacardExists\":true}," +
                          "\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultColorFilterForEvents2.txt"));
    }

    public void testGetPartInfoForEventFilterColorPart3() {
        // 3.Test:
        // Farbe wird nicht ausgefiltert, weil das AS-Ereignis "nicht relevant" das Konstruktionsereignis "0E0" unterdrückt.
        // Die Subereigniskette hat somit kein Ereignis-Bis mehr und ist somit nicht begrenzt. Das Datenkartenereignis
        // lag hinter der Subereigniskette und ist nun - ohne Bis-Begrenzung - gültig
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"partContext\":{\"moduleId\":\"65F_58_015_00001\",\"sequenceId\":\"00056\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C203006\",\"productId\":\"65F\",\"fin\":\"WDB2030061F261383\",\"datacardExists\":true}," +
                          "\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultColorFilterForEvents3.txt"));
    }


    // Explizite Tests für die Webservice-Token Admin-Optionen
    public void testActiveAndInactiveTokenValidity() {
        UniversalConfiguration pluginConfig = iPartsWebservicePlugin.getPluginConfig();
        ConfigBase config = pluginConfig.getConfig();
        boolean oldCheckPermissions = config.getBoolean(pluginConfig.getPath() + iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_PERMISSIONS.getKey(),
                                                        false);
        boolean oldCheckCountryValidity = config.getBoolean(pluginConfig.getPath() + iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_COUNTRY_VALIDITY.getKey(),
                                                            false);
        try {
            //--------------------Admin-Optionen auf "true"----------------------
            writeBooleanConfigValues(pluginConfig, true, iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_PERMISSIONS,
                                     iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_COUNTRY_VALIDITY);


            clearWebservicePluginsCaches(); // Caches müssen gelöscht werden, damit die Admin-Änderung auch auf jeden Fall berücksichtigt wird

            // Marke und Produktgruppe
            // 1. Valide Anfrage bei der die Permissions gültig sind
            Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJFTiIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiZGUiLCJsYW5nMiI6ImZyIiwibGFuZzMiOiJlcyIsImV4cCI6OTk5OTk5OTk5OSwicGVybWlzc2lvbnMiOnsiU01UIjpbIlBBU1NFTkdFUi1DQVIiXX19.zt9aA-2EpTdvaifJFTGWbKiZTzgtcdsJLUzm0d91xoA");
            executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"identCode\":\"204001\"}", additionalRequestProperties,
                              DWFile.get(getTestWorkingDir(), "resultIdentOnlySmartBrandToken.txt"));

            // 2. Invalide Anfrage, weil die Permissions leer sind
            additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiZGUiLCJsYW5nMiI6ImZyIiwibGFuZzMiOiJlcyIsImV4cCI6OTk5OTk5OTk5OSwicGVybWlzc2lvbnMiOnt9fQ.8vwKzS_cX4EljkSU9IN0dNKdQi_fetITDhC3QzGf9ew");
            executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"identCode\":\"204001\"}", additionalRequestProperties,
                              "{\"code\":4031,\"message\":\"You are not authorized to access this information\",\"missingPermissions\":[\"MB.PASSENGER-CAR\",\"MB.TRUCK\",\"SMT.PASSENGER-CAR\"]}",
                              HttpConstants.HTTP_STATUS_FORBIDDEN);


            // Country
            // 1. Invalide Anfrage, weil Country leer ist
            additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiIiLCJicmFuZCI6Ik1CIiwiYnJhbmNoIjoiUCIsImNvbnRyYWN0Ijp0cnVlLCJsYW5nMSI6ImRlIiwibGFuZzIiOiJmciIsImxhbmczIjoiZXMiLCJleHAiOjk5OTk5OTk5OTksInBlcm1pc3Npb25zIjp7IlNNVCI6WyJQQVNTRU5HRVItQ0FSIl19fQ.Qm4ysORtF6G_4C9xfazP2UkJXeFXg-vmSEhIwxW_ZuU");
            executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"identCode\":\"204001\"}", additionalRequestProperties,
                              "{\"code\":4031,\"message\":\"You are not authorized to access this information\",\"missingPermissions\":[\"MB.PASSENGER-CAR\",\"MB.TRUCK\",\"SMT.PASSENGER-CAR\"]}",
                              HttpConstants.HTTP_STATUS_FORBIDDEN);

            // 2. Valide Anfrage für DE
            additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiZGUiLCJsYW5nMiI6ImZyIiwibGFuZzMiOiJlcyIsImV4cCI6OTk5OTk5OTk5OSwicGVybWlzc2lvbnMiOnsiTVlCIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlZBTiJdLCJNQiI6WyJQQVNTRU5HRVItQ0FSIl0sIlNNVCI6WyJQQVNTRU5HRVItQ0FSIl19fQ.hYuGzeEjZ4x-I8feu8pzixiw3z4_pi8qo6e1sNppyRE");
            executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"identCode\":\"204001\"}", additionalRequestProperties,
                              DWFile.get(getTestWorkingDir(), "resultIdentOnlyGermanCountryToken.txt"));

            // 3. Valide Anfrage für alle außer DE
            additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJFTiIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiZGUiLCJsYW5nMiI6ImZyIiwibGFuZzMiOiJlcyIsImV4cCI6OTk5OTk5OTk5OSwicGVybWlzc2lvbnMiOnsiTVlCIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlZBTiJdLCJNQiI6WyJQQVNTRU5HRVItQ0FSIl0sIlNNVCI6WyJQQVNTRU5HRVItQ0FSIl19fQ.Y3BABrd7EGSmoS3BZaYwcBy75gxCMrmy0pN29mjrHdE");
            executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"identCode\":\"204001\"}", additionalRequestProperties,
                              DWFile.get(getTestWorkingDir(), "resultIdentAllButGermanCountryToken.txt"));


            //--------------------Admin-Optionen auf "false"----------------------

            writeBooleanConfigValues(pluginConfig, false, iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_PERMISSIONS,
                                     iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_COUNTRY_VALIDITY);

            clearWebservicePluginsCaches(); // Caches müssen gelöscht werden, damit die Admin-Änderung auch auf jeden Fall berücksichtigt wird

            // Marke und Produktgruppe
            // 1. Valide Anfrage, aber jetzt mit mehr Ergebnissen (Keine Eingrenzung auf Permissions)
            additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJFTiIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiZGUiLCJsYW5nMiI6ImZyIiwibGFuZzMiOiJlcyIsImV4cCI6OTk5OTk5OTk5OSwicGVybWlzc2lvbnMiOnsiU01UIjpbIlBBU1NFTkdFUi1DQVIiXX19.zt9aA-2EpTdvaifJFTGWbKiZTzgtcdsJLUzm0d91xoA");
            executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"identCode\":\"204001\"}", additionalRequestProperties,
                              DWFile.get(getTestWorkingDir(), "resultIdentOnlySmartBrandTokenInactivePermissionsCheck.txt"));

            // 2. Valide Anfrage. Permissions sind zwar leer, aber der Permissions Check ist ausgeschaltet
            additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiZGUiLCJsYW5nMiI6ImZyIiwibGFuZzMiOiJlcyIsImV4cCI6OTk5OTk5OTk5OSwicGVybWlzc2lvbnMiOnt9fQ.8vwKzS_cX4EljkSU9IN0dNKdQi_fetITDhC3QzGf9ew");
            executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"identCode\":\"204001\"}", additionalRequestProperties,
                              DWFile.get(getTestWorkingDir(), "resultIdentEmptyPermissionsInactivePermissionsCheck.txt"));


            // Country
            // 1. Valide Anfrage, obwohl Country im Token leer ist (Country Check ist abgeschaltet)
            additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiIiLCJicmFuZCI6Ik1CIiwiYnJhbmNoIjoiUCIsImNvbnRyYWN0Ijp0cnVlLCJsYW5nMSI6ImRlIiwibGFuZzIiOiJmciIsImxhbmczIjoiZXMiLCJleHAiOjk5OTk5OTk5OTksInBlcm1pc3Npb25zIjp7IlNNVCI6WyJQQVNTRU5HRVItQ0FSIl19fQ.Qm4ysORtF6G_4C9xfazP2UkJXeFXg-vmSEhIwxW_ZuU");
            executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"identCode\":\"204001\"}", additionalRequestProperties,
                              DWFile.get(getTestWorkingDir(), "resultIdentEmptyCountryInactiveCountryCheck.txt"));

            // 2. Valide Anfrage für DE. Hier wird jetzt aber auch das "C204 NICHT_DE" Produkt zurückgeliefert, weil
            // der Country-Check abgeschaltet ist
            additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiZGUiLCJsYW5nMiI6ImZyIiwibGFuZzMiOiJlcyIsImV4cCI6OTk5OTk5OTk5OSwicGVybWlzc2lvbnMiOnsiTVlCIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlZBTiJdLCJNQiI6WyJQQVNTRU5HRVItQ0FSIl0sIlNNVCI6WyJQQVNTRU5HRVItQ0FSIl19fQ.hYuGzeEjZ4x-I8feu8pzixiw3z4_pi8qo6e1sNppyRE");
            executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"identCode\":\"204001\"}", additionalRequestProperties,
                              DWFile.get(getTestWorkingDir(), "resultIdentOnlyGermanCountryTokenInactiveCountryCheck.txt"));

            // 3. Valide Anfrage für alle außer DE. Hier wird jetzt aber auch das "C204 DE" Produkt zurückgeliefert, weil
            // der Country-Check abgeschaltet ist
            additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJFTiIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiZGUiLCJsYW5nMiI6ImZyIiwibGFuZzMiOiJlcyIsImV4cCI6OTk5OTk5OTk5OSwicGVybWlzc2lvbnMiOnsiTVlCIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlZBTiJdLCJNQiI6WyJQQVNTRU5HRVItQ0FSIl0sIlNNVCI6WyJQQVNTRU5HRVItQ0FSIl19fQ.Y3BABrd7EGSmoS3BZaYwcBy75gxCMrmy0pN29mjrHdE");
            executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"identCode\":\"204001\"}", additionalRequestProperties,
                              DWFile.get(getTestWorkingDir(), "resultIdentAllButGermanCountryTokenInactiveCountryCheck.txt"));
        } finally {
            writeBooleanConfigValues(pluginConfig, oldCheckPermissions, iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_PERMISSIONS);
            writeBooleanConfigValues(pluginConfig, oldCheckCountryValidity, iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_COUNTRY_VALIDITY);
            clearWebservicePluginsCaches(); // Caches müssen gelöscht werden, damit die Admin-Änderung auch auf jeden Fall berücksichtigt wird
        }
    }

    // Explizite Tests für die Webservice-Header-Attribute für Berechtigungen inkl. Tests für den Webservice-Token als Fallback
    public void testTokenAndHeaderAttributePermissions() {
        // Für diesen Test den Fallback auf die UserInfo im Payload deaktivieren (ist ja sowieso nur noch im DEVELOPMENT-Modus gültig)
        UniversalConfiguration pluginConfig = iPartsWebservicePlugin.getPluginConfig();
        ConfigBase config = pluginConfig.getConfig();
        boolean oldUserInPayloadFallback = config.getBoolean(pluginConfig.getPath() + iPartsWebservicePlugin.CONFIG_USER_IN_PAYLOAD_FALLBACK.getKey(),
                                                             false);
        boolean oldHeaderAttributesForPermissions = config.getBoolean(pluginConfig.getPath() + iPartsWebservicePlugin.CONFIG_HEADER_ATTRIBUTES_FOR_PERMISSIONS.getKey(),
                                                                      false);
        boolean oldCheckPermissions = config.getBoolean(pluginConfig.getPath() + iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_PERMISSIONS.getKey(),
                                                        false);
        boolean oldCheckCountryValidity = config.getBoolean(pluginConfig.getPath() + iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_COUNTRY_VALIDITY.getKey(),
                                                            false);
        try {
            //--------------------Admin-Optionen auf "false"----------------------
            writeBooleanConfigValues(pluginConfig, false, iPartsWebservicePlugin.CONFIG_USER_IN_PAYLOAD_FALLBACK,
                                     iPartsWebservicePlugin.CONFIG_HEADER_ATTRIBUTES_FOR_PERMISSIONS);
            clearWebservicePluginsCaches(); // Caches müssen gelöscht werden, damit die Admin-Änderung auch auf jeden Fall berücksichtigt wird

            // Token im Header muss funktionieren bei inaktiven Header-Attributen
            internalTestTokenForAllWebservices();

            // Header-Attribute müssen ignoriert werden falls vorhanden (weil deaktiviert im Admin-Modus)
            Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJFTiIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiZGUiLCJsYW5nMiI6ImZyIiwibGFuZzMiOiJlcyIsImV4cCI6OTk5OTk5OTk5OSwicGVybWlzc2lvbnMiOnsiU01UIjpbIlBBU1NFTkdFUi1DQVIiXX19.zt9aA-2EpTdvaifJFTGWbKiZTzgtcdsJLUzm0d91xoA");
            additionalRequestProperties.putAll(createHeaderAttributesForPermissions("IT", ".", "it", "SB"));
            executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"identCode\":\"204001\"}", additionalRequestProperties,
                              DWFile.get(getTestWorkingDir(), "resultIdentOnlySmartBrandToken.txt"));

            // Fallback auf Token im Header muss weiterhin funktionieren bei aktiven Header-Attributen
            writeBooleanConfigValues(pluginConfig, true, iPartsWebservicePlugin.CONFIG_HEADER_ATTRIBUTES_FOR_PERMISSIONS);
            clearWebservicePluginsCaches(); // Caches müssen gelöscht werden, damit die Admin-Änderung auch auf jeden Fall berücksichtigt wird
            internalTestTokenForAllWebservices();

            // Ab hier Tests mit Header-Attributen für die Berechtigungen und einem angepassten Token für Italienisch und
            // anderen Permissions (das Token muss ignoriert werden bei vorhandenen Header-Attributen für Berechtigungen)
            writeBooleanConfigValues(pluginConfig, true, iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_PERMISSIONS,
                                     iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_COUNTRY_VALIDITY);
            clearWebservicePluginsCaches(); // Caches müssen gelöscht werden, damit die Admin-Änderung auch auf jeden Fall berücksichtigt wird

            // Marke und Produktgruppe
            // 1. Valide Anfrage bei der die Permissions gültig sind
            additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJJVCIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiaXQiLCJsYW5nMiI6IiIsImxhbmczIjoiIiwiZXhwIjoxNTkxODkwNDM0LCJwZXJtaXNzaW9ucyI6e319.5JivnBT1dAIhqAYSqUnIn2929pXSrfoCujlnF1b2Nwc");
            additionalRequestProperties.putAll(createHeaderAttributesForPermissions("EN", "SMT.PASSENGER-CAR", "de, fr, es", "SB"));
            executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"identCode\":\"204001\"}", additionalRequestProperties,
                              DWFile.get(getTestWorkingDir(), "resultIdentOnlySmartBrandToken.txt"));

            // 2. Invalide Anfrage, weil die Permissions leer sind (bzw. nur ungültige Daten enthalten, weil bei leer der
            // Fallback auf das Token durchgeführt werden würde)
            additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJJVCIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiaXQiLCJsYW5nMiI6IiIsImxhbmczIjoiIiwiZXhwIjoxNTkxODkwNDM0LCJwZXJtaXNzaW9ucyI6e319.5JivnBT1dAIhqAYSqUnIn2929pXSrfoCujlnF1b2Nwc");
            additionalRequestProperties.putAll(createHeaderAttributesForPermissions("DE", ".", "de, fr, es", "SB"));
            executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"identCode\":\"204001\"}", additionalRequestProperties,
                              "{\"code\":4031,\"message\":\"You are not authorized to access this information\",\"missingPermissions\":[\"MB.PASSENGER-CAR\",\"MB.TRUCK\",\"SMT.PASSENGER-CAR\"]}",
                              HttpConstants.HTTP_STATUS_FORBIDDEN);


            // Country
            // 1. Valide Anfrage für DE
            additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJJVCIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiaXQiLCJsYW5nMiI6IiIsImxhbmczIjoiIiwiZXhwIjoxNTkxODkwNDM0LCJwZXJtaXNzaW9ucyI6e319.5JivnBT1dAIhqAYSqUnIn2929pXSrfoCujlnF1b2Nwc");
            additionalRequestProperties.putAll(createHeaderAttributesForPermissions("DE", "MYB.PASSENGER-CAR, MYB.TRUCK, MYB.VAN, MB.PASSENGER-CAR, SMT.PASSENGER-CAR",
                                                                                    "de, fr, es", "SB"));
            executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"identCode\":\"204001\"}", additionalRequestProperties,
                              DWFile.get(getTestWorkingDir(), "resultIdentOnlyGermanCountryToken.txt"));

            // 2. Valide Anfrage für alle außer DE
            additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJJVCIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiaXQiLCJsYW5nMiI6IiIsImxhbmczIjoiIiwiZXhwIjoxNTkxODkwNDM0LCJwZXJtaXNzaW9ucyI6e319.5JivnBT1dAIhqAYSqUnIn2929pXSrfoCujlnF1b2Nwc");
            additionalRequestProperties.putAll(createHeaderAttributesForPermissions("EN", "MYB.PASSENGER-CAR, MYB.TRUCK, MYB.VAN, MB.PASSENGER-CAR, SMT.PASSENGER-CAR",
                                                                                    "de, fr, es", "SB"));
            executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"identCode\":\"204001\"}", additionalRequestProperties,
                              DWFile.get(getTestWorkingDir(), "resultIdentAllButGermanCountryToken.txt"));


            // Und nochmal ganz ohne Token
            additionalRequestProperties = createHeaderAttributesForPermissions("EN", "SMT.PASSENGER-CAR", "de, fr, es", "SB");
            executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"identCode\":\"204001\"}", additionalRequestProperties,
                              DWFile.get(getTestWorkingDir(), "resultIdentOnlySmartBrandToken.txt"));

            // Und mit unvollständigen Header-Attributen mit Token als Fallback
            additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJFTiIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiZGUiLCJsYW5nMiI6ImZyIiwibGFuZzMiOiJlcyIsImV4cCI6OTk5OTk5OTk5OSwicGVybWlzc2lvbnMiOnsiU01UIjpbIlBBU1NFTkdFUi1DQVIiXX19.zt9aA-2EpTdvaifJFTGWbKiZTzgtcdsJLUzm0d91xoA");
            additionalRequestProperties.putAll(createHeaderAttributesForPermissions("IT", "", "it", "SB"));
            executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"identCode\":\"204001\"}", additionalRequestProperties,
                              DWFile.get(getTestWorkingDir(), "resultIdentOnlySmartBrandToken.txt"));

            // Berechtigungen für alle Webservices testen
            Map<String, String> requestPropertiesEmptyPermissions = createHeaderAttributesForPermissions("DE", ".", "de, fr, es", "SB");
            Map<String, String> requestPropertiesValidPermissions = createHeaderAttributesForPermissions("DE", "MB.PASSENGER-CAR, MB.TRUCK, MB.UNIMOG, MB.VAN, MB.BUS, SMT.PASSENGER-CAR, MYB.PASSENGER-CAR",
                                                                                                         "de, fr, es", "SB");
            internalTestPermissionsForAllWebservices(requestPropertiesEmptyPermissions, requestPropertiesValidPermissions);
        } finally {
            writeBooleanConfigValues(pluginConfig, oldUserInPayloadFallback, iPartsWebservicePlugin.CONFIG_USER_IN_PAYLOAD_FALLBACK);
            writeBooleanConfigValues(pluginConfig, oldHeaderAttributesForPermissions, iPartsWebservicePlugin.CONFIG_HEADER_ATTRIBUTES_FOR_PERMISSIONS);
            writeBooleanConfigValues(pluginConfig, oldCheckPermissions, iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_PERMISSIONS);
            writeBooleanConfigValues(pluginConfig, oldCheckCountryValidity, iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_COUNTRY_VALIDITY);
            clearWebservicePluginsCaches(); // Caches müssen gelöscht werden, damit die Admin-Änderung auch auf jeden Fall berücksichtigt wird
        }
    }

    private Map<String, String> createHeaderAttributesForPermissions(String country, String permissions, String languages, String issuer) {
        Map<String, String> headerAttributes = new HashMap<>(4);
        headerAttributes.put(iPartsWSAbstractEndpoint.HEADER_ATTR_COUNTRY, country);
        headerAttributes.put(iPartsWSAbstractEndpoint.HEADER_ATTR_PERMISSIONS, permissions);
        headerAttributes.put(iPartsWSAbstractEndpoint.HEADER_ATTR_LANGUAGES, languages);
        headerAttributes.put(iPartsWSAbstractEndpoint.HEADER_ATTR_ISSUER, issuer);
        return headerAttributes;
    }

    // Tests für WebService: [GetNavOpts]
    public void testGetNavOptsForProductDefault() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C204002\",\"productId\":\"C204\"},\"user\":{\"country\":\"200\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultNavOptsForProductDefault.txt"));
    }

    public void testGetNavOptsForKgTuKgGerman() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C204001\",\"productId\":\"C204_KGTU\"},\"navContext\":[{\"id\":\"29\",\"type\":\"cg_group\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"nextNodes\":[{\"id\":\"015\",\"label\":\"PEDALANLAGE MIT LAGERUNG\",\"partsAvailable\":true,\"thumbNails\":[{\"href\":\"/parts/media/drawing_B29015000211.tif\",\"id\":\"drawing_B29015000211.tif\",\"previewHref\":\"/parts/media/previews/drawing_B29015000211.tif\"},{\"href\":\"/parts/media/drawing_B29015000218.tif\",\"id\":\"drawing_B29015000218.tif\",\"previewHref\":\"/parts/media/previews/drawing_B29015000218.tif\"}],\"type\":\"cg_subgroup\"},{\"id\":\"030\",\"label\":\"PEDALANLAGE-HYDRAULIK\",\"partsAvailable\":true,\"thumbNails\":[{\"href\":\"/parts/media/drawing_B29030000097.tif\",\"id\":\"drawing_B29030000097.tif\",\"previewHref\":\"/parts/media/previews/drawing_B29030000097.tif\"},{\"href\":\"/parts/media/drawing_B29030000101.tif\",\"id\":\"drawing_B29030000101.tif\",\"previewHref\":\"/parts/media/previews/drawing_B29030000101.tif\"}],\"type\":\"cg_subgroup\"}]}");
    }

    public void testGetNavOptsForKgTuModulesGerman() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C204001\",\"productId\":\"C204_KGTU\"},\"navContext\":[{\"id\":\"29\",\"type\":\"cg_group\"},{\"id\":\"015\",\"type\":\"cg_subgroup\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"nextNodes\":[{\"id\":\"C204_29_15\",\"label\":\"PEDALANLAGE MIT LAGERUNG\",\"partsAvailable\":true,\"thumbNails\":[{\"href\":\"/parts/media/drawing_B29015000211.tif\",\"id\":\"drawing_B29015000211.tif\",\"previewHref\":\"/parts/media/previews/drawing_B29015000211.tif\"},{\"href\":\"/parts/media/drawing_B29015000218.tif\",\"id\":\"drawing_B29015000218.tif\",\"previewHref\":\"/parts/media/previews/drawing_B29015000218.tif\"}],\"type\":\"module\"}]}");
    }

    public void testGetNavOptsForEinPasHgGerman() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C204001\",\"productId\":\"C204\"},\"navContext\":[{\"type\":\"maingroup\",\"id\":\"21\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"nextNodes\":[{\"id\":\"15\",\"label\":\"Betriebsbremse\",\"type\":\"group\"}]}");
    }

    public void testGetNavOptsForEinPasModulesGerman() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C204001\",\"productId\":\"C204\"},\"navContext\":[{\"type\":\"maingroup\",\"id\":\"21\"},{\"id\":\"15\",\"type\":\"group\"},{\"id\":\"30\",\"type\":\"subgroup\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"nextNodes\":[{\"id\":\"C204_21_15_30_00001\",\"label\":\"Hauptbremszylinder/Behälter\",\"partsAvailable\":true,\"type\":\"module\"}]}");
    }

    public void testGetNavOptsForLooseSaWODatacard() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C963003\",\"productId\":\"S01\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"58\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"nextNodes\":[{\"id\":\"030\",\"label\":\"WERKZEUG UND ZUBEHOER\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"},{\"id\":\"050\",\"label\":\"TYPSCHILD,HINWEISSCHILDER\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"},{\"id\":\"300\",\"label\":\"BETRIEBS-/BEDIENANLEITUNG,WAGENPAPIERTASCHE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"},{\"id\":\"330\",\"label\":\"SERVICEHEFT/WARTUNGSHEFT\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"},{\"id\":\"380\",\"label\":\"BEDIENANLEITUNG FUNKGERAET\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}]}");
    }

    public void testGetNavOptsForLooseSaWithDatacard() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C963003\",\"fin\":\"WDB9630031L738999\",\"productId\":\"S01\",\"datacardExists\":\"true\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"58\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"nextNodes\":[{\"id\":\"030\",\"label\":\"WERKZEUG UND ZUBEHOER\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"},{\"id\":\"050\",\"label\":\"TYPSCHILD,HINWEISSCHILDER\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"},{\"id\":\"300\",\"label\":\"BETRIEBS-/BEDIENANLEITUNG,WAGENPAPIERTASCHE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"},{\"id\":\"330\",\"label\":\"SERVICEHEFT/WARTUNGSHEFT\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"},{\"id\":\"380\",\"label\":\"BEDIENANLEITUNG FUNKGERAET\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"},{\"id\":\"Z 506.239\",\"label\":\"SV WERKZEUG UND ZUBEHOER\",\"partsAvailable\":true,\"saaNavNodeInfos\":[{\"code\":\"Z 506.239/17\",\"description\":\"FAHRERHAUS KURZ (CODE F07)                FAHRERHAUS MITTELLANG (CODE F05)          FAHRERHAUS LANG (CODE F04)\"},{\"code\":\"Z 506.239/24\",\"description\":\"385/65 R 22,5\\\"\"},{\"code\":\"Z 506.239/40\",\"description\":\"FAHRERHAUS KURZ (CODE F07)                FAHRERHAUS MITTELLANG (CODE F05)          FAHRERHAUS LANG (CODE F04)\"},{\"code\":\"Z 506.239/57\",\"description\":\"SV WERKZEUG UND ZUBEHOER\"}],\"type\":\"sa_number\"},{\"id\":\"Z 506.241\",\"label\":\"SV TYPSCHILD\",\"partsAvailable\":true,\"saaNavNodeInfos\":[{\"code\":\"Z 506.241/56\",\"description\":\"EG\"}],\"type\":\"sa_number\"},{\"id\":\"Z 506.249\",\"label\":\"SV WAGENPAPIERTASCHE\",\"partsAvailable\":true,\"saaNavNodeInfos\":[{\"code\":\"Z 506.249/71\",\"description\":\"\"},{\"code\":\"Z 506.249/83\",\"description\":\"WARTUNGSHEFT DEUTSCH\"}],\"type\":\"sa_number\"},{\"id\":\"Z 506.256\",\"label\":\"SV WAGENHEBER\",\"partsAvailable\":true,\"saaNavNodeInfos\":[{\"code\":\"Z 506.256/09\",\"description\":\"SV WAGENHEBER\"},{\"code\":\"Z 506.256/14\",\"description\":\"12T                                       UNTERSTELLHOEHE 230\"}],\"type\":\"sa_number\"},{\"id\":\"Z 506.257\",\"label\":\"SV WARNDREIECK\",\"partsAvailable\":true,\"saaNavNodeInfos\":[{\"code\":\"Z 506.257/06\",\"description\":\"GEBRA\"},{\"code\":\"Z 506.257/07\",\"description\":\"\"}],\"type\":\"sa_number\"},{\"id\":\"Z 506.269\",\"label\":\"SV SCHILD FUER NACHTFAHRVERBOT\",\"partsAvailable\":true,\"saaNavNodeInfos\":[{\"code\":\"Z 506.269/19\",\"description\":\"L-SCHILD\"}],\"type\":\"sa_number\"},{\"id\":\"Z 506.359\",\"label\":\"SV REIFENFUELLSCHLAUCH\",\"partsAvailable\":true,\"saaNavNodeInfos\":[{\"code\":\"Z 506.359/04\",\"description\":\"SV REIFENFUELLSCHLAUCH\"}],\"type\":\"sa_number\"},{\"id\":\"Z 506.389\",\"label\":\"SV WARNLEUCHTE\",\"partsAvailable\":true,\"saaNavNodeInfos\":[{\"code\":\"Z 506.389/01\",\"description\":\"GROSS                                     OHNE GGVS\"}],\"type\":\"sa_number\"}]}");
    }

    public void testGetNavOptsForSAWithNavContextAndModel() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C963003\",\"productId\":\"S01\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"58\"},{\"type\":\"sa_number\",\"id\":\"Z 506.239\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"nextNodes\":[]}");
    }

    public void testGetNavOptsForSAWithNavContextAndDatacard() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C963003\",\"fin\":\"WDB9630031L738999\",\"productId\":\"S01\",\"datacardExists\":\"true\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"58\"},{\"type\":\"sa_number\",\"id\":\"Z 506.239\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"nextNodes\":[{\"id\":\"SA-Z506239\",\"label\":\"SV WERKZEUG UND ZUBEHOER\",\"partsAvailable\":true,\"type\":\"module\"}]}");
    }

    public void testGetNavOptsForInvisibleSA() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C967002\",\"productId\":\"S10\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"31\"},{\"type\":\"sa_number\",\"id\":\"Z M03.379\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"nextNodes\":[]}");
    }

    public void testGetNavOptsErrorEmpty() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{}",
                          "{\"code\":4000,\"message\":\"Attribute 'user' is missing\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testGetNavOptsErrorMissingParameter() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"}}",
                          "{\"code\":4000,\"message\":\"Attribute 'identContext' is missing\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testGetNavOptsSAfromDatacardOnly() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C963003\",\"productId\":\"S01\",\"fin\":\"WDB9630031L738998\",\"datacardExists\":true,\"filterOptions\":{\"model\":true}},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"41\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"nextNodes\":[{\"id\":\"030\",\"label\":\"GELENKWELLE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"},{\"id\":\"Z M02.804\",\"label\":\"GELENKWELLE\",\"partsAvailable\":true,\"saaNavNodeInfos\":[{\"code\":\"Z M02.804/02\",\"description\":\"GELENKWELLE\"}],\"type\":\"sa_number\"},{\"id\":\"Z M03.300\",\"label\":\"GELENKWELLE\",\"partsAvailable\":true,\"type\":\"sa_number\"}]}");
    }

    public void testGetNavOptsSAfromDatacardOnlyNoDatacard() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C963003\",\"productId\":\"S01\",\"fin\":\"WDB9630031L738998\",\"datacardExists\":false,\"filterOptions\":{\"model\":true}},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"41\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"nextNodes\":[{\"id\":\"030\",\"label\":\"GELENKWELLE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}]}");
    }

    public void testGetNavOptsSAfromDatacardWithDatacardFilter() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C963003\",\"productId\":\"S01\",\"fin\":\"WDB9630031L738998\",\"datacardExists\":true,\"filterOptions\":{\"model\":true, \"datacard\":true}},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"41\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"nextNodes\":[{\"id\":\"030\",\"label\":\"GELENKWELLE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"},{\"id\":\"Z M02.804\",\"label\":\"GELENKWELLE\",\"partsAvailable\":true,\"saaNavNodeInfos\":[{\"code\":\"Z M02.804/02\",\"description\":\"GELENKWELLE\"}],\"type\":\"sa_number\"}]}");
    }

    public void testGetNavOptsWithPictureCodeFilter() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205140\",\"productId\":\"C12\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"24\"},{\"type\":\"cg_subgroup\",\"id\":\"015\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"nextNodes\":[{\"id\":\"C12_24_015_00001\",\"label\":\"MOTORAUFHAENGUNG\",\"partsAvailable\":true,\"thumbNails\":[{\"href\":\"/parts/media/drawing_B01045000299.tif\",\"id\":\"drawing_B01045000299.tif\",\"previewHref\":\"/parts/media/previews/drawing_B01045000299.tif\"}],\"type\":\"module\"}]}");
    }

    public void testGetNavOptsWithPictureModelFilter() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205164\",\"productId\":\"C12\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"24\"},{\"type\":\"cg_subgroup\",\"id\":\"015\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"nextNodes\":[{\"id\":\"C12_24_015_00001\",\"label\":\"MOTORAUFHAENGUNG\",\"partsAvailable\":true,\"thumbNails\":[{\"href\":\"/parts/media/drawing_B01015000377.tif\",\"id\":\"drawing_B01015000377.tif\",\"previewHref\":\"/parts/media/previews/drawing_B01015000377.tif\"},{\"href\":\"/parts/media/drawing_B01045000299.tif\",\"id\":\"drawing_B01045000299.tif\",\"previewHref\":\"/parts/media/previews/drawing_B01045000299.tif\"},{\"href\":\"/parts/media/drawing_B49290000075.tif\",\"id\":\"drawing_B49290000075.tif\",\"previewHref\":\"/parts/media/previews/drawing_B49290000075.tif\"}],\"type\":\"module\"}]}");
    }

    public void testGetNavOptsWithPictureSAAFilter() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"D715516\",\"productId\":\"01C\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"25\"},{\"type\":\"cg_subgroup\",\"id\":\"250\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"nextNodes\":[{\"id\":\"01C_25_250_00001\",\"label\":\"KUPPLUNGSGEHAEUSE UND BETAETIGUNG\",\"partsAvailable\":true,\"thumbNails\":[{\"href\":\"/parts/media/drawing_B01015000377.tif\",\"id\":\"drawing_B01015000377.tif\",\"previewHref\":\"/parts/media/previews/drawing_B01015000377.tif\"}],\"type\":\"module\"}]}");
    }

    public void testGetNavOptsWithPictureValidSAAFilter() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"D715504\",\"productId\":\"01C\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"25\"},{\"type\":\"cg_subgroup\",\"id\":\"250\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"nextNodes\":[{\"id\":\"01C_25_250_00001\",\"label\":\"KUPPLUNGSGEHAEUSE UND BETAETIGUNG\",\"partsAvailable\":true,\"thumbNails\":[{\"href\":\"/parts/media/drawing_B01015000377.tif\",\"id\":\"drawing_B01015000377.tif\",\"previewHref\":\"/parts/media/previews/drawing_B01015000377.tif\"},{\"href\":\"/parts/media/drawing_B50330000185.tif\",\"id\":\"drawing_B50330000185.tif\",\"previewHref\":\"/parts/media/previews/drawing_B50330000185.tif\"}],\"type\":\"module\"}]}");
    }

    // Tests für GetNavOpts mit TopNodes
    public void testGetNavOptsWithTopNodesGermany() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"T\"],\"modelId\":\"C447601\",\"productId\":\"60V\"},\"user\":{\"country\":\"DE\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultGetNavOptsWithTopNodesGermany.txt"));
    }

    public void testGetNavOptsWithTopNodesArgentina() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"T\"],\"modelId\":\"C447601\",\"productId\":\"60V\"},\"user\":{\"country\":\"ar\",\"language\":\"en\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultGetNavOptsWithTopNodesArgentina.txt"));
    }


    // Tests für GetNavOpts mit Token
    public void testGetNavOptsGermanToken() {
        // Token zum Request-Header hinzufügen
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiZGUiLCJsYW5nMiI6ImZyIiwibGFuZzMiOiJlcyIsImV4cCI6MTU5MTg5MDQzNH0.lwoOePYi0gtA6Qzz7trra8U_JTqHQs0BUQahLzSS3rc");

        // absichtlich "en" im UserInfo vom POST-Payload angeben, weil dies durch "de" vom Token überlagert werden soll
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C204001\",\"productId\":\"C204_KGTU\"},\"navContext\":[{\"id\":\"29\",\"type\":\"cg_group\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          additionalRequestProperties,
                          "{\"nextNodes\":[{\"id\":\"015\",\"label\":\"PEDALANLAGE MIT LAGERUNG\",\"partsAvailable\":true,\"thumbNails\":[{\"href\":\"/parts/media/drawing_B29015000211.tif\",\"id\":\"drawing_B29015000211.tif\",\"previewHref\":\"/parts/media/previews/drawing_B29015000211.tif\"},{\"href\":\"/parts/media/drawing_B29015000218.tif\",\"id\":\"drawing_B29015000218.tif\",\"previewHref\":\"/parts/media/previews/drawing_B29015000218.tif\"}],\"type\":\"cg_subgroup\"},{\"id\":\"030\",\"label\":\"PEDALANLAGE-HYDRAULIK\",\"partsAvailable\":true,\"thumbNails\":[{\"href\":\"/parts/media/drawing_B29030000097.tif\",\"id\":\"drawing_B29030000097.tif\",\"previewHref\":\"/parts/media/previews/drawing_B29030000097.tif\"},{\"href\":\"/parts/media/drawing_B29030000101.tif\",\"id\":\"drawing_B29030000101.tif\",\"previewHref\":\"/parts/media/previews/drawing_B29030000101.tif\"}],\"type\":\"cg_subgroup\"}]}",
                          HttpConstants.HTTP_STATUS_OK);
    }

    public void testGetNavOptsEnglishTokenWithoutUser() {
        // Token zum Request-Header hinzufügen
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJHQiIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiZW4iLCJsYW5nMiI6ImZyIiwibGFuZzMiOiJlcyIsImV4cCI6OTk5OTk5OTk5OX0.Sv0pvLcyLBJXTpEeuhHhKzSCvBmLzQNusx1_tWU_Ofc");

        // absichtlich kein "user" im POST-Payload angegeben
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C204001\",\"productId\":\"C204_KGTU\"},\"navContext\":[{\"id\":\"29\",\"type\":\"cg_group\"}]}",
                          additionalRequestProperties,
                          "{\"nextNodes\":[{\"id\":\"015\",\"label\":\"PEDAL ASSEMBLY WITH BEARING\",\"partsAvailable\":true,\"thumbNails\":[{\"href\":\"/parts/media/drawing_B29015000211.tif\",\"id\":\"drawing_B29015000211.tif\",\"previewHref\":\"/parts/media/previews/drawing_B29015000211.tif\"},{\"href\":\"/parts/media/drawing_B29015000218.tif\",\"id\":\"drawing_B29015000218.tif\",\"previewHref\":\"/parts/media/previews/drawing_B29015000218.tif\"}],\"type\":\"cg_subgroup\"},{\"id\":\"030\",\"label\":\"PEDAL ASSEMBLY HYDRAULICS\",\"partsAvailable\":true,\"thumbNails\":[{\"href\":\"/parts/media/drawing_B29030000097.tif\",\"id\":\"drawing_B29030000097.tif\",\"previewHref\":\"/parts/media/previews/drawing_B29030000097.tif\"},{\"href\":\"/parts/media/drawing_B29030000101.tif\",\"id\":\"drawing_B29030000101.tif\",\"previewHref\":\"/parts/media/previews/drawing_B29030000101.tif\"}],\"type\":\"cg_subgroup\"}]}",
                          HttpConstants.HTTP_STATUS_OK);
    }

    public void testGetNavOptsMissingToken() {
        _universalGetNavOptsWithTokenError("{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C204001\",\"productId\":\"C204_KGTU\"},\"navContext\":[{\"id\":\"29\",\"type\":\"cg_group\"}]}",
                                           null, "{\"code\":4011,\"message\":\"Header '" + iPartsWebservicePlugin.getPluginConfig().getConfigValueAsString(iPartsWebservicePlugin.CONFIG_HEADER_TOKEN_NAME)
                                                 + "' missing in request\"}");
    }

    public void testGetNavOptsInvalidToken() {
        _universalGetNavOptsWithTokenError("{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C204001\",\"productId\":\"C204_KGTU\"},\"navContext\":[{\"id\":\"29\",\"type\":\"cg_group\"}]}",
                                           "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiZGUiLCJsYW5nMiI6ImZyIiwibGFuZzMiOiJlcyIsImV4cCI6MX0.Cc9h3b6IN7iCMZjRdhfBPzcXTrepzuSxDL3fWu6IPex",
                                           "{\"code\":4011,\"message\":\"Signature validation error for token 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiZGUiLCJsYW5nMiI6ImZyIiwibGFuZzMiOiJlcyIsImV4cCI6MX0.Cc9h3b6IN7iCMZjRdhfBPzcXTrepzuSxDL3fWu6IPex'\"}");
    }

    public void testGetNavOptsExpiredToken() {
        _universalGetNavOptsWithTokenError("{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C204001\",\"productId\":\"C204_KGTU\"},\"navContext\":[{\"id\":\"29\",\"type\":\"cg_group\"}]}",
                                           "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiZGUiLCJsYW5nMiI6ImZyIiwibGFuZzMiOiJlcyIsImV4cCI6MX0.Cc9h3b6IN7iCMZjRdhfBPzcXTrepzuSxDL3fWu6IPeA",
                                           "{\"code\":4012,\"message\":\"Token expired at 1970-01-01T01:00:01+0100 (1 seconds since epoch)\"}");
    }

    public void testGetNavOptsNeutralSteering() {
        // GetNavOpts mit "0" als Lenkung für zu keinem Fehler
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"datacardExists\":true,\"fin\":\"WDB1641560W150896\",\"modelId\":\"C164156\",\"productClassIds\":[\"P\"],\"productId\":\"63J\"},\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          DWFile.get(getTestWorkingDir(), "resultGetNavOptsNeutralSteering.txt"));
    }

    /**
     * Leere TUs werden ausgefiltert
     */
    public void testGetNavOptsHideEmptyTUs() {
        UniversalConfiguration pluginConfig = iPartsWebservicePlugin.getPluginConfig();
        try {
            writeBooleanConfigValues(pluginConfig, true, iPartsWebservicePlugin.CONFIG_HIDE_EMPTY_TUS_IN_RESPONSE);
            clearWebservicePluginsCaches();

            executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C967000\",\"productId\":\"S10\"}," +
                              "\"navContext\":[{\"id\":\"50\",\"type\":\"cg_group\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                              "{\"nextNodes\":[{\"id\":\"060\",\"label\":\"KUEHLER UND AUSGLEICHBEHAELTER\",\"partsAvailable\":true," +
                              "\"type\":\"cg_subgroup\"},{\"id\":\"300\",\"label\":\"KUEHLWASSERSCHLAEUCHE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}," +
                              "{\"id\":\"480\",\"label\":\"LADELUFTKUEHLUNG\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"},{\"id\":\"765\",\"label\":" +
                              "\"OELKUEHLUNG GETRIEBE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"},{\"id\":\"900\",\"label\":\"KONDENSATOR KLIMAANLAGE\"," +
                              "\"partsAvailable\":true,\"type\":\"cg_subgroup\"}]}");

        } finally {
            writeBooleanConfigValues(pluginConfig, false, iPartsWebservicePlugin.CONFIG_HIDE_EMPTY_TUS_IN_RESPONSE);
            clearWebservicePluginsCaches();
        }

    }

    /**
     * Liefert Produkte, die in dem Land valide sind, das im JWT Token überliefert wurde (hier im Test: "DE")
     * Test für valide Länder.
     */
    public void testIdentOnlyGermanCountryToken() {
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiZGUiLCJsYW5nMiI6ImZyIiwibGFuZzMiOiJlcyIsImV4cCI6OTk5OTk5OTk5OSwicGVybWlzc2lvbnMiOnsiTVlCIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlZBTiJdLCJNQiI6WyJQQVNTRU5HRVItQ0FSIl0sIlNNVCI6WyJQQVNTRU5HRVItQ0FSIl19fQ.hYuGzeEjZ4x-I8feu8pzixiw3z4_pi8qo6e1sNppyRE");
        executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identCode\":\"204001\"}", additionalRequestProperties,
                          DWFile.get(getTestWorkingDir(), "resultIdentOnlyGermanCountryToken.txt"));
    }

    /**
     * Liefert Produkte, die in dem Land valide sind, das im JWT Token überliefert wurde (hier im Test: alle außer "DE")
     * Test für nicht-valide Länder
     */
    public void testIdentAllButGermanCountryToken() {
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJFTiIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiZGUiLCJsYW5nMiI6ImZyIiwibGFuZzMiOiJlcyIsImV4cCI6OTk5OTk5OTk5OSwicGVybWlzc2lvbnMiOnsiTVlCIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlZBTiJdLCJNQiI6WyJQQVNTRU5HRVItQ0FSIl0sIlNNVCI6WyJQQVNTRU5HRVItQ0FSIl19fQ.Y3BABrd7EGSmoS3BZaYwcBy75gxCMrmy0pN29mjrHdE");
        executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identCode\":\"204001\"}", additionalRequestProperties,
                          DWFile.get(getTestWorkingDir(), "resultIdentAllButGermanCountryToken.txt"));
    }

    /**
     * Liefert aller Produkte die der Marke im JWT Token entsprechen (hier "SMT" = Smart)
     */
    public void testIdentOnlySmartBrandToken() {
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJFTiIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiZGUiLCJsYW5nMiI6ImZyIiwibGFuZzMiOiJlcyIsImV4cCI6OTk5OTk5OTk5OSwicGVybWlzc2lvbnMiOnsiU01UIjpbIlBBU1NFTkdFUi1DQVIiXX19.zt9aA-2EpTdvaifJFTGWbKiZTzgtcdsJLUzm0d91xoA");
        executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identCode\":\"204001\"}", additionalRequestProperties,
                          DWFile.get(getTestWorkingDir(), "resultIdentOnlySmartBrandToken.txt"));
    }

    /**
     * Liefert alle Produkte die gegenüber der Kombination Marke: "Mercedes-Benz" und Sparte: "Truck" gültig sind
     */
    public void testIdentTruckBranchToken() {
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJFTiIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiZGUiLCJsYW5nMiI6ImZyIiwibGFuZzMiOiJlcyIsImV4cCI6OTk5OTk5OTk5OSwicGVybWlzc2lvbnMiOnsiTUIiOlsiVFJVQ0siXX19.sPkgQLCRAhOoSYvW6W-Ztg-lUcxR2mNyEqDaQ4h5VCU");
        executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identCode\":\"204001\"}", additionalRequestProperties,
                          DWFile.get(getTestWorkingDir(), "resultIdentTruckBranchToken.txt"));
    }

    public void testIdentEmptyPermissionsToken() {
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiZGUiLCJsYW5nMiI6ImZyIiwibGFuZzMiOiJlcyIsImV4cCI6OTk5OTk5OTk5OSwicGVybWlzc2lvbnMiOnt9fQ.8vwKzS_cX4EljkSU9IN0dNKdQi_fetITDhC3QzGf9ew");
        executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identCode\":\"204001\"}", additionalRequestProperties,
                          "{\"code\":4031,\"message\":\"You are not authorized to access this information\",\"missingPermissions\":[\"MB.PASSENGER-CAR\",\"MB.TRUCK\",\"SMT.PASSENGER-CAR\"]}",
                          HttpConstants.HTTP_STATUS_FORBIDDEN);
    }

    public void testIdentEmptyCountryToken() {
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiIiLCJicmFuZCI6Ik1CIiwiYnJhbmNoIjoiUCIsImNvbnRyYWN0Ijp0cnVlLCJsYW5nMSI6ImRlIiwibGFuZzIiOiJmciIsImxhbmczIjoiZXMiLCJleHAiOjk5OTk5OTk5OTksInBlcm1pc3Npb25zIjp7IlNNVCI6WyJQQVNTRU5HRVItQ0FSIl19fQ.Qm4ysORtF6G_4C9xfazP2UkJXeFXg-vmSEhIwxW_ZuU");
        executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identCode\":\"204001\"}", additionalRequestProperties,
                          "{\"code\":4031,\"message\":\"You are not authorized to access this information\",\"missingPermissions\":[\"MB.PASSENGER-CAR\",\"MB.TRUCK\",\"SMT.PASSENGER-CAR\"]}",
                          HttpConstants.HTTP_STATUS_FORBIDDEN);
    }

    public void testIdentWithoutFinForRMIPart1() {
        // DAIMLER-10033 Bei RMI ist nur ein Aufruf mit FIN/VIN erlaubt

        Map<UniversalConfigOption, Boolean> configOptions = new HashMap<>();
        configOptions.put(iPartsWebservicePlugin.CONFIG_ONLY_FIN_BASED_REQUESTS_RMI, true);
        configOptions.put(iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_PERMISSIONS, false);
        executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), configOptions, new WebserviceRunnable() {
            @Override
            public void executeTest() {
                // Prüfung MIT "Nur Anfragen mit FIN/VIN und Datenkarte für RMI Typzulassung verarbeiten"
                // soll "Datacard not found" liefern.
                executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                  "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"963021\"}",
                                  "{\"code\":4041,\"message\":\"Datacard not found\"}", HttpConstants.HTTP_STATUS_NOT_FOUND);

                // Diese Datenkarte exisitiert nicht auf v1. Es darf kein Baumuster Fallback passieren
                clearWebservicePluginsCaches();
                executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                  "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"WDB9670211L867694\"}",
                                  "{\"code\":4041,\"message\":\"Datacard not found\"}", HttpConstants.HTTP_STATUS_NOT_FOUND);


            }
        });
    }

    public void testIdentWithoutFinForRMIPart2() {
        // DAIMLER-10033 Bei RMI ist nur ein Aufruf mit FIN/VIN erlaubt
        Map<UniversalConfigOption, Boolean> configOptions = new HashMap<>();
        configOptions.put(iPartsWebservicePlugin.CONFIG_ONLY_FIN_BASED_REQUESTS_RMI, false);
        configOptions.put(iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_PERMISSIONS, false);
        executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), configOptions, new WebserviceRunnable() {
            @Override
            public void executeTest() {
                // Gleicher Request, Prüfung OHNE "Nur Anfragen mit FIN/VIN und Datenkarte für RMI Typzulassung verarbeiten"
                // muss ein gültiges Ergebnis liefern.
                executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI, "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"963021\"}",
                                  DWFile.get(getTestWorkingDir(), "resultOnlyFinBasedRequestIdent.txt"));


            }
        });
    }

    private Map<String, String> createAdditionalRequestPropertiesForToken(String token) {
        Map<String, String> additionalRequestProperties = new HashMap<String, String>(1);
        String authorizationHeader = iPartsWebservicePlugin.getPluginConfig().getConfigValueAsString(iPartsWebservicePlugin.CONFIG_HEADER_TOKEN_TYPE)
                                     + " " + token;
        additionalRequestProperties.put(iPartsWebservicePlugin.getPluginConfig().getConfigValueAsString(iPartsWebservicePlugin.CONFIG_HEADER_TOKEN_NAME),
                                        authorizationHeader);
        return additionalRequestProperties;
    }

    private void _universalGetNavOptsWithTokenError(String requestString, String token, String expectedResponseString) {
        // Temporär den Fallback für "user" im POST-Payload deaktivieren, um die Auswertung vom Token zu erzwingen
        UniversalConfiguration pluginConfig = iPartsWebservicePlugin.getPluginConfig();
        writeBooleanConfigValues(pluginConfig, false, iPartsWebservicePlugin.CONFIG_USER_IN_PAYLOAD_FALLBACK);

        try {
            // Token zum Request-Header hinzufügen falls vorhanden
            Map<String, String> additionalRequestProperties = null;
            if (token != null) {
                additionalRequestProperties = createAdditionalRequestPropertiesForToken(token);
            }

            executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI, requestString, additionalRequestProperties,
                              expectedResponseString, HttpConstants.HTTP_STATUS_UNAUTHORIZED);
        } finally {
            writeBooleanConfigValues(pluginConfig, true, iPartsWebservicePlugin.CONFIG_USER_IN_PAYLOAD_FALLBACK);
        }
    }


    public void executeTestWithBooleanConfigChanges(UniversalConfiguration config, UniversalConfigOption configOption, boolean configValue,
                                                    WebserviceRunnable testRunnable) {
        Map<UniversalConfigOption, Boolean> configOptions = new HashMap<>();
        configOptions.put(configOption, configValue);
        executeTestWithBooleanConfigChanges(config, configOptions, testRunnable);
    }

    /**
     * Convenience Methode, die das setzen von Boolean Config Schaltern in einem try, finally Block für Unittests übernimmt.
     * Die WebservicePluginsCaches werden nur gelöscht wenn es auch tatsächliche Config Änderungen gab
     *
     * @param config        Die Config die manipuliert werden soll (z.B.: iPartsWebservicePlugin.getPluginConfig())
     * @param configOptions Map aus ConfigOption und Boolean Wert
     * @param testRunnable  Aufruf des eigentlichen Webservice Tests
     */
    public void executeTestWithBooleanConfigChanges(UniversalConfiguration config, Map<UniversalConfigOption, Boolean> configOptions,
                                                    WebserviceRunnable testRunnable) {
        Map<UniversalConfigOption, Boolean> oldConfigOptions = new HashMap<>();
        Map<UniversalConfigOption, Boolean> diffConfigOptions = new HashMap<>();
        for (Map.Entry<UniversalConfigOption, Boolean> entry : configOptions.entrySet()) {
            boolean readConfigValue = config.getConfigValueAsBoolean(entry.getKey());
            if (readConfigValue != entry.getValue()) {
                diffConfigOptions.put(entry.getKey(), entry.getValue());
                oldConfigOptions.put(entry.getKey(), readConfigValue);
            }
        }

        try {
            if (!diffConfigOptions.isEmpty()) {
                for (Map.Entry<UniversalConfigOption, Boolean> entry : diffConfigOptions.entrySet()) {
                    writeBooleanConfigValues(config, entry.getValue(), entry.getKey());
                }
                // Caches müssen gelöscht werden, damit die Admin-Änderung auch auf jeden Fall berücksichtigt wird
                clearWebservicePluginsCaches();
            }

            testRunnable.executeTest();
        } finally {
            if (!diffConfigOptions.isEmpty()) {
                for (Map.Entry<UniversalConfigOption, Boolean> entry : oldConfigOptions.entrySet()) {
                    writeBooleanConfigValues(config, entry.getValue(), entry.getKey());
                }
                clearWebservicePluginsCaches();
            }
        }
    }

    // Dieser Test passt thematisch nicht zu den WebServices, es rentiert sich aber nicht für das Checken eines Enums eine eigene Testunit anzulegen.
    // Es soll über einen Unittest sichergestellt werden, dass das komplexe Enum iPartsModuleTypes in der Eigenschaft: dbValue keine doppelten Werte enthält.
    public void testIpartsModuleTypesEnum() {
        // Zu jedem Element die vorhandenen dbValues zählen
        int errorCounter = 0;
        for (iPartsModuleTypes searchModuleType : iPartsModuleTypes.values()) {
            int counter = 0;
            for (iPartsModuleTypes iteratorModuleType : iPartsModuleTypes.values()) {
                if (searchModuleType.getDbValue().equals(iteratorModuleType.getDbValue())) {
                    counter++;
                }
            }
            // Wenn mehr als ein Wert für ein dbValue gefunden werden, wird ein Fehler ausgegeben.
            if (!Utils.objectEquals(1, counter)) {
                System.err.println("Multiple values for iPartsModuleTypes.dbValue: [" + searchModuleType.getDbValue() + "] are not allowed!");
                errorCounter++;
            }
        }
        assertEquals(0, errorCounter);
    }

    // Tests für WebService: [GetParts]
    public void testGetPartsWithAdditionalPartInfomation() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205002\",\"productId\":\"C01\",\"filterOptions\":{}},\"navContext\":[{\"id\":\"62\",\"type\":\"cg_group\"},{\"id\":\"060\",\"type\":\"cg_subgroup\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultPartsWithAdditionalPartInfomation.txt"));
    }

    public void testGetPartsForKgTuGerman() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C204001\",\"productId\":\"C204_KGTU\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"24\"},{\"type\":\"cg_subgroup\",\"id\":\"015\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultPartsForKgTuGerman.txt"));
    }


    public void testGetPartsOmittedPartsFilterInModelFilterPart1() {
        // In diesem Fall soll nur das Wegfall-Teil selbst ausgefiltert werden
        String result = "{\"images\":[],\"parts\":[{\"calloutId\":\"2\",\"name\":\"LIZENZ WETTERDIENST\",\"partContext\":{\"moduleId\":\"TestOmittedPartsDIALOG_00_001_00001\",\"sequenceId\":\"00002\"},\"partNo\":\"A0000000600\",\"partNoFormatted\":\"A 000 000 06 00\",\"quantity\":\"\"},{\"calloutId\":\"2\",\"name\":\"AUSFUEHRUNGSVORSCHRIFT / (BBV / RUECKENPLATTE)\",\"partContext\":{\"moduleId\":\"TestOmittedPartsDIALOG_00_001_00001\",\"sequenceId\":\"00003\"},\"partNo\":\"A0000031199\",\"partNoFormatted\":\"A 000 003 11 99\",\"quantity\":\"\"}]}";

        // Mit aktivierten Filtern
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205013\",\"productId\":\"TestOmittedPartsDIALOG\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"00\"},{\"type\":\"cg_subgroup\",\"id\":\"001\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          result);
    }

    public void testGetPartsOmittedPartsFilterInModelFilterPart2() {
        // Mit deaktivierten Filtern werden die Wegfall-Teile zwar weiterhin ausgefiltert, aber es werden auch Baumustergültigkeiten
        // berechnet und ausgegeben -> deshalb wird hier ein anderes Ergebnis erwartet
        String result = "{\"images\":[],\"parts\":[{\"calloutId\":\"2\",\"modelValidity\":[\"C205013\"],\"name\":\"LIZENZ WETTERDIENST\",\"partContext\":{\"moduleId\":\"TestOmittedPartsDIALOG_00_001_00001\",\"sequenceId\":\"00002\"},\"partNo\":\"A0000000600\",\"partNoFormatted\":\"A 000 000 06 00\",\"quantity\":\"\"},{\"calloutId\":\"2\",\"modelValidity\":[\"C205013\"],\"name\":\"AUSFUEHRUNGSVORSCHRIFT / (BBV / RUECKENPLATTE)\",\"partContext\":{\"moduleId\":\"TestOmittedPartsDIALOG_00_001_00001\",\"sequenceId\":\"00003\"},\"partNo\":\"A0000031199\",\"partNoFormatted\":\"A 000 003 11 99\",\"quantity\":\"\"}]}";
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205013\",\"productId\":\"TestOmittedPartsDIALOG\",\"filterOptions\":{}},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"00\"},{\"type\":\"cg_subgroup\",\"id\":\"001\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          result);
    }

    public void testGetPartsOmittedPartsFilterInDatacardFilter() {
        // In diesem Fall soll das Wegfall-Teil selbst und alle Positionsvarianten entfallen (hier 2 Stück)
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205240\",\"fin\":\"WDD2052401F236482\",\"datacardExists\":\"true\",\"productId\":\"TestOmittedParts\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"00\"},{\"type\":\"cg_subgroup\",\"id\":\"010\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"images\":[],\"parts\":[{\"calloutId\":\"1\",\"name\":\"AG OHNE WANDLER\",\"partContext\":{\"moduleId\":\"TestOmittedParts_00_010_00001\",\"sequenceId\":\"00001\"},\"partNo\":\"A7202705700\",\"partNoFormatted\":\"A 720 270 57 00\",\"quantity\":\"\"},{\"calloutId\":\"2\",\"name\":\"FUEHRUNGSROHR\",\"partContext\":{\"moduleId\":\"TestOmittedParts_00_010_00001\",\"sequenceId\":\"00002\"},\"partNo\":\"A7112610043\",\"partNoFormatted\":\"A 711 261 00 43\",\"quantity\":\"\"},{\"calloutId\":\"3\",\"name\":\"AG OHNE WANDLER\",\"partContext\":{\"moduleId\":\"TestOmittedParts_00_010_00001\",\"sequenceId\":\"00003\"},\"partNo\":\"A7202705600\",\"partNoFormatted\":\"A 720 270 56 00\",\"quantity\":\"\"},{\"calloutId\":\"4\",\"name\":\"STEUEREINHEIT\",\"partContext\":{\"moduleId\":\"TestOmittedParts_00_010_00001\",\"sequenceId\":\"00004\"},\"partNo\":\"A7222708600\",\"partNoFormatted\":\"A 722 270 86 00\",\"quantity\":\"\"}]}");
    }

    public void testGetPartsForKgTuWithOmittedPartsInvalid() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205002\",\"productId\":\"TestOmittedParts\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"00\"},{\"type\":\"cg_subgroup\",\"id\":\"010\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"code\":4001,\"message\":\"Path 'identContext': Model 'C205002' is invalid for product 'TestOmittedParts'\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testGetPartsForEinPasGerman() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C204001\",\"productId\":\"C204\"},\"navContext\":[{\"type\":\"maingroup\",\"id\":\"39\"},{\"type\":\"group\",\"id\":\"15\"},{\"type\":\"subgroup\",\"id\":\"12\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          DWFile.get(getTestWorkingDir(), "resultPartsForEinPasGerman.txt"));
    }

    public void testGetPartsForKgTuModuleTwoDrawingsGerman() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C204001\",\"productId\":\"C204_KGTU\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"25\"},{\"type\":\"cg_subgroup\",\"id\":\"015\"},{\"type\":\"module\",\"id\":\"C204_25_15\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultPartsForKgTuModuleTwoDrawingsGerman.txt"));
    }

    public void testGetPartsForSA() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C963003\",\"productId\":\"S01\",\"fin\":\"WDB9630031L738999\",\"datacardExists\":\"true\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"58\"},{\"type\":\"sa_number\",\"id\":\"Z 506.389\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartsForSA.txt"));
    }

    public void testGetPartsForSANoDatacard() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C963003\",\"productId\":\"S01\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"58\"},{\"type\":\"sa_number\",\"id\":\"Z 506.389\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"code\":4001,\"message\":\"Unable to find module for product \\\"S01\\\" and given navContext\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testGetPartsErrorNoModule() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C204001\",\"productId\":\"C204_KGTU\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"25\"},{\"type\":\"cg_subgroup\",\"id\":\"016\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"code\":4001,\"message\":\"Unable to find module for product \\\"C204_KGTU\\\" and given navContext\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testGetPartsErrorAmbigousModules() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C204001\",\"productId\":\"C204\"},\"navContext\":[{\"type\":\"maingroup\",\"id\":\"10\"},{\"type\":\"group\",\"id\":\"10\"},{\"type\":\"subgroup\",\"id\":\"01\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"code\":4001,\"message\":\"Ambiguous attribute 'navContext' (more than one module found): 10/10/01\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testGetPartsErrorInvalidNavNodeType() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C204001\",\"productId\":\"C204_KGTU\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"25\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"code\":4001,\"message\":\"Invalid type of last node in attribute 'navContext' (must be one of [subgroup, cg_subgroup, module]): 25\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testGetPartsErrorMissingParameter() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C204001\",\"productId\":\"C204_KGTU\"},\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"code\":4000,\"message\":\"Attribute list 'navContext' is invalid (missing or empty)\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testGetPartsDamageCodes() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"D651930\",\"productId\":\"00Q\"},\"navContext\": [{\"type\":\"cg_group\",\"id\":\"20\"}," +
                          "{\"type\":\"cg_subgroup\",\"id\":\"015\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultPartsDamageCodes.txt"));
    }

    public void testGetPartsReplacements() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205002\",\"productId\":\"C01\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"62\"}," +
                          "{\"type\":\"cg_subgroup\",\"id\":\"060\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultPartsReplacements.txt"));
    }

    /**
     * Test für Primus-Hinweise. Es gibt 4 Hinweise zu Teilenummern in der Stückliste. Der eine, ohne Nachfolgerteilenummer
     * erzeugt eine virtuelle 402er Fußnote. Die anderen 3 führen zu insgesamt 3 Ersetzungen, wobei nur 2 davon Mitlieferteile
     * erhalten.
     */
    public void testGetPartsWithPrimusReplacements() {
        UniversalConfiguration pluginConfig = iPartsPlugin.getPluginConfig();
        boolean oldIsPrimusHintHandling = pluginConfig.getConfigValueAsBoolean(iPartsPlugin.CONFIG_PRIMUS_HINT_HANDLING);

        // Verarbeitung von PRIMUS Hinweisen temporär aktivieren
        writeBooleanConfigValues(pluginConfig, true, iPartsPlugin.CONFIG_PRIMUS_HINT_HANDLING);
        try {
            clearWebservicePluginsCaches(); // Caches müssen gelöscht werden, damit die Admin-Änderung auch auf jeden Fall berücksichtigt wird
            iPartsPRIMUSReplacementsCache.clearCache(); // Auch der Cache für die PRIMUS-Ersetzungen muss neu aufgebaut werden
            executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"user\":{\"country\":\"200\",\"language\":\"en\",\"userId\":\"userId\"}," +
                              "\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"S\"],\"modelId\":\"C963002\",\"productId\":\"S01\"}," +
                              "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"25\"},{\"type\":\"cg_subgroup\",\"id\":\"330\"}]}",
                              DWFile.get(getTestWorkingDir(), "resultPartsVirtualFootNoteFromPrimusReplacement.txt"));
        } finally {
            writeBooleanConfigValues(pluginConfig, oldIsPrimusHintHandling, iPartsPlugin.CONFIG_PRIMUS_HINT_HANDLING);
            clearWebservicePluginsCaches(); // Caches müssen gelöscht werden, damit die Admin-Änderung auch auf jeden Fall berücksichtigt wird
            iPartsPRIMUSReplacementsCache.clearCache(); // Auch der Cache für die PRIMUS-Ersetzungen muss neu aufgebaut werden
        }
    }

    /**
     * Test für Primus-Hinweise inkl. Alternativteile (und Gleichteile).
     *
     * @see #testGetPartsWithPrimusReplacements()
     */
    public void testGetPartsWithPrimusReplacementsAndAlternativeParts() {
        UniversalConfiguration pluginConfig = iPartsPlugin.getPluginConfig();
        boolean oldIsPrimusHintHandling = pluginConfig.getConfigValueAsBoolean(iPartsPlugin.CONFIG_PRIMUS_HINT_HANDLING);
        boolean oldIsShowAlternativePartsForPrimus = pluginConfig.getConfigValueAsBoolean(iPartsPlugin.CONFIG_SHOW_ALTERNATIVE_PARTS_FOR_PRIMUS);
        boolean oldIsShowEqualParts = pluginConfig.getConfigValueAsBoolean(iPartsPlugin.CONFIG_SHOW_EQUAL_PARTS);

        // Verarbeitung von PRIMUS Hinweisen temporär aktivieren
        writeBooleanConfigValues(pluginConfig, true, iPartsPlugin.CONFIG_PRIMUS_HINT_HANDLING, iPartsPlugin.CONFIG_SHOW_ALTERNATIVE_PARTS_FOR_PRIMUS,
                                 iPartsPlugin.CONFIG_SHOW_EQUAL_PARTS);
        iPartsProduct product = iPartsProduct.getInstance(getProject(), new iPartsProductId("S01"));
        boolean oldIsCarAndVanProduct = product.isCarAndVanProduct();
        boolean oldIsTruckAndBusProduct = product.isTruckAndBusProduct();
        try {
            // S01 hat in der DB leider AS-Produktklasse "S" (für andere Tests), was KEIN Truck ist -> temporär auf Truck setzen
            product.__internal_setCarAndTruckProduct(false, true);

            clearWebservicePluginsCaches(); // Caches müssen gelöscht werden, damit die Admin-Änderung auch auf jeden Fall berücksichtigt wird
            iPartsPRIMUSReplacementsCache.clearCache(); // Auch der Cache für die PRIMUS-Ersetzungen muss neu aufgebaut werden
            executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"user\":{\"country\":\"200\",\"language\":\"en\",\"userId\":\"userId\"}," +
                              "\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C963002\",\"productId\":\"S01\"}," +
                              "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"25\"},{\"type\":\"cg_subgroup\",\"id\":\"330\"}]}",
                              DWFile.get(getTestWorkingDir(), "resultPartsPrimusReplacementWithAlternativeParts.txt"));
        } finally {
            writeBooleanConfigValues(pluginConfig, oldIsPrimusHintHandling, iPartsPlugin.CONFIG_PRIMUS_HINT_HANDLING);
            writeBooleanConfigValues(pluginConfig, oldIsShowAlternativePartsForPrimus, iPartsPlugin.CONFIG_SHOW_ALTERNATIVE_PARTS_FOR_PRIMUS);
            writeBooleanConfigValues(pluginConfig, oldIsShowEqualParts, iPartsPlugin.CONFIG_SHOW_EQUAL_PARTS);
            product.__internal_setCarAndTruckProduct(oldIsCarAndVanProduct, oldIsTruckAndBusProduct);
            clearWebservicePluginsCaches(); // Caches müssen gelöscht werden, damit die Admin-Änderung auch auf jeden Fall berücksichtigt wird
            iPartsPRIMUSReplacementsCache.clearCache(); // Auch der Cache für die PRIMUS-Ersetzungen muss neu aufgebaut werden
        }
    }

    public void testGetPartsAlternativeParts() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"D967820\"," +
                          "\"productId\":\"F10\",\"filterOptions\":{}},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"67\"},{\"type\":\"cg_subgroup\",\"id\":\"060\"}]}",
                          DWFile.get(getTestWorkingDir(), "resultPartsAlternatives.txt"));
    }

    public void testGetPartsAlternativePartsWithIncludedAlternativeParts() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"D967820\"," +
                          "\"productId\":\"F10\",\"filterOptions\":{}},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"67\"},{\"type\":\"cg_subgroup\",\"id\":\"060\"}],\"includeAlternativeParts\":true}",
                          DWFile.get(getTestWorkingDir(), "resultPartsAlternativesWithIncludedAlternativeParts.txt"));
    }

    public void testGetPartsReplacementChainAlternativeParts() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\": {\"aggTypeId\": \"F\", \"filterOptions\": {}, \"modelId\": \"D967820\", \"productClassIds\": [\"P\"], \"productId\": \"F10\"}, " +
                          "\"includeReplacementChain\": true, \"navContext\": [{\"id\": \"67\", \"type\": \"cg_group\"}, {\"id\": \"060\", \"type\": \"cg_subgroup\"}], \"user\": {\"country\": \"200\", \"language\": \"de\", \"userId\": \"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultPartsReplacementChainAlternativeParts.txt"));
    }

    // Hier wird gleichzeitig das Tag "primusCode74Available" in der Ersatzkette geprüft
    public void testGetPartsReplacementChain() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\": {\"aggTypeId\": \"F\", \"modelId\": \"C166074\", \"modelTypeId\": \"C166\", \"productClassIds\": [\"P\"], \"productId\": \"D62\", \"typeVersion\": \"FW\"}, \"includeReplacementChain\": true, " +
                          "\"navContext\": [{\"id\": \"46\", \"type\": \"cg_group\"}, {\"id\": \"015\", \"type\": \"cg_subgroup\"}, {\"id\": \"D62_46_015_00001\", \"type\": \"module\"}], \"user\": {\"country\": \"200\", \"language\": \"de\", \"userId\": \"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultPartsReplacementChain.txt"));
    }

    /**
     * Bei diesem GetParts-Aufruf die ReleaseInfo erzeugen lassen.
     * Alle anderen GetParts-Aufrufe überspringen die Erzeugung per Schalter: IN_UNITTEST_MODE = TRUE <<== Default!
     * Hier wird der Schalter vorher explizit auf FALSE gesetzt und nachher wieder zurück.
     */
    public void testGetPartsAlternativePartsWithReleaseInfo() {
        // Den für die Unittests "bin im Unittest Mode" == TRUE gesetzten Schalter temporär ausschalten,
        // damit gezielt eine "releaseInfo" im Ergebnisstring erzeugt wird.
        iPartsWSAbstractGetPartsEndpoint.IN_UNITTEST_MODE = false;
        try {
            clearWebservicePluginsCaches(); // Caches müssen gelöscht werden, damit die Konstanten-Änderung auch auf jeden Fall berücksichtigt wird
            executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205002\",\"productId\":\"C01\",\"filterOptions\":{}},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"29\"},{\"type\":\"cg_subgroup\",\"id\":\"015\"}]} ",
                              DWFile.get(getTestWorkingDir(), "resultPartsAlternativesWithReleaseInfo.txt"));
        } finally {
            // den temporär ausgeschalteten "bin im Unittest Mode"-Schalter wieder einschalten
            iPartsWSAbstractGetPartsEndpoint.IN_UNITTEST_MODE = true;
            clearWebservicePluginsCaches(); // Caches müssen gelöscht werden, damit die Konstanten-Änderung auch auf jeden Fall berücksichtigt wird
        }
    }

    public void testGetPartsColorTableFootnotes() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"F\"],\"modelId\":\"C967000\",\n" +
                          "\"productId\":\"S10\",\"filterOptions\":{}},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"54\"},{\"type\":\"cg_subgroup\",\"id\":\"228\"}]}",
                          DWFile.get(getTestWorkingDir(), "resultPartsColorTableFootnotes.txt"));
    }

    public void testGetPartsPartAndPositionFootnotes() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"F\"],\"modelId\":\"C967000\",\n" +
                          "\"productId\":\"S10\",\"filterOptions\":{}},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"25\"},{\"type\":\"cg_subgroup\",\"id\":\"330\"}]}",
                          DWFile.get(getTestWorkingDir(), "resultPartsPartAndPositionFootnotes.txt"));
    }

    public void testGetPartsSteeringTransmissionSAA() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"D669599\",\"productId\":\"06F\",\"filterOptions\":{\"model\":true}}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"61\"},{\"type\":\"cg_subgroup\",\"id\":\"055\"}],\"user\":{\"country\":\"200\",\"language\":\"de\"," +
                          "\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultPartsSteeringTransmission.txt"));
    }

    public void testGetPartsWithExtendedCodeFilter() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205240\",\"productId\":\"C22\",\"fin\":\"WDD2052401F236482\",\"datacardExists\":true}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"26\"},{\"type\":\"cg_subgroup\",\"id\":\"035\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultPartsWithExtendedCodeFilter.txt"));
    }


    public void testGetPartsWithExtendedCodeOnionCaseFilter() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205004\",\"productId\":\"C01\",\"fin\":\"WDD2050041F004362\",\"datacardExists\":true}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"82\"},{\"type\":\"cg_subgroup\",\"id\":\"613\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          DWFile.get(getTestWorkingDir(), "resultPartsWithExtendedCodeOnionCaseFilter.txt"));
    }


    // ohne Filter
    public void testGetPartsFilterAllRetail() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205009\",\"productId\":\"C01\"},\"navContext\":[{\"type\":\"cg_group\"," +
                          "\"id\":\"25\"},{\"type\":\"cg_subgroup\",\"id\":\"015\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"images\":[],\"parts\":[{\"calloutId\":\"10\",\"codeValidity\":\"(805/806)+M651+425;\",\"damageCodes\":[\"25200\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"C01_25_015_00001\",\"sequenceId\":\"00003\"},\"partNo\":\"A0262501401\",\"partNoFormatted\":\"A 026 250 14 01\",\"quantity\":\"1\"},{\"calloutId\":\"10\",\"codeValidity\":\"M651+425;\",\"damageCodes\":[\"25200\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"C01_25_015_00001\",\"sequenceId\":\"00004\"},\"partNo\":\"A2132500100\",\"partNoFormatted\":\"A 213 250 01 00\",\"quantity\":\"1\"},{\"calloutId\":\"50\",\"codeValidity\":\"425+-M626;\",\"level\":\"01\",\"name\":\"SECHSRUNDSCHR.\",\"partContext\":{\"moduleId\":\"C01_25_015_00001\",\"sequenceId\":\"00005\"},\"partNo\":\"N000000005805\",\"partNoFormatted\":\"N 000000 005805\",\"quantity\":\"6\"},{\"calloutId\":\"100\",\"codeValidity\":\"M651;\",\"level\":\"01\",\"name\":\"SECHSRUNDSCHR.\",\"partContext\":{\"moduleId\":\"C01_25_015_00001\",\"sequenceId\":\"00002\"},\"partNo\":\"N910143010000\",\"partNoFormatted\":\"N 910143 010000\",\"quantity\":\"11\"}]}");
    }

    // mit gesetzem BM Filter
    public void testGetPartsFilterBM() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205009\",\"productId\":\"C01\",\"filterOptions\":{\"model\":true}}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"25\"},{\"type\":\"cg_subgroup\",\"id\":\"015\"}],\"user\":{\"country\":\"200\",\"language\":\"de\"," +
                          "\"userId\":\"userId\"}}",
                          "{\"images\":[],\"parts\":[{\"calloutId\":\"10\",\"codeValidity\":\"(805/806)+M651+425;\",\"damageCodes\":[\"25200\"],\"level\":\"01\",\"name\":\"\"," +
                          "\"partContext\":{\"moduleId\":\"C01_25_015_00001\",\"sequenceId\":\"00003\"},\"partNo\":\"A0262501401\",\"partNoFormatted\":\"A 026 250 14 01\"," +
                          "\"quantity\":\"1\"},{\"calloutId\":\"10\",\"codeValidity\":\"M651+425;\",\"damageCodes\":[\"25200\"],\"level\":\"01\",\"name\":\"\",\"partContext\"" +
                          ":{\"moduleId\":\"C01_25_015_00001\",\"sequenceId\":\"00004\"},\"partNo\":\"A2132500100\",\"partNoFormatted\":\"A 213 250 01 00\",\"quantity\":\"1\"}," +
                          "{\"calloutId\":\"50\",\"codeValidity\":\"425+-M626;\",\"level\":\"01\",\"name\":\"SECHSRUNDSCHR.\",\"partContext\":{\"moduleId\":\"C01_25_015_00001\"," +
                          "\"sequenceId\":\"00005\"},\"partNo\":\"N000000005805\",\"partNoFormatted\":\"N 000000 005805\",\"quantity\":\"6\"},{\"calloutId\":\"100\"," +
                          "\"codeValidity\":\"M651;\",\"level\":\"01\",\"name\":\"SECHSRUNDSCHR.\",\"partContext\":{\"moduleId\":\"C01_25_015_00001\",\"sequenceId\":\"00002\"}," +
                          "\"partNo\":\"N910143010000\",\"partNoFormatted\":\"N 910143 010000\",\"quantity\":\"11\"}]}");
    }

    public void testGetPartsInAggregate() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"iparts_content_user\",\"language\":\"de\",\"country\":\"200\"},\"identContext\":{\"fin\":\"WDD2052421F257914\",\"releaseDate\":\"20120306\",\"aggregateNumber\":\"27492030524744\",\"aggTypeId\":\"M\",\"productClassIds\":[\"P\",\"T\"],\"productClassNames\":[\"PKW\",\"Transporter\"],\"modelId\":\"D274920\",\"modelName\":\"R4-OTTOMOTORM274E20\",\"productId\":\"D78\",\"modelDesc\":\"M274E20\",\"modelTypeId\":\"D274\",\"datacardExists\":true,\"filterOptions\":{\"model\":true,\"datacard\":true,\"saVersion\":true,\"steering\":true,\"serial\":true,\"transmission\":true,\"color\":true,\"codes\":true,\"spring\":true}},\"navContext\":[{\"id\":\"05\",\"type\":\"cg_group\",\"label\":\"MOTORSTEUERUNG\"},{\"id\":\"010\",\"type\":\"cg_subgroup\",\"label\":\"NOCKENWELLENLAGERGEHAEUSEUNDNOCKEN-WELLE\",\"partsAvailable\":true,\"thumbNails\":[{\"id\":\"drawing_B05010000008\",\"href\":\"/parts/media/drawing_B05010000008\",\"previewHref\":\"/parts/media/previews/drawing_B05010000008\"}]}]}",
                          DWFile.get(getTestWorkingDir(), "resultPartsInAggregate.txt"));
    }

    public void testGetPartsInAggregateIdentWithDatacard() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggregateNumber\": \"71168001095329\",\"aggTypeId\": \"GM\",\"datacardExists\": true,\"modelId\": \"D711680\",\"productClassIds\":[\"T\"],\"productId\": \"11Q\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"26\"},{\"type\":\"cg_subgroup\",\"id\":\"100\"}]}",
                          DWFile.get(getTestWorkingDir(), "resultPartsAggregateDataCard.txt"));
    }

    public void testGetPartsInAggregateIdentWithoutDatacard() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggregateNumber\": \"27493030008860\",\"aggTypeId\": \"M\",\"modelId\": \"D274930\",\"productClassIds\":[\"T\"],\"productId\": \"00Y\",\"filterOptions\":{\"serial\":true}},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"03\"},{\"type\":\"cg_subgroup\",\"id\":\"015\"}]}",
                          DWFile.get(getTestWorkingDir(), "resultPartsAggregateWithoutDataCard.txt"));
    }

    // Dokutyp ist ELDAS, d.h. die Ersetzungen werden hier nicht gefiltert
    public void testGetPartsInOldEngineAggregate() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggregateNumber\": \"42390530473000\",\"aggTypeId\": \"M\",\"modelId\": \"D423905\",\"productClassIds\":[\"T\"],\"productId\": \"353\",\"filterOptions\":{\"serial\":true}},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"07\"},{\"type\":\"cg_subgroup\",\"id\":\"030\"}]}",
                          DWFile.get(getTestWorkingDir(), "resultPartsInOldEngineAggregate.txt"));
    }

    // Test zu DAIMLER-6970: Nachfolger/Mitlieferteile bei RFME = A0X/A01 ausgeben auch ohne Stücklisteneintrag des Nachfolgers
    public void testGetPartsShowFilteredSuccessorAndIncludeParts() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"iparts_content_user\",\"language\":\"de\",\"country\":\"200\"},\"identContext\":{\"fin\":\"WDD2052421F257914\",\"productClassIds\":[\"P\",\"T\"],\"aggTypeId\":\"M\",\"modelId\":\"D274920\",\"datacardExists\":true,\"productId\":\"D78\"},\"navContext\":[{\"id\":\"20\",\"type\":\"cg_group\"},{\"id\":\"015\",\"type\":\"cg_subgroup\"}]}",
                          DWFile.get(getTestWorkingDir(), "resultPartsShowFilteredSuccessorAndIncludeParts.txt"));
    }

    /**
     * Beispiel für Endnummernfilter inaktiv weil 8. Stelle des Ident 7|8|9 (Austauschmotor).
     */
    public void testGetPartsInOldExchangeEngineAggregate() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggregateNumber\": \"42390537473000\",\"aggTypeId\": \"M\",\"modelId\": \"D423905\",\"productClassIds\":[\"T\"],\"productId\": \"353\",\"filterOptions\":{\"serial\":true}},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"07\"},{\"type\":\"cg_subgroup\",\"id\":\"030\"}]}",
                          DWFile.get(getTestWorkingDir(), "resultPartsInOldExchangeEngineAggregate.txt"));
    }

    public void testGetPartsExtraWW() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggTypeId\":\"GM\",\"productClassIds\":[\"P\"],\"modelId\":\"D715500\",\"productId\":\"01C\",\"filterOptions\":{}},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"26\"},{\"type\":\"cg_subgroup\",\"id\":\"500\"}]}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartsExtraWW.txt"));
    }

    public void testGetPartsSAAfromDatacardOnly() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C963003\",\"productId\":\"S01\",\"fin\":\"WDB9630031L738998\",\"datacardExists\":true,\"filterOptions\":{\"model\":true}},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"25\"},{\"type\":\"cg_subgroup\",\"id\":\"330\"}]}",
                          "{\"images\":[],\"parts\":[{\"calloutId\":\"100\",\"description\":\"FESTO\",\"level\":\"01\",\"name\":\"KUPPL.AKTUATOR\",\"partContext\":{\"moduleId\":\"S01_25_330_00001\",\"sequenceId\":\"00001\"},\"partNo\":\"A0002501462\",\"partNoFormatted\":\"A 000 250 14 62\",\"quantity\":\"1\",\"saaValidity\":[\"Z 504.651/05\",\"Z 504.651/07\"]},{\"calloutId\":\"130\",\"level\":\"01\",\"name\":\"DRUCKSTANGE\",\"partContext\":{\"moduleId\":\"S01_25_330_00001\",\"sequenceId\":\"00002\"},\"partNo\":\"A9302550117\",\"partNoFormatted\":\"A 930 255 01 17\",\"quantity\":\"1\",\"saaValidity\":[\"Z 504.651/05\",\"Z 504.651/07\"]},{\"calloutId\":\"135\",\"level\":\"01\",\"name\":\"STOESSEL\",\"partContext\":{\"moduleId\":\"S01_25_330_00001\",\"sequenceId\":\"00003\"},\"partNo\":\"A9422550117\",\"partNoFormatted\":\"A 942 255 01 17\",\"quantity\":\"1\",\"saaValidity\":[\"Z 504.651/05\",\"Z 504.651/07\"]},{\"calloutId\":\"140\",\"description\":\"M12X1.5\",\"level\":\"01\",\"name\":\"SECHSKANTMUTTER\",\"partContext\":{\"moduleId\":\"S01_25_330_00001\",\"sequenceId\":\"00004\"},\"partNo\":\"N308675012000\",\"partNoFormatted\":\"N 308675 012000\",\"quantity\":\"1\",\"saaValidity\":[\"Z 504.651/07\"]},{\"calloutId\":\"150\",\"level\":\"01\",\"name\":\"ENTLUEFTERVENT.\",\"optionalParts\":[{\"name\":\"ENTLUEFTERVENT.\",\"partContext\":{\"moduleId\":\"S01_25_330_00001\",\"sequenceId\":\"00006\"},\"partNo\":\"A9609970109\",\"partNoFormatted\":\"A 960 997 01 09\"}],\"partContext\":{\"moduleId\":\"S01_25_330_00001\",\"sequenceId\":\"00005\"},\"partNo\":\"A9609970009\",\"partNoFormatted\":\"A 960 997 00 09\",\"quantity\":\"1\",\"saaValidity\":[\"Z 504.651/05\",\"Z 504.651/07\"]},{\"calloutId\":\"150\",\"level\":\"01\",\"name\":\"ENTLUEFTERVENT.\",\"optionalParts\":[{\"name\":\"ENTLUEFTERVENT.\",\"partContext\":{\"moduleId\":\"S01_25_330_00001\",\"sequenceId\":\"00005\"},\"partNo\":\"A9609970009\",\"partNoFormatted\":\"A 960 997 00 09\"}],\"partContext\":{\"moduleId\":\"S01_25_330_00001\",\"sequenceId\":\"00006\"},\"partNo\":\"A9609970109\",\"partNoFormatted\":\"A 960 997 01 09\",\"quantity\":\"1\",\"saaValidity\":[\"Z 504.651/05\",\"Z 504.651/07\"]},{\"alternativePartsAvailable\":true,\"alternativePartsTypes\":[\"01\"],\"calloutId\":\"155\",\"level\":\"02\",\"name\":\"O-RING\",\"partContext\":{\"moduleId\":\"S01_25_330_00001\",\"sequenceId\":\"00007\"},\"partNo\":\"A0159978245\",\"partNoFormatted\":\"A 015 997 82 45\",\"quantity\":\"1\",\"saaValidity\":[\"Z 504.651/05\",\"Z 504.651/07\"]},{\"calloutId\":\"160\",\"level\":\"01\",\"name\":\"UEBERWURFSCHR.\",\"partContext\":{\"moduleId\":\"S01_25_330_00001\",\"sequenceId\":\"00008\"},\"partNo\":\"A0009978134\",\"partNoFormatted\":\"A 000 997 81 34\",\"quantity\":\"2\",\"saaValidity\":[\"Z 504.651/05\",\"Z 504.651/07\"]},{\"calloutId\":\"170\",\"description\":\"M10X45\",\"level\":\"01\",\"name\":\"SECHSRUNDSCHR.\",\"partContext\":{\"moduleId\":\"S01_25_330_00001\",\"sequenceId\":\"00009\"},\"partNo\":\"N000000007555\",\"partNoFormatted\":\"N 000000 007555\",\"quantity\":\"2\",\"saaValidity\":[\"Z 504.651/07\"]},{\"calloutId\":\"170\",\"description\":\"M10X70\",\"level\":\"01\",\"name\":\"SECHSRUNDSCHR.\",\"partContext\":{\"moduleId\":\"S01_25_330_00001\",\"sequenceId\":\"00010\"},\"partNo\":\"N000000002373\",\"partNoFormatted\":\"N 000000 002373\",\"quantity\":\"2\",\"saaValidity\":[\"Z 504.651/05\"]}]}");
    }

    public void testGetPartsSAAfromDatacardOnlyNoDatacard() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C963003\",\"productId\":\"S01\",\"fin\":\"WDB9630031L738998\",\"datacardExists\":false,\"filterOptions\":{\"model\":true}},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"25\"},{\"type\":\"cg_subgroup\",\"id\":\"330\"}]}",
                          "{\"images\":[],\"parts\":[{\"calloutId\":\"100\",\"description\":\"FESTO\",\"level\":\"01\",\"name\":\"KUPPL.AKTUATOR\",\"partContext\":{\"moduleId\":\"S01_25_330_00001\",\"sequenceId\":\"00001\"},\"partNo\":\"A0002501462\",\"partNoFormatted\":\"A 000 250 14 62\",\"quantity\":\"1\",\"saaValidity\":[\"Z 504.651/05\",\"Z 504.651/07\"]},{\"calloutId\":\"130\",\"level\":\"01\",\"name\":\"DRUCKSTANGE\",\"partContext\":{\"moduleId\":\"S01_25_330_00001\",\"sequenceId\":\"00002\"},\"partNo\":\"A9302550117\",\"partNoFormatted\":\"A 930 255 01 17\",\"quantity\":\"1\",\"saaValidity\":[\"Z 504.651/05\",\"Z 504.651/07\"]},{\"calloutId\":\"135\",\"level\":\"01\",\"name\":\"STOESSEL\",\"partContext\":{\"moduleId\":\"S01_25_330_00001\",\"sequenceId\":\"00003\"},\"partNo\":\"A9422550117\",\"partNoFormatted\":\"A 942 255 01 17\",\"quantity\":\"1\",\"saaValidity\":[\"Z 504.651/05\",\"Z 504.651/07\"]},{\"calloutId\":\"140\",\"description\":\"M12X1.5\",\"level\":\"01\",\"name\":\"SECHSKANTMUTTER\",\"partContext\":{\"moduleId\":\"S01_25_330_00001\",\"sequenceId\":\"00004\"},\"partNo\":\"N308675012000\",\"partNoFormatted\":\"N 308675 012000\",\"quantity\":\"1\",\"saaValidity\":[\"Z 504.651/07\"]},{\"calloutId\":\"150\",\"level\":\"01\",\"name\":\"ENTLUEFTERVENT.\",\"optionalParts\":[{\"name\":\"ENTLUEFTERVENT.\",\"partContext\":{\"moduleId\":\"S01_25_330_00001\",\"sequenceId\":\"00006\"},\"partNo\":\"A9609970109\",\"partNoFormatted\":\"A 960 997 01 09\"}],\"partContext\":{\"moduleId\":\"S01_25_330_00001\",\"sequenceId\":\"00005\"},\"partNo\":\"A9609970009\",\"partNoFormatted\":\"A 960 997 00 09\",\"quantity\":\"1\",\"saaValidity\":[\"Z 504.651/05\",\"Z 504.651/07\"]},{\"calloutId\":\"150\",\"level\":\"01\",\"name\":\"ENTLUEFTERVENT.\",\"optionalParts\":[{\"name\":\"ENTLUEFTERVENT.\",\"partContext\":{\"moduleId\":\"S01_25_330_00001\",\"sequenceId\":\"00005\"},\"partNo\":\"A9609970009\",\"partNoFormatted\":\"A 960 997 00 09\"}],\"partContext\":{\"moduleId\":\"S01_25_330_00001\",\"sequenceId\":\"00006\"},\"partNo\":\"A9609970109\",\"partNoFormatted\":\"A 960 997 01 09\",\"quantity\":\"1\",\"saaValidity\":[\"Z 504.651/05\",\"Z 504.651/07\"]},{\"alternativePartsAvailable\":true,\"alternativePartsTypes\":[\"01\"],\"calloutId\":\"155\",\"level\":\"02\",\"name\":\"O-RING\",\"partContext\":{\"moduleId\":\"S01_25_330_00001\",\"sequenceId\":\"00007\"},\"partNo\":\"A0159978245\",\"partNoFormatted\":\"A 015 997 82 45\",\"quantity\":\"1\",\"saaValidity\":[\"Z 504.651/05\",\"Z 504.651/07\"]},{\"calloutId\":\"160\",\"level\":\"01\",\"name\":\"UEBERWURFSCHR.\",\"partContext\":{\"moduleId\":\"S01_25_330_00001\",\"sequenceId\":\"00008\"},\"partNo\":\"A0009978134\",\"partNoFormatted\":\"A 000 997 81 34\",\"quantity\":\"2\",\"saaValidity\":[\"Z 504.651/05\",\"Z 504.651/07\"]},{\"calloutId\":\"170\",\"description\":\"M10X45\",\"level\":\"01\",\"name\":\"SECHSRUNDSCHR.\",\"partContext\":{\"moduleId\":\"S01_25_330_00001\",\"sequenceId\":\"00009\"},\"partNo\":\"N000000007555\",\"partNoFormatted\":\"N 000000 007555\",\"quantity\":\"2\",\"saaValidity\":[\"Z 504.651/07\"]}]}");
    }

    public void testGetPartsSAfromDatacardOnly() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C963003\",\"productId\":\"S01\",\"fin\":\"WDB9630031L738998\",\"datacardExists\":true,\"filterOptions\":{\"model\":true}},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"41\"},{\"type\":\"sa_number\",\"id\":\"Z M02.804\"}]}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartsSAfromDatacardOnly.txt"));
    }

    public void testGetPartsSAfromDatacardOnlyNoDatacard() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C963003\",\"productId\":\"S01\",\"fin\":\"WDB9630031L738998\",\"datacardExists\":false,\"filterOptions\":{\"model\":true}},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"41\"},{\"type\":\"sa_number\",\"id\":\"Z M02.804\"}]}",
                          "{\"code\":4001,\"message\":\"Unable to find module for product \\\"S01\\\" and given navContext\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    // DAIMLER-6067 Bei aktviertem Datenkarten-SA Filter wird für eine Teileposition die Baumustergültigkeit ignoriert, wenn eine
    // SAA-Gültigkeit an der Teileposition vorliegt und es sich nicht um die Dokumethode DIALOG/DIALOG iParts handelt
    public void testGetPartsIgnoreBMifEntryContainsSAAandBM() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"datacardExists\": true,\"aggTypeId\":\"G\",\"productClassIds\":[\"P\"],\"modelId\":\"D711680\"," +
                          "\"aggregateNumber\":\"71168001095329\",\"productId\":\"11Q\",\"filterOptions\":{\"model\":true, \"datacard\":true}}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"26\"},{\"type\":\"cg_subgroup\",\"id\":\"150\"}],\"user\":{\"country\":\"200\"," +
                          "\"language\":\"de\",\"userId\":\"userId\"}} ",
                          DWFile.get(getTestWorkingDir(), "resultGetPartsIgnoreBMifDatacardContainsSAAandBM.txt"));
    }

    public void testGetPartsForTextOnly() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI, "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}," +
                                                                         "\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C963002\",\"productId\":\"S01\"}," +
                                                                         "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"52\"},{\"type\":\"cg_subgroup\",\"id\":\"060\"}]}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartsForTextOnly.txt"));
    }

    public void testGetPartsMarketspecificSAAsEnrichment() {
        // DAIMLER-6071
        // Dieser Testfall basiert komplett auf künstlichen Daten:
        // Am Datensatz "N 304017 010042" wurde die SAA "Z  38.660/01" hinzugefügt, außerdem wurde die Datenkarte zur
        // FIN YYY9670341N687496 erzeugt.
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI, "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"fin\":\"YYY9670341N687496\",\"datacardExists\":true,\"modelId\":\"D731701\",\"productId\":\"V22\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"42\"},{\"type\":\"cg_subgroup\",\"id\":\"780\"}]}",
                          "{\"images\":[],\"parts\":[{\"calloutId\":\"45\",\"description\":\"M 10X16\",\"modelValidity\":[\"D731701\"],\"name\":\"SECHSKANTSCHR.\",\"partContext\":{\"moduleId\":\"V22_42_780_00001\",\"sequenceId\":\"00028\"},\"partNo\":\"N304017010042\",\"partNoFormatted\":\"N 304017 010042\",\"quantity\":\"8\",\"saaValidity\":[\"Z 504.534/33\",\"Z 038.660/01\"]}]}");
    }

    public void testGetPartsForMotorStarIdent() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggTypeId\":\"M\",\"productClassIds\":[\"P\"],\"modelId\":\"D924927\",\"productId\":\"07K\",\"aggregateNumber\":\"92492710843176\",\"filterOptions\":{\"serial\":true}},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"09\"},{\"type\":\"cg_subgroup\",\"id\":\"075\"}]}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartsForMotorStarIdent.txt"));
        // lfdNr 00206 - 00209 werden auf Grund des Rückmelde-Ident ausgefiltert
    }

    public void testGetPartsForMotor6DigitIdent() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggTypeId\":\"M\",\"productClassIds\":[\"P\"],\"modelId\":\"D457943\",\"productId\":\"14Y\",\"aggregateNumber\":\"45794315201851\",\"filterOptions\":{\"serial\":true}},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"01\"},{\"type\":\"cg_subgroup\",\"id\":\"015\"}]}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartsForMotor6DigitIdent.txt"));
        // z.B. lfdNr 00114 wird ausgefiltert, da nur der 6-stellige Ident verglichen wird. 00114 hat als Pem ab 201852. Die Datenkarte 7-stellig: 5201851, was eigentlich größer wäre,
        // aber da nur 6-stellig verglichen wird ist der Ident der DK kleiner (201851 < 201852) -> ausgefiltert
    }

    public void testGetPartsForMotorWithAdditionalFactory() {
        // Zum Werk 6012 und Baureihe D1569 gibt es das Zusatzwerk 010. Dadurch werden die Einträge mit LfdNr 00010 und 00025
        // nicht mehr ausgefiltert
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggTypeId\":\"M\",\"productClassIds\":[\"P\"],\"modelId\":\"D156980\",\"aggregateNumber\":\"15698060000735\",\"productId\":\"64C\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"01\"},{\"type\":\"cg_subgroup\",\"id\":\"075\"}]}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartsForMotorWithAdditionalFactory.txt"));
    }

    public void testGetPartsForPallet() {
        // DAIMLER-6917
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}," +
                          "\"identContext\":{\"aggTypeId\":\"P\",\"productClassIds\":[\"U\"],\"modelId\":\"D405611\",\"productId\":\"15H\",\"fin\":\"WDB4051011V227973\"}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"60\"},{\"type\":\"cg_subgroup\",\"id\":\"025\"}]}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartsForPallet.txt"));
    }

    public void testGetPartsWithModelValidityDIALOG() {
        // DAIMLER-7343 Erweiterung Webservice GetParts um modelValidity

        // DIALOG-Stückliste mit berechneter Baumustergültigkeit
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}," +
                          "\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205002\",\"productId\":\"C01\",\"filterOptions\":{}}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"29\"},{\"type\":\"cg_subgroup\",\"id\":\"015\"}]}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartsWithModelValidityDIALOG.txt"));
    }

    public void testGetPartsWithModelValidityELDAS() {
        // ELDAS-Stückliste mit Baumustergültigkeit in der DB
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}," +
                          "\"identContext\":{\"aggTypeId\":\"GM\",\"productClassIds\":[\"L\"],\"modelId\":\"D715520\",\"productId\":\"01C\",\"filterOptions\":{}}," +
                          "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"25\"},{\"type\":\"cg_subgroup\",\"id\":\"050\"}]}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartsWithModelValidityELDAS.txt"));

    }

    public void testGetPartsRMI() {
        // DAIMLER-10311 Beschränkung der Daten wegen RMI

        UniversalConfiguration pluginConfig = iPartsWebservicePlugin.getPluginConfig();
        writeBooleanConfigValues(pluginConfig, true, iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI);
        try {
            // Caches müssen gelöscht werden, damit die Admin-Änderung auch auf jeden Fall berücksichtigt wird
            clearWebservicePluginsCaches();

            executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}," +
                              "\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"S\"],\"modelId\":\"C463244\",\"productId\":\"37M\"}," +
                              "\"navContext\":[{\"type\":\"cg_group\",\"id\":\"28\"},{\"type\":\"cg_subgroup\",\"id\":\"090\"}]}",
                              DWFile.get(getTestWorkingDir(), "resultGetPartsRMI.txt"));
        } finally {
            writeBooleanConfigValues(pluginConfig, false, iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI);
            clearWebservicePluginsCaches();
        }
    }

    /**
     * DAIMLER-10034, getParts Aufruf nur noch mit FIN identContext
     * <p>
     * RMI: "Repair and Maintenance Information", der WS stellt Servicedienstleistern Reparatur- und Wartungsinformationen (RMI) bereit.
     * <p>
     * Der Schalter:
     * [CONFIG_ONLY_FIN_BASED_REQUESTS_RMI], "!!Nur Anfragen mit FIN/VIN und Datenkarte für RMI Typzulassung verarbeiten"
     * wird für jeden zu testenden WebService zuerst auf "false" und dann auf "true" gesetzt.
     * <p>
     * Für jeden dieser WebServices je ein positiv- und ein negativ-Beispiel.
     * - getNavOpts
     * - getPartInfo
     * - getParts
     * - partsList
     * - searchComponent
     * - searchParts
     */
    public void testGetPartsRequiredFinOrVinIdentContextGetNavOptsOn() {
        // Test 1: Schalter "Nur noch mit FIN im identContext zugelassen" [EIN]
        Map<UniversalConfigOption, Boolean> configOptions = new HashMap<>();
        configOptions.put(iPartsWebservicePlugin.CONFIG_ONLY_FIN_BASED_REQUESTS_RMI, true);
        configOptions.put(iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI, false);

        executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), configOptions, new WebserviceRunnable() {
            @Override
            public void executeTest() {
                // Test 1.1: Schalter [EIN], Datenkarte vorhanden, datacardExists [TRUE]
                executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI, "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}," +
                                                                                   "\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"fin\":\"YYY9670341N687496\",\"datacardExists\":true," +
                                                                                   "\"modelId\":\"D731701\",\"productId\":\"V22\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"42\"},{\"type\":\"cg_subgroup\",\"id\":\"780\"}]}",
                                  "{\"nextNodes\":[{\"id\":\"V22_42_780_00001\",\"label\":\"TROMMELBREMSE\",\"partsAvailable\":true,\"type\":\"module\"}]}");

                // Schalter [EIN], Datenkarte in Anfrage nicht enthalten
                executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI, "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}," +
                                                                                   "\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"]," +
                                                                                   "\"modelId\":\"D731701\",\"productId\":\"V22\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"42\"},{\"type\":\"cg_subgroup\",\"id\":\"780\"}]}",
                                  "{\"code\":4041,\"message\":\"Datacard not found\"}", HttpConstants.HTTP_STATUS_NOT_FOUND);

                // Test 1.2: Schalter [EIN], Datenkarte vorhanden, datacardExists [FALSE]
                executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI, "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}," +
                                                                                   "\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"fin\":\"YYY9670341N687496\",\"datacardExists\":false," +
                                                                                   "\"modelId\":\"D731701\",\"productId\":\"V22\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"42\"},{\"type\":\"cg_subgroup\",\"id\":\"780\"}]}",
                                  "{\"code\":4041,\"message\":\"Datacard not found\"}", HttpConstants.HTTP_STATUS_NOT_FOUND);

                // Test 1.3: Schalter [EIN], ungültige Datenkarte "WDB1111111L111111" ==> KEIN Baumusterfallback
                executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI, "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C963003\",\"productId\":\"S01\",\"fin\":\"WDB1111111L111111\",\"datacardExists\":false,\"filterOptions\":{\"model\":true}},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"41\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                                  "{\"code\":4041,\"message\":\"Datacard not found\"}", HttpConstants.HTTP_STATUS_NOT_FOUND);

            }
        });
    }

    public void testGetPartsRequiredFinOrVinIdentContextGetNavOptsOff() {
        // Schalter "Nur noch mit FIN im identContext zugelassen" [AUS]
        Map<UniversalConfigOption, Boolean> configOptions = new HashMap<>();
        configOptions.put(iPartsWebservicePlugin.CONFIG_ONLY_FIN_BASED_REQUESTS_RMI, false);
        configOptions.put(iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI, false);

        executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), configOptions, new WebserviceRunnable() {

            @Override
            public void executeTest() {
                // Test 1.4: (gleicher identContext wie Test 1.3)
                // Schalter [AUS], ungültige Datenkarte "WDB1111111L111111" ==> Baumusterfallback
                executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI, "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C963003\",\"productId\":\"S01\",\"fin\":\"WDB1111111L111111\",\"datacardExists\":false,\"filterOptions\":{\"model\":true}},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"41\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                                  "{\"nextNodes\":[{\"id\":\"030\",\"label\":\"GELENKWELLE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}]}");

                // Test 1.5 Schalter [AUS], Datenkarte vorhanden ==> gültiges Ergebnis
                executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI, "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"fin\":\"YYY9670341N687496\",\"datacardExists\":true,\"modelId\":\"D731701\",\"productId\":\"V22\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"42\"},{\"type\":\"cg_subgroup\",\"id\":\"780\"}]}",
                                  "{\"nextNodes\":[{\"id\":\"V22_42_780_00001\",\"label\":\"TROMMELBREMSE\",\"partsAvailable\":true,\"type\":\"module\"}]}");

            }
        });
    }

    public void testGetPartsRequiredFinOrVinIdentContextGetPartInfoOn() {
        // Schalter "Nur noch mit FIN im identContext zugelassen" [AUS]
        Map<UniversalConfigOption, Boolean> configOptions = new HashMap<>();
        configOptions.put(iPartsWebservicePlugin.CONFIG_ONLY_FIN_BASED_REQUESTS_RMI, true);
        configOptions.put(iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI, false);

        executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), configOptions, new WebserviceRunnable() {

            @Override
            public void executeTest() {
                // Test 2.1, Prüfung mit "Nur noch mit FIN im identContext zugelassen"
                executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI, "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"partContext\":{\"moduleId\":\"C01_68_485_00001\",\"sequenceId\":\"00017\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205002\",\"productId\":\"C01\",\"modelTypeId\":\"C205\",\"filterOptions\":{\"model\":true}}}",
                                  "{\"code\":4041,\"message\":\"Datacard not found\"}", HttpConstants.HTTP_STATUS_NOT_FOUND);

            }
        });
    }

    public void testGetPartsRequiredFinOrVinIdentContextGetPartInfoOff() {
        // Schalter "Nur noch mit FIN im identContext zugelassen" [AUS]
        Map<UniversalConfigOption, Boolean> configOptions = new HashMap<>();
        configOptions.put(iPartsWebservicePlugin.CONFIG_ONLY_FIN_BASED_REQUESTS_RMI, false);
        configOptions.put(iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI, false);

        executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), configOptions, new WebserviceRunnable() {

            @Override
            public void executeTest() {
                // Test 2.2 Prüfung ohne "Nur noch mit FIN im identContext zugelassen"
                executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI, "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"partContext\":{\"moduleId\":\"C01_68_485_00001\",\"sequenceId\":\"00017\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205002\",\"productId\":\"C01\",\"modelTypeId\":\"C205\",\"filterOptions\":{\"model\":true}}}",
                                  DWFile.get(getTestWorkingDir(), "resultOnlyFinBasedRequestGetPartInfo.txt"));
            }
        });
    }

    /**
     * Hier werden die EinPAS-Daten, je nach RMI-Konfigurationsschalter, weggelassen oder dazugemischt.
     */
    public void testGetPartsListDAIMLER15364() {
        // Token enthält Sprachen de, en, fr, d.h. de ist die Hauptsprache und en, fr sind die Rückfallsprachen
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");
        Map<UniversalConfigOption, Boolean> configOptions = new HashMap<>();
        // Schalter "Nur noch mit FIN im identContext zugelassen" [AUS]
        configOptions.put(iPartsWebservicePlugin.CONFIG_ONLY_FIN_BASED_REQUESTS_RMI, false);
        String addOn = "?model=C166823&productId=D81&extendedDescriptions=true&reducedInformation=false&#39;";

        try {
            // Ohne gesetzten Schalter werden die Ergebnisdaten MIT den EinPAS-Daten angereichert ausgegeben:
            configOptions.put(iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI, false);
            executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), configOptions, new WebserviceRunnable() {
                @Override
                public void executeTest() {
                    executeWebservice(iPartsWSPartsListEndpoint.DEFAULT_ENDPOINT_URI + addOn,
                                      null, additionalRequestProperties,
                                      DWFile.get(getTestWorkingDir(), "resultGetPartsListDAIMLER15364EinPasRMIenriched.txt"));
                }
            });

            // Mit gesetztem Schalter werden die Ergebnisdaten OHNE die EinPAS-Daten ausgegeben.
            configOptions.put(iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI, true);
            executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), configOptions, new WebserviceRunnable() {
                @Override
                public void executeTest() {
                    executeWebservice(iPartsWSPartsListEndpoint.DEFAULT_ENDPOINT_URI + addOn,
                                      null, additionalRequestProperties,
                                      DWFile.get(getTestWorkingDir(), "resultGetPartsListDAIMLER15364EinPasRMIreduced.txt"));
                }
            });
        } finally {
            configOptions.put(iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI, false);
        }
    }


    /**
     * Hier werden die EinPAS-Daten, je nach RMI-Konfigurationsschalter, weggelassen oder dazugemischt.
     */
    public void testGetPartInfoDAIMLER15364() {
        Map<UniversalConfigOption, Boolean> configOptions = new HashMap<>();
        // Schalter "Nur noch mit FIN im identContext zugelassen" [AUS]
        configOptions.put(iPartsWebservicePlugin.CONFIG_ONLY_FIN_BASED_REQUESTS_RMI, false);
        String requestString = "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"partContext\":{\"moduleId\":\"C01_68_485_00001\",\"sequenceId\":\"00016\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205002\",\"productId\":\"C01\",\"modelTypeId\":\"C205\",\"filterOptions\":{\"model\":true}}}";
        try {
            // Ohne gesetzten Schalter werden die Ergebnisdaten MIT den EinPAS-Daten angereichert ausgegeben:
            configOptions.put(iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI, false);
            executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), configOptions, new WebserviceRunnable() {
                @Override
                public void executeTest() {
                    executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI, requestString,
                                      DWFile.get(getTestWorkingDir(), "resultGetPartInfoDAIMLER15364EinPasRMIenriched.txt"));
                }
            });

            // Mit gesetztem Schalter werden die Ergebnisdaten OHNE die EinPAS-Daten ausgegeben.
            configOptions.put(iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI, true);
            executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), configOptions, new WebserviceRunnable() {
                @Override
                public void executeTest() {
                    executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI, requestString,
                                      DWFile.get(getTestWorkingDir(), "resultGetPartInfoDAIMLER15364EinPasRMIreduced.txt"));
                }
            });
        } finally {
            configOptions.put(iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI, false);
        }
    }

    public void testGetPartsRequiredFinOrVinIdentContextGetPartsOn() {
        // Schalter "Nur noch mit FIN im identContext zugelassen" [AUS]
        Map<UniversalConfigOption, Boolean> configOptions = new HashMap<>();
        configOptions.put(iPartsWebservicePlugin.CONFIG_ONLY_FIN_BASED_REQUESTS_RMI, true);
        configOptions.put(iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI, false);

        executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), configOptions, new WebserviceRunnable() {

            @Override
            public void executeTest() {
                executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI, "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205002\",\"productId\":\"C01\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"29\"},{\"type\":\"cg_subgroup\",\"id\":\"015\"}]}",
                                  "{\"code\":4041,\"message\":\"Datacard not found\"}", HttpConstants.HTTP_STATUS_NOT_FOUND);

            }
        });
    }

    public void testGetPartsRequiredFinOrVinIdentContextGetPartsOff() {
        // Schalter "Nur noch mit FIN im identContext zugelassen" [AUS]
        Map<UniversalConfigOption, Boolean> configOptions = new HashMap<>();
        configOptions.put(iPartsWebservicePlugin.CONFIG_ONLY_FIN_BASED_REQUESTS_RMI, false);
        configOptions.put(iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI, false);

        executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), configOptions, new WebserviceRunnable() {

            @Override
            public void executeTest() {
                executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI, "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205002\",\"productId\":\"C01\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"29\"},{\"type\":\"cg_subgroup\",\"id\":\"015\"}]}",
                                  DWFile.get(getTestWorkingDir(), "resultOnlyFinBasedRequestGetParts.txt"));
            }
        });
    }

    public void testGetPartsRequiredFinOrVinIdentContextPartsListOnPart1() {
        // Schalter "Nur noch mit FIN im identContext zugelassen" [AUS]
        Map<UniversalConfigOption, Boolean> configOptions = new HashMap<>();
        configOptions.put(iPartsWebservicePlugin.CONFIG_ONLY_FIN_BASED_REQUESTS_RMI, true);
        configOptions.put(iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI, false);

        executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), configOptions, new WebserviceRunnable() {

            @Override
            public void executeTest() {
                // Token enthält Sprachen zh, fr, es, d.h. zh ist die Hauptsprache und fr, es sind die Rückfallsprachen
                Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");

                // Test 4.1 Prüfung mit "Nur noch mit FIN im identContext zugelassen"
                // Datenkarte zur FIN nicht vorhanden
                executeWebservice(iPartsWSPartsListEndpoint.DEFAULT_ENDPOINT_URI + "?finOrVin=WDD2050041F111111&productId=C01&includeAggs=true",
                                  null, additionalRequestProperties,
                                  DWFile.get(getTestWorkingDir(), "resultOnlyFinBasedRequestPartsListTrue.txt"), HttpConstants.HTTP_STATUS_NOT_FOUND);
            }
        });
    }

    public void testGetPartsRequiredFinOrVinIdentContextPartsListOnPart2() {
        // Schalter "Nur noch mit FIN im identContext zugelassen" [AUS]
        Map<UniversalConfigOption, Boolean> configOptions = new HashMap<>();
        configOptions.put(iPartsWebservicePlugin.CONFIG_ONLY_FIN_BASED_REQUESTS_RMI, false);
        configOptions.put(iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI, false);

        executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), configOptions, new WebserviceRunnable() {

            @Override
            public void executeTest() {
                // Token enthält Sprachen zh, fr, es, d.h. zh ist die Hauptsprache und fr, es sind die Rückfallsprachen
                Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");

                // Test 4.2 Prüfung ohne "Nur noch mit FIN im identContext zugelassen"
                // Datenkarte zur FIN vorhanden
                //
                // Dauert ca. 1 min!
                //
                executeWebservice(iPartsWSPartsListEndpoint.DEFAULT_ENDPOINT_URI + "?finOrVin=WDD2050041F004362&productId=C01&includeAggs=true",
                                  null, additionalRequestProperties,
                                  DWFile.get(getTestWorkingDir(), "resultOnlyFinBasedRequestPartsListFalse.txt"));
            }
        });
    }

    public void testGetPartsRequiredFinOrVinIdentContextSearchComponentOn() {
        // Schalter "Nur noch mit FIN im identContext zugelassen" [AUS]
        Map<UniversalConfigOption, Boolean> configOptions = new HashMap<>();
        configOptions.put(iPartsWebservicePlugin.CONFIG_ONLY_FIN_BASED_REQUESTS_RMI, true);
        configOptions.put(iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI, false);

        executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), configOptions, new WebserviceRunnable() {

            @Override
            public void executeTest() {
                // Test 5.1 Prüfung mit "Nur noch mit FIN im identContext zugelassen"
                executeWebservice(iPartsWSsearchComponentEndpoint.DEFAULT_ENDPOINT_URI, "{\"identContext\":{\"aggTypeId\":\"M\",\"productClassIds\":[\"P\"],\"modelId\":\"D936910\",\"productId\":\"M01\",\"fin\":\"WDB9676071L972285\"},\"searchText\":\"540025/19\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                                  "{\"code\":4041,\"message\":\"Datacard not found\"}", HttpConstants.HTTP_STATUS_NOT_FOUND);

            }
        });
    }

    public void testGetPartsRequiredFinOrVinIdentContextSearchComponentOff() {
        // Schalter "Nur noch mit FIN im identContext zugelassen" [AUS]
        Map<UniversalConfigOption, Boolean> configOptions = new HashMap<>();
        configOptions.put(iPartsWebservicePlugin.CONFIG_ONLY_FIN_BASED_REQUESTS_RMI, false);
        configOptions.put(iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI, false);

        executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), configOptions, new WebserviceRunnable() {

            @Override
            public void executeTest() {
                // Test 5.2 Prüfung ohne "Nur noch mit FIN im identContext zugelassen"
                executeWebservice(iPartsWSsearchComponentEndpoint.DEFAULT_ENDPOINT_URI, "{\"identContext\":{\"aggTypeId\":\"M\",\"productClassIds\":[\"P\"],\"modelId\":\"D936910\",\"productId\":\"M01\",\"fin\":\"WDB9676071L972285\"},\"searchText\":\"540025/19\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                                  DWFile.get(getTestWorkingDir(), "resultOnlyFinBasedRequestSearchComponents.txt"));

            }
        });
    }

    public void testGetPartsRequiredFinOrVinIdentContextSearchPartsOn() {
        // Schalter "Nur noch mit FIN im identContext zugelassen" [AUS]
        Map<UniversalConfigOption, Boolean> configOptions = new HashMap<>();
        configOptions.put(iPartsWebservicePlugin.CONFIG_ONLY_FIN_BASED_REQUESTS_RMI, true);
        configOptions.put(iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI, false);

        executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), configOptions, new WebserviceRunnable() {

            @Override
            public void executeTest() {
                // Test 6.1 Prüfung mit "Nur noch mit FIN im identContext zugelassen"
                // datacardExists: TRUE
                executeWebservice(iPartsWSSearchPartsEndpoint.DEFAULT_ENDPOINT_URI,
                                  "{\"identContext\": {\"aggTypeId\": \"F\",\"datacardExists\": \"true\",\"fin\":\"WDB9630031L738999\",\"modelId\": \"C963003\",\"productId\": \"S01\",\"productClassIds\":[\"F\",\"L\",\"O\",\"P\"]},\"searchText\": \"A0015448090\", \"user\": {\"country\": \"200\",\"language\": \"de\",\"userId\": \"TRKA_tec_00\"},\"includeAggs\" : \"true\"}",
                                  "{\"searchResults\":[{\"description\":\"HELLA\",\"name\":\"\",\"navContext\":[{\"id\":\"58\",\"label\":\"WERKZEUG UND ZUBEHOER\",\"type\":\"cg_group\"},{\"id\":\"Z 506.389\",\"label\":\"SV WARNLEUCHTE\",\"partsAvailable\":true,\"type\":\"sa_number\"}],\"partNo\":\"A0015448090\",\"partNoFormatted\":\"A 001 544 80 90\"}]}");

                // Test 6.2 Prüfung mit "Nur noch mit FIN im identContext zugelassen"
                // datacardExists: FALSE
                executeWebservice(iPartsWSSearchPartsEndpoint.DEFAULT_ENDPOINT_URI,
                                  "{\"identContext\": {\"aggTypeId\": \"F\",\"datacardExists\": \"false\",\"fin\":\"WDB9630031L738999\",\"modelId\": \"C963003\",\"productId\": \"S01\",\"productClassIds\":[\"F\",\"L\",\"O\",\"P\"]},\"searchText\": \"A0015448090\", \"user\": {\"country\": \"200\",\"language\": \"de\",\"userId\": \"TRKA_tec_00\"},\"includeAggs\" : \"true\"}",
                                  "{\"code\":4041,\"message\":\"Datacard not found\"}", HttpConstants.HTTP_STATUS_NOT_FOUND);

            }
        });
    }

    public void testGetPartsRequiredFinOrVinIdentContextSearchPartsOff() {
        // Schalter "Nur noch mit FIN im identContext zugelassen" [AUS]
        Map<UniversalConfigOption, Boolean> configOptions = new HashMap<>();
        configOptions.put(iPartsWebservicePlugin.CONFIG_ONLY_FIN_BASED_REQUESTS_RMI, false);
        configOptions.put(iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI, false);

        executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), configOptions, new WebserviceRunnable() {

            @Override
            public void executeTest() {
                // Test 6.3 Prüfung ohne "Nur noch mit FIN im identContext zugelassen"
                // datacardExists: FALSE
                executeWebservice(iPartsWSSearchPartsEndpoint.DEFAULT_ENDPOINT_URI,
                                  "{\"identContext\": {\"aggTypeId\": \"F\",\"datacardExists\": \"false\",\"fin\":\"WDB9630031L738999\",\"modelId\": \"C963003\",\"productId\": \"S01\",\"productClassIds\":[\"F\",\"L\",\"O\",\"P\"]},\"searchText\": \"A0015448090\", \"user\": {\"country\": \"200\",\"language\": \"de\",\"userId\": \"TRKA_tec_00\"},\"includeAggs\" : \"true\"}",
                                  "{\"searchResults\":[]}");
            }
        });
    }

    /**
     * Jetzt werden hier zusätzlich die EinPAS-Daten, je nach RMI-Konfigurationsschalter, weggelassen oder dazugemischt.
     */
    public void testGetPartsForProductionAggregateDatacard() {
        // DAIMLER-7362 Für das Fahrerhaus kann es sein dass es keine richtige Aggregate Datenkarte innerhalb der Fahrzeugdatenkarte gibt,
        // dann soll das Feld originCabId ausgelesen werden, und der Wert zur Baumuster-Filterung verwendet werden
        // Der Fallback wird nur gemacht wenn am zugehörigen Fahrzeug Produkt das Flag "Werkseitig verbaute Aggregate nutzen" true ist,
        // Für diesen Testfall wurde das Flag am Produkt 62U entsprechend gesetzt

        Map<UniversalConfigOption, Boolean> configOptions = new HashMap<>();
        String requestString = "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"T\"],\"modelId\":\"C166823\",\"productId\":\"D81\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"82\"},{\"type\":\"cg_subgroup\",\"id\":\"175\"}],\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00162\"}}";

        // Ohne gesetzten Schalter werden die Ergebnisdaten MIT den EinPAS-Daten angereichert ausgegeben:
        configOptions.put(iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI, false);
        executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), configOptions, new WebserviceRunnable() {
            @Override
            public void executeTest() {
                executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI, requestString,
                                  "{\"images\":[],\"parts\":[{\"calloutId\":\"10\",\"codeValidity\":\"(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00018\"},\"partNo\":\"A1668202161\",\"partNoFormatted\":\"A 166 820 21 61\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00019\"},\"partNo\":\"A1668208459\",\"partNoFormatted\":\"A 166 820 84 59\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00080\"},\"partNo\":\"A1668202261\",\"partNoFormatted\":\"A 166 820 22 61\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00081\"},\"partNo\":\"A1668208559\",\"partNoFormatted\":\"A 166 820 85 59\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"613+(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00020\"},\"partNo\":\"A1668202361\",\"partNoFormatted\":\"A 166 820 23 61\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"613+(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00021\"},\"partNo\":\"A1668208659\",\"partNoFormatted\":\"A 166 820 86 59\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"613+(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00082\"},\"partNo\":\"A1668202461\",\"partNoFormatted\":\"A 166 820 24 61\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"613+(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00083\"},\"partNo\":\"A1668208759\",\"partNoFormatted\":\"A 166 820 87 59\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"(806+056/807/808/809/800)+-(P61/494/460/496/835/640/641/642/(832+703));\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00029\"},\"partNo\":\"A1668209159\",\"partNoFormatted\":\"A 166 820 91 59\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"(806+056/807/808/809/800)+-(P61/494/460/496/835/640/641/642/(832+703));\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00091\"},\"partNo\":\"A1668209259\",\"partNoFormatted\":\"A 166 820 92 59\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"613+(806+056/807/808/809/800)+-(P61/494/460/496/835/640/641/642/(832+703));\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00030\"},\"partNo\":\"A1668209359\",\"partNoFormatted\":\"A 166 820 93 59\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"613+(806+056/807/808/809/800)+-(P61/494/460/496/835/640/641/642/(832+703));\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00092\"},\"partNo\":\"A1668209459\",\"partNoFormatted\":\"A 166 820 94 59\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"(494/460/496/835)+(806+056/807/808/809/800);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00031\"},\"partNo\":\"A1668202100\",\"partNoFormatted\":\"A 166 820 21 00\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"(494/460/496/835)+(806+056/807/808/809/800);\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00093\"},\"partNo\":\"A1668202200\",\"partNoFormatted\":\"A 166 820 22 00\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"642+(806+056/807/808+-058);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00032\"},\"partNo\":\"A1668202300\",\"partNoFormatted\":\"A 166 820 23 00\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"642+(808+058/809/800);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00033\"},\"partNo\":\"A1669065703\",\"partNoFormatted\":\"A 166 906 57 03\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"642+(806+056/807/808+-058);\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00094\"},\"partNo\":\"A1668202400\",\"partNoFormatted\":\"A 166 820 24 00\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"642+(808+058/809/800);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00095\"},\"partNo\":\"A1669065803\",\"partNoFormatted\":\"A 166 906 58 03\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"641+(806+056/807/808+-058);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00034\"},\"partNo\":\"A1668202500\",\"partNoFormatted\":\"A 166 820 25 00\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"641+(808+058/809/800);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00035\"},\"partNo\":\"A1669065903\",\"partNoFormatted\":\"A 166 906 59 03\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"641+(806+056/807/808+-058);\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00096\"},\"partNo\":\"A1668202600\",\"partNoFormatted\":\"A 166 820 26 00\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"641+(808+058/809/800);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00097\"},\"partNo\":\"A1669066003\",\"partNoFormatted\":\"A 166 906 60 03\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"(640+(494/460/496/835))+(806+056/807/808+-058);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00036\"},\"partNo\":\"A1668202700\",\"partNoFormatted\":\"A 166 820 27 00\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"(640+(494/460/496/835))+(806+056/807/808+-058);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00037\"},\"partNo\":\"A1669066903\",\"partNoFormatted\":\"A 166 906 69 03\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"(640+(494/460/496/835))+(808+058/809/800);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00038\"},\"partNo\":\"A1669066503\",\"partNoFormatted\":\"A 166 906 65 03\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"(640+(494/460/496/835))+(806+056/807/808+-058);\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00098\"},\"partNo\":\"A1668202800\",\"partNoFormatted\":\"A 166 820 28 00\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"(640+(494/460/496/835))+(806+056/807/808+-058);\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00099\"},\"partNo\":\"A1669067003\",\"partNoFormatted\":\"A 166 906 70 03\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"(640+(494/460/496/835))+(808+058/809/800);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00100\"},\"partNo\":\"A1669066603\",\"partNoFormatted\":\"A 166 906 66 03\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"(494/460/496)+(803/804/805/806+-056)+-(06T/615);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00026\"},\"partNo\":\"A1668206961\",\"partNoFormatted\":\"A 166 820 69 61\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"(494/460/496)+(803/804/805/806+-056)+-(06T/615);\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00088\"},\"partNo\":\"A1668207061\",\"partNoFormatted\":\"A 166 820 70 61\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"615+(494/460/496)+(803/804/805/806+-056);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00027\"},\"partNo\":\"A1668205761\",\"partNoFormatted\":\"A 166 820 57 61\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"615+(494/460/496)+(803/804/805/806+-056);\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00089\"},\"partNo\":\"A1668205861\",\"partNoFormatted\":\"A 166 820 58 61\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"610+615+(494/460/496)+(803/804/805/806+-056);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00028\"},\"partNo\":\"A1668205961\",\"partNoFormatted\":\"A 166 820 59 61\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"610+615+(494/460/496)+(803/804/805/806+-056);\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00090\"},\"partNo\":\"A1668206061\",\"partNoFormatted\":\"A 166 820 60 61\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"622+(803/804/805/806+-056)+-(460/494/496);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00022\"},\"partNo\":\"A1668207361\",\"partNoFormatted\":\"A 166 820 73 61\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"622+(803/804/805/806+-056)+-(460/494/496);\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00084\"},\"partNo\":\"A1668207461\",\"partNoFormatted\":\"A 166 820 74 61\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"621+(803/804/805/806+-056)+-(460/494/496);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00023\"},\"partNo\":\"A1668207561\",\"partNoFormatted\":\"A 166 820 75 61\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"621+(803/804/805/806+-056)+-(460/494/496);\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00085\"},\"partNo\":\"A1668207661\",\"partNoFormatted\":\"A 166 820 76 61\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"610+622+(803/804/805/806+-056)+-(460/494/496);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00024\"},\"partNo\":\"A1668207961\",\"partNoFormatted\":\"A 166 820 79 61\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"610+622+(803/804/805/806+-056)+-(460/494/496);\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00086\"},\"partNo\":\"A1668208061\",\"partNoFormatted\":\"A 166 820 80 61\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"610+621+(803/804/805/806+-056)+-(460/494/496);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00025\"},\"partNo\":\"A1668208161\",\"partNoFormatted\":\"A 166 820 81 61\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"610+621+(803/804/805/806+-056)+-(460/494/496);\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00087\"},\"partNo\":\"A1668208261\",\"partNoFormatted\":\"A 166 820 82 61\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"P61+642+(808/809/800);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00041\"},\"partNo\":\"A1669065703\",\"partNoFormatted\":\"A 166 906 57 03\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"P61+641+(808/809/800);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00042\"},\"partNo\":\"A1669065903\",\"partNoFormatted\":\"A 166 906 59 03\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"P61+640+(494/460/496/835)+(808/809/800);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00043\"},\"partNo\":\"A1669066103\",\"partNoFormatted\":\"A 166 906 61 03\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"P61+640+(494/460/496/835)+(808/809/800);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00044\"},\"partNo\":\"A1669066503\",\"partNoFormatted\":\"A 166 906 65 03\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"P61+642+(808/809/800);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00103\"},\"partNo\":\"A1669065803\",\"partNoFormatted\":\"A 166 906 58 03\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"P61+641+(808/809/800);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00104\"},\"partNo\":\"A1669066003\",\"partNoFormatted\":\"A 166 906 60 03\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"P61+640+(494/460/496/835)+(808/809/800);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00105\"},\"partNo\":\"A1669066203\",\"partNoFormatted\":\"A 166 906 62 03\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"P61+640+(494/460/496/835)+(808/809/800);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00106\"},\"partNo\":\"A1669066603\",\"partNoFormatted\":\"A 166 906 66 03\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"20\",\"codeValidity\":\"-(615/621/622/640/641/642);\",\"damageCodes\":[\"82268\"],\"level\":\"02\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00052\"},\"partNo\":\"N000000008150\",\"partNoFormatted\":\"N 000000 008150\",\"quantity\":\"NB\"},{\"calloutId\":\"40\",\"codeValidity\":\"(615/621/622)+(803/804/805);\",\"damageCodes\":[\"82126\"],\"level\":\"02\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00055\"},\"partNo\":\"N000000007622\",\"partNoFormatted\":\"N 000000 007622\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"40\",\"codeValidity\":\"(615/621/622)+(803/804/805/806+-056);\",\"damageCodes\":[\"82126\"],\"level\":\"02\",\"name\":\"GASENTLAD.LAMPE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00056\"},\"partNo\":\"N000000004248\",\"partNoFormatted\":\"N 000000 004248\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"50\",\"codeValidity\":\"-(494/460/496/615/621/622);\",\"damageCodes\":[\"82141\"],\"level\":\"02\",\"name\":\"GLUEHLAMPE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00057\"},\"partNo\":\"N000000006370\",\"partNoFormatted\":\"N 000000 006370\",\"quantity\":\"NB\"},{\"calloutId\":\"70\",\"codeValidity\":\"-(615/621/622/640/641/642);\",\"damageCodes\":[\"82123\"],\"level\":\"02\",\"name\":\"GLUEHLAMPE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00053\"},\"partNo\":\"N400809000007\",\"partNoFormatted\":\"N 400809 000007\",\"quantity\":\"NB\"},{\"calloutId\":\"110\",\"codeValidity\":\"(615/621/622)+(803/805/805/806/807/808/809/800);\",\"damageCodes\":[\"82155\"],\"level\":\"02\",\"name\":\"SCHUTZHAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00045\"},\"partNo\":\"A0008268124\",\"partNoFormatted\":\"A 000 826 81 24\",\"quantity\":\"NB\"},{\"calloutId\":\"110\",\"codeValidity\":\"(803/804/805/805/806+-056)+-(615/621/622);\",\"damageCodes\":[\"82155\"],\"level\":\"02\",\"name\":\"SCHUTZHAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00046\"},\"partNo\":\"A0008268124\",\"partNoFormatted\":\"A 000 826 81 24\",\"quantity\":\"NB\"},{\"calloutId\":\"110\",\"codeValidity\":\"(806+056/807/808/809/800)+-(832+703);\",\"damageCodes\":[\"82155\"],\"level\":\"02\",\"name\":\"SCHUTZHAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00048\"},\"partNo\":\"A0008264324\",\"partNoFormatted\":\"A 000 826 43 24\",\"quantity\":\"NB\",\"replacedBy\":[{\"name\":\"SCHUTZHAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00049\"},\"partNo\":\"A0008268124\",\"partNoFormatted\":\"A 000 826 81 24\"}]},{\"calloutId\":\"110\",\"codeValidity\":\"(806+056/807/808/809/800)+-(832+703);\",\"damageCodes\":[\"82155\"],\"level\":\"02\",\"name\":\"SCHUTZHAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00049\"},\"partNo\":\"A0008268124\",\"partNoFormatted\":\"A 000 826 81 24\",\"quantity\":\"NB\"},{\"calloutId\":\"110\",\"codeValidity\":\"(615/621/622)+(803/805/805/806/807/808/809/800)+-(832+703);\",\"damageCodes\":[\"82155\"],\"level\":\"02\",\"name\":\"SCHUTZHAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00107\"},\"partNo\":\"A0008268124\",\"partNoFormatted\":\"A 000 826 81 24\",\"quantity\":\"NB\"},{\"calloutId\":\"110\",\"codeValidity\":\"(803/804/805/805/806+-056)+-(615/621/622/832+703);\",\"damageCodes\":[\"82155\"],\"level\":\"02\",\"name\":\"SCHUTZHAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00108\"},\"partNo\":\"A0008268124\",\"partNoFormatted\":\"A 000 826 81 24\",\"quantity\":\"NB\"},{\"calloutId\":\"120\",\"codeValidity\":\"-(832+703);\",\"damageCodes\":[\"82155\"],\"level\":\"02\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00047\"},\"partNo\":\"A2218200249\",\"partNoFormatted\":\"A 221 820 02 49\",\"quantity\":\"NB\"},{\"calloutId\":\"130\",\"codeValidity\":\"(803/804/805/805/806+-056)+-(832+703);\",\"damageCodes\":[\"82B00\"],\"level\":\"02\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00050\"},\"partNo\":\"A1668260091\",\"partNoFormatted\":\"A 166 826 00 91\",\"quantity\":\"NB\"},{\"calloutId\":\"130\",\"codeValidity\":\"(806+056/807/808/809/800)+-(832+703);\",\"damageCodes\":[\"82B00\"],\"level\":\"02\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00051\"},\"partNo\":\"A1668260000\",\"partNoFormatted\":\"A 166 826 00 00\",\"quantity\":\"NB\"},{\"calloutId\":\"130\",\"codeValidity\":\"(803/804/805/805/806+-056)+-(832+703);\",\"damageCodes\":[\"82P00\"],\"level\":\"02\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00112\"},\"partNo\":\"A1668260391\",\"partNoFormatted\":\"A 166 826 03 91\",\"quantity\":\"NB\"},{\"calloutId\":\"130\",\"codeValidity\":\"(806+056/807/808/809/800)+-(832+703);\",\"damageCodes\":[\"82P00\"],\"level\":\"02\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00113\"},\"partNo\":\"A1668260100\",\"partNoFormatted\":\"A 166 826 01 00\",\"quantity\":\"NB\"},{\"calloutId\":\"140\",\"codeValidity\":\"ET+(640/641/642);\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00077\"},\"partNo\":\"A0008260500\",\"partNoFormatted\":\"A 000 826 05 00\",\"quantity\":\"NB\"},{\"calloutId\":\"140\",\"codeValidity\":\"(615/621/622)+(802/803/804/805);\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00078\"},\"partNo\":\"A0008260600\",\"partNoFormatted\":\"A 000 826 06 00\",\"quantity\":\"NB\"},{\"calloutId\":\"140\",\"codeValidity\":\"(806/807/808/809/800);\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00079\"},\"partNo\":\"A0008260600\",\"partNoFormatted\":\"A 000 826 06 00\",\"quantity\":\"NB\"},{\"calloutId\":\"150\",\"codeValidity\":\"(615/621/622)+(803/804/805/806/807/808/809/800);\",\"damageCodes\":[\"87A0X\",\"87A0Y\",\"87N0X\",\"87N0Y\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00062\"},\"partNo\":\"A1668203689\",\"partNoFormatted\":\"A 166 820 36 89\",\"quantity\":\"NB\",\"replacedBy\":[{\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00063\"},\"partNo\":\"A2189009203\",\"partNoFormatted\":\"A 218 900 92 03\"}]},{\"calloutId\":\"150\",\"codeValidity\":\"(615/621/622)+(803/804/805/806/807/808/809/800);\",\"damageCodes\":[\"87N0X\",\"87A0X\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00125\"},\"partNo\":\"A2189009203\",\"partNoFormatted\":\"A 218 900 92 03\",\"quantity\":\"NB\"},{\"calloutId\":\"150\",\"codeValidity\":\"(610+(615/615+(494/496/460)/621/622))+(802/803/804/805);\",\"damageCodes\":[\"87A0X\",\"87A0Y\",\"87N0X\",\"87N0Y\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00064\"},\"partNo\":\"A1668203689\",\"partNoFormatted\":\"A 166 820 36 89\",\"quantity\":\"NB\",\"replacedBy\":[{\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00065\"},\"partNo\":\"A2189009203\",\"partNoFormatted\":\"A 218 900 92 03\"}]},{\"calloutId\":\"150\",\"codeValidity\":\"(610+(615/615+(494/496/460)/621/622))+(802/803/804/805);\",\"damageCodes\":[\"87A0X\",\"87N0X\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00065\"},\"partNo\":\"A2189009203\",\"partNoFormatted\":\"A 218 900 92 03\",\"quantity\":\"NB\"},{\"calloutId\":\"150\",\"codeValidity\":\"(610+(615/615+(494/496/460)/621/622))+(806/807/808/809/800);\",\"damageCodes\":[\"87N0X\",\"87A0X\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00128\"},\"partNo\":\"A2189009203\",\"partNoFormatted\":\"A 218 900 92 03\",\"quantity\":\"NB\"},{\"calloutId\":\"150\",\"codeValidity\":\"(615+(494/496/460))+(802/803/804/805);\",\"damageCodes\":[\"87A0X\",\"87A0Y\",\"87N0X\",\"87N0Y\"],\"level\":\"01\",\"name\":\"STEUERGERAET\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00068\"},\"partNo\":\"A2189000002\",\"partNoFormatted\":\"A 218 900 00 02\",\"quantity\":\"NB\",\"replacedBy\":[{\"name\":\"STEUERGERAET\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00069\"},\"partNo\":\"A2189009103\",\"partNoFormatted\":\"A 218 900 91 03\"}]},{\"calloutId\":\"150\",\"codeValidity\":\"(615+(494/496/460/737L)/622+(494/496/460/737L)+(Z04/Z05))+(802/803/804/805);\",\"damageCodes\":[\"87A0X\",\"87N0X\"],\"level\":\"01\",\"name\":\"STEUERGERAET\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00069\"},\"partNo\":\"A2189009103\",\"partNoFormatted\":\"A 218 900 91 03\",\"quantity\":\"NB\"},{\"calloutId\":\"150\",\"codeValidity\":\"(615+(494/496/460/737L)/622+(494/496/460/737L)+(Z04/Z05))+(806/807/808/809/800);\",\"damageCodes\":[\"87N0X\",\"87A0X\"],\"level\":\"01\",\"name\":\"STEUERGERAET\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00132\"},\"partNo\":\"A2189009103\",\"partNoFormatted\":\"A 218 900 91 03\",\"quantity\":\"NB\"},{\"calloutId\":\"150\",\"codeValidity\":\"(640/641/642)+(806+056/807/808/809/800);\",\"damageCodes\":[\"87A0Y\",\"87N0Y\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00061\"},\"partNo\":\"A2228700789\",\"partNoFormatted\":\"A 222 870 07 89\",\"quantity\":\"NB\"},{\"calloutId\":\"155\",\"codeValidity\":\"(615/621/622)+(802/803/804/805);\",\"level\":\"01\",\"name\":\"FLACHKOPFSCHR.\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00071\"},\"partNo\":\"N000000002856\",\"partNoFormatted\":\"N 000000 002856\",\"quantity\":\"NB\"},{\"calloutId\":\"155\",\"codeValidity\":\"(806/807/808/809/800);\",\"level\":\"01\",\"name\":\"FLACHKOPFSCHR.\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00072\"},\"partNo\":\"N000000002856\",\"partNoFormatted\":\"N 000000 002856\",\"quantity\":\"NB\"},{\"calloutId\":\"155\",\"codeValidity\":\"(615/621/622)+(806/807/808/809/800);\",\"level\":\"01\",\"name\":\"FLACHKOPFSCHR.\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00134\"},\"partNo\":\"N000000002856\",\"partNoFormatted\":\"N 000000 002856\",\"quantity\":\"NB\"},{\"calloutId\":\"160\",\"codeValidity\":\"615/621/622;\",\"damageCodes\":[\"82B03\",\"82P03\"],\"level\":\"01\",\"name\":\"STEUERGERAET\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00039\"},\"partNo\":\"A1669002800\",\"partNoFormatted\":\"A 166 900 28 00\",\"quantity\":\"NB\"},{\"calloutId\":\"160\",\"codeValidity\":\"(640/641/642)+806+056;\",\"damageCodes\":[\"87A0X\",\"87N0X\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00058\"},\"partNo\":\"A2189009904\",\"partNoFormatted\":\"A 218 900 99 04\",\"plantInformationAvailable\":true,\"quantity\":\"NB\",\"replacedBy\":[{\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00059\"},\"partNo\":\"A2189000406\",\"partNoFormatted\":\"A 218 900 04 06\"}]},{\"calloutId\":\"160\",\"codeValidity\":\"(640/641/642)+(806+056/807);\",\"damageCodes\":[\"87A0X\",\"87N0X\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00059\"},\"partNo\":\"A2189000406\",\"partNoFormatted\":\"A 218 900 04 06\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"160\",\"codeValidity\":\"(640/641/642)+(806+056/807/808/809/800);\",\"damageCodes\":[\"87A0X\",\"87N0X\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00060\"},\"partNo\":\"A2189007306\",\"partNoFormatted\":\"A 218 900 73 06\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"165\",\"codeValidity\":\"(615/621/622)+ET;\",\"level\":\"01\",\"name\":\"FLACHKOPFSCHR.\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00040\"},\"partNo\":\"N000000002856\",\"partNoFormatted\":\"N 000000 002856\",\"quantity\":\"NB\"},{\"calloutId\":\"165\",\"codeValidity\":\"ET;\",\"level\":\"01\",\"name\":\"FLACHKOPFSCHR.\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00102\"},\"partNo\":\"N000000002856\",\"partNoFormatted\":\"N 000000 002856\",\"quantity\":\"NB\"},{\"calloutId\":\"170\",\"codeValidity\":\"ET+(640/641/642);\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00076\"},\"partNo\":\"A0008260300\",\"partNoFormatted\":\"A 000 826 03 00\",\"quantity\":\"NB\"},{\"calloutId\":\"180\",\"codeValidity\":\"ET+(803/804/805/806+-056)+-(615/621/622);\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00146\"},\"partNo\":\"A1668200314\",\"partNoFormatted\":\"A 166 820 03 14\",\"quantity\":\"NB\"},{\"calloutId\":\"180\",\"codeValidity\":\"(806+056/807/808/809/800)+-(615/621/622);\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00149\"},\"partNo\":\"A1668200714\",\"partNoFormatted\":\"A 166 820 07 14\",\"quantity\":\"NB\"},{\"calloutId\":\"180\",\"codeValidity\":\"ET+(803/804/805/806+-056)+-(615/621/622);\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00151\"},\"partNo\":\"A1668200414\",\"partNoFormatted\":\"A 166 820 04 14\",\"quantity\":\"NB\"},{\"calloutId\":\"180\",\"codeValidity\":\"(615/621/622)+(802/803/804/805);\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00147\"},\"partNo\":\"A1668200514\",\"partNoFormatted\":\"A 166 820 05 14\",\"quantity\":\"NB\"},{\"calloutId\":\"180\",\"codeValidity\":\"(615/621/622)+(806/807/808/809/800);\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00148\"},\"partNo\":\"A1668200514\",\"partNoFormatted\":\"A 166 820 05 14\",\"quantity\":\"NB\"},{\"calloutId\":\"180\",\"codeValidity\":\"(615/621/622)+(802/803/804/805);\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00152\"},\"partNo\":\"A1668200614\",\"partNoFormatted\":\"A 166 820 06 14\",\"quantity\":\"NB\"},{\"calloutId\":\"180\",\"codeValidity\":\"(615/621/622)+(806/807/808/809/800);\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00153\"},\"partNo\":\"A1668200614\",\"partNoFormatted\":\"A 166 820 06 14\",\"quantity\":\"NB\"},{\"calloutId\":\"180\",\"codeValidity\":\"(806+056/807/808/809/800)+-(615/621/622);\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00154\"},\"partNo\":\"A1668200814\",\"partNoFormatted\":\"A 166 820 08 14\",\"quantity\":\"NB\"},{\"calloutId\":\"210\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00073\"},\"partNo\":\"N000000007703\",\"partNoFormatted\":\"N 000000 007703\",\"quantity\":\"NB\"},{\"calloutId\":\"230\",\"codeValidity\":\"(806+056/807/808/809/800);\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00075\"},\"partNo\":\"A0009908623\",\"partNoFormatted\":\"A 000 990 86 23\",\"quantity\":\"NB\"},{\"calloutId\":\"240\",\"codeValidity\":\"(806+056/807/808/809/800);\",\"level\":\"01\",\"name\":\"SCHRAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00074\"},\"partNo\":\"A0019908000\",\"partNoFormatted\":\"A 001 990 80 00\",\"quantity\":\"NB\"},{\"calloutId\":\"250\",\"codeValidity\":\"(803/804/805/806+-056);\",\"damageCodes\":[\"82128\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00160\"},\"partNo\":\"A2469017100\",\"partNoFormatted\":\"A 246 901 71 00\",\"quantity\":\"NB\",\"replacedBy\":[{\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00161\"},\"partNo\":\"A2469012504\",\"partNoFormatted\":\"A 246 901 25 04\"}]},{\"calloutId\":\"250\",\"codeValidity\":\"(803/804/805/806+-056);\",\"damageCodes\":[\"82128\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00161\"},\"partNo\":\"A2469012504\",\"partNoFormatted\":\"A 246 901 25 04\",\"quantity\":\"NB\"},{\"calloutId\":\"250\",\"codeValidity\":\"(806+056/807/808/809/800);\",\"damageCodes\":[\"5472D\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00164\"},\"partNo\":\"A2469002503\",\"partNoFormatted\":\"A 246 900 25 03\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"250\",\"codeValidity\":\"(806+056/807/808/809/800);\",\"damageCodes\":[\"5472D\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00165\"},\"partNo\":\"A2469002603\",\"partNoFormatted\":\"A 246 900 26 03\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"250\",\"codeValidity\":\"(806+056/807)+1U7;\",\"damageCodes\":[\"5472D\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00166\"},\"partNo\":\"A2469002603\",\"partNoFormatted\":\"A 246 900 26 03\",\"quantity\":\"NB\"},{\"calloutId\":\"250\",\"codeValidity\":\"804+610;\",\"damageCodes\":[\"82128\"],\"einPASNodeAvailable\":true,\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00162\"},\"partNo\":\"A1669012600\",\"partNoFormatted\":\"A 166 901 26 00\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"250\",\"codeValidity\":\"804+(863/865)+(610/610+(476/238/513/608));\",\"damageCodes\":[\"82128\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00163\"},\"partNo\":\"A2469012504\",\"partNoFormatted\":\"A 246 901 25 04\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"260\",\"codeValidity\":\"(642/641)+(806+056/807/808/809/800);\",\"damageCodes\":[\"82F0K\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00142\"},\"partNo\":\"A2189065800\",\"partNoFormatted\":\"A 218 906 58 00\",\"quantity\":\"NB\"},{\"calloutId\":\"260\",\"codeValidity\":\"(642/641)+(806+056/807/808/809/800);\",\"damageCodes\":[\"82U0K\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00144\"},\"partNo\":\"A2189065900\",\"partNoFormatted\":\"A 218 906 59 00\",\"quantity\":\"NB\"},{\"calloutId\":\"260\",\"codeValidity\":\"(640+(494/460/496))+(806+056/807/808/809/800);\",\"damageCodes\":[\"82F0K\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00143\"},\"partNo\":\"A2189066100\",\"partNoFormatted\":\"A 218 906 61 00\",\"quantity\":\"NB\"},{\"calloutId\":\"260\",\"codeValidity\":\"(640+(494/460/496))+(806+056/807/808/809/800);\",\"damageCodes\":[\"82U0K\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00145\"},\"partNo\":\"A2189066200\",\"partNoFormatted\":\"A 218 906 62 00\",\"quantity\":\"NB\"},{\"calloutId\":\"270\",\"codeValidity\":\"(806+056/807/808/809/800);\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00150\"},\"partNo\":\"A0234207518\",\"partNoFormatted\":\"A 023 420 75 18\",\"quantity\":\"NB\"},{\"calloutId\":\"400\",\"codeValidity\":\"(802/803/804/805/806+-056)+-772;\",\"damageCodes\":[\"87A10\"],\"level\":\"01\",\"name\":\"TAGFAHRLEUCHTE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00006\"},\"partNo\":\"A0999065700\",\"partNoFormatted\":\"A 099 906 57 00\",\"plantInformationAvailable\":true,\"quantity\":\"NB\",\"replacedBy\":[{\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00007\"},\"partNo\":\"A0999068101\",\"partNoFormatted\":\"A 099 906 81 01\"}]},{\"calloutId\":\"400\",\"codeValidity\":\"(802/803/804/805/806+-056)+-772;\",\"damageCodes\":[\"87A10\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00007\"},\"partNo\":\"A0999068101\",\"partNoFormatted\":\"A 099 906 81 01\",\"quantity\":\"NB\"},{\"calloutId\":\"400\",\"codeValidity\":\"(802/803/804/805/806+-056)+-772;\",\"damageCodes\":[\"87N10\"],\"level\":\"01\",\"name\":\"TAGFAHRLEUCHTE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00012\"},\"partNo\":\"A0999065800\",\"partNoFormatted\":\"A 099 906 58 00\",\"plantInformationAvailable\":true,\"quantity\":\"NB\",\"replacedBy\":[{\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00013\"},\"partNo\":\"A0999068201\",\"partNoFormatted\":\"A 099 906 82 01\"}]},{\"calloutId\":\"400\",\"codeValidity\":\"(802/803/804/805/806+-056)+-772;\",\"damageCodes\":[\"87N10\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00013\"},\"partNo\":\"A0999068201\",\"partNoFormatted\":\"A 099 906 82 01\",\"quantity\":\"NB\"},{\"calloutId\":\"400\",\"codeValidity\":\"(802/803/804/805/806+-056)+772;\",\"damageCodes\":[\"87A10\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00008\"},\"partNo\":\"A1649060351\",\"partNoFormatted\":\"A 164 906 03 51\",\"quantity\":\"NB\"},{\"calloutId\":\"400\",\"codeValidity\":\"772+(802/803/804/805/806+-056);\",\"damageCodes\":[\"87A10\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00014\"},\"partNo\":\"A1649060451\",\"partNoFormatted\":\"A 164 906 04 51\",\"quantity\":\"NB\"},{\"calloutId\":\"470\",\"codeValidity\":\"772+(802/803/804/805/806+-056)+-M157;\",\"level\":\"01\",\"name\":\"BLECHSCHRAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00009\"},\"partNo\":\"N000000000529\",\"partNoFormatted\":\"N 000000 000529\",\"quantity\":\"NB\"},{\"calloutId\":\"470\",\"codeValidity\":\"772+(802/803/804/805/806+-056)+-M157;\",\"level\":\"01\",\"name\":\"FEDERMUTTER\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00010\"},\"partNo\":\"A0039948645\",\"partNoFormatted\":\"A 003 994 86 45\",\"quantity\":\"NB\"},{\"calloutId\":\"480\",\"codeValidity\":\"772+(802/803/804/805/806+-056)+-M157;\",\"level\":\"01\",\"name\":\"FEDERMUTTER\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00016\"},\"partNo\":\"A0039948645\",\"partNoFormatted\":\"A 003 994 86 45\",\"quantity\":\"NB\"},{\"calloutId\":\"500\",\"codeValidity\":\"(U60+(803/804/805/806+-056))+-(832+703);\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00001\"},\"partNo\":\"A1665408332\",\"partNoFormatted\":\"A 166 540 83 32\",\"quantity\":\"NB\"},{\"calloutId\":\"500\",\"codeValidity\":\"(803/804/805/806+-056)+-(U60/(832+703));\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00002\"},\"partNo\":\"A1665408032\",\"partNoFormatted\":\"A 166 540 80 32\",\"quantity\":\"NB\"},{\"calloutId\":\"900\",\"description\":\"E1*1, E2*1\",\"level\":\"01\",\"name\":\"KUPPL.GEHAEUSE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00155\"},\"partNo\":\"A0525455626\",\"partNoFormatted\":\"A 052 545 56 26\",\"quantity\":\"NB\"},{\"calloutId\":\"901\",\"description\":\"B38/2\",\"level\":\"01\",\"name\":\"KONTAKTBUCHSE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00156\"},\"partNo\":\"A0255452026\",\"partNoFormatted\":\"A 025 545 20 26\",\"quantity\":\"NB\"}]}");
            }
        });

        // Mit gesetztem Schalter werden die Ergebnisdaten OHNE die EinPAS-Daten ausgegeben.
        configOptions.put(iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI, true);
        executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), configOptions, new WebserviceRunnable() {
            @Override
            public void executeTest() {
                executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI, requestString,
                                  "{\"parts\":[{\"codeValidity\":\"(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00018\"},\"partNo\":\"A1668202161\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00019\"},\"partNo\":\"A1668208459\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00080\"},\"partNo\":\"A1668202261\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00081\"},\"partNo\":\"A1668208559\",\"plantInformationAvailable\":true},{\"codeValidity\":\"613+(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00020\"},\"partNo\":\"A1668202361\",\"plantInformationAvailable\":true},{\"codeValidity\":\"613+(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00021\"},\"partNo\":\"A1668208659\",\"plantInformationAvailable\":true},{\"codeValidity\":\"613+(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00082\"},\"partNo\":\"A1668202461\",\"plantInformationAvailable\":true},{\"codeValidity\":\"613+(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00083\"},\"partNo\":\"A1668208759\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(806+056/807/808/809/800)+-(P61/494/460/496/835/640/641/642/(832+703));\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00029\"},\"partNo\":\"A1668209159\"},{\"codeValidity\":\"(806+056/807/808/809/800)+-(P61/494/460/496/835/640/641/642/(832+703));\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00091\"},\"partNo\":\"A1668209259\"},{\"codeValidity\":\"613+(806+056/807/808/809/800)+-(P61/494/460/496/835/640/641/642/(832+703));\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00030\"},\"partNo\":\"A1668209359\"},{\"codeValidity\":\"613+(806+056/807/808/809/800)+-(P61/494/460/496/835/640/641/642/(832+703));\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00092\"},\"partNo\":\"A1668209459\"},{\"codeValidity\":\"(494/460/496/835)+(806+056/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00031\"},\"partNo\":\"A1668202100\"},{\"codeValidity\":\"(494/460/496/835)+(806+056/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00093\"},\"partNo\":\"A1668202200\"},{\"codeValidity\":\"642+(806+056/807/808+-058);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00032\"},\"partNo\":\"A1668202300\"},{\"codeValidity\":\"642+(808+058/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00033\"},\"partNo\":\"A1669065703\"},{\"codeValidity\":\"642+(806+056/807/808+-058);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00094\"},\"partNo\":\"A1668202400\"},{\"codeValidity\":\"642+(808+058/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00095\"},\"partNo\":\"A1669065803\"},{\"codeValidity\":\"641+(806+056/807/808+-058);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00034\"},\"partNo\":\"A1668202500\"},{\"codeValidity\":\"641+(808+058/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00035\"},\"partNo\":\"A1669065903\"},{\"codeValidity\":\"641+(806+056/807/808+-058);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00096\"},\"partNo\":\"A1668202600\"},{\"codeValidity\":\"641+(808+058/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00097\"},\"partNo\":\"A1669066003\"},{\"codeValidity\":\"(640+(494/460/496/835))+(806+056/807/808+-058);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00036\"},\"partNo\":\"A1668202700\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(640+(494/460/496/835))+(806+056/807/808+-058);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00037\"},\"partNo\":\"A1669066903\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(640+(494/460/496/835))+(808+058/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00038\"},\"partNo\":\"A1669066503\"},{\"codeValidity\":\"(640+(494/460/496/835))+(806+056/807/808+-058);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00098\"},\"partNo\":\"A1668202800\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(640+(494/460/496/835))+(806+056/807/808+-058);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00099\"},\"partNo\":\"A1669067003\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(640+(494/460/496/835))+(808+058/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00100\"},\"partNo\":\"A1669066603\"},{\"codeValidity\":\"(494/460/496)+(803/804/805/806+-056)+-(06T/615);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00026\"},\"partNo\":\"A1668206961\"},{\"codeValidity\":\"(494/460/496)+(803/804/805/806+-056)+-(06T/615);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00088\"},\"partNo\":\"A1668207061\"},{\"codeValidity\":\"615+(494/460/496)+(803/804/805/806+-056);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00027\"},\"partNo\":\"A1668205761\"},{\"codeValidity\":\"615+(494/460/496)+(803/804/805/806+-056);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00089\"},\"partNo\":\"A1668205861\"},{\"codeValidity\":\"610+615+(494/460/496)+(803/804/805/806+-056);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00028\"},\"partNo\":\"A1668205961\"},{\"codeValidity\":\"610+615+(494/460/496)+(803/804/805/806+-056);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00090\"},\"partNo\":\"A1668206061\"},{\"codeValidity\":\"622+(803/804/805/806+-056)+-(460/494/496);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00022\"},\"partNo\":\"A1668207361\"},{\"codeValidity\":\"622+(803/804/805/806+-056)+-(460/494/496);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00084\"},\"partNo\":\"A1668207461\"},{\"codeValidity\":\"621+(803/804/805/806+-056)+-(460/494/496);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00023\"},\"partNo\":\"A1668207561\"},{\"codeValidity\":\"621+(803/804/805/806+-056)+-(460/494/496);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00085\"},\"partNo\":\"A1668207661\"},{\"codeValidity\":\"610+622+(803/804/805/806+-056)+-(460/494/496);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00024\"},\"partNo\":\"A1668207961\"},{\"codeValidity\":\"610+622+(803/804/805/806+-056)+-(460/494/496);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00086\"},\"partNo\":\"A1668208061\"},{\"codeValidity\":\"610+621+(803/804/805/806+-056)+-(460/494/496);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00025\"},\"partNo\":\"A1668208161\"},{\"codeValidity\":\"610+621+(803/804/805/806+-056)+-(460/494/496);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00087\"},\"partNo\":\"A1668208261\"},{\"codeValidity\":\"P61+642+(808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00041\"},\"partNo\":\"A1669065703\"},{\"codeValidity\":\"P61+641+(808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00042\"},\"partNo\":\"A1669065903\"},{\"codeValidity\":\"P61+640+(494/460/496/835)+(808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00043\"},\"partNo\":\"A1669066103\",\"plantInformationAvailable\":true},{\"codeValidity\":\"P61+640+(494/460/496/835)+(808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00044\"},\"partNo\":\"A1669066503\",\"plantInformationAvailable\":true},{\"codeValidity\":\"P61+642+(808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00103\"},\"partNo\":\"A1669065803\"},{\"codeValidity\":\"P61+641+(808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00104\"},\"partNo\":\"A1669066003\"},{\"codeValidity\":\"P61+640+(494/460/496/835)+(808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00105\"},\"partNo\":\"A1669066203\",\"plantInformationAvailable\":true},{\"codeValidity\":\"P61+640+(494/460/496/835)+(808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00106\"},\"partNo\":\"A1669066603\",\"plantInformationAvailable\":true},{\"codeValidity\":\"-(615/621/622/640/641/642);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00052\"},\"partNo\":\"N000000008150\"},{\"codeValidity\":\"(615/621/622)+(803/804/805);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00055\"},\"partNo\":\"N000000007622\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(615/621/622)+(803/804/805/806+-056);\",\"name\":\"GASENTLAD.LAMPE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00056\"},\"partNo\":\"N000000004248\",\"plantInformationAvailable\":true},{\"codeValidity\":\"-(494/460/496/615/621/622);\",\"name\":\"GLUEHLAMPE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00057\"},\"partNo\":\"N000000006370\"},{\"codeValidity\":\"-(615/621/622/640/641/642);\",\"name\":\"GLUEHLAMPE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00053\"},\"partNo\":\"N400809000007\"},{\"codeValidity\":\"(615/621/622)+(803/805/805/806/807/808/809/800);\",\"name\":\"SCHUTZHAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00045\"},\"partNo\":\"A0008268124\"},{\"codeValidity\":\"(803/804/805/805/806+-056)+-(615/621/622);\",\"name\":\"SCHUTZHAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00046\"},\"partNo\":\"A0008268124\"},{\"codeValidity\":\"(806+056/807/808/809/800)+-(832+703);\",\"name\":\"SCHUTZHAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00048\"},\"partNo\":\"A0008264324\"},{\"codeValidity\":\"(806+056/807/808/809/800)+-(832+703);\",\"name\":\"SCHUTZHAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00049\"},\"partNo\":\"A0008268124\"},{\"codeValidity\":\"(615/621/622)+(803/805/805/806/807/808/809/800)+-(832+703);\",\"name\":\"SCHUTZHAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00107\"},\"partNo\":\"A0008268124\"},{\"codeValidity\":\"(803/804/805/805/806+-056)+-(615/621/622/832+703);\",\"name\":\"SCHUTZHAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00108\"},\"partNo\":\"A0008268124\"},{\"codeValidity\":\"-(832+703);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00047\"},\"partNo\":\"A2218200249\"},{\"codeValidity\":\"(803/804/805/805/806+-056)+-(832+703);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00050\"},\"partNo\":\"A1668260091\"},{\"codeValidity\":\"(806+056/807/808/809/800)+-(832+703);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00051\"},\"partNo\":\"A1668260000\"},{\"codeValidity\":\"(803/804/805/805/806+-056)+-(832+703);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00112\"},\"partNo\":\"A1668260391\"},{\"codeValidity\":\"(806+056/807/808/809/800)+-(832+703);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00113\"},\"partNo\":\"A1668260100\"},{\"codeValidity\":\"ET+(640/641/642);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00077\"},\"partNo\":\"A0008260500\"},{\"codeValidity\":\"(615/621/622)+(802/803/804/805);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00078\"},\"partNo\":\"A0008260600\"},{\"codeValidity\":\"(806/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00079\"},\"partNo\":\"A0008260600\"},{\"codeValidity\":\"(615/621/622)+(803/804/805/806/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00062\"},\"partNo\":\"A1668203689\"},{\"codeValidity\":\"(615/621/622)+(803/804/805/806/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00125\"},\"partNo\":\"A2189009203\"},{\"codeValidity\":\"(610+(615/615+(494/496/460)/621/622))+(802/803/804/805);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00064\"},\"partNo\":\"A1668203689\"},{\"codeValidity\":\"(610+(615/615+(494/496/460)/621/622))+(802/803/804/805);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00065\"},\"partNo\":\"A2189009203\"},{\"codeValidity\":\"(610+(615/615+(494/496/460)/621/622))+(806/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00128\"},\"partNo\":\"A2189009203\"},{\"codeValidity\":\"(615+(494/496/460))+(802/803/804/805);\",\"name\":\"STEUERGERAET\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00068\"},\"partNo\":\"A2189000002\"},{\"codeValidity\":\"(615+(494/496/460/737L)/622+(494/496/460/737L)+(Z04/Z05))+(802/803/804/805);\",\"name\":\"STEUERGERAET\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00069\"},\"partNo\":\"A2189009103\"},{\"codeValidity\":\"(615+(494/496/460/737L)/622+(494/496/460/737L)+(Z04/Z05))+(806/807/808/809/800);\",\"name\":\"STEUERGERAET\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00132\"},\"partNo\":\"A2189009103\"},{\"codeValidity\":\"(640/641/642)+(806+056/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00061\"},\"partNo\":\"A2228700789\"},{\"codeValidity\":\"(615/621/622)+(802/803/804/805);\",\"name\":\"FLACHKOPFSCHR.\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00071\"},\"partNo\":\"N000000002856\"},{\"codeValidity\":\"(806/807/808/809/800);\",\"name\":\"FLACHKOPFSCHR.\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00072\"},\"partNo\":\"N000000002856\"},{\"codeValidity\":\"(615/621/622)+(806/807/808/809/800);\",\"name\":\"FLACHKOPFSCHR.\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00134\"},\"partNo\":\"N000000002856\"},{\"codeValidity\":\"615/621/622;\",\"name\":\"STEUERGERAET\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00039\"},\"partNo\":\"A1669002800\"},{\"codeValidity\":\"(640/641/642)+806+056;\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00058\"},\"partNo\":\"A2189009904\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(640/641/642)+(806+056/807);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00059\"},\"partNo\":\"A2189000406\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(640/641/642)+(806+056/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00060\"},\"partNo\":\"A2189007306\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(615/621/622)+ET;\",\"name\":\"FLACHKOPFSCHR.\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00040\"},\"partNo\":\"N000000002856\"},{\"codeValidity\":\"ET;\",\"name\":\"FLACHKOPFSCHR.\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00102\"},\"partNo\":\"N000000002856\"},{\"codeValidity\":\"ET+(640/641/642);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00076\"},\"partNo\":\"A0008260300\"},{\"codeValidity\":\"ET+(803/804/805/806+-056)+-(615/621/622);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00146\"},\"partNo\":\"A1668200314\"},{\"codeValidity\":\"(806+056/807/808/809/800)+-(615/621/622);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00149\"},\"partNo\":\"A1668200714\"},{\"codeValidity\":\"ET+(803/804/805/806+-056)+-(615/621/622);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00151\"},\"partNo\":\"A1668200414\"},{\"codeValidity\":\"(615/621/622)+(802/803/804/805);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00147\"},\"partNo\":\"A1668200514\"},{\"codeValidity\":\"(615/621/622)+(806/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00148\"},\"partNo\":\"A1668200514\"},{\"codeValidity\":\"(615/621/622)+(802/803/804/805);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00152\"},\"partNo\":\"A1668200614\"},{\"codeValidity\":\"(615/621/622)+(806/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00153\"},\"partNo\":\"A1668200614\"},{\"codeValidity\":\"(806+056/807/808/809/800)+-(615/621/622);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00154\"},\"partNo\":\"A1668200814\"},{\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00073\"},\"partNo\":\"N000000007703\"},{\"codeValidity\":\"(806+056/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00075\"},\"partNo\":\"A0009908623\"},{\"codeValidity\":\"(806+056/807/808/809/800);\",\"name\":\"SCHRAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00074\"},\"partNo\":\"A0019908000\"},{\"codeValidity\":\"(803/804/805/806+-056);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00160\"},\"partNo\":\"A2469017100\"},{\"codeValidity\":\"(803/804/805/806+-056);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00161\"},\"partNo\":\"A2469012504\"},{\"codeValidity\":\"(806+056/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00164\"},\"partNo\":\"A2469002503\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(806+056/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00165\"},\"partNo\":\"A2469002603\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(806+056/807)+1U7;\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00166\"},\"partNo\":\"A2469002603\"},{\"codeValidity\":\"804+610;\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00162\"},\"partNo\":\"A1669012600\",\"plantInformationAvailable\":true},{\"codeValidity\":\"804+(863/865)+(610/610+(476/238/513/608));\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00163\"},\"partNo\":\"A2469012504\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(642/641)+(806+056/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00142\"},\"partNo\":\"A2189065800\"},{\"codeValidity\":\"(642/641)+(806+056/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00144\"},\"partNo\":\"A2189065900\"},{\"codeValidity\":\"(640+(494/460/496))+(806+056/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00143\"},\"partNo\":\"A2189066100\"},{\"codeValidity\":\"(640+(494/460/496))+(806+056/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00145\"},\"partNo\":\"A2189066200\"},{\"codeValidity\":\"(806+056/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00150\"},\"partNo\":\"A0234207518\"},{\"codeValidity\":\"(802/803/804/805/806+-056)+-772;\",\"name\":\"TAGFAHRLEUCHTE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00006\"},\"partNo\":\"A0999065700\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(802/803/804/805/806+-056)+-772;\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00007\"},\"partNo\":\"A0999068101\"},{\"codeValidity\":\"(802/803/804/805/806+-056)+-772;\",\"name\":\"TAGFAHRLEUCHTE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00012\"},\"partNo\":\"A0999065800\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(802/803/804/805/806+-056)+-772;\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00013\"},\"partNo\":\"A0999068201\"},{\"codeValidity\":\"(802/803/804/805/806+-056)+772;\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00008\"},\"partNo\":\"A1649060351\"},{\"codeValidity\":\"772+(802/803/804/805/806+-056);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00014\"},\"partNo\":\"A1649060451\"},{\"codeValidity\":\"772+(802/803/804/805/806+-056)+-M157;\",\"name\":\"BLECHSCHRAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00009\"},\"partNo\":\"N000000000529\"},{\"codeValidity\":\"772+(802/803/804/805/806+-056)+-M157;\",\"name\":\"FEDERMUTTER\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00010\"},\"partNo\":\"A0039948645\"},{\"codeValidity\":\"772+(802/803/804/805/806+-056)+-M157;\",\"name\":\"FEDERMUTTER\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00016\"},\"partNo\":\"A0039948645\"},{\"codeValidity\":\"(U60+(803/804/805/806+-056))+-(832+703);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00001\"},\"partNo\":\"A1665408332\"},{\"codeValidity\":\"(803/804/805/806+-056)+-(U60/(832+703));\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00002\"},\"partNo\":\"A1665408032\"},{\"description\":\"E1*1, E2*1\",\"name\":\"KUPPL.GEHAEUSE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00155\"},\"partNo\":\"A0525455626\"},{\"description\":\"B38/2\",\"name\":\"KONTAKTBUCHSE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00156\"},\"partNo\":\"A0255452026\"}]}");
            }
        });

    }

    /**
     * Test auf die Ausgabe des EinPAS-Knotens mit der höchsten Versionsnummer als "latestEinPASNode" in Abhängigkeit der beiden Schalter:
     * [X] "Antwortdaten für RMI Typzulassung reduzieren"
     * [X] "Aktuellsten EinPAS-Knoten in den Antwortdaten mit ausgegeben"
     */
    public void testGetPartsForDAIMLER_15931() {
        Map<UniversalConfigOption, Boolean> configOptions = new HashMap<>();
        String requestString = "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"T\"],\"modelId\":\"C166823\",\"productId\":\"D81\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"82\"},{\"type\":\"cg_subgroup\",\"id\":\"175\"}],\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00162\"}}";

        // Grundsätzlich den Schalter setzen, um den EinPAS-Knoten mit der höchsten Versionsnummer mit auszugeben.
        configOptions.put(iPartsWebservicePlugin.CONFIG_SHOW_LATEST_EINPAS_NODE_IN_RESPONSE, true);

        // Für RMI reduziert:
        // Mit folgendem, gesetzten Schalter werden die Ergebnisdaten OHNE die EinPAS-Daten ausgegeben:
        configOptions.put(iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI, true);
        executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), configOptions, () ->
                executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI, requestString,
                                  "{\"parts\":[{\"codeValidity\":\"(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00018\"},\"partNo\":\"A1668202161\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00019\"},\"partNo\":\"A1668208459\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00080\"},\"partNo\":\"A1668202261\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00081\"},\"partNo\":\"A1668208559\",\"plantInformationAvailable\":true},{\"codeValidity\":\"613+(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00020\"},\"partNo\":\"A1668202361\",\"plantInformationAvailable\":true},{\"codeValidity\":\"613+(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00021\"},\"partNo\":\"A1668208659\",\"plantInformationAvailable\":true},{\"codeValidity\":\"613+(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00082\"},\"partNo\":\"A1668202461\",\"plantInformationAvailable\":true},{\"codeValidity\":\"613+(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00083\"},\"partNo\":\"A1668208759\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(806+056/807/808/809/800)+-(P61/494/460/496/835/640/641/642/(832+703));\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00029\"},\"partNo\":\"A1668209159\"},{\"codeValidity\":\"(806+056/807/808/809/800)+-(P61/494/460/496/835/640/641/642/(832+703));\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00091\"},\"partNo\":\"A1668209259\"},{\"codeValidity\":\"613+(806+056/807/808/809/800)+-(P61/494/460/496/835/640/641/642/(832+703));\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00030\"},\"partNo\":\"A1668209359\"},{\"codeValidity\":\"613+(806+056/807/808/809/800)+-(P61/494/460/496/835/640/641/642/(832+703));\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00092\"},\"partNo\":\"A1668209459\"},{\"codeValidity\":\"(494/460/496/835)+(806+056/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00031\"},\"partNo\":\"A1668202100\"},{\"codeValidity\":\"(494/460/496/835)+(806+056/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00093\"},\"partNo\":\"A1668202200\"},{\"codeValidity\":\"642+(806+056/807/808+-058);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00032\"},\"partNo\":\"A1668202300\"},{\"codeValidity\":\"642+(808+058/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00033\"},\"partNo\":\"A1669065703\"},{\"codeValidity\":\"642+(806+056/807/808+-058);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00094\"},\"partNo\":\"A1668202400\"},{\"codeValidity\":\"642+(808+058/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00095\"},\"partNo\":\"A1669065803\"},{\"codeValidity\":\"641+(806+056/807/808+-058);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00034\"},\"partNo\":\"A1668202500\"},{\"codeValidity\":\"641+(808+058/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00035\"},\"partNo\":\"A1669065903\"},{\"codeValidity\":\"641+(806+056/807/808+-058);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00096\"},\"partNo\":\"A1668202600\"},{\"codeValidity\":\"641+(808+058/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00097\"},\"partNo\":\"A1669066003\"},{\"codeValidity\":\"(640+(494/460/496/835))+(806+056/807/808+-058);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00036\"},\"partNo\":\"A1668202700\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(640+(494/460/496/835))+(806+056/807/808+-058);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00037\"},\"partNo\":\"A1669066903\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(640+(494/460/496/835))+(808+058/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00038\"},\"partNo\":\"A1669066503\"},{\"codeValidity\":\"(640+(494/460/496/835))+(806+056/807/808+-058);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00098\"},\"partNo\":\"A1668202800\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(640+(494/460/496/835))+(806+056/807/808+-058);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00099\"},\"partNo\":\"A1669067003\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(640+(494/460/496/835))+(808+058/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00100\"},\"partNo\":\"A1669066603\"},{\"codeValidity\":\"(494/460/496)+(803/804/805/806+-056)+-(06T/615);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00026\"},\"partNo\":\"A1668206961\"},{\"codeValidity\":\"(494/460/496)+(803/804/805/806+-056)+-(06T/615);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00088\"},\"partNo\":\"A1668207061\"},{\"codeValidity\":\"615+(494/460/496)+(803/804/805/806+-056);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00027\"},\"partNo\":\"A1668205761\"},{\"codeValidity\":\"615+(494/460/496)+(803/804/805/806+-056);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00089\"},\"partNo\":\"A1668205861\"},{\"codeValidity\":\"610+615+(494/460/496)+(803/804/805/806+-056);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00028\"},\"partNo\":\"A1668205961\"},{\"codeValidity\":\"610+615+(494/460/496)+(803/804/805/806+-056);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00090\"},\"partNo\":\"A1668206061\"},{\"codeValidity\":\"622+(803/804/805/806+-056)+-(460/494/496);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00022\"},\"partNo\":\"A1668207361\"},{\"codeValidity\":\"622+(803/804/805/806+-056)+-(460/494/496);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00084\"},\"partNo\":\"A1668207461\"},{\"codeValidity\":\"621+(803/804/805/806+-056)+-(460/494/496);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00023\"},\"partNo\":\"A1668207561\"},{\"codeValidity\":\"621+(803/804/805/806+-056)+-(460/494/496);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00085\"},\"partNo\":\"A1668207661\"},{\"codeValidity\":\"610+622+(803/804/805/806+-056)+-(460/494/496);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00024\"},\"partNo\":\"A1668207961\"},{\"codeValidity\":\"610+622+(803/804/805/806+-056)+-(460/494/496);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00086\"},\"partNo\":\"A1668208061\"},{\"codeValidity\":\"610+621+(803/804/805/806+-056)+-(460/494/496);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00025\"},\"partNo\":\"A1668208161\"},{\"codeValidity\":\"610+621+(803/804/805/806+-056)+-(460/494/496);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00087\"},\"partNo\":\"A1668208261\"},{\"codeValidity\":\"P61+642+(808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00041\"},\"partNo\":\"A1669065703\"},{\"codeValidity\":\"P61+641+(808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00042\"},\"partNo\":\"A1669065903\"},{\"codeValidity\":\"P61+640+(494/460/496/835)+(808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00043\"},\"partNo\":\"A1669066103\",\"plantInformationAvailable\":true},{\"codeValidity\":\"P61+640+(494/460/496/835)+(808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00044\"},\"partNo\":\"A1669066503\",\"plantInformationAvailable\":true},{\"codeValidity\":\"P61+642+(808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00103\"},\"partNo\":\"A1669065803\"},{\"codeValidity\":\"P61+641+(808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00104\"},\"partNo\":\"A1669066003\"},{\"codeValidity\":\"P61+640+(494/460/496/835)+(808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00105\"},\"partNo\":\"A1669066203\",\"plantInformationAvailable\":true},{\"codeValidity\":\"P61+640+(494/460/496/835)+(808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00106\"},\"partNo\":\"A1669066603\",\"plantInformationAvailable\":true},{\"codeValidity\":\"-(615/621/622/640/641/642);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00052\"},\"partNo\":\"N000000008150\"},{\"codeValidity\":\"(615/621/622)+(803/804/805);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00055\"},\"partNo\":\"N000000007622\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(615/621/622)+(803/804/805/806+-056);\",\"name\":\"GASENTLAD.LAMPE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00056\"},\"partNo\":\"N000000004248\",\"plantInformationAvailable\":true},{\"codeValidity\":\"-(494/460/496/615/621/622);\",\"name\":\"GLUEHLAMPE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00057\"},\"partNo\":\"N000000006370\"},{\"codeValidity\":\"-(615/621/622/640/641/642);\",\"name\":\"GLUEHLAMPE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00053\"},\"partNo\":\"N400809000007\"},{\"codeValidity\":\"(615/621/622)+(803/805/805/806/807/808/809/800);\",\"name\":\"SCHUTZHAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00045\"},\"partNo\":\"A0008268124\"},{\"codeValidity\":\"(803/804/805/805/806+-056)+-(615/621/622);\",\"name\":\"SCHUTZHAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00046\"},\"partNo\":\"A0008268124\"},{\"codeValidity\":\"(806+056/807/808/809/800)+-(832+703);\",\"name\":\"SCHUTZHAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00048\"},\"partNo\":\"A0008264324\"},{\"codeValidity\":\"(806+056/807/808/809/800)+-(832+703);\",\"name\":\"SCHUTZHAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00049\"},\"partNo\":\"A0008268124\"},{\"codeValidity\":\"(615/621/622)+(803/805/805/806/807/808/809/800)+-(832+703);\",\"name\":\"SCHUTZHAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00107\"},\"partNo\":\"A0008268124\"},{\"codeValidity\":\"(803/804/805/805/806+-056)+-(615/621/622/832+703);\",\"name\":\"SCHUTZHAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00108\"},\"partNo\":\"A0008268124\"},{\"codeValidity\":\"-(832+703);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00047\"},\"partNo\":\"A2218200249\"},{\"codeValidity\":\"(803/804/805/805/806+-056)+-(832+703);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00050\"},\"partNo\":\"A1668260091\"},{\"codeValidity\":\"(806+056/807/808/809/800)+-(832+703);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00051\"},\"partNo\":\"A1668260000\"},{\"codeValidity\":\"(803/804/805/805/806+-056)+-(832+703);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00112\"},\"partNo\":\"A1668260391\"},{\"codeValidity\":\"(806+056/807/808/809/800)+-(832+703);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00113\"},\"partNo\":\"A1668260100\"},{\"codeValidity\":\"ET+(640/641/642);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00077\"},\"partNo\":\"A0008260500\"},{\"codeValidity\":\"(615/621/622)+(802/803/804/805);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00078\"},\"partNo\":\"A0008260600\"},{\"codeValidity\":\"(806/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00079\"},\"partNo\":\"A0008260600\"},{\"codeValidity\":\"(615/621/622)+(803/804/805/806/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00062\"},\"partNo\":\"A1668203689\"},{\"codeValidity\":\"(615/621/622)+(803/804/805/806/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00125\"},\"partNo\":\"A2189009203\"},{\"codeValidity\":\"(610+(615/615+(494/496/460)/621/622))+(802/803/804/805);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00064\"},\"partNo\":\"A1668203689\"},{\"codeValidity\":\"(610+(615/615+(494/496/460)/621/622))+(802/803/804/805);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00065\"},\"partNo\":\"A2189009203\"},{\"codeValidity\":\"(610+(615/615+(494/496/460)/621/622))+(806/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00128\"},\"partNo\":\"A2189009203\"},{\"codeValidity\":\"(615+(494/496/460))+(802/803/804/805);\",\"name\":\"STEUERGERAET\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00068\"},\"partNo\":\"A2189000002\"},{\"codeValidity\":\"(615+(494/496/460/737L)/622+(494/496/460/737L)+(Z04/Z05))+(802/803/804/805);\",\"name\":\"STEUERGERAET\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00069\"},\"partNo\":\"A2189009103\"},{\"codeValidity\":\"(615+(494/496/460/737L)/622+(494/496/460/737L)+(Z04/Z05))+(806/807/808/809/800);\",\"name\":\"STEUERGERAET\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00132\"},\"partNo\":\"A2189009103\"},{\"codeValidity\":\"(640/641/642)+(806+056/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00061\"},\"partNo\":\"A2228700789\"},{\"codeValidity\":\"(615/621/622)+(802/803/804/805);\",\"name\":\"FLACHKOPFSCHR.\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00071\"},\"partNo\":\"N000000002856\"},{\"codeValidity\":\"(806/807/808/809/800);\",\"name\":\"FLACHKOPFSCHR.\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00072\"},\"partNo\":\"N000000002856\"},{\"codeValidity\":\"(615/621/622)+(806/807/808/809/800);\",\"name\":\"FLACHKOPFSCHR.\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00134\"},\"partNo\":\"N000000002856\"},{\"codeValidity\":\"615/621/622;\",\"name\":\"STEUERGERAET\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00039\"},\"partNo\":\"A1669002800\"},{\"codeValidity\":\"(640/641/642)+806+056;\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00058\"},\"partNo\":\"A2189009904\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(640/641/642)+(806+056/807);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00059\"},\"partNo\":\"A2189000406\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(640/641/642)+(806+056/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00060\"},\"partNo\":\"A2189007306\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(615/621/622)+ET;\",\"name\":\"FLACHKOPFSCHR.\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00040\"},\"partNo\":\"N000000002856\"},{\"codeValidity\":\"ET;\",\"name\":\"FLACHKOPFSCHR.\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00102\"},\"partNo\":\"N000000002856\"},{\"codeValidity\":\"ET+(640/641/642);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00076\"},\"partNo\":\"A0008260300\"},{\"codeValidity\":\"ET+(803/804/805/806+-056)+-(615/621/622);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00146\"},\"partNo\":\"A1668200314\"},{\"codeValidity\":\"(806+056/807/808/809/800)+-(615/621/622);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00149\"},\"partNo\":\"A1668200714\"},{\"codeValidity\":\"ET+(803/804/805/806+-056)+-(615/621/622);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00151\"},\"partNo\":\"A1668200414\"},{\"codeValidity\":\"(615/621/622)+(802/803/804/805);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00147\"},\"partNo\":\"A1668200514\"},{\"codeValidity\":\"(615/621/622)+(806/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00148\"},\"partNo\":\"A1668200514\"},{\"codeValidity\":\"(615/621/622)+(802/803/804/805);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00152\"},\"partNo\":\"A1668200614\"},{\"codeValidity\":\"(615/621/622)+(806/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00153\"},\"partNo\":\"A1668200614\"},{\"codeValidity\":\"(806+056/807/808/809/800)+-(615/621/622);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00154\"},\"partNo\":\"A1668200814\"},{\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00073\"},\"partNo\":\"N000000007703\"},{\"codeValidity\":\"(806+056/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00075\"},\"partNo\":\"A0009908623\"},{\"codeValidity\":\"(806+056/807/808/809/800);\",\"name\":\"SCHRAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00074\"},\"partNo\":\"A0019908000\"},{\"codeValidity\":\"(803/804/805/806+-056);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00160\"},\"partNo\":\"A2469017100\"},{\"codeValidity\":\"(803/804/805/806+-056);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00161\"},\"partNo\":\"A2469012504\"},{\"codeValidity\":\"(806+056/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00164\"},\"partNo\":\"A2469002503\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(806+056/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00165\"},\"partNo\":\"A2469002603\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(806+056/807)+1U7;\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00166\"},\"partNo\":\"A2469002603\"},{\"codeValidity\":\"804+610;\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00162\"},\"partNo\":\"A1669012600\",\"plantInformationAvailable\":true},{\"codeValidity\":\"804+(863/865)+(610/610+(476/238/513/608));\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00163\"},\"partNo\":\"A2469012504\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(642/641)+(806+056/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00142\"},\"partNo\":\"A2189065800\"},{\"codeValidity\":\"(642/641)+(806+056/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00144\"},\"partNo\":\"A2189065900\"},{\"codeValidity\":\"(640+(494/460/496))+(806+056/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00143\"},\"partNo\":\"A2189066100\"},{\"codeValidity\":\"(640+(494/460/496))+(806+056/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00145\"},\"partNo\":\"A2189066200\"},{\"codeValidity\":\"(806+056/807/808/809/800);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00150\"},\"partNo\":\"A0234207518\"},{\"codeValidity\":\"(802/803/804/805/806+-056)+-772;\",\"name\":\"TAGFAHRLEUCHTE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00006\"},\"partNo\":\"A0999065700\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(802/803/804/805/806+-056)+-772;\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00007\"},\"partNo\":\"A0999068101\"},{\"codeValidity\":\"(802/803/804/805/806+-056)+-772;\",\"name\":\"TAGFAHRLEUCHTE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00012\"},\"partNo\":\"A0999065800\",\"plantInformationAvailable\":true},{\"codeValidity\":\"(802/803/804/805/806+-056)+-772;\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00013\"},\"partNo\":\"A0999068201\"},{\"codeValidity\":\"(802/803/804/805/806+-056)+772;\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00008\"},\"partNo\":\"A1649060351\"},{\"codeValidity\":\"772+(802/803/804/805/806+-056);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00014\"},\"partNo\":\"A1649060451\"},{\"codeValidity\":\"772+(802/803/804/805/806+-056)+-M157;\",\"name\":\"BLECHSCHRAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00009\"},\"partNo\":\"N000000000529\"},{\"codeValidity\":\"772+(802/803/804/805/806+-056)+-M157;\",\"name\":\"FEDERMUTTER\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00010\"},\"partNo\":\"A0039948645\"},{\"codeValidity\":\"772+(802/803/804/805/806+-056)+-M157;\",\"name\":\"FEDERMUTTER\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00016\"},\"partNo\":\"A0039948645\"},{\"codeValidity\":\"(U60+(803/804/805/806+-056))+-(832+703);\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00001\"},\"partNo\":\"A1665408332\"},{\"codeValidity\":\"(803/804/805/806+-056)+-(U60/(832+703));\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00002\"},\"partNo\":\"A1665408032\"},{\"description\":\"E1*1, E2*1\",\"name\":\"KUPPL.GEHAEUSE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00155\"},\"partNo\":\"A0525455626\"},{\"description\":\"B38/2\",\"name\":\"KONTAKTBUCHSE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00156\"},\"partNo\":\"A0255452026\"}]}"));

        // Ohne gesetzten Schalter werden die Ergebnisdaten MIT den EinPAS-Daten angereichert ausgegeben:
        configOptions.put(iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI, false);
        executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), configOptions, () ->
                executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI, requestString,
                                  "{\"images\":[],\"parts\":[{\"calloutId\":\"10\",\"codeValidity\":\"(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00018\"},\"partNo\":\"A1668202161\",\"partNoFormatted\":\"A 166 820 21 61\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00019\"},\"partNo\":\"A1668208459\",\"partNoFormatted\":\"A 166 820 84 59\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00080\"},\"partNo\":\"A1668202261\",\"partNoFormatted\":\"A 166 820 22 61\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00081\"},\"partNo\":\"A1668208559\",\"partNoFormatted\":\"A 166 820 85 59\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"613+(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00020\"},\"partNo\":\"A1668202361\",\"partNoFormatted\":\"A 166 820 23 61\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"613+(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00021\"},\"partNo\":\"A1668208659\",\"partNoFormatted\":\"A 166 820 86 59\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"613+(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00082\"},\"partNo\":\"A1668202461\",\"partNoFormatted\":\"A 166 820 24 61\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"613+(803/804/805/806+-056)+-(494/460/496/615/621/622);\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00083\"},\"partNo\":\"A1668208759\",\"partNoFormatted\":\"A 166 820 87 59\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"(806+056/807/808/809/800)+-(P61/494/460/496/835/640/641/642/(832+703));\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00029\"},\"partNo\":\"A1668209159\",\"partNoFormatted\":\"A 166 820 91 59\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"(806+056/807/808/809/800)+-(P61/494/460/496/835/640/641/642/(832+703));\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00091\"},\"partNo\":\"A1668209259\",\"partNoFormatted\":\"A 166 820 92 59\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"613+(806+056/807/808/809/800)+-(P61/494/460/496/835/640/641/642/(832+703));\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00030\"},\"partNo\":\"A1668209359\",\"partNoFormatted\":\"A 166 820 93 59\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"613+(806+056/807/808/809/800)+-(P61/494/460/496/835/640/641/642/(832+703));\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00092\"},\"partNo\":\"A1668209459\",\"partNoFormatted\":\"A 166 820 94 59\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"(494/460/496/835)+(806+056/807/808/809/800);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00031\"},\"partNo\":\"A1668202100\",\"partNoFormatted\":\"A 166 820 21 00\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"(494/460/496/835)+(806+056/807/808/809/800);\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00093\"},\"partNo\":\"A1668202200\",\"partNoFormatted\":\"A 166 820 22 00\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"642+(806+056/807/808+-058);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00032\"},\"partNo\":\"A1668202300\",\"partNoFormatted\":\"A 166 820 23 00\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"642+(808+058/809/800);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00033\"},\"partNo\":\"A1669065703\",\"partNoFormatted\":\"A 166 906 57 03\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"642+(806+056/807/808+-058);\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00094\"},\"partNo\":\"A1668202400\",\"partNoFormatted\":\"A 166 820 24 00\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"642+(808+058/809/800);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00095\"},\"partNo\":\"A1669065803\",\"partNoFormatted\":\"A 166 906 58 03\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"641+(806+056/807/808+-058);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00034\"},\"partNo\":\"A1668202500\",\"partNoFormatted\":\"A 166 820 25 00\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"641+(808+058/809/800);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00035\"},\"partNo\":\"A1669065903\",\"partNoFormatted\":\"A 166 906 59 03\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"641+(806+056/807/808+-058);\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00096\"},\"partNo\":\"A1668202600\",\"partNoFormatted\":\"A 166 820 26 00\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"641+(808+058/809/800);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00097\"},\"partNo\":\"A1669066003\",\"partNoFormatted\":\"A 166 906 60 03\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"(640+(494/460/496/835))+(806+056/807/808+-058);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00036\"},\"partNo\":\"A1668202700\",\"partNoFormatted\":\"A 166 820 27 00\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"(640+(494/460/496/835))+(806+056/807/808+-058);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00037\"},\"partNo\":\"A1669066903\",\"partNoFormatted\":\"A 166 906 69 03\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"(640+(494/460/496/835))+(808+058/809/800);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00038\"},\"partNo\":\"A1669066503\",\"partNoFormatted\":\"A 166 906 65 03\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"(640+(494/460/496/835))+(806+056/807/808+-058);\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00098\"},\"partNo\":\"A1668202800\",\"partNoFormatted\":\"A 166 820 28 00\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"(640+(494/460/496/835))+(806+056/807/808+-058);\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00099\"},\"partNo\":\"A1669067003\",\"partNoFormatted\":\"A 166 906 70 03\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"(640+(494/460/496/835))+(808+058/809/800);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00100\"},\"partNo\":\"A1669066603\",\"partNoFormatted\":\"A 166 906 66 03\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"(494/460/496)+(803/804/805/806+-056)+-(06T/615);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00026\"},\"partNo\":\"A1668206961\",\"partNoFormatted\":\"A 166 820 69 61\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"(494/460/496)+(803/804/805/806+-056)+-(06T/615);\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00088\"},\"partNo\":\"A1668207061\",\"partNoFormatted\":\"A 166 820 70 61\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"615+(494/460/496)+(803/804/805/806+-056);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00027\"},\"partNo\":\"A1668205761\",\"partNoFormatted\":\"A 166 820 57 61\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"615+(494/460/496)+(803/804/805/806+-056);\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00089\"},\"partNo\":\"A1668205861\",\"partNoFormatted\":\"A 166 820 58 61\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"610+615+(494/460/496)+(803/804/805/806+-056);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00028\"},\"partNo\":\"A1668205961\",\"partNoFormatted\":\"A 166 820 59 61\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"610+615+(494/460/496)+(803/804/805/806+-056);\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00090\"},\"partNo\":\"A1668206061\",\"partNoFormatted\":\"A 166 820 60 61\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"622+(803/804/805/806+-056)+-(460/494/496);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00022\"},\"partNo\":\"A1668207361\",\"partNoFormatted\":\"A 166 820 73 61\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"622+(803/804/805/806+-056)+-(460/494/496);\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00084\"},\"partNo\":\"A1668207461\",\"partNoFormatted\":\"A 166 820 74 61\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"621+(803/804/805/806+-056)+-(460/494/496);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00023\"},\"partNo\":\"A1668207561\",\"partNoFormatted\":\"A 166 820 75 61\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"621+(803/804/805/806+-056)+-(460/494/496);\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00085\"},\"partNo\":\"A1668207661\",\"partNoFormatted\":\"A 166 820 76 61\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"610+622+(803/804/805/806+-056)+-(460/494/496);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00024\"},\"partNo\":\"A1668207961\",\"partNoFormatted\":\"A 166 820 79 61\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"610+622+(803/804/805/806+-056)+-(460/494/496);\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00086\"},\"partNo\":\"A1668208061\",\"partNoFormatted\":\"A 166 820 80 61\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"610+621+(803/804/805/806+-056)+-(460/494/496);\",\"damageCodes\":[\"82B01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00025\"},\"partNo\":\"A1668208161\",\"partNoFormatted\":\"A 166 820 81 61\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"610+621+(803/804/805/806+-056)+-(460/494/496);\",\"damageCodes\":[\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00087\"},\"partNo\":\"A1668208261\",\"partNoFormatted\":\"A 166 820 82 61\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"P61+642+(808/809/800);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00041\"},\"partNo\":\"A1669065703\",\"partNoFormatted\":\"A 166 906 57 03\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"P61+641+(808/809/800);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00042\"},\"partNo\":\"A1669065903\",\"partNoFormatted\":\"A 166 906 59 03\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"P61+640+(494/460/496/835)+(808/809/800);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00043\"},\"partNo\":\"A1669066103\",\"partNoFormatted\":\"A 166 906 61 03\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"P61+640+(494/460/496/835)+(808/809/800);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00044\"},\"partNo\":\"A1669066503\",\"partNoFormatted\":\"A 166 906 65 03\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"P61+642+(808/809/800);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00103\"},\"partNo\":\"A1669065803\",\"partNoFormatted\":\"A 166 906 58 03\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"P61+641+(808/809/800);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00104\"},\"partNo\":\"A1669066003\",\"partNoFormatted\":\"A 166 906 60 03\",\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"P61+640+(494/460/496/835)+(808/809/800);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00105\"},\"partNo\":\"A1669066203\",\"partNoFormatted\":\"A 166 906 62 03\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"10\",\"codeValidity\":\"P61+640+(494/460/496/835)+(808/809/800);\",\"damageCodes\":[\"82B01\",\"82P01\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00106\"},\"partNo\":\"A1669066603\",\"partNoFormatted\":\"A 166 906 66 03\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"20\",\"codeValidity\":\"-(615/621/622/640/641/642);\",\"damageCodes\":[\"82268\"],\"level\":\"02\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00052\"},\"partNo\":\"N000000008150\",\"partNoFormatted\":\"N 000000 008150\",\"quantity\":\"NB\"},{\"calloutId\":\"40\",\"codeValidity\":\"(615/621/622)+(803/804/805);\",\"damageCodes\":[\"82126\"],\"level\":\"02\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00055\"},\"partNo\":\"N000000007622\",\"partNoFormatted\":\"N 000000 007622\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"40\",\"codeValidity\":\"(615/621/622)+(803/804/805/806+-056);\",\"damageCodes\":[\"82126\"],\"level\":\"02\",\"name\":\"GASENTLAD.LAMPE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00056\"},\"partNo\":\"N000000004248\",\"partNoFormatted\":\"N 000000 004248\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"50\",\"codeValidity\":\"-(494/460/496/615/621/622);\",\"damageCodes\":[\"82141\"],\"level\":\"02\",\"name\":\"GLUEHLAMPE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00057\"},\"partNo\":\"N000000006370\",\"partNoFormatted\":\"N 000000 006370\",\"quantity\":\"NB\"},{\"calloutId\":\"70\",\"codeValidity\":\"-(615/621/622/640/641/642);\",\"damageCodes\":[\"82123\"],\"level\":\"02\",\"name\":\"GLUEHLAMPE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00053\"},\"partNo\":\"N400809000007\",\"partNoFormatted\":\"N 400809 000007\",\"quantity\":\"NB\"},{\"calloutId\":\"110\",\"codeValidity\":\"(615/621/622)+(803/805/805/806/807/808/809/800);\",\"damageCodes\":[\"82155\"],\"level\":\"02\",\"name\":\"SCHUTZHAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00045\"},\"partNo\":\"A0008268124\",\"partNoFormatted\":\"A 000 826 81 24\",\"quantity\":\"NB\"},{\"calloutId\":\"110\",\"codeValidity\":\"(803/804/805/805/806+-056)+-(615/621/622);\",\"damageCodes\":[\"82155\"],\"level\":\"02\",\"name\":\"SCHUTZHAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00046\"},\"partNo\":\"A0008268124\",\"partNoFormatted\":\"A 000 826 81 24\",\"quantity\":\"NB\"},{\"calloutId\":\"110\",\"codeValidity\":\"(806+056/807/808/809/800)+-(832+703);\",\"damageCodes\":[\"82155\"],\"level\":\"02\",\"name\":\"SCHUTZHAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00048\"},\"partNo\":\"A0008264324\",\"partNoFormatted\":\"A 000 826 43 24\",\"quantity\":\"NB\",\"replacedBy\":[{\"name\":\"SCHUTZHAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00049\"},\"partNo\":\"A0008268124\",\"partNoFormatted\":\"A 000 826 81 24\"}]},{\"calloutId\":\"110\",\"codeValidity\":\"(806+056/807/808/809/800)+-(832+703);\",\"damageCodes\":[\"82155\"],\"level\":\"02\",\"name\":\"SCHUTZHAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00049\"},\"partNo\":\"A0008268124\",\"partNoFormatted\":\"A 000 826 81 24\",\"quantity\":\"NB\"},{\"calloutId\":\"110\",\"codeValidity\":\"(615/621/622)+(803/805/805/806/807/808/809/800)+-(832+703);\",\"damageCodes\":[\"82155\"],\"level\":\"02\",\"name\":\"SCHUTZHAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00107\"},\"partNo\":\"A0008268124\",\"partNoFormatted\":\"A 000 826 81 24\",\"quantity\":\"NB\"},{\"calloutId\":\"110\",\"codeValidity\":\"(803/804/805/805/806+-056)+-(615/621/622/832+703);\",\"damageCodes\":[\"82155\"],\"level\":\"02\",\"name\":\"SCHUTZHAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00108\"},\"partNo\":\"A0008268124\",\"partNoFormatted\":\"A 000 826 81 24\",\"quantity\":\"NB\"},{\"calloutId\":\"120\",\"codeValidity\":\"-(832+703);\",\"damageCodes\":[\"82155\"],\"level\":\"02\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00047\"},\"partNo\":\"A2218200249\",\"partNoFormatted\":\"A 221 820 02 49\",\"quantity\":\"NB\"},{\"calloutId\":\"130\",\"codeValidity\":\"(803/804/805/805/806+-056)+-(832+703);\",\"damageCodes\":[\"82B00\"],\"level\":\"02\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00050\"},\"partNo\":\"A1668260091\",\"partNoFormatted\":\"A 166 826 00 91\",\"quantity\":\"NB\"},{\"calloutId\":\"130\",\"codeValidity\":\"(806+056/807/808/809/800)+-(832+703);\",\"damageCodes\":[\"82B00\"],\"level\":\"02\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00051\"},\"partNo\":\"A1668260000\",\"partNoFormatted\":\"A 166 826 00 00\",\"quantity\":\"NB\"},{\"calloutId\":\"130\",\"codeValidity\":\"(803/804/805/805/806+-056)+-(832+703);\",\"damageCodes\":[\"82P00\"],\"level\":\"02\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00112\"},\"partNo\":\"A1668260391\",\"partNoFormatted\":\"A 166 826 03 91\",\"quantity\":\"NB\"},{\"calloutId\":\"130\",\"codeValidity\":\"(806+056/807/808/809/800)+-(832+703);\",\"damageCodes\":[\"82P00\"],\"level\":\"02\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00113\"},\"partNo\":\"A1668260100\",\"partNoFormatted\":\"A 166 826 01 00\",\"quantity\":\"NB\"},{\"calloutId\":\"140\",\"codeValidity\":\"ET+(640/641/642);\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00077\"},\"partNo\":\"A0008260500\",\"partNoFormatted\":\"A 000 826 05 00\",\"quantity\":\"NB\"},{\"calloutId\":\"140\",\"codeValidity\":\"(615/621/622)+(802/803/804/805);\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00078\"},\"partNo\":\"A0008260600\",\"partNoFormatted\":\"A 000 826 06 00\",\"quantity\":\"NB\"},{\"calloutId\":\"140\",\"codeValidity\":\"(806/807/808/809/800);\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00079\"},\"partNo\":\"A0008260600\",\"partNoFormatted\":\"A 000 826 06 00\",\"quantity\":\"NB\"},{\"calloutId\":\"150\",\"codeValidity\":\"(615/621/622)+(803/804/805/806/807/808/809/800);\",\"damageCodes\":[\"87A0X\",\"87A0Y\",\"87N0X\",\"87N0Y\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00062\"},\"partNo\":\"A1668203689\",\"partNoFormatted\":\"A 166 820 36 89\",\"quantity\":\"NB\",\"replacedBy\":[{\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00063\"},\"partNo\":\"A2189009203\",\"partNoFormatted\":\"A 218 900 92 03\"}]},{\"calloutId\":\"150\",\"codeValidity\":\"(615/621/622)+(803/804/805/806/807/808/809/800);\",\"damageCodes\":[\"87N0X\",\"87A0X\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00125\"},\"partNo\":\"A2189009203\",\"partNoFormatted\":\"A 218 900 92 03\",\"quantity\":\"NB\"},{\"calloutId\":\"150\",\"codeValidity\":\"(610+(615/615+(494/496/460)/621/622))+(802/803/804/805);\",\"damageCodes\":[\"87A0X\",\"87A0Y\",\"87N0X\",\"87N0Y\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00064\"},\"partNo\":\"A1668203689\",\"partNoFormatted\":\"A 166 820 36 89\",\"quantity\":\"NB\",\"replacedBy\":[{\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00065\"},\"partNo\":\"A2189009203\",\"partNoFormatted\":\"A 218 900 92 03\"}]},{\"calloutId\":\"150\",\"codeValidity\":\"(610+(615/615+(494/496/460)/621/622))+(802/803/804/805);\",\"damageCodes\":[\"87A0X\",\"87N0X\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00065\"},\"partNo\":\"A2189009203\",\"partNoFormatted\":\"A 218 900 92 03\",\"quantity\":\"NB\"},{\"calloutId\":\"150\",\"codeValidity\":\"(610+(615/615+(494/496/460)/621/622))+(806/807/808/809/800);\",\"damageCodes\":[\"87N0X\",\"87A0X\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00128\"},\"partNo\":\"A2189009203\",\"partNoFormatted\":\"A 218 900 92 03\",\"quantity\":\"NB\"},{\"calloutId\":\"150\",\"codeValidity\":\"(615+(494/496/460))+(802/803/804/805);\",\"damageCodes\":[\"87A0X\",\"87A0Y\",\"87N0X\",\"87N0Y\"],\"level\":\"01\",\"name\":\"STEUERGERAET\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00068\"},\"partNo\":\"A2189000002\",\"partNoFormatted\":\"A 218 900 00 02\",\"quantity\":\"NB\",\"replacedBy\":[{\"name\":\"STEUERGERAET\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00069\"},\"partNo\":\"A2189009103\",\"partNoFormatted\":\"A 218 900 91 03\"}]},{\"calloutId\":\"150\",\"codeValidity\":\"(615+(494/496/460/737L)/622+(494/496/460/737L)+(Z04/Z05))+(802/803/804/805);\",\"damageCodes\":[\"87A0X\",\"87N0X\"],\"level\":\"01\",\"name\":\"STEUERGERAET\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00069\"},\"partNo\":\"A2189009103\",\"partNoFormatted\":\"A 218 900 91 03\",\"quantity\":\"NB\"},{\"calloutId\":\"150\",\"codeValidity\":\"(615+(494/496/460/737L)/622+(494/496/460/737L)+(Z04/Z05))+(806/807/808/809/800);\",\"damageCodes\":[\"87N0X\",\"87A0X\"],\"level\":\"01\",\"name\":\"STEUERGERAET\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00132\"},\"partNo\":\"A2189009103\",\"partNoFormatted\":\"A 218 900 91 03\",\"quantity\":\"NB\"},{\"calloutId\":\"150\",\"codeValidity\":\"(640/641/642)+(806+056/807/808/809/800);\",\"damageCodes\":[\"87A0Y\",\"87N0Y\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00061\"},\"partNo\":\"A2228700789\",\"partNoFormatted\":\"A 222 870 07 89\",\"quantity\":\"NB\"},{\"calloutId\":\"155\",\"codeValidity\":\"(615/621/622)+(802/803/804/805);\",\"level\":\"01\",\"name\":\"FLACHKOPFSCHR.\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00071\"},\"partNo\":\"N000000002856\",\"partNoFormatted\":\"N 000000 002856\",\"quantity\":\"NB\"},{\"calloutId\":\"155\",\"codeValidity\":\"(806/807/808/809/800);\",\"level\":\"01\",\"name\":\"FLACHKOPFSCHR.\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00072\"},\"partNo\":\"N000000002856\",\"partNoFormatted\":\"N 000000 002856\",\"quantity\":\"NB\"},{\"calloutId\":\"155\",\"codeValidity\":\"(615/621/622)+(806/807/808/809/800);\",\"level\":\"01\",\"name\":\"FLACHKOPFSCHR.\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00134\"},\"partNo\":\"N000000002856\",\"partNoFormatted\":\"N 000000 002856\",\"quantity\":\"NB\"},{\"calloutId\":\"160\",\"codeValidity\":\"615/621/622;\",\"damageCodes\":[\"82B03\",\"82P03\"],\"level\":\"01\",\"name\":\"STEUERGERAET\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00039\"},\"partNo\":\"A1669002800\",\"partNoFormatted\":\"A 166 900 28 00\",\"quantity\":\"NB\"},{\"calloutId\":\"160\",\"codeValidity\":\"(640/641/642)+806+056;\",\"damageCodes\":[\"87A0X\",\"87N0X\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00058\"},\"partNo\":\"A2189009904\",\"partNoFormatted\":\"A 218 900 99 04\",\"plantInformationAvailable\":true,\"quantity\":\"NB\",\"replacedBy\":[{\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00059\"},\"partNo\":\"A2189000406\",\"partNoFormatted\":\"A 218 900 04 06\"}]},{\"calloutId\":\"160\",\"codeValidity\":\"(640/641/642)+(806+056/807);\",\"damageCodes\":[\"87A0X\",\"87N0X\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00059\"},\"partNo\":\"A2189000406\",\"partNoFormatted\":\"A 218 900 04 06\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"160\",\"codeValidity\":\"(640/641/642)+(806+056/807/808/809/800);\",\"damageCodes\":[\"87A0X\",\"87N0X\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00060\"},\"partNo\":\"A2189007306\",\"partNoFormatted\":\"A 218 900 73 06\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"165\",\"codeValidity\":\"(615/621/622)+ET;\",\"level\":\"01\",\"name\":\"FLACHKOPFSCHR.\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00040\"},\"partNo\":\"N000000002856\",\"partNoFormatted\":\"N 000000 002856\",\"quantity\":\"NB\"},{\"calloutId\":\"165\",\"codeValidity\":\"ET;\",\"level\":\"01\",\"name\":\"FLACHKOPFSCHR.\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00102\"},\"partNo\":\"N000000002856\",\"partNoFormatted\":\"N 000000 002856\",\"quantity\":\"NB\"},{\"calloutId\":\"170\",\"codeValidity\":\"ET+(640/641/642);\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00076\"},\"partNo\":\"A0008260300\",\"partNoFormatted\":\"A 000 826 03 00\",\"quantity\":\"NB\"},{\"calloutId\":\"180\",\"codeValidity\":\"ET+(803/804/805/806+-056)+-(615/621/622);\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00146\"},\"partNo\":\"A1668200314\",\"partNoFormatted\":\"A 166 820 03 14\",\"quantity\":\"NB\"},{\"calloutId\":\"180\",\"codeValidity\":\"(806+056/807/808/809/800)+-(615/621/622);\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00149\"},\"partNo\":\"A1668200714\",\"partNoFormatted\":\"A 166 820 07 14\",\"quantity\":\"NB\"},{\"calloutId\":\"180\",\"codeValidity\":\"ET+(803/804/805/806+-056)+-(615/621/622);\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00151\"},\"partNo\":\"A1668200414\",\"partNoFormatted\":\"A 166 820 04 14\",\"quantity\":\"NB\"},{\"calloutId\":\"180\",\"codeValidity\":\"(615/621/622)+(802/803/804/805);\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00147\"},\"partNo\":\"A1668200514\",\"partNoFormatted\":\"A 166 820 05 14\",\"quantity\":\"NB\"},{\"calloutId\":\"180\",\"codeValidity\":\"(615/621/622)+(806/807/808/809/800);\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00148\"},\"partNo\":\"A1668200514\",\"partNoFormatted\":\"A 166 820 05 14\",\"quantity\":\"NB\"},{\"calloutId\":\"180\",\"codeValidity\":\"(615/621/622)+(802/803/804/805);\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00152\"},\"partNo\":\"A1668200614\",\"partNoFormatted\":\"A 166 820 06 14\",\"quantity\":\"NB\"},{\"calloutId\":\"180\",\"codeValidity\":\"(615/621/622)+(806/807/808/809/800);\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00153\"},\"partNo\":\"A1668200614\",\"partNoFormatted\":\"A 166 820 06 14\",\"quantity\":\"NB\"},{\"calloutId\":\"180\",\"codeValidity\":\"(806+056/807/808/809/800)+-(615/621/622);\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00154\"},\"partNo\":\"A1668200814\",\"partNoFormatted\":\"A 166 820 08 14\",\"quantity\":\"NB\"},{\"calloutId\":\"210\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00073\"},\"partNo\":\"N000000007703\",\"partNoFormatted\":\"N 000000 007703\",\"quantity\":\"NB\"},{\"calloutId\":\"230\",\"codeValidity\":\"(806+056/807/808/809/800);\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00075\"},\"partNo\":\"A0009908623\",\"partNoFormatted\":\"A 000 990 86 23\",\"quantity\":\"NB\"},{\"calloutId\":\"240\",\"codeValidity\":\"(806+056/807/808/809/800);\",\"level\":\"01\",\"name\":\"SCHRAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00074\"},\"partNo\":\"A0019908000\",\"partNoFormatted\":\"A 001 990 80 00\",\"quantity\":\"NB\"},{\"calloutId\":\"250\",\"codeValidity\":\"(803/804/805/806+-056);\",\"damageCodes\":[\"82128\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00160\"},\"partNo\":\"A2469017100\",\"partNoFormatted\":\"A 246 901 71 00\",\"quantity\":\"NB\",\"replacedBy\":[{\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00161\"},\"partNo\":\"A2469012504\",\"partNoFormatted\":\"A 246 901 25 04\"}]},{\"calloutId\":\"250\",\"codeValidity\":\"(803/804/805/806+-056);\",\"damageCodes\":[\"82128\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00161\"},\"partNo\":\"A2469012504\",\"partNoFormatted\":\"A 246 901 25 04\",\"quantity\":\"NB\"},{\"calloutId\":\"250\",\"codeValidity\":\"(806+056/807/808/809/800);\",\"damageCodes\":[\"5472D\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00164\"},\"partNo\":\"A2469002503\",\"partNoFormatted\":\"A 246 900 25 03\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"250\",\"codeValidity\":\"(806+056/807/808/809/800);\",\"damageCodes\":[\"5472D\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00165\"},\"partNo\":\"A2469002603\",\"partNoFormatted\":\"A 246 900 26 03\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"250\",\"codeValidity\":\"(806+056/807)+1U7;\",\"damageCodes\":[\"5472D\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00166\"},\"partNo\":\"A2469002603\",\"partNoFormatted\":\"A 246 900 26 03\",\"quantity\":\"NB\"},{\"calloutId\":\"250\",\"codeValidity\":\"804+610;\",\"damageCodes\":[\"82128\"],\"einPASNodeAvailable\":true,\"latestEinPASNode\":{\"g\":\"30\",\"hg\":\"36\",\"tu\":\"12\",\"version\":\"006\"},\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00162\"},\"partNo\":\"A1669012600\",\"partNoFormatted\":\"A 166 901 26 00\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"250\",\"codeValidity\":\"804+(863/865)+(610/610+(476/238/513/608));\",\"damageCodes\":[\"82128\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00163\"},\"partNo\":\"A2469012504\",\"partNoFormatted\":\"A 246 901 25 04\",\"plantInformationAvailable\":true,\"quantity\":\"NB\"},{\"calloutId\":\"260\",\"codeValidity\":\"(642/641)+(806+056/807/808/809/800);\",\"damageCodes\":[\"82F0K\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00142\"},\"partNo\":\"A2189065800\",\"partNoFormatted\":\"A 218 906 58 00\",\"quantity\":\"NB\"},{\"calloutId\":\"260\",\"codeValidity\":\"(642/641)+(806+056/807/808/809/800);\",\"damageCodes\":[\"82U0K\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00144\"},\"partNo\":\"A2189065900\",\"partNoFormatted\":\"A 218 906 59 00\",\"quantity\":\"NB\"},{\"calloutId\":\"260\",\"codeValidity\":\"(640+(494/460/496))+(806+056/807/808/809/800);\",\"damageCodes\":[\"82F0K\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00143\"},\"partNo\":\"A2189066100\",\"partNoFormatted\":\"A 218 906 61 00\",\"quantity\":\"NB\"},{\"calloutId\":\"260\",\"codeValidity\":\"(640+(494/460/496))+(806+056/807/808/809/800);\",\"damageCodes\":[\"82U0K\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00145\"},\"partNo\":\"A2189066200\",\"partNoFormatted\":\"A 218 906 62 00\",\"quantity\":\"NB\"},{\"calloutId\":\"270\",\"codeValidity\":\"(806+056/807/808/809/800);\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00150\"},\"partNo\":\"A0234207518\",\"partNoFormatted\":\"A 023 420 75 18\",\"quantity\":\"NB\"},{\"calloutId\":\"400\",\"codeValidity\":\"(802/803/804/805/806+-056)+-772;\",\"damageCodes\":[\"87A10\"],\"level\":\"01\",\"name\":\"TAGFAHRLEUCHTE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00006\"},\"partNo\":\"A0999065700\",\"partNoFormatted\":\"A 099 906 57 00\",\"plantInformationAvailable\":true,\"quantity\":\"NB\",\"replacedBy\":[{\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00007\"},\"partNo\":\"A0999068101\",\"partNoFormatted\":\"A 099 906 81 01\"}]},{\"calloutId\":\"400\",\"codeValidity\":\"(802/803/804/805/806+-056)+-772;\",\"damageCodes\":[\"87A10\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00007\"},\"partNo\":\"A0999068101\",\"partNoFormatted\":\"A 099 906 81 01\",\"quantity\":\"NB\"},{\"calloutId\":\"400\",\"codeValidity\":\"(802/803/804/805/806+-056)+-772;\",\"damageCodes\":[\"87N10\"],\"level\":\"01\",\"name\":\"TAGFAHRLEUCHTE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00012\"},\"partNo\":\"A0999065800\",\"partNoFormatted\":\"A 099 906 58 00\",\"plantInformationAvailable\":true,\"quantity\":\"NB\",\"replacedBy\":[{\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00013\"},\"partNo\":\"A0999068201\",\"partNoFormatted\":\"A 099 906 82 01\"}]},{\"calloutId\":\"400\",\"codeValidity\":\"(802/803/804/805/806+-056)+-772;\",\"damageCodes\":[\"87N10\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00013\"},\"partNo\":\"A0999068201\",\"partNoFormatted\":\"A 099 906 82 01\",\"quantity\":\"NB\"},{\"calloutId\":\"400\",\"codeValidity\":\"(802/803/804/805/806+-056)+772;\",\"damageCodes\":[\"87A10\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00008\"},\"partNo\":\"A1649060351\",\"partNoFormatted\":\"A 164 906 03 51\",\"quantity\":\"NB\"},{\"calloutId\":\"400\",\"codeValidity\":\"772+(802/803/804/805/806+-056);\",\"damageCodes\":[\"87A10\"],\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00014\"},\"partNo\":\"A1649060451\",\"partNoFormatted\":\"A 164 906 04 51\",\"quantity\":\"NB\"},{\"calloutId\":\"470\",\"codeValidity\":\"772+(802/803/804/805/806+-056)+-M157;\",\"level\":\"01\",\"name\":\"BLECHSCHRAUBE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00009\"},\"partNo\":\"N000000000529\",\"partNoFormatted\":\"N 000000 000529\",\"quantity\":\"NB\"},{\"calloutId\":\"470\",\"codeValidity\":\"772+(802/803/804/805/806+-056)+-M157;\",\"level\":\"01\",\"name\":\"FEDERMUTTER\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00010\"},\"partNo\":\"A0039948645\",\"partNoFormatted\":\"A 003 994 86 45\",\"quantity\":\"NB\"},{\"calloutId\":\"480\",\"codeValidity\":\"772+(802/803/804/805/806+-056)+-M157;\",\"level\":\"01\",\"name\":\"FEDERMUTTER\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00016\"},\"partNo\":\"A0039948645\",\"partNoFormatted\":\"A 003 994 86 45\",\"quantity\":\"NB\"},{\"calloutId\":\"500\",\"codeValidity\":\"(U60+(803/804/805/806+-056))+-(832+703);\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00001\"},\"partNo\":\"A1665408332\",\"partNoFormatted\":\"A 166 540 83 32\",\"quantity\":\"NB\"},{\"calloutId\":\"500\",\"codeValidity\":\"(803/804/805/806+-056)+-(U60/(832+703));\",\"level\":\"01\",\"name\":\"\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00002\"},\"partNo\":\"A1665408032\",\"partNoFormatted\":\"A 166 540 80 32\",\"quantity\":\"NB\"},{\"calloutId\":\"900\",\"description\":\"E1*1, E2*1\",\"level\":\"01\",\"name\":\"KUPPL.GEHAEUSE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00155\"},\"partNo\":\"A0525455626\",\"partNoFormatted\":\"A 052 545 56 26\",\"quantity\":\"NB\"},{\"calloutId\":\"901\",\"description\":\"B38/2\",\"level\":\"01\",\"name\":\"KONTAKTBUCHSE\",\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00156\"},\"partNo\":\"A0255452026\",\"partNoFormatted\":\"A 025 545 20 26\",\"quantity\":\"NB\"}]}"));
    }

    public void testGetPartsWiringHarnessKit() {
        // Die Admin-Option "Leitungssatz-Baukasten Teilepositionen filtern" muss für diesen Testfall temporär aktiviert werden
        UniversalConfiguration pluginConfig = iPartsPlugin.getPluginConfig();
        boolean filterWireHarness = pluginConfig.getConfig().getBoolean(pluginConfig.getPath() + iPartsPlugin.CONFIG_FILTER_WIRE_HARNESS_PARTS.getKey(),
                                                                        false);
        writeBooleanConfigValues(pluginConfig, true, iPartsPlugin.CONFIG_FILTER_WIRE_HARNESS_PARTS);
        try {
            clearWebservicePluginsCaches();
            executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205002\",\"productId\":\"C01\",\"filterOptions\":{}},\"navContext\":[{\"id\":\"68\",\"type\":\"cg_group\"},{\"id\":\"193\",\"type\":\"cg_subgroup\"}]}",
                              DWFile.get(getTestWorkingDir(), "resultGetPartsWiringHarnessKit.txt"));
        } finally {
            writeBooleanConfigValues(pluginConfig, filterWireHarness, iPartsPlugin.CONFIG_FILTER_WIRE_HARNESS_PARTS);
            clearWebservicePluginsCaches();
        }
    }

    public void testGetPartsES1ES2Separation() {
        // Die Admin-Option "Leitungssatz-Baukasten Teilepositionen filtern" muss für diesen Testfall temporär aktiviert werden
        UniversalConfiguration pluginConfig = iPartsPlugin.getPluginConfig();
        boolean separateEs1Es2 = pluginConfig.getConfig().getBoolean(pluginConfig.getPath() + iPartsPlugin.CONFIG_WEBSERVICES_XMLEXPORT_SEPARATE_ES12KEYS.getKey(),
                                                                     false);
        writeBooleanConfigValues(pluginConfig, true, iPartsPlugin.CONFIG_WEBSERVICES_XMLEXPORT_SEPARATE_ES12KEYS);
        try {
            clearWebservicePluginsCaches();
            executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"user\":{\"userId\":\"user\",\"country\":\"200\",\"language\":\"en\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"T\"],\"modelId\":\"C205002\",\"productId\":\"C01\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"47\"},{\"type\":\"cg_subgroup\",\"id\":\"800\"}]}",
                              DWFile.get(getTestWorkingDir(), "resultGetPartsES1ES2Separation.txt"));
        } finally {
            writeBooleanConfigValues(pluginConfig, separateEs1Es2, iPartsPlugin.CONFIG_WEBSERVICES_XMLEXPORT_SEPARATE_ES12KEYS);
            clearWebservicePluginsCaches();
        }
    }

    public void testGetPartsCountryAndSpecification() {
        // DAIMLER-13919
        // in der Ausgabe befinden sich mehrere Ländergültigkeiten und mehrere Öl-Spezifikationen an einer Teileposition
        // Die Öl-Spezifikation wurde manuell in der DB geändert, weil es sonst nur Einzelwerte gab
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C129060\",\"productId\":\"515\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"99\"},{\"type\":\"cg_subgroup\",\"id\":\"130\"}]}",
                          createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJLUiIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.i0hXcoiTYJ0G1ViavXCEWxx0oMzvcIykd1EVaYzSJGk"),
                          DWFile.get(getTestWorkingDir(), "resultGetPartsCountryAndSpecification.txt"));
    }

    public void testGetPartInfoWiringHarnessKit() {
        // Die Admin-Option "Leitungssatz-Baukasten Teilepositionen filtern" muss für diesen Testfall temporär aktiviert werden
        UniversalConfiguration pluginConfig = iPartsPlugin.getPluginConfig();
        boolean filterWireHarness = pluginConfig.getConfig().getBoolean(pluginConfig.getPath() + iPartsPlugin.CONFIG_FILTER_WIRE_HARNESS_PARTS.getKey(),
                                                                        false);
        writeBooleanConfigValues(pluginConfig, true, iPartsPlugin.CONFIG_FILTER_WIRE_HARNESS_PARTS);
        try {
            clearWebservicePluginsCaches();
            executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205002\",\"productId\":\"C01\",\"filterOptions\":{}},\"partContext\":{\"moduleId\":\"C01_68_193_00001\",\"sequenceId\":\"00054\"}}",
                              DWFile.get(getTestWorkingDir(), "resultGetPartInfoWiringHarnessKit.txt"));
        } finally {
            writeBooleanConfigValues(pluginConfig, filterWireHarness, iPartsPlugin.CONFIG_FILTER_WIRE_HARNESS_PARTS);
            clearWebservicePluginsCaches();
        }
    }

    public void testGetPartInfoWireHarnessKitWithSPKTexts() {
        // Test für DAIMLER-14465 mit Aktivem SPK Mapping
        Map<UniversalConfigOption, Boolean> configOptions = new HashMap<>();
        configOptions.put(iPartsPlugin.CONFIG_USE_SPK_MAPPING, true);
        configOptions.put(iPartsPlugin.CONFIG_FILTER_WIRE_HARNESS_PARTS, true);
        executeTestWithBooleanConfigChanges(iPartsPlugin.getPluginConfig(), configOptions, () -> {
            Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");
            executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"identContext\": {\"aggTypeId\": \"F\", \"modelId\": \"C253303\", \"productClassIds\": [\"P\"], \"productId\": \"C253_FC\"}, \"partContext\": {\"moduleId\": \"C253_FC_54_545_00001\", \"sequenceId\": \"00160\"}}",
                              additionalRequestProperties,
                              "{\"partInfo\":{\"codeValidityDetails\":[{\"code\":\"154\",\"dateFrom\":\"1996-05-29\",\"name\":\"SCHNITTMODELL MOTOR OHNE GETRIEBE, OEL, PRUEFLAUF\"},{\"code\":\"531\",\"dateFrom\":\"2011-03-09\",\"name\":\"COMAND APS NTG5/NTG5.5\"},{\"code\":\"800\",\"dateFrom\":\"2012-07-11\",\"name\":\"AEJ 19/1\"}],\"codeValidityMatrix\":[[{\"code\":\"154\",\"negative\":true},{\"code\":\"531\",\"negative\":true},{\"code\":\"547\"},{\"code\":\"800\"}],[{\"code\":\"154\",\"negative\":true},{\"code\":\"531\",\"negative\":true},{\"code\":\"548\"},{\"code\":\"800\"}],[{\"code\":\"154\",\"negative\":true},{\"code\":\"531\",\"negative\":true},{\"code\":\"549\"},{\"code\":\"800\"}]],\"wiringHarnessKit\":[{\"connectorNumber\":\"1A\",\"contactAdditionalText\":\"HEADUNIT\",\"partNumber\":\"A0005405203\",\"partNumberFormatted\":\"A 000 540 52 03\",\"partNumberType\":\"01_PIN_HOUSING\",\"referenceNumber\":\"A26/17\"},{\"connectorNumber\":\"1A\",\"contactAdditionalText\":\"HEADUNIT\",\"name\":\"Buchse nanoMQS Sn 0.35-0.35\",\"partNumber\":\"A0049821626\",\"partNumberFormatted\":\"A 004 982 16 26\",\"partNumberType\":\"02_PIN\",\"referenceNumber\":\"A26/17\"},{\"connectorNumber\":\"1A\",\"contactAdditionalText\":\"HEADUNIT\",\"partNumber\":\"A0145451126\",\"partNumberFormatted\":\"A 014 545 11 26\",\"partNumberType\":\"02_PIN\",\"referenceNumber\":\"A26/17\"},{\"connectorNumber\":\"H21\",\"contactAdditionalText\":\"HEADUNIT\",\"name\":\"Buchsengehäuse 4pol X\",\"partNumber\":\"A2125457826\",\"partNumberFormatted\":\"A 212 545 78 26\",\"partNumberType\":\"01_PIN_HOUSING\",\"referenceNumber\":\"A26/17\"},{\"connectorNumber\":\"H21\",\"contactAdditionalText\":\"HEADUNIT\",\"name\":\"Buchse HSD Au 0.5-0.5\",\"partNumber\":\"A0079822926\",\"partNumberFormatted\":\"A 007 982 29 26\",\"partNumberType\":\"02_PIN\",\"referenceNumber\":\"A26/17\"},{\"connectorNumber\":\"MF2\",\"contactAdditionalText\":\"SICHERUNGSKASTEN A-SÄULE BEIFAHRERSEITE\",\"partNumber\":\"A0375453628\",\"partNumberFormatted\":\"A 037 545 36 28\",\"partNumberType\":\"01_PIN_HOUSING\",\"referenceNumber\":\"F1/3\"},{\"connectorNumber\":\"MF2\",\"contactAdditionalText\":\"SICHERUNGSKASTEN A-SÄULE BEIFAHRERSEITE\",\"partNumber\":\"A0145451126\",\"partNumberFormatted\":\"A 014 545 11 26\",\"partNumberType\":\"02_PIN\",\"referenceNumber\":\"F1/3\"},{\"connectorNumber\":\"1\",\"contactAdditionalText\":\"MULTIMEDIA-ANSCHLUSSEINHEIT\",\"name\":\"Buchsengehäuse 8pol\",\"partNumber\":\"A0065450340\",\"partNumberFormatted\":\"A 006 545 03 40\",\"partNumberType\":\"01_PIN_HOUSING\",\"referenceNumber\":\"A90/5\"},{\"connectorNumber\":\"1\",\"contactAdditionalText\":\"MULTIMEDIA-ANSCHLUSSEINHEIT\",\"name\":\"KONTAKTFEDER\",\"partNumber\":\"A0055457526\",\"partNumberFormatted\":\"A 005 545 75 26\",\"partNumberType\":\"02_PIN\",\"referenceNumber\":\"A90/5\"},{\"connectorNumber\":\"1\",\"contactAdditionalText\":\"MULTIMEDIA-ANSCHLUSSEINHEIT\",\"partNumber\":\"A0055457626\",\"partNumberFormatted\":\"A 005 545 76 26\",\"partNumberType\":\"02_PIN\",\"referenceNumber\":\"A90/5\"},{\"connectorNumber\":\"HSD1\",\"contactAdditionalText\":\"MULTIMEDIA-ANSCHLUSSEINHEIT\",\"name\":\"Buchsengehäuse 4pol X\",\"partNumber\":\"A2125457826\",\"partNumberFormatted\":\"A 212 545 78 26\",\"partNumberType\":\"01_PIN_HOUSING\",\"referenceNumber\":\"A90/5\"},{\"connectorNumber\":\"HSD1\",\"contactAdditionalText\":\"MULTIMEDIA-ANSCHLUSSEINHEIT\",\"name\":\"Buchse HSD Au 0.5-0.5\",\"partNumber\":\"A0079822926\",\"partNumberFormatted\":\"A 007 982 29 26\",\"partNumberType\":\"02_PIN\",\"referenceNumber\":\"A90/5\"},{\"name\":\"Buchsengehäuse 9pol B\",\"partNumber\":\"A0105401581\",\"partNumberFormatted\":\"A 010 540 15 81\",\"partNumberType\":\"05_ACCESSORY\"},{\"partNumber\":\"A2125451426\",\"partNumberFormatted\":\"A 212 545 14 26\",\"partNumberType\":\"05_ACCESSORY\"},{\"partNumber\":\"N000000006465\",\"partNumberFormatted\":\"N 000000 006465\",\"partNumberType\":\"05_ACCESSORY\"}]}}",
                              HttpConstants.HTTP_STATUS_OK);
        });
    }

    public void testGetPartInfoWireHarnessKitWithSPKTextsConnectorAndSteering() {
        // Test für DAIMLER-15051 mit Aktivem SPK Mapping
        Map<UniversalConfigOption, Boolean> configOptions = new HashMap<>();
        configOptions.put(iPartsPlugin.CONFIG_USE_SPK_MAPPING, true);
        configOptions.put(iPartsPlugin.CONFIG_FILTER_WIRE_HARNESS_PARTS, true);

        // Aufruf mit Rechts-Lenker-FIN -> Texte .. Beifahrersitz ..
        executeTestWithBooleanConfigChanges(iPartsPlugin.getPluginConfig(), configOptions, () -> {
            Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");
            executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"identContext\": {\"aggTypeId\": \"F\", \"fin\": \"WDC2533052F058374\", \"modelId\": \"C253305\", \"productClassIds\": [\"P\"], \"productId\": \"C253_FC\"}, \"partContext\": {\"moduleId\": \"C253_FC_82_464_00001\", \"sequenceId\": \"00006\"}}",
                              additionalRequestProperties,
                              DWFile.get(getTestWorkingDir(), "resultGetPartInfoSPKMappingRightSteering.txt"),
                              HttpConstants.HTTP_STATUS_OK);
        });

        // Aufruf mit Links-Lenker-FIN -> Texte .. Fahrersitz ..
        executeTestWithBooleanConfigChanges(iPartsPlugin.getPluginConfig(), configOptions, () -> {
            Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");
            executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"identContext\": {\"aggTypeId\": \"F\", \"fin\": \"WDC2533051F058374\", \"modelId\": \"C253305\", \"productClassIds\": [\"P\"], \"productId\": \"C253_FC\"}, \"partContext\": {\"moduleId\": \"C253_FC_82_464_00001\", \"sequenceId\": \"00006\"}}",
                              additionalRequestProperties,
                              DWFile.get(getTestWorkingDir(), "resultGetPartInfoSPKMappingLeftSteering.txt"),
                              HttpConstants.HTTP_STATUS_OK);
        });
    }

    public void testGetPartInfoDBFallbackLanguageFromToken() {
        // Token enthält Sprachen zh, fr, es, d.h. zh ist die Hauptsprache und fr, es sind die Rückfallsprachen
        // im Ergebnis erhält man eine Teilebenennung auf chinesisch, d.h. die Hauptsprache wirkt
        // die SAA-Benennungen liegen offenbar nicht in chinesisch vor denn sie kommen in der Rückfallsprache fr
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiemgiLCJsYW5nMiI6ImZyIiwibGFuZzMiOiJlcyIsImV4cCI6MzY5MTgwNTAwMH0.Y5aIDmSHc4uHrNGvWbkqSofHD6UL1TT6fvyc3iHw-_I");

        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{" +
                          "\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}," +
                          "\"partContext\":{\"moduleId\":\"06F_92_025_00001\",\"sequenceId\":\"00001\"}," +
                          "\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"D669599\",\"productId\":\"06F\",\"filterOptions\":{}}" +
                          "}",
                          additionalRequestProperties,
                          // wenn man den chin. Text als ???????? in den String schreibt kommt zu Fehler beim Vergleich weil das Encoding unklar ist
                          "{\"partInfo\":{\"saaValidityDetails\":[{\"code\":\"Z 514.990\",\"description\":\"SV \u540E\u4FA7\u53CC\u4EBA\u5EA7\u6905\u6905\u57AB\",\"saaCodes\":[{\"code\":\"Z 514.990/02\",\"description\":\"A GAUCHE,ETOFFE DE CAPITONNAGE\"},{\"code\":\"Z 514.990/04\",\"description\":\"A GAUCHE, SIMILICUIR VERT OLIVE\"},{\"code\":\"Z 514.990/06\",\"description\":\"A GAUCHE, SIMILICUIR GRIS BLEU\"}]}]}}",
                          HttpConstants.HTTP_STATUS_OK);
    }

    public void testGetPartInfoDBFallbackLanguageFromSystem() {
        // Token enthält Sprachen zh, zh, zh, d.h. zh ist die Hauptsprache und es gibt keine Rückfallsprachen -> Systemrückfallsprachen (en, de) kommen zum Tragen
        // im Ergebnis erhält man eine Teilebenennung auf chinesisch, d.h. die Hauptsprache wirkt
        // die SAA-Benennungen liegen offenbar nicht in chinesisch vor denn sie kommen in der Systemrückfallsprache en
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiemgiLCJsYW5nMiI6InpoIiwibGFuZzMiOiJ6aCIsImV4cCI6MzY5MTgwNTAwMH0.NNLsnd3AxQQB_qYMQKAcSuPvk78672on3A-h943_MpI");

        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{" +
                          "\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}," +
                          "\"partContext\":{\"moduleId\":\"06F_92_025_00001\",\"sequenceId\":\"00001\"}," +
                          "\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"D669599\",\"productId\":\"06F\",\"filterOptions\":{}}" +
                          "}",
                          additionalRequestProperties,
                          // wenn man den chin. Text als ???????? in den String schreibt kommt zu Fehler beim Vergleich weil das Encoding unklar ist
                          "{\"partInfo\":{\"saaValidityDetails\":[{\"code\":\"Z 514.990\",\"description\":\"SV \u540E\u4FA7\u53CC\u4EBA\u5EA7\u6905\u6905\u57AB\",\"saaCodes\":[{\"code\":\"Z 514.990/02\",\"description\":\"LEFT, FABRIC\"},{\"code\":\"Z 514.990/04\",\"description\":\"LEFT, IMITATION LEATHER OLIVE-GREEN\"},{\"code\":\"Z 514.990/06\",\"description\":\"LEFT, IMITATION LEATHER BLUE-GREY\"}]}]}}",
                          HttpConstants.HTTP_STATUS_OK);
    }

    public void testGetPartInfoNoDBLanguageInToken() {
        // Token enthält überhaupt keine Sprachen -> nur die Systemrückfallsprachen (en, de) kommen zum Tragen (de aus dem
        // UserInfo DTO vom POST Payload JSON wird ignoriert)
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsImV4cCI6MzY5MTgwNTAwMH0.w8GffUsvHcJNJKgJmAlK2jjmNtjFFyes8hmHCNUYujU");

        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{" +
                          "\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}," +
                          "\"partContext\":{\"moduleId\":\"06F_92_025_00001\",\"sequenceId\":\"00001\"}," +
                          "\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"D669599\",\"productId\":\"06F\",\"filterOptions\":{}}" +
                          "}",
                          additionalRequestProperties,
                          "{\"partInfo\":{\"saaValidityDetails\":[{\"code\":\"Z 514.990\",\"description\":\"SV SEAT CUSHION,BOUBLE SSEAT,REAR\",\"saaCodes\":[{\"code\":\"Z 514.990/02\",\"description\":\"LEFT, FABRIC\"},{\"code\":\"Z 514.990/04\",\"description\":\"LEFT, IMITATION LEATHER OLIVE-GREEN\"},{\"code\":\"Z 514.990/06\",\"description\":\"LEFT, IMITATION LEATHER BLUE-GREY\"}]}]}}",
                          HttpConstants.HTTP_STATUS_OK);
    }

    // Tests für WebService: [Ident]
    public void testIdentByFINDefault() {
        // inkl. Notiz
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"WDD2050042R042987\"}",
                                                      "{\"identContexts\":[{\"aggregates\":[{\"aggregateNumber\":\"65192132498132\",\"aggTypeId\":\"M\",\"modelId\":\"D651921\",\"modelRemarks\":\"ALLISON AUTOMATIC TRANSMISSION WITH RETARDER\",\"modelTypeId\":\"D651\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PC\"],\"productId\":\"69L\"}],\"aggTypeId\":\"F\",\"connectWireHarnessEnabled\":true,\"datacardExists\":true,\"fin\":\"WDD2050042R042987\",\"integratedNavigationAvailable\":true,\"mbOilSpecs\":[{\"quantity\":\"1,2\",\"spec\":\"222.2\",\"text\":\"Siehe SI18.00-P-1311A; Einschränkung: Nur SAE 0W-30 oder SAE 5W-30 verwenden!\",\"type\":\"ENGINE_OIL\"}],\"mbSpecs\":[{\"quantity\":\"1,2\",\"spec\":\"222.2\",\"text\":\"Siehe SI18.00-P-1311A; Einschränkung: Nur SAE 0W-30 oder SAE 5W-30 verwenden!\",\"type\":\"ENGINE_OIL\"}],\"modelId\":\"C205004\",\"modelRemarks\":\"TRUCK,5400-MM WHEELBASE\",\"modelTypeId\":\"C205\",\"notes\":[{\"text\":\"Notiz zum Produkt C01\\r\\n2te Zeile\"}],\"productClassIds\":[\"P\"],\"productClassNames\":[\"PC\"],\"productId\":\"C01\"}]}");
    }

    /**
     * Bei diesem Ident-Aufruf die typeVersion auch mit ausgeben.
     * Alle anderen Ident-Aufrufe und andere Verwendungen vom IdentContext überspringen die Erzeugung per Schalter: IN_UNITTEST_MODE = TRUE <<== Default!
     * Hier wird der Schalter vorher explizit auf FALSE gesetzt und nachher wieder zurück.
     */
    public void testIdentByFINDefaultWithTypeVersion() {
        // Vorher die Prüfung der Marken und Produktgruppen abschalten
        UniversalConfiguration pluginConfig = iPartsWebservicePlugin.getPluginConfig();
        ConfigBase config = pluginConfig.getConfig();
        boolean checkPermissions = config.getBoolean(pluginConfig.getPath() + iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_PERMISSIONS.getKey(), true);
        // Den für die Unittests "bin im Unittest Mode" == TRUE gesetzten Schalter temporär ausschalten,
        // damit gezielt eine "typeVersion" im Ergebnisstring erzeugt wird.
        iPartsWSIdentContext.IN_UNITTEST_MODE = false;
        try {
            if (checkPermissions) {
                writeBooleanConfigValues(pluginConfig, false, iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_PERMISSIONS);
            }
            clearWebservicePluginsCaches(); // Caches müssen gelöscht werden, damit die Konstanten-Änderung auch auf jeden Fall berücksichtigt wird
            executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"WDD2050042R042987\"}",
                              "{\"identContexts\":[{\"aggregates\":[{\"aggregateNumber\":\"65192132498132\",\"aggTypeId\":\"M\",\"modelId\":\"D651921\",\"modelRemarks\":\"ALLISON AUTOMATIC TRANSMISSION WITH RETARDER\",\"modelTypeId\":\"D651\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PC\"],\"productId\":\"69L\",\"typeVersion\":\"M22\"}],\"aggTypeId\":\"F\",\"connectWireHarnessEnabled\":true,\"datacardExists\":true,\"fin\":\"WDD2050042R042987\",\"integratedNavigationAvailable\":true,\"mbOilSpecs\":[{\"quantity\":\"1,2\",\"spec\":\"222.2\",\"text\":\"Siehe SI18.00-P-1311A; Einschränkung: Nur SAE 0W-30 oder SAE 5W-30 verwenden!\",\"type\":\"ENGINE_OIL\"}],\"mbSpecs\":[{\"quantity\":\"1,2\",\"spec\":\"222.2\",\"text\":\"Siehe SI18.00-P-1311A; Einschränkung: Nur SAE 0W-30 oder SAE 5W-30 verwenden!\",\"type\":\"ENGINE_OIL\"}],\"modelId\":\"C205004\",\"modelRemarks\":\"TRUCK,5400-MM WHEELBASE\",\"modelTypeId\":\"C205\",\"notes\":[{\"text\":\"Notiz zum Produkt C01\\r\\n2te Zeile\"}],\"productClassIds\":[\"P\"],\"productClassNames\":[\"PC\"],\"productId\":\"C01\",\"typeVersion\":\"FW\"}]}");
        } finally {
            // den temporär ausgeschalteten "bin im Unittest Mode"-Schalter wieder einschalten
            iPartsWSIdentContext.IN_UNITTEST_MODE = true;
            writeBooleanConfigValues(pluginConfig, checkPermissions, iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_PERMISSIONS);
            clearWebservicePluginsCaches(); // Caches müssen gelöscht werden, damit die Konstanten-Änderung auch auf jeden Fall berücksichtigt wird
        }
    }

    public void testIdentByFINWithVISAttributes() {
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"WDB96763710228836\"}",
                                                      DWFile.get(getTestWorkingDir(), "resultIdentByFINWithVISAttributes.txt"));
    }

    public void testIdentByFINWithVISAttributesNoFOText() {
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"WDB96763710230592\"}",
                                                      DWFile.get(getTestWorkingDir(), "resultIdentByFINWithVISAttributesNoFOText.txt"));
    }

    public void testIdentByVINDefault() {
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"WD4PG2EE3G3138415\"}",
                                                      DWFile.get(getTestWorkingDir(), "resultIdentByVINDefault.txt"));
    }

    public void testIdentByVINFallbackToModel() {
        // inkl. Notiz
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"4JGWF02E3G3138415\"}",
                                                      "{\"identContexts\":[{\"aggregates\":[{\"aggTypeId\":\"M\",\"modelId\":\"D651921\",\"modelRemarks\":\"ALLISON AUTOMATIC TRANSMISSION WITH RETARDER\",\"modelTypeId\":\"D651\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PC\"],\"productId\":\"69L\"}],\"aggTypeId\":\"F\",\"connectWireHarnessEnabled\":true,\"errorText\":\"No datacard found for VIN: 4JGWF02E3G3138415\",\"integratedNavigationAvailable\":true,\"modelId\":\"C205002\",\"modelTypeId\":\"C205\",\"notes\":[{\"text\":\"Notiz zum Produkt C01\\r\\n2te Zeile\"}],\"productClassIds\":[\"P\"],\"productClassNames\":[\"PC\"],\"productId\":\"C01\",\"vin\":\"4JGWF02E3G3138415\"}]}");
    }

    public void testIdentByVINFallbackToModelOnlyVisible() {
        executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"MHL2106E3G3138415\"}",
                          "{\"code\":4001,\"message\":\"No datacard or fallback models found for VIN: MHL2106E3G3138415\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testIdentByVINNoFallbackToModel() {
        executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"5JGWF02E3G3138415\"}",
                          "{\"code\":4001,\"message\":\"No datacard or fallback models found for VIN: 5JGWF02E3G3138415\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testIdentByVINModelDefault() {
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"4JGWF02\"}",
                                                      "{\"identContexts\":[{\"aggregates\":[{\"aggTypeId\":\"M\",\"modelId\":\"D651921\",\"modelRemarks\":\"ALLISON AUTOMATIC TRANSMISSION WITH RETARDER\",\"modelTypeId\":\"D651\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PC\"],\"productId\":\"69L\"}],\"aggTypeId\":\"F\",\"connectWireHarnessEnabled\":true,\"integratedNavigationAvailable\":true,\"modelId\":\"C205002\",\"modelTypeId\":\"C205\",\"notes\":[{\"text\":\"Notiz zum Produkt C01\\r\\n2te Zeile\"}],\"productClassIds\":[\"P\"],\"productClassNames\":[\"PC\"],\"productId\":\"C01\"}]}");
    }

    public void testIdentByVINModelError() {
        executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"4JGWF0A\"}",
                          "{\"code\":4001,\"message\":\"No VIN model fallback available for 'identCode': 4JGWF0A\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testIdentByDModelNumber() {
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"},\"identCode\":\"D651913\"}",
                                                      "{\"identContexts\":[{\"aggTypeId\":\"M\",\"modelId\":\"D651913\",\"modelRemarks\":\"DIESELMOTOR,100 KW (136 PS),MIT AUFLADUNG,LADELUFTKUEHLUNG\",\"modelTypeId\":\"D651\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"69L\"},{\"aggTypeId\":\"M\",\"modelId\":\"D651913\",\"modelRemarks\":\"DIESELMOTOR,100 KW (136 PS),MIT AUFLADUNG,LADELUFTKUEHLUNG\",\"modelTypeId\":\"D651\",\"productClassIds\":[],\"productClassNames\":[],\"productId\":\"D6519\"},{\"aggTypeId\":\"M\",\"modelId\":\"D651913\",\"modelRemarks\":\"DIESELMOTOR,100 KW (136 PS),MIT AUFLADUNG,LADELUFTKUEHLUNG\",\"modelTypeId\":\"D651\",\"productClassIds\":[],\"productClassNames\":[],\"productId\":\"D6519_KGTU\"}]}");
    }

    public void testIdentByModelNumberSingleGerman() {
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"},\"identCode\":\"967001\"}",
                                                      DWFile.get(getTestWorkingDir(), "resultIdentByModelNumberSingleGerman.txt"));
    }

    public void testIdentByModelNumberMultipleGerman() {
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"},\"identCode\":\"204001\"}",
                                                      DWFile.get(getTestWorkingDir(), "resultIdentByModelNumberMultipleGerman.txt"));
    }

    public void testIdentByModelNumberWithProductClassIdsDefault() {
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"204001\",\"productClassIds\":[\"A\"]}",
                                                      "{\"identContexts\":[]}");
    }

    public void testIdentByModelNumberWithProductIdGerman() {
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"},\"identCode\":\"204001\",\"productId\":\"C204\"}",
                                                      "{\"identContexts\":[{\"aggregates\":[{\"aggTypeId\":\"M\",\"modelId\":\"D651913\",\"modelRemarks\":\"DIESELMOTOR,100 KW (136 PS),MIT AUFLADUNG,LADELUFTKUEHLUNG\",\"modelTypeId\":\"D651\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"69L\"},{\"aggTypeId\":\"GM\",\"modelDesc\":\"NSG 400 / 6.1\",\"modelId\":\"D711653\",\"modelTypeId\":\"D711\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"00P\"},{\"aggTypeId\":\"GM\",\"modelDesc\":\"NSG 400 / 5.3\",\"modelId\":\"D711654\",\"modelTypeId\":\"D711\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"00P\"}],\"aggTypeId\":\"F\",\"modelDesc\":\"C 200 CDI\",\"modelId\":\"C204001\",\"modelTypeId\":\"C204\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"C204\"}]}");
    }

    public void testIdentByModelNumberLong() {
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"},\"identCode\":\"731310001\"}",
                                                      "{\"identContexts\":[]}");
    }

    public void testIdentByModelNumberLongWithPrefix() {
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"},\"identCode\":\"D731310001\"}",
                                                      "{\"identContexts\":[]}");
    }

    public void testIdentErrorEmpty() {
        executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{}",
                          "{\"code\":4000,\"message\":\"Attribute 'user' is missing\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testIdentErrorMissingParameter() {
        executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"}}",
                          "{\"code\":4000,\"message\":\"Attribute 'identCode' is invalid (missing or empty)\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testIdentByFINAutoProdSelectIdentCheck() {
        // inkl. Notiz
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"WDD2050042R042987\"} ",
                                                      "{\"identContexts\":[{\"aggregates\":[{\"aggregateNumber\":\"65192132498132\",\"aggTypeId\":\"M\",\"modelId\":\"D651921\",\"modelRemarks\":\"ALLISON AUTOMATIC TRANSMISSION WITH RETARDER\",\"modelTypeId\":\"D651\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PC\"],\"productId\":\"69L\"}],\"aggTypeId\":\"F\",\"connectWireHarnessEnabled\":true,\"datacardExists\":true,\"fin\":\"WDD2050042R042987\",\"integratedNavigationAvailable\":true,\"mbOilSpecs\":[{\"quantity\":\"1,2\",\"spec\":\"222.2\",\"text\":\"Siehe SI18.00-P-1311A; Einschränkung: Nur SAE 0W-30 oder SAE 5W-30 verwenden!\",\"type\":\"ENGINE_OIL\"}],\"mbSpecs\":[{\"quantity\":\"1,2\",\"spec\":\"222.2\",\"text\":\"Siehe SI18.00-P-1311A; Einschränkung: Nur SAE 0W-30 oder SAE 5W-30 verwenden!\",\"type\":\"ENGINE_OIL\"}],\"modelId\":\"C205004\",\"modelRemarks\":\"TRUCK,5400-MM WHEELBASE\",\"modelTypeId\":\"C205\",\"notes\":[{\"text\":\"Notiz zum Produkt C01\\r\\n2te Zeile\"}],\"productClassIds\":[\"P\"],\"productClassNames\":[\"PC\"],\"productId\":\"C01\"}]}");

    }

    // Aggregatespezifische Ident-Aufrufe
    public void testIdentAggregateIdentType() {
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"93073010224672\",\"aggTypeId\":\"AS\"}",
                                                      "{\"identContexts\":[{\"aggregateNumber\":\"93073010224672\",\"aggTypeId\":\"AS\",\"datacardExists\":true,\"modelDesc\":\"MCS BOX\",\"modelId\":\"D930730\",\"modelRemarks\":\"ABGASNACHBEHANDLUNGSEINHEIT\",\"modelTypeId\":\"D930\",\"productClassIds\":[\"F\",\"S\"],\"productClassNames\":[\"smart\",\"\"],\"productId\":\"L01\"}]}");
        //DWFile.get(getTestWorkingDir(), "")
    }

    public void testIdentAggregateModelNotFound() {
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"9508901X001367\",\"aggTypeId\":\"FH\"} ",
                                                      "{\"identContexts\":[]}");
    }

    public void testIdentAggregateIdentOnly() {
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"93073010224672\"} ",
                                                      "{\"identContexts\":[{\"aggregateNumber\":\"93073010224672\",\"aggTypeId\":\"AS\",\"datacardExists\":true,\"modelDesc\":\"MCS BOX\",\"modelId\":\"D930730\",\"modelRemarks\":\"ABGASNACHBEHANDLUNGSEINHEIT\",\"modelTypeId\":\"D930\",\"productClassIds\":[\"F\",\"S\"],\"productClassNames\":[\"smart\",\"\"],\"productId\":\"L01\"}]}");
    }

    public void testIdentAggregateModelFromIdent() {
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"93073010224673\"}",
                                                      "{\"identContexts\":[{\"aggregateNumber\":\"93073010224673\",\"aggTypeId\":\"AS\",\"errorText\":\"No datacard found for aggregate ident: 93073010224673\",\"modelDesc\":\"MCS BOX\",\"modelId\":\"D930730\",\"modelRemarks\":\"ABGASNACHBEHANDLUNGSEINHEIT\",\"modelTypeId\":\"D930\",\"productClassIds\":[\"F\",\"S\"],\"productClassNames\":[\"smart\",\"\"],\"productId\":\"L01\"}]}");
    }

    public void testIdentAggregateModelOnly() {
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"930730\"}",
                                                      "{\"identContexts\":[{\"aggTypeId\":\"AS\",\"modelDesc\":\"MCS BOX\",\"modelId\":\"D930730\",\"modelRemarks\":\"ABGASNACHBEHANDLUNGSEINHEIT\",\"modelTypeId\":\"D930\",\"productClassIds\":[\"F\",\"S\"],\"productClassNames\":[\"smart\",\"\"],\"productId\":\"L01\"}]}");
    }

    public void testIdentAggregateWrongAggType() {
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"93073010224672\",\"aggTypeId\":\"M\"}",
                                                      "{\"identContexts\":[]}");
    }

    public void testIdentAggregateIgnoreWrongAggType() {
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"930730\",\"aggTypeId\":\"M\"}",
                                                      "{\"identContexts\":[{\"aggTypeId\":\"AS\",\"modelDesc\":\"MCS BOX\",\"modelId\":\"D930730\",\"modelRemarks\":\"ABGASNACHBEHANDLUNGSEINHEIT\",\"modelTypeId\":\"D930\",\"productClassIds\":[\"F\",\"S\"],\"productClassNames\":[\"smart\",\"\"],\"productId\":\"L01\"}]}");
    }

    public void testIdentAggregateWrongProdClassId() {
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"93073010224672\",\"productClassIds\":[\"P\"]}",
                                                      "{\"identContexts\":[]}");
    }

    public void testIdentAggregateWrongProductId() {
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"93073010224672\",\"productId\":\"C01\"}",
                                                      "{\"identContexts\":[]}");
    }

    public void testIdentAggregateInvalidIdentCode() {
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"93073012312\"}",
                                                      "{\"code\":4002,\"message\":\"Invalid format of attribute 'identCode': 93073012312\"}",
                                                      HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testIdentAggregateForPallet() {
        //DAIMLER-6917
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"40561110\",\"aggTypeId\":\"P\"}",
                                                      "{\"identContexts\":[{\"aggregateNumber\":\"40561110\",\"aggTypeId\":\"P\",\"errorText\":\"No datacard found for aggregate ident: 40561110\",\"modelDesc\":\"U 300 , U 400\",\"modelId\":\"D405611\",\"modelRemarks\":\"PRITSCHE 2400 X 2050 X 400 MM\",\"modelTypeId\":\"D405\",\"productClassIds\":[\"U\"],\"productClassNames\":[\"Unimog\"],\"productId\":\"15H\"}]}");
    }

    // Ident mit und ohne Admin-Option "Nur retail-relevante Produkte berücksichtigen (Flag "Produkt sichtbar")"
    public void testIdentProductInvisibleOnlyRetailRelevantProducts() {
        clearWebservicePluginsCaches(); // Caches müssen gelöscht werden, weil die Ergebnisse sich nun bei gleichem Request unterscheiden
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"D651970\"}",
                                                      "{\"identContexts\":[{\"aggTypeId\":\"M\",\"modelId\":\"D651970\",\"modelTypeId\":\"D651\",\"productClassIds\":[],\"productClassNames\":[],\"productId\":\"D6519\"},{\"aggTypeId\":\"M\",\"modelId\":\"D651970\",\"modelTypeId\":\"D651\",\"productClassIds\":[],\"productClassNames\":[],\"productId\":\"D6519_KGTU\"}]}");
    }

    public void testIdentProductInvisibleAllProducts() {
        // Temporär die Admin-Option "Nur retail-relevante Produkte berücksichtigen (Flag "Produkt sichtbar")" deaktivieren
        UniversalConfiguration pluginConfig = iPartsWebservicePlugin.getPluginConfig();
        writeBooleanConfigValues(pluginConfig, false, iPartsWebservicePlugin.CONFIG_ONLY_RETAIL_RELEVANT_PRODUCTS);
        try {
            clearWebservicePluginsCaches(); // Caches müssen gelöscht werden, weil die Ergebnisse sich nun bei gleichem Request unterscheiden
            executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                          "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"D651970\"}",
                                                          "{\"identContexts\":[{\"aggTypeId\":\"M\",\"modelId\":\"D651970\",\"modelTypeId\":\"D651\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PC\"],\"productId\":\"00R\"},{\"aggTypeId\":\"M\",\"modelId\":\"D651970\",\"modelTypeId\":\"D651\",\"productClassIds\":[],\"productClassNames\":[],\"productId\":\"D6519\"},{\"aggTypeId\":\"M\",\"modelId\":\"D651970\",\"modelTypeId\":\"D651\",\"productClassIds\":[],\"productClassNames\":[],\"productId\":\"D6519_KGTU\"}]}");
        } finally {
            writeBooleanConfigValues(pluginConfig, true, iPartsWebservicePlugin.CONFIG_ONLY_RETAIL_RELEVANT_PRODUCTS);
            clearWebservicePluginsCaches(); // Caches müssen gelöscht werden, damit die Admin-Änderung auch auf jeden Fall berücksichtigt wird
        }
    }

    // Datenkarte enthält Tauschmotor (Aggregate Datenkarte) enthält "rebuiltAggregate" Attribut
    public void testIdentWithRebuiltEngine() {
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"ZZZ9670052L852399\"}",
                                                      DWFile.get(getTestWorkingDir(), "resultIdentWithRebuiltEngine.txt"));
    }


    // Datenkarte auf [FIN]-Basis enthält ein [VERSCHROTTETES] Fahrzeug
    public void testIdentFinScrapped() {
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"WDD2210711A243775\"}",
                                                      "{\"code\":4004,\"message\":\"Vehicle scrapped\"}", 400);
    }

    // Datenkarte auf [FIN]-Basis enthält ein [GESTOHLENES] Fahrzeug
    public void testIdentFinStolen() {
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"WDC2049011F614114\"}",
                                                      "{\"code\":4005,\"message\":\"Vehicle stolen\"}", 400);
    }


    // Datenkarte auf [VIN]-Basis enthält ein [VERSCHROTTETES] Fahrzeug
    public void testIdentVinScrapped() {
        executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"VD4PG2EE3G3138411\"}",
                          "{\"code\":4004,\"message\":\"Vehicle scrapped\"}", 400);
    }

    // Datenkarte auf [VIN]-Basis enthält ein [GESTOHLENES] Fahrzeug
    public void testIdentVinStolen() {
        executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"VD5PG2EE3G3138415\"}",
                          "{\"code\":4005,\"message\":\"Vehicle stolen\"}", 400);
    }


    // Das Baumuster: [C463248] ist in drei Produkten enthalten
    // - 37M <-- fällt weg,                     DA_PRODUCT_MODELS.DPM_MODEL_VISIBLE == FALSE
    // - 62S <-- fällt über das Produkt weg     DP_PRODUCT_VISIBLE.DA_PRODUCT == FALSE
    // - 62U <== dieses Produkt (mit Aggregat 37P) wird als Ergebnis erwartet
    public void testIdentModelNotVisible() {
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"C463248\"} ",
                                                      "{\"identContexts\":[{\"aggregates\":[{\"aggTypeId\":\"FH\",\"modelDesc\":\"G 500, G 55 AMG\",\"modelId\":\"D463832\",\"modelRemarks\":\"AUFBAU,STATION-WAGEN LANG FUENFTUERIG,RADSTAND 2850 MM\",\"modelTypeId\":\"D463\",\"productClassIds\":[\"G\",\"U\"],\"productClassNames\":[\"Cross-country vehicle\",\"Unimog\"],\"productId\":\"37P\",\"productRemarks\":\"BIS CAB 144225\"}],\"aggTypeId\":\"F\",\"integratedNavigationAvailable\":true,\"modelId\":\"C463248\",\"modelTypeId\":\"C463\",\"productClassIds\":[\"G\"],\"productClassNames\":[\"Cross-country vehicle\"],\"productId\":\"62U\",\"productRemarks\":\"As of ident. no. 144226\"}]}");

    }

    /**
     * Datenkarte enthält Elektromotor und Hochvoltbatterie
     */
    public void testIdentElectroEngineHighVoltBattery() {
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"},\"identCode\":\"WDD2938901F001612\"}",
                                                      "{\"identContexts\":[{\"aggregates\":[{\"aggregateNumber\":\"78099930001919\",\"aggSubType\":\"eEngine2\",\"aggTypeId\":\"E\",\"modelId\":\"D780999\",\"modelTypeId\":\"D780\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"Z98\"},{\"aggregateNumber\":\"999999AB000415\",\"aggSubType\":\"highVoltageBat1\",\"aggTypeId\":\"B\",\"modelId\":\"D999999\",\"modelTypeId\":\"D999\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"Z97\"}],\"aggTypeId\":\"F\",\"datacardExists\":true,\"fin\":\"WDD2938901F001612\",\"integratedNavigationAvailable\":true,\"modelId\":\"C293890\",\"modelTypeId\":\"C293\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"Z99\"}]}");
    }

    /**
     * WebService [Ident], Tests für den Parameter [includeValidities]
     */
    // -------------------
    // Aggregatedatenkarte
    // -------------------
    public void testIdentIncludeValiditiesAggregatePart1() {
        // Test auf OHNE [includeValidities]
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"language\":\"en\",\"country\":\"200\"},\"identCode\":\"WDD2050042R042987\"}",
                                                      DWFile.get(getTestWorkingDir(), "resultWSIdentIncludeAGGValiditiesOHNE.txt"));
    }

    public void testIdentIncludeValiditiesAggregatePart2() {
        // Test auf MIT [includeValidities == FALSE]
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"language\":\"en\",\"country\":\"200\"},\"identCode\":\"WDD2050042R042987\",\"includeValidities\":\"false\"}",
                                                      DWFile.get(getTestWorkingDir(), "resultWSIdentIncludeAGGValiditiesFALSE.txt"));
    }

    public void testIdentIncludeValiditiesAggregatePart3() {
        // Test auf MIT [includeValidities == TRUE]
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"language\":\"en\",\"country\":\"200\"},\"identCode\":\"WDD2050042R042987\",\"includeValidities\":\"true\"}",
                                                      DWFile.get(getTestWorkingDir(), "resultWSIdentIncludeAGGValiditiesTRUE.txt"));
    }

    // --------------
    // FIN Datenkarte
    // --------------
    public void testIdentIncludeValiditiesFINPart1() {
        // Test auf OHNE [includeValidities]
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"language\":\"en\",\"country\":\"200\"},\"identCode\":\"WDD2050041F004362\"}",
                                                      DWFile.get(getTestWorkingDir(), "resultWSIdentIncludeFINValiditiesOHNE.txt"));
    }

    public void testIdentIncludeValiditiesFINPart2() {
        // Test auf MIT [includeValidities == FALSE]
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"language\":\"en\",\"country\":\"200\"},\"identCode\":\"WDD2050041F004362\",\"includeValidities\":\"false\"}",
                                                      DWFile.get(getTestWorkingDir(), "resultWSIdentIncludeFINValiditiesFALSE.txt"));
    }

    public void testIdentIncludeValiditiesFINPart3() {
        // Test auf MIT [includeValidities == TRUE]
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"language\":\"en\",\"country\":\"200\"},\"identCode\":\"WDD2050041F004362\",\"includeValidities\":\"true\"}",
                                                      DWFile.get(getTestWorkingDir(), "resultWSIdentIncludeFINValiditiesTRUE.txt"));
    }

    // --------------
    // VIN Datenkarte
    // --------------
    public void testIdentIncludeValiditiesVINPart1() {
        // Test auf OHNE [includeValidities]
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"language\":\"en\",\"country\":\"200\"},\"identCode\":\"WD4PG2EE3G3138415\"}",
                                                      DWFile.get(getTestWorkingDir(), "resultWSIdentIncludeVINValiditiesOHNE.txt"));
    }

    public void testIdentIncludeValiditiesVINPart2() {
        // Test auf MIT [includeValidities == FALSE]
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"language\":\"en\",\"country\":\"200\"},\"identCode\":\"WD4PG2EE3G3138415\",\"includeValidities\":\"false\"}",
                                                      DWFile.get(getTestWorkingDir(), "resultWSIdentIncludeVINValiditiesFALSE.txt"));
    }

    public void testIdentIncludeValiditiesVINPart3() {
        // Test auf MIT [includeValidities == TRUE]
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"language\":\"en\",\"country\":\"200\"},\"identCode\":\"WD4PG2EE3G3138415\",\"includeValidities\":\"true\"}",
                                                      DWFile.get(getTestWorkingDir(), "resultWSIdentIncludeVINValiditiesTRUE.txt"));
    }

    // ---------
    // Baumuster
    // ---------
    // Beim Baumuster darf der Schalter [includeValidities] keine Auswirkung haben ==> es ist immer die gleiche Ergebnisdatei!
    public void testIdentIncludeValiditiesModelPart1() {
        // Test auf OHNE [includeValidities]
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"language\":\"en\",\"country\":\"200\"},\"identCode\":\"C204000\"}",
                                                      DWFile.get(getTestWorkingDir(), "resultWSIdentIncludeBMValidities.txt"));
    }

    public void testIdentIncludeValiditiesModelPart2() {
        // Test auf MIT [includeValidities == FALSE]
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"language\":\"en\",\"country\":\"200\"},\"identCode\":\"C204000\",\"includeValidities\":\"false\"}",
                                                      DWFile.get(getTestWorkingDir(), "resultWSIdentIncludeBMValidities.txt"));
    }

    public void testIdentIncludeValiditiesModelPart3() {
        // Test auf MIT [includeValidities == TRUE]
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"language\":\"en\",\"country\":\"200\"},\"identCode\":\"C204000\",\"includeValidities\":\"true\"}",
                                                      DWFile.get(getTestWorkingDir(), "resultWSIdentIncludeBMValidities.txt"));
    }

    // ---------------------
    // salesAreaInformation
    // ---------------------
    public void testIdentIncludeValiditiesSalesAreaInformationPart1() {
        // Test auf OHNE [includeValidities]
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"language\":\"en\",\"country\":\"200\"},\"identCode\":\"WDB2030061F261383\"}",
                                                      DWFile.get(getTestWorkingDir(), "resultWSIdentSalesAreaInformationIncludeValiditiesOHNE.txt"));
    }

    public void testIdentIncludeValiditiesSalesAreaInformationPart2() {
        // Test auf MIT [includeValidities == FALSE]
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"language\":\"en\",\"country\":\"200\"},\"identCode\":\"WDB2030061F261383\",\"includeValidities\":\"false\"}",
                                                      DWFile.get(getTestWorkingDir(), "resultWSIdentSalesAreaInformationIncludeValiditiesFALSE.txt"));
    }

    public void testIdentIncludeValiditiesSalesAreaInformationPart3() {
        // Test auf MIT [includeValidities == TRUE]
        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                      "{\"user\":{\"userId\":\"user\",\"language\":\"en\",\"country\":\"200\"},\"identCode\":\"WDB2030061F261383\",\"includeValidities\":\"true\"}",
                                                      DWFile.get(getTestWorkingDir(), "resultWSIdentSalesAreaInformationIncludeValiditiesTRUE.txt"));
    }

    public void testIdentWithTokenPermissionsCheckPart1() {
        // Test MIT Token Permissions Check (default true); OHNE Permissions im Token; muss zu einem Fehler führen.
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiZGUiLCJsYW5nMiI6ImZyIiwibGFuZzMiOiJlcyIsImV4cCI6OTk5OTk5OTk5OSwicGVybWlzc2lvbnMiOnt9fQ.8vwKzS_cX4EljkSU9IN0dNKdQi_fetITDhC3QzGf9ew");
        executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"en\",\"country\":\"200\"},\"identCode\":\"C204000\"}",
                          additionalRequestProperties,
                          "{\"code\":4031,\"message\":\"You are not authorized to access this information\",\"missingPermissions\":[\"MB.PASSENGER-CAR\",\"MB.TRUCK\",\"SMT.PASSENGER-CAR\"]}",
                          HttpConstants.HTTP_STATUS_FORBIDDEN);
    }

    public void testIdentWithTokenPermissionsCheckPart2() {
        // Test MIT Token Permissions Check (default true); MIT allen Permissions im Token;
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");
        executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"en\",\"country\":\"200\"},\"identCode\":\"C204000\"}",
                          additionalRequestProperties,
                          "{\"identContexts\":[{\"aggregates\":[{\"aggTypeId\":\"M\",\"modelId\":\"D651913\",\"modelRemarks\":\"DIESELMOTOR,100 KW (136 PS),MIT AUFLADUNG,LADELUFTKUEHLUNG\",\"modelTypeId\":\"D651\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"69L\"},{\"aggTypeId\":\"GM\",\"modelDesc\":\"NSG 400 / 6.1\",\"modelId\":\"D711653\",\"modelTypeId\":\"D711\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"00P\"},{\"aggTypeId\":\"GM\",\"modelDesc\":\"NSG 400 / 5.3\",\"modelId\":\"D711654\",\"modelTypeId\":\"D711\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"00P\"}],\"aggTypeId\":\"F\",\"modelDesc\":\"C 180 CDI       BLUEEFFICIENCY\",\"modelId\":\"C204000\",\"modelRemarks\":\"GETRIEBE, ELEKTRONISCH-PNEUMATISCHE SCHALTUNG\",\"modelTypeId\":\"C204\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"C204\"},{\"aggTypeId\":\"M\",\"modelDesc\":\"C 180 CDI       BLUEEFFICIENCY\",\"modelId\":\"C204000\",\"modelRemarks\":\"GETRIEBE, ELEKTRONISCH-PNEUMATISCHE SCHALTUNG\",\"modelTypeId\":\"C204\",\"productClassIds\":[\"L\"],\"productClassNames\":[\"LKW\"],\"productId\":\"C204_BRANCH_TRUCK\"},{\"aggTypeId\":\"F\",\"modelDesc\":\"C 180 CDI       BLUEEFFICIENCY\",\"modelId\":\"C204000\",\"modelRemarks\":\"GETRIEBE, ELEKTRONISCH-PNEUMATISCHE SCHALTUNG\",\"modelTypeId\":\"C204\",\"productClassIds\":[\"F\"],\"productClassNames\":[\"Smart\"],\"productId\":\"C204_BRAND_SMT\"},{\"aggregates\":[{\"aggTypeId\":\"M\",\"modelId\":\"D651913\",\"modelRemarks\":\"DIESELMOTOR,100 KW (136 PS),MIT AUFLADUNG,LADELUFTKUEHLUNG\",\"modelTypeId\":\"D651\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"69L\"},{\"aggTypeId\":\"GM\",\"modelDesc\":\"NSG 400 / 6.1\",\"modelId\":\"D711653\",\"modelTypeId\":\"D711\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"00P\"},{\"aggTypeId\":\"GM\",\"modelDesc\":\"NSG 400 / 5.3\",\"modelId\":\"D711654\",\"modelTypeId\":\"D711\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"00P\"}],\"aggTypeId\":\"F\",\"modelDesc\":\"C 180 CDI       BLUEEFFICIENCY\",\"modelId\":\"C204000\",\"modelRemarks\":\"GETRIEBE, ELEKTRONISCH-PNEUMATISCHE SCHALTUNG\",\"modelTypeId\":\"C204\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"C204_DE\",\"validCountries\":[\"DE\"]},{\"aggregates\":[{\"aggTypeId\":\"M\",\"modelId\":\"D651913\",\"modelRemarks\":\"DIESELMOTOR,100 KW (136 PS),MIT AUFLADUNG,LADELUFTKUEHLUNG\",\"modelTypeId\":\"D651\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"69L\"},{\"aggTypeId\":\"GM\",\"modelDesc\":\"NSG 400 / 6.1\",\"modelId\":\"D711653\",\"modelTypeId\":\"D711\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"00P\"},{\"aggTypeId\":\"GM\",\"modelDesc\":\"NSG 400 / 5.3\",\"modelId\":\"D711654\",\"modelTypeId\":\"D711\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"00P\"}],\"aggTypeId\":\"F\",\"modelDesc\":\"C 180 CDI       BLUEEFFICIENCY\",\"modelId\":\"C204000\",\"modelRemarks\":\"GETRIEBE, ELEKTRONISCH-PNEUMATISCHE SCHALTUNG\",\"modelTypeId\":\"C204\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"C204_KGTU\"}]}",
                          HttpConstants.HTTP_STATUS_OK);
    }

    public void testIdentWithTokenPermissionsCheckPart3() {
        // Test MIT Token Permissions Check (default true); MIT PASSENGER-CAR Permissions im Token; LKW wird ausgeblendet;
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiXSwiU01UIjpbIlBBU1NFTkdFUi1DQVIiXSwiTVlCIjpbIlBBU1NFTkdFUi1DQVIiXX0sImxhbmcxIjoiZGUiLCJsYW5nMiI6ImVuIiwibGFuZzMiOiJmciIsImV4cCI6OTk5OTk5OTk5OX0.gkDKJlMCj4_qggnG_osS1OJUc7zISy2M26dqE6WUypk");
        executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"en\",\"country\":\"200\"},\"identCode\":\"C204000\"}",
                          additionalRequestProperties,
                          "{\"identContexts\":[{\"aggregates\":[{\"aggTypeId\":\"M\",\"modelId\":\"D651913\",\"modelRemarks\":\"DIESELMOTOR,100 KW (136 PS),MIT AUFLADUNG,LADELUFTKUEHLUNG\",\"modelTypeId\":\"D651\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"69L\"},{\"aggTypeId\":\"GM\",\"modelDesc\":\"NSG 400 / 6.1\",\"modelId\":\"D711653\",\"modelTypeId\":\"D711\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"00P\"},{\"aggTypeId\":\"GM\",\"modelDesc\":\"NSG 400 / 5.3\",\"modelId\":\"D711654\",\"modelTypeId\":\"D711\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"00P\"}],\"aggTypeId\":\"F\",\"modelDesc\":\"C 180 CDI       BLUEEFFICIENCY\",\"modelId\":\"C204000\",\"modelRemarks\":\"GETRIEBE, ELEKTRONISCH-PNEUMATISCHE SCHALTUNG\",\"modelTypeId\":\"C204\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"C204\"},{\"aggTypeId\":\"F\",\"modelDesc\":\"C 180 CDI       BLUEEFFICIENCY\",\"modelId\":\"C204000\",\"modelRemarks\":\"GETRIEBE, ELEKTRONISCH-PNEUMATISCHE SCHALTUNG\",\"modelTypeId\":\"C204\",\"productClassIds\":[\"F\"],\"productClassNames\":[\"Smart\"],\"productId\":\"C204_BRAND_SMT\"},{\"aggregates\":[{\"aggTypeId\":\"M\",\"modelId\":\"D651913\",\"modelRemarks\":\"DIESELMOTOR,100 KW (136 PS),MIT AUFLADUNG,LADELUFTKUEHLUNG\",\"modelTypeId\":\"D651\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"69L\"},{\"aggTypeId\":\"GM\",\"modelDesc\":\"NSG 400 / 6.1\",\"modelId\":\"D711653\",\"modelTypeId\":\"D711\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"00P\"},{\"aggTypeId\":\"GM\",\"modelDesc\":\"NSG 400 / 5.3\",\"modelId\":\"D711654\",\"modelTypeId\":\"D711\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"00P\"}],\"aggTypeId\":\"F\",\"modelDesc\":\"C 180 CDI       BLUEEFFICIENCY\",\"modelId\":\"C204000\",\"modelRemarks\":\"GETRIEBE, ELEKTRONISCH-PNEUMATISCHE SCHALTUNG\",\"modelTypeId\":\"C204\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"C204_DE\",\"validCountries\":[\"DE\"]},{\"aggregates\":[{\"aggTypeId\":\"M\",\"modelId\":\"D651913\",\"modelRemarks\":\"DIESELMOTOR,100 KW (136 PS),MIT AUFLADUNG,LADELUFTKUEHLUNG\",\"modelTypeId\":\"D651\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"69L\"},{\"aggTypeId\":\"GM\",\"modelDesc\":\"NSG 400 / 6.1\",\"modelId\":\"D711653\",\"modelTypeId\":\"D711\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"00P\"},{\"aggTypeId\":\"GM\",\"modelDesc\":\"NSG 400 / 5.3\",\"modelId\":\"D711654\",\"modelTypeId\":\"D711\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"00P\"}],\"aggTypeId\":\"F\",\"modelDesc\":\"C 180 CDI       BLUEEFFICIENCY\",\"modelId\":\"C204000\",\"modelRemarks\":\"GETRIEBE, ELEKTRONISCH-PNEUMATISCHE SCHALTUNG\",\"modelTypeId\":\"C204\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"C204_KGTU\"}]}",
                          HttpConstants.HTTP_STATUS_OK);
    }

    public void testIdentWithTokenPermissionsCheckPart4() {
        // Test MIT Token Permissions Check (default true); MIT TRUCK Permissions im Token; Nur LKW
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlRSVUNLIl19LCJsYW5nMSI6ImRlIiwibGFuZzIiOiJlbiIsImxhbmczIjoiZnIiLCJleHAiOjk5OTk5OTk5OTl9.S41S3ITWv6Cg3MIJtcNQnaI1-5gvXm8Z77d50Z9p_Qw");
        executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"en\",\"country\":\"200\"},\"identCode\":\"C204000\"}",
                          additionalRequestProperties,
                          "{\"identContexts\":[{\"aggTypeId\":\"M\",\"modelDesc\":\"C 180 CDI       BLUEEFFICIENCY\",\"modelId\":\"C204000\",\"modelRemarks\":\"GETRIEBE, ELEKTRONISCH-PNEUMATISCHE SCHALTUNG\",\"modelTypeId\":\"C204\",\"productClassIds\":[\"L\"],\"productClassNames\":[\"LKW\"],\"productId\":\"C204_BRANCH_TRUCK\"}]}",
                          HttpConstants.HTTP_STATUS_OK);
    }

    public void testIdentWithTokenPermissionsCheckPart5() {
        // Test MIT Permissions Check (default true); MIT allen Permissions im Token; productClassIds: F + O (Smart + Bus)
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");
        executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"en\",\"country\":\"200\"},\"identCode\":\"D936910\"}",
                          additionalRequestProperties,
                          "{\"identContexts\":[{\"aggTypeId\":\"M\",\"modelDesc\":\"OM 936\",\"modelId\":\"D936910\",\"modelRemarks\":\"DIESELMOTOR, MIT AUFLADUNG, LADELUFTKUEHLUNG\",\"modelTypeId\":\"D936\",\"productClassIds\":[\"F\",\"O\"],\"productClassNames\":[\"Smart\",\"Bus\"],\"productId\":\"M01\"}]}",
                          HttpConstants.HTTP_STATUS_OK);
    }

    public void testIdentWithTokenPermissionsCheckPart6() {
        // Test MIT Permissions Check (default true); MIT TRUCK Permissions im Token; not authorized;
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlRSVUNLIl19LCJsYW5nMSI6ImRlIiwibGFuZzIiOiJlbiIsImxhbmczIjoiZnIiLCJleHAiOjk5OTk5OTk5OTl9.S41S3ITWv6Cg3MIJtcNQnaI1-5gvXm8Z77d50Z9p_Qw");
        executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"en\",\"country\":\"200\"},\"identCode\":\"D936910\"}",
                          additionalRequestProperties,
                          "{\"code\":4031,\"message\":\"You are not authorized to access this information\",\"missingPermissions\":[\"MB.BUS\"]}",
                          HttpConstants.HTTP_STATUS_FORBIDDEN);
    }

    public void testIdentWithTokenPermissionsCheckPart7() {
        // Test MIT Permissions Check (default true); MIT BUS Permissions im Token; productClassIds: Nur O (Nur Bus)
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIkJVUyJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.S4GpJu5BdWeUIYlgLPr1f96LEj68k_fJsgftRsiPws4");
        executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"en\",\"country\":\"200\"},\"identCode\":\"D936910\"}",
                          additionalRequestProperties,
                          "{\"identContexts\":[{\"aggTypeId\":\"M\",\"modelDesc\":\"OM 936\",\"modelId\":\"D936910\",\"modelRemarks\":\"DIESELMOTOR, MIT AUFLADUNG, LADELUFTKUEHLUNG\",\"modelTypeId\":\"D936\",\"productClassIds\":[\"O\"],\"productClassNames\":[\"Bus\"],\"productId\":\"M01\"}]}",
                          HttpConstants.HTTP_STATUS_OK);
    }

    public void testIdentWithoutCheckModelVisibility() {
        // Test ohne Berücksichtigung der Sichtbarkeit von Baumustern: Erwartetes Ergebnis: befülltes identContexts-Objekt
        executeTestWithBooleanConfigChanges(iPartsPlugin.getPluginConfig(), iPartsPlugin.CONFIG_CHECK_MODEL_VISIBILITY,
                                            false, () -> {
                    Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");
                    executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                      "{\"user\":{\"userId\":\"user\",\"language\":\"en\",\"country\":\"200\"},\"identCode\":\"C205464\"}",
                                      additionalRequestProperties,
                                      "{\"identContexts\":[{\"aggTypeId\":\"F\",\"modelId\":\"C205464\",\"modelTypeId\":\"C205\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"C42\"}]}",
                                      HttpConstants.HTTP_STATUS_OK);
                });
    }

    public void testIdentWithCheckModelVisibility() {
        // Test mit Berücksichtigung der Sichtbarkeit von Baumustern: Erwartetes Ergebnis: Leeres Array
        // Die Admin-Option ist per default true und muss hier nicht erneut gesetzt werden.
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIkJVUyJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.S4GpJu5BdWeUIYlgLPr1f96LEj68k_fJsgftRsiPws4");
        executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"en\",\"country\":\"200\"},\"identCode\":\"C205464\"}",
                          additionalRequestProperties,
                          "{\"identContexts\":[]}",
                          HttpConstants.HTTP_STATUS_OK);
    }

    public void testFilterTruckIdentWithSpikes() {
        iPartsDataResponseSpike spikeValid = new iPartsDataResponseSpike(getProject(), new iPartsResponseSpikeId("", "", "", "", "*0069996", "15201851", "ELDAS_14Y_01_098_001", "", false));
        iPartsDataResponseSpike spikeNotValid = new iPartsDataResponseSpike(getProject(), new iPartsResponseSpikeId("", "", "", "", "*0069996", "15201900", "ELDAS_14Y_01_098_001", "", false));
        try {
            // Position wird ohne Ausreißer ausgefiltert -> Normalzustand
            executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}," +
                              "\"partContext\":{\"moduleId\":\"14Y_01_015_00001\",\"sequenceId\":\"00032\"}," +
                              "\"identContext\":{\"aggTypeId\":\"M\",\"productClassIds\":[\"P\"],\"modelId\":\"D457943\",\"productId\":\"14Y\",\"aggregateNumber\":\"45794315201851\",\"filterOptions\":{\"serial\":true}}}",
                              "{\"code\":4001,\"message\":\"Path 'partContext': SequenceId '00032' is invalid in module '14Y_01_015_00001' for current context and filter options\"}",
                              HttpConstants.HTTP_STATUS_BAD_REQUEST);
            // Beide Ausreißer anlegen und speichern. Einer ist gültig für den Ausreißer der Datenkarte (15201851), der andere nicht (15201900)
            if (spikeValid.existsInDB()) {
                spikeValid.deleteFromDB();
            }
            spikeValid.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            spikeValid.setFieldValue(FIELD_DRS_SOURCE, iPartsImportDataOrigin.ELDAS.getOrigin(), DBActionOrigin.FROM_EDIT);
            spikeValid.setFieldValue(FIELD_DRS_STATUS, iPartsDataReleaseState.RELEASED.getDbValue(), DBActionOrigin.FROM_EDIT);
            spikeValid.saveToDB();

            if (spikeNotValid.existsInDB()) {
                spikeNotValid.deleteFromDB();
            }
            spikeNotValid.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            spikeNotValid.setFieldValue(FIELD_DRS_SOURCE, iPartsImportDataOrigin.ELDAS.getOrigin(), DBActionOrigin.FROM_EDIT);
            spikeNotValid.setFieldValue(FIELD_DRS_STATUS, iPartsDataReleaseState.RELEASED.getDbValue(), DBActionOrigin.FROM_EDIT);
            spikeNotValid.saveToDB();

            // Caches löschen
            iPartsResponseSpikes.clearCache();
            EtkDataAssembly.clearGlobalEntriesCache();
            clearWebservicePluginsCaches();

            // Test durchführen. Durch den gültigen Ausreißer müsste die oben getestete Position jetzt gültig sein
            executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}," +
                              "\"partContext\":{\"moduleId\":\"14Y_01_015_00001\",\"sequenceId\":\"00032\"}," +
                              "\"identContext\":{\"aggTypeId\":\"M\",\"productClassIds\":[\"P\"],\"modelId\":\"D457943\",\"productId\":\"14Y\",\"aggregateNumber\":\"45794315201851\",\"filterOptions\":{\"serial\":true}}}",
                              "{\"partInfo\":{\"plantInformation\":[{\"aggTypeId\":\"M\",\"exceptionIdents\":[\"15201851\",\"15201900\"],\"ident\":\"*0069996\",\"type\":\"bis\"}],\"saaValidityDetails\":[{\"code\":\"Z 522.000\",\"description\":\"SV ZYLINDERKURBELGEHAEUSE\",\"saaCodes\":[{\"code\":\"Z 522.000/02\",\"description\":\"SV ZYLINDERKURBELGEHAEUSE\"},{\"code\":\"Z 522.000/07\",\"description\":\"SV ZYLINDERKURBELGEHAEUSE\"}]}]}}",
                              HttpConstants.HTTP_STATUS_OK);

            // Gültigen Ausreißer löschen -> Position ist nicht mehr gültig, obwohl ein Ausreißer existiert, der aber nicht gültig ist
            spikeValid.deleteFromDB();
            // Caches löschen
            iPartsResponseSpikes.clearCache();
            EtkDataAssembly.clearGlobalEntriesCache();
            clearWebservicePluginsCaches();
            executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}," +
                              "\"partContext\":{\"moduleId\":\"14Y_01_015_00001\",\"sequenceId\":\"00032\"}," +
                              "\"identContext\":{\"aggTypeId\":\"M\",\"productClassIds\":[\"P\"],\"modelId\":\"D457943\",\"productId\":\"14Y\",\"aggregateNumber\":\"45794315201851\",\"filterOptions\":{\"serial\":true}}}",
                              "{\"code\":4001,\"message\":\"Path 'partContext': SequenceId '00032' is invalid in module '14Y_01_015_00001' for current context and filter options\"}",
                              HttpConstants.HTTP_STATUS_BAD_REQUEST);
        } finally {
            // Beide Ausreißer löschen und Caches leeren
            spikeValid.deleteFromDB();
            spikeNotValid.deleteFromDB();
            iPartsResponseSpikes.clearCache();
            EtkDataAssembly.clearGlobalEntriesCache();
            clearWebservicePluginsCaches();
        }
    }

    // DAIMLER-14535
    // DAIMLER-14785 (inkl. saeClass)
    public void testIdentMBSpecWithModelPrefix() {
        // Motoröl
        iPartsDataModelOil modelOil = createModelOilDataObject("D", "999.9", "5W-30, 10W-40", "IPARTS.WSW1656505905165", iPartsSpecType.ENGINE_OIL);
        iPartsDataModelOilQuantity modelOilQuantity = createModelOilQuantityDataObject("D", "9,9", iPartsSpecType.ENGINE_OIL);

        try {
            // Datensatz in DA_MODEL_OIL und DA_MODEL_OIL_QUANTITY hinzufügen
            modelOil.saveToDB();
            modelOilQuantity.saveToDB();
            EtkDataAssembly.clearGlobalEntriesCache();
            iPartsModel.clearCache();
            iPartsDataCardRetrievalHelper.clearCache();
            clearWebservicePluginsCaches();

            // Nun wird mbSpecs gefüllt
            Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");
            executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"WDC2539931F684419\"}",
                              additionalRequestProperties,
                              "{\"identContexts\":[{\"aggregates\":[{\"aggregateNumber\":\"785020300001930\",\"aggSubType\":\"fuelCell1\",\"aggTypeId\":\"N\",\"modelDesc\":\".\",\"modelId\":\"D785020\",\"modelTypeId\":\"D785\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"BRE123\"}],\"aggTypeId\":\"F\",\"connectWireHarnessEnabled\":true,\"datacardExists\":true,\"fin\":\"WDC2539931F684419\",\"integratedNavigationAvailable\":true,\"mbOilSpecs\":[{\"quantity\":\"9,9\",\"saeClass\":\"5W-30, 10W-40\",\"spec\":\"999.9\",\"text\":\"Siehe SI18.00-P-1311A; Einschränkung: Nur SAE 0W-40 oder 5W-40 verwenden!\",\"type\":\"ENGINE_OIL\"}],\"mbSpecs\":[{\"quantity\":\"9,9\",\"saeClass\":\"5W-30, 10W-40\",\"spec\":\"999.9\",\"text\":\"Siehe SI18.00-P-1311A; Einschränkung: Nur SAE 0W-40 oder 5W-40 verwenden!\",\"type\":\"ENGINE_OIL\"}],\"modelDesc\":\"Test\",\"modelId\":\"C253993\",\"modelTypeId\":\"C253\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"C253_FC\"}]}",
                              HttpConstants.HTTP_STATUS_OK);

        } finally {
            // Aufräumen
            modelOil.deleteFromDB();
            modelOilQuantity.deleteFromDB();
            clearWebservicePluginsCaches();
            EtkDataAssembly.clearGlobalEntriesCache();
            iPartsModel.clearCache();
            iPartsDataCardRetrievalHelper.clearCache();
            clearWebservicePluginsCaches();
        }
    }

    // DAIMLER-14714
    public void testIdentMBSpecForBreakFluid() {
        // Bremsflüssigkeit
        iPartsDataModelOil modelOilBreakFluid = createModelOilDataObject("C", "999.9", "5W-30, 10W-40", "DICT.0EF0BDE0C4F142E4BB7E494E613935E8", iPartsSpecType.BREAK_FLUID);
        iPartsDataModelOilQuantity modelOilQuantityBreakFluid = createModelOilQuantityDataObject("C", "9,9", iPartsSpecType.BREAK_FLUID);

        try {
            // Datensatz in DA_MODEL_OIL und DA_MODEL_OIL_QUANTITY hinzufügen
            modelOilBreakFluid.saveToDB();
            modelOilQuantityBreakFluid.saveToDB();
            EtkDataAssembly.clearGlobalEntriesCache();
            iPartsModel.clearCache();
            iPartsDataCardRetrievalHelper.clearCache();
            clearWebservicePluginsCaches();

            // Nun wird mbSpecs gefüllt
            Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");
            executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"WDC2539931F684419\"}",
                              additionalRequestProperties,
                              "{\"identContexts\":[{\"aggregates\":[{\"aggregateNumber\":\"785020300001930\",\"aggSubType\":\"fuelCell1\",\"aggTypeId\":\"N\",\"modelDesc\":\".\",\"modelId\":\"D785020\",\"modelTypeId\":\"D785\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"BRE123\"}],\"aggTypeId\":\"F\",\"connectWireHarnessEnabled\":true,\"datacardExists\":true,\"fin\":\"WDC2539931F684419\",\"integratedNavigationAvailable\":true,\"mbOilSpecs\":[{\"quantity\":\"9,9\",\"saeClass\":\"5W-30, 10W-40\",\"spec\":\"999.9\",\"text\":\"ALTES TEIL DARF AN DIESER STELLE NICHT MEHR EINGEBAUT WERDEN\",\"type\":\"BREAK_FLUID\"}],\"mbSpecs\":[{\"quantity\":\"9,9\",\"saeClass\":\"5W-30, 10W-40\",\"spec\":\"999.9\",\"text\":\"ALTES TEIL DARF AN DIESER STELLE NICHT MEHR EINGEBAUT WERDEN\",\"type\":\"BREAK_FLUID\"}],\"modelDesc\":\"Test\",\"modelId\":\"C253993\",\"modelTypeId\":\"C253\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"C253_FC\"}]}",
                              HttpConstants.HTTP_STATUS_OK);

        } finally {
            // Aufräumen
            modelOilBreakFluid.deleteFromDB();
            modelOilQuantityBreakFluid.deleteFromDB();
            clearWebservicePluginsCaches();
            EtkDataAssembly.clearGlobalEntriesCache();
            iPartsModel.clearCache();
            iPartsDataCardRetrievalHelper.clearCache();
            clearWebservicePluginsCaches();
        }
    }

    // DAIMLER-14715
    public void testIdentMBSpecForCoolant() {
        // Kühlmittel
        iPartsDataModelOil modelOilCoolant = createModelOilDataObject("D651980", "999.9", "5W-30, 10W-40", "", iPartsSpecType.COOLANT);
        iPartsDataModelOilQuantity modelOilQuantityCoolant = createModelOilQuantityDataObject("D651980", "9,9", iPartsSpecType.COOLANT);

        try {
            // Datensatz in DA_MODEL_OIL und DA_MODEL_OIL_QUANTITY hinzufügen
            modelOilCoolant.saveToDB();
            modelOilQuantityCoolant.saveToDB();
            EtkDataAssembly.clearGlobalEntriesCache();
            iPartsModel.clearCache();
            iPartsDataCardRetrievalHelper.clearCache();
            clearWebservicePluginsCaches();

            // Nun wird mbSpecs gefüllt
            Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");
            executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"WDD1724032F086455\"}",
                              additionalRequestProperties,
                              "{\"identContexts\":[{\"aggregates\":[{\"aggregateNumber\":\"65198032011335\",\"aggTypeId\":\"M\",\"modelId\":\"D651980\",\"modelTypeId\":\"D651\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"69L\"}],\"aggTypeId\":\"F\",\"datacardExists\":true,\"fin\":\"WDD1724032F086455\",\"integratedNavigationAvailable\":true,\"mbOilSpecs\":[{\"spec\":\"222.2\",\"text\":\"Siehe SI18.00-P-1311A; Einschränkung: Nur SAE 0W-30 oder SAE 5W-30 verwenden!\",\"type\":\"ENGINE_OIL\"},{\"quantity\":\"9,9\",\"saeClass\":\"5W-30, 10W-40\",\"spec\":\"999.9\",\"type\":\"COOLANT\"}],\"mbSpecs\":[{\"spec\":\"222.2\",\"text\":\"Siehe SI18.00-P-1311A; Einschränkung: Nur SAE 0W-30 oder SAE 5W-30 verwenden!\",\"type\":\"ENGINE_OIL\"},{\"quantity\":\"9,9\",\"saeClass\":\"5W-30, 10W-40\",\"spec\":\"999.9\",\"type\":\"COOLANT\"}],\"modelId\":\"C172403\",\"modelTypeId\":\"C172\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"D43\"}]}",
                              HttpConstants.HTTP_STATUS_OK);

        } finally {
            // Aufräumen
            modelOilCoolant.deleteFromDB();
            modelOilQuantityCoolant.deleteFromDB();
            clearWebservicePluginsCaches();
            EtkDataAssembly.clearGlobalEntriesCache();
            iPartsModel.clearCache();
            iPartsDataCardRetrievalHelper.clearCache();
            clearWebservicePluginsCaches();
        }
    }

    // DAIMLER-15304
    public void testIdentMBSpecForRefrigerantAndRefrigeratorOil() {
        // Kältemittel
        iPartsDataModelOil modelOilRefrigerant = createModelOilDataObject("C", "361.1", "5W-30, 10W-40", "DICT.0EF0BDE0C4F142E4BB7E494E613935E8", iPartsSpecType.REFRIGERANT);
        iPartsDataModelOilQuantity modelOilQuantityRefrigerant = createModelOilQuantityDataObject("C", "9,9", iPartsSpecType.REFRIGERANT);

        // Kompressoröl
        iPartsDataModelOil modelOilRefrigeratorOil = createModelOilDataObject("C", "361.1", "5W-30, 10W-40", "DICT.0EF0BDE0C4F142E4BB7E494E613935E8", iPartsSpecType.REFRIGERATOR_OIL);
        iPartsDataModelOilQuantity modelOilQuantityRefrigeratorOil = createModelOilQuantityDataObject("C", "9,9", iPartsSpecType.REFRIGERATOR_OIL);

        try {
            // Datensatz in DA_MODEL_OIL und DA_MODEL_OIL_QUANTITY hinzufügen
            modelOilRefrigerant.saveToDB();
            modelOilQuantityRefrigerant.saveToDB();
            modelOilRefrigeratorOil.saveToDB();
            modelOilQuantityRefrigeratorOil.saveToDB();
            EtkDataAssembly.clearGlobalEntriesCache();
            iPartsModel.clearCache();
            iPartsDataCardRetrievalHelper.clearCache();
            clearWebservicePluginsCaches();

            // Nun wird mbSpecs gefüllt
            Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");
            executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"WDC2539931F684419\"}",
                              additionalRequestProperties,
                              "{\"identContexts\":[{\"aggregates\":[{\"aggregateNumber\":\"785020300001930\",\"aggSubType\":\"fuelCell1\",\"aggTypeId\":\"N\",\"modelDesc\":\".\",\"modelId\":\"D785020\",\"modelTypeId\":\"D785\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"BRE123\"}],\"aggTypeId\":\"F\",\"connectWireHarnessEnabled\":true,\"datacardExists\":true,\"fin\":\"WDC2539931F684419\",\"integratedNavigationAvailable\":true,\"mbOilSpecs\":[{\"quantity\":\"9,9\",\"saeClass\":\"5W-30, 10W-40\",\"spec\":\"361.1\",\"text\":\"ALTES TEIL DARF AN DIESER STELLE NICHT MEHR EINGEBAUT WERDEN\",\"type\":\"REFRIGERANT\"},{\"quantity\":\"9,9\",\"saeClass\":\"5W-30, 10W-40\",\"spec\":\"361.1\",\"text\":\"ALTES TEIL DARF AN DIESER STELLE NICHT MEHR EINGEBAUT WERDEN\",\"type\":\"REFRIGERATOR_OIL\"}],\"mbSpecs\":[{\"quantity\":\"9,9\",\"saeClass\":\"5W-30, 10W-40\",\"spec\":\"361.1\",\"text\":\"ALTES TEIL DARF AN DIESER STELLE NICHT MEHR EINGEBAUT WERDEN\",\"type\":\"REFRIGERANT\"},{\"quantity\":\"9,9\",\"saeClass\":\"5W-30, 10W-40\",\"spec\":\"361.1\",\"text\":\"ALTES TEIL DARF AN DIESER STELLE NICHT MEHR EINGEBAUT WERDEN\",\"type\":\"REFRIGERATOR_OIL\"}],\"modelDesc\":\"Test\",\"modelId\":\"C253993\",\"modelTypeId\":\"C253\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"C253_FC\"}]}",
                              HttpConstants.HTTP_STATUS_OK);

        } finally {
            // Aufräumen
            modelOilRefrigerant.deleteFromDB();
            modelOilQuantityRefrigerant.deleteFromDB();
            modelOilRefrigeratorOil.deleteFromDB();
            modelOilQuantityRefrigeratorOil.deleteFromDB();
            clearWebservicePluginsCaches();
            EtkDataAssembly.clearGlobalEntriesCache();
            iPartsModel.clearCache();
            iPartsDataCardRetrievalHelper.clearCache();
            clearWebservicePluginsCaches();
        }
    }

    // DAIMLER-15552
    public void testIdentMBSpecForGearOils() {
        // Vorderachsengetriebeöl
        iPartsDataModelOil modelOilFrontAxleGearOil = createModelOilDataObject("C253", "236.9", "5W-30, 10W-40", "DICT.0EF0BDE0C4F142E4BB7E494E613935E8", iPartsSpecType.FRONT_AXLE_GEAR_OIL);
        iPartsDataModelOilQuantity modelOilQuantityFrontAxleGearOil = createModelOilQuantityDataObject("D785020", "9,9", iPartsSpecType.FRONT_AXLE_GEAR_OIL);

        // Hinterachsengetriebeöl
        iPartsDataModelOil modelOilRearAxleGearOil = createModelOilDataObject("C253", "236.9", "5W-30, 10W-40", "DICT.0EF0BDE0C4F142E4BB7E494E613935E8", iPartsSpecType.REAR_AXLE_GEAR_OIL);
        iPartsDataModelOilQuantity modelOilQuantityRearAxleGearOil = createModelOilQuantityDataObject("D785020", "9,9", iPartsSpecType.REAR_AXLE_GEAR_OIL);

        // Verteilergetriebeöl
        iPartsDataModelOil modelOilTransferCaseGearOil = createModelOilDataObject("C253", "236.9", "5W-30, 10W-40", "DICT.0EF0BDE0C4F142E4BB7E494E613935E8", iPartsSpecType.TRANSFERCASE_GEAR_OIL);
        iPartsDataModelOilQuantity modelOilQuantityTransferCaseGearOil = createModelOilQuantityDataObject("D785020", "9,9", iPartsSpecType.TRANSFERCASE_GEAR_OIL);

        // Getriebeöl
        iPartsDataModelOil modelOilGearOil = createModelOilDataObject("D785020", "236.9", "5W-30, 10W-40", "DICT.0EF0BDE0C4F142E4BB7E494E613935E8", iPartsSpecType.GEAR_OIL);
        iPartsDataModelOilQuantity modelOilQuantityGearOil = createModelOilQuantityDataObject("D785020", "9,9", iPartsSpecType.GEAR_OIL);

        try {
            // Datensatz in DA_MODEL_OIL und DA_MODEL_OIL_QUANTITY hinzufügen
            modelOilFrontAxleGearOil.saveToDB();
            modelOilQuantityFrontAxleGearOil.saveToDB();
            modelOilRearAxleGearOil.saveToDB();
            modelOilQuantityRearAxleGearOil.saveToDB();
            modelOilTransferCaseGearOil.saveToDB();
            modelOilQuantityTransferCaseGearOil.saveToDB();
            modelOilGearOil.saveToDB();
            modelOilQuantityGearOil.saveToDB();
            EtkDataAssembly.clearGlobalEntriesCache();
            iPartsModel.clearCache();
            iPartsDataCardRetrievalHelper.clearCache();
            clearWebservicePluginsCaches();

            // Nun wird mbSpecs gefüllt
            Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");
            executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"identCode\":\"WDC2539931F684419\"}",
                              additionalRequestProperties,
                              "{\"identContexts\":[{\"aggregates\":[{\"aggregateNumber\":\"785020300001930\",\"aggSubType\":\"fuelCell1\",\"aggTypeId\":\"N\",\"modelDesc\":\".\",\"modelId\":\"D785020\",\"modelTypeId\":\"D785\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"BRE123\"}],\"aggTypeId\":\"F\",\"connectWireHarnessEnabled\":true,\"datacardExists\":true,\"fin\":\"WDC2539931F684419\",\"integratedNavigationAvailable\":true,\"mbOilSpecs\":[{\"saeClass\":\"5W-30, 10W-40\",\"spec\":\"236.9\",\"text\":\"ALTES TEIL DARF AN DIESER STELLE NICHT MEHR EINGEBAUT WERDEN\",\"type\":\"FRONT_AXLE_GEAR_OIL\"},{\"saeClass\":\"5W-30, 10W-40\",\"spec\":\"236.9\",\"text\":\"ALTES TEIL DARF AN DIESER STELLE NICHT MEHR EINGEBAUT WERDEN\",\"type\":\"REAR_AXLE_GEAR_OIL\"},{\"saeClass\":\"5W-30, 10W-40\",\"spec\":\"236.9\",\"text\":\"ALTES TEIL DARF AN DIESER STELLE NICHT MEHR EINGEBAUT WERDEN\",\"type\":\"TRANSFERCASE_GEAR_OIL\"},{\"quantity\":\"9,9\",\"saeClass\":\"5W-30, 10W-40\",\"spec\":\"236.9\",\"text\":\"ALTES TEIL DARF AN DIESER STELLE NICHT MEHR EINGEBAUT WERDEN\",\"type\":\"GEAR_OIL\"}],\"mbSpecs\":[{\"saeClass\":\"5W-30, 10W-40\",\"spec\":\"236.9\",\"text\":\"ALTES TEIL DARF AN DIESER STELLE NICHT MEHR EINGEBAUT WERDEN\",\"type\":\"FRONT_AXLE_GEAR_OIL\"},{\"saeClass\":\"5W-30, 10W-40\",\"spec\":\"236.9\",\"text\":\"ALTES TEIL DARF AN DIESER STELLE NICHT MEHR EINGEBAUT WERDEN\",\"type\":\"REAR_AXLE_GEAR_OIL\"},{\"saeClass\":\"5W-30, 10W-40\",\"spec\":\"236.9\",\"text\":\"ALTES TEIL DARF AN DIESER STELLE NICHT MEHR EINGEBAUT WERDEN\",\"type\":\"TRANSFERCASE_GEAR_OIL\"},{\"quantity\":\"9,9\",\"saeClass\":\"5W-30, 10W-40\",\"spec\":\"236.9\",\"text\":\"ALTES TEIL DARF AN DIESER STELLE NICHT MEHR EINGEBAUT WERDEN\",\"type\":\"GEAR_OIL\"}],\"modelDesc\":\"Test\",\"modelId\":\"C253993\",\"modelTypeId\":\"C253\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"C253_FC\"}]}",
                              HttpConstants.HTTP_STATUS_OK);

        } finally {
            // Aufräumen
            modelOilFrontAxleGearOil.deleteFromDB();
            modelOilQuantityFrontAxleGearOil.deleteFromDB();
            modelOilRearAxleGearOil.deleteFromDB();
            modelOilQuantityRearAxleGearOil.deleteFromDB();
            modelOilTransferCaseGearOil.deleteFromDB();
            modelOilQuantityTransferCaseGearOil.deleteFromDB();
            modelOilGearOil.deleteFromDB();
            modelOilQuantityGearOil.deleteFromDB();
            clearWebservicePluginsCaches();
            EtkDataAssembly.clearGlobalEntriesCache();
            iPartsModel.clearCache();
            iPartsDataCardRetrievalHelper.clearCache();
            clearWebservicePluginsCaches();
        }
    }


    private iPartsDataModelOil createModelOilDataObject(String modelNo, String specValidity, String saeClass, String textId, iPartsSpecType specType) {
        iPartsDataModelOil iPartsDataModelOil = null;
        if (StrUtils.isValid(modelNo) && StrUtils.isValid(specValidity) && (saeClass != null) && (textId != null) && (specType != null)) {
            iPartsModelOilId iPartsModelOilId = new iPartsModelOilId(modelNo, specValidity, specType.getDbValue());
            iPartsDataModelOil = new iPartsDataModelOil(getProject(), iPartsModelOilId);
            iPartsDataModelOil.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            // Inhaltlich wenig sinnvoll, aber zeigt, dass die SAE Klasse auch für Kältemittel funktioniert
            iPartsDataModelOil.setFieldValue(FIELD_DMO_SAE_CLASS, saeClass, DBActionOrigin.FROM_EDIT);
            iPartsDataModelOil.setFieldValue(FIELD_DMO_TEXT_ID, textId, DBActionOrigin.FROM_EDIT);
        }
        return iPartsDataModelOil;
    }

    private iPartsDataModelOilQuantity createModelOilQuantityDataObject(String modelNo, String quantity, iPartsSpecType specType) {
        iPartsDataModelOilQuantity iPartsDataModelOilQuantity = null;
        if (StrUtils.isValid(modelNo) && StrUtils.isValid(quantity) && (specType != null)) {
            iPartsModelOilQuantityId iPartsModelOilQuantityId = new iPartsModelOilQuantityId(modelNo, "", specType.getDbValue(), "", "");
            iPartsDataModelOilQuantity = new iPartsDataModelOilQuantity(getProject(), iPartsModelOilQuantityId);
            iPartsDataModelOilQuantity.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            iPartsDataModelOilQuantity.setFieldValue(FIELD_DMOQ_QUANTITY, "9,9", DBActionOrigin.FROM_EDIT);
        }
        return iPartsDataModelOilQuantity;
    }

    // Integrierte Gesamtnavigation
    // DAIMLER-13158
    public void testIdentIntegratedNavigationAvailable() {
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");
        executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"en\",\"country\":\"200\"},\"identCode\":\"C463248\"}",
                          additionalRequestProperties,
                          "{\"identContexts\":[{\"aggregates\":[{\"aggTypeId\":\"FH\",\"modelDesc\":\"G 500, G 55 AMG\",\"modelId\":\"D463832\",\"modelRemarks\":\"AUFBAU,STATION-WAGEN LANG FUENFTUERIG,RADSTAND 2850 MM\",\"modelTypeId\":\"D463\",\"productClassIds\":[\"G\",\"U\"],\"productClassNames\":[\"Geländewagen\",\"Unimog\"],\"productId\":\"37P\",\"productRemarks\":\"BIS CAB 144225\"}],\"aggTypeId\":\"F\",\"integratedNavigationAvailable\":true,\"modelId\":\"C463248\",\"modelTypeId\":\"C463\",\"productClassIds\":[\"G\"],\"productClassNames\":[\"Geländewagen\"],\"productId\":\"62U\",\"productRemarks\":\"Ab Ident-Nr.: 144226\"}]}",
                          HttpConstants.HTTP_STATUS_OK);
    }

    // Integrierte Gesamtnavigation für GetNavOpts, GetParts und GetPartInfo
    // DAIMLER-13159
    public void testIntegratedNavigationPart1() {
        // GetNavOpts; group; integratedNavigation = true;
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"},\"identContext\": {\"aggregates\":[{\"aggTypeId\":\"M\",\"modelId\":\"D651921\",\"modelRemarks\":\"AUTOMATISCHES GETRIEBE,ALLISON MIT RETARDER\",\"modelTypeId\":\"D651\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"69L\",\"typeVersion\":\"M22\"}],\"aggTypeId\":\"F\",\"integratedNavigationAvailable\": true,\"modelId\":\"C205002\",\"modelTypeId\":\"C205\",\"notes\":[{\"text\":\"Notiz zum Produkt C01\\r\\n2te Zeile\"}],\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"C01\",\"typeVersion\":\"FW\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"01\"}],\"integratedNavigation\": true}",
                          "{\"nextNodes\":[{\"id\":\"010\",\"label\":\"MOTOR\",\"modules\":[{\"id\":\"69L_01_010_00001\",\"label\":\"MOTOR\",\"modelId\":\"D651921\",\"partsAvailable\":true,\"thumbNails\":[{\"href\":\"/parts/media/drawing_B01010000130\",\"id\":\"drawing_B01010000130\",\"previewHref\":\"/parts/media/previews/drawing_B01010000130\"}],\"type\":\"module\"},{\"id\":\"D6519_1_10\",\"label\":\"MOTOR\",\"modelId\":\"D651921\",\"partsAvailable\":true,\"thumbNails\":[{\"href\":\"/parts/media/drawing_B01010000130.tif\",\"id\":\"drawing_B01010000130.tif\",\"previewHref\":\"/parts/media/previews/drawing_B01010000130.tif\"}],\"type\":\"module\"}],\"type\":\"cg_subgroup\"},{\"id\":\"015\",\"label\":\"ZYLINDERKURBELGEHAEUSE,DECKEL UND DICHTUNGSSATZ\",\"modules\":[{\"id\":\"69L_01_015_00001\",\"label\":\"ZYLINDERKURBELGEHAEUSE,DECKEL UND DICHTUNGSSATZ\",\"modelId\":\"D651921\",\"partsAvailable\":true,\"type\":\"module\"},{\"id\":\"D6519_1_15\",\"label\":\"KURBELGEHAEUSE,DECKEL UND DICHTUNGSSATZ\",\"modelId\":\"D651921\",\"partsAvailable\":true,\"thumbNails\":[{\"href\":\"/parts/media/drawing_B01015000377.tif\",\"id\":\"drawing_B01015000377.tif\",\"previewHref\":\"/parts/media/previews/drawing_B01015000377.tif\"}],\"type\":\"module\"}],\"type\":\"cg_subgroup\"},{\"id\":\"045\",\"label\":\"OELWANNE UND OELSTANDSANZEIGE\",\"modules\":[{\"id\":\"69L_01_045_00001\",\"label\":\"OELWANNE UND OELSTANDSANZEIGE\",\"modelId\":\"D651921\",\"partsAvailable\":true,\"type\":\"module\"},{\"id\":\"D6519_1_45\",\"label\":\"OELWANNE UND OELSTANDSANZEIGE\",\"modelId\":\"D651921\",\"partsAvailable\":true,\"thumbNails\":[{\"href\":\"/parts/media/drawing_B01045000299.tif\",\"id\":\"drawing_B01045000299.tif\",\"previewHref\":\"/parts/media/previews/drawing_B01045000299.tif\"}],\"type\":\"module\"}],\"type\":\"cg_subgroup\"},{\"id\":\"050\",\"label\":\"OELWANNE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"},{\"id\":\"060\",\"label\":\"ZYLINDERKOPF\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"},{\"id\":\"065\",\"label\":\"ZYLINDERKOPF UND DICHTUNGSSATZ\",\"modules\":[{\"id\":\"69L_01_065_00001\",\"label\":\"ZYLINDERKOPF UND DICHTUNGSSATZ\",\"modelId\":\"D651921\",\"partsAvailable\":true,\"type\":\"module\"},{\"id\":\"D6519_1_65\",\"label\":\"ZYLINDERKOPF UND DICHTUNGSSATZ\",\"modelId\":\"D651921\",\"partsAvailable\":true,\"thumbNails\":[{\"href\":\"/parts/media/drawing_B01065000100.tif\",\"id\":\"drawing_B01065000100.tif\",\"previewHref\":\"/parts/media/previews/drawing_B01065000100.tif\"}],\"type\":\"module\"}],\"type\":\"cg_subgroup\"},{\"id\":\"075\",\"label\":\"ZYLINDERKOPFHAUBE\",\"modules\":[{\"id\":\"69L_01_075_00001\",\"label\":\"ZYLINDERKOPFHAUBE\",\"modelId\":\"D651921\",\"partsAvailable\":true,\"type\":\"module\"},{\"id\":\"D6519_1_75\",\"label\":\"ZYLINDERKOPFHAUBE\",\"modelId\":\"D651921\",\"partsAvailable\":true,\"thumbNails\":[{\"href\":\"/parts/media/drawing_B01075000424.tif\",\"id\":\"drawing_B01075000424.tif\",\"previewHref\":\"/parts/media/previews/drawing_B01075000424.tif\"}],\"type\":\"module\"}],\"type\":\"cg_subgroup\"}]}");
    }

    public void testIntegratedNavigationPart2() {
        // GetNavOpts; group; integratedNavigation = false;
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"},\"identContext\": {\"aggregates\":[{\"aggTypeId\":\"M\",\"modelId\":\"D651921\",\"modelRemarks\":\"AUTOMATISCHES GETRIEBE,ALLISON MIT RETARDER\",\"modelTypeId\":\"D651\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"69L\",\"typeVersion\":\"M22\"}],\"aggTypeId\":\"F\",\"integratedNavigationAvailable\": true,\"modelId\":\"C205002\",\"modelTypeId\":\"C205\",\"notes\":[{\"text\":\"Notiz zum Produkt C01\\r\\n2te Zeile\"}],\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"C01\",\"typeVersion\":\"FW\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"01\"},{\"type\":\"cg_subgroup\",\"id\":\"045\"}],\"integratedNavigation\": false}",
                          "{\"nextNodes\":[]}");
    }

    public void testIntegratedNavigationPart3() {
        // GetNavOpts; group + subgroup; integratedNavigation = true;
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"},\"identContext\": {\"aggregates\":[{\"aggTypeId\":\"M\",\"modelId\":\"D651921\",\"modelRemarks\":\"AUTOMATISCHES GETRIEBE,ALLISON MIT RETARDER\",\"modelTypeId\":\"D651\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"69L\",\"typeVersion\":\"M22\"}],\"aggTypeId\":\"F\",\"integratedNavigationAvailable\": true,\"modelId\":\"C205002\",\"modelTypeId\":\"C205\",\"notes\":[{\"text\":\"Notiz zum Produkt C01\\r\\n2te Zeile\"}],\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"C01\",\"typeVersion\":\"FW\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"01\"},{\"type\":\"cg_subgroup\",\"id\":\"045\"}],\"integratedNavigation\": true}",
                          "{\"nextNodes\":[{\"id\":\"69L_01_045_00001\",\"label\":\"OELWANNE UND OELSTANDSANZEIGE\",\"partsAvailable\":true,\"type\":\"module\"},{\"id\":\"D6519_1_45\",\"label\":\"OELWANNE UND OELSTANDSANZEIGE\",\"partsAvailable\":true,\"thumbNails\":[{\"href\":\"/parts/media/drawing_B01045000299.tif\",\"id\":\"drawing_B01045000299.tif\",\"previewHref\":\"/parts/media/previews/drawing_B01045000299.tif\"}],\"type\":\"module\"}]}");
    }

    public void testIntegratedNavigationPart4() {
        // GetNavOpts; group + subgroup; integratedNavigation = false;
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"},\"identContext\": {\"aggregates\":[{\"aggTypeId\":\"M\",\"modelId\":\"D651921\",\"modelRemarks\":\"AUTOMATISCHES GETRIEBE,ALLISON MIT RETARDER\",\"modelTypeId\":\"D651\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"69L\",\"typeVersion\":\"M22\"}],\"aggTypeId\":\"F\",\"integratedNavigationAvailable\": true,\"modelId\":\"C205002\",\"modelTypeId\":\"C205\",\"notes\":[{\"text\":\"Notiz zum Produkt C01\\r\\n2te Zeile\"}],\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"C01\",\"typeVersion\":\"FW\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"01\"},{\"type\":\"cg_subgroup\",\"id\":\"045\"}],\"integratedNavigation\": false}",
                          "{\"nextNodes\":[]}");
    }

    public void testIntegratedNavigationPart5() {
        // GetParts; group + subgroup; integratedNavigation = true;
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"},\"identContext\":{\"aggregates\":[{\"aggTypeId\":\"M\",\"modelId\":\"D651921\",\"modelRemarks\":\"AUTOMATISCHES GETRIEBE,ALLISON MIT RETARDER\",\"modelTypeId\":\"D651\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"69L\",\"typeVersion\":\"M22\"}],\"aggTypeId\":\"F\",\"integratedNavigationAvailable\":true,\"modelId\":\"C205002\",\"modelTypeId\":\"C205\",\"notes\":[{\"text\":\"Notiz zum Produkt C01\\r2te Zeile\"}],\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"C01\",\"typeVersion\":\"FW\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"01\"},{\"type\":\"cg_subgroup\",\"id\":\"060\"}],\"integratedNavigation\":true}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartsIntegratedNavigaion.txt"));
    }

    public void testIntegratedNavigationPart6() {
        // GetParts; group + subgroup; integratedNavigation = false;
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"},\"identContext\":{\"aggregates\":[{\"aggTypeId\":\"M\",\"modelId\":\"D651921\",\"modelRemarks\":\"AUTOMATISCHES GETRIEBE,ALLISON MIT RETARDER\",\"modelTypeId\":\"D651\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"69L\",\"typeVersion\":\"M22\"}],\"aggTypeId\":\"F\",\"integratedNavigationAvailable\":true,\"modelId\":\"C205002\",\"modelTypeId\":\"C205\",\"notes\":[{\"text\":\"Notiz zum Produkt C01\\r2te Zeile\"}],\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"C01\",\"typeVersion\":\"FW\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"01\"},{\"type\":\"cg_subgroup\",\"id\":\"060\"}],\"integratedNavigation\":false}",
                          "{\"code\":4001,\"message\":\"Unable to find module for product \\\"C01\\\" and given navContext\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testIntegratedNavigationPart7() {
        // GetPartInfo; eindeutiges Module; mit Aggregaten; integratedNavigation = true;
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"},\"identContext\":{\"aggregates\":[{\"aggTypeId\":\"M\",\"modelId\":\"D651921\",\"modelRemarks\":\"AUTOMATISCHES GETRIEBE,ALLISON MIT RETARDER\",\"modelTypeId\":\"D651\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"69L\",\"typeVersion\":\"M22\"}],\"aggTypeId\":\"F\",\"integratedNavigationAvailable\":true,\"modelId\":\"C205002\",\"modelTypeId\":\"C205\",\"notes\":[{\"text\":\"Notiz zum Produkt C01\\r\\n2te Zeile\"}],\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"C01\",\"typeVersion\":\"FW\"},\"partContext\":{\"moduleId\":\"69L_01_045_00001\",\"sequenceId\":\"00099\"},\"integratedNavigation\":true}",
                          additionalRequestProperties,
                          "{\"partInfo\":{\"codeValidityDetails\":[{\"code\":\"F205\",\"dateFrom\":\"2009-04-15\",\"name\":\"BAUREIHE 205\"},{\"code\":\"F253\",\"dateFrom\":\"2010-06-30\",\"name\":\"BAUREIHE 253\"},{\"code\":\"M005\",\"dateFrom\":\"2004-05-26\",\"name\":\"FAHRZEUGE MIT 4-MATIC-/ALLRAD-ANTRIEB\"}],\"codeValidityMatrix\":[[{\"code\":\"F205\"},{\"code\":\"M005\"}],[{\"code\":\"F253\"}]],\"plantInformation\":[{\"date\":\"2015-10-02\",\"ident\":\"33271866\",\"plant\":\"3\",\"type\":\"bis\"}]}}");
    }

    public void testIntegratedNavigationPart7a() {
        // GetPartInfo; nur Fahrzeugkontext; ohne Aggregate; integratedNavigation = true;
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"},\"identContext\":{\"aggTypeId\":\"F\",\"integratedNavigationAvailable\":true,\"modelId\":\"C205002\",\"modelTypeId\":\"C205\",\"notes\":[{\"text\":\"Notiz zum Produkt C01\\r\\n2te Zeile\"}],\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"C01\",\"typeVersion\":\"FW\"},\"partContext\":{\"moduleId\":\"69L_01_045_00001\",\"sequenceId\":\"00099\"},\"integratedNavigation\":true}",
                          additionalRequestProperties,
                          "{\"partInfo\":{\"codeValidityDetails\":[{\"code\":\"F205\",\"dateFrom\":\"2009-04-15\",\"name\":\"BAUREIHE 205\"},{\"code\":\"F253\",\"dateFrom\":\"2010-06-30\",\"name\":\"BAUREIHE 253\"},{\"code\":\"M005\",\"dateFrom\":\"2004-05-26\",\"name\":\"FAHRZEUGE MIT 4-MATIC-/ALLRAD-ANTRIEB\"}],\"codeValidityMatrix\":[[{\"code\":\"F205\"},{\"code\":\"M005\"}],[{\"code\":\"F253\"}]],\"plantInformation\":[{\"date\":\"2015-10-02\",\"ident\":\"33271866\",\"plant\":\"3\",\"type\":\"bis\"}]}}");
    }

    public void testIntegratedNavigationPart8() {
        // GetPartInfo; eindeutiges Module; integratedNavigation = false;
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"},\"identContext\":{\"aggregates\":[{\"aggTypeId\":\"M\",\"modelId\":\"D651921\",\"modelRemarks\":\"AUTOMATISCHES GETRIEBE,ALLISON MIT RETARDER\",\"modelTypeId\":\"D651\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"69L\",\"typeVersion\":\"M22\"}],\"aggTypeId\":\"F\",\"integratedNavigationAvailable\":true,\"modelId\":\"C205002\",\"modelTypeId\":\"C205\",\"notes\":[{\"text\":\"Notiz zum Produkt C01\\r\\n2te Zeile\"}],\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"C01\",\"typeVersion\":\"FW\"},\"partContext\":{\"moduleId\":\"69L_01_045_00001\",\"sequenceId\":\"00099\"},\"integratedNavigation\":false}",
                          "{\"code\":4001,\"message\":\"Module '69L_01_045_00001' is invalid for product 'C01', current context and filter options\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    // Tests für WebService: [GetModels]
    public void testGetModelsEmpty() {
        executeWebservice(iPartsWSGetModelsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"},\"productClassIds\":[\"P\"],\"aggTypeId\":\"F\",\"productId\":\"GAGA\"}",
                          "{\"models\":[]}");
    }

    public void testGetModelsModelTypeIdModel() {
        // Baumusterprodukt
        executeWebservice(iPartsWSGetModelsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"},\"productClassIds\":[\"P\"],\"aggTypeId\":\"F\",\"modelTypeId\":\"C205\"}",
                          DWFile.get(getTestWorkingDir(), "resultModelsModelTypeId.txt"));
    }

    public void testGetModelsModelTypeIdSeries() {
        // Baureihenprodukt
        executeWebservice(iPartsWSGetModelsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"},\"productClassIds\":[\"P\",\"T\",\"M\"],\"aggTypeId\":\"F\",\"modelTypeId\":\"C204\"}",
                          DWFile.get(getTestWorkingDir(), "resultModelsModelTypeIdSeries.txt"));

    }

    public void testGetModelsProductName() {
        executeWebservice(iPartsWSGetModelsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"},\"productClassIds\":[\"P\"],\"aggTypeId\":\"F\",\"productName\":\"C-Klasse*\"}",
                          DWFile.get(getTestWorkingDir(), "resultModelsProductName.txt"));
    }

    public void testGetModelsProductClassIds() {
        executeWebservice(iPartsWSGetModelsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"},\"productClassIds\":[\"P\"],\"aggTypeId\":\"F\"}",
                          DWFile.get(getTestWorkingDir(), "resultModelsProductClassIds.txt"));
    }

    public void testGetModelsProductIdModel() {
        // Baumusterprodukt
        executeWebservice(iPartsWSGetModelsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"},\"productClassIds\":[\"P\"],\"aggTypeId\":\"F\",\"productId\":\"C21\"}",
                          DWFile.get(getTestWorkingDir(), "resultModelsProductProductId.txt"));
    }

    public void testGetModelsProductIdSeries() {
        // Baureihenprodukt
        executeWebservice(iPartsWSGetModelsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"},\"productClassIds\":[\"P\",\"L\",\"T\"],\"aggTypeId\":\"F\",\"productId\":\"C22\"}",
                          DWFile.get(getTestWorkingDir(), "resultModelsProductId.txt"));
    }

    public void testGetModelsErrorEmpty() {
        executeWebservice(iPartsWSGetModelsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{}",
                          "{\"code\":4000,\"message\":\"Attribute 'user' is missing\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testGetModelsErrorMissingParameter() {
        executeWebservice(iPartsWSGetModelsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"}}",
                          "{\"code\":4000,\"message\":\"Attribute list 'productClassIds' is invalid (missing or empty)\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testGetModelsErrorInvalidCombinations() {
        executeWebservice(iPartsWSGetModelsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"},\"productClassIds\":[\"P\"],\"aggTypeId\":\"F\",\"productId\":\"S10\",\"productName\":\"Test\"}",
                          "{\"code\":4003,\"message\":\"At least one of the attributes [productName, productId] must be empty: [Test, S10]\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }


    // Tests für WebService: [GetProductGroups]
    public void testGetProductGroupsGerman() {
        executeWebservice(iPartsWSGetProductGroupsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"herbert\",\"language\":\"de\",\"country\":\"200\"}}",
                          "{\"productGroups\":[{\"aggregateTypes\":[{\"aggTypeId\":\"E\",\"aggTypeName\":\"Elektromotor\"},{\"aggTypeId\":\"G\",\"aggTypeName\":\"Getriebe\"},{\"aggTypeId\":\"M\",\"aggTypeName\":\"Motor\"},{\"aggTypeId\":\"N\",\"aggTypeName\":\"Brennstoffzelle\"}],\"productGroupId\":\"A\",\"productGroupName\":\"AGGREGATE\"},{\"aggregateTypes\":[{\"aggTypeId\":\"F\",\"aggTypeName\":\"Fahrzeug\"}],\"productGroupId\":\"N\",\"productGroupName\":\"LKW\"},{\"aggregateTypes\":[{\"aggTypeId\":\"B\",\"aggTypeName\":\"Hochvoltbatterie\"},{\"aggTypeId\":\"E\",\"aggTypeName\":\"Elektromotor\"},{\"aggTypeId\":\"F\",\"aggTypeName\":\"Fahrzeug\"},{\"aggTypeId\":\"G\",\"aggTypeName\":\"Getriebe\"},{\"aggTypeId\":\"M\",\"aggTypeName\":\"Motor\"}],\"productGroupId\":\"P\",\"productGroupName\":\"PKW\"}]}");
    }

    public void testGetProductGroupsDefault() {
        executeWebservice(iPartsWSGetProductGroupsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"herbert\",\"country\":\"200\"}}",
                          "{\"productGroups\":[{\"aggregateTypes\":[{\"aggTypeId\":\"E\",\"aggTypeName\":\"Electric motor\"},{\"aggTypeId\":\"G\",\"aggTypeName\":\"Transmission\"},{\"aggTypeId\":\"M\",\"aggTypeName\":\"Engine\"},{\"aggTypeId\":\"N\",\"aggTypeName\":\"Fuel cell\"}],\"productGroupId\":\"A\",\"productGroupName\":\"AGGREGATE\"},{\"aggregateTypes\":[{\"aggTypeId\":\"F\",\"aggTypeName\":\"Vehicle\"}],\"productGroupId\":\"N\",\"productGroupName\":\"LKW\"},{\"aggregateTypes\":[{\"aggTypeId\":\"B\",\"aggTypeName\":\"High-voltage battery\"},{\"aggTypeId\":\"E\",\"aggTypeName\":\"Electric motor\"},{\"aggTypeId\":\"F\",\"aggTypeName\":\"Vehicle\"},{\"aggTypeId\":\"G\",\"aggTypeName\":\"Transmission\"},{\"aggTypeId\":\"M\",\"aggTypeName\":\"Engine\"}],\"productGroupId\":\"P\",\"productGroupName\":\"PKW\"}]}");
    }

    public void testGetProductGroupsErrorMalformedJSON() {
        executeWebservice(iPartsWSGetProductGroupsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"language\":\"de\";\"country\":\"200\"}}",
                          "Bad Request - \"POST /ident/GetProductGroups\": Request parameter of class \"iPartsWSGetProductGroupsRequest\" has malformed JSON content for Web service \"POST /ident/GetProductGroups\" (Could not deserialize to property 'user' of class class de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getproductgroups.iPartsWSGetProductGroupsRequest): {\"user\":{\"language\":\"de\";\"country\":\"200\"}}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testGetProductGroupsErrorEmpty() {
        executeWebservice(iPartsWSGetProductGroupsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{}",
                          "{\"code\":4000,\"message\":\"Attribute 'user' is missing\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testGetProductGroupsErrorUserIdMissing() {
        executeWebservice(iPartsWSGetProductGroupsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"language\":\"de\",\"country\":\"200\"}}",
                          "{\"code\":4000,\"message\":\"Path 'user': Attribute 'userId' is invalid (missing or empty)\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    // DAIMLER-4495 TODO Pflichtattribute im Token definieren
//    public void testGetProductGroupsErrorCountryMissing() {
//        executeWebservice(iPartsWSGetProductGroupsEndpoint.DEFAULT_ENDPOINT_URI,
//                          "{\"user\":{\"userId\":\"sb\"}}",
//                          "{\"code\":4000,\"message\":\"Path 'user': Attribute 'country' is invalid (missing or empty)\"}",
//                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
//    }


    // Tests für WebService: [GetProductClasses]
    public void testGetProductClassesGerman() {
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7IlNNVCI6WyJQQVNTRU5HRVItQ0FSIl19LCJsYW5nMSI6ImVuIiwibGFuZzIiOiJmciIsImxhbmczIjoiZXMiLCJleHAiOjk5OTk5OTk5OTl9.QFGAQujKok_8qDevclsY-mcLtrDaQf65QLZQA3P61wQ");
        executeWebservice(iPartsWSGetProductClassesEndpoint.DEFAULT_ENDPOINT_URI,
                          "{}",
                          additionalRequestProperties,
                          DWFile.get(getTestWorkingDir(), "resultGetProductClassesGerman.txt"));
    }

    public void testGetProductClassesDefaultCheckPermissionsOn() {
        executeTestWithBooleanConfigChanges(
                iPartsWebservicePlugin.getPluginConfig(), iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_PERMISSIONS, true,
                new WebserviceRunnable() {

                    @Override
                    public void executeTest() {
                        // Der alte Aufruf MIT gesetztem Schalter für die Berechtigungsprüfung muss zu einem Fehler führen.
                        executeWebservice(iPartsWSGetProductClassesEndpoint.DEFAULT_ENDPOINT_URI,
                                          "{\"user\":{\"userId\":\"user\",\"country\":\"200\"}}",
                                          "{\"code\":4031,\"message\":\"You are not authorized to access this information\"}",
                                          HttpConstants.HTTP_STATUS_FORBIDDEN);

                        // Ein Aufruf MIT gesetztem Schalter und gültiger Berechtigung für MB.PASSENGER-CAR
                        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiXX0sImxhbmcxIjoiZGUiLCJsYW5nMiI6ImVuIiwibGFuZzMiOiJmciIsImV4cCI6OTk5OTk5OTk5OX0.BzJsGY4IGuM3gr1K8M5WyPRqDu8U1eImMvDpC7gHIew");
                        executeWebservice(iPartsWSGetProductClassesEndpoint.DEFAULT_ENDPOINT_URI,
                                          "{}",
                                          additionalRequestProperties,
                                          DWFile.get(getTestWorkingDir(), "resultGetProductClassesWithCarPermission.txt"));

                    }
                });
    }

    public void testGetProductClassesDefaultCheckPermissionsOff() {
        executeTestWithBooleanConfigChanges(
                iPartsWebservicePlugin.getPluginConfig(), iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_PERMISSIONS, false,
                new WebserviceRunnable() {

                    @Override
                    public void executeTest() {
                        // Der alte Aufruf OHNE gesetzten Schalter für die Berechtigungsprüfung muss zu einem validen Ergebnis führen.
                        executeWebservice(iPartsWSGetProductClassesEndpoint.DEFAULT_ENDPOINT_URI,
                                          "{\"user\":{\"userId\":\"user\",\"country\":\"200\"}}",
                                          DWFile.get(getTestWorkingDir(), "resultGetProductClassesDefault.txt"));
                    }
                });
    }

    public void testGetProductClassesWithDifferentPermissionsPart1() {
        // -----------------------------------------------------------------
        // CONFIG_CHECK_TOKEN_PERMISSIONS ist per Default auf 'true'
        // -----------------------------------------------------------------

        // -- ALLE -- Berechtigungen im Token verschlüsselt
        //
        //   "permissions": {
        //    "MB": [
        //      "PASSENGER-CAR",
        //      "TRUCK",
        //      "UNIMOG",
        //      "VAN",
        //      "BUS"
        //    ],
        //    "SMT": [
        //      "PASSENGER-CAR"
        //    ],
        //    "MYB": [
        //      "PASSENGER-CAR"
        //    ]
        //  }
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");
        executeWebservice(iPartsWSGetProductClassesEndpoint.DEFAULT_ENDPOINT_URI,
                          "{}",
                          additionalRequestProperties,
                          DWFile.get(getTestWorkingDir(), "resultGetProductClassesAllPermissions.txt"));
    }

    public void testGetProductClassesWithDifferentPermissionsPart2() {
        // -- KEINE -- Berechtigungen im Token verschlüsselt
        // "permissions": {}
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiZGUiLCJsYW5nMiI6ImZyIiwibGFuZzMiOiJlcyIsImV4cCI6OTk5OTk5OTk5OSwicGVybWlzc2lvbnMiOnt9fQ.8vwKzS_cX4EljkSU9IN0dNKdQi_fetITDhC3QzGf9ew");
        executeWebservice(iPartsWSGetProductClassesEndpoint.DEFAULT_ENDPOINT_URI,
                          "{}",
                          additionalRequestProperties,
                          "{\"code\":4031,\"message\":\"You are not authorized to access this information\"}",
                          HttpConstants.HTTP_STATUS_FORBIDDEN);
    }

    public void testGetProductClassesWithDifferentPermissionsPart3() {
        // -- REDUTZIERTE -- Berechtigung, nur noch MB ohne PASSENGER-CAR
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlRSVUNLIiwiVU5JTU9HIiwiVkFOIiwiQlVTIl19LCJsYW5nMSI6ImRlIiwibGFuZzIiOiJlbiIsImxhbmczIjoiZnIiLCJleHAiOjk5OTk5OTk5OTl9.KGWdGLptTlrFCAa9s5Wvu3I2m44zvCQT_nbZ02gpW2E");
        executeWebservice(iPartsWSGetProductClassesEndpoint.DEFAULT_ENDPOINT_URI,
                          "{}",
                          additionalRequestProperties,
                          DWFile.get(getTestWorkingDir(), "resultGetProductClassesReducedPermissions01.txt"));
    }

    public void testGetProductClassesWithDifferentPermissionsPart4() {
        // -- REDUZIERTE -- Berechtigung, nur noch MB, TUCK
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlRSVUNLIl19LCJsYW5nMSI6ImRlIiwibGFuZzIiOiJlbiIsImxhbmczIjoiZnIiLCJleHAiOjk5OTk5OTk5OTl9.S41S3ITWv6Cg3MIJtcNQnaI1-5gvXm8Z77d50Z9p_Qw");
        executeWebservice(iPartsWSGetProductClassesEndpoint.DEFAULT_ENDPOINT_URI,
                          "{}",
                          additionalRequestProperties,
                          DWFile.get(getTestWorkingDir(), "resultGetProductClassesReducedPermissions02.txt"));
    }

    public void testGetProductClassesWithDifferentPermissionsPart5() {
        // -- REDUZIERTE -- Berechtigung, nur noch MB, UNIMOG
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlVOSU1PRyJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.k9M88QY5GQUdIkYjQDD27CKJ8GnAzZEi10_tbe_JPdY");
        executeWebservice(iPartsWSGetProductClassesEndpoint.DEFAULT_ENDPOINT_URI,
                          "{}",
                          additionalRequestProperties,
                          DWFile.get(getTestWorkingDir(), "resultGetProductClassesReducedPermissions03.txt"));
    }

    public void testGetProductClassesWithDifferentPermissionsPart6() {
        // -- REDUTZIERTE -- Berechtigung, nur noch MB, VAN
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlZBTiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.vg_I_2_yKK2gPWS-5xUKtJt9m_4Ja-R9wS5UxarRDXo");
        executeWebservice(iPartsWSGetProductClassesEndpoint.DEFAULT_ENDPOINT_URI,
                          "{}",
                          additionalRequestProperties,
                          DWFile.get(getTestWorkingDir(), "resultGetProductClassesReducedPermissions04.txt"));
    }

    public void testGetProductClassesWithDifferentPermissionsPart7() {
        // -- REDUTZIERTE -- Berechtigung, nur noch MB, BUS
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIkJVUyJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.S4GpJu5BdWeUIYlgLPr1f96LEj68k_fJsgftRsiPws4");
        executeWebservice(iPartsWSGetProductClassesEndpoint.DEFAULT_ENDPOINT_URI,
                          "{}",
                          additionalRequestProperties,
                          DWFile.get(getTestWorkingDir(), "resultGetProductClassesReducedPermissions05.txt"));
    }

    public void testGetProductClassesWithDifferentPermissionsPart8() {
        // -- REDUTZIERTE -- Berechtigung, nur noch SMT, PASSENGER-CAR
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7IlNNVCI6WyJQQVNTRU5HRVItQ0FSIl19LCJsYW5nMSI6ImRlIiwibGFuZzIiOiJlbiIsImxhbmczIjoiZnIiLCJleHAiOjk5OTk5OTk5OTl9.JLb_z6Ag7UyMUJ-Dr7MZC4HNb-h9sBKbAVqwYGNwVwg");
        executeWebservice(iPartsWSGetProductClassesEndpoint.DEFAULT_ENDPOINT_URI,
                          "{}",
                          additionalRequestProperties,
                          DWFile.get(getTestWorkingDir(), "resultGetProductClassesReducedPermissions06.txt"));

    }


    // Tests für WebService: [GetModelTypes]
    public void testGetModelTypesByProductNameGerman() {
        executeWebservice(iPartsWSGetModelTypesEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"},\"productClassIds\":[\"P\"],\"aggTypeId\":\"F\", \"includeModels\":true}",
                          DWFile.get(getTestWorkingDir(), "resultModelTypesByProductNameGerman.txt"));
    }

    public void testGetModelTypesWithModelsDefault() {
        executeWebservice(iPartsWSGetModelTypesEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"country\":\"200\"},\"productClassIds\":[\"P\"],\"aggTypeId\":\"F\", \"includeModels\":true}",
                          DWFile.get(getTestWorkingDir(), "resultModelTypesWithModelsDefault.txt"));
    }

    public void testGetModelTypesByProductNameWithModelsGerman() {
        executeWebservice(iPartsWSGetModelTypesEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"},\"aggTypeId\":\"F\",\"productName\":\"C21*\",\"productClassIds\":[\"P\"], \"includeModels\":true}",
                          "{\"modelTypes\":[{\"models\":[{\"aggTypeId\":\"F\",\"modelId\":\"C205204\",\"modelName\":\"\",\"modelTypeId\":\"C205\",\"productClassIds\":[\"P\"]},{\"aggTypeId\":\"F\",\"modelId\":\"C205205\",\"modelName\":\"\",\"modelTypeId\":\"C205\",\"productClassIds\":[\"P\"]},{\"aggTypeId\":\"F\",\"modelId\":\"C205207\",\"modelName\":\"\",\"modelTypeId\":\"C205\",\"productClassIds\":[\"P\"]},{\"aggTypeId\":\"F\",\"modelId\":\"C205208\",\"modelName\":\"\",\"modelTypeId\":\"C205\",\"productClassIds\":[\"P\"]},{\"aggTypeId\":\"F\",\"modelId\":\"C205209\",\"modelName\":\"\",\"modelTypeId\":\"C205\",\"productClassIds\":[\"P\"]},{\"aggTypeId\":\"F\",\"modelId\":\"C205236\",\"modelName\":\"\",\"modelTypeId\":\"C205\",\"productClassIds\":[\"P\"]},{\"aggTypeId\":\"F\",\"modelId\":\"C205237\",\"modelName\":\"\",\"modelTypeId\":\"C205\",\"productClassIds\":[\"P\"]}],\"modelTypeId\":\"C205\",\"productionEnd\":\"\",\"productionStart\":\"\",\"releaseEnd\":\"\",\"releaseStart\":\"\"}]}");
    }

    public void testGetModelTypesByProductIdWithModelsGerman() {
        executeWebservice(iPartsWSGetModelTypesEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"},\"aggTypeId\":\"F\",\"productId\":\"C21\",\"productClassIds\":[\"P\"], \"includeModels\":true}",
                          "{\"modelTypes\":[{\"models\":[{\"aggTypeId\":\"F\",\"modelId\":\"C205204\",\"modelName\":\"\",\"modelTypeId\":\"C205\",\"productClassIds\":[\"P\"]},{\"aggTypeId\":\"F\",\"modelId\":\"C205205\",\"modelName\":\"\",\"modelTypeId\":\"C205\",\"productClassIds\":[\"P\"]},{\"aggTypeId\":\"F\",\"modelId\":\"C205207\",\"modelName\":\"\",\"modelTypeId\":\"C205\",\"productClassIds\":[\"P\"]},{\"aggTypeId\":\"F\",\"modelId\":\"C205208\",\"modelName\":\"\",\"modelTypeId\":\"C205\",\"productClassIds\":[\"P\"]},{\"aggTypeId\":\"F\",\"modelId\":\"C205209\",\"modelName\":\"\",\"modelTypeId\":\"C205\",\"productClassIds\":[\"P\"]},{\"aggTypeId\":\"F\",\"modelId\":\"C205236\",\"modelName\":\"\",\"modelTypeId\":\"C205\",\"productClassIds\":[\"P\"]},{\"aggTypeId\":\"F\",\"modelId\":\"C205237\",\"modelName\":\"\",\"modelTypeId\":\"C205\",\"productClassIds\":[\"P\"]}],\"modelTypeId\":\"C205\",\"productionEnd\":\"\",\"productionStart\":\"\",\"releaseEnd\":\"\",\"releaseStart\":\"\"}]}");
    }

    public void testGetModelTypesByProductIdInvalid() {
        // Wildcards funktionieren nicht bei productId
        executeWebservice(iPartsWSGetModelTypesEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"},\"aggTypeId\":\"F\",\"productId\":\"C21*\",\"productClassIds\":[\"P\"]}",
                          "{\"modelTypes\":[]}");
    }

    public void testGetModelTypesErrorEmpty() {
        executeWebservice(iPartsWSGetModelTypesEndpoint.DEFAULT_ENDPOINT_URI,
                          "{}",
                          "{\"code\":4000,\"message\":\"Attribute 'user' is missing\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testGetModelTypesErrorMissingParameter() {
        executeWebservice(iPartsWSGetModelTypesEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"}}",
                          "{\"code\":4000,\"message\":\"Attribute list 'productClassIds' is invalid (missing or empty)\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testGetModelTypsErrorInvalidCombinations() {
        executeWebservice(iPartsWSGetModelTypesEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"de\",\"country\":\"200\"},\"productClassIds\":[\"P\"],\"aggTypeId\":\"F\",\"productId\":\"S10\",\"productName\":\"Test\"}",
                          "{\"code\":4003,\"message\":\"At least one of the attributes [productName, productId] must be empty: [Test, S10]\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    // Tests für WebService: [media]

    // nicht existente Bild-ID führt zu 404
    public void testGetMediaWrongImageId() {
        executeWebservice(iPartsWSGetMediaEndpoint.DEFAULT_ENDPOINT_URI + "/123", null, null,
                          null, // null body content führt zu GET
                          MimeTypes.MIME_TYPE_PNG, null,
                          "{\"code\":4001,\"message\":\"No media object found for '123'\"}",
                          HttpConstants.HTTP_STATUS_NOT_FOUND);
    }

    // Aufruf mit POST statt GET führt zu 405
    public void testGetMediaWrongHttpMethod() {
        executeWebservice(iPartsWSGetMediaEndpoint.DEFAULT_ENDPOINT_URI + "/abc", null, null,
                          "{}", // nicht leerer body content führt zu POST
                          MimeTypes.MIME_TYPE_PNG, null,
                          null, // ich finde es nicht so sinnvoll hier den exakten Text zu prüfen
                          HttpConstants.HTTP_STATUS_METHOD_NOT_ALLOWED);
    }

    // Test für Aufruf von GetMedia ohne Authentifizierung (muss je nach Admin-Option funktionieren)
    public void testGetMediaWithoutAuthentificationPart1() {
        Map<UniversalConfigOption, Boolean> configOptions = new HashMap<>();

        //--------------------Admin-Optionen auf "false" außer HEADER_ATTRIBUTES_FOR_PERMISSIONS ----------------------
        configOptions.put(iPartsWebservicePlugin.CONFIG_USER_IN_PAYLOAD_FALLBACK, false);
        configOptions.put(iPartsWebservicePlugin.CONFIG_NO_AUTHENTIFICATION_GET_MEDIA, false);
        configOptions.put(iPartsWebservicePlugin.CONFIG_HEADER_ATTRIBUTES_FOR_PERMISSIONS, true);

        executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), configOptions, new WebserviceRunnable() {

            @Override
            public void executeTest() {
                // Test ohne Authentifizierung mit nicht erlaubter fehlender Authentifizierung -> Fehler
                executeWebservice(iPartsWSGetMediaEndpoint.DEFAULT_ENDPOINT_URI + "/drawing_B58300000172", null, null,
                                  null, // null body content führt zu GET
                                  MimeTypes.MIME_TYPE_PNG, null,
                                  "{\"code\":4011,\"message\":\"Header 'Authorization' missing in request\"}",
                                  HttpConstants.HTTP_STATUS_UNAUTHORIZED);

                // Test mit Authentifizierung -> OK
                Map<String, String> additionalRequestProperties = createHeaderAttributesForPermissions("DE", "SMT.PASSENGER-CAR", "de, fr, es", "SB");
                executeWebservice(iPartsWSGetMediaEndpoint.DEFAULT_ENDPOINT_URI + "/drawing_B58300000172", null, null,
                                  null, // null body content führt zu GET
                                  MimeTypes.MIME_TYPE_PNG, additionalRequestProperties,
                                  "OK",
                                  HttpConstants.HTTP_STATUS_OK);

            }
        });
    }

    // Test für Aufruf von GetMedia ohne Authentifizierung (muss je nach Admin-Option funktionieren)
    public void testGetMediaWithoutAuthentificationPart2() {
        Map<UniversalConfigOption, Boolean> configOptions = new HashMap<>();

        //--------------------Admin-Optionen auf "false" außer HEADER_ATTRIBUTES_FOR_PERMISSIONS ----------------------
        configOptions.put(iPartsWebservicePlugin.CONFIG_USER_IN_PAYLOAD_FALLBACK, false);
        configOptions.put(iPartsWebservicePlugin.CONFIG_NO_AUTHENTIFICATION_GET_MEDIA, true);
        configOptions.put(iPartsWebservicePlugin.CONFIG_HEADER_ATTRIBUTES_FOR_PERMISSIONS, true);

        executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), configOptions, new WebserviceRunnable() {

            @Override
            public void executeTest() {
                // Jetzt mit erlaubter fehlender Authentifizierung
                // Test ohne Authentifizierung mit erlaubter fehlender Authentifizierung -> OK
                executeWebservice(iPartsWSGetMediaEndpoint.DEFAULT_ENDPOINT_URI + "/drawing_B58300000172", null, null,
                                  null, // null body content führt zu GET
                                  MimeTypes.MIME_TYPE_PNG, null,
                                  "OK",
                                  HttpConstants.HTTP_STATUS_OK);

            }
        });
    }

    // Tests für WebService: [SearchParts]
    // 1
    public void testSearchPartsForKgTuVehicle() {
        executeWebservice(iPartsWSSearchPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\": {\"aggTypeId\": \"F\",\"datacardExists\": false,\"modelId\": \"C967000\",\"productId\": \"S10\",\"productClassIds\":[\"F\",\"L\",\"O\",\"P\"]},\"searchText\": \"filterpatrone\", \"user\": {\"country\": \"200\",\"language\": \"de\",\"userId\": \"TRKA_tec_00\"},\"includeAggs\" : \"true\"}",
                          DWFile.get(getTestWorkingDir(), "resultSearchPartsForKgTuVehicle.txt"));
    }

    // 2: wie 1 mit passenden NavContext -> gleiche Treffer wie 1
    public void testSearchPartsForKgTuVehicleWithResultNavContext() {
        executeWebservice(iPartsWSSearchPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\": {\"aggTypeId\": \"F\",\"datacardExists\": false,\"modelId\": \"C967000\",\"productId\": \"S10\",\"productClassIds\":[\"F\",\"L\",\"O\",\"P\"]},\"searchText\": \"filterpatrone\", \"user\": {\"country\": \"200\",\"language\": \"de\",\"userId\": \"TRKA_tec_00\"},\"navContext\": [{\"id\": \"47\",\"type\": \"cg_group\",\"label\": \"Kupplung\"}],\"includeAggs\" : \"true\"}",
                          DWFile.get(getTestWorkingDir(), "resultSearchPartsForKgTuVehicle.txt"));
    }

    // 3: wie 1 mit Wildcards
    public void testSearchPartsForKgTuVehicleWithWildcards() {
        executeWebservice(iPartsWSSearchPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\": {\"aggTypeId\": \"F\",\"datacardExists\": false,\"modelId\": \"C967000\",\"productId\": \"S10\",\"productClassIds\":[\"F\",\"L\",\"O\",\"P\"]},\"searchText\": \"filter?atr*\", \"user\": {\"country\": \"200\",\"language\": \"de\",\"userId\": \"TRKA_tec_00\"},\"includeAggs\" : \"true\"}",
                          DWFile.get(getTestWorkingDir(), "resultSearchPartsForKgTuVehicle.txt"));
    }

    // Unterschiedlich viele Suchtreffer bei Suche mit/ohne Aggregate
    public void testSearchPartsForKgTuWithoutAggregates() {
        executeWebservice(iPartsWSSearchPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\": {\"aggTypeId\": \"F\",\"datacardExists\": false,\"modelId\": \"C205002\",\"productId\": \"C01\",\"productClassIds\":[\"F\",\"L\",\"O\",\"P\"]},\"searchText\": \"schraube\", \"user\": {\"country\": \"200\",\"language\": \"de\",\"userId\": \"TRKA_tec_00\"},\"includeAggs\" : \"false\"}",
                          DWFile.get(getTestWorkingDir(), "resultSearchPartsForKgTuExcludingAggs.txt"));
    }

    public void testSearchPartsForKgTuWithAggregates() {
        executeWebservice(iPartsWSSearchPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\": {\"aggTypeId\": \"F\",\"datacardExists\": false,\"modelId\": \"C205002\",\"productId\": \"C01\",\"productClassIds\":[\"F\",\"L\",\"O\",\"P\"]},\"searchText\": \"schraube\", \"user\": {\"country\": \"200\",\"language\": \"de\",\"userId\": \"TRKA_tec_00\"},\"includeAggs\" : \"true\"}",
                          DWFile.get(getTestWorkingDir(), "resultSearchPartsForKgTuIncludingAggs.txt"));
    }

    public void testSearchPartsForKgTuVehicleWithNavContext() {
        executeWebservice(iPartsWSSearchPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\": {\"aggTypeId\": \"F\",\"datacardExists\": false,\"modelId\": \"C205002\",\"productId\": \"C01\",\"productClassIds\":[\"F\",\"L\",\"O\",\"P\"]},\"searchText\": \"schraube\", \"user\": {\"country\": \"200\",\"language\": \"de\",\"userId\": \"TRKA_tec_00\"},\"includeAggs\" : \"true\",\"navContext\": [{\"id\": \"21\",\"type\": \"cg_group\",\"label\": \"\"},{\"id\": \"300\",\"type\": \"cg_subgroup\",\"label\": \"\"}]}",
                          DWFile.get(getTestWorkingDir(), "resultSearchPartsForKgTuWithNavContext.txt"));
    }

    public void testSearchPartsForSA() {
        executeWebservice(iPartsWSSearchPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\": {\"aggTypeId\": \"F\",\"datacardExists\": \"true\",\"fin\":\"WDB9630031L738999\",\"modelId\": \"C963003\",\"productId\": \"S01\",\"productClassIds\":[\"F\",\"L\",\"O\",\"P\"]},\"searchText\": \"A0015448090\", \"user\": {\"country\": \"200\",\"language\": \"de\",\"userId\": \"TRKA_tec_00\"},\"includeAggs\" : \"true\"}",
                          "{\"searchResults\":[{\"description\":\"HELLA\",\"name\":\"\",\"navContext\":[{\"id\":\"58\",\"label\":\"WERKZEUG UND ZUBEHOER\",\"type\":\"cg_group\"},{\"id\":\"Z 506.389\",\"label\":\"SV WARNLEUCHTE\",\"partsAvailable\":true,\"type\":\"sa_number\"}],\"partNo\":\"A0015448090\",\"partNoFormatted\":\"A 001 544 80 90\"}]}");
    }

    public void testSearchPartsForSANoDatacard() {
        executeWebservice(iPartsWSSearchPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\": {\"aggTypeId\": \"F\",\"modelId\": \"C963003\",\"productId\": \"S01\",\"productClassIds\":[\"F\",\"L\",\"O\",\"P\"]},\"searchText\": \"A0015448090\", \"user\": {\"country\": \"200\",\"language\": \"de\",\"userId\": \"TRKA_tec_00\"},\"includeAggs\" : \"true\"}",
                          "{\"searchResults\":[]}");
    }


    public void testSearchPartsForSAWithoutIncludeSAs() {
        executeWebservice(iPartsWSSearchPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\": {\"aggTypeId\": \"F\",\"datacardExists\": false,\"modelId\": \"C967002\",\"productId\": \"S10\",\"productClassIds\":[\"F\",\"L\",\"O\",\"P\"]},\"searchText\": \"A3756002343\", \"user\": {\"country\": \"200\",\"language\": \"de\",\"userId\": \"TRKA_tec_00\"},\"includeAggs\" : \"true\",\"includeSAs\": \"false\"}",
                          "{\"searchResults\":[]}");
    }

    // mit aktiviertem BM Filter
    public void testSearchPartsFilterBM() {
        executeWebservice(iPartsWSSearchPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C447605\",\"productId\":\"60V\",\"filterOptions\":{\"model\":true}}," +
                          "\"searchText\":\"A447545*\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          DWFile.get(getTestWorkingDir(), "resultSearchPartsFilterBM.txt"));
    }

    // mit zusätzlich aktiviertem Code Filter
    public void testSearchPartsFilterBMCode() {
        executeWebservice(iPartsWSSearchPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C447605\",\"productId\":\"60V\",\"filterOptions\":{\"model\":true," +
                          "\"codes\":true}},\"searchText\":\"A447545*\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          DWFile.get(getTestWorkingDir(), "resultSearchPartsFilterBMCode.txt"));
    }

    // mit zusätzlich aktiviertem Code Filter inklusive ES2 Keys
    public void testSearchPartsFilterBMCodeIncludingES2Keys() {
        executeWebservice(iPartsWSSearchPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C447605\",\"productId\":\"60V\",\"filterOptions\":{\"model\":true," +
                          "\"codes\":true}},\"searchText\":\"A447545*\",\"includeES2Keys\":\"true\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          DWFile.get(getTestWorkingDir(), "resultSearchPartsFilterBMCodeIncludingES2Keys.txt"));
    }

    // Suche mit Datenkarte inklusive gefilterten ES2 Keys
    public void testSearchPartsFilterIncludingES2Keys() {
        executeWebservice(iPartsWSSearchPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C246244\",\"productId\":\"P02\"," +
                          "\"datacardExists\":true,\"fin\":\"WDD2462441J179125\"},\"searchText\":\"A24668000*\",\"includeES2Keys\":\"true\"," +
                          "\"user\":{\"country\":\"DE\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          DWFile.get(getTestWorkingDir(), "resultSearchPartsFilterIncludingES2Keys.txt"));
    }

    // Es gibt genau einen Treffer im Motor. Bei normaler Filterung würde dieser ausgefiltert werden
    public void testSearchPartsMultiLayer() {
        executeWebservice(iPartsWSSearchPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"datacardExists\":true,\"fin\":\"WDD2050041F004362\",\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205004\",\"productId\":\"C01\"},\"searchText\":\"A0011596601\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"},\"includeAggs\":true}",
                          "{\"searchResults\":[{\"aggProductId\":\"69L\",\"aggTypeId\":\"M\",\"name\":\"GLUEHKERZE\",\"navContext\":[{\"id\":\"15\",\"label\":\"MOTORELEKTRIK\",\"type\":\"cg_group\"},{\"id\":\"105\",\"label\":\"ANBAUTEILE,GLUEHKERZEN UND DREHZAHL- GEBER\",\"type\":\"cg_subgroup\"},{\"id\":\"69L_15_105_00001\",\"label\":\"ANBAUTEILE,GLUEHKERZEN UND DREHZAHL- GEBER\",\"partsAvailable\":true,\"type\":\"module\"}],\"partNo\":\"A0011596601\",\"partNoFormatted\":\"A 001 159 66 01\"}]}");
    }

    // gleicher Fall wie oben, nur mit Baumuster statt Datenkarte
    public void testSearchPartsMultiLayerBM() {
        executeWebservice(iPartsWSSearchPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205004\",\"productId\":\"C01\"},\"searchText\":\"A0011597301\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"},\"includeAggs\":true}",
                          "{\"searchResults\":[{\"aggProductId\":\"69L\",\"aggTypeId\":\"M\",\"name\":\"GLUEHSTIFTKERZE\",\"navContext\":[{\"id\":\"15\",\"label\":\"MOTORELEKTRIK\",\"type\":\"cg_group\"},{\"id\":\"105\",\"label\":\"ANBAUTEILE,GLUEHKERZEN UND DREHZAHL- GEBER\",\"type\":\"cg_subgroup\"},{\"id\":\"69L_15_105_00001\",\"label\":\"ANBAUTEILE,GLUEHKERZEN UND DREHZAHL- GEBER\",\"partsAvailable\":true,\"type\":\"module\"}],\"partNo\":\"A0011597301\",\"partNoFormatted\":\"A 001 159 73 01\"}]}");
    }

    // Suche in einer freien SA von einem Aggregat des Fahrzeugs
    public void testSearchPartsMultiLayerInAggSA() {
        executeWebservice(iPartsWSSearchPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C967005\",\"productId\":\"S10\",\"filterOptions\":{\"model\":true}},\"searchText\":\"A0141549302\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"15\"}]}",
                          "{\"searchResults\":[{\"aggProductId\":\"M01\",\"aggTypeId\":\"M\",\"description\":\"BOSCH 24V/150A\",\"name\":\"GENERATOR\",\"navContext\":[{\"id\":\"15\",\"label\":\"ELEKTRISCHE AUSRUESTUNG\",\"type\":\"cg_group\"},{\"id\":\"030\",\"label\":\"DREHSTROMGENERATOR\",\"type\":\"cg_subgroup\"},{\"id\":\"M01_15_030_00001\",\"label\":\"DREHSTROMGENERATOR\",\"partsAvailable\":true,\"type\":\"module\"}],\"partNo\":\"A0141549302\",\"partNoFormatted\":\"A 014 154 93 02\"}]}");
    }

    public void testSearchPartsMultiLayerInAggSANoDatacard() {
        executeWebservice(iPartsWSSearchPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C967005\",\"productId\":\"S10\",\"filterOptions\":{\"model\":true}},\"searchText\":\"A0141549302\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"15\"}]}",
                          "{\"searchResults\":[{\"aggProductId\":\"M01\",\"aggTypeId\":\"M\",\"description\":\"BOSCH 24V/150A\",\"name\":\"GENERATOR\",\"navContext\":[{\"id\":\"15\",\"label\":\"ELEKTRISCHE AUSRUESTUNG\",\"type\":\"cg_group\"},{\"id\":\"030\",\"label\":\"DREHSTROMGENERATOR\",\"type\":\"cg_subgroup\"},{\"id\":\"M01_15_030_00001\",\"label\":\"DREHSTROMGENERATOR\",\"partsAvailable\":true,\"type\":\"module\"}],\"partNo\":\"A0141549302\",\"partNoFormatted\":\"A 014 154 93 02\"}]}");
    }

    // Eintrag ist nur im Motor vorhanden, bei includeAggs: false darf hier kein Ergebnis kommen
    public void testSearchPartsMultiLayerNoMatch() {
        executeWebservice(iPartsWSSearchPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"datacardExists\":true,\"fin\":\"WDD2050041F004362\",\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205004\",\"productId\":\"C01\"},\"searchText\":\"A0011597301\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"},\"includeAggs\":false}",
                          "{\"searchResults\":[]}");
    }

    // Backslash im Suchfeld sollte nicht mit java.sql.SQLDataException: ORA-01424 enden
    public void testSearchPartsBackslashInSearchString() {
        executeWebservice(iPartsWSSearchPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C447605\",\"productId\":\"60V\"},\"searchText\":\"00\\\\30*\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"searchResults\":[]}");
    }

    // Tests für WebService: [SearchPartsWOContext]
    public void testSearchPartsWOContextForKgTuWithoutModelType() {
        executeWebservice(iPartsWSSearchPartsWOContextEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"searchText\":\"N910105010016\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          DWFile.get(getTestWorkingDir(), "resultSearchPartsWOContextForKgTuWithoutModelType.txt"));
    }

    public void testSearchPartsWOContextForKgTuWithoutModelTypeFilterAggType() {
        executeWebservice(iPartsWSSearchPartsWOContextEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"searchText\":\"N910105010016\",\"aggTypeId\":\"M\",\"user\":{\"country\":\"200\",\"userId\":\"TRKA_tec_00\"}}",
                          DWFile.get(getTestWorkingDir(), "resultSearchPartsWOContextForKgTuWithoutModelTypeFilterAggType.txt"));
    }

    public void testSearchPartsWOContextForKgTuWithoutModelTypeMultipleGerman() {
        executeWebservice(iPartsWSSearchPartsWOContextEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"searchText\":\"N91010501001*\",\"aggTypeId\":\"M\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          DWFile.get(getTestWorkingDir(), "resultSearchPartsWOContextForKgTuWithoutModelTypeMultipleGerman.txt"));
    }

    public void testSearchPartsWOContextForKgTuWithModelTypeGerman() {
        executeWebservice(iPartsWSSearchPartsWOContextEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"searchText\":\"N910105010016\",\"modelTypeId\":\"C205\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          DWFile.get(getTestWorkingDir(), "resultSearchPartsWOContextForKgTuWithModelTypeGerman.txt"));
    }

    public void testSearchPartsWOContextForKgTuWithModelTypeMultipleIncludeNavContextGerman() {
        executeWebservice(iPartsWSSearchPartsWOContextEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"searchText\":\"N91010501001*\",\"modelTypeId\":\"C205\",\"includeNavContext\":true,\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          DWFile.get(getTestWorkingDir(), "resultSearchPartsWOContextForKgTu5.txt"));
    }

    public void testSearchPartsWOContextForKgTuWithModelTypeMultipleIncludeNavContextWithNavContextGerman() {
        executeWebservice(iPartsWSSearchPartsWOContextEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"searchText\":\"N91010501001*\",\"modelTypeId\":\"C205\",\"includeNavContext\":true,\"navContext\":[{\"type\":\"cg_group\",\"id\":\"32\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          DWFile.get(getTestWorkingDir(), "resultSearchPartsWOContextForKgTu6.txt"));
    }

    public void testSearchPartsWOContextSearchModeOnlySA() {
        executeWebservice(iPartsWSSearchPartsWOContextEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"searchText\":\"A0044294444\",\"modelTypeId\":\"C963\",\"includeNavContext\":true,\"searchMode\":\"onlySA\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"searchResults\":[{\"modelTypes\":[{\"models\":[{\"aggTypeId\":\"\",\"modelId\":\"C963005\",\"navNodesList\":[[{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"Z M02.819\",\"label\":\"LUFTFEDERVENTIL\",\"partsAvailable\":true,\"type\":\"sa_number\"}],[{\"id\":\"42\",\"label\":\"BREMSANLAGE\",\"type\":\"cg_group\"},{\"id\":\"Z M02.819\",\"label\":\"LUFTFEDERVENTIL\",\"partsAvailable\":true,\"type\":\"sa_number\"}]],\"productClassIds\":[\"S\"],\"productId\":\"S01\"},{\"aggTypeId\":\"\",\"modelId\":\"C963020\",\"navNodesList\":[[{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"Z M02.819\",\"label\":\"LUFTFEDERVENTIL\",\"partsAvailable\":true,\"type\":\"sa_number\"}],[{\"id\":\"42\",\"label\":\"BREMSANLAGE\",\"type\":\"cg_group\"},{\"id\":\"Z M02.819\",\"label\":\"LUFTFEDERVENTIL\",\"partsAvailable\":true,\"type\":\"sa_number\"}]],\"productClassIds\":[\"S\"],\"productId\":\"S01\"},{\"aggTypeId\":\"\",\"modelId\":\"C963024\",\"navNodesList\":[[{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"Z M03.271\",\"label\":\"LUFTFEDER STEUERUNG\",\"partsAvailable\":true,\"type\":\"sa_number\"}],[{\"id\":\"42\",\"label\":\"BREMSANLAGE\",\"type\":\"cg_group\"},{\"id\":\"Z M03.271\",\"label\":\"LUFTFEDER STEUERUNG\",\"partsAvailable\":true,\"type\":\"sa_number\"}],[{\"id\":\"54\",\"label\":\"ELEKTR. AUSRUESTUNG UND INSTRUMENTE\",\"type\":\"cg_group\"},{\"id\":\"Z M03.271\",\"label\":\"LUFTFEDER STEUERUNG\",\"partsAvailable\":true,\"type\":\"sa_number\"}]],\"productClassIds\":[\"S\"],\"productId\":\"S01\"},{\"aggTypeId\":\"\",\"modelId\":\"C963025\",\"navNodesList\":[[{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"Z M02.819\",\"label\":\"LUFTFEDERVENTIL\",\"partsAvailable\":true,\"type\":\"sa_number\"}],[{\"id\":\"42\",\"label\":\"BREMSANLAGE\",\"type\":\"cg_group\"},{\"id\":\"Z M02.819\",\"label\":\"LUFTFEDERVENTIL\",\"partsAvailable\":true,\"type\":\"sa_number\"}]],\"productClassIds\":[\"S\"],\"productId\":\"S01\"}],\"modelTypeId\":\"C963\",\"productClassIds\":[\"S\"]}],\"name\":\"VENTIL\",\"partNo\":\"A0044294444\",\"partNoFormatted\":\"A 004 429 44 44\"}]}");
    }

    public void testSearchPartsWOContextSearchModeOnlyModel() {
        executeWebservice(iPartsWSSearchPartsWOContextEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"searchText\":\"A0044294444\",\"modelTypeId\":\"C963\",\"includeNavContext\":true,\"searchMode\":\"onlyModel\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"searchResults\":[{\"modelTypes\":[{\"models\":[{\"aggTypeId\":\"\",\"modelId\":\"C963020\",\"navNodesList\":[[{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"510\",\"label\":\"LUFTFEDERVENTILE HINTERACHSE,NACHLAUFUND VORLAUFACHSE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}]],\"productClassIds\":[\"S\"],\"productId\":\"S01\"},{\"aggTypeId\":\"\",\"modelId\":\"C963021\",\"navNodesList\":[[{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"510\",\"label\":\"LUFTFEDERVENTILE HINTERACHSE,NACHLAUFUND VORLAUFACHSE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}]],\"productClassIds\":[\"S\"],\"productId\":\"S01\"},{\"aggTypeId\":\"\",\"modelId\":\"C963025\",\"navNodesList\":[[{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"510\",\"label\":\"LUFTFEDERVENTILE HINTERACHSE,NACHLAUFUND VORLAUFACHSE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}]],\"productClassIds\":[\"S\"],\"productId\":\"S01\"},{\"aggTypeId\":\"\",\"modelId\":\"C963042\",\"navNodesList\":[[{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"510\",\"label\":\"LUFTFEDERVENTILE HINTERACHSE,NACHLAUFUND VORLAUFACHSE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}]],\"productClassIds\":[\"S\"],\"productId\":\"S01\"},{\"aggTypeId\":\"\",\"modelId\":\"C963420\",\"navNodesList\":[[{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"510\",\"label\":\"LUFTFEDERVENTILE HINTERACHSE,NACHLAUFUND VORLAUFACHSE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}]],\"productClassIds\":[\"S\"],\"productId\":\"S01\"},{\"aggTypeId\":\"\",\"modelId\":\"C963425\",\"navNodesList\":[[{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"510\",\"label\":\"LUFTFEDERVENTILE HINTERACHSE,NACHLAUFUND VORLAUFACHSE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}]],\"productClassIds\":[\"S\"],\"productId\":\"S01\"}],\"modelTypeId\":\"C963\",\"productClassIds\":[\"S\"]}],\"name\":\"VENTIL\",\"partNo\":\"A0044294444\",\"partNoFormatted\":\"A 004 429 44 44\"}]}");
    }

    public void testSearchPartsWOContextSearchModeAll() {
        executeWebservice(iPartsWSSearchPartsWOContextEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"searchText\":\"A0044294444\",\"modelTypeId\":\"C963\",\"includeNavContext\":true,\"searchMode\":\"all\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"searchResults\":[{\"modelTypes\":[{\"models\":[{\"aggTypeId\":\"\",\"modelId\":\"C963005\",\"navNodesList\":[[{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"Z M02.819\",\"label\":\"LUFTFEDERVENTIL\",\"partsAvailable\":true,\"type\":\"sa_number\"}],[{\"id\":\"42\",\"label\":\"BREMSANLAGE\",\"type\":\"cg_group\"},{\"id\":\"Z M02.819\",\"label\":\"LUFTFEDERVENTIL\",\"partsAvailable\":true,\"type\":\"sa_number\"}]],\"productClassIds\":[\"S\"],\"productId\":\"S01\"},{\"aggTypeId\":\"\",\"modelId\":\"C963020\",\"navNodesList\":[[{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"510\",\"label\":\"LUFTFEDERVENTILE HINTERACHSE,NACHLAUFUND VORLAUFACHSE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}],[{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"Z M02.819\",\"label\":\"LUFTFEDERVENTIL\",\"partsAvailable\":true,\"type\":\"sa_number\"}],[{\"id\":\"42\",\"label\":\"BREMSANLAGE\",\"type\":\"cg_group\"},{\"id\":\"Z M02.819\",\"label\":\"LUFTFEDERVENTIL\",\"partsAvailable\":true,\"type\":\"sa_number\"}]],\"productClassIds\":[\"S\"],\"productId\":\"S01\"},{\"aggTypeId\":\"\",\"modelId\":\"C963021\",\"navNodesList\":[[{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"510\",\"label\":\"LUFTFEDERVENTILE HINTERACHSE,NACHLAUFUND VORLAUFACHSE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}]],\"productClassIds\":[\"S\"],\"productId\":\"S01\"},{\"aggTypeId\":\"\",\"modelId\":\"C963024\",\"navNodesList\":[[{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"Z M03.271\",\"label\":\"LUFTFEDER STEUERUNG\",\"partsAvailable\":true,\"type\":\"sa_number\"}],[{\"id\":\"42\",\"label\":\"BREMSANLAGE\",\"type\":\"cg_group\"},{\"id\":\"Z M03.271\",\"label\":\"LUFTFEDER STEUERUNG\",\"partsAvailable\":true,\"type\":\"sa_number\"}],[{\"id\":\"54\",\"label\":\"ELEKTR. AUSRUESTUNG UND INSTRUMENTE\",\"type\":\"cg_group\"},{\"id\":\"Z M03.271\",\"label\":\"LUFTFEDER STEUERUNG\",\"partsAvailable\":true,\"type\":\"sa_number\"}]],\"productClassIds\":[\"S\"],\"productId\":\"S01\"},{\"aggTypeId\":\"\",\"modelId\":\"C963025\",\"navNodesList\":[[{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"510\",\"label\":\"LUFTFEDERVENTILE HINTERACHSE,NACHLAUFUND VORLAUFACHSE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}],[{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"Z M02.819\",\"label\":\"LUFTFEDERVENTIL\",\"partsAvailable\":true,\"type\":\"sa_number\"}],[{\"id\":\"42\",\"label\":\"BREMSANLAGE\",\"type\":\"cg_group\"},{\"id\":\"Z M02.819\",\"label\":\"LUFTFEDERVENTIL\",\"partsAvailable\":true,\"type\":\"sa_number\"}]],\"productClassIds\":[\"S\"],\"productId\":\"S01\"},{\"aggTypeId\":\"\",\"modelId\":\"C963042\",\"navNodesList\":[[{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"510\",\"label\":\"LUFTFEDERVENTILE HINTERACHSE,NACHLAUFUND VORLAUFACHSE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}]],\"productClassIds\":[\"S\"],\"productId\":\"S01\"},{\"aggTypeId\":\"\",\"modelId\":\"C963420\",\"navNodesList\":[[{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"510\",\"label\":\"LUFTFEDERVENTILE HINTERACHSE,NACHLAUFUND VORLAUFACHSE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}]],\"productClassIds\":[\"S\"],\"productId\":\"S01\"},{\"aggTypeId\":\"\",\"modelId\":\"C963425\",\"navNodesList\":[[{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"510\",\"label\":\"LUFTFEDERVENTILE HINTERACHSE,NACHLAUFUND VORLAUFACHSE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}]],\"productClassIds\":[\"S\"],\"productId\":\"S01\"}],\"modelTypeId\":\"C963\",\"productClassIds\":[\"S\"]}],\"name\":\"VENTIL\",\"partNo\":\"A0044294444\",\"partNoFormatted\":\"A 004 429 44 44\"}]}");
    }

    public void testSearchPartsWOContextSearchModeDefault() {
        executeWebservice(iPartsWSSearchPartsWOContextEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"searchText\":\"A0044294444\",\"modelTypeId\":\"C963\",\"includeNavContext\":true,\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"searchResults\":[{\"modelTypes\":[{\"models\":[{\"aggTypeId\":\"\",\"modelId\":\"C963005\",\"navNodesList\":[[{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"Z M02.819\",\"label\":\"LUFTFEDERVENTIL\",\"partsAvailable\":true,\"type\":\"sa_number\"}],[{\"id\":\"42\",\"label\":\"BREMSANLAGE\",\"type\":\"cg_group\"},{\"id\":\"Z M02.819\",\"label\":\"LUFTFEDERVENTIL\",\"partsAvailable\":true,\"type\":\"sa_number\"}]],\"productClassIds\":[\"S\"],\"productId\":\"S01\"},{\"aggTypeId\":\"\",\"modelId\":\"C963020\",\"navNodesList\":[[{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"510\",\"label\":\"LUFTFEDERVENTILE HINTERACHSE,NACHLAUFUND VORLAUFACHSE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}],[{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"Z M02.819\",\"label\":\"LUFTFEDERVENTIL\",\"partsAvailable\":true,\"type\":\"sa_number\"}],[{\"id\":\"42\",\"label\":\"BREMSANLAGE\",\"type\":\"cg_group\"},{\"id\":\"Z M02.819\",\"label\":\"LUFTFEDERVENTIL\",\"partsAvailable\":true,\"type\":\"sa_number\"}]],\"productClassIds\":[\"S\"],\"productId\":\"S01\"},{\"aggTypeId\":\"\",\"modelId\":\"C963021\",\"navNodesList\":[[{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"510\",\"label\":\"LUFTFEDERVENTILE HINTERACHSE,NACHLAUFUND VORLAUFACHSE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}]],\"productClassIds\":[\"S\"],\"productId\":\"S01\"},{\"aggTypeId\":\"\",\"modelId\":\"C963024\",\"navNodesList\":[[{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"Z M03.271\",\"label\":\"LUFTFEDER STEUERUNG\",\"partsAvailable\":true,\"type\":\"sa_number\"}],[{\"id\":\"42\",\"label\":\"BREMSANLAGE\",\"type\":\"cg_group\"},{\"id\":\"Z M03.271\",\"label\":\"LUFTFEDER STEUERUNG\",\"partsAvailable\":true,\"type\":\"sa_number\"}],[{\"id\":\"54\",\"label\":\"ELEKTR. AUSRUESTUNG UND INSTRUMENTE\",\"type\":\"cg_group\"},{\"id\":\"Z M03.271\",\"label\":\"LUFTFEDER STEUERUNG\",\"partsAvailable\":true,\"type\":\"sa_number\"}]],\"productClassIds\":[\"S\"],\"productId\":\"S01\"},{\"aggTypeId\":\"\",\"modelId\":\"C963025\",\"navNodesList\":[[{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"510\",\"label\":\"LUFTFEDERVENTILE HINTERACHSE,NACHLAUFUND VORLAUFACHSE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}],[{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"Z M02.819\",\"label\":\"LUFTFEDERVENTIL\",\"partsAvailable\":true,\"type\":\"sa_number\"}],[{\"id\":\"42\",\"label\":\"BREMSANLAGE\",\"type\":\"cg_group\"},{\"id\":\"Z M02.819\",\"label\":\"LUFTFEDERVENTIL\",\"partsAvailable\":true,\"type\":\"sa_number\"}]],\"productClassIds\":[\"S\"],\"productId\":\"S01\"},{\"aggTypeId\":\"\",\"modelId\":\"C963042\",\"navNodesList\":[[{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"510\",\"label\":\"LUFTFEDERVENTILE HINTERACHSE,NACHLAUFUND VORLAUFACHSE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}]],\"productClassIds\":[\"S\"],\"productId\":\"S01\"},{\"aggTypeId\":\"\",\"modelId\":\"C963420\",\"navNodesList\":[[{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"510\",\"label\":\"LUFTFEDERVENTILE HINTERACHSE,NACHLAUFUND VORLAUFACHSE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}]],\"productClassIds\":[\"S\"],\"productId\":\"S01\"},{\"aggTypeId\":\"\",\"modelId\":\"C963425\",\"navNodesList\":[[{\"id\":\"32\",\"label\":\"FEDERN UND AUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"510\",\"label\":\"LUFTFEDERVENTILE HINTERACHSE,NACHLAUFUND VORLAUFACHSE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}]],\"productClassIds\":[\"S\"],\"productId\":\"S01\"}],\"modelTypeId\":\"C963\",\"productClassIds\":[\"S\"]}],\"name\":\"VENTIL\",\"partNo\":\"A0044294444\",\"partNoFormatted\":\"A 004 429 44 44\"}]}");
    }

    // SearchPartsWOContext mit und ohne Admin-Option "Nur retail-relevante Produkte berücksichtigen (Flag "Produkt sichtbar")"
    public void testSearchPartsWOContextProductInvisibleOnlyRetailRelevantProducts() {
        clearWebservicePluginsCaches(); // Caches müssen gelöscht werden, weil die Ergebnisse sich nun bei gleichem Request unterscheiden
        executeWebservice(iPartsWSSearchPartsWOContextEndpoint.DEFAULT_ENDPOINT_URI,
                          " {\"searchText\":\"A6510105508\",\"modelTypeId\":\"D651\",\"includeNavContext\":true,\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          DWFile.get(getTestWorkingDir(), "resultSearchPartsWOContextProductInvisibleOnlyRetailRelevantProducts.txt"));
    }

    public void testSearchPartsWOContextProductInvisibleAllProducts() {
        // Temporär die Admin-Option "Nur retail-relevante Produkte berücksichtigen (Flag "Produkt sichtbar")" deaktivieren
        UniversalConfiguration pluginConfig = iPartsWebservicePlugin.getPluginConfig();
        writeBooleanConfigValues(pluginConfig, false, iPartsWebservicePlugin.CONFIG_ONLY_RETAIL_RELEVANT_PRODUCTS);

        try {
            clearWebservicePluginsCaches(); // Caches müssen gelöscht werden, weil die Ergebnisse sich nun bei gleichem Request unterscheiden
            executeWebservice(iPartsWSSearchPartsWOContextEndpoint.DEFAULT_ENDPOINT_URI,
                              " {\"searchText\":\"A6510105508\",\"modelTypeId\":\"D651\",\"includeNavContext\":true,\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                              DWFile.get(getTestWorkingDir(), "resultSearchPartsWOContextProductInvisibleAllProducts.txt"));
        } finally {
            writeBooleanConfigValues(pluginConfig, true, iPartsWebservicePlugin.CONFIG_ONLY_RETAIL_RELEVANT_PRODUCTS);
            clearWebservicePluginsCaches(); // Caches müssen gelöscht werden, damit die Admin-Änderung auch auf jeden Fall berücksichtigt wird
        }
    }

    // Backslash im Suchfeld sollte nicht mit java.sql.SQLDataException: ORA-01424 enden
    public void testSearchPartsWOContextBackslashInSearchText() {
        executeWebservice(iPartsWSSearchPartsWOContextEndpoint.DEFAULT_ENDPOINT_URI,
                          " {\"searchText\":\"00\\\\30*\",\"modelTypeId\":\"C405\",\"includeNavContext\":true,\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}", "{\"searchResults\":[]}");
    }

    public void testSearchPartsWOContextSearchModeSupplierNumberNoUsage() {
        // leeres SearchResult weil es zwar Treffer im Suppliermapping gibt, die Materialnummern aber nicht verwendet werden
        executeWebservice(iPartsWSSearchPartsWOContextEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"searchText\":\"BO 2\",\"searchMode\":\"supplierNumber\",\"user\":{\"country\":\"200\"," +
                          "\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"searchResults\":[]}");
    }

    public void testSearchPartsWOContextSearchModeSupplierNumberWithUsage() {
        executeWebservice(iPartsWSSearchPartsWOContextEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"searchText\":\"4130\",\"searchMode\":\"supplierNumber\",\"user\":{\"country\":\"200\"," +
                          "\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"searchResults\":[{\"name\":\"\",\"partNo\":\"A0000320006\",\"partNoFormatted\":" +
                          "\"A 000 032 00 06\",\"supplierName\":\"ZF\",\"supplierPartNo\":\"4130 302 115\"}]}");
    }

    public void testSearchPartsWOContextSearchModeMasterDataCustProp() {
        executeWebservice(iPartsWSSearchPartsWOContextEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"searchText\":\"A0029907600\",\"searchMode\":\"masterData\",\"user\":{\"country\":\"200\"," +
                          "\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"searchResults\":[{\"additionalPartInformation\":[{\"description\":\"Gew. Nenndurchmesser (A03)\"," +
                          "\"source\":\"iParts\",\"type\":\"GDurc\",\"value\":\"6,000\"},{\"description\":\"Gew. Kennbuchstabe\",\"source\":\"iParts\"," +
                          "\"type\":\"GKenn\",\"value\":\"M\"},{\"description\":\"Gew. Steigung (A04)\",\"source\":\"iParts\",\"type\":\"GStei\"," +
                          "\"value\":\"1,000\"},{\"description\":\"Festigkeit/ Härteklasse\",\"source\":\"iParts\",\"type\":\"SHart\",\"value\":\"10.9\"}," +
                          "{\"description\":\"Schraubenlänge/ Nennlänge (B)\",\"source\":\"iParts\",\"type\":\"SLang\",\"value\":\"24,00\"}]," +
                          "\"partNo\":\"A0029907600\",\"partNoFormatted\":\"A 002 990 76 00\"}]}");
    }

    public void testSearchPartsWOContextSearchModeMasterDataLanguageAndMatFields() {
        executeWebservice(iPartsWSSearchPartsWOContextEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"searchText\":\"A0009907006\",\"searchMode\":\"masterData\",\"user\":{\"country\":\"200\"," +
                          "\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          DWFile.get(getTestWorkingDir(), "resultSearchPartsWOContextSearchModeMasterDataLanguageAndMatFields.txt"));
    }

    // Tests für WebService: [GetPartInfo]

    // DAIMLER-10036 Beschränkung der Daten wegen RMI
    public void testGetPartInfoRMI() {
        UniversalConfiguration pluginConfig = iPartsWebservicePlugin.getPluginConfig();
        writeBooleanConfigValues(pluginConfig, true, iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI);
        try {
            // Caches müssen gelöscht werden, damit die Admin-Änderung auch auf jeden Fall berücksichtigt wird
            clearWebservicePluginsCaches();

            executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"user\":{\"country\":\"200\",\"language\":\"en\",\"userId\":\"userId\"},\"partContext\":{\"moduleId\":\"C253_FC_68_485_00001\",\"sequenceId\":\"00021\"}," +
                              "\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C253305\",\"productId\":\"C253_FC\",\"filterOptions\":{\"model\":true}}}",
                              DWFile.get(getTestWorkingDir(), "resultGetPartInfoRMI.txt"));
        } finally {
            writeBooleanConfigValues(pluginConfig, false, iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI);
            clearWebservicePluginsCaches();
        }
    }

    /**
     * Hier werden zusätzlich zu den RMI-Daten auch noch die EinPAS-Daten einmal dazugemischt und einmal weggelassen.
     */
    public void testGetPartInfoEinPASRMIreduced() {
        Map<UniversalConfigOption, Boolean> configOptions = new HashMap<>();
        String requestString = "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"T\"],\"modelId\":\"C166823\",\"productId\":\"D81\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"82\"},{\"type\":\"cg_subgroup\",\"id\":\"175\"}],\"partContext\":{\"moduleId\":\"D81_82_175_00001\",\"sequenceId\":\"00162\"}}";

        try {
            // Ohne gesetzten Schalter werden die Ergebnisdaten MIT den EinPAS-Daten angereichert ausgegeben:
            configOptions.put(iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI, false);
            executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), configOptions, new WebserviceRunnable() {
                @Override
                public void executeTest() {
                    executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI, requestString,
                                      DWFile.get(getTestWorkingDir(), "resultGetPartInfoRMInotReduced.txt"));
                }
            });

            // Mit gesetztem Schalter werden die Ergebnisdaten OHNE die EinPAS-Daten ausgegeben.
            configOptions.put(iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI, true);
            executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), configOptions, new WebserviceRunnable() {
                @Override
                public void executeTest() {
                    executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI, requestString,
                                      DWFile.get(getTestWorkingDir(), "resultGetPartInfoRMIreduced.txt"));
                }
            });
        } finally {
            configOptions.put(iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI, false);
        }
    }

    public void testGetPartInfoSAAEldasPlantInfo() {
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\" : {\"country\" : \"200\",\"language\" : \"de\",\"userId\" : \"userId\"},\"partContext\" : {\"moduleId\" : \"06F_31_190_00001\",\"sequenceId\" : \"00003\"}," +
                          "\"identContext\" : {\"aggTypeId\" : \"FH\",\"productClassIds\" : [\"T\",\"L\"],\"modelId\" : \"D669599\",\"productId\" : \"06F\"}}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartInfoSAAEldasPlantInfo.txt"));
    }

    public void testGetPartInfoCodeColorsPlants() {
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\" : {\"country\" : \"200\",\"language\" : \"de\",\"userId\" : \"userId\"},\"partContext\" : {\"moduleId\" : \"C01_68_193_00001\",\"sequenceId\" : \"00225\"}," +
                          "\"identContext\" : {\"aggTypeId\" : \"F\",\"productClassIds\" : [\"P\"],\"modelId\" : \"C205002\",\"productId\" : \"C01\",\"modelTypeId\":\"C205\"," +
                          "\"filterOptions\":{\"model\":true}}}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartInfoCodeColorsPlants.txt"));
    }

    public void testGetPartInfoForSA() {
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C967607\",\"productId\":\"S10\",\"fin\":\"WDB9676071L972285\",\"datacardExists\":\"true\"},\"partContext\" : {\"moduleId\" : \"SA-Z506389\",\"sequenceId\" : \"00003\"},\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"partInfo\":{\"saaValidityDetails\":[{\"code\":\"Z 506.389\",\"description\":\"SV WARNLEUCHTE\",\"saaCodes\":[{\"code\":\"Z 506.389/01\",\"description\":\"GROSS                                     OHNE GGVS\"},{\"code\":\"Z 506.389/02\",\"description\":\"GEFAHRENKLASSE III A                      KLEIN\"},{\"code\":\"Z 506.389/03\",\"description\":\"GEFAHRENKLASSE III A                      GROSS\"},{\"code\":\"Z 506.389/04\",\"description\":\"KLEIN                                     OHNE GGVS\"},{\"code\":\"Z 506.389/05\",\"description\":\"KLEIN                                     GGVS\"}]}]}}");
    }

    public void testGetPartInfoForSANoDatacard() {
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C967607\",\"productId\":\"S10\"},\"partContext\" : {\"moduleId\" : \"SA-Z506389\",\"sequenceId\" : \"00003\"},\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"code\":4001,\"message\":\"Module 'SA-Z506389' is invalid for product 'S10', current context and filter options\"}", HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testGetPartInfoForInvalidSA() {
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C967002\",\"productId\":\"S10\"},\"partContext\" : {\"moduleId\" : \"SA-ZM03379\",\"sequenceId\" : \"00006\"},\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"code\":4001,\"message\":\"Module 'SA-ZM03379' is invalid for product 'S10', current context and filter options\"}", 400);
    }

    // sequenceID 00001 wird vom Baumuster Filter ausgefiltert
    public void testGetPartInfoNoResult() {
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\" : {\"country\" : \"200\",\"language\" : \"de\",\"userId\" : \"userId\"},\"partContext\" : {\"moduleId\" : \"C01_21_300_00001\",\"sequenceId\" : \"00001\"}," +
                          "\"identContext\" : {\"aggTypeId\" : \"F\",\"productClassIds\" : [\"P\"],\"modelId\" : \"C205002\",\"productId\" : \"C01\"}}", "{\"code\":4001,\"message\":\"Path 'partContext': SequenceId '00001' is invalid in module 'C01_21_300_00001' for current context and filter options\"}", HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testGetPartInfoAlternativeParts() {
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"partContext\":{\"moduleId\":\"F10_67_060_00001\",\"sequenceId\":\"00016\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"D967820\",\"productId\":\"F10\",\"filterOptions\":{}}}",
                          "{\"partInfo\":{\"alternativeParts\":[{\"es1Key\":\"79\",\"es2Key\":\"7065\",\"name\":\"GLASSCHEIBE\",\"partNo\":\"A9416780110\",\"partNoFormatted\":\"A 941 678 01 10\",\"type\":\"01\"},{\"es1Key\":\"92\",\"es2Key\":\"7025\",\"name\":\"GLASSCHEIBE\",\"partNo\":\"A9416780110\",\"partNoFormatted\":\"A 941 678 01 10\",\"type\":\"01\"}],\"saaValidityDetails\":[{\"code\":\"Z 508.529\",\"description\":\"SV FENSTERANLAGE HINTEN\",\"saaCodes\":[{\"code\":\"Z 508.529/05\",\"description\":\"SV FENSTERANLAGE HINTEN\"}]}]}}");
    }

    // mit aktivem BM Filter ist das Teil trotzdem noch da, in den nächsten beiden Tests wird es jeweils ausgefiltert
    public void testGetPartInfoFilterBM() {
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"partContext\":{\"moduleId\":\"60V_24_060_00001\",\"sequenceId\":\"00088\"}," +
                          "\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"T\"],\"modelId\":\"C447703\",\"productId\":\"60V\",\"datacardExists\":true," +
                          "\"fin\":\"WD444770313138415\",\"filterOptions\":{\"model\":true}}}",
                          "{\"partInfo\":{\"codeValidityDetails\":[{\"code\":\"GE1\",\"dateFrom\":\"2012-08-06\",\"name\":\"6-GANG-SCHALTGETRIEBE TSG 380\"},{\"code\":\"MG3\",\"dateFrom\":\"2009-03-19\",\"name\":\"MOTOR OM651 DE22LA 120KW (163PS) 3800/MIN\"},{\"code\":\"MG7\",\"dateFrom\":\"2009-11-26\",\"name\":\"MOTOR OM651 DE 22 LA 100 KW (136 PS) 3800/MIN\"}],\"codeValidityMatrix\":[[{\"code\":\"GE1\"},{\"code\":\"MG3\"}],[{\"code\":\"GE1\"},{\"code\":\"MG7\"}]]}}");
    }

    public void testGetPartInfoFactoryData() {
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI, "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\n" +
                                                                            "\"partContext\":{\"moduleId\":\"C02_21_230_00001\",\"sequenceId\":\"00007\"},\n" +
                                                                            "\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205040\",\"productId\":\"C02\",\"modelTypeId\":\"C205\",\"filterOptions\":{}}}",
                          "{\"partInfo\":{\"codeValidityDetails\":[{\"code\":\"M014\",\"dateFrom\":\"2009-01-16\",\"name\":\"MOTOR LEISTUNGSGESTEIGERT\"},{\"code\":\"M274\",\"dateFrom\":\"2009-01-16\",\"name\":\"R4-OTTOMOTOR M274\"},{\"code\":\"ME06\",\"dateFrom\":\"2011-08-22\",\"name\":\"HYBRIDANTRIEB 60 KW - VARIANTE (INKL. PLUGIN)\"}],\"codeValidityMatrix\":[[{\"code\":\"M014\"},{\"code\":\"M274\"},{\"code\":\"ME06\",\"negative\":true}]],\"plantInformation\":[{\"date\":\"2015-06-30\",\"plant\":\"F\",\"type\":\"bis\"},{\"date\":\"2015-10-09\",\"ident\":\"L083556\",\"plant\":\"L\",\"type\":\"bis\"},{\"plant\":\"U\",\"type\":\"bis\"},{\"date\":\"2044-04-04\",\"plant\":\"R\",\"type\":\"bis\"},{\"date\":\"2015-06-30\",\"ident\":\"L007279\",\"plant\":\"L, M, V (CKD)\",\"type\":\"bis\"},{\"date\":\"2015-06-30\",\"ident\":\"M000001\",\"plant\":\"L, M, V (CKD)\",\"type\":\"bis\"},{\"date\":\"2015-06-30\",\"ident\":\"V001231\",\"plant\":\"L, M, V (CKD)\",\"type\":\"bis\"}]}}");
    }

    // mit aktivem Lenkungs Filter wird das Teil ausgefiltert
    public void testGetPartInfoFilterSteering() {
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"partContext\":{\"moduleId\":\"60V_24_060_00001\",\"sequenceId\":\"00088\"}," +
                          "\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"T\"],\"modelId\":\"C447703\",\"productId\":\"60V\",\"datacardExists\":true," +
                          "\"fin\":\"WD444770313138415\",\"filterOptions\":{\"model\":true,\"steering\":true}}}",
                          "{\"code\":4001,\"message\":\"Path 'partContext': SequenceId '00088' is invalid in module '60V_24_060_00001' for current context and filter options\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testGetPartInfoColorFilter() {
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"partContext\":{\"moduleId\":\"C01_68_193_00001\",\"sequenceId\":\"00041\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205002\",\"productId\":\"C01\",\"modelTypeId\":\"C205\",\"filterOptions\":{\"model\":true}}}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartInfoColorFilter.txt"));
    }

    public void testGetPartInfoPrioritiseOmittedPartPart1() {
        // Stücklistenposition existiert
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"partContext\":{\"moduleId\":\"C01_91_065_00001\",\"sequenceId\":\"00458\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205009\",\"productId\":\"C01\",\"modelTypeId\":\"C205\",\"filterOptions\":{}}}",
                          "{\"partInfo\":{\"codeValidityDetails\":[{\"code\":\"555\",\"dateFrom\":\"2012-02-28\",\"name\":\"AMG-PERFORMANCE-SITZ\"},{\"code\":\"800A\",\"dateFrom\":\"2012-08-06\",\"name\":\"LEDER / NAPPA / SEMIANILIN\"},{\"code\":\"830\",\"dateFrom\":\"2009-02-13\",\"name\":\"CHINA-FAHRZEUGE-ZUSATZTEILE\"},{\"code\":\"850A\",\"dateFrom\":\"2013-03-18\",\"name\":\"LEDER / NAPPA / SEMIANILIN\"}],\"codeValidityMatrix\":[[{\"code\":\"555\",\"negative\":true},{\"code\":\"800A\"},{\"code\":\"830\",\"negative\":true}],[{\"code\":\"555\",\"negative\":true},{\"code\":\"830\",\"negative\":true},{\"code\":\"850A\"}]]}}");
    }

    public void testGetPartInfoPrioritiseOmittedPartPart2() {
        // Stücklistenposition wurde von Wegfallsachnummer verdrängt
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"partContext\":{\"moduleId\":\"C01_91_065_00001\",\"sequenceId\":\"00458\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205009\",\"productId\":\"C01\",\"modelTypeId\":\"C205\",\"filterOptions\":{\"model\":true}}}",
                          "{\"code\":4001,\"message\":\"Path 'partContext': SequenceId '00458' is invalid in module 'C01_91_065_00001' for current context and filter options\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testGetPartInfoModelyearFilterForColor() {
        // Test für DAIMLER-6883
        // Im Endnummernfilter für Farben werden Farben ausgefiltert falls das Datum gültig ist, aber der ModelljahrCode auf der Datenkarte enthalten
        // In diesem Beispiel ist das Datum gültig, aber der ModelljahrCode NICHT auf der Datenkarte, deshalb bleibt die eine Farbe übrig
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"partContext\":{\"moduleId\":\"D43_68_193_00001\",\"sequenceId\":\"00063\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C172448\",\"fin\":\"WDD1724481F005653\",\"productId\":\"D43\",\"datacardExists\":true}}",
                          "{\"partInfo\":{\"codeValidityDetails\":[{\"code\":\"460\",\"dateFrom\":\"2006-03-29\",\"name\":\"KANADA-AUSFUEHRUNG / ZUSATZTEILE\"},{\"code\":\"494\",\"dateFrom\":\"2006-03-29\",\"name\":\"USA-AUSFUEHRUNG\"},{\"code\":\"802\",\"dateFrom\":\"1995-10-02\",\"name\":\"MODELLJAHRWECHSEL\"},{\"code\":\"803\",\"dateFrom\":\"2009-02-26\",\"name\":\"AEJ 12/1\"},{\"code\":\"804\",\"dateFrom\":\"2009-02-26\",\"name\":\"AEJ 13/1\"},{\"code\":\"805\",\"dateFrom\":\"2010-03-16\",\"name\":\"AEJ 14/1\"}],\"codeValidityMatrix\":[[{\"code\":\"460\",\"negative\":true},{\"code\":\"494\",\"negative\":true},{\"code\":\"802\"}],[{\"code\":\"460\",\"negative\":true},{\"code\":\"494\",\"negative\":true},{\"code\":\"803\"}],[{\"code\":\"460\",\"negative\":true},{\"code\":\"494\",\"negative\":true},{\"code\":\"804\"}],[{\"code\":\"460\",\"negative\":true},{\"code\":\"494\",\"negative\":true},{\"code\":\"805\"}]],\"colors\":[{\"codeValidity\":\"001A/101A/108A/201A/208A/801A/807A/821A/854A/701A/501A/521A/507A/554A;\",\"codeValidityDetails\":[{\"code\":\"001A\",\"dateFrom\":\"2009-02-23\",\"name\":\"STOFF - SCHWARZ / ANTHRAZIT\"},{\"code\":\"101A\",\"dateFrom\":\"2010-01-25\",\"name\":\"KUNSTLEDER - SCHWARZ / ANTHRAZIT\"},{\"code\":\"108A\",\"dateFrom\":\"2010-01-25\",\"name\":\"KUNSTLEDER - GRAU\"},{\"code\":\"201A\",\"dateFrom\":\"2007-07-02\",\"name\":\"LEDER - SCHWARZ / ANTHRAZIT\"},{\"code\":\"208A\",\"dateFrom\":\"2009-02-23\",\"name\":\"LEDER - GRAU\"},{\"code\":\"501A\",\"dateFrom\":\"2010-03-04\",\"name\":\"LEDER EXCLUSIV - SCHWARZ / ANTHRAZIT\"},{\"code\":\"507A\",\"dateFrom\":\"2010-03-04\",\"name\":\"LEDER EXCLUSIV - ROT\"},{\"code\":\"521A\",\"dateFrom\":\"2013-10-21\",\"name\":\"LEDER EXCLUSIV - SCHWARZ / ANTHRAZIT\"},{\"code\":\"554A\",\"dateFrom\":\"2010-03-04\",\"name\":\"LEDER EXCLUSIV - BRAUN\"},{\"code\":\"701A\",\"dateFrom\":\"2009-12-16\",\"name\":\"LEDERKOMBINATION - SCHWARZ / ANTHRAZIT\"},{\"code\":\"801A\",\"dateFrom\":\"2006-08-04\",\"name\":\"LEDER / NAPPA / SEMIANILIN - SCHWARZ / ANTHRAZIT\"},{\"code\":\"807A\",\"dateFrom\":\"2009-02-23\",\"name\":\"LEDER / NAPPA / SEMIANILIN - ROT\"},{\"code\":\"821A\",\"dateFrom\":\"2013-06-24\",\"name\":\"LEDER / NAPPA / SEMIANILIN - SCHWARZ / ANTHRAZIT\"},{\"code\":\"854A\",\"dateFrom\":\"2009-02-23\",\"name\":\"LEDER / NAPPA / SEMIANILIN - BRAUN\"}],\"codeValidityMatrix\":[[{\"code\":\"001A\"}],[{\"code\":\"101A\"}],[{\"code\":\"108A\"}],[{\"code\":\"201A\"}],[{\"code\":\"208A\"}],[{\"code\":\"801A\"}],[{\"code\":\"807A\"}],[{\"code\":\"821A\"}],[{\"code\":\"854A\"}],[{\"code\":\"701A\"}],[{\"code\":\"501A\"}],[{\"code\":\"521A\"}],[{\"code\":\"507A\"}],[{\"code\":\"554A\"}]],\"es2Key\":\"9H14\",\"plantInformation\":[{\"date\":\"2010-01-01\",\"plant\":\"F\",\"type\":\"von\"},{\"date\":\"2016-02-28\",\"modelYear\":\"807\",\"modelYearDetails\":[{\"code\":\"807\",\"dateFrom\":\"2011-05-19\",\"name\":\"AEJ 16/1\"}],\"plant\":\"F\",\"type\":\"bis\"}]}]}}");
    }

    public void testGetPartInfoMaxModelTimeSliceForColorTable() {
        // DAIMLER-6972: Zeitscheibenüberlappung der Farb-/Variantentabelle mit maximaler BM-Zeitscheibe aller Baumuster durchführen
        executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"partContext\":{\"moduleId\":\"66W_69_045_00001\",\"sequenceId\":\"00085\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C219354\",\"productId\":\"66W\"}}",
                          "{\"partInfo\":{\"colors\":[{\"es2Key\":\"9051\",\"plantInformation\":[{\"date\":\"2005-06-30\",\"ident\":\"A038332\",\"modelYear\":\"807\",\"modelYearDetails\":[{\"code\":\"807\",\"dateFrom\":\"2002-12-10\",\"name\":\"AEJ 06/1\"}],\"plant\":\"A\",\"type\":\"von\"}]}]}}");
    }

    // Tests für WebService: [GetMaterialNav]
    public void testGetMaterialNavForPKWDefault() {
        executeWebservice(iPartsWSGetMaterialNavEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"productId\":\"598\",\"assortmentClassId\":\"P\",\"user\":{\"country\":\"200\",\"userId\":\"userId\"}}",
                          "{\"nextNodes\":[{\"id\":\"01\",\"label\":\"GROUP SURVEY\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"10\",\"label\":\"TOUCH-UP PENCILS\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"11\",\"label\":\"SPRAY CANS\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"13\",\"label\":\"ADHESIVES/PRIMER\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"14\",\"label\":\"BODY REPAIR WORK MATERIAL\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"17\",\"label\":\"SEALING COMPOUND\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"18\",\"label\":\"ENGINE OIL\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"19\",\"label\":\"SEALING TAPES/CORDS\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"20\",\"label\":\"ENGINE COOLING SERVICE MATERIAL\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"21\",\"label\":\"EXTERIOR CAR CARE\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"22\",\"label\":\"INTERIOR CAR CARE\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"24\",\"label\":\"GRAISSES/PASTES\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"26\",\"label\":\"GEAR OIL / HYDRAULIC OIL\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"42\",\"label\":\"BRAKES SERVICE MATERIAL\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"47\",\"label\":\"FUEL SYSTEM SERVICE MATERIAL\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"49\",\"label\":\"EXHAUST SYSTEM SERVICE MATERIAL\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"54\",\"label\":\"ELECTROLYTES AND ELECT. LINE\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"67\",\"label\":\"WINDOW INSTALLATION SERVICE MATERIAL\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"83\",\"label\":\"AIR CONDITIONING SERVICE MATERIAL\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"86\",\"label\":\"WINDSHIELD WASHER SYSTEM SERVICE MATERIAL\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"92\",\"label\":\"TARPAULINS\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"96\",\"label\":\"SPECIAL CUSTOMERS\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"99\",\"label\":\"NEW AS TEST\",\"partsAvailable\":true,\"type\":\"cg_group\"}]}");
    }

    public void testGetMaterialNavForLKWGerman() {
        executeWebservice(iPartsWSGetMaterialNavEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"productId\":\"598\",\"assortmentClassId\":\"L\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"nextNodes\":[{\"id\":\"01\",\"label\":\"GRUPPENUEBERSICHT\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"10\",\"label\":\"LACKSTIFTE\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"11\",\"label\":\"SPRAYDOSEN\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"13\",\"label\":\"KLEBSTOFFE/PRIMER\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"14\",\"label\":\"ARBEITSMATERIAL KAROSSERIEREPARATUR\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"17\",\"label\":\"DICHTMITTEL UND KLEBER FUER AGGREGAT\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"18\",\"label\":\"MOTOROEL\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"19\",\"label\":\"ABDICHTBAENDER / -SCHNUERE\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"20\",\"label\":\"SERVICEMATERIAL MOTORKUEHLUNG\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"21\",\"label\":\"AUTOPFLEGE EXTERIEUR\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"22\",\"label\":\"AUTOPFLEGE INTERIEUR\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"24\",\"label\":\"FETTE / PASTEN\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"26\",\"label\":\"GETRIEBEOELE UND HYDRAULIKOELE\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"35\",\"label\":\"HINTERACHSE\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"42\",\"label\":\"SERVICEMATERIAL BREMSEN\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"46\",\"label\":\"000001\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"47\",\"label\":\"SERVICEMATERIAL KRAFTSTOFFANLAGE\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"49\",\"label\":\"SERVICEMATERIAL ABGASANLAGE\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"54\",\"label\":\"BATTERIESAEURE UND ELEKT. LEITUNGEN\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"67\",\"label\":\"SERVICEMATERIAL EINGLASUNG\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"72\",\"label\":\"TUEREN\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"83\",\"label\":\"SERVICEMATERIAL KLIMAANLAGE\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"86\",\"label\":\"SERVICEMATERIAL SCHEIBENWASCHANLAGE\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"96\",\"label\":\"SONDERKUNDEN\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"99\",\"label\":\"TEST AS NEU\",\"partsAvailable\":true,\"type\":\"cg_group\"}]}");
    }

    public void testGetMaterialNavForKgPKW() {
        executeWebservice(iPartsWSGetMaterialNavEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"productId\":\"598\",\"navContext\":[{\"id\": \"10\",\"type\": \"cg_group\"}],\"assortmentClassId\":\"P\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"nextNodes\":[{\"id\":\"598_10_001_00001\",\"label\":\"ALLGEMEINTEILE\",\"partsAvailable\":true,\"type\":\"module\"}]}");
    }

    public void testGetMaterialNavForKgTuPKW() {
        executeWebservice(iPartsWSGetMaterialNavEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"productId\":\"598\",\"navContext\":[{\"id\": \"10\",\"type\": \"cg_group\"},{\"id\": \"001\",\"type\": \"cg_subgroup\"}],\"assortmentClassId\":\"P\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}} ",
                          "{\"nextNodes\":[{\"id\":\"598_10_001_00001\",\"label\":\"ALLGEMEINTEILE\",\"partsAvailable\":true,\"type\":\"module\"}]}");
    }

    public void testGetMaterialNavForInvalidKgPKW() {
        executeWebservice(iPartsWSGetMaterialNavEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"productId\":\"598\",\"navContext\":[{\"id\": \"35\",\"type\": \"cg_group\"}],\"assortmentClassId\":\"P\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"nextNodes\":[]}");
    }

    public void testGetMaterialNavForTransporterAssortmentClass() {
        executeWebservice(iPartsWSGetMaterialNavEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"productId\":\"598\",\"assortmentClassId\":\"T\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"nextNodes\":[{\"id\":\"96\",\"label\":\"SONDERKUNDEN\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"99\",\"label\":\"TEST AS NEU\",\"partsAvailable\":true,\"type\":\"cg_group\"}]}");
    }

    public void testGetMaterialNavProductClassIdSmart() {
        executeWebservice(iPartsWSGetMaterialNavEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"productId\":\"598\",\"productClassId\":\"F\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"nextNodes\":[{\"id\":\"01\",\"label\":\"GRUPPENUEBERSICHT\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"10\",\"label\":\"LACKSTIFTE\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"13\",\"label\":\"KLEBSTOFFE/PRIMER\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"14\",\"label\":\"ARBEITSMATERIAL KAROSSERIEREPARATUR\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"17\",\"label\":\"DICHTMITTEL UND KLEBER FUER AGGREGAT\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"18\",\"label\":\"MOTOROEL\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"19\",\"label\":\"ABDICHTBAENDER / -SCHNUERE\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"20\",\"label\":\"SERVICEMATERIAL MOTORKUEHLUNG\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"21\",\"label\":\"AUTOPFLEGE EXTERIEUR\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"22\",\"label\":\"AUTOPFLEGE INTERIEUR\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"24\",\"label\":\"FETTE / PASTEN\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"26\",\"label\":\"GETRIEBEOELE UND HYDRAULIKOELE\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"42\",\"label\":\"SERVICEMATERIAL BREMSEN\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"47\",\"label\":\"SERVICEMATERIAL KRAFTSTOFFANLAGE\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"54\",\"label\":\"BATTERIESAEURE UND ELEKT. LEITUNGEN\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"83\",\"label\":\"SERVICEMATERIAL KLIMAANLAGE\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"86\",\"label\":\"SERVICEMATERIAL SCHEIBENWASCHANLAGE\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"96\",\"label\":\"SONDERKUNDEN\",\"partsAvailable\":true,\"type\":\"cg_group\"},{\"id\":\"99\",\"label\":\"TEST AS NEU\",\"partsAvailable\":true,\"type\":\"cg_group\"}]}");
    }

    public void testGetMaterialNavForInvalidAssortmentClass() {
        executeWebservice(iPartsWSGetMaterialNavEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"productId\":\"598\",\"assortmentClassId\":\"A\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"code\":4000,\"message\":\"Attribute 'assortmentClassId' must be one of [G, L, P, F, T, U]\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }


    // Tests für WebService: [GetMaterialParts]
    public void testGetMaterialParts() {
        executeWebservice(iPartsWSGetMaterialPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"TRKA_tec_00\",\"language\":\"de\",\"country\":\"200\"},\"productId\":\"598\",\"assortmentClassId\":\"P\",\"navContext\":[{\"type\":\"cg_group\",\"id\":\"47\"}]}",
                          DWFile.get(getTestWorkingDir(), "resultGetMaterialParts.txt"));
    }

    public void testGetMaterialPartsSmart() {
        executeWebservice(iPartsWSGetMaterialPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"TRKA_tec_00\",\"language\":\"de\",\"country\":\"200\"},\"productId\":\"598\",\"assortmentClassId\":\"F\",\"navContext\":[{\"type\":\"cg_group\",\"id\":\"47\"}]}",
                          "{\"images\":[],\"parts\":[{\"calloutId\":\"-----\",\"description\":\"100 ML\",\"footNotes\":[{\"id\":\"050\",\"text\":\"BEI TRANSPORT, LAGERUNG UND VERARBEITUNG GEFAHRGUT VORSCHRIFT BZW. GEFAHRSTOFFVERORDNUNG BEACHTEN\",\"type\":\"text\"}],\"level\":\"01\",\"name\":\"KRFTST.ADDITIV\",\"partContext\":{\"moduleId\":\"598_47_001_00001\",\"sequenceId\":\"00011\"},\"partNo\":\"A0009893045\",\"partNoFormatted\":\"A 000 989 30 45\",\"quantity\":\"1\",\"shelflife\":\"24\"},{\"calloutId\":\"-----\",\"description\":\"5 L\",\"footNotes\":[{\"id\":\"050\",\"text\":\"BEI TRANSPORT, LAGERUNG UND VERARBEITUNG GEFAHRGUT VORSCHRIFT BZW. GEFAHRSTOFFVERORDNUNG BEACHTEN\",\"type\":\"text\"}],\"level\":\"01\",\"name\":\"KRFTST.ADDITIV\",\"partContext\":{\"moduleId\":\"598_47_001_00001\",\"sequenceId\":\"00012\"},\"partNo\":\"A0009893145\",\"partNoFormatted\":\"A 000 989 31 45\",\"quantity\":\"1\"}]}");
    }

    public void testGetMaterialPartsInvalidAssortmentClass() {
        executeWebservice(iPartsWSGetMaterialPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"TRKA_tec_00\",\"language\":\"de\",\"country\":\"200\"},\"productId\":\"598\",\"assortmentClassId\":\"P\",\"navContext\":[{\"type\":\"cg_group\",\"id\":\"46\"}]}",
                          "{\"code\":4001,\"message\":\"Unable to find module for product \\\"598\\\" and given navContext\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testGetMaterialPartsInvalidCallWithBothClassIds() {
        executeWebservice(iPartsWSGetMaterialPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"TRKA_tec_00\",\"language\":\"de\",\"country\":\"200\"},\"productId\":\"598\",\"assortmentClassId\":\"L\", \"productClassId\":\"L\",\"navContext\":[{\"type\":\"cg_group\",\"id\":\"35\"}]}",
                          "{\"code\":4003,\"message\":\"More than one of the exclusive attributes [assortmentClassId, productClassId] is not empty: [L, L]\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testGetMaterialPartsIdentContextAndDataCard() {
        // Land ES und Code 2U6 auf der Datenkarte -> Datensätze mit lfdNr 00001 (Land DE) und 00004 (Code xyz) werden ausgefiltert
        executeWebservice(iPartsWSGetMaterialPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"TRKA_tec_00\",\"language\":\"de\",\"country\":\"ES\"},\"productId\":\"598\",\"assortmentClassId\":\"L\",\"navContext\":[{\"type\":\"cg_group\",\"id\":\"35\"}],\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205004\",\"productId\":\"C01\",\"fin\":\"WDD2050041F004362\",\"datacardExists\":true}}",
                          DWFile.get(getTestWorkingDir(), "resultGetMaterialPartsIdentContextAndDataCard.txt"));
    }

    public void testGetMaterialPartsIdentContextWithoutDataCard() {
        // Keine erweiterte Filterung, weil Datenkarte nicht vorhanden
        executeWebservice(iPartsWSGetMaterialPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"TRKA_tec_00\",\"language\":\"de\",\"country\":\"ES\"},\"productId\":\"598\",\"assortmentClassId\":\"L\",\"navContext\":[{\"type\":\"cg_group\",\"id\":\"35\"}],\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205004\",\"productId\":\"C01\",\"fin\":\"WDD2050041F004362\",\"datacardExists\":false}}",
                          DWFile.get(getTestWorkingDir(), "resultGetMaterialPartsIdentContextWithoutDataCard.txt"));
    }

    // DAIMLER-14638
    public void testGetMaterialPartsES1ES2PictureAvailable() {
        executeWebservice(iPartsWSGetMaterialPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"productId\":\"598\",\"productClassId\":\"P\",\"navContext\":[{\"type\":\"cg_group\",\"id\":\"99\"}],\"user\":{\"country\":\"DE\",\"language\":\"de\",\"userId\":\"userId\"}}",
                          "{\"images\":[],\"parts\":[{\"calloutId\":\"?\",\"es1Key\":\"11\",\"name\":\"\",\"partContext\":{\"moduleId\":\"598_99_999_00001\",\"sequenceId\":\"00001\"},\"partNo\":\"A0009895404  11  FLEM\",\"partNoFormatted\":\"A0009895404  11  FLEM\",\"pictureAvailable\":true,\"quantity\":\"NB\"}]}");
    }

    // Tests für WebService: [GetMaterialPartInfo]
    public void testGetMaterialPartInfoWithSortedPrimusColors() {
        executeWebservice(iPartsWSGetMaterialPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"TRKA_tec_00\",\"language\":\"de\",\"country\":\"200\"},\"partContext\":{\"moduleId\":\"598_10_001_00001\",\"sequenceId\":\"00004\"},\"productId\":\"598\"}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartInfoWithSortedPrimusColors.txt"));
    }

    // Tests für WebService: [SearchMaterialParts]
    public void testSearchMaterialParts() {
        executeWebservice(iPartsWSSearchMaterialPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"TRKA_tec_00\",\"language\":\"de\",\"country\":\"200\"},\"searchText\":\"TS LACK*\",\"navContext\":[{\"type\":\"cg_group\",\"id\":\"10\"}],\"productId\":\"598\",\"assortmentClassId\":\"F\"}",
                          "{\"searchResults\":[{\"assortmentClassIds\":[\"F\"],\"name\":\"TS LACKSTIFT\",\"navContext\":[{\"id\":\"10\",\"label\":\"LACKSTIFTE\",\"type\":\"cg_group\"},{\"id\":\"001\",\"label\":\"ALLGEMEINTEILE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}],\"partNo\":\"A0009862650\",\"partNoFormatted\":\"A 000 986 26 50\",\"pictureAvailable\":true}]}");
    }

    public void testSearchMaterialPartsWrongAssormentClass() {
        executeWebservice(iPartsWSSearchMaterialPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"TRKA_tec_00\",\"language\":\"de\",\"country\":\"200\"},\"searchText\":\"KLEBER\",\"navContext\":[{\"type\":\"cg_group\",\"id\":\"13\"}],\"productId\":\"598\",\"assortmentClassId\":\"F\"}",
                          "{\"searchResults\":[]}");
    }

    public void testSearchMaterialPartsAssormentClassNotInASProductClasses() {
        executeWebservice(iPartsWSSearchMaterialPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"TRKA_tec_00\",\"language\":\"de\",\"country\":\"200\"},\"searchText\":\"KLEBER\",\"navContext\":[{\"type\":\"cg_group\",\"id\":\"13\"}],\"productId\":\"598\",\"assortmentClassId\":\"K\"}",
                          "{\"code\":4000,\"message\":\"Attribute 'assortmentClassId' must be one of [G, L, P, F, T, U]\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testSearchMaterialPartsWrongProductId() {
        executeWebservice(iPartsWSSearchMaterialPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"TRKA_tec_00\",\"language\":\"de\",\"country\":\"200\"},\"searchText\":\"KLEBER\",\"navContext\":[{\"type\":\"cg_group\",\"id\":\"13\"}],\"productId\":\"599\",\"assortmentClassId\":\"F\"}",
                          "{\"code\":4001,\"message\":\"Invalid special product '599'\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testSearchMaterialPartsMinimalRequest() {
        executeWebservice(iPartsWSSearchMaterialPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"TRKA_tec_00\",\"language\":\"de\",\"country\":\"200\"},\"searchText\":\"PASTE\",\"productId\":\"598\"}",
                          "{\"searchResults\":[{\"assortmentClassIds\":[\"L\"],\"description\":\"100 G\",\"name\":\"PASTE\",\"navContext\":[{\"id\":\"24\",\"label\":\"FETTE / PASTEN\",\"type\":\"cg_group\"},{\"id\":\"001\",\"label\":\"GUMMILAGER AN MOTORTRAEGER\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}],\"partNo\":\"A0009895151\",\"partNoFormatted\":\"A 000 989 51 51\"},{\"assortmentClassIds\":[\"L\"],\"description\":\"100G\",\"name\":\"PASTE\",\"navContext\":[{\"id\":\"35\",\"label\":\"HINTERACHSE\",\"type\":\"cg_group\"},{\"id\":\"001\",\"label\":\"HINTERACHSE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}],\"partNo\":\"A0009895151\",\"partNoFormatted\":\"A 000 989 51 51\"},{\"assortmentClassIds\":[\"L\"],\"description\":\"100G\",\"name\":\"PASTE\",\"navContext\":[{\"id\":\"72\",\"label\":\"TUEREN\",\"type\":\"cg_group\"},{\"id\":\"025\",\"label\":\"TUEREN\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}],\"partNo\":\"A0009895151\",\"partNoFormatted\":\"A 000 989 51 51\"}]}");
    }

    // Backslash im Suchfeld sollte nicht mit java.sql.SQLDataException: ORA-01424 enden
    public void testSearchMaterialPartsBackslashInSearchText() {
        executeWebservice(iPartsWSSearchMaterialPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"TRKA_tec_00\",\"language\":\"de\",\"country\":\"200\"},\"searchText\":\"00\\\\30*\",\"navContext\":[{\"type\":\"cg_group\",\"id\":\"10\"}],\"productId\":\"598\",\"assortmentClassId\":\"F\"}", "{\"searchResults\":[]}");
    }

    public void testSearchMaterialWithoutBothClassIds() {
        executeWebservice(iPartsWSSearchMaterialPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"TRKA_tec_00\",\"language\":\"de\",\"country\":\"200\"},\"searchText\":\"TS LACK*\",\"navContext\":[{\"type\":\"cg_group\",\"id\":\"10\"}],\"productId\":\"598\"}", "{\"searchResults\":[{\"assortmentClassIds\":[\"P\",\"L\"],\"name\":\"TS LACKSTIFT\",\"navContext\":[{\"id\":\"10\",\"label\":\"LACKSTIFTE\",\"type\":\"cg_group\"},{\"id\":\"001\",\"label\":\"ALLGEMEINTEILE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}],\"partNo\":\"A0009862550\",\"partNoFormatted\":\"A 000 986 25 50\"},{\"assortmentClassIds\":[\"P\",\"L\"],\"name\":\"TS LACKSTIFT\",\"navContext\":[{\"id\":\"10\",\"label\":\"LACKSTIFTE\",\"type\":\"cg_group\"},{\"id\":\"001\",\"label\":\"ALLGEMEINTEILE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}],\"partNo\":\"A0009862650\",\"partNoFormatted\":\"A 000 986 26 50\",\"pictureAvailable\":true},{\"assortmentClassIds\":[\"F\"],\"name\":\"TS LACKSTIFT\",\"navContext\":[{\"id\":\"10\",\"label\":\"LACKSTIFTE\",\"type\":\"cg_group\"},{\"id\":\"001\",\"label\":\"ALLGEMEINTEILE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}],\"partNo\":\"A0009862650\",\"partNoFormatted\":\"A 000 986 26 50\",\"pictureAvailable\":true}]}");
    }

    public void testSearchMaterialPartsIdentContextAndDataCard() {
        // Land DE und Code 2U6 auf der Datenkarte -> Datensätze mit lfdNr 00003 (Land ES) und 00004 (Code xyz) werden ausgefiltert
        executeWebservice(iPartsWSSearchMaterialPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"TRKA_tec_00\",\"language\":\"de\",\"country\":\"DE\"},\"searchText\":\"A0*989*\",\"navContext\":[{\"type\":\"cg_group\",\"id\":\"35\"}],\"productId\":\"598\",\"assortmentClassId\":\"L\",\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205004\",\"productId\":\"C01\",\"fin\":\"WDD2050041F004362\",\"datacardExists\":true}}",
                          "{\"searchResults\":[{\"assortmentClassIds\":[\"L\"],\"description\":\"100G\",\"name\":\"PASTE\",\"navContext\":[{\"id\":\"35\",\"label\":\"HINTERACHSE\",\"type\":\"cg_group\"},{\"id\":\"001\",\"label\":\"HINTERACHSE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}],\"partNo\":\"A0009895151\",\"partNoFormatted\":\"A 000 989 51 51\"},{\"assortmentClassIds\":[\"L\"],\"description\":\"50G\",\"name\":\"DICHTUNGSMASSE\",\"navContext\":[{\"id\":\"35\",\"label\":\"HINTERACHSE\",\"type\":\"cg_group\"},{\"id\":\"001\",\"label\":\"HINTERACHSE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}],\"partNo\":\"A0029897120\",\"partNoFormatted\":\"A 002 989 71 20\"}]}");
    }

    // Tests für Datenkarten-WebServices
    public void testVehicleDatacards() {
        try {
            String baseURI = "http://" + getWebserviceHost() + ":" + getWebservicePort() + iPartsWSDatacardsSimulationEndpoint.DEFAULT_ENDPOINT_URI;
            String language = getProject().getDBLanguage(); // "DE"
            iPartsDataCardRetrievalHelper.DatacardType type = iPartsDataCardRetrievalHelper.DatacardType.VEHICLE;

            // Vorhandene Datenkarten in Untervezeichnissen bzw. im Hauptverzeichnis für die Datenkarten-Simulation
            String fin = "WDB9630031L738999";
            String datacardJson = iPartsDataCardRetrievalHelper.getJsonFromWebservice(baseURI, fin, language, type);
            iPartsWSvehicleInclMasterData dataCard = iPartsDataCardRetrievalHelper.getResponseAsVehicleDatacardJSONObject(datacardJson, new FinId(fin));
            assertNotNull(dataCard);
            assertEquals("WDB9630031L738999", dataCard.getFin());

            fin = "WD444770313138415";
            datacardJson = iPartsDataCardRetrievalHelper.getJsonFromWebservice(baseURI, fin, language, type);
            dataCard = iPartsDataCardRetrievalHelper.getResponseAsVehicleDatacardJSONObject(datacardJson, new FinId(fin));
            assertNotNull(dataCard);
            assertEquals("WD444770313138415", dataCard.getFin());

            // Nicht vorhandene Datenkarte
            fin = "WDB9630031L738997";
            datacardJson = iPartsDataCardRetrievalHelper.getJsonFromWebservice(baseURI, fin, language, type);
            assertNull(datacardJson);

            // Ungültige FIN
            fin = "Ungültig";
            datacardJson = iPartsDataCardRetrievalHelper.getJsonFromWebservice(baseURI, fin, language, type);
            assertNull(datacardJson);
        } catch (DataCardRetrievalException e) {
            fail(e.getMessage());
        }
    }

    public void testFixingPartsForVehicles() {
        try {
            String baseURI = "http://" + getWebserviceHost() + ":" + getWebservicePort() + iPartsWSDatacardsSimulationEndpoint.DEFAULT_ENDPOINT_URI;
            String language = getProject().getDBLanguage(); // "DE"
            iPartsDataCardRetrievalHelper.DatacardType type = iPartsDataCardRetrievalHelper.DatacardType.FIXING_PARTS;

            // Vorhandene Befestigungsteile in Untervezeichnissen bzw. im Hauptverzeichnis für die Datenkarten-Simulation
            String fin = "WDB9644262L990584";
            String datacardJson = iPartsDataCardRetrievalHelper.getJsonFromWebservice(baseURI, fin, language, type);
            iPartsWSvehicle vehicleJSON = iPartsDataCardRetrievalHelper.getResponseAsFixingPartsJSONObject(datacardJson, new FinId(fin));

            assertNotNull(vehicleJSON.getChassisHoleInformation());
            assertEquals(378, vehicleJSON.getChassisHoleInformation().size());
            iPartsWSchassisHoleInformation chassisHoleInformation = vehicleJSON.getChassisHoleInformation().get(0);
            assertNotNull(chassisHoleInformation);
            assertEquals(2, chassisHoleInformation.getFixingParts().size());
            // Check, ob FixingPart gesetzt ist
            iPartsWSfixingParts fixingPart = chassisHoleInformation.getFixingParts().get(0);
            assertNotNull(fixingPart);
            assertEquals("A0209905301", fixingPart.getPartNumber());

            // Nicht vorhandene Befestigungsteile
            fin = "WDB9630031L738998";
            datacardJson = iPartsDataCardRetrievalHelper.getJsonFromWebservice(baseURI, fin, language, type);
            assertNull(datacardJson);

            // Ungültige FIN
            fin = "Ungültig";
            datacardJson = iPartsDataCardRetrievalHelper.getJsonFromWebservice(baseURI, fin, language, type);
            assertNull(datacardJson);
        } catch (DataCardRetrievalException e) {
            fail(e.getMessage());
        }
    }

    public void testAggregateDatacards() {
        try {
            String baseURI = "http://" + getWebserviceHost() + ":" + getWebservicePort() + iPartsWSDatacardsSimulationEndpoint.DEFAULT_ENDPOINT_URI;
            String language = getProject().getDBLanguage(); // "DE"
            iPartsDataCardRetrievalHelper.DatacardType type = iPartsDataCardRetrievalHelper.DatacardType.AGGREGATE;

            // Vorhandene Aggregatedatenkarte für Abgassystem
            String ident = "93073010224672";
            DCAggregateTypes identType = DCAggregateTypes.AFTER_TREATMENT_SYSTEM;
            String request = identType.getJsonName() + "/" + ident;
            String datacardJson = iPartsDataCardRetrievalHelper.getJsonFromWebservice(baseURI, request, language, type);
            iPartsWSactiveGeneralMajAssy jsonObject = iPartsDataCardRetrievalHelper.getResponseAsAggregateDatacardJSONObject(datacardJson, identType, ident);

            assertNotNull(jsonObject.getAfterTreatmentSystem());
            assertEquals(jsonObject.getAfterTreatmentSystem().getId(), ident);

            // falscher ident
            ident = "93073010224673";
            request = identType.getJsonName() + "/" + ident;
            datacardJson = iPartsDataCardRetrievalHelper.getJsonFromWebservice(baseURI, request, language, type);
            assertNull(datacardJson);

            // falscher type
            ident = "93073010224672";
            identType = DCAggregateTypes.AXLE;
            request = identType.getJsonName() + "/" + ident;
            datacardJson = iPartsDataCardRetrievalHelper.getJsonFromWebservice(baseURI, request, language, type);
            assertNull(datacardJson);

        } catch (DataCardRetrievalException e) {
            fail(e.getMessage());
        }
    }


    // Tests für Webservice: [SearchComponent]
    public void testSearchComponentFin() {
        executeWebservice(iPartsWSsearchComponentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C967607\",\"productId\":\"S10\",\"fin\":\"WDB9676071L972285\"},\"searchText\":\"509361/30\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"searchResults\":[{\"identContext\":{\"aggTypeId\":\"F\",\"filterOptions\":{\"model\":true,\"saVersion\":true},\"fin\":\"WDB9676071L972285\",\"modelId\":\"C967607\",\"productClassIds\":[\"L\"],\"productId\":\"S10\"},\"navContext\":[{\"id\":\"47\",\"label\":\"KRAFTSTOFFANLAGE\",\"type\":\"cg_group\"},{\"id\":\"060\",\"label\":\"KRAFTSTOFFLEITUNGEN UND ENTLUEFTUNG\",\"type\":\"cg_subgroup\"},{\"id\":\"S10_47_060_00001\",\"label\":\"KRAFTSTOFFLEITUNGEN UND ENTLUEFTUNG\",\"partsAvailable\":true,\"type\":\"module\"}]}]}");
    }

    public void testSearchComponentBMIncludeBMwithSAAInAgg() {
        executeWebservice(iPartsWSsearchComponentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C967607\",\"productId\":\"S10\",\"fin\":\"WDB9676071L972285\",\"datacardExists\":\"true\"},\"searchText\":\"Z 506.389/01\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"searchResults\":[{\"identContext\":{\"aggTypeId\":\"F\",\"datacardExists\":true,\"filterOptions\":{\"model\":true,\"saVersion\":true},\"fin\":\"WDB9676071L972285\",\"modelId\":\"C967607\",\"productClassIds\":[\"L\"],\"productId\":\"S10\"},\"navContext\":[{\"id\":\"58\",\"label\":\"WERKZEUG UND ZUBEHOER\",\"type\":\"cg_group\"},{\"id\":\"Z 506.389\",\"label\":\"SV WARNLEUCHTE\",\"partsAvailable\":true,\"type\":\"sa_number\"}]}]}");
    }

    public void testSearchComponentBMIncludeBMwithSAAInAggNoDatacard() {
        executeWebservice(iPartsWSsearchComponentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C967607\",\"productId\":\"S10\"},\"searchText\":\"Z 506.389/01\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"searchResults\":[{\"identContext\":{\"aggTypeId\":\"F\",\"filterOptions\":{\"model\":true,\"saVersion\":true},\"modelId\":\"C967607\",\"productClassIds\":[\"L\"],\"productId\":\"S10\"},\"navContext\":[{\"id\":\"58\",\"label\":\"WERKZEUG UND ZUBEHOER\",\"type\":\"cg_group\"},{\"id\":\"Z 506.389\",\"label\":\"SV WARNLEUCHTE\",\"partsAvailable\":true,\"type\":\"sa_number\"}]}]}");
    }

    public void testSearchComponentBMIncludeBM() {
        executeWebservice(iPartsWSsearchComponentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C967607\",\"productId\":\"S10\",\"fin\":\"WDB9676071L972285\"},\"searchText\":\"540025/19\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"searchResults\":[{\"identContext\":{\"aggTypeId\":\"M\",\"filterOptions\":{\"model\":true,\"saVersion\":true},\"modelDesc\":\"OM 936\",\"modelId\":\"D936910\",\"modelRemarks\":\"DIESELMOTOR, MIT AUFLADUNG, LADELUFTKUEHLUNG\",\"modelTypeId\":\"D936\",\"productClassIds\":[\"F\",\"M\",\"S\",\"O\",\"E\"],\"productClassNames\":[\"Smart\",\"\",\"\",\"Bus\",\"\"],\"productId\":\"M01\"},\"navContext\":[{\"id\":\"18\",\"label\":\"MOTORSCHMIERUNG\",\"type\":\"cg_group\"},{\"id\":\"045\",\"label\":\"OELFILTER UND OELKUEHLER\",\"type\":\"cg_subgroup\"},{\"id\":\"M01_18_045_00001\",\"label\":\"OELFILTER UND OELKUEHLER\",\"partsAvailable\":true,\"type\":\"module\"}]},{\"identContext\":{\"aggTypeId\":\"M\",\"filterOptions\":{\"model\":true,\"saVersion\":true},\"modelDesc\":\"OM 936\",\"modelId\":\"D936910\",\"modelRemarks\":\"DIESELMOTOR, MIT AUFLADUNG, LADELUFTKUEHLUNG\",\"modelTypeId\":\"D936\",\"productClassIds\":[\"F\",\"M\",\"S\",\"O\",\"E\"],\"productClassNames\":[\"Smart\",\"\",\"\",\"Bus\",\"\"],\"productId\":\"M01\"},\"navContext\":[{\"id\":\"20\",\"label\":\"MOTORKUEHLUNG\",\"type\":\"cg_group\"},{\"id\":\"015\",\"label\":\"WASSERPUMPE\",\"type\":\"cg_subgroup\"},{\"id\":\"M01_20_015_00001\",\"label\":\"WASSERPUMPE\",\"partsAvailable\":true,\"type\":\"module\"}]}]}");
    }

    public void testSearchComponentBMWithoutBM() {
        executeWebservice(iPartsWSsearchComponentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C967607\",\"productId\":\"S10\",\"fin\":\"WDB9676071L972285\"},\"searchText\":\"540025/19\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"},\"includeAggs\":false}",
                          "{\"searchResults\":[]}");
    }

    public void testSearchComponentAggBM() {
        executeWebservice(iPartsWSsearchComponentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"M\",\"productClassIds\":[\"F\"],\"modelId\":\"D936910\",\"productId\":\"M01\"},\"searchText\":\"Z54002519\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"searchResults\":[{\"identContext\":{\"aggTypeId\":\"M\",\"filterOptions\":{\"model\":true,\"saVersion\":true},\"modelId\":\"D936910\",\"productClassIds\":[\"F\"],\"productId\":\"M01\"},\"navContext\":[{\"id\":\"18\",\"label\":\"MOTORSCHMIERUNG\",\"type\":\"cg_group\"},{\"id\":\"045\",\"label\":\"OELFILTER UND OELKUEHLER\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}]},{\"identContext\":{\"aggTypeId\":\"M\",\"filterOptions\":{\"model\":true,\"saVersion\":true},\"modelId\":\"D936910\",\"productClassIds\":[\"F\"],\"productId\":\"M01\"},\"navContext\":[{\"id\":\"20\",\"label\":\"MOTORKUEHLUNG\",\"type\":\"cg_group\"},{\"id\":\"015\",\"label\":\"WASSERPUMPE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}]}]}");
    }

    public void testSearchComponentBK() {
        executeWebservice(iPartsWSsearchComponentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C967000\",\"productId\":\"S10\"},\"searchText\":\"A0009906155\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"searchResults\":[{\"identContext\":{\"aggTypeId\":\"F\",\"filterOptions\":{\"model\":true,\"saVersion\":true},\"modelId\":\"C967000\",\"productClassIds\":[\"L\"],\"productId\":\"S10\"},\"navContext\":[{\"id\":\"31\",\"label\":\"RAHMEN\",\"type\":\"cg_group\"},{\"id\":\"120\",\"label\":\"RAHMEN VOLLSTAENDIG,VERSCHRAUBUNGSTEILE\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}]}]}");
    }

    public void testSearchComponentSAA() {
        // Nur ein Treffer, obwohl es eigentlich auch im S10 - 31 und F10 - 61 Treffer gibt. Problem liegt allgemein an
        // der SA Suche, weil wir nur eine Verortung für SAAs können. (SearchParts macht das gleiche)
        executeWebservice(iPartsWSsearchComponentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"L\"],\"modelId\":\"C967001\",\"productId\":\"S10\"},\"searchText\":\"Z M02.521/01\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"},\"includeSAs\":false}",
                          "{\"searchResults\":[]}");
    }

    public void testSearchComponentSAAIncludeSAAs() {
        // Nur ein Treffer, obwohl es eigentlich auch im S10 - 31 und F10 - 61 Treffer gibt. Problem liegt allgemein an
        // der SA Suche, weil wir nur eine Verortung für SAAs können. (SearchParts macht das gleiche)
        executeWebservice(iPartsWSsearchComponentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C967607\",\"productId\":\"S10\",\"fin\":\"WDB9676071L972285\",\"datacardExists\":\"true\"},\"searchText\":\"Z 506.389/01\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"searchResults\":[{\"identContext\":{\"aggTypeId\":\"F\",\"datacardExists\":true,\"filterOptions\":{\"model\":true,\"saVersion\":true},\"fin\":\"WDB9676071L972285\",\"modelId\":\"C967607\",\"productClassIds\":[\"P\"],\"productId\":\"S10\"},\"navContext\":[{\"id\":\"58\",\"label\":\"WERKZEUG UND ZUBEHOER\",\"type\":\"cg_group\"},{\"id\":\"Z 506.389\",\"label\":\"SV WARNLEUCHTE\",\"partsAvailable\":true,\"type\":\"sa_number\"}]}]}");
    }

    public void testSearchComponentSAAIncludeSAAsNoDatacard() {
        executeWebservice(iPartsWSsearchComponentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C967607\",\"productId\":\"S10\"},\"searchText\":\"Z 506.389/01\",\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"searchResults\":[{\"identContext\":{\"aggTypeId\":\"F\",\"filterOptions\":{\"model\":true,\"saVersion\":true},\"modelId\":\"C967607\",\"productClassIds\":[\"P\"],\"productId\":\"S10\"},\"navContext\":[{\"id\":\"58\",\"label\":\"WERKZEUG UND ZUBEHOER\",\"type\":\"cg_group\"},{\"id\":\"Z 506.389\",\"label\":\"SV WARNLEUCHTE\",\"partsAvailable\":true,\"type\":\"sa_number\"}]}]}");
    }

    public void testSearchComponentSAAFilteredOut() {
        executeWebservice(iPartsWSsearchComponentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C967000\",\"productId\":\"S10\"},\"searchText\":\"ZM0264301\"," +
                          "\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"searchResults\":[]}");
    }

    // Backslash im Suchfeld sollte nicht mit java.sql.SQLDataException: ORA-01424 enden
    public void testSearchComponentBackslashInSearchText() {
        executeWebservice(iPartsWSsearchComponentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"M\",\"productClassIds\":[\"P\"],\"modelId\":\"D936910\",\"productId\":\"M01\"},\"searchText\":\"00\\\\30*\"," +
                          "\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"searchResults\":[]}");
    }

    // Notizen
    // Ident WS hier nicht separat abgetestet, da Testfälle bei testIdentByFINAutoProdSelectIdentCheck, testIdentByFINDefault, testIdentByVINFallbackToModel enthalten
    public void testNotesGetNavOptsForTU() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205002\",\"productId\":\"C01\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"24\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"nextNodes\":[{\"id\":\"015\",\"label\":\"MOTORAUFHAENGUNG\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}]}");
    }

    public void testNotesGetNavOptsForKG() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205002\",\"productId\":\"C01\"},\"navContext\":[],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"nextNodes\":[{\"id\":\"24\",\"label\":\"MOTORAUFHAENGUNG\",\"type\":\"cg_group\"},{\"id\":\"26\",\"label\":\"SCHALTUNG\",\"type\":\"cg_group\"},{\"id\":\"27\",\"label\":\"AUTOMATISCHES MB-GETRIEBE\",\"type\":\"cg_group\"},{\"id\":\"29\",\"label\":\"PEDALANLAGE\",\"type\":\"cg_group\"},{\"id\":\"30\",\"label\":\"REGULIERUNG\",\"type\":\"cg_group\"},{\"id\":\"31\",\"label\":\"ANHAENGEVORRICHTUNG\",\"type\":\"cg_group\"},{\"id\":\"32\",\"label\":\"FEDERN,AUFHAENGUNG UND HYDRAULIK\",\"type\":\"cg_group\"},{\"id\":\"33\",\"label\":\"VORDERACHSE\",\"type\":\"cg_group\"},{\"id\":\"35\",\"label\":\"HINTERACHSE\",\"type\":\"cg_group\"},{\"id\":\"40\",\"label\":\"RAEDER\",\"type\":\"cg_group\"},{\"id\":\"41\",\"label\":\"GELENKWELLE\",\"type\":\"cg_group\"},{\"id\":\"42\",\"label\":\"BREMSANLAGE\",\"type\":\"cg_group\"},{\"id\":\"46\",\"label\":\"LENKUNG\",\"type\":\"cg_group\"},{\"id\":\"47\",\"label\":\"KRAFTSTOFFANLAGE\",\"type\":\"cg_group\"},{\"id\":\"49\",\"label\":\"AUSPUFFANLAGE\",\"type\":\"cg_group\"},{\"id\":\"50\",\"label\":\"KUEHLER\",\"type\":\"cg_group\"},{\"id\":\"52\",\"label\":\"FAHRGESTELLBLECHTEILE / LUFTANSAUGUNG\",\"type\":\"cg_group\"},{\"id\":\"54\",\"label\":\"ELEKTRISCHE AUSRUESTUNG UND INSTRUMENTE\",\"type\":\"cg_group\"},{\"id\":\"58\",\"label\":\"WERKZEUG UND ZUBEHOER\",\"type\":\"cg_group\"},{\"id\":\"60\",\"label\":\"ROHBAU\",\"type\":\"cg_group\"},{\"id\":\"61\",\"label\":\"UNTERBAU\",\"type\":\"cg_group\"},{\"id\":\"62\",\"label\":\"VORBAU,VORDERWAND\",\"type\":\"cg_group\"},{\"id\":\"63\",\"label\":\"SEITENWAENDE\",\"type\":\"cg_group\"},{\"id\":\"64\",\"label\":\"HECK\",\"type\":\"cg_group\"},{\"id\":\"65\",\"label\":\"DACH\",\"type\":\"cg_group\"},{\"id\":\"67\",\"label\":\"FENSTERANLAGE\",\"type\":\"cg_group\"},{\"id\":\"68\",\"label\":\"VERKLEIDUNG\",\"type\":\"cg_group\"},{\"id\":\"69\",\"label\":\"VERKLEIDUNG UND AUSSCHLAG\",\"type\":\"cg_group\"},{\"id\":\"72\",\"label\":\"FAHRERTUEREN\",\"type\":\"cg_group\"},{\"id\":\"73\",\"label\":\"FONDTUEREN\",\"type\":\"cg_group\"},{\"id\":\"75\",\"label\":\"HECKDECKEL\",\"type\":\"cg_group\"},{\"id\":\"78\",\"label\":\"SCHIEBEDACH\",\"type\":\"cg_group\"},{\"id\":\"82\",\"label\":\"ELEKTRISCHE ANLAGE\",\"type\":\"cg_group\"},{\"id\":\"83\",\"label\":\"HEIZUNG UND LUEFTUNG\",\"type\":\"cg_group\"},{\"id\":\"86\",\"label\":\"WASCHANLAGE\",\"type\":\"cg_group\"},{\"id\":\"88\",\"label\":\"ANBAUTEILE\",\"type\":\"cg_group\"},{\"id\":\"91\",\"label\":\"FAHRERSITZ\",\"type\":\"cg_group\"},{\"id\":\"92\",\"label\":\"FONDSITZ\",\"type\":\"cg_group\"},{\"id\":\"98\",\"label\":\"ZUBEHOER\",\"type\":\"cg_group\"},{\"id\":\"99\",\"label\":\"SONDEREINBAUTEN\",\"type\":\"cg_group\"}]}");
    }

    public void testGetNavOptsForProductsWithVisibleSAs() {
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"VA\",\"productClassIds\":[\"\"],\"modelId\":\"D737220\",\"productId\":\"37T\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"42\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          DWFile.get(getTestWorkingDir(), "resultGetNavOptsForProductsWithVisibleSAs.txt"));
    }

    public void testNotesGetPartsWithImage() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205002\",\"productId\":\"C01\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"21\"},{\"type\":\"cg_subgroup\",\"id\":\"230\"}],\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"images\":[{\"href\":\"/parts/media/drawing_B21230000090\",\"id\":\"drawing_B21230000090\",\"notes\":[{\"text\":\"Noitz zum Bild C01 21 230\"}],\"previewHref\":\"/parts/media/previews/drawing_B21230000090\"}],\"parts\":[{\"calloutId\":\"10\",\"codeValidity\":\"M651;\",\"damageCodes\":[\"20240\"],\"level\":\"01\",\"name\":\"\",\"notes\":[{\"text\":\"Notiz zur Stücklistenposition 10 \"}],\"partContext\":{\"moduleId\":\"C01_21_230_00001\",\"sequenceId\":\"00006\"},\"partNo\":\"A0039932196\",\"partNoFormatted\":\"A 003 993 21 96\",\"quantity\":\"1\"}]}");
    }

    public void testNotesGetPartsEnglish() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205002\",\"productId\":\"C01\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"21\"},{\"type\":\"cg_subgroup\",\"id\":\"230\"}],\"user\":{\"country\":\"200\",\"language\":\"en\",\"userId\":\"TRKA_tec_00\"}}",
                          "{\"images\":[{\"href\":\"/parts/media/drawing_B21230000090\",\"id\":\"drawing_B21230000090\",\"notes\":[{\"text\":\"Noitz zum Bild C01 21 230\"}],\"previewHref\":\"/parts/media/previews/drawing_B21230000090\"}],\"parts\":[{\"calloutId\":\"10\",\"codeValidity\":\"M651;\",\"damageCodes\":[\"20240\"],\"level\":\"01\",\"name\":\"\",\"notes\":[{\"text\":\"This is the english note for partlist pos 10\"}],\"partContext\":{\"moduleId\":\"C01_21_230_00001\",\"sequenceId\":\"00006\"},\"partNo\":\"A0039932196\",\"partNoFormatted\":\"A 003 993 21 96\",\"quantity\":\"1\"}]}");
    }


    // Tests für Webservice: [partsList]

    /**
     * Jetzt werden hier zusätzlich die EinPAS-Daten, je nach RMI-Konfigurationsschalter, weggelassen oder dazugemischt.
     */
    public void testPartsListForModel() {
        // Token enthält Sprachen de, en, fr, d.h. de ist die Hauptsprache und en, fr sind die Rückfallsprachen
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");
        Map<UniversalConfigOption, Boolean> configOptions = new HashMap<>();
        String modelAddOn = "?model=C205003";

        try {
            // Ohne gesetzten Schalter werden die Ergebnisdaten MIT den EinPAS-Daten angereichert ausgegeben:
            configOptions.put(iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI, false);
            executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), configOptions, new WebserviceRunnable() {
                @Override
                public void executeTest() {
                    executeWebservice(iPartsWSPartsListEndpoint.DEFAULT_ENDPOINT_URI + modelAddOn,
                                      null, additionalRequestProperties,
                                      DWFile.get(getTestWorkingDir(), "resultPartsListForModel.txt"));
                }
            });

            // Mit gesetztem Schalter werden die Ergebnisdaten OHNE die EinPAS-Daten ausgegeben.
            configOptions.put(iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI, true);
            executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), configOptions, new WebserviceRunnable() {
                @Override
                public void executeTest() {
                    executeWebservice(iPartsWSPartsListEndpoint.DEFAULT_ENDPOINT_URI + modelAddOn,
                                      null, additionalRequestProperties,
                                      DWFile.get(getTestWorkingDir(), "resultPartsListForModelRMIreduced.txt"));
                }
            });
        } finally {
            configOptions.put(iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI, false);
        }
    }

    public void testPartsListDBFallbackLanguageFromToken() {
        // Token enthält Sprachen zh, fr, es, d.h. zh ist die Hauptsprache und fr, es sind die Rückfallsprachen
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiemgiLCJsYW5nMiI6ImZyIiwibGFuZzMiOiJlcyIsImV4cCI6MzY5MTgwNTAwMH0.Y5aIDmSHc4uHrNGvWbkqSofHD6UL1TT6fvyc3iHw-_I");

        executeWebserviceWithoutTokenPermissionsCheck(iPartsWSPartsListEndpoint.DEFAULT_ENDPOINT_URI + "?model=C205003",
                                                      null, additionalRequestProperties,
                                                      DWFile.get(getTestWorkingDir(), "resultPartsListDBFallbackLanguageFromToken.txt"));
    }

    public void testPartsListForAmbiguousProducts() {
        // Token enthält Sprachen de, en, fr, d.h. de ist die Hauptsprache und en, fr sind die Rückfallsprachen
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");

        executeWebservice(iPartsWSPartsListEndpoint.DEFAULT_ENDPOINT_URI + "?model=C205240",
                          null, additionalRequestProperties,
                          "{\"ambiguousProductIds\":[\"C22\",\"TestOmittedParts\"]}", HttpConstants.HTTP_STATUS_OK);
    }

    public void testPartsListFINWithProduct() {
        // Token enthält Sprachen zh, fr, es, d.h. zh ist die Hauptsprache und fr, es sind die Rückfallsprachen
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");

        executeWebservice(iPartsWSPartsListEndpoint.DEFAULT_ENDPOINT_URI + "?finOrVin=WDD2050041F004362&productId=C01",
                          null, additionalRequestProperties,
                          DWFile.get(getTestWorkingDir(), "resultPartsListFINWithProduct.txt"));
    }

    public void testPartsListFINWithTextIds() {
        // Token enthält Sprachen zh, fr, es, d.h. zh ist die Hauptsprache und fr, es sind die Rückfallsprachen
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");
        // Das ist die gleiche Anfrage wie bei "testPartsListFINWithProduct()".
        // Zusätzlich wurde hier der Parameter "extendedDescriptions" auf "true" gesetzt, damit wir die Texte samt Text-Ids bekommen
        executeWebservice(iPartsWSPartsListEndpoint.DEFAULT_ENDPOINT_URI + "?finOrVin=WDD2050041F004362&productId=C01&extendedDescriptions=true",
                          null, additionalRequestProperties,
                          DWFile.get(getTestWorkingDir(), "resultPartsListFINWithProductWithTextIds.txt"));
    }

    public void testPartsListFINWithTextIdsAggregate() {
        // Token enthält Sprachen zh, fr, es, d.h. zh ist die Hauptsprache und fr, es sind die Rückfallsprachen
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");
        // Das ist die gleichen Anfrage wie bei "testPartsListAggregate()".
        // Zusätzlich wurde hier der Parameter "extendedDescriptions" auf "true" gesetzt, damit wir die Texte samt Text-Ids bekommen
        executeWebservice(iPartsWSPartsListEndpoint.DEFAULT_ENDPOINT_URI + "?finOrVin=71168001095329&extendedDescriptions=true",
                          null, additionalRequestProperties,
                          DWFile.get(getTestWorkingDir(), "resultPartsListAggregateWithTextIds.txt"));
    }

    public void testPartsListExtendedDescriptionsSequenceIdPart1() {
        // Token enthält Sprachen zh, fr, es, d.h. zh ist die Hauptsprache und fr, es sind die Rückfallsprachen
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");

        // --- TEST (1) ---
        // Ohne "extendedDescriptions"
        executeWebservice(iPartsWSPartsListEndpoint.DEFAULT_ENDPOINT_URI + "?finOrVin=71168001095329",
                          null, additionalRequestProperties,
                          DWFile.get(getTestWorkingDir(), "resultPartsListNOExtendedDescriptions.txt"));
    }

    public void testPartsListExtendedDescriptionsSequenceIdPart2() {
        // Token enthält Sprachen zh, fr, es, d.h. zh ist die Hauptsprache und fr, es sind die Rückfallsprachen
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");

        // --- TEST (2) ---
        // "extendedDescriptions = false"
        executeWebservice(iPartsWSPartsListEndpoint.DEFAULT_ENDPOINT_URI + "?finOrVin=71168001095329&extendedDescriptions=false",
                          null, additionalRequestProperties,
                          DWFile.get(getTestWorkingDir(), "resultPartsListExtendedDescriptionsFALSESequenceId.txt"));
    }

    public void testPartsListExtendedDescriptionsSequenceIdPart3() {
        // Token enthält Sprachen zh, fr, es, d.h. zh ist die Hauptsprache und fr, es sind die Rückfallsprachen
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");

        // --- TEST (3) ---
        // "extendedDescriptions = true"
        executeWebservice(iPartsWSPartsListEndpoint.DEFAULT_ENDPOINT_URI + "?finOrVin=71168001095329&extendedDescriptions=true",
                          null, additionalRequestProperties,
                          DWFile.get(getTestWorkingDir(), "resultPartsListExtendedDescriptionsTRUEequenceId.txt"));
    }

    public void testPartsListReducedInformationPart1() {
        // Token enthält Sprachen zh, fr, es, d.h. zh ist die Hauptsprache und fr, es sind die Rückfallsprachen
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");

        // Mit "reducedInformation = true"
        executeWebservice(iPartsWSPartsListEndpoint.DEFAULT_ENDPOINT_URI + "?finOrVin=71168001095329&reducedInformation=true",
                          null, additionalRequestProperties,
                          DWFile.get(getTestWorkingDir(), "resultPartsListReducedInformation.txt"));
    }

    public void testPartsListReducedInformationPart2() {
        // Token enthält Sprachen zh, fr, es, d.h. zh ist die Hauptsprache und fr, es sind die Rückfallsprachen
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");

        // "extendedDescriptions = true UND reducedInformation = true" - soll einen HTTP Fehlercode 400 - Bad Request zurückliefern
        executeWebservice(iPartsWSPartsListEndpoint.DEFAULT_ENDPOINT_URI + "?finOrVin=71168001095329&reducedInformation=true&extendedDescriptions=true",
                          null, additionalRequestProperties,
                          "{\"code\":4003,\"message\":\"More than one of the exclusive attributes [extendedDescriptions, reducedInformation] is true\"}",
                          HttpConstants.HTTP_STATUS_BAD_REQUEST);
    }

    public void testPartsListFINWithProductAndAggs() {
        // Token enthält Sprachen zh, fr, es, d.h. zh ist die Hauptsprache und fr, es sind die Rückfallsprachen
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");

        executeWebservice(iPartsWSPartsListEndpoint.DEFAULT_ENDPOINT_URI + "?finOrVin=WDD2050041F004362&productId=C01&includeAggs=true",
                          null, additionalRequestProperties,
                          DWFile.get(getTestWorkingDir(), "resultPartsListFINWithProductAndAggs.txt"));
    }


    // s. DAIMLER-16365
    public void testPartsListAggregateSAWithProductInfo() {
        // Token enthält Sprachen de, en, fr, d.h. de ist die Hauptsprache und en, fr sind die Rückfallsprachen
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");

        executeWebservice(iPartsWSPartsListEndpoint.DEFAULT_ENDPOINT_URI + "?finOrVin=WDB9670271L896656&productId=S10&includeAggs=true",
                          null, additionalRequestProperties,
                          DWFile.get(getTestWorkingDir(), "resultPartsListAggregateSAWithProductInfo.txt"));
    }

    public void testPartsListAggregate() {
        // Token enthält Sprachen zh, fr, es, d.h. zh ist die Hauptsprache und fr, es sind die Rückfallsprachen
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");

        executeWebservice(iPartsWSPartsListEndpoint.DEFAULT_ENDPOINT_URI + "?finOrVin=71168001095329",
                          null, additionalRequestProperties,
                          DWFile.get(getTestWorkingDir(), "resultPartsListAggregate.txt"));
    }

    public void testPartsListForSpecialCatalog() {
        // Token enthält Sprachen de, en, fr, d.h. de ist die Hauptsprache und en, fr sind die Rückfallsprachen
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");

        executeWebservice(iPartsWSPartsListEndpoint.DEFAULT_ENDPOINT_URI + "?productClassId=P&productId=598&extendedDescriptions=true",
                          null, additionalRequestProperties,
                          DWFile.get(getTestWorkingDir(), "resultPartsListForSpecialCatalog.txt"));
    }

    public void testWiringHarnessKit() {
        // Die Admin-Option "Leitungssatz-Baukasten Teilepositionen filtern" muss für diesen Testfall temporär aktiviert werden
        UniversalConfiguration pluginConfig = iPartsPlugin.getPluginConfig();
        boolean filterWireHarness = pluginConfig.getConfig().getBoolean(pluginConfig.getPath() + iPartsPlugin.CONFIG_FILTER_WIRE_HARNESS_PARTS.getKey(),
                                                                        false);
        writeBooleanConfigValues(pluginConfig, true, iPartsPlugin.CONFIG_FILTER_WIRE_HARNESS_PARTS);
        try {
            clearWebservicePluginsCaches();
            EtkDataAssembly.clearGlobalEntriesCache();
            iPartsFilter.get().clearCacheData();

            // Token enthält Sprachen zh, fr, es, d.h. zh ist die Hauptsprache und fr, es sind die Rückfallsprachen
            Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");

            executeWebservice(iPartsWSPartsListEndpoint.DEFAULT_ENDPOINT_URI + "?model=C205002&productId=C01&extendedDescriptions=true",
                              null, additionalRequestProperties,
                              DWFile.get(getTestWorkingDir(), "resultPartsListWiringHarnessKit.txt"));

            // Test, ob V-Teile durch Nachfolger ersetzt werden. In der DB gibt es ein V-Teile Mapping von A9240101005
            // auf A2529202600 und A2538881301, d.h. es werden die gemappten Teilenummern ausgegeben
            executeWebservice(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"partContext\":{\"moduleId\":\"D29_54_058_00001\",\"sequenceId\":\"00001\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C292356\",\"productId\":\"D29\",\"modelTypeId\":\"C292\",\"filterOptions\":{\"model\":true}}," +
                              "\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"}}",
                              DWFile.get(getTestWorkingDir(), "resultGetPartInfoWireHarnessSimplifiedParts.txt"));
        } catch (Exception e) {
            printWireHarnessData();
        } finally {
            writeBooleanConfigValues(pluginConfig, filterWireHarness, iPartsPlugin.CONFIG_FILTER_WIRE_HARNESS_PARTS);
            clearWebservicePluginsCaches();
            EtkDataAssembly.clearGlobalEntriesCache();
            iPartsFilter.get().clearCacheData();
        }
    }

    /**
     * Test-Code für den testWiringHarnessKit Test
     */
    private void printWireHarnessData() {
        System.out.println("Wire harness config value: " + iPartsWireHarnessHelper.isWireHarnessFilterConfigActive());
        EtkProject project = getProject();
        iPartsWireHarness wireHarnessCache = iPartsWireHarness.getInstance(project);
        String originalMatNumber = "A1669067701";
        if (wireHarnessCache.isWireHarness(originalMatNumber)) {
            List<iPartsDataWireHarness> dataWireHarnessList = iPartsFilterHelper.getFilteredWireHarnessComponent(project, originalMatNumber,
                                                                                                                 iPartsWSWireHarnessHelper.WIRING_HARNESS_DISPLAY_FIELDS);
            if (!dataWireHarnessList.isEmpty()) {
                List<iPartsWSWiringHarness> iPartsWSWiringHarnessList = iPartsWSWireHarnessHelper.fillWiringHarnessKit(project,
                                                                                                                       dataWireHarnessList,
                                                                                                                       true, null, null, "L");
                if (iPartsWSWiringHarnessList.isEmpty()) {
                    System.out.println("No components after filling");
                }
            } else {
                System.out.println("No filtered wire harness components");
                iPartsDataWireHarnessList oneWireHarness = iPartsDataWireHarnessList.loadOneWireHarness(project, originalMatNumber,
                                                                                                        iPartsWSWireHarnessHelper.WIRING_HARNESS_DISPLAY_FIELDS);
                System.out.println("Load one wire harness result size: " + oneWireHarness.size());
                if (oneWireHarness.size() == 1) {
                    iPartsDataWireHarness wireHarness = oneWireHarness.get(0);
                    String[] multiLangAndArrayFields = iPartsWSWireHarnessHelper.WIRING_HARNESS_DISPLAY_FIELDS.getFields()
                            .stream()
                            .filter(field -> field.isArray() || field.isMultiLanguage())
                            .map(field -> field.getKey().getFieldName())
                            .toArray(String[]::new);
                    List<iPartsDataWireHarness> result = iPartsFilterHelper.getValidWireHarnessComponent(project, wireHarness,
                                                                                                         multiLangAndArrayFields);
                    System.out.println("List size after filtering wire harness components: " + result.size() + "; Wire harness id: "
                                       + wireHarness.getAsId().toStringForLogMessages());

                    Optional<List<EtkDataPart>> simplifiedParts
                            = iPartsWireHarnessSimplifiedParts.getInstance(project).getSimplifiedPartsForWHPart(wireHarness.getAsId().getSubSnr(),
                                                                                                                multiLangAndArrayFields);
                    System.out.println("Simplified parts result size: " + (simplifiedParts.isPresent() ? simplifiedParts.get().size() : "0")
                                       + ";  SubSnr: " + wireHarness.getAsId().getSubSnr());
                }
            }
        } else {
            System.out.println("No wireHarnessCache entry");
        }
    }

    public void testPartsListEinPasDAIMLER11967() {
        // Token enthält Sprachen zh, fr, es, d.h. zh ist die Hauptsprache und fr, es sind die Rückfallsprachen
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");

        executeWebservice(iPartsWSPartsListEndpoint.DEFAULT_ENDPOINT_URI + "?model=C166823&productId=D81&extendedDescriptions=true",
                          null, additionalRequestProperties,
                          DWFile.get(getTestWorkingDir(), "resultPartsListEinPasDAIMLER11967.txt"));
    }

    public void testGetPartsEinPasDAIMLER13600() {
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"country\":\"200\",\"language\":\"de\",\"userId\":\"userId\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"T\"],\"modelId\":\"C166823\",\"productId\":\"D81\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"82\"},{\"type\":\"cg_subgroup\",\"id\":\"175\"}]}",
                          DWFile.get(getTestWorkingDir(), "resultGetPartsEinPasDAIMLER13600.txt"));
    }

    public void testPartsListImagesDAIMLER12186() {
        // Token enthält Sprachen zh, fr, es, d.h. zh ist die Hauptsprache und fr, es sind die Rückfallsprachen
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");

        executeWebservice(iPartsWSPartsListEndpoint.DEFAULT_ENDPOINT_URI + "?model=C205003&productId=C01&images=true",
                          null, additionalRequestProperties,
                          DWFile.get(getTestWorkingDir(), "resultPartsListImagesDAIMLER12186.txt"));
    }

    // Test, auf "modelRemarks" in unterschiedlichen Sprachen unter verschiedenen Voraussetzungen
    public void testIPartsWSIdentModelRemarksPart1() {

        // Vorher die Prüfung der Marken und Produktgruppen abschalten
        executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_PERMISSIONS,
                                            false, () -> {
                    // --- TEST (1) ---
                    // Token enthält die Sprache "country": "DE", Anforderung die Sprache "language":"en" ==> Englischer Text mindestens bei "modelRemarks"
                    Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjoxNTkxODkwNDM0fQ.QWD9MPpCNv4BAvnGNHmO6bX6Wt11cqadYNLwLexKMYg");

                    executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                      "{\"user\":{\"userId\":\"user\",\"language\":\"en\",\"country\":\"200\"},\"identCode\":\"C204000\"}", additionalRequestProperties,
                                      DWFile.get(getTestWorkingDir(), "resultWSIdentModelRemarks.txt"));
                });
    }


    public void testIPartsWSIdentModelRemarksPart2() {
        // Vorher die Prüfung der Marken und Produktgruppen abschalten
        executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_PERMISSIONS,
                                            false, () -> {
                    // --- TEST (2) ---
                    // Token enthält die Sprache "country": "DE", Anforderung die Sprache "language":"ru" ==> russischer Text mindestens bei "modelRemarks"
                    // !!!ACHTUNG!!! Die russischen Zeichen werden nur als UTF-8 richtig dargestellt.
                    Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjoxNTkxODkwNDM0fQ.QWD9MPpCNv4BAvnGNHmO6bX6Wt11cqadYNLwLexKMYg");

                    executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                      "{\"user\":{\"userId\":\"user\",\"language\":\"ru\",\"country\":\"200\"},\"identCode\":\"C204000\"}", additionalRequestProperties,
                                      DWFile.get(getTestWorkingDir(), "resultWSIdentModelRemarksRussianLang.txt"));
                });
    }

    public void testIPartsWSIdentModelRemarksPart3() {
        // Vorher die Prüfung der Marken und Produktgruppen abschalten
        executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_PERMISSIONS,
                                            false, () -> {
                    // --- TEST (3) ---
                    // Token enthält die Sprache "country": "EN", Anforderung die Sprache "language":"pt" ==> Portugiesischer Text mindestens bei "modelRemarks"
                    Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJFTiIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjoxNTkxODkwNDM0fQ.8QmqRk7HzG4DM9MnNU6c4gE3Pr3jC6IeWs5VtdsSob4");

                    executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                      "{\"user\":{\"userId\":\"user\",\"language\":\"pt\",\"country\":\"200\"},\"identCode\":\"C204022\"}", additionalRequestProperties,
                                      "{\"identContexts\":[{\"aggregates\":[{\"aggTypeId\":\"GM\",\"modelDesc\":\"NSG 510\",\"modelId\":\"D711670\",\"modelTypeId\":\"D711\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"Carro\"],\"productId\":\"00P\"}],\"aggTypeId\":\"F\",\"integratedNavigationAvailable\":true,\"modelDesc\":\"C 320/350 CDI\",\"modelId\":\"C204022\",\"modelRemarks\":\"CARROSSERIA DE STATION WAGON,DISTANCIA ENTRE EIXOS,2400 MM\",\"modelTypeId\":\"C204\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"Carro\"],\"productId\":\"C204\"},{\"aggTypeId\":\"M\",\"modelDesc\":\"C 320/350 CDI\",\"modelId\":\"C204022\",\"modelRemarks\":\"CARROSSERIA DE STATION WAGON,DISTANCIA ENTRE EIXOS,2400 MM\",\"modelTypeId\":\"C204\",\"productClassIds\":[\"L\"],\"productClassNames\":[\"Caminhão\"],\"productId\":\"C204_BRANCH_TRUCK\"},{\"aggTypeId\":\"F\",\"modelDesc\":\"C 320/350 CDI\",\"modelId\":\"C204022\",\"modelRemarks\":\"CARROSSERIA DE STATION WAGON,DISTANCIA ENTRE EIXOS,2400 MM\",\"modelTypeId\":\"C204\",\"productClassIds\":[\"F\"],\"productClassNames\":[\"Smart\"],\"productId\":\"C204_BRAND_SMT\"},{\"aggregates\":[{\"aggTypeId\":\"GM\",\"modelDesc\":\"NSG 510\",\"modelId\":\"D711670\",\"modelTypeId\":\"D711\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"Carro\"],\"productId\":\"00P\"}],\"aggTypeId\":\"F\",\"integratedNavigationAvailable\":true,\"modelDesc\":\"C 320/350 CDI\",\"modelId\":\"C204022\",\"modelRemarks\":\"CARROSSERIA DE STATION WAGON,DISTANCIA ENTRE EIXOS,2400 MM\",\"modelTypeId\":\"C204\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"Carro\"],\"productId\":\"C204_DE\",\"validCountries\":[\"DE\"]},{\"aggregates\":[{\"aggTypeId\":\"GM\",\"modelDesc\":\"NSG 510\",\"modelId\":\"D711670\",\"modelTypeId\":\"D711\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"Carro\"],\"productId\":\"00P\"}],\"aggTypeId\":\"F\",\"integratedNavigationAvailable\":true,\"modelDesc\":\"C 320/350 CDI\",\"modelId\":\"C204022\",\"modelRemarks\":\"CARROSSERIA DE STATION WAGON,DISTANCIA ENTRE EIXOS,2400 MM\",\"modelTypeId\":\"C204\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"Carro\"],\"productId\":\"C204_KGTU\"},{\"aggregates\":[{\"aggTypeId\":\"GM\",\"modelDesc\":\"NSG 510\",\"modelId\":\"D711670\",\"modelTypeId\":\"D711\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"Carro\"],\"productId\":\"00P\"}],\"aggTypeId\":\"F\",\"integratedNavigationAvailable\":true,\"invalidCountries\":[\"DE\"],\"modelDesc\":\"C 320/350 CDI\",\"modelId\":\"C204022\",\"modelRemarks\":\"CARROSSERIA DE STATION WAGON,DISTANCIA ENTRE EIXOS,2400 MM\",\"modelTypeId\":\"C204\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"Carro\"],\"productId\":\"C204_NOT_DE\"}]}",
                                      HttpConstants.HTTP_STATUS_OK);

                });
    }

    public void testIPartsWSIdentModelRemarksPart4() {
        // Vorher die Prüfung der Marken und Produktgruppen abschalten
        executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_PERMISSIONS,
                                            false, () -> {
                    // --- TEST (4) ---
                    // Token enthält KEINE Sprache, Anforderung die Sprache "language":"en" ==> Englische Texte werden erwartet mindestens bei "modelRemarks"
                    // Test mit einer FIN, Notizen sind in deutsch enthalten
                    Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjoxNTkxODkwNDM0fQ.F2e44DYOSdyxRkYftJ9KG1JoSXugd68vdMJ1IwDMPT8");

                    executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                      "{\"user\":{\"userId\":\"user\",\"language\":\"en\",\"country\":\"200\"},\"identCode\":\"WDD2050042R042987\"}", additionalRequestProperties,
                                      "{\"identContexts\":[{\"aggregates\":[{\"aggregateNumber\":\"65192132498132\",\"aggTypeId\":\"M\",\"modelId\":\"D651921\",\"modelRemarks\":\"ALLISON AUTOMATIC TRANSMISSION WITH RETARDER\",\"modelTypeId\":\"D651\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PC\"],\"productId\":\"69L\"}],\"aggTypeId\":\"F\",\"connectWireHarnessEnabled\":true,\"datacardExists\":true,\"fin\":\"WDD2050042R042987\",\"integratedNavigationAvailable\":true,\"mbOilSpecs\":[{\"quantity\":\"1,2\",\"spec\":\"222.2\",\"text\":\"Siehe SI18.00-P-1311A; Einschränkung: Nur SAE 0W-30 oder SAE 5W-30 verwenden!\",\"type\":\"ENGINE_OIL\"}],\"mbSpecs\":[{\"quantity\":\"1,2\",\"spec\":\"222.2\",\"text\":\"Siehe SI18.00-P-1311A; Einschränkung: Nur SAE 0W-30 oder SAE 5W-30 verwenden!\",\"type\":\"ENGINE_OIL\"}],\"modelId\":\"C205004\",\"modelRemarks\":\"TRUCK,5400-MM WHEELBASE\",\"modelTypeId\":\"C205\",\"notes\":[{\"text\":\"Notiz zum Produkt C01\\r\\n2te Zeile\"}],\"productClassIds\":[\"P\"],\"productClassNames\":[\"PC\"],\"productId\":\"C01\"}]}",
                                      HttpConstants.HTTP_STATUS_OK);

                });
    }

    public void testIPartsWSIdentMBOilSpecsTests_WITH_Texts() {
        // Token enthält KEINE Sprache, Anforderung die Sprache "language":"en" ==> Englische Texte werden erwartet mindestens bei "modelRemarks"
        // Test mit einer FIN, Notizen sind in deutsch enthalten
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");

        // Datenkarte [WDD2050042R042987]
        executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"user\":{\"userId\":\"user\",\"language\":\"en\",\"country\":\"200\"},\"identCode\":\"WDD2050042R042987\"}", additionalRequestProperties,
                          DWFile.get(getTestWorkingDir(), "resultWSIdentMBOilSpecsTests_WITH_Texts.txt"));
    }

    public void testIPartsWSIdentModelRemarksPart5() {
        // Vorher die Prüfung der Marken und Produktgruppen abschalten
        executeTestWithBooleanConfigChanges(iPartsWebservicePlugin.getPluginConfig(), iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_PERMISSIONS,
                                            false, () -> {
                    // --- TEST (5) ---
                    // Token enthält KEINE Sprache, Anforderung die Sprache "language":"fr" ==> Französische Texte werden erwartet mindestens bei "modelRemarks"
                    Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjoxNTkxODkwNDM0fQ.F2e44DYOSdyxRkYftJ9KG1JoSXugd68vdMJ1IwDMPT8");

                    executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                      "{\"user\":{\"userId\":\"user\",\"language\":\"fr\",\"country\":\"200\"},\"identCode\":\"204023\",\"productClassIds\":[\"P\"]}", additionalRequestProperties,
                                      "{\"identContexts\":[{\"aggregates\":[{\"aggTypeId\":\"GM\",\"modelDesc\":\"NSG 510\",\"modelId\":\"D711670\",\"modelTypeId\":\"D711\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"VP\"],\"productId\":\"00P\"}],\"aggTypeId\":\"F\",\"integratedNavigationAvailable\":true,\"modelDesc\":\"C 350 CDI\",\"modelId\":\"C204023\",\"modelRemarks\":\"MOTEUR DIESEL,100 KW (136 CH),A SURALIMENTATION,REFROIDISSEMENT D'AIR DE CHARGE\",\"modelTypeId\":\"C204\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"VP\"],\"productId\":\"C204\"},{\"aggregates\":[{\"aggTypeId\":\"GM\",\"modelDesc\":\"NSG 510\",\"modelId\":\"D711670\",\"modelTypeId\":\"D711\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"VP\"],\"productId\":\"00P\"}],\"aggTypeId\":\"F\",\"integratedNavigationAvailable\":true,\"modelDesc\":\"C 350 CDI\",\"modelId\":\"C204023\",\"modelRemarks\":\"MOTEUR DIESEL,100 KW (136 CH),A SURALIMENTATION,REFROIDISSEMENT D'AIR DE CHARGE\",\"modelTypeId\":\"C204\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"VP\"],\"productId\":\"C204_DE\",\"validCountries\":[\"DE\"]},{\"aggregates\":[{\"aggTypeId\":\"GM\",\"modelDesc\":\"NSG 510\",\"modelId\":\"D711670\",\"modelTypeId\":\"D711\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"VP\"],\"productId\":\"00P\"}],\"aggTypeId\":\"F\",\"integratedNavigationAvailable\":true,\"modelDesc\":\"C 350 CDI\",\"modelId\":\"C204023\",\"modelRemarks\":\"MOTEUR DIESEL,100 KW (136 CH),A SURALIMENTATION,REFROIDISSEMENT D'AIR DE CHARGE\",\"modelTypeId\":\"C204\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"VP\"],\"productId\":\"C204_KGTU\"},{\"aggregates\":[{\"aggTypeId\":\"GM\",\"modelDesc\":\"NSG 510\",\"modelId\":\"D711670\",\"modelTypeId\":\"D711\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"VP\"],\"productId\":\"00P\"}],\"aggTypeId\":\"F\",\"integratedNavigationAvailable\":true,\"invalidCountries\":[\"DE\"],\"modelDesc\":\"C 350 CDI\",\"modelId\":\"C204023\",\"modelRemarks\":\"MOTEUR DIESEL,100 KW (136 CH),A SURALIMENTATION,REFROIDISSEMENT D'AIR DE CHARGE\",\"modelTypeId\":\"C204\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"VP\"],\"productId\":\"C204_NOT_DE\"}]}",
                                      HttpConstants.HTTP_STATUS_OK);
                });
    }

    public void testiPartsWSVisualNav() {
        // Token enthält Sprachen de, en, fr, d.h. de ist die Hauptsprache und en, fr sind die Rückfallsprachen
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");

        iPartsDataModule module = new iPartsDataModule(getProject(), new iPartsModuleId("66U_Navigation"));
        assertTrue(module.existsInDB());

        try {
            // TEST (1):
            // * Einstieg mit Datenkarte
            // * DM_MODULE_HIDDEN = '1'
            // ==> "visualNavAvailable" soll NICHT ausgegeben werden.
            module.setFieldValueAsBoolean(iPartsConst.FIELD_DM_MODULE_HIDDEN, true, DBActionOrigin.FROM_EDIT);
            module.saveToDB();
            clearCachesForTestiPartsWSIdentVisualNavAvailable();

            executeWebservice(iPartsWSVisualNavEndpoint.DEFAULT_ENDPOINT_URI + "?fin=WDB2037081E047263",
                              null, additionalRequestProperties,
                              "{\"cgSubgroups\":[],\"navImages\":[{\"callouts\":[{\"id\":\"25\"},{\"id\":\"33\"},{\"id\":\"6\"},{\"id\":\"22\"},{\"id\":\"29\"},{\"id\":\"36\"},{\"id\":\"3\"},{\"id\":\"38\"},{\"id\":\"(28)\"},{\"id\":\"13\"},{\"id\":\"14\"},{\"id\":\"(25)\"},{\"id\":\"30\"},{\"id\":\"(4)\"},{\"id\":\"24\"},{\"id\":\"11\"},{\"id\":\"(25)\"},{\"id\":\"(28)\"},{\"id\":\"22\"},{\"id\":\"29\"},{\"id\":\"20\"},{\"id\":\"27\"},{\"id\":\"35\"},{\"id\":\"22\"},{\"id\":\"29\"},{\"id\":\"24\"},{\"id\":\"30\"},{\"id\":\"32\"},{\"id\":\"32\"},{\"id\":\"26\"},{\"id\":\"(21)\"},{\"id\":\"23\"},{\"id\":\"(28)\"},{\"id\":\"32\"},{\"id\":\"19\"},{\"id\":\"37\"},{\"id\":\"12\"},{\"id\":\"10\"},{\"id\":\"(21)\"},{\"id\":\"(28)\"},{\"id\":\"6\"},{\"id\":\"33\"},{\"id\":\"29\"},{\"id\":\"25\"},{\"id\":\"22\"},{\"id\":\"7\"},{\"id\":\"33\"},{\"id\":\"1\"},{\"id\":\"25\"},{\"id\":\"8\"},{\"id\":\"8\"},{\"id\":\"20\"},{\"id\":\"27\"},{\"id\":\"35\"},{\"id\":\"5\"},{\"id\":\"9\"},{\"id\":\"29\"},{\"id\":\"22\"}],\"href\":\"/parts/media/drawing_PV274.308.266.328_version_699\",\"id\":\"drawing_PV274.308.266.328_version_699\",\"previewHref\":\"/parts/media/previews/drawing_PV274.308.266.328_version_699\"},{\"callouts\":[{\"id\":\"520\"},{\"id\":\"500\"},{\"id\":\"420\"},{\"id\":\"400\"},{\"id\":\"250\"},{\"id\":\"240\"},{\"id\":\"230\"},{\"id\":\"220\"},{\"id\":\"200\"},{\"id\":\"100\"},{\"id\":\"80\"},{\"id\":\"60\"},{\"id\":\"40\"},{\"id\":\"20\"},{\"id\":\"300\"},{\"id\":\"310\"}],\"href\":\"/parts/media/drawing_PV274.308.266.328_version_699_usage_SVG\",\"id\":\"drawing_PV274.308.266.328_version_699_usage_SVG\",\"previewHref\":\"/parts/media/previews/drawing_PV274.308.266.328_version_699_usage_SVG\"}]}");

            // TEST (2):
            // * Einstieg mit Datenkarte
            // * DM_MODULE_HIDDEN = '0'
            // ==> "visualNavAvailable" soll ausgegeben werden.
            module.setFieldValueAsBoolean(iPartsConst.FIELD_DM_MODULE_HIDDEN, false, DBActionOrigin.FROM_EDIT);
            module.saveToDB();
            clearCachesForTestiPartsWSIdentVisualNavAvailable();

            executeWebservice(iPartsWSVisualNavEndpoint.DEFAULT_ENDPOINT_URI + "?fin=WDB2037081E047263",
                              null, additionalRequestProperties,
                              "{\"cgSubgroups\":[{\"calloutId\":\"25\",\"cg\":\"21\",\"csg\":\"300\",\"description\":\"ABSCHIRMUNGEN UND ABDAEMPFUNGEN AN MOTOR,GETRIEBE,VORDERACHSGETRIEBE\",\"images\":[{\"href\":\"/parts/media/drawing_PV274.308.266.328_version_699\",\"id\":\"drawing_PV274.308.266.328_version_699\",\"previewHref\":\"/parts/media/previews/drawing_PV274.308.266.328_version_699\"}],\"modelId\":\"C203708\",\"moduleId\":\"66U_21_300_00001\",\"productId\":\"66U\"},{\"calloutId\":\"33\",\"cg\":\"29\",\"csg\":\"015\",\"description\":\"PEDALANLAGE MIT LAGERUNG\",\"modelId\":\"C203708\",\"moduleId\":\"66U_29_015_00001\",\"productId\":\"66U\"}],\"navImages\":[{\"callouts\":[{\"active\":true,\"id\":\"25\"},{\"active\":true,\"id\":\"33\"},{\"id\":\"6\"},{\"id\":\"22\"},{\"id\":\"29\"},{\"id\":\"36\"},{\"id\":\"3\"},{\"id\":\"38\"},{\"id\":\"(28)\"},{\"id\":\"13\"},{\"id\":\"14\"},{\"id\":\"(25)\"},{\"id\":\"30\"},{\"id\":\"(4)\"},{\"id\":\"24\"},{\"id\":\"11\"},{\"id\":\"(25)\"},{\"id\":\"(28)\"},{\"id\":\"22\"},{\"id\":\"29\"},{\"id\":\"20\"},{\"id\":\"27\"},{\"id\":\"35\"},{\"id\":\"22\"},{\"id\":\"29\"},{\"id\":\"24\"},{\"id\":\"30\"},{\"id\":\"32\"},{\"id\":\"32\"},{\"id\":\"26\"},{\"id\":\"(21)\"},{\"id\":\"23\"},{\"id\":\"(28)\"},{\"id\":\"32\"},{\"id\":\"19\"},{\"id\":\"37\"},{\"id\":\"12\"},{\"id\":\"10\"},{\"id\":\"(21)\"},{\"id\":\"(28)\"},{\"id\":\"6\"},{\"active\":true,\"id\":\"33\"},{\"id\":\"29\"},{\"active\":true,\"id\":\"25\"},{\"id\":\"22\"},{\"id\":\"7\"},{\"active\":true,\"id\":\"33\"},{\"id\":\"1\"},{\"active\":true,\"id\":\"25\"},{\"id\":\"8\"},{\"id\":\"8\"},{\"id\":\"20\"},{\"id\":\"27\"},{\"id\":\"35\"},{\"id\":\"5\"},{\"id\":\"9\"},{\"id\":\"29\"},{\"id\":\"22\"}],\"href\":\"/parts/media/drawing_PV274.308.266.328_version_699\",\"id\":\"drawing_PV274.308.266.328_version_699\",\"previewHref\":\"/parts/media/previews/drawing_PV274.308.266.328_version_699\"},{\"callouts\":[{\"id\":\"520\"},{\"id\":\"500\"},{\"id\":\"420\"},{\"id\":\"400\"},{\"id\":\"250\"},{\"id\":\"240\"},{\"id\":\"230\"},{\"id\":\"220\"},{\"id\":\"200\"},{\"id\":\"100\"},{\"id\":\"80\"},{\"id\":\"60\"},{\"id\":\"40\"},{\"id\":\"20\"},{\"id\":\"300\"},{\"id\":\"310\"}],\"href\":\"/parts/media/drawing_PV274.308.266.328_version_699_usage_SVG\",\"id\":\"drawing_PV274.308.266.328_version_699_usage_SVG\",\"previewHref\":\"/parts/media/previews/drawing_PV274.308.266.328_version_699_usage_SVG\"}]}");

        } finally {
            // Die für den Test manipulierten Daten wieder zurücksetzen.
            module.setFieldValueAsBoolean(iPartsConst.FIELD_DM_MODULE_HIDDEN, true, DBActionOrigin.FROM_EDIT);
            module.saveToDB();
            clearCachesForTestiPartsWSIdentVisualNavAvailable();
        }
    }

    public void testiPartsDAIMLER15581WSgetNavOptsWithSpecialRight() {
        //        Produkt: 66D KGTU 24/015
        // MIT dem Recht "permissions": { "SPECIAL": [ "HighvoltbatteryRepair" ], ohne "country" ...
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb3VudHJ5IjoiREUiLCJzdWIiOiJ1c2VyIiwicGVybWlzc2lvbnMiOnsiU1BFQ0lBTCI6WyJIaWdodm9sdGJhdHRlcnlSZXBhaXIiXSwiTUIiOlsiQlVTIiwiVkFOIiwiVFJVQ0siLCJVTklNT0ciLCJQQVNTRU5HRVItQ0FSIl0sIk1ZQiI6WyJQQVNTRU5HRVItQ0FSIl0sIlNNVCI6WyJQQVNTRU5HRVItQ0FSIl19LCJpc3MiOiJpUGFydHNUZWFtIiwibGFuZzMiOiJGUiIsImxhbmcyIjoiREUiLCJsYW5nMSI6IkRFIiwiZXhwIjo5Njk2NjQ0NDA2LCJpYXQiOjE2ODc4ODc5Mjd9.M_otnScjN4UV938govYsHOkHwiHUtmYgQJxdDpd3oVU");
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C245207\",\"productId\":\"66D\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"26\"}],\"user\":{\"country\":\"200\",\"userId\":\"userId\"}}",
                          additionalRequestProperties,
                          // ACHTUNG: gleiches Ergebnis und gleiche Ergebnisdatei wie testiPartsDAIMLER15581WSgetNavOptsWithSpecialRight()
                          DWFile.get(getTestWorkingDir(), "resultDAIMLER15581WSgetNavOpts.txt"));
    }

    public void testiPartsDAIMLER15581WSgetNavOptsNoSpecialRight() {
        // OHNE das Recht "permissions": { "SPECIAL": [ "HighvoltbatteryRepair" ], "country" : "DE"...
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb3VudHJ5IjoiREUiLCJzdWIiOiJ1c2VyIiwicGVybWlzc2lvbnMiOnsiTUIiOlsiQlVTIiwiVkFOIiwiVFJVQ0siLCJVTklNT0ciLCJQQVNTRU5HRVItQ0FSIl0sIk1ZQiI6WyJQQVNTRU5HRVItQ0FSIl0sIlNNVCI6WyJQQVNTRU5HRVItQ0FSIl19LCJpc3MiOiJpUGFydHNUZWFtIiwibGFuZzMiOiJGUiIsImxhbmcyIjoiREUiLCJsYW5nMSI6IkRFIiwiZXhwIjo5Njk2NjQ0NDA2LCJpYXQiOjE2ODc4ODc5Mjd9.VChuQBaFyknbs2rHraC_Bs33UqGQmnIC-rtRDKjqt_o");
        executeWebservice(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C245207\",\"productId\":\"66D\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"26\"}],\"user\":{\"country\":\"200\",\"userId\":\"userId\"}}",
                          additionalRequestProperties,
                          // ACHTUNG: gleiches Ergebnis und gleiche Ergebnisdatei wie testiPartsDAIMLER15581WSgetNavOptsWithSpecialRight()
                          DWFile.get(getTestWorkingDir(), "resultDAIMLER15581WSgetNavOpts.txt"));
    }

    public void testiPartsDAIMLER15581WSgetPartsWithSpecialRight() {
        //        Produkt: 66D KGTU 24/015
        // MIT dem Recht "permissions": { "SPECIAL": [ "HighvoltbatteryRepair" ], ... soll etwas ausgegeben werden.
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb3VudHJ5IjoiREUiLCJzdWIiOiJ1c2VyIiwicGVybWlzc2lvbnMiOnsiU1BFQ0lBTCI6WyJIaWdodm9sdGJhdHRlcnlSZXBhaXIiXSwiTUIiOlsiQlVTIiwiVkFOIiwiVFJVQ0siLCJVTklNT0ciLCJQQVNTRU5HRVItQ0FSIl0sIk1ZQiI6WyJQQVNTRU5HRVItQ0FSIl0sIlNNVCI6WyJQQVNTRU5HRVItQ0FSIl19LCJpc3MiOiJpUGFydHNUZWFtIiwibGFuZzMiOiJGUiIsImxhbmcyIjoiREUiLCJsYW5nMSI6IkRFIiwiZXhwIjo5Njk2NjQ0NDA2LCJpYXQiOjE2ODc4ODc5Mjd9.M_otnScjN4UV938govYsHOkHwiHUtmYgQJxdDpd3oVU");
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C219356\",\"productId\":\"66W\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"29\"},{\"type\":\"cg_subgroup\",\"id\":\"015\"}],\"user\":{\"country\":\"200\",\"userId\":\"userId\"}}",
                          additionalRequestProperties, DWFile.get(getTestWorkingDir(), "resultDAIMLER15581WSGetParts_1.txt"));
    }

    public void testiPartsDAIMLER15581WSgetPartsNoSpecialRight() {
        //        Produkt: 66D KGTU 24/015
        // OHNE das Recht "permissions": { "SPECIAL": [ "HighvoltbatteryRepair" ], ... darf kein gültiges Ergebnis zurück kommen.
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb3VudHJ5IjoiREUiLCJzdWIiOiJ1c2VyIiwicGVybWlzc2lvbnMiOnsiTUIiOlsiQlVTIiwiVkFOIiwiVFJVQ0siLCJVTklNT0ciLCJQQVNTRU5HRVItQ0FSIl0sIk1ZQiI6WyJQQVNTRU5HRVItQ0FSIl0sIlNNVCI6WyJQQVNTRU5HRVItQ0FSIl19LCJpc3MiOiJpUGFydHNUZWFtIiwibGFuZzMiOiJGUiIsImxhbmcyIjoiREUiLCJsYW5nMSI6IkRFIiwiZXhwIjo5Njk2NjQ0NDA2LCJpYXQiOjE2ODc4ODc5Mjd9.VChuQBaFyknbs2rHraC_Bs33UqGQmnIC-rtRDKjqt_o");
        executeWebservice(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C219356\",\"productId\":\"66W\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"29\"},{\"type\":\"cg_subgroup\",\"id\":\"015\"}],\"user\":{\"country\":\"200\",\"userId\":\"userId\"}}",
                          additionalRequestProperties, DWFile.get(getTestWorkingDir(), "resultDAIMLER15581WSGetParts_2.txt"), 400);
    }

    /*
     * Überprüft, ob ein Produkt für den Navigations-TU freigeschaltet ist und ob dieser TU sichtbar ist.
     * Beim WS-Ident darf bei Baumuster-Einstieg das Flag "visualNavAvailable" grundsätzlich nicht ausgegeben werden.
     * Zudem soll in allen anderen Einstiegsfällen geprüft werden, ob am Navigationsmodul "TU ausblenden" gesetzt ist.
     * Ist das so, soll das Flag "visualNavAvailable" ebenfalls nicht ausgegeben werden.
     */
    public void testiPartsWSIdentVisualNavAvailableDAIMLER15587() {
        // Token enthält Sprachen de, en, fr, d.h. de ist die Hauptsprache und en, fr sind die Rückfallsprachen
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");

        iPartsDataModule module = new iPartsDataModule(getProject(), new iPartsModuleId("63J_Navigation"));
        assertTrue(module.existsInDB());

        try {
            // TEST (1):
            // * Einstieg mit Datenkarte
            // * DM_MODULE_HIDDEN = '1'
            // ==> "visualNavAvailable" soll NICHT ausgegeben werden.
            module.setFieldValueAsBoolean(iPartsConst.FIELD_DM_MODULE_HIDDEN, true, DBActionOrigin.FROM_EDIT);
            module.saveToDB();
            clearCachesForTestiPartsWSIdentVisualNavAvailable();

            // Datenkarte über Ident WS prüfen
            executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"identCode\":\"WDC1641222A546031\"}", additionalRequestProperties,
                              "{\"identContexts\":[{\"aggTypeId\":\"F\",\"datacardExists\":true,\"fin\":\"WDC1641222A546031\",\"modelId\":\"C164122\",\"modelTypeId\":\"C164\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"63J\"}]}", 200);

            // TEST (2):
            // * Einstieg mit Datenkarte
            // * DM_MODULE_HIDDEN = '0'
            // ==> "visualNavAvailable" soll ausgegeben werden.
            module.setFieldValueAsBoolean(iPartsConst.FIELD_DM_MODULE_HIDDEN, false, DBActionOrigin.FROM_EDIT);
            module.saveToDB();
            clearCachesForTestiPartsWSIdentVisualNavAvailable();

            // Datenkarte über Ident WS prüfen
            executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"identCode\":\"WDC1641222A546031\"}", additionalRequestProperties,
                              "{\"identContexts\":[{\"aggTypeId\":\"F\",\"datacardExists\":true,\"fin\":\"WDC1641222A546031\",\"modelId\":\"C164122\",\"modelTypeId\":\"C164\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"63J\",\"visualNavAvailable\":true}]}", 200);

            // TEST (3):
            // * Einstieg mit Baumuster: C164122
            // * DM_MODULE_HIDDEN = '0'
            // ==> "visualNavAvailable" soll NICHT ausgegeben werden.
            module.setFieldValueAsBoolean(iPartsConst.FIELD_DM_MODULE_HIDDEN, false, DBActionOrigin.FROM_EDIT);
            module.saveToDB();
            clearCachesForTestiPartsWSIdentVisualNavAvailable();

            // Baumuster über Ident WS prüfen
            additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");
            executeWebservice(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                              "{\"identCode\":\"C164122\"}", additionalRequestProperties,
                              "{\"identContexts\":[{\"aggTypeId\":\"F\",\"modelId\":\"C164122\",\"modelTypeId\":\"C164\",\"productClassIds\":[\"P\"],\"productClassNames\":[\"PKW\"],\"productId\":\"63J\"}]}", 200);
        } finally {
            // Die für den Test manipulierten Daten wieder zurücksetzen.
            module.setFieldValueAsBoolean(iPartsConst.FIELD_DM_MODULE_HIDDEN, false, DBActionOrigin.FROM_EDIT);
            module.saveToDB();
            clearCachesForTestiPartsWSIdentVisualNavAvailable();
        }
    }


    /**
     * Das Löschen dieser Caches wird mehrfach gebraucht.
     */
    private void clearCachesForTestiPartsWSIdentVisualNavAvailable() {
        clearWebservicePluginsCaches();
        EtkDataAssembly.clearGlobalEntriesCache();
        iPartsDataAssembly.clearAssemblyMetaDataCaches();
    }

    /**
     * Test auf einen Navigations-TU, bei dem Bilder enthalten sind, die in [IMAGES].[I_NAVIGATION_PERSPECTIVE] mit dem Enum "NavigationPerspective" gefüllt sind.
     * Im Response wird der sprachabhängige Text des Enums erwartet.
     */
    public void testiPartsWSVisualNavPerspectiveType() {
        // Token enthält Country DE und Sprachen de, en, fr, d.h. DE ist die Hauptsprache und en, fr sind die Rückfallsprachen
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");

        // WDB1641560W150896, Produkt 63J, PKW, Baureihe C164
        executeWebservice(iPartsWSVisualNavEndpoint.DEFAULT_ENDPOINT_URI + "?fin=WDB1641560W150896",
                          null, additionalRequestProperties,
                          "{\"cgSubgroups\":[{\"calloutId\":\"10\",\"cg\":\"21\",\"csg\":\"230\",\"description\":\"KEILRIEMEN,RIEMENSPANNER,UMLENKROLLE\",\"modelId\":\"C164156\",\"moduleId\":\"63J_21_230_00001\",\"productId\":\"63J\"},{\"calloutId\":\"20\",\"cg\":\"21\",\"csg\":\"300\",\"description\":\"ABSCHIRMUNGEN UND ABDAEMPFUNGEN AN MOTOR,GETRIEBE,VORDERACHSGETRIEBE\",\"modelId\":\"C164156\",\"moduleId\":\"63J_21_300_00001\",\"productId\":\"63J\"},{\"calloutId\":\"30\",\"cg\":\"24\",\"csg\":\"015\",\"description\":\"MOTORAUFHAENGUNG\",\"modelId\":\"C164156\",\"moduleId\":\"63J_24_015_00001\",\"productId\":\"63J\"},{\"calloutId\":\"40\",\"cg\":\"27\",\"csg\":\"125\",\"description\":\"GETRIEBE ANBAUTEILE, OELEINFUELLROHR\",\"modelId\":\"C164156\",\"moduleId\":\"63J_27_125_00001\",\"productId\":\"63J\"},{\"calloutId\":\"50\",\"cg\":\"27\",\"csg\":\"215\",\"description\":\"LENKRADSCHALTUNG\",\"modelId\":\"C164156\",\"moduleId\":\"63J_27_215_00001\",\"productId\":\"63J\"},{\"calloutId\":\"60\",\"cg\":\"28\",\"csg\":\"010\",\"description\":\"VERTEILERGETRIEBE VOLLSTAENDIG\",\"modelId\":\"C164156\",\"moduleId\":\"63J_28_010_00001\",\"productId\":\"63J\"},{\"calloutId\":\"70\",\"cg\":\"29\",\"csg\":\"015\",\"description\":\"PEDALANLAGE MIT LAGERUNG\",\"modelId\":\"C164156\",\"moduleId\":\"63J_29_015_00001\",\"productId\":\"63J\"}],\"navImages\":[{\"href\":\"/parts/media/drawing_Cockpit_with_electric\",\"id\":\"drawing_Cockpit_with_electric\",\"previewHref\":\"/parts/media/previews/drawing_Cockpit_with_electric\",\"type\":\"Cockpit_with_electric\"},{\"href\":\"/parts/media/drawing_Heating_and_cooling\",\"id\":\"drawing_Heating_and_cooling\",\"previewHref\":\"/parts/media/previews/drawing_Heating_and_cooling\",\"type\":\"Heating_and_cooling\"},{\"href\":\"/parts/media/drawing_Inside\",\"id\":\"drawing_Inside\",\"previewHref\":\"/parts/media/previews/drawing_Inside\",\"type\":\"Inside\"},{\"href\":\"/parts/media/drawing_Inside_with_electric\",\"id\":\"drawing_Inside_with_electric\",\"previewHref\":\"/parts/media/previews/drawing_Inside_with_electric\",\"type\":\"Inside_with_electric\"},{\"href\":\"/parts/media/drawing_Outside\",\"id\":\"drawing_Outside\",\"previewHref\":\"/parts/media/previews/drawing_Outside\",\"type\":\"Outside\"},{\"href\":\"/parts/media/drawing_Outside_with_electric\",\"id\":\"drawing_Outside_with_electric\",\"previewHref\":\"/parts/media/previews/drawing_Outside_with_electric\",\"type\":\"Outside_with_electric\"},{\"href\":\"/parts/media/drawing_Technics\",\"id\":\"drawing_Technics\",\"previewHref\":\"/parts/media/previews/drawing_Technics\",\"type\":\"Technics\"}]}");

    }

    public void testValidateParts() {
        // Token enthält Country DE und Sprachen de, en, fr, d.h. DE ist die Hauptsprache und en, fr sind die Rückfallsprachen
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");

        executeWebservice(iPartsWSValidatePartsEndpoint.DEFAULT_ENDPOINT_URI + "?finOrVin=WDD2050371R344972&parts=A6079900203,A6269900304,A2057200105,A2056900287,A2057200305,A2056900288",
                          null, additionalRequestProperties,
                          "{\"searchResults\":[{\"aggProductId\":\"D96\",\"aggTypeId\":\"M\",\"calloutId\":\"10\",\"name\":\"\",\"navContext\":[{\"id\":\"01\",\"label\":\"MOTORGEHAEUSE\",\"type\":\"cg_group\"},{\"id\":\"015\",\"label\":\"ZYLINDERKURBELGEHAEUSE,DECKEL UND DICHTUNGSSATZ\",\"type\":\"cg_subgroup\"},{\"id\":\"D96_01_015_00001\",\"label\":\"ZYLINDERKURBELGEHAEUSE,DECKEL UND DICHTUNGSSATZ\",\"partsAvailable\":true,\"type\":\"module\"}],\"partContext\":{\"moduleId\":\"D96_01_015_00001\",\"sequenceId\":\"00001\"},\"partNo\":\"A6079900203\",\"partNoFormatted\":\"A 607 990 02 03\"},{\"aggProductId\":\"D96\",\"aggTypeId\":\"M\",\"calloutId\":\"20\",\"name\":\"\",\"navContext\":[{\"id\":\"25\",\"label\":\"KUPPLUNG\",\"type\":\"cg_group\"},{\"id\":\"015\",\"label\":\"KUPPLUNG\",\"type\":\"cg_subgroup\"},{\"id\":\"D96_25_015_00001\",\"label\":\"KUPPLUNG\",\"partsAvailable\":true,\"type\":\"module\"}],\"partContext\":{\"moduleId\":\"D96_25_015_00001\",\"sequenceId\":\"00002\"},\"partNo\":\"A6269900304\",\"partNoFormatted\":\"A 626 990 03 04\"},{\"calloutId\":\"10\",\"name\":\"\",\"navContext\":[{\"id\":\"72\",\"label\":\"FAHRERTUEREN\",\"type\":\"cg_group\"},{\"id\":\"015\",\"label\":\"FAHRERTUEREN\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}],\"partContext\":{\"moduleId\":\"C01_72_015_00001\",\"sequenceId\":\"00002\"},\"partNo\":\"A2057200105\",\"partNoFormatted\":\"A 205 720 01 05\"},{\"calloutId\":\"100\",\"name\":\"\",\"navContext\":[{\"id\":\"72\",\"label\":\"FAHRERTUEREN\",\"type\":\"cg_group\"},{\"id\":\"015\",\"label\":\"FAHRERTUEREN\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}],\"partContext\":{\"moduleId\":\"C01_72_015_00001\",\"sequenceId\":\"00071\"},\"partNo\":\"A2056900287\",\"partNoFormatted\":\"A 205 690 02 87\"}]}");
    }

    public void testValidatePartsColorInfo() {
        // Token enthält Country DE und Sprachen de, en, fr, d.h. DE ist die Hauptsprache und en, fr sind die Rückfallsprachen
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");

        executeWebservice(iPartsWSValidatePartsEndpoint.DEFAULT_ENDPOINT_URI + "?finOrVin=WDD2050371R344972&parts=A1714000025",
                          null, additionalRequestProperties,
                          "{\"searchResults\":[{\"calloutId\":\"520\",\"colorInfoAvailable\":true,\"es2Key\":\"5337\",\"name\":\"RADDECKEL\",\"navContext\":[{\"id\":\"40\",\"label\":\"RAEDER\",\"type\":\"cg_group\"},{\"id\":\"015\",\"label\":\"RAEDER\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}],\"partContext\":{\"moduleId\":\"C01_40_015_00001\",\"sequenceId\":\"00193\"},\"partNo\":\"A1714000025\",\"partNoFormatted\":\"A 171 400 00 25\"}]}");
    }

    public void testValidatePartsAlternativeParts() {
        // Token enthält Country DE und Sprachen de, en, fr, d.h. DE ist die Hauptsprache und en, fr sind die Rückfallsprachen
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");

        executeWebservice(iPartsWSValidatePartsEndpoint.DEFAULT_ENDPOINT_URI + "?finOrVin=WDB9630031L738996&parts=A0159978245",
                          null, additionalRequestProperties,
                          "{\"searchResults\":[{\"alternativePartsAvailable\":true,\"calloutId\":\"155\",\"name\":\"O-RING\",\"navContext\":[{\"id\":\"25\",\"label\":\"KUPPLUNG\",\"type\":\"cg_group\"},{\"id\":\"330\",\"label\":\"HYDRAULISCH-PNEUMATISCHE BETAETIGUNG\",\"partsAvailable\":true,\"type\":\"cg_subgroup\"}],\"partContext\":{\"moduleId\":\"S01_25_330_00001\",\"sequenceId\":\"00007\"},\"partNo\":\"A0159978245\",\"partNoFormatted\":\"A 015 997 82 45\"}]}");
    }

    public void executeWebserviceWithoutTokenPermissionsCheck(String endpointURI, String requestString, DWFile expectedResponseFile) {
        String expectedResponseString = getExpectedResponseString(expectedResponseFile);
        executeWebserviceWithoutTokenPermissionsCheck(endpointURI, requestString, null, expectedResponseString, HttpConstants.HTTP_STATUS_OK);
    }

    public void executeWebserviceWithoutTokenPermissionsCheck(String endpointURI, String requestString, String expectedResponseString) {
        executeWebserviceWithoutTokenPermissionsCheck(endpointURI, requestString, null, expectedResponseString, HttpConstants.HTTP_STATUS_OK);
    }

    public void executeWebserviceWithoutTokenPermissionsCheck(String endpointURI, String requestString, String expectedResponseString,
                                                              int expectedResponseCode) {
        executeWebserviceWithoutTokenPermissionsCheck(endpointURI, requestString, null, expectedResponseString, expectedResponseCode);
    }

    public void executeWebserviceWithoutTokenPermissionsCheck(String endpointURI, String requestString, Map<String, String> additionalRequestProperties,
                                                              DWFile expectedResponseFile) {
        String expectedResponseString = getExpectedResponseString(expectedResponseFile);
        executeWebserviceWithoutTokenPermissionsCheck(endpointURI, requestString, additionalRequestProperties, expectedResponseString,
                                                      HttpConstants.HTTP_STATUS_OK);
    }

    /**
     * Diese Methode führt den Webservice aus und schaltet vorher die Permissions-Prüfung aus.
     *
     * @param endpointURI
     * @param requestString
     * @param additionalRequestProperties
     * @param expectedResponseString
     * @param expectedResponseCode
     */
    public void executeWebserviceWithoutTokenPermissionsCheck(String endpointURI, String requestString, Map<String, String> additionalRequestProperties, String expectedResponseString, int expectedResponseCode) {
        // Vorher die Prüfung der Marken und Produktgruppen abschalten
        UniversalConfiguration pluginConfig = iPartsWebservicePlugin.getPluginConfig();
        boolean checkPermissions = readBooleanConfigValue(pluginConfig, iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_PERMISSIONS, true);
        try {
            if (checkPermissions) {
                writeBooleanConfigValues(pluginConfig, false, iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_PERMISSIONS);
                clearWebservicePluginsCaches();
            }
            executeWebservice(endpointURI, requestString, additionalRequestProperties, expectedResponseString, expectedResponseCode);
        } finally {
            if (checkPermissions) {
                writeBooleanConfigValues(pluginConfig, true, iPartsWebservicePlugin.CONFIG_CHECK_TOKEN_PERMISSIONS);
                clearWebservicePluginsCaches();
            }
        }
    }

    // Token Gültigkeitsprüfung für alle Webservices
    private void internalTestTokenForAllWebservices() {
        Map<String, String> requestPropertiesEmptyToken = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7fSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.C6DBNBHUQCq4cgjYrNrSet-Ym39hh_fasSWR9SVM6GY");
        Map<String, String> requestPropertiesValidToken = createAdditionalRequestPropertiesForToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsInBlcm1pc3Npb25zIjp7Ik1CIjpbIlBBU1NFTkdFUi1DQVIiLCJUUlVDSyIsIlVOSU1PRyIsIlZBTiIsIkJVUyJdLCJTTVQiOlsiUEFTU0VOR0VSLUNBUiJdLCJNWUIiOlsiUEFTU0VOR0VSLUNBUiJdfSwibGFuZzEiOiJkZSIsImxhbmcyIjoiZW4iLCJsYW5nMyI6ImZyIiwiZXhwIjo5OTk5OTk5OTk5fQ.xhgIpA_vZv-G33HgkFWsEYLzyCEthET8-0inmkhxHRE");
        internalTestPermissionsForAllWebservices(requestPropertiesEmptyToken, requestPropertiesValidToken);
    }

    private void internalTestPermissionsForAllWebservices(Map<String, String> requestPropertiesEmptyPermissions, Map<String, String> requestPropertiesValidPermissions) {
        Map<String, String> allEndpointsWithRequestForTokenTests = new HashMap<>();
        allEndpointsWithRequestForTokenTests.put(iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI,
                                                 "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C204001\",\"productId\":\"C204\"},\"navContext\":[{\"type\":\"maingroup\",\"id\":\"21\"}]}");
        allEndpointsWithRequestForTokenTests.put(iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI,
                                                 "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205002\",\"productId\":\"C01\"},\"navContext\":[{\"type\":\"cg_group\",\"id\":\"64\"},{\"type\":\"cg_subgroup\",\"id\":\"015\"}]} ");
        allEndpointsWithRequestForTokenTests.put(iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                                                 "{\"partContext\":{\"moduleId\":\"C01_65_015_00001\",\"sequenceId\":\"00012\"},\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205002\",\"productId\":\"C01\",\"modelTypeId\":\"C205\"}}");
        allEndpointsWithRequestForTokenTests.put(iPartsWSSearchPartsEndpoint.DEFAULT_ENDPOINT_URI,
                                                 "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C205002\",\"productId\":\"C01\"},\"searchText\":\"Q0000000001\",\"includeAggs\":true}");
        allEndpointsWithRequestForTokenTests.put(iPartsWSSearchPartsWOContextEndpoint.DEFAULT_ENDPOINT_URI,
                                                 "{\"searchText\":\"Q0000000001\"}");
        allEndpointsWithRequestForTokenTests.put(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI,
                                                 "{\"identCode\":\"C205002\"} ");
        allEndpointsWithRequestForTokenTests.put(iPartsWSGetModelTypesEndpoint.DEFAULT_ENDPOINT_URI,
                                                 "{\"aggTypeId\":\"F\",\"productName\":\"C01\",\"productClassIds\":[\"P\"], \"includeModels\":true}");
        allEndpointsWithRequestForTokenTests.put(iPartsWSGetModelsEndpoint.DEFAULT_ENDPOINT_URI,
                                                 "{\"productClassIds\":[\"P\",\"U\",\"L\"],\"aggTypeId\":\"M\",\"productId\":\"69L\"}");
        allEndpointsWithRequestForTokenTests.put(iPartsWSGetMaterialNavEndpoint.DEFAULT_ENDPOINT_URI,
                                                 "{\"productId\":\"598\",\"assortmentClassId\":\"P\"}");
        allEndpointsWithRequestForTokenTests.put(iPartsWSGetMaterialPartsEndpoint.DEFAULT_ENDPOINT_URI,
                                                 "{\"productId\":\"598\",\"assortmentClassId\":\"L\",\"navContext\":[{\"type\":\"cg_group\",\"id\":\"35\"}]}");
        allEndpointsWithRequestForTokenTests.put(iPartsWSGetMaterialPartInfoEndpoint.DEFAULT_ENDPOINT_URI,
                                                 "{\"productId\":\"598\",\"partContext\":{\"moduleId\":\"598_10_001_00001\",\"sequenceId\":\"00004\"}}");
        allEndpointsWithRequestForTokenTests.put(iPartsWSSearchMaterialPartsEndpoint.DEFAULT_ENDPOINT_URI,
                                                 "{\"searchText\":\"TS LACK*\",\"navContext\":[{\"type\":\"cg_group\",\"id\":\"10\"}],\"productId\":\"598\",\"assortmentClassId\":\"F\"}");
        allEndpointsWithRequestForTokenTests.put(iPartsWSsearchComponentEndpoint.DEFAULT_ENDPOINT_URI,
                                                 "{\"identContext\":{\"aggTypeId\":\"F\",\"productClassIds\":[\"P\"],\"modelId\":\"C967001\",\"productId\":\"S10\"},\"searchText\":\"Z M03.725/01\",\"includeAggs\":true}");

        String errorAnswer = "{\"code\":4031,\"message\":\"You are not authorized to access this information\"}";
        String errorAnswerWithMissingPermissions = "{\"code\":4031,\"message\":\"You are not authorized to access this information\",\"missingPermissions\":[\"MB.PASSENGER-CAR\"]}";

        for (Map.Entry<String, String> requestParams : allEndpointsWithRequestForTokenTests.entrySet()) {
            String endpoint = requestParams.getKey();
            String request = requestParams.getValue();

            if (endpoint.equals(iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI)) {
                executeWebservice(endpoint, request, requestPropertiesEmptyPermissions, errorAnswerWithMissingPermissions, HttpConstants.HTTP_STATUS_FORBIDDEN);
            } else {
                executeWebservice(endpoint, request, requestPropertiesEmptyPermissions, errorAnswer, HttpConstants.HTTP_STATUS_FORBIDDEN);
            }
            executeWebservice(endpoint, request, requestPropertiesValidPermissions, (String)null, HttpConstants.HTTP_STATUS_OK);
        }
    }


    // Hinzufügen von Ereignis-Code-Regeln zu den normalen Code-Regeln (wird von den Webservices verwendet)
    public void testAddEventsCodes() {
        EtkProject project = getProject();
        iPartsDialogSeries series = iPartsDialogSeries.getInstance(project, new iPartsSeriesId("C203"));
        Map<String, iPartsEvent> eventsMap = series.getEventsMap();

        iPartsEvent event0 = eventsMap.get("6E0");
        if ((event0 == null) || (event0.getOrdinal() != 0)) {
            fail("Event 6E0 is not the first event of series C203");
        }

        iPartsEvent event1 = eventsMap.get("7E0");
        if ((event1 == null) || (event1.getOrdinal() != 1)) {
            fail("Event 7E0 is not the second event of series C203");
        }

        iPartsEvent eventLast = eventsMap.get("2E5");
        if ((eventLast == null) || (eventLast.getOrdinal() != eventsMap.size() - 1)) {
            fail("Event 2E5 is not the last event of series C203");
        }

        // Keine Ereignisse
        assertEquals("", DaimlerCodes.addEventsCodes("", null, null, project));
        assertEquals(";", DaimlerCodes.addEventsCodes(";", null, null, project));
        assertEquals("M08;", DaimlerCodes.addEventsCodes("M08;", null, null, project));
        assertEquals("(M08);", DaimlerCodes.addEventsCodes("(M08);", null, null, project));

        // Nur Ereignis-bis event0 (ohne Effekt, weil Ereignis-bis exklusiv)
        assertEquals("", DaimlerCodes.addEventsCodes("", null, event0, project));

        // Nur Ereignis-bis event1
        assertEquals("807;", DaimlerCodes.addEventsCodes("", null, event1, project));
        assertEquals("807;", DaimlerCodes.addEventsCodes(";", null, event1, project));
        assertEquals("(M08)+(807);", DaimlerCodes.addEventsCodes("M08;", null, event1, project));
        assertEquals("(M08)+(807);", DaimlerCodes.addEventsCodes("(M08);", null, event1, project));
        assertEquals("((M08)+(M09/M10))+(807);", DaimlerCodes.addEventsCodes("(M08)+(M09/M10);", null, event1, project));

        // Nur Ereignis-bis eventLast
        assertEquals("807/808+-058/808+058/809+-059/809+059/800+-050/800+050/801+-051/801+051/802+-052/802+052/803+-053;",
                     DaimlerCodes.addEventsCodes("", null, eventLast, project));
        assertEquals("807/808+-058/808+058/809+-059/809+059/800+-050/800+050/801+-051/801+051/802+-052/802+052/803+-053;",
                     DaimlerCodes.addEventsCodes(";", null, eventLast, project));
        assertEquals("(M08)+(807/808+-058/808+058/809+-059/809+059/800+-050/800+050/801+-051/801+051/802+-052/802+052/803+-053);",
                     DaimlerCodes.addEventsCodes("M08;", null, eventLast, project));
        assertEquals("(M08)+(807/808+-058/808+058/809+-059/809+059/800+-050/800+050/801+-051/801+051/802+-052/802+052/803+-053);",
                     DaimlerCodes.addEventsCodes("(M08);", null, eventLast, project));
        assertEquals("((M08)+(M09/M10))+(807/808+-058/808+058/809+-059/809+059/800+-050/800+050/801+-051/801+051/802+-052/802+052/803+-053);",
                     DaimlerCodes.addEventsCodes("(M08)+(M09/M10);", null, eventLast, project));

        // Nur Ereignis-ab event0
        assertEquals("807/808+-058/808+058/809+-059/809+059/800+-050/800+050/801+-051/801+051/802+-052/802+052/803+-053/803+053;",
                     DaimlerCodes.addEventsCodes("", event0, null, project));
        assertEquals("807/808+-058/808+058/809+-059/809+059/800+-050/800+050/801+-051/801+051/802+-052/802+052/803+-053/803+053;",
                     DaimlerCodes.addEventsCodes(";", event0, null, project));
        assertEquals("(M08)+(807/808+-058/808+058/809+-059/809+059/800+-050/800+050/801+-051/801+051/802+-052/802+052/803+-053/803+053);",
                     DaimlerCodes.addEventsCodes("M08;", event0, null, project));
        assertEquals("(M08)+(807/808+-058/808+058/809+-059/809+059/800+-050/800+050/801+-051/801+051/802+-052/802+052/803+-053/803+053);",
                     DaimlerCodes.addEventsCodes("(M08);", event0, null, project));
        assertEquals("((M08)+(M09/M10))+(807/808+-058/808+058/809+-059/809+059/800+-050/800+050/801+-051/801+051/802+-052/802+052/803+-053/803+053);",
                     DaimlerCodes.addEventsCodes("(M08)+(M09/M10);", event0, null, project));

        // Nur Ereignis-ab event1
        assertEquals("((M08)+(M09/M10))+(808+-058/808+058/809+-059/809+059/800+-050/800+050/801+-051/801+051/802+-052/802+052/803+-053/803+053);",
                     DaimlerCodes.addEventsCodes("(M08)+(M09/M10);", event1, null, project));

        // Nur Ereignis-ab eventLast
        assertEquals("((M08)+(M09/M10))+(803+053);", DaimlerCodes.addEventsCodes("(M08)+(M09/M10);", eventLast, null, project));

        // Ereignis-ab event0 und Ereignis-bis event1
        assertEquals("((M08)+(M09/M10))+(807);", DaimlerCodes.addEventsCodes("(M08)+(M09/M10);", event0, event1, project));

        // Ereignis-ab event1 und Ereignis-bis eventLast
        assertEquals("((M08)+(M09/M10))+(808+-058/808+058/809+-059/809+059/800+-050/800+050/801+-051/801+051/802+-052/802+052/803+-053);",
                     DaimlerCodes.addEventsCodes("(M08)+(M09/M10);", event1, eventLast, project));

        // Ereignis-ab event1 und Ereignis-bis event1 (ohne Effekt, weil Ereignis-ab == Ereignis-bis)
        assertEquals("(M08)+(M09/M10);", DaimlerCodes.addEventsCodes("(M08)+(M09/M10);", event1, event1, project));

        // Ereignis-ab event1 und Ereignis-bis event0 (ohne Effekt, weil Ereignis-ab > Ereignis-bis)
        assertEquals("(M08)+(M09/M10);", DaimlerCodes.addEventsCodes("(M08)+(M09/M10);", event1, event0, project));

        int numIterations = 10000;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < numIterations; i++) {
            DaimlerCodes.addEventsCodes("(M08)+(M09/M10);", event1, eventLast, project);
        }
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Duration for adding " + numIterations + " times events code rules to a normal code rule: "
                           + duration + " ms (" + ((double)duration / numIterations) + " ms in average)");
    }
}