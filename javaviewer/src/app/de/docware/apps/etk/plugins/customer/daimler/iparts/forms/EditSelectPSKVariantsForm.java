package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPSKProductVariant;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPSKProductVariantList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsPSKProductVariantId;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

// Formular zur Auswahl von Variantengültigkeiten für PSK
public class EditSelectPSKVariantsForm extends EditSelectDataObjectsForm {

    private iPartsDataPSKProductVariantList availablePSKVariants;
    private Collection<String> initialSelection;
    private Set<String> productNumbers;

    /**
     * Bei mehreren Produkten wird zu jedem Produkt die Varianten im Dialog gezeigt
     *
     * @param parentForm
     * @param availablePSKVariants
     * @param selectedPSKVariantIds
     * @return
     */
    public static Collection<String> showSelectionVariants(AbstractJavaViewerForm parentForm,
                                                           iPartsDataPSKProductVariantList availablePSKVariants,
                                                           Collection<String> selectedPSKVariantIds) {
        EditSelectPSKVariantsForm dlg = new EditSelectPSKVariantsForm(parentForm.getConnector(), parentForm, availablePSKVariants,
                                                                      selectedPSKVariantIds);
        Set<String> productNumbers = dlg.getProductNumbers();
        String productsNumbersString = StrUtils.stringListToString(productNumbers, ", ");
        dlg.setTitle(TranslationHandler.translate(TranslationHandler.translate("!!PSK-Varianten von \"%1\"",
                                                                               productsNumbersString)));

        // Mögliche Varianten an das Grid übergeben
        List<EtkDataObject> availablePSKVariantsForProductList = new ArrayList<>(availablePSKVariants.getAsList());
        dlg.fillAvailableEntries(availablePSKVariantsForProductList);

        // Falls Varianten schon selektiert wurden, dann diese setzen
        if (selectedPSKVariantIds == null) {
            selectedPSKVariantIds = new DwList<>();
        }
        dlg.fillSelectedPSKVariants(selectedPSKVariantIds);

        if (dlg.showModal() == ModalResult.OK) {
            return dlg.getSelectedPSKVariantIds();
        }
        return null;
    }

    /**
     * Erzeugt eine Instanz von EditSelectDataObjectsForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     * @param parentForm
     * @param availablePSKVariants
     * @param selectedPSKVariantIds
     */
    public EditSelectPSKVariantsForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                     iPartsDataPSKProductVariantList availablePSKVariants, Collection<String> selectedPSKVariantIds) {
        super(dataConnector, parentForm, iPartsConst.TABLE_DA_PSK_PRODUCT_VARIANTS, "", "");
        setAvailableEntriesTitle("!!Verfügbare Varianten:");
        setSelectedEntriesTitle("!!Ausgewählte Varianten:");
        setWithDeleteEntry(true);
        setMoveEntriesVisible(false);
        this.availablePSKVariants = availablePSKVariants;
        this.initialSelection = selectedPSKVariantIds;
        productNumbers = new TreeSet<>();
        for (iPartsDataPSKProductVariant pskProductVariant : availablePSKVariants) {
            productNumbers.add(pskProductVariant.getAsId().getProductNumber());
        }
    }

    @Override
    protected EtkDisplayFields buildDefaultDisplayFields(boolean forSelectedEntries) {
        EtkDisplayFields displayFields = new EtkDisplayFields();
        EtkDatabaseTable tableDef = getConfig().getDBDescription().findTable(searchTable);
        displayFields.addFeld(createDisplayField(tableDef, iPartsConst.FIELD_DPPV_VARIANT_ID));
        displayFields.addFeld(createDisplayField(tableDef, iPartsConst.FIELD_DPPV_NAME1));
        displayFields.loadStandards(getConfig());
        return displayFields;
    }

    public void fillSelectedPSKVariants(Collection<String> selectedPSKVariantsIds) {
        List<EtkDataObject> selectedList = new DwList<>();
        for (String variantId : selectedPSKVariantsIds) {
            // Falls es mehrere Produkte gibt, muss das passende für die VariantenIDs gesucht werden.
            // Der erst beste Treffer passt. Wir kommen dann nämlich aus dem Filterdialog und es wird nur
            // die Varianten ID gebraucht -> Das Produkt ist egal
            iPartsDataPSKProductVariant foundProductVariant = null;
            for (iPartsDataPSKProductVariant productVariant : availablePSKVariants) {
                iPartsPSKProductVariantId productVariantId = productVariant.getAsId();
                if (variantId.equals(productVariantId.getVariantId())) {
                    foundProductVariant = productVariant;
                    break;
                }
            }
            if (foundProductVariant != null) {
                selectedList.add(foundProductVariant);
            }
        }
        doAddEntries(selectedList);
        doEnableOKButton();
    }

    public Collection<String> getSelectedPSKVariantIds() {
        List<String> resultList = new DwList<>();
        List<EtkDataObject> list = getCompleteSelectedList();
        if (list != null) {
            for (EtkDataObject dataObject : list) {
                if (dataObject instanceof iPartsDataPSKProductVariant) {
                    resultList.add(((iPartsDataPSKProductVariant)dataObject).getAsId().getVariantId());
                }
            }
        }
        return resultList;
    }

    @Override
    protected boolean areEntriesChanged() {
        Collection<String> actualSelection = getSelectedPSKVariantIds();
        if (actualSelection.size() != initialSelection.size()) {
            return true;
        } else {
            return !initialSelection.containsAll(actualSelection);
        }
    }

    public Set<String> getProductNumbers() {
        return productNumbers;
    }
}
