/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.SetOfEnumDataType;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFieldKeyNormal;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.forms.common.SimpleSelectSearchResultGrid;
import de.docware.apps.etk.base.forms.events.OnCreateAttributesEvent;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDbObjectsLayer;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetSource;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsProductModelsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsProductModels;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.OnEditChangeRecordEvent;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.*;
import de.docware.framework.modules.gui.app.DWLayoutManager;
import de.docware.framework.modules.gui.controls.GuiSeparator;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogButtons;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogIcon;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerFireOnce;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Formular für die Anzeige der Stammdaten von Produkten (Tabelle DA_PRODUCT und DA_PRODUCT_SERIES).
 */
public class MasterDataProductForm extends SimpleMasterDataSearchFilterGrid {

    private final static String SPECIAL_FIELD_NAME_FOR_FACTORY_NUMBERS = TableAndFieldName.make(TABLE_DA_PRODUCT, iPartsDataVirtualFieldsDefinition.DP_VALID_FACTORIES) + "_";
    private final static String SPECIAL_FIELD_NAME_FOR_VARIANTS = TableAndFieldName.make(TABLE_DA_PRODUCT, iPartsDataVirtualFieldsDefinition.DP_VARIANTS) + "_";
    private final static String SPECIAL_FIELD_NAME_FOR_CONNECTED_MODEL_IDS = TableAndFieldName.make(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_MODEL_NO) + "_";

    private Set<String> modelTypesSearchTexts;
    private final boolean isPSKAllowed = iPartsRight.checkPSKInSession();
    private final boolean hasBothCarAndTruckRights = iPartsRight.checkUserHasBothVehicleTypeRightsInSession();

    /**
     * Zeigt Produkt-Stammdaten an. Wenn ein Produktknoten im Navigationsbaum selektiert ist, wird er als
     * erster Treffer in der Produkt-Stammdatenliste angezeigt. Falls keiner selektiert ist, wird eine leere Stammdatenliste
     * angezeigt.
     *
     * @param owner
     */
    public static void showProductMasterData(AbstractJavaViewerForm owner) {
        // Aktive Form holen
        AbstractJavaViewerForm activeForm = owner.getConnector().getActiveForm();
        iPartsProductId productId = new iPartsProductId(getIdFromTreeSelectionForType(activeForm, iPartsProductId.TYPE));
        if (!productId.isValidId()) {
            productId = null;
        }
        showProductMasterData(activeForm.getConnector(), activeForm, productId, null);
    }

