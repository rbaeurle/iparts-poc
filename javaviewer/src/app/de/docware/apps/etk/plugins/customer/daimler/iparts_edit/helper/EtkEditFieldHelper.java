/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.config.EtkConfig;
import de.docware.apps.etk.base.config.EtkConfigConst;
import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumDataType;
import de.docware.apps.etk.base.config.partlist.*;
import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImage;
import de.docware.apps.etk.plugins.EtkPluginApi;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFieldsEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFieldsEditList;
import de.docware.framework.modules.config.db.DBConst;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.gui.app.DWLayoutManager;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Helper für EtkEditField
 */
public class EtkEditFieldHelper implements EtkDbConst {


    public static boolean isEnumField(String tableName, String fieldName) {
        return ((tableName.equals(TABLE_KATALOG) && fieldName.equals(FIELD_K_EBENE)) ||
                (tableName.equals(TABLE_POOL) && fieldName.equals(FIELD_P_SPRACH)) ||
                (tableName.equals(TABLE_ENUM) && fieldName.equals(FIELD_E_SHOWICON)) ||
                (tableName.equals(TABLE_POOL) && fieldName.equals(FIELD_P_USAGE)));
    }

    public static boolean isEnumField(String tableAndFieldName) {
        return isEnumField(TableAndFieldName.getTableName(tableAndFieldName), TableAndFieldName.getFieldName(tableAndFieldName));
    }

    public static void getEnumEditFieldDisplayValues(EtkConfig config, String tableName, String fieldName, List<String> values) {
        values.clear();
        if (isEnumField(tableName, fieldName)) {
            List<String> enumValues = new ArrayList<String>();
            if (tableName.equals(TABLE_KATALOG) && fieldName.equals(FIELD_K_EBENE)) {
                for (EtkEbenenDaten etkEbene : config.getPartsDescription().getEbenenListe()) {
                    enumValues.add(etkEbene.getName());
                }
            } else {
                if (tableName.equals(TABLE_POOL) && fieldName.equals(FIELD_P_SPRACH)) {
                    enumValues.add(EtkDataImage.IMAGE_LANGUAGE_NEUTRAL);
                    for (String lang : config.getDatabaseLanguages()) {
                        enumValues.add(lang);
                    }
                } else {
                    if (tableName.equals(TABLE_ENUM) && fieldName.equals(FIELD_E_SHOWICON)) {
                        enumValues.add(Integer.toString(EnumDataType.ENUMSHOWTEXT));
                        enumValues.add(Integer.toString(EnumDataType.ENUMSHOWICON));
                        enumValues.add(Integer.toString(EnumDataType.ENUMSHOWTEXTANDICON));
                    } else {
                        if (tableName.equals(TABLE_POOL) && fieldName.equals(FIELD_P_USAGE)) {
                            enumValues.add(EtkDataImage.IMAGE_USAGE_2D);     // 2D
                            enumValues.add(EtkDataImage.IMAGE_USAGE_3D);     // 3D
                            enumValues.add(EtkDataImage.IMAGE_USAGE_PRINT);  // Druck
                        }
                    }
                }
            }
            for (String enumValue : enumValues) {
                if (values.indexOf(enumValue) < 0) {
                    values.add(enumValue);
                }
            }
        }
    }

    public static String getEnumDBValueOfDisplayValue(String displayValue, String tableName, String fieldName) {
        String result = displayValue;
        if (isEnumField(tableName, fieldName)) {
            if (tableName.equals(TABLE_POOL) && fieldName.equals(FIELD_P_SPRACH)) {
                if (displayValue.equals(EtkDataImage.IMAGE_LANGUAGE_NEUTRAL)) {
                    result = EtkDataImage.LANGUAGE_UNDEFINED;
                }
            }
        }
        return result;
    }

    public static void getEtkConfigStkLstFields(EtkProject project, String MemoryKey, EtkEditFields data) {
        boolean doAdd = false;
        EtkDisplayFields editFields = new EtkDisplayFields();
        EtkDisplayFields dataFields = new EtkDisplayFields();
        data.clear();
        EtkStuecklistenDescription.getStkLstFelder(project.getConfig(), EtkConfigConst.EDIT_STK_FIELDS, editFields);
        for (int lfdNr = 0; lfdNr < editFields.size(); lfdNr++) {
            EtkDisplayField editField = editFields.getFeld(lfdNr);
            if (editField.isMultiLanguage()) {
                // Mehrsprachige dürfen in Data mehrmals vorkommen, wenn sie
                // unterschiedliche Sprachen haben
                doAdd = dataFields.getIndexOfFeldAndSprache(editField.getKey(), editField.getLanguage()) == -1;
            } else {
                doAdd = dataFields.getIndexOfFeld(editField.getKey().getTableName(), editField.getKey().getFieldName(), false) == -1;
            }
            if (doAdd) {
                EtkDisplayField edField = new EtkDisplayField();
                edField.assign(editField);
                dataFields.addFeld(edField);
                EtkEditField eField = new EtkEditField();
                edField.assign(editField);
                data.addFeld(eField);
            }
        }
    }

    /**
     * Fügt Feld (falls noch nicht vorhanden) zu Feldliste hinzu
     *
     * @param editFields
     * @param stkFields
     * @param tableName
     * @param fieldName
     * @param visible
     * @param width
     */
    public static void addFieldToEditFields(EtkDisplayFields editFields, EtkDisplayFields stkFields, String tableName, String fieldName, boolean visible, int width) {
        if (editFields.getIndexOfFeld(tableName, fieldName, false) < 0) {
            EtkDisplayField edField = new EtkDisplayField();
            edField.assign(stkFields.getFeldByName(tableName, fieldName, false));
            edField.setVisible(visible);
            if (width > 0) {
                edField.setWidth(width);
            }
            editFields.addFeld(edField);
        }
    }

    public static void addFieldToEditFields(EtkDisplayFields editFields, EtkDisplayFields stkFields, String tableName, String fieldName, boolean visible) {
        addFieldToEditFields(editFields, stkFields, tableName, fieldName, visible, -1);
    }

