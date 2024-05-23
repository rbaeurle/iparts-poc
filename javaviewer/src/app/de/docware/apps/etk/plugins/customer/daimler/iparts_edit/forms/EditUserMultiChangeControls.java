/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.forms.common.EditControlFactory;
import de.docware.apps.etk.base.forms.common.EnumComboBox;
import de.docware.apps.etk.base.forms.common.EnumRComboBox;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMeta;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelPropertiesList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.iPartsDictConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDocuRelevantTruck;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.config.iPartsNutzDokProcessingState;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataKEMWorkBasketEDS;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataKEMWorkBasketMBS;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataWorkBasketSaaStates;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditUserControlsHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiCheckbox;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Dialog um in einer Tabelle mehrere Zellenwerte zu vereinheitlichen
 */
public class EditUserMultiChangeControls extends EditUserControls {

    /**
     * Hier können bestimmte Initialwerte für die editierbaren Felder gesetzt werden.
     *
     * @param externalEditFields
     * @return
     */
    public static List<FieldAndDefaultValue> getFieldsAndDefaultValuesForUnify(EtkEditFields externalEditFields) {
        List<FieldAndDefaultValue> result = new DwList<>();
        for (EtkEditField value : externalEditFields.getVisibleEditFields()) {
            result.add(new FieldAndDefaultValue(value.getKey().getFieldName(), "", value.isArray(), value.isMultiLanguage(), value.getKey().isVirtualKey()));
        }
        return result;
    }

    // Enum um zu Bestimmen woher der Aufruf zum Vereinheitlichen kommt
    public enum UnifySource {
        CONSTURCTION, AFTERSALES, OTHER
    }

    public static final String LABEL_TEXT_MULTI_EDIT = "!!Übernehmen:";

    public static DBDataObjectAttributes showEditUserMultiChangeControlsForSaaWorkBasket(AbstractJavaViewerFormIConnector dataConnector,
                                                                                         EtkEditFields externalEditFields,
                                                                                         List<iPartsDataWorkBasketSaaStates> wbsStateList) {
        return showEditUserMultiChangeControls(dataConnector, externalEditFields,
                                               getInitialAttributesForUnify(wbsStateList, getFieldsAndDefaultValuesForSaaWorkBasketUnify()),
                                               null, UnifySource.OTHER);
    }

    public static DBDataObjectAttributes showEditUserMultiChangeControlsForKemWorkBasket(AbstractJavaViewerFormIConnector dataConnector,
                                                                                         EtkEditFields externalEditFields,
                                                                                         List<iPartsDataKEMWorkBasketEDS> kemWorkBasketList) {
        return showEditUserMultiChangeControls(dataConnector, externalEditFields,
                                               getInitialAttributesForUnify(kemWorkBasketList, getFieldsAndDefaultValuesForKemWorkBasketUnify()),
                                               null, UnifySource.OTHER);
    }

    public static DBDataObjectAttributes showEditUserMultiChangeControlsForKemWorkBasketMBS(AbstractJavaViewerFormIConnector dataConnector,
                                                                                            EtkEditFields externalEditFields,
                                                                                            List<iPartsDataKEMWorkBasketMBS> kemWorkBasketList) {
        return showEditUserMultiChangeControls(dataConnector, externalEditFields,
                                               getInitialAttributesForUnify(kemWorkBasketList, getFieldsAndDefaultValuesForKemMbsWorkBasketUnify()),
                                               null, UnifySource.OTHER);
    }

    public static DBDataObjectAttributes showEditUserMultiChangeControlsForMissingSaa(AbstractJavaViewerFormIConnector dataConnector,
                                                                                      EtkEditFields externalEditFields,
                                                                                      List<EtkDataObject> dataNutzDokSAAList) {
        return showEditUserMultiChangeControls(dataConnector, externalEditFields,
                                               getInitialAttributesForUnify(dataNutzDokSAAList, getFieldsAndDefaultValuesForMissingSaa()),
                                               null, UnifySource.OTHER);
    }

    public static DBDataObjectAttributes showEditUserMultiChangeControlsForMissingKem(AbstractJavaViewerFormIConnector dataConnector,
                                                                                      EtkEditFields externalEditFields,
                                                                                      List<EtkDataObject> dataNutzDokKEMList) {
        return showEditUserMultiChangeControls(dataConnector, externalEditFields,
                                               getInitialAttributesForUnify(dataNutzDokKEMList, getFieldsAndDefaultValuesForMissingKem()),
                                               null, UnifySource.OTHER);
    }

