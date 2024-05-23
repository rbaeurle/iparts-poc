/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPSKProductVariant;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPSKProductVariantList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsPSKProductVariantId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.config.db.DBConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.misc.id.IdWithType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Anzeige zum Verwalten von Varianten zu einem Produkt innerhalb eines Grids
 */
public class EditProductVariantsForm extends AbstractSimpleDataObjectGridEditForm implements iPartsConst {

    public static Collection<iPartsDataPSKProductVariant> showProductVariants(AbstractJavaViewerForm parentForm, iPartsProductId productId,
                                                                              Map<iPartsPSKProductVariantId, iPartsDataPSKProductVariant> selectedVariants, boolean isEditAllowed) {
        if (iPartsRight.checkPSKInSession()) {
            EditProductVariantsForm productVariantsForm = new EditProductVariantsForm(parentForm.getConnector(), parentForm, productId, selectedVariants, isEditAllowed);
            if (productVariantsForm.showModal() == ModalResult.OK) {
                return productVariantsForm.getVariantsFromGrid();
            }
        }
        return null;
    }

    private final iPartsProductId productId;
    private Map<iPartsPSKProductVariantId, iPartsDataPSKProductVariant> selectedVariantsMap;

    protected EditProductVariantsForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                      iPartsProductId productId, Map<iPartsPSKProductVariantId, iPartsDataPSKProductVariant> selectedVariants,
                                      boolean isEditAllowed) {
        super(dataConnector, parentForm, "", "!!Varianten",
              TranslationHandler.translate("!!Varianten zum Produkt %1", productId.getProductNumber()),
              "!!Varianten bearbeiten", isEditAllowed);
        this.productId = productId;
        initForm(selectedVariants);
    }

    private void initForm(Map<iPartsPSKProductVariantId, iPartsDataPSKProductVariant> selectedVariants) {
        setSelectedVariants(selectedVariants);
        getGrid().setNoResultsLabelText("!!Keine Varianten vorhanden");
        dataToGrid();
    }

    private void setSelectedVariants(Map<iPartsPSKProductVariantId, iPartsDataPSKProductVariant> selectedVariants) {
        selectedVariantsMap = selectedVariants;
    }

    /**
     * Liefert alle vorhandenen Varianten-Kennung zu diesem Produkt
     *
     * @return
     */
    private Set<String> getExistingVariantIds() {
        Set<String> result = new HashSet<>();
        if (iPartsRight.checkPSKInSession()) {
            Collection<iPartsDataPSKProductVariant> variantsFromGrid = getVariantsFromGrid();
            if (variantsFromGrid != null) {
                result = variantsFromGrid.stream()
                        .map(entry -> entry.getAsId().getVariantId())
                        .collect(Collectors.toSet());
            }
        }
        return result;
    }

    private Collection<iPartsDataPSKProductVariant> getVariantsFromGrid() {
        if (iPartsRight.checkPSKInSession()) {
            return getGrid().getDataObjectList(iPartsDataPSKProductVariant.class);
        }
        return null;
    }

    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields() {
        EtkDisplayFields displayFields = getProject().getAllDisplayFieldsForTable(TABLE_DA_PSK_PRODUCT_VARIANTS);
        if (displayFields != null) {
            displayFields.removeField(TABLE_DA_PSK_PRODUCT_VARIANTS, DBConst.FIELD_STAMP, false);
            return displayFields.getFields();
        }
        return null;
    }

    @Override
    protected DBDataObjectList<? extends EtkDataObject> createDataObjectList() {
        iPartsDataPSKProductVariantList list = new iPartsDataPSKProductVariantList();
        list.addAll(selectedVariantsMap.values(), DBActionOrigin.FROM_DB);
        return list;
    }

    @Override
    protected boolean isDeletionEnabled() {
        List<IdWithType> allProductVariantIds = getGrid().getSelectedObjectIds(TABLE_DA_PSK_PRODUCT_VARIANTS);
        return isDeletionEnabled(allProductVariantIds);
    }

    protected boolean isDeletionEnabled(List<IdWithType> allProductVariantIds) {
        return !allProductVariantIds.isEmpty();
    }

    @Override
    protected void doDelete() {
        List<IdWithType> allProductVariants = getGrid().getSelectedObjectIds(TABLE_DA_PSK_PRODUCT_VARIANTS);
        if (isDeletionEnabled(allProductVariants)) {
            if (MessageDialog.showYesNo("!!Möchten Sie wirklich alle ausgewählten Produktvarianten entfernen?") == ModalResult.YES) {
                allProductVariants.forEach(entry -> {
                    if (entry instanceof iPartsPSKProductVariantId) {
                        selectedVariantsMap.remove(entry);
                    }
                });
                dataToGrid();
            }
        }
    }

    @Override
    protected void doEdit() {
        List<List<EtkDataObject>> selection = getGrid().getMultiSelection();
        if (selection.size() == 1) {
            Optional<EtkDataObject> selectedProductVariant = selection.get(0).stream()
                    .filter(entry -> entry instanceof iPartsDataPSKProductVariant)
                    .findFirst();
            if (selectedProductVariant.isPresent()) {
                Set<String> allExistingVariants = getExistingVariantIds();
                iPartsDataPSKProductVariant productVariant = (iPartsDataPSKProductVariant)selectedProductVariant.get();
                allExistingVariants.remove(productVariant.getAsId().getVariantId());
                if (EditUserControlForProductVariants.editProductVariant(getConnector(), getParentForm(), productVariant, allExistingVariants)) {
                    dataToGrid();
                    setSelection(productVariant.getAsId());
                }
            }
        }
    }

    @Override
    protected void dataToGrid() {
        int sortColumn = getGrid().getSortColumnOfTable();
        super.dataToGrid();
        if (sortColumn < 0) {
            sortColumn = 1;
        }
        getGrid().sortTableAfterColumn(sortColumn, true);
    }

    @Override
    protected void doNew() {
        iPartsDataPSKProductVariant newVariant = EditUserControlForProductVariants.createNewProductVariant(getConnector(), getParentForm(), productId, getExistingVariantIds());
        if (newVariant != null) {
            iPartsPSKProductVariantId newId = newVariant.getAsId();
            selectedVariantsMap.put(newId, newVariant);
            dataToGrid();
            setSelection(newId);
        }
    }

    private void setSelection(iPartsPSKProductVariantId selectedId) {
        getGrid().setSelectedObjectId(selectedId, TABLE_DA_PSK_PRODUCT_VARIANTS, true, true);
    }
}
