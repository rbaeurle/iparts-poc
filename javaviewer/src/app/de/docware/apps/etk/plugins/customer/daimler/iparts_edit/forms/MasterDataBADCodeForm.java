/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFieldKeyNormal;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.forms.common.EditControls;
import de.docware.apps.etk.base.forms.common.SimpleSelectSearchResultGrid;
import de.docware.apps.etk.base.forms.events.OnCreateAttributesEvent;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDbObjectsLayer;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetSource;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsAAPartsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.OnEditChangeRecordEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EtkEditFieldHelper;
import de.docware.framework.modules.config.db.DBConst;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.controls.table.HtmlTablePageSplitMode;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogButtons;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogIcon;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.utils.EtkDataArray;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

import java.util.Arrays;
import java.util.List;

/**
 * Formular für die Anzeige der BAD_Code (Tabelle DA_BAD_CODE).
 */
public class MasterDataBADCodeForm extends SimpleMasterDataSearchFilterGrid {


    public static void showBADCodeForSeries(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                            final iPartsSeriesId seriesId) {

        OnEditChangeRecordEvent onEditChangeRecordEvent = new OnEditChangeRecordEvent() {
            @Override
            public boolean onEditCreateRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName, IdWithType id, DBDataObjectAttributes attributes) {
                return onEditCreateOrModifyRecordEvent(dataConnector, id, attributes, true);
            }

            @Override
            public boolean onEditModifyRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName, IdWithType id, DBDataObjectAttributes attributes) {
                return onEditCreateOrModifyRecordEvent(dataConnector, id, attributes, false);
            }

            private boolean onEditCreateOrModifyRecordEvent(AbstractJavaViewerFormIConnector dataConnector,
                                                            IdWithType id, DBDataObjectAttributes attributes, boolean createRecord) {
                iPartsBadCodeId badCodeId = new iPartsBadCodeId(id.getValue(1), id.getValue(2), id.getValue(3));
                EtkProject project = dataConnector.getProject();
                iPartsDataBadCode badCode = new iPartsDataBadCode(project, badCodeId);
                if (badCode.loadFromDB(badCodeId) && createRecord) {
                    String msg = "!!Der BAD-Code Datensatz ist bereits vorhanden und kann nicht neu angelegt werden!";
                    MessageDialog.show(msg, "!!Erzeugen", MessageDialogIcon.ERROR, MessageDialogButtons.OK);
                    return true;
                }

                badCode.assignAttributesValues(project, attributes, false, DBActionOrigin.FROM_EDIT);

                dataConnector.getProject().getDbLayer().startTransaction();
                try {
                    if (iPartsRevisionChangeSet.saveDataObjectWithChangeSet(dataConnector.getProject(), badCode, iPartsChangeSetSource.SERIES)) {
                        badCode.saveToDB();
                        dataConnector.getProject().getDbLayer().commit();
                        // Da durch das Ändern der BAD-Code etwas an der Baureihe geändert wurde, ist die Action immer MODIFIED (--> Löschen der Series-Caches)
                        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.SERIES,
                                                                                                                  iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                                                  seriesId, false));
                        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new DataChangedEvent());
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
            public boolean onEditAskForDelete(AbstractJavaViewerFormIConnector dataConnector, String tableName, DBDataObjectAttributesList attributeList) {
                String msg = "!!Wollen Sie den selektierten BAD-Code Datensatz wirklich löschen?";
                if (attributeList.size() > 1) {
                    msg = "!!Wollen Sie die selektierten BAD-Code Datensätze wirklich löschen?";
                }
                if (MessageDialog.show(msg, "!!Löschen", MessageDialogIcon.CONFIRMATION,
                                       MessageDialogButtons.YES, MessageDialogButtons.NO) == ModalResult.YES) {
                    return true;
                }
                return false;
            }