    public static DBDataObjectAttributes showEditUserMultiChangeControlsForModels(AbstractJavaViewerFormIConnector dataConnector,
                                                                                  EtkEditFields externalEditFields,
                                                                                  List<iPartsDataModel> modelList) {
        return showEditUserMultiChangeControls(dataConnector, externalEditFields,
                                               getInitialAttributesForUnify(modelList, getFieldsAndDefaultValuesForModelUnify()),
                                               null, UnifySource.OTHER);
    }

    public static DBDataObjectAttributes showEditUserMultiChangeControlsForConstModels(AbstractJavaViewerFormIConnector dataConnector,
                                                                                       EtkEditFields externalEditFields,
                                                                                       iPartsDataModelPropertiesList dataModelPropertiesList) {
        return showEditUserMultiChangeControls(dataConnector, externalEditFields,
                                               getInitialAttributesForUnify(dataModelPropertiesList.getAsList(), getFieldsAndDefaultValuesForConstModelUnify()),
                                               null, UnifySource.OTHER);
    }

    public static DBDataObjectAttributes showEditUserMultiChangeControlsForProducts(AbstractJavaViewerFormIConnector dataConnector,
                                                                                    EtkEditFields externalEditFields,
                                                                                    List<iPartsDataProduct> productList) {
        return showEditUserMultiChangeControls(dataConnector, externalEditFields,
                                               getInitialAttributesForUnify(productList, getFieldsAndDefaultValuesForProductUnify()),
                                               null, UnifySource.OTHER);
    }

    public static DBDataObjectAttributes showEditUserMultiChangeControlsForDictMeta(AbstractJavaViewerFormIConnector dataConnector,
                                                                                    EtkEditFields externalEditFields,
                                                                                    List<iPartsDataDictMeta> dictMetaList) {
        return showEditUserMultiChangeControls(dataConnector, externalEditFields,
                                               getInitialAttributesForUnify(dictMetaList, getFieldsAndDefaultValuesForDictMetaUnify()),
                                               null, UnifySource.OTHER);
    }

    public static DBDataObjectAttributes showEditUserMultiChangeControls(AbstractJavaViewerFormIConnector dataConnector,
                                                                         EtkEditFields externalEditFields,
                                                                         DBDataObjectAttributes initialValues,
                                                                         EditUserMultiChangeControls externalMultiControl,
                                                                         UnifySource unifySource) {

        EditUserMultiChangeControls eCtrl;
        if (externalMultiControl == null) {
            eCtrl = new EditUserMultiChangeControls(dataConnector, dataConnector.getActiveForm(),
                                                    externalEditFields,
                                                    initialValues, unifySource);
        } else {
            eCtrl = externalMultiControl;
        }
        eCtrl.setTitle("!!Werte mehrfach ersetzen");
        ModalResult modal = eCtrl.showModal();
        if (modal == ModalResult.OK) {
            return eCtrl.getSelectedAttributes();
        }
        return null;
    }

    /**
     * Liefert die Attribute, die bereits feste Werte enthalten. Damit wird beim Bauen des EditControls der übergebene
     * Wert anstelle des Default-Wertes angezeigt.
     *
     * @param selectedDataObjects
     * @param defaultValues
     * @param overrideEmptyValue  Wenn "true", dann wird ein leere Wert nicht als eigenständiger Wert betrachtet
     * @return
     */
    public static DBDataObjectAttributes getInitialAttributesForUnify(List<? extends EtkDataObject> selectedDataObjects,
                                                                      List<FieldAndDefaultValue> defaultValues, boolean overrideEmptyValue) {
        if (selectedDataObjects == null) {
            return null;
        }
        Map<String, FieldAndDefaultValue> initialEditValues = new HashMap<>();
        setInitialEditValueForAttributes(selectedDataObjects, initialEditValues, defaultValues, overrideEmptyValue);
        DBDataObjectAttributes attributes = new DBDataObjectAttributes();
        // Baut aus den Feldern und ihren bestimmten Werten die DBDataObjectAttributes zusammen
        for (Map.Entry<String, FieldAndDefaultValue> entry : initialEditValues.entrySet()) {
            FieldAndDefaultValue fieldAndDefaultValue = entry.getValue();
            String fieldname = entry.getKey();
            boolean isVirtual = fieldAndDefaultValue.isVirtual();
            if (fieldAndDefaultValue.isMultiLang()) {
                EtkMultiSprache initialText = fieldAndDefaultValue.getMultiLangValue();
                DBDataObjectAttribute attribute = new DBDataObjectAttribute(fieldname, DBDataObjectAttribute.TYPE.MULTI_LANGUAGE, isVirtual, true);
                attribute.setValueAsMultiLanguage(initialText, DBActionOrigin.FROM_DB);
                attribute.setTextIdForMultiLanguage(initialText.getTextId(), initialText.getTextId(), DBActionOrigin.FROM_DB);
                attributes.addField(attribute, DBActionOrigin.FROM_DB);
            } else if (fieldAndDefaultValue.isArray()) {
                EtkDataArray initialArray = fieldAndDefaultValue.getArrayValue();
                DBDataObjectAttribute attribute = new DBDataObjectAttribute(fieldname, DBDataObjectAttribute.TYPE.ARRAY, isVirtual, true);
                attribute.setValueAsArray(initialArray, DBActionOrigin.FROM_DB);
                attribute.setIdForArray(initialArray.getArrayId(), DBActionOrigin.FROM_DB);
                attributes.addField(attribute, DBActionOrigin.FROM_DB);
            } else {
                DBDataObjectAttribute attribute = new DBDataObjectAttribute(fieldname, DBDataObjectAttribute.TYPE.STRING, isVirtual, true);
                attribute.setValueAsString(fieldAndDefaultValue.getStringValue(), DBActionOrigin.FROM_DB);
                attributes.addField(attribute, DBActionOrigin.FROM_DB);
            }
        }
        return attributes;
    }

