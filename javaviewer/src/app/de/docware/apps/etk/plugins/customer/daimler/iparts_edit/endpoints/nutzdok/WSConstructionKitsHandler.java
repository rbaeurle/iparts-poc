/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.nutzdok;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumValue;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.webservice.endpoints.helper.WSHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSaa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataNutzDokAnnotation;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsNutzDokAnnotationId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsNutzDokKEMId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsNutzDokSAAId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ImportExportLogHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDataScopeKgMappingCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.config.iPartsNutzDokProcessingState;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataNutzDokKEM;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataNutzDokSAA;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.iPartsCortexImportEndpointNames;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.iPartsDataCortexImport;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.iPartsDataCortexImportList;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.iPartsWSWorkBasketItem;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.iPartsWSWorkBasketItemAnnotation;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Handler des Cortex Schedulers für CONSTRUCTION_KITS
 */
public class WSConstructionKitsHandler extends AbstractWSConstructionKitsHandler {

    private Set<String> kemNoSet;
    private Map<IdWithType, EtkDataObject> saaMap;
    private Map<IdWithType, EtkDataObject> kemMap;
    private int nSAAs;
    private EnumValue etsEnumValues;
    private String docuStartDate;

    public WSConstructionKitsHandler(EtkProject project) {
        super(project, iPartsCortexImportEndpointNames.CONSTRUCTION_KITS);
    }

    public WSConstructionKitsHandler(EtkProject project, ImportExportLogHelper logHelper) {
        super(project, iPartsCortexImportEndpointNames.CONSTRUCTION_KITS, logHelper);
    }

    @Override
    protected boolean doBeforeLogic() {
        return true;
    }

    /**
     * Lokale Variablen initialisieren
     *
     * @param handleList
     * @return
     */
    @Override
    protected boolean init(iPartsDataCortexImportList handleList) {
        boolean result = super.init(handleList);
        if (result) {
            saaMap = new HashMap<>();
            kemMap = new HashMap<>();
            kemNoSet = new HashSet<>();
            nSAAs = 0;
            etsEnumValues = getEnumValues(TableAndFieldName.make(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_ETS));
            if (etsEnumValues == null) {
                // probier noch die 2. Tabelle
                etsEnumValues = getEnumValues(TableAndFieldName.make(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_ETS));
                if (etsEnumValues == null) {
                    etsEnumValues = new EnumValue();
                }
            }
            // Datum ohne Uhrzeit
            docuStartDate = SQLStringConvert.calendarToPPDateString(Calendar.getInstance()) + StrUtils.leftFill("", 6, '0');
        }
        return result;
    }

    private EnumValue getEnumValues(String tableAndFieldName) {
        String edsMarketEtkzEnumKey = getProject().getEtkDbs().getEnum(tableAndFieldName);
        EnumValue enumValues = getProject().getEtkDbs().getEnumValue(edsMarketEtkzEnumKey);
        if (enumValues == null) {
            addLogWarning("!!Ungültiger enum-Wert für Tabelle: \"%1\"", tableAndFieldName);
        }
        return enumValues;
    }

    /**
     * Die eigentliche Ausführung des CONSTRUCTION_KITS Handlers
     *
     * @param dataCortexImport
     * @return
     */
    protected boolean doExecute(iPartsDataCortexImport dataCortexImport) {
        // aus dem Blob des Cortex-Objects das Transfer-Object bilden
        iPartsWSConstructionKitsRequest requestObject = getWSObject(dataCortexImport,
                                                                    iPartsWSConstructionKitsRequest.class);
        if ((requestObject == null) || (requestObject.getWorkBasketItems() == null)) {
            return false;
        }
        saaMap.clear();
        kemMap.clear();
        // beim Transfer-Object handelt es sich um eine Liste von iPartsWSWorkBasketItems
        for (iPartsWSWorkBasketItem workBasketItem : requestObject.getWorkBasketItems()) {
            // Behandlung eines iPartsWSWorkBasketItems
            EtkDataObject dataObject = handleWSWorkBasketItem(workBasketItem);
            if (dataObject != null) {
                // Zwischenergebnisse merken
                if (dataObject.getTableName().equals(TABLE_DA_NUTZDOK_KEM)) {
                    kemNoSet.add(((iPartsDataNutzDokKEM)dataObject).getAsId().getKEMNo());
                } else {
                    nSAAs++;
                }
                // und speichern
                addToSave(dataObject);
            }
        }
        // Schlussmeldung ausgeben
        return addLogMsgAfterDoExecute(dataCortexImport, true);
    }

