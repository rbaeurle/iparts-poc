package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.db.EtkDbs;
import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.misc.EtkEndpointHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.viewer.JavaViewerApplication;
import de.docware.framework.modules.db.serialization.SerializedDBDataObject;
import de.docware.framework.modules.db.serialization.SerializedDBDataObjectState;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;

import java.util.*;

/**
 * Basisklasse zum Verändern von ChangeSets (speziell von nicht freigegebenen Autoren-Aufträgen).
 */
public class ChangeSetModificator {

    private final LogChannels logChannel;

    public ChangeSetModificator(LogChannels logChannel) {
        this.logChannel = logChannel;
    }

    /**
     * Führt die Änderungen in {@code changeSetModificationTasks} in dem jeweils dazugehörigen {@link iPartsRevisionChangeSet}
     * durch.
     *
     * @param changeSetModificationTasks
     * @param loadSerializedDataObjects
     * @param userName                   Benutzername für das Speichern in den ChangeSets
     */
    public void executeChangesInAllChangeSets(final List<ChangeSetModificationTask> changeSetModificationTasks,
                                              final boolean loadSerializedDataObjects, final String userName) {
        // Tasks pro ChangeSet gruppieren, sodass das ChangeSet später nur einmal aktiviert werden muss, um alle seine Tasks abzuarbeiten.
        final List<iPartsRevisionChangeSet> activeChangeSets = new DwList<>();
        final Map<String, List<ChangeSetModificationTask>> changeSetToChangeTasksMap = new HashMap<>();
        for (ChangeSetModificationTask changeSetModificationTask : changeSetModificationTasks) {
            List<ChangeSetModificationTask> changeSetTasks = changeSetToChangeTasksMap.get(changeSetModificationTask.authorOrderChangeSetId);
            if (changeSetTasks == null) {
                changeSetTasks = new DwList<>();
                changeSetToChangeTasksMap.put(changeSetModificationTask.authorOrderChangeSetId, changeSetTasks);
            }
            changeSetTasks.add(changeSetModificationTask);
        }

        if (!isCancelled() && !changeSetModificationTasks.isEmpty()) {
            logMessage("!!Bearbeite %1 relevante noch nicht freigegebene Autoren-Aufträge...",
                       String.valueOf(changeSetToChangeTasksMap.size()));
        } else {
            return;
        }

        final int maxProgress = changeSetModificationTasks.size();
        onProgressChanged(0, maxProgress);

        Session session = Session.get();
        if (session == null) {
            onCriticalError("!!Fehler beim Simulieren der Änderungen von noch nicht freigegebenen Autoren-Aufträgen: %1",
                            "Session = null");
            return;
        }

        // EtkEndpointHelper.createProject(Session) kann nicht verwendet werden, weil das temporäre EtkProject nicht
        // als EtkProject in der globalen sessionForCalculation gesetzt werden soll
        DWFile dwkFile = (DWFile)session.getAttribute(JavaViewerApplication.SESSION_KEY_DWK_FILE);
        final EtkProject projectForChangeSet = EtkEndpointHelper.createProject(dwkFile, true);
        if (projectForChangeSet == null) {
            onCriticalError("!!Fehler beim Simulieren der Änderungen von noch nicht freigegebenen Autoren-Aufträgen: %1",
                            "EtkProject = null");
            return;
        }

        final Set<String> modifiedAuthorOrderChangeSetIds = new TreeSet<>();

        // Änderungen an den ChangeSets der Autoren-Aufträge müssen in einem eigenen Thread mit separater
        // Transaktion stattfinden, weil es für den aktuellen Thread bereits eine laufende Transaktion gibt
        // mit dem EtkDbs vom Import-EtkProject und projectForChangeSet sein eigenes EtkDbs hat (es würde
        // sonst zu einer Exception in DBDatabase.startTransactionIntern() kommen)
        final FrameworkThread thread = session.startChildThread(new FrameworkRunnable() {
            @Override
            public void run(FrameworkThread thread) {
                // EtkRevisionsHelper muss für Sessions ohne GUI explizit gesetzt werden
                EtkRevisionsHelper revisionsHelper = new iPartsRevisionsHelper();
                EtkDbs etkDbs = projectForChangeSet.getEtkDbs();
                etkDbs.setRevisionsHelper(revisionsHelper);

                Map<iPartsRevisionChangeSet, GenericEtkDataObjectList> changeSetToDataObjectListMap = new HashMap<>();
                int progressCounter = 0;
                for (Map.Entry<String, List<ChangeSetModificationTask>> changeSetWithModificationTasks : changeSetToChangeTasksMap.entrySet()) {
                    if (isCancelled()) {
                        break;
                    }

                    iPartsChangeSetId authorOrderChangeSetId = new iPartsChangeSetId(changeSetWithModificationTasks.getKey());
                    iPartsRevisionChangeSet authorOrderChangeSet = new iPartsRevisionChangeSet(authorOrderChangeSetId,
                                                                                               projectForChangeSet,
                                                                                               loadSerializedDataObjects);
                    authorOrderChangeSet.setExplicitUser(userName);

                    // ChangeSet temporär aktivieren im projectForChangeSet
                    activeChangeSets.clear();
                    activeChangeSets.add(authorOrderChangeSet);
                    revisionsHelper.setActiveRevisionChangeSets(activeChangeSets, authorOrderChangeSet, false,
                                                                projectForChangeSet);

                    GenericEtkDataObjectList dataObjectListForChangeSet = new GenericEtkDataObjectList();

                    List<ChangeSetModificationTask> changeSetModificationTasks = changeSetWithModificationTasks.getValue();
                    for (ChangeSetModificationTask changeSetModificationTask : changeSetModificationTasks) {
                        if (isCancelled()) {
                            break;
                        }

                        changeSetModificationTask.modifyChangeSet(projectForChangeSet, authorOrderChangeSet, dataObjectListForChangeSet);
                        progressCounter++;
                        onProgressChanged(progressCounter, maxProgress);
                    }

                    if (isCancelled()) {
                        break;
                    }

                    // Geänderte Stücklisteneinträge zum ChangeSet des Autoren-Auftrags hinzufügen
                    if (!dataObjectListForChangeSet.isEmpty()) {
                        changeSetToDataObjectListMap.put(authorOrderChangeSet, dataObjectListForChangeSet);
                    }
                }

                if (isCancelled()) {
                    return;
                }

                etkDbs.startTransaction();
                etkDbs.startBatchStatement();

                try {
                    // Erst jetzt eine Transaktion starten und alle Änderungen in allen ChangeSets gesammelt speichern.
                    // Die Transaktion schon vor dem Simulieren der ChangeSets zu starten hätte das Problem, dass
                    // z.B beim Nachladen von bestimmten Feldern eine Pseudo-Transaktion innerhalb einer echten Transaktion
                    // stattfinden würde, was zu einer RuntimeException führt.
                    for (Map.Entry<iPartsRevisionChangeSet, GenericEtkDataObjectList> entry : changeSetToDataObjectListMap.entrySet()) {
                        if (isCancelled()) {
                            break;
                        }

                        iPartsRevisionChangeSet authorOrderChangeSet = entry.getKey();
                        GenericEtkDataObjectList dataObjectListForChangeSet = entry.getValue();
                        List<SerializedDBDataObject> serializedDBDataObjects = authorOrderChangeSet.addDataObjectList(dataObjectListForChangeSet);
                        if ((serializedDBDataObjects != null) && !serializedDBDataObjects.isEmpty()) {
                            modifiedAuthorOrderChangeSetIds.add(authorOrderChangeSet.getChangeSetId().getGUID());
                        }
                    }

                    if (!isCancelled()) {
                        etkDbs.endBatchStatement();
                        etkDbs.commit();
                    } else {
                        etkDbs.cancelBatchStatement();
                        etkDbs.rollback();
                    }
                } catch (Exception e) {
                    onCriticalError("!!Fehler beim Simulieren der Änderungen von noch nicht freigegebenen Autoren-Aufträgen: %1",
                                    Utils.exceptionToString(e));
                    etkDbs.cancelBatchStatement();
                    etkDbs.rollback();
                } finally {
                    // Temporäres EtkProject wieder aufräumen
                    revisionsHelper.clearActiveRevisionChangeSets(projectForChangeSet, false);
                    projectForChangeSet.setDBActive(false, false);
                }
            }
        });
        thread.waitFinished();

        if (!isCancelled()) {
            onProgressChanged(maxProgress, maxProgress);
            if (!modifiedAuthorOrderChangeSetIds.isEmpty()) {
                logMessage("!!Es wurden %1 veränderte noch nicht freigegebene Autoren-Aufträge gespeichert mit den folgenden Änderungsset-IDs: %2",
                           String.valueOf(modifiedAuthorOrderChangeSetIds.size()),
                           "\n" + StrUtils.stringListToString(modifiedAuthorOrderChangeSetIds, ", "));

                // Alle Sessions in allen Cluster-Knoten über die externe Veränderung benachrichtigen
                ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsExternalModifiedChangeSetEvent(modifiedAuthorOrderChangeSetIds));
            } else {
                logMessage("!!Es wurden keine noch nicht freigegebenen Autoren-Aufträge verändert");
            }
        }
    }

    /**
     * Liefert alle Stücklisteneinträge mit dem übergebenen {@code bctePrimaryKey}, die in dem übergebenen {@link iPartsRevisionChangeSet}
     * gelöscht wurden.
     *
     * @param projectForChangeSet
     * @param authorOrderChangeSet
     * @param bctePrimaryKey
     * @return
     */
    public static List<EtkDataPartListEntry> getDeletedASPartListEntriesForBCTEKey(EtkProject projectForChangeSet, iPartsRevisionChangeSet authorOrderChangeSet,
                                                                                   iPartsDialogBCTEPrimaryKey bctePrimaryKey) {
        EnumSet<SerializedDBDataObjectState> validStates = EnumSet.of(SerializedDBDataObjectState.DELETED, SerializedDBDataObjectState.DELETED_COMMITTED);
        return getASPartListEntriesForBCTEKey(projectForChangeSet, authorOrderChangeSet, bctePrimaryKey, validStates);
    }

    /**
     * Liefert alle Stücklisteneinträge mit dem übergebenen {@code bctePrimaryKey}, die in dem übergebenen {@link iPartsRevisionChangeSet}
     * neu angelegt wurden, also alle, die folgenden Status haben:
     * {@link SerializedDBDataObjectState#NEW} oder {@link SerializedDBDataObjectState#REPLACED} (sollte nicht vorkommen,
     * da man die laufende Nummer von Stücklisteneinträgen nicht editiern kann und somit keinen bereits vorhandenen
     * Eintrag mit einem neu angelegten überschreiben kann).
     *
     * @param projectForChangeSet
     * @param authorOrderChangeSet
     * @param bctePrimaryKey
     * @return
     */
    public static List<EtkDataPartListEntry> getNewASPartListEntriesForBCTEKey(EtkProject projectForChangeSet, iPartsRevisionChangeSet authorOrderChangeSet,
                                                                               iPartsDialogBCTEPrimaryKey bctePrimaryKey) {
        EnumSet<SerializedDBDataObjectState> validStates = EnumSet.of(SerializedDBDataObjectState.NEW, SerializedDBDataObjectState.REPLACED);
        List<EtkDataPartListEntry> partListEntries = getASPartListEntriesForBCTEKey(projectForChangeSet, authorOrderChangeSet, bctePrimaryKey, validStates);
        List<EtkDataPartListEntry> result = new DwList<>();
        for (EtkDataPartListEntry partListEntry : partListEntries) {
            if (partListEntry.existsInDB()) { // Hier wird das ChangeSet vom Autoren-Auftrag simuliert
                result.add(partListEntry);
            }
        }
        return result;
    }

    /**
     * Liefert alle Stücklisteneinträge mit dem übergebenen {@code bctePrimaryKey}, die in dem übergebenen {@link iPartsRevisionChangeSet}
     * neu angelegt oder verändert wurden.
     *
     * @param projectForChangeSet
     * @param authorOrderChangeSet
     * @param bctePrimaryKey
     * @return
     */
    public static List<EtkDataPartListEntry> getNonDeletedASPartListEntriesForBCTEKey(EtkProject projectForChangeSet, iPartsRevisionChangeSet authorOrderChangeSet,
                                                                                      iPartsDialogBCTEPrimaryKey bctePrimaryKey) {
        EnumSet<SerializedDBDataObjectState> validStates = EnumSet.allOf(SerializedDBDataObjectState.class);
        validStates.remove(SerializedDBDataObjectState.DELETED);
        validStates.remove(SerializedDBDataObjectState.DELETED_COMMITTED);
        List<EtkDataPartListEntry> partListEntries = getASPartListEntriesForBCTEKey(projectForChangeSet, authorOrderChangeSet,
                                                                                    bctePrimaryKey, validStates);
        List<EtkDataPartListEntry> result = new DwList<>();
        for (EtkDataPartListEntry partListEntry : partListEntries) {
            if (partListEntry.existsInDB()) { // Hier wird das ChangeSet vom Autoren-Auftrag simuliert
                result.add(partListEntry);
            }
        }
        return result;
    }

    private static List<EtkDataPartListEntry> getASPartListEntriesForBCTEKey(EtkProject projectForChangeSet, iPartsRevisionChangeSet authorOrderChangeSet,
                                                                             iPartsDialogBCTEPrimaryKey bctePrimaryKey, Set<SerializedDBDataObjectState> validStates) {
        List<EtkDataPartListEntry> partListEntries = new DwList<>();
        if (bctePrimaryKey == null) {
            return partListEntries;
        }

        // Nur die serialisierten Stücklisteneinträge über den BCTE-Schlüssel ins ChangeSet laden
        Collection<SerializedDBDataObject> serializedDBDataObjects = authorOrderChangeSet.loadSerializedDataObjectsBySourceGUID(bctePrimaryKey.createDialogGUID(),
                                                                                                                                PartListEntryId.TYPE);
        if (serializedDBDataObjects == null) {
            return partListEntries;
        }

        for (SerializedDBDataObject serializedDBDataObject : serializedDBDataObjects) {
            // Zustand des Stücklisteneintrags muss passen
            if (validStates.contains(serializedDBDataObject.getState())) {
                PartListEntryId partListEntryId = new PartListEntryId(serializedDBDataObject.getPkValues());
                EtkDataPartListEntry partListEntry = EtkDataObjectFactory.createDataPartListEntry(projectForChangeSet,
                                                                                                  partListEntryId);
                partListEntries.add(partListEntry);
            }
        }
        return partListEntries;
    }

    public void logMessage(String message, String... placeHolderTexts) {
        String translation = TranslationHandler.getUiTranslationHandler().getTextForLanguage(message, iPartsConst.LOG_FILES_LANGUAGE, placeHolderTexts);
        Logger.log(logChannel, LogType.DEBUG, translation);
    }

    public void onProgressChanged(int progressCounter, int maxProgress) {
        // Basisklasse hat keine GUI die den Progress anzeigen könnte.
    }

    public void onCriticalError(String message, String... placeHolderTexts) {
        String translation = TranslationHandler.getUiTranslationHandler().getTextForLanguage(message, iPartsConst.LOG_FILES_LANGUAGE, placeHolderTexts);
        Logger.log(logChannel, LogType.ERROR, translation);
    }

    public boolean isCancelled() {
        // in der Basisklasse kann die Berechnung nicht abgebrochen werden.
        return false;
    }

    /**
     * Interface für einen Callback, der definiert, was in dem ChangeSet mit der übergebenen ChangeSet-ID passieren soll.
     * Alle dem Helper übergebenen Tasks werden dann pro ChangeSet ausgeführt und alle vorgenommenen Änderungen
     * am Ende gesammelt gespeichert.
     */
    public abstract static class ChangeSetModificationTask {

        private String authorOrderChangeSetId;

        public ChangeSetModificationTask(String authorOrderChangeSetId) {
            this.authorOrderChangeSetId = authorOrderChangeSetId;
        }

        /**
         * Definiert welche Änderungen in dem ChangeSet dieses Tasks gemacht werden. Änderungen dürfen hier NICHT
         * direkt in die Datenbank geschrieben werden, sondern müssen in der übergebenen dataObjectListForChangeSet
         * abgelegt werden. Diese werden dann in einer Transaktion gespeichert. Würde man Änderungen direkt speichern
         * würden sie nicht transaktionssicher sofort in der Datenbank landen.
         *
         * @param projectForChangeSet        Projekt, das während der Simulation des ChangeSets verwendet wird.
         * @param authorOrderChangeSet       Das geladene ChangeSet mit der {@link #authorOrderChangeSetId}
         * @param dataObjectListForChangeSet DataObject-Liste, in die die Änderungen gelegt werden müssen.
         */
        public abstract void modifyChangeSet(EtkProject projectForChangeSet, iPartsRevisionChangeSet authorOrderChangeSet,
                                             GenericEtkDataObjectList dataObjectListForChangeSet);
    }
}
