/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.forms.common.EditControlFactory;
import de.docware.apps.etk.base.forms.common.SimpleSelectSearchResultGrid;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDbObjectsLayer;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.filter.EtkFilterTyp;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetSource;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSaa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSAAModels;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsSAAModelsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.OnEditChangeRecordEvent;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.config.db.EtkFieldType;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.table.HtmlTablePageSplitMode;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogButtons;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogIcon;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;
import org.apache.commons.collections4.map.LRUMap;

import java.util.*;

/**
 * Formular für die Anzeige der Stammdaten von SAA/BK-Gültigkeiten zu Baumuster (Tabelle TABLE_DA_EDS_SAA_MODELS).
 */
public class MasterDataSAAsModelsForm extends SimpleMasterDataSearchFilterGrid {

    private static int MIN_DIGITS_SEARCH = 4;

    public static void showSAAsModelsMasterData(AbstractJavaViewerForm owner) {
        // Aktive Form holen
        AbstractJavaViewerForm activeForm = owner.getConnector().getActiveForm();
        showSAAsModelsMasterData(activeForm.getConnector(), activeForm, null);
    }

    public static void showSAAsModelsMasterData(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                OnEditChangeRecordEvent onEditChangeRecordEvent) {
        if (onEditChangeRecordEvent == null) {
            // Falls nicht extern definiert: Hier die Callbacks für Edit, Modify und Delete definieren
            onEditChangeRecordEvent = new OnEditChangeRecordEvent() {
                private boolean onEditCreateOrModifyRecordEvent(AbstractJavaViewerFormIConnector dataConnector,
                                                                IdWithType id, DBDataObjectAttributes attributes,
                                                                boolean createRecord) {
                    EtkProject project = dataConnector.getProject();

                    // trim(), toUpperCase() und Formatierung der SAA entfernen solange es reine Edit-Felder sind
                    String saaNumber = new iPartsNumberHelper().unformatSaaBkForEdit(dataConnector.getProject(), id.getValue(1).trim().toUpperCase());
                    String modelNumber = id.getValue(2).trim().toUpperCase();

                    // Check, ob das BM zu den Eigenschaften des Benutzers passt
                    if (!isModelForCreateOrDeleteValid(dataConnector.getProject(), modelNumber)) {
                        MessageDialog.show(TranslationHandler.translate("!!Das Anlegen der SAA/BK-Gültigkeit zum " +
                                                                        "Baumuster \"%1\" ist mit den aktuellen " +
                                                                        "Benutzereigenschaften nicht zulässig!",
                                                                        modelNumber), "!!Erzeugen");

                        return false;
                    }

                    iPartsSAAModelsId saaModelsId = new iPartsSAAModelsId(saaNumber, modelNumber);

                    for (DBDataObjectAttribute attrib : attributes.getFields()) {
                        attrib.setValueAsString(attrib.getAsString().toUpperCase(), DBActionOrigin.FROM_DB);
                    }

                    iPartsDataSAAModels dataSAAModels = new iPartsDataSAAModels(project, saaModelsId);
                    if (dataSAAModels.loadFromDB(saaModelsId) && createRecord) {
                        String msg = "!!Die SAA/BK-Gültigkeit zu Baumuster ist bereits vorhanden und kann nicht neu angelegt werden!";
                        MessageDialog.show(msg, "!!Erzeugen", MessageDialogIcon.ERROR, MessageDialogButtons.OK);
                        return true;
                    }
                    if (createRecord) {
                        dataSAAModels.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                    }
                    dataSAAModels.assignAttributesValues(project, attributes, true, DBActionOrigin.FROM_EDIT);

                    // ID erneut setzen, weil ansonsten die formatierte SAA in den Attributen landen könnte
                    dataSAAModels.setId(saaModelsId, DBActionOrigin.FROM_EDIT);
                    dataSAAModels.updateOldId();

                    project.getDbLayer().startTransaction();
                    try {
                        if (iPartsRevisionChangeSet.saveDataObjectWithChangeSet(project, dataSAAModels, iPartsChangeSetSource.SAA_MODEL)) {
                            dataSAAModels.saveToDB();
                            project.getDbLayer().commit();

                            // Geändertes Baumuster (Produkte sind nicht notwendig) aus dem Cache löschen falls dort vorhanden
                            ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.MODEL,
                                                                                                                      iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                                                      new iPartsModelId(dataSAAModels.getAsId().getModelNumber()),
                                                                                                                      false));

                            return true;
                        } else {
                            project.getDbLayer().rollback();
                            return false;
                        }
                    } catch (Exception e) {
                        project.getDbLayer().rollback();
                        Logger.getLogger().handleRuntimeException(e);
                    }
                    return false;
                }

                /**
                 * Check, ob das übergebene Baumuster für die Neuanlage oder das Löschen gültig ist.
                 *
                 * @param project
                 * @param modelNumber
                 * @return
                 */
                private boolean isModelForCreateOrDeleteValid(EtkProject project, String modelNumber) {
                    iPartsDataModel model = new iPartsDataModel(project, new iPartsModelId(modelNumber));
                    if (model.existsInDB()) {
                        String source = model.getFieldValue(FIELD_DM_SOURCE);
                        return iPartsFilterHelper.isASModelVisibleForUserInSession(modelNumber, source,
                                                                                   iPartsRight.checkCarAndVanInSession(),
                                                                                   iPartsRight.checkTruckAndBusInSession(),
                                                                                   project);
                    }
                    return true;
                }

                @Override
                public boolean onEditCreateRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName, IdWithType id, DBDataObjectAttributes attributes) {
                    return onEditCreateOrModifyRecordEvent(dataConnector, id, attributes, true);
                }

