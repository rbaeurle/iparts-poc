/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.SetOfEnumDataType;
import de.docware.apps.etk.base.db.EtkDbs;
import de.docware.apps.etk.base.forms.common.*;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.framework.modules.config.db.EtkFieldType;
import de.docware.framework.modules.db.EtkFieldInfo;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.util.sql.TableAndFieldName;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Hilfsklasse zum Befüllen der Spaltenfilter- und Edit-Comboboxen für die AusführungsArt (FIELD_K_AA)
 * In der Retailsicht mit Produktbezug werden die AA-Werte auf die gültigen AAs aus der Produkt-Baumuster-Zuordnung eingeschränkt
 */
public class iPartsAAPartsHelper {

    /**
     * In der Retailsicht mit Produktbezug werden die AA-Werte auf die gültigen AAs aus der Produkt-Baumuster-Zuordnung eingeschränkt
     * dies gilt für die Spaltenfilter als auch für Edit-Controls
     *
     * @param editControl
     * @param project
     * @param assembly
     * @return
     */
    public static boolean setAAEnumValuesByProduct(EditControlFactory editControl, EtkProject project,
                                                   iPartsDataAssembly assembly) {
        String fieldName = editControl.getField().getName();
        if (fieldName.equals(iPartsConst.FIELD_K_AA)) {
            // AS-Stückliste: hole AA's aus Product-Baumuster
            iPartsProductId productIdFromModuleUsage = assembly.getProductIdFromModuleUsage();
            if (productIdFromModuleUsage != null) {
                iPartsProduct product = iPartsProduct.getInstance(project, productIdFromModuleUsage);
                Set<String> aaValuesFromProductModels = product.getAAsFromModels(project);
                Set<String> valueSet = new TreeSet<>();
                if (aaValuesFromProductModels != null) {
                    EtkFieldType fieldType = editControl.getField().getType();
                    for (String aaFromProductModels : aaValuesFromProductModels) {
                        valueSet.add((fieldType == EtkFieldType.feSetOfEnum) ? SetOfEnumDataType.getSetOfEnumTag(aaFromProductModels) : aaFromProductModels);
                    }
                }
                if (!valueSet.isEmpty()) {
                    correctEnumValues(editControl, project, valueSet);
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean setAAEnumValuesByX4E(EditControlFactory editControl, EtkProject project, iPartsDataAssembly assembly) {
        String fieldName = editControl.getField().getName();
        if (fieldName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA) || fieldName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA_SOE)) {
            // DIALOG Konstruktion: hole AA's aus X4E
            HmMSmId hmMSmId = null;
            List<iPartsVirtualNode> virtualNodes = assembly.getVirtualNodesPath();
            if ((virtualNodes != null) && iPartsVirtualNode.isHmMSmNode(virtualNodes)) {
                hmMSmId = (HmMSmId)virtualNodes.get(1).getId();
            }
            if (hmMSmId != null) {
                if (doSetAAEnumValuesByX4E(editControl, project, hmMSmId.getSeries())) {
                    return true;
                }

            }
        }
        return false;
    }

    public static boolean setAAEnumValuesByX4E(EditControlFactory editControl, EtkProject project, String seriesNumber) {
        String fieldName = editControl.getField().getName();
        if (fieldName.equals(iPartsConst.FIELD_DBC_AA) || fieldName.equals(iPartsConst.FIELD_DSP_AA)) {
            // BadCode Form oder SOP Form: hole AA's aus X4E
            return doSetAAEnumValuesByX4E(editControl, project, seriesNumber);
        }
        return false;
    }

    protected static boolean doSetAAEnumValuesByX4E(EditControlFactory editControl, EtkProject project, String seriesNo) {
        // Liste der gültigen AA's aus X4E (DA_SERIES_CODES) holen
        Set<String> valueSet = getAllAAforSeriesByX4E(project, seriesNo, editControl.getField().getType());
        if (!valueSet.isEmpty()) {
            correctEnumValues(editControl, project, valueSet);
            return true;
        }
        return false;
    }

    protected static Set<String> getAllAAforSeriesByX4E(EtkProject project, String seriesNo, EtkFieldType fieldType) {
        Set<String> valueSet = new TreeSet<>();
        // Liste der gültigen AA's aus X4E (DA_SERIES_CODES) holen
        Collection<String> allAAsForSeries = iPartsDialogSeries.getInstance(project, new iPartsSeriesId(seriesNo)).getValidAAForSeries(project);
        if (!allAAsForSeries.isEmpty()) {
            for (String value : allAAsForSeries) {
                valueSet.add((fieldType == EtkFieldType.feSetOfEnum) ? SetOfEnumDataType.getSetOfEnumTag(value) : value);
            }
        }
        return valueSet;
    }

    private static void correctEnumValues(EditControlFactory editControl, EtkProject project, Set<String> valueSet) {
        if (!valueSet.isEmpty()) {
            correctEnumTexts(project, editControl.getControl(), editControl.getTableName(), editControl.getField().getName(),
                             valueSet, editControl.getInitialValue());
        }
    }

    private static void correctEnumTexts(EtkProject project, AbstractGuiControl control, String tableName, String fieldName,
                                         Set<String> valueSet, String initialValue) {
        EtkDbs etkDbs = project.getEtkDbs();
        String dbLanguage = project.getDBLanguage();
        if ((control instanceof EnumCheckComboBox) || (control instanceof EnumComboBox)) {
            if (control instanceof EnumCheckComboBox) {
                EnumCheckComboBox comboBox = (EnumCheckComboBox)control;
                valueSet.addAll(comboBox.getTokenList(""));
            }
            EnumComboBox comboBox = (EnumComboBox)control;
            comboBox.removeAllItems();
            if (!valueSet.isEmpty()) {
                if (!comboBox.isIgnoreBlankTexts()) {
                    comboBox.addItem("", "");
                    comboBox.getTokens().add("");
                }
                String enumKey = etkDbs.getEnum(TableAndFieldName.make(tableName, fieldName));
                for (String token : valueSet) {
                    // Versuchen, eine Benennung für das Token zu ermitteln mit Fallback auf den Token selbst
                    String tokenValue = SetOfEnumDataType.getSetOfEnumToken(token);
                    String enumText = etkDbs.getEnums().getEnumText(enumKey, tokenValue, dbLanguage,
                                                                    tokenValue, project, true);
                    comboBox.addItem(token, enumText);
                    comboBox.getTokens().add(token);
                }
            }
            if (initialValue != null) {
                comboBox.setActToken(initialValue);
            }
        } else if ((control instanceof EnumCheckRComboBox) || (control instanceof EnumRComboBox)) {
            if (control instanceof EnumCheckRComboBox) {
                EnumCheckRComboBox comboBox = (EnumCheckRComboBox)control;
                valueSet.addAll(comboBox.getTokenList(""));
            }
            EnumRComboBox comboBox = (EnumRComboBox)control;
            comboBox.removeAllItems();
            if (!valueSet.isEmpty()) {
                if (!comboBox.isIgnoreBlankTexts()) {
                    comboBox.addItem("", "");
                    comboBox.getTokens().add("");
                }
                String enumKey = etkDbs.getEnum(TableAndFieldName.make(tableName, fieldName));
                for (String token : valueSet) {
                    // Versuchen, eine Benennung für das Token zu ermitteln mit Fallback auf den Token selbst
                    String tokenValue = SetOfEnumDataType.getSetOfEnumToken(token);
                    String enumText = etkDbs.getEnums().getEnumText(enumKey, tokenValue, dbLanguage,
                                                                    tokenValue, project, true);
                    comboBox.addItem(token, enumText);
                    comboBox.getTokens().add(token);
                }
            }
            if (initialValue != null) {
                comboBox.setActToken(initialValue);
            }
        }
    }

    public static void setEnumTexteByX4E(EtkProject project, EnumRComboBox rComboBox, String tableName, String fieldName,
                                         String seriesNumber, String dbLanguage, boolean enableSort) {
        // aus X4E initial befüllen
        EtkFieldType fieldType = EtkFieldType.feEnum;
        EtkFieldInfo fieldInfo = project.getEtkDbs().getFieldInfo(tableName, fieldName);
        if (fieldInfo != null) {
            fieldType = fieldInfo.getFieldType();
        }
        // Liste der gültigen AA's aus X4E (DA_SERIES_CODES) holen
        Set<String> valueSet = getAllAAforSeriesByX4E(project, seriesNumber, fieldType);
        if (!valueSet.isEmpty()) {
            correctEnumTexts(project, rComboBox, tableName, fieldName, valueSet, null);
        } else {
            // damit ggf richtig sortiert wird
            rComboBox.removeAllItems();
            rComboBox.setEnumTexte(project, tableName, fieldName, dbLanguage, enableSort);
        }
    }


}
