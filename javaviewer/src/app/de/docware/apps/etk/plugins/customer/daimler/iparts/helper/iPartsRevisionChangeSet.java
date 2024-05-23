/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.EtkDbObjectsLayer;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataObjectWithPart;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEntrySourceType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsUserAdminDb;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsAuthorOrderStatus;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrderList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsColorTableDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataColorTableToPartList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataReportConstNodeList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsFactoryDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteCatalogueRefId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsCombTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDbObjectsLayer;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.order.iPartsPicOrderModulesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsReplacePartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleEinPASId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.footnotes.iPartsPartFootnotesCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsResponseData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsResponseSpikes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuForProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductStructures;
import de.docware.framework.modules.db.*;
import de.docware.framework.modules.db.serialization.*;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.SQLStringConvert;

import java.util.*;

/**
 * iParts-spezifisches Änderungsset zur Verwaltung von mehreren Änderungsständen von verschiedenen {@link EtkDataObject}s
 * in Form von {@link SerializedDBDataObject}s für Edit-Funktionen.
 */
public class iPartsRevisionChangeSet extends AbstractRevisionChangeSet<iPartsChangeSetId, iPartsChangeSetEntryId, iPartsDataChangeSetEntry> implements iPartsConst {

    public static final int CURRENT_CHANGE_SET_VERSION = 2;
    public static final Set<String> SYSTEM_USER_IDS = new HashSet<>();

    private iPartsChangeSetSource source;
    private SerializedDbDataObjectAsJSON serializedDbDataObjectAsJSON = new SerializedDbDataObjectAsJSON(true);

    static {
        SYSTEM_USER_IDS.add(TECHNICAL_USER_DIALOG_DELTA_SUPPLY);
        SYSTEM_USER_IDS.add(TECHNICAL_USER_AUTO_RELEASE);
        SYSTEM_USER_IDS.add(TECHNICAL_USER_EXTEND_MODEL_VALIDITY);
        SYSTEM_USER_IDS.add(TECHNICAL_USER_DATA_CORRECTION);
    }

    /**
     * Erzeugt ein temporäres {@link AbstractRevisionChangeSet} z.B. für Edit-Aktionen, die nur temporär im Speicher
     * stattfinden sollen.
     *
     * @param project
     * @return
     */
    public static iPartsRevisionChangeSet createTempChangeSet(EtkProject project, iPartsChangeSetSource source) {
        iPartsChangeSetId tempChangeSetId = new iPartsChangeSetId(StrUtils.makeGUID());

        // loadSerializedDataObjects macht bei einem neuen ChangeSet mit GUID keinen Sinn
        return new iPartsRevisionChangeSet(tempChangeSetId, project, false, source);
    }

    /**
     * Speichert das dataObject mit einem ChangeSet als NEW oder MODIFIED in der DB ab
     *
     * @param project
     * @param dataObject
     * @param source
     * @return
     */
    public static boolean saveDataObjectWithChangeSet(EtkProject project, EtkDataObject dataObject, iPartsChangeSetSource source) {
        iPartsRevisionChangeSet changeSet = iPartsRevisionChangeSet.createTempChangeSet(project, source);
        changeSet.addDataObject(dataObject, false, false, false);
        return changeSet.commit();
    }

    /**
     * Speichert das dataObject mit einem ChangeSet als DELETED in der DB ab
     *
     * @param project
     * @param dataObject
     * @param source
     * @return
     */
    public static boolean deleteDataObjectWithChangeSet(EtkProject project, EtkDataObject dataObject, iPartsChangeSetSource source) {
        iPartsRevisionChangeSet changeSet = iPartsRevisionChangeSet.createTempChangeSet(project, source);
        changeSet.addDataObject(dataObject, true, false, false);
        return changeSet.commit();
    }

    /**
     * Speichert alle dataObjects der dataObjectList in einem ChangeSet als DELETED in der DB ab
     *
     * @param project
     * @param dataObjectList
     * @param source
     * @return
     */
    public static boolean deleteDataObjectListWithChangeSet(EtkProject project, EtkDataObjectList dataObjectList, iPartsChangeSetSource source) {
        iPartsRevisionChangeSet changeSet = iPartsRevisionChangeSet.createTempChangeSet(project, source);
        changeSet.addDataObjectList(dataObjectList);
        return changeSet.commit();
    }

    /**
     * Speichert alle dataObjects der dataObjectList in einem ChangeSet in der DB ab
     *
     * @param project
     * @param dataObjectList
     * @param source
     * @return
     */
    public static boolean saveDataObjectListWithChangeSet(EtkProject project, EtkDataObjectList dataObjectList, iPartsChangeSetSource source) {
        iPartsRevisionChangeSet changeSet = iPartsRevisionChangeSet.createTempChangeSet(project, source);
        changeSet.addDataObjectList(dataObjectList, false, false);
        return changeSet.commit();
    }

    /**
     * Löscht alle relevanten Caches für die aktiven ChangeSets.
     *
     * @param project
     */
    public static void clearCachesForActiveChangeSets(EtkProject project) {
        // Speicherhungrige Caches für das zu deaktivierende ChangeSet löschen
        // iPartsStructure benötigt nicht so viel Speicher, ist dafür aber relativ aufwändig in der Erzeugung und
        // wird immer benötigt -> diesen Cache nicht löschen
        iPartsProductStructures.removeCacheForActiveChangeSets(project);
        KgTuForProduct.removeCacheForActiveChangeSets(project);
        EtkDataAssembly.removeCacheForActiveChangeSets(project);
        iPartsResponseData.removeCacheForActiveChangeSets(project);
        iPartsResponseSpikes.removeCacheForActiveChangeSets(project);
        iPartsPartFootnotesCache.removeCacheForActiveChangeSets(project);
    }

    public iPartsRevisionChangeSet(iPartsChangeSetId changeSetId, EtkProject project) {
        this(changeSetId, project, true);
    }

    public iPartsRevisionChangeSet(iPartsChangeSetId changeSetId, EtkProject project, iPartsChangeSetSource source) {
        this(changeSetId, project, true, source);
    }

    public iPartsRevisionChangeSet(iPartsChangeSetId changeSetId, EtkProject project, boolean loadSerializedDataObjects) {
        this(changeSetId, project, loadSerializedDataObjects, iPartsChangeSetSource.AUTHOR_ORDER);
    }

    protected iPartsRevisionChangeSet(iPartsChangeSetId changeSetId, EtkProject project, boolean loadSerializedDataObjects,
                                      iPartsChangeSetSource source) {
        super(changeSetId, iPartsPlugin.LOG_CHANNEL_CHANGE_SETS, project);
        if (loadSerializedDataObjects) {
            loadFromDB();
        }
        this.source = source;
    }

    /**
     * Legt dieses {@link iPartsRevisionChangeSet} als {@link iPartsDataChangeSet} mit der angegebenen Quelle in der Datenbank
     * an falls es nicht bereits existiert.
     *
     * @param source
     * @return {@link iPartsDataChangeSet}, das neu erzeugt bzw. aus der Datenbank geladen wurde
     */
    public synchronized iPartsDataChangeSet createInDBIfNotExists(final iPartsChangeSetSource source) {
        final EtkDbObjectsLayer dbLayer = project.getDbLayer();
        final VarParam<iPartsDataChangeSet> resultChangeSet = new VarParam<iPartsDataChangeSet>(null);

        project.getEtkDbs().getRevisionsHelper().executeWithoutPseudoTransactions(new FrameworkRunnable() {
            @Override
            public void run(FrameworkThread thread) {
                dbLayer.startTransaction();
                try {
                    // ChangeSet speichern
                    iPartsDataChangeSet dataChangeSet = new iPartsDataChangeSet(project, changeSetId);
                    if (!dataChangeSet.existsInDB()) {
                        dataChangeSet.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                        dataChangeSet.setSource(source, DBActionOrigin.FROM_EDIT);
                        dataChangeSet.setStatus(iPartsChangeSetStatus.NEW, DBActionOrigin.FROM_EDIT);
                        dataChangeSet.saveToDB(false, DBDataObject.PrimaryKeyExistsInDB.FALSE);
                    }
                    resultChangeSet.setValue(dataChangeSet);

                    dbLayer.commit();
                } catch (Throwable e) {
                    dbLayer.rollback();
                    resultChangeSet.setValue(null);
                    Logger.getLogger().throwRuntimeException(e);
                }
            }
        });

        return resultChangeSet.getValue();
    }

    /**
     * Lädt alle serialisierten {@link EtkDataObject}s dieses {@link iPartsRevisionChangeSet} aus der Datenbank und entfernt
     * vorher alle bereits in dieser Instanz vorhandenen {@link SerializedDBDataObject}s.
     */
    public synchronized void loadFromDB() {
        serializedDataObjectsMap.clear();
        serializedDataObjectPerTableMap.clear();
        iPartsDataChangeSetEntryList dataChangeSetEntryList = iPartsDataChangeSetEntryList.loadChangeSetEntriesForChangeSetAndDataObjectIdWithType(project, changeSetId, null);
        if (!dataChangeSetEntryList.isEmpty()) {
            addDataChangeSetEntryList(dataChangeSetEntryList, null);
        }
    }


    /**
     * Lädt nur die serialisierten {@link EtkDataObject}s dieses {@link iPartsRevisionChangeSet} aus der Datenbank mit der
     * angegebenen {@code sourceGUID} und optionalem {@link DBDataObject}-Typ.
     *
     * @param sourceGUID     Kann auch {@code null} oder leer sein zum Ignorieren
     * @param dataObjectType
     * @return Passende geladenen {@link SerializedDBDataObject}s für die übergebenen Parameter
     */
    public synchronized Collection<SerializedDBDataObject> loadSerializedDataObjectsBySourceGUID(String sourceGUID,
                                                                                                 String dataObjectType) {
        iPartsDataChangeSetEntryList dataChangeSetEntryList;
        if (StrUtils.isValid(sourceGUID)) {
            dataChangeSetEntryList = iPartsDataChangeSetEntryList.loadChangeSetEntriesForChangeSetAndTypeBySourceGUID(project,
                                                                                                                      sourceGUID,
                                                                                                                      changeSetId,
                                                                                                                      dataObjectType);
        } else {
            dataChangeSetEntryList = iPartsDataChangeSetEntryList.loadChangeSetEntriesForChangeSetAndIdType(project, changeSetId,
                                                                                                            null, dataObjectType);
        }

        if (!dataChangeSetEntryList.isEmpty()) {
            Collection<SerializedDBDataObject> serializedDBDataObjects = new DwList<>();
            addDataChangeSetEntryList(dataChangeSetEntryList, serializedDBDataObjects);
            return serializedDBDataObjects;
        } else {
            return null;
        }
    }