            @Override
            public boolean onEditDeleteRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName, DBDataObjectAttributesList attributesList) {
                if ((attributesList != null) && !attributesList.isEmpty()) {
                    EtkProject project = dataConnector.getProject();
                    EtkDbObjectsLayer dbLayer = project.getDbLayer();
                    dbLayer.startTransaction();
                    dbLayer.startBatchStatement();
                    try {
                        iPartsDataBadCodeList badCodeList = new iPartsDataBadCodeList();
                        for (DBDataObjectAttributes attributes : attributesList) {
                            iPartsBadCodeId badCodeId = new iPartsBadCodeId(attributes.getField(FIELD_DBC_SERIES_NO).getAsString(),
                                                                            attributes.getField(FIELD_DBC_AA).getAsString(),
                                                                            attributes.getField(FIELD_DBC_CODE_ID).getAsString());
                            iPartsDataBadCode badCode = new iPartsDataBadCode(project, badCodeId);
                            badCode.setAttributes(attributes, DBActionOrigin.FROM_DB);
                            badCodeList.delete(badCode, true, DBActionOrigin.FROM_DB);
                        }
                        if (iPartsRevisionChangeSet.deleteDataObjectListWithChangeSet(project, badCodeList, iPartsChangeSetSource.SERIES)) {
                            badCodeList.deleteFromDB(project, true);
                        } else {
                            dbLayer.cancelBatchStatement();
                            dbLayer.rollback();
                            return false;
                        }
                        dbLayer.endBatchStatement();
                        dbLayer.commit();
                        // Da durch das Löschen der BAD-Code etwas an der Baureihe geändert wurde, ist die Action immer MODIFIED (--> Löschen der Series-Caches)
                        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.SERIES,
                                                                                                                  iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                                                  seriesId, false));
                        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new DataChangedEvent());
                        return true;
                    } catch (Exception e) {
                        dbLayer.cancelBatchStatement();
                        dbLayer.rollback();
                        Logger.getLogger().handleRuntimeException(e);
                    }
                }
                return false;
            }
        };

        MasterDataBADCodeForm dlg = new MasterDataBADCodeForm(dataConnector, parentForm, TABLE_DA_BAD_CODE, onEditChangeRecordEvent);

        // Anzeigefelder definieren
        EtkDisplayFields displayFields = getDisplayFields(dataConnector);

        // Editfelder fürs Editieren festlegen
        EtkEditFields editFields = getEditFields(dataConnector);

        EtkDisplayFields searchFields = getSearchFields(dataConnector);

        dlg.setDisplayResultFields(displayFields);
        dlg.setEditFields(editFields);
        dlg.setSearchFields(searchFields);
        boolean editMasterDataAllowed = iPartsRight.EDIT_MASTER_DATA.checkRightInSession();
        boolean deleteMasterDataAllowed = iPartsRight.DELETE_MASTER_DATA.checkRightInSession();
        dlg.setEditAllowed(editMasterDataAllowed || deleteMasterDataAllowed);
        dlg.setNewAllowed(editMasterDataAllowed);
        dlg.setModifyAllowed(editMasterDataAllowed);
        dlg.setDeleteAllowed(editMasterDataAllowed || deleteMasterDataAllowed);
        dlg.showSearchFields(false);
        dlg.setMaxResults(-1);
        dlg.setTitlePrefix("!!BAD-Code");
        dlg.setWindowName("BADCodeMasterData");

        if ((seriesId != null) && seriesId.isValidId()) {
            dlg.setTitle(TranslationHandler.translate("!!BAD-Code zur Baureihe \"%1\"",
                                                      seriesId.getSeriesNumber()));
            // Suchwerte setzen und Suche starten
            DBDataObjectAttributes searchAttributes = new DBDataObjectAttributes();
            searchAttributes.addField(FIELD_DBC_SERIES_NO, seriesId.getSeriesNumber(), DBActionOrigin.FROM_DB);
            dlg.setSearchValues(searchAttributes);
            dlg.setSeriesId(seriesId);
        }
        dlg.doResizeWindow(SCREEN_SIZES.SCALE_FROM_PARENT);
        dlg.showModal();
    }


    private static EtkDisplayFields getDisplayFields(AbstractJavaViewerFormIConnector dataConnector) {
        // Anzeigefelder definieren
        EtkDisplayFields displayFields = new EtkDisplayFields();
        EtkProject project = dataConnector.getProject();
        displayFields.load(dataConnector.getConfig(), iPartsEditConfigConst.iPARTS_EDIT_MASTER_BAD_CODE_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS);
        if (displayFields.size() == 0) {
            EtkDatabaseTable table = dataConnector.getConfig().getDBDescription().getTable(TABLE_DA_BAD_CODE);
            for (EtkDatabaseField etkField : table.getFieldList()) {
                if (!etkField.getName().equals(DBConst.FIELD_STAMP)) {
                    EtkDisplayField displayField = addDisplayField(TABLE_DA_BAD_CODE, etkField.getName(), etkField.isMultiLanguage(),
                                                                   etkField.isArray(), null, project, displayFields);
                    if (displayField.getKey().getFieldName().equals(FIELD_DBC_AA) || displayField.getKey().getFieldName().equals(FIELD_DBC_CODE_ID)) {
                        displayField.setColumnFilterEnabled(true);
                    }
                }
            }
            addDisplayField(TABLE_DA_BAD_CODE, iPartsDataVirtualFieldsDefinition.DBC_BAD_CODE_EXPIRED,
                            false, false, "!!Abgelaufen", project, displayFields);
        }

        // Falls Feld DBC_BAD_CODE_EXPIRED vorhanden, soll es filterbar sein
        if (displayFields.contains(TableAndFieldName.make(TABLE_DA_BAD_CODE, iPartsDataVirtualFieldsDefinition.DBC_BAD_CODE_EXPIRED), false)) {
            displayFields.getFeldByName(TableAndFieldName.make(TABLE_DA_BAD_CODE, iPartsDataVirtualFieldsDefinition.DBC_BAD_CODE_EXPIRED), false).setColumnFilterEnabled(true);
        }
        return displayFields;
    }

    private static EtkEditFields getEditFields(AbstractJavaViewerFormIConnector dataConnector) {
        // Editfelder fürs Editieren festlegen
        EtkEditFields editFields = new EtkEditFields();
        EtkProject project = dataConnector.getProject();
        editFields.load(dataConnector.getConfig(), iPartsEditConfigConst.iPARTS_EDIT_MASTER_BAD_CODE_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_EDITFIELDS);
        if (editFields.size() == 0) {
            EtkEditFieldHelper.getEditFields(project, TABLE_DA_BAD_CODE, editFields, true);
        }
        iPartsDataBadCode dataBadCode = new iPartsDataBadCode(dataConnector.getProject(), null);
        for (String pkValue : dataBadCode.getPKFields()) {
            EtkEditField editField = editFields.getFeldByName(TABLE_DA_BAD_CODE, pkValue, false);
            if (editField != null) {
                editField.setMussFeld(true);
                editField.setEditierbar(!pkValue.equals(FIELD_DBC_SERIES_NO));
            }
        }
        return editFields;
    }

    private static EtkDisplayFields getSearchFields(AbstractJavaViewerFormIConnector dataConnector) {
        EtkDisplayFields searchFields = new EtkDisplayFields();
        if (searchFields.size() == 0) {
            // Suchfelder definieren
            searchFields.addFeld(createSearchField(dataConnector.getProject(), TABLE_DA_BAD_CODE, FIELD_DBC_SERIES_NO, false, false));
        }
        return searchFields;
    }

    private iPartsSeriesId seriesId;

    private MasterDataBADCodeForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName, OnEditChangeRecordEvent onEditChangeRecordEvent) {
        super(dataConnector, parentForm, tableName, onEditChangeRecordEvent);
        doResizeWindow(SCREEN_SIZES.MAXIMIZE);
    }

    public iPartsSeriesId getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(iPartsSeriesId seriesId) {
        this.seriesId = seriesId;
        if ((seriesId != null) && seriesId.isValidId()) {
            setOnCreateEvent(new OnCreateAttributesEvent() {
                @Override
                public DBDataObjectAttributes onCreateAttributesEvent() {
                    if ((getSeriesId() != null) && getSeriesId().isValidId()) {
                        DBDataObjectAttributes initialAttributes = new DBDataObjectAttributes();
                        DBDataObjectAttribute attr = new DBDataObjectAttribute(FIELD_DBC_SERIES_NO, DBDataObjectAttribute.TYPE.STRING, false);
                        attr.setValueAsString(getSeriesId().getSeriesNumber(), DBActionOrigin.FROM_DB);
                        initialAttributes.addField(attr, DBActionOrigin.FROM_DB);
                        return initialAttributes;
                    }
                    return null;
                }
            });
        } else {
            setOnCreateEvent(null);
        }
    }

    @Override
    public void setMaxResults(int maxResults) {
        super.setMaxResults(maxResults);
        if ((maxResults == -1) && J2EEHandler.isJ2EE()) {
            getTable().setPageSplitNumberOfEntriesPerPage(iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE);
        }
    }

    @Override
    public SimpleSelectSearchResultGrid.GuiTableRowWithAttributes addAttributesToGrid(DBDataObjectAttributes attributes) {
        attributes = calculateVirtualFieldValues(attributes);
        SimpleSelectSearchResultGrid.GuiTableRowWithAttributes row = super.addAttributesToGrid(attributes);
        if (J2EEHandler.isJ2EE()) {
            if ((getMaxResults() == -1) && getTable().getHtmlTablePageSplitMode() != HtmlTablePageSplitMode.BUTTONS) {
                int pageSplitNumberOfEntriesPerPage = getTable().getPageSplitNumberOfEntriesPerPage();
                if ((pageSplitNumberOfEntriesPerPage > 0) && (getTable().getRowCount() > pageSplitNumberOfEntriesPerPage)) {
                    getTable().setHtmlTablePageSplitMode(HtmlTablePageSplitMode.BUTTONS);
                }
            }
        }

        return row;
    }

    private DBDataObjectAttributes calculateVirtualFieldValues(DBDataObjectAttributes attributes) {
        EtkDisplayFields displayFields = getDisplayResultFields();
        if (displayFields != null) {
            EtkDisplayField badCodeExpiredField = displayFields.getFeldByName(TABLE_DA_BAD_CODE, iPartsDataVirtualFieldsDefinition.DBC_BAD_CODE_EXPIRED, false);
            if ((badCodeExpiredField != null) && badCodeExpiredField.isVisible()) {
                DBDataObjectAttribute attribute = new DBDataObjectAttribute(iPartsDataVirtualFieldsDefinition.DBC_BAD_CODE_EXPIRED, DBDataObjectAttribute.TYPE.STRING, true);
                String expiringDate = attributes.getFieldValue(FIELD_DBC_EXPIRY_DATE);
                attribute.setValueAsBoolean(iPartsDataBadCode.isExpired(expiringDate), DBActionOrigin.FROM_DB);
                attributes.addField(attribute, DBActionOrigin.FROM_DB);
            }
        }
        return attributes;
    }

    @Override
    protected void doNew(Event event) {
        endSearch();
        EtkDatabaseTable tableDef = getProject().getConfig().getDBDescription().findTable(searchTable);
        List<String> pkFields = tableDef.getPrimaryKeyFields();

        String[] emptyPkValues = new String[pkFields.size()];
        Arrays.fill(emptyPkValues, "");
        emptyPkValues[0] = getSeriesId().getSeriesNumber();
        IdWithType id = new IdWithType("xx", emptyPkValues);

        // Beim Neu anlegen sind alle Felder editierbar => deswegen Kopie
        EtkEditFields editNewFields = new EtkEditFields();
        editNewFields.assign(editFields);
        for (EtkEditField field : editNewFields.getFields()) {
            field.setEditierbar(!field.getKey().getFieldName().equals(FIELD_DBC_SERIES_NO));
        }

        DBDataObjectAttributes initialAttributes = null;
        if (onCreateEvent != null) {
            initialAttributes = onCreateEvent.onCreateAttributesEvent();
        }

        EditUserControlForCreateBadCode eCtrl = new EditUserControlForCreateBadCode(getConnector(), this, searchTable, id, initialAttributes, editNewFields);
        eCtrl.setTitle(titleForCreate);
        eCtrl.setWindowName(editControlsWindowName);
        if (eCtrl.showModal() == ModalResult.OK) {
            if (onEditChangeRecordEvent != null) {
                id = buildIdFromAttributes(eCtrl.getAttributes());
                if (onEditChangeRecordEvent.onEditCreateRecordEvent(getConnector(), searchTable, id, eCtrl.getAttributes())) {
                    setSelectionAfterSearch(eCtrl.getAttributes());
                    setSearchValues(eCtrl.getAttributes());
                }
            }
        }
    }

    protected void doEditOrView(Event event) {
        endSearch();
        DBDataObjectAttributes attributes = getSelectedAttributes();
        if (attributes != null) {
            IdWithType id = buildIdFromAttributes(attributes);
            EditUserControlsForBADCode eCtrl = new EditUserControlsForBADCode(getConnector(), this, searchTable, id, attributes, editFields);
            boolean editAndModifyAllowed = isEditAllowed() && isModifyAllowed();
            eCtrl.setReadOnly(!editAndModifyAllowed);
            eCtrl.setTitle(editAndModifyAllowed ? titleForEdit : titleForView);
            eCtrl.setWindowName(editControlsWindowName);
            if (eCtrl.showModal() == ModalResult.OK) {
                if (onEditChangeRecordEvent != null) {
                    if (onEditChangeRecordEvent.onEditModifyRecordEvent(getConnector(), searchTable, id, eCtrl.getAttributes())) {
                        // Suche nochmals starten als Refresh für Table
                        setSelectionAfterSearch(eCtrl.getAttributes());
                        clearGrid();
                        internalStartSearch();
                    }
                }
            }
        }
    }

    // Routinen für EditUserControlsForBADCode und EditUserControlForCreateBadCode
    private void modifyEditControlForBADCode(EditControl ctrl, EtkEditField field, String initialValue, EtkDataArray initialDataArray,
                                             String tableName) {
        if (field.getKey().getName().equals(TableAndFieldName.make(tableName, FIELD_DBC_AA))) {
            iPartsAAPartsHelper.setAAEnumValuesByX4E(ctrl.getEditControl(), getProject(), getSeriesId().getSeriesNumber());
        }
    }

    private boolean isFieldPKValueOrMandatoryForBADCode(EtkEditField field, List<String> pkFields, boolean isPkOrMandatory) {
        if (isPkOrMandatory) {
            if (field.getKey().getFieldName().equals(FIELD_DBC_AA)) {
                // Ausführungsart ist PKValue, darf aber leer sein
                return false;
            }
        }
        return isPkOrMandatory;
    }

    private iPartsGuiCodeTextField getGuiCodeTextField(EtkEditFields editFields, EditControls editControls) {
        iPartsGuiCodeTextField codeTextField = null;
        int index = editFields.getIndexOfVisibleTableAndFeldName(new EtkDisplayFieldKeyNormal(TABLE_DA_BAD_CODE, FIELD_DBC_CODE_ID));
        if (index != -1) {
            EditControl ctrl = editControls.getControlByFeldIndex(index);
            if ((ctrl != null) && (ctrl.getEditControl().getControl() != null)) {
                if (ctrl.getEditControl().getControl() instanceof iPartsGuiCodeTextField) {
                    codeTextField = (iPartsGuiCodeTextField)ctrl.getEditControl().getControl();
                }
            }
        }
        return codeTextField;
    }

    private boolean checkValueWithCodeTextField(EtkEditFields editFields, EditControls editControls, DBDataObjectAttributes attributes) {
        boolean result = true;
        iPartsGuiCodeTextField codeTextField = getGuiCodeTextField(editFields, editControls);
        if (codeTextField != null) {
            String productGroup = "";
            iPartsDataSeries series = new iPartsDataSeries(getProject(), getSeriesId());
            if (series.loadFromDB(getSeriesId())) {
                productGroup = series.getFieldValue(FIELD_DS_PRODUCT_GRP);
            }
            String ausfuehrungsArt = attributes.getField(FIELD_DBC_AA).getAsString();

            // BAD-Code gibt es nur für DIALOG Baureihen, also kann hier fest DIALOG als DokuTyp angenommen werden
            codeTextField.init(getConnector().getProject(), iPartsDocumentationType.DIALOG, getSeriesId().getSeriesNumber(),
                               productGroup, "", ausfuehrungsArt, iPartsGuiCodeTextField.CODE_TEST_TYPE.PRODUCTGRP_ONLY);
            if (!codeTextField.checkInput()) {
                String errorMessage = codeTextField.getErrorMessage();
                errorMessage = checkIfExpired(editFields, editControls, attributes, errorMessage);
                errorMessage = checkExpireDateVersusPermanent(editFields, editControls, attributes, errorMessage);
                if (StrUtils.isValid(errorMessage)) {
                    MessageDialog.showError(errorMessage);
                    result = false;
                } else {
                    String warningMessage = codeTextField.getWarningMessage();
                    if (StrUtils.isValid(warningMessage)) {
                        warningMessage += "\n\n" + TranslationHandler.translate("!!Trotzdem speichern?");
                        if (MessageDialog.showYesNo(warningMessage) == ModalResult.NO) {
                            result = false;
                        }
                    }
                }
            } else {
                String errorMessage = checkIfExpired(editFields, editControls, attributes, "");
                errorMessage = checkExpireDateVersusPermanent(editFields, editControls, attributes, errorMessage);
                if (StrUtils.isValid(errorMessage)) {
                    MessageDialog.showError(errorMessage);
                    result = false;
                }
            }
        }
        return result;
    }

    private String checkIfExpired(EtkEditFields editFields, EditControls editControls, DBDataObjectAttributes attributes, String errorMessage) {
        String errMsg = "";
        String expiringDate = attributes.getField(FIELD_DBC_EXPIRY_DATE).getAsString();
        if (!expiringDate.equals("") && iPartsDataBadCode.isExpired(expiringDate)) {
            errMsg = "!!\"%1\" muss das aktuelle oder ein zukünftiges Datum besitzen!";
        }

        if (StrUtils.isValid(errMsg)) {
            String dateExpireText = getLabelText(editFields, editControls, TABLE_DA_BAD_CODE, FIELD_DBC_EXPIRY_DATE, "!!Verfallsdatum");

            if (StrUtils.isValid(errorMessage)) {
                errorMessage += "\n";
            } else {
                errorMessage = "";
            }
            errorMessage += TranslationHandler.translate(errMsg, dateExpireText);
        }

        return errorMessage;
    }

    private String checkExpireDateVersusPermanent(EtkEditFields editFields, EditControls editControls, DBDataObjectAttributes attributes, String errorMessage) {
        String errMsg = "";
        if (StrUtils.isValid(attributes.getField(FIELD_DBC_EXPIRY_DATE).getAsString())) {
            // Verfallsdatum gesetzt
            if (attributes.getField(FIELD_DBC_PERMANENT_BAD_CODE).getAsBoolean()) {
                // Verfallsdatum + Permanent gleichzeitg ist verboten
                errMsg = "!!\"%1\" und \"%2\" dürfen nicht gleichzeitig gesetzt sein!";
            }
        } else {
            // Verfallsdatum nicht gesetzt
            if (!attributes.getField(FIELD_DBC_PERMANENT_BAD_CODE).getAsBoolean()) {
                // Verfallsdatum oder Permamnent muss gestezt sein
                errMsg = "!!\"%1\" oder \"%2\" muss gesetzt sein!";
            }

        }
        if (StrUtils.isValid(errMsg)) {
            String dateExpireText = getLabelText(editFields, editControls, TABLE_DA_BAD_CODE, FIELD_DBC_EXPIRY_DATE, "!!Verfallsdatum");
            String permanentText = getLabelText(editFields, editControls, TABLE_DA_BAD_CODE, FIELD_DBC_PERMANENT_BAD_CODE, "!!Dauerhaft");
            ;
            if (StrUtils.isValid(errorMessage)) {
                errorMessage += "\n";
            } else {
                errorMessage = "";
            }
            errorMessage += TranslationHandler.translate(errMsg, dateExpireText, permanentText);
        }
        return errorMessage;
    }

    private String getLabelText(EtkEditFields editFields, EditControls editControls, String tableName, String fieldName, String defaultText) {
        String result;
        int index = editFields.getIndexOfVisibleTableAndFeldName(new EtkDisplayFieldKeyNormal(tableName, fieldName));
        if (index != -1) {
            EditControl ctrl = editControls.getControlByFeldIndex(index);
            result = ctrl.getLabel().getText();
        } else {
            result = TranslationHandler.translate(defaultText);
        }
        return result;
    }

    private class EditUserControlsForBADCode extends EditUserControls {

        private EditUserControlsForBADCode(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName, IdWithType id, DBDataObjectAttributes attributes, EtkEditFields externalEditFields) {
            super(dataConnector, parentForm, tableName, id, attributes, externalEditFields);
        }

        @Override
        protected void modifyEditControl(EditControl ctrl, EtkEditField field, String initialValue, EtkDataArray initialDataArray) {
            modifyEditControlForBADCode(ctrl, field, initialValue, initialDataArray, tableName);
        }

        @Override
        protected boolean checkValues() {
            boolean result = super.checkValues();
            if (result) {
                if ((attributes != null) && (tableName != null)) {
                    result = checkValueWithCodeTextField(editFields, editControls, attributes);
                }
            }
            return result;
        }

        @Override
        protected boolean isFieldPKValueOrMandatory(EtkEditField field, List<String> pkFields) {
            return isFieldPKValueOrMandatoryForBADCode(field, pkFields, super.isFieldPKValueOrMandatory(field, pkFields));
        }

    }

    private class EditUserControlForCreateBadCode extends EditUserControlForCreate {

        public EditUserControlForCreateBadCode(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName, IdWithType id, DBDataObjectAttributes attributes, EtkEditFields externalEditFields) {
            super(dataConnector, parentForm, tableName, id, attributes, externalEditFields);
        }

        @Override
        protected void modifyEditControl(EditControl ctrl, EtkEditField field, String initialValue, EtkDataArray initialDataArray) {
            modifyEditControlForBADCode(ctrl, field, initialValue, initialDataArray, tableName);
        }

        @Override
        protected boolean isFieldPKValueOrMandatory(EtkEditField field, List<String> pkFields) {
            return isFieldPKValueOrMandatoryForBADCode(field, pkFields, super.isFieldPKValueOrMandatory(field, pkFields));
        }

        @Override
        protected boolean checkValues() {
            boolean result = super.checkValues();
            if (result) {
                if ((attributes != null) && (tableName != null)) {
                    result = checkValueWithCodeTextField(editFields, editControls, attributes);
                }
            }
            return result;
        }

    }

}