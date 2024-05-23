/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.masterdata;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.iPartsMainImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.AbstractMBSDataHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.MBSDataImporter;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handler für das Verarbeiten von SAP-MBS Teile-Stammdaten
 */
public class MBSPartDataHandler extends AbstractMBSDataHandler {

    private static final String TRIGGER_ELEMENT = "PartMasterData";

    // Felder, die für das DB Objekt verwendet werden
    private static final String PARTS_TYPE = "PartsType";
    private static final String DRAWING_GEOMETRY_VERSION = "DrawingGeometryVersion";
    private static final String DRAWING_DATE_OR_TYPE = "DrawingDateOrType";
    private static final String REFERENCE_DRAWING = "ReferenceDrawing";
    private static final String COLOR_ITEM_TYPE = "ColorItemType";
    private static final String SAFETY_RELEVANT = "SafetyRelevant";
    private static final String CERTIFICATION_RELEVANT = "CertificationRelevant";
    private static final String REMARK_ONE = "Remark1";
    private static final String REMARK_TWO = "Remark2";
    private static final String SPARE_PART_IDENTIFIER = "SparePartIdentifier";

    private Map<PartId, Map<String, String>> partNoToAttributesMap; // Alle Datensätze zu einer Teilenummer
    private Map<PartId, EtkDataPart> dbObjects;
    private String previousPartNumber = "";

    public MBSPartDataHandler(EtkProject project, MBSDataImporter importer) {
        super(project, TRIGGER_ELEMENT, importer, "!!SAP-MBS Teile-Stammdaten", TABLE_MAT);
        this.partNoToAttributesMap = new HashMap<>();
        this.dbObjects = new HashMap<>();
    }

    @Override
    protected void initMapping(Map<String, String> mapping) {
        mapping.put(FIELD_M_MATNR, PART_NUMBER);
        mapping.put(FIELD_M_ASSEMBLYSIGN, PARTS_TYPE);
        mapping.put(FIELD_M_CONST_DESC, DESCRIPTION);
        mapping.put(FIELD_M_IMAGESTATE, DRAWING_GEOMETRY_VERSION);
        mapping.put(FIELD_M_IMAGEDATE, DRAWING_DATE_OR_TYPE);
        mapping.put(FIELD_M_REFSER, REFERENCE_DRAWING);
        mapping.put(FIELD_M_QUANTUNIT, QUANTITY_UNIT);
        mapping.put(FIELD_M_VARIANT_SIGN, COLOR_ITEM_TYPE);
        mapping.put(FIELD_M_SECURITYSIGN, SAFETY_RELEVANT);
        mapping.put(FIELD_M_CERTREL, CERTIFICATION_RELEVANT);
        mapping.put(FIELD_M_NOTEONE, REMARK_ONE);
        mapping.put(FIELD_M_NOTETWO, REMARK_TWO);
        mapping.put(FIELD_M_ETKZ_MBS, SPARE_PART_IDENTIFIER);
    }

    /**
     * Verarbeitet einen kompletten XML Teilestamm Block
     */
    @Override
    protected void handleCurrentRecord() {
        String partNumber = getCurrentRecord().get(PART_NUMBER);
        // Ab einer bestimmter Grenze sollen die gesammelten Daten gespeichert werden
        if ((partNoToAttributesMap.size() >= MAX_DB_OBJECTS_CACHE_SIZE) && !previousPartNumber.equals(partNumber)) {
            storeData();
        }
        previousPartNumber = partNumber;

        // Check, ob es sich um ein INSERT oder ein UPDATE handelt. Bei einem DELETE sollen wir nichts tun.
        if (!isValidAction(partNumber)) {
            return;
        }

        // Die Teilenummer, um die es gerade geht
        if (!StrUtils.isValid(partNumber)) {
            return;
        }

        PartId partId = new PartId(partNumber, "");
        // Check, ob das Teil schon aus der DB geladen wurde. Falles nicht nicht existiert, wird ein leeres, neue DB Objekt angelegt
        EtkDataPart partFromDB = getPartFromDB(partId);
        // Existiert der Datensatz in der DB muss geprüft werden, ob ReleaseDateTo = unendlich. Falls nicht,
        // soll der Teilestamm nicht verarbeitet werden.
        if (!isImportDateTimeIsValid(partFromDB)) {
            return;
        }

        // M_BESTNR setzen
        partFromDB.setFieldValue(FIELD_M_BESTNR, partNumber, DBActionOrigin.FROM_EDIT);

        String importDrawingGeometryVersion = getCurrentRecord().get(DRAWING_GEOMETRY_VERSION);
        if (StrUtils.isValid(importDrawingGeometryVersion)) {
            String value = StrUtils.leftFill(importDrawingGeometryVersion, 4, '0');
            setFieldValue(DRAWING_GEOMETRY_VERSION, value);
        }

        String importDate = getCurrentRecord().get(DRAWING_DATE_OR_TYPE);
        if (StrUtils.isValid(importDate)) {
            String convertedDate = iPartsMainImportHelper.convertImageDateWithoutFirstTwoYearDigits(importDate, "50");
            setFieldValue(DRAWING_DATE_OR_TYPE, convertedDate);
        }

        // Nachdem der Importdatensatz vom RELEASE_DATE_TO Datum zum DB Datensatz passt, muss geprüft werden, ob
        // bisherige Datensätze neuer/älter sind und dementsprechend die Werte überschreiben.
        fillMainPartDataRecord(partId);
    }

