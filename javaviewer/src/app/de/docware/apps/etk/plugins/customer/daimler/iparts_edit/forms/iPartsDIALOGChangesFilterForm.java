/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.*;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDbObjectsLayer;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataReplacePart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsReplacePartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ASUsageHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.OnEditChangeRecordEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.framework.modules.config.db.EtkFieldType;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.table.HtmlTablePageSplitMode;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogButtons;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogIcon;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;
import org.apache.commons.collections4.list.TreeList;

import java.util.*;

/**
 * Formular zur Suche und Anzeige der Änderungsstände (DA_DIALOG_CHANGES), die nicht mehr benötigt werden
 */
public class iPartsDIALOGChangesFilterForm extends SimpleMasterDataSearchFilterGrid {

    private static final int HOW_MUCH_MAX_ROWS_PER_PAGE = 1000;
    private static final String FIELD_DUMMY_DDC_DO_ID = "dummyDdcDoId";
    private static final String ID_DELIMITER = " / ";
    private static final String COLORTABLE_FACTORY_DATA_WITHOUT_RETAIL_USAGE = "colortableFactoryDataWithoutRetailUsage";
    private static final String PARTLISTENTRY_ETKZ_WITHOUT_RETAIL_USAGE = "partlistentryEtkzWithoutRetailUsage";
    private static final List<SpecialSearchToken> SPECIAL_SEARCH_TOKENS =
            Arrays.asList(new SpecialSearchToken(iPartsDataDIALOGChange.ChangeType.COLORTABLE_FACTORY_DATA, COLORTABLE_FACTORY_DATA_WITHOUT_RETAIL_USAGE),
                          new SpecialSearchToken(iPartsDataDIALOGChange.ChangeType.PARTLISTENTRY_ETKZ, PARTLISTENTRY_ETKZ_WITHOUT_RETAIL_USAGE));
    private static boolean checkOnlyASUsage = false; // Kenner ob, nur der Verwendungs-Check gemacht werden soll

    public static void showFilteredDialogChangesData(AbstractJavaViewerForm owner) {
        // Aktive Form holen
        AbstractJavaViewerForm activeForm = owner.getConnector().getActiveForm();
        OnEditChangeRecordEvent onEditChangeRecordEvent = new OnEditChangeRecordEvent() {
            @Override
            public boolean onEditCreateRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName, IdWithType id, DBDataObjectAttributes attributes) {
                return false;
            }

            @Override
            public boolean onEditModifyRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName, IdWithType id, DBDataObjectAttributes attributes) {
                return false;
            }

            @Override
            public boolean onEditAskForDelete(AbstractJavaViewerFormIConnector dataConnector, String tableName, DBDataObjectAttributesList attributeList) {
                String msg = "!!Wollen Sie den selektierten Änderungsstand wirklich löschen?";
                if (attributeList.size() > 1) {
                    msg = "!!Wollen Sie die selektierten Änderungsstände wirklich löschen?";
                }
                return MessageDialog.show(msg, "!!Löschen", MessageDialogIcon.CONFIRMATION,
                                          MessageDialogButtons.YES, MessageDialogButtons.NO) == ModalResult.YES;
            }

