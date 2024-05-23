/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPas;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditFormComboboxHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiComboBox;
import de.docware.framework.modules.gui.controls.GuiTextField;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.misc.DWFontStyle;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListeners;
import de.docware.framework.utils.EtkMultiSprache;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Dialog zur Eingabe eines EinPas-Knotens (2. Version)
 */
public class EditEinPas2Dialog extends AbstractJavaViewerForm {

    // Defaultwerte
    private String startLanguageDefaultValue = Language.DE.getCode();

    // Spezifische Eigenschaften der Komponente
    private String startLanguage = startLanguageDefaultValue;

    // Weitere benötigte Variablen

    // Die Instanz die die Aktionen abarbeitet
    protected EventListeners eventListeners;
    private EinPas einPas = null;
    private EinPasId startEinPasId = null;
    private boolean isReadOnly = false;

    // TextFelder für ReadOnly Betrieb
    private GuiTextField textfield_EinPAS_HG = null;
    private GuiTextField textfield_EinPAS_G = null;
    private GuiTextField textfield_EinPAS_TU = null;

    /**
     * Methode zur Anzeige und Ausführung des Dialogs mit der skipOption
     *
     * @param dataConnector
     * @param parentForm
     * @param startEinPasId Vorbesetzung der EinPasId oder null
     * @param withSkip      true: der Cancel-Button zeigt 'Überspringen' und liefert ModalResult.OK
     *                      damit kann der Returnwert auch null sein
     * @return gültige EinPasId oder null (Vorsicht bei Option withSkip)
     */
    public static EinPasId showEinPasDialogWithSkipOption(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                          EinPasId startEinPasId, boolean withSkip) {
        EditEinPas2Dialog einPasDlg = new EditEinPas2Dialog(dataConnector, parentForm, null);
        if (withSkip) {
            einPasDlg.setCancelButtonText("!!Überspringen", ModalResult.IGNORE);
        }
        einPasDlg.setStartEinPasId(startEinPasId);
        if (einPasDlg.showModal() == ModalResult.OK) {
            return einPasDlg.getEinPasId();
        }
        return null;
    }