    @Override
    protected void addToSave(EtkDataObject dataObject) {
        boolean doAdd = true;
        if (dataObject.getTableName().equals(TABLE_DA_NUTZDOK_SAA)) {
            // put liefert ein in der Map bereits vorhandenes Objekt zurück, sonst null
            doAdd = (saaMap.put(dataObject.getAsId(), dataObject) == null);
        } else if (dataObject.getTableName().equals(TABLE_DA_NUTZDOK_KEM)) {
            // put liefert ein in der Map bereits vorhandenes Objekt zurück, sonst null
            doAdd = (kemMap.put(dataObject.getAsId(), dataObject) == null);
        }
        if (doAdd) {
            super.addToSave(dataObject);
        }
    }


    /**
     * Nach Durchlauf aller CONSTRUCTION_KITS Cortex-Elemente den KemCalculator ausrufen
     *
     * @param handleList
     */
    @Override
    protected void doAfterExecute(iPartsDataCortexImportList handleList) {
        if (getExecuteResult()) {
            // Arbeitsvorrat für aufgesammelte KEMs berechnen
            // steht Ende des Imports aller Einzeldateien
            iPartsWBItemForKemCalculator kemHelper = new iPartsWBItemForKemCalculator(getProject(), getLogHelper());
            if (kemHelper.calcWorkBasketItemsForKem(kemNoSet, getDataObjectsToBeSaved())) {
                // Speichern
                doSaveInTransaction();
            }
        }
        super.doAfterExecute(handleList);

        if (getExecuteResult()) {
            // Im Erfolgsfall Meldung
            getLogHelper().addLogMsgWithTranslation("!!Import erfolgreich. Es wurden %1 KEMs und %2 SAAs importiert.",
                                                    Integer.toString(kemNoSet.size()),
                                                    Integer.toString(nSAAs));
        }
    }

    /**
     * Typenbestimmung und Verarbeitung eines {@link iPartsWSWorkBasketItem}s
     *
     * @param workBasketItem
     * @return
     */
    private EtkDataObject handleWSWorkBasketItem(iPartsWSWorkBasketItem workBasketItem) {
        if (workBasketItem.getTypeAsEnum() == iPartsWSWorkBasketItem.TYPE.KEM) {
            // KEM Arbeitsvorrat
            return handleKEMWorkBasketItem(workBasketItem);
        } else if (workBasketItem.getTypeAsEnum() == iPartsWSWorkBasketItem.TYPE.SAA) {
            // SAA Arbeitsvorrat
            return handleSAAWorkBasketItem(workBasketItem);
        } else {
            // Kann wegen der Validierung gar nicht passieren.
            addLogError("!!Unbekannetr Typ: \"%1\".", workBasketItem.getType());
            return null;
        }
    }

    /**
     * Erzeugt aus dem übergebenen KEM Objekt {@link iPartsWSWorkBasketItem} ein Arbeitsvorrat Datenbankobjekt {@link iPartsDataNutzDokKEM}
     *
     * @param workBasketItem
     * @return
     */
    private iPartsDataNutzDokKEM handleKEMWorkBasketItem(iPartsWSWorkBasketItem workBasketItem) {
        iPartsDataNutzDokKEM nutzDokKEM;
        iPartsNutzDokKEMId nutzDokKEMId = new iPartsNutzDokKEMId(workBasketItem.getId());
        boolean existsInDB;
        EtkDataObject usedNutzDokKEM = kemMap.get(nutzDokKEMId);
        if (usedNutzDokKEM != null) {
            nutzDokKEM = (iPartsDataNutzDokKEM)usedNutzDokKEM;
            existsInDB = true;
        } else {
            nutzDokKEM = new iPartsDataNutzDokKEM(getProject(), nutzDokKEMId);
            existsInDB = initIfNotExists(nutzDokKEM);
        }

        fillNutzDokKemAttributes(workBasketItem, nutzDokKEM, existsInDB);
        if (existsInDB) {
            if (!logAlreadyExistsMessage(nutzDokKEM, true)) {
                return null;
            }
        }
        return nutzDokKEM;
    }