            @Override
            public boolean onEditDeleteRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName, DBDataObjectAttributesList attributeList) {
                if ((attributeList != null) && !attributeList.isEmpty()) {
                    EtkProject project = dataConnector.getProject();
                    EtkDbObjectsLayer dbLayer = project.getDbLayer();
                    dbLayer.startTransaction();
                    try {
                        iPartsDataDIALOGChangeList dialogChangeList = new iPartsDataDIALOGChangeList();
                        Set<AssemblyId> assemblyIdsToRemoveFromCacheSet = new HashSet<>();
                        for (DBDataObjectAttributes attributes : attributeList) {
                            iPartsDialogChangesId dataDIALOGChangeId = new iPartsDialogChangesId(attributes.getFieldValue(FIELD_DDC_DO_TYPE),
                                                                                                 attributes.getFieldValue(FIELD_DDC_DO_ID),
                                                                                                 attributes.getFieldValue(FIELD_DDC_HASH));
                            iPartsDataDIALOGChange dataDIALOGChange = new iPartsDataDIALOGChange(project, dataDIALOGChangeId);
                            if (dataDIALOGChange.existsInDB()) {
                                dialogChangeList.add(dataDIALOGChange, DBActionOrigin.FROM_EDIT);
                                // Bei gefundenen Änderungshinweisen bei der Sondersuche von "ET-KZ-Änderung der Teileposition ohne Verwendung"
                                // muss beim Löschen noch der Status der dazugehörenden DIALOG-Teileposition auf freigegeben gesetzt werden
                                if (checkOnlyASUsage &&
                                    dataDIALOGChange.getFieldValue(FIELD_DDC_DO_TYPE).
                                            equals(iPartsDataDIALOGChange.ChangeType.PARTLISTENTRY_ETKZ.getDbKey())) {
                                    String guid = dataDIALOGChange.getFieldValue(FIELD_DDC_BCTE);
                                    iPartsDialogId dialogId = new iPartsDialogId(guid);
                                    iPartsDataDialogData dialogData = new iPartsDataDialogData(project, dialogId);
                                    if (dialogData.existsInDB()) {
                                        String status = dialogData.getFieldValue(FIELD_DD_STATUS);
                                        if (!status.equals(iPartsDataReleaseState.RELEASED.getDbValue())) {
                                            dialogData.setFieldValue(FIELD_DD_STATUS, iPartsDataReleaseState.RELEASED.getDbValue(),
                                                                     DBActionOrigin.FROM_EDIT);
                                            dialogData.saveToDB();
                                            // Betroffene Konstruktionsstückliste aus dem Cache löschen
                                            iPartsDialogBCTEPrimaryKey bcteKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(guid);
                                            if (bcteKey != null) {
                                                String virtualIdString = iPartsVirtualNode.getVirtualIdString(bcteKey.getHmMSmId());
                                                PartListEntryId partListEntryId = new PartListEntryId(virtualIdString, "", bcteKey.createDialogGUID());
                                                AssemblyId constAssemblyId = partListEntryId.getOwnerAssemblyId();
                                                assemblyIdsToRemoveFromCacheSet.add(constAssemblyId);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        dialogChangeList.deleteAll(DBActionOrigin.FROM_EDIT);
                        dialogChangeList.saveToDB(project);
                        dbLayer.commit();

                        for (AssemblyId assemblyIdToRemoveFromCache : assemblyIdsToRemoveFromCacheSet) {
                            EtkDataAssembly.removeDataAssemblyFromCache(project, assemblyIdToRemoveFromCache);
                        }
                        project.fireProjectEvent(new DataChangedEvent());
                        return true;
                    } catch (Exception e) {
                        dbLayer.rollback();
                        Logger.getLogger().handleRuntimeException(e);
                    }
                }
                return false;
            }
        };
        iPartsDIALOGChangesFilterForm dlg = new iPartsDIALOGChangesFilterForm(activeForm.getConnector(), activeForm,
                                                                              iPartsConst.TABLE_DA_DIALOG_CHANGES, onEditChangeRecordEvent);
        EtkProject project = activeForm.getProject();
        //Suchfelder definieren
        EtkDisplayFields searchFields = getSearchFieldsForDialogChangesFilterForm(project);
        //AnzeigeFelder definieren
        EtkDisplayFields displayFields = getDisplayFieldsForDialogChangesFilterForm(project);
        //Sortierung
        LinkedHashMap<String, Boolean> sortFields = new LinkedHashMap<>();
        sortFields.put(FIELD_DDC_DO_ID, false);

        dlg.doResizeWindow(SCREEN_SIZES.MAXIMIZE);
        dlg.setSortFields(sortFields);
        dlg.setSearchFields(searchFields);
        dlg.setDisplayResultFields(displayFields);
        // Vorsichtshabler nochmal einschränken
        // Nur wer die Datenbank-Tools sehen darf, darf auch löschen
        dlg.setEditAllowed(iPartsRight.VIEW_DATABASE_TOOLS.checkRightInSession());
        dlg.setTitle("!!Verwaltungstabelle der Änderungsstände bereinigen");
        dlg.setWindowName("DIALOGChangesFilterForm");
        dlg.showModal();
    }

    public static EtkDisplayFields getSearchFieldsForDialogChangesFilterForm(EtkProject project) {
        EtkDisplayFields etkSearchFields = new EtkDisplayFields();
        etkSearchFields.addFeld(createSearchField(project, TABLE_DA_DIALOG_CHANGES, FIELD_DDC_DO_TYPE, false, false));
        etkSearchFields.addFeld(createSearchField(project, TABLE_DA_DIALOG_CHANGES, FIELD_DDC_SERIES_NO, false, false));
        return etkSearchFields;
    }

    public static EtkDisplayFields getDisplayFieldsForDialogChangesFilterForm(EtkProject project) {
        EtkDisplayFields etkDisplayFields = new EtkDisplayFields();
        addDisplayField(TABLE_DA_DIALOG_CHANGES, FIELD_DDC_DO_TYPE, false, false, null, project, etkDisplayFields);
        EtkDisplayField displayField = addDisplayField(TABLE_DA_DIALOG_CHANGES,
                                                       FIELD_DDC_DO_ID, false, false, null, project, etkDisplayFields);
        displayField.setVisible(false);
        EtkDisplayField dummyDisplayField = addDisplayField(TABLE_DA_DIALOG_CHANGES,
                                                            FIELD_DUMMY_DDC_DO_ID, false, false, null, project, etkDisplayFields);
        dummyDisplayField.setDefaultText(false);
        dummyDisplayField.setText(displayField.getText());
        dummyDisplayField.setColumnFilterEnabled(true);
        addDisplayField(TABLE_DA_DIALOG_CHANGES, FIELD_DDC_HASH, false, false, null, project, etkDisplayFields);
        displayField = addDisplayField(TABLE_DA_DIALOG_CHANGES, FIELD_DDC_SERIES_NO, false, false, null, project, etkDisplayFields);
        displayField.setColumnFilterEnabled(true);
        displayField = addDisplayField(TABLE_DA_DIALOG_CHANGES,
                                       iPartsConst.FIELD_DDC_BCTE, false, false, null, project, etkDisplayFields);
        displayField.setColumnFilterEnabled(true);
        displayField = addDisplayField(TABLE_DA_DIALOG_CHANGES, FIELD_DDC_MATNR, false, false, null, project, etkDisplayFields);
        displayField.setColumnFilterEnabled(true);
        addDisplayField(TABLE_DA_DIALOG_CHANGES, FIELD_DDC_CHANGE_SET_GUID, false, false, null, project, etkDisplayFields);
        return etkDisplayFields;
    }


    private final ASUsageHelper asUsageHelper;
    private boolean hasEntries;

    /**
     * Erzeugt eine Instanz von SimpleMasterDataSearchResultGrid.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     * @param parentForm
     * @param tableName
     * @param onEditChangeRecordEvent
     */
    public iPartsDIALOGChangesFilterForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                         String tableName, OnEditChangeRecordEvent onEditChangeRecordEvent) {
        super(dataConnector, parentForm, tableName, onEditChangeRecordEvent);
        getTable().addEventListener(new EventListener(Event.TABLE_COLUMN_FILTER_CHANGED_EVENT) {
            @Override
            public void fire(Event event) {
                showResultCount();
            }
        });
        DIALOGChangesFilterFactory filterFactory = new DIALOGChangesFilterFactory(getProject());
        setColumnFilterFactory(filterFactory);
        asUsageHelper = new ASUsageHelper(getProject());
        hasEntries = false;
    }

    /**
     * Es muss nur Löschen möglich sein
     *
     * @param editAllowed
     */
    @Override
    public void setEditAllowed(boolean editAllowed) {
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, getTable().getContextMenu());
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW, getTable().getContextMenu());
        if (editAllowed) {
            toolbarHelper.showToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, getTable().getContextMenu());
        } else {
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, getTable().getContextMenu());
        }
    }

    @Override
    protected void modifySearchControl(EtkDisplayField searchField, EditControl ctrl) {
        if (searchField.getKey().getName().equals(TableAndFieldName.make(TABLE_DA_DIALOG_CHANGES, FIELD_DDC_DO_TYPE))) {
            EnumRComboBox changeTypesComboBox = new EnumRComboBox();
            changeTypesComboBox.addToken("", "");
            for (iPartsDataDIALOGChange.ChangeType changeType : iPartsDataDIALOGChange.ChangeType.values()) {
                if (iPartsDataDIALOGChange.ChangeType.isHandledForDelete(changeType)) {
                    changeTypesComboBox.addToken(changeType.getDbKey(), TranslationHandler.translate(changeType.getDisplayKey()));
                }
            }

            for (SpecialSearchToken specialSearchToken : SPECIAL_SEARCH_TOKENS) {
                changeTypesComboBox.addToken(specialSearchToken.getSearchTokenName(),
                                             specialSearchToken.getTokenText());
            }
            // String-Feld durch Combobox mit den Enum-Werten für Änderungstypen ersetzen
            ctrl.getEditControl().setControl(changeTypesComboBox);
        }
    }

    @Override
    protected boolean executeExplicitSearch() {
        // Zurücksetzen für neue Suche
        checkOnlyASUsage = false;
        Map<EtkDisplayField, String> searchFieldsAndValuesForQuery = getSearchFieldsAndValuesForQuery(true, false);
        String[] whereFields = new String[]{};
        String[] whereValues = new String[]{};
        for (Map.Entry<EtkDisplayField, String> entry : searchFieldsAndValuesForQuery.entrySet()) {
            String fieldName = entry.getKey().getKey().getFieldName();
            whereFields = StrUtils.mergeArrays(whereFields, fieldName);
            String value = entry.getValue();
            if (!fieldName.equals(FIELD_DDC_DO_TYPE)) {
                value = value.trim().toUpperCase() + "*";
            }
            // Spezielle Suchanfragen behandeln
            for (SpecialSearchToken specialSearchToken : SPECIAL_SEARCH_TOKENS) {
                if (value.equals(specialSearchToken.getSearchTokenName())) {
                    value = specialSearchToken.getRelevantChangeType().getDbKey();
                    checkOnlyASUsage = true;
                    //Gibt nur einen Token -> Abbrechen
                    break;
                }
            }
            whereValues = StrUtils.mergeArrays(whereValues, value);
        }
        String[] sortFieldArray = sortFields.keySet().toArray(new String[0]);
        iPartsDataDIALOGChangeList dialogChangesList = iPartsDataDIALOGChangeList.loadAndSortForDCType(getProject(), whereFields, whereValues, sortFieldArray);

        // Falls es eine maximale Anzahl der Zeilen in der Anzeige gibt
        // Records mitzählen und abbrechen
        int processedRecords = 0;
        boolean checkSplitMode = getTable().getHtmlTablePageSplitMode() == HtmlTablePageSplitMode.NO_SPLIT;
        for (iPartsDataDIALOGChange dialogChanges : dialogChangesList) {
            FrameworkThread searchThreadLocal = searchThread;
            if ((searchThreadLocal == null) || searchThreadLocal.wasCanceled() || checkMaxResultsExceeded(processedRecords)) {
                return true;
            }
            String doType = dialogChanges.getAsId().getDoType();
            iPartsDataDIALOGChange.ChangeType changeType = iPartsDataDIALOGChange.ChangeType.getChangeType(doType);
            if (!iPartsDataDIALOGChange.ChangeType.isHandledForDelete(changeType)) {
                continue;
            }
            IdWithType doId = IdWithType.fromDBString(changeType.getDbKey(), dialogChanges.getAsId().getDoId());
            if (doId == null) {
                continue;
            }
            boolean isDeletable;
            switch (changeType) {
                case FACTORY_DATA:
                    iPartsFactoryDataId factoryDataId = new iPartsFactoryDataId(doId.toStringArrayWithoutType());
                    iPartsDataFactoryData factoryData = new iPartsDataFactoryData(getProject(), factoryDataId);
                    // Besitzt der Datensatz den Status "NEW" oder "CHECK_DELETION", darf er nicht gelöscht werden
                    isDeletable = !dataObjectHasOneOfTheGivenStates(factoryData, FIELD_DFD_STATUS,
                                                                    iPartsDataReleaseState.NEW, iPartsDataReleaseState.CHECK_DELETION);
                    processedRecords += processRecord(isDeletable, dialogChanges, factoryDataId);
                    break;
                case COLORTABLE_FACTORY_DATA:
                    // Für diesen Datentyp gibt es zwei Token in der Combobox
                    // 1. Status und Verwendung der Teilepos Katalog und in freigegeben Autorenaufträgen prüfen
                    // 2. Verwendung der Teilepos im Katalog prüfen
                    iPartsColorTableFactoryId colorTableFactoryDataId = new iPartsColorTableFactoryId(doId.toStringArrayWithoutType());
                    iPartsDataColorTableFactory colorTableFactoryData = new iPartsDataColorTableFactory(getProject(), colorTableFactoryDataId);
                    if (!checkOnlyASUsage) {
                        // Besitzt der Datensatz den Status "NEW" oder "CHECK_DELETION"
                        isDeletable = !dataObjectHasOneOfTheGivenStates(colorTableFactoryData, FIELD_DCCF_STATUS,
                                                                        iPartsDataReleaseState.NEW, iPartsDataReleaseState.CHECK_DELETION);

                        // Darf der Datensatz nicht gelöscht werden, nochmal kontrollieren, ob die dazugehörige Teilepos im Retail verwendet wird
                        // Ist das nicht der Fall darf gelöscht werden
                        if (!isDeletable) {
                            isDeletable = !asUsageHelper.isMatNrUsedInAS(dialogChanges.getFieldValue(FIELD_DDC_SERIES_NO),
                                                                         dialogChanges.getFieldValue(FIELD_DDC_MATNR));
                        }
                    } else {
                        // Bei COLORTABLE_FACTORY_DATA_WITHOUT_RETAIL_USAGE sollen nur die Verwendungen der
                        // Teilepositionen, die zu den Änderungsständen von den Werkseinsatzdaten zu Varianten(tabelle)
                        // gehören, im Katalog geprüft werden
                        // Falls keine Verwendung vorhanden ist, kann gelöscht werden
                        isDeletable = !asUsageHelper.isMatNrUsedInAsPartList(dialogChanges.getFieldValue(FIELD_DDC_SERIES_NO),
                                                                             dialogChanges.getFieldValue(FIELD_DDC_MATNR));
                    }
                    processedRecords += processRecord(isDeletable, dialogChanges, colorTableFactoryDataId);
                    break;
                case REPLACEMENT_AS:
                    iPartsReplacePartId replacePartId = new iPartsReplacePartId(doId.toStringArrayWithoutType());
                    iPartsDataReplacePart replacePartData = new iPartsDataReplacePart(getProject(), replacePartId);
                    // Besitzt der Datensatz den Status "NEW" oder "CHECK_NOT_RELEVANT"
                    isDeletable = !dataObjectHasOneOfTheGivenStates(replacePartData, FIELD_DRP_STATUS,
                                                                    iPartsDataReleaseState.NEW, iPartsDataReleaseState.CHECK_NOT_RELEVANT);
                    // Darf der Datensatz nicht gelöscht werden, nochmal kontrollieren, ob die dazugehörige Teilepos im Retail verwendet wird
                    // Ist das nicht der Fall darf gelöscht werden
                    if (!isDeletable) {
                        isDeletable = entryIsNotUsedInAS(dialogChanges, asUsageHelper);
                    }
                    processedRecords += processRecord(isDeletable, dialogChanges, replacePartId);
                    break;
                case PARTLISTENTRY_ETKZ:
                    iPartsDialogId dialogId = new iPartsDialogId(doId.toStringArrayWithoutType());
                    iPartsDataDialogData dialogData = new iPartsDataDialogData(getProject(), dialogId);
                    // DD_STATUS wird nur durch die Bestätigung im Edit auf RELEASED gesetzt -> Prüfung kann auf
                    // Status eingeschränkt werden
                    // Achtung!! Hier ist es andersrum, wenn der Datensatz diesen Status besitzt soll der Datensatz
                    // zum Löschen angeboten werden
                    if (!checkOnlyASUsage) {
                        isDeletable = dataObjectHasOneOfTheGivenStates(dialogData, FIELD_DD_STATUS,
                                                                       iPartsDataReleaseState.RELEASED);
                    } else {
                        // Bei PARTLISTENTRY_ETKZ_WITHOUT_RETAIL_USAGE sollen nur die Verwendungen der
                        // Teilepositionen, die zu den Änderungsständen der ETK-Änderung der Teileposition
                        // gehören, im Katalog geprüft werden
                        // Falls keine Verwendung vorhanden ist, kann gelöscht werden
                        iPartsDialogBCTEPrimaryKey bctePrimaryKey =
                                iPartsDialogBCTEPrimaryKey.createFromDialogGuid(dialogChanges.getFieldValue(FIELD_DDC_BCTE));
                        // Falls kein gültiger BCTE vorhanden ist, kann auch gelöscht werden. Da stimmt was nicht
                        isDeletable = true;
                        if (bctePrimaryKey != null) {
                            isDeletable = !asUsageHelper.isUsedInASPartList(bctePrimaryKey);
                        }
                    }
                    processedRecords += processRecord(isDeletable, dialogChanges, dialogId);
                    break;
                case MAT_ETKZ:
                    PartId partId = new PartId(doId.toStringArrayWithoutType());
                    // Gibt es keine Verwendung in einer Retail-Stückliste bzw. in nicht freigegebenen Autorenaufträgen,
                    // dann Datensatz zum Löschen anbieten
                    isDeletable = entryIsNotUsedInAS(dialogChanges, asUsageHelper);
                    processedRecords += processRecord(isDeletable, dialogChanges, partId);
                    break;
            }
            // Bei zu vielen Datensätzen Seiten im Grid einführen
            if (checkSplitMode && (processedRecords >= HOW_MUCH_MAX_ROWS_PER_PAGE)) {
                getTable().setHtmlTablePageSplitMode(HtmlTablePageSplitMode.BUTTONS);
                getTable().setPageSplitNumberOfEntriesPerPage(HOW_MUCH_MAX_ROWS_PER_PAGE);
                checkSplitMode = false;
            }
        }
        return true;
    }

    /**
     * Überprüft, ob der {@link iPartsDialogBCTEPrimaryKey} in einem aktiven ChangeSet oder im Katalog verwendet wird.
     *
     * @param dialogChange
     * @param asUsageHelper
     * @return
     */
    private boolean entryIsNotUsedInAS(iPartsDataDIALOGChange dialogChange, ASUsageHelper asUsageHelper) {
        String bcteKey = dialogChange.getFieldValue(FIELD_DDC_BCTE);
        iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(bcteKey);
        if (bctePrimaryKey != null) {
            return !asUsageHelper.isUsedInAS(bctePrimaryKey);
        }
        return false;
    }

    /**
     * Kontrollieren, ob der Record einen der übergebenen Status besitzt
     *
     * @param object
     * @param statusFieldName
     * @param stateValuesToLookFor
     * @return
     */
    private boolean dataObjectHasOneOfTheGivenStates(EtkDataObject object, String statusFieldName,
                                                     iPartsDataReleaseState... stateValuesToLookFor) {
        if (object.existsInDB()) {
            iPartsDataReleaseState dataObjectStatus = iPartsDataReleaseState.getTypeByDBValue(object.getFieldValue(statusFieldName));
            // Check, ob das Objekt einen der gesuchten Status besitzt
            for (iPartsDataReleaseState statusValue : stateValuesToLookFor) {
                if (dataObjectStatus == statusValue) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Mat-Nr formatieren und Record ins Grid schreiben
     *
     * @param dialogChanges
     * @return
     */
    private int processRecord(boolean isDeletable, iPartsDataDIALOGChange dialogChanges, IdWithType dummyDoId) {
        VarParam<Integer> processedRecordsLocal = new VarParam(0);
        if (isDeletable) {
            String matNo = dialogChanges.getFieldValue(FIELD_DDC_MATNR);
            if (StrUtils.isValid(matNo)) {
                dialogChanges.setFieldValue(FIELD_DDC_MATNR, iPartsNumberHelper.formatPartNo(getProject(), matNo), DBActionOrigin.FROM_DB);
            }

            String value = "";
            if (dummyDoId != null) {
                value = dummyDoId.toString(ID_DELIMITER);
            }
            dialogChanges.getAttributes().addField(FIELD_DUMMY_DDC_DO_ID, value, DBActionOrigin.FROM_DB);

            Session.invokeThreadSafeInSession(() -> processedRecordsLocal.setValue(processResultAttributes(dialogChanges.getAttributes())));
        }
        return processedRecordsLocal.getValue();
    }

    @Override
    protected void doDelete(Event event) {
        endSearch();
        DBDataObjectAttributesList attributeList = getSelectedAttributesList();
        if (onEditChangeRecordEvent != null) {
            if (onEditChangeRecordEvent.onEditAskForDelete(getConnector(), searchTable, attributeList)) {
                if (onEditChangeRecordEvent.onEditDeleteRecordEvent(getConnector(), searchTable, attributeList)) {
                    // Die gelöschten Elemente aus der Selektion rausnehmen. Wichtig, damit z.B die Sichtbarkeit von Kontextmenüeinträgen
                    // nachfolgend nicht mehr für die selektierten Einträge ausgewertet wird. Diese Einträge sind ja gerade
                    // eben gelöscht worden.
                    List<IdWithType> idList = new DwList<>();
                    int[] selectedIndices = getTable().getSelectedRowIndices();
                    getTable().clearSelection();
                    getTable().switchOffEventListeners();
                    try {
                        for (int lfdNr = selectedIndices.length - 1; lfdNr >= 0; lfdNr--) {
                            IdWithType id = buildIdFromAttributes(lfdNr);
                            if (id != null) {
                                idList.add(id);
                            }
                            getTable().removeRow(selectedIndices[lfdNr]);
                        }
                        removeEntries(idList);
                    } finally {
                        getTable().switchOnEventListeners();
                    }
                    showResultCount();
                }
            }
        }
    }

    /**
     * Entfernen der Einträge, falls der Spaltenfilter aktiv ist
     *
     * @param idListe
     * @return
     */
    public boolean removeEntries(List<IdWithType> idListe) {
        if (hasEntries && !idListe.isEmpty()) {
            // Spaltenfilter ist(war) aktiv => entferne die geforderten Zeilen
            List<SimpleSelectSearchResultGrid.GuiTableRowWithAttributes> entries = super.getEntries();
            List<Integer> deleteIndizes = new TreeList();
            for (int lfdNr = 0; lfdNr < entries.size(); lfdNr++) {
                SimpleSelectSearchResultGrid.GuiTableRowWithAttributes row = entries.get(lfdNr);
                IdWithType id = buildIdFromAttributes(row.attributes);
                if ((id != null) && idListe.contains(id)) {
                    deleteIndizes.add(lfdNr);
                }
            }
            if (!deleteIndizes.isEmpty()) {
                for (int lfdNr = deleteIndizes.size() - 1; lfdNr >= 0; lfdNr--) {
                    entries.remove(entries.get(deleteIndizes.get(lfdNr)));
                }
                return true;
            }
        }
        return false;
    }


    @Override
    protected List<SimpleSelectSearchResultGrid.GuiTableRowWithAttributes> getEntries() {
        List<SimpleSelectSearchResultGrid.GuiTableRowWithAttributes> result = super.getEntries();
        hasEntries = (result != null);
        return result;
    }

    @Override
    protected void clearEntries() {
        hasEntries = false;
        super.clearEntries();
    }


    private class DIALOGChangesFilterFactory extends SimpleMasterDataSearchFilterFactory {

        public DIALOGChangesFilterFactory(EtkProject project) {
            super(project);
        }

        @Override
        protected boolean changeColumnTableFilterValues(int column, EditControlFactory editControl) {
            if (editControl.getValues().editCreateMode == EditCreateMode.ecmTableColumnFilter) {
                String fieldName = editControl.getFieldName();
                if (fieldName.equals(FIELD_DDC_SERIES_NO)) {
                    // Trick um im Tabellenfilter ein SetOfEnum Eingabefeld zu erzeugen, dass als Tokens
                    // die Werte aus der zugehörigen Spalte der Tabelle enthält
                    editControl.getValues().field = editControl.getField().cloneMe();  // zur Sicherheit, damit die Originalwerte sich nicht ändern
                    editControl.getValues().field.setType(EtkFieldType.feSetOfEnum);  // behaupte, das Feld ist ein SetOfEnum
                    editControl.getOptions().handleAsSetOfEnum = true;  // und soll als SetOfEnum behandelt werden
                    editControl.getOptions().searchDisjunctive = true;
                    // alles weitere übernimmt EditControlFactory und das FilterInterface
                    AbstractGuiControl guiCtrl = EditControlFactory.doCreateEnumCheckBoxForTableColumnFilter(editControl.getValues(), editControl.getOptions());
                    if (guiCtrl != null) {
                        editControl.setControl(guiCtrl);
                    }
                }
            }
            return super.changeColumnTableFilterValues(column, editControl);
        }

    }

    private static class SpecialSearchToken {

        private final iPartsDataDIALOGChange.ChangeType relevantChangeType;
        private final String searchTokenName;
        private final String tokenText;

        public SpecialSearchToken(iPartsDataDIALOGChange.ChangeType relevantChangeType, String searchTokenName) {
            this.relevantChangeType = relevantChangeType;
            this.searchTokenName = searchTokenName;
            this.tokenText = TranslationHandler.translate("!!%1 ohne Verwendung", TranslationHandler.translate(relevantChangeType.getDisplayKey()));
        }

        public iPartsDataDIALOGChange.ChangeType getRelevantChangeType() {
            return relevantChangeType;
        }

        public String getSearchTokenName() {
            return searchTokenName;
        }

        public String getTokenText() {
            return tokenText;
        }

    }
}
