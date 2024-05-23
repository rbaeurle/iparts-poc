/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.dialog.AbstractCatalogDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.*;

/**
 * CSV-Importer für Gleichteile
 */
public class iPartsEqualPartsImporter extends AbstractCatalogDataImporter implements iPartsConst {

    private static final String TABLE_NAME = TABLE_MAT;
    private static final int MAX_OBJECTS_FOR_IMPORT = 5000;
    private static final char CSV_IMPORT_SEPARATOR = ';';

    // Feldnamen in CSV-Header
    private static final String PARTNO = "Teilenummer";
    private static final String MAPPED_PARTNO = "Gemappte Teilenummer";
    private static final String ORGANISATION = "Organisation";

    private final String[] headerNames = new String[]{
            PARTNO,
            MAPPED_PARTNO,
            ORGANISATION };

    private enum OrganisationType {
        MBAG,
        DTAG,
        UNKNOWN;

        private static OrganisationType getOrganisation(String importValue) {
            for (OrganisationType orga : OrganisationType.values()) {
                if (orga.name().equals(importValue)) {
                    return orga;
                }
            }
            return UNKNOWN;
        }
    }

    private final boolean importToDB = true;
    private final boolean doBufferSave = true;
    private final iPartsNumberHelper numberHelper;
    private Map<String, PartNoMapping> importMap;
    private Set<String> invalidPartNumbers;

    public iPartsEqualPartsImporter(EtkProject project) {
        super(project, "!!Import Gleichteile", true,
              new FilesImporterFileListType(TABLE_NAME, "!!Import Gleichteile",
                                            false, false, false,
                                            new String[]{ MimeTypes.EXTENSION_CSV, MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_ALL_FILES }));
        numberHelper = new iPartsNumberHelper();
    }


    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        // Hier bemerkte Fehler führen zum Abbruch, das ist aber nicht gewünscht.
        importer.setMustExists(headerNames);
        importer.setMustHaveData(headerNames);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        boolean isValid = true;
        if (!importer.isXMLMixedTable() && !importer.getTableNames().isEmpty()) {
            if (!importer.getTableNames().get(0).equals(TABLE_NAME)) {
                getMessageLog().fireMessage(translateForLog("!!Falscher Importtabellenname %2 statt %3",
                                                            importer.getTableNames().get(0),
                                                            TABLE_NAME),
                                            MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
                isValid = false;
            }
        }
        return isValid;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        return importer.isRecordValid(importRec, errors);
    }

    @Override
    protected void preImportTask() {
        importMap = new HashMap<>();
        invalidPartNumbers = new HashSet<>();
        progressMessageType = ProgressMessageType.READING;
        setBufferedSave(doBufferSave);
        super.preImportTask();
    }

    /**
     * Liefert die Teilenummer zum übergebenen Feldnamen aus dem übergebenen <code>importRec</code>
     *
     * @param importRec
     * @param fieldName
     * @param recordNo
     * @param textPartNoType
     * @return
     */
    private String getPartNoFromRecord(Map<String, String> importRec, String fieldName, int recordNo, String textPartNoType) {
        String partNo = importRec.get(fieldName);
        if (StrUtils.isEmpty(partNo)) {
            fireWarning(recordNo, "!!Ungültige %1", fieldName);
            return null;
        }
        // Zur Sicherheit. Eigentlich sollten die Nummern im Eingabeformat kommen
        String formattedPartNo = numberHelper.checkNumberInputFormat(partNo, getMessageLog());
        if (!formattedPartNo.equals(partNo)) {
            // Ist die Teilenummer nach dem formatieren leer bzw. ungültig -> Meldung ausgeben
            if (StrUtils.isEmpty(formattedPartNo)) {
                fireWarning(recordNo, "!!Ungültige %1 \"%2\"", fieldName, partNo);
                return null;
            } else {
                fireMessage("!!Record %1: %2.", recordNo,
                            "!!Die %1 Teilenummer \"%2\" liegt nicht im Eingabeformat vor und wurde " +
                            "formatiert zu \"%3\"", translateForLog(textPartNoType), partNo, formattedPartNo);
                return formattedPartNo;
            }
        }
        return partNo;
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        // Organisation bestimmen
        String orgaValue = importRec.get(ORGANISATION);
        OrganisationType orgaType = OrganisationType.getOrganisation(orgaValue);
        // Check, ob der Wert für die Organisationseinheit in der Importdatei gültig und bekannt ist
        if (orgaType == OrganisationType.UNKNOWN) {
            fireWarning(recordNo, "!!Ungültige %1: %2", ORGANISATION, orgaValue);
            reduceRecordCount();
            return;
        }
        // Aktuelle und gemappte Teilenummer bestimmen
        String partNo = getPartNoFromRecord(importRec, PARTNO, recordNo, "!!aktuelle");
        if (invalidPartNumbers.contains(partNo)) {
            reduceRecordCount();
            return;
        }
        String mappedPartNo = getPartNoFromRecord(importRec, MAPPED_PARTNO, recordNo, "!!gemappte");
        if (!StrUtils.isValid(partNo, mappedPartNo)) {
            // Mind. eine Teilenummer ist nicht gültig
            return;
        }
        // Check, ob es zur aktuellen Teilenummer schon ein Mapping gibt
        PartNoMapping partNoMapping = importMap.get(partNo);
        if (partNoMapping != null) {
            // Bestehenden Datensatz befüllen sofern das Mapping gültig ist
            addMappingToExistingPartNoMapping(orgaType, partNoMapping, recordNo, mappedPartNo);
        } else {
            // Neues Mapping erzeugen
            partNoMapping = createNewPartNoMapping(partNo, mappedPartNo, recordNo, orgaType);
            if (partNoMapping == null) {
                return;
            }
            importMap.put(partNo, partNoMapping);
        }
        // Zur Sicherheit in regelmäßigen Abständen die Daten in die DB speichern, damit der Speicher nicht zu voll wird
        if (importMap.size() >= MAX_OBJECTS_FOR_IMPORT) {
            saveData();
        }
    }