    /**
     * Erzeugt aus dem übergebenen SAA Objekt {@link iPartsWSWorkBasketItem} ein Arbeitsvorrat Datenbankobjekt {@link iPartsDataNutzDokSAA}
     *
     * @param workBasketItem
     * @return
     */
    private iPartsDataNutzDokSAA handleSAAWorkBasketItem(iPartsWSWorkBasketItem workBasketItem) {
        String saaNo = getSaaNoInDbFormat(workBasketItem.getId());
        if (StrUtils.isEmpty(saaNo)) {
            return null;
        }

        iPartsDataNutzDokSAA nutzDokSAA;
        iPartsNutzDokSAAId nutzDokSAAId = new iPartsNutzDokSAAId(saaNo);
        EtkDataObject usedNutzDokSAA = saaMap.get(nutzDokSAAId);
        if (usedNutzDokSAA == null) {
            // Anlage der SA- und SAA-Stammdaten, wenn nicht vorhanden
            handleMissingSaaMasterData(workBasketItem, saaNo);
        }

        boolean existsInDB;
        if (usedNutzDokSAA != null) {
            nutzDokSAA = (iPartsDataNutzDokSAA)usedNutzDokSAA;
            existsInDB = true;
        } else {
            nutzDokSAA = new iPartsDataNutzDokSAA(getProject(), nutzDokSAAId);
            existsInDB = initIfNotExists(nutzDokSAA);
        }

        fillNutzDokSaaAttributes(workBasketItem, nutzDokSAA, existsInDB);
        if (existsInDB) {
            if (!logAlreadyExistsMessage(nutzDokSAA, true)) {
                return null;
            }
        }
        return nutzDokSAA;
    }

    private void handleMissingSaaMasterData(iPartsWSWorkBasketItem workBasketItem, String saaNo) {
        // Fehlender SAA Stammdatensatz
        if (StrUtils.isValid(saaNo)) {
            // Erzeugen einer SAA-ID, um nach dieser in der Tabelle DA_SAA (Stammdaten) zu suchen
            iPartsSaaId masterDataSaaId = new iPartsSaaId(saaNo);
            // Anlage eines neuen DataSaa-Objekts mit dem EtkProject aus der aktuellen Session und der SAA-ID
            iPartsDataSaa masterDataSaa = new iPartsDataSaa(getProject(), masterDataSaaId);
            // Überprüfung, ob dieser Datensatz bereits in der Datenbank existiert
            boolean masterDataSaaExistsInDb = initIfNotExists(masterDataSaa);
            // Falls dieser nicht existiert, wurde das Objekt in initIfNotExists() bereits mit Standard-Werten initialisiert
            // Erweiterung um EDAT, ADAT, SOURCE und der Original ID in CONST_SAA.
            if (!masterDataSaaExistsInDb) {
                // Master SAA-Daten anlegen und speichern
                fillSaaSaMasterDataAttributes(workBasketItem, masterDataSaa, saaNo);
                addToSave(masterDataSaa);
            }
        }

        // Fehlender SA Stammdatensatz
        String saNo = iPartsNumberHelper.convertSAAtoSANumber(saaNo);
        if (StrUtils.isValid(saNo)) {
            // Erzeugen einer SA-ID, um nach dieser in der Tabelle DA_SA (Stammdaten) zu suchen
            iPartsSaId masterDataSaId = new iPartsSaId(saNo);
            // Anlage eines neuen DataSa-Objekts mit dem EtkProject aus der aktuellen Session und der SA-ID
            iPartsDataSa masterDataSa = new iPartsDataSa(getProject(), masterDataSaId);
            // Überprüfung, ob dieser Datensatz bereits in der Datenbank existiert
            boolean masterDataSaExistsInDb = initIfNotExists(masterDataSa);
            // Falls dieser nicht existiert, wurde das Objekt in initIfNotExists() bereits mit Standard-Werten initialisiert
            // Erweiterung um EDAT, ADAT, SOURCE und der Original ID in CONST_SA.
            if (!masterDataSaExistsInDb) {
                // Master SA-Daten anlegen und speichern
                fillSaaSaMasterDataAttributes(workBasketItem, masterDataSa, saNo);
                addToSave(masterDataSa);
            }
        }
    }