    public synchronized void addDataChangeSetEntryList(iPartsDataChangeSetEntryList dataChangeSetEntryList, Collection<SerializedDBDataObject> serializedDBDataObjects) {
        EtkDataObjectList<iPartsDataChangeSetEntry> modifiedDataChangeSetEntryList = new GenericEtkDataObjectList<>();
        for (iPartsDataChangeSetEntry changeSetEntry : dataChangeSetEntryList) {
            // SerializedDBDataObject aus den JSON-Strings der aktuellen Daten aller serialisierten DBDataObjects erzeugen
            // und zurückliefern
            String jsonString = changeSetEntry.getCurrentData();
            if (!jsonString.isEmpty()) {
                SerializedDBDataObject serializedDBDataObject = serializedDbDataObjectAsJSON.getFromJSON(jsonString);

                iPartsDataChangeSetEntry dataChangeSetEntry = checkSerializedDataObjectVersionWithUpdate(serializedDBDataObject);
                if (dataChangeSetEntry != null) {
                    modifiedDataChangeSetEntryList.add(dataChangeSetEntry, DBActionOrigin.FROM_EDIT);
                }

                setSerializedDataObject(changeSetEntry.getAsId().getDataObjectIdWithType(), serializedDBDataObject);
                if (serializedDBDataObjects != null) {
                    serializedDBDataObjects.add(serializedDBDataObject);
                }
            }
        }

        // Veränderte ChangeSetEntries in der DB speichern
        if (!modifiedDataChangeSetEntryList.isEmpty()) {
            EtkDbObjectsLayer dbLayer = project.getDbLayer();
            dbLayer.startTransaction();
            dbLayer.startBatchStatement();
            try {
                modifiedDataChangeSetEntryList.saveToDB(project);
                dbLayer.endBatchStatement();
                dbLayer.commit();
            } catch (Throwable e) {
                dbLayer.cancelBatchStatement();
                dbLayer.rollback();
                Logger.getLogger().throwRuntimeException(e);
            }
        }
    }

    private synchronized iPartsDataChangeSetEntry checkSerializedDataObjectVersionWithUpdate(SerializedDBDataObject serializedDBDataObject) {
        if (serializedDBDataObject.getVersion() < CURRENT_CHANGE_SET_VERSION) {
            checkSerializedDataObjectForMAssembly(serializedDBDataObject);

            // Aktualisiertes SerializedDBDataObject im ChangeSetEntry speichern
            serializedDBDataObject.setVersion(CURRENT_CHANGE_SET_VERSION);
            iPartsChangeSetEntryId changeSetEntryId = new iPartsChangeSetEntryId(changeSetId, serializedDBDataObject.createId());
            iPartsDataChangeSetEntry dataChangeSetEntry = new iPartsDataChangeSetEntry(project, changeSetEntryId);
            dataChangeSetEntry.setCurrentData(serializedDbDataObjectAsJSON.getAsJSON(serializedDBDataObject), DBActionOrigin.FROM_EDIT);
            return dataChangeSetEntry;
        }

        return null;
    }

