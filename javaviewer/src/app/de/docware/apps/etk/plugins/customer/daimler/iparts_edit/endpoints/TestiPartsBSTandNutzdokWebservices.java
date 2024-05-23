/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints;

import de.docware.apps.etk.plugins.AbstractJavaViewerPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst.iPartsDataWorkOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst.iPartsDataWorkOrderTask;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst.iPartsWorkOrderId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst.iPartsWorkOrderTaskId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataNutzDokRemark;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsNutzDokRemarkId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.tests.AbstractTestiPartsWSEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.bst.iPartsWSBSTEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.nutzdok.iPartsWSConstructionKitsEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.nutzdok_remarks.iPartsWSConstructionKitsRemarksEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.nutzdok_remarks.iPartsWSDeleteConstructionKitsKemRemarkEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.nutzdok_remarks.iPartsWSDeleteConstructionKitsSaaRemarkEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.http.HttpConstants;
import de.docware.framework.modules.webservice.restful.RESTfulEndpoint;
import de.docware.util.StrUtils;
import de.docware.util.test.FrameworkTestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit-Tests für die iParts BST- und Nutzdok-Webservices mit der Standard Oracle DB für Webservices Unit-Tests.
 */
public class TestiPartsBSTandNutzdokWebservices extends AbstractTestiPartsWSEndpoint {

    // erzeugten Clone nicht erzeugen und löschen (falls besetzt) Clone muss existieren
    public static String CLONE_SID = "";

    /**
     * Eine manuelle {@link TestSuite} und {@link FrameworkTestSetup} erzeugen, um komplette Tests mit einem globalen SetUp
     * und TearDown zu ermöglichen inkl. Nachtests.
     *
     * @return
     */
    public static Test suite() {
        return createTestSuite(TestiPartsBSTandNutzdokWebservices.class);

        // Einzelmethoden ausführen (Liste von Methoden angeben)
//        return createTestSuite(TestiPartsWebservices.class, "testGetPartsOmittedPartsFilterInDatacardFilter");
    }

    /**
     * Standardkonstruktor.
     */
    public TestiPartsBSTandNutzdokWebservices() {
        super();
    }

    /**
     * Konstruktor für den kompletten Test über eine manuell erzeugte {@link TestSuite}.
     *
     * @param globalTest
     * @param methodName
     */
    public TestiPartsBSTandNutzdokWebservices(TestiPartsBSTandNutzdokWebservices globalTest, String methodName) {
        super(globalTest, methodName);
    }

    @Override
    protected boolean isReadOnlyCloneDB() {
        return false;
    }

    @Override
    public void globalSetUp() throws Exception {
        super.globalSetUp();
        iPartsEditPlugin.getKeystoreManagerBST().startKeystoreDirectoryMonitor();
        iPartsEditPlugin.getKeystoreManagerNutzDok().startKeystoreDirectoryMonitor();
    }

    public String createOracleClone(OracleCloneParams oracleCloneParams) {
        if (StrUtils.isEmpty(CLONE_SID)) {
            return super.createOracleClone(oracleCloneParams);
        } else {
            System.out.println("Oracle clone will not be created for Sid \"" + CLONE_SID + "\"!");
            return CLONE_SID;
        }
    }

    @Override
    public void removeOracleClone(OracleCloneParams oracleCloneParams) {
        if (StrUtils.isEmpty(CLONE_SID)) {
            super.removeOracleClone(oracleCloneParams);
        } else {
            System.out.println("Oracle clone with Sid \"" + CLONE_SID + "\" is still running!");
        }
    }

    @Override
    protected void globalTearDown() throws Exception {
        iPartsEditPlugin.getKeystoreManagerBST().stopKeystoreDirectoryMonitor();
        iPartsEditPlugin.getKeystoreManagerNutzDok().stopKeystoreDirectoryMonitor();
        super.globalTearDown();
    }

    @Override
    protected AbstractJavaViewerPlugin[] createWebservicePlugins() {
        return new AbstractJavaViewerPlugin[]{ new iPartsEditPlugin(getWebserviceHost(), getWebservicePort()) };
    }

    private Map<String, String> createAdditionalRequestPropertiesForBSTToken(String token) {
        Map<String, String> additionalRequestProperties = new HashMap<>(1);
        String authorizationHeader = iPartsEditPlugin.getPluginConfig().getConfigValueAsString(iPartsEditPlugin.CONFIG_BST_HEADER_TOKEN_TYPE)
                                     + " " + token;
        additionalRequestProperties.put(iPartsEditPlugin.getPluginConfig().getConfigValueAsString(iPartsEditPlugin.CONFIG_BST_HEADER_TOKEN_NAME),
                                        authorizationHeader);
        return additionalRequestProperties;
    }

    private void universalBSTWithTokenError(String requestString, String token, String expectedResponseString) {
        // Token zum Request-Header hinzufügen falls vorhanden
        Map<String, String> additionalRequestProperties = null;
        if (token != null) {
            additionalRequestProperties = createAdditionalRequestPropertiesForBSTToken(token);
        }

        executeWebservice(iPartsWSBSTEndpoint.DEFAULT_ENDPOINT_URI, requestString, additionalRequestProperties,
                          expectedResponseString, HttpConstants.HTTP_STATUS_UNAUTHORIZED);
    }