    public static void addFieldToEditFields(EtkEditFields editFields, EtkEditFields partListFields, String tableName, String fieldName, boolean visible, int width) {
        EtkDisplayFieldKeyNormal base = new EtkDisplayFieldKeyNormal(tableName, fieldName);
        int index = partListFields.getIndexOfTableAndFeldName(base);
        if (index != -1) {
            if (editFields.getIndexOfTableAndFeldName(base) < 0) {
                EtkEditField edField = new EtkEditField();
                edField.assign(partListFields.getFeld(index));
                edField.setVisible(visible);
                if (width > 0) {
                    edField.setWidth(width);
                }
                editFields.addFeld(edField);
            }
        }
    }

    public static void addFieldToEditFields(EtkEditFields editFields, EtkDisplayFields partListFields, String tableName, String fieldName, boolean visible, int width) {
        EtkDisplayFieldKeyNormal base = new EtkDisplayFieldKeyNormal(tableName, fieldName);
        int index = partListFields.getIndexOfFeld(tableName, fieldName, false);
        if (index != -1) {
            if (editFields.getIndexOfTableAndFeldName(base) < 0) {
                EtkEditField edField = new EtkEditField();
                edField.assign(partListFields.getFeld(index));
                edField.setVisible(visible);
                if (width > 0) {
                    edField.setWidth(width);
                }
                editFields.addFeld(edField);
            }
        }
    }

    public static boolean isUserDefined(EtkProject project, String tableName, String fieldName) {
        return project.getFieldDescription(tableName, fieldName).isUserDefined();
    }

    /**
     * Benutzerdefinierte Felder hinzufügen
     *
     * @param project
     * @param editFields
     * @param stkFields
     * @param tableName
     */
    public static void addUserDefinedFields(EtkProject project, EtkDisplayFields editFields, EtkDisplayFields stkFields, String tableName) {
        for (int lfdNr = 0; lfdNr < stkFields.size(); lfdNr++) {
            if (isUserDefined(project, tableName, stkFields.getFeld(lfdNr).getKey().getFieldName())) {
                EtkDisplayField edField = new EtkDisplayField();
                edField.assign(stkFields.getFeld(lfdNr));
                editFields.addFeld(edField);
            }
        }
    }

    public static void addUserDefinedFields(EtkProject project, EtkEditFields editFields, EtkDisplayFields stkFields, String tableName) {
        for (int lfdNr = 0; lfdNr < stkFields.size(); lfdNr++) {
            if (isUserDefined(project, tableName, stkFields.getFeld(lfdNr).getKey().getFieldName())) {
                EtkEditField edField = new EtkEditField();
                edField.assign(stkFields.getFeld(lfdNr));
                editFields.addFeld(edField);
            }
        }
    }

    public static void addUserDefinedFields(EtkProject project, EtkEditFields editFields, EtkEditFields stkFields, String tableName) {
        for (int lfdNr = 0; lfdNr < stkFields.size(); lfdNr++) {
            if (isUserDefined(project, tableName, stkFields.getFeld(lfdNr).getKey().getFieldName())) {
                EtkEditField edField = new EtkEditField();
                edField.assign(stkFields.getFeld(lfdNr));
                editFields.addFeld(edField);
            }
        }
    }

    private static void addFieldList(EtkEditFields editFields, EtkDisplayFields fieldList, boolean visible) {
        boolean doCopy = false;
        for (int lfdNr = 0; lfdNr < fieldList.size(); lfdNr++) {
            EtkDisplayField field = fieldList.getFeld(lfdNr);
            if (field.isMultiLanguage()) {
                doCopy = editFields.getIndexOfTableAndFeldNameAndSprache(field.getKey(), field.getLanguage()) == -1;
            } else {
                doCopy = editFields.getIndexOfFeld(field) == -1;
            }
            if (doCopy) {
                EtkEditField edField = new EtkEditField();
                edField.assign(field);
                edField.setVisible(visible);
                editFields.addFeld(edField);
            }
        }
    }

    private static void addFieldList(EtkEditFields editFields, EtkEditFields fieldList, boolean visible) {
        boolean doCopy = false;
        for (int lfdNr = 0; lfdNr < fieldList.size(); lfdNr++) {
            EtkEditField field = fieldList.getFeld(lfdNr);
            if (field.isMultiLanguage()) {
                doCopy = editFields.getIndexOfTableAndFeldNameAndSprache(field.getKey(), field.getLanguage()) == -1;
            } else {
                doCopy = editFields.getIndexOfFeld(field) == -1;
            }
            if (doCopy) {
                EtkEditField edField = new EtkEditField();
                edField.assign(field);
                edField.setVisible(visible);
                editFields.addFeld(edField);
            }
        }
    }

    public static boolean isEditableField(String tableName, String fieldName) {
        if (tableName.equals(TABLE_KAPITEL)) {
            return !Utils.contains(new String[]{ FIELD_K_KNOTEN, FIELD_K_KNVER, FIELD_K_SPRACH, FIELD_K_LFDNR, FIELD_K_KAP,
                                                 FIELD_K_NR, FIELD_K_NRVER, FIELD_K_DSPRACH, FIELD_K_VKNOTEN, FIELD_K_VKNVER,
                                                 FIELD_K_VSPRACH, FIELD_K_SEQNR, DBConst.FIELD_STAMP }, fieldName);
        } else {
            if (tableName.equals(TABLE_DOKULINK)) {
                return !Utils.contains(new String[]{ FIELD_D_KNOTEN, FIELD_D_KNVER, FIELD_D_SPRACH, FIELD_D_LFDNR, FIELD_D_KAP,
                                                     FIELD_D_NR, FIELD_D_VER, FIELD_D_DSPRACH, FIELD_D_VKNOTEN, FIELD_D_VKNVER,
                                                     FIELD_D_VSPRACH, FIELD_D_KVARI, FIELD_D_KVER, FIELD_D_MATNR, FIELD_D_MVER,
                                                     FIELD_D_SEQNR, DBConst.FIELD_STAMP }, fieldName);
            } else {
                if (tableName.equals(TABLE_KATALOG)) {
                    return !Utils.contains(new String[]{ FIELD_K_SACH, FIELD_K_SVER, FIELD_K_LFDNR, FIELD_K_SEQNR, DBConst.FIELD_STAMP }, fieldName);
                } else {
                    if (tableName.equals(TABLE_IMAGES)) {
                        return !Utils.contains(new String[]{ FIELD_I_TIFFNAME, FIELD_I_VER, FIELD_I_BLATT, DBConst.FIELD_STAMP }, fieldName);
                    } else {
                        if (tableName.equals(TABLE_STRUKT)) {
                            return !Utils.contains(new String[]{ FIELD_S_KNOTEN, FIELD_S_VER, FIELD_S_LFDNR, FIELD_S_TYP,
                                                                 FIELD_S_VKNOTEN, FIELD_S_VVER, FIELD_S_KVARI, FIELD_S_KVER, DBConst.FIELD_STAMP }, fieldName);
                        }
                    }
                }
            }
        }
        return !fieldName.equals(DBConst.FIELD_STAMP);
    }

