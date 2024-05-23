/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuListItem;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiComboBox;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogButtons;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogIcon;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.responsive.base.theme.ThemeManager;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.collections.dwlist.DwList;

import java.util.List;

/**
 * Dialog für die Bestimmung einer neuen Modul Nummer und Bezeichnung, sowie Typ und Verortung (2. Entwurd)
 */
public class EditModulCreateForm extends AbstractJavaViewerForm {

    private EditProductSelectDialog productDialog;
    private EditEinPas2Dialog einPas2Dialog;
    private EditKGTUDialog kgTuDialog;
    private boolean closeDlgIfModuleNoChanged = false; //Dialog schließen, falls neue Modul Nummer berechnet wird
    private boolean selectModuleType;   // true, wenn die Modultypauswahl angezeigt werden soll
    private boolean isModuleNoValid;   // true, wenn die Modultypauswahl angezeigt werden soll

    /**
     * Erzeugt eine Instanz von EditModulCreateForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditModulCreateForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                               iPartsProductId productId, EinPasId einPasId, KgTuListItem kgTuListItem, boolean selectModuleType) {
        super(dataConnector, parentForm);

        this.selectModuleType = selectModuleType;

        $$internalCreateGui$$(null);
        postCreateGui(productId, einPasId, kgTuListItem);
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui(iPartsProductId productId, EinPasId einPasId, KgTuListItem kgTuListItem) {
        mainWindow.buttonpanel.setButtonModalResult(GuiButtonOnPanel.ButtonType.OK, ModalResult.NONE);

        // Produkt-Dialog aufschnappen
        mainWindow.panelProduct.removeAllChildren();
        mainWindow.panelProduct.setTitle("");
        productDialog = new EditProductSelectDialog(getConnector(), this, null);
        AbstractGuiControl productGui = productDialog.getProductPanel();
        productGui.removeFromParent();
        ConstraintsBorder guiConstraints = new ConstraintsBorder();
        guiConstraints.setPosition(ConstraintsBorder.POSITION_CENTER);
        productGui.setConstraints(guiConstraints);
        mainWindow.panelProduct.setLayout(new LayoutBorder());
        mainWindow.panelProduct.addChild(productGui);
        EventListener onProductChangeListener = new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                onProductChange(event);
            }
        };
        productDialog.setProductId(productId);
        productDialog.setOnProductChangeListener(onProductChangeListener);

        if (selectModuleType) {
            fillModuleTypeCombo();
        } else {
            mainWindow.combobox_ModulTyp.setVisible(false);
            mainWindow.label_ModulTyp.setVisible(false);
        }

        EtkMultiSprache tuName;
        iPartsConst.PRODUCT_STRUCTURING_TYPE productType = iPartsConst.PRODUCT_STRUCTURING_TYPE.EINPAS;
        if (kgTuListItem == null) {
            if (einPasId == null) {
                productType = iPartsProduct.getInstance(getProject(), productId).getProductStructuringType();
            }
        } else {
            productType = iPartsConst.PRODUCT_STRUCTURING_TYPE.KG_TU;
        }
        if (productType == iPartsConst.PRODUCT_STRUCTURING_TYPE.EINPAS) {
            tuName = createAndInsertEinPAS(einPasId);
        } else {
            tuName = createAndInsertKgTu(productId, kgTuListItem);
        }
        fillModuleName(tuName);

        fillModuleNo();
        mainWindow.multilangedit_Name.setStartLanguage(getProject().getDBLanguage());

        if (iPartsConst.ONLY_SINGLE_MODULE_PER_KGTU) {
            mainWindow.textfield_ModulNo.setEditable(false);
        }
        enableButtons();
        ThemeManager.get().render(mainWindow);
        mainWindow.pack();
    }

    private EtkMultiSprache createAndInsertKgTu(iPartsProductId productId, KgTuListItem kgTuListItem) {
        ConstraintsBorder guiConstraints;
        EtkMultiSprache tuName;//KgTuTemplate-Dialog als ReadOnly mit Zusatz-Button aufschnappen
        mainWindow.panel_EinPAS.removeAllChildren();
        kgTuDialog = new EditKGTUDialog(getConnector(), this, productId);
        kgTuDialog.setStartLanguage(getProject().getDBLanguage());
        AbstractGuiControl gui = kgTuDialog.getGui();
        guiConstraints = new ConstraintsBorder();
        guiConstraints.setPosition(ConstraintsBorder.POSITION_CENTER);
        gui.setConstraints(guiConstraints);
        mainWindow.panel_EinPAS.addChild(gui);
        mainWindow.panel_EinPAS.setMinimumHeight(80);
        kgTuDialog.setKgTuForReadOnlyFromListItem(kgTuListItem);
        kgTuDialog.setPanelTitle("!!KG-TU Struktur");
        kgTuDialog.setButtonKGTUChangeListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                onKgTuChange(event);
            }
        });
        tuName = kgTuDialog.getTUDescription();
        einPas2Dialog = null;
        return tuName;
    }

    private EtkMultiSprache createAndInsertEinPAS(EinPasId einPasId) {
        ConstraintsBorder guiConstraints;
        EtkMultiSprache tuName;//EinPAS-Dialog als ReadOnly mit Zusatz-Button aufschnappen
        mainWindow.panel_EinPAS.removeAllChildren();
        einPas2Dialog = new EditEinPas2Dialog(getConnector(), this, einPasId);
        einPas2Dialog.setStartLanguage(getProject().getDBLanguage());
        AbstractGuiControl gui = einPas2Dialog.getGui();
        guiConstraints = new ConstraintsBorder();
        guiConstraints.setPosition(ConstraintsBorder.POSITION_CENTER);
        gui.setConstraints(guiConstraints);
        mainWindow.panel_EinPAS.addChild(gui);
        mainWindow.panel_EinPAS.setMinimumHeight(120);
        einPas2Dialog.setReadOnly(true);
        einPas2Dialog.setPanelTitle("!!EinPAS Struktur");
        einPas2Dialog.setButtonEinPASChangeListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                onEinPASChange(event);
            }
        });
        tuName = einPas2Dialog.getTUDescription();
        kgTuDialog = null;
        return tuName;
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
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

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public iPartsProductId getProductId() {
        return productDialog.getProductId();
    }

    public String getModuleNumber() {
        return mainWindow.textfield_ModulNo.getText();
    }

    public String getModuleVersion() {
        return "";
    }

    public EtkMultiSprache getModuleName() {
        // Benennung soll nicht editiert werden können (DAIMLER-5901)
        return mainWindow.multilangedit_Name.getMultiLanguage().cloneMe();
    }

    public iPartsConst.PRODUCT_STRUCTURING_TYPE getType() {
        if (einPas2Dialog == null) {
            return iPartsConst.PRODUCT_STRUCTURING_TYPE.KG_TU;
        }
        return iPartsConst.PRODUCT_STRUCTURING_TYPE.EINPAS;
    }

    public boolean isEinPAS() {
        return getType() == iPartsConst.PRODUCT_STRUCTURING_TYPE.EINPAS;
    }

    public boolean isKG_TU() {
        return getType() == iPartsConst.PRODUCT_STRUCTURING_TYPE.KG_TU;
    }

    public EinPasId getEinPasId() {
        if (isEinPAS()) {
            return einPas2Dialog.getEinPasId();
        }
        return null;
    }

    public KgTuId getKgTuId() {
        if (isKG_TU()) {
            return kgTuDialog.getKgTuId();
        }
        return null;
    }

    public KgTuListItem getKgTuNode() {
        if (isKG_TU()) {
            return kgTuDialog.getKgTuNode();
        }
        return null;
    }

    public iPartsModuleTypes getModuleType() {
        return getSelectedModuleType();
    }

    /**
     * ModuleTyp Combobox füllen
     */
    private void fillModuleTypeCombo() {
        mainWindow.combobox_ModulTyp.switchOffEventListeners();
        mainWindow.combobox_ModulTyp.removeAllItems();
        mainWindow.combobox_ModulTyp.addItem(iPartsModuleTypes.DialogRetail, iPartsModuleTypes.DialogRetail.getDescription());
        mainWindow.combobox_ModulTyp.addItem(iPartsModuleTypes.EDSRetail, iPartsModuleTypes.EDSRetail.getDescription());

        // PSK Doku-Methoden bei PSK-Produkten hinzufügen
        iPartsProductId productId = productDialog.getProductId();
        if ((productId != null) && iPartsRight.checkPSKInSession() && iPartsProduct.getInstance(getProject(), productId).isPSK()) {
            mainWindow.combobox_ModulTyp.addItem(iPartsModuleTypes.PSK_PKW, iPartsModuleTypes.PSK_PKW.getDescription());
            mainWindow.combobox_ModulTyp.addItem(iPartsModuleTypes.PSK_TRUCK, iPartsModuleTypes.PSK_TRUCK.getDescription());
        }

        mainWindow.combobox_ModulTyp.setSelectedIndex(-1);
        mainWindow.combobox_ModulTyp.switchOnEventListeners();
    }

