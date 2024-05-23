/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProductModels;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProductModelsList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsProductModels;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;

/**
 * Helfer zum Synchronisieren der DA_PRODUCT_MODELS und DA_MODEL Tabelle
 */
public class iPartsProductModelHelper implements iPartsConst {

    public static void syncProductModelsWithModel(iPartsDataModel dataModel, iPartsDataProductModelsList productModelsList) {
        if (productModelsList.isEmpty()) {
            return;
        } else if (productModelsList.size() == 1) {
            // Zum aktuellen Baumuster gibt es nur eine Produkt-zu-Baumuster-Beziehung
            iPartsDataProductModels productModel = productModelsList.get(0);
            if (!dataModel.isEdited()) {
                // Baumuster wurde vom Autor noch nicht editiert -> Werte von aus der Produkt-zu-Baumuster-Beziehung
                // im Baumusterstamm ablegen
                moveProductModelDataFromProductModelToModel(productModel, dataModel);
            }
            // Weil die Daten nun im Baumusterstamm liegen (DA_MODEL) oder von Autor manuell editiert wurden -> Werte
            // in der Produkt-zu-Baumuster-Beziehung entfernen
            clearProductModelValuesInDataObject(productModel, FIELD_DPM_TEXTNR, FIELD_DPM_VALID_FROM, FIELD_DPM_VALID_TO);
        } else {
            // Wir haben zu einem Baumuster mehr als eine Produkt-zu-Baumuster-Beziehung
            if (dataModel.isEdited()) {
                // Wurden die Daten vom Autor editiert, dann sind die Daten aus der Migration nicht gültig -> alle
                // Produkt-zu-Baumuster-Beziehungen werden geleert
                clearAllProductModelsInList(productModelsList);
            } else {
                // Die Baumusterstammdaten wurden nicht editiert
                iPartsDataProductModelsList listWithEmptyDataSets = new iPartsDataProductModelsList();
                EtkMultiSprache addTextToCompare = null;
                String validFromValueToCompare = null;
                String validToValueToCompare = null;
                boolean differentProductModelValues = false;
                // Die gefundenen Produkt-zu-Baumuster-Beziehungen müssen nun bezüglich ihrer Daten verglichen werden
                for (iPartsDataProductModels productModel : productModelsList) {
                    String currentValidFrom = productModel.getFieldValue(FIELD_DPM_VALID_FROM);
                    String currentValidTo = productModel.getFieldValue(FIELD_DPM_VALID_TO);
                    EtkMultiSprache currentAddText = productModel.getFieldValueAsMultiLanguage(FIELD_DPM_TEXTNR);
                    // alle "leeren" Produkt-zu-Baumuster-Beziehungen werden aufegsammelt
                    if (isProductModelValuesEmpty(currentAddText, currentValidFrom, currentValidTo)) {
                        listWithEmptyDataSets.add(productModel, DBActionOrigin.FROM_EDIT);
                    }
                    if (addTextToCompare == null) {
                        addTextToCompare = currentAddText;
                    }

                    if (validFromValueToCompare == null) {
                        validFromValueToCompare = currentValidFrom;
                    }

                    if (validToValueToCompare == null) {
                        validToValueToCompare = currentValidTo;
                    }
                    // Vergleichen der einzelnen Werte der jeweiligen Datensätze
                    differentProductModelValues |= !compareProductModelValue(addTextToCompare, validFromValueToCompare,
                                                                             validToValueToCompare, currentAddText,
                                                                             currentValidFrom, currentValidTo);
                }

                if (differentProductModelValues) {
                    // Die gefundenen Produkt-zu-Baumuster-Beziehungen sind bezüglich ihrer Werte unterschiedlich
                    if (!listWithEmptyDataSets.isEmpty()) {
                        // Wenn es leere Produkt-zu-Baumuster-Beziehungen gibt, werden diese mit den Werten aus dem Baumusterstamm befüllt
                        for (iPartsDataProductModels emptyProductModel : listWithEmptyDataSets) {
                            moveProductModelDataFromModelToProductModel(emptyProductModel, dataModel);
                        }
                        // Jetzt hat jede Produkt-zu-Baumuster-Beziehung eigene Werte (wir haben also alle Fälle abgedeckt)
                        // -> Der Baumusterstamm kann geleert werden
                        clearModel(dataModel);
                    }
                } else {
                    // Die gefundenen Produkt-zu-Baumuster-Beziehungen enthalten die gleichen Werte
                    if (listWithEmptyDataSets.isEmpty()) {
                        // Alle Datensätze haben die gleichen nicht-leeren Werte -> Befülle den Baumusterstamm mit
                        // den Werten und leere die entsprechenden Felder in den Produkt-zu-Baumuster-Beziehungen
                        moveProductModelDataFromProductModelToModel(productModelsList.get(0), dataModel);
                        clearAllProductModelsInList(productModelsList);
                    }
                }
            }
        }
    }