    /**
     * Erzeugt ein neues Mapping-Objekt für die übergebenen Teilenummern
     *
     * @param partNo
     * @param mappedPartNo
     * @param recordNo
     * @param orgaType
     * @return
     */
    private PartNoMapping createNewPartNoMapping(String partNo, String mappedPartNo, int recordNo, OrganisationType orgaType) {
        PartNoMapping partNoMapping = new PartNoMapping(partNo);
        // Check, ob das aktuelle Teil in der DB existiert
        if (!partNoMapping.existInDB()) {
            fireWarning(recordNo, "!!%1 \"%2\" nicht in DB vorhanden", PARTNO, partNoMapping.getPartNo());
            reduceRecordCount();
            invalidPartNumbers.add(partNoMapping.getPartNo());
            return null;
        }
        // Abhängig von der Organisation die gemappte Teilenummer setzen
        if (orgaType == OrganisationType.MBAG) {
            partNoMapping.setCarPartNo(mappedPartNo);
        } else if (orgaType == OrganisationType.DTAG) {
            partNoMapping.setTruckPartNo(mappedPartNo);
        } else {
            fireWarning(recordNo, "!!Ungültige Organisationseinheit für das Erzeugen des Mappings zur" +
                                  " Teilenummer \"%1\"", partNoMapping.getPartNo());
            reduceRecordCount();
            return null;
        }
        return partNoMapping;
    }

    /**
     * Fügt dem übergebenen Mapping ein weiteres Mapping hinzu, sofern das gültig ist
     *
     * @param orgaType
     * @param partNoMapping
     * @param recordNo
     * @param mappedPartNo
     * @return
     */
    private void addMappingToExistingPartNoMapping(OrganisationType orgaType, PartNoMapping partNoMapping, int recordNo, String mappedPartNo) {
        if (orgaType == OrganisationType.MBAG) {
            if (partNoMapping.isCarPartNoValid()) {
                // Für die aktuelle Teilenummer gab es in der Importdatei schon ein Mapping mit einer PKW Teilenummer
                fireWarning(recordNo, "!!%1 \"%2\" wurde bereits bearbeitet", PARTNO, partNoMapping.getPartNo());
                reduceRecordCount();
                return;
            }
            partNoMapping.setCarPartNo(mappedPartNo);
        } else if (orgaType == OrganisationType.DTAG) {
            if (partNoMapping.isTruckPartNoValid()) {
                // Für die aktuelle Teilenummer gab es in der Importdatei schon ein Mapping mit einer TRUCK Teilenummer
                fireWarning(recordNo, "!!%1 \"%2\" wurde bereits bearbeitet", PARTNO, partNoMapping.getPartNo());
                reduceRecordCount();
                return;
            }
            partNoMapping.setTruckPartNo(mappedPartNo);
        } else {
            fireWarning(recordNo, "!!Ungültige Organisationseinheit für das Mapping zur Teilenummer \"%1\"", partNoMapping.getPartNo());
            reduceRecordCount();
        }
    }

