/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControls;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EtkEditFieldHelper;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.event.Event;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.TableAndFieldName;

import java.util.List;

/**
 *
 */
public class EditUserControlForCreate extends EditUserControls {

    protected static EtkEditFields modifyEditFields(EtkEditFields editFields, String tableName, String[] mustHaveValueFieldNames,
                                                    String[] allowedEmptyPKFields, String[] invisibleFieldNames, String[] readOnlyFieldNames,
                                                    String... extraReadOnlyFieldNames) {

        setAllEditableFields(readOnlyFieldNames, editFields, tableName, false);
        setAllEditableFields(extraReadOnlyFieldNames, editFields, tableName, false);
        setAllMustFields(mustHaveValueFieldNames, editFields, tableName, true);
        setAllVisibleFields(invisibleFieldNames, editFields, tableName, false);
        setAllMustFields(allowedEmptyPKFields, editFields, tableName, false);

        return editFields;
    }

    private enum EDIT_FIELD_TYPE {
        MUST,
        VISIBLE,
        EDITABLE
    }

    protected static void setAllMustFields(String[] fieldNames, EtkEditFields editFields, String tableName, boolean value) {
        setAllFields(fieldNames, editFields, tableName, value, EDIT_FIELD_TYPE.MUST);
    }

    protected static void setAllVisibleFields(String[] fieldNames, EtkEditFields editFields, String tableName, boolean value) {
        setAllFields(fieldNames, editFields, tableName, value, EDIT_FIELD_TYPE.VISIBLE);
    }

    protected static void setAllEditableFields(String[] fieldNames, EtkEditFields editFields, String tableName, boolean value) {
        setAllFields(fieldNames, editFields, tableName, value, EDIT_FIELD_TYPE.EDITABLE);
    }

    protected static void setAllFields(String[] fieldNames, EtkEditFields editFields, String tableName, boolean value,
                                       EDIT_FIELD_TYPE type) {
        if (fieldNames != null) {
            for (String fieldName : fieldNames) {
                setOneField(editFields, tableName, fieldName, value, type);
            }
        }
    }

    protected static void setOneField(EtkEditFields editFields, String tableName, String fieldName, boolean value,
                                      EDIT_FIELD_TYPE type) {
        EtkEditField editField = editFields.getFeldByName(tableName, fieldName);
        if (editField != null) {
            switch (type) {
                case MUST:
                    editField.setMussFeld(value);
                    break;
                case VISIBLE:
                    editField.setVisible(value);
                    break;
                case EDITABLE:
                    editField.setEditierbar(value);
                    break;
            }
        }
    }

    protected static EtkEditFields modifyEditFields(EtkProject project, String configKey, String tableName, String[] mustHaveValueFieldNames,
                                                    String[] allowedEmptyPKFields, String[] invisibleFieldNames, String[] readOnlyFieldNames,
                                                    String... extraReadOnlyFieldNames) {
        EtkEditFields editFields = new EtkEditFields();
        // Konfiguration soll komplett über WB passieren, die unteren Funktionen sind nur noch als Platzhalter
        // vorhanden, falls die Konfiguration doch überschrieben werden soll
        editFields.load(project.getConfig(), configKey + iPartsEditConfigConst.REL_EDIT_MASTER_EDITFIELDS);
        if (editFields.size() == 0) {
            EtkEditFieldHelper.getEditFields(project, tableName, editFields, false);
        }
        return modifyEditFields(editFields, tableName, mustHaveValueFieldNames, allowedEmptyPKFields, invisibleFieldNames,
                                readOnlyFieldNames, extraReadOnlyFieldNames);
    }

    protected static EtkEditFields modifyEditFields(EtkEditFields editFields, String[] mustHaveValueTableAndFieldNames,
                                                    String[] allowedEmptyPKFields, String[] invisibleTableAndFieldNames,
                                                    String[] readOnlyTableAndFieldNames, String... extraReadOnlyTableAndFieldNames) {

        setAllEditableFields(readOnlyTableAndFieldNames, editFields, false);
        setAllEditableFields(extraReadOnlyTableAndFieldNames, editFields, false);
        setAllMustFields(mustHaveValueTableAndFieldNames, editFields, true);
        setAllVisibleFields(invisibleTableAndFieldNames, editFields, false);
        setAllMustFields(allowedEmptyPKFields, editFields, false);

        return editFields;
    }