    private static boolean isDefaultEditField(EtkProject project, String tableName, String fieldName) {
        if (isUserDefined(project, tableName, fieldName)) {
            return true;
        }
        if (tableName.equals(TABLE_POOL)) {
            return Utils.contains(new String[]{ FIELD_P_IMAGES, FIELD_P_SPRACH, FIELD_P_USAGE }, fieldName);
        }
        if (tableName.equals(TABLE_KATALOG)) {
            return Utils.contains(new String[]{ FIELD_K_POS, FIELD_K_MATNR, FIELD_K_MENGE, FIELD_K_MENGEART }, fieldName);
        }
        if (tableName.equals(TABLE_MAT)) {
            return Utils.contains(new String[]{ FIELD_M_MATNR, FIELD_M_TEXTNR, FIELD_M_BESTNR, FIELD_M_BESTFLAG }, fieldName);
        }
        if (tableName.equals(TABLE_DOKU)) {
            return Utils.contains(new String[]{ FIELD_D_SPRACH, FIELD_D_NR, FIELD_D_TITEL, FIELD_D_FILE }, fieldName);
        }
        if (tableName.equals(TABLE_KAPITEL)) {
            return Utils.contains(new String[]{ FIELD_K_TEXT, FIELD_K_SEITE, FIELD_K_FETT }, fieldName);
        }
        if (tableName.equals(TABLE_IMAGES)) {
            return Utils.contains(new String[]{ FIELD_I_IMAGES, FIELD_I_BLATT }, fieldName);
        }
        if (tableName.equals(TABLE_POOLENTRY)) {
            return Utils.contains(new String[]{ FIELD_PE_IMAGES }, fieldName);
        }
        if (tableName.equals(TABLE_DOKULINK)) {
            return Utils.contains(new String[]{ FIELD_D_TEXT, FIELD_D_SEITE, FIELD_D_KVARI, FIELD_D_MATNR }, fieldName);
        }
        if (tableName.equals(TABLE_STRUKT)) {
            return Utils.contains(new String[]{ FIELD_S_TEXT, FIELD_S_ICON, FIELD_S_KVARI }, fieldName);
        }
        if (tableName.equals(TABLE_BEST_H)) {
            return Utils.contains(new String[]{ FIELD_B_BETREFF, FIELD_B_VERSAND, FIELD_B_BDATUM, FIELD_B_DATUM, FIELD_B_ART }, fieldName);
        }
        return false;
    }


    /**
     * Liefert die Konfigurierten Editfelder für den Baugruppen-Edit-Dialog.
     * Falls K_VARI/K_VER/K_SACH/K_SVER nicht zu den Konfigurierten Editfeldern gehört, werden
     * diese Felder ergänzt. Die Editierbaren Felder erhalten das Attribut
     * TStücklistenFeld.Visible = True, die anderen = False.
     *
     * @param project
     * @param editFields
     */

    public static void getEditFieldsAssembly(EtkProject project, EtkEditFields editFields) {
        EtkDisplayFields editFieldsCatalog;
        EtkDisplayFields editFieldsMaterial;
        //EtkDisplayFields editDisplayFields;
        EtkEditFields editFieldsEditable = new EtkEditFields();
        editFieldsMaterial = EtkDbsHelper.getDescriptionAsEditField(TABLE_MAT, project);
        editFieldsCatalog = EtkDbsHelper.getDescriptionAsEditField(TABLE_KATALOG, project);
        getEtkConfigStkLstFields(project, EtkConfigConst.EDIT_BAUGRUPPEN_FIELDS, editFieldsEditable);
        if (editFieldsEditable.size() == 0) {

            // Die Default-Edifelder selbst zusammenbasteln :
            addFieldToEditFields(editFieldsEditable, editFieldsMaterial, TABLE_MAT, FIELD_M_MATNR, true, -1);
            addFieldToEditFields(editFieldsEditable, editFieldsMaterial, TABLE_MAT, FIELD_M_TEXTNR, true, -1);
            addFieldToEditFields(editFieldsEditable, editFieldsCatalog, TABLE_KATALOG, FIELD_K_EBENE, true, -1);
            addFieldToEditFields(editFieldsEditable, editFieldsMaterial, TABLE_MAT, FIELD_M_BESTNR, true, -1);
            addFieldToEditFields(editFieldsEditable, editFieldsMaterial, TABLE_MAT, FIELD_M_BESTFLAG, true, -1);

            // Userdefined Fields Katalog, Material
            addUserDefinedFields(project, editFieldsEditable, editFieldsCatalog, TABLE_KATALOG);
            addUserDefinedFields(project, editFieldsEditable, editFieldsMaterial, TABLE_MAT);
        }
        // Jetzt EditFelder Füllen ; erst mal die editierbaren
        //editDisplayFields = new EtkDisplayFields();
        addFieldList(/*editDisplayFields*/ editFields, editFieldsEditable, true);

        // Jetzt noch sicherstellen, dass mindestens ein Feld vorhanden ist,
        // das nötig ist um im Editor eine neue BG zu erzeugen
//        if ((editDisplayFields.getIndexOfFeld(TABLE_KATALOG, FIELD_K_VARI, false) < 0) &&
//            (editDisplayFields.getIndexOfFeld(TABLE_KATALOG, FIELD_K_MATNR, false) < 0) &&
//            (editDisplayFields.getIndexOfFeld(TABLE_MAT, FIELD_M_MATNR, false) < 0)) {
//            addFieldToEditFields(editDisplayFields, editFieldsMaterial, TABLE_MAT, FIELD_M_MATNR, true);
//        }
        if ((editFields.getFeldByName(TABLE_KATALOG, FIELD_K_VARI, false) == null) &&
            (editFields.getFeldByName(TABLE_KATALOG, FIELD_K_MATNR, false) == null) &&
            (editFields.getFeldByName(TABLE_MAT, FIELD_M_MATNR, false) == null)) {
            addFieldToEditFields(editFields, editFieldsMaterial, TABLE_MAT, FIELD_M_MATNR, true, -1);
        }
        // jetzt noch die restlichen Felder ergaenzen
//        addFieldList(editDisplayFields, editFieldsCatalog, false);
//        addFieldList(editDisplayFields, editFieldsMaterial, false);
        addFieldList(editFields, editFieldsCatalog, false);
        addFieldList(editFields, editFieldsMaterial, false);
        // Editing : means field is editable
//        editFields.clear();
        for (int lfdNr = 0; lfdNr < /*editDisplayFields.size()*/ editFields.size(); lfdNr++) {
//            EtkDisplayField field = editDisplayFields.getFeld(lfdNr);
//            EtkEditField eField = new EtkEditField(field.getKey().getNameMechanic(), field.isMultiLanguage());
//            if (!eField.getKey().getFieldName().equals(DBConst.FIELD_STAMP)) {
//                eField.assign(field);
//                eField.setEditierbar(isEditableField(field.getKey().getTableName(), field.getKey().getFieldName()));
//                editFields.addField(eField);
//            }

            editFields.getFeld(lfdNr).setEditierbar(isEditableField(editFields.getFeld(lfdNr).getKey().getTableName(), editFields.getFeld(lfdNr).getKey().getFieldName()));
        }

    }