    @Override
    protected void postImportTask() {
        if (!isCancelled() && importToDB) {
            saveData();
        }
        invalidPartNumbers.clear();
//        cancelImport("!!zum Test");
        super.postImportTask();
    }

    /**
     * Speichert die aufgesammelten Datensätze
     */
    private void saveData() {
        int totalRecordCount = importMap.size();
        if (totalRecordCount > 0) {
            fireMessage("!!Aktualisiere Einträge (%1)", String.valueOf(totalRecordCount));
            int currentRecordCounter = 0;
            for (PartNoMapping partNoMapping : importMap.values()) {
                if (isCancelled()) {
                    return;
                }
                partNoMapping.storeData();
                // Den Fortschrittsbalken füttern.
                currentRecordCounter++;
                updateProgress(currentRecordCounter, totalRecordCount);
            }
            getMessageLog().hideProgress();
            importMap.clear();
        }
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(TABLE_NAME)) {
            return importMasterData(prepareImporterKeyValue(importFile, TABLE_NAME, CSV_IMPORT_SEPARATOR, withHeader, headerNames));
        }
        return false;
    }

    private void fireMessage(String translationsKey, String... placeHolderTexts) {
        getMessageLog().fireMessage(translateForLog(translationsKey, placeHolderTexts), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
    }

    private void fireMessage(String translationsKey, int recordNo, String secondTranslationsKey, String... placeHolderTexts) {
        String extraMsg = translateForLog(secondTranslationsKey, placeHolderTexts);
        String msg = translateForLog(translationsKey, String.valueOf(recordNo), extraMsg);
        getMessageLog().fireMessage(msg, MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
    }

    private void fireWarning(int recordNo, String secondTranslationsKey, String... placeHolderTexts) {
        fireWarning("!!Record %1: %2. Wird übersprungen.", recordNo, secondTranslationsKey, placeHolderTexts);
    }

    private void fireWarning(String translationsKey, int recordNo, String secondTranslationsKey, String... placeHolderTexts) {
        String extraMsg = translateForLog(secondTranslationsKey, placeHolderTexts);
        String msg = translateForLog(translationsKey, String.valueOf(recordNo), extraMsg);
        fireWarning(msg);
    }

    private void fireWarning(String translationsKey, String... placeHolderTexts) {
        getMessageLog().fireMessage(translateForLog(translationsKey, placeHolderTexts), MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
    }

    /**
     * Hilfsklasse zum Darstellen der Mappings von PKW udn Truck Teilenummern auf eine aktuelle Teilenummer
     */
    private class PartNoMapping {

        private final EtkDataPart dataPart;
        private String carPartNo;
        private String truckPartNo;

        public PartNoMapping(String mainPartNo) {
            this.dataPart = EtkDataObjectFactory.createDataPart(getProject(), new iPartsPartId(mainPartNo, ""));
        }

        public String getTruckPartNo() {
            return this.truckPartNo;
        }

        public void setTruckPartNo(String truckPartNo) {
            this.truckPartNo = truckPartNo;
        }

        public boolean isTruckPartNoValid() {
            return StrUtils.isValid(getTruckPartNo());
        }

        public String getCarPartNo() {
            return this.carPartNo;
        }

        public void setCarPartNo(String carPartNo) {
            this.carPartNo = carPartNo;
        }

        public boolean isCarPartNoValid() {
            return StrUtils.isValid(getCarPartNo());
        }

        public boolean existInDB() {
            return dataPart.existsInDB();
        }

        public String getPartNo() {
            return dataPart.getAsId().getMatNr();
        }

        /**
         * Speichert den Datensatz in der DB
         */
        public void storeData() {
            if (dataPart != null) {
                if (isCarPartNoValid()) {
                    dataPart.setFieldValue(FIELD_M_MATNR_MBAG, getCarPartNo(), DBActionOrigin.FROM_EDIT);
                }
                if (isTruckPartNoValid()) {
                    dataPart.setFieldValue(FIELD_M_MATNR_DTAG, getTruckPartNo(), DBActionOrigin.FROM_EDIT);
                }
                saveToDB(dataPart);
            } else {
                fireWarning("!!Gleichteile-Datensatz für %1 \"%2\" kann nicht gespeichert werden.",
                            PARTNO, getPartNo());
            }
        }
    }
}