    /**
     * Befüllt das Teilenummer-Hauptobjekt mit den Daten des aktuellen Importdatensatzes (pro Teilenummer existiert nur
     * ein Hauptobjekt)
     *
     * @param partId
     */
    private void fillMainPartDataRecord(PartId partId) {
        Map<String, String> existingImportObject = partNoToAttributesMap.get(partId);
        if (existingImportObject == null) {
            partNoToAttributesMap.put(partId, getCurrentRecord());
        } else {
            String existingDateFrom = getImportHelper().getMBSDateTimeValue(existingImportObject.get(RELEASE_DATE_FROM));
            String newDateFrom = getReleaseDateFrom();
            if (StrUtils.isEmpty(existingDateFrom) || (StrUtils.isValid(newDateFrom) && (existingDateFrom.compareTo(newDateFrom) >= 0))) {
                // aktuelle Datensatz ist neuer als der bisherige -> alle Daten vom neuen übernehmen
                getCurrentRecord().forEach(existingImportObject::put);
            } else {
                // aktuelle Datensatz ist älter als der bisherige -> nur die Daten übernehmen, die noch nicht existieren
                getCurrentRecord().forEach(existingImportObject::putIfAbsent);
            }
        }
    }

    /**
     * Überprüft, ob das Freigabedatum des Importdatensatzes bei einem bestehenden Datensatz gleich unendlich ist
     *
     * @param partFromDB
     * @return
     */
    private boolean isImportDateTimeIsValid(EtkDataPart partFromDB) {
        if (!partFromDB.isNew()) {
            String contentDateTime = getReleaseDateTo();
            if (StrUtils.isValid(contentDateTime)) {
                writeMessage(TranslationHandler.translate("!!Freigabedatum \"%1\" ist nicht unendlich bei existierenden" +
                                                          " Teilestamm für Teilenummer \"%2\"",
                                                          contentDateTime, partFromDB.getAsId().getMatNr()),
                             MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                return false;
            }
        }
        return true;
    }

    /**
     * Lädt den Teilestamm aus der DB sofern er noch nicht geladen wurde
     *
     * @param partId
     * @return
     */
    private EtkDataPart getPartFromDB(PartId partId) {
        return dbObjects.computeIfAbsent(partId, partIdFormDBObject -> {
            EtkDataPart result = EtkDataObjectFactory.createDataPart(getProject(), partIdFormDBObject);
            if (!result.existsInDB()) {
                result.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
            }
            return result;
        });
    }

    /**
     * Speichert die aufgesammelten Teilestämme in der DB
     */
    private void storeData() {
        dbObjects.forEach((partId, part) -> {
            List<String> sources = part.getFieldValueAsSetOfEnum(FIELD_M_SOURCE);
            // nicht neu und nicht ausschließlich Quelle MBS
            boolean foundConflictingSource = !part.isNew()
                                             && ((sources.size() != 1) || !sources.contains(iPartsImportDataOrigin.SAP_MBS.getOrigin()));
            Map<String, String> record = partNoToAttributesMap.get(partId);
            if (record != null) {
                String releaseDateFromValidRecord = getImportHelper().getMBSDateTimeValue(record.get(RELEASE_DATE_FROM));
                if (foundConflictingSource) {
                    fillAttributesForConflictingSources(part, record);
                } else {
                    getImportHelper().fillOverrideCompleteDataForSAPMBS(part, record);
                    part.setFieldValue(FIELD_M_LAST_MODIFIED, releaseDateFromValidRecord, DBActionOrigin.FROM_EDIT);
                }
                if (StrUtils.isEmpty(releaseDateFromValidRecord)) {
                    // Kann eigentlich nicht passieren, da laut Aussage Daimler die Datumsangaben immer gesetzt sind
                    writeMessage(TranslationHandler.translate("!!Datensatz mit der Teilenummer \"%1\" übersprungen, " +
                                                              "weil das Änderungsdatum ab leer ist!",
                                                              partId.getMatNr()),
                                 MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                } else {
                    savePart(part);
                }
            }
        });
        handleBufferedData();
    }

    /**
     * Speichert die im Importer gebufferten Objekte und leert die temporären Caches hier im Handler
     */
    private void handleBufferedData() {
        // Alle Objekte aus dbObjects wurden nun an den Importer übergeben. Jetzt kann die Map geleert werden.
        getImporter().saveBufferedList();
        dbObjects.clear();
        partNoToAttributesMap.clear();
    }

    private void fillAttributesForConflictingSources(EtkDataPart part, Map<String, String> record) {
        String sparePartIdentifier = record.get(SPARE_PART_IDENTIFIER);
        if (sparePartIdentifier == null) {
            sparePartIdentifier = "";
        }
        part.setFieldValue(FIELD_M_ETKZ_MBS, sparePartIdentifier, DBActionOrigin.FROM_EDIT);

        // Deutsche Konstruktions-Benennung setzen falls diese leer ist
        if (part.getFieldValueAsMultiLanguage(FIELD_M_CONST_DESC).getText(Language.DE.getCode()).isEmpty()) {
            getImportHelper().fillOverrideOneLanguageText(part, Language.DE, FIELD_M_CONST_DESC, record.get(DESCRIPTION));
        }
    }

    /**
     * Setzt beim übergebenen {@link EtkDataPart} die Quelle und speichert den Datensatz, sofern er Änderungen enthält.
     *
     * @param dataPart
     */
    private void savePart(EtkDataPart dataPart) {
        dataPart.addSetOfEnumValueToFieldValue(FIELD_M_SOURCE, iPartsImportDataOrigin.SAP_MBS.getOrigin(), DBActionOrigin.FROM_EDIT);
        if (dataPart.isModifiedWithChildren()) {
            saveDataObject(dataPart);
        }
    }

    @Override
    public void onEndDocument() {
        super.onEndDocument();
        storeData();
        dbObjects.clear();
        partNoToAttributesMap.clear();
    }
}
