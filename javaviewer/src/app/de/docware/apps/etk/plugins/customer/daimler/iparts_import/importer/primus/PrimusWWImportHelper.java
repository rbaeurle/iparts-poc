/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.primus;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataPrimusWWPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataPrimusWWPartList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsPrimusWWPartId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.SQLStringConvert;

import java.util.*;

/**
 * Import-Hilfsklasse für Primus-Wahlweise-Teile
 */
class PrimusWWImportHelper extends AbstractPrimusImportHelper {

    private static class WWPart {

        String wwPartNumber;
    }

    private int importedWWCount;
    private int deletedWWCount;
    private List<WWPart> wwParts;

    // WWTyp "1" + "2" sollen verarbeitet, WWTyp "3" hingegen übersprungen werden.
    private static final Set<String> VALID_WW_TYPES = new HashSet<>(Arrays.asList("1", "2"));

    public PrimusWWImportHelper(EtkProject project, String logLanguage, EtkMessageLog messageLog) {
        super(project, messageLog, logLanguage, getMapping(), TABLE_DA_PRIMUS_WW_PART);
        wwParts = new DwList<>();
    }

    public int getImportedWWCount() {
        return importedWWCount;
    }

    public int getDeletedWWCount() {
        return deletedWWCount;
    }

    private static HashMap<String, String> getMapping() {
        HashMap<String, String> mappingPrimusWWPart = new HashMap<>();
        mappingPrimusWWPart.put(FIELD_PWP_ID, MSG_WW_ID);
        mappingPrimusWWPart.put(FIELD_PWP_PART_NO, MSG_PTN);
        mappingPrimusWWPart.put(FIELD_PWP_WW_PART_NO, MSG_WW_PART_NUMBER);
        mappingPrimusWWPart.put(FIELD_PWP_WW_TYPE, MSG_WW_TYPE);
        mappingPrimusWWPart.put(FIELD_PWP_TIMESTAMP, MSG_TIMESTAMP);
        return mappingPrimusWWPart;
    }

    @Override
    protected boolean handleCurrentTag(String tagPath, String tagContent, int tagCount, Map<String, String> currentRecord) {
        if (tagPath.equals(MSG_WW_PART)) {
            String isLeadingPart = handleValueOfSpecialField(MSG_WW_LEADING_PART, currentRecord.get(MSG_WW_LEADING_PART));
            if (Utils.objectEquals(isLeadingPart, SQLStringConvert.booleanToPPString(false))) {
                WWPart wwPart = new WWPart();
                wwPart.wwPartNumber = currentRecord.get(MSG_WW_PART_NUMBER);
                wwParts.add(wwPart);
            }
        }
        return true;
    }

    @Override
    public GenericEtkDataObjectList<EtkDataObject> handleRecord(Map<String, String> currentRecord) {
        GenericEtkDataObjectList<EtkDataObject> dataObjectsToBeSaved;
        PrimusAction currentAction = getCurrentAction(currentRecord);
        if (currentAction == PrimusAction.WW_DEL) {
            dataObjectsToBeSaved = getDeletedDataObjectFromRecord(currentRecord);
        } else {
            dataObjectsToBeSaved = createDataObjectsFromRecord(currentRecord, currentAction);
        }
        return dataObjectsToBeSaved;
    }

    @Override
    protected String handleValueOfSpecialField(String sourceField, String value) {
        value = super.handleValueOfSpecialField(sourceField, value);

        if (sourceField.equals(MSG_WW_LEADING_PART)) {
            value = SQLStringConvert.booleanToPPString(value.equals("J"));
        }

        return value;
    }

    @Override
    public void initActionMapping(Map<String, PrimusAction> actionMapping) {
        actionMapping.put("WWIN", PrimusAction.WW_INSERT);
        actionMapping.put("WWUP", PrimusAction.WW_UPDATE);
        actionMapping.put("WWDEL", PrimusAction.WW_DEL);
    }

    /**
     * Liefert alle zu löschenden Objekt bei einer PRIMUS WW-Löschoperation {@link PrimusAction#WW_DEL}.
     *
     * @param currentRecord
     * @return
     */
    private GenericEtkDataObjectList<EtkDataObject> getDeletedDataObjectFromRecord(Map<String, String> currentRecord) {
        GenericEtkDataObjectList<EtkDataObject> deletedDataObjects = new GenericEtkDataObjectList<>();
        String leadingPartNo = currentRecord.get(MSG_PTN);
        EtkDataPart leadingPart = createPartFromPrimusPartNo(leadingPartNo, true);
        // Nur herausspringen, wenn die führende Teilenummer nicht existiert
        if (leadingPart == null) {
            return null;
        }

        leadingPartNo = leadingPart.getAsId().getMatNr();
        String wwID = currentRecord.get(MSG_WW_ID);
        if (StrUtils.isValid(leadingPartNo, wwID)) {
            addExistingDataObjectsToDeletionList(deletedDataObjects, leadingPartNo, wwID);
            deletedWWCount += deletedDataObjects.size();
        }
        return deletedDataObjects;
    }

