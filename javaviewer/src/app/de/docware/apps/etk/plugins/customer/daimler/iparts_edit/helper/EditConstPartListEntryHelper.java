/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFieldKeyNormal;
import de.docware.apps.etk.base.config.partlist.EtkEbenenDaten;
import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDbObjectsLayer;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDialogData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDialogDataList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDocuRelevant;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditUserControlForCreateConstPartList;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.config.db.VirtualFieldDefinition;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.GuiWindow;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.SQLStringConvert;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Hilfsklasse zum Anlegen, Editieren und Löschen von Stücklisteneinträgen in der DIALOG-Konstruktion
 */
public class EditConstPartListEntryHelper implements iPartsConst {

    public static final String iPARTS_EDIT_CONSTRUCTION_PARTLISTENTRY_KEY = iPartsEditConfigConst.iPARTS_EDIT_CONFIG_KEY + "/" + "ConstPartListEntry";

    // absolute MUSS-Felder
    private static final String[] MUST_FIELDS = new String[]{ FIELD_DD_SERIES_NO, FIELD_DD_HM, FIELD_DD_M, FIELD_DD_SM,
                                                              FIELD_DD_POSE, FIELD_DD_POSV, FIELD_DD_WW, FIELD_DD_ETZ,
                                                              FIELD_DD_AA, FIELD_DD_SDATA, FIELD_DD_PARTNO, FIELD_DD_QUANTITY };
    // MUSS-Felder, vorbesetzt, aber dennoch änderbar (für Editieren)
    private static final String[] EDIT_DISABLED_FIELDS_FOR_EDIT = new String[]{ FIELD_DD_PARTNO, FIELD_DD_QUANTITY };
    // MUSS-Felder, vorbesetzt, aber dennoch änderbar (für Erzeugen)
    private static final String[] EDIT_DISABLED_FIELDS = { FIELD_DD_POSE, FIELD_DD_POSV, FIELD_DD_AA, FIELD_DD_WW, FIELD_DD_ETZ, FIELD_DD_PARTNO, FIELD_DD_QUANTITY };

    // Felder die wenn sie über die WB konfiguriert sind nie editiert werden dürfen
    private static final String[] READONLY_FIELDS = new String[]{ FIELD_DD_STATUS };

    // unsichtbare Felder
    private static final String[] INVISIBLE_FIELDS = new String[]{ FIELD_DD_GUID/*, FIELD_DD_STATUS */ };
    // MUSS-Felder, die aber leer sein dürfen
    private static final String[] EMPTY_VALUES_ALLOWED_FIELDS = new String[]{ FIELD_DD_WW, FIELD_DD_ETZ };

    // Zusätzliche Felder, die mit geladen werden müssen, weil sie von anderen Felder gebraucht werden
    private static final String[] ADDITIONAL_EDIT_FIELDS = new String[]{ FIELD_DD_PRODUCT_GRP };
//                                                                         FIELD_DD_ETKZ, FIELD_DD_HIERARCHY, FIELD_DD_CODES,
//                                                                         FIELD_DD_STEERING,
//                                                                         FIELD_DD_DOCU_RELEVANT, FIELD_DD_STATUS,
//                                                                         FIELD_DD_KEMA, FIELD_DD_KEMB,
//                                                                         FIELD_DD_EVENT_FROM, FIELD_DD_EVENT_TO };


    private AssemblyListFormIConnector connector;
    private EtkProject project;
    private AssemblyId currentAssemblyId;
    private List<EtkDataPartListEntry> selectedPartListEntries;

    public EditConstPartListEntryHelper(AssemblyListFormIConnector connector) {
        this.connector = connector;
        this.project = connector.getProject();
        this.currentAssemblyId = connector.getCurrentAssembly().getAsId();
        this.selectedPartListEntries = connector.getSelectedPartListEntries();
    }

    public AssemblyListFormIConnector getConnector() {
        return connector;
    }

    public EtkProject getProject() {
        return project;
    }

    public AssemblyId getCurrentAssemblyId() {
        return currentAssemblyId;
    }

    public List<EtkDataPartListEntry> getSelectedPartListEntries() {
        return selectedPartListEntries;
    }

