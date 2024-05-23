/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDbObjectsLayer;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetSource;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.iPartsMainImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.OnEditChangeRecordEvent;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.*;
import de.docware.framework.modules.gui.controls.GuiSeparator;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogButtons;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogIcon;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;

import java.util.*;

/**
 * Formular für die Anzeige der Stammdaten von Baureihen (Tabelle DA_SERIES).
 */
public class MasterDataSeriesForm extends SimpleMasterDataSearchFilterGrid {

    public static final String FIELD_DS_SOP = "DS_SOP";

    /**
     * Zeigt Baureihenstammdaten an. Wenn ein Baureihenknoten im Navigationsbaum selektiert ist, wird er als
     * erster Treffer in der Stammdatenliste angezeigt. Falls keiner selektiert ist, wird eine leere Stammdatenliste
     * angezeigt.
     *
     * @param owner
     */
    public static void showSeriesMasterData(AbstractJavaViewerForm owner) {
        // Aktive Form holen
        AbstractJavaViewerForm activeForm = owner.getConnector().getActiveForm();
        iPartsSeriesId seriesId = null;

        // Produkt?
        iPartsProductId productId = new iPartsProductId(getIdFromTreeSelectionForType(activeForm, iPartsProductId.TYPE));
        if (productId.isValidId()) {
            EtkProject project = owner.getConnector().getProject();
            iPartsProduct product = iPartsProduct.getInstance(project, productId);
            seriesId = product.getReferencedSeries();
        }

        if (seriesId == null) {
            // Baureihe?
            seriesId = new iPartsSeriesId(getIdFromTreeSelectionForType(activeForm, iPartsSeriesId.TYPE));
            if (!seriesId.isValidId()) {
                seriesId = null;
            }
        }

        showSeriesMasterData(activeForm.getConnector(), activeForm, seriesId, null);
    }