    /**
     * Anzeige der Produkt Tabelle (DA_PRODUCT)
     *
     * @param dataConnector
     * @param parentForm
     * @param onEditChangeRecordEvent
     */
    public static void showProductMasterData(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                             iPartsProductId productId, OnEditChangeRecordEvent onEditChangeRecordEvent) {
        if (onEditChangeRecordEvent == null) {
            // Falls nicht extern definiert: Hier die Callbacks für Edit, Modify und Delete definieren
            onEditChangeRecordEvent = new OnEditChangeRecordEvent() {
                private boolean onEditCreateOrModifyRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName,
                                                                IdWithType id, DBDataObjectAttributes attributes, boolean createRecord) {
                    iPartsProductId productId = new iPartsProductId(id.getValue(1));
                    EtkProject project = dataConnector.getProject();
                    iPartsDataProduct dataProduct = new iPartsDataProduct(project, productId);
                    if (dataProduct.loadFromDB(productId) && createRecord) {
                        String msg = "!!Das Produkt ist bereits vorhanden und kann nicht neu angelegt werden!";
                        MessageDialog.show(msg, "!!Erzeugen", MessageDialogIcon.ERROR, MessageDialogButtons.OK);
                        return true;
                    }

                    if (createRecord) {
                        dataProduct.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                    }

                    //zur Sicherheit die Attributes kopieren
                    DBDataObjectAttributes newAttributes = new DBDataObjectAttributes();
                    newAttributes.assign(attributes, DBActionOrigin.FROM_DB);

                    // Werke zum Produkt mit den echten Daten abgleichen
                    Set<String> factoryNumbersList = getFactoryNumbersFromAttributes(newAttributes);
                    updateProductFactories(dataConnector, dataProduct, factoryNumbersList);

                    if (iPartsRight.checkPSKInSession()) {
                        // Varianten zu Produkt mit den echten Daten abgleichen
                        Set<String> variantsValuesList = getVariantValuesFromAttributes(newAttributes);
                        updateProductVariants(dataConnector, dataProduct, variantsValuesList);
                    }

                    //getConnectedModelIds rausholen
                    List<iPartsModelId> modelIdList = getConnectedModelIdsFromAttributes(newAttributes);

                    updateProductModels(dataConnector, dataProduct, modelIdList);
                    dataProduct.assignAttributesValues(project, newAttributes, true, DBActionOrigin.FROM_EDIT);

                    // Fremde Felder (darunter auch die berechneten Typkennzahlen) vor dem Speichern in die Datenbank entfernen
                    dataProduct.removeForeignTablesAttributes();

                    project.getDbLayer().startTransaction();
                    try {
                        boolean wasAggregateProduct = false;

                        // Bei einem neuen Produkt macht die Abfrage keinen Sinn, da die Daten noch gar nicht abgespeichert wurden
                        if (!createRecord) {
                            iPartsProduct product = iPartsProduct.getInstance(project, productId);
                            wasAggregateProduct = product.isAggregateProduct(project);
                        }

                        iPartsRevisionChangeSet changeSet = iPartsRevisionChangeSet.createTempChangeSet(project, iPartsChangeSetSource.PRODUCT);

                        // DAIMLER-4841: Änderungszeitstempel am Produkt setzen
                        dataProduct.refreshModificationTimeStamp();

                        changeSet.addDataObject(dataProduct, false, false, false);
                        if (!dataObjectList.isEmpty()) {
                            //changeSet.addDataObjectList(dataObjectList);
                            for (EtkDataObject dataObject : dataObjectList) {
                                changeSet.addDataObject(dataObject, false, false, false);
                            }
                        }

                        if (changeSet.commit()) {
                            Set<iPartsModelId> newModelIds = new HashSet<>();
                            Set<iPartsModelId> modifiedModelIds = new HashSet<>();
                            dataProduct.saveToDB();
                            if (!dataObjectList.isEmpty()) {
                                GenericEtkDataObjectList objectsForDB = new GenericEtkDataObjectList();
                                for (EtkDataObject dataObject : dataObjectList) {
                                    if (dataObject instanceof iPartsDataModel) {
                                        iPartsDataModel dataModel = (iPartsDataModel)dataObject;
                                        if (dataModel.isNew()) {
                                            newModelIds.add(dataModel.getAsId());
                                        } else {
                                            modifiedModelIds.add(dataModel.getAsId());
                                        }
                                        objectsForDB.add(dataModel, DBActionOrigin.FROM_EDIT);
                                    } else if ((dataObject instanceof iPartsDataModelProperties) || (dataObject instanceof iPartsDataPSKProductVariant)) {
                                        objectsForDB.add(dataObject, DBActionOrigin.FROM_EDIT);
                                    }
                                }
                                objectsForDB.saveToDB(project);
                            }
                            project.getDbLayer().commit();

                            // Produkt aus dem Cache entfernen, damit die folgende Abfrage über iPartsProduct.getInstance()
                            // nicht auf alten Daten arbeitet
                            iPartsProduct.removeProductFromCache(project, productId);

                            boolean clearProductsCache = wasAggregateProduct || iPartsProduct.getInstance(project, productId).isAggregateProduct(project);
                            iPartsDataChangedEventByEdit.Action action = createRecord ? iPartsDataChangedEventByEdit.Action.NEW : iPartsDataChangedEventByEdit.Action.MODIFIED;
                            ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PRODUCT,
                                                                                                                      action,
                                                                                                                      productId,
                                                                                                                      clearProductsCache));

                            // Benachrichtigung für alle neuen und veränderten AS-Baumuster versenden
                            if (!newModelIds.isEmpty()) {
                                ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.MODEL,
                                                                                                                          iPartsDataChangedEventByEdit.Action.NEW,
                                                                                                                          newModelIds,
                                                                                                                          false));
                            }
                            if (!modifiedModelIds.isEmpty()) {
                                ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.MODEL,
                                                                                                                          iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                                                          modifiedModelIds,
                                                                                                                          false));
                            }

                            // Bei veränderten Produkten den Cache iPartsProductModels aktualisieren (wird bei gelöschten
                            // und neuen Produkten über den iPartsDataChangedEventByEdit gemacht)
                            if (action == iPartsDataChangedEventByEdit.Action.MODIFIED) {
                                iPartsProductModels.getInstance(project).updateCacheByProduct(project, productId);
                            }

                            ApplicationEvents.fireEventInAllProjectsAndAllClusters(new DataChangedEvent(null));
                            return true;
                        } else {
                            project.getDbLayer().rollback();
                            return false;
                        }
                    } catch (Exception e) {
                        project.getDbLayer().rollback();
                        Logger.getLogger().handleRuntimeException(e);
                    } finally {
                        dataObjectList.clear();
                    }
                    return false;
                }

                private Set<String> getFactoryNumbersFromAttributes(DBDataObjectAttributes attributes) {
                    return getValuesForPrefixFromAttributes(attributes, SPECIAL_FIELD_NAME_FOR_FACTORY_NUMBERS);
                }

                private Set<String> getVariantValuesFromAttributes(DBDataObjectAttributes attributes) {
                    return getValuesForPrefixFromAttributes(attributes, SPECIAL_FIELD_NAME_FOR_VARIANTS);
                }

                /**
                 * Liefert die künstlich angelegten String Attribute zu dem übergebenen Attribut-Präfix
                 *
                 * @param attributes
                 * @param attributePrefix
                 * @return
                 */
                private Set<String> getValuesForPrefixFromAttributes(DBDataObjectAttributes attributes, String attributePrefix) {
                    Set<String> resultSet = new TreeSet<>();
                    int lfdNr = 0;
                    String attributeName = attributePrefix + lfdNr;
                    DBDataObjectAttribute attribute = attributes.getField(attributeName, false);
                    while (attribute != null) {
                        resultSet.add(attribute.getAsString());
                        attributes.deleteField(attributeName, false, DBActionOrigin.FROM_DB);
                        lfdNr++;
                        attributeName = attributePrefix + lfdNr;
                        attribute = attributes.getField(attributeName, false);
                    }
                    return resultSet;
                }

                private void updateProductFactories(AbstractJavaViewerFormIConnector dataConnector, iPartsDataProduct dataProduct,
                                                    Set<String> factoryNumbersSet) {
                    Set<String> newFactoryNumbersSet = new LinkedHashSet<>(factoryNumbersSet);
                    DBDataObjectList<iPartsDataProductFactory> factories = dataProduct.getProductFactoriesList();
                    if (!factories.isEmpty()) {
                        Iterator<iPartsDataProductFactory> productFactoriesIterator = factories.iterator();
                        while (productFactoriesIterator.hasNext()) {
                            iPartsDataProductFactory productFactory = productFactoriesIterator.next();
                            String factoryNumberFromProduct = productFactory.getAsId().getFactoryNumber();
                            if (!newFactoryNumbersSet.contains(factoryNumberFromProduct)) {
                                // Werksnummer ist nicht mehr ausgewählt -> aus productFactoriesList entfernen
                                productFactoriesIterator.remove();
                            } else {
                                // Werksnummer war bisher bereits zugewiesen -> aus newFactoryNumbersSet entfernen
                                newFactoryNumbersSet.remove(factoryNumberFromProduct);
                            }
                        }
                    }

                    // Alle neuen Werksnummern hinzufügen, die jetzt noch in newFactoryNumbers enthalten sind
                    String productNumber = dataProduct.getAsId().getProductNumber();
                    EtkProject project = dataConnector.getProject();
                    for (String factoryNumber : newFactoryNumbersSet) {
                        iPartsProductFactoryId productFactoryId = new iPartsProductFactoryId(productNumber, factoryNumber);
                        iPartsDataProductFactory productFactory = new iPartsDataProductFactory(project, productFactoryId);
                        productFactory.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                        dataProduct.getProductFactoriesList().add(productFactory, DBActionOrigin.FROM_EDIT);
                    }
                }

                /**
                 * Gleicht die bearbeiteten Produktvarianten mit den echten Varianten am Produkt ab
                 *
                 * @param dataConnector
                 * @param dataProduct
                 * @param variantValues
                 */
                private void updateProductVariants(AbstractJavaViewerFormIConnector dataConnector, iPartsDataProduct dataProduct,
                                                   Set<String> variantValues) {
                    if (!iPartsRight.checkPSKInSession()) {
                        return;
                    }
                    Set<String> newVariantValuesSet = new LinkedHashSet<>(variantValues);
                    DBDataObjectList<iPartsDataPSKProductVariant> variantsList = dataProduct.getProductVariantsList();
                    if (!variantsList.isEmpty()) {
                        Iterator<iPartsDataPSKProductVariant> productVariantsIterator = variantsList.iterator();
                        while (productVariantsIterator.hasNext()) {
                            iPartsDataPSKProductVariant productVariant = productVariantsIterator.next();
                            String variantFromProduct = productVariant.getAsId().getVariantId();
                            if (!newVariantValuesSet.contains(variantFromProduct)) {
                                // Variante ist nicht mehr ausgewählt -> aus productVariantsList entfernen
                                productVariantsIterator.remove();
                            } else {
                                // Variante war bisher bereits zugewiesen -> aus newVariantValuesSet entfernen
                                newVariantValuesSet.remove(variantFromProduct);
                            }
                        }
                    }

                    // Alle neuen Varianten hinzufügen, die jetzt noch in newVariantValuesSet enthalten sind
                    String productNumber = dataProduct.getAsId().getProductNumber();
                    EtkProject project = dataConnector.getProject();
                    for (String variantId : newVariantValuesSet) {
                        iPartsPSKProductVariantId productVariantId = new iPartsPSKProductVariantId(productNumber, variantId);
                        iPartsDataPSKProductVariant productVariant = new iPartsDataPSKProductVariant(project, productVariantId);
                        productVariant.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                        dataProduct.getProductVariantsList().add(productVariant, DBActionOrigin.FROM_EDIT);
                    }
                }

                private List<iPartsModelId> getConnectedModelIdsFromAttributes(DBDataObjectAttributes attributes) {
                    List<iPartsModelId> modelIdList = new DwList<>();
                    int lfdNr = 0;
                    String attributeName = SPECIAL_FIELD_NAME_FOR_CONNECTED_MODEL_IDS + lfdNr;
                    DBDataObjectAttribute connectedModelAttribute = attributes.getField(attributeName, false);
                    while (connectedModelAttribute != null) {
                        modelIdList.add(new iPartsModelId(connectedModelAttribute.getAsString()));
                        attributes.deleteField(attributeName, false, DBActionOrigin.FROM_DB);
                        lfdNr++;
                        attributeName = SPECIAL_FIELD_NAME_FOR_CONNECTED_MODEL_IDS + lfdNr;
                        connectedModelAttribute = attributes.getField(attributeName, false);
                    }
                    return modelIdList;
                }

                private void updateProductModels(AbstractJavaViewerFormIConnector dataConnector, iPartsDataProduct dataProduct,
                                                 List<iPartsModelId> connectedModelIdList) {
                    EtkProject project = dataConnector.getProject();

                    // Baumuster
                    if (!connectedModelIdList.isEmpty()) {
                        Set<iPartsModelId> newModelIds = new LinkedHashSet<>(connectedModelIdList);
                        if (!dataProduct.getProductModelsList().isEmpty()) {
                            Iterator<iPartsDataProductModels> productModelsIterator = dataProduct.getProductModelsList().iterator();
                            while (productModelsIterator.hasNext()) {
                                iPartsDataProductModels productModel = productModelsIterator.next();
                                iPartsModelId modelIdFromProduct = new iPartsModelId(productModel.getAsId().getModelNumber());
                                if (!newModelIds.contains(modelIdFromProduct)) {
                                    // Baumuster ist nicht mehr ausgewählt -> aus productModelsList entfernen
                                    productModelsIterator.remove();
                                } else {
                                    // Baumuster war bisher bereits zugewiesen -> aus newModelIds entfernen
                                    newModelIds.remove(modelIdFromProduct);
                                }
                            }
                        }

                        // Alle neuen Baumuster hinzufügen, die jetzt noch in newModelIds enthalten sind
                        for (iPartsModelId modelId : newModelIds) {
                            iPartsProductModelsId productModelId = new iPartsProductModelsId(dataProduct.getAsId(), modelId);
                            iPartsDataProductModels productModel = new iPartsDataProductModels(project, productModelId);
                            productModel.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
                            dataProduct.getProductModelsList().add(productModel, DBActionOrigin.FROM_EDIT);
                        }
                    } else {
                        // Keine Baumuster ausgewählt => alle alten Baumuster löschen
                        dataProduct.getProductModelsList().deleteAll(DBActionOrigin.FROM_EDIT);
                    }
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
                        // Auskommentiert, weil wir aktuell das Löschen von Produkten mit Modulen zulassen und die dazugehörigen Module
                        // sogar explizit löschen
//                        List<String> noDelList = new DwList<String>();
//                        for (int lfdNr = attributeList.size() - 1; lfdNr >= 0; lfdNr--) {
//                            DBDataObjectAttributes attributes = attributeList.get(lfdNr);
//                            iPartsProductId productId = new iPartsProductId(attributes.getField(FIELD_DP_PRODUCT_NO).getAsString());
//                            iPartsDataProduct dataProduct = new iPartsDataProduct(dataConnector.getProject(), productId);
//                            if (!dataProduct.getProductModulesList().isEmpty()) {
//                                noDelList.add(productId.getProductNumber());
//                                attributeList.remove(lfdNr);
//                            }
//                        }
//                        if (!noDelList.isEmpty()) {
//                            String msg = "!!Die selektierten Produkte enthalten bereits Module und können nicht gelöscht werden!";
//                            MessageDialog.show(msg, "!!Löschen", MessageDialogIcon.WARNING, MessageDialogButtons.OK);
//
//                        } else {
                        String msg = "!!Wollen Sie das selektierte Produkt inkl. aller enthaltenen Module wirklich löschen?";
                        if (attributeList.size() > 1) {
                            msg = "!!Wollen Sie die selektierten Produkte inkl. aller enthaltenen Module wirklich löschen?";
                        }
                        if (MessageDialog.show(msg, "!!Löschen", MessageDialogIcon.CONFIRMATION,
                                               MessageDialogButtons.YES, MessageDialogButtons.NO) == ModalResult.YES) {
                            return true;
                        }
//                        }
                    }
                    return false;
                }

                @Override
                public boolean onEditDeleteRecordEvent(final AbstractJavaViewerFormIConnector dataConnector, String tableName,
                                                       final DBDataObjectAttributesList attributesList) {
                    final VarParam<Boolean> success = new VarParam<>(false);
                    if ((attributesList != null) && !attributesList.isEmpty()) {
                        final EtkMessageLogForm messageLogForm = new EtkMessageLogForm("!!Löschen", "!!Produkt löschen inkl. aller enthaltenen Module",
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
                                            boolean clearProductCache = false;
                                            List<iPartsProductId> productIds = new ArrayList<>(attributesList.size());
                                            for (DBDataObjectAttributes attributes : attributesList) {
                                                iPartsProductId productId = new iPartsProductId(attributes.getField(FIELD_DP_PRODUCT_NO).getAsString());

                                                if (!iPartsRight.checkProductEditableInSession(productId, iPartsRight.DELETE_MASTER_DATA,
                                                                                               true, project)) {
                                                    continue;
                                                }

                                                if (iPartsProduct.getInstance(project, productId).isAggregateProduct(project)) {
                                                    clearProductCache = true;
                                                } else {
                                                    productIds.add(productId);
                                                }
                                                messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Produkt %1 wird gelöscht",
                                                                                                                        productId.getProductNumber()),
                                                                                           MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                                                iPartsDataProduct dataProduct = new iPartsDataProduct(project, productId);
                                                dataProduct.loadChildren(); // Kinder müssen vor dem Löschen geladen werden, damit diese ebenfalls gelöscht werden
                                                if (iPartsRevisionChangeSet.deleteDataObjectWithChangeSet(project, dataProduct, iPartsChangeSetSource.PRODUCT)) {
                                                    if (!dataProduct.deleteFromDBWithModules(true, messageLogForm.getMessageLog())) {
                                                        deleteOK = false;
                                                        break;
                                                    }
                                                } else {
                                                    deleteOK = false;
                                                    break;
                                                }
                                            }

                                            if (deleteOK) {
                                                dbLayer.endBatchStatement();
                                                dbLayer.commit();

                                                messageLogForm.getMessageLog().fireMessage("!!Löschen abgeschlossen", MessageLogType.tmlMessage,
                                                                                           MessageLogOption.TIME_STAMP);

                                                if (clearProductCache || !productIds.isEmpty()) {
                                                    ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PRODUCT,
                                                                                                                                              iPartsDataChangedEventByEdit.Action.DELETED,
                                                                                                                                              productIds,
                                                                                                                                              clearProductCache));
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
                        });
                    }
                    return success.getValue();
                }
            };
        }

        MasterDataProductForm dlg = new MasterDataProductForm(dataConnector, parentForm, TABLE_DA_PRODUCT, onEditChangeRecordEvent);
        // Suchfelder definieren
        EtkDisplayFields searchFields = getSearchFields(dataConnector);
        // Anzeigefelder definieren
        EtkDisplayFields displayFields = getDisplayFields(dataConnector);
        // Editfelder fürs Editieren festlegen
        EtkEditFields editFields = getEditFields(dataConnector);
        //Required Felder setzen, auf jeden Fall alle aus den EditFeldern, die nicht in den DisplayFields enthalten sind
        EtkDisplayFields requiredFields = getRequiredFields(dataConnector, displayFields, editFields);

        // Sortierung
        LinkedHashMap<String, Boolean> sortFields = new LinkedHashMap<>();
        sortFields.put(FIELD_DP_PRODUCT_NO, false);
        dlg.setSortFields(sortFields);

        dlg.setDisplayResultFields(displayFields);
        dlg.setSearchFields(searchFields);
        dlg.setEditFields(editFields);
        dlg.setRequiredResultFields(requiredFields);
        boolean isCarOrTruckUser = dlg.isCarAndVanInSession() || dlg.isTruckAndBusInSession();
        boolean editMasterDataAllowed = iPartsRight.EDIT_MASTER_DATA.checkRightInSession() && isCarOrTruckUser;
        boolean deleteMasterDataAllowed = iPartsRight.DELETE_MASTER_DATA.checkRightInSession() && isCarOrTruckUser;
        dlg.setEditAllowed(editMasterDataAllowed || deleteMasterDataAllowed);
        dlg.setNewAllowed(editMasterDataAllowed);
        dlg.setModifyAllowed(editMasterDataAllowed);
        dlg.setDeleteAllowed(deleteMasterDataAllowed);
        dlg.setTitlePrefix("!!Produkt");
        dlg.setWindowName("ProductMasterData");
        //Vorbesetzung bei NEW:
        dlg.setOnCreateEvent(new OnCreateAttributesEvent() {
            @Override
            public DBDataObjectAttributes onCreateAttributesEvent() {
                DBDataObjectAttributes attributes = new DBDataObjectAttributes();
                attributes.addField(FIELD_DP_STRUCTURING_TYPE, PRODUCT_STRUCTURING_TYPE.KG_TU.name(), DBActionOrigin.FROM_DB);
                attributes.addField(FIELD_DP_BRAND, SetOfEnumDataType.getSetOfEnumTag(iPartsConst.BRAND_MERCEDES_BENZ), DBActionOrigin.FROM_DB);
                // DAIMLER-14042
                attributes.addField(FIELD_DP_FULL_LANGUAGE_SUPPORT, SQLStringConvert.booleanToPPString(true), DBActionOrigin.FROM_DB);
                return attributes;
            }
        });

        if ((productId != null) && productId.isValidId()) {
            // Suchwerte setzen und Suche starten
            DBDataObjectAttributes searchAttributes = new DBDataObjectAttributes();
            searchAttributes.addField(FIELD_DP_PRODUCT_NO, productId.getProductNumber(), DBActionOrigin.FROM_DB);
            dlg.setSearchValues(searchAttributes);
        }
        dlg.showModal();
    }

    /**
     * Anzeige des Product/Models Dialog mit modelId und wahlweise Edit (ohne Delete)
     *
     * @param dataConnector
     * @param parentForm
     * @param modelId
     * @param editAllowed
     */
    private static void showProductMasterDataForModel(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                      iPartsModelId modelId, boolean editAllowed) {
        MasterDataProductForm dlg = new MasterDataProductForm(dataConnector, parentForm, TABLE_DA_PRODUCT, null);
        EtkProject project = dataConnector.getProject();

        // Anzeigefelder definieren
        EtkDisplayFields displayFields = getDisplayFields(dataConnector);
        // Editfelder fürs Editieren festlegen
        EtkEditFields editFields = getEditFields(dataConnector);
        editAllowed &= iPartsRight.EDIT_MASTER_DATA.checkRightInSession();

        dlg.setDisplayResultFields(displayFields);
        dlg.setSearchFields(null);
        dlg.showSearchFields(false);
        dlg.setEditFields(editFields);
        dlg.setRequiredResultFields(null);
        dlg.setEditAllowed(editAllowed);
        dlg.setDeleteAllowed(false);
        dlg.setTitlePrefix("!!Produkt");
        dlg.setWindowName("ProductForModelMasterData");
        dlg.setTitle(TranslationHandler.translate("!!Produkt Anzeige für Baumuster \"%1\"", modelId.getModelNumber()));
        dlg.doResizeWindow(SCREEN_SIZES.SCALE_FROM_PARENT);

        // Produkte für das Baumuster bestimmen und zur Liste hinzufügen
        // Alle Produkte zum Baumuster bestimmen. Nicht sichtbare Baumuster ebenfalls berücksichtigen.
        List<iPartsProduct> productList = iPartsProductHelper.getProductsForModel(project, modelId, null, null, false);
        List<DBDataObjectAttributes> attributesList = new DwList<>();
        for (iPartsProduct product : productList) {
            //iPartsProduct dataProduct = iPartsProduct.getInstance(dataConnector.getProject(), new iPartsProductId(dataProductModel.getAsId().getProductNumber()));
            iPartsDataProduct dataProduct = new iPartsDataProduct(project, product.getAsId());
            if (dataProduct.existsInDB()) {
                attributesList.add(dataProduct.getAttributes());
            }
        }

        dlg.fillByAttributesList(attributesList);

        dlg.showModal();
    }

    /**
     * Anzeige des Produkt-zu-Baumuster-Dialogs mit der übergebenen <i>modelId</i> ohne Edit.
     *
     * @param dataConnector
     * @param parentForm
     * @param modelId
     */
    public static void showProductMasterDataForModel(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                     iPartsModelId modelId) {
        showProductMasterDataForModel(dataConnector, parentForm, modelId, false);
    }

    private static EtkDisplayFields getRequiredFields(AbstractJavaViewerFormIConnector dataConnector, EtkDisplayFields displayFields, EtkEditFields editFields) {
        //Required Felder setzen, auf jeden Fall alle aus den EditFeldern, die nicht in den DisplayFields enthalten sind
        EtkDisplayFields requiredFields = new EtkDisplayFields();
        EtkProject project = dataConnector.getProject();

        for (EtkEditField editField : editFields.getFields()) {
            if (!displayFields.contains(editField.getKey().getName(), false)) {
                requiredFields.addFeld(createDisplayField(project, editField.getKey().getTableName(),
                                                          editField.getKey().getFieldName(), editField.isMultiLanguage(), editField.isArray()));
            }
        }
        return requiredFields;
    }

    private static EtkEditFields getEditFields(AbstractJavaViewerFormIConnector dataConnector) {
        // Editfelder fürs Editieren festlegen
        EtkEditFields editFields = new EtkEditFields();
        EtkProject project = dataConnector.getProject();

        boolean isPSKAllowed = iPartsRight.checkPSKInSession();
        editFields.load(dataConnector.getConfig(), iPartsEditConfigConst.iPARTS_EDIT_MASTER_PRODUCT_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_EDITFIELDS);
        if (editFields.size() == 0) {
            editFields.addFeld(createEditField(project, TABLE_DA_PRODUCT, FIELD_DP_PRODUCT_NO, false));
            EtkEditField structuringTypeEditField = createEditField(project, TABLE_DA_PRODUCT, FIELD_DP_STRUCTURING_TYPE, false);
            structuringTypeEditField.setMussFeld(true);
            editFields.addFeld(structuringTypeEditField);
            if (isPSKAllowed) {
                editFields.addFeld(createEditField(project, TABLE_DA_PRODUCT, FIELD_DP_PSK, false));
            }
            EtkEditField docuMethodEditField = createEditField(project, TABLE_DA_PRODUCT, FIELD_DP_DOCU_METHOD, false);
            docuMethodEditField.setMussFeld(true);
            editFields.addFeld(docuMethodEditField);
            editFields.addFeld(createEditField(project, TABLE_DA_PRODUCT, FIELD_DP_TITLE, true));
            //DAIMLER-1512: die beiden neuen Felder einhängen
            editFields.addFeld(createEditField(project, TABLE_DA_PRODUCT, FIELD_DP_PRODUCT_VISIBLE, false));
            editFields.addFeld(createEditField(project, TABLE_DA_PRODUCT, FIELD_DP_EPC_RELEVANT, false));
            editFields.addFeld(createEditField(project, TABLE_DA_PRODUCT, FIELD_DP_IS_SPECIAL_CAT, false));
            editFields.addFeld(createEditField(project, TABLE_DA_PRODUCT, FIELD_DP_TTZ_FILTER, false));
            editFields.addFeld(createEditField(project, TABLE_DA_PRODUCT, FIELD_DP_SCORING_WITH_MCODES, false));
            editFields.addFeld(createEditField(project, TABLE_DA_PRODUCT, FIELD_DP_SHOW_SAS, false));
            editFields.addFeld(createEditField(project, TABLE_DA_PRODUCT, FIELD_DP_NO_PRIMUS_HINTS, false));
            editFields.addFeld(createEditField(project, TABLE_DA_PRODUCT, FIELD_DP_USE_SVGS, false));
            editFields.addFeld(createEditField(project, TABLE_DA_PRODUCT, FIELD_DP_FULL_LANGUAGE_SUPPORT, false));
            EtkEditField editFieldBrand = createEditField(project, TABLE_DA_PRODUCT, FIELD_DP_BRAND, false);
            editFieldBrand.setMussFeld(true);
            editFields.addFeld(editFieldBrand);
            editFields.addFeld(createEditField(project, TABLE_DA_PRODUCT, FIELD_DP_PRODUCT_GRP, false));
            editFields.addFeld(createEditField(project, TABLE_DA_PRODUCT, FIELD_DP_ASPRODUCT_CLASSES, false));
            editFields.addFeld(createEditField(project, TABLE_DA_PRODUCT, FIELD_DP_AGGREGATE_TYPE, false));
            editFields.addFeld(createEditField(project, TABLE_DA_PRODUCT, FIELD_DP_VALID_COUNTRIES, false));
            editFields.addFeld(createEditField(project, TABLE_DA_PRODUCT, FIELD_DP_INVALID_COUNTRIES, false));
            editFields.addFeld(createEditField(project, TABLE_DA_PRODUCT, FIELD_DP_IDENT_CLASS_OLD, false));
            editFields.addFeld(createEditField(project, TABLE_DA_PRODUCT, FIELD_DP_PICTURE, false));
            editFields.addFeld(createEditField(project, TABLE_DA_PRODUCT, FIELD_DP_SERIES_REF, false));
            editFields.addFeld(createEditField(project, TABLE_DA_PRODUCT, FIELD_DP_DISABLED_FILTERS, false));
            editFields.addFeld(createEditField(project, TABLE_DA_PRODUCT, FIELD_DP_SUPPLIER_NO, false));
            editFields.addFeld(createEditField(project, TABLE_DA_PRODUCT, FIELD_DP_USE_FACTORY, false));
            EtkEditField editField = createEditField(project, TABLE_DA_PRODUCT, FIELD_DP_FINS, false);
            editField.setArray(true);
            editFields.addFeld(editField);
        } else {
            if (!isPSKAllowed) {
                int pskIndex = editFields.getIndexOfTableAndFeldName(new EtkDisplayFieldKeyNormal(TABLE_DA_PRODUCT, FIELD_DP_PSK));
                if (pskIndex >= 0) {
                    editFields.removeField(pskIndex);
                }
            }
        }

        // Virtuelles Feld für die Werke zu Produkt
        EtkEditField virtualEditField = createEditField(project, TABLE_DA_PRODUCT, iPartsDataVirtualFieldsDefinition.DP_VALID_FACTORIES, false);
        createVirtualField(project, virtualEditField, editFields, "!!Werke zu Produkt");
        // Virtuelles Feld für die Varianten zum Produkt. Nur erzeugen, wenn man das Recht dazu hat
        if (isPSKAllowed) {
            virtualEditField = createEditField(project, TABLE_DA_PRODUCT, iPartsDataVirtualFieldsDefinition.DP_VARIANTS, false);
            createVirtualField(project, virtualEditField, editFields, "!!Varianten zum Produkt");
        }

        EtkDatabaseTable tableDef = dataConnector.getConfig().getDBDescription().findTable(TABLE_DA_PRODUCT);
        List<String> pkFields = tableDef.getPrimaryKeyFields();
        for (EtkEditField eField : editFields.getFields()) {
            if (pkFields.contains(eField.getKey().getFieldName())) {
                eField.setMussFeld(true);
                eField.setEditierbar(false);  // ist PK-Value => nicht editierbar
            }
        }
        int asIndex = editFields.getIndexOfTableAndFeldName(new EtkDisplayFieldKeyNormal(TABLE_DA_PRODUCT, FIELD_DP_ASPRODUCT_CLASSES));
        if (asIndex >= 0) {
            editFields.getFeld(asIndex).setMussFeld(true);
        }
        return editFields;
    }

    private static void createVirtualField(EtkProject project, EtkEditField editField, EtkEditFields editFields, String labelTextValue) {
        editField.setDefaultText(false);
        EtkMultiSprache labelText = new EtkMultiSprache();
        labelText.setText(project.getViewerLanguage(), labelTextValue);
        editField.setText(labelText);
        editFields.addFeld(editField);
    }

    private static EtkDisplayFields getDisplayFields(AbstractJavaViewerFormIConnector dataConnector) {
        // Anzeigefelder definieren
        EtkDisplayFields displayFields = new EtkDisplayFields();
        EtkProject project = dataConnector.getProject();

        boolean isPSKAllowed = iPartsRight.checkPSKInSession();
        displayFields.load(dataConnector.getConfig(), iPartsEditConfigConst.iPARTS_EDIT_MASTER_PRODUCT_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS);
        if (displayFields.size() == 0) {
            EtkDisplayField displayField = addDisplayField(TABLE_DA_PRODUCT, FIELD_DP_PRODUCT_NO, false, false, null, project, displayFields);
            displayField.setColumnFilterEnabled(true);
            displayField = addDisplayField(TABLE_DA_PRODUCT, FIELD_DP_STRUCTURING_TYPE, false, false, null, project, displayFields);
            displayField.setColumnFilterEnabled(true);
            if (isPSKAllowed) {
                addDisplayField(TABLE_DA_PRODUCT, FIELD_DP_PSK, false, false, null, project, displayFields);
            }
            addDisplayField(TABLE_DA_PRODUCT, FIELD_DP_TITLE, true, false, null, project, displayFields);
            addDisplayField(TABLE_DA_PRODUCT, FIELD_DP_PRODUCT_VISIBLE, false, false, null, project, displayFields);
            addDisplayField(TABLE_DA_PRODUCT, FIELD_DP_EPC_RELEVANT, false, false, null, project, displayFields);
            addDisplayField(TABLE_DA_PRODUCT, FIELD_DP_IS_SPECIAL_CAT, false, false, null, project, displayFields);
            addDisplayField(TABLE_DA_PRODUCT, FIELD_DP_PICTURE, false, false, null, project, displayFields);
            addDisplayField(TABLE_DA_PRODUCT, FIELD_DP_SERIES_REF, false, false, null, project, displayFields);
            addDisplayField(TABLE_DA_PRODUCT, FIELD_DP_DATASET_DATE, false, false, null, project, displayFields);
            addDisplayField(TABLE_DA_PRODUCT, FIELD_DP_MIGRATION_DATE, false, false, null, project, displayFields);
            addDisplayField(TABLE_DA_PRODUCT, FIELD_DP_SOURCE, false, false, null, project, displayFields);
            addDisplayField(TABLE_DA_PRODUCT, FIELD_DP_NO_PRIMUS_HINTS, false, false, null, project, displayFields);
            addDisplayField(TABLE_DA_PRODUCT, FIELD_DP_USE_SVGS, false, false, null, project, displayFields);
            addDisplayField(TABLE_DA_PRODUCT, FIELD_DP_FULL_LANGUAGE_SUPPORT, false, false, null, project, displayFields);

            addDisplayField(TABLE_DA_PRODUCT, iPartsDataVirtualFieldsDefinition.DP_MODEL_TYPES, false, true, null, project, displayFields);
        } else {
            if (!isPSKAllowed) {
                int pskIndex = displayFields.getIndexOfFeld(TABLE_DA_PRODUCT, FIELD_DP_PSK, false);
                if (pskIndex >= 0) {
                    displayFields.removeField(pskIndex);
                }
            }
        }
        return displayFields;
    }

    private static EtkDisplayFields getSearchFields(AbstractJavaViewerFormIConnector dataConnector) {
        // Suchfelder definieren
        EtkDisplayFields searchFields = new EtkDisplayFields();
        EtkProject project = dataConnector.getProject();

        searchFields.load(dataConnector.getConfig(), iPartsEditConfigConst.iPARTS_EDIT_MASTER_PRODUCT_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_SEARCHFIELDS,
                          dataConnector.getConfig().getCurrentDatabaseLanguage());
        if (searchFields.size() == 0) {
            // Suchfelder definieren
            searchFields.addFeld(createSearchField(project, TABLE_DA_PRODUCT, FIELD_DP_PRODUCT_NO, false, false));
            searchFields.addFeld(createSearchField(project, TABLE_DA_PRODUCT, FIELD_DP_STRUCTURING_TYPE, false, false));

            EtkDisplayField displayField = createSearchField(project, TABLE_DA_PRODUCT, iPartsDataVirtualFieldsDefinition.DP_MODEL_TYPES, false, true);
            searchFields.addFeld(displayField);
        } else {
            if (!iPartsRight.checkPSKInSession()) {
                int pskIndex = searchFields.getIndexOfFeld(TABLE_DA_PRODUCT, FIELD_DP_PSK, false);
                if (pskIndex >= 0) {
                    searchFields.removeField(pskIndex);
                }
            }
        }
        return searchFields;
    }

    private GuiMenuItem showModelMenuItem;
    private GuiSeparator changeProductValuesSeparator;
    private GuiMenuItem changeProductValuesMenuItem;
    private GuiMenuItem retrieveProductPicturesMenuItem;
    private GuiMenuItem showSeriesEventsMenuItem;

    /**
     * Erzeugt eine Instanz von SimpleMasterDataSearchResultGrid.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public MasterDataProductForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName, OnEditChangeRecordEvent onEditChangeRecordEvent) {
        super(dataConnector, parentForm, tableName, onEditChangeRecordEvent);
        doResizeWindow(SCREEN_SIZES.MAXIMIZE);
    }

    @Override
    protected void addToolbarButtonsAndContextMenuItems(EditToolbarButtonMenuHelper toolbarHelper, GuiContextMenu contextMenu) {
        super.addToolbarButtonsAndContextMenuItems(toolbarHelper, contextMenu);

        boolean editMasterDataAllowed = iPartsRight.EDIT_MASTER_DATA.checkRightInSession();
        if (editMasterDataAllowed) {
            GuiSeparator separator = new de.docware.framework.modules.gui.controls.GuiSeparator();
            separator.setName("menuSeparator1");
            contextMenu.addChild(separator);
        }

        showModelMenuItem = toolbarHelper.createMenuEntry("bmanzeige", "!!Baumuster anzeigen...", DefaultImages.module.getImage(), new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                endSearch();
                doShowModels(event);
            }
        }, getUITranslationHandler());
        contextMenu.addChild(showModelMenuItem);
        showSeriesEventsMenuItem = toolbarHelper.createMenuEntry("showSeriesEvents", "!!Ereigniskette zur Baureihe anzeigen...", DefaultImages.module.getImage(), new de.docware.framework.modules.gui.event.EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                doShowSeriesEvents(event);
            }
        }, getUITranslationHandler());
        contextMenu.addChild(showSeriesEventsMenuItem);

        if (editMasterDataAllowed) {
            changeProductValuesSeparator = new de.docware.framework.modules.gui.controls.GuiSeparator();
            changeProductValuesSeparator.setName("changeProductValuesSeparator");
            contextMenu.addChild(changeProductValuesSeparator);

            changeProductValuesMenuItem = toolbarHelper.createMenuEntry("changevalues", "!!Produktwerte vereinheitlichen...", DefaultImages.module.getImage(), new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    endSearch();
                    doChangeProductValues(event);
                }
            }, getUITranslationHandler());
            contextMenu.addChild(changeProductValuesMenuItem);
        }

        boolean retrievePicturesAllowed = iPartsRight.RETRIEVE_PICTURES.checkRightInSession();
        if (retrievePicturesAllowed) {
            GuiSeparator separator = new de.docware.framework.modules.gui.controls.GuiSeparator();
            separator.setName("menuSeparatorRetrievePictures");
            contextMenu.addChild(separator);

            retrieveProductPicturesMenuItem = toolbarHelper.createMenuEntry("retrievePictures", "!!Zeichnungen nachfordern...",
                                                                            DefaultImages.image.getImage(), new EventListener(Event.MENU_ITEM_EVENT) {
                        @Override
                        public void fire(Event event) {
                            endSearch();
                            doRetrieveProductPictures(event);
                        }
                    }, getUITranslationHandler());
            contextMenu.addChild(retrieveProductPicturesMenuItem);
        }
    }

    private void doShowModels(Event event) {
        DBDataObjectAttributes attributes = getSelectedAttributes();
        if ((attributes != null) && !attributes.isEmpty()) {
            IdWithType id = buildIdFromAttributes(attributes);
            iPartsProductId productId = new iPartsProductId(id.toStringArrayWithoutType()[0]);
            MasterDataModelAfterSalesForm.showModelAfterSalesForProductData(getConnector(), this, productId);
        }
    }

    /**
     * Zeigt den Dialog zum Nachfordern von Zeichnungen an.
     *
     * @param event
     */
    private void doRetrieveProductPictures(Event event) {
        List<iPartsProductId> productIds = new DwList<iPartsProductId>();
        // Ausgewählte Produkte bestimmen
        DBDataObjectAttributesList selectedAttributesList = getSelectedAttributesList();
        boolean isSingleSelection = selectedAttributesList.size() == 1;
        for (DBDataObjectAttributes selectedAttributes : selectedAttributesList) {
            IdWithType id = buildIdFromAttributes(selectedAttributes);
            iPartsProductId productId = new iPartsProductId(id.getValue(1));

            // Bei nur einem nicht editierbaren Produkt eine Meldung ausgeben und den Dialog gar nicht erst anzeigen
            if (!iPartsRight.checkProductEditableInSession(productId, iPartsRight.EDIT_PARTS_DATA, isSingleSelection, getProject())) {
                if (isSingleSelection) {
                    return;
                } else {
                    continue;
                }
            }

            productIds.add(productId);
        }

        if (!productIds.isEmpty()) {
            RequestPicturesForm.showRequestOptionsForProducts(getConnector(), this, productIds);
        } else {
            MessageDialog.showWarning(TranslationHandler.translate("!!Keine Rechte zum Editieren der ausgewählten Produkte für den Benutzer \"%1\".",
                                                                   iPartsUserAdminDb.getLoginUserFullName()));
        }
    }

    private void doChangeProductValues(Event event) {
        String[] fieldNames = new String[]{ FIELD_DP_PRODUCT_VISIBLE, FIELD_DP_EPC_RELEVANT, FIELD_DP_USE_SVGS,
                                            FIELD_DP_PREFER_SVG, FIELD_DP_FULL_LANGUAGE_SUPPORT, FIELD_DP_DIALOG_POS_CHECK };
        iPartsDataProductList productList = new iPartsDataProductList();
        DBDataObjectAttributesList selectedAttributesList = getSelectedAttributesList();
        boolean isSingleSelection = selectedAttributesList.size() == 1;
        for (DBDataObjectAttributes selectAttribute : selectedAttributesList) {
            IdWithType id = buildIdFromAttributes(selectAttribute);
            iPartsProductId productId = new iPartsProductId(id.getValue(1));

            // Bei nur einem nicht editierbaren Produkt eine Meldung ausgeben und den Dialog gar nicht erst anzeigen
            if (!iPartsRight.checkProductEditableInSession(productId, iPartsRight.EDIT_MASTER_DATA, isSingleSelection, getProject())) {
                if (isSingleSelection) {
                    return;
                } else {
                    continue;
                }
            }

            iPartsDataProduct dataProduct = new iPartsDataProduct(getProject(), productId);
            if (dataProduct.loadFromDB(productId)) {
                productList.add(dataProduct, DBActionOrigin.FROM_DB);
            }
        }
        if (!productList.isEmpty()) {
            EtkEditFields externalEditFields = new EtkEditFields();
            for (String fieldName : fieldNames) {
                externalEditFields.addField(SimpleMasterDataSearchResultGrid.createEditField(getConnector().getProject(),
                                                                                             iPartsConst.TABLE_DA_PRODUCT, fieldName, false));
            }
            DBDataObjectAttributes attributes = EditUserMultiChangeControls.showEditUserMultiChangeControlsForProducts(getConnector(), externalEditFields, productList.getAsList());
            if (attributes != null) {
                for (iPartsDataProduct dataProduct : productList) {
                    dataProduct.assignAttributesValues(getProject(), attributes, true, DBActionOrigin.FROM_EDIT);
                }
                getDbLayer().startTransaction();
                try {
                    if (iPartsRevisionChangeSet.saveDataObjectListWithChangeSet(getProject(), productList, iPartsChangeSetSource.PRODUCT)) {
                        productList.saveToDB(getProject());
                        getDbLayer().commit();

                        // Produkte aus dem Cache entfernen und DataChangedEvent feuern, damit z.B. der Baugruppenbaum aktualisiert wird
                        Set<iPartsProductId> productIds = new HashSet<>();
                        for (iPartsDataProduct dataProduct : productList) {
                            productIds.add(dataProduct.getAsId());
                        }
                        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PRODUCT,
                                                                                                                  iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                                                  productIds,
                                                                                                                  false));
                        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new DataChangedEvent(null));

                        // Suche nochmals starten als Refresh für Table
                        startSearch();
                    } else {
                        getDbLayer().rollback();
                    }
                } catch (Exception e) {
                    getDbLayer().rollback();
                    Logger.getLogger().handleRuntimeException(e);
                }
            }
        } else {
            MessageDialog.showWarning(TranslationHandler.translate("!!Keine Rechte zum Editieren der ausgewählten Produkte für den Benutzer \"%1\".",
                                                                   iPartsUserAdminDb.getLoginUserFullName()));
        }
    }

    @Override
    protected Map<EtkDisplayField, String> getSearchFieldsAndValuesForQuery(boolean filterEmptyValues, boolean applyWildcardSettings) {
        Map<EtkDisplayField, String> searchFieldsAndValues = super.getSearchFieldsAndValuesForQuery(filterEmptyValues, applyWildcardSettings);
        if (!isPSKAllowed) {
            // PSK-Produkte gar nicht auflisten
            searchFieldsAndValues.put(new EtkDisplayField(TABLE_DA_PRODUCT, FIELD_DP_PSK, false, false), SQLStringConvert.booleanToPPString(Boolean.FALSE));
        }
        return searchFieldsAndValues;
    }

    @Override
    protected boolean doValidateAttributes(DBDataObjectAttributes attributes) {
        if (!super.doValidateAttributes(attributes)) {
            return false;
        }
        iPartsProduct product = iPartsProduct.getInstance(getProject(), new iPartsProductId(attributes.getFieldValue(FIELD_DP_PRODUCT_NO)));
        // PSK Produkte sollen immer angezeigt werden, wenn der Benutzer die PSK Rechte hat. Der Check auf Basis
        // der Produktklassen soll keinen Einfluss darauf haben.
        if (product.isPSK()) {
            // Es ist ein PSK Produkt. Hat der Benutzer keine PSK Eigenschaft -> ausblenden. Falls doch, anzeigen.
            if (!isPSKAllowed) {
                return false;
            }
        } else if (!hasBothCarAndTruckRights && !iPartsFilterHelper.isProductVisibleForUserInSession(product)) { // Filterung nach Benutzer-Eigenschaften
            // Hat der Benutzer nicht beide Eigenschaften und ist das Produkt zu seinen Eigenschaften in der Session nicht
            // gültig, wird das Produkt ausgeblendet.
            return false;
        }

        // Filterung nach Typkennzahlen über das Suchfeld
        Set<String> allModelTypes = product.getReferencedSeriesOrAllModelTypes(getProject());
        if ((modelTypesSearchTexts != null) && !modelTypesSearchTexts.isEmpty()) {
            for (String modelType : allModelTypes) {
                for (String modelTypeSearchText : modelTypesSearchTexts) {
                    if (StrUtils.matchesSqlLike(modelTypeSearchText, modelType, false)) {
                        return true;
                    }
                }
            }
        } else {
            return true; // Keine Filterung nach Typkennzahlen
        }

        return false;
    }

    @Override
    protected SimpleSelectSearchResultGrid.GuiTableRowWithAttributes createRow(DBDataObjectAttributes attributes) {
        calculateVirtualFieldValues(attributes);
        return super.createRow(attributes);
    }

    private void calculateVirtualFieldValues(DBDataObjectAttributes attributes) {
        EtkDisplayFields displayFields = getDisplayResultFields();
        if (displayFields != null) {
            EtkDisplayField modelTypesField = displayFields.getFeldByName(TABLE_DA_PRODUCT, iPartsDataVirtualFieldsDefinition.DP_MODEL_TYPES, false);
            if ((modelTypesField != null) && modelTypesField.isVisible()) {
                iPartsDataProduct dataProduct = new iPartsDataProduct(getProject(), new iPartsProductId(attributes.getField(FIELD_DP_PRODUCT_NO).getAsString()));
                attributes.addField(dataProduct.getModelTypeAttribute(), DBActionOrigin.FROM_DB);
            }
        }
    }

    @Override
    protected void enableButtons() {
        super.enableButtons();

        int selectionRowCount = getTable().getSelectedRows().size();
        boolean singleSelection = selectionRowCount == 1;
        boolean multiSelection = selectionRowCount > 0;
        boolean isEventEnabled = singleSelection;

        showModelMenuItem.setEnabled(singleSelection);
        if (isEventEnabled) {
            iPartsSeriesId seriesId = getSelectedReferencedSeriesId();
            isEventEnabled = (seriesId != null) && seriesId.isValidId();
            if (isEventEnabled) {
                iPartsDialogSeries dialogSeries = iPartsDialogSeries.getInstance(getProject(), seriesId);
                isEventEnabled = dialogSeries.isEventTriggered();
            }
        }
        showSeriesEventsMenuItem.setEnabled(isEventEnabled);
        if (changeProductValuesMenuItem != null) {
            boolean changeProductValuesAllowed = isEditAllowed && iPartsRight.EDIT_MASTER_DATA.checkRightInSession();
            changeProductValuesSeparator.setVisible(changeProductValuesAllowed);
            changeProductValuesMenuItem.setVisible(changeProductValuesAllowed);
            changeProductValuesMenuItem.setEnabled(multiSelection && isEditAllowed);
        }
        if (retrieveProductPicturesMenuItem != null) {
            retrieveProductPicturesMenuItem.setEnabled(multiSelection);
        }

        // Auskommentiert, weil wir aktuell das Löschen von Produkten mit Modulen zulassen und die dazugehörigen Module
        // sogar explizit löschen
//        AbstractGuiToolComponent deleteButton = toolbarManager.getButton(EditToolbarButtonAlias.EDIT_DELETE.getAlias());
//        if (deleteButton.isEnabled()) {
//            DBDataObjectAttributes attributes = getSelectedAttributes();
//            if (attributes != null) {
//                deleteButton.setEnabled(!hasModules(getProject(), attributes));
//            }
//        }
    }

    private iPartsSeriesId getSelectedReferencedSeriesId() {
        DBDataObjectAttributes attributes = getSelectedAttributes();
        if ((attributes != null) && !attributes.isEmpty()) {
            IdWithType id = buildIdFromAttributes(getSelection());
            iPartsProductId productId = new iPartsProductId(id.getValue(1));
            iPartsProduct product = iPartsProduct.getInstance(getProject(), productId);
            return product.getReferencedSeries();
        }
        return null;
    }

    private boolean hasModules(EtkProject project, DBDataObjectAttributes attributes) {
        return hasModules(project, new iPartsProductId(attributes.getField(FIELD_DP_PRODUCT_NO).getAsString()));
    }

    private boolean hasModules(EtkProject project, iPartsProductId productId) {
        return iPartsProductStructures.getInstance(project, productId).hasModules(project);
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

        EditUserControlForProduct eCtrl = new EditUserControlForProduct(getConnector(), this, id, initialAttributes, editNewFields, true);
        eCtrl.setTitle(titleForCreate);
        eCtrl.setWindowName(editControlsWindowName);
        if (eCtrl.showModal() == ModalResult.OK) {
            if (onEditChangeRecordEvent != null) {
                id = buildIdFromAttributes(eCtrl.getAttributes());
                onEditChangeRecordEvent.dataObjectList.clear();
                addObjectsToList(eCtrl.getModifiedASDataModels());
                addObjectsToList(eCtrl.getModifiedConstDataModels());
                if (isPSKAllowed) {
                    addObjectsToList(eCtrl.getModifiedProductVariants());
                }
                if (onEditChangeRecordEvent.onEditCreateRecordEvent(getConnector(), searchTable, id, getAttributesWithStructureAndSeriesIds(eCtrl))) {
                    setSelectionAfterSearch(eCtrl.getAttributes());
                    setSearchValues(eCtrl.getAttributes());
                }
            }
        }
    }

    private DBDataObjectAttributes getAttributesWithStructureAndSeriesIds(EditUserControlForProduct eCtrl) {
        DBDataObjectAttributes attributes = new DBDataObjectAttributes();
        attributes.assign(eCtrl.getAttributes(), DBActionOrigin.FROM_DB);

        // Hier die Werksnummern hinzufügen
        Set<String> factoryNumbersSet = eCtrl.getFactoryNumbers();
        int lfdNr = 0;
        for (String factoryNumber : factoryNumbersSet) {
            attributes.addField(SPECIAL_FIELD_NAME_FOR_FACTORY_NUMBERS + lfdNr, factoryNumber, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
            lfdNr++;
        }

        if (isPSKAllowed) {
            // Hier künstliche Attribute mit den Varianten zu Produkt IDs anlegen
            Set<String> variantsValues = eCtrl.getProductVariantsValuesAsString();
            lfdNr = 0;
            for (String variant : variantsValues) {
                attributes.addField(SPECIAL_FIELD_NAME_FOR_VARIANTS + lfdNr, variant, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                lfdNr++;
            }
        }

        // Hier getConnectedModelIds hinzufügen
        List<iPartsModelId> modelIdList = eCtrl.getConnectedModelIds();
        lfdNr = 0;
        for (iPartsModelId modelId : modelIdList) {
            attributes.addField(SPECIAL_FIELD_NAME_FOR_CONNECTED_MODEL_IDS + lfdNr, modelId.getModelNumber(), DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
            lfdNr++;
        }
        return attributes;
    }

    @Override
    protected void doEditOrView(Event event) {
        endSearch();
        DBDataObjectAttributes attributes = getSelectedAttributes();
        if (attributes != null) {
            IdWithType id = buildIdFromAttributes(attributes);
            iPartsProductId productId = new iPartsProductId(id.getValue(1));
            iPartsProduct product = iPartsProduct.getInstance(getProject(), productId);
            boolean hasModules = iPartsProductStructures.getInstance(getProject(), productId).hasModules(getProject());
            EtkEditFields editNewFields = new EtkEditFields();
            editNewFields.assign(editFields);
            EtkEditField structuringTypeField = editNewFields.getFeldByName(TABLE_DA_PRODUCT, FIELD_DP_STRUCTURING_TYPE);
            if (structuringTypeField != null) {
                structuringTypeField.setEditierbar(structuringTypeField.isEditierbar() && !hasModules);
            }
            EditUserControlForProduct eCtrl = new EditUserControlForProduct(getConnector(), this, id, attributes, editNewFields, false);
            eCtrl.setWidth(Math.max(800, eCtrl.getPanelEditFields().getPreferredWidth() + 2 * DWLayoutManager.get().getBigPadding() + 8));
            boolean productEditable = isEditAllowed() && isModifyAllowed() && iPartsRight.checkProductEditableInSession(product.getAsId(),
                                                                                                                        iPartsRight.EDIT_MASTER_DATA,
                                                                                                                        true, getProject());
            eCtrl.setReadOnly(!productEditable);
            eCtrl.setTitle(productEditable ? titleForEdit : titleForView);
            eCtrl.setWindowName(editControlsWindowName);
            List<iPartsModelId> modelIds = new DwList<>();
            Set<String> models = product.getModelNumbers(getProject());
            if (!models.isEmpty()) {
                for (String model : models) {
                    modelIds.add(new iPartsModelId(model));
                }
                eCtrl.setConnectedModelIds(modelIds);
            }

            // Nach dem Öffnen vom Dialog die ScrollPosition auf jeden Fall auf (0, 0) setzen
            eCtrl.getGui().addEventListener(new EventListenerFireOnce(Event.OPENED_EVENT) {
                @Override
                public void fireOnce(Event event) {
                    eCtrl.getScrollPane().setScrollPosition(0, 0);
                }
            });

            if (eCtrl.showModal() == ModalResult.OK) {
                if (onEditChangeRecordEvent != null) {
                    onEditChangeRecordEvent.dataObjectList.clear();

                    addObjectsToList(eCtrl.getModifiedASDataModels());
                    addObjectsToList(eCtrl.getModifiedConstDataModels());
                    if (isPSKAllowed) {
                        addObjectsToList(eCtrl.getModifiedProductVariants());
                    }

                    if (onEditChangeRecordEvent.onEditModifyRecordEvent(getConnector(), searchTable, id, getAttributesWithStructureAndSeriesIds(eCtrl))) {
                        // Suche nochmals starten als Refresh für Table
                        setSelectionAfterSearch(eCtrl.getAttributes());
                        startSearch(true);
                    }
                }
            }
        }
    }

    private void addObjectsToList(Collection<? extends EtkDataObject> dataObjects) {
        if ((dataObjects != null) && !dataObjects.isEmpty()) {
            for (EtkDataObject dataObject : dataObjects) {
                onEditChangeRecordEvent.dataObjectList.add(dataObject);
            }
        }
    }

    @Override
    protected synchronized void internalStartSearch() {
        setMaxResults(-1); // Wegen diversen Filterungen (MBAB/DTAG und/oder Typkennzahlen) müssen alle Produkte aus der DB geladen werden

        // Wird nach den Typkennzahlen gesucht?
        String modelTypesSearchText = getSearchValue(TABLE_DA_PRODUCT, iPartsDataVirtualFieldsDefinition.DP_MODEL_TYPES, false);
        if (StrUtils.isValid(modelTypesSearchText)) {
            List<String> modelTypeSearchTextsRaw = StrUtils.toStringList(modelTypesSearchText, ",", false, true);
            modelTypesSearchTexts = new HashSet<>(modelTypeSearchTextsRaw.size());
            for (String modelTypeSearchTextRaw : modelTypeSearchTextsRaw) {
                // Ist der Suchtext ohne Wildcards leer?
                if (StrUtils.removeCharsFromString(modelTypeSearchTextRaw, new char[]{ '*', '?' }).trim().isEmpty()) {
                    continue;
                }

                modelTypesSearchTexts.add(modelTypeSearchTextRaw + "*"); // Wildcard hinten
            }

            // Überprüfen, ob nur nach den Typkennzahlen gesucht wird
            boolean searchModelTypesOnly = true;
            List<String> searchValues = getSearchValues();
            int searchValueCounter = 0;
            for (String searchValue : searchValues) {
                if (!searchValue.isEmpty()) {
                    searchValueCounter++;
                }

                if (searchValueCounter > 1) {
                    searchModelTypesOnly = false;
                    break;
                }
            }

            if (modelTypesSearchTexts.isEmpty() && searchModelTypesOnly) {
                showNoResultsLabel(true, false);
                MessageDialog.show("!!Der Suchtext für die Typkennzahlen darf nicht nur aus Wildcards und Leerzeichen bestehen.",
                                   "!!Ungültiger Suchtext für die Typkennzahlen");
                return;
            }
        } else {
            // kein Suchtext für Typekennzahlen eingegeben --> Suchstring zurücksetzen
            modelTypesSearchTexts = null;
        }

        super.internalStartSearch();
    }

    private void doShowSeriesEvents(Event event) {
        iPartsSeriesId seriesId = getSelectedReferencedSeriesId();
        if ((seriesId != null) && seriesId.isValidId()) {
            EditSeriesEventsForm.showSeriesEventsForSeries(getConnector(), this, seriesId);
        }
    }

}