    /**
     * Modulname setzen (in der Regel die TU-Description)
     *
     * @param multiName
     */
    private void fillModuleName(EtkMultiSprache multiName) {
        List<String> dbLanguages = getProject().getConfig().getDatabaseLanguages();
        if (multiName == null) {
            multiName = new EtkMultiSprache("", dbLanguages);
        } else {
            multiName.completeWithLanguages(dbLanguages);
        }
        mainWindow.multilangedit_Name.setMultiLanguage(multiName);
    }

    /**
     * Modul Nummer bilden und setzen
     */
    private void fillModuleNo() {
        String modulNo;
        isModuleNoValid = true;
        if (isEinPAS()) {
            modulNo = EditModuleHelper.buildEinPasModuleNumber(getProductId(), getEinPasId(), getProject());
        } else {
            if (iPartsConst.ONLY_SINGLE_MODULE_PER_KGTU) {
                modulNo = EditModuleHelper.createStandardModuleName(getProductId(), getKgTuId());
                if ((getKgTuId() != null) && getKgTuId().isTuNode()) {
                    if (existsModule(modulNo)) {
                        modulNo = "<" + TranslationHandler.translate("!!TU \"%1\" existiert bereits!", modulNo) + ">";
                        isModuleNoValid = false;
                    } else {
                        if (EditModuleHelper.isStandardModuleInReservedPK(getProject(), getProductId(), getKgTuId(), true, null)) {
                            modulNo = "<" + TranslationHandler.translate("!!TU \"%1\" ist bereits in einem Autoren-Auftrag angelegt worden!", modulNo) + ">";
                            isModuleNoValid = false;
                        }
                    }
                }
            } else {
                modulNo = EditModuleHelper.buildKgTuModuleNumber(getProductId(), getKgTuId(), getProject());
            }
        }

        mainWindow.textfield_ModulNo.setText(modulNo);
    }