                @Override
                public boolean onEditModifyRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName, IdWithType id, DBDataObjectAttributes attributes) {
                    return onEditCreateOrModifyRecordEvent(dataConnector, id, attributes, false);
                }

                @Override
                public boolean onEditAskForDelete(AbstractJavaViewerFormIConnector dataConnector, String tableName, DBDataObjectAttributesList attributeList) {
                    if ((attributeList != null) && !attributeList.isEmpty()) {
                        String msg = "!!Wollen Sie die selektierte SAA/BK-Gültigkeit zu Baumuster wirklich löschen?";
                        if (attributeList.size() > 1) {
                            msg = "!!Wollen Sie die selektierten SAA/BK-Gültigkeiten zu Baumuster wirklich löschen?";
                        }
                        if (MessageDialog.show(msg, "!!Löschen", MessageDialogIcon.CONFIRMATION,
                                               MessageDialogButtons.YES, MessageDialogButtons.NO) == ModalResult.YES) {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public boolean onEditDeleteRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName, DBDataObjectAttributesList attributesList) {
                    if ((attributesList != null) && !attributesList.isEmpty()) {
                        EtkProject project = dataConnector.getProject();
                        // Zur Sicherheit wird hier geprüft, ob die Baumuster zu den Eigenschaften des Benutzers passen.
                        // Eigentlich sollten hier keine Einträge auftauchen, die nicht zu den Eigenschaften passen, da
                        // sie schon bei der Anzeige gefiltert werden.
                        List<DBDataObjectAttributes> validAttributes = filterValidAttributesForDeleteWithInfoMessage(project,
                                                                                                                     attributesList);
                        if (validAttributes.isEmpty()) {
                            return false;
                        }
                        EtkDbObjectsLayer dbLayer = project.getDbLayer();
                        dbLayer.startTransaction();
                        dbLayer.startBatchStatement();
                        try {
                            List<iPartsModelId> modelIds = new DwList<>();
                            for (DBDataObjectAttributes attributes : validAttributes) {
                                iPartsModelId modelId = new iPartsModelId(attributes.getField(FIELD_DA_ESM_MODEL_NO).getAsString());
                                modelIds.add(modelId);
                                iPartsSAAModelsId saaModelsId = new iPartsSAAModelsId(attributes.getField(FIELD_DA_ESM_SAA_NO).getAsString(),
                                                                                      modelId.getModelNumber());
                                iPartsDataSAAModels dataSAAModels = new iPartsDataSAAModels(project, saaModelsId);
                                if (iPartsRevisionChangeSet.deleteDataObjectWithChangeSet(project, dataSAAModels, iPartsChangeSetSource.SAA_MODEL)) {
                                    dataSAAModels.deleteFromDB(true);
                                } else {
                                    dbLayer.cancelBatchStatement();
                                    dbLayer.rollback();
                                    return false;
                                }
                            }
                            dbLayer.endBatchStatement();
                            dbLayer.commit();

                            // Geänderte Baummuster (Produkte sind nicht notwendig) aus dem Cache löschen falls dort vorhanden
                            ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.MODEL,
                                                                                                                      iPartsDataChangedEventByEdit.Action.DELETED,
                                                                                                                      modelIds,
                                                                                                                      false));

                            return true;
                        } catch (Exception e) {
                            dbLayer.cancelBatchStatement();
                            dbLayer.rollback();
                            Logger.getLogger().handleRuntimeException(e);
                        }
                    }
                    return false;
                }

                /**
                 * Überprüft, ob die Datensätze, die gelöscht werden sollen, zu den Eigenschaften des Benutzers passen
                 *
                 * @param project
                 * @param attributesList
                 * @return
                 */
                private List<DBDataObjectAttributes> filterValidAttributesForDeleteWithInfoMessage(EtkProject project,
                                                                                                   DBDataObjectAttributesList attributesList) {
                    List<DBDataObjectAttributes> validAttributes = new ArrayList<>();
                    StringBuilder builder = new StringBuilder();
                    attributesList.forEach(attributes -> {
                        String modelNumber = attributes.getFieldValue(FIELD_DA_ESM_MODEL_NO);
                        if (!isModelForCreateOrDeleteValid(project, modelNumber)) {
                            if (builder.length() != 0) {
                                builder.append(", ");
                            }
                            builder.append(modelNumber);
                        } else {
                            validAttributes.add(attributes);
                        }
                    });
                    if (builder.length() > 0) {
                        MessageDialog.show(TranslationHandler.translate("!!Das Löschen der SAA/BK-Gültigkeit zum " +
                                                                        "Baumuster \"%1\" ist mit den aktuellen " +
                                                                        "Benutzereigenschaften nicht zulässig!",
                                                                        builder.toString()), "!!Löschen");
                    }
                    return validAttributes;
                }
            };
        }

        MasterDataSAAsModelsForm dlg = new MasterDataSAAsModelsForm(dataConnector, parentForm, TABLE_DA_EDS_SAA_MODELS, onEditChangeRecordEvent);
        EtkProject project = dataConnector.getProject();

        // Suchfelder laden
        EtkDisplayFields searchFields = new EtkDisplayFields();
        searchFields.load(dataConnector.getConfig(), iPartsEditConfigConst.iPARTS_EDIT_MASTER_SAA_MODEL_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_SEARCHFIELDS,
                          dataConnector.getConfig().getCurrentDatabaseLanguage());
        if (searchFields.size() == 0) {
            //Suchfelder definieren
            addSearchField(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_MODEL_NO, "!!Baumusternummer", project, searchFields);
            addSearchField(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_SAA_NO, "!!SAA/BK Nummer", project, searchFields);
        }

        // Anzeigefelder laden
        EtkDisplayFields displayFields = new EtkDisplayFields();
        displayFields.load(dataConnector.getConfig(), iPartsEditConfigConst.iPARTS_EDIT_MASTER_SAA_MODEL_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS);
        if (displayFields.size() == 0) {
            // Anzeigefelder definieren
            EtkDisplayField displayField = addDisplayField(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_MODEL_NO, false, false, "!!Baumusternummer", project, displayFields);
            displayField.setColumnFilterEnabled(true);
            addDisplayField(TABLE_DA_MODEL, FIELD_DM_SALES_TITLE, true, false, "!!Baumusterbenennung", project, displayFields);
            displayField = addDisplayField(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_SAA_NO, false, false, "!!SAA/BK Nummer", project, displayFields);
            displayField.setColumnFilterEnabled(true);
            // virtuelle Anzeigefelder
            addDisplayField(TABLE_DA_EDS_SAA_MODELS, iPartsDataVirtualFieldsDefinition.DESM_DESCRIPTION, true, false, "!!SAA/BK Benennung", project, displayFields);

            displayField = addDisplayField(TABLE_DA_MODEL, FIELD_DM_SOURCE, false, false, "!!Quelle", project, displayFields);
            displayField.setColumnFilterEnabled(true);
        }

        // Benötigte Ergebnisfelder (abgesehen von den Primärschlüsselfeldern) definieren
        EtkDisplayFields requiredResultFields = new EtkDisplayFields();
        requiredResultFields.addFeld(createDisplayField(project, TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_MODEL_NO, false, false));
        requiredResultFields.addFeld(createDisplayField(project, TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_SAA_NO, false, false));
        requiredResultFields.addFeld(createDisplayField(project, TABLE_DA_MODEL, FIELD_DM_SOURCE, false, false)); // Wichtig für die Zuordnung zu den Benutzereigenschaften

        // Editfelder fürs Editieren laden
        EtkEditFields editFields = new EtkEditFields();
        editFields.load(dataConnector.getConfig(), iPartsEditConfigConst.iPARTS_EDIT_MASTER_SAA_MODEL_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_EDITFIELDS);
        if (editFields.size() == 0) {
            // Editfelder fürs Editieren festlegen
            addEditField(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_MODEL_NO, false, "!!Baumusternummer", project, editFields);
            addEditField(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_SAA_NO, false, "!!SAA/BK Nummer", project, editFields);
        }
        EtkDatabaseTable tableDef = dataConnector.getConfig().getDBDescription().findTable(TABLE_DA_EDS_SAA_MODELS);
        List<String> pkFields = tableDef.getPrimaryKeyFields();
        for (EtkEditField eField : editFields.getFields()) {
            if (pkFields.contains(eField.getKey().getFieldName())) {
                eField.setMussFeld(true);
                eField.setEditierbar(false);  // ist PK-Value => nicht editierbar
            }
        }

        // Sortierung
        LinkedHashMap<String, Boolean> sortFields = new LinkedHashMap<>();
        sortFields.put(FIELD_DA_ESM_SAA_NO, false);
        sortFields.put(FIELD_DA_ESM_MODEL_NO, false);
        dlg.setSortFields(sortFields);
        dlg.setMaxSearchControlsPerRow(3);
        dlg.setDisplayResultFields(displayFields);
        dlg.setRequiredResultFields(requiredResultFields);
        dlg.setSearchFields(searchFields);
        dlg.setEditFields(editFields);
        boolean editVehiclePartsDataAllowed = iPartsRight.EDIT_VEHICLE_PARTS_DATA.checkRightInSession();
        dlg.setEditAllowed(editVehiclePartsDataAllowed);
        dlg.setNewAllowed(editVehiclePartsDataAllowed);
        dlg.setModifyAllowed(false);
        dlg.setDeleteAllowed(editVehiclePartsDataAllowed);
        dlg.setTitlePrefix("!!SAA/BK-Gültigkeiten zu Baumuster");
        dlg.setWindowName("SAAsModelsMasterData");

        dlg.showModal();
    }

    private final Map<IdWithType, EtkDataObject> dataObjectCache;
    private final Map<String, Set<String>> additionalFields;
    private final Set<String> additionalVirtualFields;
    private final iPartsNumberHelper numberHelper;
    private final Map<String, Boolean> modelVisibleInSessionMap;
    private Map<String, String> virtualFieldMapping;

    /**
     * Erzeugt eine Instanz von SimpleMasterDataSearchResultGrid.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public MasterDataSAAsModelsForm(AbstractJavaViewerFormIConnector dataConnector, final AbstractJavaViewerForm parentForm, String tableName, OnEditChangeRecordEvent onEditChangeRecordEvent) {
        super(dataConnector, parentForm, tableName, onEditChangeRecordEvent);
        doResizeWindow(SCREEN_SIZES.MAXIMIZE);
        dataObjectCache = new LRUMap(iPartsConst.MAX_CACHE_SIZE_MASTER_DATA);
        additionalFields = new LinkedHashMap<>();
        additionalVirtualFields = new HashSet<>();
        numberHelper = new iPartsNumberHelper();
        modelVisibleInSessionMap = new HashMap<>();
        setColumnFilterFactory(new SimpleMasterDataSearchFilterSAAsModelsFactory(getProject()));
        setOnStartSearchEvent(dataObjectCache::clear);
        initVirtualMapping();

        // SAA aus der aktuellen Selektion vorbelegen (falls vorhanden)
        setOnCreateEvent(() -> {
            DBDataObjectAttributes selectedAttributes = getSelection();
            if (selectedAttributes != null) {
                DBDataObjectAttributes initialAttributes = new DBDataObjectAttributes();

                // SAA formatieren und vorbelegen
                String formattedSAANumber = iPartsNumberHelper.formatPartNo(getProject(), selectedAttributes.getFieldValue(FIELD_DA_ESM_SAA_NO));
                initialAttributes.addField(FIELD_DA_ESM_SAA_NO, formattedSAANumber, DBActionOrigin.FROM_DB);

                return initialAttributes;
            }

            return null;
        });

        setOnEditOrViewEvent((existingAttributes, editAllowed) -> {
            // SAA formatieren
            DBDataObjectAttributes attributesWithFormattedSAA = existingAttributes.cloneMe(DBActionOrigin.FROM_DB);
            String formattedSAANumber = iPartsNumberHelper.formatPartNo(getProject(), attributesWithFormattedSAA.getFieldValue(FIELD_DA_ESM_SAA_NO));
            attributesWithFormattedSAA.addField(FIELD_DA_ESM_SAA_NO, formattedSAANumber, DBActionOrigin.FROM_DB);
            return attributesWithFormattedSAA;
        });

        // Table in den Page-Mode setzen
        getTable().setPageSplitNumberOfEntriesPerPage(J2EEHandler.isJ2EE() ? MAX_SELECT_SEARCH_RESULTS_SIZE : -1);
        getTable().setHtmlTablePageSplitMode(HtmlTablePageSplitMode.BUTTONS);
    }

    /**
     * Mapping für virtuelle Felder (Abhängigkeit bezüglich BK und SAA)
     */
    private void initVirtualMapping() {
        virtualFieldMapping = new HashMap<>();
        virtualFieldMapping.put(makeVirtualMappinKey(TABLE_DA_SAA, iPartsDataVirtualFieldsDefinition.DESM_DESCRIPTION), FIELD_DS_DESC);
        virtualFieldMapping.put(makeVirtualMappinKey(TABLE_DA_SAA, iPartsDataVirtualFieldsDefinition.DESM_DESCRIPTION_CONST), FIELD_DS_CONST_DESC);
        virtualFieldMapping.put(makeVirtualMappinKey(TABLE_MAT, iPartsDataVirtualFieldsDefinition.DESM_DESCRIPTION), FIELD_M_TEXTNR);
        virtualFieldMapping.put(makeVirtualMappinKey(TABLE_MAT, iPartsDataVirtualFieldsDefinition.DESM_DESCRIPTION_CONST), FIELD_M_CONST_DESC);

    }

    private String makeVirtualMappinKey(String tablename, String virtualField) {
        return tablename + "||" + virtualField;
    }

    @Override
    public void setDisplayResultFields(EtkDisplayFields displayResultFields) {
        super.setDisplayResultFields(displayResultFields);
        // Felder zu den additionalFields und den additionalVirtualFields hinzufügen, damit wir die Werte nachladen können
        addAdditionalFields(displayResultFields);
    }

    @Override
    public void setRequiredResultFields(EtkDisplayFields requiredResultFields) {
        super.setRequiredResultFields(requiredResultFields);
        // Felder zu den additionalFields und den additionalVirtualFields hinzufügen, damit wir die Werte nachladen können
        addAdditionalFields(requiredResultFields);
    }

    private void addAdditionalFields(EtkDisplayFields fieldsToCheck) {
        additionalFields.clear();
        for (EtkDisplayField displayField : fieldsToCheck.getFields()) {
            String tablename = displayField.getKey().getTableName();
            String fieldname = displayField.getKey().getFieldName();
            if (!tablename.equals(TABLE_DA_EDS_SAA_MODELS)) {
                Set<String> fieldNames = additionalFields.computeIfAbsent(tablename, k -> new HashSet<>());
                fieldNames.add(fieldname);
            } else if (VirtualFieldsUtils.isVirtualField(fieldname)) {
                addVirtualDisplayField(fieldname);
            }
        }
    }

    @Override
    protected List<String> getSearchValues() {
        List<String> liste = new DwList<>();
        for (int lfdNr = 0; lfdNr < searchFields.size(); lfdNr++) {
            EditControlFactory ctrl = editControls.getControlByFeldIndex(lfdNr).getEditControl();
            String value = ctrl.getText();
            if (TableAndFieldName.make(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_SAA_NO).equals(ctrl.getTableFieldName())) {
                // Sonderbehandlung für SAA/BK Nummern
                value = numberHelper.unformatSaaBkForEdit(getProject(), value);
            }
            liste.add(value);
        }
        return liste;
    }

    @Override
    protected boolean checkControlChange() {
        boolean isEnabled = super.checkControlChange();
        if (isEnabled) {
            isEnabled = false;
            for (int i = 0; i < searchFields.getVisibleFields().size(); i++) {
                EditControl searchControl = editControls.getControlByFeldIndex(i);
                if (searchControl != null) {
                    EditControlFactory searchEditControl = searchControl.getEditControl();
                    // Suchtext ohne Wildcards soll in mindestens einem relevanten Feld mindestens MIN_DIGITS_SEARCH lang sein
                    if (searchEditControl.getTableFieldName().equals(TableAndFieldName.make(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_MODEL_NO)) ||
                        searchEditControl.getTableFieldName().equals(TableAndFieldName.make(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_SAA_NO))) {
                        String value = searchEditControl.getText().trim();
                        if (StrUtils.isValid(value)) {
                            if (value.replace("*", "").replace("?", "").length() >= MIN_DIGITS_SEARCH) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return isEnabled;
    }

    @Override
    protected SimpleSelectSearchResultGrid.GuiTableRowWithAttributes createRow(DBDataObjectAttributes attributes) {
        // attributes enthält nur die Attribute für die Suchtabelle TABLE_DA_EDS_SAA_MODELS
        // -> passende Attribute von TABLE_DA_MODEL und TABLE_DA_SAA hinzufügen (kann dann auch in den Ergebnissen
        // angezeigt werden)
        for (String tableName : additionalFields.keySet()) {
            Set<String> fieldNames = additionalFields.get(tableName);
            for (String fieldName : fieldNames) {
                addExtraAttributes(tableName, fieldName, attributes);
            }
        }
        calculateVirtualFieldValues(attributes);

        // Check, ob die Zeile überhaupt gültig ist und angezeigt werden darf
        if (!checkIfRowIsVisible(attributes)) {
            return null;
        }

        return super.createRow(attributes);
    }

    /**
     * Überprüft, ob die Attribute der übergebenen Zeile gültig sind und angezeigt werden dürfen
     *
     * @param attributes
     * @return
     */
    private boolean checkIfRowIsVisible(DBDataObjectAttributes attributes) {
        String modelNo = attributes.getFieldValue(FIELD_DA_ESM_MODEL_NO);
        return modelVisibleInSessionMap.computeIfAbsent(modelNo, modelNumber -> {
            String source = attributes.getFieldValue(FIELD_DM_SOURCE);
            return iPartsFilterHelper.isASModelVisibleForUserInSession(modelNumber, source,
                                                                       isCarAndVanInSession(), isTruckAndBusInSession(),
                                                                       getProject());
        });
    }

    private void addExtraAttributes(String tableName, String fieldName, DBDataObjectAttributes attributes) {
        addExtraAttributes(tableName, fieldName, "", attributes);
    }

    public void addVirtualDisplayField(String... virtFields) {
        Collections.addAll(additionalVirtualFields, virtFields);
    }

    private void addExtraAttributes(String tableName, String fieldName, String virtFieldName, DBDataObjectAttributes attributes) {
        IdWithType id = getIdForTable(tableName, attributes);
        if (id == null) {
            return;
        }
        EtkDataObject dataObject = dataObjectCache.get(id);
        if (dataObject == null) {
            if (tableName.equals(TABLE_DA_MODEL) && id.getType().equals(iPartsModelId.TYPE)) {
                dataObject = new iPartsDataModel(getProject(), (iPartsModelId)id);
            } else if (tableName.equals(TABLE_DA_SAA) && id.getType().equals(iPartsSaaId.TYPE)) {
                dataObject = new iPartsDataSaa(getProject(), (iPartsSaaId)id);
            } else if (tableName.equals(TABLE_MAT) && id.getType().equals(iPartsPartId.TYPE)) {
                dataObject = EtkDataObjectFactory.createDataPart(getProject(), (PartId)id);
            }
            if (dataObject == null) {
                return;
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
        if (tableName.equals(TABLE_DA_MODEL)) {
            return new iPartsModelId(attributes.getField(FIELD_DA_ESM_MODEL_NO).getAsString());
        } else if (tableName.equals(TABLE_DA_SAA)) {
            return new iPartsSaaId(attributes.getField(FIELD_DA_ESM_SAA_NO).getAsString());
        } else if (tableName.equals(TABLE_MAT)) {
            return new iPartsPartId(attributes.getField(FIELD_DA_ESM_SAA_NO).getAsString(), "");
        }
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
            if (attribute != null) {
                if (destAttribute != null) {
                    destAttribute.assign(attribute);
                } else {
                    destAttribute = new DBDataObjectAttribute(attribute);
                    destAttribute.__internal_setName(destField);
                    attributes.addField(destAttribute, DBActionOrigin.FROM_DB);
                }
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

    public void calculateVirtualFieldValues(DBDataObjectAttributes attributes) {
        for (String virtField : additionalVirtualFields) {
            if (VirtualFieldsUtils.isVirtualField(virtField)) {
                String tablename;
                if (numberHelper.isValidSaa(attributes.getField(FIELD_DA_ESM_SAA_NO).getAsString())) {
                    tablename = TABLE_DA_SAA;
                } else {
                    tablename = TABLE_MAT;
                }
                addExtraAttributes(tablename, virtualFieldMapping.get(makeVirtualMappinKey(tablename, virtField)), virtField, attributes);
            }
        }
    }

    public class SimpleMasterDataSearchFilterSAAsModelsFactory extends SimpleMasterDataSearchFilterFactory {

        public SimpleMasterDataSearchFilterSAAsModelsFactory(EtkProject project) {
            super(project);
        }

        /**
         * Überlagerung wegen SAAs/BK spezieller Formatierung
         *
         * @param column
         * @param filterControl
         * @return
         */
        @Override
        public Object getFilterValue(int column, AbstractGuiControl filterControl) {
            if (filterControl != null) {
                EditControlFactory editControl = getFilterEditForColumn(column);
                if (editControl != null) {
                    if (editControl.getTableFieldName().equals(TableAndFieldName.make(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_SAA_NO))) {
                        String filterValue = editControl.getText();
                        if (filterValue.isEmpty()) {
                            return null;
                        }
                        EtkFilterTyp filterTyp = getFilterTypByName(getFilterName(column, editControl.getTableFieldName()));
                        if (filterTyp != null) {
                            filterTyp.getFilterValues().clear();

                            filterValue = numberHelper.unformatSaaBkForDB(getProject(), filterValue, true);
                            EtkFieldType fieldType = getProject().getFieldDescription(editControl.getTableName(), editControl.getFieldName()).getType();
                            EtkDisplayField whereField = getDisplayFieldList().get(column - doCalculateStartColumnIndexForDisplayFields());
                            if (fieldType.isWildCardType() && !whereField.isSearchExact()) {
                                //eigenes WildCardSetting mit * am Ende
                                WildCardSettings wildCardSettings = new WildCardSettings();
                                wildCardSettings.addWildCardEnd();
                                wildCardSettings.addSpaceToWildCard();
                                filterValue = wildCardSettings.makeWildCard(filterValue);
                            }
                            filterTyp.addFilterValue(filterValue);
                            filterTyp.setActive(true);
                            return filterControl;
                        }
                    }
                }
            }
            return super.getFilterValue(column, filterControl);
        }

        @Override
        protected List<SimpleSelectSearchResultGrid.GuiTableRowWithAttributes> calculateFilteredList() {
            List<SimpleSelectSearchResultGrid.GuiTableRowWithAttributes> entries;
            if (getAssemblyListFilter().isDBFilterActive()) {
                entries = new DwList<>();
                Set<String> tableNames = new TreeSet<>();
                Collection<EtkFilterTyp> activeFilters = getAssemblyListFilter().getActiveFilter();
                for (EtkFilterTyp filterTyp : activeFilters) {
                    if (filterTyp.getFilterTypus() == EtkFilterTyp.FilterTypus.GRIDFILTER) {
                        String tableName = filterTyp.getName();
                        tableName = tableName.substring(tableName.indexOf('_') + 1);
                        tableNames.add(TableAndFieldName.getTableName(tableName));
                    }
                }

                String language = getProject().getViewerLanguage();
                //filtern
                for (SimpleSelectSearchResultGrid.GuiTableRowWithAttributes entry : getEntries()) {
                    boolean addEntry = true;
                    for (String tableName : tableNames) {
                        if (!getAssemblyListFilter().checkFilter(tableName, entry.attributes, language)) {
                            addEntry = false;
                            break;
                        }
                    }

                    if (addEntry) { // Eintrag wurde nicht ausgefiltert
                        entries.add(entry);
                    }
                }
            } else {
                entries = new DwList<>(getEntries());
            }
            return entries;
        }

    }
}
