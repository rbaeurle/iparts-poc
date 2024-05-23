/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.forms.common.EditControlFactory;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMeta;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictTextKindId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTxtKindIdByMADId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditFormComboboxHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiButton;
import de.docware.framework.modules.gui.controls.GuiTextField;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.misc.DWFontStyle;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListeners;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.framework.modules.gui.misc.resources.FrameworkImage;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.responsive.base.theme.ThemeManager;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;

import java.util.*;

/**
 * Dialog zur Eingabe eines KG/TU-Knotens
 */
public class EditKGTUDialog extends AbstractJavaViewerForm {

    public static KgTuListItem modifyKgTuDescription(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                     iPartsProductId productId, KgTuId kgTuId) {
        // KG oder TU-Node: Title ändern
        EtkProject project = dataConnector.getProject();
        KgTuNode kgTuNode = KgTuForProduct.getInstance(project, productId).getNode(kgTuId);
        if (kgTuNode == null) {
            return null;
        }
        EtkEditFields editFields = new EtkEditFields();
        String fieldName;
        String title;
        KgTuNode kgNode;
        KgTuNode tuNode;
        if (kgTuId.isKgNode()) {
            // KG-Node: Title ändern
            title = "!!KG-Benennung ändern";
            fieldName = iPartsConst.FIELD_DA_DKT_KG;
            kgNode = kgTuNode;
            tuNode = null;
            if (checkIsInTranslation(project, kgNode, "!!KG", title)) {
                return null;
            }
        } else {
            // TU-Node: Title ändern
            title = "!!TU-Benennung ändern";
            fieldName = iPartsConst.FIELD_DA_DKT_TU;
            KgTuId kgId = new KgTuId(kgTuId.getKg(), "");
            kgNode = KgTuForProduct.getInstance(dataConnector.getProject(), productId).getNode(kgId);
            tuNode = kgTuNode;
            if (checkIsInTranslation(project, tuNode, "!!TU", title)) {
                return null;
            }
        }
        EtkEditField editField = new EtkEditField(iPartsConst.TABLE_DA_KGTU_TEMPLATE, fieldName, false);
        editField.setEditierbar(false);
        editField.setMussFeld(false);
        editFields.addField(editField);
        editField = new EtkEditField(iPartsConst.TABLE_DA_KGTU_TEMPLATE, iPartsConst.FIELD_DA_DKT_DESC, true);
        editField.setMussFeld(true);
        editFields.addField(editField);

        String value = kgTuNode.getNumber();
        EtkMultiSprache multi = kgTuNode.getTitle().cloneMe();
        // TextId zurücksetzen => damit wird automatisch beim Speichern eine neue TextId erzeugt und angelegt
        multi.setTextId("");

        DBDataObjectAttributes attributes = new DBDataObjectAttributes();
        attributes.addField(fieldName, value, DBActionOrigin.FROM_DB);
        DBDataObjectAttribute attribute = new DBDataObjectAttribute(iPartsConst.FIELD_DA_DKT_DESC, DBDataObjectAttribute.TYPE.MULTI_LANGUAGE, false);
        attribute.setValueAsMultiLanguage(multi, DBActionOrigin.FROM_DB);
        attributes.addField(attribute, DBActionOrigin.FROM_DB);

        // Editor aufrufen
        EditUserControlForKgTu dlg = new EditUserControlForKgTu(dataConnector, parentForm, iPartsConst.TABLE_DA_KGTU_TEMPLATE,
                                                                null, attributes, editFields, false, kgTuId.isKgNode());
        dlg.setTitle(title);
        if (ModalResult.OK == dlg.showModal()) {
            // es wurde etwas geändert
            DBDataObjectAttributes attrib = dlg.getAttributes();
            KgTuListItem kgTuListItem;
            if (kgTuId.isKgNode()) {
                kgTuListItem = new KgTuListItem(kgNode, KgTuListItem.Source.PRODUCT, false);
            } else {
                KgTuListItem kgListItem = new KgTuListItem(kgNode, KgTuListItem.Source.PRODUCT, true);
                kgTuListItem = new KgTuListItem(tuNode, KgTuListItem.Source.PRODUCT, kgListItem, false);
                kgListItem.addChild(kgTuListItem);
            }
            kgTuListItem.setPskNature(KgTuListItem.PSK_NATURE.PSK_CHANGED_TITLE);
            kgTuListItem.getKgTuNode().setTitle(attrib.getField(iPartsConst.FIELD_DA_DKT_DESC).getAsMultiLanguageInternal());

            return kgTuListItem;
        }
        return null;
    }

    private static final String KG_TOOLTIP = "!!KG-Knoten bearbeiten";
    private static final String KG_TOOLTIP_NEW = "!!KG-Knoten anlegen";
    private static final String KG_TOOLTIP_IN_TRANLATION = "!!KG-Knoten befindet sich im Übersetzungsprozess!";
    private static final String TU_TOOLTIP = "!!TU-Knoten bearbeiten";
    private static final String TU_TOOLTIP_NEW = "!!TU-Knoten anlegen";
    private static final String TU_TOOLTIP_IN_TRANLATION = "!!TU-Knoten befindet sich im Übersetzungsprozess!";

    // Defaultwerte
    private String startLanguageDefaultValue = Language.DE.getCode();

    // Spezifische Eigenschaften der Komponente
    private String startLanguage = startLanguageDefaultValue;

    private boolean isReadOnly = false;
    private OnVerifyEvent onVerifyEvent = null;
    private EventListener doVerifyEvent;

    // TextFelder für ReadOnly Betrieb
    private GuiTextField textfieldKG = null;
    private GuiTextField textfieldTU = null;

    protected EventListeners eventListeners;
    private TreeMap<String, KgTuListItem> kgTuMap;

    private iPartsProductId productId;
    private KgTuId startKgTuId = null;
    private KgTuId readOnlyKgTuId = null;

    private RComboBox<iPartsProductId> comboboxProduct;
    private RComboBox<KgTuListItem> comboboxKG;
    private RComboBox<KgTuListItem> comboboxTU;
    private GuiButton buttonPSKKGEdit;
    private GuiButton buttonPSKKgNew;
    private GuiButton buttonPSKTUEdit;
    private GuiButton buttonPSKTUNew;
    private boolean isPSKUser;
    private boolean isPSKProduct;
    private boolean isSpecialCatalog;