    /**
     * neuen Stücklisteneintrag in der Konstruktion anlegen (iParts-Stücklisteneintrag)
     */
    public void createConstPartListEntry() {
        EditUserControlForCreateConstPartList eCtrl = prepareEditControl(true);
        if (eCtrl != null) {
//                eCtrl.setWindowName(editControlsWindowName);
            if (eCtrl.showModal() == ModalResult.OK) {
                DBDataObjectAttributes attributes = eCtrl.getAttributes();
                iPartsDialogBCTEPrimaryKey bctePrimaryKey = buildIPartsBCTEKeyFromDIALOGAttributes(attributes);
                iPartsDialogId id = new iPartsDialogId(bctePrimaryKey.createDialogGUID());
                attributes.addField(FIELD_DD_GUID, id.getDialogGuid(), DBActionOrigin.FROM_DB);
                attributes.addField(FIELD_DD_POSV, bctePrimaryKey.getPosV(), DBActionOrigin.FROM_DB);
                iPartsDataDialogData dataDialog = new iPartsDataDialogData(getProject(), id);
                if (dataDialog.existsInDB()) {
                    // Fehlermeldung
                    MessageDialog.showError("!!Stücklisteneintrag existiert bereits in der Datenbank.");
                    return;
                }
                dataDialog.assignAttributes(getProject(), attributes, false, DBActionOrigin.FROM_EDIT);

                saveDataAndFireEvents(dataDialog, true);
                // Selektion in der Stückliste neu setzen
                selectEntryInPartList(id);
            }
        }
    }

    /**
     * bestehenden iParts-Stücklisteneintrag editieren bzw anzeigen
     */
    public void editConstPartListEntry() {
        EditUserControlForCreateConstPartList eCtrl = prepareEditControl(false);
        if (eCtrl != null) {
            iPartsDialogId oldDialogId = new iPartsDialogId(getSelectedPartListEntries().get(0).getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GUID));
//                eCtrl.setWindowName(editControlsWindowName);
            if (eCtrl.showModal() == ModalResult.OK) {
                DBDataObjectAttributes attributes = eCtrl.getAttributes();
                iPartsDialogBCTEPrimaryKey bctePrimaryKey = buildIPartsBCTEKeyFromDIALOGAttributes(attributes);
                iPartsDialogId id = new iPartsDialogId(bctePrimaryKey.createDialogGUID());
                iPartsDataDialogData dataDialog = new iPartsDataDialogData(getProject(), id);
                iPartsDataDialogDataList dataList = new iPartsDataDialogDataList();
                if (id.equals(oldDialogId)) {
                    if (!dataDialog.existsInDB()) {
                        // Fehlermeldung
                        MessageDialog.showError("!!Stücklisteneintrag existiert nicht in der Datenbank.");
                        return;
                    }
                } else {
                    // der Primärschlüssel hat sich geändert
                    // ggf Löschen des alten Eintrags
                    iPartsDataDialogData oldDataDialog = new iPartsDataDialogData(getProject(), oldDialogId);
                    if (oldDataDialog.existsInDB()) {
                        dataList.delete(oldDataDialog, true, DBActionOrigin.FROM_EDIT);
                    }
                    // => DD_GUID nachziehen
                    attributes.addField(FIELD_DD_GUID, id.getDialogGuid(), DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                }
                dataDialog.assignAttributes(getProject(), attributes, true, DBActionOrigin.FROM_EDIT);
                dataList.add(dataDialog, DBActionOrigin.FROM_EDIT);

                saveDataAndFireEvents(dataList, false);
                // Selektion in der Stückliste neu setzen
                selectEntryInPartList(id);
            }
        }
    }