    // Alle Testmethoden müssen mit "test" anfangen

    // BST
    public void testBSTisRecordCreated() {
        String requestString = "{\"wpid\":2387,\"branch\":\"TRUCK\",\"isCostNeutral\":false,\"isInternalOrder\":true,\"title\":\"Titel\",\"workDeliveryTs\":\"2019-12-27\",\"workBeginTs\":\"2019-01-02\",\"tasks\":[{\"activityName\":\"Bearbeitung Bildtafel\",\"activityType\":\"LB 3.12\",\"amount\":10}]}";
        Map<String, String> requestPropertiesValidToken = createAdditionalRequestPropertiesForBSTToken("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJ4ZW50cnktcG9ydGFsIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjk5OTk5OTk5OTl9.V3fwpufoALjLG1AUXXejus8JA-fVK5ZHW6fy4NFkdqxxOub4Ww7V_zn3F5lD89HOSyHqTY2ZT-ygSA40EbHRVZN2YKyQkS_H8ohetV-OP4sHLlR8plc2Cb1VFy0_Sjpkmvnmz5388LAkf8OJ3AgI3WlXp3PMlI6JPO5dutG77B4");
        executeWebservice(iPartsWSBSTEndpoint.DEFAULT_ENDPOINT_URI, requestString, requestPropertiesValidToken, "{}", HttpConstants.HTTP_STATUS_OK);
        iPartsDataWorkOrder workOrder = new iPartsDataWorkOrder(getProject(), new iPartsWorkOrderId("2387"));
        assertTrue(workOrder.existsInDB());
        iPartsDataWorkOrderTask workOrderTask = new iPartsDataWorkOrderTask(getProject(), new iPartsWorkOrderTaskId("2387", "00001"));
        assertTrue(workOrderTask.existsInDB());

        executeWebservice(iPartsWSBSTEndpoint.DEFAULT_ENDPOINT_URI, requestString, requestPropertiesValidToken, "{\"code\":4090,\"message\":\"workorder (2387) already exists in database. Aborting import.\"}", HttpConstants.HTTP_STATUS_CONFLICT);

        // Check mit Supplier und einem Array
        requestString = "{\"wpid\":2388,\"branch\":\"TRUCK\",\"subBranches\":[\"LKW\",\"MB-Tac\",\"Powersystems\",\"Unimog\"],\"isCostNeutral\":false,\"isInternalOrder\":false,\"title\":\"Titel\",\"workDeliveryTs\":\"2019-12-27\",\"workBeginTs\":\"2019-01-02\",\"supplier\":{\"id\":\"4711 zzz\",\"shortname\":\"k + k\",\"name\":\"k+k information services GmbH\"},\"tasks\":[{\"activityName\":\"Bearbeitung Bildtafel\",\"activityType\":\"LB 3.12\",\"amount\":10}]}";
        executeWebservice(iPartsWSBSTEndpoint.DEFAULT_ENDPOINT_URI, requestString, requestPropertiesValidToken, "{}", HttpConstants.HTTP_STATUS_OK);
        workOrder = new iPartsDataWorkOrder(getProject(), new iPartsWorkOrderId("2388"));
        assertTrue(workOrder.existsInDB());
        assertNotNull(workOrder.getFieldValueAsArray(iPartsConst.FIELD_DWO_SUB_BRANCHES));
        assertEquals("4711 zzz", workOrder.getFieldValue(iPartsConst.FIELD_DWO_SUPPLIER_NO));
        workOrderTask = new iPartsDataWorkOrderTask(getProject(), new iPartsWorkOrderTaskId("2388", "00001"));
        assertTrue(workOrderTask.existsInDB());

        executeWebservice(iPartsWSBSTEndpoint.DEFAULT_ENDPOINT_URI, requestString, requestPropertiesValidToken, "{\"code\":4090,\"message\":\"workorder (2388) already exists in database. Aborting import.\"}", HttpConstants.HTTP_STATUS_CONFLICT);

        getProject().getDbLayer().startTransaction();
        try {
            iPartsWorkOrderTaskId taskId = new iPartsWorkOrderTaskId("2389", "00001");
            workOrderTask = new iPartsDataWorkOrderTask(getProject(), taskId);
            workOrderTask.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            workOrderTask.saveToDB();
            getProject().getDbLayer().commit();
        } catch (Exception e) {
            getProject().getDbLayer().rollback();
            fail();
        }
        requestString = "{\"wpid\":2389,\"branch\":\"TRUCK\",\"subBranches\":[\"LKW\",\"MB-Tac\",\"Powersystems\",\"Unimog\"],\"isCostNeutral\":false,\"isInternalOrder\":false,\"title\":\"Titel\",\"workDeliveryTs\":\"2019-12-27\",\"workBeginTs\":\"2019-01-02\",\"supplier\":{\"id\":\"4711 zzz\",\"shortname\":\"k + k\",\"name\":\"k+k information services GmbH\"},\"tasks\":[{\"activityName\":\"Bearbeitung Bildtafel\",\"activityType\":\"LB 3.12\",\"amount\":10}]}";
        executeWebservice(iPartsWSBSTEndpoint.DEFAULT_ENDPOINT_URI, requestString, requestPropertiesValidToken, "{\"code\":4090,\"message\":\"workorder tasks already exist for new workorder (2389). Aborting import.\"}", HttpConstants.HTTP_STATUS_CONFLICT);

    }