    private static void getDescriptionAsEditFields(EtkProject project, String tableName, EtkEditFields editFields) {
        editFields.clear();
        AbstractEtkDisplayFields<EtkDisplayFieldWithAbstractKey> felder = AbstractEtkDisplayFields.descriptionAsEtkDisplayFields(tableName, project.getConfig());
        for (EtkDisplayFieldWithAbstractKey field : felder.getFields()) {
            EtkEditField eField = new EtkEditField(field.getKey().getNameMechanic(), field.isMultiLanguage());
            if (!eField.getKey().getFieldName().equals(DBConst.FIELD_STAMP)) {
                eField.assign(field);
                editFields.addField(eField);
            }
        }
    }

    /**
     * Holt aus der Tabellenbeschreibung alle Felder und befüllt die übergebenen {@link EtkEditFields} mit den jeweiligen
     * {@link EtkEditField}s. Als Parameter kann auch ein Array mit Feldnamen übergeben werden, die nicht zu den EtkEditFields
     * hinzugefügt werden sollen.
     *
     * @param project
     * @param tableName
     * @param editFields
     * @param omittedFieldNames
     */
    public static void getDescriptionAsEditFields(EtkProject project, String tableName, EtkEditFields editFields, String... omittedFieldNames) {
        editFields.clear();
        AbstractEtkDisplayFields<EtkDisplayFieldWithAbstractKey> felder = AbstractEtkDisplayFields.descriptionAsEtkDisplayFields(tableName, project.getConfig());
        Set<String> omittedFields;
        if (omittedFieldNames == null) {
            omittedFields = new HashSet<String>();
        } else {
            omittedFields = new HashSet<String>(StrUtils.toStringArrayList(omittedFieldNames));
        }
        for (EtkDisplayFieldWithAbstractKey field : felder.getFields()) {
            EtkEditField eField = new EtkEditField(field.getKey().getNameMechanic(), field.isMultiLanguage());
            boolean isOmittedField = false;
            if (!omittedFields.isEmpty()) {
                isOmittedField = omittedFields.contains(eField.getKey().getFieldName());
            }
            if (!eField.getKey().getFieldName().equals(DBConst.FIELD_STAMP)) {
                eField.assign(field);
                eField.setVisible(!isOmittedField);
                editFields.addField(eField);
            }
        }
    }

    /**
     * Konvertiert die übergebenen {@link EtkDisplayFields} in {@link EtkEditFields}
     *
     * @param displayFields
     * @param editFields
     */
    public static void convertDisplayFieldsToEditFields(EtkDisplayFields displayFields, EtkEditFields editFields) {
        editFields.clear();
        if ((displayFields == null) || (displayFields.size() == 0)) {
            return;
        }
        for (EtkDisplayFieldWithAbstractKey field : displayFields.getFields()) {
            EtkEditField eField = new EtkEditField(field.getKey().getNameMechanic(), field.isMultiLanguage());
            if (!eField.getKey().getFieldName().equals(DBConst.FIELD_STAMP)) {
                eField.assign(field);
                editFields.addField(eField);
            }
        }
    }

    private static void getPartListFieldsFromConfig(EtkProject project, String key, EtkEditFields editFields) {
        EtkEditFields eFields = new EtkEditFields();
        eFields.load(project.getConfig(), key);
        boolean doAdd;
        for (EtkEditField field : eFields.getVisibleEditFields()) {
            if (field.isMultiLanguage()) {
                // Mehrsprachige dürfen in Data mehrmals vorkommen, wenn sie
                // unterschiedliche Sprachen haben
                doAdd = editFields.getIndexOfTableAndFeldNameAndSprache(field.getKey(), field.getLanguage()) < 0;
            } else {
                // nicht mehrsprachige, also insbesondere die Keys, dürfen in Data nicht doppelt vorkommen
                doAdd = editFields.getIndexOfTableAndFeldName(field.getKey()) < 0;
            }
            if (doAdd) {
                EtkEditField eField = new EtkEditField(field.getKey().getNameMechanic(), field.isMultiLanguage());
                eField.assign(field);
                editFields.addField(eField);
            }
        }
    }

    private static void getDefaultEditFields(EtkProject project, String tableName, EtkEditFields editFields) {
        getDescriptionAsEditFields(project, tableName, editFields);
        for (int lfdNr = editFields.size() - 1; lfdNr >= 0; lfdNr--) {
            if (isDefaultEditField(project, tableName, editFields.getFeld(lfdNr).getKey().getFieldName())) {
                editFields.deleteFeld(lfdNr);
            } else {
                editFields.getFeld(lfdNr).setEditierbar(true);
            }
        }
    }

