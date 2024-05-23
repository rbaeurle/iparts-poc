/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.searchgrids.SelectSearchGridProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.responsive.base.theme.ThemeManager;
import de.docware.framework.utils.EtkMultiSprache;

/**
 * Dialog zur Eingabe einer SeriesId (Baureihe/Produkt)
 */
public class EditProductSelectDialog extends AbstractJavaViewerForm {

    private iPartsProductId productId;
    private boolean isEditable;
    private EventListener onProductChange = null;

    public static iPartsProductId showProductDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                    iPartsProductId productId, boolean isEditable) {
        EditProductSelectDialog seriesDlg = new EditProductSelectDialog(dataConnector, parentForm, productId);
        seriesDlg.setEditable(isEditable);
        String title = "!!Produkt festlegen";
        if (!isEditable) {
            title = "!!Produkt anzeigen";
        }
        seriesDlg.setTitle(title);

        if (seriesDlg.showModal() == ModalResult.OK) {
            return seriesDlg.getProductId();
        }
        return null;

    }

    /**
     * Erzeugt eine Instanz von EditProductSelectDialog.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditProductSelectDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, iPartsProductId productId) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.productId = productId;
        this.isEditable = true;
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        fillProduct();
        enableButtons();
        ThemeManager.get().render(mainWindow);
        mainWindow.buttonProductChange.setMaximumWidth(mainWindow.textfieldProductNo.getPreferredHeight());
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public GuiPanel getProductPanel() {
        return mainWindow.panelProduct;
    }

    public ModalResult showModal() {
        ModalResult modalResult = mainWindow.showModal();
        super.close();
        return modalResult;
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }

    @Override
    public EditModuleFormIConnector getConnector() {
        return (EditModuleFormIConnector)super.getConnector();
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    /**
     * zusätzliche Überschrift auf dem Produkt-Panel
     *
     * @param title
     */
    public void setPanelTitle(String title) {
        mainWindow.panelProduct.setTitle(title);
    }

    public iPartsProductId getProductId() {
        return productId;
    }

    public void setProductId(iPartsProductId productId) {
        this.productId = productId;
        fillProduct();
        enableButtons();
    }

    public boolean isProductIdValid() {
        return (productId != null) && !productId.isEmpty();
    }

    public String getProductName(String language) {
        return mainWindow.multilangeditProductName.getMultiLanguage().getText(language);
    }

    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean editable) {
        if (isEditable == editable) {
            return;
        }
        mainWindow.buttonProductChange.setVisible(editable);
        mainWindow.buttonPanel.setButtonVisible(GuiButtonOnPanel.ButtonType.CANCEL, editable);

        if (editable) {
            mainWindow.buttonPanel.setButtonModalResult(GuiButtonOnPanel.ButtonType.OK, ModalResult.OK);
        } else {
            mainWindow.buttonPanel.setButtonModalResult(GuiButtonOnPanel.ButtonType.OK, ModalResult.NONE);
        }
        isEditable = editable;
        enableButtons();
    }

    public EventListener getOnProductChange() {
        return onProductChange;
    }

    public void setOnProductChangeListener(EventListener onProductChange) {
        this.onProductChange = onProductChange;
    }

    private boolean checkData() {
        if (isEditable) {
            return isProductIdValid();
        }
        return true;
    }

    private void enableButtons() {
        boolean isOK = checkData();
        mainWindow.buttonPanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, isOK);
    }

    /**
     * Produkt Felder füllen
     */
    private void fillProduct() {
        if (checkData()) {
            mainWindow.textfieldProductNo.setText(productId.getProductNumber());
            iPartsProduct product = iPartsProduct.getInstance(getProject(), productId);
            EtkMultiSprache multi = product.getProductTitle(getProject());
            mainWindow.multilangeditProductName.setMultiLanguage(multi);
//            mainWindow.multilangeditProductName.setMultiLanguage(product.getProductTitle(getProject()));
        } else {
            mainWindow.textfieldProductNo.setText("");
        }
        mainWindow.multilangeditProductName.setStartLanguage(getProject().getDBLanguage());
    }

    /**
     * Callback, wenn sich Produkt geändert hat
     * Bestzen der notwendigen Felder
     *
     * @param event
     */
    private void onProductChange(Event event) {
        SelectSearchGridProduct selectSearchGridProduct = new SelectSearchGridProduct(this);
        String productNo = selectSearchGridProduct.showGridSelectionDialog(null);
        if (!productNo.isEmpty()) {
            setProductId(new iPartsProductId(productNo));
            if (onProductChange != null) {
                onProductChange.fire(event);
            }
        }
    }


    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelProduct;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfieldProductNo;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiMultiLangEdit multilangeditProductName;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton buttonProductChange;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setWidth(600);
            this.setHeight(240);
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
            title = new de.docware.framework.modules.gui.controls.GuiTitle();
            title.setName("title");
            title.__internal_setGenerationDpi(96);
            title.registerTranslationHandler(translationHandler);
            title.setScaleForResolution(true);
            title.setMinimumWidth(10);
            title.setMinimumHeight(50);
            title.setTitle("...");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleConstraints.setPosition("north");
            title.setConstraints(titleConstraints);
            this.addChild(title);
            panelMain = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelMain.setName("panelMain");
            panelMain.__internal_setGenerationDpi(96);
            panelMain.registerTranslationHandler(translationHandler);
            panelMain.setScaleForResolution(true);
            panelMain.setMinimumWidth(10);
            panelMain.setMinimumHeight(10);
            panelMain.setPaddingTop(4);
            panelMain.setPaddingLeft(4);
            panelMain.setPaddingRight(4);
            panelMain.setPaddingBottom(4);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelMainLayout.setCentered(false);
            panelMain.setLayout(panelMainLayout);
            panelProduct = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelProduct.setName("panelProduct");
            panelProduct.__internal_setGenerationDpi(96);
            panelProduct.registerTranslationHandler(translationHandler);
            panelProduct.setScaleForResolution(true);
            panelProduct.setMinimumWidth(10);
            panelProduct.setMinimumHeight(10);
            panelProduct.setPaddingRight(8);
            panelProduct.setTitle("!!Produkt");
            de.docware.framework.modules.gui.layout.LayoutGridBag panelProductLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelProductLayout.setCentered(false);
            panelProduct.setLayout(panelProductLayout);
            textfieldProductNo = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfieldProductNo.setName("textfieldProductNo");
            textfieldProductNo.__internal_setGenerationDpi(96);
            textfieldProductNo.registerTranslationHandler(translationHandler);
            textfieldProductNo.setScaleForResolution(true);
            textfieldProductNo.setMinimumWidth(100);
            textfieldProductNo.setMinimumHeight(10);
            textfieldProductNo.setBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignBackground"));
            textfieldProductNo.setText("C204");
            textfieldProductNo.setEditable(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfieldProductNoConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "e", "v", 4, 8, 8, 4);
            textfieldProductNo.setConstraints(textfieldProductNoConstraints);
            panelProduct.addChild(textfieldProductNo);
            multilangeditProductName = new de.docware.framework.modules.gui.controls.GuiMultiLangEdit();
            multilangeditProductName.setName("multilangeditProductName");
            multilangeditProductName.__internal_setGenerationDpi(96);
            multilangeditProductName.registerTranslationHandler(translationHandler);
            multilangeditProductName.setScaleForResolution(true);
            multilangeditProductName.setMinimumWidth(10);
            multilangeditProductName.setMinimumHeight(10);
            multilangeditProductName.setBackgroundColor(new java.awt.Color(255, 255, 255, 0));
            multilangeditProductName.setReadOnly(true);
            multilangeditProductName.setExtendToFullHeight(true);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag multilangeditProductNameConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 100.0, 0.0, "c", "b", 4, 4, 8, 0);
            multilangeditProductName.setConstraints(multilangeditProductNameConstraints);
            panelProduct.addChild(multilangeditProductName);
            buttonProductChange = new de.docware.framework.modules.gui.controls.GuiButton();
            buttonProductChange.setName("buttonProductChange");
            buttonProductChange.__internal_setGenerationDpi(96);
            buttonProductChange.registerTranslationHandler(translationHandler);
            buttonProductChange.setScaleForResolution(true);
            buttonProductChange.setMinimumWidth(5);
            buttonProductChange.setMinimumHeight(10);
            buttonProductChange.setMaximumWidth(30);
            buttonProductChange.setMnemonicEnabled(true);
            buttonProductChange.setText("...");
            buttonProductChange.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onProductChange(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag buttonProductChangeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(2, 0, 1, 1, 0.0, 100.0, "w", "v", 4, 0, 8, 0);
            buttonProductChange.setConstraints(buttonProductChangeConstraints);
            panelProduct.addChild(buttonProductChange);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelProductConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "n", "h", 0, 0, 0, 0);
            panelProduct.setConstraints(panelProductConstraints);
            panelMain.addChild(panelProduct);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMainConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMain.setConstraints(panelMainConstraints);
            this.addChild(panelMain);
            buttonPanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonPanel.setName("buttonPanel");
            buttonPanel.__internal_setGenerationDpi(96);
            buttonPanel.registerTranslationHandler(translationHandler);
            buttonPanel.setScaleForResolution(true);
            buttonPanel.setMinimumWidth(10);
            buttonPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder buttonPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            buttonPanelConstraints.setPosition("south");
            buttonPanel.setConstraints(buttonPanelConstraints);
            this.addChild(buttonPanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}