    protected static void setAllMustFields(String[] tableAndFieldNames, EtkEditFields editFields, boolean value) {
        setAllFields(tableAndFieldNames, editFields, value, EDIT_FIELD_TYPE.MUST);
    }

    protected static void setAllVisibleFields(String[] tableAndFieldNames, EtkEditFields editFields, boolean value) {
        setAllFields(tableAndFieldNames, editFields, value, EDIT_FIELD_TYPE.VISIBLE);
    }

    protected static void setAllEditableFields(String[] tableAndFieldNames, EtkEditFields editFields, boolean value) {
        setAllFields(tableAndFieldNames, editFields, value, EDIT_FIELD_TYPE.EDITABLE);
    }

    protected static void setAllFields(String[] tableAndFieldNames, EtkEditFields editFields, boolean value,
                                       EDIT_FIELD_TYPE type) {
        if (tableAndFieldNames != null) {
            for (String tableAndFieldName : tableAndFieldNames) {
                setOneField(editFields, TableAndFieldName.getTableName(tableAndFieldName), TableAndFieldName.getFieldName(tableAndFieldName), value, type);
            }
        }
    }

    /**
     * Die Angaben für Felder müssen in dieser Version alle jeweils auch den Tabellennamen enthalten
     *
     * @param project
     * @param configKey
     * @param tableNameForDefaultFields Tabellen Name um Default EditFields zu erzeugen falls keine kinfiguriert sind
     * @param mustHaveValueFieldNames   TableAndFieldname
     * @param allowedEmptyPKFields      TableAndFieldname
     * @param invisibleFieldNames       TableAndFieldname
     * @param readOnlyFieldNames        TableAndFieldname
     * @param extraReadOnlyFieldNames   TableAndFieldname
     * @return
     */
    protected static EtkEditFields modifyEditFieldsTableAndFieldname(EtkProject project, String configKey, String tableNameForDefaultFields, String[] mustHaveValueFieldNames,
                                                                     String[] allowedEmptyPKFields, String[] invisibleFieldNames, String[] readOnlyFieldNames,
                                                                     String... extraReadOnlyFieldNames) {
        EtkEditFields editFields = new EtkEditFields();
        // Konfiguration soll komplett über WB passieren, die unteren Funktionen sind nur noch als Platzhalter
        // vorhanden, falls die Konfiguration doch überschrieben werden soll
        editFields.load(project.getConfig(), configKey + iPartsEditConfigConst.REL_EDIT_MASTER_EDITFIELDS);
        if (editFields.size() == 0) {
            EtkEditFieldHelper.getEditFields(project, tableNameForDefaultFields, editFields, false);
        }
        return modifyEditFields(editFields, mustHaveValueFieldNames, allowedEmptyPKFields, invisibleFieldNames,
                                readOnlyFieldNames, extraReadOnlyFieldNames);
    }


    public EditUserControlForCreate(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName,
                                    IdWithType id, DBDataObjectAttributes attributes, EtkEditFields externalEditFields) {
        super(dataConnector, parentForm, tableName, id, attributes, externalEditFields, false, "");
    }

    public EditUserControlForCreate(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName,
                                    IdWithType id, DBDataObjectAttributes attributes, EtkEditFields externalEditFields, EditControls editControls) {
        super(dataConnector, parentForm, tableName, id, attributes, externalEditFields, false, "", editControls);
    }

    public EditUserControlForCreate(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName, IdWithType id) {
        super(dataConnector, parentForm, tableName, id);
    }

    protected void setAttributes() {
        if (externalEditFields != null) { // explizit gesetzte externe Edit-Felder durchgehen und darauf basierend neue Attribute erstellen
            if (attributes == null) {
                attributes = new DBDataObjectAttributes();
            }
            for (EtkEditField externalEditField : externalEditFields.getFields()) {
                EtkDatabaseTable tableDef = getProject().getConfig().getDBDescription().findTable(externalEditField.getKey().getTableName());
                if (tableDef != null) {
                    EtkDatabaseField dbField = tableDef.getField(externalEditField.getKey().getFieldName());
                    if (dbField != null) {
                        addAttributeForDBField(dbField);
                    }
                }
            }
        } else { // basierend auf der übergebenen Tabelle alle Attribute für diese Tabelle erstellen
            EtkDatabaseTable tableDef = getProject().getConfig().getDBDescription().findTable(tableName);
            if (tableDef != null) {
                if (attributes == null) {
                    attributes = new DBDataObjectAttributes();
                }
                for (EtkDatabaseField dbField : tableDef.getFieldList()) {
                    addAttributeForDBField(dbField);
                }
            }
        }
    }

