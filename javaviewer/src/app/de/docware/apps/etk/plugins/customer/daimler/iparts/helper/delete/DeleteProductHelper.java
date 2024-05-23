/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper.delete;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDbObjectsLayer;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.utils.VarParam;
import de.docware.util.Utils;
import de.docware.util.date.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class DeleteProductHelper extends AbstractDeleteDataHelper {

    public DeleteProductHelper(EtkProject project) {
        super(project);
    }

    public boolean doDeleteProducts(List<iPartsProductId> productIdsToDelete) {
        showTestModi();
        if (!Utils.isValid(productIdsToDelete)) {
            addMessage("!!Es müssen keine Produkte gelöscht werden");
            return true;
        }
        addMessage("!!%1 Produkte werden gelöscht:", String.valueOf(productIdsToDelete.size()));
        VarParam<Boolean> success = new VarParam<>(false);
        long startTime = System.currentTimeMillis();

        project.executeWithoutActiveChangeSets(() -> {
            int progress = 0;
            List<iPartsProductId> productIds = new ArrayList<>(productIdsToDelete.size());
            boolean clearProductCache = false;
            // alle Produkte löschen
            for (iPartsProductId productId : productIdsToDelete) {
                if (Thread.currentThread().isInterrupted()) {
                    success.setValue(false);
                    break;
                }
                externMsgInterface.fireProgress(progress, productIdsToDelete.size());
                success.setValue(doDeleteProduct(productId));
                if (!success.getValue()) {
                    break;
                }
                if (iPartsProduct.getInstance(project, productId).isAggregateProduct(project)) {
                    clearProductCache = true;
                } else {
                    productIds.add(productId);
                }
                progress++;
            }
            externMsgInterface.hideProgress();
            if (success.getValue()) {
                if (clearProductCache || !productIds.isEmpty()) {
                    ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PRODUCT,
                                                                                                              iPartsDataChangedEventByEdit.Action.DELETED,
                                                                                                              productIds,
                                                                                                              clearProductCache));
                    ApplicationEvents.fireEventInAllProjectsAndAllClusters(new DataChangedEvent(null));
                }
            }
        }, true);

        if (success.getValue()) {
            deleteDuration = System.currentTimeMillis() - startTime;
            String timeDurationString = DateUtils.formatTimeDurationString(deleteDuration, false, false, getLogLanguage());
            addMessage("!!Laufzeit zum Löschen von %1 Produkten: %2", String.valueOf(productIdsToDelete.size()), timeDurationString);
        }
        return success.getValue();
    }

    public boolean doDeleteProduct(iPartsProductId productId) {
        if ((productId == null) || !productId.isValidId()) {
            addError("!!Ungültiges Produkt");
            return false;
        }

        // Beim Bereinigen der Datenbank explizit keine Berechtigungen zum Löschen der Produkte prüfen, aber AS-Produktklassen
        // dürfen nicht leer sein
        iPartsProduct product = iPartsProduct.getInstance(project, productId);
        if (product.getAsProductClasses().isEmpty()) {
            addWarning("!!Produkt %1 hat keine AS-Produktklassen und wird deswegen nicht gelöscht", productId.getProductNumber());
            return true;
        }

        EtkDbObjectsLayer dbLayer = project.getDbLayer();
        boolean deleteOK = true;
        try {
            dbLayer.startTransaction();
            dbLayer.startBatchStatement();

            addMessage("!!Produkt %1 wird gelöscht", productId.getProductNumber());
            if (!TEST_MODE) {
                iPartsDataProduct dataProduct = new iPartsDataProduct(project, productId);
                dataProduct.loadChildren(); // Kinder müssen vor dem Löschen geladen werden, damit diese ebenfalls gelöscht werden
                if (!dataProduct.deleteFromDBWithModules(true, null)) {
                    addError("!!Fehler beim  Löschen von Produkt \"%1\".", productId.getProductNumber());
                    deleteOK = false;
                }
            }

            if (deleteOK) {
                if (TEST_SYNTAX) {
                    dbLayer.cancelBatchStatement();
                    dbLayer.rollback();
                } else {
                    dbLayer.endBatchStatement();
                    dbLayer.commit();
                }

                addMessage("!!Löschen abgeschlossen");
            } else {
                dbLayer.cancelBatchStatement();
                dbLayer.rollback();
            }
        } catch (Exception e) {
            dbLayer.cancelBatchStatement();
            dbLayer.rollback();
            addException(e);
            deleteOK = false;
        }

        return deleteOK;
    }
}