    public static DBDataObjectAttributes getInitialAttributesForUnify(List<? extends EtkDataObject> selectedDataObjects,
                                                                      List<FieldAndDefaultValue> defaultValues) {
        return getInitialAttributesForUnify(selectedDataObjects, defaultValues, false);
    }

    /**
     * Befüllt die übergebene Map mit den Feldnamen und den Werten die im Dialog angezeigt werden sollen. Dabei werden
     * alle relevanten Edit-Felder der übergebenen {@link EtkDataObject}s überprüft. Enthalten alle Objekte den
     * gleichen Wert, dann wird dieser als Anzeigewert bestimmt. Falls unterschiedliche Werte gefunden wurden, wird der
     * für den Feldnamen übergeben Default-Wert genommen.
     *
     * @param selectedDataObjects
     * @param initialEditValues
     * @param fieldAndDefaultValues
     * @param overrideEmptyValue    Wenn "true", dann wird ein leere Wert nicht als eigenständiger Wert betrachtet
     */
    private static void setInitialEditValueForAttributes(List<? extends EtkDataObject> selectedDataObjects,
                                                         Map<String, FieldAndDefaultValue> initialEditValues,
                                                         List<FieldAndDefaultValue> fieldAndDefaultValues,
                                                         boolean overrideEmptyValue) {
        if (initialEditValues == null) {
            return;
        }
        // Durchlaufe alle Felder
        for (FieldAndDefaultValue fieldAndDefaultValue : fieldAndDefaultValues) {
            String resultString = "";
            EtkMultiSprache resultMultiLang = null;
            EtkDataArray resultDataArray = null;
            if (selectedDataObjects.size() == 1) {
                if (fieldAndDefaultValue.isArray()) {
                    resultDataArray = selectedDataObjects.get(0).getFieldValueAsArray(fieldAndDefaultValue.getFieldname());
                } else if (fieldAndDefaultValue.isMultiLang()) {
                    resultMultiLang = selectedDataObjects.get(0).getFieldValueAsMultiLanguage(fieldAndDefaultValue.getFieldname());
                } else {
                    resultString = selectedDataObjects.get(0).getFieldValue(fieldAndDefaultValue.getFieldname());
                }
            } else {
                for (EtkDataObject dataObject : selectedDataObjects) {
                    dataObject.setLogLoadFieldIfNeeded(false);
                    if (fieldAndDefaultValue.isArray()) {
                        EtkDataArray currentArray = dataObject.getFieldValueAsArray(fieldAndDefaultValue.getFieldname());
                        // Leerer Wert soll nicht als "echter" Wert betrachtet werden, daher bei leeren Arrays einfach weitermachen
                        if (overrideEmptyValue && ((currentArray == null) || currentArray.isEmpty())) {
                            continue;
                        }
                        if ((resultDataArray != null) && !resultDataArray.equalValues(currentArray)) {
                            resultDataArray = null;
                            break;
                        } else {
                            resultDataArray = currentArray;
                        }
                    } else if (fieldAndDefaultValue.isMultiLang()) {
                        EtkMultiSprache currentMulti = dataObject.getFieldValueAsMultiLanguage(fieldAndDefaultValue.getFieldname());
                        // Leerer Wert soll nicht als "echter" Wert betrachtet werden, daher bei leeren Texten einfach weitermachen
                        if (overrideEmptyValue && ((currentMulti == null) || currentMulti.isEmpty())) {
                            continue;
                        }
                        if ((resultMultiLang != null) && !resultMultiLang.equalContent(currentMulti)) {
                            resultMultiLang = null;
                            break;
                        } else {
                            resultMultiLang = currentMulti;
                        }
                    } else {
                        String currentValue = dataObject.getFieldValue(fieldAndDefaultValue.getFieldname());
                        // Leerer Wert soll nicht als "echter" Wert betrachtet werden, daher bei leeren Werten einfach weitermachen
                        if (overrideEmptyValue && StrUtils.isEmpty(currentValue)) {
                            continue;
                        }
                        if (StrUtils.isValid(resultString) && !resultString.equals(currentValue)) {
                            resultString = fieldAndDefaultValue.getStringValue();
                            break;
                        } else {
                            resultString = currentValue;
                        }
                    }
                    dataObject.setLogLoadFieldIfNeeded(true);
                }
            }

            // Hatten alle Objekte den gleichen Wert, dann wird er hier (abhängig vom Typ) gesetzt
            if (fieldAndDefaultValue.isArray()) {
                if (resultDataArray != null) {
                    fieldAndDefaultValue.setDefaultValue(resultDataArray);
                }

            } else if (fieldAndDefaultValue.isMultiLang()) {
                if (resultMultiLang != null) {
                    fieldAndDefaultValue.setDefaultValue(resultMultiLang);
                }
            } else {
                if (StrUtils.isValid(resultString)) {
                    fieldAndDefaultValue.setStringValue(resultString);
                }
            }
            initialEditValues.put(fieldAndDefaultValue.getFieldname(), fieldAndDefaultValue);
        }
    }