    public static void getEditFields(EtkProject project, String tableName, EtkEditFields editFields, boolean showAllFields) {
        editFields.clear();
        if (!tableName.isEmpty()) {

            if (showAllFields) {
                // EditFelder aus Description füllen
                getDescriptionAsEditFields(project, tableName, editFields);
            } else if (tableName.equals(TABLE_DOKULINK)) { // EditFelder aus EtkConfig füllen
                getEditFieldsForFieldNameWithText(EtkConfigConst.EDIT_DOKULINK_FIELDS, tableName, FIELD_D_TEXT, true,
                                                  editFields, project);
            } else if (tableName.equals(TABLE_KAPITEL)) {
                getEditFieldsForFieldNameWithText(EtkConfigConst.EDIT_KAPITEL_FIELDS, tableName, FIELD_K_TEXT, true,
                                                  editFields, project);
            } else if (tableName.equals(TABLE_IMAGES)) {
                EtkEditFields tmpEditFields = getEditFieldsForTable(project, tableName, EtkConfigConst.EDIT_IMAGES_FIELDS);
                // tmpEditFelder an EditFelder anhängen
                for (EtkEditField field : tmpEditFields.getVisibleEditFields()) {
                    editFields.addField(field);
                }
            } else if (tableName.equals(TABLE_STRUKT)) {
                getEditFieldsForFieldNameWithText(EtkConfigConst.EDIT_STRUCTURE_FIELDS, tableName, FIELD_S_TEXT, false,
                                                  editFields, project);

                // Jetzt noch sicherstellen, dass die Keys mit enthalten sind
                EtkEditFields editFieldsStruct = new EtkEditFields();
                getDescriptionAsEditFields(project, tableName, editFieldsStruct);
                addFieldToEditFields(editFields, editFieldsStruct, tableName, FIELD_S_KNOTEN, false, -1);
                addFieldToEditFields(editFields, editFieldsStruct, tableName, FIELD_S_VER, false, -1);
                addFieldToEditFields(editFields, editFieldsStruct, tableName, FIELD_S_LFDNR, false, -1);
                addFieldToEditFields(editFields, editFieldsStruct, tableName, FIELD_S_VKNOTEN, false, -1);
                addFieldToEditFields(editFields, editFieldsStruct, tableName, FIELD_S_VVER, false, -1);
                addFieldToEditFields(editFields, editFieldsStruct, tableName, FIELD_S_KVARI, false, -1);
                addFieldToEditFields(editFields, editFieldsStruct, tableName, FIELD_S_KVER, false, -1);
            } else if (tableName.equals(TABLE_MAT)) {
                EtkEditFields tmpEditFields = getEditFieldsForTable(project, tableName, EtkConfigConst.EDIT_MATERIAL_FIELDS);
                // tmpEditFelder an EditFelder anhängen
                for (EtkEditField field : tmpEditFields.getVisibleEditFields()) {
                    editFields.addField(field);
                }
                // Jetzt noch sicherstellen, dass die Keys mit enthalten sind
                EtkEditFields editFieldsMaterial = new EtkEditFields();
                getDescriptionAsEditFields(project, tableName, editFieldsMaterial);
                addFieldToEditFields(editFields, editFieldsMaterial, tableName, FIELD_M_MATNR, false, -1);
                addFieldToEditFields(editFields, editFieldsMaterial, tableName, FIELD_M_VER, false, -1);
            } else if (tableName.equals(TABLE_DOKU)) {
                EtkEditFields tmpEditFields = getEditFieldsForTable(project, tableName, EtkConfigConst.EDIT_DOKUPOOL_FIELDS);
                int index = tmpEditFields.getIndexOfTableAndFeldName(new EtkDisplayFieldKeyNormal(tableName, FIELD_D_SPRACH));
                if (index >= 0) {
                    tmpEditFields.getFeld(index).setWidth(6);
                }
                // tmpEditFelder an EditFelder anhängen
                for (EtkEditField field : tmpEditFields.getVisibleEditFields()) {
                    editFields.addField(field);
                }
                // Jetzt noch sicherstellen, dass die Keys mit enthalten sind
                EtkEditFields newEditFields = new EtkEditFields();
                getDescriptionAsEditFields(project, tableName, newEditFields);
                addFieldToEditFields(editFields, newEditFields, tableName, FIELD_D_SPRACH, false, -1);
                addFieldToEditFields(editFields, newEditFields, tableName, FIELD_D_NR, false, -1);
                addFieldToEditFields(editFields, newEditFields, tableName, FIELD_D_FILE, false, -1);
                addFieldToEditFields(editFields, newEditFields, tableName, FIELD_D_VER, false, -1);
            } else if (tableName.equals(TABLE_POOL)) {
                EtkEditFields tmpEditFields = getEditFieldsForTable(project, tableName, EtkConfigConst.EDIT_ZEICHNUNGSPOOL_FIELDS);
                //Passende Breiten Setzen
                int index = tmpEditFields.getIndexOfTableAndFeldName(new EtkDisplayFieldKeyNormal(tableName, FIELD_P_SPRACH));
                if (index >= 0) {
                    tmpEditFields.getFeld(index).setWidth(6);
                }
                index = tmpEditFields.getIndexOfTableAndFeldName(new EtkDisplayFieldKeyNormal(tableName, FIELD_P_USAGE));
                if (index >= 0) {
                    tmpEditFields.getFeld(index).setWidth(6);
                }
                //Blob entfernen
                index = tmpEditFields.getIndexOfTableAndFeldName(new EtkDisplayFieldKeyNormal(tableName, FIELD_P_DATA));
                if (index >= 0) {
                    tmpEditFields.deleteFeld(index);
                }
                // tmpEditFelder an EditFelder anhängen
                for (EtkEditField field : tmpEditFields.getVisibleEditFields()) {
                    editFields.addField(field);
                }
                // Jetzt noch sicherstellen, dass die Keys mit enthalten sind
                EtkEditFields newEditFields = new EtkEditFields();
                getDescriptionAsEditFields(project, tableName, newEditFields);
                addFieldToEditFields(editFields, newEditFields, tableName, FIELD_P_IMAGES, false, -1);
                addFieldToEditFields(editFields, newEditFields, tableName, FIELD_P_VER, false, -1);
                addFieldToEditFields(editFields, newEditFields, tableName, FIELD_P_SPRACH, true, -1);
                addFieldToEditFields(editFields, newEditFields, tableName, FIELD_P_USAGE, true, -1);
                addFieldToEditFields(editFields, newEditFields, tableName, FIELD_P_IMGTYPE, false, -1);
            } else if (tableName.equals(TABLE_POOLENTRY)) {
                getEditFieldsPoolEntryEdit(project, editFields);
            } else if (tableName.equals(iPartsConst.TABLE_DA_MODULE)) {
                getEditFieldsForModuleEntryEdit(project, editFields);
            } else {
                getDescriptionAsEditFields(project, tableName, editFields);
            }
        }
    }

