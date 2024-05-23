/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.*;
import de.docware.apps.etk.base.forms.events.OnStartSearchEvent;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.*;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetSource;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsSAId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.OnEditChangeRecordEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EtkEditFieldHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo.iPartsRelatedInfoMasterDataForm;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiSeparator;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogButtons;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogIcon;
import de.docware.framework.modules.gui.event.Event;
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
import de.docware.util.sql.TableAndFieldName;
import org.apache.commons.collections4.map.LRUMap;

import java.util.*;

/**
 * Formular für die Anzeige der Stammdaten von freien SAs (Tabelle DA_SA).
 */
public class MasterDataFreeSAsForm extends SimpleMasterDataSearchFilterGrid {

    private static final String[] FIELDS_FOR_MODIFY = new String[]{ FIELD_DS_NOT_DOCU_RELEVANT, FIELD_DS_DESC };

    private Map<IdWithType, EtkDataObject> dataObjectCache;
    private Map<String, Set<String>> additionalFields;
    private Set<String> additionalVirtualFields;
    private GuiMenuItem showSAMasterDataMenuItem;
    private GuiMenuItem retrieveSAPicturesMenuItem;
    private Map<String, String> virtualFieldMapping;

    /**
     * Zeigt Baureihenstammdaten an. Wenn ein Baureihenknoten im Navigationsbaum selektiert ist, wird er als
     * erster Treffer in der Stammdatenliste angezeigt. Falls keiner selektiert ist, wird eine leere Stammdatenliste
     * angezeigt.
     *
     * @param owner
     */
    public static void showFreeSAsMasterData(AbstractJavaViewerForm owner) {
        // Aktive Form holen
        AbstractJavaViewerForm activeForm = owner.getConnector().getActiveForm();
        String treeValue = getIdFromTreeSelectionForType(activeForm, iPartsSaId.TYPE);
        iPartsSaId saId = new iPartsSaId(treeValue);
        if (!saId.isValidId()) {
            saId = null;
        }
        showFreeSAsMasterData(activeForm.getConnector(), activeForm, saId, null);
    }