    /**
     * Sammelt die Produkt-Felder zusammen, die vordefinierte Default-Werte haben sollen
     *
     * @return
     */
    public static List<FieldAndDefaultValue> getFieldsAndDefaultValuesForProductUnify() {
        List<FieldAndDefaultValue> result = new DwList<>();
        result.add(new FieldAndDefaultValue(iPartsConst.FIELD_DP_PRODUCT_VISIBLE, SQLStringConvert.booleanToPPString(true)));
        result.add(new FieldAndDefaultValue(iPartsConst.FIELD_DP_EPC_RELEVANT, SQLStringConvert.booleanToPPString(true)));
        result.add(new FieldAndDefaultValue(iPartsConst.FIELD_DP_USE_SVGS, SQLStringConvert.booleanToPPString(false)));
        result.add(new FieldAndDefaultValue(iPartsConst.FIELD_DP_PREFER_SVG, SQLStringConvert.booleanToPPString(false)));
        result.add(new FieldAndDefaultValue(iPartsConst.FIELD_DP_FULL_LANGUAGE_SUPPORT, SQLStringConvert.booleanToPPString(false)));
        return result;
    }

    public static List<FieldAndDefaultValue> getFieldsAndDefaultValuesForDictMetaUnify() {
        List<FieldAndDefaultValue> result = new DwList<>();
        result.add(new FieldAndDefaultValue(iPartsConst.FIELD_DA_DICT_META_STATE, iPartsDictConst.DICT_STATUS_CONSOLIDATED));
        return result;
    }

    /**
     * Sammelt die Konstruktions-Model-Felder zusammen, die vordefinierte Default-Werte haben sollen
     *
     * @return
     */
    public static List<FieldAndDefaultValue> getFieldsAndDefaultValuesForConstModelUnify() {
        List<FieldAndDefaultValue> result = new DwList<>();
        return result;
    }

    /**
     * Sammelt die Model-Felder zusammen, die vordefinierte Default-Werte haben sollen
     *
     * @return
     */
    public static List<FieldAndDefaultValue> getFieldsAndDefaultValuesForModelUnify() {
        List<FieldAndDefaultValue> result = new DwList<FieldAndDefaultValue>();
        result.add(new FieldAndDefaultValue(iPartsConst.FIELD_DM_MODEL_VISIBLE, SQLStringConvert.booleanToPPString(true)));
        return result;
    }