    private static void getEditFieldsForFieldNameWithText(String editFieldsKey, String tableName, String textFieldName,
                                                          boolean addFieldsFromDescription, EtkEditFields editFields,
                                                          EtkProject project) {
        EtkEditFields tmpEditFields = getEditFieldsForTable(project, tableName, editFieldsKey);
        // Als erstes Textfeld hinzufügen
        addFieldToEditFields(editFields, tmpEditFields, tableName, textFieldName, true, -1);

        EtkEditFields newEditFields = null;
        if (addFieldsFromDescription) {
            // Falls es nicht konfiguriert war, dann Textfeld aus der Description holen
            newEditFields = new EtkEditFields();
            getDescriptionAsEditFields(project, tableName, newEditFields);
            addFieldToEditFields(editFields, newEditFields, tableName, textFieldName, true, -1);
        }

        // Dann tmpEditFields, d.h. die konfigurierten Felder, anhängen soweit noch nicht drin
        for (EtkEditField field : tmpEditFields.getVisibleEditFields()) {
            editFields.addField(field);
        }

        if (addFieldsFromDescription) {
            // Jetzt den Rest invisible dranhängen
            for (EtkEditField field : newEditFields.getVisibleEditFields()) {
                field.setVisible(false);
                editFields.addField(field);
            }
        }
    }

    private static EtkEditFields getEditFieldsForTable(EtkProject project, String tableName, String key) {
        EtkEditFields tmpEditFields = new EtkEditFields();
        getPartListFieldsFromConfig(project, key, tmpEditFields);
        if (tmpEditFields.size() == 0) {
            getDefaultEditFields(project, tableName, tmpEditFields);
        }
        return tmpEditFields;
    }

    public static void getEditFieldMaterialEdit(EtkProject project, EtkEditFields editFields) {
        getEditFields(project, TABLE_MAT, editFields, false);
    }

    public static void getEditFieldMaterialEditAll(EtkProject project, EtkEditFields editFields) {
        getEditFields(project, TABLE_MAT, editFields, true);
    }

    public static void getEditFieldsPoolEntryEdit(EtkProject project, EtkEditFields editFields) {
        EtkEditFields allTableFields = new EtkEditFields();
        EtkEditFields editFieldsPool = new EtkEditFields();
        getDescriptionAsEditFields(project, TABLE_POOLENTRY, allTableFields);
        getDefaultEditFields(project, TABLE_POOLENTRY, editFields);
        // wenn bei den PoolEntrys P_Ver konfiguriert, wird PE_Ver hinzugefügt
        getEditFields(project, TABLE_POOL, editFieldsPool, false);
        EtkEditField poolField = editFieldsPool.getFeldByName(TABLE_POOL, FIELD_P_VER);
        if (poolField != null) {
            addFieldToEditFields(editFields, allTableFields, TABLE_POOLENTRY, FIELD_PE_VER, poolField.isVisible(), -1);
        }
    }

    public static void getEditFieldsForModuleEntryEdit(EtkProject project, EtkEditFields editFields) {
        // Die eingestellten Felder aus der Konfiguration holen
        EtkEditFields tmpEditFields = getEditFieldsForTable(project, iPartsConst.TABLE_DA_MODULE,
                                                            iPartsEditConfigConst.iPARTS_EDIT_MASTER_MODULE_KEY +
                                                            iPartsEditConfigConst.REL_EDIT_MASTER_EDITFIELDS);
        // Sind keine Felder konfiguriert, alle Felder anzeigen.
        if (tmpEditFields.size() <= 0) {
            getDescriptionAsEditFields(project, iPartsConst.TABLE_DA_MODULE, tmpEditFields);
        }

        // Sicherstellen, dass das Schlüsselfeld enthalten ist ...
        EtkEditField keyField = tmpEditFields.getFeldByName(iPartsConst.TABLE_DA_MODULE, iPartsConst.FIELD_DM_MODULE_NO);
        EtkEditFields missingKeyFields = new EtkEditFields();
        if (keyField == null) {
            EtkEditFields allEditFields = new EtkEditFields();
            getDescriptionAsEditFields(project, iPartsConst.TABLE_DA_MODULE, allEditFields);
            addFieldToEditFields(missingKeyFields, allEditFields, iPartsConst.TABLE_DA_MODULE, iPartsConst.FIELD_DM_MODULE_NO, true, -1);
        }

        // Jetzt erst die fehlenden Schlüsselfelder in die Ergebnisliste übertragen:
        // Schlüsselfelder vor normalen Feldern, außer es ist über die Konfiguration anders festgelegt.
        for (EtkEditField field : missingKeyFields.getVisibleFields()) {
            editFields.addField(field);
        }

        // Dann die ermittelten Felder an Ergebnisfelder anhängen:
        for (EtkEditField field : tmpEditFields.getVisibleEditFields()) {
            editFields.addField(field);
        }

        // Wenn KEIN Change-Set aktiv ist, dürfen die angezeigten Felder nicht editiert werden können.
        if (!project.getEtkDbs().isRevisionChangeSetActiveForEdit()) {
            for (EtkEditField field : editFields.getVisibleEditFields()) {
                field.setEditierbar(false);
            }
        } else {
            // Das Feld FIELD_DM_SPECIAL_TU darf nur vom Produktadmin editiert werden
            keyField = editFields.getFeldByName(iPartsConst.TABLE_DA_MODULE, iPartsConst.FIELD_DM_SPECIAL_TU);
            if (keyField != null) {
                String loginUserIdForSession = iPartsUserAdminDb.getLoginUserIdForSession();
                keyField.setEditierbar(iPartsUserAdminCache.getInstance(loginUserIdForSession).isUserRole(iPartsUserAdminDb.ROLE_ID_PRODUCT_ADMIN));
            }
        }

        // Zum Schluss noch sicherstellen, dass das Schlüsselfeld NIE editierbar ist!
        keyField = editFields.getFeldByName(iPartsConst.TABLE_DA_MODULE, iPartsConst.FIELD_DM_MODULE_NO);
        if (keyField != null) {
            keyField.setEditierbar(false);
        }
    }