    public void testBSTTokenErrors() {
        // Ungültige Tokens
        universalBSTWithTokenError("{}", "", "{\"code\":4011,\"message\":\"Authorization header 'Bearer' invalid for JWT configuration\"}");
        universalBSTWithTokenError("{}", "abc.def.ghi", "{\"code\":4011,\"message\":\"JWT header token 'abc.def.ghi' caused an UTF-8 error\"}");

        // Ungültige Signatur
        universalBSTWithTokenError("{}", "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJ4ZW50cnktcG9ydGFsIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjB9.Nhy-xVi7ZSYbpDuDFz2uZE9xlxjapdtbw7fT_g4DlAe9kmIvS1i1Bt9XtOKLGmjnAKMHZJh8hVgGXL0gvn2B-Zcf2T0ftRvVeiB5HjrQ-cCPg7wkgiQxpJtjUlXBHCfNynY164KWQYepD4auHaM5c365l9YLLjUFU8CN1a9Txyz",
                                   "{\"code\":4011,\"message\":\"Signature validation error for token 'eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJ4ZW50cnktcG9ydGFsIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjB9.Nhy-xVi7ZSYbpDuDFz2uZE9xlxjapdtbw7fT_g4DlAe9kmIvS1i1Bt9XtOKLGmjnAKMHZJh8hVgGXL0gvn2B-Zcf2T0ftRvVeiB5HjrQ-cCPg7wkgiQxpJtjUlXBHCfNynY164KWQYepD4auHaM5c365l9YLLjUFU8CN1a9Txyz'\"}");

        // Token abgelaufen
        universalBSTWithTokenError("{}", "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJ4ZW50cnktcG9ydGFsIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjB9.Nhy-xVi7ZSYbpDuDFz2uZE9xlxjapdtbw7fT_g4DlAe9kmIvS1i1Bt9XtOKLGmjnAKMHZJh8hVgGXL0gvn2B-Zcf2T0ftRvVeiB5HjrQ-cCPg7wkgiQxpJtjUlXBHCfNynY164KWQYepD4auHaM5c365l9YLLjUFU8CN1a9TUjU",
                                   "{\"code\":4012,\"message\":\"Token expired at 1970-01-01T01:00:00+0100 (0 seconds since epoch)\"}");

        // Kein Issuer gefunden
        universalBSTWithTokenError("{}", "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJoZXJiZXJ0IiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjk5OTk5OTk5OTl9.OdJlzsXwnswEFnKnsH69ZQL-5PEfiRp1mAlcMjwqNVL9IG4hLC0ohJnmFEAmZeCosSsJ8ZgrQ2EY74Yx6Z6z1OXW5ZNAZW2dmDFc47AJleLvxY6urSaVFOrZ19BSPo63TEcDWVf51TpRHHJ57irp8mOZE2mFMJPbd_iiwWaYoVw",
                                   "{\"code\":4011,\"message\":\"No public key definition found for issuer 'herbert'\"}");

        // HS256 nicht zulässig
        universalBSTWithTokenError("{}", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJ4ZW50cnktcG9ydGFsIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjk5OTk5OTk5OTl9.rjRRZG0eB9T0C8-pnsZkCSjv1D0YMmAIdDPUn4mXbmM",
                                   "{\"code\":4011,\"message\":\"Algorithm HS256 supported but not allowed\"}");
    }


    private Map<String, String> createAdditionalRequestPropertiesForNutzdokToken(String token) {
        Map<String, String> additionalRequestProperties = new HashMap<>(1);
        String authorizationHeader = iPartsEditPlugin.getPluginConfig().getConfigValueAsString(iPartsEditPlugin.CONFIG_NUTZDOK_HEADER_TOKEN_TYPE)
                                     + " " + token;
        additionalRequestProperties.put(iPartsEditPlugin.getPluginConfig().getConfigValueAsString(iPartsEditPlugin.CONFIG_NUTZDOK_HEADER_TOKEN_NAME),
                                        authorizationHeader);
        return additionalRequestProperties;
    }