    public static List<FieldAndDefaultValue> getFieldsAndDefaultValuesForKemWorkBasketUnify() {
        List<FieldAndDefaultValue> result = new DwList<FieldAndDefaultValue>();
        result.add(new FieldAndDefaultValue(iPartsConst.FIELD_DKWB_DOCU_RELEVANT, iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NOT_SPECIFIED.getDbValue()));
        return result;
    }

    public static List<FieldAndDefaultValue> getFieldsAndDefaultValuesForKemMbsWorkBasketUnify() {
        List<FieldAndDefaultValue> result = new DwList<FieldAndDefaultValue>();
        result.add(new FieldAndDefaultValue(iPartsConst.FIELD_DKWM_DOCU_RELEVANT, iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NOT_SPECIFIED.getDbValue()));
        return result;
    }

    public static List<FieldAndDefaultValue> getFieldsAndDefaultValuesForMissingSaa() {
        List<FieldAndDefaultValue> result = new DwList<FieldAndDefaultValue>();
        result.add(new FieldAndDefaultValue(iPartsConst.FIELD_DNS_PROCESSING_STATE, iPartsNutzDokProcessingState.NEW.getDBValue()));
        return result;
    }

    public static List<FieldAndDefaultValue> getFieldsAndDefaultValuesForMissingKem() {
        List<FieldAndDefaultValue> result = new DwList<FieldAndDefaultValue>();
        result.add(new FieldAndDefaultValue(iPartsConst.FIELD_DNK_PROCESSING_STATE, iPartsNutzDokProcessingState.NEW.getDBValue()));
        return result;
    }

    public static List<FieldAndDefaultValue> getFieldsAndDefaultValuesForSaaWorkBasketUnify() {
        List<FieldAndDefaultValue> result = new DwList<FieldAndDefaultValue>();
        result.add(new FieldAndDefaultValue(iPartsConst.FIELD_WBS_DOCU_RELEVANT, iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NOT_SPECIFIED.getDbValue()));
        return result;
    }

    protected List<GuiCheckbox> checkboxList;
    protected GuiLabel label;
    private boolean fieldActivatesCheckbox;
    private boolean allowEmptyEnumValues;

    public EditUserMultiChangeControls(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                       EtkEditFields externalEditFields, DBDataObjectAttributes initialAttributes,
                                       boolean startPostPostCreateGui, boolean fieldActivatesCheckbox, boolean allowEmptyEnumValues,
                                       UnifySource unifySource) {
        super(dataConnector, parentForm, null, null, initialAttributes,
              // Virtuelle Felder, die nicht editierbar sein sollen, hier entfernen
              iPartsEditUserControlsHelper.getEditFieldsWithoutNonEditableEditFields(externalEditFields, unifySource));
        this.fieldActivatesCheckbox = fieldActivatesCheckbox;
        this.allowEmptyEnumValues = allowEmptyEnumValues;
        if (startPostPostCreateGui) {
            postPostCreateGui();
        }
    }

    public EditUserMultiChangeControls(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                       EtkEditFields externalEditFields, DBDataObjectAttributes initialAttributes,
                                       UnifySource unifySource) {
        this(dataConnector, parentForm, externalEditFields, initialAttributes, true, false, false, unifySource);
    }

    /**
     * liefert nur die {@link DBDataObjectAttributes} die mit Übernahme-Haken versehen sind
     *
     * @return
     */
    public DBDataObjectAttributes getSelectedAttributes() {
        DBDataObjectAttributes result = new DBDataObjectAttributes();
        int index = 0;
        for (GuiCheckbox control : checkboxList) {
            EditControlFactory ctrl = editControls.getControlByFeldIndex(index).getEditControl();
            if (control.isSelected()) {
                result.addField(attributes.getField(ctrl.getFieldName(), false), DBActionOrigin.FROM_DB);
            }
            index++;
        }
        return result;
    }

    public Set<String> getSelectedCheckBoxValues() {
        Set<String> result = new HashSet<>();
        int index = 0;
        for (GuiCheckbox control : checkboxList) {
            if (control.isSelected()) {
                EditControlFactory ctrl = editControls.getControlByFeldIndex(index).getEditControl();
                result.add(ctrl.getTableFieldName());
            }
            index++;
        }
        return result;
    }

    public void setSelectedCheckBoxValues(List<String> tableAndFieldNames) {
        if ((tableAndFieldNames != null) && !tableAndFieldNames.isEmpty()) {
            int index = 0;
            for (GuiCheckbox control : checkboxList) {
                EditControlFactory ctrl = editControls.getControlByFeldIndex(index).getEditControl();
                if (tableAndFieldNames.contains(ctrl.getTableFieldName())) {
                    control.switchOffEventListeners();
                    control.setSelected(true);
                    control.switchOnEventListeners();
                }
                index++;
            }
            enableEditControls(null);
        }
    }