    private void checkSerializedDataObjectForMAssembly(SerializedDBDataObject serializedDBDataObject) {
        // Mit Version 2 Einführung von MAT.M_ASSEMBLY für den Materialstamm von Modulen
        if ((serializedDBDataObject.getVersion() < 2) && ((serializedDBDataObject.getState() == SerializedDBDataObjectState.NEW)
                                                          || (serializedDBDataObject.getState() == SerializedDBDataObjectState.REPLACED))
            && serializedDBDataObject.getType().equals(AssemblyId.TYPE)) {
            // Altes Modul ohne MAT.M_ASSEMBLY -> M_ASSEMBLY auf true setzen im SerializedDBDataObject und speichern
            List<SerializedDBDataObjectList<SerializedDBDataObject>> children = serializedDBDataObject.getCompositeChildren();
            if (children != null) {
                for (SerializedDBDataObjectList<SerializedDBDataObject> child : children) {
                    if (child.getChildName().equals(EtkDataObjectWithPart.AGGREGATE_NAME_PART)) {
                        if (!child.getList().isEmpty()) {
                            // Es kann nur genau ein Materialstamm geben
                            SerializedDBDataObject matSerializedDBDataObject = child.getList().get(0);
                            SerializedDBDataObjectAttribute mAssemblyAttribute = matSerializedDBDataObject.getAttribute(FIELD_M_ASSEMBLY);
                            if (mAssemblyAttribute != null) {
                                if (SQLStringConvert.ppStringToBoolean(mAssemblyAttribute.getValue())) {
                                    break; // M_ASSEMBLY steht schon auf true (kann eigentlich nicht sein...)
                                }
                            } else {
                                mAssemblyAttribute = new SerializedDBDataObjectAttribute();
                                mAssemblyAttribute.setName(FIELD_M_ASSEMBLY);
                                mAssemblyAttribute.setType(DBDataObjectAttribute.TYPE.STRING);

                                // Benutzer und Datum vom Materialstamm setzen
                                mAssemblyAttribute.setUserId(matSerializedDBDataObject.getUserId());
                                mAssemblyAttribute.setDateTime(matSerializedDBDataObject.getDateTime());

                                // Attribut zum matSerializedDBDataObject hinzufügen
                                matSerializedDBDataObject.addAttribute(mAssemblyAttribute);
                            }

                            // M_ASSEMBLY auf true setzen
                            mAssemblyAttribute.setOldValue(SQLStringConvert.booleanToPPString(false));
                            mAssemblyAttribute.setValue(SQLStringConvert.booleanToPPString(true));
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * Lädt alle historischen Änderungsstände für dieses {@link iPartsRevisionChangeSet} aus der Datenbank und liefert diese
     * in einer Map von {@link IdWithType} der serialisierten {@link EtkDataObject}s auf {@link SerializedDBDataObjectHistory} zurück.
     */
    public Map<IdWithType, SerializedDBDataObjectHistory> loadHistoryFromDB() {
        Map<IdWithType, SerializedDBDataObjectHistory> serializedHistoriesMap = new LinkedHashMap<>();
        iPartsDataChangeSetEntryList dataChangeSetEntryList = iPartsDataChangeSetEntryList.loadChangeSetEntriesForChangeSetAndDataObjectIdWithType(project, changeSetId, null);
        if (!dataChangeSetEntryList.isEmpty()) {
            SerializedDbDataObjectAsJSON serializedDbDataObjectAsJSON = new SerializedDbDataObjectAsJSON(true);
            for (iPartsDataChangeSetEntry changeSetEntry : dataChangeSetEntryList) {
                // SerializedDBDataObjectHistory aus den JSON-Strings der histortischen Daten aller serialisierten DBDataObjects
                // erzeugen und zur Map hinzufügen
                String jsonString = changeSetEntry.getHistoryData();
                if (!jsonString.isEmpty()) {
                    SerializedDBDataObjectHistory serializedHistory = serializedDbDataObjectAsJSON.getHistoryFromJSON(jsonString);
                    serializedHistoriesMap.put(changeSetEntry.getAsId().getDataObjectIdWithType(), serializedHistory);
                }
            }
        }
        return serializedHistoriesMap;
    }

    /**
     * Lädt alle historischen Änderungsstände für dieses {@link iPartsRevisionChangeSet} aus der Datenbank und liefert diese
     * pro ID zusammengeführt und bei Bedarf je nach Parameter manipuliert in einer Map von {@link IdWithType} der serialisierten
     * {@link EtkDataObject}s auf {@link SerializedDBDataObject} zurück.
     *
     * @param inheritUserAndDateTime Bei {@code true} wird der Benutzer und das Änderungsdatum an alle relevanten Unter-Elemente
     *                               vererbt. Wird bei gesetztem Flag {@code removeUserAndDateTime} ignoriert.
     * @param removeUserAndDateTime  Bei {@code true} wird der Benutzer und das Änderungsdatum an allen relevanten Unter-Elementen
     *                               entfernt.
     * @param ignoreDataOfUserIds    Optionales Set von Benutzer-IDs, deren {@link SerializedDBDataObject}s beim Zusammenführen
     *                               ignoriert werden sollen
     * @return
     */
    public Map<IdWithType, SerializedDBDataObject> getMergedSerializedDataObjects(boolean inheritUserAndDateTime,
                                                                                  boolean removeUserAndDateTime,
                                                                                  Set<String> ignoreDataOfUserIds) {
        if (removeUserAndDateTime) {
            inheritUserAndDateTime = false;
        }

        Map<IdWithType, SerializedDBDataObject> serializedDBDataObjectsMap = new LinkedHashMap<>();
        Map<IdWithType, SerializedDBDataObjectHistory> serializedHistoriesMap = loadHistoryFromDB();
        for (Map.Entry<IdWithType, SerializedDBDataObjectHistory> historyEntry : serializedHistoriesMap.entrySet()) {
            SerializedDBDataObject mergedDBDataObject = historyEntry.getValue().mergeSerializedDBDataObject(inheritUserAndDateTime,
                                                                                                            ignoreDataOfUserIds);
            if (mergedDBDataObject != null) {
                if (removeUserAndDateTime) {
                    mergedDBDataObject.inheritUserAndDateTime(null, null, true);
                }
                serializedDBDataObjectsMap.put(historyEntry.getKey(), mergedDBDataObject);
            }
        }

        return serializedDBDataObjectsMap;
    }

    /**
     * Extrahiert aus diesem {@link iPartsRevisionChangeSet} mit den Stati states die {@link AssemblyId}s aller Module.
     *
     * @param states
     * @return
     */
    public synchronized Set<AssemblyId> getModuleIdsWithStateAnyOf(SerializedDBDataObjectState... states) {
        Set<AssemblyId> result = new TreeSet<>();
        for (SerializedDBDataObject serializedDBDataObject : serializedDataObjectsMap.values()) {
            if (serializedDBDataObject.getType().equals(AssemblyId.TYPE) && Utils.contains(states, serializedDBDataObject.getState())) {
                IdWithType id = serializedDBDataObject.createId();
                AssemblyId assemblyId = new AssemblyId(id.getValue(1), id.getValue(2));
                result.add(assemblyId);
            }
        }
        return result;
    }

    /**
     * Extrahiert aus diesem {@link iPartsRevisionChangeSet} die {@link AssemblyId}s aller bearbeiteten Module.
     *
     * @return
     */
    public Set<AssemblyId> getModifiedModuleIds() {
        return getModuleIdsWithStateAnyOf(SerializedDBDataObjectState.NEW, SerializedDBDataObjectState.REPLACED,
                                          SerializedDBDataObjectState.MODIFIED, SerializedDBDataObjectState.DELETED,
                                          SerializedDBDataObjectState.LOADED, SerializedDBDataObjectState.COMMITTED,
                                          SerializedDBDataObjectState.DELETED_COMMITTED);
    }

    /**
     * Serialisiert die übergebene {@link EtkDataObjectList} und fügt die einzelnen {@link EtkDataObject}s zu diesem
     * {@link AbstractRevisionChangeSet} hinzu inklusive der gelöschten {@link EtkDataObject}s (sowohl im Speicher als auch
     * in der Datenbank). Falls nicht bereits vorhanden wird das Änderungsset selbst in der Datenbank
     * angelegt mit Quelle {@link iPartsChangeSetSource#AUTHOR_ORDER}.
     *
     * @param resetModifiedFlags Flag, ob alle Flags, die dieses {@link DBDataObject} als verändert markieren, zurückgesetzt
     *                           werden sollen.
     * @param isCommitted        Flag, ob die {@link DBDataObject}s außerhalb dieser Methode bereits direkt in der Datenbank
     *                           abgespeichert werden, weil sie in diesem {@link AbstractRevisionChangeSet} lediglich aufglistet,
     *                           aber nicht mehr simuliert oder bei der Freigabe in der Datenbank abgespeichert werden sollen.
     * @return Liste der serialisierten {@link EtkDataObject}s als {@link SerializedDBDataObject}s bzw. {@code null} falls
     * das jeweils entsprechende {@link EtkDataObject} unverändert war.
     */
    // Hier kein synchronized, weil es sonst einen Deadlock mit super.addDataObjectList() im Runnable des anderen Threads gibt
    @Override
    public List<SerializedDBDataObject> addDataObjectList(DBDataObjectList dataObjectList, boolean resetModifiedFlags,
                                                          boolean isCommitted) {
        final VarParam<List<SerializedDBDataObject>> serializedDBDataObjectList = new VarParam<>();

        FrameworkRunnable addDataObjectListRunnable = new FrameworkRunnable() {
            @Override
            public void run(FrameworkThread thread) {
                // Hier schon synchronisieren und nicht erst indirekt über super.addDataObjectList(), damit gar nicht erst
                // parallel eine neue Transaktion gestartet wird
                synchronized (iPartsRevisionChangeSet.this) {
                    EtkDbObjectsLayer dbLayer = project.getDbLayer();
                    dbLayer.startTransaction();
                    try {
                        serializedDBDataObjectList.setValue(iPartsRevisionChangeSet.super.addDataObjectList(dataObjectList,
                                                                                                            resetModifiedFlags,
                                                                                                            isCommitted));

                        dbLayer.commit();
                    } catch (Throwable e) {
                        dbLayer.rollback();
                        loadFromDB(); // Zum Korrigieren das ChangeSet wieder komplett aus der Datenbank laden
                        Logger.getLogger().throwRuntimeException(e);
                    }
                }
            }
        };

        if (project.getEtkDbs().getRevisionsHelper() == null) {
            // wenn es keinen RevisionsHelper gibt, dann können auch keine Changesets aktiv sein, also braucht man auch
            // keine Pseudotransaktionen
            addDataObjectListRunnable.run(null);
        } else {
            // SerializedDBDataObjects erstellen und die ChangeSetEntries innerhalb von einer Transaktion außerhalb einer
            // evtl. aktiven Pseudo-Transaktion
            project.getEtkDbs().getRevisionsHelper().executeWithoutPseudoTransactions(addDataObjectListRunnable);
        }

        return serializedDBDataObjectList.getValue();
    }

    /**
     * Fügt die einzelnen {@link SerializedDBDataObject}s der übergebenen Liste zu diesem {@link AbstractRevisionChangeSet}
     * hinzu (sowohl im Speicher als auch in der Datenbank), was einem Merge von evtl. vorhandenen ChangeSetEntries mit den
     * übergebenen {@link SerializedDBDataObject}s entspricht. Falls nicht bereits vorhanden wird das Änderungsset selbst
     * in der Datenbank angelegt mit Quelle {@link iPartsChangeSetSource#AUTHOR_ORDER}.
     *
     * @param serializedDBDataObjectList
     */
    // Hier kein synchronized, weil es sonst einen Deadlock mit super.addSerializedDataObjectList() im Runnable des anderen Threads gibt
    @Override
    public void addSerializedDataObjectList(final Collection<SerializedDBDataObject> serializedDBDataObjectList) {
        // SerializedDBDataObjects und die ChangeSetEntries innerhalb von einer Transaktion außerhalb einer evtl. aktiven
        // Pseudo-Transaktion
        project.getEtkDbs().getRevisionsHelper().executeWithoutPseudoTransactions(new FrameworkRunnable() {
            @Override
            public void run(FrameworkThread thread) {
                // Hier schon synchronisieren und nicht erst indirekt über super.addSerializedDataObjectList(), damit gar
                // nicht erst parallel eine neue Transaktion gestartet wird
                synchronized (iPartsRevisionChangeSet.this) {
                    EtkDbObjectsLayer dbLayer = project.getDbLayer();
                    dbLayer.startTransaction();
                    try {
                        iPartsRevisionChangeSet.super.addSerializedDataObjectList(serializedDBDataObjectList);

                        dbLayer.commit();
                    } catch (Throwable e) {
                        dbLayer.rollback();
                        loadFromDB(); // Zum Korrigieren das ChangeSet wieder komplett aus der Datenbank laden
                        Logger.getLogger().throwRuntimeException(e);
                    }
                }
            }
        });
    }

    @Override
    protected synchronized void saveAddedSerializedDataObjects(Map<iPartsChangeSetEntryId, iPartsDataChangeSetEntry> addedSerializedDataObjectsMap) {
        if (addedSerializedDataObjectsMap.isEmpty()) {
            return;
        }

        // ChangeSet speichern (sollte eigentlich schon in der DB existieren; falls nicht, wird es neu angelegt
        iPartsDataChangeSet dataChangeSet = new iPartsDataChangeSet(project, changeSetId);
        boolean changeSetExists = true;
        if (!dataChangeSet.existsInDB()) {
            changeSetExists = false;
            dataChangeSet.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            dataChangeSet.setSource(source, DBActionOrigin.FROM_EDIT);
        } else if (dataChangeSet.getStatus() == iPartsChangeSetStatus.COMMITTED) {
            String firstDataObjectId = addedSerializedDataObjectsMap.keySet().iterator().next().getDataObjectId();
            Logger.getLogger().throwRuntimeException("Trying to add a change set entry for the DBDataObject \"" + firstDataObjectId
                                                     + "\" to the already committed change set \"" + changeSetId.getGUID() + "\"!");
        }

        project.getEtkDbs().startBatchStatement();
        try {
            // Beim ersten Speichern: Statusübergang des Autoren-Auftrags von ORDERED auf WORKING
            if (isEmpty()) {
                iPartsDataAuthorOrder dataAuthorOrder = iPartsDataAuthorOrderList.getAuthorOrderByChangeSet(iPartsRevisionChangeSet.this);
                if (dataAuthorOrder != null) {
                    iPartsAuthorOrderStatus currentStatus = dataAuthorOrder.getStatus();
                    EnumSet<iPartsAuthorOrderStatus> startStates = iPartsAuthorOrderStatus.getGoToStatesForGivenState(iPartsAuthorOrderStatus.getStartState());
                    if (startStates.contains(currentStatus)) {
                        dataAuthorOrder.changeStatus(iPartsAuthorOrderStatus.getNextState(currentStatus));
                        dataAuthorOrder.saveToDB(false, DBDataObject.PrimaryKeyExistsInDB.TRUE);
                    }
                }
            }

            // Status vom ChangeSet muss IN_PROCESS sein
            if (dataChangeSet.getStatus() != iPartsChangeSetStatus.IN_PROCESS) {
                dataChangeSet.setStatus(iPartsChangeSetStatus.IN_PROCESS, DBActionOrigin.FROM_EDIT);
                dataChangeSet.saveToDB(false, changeSetExists ? DBDataObject.PrimaryKeyExistsInDB.TRUE : DBDataObject.PrimaryKeyExistsInDB.FALSE);
            }

            // Zunächst alle alten Primärschlüssel löschen
            for (Map.Entry<iPartsChangeSetEntryId, iPartsDataChangeSetEntry> addedSerializedDataObjectEntry : addedSerializedDataObjectsMap.entrySet()) {
                if (addedSerializedDataObjectEntry.getValue() == null) { // null ist Kenner für das Löschen von alten Primärschlüsseln
                    iPartsChangeSetEntryId oldChangeSetEntryId = addedSerializedDataObjectEntry.getKey();
                    IdWithType oldDataObjectId = oldChangeSetEntryId.getDataObjectIdWithType();
                    setSerializedDataObject(oldDataObjectId, null);

                    // Datensatz aus der DB löschen
                    iPartsDataChangeSetEntry dataChangeSetEntry = new iPartsDataChangeSetEntry(project, oldChangeSetEntryId);
                    dataChangeSetEntry.deleteFromDB(true);
                }
            }

            // Batch-Statements ausführen, damit alle gelöschten ChangeSetEntries auch wirklich geköscht sind
            project.getEtkDbs().endBatchStatement();
            project.getEtkDbs().startBatchStatement();

            // Jetzt für jeden ChangeSetEntry prüfen, ob dieser neu ist, damit das Batch-Statement nicht durch die Selects
            // immer unterbrochen wird
            for (iPartsDataChangeSetEntry dataChangeSetEntry : addedSerializedDataObjectsMap.values()) {
                if (dataChangeSetEntry != null) { // null ist Kenner für das Löschen von alten Primärschlüsseln
                    // Explizit ein neues iPartsDataChangeSetEntry-Objekt verwenden für die Prüfung
                    iPartsChangeSetEntryId changeSetEntryId = dataChangeSetEntry.getAsId();
                    iPartsDataChangeSetEntry dataChangeSetEntryForExistsCheck = new iPartsDataChangeSetEntry(project, changeSetEntryId);
                    dataChangeSetEntry.__internal_setNew(!dataChangeSetEntryForExistsCheck.existsInDB());
                }
            }

            // Jetzt erst die SerializedDBDataObjects in den ChangeSetEntries mit den (neuen) Primärschlüsseln speichern
            for (iPartsDataChangeSetEntry dataChangeSetEntry : addedSerializedDataObjectsMap.values()) {
                if (dataChangeSetEntry != null) { // null ist Kenner für das Löschen von alten Primärschlüsseln
                    SerializedDBDataObject serializedDataObject = dataChangeSetEntry.getSerializedDBDataObject();

                    // SerializedDBDataObject mit dem (neuen) Primärschlüssel in der serializedDataObjectsMap ablegen
                    setSerializedDataObject(serializedDataObject.createId(), serializedDataObject);

                    // ChangeSetEntry speichern
                    DBDataObject.PrimaryKeyExistsInDB forcePKExistsInDB = dataChangeSetEntry.isNew() ? DBDataObject.PrimaryKeyExistsInDB.FALSE
                                                                                                     : DBDataObject.PrimaryKeyExistsInDB.TRUE;
                    dataChangeSetEntry.saveToDB(false, forcePKExistsInDB);
                }
            }

            project.getEtkDbs().endBatchStatement();
        } catch (Exception e) {
            project.getEtkDbs().cancelBatchStatement();
            throw e;
        }
    }

    @Override
    protected synchronized void addSerializedDataObject(SerializedDBDataObject serializedDBDataObject,
                                                        Map<iPartsChangeSetEntryId, iPartsDataChangeSetEntry> addedSerializedDataObjectsMap,
                                                        Set<iPartsChangeSetEntryId> alreadyProcessedChangeSetIds) {
        inheritExplicitUserForSerializedDBDataObject(serializedDBDataObject);
        serializedDBDataObject.setVersion(CURRENT_CHANGE_SET_VERSION);

        IdWithType dataObjectId = serializedDBDataObject.createId();
        IdWithType dataObjectOldId = serializedDBDataObject.createOldId();
        if (dataObjectOldId == null) {
            // Primärschlüssel bleibt gleich
            dataObjectOldId = dataObjectId;
        } else {
            // Der Primärschlüssel hat sich in dieser Aktion geändert. Die bisherigen Änderungen müssen aus dem alten Schlüssel geladen werden.
            // -> dataObjectOldId ist nicht null und unterscheidet sich von dataObjectId
        }

        // ChangeSetEntry laden, bei Primärschlüsseländerung Key ändern und speichern.
        // Der ChangeSetEntry muss mit dem bisherigen Primärschlüssel geladen werden.
        iPartsChangeSetEntryId changeSetEntryId = createChangeSetEntryId(changeSetId, dataObjectOldId);
        iPartsDataChangeSetEntry dataChangeSetEntry = new iPartsDataChangeSetEntry(project, changeSetEntryId);
        SerializedDBDataObject serializedCurrentData;
        SerializedDBDataObjectHistory serializedHistory;
        if (alreadyProcessedChangeSetIds.contains(changeSetEntryId) || !dataChangeSetEntry.existsInDB()) {
            dataChangeSetEntry.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);

            // Verweis Konstruktion Datensatz GUID setzen
            if (serializedDBDataObject.getType().equals(PartListEntryId.TYPE)) {
                String kSourceGUID = serializedDBDataObject.getAttributeValue(FIELD_K_SOURCE_GUID, true, project);
                if (kSourceGUID != null) {
                    dataChangeSetEntry.setFieldValue(FIELD_DCE_DO_SOURCE_GUID, kSourceGUID, DBActionOrigin.FROM_EDIT);
                }
                String kMatNr = serializedDBDataObject.getAttributeValue(FIELD_K_MATNR, true, project);
                if (StrUtils.isValid(kMatNr)) {
                    dataChangeSetEntry.setFieldValue(FIELD_DCE_MATNR, kMatNr, DBActionOrigin.FROM_EDIT);
                }
            } else if (serializedDBDataObject.getType().equals(iPartsFactoryDataId.TYPE)) {
                String factoryDataGUID = serializedDBDataObject.getAttributeValue(FIELD_DFD_GUID, true, project);
                if (factoryDataGUID != null) {
                    dataChangeSetEntry.setFieldValue(FIELD_DCE_DO_SOURCE_GUID, factoryDataGUID, DBActionOrigin.FROM_EDIT);
                }
            }

            serializedCurrentData = serializedDBDataObject;
            serializedHistory = SerializedDBDataObjectFactory.createSerializedDBDataObjectHistory();
        } else { // Bisheriges SerializedDBDataObject und History aus der DB laden
            String currentData = dataChangeSetEntry.getCurrentData();
            if (currentData.isEmpty()) {
                serializedCurrentData = serializedDBDataObject;
            } else { // Bisheriges SerializedDBDataObject mit dem neuen SerializedDBDataObject zusammenführen
                serializedCurrentData = serializedDbDataObjectAsJSON.getFromJSON(currentData);

                boolean wasDeleted = serializedCurrentData.getState() == SerializedDBDataObjectState.DELETED;

                // Falls das SerializedDBDataObject vorher DELETED war und nun angeblich REVERTED ist, dann wurde dieses
                // SerializedDBDataObject in der ChangeSet-Simulation bereits berücksichtigt (z.B. weil das SerializedDBDataObject
                // fälschlicherweise doppelt zum ChangeSet hinzugefügt wird) -> neuer Zustand ist wieder DELETED und nicht REVERTED
                // Zustand muss vor dem Merge korrigiert werden, da ansonsten alle Attribute durch REVERTED entfernt werden würden.
                if ((serializedDBDataObject.getState() == SerializedDBDataObjectState.REVERTED) && wasDeleted) {
                    serializedDBDataObject.setState(SerializedDBDataObjectState.DELETED);
                }

                serializedCurrentData.merge(serializedDBDataObject, false, compressSerializedDataObjectsAfterMerge);
            }

            String historyData = dataChangeSetEntry.getHistoryData();
            if (historyData.isEmpty()) {
                serializedHistory = SerializedDBDataObjectFactory.createSerializedDBDataObjectHistory();
            } else {
                serializedHistory = serializedDbDataObjectAsJSON.getHistoryFromJSON(historyData);
            }
        }

        // Edit-Info für Stücklisteneinträge setzen
        if (serializedCurrentData.getType().equals(PartListEntryId.TYPE)) {
            // DAIMLER-15496 Gelöschte Stücklisteneinträge ermitteln, die ursprünglich automatisch erzeugt wurden
            boolean deletedAfterAutoCreated = false;
            if (((serializedCurrentData.getState() == SerializedDBDataObjectState.REVERTED) && serializedCurrentData.isKeepRevertedState())
                || (serializedCurrentData.getState() == SerializedDBDataObjectState.DELETED)) {
                if (Utils.objectEquals(serializedCurrentData.getAttributeValue(FIELD_K_WAS_AUTO_CREATED, true, project), SQLStringConvert.booleanToPPString(true))
                    && Utils.objectEquals(serializedCurrentData.getAttributeValue(FIELD_K_AUTO_CREATED, true, project), SQLStringConvert.booleanToPPString(false))) {
                    deletedAfterAutoCreated = true;
                    dataChangeSetEntry.setFieldValue(FIELD_DCE_EDIT_INFO, CHANGE_SET_ENTRY_EDIT_INFO.DELETED_AFTER_AUTO_CREATED.name(), DBActionOrigin.FROM_EDIT);
                }
            }

            if (!deletedAfterAutoCreated) {
                boolean modelOrSAAValidityChanged = isArrayValueChanged(serializedCurrentData, FIELD_K_MODEL_VALIDITY);
                if (!modelOrSAAValidityChanged) {
                    modelOrSAAValidityChanged = isArrayValueChanged(serializedCurrentData, FIELD_K_SA_VALIDITY);
                }
                if (modelOrSAAValidityChanged) {
                    dataChangeSetEntry.setFieldValue(FIELD_DCE_EDIT_INFO, CHANGE_SET_ENTRY_EDIT_INFO.SAA_WORK_BASKET_RELEVANT.name(),
                                                     DBActionOrigin.FROM_EDIT);
                } else { // Es gibt aktuell keinen anderen Enum-Wert abgesehen von SAA_WORK_BASKET_RELEVANT und DELETED_AFTER_AUTO_CREATED -> leer setzen
                    dataChangeSetEntry.setFieldValue(FIELD_DCE_EDIT_INFO, "", DBActionOrigin.FROM_EDIT);
                }
            }
        }

        alreadyProcessedChangeSetIds.add(changeSetEntryId);
        if (!serializedCurrentData.isRevertedWithoutKeepState()) {
            if (!iPartsDbObjectsLayer.TEXTS_WITH_ID_CREATABLE_IN_CHANGE_SETS) {
                checkAndCorrectMultiLanguageAttributes(serializedCurrentData);
            }

            dataChangeSetEntry.setCurrentData(serializedDbDataObjectAsJSON.getAsJSON(serializedCurrentData), DBActionOrigin.FROM_EDIT);
            dataChangeSetEntry.setSerializedDBDataObject(serializedCurrentData);

            serializedHistory.addHistoryEntry(serializedDBDataObject, StrUtils.isEmpty(getExplicitUser()));
            dataChangeSetEntry.setHistoryData(serializedDbDataObjectAsJSON.getAsJSON(serializedHistory), DBActionOrigin.FROM_EDIT);

            iPartsChangeSetEntryId newChangeSetEntryId;
            if (!Utils.objectEquals(dataObjectId, dataObjectOldId)) {
                // Bei einer Primärschlüsseländerung muss die alte ID im Entry für Recherchezwecke gespeichert werden
                // Falls der Wert aber schon gesetzt ist, dann kommt die alte ID aus einer vorherigen Änderung und dann ist das
                // der wirklich alte: aus 1 -> 2 und 2 -> 3 wird 1 -> 3
                if (dataChangeSetEntry.getFieldValue(FIELD_DCE_DO_ID_OLD).isEmpty()) {
                    dataChangeSetEntry.setFieldValue(FIELD_DCE_DO_ID_OLD, dataObjectOldId.toDBString(),
                                                     DBActionOrigin.FROM_EDIT);
                }

                // Bei Primärschlüsseländerungen merken, dass das alte SerializedDBDataObject gelöscht werden muss
                // isDeleteOldId() muss vom übergebenen serializedDBDataObject und NICHT vom serializedCurrentData
                // überprüft werden wegen Primärschlüsseländerungen der Art 1 -> 2 -> 1
                if (serializedDBDataObject.isDeleteOldId()) {
                    // Merken, dass wir am Ende den alten Datensatz löschen müssen
                    addedSerializedDataObjectsMap.put(changeSetEntryId, null); // null ist Kenner für das Löschen
                }
                newChangeSetEntryId = createChangeSetEntryId(changeSetId, dataObjectId);

                // BLOBs werden automatisch nachgeladen falls notwendig
                dataChangeSetEntry.setId(newChangeSetEntryId, DBActionOrigin.FROM_EDIT);
                dataChangeSetEntry.setDeleteOldId(false);
            } else {
                newChangeSetEntryId = changeSetEntryId;
            }

            // (Neuen) Primärschlüssel und ChangeSetEntry in der addedSerializedDataObjectsMap ablegen
            addedSerializedDataObjectsMap.put(newChangeSetEntryId, dataChangeSetEntry);
        } else {
            // Entries, die reverted wurden (angelegt und wieder gelöscht) müssen gelöscht werden
            addedSerializedDataObjectsMap.put(changeSetEntryId, null); // null ist Kenner für das Löschen
        }
    }

    private boolean isArrayValueChanged(SerializedDBDataObject serializedDBDataObject, String attributeName) {
        boolean arrayValueChanged = false;
        SerializedDBDataObjectAttribute attribute = serializedDBDataObject.getAttribute(attributeName);
        if (attribute != null) {
            if (serializedDBDataObject.getState() == SerializedDBDataObjectState.NEW) {
                // Bei neuen SerializedDBDataObjects muss es mindestens einen Eintrag im Array geben
                SerializedEtkDataArray array = attribute.getArray();
                arrayValueChanged = (array != null) && !array.getValues().isEmpty();
            } else {
                // Bei allen anderen Zuständen den alten Array-Wert mit dem neuen Array-Wert vergleichen
                arrayValueChanged = attribute.isValueModified();
            }
        }
        return arrayValueChanged;
    }

    @Override
    public synchronized boolean commit() {
        return commit(false, true, null);
    }

    /**
     * Gibt dieses Änderungsset frei und speichert die Änderungen dauerhaft in der Datenbank ab.
     *
     * @param logCommitAsInfo            Soll der Commit als Info geloggt werden?
     * @param clearCachesAfterCommit     Sollen nach dem Commit (falls bei dieser {@link iPartsChangeSetSource} der ChangeSet-Inhalt
     *                                   überhaupt in der Datenbank gespeichert wird) die Caches gelöscht werden?
     * @param afterSaveChangeSetRunnable Optionales {@link Runnable}, welches nach dem Speichern des Änderungssets in der
     *                                   Datenbank vor dem finalen Commit und evtl. Löschen von Caches ausgeführt wird
     * @return
     */
    public synchronized boolean commit(boolean logCommitAsInfo, boolean clearCachesAfterCommit, Runnable afterSaveChangeSetRunnable) {
        return commit(logCommitAsInfo, clearCachesAfterCommit, false, null, afterSaveChangeSetRunnable);
    }

    /**
     * Gibt dieses Änderungsset frei und speichert die Änderungen dauerhaft in der Datenbank ab.
     *
     * @param logCommitAsInfo             Soll der Commit als Info geloggt werden?
     * @param clearCachesAfterCommit      Sollen nach dem Commit (falls bei dieser {@link iPartsChangeSetSource} der ChangeSet-Inhalt
     *                                    überhaupt in der Datenbank gespeichert wird) die Caches gelöscht werden?
     * @param forceClearCachesAfterCommit Sollen nach dem Commit die Caches auf jeden Fall gelöscht werden?
     * @param beforeSaveChangeSetRunnable Optionales {@link BeforeSaveChangeSetInterface}, welches vor dem Speichern des
     *                                    Änderungssets in der Datenbank ausgeführt wird. Übergeben wird auch das Änderungsset.
     * @param afterSaveChangeSetRunnable  Optionales {@link Runnable}, welches nach dem Speichern des Änderungssets in der
     *                                    Datenbank vor dem finalen Commit und evtl. Löschen von Caches ausgeführt wird
     * @return
     */
    public synchronized boolean commit(boolean logCommitAsInfo, boolean clearCachesAfterCommit, boolean forceClearCachesAfterCommit,
                                       BeforeSaveChangeSetInterface beforeSaveChangeSetRunnable, Runnable afterSaveChangeSetRunnable) {
        if (isEmpty()) {
            return false;
        }

        Logger.log(iPartsPlugin.LOG_CHANNEL_CHANGE_SETS, LogType.DEBUG, "Committing change set \"" + changeSetId.getGUID()
                                                                        + "\" by " + iPartsUserAdminDb.getUserNameForLogging(project)
                                                                        + "...");
        boolean saveDataObjectsToDB = !iPartsChangeSetSource.isSpecialChangeSetSource(source);
        iPartsDataChangeSet dataChangeSet = new iPartsDataChangeSet(project, changeSetId);
        if (!dataChangeSet.existsInDB()) {
            Logger.getLogger().throwRuntimeException("Change set \"" + changeSetId.getGUID() + "\" does not exist in the database but should be committed!");
        } else if (dataChangeSet.getStatus() == iPartsChangeSetStatus.COMMITTED) {
            Logger.getLogger().throwRuntimeException("Change set \"" + changeSetId.getGUID() + "\" is already committed!");
        }

        // Inhalte vom ChangeSet durchsuchen für minimales Löschen der Caches
        boolean containsResponseData = getSerializedObjectsByTable(TABLE_DA_RESPONSE_DATA) != null;
        boolean containsResponseSpikes = getSerializedObjectsByTable(TABLE_DA_RESPONSE_SPIKES) != null;
        Set<iPartsAssemblyId> modifiedAssemblyIds = getModifiedAssemblyIds(containsResponseData, containsResponseSpikes);
        Set<iPartsProductId> modifiedProductIds = getModifiedProductIds();

        EtkDbObjectsLayer dbLayer = project.getDbLayer();
        dbLayer.startTransaction();
        dbLayer.startBatchStatement();
        try {
            if (saveDataObjectsToDB) {
                saveToDB();
            }

            // ChangeSet speichern
            dataChangeSet.setStatus(iPartsChangeSetStatus.COMMITTED, DBActionOrigin.FROM_EDIT);
            dataChangeSet.setCommitDate(Calendar.getInstance(), DBActionOrigin.FROM_EDIT);
            dataChangeSet.setSource(source, DBActionOrigin.FROM_EDIT);
            if (beforeSaveChangeSetRunnable != null) {
                beforeSaveChangeSetRunnable.beforeSaveChangeSet(project, dataChangeSet);
            }
            dataChangeSet.saveToDB(false, DBDataObject.PrimaryKeyExistsInDB.TRUE);

            // Alle Berechnungen für die Auswertung von Teilepositionen löschen für das freigegebene ChangeSet
            iPartsDataReportConstNodeList.deleteAllDataForChangesetGuid(project, changeSetId.getGUID());

            // Alle Primärschlüssel-Reservierungen löschen für das freigegebene ChangeSet
            iPartsDataReservedPKList.deletePrimaryKeysForChangeSet(project, changeSetId);

            if (!Thread.currentThread().isInterrupted()) {
                if (afterSaveChangeSetRunnable != null) {
                    afterSaveChangeSetRunnable.run();
                }
                dbLayer.endBatchStatement();
                dbLayer.commit();
                if (forceClearCachesAfterCommit || (saveDataObjectsToDB && clearCachesAfterCommit)) {
                    try {
                        ClearRetailRelevantCachesEvent.invalidateRetailRelevantCaches(containsResponseData, containsResponseSpikes,
                                                                                      modifiedAssemblyIds, modifiedProductIds);
                    } catch (Exception e) {
                        Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                    }
                }

                Logger.log(iPartsPlugin.LOG_CHANNEL_CHANGE_SETS, logCommitAsInfo ? LogType.INFO : LogType.DEBUG,
                           "Change set \"" + changeSetId.getGUID() + "\" committed successfully by " + iPartsUserAdminDb.getUserNameForLogging(project));
                return true;
            } else {
                dbLayer.cancelBatchStatement();
                dbLayer.rollback();
                Logger.log(iPartsPlugin.LOG_CHANNEL_CHANGE_SETS, LogType.DEBUG, "Commit of change set \"" + changeSetId.getGUID()
                                                                                + "\" cancelled by " + iPartsUserAdminDb.getUserNameForLogging(project));
                return false;
            }
        } catch (Throwable e) {
            dbLayer.cancelBatchStatement();
            dbLayer.rollback();
            Logger.log(iPartsPlugin.LOG_CHANNEL_CHANGE_SETS, LogType.ERROR, "Error during commit of change set \"" + changeSetId.getGUID()
                                                                            + "\"");
            Logger.getLogger().throwRuntimeException(e);
            return false;
        }
    }

    private Set<String> getPrimaryKeysByTable(String tableName, int primaryKeyIndex) {
        Set<String> primaryKeys = new TreeSet<>();
        Collection<SerializedDBDataObject> serializedDataObjects = getSerializedObjectsByTable(tableName);
        if (serializedDataObjects != null) {
            for (SerializedDBDataObject serializedDataObject : serializedDataObjects) {
                if (serializedDataObject.getState().isMustBeSimulatedAndSaved()) {
                    IdWithType id = serializedDataObject.createId();
                    if (id.getIdLength() > primaryKeyIndex) {
                        primaryKeys.add(id.getValue(primaryKeyIndex + 1)); // Typ ist an Index 0
                    }
                }
            }
        }
        return primaryKeys;
    }

    /**
     * Liefert alle durch dieses {@link iPartsRevisionChangeSet} direkt oder indirekt veränderten {@link iPartsProductId}s
     * zurück, um diese nach dem Commit aus dem Cache zu entfernen.
     *
     * @return
     */
    public Set<iPartsProductId> getModifiedProductIds() {
        // Relevante Produkte (bzw. genauer Produktstrukturen) anhand der SerializedDBDataObjects im ChangeSet bestimmen
        Set<iPartsProductId> modifiedProductIds = new TreeSet<>();
        modifiedProductIds.addAll(getProductIDsByTable(TABLE_DA_PRODUCT_MODULES, 0));
        modifiedProductIds.addAll(getProductIDsByTable(TABLE_DA_PRODUCT_SAS, 0));
        modifiedProductIds.addAll(getProductIDsByTable(TABLE_DA_MODULES_EINPAS, 0));
        modifiedProductIds.addAll(getProductIDsByTable(TABLE_DA_KGTU_AS, 0));

        // Alle Fahrzeug-Produkte von veränderten Aggregate-Produkten sind ebenfalls relevant
        Set<iPartsProductId> vehicleProductIds = new TreeSet<>();
        for (iPartsProductId modifiedProductId : modifiedProductIds) {
            iPartsProduct product = iPartsProduct.getInstance(project, modifiedProductId);
            if (product.isAggregateProduct(project)) {
                List<iPartsProduct> vehicleProducts = product.getVehicles(project);
                for (iPartsProduct vehicleProduct : vehicleProducts) {
                    vehicleProductIds.add(vehicleProduct.getAsId());
                }
            }
        }
        modifiedProductIds.addAll(vehicleProductIds);
        return modifiedProductIds;
    }

    private Set<iPartsProductId> getProductIDsByTable(String tableName, int pkIndexForProductNumber) {
        Set<iPartsProductId> productIds = new TreeSet<>();
        for (String primaryKey : getPrimaryKeysByTable(tableName, pkIndexForProductNumber)) {
            productIds.add(new iPartsProductId(primaryKey));
        }
        return productIds;
    }

    /**
     * Liefert alle durch dieses {@link iPartsRevisionChangeSet} direkt oder indirekt veränderten {@link iPartsAssemblyId}s
     * zurück, um diese nach dem Commit aus dem Cache zu entfernen.
     *
     * @param containsResponseData   Flag, ob in diesem {@link iPartsRevisionChangeSet} veränderte Idents enthalten sind
     * @param containsResponseSpikes Flag, ob in diesem {@link iPartsRevisionChangeSet} veränderte Ausreißer enthalten sind
     * @return
     */
    public Set<iPartsAssemblyId> getModifiedAssemblyIds(boolean containsResponseData, boolean containsResponseSpikes) {
        Set<iPartsAssemblyId> modifiedAssemblyIds = new TreeSet<>();

        // Einzelne Module müssen nicht bestimmt werden, wenn Idents oder Ausreißer verändert wurden, weil in diesem Fall
        // alle Retail-Module aus dem Cache entfernt werden müssen aufgrund der potenziell sehr großen Zahl an betroffenen Modulen
        if (containsResponseData || containsResponseSpikes) {
            return modifiedAssemblyIds;
        }

        // Relevante Module anhand der SerializedDBDataObjects im ChangeSet bestimmen
        modifiedAssemblyIds.addAll(getAssemblyIDsByTable(TABLE_DA_MODULE, 0, false));
        modifiedAssemblyIds.addAll(getAssemblyIDsByTable(TABLE_KATALOG, 0, true)); // inkl. Konstruktions-Stücklisteneinträgen

        // Relevante Module anhand des BCTE-Schlüssel von Werkseinsatzdaten bestimmen
        addModifiedAssemblyIdsForFactoryData(modifiedAssemblyIds);

        // Relevante Module für Farbvarianten(tabellen) bestimmen
        addModifiedAssemblyIdsForColorTables(modifiedAssemblyIds);

        return modifiedAssemblyIds;
    }

    private void addModifiedAssemblyIdsForFactoryData(Set<iPartsAssemblyId> modifiedAssemblyIds) {
        Set<iPartsDialogBCTEPrimaryKey> bcteKeysForModifiedFactoryData = getBCTEKeysForModifiedFactoryData();
        if (!bcteKeysForModifiedFactoryData.isEmpty()) {
            ASUsageHelper asUsageHelper = new ASUsageHelper(project);
            for (iPartsDialogBCTEPrimaryKey bcteKey : bcteKeysForModifiedFactoryData) {
                List<PartListEntryId> partListEntryIds = asUsageHelper.getPartListEntryIdsUsedInAS(bcteKey);
                if (partListEntryIds != null) {
                    for (PartListEntryId partListEntryId : partListEntryIds) {
                        modifiedAssemblyIds.add(new iPartsAssemblyId(partListEntryId.getKVari(), partListEntryId.getKVer()));
                    }
                }

                // Die Konstruktions-Stückliste ist ebenfalls betroffen
                String virtualIdString = iPartsVirtualNode.getVirtualIdString(bcteKey.getHmMSmId());
                modifiedAssemblyIds.add(new iPartsAssemblyId(virtualIdString, ""));
            }
        }
    }

    private void addModifiedAssemblyIdsForColorTables(final Set<iPartsAssemblyId> modifiedAssemblyIds) {
        Set<iPartsColorTableDataId> modifiedColorTableIds = new TreeSet<>();
        modifiedColorTableIds.addAll(getColorTableIDsByTable(TABLE_DA_COLORTABLE_DATA, 0));
        modifiedColorTableIds.addAll(getColorTableIDsByTable(TABLE_DA_COLORTABLE_PART, 0));
        modifiedColorTableIds.addAll(getColorTableIDsByTable(TABLE_DA_COLORTABLE_CONTENT, 0));
        modifiedColorTableIds.addAll(getColorTableIDsByTable(TABLE_DA_COLORTABLE_FACTORY, 0));
        if (!modifiedColorTableIds.isEmpty()) {
            // iPartsAssemblyIds für alle Module hinzufügen, deren Baureihe mit der Baureihe der Farbvariantentabelle übereinstimmt
            EtkDataObjectList.FoundAttributesCallback foundAttributesCallback = new EtkDataObjectList.FoundAttributesCallback() {
                @Override
                public boolean foundAttributes(DBDataObjectAttributes attributes) {
                    String sourceGUID = attributes.getFieldValue(FIELD_K_SOURCE_GUID);
                    iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(sourceGUID);
                    if (bctePrimaryKey != null) {
                        String seriesNumber = attributes.getFieldValue(FIELD_DCTD_VALID_SERIES);
                        if (bctePrimaryKey.getHmMSmId().getSeries().equals(seriesNumber)) {
                            modifiedAssemblyIds.add(new iPartsAssemblyId(attributes.getFieldValue(FIELD_K_VARI),
                                                                         attributes.getFieldValue(FIELD_K_VER)));
                        }
                    }
                    return false;
                }
            };

            String[] whereFields = new String[]{ FIELD_DCTP_TABLE_ID };
            EtkDisplayFields selectFields = new EtkDisplayFields();
            selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_VARI, false, false));
            selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_VER, false, false));
            selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_SOURCE_GUID, false, false));
            selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_DATA, FIELD_DCTD_VALID_SERIES, false, false));
            for (iPartsColorTableDataId colorTableId : modifiedColorTableIds) {
                String[] whereValues = new String[]{ colorTableId.getColorTableId() };
                iPartsDataColorTableToPartList dataColorTableToPartList = new iPartsDataColorTableToPartList();
                dataColorTableToPartList.searchSortAndFillWithJoin(project, null, selectFields, whereFields, whereValues, false, null,
                                                                   false, foundAttributesCallback,
                                                                   new EtkDataObjectList.JoinData(TABLE_KATALOG,
                                                                                                  new String[]{ FIELD_DCTP_PART },
                                                                                                  new String[]{ FIELD_K_MATNR },
                                                                                                  false, false),
                                                                   new EtkDataObjectList.JoinData(TABLE_DA_COLORTABLE_DATA,
                                                                                                  new String[]{ FIELD_DCTP_TABLE_ID },
                                                                                                  new String[]{ FIELD_DCTD_TABLE_ID },
                                                                                                  false, false));

            }
        }
    }

    public Set<iPartsAssemblyId> getAssemblyIDsByTable(String tableName, int pkIndexForModuleNumber, boolean includeConstructionAssemblies) {
        Set<iPartsAssemblyId> assemblyIds = new TreeSet<>();
        Collection<SerializedDBDataObject> serializedDataObjects = getSerializedObjectsByTable(tableName);
        if (serializedDataObjects != null) {
            for (SerializedDBDataObject serializedDataObject : serializedDataObjects) {
                if (serializedDataObject.getState().isMustBeSimulatedAndSaved()) {
                    IdWithType id = serializedDataObject.createId();
                    if (id.getIdLength() > pkIndexForModuleNumber) {
                        assemblyIds.add(new iPartsAssemblyId(id.getValue(pkIndexForModuleNumber + 1), "")); // Typ ist an Index 0
                    }

                    // Die dazugehörigen DIALOG-Konstruktions-Stücklisten sind ebenfalls relevant (außer bei nur veränderten
                    // Stücklisteneinträgen, weil sich dadurch nichts in der DIALOG-Konstruktions-Stückliste geändert haben kann)
                    if (includeConstructionAssemblies && tableName.equals(TABLE_KATALOG) && (serializedDataObject.getState() != SerializedDBDataObjectState.MODIFIED)) {
                        iPartsEntrySourceType sourceType = iPartsEntrySourceType.getFromDbValue(serializedDataObject.getAttributeValue(iPartsConst.FIELD_K_SOURCE_TYPE,
                                                                                                                                       true, project));
                        if (sourceType == iPartsEntrySourceType.DIALOG) {
                            String sourceGuid = serializedDataObject.getAttributeValue(iPartsConst.FIELD_K_SOURCE_GUID, true, project);
                            if (StrUtils.isValid(sourceGuid)) {
                                iPartsDialogBCTEPrimaryKey bcteKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(sourceGuid);
                                if (bcteKey != null) {
                                    String virtualIdString = iPartsVirtualNode.getVirtualIdString(bcteKey.getHmMSmId());
                                    assemblyIds.add(new iPartsAssemblyId(virtualIdString, ""));
                                }
                            }
                        }

                    }
                }
            }
        }
        return assemblyIds;
    }

    private Set<iPartsDialogBCTEPrimaryKey> getBCTEKeysForModifiedFactoryData() {
        Set<iPartsDialogBCTEPrimaryKey> bcteKeys = new TreeSet<>();
        for (String primaryKey : getPrimaryKeysByTable(TABLE_DA_FACTORY_DATA, 0)) {
            iPartsDialogBCTEPrimaryKey bcteKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(primaryKey);
            if (bcteKey != null) {
                bcteKeys.add(bcteKey);
            }
        }
        return bcteKeys;
    }

    private Set<iPartsColorTableDataId> getColorTableIDsByTable(String tableName, int pkIndexForColorTableNumber) {
        Set<iPartsColorTableDataId> colorTableIds = new TreeSet<>();
        for (String primaryKey : getPrimaryKeysByTable(tableName, pkIndexForColorTableNumber)) {
            colorTableIds.add(new iPartsColorTableDataId(primaryKey));
        }
        return colorTableIds;
    }

    @Override
    protected iPartsChangeSetEntryId createChangeSetEntryId(iPartsChangeSetId changeSetId, IdWithType dataObjectIdWithType) {
        return new iPartsChangeSetEntryId(changeSetId, dataObjectIdWithType);
    }

    @Override
    public synchronized Collection<SerializedDBDataObject> getSerializedObjectsByTable(String tableName) {
        // Für ChangeSets und ChangeSetEntries niemals eine GetRecords-Simulation machen
        if (tableName.equals(TABLE_DA_CHANGE_SET) || tableName.equals(TABLE_DA_CHANGE_SET_ENTRY)) {
            return null;
        }

        return super.getSerializedObjectsByTable(tableName);
    }

    @Override
    public Collection<SerializedDBDataObject> removeUnmodifiedSerializedDataObjects() {
        Collection<SerializedDBDataObject> unmodifiedSerializedDataObjects = super.removeUnmodifiedSerializedDataObjects();
        if (!unmodifiedSerializedDataObjects.isEmpty()) {
            // Unveränderte ChangeSetEntries auch aus der DB löschen
            EtkDbObjectsLayer dbLayer = project.getDbLayer();
            dbLayer.startTransaction();
            dbLayer.startBatchStatement();
            try {
                for (SerializedDBDataObject unmodifiedSerializedDataObject : unmodifiedSerializedDataObjects) {
                    iPartsChangeSetEntryId changeSetEntryId = new iPartsChangeSetEntryId(getChangeSetId(), unmodifiedSerializedDataObject.createId());
                    if (changeSetEntryId != null) {
                        iPartsDataChangeSetEntry dataChangeSetEntry = new iPartsDataChangeSetEntry(project, changeSetEntryId);
                        dataChangeSetEntry.deleteFromDB(true);
                    }
                }
                dbLayer.endBatchStatement();
                dbLayer.commit();
            } catch (RuntimeException e) {
                dbLayer.cancelBatchStatement();
                dbLayer.rollback();
                throw e;
            }
        }
        return unmodifiedSerializedDataObjects;
    }

    /**
     * Berechnet alle relevanten {@link SerializedDBDataObject}s basierend auf den übergebenen {@link SerializedDBDataObject}s
     * aufgrund von Daten-Zusammenhängen wie z.B. BCTE-Schlüssel.
     *
     * @param serializedDBDataObjects
     * @param undoableDataTypes
     * @return
     */
    public Collection<SerializedDBDataObject> calculateRelevantSerializedDBDataObjects(List<SerializedDBDataObject> serializedDBDataObjects,
                                                                                       Set<String> undoableDataTypes) {
        Set<SerializedDBDataObject> relevantSerializedDBDataObjects = new LinkedHashSet<>();
        if (!Utils.isValid(serializedDBDataObjects)) {
            return relevantSerializedDBDataObjects;
        }

        Collection<SerializedDBDataObject> serializedPLEList = getSerializedObjectsByTable(TABLE_KATALOG);
        Map<PartListEntryId, SerializedDBDataObject> relevantPLESerializedDBDataObjectsMap = new TreeMap<>();
        Set<String> relevantModuleNumbers = new LinkedHashSet<>();
        for (SerializedDBDataObject serializedDBDataObject : serializedDBDataObjects) {
            String type = serializedDBDataObject.getType();
            if (type.equals(PartListEntryId.TYPE)) {
                PartListEntryId partListEntryId = new PartListEntryId(serializedDBDataObject.getPkValues());
                relevantPLESerializedDBDataObjectsMap.put(partListEntryId, serializedDBDataObject);
                relevantModuleNumbers.add(partListEntryId.getKVari());
                addRelevantObjectsWithSameBCTEKeyWithoutAA(serializedDBDataObject, serializedPLEList, relevantPLESerializedDBDataObjectsMap,
                                                           relevantModuleNumbers);
            } else if (undoableDataTypes.contains(type)) {
                // Änderungen an modul-bezogenen Datentypen können nur rückgängig gemacht werden, wenn der Zustand
                // verändert ist
                if ((type.equals(AssemblyId.TYPE) || type.equals(iPartsModuleId.TYPE) || type.equals(iPartsModuleEinPASId.TYPE))
                    && (serializedDBDataObject.getState() != SerializedDBDataObjectState.MODIFIED)) {
                    continue;
                }

                // Relevante Modulnummer bestimmen
                if (type.equals(AssemblyId.TYPE)) {
                    AssemblyId assemblyId = new AssemblyId(serializedDBDataObject.getPkValues());
                    relevantModuleNumbers.add(assemblyId.getKVari());
                } else if (type.equals(iPartsModuleId.TYPE)) {
                    iPartsModuleId moduleId = new iPartsModuleId(serializedDBDataObject.getPkValues());
                    relevantModuleNumbers.add(moduleId.getModuleNumber());
                } else if (type.equals(iPartsModuleEinPASId.TYPE)) {
                    iPartsModuleEinPASId moduleEinPASId = new iPartsModuleEinPASId(serializedDBDataObject.getPkValues());
                    relevantModuleNumbers.add(moduleEinPASId.getModuleNumber());
                }

                // Andere Datentypen direkt zu den relevanten SerializedDBDataObjects hinzufügen
                relevantSerializedDBDataObjects.add(serializedDBDataObject);
            }
        }

        if (!relevantPLESerializedDBDataObjectsMap.isEmpty()) {
            // Alle zusätzlichen relevanten Daten der aufgesammelten relevanten Stücklisteneinträge berücksichtigen
            addOtherRelevantObjectsForPartlistEntries(relevantPLESerializedDBDataObjectsMap, relevantSerializedDBDataObjects);
        }

        if (!relevantModuleNumbers.isEmpty()) {
            Map<IdWithType, SerializedDBDataObject> relevantSerializedDBDataObjectsMap = new LinkedHashMap<>();
            for (SerializedDBDataObject relevantSerializedDBDataObject : relevantSerializedDBDataObjects) {
                relevantSerializedDBDataObjectsMap.put(relevantSerializedDBDataObject.createId(), relevantSerializedDBDataObject);
            }

            // SerializedDBDataObject der Modul-Änderung hinzufügen bzw. entfernen abhängig von echten Änderungen im Modul
            for (String relevantModuleNumber : relevantModuleNumbers) {
                addOrRemoveSerializedModuleForUndo(relevantModuleNumber, relevantSerializedDBDataObjectsMap);
            }

            return relevantSerializedDBDataObjectsMap.values();
        } else {
            return relevantSerializedDBDataObjects;
        }
    }

    private void addOrRemoveSerializedModuleForUndo(String moduleNumber, Map<IdWithType, SerializedDBDataObject> serializedDBDataObjectsMapForUndo) {
        AssemblyId assemblyId = new AssemblyId(moduleNumber, "");
        SerializedDBDataObject serializedAssembly = getSerializedDataObject(assemblyId);
        if (serializedAssembly == null) {
            return;
        }

        // Falls es echte Änderungen an Daten in dem Modul gibt, dann darf das Modul selbst nicht rückgängig gemacht werden.
        // Andernfalls soll das Modul selbst ebenfalls rückgängig gemacht werden.
        if (hasRealModuleChangeForUndo(moduleNumber, serializedAssembly, serializedDBDataObjectsMapForUndo)) {
            serializedDBDataObjectsMapForUndo.remove(serializedAssembly.createId());
        } else {
            serializedDBDataObjectsMapForUndo.put(serializedAssembly.createId(), serializedAssembly);
        }
    }

    private boolean hasRealModuleChangeForUndo(String moduleNumber, SerializedDBDataObject serializedAssembly,
                                               Map<IdWithType, SerializedDBDataObject> serializedDBDataObjectsMapForUndo) {
        // Modul selbst überprüfen
        if (!serializedDBDataObjectsMapForUndo.containsKey(serializedAssembly.createId())) {
            // Modul selbst soll nicht rückgängig gemacht werden -> Echte Änderung am Modul selbst vorhanden?
            if ((serializedAssembly.getAttributes() != null) || (serializedAssembly.getCompositeChildren() != null)) {
                return true;
            }
        }

        // Modul-Metadaten überprüfen
        iPartsModuleId moduleId = new iPartsModuleId(moduleNumber);
        SerializedDBDataObject serializedModule = getSerializedDataObject(moduleId);
        if ((serializedModule != null) && !serializedDBDataObjectsMapForUndo.containsKey(serializedModule.createId())) {
            return true;
        }

        // Modul-Verwendung überprüfen
        Collection<SerializedDBDataObject> serializedModulesEinPASList = getSerializedObjectsByTable(TABLE_DA_MODULES_EINPAS);
        if (serializedModulesEinPASList != null) {
            for (SerializedDBDataObject serializedModulesEinPAS : serializedModulesEinPASList) {
                if (serializedDBDataObjectsMapForUndo.containsKey(serializedModulesEinPAS.createId())) {
                    continue;
                }
                iPartsModuleEinPASId modulesEinPASId = new iPartsModuleEinPASId(serializedModulesEinPAS.getPkValues());
                if (modulesEinPASId.getModuleNumber().equals(moduleNumber)) {
                    return true;
                }
            }
        }

        // Bildaufträge überprüfen
        Collection<SerializedDBDataObject> serializedPicOrderModulesList = getSerializedObjectsByTable(TABLE_DA_PICORDER_MODULES);
        if (serializedPicOrderModulesList != null) {
            for (SerializedDBDataObject serializedPicOrderModules : serializedPicOrderModulesList) {
                if (serializedDBDataObjectsMapForUndo.containsKey(serializedPicOrderModules.createId())) {
                    continue;
                }
                iPartsPicOrderModulesId picOrdermodulesId = new iPartsPicOrderModulesId(serializedPicOrderModules.getPkValues());
                if (picOrdermodulesId.getModuleNo().equals(moduleNumber)) {
                    return true;
                }
            }
        }

        // Stücklisteneinträge überprüfen
        Collection<SerializedDBDataObject> serializedPartListEntriesList = getSerializedObjectsByTable(TABLE_KATALOG);
        if (serializedPartListEntriesList != null) {
            for (SerializedDBDataObject serializedPartListEntry : serializedPartListEntriesList) {
                if (!serializedPartListEntry.getType().equals(PartListEntryId.TYPE) || serializedDBDataObjectsMapForUndo.containsKey(serializedPartListEntry.createId())) {
                    continue;
                }
                PartListEntryId partListEntryId = new PartListEntryId(serializedPartListEntry.getPkValues());
                if (partListEntryId.getKVari().equals(moduleNumber)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void addOtherRelevantObjectsForPartlistEntries(Map<PartListEntryId, SerializedDBDataObject> relevantPLESerializedDBDataObjectsMap,
                                                           Set<SerializedDBDataObject> relevantSerializedDBDataObjects) {
        // Alle zusätzlichen relevanten Daten der aufgesammelten relevanten Stücklisteneinträge berücksichtigen
        Collection<SerializedDBDataObject> serializedCombTextList = getSerializedObjectsByTable(TABLE_DA_COMB_TEXT);
        Collection<SerializedDBDataObject> serializedFootNoteCatalogueRefList = getSerializedObjectsByTable(TABLE_DA_FN_KATALOG_REF);
        Collection<SerializedDBDataObject> serializedReplacePartList = getSerializedObjectsByTable(TABLE_DA_REPLACE_PART);
        Collection<SerializedDBDataObject> serializedFactoryDataList = getSerializedObjectsByTable(TABLE_DA_FACTORY_DATA);
        for (Map.Entry<PartListEntryId, SerializedDBDataObject> relevantPLESerializedDBDataObject : relevantPLESerializedDBDataObjectsMap.entrySet()) {
            PartListEntryId partListEntryId = relevantPLESerializedDBDataObject.getKey();
            SerializedDBDataObject pleSerializedDBDataObject = relevantPLESerializedDBDataObject.getValue();
            relevantSerializedDBDataObjects.add(pleSerializedDBDataObject);

            // Relevante SerializedDBDataObjects für kombinierte Texte hinzufügen
            if (Utils.isValid(serializedCombTextList)) {
                for (SerializedDBDataObject serializedCombText : serializedCombTextList) {
                    iPartsCombTextId combTextId = new iPartsCombTextId(serializedCombText.getPkValues());
                    if (combTextId.getPartListEntryId().equals(partListEntryId)) {
                        relevantSerializedDBDataObjects.add(serializedCombText);
                    }
                }
            }

            // Relevante SerializedDBDataObjects für Fußnoten hinzufügen
            if (Utils.isValid(serializedFootNoteCatalogueRefList)) {
                for (SerializedDBDataObject serializedFootNoteCatalogueRef : serializedFootNoteCatalogueRefList) {
                    iPartsFootNoteCatalogueRefId footNoteCatalogueRefId = new iPartsFootNoteCatalogueRefId(serializedFootNoteCatalogueRef.getPkValues());
                    if (footNoteCatalogueRefId.getPartListEntryId().equals(partListEntryId)) {
                        relevantSerializedDBDataObjects.add(serializedFootNoteCatalogueRef);
                    }
                }
            }

            // Relevante SerializedDBDataObjects für Ersetzungen hinzufügen
            if (Utils.isValid(serializedReplacePartList)) {
                for (SerializedDBDataObject serializedReplacePart : serializedReplacePartList) {
                    iPartsReplacePartId replacePartId = new iPartsReplacePartId(serializedReplacePart.getPkValues());

                    // Vorgänger
                    PartListEntryId predecessorPartListEntryId = replacePartId.getPredecessorPartListEntryId();
                    boolean isRelevant = predecessorPartListEntryId.equals(partListEntryId);

                    if (!isRelevant) {
                        // Nachfolger
                        String replaceLfdNr = serializedReplacePart.getAttributeValue(FIELD_DRP_REPLACE_LFDNR, true, getProject());
                        if (StrUtils.isValid(replaceLfdNr)) {
                            PartListEntryId successorPartListEntryId = new PartListEntryId(predecessorPartListEntryId.getOwnerAssemblyId(), replaceLfdNr);
                            isRelevant = successorPartListEntryId.equals(partListEntryId);
                        }
                    }

                    if (isRelevant) {
                        relevantSerializedDBDataObjects.add(serializedReplacePart);
                    }
                }
            }

            // Relevante SerializedDBDataObjects für Werksdaten hinzufügen
            if (Utils.isValid(serializedFactoryDataList)) {
                String sourceGUID = pleSerializedDBDataObject.getAttributeValue(FIELD_K_SOURCE_GUID, true, getProject());
                for (SerializedDBDataObject serializedFactoryData : serializedFactoryDataList) {
                    iPartsFactoryDataId factoryDataId = new iPartsFactoryDataId(serializedFactoryData.getPkValues());
                    if (factoryDataId.getGuid().equals(sourceGUID)) {
                        relevantSerializedDBDataObjects.add(serializedFactoryData);
                    }
                }
            }
        }
    }

    private void addRelevantObjectsWithSameBCTEKeyWithoutAA(SerializedDBDataObject serializedDBDataObject, Collection<SerializedDBDataObject> serializedPLEList,
                                                            Map<PartListEntryId, SerializedDBDataObject> relevantPLESerializedDBDataObjectsMap,
                                                            Set<String> relevantModuleNumbers) {
        String sourceGUID = serializedDBDataObject.getAttributeValue(FIELD_K_SOURCE_GUID, true, getProject());
        if (StrUtils.isValid(sourceGUID)) {
            // Relevante SerializedDBDataObjects für andere Stücklisteneinträge hinzufügen
            iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(sourceGUID);
            if (bctePrimaryKey != null) {
                // Alle relevanten SerializedDBDataObjects für diesen BCTE-Schlüssel aufsammeln (andere Stücklisteneinträge
                // mit gleichem BCTE-Schlüssel ohne AA)
                for (SerializedDBDataObject serializedPLE : serializedPLEList) {
                    if (!serializedPLE.getType().equals(PartListEntryId.TYPE)) {
                        continue;
                    }
                    String otherSourceGUID = serializedPLE.getAttributeValue(FIELD_K_SOURCE_GUID, true, getProject());
                    if (StrUtils.isValid(otherSourceGUID) && !sourceGUID.equals(otherSourceGUID)) {
                        iPartsDialogBCTEPrimaryKey otherBctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(otherSourceGUID);
                        if ((otherBctePrimaryKey != null) && otherBctePrimaryKey.isTheSameWithoutAA(bctePrimaryKey)) {
                            PartListEntryId otherPLEId = new PartListEntryId(serializedPLE.getPkValues());
                            if (!relevantPLESerializedDBDataObjectsMap.containsKey(otherPLEId)) {
                                relevantPLESerializedDBDataObjectsMap.put(otherPLEId, serializedPLE);
                                relevantModuleNumbers.add(otherPLEId.getKVari());
                            }
                        }
                    }
                }
            }
        }
    }
}