    /**
     * Befüllt das KEM Datenbank Objekt mit den Informationen aus dem KEM {@link iPartsWSWorkBasketItem}
     *
     * @param workBasketItem
     * @param nutzDokKEM
     * @param existsInDB
     */
    private void fillNutzDokKemAttributes(iPartsWSWorkBasketItem workBasketItem, EtkDataObject nutzDokKEM, boolean existsInDB) {
        String scopeID = WSHelper.getEmptyStringForNull(workBasketItem.getGroupId());
        setString(nutzDokKEM, FIELD_DNK_SCOPE_ID, scopeID);
        setString(nutzDokKEM, FIELD_DNK_GROUP, iPartsDataScopeKgMappingCache.getKGForScopeId(getProject(), scopeID));
        setBoolean(nutzDokKEM, FIELD_DNK_TO_FROM_FLAG, workBasketItem.isToFrom());
        setBoolean(nutzDokKEM, FIELD_DNK_FLASH_FLAG, workBasketItem.isFlash());
        setBoolean(nutzDokKEM, FIELD_DNK_EVO_FLAG, workBasketItem.isEvo());
        setBoolean(nutzDokKEM, FIELD_DNK_PRIORITY_FLAG, workBasketItem.isPriority());
        setBoolean(nutzDokKEM, FIELD_DNK_TC_FLAG, workBasketItem.isTc());
        setString(nutzDokKEM, FIELD_DNK_DISTRIBUTION, WSHelper.getEmptyStringForNull(workBasketItem.getVer()));
        setString(nutzDokKEM, FIELD_DNK_EVALUATION_FLAG, WSHelper.getEmptyStringForNull(workBasketItem.getAus()));

        Set<String> etsEnumTokens = new HashSet<>();
        setETSFieldFromRequest(workBasketItem, nutzDokKEM, FIELD_DNK_ETS, FIELD_DNK_ETS_UNCONFIRMED, existsInDB, etsEnumTokens);

        setString(nutzDokKEM, FIELD_DNK_LAST_USER, WSHelper.getEmptyStringForNull(workBasketItem.getLastUser()));
        if (!existsInDB && StrUtils.isEmpty(nutzDokKEM.getFieldValue(FIELD_DNK_DOCU_START_DATE))) {
            setString(nutzDokKEM, FIELD_DNK_DOCU_START_DATE, docuStartDate);
        }
        setString(nutzDokKEM, FIELD_DNK_MANUAL_START_DATE, getTimeZoneDateAsISODate(workBasketItem.getManualStartTs()));

        // spezifisch
        setString(nutzDokKEM, FIELD_DNK_DOCU_TEAM, WSHelper.getEmptyStringForNull(workBasketItem.getDocTeam()));
        setString(nutzDokKEM, FIELD_DNK_DOCU_USER, WSHelper.getEmptyStringForNull(workBasketItem.getDocUser()));
        setString(nutzDokKEM, FIELD_DNK_REMARK, WSHelper.getEmptyStringForNull(workBasketItem.getMarker()));
        setBoolean(nutzDokKEM, FIELD_DNK_SIMPLIFIED_FLAG, workBasketItem.isSimplified());
        setBoolean(nutzDokKEM, FIELD_DNK_PAPER_FLAG, workBasketItem.isPaper());
        setString(nutzDokKEM, FIELD_DNK_PEM, WSHelper.getEmptyStringForNull(workBasketItem.getPemNo()));
        setString(nutzDokKEM, FIELD_DNK_PEM_DATE, getTimeZoneDateAsISODate(workBasketItem.getPemDate()));
        setString(nutzDokKEM, FIELD_DNK_PEM_STATUS, WSHelper.getEmptyStringForNull(workBasketItem.getPemStatus()));

        if (StrUtils.isEmpty(nutzDokKEM.getFieldValue(FIELD_DNK_PROCESSING_STATE))) {
            setString(nutzDokKEM, FIELD_DNK_PROCESSING_STATE, iPartsNutzDokProcessingState.NEW.getDBValue());
        }
        handleKemOrSaaAnnotations(workBasketItem, nutzDokKEM, etsEnumTokens);
    }