    public void testNutzdokConstructionKitsAnnotations() {
        // Dieser Test darf nur ausgeführt werden, wenn keine Cortex-Verarbeitung stattfindet
        boolean doSaveViaCortex = iPartsWSConstructionKitsEndpoint.SAVE_VIA_CORTEX_TABLE;
        if (doSaveViaCortex) {
            // Test darf nur durchlaufen für einen bestehenden Clone
            if (StrUtils.isEmpty(CLONE_SID)) {
                System.out.println("No Test testNutzdokConstructionKitsAnnotations() without CloneSid");
                return;
            }
            System.out.println("Modified Test testNutzdokConstructionKitsAnnotations() for Cortex");
        }

        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForNutzdokToken("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiZGUiLCJsYW5nMiI6ImZyIiwibGFuZzMiOiJlcyIsImlhdCI6MTUxNjU3NTYwMCwiZXhwIjoxNzE5NzcyNDAwLCJwZXJtaXNzaW9ucyI6eyJNQiI6WyJUUlVDSyJdfX0.otYXLd7qIm62SPwXuYZKMaOvMXk6t8bvli0tNo860bi2qbyabp4mwiANF-PsLVTSWLE1X-kmS3PigKTgKLcey2HEeRq-sNByXeOUp-6uRtDhwGfDwfEYB_zCenTMXHmJiqWA_QM8qiaWqrWCfiHxLvWHwr253XYXfxUOLN6dh2M");
        iPartsNutzDokRemarkId remarkId;
        iPartsDataNutzDokRemark remark;
        String tmpStr;

// ----------------------------------
//           _  _______ __  __
//          | |/ / ____|  \/  |
//          | ' /|  _| | |\/| |
//          | . \| |___| |  | |
//          |_|\_\_____|_|  |_|
//
// ----------------------------------

        // Bemerkungstexte für KEMs + SAAs anlegen mit:
        // PUT
        // http://localhost:8080/FrameworkApp/iPartsEdit/constructionKits/annotation

        // Bemerkungstexte für KEM-Datensätze löschen mit:
        // DELETE
        // http://localhost:1235//iPartsEdit/constructionKits/KEM/KEM_UT_001/annotation/UTDAIMLER10558

        String deleteKemRemarksURI = iPartsWSConstructionKitsEndpoint.DEFAULT_ENDPOINT_URI + iPartsWSDeleteConstructionKitsKemRemarkEndpoint.ENDPOINT_URI_SUFFIX;

        // ----------------------------
        // KEM Test [1]
        // ----------------------------

        // Sicherstellen, dass Bemerkungstext NICHT in der DB existiert
        remarkId = new iPartsNutzDokRemarkId("KEM_UT_001", "KEM", "UTDAIMLER10558");
        remark = new iPartsDataNutzDokRemark(getProject(), remarkId);
        if (remark.existsInDB()) {
            remark.deleteFromDB();
        }

        // Bemerkungstext für KEM anlegen
        executeWebservice(iPartsWSConstructionKitsRemarksEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\n" +
                          "  \"id\": \"" + remarkId.getRemarkId() + "\",\n" +
                          "  \"refId\": \"" + remarkId.getRefId() + "\",\n" +   // KEM oder SAA
                          "  \"type\": \"" + remarkId.getRefType() + "\",\n" +
                          "  \"user\": \"UnitTestUser\",\n" +
                          "  \"updateTs\": \"2020-06-15T11:34:27+01:00\",\n" +
                          "  \"data\": \"e1xydGYxXGFuc2lcYW5zaWNwZzEyNTJcZGVmZjB7XGZvbnR0Ymx7XGYwXGZuaWxcZmNoYXJzZXQwIE1TIFNhbnMgU2VyaWY7fX0NClx2aWV3a2luZDRcdWMxXHBhcmRcbGFuZzEwMzFcZjBcZnMxNiBOZXVlIEdldHJpZWJlIHd1cmRlbiB2b24gVVYgLT4gR0VaIC8gV2FubiBzZXR6ZW4gZGllIGJlaWRlbiBuZXVlbiBBIDk1NiAyNzAgMTEgMDEgVzIyMjdBMDggKyBBIDk1NiAyNzgwIDEyIDAxIFcyMjI3QTA5IGluIGRlciBTZXJpZSBlaW4uIEJldHJvZmZlbmUgU3RcJ2ZjbGkgaXN0IHZvbiAyMDAzIHVuZCBkYSBpc3QgbGVpZGVyIGtlaW4gVGVybWluIGVyc2ljaHRsaWNoISBTdFwnZmNja2xpc3Rlbi1GcmVpZ2FiZSBlcmZvbGd0IGx0LiBLXCdjNEEgZHVyY2ggQ1RULCBkYWhlciBBdWZ0cmFnIG5pY2h0IEVULXJlbGV2YW50LlxwYXINCn0NCgA=\"\n" +
                          "}", additionalRequestProperties,
                          "",
                          HttpConstants.HTTP_STATUS_OK);

        if (!doSaveViaCortex) {
            // Checken, ob der angelegte Datensatz auch wirklich in der DB existiert
            remark = new iPartsDataNutzDokRemark(getProject(), remarkId);
            assertTrue(remark.existsInDB());
        }


        // Bemerkungstext für KEM wieder löschen ...
        tmpStr = deleteKemRemarksURI + "/" + remarkId.getRefId() + "/annotation/" + remarkId.getRemarkId();
        executeWebservice(tmpStr, RESTfulEndpoint.HttpMethod.DELETE,
                          "", additionalRequestProperties,
                          "",
                          HttpConstants.HTTP_STATUS_OK);

        if (!doSaveViaCortex) {
            // ... und kontrollieren, ob der Datensatz auch wirklich wieder raus ist aus der Datenbank.
            remark = new iPartsDataNutzDokRemark(getProject(), remarkId);
            assertFalse(remark.existsInDB());
        }

// ----------------------------------
//           ____    _        _
//          / ___|  / \      / \
//          \___ \ / _ \    / _ \
//           ___) / ___ \  / ___ \
//          |____/_/   \_\/_/   \_\
//
// ----------------------------------

        // Bemerkungstexte für SAA-Datensätze löschen mit:
        // DELETE
        // http://localhost:1235//iPartsEdit/constructionKits/SAA/Z234567AB/annotation/UTDAIMLER10558

        String deleteSaaRemarksURI = iPartsWSConstructionKitsEndpoint.DEFAULT_ENDPOINT_URI + iPartsWSDeleteConstructionKitsSaaRemarkEndpoint.ENDPOINT_URI_SUFFIX;

        // ----------------------------
        // SAA-Test [1]
        // ----------------------------

        // Sicherstellen, dass Bemerkungstext NICHT in der DB existiert
        remarkId = new iPartsNutzDokRemarkId("Z234567AB", "SAA", "UTDAIMLER10558");
        remark = new iPartsDataNutzDokRemark(getProject(), remarkId);
        if (remark.existsInDB()) {
            remark.deleteFromDB();
        }

        int expectedResponseCode = HttpConstants.HTTP_STATUS_OK;
        String expectedResponseString = "";
        if (!doSaveViaCortex) {
            expectedResponseCode = HttpConstants.HTTP_STATUS_INTERNAL_SERVER_ERROR;
            expectedResponseString = "{\"code\":5000,\"message\":\"String 'Z234567AB' wurde zu SA/SAA 'Z234567AB' formatiert. Das Ergebnis entspricht nicht der Vorgabe\"}";
        }
        // Bemerkungstext für SAA anlegen, Syntaxfehler in der SAA-Benennung [Z234567AB], die letzten beiden Zeichen dürfen nur Ziffern sein
        executeWebservice(iPartsWSConstructionKitsRemarksEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\n" +
                          "  \"id\": \"" + remarkId.getRemarkId() + "\",\n" +
                          "  \"refId\": \"" + remarkId.getRefId() + "\",\n" +   // KEM oder SAA
                          "  \"type\": \"" + remarkId.getRefType() + "\",\n" +
                          "  \"user\": \"UnitTestUser\",\n" +
                          "  \"updateTs\": \"2020-06-15T11:34:27+01:00\",\n" +
                          "  \"data\": \"e1xydGYxXGFuc2lcYW5zaWNwZzEyNTJcZGVmZjB7XGZvbnR0Ymx7XGYwXGZuaWxcZmNoYXJzZXQwIE1TIFNhbnMgU2VyaWY7fX0NClx2aWV3a2luZDRcdWMxXHBhcmRcbGFuZzEwMzFcZjBcZnMxNiBOZXVlIEdldHJpZWJlIHd1cmRlbiB2b24gVVYgLT4gR0VaIC8gV2FubiBzZXR6ZW4gZGllIGJlaWRlbiBuZXVlbiBBIDk1NiAyNzAgMTEgMDEgVzIyMjdBMDggKyBBIDk1NiAyNzgwIDEyIDAxIFcyMjI3QTA5IGluIGRlciBTZXJpZSBlaW4uIEJldHJvZmZlbmUgU3RcJ2ZjbGkgaXN0IHZvbiAyMDAzIHVuZCBkYSBpc3QgbGVpZGVyIGtlaW4gVGVybWluIGVyc2ljaHRsaWNoISBTdFwnZmNja2xpc3Rlbi1GcmVpZ2FiZSBlcmZvbGd0IGx0LiBLXCdjNEEgZHVyY2ggQ1RULCBkYWhlciBBdWZ0cmFnIG5pY2h0IEVULXJlbGV2YW50LlxwYXINCn0NCgA=\"\n" +
                          "}", additionalRequestProperties,
                          expectedResponseString,
                          expectedResponseCode);

        if (!doSaveViaCortex) {
            // Checken, ob der fehlerhafte Datensatz auch wirklich NICHT in der DB existiert
            remark = new iPartsDataNutzDokRemark(getProject(), remarkId);
            assertFalse(remark.existsInDB());
        }

        // ----------------------------
        // SAA-Test [2]
        // ----------------------------

        // Diese SAA [Z23456789] gibt's nicht, ohne Anlegen löschen muss zu einer Fehlermeldung führen.
        remarkId = new iPartsNutzDokRemarkId("Z234567AB", "SAA", "UTDAIMLER10558");
        // Bemerkungstext für SAA löschen ...
        tmpStr = deleteSaaRemarksURI + "/" + remarkId.getRefId() + "/annotation/" + remarkId.getRemarkId();
        expectedResponseCode = HttpConstants.HTTP_STATUS_OK;
        expectedResponseString = "";
        if (!doSaveViaCortex) {
            expectedResponseCode = HttpConstants.HTTP_STATUS_BAD_REQUEST;
            expectedResponseString = "{\"code\":4001,\"message\":\"String 'Z234567AB' wurde zu SA/SAA 'Z234567AB' formatiert. Das Ergebnis entspricht nicht der Vorgabe\"}";
        }
        executeWebservice(tmpStr, RESTfulEndpoint.HttpMethod.DELETE,
                          "", additionalRequestProperties,
                          expectedResponseString,
                          expectedResponseCode);

        // ----------------------------
        // SAA-Test [3]
        // ----------------------------

        // SAA mit "Z0*" wird als "Z *" verarbeitet
        // Die SAA [Z03456789] gibt's nicht, ohne Anlegen löschen muss zu einer Fehlerausgabe führen mit "Z *" in der Fehlerausgabe.
        remarkId = new iPartsNutzDokRemarkId("Z03456789", "SAA", "UTDAIMLER10558");
        // Bemerkungstext für SAA löschen ...
        tmpStr = deleteSaaRemarksURI + "/" + remarkId.getRefId() + "/annotation/" + remarkId.getRemarkId();
        expectedResponseCode = HttpConstants.HTTP_STATUS_OK;
        expectedResponseString = "";
        if (!doSaveViaCortex) {
            expectedResponseCode = HttpConstants.HTTP_STATUS_NOT_FOUND;
            expectedResponseString = "{\"code\":4041,\"message\":\"No SAA work basket item found for 'Z 3456789'\"}";
        }
        executeWebservice(tmpStr, RESTfulEndpoint.HttpMethod.DELETE,
                          "", additionalRequestProperties,
                          expectedResponseString,
                          expectedResponseCode);

        // ----------------------------
        // SAA-Test [4]
        // ----------------------------

        // Auch eine SAA mit "Z *" sollte akzeptiert werden.
        // Die SAA [Z 3456789] gibt's nicht
        remarkId = new iPartsNutzDokRemarkId("Z%204187824", "SAA", "UTDAIMLER10558");
        // Bemerkungstext für SAA löschen ...
        tmpStr = deleteSaaRemarksURI + "/" + remarkId.getRefId() + "/annotation/" + remarkId.getRemarkId();
        expectedResponseCode = HttpConstants.HTTP_STATUS_OK;
        expectedResponseString = "";
        if (!doSaveViaCortex) {
            expectedResponseCode = HttpConstants.HTTP_STATUS_NOT_FOUND;
            expectedResponseString = "{\"code\":4041,\"message\":\"No SAA work basket item found for 'Z 4187824'\"}";
        }
        executeWebservice(tmpStr, RESTfulEndpoint.HttpMethod.DELETE,
                          "", additionalRequestProperties,
                          expectedResponseString,
                          expectedResponseCode);

        // ----------------------------
        // SAA-Test [5]
        // ----------------------------

        // Sicherstellen, dass Bemerkungstext NICHT in der DB existiert
        remarkId = new iPartsNutzDokRemarkId("Z 4187824", "SAA", "SANN111");
        remark = new iPartsDataNutzDokRemark(getProject(), remarkId);
        if (remark.existsInDB()) {
            remark.deleteFromDB();
        }

        expectedResponseCode = HttpConstants.HTTP_STATUS_OK;
        // SAA [Z 4187824] mit Annotation [SANN111] anlegen.
        executeWebservice(iPartsWSConstructionKitsRemarksEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\n" +
                          "  \"id\": \"" + remarkId.getRemarkId() + "\",\n" +
                          "  \"refId\": \"" + remarkId.getRefId() + "\",\n" +   // KEM oder SAA
                          "  \"type\": \"" + remarkId.getRefType() + "\",\n" +
                          "  \"user\": \"UnitTestUser\",\n" +
                          "  \"updateTs\": \"2020-06-15T11:34:27+01:00\",\n" +
                          "  \"data\": \"e1xydGYxXGFuc2lcYW5zaWNwZzEyNTJcZGVmZjB7XGZvbnR0Ymx7XGYwXGZuaWxcZmNoYXJzZXQwIE1TIFNhbnMgU2VyaWY7fX0NClx2aWV3a2luZDRcdWMxXHBhcmRcbGFuZzEwMzFcZjBcZnMxNiBOZXVlIEdldHJpZWJlIHd1cmRlbiB2b24gVVYgLT4gR0VaIC8gV2FubiBzZXR6ZW4gZGllIGJlaWRlbiBuZXVlbiBBIDk1NiAyNzAgMTEgMDEgVzIyMjdBMDggKyBBIDk1NiAyNzgwIDEyIDAxIFcyMjI3QTA5IGluIGRlciBTZXJpZSBlaW4uIEJldHJvZmZlbmUgU3RcJ2ZjbGkgaXN0IHZvbiAyMDAzIHVuZCBkYSBpc3QgbGVpZGVyIGtlaW4gVGVybWluIGVyc2ljaHRsaWNoISBTdFwnZmNja2xpc3Rlbi1GcmVpZ2FiZSBlcmZvbGd0IGx0LiBLXCdjNEEgZHVyY2ggQ1RULCBkYWhlciBBdWZ0cmFnIG5pY2h0IEVULXJlbGV2YW50LlxwYXINCn0NCgA=\"\n" +
                          "}", additionalRequestProperties,
                          "",
                          expectedResponseCode);

        if (!doSaveViaCortex) {
            // Kontrollieren, ob der Datensatz angelegt wurde:
            remark = new iPartsDataNutzDokRemark(getProject(), remarkId);
            assertTrue(remark.existsInDB());
        }

        // Bemerkungstext für SAA "Z%20..." löschen, also angelegt als: "Z 4187824", gelöscht wird nun "Z%204187824"
        tmpStr = deleteSaaRemarksURI + "/" + "Z%204187824" + "/annotation/" + remarkId.getRemarkId();
        expectedResponseCode = HttpConstants.HTTP_STATUS_OK;
        executeWebservice(tmpStr, RESTfulEndpoint.HttpMethod.DELETE,
                          "", additionalRequestProperties,
                          "",
                          expectedResponseCode);

        if (!doSaveViaCortex) {
            // Checken, ob der Datensatz auch wirklich gelöscht wurde
            remark = new iPartsDataNutzDokRemark(getProject(), remarkId);
            assertFalse(remark.existsInDB());
        }
    }