    public static void showFreeSAsMasterData(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                             iPartsSaId saId, OnEditChangeRecordEvent onEditChangeRecordEvent) {
        if (onEditChangeRecordEvent == null) {
            onEditChangeRecordEvent = new OnEditChangeRecordEvent() {
                @Override
                public boolean onEditCreateRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName, IdWithType id, DBDataObjectAttributes attributes) {
                    return false;
                }

                @Override
                public boolean onEditModifyRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName, IdWithType id, DBDataObjectAttributes attributes) {
                    EtkProject project = dataConnector.getProject();
                    iPartsSaId saId = new iPartsSaId(id.getValue(1));
                    iPartsDataSa dataSa = new iPartsDataSa(project, saId);
                    if (!dataSa.existsInDB()) {
                        dataSa.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                    }
                    dataSa.assignAttributes(project, attributes, false, DBActionOrigin.FROM_EDIT);
                    GenericEtkDataObjectList dataObjectList = new GenericEtkDataObjectList();
                    boolean result = prepareForSave(project, dataSa, dataObjectList);
                    if (result) {
                        result = iPartsRevisionChangeSet.saveDataObjectListWithChangeSet(project, dataObjectList, iPartsChangeSetSource.SA);
                        if (result) {
                            project.getDbLayer().startTransaction();
                            try {
                                dataObjectList.saveToDB(project);
                                project.getDbLayer().commit();

                                Set<iPartsSaId> modifiedSaIds = new HashSet<>();
                                modifiedSaIds.add(saId);
                                ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.SA,
                                                                                                                          iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                                                          modifiedSaIds,
                                                                                                                          false));
                                ApplicationEvents.fireEventInAllProjectsAndAllClusters(new DataChangedEvent(null));
                            } catch (Exception e) {
                                project.getDbLayer().rollback();
                                Logger.getLogger().handleRuntimeException(e);
                                result = false;
                            }
                        }
                    }
                    return result;
                }

                private boolean prepareForSave(EtkProject project, iPartsDataSa dataSa,
                                               GenericEtkDataObjectList dataObjectList) {
                    dataObjectList.clear(DBActionOrigin.FROM_DB);
                    dataObjectList.add(dataSa, DBActionOrigin.FROM_EDIT);
                    boolean textChanged = dataSa.getAttributes().getField(FIELD_DS_DESC).isModified();
                    if (textChanged) {
                        // dann die Module suchen
                        iPartsDataSAModulesList dataSAModulesList = iPartsDataSAModulesList.loadDataForSA(project,
                                                                                                          new iPartsSAId(dataSa.getAsId().getSaNumber()));
                        if (!dataSAModulesList.isEmpty()) {
                            EtkMultiSprache multi = dataSa.getFieldValueAsMultiLanguage(FIELD_DS_DESC);
                            for (iPartsDataSAModules dataSAModule : dataSAModulesList) {
                                String modulNumber = dataSAModule.getFieldValue(FIELD_DSM_MODULE_NO);
                                AssemblyId assemblyId = new AssemblyId(modulNumber, "");
                                EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);
                                if (assembly.existsInDB()) {
                                    EtkDataPart part = assembly.getPart();
                                    part.setFieldValueAsMultiLanguage(FIELD_M_TEXTNR, multi.cloneMe(), DBActionOrigin.FROM_EDIT);
                                    dataObjectList.add(assembly, DBActionOrigin.FROM_EDIT);
                                }
                            }
                        }
                    }
                    return true;
                }

                @Override
                public boolean onEditAskForDelete(AbstractJavaViewerFormIConnector dataConnector, String tableName, DBDataObjectAttributesList attributeList) {
                    String msg = "!!Wollen Sie die selektierte freie SA inkl. aller Modulinhalte wirklich löschen?";
                    if (attributeList.size() > 1) {
                        msg = "!!Wollen Sie die selektierten freien SAs inkl. aller Modulinhalte wirklich löschen?";
                    }
                    if (MessageDialog.show(msg, "!!Löschen", MessageDialogIcon.CONFIRMATION,
                                           MessageDialogButtons.YES, MessageDialogButtons.NO) == ModalResult.YES) {
                        return true;
                    }
                    return false;
                }

                @Override
                public boolean onEditDeleteRecordEvent(final AbstractJavaViewerFormIConnector dataConnector, String tableName,
                                                       final DBDataObjectAttributesList attributeList) {
                    final VarParam<Boolean> success = new VarParam<>(false);
                    if ((attributeList != null) && !attributeList.isEmpty()) {
                        final EtkMessageLogForm messageLogForm = new EtkMessageLogForm("!!Löschen", "!!SA löschen inkl. dazugehöriger Modulinhalte",
                                                                                       DefaultImages.delete.getImage());
                        messageLogForm.showModal(new FrameworkRunnable() {
                            @Override
                            public void run(FrameworkThread thread) {
                                final EtkProject project = dataConnector.getProject();

                                // Aktive Änderungssets temporär deaktivieren
                                project.executeWithoutActiveChangeSets(new Runnable() {
                                    @Override
                                    public void run() {
                                        EtkDbObjectsLayer dbLayer = project.getDbLayer();
                                        try {
                                            dbLayer.startTransaction();
                                            dbLayer.startBatchStatement();
                                            boolean deleteOK = true;
                                            String language = project.getViewerLanguage();
                                            List<iPartsSaId> saIds = new ArrayList<iPartsSaId>(attributeList.size());

                                            for (DBDataObjectAttributes attributes : attributeList) {
                                                String saNumber = attributes.getFieldValue(FIELD_DS_SA);
                                                if (StrUtils.isValid(saNumber)) {
                                                    iPartsSaId saId = new iPartsSaId(saNumber);
                                                    // ToDo besitzen SA's analog zu Product ein eigenes SA-spezifisches Lösch-Recht?
//                                                    if (!iPartsRight.checkSAEditableInSession(saId, iPartsRight.DELETE_MASTER_DATA, true)) {
//                                                        continue;
//                                                    }
                                                    String saNumberForView = project.getVisObject().asText(TABLE_DA_SA, FIELD_DS_SA, saNumber, language);
                                                    messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!SA %1 wird gelöscht",
                                                                                                                            saNumberForView),
                                                                                               MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                                                    iPartsDataSa dataSa = new iPartsDataSa(project, saId);
                                                    // Im Changeset wird nur die SA selbst als gelöscht markiert
                                                    deleteOK = iPartsRevisionChangeSet.deleteDataObjectWithChangeSet(project, dataSa, iPartsChangeSetSource.SA);
                                                    if (deleteOK) {
                                                        // Das tatsächliche Löschen der SA inkl. Module direkt in der DB erfolgt dann hier (analog Produkt)
                                                        deleteOK = deleteSAModules(project, dataSa);
                                                    }
                                                    if (deleteOK) {
                                                        saIds.add(saId);
                                                    } else {
                                                        break;
                                                    }
                                                }
                                            }
                                            if (deleteOK) {
                                                dbLayer.endBatchStatement();
                                                dbLayer.commit();

                                                messageLogForm.getMessageLog().fireMessage("!!Löschen abgeschlossen", MessageLogType.tmlMessage,
                                                                                           MessageLogOption.TIME_STAMP);

                                                if (!saIds.isEmpty()) {
                                                    ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<iPartsSaId>(iPartsDataChangedEventByEdit.DataType.SA,
                                                                                                                                                        iPartsDataChangedEventByEdit.Action.DELETED,
                                                                                                                                                        saIds,
                                                                                                                                                        true));
                                                    ApplicationEvents.fireEventInAllProjectsAndAllClusters(new DataChangedEvent(null));
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
                                }, true);
                            }

                            private boolean deleteSAModules(EtkProject project, iPartsDataSa sa) {
                                // zuerst die SA selbst löschen
                                sa.deleteFromDB(true);

                                // dann die Module löschen
                                iPartsDataSAModulesList dataSAModulesList = iPartsDataSAModulesList.loadDataForSA(project,
                                                                                                                  new iPartsSAId(sa.getAsId().getSaNumber()));
                                if (!dataSAModulesList.isEmpty()) { // SA besitzt Stücklisteneinträge
                                    // Es dürfte eigentlich nur einen Eintrag geben, aber sicher ist sicher
                                    for (iPartsDataSAModules dataSAModule : dataSAModulesList) {
                                        String modulNumber = dataSAModule.getFieldValue(FIELD_DSM_MODULE_NO);
                                        if (!deleteSAModul(project, modulNumber)) {
                                            return false;
                                        }
                                    }
                                }
                                return true;
                            }

                            private boolean deleteSAModul(EtkProject project, String modulNumber) {
                                AssemblyId assemblyId = new AssemblyId(modulNumber, "");
                                EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);
                                if (assembly.existsInDB()) {
                                    if (assembly instanceof iPartsDataAssembly) {
                                        iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)assembly;
                                        if (!iPartsAssembly.delete_iPartsAssembly(true)) {
                                            return false;
                                        }
                                    }
                                }
                                return true;
                            }
                        });
                    }
                    return success.getValue();
                }
            };
        }

        MasterDataFreeSAsForm dlg = new MasterDataFreeSAsForm(dataConnector, parentForm, TABLE_DA_SA, onEditChangeRecordEvent);

        EtkProject project = dataConnector.getProject();
        // Suchfelder definieren
        EtkDisplayFields searchFields = getSearchFieldsForSaForm(project);

        // Anzeigefelder definieren
        EtkDisplayFields displayFields = getDisplayFieldsForSaForm(project);

        // Editfelder fürs Editieren festlegen
        EtkEditFields editFields = getEditFieldsForSaForm(project);

        // Sortierung
        LinkedHashMap<String, Boolean> sortFields = new LinkedHashMap<>();
        sortFields.put(FIELD_DS_SA, false);
        dlg.setSortFields(sortFields);

        dlg.setDisplayResultFields(displayFields);
        dlg.setSearchFields(searchFields);
        dlg.setEditFields(editFields);
        boolean isCarOrTruckUser = dlg.isCarAndVanInSession() || dlg.isTruckAndBusInSession();
        boolean editMasterDataAllowed = false; // iPartsRight.EDIT_MASTER_DATA.checkRightInSession();
        boolean deleteMasterDataAllowed = iPartsRight.DELETE_MASTER_DATA.checkRightInSession() && isCarOrTruckUser;
        boolean modifyMasterDataAllowed = iPartsRight.EDIT_TEXT_PRODUCT_ADMIN.checkRightInSession() && isCarOrTruckUser;
        dlg.setEditAllowed(editMasterDataAllowed || deleteMasterDataAllowed || modifyMasterDataAllowed);
        dlg.setNewAllowed(editMasterDataAllowed);
        dlg.setModifyAllowed(modifyMasterDataAllowed);
        dlg.setDeleteAllowed(deleteMasterDataAllowed);
        dlg.setTitlePrefix("!!Freie SAs");
        dlg.setWindowName("FreeSAsMasterData");
        dlg.setMaxResults(J2EEHandler.isJ2EE() ? MAX_SELECT_SEARCH_RESULTS_SIZE : -1);

        // Wurde eine SA übergeben, dann direkt danach suchen
        if ((saId != null) && saId.isValidId()) {
            // Suchwerte setzen und Suche starten
            DBDataObjectAttributes searchAttributes = new DBDataObjectAttributes();
            searchAttributes.addField(FIELD_DS_SA, saId.getSaNumber(), DBActionOrigin.FROM_DB);
            dlg.setSearchValues(searchAttributes);
        }

        dlg.showModal();
    }

    @Override
    protected void modifySearchControl(EtkDisplayField searchField, EditControl ctrl) {
        if (searchField.getKey().getFieldName().equals(FIELD_DS_SOURCE) && (ctrl.getEditControl().getControl() instanceof EnumRComboBox)) {
            // Sichtbarkeit der Quellen mit den Benutzer-Eigenschaften filtern
            EnumRComboBox sourceComboBox = ((EnumRComboBox)ctrl.getEditControl().getControl());
            sourceComboBox.setMaximumRowCount(20);
            for (String source : new ArrayList<>(sourceComboBox.getTokens())) {
                if (!iPartsImportDataOrigin.isSourceVisible(source, isCarAndVanInSession(), isTruckAndBusInSession())) {
                    sourceComboBox.removeItemByUserObject(source);
                }
            }
        } else {
            super.modifySearchControl(searchField, ctrl);
        }
    }

    public static EtkDisplayFields getSearchFieldsForSaForm(EtkProject project) {
        EtkDisplayFields searchFields = new EtkDisplayFields();
        searchFields.load(project.getConfig(), iPartsEditConfigConst.iPARTS_EDIT_MASTER_FREE_SA_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_SEARCHFIELDS,
                          project.getConfig().getCurrentDatabaseLanguage());
        if (searchFields.size() == 0) {
            // Suchfelder definieren
            searchFields.addFeld(createSearchField(project, TABLE_DA_SA, FIELD_DS_SA, false, false));
            searchFields.addFeld(createSearchField(project, TABLE_DA_SA, FIELD_DS_SOURCE, false, false));
        }
        return searchFields;
    }

    public static EtkDisplayFields getDisplayFieldsForSaForm(EtkProject project) {
        EtkDisplayFields displayFields = new EtkDisplayFields();
        displayFields.load(project.getConfig(), iPartsEditConfigConst.iPARTS_EDIT_MASTER_FREE_SA_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS);
        if (displayFields.size() == 0) {
//            EtkProject project = dataConnector.getProject();
            EtkDisplayField displayField = addDisplayField(TABLE_DA_SA, FIELD_DS_SA, false, false, null, project, displayFields);
            displayField.setColumnFilterEnabled(true);
            displayField = addDisplayField(TABLE_DA_SA, FIELD_DS_DESC, true, false, null, project, displayFields);
            displayField.setColumnFilterEnabled(true);
            displayField = addDisplayField(TABLE_DA_SA, FIELD_DS_CONST_DESC, true, false, null, project, displayFields);
            displayField.setColumnFilterEnabled(true);
            addDisplayField(TABLE_DA_SA, FIELD_DS_EDAT, false, false, null, project, displayFields);
            addDisplayField(TABLE_DA_SA, FIELD_DS_ADAT, false, false, null, project, displayFields);
            displayField = addDisplayField(TABLE_DA_SA, FIELD_DS_CODES, false, false, null, project, displayFields);
            displayField.setColumnFilterEnabled(true);
            displayField = addDisplayField(TABLE_DA_SA, FIELD_DS_SOURCE, false, false, null, project, displayFields);
            displayField.setColumnFilterEnabled(true);
        } else {
            // Das Feld DS_SOURCE muss unbedingt im SQL Select mit dabei sein
            if (!displayFields.contains(TABLE_DA_SA, FIELD_DS_SOURCE, false)) {
                EtkDisplayField displayField = addDisplayField(TABLE_DA_SA, FIELD_DS_SOURCE, false, false, null, project, displayFields);
                displayField.setVisible(false);
            }
        }
        return displayFields;
    }

    public static EtkEditFields getEditFieldsForSaForm(EtkProject project) {
        EtkDatabaseTable tableDef = project.getConfig().getDBDescription().findTable(TABLE_DA_SA);
        // Editfelder fürs Editieren festlegen
        EtkEditFields editFields = new EtkEditFields();
        EtkEditFieldHelper.getEditFields(project, TABLE_DA_SA, editFields, true);

        List<String> editableFields = new DwList<>(FIELDS_FOR_MODIFY);
        List<String> pkFields = tableDef.getPrimaryKeyFields();
        for (EtkEditField eField : editFields.getFields()) {
            if (pkFields.contains(eField.getKey().getFieldName())) {
                eField.setMussFeld(true);
                eField.setEditierbar(false);  // ist PK-Value => nicht editierbar
            } else {
                eField.setEditierbar(editableFields.contains(eField.getKey().getFieldName()));
            }
        }
        return editFields;
    }

    /**
     * Erzeugt eine Instanz von SimpleMasterDataSearchResultGrid.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     * @param parentForm
     * @param tableName
     * @param onEditChangeRecordEvent
     */
    public MasterDataFreeSAsForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName, OnEditChangeRecordEvent onEditChangeRecordEvent) {
        super(dataConnector, parentForm, tableName, onEditChangeRecordEvent);
        doResizeWindow(SCREEN_SIZES.MAXIMIZE);
        dataObjectCache = new LRUMap(iPartsConst.MAX_CACHE_SIZE_MASTER_DATA);
        additionalFields = new LinkedHashMap<>();
        additionalVirtualFields = new HashSet<>();
        setOnStartSearchEvent(new OnStartSearchEvent() {
            @Override
            public void onStartSearch() {
                dataObjectCache.clear();
            }
        });
        initVirtualMapping();
    }

    public void hideWorkToolbarButton(boolean hide) {
        if (hide) {
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, getTable().getContextMenu());
        } else {
            toolbarHelper.showToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, getTable().getContextMenu());
        }
    }

    /**
     * Mapping für virtuelle Felder (Abhängigkeit bezüglich BK und SAA)
     */
    private void initVirtualMapping() {
        virtualFieldMapping = new HashMap<String, String>();
//        virtualFieldMapping.put(makeVirtualMappinKey(TABLE_DA_SAA, iPartsDataVirtualFieldsDefinition.DESM_DESCRIPTION), FIELD_DS_DESC);
//        virtualFieldMapping.put(makeVirtualMappinKey(TABLE_DA_SAA, iPartsDataVirtualFieldsDefinition.DESM_DESCRIPTION_CONST), FIELD_DS_CONST_DESC);
//        virtualFieldMapping.put(makeVirtualMappinKey(TABLE_MAT, iPartsDataVirtualFieldsDefinition.DESM_DESCRIPTION), FIELD_M_TEXTNR);
//        virtualFieldMapping.put(makeVirtualMappinKey(TABLE_MAT, iPartsDataVirtualFieldsDefinition.DESM_DESCRIPTION_CONST), FIELD_M_CONST_DESC);
    }

    private String makeVirtualMappinKey(String tablename, String virtualField) {
        return tablename + "||" + virtualField;
    }

    @Override
    protected void addToolbarButtonsAndContextMenuItems(EditToolbarButtonMenuHelper toolbarHelper, GuiContextMenu contextMenu) {
        super.addToolbarButtonsAndContextMenuItems(toolbarHelper, contextMenu);

        boolean editMasterDataAllowed = iPartsRight.EDIT_MASTER_DATA.checkRightInSession();
        if (editMasterDataAllowed) {
            GuiSeparator separator = new de.docware.framework.modules.gui.controls.GuiSeparator();
            separator.setUserObject(SEPERATOR_ALIAS);
            separator.setName("menuSeparator1");
            contextMenu.addChild(separator);
        }

        showSAMasterDataMenuItem = toolbarHelper.createMenuEntry("saMasterData", "!!SA-Stammdaten anzeigen...", DefaultImages.module.getImage(), new de.docware.framework.modules.gui.event.EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                endSearch();
                doShowSAAs(event);
            }
        }, getUITranslationHandler());
        contextMenu.addChild(showSAMasterDataMenuItem);

        boolean retrievePicturesAllowed = iPartsRight.RETRIEVE_PICTURES.checkRightInSession();
        if (retrievePicturesAllowed) {
            GuiSeparator separator = new de.docware.framework.modules.gui.controls.GuiSeparator();
            separator.setName("menuSeparatorRetrievePictures");
            separator.setUserObject(SEPERATOR_ALIAS);
            contextMenu.addChild(separator);

            retrieveSAPicturesMenuItem = toolbarHelper.createMenuEntry("retrievePictures", "!!Zeichnungen nachfordern...",
                                                                       DefaultImages.image.getImage(), new de.docware.framework.modules.gui.event.EventListener(Event.MENU_ITEM_EVENT) {
                        @Override
                        public void fire(Event event) {
                            endSearch();
                            doRetrieveSAPictures(event);
                        }
                    }, getUITranslationHandler());
            contextMenu.addChild(retrieveSAPicturesMenuItem);
        }
    }

    protected void hideContextMenuItems() {
        GuiContextMenu contextMenu = getTable().getContextMenu();
        for (AbstractGuiControl child : contextMenu.getChildren()) {
            if (child.getUserObject() == SEPERATOR_ALIAS) {
                child.setVisible(false);
            } else if ((child == retrieveSAPicturesMenuItem) || (child == showSAMasterDataMenuItem)) {
                child.setVisible(false);
            }
        }
    }

    private void doShowSAAs(Event event) {
        DBDataObjectAttributes attributes = getSelectedAttributes();
        if ((attributes != null) && !attributes.isEmpty()) {
            IdWithType id = buildIdFromAttributes(attributes);
            // DAIMLER-7036
            iPartsSaId saId = new iPartsSaId(id.toStringArrayWithoutType()[0]);
            iPartsRelatedInfoMasterDataForm.showRelatedInfoMasterDataForm(super.createRelatedInfoConnector(), this,
                                                                          saId, "!!SA-Stammdaten");
        }
    }

    /**
     * Zeigt den Dialog zum Nachfordern von Zeichnungen an.
     *
     * @param event
     */
    private void doRetrieveSAPictures(Event event) {
        List<iPartsSaId> saIds = new DwList<>();
        // Ausgewählte SAs bestimmen
        DBDataObjectAttributesList selectedAttributesList = getSelectedAttributesList();
        for (DBDataObjectAttributes selectedAttributes : selectedAttributesList) {
            IdWithType id = buildIdFromAttributes(selectedAttributes);
            iPartsSaId saId = new iPartsSaId(id.getValue(1));
            saIds.add(saId);
        }
        // todo: Hier eventuell Rechte des Benutzers abfragen (falls Rechte für einzelne SAs eingeschränkt werden können)
        if (!saIds.isEmpty()) {
            RequestPicturesForm.showRequestOptionsForSAs(getConnector(), this, saIds);
        }
//        else {
//            MessageDialog.showWarning(TranslationHandler.translate("!!Keine Rechte zum Editieren der ausgewählten Produkte für den Benutzer \"%1\".",
//                                                                   iPartsUserAdminDb.getLoginUserFullName()));
//        }
    }

    @Override
    protected void enableButtons() {
        super.enableButtons();

        int selectionRowCount = getTable().getSelectedRows().size();
        boolean singleSelection = selectionRowCount == 1;
        boolean multiSelection = selectionRowCount > 0;

        showSAMasterDataMenuItem.setEnabled(singleSelection);
        if (retrieveSAPicturesMenuItem != null) {
            retrieveSAPicturesMenuItem.setEnabled(multiSelection && iPartsRight.EDIT_PARTS_DATA.checkRightInSession());
        }
    }


    @Override
    public void setDisplayResultFields(EtkDisplayFields displayResultFields) {
        super.setDisplayResultFields(displayResultFields);
        additionalFields.clear();
        for (EtkDisplayField displayField : displayResultFields.getFields()) {
            String tablename = displayField.getKey().getTableName();
            String fieldname = displayField.getKey().getFieldName();
            if (!tablename.equals(TABLE_DA_SA)) {
                Set<String> fieldNames = additionalFields.get(tablename);
                if (fieldNames == null) {
                    fieldNames = new HashSet<String>();
                    additionalFields.put(tablename, fieldNames);
                }
                fieldNames.add(fieldname);
            } else if (VirtualFieldsUtils.isVirtualField(fieldname)) {
                addVirtualDisplayField(fieldname);
            }
        }
    }

    public void addVirtualDisplayField(String... virtFields) {
        for (String field : virtFields) {
            additionalVirtualFields.add(field);
        }
    }

    @Override
    protected List<String> getSearchValues() {
        List<String> liste = new DwList<String>();
        iPartsNumberHelper numberHelper = new iPartsNumberHelper();
        for (int lfdNr = 0; lfdNr < searchFields.size(); lfdNr++) {
            EditControlFactory ctrl = editControls.getControlByFeldIndex(lfdNr).getEditControl();
            String value = ctrl.getText();
            if (TableAndFieldName.make(TABLE_DA_SA, FIELD_DS_SA).equals(ctrl.getTableFieldName())) {
                // Sonderbehandlung für SA Nummern
                value = numberHelper.unformatSaaBkForEdit(getProject(), value);
            }
            liste.add(value);
        }
        return liste;
    }

    @Override
    protected SimpleSelectSearchResultGrid.GuiTableRowWithAttributes createRow(DBDataObjectAttributes attributes) {
        // attributes enthält nur die Attribute für die Suchtabelle TABLE_DA_PRODUCT_SAS
        // -> passende Attribute von TABLE_DA_PRODUCT, TABLE_DA_SA und TABLE_DA_KGTU_AS hinzufügen (kann dann auch in den Ergebnissen
        // angezeigt werden)
        iPartsNumberHelper helper = new iPartsNumberHelper();
        for (String tableName : additionalFields.keySet()) {
            Set<String> fieldNames = additionalFields.get(tableName);
            for (String fieldName : fieldNames) {
                addExtraAttributes(tableName, fieldName, attributes);
            }
        }
        calculateVirtualFieldValues(attributes, helper);

        return super.createRow(attributes);
    }

    @Override
    protected boolean doValidateAttributes(DBDataObjectAttributes attributes) {
        String source = attributes.getFieldValue(FIELD_DS_SOURCE);
        return iPartsImportDataOrigin.isSourceVisible(source, isCarAndVanInSession(), isTruckAndBusInSession());
    }

    public void calculateVirtualFieldValues(DBDataObjectAttributes attributes, iPartsNumberHelper helper) {
        for (String virtField : additionalVirtualFields) {
            if (VirtualFieldsUtils.isVirtualField(virtField)) {
//                String tablename;
//                if (helper.isValidSaa(attributes.getField(FIELD_DA_ESM_SAA_NO).getAsString())) {
//                    tablename = TABLE_DA_SAA;
//                } else {
//                    tablename = TABLE_MAT;
//                }
//                addExtraAttributes(tablename, virtualFieldMapping.get(makeVirtualMappinKey(tablename, virtField)), virtField, attributes);
            }
        }
    }

    private void addExtraAttributes(String tableName, String fieldName, DBDataObjectAttributes attributes) {
        addExtraAttributes(tableName, fieldName, "", attributes);
    }

    private void addExtraAttributes(String tableName, String fieldName, String virtFieldName, DBDataObjectAttributes attributes) {
        IdWithType id = getIdForTable(tableName, attributes);
        if (id == null) {
            return;
        }
        EtkDataObject dataObject = dataObjectCache.get(id);
        if (dataObject == null) {
            if (tableName.equals(TABLE_DA_PRODUCT) && id.getType().equals(iPartsProductId.TYPE)) {
                dataObject = new iPartsDataProduct(getProject(), (iPartsProductId)id);
            } else if (tableName.equals(TABLE_DA_KGTU_AS) && id.getType().equals(iPartsDataKgTuAfterSalesId.TYPE)) {
                dataObject = new iPartsDataKgTuAfterSales(getProject(), (iPartsDataKgTuAfterSalesId)id);
            }
            dataObjectCache.put(id, dataObject);
        }
        loadFieldValue(dataObject, fieldName);
        assignAttributeValues(dataObject, fieldName, (StrUtils.isEmpty(virtFieldName) ? fieldName : virtFieldName), attributes);
    }

    /**
     * Gibt in Abhängigkeit der übergebenen Tabelle die zugehörige {@link IdWithType} zurück
     *
     * @param tableName
     * @param attributes
     * @return
     */
    private IdWithType getIdForTable(String tableName, DBDataObjectAttributes attributes) {
//        if (tableName.equals(TABLE_DA_PRODUCT)) {
//            return new iPartsProductId(attributes.getField(FIELD_DPS_PRODUCT_NO).getAsString());
//        } else if (tableName.equals(TABLE_DA_KGTU_AS)) {
//            return new iPartsDataKgTuAfterSalesId(attributes.getFieldValue(FIELD_DPS_PRODUCT_NO), attributes.getFieldValue(FIELD_DPS_KG), "");
//        }
        return null;
    }

    /**
     * Weist das vorgegeben Attribut aus {@link EtkDataObject} der übergebenen Liste von Attributen zu ({@link DBDataObjectAttributes}).
     * Ist das Zielfeld ein virtuelles Feld, dann muss der Feldname des virtuelle Feldes ebenfalls übergeben werden
     *
     * @param dataObject
     * @param fieldName
     * @param virtFieldName
     * @param attributes
     */
    private void assignAttributeValues(EtkDataObject dataObject, String fieldName, String virtFieldName, DBDataObjectAttributes attributes) {
        if (dataObject != null) {
            String destField = StrUtils.isEmpty(virtFieldName) ? fieldName : virtFieldName;
            DBDataObjectAttribute attribute = dataObject.getAttributes().getField(fieldName, false);
            DBDataObjectAttribute destAttribute = attributes.getField(destField, false);
            if ((attribute != null) && (destAttribute != null)) {
                destAttribute.assign(attribute);
            }
        }
    }

    /**
     * Lädt die Werte für das übergebene Feld
     *
     * @param dataObject
     * @param fieldName
     */
    private void loadFieldValue(EtkDataObject dataObject, String fieldName) {
        if (dataObject.existsInDB()) {
            DBDataObjectAttribute attribute = dataObject.getAttributes().getField(fieldName, false);
            if (attribute != null) {
                if (attribute.getType() == DBDataObjectAttribute.TYPE.MULTI_LANGUAGE) {
                    attribute.getAsMultiLanguage(dataObject, false);
                } else if (attribute.getType() == DBDataObjectAttribute.TYPE.ARRAY) {
                    attribute.getAsArray(dataObject);
                }
            }
        } else {
            dataObject.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        }
    }

    @Override
    protected void doEditOrView(Event event) {
        endSearch();
        DBDataObjectAttributes attributes = getSelectedAttributes();
        if (attributes != null) {
            IdWithType id = buildIdFromAttributes(attributes);
            EditUserControlForSAorSAA eCtrl = new EditUserControlForSAorSAA(getConnector(), this, searchTable,
                                                                            id, attributes, editFields,
                                                                            EnumSet.of(DictTextKindTypes.SA_NAME));
            boolean editAndModifyAllowed = isEditAllowed() && isModifyAllowed();
            eCtrl.setReadOnly(!editAndModifyAllowed);
            String key = "!!Stammdaten für SA \"%1\" bearbeiten";
            if (!editAndModifyAllowed) {
                key = "!!Stammdaten für SA \"%1\" anzeigen";
            }

            eCtrl.setTitle(TranslationHandler.translate(key, iPartsNumberHelper.formatPartNo(getProject(), id.getValue(1))));
            eCtrl.setWindowName(editControlsWindowName);
            eCtrl.handleSpecialMultiLangFields(FIELD_DS_DESC);

            if (eCtrl.showModal() == ModalResult.OK) {
                if (onEditChangeRecordEvent != null) {
                    if (onEditChangeRecordEvent.onEditModifyRecordEvent(getConnector(), searchTable, id, eCtrl.getAttributes())) {
                        GuiTableRow row = getTable().getSelectedRow();
                        List<Integer> columnIndices = new DwList<>();
                        for (EtkEditField editField : editFields.getVisibleEditFields()) {
                            if (editField.isEditierbar()) {
                                String tableName = editField.getKey().getTableName();
                                String fieldName = editField.getKey().getFieldName();
                                int column = displayResultFields.getIndexOfVisibleFeld(tableName, fieldName,
                                                                                       editField.getKey().isUsageField());
                                if (column != -1) {
                                    String value = getVisualValueOfFieldValue(tableName, fieldName, attributes.getField(fieldName), editField.isMultiLanguage());
                                    GuiLabel label = new GuiLabel(value);
                                    row.replaceChild(column, label);
                                    columnIndices.add(column);
                                }
                            }
                        }
                        if (!columnIndices.isEmpty()) {
                            int[] selectedRowNos = getTable().getSelectedRowIndices();
                            getTable().updateFilterAndSortForModifiedColumns(columnIndices);
                            if (getTable().getRowCount() > 0) {
                                getTable().setSelectedRows(selectedRowNos, false, true);
                            }
                        }
                        enableButtons();
                    }
                }
            }
        }
    }

}