    /**
     * Leert die Felder aller {@link iPartsDataProductModels} Objekte in der übergebenen Liste, die sich mit den Feldern
     * aus DA_MODEL überlappen
     *
     * @param list
     */
    private static void clearAllProductModelsInList(iPartsDataProductModelsList list) {
        if (list == null) {
            return;
        }
        for (iPartsDataProductModels productModel : list) {
            clearProductModel(productModel);
        }
    }

    /**
     * Leert die Felder des übergebenen Baumuster Objekts, die sich mit den Feldern aus DA_PRODUCT_MODEL überlappen
     *
     * @param dataModel
     */
    private static void clearModel(iPartsDataModel dataModel) {
        if (dataModel == null) {
            return;
        }
        clearProductModelValuesInDataObject(dataModel, FIELD_DM_ADD_TEXT, FIELD_DM_VALID_FROM, FIELD_DM_VALID_TO);
    }

    /**
     * Leert die Felder der übergebenen Produkt-zu-Baumuster-Beziehung, die sich mit den Feldern aus DA_MODEL überlappen
     *
     * @param productModel
     */
    private static void clearProductModel(iPartsDataProductModels productModel) {
        if (productModel == null) {
            return;
        }
        clearProductModelValuesInDataObject(productModel, FIELD_DPM_TEXTNR, FIELD_DPM_VALID_FROM, FIELD_DPM_VALID_TO);
    }

    /**
     * Leert die Felder "Zusatztext", "Gültig ab" und "Gültig bis"
     *
     * @param dataObject
     * @param addTextFieldname
     * @param validFromFieldname
     * @param validToFieldname
     */
    private static void clearProductModelValuesInDataObject(EtkDataObject dataObject, String addTextFieldname, String validFromFieldname, String validToFieldname) {
        dataObject.getFieldValueAsMultiLanguage(addTextFieldname);
        dataObject.setFieldValueAsMultiLanguage(addTextFieldname, new EtkMultiSprache(), DBActionOrigin.FROM_EDIT);
        dataObject.setFieldValue(validFromFieldname, "", DBActionOrigin.FROM_EDIT);
        dataObject.setFieldValue(validToFieldname, "", DBActionOrigin.FROM_EDIT);
    }

    /**
     * Kopiert die Werte für "Zusatztext", "Gültig ab" und "Gültig bis" von der Produkt-zu-Baumuster-Beziehung in den Baumusterstamm
     *
     * @param productModel
     * @param dataModel
     */
    private static void moveProductModelDataFromProductModelToModel(iPartsDataProductModels productModel, iPartsDataModel dataModel) {
        moveProductModelData(productModel, FIELD_DPM_TEXTNR, FIELD_DPM_VALID_FROM, FIELD_DPM_VALID_TO, dataModel, FIELD_DM_ADD_TEXT, FIELD_DM_VALID_FROM, FIELD_DM_VALID_TO);
    }