    /**
     * selektierte iParts-Stücklisteneinträge löschen
     * (bereits nach AS übernommene Einträge können nicht gelöscht werden)
     */
    public void deleteConstPartListEntry() {
        if (!isRevisionChangeSetActive()) {
            return;
        }

        if ((getSelectedPartListEntries() != null) && !getSelectedPartListEntries().isEmpty()) {
            iPartsDataDialogDataList dataList = new iPartsDataDialogDataList();
            int assignedCount = 0;
            for (EtkDataPartListEntry partListEntry : getSelectedPartListEntries()) {
                if (partListEntry instanceof iPartsDataPartListEntry) {
                    iPartsDataPartListEntry selectedPartListEntry = (iPartsDataPartListEntry)partListEntry;
                    iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(selectedPartListEntry);
                    if ((bctePrimaryKey != null) && bctePrimaryKey.isIPartsCreatedBCTEKey()) {
                        if (selectedPartListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_RETAILUSE).equals(RETAIL_ASSIGNED)) {
                            assignedCount++;
                            continue;
                        }
                        iPartsDialogId id = new iPartsDialogId(bctePrimaryKey.createDialogGUID());
                        iPartsDataDialogData dataDialog = new iPartsDataDialogData(getProject(), id);
                        dataList.delete(dataDialog, true, DBActionOrigin.FROM_EDIT);
                    }
                }
            }

            if (!dataList.getDeletedList().isEmpty()) {
                StringBuilder str = new StringBuilder();
                if (dataList.getDeletedList().size() == 1) {
                    str.append(TranslationHandler.translate("!!Wollen Sie den selektierten Stücklisteneintrag wirklich löschen?"));
                } else {
                    str.append(TranslationHandler.translate("!!Wollen Sie die %1 selektierten Stücklisteneinträge wirklich löschen?",
                                                            String.valueOf(dataList.getDeletedList().size())));
                }
                if (assignedCount > 0) {
                    str.append("\n");
                    str.append("(");
                    if (assignedCount == 1) {
                        str.append(TranslationHandler.translate("!!Ein bereits nach AS übernommener Stücklisteneintrag wird nicht gelöscht"));
                    } else {
                        str.append(TranslationHandler.translate("!!%1 bereits nach AS übernommene Stücklisteneinträge werden nicht gelöscht",
                                                                String.valueOf(assignedCount)));
                    }
                    str.append(")");
                }
                if (ModalResult.NO == MessageDialog.showYesNo(str.toString())) {
                    return;
                }
                saveDataAndFireEvents(dataList, true);
            } else {
                if (assignedCount > 0) {
                    MessageDialog.show("!!Es sind nur bereits nach AS übernommene Stücklisteneinträge selektiert!");
                }
            }
        }
    }

    public void selectEntryInPartList(iPartsDialogId dialogId) {
        // Selektion in der Stückliste neu setzen
        EtkDataPartListEntry partListEntry = getConnector().getCurrentAssembly().getPartListEntryFromKLfdNr(dialogId.getDialogGuid());
        if (partListEntry != null) {
            List<EtkDataPartListEntry> list = new DwList<>();
            list.add(partListEntry);

            getConnector().setSelectedPartListEntries(list);
            getConnector().updateAllViews(getConnector().getActiveForm(), false);
        }
    }

    /**
     * DatenListe speichern (direkt in DB und ChangeSet)
     * Alle Cluster und sich selbst updaten
     *
     * @param dataList
     * @param markParentHmMSmNodesAsModified
     */
    protected void saveDataAndFireEvents(iPartsDataDialogDataList dataList, boolean markParentHmMSmNodesAsModified) {
        GuiWindow.showWaitCursorForRootWindow(true);
        try {
            Set<AssemblyId> modifiedAssemblyIds = saveDataDialogData(dataList, markParentHmMSmNodesAsModified);
            if (modifiedAssemblyIds != null && !modifiedAssemblyIds.isEmpty()) {
                fireClusterEvent(modifiedAssemblyIds);
            }
        } finally {
            GuiWindow.showWaitCursorForRootWindow(false);
        }
    }

    /**
     * Daten speichern (direkt in DB und ChangeSet)
     * Alle Cluster und sich selbst updaten
     *
     * @param dataDialog
     * @param markParentHmMSmNodesAsModified
     */
    protected void saveDataAndFireEvents(iPartsDataDialogData dataDialog, boolean markParentHmMSmNodesAsModified) {
        iPartsDataDialogDataList list = new iPartsDataDialogDataList();
        list.add(dataDialog, DBActionOrigin.FROM_EDIT);
        saveDataAndFireEvents(list, markParentHmMSmNodesAsModified);
    }

    /**
     * DatenListe speichern (direkt in DB und ChangeSet)
     *
     * @param dataList
     * @param markParentHmMSmNodesAsModified
     * @return die modifizierten AssemblyIds
     */
    protected Set<AssemblyId> saveDataDialogData(final iPartsDataDialogDataList dataList, boolean markParentHmMSmNodesAsModified) {
        EtkRevisionsHelper revisionsHelper = getProject().getRevisionsHelper();
        if (revisionsHelper == null) {
            return null;
        }
        AbstractRevisionChangeSet activeChangeSet = revisionsHelper.getActiveRevisionChangeSetForEdit();
        if (activeChangeSet == null) {
            return null;
        }

        if (dataList.isEmpty() && dataList.getDeletedList().isEmpty()) {
            return null;
        }
        // geänderte Werte an alle selektierten PartListEntries übergeben und in Tabelle DA_DIALOG speichern
        GenericEtkDataObjectList genericList = new GenericEtkDataObjectList();
        genericList.addAll(dataList, DBActionOrigin.FROM_EDIT);

        final EtkDbObjectsLayer dbLayer = getProject().getDbLayer();

        // Speichern im ChangeSet muss auch innerhalb der Transaktion stattfinden
        dbLayer.startTransaction();
        dbLayer.startBatchStatement();
        activeChangeSet.addDataObjectListCommitted(genericList);
        final Set<AssemblyId> modifiedAssemblyIds = new HashSet<>();
        // direkt in der Tabelle DA_DIALOG ohne aktive ChangeSets speichern
        project.executeWithoutActiveChangeSets(new Runnable() {
            @Override
            public void run() {
                try {
                    // geänderte BCTE Schlüssel merken
                    Set<iPartsDialogBCTEPrimaryKey> modifiedEntries = extractBCTEKeys(dataList, false);
                    Set<iPartsDialogBCTEPrimaryKey> deletedEntries = extractBCTEKeys(dataList, true);

                    // in der Datenbank löschen
                    dataList.saveToDB(getProject());
                    dbLayer.endBatchStatement();
                    dbLayer.commit();

                    // ggf. den Baureihen Cache modifizieren
                    modifiedAssemblyIds.addAll(modifySeriesCache(modifiedEntries, false, markParentHmMSmNodesAsModified));
                    modifiedAssemblyIds.addAll(modifySeriesCache(deletedEntries, true, markParentHmMSmNodesAsModified));
                } catch (Exception e) {
                    dbLayer.cancelBatchStatement();
                    dbLayer.rollback();
                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                    MessageDialog.showError("!!Fehler beim Speichern.");
                    modifiedAssemblyIds.clear();
                }
            }
        }, false); // fireDataChangedEvent ist hier nicht notwendig, weil in den aufrufenden Methoden ein DataChangedEvent gefeuert wird
        return modifiedAssemblyIds;
    }

    /**
     * DataObject speichern (direkt in DB und ChangeSet)
     *
     * @param dataDialog
     * @param markParentHmMSmNodesAsModified
     * @return
     */
    protected Set<AssemblyId> saveDataDialogData(iPartsDataDialogData dataDialog, boolean markParentHmMSmNodesAsModified) {
        iPartsDataDialogDataList dataList = new iPartsDataDialogDataList();
        dataList.add(dataDialog, DBActionOrigin.FROM_EDIT);
        return saveDataDialogData(dataList, markParentHmMSmNodesAsModified);
    }

    private Set<iPartsDialogBCTEPrimaryKey> extractBCTEKeys(iPartsDataDialogDataList dataList, boolean isDelete) {
        Set<iPartsDialogBCTEPrimaryKey> modifiedBCTEKeys = new HashSet<>();

        List<iPartsDataDialogData> list;
        if (isDelete) {
            list = dataList.getDeletedList();
        } else {
            list = dataList.getAsList();
        }

        for (iPartsDataDialogData dialogData : list) {
            iPartsDialogBCTEPrimaryKey dialogGuid =
                    iPartsDialogBCTEPrimaryKey.createFromDialogGuid(dialogData.getAsId().getDialogGuid());
            if (dialogGuid != null) {
                modifiedBCTEKeys.add(dialogGuid);
            }
        }
        return modifiedBCTEKeys;
    }

    private Set<AssemblyId> modifySeriesCache(Set<iPartsDialogBCTEPrimaryKey> modifiedKeys, boolean isDelete, boolean markParentHmMSmNodesAsModified) {
        Set<AssemblyId> modifiedAssemblyIds = new HashSet<>();
        for (iPartsDialogBCTEPrimaryKey bcteKey : modifiedKeys) {
            HmMSmId targetHmMSmId = bcteKey.getHmMSmId();
            String targetVirtualNodePath = iPartsVirtualNode.getVirtualIdString(targetHmMSmId);
            iPartsAssemblyId targetAssemblyId = new iPartsAssemblyId(targetVirtualNodePath, "");
            modifiedAssemblyIds.add(targetAssemblyId);

            if (markParentHmMSmNodesAsModified) {
                HmMSmId parentId = targetHmMSmId.getParentId();
                while (parentId != null) {
                    modifiedAssemblyIds.add(new iPartsAssemblyId(iPartsVirtualNode.getVirtualIdString(parentId), ""));
                    parentId = parentId.getParentId();
                }
            }

            iPartsDialogSeries series = iPartsDialogSeries.getInstance(getProject(), targetHmMSmId.getSeriesId());
            if (isDelete) {

                // prüfen ob es für diesen HMMSM Knoten noch Stücklisteneinträge gibt
                // dazu wird nicht die komplette Stückliste geladen, weil diese danach sowieso wieder aus dem Cache gelöscht
                // werden würde, und der Overhead für diese simple Prüfung sonst zu groß wäre
                iPartsDataDialogDataList partListUnfiltered = new iPartsDataDialogDataList();
                String[] whereFields = new String[]{ FIELD_DD_SERIES_NO, FIELD_DD_HM, FIELD_DD_M, FIELD_DD_SM };
                String[] whereValues = new String[]{ targetHmMSmId.getSeries(), targetHmMSmId.getHm(), targetHmMSmId.getM(),
                                                     targetHmMSmId.getSm() };
                partListUnfiltered.searchSortAndFill(getProject(), TABLE_DA_DIALOG, whereFields, whereValues, null,
                                                     DBDataObjectList.LoadType.ONLY_IDS, DBActionOrigin.FROM_DB);

                if (partListUnfiltered.isEmpty()) {
                    // den Baureihen Cache löschen nur falls die Stückliste jetzt leer ist
                    series.removeSubModuleIfExists(targetHmMSmId);
                    // im Anschluss wird durch UpdateAllViews die aktuelle Stückliste nochmal im jetzt leeren Zustand geladen
                    // und im Cache abgelegt. Sie wird aber nicht angezeigt, weil der Modulknoten aus der Baureihe gelöscht wurde
                    // Dadurch dass es ein LRUCache ist, stört die leere Stückliste dort nicht. Bei einer erneuten Edit Aktion
                    // wird sie sowieso wieder aus dem Cache entfernt
                }
            } else {
                // für den Fall dass dies der erste Stücklisteneintrag für diese Stückliste ist muss die Liste
                // der gültigen Submodule für diese Baureihe erweitert werden
                series.addSubModuleIfNotExists(targetHmMSmId);
            }
        }
        return modifiedAssemblyIds;
    }

    /**
     * Alle Cluster und sich selbst updaten
     *
     * @param modifiedAssemblyIds
     */
    private void fireClusterEvent(Set<AssemblyId> modifiedAssemblyIds) {
        // Alle modifizierten Assemblies aus dem Cache für das aktive ChangeSet und in allen Cluster-Knoten löschen
        for (AssemblyId modifiedAssemblyId : modifiedAssemblyIds) {
            EtkDataAssembly.removeDataAssemblyFromAllCaches(getProject(), modifiedAssemblyId);
        }

        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PART_LIST,
                                                                                                  iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                                  modifiedAssemblyIds, false));
        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new DataChangedEvent());
    }

    /**
     * Sicherheitsabfrage - Autoren-Auftrag aktiv? und Anzeige
     *
     * @return
     */
    private boolean isRevisionChangeSetActive() {
        if (!getProject().getEtkDbs().isRevisionChangeSetActiveForEdit()) {
            MessageDialog.showWarning("!!Für diese Edit-Aktion muss ein Autoren-Auftrag aktiv sein.");
            return false;
        }
        return true;
    }

    /**
     * EditFelder und InitilaValues laden bzw erzeugen
     * EditUserControl je nach Anforderung erzeugen und initialisieren
     *
     * @param forCreate
     * @return
     */
    protected EditUserControlForCreateConstPartList prepareEditControl(boolean forCreate) {
        if (!isRevisionChangeSetActive()) {
            return null;
        }
        if ((getSelectedPartListEntries() != null) && (getSelectedPartListEntries().size() == 1)) {
            EtkDataPartListEntry partListEntry = getSelectedPartListEntries().get(0);
            if (partListEntry instanceof iPartsDataPartListEntry) {
                iPartsDataPartListEntry selectedPartListEntry = (iPartsDataPartListEntry)partListEntry;
                EtkEditFields editFields = new EtkEditFields();
                editFields.load(getProject().getConfig(), iPARTS_EDIT_CONSTRUCTION_PARTLISTENTRY_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_EDITFIELDS);
                DBDataObjectAttributes initialAttributes = new DBDataObjectAttributes();
                if (editFields.size() == 0) {
                    editFields = getDefaultEditFields(forCreate);
                } else {
                    checkWBConfiguratedFields(editFields, forCreate);
                }
                checkReadOnlyFields(editFields);
                setInitialAttributes(selectedPartListEntry, editFields, initialAttributes, forCreate);
                iPartsDialogId dialogId;
                if (forCreate) {
                    // damit EditUserControl nicht die Werte aus der DB für den selectedPartListEntry lädt
                    iPartsDialogBCTEPrimaryKey bctePrimaryKey = buildIPartsBCTEKeyFromPartListEntry(selectedPartListEntry);
                    String dialogGuid = (bctePrimaryKey != null) ? bctePrimaryKey.createDialogGUID() : "";
                    dialogId = new iPartsDialogId(dialogGuid);
                } else {
                    dialogId = new iPartsDialogId(selectedPartListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GUID));
                }

                EditUserControlForCreateConstPartList eCtrl = new EditUserControlForCreateConstPartList(getConnector(), getConnector().getActiveForm(),
                                                                                                        dialogId, initialAttributes, editFields);
                eCtrl.setIsEmptyValueAllowedFields(EMPTY_VALUES_ALLOWED_FIELDS);
                if (forCreate) {
                    eCtrl.setTitle("!!Neue DIALOG-Position anlegen");
                    eCtrl.modifyAttributesForDirectReturn();
                    eCtrl.setHmMSmEditorEnabled(true);
                } else {
                    boolean isUsedinAS = selectedPartListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_RETAILUSE).equals(RETAIL_ASSIGNED);
                    if (isUsedinAS) {
                        eCtrl.setTitle("!!DIALOG-Position anzeigen");
                        eCtrl.setReadOnly(true);
                    } else {
                        eCtrl.setTitle("!!DIALOG-Position bearbeiten");
                    }
                    eCtrl.setHmMSmEditorEnabled(false);
                }
                return eCtrl;
            }
        }
        return null;
    }

    /**
     * Default EditFelder anlegen
     * Es werden alle Felder des DIALOG-Schlüssels + DD_SDATB, DD_PARTNO und DD_QUANTITY als MUSS-Felder erzeugt
     * Beim Erzeugen dürfen die EDIT_DISABLED_FIELDS trotzdem editiert werden; bei Edit EDIT_DISABLED_FIELDS_FOR_EDIT
     * Die EMPTY_VALUES_ALLOWED_FIELDS MUSS-Felder dürfen leer sein
     *
     * @param forCreate
     * @return
     */
    private EtkEditFields getDefaultEditFields(boolean forCreate) {
        EtkEditFields editFields = new EtkEditFields();
        checkWBConfiguratedFields(editFields, forCreate);

        EtkEbenenDaten ebene = getProject().getConfig().getPartsDescription().getEbene(getConnector().getCurrentAssembly().getEbeneName());
        List<VirtualFieldDefinition> mappingList = iPartsDataVirtualFieldsDefinition.getMapping(TABLE_DA_DIALOG, TABLE_KATALOG);
        EtkDatabaseTable tableDef = getProject().getConfig().getDBDescription().findTable(TABLE_DA_DIALOG);
        // die restlichen Felder werden aus den konfigurierten DisplayFields der aktuellen Tabelle gebildet
        for (EtkDisplayField displayField : ebene.getVisibleFields()) {
            // existiert fieldName im VirtualMapping (berechnete virtuelle Felder entfallen)?
            VirtualFieldDefinition mapping = findVirtualInMapping(displayField.getKey().getFieldName(), mappingList);
            if (mapping != null) {
                // ist fieldName bereits in editFields enthalten?
                if (editFields.getFeldByName(mapping.getSourceTable(), mapping.getSourceFieldName()) == null) {
                    // gehört der fieldName zur Tabelle DA_DIALOG?
                    EtkDatabaseField field = tableDef.getField(mapping.getSourceFieldName());
                    if (field != null) {
                        // als normales Edit-Feld hinzufügen
                        EtkEditField editField = new EtkEditField(mapping.getSourceTable(), mapping.getSourceFieldName(),
                                                                  displayField.isMultiLanguage());
                        editField.setArray(displayField.isArray());
                        editField.setMussFeld(false);
                        editField.setEditierbar(true);
                        editFields.addField(editField);
                    }
                }
            }

        }
        // Sonderbehandlung für DD_PRODUCT_GRP
        if (editFields.getFeldByName(TABLE_DA_DIALOG, FIELD_DD_PRODUCT_GRP) == null) {
            EtkEditField editField = new EtkEditField(TABLE_DA_DIALOG, FIELD_DD_PRODUCT_GRP, false);
            editField.setArray(false);
            editField.setMussFeld(false);
            editField.setEditierbar(false);
            editField.setVisible(false);
            editFields.addField(editField);
        }
        addAdditionalEditFields(editFields, mappingList, tableDef);

        return editFields;
    }

    /**
     * Die konfigurierten Edit-Felder aus der WB überprüfen, ob alle Key-Felder des DIALOG-Schlüssel vorhanden sind,
     * und ggf hinzufühen
     *
     * @param editFields
     * @param forCreate
     */
    private void checkWBConfiguratedFields(EtkEditFields editFields, boolean forCreate) {
        // Felder sind MUSS-Felder, aber diabled, dh vorbesetzt, nicht änderbar
        List<String> mustFields = new DwList<>(MUST_FIELDS);
        // unsichtbare Felder
        List<String> invisibleFields = new DwList<>(INVISIBLE_FIELDS);
        // MUSS-Felder, vorbesetzt, aber dennoch änderbar
        List<String> editDisabledFields;
        if (forCreate) {
            editDisabledFields = new DwList<>(EDIT_DISABLED_FIELDS);
        } else {
            editDisabledFields = new DwList<>(EDIT_DISABLED_FIELDS_FOR_EDIT);
        }

        EtkDatabaseTable tableDef = getProject().getConfig().getDBDescription().findTable(TABLE_DA_DIALOG);
        for (String fieldName : mustFields) {
            EtkEditField editField = editFields.getFeldByName(TABLE_DA_DIALOG, fieldName);
            if (editField == null) {
                EtkDatabaseField field = tableDef.getField(fieldName);
                if (field != null) {
                    editField = new EtkEditField(TABLE_DA_DIALOG, fieldName, field.isMultiLanguage());
                    editField.setArray(field.isArray());
                    editFields.addField(editField);
                } else {
                    continue;
                }
            }
            editField.setEditierbar(editDisabledFields.contains(fieldName));
            editField.setMussFeld(true);
        }
        for (String fieldName : invisibleFields) {
            EtkEditField editField = editFields.getFeldByName(TABLE_DA_DIALOG, fieldName);
            if (editField != null) {
                editField.setEditierbar(false);
            }
        }

        // extra-Felder, die mit geladen werden
        List<VirtualFieldDefinition> mappingList = iPartsDataVirtualFieldsDefinition.getMapping(TABLE_DA_DIALOG, TABLE_KATALOG);
        addAdditionalEditFields(editFields, mappingList, tableDef);
    }

    private static void checkReadOnlyFields(EtkEditFields editFields) {
        for (String fieldName : READONLY_FIELDS) {
            EtkEditField editField = editFields.getFeldByName(TABLE_DA_DIALOG, fieldName);
            if (editField != null) {
                editField.setEditierbar(false);
            }
        }
    }

    /**
     * Für die Vorbesetzung die Werte aus dem selektierten Stücklisteneintrag übernehmen
     * Spezialfälle: DD_SDATA - aktuelles Datum; DD_SDATB leer; POSV Berücksichtigung iParts erzeugter StüLi-Eintrag
     *
     * @param selectedPartListEntry
     * @param editFields
     * @param initialAttributes
     * @param forCreate
     */
    private void setInitialAttributes(iPartsDataPartListEntry selectedPartListEntry,
                                      EtkEditFields editFields, DBDataObjectAttributes initialAttributes, boolean forCreate) {
        EtkDatabaseTable tableDef = getProject().getConfig().getDBDescription().findTable(TABLE_DA_DIALOG);
        List<VirtualFieldDefinition> mappingList = iPartsDataVirtualFieldsDefinition.getMapping(TABLE_DA_DIALOG, TABLE_KATALOG);
        for (VirtualFieldDefinition mapping : mappingList) {
            EtkDatabaseField field = tableDef.getField(mapping.getSourceFieldName());
            if (field != null) {
                EtkEditField editField = editFields.getFeldByName(mapping.getSourceTable(), mapping.getSourceFieldName());
                if (editField != null) {
                    DBDataObjectAttribute attrib = new DBDataObjectAttribute(mapping.getSourceFieldName(), DBDataObjectAttribute.TYPE.STRING, false);
                    attrib.setValueAsString(selectedPartListEntry.getFieldValue(mapping.getVirtualFieldName()), DBActionOrigin.FROM_DB);
                    initialAttributes.addField(attrib, DBActionOrigin.FROM_DB);
                }
                // Diese Felder müssen immer gesetzt werden, auch wenn sie nicht im Edit konfiguriert sind
                if (forCreate) {
                    switch (mapping.getSourceFieldName()) {
                        case FIELD_DD_SDATA:
                            initialAttributes.addField(FIELD_DD_SDATA, SQLStringConvert.calendarToPPDateTimeString(Calendar.getInstance()), DBActionOrigin.FROM_DB);
                            break;
                        case FIELD_DD_SDATB:
                            initialAttributes.addField(FIELD_DD_SDATB, "", DBActionOrigin.FROM_DB);
                            break;
                        case FIELD_DD_POSV:
                            // damit auch von bereits neu angelegten StüLis (haben bereits iPARTS-PosV) neue erzeugt werden können
                            String posV = iPartsDialogBCTEPrimaryKey.normalizeIPartsCreatedPosV(selectedPartListEntry.getFieldValue(mapping.getVirtualFieldName()));
                            initialAttributes.addField(FIELD_DD_POSV, posV, DBActionOrigin.FROM_DB);
                            break;
                        case FIELD_DD_DOCU_RELEVANT:
                            initialAttributes.addField(FIELD_DD_DOCU_RELEVANT, iPartsDocuRelevant.DOCU_RELEVANT_YES.getDbValue(), DBActionOrigin.FROM_DB);
                            break;
                    }
                }
            }
        }
    }

    private void addAdditionalEditFields(EtkEditFields editFields, List<VirtualFieldDefinition> mappingList, EtkDatabaseTable tableDef) {
        // extra-Felder, die mit geladen werden
        List<String> additionalFields = new DwList<>(ADDITIONAL_EDIT_FIELDS);
        for (String fieldName : additionalFields) {
            int indexOfEditField = editFields.getIndexOfTableAndFeldName(new EtkDisplayFieldKeyNormal(TABLE_DA_DIALOG, fieldName));
            VirtualFieldDefinition mapping = findInMapping(fieldName, mappingList);
            if ((indexOfEditField >= 0) || (mapping == null)) {
                continue;
            }
            EtkDatabaseField field = tableDef.getField(mapping.getSourceFieldName());
            if (field != null) {
                EtkEditField editField = new EtkEditField(mapping.getSourceTable(), mapping.getSourceFieldName(),
                                                          field.isMultiLanguage());
                editField.setArray(field.isArray());
                editField.setMussFeld(false);
                editField.setEditierbar(false);
                editField.setVisible(false);
                editFields.addField(editField);
            }
        }
    }

    private static VirtualFieldDefinition findVirtualInMapping(String fieldName, List<VirtualFieldDefinition> mappingList) {
        for (VirtualFieldDefinition mapping : mappingList) {
            if (mapping.getVirtualFieldName().equals(fieldName)) {
                return mapping;
            }
        }
        return null;
    }

    private static VirtualFieldDefinition findInMapping(String fieldName, List<VirtualFieldDefinition> mappingList) {
        for (VirtualFieldDefinition mapping : mappingList) {
            if (mapping.getSourceFieldName().equals(fieldName)) {
                return mapping;
            }
        }
        return null;
    }

    /**
     * iParts BCTE-Key aus Attributen von Tablee DA_DIALOG bilden
     *
     * @param attributes
     * @return
     */
    private iPartsDialogBCTEPrimaryKey buildIPartsBCTEKeyFromDIALOGAttributes(DBDataObjectAttributes attributes) {
        return new iPartsDialogBCTEPrimaryKey(attributes.getField(FIELD_DD_SERIES_NO).getAsString(),
                                              attributes.getField(FIELD_DD_HM).getAsString(),
                                              attributes.getField(FIELD_DD_M).getAsString(),
                                              attributes.getField(FIELD_DD_SM).getAsString(),
                                              attributes.getField(FIELD_DD_POSE).getAsString(),
                                              iPartsDialogBCTEPrimaryKey.makeIPartsCreatedPosV(attributes.getField(FIELD_DD_POSV).getAsString()),
                                              attributes.getField(FIELD_DD_WW).getAsString(),
                                              attributes.getField(FIELD_DD_ETZ).getAsString(),
                                              attributes.getField(FIELD_DD_AA).getAsString(),
                                              attributes.getField(FIELD_DD_SDATA).getAsString());
    }

    /**
     * iParts BCTE-Key aus einem Stücklisten-Eintrag bilden
     *
     * @param partListEntry
     * @return
     */
    private iPartsDialogBCTEPrimaryKey buildIPartsBCTEKeyFromPartListEntry(iPartsDataPartListEntry partListEntry) {
        iPartsDialogBCTEPrimaryKey bcteKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(partListEntry);
        if (bcteKey == null) {
            return null;
        }
        return new iPartsDialogBCTEPrimaryKey(bcteKey.getHmMSmId(), bcteKey.getPosE(),
                                              iPartsDialogBCTEPrimaryKey.makeIPartsCreatedPosV(bcteKey.getPosV()),
                                              bcteKey.getWW(), bcteKey.getET(), bcteKey.getAA(), bcteKey.getSData());
    }
}
