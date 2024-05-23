package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.EtkConfig;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.OnEditChangeRecordEvent;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * Formular für die Anzeige der Varianten (Tabelle DA_COLORTABLE_PART).
 */
public class MasterDataVariantsForm extends SimpleMasterDataSearchFilterGrid {

    /**
     * Zeigt Baureihenstammdaten an. Wenn ein Baureihenknoten im Navigationsbaum selektiert ist, wird er als
     * erster Treffer in der Stammdatenliste angezeigt. Falls keiner selektiert ist, wird eine leere Stammdatenliste
     * angezeigt.
     *
     * @param owner
     */
    public static void showVariantsMasterData(AbstractJavaViewerForm owner) {
        // Aktive Form holen
        AbstractJavaViewerForm activeForm = owner.getConnector().getActiveForm();
//        iPartsSeriesId seriesId = null;
//
//        // Produkt?
//        iPartsProductId productId = new iPartsProductId(getIdFromTreeSelectionForType(activeForm, iPartsProductId.TYPE));
//        if (productId.isValidId()) {
//            EtkProject project = owner.getConnector().getProject();
//            iPartsProduct product = iPartsProduct.getInstance(project, productId);
//            seriesId = product.getReferencedSeries();
//        }
//
//        if (seriesId == null) {
//            // Baureihe?
//            seriesId = new iPartsSeriesId(getIdFromTreeSelectionForType(activeForm, iPartsSeriesId.TYPE));
//            if (!seriesId.isValidId()) {
//                seriesId = null;
//            }
//        }

        showVariantsMasterData(activeForm.getConnector(), activeForm, null);
    }

    /**
     * Anzeige der Baureihen Tabelle (DA_Series)
     *
     * @param dataConnector
     * @param parentForm
     * @param onEditChangeRecordEvent
     */
    public static void showVariantsMasterData(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                              OnEditChangeRecordEvent onEditChangeRecordEvent) {
        if (onEditChangeRecordEvent == null) {
            // Falls nicht extern definiert: Hier die Callbacks für Edit, Modify und Delete definieren
            onEditChangeRecordEvent = new OnEditChangeRecordEvent() {
                private boolean onEditCreateOrModifyRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName,
                                                                IdWithType id, DBDataObjectAttributes attributes, boolean createRecord) {
//                    iPartsDataSeries dataSeries = getDataSeriesFromSOPField();
//                    if (dataSeries == null) {
//                        iPartsSeriesId seriesId = new iPartsSeriesId(id.getValue(1));
//                        dataSeries = new iPartsDataSeries(dataConnector.getProject(), seriesId);
//                        if (dataSeries.loadFromDB(seriesId) && createRecord) {
//                            String msg = "!!Die Baureihe ist bereits vorhanden und kann nicht neu angelegt werden!";
//                            MessageDialog.show(msg, "!!Erzeugen", MessageDialogIcon.ERROR, MessageDialogButtons.OK);
//                            return true;
//                        }
//                        if (createRecord) {
//                            dataSeries.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
//                        }
//                    } else {
//                        if (createRecord) {
//                            // dataSeries ist bereits vorbesetzt => extra Kontrolle
//                            iPartsDataSeries testDataSeries = new iPartsDataSeries(dataConnector.getProject(), dataSeries.getAsId());
//                            if (testDataSeries.existsInDB()) {
//                                String msg = "!!Die Baureihe ist bereits vorhanden und kann nicht neu angelegt werden!";
//                                MessageDialog.show(msg, "!!Erzeugen", MessageDialogIcon.ERROR, MessageDialogButtons.OK);
//                                return true;
//                            }
//
//                        }
//                    }
//
//                    dataSeries.assignAttributesValues(dataConnector.getProject(), attributes, true, DBActionOrigin.FROM_EDIT);
//
//                    dataConnector.getProject().getDbLayer().startTransaction();
//                    try {
//                        if (iPartsRevisionChangeSet.saveDataObjectWithChangeSet(dataConnector.getProject(), dataSeries, iPartsChangeSetSource.SERIES)) {
//                            dataSeries.saveToDB();
//                            dataConnector.getProject().getDbLayer().commit();
//                            iPartsDataChangedEventByEdit.Action action = createRecord ? iPartsDataChangedEventByEdit.Action.NEW : iPartsDataChangedEventByEdit.Action.MODIFIED;
//                            ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<iPartsSeriesId>(iPartsDataChangedEventByEdit.DataType.SERIES,
//                                                                                                                                    action, dataSeries.getAsId(), false));
//                            return true;
//                        } else {
//                            dataConnector.getProject().getDbLayer().rollback();
//                        }
//                    } catch (Exception e) {
//                        dataConnector.getProject().getDbLayer().rollback();
//                        Logger.getLogger().handleRuntimeException(e);
//                    }
                    return false;
                }

                @Override
                public boolean onEditCreateRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName,
                                                       IdWithType id, DBDataObjectAttributes attributes) {
                    return onEditCreateOrModifyRecordEvent(dataConnector, tableName, id, attributes, true);
                }

                @Override
                public boolean onEditModifyRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName,
                                                       IdWithType id, DBDataObjectAttributes attributes) {
                    return onEditCreateOrModifyRecordEvent(dataConnector, tableName, id, attributes, false);
                }