    protected void addAttributeForDBField(EtkDatabaseField databaseField) {
        String attributeName = databaseField.getName();
        if (attributes.containsKey(attributeName)) {
            return;
        }

        switch (databaseField.getType()) {
            case feBlob:
                attributes.addField(new DBDataObjectAttribute(attributeName, DBDataObjectAttribute.TYPE.BLOB, false, true), DBActionOrigin.FROM_DB);
                break;
            case feFloat:
                attributes.addField(attributeName, SQLStringConvert.doubleToPPString(0.0d), DBActionOrigin.FROM_DB);
                break;
            case feInteger:
                attributes.addField(attributeName, SQLStringConvert.intToPPString(0), DBActionOrigin.FROM_DB);
                break;
            case feBoolean:
                attributes.addField(attributeName, SQLStringConvert.booleanToPPString(false), DBActionOrigin.FROM_DB);
                break;
            default:
                if (databaseField.isMultiLanguage()) {
                    attributes.addField(new DBDataObjectAttribute(attributeName, DBDataObjectAttribute.TYPE.MULTI_LANGUAGE, false, true), DBActionOrigin.FROM_DB);
                } else if (databaseField.isArray()) {
                    attributes.addField(new DBDataObjectAttribute(attributeName, DBDataObjectAttribute.TYPE.ARRAY, false, true), DBActionOrigin.FROM_DB);
                } else {
                    attributes.addField(attributeName, "", DBActionOrigin.FROM_DB);
                }
                break;
        }
    }

    protected void doEnableButtons(Event event) {
        if (!readOnly) {
            enableOKButton(checkPkValuesForModified());
        }
    }

    protected boolean checkPkValuesForModified() {
        return checkAllMustFieldsFilled(true);
    }

    /**
     * Überprüft, ob die Muss-Felder befüllt sind
     *
     * @param checkClonedAttributesModified
     * @return
     */
    protected boolean checkAllMustFieldsFilled(boolean checkClonedAttributesModified) {
        int index = 0;
        EtkDatabaseTable tableDef = getProject().getConfig().getDBDescription().findTable(tableName);
        List<String> pkFields = tableDef.getPrimaryKeyFields();

        DBDataObjectAttributes clonedAttributes = new DBDataObjectAttributes();
        clonedAttributes.assign(attributes, DBActionOrigin.FROM_DB);
        boolean allMustFieldsFilled = true;
        for (EtkEditField field : editFields.getVisibleEditFields()) {
            if (isFieldPKValueOrMandatory(field, pkFields)) {
                DBDataObjectAttribute clonedAttribute = clonedAttributes.getField(field.getKey().getFieldName());
                fillAttribByEditControlValue(index, field, clonedAttribute);
                if (field.isMussFeld()) {
                    if (!isMustValueNotEmpty(field, clonedAttribute)) {
                        allMustFieldsFilled = false;
                    }
                }
            }
            index++;
        }
        if (checkClonedAttributesModified) {
            return allMustFieldsFilled && clonedAttributes.isModified();
        }
        return allMustFieldsFilled;
    }

    @Override
    protected boolean isMandatoryAttributeValueEmpty(EtkEditField field, DBDataObjectAttribute attrib) {
        return (attrib != null) && attrib.isEmpty(false, null);
    }

    /**
     * Überprüft, ob ein Muss-Feld einen nicht leeren Attributwert besitzt
     *
     * @param field
     * @param attribute
     * @return
     */
    protected boolean isMustValueNotEmpty(EtkEditField field, DBDataObjectAttribute attribute) {
        return !isMandatoryAttributeValueEmpty(field, attribute);
    }

    /**
     * Überprüft die Muss-Felder
     *
     * @return true: eines der Muss-Felder ist leer
     */
    protected boolean checkMustFieldsHaveValues() {
        int index = 0;
        if (attributes != null) {
            for (EtkEditField field : editFields.getVisibleEditFields()) {
                if (field.isMussFeld()) {
                    DBDataObjectAttribute attrib = getCurrentAttribByEditControlValue(index, field);
                    if (isMandatoryAttributeValueEmpty(field, attrib)) { // hier keine Logausgabe machen, wenn attrib null ist, weil das in collectEditValues() bereits gemacht wird
                        return true;
                    }
                }
                index++;
            }
            return false;
        }
        return true;
    }
}