    /**
     * Methode zur Anzeige und Ausführung des Dialogs mit der aktuellen DB-Sprache.
     *
     * @param dataConnector
     * @param parentForm
     * @param startKgTuListItem Vorbesetzung der KgTuId oder null
     * @return gültige EinPasId oder null
     */
    public static KgTuListItem showKgTuDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                              iPartsProductId productId, KgTuListItem startKgTuListItem /*KgTuId startKgTuId*/) {
        ProductKgTuSelection productKgTuSelection = showKgTuDialogWithSkipOptionForListItem(dataConnector, parentForm, productId, startKgTuListItem,
                                                                                            false, null, null);
        if (productKgTuSelection != null) {
            return productKgTuSelection.getKgTuListItem();
        } else {
            return null;
        }
    }

    /**
     * Methode zur Anzeige und Ausführung des Dialogs mit der aktuellen DB-Sprache inkl. optionaler Verifizierung sowie
     * Liste verfügbarer Produkte für die Produktauswahl.
     *
     * @param dataConnector
     * @param parentForm
     * @param productId
     * @param startKgTuId       Vorbesetzung der KgTuId oder null
     * @param onVerifyEvent
     * @param availableProducts Verfügbare Produkte oberhalb der KG/TU-Auswahl; bei {@code null} wird die Produktauswahl
     *                          nicht angezeigt
     * @return
     */
    public static ProductKgTuSelection showKgTuDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                      iPartsProductId productId, KgTuId startKgTuId, OnVerifyEvent onVerifyEvent,
                                                      Collection<iPartsProductId> availableProducts) {
        return showKgTuDialogWithSkipOption(dataConnector, parentForm, productId, startKgTuId, false, onVerifyEvent, availableProducts);
    }

    /**
     * Methode zur Anzeige und Ausführung des Dialogs mit der aktuellen DB-Sprache und Option, die Auswahl zu überspringen.
     *
     * @param dataConnector
     * @param parentForm
     * @param startKgTuId       Vorbesetzung der KgTuId oder null
     * @param withSkip          true: der Cancel-Button zeigt 'Überspringen' und liefert ModalResult.OK
     *                          damit kann der Returnwert auch null sein
     * @param onVerifyEvent
     * @param availableProducts Verfügbare Produkte oberhalb der KG/TU-Auswahl; bei {@code null} wird die Produktauswahl
     *                          nicht angezeigt
     * @return gültige {@link ProductKgTuSelection} oder null wenn übersprungen (Vorsicht bei Option {@code withSkip})
     */
    public static ProductKgTuSelection showKgTuDialogWithSkipOption(AbstractJavaViewerFormIConnector dataConnector,
                                                                    AbstractJavaViewerForm parentForm,
                                                                    iPartsProductId productId, KgTuId startKgTuId,
                                                                    boolean withSkip, OnVerifyEvent onVerifyEvent,
                                                                    Collection<iPartsProductId> availableProducts) {
        EditKGTUDialog kgTuDlg = createDialog(dataConnector, parentForm, productId, withSkip, availableProducts, onVerifyEvent);
        kgTuDlg.setStartKgTuId(startKgTuId);
        return showDialog(kgTuDlg);
    }

    public static ProductKgTuSelection showKgTuDialogWithSkipOptionForListItem(AbstractJavaViewerFormIConnector dataConnector,
                                                                               AbstractJavaViewerForm parentForm,
                                                                               iPartsProductId productId,
                                                                               KgTuListItem startKgTuListItem,
                                                                               boolean withSkip, OnVerifyEvent onVerifyEvent,
                                                                               Collection<iPartsProductId> availableProducts) {
        EditKGTUDialog kgTuDlg = createDialog(dataConnector, parentForm, productId, withSkip, availableProducts, onVerifyEvent);
        kgTuDlg.setStartKgTuIdFromListItem(startKgTuListItem /*startKgTuId*/);
        return showDialog(kgTuDlg);
    }

    private static ProductKgTuSelection showDialog(EditKGTUDialog kgTuDlg) {
        if (kgTuDlg.showModal() == ModalResult.OK) {
            return new ProductKgTuSelection(kgTuDlg.getProductId(), kgTuDlg.getKgTuNode(), kgTuDlg.isCheckboxOpenTuSelected());
        }
        return null;
    }

    private static EditKGTUDialog createDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                               iPartsProductId productId, boolean withSkip, Collection<iPartsProductId> availableProducts, OnVerifyEvent onVerifyEvent) {
        EditKGTUDialog kgTuDlg = new EditKGTUDialog(dataConnector, parentForm, productId);
        kgTuDlg.setStartLanguage(dataConnector.getProject().getDBLanguage());
        if (withSkip) {
            kgTuDlg.setCancelButtonText("!!Überspringen", ModalResult.IGNORE);
        }
        if (availableProducts != null) {
            kgTuDlg.setAvailableProducts(availableProducts);
        }
        if (onVerifyEvent != null) {
            kgTuDlg.setOnVerifyEvent(onVerifyEvent);
        }
        return kgTuDlg;
    }

    public interface OnVerifyEvent {

        boolean verify(EditKGTUDialog dialog);
    }

    /**
     * Erzeugt eine Instanz von EditKGTUDialog.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditKGTUDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, iPartsProductId productId) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.productId = productId;
        this.eventListeners = new EventListeners();
        this.isPSKUser = false;
        this.isPSKProduct = false;
        this.isSpecialCatalog = false;

        this.kgTuMap = new TreeMap<>();
        this.doVerifyEvent = new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                GuiButtonOnPanel okButton = mainWindow.buttonpanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK);
                if (!onVerifyEvent.verify(EditKGTUDialog.this)) {
                    okButton.setEnabled(false);
                } else {
                    okButton.setModalResult(ModalResult.OK);
                    okButton.removeEventListener(this);
                    okButton.doClick();
                }
            }
        };

        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        setTitle("!!KG-TU Verortung festlegen");

        mainWindow.label_product.setVisible(false);
        comboboxProduct = RComboBox.replaceGuiComboBox(mainWindow.combobox_product);
        comboboxProduct.setVisible(false);
        comboboxProduct.addEventListener(new EventListener(Event.COMBOBOX_CLOSED_EVENT) {
            @Override
            public void fire(Event event) {
                // Beim Schließen mit Auswahl eines Produkts automatisch die KG-ComboBox fokussieren
                boolean comboBoxClosedByControl = event.getBooleanParameter(Event.EVENT_PARAMETER_COMBOBOX_CLOSED_BY_CONTROL);
                if (!comboBoxClosedByControl) { // DropDown-Popup wurde nicht durch das Control selbst geschlossen -> es wurde z.B. TAB gedrückt
                    if (comboboxProduct.getSelectedIndex() != -1) {
                        comboboxKG.requestFocus();
                    }
                }
            }
        });

        comboboxKG = RComboBox.replaceGuiComboBox(mainWindow.combobox_KGTU_KG);
        comboboxKG.requestFocus();
        comboboxKG.addEventListener(new EventListener(Event.COMBOBOX_CLOSED_EVENT) {
            @Override
            public void fire(Event event) {
                // Beim Schließen mit Auswahl eines KGs automatisch die TU-ComboBox fokussieren
                boolean comboBoxClosedByControl = event.getBooleanParameter(Event.EVENT_PARAMETER_COMBOBOX_CLOSED_BY_CONTROL);
                if (!comboBoxClosedByControl) { // DropDown-Popup wurde nicht durch das Control selbst geschlossen -> es wurde z.B. TAB gedrückt
                    if (comboboxKG.getSelectedIndex() != -1) {
                        comboboxTU.requestFocus();
                    }
                }
            }
        });

        comboboxTU = RComboBox.replaceGuiComboBox(mainWindow.combobox_KGTU_TU);
        comboboxTU.addEventListener(new EventListener(Event.COMBOBOX_CLOSED_EVENT) {
            @Override
            public void fire(Event event) {
                // Beim Schließen mit Auswahl eines TUs automatisch den OK-Button fokussieren
                boolean comboBoxClosedByControl = event.getBooleanParameter(Event.EVENT_PARAMETER_COMBOBOX_CLOSED_BY_CONTROL);
                if (!comboBoxClosedByControl) { // DropDown-Popup wurde nicht durch das Control selbst geschlossen -> es wurde z.B. TAB gedrückt
                    if (comboboxTU.getSelectedIndex() != -1) {
                        GuiButtonOnPanel buttonOnPanel = mainWindow.buttonpanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK);
                        if (buttonOnPanel != null) {
                            buttonOnPanel.requestFocus();
                        }
                    }
                }
            }
        });

        createPSKButtons();
        // zusätzlichen Button verstecken
        setButtonKGTUChangeListener(null);
        // Aufruf kommt vom Verschieben/Kopieren einer POS in einen anderen TU
        if ((parentForm != null) && (parentForm instanceof EditAssemblyListForm)) {
            mainWindow.checkbox_open_TU.setVisible(true);
        }

        // Aktive ChangeSets berücksichtigen
        startPseudoTransactionForActiveChangeSet(true);
        try {
            init();
        } finally {
            stopPseudoTransactionForActiveChangeSet();
        }

        ThemeManager.get().render(mainWindow);
        updateButtonsMaxWidth();
        mainWindow.pack();
    }

    private void init() {
        kgTuMap.clear();
        if ((productId != null) && productId.isValidId()) {
            isPSKUser = iPartsRight.checkPSKInSession();
            isPSKProduct = iPartsProduct.getInstance(getProject(), productId).isPSK();
            isSpecialCatalog = iPartsProduct.getInstance(getProject(), productId).isSpecialCatalog();

            setPSKButtonsVisible(!isReadOnly && !mainWindow.checkbox_open_TU.isVisible() && isExtraHandling() /*isPSKUser && isPSKProduct*/);
            // KgTU initialisieren
            KgTuForProduct kgTuProduct = KgTuForProduct.getInstance(getProject(), productId);
            Map<String, KgTuTemplate> kgTuTemplateMap = KgTuTemplate.getInstance(productId, getProject());

            boolean hasTemplateValues = false;

            for (KgTuNode kgNode : kgTuProduct.getKgNodeList()) {
                String kgNr = kgNode.getId().getKg();
                KgTuListItem kgListItem = new KgTuListItem(kgNode, KgTuListItem.Source.PRODUCT, true);
                //alle TU Kinder auch hinzufügen
                for (KgTuNode tuNode : kgNode.getChildren()) {
                    kgListItem.addChild(new KgTuListItem(tuNode, KgTuListItem.Source.PRODUCT, kgListItem, false));
                }
                kgTuMap.put(kgNr, kgListItem);
            }

            // Jetzt noch die Einträge aus den nach AS Produktklassen sortierten Templates dazu mischen
            for (KgTuTemplate kgTuTemplate : kgTuTemplateMap.values()) {
                for (KgTuNode kgNode : kgTuTemplate.getKgNodeList()) {
                    String kgNr = kgNode.getId().getKg();
                    KgTuListItem kgListItem = kgTuMap.get(kgNr);
                    if (kgListItem == null) {
                        kgListItem = new KgTuListItem(kgNode, KgTuListItem.Source.TEMPLATE, true);
                        kgTuMap.put(kgNr, kgListItem);
                        hasTemplateValues = true;
                    }
                    //alle TU Kinder auch hinzufügen
                    for (KgTuNode tuNode : kgNode.getChildren()) {
                        boolean inserted = kgListItem.addChild(new KgTuListItem(tuNode, KgTuListItem.Source.TEMPLATE, kgListItem, false));
                        if (inserted) {
                            hasTemplateValues = true;
                        }
                    }
                }
            }

            mainWindow.label_info.setVisible(hasTemplateValues);

            fillComboBoxItems(comboboxKG, kgTuMap.values());
            EditFormComboboxHelper.clearComboBox(comboboxTU);
            setStartKgTuId(startKgTuId);
        }
        enableButtons();
    }

    private void updateButtonsMaxWidth() {
        int buttonMaxWidth = isReadOnly ? textfieldKG.getPreferredHeight() : comboboxKG.getPreferredHeight();
        mainWindow.button_KGTUChange.setMaximumWidth(buttonMaxWidth);
        if (buttonPSKKGEdit != null) {
            buttonPSKKGEdit.setMaximumWidth(buttonMaxWidth);
            buttonPSKKgNew.setMaximumWidth(buttonMaxWidth);
            buttonPSKTUEdit.setMaximumWidth(buttonMaxWidth);
            buttonPSKTUNew.setMaximumWidth(buttonMaxWidth);
        }
    }

    private boolean isPSK() {
        return isPSKUser & isPSKProduct;
    }

    private boolean isSpecialCatalog() {
        return isSpecialCatalog;
    }

    private boolean isExtraHandling() {
        return isPSK() || isSpecialCatalog();
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panel_KGTU;
    }

    public ModalResult showModal() {
        return mainWindow.showModal();
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public void setOnVerifyEvent(final OnVerifyEvent onVerifyEvent) {
        this.onVerifyEvent = onVerifyEvent;
        GuiButtonOnPanel okButton = mainWindow.buttonpanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK);
        if (onVerifyEvent != null) {
            okButton.addEventListener(doVerifyEvent);
            okButton.setModalResult(ModalResult.NONE);
        } else {
            okButton.removeEventListener(doVerifyEvent);
            okButton.setModalResult(ModalResult.OK);
        }
    }

    /**
     * Überprüfungsfunktion, ob EinPasPanel initialisiert ist
     *
     * @return
     */
    public boolean isInit() {
        return (kgTuMap != null);
    }

    /**
     * Test, ob alle Struktur Felder besetzt sind
     *
     * @return
     */
    public boolean isValid() {
        if (isInit()) {
            // Bei ReadOnly ist das Control nur gültig, wenn beide Texteflder einen Wert haben (analog zu Comboboxen bei
            // nicht ReadOnly)
            if (isReadOnly) {
                return StrUtils.isValid(textfieldKG.getText(), textfieldTU.getText());
            } else {
                return (getSelectedKGNumber() != null) && (getSelectedTUNumber() != null);
            }
        }
        return false;
    }

    /**
     * zusätzliche Überschrift auf dem EinPAS-Panel
     *
     * @param title
     */
    public void setPanelTitle(String title) {
        mainWindow.panel_KGTU.setTitle(title);
    }

    public void setPanelTitleFontStyle(DWFontStyle style) {
        // der title-Fontstyle kann i.A. nicht geändertwerden => ändere Fontstyle der Labels
        mainWindow.label_KG.setFontStyle(style);
        mainWindow.label_TU.setFontStyle(style);
    }

    public String getStartLanguage() {
        return startLanguage;
    }

    public void setStartLanguage(String startLanguage) {
        this.startLanguage = startLanguage;
    }

    public iPartsProductId getProductId() {
        return productId;
    }

    public void setProductId(iPartsProductId productId) {
        // Aktive ChangeSets berücksichtigen
        startPseudoTransactionForActiveChangeSet(true);
        try {
            if (this.productId != null) {
                if (productId != null) {
                    if (!this.productId.equals(productId)) {
                        this.productId = productId;
                        init();
                    }
                } else {
                    this.productId = productId;
                    kgTuMap = null;
                }
            } else {
                this.productId = productId;
                init();
            }
        } finally {
            stopPseudoTransactionForActiveChangeSet();
        }
    }

    /**
     * ReadOnly setzen
     * Die Comboboxen werden durch TextFields (editable=false + spezielle Background Color) ersetzt
     *
     * @param value
     */
    public void setReadOnly(boolean value) {
        if (value != isReadOnly) {
            if (value) {
                boolean setNames = (textfieldKG == null);
                textfieldKG = EditFormComboboxHelper.replaceComboBoxByTextField(comboboxKG, textfieldKG, mainWindow.panel_KGTU);
                textfieldTU = EditFormComboboxHelper.replaceComboBoxByTextField(comboboxTU, textfieldTU, mainWindow.panel_KGTU);
                if (setNames) {
                    textfieldKG.setName("textfield_KGTU_KG");
                    textfieldTU.setName("textfield_KGTU_TU");
                }
            } else {
                readOnlyKgTuId = null;
                EditFormComboboxHelper.replaceTextFieldByComboBox(textfieldKG, comboboxKG, mainWindow.panel_KGTU);
                EditFormComboboxHelper.replaceTextFieldByComboBox(textfieldTU, comboboxTU, mainWindow.panel_KGTU);
            }
            isReadOnly = value;
            updateButtonsMaxWidth();
        }
    }

    private boolean isCheckboxOpenTuSelected() {
        return mainWindow.checkbox_open_TU.isSelected();
    }

    /**
     * Setzt die verfügbaren Produkte für die Produktauswahl
     *
     * @param availableProducts Verfügbare Produkte oberhalb der KG/TU-Auswahl; bei {@code null} wird die Produktauswahl
     *                          nicht angezeigt
     */
    public void setAvailableProducts(Collection<iPartsProductId> availableProducts) {
        comboboxProduct.switchOffEventListeners();
        try {
            comboboxProduct.removeAllItems();
            if (availableProducts != null) {
                EtkProject project = getProject();
                String dbLanguage = project.getDBLanguage();
                List<String> dataBaseFallbackLanguages = project.getDataBaseFallbackLanguages();
                for (iPartsProductId productId : availableProducts) {
                    iPartsProduct product = iPartsProduct.getInstance(project, productId);
                    String productTitle = product.getProductTitle(project).getTextByNearestLanguage(dbLanguage, dataBaseFallbackLanguages);
                    comboboxProduct.addItem(productId, productId.getProductNumber() + " - " + productTitle);
                }

                mainWindow.label_product.setVisible(true);
                comboboxProduct.setVisible(true);
                comboboxProduct.setEnabled(comboboxProduct.getItemCount() > 1);
            } else {
                mainWindow.label_product.setVisible(false);
                comboboxProduct.setVisible(false);
            }
            mainWindow.pack();
        } finally {
            comboboxProduct.switchOnEventListeners();
        }
        comboboxProduct.setSelectedUserObject(productId);
    }

    /**
     * neue KgTuId setzen
     *
     * @param startKgTuId
     */
    public void setStartKgTuId(KgTuId startKgTuId) {
        if (isReadOnly) {
            readOnlyKgTuId = startKgTuId;
        }
        if (startKgTuId != null) {
            if (isInit()) {
                KgTuListItem kgListItem = kgTuMap.get(startKgTuId.getKg());
                if (kgListItem != null) {
                    setSelectedIndexByNode(comboboxKG, kgListItem);
                    EditFormComboboxHelper.setTextFieldText(textfieldKG, comboboxKG);
                    for (KgTuListItem tuNode : kgListItem.getChildren()) {
                        if (tuNode.getKgTuId().getTu().equals(startKgTuId.getTu())) {
                            setSelectedIndexByNode(comboboxTU, tuNode);
                            EditFormComboboxHelper.setTextFieldText(textfieldTU, comboboxTU);
                            this.startKgTuId = null;
                            return;
                        }
                    }
                    this.startKgTuId = null;
                } else {
                    this.startKgTuId = startKgTuId;
                }
            } else {
                this.startKgTuId = startKgTuId;
            }
        }
    }

    public void setStartKgTuIdFromListItem(KgTuListItem startKgTuListItem) {
        handleStartKgTuListItem(startKgTuListItem);
        KgTuId kgTuId = null;
        if (startKgTuListItem != null) {
            kgTuId = startKgTuListItem.getKgTuId();
        }
        setStartKgTuId(kgTuId);
    }

    public void setKgTuForReadOnlyFromListItem(KgTuListItem kgTuListItem) {
        handleStartKgTuListItem(kgTuListItem);
        KgTuId kgTuId = null;
        if (kgTuListItem != null) {
            kgTuId = kgTuListItem.getKgTuId();
        }
        setKgTuForReadOnly(kgTuId);
    }

    public boolean isSelectedKgNodeTitleChanged() {
        KgTuListItem kgListItem = getSelectedUserObject(comboboxKG);
        if (kgListItem != null) {
            return kgListItem.getPskNature() == KgTuListItem.PSK_NATURE.PSK_CHANGED_TITLE;
        }
        return false;
    }

    public boolean isSelectedTuNodeTitleChanged() {
        KgTuListItem tuListItem = getSelectedUserObject(comboboxTU);
        if (tuListItem != null) {
            return tuListItem.getPskNature() == KgTuListItem.PSK_NATURE.PSK_CHANGED_TITLE;
        }
        return true;
    }

    /**
     * einen PSK-modifizierten KH-Node in bestehende Struktur einfügen
     *
     * @param startKgTuListItem
     */
    private void handleStartKgTuListItem(KgTuListItem startKgTuListItem) {
        if (isExtraHandling() /*isPSKUser && isPSKProduct*/ && (startKgTuListItem != null)) {
            // es handelt sich um PSK
            KgTuListItem kgNode = startKgTuListItem.getParent();
            String kgNumber = kgNode.getKgTuId().getKg();
            boolean kgIsChanged = false;
            if (kgNode.getPskNature() != KgTuListItem.PSK_NATURE.PSK_NONE) {
                // neuer oder modifizierter Kg-Node
                if (kgNode.getPskNature() == KgTuListItem.PSK_NATURE.PSK_NEW_NODE) {
                    // neuer Kg-Node
                    createNewKgNode(kgNumber, kgNode.getKgTuNode().getTitle().cloneMe());
                    kgIsChanged = true;
                } else if (kgNode.getPskNature() == KgTuListItem.PSK_NATURE.PSK_CHANGED_TITLE) {
                    // modifizierter Kg-Node (title)
                    KgTuListItem kgMapNode = kgTuMap.get(kgNode.getKgTuId().getKg());
                    if (kgMapNode != null) {
                        modifyKgNode(kgMapNode, kgNode.getKgTuNode().getTitle().cloneMe());
                    }
                    kgIsChanged = true;
                }
            }

            boolean tuIsChanged = false;
            if (kgNode.getChildren() != null) {
                // Kg-Node besitzt TU-Nodes
                kgNode = kgTuMap.get(kgNumber);
                for (KgTuListItem tuNode : startKgTuListItem.getParent().getChildren()) {
                    if (tuNode.getPskNature() == KgTuListItem.PSK_NATURE.PSK_NEW_NODE) {
                        // neuer TU-Node
                        createNewTuNodeWithCopyKg(kgNode, tuNode.getKgTuId().getTu(), tuNode.getKgTuNode().getTitle().cloneMe());
                        kgNode = kgTuMap.get(kgNumber);
                        tuIsChanged = true;
                    } else if (tuNode.getPskNature() == KgTuListItem.PSK_NATURE.PSK_CHANGED_TITLE) {
                        // modifizierter Kg-Node
                        KgTuListItem tuMapNode = getTuNodeFromKg(kgNode, tuNode.getKgTuId());
                        if (tuMapNode != null) {
                            modifyTuNode(kgNode, tuMapNode, tuNode.getKgTuNode().getTitle().cloneMe());
                        }
                        kgNode = kgTuMap.get(kgNumber);
                        tuIsChanged = true;
                    }
                }
            }
            String tuNumber = startKgTuListItem.getKgTuId().getTu();
            if (kgIsChanged) {
                fillComboBoxItems(comboboxKG, kgTuMap.values());
                KgTuListItem selectItem = kgTuMap.get(kgNumber);
                if (selectItem != null) {
                    setSelectedIndexByNode(comboboxKG, selectItem);
                }
            }
            if (tuIsChanged) {
                fillComboBoxItems(comboboxTU, kgNode.getChildren());
            }

            KgTuListItem select = getTuNodeFromKg(kgNode, new KgTuId(kgNumber, tuNumber));
            if (select != null) {
                setSelectedIndexByNode(comboboxTU, select);
            }
        }
    }

    /**
     * Setzt eine feste KG/TU-Struktur für den Nicht-Edit-Fall (Textfelder und keine ComboBoxen)
     *
     * @param kgTuId
     */
    public void setKgTuForReadOnly(KgTuId kgTuId) {
        if (!isReadOnly) {
            setReadOnly(true);
        }

        // Damit die ComboBoxen die richtigen KgtuListItems auswählen für die angezeigten Texte
        setStartKgTuId(kgTuId);

        // KG-Benennung
        String selectedKgText = comboboxKG.getSelectedItem();
        if (StrUtils.isValid(selectedKgText)) {
            textfieldKG.setText(selectedKgText);
        } else if (kgTuId != null) { // Fallback auf die reine KG-Nummer
            textfieldKG.setText(kgTuId.getKg());
        }

        // TU-Benennung
        String selectedTuText = comboboxTU.getSelectedItem();
        if (StrUtils.isValid(selectedTuText)) {
            textfieldTU.setText(selectedTuText);
        } else if (kgTuId != null) { // Fallback auf die reine TU-Nummer
            textfieldTU.setText(kgTuId.getTu());
        }
    }

    /**
     * Selektierte KG-Nummer holen
     *
     * @return
     */
    public String getKG() {
        if (isValid()) {
            return getSelectedKGNumber();
        }
        return null;
    }

    /**
     * Selektierte TU-Nummer holen
     *
     * @return
     */
    public String getTU() {
        if (isValid()) {
            return getSelectedTUNumber();
        }
        return null;
    }

    /**
     * selektierte KG-Description holen
     *
     * @return
     */
    public EtkMultiSprache getKGDescription() {
        KgTuListItem kgNode = getSelectedUserObject(comboboxKG);
        if (kgNode != null) {
            return kgNode.getKgTuNode().getTitle().cloneMe();
        }
        return null;
    }

    /**
     * selektierte TU-Description holen
     *
     * @return
     */
    public EtkMultiSprache getTUDescription() {
        String tuName = getTU();
        if (StrUtils.isValid(tuName)) {
            KgTuListItem tuNode = getSelectedUserObject(comboboxTU);
            if (tuNode != null) {
                return tuNode.getKgTuNode().getTitle().cloneMe();
            }
        }
        return null;
    }

    /**
     * Liefert Ergebnis als KgTuId
     *
     * @return
     */
    public KgTuId getKgTuId() {
        if (isValid()) {
            // Im ReadOnly Fall einfach readOnlyKgTuId zurückliefern falls gesetzt
            if (readOnlyKgTuId != null) {
                return readOnlyKgTuId;
            } else {
                KgTuListItem selectedTU = getSelectedUserObject(comboboxTU);
                if (selectedTU != null) {
                    return selectedTU.getKgTuId();
                }
            }
        }
        return null;
    }

    public KgTuListItem getKgTuNode() {
        if (isValid()) {
            return getSelectedUserObject(comboboxTU);
        }
        return null;
    }

    public KgTuListItem getKgNode() {
        KgTuListItem selectedItem = getKgTuNode();
        if (selectedItem != null) {
            return selectedItem.getParent();
        }
        return null;
    }

    public void setNotExistingKgTuNodeInfoVisible(boolean visible) {
        mainWindow.label_info.setVisible(visible);
    }

    /**
     * Eventlistener für Änderugen bei jeder ComboBox setzen
     *
     * @param eventListener
     */
    public void addEventListener(EventListener eventListener) {
        eventListeners.addEventListener(eventListener);
    }

    /**
     * Eventlistener für Änderugen bei jeder ComboBox löschen
     *
     * @param eventListener
     */
    public void removeEventListener(EventListener eventListener) {
        eventListeners.removeEventListener(eventListener);
    }

    /**
     * Eventlistener für den zusätzlichen Button setzen (damit wird der Button automatisch sichtbar)
     * oder Löschen (damit wird der Button automatisch unsichtbar)
     *
     * @param eventListener
     */
    public void setButtonKGTUChangeListener(EventListener eventListener) {
        if (eventListener != null) {
            mainWindow.button_KGTUChange.addEventListener(eventListener);
            mainWindow.button_KGTUChange.setVisible(true);
        } else {
            mainWindow.button_KGTUChange.removeEventListeners(Event.ACTION_PERFORMED_EVENT);
            mainWindow.button_KGTUChange.setVisible(false);
        }
        setPSKButtonsVisible(!isReadOnly && !mainWindow.button_KGTUChange.isVisible() && isExtraHandling() /*isPSKUser && isPSKProduct*/);
    }

    /**
     * Sichtbarkeit der PSK-Button setzen
     *
     * @param visible
     */
    private void setPSKButtonsVisible(boolean visible) {
        buttonPSKKGEdit.setVisible(visible);
        buttonPSKKgNew.setVisible(visible);
        buttonPSKTUEdit.setVisible(visible);
        buttonPSKTUNew.setVisible(visible);
        buttonPSKTUNew.setEnabled(false);
    }

    /**
     * PSK-Buttons erzeugen
     */
    private void createPSKButtons() {
        buttonPSKKGEdit = createButton("button_PSK_KG_Edit", KG_TOOLTIP, EditToolbarButtonAlias.IMG_EDIT.getImage());
        addPSKButtonToPanel(buttonPSKKGEdit, 1, 0, false, new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doEditPskKG();
            }
        });

        buttonPSKKgNew = createButton("button_PSK_KG_New", KG_TOOLTIP_NEW, EditToolbarButtonAlias.IMG_NEW.getImage());
        addPSKButtonToPanel(buttonPSKKgNew, 2, 0, true, new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doCreatePskKG();
            }
        });

        buttonPSKTUEdit = createButton("button_PSK_TU_Edit", TU_TOOLTIP, EditToolbarButtonAlias.IMG_EDIT.getImage());
        addPSKButtonToPanel(buttonPSKTUEdit, 1, 1, false, new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doEditPskTU();
            }
        });

        buttonPSKTUNew = createButton("button_PSK_TU_New", TU_TOOLTIP_NEW, EditToolbarButtonAlias.IMG_NEW.getImage());
        addPSKButtonToPanel(buttonPSKTUNew, 2, 1, true, new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doCreatePskTU();
            }
        });

        setPSKButtonsVisible(false);
    }

    private void addPSKButtonToPanel(GuiButton button, int gridX, int gridY, boolean enabled, EventListener eventListener) {
        ConstraintsGridBag constraints = (ConstraintsGridBag)mainWindow.button_KGTUChange.getConstraints(); // getConstraints() liefert bereits einen Klon
        constraints.setInsetsLeft(8);
        constraints.setGridx(constraints.getGridx() + gridX);
        constraints.setGridy(constraints.getGridy() + gridY);
        button.setConstraints(constraints);
        button.setEnabled(enabled);
        button.addEventListener(eventListener);
        mainWindow.panel_KGTU.addChild(button);
    }

    /**
     * PSK-KG-Node editieren
     */
    private void doEditPskKG() {
        doEditPskKgTuCall(true, true);
    }

    /**
     * PSK-KG-Node neu anlegen
     */
    private void doCreatePskKG() {
        doEditPskKgTuCall(false, true);
    }

    /**
     * PSK-TU-Node editieren
     */
    private void doEditPskTU() {
        doEditPskKgTuCall(true, false);
    }

    /**
     * PSK-TU-Node neu anlegen
     */
    private void doCreatePskTU() {
        doEditPskKgTuCall(false, false);
    }

    /**
     * Edit für Änderung PSK-Kg/Tu-Node aufrufen und Ergebnis einfügen
     *
     * @param isEdit
     * @param isKg
     */
    private void doEditPskKgTuCall(boolean isEdit, boolean isKg) {
        EtkEditFields editFields = new EtkEditFields();
        String fieldName = iPartsConst.FIELD_DA_DKT_KG;
        if (!isKg) {
            fieldName = iPartsConst.FIELD_DA_DKT_TU;
        }
        EtkEditField editField = new EtkEditField(iPartsConst.TABLE_DA_KGTU_TEMPLATE, fieldName, false);
        editField.setEditierbar(!isEdit);
        editField.setMussFeld(!isEdit);
        editFields.addField(editField);
        editField = new EtkEditField(iPartsConst.TABLE_DA_KGTU_TEMPLATE, iPartsConst.FIELD_DA_DKT_DESC, true);
        editField.setMussFeld(true);
        editFields.addField(editField);

        DBDataObjectAttributes attributes = null;
        String title = "";
        List<String> usedNodeNumbers = new DwList<>();
        if (isEdit) {
            // KG oder TU-Node: Title ändern
            String value;
            EtkMultiSprache multi;
            if (isKg) {
                // KG-Node: Title ändern
                title = "!!KG-Benennung ändern";
                if (checkIsInTranslation(comboboxKG, "!!KG", title)) {
                    return;
                }
                value = getSelectedKGNumber();
                multi = getKGDescription();
                for (KgTuListItem kgItem : comboboxKG.getUserObjects()) {
                    usedNodeNumbers.add(kgItem.getKgTuId().getKg());
                }
            } else {
                // TU-Node: Title ändern
                title = "!!TU-Benennung ändern";
                if (checkIsInTranslation(comboboxTU, "!!TU", title)) {
                    return;
                }
                value = getSelectedTUNumber();
                multi = getTUDescription();
                for (KgTuListItem kgItem : comboboxTU.getUserObjects()) {
                    usedNodeNumbers.add(kgItem.getKgTuId().getTu());
                }
            }
            attributes = new DBDataObjectAttributes();
            DBDataObjectAttribute attribute = new DBDataObjectAttribute(fieldName, DBDataObjectAttribute.TYPE.STRING, false);
            attribute.setValueAsString(value, DBActionOrigin.FROM_DB);
            attributes.addField(attribute, DBActionOrigin.FROM_DB);
            attribute = new DBDataObjectAttribute(iPartsConst.FIELD_DA_DKT_DESC, DBDataObjectAttribute.TYPE.MULTI_LANGUAGE, false);
            attribute.setValueAsMultiLanguage(multi, DBActionOrigin.FROM_DB);
            attributes.addField(attribute, DBActionOrigin.FROM_DB);
        } else {
            // KG oder TU-Node: Neu anlegen
            if (isKg) {
                // KG-Node: Neu anlegen
                title = "!!KG und Benennung anlegen";
                // Liste der bereits belegten KG-Nodes
                for (KgTuListItem kgItem : comboboxKG.getUserObjects()) {
                    usedNodeNumbers.add(kgItem.getKgTuId().getKg());
                }
            } else {
                // TU-Node: Neu anlegen
                title = "!!TU und Benennung anlegen";
                // Liste der bereits belegten TU-Nodes
                for (KgTuListItem kgItem : comboboxTU.getUserObjects()) {
                    usedNodeNumbers.add(kgItem.getKgTuId().getTu());
                }
            }
        }

        // Editor aufrufen
        EditUserControlForKgTu dlg = new EditUserControlForKgTu(getConnector(), this, iPartsConst.TABLE_DA_KGTU_TEMPLATE,
                                                                null, attributes, editFields, !isEdit, isKg);
        dlg.setTitle(title);
        dlg.setUsedNodeNumbers(usedNodeNumbers);
        if (ModalResult.OK == dlg.showModal()) {
            // es wurde etwas geändert
            DBDataObjectAttributes modifiedAttributes = dlg.getAttributes();
            if (isEdit) {
                // KG oder TU-Node: Title ändern
                KgTuListItem selectedNode;
                if (isKg) {
                    // KG-Knoten: Titel geändert
                    selectedNode = getSelectedUserObject(comboboxKG);
                    handleKgNodeTitleChanged(selectedNode, modifiedAttributes.getField(iPartsConst.FIELD_DA_DKT_DESC).getAsMultiLanguage(null, false), true);
                } else {
                    // TU-Knoten: Titel geändert
                    handleTuNodeTitleChanged(getSelectedUserObject(comboboxKG), getSelectedUserObject(comboboxTU),
                                             modifiedAttributes.getField(iPartsConst.FIELD_DA_DKT_DESC).getAsMultiLanguage(null, false), true);
                }
            } else {
                // KG oder TU-Node: Neu anlegen
                if (isKg) {
                    // KG-Knoten: Neuer KG-Knoten ggf mit Titel geändert
                    String kgNumber = modifiedAttributes.getField(iPartsConst.FIELD_DA_DKT_KG).getAsString();
                    handleNewKgNode(kgNumber, modifiedAttributes.getField(iPartsConst.FIELD_DA_DKT_DESC).getAsMultiLanguage(null, false), true);
                } else {
                    // TU-Knoten: Neuer TU-Knoten ggf mit Titel geändert
                    KgTuListItem selectedNode = getSelectedUserObject(comboboxKG);
                    String tuNumber = modifiedAttributes.getField(iPartsConst.FIELD_DA_DKT_TU).getAsString();
                    handleNewTuNode(selectedNode, tuNumber,
                                    modifiedAttributes.getField(iPartsConst.FIELD_DA_DKT_DESC).getAsMultiLanguage(null, false), true);
                }
            }
        }
    }

    private boolean checkIsInTranslation(RComboBox<KgTuListItem> combobox, String sourceName, String title) {
        KgTuListItem selectedNode = getSelectedUserObject(combobox);
        if (isInTranslation(selectedNode)) {
            showInTranslationMessage(sourceName, title);
            return true;
        }

        return false;
    }

    private static boolean checkIsInTranslation(EtkProject project, KgTuNode kgTuNode, String sourceName, String title) {
        if (isInTranslation(project, kgTuNode)) {
            showInTranslationMessage(sourceName, title);
            return true;
        }

        return false;
    }

    private static void showInTranslationMessage(String sourceName, String title) {
        String message = TranslationHandler.translate("!!Bearbeiten des %1-Textes nicht möglich, da dieser sich gerade im Übersetzungsprozess befindet.",
                                                      TranslationHandler.translate(sourceName));
        MessageDialog.show(message, title);
    }

    /**
     * KG-Knoten: Titel geändert
     * falls noch 'orginal' KG-Node: KG-Node und alle TU-Nodes kopieren und neuen Title bei KG-Node setzen
     * sonst nur Title beim KG-Node setzen
     * ComboBoxKg aktualisieren und selectedNode setzen
     *
     * @param kgNode
     * @param title
     * @param withSelection
     */
    private void handleKgNodeTitleChanged(KgTuListItem kgNode, EtkMultiSprache title, boolean withSelection) {
        KgTuListItem newKgNode = modifyKgNode(kgNode, title);
        // Bei einer Titeländerung soll der ausgewählte TU Knoten wieder gesetzt werden
        KgTuListItem selectedTUNode = getSelectedUserObject(comboboxTU);
        fillComboBoxItems(comboboxKG, kgTuMap.values());
        if (withSelection) {
            setSelectedIndexByNode(comboboxKG, newKgNode);
            if (selectedTUNode != null) {
                setSelectedIndexByNode(comboboxTU, selectedTUNode);
            }
        }
    }

    /**
     * TU-Knoten: Titel geändert
     * falls noch 'orginal' TU-Node: KG-Node und alle TU-Nodes kopieren und neuen Title bei TU-Node setzen
     * sonst nur Title beim TU-Node setzen
     * ComboBoxKg aktualisieren
     * ComboBoxTU aktualisieren und selectedNode setzen
     *
     * @param kgNode
     * @param tuNode
     * @param title
     * @param withSelection
     */
    private void handleTuNodeTitleChanged(KgTuListItem kgNode, KgTuListItem tuNode, EtkMultiSprache title, boolean withSelection) {
        KgTuListItem newTuNode = modifyTuNode(kgNode, tuNode, title);
        if (newTuNode == null) {
            return;
        }
        fillComboBoxItems(comboboxKG, kgTuMap.values());
        KgTuListItem newKgNode = kgTuMap.get(kgNode.getKgTuId().getKg());
        comboboxKG.switchOffEventListeners();
        setSelectedIndexByNode(comboboxKG, newKgNode);
        comboboxKG.switchOnEventListeners();
        if (newKgNode != null) {
            fillComboBoxItems(comboboxTU, newKgNode.getChildren());
        }
        if (withSelection) {
            setSelectedIndexByNode(comboboxTU, newTuNode);
        }
    }

    /**
     * KG-Knoten: Neuer KG-Knoten ggf mit Titel geändert
     * neuen KG-Node anlegen
     * ComboBoxKg aktualisieren und selectedNode setzen
     *
     * @param kgNumber
     * @param title
     * @param withSelection
     */
    private void handleNewKgNode(String kgNumber, EtkMultiSprache title, boolean withSelection) {
        KgTuListItem newKgNode = createNewKgNode(kgNumber, title);
        fillComboBoxItems(comboboxKG, kgTuMap.values());
        buttonPSKTUNew.setEnabled(true);
        if (withSelection) {
            setSelectedIndexByNode(comboboxKG, newKgNode);
        }
    }

    /**
     * TU-Knoten: Neuer TU-Knoten ggf mit Titel geändert
     * falls noch 'orginal' TU-Node: KG-Node und alle TU-Nodes kopieren und neuen TU-Node einfügen
     * sonst neuen TU-Node einfügen
     * ComboBoxKg aktualisieren
     * ComboBoxTU aktualisieren und selectedNode setzen
     *
     * @param kgNode
     * @param tuNumber
     * @param title
     * @param withSelection
     */
    private void handleNewTuNode(KgTuListItem kgNode, String tuNumber, EtkMultiSprache title, boolean withSelection) {
        KgTuListItem newTuNode = createNewTuNodeWithCopyKg(kgNode, tuNumber, title);
        KgTuListItem newKgNode = kgTuMap.get(kgNode.getKgTuId().getKg());
        if (newKgNode != null) {
            fillComboBoxItems(comboboxKG, kgTuMap.values());
            setSelectedIndexByNode(comboboxKG, newKgNode);
            fillComboBoxItems(comboboxTU, newKgNode.getChildren());
        }
        if (withSelection) {
            setSelectedIndexByNode(comboboxTU, newTuNode);
        }
    }

    /**
     * KG-Knoten: Titel geändert
     * falls noch 'orginal' KG-Node: KG-Node und alle TU-Nodes kopieren und neuen Title bei KG-Node setzen
     * sonst nur Title beim KG-Node setzen
     * KG-Knoten zur kgTuMap hinzufügen/ersetzen
     *
     * @param kgNode
     * @param title
     * @return
     */
    private KgTuListItem modifyKgNode(KgTuListItem kgNode, EtkMultiSprache title) {
        KgTuListItem newKgNode;
        if (kgNode.getPskNature() != KgTuListItem.PSK_NATURE.PSK_NONE) {
            newKgNode = kgNode;
        } else {
            newKgNode = copyKgTuListItem(kgNode, null);
        }
        newKgNode.getKgTuNode().setTitle(title);
        if (newKgNode.getPskNature() == KgTuListItem.PSK_NATURE.PSK_NONE) {
            newKgNode.setPskNature(KgTuListItem.PSK_NATURE.PSK_CHANGED_TITLE);
        }
        replaceInKgTuMap(newKgNode);
        return newKgNode;
    }

    /**
     * falls noch 'orginal' TU-Node: KG-Node und alle TU-Nodes kopieren und neuen Title bei TU-Node setzen
     * sonst nur Title beim TU-Node setzen
     * KG-Knoten und damit auch TU-Node zur kgTuMap hinzufügen/ersetzen
     *
     * @param kgNode
     * @param tuNode
     * @param title
     * @return
     */
    private KgTuListItem modifyTuNode(KgTuListItem kgNode, KgTuListItem tuNode, EtkMultiSprache title) {
        KgTuListItem newKgNode;
        if (tuNode.getPskNature() != KgTuListItem.PSK_NATURE.PSK_NONE) {
            newKgNode = kgNode;
        } else {
            newKgNode = copyKgTuListItem(kgNode, null);
        }
        KgTuListItem newTuNode = getTuNodeFromKg(newKgNode, tuNode.getKgTuId());
        if (newTuNode == null) {
            return null;
        }
        newTuNode.getKgTuNode().setTitle(title);
        if (tuNode.getPskNature() == KgTuListItem.PSK_NATURE.PSK_NONE) {
            newTuNode.setPskNature(KgTuListItem.PSK_NATURE.PSK_CHANGED_TITLE);
        } else {
            newTuNode.setPskNature(tuNode.getPskNature());
        }
        replaceInKgTuMap(newKgNode);
        return newTuNode;
    }

    /**
     * KG-Knoten: Neuer KG-Knoten ggf mit Titel geändert
     * neuen KG-Node anlegen
     * KG-Knoten zur kgTuMap hinzufügen/ersetzen
     *
     * @param kgNumber
     * @param title
     * @return
     */
    private KgTuListItem createNewKgNode(String kgNumber, EtkMultiSprache title) {
        KgTuListItem newKgNode = createNewKgTuListItem(KgTuType.KG, kgNumber, KgTuListItem.Source.PRODUCT, null);
        newKgNode.getKgTuNode().setTitle(title);
        newKgNode.setPskNature(KgTuListItem.PSK_NATURE.PSK_NEW_NODE);
        replaceInKgTuMap(newKgNode);
        return newKgNode;
    }

    /**
     * neuen TU-Node anlegen
     *
     * @param tuNumber
     * @param title
     * @param parent
     * @return
     */
    private KgTuListItem createNewTuNode(String tuNumber, EtkMultiSprache title, KgTuListItem parent) {
        KgTuListItem newTuNode = createNewKgTuListItem(KgTuType.TU, tuNumber, KgTuListItem.Source.PRODUCT, parent);
        newTuNode.getKgTuNode().setTitle(title);
        newTuNode.setPskNature(KgTuListItem.PSK_NATURE.PSK_NEW_NODE);
        return newTuNode;
    }

    /**
     * KG-Node ggf kopieren, neuen TU-Node anlegen, dem KG-Node zuweisen
     * KG-Knoten zur kgTuMap hinzufügen/ersetzen
     *
     * @param kgNode
     * @param tuNumber
     * @param title
     * @return
     */
    private KgTuListItem createNewTuNodeWithCopyKg(KgTuListItem kgNode, String tuNumber, EtkMultiSprache title) {
        KgTuListItem newKgNode = copyKgTuListItem(kgNode, null);

        KgTuListItem newTuNode = createNewTuNode(tuNumber, title, newKgNode);

        newKgNode.addChild(newTuNode);
        replaceInKgTuMap(newKgNode);
        return newTuNode;
    }

    private void replaceInKgTuMap(KgTuListItem kgNode) {
        String kgNumber = kgNode.getKgTuId().getKg();
        if (kgTuMap.get(kgNumber) != null) {
            kgTuMap.remove(kgNumber);
        }
        kgTuMap.put(kgNumber, kgNode);
    }

    /**
     * TU-Node unterhalb des KG-Nodes suchen
     *
     * @param kgListItem
     * @param tuId
     * @return
     */
    private KgTuListItem getTuNodeFromKg(KgTuListItem kgListItem, KgTuId tuId) {
        if (kgListItem.getChildren() != null) {
            for (KgTuListItem itemNode : kgListItem.getChildren()) {
                if (itemNode.getKgTuId().equals(tuId)) {
                    return itemNode;
                }
            }
        }
        return null;
    }

    /**
     * neues KgTuListItem anlegen
     *
     * @param type
     * @param number
     * @param source
     * @param parent
     * @return
     */
    private KgTuListItem createNewKgTuListItem(KgTuType type, String number, KgTuListItem.Source source, KgTuListItem parent) {
        KgTuNode kgTuParent = null;
        if (parent != null) {
            kgTuParent = parent.getKgTuNode();
        }
        KgTuNode node = new KgTuNode(type, number, kgTuParent);
        return new KgTuListItem(node, source, parent, false);
    }

    /**
     * neuen KG-Node anlegen und rekursiv die daqrunterliegenden TU-Nodes kopieren
     *
     * @param sourceItem
     * @param parent
     * @return
     */
    private KgTuListItem copyKgTuListItem(KgTuListItem sourceItem, KgTuListItem parent) {
        KgTuListItem.Source source = KgTuListItem.Source.PRODUCT;
        if (sourceItem.isSourceTemplate()) {
            source = KgTuListItem.Source.TEMPLATE;
        }
        KgTuType type = KgTuType.KG;
        String number = sourceItem.getKgTuNode().getId().getKg();
        if (sourceItem.getKgTuNode().getId().isTuNode()) {
            type = KgTuType.TU;
            number = sourceItem.getKgTuNode().getId().getTu();
        }
        KgTuListItem newKgNode = createNewKgTuListItem(type, number, source, parent);
        if (type == KgTuType.KG) {
            newKgNode.getKgTuNode().setTitle(sourceItem.getKgTuNode().getTitle().cloneMe());
            newKgNode.setPskNature(sourceItem.getPskNature());
            if (sourceItem.getChildren() != null) {
                for (KgTuListItem child : sourceItem.getChildren()) {
                    KgTuListItem newChild = copyKgTuListItem(child, newKgNode);
                    newChild.getKgTuNode().setTitle(child.getKgTuNode().getTitle().cloneMe());
                    newChild.setPskNature(child.getPskNature());
                    newKgNode.addChild(newChild);
                }
            }
        }
        return newKgNode;
    }

    private GuiButton createButton(String name, String toolTip, FrameworkImage image) {
        GuiButton button = new GuiButton();
        button.setName(name);
        button.__internal_setGenerationDpi(120);
        button.registerTranslationHandler(getUITranslationHandler());
        button.setScaleForResolution(true);
        button.setMaximumWidth(mainWindow.button_KGTUChange.getPreferredHeight());
        button.setMnemonicEnabled(true);
        button.setIcon(image);
        button.setTooltip(toolTip);
        return button;
    }

    /**
     * Überschreiben des Cancel-Button Textes
     *
     * @param text
     */
    public void setCancelButtonText(String text) {
        mainWindow.buttonpanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.CANCEL).setText(text);
    }

    /**
     * Überschreiben des Cancel-Button Textes und des Modal-Results
     *
     * @param text
     * @param modalResult
     */
    public void setCancelButtonText(String text, ModalResult modalResult) {
        setCancelButtonText(text);
        mainWindow.buttonpanel.setButtonModalResult(GuiButtonOnPanel.ButtonType.CANCEL, modalResult);
    }

    /**
     * Callback für Produkt-Combobox
     * (=> Setzen vom Produkt und Besetzen der KG- und TU-Comboboxen)
     *
     * @param event
     */
    private void onChangeProductCallback(Event event) {
        startKgTuId = new KgTuId(StrUtils.getEmptyOrValidString(getSelectedKGNumber()), StrUtils.getEmptyOrValidString(getSelectedTUNumber()));
        setProductId(comboboxProduct.getSelectedUserObject());
    }

    /**
     * Callback für KG-Combobox
     * (=> besetzen der TU-Combobox)
     *
     * @param event
     */
    private void onChangeKGCallback(Event event) {
        String numberKGNode = getSelectedKGNumber();
        boolean isValidKG = !StrUtils.isEmpty(numberKGNode);
        String toolTip = KG_TOOLTIP;
        if (isValidKG) {
            KgTuListItem kgNode = kgTuMap.get(numberKGNode);
            if (kgNode != null) {
                fillComboBoxItems(comboboxTU, kgNode.getChildren());
                fireOnChangeEvent(event);
                buttonPSKTUEdit.setEnabled(false);
                buttonPSKTUNew.setEnabled(true);
                if (isInTranslation(kgNode)) {
                    toolTip = KG_TOOLTIP_IN_TRANLATION;
                }
            }
        }
        buttonPSKKGEdit.setTooltip(toolTip);
        buttonPSKKGEdit.setEnabled(isValidKG);
    }

    /**
     * Callback für TU-Combobox
     * (i.A. not used)
     *
     * @param event
     */
    private void onChangeTUCallback(Event event) {
        String numberTUNode = getSelectedTUNumber();
        boolean isValidTU = !StrUtils.isEmpty(numberTUNode);
        String toolTip = TU_TOOLTIP;
        if (isValidTU) {
            KgTuListItem tuNode = getSelectedUserObject(comboboxTU);
            if (isInTranslation(tuNode)) {
                toolTip = TU_TOOLTIP_IN_TRANLATION;
            }
        }
        buttonPSKTUEdit.setTooltip(toolTip);
        buttonPSKTUEdit.setEnabled(isValidTU);
        fireOnChangeEvent(event);
    }

    private boolean isInTranslation(KgTuListItem kgTuListItem) {
        return isInTranslation(getProject(), kgTuListItem.getKgTuNode());
    }

    private static boolean isInTranslation(EtkProject project, KgTuNode kgTuNode) {
        iPartsDictTextKindId textKindId = DictTxtKindIdByMADId.getInstance(project).getTxtKindId(DictTextKindTypes.KG_TU_NAME);
        String textId = kgTuNode.getTitle().getTextId();
        iPartsDataDictMeta dataDictMeta = KgTuListItem.findDictData(project, textKindId, textId, iPartsImportDataOrigin.UNKNOWN);
        if (dataDictMeta != null) {
            return dataDictMeta.isInTranslationWorkflow();
        }
        return false;
    }

    /**
     * @param event
     */
    private void fireOnChangeEvent(Event event) {
        for (EventListener listener : eventListeners.getListeners(Event.ON_CHANGE_EVENT)) {
            listener.fire(event);
        }
        enableButtons();
    }

    private void enableButtons() {
        mainWindow.buttonpanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, isValid());
    }

    /* Hilfsroutinen */

    /**
     * Setzen des SelectIndex einer Combobox by EinPasNode
     *
     * @param combobox
     * @param node
     */
    private void setSelectedIndexByNode(RComboBox<KgTuListItem> combobox, KgTuListItem node) {
        if (node != null) {
            for (KgTuListItem kgTuListItem : combobox.getUserObjects()) {
                if (kgTuListItem.compareTo(node) == 0) {
                    combobox.setSelectedUserObject(kgTuListItem);
                    return;
                }
            }
        }
    }

    /**
     * liefert die selektierte KG-Nummer
     *
     * @return
     */
    private String getSelectedKGNumber() {
        return getSelectedNumber(comboboxKG);
    }

    /**
     * liefert die selektierte TU-Nummer
     *
     * @return
     */
    private String getSelectedTUNumber() {
        return getSelectedNumber(comboboxTU);
    }

    /**
     * liefert die Selektierte EinPAS Nummer einer Combobox
     *
     * @param combobox
     * @return
     */
    private String getSelectedNumber(RComboBox<KgTuListItem> combobox) {
        KgTuListItem selectedNode = getSelectedUserObject(combobox);
        if (selectedNode != null) {
            return selectedNode.getKgTuNode().getNumber();
        }
        return null;
    }

    /**
     * liefert das selektierte Userobject einer Combobox
     *
     * @param combobox
     * @return
     */
    private KgTuListItem getSelectedUserObject(RComboBox<KgTuListItem> combobox) {
        return combobox.getSelectedUserObject();
    }

    /**
     * Füllen der Items einer Combobox
     *
     * @param combobox
     * @param nodes
     */
    private void fillComboBoxItems(RComboBox<KgTuListItem> combobox, Collection<KgTuListItem> nodes) {
        combobox.switchOffEventListeners();
        combobox.removeAllItems();
        if (nodes != null) {
            for (KgTuListItem nodeItem : nodes) {
                combobox.addItem(nodeItem, buildKgTuComboText(nodeItem, startLanguage, getProject().getDataBaseFallbackLanguages()));
            }
            combobox.setMinimumWidth(nodes.isEmpty() ? 320 : 10);
            combobox.setEnabled(true);
        } else {
            combobox.setEnabled(false);
        }
        combobox.setSelectedIndex(-1);
        combobox.switchOnEventListeners();
        if (combobox == comboboxTU) {
            buttonPSKTUNew.setEnabled(combobox.isEnabled());
        }
    }

    /**
     * hier wird der Text für die Comboboxen erzeugt
     * (kann überschrieben werden)
     *
     * @param nodeItem
     * @param language
     * @return
     */
    static public String buildKgTuComboText(KgTuListItem nodeItem, String language, List<String> fallbackLanguages) {
        if (nodeItem != null) {
            if (nodeItem.isSourceTemplate()) {
                return (nodeItem.getKgTuNode().getNumberAndTitle(language, fallbackLanguages) + " *");
            } else {
                return nodeItem.getKgTuNode().getNumberAndTitle(language, fallbackLanguages);
            }
        }
        return "";
    }


    /**
     * Datenklasse für das selektierte Produkt und den selektierten KG/TU.
     */
    public static class ProductKgTuSelection {

        private iPartsProductId productId;
        private KgTuListItem kgTuListItem;
        private boolean openTU;

        public ProductKgTuSelection(iPartsProductId productId, KgTuListItem kgTuListItem, boolean openTU) {
            this.productId = productId;
            this.kgTuListItem = kgTuListItem;
            this.openTU = openTU;
        }

        public iPartsProductId getProductId() {
            return productId;
        }

        public KgTuListItem getKgTuListItem() {
            return kgTuListItem;
        }

        public boolean isOpenTU() {
            return openTU;
        }
    }

    private static class EditUserControlForKgTu extends EditUserControlForCreate {

        private boolean isNew;
        private boolean isKG;
        private List<String> usedNodeNumbers;

        public EditUserControlForKgTu(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName,
                                      IdWithType id, DBDataObjectAttributes attributes, EtkEditFields externalEditFields,
                                      boolean isNew, boolean isKG) {
            super(dataConnector, parentForm, tableName, id, attributes, externalEditFields);
            this.isNew = isNew;
            this.isKG = isKG;
            setUsedNodeNumbers(null);
        }

        public void setUsedNodeNumbers(List<String> usedNodeNumbers) {
            this.usedNodeNumbers = usedNodeNumbers;
            if (this.usedNodeNumbers == null) {
                this.usedNodeNumbers = new DwList<>();
            }
        }

        @Override
        protected boolean checkPkValuesForModified() {
            boolean result = super.checkPkValuesForModified();
            String fieldName;
            int maxLen;
            if (isKG) {
                fieldName = iPartsConst.FIELD_DA_DKT_KG;
                maxLen = 2;
            } else {
                fieldName = iPartsConst.FIELD_DA_DKT_TU;
                maxLen = 3;
            }
            if (result) {
                if (isNew) {
                    // KG oder TU Nummer überprüfen
                    result = checkKgOrTuNode(fieldName, maxLen);
                }
            } else {
                if (isNew) {
                    // KG oder TU Nummer überprüfen
                    checkKgOrTuNode(fieldName, maxLen);
                }
            }
            return result;
        }

        private String formatKgTuNumber(String nodeNumber, int maxLen) {
            return StrUtils.leftFill(nodeNumber, maxLen, '0');
        }

        private boolean checkKgOrTuNode(String fieldName, int maxLen) {
            boolean result = true;
            DBDataObjectAttribute attrib = getCurrentAttributeValue(fieldName);
            if (attrib == null) {
                return result;
            }
            String nodeNumber = attrib.getAsString();
            result = (nodeNumber.length() > 0) && (nodeNumber.length() <= maxLen);
            if (result) {
                nodeNumber = formatKgTuNumber(nodeNumber, maxLen);
                result = !usedNodeNumbers.contains(nodeNumber);
                if (!result) {
                    String keyWord = "KG";
                    if (fieldName.equals(iPartsConst.FIELD_DA_DKT_TU)) {
                        keyWord = "TU";
                    }
                    setEditControlTooltip(fieldName, TranslationHandler.translate("!!%1 Nummer \"%2\" ist bereits belegt", keyWord, nodeNumber));
                } else {
                    setEditControlTooltip(fieldName, "");
                }
            } else {
                if (nodeNumber.length() > maxLen) {
                    String keyWord = "KG";
                    if (fieldName.equals(iPartsConst.FIELD_DA_DKT_TU)) {
                        keyWord = "TU";
                    }
                    setEditControlTooltip(fieldName, TranslationHandler.translate("!!%1 Nummer \"%2\" muss %3-stellig sein", keyWord, nodeNumber, String.valueOf(maxLen)));
                } else {
                    setEditControlTooltip(fieldName, "");
                }
            }
            return result;
        }

        private void setEditControlTooltip(String fieldName, String msg) {
            EditControl control = getEditControlByFieldName(fieldName);
            if (StrUtils.isValid(msg)) {
                control.getAbstractGuiControl().setBackgroundColor(Colors.clDesignErrorBackground);
            } else {
                control.getAbstractGuiControl().setBackgroundColor(Colors.clDesignTextFieldEnabledBackground);
            }
            control.getAbstractGuiControl().setTooltip(msg);
        }

        @Override
        protected void modifyEditControl(EditControl ctrl, EtkEditField field, String initialValue, EtkDataArray initialDataArray) {
            String fieldName = field.getKey().getFieldName();
            if (fieldName.equals(iPartsConst.FIELD_DA_DKT_KG)) {
                iPartsGuiHotSpotTextField textField = new iPartsGuiHotSpotTextField();
                ctrl.getEditControl().setControl(textField);
                textField.setText(initialValue);
            } else if (fieldName.equals(iPartsConst.FIELD_DA_DKT_TU)) {
                iPartsGuiHotSpotTextField textField = new iPartsGuiHotSpotTextField();
                ctrl.getEditControl().setControl(textField);
                textField.setText(initialValue);
            } else if (fieldName.equals(iPartsConst.FIELD_DA_DKT_DESC)) {
                // Edit-Control für KG/TU-Text tauschen
                GuiMultiLangEditDict langEditDict = new GuiMultiLangEditDict();
                langEditDict.setDataConnector(getConnector());
                EditControlFactory.setDefaultLayout(langEditDict);
                langEditDict.setStartLanguage(Language.findLanguage(ctrl.getEditControl().getValues().dbLanguage));
                langEditDict.setSearchTextKindTypes(EnumSet.of(DictTextKindTypes.KG_TU_NAME));
                langEditDict.setTableForDictionary(iPartsConst.TABLE_DA_KGTU_TEMPLATE);
                langEditDict.setForeignSourceForCreate(iPartsImportDataOrigin.PSK.getOrigin());
                ctrl.getEditControl().setControl(langEditDict);
            }
        }

        @Override
        protected void fillAttribByEditControlValue(int index, EtkEditField field, DBDataObjectAttribute attrib) {
            String fieldName = field.getKey().getFieldName();
            int maxLen = -1;
            if (fieldName.equals(iPartsConst.FIELD_DA_DKT_KG)) {
                maxLen = 2;
            } else if (fieldName.equals(iPartsConst.FIELD_DA_DKT_TU)) {
                maxLen = 3;
            } else {
                super.fillAttribByEditControlValue(index, field, attrib);
                return;
            }
            EditControl controlByFeldIndex = editControls.getControlByFeldIndex(index);
            if (controlByFeldIndex == null) {
                return;
            }
            EditControlFactory ctrl = controlByFeldIndex.getEditControl();
            String value = ctrl.getText();
            value = formatKgTuNumber(value, maxLen);
            attrib.setValueAsString(value, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);

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
        private de.docware.framework.modules.gui.controls.GuiPanel panel_KGTU;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_product;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<KgTuListItem> combobox_product;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_KG;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<KgTuListItem> combobox_KGTU_KG;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton button_KGTUChange;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_TU;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<KgTuListItem> combobox_KGTU_TU;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_infoSpacer;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_info;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiCheckbox checkbox_open_TU;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setName("EditKGTUDialog");
            this.setWidth(700);
            this.setHeight(200);
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
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleConstraints.setPosition("north");
            title.setConstraints(titleConstraints);
            this.addChild(title);
            panel_KGTU = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_KGTU.setName("panel_KGTU");
            panel_KGTU.__internal_setGenerationDpi(96);
            panel_KGTU.registerTranslationHandler(translationHandler);
            panel_KGTU.setScaleForResolution(true);
            panel_KGTU.setMinimumWidth(10);
            panel_KGTU.setMinimumHeight(10);
            panel_KGTU.setPaddingRight(8);
            de.docware.framework.modules.gui.layout.LayoutGridBag panel_KGTULayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panel_KGTULayout.setCentered(false);
            panel_KGTU.setLayout(panel_KGTULayout);
            label_product = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_product.setName("label_product");
            label_product.__internal_setGenerationDpi(96);
            label_product.registerTranslationHandler(translationHandler);
            label_product.setScaleForResolution(true);
            label_product.setMinimumWidth(10);
            label_product.setMinimumHeight(10);
            label_product.setText("!!Produkt");
            label_product.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_productConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "e", "n", 8, 8, 8, 4);
            label_product.setConstraints(label_productConstraints);
            panel_KGTU.addChild(label_product);
            combobox_product = new de.docware.framework.modules.gui.controls.GuiComboBox<KgTuListItem>();
            combobox_product.setName("combobox_product");
            combobox_product.__internal_setGenerationDpi(96);
            combobox_product.registerTranslationHandler(translationHandler);
            combobox_product.setScaleForResolution(true);
            combobox_product.setMinimumWidth(10);
            combobox_product.setMinimumHeight(10);
            combobox_product.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onChangeProductCallback(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag combobox_productConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, "c", "b", 8, 4, 8, 0);
            combobox_product.setConstraints(combobox_productConstraints);
            panel_KGTU.addChild(combobox_product);
            label_KG = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_KG.setName("label_KG");
            label_KG.__internal_setGenerationDpi(96);
            label_KG.registerTranslationHandler(translationHandler);
            label_KG.setScaleForResolution(true);
            label_KG.setMinimumWidth(10);
            label_KG.setMinimumHeight(10);
            label_KG.setText("!!Konstruktionsgruppe");
            label_KG.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_KGConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "e", "n", 4, 8, 4, 4);
            label_KG.setConstraints(label_KGConstraints);
            panel_KGTU.addChild(label_KG);
            combobox_KGTU_KG = new de.docware.framework.modules.gui.controls.GuiComboBox<KgTuListItem>();
            combobox_KGTU_KG.setName("combobox_KGTU_KG");
            combobox_KGTU_KG.__internal_setGenerationDpi(96);
            combobox_KGTU_KG.registerTranslationHandler(translationHandler);
            combobox_KGTU_KG.setScaleForResolution(true);
            combobox_KGTU_KG.setMinimumWidth(10);
            combobox_KGTU_KG.setMinimumHeight(10);
            combobox_KGTU_KG.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onChangeKGCallback(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag combobox_KGTU_KGConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 1, 1, 1, 0.0, 0.0, "c", "b", 4, 4, 4, 0);
            combobox_KGTU_KG.setConstraints(combobox_KGTU_KGConstraints);
            panel_KGTU.addChild(combobox_KGTU_KG);
            button_KGTUChange = new de.docware.framework.modules.gui.controls.GuiButton();
            button_KGTUChange.setName("button_KGTUChange");
            button_KGTUChange.__internal_setGenerationDpi(96);
            button_KGTUChange.registerTranslationHandler(translationHandler);
            button_KGTUChange.setScaleForResolution(true);
            button_KGTUChange.setMinimumWidth(5);
            button_KGTUChange.setMinimumHeight(10);
            button_KGTUChange.setMaximumWidth(30);
            button_KGTUChange.setMnemonicEnabled(true);
            button_KGTUChange.setText("...");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag button_KGTUChangeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(2, 1, 1, 1, 0.0, 0.0, "c", "v", 4, 0, 4, 0);
            button_KGTUChange.setConstraints(button_KGTUChangeConstraints);
            panel_KGTU.addChild(button_KGTUChange);
            label_TU = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_TU.setName("label_TU");
            label_TU.__internal_setGenerationDpi(96);
            label_TU.registerTranslationHandler(translationHandler);
            label_TU.setScaleForResolution(true);
            label_TU.setMinimumWidth(10);
            label_TU.setMinimumHeight(10);
            label_TU.setText("!!Technischer Umfang");
            label_TU.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_TUConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 0.0, 0.0, "e", "n", 4, 8, 4, 4);
            label_TU.setConstraints(label_TUConstraints);
            panel_KGTU.addChild(label_TU);
            combobox_KGTU_TU = new de.docware.framework.modules.gui.controls.GuiComboBox<KgTuListItem>();
            combobox_KGTU_TU.setName("combobox_KGTU_TU");
            combobox_KGTU_TU.__internal_setGenerationDpi(96);
            combobox_KGTU_TU.registerTranslationHandler(translationHandler);
            combobox_KGTU_TU.setScaleForResolution(true);
            combobox_KGTU_TU.setMinimumWidth(10);
            combobox_KGTU_TU.setMinimumHeight(10);
            combobox_KGTU_TU.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onChangeTUCallback(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag combobox_KGTU_TUConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 2, 1, 1, 100.0, 0.0, "c", "b", 4, 4, 4, 0);
            combobox_KGTU_TU.setConstraints(combobox_KGTU_TUConstraints);
            panel_KGTU.addChild(combobox_KGTU_TU);
            label_infoSpacer = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_infoSpacer.setName("label_infoSpacer");
            label_infoSpacer.__internal_setGenerationDpi(96);
            label_infoSpacer.registerTranslationHandler(translationHandler);
            label_infoSpacer.setScaleForResolution(true);
            label_infoSpacer.setMinimumWidth(10);
            label_infoSpacer.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_infoSpacerConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 3, 1, 1, 0.0, 0.0, "c", "n", 0, 0, 0, 0);
            label_infoSpacer.setConstraints(label_infoSpacerConstraints);
            panel_KGTU.addChild(label_infoSpacer);
            label_info = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_info.setName("label_info");
            label_info.__internal_setGenerationDpi(96);
            label_info.registerTranslationHandler(translationHandler);
            label_info.setScaleForResolution(true);
            label_info.setMinimumWidth(10);
            label_info.setMinimumHeight(10);
            label_info.setVisible(false);
            label_info.setText("!!* Standard-Benennung (bisher keine Verwendung im Produkt)");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_infoConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 3, 1, 1, 0.0, 0.0, "w", "h", 4, 4, 4, 0);
            label_info.setConstraints(label_infoConstraints);
            panel_KGTU.addChild(label_info);
            checkbox_open_TU = new de.docware.framework.modules.gui.controls.GuiCheckbox();
            checkbox_open_TU.setName("checkbox_open_TU");
            checkbox_open_TU.__internal_setGenerationDpi(96);
            checkbox_open_TU.registerTranslationHandler(translationHandler);
            checkbox_open_TU.setScaleForResolution(true);
            checkbox_open_TU.setMinimumWidth(10);
            checkbox_open_TU.setMinimumHeight(10);
            checkbox_open_TU.setVisible(false);
            checkbox_open_TU.setName("checkbox_open_TU");
            checkbox_open_TU.setText("!!Nach dem Speichern technischen Umfang zur Bearbeitung öffnen");
            checkbox_open_TU.setSelected(true);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag checkbox_open_TUConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 4, 1, 1, 0.0, 0.0, "w", "h", 4, 4, 4, 0);
            checkbox_open_TU.setConstraints(checkbox_open_TUConstraints);
            panel_KGTU.addChild(checkbox_open_TU);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panel_KGTUConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panel_KGTU.setConstraints(panel_KGTUConstraints);
            this.addChild(panel_KGTU);
            buttonpanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonpanel.setName("buttonpanel");
            buttonpanel.__internal_setGenerationDpi(96);
            buttonpanel.registerTranslationHandler(translationHandler);
            buttonpanel.setScaleForResolution(true);
            buttonpanel.setMinimumWidth(10);
            buttonpanel.setMinimumHeight(10);
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