    /**
     * Methode zur Anzeige und Ausführung des Dialogs
     *
     * @param dataConnector
     * @param parentForm
     * @param startEinPasId Vorbesetzung der EinPasId oder null
     * @return gültige EinPasId oder null
     */
    public static EinPasId showEinPasDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                            EinPasId startEinPasId) {
        return showEinPasDialogWithSkipOption(dataConnector, parentForm, startEinPasId, false);
    }

    /**
     * Methode zur Anzeige und Ausführung des Dialogs mit skip
     *
     * @param dataConnector
     * @param parentForm
     * @param startEinPasId Vorbesetzung der EinPasId oder null
     * @return gültige EinPasId oder null
     */
    public static EinPasId showEinPasDialogWithSkip(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                    EinPasId startEinPasId) {
        return showEinPasDialogWithSkipOption(dataConnector, parentForm, startEinPasId, true);
    }


    /**
     * Erzeugt eine Instanz von EditEinPas2Dialog.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditEinPas2Dialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, EinPasId startEinPasId) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.startEinPasId = startEinPasId;
        this.eventListeners = new EventListeners();
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        setTitle("!!EinPAS Verortung festlegen");
        // zusätzlichen Button verstecken
        setButtonEinPASChangeListener(null);
        init();
    }

    private void init() {
        // EinPas initialisieren
        einPas = EinPas.getInstance(getProject());
        Collection<EinPasNode> hgNodes = einPas.getHGNodeList();

        // besetzen der HG-Combobox + Vorbereitung G-/TU-Combobox
        fillComboBoxItems(mainWindow.combobox_EinPAS_HG, hgNodes);
        EditFormComboboxHelper.clearComboBox(mainWindow.combobox_EinPAS_G);
        EditFormComboboxHelper.clearComboBox(mainWindow.combobox_EinPAS_TU);
        setStartEinPasId(startEinPasId);
        enableButtons();
    }


    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public ModalResult showModal() {
        return mainWindow.showModal();
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    /**
     * zusätzliche Überschrift auf dem EinPAS-Panel
     *
     * @param title
     */
    public void setPanelTitle(String title) {
        mainWindow.panel_EinPAS.setTitle(title);
    }

    public void setPanelTitleFontStyle(DWFontStyle style) {
        // der title-Fontstyle kann i.A. nicht geändertwerden => ändere Fontstyle der Labels
        mainWindow.label_EinPAS_HG.setFontStyle(style);
        mainWindow.label_EinPAS_G.setFontStyle(style);
        mainWindow.label_EinPAS_TU.setFontStyle(style);
    }

    public String getStartLanguage() {
        return startLanguage;
    }

    public void setStartLanguage(String startLanguage) {
        this.startLanguage = startLanguage;
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
                boolean setNames = (textfield_EinPAS_HG == null);
                textfield_EinPAS_HG = EditFormComboboxHelper.replaceComboBoxByTextField(mainWindow.combobox_EinPAS_HG, textfield_EinPAS_HG, mainWindow.panel_EinPAS);
                textfield_EinPAS_G = EditFormComboboxHelper.replaceComboBoxByTextField(mainWindow.combobox_EinPAS_G, textfield_EinPAS_G, mainWindow.panel_EinPAS);
                textfield_EinPAS_TU = EditFormComboboxHelper.replaceComboBoxByTextField(mainWindow.combobox_EinPAS_TU, textfield_EinPAS_TU, mainWindow.panel_EinPAS);
                if (setNames) {
                    textfield_EinPAS_HG.setName("textfield_EinPAS_HG");
                    textfield_EinPAS_G.setName("textfield_EinPAS_G");
                    textfield_EinPAS_TU.setName("textfield_EinPAS_TU");
                }
            } else {
                EditFormComboboxHelper.replaceTextFieldByComboBox(textfield_EinPAS_HG, mainWindow.combobox_EinPAS_HG, mainWindow.panel_EinPAS);
                EditFormComboboxHelper.replaceTextFieldByComboBox(textfield_EinPAS_G, mainWindow.combobox_EinPAS_G, mainWindow.panel_EinPAS);
                EditFormComboboxHelper.replaceTextFieldByComboBox(textfield_EinPAS_TU, mainWindow.combobox_EinPAS_TU, mainWindow.panel_EinPAS);
            }
            isReadOnly = value;
        }
    }

    /**
     * Überprüfungsfunktion, ob EinPasPanel initialisiert ist
     *
     * @return
     */
    public boolean isInit() {
        return einPas != null;
    }

    /**
     * Test, ob alle EinPas Felder besetzt sind
     *
     * @return
     */
    public boolean isValid() {
        if (isInit()) {
            return (getSelectedHGNumber() != null) && (getSelectedGNumber() != null) &&
                   (getSelectedTUNumber() != null);
        }
        return false;
    }

    /**
     * Selektierte HG-Nummer holen
     *
     * @return
     */
    public String getHG() {
        if (isValid()) {
            return getSelectedHGNumber();
        }
        return null;
    }

    /**
     * Selektierte G-Nummer holen
     *
     * @return
     */
    public String getG() {
        if (isValid()) {
            return getSelectedGNumber();
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
     * selektierten TU-Description holen
     *
     * @return
     */
    public EtkMultiSprache getTUDescription() {
        String tuName = getTU();
        if ((tuName != null) && !tuName.isEmpty()) {
            EinPasNode node = getSelectedUserObject(mainWindow.combobox_EinPAS_TU);
            if (node != null) {
                return node.getTitle().cloneMe();
            }
        }
        return null;
    }

    /**
     * Liefert Ergebnis als EinPasId
     *
     * @return
     */
    public EinPasId getEinPasId() {
        if (isValid()) {
            return new EinPasId(getSelectedHGNumber(), getSelectedGNumber(), getSelectedTUNumber());
        }
        return null;
    }

    /**
     * neue EinPasId setzen
     *
     * @param startEinPasId
     */
    public void setStartEinPasId(EinPasId startEinPasId) {
        if (startEinPasId != null) {
            if (isInit()) {
                EinPasNode node = einPas.getHGNode(startEinPasId.getHg());
                setSelectedIndexByNode(mainWindow.combobox_EinPAS_HG, node);
                node = einPas.getGNode(startEinPasId.getHg(), startEinPasId.getG());
                setSelectedIndexByNode(mainWindow.combobox_EinPAS_G, node);
                node = einPas.getTuNode(startEinPasId.getHg(), startEinPasId.getG(), startEinPasId.getTu());
                setSelectedIndexByNode(mainWindow.combobox_EinPAS_TU, node);
                EditFormComboboxHelper.setTextFieldText(textfield_EinPAS_HG, mainWindow.combobox_EinPAS_HG);
                EditFormComboboxHelper.setTextFieldText(textfield_EinPAS_G, mainWindow.combobox_EinPAS_G);
                EditFormComboboxHelper.setTextFieldText(textfield_EinPAS_TU, mainWindow.combobox_EinPAS_TU);
                this.startEinPasId = null;
            } else {
                this.startEinPasId = startEinPasId;
            }
        }
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
    public void setButtonEinPASChangeListener(EventListener eventListener) {
        if (eventListener != null) {
            mainWindow.button_EinPASChange.addEventListener(eventListener);
            mainWindow.button_EinPASChange.setVisible(true);
        } else {
            mainWindow.button_EinPASChange.removeEventListeners(Event.ACTION_PERFORMED_EVENT);
            mainWindow.button_EinPASChange.setVisible(false);
        }
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
     * Callback für HG-Combobox
     * (=> besetzen der G-Combobox)
     *
     * @param event
     */
    private void onChange_CB_HG(Event event) {
        String numberHGNode = getSelectedHGNumber();
        if (numberHGNode != null) {
            Collection<EinPasNode> gNodes = einPas.getGNodeList(numberHGNode);
            fillComboBoxItems(mainWindow.combobox_EinPAS_G, gNodes);
            EditFormComboboxHelper.clearComboBox(mainWindow.combobox_EinPAS_TU);
            fireOnChangeEvent(event);
        }
    }

    /**
     * Callback für G-Combobox
     * (=> besetzen der TU-Combobox)
     *
     * @param event
     */
    private void onChange_CB_G(Event event) {
        String numberHGNode = getSelectedHGNumber();
        String numberGNode = getSelectedGNumber();
        if ((numberHGNode != null) && (numberGNode != null)) {
            Collection<EinPasNode> tuNodes = einPas.getTUNodeList(numberHGNode, numberGNode);
            fillComboBoxItems(mainWindow.combobox_EinPAS_TU, tuNodes);
            fireOnChangeEvent(event);
        }
    }

    /**
     * Callback für TU-Combobox
     * (i.A. not used)
     *
     * @param event
     */
    private void onChange_CB_TU(Event event) {
        fireOnChangeEvent(event);
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
    private void setSelectedIndexByNode(GuiComboBox<EinPasNode> combobox, EinPasNode node) {
        if (node != null) {
            int index = -1;
            for (int lfdNr = 0; lfdNr < combobox.getItemCount(); lfdNr++) {
                EinPasNode actNode = combobox.getUserObject(lfdNr);
                if (actNode.getNumber().equals(node.getNumber())) {
                    index = lfdNr;
                    break;
                }
            }
            if (index != -1) {
                combobox.setSelectedIndex(index);
            }
        }
    }

    /**
     * liefert das selektierte Userobject einer Combobox
     *
     * @param combobox
     * @return
     */
    private EinPasNode getSelectedUserObject(GuiComboBox<EinPasNode> combobox) {
        int index = combobox.getSelectedIndex();
        if (index != -1) {
            return combobox.getUserObject(index);
        }
        return null;
    }

    /**
     * liefert die Selektierte EinPAS Nummer einer Combobox
     *
     * @param combobox
     * @return
     */
    private String getSelectedNumber(GuiComboBox<EinPasNode> combobox) {
        EinPasNode selectedNode = getSelectedUserObject(combobox);
        if (selectedNode != null) {
            return selectedNode.getNumber();
        }
        return null;
    }

    /**
     * liefert die selektierte HG-Nummer
     *
     * @return
     */
    private String getSelectedHGNumber() {
        return getSelectedNumber(mainWindow.combobox_EinPAS_HG);
    }

    /**
     * liefert die selektierte G-Nummer
     *
     * @return
     */
    private String getSelectedGNumber() {
        return getSelectedNumber(mainWindow.combobox_EinPAS_G);
    }

    /**
     * liefert die selektierte TU-Nummer
     *
     * @return
     */
    private String getSelectedTUNumber() {
        return getSelectedNumber(mainWindow.combobox_EinPAS_TU);
    }

    /**
     * Füllen der Items einer Combobox
     *
     * @param combobox
     * @param nodes
     */
    private void fillComboBoxItems(GuiComboBox<EinPasNode> combobox, Collection<EinPasNode> nodes) {
        combobox.switchOffEventListeners();
        combobox.removeAllItems();
        if (nodes != null) {
            Iterator<EinPasNode> iter = nodes.iterator();
            while (iter.hasNext()) {
                EinPasNode node = iter.next();
                combobox.addItem(node, buildEinPasComboText(node, getProject().getDBLanguage(), getProject().getDataBaseFallbackLanguages()));
            }
            combobox.setEnabled(true);
        } else {
            combobox.setEnabled(false);
        }
        combobox.setSelectedIndex(-1);
        combobox.switchOnEventListeners();
    }

    /**
     * hier wird der Text für die Comboboxen erzeugt
     * (kann überschrieben werden)
     *
     * @param node
     * @param language
     * @return
     */
    static public String buildEinPasComboText(EinPasNode node, String language, List<String> fallbackLanguages) {
        if (node != null) {
            return node.getNumberAndTitle(language, fallbackLanguages);
        }
        return "";
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
        private de.docware.framework.modules.gui.controls.GuiPanel panel_EinPAS;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_EinPAS_HG;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasNode> combobox_EinPAS_HG;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_EinPAS_G;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasNode> combobox_EinPAS_G;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_EinPAS_TU;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasNode> combobox_EinPAS_TU;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton button_EinPASChange;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_0;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setWidth(700);
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
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMain.setLayout(panelMainLayout);
            panel_EinPAS = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_EinPAS.setName("panel_EinPAS");
            panel_EinPAS.__internal_setGenerationDpi(96);
            panel_EinPAS.registerTranslationHandler(translationHandler);
            panel_EinPAS.setScaleForResolution(true);
            panel_EinPAS.setMinimumWidth(10);
            panel_EinPAS.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panel_EinPASLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panel_EinPAS.setLayout(panel_EinPASLayout);
            label_EinPAS_HG = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_EinPAS_HG.setName("label_EinPAS_HG");
            label_EinPAS_HG.__internal_setGenerationDpi(96);
            label_EinPAS_HG.registerTranslationHandler(translationHandler);
            label_EinPAS_HG.setScaleForResolution(true);
            label_EinPAS_HG.setMinimumWidth(10);
            label_EinPAS_HG.setMinimumHeight(10);
            label_EinPAS_HG.setText("!!Hauptgruppe");
            label_EinPAS_HG.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_EinPAS_HGConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "e", "n", 8, 8, 4, 4);
            label_EinPAS_HG.setConstraints(label_EinPAS_HGConstraints);
            panel_EinPAS.addChild(label_EinPAS_HG);
            combobox_EinPAS_HG = new de.docware.framework.modules.gui.controls.GuiComboBox<de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasNode>();
            combobox_EinPAS_HG.setName("combobox_EinPAS_HG");
            combobox_EinPAS_HG.__internal_setGenerationDpi(96);
            combobox_EinPAS_HG.registerTranslationHandler(translationHandler);
            combobox_EinPAS_HG.setScaleForResolution(true);
            combobox_EinPAS_HG.setMinimumWidth(10);
            combobox_EinPAS_HG.setMinimumHeight(10);
            combobox_EinPAS_HG.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onChange_CB_HG(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag combobox_EinPAS_HGConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 100.0, 0.0, "c", "h", 8, 4, 4, 4);
            combobox_EinPAS_HG.setConstraints(combobox_EinPAS_HGConstraints);
            panel_EinPAS.addChild(combobox_EinPAS_HG);
            label_EinPAS_G = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_EinPAS_G.setName("label_EinPAS_G");
            label_EinPAS_G.__internal_setGenerationDpi(96);
            label_EinPAS_G.registerTranslationHandler(translationHandler);
            label_EinPAS_G.setScaleForResolution(true);
            label_EinPAS_G.setMinimumWidth(10);
            label_EinPAS_G.setMinimumHeight(10);
            label_EinPAS_G.setText("!!Gruppe");
            label_EinPAS_G.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_EinPAS_GConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "e", "n", 4, 8, 4, 4);
            label_EinPAS_G.setConstraints(label_EinPAS_GConstraints);
            panel_EinPAS.addChild(label_EinPAS_G);
            combobox_EinPAS_G = new de.docware.framework.modules.gui.controls.GuiComboBox<de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasNode>();
            combobox_EinPAS_G.setName("combobox_EinPAS_G");
            combobox_EinPAS_G.__internal_setGenerationDpi(96);
            combobox_EinPAS_G.registerTranslationHandler(translationHandler);
            combobox_EinPAS_G.setScaleForResolution(true);
            combobox_EinPAS_G.setMinimumWidth(10);
            combobox_EinPAS_G.setMinimumHeight(10);
            combobox_EinPAS_G.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onChange_CB_G(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag combobox_EinPAS_GConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 1, 1, 1, 0.0, 0.0, "c", "h", 4, 4, 4, 4);
            combobox_EinPAS_G.setConstraints(combobox_EinPAS_GConstraints);
            panel_EinPAS.addChild(combobox_EinPAS_G);
            label_EinPAS_TU = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_EinPAS_TU.setName("label_EinPAS_TU");
            label_EinPAS_TU.__internal_setGenerationDpi(96);
            label_EinPAS_TU.registerTranslationHandler(translationHandler);
            label_EinPAS_TU.setScaleForResolution(true);
            label_EinPAS_TU.setMinimumWidth(10);
            label_EinPAS_TU.setMinimumHeight(10);
            label_EinPAS_TU.setText("!!Technischer Umfang");
            label_EinPAS_TU.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_EinPAS_TUConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 0.0, 0.0, "e", "n", 4, 8, 8, 4);
            label_EinPAS_TU.setConstraints(label_EinPAS_TUConstraints);
            panel_EinPAS.addChild(label_EinPAS_TU);
            combobox_EinPAS_TU = new de.docware.framework.modules.gui.controls.GuiComboBox<de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasNode>();
            combobox_EinPAS_TU.setName("combobox_EinPAS_TU");
            combobox_EinPAS_TU.__internal_setGenerationDpi(96);
            combobox_EinPAS_TU.registerTranslationHandler(translationHandler);
            combobox_EinPAS_TU.setScaleForResolution(true);
            combobox_EinPAS_TU.setMinimumWidth(10);
            combobox_EinPAS_TU.setMinimumHeight(10);
            combobox_EinPAS_TU.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onChange_CB_TU(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag combobox_EinPAS_TUConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 2, 1, 1, 0.0, 0.0, "c", "h", 4, 4, 8, 4);
            combobox_EinPAS_TU.setConstraints(combobox_EinPAS_TUConstraints);
            panel_EinPAS.addChild(combobox_EinPAS_TU);
            button_EinPASChange = new de.docware.framework.modules.gui.controls.GuiButton();
            button_EinPASChange.setName("button_EinPASChange");
            button_EinPASChange.__internal_setGenerationDpi(96);
            button_EinPASChange.registerTranslationHandler(translationHandler);
            button_EinPASChange.setScaleForResolution(true);
            button_EinPASChange.setMinimumWidth(5);
            button_EinPASChange.setMinimumHeight(10);
            button_EinPASChange.setMaximumWidth(30);
            button_EinPASChange.setMaximumHeight(20);
            button_EinPASChange.setMnemonicEnabled(true);
            button_EinPASChange.setText("...");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag button_EinPASChangeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(2, 1, 1, 1, 0.0, 0.0, "c", "n", 0, 0, 0, 0);
            button_EinPASChange.setConstraints(button_EinPASChangeConstraints);
            panel_EinPAS.addChild(button_EinPASChange);
            label_0 = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_0.setName("label_0");
            label_0.__internal_setGenerationDpi(96);
            label_0.registerTranslationHandler(translationHandler);
            label_0.setScaleForResolution(true);
            label_0.setMinimumWidth(10);
            label_0.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_0Constraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(3, 3, 1, 1, 0.0, 100.0, "c", "n", 0, 0, 0, 0);
            label_0.setConstraints(label_0Constraints);
            panel_EinPAS.addChild(label_0);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panel_EinPASConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
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