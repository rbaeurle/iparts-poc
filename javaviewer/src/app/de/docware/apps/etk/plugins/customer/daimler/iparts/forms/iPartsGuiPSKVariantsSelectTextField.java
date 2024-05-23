/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPSKProductVariant;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPSKProductVariantList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.GuiButtonTextField;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * {@link GuiButtonTextField} zur Auswahl von PSK Varianten.
 */
public class iPartsGuiPSKVariantsSelectTextField extends iPartsGuiArraySelectionTextField {

    public static final String TYPE = "iPartsGuiPSKVariantsSelectTextField";

    private Set<iPartsProductId> productIds; // Braucht man falls der Dialog nicht mit ProduktIDs initialisiert wird

    public iPartsGuiPSKVariantsSelectTextField(EtkProject project) {
        super(project, TYPE);
        productIds = new TreeSet<>();
    }


    @Override
    protected String getArrayAsFormattedString() {
        // String mit den visualisierten Arraywerten
        return project.getVisObject().getArrayAsFormattedString(dataArray, "", project.getDBLanguage(), iPartsConst.TABLE_KATALOG,
                                                                iPartsConst.FIELD_K_PSK_VARIANT_VALIDITY, false);
    }

    /**
     * ProductIds müssen später von außerhalb gesetzt werden
     *
     * @param parentForm
     */
    public void init(final AbstractJavaViewerForm parentForm) {
        if (parentForm != null) {
            addEventListener(new EventListener(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(Event event) {
                    Collection<String> pskVariants = EditSelectPSKVariantsForm.showSelectionVariants(parentForm, getProductVariantList(getProductIds()),
                                                                                                     dataArray.getArrayAsStringList());
                    if (pskVariants != null) {
                        addDataArrayFromSelection(pskVariants);
                    }
                }
            });
            addRadioButtons("!!Vereinheitlichen", "!!Hinzufügen");
        } else {
            removeEventListeners(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT);
        }
    }

    /**
     * Dialog initialisieren mit nur einem Produkt
     *
     * @param parentForm
     * @param productId
     */
    public void init(final AbstractJavaViewerForm parentForm, iPartsProductId productId) {
        Set<iPartsProductId> productIds = new TreeSet<>();
        if (productId != null) {
            productIds.add(productId);
        }
        init(parentForm, productIds);
    }

    public void init(final AbstractJavaViewerForm parentForm, Set<iPartsProductId> productIds) {
        setProductIds(productIds);
        init(parentForm);
    }

    private iPartsDataPSKProductVariantList getProductVariantList(Collection<iPartsProductId> productIds) {
        iPartsDataPSKProductVariantList productVariantList = new iPartsDataPSKProductVariantList();
        Set<String> variantIds = new HashSet<>();
        for (iPartsProductId productId : productIds) {
            iPartsDataPSKProductVariantList productVariantListOfProductId = iPartsDataPSKProductVariantList.loadPSKProductVariants(project, productId);
            for (iPartsDataPSKProductVariant dataPSKProductVariant : productVariantListOfProductId) {
                // Varianten-IDs nur einmal (für das erste Produkt) hinzufügen
                if (variantIds.add(dataPSKProductVariant.getAsId().getVariantId())) {
                    productVariantList.add(dataPSKProductVariant, DBActionOrigin.FROM_DB);
                }
            }
        }
        return productVariantList;
    }

    @Override
    public void setProductId(iPartsProductId productId) {
        Set<iPartsProductId> productIds = new HashSet<>();
        productIds.add(productId);
        setProductIds(productIds);
    }

    public void setProductIds(Set<iPartsProductId> productIds) {
        this.productIds = productIds;
    }

    public Set<iPartsProductId> getProductIds() {
        return productIds;
    }
}
