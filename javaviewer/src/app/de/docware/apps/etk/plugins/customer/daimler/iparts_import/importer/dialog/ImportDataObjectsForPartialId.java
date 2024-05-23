/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDIALOGChange;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDIALOGChangeList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Hilfsklasse für Importer für Datensätze, die verschieden Stände haben. Die werden durch ein Änderungsdatum und/oder
 * eine Sequenznummer im Primärschlüssel voneinander abgegrenzt. Diese Datenstruktur bietet Methoden um das Anlegen
 * von mehreren aufeinanderfolgenden Ständen mit gleichen Nutzdaten zu verhindern, was redundant wäre.
 */
abstract class ImportDataObjectsForPartialId<DBOType extends EtkDataObject> {

    private List<DBOType> dataObjectsInDB;          // alle Datensätze zur ID, die gegebenenfalls schon in der DB existieren
    private List<DBOType> importDataObjects;        // alle Datensätze zur ID, die gegebenenfalls importiert werden sollen
    private DBOType importDeletionDataObject;       // der Löschdatensatz zur ID, der gegebenenfalls importiert werden soll
    private String referencedSeriesNumber;
    private Set<String> referencedPartNumbers;      // Referenzierte Teilenummern für DA_DIALOG_CHANGES Einträge bei AS Werksdaten von Variantentabellen (VX9 und VX10)
    private String referencedBCTEKey;               // Referenzierter BCTE Schlüssel für den DA_DIALOG_CHANGES Eintrag bei AS Werksdaten von Stücklistenpositionen (VBW)

    ImportDataObjectsForPartialId() {
        dataObjectsInDB = new DwList<>();
        importDataObjects = new DwList<>();
    }

    /**
     * Falls für diese Id schon ein Löschdatensatz besteht, diesen löschen, da der Datensatz wieder gekommen ist
     * Falls das Datenobjekt hinzugefügt wurde gibt die Methode true zurück
     * Falls das Datenobjekt nicht hinzugefügt wurde, gibt das Datenobjekt false zurück
     *
     * @param dataObject
     * @param importer
     * @param mapping
     * @return
     */
    public boolean addImportedData(DBOType dataObject, AbstractDIALOGDataImporter importer, HashMap<String, String> mapping) {
        if (importDeletionDataObject != null) {
            // Vorher kam schon ein Löschsatz für diesen Datensatz --> Den Löschsatz rausnehmen damit er später nicht gespeichert wird
            // Muss zuerst passieren, damit danach beim Duplikatscheck dieser nicht mit rangezogen wird
            importDeletionDataObject = null;
            importer.reduceRecordCount();
        }
        String adat = null;
        if (getAdatField() != null) {
            adat = dataObject.getFieldValue(getAdatField());
        }
        DBOType newestDataObject = getNewestDataObject(adat);
        if ((newestDataObject != null) && (getSource(newestDataObject) == iPartsImportDataOrigin.MAD)) {
            // Wenn der neueste Datensatz in der Datenbank die Quelle MAD hat, dann wird dieser durch den
            // zu importierenden DIALOG Datensatz überschrieben. Der neue Datensatz hat eine andere ID,
            // da sein ADAT vorher im importRecord auf das aktuelle Datum gesetzt wurde.
            // Quelle MAD kann nur bei Objekten aus der Datenbank stehen, da der Importer Quelle DIALOG setzt.
            newestDataObject.deleteFromDB(true);
            dataObjectsInDB.remove(newestDataObject);
            importDataObjects.add(dataObject);
            return true;
        } else if (!isDuplicateOfNewestDataObject(dataObject, mapping)) {
            importDataObjects.add(dataObject);
            return true;
        } else {
            importer.reduceRecordCount();
            return false;
        }
    }