    // Laden der Einstellungen für den Stücklistenedit
    public static void getAssemblyFieldsEditList(EtkProject project, String rootKey, EtkEditFieldsEditList data) {
        List<String> typNames;
        EtkEditFieldsEdit item;

        data.clear();
        // Tabellen laden
        typNames = project.getConfig().getKeys(rootKey + "/Items");
        data.setStandardTypName(project.getConfig().getString(rootKey + "/StdTyp", ""));
        for (String typName : typNames) {
            item = new EtkEditFieldsEdit();
            // Im Namen das 'K' wieder entfernen
            item.setName(typName.substring(1));
            // Jetzt alle Daten der Ebene laden
            String key = rootKey + "/Items/" + typName;
            item.load(project.getConfig(), key);
            data.add(item);
        }
    }

    public static void getEditFieldsForProductEdit(EtkProject project, String defaultAssemblyTyp, EtkEditFields editFields) {
        EtkEditFieldsEditList allAssemblyTypes = new EtkEditFieldsEditList();
        EtkEditFieldsEdit myTyp;

        getAssemblyFieldsEditList(project, EtkConfigConst.EDIT_STK_FIELDS, allAssemblyTypes);
        myTyp = allAssemblyTypes.getTypByName(defaultAssemblyTyp, defaultAssemblyTyp.isEmpty());
        if (myTyp != null) {
            editFields.addFelder(myTyp);
        } else {
            // versuche Default-Typ
            myTyp = allAssemblyTypes.getTypByName("", true);
            if (myTyp != null) {
                editFields.addFelder(myTyp);
            }
        }
        if (editFields.size() == 0) {
            // Defaulteditfelder
            EtkEditFields editFieldsKatalog = new EtkEditFields();
            EtkEditFields editFieldsMaterial = new EtkEditFields();
            editFields.clear();

            getDescriptionAsEditFields(project, TABLE_KATALOG, editFieldsKatalog);
            getDescriptionAsEditFields(project, TABLE_MAT, editFieldsMaterial);

            addFieldToEditFields(editFields, editFieldsMaterial, TABLE_MAT, FIELD_M_MATNR, true, -1);
            addFieldToEditFields(editFields, editFieldsMaterial, TABLE_MAT, FIELD_M_TEXTNR, true, -1);

            // Userdefined Fields
            addUserDefinedFields(project, editFields, editFieldsKatalog, TABLE_KATALOG);

        }
        // Editing : Nachbearbeitung
        for (int lfdNr = 0; lfdNr < editFields.size(); lfdNr++) {
            EtkEditField field = editFields.getFeld(lfdNr);
            if (field.getKey().getTableName().equals(TABLE_KATALOG)) {
                if (field.isEditierbar()) {
                    field.setEditierbar(isEditableField(field.getKey().getTableName(), field.getKey().getFieldName()));
                }
            } else {
                field.setEditierbar(false);
            }
        }
    }

    public static void removePSKMatFieldsIfNecessary(AbstractEtkDisplayFields<? extends EtkEditField> editFields, boolean isPSKAllowed) {
        // PSK-Teilestammfelder nur als PSK-Benutzer anzeigen
        if (!isPSKAllowed) {
            Iterator<? extends EtkEditField> fieldsIterator = editFields.getFields().iterator();
            while (fieldsIterator.hasNext()) {
                EtkEditField field = fieldsIterator.next();
                if (field.getKey().getName().startsWith(iPartsConst.MAT_PSK_TABLE_AND_FIELD_PREFIX)) {
                    fieldsIterator.remove();
                }
            }
        }
    }

    /*====================================================================================*/
    final static private String CONFIG_KEY_DIALOG_MATRIX_EDIT_PARTENTRY = "Plugin/iPartsEdit/EditPartsList/Dialog";
    final static private String CONFIG_KEY_DIALOG_MATRIX_EDIT_PARTENTRY_REPLACEMENTS = "Plugin/iPartsEdit/EditPartsList/DialogReplacements";
    final static private String CONFIG_KEY_EDS_MATRIX_EDIT_PARTENTRY = "Plugin/iPartsEdit/EditPartsList/Eds";
    final static private String CONFIG_KEY_EDS_MATRIX_EDIT_PARTENTRY_REPLACEMENTS = "Plugin/iPartsEdit/EditPartsList/EdsReplacements";
    final static private String CONFIG_KEY_MATRIX_EDIT_MATERIAL = "Plugin/iPartsEdit/Material";