    /**
     * Erzeugt die Annotation-Objects, falls vorhanden
     *
     * @param workBasketItem
     * @param nutzDokKEMorSAA
     * @param etsEnumTokens
     */
    private void handleKemOrSaaAnnotations(iPartsWSWorkBasketItem workBasketItem, EtkDataObject nutzDokKEMorSAA,
                                           Set<String> etsEnumTokens) {
        List<iPartsWSWorkBasketItemAnnotation> annotationList = workBasketItem.getAnnotation();
        if ((annotationList == null) || etsEnumTokens.isEmpty()) {
            return;
        }
        String refType;
        String refId;
        if (workBasketItem.getTypeAsEnum() == iPartsWSWorkBasketItem.TYPE.KEM) {
            refType = iPartsWSWorkBasketItem.TYPE.KEM.name();
            refId = ((iPartsDataNutzDokKEM)nutzDokKEMorSAA).getAsId().getKEMNo();
        } else {
            refType = iPartsWSWorkBasketItem.TYPE.SAA.name();
            refId = ((iPartsDataNutzDokSAA)nutzDokKEMorSAA).getAsId().getSAANo();
        }
        for (String ets : etsEnumTokens) {
            for (iPartsWSWorkBasketItemAnnotation annotation : annotationList) {
                iPartsNutzDokAnnotationId annotationId = new iPartsNutzDokAnnotationId(refId, refType, annotation.getDate(), ets,
                                                                                       StrUtils.leftFill(String.valueOf(annotation.getId()), 5, '0'));
                iPartsDataNutzDokAnnotation dataAnnotation = new iPartsDataNutzDokAnnotation(getProject(), annotationId);
                if (!dataAnnotation.existsInDB()) {
                    dataAnnotation.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                }
                dataAnnotation.setDate(getDateFromAnnotation(annotation.getDate()));
                dataAnnotation.setAuthor(WSHelper.getEmptyStringForNull(annotation.getEditor()));
                dataAnnotation.setText(WSHelper.getEmptyStringForNull(annotation.getText()));
                getDataObjectsToBeSaved().add(dataAnnotation, DBActionOrigin.FROM_EDIT);
            }
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
        setString(masterDataObject, FIELD_DS_EDAT, currentDateFormatted);
        setString(masterDataObject, FIELD_DS_ADAT, currentDateFormatted);
        setString(masterDataObject, FIELD_DS_SOURCE, iPartsImportDataOrigin.NUTZDOK.getOrigin());

        // Wenn sich Konstruktions-SA(A) und Retail-SA(A) unterscheiden, dann die Konstruktions-SA(A) explizit abspeichern
        String constructionSaaSaNo = numberHelper.unformatSaaForDB(workBasketItem.getId());

        // Wenn das masterDataObject eine SAA ist, muss das Feld DS_CONST_SAA befüllt werden
        if (masterDataObject instanceof iPartsDataSaa) {
            if (!constructionSaaSaNo.equals(retailSaaSaNo)) {
                setString(masterDataObject, FIELD_DS_CONST_SAA, constructionSaaSaNo);
            }
        } else { // ansonsten ist es eine SA und das Feld DS_CONST_SA muss befüllt werden
            constructionSaaSaNo = iPartsNumberHelper.convertSAAtoSANumber(constructionSaaSaNo);
            if (!constructionSaaSaNo.equals(retailSaaSaNo)) {
                setString(masterDataObject, FIELD_DS_CONST_SA, constructionSaaSaNo);
            }
        }
    }

    /**
     * Setzt die markt-spezifischen ET-Sichten aus der Webservice-Anfrage
     *
     * @param workBasketItem
     * @param nutzDokDataObject
     * @param etsFieldName
     * @param etsUnconfirmedFieldName
     * @param existsInDB
     * @param etsEnumTokens
     */
    private void setETSFieldFromRequest(iPartsWSWorkBasketItem workBasketItem,
                                        EtkDataObject nutzDokDataObject, String etsFieldName, String etsUnconfirmedFieldName,
                                        boolean existsInDB, Set<String> etsEnumTokens) {
        etsEnumTokens.clear();
        if (StrUtils.isValid(workBasketItem.getEts())) {
            List<String> etsList = StrUtils.toStringList(workBasketItem.getEts(), ",", false, true);
            for (String ets : etsList) {
                String enumToken = ets;
                if (enumToken.contains("_")) { // Nur dann ab dem Underscore alles entfernen, wenn auch ein Underscore vorhanden ist
                    enumToken = StrUtils.stringUpToCharacter(enumToken, '_');
                }
                if (!etsEnumValues.containsKey(enumToken)) {
                    addLogWarning("!!Ungültiger EDS Market Etkz: \"%1\"", enumToken);
                } else {
                    etsEnumTokens.add(enumToken);
                }
            }

            if (existsInDB) {
                List<String> enumList = nutzDokDataObject.getFieldValueAsSetOfEnum(etsFieldName);
                List<String> unconfirmedEnumTokens = nutzDokDataObject.getFieldValueAsSetOfEnum(etsUnconfirmedFieldName);

                for (String enumValue : etsEnumTokens) {
                    if (!enumList.contains(enumValue) && !unconfirmedEnumTokens.contains(enumValue)) {
                        unconfirmedEnumTokens.add(enumValue);
                    }
                }
                if (!unconfirmedEnumTokens.isEmpty()) {
                    nutzDokDataObject.setFieldValueAsSetOfEnum(etsUnconfirmedFieldName, unconfirmedEnumTokens, DBActionOrigin.FROM_EDIT);
                }
            } else {
                nutzDokDataObject.setFieldValueAsSetOfEnum(etsFieldName, etsEnumTokens, DBActionOrigin.FROM_EDIT);
            }
        }
    }

    /**
     * Befüllt das SAA Datenbank Objekt mit den Informationen aus dem SAA {@link iPartsWSWorkBasketItem}
     *
     * @param workBasketItem
     * @param nutzDokSAA
     * @param existsInDB
     */
    private void fillNutzDokSaaAttributes(iPartsWSWorkBasketItem workBasketItem, EtkDataObject nutzDokSAA, boolean existsInDB) {
        String scopeID = WSHelper.getEmptyStringForNull(workBasketItem.getGroupId());
        setString(nutzDokSAA, FIELD_DNS_SCOPE_ID, scopeID);
        setString(nutzDokSAA, FIELD_DNS_GROUP, iPartsDataScopeKgMappingCache.getKGForScopeId(getProject(), scopeID));
        setBoolean(nutzDokSAA, FIELD_DNS_TO_FROM_FLAG, workBasketItem.isToFrom());
        setBoolean(nutzDokSAA, FIELD_DNS_FLASH_FLAG, workBasketItem.isFlash());
        setBoolean(nutzDokSAA, FIELD_DNS_PRIORITY_FLAG, workBasketItem.isPriority());
        setBoolean(nutzDokSAA, FIELD_DNS_TC_FLAG, workBasketItem.isTc());
        setString(nutzDokSAA, FIELD_DNS_DISTRIBUTION, WSHelper.getEmptyStringForNull(workBasketItem.getVer()));
        setString(nutzDokSAA, FIELD_DNS_EVALUATION_FLAG, WSHelper.getEmptyStringForNull(workBasketItem.getAus()));

        Set<String> etsEnumTokens = new HashSet<>();
        setETSFieldFromRequest(workBasketItem, nutzDokSAA, FIELD_DNS_ETS, FIELD_DNS_ETS_UNCONFIRMED, existsInDB, etsEnumTokens);

        setString(nutzDokSAA, FIELD_DNS_LAST_USER, WSHelper.getEmptyStringForNull(workBasketItem.getLastUser()));
        if (!existsInDB && StrUtils.isEmpty(nutzDokSAA.getFieldValue(FIELD_DNS_DOCU_START_DATE))) {
            setString(nutzDokSAA, FIELD_DNS_DOCU_START_DATE, docuStartDate);
        }
        setString(nutzDokSAA, FIELD_DNS_MANUAL_START_DATE, getTimeZoneDateAsISODate(workBasketItem.getManualStartTs()));

        // spezifisch
        if (workBasketItem.getPlanNumber() != null) {
            setInteger(nutzDokSAA, FIELD_DNS_PLAN_NUMBER, workBasketItem.getPlanNumber());
        }
        setString(nutzDokSAA, FIELD_DNS_BEGIN_USAGE_DATE, getTimeZoneDateAsISODate(workBasketItem.getBeginUsageTs()));

        if (StrUtils.isEmpty(nutzDokSAA.getFieldValue(FIELD_DNS_PROCESSING_STATE))) {
            setString(nutzDokSAA, FIELD_DNS_PROCESSING_STATE, iPartsNutzDokProcessingState.NEW.getDBValue());
        }
        handleKemOrSaaAnnotations(workBasketItem, nutzDokSAA, etsEnumTokens);

    }

    private void setBoolean(EtkDataObject nutzDok, String fieldName, boolean value) {
        nutzDok.setFieldValueAsBoolean(fieldName, value, DBActionOrigin.FROM_EDIT);
    }

    private void setString(EtkDataObject nutzDok, String fieldName, String value) {
        nutzDok.setFieldValue(fieldName, value, DBActionOrigin.FROM_EDIT);
    }

    private void setInteger(EtkDataObject nutzDok, String fieldName, int value) {
        nutzDok.setFieldValueAsInteger(fieldName, value, DBActionOrigin.FROM_EDIT);
    }
}