    private GenericEtkDataObjectList<EtkDataObject> createDataObjectsFromRecord(Map<String, String> currentRecord, PrimusAction currentAction) {
        // Check, ob es das Element für die führende Teilenummer gibt
        if (!checkIfTagExists(currentRecord, MSG_PTN)) {
            return null;
        }
        String leadingPartNo = currentRecord.get(MSG_PTN);
        EtkDataPart leadingPart = createPartFromPrimusPartNo(leadingPartNo, true);
        // Nur herausspringen, wenn die führende Teilenummer nicht existiert
        if (leadingPart == null) {
            return null;
        }
        GenericEtkDataObjectList<EtkDataObject> dataObjectsToBeSaved = new GenericEtkDataObjectList<>();
        // NICHT existsInDB abfragen, da es immer true wäre, weil das DB-Objekt schon initialisiert wurde.
        if (leadingPart.isNew()) {
            // Brand ist nur bei der führenden Teilenummer bekannt. Falls das Teil nicht existiert, dieses Feld zusätzlich setzen.
            leadingPart.setFieldValue(FIELD_M_BRAND, currentRecord.get(MSG_BRD), DBActionOrigin.FROM_EDIT);
            dataObjectsToBeSaved.add(leadingPart, DBActionOrigin.FROM_EDIT);
        }
        handleWWPart(leadingPart, currentRecord, currentAction, dataObjectsToBeSaved);
        return dataObjectsToBeSaved;
    }

    /**
     * Verarbeitet ein PRIMUS Wahlweise-Teil und speichert das Ergebnis in DA_PRIMUS_WW_PART
     *
     * @param part
     * @param currentRecord
     * @param currentAction
     * @param dataObjectsToBeSaved
     */
    private void handleWWPart(EtkDataPart part, Map<String, String> currentRecord, PrimusAction currentAction, GenericEtkDataObjectList<EtkDataObject> dataObjectsToBeSaved) {
        String wwType = currentRecord.get(MSG_WW_TYPE);
        // Hierher kommt man nur im Fall von WW_INSERT oder WW_UPDATE, die Überprüfung der PrimusAction kann weggelassen werden.
        // WWTyp "1" + "2" sollen verarbeitet, WWTyp "3" hingegen übersprungen werden.
        if ((StrUtils.isValid(wwType)) && VALID_WW_TYPES.contains(wwType)) {
            String leadingPartNo = part.getAsId().getMatNr();
            String wwID = currentRecord.get(MSG_WW_ID);

            if (currentAction == PrimusAction.WW_UPDATE) {
                // Sollte ein Hinweis zum führenden Teil mit passender WW-ID aus PRIMUS bereits existieren, sollen zunächst alle
                // bestehenden Hinweise gelöscht und anschließend neu angelegt werden.
                addExistingDataObjectsToDeletionList(dataObjectsToBeSaved, leadingPartNo, wwID);
            }

            // Wahlweise-Teile erzeugen
            if (checkIfTagExists(currentRecord, MSG_WW_PARTS) && Utils.objectEquals(currentRecord.get(MSG_WW_STATUS), "30")) { // WW-Status „30" = aktiv
                String timestamp = handleValueOfSpecialField(MSG_TIMESTAMP, currentRecord.get(MSG_TIMESTAMP));
                for (WWPart wwPart : wwParts) {
                    EtkDataPart wwDataPart = createPartFromPrimusPartNo(wwPart.wwPartNumber, true);
                    if (wwDataPart != null) {
                        if (wwDataPart.isNew()) {
                            dataObjectsToBeSaved.add(wwDataPart, true, DBActionOrigin.FROM_EDIT);
                        }
                        iPartsPrimusWWPartId leadingPartId = new iPartsPrimusWWPartId(leadingPartNo, wwID, wwDataPart.getAsId().getMatNr());
                        if (leadingPartId.isValidId()) {
                            importedWWCount++;
                            iPartsDataPrimusWWPart wwPrimusDataPart = new iPartsDataPrimusWWPart(getProject(), leadingPartId);
                            wwPrimusDataPart.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                            wwPrimusDataPart.setFieldValue(FIELD_PWP_WW_TYPE, wwType, DBActionOrigin.FROM_EDIT);
                            wwPrimusDataPart.setFieldValue(FIELD_PWP_TIMESTAMP, timestamp, DBActionOrigin.FROM_EDIT);
                            wwPrimusDataPart.setFieldValue(FIELD_PWP_WW_PART_NO, wwDataPart.getAsId().getMatNr(), DBActionOrigin.FROM_EDIT);
                            dataObjectsToBeSaved.add(wwPrimusDataPart, DBActionOrigin.FROM_EDIT);
                        }
                    }
                }
            }
            wwParts.clear();
        } else {
            messageLog.fireMessage(translateForLog("!!Unzulässiger WWType \"%1\" wird ignoriert.", StrUtils.isValid(wwType) ? wwType : ""),
                                   MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
        }
    }

    /**
     * Markiert alle PRIMUS Wahlweise-Teile für die übergebene führende Teilenummer und WW-ID als zu löschende Objekte.
     *
     * @param dataObjectsToBeSaved
     * @param leadingPartNo
     * @param wwID
     */
    private void addExistingDataObjectsToDeletionList(GenericEtkDataObjectList<EtkDataObject> dataObjectsToBeSaved,
                                                      String leadingPartNo, String wwID) {
        iPartsDataPrimusWWPartList dataPrimusWWPartList = iPartsDataPrimusWWPartList.loadPrimusWWPartsForLeadingPartAndWWID(leadingPartNo,
                                                                                                                            wwID, getProject());
        for (iPartsDataPrimusWWPart dataPrimusWWPart : dataPrimusWWPartList) {
            dataObjectsToBeSaved.delete(dataPrimusWWPart, true, DBActionOrigin.FROM_EDIT);
        }
    }
}