    /**
     * Kopiert die Werte für "Zusatztext", "Gültig ab" und "Gültig bis" vom Baumusterstam in die Produkt-zu-Baumuster-Beziehung
     *
     * @param productModel
     * @param dataModel
     */
    private static void moveProductModelDataFromModelToProductModel(iPartsDataProductModels productModel, iPartsDataModel dataModel) {
        moveProductModelData(dataModel, FIELD_DM_ADD_TEXT, FIELD_DM_VALID_FROM, FIELD_DM_VALID_TO, productModel, FIELD_DPM_TEXTNR, FIELD_DPM_VALID_FROM, FIELD_DPM_VALID_TO);
    }

    /**
     * Vergleicht die übergebenen Werte
     *
     * @param addTextToCompare
     * @param validFromValueToCompare
     * @param validToValueToCompare
     * @param currentAddText
     * @param currentValidFrom
     * @param currentValidTo
     * @return
     */
    private static boolean compareProductModelValue(EtkMultiSprache addTextToCompare, String validFromValueToCompare,
                                                    String validToValueToCompare, EtkMultiSprache currentAddText,
                                                    String currentValidFrom, String currentValidTo) {
        return StrUtils.stringEquals(validFromValueToCompare, currentValidFrom) &&
               StrUtils.stringEquals(validToValueToCompare, currentValidTo) &&
               addTextToCompare.equalContent(currentAddText);
    }

    /**
     * Liefert zurück, ob die übergebenen Werte leer sind
     *
     * @param addText
     * @param validFrom
     * @param validTo
     * @return
     */
    private static boolean isProductModelValuesEmpty(EtkMultiSprache addText, String validFrom, String validTo) {
        return addText.isEmpty() && StrUtils.isEmpty(validFrom, validTo);
    }

    private static void moveProductModelData(EtkDataObject fromDataObject, String fromObjectAddTextFieldname, String fromObjectValidFromFieldname,
                                             String fromObjectValidToFieldname, EtkDataObject toDataObject, String toObjectAddTextFieldname,
                                             String toObjectValidFromFieldname, String toObjectValidToFieldname) {
        toDataObject.setFieldValueAsMultiLanguage(toObjectAddTextFieldname, fromDataObject.getFieldValueAsMultiLanguage(fromObjectAddTextFieldname), DBActionOrigin.FROM_EDIT);

        String validFrom = fromDataObject.getFieldValue(fromObjectValidFromFieldname);
        if (!validFrom.isEmpty()) {
            validFrom = StrUtils.padStringWithCharsUpToLength(validFrom, '0', 14);
        }
        toDataObject.setFieldValue(toObjectValidFromFieldname, validFrom, DBActionOrigin.FROM_EDIT);

        String validTo = fromDataObject.getFieldValue(fromObjectValidToFieldname);
        if (!validTo.isEmpty()) {
            validTo = StrUtils.padStringWithCharsUpToLength(validTo, '0', 14);
        }
        toDataObject.setFieldValue(toObjectValidToFieldname, validTo, DBActionOrigin.FROM_EDIT);
    }

    /**
     * Liefert den "Gültig ab" Wert. Zuerst wird in dem übergebenen {@link iPartsDataProductModels} Objekt nachgeschaut,
     * ob ein passender Wert existiert. Ist das nicht der Fall, gibt es einen Fallback auf die Baumusterstammdaten.
     *
     * @param project
     * @param dataProductModel
     * @param modelIdExtern    Baumuster-Id als Fallback
     * @return
     */
    public static String getValidFromValue(EtkProject project, iPartsDataProductModels dataProductModel, iPartsModelId modelIdExtern) {
        return getValidFromOrToValue(project, dataProductModel, modelIdExtern, FIELD_DPM_VALID_FROM, true);
    }

    /**
     * Liefert den "Gültig bis" Wert. Zuerst wird in dem übergebenen {@link iPartsDataProductModels} Objekt nachgeschaut,
     * ob ein passender Wert existiert. Ist das nicht der Fall, gibt es einen Fallback auf die Baumusterstammdaten.
     *
     * @param project
     * @param dataProductModel
     * @return
     */
    public static String getValidToValue(EtkProject project, iPartsDataProductModels dataProductModel, iPartsModelId modelIdExtern) {
        return getValidFromOrToValue(project, dataProductModel, modelIdExtern, FIELD_DPM_VALID_TO, false);
    }