    /**
     * postCreateGui leer überschreiben, relevante Sachen werden in postPostCreateGui durchgeführt
     * Dieser Schritt ist nötig, damit mit addEditFieldChild Überschriften und Übernahme Checkboxen korrekt erzeugt werden
     */
    @Override
    protected void postCreateGui() {
    }

    /**
     * Ersatz für postCreateGui
     */
    protected void postPostCreateGui() {
        clearEditFieldsPanel();
        editFields = new EtkEditFields();
        if (externalEditFields != null) {
            editFields = externalEditFields;
            if (attributes == null) {
                attributes = new DBDataObjectAttributes();
            }
            for (EtkEditField editField : editFields.getFields()) {
                if (!attributes.containsKey(editField.getKey().getFieldName())) {
                    boolean isVirtual = editField.getKey().isVirtualKey();
                    String fieldname = editField.getKey().getFieldName();
                    if (editField.isMultiLanguage()) {
                        DBDataObjectAttribute attribute = new DBDataObjectAttribute(fieldname, DBDataObjectAttribute.TYPE.MULTI_LANGUAGE, isVirtual, true);
                        attributes.addField(attribute, DBActionOrigin.FROM_DB);
                    } else if (editField.isArray()) {
                        DBDataObjectAttribute attribute = new DBDataObjectAttribute(fieldname, DBDataObjectAttribute.TYPE.ARRAY, isVirtual, true);
                        attributes.addField(attribute, DBActionOrigin.FROM_DB);
                    } else {
                        DBDataObjectAttribute attribute = new DBDataObjectAttribute(fieldname, DBDataObjectAttribute.TYPE.STRING, isVirtual, true);
                        attribute.setValueAsString("", DBActionOrigin.FROM_DB);
                        attributes.addField(attribute, DBActionOrigin.FROM_DB);
                    }

                }
            }
            prepareControls(editFields);
        } else {
            GuiLabel noMaterialLabel = new GuiLabel(TranslationHandler.translate("!!Stücklistendaten nicht gefunden!"));
            noMaterialLabel.setConstraints(new ConstraintsGridBag(0, 0, 1, 1, 100, 0, ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_NONE, 8, 8, 8, 8));
            super.addEditFieldChild(noMaterialLabel);
            setReadOnly(true);
        }

        if ((checkboxList != null) && (checkboxList.size() == 1)) {
            checkboxList.get(0).setSelected(true);
        }
        enableEditControls(null);
        doEnableButtons(null);
    }

    @Override
    protected void modifyEditControl(EditControl ctrl, EtkEditField field, String initialValue, EtkDataArray initialDataArray) {
        if (!allowEmptyEnumValues) {
            // EnumComboBoxen ohne leeren Startwert
            if (ctrl.getEditControl().getControl() instanceof EnumComboBox) {
                EnumComboBox enumComboBox = (EnumComboBox)ctrl.getEditControl().getControl();
                if (enumComboBox.getIndexOfItem("") > -1) {
                    enumComboBox.removeItem("");
                }
            } else if (ctrl.getEditControl().getControl() instanceof EnumRComboBox) {
                // Kopierter Part, der wieder entfernt werden kann, wenn GuiCombobox und RCombobox zusammengeführt wurden
                EnumRComboBox enumComboBox = (EnumRComboBox)ctrl.getEditControl().getControl();
                if (enumComboBox.getIndexOfItem("") > -1) {
                    enumComboBox.removeItem("");
                }
            }
        }

        if (field.getKey().getName().equals(TableAndFieldName.make(iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_POS))) {
            // numerischer Editor für HotSpot
            ctrl.getEditControl().setControl(new iPartsGuiHotSpotTextField(initialValue, true));
        }

    }

    /**
     * {@link ConstraintsGridBag} für Übernahme-Checkboxen erzeugen
     *
     * @param gridy
     * @return
     */
    protected ConstraintsGridBag createCheckBoxConstraints(int gridy) {
        return new ConstraintsGridBag(0, gridy, 1, 1, 0.0, 0.0, ConstraintsGridBag.ANCHOR_NORTH, ConstraintsGridBag.FILL_NONE, 8, 4, 4, 4);
    }

