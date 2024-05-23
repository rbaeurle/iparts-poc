/*
 * Copyright (c) 2019 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelProperties;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.DIALOGModelsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelPropertiesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;

import java.awt.*;
import java.util.List;

public class EditUserControlForModelMapping extends EditUserControlForCreate implements iPartsConst {

    private static final String[] MODEL_DEFAULT_FIELDS = new String[]{ FIELD_DM_MODEL_NO, FIELD_DM_SALES_TITLE,
                                                                       FIELD_DM_VALID_FROM, FIELD_DM_VALID_TO,
                                                                       FIELD_DM_CODE, FIELD_DM_STEERING };
    private static final String[] MODEL_MUST_FIELDS = new String[]{ FIELD_DM_SALES_TITLE,
                                                                    FIELD_DM_VALID_FROM, FIELD_DM_VALID_TO,
                                                                    FIELD_DM_CODE };
    private static final String[] MODEL_EDITABLE_FIELDS = new String[]{ FIELD_DM_SALES_TITLE,
                                                                        FIELD_DM_VALID_FROM, FIELD_DM_VALID_TO,
                                                                        FIELD_DM_STEERING };
    private static final String[] MODEL_EXTRA_EMPTY_FIELDS = new String[]{ FIELD_DM_VALID_TO, FIELD_DM_STEERING };

    public static iPartsDataModel editASModelData(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                  iPartsDataModelProperties sourceConstructionModel, iPartsDataModelProperties connectedConstructionModel,
                                                  iPartsDataModel existingASDataModel, boolean isNew) {
        EtkProject project = dataConnector.getProject();
        iPartsDataModel afterSalesModel;
        String title;
        boolean clearExtraFields = true;
        boolean override = true;
        boolean constModelNotFound = false;
        if (existingASDataModel != null) {
            // Handelt es sich um ein neues AS-Baumuster
            if (isNew) {
                title = "!!AS-Baumuster \"%1\" neu anlegen";
            } else if (connectedConstructionModel == null) {
                // Am AS-Baumuster hängt ein Konstruktionsbaumuster das nicht einmal mehr in der DB existiert
                title = "!!AS-Baumuster \"%1\" bearbeiten";
                connectedConstructionModel = new iPartsDataModelProperties(project, new iPartsModelPropertiesId());
                constModelNotFound = true;
            } else {
                // Das Konstruktionsbaumuster ist das gleiche Baumuster, das schon am AS-Baumuster hängt
                if (connectedConstructionModel.getAsId().equals(sourceConstructionModel.getAsId())) {
                    title = "!!AS-Baumuster \"%1\" bearbeiten";
                    clearExtraFields = false;
                    override = false;
                    connectedConstructionModel = null;
                } else {
                    // Entweder unterschiedliche Konstruktionsbaumuster (auch Datum beachten) oder ein maschinell erzeugtes AS-Baumuster
                    title = "!!AS-Baumuster \"%1\" überschreiben";
                    iPartsModelPropertiesId relatedDataModelId = existingASDataModel.getRelatedConstructionId();
                    if (relatedDataModelId.isValidId()) {
                        if (relatedDataModelId.equals(sourceConstructionModel.getAsId())) {
                            clearExtraFields = false;
                            override = false;
                        }
                    } else {
                        // maschinell erzeugt
                        connectedConstructionModel = new iPartsDataModelProperties(project, new iPartsModelPropertiesId());
                    }
                }
            }
            // Kopie erzeugen, damit Änderungen bei Abbrechen nicht indirekt übergeben wird
            afterSalesModel = new iPartsDataModel(project, null);
            afterSalesModel.assign(project, existingASDataModel, DBActionOrigin.FROM_EDIT);

            if (override) {
                // übernehme die Daten aus dem konstruktiven BM
                DIALOGModelsHelper.copyAttributeValuesFromConstructionToAS(project, afterSalesModel, sourceConstructionModel,
                                                                           iPartsImportDataOrigin.DIALOG, true);
            }

            IdWithType id = new IdWithType("x", new String[]{ "1" });
            EtkEditFields editFields = getEditFields(dataConnector, iPartsEditConfigConst.iPARTS_EDIT_MASTER_MODEL_AFTER_SALES_ASSIGNMENT_KEY);

            // attributes besetzen
            DBDataObjectAttributes attributes = afterSalesModel.getAttributes();
            if (clearExtraFields) {
                for (String field : MODEL_EXTRA_EMPTY_FIELDS) {
                    DBDataObjectAttribute attrib = attributes.getField(field);
                    if (attrib != null) {
                        attrib.setValueAsString("", DBActionOrigin.FROM_DB);
                    }
                }
            }

            EditUserControlForModelMapping userCtrl = new EditUserControlForModelMapping(dataConnector,
                                                                                         parentForm,
                                                                                         id, attributes, editFields,
                                                                                         connectedConstructionModel,
                                                                                         constModelNotFound);
            userCtrl.setTitle(TranslationHandler.translate(title, afterSalesModel.getAsId().getModelNumber()));

            if (ModalResult.OK == userCtrl.showModal()) {
                afterSalesModel.setFieldValueAsBoolean(iPartsConst.FIELD_DM_MODEL_VISIBLE, true, DBActionOrigin.FROM_EDIT);
                if (sourceConstructionModel.getAsId().isValidId()) {
                    afterSalesModel.setRelatedConstructionId(sourceConstructionModel.getAsId(), DBActionOrigin.FROM_EDIT);
                }
                return afterSalesModel;
            }
        }
        return null;
    }

    public static iPartsDataModel editASSelectedModelData(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                          iPartsDataModel existingASDataModel,
                                                          iPartsDataModelProperties connectedConstructionModel, boolean constModelNotFound) {
        EtkProject project = dataConnector.getProject();
        iPartsDataModel afterSalesModel;

        // erzeuge eine Arbeitskopie (Änderungen werden beim Abbrechen nicht übernommen)
        afterSalesModel = new iPartsDataModel(project, null);
        afterSalesModel.assign(project, existingASDataModel, DBActionOrigin.FROM_EDIT);

        IdWithType id = new IdWithType("x", new String[]{ "1" });

        // EditFields besetzen
        EtkEditFields editFields = getEditFields(dataConnector, iPartsEditConfigConst.iPARTS_EDIT_MASTER_MODEL_AFTER_SALES_KEY);

        // attributes besetzen
        DBDataObjectAttributes attributes = afterSalesModel.getAttributes();

        EditUserControlForModelMapping userCtrl = new EditUserControlForModelMapping(dataConnector,
                                                                                     parentForm,
                                                                                     id, attributes, editFields,
                                                                                     connectedConstructionModel, constModelNotFound);
        userCtrl.setTitle(TranslationHandler.translate("!!AS-Baumuster \"%1\" bearbeiten", afterSalesModel.getAsId().getModelNumber()));
        userCtrl.setConnectionTitle("!!Konstruktions-Baumuster Verknüpfung");
        if (ModalResult.OK == userCtrl.showModal()) {
//            afterSalesModel.setFieldValueAsBoolean(iPartsConst.FIELD_DM_MODEL_VISIBLE, true, DBActionOrigin.FROM_EDIT);
            return afterSalesModel;
        }
        return null;
    }

    private static EtkEditFields getEditFields(AbstractJavaViewerFormIConnector dataConnector, String rootKey) {
        // EditFields besetzen
        EtkEditFields editFields = new EtkEditFields();
        editFields.load(dataConnector.getConfig(), rootKey + iPartsEditConfigConst.REL_EDIT_MASTER_EDITFIELDS);
        if (editFields.size() == 0) {
            editFields = getDefaultEditFields(dataConnector);
        } else {
            editFields = checkConfiguratedEditFields(dataConnector, editFields);
        }
        return editFields;
    }

    private static EtkEditFields getDefaultEditFields(AbstractJavaViewerFormIConnector dataConnector) {
        EtkEditFields editFields = new EtkEditFields();
        List<String> defaultFields = new DwList<>(MODEL_DEFAULT_FIELDS);
        List<String> mustFields = new DwList<>(MODEL_MUST_FIELDS);
        List<String> editableFields = new DwList<>(MODEL_EDITABLE_FIELDS);

        EtkDatabaseTable tableDef = dataConnector.getConfig().getDBDescription().findTable(TABLE_DA_MODEL);
        if (tableDef != null) {
            for (String fieldName : defaultFields) {
                EtkEditField editField = createEditFieldForViewing(tableDef, fieldName);
                editField.setMussFeld(mustFields.contains(fieldName));
                editField.setEditierbar(editableFields.contains(fieldName));
                editFields.addFeld(editField);
            }
        }
        return editFields;
    }

    private static EtkEditFields checkConfiguratedEditFields(AbstractJavaViewerFormIConnector dataConnector, EtkEditFields etkEditFields) {
        EtkEditFields defaultEditFields = getDefaultEditFields(dataConnector);
        EtkDatabaseTable tableDef = dataConnector.getConfig().getDBDescription().findTable(TABLE_DA_MODEL);
        List<String> mustFields = new DwList<>(MODEL_MUST_FIELDS);
        List<String> editableFields = new DwList<>(MODEL_EDITABLE_FIELDS);

        for (EtkEditField editField : etkEditFields.getFields()) {
            EtkEditField defaultEditField = defaultEditFields.getFeldByKey(editField.getKey(), editField.getKey().isUsageField());
            if (defaultEditField != null) {
                editField.setMussFeld(defaultEditField.isMussFeld());
                editField.setEditierbar(defaultEditField.isEditierbar());
            } else {
                String fieldName = editField.getKey().getFieldName();
                if (mustFields.contains(fieldName)) {
                    editField.setMussFeld(true);
                    editField.setEditierbar(editableFields.contains(fieldName));
                } else {
                    editField.setMussFeld(false);
                    editField.setEditierbar(false);
                }
            }
        }
        for (String fieldName : mustFields) {
            EtkEditField editField = etkEditFields.getFeldByName(TABLE_DA_MODEL, fieldName);
            if (editField == null) {
                editField = createEditFieldForViewing(tableDef, fieldName);
                editField.setMussFeld(true);
                editField.setEditierbar(editableFields.contains(fieldName));
                etkEditFields.addFeld(editField);
            }
        }

        return etkEditFields;
    }

    private static EtkEditField createEditFieldForViewing(EtkDatabaseTable tableDef, String fieldName) {
        EtkDatabaseField dbField = tableDef.getField(fieldName);
        boolean isMultiLang = false;
        boolean isArray = false;
        if (dbField != null) {
            isMultiLang = dbField.isMultiLanguage();
            isArray = dbField.isArray();
        }
        EtkEditField editField = new EtkEditField(tableDef.getName(), fieldName, isMultiLang);
        editField.setArray(isArray);
        editField.setMussFeld(false);
        editField.setEditierbar(false);
        return editField;
    }


    private GuiPanel panelConnection;

    public EditUserControlForModelMapping(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                          IdWithType id, DBDataObjectAttributes attributes,
                                          EtkEditFields externalEditFields, iPartsDataModelProperties dataModelProperties,
                                          boolean constModelNotFound) {
        super(dataConnector, parentForm, TABLE_DA_MODEL, id, attributes, externalEditFields);
        postPostCreateGui(dataModelProperties, constModelNotFound);
    }

    /**
     * Aufbau und Anzeige des unteren Teils der EditUserControls
     *
     * @param dataModelProperties
     * @param constModelNotFound
     */
    private void postPostCreateGui(iPartsDataModelProperties dataModelProperties, boolean constModelNotFound) {
        if (dataModelProperties == null) {
            return;
        }
        panelConnection = new GuiPanel();
        panelConnection.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        panelConnection.setTitle("!!Bisherige Konstruktions-Baumuster Verknüpfung");
        panelConnection.setLayout(new LayoutBorder());

        int height;
        if (!dataModelProperties.getAsId().isValidId()) {
            GuiLabel label;
            if (constModelNotFound) {
                label = new GuiLabel("!!Verknüpftes Konstruktions-Baumuster existiert nicht!");
            } else {
                label = new GuiLabel("!!maschinell erzeugt");
            }
            label.setBorderWidth(4);
            label.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
            panelConnection.addChild(label);
            height = 60;
        } else {
            EtkEditFields editFields = getViewingEditFields();
            IdWithType id = new IdWithType("x", new String[]{ "1", "1" });
            DBDataObjectAttributes attributes = dataModelProperties.getAttributes();
            EditUserControls eCtrl = new EditUserControls(getConnector(), this, TABLE_DA_MODEL_PROPERTIES,
                                                          id, attributes, editFields);
            eCtrl.setReadOnly(true);
            panelConnection.addChildBorderCenter(eCtrl.getGui());
            height = (int)eCtrl.getCalculatedPanelSize().getHeight() - 22;
        }

        Dimension screenSize = FrameworkUtils.getScreenSize();
        height = Math.min(getHeight() + height, screenSize.height - 20);
        addChildAsSplitPaneElement(panelConnection, height);
    }

    public void setConnectionTitle(String title) {
        if (panelConnection != null) {
            panelConnection.setTitle(title);
        }
    }

    private EtkEditFields getViewingEditFields() {
        EtkEditFields editFields = new EtkEditFields();
        EtkDatabaseTable tableDef = getConfig().getDBDescription().findTable(TABLE_DA_MODEL_PROPERTIES);

        editFields.addField(createEditFieldForViewing(tableDef, FIELD_DMA_MODEL_NO));
        editFields.addField(createEditFieldForViewing(tableDef, FIELD_DMA_DATA));
        editFields.addField(createEditFieldForViewing(tableDef, FIELD_DMA_DATB));
        editFields.addField(createEditFieldForViewing(tableDef, FIELD_DMA_CODE));
        editFields.addField(createEditFieldForViewing(tableDef, FIELD_DMA_AA));

        return editFields;
    }

    protected boolean isMandatoryAttributeValueEmpty(EtkEditField field, DBDataObjectAttribute attrib) {
        if (attrib != null) {
            List<String> allowedEmptyFields = new DwList<>(MODEL_EXTRA_EMPTY_FIELDS);
            if (!allowedEmptyFields.contains(field.getKey().getFieldName())) {
                return super.isMandatoryAttributeValueEmpty(field, attrib);
            }
        }
        return false;
    }

    @Override
    protected boolean checkPkValuesForModified() {
        return checkAllMustFieldsFilled(false);
    }

    @Override
    public EditResult stopAndStoreEdit() {
        if (!readOnly) {
            if (checkCompletionOfFormValues()) {
                DBDataObjectAttributes oldAttributes = null;
                if (attributes != null) {
                    oldAttributes = attributes.cloneMe(DBActionOrigin.FROM_DB);
                }
                collectEditValues();
//                if (!isModified()) {
//                    return EditResult.UNMODIFIED;
//                } else {
                if (checkValues()) {
                    // Falls in den Controls eigene Objekte (kombinierte Texte, Werkeinsatzdaten) angelegt wurden, diese hier speichern
                    saveAdditionalData();
                    return EditResult.STORED;
                } else {
                    if (oldAttributes != null) {
                        attributes.assign(oldAttributes, DBActionOrigin.FROM_DB);
                    }
                    return EditResult.ERROR;
                }
//                }
            } else {
                return EditResult.ERROR;
            }
        } else {
            return EditResult.UNMODIFIED;
        }
    }

}