                @Override
                public boolean onEditAskForDelete(AbstractJavaViewerFormIConnector dataConnector, String tableName, DBDataObjectAttributesList attributeList) {
//                    if ((attributeList != null) && !attributeList.isEmpty()) {
//                        List<String> noDelList = new DwList<String>();
//                        for (int lfdNr = attributeList.size() - 1; lfdNr >= 0; lfdNr--) {
//                            DBDataObjectAttributes attributes = attributeList.get(lfdNr);
//                            iPartsSeriesId seriesId = new iPartsSeriesId(attributes.getField(FIELD_DS_SERIES_NO).getAsString());
//                            iPartsDataSeries dataSeries = new iPartsDataSeries(dataConnector.getProject(), seriesId);
//                            if (dataSeries.hasDependencies()) {
//                                noDelList.add(seriesId.getSeriesNumber());
//                                attributeList.remove(lfdNr);
//                            }
//                        }
//                        if (noDelList.size() > 0) {
//                            String msg = "!!Die selektierten Baureihen werden noch in mindestens einem Produkt verwendet und können nicht gelöscht werden!";
//                            if (noDelList.size() == 1) {
//                                msg = "!!Die selektierte Baureihe wird noch in mindestens einem Produkt verwendet und kann nicht gelöscht werden!";
//                            }
//                            MessageDialog.show(msg, "!!Löschen", MessageDialogIcon.WARNING, MessageDialogButtons.OK);
//
//                        } else {
//                            String msg = "!!Wollen Sie die selektierte Baureihe inkl. aller dazugehörigen Baumuster (Konstruktion und After-Sales) sowie weiterer Referenzen wirklich löschen?";
//                            if (attributeList.size() > 1) {
//                                msg = "!!Wollen Sie die selektierten Baureihen inkl. aller dazugehörigen Baumuster (Konstruktion und After-Sales) sowie weiterer Referenzen wirklich löschen?";
//                            }
//                            if (MessageDialog.show(msg, "!!Löschen", MessageDialogIcon.CONFIRMATION,
//                                                   MessageDialogButtons.YES, MessageDialogButtons.NO) == ModalResult.YES) {
//                                return true;
//                            }
//                        }
//                    }
                    return false;
                }