    /**
     * {@link ConstraintsGridBag} für EditControl-Labels um 1 nach rechts und unten schieben
     *
     * @param label
     * @param gridy
     * @return
     */
    @Override
    protected ConstraintsGridBag createLabelConstraints(GuiLabel label, int gridy) {
        ConstraintsGridBag constraintsGridBag = super.createLabelConstraints(label, gridy + 1);
        constraintsGridBag.setGridx(constraintsGridBag.getGridx() + 1);
        return constraintsGridBag;
    }

    /**
     * {@link ConstraintsGridBag} für EditControl-Controls um 1 nach rechts und unten schieben
     *
     * @param gridy
     * @return
     */
    @Override
    protected ConstraintsGridBag createValueConstraints(int gridy) {
        ConstraintsGridBag constraintsGridBag = super.createValueConstraints(gridy + 1);
        constraintsGridBag.setGridx(constraintsGridBag.getGridx() + 1);
        return constraintsGridBag;
    }

    /**
     * Überlagerung der addEditControlChild Methode, damit die Übernahme-Checkboxen und die Überschrift erzeugt werden kann
     *
     * @param ctrl
     */
    @Override
    protected void addEditControlChild(EditControl ctrl) {
        // erster Aufruf?
        if (checkboxList == null) {
            checkboxList = new DwList<>();

            // Überschrift erzeugen
            label = createLabel("label", getLabelText());
            label.setConstraints(createHeadingConstraints());
            super.addEditFieldChild(label);
        }

        // Checkbox und Label
        // Übernahme-Checkbox anlegen
        final GuiCheckbox checkBox = createCheckBox();
        ConstraintsGridBag checkboxConstraints = (ConstraintsGridBag)ctrl.getLabel().getConstraints();
        checkBox.setConstraints(createCheckBoxConstraints(checkboxConstraints.getGridy()));
        checkboxList.add(checkBox);
        AbstractGuiControl control = ctrl.getEditControl().getControl();
        if (fieldActivatesCheckbox) {
            control.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
                @Override
                public void fire(Event event) {
                    checkBox.setSelected(true);
                }
            });
        }
        super.addEditFieldChild(checkBox);
        super.addEditFieldChild(ctrl.getLabel());
        super.addEditFieldChild(control);
    }

    protected ConstraintsGridBag createHeadingConstraints() {
        return createCheckBoxConstraints(0);
    }

    protected String getLabelText() {
        return LABEL_TEXT_MULTI_EDIT;
    }

    protected GuiLabel createLabel(String name, String text) {
        GuiLabel label = new GuiLabel();
        label.setName(name);
        label.__internal_setGenerationDpi(96);
        label.registerTranslationHandler(getUITranslationHandler());
        label.setScaleForResolution(true);
        label.setMinimumWidth(10);
        label.setMinimumHeight(10);
        label.setText(text);
        return label;
    }

    protected GuiCheckbox createCheckBox() {
        GuiCheckbox checkBox = new GuiCheckbox();
        checkBox.setName("checkbox_" + checkboxList.size());
        checkBox.__internal_setGenerationDpi(96);
        checkBox.registerTranslationHandler(getUITranslationHandler());
        checkBox.setScaleForResolution(true);
        checkBox.setMinimumWidth(10);
        checkBox.setMinimumHeight(10);
        checkBox.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                enableEditControls(event);
            }
        });
        return checkBox;
    }

    /**
     * Je nach Zustand der Übernahme-Checkboxen die EditControls auf ReadOnly/ReadWrite setzen
     *
     * @param event
     */
    protected void enableEditControls(Event event) {
        int index = 0;
        for (GuiCheckbox control : checkboxList) {
            EditControlFactory ctrl = editControls.getControlByFeldIndex(index).getEditControl();
            if (control.isSelected() || fieldActivatesCheckbox) {
                ctrl.setReadOnly(false);
            } else {
                ctrl.setReadOnly(true);
            }
            index++;
        }
        doEnableButtons(event);
    }

    protected boolean isOneCheckBoxChecked() {
        boolean isChecked = false;
        for (GuiCheckbox control : checkboxList) {
            if (control.isEnabled() && control.isSelected()) {
                isChecked = true;
                break;
            }
        }
        return isChecked;
    }

    protected GuiCheckbox getCheckBoxForControl(EditControlFactory controlFactory) {
        if ((controlFactory == null) || (checkboxList == null) || checkboxList.isEmpty()) {
            return null;
        }
        for (int index = 0; index < editControls.size(); index++) {
            EditControlFactory ctrl = editControls.getControlByFeldIndex(index).getEditControl();
            if (ctrl.getTableFieldName().equals(controlFactory.getTableFieldName())) {
                if (checkboxList.size() > index) {
                    return checkboxList.get(index);
                }
            }
        }
        return null;
    }

    /**
     * OK-Button hängt nur noch von ReadOnly und mindestens einer getzten Übernahme-Checkbox ab
     *
     * @param event
     */
    @Override
    protected void doEnableButtons(Event event) {
        doEnableCheckbox();
        boolean isChecked = isOneCheckBoxChecked();
        enableOKButton(readOnly || isChecked);
    }

    /**
     * Enabled oder Diasabled einzelnen Checkboxen
     */
    private void doEnableCheckbox() {
        if ((checkboxList != null) && !checkboxList.isEmpty()) {
            for (int index = 0; index < checkboxList.size(); index++) {
                EditControlFactory ctrl = editControls.getControlByFeldIndex(index).getEditControl();
                EtkEditField field = editFields.getFeldByName(ctrl.getTableName(), ctrl.getFieldName());
                GuiCheckbox checkBox = checkboxList.get(index);
                boolean isCheckBoxEnabled;
                // EditFields, die via Workbench auf "nicht editierbar" gesetzt wurden, dürfen nicht freigeschaltet werden
                if ((field != null) && !field.isEditierbar()) {
                    isCheckBoxEnabled = false;
                } else {
                    isCheckBoxEnabled = checkBoxEnabled(ctrl, checkBox);
                }
                ctrl.getControl().setEnabled(isCheckBoxEnabled);
                checkBox.setEnabled(isCheckBoxEnabled);
            }
        }
    }

    /**
     * Liefert zurück, ob die Checkbox enabled ist
     *
     * @param ctrl
     * @param checkbox
     * @return
     */
    protected boolean checkBoxEnabled(EditControlFactory ctrl, GuiCheckbox checkbox) {
        return true;
    }

    @Override
    protected boolean isModified() {
        boolean isModified = super.isModified();
        if (!isModified) {
            isModified = isOneCheckBoxChecked();
        }
        return isModified;
    }

    @Override
    protected int getRequiredAdditionalWidth() {
        return super.getRequiredAdditionalWidth() + 60; // 60px zusätzlich wegen den Checkboxen
    }

    @Override
    protected int getRequiredAdditionalHeight() {
        return super.getRequiredAdditionalHeight() + 32; // 32px zusätzlich wegen der Überschrift
    }

    @Override
    protected int getScreenBorderWidth() {
        return 20; // 20px für möglichst kleinen Rand, weil der Dialog sehr breit werden kann und Calendar hier nicht wirklich benötigt werden
    }

    public boolean isFieldActivatesCheckbox() {
        return fieldActivatesCheckbox;
    }

    protected static class FieldAndDefaultValue {

        private String fieldname;
        private String stringValue;
        private boolean isArray;
        private EtkDataArray arrayValue;
        private boolean isMultiLang;
        private EtkMultiSprache multiLangValue;
        private boolean isVirtual;

        public FieldAndDefaultValue(String fieldname, String defaultValue, boolean isArray, boolean isMultiLang, boolean virtualKey) {
            this(fieldname, defaultValue);
            this.isArray = isArray;
            this.isMultiLang = isMultiLang;
            this.isVirtual = virtualKey;
        }

        public FieldAndDefaultValue(String fieldname, String defaultValue) {
            this.fieldname = fieldname;
            this.stringValue = defaultValue;
        }

        public String getFieldname() {
            return fieldname;
        }

        public String getStringValue() {
            return stringValue;
        }

        public boolean isArray() {
            return isArray;
        }

        public boolean isMultiLang() {
            return isMultiLang;
        }

        public void setStringValue(String resultString) {
            this.stringValue = resultString;
        }

        public void setDefaultValue(EtkDataArray arrayValue) {
            if ((arrayValue != null) && arrayValue.isEmpty()) {
                this.arrayValue = null;
            }
            this.arrayValue = arrayValue;
        }

        public void setDefaultValue(EtkMultiSprache multiLangValue) {
            if ((multiLangValue != null) && multiLangValue.isEmpty()) {
                this.multiLangValue = null;
            }
            this.multiLangValue = multiLangValue;
        }

        public boolean isVirtual() {
            return isVirtual;
        }

        public EtkDataArray getArrayValue() {
            if (arrayValue == null) {
                return new EtkDataArray();
            }
            return arrayValue;
        }

        public EtkMultiSprache getMultiLangValue() {
            if (multiLangValue == null) {
                return new EtkMultiSprache();
            }
            return multiLangValue;
        }


    }
}