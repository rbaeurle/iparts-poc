/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumEntry;
import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumValue;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListeners;
import de.docware.util.collections.dwlist.DwList;

import java.util.Collections;
import java.util.List;

/**
 * Dialog zur Eingabe der RFME Flags für aktuelles Teil und Nachfolger
 */
public class EditRFMEFlagsForm extends AbstractJavaViewerForm {

    public static String ENUM_KEY_RFMEA = "RFMEA";
    public static String ENUM_KEY_RFMEN = "RFMEN";

    protected EventListeners eventListeners;
    private boolean forceBlankEntry;
    private boolean isReadOnly;
    private RFMEFlags lastRFMEFlags;

    public EditRFMEFlagsForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        this(dataConnector, parentForm, true, false);
    }

    public EditRFMEFlagsForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, boolean forceBlankEntry, boolean readOnly) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);

        this.forceBlankEntry = forceBlankEntry;
        this.isReadOnly = false;
        eventListeners = new EventListeners();
        this.lastRFMEFlags = new RFMEFlags();

        postCreateGui();
        setReadOnly(readOnly);
    }

    private void postCreateGui() {
        mainWindow.comboboxRFMEA.switchOffEventListeners();
        mainWindow.comboboxRFMEN.switchOffEventListeners();

        mainWindow.comboboxRFMEA.removeAllItems();
        mainWindow.comboboxRFMEN.removeAllItems();

//        mainWindow.comboboxRFMEA.setFontName("Monospaced");
//        mainWindow.comboboxRFMEN.setFontName("Monospaced");

        mainWindow.comboboxRFMEA.addItems(getEnumList(ENUM_KEY_RFMEA));
        mainWindow.comboboxRFMEA.setMaximumRowCount(Math.min(mainWindow.comboboxRFMEA.getItemCount(), 15));

        mainWindow.comboboxRFMEN.addItems(getEnumList(ENUM_KEY_RFMEN));
        mainWindow.comboboxRFMEN.setMaximumRowCount(Math.min(mainWindow.comboboxRFMEN.getItemCount(), 15));

        mainWindow.comboboxRFMEA.switchOnEventListeners();
        mainWindow.comboboxRFMEN.switchOnEventListeners();
    }

    private List<String> getEnumList(String enumKey) {
        List<String> rfmeTexts = new DwList<String>();
        EnumValue rfmeaEmum = getEtkDbs().getEnumValue(enumKey);
        if (rfmeaEmum != null) {
            for (EnumEntry enumEntry : rfmeaEmum.values()) {
                String enumText = enumEntry.getEnumText().getText(Language.DE.getCode());
                rfmeTexts.add(enumText);
            }
            Collections.sort(rfmeTexts);
            if (forceBlankEntry && !rfmeTexts.contains("")) {
                rfmeTexts.add(0, "");
            }
        }
        return rfmeTexts;
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    /**
     * Eventlistener für Änderungen im Dialog setzen
     *
     * @param eventListener
     */
    public void addEventListener(EventListener eventListener) {
        eventListeners.addEventListener(eventListener);
    }

    /**
     * Eventlistener für Änderungen im Dialog löschen
     *
     * @param eventListener
     */
    public void removeEventListener(EventListener eventListener) {
        eventListeners.removeEventListener(eventListener);
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void setReadOnly(boolean readOnly) {
        if (isReadOnly == readOnly) {
            return;
        }
        isReadOnly = readOnly;
        mainWindow.buttonpanel.setButtonVisible(GuiButtonOnPanel.ButtonType.CANCEL, !isReadOnly);
        mainWindow.comboboxRFMEA.setEnabled(!isReadOnly);
        mainWindow.comboboxRFMEN.setEnabled(!isReadOnly);
        if (isReadOnly) {
            mainWindow.buttonpanel.setButtonModalResult(GuiButtonOnPanel.ButtonType.OK, ModalResult.CANCEL);
            mainWindow.buttonpanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, true);
        } else {
            mainWindow.buttonpanel.setButtonModalResult(GuiButtonOnPanel.ButtonType.OK, ModalResult.OK);
            enableButtons();
        }
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

    private void onChangeComboboxRFMEA(Event event) {
        fireOnChangeEvent(null);

    }

    private void onChangeComboboxRFMEN(Event event) {
        fireOnChangeEvent(null);
    }

    /**
     * Test, ob die Eingaben gültig sind
     *
     * @return
     */
    public boolean isValid() {
        // aktuell darf der Benutzer alles eingeben
        return true;
    }

    public boolean isChanged() {
        RFMEFlags currentRFMEFlags = getCurrentRFMEFlags();
        return !lastRFMEFlags.isEqualTo(currentRFMEFlags);
    }

    private void enableButtons() {
        if (!isReadOnly) {
            mainWindow.buttonpanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, isValid() && isChanged());
        }
    }

    public ModalResult showModal() {
        return mainWindow.showModal();
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public void init(String rfmea, String rfmen) {
        if ((rfmea == null) && (rfmen == null)) {
            return;
        }
        mainWindow.comboboxRFMEA.switchOffEventListeners();
        mainWindow.comboboxRFMEN.switchOffEventListeners();

        if (rfmea != null) {
            mainWindow.comboboxRFMEA.setSelectedItem(rfmea.replace(' ', '-'));
        }
        if (rfmen != null) {
            mainWindow.comboboxRFMEN.setSelectedItem(rfmen.replace(' ', '-'));
        }

        mainWindow.comboboxRFMEA.switchOnEventListeners();
        mainWindow.comboboxRFMEN.switchOnEventListeners();

        lastRFMEFlags.rfmeaFlags = getSelectedRFMEA();
        lastRFMEFlags.rfmenFlags = getSelectedRFMEN();
    }

    public String getSelectedRFMEA() {
        return mainWindow.comboboxRFMEA.getSelectedItem().replace('-', ' ');
    }

    public String getSelectedRFMEN() {
        return mainWindow.comboboxRFMEN.getSelectedItem().replace('-', ' ');
    }

    private RFMEFlags getCurrentRFMEFlags() {
        RFMEFlags currentRFMEFlags = new RFMEFlags();
        currentRFMEFlags.rfmeaFlags = getSelectedRFMEA();
        currentRFMEFlags.rfmenFlags = getSelectedRFMEN();
        return currentRFMEFlags;
    }

    private class RFMEFlags {

        public String rfmeaFlags = "";
        public String rfmenFlags = "";

        public boolean isEqualTo(RFMEFlags testRFMEFlags) {
            return rfmeaFlags.equals(testRFMEFlags.rfmeaFlags) && rfmenFlags.equals(testRFMEFlags.rfmenFlags);
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
        private de.docware.framework.modules.gui.controls.GuiLabel labelRFMEA;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<String> comboboxRFMEA;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelRFMEN;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<String> comboboxRFMEN;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
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
            panelMain = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelMain.setName("panelMain");
            panelMain.__internal_setGenerationDpi(96);
            panelMain.registerTranslationHandler(translationHandler);
            panelMain.setScaleForResolution(true);
            panelMain.setMinimumWidth(10);
            panelMain.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelMainLayout.setCentered(false);
            panelMain.setLayout(panelMainLayout);
            labelRFMEA = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelRFMEA.setName("labelRFMEA");
            labelRFMEA.__internal_setGenerationDpi(96);
            labelRFMEA.registerTranslationHandler(translationHandler);
            labelRFMEA.setScaleForResolution(true);
            labelRFMEA.setMinimumWidth(10);
            labelRFMEA.setMinimumHeight(10);
            labelRFMEA.setText("!!RFME Aktuell");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelRFMEAConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "c", "n", 4, 8, 4, 8);
            labelRFMEA.setConstraints(labelRFMEAConstraints);
            panelMain.addChild(labelRFMEA);
            comboboxRFMEA = new de.docware.framework.modules.gui.controls.GuiComboBox<String>();
            comboboxRFMEA.setName("comboboxRFMEA");
            comboboxRFMEA.__internal_setGenerationDpi(96);
            comboboxRFMEA.registerTranslationHandler(translationHandler);
            comboboxRFMEA.setScaleForResolution(true);
            comboboxRFMEA.setMinimumWidth(70);
            comboboxRFMEA.setMinimumHeight(10);
            comboboxRFMEA.setMaximumWidth(100);
            comboboxRFMEA.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onChangeComboboxRFMEA(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag comboboxRFMEAConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, "c", "n", 4, 8, 4, 8);
            comboboxRFMEA.setConstraints(comboboxRFMEAConstraints);
            panelMain.addChild(comboboxRFMEA);
            labelRFMEN = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelRFMEN.setName("labelRFMEN");
            labelRFMEN.__internal_setGenerationDpi(96);
            labelRFMEN.registerTranslationHandler(translationHandler);
            labelRFMEN.setScaleForResolution(true);
            labelRFMEN.setMinimumWidth(10);
            labelRFMEN.setMinimumHeight(10);
            labelRFMEN.setText("!!RFME Nachfolger");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelRFMENConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(3, 0, 1, 1, 0.0, 0.0, "c", "n", 4, 8, 4, 8);
            labelRFMEN.setConstraints(labelRFMENConstraints);
            panelMain.addChild(labelRFMEN);
            comboboxRFMEN = new de.docware.framework.modules.gui.controls.GuiComboBox<String>();
            comboboxRFMEN.setName("comboboxRFMEN");
            comboboxRFMEN.__internal_setGenerationDpi(96);
            comboboxRFMEN.registerTranslationHandler(translationHandler);
            comboboxRFMEN.setScaleForResolution(true);
            comboboxRFMEN.setMinimumWidth(70);
            comboboxRFMEN.setMinimumHeight(10);
            comboboxRFMEN.setMaximumWidth(100);
            comboboxRFMEN.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onChangeComboboxRFMEN(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag comboboxRFMENConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(4, 0, 1, 1, 0.0, 0.0, "c", "n", 4, 8, 4, 8);
            comboboxRFMEN.setConstraints(comboboxRFMENConstraints);
            panelMain.addChild(comboboxRFMEN);
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