                @Override
                public boolean onEditDeleteRecordEvent(final AbstractJavaViewerFormIConnector dataConnector, String tableName,
                                                       final DBDataObjectAttributesList attributesList) {
                    final VarParam<Boolean> success = new VarParam<Boolean>(false);
                    if ((attributesList != null) && !attributesList.isEmpty()) {
//                        final EtkMessageLogForm messageLogForm = new EtkMessageLogForm("!!Löschen", "!!Baureihe löschen inkl. aller dazugehörigen Baumuster (Konstruktion und After-Sales) sowie weiterer Referenzen",
//                                                                                       DefaultImages.delete.getImage());
//                        messageLogForm.showModal(new FrameworkRunnable() {
//                            @Override
//                            public void run(FrameworkThread thread) {
//                                EtkDbObjectsLayer dbLayer = dataConnector.getProject().getDbLayer();
//                                dbLayer.startTransaction();
//                                dbLayer.startBatchStatement();
//                                try {
//                                    boolean deleteOK = true;
//                                    List<iPartsSeriesId> seriesIds = new ArrayList<iPartsSeriesId>(attributesList.size());
//                                    Set<iPartsModelId> modelIds = new HashSet<iPartsModelId>(attributesList.size() * 10);
//                                    for (DBDataObjectAttributes attributes : attributesList) {
//                                        iPartsSeriesId seriesId = new iPartsSeriesId(attributes.getField(FIELD_DS_SERIES_NO).getAsString());
//                                        messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Baureihe %1 wird gelöscht",
//                                                                                                                seriesId.getSeriesNumber()),
//                                                                                   MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
//
//                                        // After-Sales-Baumuster von der Baureihe bestimmen für späteren Event
//                                        iPartsDataModelList modelList = iPartsDataModelList.loadDataModelList(dataConnector.getProject(),
//                                                                                                              seriesId.getSeriesNumber(),
//                                                                                                              DBDataObjectList.LoadType.ONLY_IDS);
//                                        for (iPartsDataModel dataModel : modelList) {
//                                            modelIds.add(dataModel.getAsId());
//                                        }
//
//                                        iPartsDataSeries dataSeries = new iPartsDataSeries(dataConnector.getProject(), seriesId);
//                                        dataSeries.loadChildren();
//                                        if (iPartsRevisionChangeSet.deleteDataObjectWithChangeSet(dataConnector.getProject(), dataSeries, iPartsChangeSetSource.SERIES)) {
//                                            if (!dataSeries.deleteFromDBWithModels(true, messageLogForm.getMessageLog())) {
//                                                deleteOK = false;
//                                                break;
//                                            }
//                                        } else {
//                                            deleteOK = false;
//                                            break;
//                                        }
//                                        seriesIds.add(seriesId);
//                                    }
//
//                                    if (deleteOK) {
//                                        dbLayer.endBatchStatement();
//                                        dbLayer.commit();
//
//                                        messageLogForm.getMessageLog().fireMessage("!!Löschen abgeschlossen", MessageLogType.tmlMessage,
//                                                                                   MessageLogOption.TIME_STAMP);
//
//                                        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<iPartsSeriesId>(iPartsDataChangedEventByEdit.DataType.SERIES,
//                                                                                                                                                iPartsDataChangedEventByEdit.Action.DELETED,
//                                                                                                                                                seriesIds, false));
//                                        if (!modelIds.isEmpty()) {
//                                            ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<iPartsModelId>(iPartsDataChangedEventByEdit.DataType.MODEL,
//                                                                                                                                                   iPartsDataChangedEventByEdit.Action.DELETED,
//                                                                                                                                                   modelIds, false));
//                                        }
//                                        success.setValue(true);
//                                    } else {
//                                        dbLayer.cancelBatchStatement();
//                                        dbLayer.rollback();
//                                        success.setValue(false);
//                                    }
//                                } catch (Exception e) {
//                                    dbLayer.cancelBatchStatement();
//                                    dbLayer.rollback();
//                                    messageLogForm.getMessageLog().fireMessage(e.getMessage(), MessageLogType.tmlError);
//                                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
//                                }
//                            }
//                        });
                    }
                    return success.getValue();
                }
            };
        }

        MasterDataVariantsForm dlg = new MasterDataVariantsForm(dataConnector, parentForm, TABLE_DA_COLORTABLE_PART, onEditChangeRecordEvent);

        // Suchfelder definieren
        EtkDisplayFields searchFields = new EtkDisplayFields();
        EtkProject project = dataConnector.getProject();
        EtkConfig config = dataConnector.getConfig();
        searchFields.load(config, iPartsEditConfigConst.iPARTS_EDIT_MASTER_VARIANTS_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_SEARCHFIELDS,
                          config.getCurrentDatabaseLanguage());
        if (searchFields.size() == 0) {
            // Suchfelder definieren
            searchFields.addFeld(createSearchField(project, TABLE_DA_COLORTABLE_PART, FIELD_DCTP_TABLE_ID, false, false));
//            searchFields.addFeld(createSearchField(dataConnector.getProject(), TABLE_DA_SERIES, FIELD_DS_TYPE, false, false));
        }

        // Anzeigefelder definieren
        EtkDisplayFields displayFields = new EtkDisplayFields();
        displayFields.load(config, iPartsEditConfigConst.iPARTS_EDIT_MASTER_VARIANTS_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS);
        if (displayFields.size() == 0) {
            EtkDisplayField displayField = addDisplayField(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_TABLE_ID, false, false, null, project, displayFields);
            displayField.setColumnFilterEnabled(true);
            displayField = addDisplayField(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_SDATA, false, false, null, project, displayFields);
            //displayField.setColumnFilterEnabled(true);
            addDisplayField(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_SDATB, false, false, null, project, displayFields);
            addDisplayField(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_STATUS, false, false, null, project, displayFields);
        }

        // Editfelder fürs Editieren festlegen
        EtkEditFields editFields = new EtkEditFields();
        editFields.load(config, iPartsEditConfigConst.iPARTS_EDIT_MASTER_VARIANTS_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_EDITFIELDS);
        if (editFields.size() == 0) {
            editFields.addFeld(createEditField(project, TABLE_DA_COLORTABLE_PART, FIELD_DCTP_TABLE_ID, false));
            editFields.addFeld(createEditField(project, TABLE_DA_COLORTABLE_PART, FIELD_DCTP_SDATA, false));
            editFields.addFeld(createEditField(project, TABLE_DA_COLORTABLE_PART, FIELD_DCTP_SDATB, false));
            editFields.addFeld(createEditField(project, TABLE_DA_COLORTABLE_PART, FIELD_DCTP_STATUS, false));
        }
        EtkDatabaseTable tableDef = config.getDBDescription().findTable(TABLE_DA_COLORTABLE_PART);
        List<String> pkFields = tableDef.getPrimaryKeyFields();
        for (EtkEditField eField : editFields.getFields()) {
            if (pkFields.contains(eField.getKey().getFieldName())) {
                eField.setMussFeld(true);
                eField.setEditierbar(false);  // ist PK-Value => nicht editierbar
            }
        }

        // Sortierung
        LinkedHashMap<String, Boolean> sortFields = new LinkedHashMap<>();
        sortFields.put(FIELD_DCTP_TABLE_ID, false);
        dlg.setSortFields(sortFields);

        dlg.setDisplayResultFields(displayFields);
        dlg.setSearchFields(searchFields);
        dlg.setEditFields(editFields);
        boolean editMasterDataAllowed = iPartsRight.EDIT_MASTER_DATA.checkRightInSession();
        boolean deleteMasterDataAllowed = iPartsRight.DELETE_MASTER_DATA.checkRightInSession();
        dlg.setEditAllowed(editMasterDataAllowed || deleteMasterDataAllowed);
        dlg.setNewAllowed(editMasterDataAllowed);
        dlg.setModifyAllowed(editMasterDataAllowed);
        dlg.setDeleteAllowed(deleteMasterDataAllowed);
        dlg.setTitlePrefix("!!Variant");
        dlg.setWindowName("VariantsMasterData");
        dlg.setMaxResults(J2EEHandler.isJ2EE() ? MAX_SELECT_SEARCH_RESULTS_SIZE : -1);


        dlg.showModal();
    }

    private Set<String> foundColorTableIds;

    /**
     * Erzeugt eine Instanz von SimpleMasterDataSearchResultGrid.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     * @param parentForm
     * @param tableName
     * @param onEditChangeRecordEvent
     */
    public MasterDataVariantsForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName, OnEditChangeRecordEvent onEditChangeRecordEvent) {
        super(dataConnector, parentForm, tableName, onEditChangeRecordEvent);
        doResizeWindow(SCREEN_SIZES.MAXIMIZE);
    }

    @Override
    protected void addToolbarButtonsAndContextMenuItems(EditToolbarButtonMenuHelper toolbarHelper, GuiContextMenu
            contextMenu) {
        super.addToolbarButtonsAndContextMenuItems(toolbarHelper, contextMenu);
    }

    @Override
    protected synchronized void startSearch() {
        super.startSearch();
        foundColorTableIds = new HashSet<>();
    }

    @Override
    protected boolean useMaxResultsForSQLHitLimit() {
        return false; // Aufgrund der Filterung in doValidateAttributes()
    }

    @Override
    protected boolean doValidateAttributes(DBDataObjectAttributes attributes) {
        String colorTableId = attributes.getFieldValue(FIELD_DCTP_TABLE_ID);
        String sDatB = attributes.getFieldValue(FIELD_DCTP_SDATB);
        if (StrUtils.isValid(colorTableId) && StrUtils.isEmpty(sDatB)) {
            if (!foundColorTableIds.contains(colorTableId)) {
                foundColorTableIds.add(colorTableId);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void enableButtons() {
        super.enableButtons();
    }

    @Override
    protected void doNew(Event event) {
        super.doNew(event);
    }

    @Override
    protected void doEditOrView(Event event) {
        super.doEditOrView(event);
    }


}

