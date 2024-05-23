/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPSKProductVariant;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPSKProductVariantList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsPSKProductVariantId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObject;
import de.docware.framework.modules.gui.controls.GuiButtonTextField;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventCreator;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.util.StrUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {@link GuiButtonTextField} für das Öffnen der Verwaltung von Varianten zum Produkt
 */
public class iPartsGuiProductVariantsSelectTextField extends AbstractProductRelatedButtonTextField {

    private static final String PRODUCT_NOT_VALID_TOOLTIP = "!!Varianten können nur zu einer gültigen Produktnummer angelegt werden";

    private Map<iPartsPSKProductVariantId, iPartsDataPSKProductVariant> selectedVariants;
    private int initialSize;
    private EventListener currentListener;
    private boolean isForProductCreation;

    public iPartsGuiProductVariantsSelectTextField(EtkProject project) {
        super(project);
        selectedVariants = new LinkedHashMap<>();
    }

    private void checkProductNumber(iPartsProductId productId) {
        if (!selectedVariants.isEmpty()) {
            selectedVariants.forEach((key, productVariant) -> {
                productVariant.setFieldValue(FIELD_DPPV_PRODUCT_NO, productId.getProductNumber(), DBActionOrigin.FROM_EDIT);
                productVariant.updateIdFromPrimaryKeys();
                if (productVariant.isNew()) {
                    productVariant.updateOldId();
                }
            });
        }
    }

    private void addVariantListener(AbstractJavaViewerForm parentForm, iPartsProductId productId) {
        currentListener = new EventListener(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                // Edit ist nur erlaubt, wenn man das Recht für das Löschen und bearbeiten der Stammdaten hat
                boolean editMasterDataAllowed = iPartsRight.EDIT_MASTER_DATA.checkRightInSession();
                boolean deleteMasterDataAllowed = iPartsRight.DELETE_MASTER_DATA.checkRightInSession();
                // Hole alle Varianten, die angezeigt wurden
                Collection<iPartsDataPSKProductVariant> variants = EditProductVariantsForm.showProductVariants(parentForm, productId, selectedVariants, editMasterDataAllowed || deleteMasterDataAllowed);
                if (variants != null) {
                    // Zwischenspeichern
                    selectedVariants = variants.stream()
                            .collect(Collectors.toMap(iPartsDataPSKProductVariant::getAsId, Function.identity(), (entry1, entry2) -> entry2, LinkedHashMap::new));
                    // Angepasste Varianten ins das Textfield eintragen
                    String newText = variantsToString(selectedVariants);
                    // Hat sich der Text nicht geändert aber der Inhalt einer Variante, muss das Produkt-Control
                    // informiert werden (für Validierungsprüfungen)
                    if (newText.equals(getText())) {
                        // Finde mind. eine Variante, die modifiziert wurde
                        Optional<iPartsDataPSKProductVariant> result = variants.stream()
                                .filter(DBDataObject::isModifiedWithChildren)
                                .findFirst();
                        if (result.isPresent()) {
                            fireEvent(EventCreator.createOnChangeEvent(eventHandlerComponent, uniqueId));
                        }
                    } else {
                        setText(newText);
                    }
                }
            }
        };
        addEventListener(currentListener);
    }

    private void removeVariantListener() {
        if (currentListener != null) {
            removeEventListener(currentListener);
        }
    }

    private void setToolTipForFieldAndButton(String text) {
        setTooltip(text);
        setButtonTooltip(text);
    }

    /**
     * Liefert die textuelle Darstellung aller Varianten zum Produkt
     *
     * @param selectedVariants
     * @return
     */
    private String variantsToString(Map<iPartsPSKProductVariantId, iPartsDataPSKProductVariant> selectedVariants) {
        return selectedVariants.values().stream()
                .map(entry -> entry.getAsId().getVariantId())
                .collect(Collectors.joining("; "));
    }

    public boolean isForProductCreation() {
        return isForProductCreation;
    }

    public void setForProductCreation(boolean forProductCreation) {
        isForProductCreation = forProductCreation;
    }

    /**
     * Liefert alle neuen oder modifizierten Varianten {@link iPartsDataPSKProductVariant}
     *
     * @return
     */
    public Collection<iPartsDataPSKProductVariant> getModifiedVariantsObjects() {
        return selectedVariants.values().stream()
                .filter(entry -> entry.isNew() || entry.isModifiedWithChildren())
                .collect(Collectors.toList());
    }

    public Collection<iPartsDataPSKProductVariant> getSelectedVariants() {
        return selectedVariants.values();
    }

    @Override
    public void init(AbstractJavaViewerForm parentForm, iPartsProductId productId) {
        if (!iPartsRight.checkPSKInSession()) {
            return;
        }
        boolean validProductNo = (productId != null) && StrUtils.isValid(productId.getProductNumber());
        setButtonEnabled(validProductNo);
        if (validProductNo) {
            setToolTipForFieldAndButton("");
            setProductId(productId);
            // Check, ob das Produkt an den Varianten noch stimmt (Bei Neuanlage kann das Produkt nach der Erstellung der Varianten geändert werden)
            checkProductNumber(productId);
            // Nur bei einem existierenden Produkt alle Varianten aus der DB laden
            if (!isForProductCreation) {
                iPartsDataPSKProductVariantList productVariantsList = iPartsDataPSKProductVariantList.loadPSKProductVariants(getProject(), productId);
                productVariantsList.getAsList().forEach(productVariant -> selectedVariants.put(productVariant.getAsId(), productVariant));
            }
            initialSize = selectedVariants.size();
            setText(variantsToString(selectedVariants));
            if (parentForm != null) {
                removeVariantListener();
                // Listener für das Öffnen der Varianten Anzeige
                addVariantListener(parentForm, productId);
            } else {
                removeEventListeners(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT);
            }
        } else {
            setToolTipForFieldAndButton(PRODUCT_NOT_VALID_TOOLTIP);
        }
    }

    @Override
    public boolean isModified() {
        if (selectedVariants.size() != initialSize) {
            return true;
        }
        return !getModifiedVariantsObjects().isEmpty();
    }
}