    /**
     * Anzeige der Baureihen Tabelle (DA_Series)
     *
     * @param dataConnector
     * @param parentForm
     * @param onEditChangeRecordEvent
     */
    public static void showSeriesMasterData(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                            iPartsSeriesId seriesId, OnEditChangeRecordEvent onEditChangeRecordEvent) {
        if (onEditChangeRecordEvent == null) {
            // Falls nicht extern definiert: Hier die Callbacks für Edit, Modify und Delete definieren
            onEditChangeRecordEvent = new OnEditChangeRecordEvent() {
                private boolean onEditCreateOrModifyRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName,
                                                                IdWithType id, DBDataObjectAttributes attributes, boolean createRecord) {
                    iPartsDataSeries dataSeries = getDataSeriesFromSOPField();
                    if (dataSeries == null) {
                        iPartsSeriesId seriesId = new iPartsSeriesId(id.getValue(1));
                        dataSeries = new iPartsDataSeries(dataConnector.getProject(), seriesId);
                        if (dataSeries.loadFromDB(seriesId) && createRecord) {
                            String msg = "!!Die Baureihe ist bereits vorhanden und kann nicht neu angelegt werden!";
                            MessageDialog.show(msg, "!!Erzeugen", MessageDialogIcon.ERROR, MessageDialogButtons.OK);
                            return true;
                        }
                        if (createRecord) {
                            dataSeries.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                        }
                    } else {
                        if (createRecord) {
                            // dataSeries ist bereits vorbesetzt => extra Kontrolle
                            iPartsDataSeries testDataSeries = new iPartsDataSeries(dataConnector.getProject(), dataSeries.getAsId());
                            if (testDataSeries.existsInDB()) {
                                String msg = "!!Die Baureihe ist bereits vorhanden und kann nicht neu angelegt werden!";
                                MessageDialog.show(msg, "!!Erzeugen", MessageDialogIcon.ERROR, MessageDialogButtons.OK);
                                return true;
                            }

                        }
                    }
                    // Neuladen der Stückliste, wenn sich die Strukturstufe an der Baureihe, das Berechnungsmodell oder
                    // andere relevante Daten geändert haben, z.B. die Auslauftermine der Baureihe.
                    boolean reloadAssembly = false;
                    if (!createRecord) {
                        reloadAssembly = (attributes.fieldExists(FIELD_DS_ALTERNATIVE_CALC) && (dataSeries.getFieldValueAsBoolean(FIELD_DS_ALTERNATIVE_CALC) != attributes.getField(FIELD_DS_ALTERNATIVE_CALC).getAsBoolean()))
                                         || (attributes.fieldExists(FIELD_DS_V_POSITION_CHECK) && (dataSeries.getFieldValueAsBoolean(FIELD_DS_V_POSITION_CHECK) != attributes.getField(FIELD_DS_V_POSITION_CHECK).getAsBoolean()))
                                         || (attributes.fieldExists(FIELD_DS_HIERARCHY) && !dataSeries.getFieldValue(FIELD_DS_HIERARCHY).equals(attributes.getFieldValue(FIELD_DS_HIERARCHY)))
                                         || (attributes.fieldExists(FIELD_DS_EVENT_FLAG) && !dataSeries.getFieldValue(FIELD_DS_EVENT_FLAG).equals(attributes.getFieldValue(FIELD_DS_EVENT_FLAG)))
                                         || (attributes.fieldExists(FIELD_DS_IMPORT_RELEVANT) && !dataSeries.getFieldValue(FIELD_DS_IMPORT_RELEVANT).equals(attributes.getFieldValue(FIELD_DS_IMPORT_RELEVANT)))
                                         || dataSeries.getSeriesExpireDateList().isModifiedWithChildren();
                    }
                    dataSeries.assignAttributesValues(dataConnector.getProject(), attributes, true, DBActionOrigin.FROM_EDIT);

                    dataConnector.getProject().getDbLayer().startTransaction();
                    try {
                        if (iPartsRevisionChangeSet.saveDataObjectWithChangeSet(dataConnector.getProject(), dataSeries, iPartsChangeSetSource.SERIES)) {
                            dataSeries.saveToDB();
                            dataConnector.getProject().getDbLayer().commit();
                            iPartsDataChangedEventByEdit.Action action = createRecord ? iPartsDataChangedEventByEdit.Action.NEW : iPartsDataChangedEventByEdit.Action.MODIFIED;
                            ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.SERIES,
                                                                                                                      action, dataSeries.getAsId(), false));
                            if (reloadAssembly) {
                                ApplicationEvents.fireEventInAllProjectsAndAllClusters(new DataChangedEvent());
                            }
                            return true;
                        } else {
                            dataConnector.getProject().getDbLayer().rollback();
                        }
                    } catch (Exception e) {
                        dataConnector.getProject().getDbLayer().rollback();
                        Logger.getLogger().handleRuntimeException(e);
                    }
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
                    if ((attributeList != null) && !attributeList.isEmpty()) {
                        List<String> noDelList = new DwList<String>();
                        for (int lfdNr = attributeList.size() - 1; lfdNr >= 0; lfdNr--) {
                            DBDataObjectAttributes attributes = attributeList.get(lfdNr);
                            iPartsSeriesId seriesId = new iPartsSeriesId(attributes.getField(FIELD_DS_SERIES_NO).getAsString());
                            iPartsDataSeries dataSeries = new iPartsDataSeries(dataConnector.getProject(), seriesId);
                            if (dataSeries.hasDependencies()) {
                                noDelList.add(seriesId.getSeriesNumber());
                                attributeList.remove(lfdNr);
                            }
                        }
                        if (noDelList.size() > 0) {
                            String msg = "!!Die selektierten Baureihen werden noch in mindestens einem Produkt verwendet und können nicht gelöscht werden!";
                            if (noDelList.size() == 1) {
                                msg = "!!Die selektierte Baureihe wird noch in mindestens einem Produkt verwendet und kann nicht gelöscht werden!";
                            }
                            MessageDialog.show(msg, "!!Löschen", MessageDialogIcon.WARNING, MessageDialogButtons.OK);

                        } else {
                            String msg = "!!Wollen Sie die selektierte Baureihe inkl. aller dazugehörigen Baumuster (Konstruktion und After-Sales) sowie weiterer Referenzen wirklich löschen?";
                            if (attributeList.size() > 1) {
                                msg = "!!Wollen Sie die selektierten Baureihen inkl. aller dazugehörigen Baumuster (Konstruktion und After-Sales) sowie weiterer Referenzen wirklich löschen?";
                            }
                            if (MessageDialog.show(msg, "!!Löschen", MessageDialogIcon.CONFIRMATION,
                                                   MessageDialogButtons.YES, MessageDialogButtons.NO) == ModalResult.YES) {
                                return true;
                            }
                        }
                    }
                    return false;
                }