    public void addImportedDeletionData(DBOType deletionDataObject, AbstractDIALOGDataImporter importer, HashMap<String, String> mapping) {
        if (dataObjectsInDB.isEmpty() && importDataObjects.isEmpty()) {
            // Zu löschender Datensatz ist nicht in der Tabelle und nicht vorher importiert worden also verwirf diesen Löschdatensatz
            importer.reduceRecordCount();
            return;
        }
        if (isDuplicateOfNewestDataObject(deletionDataObject, mapping)) {
            importer.reduceRecordCount();
            return;
        }
        if (deletionDataObject != null) {
            // Datensatzschlüssel merken um den Löschdatensatz später zu speichern, falls nicht der gleiche VBW-Satz ohne Löschkennzeichen kommt
            importDeletionDataObject = deletionDataObject;
        }
    }

    public boolean hasData() {
        return !dataObjectsInDB.isEmpty() || !importDataObjects.isEmpty() || (importDeletionDataObject != null);
    }

    public DBOType getNewestDataObject(String adat) {
        if (hasData()) {
            List<DBOType> allData = new DwList<>(dataObjectsInDB);
            allData.addAll(importDataObjects);
            if (importDeletionDataObject != null) {
                allData.add(importDeletionDataObject);
            }
            Collections.sort(allData, getComparator());

            String adatFieldName = getAdatField();
            if ((adatFieldName == null) || (adat == null)) {
                // Neuestes DataObject unabhängig vom ADAT
                return allData.get(0);
            } else {
                // Neuestes DataObject passend zum ADAT suchen
                for (DBOType dataObject : allData) {
                    if (dataObject.getFieldValue(adatFieldName).equals(adat)) {
                        return dataObject;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Prüft, ob alle (für den Import relevanten) Felder des Datensatzes gleich denen des Datensatzes mit höchstem ADAT
     * sind und ob beide Datensätze ein Löschsatz bzw. normaler Datensatz sind.
     * Wichtig, da ein bestehender Datensatz (normaler oder Löschsatz) der aus DIALOG mit gleichem Dateninhalt zum höchsten ADAT kommt
     * nicht verarbeitet werden darf.
     * DAIMLER-8544: Beispiel: ein normaler Datensatz steht als "freigegeben" in der Datenbank.
     * Der zu importierende hat den gleichen Dateninhalt aber einen anderen Status, und zwar "neu".
     * Damit darf er trotzdem nicht importiert werden. Nur wenn er ein Löschsatz wäre.
     * Das ist wichtig, da nach der DIALOG-Urladung manche Datensätze nochmal per Delta-Versorgung kommen.
     * Diese sollen NICHT mit neuem Status nochmal importiert werden, welchen sie haben würden, wenn der Datensatz zwischen
     * Urladung und Deltaladung im AS verwendet wurde (auch in aktiven Changesets).
     */
    private boolean isDuplicateOfNewestDataObject(DBOType dataObject, HashMap<String, String> mapping) {
        if (hasData()) {
            String adat = null;
            if (getAdatField() != null) {
                adat = dataObject.getFieldValue(getAdatField());
            }
            DBOType newestDataObject = getNewestDataObject(adat);
            if (newestDataObject != null) {
                for (String mappedDBField : mapping.keySet()) {
                    if (!newestDataObject.getFieldValue(mappedDBField).equals(dataObject.getFieldValue(mappedDBField))) {
                        return false;
                    }
                }
                return getStatus(dataObject).isDeletedReleaseState() == getStatus(newestDataObject).isDeletedReleaseState();
            }
        }
        return false;
    }

    /**
     * Speichert alle Datensätze und setzt, falls der Status des neuesten Datensatzes {@link iPartsDataReleaseState#RELEASED}
     * ist, alle älteren Stände auf {@link iPartsDataReleaseState#NOT_RELEVANT}, falls deren Status das zulässt, da nur ein
     * Datensatz freigegeben sein kann. Dieser neueste Datensatz muss nicht zwingend ein gerade importierter sein, sondern kann
     * auch schon in der Datenbank existiert haben, wenn ältere Daten nach den neueren importiert werden (siehe DAIMLER-9614).
     * Im Fall von {@link iPartsDataReleaseState#NEW} werden entsprechende Einträge in DA_DIALOG_CHANGES angelegt,
     * da diese Datensätze von einem Autor überprüft werden müssen.
     */
    public void saveImportDataObjects(AbstractDIALOGDataImporter importer, DIALOGImportHelper helper) {
        // alle Datensätze die freigegeben werden sollen aufsammeln, um dann später nur den aktuellsten auch wirklich freizugeben
        List<DBOType> releasedDataObjects = new DwList<>();
        for (DBOType importDataObject : importDataObjects) {
            if (getStatus(importDataObject) == iPartsDataReleaseState.RELEASED) {
                releasedDataObjects.add(importDataObject);
            }
        }
        for (DBOType importDataObject : dataObjectsInDB) {
            if (getStatus(importDataObject) == iPartsDataReleaseState.RELEASED) {
                releasedDataObjects.add(importDataObject);
            }
        }

        EtkDataObjectList<DBOType> dataObjectListToBeSaved = new GenericEtkDataObjectList<>();
        if (!releasedDataObjects.isEmpty()) {
            Collections.sort(releasedDataObjects, getComparator());
            // es darf nur der neuste Datensatz wirklich freigegeben werden, alle anderen bekommen den Status "nicht relevant"
            for (int i = 1; i < releasedDataObjects.size(); i++) {
                DIALOGImportHelper.setNotRelevantStateIfAllowed(releasedDataObjects.get(i), getStatusField());
                dataObjectListToBeSaved.add(releasedDataObjects.get(i), DBActionOrigin.FROM_EDIT);
            }
        }

        for (DBOType importDataObject : importDataObjects) {
            dataObjectListToBeSaved.add(importDataObject, true, DBActionOrigin.FROM_EDIT);
            // Falls der Datensatz "neu" ist, noch zusätzlich DialogChanges Einträge anlegen
            if (getStatus(importDataObject) == iPartsDataReleaseState.NEW) {
                saveDialogChanges(importer, helper, importDataObject);
            }
        }

        // Jetzt werden alle zu importierenden Datensätze gespeichert und bereits in der Datenbank existierende angepasst.
        for (DBOType dataObject : dataObjectListToBeSaved) {
            importer.saveToDB(dataObject);
        }
    }

    /**
     * Speichert den Löschdatensatz und setzt, falls der Status {@link iPartsDataReleaseState#DELETED} ist, alle
     * älteren Stände (älteres ADAT) auf {@link iPartsDataReleaseState#NOT_RELEVANT}, da der Datensatz damit final gelöscht
     * markiert ist und ältere stände irrelevant sind. Im Fall von {@link iPartsDataReleaseState#CHECK_DELETION} werden
     * entsprechende Einträge in DA_DIALOG_CHANGES angelegt, da diese Datensätze von einem Autor überprüft werden müssen.
     */
    public void saveDeletionDataObject(AbstractDIALOGDataImporter importer, DIALOGImportHelper helper,
                                       GenericEtkDataObjectList<iPartsDataDIALOGChange> dialogChangesDeleteList) {
        if (importDeletionDataObject != null) {
            importer.saveToDB(importDeletionDataObject);
            if (getStatus(importDeletionDataObject).equals(iPartsDataReleaseState.DELETED)) {
                // Löschdatensatz mit Status "gelöscht" wurde angelegt --> Datensätze mit älterem ADAT auf "nicht-relevant" setzen
                // wenn es einen Löschdatensatz gibt, dann haben alle anderen Datensätze ein älteres ADAT
                List<DBOType> dataObjectHistoryList = dataObjectsInDB;
                dataObjectHistoryList.addAll(importDataObjects);
                for (DBOType olderDataObject : dataObjectHistoryList) {
                    iPartsDataReleaseState previousState = iPartsDataReleaseState.getTypeByDBValue(olderDataObject.getFieldValue(getStatusField()));
                    if (previousState != iPartsDataReleaseState.RELEASED) {
                        // Falls der Datensatz zuvor nicht freigegeben war, kann es potentiell DA_DIALOG_CHANGES-Einträge geben.
                        // Diese müssen hier ermittelt und am Ende des Importers gelöscht werden.
                        iPartsDataDIALOGChangeList dialogChanges = iPartsDataDIALOGChangeList.loadDIALOGChangesForDataObject(
                                olderDataObject.getAsId().toDBString(), helper.getProject());
                        dialogChangesDeleteList.deleteAll(dialogChanges, true, false, DBActionOrigin.FROM_DB);
                    }
                    olderDataObject.setFieldValue(getStatusField(), iPartsDataReleaseState.NOT_RELEVANT.getDbValue(), DBActionOrigin.FROM_EDIT);
                    importer.saveToDB(olderDataObject);
                }
            }
            if (getStatus(importDeletionDataObject).equals(iPartsDataReleaseState.CHECK_DELETION)) {
                saveDialogChanges(importer, helper, importDeletionDataObject);
            }
        }
    }

    /**
     * Erzeugt ein {@link iPartsDataDIALOGChange} Objekt für den Typ und die ID des übergebenen <code>dataObject</code>.
     *
     * @param importer
     * @param helper
     * @param dataObject
     */
    private void saveDialogChanges(AbstractDIALOGDataImporter importer, DIALOGImportHelper helper, DBOType dataObject) {
        iPartsDataDIALOGChange.ChangeType changeType = iPartsDataDIALOGChange.ChangeType.getChangeType(dataObject.getAsId().getType());
        // Bei AS Werksdaten von Stücklistenpositionen gibt es keine referenzierten Teilenummern
        if (referencedPartNumbers != null) {
            for (String referencedPartNumber : referencedPartNumbers) {
                iPartsDataDIALOGChange dataDialogChange = helper.createChangeRecord(changeType, dataObject.getAsId(),
                                                                                    referencedSeriesNumber, "", referencedPartNumber, "");
                importer.saveToDB(dataDialogChange);
            }
        }
        // Bei AS Werksdaten von Variantentabellen gibt es keinen referenzierten BCTE Schlüssel
        if (referencedBCTEKey != null) {
            iPartsDataDIALOGChange dataDialogChange = helper.createChangeRecord(changeType, dataObject.getAsId(),
                                                                                referencedSeriesNumber, referencedBCTEKey, "", "");
            importer.saveToDB(dataDialogChange);
        }
    }

    public List<DBOType> getDataObjectsInDB() {
        return dataObjectsInDB;
    }

    public void setDBData(List<DBOType> dbData) {
        if (dbData != null) {
            dataObjectsInDB.addAll(dbData);
        }
    }

    public List<DBOType> getImportDataObjects() {
        return importDataObjects;
    }

    public DBOType getImportDeletionDataObject() {
        return importDeletionDataObject;
    }


    public String getReferencedSeriesNumber() {
        return referencedSeriesNumber;
    }

    public Set<String> getReferencedPartNumbers() {
        return referencedPartNumbers;
    }

    public void setReferencedSeriesNumber(String referencedSeriesNumber) {
        this.referencedSeriesNumber = referencedSeriesNumber;
    }

    public void setReferencedBCTEKey(String referencedBCTEKey) {
        this.referencedBCTEKey = referencedBCTEKey;
    }

    public void setReferencedPartNumbers(Set<String> referencedPartNumbers) {
        this.referencedPartNumbers = referencedPartNumbers;
    }

    public iPartsDataReleaseState getStatus(DBOType dataObject) {
        return iPartsDataReleaseState.getTypeByDBValue(dataObject.getFieldValue(getStatusField()));
    }

    public iPartsImportDataOrigin getSource(DBOType dataObject) {
        return iPartsImportDataOrigin.getTypeFromCode(dataObject.getFieldValue(getSourceField()));
    }

    public abstract Comparator<DBOType> getComparator();

    public abstract String getStatusField();

    public abstract String getSourceField();

    public abstract String getAdatField();
}