    /**
     * Liefert den "Gültig ab" oder "Gültig bis" Wert anhängig vom übergebenen Feldnamen und der Option <code>retrieveFromValue</code>.
     * Zuerst wird in dem übergebenen {@link iPartsDataProductModels} Objekt nachgeschaut, ob ein passender Wert existiert.
     * Ist das nicht der Fall, gibt es einen Fallback auf die Baumusterstammdaten mit der Baumusternummer aus dem übergebenen
     * {@link iPartsDataProductModels} Objekt.
     *
     * Sollte das übergebene {@link iPartsDataProductModels} Objekt nicht existieren, kann über <code>modelIdExtern</code>
     * das Baumuster explizit übergeben werden (z.B. wenn keine Produkt-zu-Baumuster - Beziehungen existieren, das Produkt
     * aber über die Baureihe Baumuster besitzt).
     *
     * @param project
     * @param dataProductModel
     * @return
     */
    private static String getValidFromOrToValue(EtkProject project, iPartsDataProductModels dataProductModel, iPartsModelId modelIdExtern,
                                                String fieldname, boolean retrieveFromValue) {
        String validDateValueFromProductModel = (dataProductModel != null) ? dataProductModel.getFieldValue(fieldname) : "";
        if (StrUtils.isValid(validDateValueFromProductModel)) {
            return StrUtils.padStringWithCharsUpToLength(validDateValueFromProductModel, '0', 14); // Zielformat ist yyyyMMddHHmmss
        }

        // Wenn die Produkt-zu-Baumuster Beziehung keine Ergebnisse liefert, dann via Baumusterstammdaten an die
        // Datumsangaben kommen (entweder "null" oder kein echtes Datum)
        iPartsModelId modelId = null;

        // Versuche an das Baumuster zu kommen aus der Produkt-zu-Baumuster Beziehung, sofern es nicht "null" ist
        if (dataProductModel != null) {
            modelId = new iPartsModelId(dataProductModel.getAsId().getModelNumber());
        }

        // Existiert die Produkt-zu-Baumuster Beziehung nicht, dann nimm das zusätzlich übergebene Baumuster
        if ((modelId == null) || !modelId.isModelNumberValid(true)) {
            modelId = modelIdExtern;
        }

        // Keine Möglichkeit an das Baumuster zu kommen -> raus
        if ((modelId == null) || !modelId.isModelNumberValid(true)) {
            return "";
        }

        if (retrieveFromValue) {
            return iPartsModel.getInstance(project, modelId).getValidFrom();
        } else {
            return iPartsModel.getInstance(project, modelId).getValidTo();
        }
    }

    /**
     * Liefert den "Zusatztext" Wert. Zuerst wird in dem übergebenen {@link iPartsDataProductModels} Objekt nachgeschaut,
     * ob ein passender Wert existiert. Ist das nicht der Fall, gibt es einen Fallback auf die Baumusterstammdaten.
     *
     * @param project
     * @param dataProductModel
     * @return
     */
    public static EtkMultiSprache getModelAddText(EtkProject project, iPartsDataProductModels dataProductModel) {
        return getModelAddText(project, dataProductModel.getAsId().getModelNumber(), dataProductModel.getAsId().getProductNumber());
    }


    public static EtkMultiSprache getModelAddText(EtkProject project, String modelNumber, String productNumber) {
        if (StrUtils.isValid(modelNumber)) {
            if (StrUtils.isValid(productNumber)) {
                iPartsDataProductModels productModel = iPartsProductModels.getInstance(project).getProductModelsByModelAndProduct(project, modelNumber, productNumber);
                if (productModel != null) {
                    EtkMultiSprache productModeltext = productModel.getFieldValueAsMultiLanguage(FIELD_DPM_TEXTNR);
                    if ((productModeltext != null) && !productModeltext.isEmpty()) {
                        return productModeltext;
                    }
                }
            }
            return iPartsModel.getInstance(project, new iPartsModelId(modelNumber)).getModelAddText(project);
        }
        return new EtkMultiSprache();
    }
}