                @Override
                public boolean onEditDeleteRecordEvent(final AbstractJavaViewerFormIConnector dataConnector, String tableName,
                                                       final DBDataObjectAttributesList attributesList) {
                    final VarParam<Boolean> success = new VarParam<Boolean>(false);
                    if ((attributesList != null) && !attributesList.isEmpty()) {
                        final EtkMessageLogForm messageLogForm = new EtkMessageLogForm("!!Löschen", "!!Baureihe löschen inkl. aller dazugehörigen Baumuster (Konstruktion und After-Sales) sowie weiterer Referenzen",
                                                                                       DefaultImages.delete.getImage());
                        messageLogForm.showModal(new FrameworkRunnable() {
                            @Override
                            public void run(FrameworkThread thread) {
                                EtkDbObjectsLayer dbLayer = dataConnector.getProject().getDbLayer();
                                dbLayer.startTransaction();
                                dbLayer.startBatchStatement();
                                try {
                                    boolean deleteOK = true;
                                    List<iPartsSeriesId> seriesIds = new ArrayList<iPartsSeriesId>(attributesList.size());
                                    Set<iPartsModelId> modelIds = new HashSet<iPartsModelId>(attributesList.size() * 10);
                                    for (DBDataObjectAttributes attributes : attributesList) {
                                        iPartsSeriesId seriesId = new iPartsSeriesId(attributes.getField(FIELD_DS_SERIES_NO).getAsString());
                                        messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Baureihe %1 wird gelöscht",
                                                                                                                seriesId.getSeriesNumber()),
                                                                                   MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);

                                        // After-Sales-Baumuster von der Baureihe bestimmen für späteren Event
                                        iPartsDataModelList modelList = iPartsDataModelList.loadDataModelList(dataConnector.getProject(),
                                                                                                              seriesId.getSeriesNumber(),
                                                                                                              DBDataObjectList.LoadType.ONLY_IDS);
                                        for (iPartsDataModel dataModel : modelList) {
                                            modelIds.add(dataModel.getAsId());
                                        }

                                        iPartsDataSeries dataSeries = new iPartsDataSeries(dataConnector.getProject(), seriesId);
                                        dataSeries.loadChildren();
                                        if (iPartsRevisionChangeSet.deleteDataObjectWithChangeSet(dataConnector.getProject(), dataSeries, iPartsChangeSetSource.SERIES)) {
                                            if (!dataSeries.deleteFromDBWithModels(true, messageLogForm.getMessageLog())) {
                                                deleteOK = false;
                                                break;
                                            }
                                        } else {
                                            deleteOK = false;
                                            break;
                                        }
                                        seriesIds.add(seriesId);
                                    }

                                    if (deleteOK) {
                                        dbLayer.endBatchStatement();
                                        dbLayer.commit();

                                        messageLogForm.getMessageLog().fireMessage("!!Löschen abgeschlossen", MessageLogType.tmlMessage,
                                                                                   MessageLogOption.TIME_STAMP);

                                        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.SERIES,
                                                                                                                                  iPartsDataChangedEventByEdit.Action.DELETED,
                                                                                                                                  seriesIds, false));
                                        if (!modelIds.isEmpty()) {
                                            ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.MODEL,
                                                                                                                                      iPartsDataChangedEventByEdit.Action.DELETED,
                                                                                                                                      modelIds, false));
                                        }
                                        success.setValue(true);
                                    } else {
                                        dbLayer.cancelBatchStatement();
                                        dbLayer.rollback();
                                        success.setValue(false);
                                    }
                                } catch (Exception e) {
                                    dbLayer.cancelBatchStatement();
                                    dbLayer.rollback();
                                    messageLogForm.getMessageLog().fireMessage(e.getMessage(), MessageLogType.tmlError);
                                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                                }
                            }
                        });
                    }
                    return success.getValue();
                }
            };
        }

        MasterDataSeriesForm dlg = new MasterDataSeriesForm(dataConnector, parentForm, TABLE_DA_SERIES, onEditChangeRecordEvent);

        // Suchfelder definieren
        EtkDisplayFields searchFields = new EtkDisplayFields();
        searchFields.load(dataConnector.getConfig(), iPartsEditConfigConst.iPARTS_EDIT_MASTER_SERIES_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_SEARCHFIELDS,
                          dataConnector.getConfig().getCurrentDatabaseLanguage());
        if (searchFields.size() == 0) {
            // Suchfelder definieren
            searchFields.addFeld(createSearchField(dataConnector.getProject(), TABLE_DA_SERIES, FIELD_DS_SERIES_NO, false, false));
            searchFields.addFeld(createSearchField(dataConnector.getProject(), TABLE_DA_SERIES, FIELD_DS_TYPE, false, false));
        }

        // Anzeigefelder definieren
        EtkDisplayFields displayFields = new EtkDisplayFields();
        displayFields.load(dataConnector.getConfig(), iPartsEditConfigConst.iPARTS_EDIT_MASTER_SERIES_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS);
        if (displayFields.size() == 0) {
            EtkProject project = dataConnector.getProject();
            EtkDisplayField displayField = addDisplayField(TABLE_DA_SERIES, FIELD_DS_SERIES_NO, false, false, null, project, displayFields);
            displayField.setColumnFilterEnabled(true);
            displayField = addDisplayField(TABLE_DA_SERIES, FIELD_DS_TYPE, false, false, null, project, displayFields);
            displayField.setColumnFilterEnabled(true);
            addDisplayField(TABLE_DA_SERIES, FIELD_DS_NAME, true, false, null, project, displayFields);
        }

        // Editfelder fürs Editieren festlegen
        EtkEditFields editFields = new EtkEditFields();
        editFields.load(dataConnector.getConfig(), iPartsEditConfigConst.iPARTS_EDIT_MASTER_SERIES_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_EDITFIELDS);
        if (editFields.size() == 0) {
            editFields.addFeld(createEditField(dataConnector.getProject(), TABLE_DA_SERIES, FIELD_DS_SERIES_NO, false));
            editFields.addFeld(createEditField(dataConnector.getProject(), TABLE_DA_SERIES, FIELD_DS_TYPE, false));
            editFields.addFeld(createEditField(dataConnector.getProject(), TABLE_DA_SERIES, FIELD_DS_NAME, true));
        }
        if (editFields.getFeldByName(TABLE_DA_SERIES, FIELD_DS_SOP) == null) {
            EtkEditField editField = createEditField(dataConnector.getProject(), TABLE_DA_SERIES, FIELD_DS_SOP, false);
            editField.setText(new EtkMultiSprache("!!Start of Production (SOP)", dataConnector.getConfig().getDatabaseLanguages()));
            editField.setDefaultText(false);
            editFields.addFeld(editField);
        }
        disablePKFieldsForEdit(dataConnector, editFields, TABLE_DA_SERIES);

        // Sortierung
        LinkedHashMap<String, Boolean> sortFields = new LinkedHashMap<>();
        sortFields.put(FIELD_DS_SERIES_NO, false);
        dlg.setSortFields(sortFields);

        dlg.setDisplayResultFields(displayFields);
        dlg.setSearchFields(searchFields);
        dlg.setEditFields(editFields);
        // Hat der Anwender nur das Recht zum Markieren von Baureihen für den automatischen Export, muss der Edit erlaubt sein
        boolean editMasterDataAllowed = iPartsRight.EDIT_MASTER_DATA.checkRightInSession() || iPartsRight.ADD_SERIES_TO_AUTO_CALC_AND_EXPORT.checkRightInSession();
        boolean newMasterDataAllowed = iPartsRight.EDIT_MASTER_DATA.checkRightInSession();
        boolean deleteMasterDataAllowed = iPartsRight.DELETE_MASTER_DATA.checkRightInSession();
        dlg.setEditAllowed(editMasterDataAllowed || deleteMasterDataAllowed);
        dlg.setNewAllowed(newMasterDataAllowed);
        dlg.setModifyAllowed(editMasterDataAllowed);
        dlg.setDeleteAllowed(deleteMasterDataAllowed);
        dlg.setTitlePrefix("!!Baureihe");
        dlg.setWindowName("SeriesMasterData");
        dlg.setMaxResults(J2EEHandler.isJ2EE() ? MAX_SELECT_SEARCH_RESULTS_SIZE : -1);

        if ((seriesId != null) && seriesId.isValidId()) {
            // Suchwerte setzen und Suche starten
            DBDataObjectAttributes searchAttributes = new DBDataObjectAttributes();
            searchAttributes.addField(FIELD_DS_SERIES_NO, seriesId.getSeriesNumber(), DBActionOrigin.FROM_DB);
            dlg.setSearchValues(searchAttributes);
        }
        dlg.showModal();
    }

    private static iPartsDataSeries dataSeriesFromSOPField;

    private static iPartsDataSeries getDataSeriesFromSOPField() {
        return dataSeriesFromSOPField;
    }

    private static void setDataSeriesFromSOPField(iPartsDataSeries dataSeriesFromSOPField) {
        MasterDataSeriesForm.dataSeriesFromSOPField = dataSeriesFromSOPField;
    }

    private GuiMenuItem showBADCodeMenuItem;
    private GuiMenuItem showSeriesEventsMenuItem;
    private GuiMenuItem showSeriesCodesMenuItem;

    private MasterDataSeriesForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName, OnEditChangeRecordEvent onEditChangeRecordEvent) {
        super(dataConnector, parentForm, tableName, onEditChangeRecordEvent);
        doResizeWindow(SCREEN_SIZES.MAXIMIZE);
    }

    @Override
    protected void addToolbarButtonsAndContextMenuItems(EditToolbarButtonMenuHelper toolbarHelper, GuiContextMenu contextMenu) {
        super.addToolbarButtonsAndContextMenuItems(toolbarHelper, contextMenu);

        GuiSeparator separatorShowBADCode = new de.docware.framework.modules.gui.controls.GuiSeparator();
        separatorShowBADCode.setName("menuSeparatorShowBADCode");
        contextMenu.addChild(separatorShowBADCode);

        boolean isEditAllowed = iPartsRight.EDIT_MASTER_DATA.checkRightInSession() || iPartsRight.DELETE_MASTER_DATA.checkRightInSession();
        showBADCodeMenuItem = toolbarHelper.createMenuEntry("showBADCode", isEditAllowed ? "!!BAD-Code bearbeiten..." : "!!BAD-Code anzeigen...",
                                                            DefaultImages.module.getImage(), new de.docware.framework.modules.gui.event.EventListener(Event.MENU_ITEM_EVENT) {
                    @Override
                    public void fire(Event event) {
                        doShowBADCode(event);
                    }
                }, getUITranslationHandler());
        contextMenu.addChild(showBADCodeMenuItem);
        showSeriesEventsMenuItem = toolbarHelper.createMenuEntry("showSeriesEvents", "!!Ereigniskette zur Baureihe anzeigen...", DefaultImages.module.getImage(), new de.docware.framework.modules.gui.event.EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                doShowSeriesEvents(event);
            }
        }, getUITranslationHandler());
        contextMenu.addChild(showSeriesEventsMenuItem);

        showSeriesCodesMenuItem = toolbarHelper.createMenuEntry("showSeriesCodes", "!!Code zur Baureihe anzeigen...", DefaultImages.module.getImage(), new de.docware.framework.modules.gui.event.EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                doShowSeriesCodes(event);
            }
        }, getUITranslationHandler());
        contextMenu.addChild(showSeriesCodesMenuItem);


        GuiSeparator separatorSearchMADData = new de.docware.framework.modules.gui.controls.GuiSeparator();
        separatorSearchMADData.setName("menuSeparatorSearchMADData");
        contextMenu.addChild(separatorSearchMADData);

        GuiMenuItem changeProductValuesMenuItem = toolbarHelper.createMenuEntry("searchMADData", "!!Suche verwaiste MAD Daten", DefaultImages.search.getImage(), new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                Set<String> chosenSeries = new HashSet<>();
                for (DBDataObjectAttributes selection : getMultiSelection()) {
                    String series = selection.getFieldValue(FIELD_DS_SERIES_NO);
                    if (StrUtils.isValid(series)) {
                        chosenSeries.add(series.toUpperCase());
                    }
                }
                iPartsMainImportHelper.searchMADDatAfterDIALOGImport(getProject(), chosenSeries);
            }
        }, getUITranslationHandler());
        contextMenu.addChild(changeProductValuesMenuItem);
    }

    @Override
    protected void enableButtons() {
        super.enableButtons();

        int selectionRowCount = getTable().getSelectedRows().size();
        boolean singleSelectionEnabled = selectionRowCount == 1;
        boolean isEventEnabled = singleSelectionEnabled;

        if (singleSelectionEnabled) {
            iPartsSeriesId seriesId = new iPartsSeriesId(getSelection().getField(FIELD_DS_SERIES_NO, false).getAsString());
            iPartsDialogSeries dialogSeries = iPartsDialogSeries.getInstance(getProject(), seriesId);
            isEventEnabled = dialogSeries.isEventTriggered();
        }
        showBADCodeMenuItem.setEnabled(singleSelectionEnabled);
        showSeriesCodesMenuItem.setEnabled(singleSelectionEnabled);
        showSeriesEventsMenuItem.setEnabled(isEventEnabled);
    }

    @Override
    protected void doNew(Event event) {
        endSearch();
        EtkDatabaseTable tableDef = getProject().getConfig().getDBDescription().findTable(searchTable);
        List<String> pkFields = tableDef.getPrimaryKeyFields();

        String[] emptyPkValues = new String[pkFields.size()];
        Arrays.fill(emptyPkValues, "");
        IdWithType id = new IdWithType("xx", emptyPkValues);

        // Beim Neu anlegen sind alle Felder editierbar => deswegen Kopie
        EtkEditFields editNewFields = new EtkEditFields();
        editNewFields.assign(editFields);
        for (EtkEditField field : editNewFields.getFields()) {
            field.setEditierbar(true);
        }

        DBDataObjectAttributes initialAttributes = null;
        if (onCreateEvent != null) {
            initialAttributes = onCreateEvent.onCreateAttributesEvent();
        }

        EditUserControlForCreateSeries eCtrl = new EditUserControlForCreateSeries(getConnector(), this, searchTable, id, initialAttributes, editNewFields);
        eCtrl.setTitle(titleForCreate);
        eCtrl.setWindowName(editControlsWindowName);
        setDataSeriesFromSOPField(null);
        if (eCtrl.showModal() == ModalResult.OK) {
            if (onEditChangeRecordEvent != null) {
                id = buildIdFromAttributes(eCtrl.getAttributes());
                setDataSeriesFromSOPField(eCtrl.getDataFromSOPField());
                if (onEditChangeRecordEvent.onEditCreateRecordEvent(getConnector(), searchTable, id, eCtrl.getAttributes())) {
                    setSelectionAfterSearch(eCtrl.getAttributes());
                    setSearchValues(eCtrl.getAttributes());
                }
            }
        }
    }

    @Override
    protected void doEditOrView(Event event) {
        endSearch();
        DBDataObjectAttributes attributes = getSelectedAttributes();
        if (attributes != null) {
            IdWithType id = buildIdFromAttributes(attributes);

            EditUserControlForSeries eCtrl = new EditUserControlForSeries(getConnector(), this, searchTable, id, attributes, editFields);
            boolean editAndModifyAllowed = isEditAllowed() && isModifyAllowed();
            eCtrl.setReadOnly(!editAndModifyAllowed);
            eCtrl.setTitle(editAndModifyAllowed ? titleForEdit : titleForView);
            eCtrl.setWindowName(editControlsWindowName);
            setDataSeriesFromSOPField(null);
            if (eCtrl.showModal() == ModalResult.OK) {
                setDataSeriesFromSOPField(eCtrl.getDataFromSOPField());
                if (onEditChangeRecordEvent != null) {
                    if (onEditChangeRecordEvent.onEditModifyRecordEvent(getConnector(), searchTable, id, eCtrl.getAttributes())) {
                        // Suche nochmals starten als Refresh für Table
                        setSelectionAfterSearch(eCtrl.getAttributes());
                        startSearch(true);
                    }
                }
            }
        }
    }

    private void doShowBADCode(Event event) {
        DBDataObjectAttributes attributes = getSelectedAttributes();
        if ((attributes != null) && !attributes.isEmpty()) {
            iPartsSeriesId seriesId = new iPartsSeriesId(attributes.getField(FIELD_DS_SERIES_NO, false).getAsString());
            MasterDataBADCodeForm.showBADCodeForSeries(getConnector(), this, seriesId);
        }
    }

    private void doShowSeriesCodes(Event event) {
        DBDataObjectAttributes attributes = getSelectedAttributes();
        if ((attributes != null) && !attributes.isEmpty()) {
            iPartsSeriesId seriesId = new iPartsSeriesId(attributes.getField(FIELD_DS_SERIES_NO, false).getAsString());
            EditSeriesCodesForm.showSeriesCodesForSeries(getConnector(), this, seriesId);
        }
    }

    private void doShowSeriesEvents(Event event) {
        DBDataObjectAttributes attributes = getSelectedAttributes();
        if ((attributes != null) && !attributes.isEmpty()) {
            iPartsSeriesId seriesId = new iPartsSeriesId(attributes.getField(FIELD_DS_SERIES_NO, false).getAsString());
            EditSeriesEventsForm.showSeriesEventsForSeries(getConnector(), this, seriesId);
        }
    }

    @Override
    protected String getVisualValueOfFieldValue(String tableName, String fieldName, DBDataObjectAttribute fieldValue, boolean isMultiLanguage) {
        if (tableName.equals(TABLE_DA_SERIES) && fieldName.equals(FIELD_DS_HIERARCHY)) {
            String currentValue = fieldValue.getAsString();
            if (StrUtils.isEmpty(currentValue)) {
                fieldValue.setValueAsString(TranslationHandler.translate(ADDITIONAL_ENUM_VALUE_FOR_SERIES_HIERARCHY), DBActionOrigin.FROM_DB);
            }
        }
        return super.getVisualValueOfFieldValue(tableName, fieldName, fieldValue, isMultiLanguage);
    }
}