    public static void getEditFieldsForDocuType(EtkProject project, String defaultAssemblyTyp, iPartsDocumentationType docuType,
                                                String relatedInfoName, iPartsMatrixEditFields matrixEditFields) {
        matrixEditFields.clear();
        if (docuType.isPKWDocumentationType()) {
            if (StrUtils.isValid(relatedInfoName) && relatedInfoName.equals(iPartsConst.CONFIG_KEY_RELATED_INFO_EDIT_DETAILS_DATA)) {
                matrixEditFields.load(project.getConfig(), CONFIG_KEY_DIALOG_MATRIX_EDIT_PARTENTRY);
            } else {
                matrixEditFields.load(project.getConfig(), CONFIG_KEY_DIALOG_MATRIX_EDIT_PARTENTRY_REPLACEMENTS);
                if (matrixEditFields.size() == 0) { // Fallback auf Stücklistendaten-Konfiguration
                    matrixEditFields.load(project.getConfig(), CONFIG_KEY_DIALOG_MATRIX_EDIT_PARTENTRY);
                }
            }
        } else if (docuType.isTruckDocumentationType()) {
            if (StrUtils.isValid(relatedInfoName) && relatedInfoName.equals(iPartsConst.CONFIG_KEY_RELATED_INFO_EDIT_DETAILS_DATA)) {
                matrixEditFields.load(project.getConfig(), CONFIG_KEY_EDS_MATRIX_EDIT_PARTENTRY);
            } else {
                matrixEditFields.load(project.getConfig(), CONFIG_KEY_EDS_MATRIX_EDIT_PARTENTRY_REPLACEMENTS);
                if (matrixEditFields.size() == 0) { // Fallback auf Stücklistendaten-Konfiguration
                    matrixEditFields.load(project.getConfig(), CONFIG_KEY_EDS_MATRIX_EDIT_PARTENTRY);
                }
            }
        } else {
            docuType = iPartsDocumentationType.UNKNOWN;
        }
        if (matrixEditFields.size() == 0) {
            docuType = iPartsDocumentationType.UNKNOWN;
        }
        if (docuType == iPartsDocumentationType.UNKNOWN) {
            // als Rückfallposition: EditFields 3spaltig
            EtkEditFields editFields = new EtkEditFields();
            EtkEditFieldHelper.getEditFieldsForProductEdit(project, defaultAssemblyTyp, editFields);
            convertEditFieldsToMatrixEditFields(editFields, matrixEditFields, 3);
        }

        // Editing : Nachbearbeitung
        for (int lfdNr = 0; lfdNr < matrixEditFields.size(); lfdNr++) {
            iPartsMatrixEditField field = matrixEditFields.getFeld(lfdNr);
            if (field.getKey().getTableName().equals(TABLE_KATALOG)) {
                if (field.isEditierbar()) {
                    field.setEditierbar(isEditableField(field.getKey().getTableName(), field.getKey().getFieldName()));
                }
            } else {
                field.setEditierbar(false);
            }
        }
    }

    public static void convertEditFieldsToMatrixEditFields(EtkEditFields editFields, iPartsMatrixEditFields matrixEditFields, int colummCount) {
        int colNo = 0;
        int rowNo = 0;
        int colWidth = DWLayoutManager.get().isResponsiveMode() ? 59 : 60; // Bei Responsive sind die Abstände größer
        for (EtkEditField field : editFields.getFields()) {
            iPartsMatrixEditField matrixEditField = new iPartsMatrixEditField(field);
            matrixEditField.setWidth(colWidth);
            matrixEditField.setColNo(colNo);
            matrixEditField.setRowNo(rowNo);
            matrixEditField.setLabelWidth(matrixEditField.getWidth() * 35 / 100);
            matrixEditFields.addField(matrixEditField);
            colNo++;
            if (colNo >= colummCount) {
                colNo = 0;
                rowNo++;
            }
        }
    }

    public static void loadMatrixEditFields(EtkProject project, String tableName, iPartsMatrixEditFields matrixEditFields, boolean showAllFields) {
        matrixEditFields.clear();
        boolean doReformat = true;
        if (StrUtils.isValid(tableName) && tableName.equals(TABLE_MAT)) {
            matrixEditFields.load(project.getConfig(), CONFIG_KEY_MATRIX_EDIT_MATERIAL);
            doReformat = matrixEditFields.size() == 0;
        }
        if (doReformat) {
            EtkEditFields editFields = new EtkEditFields();
            getEditFields(project, tableName, editFields, showAllFields);
            convertEditFieldsToMatrixEditFields(editFields, matrixEditFields, 3);
        }
    }

    /**
     * Erzeugt ein {@link EtkEditField} für ein virtuelles Feld
     *
     * @param connector
     * @param virtualFieldName
     * @param visibleText
     * @return
     */
    public static EtkEditField getEditFieldForVirtualField(AssemblyListFormIConnector connector, String virtualFieldName, String visibleText) {
        List<EtkDisplayField> displayFields = connector.getCurrentAssembly().getEbene().getFields();
        // Suche in den DisplayFields nach virtualFieldName
        EtkDisplayField currentDisplayField = null;
        for (EtkDisplayField displayField : displayFields) {
            if (displayField.getKey().getFieldName().equals(virtualFieldName)) {
                currentDisplayField = displayField;
                break;
            }
        }
        EtkEditField editField;
        String language = connector.getProject().getViewerLanguage();
        if (currentDisplayField != null) {
            // Virtuelles Feld wird angezeigt => editField aus DisplayField zusammenbauen
            String fieldName = currentDisplayField.getKey().getFieldName();
            editField = new EtkEditField(currentDisplayField.getKey().getTableName(), fieldName,
                                         currentDisplayField.isMultiLanguage());
            editField.setArray(currentDisplayField.isArray());
            setLabelTextForVirtField(fieldName, editField, language, connector.getCurrentAssembly().getEbeneName(),
                                     currentDisplayField);
        } else {
            // Virtuelles Feld wird nicht angezeigt => editField für Anzeige zusammenbauen
            editField = new EtkEditField(iPartsConst.TABLE_KATALOG, virtualFieldName, false);
            // Text nicht in DisplayFields enthalten => selber setzen
            EtkMultiSprache multi = new EtkMultiSprache();
            multi.setText(language, TranslationHandler.translate(visibleText));
            editField.setText(multi);
            editField.setDefaultText(false);
        }
        return editField;
    }

    /**
     * Setzt den Text für das übergebene virtuelle Edit-Feld
     *
     * @param fieldName
     * @param editField
     * @param language
     * @param ebeneName
     * @param displayField
     */
    public static void setLabelTextForVirtField(String fieldName, EtkEditField editField, String language,
                                                String ebeneName, EtkDisplayField displayField) {
        if (VirtualFieldsUtils.isVirtualField(fieldName)) {
            EtkMultiSprache multi = new EtkMultiSprache();
            GuiLabel label = EtkPluginApi.getAssemblyListColumnHeaderLabel(ebeneName, displayField);
            if (label != null) {
                multi.setText(language, label.getText());
            } else {
                multi.setText(language, displayField.getText().getText(language));
            }
            editField.setText(multi);
            editField.setDefaultText(false);
        }
    }
}