    /**
     * liefert den selektierten ModulTyp
     *
     * @return
     */
    private iPartsModuleTypes getSelectedModuleType() {
        return getSelectedUserObject(mainWindow.combobox_ModulTyp);
    }

    /**
     * Setzt den gewünschten {@link iPartsModuleTypes} in der Auswahl-ComboBox.
     *
     * @param moduleType
     */
    public void setSelectedModuleType(iPartsModuleTypes moduleType) {
        mainWindow.combobox_ModulTyp.setSelectedUserObject(moduleType);
    }

    private iPartsModuleTypes getSelectedUserObject(GuiComboBox combobox) {
        int index = combobox.getSelectedIndex();
        if (index != -1) {
            return (iPartsModuleTypes)combobox.getUserObject(index);
        }
        return iPartsModuleTypes.UNKNOWN;
    }

    /**
     * Abfrage, ob Modul mit dieser Nummer bereits existiert
     *
     * @param moduleNumber
     * @return
     */
    private boolean existsModule(String moduleNumber) {
        boolean result = getProject().getDB().getRecordExists(iPartsConst.TABLE_DA_MODULE, new String[]{ iPartsConst.FIELD_DM_MODULE_NO },
                                                              new String[]{ moduleNumber });
        return result;
//      fürs Debugging
//        return getProject().getDB().getRecordExists(iPartsConst.TABLE_DA_MODULE, new String[]{ iPartsConst.FIELD_DM_MODULE_NO },
//                                                    new String[]{ moduleNumber });
    }

