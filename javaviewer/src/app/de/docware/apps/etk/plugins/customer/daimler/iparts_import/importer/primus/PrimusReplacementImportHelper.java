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
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataPrimusIncludePart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataPrimusReplacePart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsPrimusIncludePartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsPrimusReplacePartId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class PrimusReplacementImportHelper extends AbstractPrimusImportHelper {

    private static class AdditionalPart {

        String additionalPartNumber;
        String quantity;
    }

    private int importedReplacementsCount;
    private int deletedReplacementsCount;
    private List<AdditionalPart> additionalParts;

    public PrimusReplacementImportHelper(EtkProject project, String logLanguage, EtkMessageLog messageLog) {
        super(project, messageLog, logLanguage, getMapping(), TABLE_DA_PRIMUS_REPLACE_PART);
        additionalParts = new DwList<>();
    }

    public int getImportedReplacementsCount() {
        return importedReplacementsCount;
    }

    public int getDeletedReplacementsCount() {
        return deletedReplacementsCount;
    }

    private static HashMap<String, String> getMapping() {
        HashMap<String, String> mappingPrimusReplacePart = new HashMap<>();
        mappingPrimusReplacePart.put(FIELD_PRP_BRAND, MSG_BRD);
        mappingPrimusReplacePart.put(FIELD_PRP_PSS_CODE_FORWARD, MSG_PSS_CODE_FORW);
        mappingPrimusReplacePart.put(FIELD_PRP_PSS_CODE_BACK, MSG_PSS_CODE_BACK);
        mappingPrimusReplacePart.put(FIELD_PRP_PSS_INFO_TYPE, MSG_PSS_INFO_TYPE);
        mappingPrimusReplacePart.put(FIELD_PRP_LIFECYCLE_STATE, MSG_STATE_OF_LIFECYCLE);
        return mappingPrimusReplacePart;
    }

    @Override
    protected boolean handleCurrentTag(String tagPath, String tagContent, int tagCount, Map<String, String> currentRecord) {
        if (tagPath.equals(MSG_ADDITIONAL_PART)) {
            AdditionalPart additionalPart = new AdditionalPart();
            additionalPart.additionalPartNumber = currentRecord.get(MSG_ADDITIONAL_PART_NUMBER);
            additionalPart.quantity = currentRecord.get(MSG_ADDITIONAL_PART_QUANTITY);
            additionalParts.add(additionalPart);
        }
        return true;
    }

    @Override
    public GenericEtkDataObjectList<EtkDataObject> handleRecord(Map<String, String> currentRecord) {
        GenericEtkDataObjectList<EtkDataObject> dataObjectsToBeSaved;
        if (getCurrentAction(currentRecord) == PrimusAction.DELETE) {
            dataObjectsToBeSaved = getDeletedDataObjectFromRecord(currentRecord);
        } else {
            dataObjectsToBeSaved = createDataObjectsFromRecord(currentRecord);
        }
        additionalParts.clear();
        return dataObjectsToBeSaved;
    }

    @Override
    public void initActionMapping(Map<String, PrimusAction> actionMapping) {
        actionMapping.put("PSS", PrimusAction.INSERT);
        actionMapping.put("PSUP", PrimusAction.UPDATE);
        actionMapping.put("PSDEL", PrimusAction.DELETE);
    }

    /**
     * Liefert alle zu löschenden Objekt bei einer PRIMUS Löschoperation ({@link PrimusAction}
     *
     * @param currentRecord
     * @return
     */
    private GenericEtkDataObjectList<EtkDataObject> getDeletedDataObjectFromRecord(Map<String, String> currentRecord) {
        if (!checkIfTagExists(currentRecord, MSG_PTN)) {
            return null;
        }
        // Vorgängerteil erzeugen
        EtkDataPart part = createPartFromPrimusPartNo(currentRecord.get(MSG_PTN), false);
        if (part == null) {
            return null;
        }
        GenericEtkDataObjectList<EtkDataObject> deletedDataObjects = new GenericEtkDataObjectList<>();
        iPartsPrimusReplacePartId primusReplacementId = new iPartsPrimusReplacePartId(part.getAsId().getMatNr());
        iPartsDataPrimusReplacePart primusReplacement = new iPartsDataPrimusReplacePart(getProject(), primusReplacementId);
        addExistingDataObjectsToDeletionList(deletedDataObjects, primusReplacement);
        if (deletedDataObjects.getDeletedList().isEmpty()) {
            messageLog.fireMessage(translateForLog("!!Zum Löschdatensatz wurde keine Ersetzung mit der Id \"%1\" gefunden.",
                                                   primusReplacementId.toStringForLogMessages()),
                                   MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            return null;
        }
        deletedReplacementsCount++;
        return deletedDataObjects;
    }

    private GenericEtkDataObjectList<EtkDataObject> createDataObjectsFromRecord(Map<String, String> currentRecord) {
        // Check, ob es das Vorgänger Element gibt
        if (!checkIfTagExists(currentRecord, MSG_PTN)) {
            return null;
        }
        String partNumber = currentRecord.get(MSG_PTN);
        EtkDataPart part = createPartFromPrimusPartNo(partNumber, true);
        // Nur herausspringen, wenn der Vorgänger nicht existiert
        if (part == null) {
            return null;
        }
        GenericEtkDataObjectList<EtkDataObject> dataObjectsToBeSaved = new GenericEtkDataObjectList<>();
        // NICHT existsInDB abfragen, da es immer true wäre, weil das DB-Objekt schon initialisiert wurde.
        if (part.isNew()) {
            // Brand ist nur beim Vorgängerteil bekannt. Falls das Teil nicht existiert, dieses Feld zusätzlich setzen.
            part.setFieldValue(FIELD_M_BRAND, currentRecord.get(MSG_BRD), DBActionOrigin.FROM_EDIT);
            dataObjectsToBeSaved.add(part, DBActionOrigin.FROM_EDIT);
        }
        handleReplacement(part, currentRecord, dataObjectsToBeSaved);
        return dataObjectsToBeSaved;
    }

    /**
     * Verarbeitet die Mitlieferteile zu einer PRIMUS Ersetzung und speichert das Ergebnis in DA_PRIMUS_INCLUDE_PART
     *
     * @param replacement
     * @param currentRecord
     * @param dataObjectsToBeSaved
     */
    private void handleAdditionalParts(iPartsDataPrimusReplacePart replacement, Map<String, String> currentRecord, GenericEtkDataObjectList<EtkDataObject> dataObjectsToBeSaved) {
        for (AdditionalPart additionalPart : additionalParts) {
            EtkDataPart includePart = createPartFromPrimusPartNo(additionalPart.additionalPartNumber, true);
            if (includePart == null) {
                continue;
            }
            if (includePart.isNew()) {
                dataObjectsToBeSaved.add(includePart, true, DBActionOrigin.FROM_EDIT);
            }
            iPartsPrimusIncludePartId includePartId = new iPartsPrimusIncludePartId(replacement.getAsId().getPartNo(),
                                                                                    includePart.getAsId().getMatNr());
            iPartsDataPrimusIncludePart replacementIncludePart = new iPartsDataPrimusIncludePart(getProject(), includePartId);

            // Mitlieferteile werden vorher auf jeden Fall gelöscht.
            replacementIncludePart.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            replacementIncludePart.setFieldValue(FIELD_PIP_QUANTITY, additionalPart.quantity, DBActionOrigin.FROM_EDIT);
            dataObjectsToBeSaved.add(replacementIncludePart, DBActionOrigin.FROM_EDIT);
        }
    }

    /**
     * Verarbeitet eine PRIMUS Ersetzung und speichert das Ergebnis in DA_PRIMUS_REPLACE_PART
     *
     * @param part
     * @param currentRecord
     * @param dataObjectsToBeSaved
     */
    private void handleReplacement(EtkDataPart part, Map<String, String> currentRecord, GenericEtkDataObjectList<EtkDataObject> dataObjectsToBeSaved) {
        importedReplacementsCount++;
        iPartsPrimusReplacePartId replacePartId = new iPartsPrimusReplacePartId(part.getAsId().getMatNr());
        iPartsDataPrimusReplacePart replacement = new iPartsDataPrimusReplacePart(getProject(), replacePartId);
        // Sollte ein Hinweis zum Vorgängerteil aus PRIMUS bereits existieren, soll zunächst der bestehende Hinweis
        // inkl. der Mitlieferteile gelöscht und anschließend neu angelegt werden.
        addExistingDataObjectsToDeletionList(dataObjectsToBeSaved, replacement);
        replacement.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        fillOverrideCompleteDataReverse(replacement, currentRecord, null);
        // Nachfolgerteil erzeugen
        String successorPartNumber = currentRecord.get(MSG_PTN_NEW);
        if (StrUtils.isValid(successorPartNumber)) {
            EtkDataPart successorPart = createPartFromPrimusPartNo(successorPartNumber, true);
            if (successorPart != null) {
                if (successorPart.isNew()) {
                    dataObjectsToBeSaved.add(successorPart, true, DBActionOrigin.FROM_EDIT);
                }
                replacement.setFieldValue(FIELD_PRP_SUCCESSOR_PARTNO, successorPart.getAsId().getMatNr(), DBActionOrigin.FROM_EDIT);
            }
        }
        dataObjectsToBeSaved.add(replacement, DBActionOrigin.FROM_EDIT);
        // Mitlieferteile verarbeiten
        handleAdditionalParts(replacement, currentRecord, dataObjectsToBeSaved);
    }

    /**
     * Markiert die übergebene PRIMUS Ersetzung samt Mitlieferteile als zu löschende Objekte
     *
     * @param dataObjectsToBeSaved
     * @param replacement
     */
    private void addExistingDataObjectsToDeletionList(GenericEtkDataObjectList<EtkDataObject> dataObjectsToBeSaved,
                                                      iPartsDataPrimusReplacePart replacement) {
        if (replacement.existsInDB()) {
            dataObjectsToBeSaved.delete(replacement, true, DBActionOrigin.FROM_EDIT);
            for (iPartsDataPrimusIncludePart includePart : replacement.getIncludeParts()) {
                dataObjectsToBeSaved.delete(includePart, true, DBActionOrigin.FROM_EDIT);
            }
        }
    }
}
