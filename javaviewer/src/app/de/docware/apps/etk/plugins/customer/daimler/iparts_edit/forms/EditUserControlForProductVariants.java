/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPSKProductVariant;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsPSKProductVariantId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.util.StrUtils;

import java.util.Set;

/**
 * EditControl zum Anlegen und Bearbeiten von Varianten zu einem Produkt
 */
public class EditUserControlForProductVariants extends EditUserControlForCreate implements iPartsConst {

    /**
     * Editiert die übergeben Varianten zum Produkt {@link iPartsDataPSKProductVariant}
     *
     * @param dataConnector
     * @param parentForm
     * @param productVariant
     * @param existingVariantIds
     * @return
     */
    public static boolean editProductVariant(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                             iPartsDataPSKProductVariant productVariant, Set<String> existingVariantIds) {
        if (iPartsRight.checkPSKInSession()) {
            if (productVariant != null) {
                EditUserControlForProductVariants userControl = createProductVariantControl(dataConnector, parentForm, productVariant, existingVariantIds, false);
                userControl.setTitle("!!Produktvariante bearbeiten");
                // Varianten-ID darf nicht mehr editiert werden
                EditControl variantIdEditControl = userControl.getEditControlByFieldName(iPartsConst.FIELD_DPPV_VARIANT_ID);
                if (variantIdEditControl != null) {
                    variantIdEditControl.getEditControl().setReadOnly(true);
                }
                return showProductVariant(userControl, productVariant) != null;
            }
        }
        return false;
    }

    /**
     * Erzeugt eine neuen Variante {@link iPartsDataPSKProductVariant} zum übergebenen Produkt
     *
     * @param dataConnector
     * @param parentForm
     * @param productId
     * @param existingVariantIds
     * @return
     */
    public static iPartsDataPSKProductVariant createNewProductVariant(AbstractJavaViewerFormIConnector dataConnector,
                                                                      AbstractJavaViewerForm parentForm,
                                                                      iPartsProductId productId, Set<String> existingVariantIds) {
        if (iPartsRight.checkPSKInSession()) {
            iPartsDataPSKProductVariant result = createNewProductVariantDataObject(dataConnector, productId);
            EditUserControlForProductVariants userControl = createProductVariantControl(dataConnector, parentForm, result,
                                                                                        existingVariantIds, true);
            userControl.setTitle("!!Produktvariante anlegen");
            return showProductVariant(userControl, result);
        }
        return null;
    }

    /**
     * Zeig den Editor zur übergeben Variante an
     *
     * @param userControl
     * @param productVariant
     * @return
     */
    private static iPartsDataPSKProductVariant showProductVariant(EditUserControlForProductVariants userControl,
                                                                  iPartsDataPSKProductVariant productVariant) {
        if (userControl.showModal() == ModalResult.OK) {
            productVariant.setAttributes(userControl.getAttributes(), true, true, DBActionOrigin.FROM_EDIT);
            productVariant.updateIdFromPrimaryKeys();
            return productVariant;
        }
        return null;
    }

    /**
     * Erzeugt einen Editor für die übergebene Variante zum Produkt
     *
     * @param dataConnector
     * @param parentForm
     * @param productVariant
     * @param existingVariantIds
     * @return
     */
    private static EditUserControlForProductVariants createProductVariantControl(AbstractJavaViewerFormIConnector dataConnector,
                                                                                 AbstractJavaViewerForm parentForm,
                                                                                 iPartsDataPSKProductVariant productVariant,
                                                                                 Set<String> existingVariantIds, boolean isNEWForm) {
        EditUserControlForProductVariants userControl = new EditUserControlForProductVariants(dataConnector, parentForm,
                                                                                              productVariant, existingVariantIds, isNEWForm);
        userControl.setWindowName("!!Variante zu Produkt");
        return userControl;
    }

    /**
     * Erzeugt eine neue und nicht befüllte Variante zum Produkt
     *
     * @param dataConnector
     * @param productId
     * @return
     */
    private static iPartsDataPSKProductVariant createNewProductVariantDataObject(AbstractJavaViewerFormIConnector dataConnector,
                                                                                 iPartsProductId productId) {
        iPartsPSKProductVariantId productVariantId = new iPartsPSKProductVariantId(productId.getProductNumber(), "");
        iPartsDataPSKProductVariant result = new iPartsDataPSKProductVariant(dataConnector.getProject(), productVariantId);
        result.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
        return result;
    }

    @Override
    protected void modifyEditFieldsCreatedFromTablename() {
        modifyEditFields(editFields, TABLE_DA_PSK_PRODUCT_VARIANTS, new String[]{ FIELD_DPPV_VARIANT_ID, FIELD_DPPV_NAME1 },
                         null, null, new String[]{ FIELD_DPPV_PRODUCT_NO });
    }

    private final Set<String> existingVariantIds;
    private boolean isNewForm;


    public EditUserControlForProductVariants(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                             iPartsDataPSKProductVariant productVariant, Set<String> existingVariantIds, boolean isNewForm) {
        super(dataConnector, parentForm, TABLE_DA_PSK_PRODUCT_VARIANTS, productVariant.getAsId(),
              productVariant.getAttributes(), null);
        this.existingVariantIds = existingVariantIds;
        this.isNewForm = isNewForm;
    }

    @Override
    protected boolean checkPkValuesForModified() {
        boolean result = super.checkPkValuesForModified();
        // Check, ob die VariantenID (Kennung) schon an einer anderen Varianten existiert
        if ((existingVariantIds != null) && !existingVariantIds.isEmpty()) {
            EditControl control = getEditControlByFieldName(FIELD_DPPV_VARIANT_ID);
            if (control != null) {
                // Beim Bearbeiten ist das Feld nicht editierbar. Hier keine Hintergrundfarbe ändern
                if (isNewForm) {
                    String currentValue = control.getText().trim();
                    if (StrUtils.isValid(currentValue)) {
                        if (existingVariantIds.contains(currentValue)) {
                            control.getAbstractGuiControl().setBackgroundColor(Colors.clDesignErrorBackground);
                            control.getAbstractGuiControl().setTooltip("!!Zum ausgewählten Produkt existiert schon eine Variante mit dieser ID!");
                            result = false;
                        } else {

                            control.getAbstractGuiControl().setBackgroundColor(Colors.clDesignTextFieldEnabledBackground);
                            control.getAbstractGuiControl().setTooltip("");
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    protected boolean isMandatoryAttributeValueEmpty(EtkEditField field, DBDataObjectAttribute attrib) {
        if (field.isMultiLanguage()) {
            return attrib.getMultiLanguageText(getProject().getDBLanguage(), null).isEmpty();
        } else {
            return super.isMandatoryAttributeValueEmpty(field, attrib);
        }
    }

    @Override
    protected void doEnableButtons(Event event) {
        // Prüft, ob Muss-Felder gefüllt sind und kontrolliert farbige Hinterlegung bei der Varianten-ID
        boolean enabled = checkPkValuesForModified();
        if (!isNewForm) {
            // Beim Edit Muss-Felder prüfen, ob sie gefüllt sind und ob sich was geändert hat im Dialog
            enabled = !checkMustFieldsHaveValues();
            if (enabled) {
                enabled = checkForModified();
            }
        }
        enableOKButton(enabled);
    }
}