    /**
     * Überprüfung, ob die Modulnummer mit der berechneten übereinstimmt
     *
     * @param moduleNo
     * @return
     */
    private boolean isStandardModuleNo(String moduleNo) {
        int serial = EditModuleHelper.extractModuleNumberSerial(moduleNo);
        String newModuleName;
        if (isEinPAS()) {
            newModuleName = EditModuleHelper.buildEinPasModuleNumberWithoutSerial(getProductId(), getEinPasId());
        } else {
            newModuleName = EditModuleHelper.buildKgTuModuleNumberWithoutSerial(getProductId(), getKgTuId());
        }
        if (serial >= 0) {
            newModuleName = newModuleName + EditModuleHelper.IPARTS_MODULE_NAME_DELIMITER + EditModuleHelper.formatModuleSerialNumber(serial);
        }
        return newModuleName.equals(moduleNo);
    }

    /**
     * Handling des OK-Buttons
     */
    private void enableButtons() {
        List<String> warnings = new DwList<String>();
        boolean isOK = checkData(warnings);
        String hint = "";
        if (!isOK) {
            hint = TranslationHandler.translate("!!Fehler:");
            hint += "\n";
            for (String msg : warnings) {
                hint += "\n";
                hint += TranslationHandler.translate(msg);
            }
        }
        mainWindow.buttonpanel.setTooltip(hint);
        mainWindow.buttonpanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, isOK);
    }


    /**
     * Callback, wenn sich Produkt geändert hat
     * Besetzen der notwendigen Felder
     *
     * @param event
     */
    private void onProductChange(Event event) {
        if (productDialog.isProductIdValid()) {
            iPartsProductId productId = productDialog.getProductId();
            iPartsProduct product = iPartsProduct.getInstance(getProject(), productId);
            iPartsConst.PRODUCT_STRUCTURING_TYPE productType = product.getProductStructuringType();
            if (productType != getType()) {
                if (productType == iPartsConst.PRODUCT_STRUCTURING_TYPE.EINPAS) {
                    createAndInsertEinPAS(null);
                } else {
                    createAndInsertKgTu(productId, null);
                }
            }
            fillModuleNo();
        }
        enableButtons();
    }

    /**
     * Callback, wenn sich die EinPasId geändert hat
     * Bestzen der notwendigen Felder
     *
     * @param event
     */
    private void onEinPASChange(Event event) {
        EinPasId einPasId = EditEinPas2Dialog.showEinPasDialog(getConnector(), this, getEinPasId());
        if (einPasId != null) {
            einPas2Dialog.setStartEinPasId(einPasId);
            EtkMultiSprache tuName = einPas2Dialog.getTUDescription();
            fillModuleName(tuName);
            fillModuleNo();
            enableButtons();
        }
    }

    /**
     * Callback, wenn sich die EinPasId geändert hat
     * Bestzen der notwendigen Felder
     *
     * @param event
     */
    private void onKgTuChange(Event event) {
        KgTuListItem kgTuId = EditKGTUDialog.showKgTuDialog(getConnector(), this, getProductId(), kgTuDialog.getKgTuNode() /*getKgTuId()*/);
        if (kgTuId != null) {
            kgTuDialog.setKgTuForReadOnlyFromListItem(kgTuId);
            EtkMultiSprache tuName = kgTuDialog.getTUDescription();
            fillModuleName(tuName);
            fillModuleNo();
            enableButtons();
        }
    }

    /**
     * Callback, wenn sich der Modulname geändert hat
     *
     * @param event
     */
    private void onChangeEvent(Event event) {
        enableButtons();
    }

    /**
     * Cancel Klick
     *
     * @param event
     */
    private void onCancelClick(Event event) {
        close();
    }

    /**
     * Überprüfung der eingegebenen Daten
     *
     * @param warnings
     * @return
     */
    private boolean checkData(List<String> warnings) {
        if (warnings != null) {
            if (isEinPAS()) {
                if (getEinPasId() == null) {
                    warnings.add("!!EinPAS Verortung ist nicht gesetzt");
                    return false;
                }
            } else {
                if (getKgTuId() == null) {
                    warnings.add("!!KG/TU Verortung ist nicht gesetzt");
                    return false;
                }
            }
        }
        if (getProductId().isEmpty()) {
            if (warnings != null) {
                warnings.add("!!Produkt ist nicht gesetzt");
            }
            return false;
        }
        if (mainWindow.textfield_ModulNo.getText().isEmpty()) {
            if (warnings != null) {
                warnings.add(TranslationHandler.translate("!!%1 ist nicht gesetzt", TranslationHandler.translate("!!Modulnummer")));
            }
            return false;
        }
        if (mainWindow.multilangedit_Name.getMultiLanguage().allStringsAreEmpty()) {
            if (warnings != null) {
                warnings.add("!!Modulname ist nicht gesetzt");
            }
            return false;
        }
        if (selectModuleType) {
            if (getSelectedModuleType() == iPartsModuleTypes.UNKNOWN) {
                if (warnings != null) {
                    warnings.add("!!Modul-Typ ist nicht gesetzt");
                }
                return false;
            }
        }
        if (!isModuleNoValid) {
            if (warnings != null) {
                warnings.add(mainWindow.textfield_ModulNo.getText());
            }
            return false;
        }

        return true;
    }

    /**
     * OK Klick
     *
     * @param event
     */
    private void onOKClick(Event event) {
        List<String> warnings = new DwList<String>();
        boolean doContinue = checkData(warnings);

        if (!doContinue) {
            // Fehleranzeige
            if ((getEinPasId() == null) || (getKgTuId() == null)) {
                if (MessageDialog.show(TranslationHandler.translate(warnings.get(0)) + '\n'
                                       + TranslationHandler.translate("!!Wollen Sie das Modul trotzdem anlegen?"), "!!Anlegen", MessageDialogIcon.CONFIRMATION,
                                       MessageDialogButtons.YES, MessageDialogButtons.NO) == ModalResult.YES) {
                    doContinue = true;
                }
            } else {
                MessageDialog.showWarning(warnings);
            }
        }

        if (doContinue) {
            // Überprüfung ob Modul bereits existiert
            String moduleNumber = getModuleNumber();
            if (existsModule(moduleNumber)) {
                String msg = TranslationHandler.translate("!!Die Modulnummer existiert bereits:") + " '" + moduleNumber + "'" + '\n';
                if (!isStandardModuleNo(moduleNumber)) {
                    msg = msg + TranslationHandler.translate("!!Modulnummer entspricht nicht den Erzeugungsregeln!") + '\n';
                }
                msg = msg + TranslationHandler.translate("!!Soll eine neue Modulnummer erzeugt werden?");
                if (MessageDialog.show(msg, "!!Anlegen", MessageDialogIcon.CONFIRMATION,
                                       MessageDialogButtons.YES, MessageDialogButtons.NO) == ModalResult.YES) {
                    fillModuleNo();
                    if (closeDlgIfModuleNoChanged) {
                        mainWindow.setModalResult(ModalResult.OK);
                        close();
                    } else {
                        enableButtons();
                    }
                }
            } else {
                mainWindow.setModalResult(ModalResult.OK);
                close();
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
        private de.docware.framework.modules.gui.controls.GuiPanel panel_Modul;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_ModulTyp;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes> combobox_ModulTyp;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_ModulNo;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfield_ModulNo;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_Name;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiMultiLangEdit multilangedit_Name;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_EinPAS;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setWidth(600);
            this.setHeight(460);
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
            panelMain.setPaddingTop(8);
            panelMain.setPaddingLeft(8);
            panelMain.setPaddingRight(8);
            panelMain.setPaddingBottom(8);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelMain.setLayout(panelMainLayout);
            panelProduct = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelProduct.setName("panelProduct");
            panelProduct.__internal_setGenerationDpi(96);
            panelProduct.registerTranslationHandler(translationHandler);
            panelProduct.setScaleForResolution(true);
            panelProduct.setMinimumWidth(10);
            panelProduct.setMinimumHeight(10);
            panelProduct.setTitle("!!Produkt");
            de.docware.framework.modules.gui.layout.LayoutGridBag panelProductLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelProduct.setLayout(panelProductLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelProductConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "c", "h", 0, 0, 4, 0);
            panelProduct.setConstraints(panelProductConstraints);
            panelMain.addChild(panelProduct);
            panel_Modul = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_Modul.setName("panel_Modul");
            panel_Modul.__internal_setGenerationDpi(96);
            panel_Modul.registerTranslationHandler(translationHandler);
            panel_Modul.setScaleForResolution(true);
            panel_Modul.setMinimumWidth(10);
            panel_Modul.setMinimumHeight(10);
            panel_Modul.setTitle("!!Modul");
            de.docware.framework.modules.gui.layout.LayoutGridBag panel_ModulLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panel_ModulLayout.setCentered(false);
            panel_Modul.setLayout(panel_ModulLayout);
            label_ModulTyp = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_ModulTyp.setName("label_ModulTyp");
            label_ModulTyp.__internal_setGenerationDpi(96);
            label_ModulTyp.registerTranslationHandler(translationHandler);
            label_ModulTyp.setScaleForResolution(true);
            label_ModulTyp.setMinimumWidth(10);
            label_ModulTyp.setMinimumHeight(10);
            label_ModulTyp.setText("!!Modul-Typ");
            label_ModulTyp.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_ModulTypConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "e", "n", 8, 8, 4, 4);
            label_ModulTyp.setConstraints(label_ModulTypConstraints);
            panel_Modul.addChild(label_ModulTyp);
            combobox_ModulTyp = new de.docware.framework.modules.gui.controls.GuiComboBox<de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes>();
            combobox_ModulTyp.setName("combobox_ModulTyp");
            combobox_ModulTyp.__internal_setGenerationDpi(96);
            combobox_ModulTyp.registerTranslationHandler(translationHandler);
            combobox_ModulTyp.setScaleForResolution(true);
            combobox_ModulTyp.setMinimumWidth(10);
            combobox_ModulTyp.setMinimumHeight(10);
            combobox_ModulTyp.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onChangeEvent(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag combobox_ModulTypConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 100.0, 0.0, "c", "h", 8, 4, 4, 8);
            combobox_ModulTyp.setConstraints(combobox_ModulTypConstraints);
            panel_Modul.addChild(combobox_ModulTyp);
            label_ModulNo = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_ModulNo.setName("label_ModulNo");
            label_ModulNo.__internal_setGenerationDpi(96);
            label_ModulNo.registerTranslationHandler(translationHandler);
            label_ModulNo.setScaleForResolution(true);
            label_ModulNo.setMinimumWidth(110);
            label_ModulNo.setMinimumHeight(10);
            label_ModulNo.setText("!!Modulnummer");
            label_ModulNo.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_ModulNoConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "e", "n", 4, 8, 4, 4);
            label_ModulNo.setConstraints(label_ModulNoConstraints);
            panel_Modul.addChild(label_ModulNo);
            textfield_ModulNo = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfield_ModulNo.setName("textfield_ModulNo");
            textfield_ModulNo.__internal_setGenerationDpi(96);
            textfield_ModulNo.registerTranslationHandler(translationHandler);
            textfield_ModulNo.setScaleForResolution(true);
            textfield_ModulNo.setMinimumWidth(200);
            textfield_ModulNo.setMinimumHeight(10);
            textfield_ModulNo.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onChangeEvent(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfield_ModulNoConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 1, 1, 1, 100.0, 0.0, "c", "h", 4, 4, 4, 8);
            textfield_ModulNo.setConstraints(textfield_ModulNoConstraints);
            panel_Modul.addChild(textfield_ModulNo);
            label_Name = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_Name.setName("label_Name");
            label_Name.__internal_setGenerationDpi(96);
            label_Name.registerTranslationHandler(translationHandler);
            label_Name.setScaleForResolution(true);
            label_Name.setMinimumWidth(10);
            label_Name.setMinimumHeight(10);
            label_Name.setText("!!Benennung");
            label_Name.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_NameConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 0.0, 0.0, "e", "n", 4, 8, 8, 4);
            label_Name.setConstraints(label_NameConstraints);
            panel_Modul.addChild(label_Name);
            multilangedit_Name = new de.docware.framework.modules.gui.controls.GuiMultiLangEdit();
            multilangedit_Name.setName("multilangedit_Name");
            multilangedit_Name.__internal_setGenerationDpi(96);
            multilangedit_Name.registerTranslationHandler(translationHandler);
            multilangedit_Name.setScaleForResolution(true);
            multilangedit_Name.setMinimumWidth(10);
            multilangedit_Name.setMinimumHeight(10);
            multilangedit_Name.setBackgroundColor(new java.awt.Color(255, 255, 255, 0));
            multilangedit_Name.setReadOnly(true);
            multilangedit_Name.setExtendToFullHeight(true);
            multilangedit_Name.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onChangeEvent(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag multilangedit_NameConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 2, 1, 1, 100.0, 0.0, "c", "h", 4, 4, 8, 8);
            multilangedit_Name.setConstraints(multilangedit_NameConstraints);
            panel_Modul.addChild(multilangedit_Name);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panel_ModulConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 0.0, 0.0, "c", "h", 4, 0, 0, 0);
            panel_Modul.setConstraints(panel_ModulConstraints);
            panelMain.addChild(panel_Modul);
            panel_EinPAS = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_EinPAS.setName("panel_EinPAS");
            panel_EinPAS.__internal_setGenerationDpi(96);
            panel_EinPAS.registerTranslationHandler(translationHandler);
            panel_EinPAS.setScaleForResolution(true);
            panel_EinPAS.setMinimumWidth(10);
            panel_EinPAS.setMinimumHeight(120);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_EinPASLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_EinPAS.setLayout(panel_EinPASLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panel_EinPASConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "c", "h", 0, 0, 0, 0);
            panel_EinPAS.setConstraints(panel_EinPASConstraints);
            panelMain.addChild(panel_EinPAS);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMainConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMain.setConstraints(panelMainConstraints);
            this.addChild(panelMain);
            buttonpanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonpanel.setName("buttonpanel");
            buttonpanel.__internal_setGenerationDpi(96);
            buttonpanel.registerTranslationHandler(translationHandler);
            buttonpanel.setScaleForResolution(true);
            buttonpanel.setMinimumWidth(10);
            buttonpanel.setMinimumHeight(10);
            buttonpanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonOKActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onOKClick(event);
                }
            });
            buttonpanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonCancelActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onCancelClick(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder buttonpanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            buttonpanelConstraints.setPosition("south");
            buttonpanel.setConstraints(buttonpanelConstraints);
            this.addChild(buttonpanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}