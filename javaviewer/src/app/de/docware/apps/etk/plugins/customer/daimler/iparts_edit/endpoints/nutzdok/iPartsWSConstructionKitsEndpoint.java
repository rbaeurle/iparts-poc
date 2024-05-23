/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.nutzdok;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumValue;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.webservice.endpoints.helper.WSHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSaa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsNutzDokKEMId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsNutzDokSAAId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ImportExportLogHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDataScopeKgMappingCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportDateHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.config.iPartsNutzDokProcessingState;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataNutzDokKEM;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataNutzDokSAA;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.iPartsCortexImportEndpointNames;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.iPartsCortexImportMethod;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.iPartsWSAbstractEndpointNutzDok;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.iPartsWSWorkBasketItem;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.framework.modules.webservice.restful.WSError;
import de.docware.framework.modules.webservice.restful.annotations.Consumes;
import de.docware.framework.modules.webservice.restful.annotations.Produces;
import de.docware.framework.modules.webservice.restful.annotations.methods.POST;
import de.docware.framework.modules.webservice.restful.annotations.methods.PUT;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateUtils;
import de.docware.util.sql.TableAndFieldName;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Webservice für das Anlegen von Arbeitsaufträgen aus NutzDok, für KEMs oder SAAs, in den Tabellen
 * {@link iPartsConst#TABLE_DA_NUTZDOK_KEM} und {@link iPartsConst#TABLE_DA_NUTZDOK_SAA}).
 */
public class iPartsWSConstructionKitsEndpoint extends iPartsWSAbstractEndpointNutzDok<iPartsWSConstructionKitsRequest> implements iPartsConst {

    public static final String DEFAULT_ENDPOINT_URI = "/iPartsEdit/constructionKits";

    public iPartsWSConstructionKitsEndpoint(String endpointUri) {
        super(endpointUri);
        setResponseCacheSize(0); // Der ConstructionKits Webservice hat keinen JSON-Response-Cache
    }

    /**
     * DAIMLER-5088: PUT-Aufruf soll auch unterstützt werden und sich genau wir der POST-Aufruf verhalten.
     *
     * @return
     */
    @PUT
    @POST
    @Produces(MimeTypes.MIME_TYPE_JSON)
    @Consumes(MimeTypes.MIME_TYPE_JSON)
    public RESTfulTransferObjectInterface handleWebserviceRequest(iPartsWSConstructionKitsRequest requestObject) {
        return handleWebserviceRequestIntern(requestObject);
    }

    @Override
    protected RESTfulTransferObjectInterface executeWebservice(EtkProject project, iPartsWSConstructionKitsRequest requestObject) throws RESTfulWebApplicationException {

        ImportExportLogHelper logHelper = ImportExportLogHelper.createLogHelperWithRunningJob("Import constructionKits WebService");

        CortexResult cortexResult = createAndSaveCortexElement(project, logHelper, requestObject,
                                                               iPartsCortexImportEndpointNames.CONSTRUCTION_KITS,
                                                               iPartsCortexImportMethod.INSERT);
        if (cortexResult == CortexResult.OK_STOP) {
            // keine weitere WebService-Endpoint Logik
            iPartsJobsManager.getInstance().jobProcessed(logHelper.getLogFile());
        } else if (cortexResult == CortexResult.ERROR) {
            // es ist ein Fehler aufgetreten beim Speichern des CortexRecordsImports
            iPartsJobsManager.getInstance().jobError(logHelper.getLogFile());
        } else {
            // der ehemalige WebService Endpoint
            try {
                GenericEtkDataObjectList<EtkDataObject> dataObjectsToBeSaved = new GenericEtkDataObjectList<>();
                Set<String> kemNoSet = new HashSet<>();
                int nSAAs = 0;
                for (iPartsWSWorkBasketItem workBasketItem : requestObject.getWorkBasketItems()) {
                    EtkDataObject dataObject = handleWSWorkBasketItem(project, workBasketItem, logHelper);
                    if (dataObject != null) {
                        if (dataObject.getTableName().equals(TABLE_DA_NUTZDOK_KEM)) {
                            kemNoSet.add(((iPartsDataNutzDokKEM)dataObject).getAsId().getKEMNo());
                        } else {
                            nSAAs++;
                        }
                        dataObjectsToBeSaved.add(dataObject, DBActionOrigin.FROM_EDIT);
                    }
                }

                // Arbeitsvorrat für aufgesammelte KEMs berechnen
                iPartsWBItemForKemCalculator kemHelper = new iPartsWBItemForKemCalculator(project, logHelper);
                kemHelper.calcWorkBasketItemsForKem(kemNoSet, dataObjectsToBeSaved);

                saveInTransaction(project, dataObjectsToBeSaved);

                logHelper.fireLineSeparator();
                logHelper.addLogMsgWithTranslation("!!Import erfolgreich. Es wurden %1 KEMs und %2 SAAs importiert.",
                                                   Integer.toString(kemNoSet.size()),
                                                   Integer.toString(nSAAs));
                iPartsJobsManager.getInstance().jobProcessed(logHelper.getLogFile());
            } catch (RuntimeException e) {
                // Falls ein Fehler auftritt, diesen abfangen und weiterwerfen, damit man das Job-Log hier abbrechen kann.
                fireExceptionLogErrors(logHelper, null, e);
                iPartsJobsManager.getInstance().jobError(logHelper.getLogFile());
                throw e;
            }
        }
        return null;
    }

    /**
     * Einzelaufruf mit einem iPartsWSWorkBasketItem
     *
     * @param project
     * @param workBasketItem
     * @param logHelper
     * @return
     */
    private EtkDataObject handleWSWorkBasketItem(EtkProject project, iPartsWSWorkBasketItem workBasketItem,
                                                 ImportExportLogHelper logHelper) {
        if (workBasketItem.getTypeAsEnum() == iPartsWSWorkBasketItem.TYPE.KEM) {
            // KEM Arbeitsvorrat
            return handleKEMWorkBasketItem(project, workBasketItem, logHelper);
        } else if (workBasketItem.getTypeAsEnum() == iPartsWSWorkBasketItem.TYPE.SAA) {
            // SAA Arbeitsvorrat
            return handleSAAWorkBasketItem(project, workBasketItem, logHelper);
        } else {
            // Kann wegen der Validierung gar nicht passieren.
            throwBadRequestError(WSError.INTERNAL_ERROR, "Unknown type", null);
            return null;
        }
    }

    /**
     * Erzeugt aus dem übergebenen SAA Objekt {@link iPartsWSWorkBasketItem} ein Arbeitsvorrat Datenbankobjekt {@link iPartsDataNutzDokSAA}
     *
     * @param project
     * @param workBasketItem
     * @param logHelper
     * @return
     */
    private iPartsDataNutzDokSAA handleSAAWorkBasketItem(EtkProject project, iPartsWSWorkBasketItem workBasketItem,
                                                         ImportExportLogHelper logHelper) {
        String saaNo;
        try {
            saaNo = getRetailSaaInDbFormat(workBasketItem.getId());
        } catch (RuntimeException e) {
            throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, e.getMessage(), null);
            return null;
        }

        // Anlage der SA- und SAA-Stammdaten, wenn nicht vorhanden
        handleMissingSaaMasterData(project, workBasketItem, saaNo);

        iPartsNutzDokSAAId nutzDokSAAId = new iPartsNutzDokSAAId(saaNo);
        iPartsDataNutzDokSAA nutzDokSAA = new iPartsDataNutzDokSAA(project, nutzDokSAAId);
        boolean existedInDB = initIfNotExists(nutzDokSAA);
        fillNutzDokSaaAttributes(project, workBasketItem, nutzDokSAA);
        if (existedInDB) {
            if (!logAlreadyExistsMessage(nutzDokSAA, logHelper, true)) {
                return null;
            }
        }
        return nutzDokSAA;
    }

    private void handleMissingSaaMasterData(EtkProject project, iPartsWSWorkBasketItem workBasketItem, String saaNo) {
        // Fehlender SAA Stammdatensatz
        if (StrUtils.isValid(saaNo)) {
            // Erzeugen einer SAA-ID, um nach dieser in der Tabelle DA_SAA (Stammdaten) zu suchen
            iPartsSaaId masterDataSaaId = new iPartsSaaId(saaNo);
            // Anlage eines neuen DataSaa-Objekts mit dem EtkProject aus der aktuellen Session und der SAA-ID
            iPartsDataSaa masterDataSaa = new iPartsDataSaa(project, masterDataSaaId);
            // Überprüfung, ob dieser Datensatz bereits in der Datenbank existiert
            boolean masterDataSaaExistsInDb = initIfNotExists(masterDataSaa);
            // Falls dieser nicht existiert, wurde das Objekt in initIfNotExists() bereits mit Standard-Werten initialisiert
            // Erweiterung um EDAT, ADAT, SOURCE und der Original ID in CONST_SAA.
            if (!masterDataSaaExistsInDb) {
                fillSaaSaMasterDataAttributes(workBasketItem, masterDataSaa, saaNo);
                masterDataSaa.saveToDB();
            }
        }

        // Fehlender SA Stammdatensatz
        String saNo = iPartsNumberHelper.convertSAAtoSANumber(saaNo);
        if (StrUtils.isValid(saNo)) {
            // Erzeugen einer SA-ID, um nach dieser in der Tabelle DA_SA (Stammdaten) zu suchen
            iPartsSaId masterDataSaId = new iPartsSaId(saNo);
            // Anlage eines neuen DataSa-Objekts mit dem EtkProject aus der aktuellen Session und der SA-ID
            iPartsDataSa masterDataSa = new iPartsDataSa(project, masterDataSaId);
            // Überprüfung, ob dieser Datensatz bereits in der Datenbank existiert
            boolean masterDataSaExistsInDb = initIfNotExists(masterDataSa);
            // Falls dieser nicht existiert, wurde das Objekt in initIfNotExists() bereits mit Standard-Werten initialisiert
            // Erweiterung um EDAT, ADAT, SOURCE und der Original ID in CONST_SA.
            if (!masterDataSaExistsInDb) {
                fillSaaSaMasterDataAttributes(workBasketItem, masterDataSa, saNo);
                masterDataSa.saveToDB();
            }
        }
    }

    /**
     * Erzeugt aus dem übergebenen KEM Objekt {@link iPartsWSWorkBasketItem} ein Arbeitsvorrat Datenbankobjekt {@link iPartsDataNutzDokKEM}
     *
     * @param project
     * @param workBasketItem
     * @param logHelper
     * @return
     */
    private iPartsDataNutzDokKEM handleKEMWorkBasketItem(EtkProject project, iPartsWSWorkBasketItem workBasketItem,
                                                         ImportExportLogHelper logHelper) {
        iPartsNutzDokKEMId nutzDokKEMId = new iPartsNutzDokKEMId(workBasketItem.getId());
        iPartsDataNutzDokKEM nutzDokKEM = new iPartsDataNutzDokKEM(project, nutzDokKEMId);
        boolean existedInDB = initIfNotExists(nutzDokKEM);
        fillNutzDokKemAttributes(project, workBasketItem, nutzDokKEM);
        if (existedInDB) {
            if (!logAlreadyExistsMessage(nutzDokKEM, logHelper, true)) {
                return null;
            }
        }
        return nutzDokKEM;
    }

    /**
     * Befüllt das SAA Datenbank Objekt mit den Informationen aus dem SAA {@link iPartsWSWorkBasketItem}
     *
     * @param project
     * @param workBasketItem
     * @param nutzDokSAA
     */
    private void fillNutzDokSaaAttributes(EtkProject project, iPartsWSWorkBasketItem workBasketItem, EtkDataObject nutzDokSAA) {
        String scopeID = WSHelper.getEmptyStringForNull(workBasketItem.getGroupId());
        nutzDokSAA.setFieldValue(FIELD_DNS_SCOPE_ID, scopeID, DBActionOrigin.FROM_EDIT);
        nutzDokSAA.setFieldValue(FIELD_DNS_GROUP, iPartsDataScopeKgMappingCache.getKGForScopeId(project, scopeID), DBActionOrigin.FROM_EDIT);
        nutzDokSAA.setFieldValueAsBoolean(FIELD_DNS_TO_FROM_FLAG, workBasketItem.isToFrom(), DBActionOrigin.FROM_EDIT);
        nutzDokSAA.setFieldValueAsBoolean(FIELD_DNS_FLASH_FLAG, workBasketItem.isFlash(), DBActionOrigin.FROM_EDIT);
        nutzDokSAA.setFieldValueAsBoolean(FIELD_DNS_EVO_FLAG, workBasketItem.isEvo(), DBActionOrigin.FROM_EDIT);
        nutzDokSAA.setFieldValueAsBoolean(FIELD_DNS_PRIORITY_FLAG, workBasketItem.isPriority(), DBActionOrigin.FROM_EDIT);
        nutzDokSAA.setFieldValueAsBoolean(FIELD_DNS_TC_FLAG, workBasketItem.isTc(), DBActionOrigin.FROM_EDIT);
        nutzDokSAA.setFieldValue(FIELD_DNS_DISTRIBUTION, WSHelper.getEmptyStringForNull(workBasketItem.getVer()), DBActionOrigin.FROM_EDIT);
        nutzDokSAA.setFieldValue(FIELD_DNS_EVALUATION_FLAG, WSHelper.getEmptyStringForNull(workBasketItem.getAus()), DBActionOrigin.FROM_EDIT);

        setETSFieldFromRequest(project, workBasketItem, nutzDokSAA, TableAndFieldName.make(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_ETS));

        nutzDokSAA.setFieldValue(FIELD_DNS_LAST_USER, WSHelper.getEmptyStringForNull(workBasketItem.getLastUser()), DBActionOrigin.FROM_EDIT);
        nutzDokSAA.setFieldValue(FIELD_DNS_DOCU_START_DATE, XMLImportExportDateHelper.getTimeZoneDateAsISODate(workBasketItem.getDocStartTs()), DBActionOrigin.FROM_EDIT);
        nutzDokSAA.setFieldValue(FIELD_DNS_MANUAL_START_DATE, XMLImportExportDateHelper.getTimeZoneDateAsISODate(workBasketItem.getManualStartTs()), DBActionOrigin.FROM_EDIT);

        // spezifisch
        if (workBasketItem.getPlanNumber() != null) {
            nutzDokSAA.setFieldValueAsInteger(FIELD_DNS_PLAN_NUMBER, workBasketItem.getPlanNumber(), DBActionOrigin.FROM_EDIT);
        }
        nutzDokSAA.setFieldValue(FIELD_DNS_BEGIN_USAGE_DATE, XMLImportExportDateHelper.getTimeZoneDateAsISODate(workBasketItem.getBeginUsageTs()), DBActionOrigin.FROM_EDIT);

        if (StrUtils.isEmpty(nutzDokSAA.getFieldValue(FIELD_DNS_PROCESSING_STATE))) {
            nutzDokSAA.setFieldValue(FIELD_DNS_PROCESSING_STATE, iPartsNutzDokProcessingState.NEW.getDBValue(), DBActionOrigin.FROM_EDIT);
        }
    }

    /**
     * Befüllt das KEM Datenbank Objekt mit den Informationen aus dem KEM {@link iPartsWSWorkBasketItem}
     *
     * @param project
     * @param workBasketItem
     * @param nutzDokKEM
     */
    private void fillNutzDokKemAttributes(EtkProject project, iPartsWSWorkBasketItem workBasketItem, EtkDataObject nutzDokKEM) {
        String scopeID = WSHelper.getEmptyStringForNull(workBasketItem.getGroupId());
        nutzDokKEM.setFieldValue(FIELD_DNK_SCOPE_ID, scopeID, DBActionOrigin.FROM_EDIT);
        nutzDokKEM.setFieldValue(FIELD_DNK_GROUP, iPartsDataScopeKgMappingCache.getKGForScopeId(project, scopeID), DBActionOrigin.FROM_EDIT);
        nutzDokKEM.setFieldValueAsBoolean(FIELD_DNK_TO_FROM_FLAG, workBasketItem.isToFrom(), DBActionOrigin.FROM_EDIT);
        nutzDokKEM.setFieldValueAsBoolean(FIELD_DNK_FLASH_FLAG, workBasketItem.isFlash(), DBActionOrigin.FROM_EDIT);
        nutzDokKEM.setFieldValueAsBoolean(FIELD_DNK_EVO_FLAG, workBasketItem.isEvo(), DBActionOrigin.FROM_EDIT);
        nutzDokKEM.setFieldValueAsBoolean(FIELD_DNK_PRIORITY_FLAG, workBasketItem.isPriority(), DBActionOrigin.FROM_EDIT);
        nutzDokKEM.setFieldValueAsBoolean(FIELD_DNK_TC_FLAG, workBasketItem.isTc(), DBActionOrigin.FROM_EDIT);
        nutzDokKEM.setFieldValue(FIELD_DNK_DISTRIBUTION, WSHelper.getEmptyStringForNull(workBasketItem.getVer()), DBActionOrigin.FROM_EDIT);
        nutzDokKEM.setFieldValue(FIELD_DNK_EVALUATION_FLAG, WSHelper.getEmptyStringForNull(workBasketItem.getAus()), DBActionOrigin.FROM_EDIT);

        setETSFieldFromRequest(project, workBasketItem, nutzDokKEM, TableAndFieldName.make(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_ETS));

        nutzDokKEM.setFieldValue(FIELD_DNK_LAST_USER, WSHelper.getEmptyStringForNull(workBasketItem.getLastUser()), DBActionOrigin.FROM_EDIT);
        nutzDokKEM.setFieldValue(FIELD_DNK_DOCU_START_DATE, XMLImportExportDateHelper.getTimeZoneDateAsISODate(workBasketItem.getDocStartTs()), DBActionOrigin.FROM_EDIT);
        nutzDokKEM.setFieldValue(FIELD_DNK_MANUAL_START_DATE, XMLImportExportDateHelper.getTimeZoneDateAsISODate(workBasketItem.getManualStartTs()), DBActionOrigin.FROM_EDIT);

        // spezifisch
        nutzDokKEM.setFieldValue(FIELD_DNK_DOCU_TEAM, WSHelper.getEmptyStringForNull(workBasketItem.getDocTeam()), DBActionOrigin.FROM_EDIT);
        nutzDokKEM.setFieldValue(FIELD_DNK_DOCU_USER, WSHelper.getEmptyStringForNull(workBasketItem.getDocUser()), DBActionOrigin.FROM_EDIT);
        nutzDokKEM.setFieldValue(FIELD_DNK_REMARK, WSHelper.getEmptyStringForNull(workBasketItem.getMarker()), DBActionOrigin.FROM_EDIT);
        nutzDokKEM.setFieldValueAsBoolean(FIELD_DNK_SIMPLIFIED_FLAG, workBasketItem.isSimplified(), DBActionOrigin.FROM_EDIT);
        nutzDokKEM.setFieldValueAsBoolean(FIELD_DNK_PAPER_FLAG, workBasketItem.isPaper(), DBActionOrigin.FROM_EDIT);
        nutzDokKEM.setFieldValue(FIELD_DNK_PEM, WSHelper.getEmptyStringForNull(workBasketItem.getPemNo()), DBActionOrigin.FROM_EDIT);
        nutzDokKEM.setFieldValue(FIELD_DNK_PEM_DATE, XMLImportExportDateHelper.getTimeZoneDateAsISODate(workBasketItem.getPemDate()), DBActionOrigin.FROM_EDIT);
        nutzDokKEM.setFieldValue(FIELD_DNK_PEM_STATUS, WSHelper.getEmptyStringForNull(workBasketItem.getPemStatus()), DBActionOrigin.FROM_EDIT);

        if (StrUtils.isEmpty(nutzDokKEM.getFieldValue(FIELD_DNK_PROCESSING_STATE))) {
            nutzDokKEM.setFieldValue(FIELD_DNK_PROCESSING_STATE, iPartsNutzDokProcessingState.NEW.getDBValue(), DBActionOrigin.FROM_EDIT);
        }
    }

    /**
     * Setzt die markt-spezifischen ET-Sichten aus der Webservice-Anfrage
     *
     * @param project
     * @param workBasketItem
     * @param nutzDokDataObject
     * @param tableAndFieldName
     */
    private void setETSFieldFromRequest(EtkProject project, iPartsWSWorkBasketItem workBasketItem,
                                        EtkDataObject nutzDokDataObject, String tableAndFieldName) {
        if (workBasketItem.getEts() != null) {
            String edsMarketEtkzEnumKey = project.getEtkDbs().getEnum(tableAndFieldName);
            EnumValue enumValues = project.getEtkDbs().getEnumValue(edsMarketEtkzEnumKey);
            if ((enumValues == null)) {
                throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, "Unknown enum: '" + edsMarketEtkzEnumKey + "'", null);
            }
            List<String> enumTokens = new DwList<>();
            List<String> etsList = StrUtils.toStringList(workBasketItem.getEts(), ",", false, true);
            for (String ets : etsList) {
                String enumToken = ets;
                if (enumToken.contains("_")) { // Nur dann ab dem Underscore alles entfernen, wenn auch ein Underscore vorhanden ist
                    enumToken = StrUtils.stringUpToCharacter(enumToken, '_');
                }
                if ((enumValues == null) || !enumValues.containsKey(enumToken)) {
                    throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, "Unknown EDS market etkz: '" + enumToken + "'", null);
                }
                enumTokens.add(enumToken);
            }
            nutzDokDataObject.setFieldValueAsSetOfEnum(TableAndFieldName.getFieldName(tableAndFieldName), enumTokens, DBActionOrigin.FROM_EDIT);
        }
    }

    /**
     * Befüllt ein übergebenes SA(A) Stammdaten Objekt mit Werten aus dem Request-Payload (s. {@link iPartsWSWorkBasketItem})
     *
     * @param workBasketItem   Das iPartsWSWorkBasketItem aus dem Request-Payload
     * @param masterDataObject Das zu befüllende SA(A) Stammdaten Objekt
     * @param retailSaaSaNo    Die Retail-SA(A)-Nummer
     */
    private void fillSaaSaMasterDataAttributes(iPartsWSWorkBasketItem workBasketItem, EtkDataObject masterDataObject, String retailSaaSaNo) {
        String currentDateFormatted = DateUtils.getCurrentDateFormatted(DateUtils.simpleTimeFormatyyyyMMddHHmmss);
        masterDataObject.setFieldValue(FIELD_DS_EDAT, currentDateFormatted, DBActionOrigin.FROM_EDIT);
        masterDataObject.setFieldValue(FIELD_DS_ADAT, currentDateFormatted, DBActionOrigin.FROM_EDIT);
        masterDataObject.setFieldValue(FIELD_DS_SOURCE, iPartsImportDataOrigin.NUTZDOK.getOrigin(), DBActionOrigin.FROM_EDIT);

        // Wenn sich Konstruktions-SA(A) und Retail-SA(A) unterscheiden, dann die Konstruktions-SA(A) explizit abspeichern
        String constructionSaaSaNo = numberHelper.unformatSaaForDB(workBasketItem.getId());

        // Wenn das masterDataObject eine SAA ist, muss das Feld DS_CONST_SAA befüllt werden
        if (masterDataObject instanceof iPartsDataSaa) {
            if (!constructionSaaSaNo.equals(retailSaaSaNo)) {
                masterDataObject.setFieldValue(FIELD_DS_CONST_SAA, constructionSaaSaNo, DBActionOrigin.FROM_EDIT);
            }
        } else { // ansonsten ist es eine SA und das Feld DS_CONST_SA muss befüllt werden
            constructionSaaSaNo = iPartsNumberHelper.convertSAAtoSANumber(constructionSaaSaNo);
            if ((constructionSaaSaNo != null) && !constructionSaaSaNo.equals(retailSaaSaNo)) {
                masterDataObject.setFieldValue(FIELD_DS_CONST_SA, constructionSaaSaNo, DBActionOrigin.FROM_EDIT);
            }
        }
    }
}