    public void testCortexNutzdokConstructionKitsAnnotations() {
        // Test darf nur durchlaufen für einen bestehenden Clone
        if (StrUtils.isEmpty(CLONE_SID)) {
            System.out.println("No Test testCortexNutzdokConstructionKitsAnnotations() without CloneSid");
            return;
        }
        Map<String, String> additionalRequestProperties = createAdditionalRequestPropertiesForNutzdokToken("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiZGUiLCJsYW5nMiI6ImZyIiwibGFuZzMiOiJlcyIsImlhdCI6MTUxNjU3NTYwMCwiZXhwIjoxNzE5NzcyNDAwLCJwZXJtaXNzaW9ucyI6eyJNQiI6WyJUUlVDSyJdfX0.otYXLd7qIm62SPwXuYZKMaOvMXk6t8bvli0tNo860bi2qbyabp4mwiANF-PsLVTSWLE1X-kmS3PigKTgKLcey2HEeRq-sNByXeOUp-6uRtDhwGfDwfEYB_zCenTMXHmJiqWA_QM8qiaWqrWCfiHxLvWHwr253XYXfxUOLN6dh2M");
        iPartsNutzDokRemarkId remarkId;
        String tmpStr;

        String requestString = "{\n    \"workBasketItems\": [{\n            \"id\": \"Z 541.878/24\",\n            \"type\": \"SAa\",\n            \"groupId\": \"42\",\n            \"ver\": \"Papier\",\n            \"toFrom\": false,\n            \"ver\": \"FUSO\",\n            \"flash\": false,\n            \"priority\": false,\n            \"evo\": false,\n            \"tc\": false,\n            \"lastUser\": \"JUSTAUDE\",\n            \"beginUsageTs\": \"2013-03-01T00:00:00+02:00\",\n            \"manualStartTs\": \"2013-03-05T14:07:00+02:00\"\n        }, {\n            \"id\": \"ZAA0503812N02\",\n            \"type\": \"KEM\",\n            \"docTeam\": \"CB\",\n            \"docUser\": \"RSTEINE\",\n            \"groupId\": \"35\",\n            \"marker\": \"N E U\",\n            \"paper\": true,\n            \"toFrom\": false,\n            \"simplified\": false,\n            \"flash\": false,\n            \"priority\": false,\n            \"evo\": false,\n            \"tc\": false,\n            \"lastUser\": \"RMIESKE\",\n            \"docStartTs\": \"2013-12-09T11:05:00+02:00\",\n            \"pemNo\": \"D53572\",\n            \"pemDate\": \"2017-01-06T00:00:00+02:00\",\n            \"pemStatus\": \"V\",\n            \"manualStartTs\": \"2017-03-05T14:07:00+02:00\"\n        }, {\n            \"id\": \"Z 541.878/26\",\n            \"type\": \"SAa\",\n            \"groupId\": \"42\",\n            \"ver\": \"Papier\",\n            \"toFrom\": false,\n            \"ver\": \"FUSO\",\n            \"flash\": false,\n            \"priority\": false,\n            \"evo\": false,\n            \"tc\": false,\n            \"lastUser\": \"JUSTAUDE\",\n            \"beginUsageTs\": \"2013-03-01T00:00:00+02:00\",\n            \"manualStartTs\": \"2013-03-05T14:07:00+02:00\"\n        }\n    ]\n} ";
        executeWebservice(iPartsWSConstructionKitsEndpoint.DEFAULT_ENDPOINT_URI,
                          requestString, additionalRequestProperties,
                          "",
                          HttpConstants.HTTP_STATUS_OK);


        // ----------------------------------
        //           _  _______ __  __
        //          | |/ / ____|  \/  |
        //          | ' /|  _| | |\/| |
        //          | . \| |___| |  | |
        //          |_|\_\_____|_|  |_|
        //
        // ----------------------------------

        // Bemerkungstexte für KEMs + SAAs anlegen mit:
        // PUT
        // http://localhost:8080/FrameworkApp/iPartsEdit/constructionKits/annotation

        // Bemerkungstexte für KEM-Datensätze löschen mit:
        // DELETE
        // http://localhost:1235//iPartsEdit/constructionKits/KEM/KEM_UT_001/annotation/UTDAIMLER10558

        String deleteKemRemarksURI = iPartsWSConstructionKitsEndpoint.DEFAULT_ENDPOINT_URI + iPartsWSDeleteConstructionKitsKemRemarkEndpoint.ENDPOINT_URI_SUFFIX;

        // ----------------------------
        // KEM Test [1]
        // ----------------------------

        // Kem-Id besetzen
        remarkId = new iPartsNutzDokRemarkId("KEM_UT_001", "KEM", "UTDAIMLER10558");
//
        // Bemerkungstext für KEM anlegen
        executeWebservice(iPartsWSConstructionKitsRemarksEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\n" +
                          "  \"id\": \"" + remarkId.getRemarkId() + "\",\n" +
                          "  \"refId\": \"" + remarkId.getRefId() + "\",\n" +   // KEM oder SAA
                          "  \"type\": \"" + remarkId.getRefType() + "\",\n" +
                          "  \"user\": \"UnitTestUser\",\n" +
                          "  \"updateTs\": \"2020-06-15T11:34:27+01:00\",\n" +
                          "  \"data\": \"e1xydGYxXGFuc2lcYW5zaWNwZzEyNTJcZGVmZjB7XGZvbnR0Ymx7XGYwXGZuaWxcZmNoYXJzZXQwIE1TIFNhbnMgU2VyaWY7fX0NClx2aWV3a2luZDRcdWMxXHBhcmRcbGFuZzEwMzFcZjBcZnMxNiBOZXVlIEdldHJpZWJlIHd1cmRlbiB2b24gVVYgLT4gR0VaIC8gV2FubiBzZXR6ZW4gZGllIGJlaWRlbiBuZXVlbiBBIDk1NiAyNzAgMTEgMDEgVzIyMjdBMDggKyBBIDk1NiAyNzgwIDEyIDAxIFcyMjI3QTA5IGluIGRlciBTZXJpZSBlaW4uIEJldHJvZmZlbmUgU3RcJ2ZjbGkgaXN0IHZvbiAyMDAzIHVuZCBkYSBpc3QgbGVpZGVyIGtlaW4gVGVybWluIGVyc2ljaHRsaWNoISBTdFwnZmNja2xpc3Rlbi1GcmVpZ2FiZSBlcmZvbGd0IGx0LiBLXCdjNEEgZHVyY2ggQ1RULCBkYWhlciBBdWZ0cmFnIG5pY2h0IEVULXJlbGV2YW50LlxwYXINCn0NCgA=\"\n" +
                          "}", additionalRequestProperties,
                          "",
                          HttpConstants.HTTP_STATUS_OK);

        // Bemerkungstext für KEM wieder löschen ...
        tmpStr = deleteKemRemarksURI + "/" + remarkId.getRefId() + "/annotation/" + remarkId.getRemarkId();
        executeWebservice(tmpStr, RESTfulEndpoint.HttpMethod.DELETE,
                          "", additionalRequestProperties,
                          "",
                          HttpConstants.HTTP_STATUS_OK);

        // ----------------------------------
        //           ____    _        _
        //          / ___|  / \      / \
        //          \___ \ / _ \    / _ \
        //           ___) / ___ \  / ___ \
        //          |____/_/   \_\/_/   \_\
        //
        // ----------------------------------

        String deleteSaaRemarksURI = iPartsWSConstructionKitsEndpoint.DEFAULT_ENDPOINT_URI + iPartsWSDeleteConstructionKitsSaaRemarkEndpoint.ENDPOINT_URI_SUFFIX;

        // ----------------------------
        // SAA-Test [1]
        // ----------------------------

        // Remark-Id besetzten
        remarkId = new iPartsNutzDokRemarkId("Z 4187824", "SAA", "SANN111");

        // SAA [Z 4187824] mit Annotation [SANN111] anlegen.
        executeWebservice(iPartsWSConstructionKitsRemarksEndpoint.DEFAULT_ENDPOINT_URI,
                          "{\n" +
                          "  \"id\": \"" + remarkId.getRemarkId() + "\",\n" +
                          "  \"refId\": \"" + remarkId.getRefId() + "\",\n" +   // KEM oder SAA
                          "  \"type\": \"" + remarkId.getRefType() + "\",\n" +
                          "  \"user\": \"UnitTestUser\",\n" +
                          "  \"updateTs\": \"2020-06-15T11:34:27+01:00\",\n" +
                          "  \"data\": \"e1xydGYxXGFuc2lcYW5zaWNwZzEyNTJcZGVmZjB7XGZvbnR0Ymx7XGYwXGZuaWxcZmNoYXJzZXQwIE1TIFNhbnMgU2VyaWY7fX0NClx2aWV3a2luZDRcdWMxXHBhcmRcbGFuZzEwMzFcZjBcZnMxNiBOZXVlIEdldHJpZWJlIHd1cmRlbiB2b24gVVYgLT4gR0VaIC8gV2FubiBzZXR6ZW4gZGllIGJlaWRlbiBuZXVlbiBBIDk1NiAyNzAgMTEgMDEgVzIyMjdBMDggKyBBIDk1NiAyNzgwIDEyIDAxIFcyMjI3QTA5IGluIGRlciBTZXJpZSBlaW4uIEJldHJvZmZlbmUgU3RcJ2ZjbGkgaXN0IHZvbiAyMDAzIHVuZCBkYSBpc3QgbGVpZGVyIGtlaW4gVGVybWluIGVyc2ljaHRsaWNoISBTdFwnZmNja2xpc3Rlbi1GcmVpZ2FiZSBlcmZvbGd0IGx0LiBLXCdjNEEgZHVyY2ggQ1RULCBkYWhlciBBdWZ0cmFnIG5pY2h0IEVULXJlbGV2YW50LlxwYXINCn0NCgA=\"\n" +
                          "}", additionalRequestProperties,
                          "",
                          HttpConstants.HTTP_STATUS_OK);

        // Bemerkungstext für SAA "Z%20..." löschen, also angelegt als: "Z 4187824", gelöscht wird nun "Z%204187824"
        tmpStr = deleteSaaRemarksURI + "/" + "Z%204187824" + "/annotation/" + remarkId.getRemarkId();
        executeWebservice(tmpStr, RESTfulEndpoint.HttpMethod.DELETE,
                          "", additionalRequestProperties,
                          "",
                          HttpConstants.HTTP_STATUS_OK);

    }
}
